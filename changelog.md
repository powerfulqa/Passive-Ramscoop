# Changelog

## [0.4.1] - 2025-08-14
### Fixed
- LunaLib settings are now reliably applied on game load and at runtime
- Removed reflection-based reads (blocked in scripts); use direct LunaLib API
- Ensured plugin is the single source of truth; runtime script reads from plugin only
- Absolute guard prevents supply generation when disabled

### Technical
- Build script compiles against LunaLib JAR (auto-detected path)
- Minimal, useful logging; removed noisy per-frame and file-based debug output
- Cleaned stray compiled classes and leftover files from repo

### Notes
- `settings.json` serves as a baseline only when LunaLib isn’t ready; LunaLib overrides once available

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
