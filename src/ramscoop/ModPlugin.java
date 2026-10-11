package ramscoop;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import lunalib.lunaSettings.LunaSettings;
import lunalib.lunaSettings.LunaSettingsListener;
import org.apache.log4j.Logger;

import java.awt.Color;

/**
 * Loads settings from LunaLib (a hard dependency, see mod_info.json) and starts the
 * Ramscoop script. Settings are re-read only when LunaLib reports a change.
 *
 * Percent sliders in LunaSettings.csv are stored as 0..100 and converted to fractions
 * here, so Ramscoop only ever sees fractions.
 */
public class ModPlugin extends BaseModPlugin implements LunaSettingsListener {
    private static final Logger LOG = Global.getLogger(ModPlugin.class);
    public static final String MOD_ID = "m561_ramscoop";

    // Defaults mirror LunaSettings.csv and are only used if LunaLib returns null.
    private static final Color DEFAULT_TEXT_COLOR = new Color(0xD3, 0xD3, 0xD3);

    // General
    public static boolean scoop_enabled = true;
    public static boolean enable_fuel = true;
    public static boolean enable_supplies = true;
    // Nebula fuel (fractions of max fuel)
    public static boolean nebula_enable_fuel = true;
    public static float nebula_fuel_per_day = 0.04f;
    public static float nebula_percent_fuel_limit = 0.20f;
    public static float nebula_hard_fuel_limit = 0f;
    public static float nebula_fuel_cap_margin = 0f;
    // Nebula supplies
    public static float percent_supply_limit = 0.20f; // fraction of cargo capacity
    public static float hard_supply_limit = 0f;
    public static float supplies_per_crew = 0.1f;
    public static String crew_usage = "extra"; // extra | all | nocrew
    public static String no_crew_gen = "percent"; // percent | flat
    public static float no_crew_rate_percent = 0.10f; // fraction of cargo capacity per day
    public static float no_crew_rate_flat = 10f; // units per day
    // Corona fuel (fractions of max fuel)
    public static boolean corona_enable_fuel = true;
    public static float corona_fuel_per_day = 0.25f;
    public static boolean corona_caps_reuse = true;
    public static float corona_percent_fuel_limit = 0.20f;
    public static float corona_hard_fuel_limit = 0f;
    public static float corona_fuel_cap_margin = 0f;
    // Visual feedback
    public static boolean enable_visual_feedback = true;
    public static float floating_text_duration = 0.7f;
    public static boolean notify_nebula_entry = true;
    public static boolean notify_nebula_exit = true;
    public static boolean notify_corona_entry = true;
    public static boolean notify_corona_exit = true;
    public static Color color_nebula_active = DEFAULT_TEXT_COLOR;
    public static Color color_nebula_inactive = DEFAULT_TEXT_COLOR;
    public static Color color_corona_active = DEFAULT_TEXT_COLOR;
    public static Color color_corona_inactive = DEFAULT_TEXT_COLOR;

    @Override
    public void onApplicationLoad() {
        // Register once per application run; LunaLib keeps listeners in a static list
        // that is never cleared, so registering in onGameLoad would add duplicates.
        if (!LunaSettings.hasSettingsListenerOfClass(ModPlugin.class)) {
            LunaSettings.addSettingsListener(this);
        }
    }

    @Override
    public void onGameLoad(boolean newGame) {
        loadSettings();
        // Transient: not written to the save, so re-adding on every load cannot stack.
        Global.getSector().addTransientScript(new Ramscoop());
        // Versions <= 0.7.3 kept the toggle in fleet memory, which leaked into saves.
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        if (fleet != null) {
            fleet.getMemoryWithoutUpdate().unset("$ramscoop_enabled");
        }
    }

    @Override
    public void settingsChanged(String modID) {
        if (MOD_ID.equals(modID)) {
            loadSettings();
        }
    }

