# Ramscoop Mod for Starsector

## Overview
Ramscoop is a small utility mod that adds an automatic process to your fleet to gather resources from nebulas, generating fuel and supplies over time.

## Features
- Automatically generate fuel and supplies while in nebulas
- Configurable generation rates and limits
- Multiple crew usage options for supply generation

## Installation
1. Extract the `Passive-Ramscoop` folder to your Starsector `mods` directory
2. Enable the mod in the Starsector launcher

## Configuration
All settings can be adjusted in the `settings.json` file:

- `enable_fuel`: Set to true/false to enable/disable fuel generation
- `enable_supplies`: Set to true/false to enable/disable supplies generation
- `fuel_per_day`: Percentage of max fuel to generate per day (decimal format, 0.1 = 10%)
- `percent_supply_limit`: Maximum percentage of cargo capacity to fill with supplies
- `hard_supply_limit`: Hard limit on supply generation (0 = no limit)
- `crew_usage`: How crew affects supply generation ("extra", "all", or "nocrew")
- `supply_per_crew`: Amount of supplies each crew generates per day
- `no_crew_gen`: For "nocrew" option, generation method ("percent" or "flat")
- `no_crew_rate`: Rate value for the selected "no_crew_gen" option

## Compatibility
- Starsector 0.98a-RC8
- No known mod conflicts

## Credits
- Original mod by Meridias561
- Updated version by powerfulqa

## License
Free for non-commercial use
