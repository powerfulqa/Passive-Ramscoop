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
         if (!interval.intervalElapsed()) return; // Skip if not time (efficiency; no behavior change)

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

         if (nebulaMod != null) {
            float days;
            // Absolute guard: never generate supplies when disabled
            if (!enable_supplies) {
                // keep a clear trace once per reload cycle
                LOG.info("[Ramscoop] Supplies disabled (nebula present)");
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
                  days = Global.getSector().getClock().convertToDays(amount);
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

            // Generate fuel if enabled and not at max capacity
            if (enable_fuel && fuel < fleet.getCargo().getMaxFuel()) {
               days = Global.getSector().getClock().convertToDays(amount);
               float fuelToAdd = fuelperday * days;
               fleet.getCargo().addFuel(fuelToAdd);
                if (fuelToAdd > 0.5f) { LOG.info("[Ramscoop] Added fuel: " + fuelToAdd); }
            } else if (!enable_fuel) {
               LOG.info("[Ramscoop] Fuel generation disabled");
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
