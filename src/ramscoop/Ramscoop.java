package ramscoop;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.fleet.MutableFleetStatsAPI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class Ramscoop implements EveryFrameScript {
   private float settingsCheckTimer = 0f;
   private static final float SETTINGS_CHECK_INTERVAL = 5f; // Check every 5 seconds
   
   public Ramscoop() {
      System.out.println("Ramscoop: Ramscoop constructor called");
      System.out.println("Ramscoop: Initial settings - fuel: " + ModPlugin.enable_fuel + ", supplies: " + ModPlugin.enable_supplies);
   }
   
   public boolean isNebula(StatMod mod) {
      return mod != null && mod.source != null && mod.source.contains("nebula_stat_mod");
   }

   private void reloadSettingsIfNeeded() {
      try {
         // Check if LunaLib is available and reload settings
         if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            // Use reflection to call ModPlugin.loadSettings() - we need to make it public
            Class<?> modPluginClass = Class.forName("ramscoop.ModPlugin");
            Object modPluginInstance = modPluginClass.newInstance();
            modPluginClass.getMethod("reloadSettings").invoke(modPluginInstance);
         }
      } catch (Exception e) {
         // Silently ignore errors - settings reloading is not critical
      }
   }

   public void advance(float amount) {
      try {
         // Debug: Always log that we're running (but throttle it)
         if (settingsCheckTimer == 0f) { // Only log at start and after timer resets
            System.out.println("Ramscoop: advance() called - fuel enabled: " + ModPlugin.enable_fuel + ", supplies enabled: " + ModPlugin.enable_supplies);
         }
         
         // Periodically reload settings to pick up changes from LunaLib
         settingsCheckTimer += amount;
         if (settingsCheckTimer >= SETTINGS_CHECK_INTERVAL) {
            settingsCheckTimer = 0f;
            reloadSettingsIfNeeded();
         }
         
         CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
         if (fleet == null) {
            return;
         }

         // Debug: Log current settings state periodically (every 30 seconds)
         if (settingsCheckTimer < 1f) { // Only log once per reload cycle
            System.out.println("Ramscoop: Current settings - fuel: " + ModPlugin.enable_fuel + ", supplies: " + ModPlugin.enable_supplies);
         }

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
         double fuelperdayd = Math.floor((double)(fleet.getCargo().getMaxFuel() * ModPlugin.fuel_per_day));
         float fuelperday = (float)fuelperdayd;
         double maxsuppliesd = Math.floor((double)(fleet.getCargo().getMaxCapacity() * ModPlugin.percent_supply_limit));
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

         if (ModPlugin.hard_supply_limit == 0.0F) {
            maxsupplies = maxpercentsupplies;
         } else {
            maxsupplies = Math.min(maxpercentsupplies, ModPlugin.hard_supply_limit);
         }

         if (nebulaMod != null) {
            float days;
            if (ModPlugin.enable_supplies) {
               // Calculate supplies generation based on crew settings
               switch (ModPlugin.crew_usage) {
                  case "nocrew":
                     switch (ModPlugin.no_crew_gen) {
                        case "percent":
                           suppliesperdayd = Math.floor((double)(fleet.getCargo().getMaxCapacity() * ModPlugin.no_crew_rate));
                           suppliesperday = (float)suppliesperdayd;
                           break;
                        case "flat":
                           suppliesperday = ModPlugin.no_crew_rate;
                           break;
                     }
                     break;
                  case "all":
                     suppliesperday = currentcrew * ModPlugin.supplies_per_crew;
                     break;
                  case "extra":
                     if (currentcrew > minimumcrew) {
                        extracrew = currentcrew - minimumcrew;
                        suppliesperday = extracrew * ModPlugin.supplies_per_crew;
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
                  // Debug logging
                  if (suppliesToAdd > 0.001f) { // Only log meaningful amounts
                     System.out.println("Ramscoop: Added " + suppliesToAdd + " supplies (enabled: " + ModPlugin.enable_supplies + ")");
                  }
               } else if (!ModPlugin.enable_supplies) {
                  // Debug: Log when supplies generation is disabled
                  System.out.println("Ramscoop: Supplies generation disabled in settings");
               }
            }

            // Generate fuel if enabled and not at max capacity
            if (ModPlugin.enable_fuel && fuel < fleet.getCargo().getMaxFuel()) {
               days = Global.getSector().getClock().convertToDays(amount);
               float fuelToAdd = fuelperday * days;
               fleet.getCargo().addFuel(fuelToAdd);
               // Debug logging
               if (fuelToAdd > 0.001f) { // Only log meaningful amounts
                  System.out.println("Ramscoop: Added " + fuelToAdd + " fuel (enabled: " + ModPlugin.enable_fuel + ")");
               }
            } else if (!ModPlugin.enable_fuel) {
               // Debug: Log when fuel generation is disabled
               System.out.println("Ramscoop: Fuel generation disabled in settings");
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
