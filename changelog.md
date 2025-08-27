# Changelog

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
- **Configuration Interface**: 9 configurable settings organized in logical sections
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
- **Performance**: Optimized for minimal frame impact

## [0.2.0] - 2024-12-17
### Added
- **Basic Resource Generation**: Simple fuel and supply generation
- **Mod Structure**: Proper Starsector mod organization

## [0.1.0] - 2024-12-16
### Added
- **Initial Release**: Basic mod framework and structure
