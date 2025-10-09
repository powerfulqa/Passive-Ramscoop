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
- Current version: 0.6.3

## Development
This mod includes build scripts for development:
- `build.ps1` – PowerShell build script
- `build.bat` – Windows batch build script

See [changelog.md](changelog.md) for version history and [MIGRATION_REPORT.md](MIGRATION_REPORT.md) for technical migration details.

Build note: the PowerShell script compiles against the Starsector API and LunaLib. If your LunaLib folder name differs from `03_LunaLib`, update the script or symlink accordingly.LunaLib-2.0.4`, update the LunaLib path in `build.ps1` accordingly.

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

See [DEVELOPMENT.md](DEVELOPMENT.md) for detailed development information.

## Version History
See [changelog.md](changelog.md) for complete version history and [MIGRATION_REPORT.md](MIGRATION_REPORT.md) for technical migration details.

## Licence
Creative Commons Attribution-NonCommercial 4.0 International Licence (CC BY-NC 4.0) with additional restrictions.

See [LICENSE.txt](LICENSE.txt) for full details.
