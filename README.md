# Passive Ramscoop - Automated Resource Collection for Starsector

## Downloads
[![GitHub total downloads](https://img.shields.io/github/downloads/powerfulqa/Passive-Ramscoop/total?label=GitHub%20total%20downloads)](https://github.com/powerfulqa/Passive-Ramscoop/releases)
[![Latest release downloads](https://img.shields.io/github/downloads/powerfulqa/Passive-Ramscoop/latest/total?label=Latest%20release%20downloads)](https://github.com/powerfulqa/Passive-Ramscoop/releases/latest)
[![Nexus Mods](https://img.shields.io/badge/Nexus%20Mods-Passive%20Ramscoop-d98f40)](https://www.nexusmods.com/starsector/mods/147)

**Get the mod:**
- **GitHub:** [Latest Release](https://github.com/powerfulqa/Passive-Ramscoop/releases) - Direct downloads tracked in badges above
- **NexusMods:** [Passive Ramscoop](https://www.nexusmods.com/starsector/mods/147)

## What This Mod Does
Ever wished your fleet could collect fuel and supplies whilst travelling through nebulae and near stars? The Passive Ramscoop mod makes this dream a reality! Your ships will automatically gather resources as you explore, reducing the need for constant supply runs.

It is balanced as a safety net: if you are very low on fuel, a nebula or star corona will always get you some. By default it only fills the fuel tank to 20%, but everything is configurable in the in-game settings menu to suit your preferred difficulty.

**Perfect for players who want:**
- Less micromanagement of fuel and supplies
- More immersive exploration gameplay  
- Realistic "ramscoop" technology like in science fiction
- Configurable resource generation that fits your playstyle

## Key Features
✨ **Automatic Resource Collection**
- Fuel generation whilst travelling through nebulae and near star coronas
- Supply generation in nebulae (with configurable crew usage options), optionally in coronas too
- No manual activation required - just fly through space!
- A message log entry when you leave, e.g. "Ramscoop collected +12 fuel and +30 supplies"

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
4. **Start playing!** - Settings can be accessed in-game by pressing F3 (LunaLib default; Shift+F2 always works) or via Mod Settings on the main menu

## How to Use

### Quick Start - Default Settings
The mod works with the default settings. Once installed, you will collect:
- **Nebula fuel**: 4% of your maximum fuel capacity per day
- **Corona fuel**: 25% of your maximum fuel capacity per day (a corona overlapping a nebula uses the corona rate)
- **Supplies**: in nebulae, 0.1 supplies per day for each crew member above your fleet's minimum crew
- **Limits**: fuel stops at 20% of your tank capacity, supplies at 20% of your cargo space

### Customising Your Experience
Press **F3** (or **Shift+F2**) in-game and open **Ramscoop**:

**🎮 General** - master on/off switch, plus fuel and supply switches

**🌌 Nebula** - fuel rate and caps, supply caps, and how crew affect supply generation

**☀️ Corona** - fuel rate and caps (or reuse the Nebula caps), and opt-in corona supplies

**💬 Notifications** - floating text on entry/exit with colours, and the message log report

### Troubleshooting
**Not collecting resources?**
- Make sure you're in a nebula (coloured space clouds) or a star corona
- Check you are below the caps: by default fuel stops at 20% of your tanks
- Check Scoop Enabled in the settings menu (F3 or Shift+F2)
- Verify both this mod and LunaLib are enabled in the launcher

## Compatibility
- Starsector 0.98a-RC8
- LunaLib 2.0.4+ (required)
- Version Checker and TriOS supported
- No known mod conflicts

## Credits & History
🎖️ **Original Creator**: Meridias561 - Created the original Passive Ramscoop mod  
🛠️ **Current Maintainer**: PowerfulQA - Updated for modern Starsector, added LunaLib integration  
🧪 **Testing & UX**: @Nerhtal - Playtesting and user experience improvements  

**With permission from the original author**, this mod has been updated and modernised for current Starsector versions with many quality-of-life improvements including the in-game configuration system.

---

## Development
Build with `./build.sh` (Linux/macOS) or `build.ps1` (Windows). See [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md).

## Version History
See [changelog.txt](changelog.txt).

## Licence
Creative Commons Attribution-NonCommercial 4.0 International Licence (CC BY-NC 4.0) with additional restrictions.

See [LICENSE.txt](LICENSE.txt) for full details.
