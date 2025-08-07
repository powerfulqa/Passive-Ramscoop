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

You can customize the mod's behavior by editing the `data/config/settings.json` file:

- `enable_fuel`: Set to `true` to enable fuel generation, `false` to disable
- `enable_supplies`: Set to `true` to enable supplies generation, `false` to disable
- `fuel_per_day`: Percentage of max fuel to generate per day (e.g., 0.1 = 10%)
- `percent_supply_limit`: Maximum percentage of cargo space that can be filled with generated supplies
- `hard_supply_limit`: Hard limit on the amount of supplies that can be generated (0 = no limit)
- `crew_usage`: Method for calculating supply generation based on crew:
  - `extra`: Use only excess crew over the fleet minimum
  - `all`: Use all crew for supply generation
  - `nocrew`: Don't use crew for calculation
- `supply_per_crew`: Amount of supplies each crew member generates per day
- `no_crew_gen`: For 'nocrew' mode, choose between 'percent' or 'flat' generation rate
- `no_crew_rate`: Value for the percent or flat rate generation

## Compatibility

- Starsector 0.98a-RC8

## Changes in Version 0.3

- Updated to be compatible with Starsector 0.98a-RC8
- Fixed several bugs including nebula detection issues
- Improved stability and error handling
- Fixed settings loading issues

## Credits

Original mod by Meridias561 (for Starsector 0.95a)
Updated for Starsector 0.98a-RC8 by PowerfulQA

## License

This mod is open source and released under the MIT License. See the LICENSE file for details.
