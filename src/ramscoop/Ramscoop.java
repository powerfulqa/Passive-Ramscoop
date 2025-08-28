package ramscoop;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import org.apache.log4j.Logger;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.fleet.MutableFleetStatsAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.CampaignTerrainAPI;
import com.fs.starfarer.api.campaign.CampaignTerrainPlugin;
import com.fs.starfarer.api.campaign.PlanetAPI;
import org.lwjgl.util.vector.Vector2f;
import java.util.Locale;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import com.fs.starfarer.api.util.IntervalUtil;

public class Ramscoop implements EveryFrameScript {
   private static final Logger LOG = Global.getLogger(Ramscoop.class);
   private float settingsCheckTimer = 0f;
   private static final float SETTINGS_CHECK_INTERVAL = 10f;
   
   // Store settings used by generation
   private boolean enable_fuel = true;
   private boolean enable_supplies = true;
   private float fuel_per_day = 0.1f;
   private float supplies_per_crew = 0.1f;
   private float percent_supply_limit = 0.35f;
   private float hard_supply_limit = 0.0f;
   private String crew_usage = "extra";
   private String no_crew_gen = "percent";
   private float no_crew_rate = 0.1f;
   
   // Load legacy config only once if LunaLib is not enabled
   private boolean jsonLoaded = false;
   
   private final IntervalUtil interval = new IntervalUtil(0.09f, 0.11f); // ~0.1 days; adjust if needed for balance.
   private float elapsedSinceTick = 0f;
   
   public Ramscoop() {
      LOG.info("[Ramscoop] Initialized");
      // Proactively ask plugin to attempt loading if not done yet
      try { ramscoop.ModPlugin.reloadSettings(); } catch (Throwable ignored) {}
   }

   public boolean isNebula(StatMod mod) {
      return mod != null && mod.source != null && mod.source.contains("nebula_stat_mod");
   }

   private void loadSettingsFromJsonOnceIfNeeded() {
      if (jsonLoaded) return;
      try {
         org.json.JSONObject config = Global.getSettings().loadJSON("settings.json", "m561_ramscoop");
         enable_fuel = config.getBoolean("enable_fuel");
         enable_supplies = config.getBoolean("enable_supplies");
         if (config.has("fuel_per_day")) {
            fuel_per_day = (float) config.getDouble("fuel_per_day");
         }
         supplies_per_crew = (float) config.getDouble("supply_per_crew");
         percent_supply_limit = (float) config.getDouble("percent_supply_limit");
         hard_supply_limit = (float) config.getDouble("hard_supply_limit");
         crew_usage = config.get("crew_usage").toString();
         no_crew_gen = config.get("no_crew_gen").toString();
         no_crew_rate = (float) config.getDouble("no_crew_rate");
         System.out.println("Ramscoop: Settings loaded (settings.json) fuel=" + enable_fuel + " supplies=" + enable_supplies);
      } catch (Throwable t) {
         System.out.println("Ramscoop: Failed to load settings.json: " + t.getMessage());
      } finally {
         jsonLoaded = true;
      }
   }

