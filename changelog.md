# Ramscoop Changelog

## Version 0.4.0 (2025-08-11)
- **NEW:** Added LunaLib Settings support for in-game configuration
- **NEW:** Added LunaLib Version Checker support for automatic update notifications
- **NEW:** All mod settings can now be adjusted through the LunaLib settings menu (F2 in campaign or mod settings during new game creation)
- **NEW:** Improved settings management with automatic fallback to settings.json if LunaLib is not available
- **IMPROVED:** Added fuel_per_day setting for more precise fuel generation control
- **IMPROVED:** Better error handling and logging for settings loading
- **ENHANCED:** Maintains full backward compatibility with existing settings.json configurations
- **FIXED:** Resolved CSV format issues that caused crashes when accessing mod settings
- LunaLib is an optional dependency - mod works with or without it
- Settings menu provides user-friendly interface with descriptions and validation
- Version checker integration allows for automatic update notifications through LunaLib
- No gameplay changes - all existing functionality preserved

## Version 0.3.0 (2025-08-11)
- Updated for Starsector 0.98a-RC8 compatibility
- Fixed settings.json format to use valid JSON with comments
- Added simple error logging to help diagnose issues (no log4j dependency)
- Added null checks to prevent NullPointerExceptions
- Updated the loadJSON call to include modId parameter as required by newer API
- Restructured code for better readability and maintainability
- Added both batch (.bat) and PowerShell (.ps1) build scripts for easier compilation

## Version 0.2c
- Original version by Meridias561 for Starsector 0.95a
