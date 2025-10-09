# Copilot Instructions for Passive Ramscoop

## Project Overview
Starsector 0.98a-RC8 mod that passively generates fuel/supplies from nebulae and star coronas. Built with Java 8, LunaLib 2.0.4 integration, TriOS mod manager support.

## Architecture

### Two-Class Design
1. **ModPlugin.java** - Mod entry point, settings orchestration
   - `onApplicationLoad()`: Called once on game start
   - `onGameLoad()`: Called when loading a save - loads settings here
   - Settings priority: LunaLib (if available) → settings.json → hardcoded defaults
   
2. **Ramscoop.java** - EveryFrameScript implementation for runtime logic
   - `advance(amount)`: Called every frame with delta time
   - Reads settings from public static fields in ModPlugin
   - Uses `IntervalUtil(0.09f, 0.11f)` to throttle expensive operations
   - Adds fuel/supplies via `MutableFleetStatsAPI.getDynamic().getMod("nebula_stat_mod")`

### Settings Architecture
```
LunaSettings.csv (UI config) 
    ↓
ModPlugin.loadLunaLibSettings() (runtime loading via reflection)
    ↓ (fallback if LunaLib not ready)
ModPlugin.loadLegacySettings() (settings.json)
    ↓
public static fields (Ramscoop reads these)
```

**CRITICAL**: CSV `fieldID` column must EXACTLY match `LunaSettings.get*("fieldID")` calls (case-sensitive).

## Key Workflows

### Build Process
```powershell
.\build.ps1  # PowerShell script handles everything
```
- Discovers LunaLib jars dynamically from `mods/03_LunaLib-*/jars/`
- Compiles with `--release 8` for Java 8 compatibility
- Packages to `jars/Ramscoop.jar` with correct `ramscoop/` package structure
- Classpath includes 11 Starsector core JARs + LunaLib

### Release Workflow
1. Update version in 3 places:
   - `mod_info.json` (version field)
   - `Ramscoop.version` (modVersion.patch field)
   - Both changelogs (CHANGELOG.md + changelog.txt)
2. Commit changes
3. Tag: `git tag v0.6.X` (semantic versioning with `v` prefix)
4. Push: `git push && git push --tags`
5. GitHub Actions auto-builds and creates release

### TriOS Compatibility Requirements
- **Ramscoop.version**: Semantic versioning with `major.minor.patch` fields
- **changelog.txt**: Plain text with "Version X.X.X" headers (not markdown ##)
- **URLs**: `directDownloadURL` and `changelogURL` must match actual file paths
- **CHANGELOG.md**: Markdown format for GitHub (maintained separately)

## Code Patterns

### LunaLib Integration (with fallback)
```java
// In ModPlugin.java
private void loadLunaLibSettings() {
    try {
        myField = LunaSettings.getDouble("modId", "fieldID");
    } catch (Exception e) {
        Global.getLogger(ModPlugin.class).warn("LunaLib setting 'fieldID' failed: " + e.getMessage());
        // Falls back to loadLegacySettings() if this fails
    }
}
```

### Performance-Conscious Runtime (Ramscoop.java)
```java
// Use IntervalUtil to avoid per-frame overhead
private transient IntervalUtil interval = new IntervalUtil(0.09f, 0.11f);

public void advance(float amount) {
    interval.advance(amount);
    if (!interval.intervalElapsed()) return;  // Early exit most frames
    
    // Expensive logic only runs every ~0.1 seconds
}
```

### Terrain Detection
```java
// Check for corona via terrain plugin first (accurate)
if (terrain.getPlugin() instanceof CoronaBurstTerrainPlugin) {
    // Apply corona bonuses
}

// Fallback to star distance check (less reliable)
SectorEntityToken nearestStar = Misc.getNearestStarOrBlackHole(fleet);
float distance = Misc.getDistance(fleet, nearestStar);
```

## Common Pitfalls

1. **CSV Key Mismatches**: If LunaLib throws "Value X not found in JSONObject"
   - Check CSV `fieldID` column matches `LunaSettings.get*("X")` exactly
   - LunaLib only saves settings after user interaction - provide defaults in CSV

2. **TriOS Version Not Updating**: 
   - Increment `patch` field in `Ramscoop.version`, not just `mod_info.json`
   - URL paths are case-sensitive (use `changelog.txt`, not `CHANGELOG.md`)

3. **Build Failures**:
   - Ensure LunaLib jar exists in `mods/03_LunaLib-*/jars/`
   - Verify JAVA_HOME points to JDK 8-21 (not just JRE)

4. **Runtime NPEs**:
   - Always null-check `LunaSettingsAPI.isReady()` before reading settings
   - Ramscoop fields read from ModPlugin must have defaults

5. **LunaLib CSV Percent Sign Escaping**: If you get `MissingFormatArgumentException` when opening LunaLib settings
   - **Root Cause**: LunaLib uses `String.format()` to render CSV `tooltip` fields - any `%` is interpreted as a format specifier
   - **Symptoms**: Game crashes when opening settings menu with stack trace showing `java.util.UnknownFormatConversionException` or `MissingFormatArgumentException`
   - **Fix**: Escape all literal `%` characters as `%%` in CSV `tooltip` and `description` columns
   - **Example**: Change `"20% of max fuel"` → `"20%% of max fuel"` in LunaSettings.csv
   - **Validation**: After CSV changes, always test by opening LunaLib settings menu in-game before committing
   - Common affected settings: nebula_fuel_per_day, nebula_percent_fuel_limit, ramscoop_percent_supply_limit, corona_fuel_per_day

## File Structure
```
mod_info.json           # Mod metadata, dependencies
Ramscoop.version        # Version checker for TriOS (JSON with comments)
CHANGELOG.md            # Markdown changelog for GitHub
changelog.txt           # Plain text changelog for TriOS
data/config/
  LunaSettings.csv      # Settings UI configuration
  settings.json         # Fallback settings (legacy)
src/ramscoop/
  ModPlugin.java        # Entry point, settings loading
  Ramscoop.java         # Runtime fuel/supply generation
build.ps1               # Build automation
```

## References
- See `.github/prompts/StarSector.prompt.md` for detailed development guidelines
- LunaLib documentation: Reflection-based API, JSON storage in saves/common/LunaSettings/
- Starsector API: 0.98a-RC8, Java 8 syntax max

## Quick Start for AI Agents
1. Read `ModPlugin.java` - understand settings loading chain
2. Read `Ramscoop.java` - see how settings are consumed at runtime
3. Check `LunaSettings.csv` - verify fieldID keys match code
4. For builds: `.\build.ps1` (PowerShell)
5. For releases: Update 3 version locations, tag with `v*`, push
