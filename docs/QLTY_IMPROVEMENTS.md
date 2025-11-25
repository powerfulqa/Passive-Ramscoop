# QLTY Standards & Architectural Improvements

This document outlines a plan for refactoring the Passive Ramscoop mod to align with [QLTY standards](https://docs.qlty.sh/what-is-qlty) (Reliability, Maintainability, Observability). These changes are architectural improvements intended for a future update to enhance robustness and developer experience without altering core gameplay behavior.

## 1. Reliability: Robust Settings Loading

**Current Risk:**
The "Two-Class Design" relies on `ModPlugin` loading settings via reflection from LunaLib. If a specific key is missing or malformed, it could throw an exception. Additionally, if LunaLib is enabled in the launcher but the jar is missing, accessing `LunaSettings` directly can cause a `NoClassDefFoundError`.

**Proposed Improvement: Safe Loader Pattern**
1.  Wrap LunaLib calls in a separate method that is **only** called if `Global.getSettings().getModManager().isModEnabled("lunalib")` returns true.
2.  Implement a helper method that encapsulates the LunaLib call, the fallback to legacy settings, and the default value.

```java
// Example Implementation Pattern
private void loadSettings() {
    // 1. Load Defaults
    // 2. Load Legacy JSON
    // 3. Override with LunaLib (Guarded)
    if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
        loadLunaLibSettings();
    }
}

private void loadLunaLibSettings() {
    // Use helper methods to handle exceptions gracefully
    fuelPerDay = safeGetFloat(KEY_FUEL_PER_DAY, fuelPerDay);
}
```

## 2. Maintainability: Magic Strings & CSV Consistency

**Current Risk:**
Using string literals like `"nebula_fuel_per_day"` in multiple places increases the risk of drift.

**Proposed Improvement: Centralized Constants**
1.  Define all setting keys as `private static final String` constants in `ModPlugin`.
2.  Use these constants in both the JSON loader and the LunaLib loader.

**Validation:**
Add a check in `build.ps1` to grep for these keys in `ModPlugin.java` and ensure they exist in `data/config/LunaSettings.csv`.

## 3. Performance & Safety: Runtime Logic

**Current Risk:**
The `advance()` method runs every frame. Expensive checks or math errors (division by zero) can cause issues.

**Proposed Improvement:**
1.  **Interval First:** Ensure `interval.intervalElapsed()` is the very first check in `advance()`.
2.  **Early Exit:** Return immediately if the mod is disabled via settings.
3.  **Math Safety:** Guard against division by zero (e.g., if `maxFuel` is 0).

```java
public void advance(float amount) {
    // 1. Performance: Interval check first
    interval.advance(amount);
    if (!interval.intervalElapsed()) return;

    // 2. Reliability: Null checks & Math Safety
    CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
    if (fleet == null) return;
    
    float maxFuel = fleet.getCargo().getMaxFuel();
    if (maxFuel <= 0) return; // Prevent division by zero

    // ... calculation logic ...
}
```

## 4. Configuration Safety: CSV Escaping

**Current Risk:**
`MissingFormatArgumentException` caused by unescaped `%` signs in tooltips crashes the game UI.

**Proposed Improvement:**
Audit `data/config/LunaSettings.csv` and ensure **every** description or tooltip that uses a percent sign escapes it as `%%`.

## 5. Observability: Runtime Feedback & Versioning

**Current Risk:**
Users cannot easily verify if the mod is working (adding fuel) without staring at the UI. Version mismatches cause update issues.

**Proposed Improvement:**
1.  **Debug Mode:** Add a `ramscoop_debug_mode` setting. If enabled, print "Ramscoop: +X fuel" to the campaign message log.
2.  **CI Checks:** Ensure CI runs `.github/scripts/check-versions.ps1`.

## 6. Build Robustness

**Current Risk:**
`build.ps1` might pick up multiple LunaLib jars or run with the wrong JDK.

**Proposed Improvement:**
1.  **Jar Selection:** Sort found LunaLib jars and pick the latest one to avoid classpath duplicates.
2.  **JDK Validation:** Explicitly check for JDK 8 compatibility or warn if running on a newer JDK without `--release 8`.
