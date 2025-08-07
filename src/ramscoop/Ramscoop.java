package ramscoop;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.fleet.MutableFleetStatsAPI;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Ramscoop implements EveryFrameScript, Serializable {
    
    // Add serialVersionUID to ensure compatibility across versions
    private static final long serialVersionUID = 1L;
    
    // Make fields transient to prevent serialization issues
    private transient float daysAccumulator = 0f;
    
    // Create a placeholder ArrayList field to handle deserialization
    private ArrayList<?> compatibilityList = new ArrayList<>();
    
    /**
     * Checks if a stat mod is from a nebula effect.
     * @param mod The stat mod to check
     * @return true if it's a nebula stat mod
     */
    public boolean isNebula(MutableStat.StatMod mod) {
        return mod != null && mod.source != null && mod.source.contains("nebula_stat_mod");
    }

    @Override
    public void advance(float amount) {
        try {
            // Get the player fleet
            CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
            if (fleet == null) {
                return;
            }
            
            // Get fleet data
            FleetDataAPI fleetData = fleet.getFleetData();
            MutableStat.StatMod nebulaMod = null;
            MutableFleetStatsAPI stats = fleetData.getFleet().getStats();
            
            // In 0.98aRC8, make sure to properly use generics for better type safety
            @SuppressWarnings("unchecked")
            Map<String, MutableStat.StatMod> mods = stats.getFleetwideMaxBurnMod().getMultBonuses();
            
            // Get current resources
            float fuel = fleet.getCargo().getFuel();
            float supplies = fleet.getCargo().getSupplies();
            float minimumcrew = fleetData.getMinCrew();
            float currentcrew = fleet.getCargo().getCrew();
            float extracrew = 0.0f;
            double suppliesperdayd = 0.0;
            float suppliesperday = 0.0f;
            float minspace = 0.0f;
            
            // Calculate fuel generation rate (updated for 0.98aRC8)
            double fuelperdayd = Math.floor(fleet.getCargo().getMaxFuel() * ModPlugin.fuel_per_day);
            float fuelperday = (float)fuelperdayd;
            
            // Calculate max supplies
            double maxsuppliesd = Math.floor(fleet.getCargo().getMaxCapacity() * ModPlugin.percent_supply_limit);
            float maxpercentsupplies = (float)maxsuppliesd;
            float maxsupplies = 0.0f;
            
            // Check if we're in a nebula by looking for the nebula stat mod
            for (Map.Entry<String, MutableStat.StatMod> entry : mods.entrySet()) {
                MutableStat.StatMod statMod = entry.getValue();
                if (isNebula(statMod)) {
                    nebulaMod = statMod;
                    break;
                }
            }
            
            // Set max supplies based on configured limits
            maxsupplies = ModPlugin.hard_supply_limit == 0.0f ? 
                maxpercentsupplies : 
                Math.min(maxpercentsupplies, ModPlugin.hard_supply_limit);
            
            // Only process if we're in a nebula
            if (nebulaMod != null) {
                // Handle supply generation if enabled
                if (ModPlugin.enable_supplies) {
                    if ("extra".equals(ModPlugin.crew_usage)) {
                        if (currentcrew > minimumcrew) {
                            extracrew = currentcrew - minimumcrew;
                            suppliesperday = extracrew * ModPlugin.supplies_per_crew;
                        }
                    } else if ("all".equals(ModPlugin.crew_usage)) {
                        suppliesperday = currentcrew * ModPlugin.supplies_per_crew;
                    } else if ("nocrew".equals(ModPlugin.crew_usage)) {
                        if ("percent".equals(ModPlugin.no_crew_gen)) {
                            suppliesperdayd = Math.floor(fleet.getCargo().getMaxCapacity() * ModPlugin.no_crew_rate);
                            suppliesperday = (float)suppliesperdayd;
                        } else if ("flat".equals(ModPlugin.no_crew_gen)) {
                            suppliesperday = ModPlugin.no_crew_rate;
                        }
                    }
                    
                    // Calculate available space
                    minspace = supplies < maxsupplies ? 
                        Math.min(maxsupplies - supplies, fleet.getCargo().getSpaceLeft()) : 
                        fleet.getCargo().getSpaceLeft();
                    
                    // Add supplies based on time passed and available space
                    if (fleet.getCargo().getSpaceLeft() > 0.0f && suppliesperday > 0.0f && supplies < maxsupplies) {
                        float days = Global.getSector().getClock().convertToDays(amount);
                        if (suppliesperday * days < minspace) {
                            fleet.getCargo().addSupplies(suppliesperday * days);
                        } else {
                            fleet.getCargo().addSupplies(minspace);
                        }
                    }
                }
                
                // Handle fuel generation if enabled and not at max capacity
                if (ModPlugin.enable_fuel && fuel < fleet.getCargo().getMaxFuel()) {
                    float days = Global.getSector().getClock().convertToDays(amount);
                    fleet.getCargo().addFuel(fuelperday * days);
                }
            }
        } catch (Exception e) {
            // Log the exception for debugging
            Global.getLogger(Ramscoop.class).error("Error in Ramscoop.advance", e);
        }
    }

    @Override
    public boolean isDone() {
        return false;  // Script never terminates on its own
    }

    @Override
    public boolean runWhilePaused() {
        return false;  // Don't run when game is paused
    }
    
    // Make the class more resilient to serialization/deserialization
    // This helps with save compatibility when the mod is removed
    private Object readResolve() {
        // Re-initialize transient fields
        daysAccumulator = 0f;
        
        // Make sure we have a compatibilityList initialized to prevent null pointer exceptions
        if (compatibilityList == null) {
            compatibilityList = new ArrayList<>();
        }
        
        return this;
    }
    
    // Special methods to handle the ArrayList conversion error
    public void setScripts(ArrayList<?> list) {
        compatibilityList = list != null ? list : new ArrayList<>();
    }
    
    public ArrayList<?> getScripts() {
        return compatibilityList;
    }
    
    // Special method to allow this class to be converted to ArrayList when needed
    // This helps with backwards compatibility when mod is disabled
    public Object writeReplace() {
        // Return an empty ArrayList instead of this object when saving
        // This will make old saves compatible with future game versions without the mod
        return new ArrayList<>();
    }
}
