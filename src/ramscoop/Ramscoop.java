package ramscoop;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import org.apache.log4j.Logger;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.fleet.MutableFleetStatsAPI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

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
   
   public Ramscoop() {
      LOG.info("[Ramscoop] Initialized");
      // Proactively ask plugin to attempt loading if not done yet
      try { ramscoop.ModPlugin.reloadSettings(); } catch (Throwable ignored) {}
   }

    // Lightweight file-based debug logger to ensure messages are captured even if stdout is filtered
     private void debugLog(String message) { /* trimmed */ }
   
   public boolean isNebula(StatMod mod) {
      return mod != null && mod.source != null && mod.source.contains("nebula_stat_mod");
   }

   // Reflective helpers to support LunaLib API variations (2-arg or 3-arg with default)
   private static boolean reflectGetBoolean(Object target, Class<?> clazz, String method, String modId, String key, boolean defVal) throws Exception {
      try {
         return (Boolean) clazz.getMethod(method, String.class, String.class).invoke(target, modId, key);
      } catch (NoSuchMethodException nsme) {
         try {
            return (Boolean) clazz.getMethod(method, String.class, String.class, boolean.class).invoke(target, modId, key, defVal);
         } catch (NoSuchMethodException nsme2) {
            return (Boolean) clazz.getMethod(method, String.class, String.class, Boolean.class).invoke(target, modId, key, Boolean.valueOf(defVal));
         }
      }
   }
   private static double reflectGetDouble(Object target, Class<?> clazz, String method, String modId, String key, double defVal) throws Exception {
      try {
         return (Double) clazz.getMethod(method, String.class, String.class).invoke(target, modId, key);
      } catch (NoSuchMethodException nsme) {
         try {
            return (Double) clazz.getMethod(method, String.class, String.class, double.class).invoke(target, modId, key, defVal);
         } catch (NoSuchMethodException nsme2) {
            return (Double) clazz.getMethod(method, String.class, String.class, Double.class).invoke(target, modId, key, Double.valueOf(defVal));
         }
      }
   }
   private static String reflectGetString(Object target, Class<?> clazz, String method, String modId, String key, String defVal) throws Exception {
      try {
         return (String) clazz.getMethod(method, String.class, String.class).invoke(target, modId, key);
      } catch (NoSuchMethodException nsme) {
         return (String) clazz.getMethod(method, String.class, String.class, String.class).invoke(target, modId, key, defVal);
      }
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

   private void readLunaLibEveryFrame() {
      try {
         Class<?> lunaSettingsClass = Class.forName("lunalib.lunaSettings.LunaSettings");
         String modId = "m561_ramscoop";
         // Try static first
         try {
            enable_fuel = reflectGetBoolean(null, lunaSettingsClass, "getBoolean", modId, "ramscoop_enable_fuel", enable_fuel);
             // Default supplies to false if read fails, to avoid accidental generation
             enable_supplies = reflectGetBoolean(null, lunaSettingsClass, "getBoolean", modId, "ramscoop_enable_supplies", false);
            fuel_per_day = (float) reflectGetDouble(null, lunaSettingsClass, "getDouble", modId, "ramscoop_fuel_per_day", fuel_per_day);
            percent_supply_limit = (float) reflectGetDouble(null, lunaSettingsClass, "getDouble", modId, "ramscoop_percent_supply_limit", percent_supply_limit);
            hard_supply_limit = (float) reflectGetDouble(null, lunaSettingsClass, "getDouble", modId, "ramscoop_hard_supply_limit", hard_supply_limit);
            supplies_per_crew = (float) reflectGetDouble(null, lunaSettingsClass, "getDouble", modId, "ramscoop_supply_per_crew", supplies_per_crew);
            crew_usage = reflectGetString(null, lunaSettingsClass, "getString", modId, "ramscoop_crew_usage", crew_usage);
            no_crew_gen = reflectGetString(null, lunaSettingsClass, "getString", modId, "ramscoop_no_crew_gen", no_crew_gen);
            no_crew_rate = (float) reflectGetDouble(null, lunaSettingsClass, "getDouble", modId, "ramscoop_no_crew_rate", no_crew_rate);
             debugLog("LunaLib read (static): fuel=" + enable_fuel + ", supplies=" + enable_supplies);
            return;
         } catch (Throwable staticFail) {
            // Fallback to instance
            Object inst;
            try {
               inst = lunaSettingsClass.getField("INSTANCE").get(null);
            } catch (NoSuchFieldException nsf) {
               inst = lunaSettingsClass.newInstance();
            }
            enable_fuel = reflectGetBoolean(inst, lunaSettingsClass, "getBoolean", modId, "ramscoop_enable_fuel", enable_fuel);
             // Default supplies to false if read fails, to avoid accidental generation
             enable_supplies = reflectGetBoolean(inst, lunaSettingsClass, "getBoolean", modId, "ramscoop_enable_supplies", false);
            fuel_per_day = (float) reflectGetDouble(inst, lunaSettingsClass, "getDouble", modId, "ramscoop_fuel_per_day", fuel_per_day);
            percent_supply_limit = (float) reflectGetDouble(inst, lunaSettingsClass, "getDouble", modId, "ramscoop_percent_supply_limit", percent_supply_limit);
            hard_supply_limit = (float) reflectGetDouble(inst, lunaSettingsClass, "getDouble", modId, "ramscoop_hard_supply_limit", hard_supply_limit);
            supplies_per_crew = (float) reflectGetDouble(inst, lunaSettingsClass, "getDouble", modId, "ramscoop_supply_per_crew", supplies_per_crew);
            crew_usage = reflectGetString(inst, lunaSettingsClass, "getString", modId, "ramscoop_crew_usage", crew_usage);
            no_crew_gen = reflectGetString(inst, lunaSettingsClass, "getString", modId, "ramscoop_no_crew_gen", no_crew_gen);
            no_crew_rate = (float) reflectGetDouble(inst, lunaSettingsClass, "getDouble", modId, "ramscoop_no_crew_rate", no_crew_rate);
             debugLog("LunaLib read (instance): fuel=" + enable_fuel + ", supplies=" + enable_supplies);
         }
      } catch (ClassNotFoundException e) {
         // LunaLib not present
      } catch (Throwable t) {
         // Keep previous values on error
      }
   }

   public void advance(float amount) {
      try {
			// Read current settings from ModPlugin (single source of truth)
			enable_fuel = ModPlugin.enable_fuel;
			enable_supplies = ModPlugin.enable_supplies;
			fuel_per_day = ModPlugin.fuel_per_day;
			supplies_per_crew = ModPlugin.supplies_per_crew;
			percent_supply_limit = ModPlugin.percent_supply_limit;
			hard_supply_limit = ModPlugin.hard_supply_limit;
			crew_usage = ModPlugin.crew_usage;
			no_crew_gen = ModPlugin.no_crew_gen;
			no_crew_rate = ModPlugin.no_crew_rate;
			debugLog("Pulled settings from ModPlugin: fuel=" + enable_fuel + ", supplies=" + enable_supplies);

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
