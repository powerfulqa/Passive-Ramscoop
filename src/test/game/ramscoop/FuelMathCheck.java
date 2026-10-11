package ramscoop;

/** Run by build.sh against the game jars: checks Ramscoop.fuelToAdd caps and rounding. */
public class FuelMathCheck {
    public static void main(String[] args) {
        // Small tank still generates (old Math.floor rounded 20 * 4% down to 0).
        check(Ramscoop.fuelToAdd(20f, 0f, 0.04f, 1f, 0f, 0f, 1f), 0.8f);
        // Soft cap 20% of 100 = 20; at 19 only 1 unit of room.
        check(Ramscoop.fuelToAdd(100f, 19f, 0.25f, 0.2f, 0f, 0f, 1f), 1f);
        // Hard cap wins over soft cap.
        check(Ramscoop.fuelToAdd(1000f, 0f, 1f, 1f, 50f, 0f, 1f), 50f);
        // Margin keeps room below the cap.
        check(Ramscoop.fuelToAdd(100f, 15f, 1f, 0.2f, 0f, 5f, 1f), 0f);
        // Above cap never removes fuel.
        check(Ramscoop.fuelToAdd(100f, 90f, 1f, 0.2f, 0f, 0f, 1f), 0f);
        System.out.println("FuelMathCheck passed");
    }

    private static void check(float actual, float expected) {
        if (Math.abs(actual - expected) > 1e-4f) {
            throw new AssertionError("expected " + expected + " got " + actual);
        }
    }
}
