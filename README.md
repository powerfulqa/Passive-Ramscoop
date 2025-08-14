
# Ramscoop Mod for Starsector

## Overview
Ramscoop is a utility mod for Starsector that automatically gathers resources from nebulas, generating fuel and supplies for your fleet over time.

## Features
- Automatically generates fuel and supplies while in nebulas
- Configurable generation rates and limits
- Multiple crew usage options for supply generation
- LunaLib settings integration for in-game configuration (required)
- LunaLib Version Checker support for automatic updates

## Installation
1. Extract the mod files to your Starsector `mods` directory. The folder should be named `Passive-Ramscoop`.
2. Install and enable [LunaLib](https://fractalsoftworks.com/forum/index.php?topic=25658) (required).
3. Enable Ramscoop in the Starsector launcher.

## Configuration

### LunaLib Settings
Configure all mod settings through the in-game settings menu:
- Press **F2** during campaign mode to open the settings menu
- Or access "Mod Settings" when creating a new game
- Look for the "Ramscoop Configuration" section

The settings interface provides descriptions for each option and validates input ranges automatically.

### Manual Configuration (settings.json)
Primarily for development. Note: this mod uses a non-standard JSON format where some string values are unquoted (e.g., `crew_usage: extra`).

#### Settings Reference:
- `enable_fuel`: Set to true/false to enable or disable fuel generation
- `enable_supplies`: Set to true/false to enable or disable supplies generation
- `fuel_per_day`: Percentage of maximum fuel to generate per day (decimal format, `.1` = 10%)
- `percent_supply_limit`: Maximum percentage of cargo capacity to fill with supplies (`.35` = 35%)
- `hard_supply_limit`: Hard limit on supply generation (`0` = no limit)
- `crew_usage`: How crew affects supply generation (`extra`, `all`, or `nocrew` – unquoted values)
- `supply_per_crew`: Amount of supplies each crew generates per day (`.1` = 0.1 supplies per crew)
- `no_crew_gen`: For `nocrew` option, generation method (`percent` or `flat` – unquoted values)
- `no_crew_rate`: Rate value for the selected `no_crew_gen` option (`.1` = 10% or 0.1 flat rate)

## Compatibility
- Starsector 0.98a-RC8
- LunaLib 2.0.4+ (required)
- LunaLib Version Checker supported
- No known mod conflicts
- Current version: 0.4.1

## Development
This mod includes build scripts for development:
- `build.ps1` – PowerShell build script
- `build.bat` – Windows batch build script

See [CHANGELOG.md](CHANGELOG.md) for version history and [MIGRATION_REPORT.md](MIGRATION_REPORT.md) for technical migration details.

Build note: the PowerShell script compiles against the Starsector API and LunaLib. If your LunaLib folder name differs from `03_LunaLib-2.0.4`, update the LunaLib path in `build.ps1` accordingly.

## Credits
- Original mod by Meridias561 ([Nexus Mods profile](https://next.nexusmods.com/profile/Meridias561))
- Updated for Starsector 0.98a-RC8 by powerfulqa
- Robust LunaLib settings application finalized in v0.4.1

## Licence
Creative Commons Attribution-NonCommercial 4.0 International Licence (CC BY-NC 4.0) with additional restrictions.

See [LICENSE.txt](LICENSE.txt) for full details.
