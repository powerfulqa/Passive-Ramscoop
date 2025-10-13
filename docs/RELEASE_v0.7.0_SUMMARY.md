````markdown
# Release v0.7.0 Summary

## Release Date
2025-10-13

## Changes Overview

### Added
- Per-event notification toggles for Nebula Entry, Nebula Exit, Corona Entry, and Corona Exit exposed via LunaLib.

### Changed
- Reordered Notification UI in `data/config/LunaSettings.csv` to group Nebula and Corona settings together.
- Removed legacy color preset radio controls; LunaLib color pickers (hex + HSV sliders) are now the single source of color configuration.
- Default notification colors set to light gray (#D3D3D3).

### Removed
- Removed delayed "blue" secondary floating-text effect and its scheduling code (cosmetic only).

## Files Modified
- `data/config/LunaSettings.csv` — reorganised notification section, added per-event toggles, default color updated
- `src/ramscoop/ModPlugin.java` — added LunaLib reads for new toggles, default colors updated, debug logging updated
- `src/ramscoop/Ramscoop.java` — notifications gated by runtime scoop toggle, removed blue-text scheduling
- `Ramscoop.version`, `version.json`, `mod_info.json` — version bumped to 0.7.0
- `CHANGELOG.md`, `changelog.txt` — added v0.7.0 entries

## Testing
- Rebuilt JAR with the project's build script and verified compilation
- Confirmed in-game via user testing that per-event toggles appear and function, notifications respect the master Scoop toggle, and the delayed blue text is removed

## Notes
- This release is a small feature addition and UI rework; it is backwards-compatible with existing saves and settings. LunaLib-stored color values will continue to override new defaults where present.

---

**Build Status**: ✅ Successful
**Testing Status**: ✅ User-verified
**Ready for Release**: ✅ Yes

````
