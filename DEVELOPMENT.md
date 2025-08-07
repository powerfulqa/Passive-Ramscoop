# Development Guide for Passive-Ramscoop Mod

This document provides information for developers who want to work on the Passive-Ramscoop mod.

## Project Structure

```
Passive-Ramscoop/
├── data/               # Game data files
│   └── config/
│       └── settings.json
├── jars/               # Compiled mod JAR files
│   └── Ramscoop.jar
├── src/                # Source code
│   └── ramscoop/
│       ├── ModPlugin.java
│       └── Ramscoop.java
├── build.bat           # Windows build script (not tracked in git)
├── build.xml           # Ant build script 
├── mod_info.json       # Mod metadata
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
   - Run `build.bat` or use Ant with `build.xml` to compile
   - The built JAR is placed in the `jars` directory
   - Launch Starsector to test your changes

3. **Source Control**:
   - The repository contains both source code and compiled assets
   - Build scripts are excluded from git but should be kept locally
   - Build artifacts (in `build/` directory) are excluded from git

## Resource Generation Logic

The mod works as follows:

1. `ModPlugin` initializes the mod and registers the `Ramscoop` instance
2. `Ramscoop` implements the `EveryFrameScript` interface to run every frame
3. When the player fleet is in a nebula, resources are gradually accumulated
4. Settings in `data/config/settings.json` control generation rates and limits

## Contributing

If you want to contribute to this project:

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

Please make sure to test your changes thoroughly before submitting.
