# Development Guide for Passive-Ramscoop

This document describes how to build and contribute to Passive Ramscoop, plus developer notes about the runtime architecture and common pitfalls.

## Requirements
- JDK 11 or later installed (JDK 17 recommended). NOTE: the build scripts compile with `--release 8` so the produced bytecode is compatible with Java 8 (required by Starsector). Using JDK 11+ is recommended for development tooling, but the build intentionally targets Java 8 compatibility.
- PowerShell to run `build.ps1` (Windows) or PowerShell Core on other platforms
- Starsector 0.98a-RC8 installation for local compilation against the game jars (optional if you use the committed `jars/Ramscoop.jar` in CI)
- LunaLib installed in your local Starsector `mods/` folder if you want to compile against LunaLib API locally

## Quick start
1. Clone the repository and open it in your IDE of choice.
2. Ensure `JAVA_HOME` points to a JDK installation.
3. Build the mod:

```powershell
pwsh .\build.ps1
```

The output JAR is placed in `jars/Ramscoop.jar`.

## Project layout (short)
- `src/ramscoop/` — Java sources (`ModPlugin.java`, `Ramscoop.java`) 
- `data/config/LunaSettings.csv` — LunaLib UI configuration
- `settings.json` — legacy fallback config
- `Ramscoop.version`, `mod_info.json`, `version.json` — release/version metadata
- `.github/scripts/check-versions.ps1` — version consistency checker used by CI

## Architecture overview
- `ModPlugin.java` — mod entry point and settings orchestration
  - Loads settings with priority: LunaLib (if available) → `settings.json` → hardcoded defaults
  - Exposes public static fields consumed by runtime logic
- `Ramscoop.java` — runtime `EveryFrameScript` implementation
  - `advance(float amount)` called frequently; expensive work is throttled with an `IntervalUtil`
  - Applies fuel / supplies to the player fleet via dynamic stats modifications

## Key developer notes & pitfalls
- LunaLib CSV percent signs: LunaLib renders CSV tooltips with `String.format()`; escape literal percent signs as `%%` in `LunaSettings.csv`.
- CSV keys must exactly match the code's `LunaSettings.get*(MOD_ID, "fieldID")` calls (case-sensitive).
- Use `DEBUG_MODE` guarded logging; leave `DEBUG_MODE = false` for release builds.
- The code attempts to read new LunaLib Color values first (the `_v2` keys). Legacy string hex values are no longer exposed in the CSV UI to avoid menu clutter.

## Build / compile notes
- The `build.ps1` script attempts to discover a Starsector install and LunaLib jars; if not found it will still compile but you may miss LunaLib API access locally. CI relies on a committed `jars/Ramscoop.jar`, so the CI job does not require a local Starsector installation.
- If you need to compile against a Starsector core locally, set `SS_DIR` at the top of `build.ps1` or install Starsector into a standard path.

### Build Artifact Policy
- **Never commit built artifacts** (JARs, compiled `.class` files) to version control during feature development.
- Keep `jars/` and `build/classes/` entries out of commits.
- The build script produces the jar locally for testing, and the release workflow will package it for distribution.
- *Exception:* If the CI workflow specifically requires a pre-built JAR in the repo (as noted above), ensure it is only updated during release preparation.

### Mandatory Restart
- **Restart the game after building:** After running `build.ps1` or `build.bat`, you must fully exit Starsector and restart the launcher/game.
- **Why?** The Starsector class loader does not hot-reload JARs. Reloading a save or using "Reload Scripts" (if available via other mods) will often keep the old version of the JAR in memory, leading to confusing debugging sessions where changes don't appear to take effect.

Note on PowerShell compatibility
- The `build.ps1` script is compatible with both Windows PowerShell 5.1 and PowerShell 7+ (`pwsh`). Older versions of PowerShell may lack some modern cmdlets (for example `Join-String`), so the script prefers cross-version constructs where possible. If you run into odd errors in PowerShell 5.1, try the script with `pwsh` or update your PowerShell to a newer release.

## Versioning & release automation (important)
We added an automated consistency check and release helper to avoid TriOS failures caused by mismatched version metadata.

- `.github/scripts/check-versions.ps1` compares the versions in `mod_info.json`, `version.json`, and `Ramscoop.version` and exits non-zero if they differ.
- CI (`.github/workflows/ci.yml`) runs the check on PRs and pushes and will fail the job if versions are inconsistent.
- The release workflow (`.github/workflows/release.yml`) extracts the release tag (e.g., `v0.7.2` → `0.7.2`), runs the checker in `-Fix` mode to update those three files to the tag version, and commits/pushes the updated files back to `main` so packaging uses the correct metadata.

Notes and caveats:
- The `-Fix` mode rewrites `Ramscoop.version` as JSON and will remove any comment lines. If you rely on comments in that file, suggest leaving them out or ask for a conservative in-place patcher (we can add one).
- If you prefer the release workflow to create a PR instead of committing to `main` directly, we can change that behavior.

## How to run the version checker locally
- Check only:

```powershell
pwsh .github/scripts/check-versions.ps1
```

- Auto-fix locally (this will rewrite files):

```powershell
pwsh .github/scripts/check-versions.ps1 -Fix -Version 0.7.2
```

After running `-Fix`, review the changed files and commit them.

## Testing checklist (developer)
- Build with `pwsh .\build.ps1` and confirm `jars/Ramscoop.jar` exists
- Run the game with LunaLib enabled and open the settings to ensure all entries show correctly
- Verify runtime behavior by entering a nebula/corona and observing fuel/supplies generation
- Run `pwsh .github/scripts/check-versions.ps1` to ensure version metadata is consistent before tagging a release

## Release process (maintainer)
1. Tag the release with `git tag vMAJOR.MINOR.PATCH` and push the tag
2. The release workflow will run, auto-fix version files to the tag (if necessary), commit them, and package the release assets
3. Verify the created GitHub release and that `Ramscoop-<tag>.zip` contains the expected files
4. TriOS and other automated updaters will read `Ramscoop.version` and `mod_info.json` to detect the new release

## Contributing
- Fork, create a branch, make changes, open a PR
- Ensure `pwsh .github/scripts/check-versions.ps1` passes on your branch before requesting review
- Keep runtime `DEBUG_MODE` off for PRs unless debugging a specific issue

---
