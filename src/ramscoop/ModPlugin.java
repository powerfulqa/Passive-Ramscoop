package ramscoop;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import org.apache.log4j.Logger;

/**
 * Main mod plugin class for the Ramscoop mod.
 * Handles initialization and configuration of the passive resource generation system.
 * This is a template/placeholder file that would need to be completed with proper
 * implementation based on the compiled JAR's functionality.
 */
public class ModPlugin extends BaseModPlugin {
    private static final Logger log = Logger.getLogger(ModPlugin.class);
    
    /**
     * Called when the game is first loaded
     */
    @Override
    public void onApplicationLoad() {
        try {
            log.info("Ramscoop mod - loading settings from configuration file");
            // Load settings from config file
        } catch (Exception e) {
            log.error("Failed to load Ramscoop settings", e);
        }
    }
    
    /**
     * Called when a save is loaded or a new game is created
     */
    @Override
    public void onGameLoad(boolean newGame) {
        try {
            log.info("Ramscoop mod - initializing resource generation system");
            
            // Create a single instance of Ramscoop
            Ramscoop ramscoop = new Ramscoop();
            
            // Add the Ramscoop as an EveryFrameScript
            Global.getSector().addScript(ramscoop);
            
            log.info("Ramscoop passive resource generation system initialized");
        } catch (Exception e) {
            log.error("Failed to initialize Ramscoop system", e);
        }
    }
}
