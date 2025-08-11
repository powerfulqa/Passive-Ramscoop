# Ramscoop Migration Report

## Overview
This report documents the migration of the Ramscoop mod from Starsector 0.95a to Starsector 0.98a-RC8.

## API Changes and Adaptations

### Settings Loading
- **Old API:** `Global.getSettings().loadJSON("settings.json")`
- **New API:** `Global.getSettings().loadJSON("settings.json", "m561_ramscoop")`
- **Notes:** The newer API requires specifying the mod ID as the second parameter to correctly locate mod-specific files.

### Null Checks
- Added null checks in `isNebula()` method to prevent potential NullPointerExceptions.

### Exception Handling
- Added simple error logging using `System.out.println()` and `printStackTrace()` to avoid dependency on log4j.

### Code Structure
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

## Testing Checklist
- [ ] Launch game with the updated mod
- [ ] Start a new game
- [ ] Enter a nebula with a fleet
- [ ] Verify fuel generation works
- [ ] Verify supplies generation works
- [ ] Check settings.json is properly read
- [ ] Verify no errors in starsector.log

## Known Issues
- None identified at this time.
