package ramscoop;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignTerrainAPI;
import com.fs.starfarer.api.campaign.CampaignTerrainPlugin;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.combat.BattleCreationPluginImpl.NebulaTextureProvider;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import org.apache.log4j.Logger;

import java.awt.Color;

/**
 * Generates resources for the player fleet:
 * - nebula: supplies plus slow fuel
 * - star corona: fast fuel, plus opt-in supplies (each takes over from the nebula rate when both apply)
 * What was collected is posted to the message log on leaving (and on save, see ModPlugin).
 */
public class Ramscoop implements EveryFrameScript {
    private static final Logger LOG = Global.getLogger(Ramscoop.class);

    private final IntervalUtil interval = new IntervalUtil(0.09f, 0.11f);
    private float elapsedSinceTick = 0f;
    private boolean errorLogged = false;

    // null = unknown, so the first observation after load never shows a cue
    private Boolean lastEnabled = null;
    private Boolean lastInNebula = null;
    private Boolean lastInCorona = null;
    private float fuelGained = 0f;
    private float suppliesGained = 0f;

    @Override
    public void advance(float amount) {
        elapsedSinceTick += amount;
        interval.advance(amount);
        if (!interval.intervalElapsed()) {
            return;
        }
        float days = Global.getSector().getClock().convertToDays(elapsedSinceTick);
        elapsedSinceTick = 0f;

        try {
            tick(days);
        } catch (RuntimeException e) {
            // Never crash the campaign over a resource tick; log once per session.
            if (!errorLogged) {
                LOG.error("[Ramscoop] Tick failed", e);
                errorLogged = true;
            }
        }
    }

    private void tick(float days) {
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        if (fleet == null) {
            return;
        }

        boolean enabled = ModPlugin.scoop_enabled;
        if (lastEnabled != null && lastEnabled != enabled) {
            showText(fleet, enabled ? "Ramscoop Active" : "Ramscoop Inactive",
                    enabled ? ModPlugin.color_nebula_active : ModPlugin.color_nebula_inactive);
        }
        lastEnabled = enabled;
        if (!enabled) {
            reportGains();
            lastInNebula = null;
            lastInCorona = null;
            return;
        }

        boolean inNebula = false;
        boolean inCorona = false;
        LocationAPI loc = fleet.getContainingLocation();
        if (loc != null) {
            for (CampaignTerrainAPI t : loc.getTerrainCopy()) {
                CampaignTerrainPlugin plugin = t.getPlugin();
                if (!inNebula && plugin instanceof NebulaTextureProvider) {
                    inNebula = plugin.containsEntity(fleet);
                } else if (!inCorona && isCorona(t.getType())) {
                    inCorona = plugin.containsEntity(fleet);
                }
                if (inNebula && inCorona) {
                    break;
                }
            }
        }

        lastInNebula = cue(fleet, lastInNebula, inNebula, ModPlugin.notify_nebula_entry, ModPlugin.notify_nebula_exit,
                ModPlugin.color_nebula_active, ModPlugin.color_nebula_inactive);
        lastInCorona = cue(fleet, lastInCorona, inCorona, ModPlugin.notify_corona_entry, ModPlugin.notify_corona_exit,
                ModPlugin.color_corona_active, ModPlugin.color_corona_inactive);

        if (!inNebula && !inCorona) {
            reportGains();
            return;
        }

        CargoAPI cargo = fleet.getCargo();
        if (ModPlugin.enable_fuel) {
            if (inCorona && ModPlugin.corona_enable_fuel) {
                if (ModPlugin.corona_caps_reuse) {
                    fuelGained += addFuel(cargo, ModPlugin.corona_fuel_per_day, ModPlugin.nebula_percent_fuel_limit,
                            ModPlugin.nebula_hard_fuel_limit, ModPlugin.nebula_fuel_cap_margin, days);
                } else {
                    fuelGained += addFuel(cargo, ModPlugin.corona_fuel_per_day, ModPlugin.corona_percent_fuel_limit,
                            ModPlugin.corona_hard_fuel_limit, ModPlugin.corona_fuel_cap_margin, days);
                }
            } else if (inNebula && ModPlugin.nebula_enable_fuel) {
                fuelGained += addFuel(cargo, ModPlugin.nebula_fuel_per_day, ModPlugin.nebula_percent_fuel_limit,
                        ModPlugin.nebula_hard_fuel_limit, ModPlugin.nebula_fuel_cap_margin, days);
            }
        }
        if (ModPlugin.enable_supplies) {
            if (inCorona && ModPlugin.corona_enable_supplies) {
                suppliesGained += addSupplies(fleet, cargo, ModPlugin.corona_supply_rate, days);
            } else if (inNebula) {
                suppliesGained += addSupplies(fleet, cargo, 1f, days);
            }
        }
    }

