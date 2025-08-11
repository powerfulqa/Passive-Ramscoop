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
   public boolean isNebula(StatMod mod) {
      return mod != null && mod.source != null && mod.source.contains("nebula_stat_mod");
   }

   public void advance(float amount) {
      try {
         CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
         if (fleet == null) {
            return;
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
                  if (suppliesperday * days < minspace) {
                     fleet.getCargo().addSupplies(suppliesperday * days);
                  } else {
                     fleet.getCargo().addSupplies(minspace);
                  }
               }
            }

            // Generate fuel if enabled and not at max capacity
            if (ModPlugin.enable_fuel && fuel < fleet.getCargo().getMaxFuel()) {
               days = Global.getSector().getClock().convertToDays(amount);
               fleet.getCargo().addFuel(fuelperday * days);
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
