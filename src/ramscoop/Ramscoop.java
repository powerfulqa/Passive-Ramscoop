package ramscoop;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;

/**
 * Main class implementing the Ramscoop functionality.
 * Handles the passive generation of fuel and supplies while in nebulas.
 */
public class Ramscoop implements EveryFrameScript {
    
    // Configuration settings
    private boolean enableFuel = true;
    private boolean enableSupplies = true;
    private float fuelPerDay = 0.1f;
    private float percentSupplyLimit = 0.35f;
    private int hardSupplyLimit = 0;
    
    // Internal tracking variables
    private float daysPassed = 0f;
    private boolean inNebula = false;
    private boolean isDone = false;
    
    public Ramscoop() {
        // Initialize ramscoop system
        loadSettings();
    }
    
    /**
     * Load settings from configuration
     */
    private void loadSettings() {
        try {
            // This would normally load from settings.json
            // We're using defaults for now
        } catch (Exception e) {
            // Log error
        }
    }
    
    /**
     * Check if in nebula
     */
    private boolean isInNebula() {
        try {
            return Global.getSector().getPlayerFleet().getContainingLocation().isNebula();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Generate resources
     */
    private void generateResources() {
        try {
            // Get player fleet
            Object playerFleet = Global.getSector().getPlayerFleet();
            
            // This would normally:
            // 1. Generate fuel based on settings
            // 2. Generate supplies based on settings
            // 3. Add them to player cargo
        } catch (Exception e) {
            // Log error
        }
    }
    
    // EveryFrameScript implementation
    @Override
    public boolean isDone() {
        return isDone;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void advance(float amount) {
        try {
            // Check if game is paused
            if (Global.getSector().isPaused()) return;
            
            // Check nebula status
            boolean currentlyInNebula = isInNebula();
            
            // Only process when in a nebula
            if (currentlyInNebula) {
                // Convert time to days (10 seconds = 1 day)
                daysPassed += amount / 10f;
                
                // Generate resources once per day
                if (daysPassed >= 1.0f) {
                    generateResources();
                    daysPassed = 0f;
                    
                    // Notify player when entering nebula
                    if (!inNebula) {
                        Global.getSector().getCampaignUI().addMessage(
                            "Your fleet's ramscoops are now collecting resources from the nebula."
                        );
                    }
                }
            } else if (inNebula) {
                // Notify player when leaving nebula
                Global.getSector().getCampaignUI().addMessage(
                    "Your fleet has left the nebula. Ramscoop collection has stopped."
                );
                
                // Generate partial day resources
                if (daysPassed > 0) {
                    generateResources();
                    daysPassed = 0f;
                }
            }
            
            // Update state
            inNebula = currentlyInNebula;
        } catch (Exception e) {
            // Log error
        }
    }
}
