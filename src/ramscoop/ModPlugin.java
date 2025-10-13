package ramscoop;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import lunalib.lunaSettings.LunaSettings;
import java.awt.Color;

public class ModPlugin extends BaseModPlugin {
    private static final Logger LOG = Global.getLogger(ModPlugin.class);
    public static final String MOD_ID = "m561_ramscoop";

    // Default constant values (fallbacks if LunaLib/settings.json not available)
    private static final float DEFAULT_FUEL_PER_DAY = 0.1f;
    private static final float DEFAULT_SUPPLIES_PER_CREW = 0.1f;
    private static final float DEFAULT_PERCENT_SUPPLY_LIMIT = 0.35f;
    private static final float DEFAULT_HARD_SUPPLY_LIMIT = 0.0f;
    private static final float DEFAULT_PERCENT_FUEL_LIMIT = 1.0f; // 100% - no limit by default
    private static final float DEFAULT_HARD_FUEL_LIMIT = 0.0f; // disabled by default
    private static final float DEFAULT_FUEL_CAP_MARGIN = 0.0f;
    private static final float DEFAULT_CORONA_FUEL_PER_DAY = 0.25f; // 25% max fuel per day
    private static final float DEFAULT_NO_CREW_RATE = 0.1f;
    private static final String DEFAULT_CREW_USAGE = "extra";
    private static final String DEFAULT_NO_CREW_GEN = "percent";

    // Runtime settings (loaded from LunaLib or settings.json, defaults above used
    // as fallback)
    public static boolean enable_fuel = true;
    public static boolean enable_supplies = true;
    public static float fuel_per_day = DEFAULT_FUEL_PER_DAY;
    public static float supplies_per_crew = DEFAULT_SUPPLIES_PER_CREW;
    public static float percent_supply_limit = DEFAULT_PERCENT_SUPPLY_LIMIT;
    public static float hard_supply_limit = DEFAULT_HARD_SUPPLY_LIMIT;
    // Nebula caps
    public static float nebula_percent_fuel_limit = DEFAULT_PERCENT_FUEL_LIMIT;
    public static float nebula_hard_fuel_limit = DEFAULT_HARD_FUEL_LIMIT;
    public static float nebula_fuel_cap_margin = DEFAULT_FUEL_CAP_MARGIN;
    // Corona caps
    public static float corona_percent_fuel_limit = DEFAULT_PERCENT_FUEL_LIMIT;
    public static float corona_hard_fuel_limit = DEFAULT_HARD_FUEL_LIMIT;
    public static float corona_fuel_cap_margin = DEFAULT_FUEL_CAP_MARGIN;
    // Runtime toggles and modes
    public static boolean scoop_toggle_default_on = true;
    public static String crew_usage = DEFAULT_CREW_USAGE;
    public static String no_crew_gen = DEFAULT_NO_CREW_GEN;
    public static float no_crew_rate = DEFAULT_NO_CREW_RATE;
    // Corona settings
    public static boolean corona_enable_fuel = true;
    public static float corona_fuel_per_day = DEFAULT_CORONA_FUEL_PER_DAY;
    public static boolean corona_caps_reuse = true;
    // Visual feedback
    public static boolean enable_visual_feedback = false;
    public static float floating_text_duration = 0.7f;
    // Per-event notification toggles
    public static boolean notify_nebula_entry = true;
    public static boolean notify_nebula_exit = true;
    public static boolean notify_corona_entry = true;
    public static boolean notify_corona_exit = true;
    // Visual feedback colors
    public static Color color_toggle_active = Color.CYAN;
    public static Color color_toggle_active_secondary = Color.BLUE;
    public static Color color_toggle_inactive = Color.LIGHT_GRAY;
    public static Color color_nebula_active = Color.LIGHT_GRAY;
    public static Color color_nebula_inactive = Color.LIGHT_GRAY;
    public static Color color_corona_active = Color.LIGHT_GRAY;
    public static Color color_corona_inactive = Color.LIGHT_GRAY;

    // Track if LunaLib is being used and if we've successfully loaded settings
    private static boolean lunaLibReady = false;
    private static boolean settingsLoaded = false;

    // Debug flag - set to false for production builds
    private static final boolean DEBUG_MODE = false;

