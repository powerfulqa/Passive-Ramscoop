
# Development Guide for Passive-Ramscoop Mod

This document provides information for developers wishing to work on the Passive-Ramscoop mod.

## Project Structure

```
Passive-Ramscoop/
├── jars/               # Compiled mod JAR files
│   └── Ramscoop.jar
├── src/                # Source code
│   └── ramscoop/
│       ├── ModPlugin.java
│       ├── Ramscoop.java
├── build.bat           # Windows build script
├── build.ps1           # PowerShell build script
├── mod_info.json       # Mod metadata
├── settings.json       # Mod configuration
├── README.md           # User documentation
├── DEVELOPMENT.md      # Developer documentation (this file)
├── CHANGELOG.md        # Version history
├── MIGRATION_REPORT.md # Technical migration details
├── LICENSE.txt         # Licence information
├── LunaLib.version     # Optional LunaLib version file
├── version.json        # Mod version file
├── changelog.txt       # Changelog for releases
└── SUMMARY.md          # Project summary
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

3. **Source Control:**
   - The repository contains both source code and compiled assets
   - Build scripts and artefacts should be excluded from git where appropriate

4. **Creating Releases:**
   - Update version numbers in `mod_info.json` and `version.json`
   - Update the `CHANGELOG.md` with your changes (the `changelog.txt` will be automatically generated)
   - Commit changes: `git add . && git commit -m "Prepare release vX.Y"`
   - Create and push a version tag: `git tag vX.Y && git push origin vX.Y`
   - GitHub Actions will automatically:
     - Generate `changelog.txt` from `CHANGELOG.md`
     - Create a release package with only user-required files
     - Attach it to a new GitHub release
     - Publish with notes from your changelog

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

This project is licensed under the Creative Commons Attribution-NonCommercial 4.0 International Licence (CC BY-NC 4.0) – see the [LICENSE.txt](LICENSE.txt) file for details.

This licence allows:
- Free use and modification of the mod for personal use
- Sharing the mod with others for non-commercial purposes
- Creating and sharing derivative works (provided they are also non-commercial)

This licence prohibits:
- Using the mod for commercial purposes
- Selling the mod or any derivative works based on it

Any modifications or distributions must give appropriate credit to the original authors.
