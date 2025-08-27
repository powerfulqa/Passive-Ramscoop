package ramscoop;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import lunalib.lunaSettings.LunaSettings;

public class ModPlugin extends BaseModPlugin {
    private static final Logger LOG = Global.getLogger(ModPlugin.class);
    public static final String MOD_ID = "m561_ramscoop";
    
    // Default values (fallbacks; will be overridden by LunaLib or settings.json)
    public static boolean enable_fuel = true;
    public static boolean enable_supplies = true;
    public static float fuel_per_day = 0.1f;
    public static float supplies_per_crew = 0.1f;
    public static float percent_supply_limit = 0.35f;
    public static float hard_supply_limit = 0.0f;
    // New fuel limiting settings
    public static float percent_fuel_limit = 1.0f; // 1.0 = 100% (no soft cap by default)
    public static float hard_fuel_limit = 0.0f;     // 0 = disabled
    public static float fuel_cap_margin = 0.0f;     // 0 = no margin, set to 1.0 to keep 1 unit below
    public static boolean scoop_toggle_default_on = true; // default enabled
    public static String crew_usage = "extra";
    public static String no_crew_gen = "percent";
    public static float no_crew_rate = 0.1f;
    
    // Track if LunaLib is being used and if we've successfully loaded settings
    private static boolean lunaLibReady = false;
    private static boolean settingsLoaded = false;
    
