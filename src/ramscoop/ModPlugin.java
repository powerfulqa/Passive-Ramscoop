package ramscoop;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import org.json.JSONObject;

public class ModPlugin extends BaseModPlugin {
    public static final String MOD_ID = "m561_ramscoop";
    
    // Default values (fallback if neither LunaLib nor settings.json is available)
    public static boolean enable_fuel = true;
    public static boolean enable_supplies = true;
    public static float fuel_per_day = 0.1f;
    public static float supplies_per_crew = 0.1f;
    public static float percent_supply_limit = 0.35f;
    public static float hard_supply_limit = 0.0f;
    public static String crew_usage = "extra";
    public static String no_crew_gen = "percent";
    public static float no_crew_rate = 0.1f;

    private void loadSettings() {
        try {
            // Try LunaLib first (soft dependency)
            if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
                loadLunaLibSettings();
                System.out.println("Ramscoop: Loaded settings from LunaLib");
            } else {
                // Fallback to settings.json
                loadLegacySettings();
                System.out.println("Ramscoop: Loaded settings from settings.json (LunaLib not available)");
            }
        } catch (Exception exception) {
            System.out.println("Ramscoop: Error loading settings - " + exception.getMessage());
            exception.printStackTrace();
            System.out.println("Ramscoop: Using default values");
        }
    }
    
    private void loadLunaLibSettings() {
        try {
            // Use reflection to avoid hard dependency on LunaLib
            Class<?> lunaSettingsClass = Class.forName("lunalib.lunaSettings.LunaSettings");
            
            enable_fuel = (Boolean) lunaSettingsClass.getMethod("getBoolean", String.class, String.class)
                .invoke(null, MOD_ID, "ramscoop_enable_fuel");
            enable_supplies = (Boolean) lunaSettingsClass.getMethod("getBoolean", String.class, String.class)
                .invoke(null, MOD_ID, "ramscoop_enable_supplies");
            fuel_per_day = ((Double) lunaSettingsClass.getMethod("getDouble", String.class, String.class)
                .invoke(null, MOD_ID, "ramscoop_fuel_per_day")).floatValue();
            percent_supply_limit = ((Double) lunaSettingsClass.getMethod("getDouble", String.class, String.class)
                .invoke(null, MOD_ID, "ramscoop_percent_supply_limit")).floatValue();
            hard_supply_limit = ((Double) lunaSettingsClass.getMethod("getDouble", String.class, String.class)
                .invoke(null, MOD_ID, "ramscoop_hard_supply_limit")).floatValue();
            supplies_per_crew = ((Double) lunaSettingsClass.getMethod("getDouble", String.class, String.class)
                .invoke(null, MOD_ID, "ramscoop_supply_per_crew")).floatValue();
            crew_usage = (String) lunaSettingsClass.getMethod("getString", String.class, String.class)
                .invoke(null, MOD_ID, "ramscoop_crew_usage");
            no_crew_gen = (String) lunaSettingsClass.getMethod("getString", String.class, String.class)
                .invoke(null, MOD_ID, "ramscoop_no_crew_gen");
            no_crew_rate = ((Double) lunaSettingsClass.getMethod("getDouble", String.class, String.class)
                .invoke(null, MOD_ID, "ramscoop_no_crew_rate")).floatValue();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load LunaLib settings", e);
        }
    }
    
    private void loadLegacySettings() {
        try {
            JSONObject config = Global.getSettings().loadJSON("settings.json", MOD_ID);
            enable_fuel = config.getBoolean("enable_fuel");
            enable_supplies = config.getBoolean("enable_supplies");
            
            // Handle fuel_per_day (might not exist in older settings.json)
            if (config.has("fuel_per_day")) {
                fuel_per_day = (float)config.getDouble("fuel_per_day");
            }
            
            supplies_per_crew = (float)config.getDouble("supply_per_crew");
            percent_supply_limit = (float)config.getDouble("percent_supply_limit");
            hard_supply_limit = (float)config.getDouble("hard_supply_limit");
            
            // Use flexible parsing for non-standard JSON values
            crew_usage = config.get("crew_usage").toString();
            no_crew_gen = config.get("no_crew_gen").toString();
            no_crew_rate = (float)config.getDouble("no_crew_rate");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load legacy settings", e);
        }
    }

    @Override
    public void onGameLoad(boolean newGame) {
        loadSettings();
        Global.getSector().addTransientScript(new Ramscoop());
    }
}
