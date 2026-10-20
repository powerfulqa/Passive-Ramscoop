# Development

## Build
Needs JDK 17+, a Starsector 0.98a install and LunaLib in its `mods/` folder.

```sh
./build.sh [--install]        # Linux/macOS
pwsh build.ps1 [-Install]     # Windows (also works with Windows PowerShell 5.1)
```

Both compile `src/ramscoop` for Java 17 into `jars/Ramscoop.jar`, then run `FuelMathCheck` (fuel cap/rate maths, needs the game jars).
If the game or LunaLib is not found, set `SS_DIR` (the folder holding `starfarer.api.jar`) and/or `LUNALIB_JAR`.

The install option replaces the game's `mods/Ramscoop` with only the files the game uses (the same set as the release zip).
Starsector does not hot-reload jars: fully restart the game after each build.
`jars/Ramscoop.jar` is committed; rebuild and commit it with any change under `src/ramscoop`.

## Layout
- `src/ramscoop/ModPlugin.java` - reads LunaLib settings into static fields, on game load and when the player saves the settings menu. Percent sliders are 0-100 in the CSV and converted to fractions here.
- `src/ramscoop/Ramscoop.java` - transient `EveryFrameScript`: detects nebula/corona terrain, adds fuel and supplies, shows floating text and the message log report.
- `data/config/LunaSettings.csv` - the in-game settings menu.
- `changelog.txt` - the only changelog (plain text, `Version X.Y.Z (date)` headers, as read by TriOS and Version Checker).
- `mod_info.json`, `Ramscoop.version` - version metadata; versions and download URLs must match.

## Save safety
The mod is a utility mod (`"utility":true`) so players can remove it from a save. Nothing from `ramscoop` may be written to the save: use `addTransientScript`/`addTransientListener`, never `addScript`, persistent data or memory keys. `RepoCheck` enforces this.

## Editing LunaSettings.csv
- Every `fieldID` must be read in `ModPlugin.loadSettings()` and vice versa (checked by `RepoCheck`).
- Write literal percent signs in descriptions as `%%`; LunaLib formats them with `String.format()`.
- Text in `[brackets]` is highlighted in the menu; use it for the key words of a description.
- When a setting changes scale or default, give it a new key (e.g. `_v2`) so old saved values are not reused.

## Tests and CI
`src/test/java/ramscoop/RepoCheck.java` checks the settings CSV, its keys against `ModPlugin`, and the version files. It has no game dependency:

```sh
javac -d build/test-classes src/test/java/ramscoop/RepoCheck.java && java -cp build/test-classes ramscoop.RepoCheck
```

CI (`.github/workflows/ci.yml`) runs it on every push and PR. It also checks that the committed jar has a Java 17 class for every source file, and warns if sources were committed after the jar.
CI cannot compile the mod because the game jars are not public.

## Release
1. Set the version in `mod_info.json` and `Ramscoop.version` (both `version` and `directDownloadURL`).
2. Date the top `changelog.txt` entry: `Version X.Y.Z (YYYY-MM-DD)`.
3. Rebuild, commit, then `git tag vX.Y.Z && git push --tags`.

`.github/workflows/release.yml` runs CI, then `RepoCheck vX.Y.Z` checks that the tag, version files and changelog agree, then publishes `Ramscoop-vX.Y.Z.zip` with that changelog section as the release notes.
