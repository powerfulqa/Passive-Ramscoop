# Changelog 

## [0.7.0] - 2025-10-13
### Added
- Per-event notification toggles (nebula entry/exit, corona entry/exit) exposed via LunaLib so players can independently enable or disable floating-text notifications for each event.
### Changed
- Reworked Notification UI: grouped Nebula and Corona notification settings and removed legacy color preset radios in favor of LunaLib HSV color pickers.
- Default notification colors changed to light gray (#D3D3D3).

## [0.6.3] - 2025-10-09
### Fixed
- Fixed crash when opening LunaLib settings menu (percent signs in descriptions are now properly escaped as %%)
- Eliminated excessive debug logging that was bloating log files (174+ entries per session reduced to 0)

### Changed
- Improved all LunaSettings descriptions with detailed explanations and practical examples for better user experience
- Code cleanup: Added descriptive constants for all default values (improved maintainability)
- All debug logging now controlled by DEBUG_MODE flag (set to false for production builds)

### Technical
- Added DEBUG_MODE flag to both ModPlugin and Ramscoop classes
- Conditionalized 7 debug logging statements
- No functionality changes to fuel/supply generation mechanics
- 100% backward compatible with existing saves and configurations

## [0.6.2] - 2025-10-09
### Fixed
- Fixed key mismatch: nebula_percent_supply_limit -> ramscoop_percent_supply_limit
- Fixed key mismatch: nebula_hard_supply_limit -> ramscoop_hard_supply_limit
- Added null-handling for supply limit settings to prevent NullPointerException
- Added ramscoop_supply_per_crew key (code expected this but CSV only had nebula_supply_per_crew)
- Added corona_caps_reuse boolean setting for corona fuel cap reuse control
- Kept nebula_supply_per_crew as legacy key for backward compatibility
- Completes fix for 'Value not found in JSONObject' errors
- Updated version to 0.6.2 across mod_info.json, version.json, and CHANGELOG.md"

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
