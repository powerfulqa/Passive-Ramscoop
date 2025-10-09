
# Development Guide for Passive-Ramscoop1. **Set Up Environment:**
   - JDK 8-21 (build script ## Resource Generation Logic

### A## Resource Generation Logic

### Architecture Overview
- **`ModPlugin.java`** - Mod entry point and settings orchestration
  - `onApplicationLoad()`: Called once on game start
  - `onGameLoad()`: Called when loading a save - loads settings here
  - Settings priority: LunaLib (if available) → settings.json → hardcoded defaults
  - Exposes public static fields that `Ramscoop.java` reads at runtime

- **`Ramscoop.java`** - EveryFrameScript implementation for runtime logic
  - `advance(amount)`: Called every frame with delta time
  - Reads settings from public static fields in ModPlugin
  - Uses `IntervalUtil(0.09f, 0.11f)` to throttle expensive operations (~10 times/second)
  - Adds fuel/supplies via `MutableFleetStatsAPI.getDynamic().getMod("nebula_stat_mod")`

### Settings Loading Chain
```
LunaSettings.csv (UI config defined in data/config/LunaSettings.csv)
    ↓
ModPlugin.loadLunaLibSettings() (runtime loading via reflection)
    ↓ (fallback if LunaLib not ready)
ModPlugin.loadLegacySettings() (data/config/settings.json)
    ↓
public static fields (Ramscoop reads these)
```

**CRITICAL**: CSV `fieldID` column must EXACTLY match `LunaSettings.get*("fieldID")` calls (case-sensitive).

### Terrain Detection
- **Nebula**: Checks for "nebula_stat_mod" in fleet stats
- **Corona**: 
  - Primary: Checks terrain plugin for `CoronaBurstTerrainPlugin` instance
  - Fallback: Measures distance to nearest star using `Misc.getNearestStarOrBlackHole()`

### Performance Considerations
- Uses `IntervalUtil` to avoid per-frame overhead (~0.1 second intervals)
- Early exit if interval hasn't elapsed
- Conditional logging controlled by `DEBUG_MODE` flags (set to `false` for production)

## Common Development Pitfalls

### 1. LunaLib CSV Percent Sign Escaping
**Problem**: Game crashes with `MissingFormatArgumentException` when opening LunaLib settings menu

**Root Cause**: LunaLib uses `String.format()` to render CSV `tooltip` and `description` fields. Any `%` character is interpreted as a format specifier.

**Solution**: Escape all literal `%` characters as `%%` in `LunaSettings.csv`:
- ❌ Wrong: `"20% of max fuel"`
- ✅ Correct: `"20%% of max fuel"`

**Validation**: Always test by opening LunaLib settings menu in-game after modifying CSV descriptions.

### 2. CSV Key Mismatches
**Problem**: LunaLib throws "Value X not found in JSONObject"

**Solution**: 
- Check CSV `fieldID` column matches `LunaSettings.get*("X")` calls exactly (case-sensitive)
- LunaLib only saves settings after user interaction - always provide defaults in CSV

### 3. TriOS Version Not Updating
**Problem**: TriOS shows old version number after release

**Solution**:
- Increment `patch` field in `Ramscoop.version`, not just `mod_info.json`
- Update `directDownloadURL` to point to new release version
- URL paths are case-sensitive (use `changelog.txt`, not `CHANGELOG.md`)
- Wait 1-5 minutes for GitHub CDN to update raw files

### 4. Build Failures
**Common causes**:
- LunaLib jar doesn't exist in `mods/03_LunaLib-*/jars/`
- JAVA_HOME points to JRE instead of JDK
- Incorrect Starsector installation path in build script

### 5. Runtime NPEs
**Solution**:
- Always null-check `LunaSettingsAPI.isReady()` before reading settings
- All Ramscoop fields read from ModPlugin must have defaults
- Use try-catch blocks for optional LunaLib settings

### 6. Debug Logging in Production
**Problem**: Excessive debug logging bloats log files (100+ entries per session)

**Solution**: 
- Use `DEBUG_MODE` flags (set to `false` for releases)
- Conditionalize all debug logging: `if (DEBUG_MODE) { LOG.info(...); }`

## Contributinge Overview
- **`ModPlugin.java`** - Mod entry point and settings orchestration
  - `onApplicationLoad()`: Called once on game start
  - `onGameLoad()`: Called when loading a save - loads settings here
  - Settings priority: LunaLib (if available) → settings.json → hardcoded defaults
  - Exposes public static fields that `Ramscoop.java` reads at runtime

- **`Ramscoop.java`** - EveryFrameScript implementation for runtime logic
  - `advance(amount)`: Called every frame with delta time
  - Reads settings from public static fields in ModPlugin
  - Uses `IntervalUtil(0.09f, 0.11f)` to throttle expensive operations (~10 times/second)
  - Adds fuel/supplies via `MutableFleetStatsAPI.getDynamic().getMod("nebula_stat_mod")`

### Settings Loading Chain
```
LunaSettings.csv (UI config defined in data/config/LunaSettings.csv)
    ↓
ModPlugin.loadLunaLibSettings() (runtime loading via reflection)
    ↓ (fallback if LunaLib not ready)
ModPlugin.loadLegacySettings() (data/config/settings.json)
    ↓
public static fields (Ramscoop reads these)
```

**CRITICAL**: CSV `fieldID` column must EXACTLY match `LunaSettings.get*("fieldID")` calls (case-sensitive).

### Terrain Detection
- **Nebula**: Checks for "nebula_stat_mod" in fleet stats
- **Corona**: 
  - Primary: Checks terrain plugin for `CoronaBurstTerrainPlugin` instance
  - Fallback: Measures distance to nearest star using `Misc.getNearestStarOrBlackHole()`

### Performance Considerations
- Uses `IntervalUtil` to avoid per-frame overhead (~0.1 second intervals)
- Early exit if interval hasn't elapsed
- Conditional logging controlled by `DEBUG_MODE` flags (set to `false` for production)release 8` for Java 8 compatibility)
   - Starsector 0.98a-RC8 installation
   - Starsector API JAR files (11 JARs in your Starsector installation directory)
   - LunaLib mod installed in `mods/03_LunaLib-*/jars/` (required for compilation)
   - An IDE of your choice (IntelliJ IDEA, Eclipse, VS Code, etc.)

2. **Build Process:**
   - Edit Java source files in the `src/ramscoop/` directory
   - Run `build.ps1` (PowerShell - recommended) or `build.bat` (batch) to compile
   - The build script:
     - Auto-discovers LunaLib jars from `mods/03_LunaLib-*/jars/`
     - Compiles with `--release 8` for Java 8 compatibility
     - Includes 11 Starsector core JARs + LunaLib in classpath
     - Packages to `jars/Ramscoop.jar` with correct `ramscoop/` package structure
   - Launch Starsector to test your changesis document provides information for developers wishing to work on the Passive-Ramscoop mod.

## Project Structure

```
Passive-Ramscoop/
├── .github/            # GitHub Actions workflows and instructions
│   ├── copilot-instructions.md
│   └── prompts/
│       └── StarSector.prompt.md
├── data/               # Mod data files
│   └── config/
│       ├── LunaSettings.csv    # LunaLib settings UI configuration
│       └── settings.json       # Fallback/legacy settings
├── docs/               # Documentation files
│   ├── DEVELOPMENT.md          # Developer documentation (this file)
│   ├── MIGRATION_REPORT.md     # Technical migration details
│   ├── NEXUS_DESCRIPTION.md    # Nexus Mods description
│   └── RELEASE_v0.6.3_SUMMARY.md
├── jars/               # Compiled mod JAR files
│   └── Ramscoop.jar
├── src/                # Source code
│   └── ramscoop/
│       ├── ModPlugin.java      # Mod entry point, settings orchestration
│       └── Ramscoop.java       # Runtime fuel/supply generation logic
├── build.bat           # Windows batch build script
├── build.ps1           # PowerShell build script (recommended)
├── CHANGELOG.md        # Version history (Markdown for GitHub)
├── changelog.txt       # Version history (plain text for TriOS)
├── LICENSE.txt         # Licence information
├── LunaLib.version     # LunaLib version file
├── mod_info.json       # Mod metadata
├── Ramscoop.version    # Version file for TriOS mod manager
├── README.md           # User documentation
└── version.json        # Additional version metadata
```

## Development Workflow

1. **Set Up Environment:**
   - JDK 8 or later (compatible with Starsector’s Java version)
   - Starsector API JAR files (found in your Starsector installation)
   - An IDE of your choice (IntelliJ IDEA, Eclipse, VS Code, etc.)

2. **Build Process:**
   - Edit Java source files in the `src` directory
   - Run `build.bat` (Windows) or `build.ps1` (PowerShell) to compile
   - The built JAR will be placed in the `jars` directory
   - Launch Starsector to test your changes

### Building dev helper locally
**Note:** The `dev/` folder and TestModPlugin are no longer part of the current project structure. This section is kept for historical reference only.

If you want to compile the development-only helper (`dev/src/ramscoop/TestModPlugin.java`) locally for debugging, ensure the Starsector API JAR is on your classpath and run the following from PowerShell:

```powershell
$API = "C:\Program Files (x86)\Fractal Softworks\Starsector\starfarer.api.jar"
javac -classpath "$API" -d out dev\src\ramscoop\TestModPlugin.java
jar cf jars\TestModHelper.jar -C out .
```

Notes:
- The `dev/` folder is intentionally ignored and not included in release builds.
- The helper requires the Starsector API JAR to compile; adjust `$API` if your installation path differs.

3. **Source Control:**
   - The repository contains both source code and compiled assets
   - Build scripts and artefacts should be excluded from git where appropriate

4. **Creating Releases:**
   - Update version numbers in 3 places:
     - `mod_info.json` (version field)
     - `Ramscoop.version` (modVersion.patch field + directDownloadURL)
     - Both changelogs: `CHANGELOG.md` and `changelog.txt`
   - Commit changes: `git add . && git commit -m "Release vX.Y.Z: Description"`
   - Create and push a version tag: `git tag vX.Y.Z && git push && git push --tags`
   - GitHub Actions will automatically:
     - Build the mod with Java 8 compatibility
     - Create a GitHub release with the tag
     - Attach `Ramscoop-vX.Y.Z.zip` to the release
     - TriOS mod manager will auto-update from `directDownloadURL`

## Resource Generation Logic

- `ModPlugin` initialises the mod and registers the `Ramscoop` instance
- `Ramscoop` implements the `EveryFrameScript` interface to run every frame
- When the player fleet is in a nebula, resources are gradually accumulated:
  - Nebula detection is done by checking for "nebula_stat_mod" in fleet stats
- Settings in `settings.json` control generation rates and limits
- The mod uses the game’s settings loading mechanism with the mod ID: `Global.getSettings().loadJSON("settings.json", "m561_ramscoop")`

## Contributing

If you wish to contribute to this project:

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

Please ensure you test your changes thoroughly before submitting.

## Licence

This project is licenced under the Creative Commons Attribution-NonCommercial 4.0 International Licence (CC BY-NC 4.0) – see the [LICENSE.txt](LICENSE.txt) file for details.

This licence allows:
- Free use and modification of the mod for personal use
- Sharing the mod with others for non-commercial purposes
- Creating and sharing derivative works (provided they are also non-commercial)

This licence prohibits:
- Using the mod for commercial purposes
- Selling the mod or any derivative works based on it

Any modifications or distributions must give appropriate credit to the original authors.
