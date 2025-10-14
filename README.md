# Passive Ramscoop - Automated Resource Collection for Starsector

## What This Mod Does
Ever wished your fleet could collect fuel and supplies whilst travelling through nebulae and near stars? The Passive Ramscoop mod makes this dream a reality! Your ships will automatically gather resources as you explore, reducing the need for constant supply runs.

**Perfect for players who want:**
- Less micromanagement of fuel and supplies
- More immersive exploration gameplay  
- Realistic "ramscoop" technology like in science fiction
- Configurable resource generation that fits your playstyle

## Overview
This mod automatically generates fuel and supplies for your fleet when travelling through nebulae and star coronas. The idea around balancing it was that its used to make sure you can always get some fuel if you are very low by entering a nebula or star corona. This only fills the fuel tank currently to 20% by default, but everything is fully configurable through an easy-to-use in-game settings menu, so you can adjust the rates to match your preferred difficulty level.

## Key Features
✨ **Automatic Resource Collection**
- Fuel generation whilst travelling through nebulae and near star coronas
- Supply generation in nebulae (with configurable crew usage options)
- No manual activation required - just fly through space!

🎛️ **Fully Configurable**
- Easy in-game settings menu (no file editing required)
- Separate controls for nebula and corona environments
- Adjustable generation rates, caps, and limits
- Master on/off toggle for quick disable

🛡️ **Smart & Safe**
- Prevents overfilling your fuel tanks
- Respects your cargo capacity limits
- Balanced default settings that won't break gameplay
- Works seamlessly with other mods

## Installation
1. **Download and install [LunaLib](https://fractalsoftworks.com/forum/index.php?topic=25658)** (required dependency)
2. **Download this mod** and extract to your Starsector `mods` directory 
3. **Enable both mods** in the Starsector launcher
4. **Start playing!** - Settings can be accessed in-game by pressing F2

> **Note:** LunaLib is required for the in-game settings menu. Without it, the mod will use default settings from a configuration file.

## How to Use

### Quick Start - Default Settings
The mod works great with default settings! Once installed, you'll automatically collect:
- **Fuel**: 4% of your maximum fuel capacity per day whilst in nebulae or coronas
- **Supplies**: Based on your crew size whilst in nebulae (extra crew members generate 0.1 supplies/day each)
- **Limits**: Fuel stops at 20% of your tank capacity, supplies at 20% of cargo space

### Customising Your Experience
Press **F2** in-game to open the settings menu and find "Ramscoop Configuration":

**🎮 General Tab**
- Master on/off switch for the entire mod
- Enable/disable fuel and supply generation separately

**🌌 Nebula Tab** 
- Adjust fuel generation rate (0-100% per day)
- Set fuel collection limits to prevent overfilling
- Configure supply generation based on crew or cargo space
- Choose how crew affects supply generation

**☀️ Corona Tab**
- Separate fuel generation settings for star coronas
- Independent fuel caps and rates
- (No supply generation in coronas - too dangerous!)

### Troubleshooting
**Not collecting resources?** 
- Make sure you're in a nebula (purple/coloured space clouds) or near a star corona
- Check that the mod is enabled in your F2 settings menu
- Verify both this mod and LunaLib are enabled in the launcher

**Want different rates?**
- All generation rates can be adjusted from 0% to 100% per day
- Set any rate to 0% to disable that type of resource collection
- Fuel and supply generation can be controlled independently

**Advanced Configuration**
For modders and advanced users, manual configuration is available through `settings.json`. See the technical documentation section below for details.

## Compatibility
- Starsector 0.98a-RC8
- LunaLib 2.0.4+ (required)
- LunaLib Version Checker supported
- No known mod conflicts
- Current version: 0.7.2

## What's new (0.7.0)
- Per-event notification toggles: you can now enable/disable floating notifications separately for nebula entry/exit and corona entry/exit via the LunaLib settings UI.
- UI rework: color presets removed in favor of HSV color pickers; saved HSV values are now respected correctly.
- Default notification colours changed to light gray (#D3D3D3) to reduce visual noise; Java default falls back to Color.LIGHT_GRAY.
- Notifications are now gated by the master "Scoop" toggle at runtime — turning the master toggle off stops all ramscoop notifications immediately.

## What's new (0.7.2)

- CSV validator & CI integration: added `.github/scripts/validate-luna-csv.py` and a CI step to block malformed `LunaSettings.csv` and to prevent reintroduction of legacy LunaLib UI keys.
- UI cleanup: removed legacy `nebula_*` UI entries from the LunaLib settings menu to keep the UI clear and consistent.
- No-Crew controls split: replaced the combined no-crew rate control with two dedicated LunaLib fields:
	- `nebula_no_crew_rate_percent` — percent slider (0–100), converted to a fraction in code.
	- `nebula_no_crew_rate_flat` — flat supply units per day.
- Updated code: `ModPlugin` and runtime logic updated to read the new keys and handle percent→fraction conversion.

## TriOS compatibility (quick notes)
This project supports packaging for TriOS. See `docs/TRIOS_COMPATIBILITY.md` for a short checklist and validation steps you can run before creating a TriOS release.

## Development
This mod includes build scripts for development:
- `build.ps1` – PowerShell build script
- `build.bat` – Windows batch build script

New: CI and release checks
- A PowerShell helper lives at `.github/scripts/check-versions.ps1`. It validates that `mod_info.json`, `version.json`, and `Ramscoop.version` all match.
- CI (PRs/pushes) runs the checker and will fail if versions differ. This prevents mismatched releases from being merged.
- Release workflow automatically extracts the release tag, runs the checker in fix mode and will commit updated version files back to `main` so packaging uses the correct version.

Build note: the PowerShell script compiles against the Starsector API and LunaLib. The `build.ps1` script dynamically discovers LunaLib jars under your Starsector `mods` folder (if present) and will include them in the classpath. Ensure `JAVA_HOME` points to a JDK installation (the script will also attempt common install paths).

## Credits & History
🎖️ **Original Creator**: Meridias561 - Created the original Passive Ramscoop mod  
🛠️ **Current Maintainer**: PowerfulQA - Updated for modern Starsector, added LunaLib integration  
🧪 **Testing & UX**: @Nerhtal - Playtesting and user experience improvements  

**With permission from the original author**, this mod has been updated and modernised for current Starsector versions with many quality-of-life improvements including the in-game configuration system.

---

## Technical Documentation

### Manual Configuration (settings.json)
For advanced users and modders. LunaLib settings take priority when available.

#### Key Settings:
- `scoop_toggle_default_on`: Master toggle default state
- `enable_fuel`/`enable_supplies`: Enable/disable resource types
- `fuel_per_day`: Daily fuel generation rate (fraction of max capacity)
- Nebula/Corona fuel: Independent rate and cap settings
- `percent_supply_limit`: Supply generation limit (fraction of cargo space)
- `crew_usage`: Crew usage mode (`extra`, `all`, or `nocrew`)

### Development
Build scripts available for developers:
- `build.ps1` – PowerShell build script  
- `build.bat` – Windows batch build script

See [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md) for detailed development information.

## Version History
See [CHANGELOG.md](CHANGELOG.md) for complete version history and [docs/MIGRATION_REPORT.md](docs/MIGRATION_REPORT.md) for technical migration details.

## Licence
Creative Commons Attribution-NonCommercial 4.0 International Licence (CC BY-NC 4.0) with additional restrictions.

See [LICENSE.txt](LICENSE.txt) for full details.