    public ModPlugin() {
        try {
            LOG.info("[Ramscoop] ModPlugin constructed (v0.4.1)");
        } catch (Exception e) {
            System.out.println("Ramscoop: CRITICAL ERROR in constructor: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Simple readiness: treat LunaLib as ready if the mod is enabled; API calls will work when it's initialized
    private static boolean isLunaLibReady() {
        return Global.getSettings().getModManager().isModEnabled("lunalib");
    }

    private static void loadSettings() {
        // Minimal logging
        try {
            // Check if LunaLib is ready
            boolean lunaLibEnabled = Global.getSettings().getModManager().isModEnabled("lunalib");
            LOG.info("[Ramscoop] LunaLib enabled: " + lunaLibEnabled);
            
            if (lunaLibEnabled && isLunaLibReady()) {
                loadLunaLibSettings();
                lunaLibReady = true;
                settingsLoaded = true;
                LOG.info("[Ramscoop] Loaded settings from LunaLib");
            } else if (lunaLibEnabled) {
                // LunaLib is enabled but not ready yet - use settings.json as a baseline
                LOG.info("[Ramscoop] LunaLib not ready yet; using settings.json until it becomes ready");
                try {
                    loadLegacySettings();
                    settingsLoaded = true;
                    LOG.info("[Ramscoop] Baseline settings from settings.json applied while waiting for LunaLib");
                } catch (Throwable t) {
                    LOG.info("[Ramscoop] No settings.json baseline available; keeping defaults");
                }
            } else {
                // Fallback to settings.json
                loadLegacySettings();
                settingsLoaded = true;
                LOG.info("[Ramscoop] Loaded settings from settings.json (LunaLib not available)");
            }
        } catch (Exception exception) {
            LOG.error("[Ramscoop] Error loading settings", exception);
            exception.printStackTrace();
            LOG.info("[Ramscoop] Using default values");
            settingsLoaded = true; // Mark as loaded so we don't keep retrying on error
        }
        LOG.info("[Ramscoop] Settings load complete");
    }
    
    private static void loadLunaLibSettings() {
        try {
            // Direct API calls (LunaLib is a required dependency)
            enable_fuel = LunaSettings.getBoolean(MOD_ID, "ramscoop_enable_fuel");
            enable_supplies = LunaSettings.getBoolean(MOD_ID, "ramscoop_enable_supplies");
            fuel_per_day = LunaSettings.getDouble(MOD_ID, "ramscoop_fuel_per_day").floatValue();
            // New fuel limiting settings via LunaLib (with safe defaults if missing)
            try { percent_fuel_limit = LunaSettings.getDouble(MOD_ID, "ramscoop_percent_fuel_limit").floatValue(); } catch (Throwable ignored) {}
            try { hard_fuel_limit = LunaSettings.getDouble(MOD_ID, "ramscoop_hard_fuel_limit").floatValue(); } catch (Throwable ignored) {}
            try { fuel_cap_margin = LunaSettings.getDouble(MOD_ID, "ramscoop_fuel_cap_margin").floatValue(); } catch (Throwable ignored) {}
            percent_supply_limit = LunaSettings.getDouble(MOD_ID, "ramscoop_percent_supply_limit").floatValue();
            hard_supply_limit = LunaSettings.getDouble(MOD_ID, "ramscoop_hard_supply_limit").floatValue();
            supplies_per_crew = LunaSettings.getDouble(MOD_ID, "ramscoop_supply_per_crew").floatValue();
            crew_usage = LunaSettings.getString(MOD_ID, "ramscoop_crew_usage");
            no_crew_gen = LunaSettings.getString(MOD_ID, "ramscoop_no_crew_gen");
            no_crew_rate = LunaSettings.getDouble(MOD_ID, "ramscoop_no_crew_rate").floatValue();
            try { scoop_toggle_default_on = LunaSettings.getBoolean(MOD_ID, "ramscoop_toggle_default_on"); } catch (Throwable ignored) {}
            
            // Debug logging
            System.out.println("Ramscoop: LunaLib Settings Loaded:");
            LOG.info("[Ramscoop] LunaLib Settings Loaded");
            System.out.println("  enable_fuel: " + enable_fuel);
            System.out.println("  enable_supplies: " + enable_supplies);
            System.out.println("  fuel_per_day: " + fuel_per_day);
            System.out.println("  supplies_per_crew: " + supplies_per_crew);
            System.out.println("  crew_usage: " + crew_usage);
            System.out.println("  percent_fuel_limit: " + percent_fuel_limit);
            System.out.println("  hard_fuel_limit: " + hard_fuel_limit);
            System.out.println("  fuel_cap_margin: " + fuel_cap_margin);
            System.out.println("  scoop_toggle_default_on: " + scoop_toggle_default_on);
            // Single-line summary for easy grep
            System.out.println(
                "Ramscoop: Final settings from LunaLib -> fuel=" + enable_fuel +
                ", supplies=" + enable_supplies +
                ", fuel_per_day=" + fuel_per_day +
                ", percent_fuel_limit=" + percent_fuel_limit +
                ", hard_fuel_limit=" + hard_fuel_limit +
                ", fuel_cap_margin=" + fuel_cap_margin +
                ", percent_supply_limit=" + percent_supply_limit +
                ", hard_supply_limit=" + hard_supply_limit +
                ", crew_usage=" + crew_usage +
                ", no_crew_gen=" + no_crew_gen +
                ", no_crew_rate=" + no_crew_rate
            );
            LOG.info("[Ramscoop] Final settings from LunaLib -> fuel=" + enable_fuel +
                    ", supplies=" + enable_supplies +
                    ", fuel_per_day=" + fuel_per_day +
                    ", percent_fuel_limit=" + percent_fuel_limit +
                    ", hard_fuel_limit=" + hard_fuel_limit +
                    ", fuel_cap_margin=" + fuel_cap_margin +
                    ", percent_supply_limit=" + percent_supply_limit +
                    ", hard_supply_limit=" + hard_supply_limit +
                    ", crew_usage=" + crew_usage +
                    ", no_crew_gen=" + no_crew_gen +
                    ", no_crew_rate=" + no_crew_rate);
            
            // Listener optional; our runtime script periodically calls reloadSettings()
        } catch (Exception e) {
            System.out.println("Ramscoop: Failed to load LunaLib settings: " + e.getMessage());
            LOG.error("[Ramscoop] Failed to load LunaLib settings", e);
            e.printStackTrace();
            throw new RuntimeException("Failed to load LunaLib settings", e);
        }
    }
    
    private static void loadLegacySettings() {
        try {
            JSONObject config = Global.getSettings().loadJSON("settings.json", MOD_ID);
            enable_fuel = config.getBoolean("enable_fuel");
            enable_supplies = config.getBoolean("enable_supplies");
            
            // Handle fuel_per_day (might not exist in older settings.json)
            if (config.has("fuel_per_day")) {
                fuel_per_day = (float)config.getDouble("fuel_per_day");
            }
            // Optional new fuel limit settings (legacy JSON)
            if (config.has("percent_fuel_limit")) {
                percent_fuel_limit = (float)config.getDouble("percent_fuel_limit");
            }
            if (config.has("hard_fuel_limit")) {
                hard_fuel_limit = (float)config.getDouble("hard_fuel_limit");
            }
            if (config.has("fuel_cap_margin")) {
                fuel_cap_margin = (float)config.getDouble("fuel_cap_margin");
            }
            
            supplies_per_crew = (float)config.getDouble("supply_per_crew");
            percent_supply_limit = (float)config.getDouble("percent_supply_limit");
            hard_supply_limit = (float)config.getDouble("hard_supply_limit");
            
            // Use flexible parsing for non-standard JSON values
            crew_usage = config.get("crew_usage").toString();
            no_crew_gen = config.get("no_crew_gen").toString();
            no_crew_rate = (float)config.getDouble("no_crew_rate");
            if (config.has("scoop_toggle_default_on")) {
                try { scoop_toggle_default_on = config.getBoolean("scoop_toggle_default_on"); } catch (Throwable ignored) {}
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load legacy settings", e);
        }
    }

    @Override
    public void onGameLoad(boolean newGame) {
        LOG.info("[Ramscoop] onGameLoad()");
        // Load settings once here
        loadSettings();
        // Log a single-line snapshot for grep
        LOG.info("[Ramscoop] Snapshot onGameLoad -> fuel=" + enable_fuel +
                ", supplies=" + enable_supplies +
                ", fuel_per_day=" + fuel_per_day +
                ", percent_fuel_limit=" + percent_fuel_limit +
                ", hard_fuel_limit=" + hard_fuel_limit +
                ", fuel_cap_margin=" + fuel_cap_margin +
                ", percent_supply_limit=" + percent_supply_limit +
                ", hard_supply_limit=" + hard_supply_limit +
                ", crew_usage=" + crew_usage +
                ", no_crew_gen=" + no_crew_gen +
                ", no_crew_rate=" + no_crew_rate);
        // Start the runtime script
        Global.getSector().addTransientScript(new Ramscoop());
        // Initialize runtime toggle state in player fleet memory
        try {
            com.fs.starfarer.api.campaign.CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
            if (fleet != null) {
                fleet.getMemoryWithoutUpdate().set("$ramscoop_enabled", scoop_toggle_default_on);
                LOG.info("[Ramscoop] Initial toggle state set: " + scoop_toggle_default_on);
            }
        } catch (Throwable t) {
            // Non-fatal
        }
        LOG.info("[Ramscoop] initialization complete");
    }
    
    /**
     * Public method for reloading settings - can be called by Ramscoop periodically
     * This method will retry LunaLib loading if it wasn't ready before
     */
    public static void reloadSettings() {
        // Always try to reload if LunaLib is enabled but we haven't successfully used it yet
        if (Global.getSettings().getModManager().isModEnabled("lunalib") && !lunaLibReady) {
            System.out.println("Ramscoop: LunaLib is enabled but not ready, attempting to load settings...");
            LOG.info("[Ramscoop] LunaLib is enabled but not ready, attempting to load settings...");
            loadSettings();
        } else if (!settingsLoaded) {
            // If no settings loaded yet, try to load them
            System.out.println("Ramscoop: No settings loaded yet, attempting to load...");
            LOG.info("[Ramscoop] No settings loaded yet, attempting to load...");
            loadSettings();
        } else {
            System.out.println("Ramscoop: Settings already loaded, LunaLib ready: " + lunaLibReady);
            LOG.info("[Ramscoop] Settings already loaded, LunaLib ready: " + lunaLibReady);
        }
    }
    
    /**
     * Check if settings have been successfully loaded
     */
    public static boolean areSettingsLoaded() {
        return settingsLoaded;
    }
    
    /**
     * Check if LunaLib is ready and we're using it
     */
    public static boolean isUsingLunaLib() {
        return lunaLibReady;
    }
    
    /**
     * Log comprehensive settings status for debugging
     */
    public static void logSettingsStatus() {
        System.out.println("=== RAMSCOOP SETTINGS STATUS ===");
        LOG.info("[Ramscoop] === SETTINGS STATUS ===");
        System.out.println("Settings loaded: " + settingsLoaded);
        System.out.println("LunaLib ready: " + lunaLibReady);
        System.out.println("Current values:");
        System.out.println("  enable_fuel: " + enable_fuel);
        System.out.println("  enable_supplies: " + enable_supplies);
        System.out.println("  fuel_per_day: " + fuel_per_day);
        System.out.println("  supplies_per_crew: " + supplies_per_crew);
        System.out.println("  percent_supply_limit: " + percent_supply_limit);
        System.out.println("  hard_supply_limit: " + hard_supply_limit);
        System.out.println("  crew_usage: " + crew_usage);
        System.out.println("  no_crew_gen: " + no_crew_gen);
        System.out.println("  no_crew_rate: " + no_crew_rate);
        System.out.println("================================");
        LOG.info("[Ramscoop] ================================");
    }
}