    public ModPlugin() {
        try {
            if (DEBUG_MODE) {
                LOG.info("[Ramscoop] ModPlugin constructed (v0.6.2)");
            }
        } catch (Exception e) {
            // Critical errors should always be reported
            System.out.println("Ramscoop: CRITICAL ERROR in constructor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onApplicationLoad() {
        // No legacy color migration needed - only using new Color keys
    }

    // Simple readiness: treat LunaLib as ready if the mod is enabled; API calls
    // will work when it's initialized
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
                // Seed from legacy settings first so missing LunaLib keys fall back to
                // settings.json
                try {
                    loadLegacySettings();
                } catch (Throwable ignored) {
                }
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

    private static void setColorsFromSelections(Color nebulaActive, Color nebulaInactive, Color coronaActive, Color coronaInactive) {
        // Toggle uses nebula colors
        color_toggle_active = nebulaActive;
        color_toggle_active_secondary = nebulaActive;
        color_toggle_inactive = nebulaInactive;
        
        // Nebula colors
        color_nebula_active = nebulaActive;
        color_nebula_inactive = nebulaInactive;
        
        // Corona colors
        color_corona_active = coronaActive;
        color_corona_inactive = coronaInactive;
    }

    private static Color parseColor(String colorName) {
        if (colorName == null) return Color.CYAN; // default
        switch (colorName.toLowerCase()) {
            case "black": return Color.BLACK;
            case "blue": return Color.BLUE;
            case "cyan": return Color.CYAN;
            case "dark gray": return Color.DARK_GRAY;
            case "gray": return Color.GRAY;
            case "green": return Color.GREEN;
            case "light gray": return Color.LIGHT_GRAY;
            case "magenta": return Color.MAGENTA;
            case "orange": return Color.ORANGE;
            case "pink": return Color.PINK;
            case "red": return Color.RED;
            case "white": return Color.WHITE;
            case "yellow": return Color.YELLOW;
            default: return Color.CYAN; // fallback
        }
    }

    /**
     * Compare two colors for equality (including alpha).
     */
    private static boolean colorsEqual(Color a, Color b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.getRed() == b.getRed() && a.getGreen() == b.getGreen() && a.getBlue() == b.getBlue() && a.getAlpha() == b.getAlpha();
    }

    /**
     * Parse a hex color string like "#RRGGBB" or "RRGGBB" (also handles 8-digit ARGB/RRGGBBAA)
     */
    private static Color parseHexColor(String hexColor, Color fallback) {
        if (hexColor == null) return fallback;
        String s = hexColor.trim();
        if (s.startsWith("#")) s = s.substring(1);
        try {
            if (s.length() == 6) {
                int r = Integer.parseInt(s.substring(0, 2), 16);
                int g = Integer.parseInt(s.substring(2, 4), 16);
                int b = Integer.parseInt(s.substring(4, 6), 16);
                return new Color(r, g, b);
            } else if (s.length() == 8) {
                int r = Integer.parseInt(s.substring(0, 2), 16);
                int g = Integer.parseInt(s.substring(2, 4), 16);
                int b = Integer.parseInt(s.substring(4, 6), 16);
                int a = Integer.parseInt(s.substring(6, 8), 16);
                return new Color(r, g, b, a);
            }
        } catch (Exception e) {
            LOG.warn("[Ramscoop] parseHexColor failed for '" + hexColor + "': " + e.getMessage());
        }
        return fallback;
    }

    /**
     * Compute an "inactive" / desaturated variant of a color by blending with a light gray.
     */
    private static Color makeInactive(Color c) {
        if (c == null) return Color.LIGHT_GRAY;
        int r = (c.getRed() + 192) / 2;
        int g = (c.getGreen() + 192) / 2;
        int b = (c.getBlue() + 192) / 2;
        int a = c.getAlpha();
        return new Color(Math.min(255, r), Math.min(255, g), Math.min(255, b), a);
    }

    /**
     * Targeted debug: log raw LunaLib stored values for a key both as Color and as String
     * so we can detect whether the saved value is a Color object or a legacy hex string.
     */
    private static void logRawLunaValue(String key) {
        try {
            // Try reading as Color
            try {
                Color c = LunaSettings.getColor(MOD_ID, key);
                if (c != null) {
                    LOG.info(String.format("[Ramscoop] LunaLib stored (Color) for '%s' -> #%02X%02X%02X alpha=%d",
                            key, c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()));
                } else {
                    LOG.info(String.format("[Ramscoop] LunaLib stored (Color) for '%s' -> null", key));
                }
            } catch (Throwable t) {
                LOG.info(String.format("[Ramscoop] LunaLib getColor('%s') threw: %s", key, t.getClass().getSimpleName()));
            }

            // Try reading as String (legacy hex storage)
            try {
                String s = LunaSettings.getString(MOD_ID, key);
                if (s != null) {
                    LOG.info(String.format("[Ramscoop] LunaLib stored (String) for '%s' -> '%s'", key, s));
                } else {
                    LOG.info(String.format("[Ramscoop] LunaLib stored (String) for '%s' -> null", key));
                }
            } catch (Throwable t) {
                LOG.info(String.format("[Ramscoop] LunaLib getString('%s') threw: %s", key, t.getClass().getSimpleName()));
            }
        } catch (Throwable t) {
            LOG.warn("[Ramscoop] Failed to inspect LunaLib value for key '" + key + "': " + t.getMessage());
        }
    }

    private static void loadLunaLibSettings() {
        try {
            enable_fuel = LunaSettings.getBoolean(MOD_ID, "ramscoop_enable_fuel");
            enable_supplies = LunaSettings.getBoolean(MOD_ID, "ramscoop_enable_supplies");
            // UI provides 0..100 percent per day; convert to 0..1 fraction
            fuel_per_day = LunaSettings.getDouble(MOD_ID, "nebula_fuel_per_day").floatValue() / 100f; // Matches CSV key
                                                                                                      // for nebula
            // New fuel limiting settings via LunaLib (with safe defaults if missing)
            // Nebula caps via UI (percent sliders)
            try {
                nebula_percent_fuel_limit = LunaSettings.getDouble(MOD_ID, "nebula_percent_fuel_limit").floatValue()
                        / 100f;
            } catch (Throwable ignored) {
            }
            try {
                nebula_hard_fuel_limit = LunaSettings.getDouble(MOD_ID, "nebula_hard_fuel_limit").floatValue();
            } catch (Throwable ignored) {
            }
            try {
                nebula_fuel_cap_margin = LunaSettings.getDouble(MOD_ID, "nebula_fuel_cap_margin").floatValue();
            } catch (Throwable ignored) {
            }

            // Supply limit settings with null-handling (fixed: nebula_* → ramscoop_* key
            // names)
            try {
                percent_supply_limit = LunaSettings.getDouble(MOD_ID, "ramscoop_percent_supply_limit").floatValue();
            } catch (Throwable ignored) {
            }
            try {
                hard_supply_limit = LunaSettings.getDouble(MOD_ID, "ramscoop_hard_supply_limit").floatValue();
            } catch (Throwable ignored) {
            }
            try {
                supplies_per_crew = LunaSettings.getDouble(MOD_ID, "ramscoop_supply_per_crew").floatValue();
            } catch (Throwable ignored) {
            }
            // Supply settings now live under Nebula as duplicates; read either key
            try {
                crew_usage = LunaSettings.getString(MOD_ID, "nebula_crew_usage");
            } catch (Throwable e1) {
                try {
                    crew_usage = LunaSettings.getString(MOD_ID, "ramscoop_crew_usage");
                } catch (Throwable ignored) {
                }
            }
            try {
                no_crew_gen = LunaSettings.getString(MOD_ID, "nebula_no_crew_gen");
            } catch (Throwable e2) {
                try {
                    no_crew_gen = LunaSettings.getString(MOD_ID, "ramscoop_no_crew_gen");
                } catch (Throwable ignored) {
                }
            }
            try {
                no_crew_rate = LunaSettings.getDouble(MOD_ID, "nebula_no_crew_rate").floatValue();
            } catch (Throwable e3) {
                try {
                    no_crew_rate = LunaSettings.getDouble(MOD_ID, "ramscoop_no_crew_rate").floatValue();
                } catch (Throwable ignored) {
                }
            }
            // Corona (UI provides percent/day for fuel rate)
            try {
                corona_enable_fuel = LunaSettings.getBoolean(MOD_ID, "corona_enable_fuel");
            } catch (Throwable ignored) {
            }
            try {
                corona_fuel_per_day = LunaSettings.getDouble(MOD_ID, "corona_fuel_per_day").floatValue() / 100f;
            } catch (Throwable ignored) {
            }
            try {
                corona_caps_reuse = LunaSettings.getBoolean(MOD_ID, "corona_caps_reuse");
            } catch (Throwable ignored) {
            }
            // Corona caps via UI (percent slider for soft cap)
            try {
                corona_percent_fuel_limit = LunaSettings.getDouble(MOD_ID, "corona_percent_fuel_limit").floatValue()
                        / 100f;
            } catch (Throwable ignored) {
            }
            try {
                corona_hard_fuel_limit = LunaSettings.getDouble(MOD_ID, "corona_hard_fuel_limit").floatValue();
            } catch (Throwable ignored) {
            }
            try {
                corona_fuel_cap_margin = LunaSettings.getDouble(MOD_ID, "corona_fuel_cap_margin").floatValue();
            } catch (Throwable ignored) {
            }
            try {
                scoop_toggle_default_on = LunaSettings.getBoolean(MOD_ID, "ramscoop_toggle_default_on");
                // Apply immediately at runtime so UI changes take effect without reload
                try {
                    com.fs.starfarer.api.campaign.CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
                    if (fleet != null) {
                        fleet.getMemoryWithoutUpdate().set("$ramscoop_enabled", scoop_toggle_default_on);
                    }
                } catch (Throwable ignored2) {
                }
            } catch (Throwable ignored) {
            }
            try {
                notify_nebula_entry = LunaSettings.getBoolean(MOD_ID, "ramscoop_notify_nebula_entry");
            } catch (Throwable ignored) {
            }
            try {
                notify_nebula_exit = LunaSettings.getBoolean(MOD_ID, "ramscoop_notify_nebula_exit");
            } catch (Throwable ignored) {
            }
            try {
                notify_corona_entry = LunaSettings.getBoolean(MOD_ID, "ramscoop_notify_corona_entry");
            } catch (Throwable ignored) {
            }
            try {
                notify_corona_exit = LunaSettings.getBoolean(MOD_ID, "ramscoop_notify_corona_exit");
            } catch (Throwable ignored) {
            }
            try {
                enable_visual_feedback = LunaSettings.getBoolean(MOD_ID, "ramscoop_enable_visual_feedback");
            } catch (Throwable ignored) {
            }
            try {
                floating_text_duration = LunaSettings.getDouble(MOD_ID, "ramscoop_floating_text_scale").floatValue();
            } catch (Throwable ignored) {
            }
                try {
                    // First, log raw stored LunaLib values for inspection (helps diagnose HSV picker issues)
                    logRawLunaValue("ramscoop_color_nebula_active_v2");
                    logRawLunaValue("ramscoop_color_nebula_inactive_v2");
                    logRawLunaValue("ramscoop_color_corona_active_v2");
                    logRawLunaValue("ramscoop_color_corona_inactive_v2");

                    // First try LunaLib's Color API (preferred - provides the HSV picker UI)
                Color nebulaActive = null;
                Color nebulaInactive = null;
                Color coronaActive = null;
                Color coronaInactive = null;
                // No preset radio values: rely on LunaLib Color pickers and hex fallbacks

                try {
                    // Prefer the new v2 keys to avoid reading old malformed stored values
                    nebulaActive = LunaSettings.getColor(MOD_ID, "ramscoop_color_nebula_active_v2");
                    // nebulaActive read from LunaLib
                } catch (Throwable t) {
                    // not fatal, we'll try fallback below
                }
                try {
                    nebulaInactive = LunaSettings.getColor(MOD_ID, "ramscoop_color_nebula_inactive_v2");
                    // nebulaInactive read from LunaLib
                } catch (Throwable t) {
                }
                try {
                    coronaActive = LunaSettings.getColor(MOD_ID, "ramscoop_color_corona_active_v2");
                    // coronaActive read from LunaLib
                } catch (Throwable t) {
                }
                try {
                    coronaInactive = LunaSettings.getColor(MOD_ID, "ramscoop_color_corona_inactive_v2");
                    // coronaInactive read from LunaLib
                } catch (Throwable t) {
                }

                // If any of the above are null (LunaLib didn't provide a Color), use defaults
                if (nebulaActive == null) {
                    nebulaActive = color_nebula_active; // use default
                }
                if (nebulaInactive == null) {
                    nebulaInactive = color_nebula_inactive; // use default
                }
                if (coronaActive == null) {
                    coronaActive = color_corona_active; // use default
                }
                if (coronaInactive == null) {
                    coronaInactive = color_corona_inactive; // use default
                }

                // Allow user-selected presets (radio buttons). If the preset is not Custom,
                // apply the preset (explicit user intent). If the preset is Custom or
                // missing, preserve any LunaLib Color value already read above.
                // Preset radio options removed: rely solely on LunaLib Color pickers and
                // legacy hex string fallbacks above.

                setColorsFromSelections(nebulaActive, nebulaInactive, coronaActive, coronaInactive);

                // Debug: log resolved colors so we can see what colors are actually being used
                try {
                    LOG.info(String.format("[Ramscoop] Resolved colors - nebulaActive: #%02X%02X%02X alpha=%d, nebulaInactive: #%02X%02X%02X alpha=%d, coronaActive: #%02X%02X%02X alpha=%d, coronaInactive: #%02X%02X%02X alpha=%d",
                            color_nebula_active.getRed(), color_nebula_active.getGreen(), color_nebula_active.getBlue(), color_nebula_active.getAlpha(),
                            color_nebula_inactive.getRed(), color_nebula_inactive.getGreen(), color_nebula_inactive.getBlue(), color_nebula_inactive.getAlpha(),
                            color_corona_active.getRed(), color_corona_active.getGreen(), color_corona_active.getBlue(), color_corona_active.getAlpha(),
                            color_corona_inactive.getRed(), color_corona_inactive.getGreen(), color_corona_inactive.getBlue(), color_corona_inactive.getAlpha()));
                } catch (Throwable ignored) {
                }
            } catch (Throwable t) {
                LOG.warn("[Ramscoop] Error reading color settings (falling back to defaults): " + t.getMessage());
            }

            // Debug logging
            LOG.info(
                    "[Ramscoop] Final settings from LunaLib -> fuel=" + enable_fuel +
                            ", supplies=" + enable_supplies +
                            ", fuel_per_day=" + fuel_per_day +
                            ", nebula_percent_fuel_limit=" + nebula_percent_fuel_limit +
                            ", nebula_hard_fuel_limit=" + nebula_hard_fuel_limit +
                            ", nebula_fuel_cap_margin=" + nebula_fuel_cap_margin +
                            ", corona_percent_fuel_limit=" + corona_percent_fuel_limit +
                            ", corona_hard_fuel_limit=" + corona_hard_fuel_limit +
                            ", corona_fuel_cap_margin=" + corona_fuel_cap_margin +
                            ", percent_supply_limit=" + percent_supply_limit +
                            ", hard_supply_limit=" + hard_supply_limit +
                            ", crew_usage=" + crew_usage +
                                ", no_crew_gen=" + no_crew_gen +
                                ", no_crew_rate=" + no_crew_rate +
                                ", notify_nebula_entry=" + notify_nebula_entry +
                                ", notify_nebula_exit=" + notify_nebula_exit +
                                ", notify_corona_entry=" + notify_corona_entry +
                                ", notify_corona_exit=" + notify_corona_exit);
        } catch (Exception e) {
            LOG.warn("[Ramscoop] Failed to load LunaLib settings: " + e.getMessage(), e);
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
                fuel_per_day = (float) config.getDouble("fuel_per_day");
            }
            // Optional new fuel limit settings (legacy JSON)
            if (config.has("nebula_percent_fuel_limit"))
                nebula_percent_fuel_limit = (float) config.getDouble("nebula_percent_fuel_limit");
            if (config.has("nebula_hard_fuel_limit"))
                nebula_hard_fuel_limit = (float) config.getDouble("nebula_hard_fuel_limit");
            if (config.has("nebula_fuel_cap_margin"))
                nebula_fuel_cap_margin = (float) config.getDouble("nebula_fuel_cap_margin");

            percent_supply_limit = (float) config.getDouble("percent_supply_limit");
            hard_supply_limit = (float) config.getDouble("hard_supply_limit");

            supplies_per_crew = (float) config.getDouble("supply_per_crew");
            // Accept new nebula_* keys or legacy names
            if (config.has("nebula_crew_usage"))
                crew_usage = config.get("nebula_crew_usage").toString();
            else
                crew_usage = config.get("crew_usage").toString();
            if (config.has("nebula_no_crew_gen"))
                no_crew_gen = config.get("nebula_no_crew_gen").toString();
            else
                no_crew_gen = config.get("no_crew_gen").toString();
            if (config.has("nebula_no_crew_rate"))
                no_crew_rate = (float) config.getDouble("nebula_no_crew_rate");
            else
                no_crew_rate = (float) config.getDouble("no_crew_rate");
            if (config.has("scoop_toggle_default_on")) {
                try {
                    scoop_toggle_default_on = config.getBoolean("scoop_toggle_default_on");
                } catch (Throwable ignored) {
                }
            }
            // Corona legacy
            if (config.has("corona_enable_fuel"))
                corona_enable_fuel = config.getBoolean("corona_enable_fuel");
            if (config.has("corona_fuel_per_day"))
                corona_fuel_per_day = (float) config.getDouble("corona_fuel_per_day");
            if (config.has("corona_caps_reuse"))
                corona_caps_reuse = config.getBoolean("corona_caps_reuse");
            if (config.has("corona_percent_fuel_limit"))
                corona_percent_fuel_limit = (float) config.getDouble("corona_percent_fuel_limit");
            if (config.has("corona_hard_fuel_limit"))
                corona_hard_fuel_limit = (float) config.getDouble("corona_hard_fuel_limit");
            if (config.has("corona_fuel_cap_margin"))
                corona_fuel_cap_margin = (float) config.getDouble("corona_fuel_cap_margin");
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
                ", nebula_percent_fuel_limit=" + nebula_percent_fuel_limit +
                ", nebula_hard_fuel_limit=" + nebula_hard_fuel_limit +
                ", nebula_fuel_cap_margin=" + nebula_fuel_cap_margin +
                ", corona_percent_fuel_limit=" + corona_percent_fuel_limit +
                ", corona_hard_fuel_limit=" + corona_hard_fuel_limit +
                ", corona_fuel_cap_margin=" + corona_fuel_cap_margin +
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
        // No debug startup floating text (feature verified in-game)
        LOG.info("[Ramscoop] initialization complete");
    }

    /**
     * Public method for reloading settings - can be called by Ramscoop periodically
     * This method will retry LunaLib loading if it wasn't ready before
     */
    public static void reloadSettings() {
        // If LunaLib is present, try to refresh LunaLib-backed settings every time
        // this method is called. This allows runtime toggles (from the LunaLib UI)
        // to be applied to the player's fleet memory without requiring a reload.
        try {
            if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
                LOG.info("[Ramscoop] Reloading LunaLib settings (runtime refresh)");
                try {
                    // Load only the LunaLib-driven values (less heavy than full load)
                    loadLunaLibSettings();
                    lunaLibReady = true;
                    settingsLoaded = true;
                    LOG.info("[Ramscoop] LunaLib settings reloaded successfully");
                } catch (Throwable t) {
                    // Don't throw - we'll retry later. Keep existing values and log.
                    LOG.warn("[Ramscoop] Failed to reload LunaLib settings: " + t.getMessage());
                }
                return;
            }
        } catch (Throwable ignored) {
            // Defensive: if mod manager isn't available for some reason, fall back
        }

        // If LunaLib isn't enabled or wasn't available, fall back to attempting a
        // full settings load when nothing has been loaded yet.
        if (!settingsLoaded) {
            System.out.println("Ramscoop: No settings loaded yet, attempting to load...");
            LOG.info("[Ramscoop] No settings loaded yet, attempting to load...");
            loadSettings();
        } else {
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
