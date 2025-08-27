# Ramscoop Migration Report

## Overview
This report documents the migration and enhancement of the Ramscoop mod from Starsector 0.95a to Starsector 0.98a-RC8, the addition of LunaLib support in version 0.4.0, and configuration/UI refinements in 0.5.0.

## Version 0.4.0 - LunaLib Integration

### New Features Added
- **LunaLib Settings Support:** Added comprehensive in-game settings configuration through LunaLib
- **LunaLib Version Checker Support:** Added LunaLib.version file for automatic update notifications
- **Soft Dependency Implementation:** LunaLib is optional - mod functions normally without it
- **Enhanced Settings Management:** Improved settings loading with automatic fallback

### Files Added/Modified

#### New Files:
- `data/config/LunaSettings.csv` - LunaLib configuration file defining all mod settings
- `LunaLib.version` - Version checker file for LunaLib update notifications

#### Modified Files:
- `src/ramscoop/ModPlugin.java` - Added LunaLib integration with reflection-based loading
- `mod_info.json` - Updated version to 0.4.0, enhanced description
- `Ramscoop.version` - Version Checker integration file
- `README.md` - Added LunaLib documentation and usage instructions
- `changelog.md` - Documented new features

### Technical Implementation Details

#### LunaLib Integration Approach
- Uses Java reflection to avoid hard dependency on LunaLib JAR
- Implements graceful fallback to settings.json if LunaLib is not available
- Maintains full backward compatibility with existing configurations

#### Settings Mapping
The following settings were mapped from settings.json to LunaLib configuration:

| settings.json | LunaSettings.csv | Type | Description |
|---------------|------------------|------|-------------|
| enable_fuel | ramscoop_enable_fuel | Boolean | Enable fuel generation |
| enable_supplies | ramscoop_enable_supplies | Boolean | Enable supply generation |
| fuel_per_day | ramscoop_fuel_per_day | Double | Fuel generation rate |
| percent_supply_limit | ramscoop_percent_supply_limit | Double | Supply limit percentage |
| hard_supply_limit | ramscoop_hard_supply_limit | Double | Hard supply limit |
| crew_usage | ramscoop_crew_usage | Radio | Crew usage mode |
| supply_per_crew | ramscoop_supply_per_crew | Double | Supply per crew |
| no_crew_gen | ramscoop_no_crew_gen | Radio | No-crew generation type |
| no_crew_rate | ramscoop_no_crew_rate | Double | No-crew generation rate |

## Version 0.5.0 - Configuration Refinements

### Key Changes
- Fuel UI: rate and soft cap are 0–100% sliders; converted to fractions in code
- Fuel clamping: soft cap (fraction of max), hard cap (absolute), margin (units)
- Supplies respect the master "Scoop Enabled" toggle
- Immediate application: toggling in LunaLib updates fleet memory at runtime
- Defaults aligned for gameplay convenience (fuel 4%/day, soft cap 20%; supplies 20%)

### Files Modified
- `data/config/LunaSettings.csv` – added tabs and new fields/types; adjusted defaults
- `settings.json` – aligned fallback defaults (decimal fractions)
- `src/ramscoop/ModPlugin.java` – convert % sliders to fractions; seed from legacy before LunaLib; sync toggle to memory
- `src/ramscoop/Ramscoop.java` – clamp fuel adds; guard supplies by master toggle

#### Code Architecture Changes
- **ModPlugin.java Changes:**
  - Added `MOD_ID` constant for consistent mod identification
  - Implemented `loadSettings()` method with smart detection of available settings sources
  - Added `loadLunaLibSettings()` using reflection for dynamic LunaLib access
  - Enhanced `loadLegacySettings()` with better error handling
  - Added `fuel_per_day` variable to match LunaLib configuration

- **Ramscoop.java Changes:**
  - Updated fuel generation calculation to use `ModPlugin.fuel_per_day` instead of `supplies_per_crew`

## Version 0.3.0 - API Migration (Previous)

### API Changes and Adaptations

#### Settings Loading
- **Old API:** `Global.getSettings().loadJSON("settings.json")`
- **New API:** `Global.getSettings().loadJSON("settings.json", "m561_ramscoop")`
- **Notes:** The newer API requires specifying the mod ID as the second parameter to correctly locate mod-specific files.

#### Null Checks
- Added null checks in `isNebula()` method to prevent potential NullPointerExceptions.

#### Exception Handling
- Added simple error logging using `System.out.println()` and `printStackTrace()` to avoid dependency on log4j.

#### Code Structure
- Modernized the switch statement structure for better readability.
- Added comments to explain key components of the code.

## Build Process

### Required Libraries
- starfarer.api.jar
- starfarer_obf.jar
- janino.jar
- commons-compiler.jar
- commons-compiler-jdk.jar
- fs.common_obf.jar
- fs.sound_obf.jar
- lwjgl.jar
- lwjgl_util.jar
- json.jar

### Build Instructions
Two build scripts are provided for flexibility:

#### Using PowerShell (Recommended)
1. Ensure Starsector is installed at the path specified in `build.ps1` or update the path.
2. Run `.\build.ps1` from PowerShell.
3. The compiled JAR will be placed in `Passive-Ramscoop/jars/`.

#### Using Batch File (Alternative)
1. Ensure Starsector is installed at the path specified in `build.bat` or update the path.
2. Run `build.bat` from the command line.
3. The compiled JAR will be placed in `Passive-Ramscoop/jars/`.

## Testing Checklist (v0.4.0)

### With LunaLib Installed:
- [ ] Launch game with both Ramscoop and LunaLib enabled
- [ ] Access settings via F2 during campaign
- [ ] Verify all Ramscoop settings appear in LunaLib menu
- [ ] Test settings changes are applied immediately
- [ ] Enter nebula and verify resource generation works with LunaLib settings
- [ ] Check console output for "Loaded settings from LunaLib" message

### Without LunaLib:
- [ ] Launch game with only Ramscoop enabled
- [ ] Verify settings.json is loaded correctly
- [ ] Enter nebula and verify resource generation works with settings.json
- [ ] Check console output for "Loaded settings from settings.json (LunaLib not available)" message

### General Testing:
- [ ] Start a new game
- [ ] Enter a nebula with a fleet
- [ ] Verify fuel generation works
- [ ] Verify supplies generation works
- [ ] Verify no errors in starsector.log
- [ ] Test with different crew usage modes
- [ ] Verify hard limits are respected

## Known Issues
- None identified at this time.

## Future Considerations
- Consider adding visual feedback when resources are being generated
- Potential integration with other settings management mods
- Enhanced error reporting for invalid LunaLib configurations
