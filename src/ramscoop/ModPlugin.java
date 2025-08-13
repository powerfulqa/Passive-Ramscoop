package ramscoop;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import org.json.JSONObject;

public class ModPlugin extends BaseModPlugin {
    public static final String MOD_ID = "m561_ramscoop";
    
    static {
        System.out.println("=== RAMSCOOP MOD LOADING ===");
        System.out.println("Ramscoop: ModPlugin class loaded - STATIC INITIALIZER");
        System.out.println("Ramscoop: Current time: " + System.currentTimeMillis());
        System.out.println("Ramscoop: Version 0.4.0 with LunaLib support");
        System.out.println("=== RAMSCOOP STATIC INIT COMPLETE ===");
        
        // Also write to a debug file
        try {
            java.io.FileWriter debugFile = new java.io.FileWriter("ramscoop_debug.txt", true);
            debugFile.write("=== RAMSCOOP STATIC INITIALIZER RAN ===\n");
            debugFile.write("Time: " + new java.util.Date() + "\n");
            debugFile.write("========================================\n");
            debugFile.close();
        } catch (Exception e) {
            // Ignore file write errors
        }
    }
    
    public ModPlugin() {
        System.out.println("Ramscoop: ModPlugin constructor called");
    }
    
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
        System.out.println("Ramscoop: Starting settings load...");
        try {
            // Try LunaLib first (soft dependency)
            boolean lunaLibEnabled = Global.getSettings().getModManager().isModEnabled("lunalib");
            System.out.println("Ramscoop: LunaLib enabled: " + lunaLibEnabled);
            
            if (lunaLibEnabled) {
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
        System.out.println("Ramscoop: Settings load complete");
    }
    
    private void loadLunaLibSettings() {
        try {
            // Use reflection to avoid hard dependency on LunaLib
            Class<?> lunaSettingsClass = Class.forName("lunalib.lunaSettings.LunaSettings");
            System.out.println("Ramscoop: Found LunaSettings class");
            
            // Test if our mod is recognized by LunaLib
            try {
                Object testValue = lunaSettingsClass.getMethod("getBoolean", String.class, String.class)
                    .invoke(null, MOD_ID, "ramscoop_enable_fuel");
                System.out.println("Ramscoop: LunaLib test read successful: " + testValue);
            } catch (Exception e) {
                System.out.println("Ramscoop: LunaLib test read failed: " + e.getMessage());
                throw e;
            }
            
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
            
            // Debug logging
            System.out.println("Ramscoop: LunaLib Settings Loaded:");
            System.out.println("  enable_fuel: " + enable_fuel);
            System.out.println("  enable_supplies: " + enable_supplies);
            System.out.println("  fuel_per_day: " + fuel_per_day);
            System.out.println("  supplies_per_crew: " + supplies_per_crew);
            System.out.println("  crew_usage: " + crew_usage);
            
            // Try to register a settings change listener if available
            try {
                Class<?> lunaEventsClass = Class.forName("lunalib.lunaEvents.LunaEvents");
                Object listener = new Object() {
                    public void notifySettingsChanged(String modId) {
                        if (MOD_ID.equals(modId)) {
                            System.out.println("Ramscoop: Settings changed, reloading...");
                            loadSettings();
                        }
                    }
                };
                lunaEventsClass.getMethod("addSettingsListener", Object.class).invoke(null, listener);
                System.out.println("Ramscoop: Registered settings change listener");
            } catch (Exception e) {
                System.out.println("Ramscoop: Settings change listener not available, settings will only load on game start");
            }
        } catch (Exception e) {
            System.out.println("Ramscoop: Failed to load LunaLib settings: " + e.getMessage());
            e.printStackTrace();
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
        System.out.println("Ramscoop: ModPlugin.onGameLoad() called");
        loadSettings();
        Global.getSector().addTransientScript(new Ramscoop());
        System.out.println("Ramscoop: ModPlugin initialization complete");
    }
    
    // Public method for reloading settings (called by Ramscoop periodically)
    public void reloadSettings() {
        loadSettings();
    }
}