   // Wrap advance contents in try-catch to fix syntax error
   public void advance(float amount) {
      try {
         interval.advance(amount);
         elapsedSinceTick += amount;
         if (!interval.intervalElapsed()) return; // Skip until tick fires
         float daysElapsed = Global.getSector().getClock().convertToDays(elapsedSinceTick);
         elapsedSinceTick = 0f;

         // Pull settings less often (cache locally; update only on interval)
         enable_fuel = ModPlugin.enable_fuel;
         enable_supplies = ModPlugin.enable_supplies;
         fuel_per_day = ModPlugin.fuel_per_day;
         supplies_per_crew = ModPlugin.supplies_per_crew;
         percent_supply_limit = ModPlugin.percent_supply_limit;
         hard_supply_limit = ModPlugin.hard_supply_limit;
         crew_usage = ModPlugin.crew_usage;
         no_crew_gen = ModPlugin.no_crew_gen;
         no_crew_rate = ModPlugin.no_crew_rate;

         // Debug: Always log that we're running (but throttle it)
            // one-time trace per cycle kept minimal
         
          // Periodically retry loading settings if not yet loaded (or LunaLib not yet ready)
          settingsCheckTimer += amount;
          if (settingsCheckTimer >= SETTINGS_CHECK_INTERVAL) {
             settingsCheckTimer = 0f;
             try { ramscoop.ModPlugin.reloadSettings(); } catch (Throwable ignored) {}
          }
         
         CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
         if (fleet == null) {
            return;
         }

          // Trimmed per-frame logging

         FleetDataAPI fleetData = fleet.getFleetData();
         StatMod nebulaMod = null;
         MutableFleetStatsAPI stats = fleetData.getFleet().getStats();
         HashMap<String, StatMod> mods = stats.getFleetwideMaxBurnMod().getMultBonuses();
         float fuel = fleet.getCargo().getFuel();
         float supplies = fleet.getCargo().getSupplies();
         float minimumcrew = fleet.getFleetData().getMinCrew();
         float currentcrew = (float)fleet.getCargo().getCrew();
         float extracrew = 0.0F;
         double suppliesperdayd = 0.0D;
         float suppliesperday = 0.0F;
         float minspace = 0.0F;
         double fuelperdayd = Math.floor((double)(fleet.getCargo().getMaxFuel() * fuel_per_day));
         float fuelperday = (float)fuelperdayd;
         double maxsuppliesd = Math.floor((double)(fleet.getCargo().getMaxCapacity() * percent_supply_limit));
         float maxpercentsupplies = (float)maxsuppliesd;
         float maxsupplies = 0.0F;
         
         // Check for nebula status
         for (Entry<String, StatMod> mod : mods.entrySet()) {
            StatMod statMod = mod.getValue();
            if (this.isNebula(statMod)) {
               nebulaMod = statMod;
               break;
            }
         }

         if (hard_supply_limit == 0.0F) {
            maxsupplies = maxpercentsupplies;
         } else {
            maxsupplies = Math.min(maxpercentsupplies, hard_supply_limit);
         }

         // Restore scoop toggle check (from original line 210)
         boolean scoopEnabled = true;
         try { scoopEnabled = fleet.getMemoryWithoutUpdate().getBoolean("$ramscoop_enabled"); } catch (Throwable ignored) {}

         // Restore corona detection and generation (from original lines 214-310), with caps
         boolean inCorona = false;
         try {
             LocationAPI loc = fleet.getContainingLocation();
             if (loc != null) {
                 for (CampaignTerrainAPI t : loc.getTerrainCopy()) {
                     boolean looksCorona = false;
                     try {
                         String type = t.getType();
                         looksCorona = type != null && type.toLowerCase(Locale.ROOT).contains("corona");
                     } catch (Throwable ignored2) {}
                     if (!looksCorona) {
                         try {
                             Object plugin = t.getPlugin();
                             if (plugin != null) {
                                 String cn = plugin.getClass().getName().toLowerCase(Locale.ROOT);
                                 looksCorona = cn.contains("corona");
                             }
                         } catch (Throwable ignored3) {}
                     }
                     if (looksCorona) {
                         try {
                             Object pluginObj = t.getPlugin();
                             if (pluginObj != null) {
                                 // Try containsEntity
                                 try {
                                     Class<?> setClazz = Class.forName("com.fs.starfarer.api.campaign.SectorEntityToken");
                                     java.lang.reflect.Method m = pluginObj.getClass().getMethod("containsEntity", setClazz);
                                     Object r = m.invoke(pluginObj, fleet);
                                     if (r instanceof Boolean && ((Boolean) r)) { inCorona = true; break; }
                                 } catch (Throwable ignoredCE) {}
                                 // Try containsPoint
                                 try {
                                     Class<?> v2 = Class.forName("org.lwjgl.util.vector.Vector2f");
                                     java.lang.reflect.Method m2 = pluginObj.getClass().getMethod("containsPoint", v2);
                                     Object r2 = m2.invoke(pluginObj, fleet.getLocation());
                                     if (r2 instanceof Boolean && ((Boolean) r2)) { inCorona = true; break; }
                                 } catch (Throwable ignoredCP) {}
                             }
                         } catch (Throwable ignored4) {}
                     }
                 }
                 // Fallback: distance to star
                 if (!inCorona) {
                     try {
                         java.util.List<PlanetAPI> planets = loc.getPlanets();
                         for (PlanetAPI p : planets) {
                             try {
                                 if (p.isStar()) {
                                     Vector2f fp = fleet.getLocation();
                                     Vector2f sp = p.getLocation();
                                     float dx = fp.x - sp.x;
                                     float dy = fp.y - sp.y;
                                     float dist = (float)Math.sqrt(dx*dx + dy*dy);
                                     float buffer = 1000f;
                                     if (dist <= p.getRadius() + buffer) { inCorona = true; break; }
                                 }
                             } catch (Throwable ignoredStar) {}
                         }
                     } catch (Throwable ignoredPlanets) {}
                 }
             }
         } catch (Throwable ignored) {}

         // Corona behavior (takes precedence if detected)
         if (inCorona) {
             float days = daysElapsed;
             // Fuel: generate faster in corona if enabled
             if (ModPlugin.corona_enable_fuel && enable_fuel && scoopEnabled) {
                 float maxFuel = fleet.getCargo().getMaxFuel();
                 double coronaPerDayD = Math.floor((double)(maxFuel * ModPlugin.corona_fuel_per_day));
                 float coronaPerDay = (float)coronaPerDayD;
                 float add = coronaPerDay * days;
                 // Use corona caps
                 float coronaSoft = (float)Math.floor(maxFuel * ModPlugin.corona_percent_fuel_limit);
                 float coronaHard = ModPlugin.corona_hard_fuel_limit;
                 float coronaMargin = ModPlugin.corona_fuel_cap_margin;
                 float hardCap = coronaHard > 0f ? coronaHard : Float.MAX_VALUE;
                 float targetCap = Math.min(maxFuel, Math.min(coronaSoft, hardCap));
                 float margin = Math.max(0f, coronaMargin);
                 // Diagnostic log per prompt (minimal)
                 LOG.info("[Ramscoop] Corona mode: add=" + add + ", soft=" + coronaSoft + ", hard=" + coronaHard + ", margin=" + margin + ", fuel=" + fuel + ", target=" + targetCap);
                 if (fuel < targetCap - margin) {
                     float remaining = Math.max(0f, (targetCap - margin) - fuel);
                     float fuelToAdd = Math.min(add, remaining);
                     if (fuelToAdd > 0f) {
                         fleet.getCargo().addFuel(fuelToAdd);
                     }
                 }
             }
             return;
         }

         // Adjust structure: Move fuel logic inside nebula if, declare days once per block

         // Nebula block
         if (nebulaMod != null) {
             float days = daysElapsed; // aggregated time since last tick

             // Supplies
             if (!enable_supplies || !scoopEnabled) {
                 LOG.info("[Ramscoop] Supplies disabled (nebula present or scoop off)");
             } else {
                 // Calculate supplies generation based on crew settings
                 switch (crew_usage) {
                    case "nocrew":
                       switch (no_crew_gen) {
                          case "percent":
                             suppliesperdayd = Math.floor((double)(fleet.getCargo().getMaxCapacity() * no_crew_rate));
                             suppliesperday = (float)suppliesperdayd;
                             break;
                          case "flat":
                             suppliesperday = no_crew_rate;
                             break;
                       }
                       break;
                    case "all":
                       suppliesperday = currentcrew * supplies_per_crew;
                       break;
                    case "extra":
                       if (currentcrew > minimumcrew) {
                          extracrew = currentcrew - minimumcrew;
                          suppliesperday = extracrew * supplies_per_crew;
                       }
                       break;
                 }

                 // Calculate available space for supplies
                 if (supplies < maxsupplies) {
                    minspace = Math.min(maxsupplies - supplies, fleet.getCargo().getSpaceLeft());
                 } else {
                    minspace = fleet.getCargo().getSpaceLeft();
                 }

                 // Add supplies based on available space
                 if (fleet.getCargo().getSpaceLeft() > 0.0F && suppliesperday > 0.0F && supplies < maxsupplies) {
                    float suppliesToAdd;
                    if (suppliesperday * days < minspace) {
                       suppliesToAdd = suppliesperday * days;
                       fleet.getCargo().addSupplies(suppliesToAdd);
                    } else {
                       suppliesToAdd = minspace;
                       fleet.getCargo().addSupplies(suppliesToAdd);
                    }
                     // Optional: keep minimal meaningful trace
                     if (suppliesToAdd > 0.5f) { LOG.info("[Ramscoop] Added supplies: " + suppliesToAdd); }
                 }
             }

             // Fuel (move try inside)
             try {
                 if (enable_fuel && scoopEnabled) {
                     // No need to redeclare days; use the one above
                     float maxFuel = fleet.getCargo().getMaxFuel();
                     float nebSoft = (float)Math.floor(maxFuel * ModPlugin.nebula_percent_fuel_limit);
                     float nebHardCfg = ModPlugin.nebula_hard_fuel_limit;
                     float nebMargin = ModPlugin.nebula_fuel_cap_margin;
                     float hardCap = nebHardCfg > 0f ? nebHardCfg : Float.MAX_VALUE;
                     float targetCap = Math.min(maxFuel, Math.min(nebSoft, hardCap));
                     float margin = Math.max(0f, nebMargin);
                     if (fuel < targetCap - margin) {
                         float remaining = Math.max(0f, (targetCap - margin) - fuel);
                         float fuelToAdd = Math.min(fuelperday * days, remaining);
                         if (fuelToAdd > 0f) {
                             fleet.getCargo().addFuel(fuelToAdd);
                             if (fuelToAdd > 0.5f) { LOG.info("[Ramscoop] Added fuel: " + fuelToAdd); }
                         }
                     }
                 } else if (!enable_fuel) {
                     LOG.info("[Ramscoop] Fuel generation disabled");
                 }
             } catch (Throwable t) {
                 // Non-fatal
             }
         }
      } catch (Exception e) {
         // Use simple println for logging errors to avoid log4j dependency issues
         System.out.println("Ramscoop: Error in advance method - " + e.getMessage());
         e.printStackTrace();
      }
   }

   public boolean isDone() {
      return false;
   }

   public boolean runWhilePaused() {
      return false;
   }
}
