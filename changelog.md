# Changelog 

## [0.6.1] - 2025-08-28
### Fixed
- Minor documentation updates and version bump to 0.6.1
- Small code clean up
- Updated all documentation to UK English
- Improved user-friendly documentation and added Nexus Mods description

## [0.6.0] - 2025-08-27 (Corona Time)
### Added
- Corona fuel generation with independent caps (soft/hard/margin) and per-tab rate.
- Nebula tab now includes supplies controls; Corona tab is fuel-only.
- Robust corona detection with terrain-plugin checks and star-distance fallback.

### Changed
- Removed Global Fuel Caps and the use-global toggles; each tab controls its own caps.
- Simplified UI into tabs: General, Nebula, Corona.

### Fixed
- Cases where corona detection failed are now covered by fallback; added a one-line diagnostic when corona fuel runs.
- Corona fuel caps were not applied in some cases due to caps not being loaded; loading fixed and caps now respected.

## [0.5.0] - 2025-08-27
### Added
- LunaLib tabs: General, Fuel, Supplies.
- Fuel controls: soft cap (0–100%), hard cap, margin; rate now a percent slider.
- Global "Scoop Enabled" toggle (runtime) affecting fuel and supplies.

### Changed
- Defaults: Fuel rate 4%, fuel soft cap 20%, hard cap 0, margin 0. Supplies limit default 0.20.
- Clamp fuel addition to cap minus margin to prevent 0.1 overfill.

### Fixed
- Supplies now respect the global Scoop toggle.
- LunaLib settings seed from legacy settings.json when missing, then apply immediately.

## [0.4.1] - 2024-12-19
### Fixed
- **CRITICAL FIX**: LunaLib settings are now properly applied when the game loads
- **Settings Loading Issue**: Fixed timing problem where LunaLib settings were accessed before LunaLib was fully initialized
- **Settings Refresh**: Improved periodic settings reload mechanism to properly detect when LunaLib becomes available
- **Debug Logging**: Added comprehensive settings status logging to help troubleshoot configuration issues
- **LunaLib Ready Check**: Added robust verification that LunaLib is fully initialized before attempting to load settings

### Technical Improvements
- Added `isLunaLibReady()` method to verify LunaLib initialization state
- Implemented proper settings loading state tracking (`settingsLoaded`, `lunaLibReady`)
- Enhanced `reloadSettings()` method with better LunaLib availability detection
- Added `triggerSettingsReload()` static method for external access
- Added `logSettingsStatus()` method for comprehensive debugging
- Improved error handling and fallback mechanisms

### User Experience
- LunaLib settings now apply immediately when the game loads
- No more need to restart the game to see LunaLib configuration changes
- Better feedback about when and how settings are loaded
- Maintains backward compatibility with settings.json fallback

## [0.4.0] - 2024-12-19
### Added
- **LunaLib Integration**: Full in-game configuration support via LunaLib
- **LunaLib Version Checker**: Automatic update notifications support
- **Soft Dependency**: Works with or without LunaLib installed
- **Enhanced Settings Management**: Smart detection and loading system

### Changed
- **Settings Loading**: LunaLib settings now take priority over settings.json
- **Configuration Interface**: 9 configurable settings organised in logical sections
- **Real-time Configuration**: Changes apply immediately without restart

### Fixed
- **CSV Format Issue**: Removed problematic `%` characters from LunaSettings.csv
- **Crash Prevention**: Eliminated "Fatal: Format specifier '%'" error

## [0.3.0] - 2024-12-18
### Added
- **Nebula Detection**: Automatic resource generation while traveling through nebulas
- **Fuel Generation**: Configurable fuel generation rate
- **Supply Generation**: Configurable supply generation with crew usage options
- **Settings Configuration**: JSON-based configuration file

### Changed
- **Resource Generation**: Now only occurs in nebula regions
- **Performance**: Optimised for minimal frame impact

## [0.2c] - 2025-08-11
### Added
- Final release by original author
- For StarSector version 0.95a

## [0.2b] - 2021-04-06
### Added
- Updates and improvements by original author
- For StarSector version 0.95a

## [0.2a] - 2021-04-04
### Added
- Minor updates and fixes by original author
- For StarSector version 0.95a

## [0.2] - 2021-04-03
### Added
- Second release by original author
- For StarSector version 0.95a

## [0.1] - 2021-03-31
### Added
- **Initial Release**: Original mod by the original author
- Basic passive ramscoop functionality
- For StarSector version 0.95a
