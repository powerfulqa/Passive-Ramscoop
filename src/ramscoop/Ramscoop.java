package ramscoop;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignUIAPI;
import com.fs.starfarer.api.campaign.CampaignTerrainAPI;
import com.fs.starfarer.api.campaign.CampaignTerrainPlugin;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.listeners.CampaignInputListener;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.combat.BattleCreationPluginImpl.NebulaTextureProvider;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import org.apache.log4j.Logger;

import java.awt.Color;
import java.util.List;

/**
 * Generates resources for the player fleet:
 * - nebula: supplies plus slow fuel
 * - star corona: fast fuel, plus opt-in supplies (each takes over from the nebula rate when both apply)
 * What was collected is posted to the message log on leaving (and on save, see ModPlugin).
 * Also listens for the optional toggle key, which flips the master switch until the settings reload.
 */
public class Ramscoop implements EveryFrameScript, CampaignInputListener {
    private static final Logger LOG = Global.getLogger(Ramscoop.class);

    private final IntervalUtil interval = new IntervalUtil(0.09f, 0.11f);
    private float elapsedSinceTick = 0f;
    private boolean errorLogged = false;

    // Ticks (~0.1s each) a nebula/corona change must last before it counts, so ragged nebula edges do not flicker.
    static final int SETTLE_TICKS = 5;

    // null = unknown, so the first observation after load never shows a cue
    private Boolean lastEnabled = null;
    private final Settled nebula = new Settled();
    private final Settled corona = new Settled();
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
            nebula.reset();
            corona.reset();
            return;
        }

        boolean rawNebula = false;
        boolean rawCorona = false;
        LocationAPI loc = fleet.getContainingLocation();
        // Hyperspace has no nebula or corona terrain; skip copying its terrain list every tick.
        if (loc != null && !loc.isHyperspace()) {
            for (CampaignTerrainAPI t : loc.getTerrainCopy()) {
                CampaignTerrainPlugin plugin = t.getPlugin();
                if (!rawNebula && plugin instanceof NebulaTextureProvider) {
                    rawNebula = plugin.containsEntity(fleet);
                } else if (!rawCorona && isCorona(t.getType())) {
                    rawCorona = plugin.containsEntity(fleet);
                }
                if (rawNebula && rawCorona) {
                    break;
                }
            }
        }

        boolean inNebula = cue(fleet, nebula, rawNebula, "Nebula", ModPlugin.notify_nebula_entry,
                ModPlugin.notify_nebula_exit, ModPlugin.color_nebula_active, ModPlugin.color_nebula_inactive);
        boolean inCorona = cue(fleet, corona, rawCorona, "Corona", ModPlugin.notify_corona_entry,
                ModPlugin.notify_corona_exit, ModPlugin.color_corona_active, ModPlugin.color_corona_inactive);

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

    /** Supplies to add: gen, limited by free cargo space and by the soft/hard cap minus what the caps count. */
    static float suppliesToAdd(float maxCapacity, float counted, float spaceLeft, float pctCap, float hardCap,
            float gen) {
        float cap = maxCapacity * pctCap;
        if (hardCap > 0f) {
            cap = Math.min(cap, hardCap);
        }
        return Math.max(0f, Math.min(gen, Math.min(cap - counted, spaceLeft)));
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
        // The caps count supplies only, or everything in the hold when supply_cap_all_cargo is on.
        float counted = ModPlugin.supply_cap_all_cargo ? cargo.getSpaceUsed() : cargo.getSupplies();
        float add = suppliesToAdd(cargo.getMaxCapacity(), counted, cargo.getSpaceLeft(), ModPlugin.percent_supply_limit,
                ModPlugin.hard_supply_limit, perDay * rateMult * days);
        if (add <= 0f) {
            return 0f;
        }
        cargo.addSupplies(add);
        return add;
    }

    /**
     * Posts what was collected since the last report to the message log and resets what was posted.
     * Totals under one unit are kept and added to the next report instead of posting "+0.2 fuel".
     */
    void reportGains() {
        if (!ModPlugin.report_gains) {
            fuelGained = 0f;
            suppliesGained = 0f;
            return;
        }
        String fuel = gain(fuelGained, "fuel");
        String supplies = gain(suppliesGained, "supplies");
        if (fuel != null) {
            fuelGained = 0f;
        }
        if (supplies != null) {
            suppliesGained = 0f;
        }
        if (fuel == null && supplies == null) {
            return;
        }
        String both = fuel != null && supplies != null ? fuel + " and " + supplies : fuel != null ? fuel : supplies;
        Global.getSector().getCampaignUI().addMessage("Ramscoop collected " + both, Misc.getPositiveHighlightColor());
    }

    private static String gain(float amount, String what) {
        return amount >= 1f ? "+" + Misc.getRoundedValueMaxOneAfterDecimal(amount) + " " + what : null;
    }

    /** Settles the raw in/out reading, shows an enter/exit cue when the settled state flips, and returns it. */
    private static boolean cue(CampaignFleetAPI fleet, Settled state, boolean raw, String where, boolean showEntry,
            boolean showExit, Color entryColor, Color exitColor) {
        boolean flipped = state.update(raw);
        boolean in = state.value;
        if (flipped && (in ? showEntry : showExit)) {
            showText(fleet, where + (in ? ": scooping" : ": stopped"), in ? entryColor : exitColor);
        }
        return in;
    }

    /** A boolean that only changes once a new value has been seen SETTLE_TICKS ticks in a row; null until first seen. */
    static final class Settled {
        Boolean value;
        private int pending;

        /** Returns true when the settled value flips; the first observation is taken as is and never counts as a flip. */
        boolean update(boolean now) {
            if (value == null || value == now) {
                value = now;
                pending = 0;
                return false;
            }
            if (++pending < SETTLE_TICKS) {
                return false;
            }
            value = now;
            pending = 0;
            return true;
        }

        void reset() {
            value = null;
            pending = 0;
        }
    }

    private static void showText(CampaignFleetAPI fleet, String text, Color color) {
        if (ModPlugin.enable_visual_feedback) {
            fleet.addFloatingText(text, color, ModPlugin.floating_text_duration);
        }
    }

    @Override
    public void processCampaignInputPreCore(List<InputEventAPI> events) {
        if (ModPlugin.toggle_key <= 0) {
            return;
        }
        // Only on the campaign map, so typing in a menu or dialog never flips the switch.
        CampaignUIAPI ui = Global.getSector().getCampaignUI();
        if (ui.isShowingDialog() || ui.isShowingMenu() || ui.getCurrentCoreTab() != null) {
            return;
        }
        for (InputEventAPI e : events) {
            if (!e.isConsumed() && e.isKeyDownEvent() && !e.isRepeat() && e.getEventValue() == ModPlugin.toggle_key) {
                e.consume();
                ModPlugin.scoop_enabled = !ModPlugin.scoop_enabled;
                ui.addMessage(ModPlugin.scoop_enabled ? "Ramscoop switched on" : "Ramscoop switched off",
                        Misc.getHighlightColor());
            }
        }
    }

    @Override
    public void processCampaignInputPreFleetControl(List<InputEventAPI> events) {
    }

    @Override
    public void processCampaignInputPostCore(List<InputEventAPI> events) {
    }

    @Override
    public int getListenerInputPriority() {
        return 0;
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
