package ramscoop;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import org.json.JSONObject;

public class ModPlugin extends BaseModPlugin {
    // Configuration variables from settings.json
    public static boolean enable_fuel = true;
    public static boolean enable_supplies = true;
    public static float fuel_per_day = 0.1f;
    public static float percent_supply_limit = 0.35f;
    public static float hard_supply_limit = 0.0f;
    public static String crew_usage = "extra";
    public static float supplies_per_crew = 0.1f;
    public static String no_crew_gen = "percent";
    public static float no_crew_rate = 0.1f;

    @Override
    public void onGameLoad(boolean newGame) {
        loadSettings();
        
        // Add the Ramscoop script to the sector if it's not already there
        boolean found = false;
        for (Object script : Global.getSector().getScripts()) {
            if (script instanceof Ramscoop) {
                found = true;
                break;
            }
        }
        
        if (!found) {
            Global.getSector().addScript(new Ramscoop());
            Global.getLogger(ModPlugin.class).info("Ramscoop initialized");
        }
    }
    
    private void loadSettings() {
        try {
            // Use SafeJsonReader utility to avoid reflection issues
            JSONObject settings = Global.getSettings().loadJSON("settings.json", "m561_ramscoop");
            
            // Read values directly from the JSONObject
            if (settings != null) {
                if (settings.has("enable_fuel")) enable_fuel = settings.getBoolean("enable_fuel");
                if (settings.has("enable_supplies")) enable_supplies = settings.getBoolean("enable_supplies");
                if (settings.has("fuel_per_day")) fuel_per_day = (float) settings.getDouble("fuel_per_day");
                if (settings.has("percent_supply_limit")) percent_supply_limit = (float) settings.getDouble("percent_supply_limit");
                if (settings.has("hard_supply_limit")) hard_supply_limit = (float) settings.getDouble("hard_supply_limit");
                if (settings.has("crew_usage")) crew_usage = settings.getString("crew_usage");
                if (settings.has("supplies_per_crew")) supplies_per_crew = (float) settings.getDouble("supplies_per_crew");
                if (settings.has("no_crew_gen")) no_crew_gen = settings.getString("no_crew_gen");
                if (settings.has("no_crew_rate")) no_crew_rate = (float) settings.getDouble("no_crew_rate");
                
                Global.getLogger(ModPlugin.class).info("Ramscoop settings loaded");
            }
        } catch (Exception e) {
            Global.getLogger(ModPlugin.class).error("Error loading Ramscoop settings", e);
            // Fall back to default settings if loading fails
        }
    }
}
