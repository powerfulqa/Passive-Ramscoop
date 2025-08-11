package ramscoop;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import org.json.JSONObject;

public class ModPlugin extends BaseModPlugin {
    public static boolean enable_fuel = true;
    public static boolean enable_supplies = true;
    public static float supplies_per_crew = 0.1f;
    public static float percent_supply_limit = 0.25f;
    public static float hard_supply_limit = 500.0f;
    public static String crew_usage = "extra";
    public static String no_crew_gen = "percent";
    public static float no_crew_rate = 0.01f;

    @Override
    public void onGameLoad(boolean newGame) {
        try {
            JSONObject config = Global.getSettings().loadJSON("settings.json", "m561_ramscoop"); // Updated to include modId
            enable_fuel = config.getBoolean("enable_fuel");
            enable_supplies = config.getBoolean("enable_supplies");
            supplies_per_crew = (float)config.getDouble("supply_per_crew");
            percent_supply_limit = (float)config.getDouble("percent_supply_limit");
            hard_supply_limit = (float)config.getDouble("hard_supply_limit");
            crew_usage = config.getString("crew_usage");
            no_crew_gen = config.getString("no_crew_gen");
            no_crew_rate = (float)config.getDouble("no_crew_rate");
            Global.getSector().addTransientScript(new Ramscoop());
        }
        catch (Exception exception) {
            // Use simple println for logging errors to avoid log4j dependency issues
            System.out.println("Ramscoop: Error loading settings - " + exception.getMessage());
            exception.printStackTrace();
        }
    }
}