    private static boolean isCorona(String type) {
        return Terrain.CORONA.equals(type) || Terrain.CORONA_JET.equals(type)
                || Terrain.CORONA_AKA_MAINYU.equals(type);
    }

    /** Adds fuel at ratePerDay (fraction of max fuel) up to the soft/hard cap minus margin. */
    static float fuelToAdd(float maxFuel, float fuel, float ratePerDay, float pctCap, float hardCap, float margin,
            float days) {
        float cap = maxFuel * Math.min(1f, pctCap);
        if (hardCap > 0f) {
            cap = Math.min(cap, hardCap);
        }
        float room = cap - Math.max(0f, margin) - fuel;
        return Math.max(0f, Math.min(maxFuel * ratePerDay * days, room));
    }

    private static float addFuel(CargoAPI cargo, float ratePerDay, float pctCap, float hardCap, float margin,
            float days) {
        float add = fuelToAdd(cargo.getMaxFuel(), cargo.getFuel(), ratePerDay, pctCap, hardCap, margin, days);
        if (add > 0f) {
            cargo.addFuel(add);
        }
        return add;
    }

    /** Adds supplies at the configured nebula rate times rateMult; returns the amount added. */
    private static float addSupplies(CampaignFleetAPI fleet, CargoAPI cargo, float rateMult, float days) {
        float perDay;
        switch (ModPlugin.crew_usage) {
            case "nocrew":
                perDay = "flat".equals(ModPlugin.no_crew_gen)
                        ? ModPlugin.no_crew_rate_flat
                        : cargo.getMaxCapacity() * ModPlugin.no_crew_rate_percent;
                break;
            case "all":
                perDay = cargo.getCrew() * ModPlugin.supplies_per_crew;
                break;
            default: // "extra"
                perDay = Math.max(0f, cargo.getCrew() - fleet.getFleetData().getMinCrew())
                        * ModPlugin.supplies_per_crew;
                break;
        }
        float cap = cargo.getMaxCapacity() * ModPlugin.percent_supply_limit;
        if (ModPlugin.hard_supply_limit > 0f) {
            cap = Math.min(cap, ModPlugin.hard_supply_limit);
        }
        float add = Math.min(perDay * rateMult * days, Math.min(cap - cargo.getSupplies(), cargo.getSpaceLeft()));
        if (add <= 0f) {
            return 0f;
        }
        cargo.addSupplies(add);
        return add;
    }

    /** Posts what was collected since the last report to the message log, then resets the totals. */
    void reportGains() {
        String fuel = gain(fuelGained, "fuel");
        String supplies = gain(suppliesGained, "supplies");
        fuelGained = 0f;
        suppliesGained = 0f;
        if (!ModPlugin.report_gains || (fuel == null && supplies == null)) {
            return;
        }
        String both = fuel != null && supplies != null ? fuel + " and " + supplies : fuel != null ? fuel : supplies;
        Global.getSector().getCampaignUI().addMessage("Ramscoop collected " + both, Misc.getPositiveHighlightColor());
    }

    private static String gain(float amount, String what) {
        return amount >= 0.05f ? "+" + Misc.getRoundedValueMaxOneAfterDecimal(amount) + " " + what : null;
    }

    /** Shows an enter/exit cue when the state flips; returns the new state to remember. */
    private static Boolean cue(CampaignFleetAPI fleet, Boolean last, boolean now, boolean showEntry,
            boolean showExit, Color entryColor, Color exitColor) {
        if (last != null && last != now && (now ? showEntry : showExit)) {
            showText(fleet, now ? "Ramscoop:Active" : "Ramscoop:Inactive", now ? entryColor : exitColor);
        }
        return now;
    }

    private static void showText(CampaignFleetAPI fleet, String text, Color color) {
        if (ModPlugin.enable_visual_feedback) {
            fleet.addFloatingText(text, color, ModPlugin.floating_text_duration);
        }
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }
}
