# Ramscoop Mod v0.3 for Starsector 0.98a-RC8

This mod adds an automatic process to your fleet to scoop up 'stuff' from nebulas to create fuel and supplies.

## Features

- Automatically generate fuel and supplies while in nebulas
- Configurable generation rates
- Options to use crew for supply generation
- Customizable limits to prevent excessive resource accumulation

## Installation

1. Copy the `Ramscoop-0.3` folder to your Starsector mods directory
2. Start Starsector and enable the mod in the launcher

## Configuration

You can customize the mod's behavior by editing the `settings.json` file in the root directory of the mod:

- `enable_fuel`: Set to `true` to enable fuel generation, `false` to disable
- `enable_supplies`: Set to `true` to enable supplies generation, `false` to disable
- `fuel_per_day`: Percentage of max fuel to generate per day (e.g., 0.1 = 10%)
- `percent_supply_limit`: Maximum percentage of cargo space that can be filled with generated supplies
- `hard_supply_limit`: Hard limit on the amount of supplies that can be generated (0 = no limit)
- `crew_usage`: Method for calculating supply generation based on crew:
  - `extra`: Use only excess crew over the fleet minimum
  - `all`: Use all crew for supply generation
  - `nocrew`: Don't use crew for calculation
- `supplies_per_crew`: Amount of supplies each crew member generates per day
- `no_crew_gen`: For 'nocrew' mode, choose between 'percent' or 'flat' generation rate
- `no_crew_rate`: Value for the percent or flat rate generation

## Compatibility

- Starsector 0.98a-RC8

## Safe Mod Removal

This mod is designed to be safely removable without breaking saves:

1. **Recommended Method**: Disable the mod in the launcher and load your save. Play for a few minutes and save the game. This allows the mod to cleanly remove itself.

2. **Direct Removal**: You can also directly remove the mod files. The mod is designed to handle its absence gracefully, although you may see a one-time warning message when loading a save that previously used the mod.

## Changes in Version 0.3

- Updated to be compatible with Starsector 0.98a-RC8
- Fixed nebula detection using fleet stat modifiers for more reliable detection
- Improved stability and error handling
- Fixed settings loading issues with proper mod ID reference
- Simplified mod structure with settings file in root directory
- Added detailed documentation for developers
- Added simple_build.bat as a lightweight build alternative

## Credits

Original mod by Meridias561 (for Starsector 0.95a)
Updated for Starsector 0.98a-RC8 by PowerfulQA

To contact the original author for permissions:
- Meridias561's Nexus Mods profile: https://next.nexusmods.com/profile/Meridias561

## License

This mod is released under the Creative Commons Attribution-NonCommercial 4.0 International License (CC BY-NC 4.0) with additional restrictions from the original author. These include:

1. You must give appropriate credit to the original creators
2. You may not use it for commercial purposes (no selling of this mod or derivatives)
3. **You must obtain explicit permission from the original author (Meridias561) before modifying any files**
4. **You must obtain explicit permission before using any assets from this mod in other projects**
5. You must credit the original author when uploading or sharing this mod

Contact information for permission requests can be found in the Credits section above.
See the LICENSE file for complete details.
