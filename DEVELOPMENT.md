# Development Guide for Passive-Ramscoop Mod

This document provides information for developers who want to work on the Passive-Ramscoop mod.

## Project Structure

```
Passive-Ramscoop/
├── jars/               # Compiled mod JAR files
│   └── Ramscoop.jar
├── src/                # Source code
│   └── ramscoop/
│       ├── ModPlugin.java
│       ├── Ramscoop.java
│       └── RamscoopSaveProxy.java
├── build.bat           # Windows build script
├── build.xml           # Ant build script (cross-platform)
├── find_java.ps1       # PowerShell script to locate Java installations
├── mod_info.json       # Mod metadata
├── data/               # Mod data files
│   └── config/         
│       └── settings.json # Mod configuration
├── README.md           # User documentation
└── DEVELOPMENT.md      # Developer documentation (this file)
```

## Development Workflow

1. **Setup Environment**:
   - JDK 8 or later (compatible with Starsector's Java version)
   - Starsector API JAR files (typically in your Starsector installation)
   - An IDE of your choice (IntelliJ IDEA, Eclipse, VS Code, etc.)

2. **Build Process**:
   - Edit the Java source files in the `src` directory
   - Run `build.bat` to compile (Windows) or use Ant with `build.xml` (cross-platform)
   - For release packaging, run `build.bat release` or `ant package` 
   - The built JAR is placed in the `jars` directory
   - Launch Starsector to test your changes
   - If you need help finding Java, use the `find_java.ps1` script

3. **Source Control**:
   - The repository contains both source code and compiled assets
   - Build scripts are excluded from git but should be kept locally
   - Build artifacts (in `build/` directory) are excluded from git

4. **Creating Releases**:
   - Update version numbers in `mod_info.json` and `Ramscoop.version`
   - Update the `changelog.md` with your changes
   - Commit changes: `git add . && git commit -m "Prepare release vX.Y"`
   - Create and push a version tag: `git tag vX.Y && git push origin vX.Y`
   - GitHub Actions will automatically:
     - Create a release package with only user-required files
     - Attach it to a new GitHub release
     - Publish with notes from your changelog

## Resource Generation Logic

The mod works as follows:

1. `ModPlugin` initializes the mod and registers the `Ramscoop` instance
2. `Ramscoop` implements the `EveryFrameScript` interface to run every frame
3. When the player fleet is in a nebula, resources are gradually accumulated:
   - Nebula detection is done by checking for "nebula_stat_mod" in fleet stats
   - This is more reliable than using direct location checks
4. Settings in `settings.json` (in the data/config directory) control generation rates and limits
5. The mod uses the game's settings loading mechanism with the mod ID: `Global.getSettings().loadJSON("settings.json", "m561_ramscoop")`

## Serialization Design

This mod is marked as a utility mod in `mod_info.json` using `"utility": true`, which means it can be safely added or removed from existing saves at any time. However, we still implement proper serialization to ensure maximum compatibility:

1. `Ramscoop` implements `Serializable` with a proper `serialVersionUID`
2. Transient fields are marked with the `transient` keyword to prevent serialization issues
3. The `writeReplace()` method returns an empty ArrayList when saving, ensuring no mod-specific classes are stored
4. `RamscoopSaveProxy` provides compatibility for saved games when the mod is uninstalled
5. `ModPlugin` handles proper cleanup in `beforeGameSave()` and restoration in `onGameSave()`

While these serialization safeguards aren't strictly necessary for utility mods, they provide an extra layer of safety and ensure the mod behaves predictably when enabled or disabled.

## Contributing

If you want to contribute to this project:

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

Please make sure to test your changes thoroughly before submitting.

## License

This project is licensed under the Creative Commons Attribution-NonCommercial 4.0 International License (CC BY-NC 4.0) - see the [LICENSE](LICENSE) file for details.

This license allows:
- Freely using and modifying the mod for personal use
- Sharing the mod with others for non-commercial purposes
- Creating and sharing derivative works (as long as they're also non-commercial)

This license prohibits:
- Using the mod for commercial purposes
- Selling the mod or any derivative works based on it

Any modifications or distributions must give appropriate credit to the original authors.
