# Source Code Structure

This directory contains the source code for the Passive-Ramscoop mod. The code is organized as follows:

## Package Structure

- `ramscoop/` - Main package containing all mod classes
  - `ModPlugin.java` - Entry point for the mod, handles initialization and configuration
  - `Ramscoop.java` - Core implementation of the resource generation system

## Development Workflow

1. Make changes to the source files in this directory
2. Compile using build.bat or build.xml in the root directory
3. The compiled JAR will be placed in the jars/ directory
4. Launch Starsector to test your changes

## Implementation Details

- The mod uses Starsector's EveryFrameScript system to run code each frame
- Resource generation happens when the player fleet is in a nebula
- Settings are loaded from data/config/settings.json

## Resources for Starsector Modding

- Official Modding Forum: https://fractalsoftworks.com/forum/index.php?board=1.0
- Modding Documentation: https://starsector.fandom.com/wiki/Modding
- Javadocs: https://fractalsoftworks.com/starfarer.api/
