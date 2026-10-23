package ramscoop;

/** Run by build.sh against the game jars: checks Ramscoop.fuelToAdd and suppliesToAdd caps, and the edge debounce. */
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
        // Supplies: 1000 cargo, 80% cap, 100 supplies plus 600 other cargo (300 free).
        // Counting supplies only, free space is the limit; counting all cargo, the cap is.
        check(Ramscoop.suppliesToAdd(1000f, 100f, 300f, 0.8f, 0f, 500f), 300f);
        check(Ramscoop.suppliesToAdd(1000f, 700f, 300f, 0.8f, 0f, 500f), 100f);
        // Hard cap wins over soft cap; generation below the room is added in full.
        check(Ramscoop.suppliesToAdd(1000f, 0f, 1000f, 1f, 50f, 500f), 50f);
        check(Ramscoop.suppliesToAdd(1000f, 0f, 1000f, 1f, 0f, 5f), 5f);
        // Above cap never removes supplies.
        check(Ramscoop.suppliesToAdd(1000f, 900f, 100f, 0.8f, 0f, 500f), 0f);
        // Debounce: first reading is taken as is; a flip needs SETTLE_TICKS readings in a row.
        Ramscoop.Settled s = new Ramscoop.Settled();
        checkTrue(!s.update(true) && s.value);
        for (int i = 1; i < Ramscoop.SETTLE_TICKS; i++) {
            checkTrue(!s.update(false) && s.value);
        }
        checkTrue(!s.update(true) && s.value); // edge flicker resets the count
        for (int i = 1; i < Ramscoop.SETTLE_TICKS; i++) {
            s.update(false);
        }
        checkTrue(s.update(false) && !s.value);
        System.out.println("FuelMathCheck passed");
    }

    private static void checkTrue(boolean ok) {
        if (!ok) {
            throw new AssertionError("debounce check failed");
        }
    }

    private static void check(float actual, float expected) {
        if (Math.abs(actual - expected) > 1e-4f) {
            throw new AssertionError("expected " + expected + " got " + actual);
        }
    }
}
