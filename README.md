# Ramscoop Mod for Starsector

## Overview
Ramscoop is a small utility mod that adds an automatic process to your fleet to gather resources from nebulas, generating fuel and supplies over time.

## Features
- Automatically generate fuel and supplies while in nebulas
- Configurable generation rates and limits
- Multiple crew usage options for supply generation

## Installation
1. Extract the mod files to your Starsector `mods` directory (the folder should be named `Passive-Ramscoop`)
2. Enable the mod in the Starsector launcher

## Configuration
All settings can be adjusted in the `settings.json` file. Note that this mod uses a non-standard JSON format where some string values are unquoted:

- `enable_fuel`: Set to true/false to enable/disable fuel generation
- `enable_supplies`: Set to true/false to enable/disable supplies generation
- `fuel_per_day`: Percentage of max fuel to generate per day (decimal format, .1 = 10%)
- `percent_supply_limit`: Maximum percentage of cargo capacity to fill with supplies (.35 = 35%)
- `hard_supply_limit`: Hard limit on supply generation (0 = no limit)
- `crew_usage`: How crew affects supply generation (extra, all, or nocrew - note: unquoted values)
- `supply_per_crew`: Amount of supplies each crew generates per day (.1 = 0.1 supplies per crew)
- `no_crew_gen`: For "nocrew" option, generation method (percent or flat - note: unquoted values)
- `no_crew_rate`: Rate value for the selected "no_crew_gen" option (.1 = 10% or 0.1 flat rate)

## Compatibility
- Starsector 0.98a-RC8
- No known mod conflicts
- Current version: 0.3.0

## Development
This mod includes build scripts for development:
- `build.ps1` - PowerShell build script
- `build.bat` - Windows batch build script

See [CHANGELOG.md](CHANGELOG.md) for version history and [MIGRATION_REPORT.md](MIGRATION_REPORT.md) for technical migration details.

## Credits
- Original mod by Meridias561 (https://next.nexusmods.com/profile/Meridias561)
- Updated version by powerfulqa

## License
Creative Commons Attribution-NonCommercial 4.0 International License (CC BY-NC 4.0) with additional restrictions.

See [LICENSE.txt](LICENSE.txt) for full details.