    static void loadSettings() {
        scoop_enabled = or(LunaSettings.getBoolean(MOD_ID, "ramscoop_toggle_default_on"), scoop_enabled);
        enable_fuel = or(LunaSettings.getBoolean(MOD_ID, "ramscoop_enable_fuel"), enable_fuel);
        enable_supplies = or(LunaSettings.getBoolean(MOD_ID, "ramscoop_enable_supplies"), enable_supplies);

        nebula_enable_fuel = or(LunaSettings.getBoolean(MOD_ID, "nebula_enable_fuel"), nebula_enable_fuel);
        nebula_fuel_per_day = pct(LunaSettings.getDouble(MOD_ID, "nebula_fuel_per_day"), nebula_fuel_per_day);
        nebula_percent_fuel_limit = pct(LunaSettings.getDouble(MOD_ID, "nebula_percent_fuel_limit"), nebula_percent_fuel_limit);
        nebula_hard_fuel_limit = or(LunaSettings.getDouble(MOD_ID, "nebula_hard_fuel_limit"), nebula_hard_fuel_limit);
        nebula_fuel_cap_margin = or(LunaSettings.getDouble(MOD_ID, "nebula_fuel_cap_margin"), nebula_fuel_cap_margin);

        // This slider is already a fraction (0.1..1.0) in the CSV.
        percent_supply_limit = or(LunaSettings.getDouble(MOD_ID, "ramscoop_percent_supply_limit"), percent_supply_limit);
        hard_supply_limit = or(LunaSettings.getDouble(MOD_ID, "ramscoop_hard_supply_limit"), hard_supply_limit);
        supplies_per_crew = or(LunaSettings.getDouble(MOD_ID, "ramscoop_supply_per_crew"), supplies_per_crew);
        crew_usage = or(LunaSettings.getString(MOD_ID, "nebula_crew_usage"), crew_usage);
        no_crew_gen = or(LunaSettings.getString(MOD_ID, "nebula_no_crew_gen"), no_crew_gen);
        no_crew_rate_percent = pct(LunaSettings.getDouble(MOD_ID, "nebula_no_crew_rate_percent"), no_crew_rate_percent);
        no_crew_rate_flat = or(LunaSettings.getDouble(MOD_ID, "nebula_no_crew_rate_flat"), no_crew_rate_flat);

        corona_enable_fuel = or(LunaSettings.getBoolean(MOD_ID, "corona_enable_fuel"), corona_enable_fuel);
        corona_fuel_per_day = pct(LunaSettings.getDouble(MOD_ID, "corona_fuel_per_day"), corona_fuel_per_day);
        corona_caps_reuse = or(LunaSettings.getBoolean(MOD_ID, "corona_caps_reuse"), corona_caps_reuse);
        corona_percent_fuel_limit = pct(LunaSettings.getDouble(MOD_ID, "corona_percent_fuel_limit"), corona_percent_fuel_limit);
        corona_hard_fuel_limit = or(LunaSettings.getDouble(MOD_ID, "corona_hard_fuel_limit"), corona_hard_fuel_limit);
        corona_fuel_cap_margin = or(LunaSettings.getDouble(MOD_ID, "corona_fuel_cap_margin"), corona_fuel_cap_margin);

        enable_visual_feedback = or(LunaSettings.getBoolean(MOD_ID, "ramscoop_enable_visual_feedback"), enable_visual_feedback);
        floating_text_duration = or(LunaSettings.getDouble(MOD_ID, "ramscoop_floating_text_scale"), floating_text_duration);
        notify_nebula_entry = or(LunaSettings.getBoolean(MOD_ID, "ramscoop_notify_nebula_entry"), notify_nebula_entry);
        notify_nebula_exit = or(LunaSettings.getBoolean(MOD_ID, "ramscoop_notify_nebula_exit"), notify_nebula_exit);
        notify_corona_entry = or(LunaSettings.getBoolean(MOD_ID, "ramscoop_notify_corona_entry"), notify_corona_entry);
        notify_corona_exit = or(LunaSettings.getBoolean(MOD_ID, "ramscoop_notify_corona_exit"), notify_corona_exit);
        color_nebula_active = or(LunaSettings.getColor(MOD_ID, "ramscoop_color_nebula_active_v2"), color_nebula_active);
        color_nebula_inactive = or(LunaSettings.getColor(MOD_ID, "ramscoop_color_nebula_inactive_v2"), color_nebula_inactive);
        color_corona_active = or(LunaSettings.getColor(MOD_ID, "ramscoop_color_corona_active_v2"), color_corona_active);
        color_corona_inactive = or(LunaSettings.getColor(MOD_ID, "ramscoop_color_corona_inactive_v2"), color_corona_inactive);

        LOG.info("[Ramscoop] Settings loaded: enabled=" + scoop_enabled
                + " fuel=" + enable_fuel + " supplies=" + enable_supplies
                + " nebulaFuel=" + nebula_enable_fuel + "@" + nebula_fuel_per_day
                + " coronaFuel=" + corona_enable_fuel + "@" + corona_fuel_per_day
                + " coronaCapsReuse=" + corona_caps_reuse
                + " crewUsage=" + crew_usage + " noCrewGen=" + no_crew_gen);
    }

    // LunaLib getters return null (and log) for missing keys or type mismatches.
    private static boolean or(Boolean v, boolean fallback) {
        return v != null ? v : fallback;
    }

    private static float or(Double v, float fallback) {
        return v != null ? v.floatValue() : fallback;
    }

    private static float pct(Double v, float fallback) {
        return v != null ? v.floatValue() / 100f : fallback;
    }

    private static String or(String v, String fallback) {
        return v != null ? v.trim() : fallback;
    }

    private static Color or(Color v, Color fallback) {
        return v != null ? v : fallback;
    }
}
