# Ramscoop Mod for Starsector

## Overview
Ramscoop is a utility mod for Starsector that automatically gathers resources from nebulas and star coronas, generating fuel and supplies for your fleet over time. As of v0.6.0 ("Corona Time") it adds separate Nebula/Corona tabs with independent fuel caps, improved corona detection, and a simplified UI (no global caps).

## Features
- Automatically generates fuel and supplies while in nebulas
- Global "Scoop Enabled" toggle (runtime) that affects both fuel and supplies
- Fuel controls: rate (percent/day), soft cap (0–100%), hard cap (absolute units), and margin (units) to avoid overfill
- Supply controls: percent-of-cargo soft cap, optional hard cap, crew-usage modes (extra/all/nocrew)
- Organized LunaLib settings with tabs: General, Nebula, Corona (LunaLib required)
- Safe clamping to prevent going 0.1 over the cap
- LunaLib Version Checker support for automatic updates

## Installation
1. Extract the mod files to your Starsector `mods` directory. The folder should be named `Passive-Ramscoop`.
2. Install and enable [LunaLib](https://fractalsoftworks.com/forum/index.php?topic=25658) (required).
3. Enable Ramscoop in the Starsector launcher.

## Configuration

### LunaLib Settings (v0.6.1)
Configure all mod settings through the in-game settings menu:
- Press **F2** during campaign mode to open the settings menu
- Or access "Mod Settings" when creating a new game
- Tabs: **General**, **Nebula**, **Corona** under "Ramscoop Configuration"

Key settings:
- General: "Scoop Enabled" (master toggle), enable fuel, enable supplies
- Nebula: Fuel Rate (%/day) and Nebula Fuel Caps (soft/hard/margin), plus Supplies (limit, hard limit, crew usage/modes)
- Corona: Fuel Rate (%/day) and Corona Fuel Caps (soft/hard/margin). No supplies in corona.

Defaults (v0.6.1):
- Fuel rate 4%; fuel soft cap 20%; hard cap 0; margin 0
- Supply limit 20%; hard limit 0; crew usage "extra"; per-crew 0.1

### Manual Configuration (settings.json)
Primarily for development. Note: this mod uses a non-standard JSON format where some string values are unquoted (e.g., `crew_usage: extra`). LunaLib values are applied first if present; missing keys fall back to `settings.json`.

#### Settings Reference:
- `scoop_toggle_default_on`: true/false master toggle default on load
- `enable_fuel`: Enable/disable fuel generation
- `enable_supplies`: Enable/disable supplies generation
- `fuel_per_day`: Fuel generated per day as a fraction of max fuel (e.g., `.04` = 4%)
- Nebula fuel: `nebula_fuel_per_day`, `nebula_percent_fuel_limit`, `nebula_hard_fuel_limit`, `nebula_fuel_cap_margin`
- Corona fuel: `corona_fuel_per_day`, `corona_percent_fuel_limit`, `corona_hard_fuel_limit`, `corona_fuel_cap_margin`
- `percent_supply_limit`: Fraction of cargo capacity to fill with supplies (`.20` = 20%)
- `hard_supply_limit`: Hard limit on supply generation (`0` = no limit)
- `crew_usage`: How crew affects supply generation (`extra`, `all`, or `nocrew` – unquoted)
- `supply_per_crew`: Supplies generated per crew per day (e.g., `.1`)
- `no_crew_gen`: For `nocrew`, choose `percent` (fraction of cargo/day) or `flat`
- `no_crew_rate`: Rate used with `no_crew_gen`

Notes:
- Fuel soft cap and rate sliders in the UI are 0–100%; the code converts them to fractions internally.
- The mod clamps fuel additions to the minimum of the soft cap and hard cap, then subtracts the margin; this prevents the historical 0.1 over-cap issue.
- Corona detection is robust (terrain plugin checks with a star-distance fallback). If corona fuel seems inactive, check starsector.log for a line starting with "[Ramscoop] Corona mode:".

## Compatibility
- Starsector 0.98a-RC8
- LunaLib 2.0.4+ (required)
- LunaLib Version Checker supported
- No known mod conflicts
- Current version: 0.6.1

## Development
This mod includes build scripts for development:
- `build.ps1` – PowerShell build script
- `build.bat` – Windows batch build script

See [changelog.md](changelog.md) for version history and [MIGRATION_REPORT.md](MIGRATION_REPORT.md) for technical migration details.

Build note: the PowerShell script compiles against the Starsector API and LunaLib. If your LunaLib folder name differs from `03_LunaLib-2.0.4`, update the LunaLib path in `build.ps1` accordingly.

## Credits
- Original mod by Meridias561 ([Nexus Mods profile](https://next.nexusmods.com/profile/Meridias561))
- Updated for Starsector 0.98a-RC8 by powerfulqa
- Robust LunaLib settings application finalized in v0.4.1
- Additional Play Testing and UX consideration @Nerhtal

## Licence
Creative Commons Attribution-NonCommercial 4.0 International Licence (CC BY-NC 4.0) with additional restrictions.

See [LICENSE.txt](LICENSE.txt) for full details.
