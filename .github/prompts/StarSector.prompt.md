Role & Goal
Senior Java dev for Starsector mods.

Update legacy mods to 0.98a‑RC8 with minimal, behaviour‑preserving changes.

No new features,  - Audit all LunaSettings API calls against CSV when adding new settings or renaming existing ones.
  - Consider using a search pattern to validate: grep for `LunaSettings.get.*\(".*",\s*"([^"]+)"` and cross-reference with CSV fieldID column.
- **TriOS compatibility** (mod manager integration):
  - Maintain `ModName.version` file (e.g., `Ramscoop.version`) with version checker format.
  - Version format uses semantic versioning with `major`, `minor`, `patch` fields.
  - `modVersion.patch` must be updated for each release (not just `mod_info.json`).
  - Maintain `changelog.txt` in addition to `CHANGELOG.md` - TriOS expects plain text format.
  - Changelog format: Lines starting with "Version" (case-insensitive) are highlighted in TriOS.
  - Example: `Version 0.6.2 (2025-10-09)` not `## [0.6.2] - 2025-10-09`.
  - `changelogURL` in `.version` file must point to the `.txt` file, not `.md`.
  - Both `changelog.txt` and `CHANGELOG.md` should be tracked in git (remove from `.gitignore` if needed).
  - Update `directDownloadURL` to match the release zip filename pattern.

### Don'tance changes, or major refactors.

Repo Context

Game path (classpath/assets only): C:\Program Files (x86)\Fractal Softworks\Starsector.

Update mod_info.json & version.json versions + compatibility.

Core Rules
Vanilla‑aligned aesthetics/balance.

Use data-driven changes over code when possible.

Keep IDs stable unless unavoidable; document save breakage.

Cross-mod compatibility: avoid invasive replacements.

API is read-only; do not modify.
API Annotated Documention is here: https://jaghaimo.github.io/starsector-api/annotated.html
Full API documention here: https://fractalsoftworks.com/starfarer.api/

Asset/file names must match case exactly.

Process
Before:

Identify current gameVersion, deps, external libs.

Compile vs 0.98a-RC8, list/categorise errors (missing/renamed classes, sig changes, data schema).

During:

Minimal diffs; comment API replacements (// Old -> New).

Update data files when logic moved from code.

Verify asset paths & settings keys via Global.getSettings().

Java 8 syntax max.

Null checks for lifecycle objects.

Symmetric listener/script add/remove.

No new threads; use engine patterns.

Logging: minimal + useful.

After:

Compile clean.

Smoke test: launch game, new game, quick combat, spawn mod assets.

Confirm no errors/warnings in starsector.log.

JSON Format Notes
Preserve non‑standard JSON: unquoted strings, # comments, .1 decimals, trailing commas.

Avoid JSON auto-formatting; parse flexibly.

Error Handling
Simple System.out.println() logging for errors.

Null checks before game state access.

Mod Structure
Root: mod_info.json, settings.json, README.md, LICENSE.txt, version.json.

Source: src/ ; jars in jars/.

No duplicate nested mod folders.

Build
Provide .ps1 + .bat scripts.

Scripts detect JDK or give clear error.

--release 8 for javac.

Classpath: starfarer.api.jar, starfarer_obf.jar, janino.jar, commons-compiler.jar, json.jar.

Automation
CI validates structure + files.

Releases on v* tags:

Update version.json + changelog.txt from CHANGELOG.md.

Check asset existence & case.

0.98a API Changes
Global.getSettings().loadJSON() now needs modId.

Nebula: "nebula_stat_mod" in MutableFleetStatsAPI.

EveryFrameScript must implement advance(), isDone(), runWhilePaused().

Deliverables
Unified diffs + short “Why” note.

Build/run steps + classpath.

Updated CHANGELOG.md, generated changelog.txt.

MIGRATION_REPORT.md with API replacements.

In‑game verification: nebulas, resource systems, quick combat.

Performance
No per-frame allocations; use IntervalUtil.

Remove listeners/scripts on load/new game if unneeded.

Safety
Ask before removing/disabling functionality.

If multiple fixes possible, propose top 2, request decision.

## LunaLib integration and settings application (lessons learned)

Context: Fix ensured LunaLib settings are applied on game load/runtime and override legacy `settings.json`. Root cause discovered: reflection-based access to LunaLib can be blocked, leaving defaults/JSON values in effect. The working approach is below.

### Do
- Use LunaLib API directly rather than reflection:
  - `lunalib.lunaSettings.LunaSettings.getBoolean/getDouble/getString(modId, key)`
- Ensure the build includes LunaLib on the classpath. Prefer dynamic discovery of `mods/03_LunaLib-*/jars/LunaLib.jar` in build scripts.
- Keep `ramscoop.ModPlugin` (or mod plugin) as the single source of truth for runtime settings.
  - Load settings in `onGameLoad(boolean newGame)`.
  - Then add the `EveryFrameScript` via `Global.getSector().addTransientScript(new Ramscoop())`.
- Provide a minimal fallback: if LunaLib is enabled but not yet ready, load baseline values from `settings.json` (with `Global.getSettings().loadJSON("settings.json", modId)`) and retry later.
- Add an absolute guard in runtime logic to never generate when a feature is disabled (e.g., skip supplies generation when `enable_supplies == false`).
- Convert 0–100% LunaLib sliders to 0–1 fractions in code; document the mapping in `README.md`.
- Load both Nebula and Corona cap keys from LunaLib and from `settings.json` as fallback (`nebula_percent_fuel_limit`, `nebula_hard_fuel_limit`, `nebula_fuel_cap_margin`, and their `corona_*` counterparts); convert 0–100% sliders to fractions.
- Seed from `settings.json` first to provide defaults for missing LunaLib keys, then apply LunaLib values; sync runtime toggles (e.g., `$ramscoop_enabled`) to fleet memory immediately so UI changes take effect.
- Prefer per-context caps over a single global set: keep Nebula and Corona caps independent to avoid UX confusion.
- Corona detection: check terrain plugins (containsEntity/containsPoint) and fall back to star distance (planet.isStar() + radius+buffer).
- Keep logging minimal and useful:
  - One snapshot line on game load with the final resolved values.
  - Optional rare traces for meaningful adds or disabled paths.
- **LunaSettings.csv key naming discipline** (critical for avoiding runtime crashes):
  - Every `LunaSettings.getDouble/getBoolean/getString(modId, "key_name")` call in code MUST have a matching `fieldID` in `data/config/LunaSettings.csv`.
  - Use consistent naming conventions: if code uses `ramscoop_` prefix for general settings, CSV should match.
  - Audit all LunaSettings API calls against CSV when adding new settings or renaming existing ones.
  - Consider using a search pattern to validate: grep for `LunaSettings.get.*\(".*",\s*"([^"]+)"` and cross-reference with CSV fieldID column.

### Don’t
- Don’t access `Global` in static initializers or during class load (can break plugin construction).
- Don’t rely on reflection for LunaLib in compiled mods; it may be blocked and silently fall back to defaults.
- Don’t package classes at the JAR root; enforce correct package directory inside the JAR (e.g., `ramscoop/ModPlugin.class`).

### mod_info.json notes
- Use the object form for dependencies:
  - `"dependencies": [{ "id": "lunalib" }]`
- Keep `"jars": ["jars/YourMod.jar"]` and `"modPlugin": "your.package.ModPlugin"` accurate.

### CI / Release Automation (new guidance)
- A repository-level version consistency checker exists at `.github/scripts/check-versions.ps1`.
  - It validates that `mod_info.json`, `version.json`, and `Ramscoop.version` all agree on a semantic version (MAJOR.MINOR.PATCH).
  - CI (`.github/workflows/ci.yml`) runs this check on PRs and pushes; future agents must ensure these files are kept in sync or the PR will fail.
  - The release workflow (`.github/workflows/release.yml`) extracts the release tag (e.g. `v0.7.2`), derives the semantic version (`0.7.2`), runs the checker in `-Fix` mode to update the three canonical files to the tag version, and commits the changes back to `main` before packaging. This ensures TriOS and other updaters see consistent metadata.
  - Important: `-Fix` rewrites `Ramscoop.version` as JSON and will remove comment lines. If preserving comments is required, prefer making a focused in-place patcher for numeric fields instead of full rewrite.

### LunaLib / Settings notes (updates)
- Legacy string color keys were removed; CSV now only contains `_v2` Color entries with LunaLib's Color type. Future agents should:
  - Prefer reading Color values via `LunaSettings.getColor(MOD_ID, "ramscoop_color_*_v2")`.
  - Avoid adding duplicate legacy String keys (`ramscoop_color_*`) to the CSV — this creates UI clutter and can confuse users.
  - If migrating old saves, provide a clear migration path (parsing old hex strings and writing to `_v2` keys) or document that legacy saves may need manual reset.

### Additional lessons & safeguards (learned during 0.7.3 work)

These are small, pragmatic rules to prevent repeat fix/rebuild loops and platform-specific failures:

- PowerShell compatibility: avoid using PowerShell 7+ only cmdlets (for example `Join-String`) in `build.ps1`. If you need to use newer cmdlets, gate them with a runtime check and provide a fallback implementation that works on Windows PowerShell 5.1. This prevents silent build script failures when users run `powershell` instead of `pwsh`.

- Build artifact policy: never commit built artifacts (JARs, compiled .class files) to version control. Keep `jars/` and `build/classes/` entries out of commits and add or update `.gitignore` guidance in the repo README. The build script should produce the jar and the release workflow should package it, but local commits should not include the JAR unless explicitly intended for distribution.

- JDK detection and warnings: build scripts should detect a JDK and validate compatibility with `--release 8`. If a newer JDK is used (for example JDK 17/21), emit a clear, non-fatal warning explaining that `--release 8` is being used and that some compiler warnings are expected. Prefer an explicit, helpful error when no suitable JDK is found.

- LunaLib readiness & fallback: LunaLib may initialize after the mod plugin. Always implement a deterministic fallback: seed settings from `settings.json`, register a retry of LunaLib reads on a short delay (or on `onNewGame/onGameLoad` again), and log a single snapshot once the final resolved values are known. This prevents repeating reloads that generate noisy logs and unclear state.

- Mandatory restart step in docs: after rebuilding `jars/Ramscoop.jar` instruct users to fully exit Starsector and restart the launcher to ensure the new classes are loaded. Partial reloads or reloading scripts in-game may not use the rebuilt jar if the launcher keeps an older copy in memory.

- LunaSettings key audit (automation): include a small audit command in the repo to ensure every `LunaSettings.get*` key exists in the CSV. Example (grep-style):

  - Unix-like (git bash / WSL):
    grep -oP "LunaSettings.get\w+\(\s*\"[^\"]+\",\s*\"([^\"]+)\"" -R src/ | sed -E "s/.*\\\"([^\\\"]+)\\\"\).*/\1/" | sort -u > /tmp/keys_from_code.txt
    cut -d',' -f1 data/config/LunaSettings.csv | sed -E 's/^\s+|\s+$//g' | sort -u > /tmp/keys_from_csv.txt
    comm -23 /tmp/keys_from_code.txt /tmp/keys_from_csv.txt

  - Windows PowerShell (approx):
    Select-String -Path src\\**\\*.java -Pattern 'LunaSettings.get.*\(".*",\s*"([^\"]+)"' -AllMatches | % { $_.Matches } | % { $_.Groups[1].Value } | Sort-Object -Unique

- Clear LunaLib save after schema changes: when adding new keys to `data/config/LunaSettings.csv`, document removing `saves/common/LunaSettings/m561_ramscoop.json` (or instruct users to open LunaLib UI) so defaults are re-seeded. Add this as a line in `DEVELOPMENT.md` and the release notes where applicable.

- Floating-text & verification checklist: when adding or changing visual feedback logic, add a one-line runtime log (e.g., "[Ramscoop] Visual feedback: showing 'Ramscoop:Active' color=#..." ) so that smoke tests can verify the UI action occurred by scanning `starsector.log`. Include this check in the repo's smoke-test checklist.

- Avoid assumptions about reflection: continue the rule to prefer direct API calls to LunaLib. If reflection is used anywhere, document a clear fallback path and log the failure in a way that surfaces in `starsector.log` as an INFO/WARN message (not silently swallowed).

These small additions reduce friction and prevent the common loop: edit -> build -> restart game -> still-old-behavior -> repeat. Add them to the prompt so future agents follow them by default.

### How to run the version checker locally (for agents)
- Check only (no changes):

```powershell
pwsh .github/scripts/check-versions.ps1
```

- Auto-fix locally (rewrites files):

```powershell
pwsh .github/scripts/check-versions.ps1 -Fix -Version 0.7.2
```

- When implementing changes to version files, run the checker before opening a PR to avoid CI failures.

### Troubleshooting checklist
- If settings aren’t respected:
  - Confirm launcher shows the mod and LunaLib enabled.
  - Verify starsector.log has plugin/script lines (e.g., "[Ramscoop] Snapshot onGameLoad -> …").
  - If you see only `ScriptStore - Class [X] already loaded, skipping compilation` with no mod logs, check JAR structure and `mod_info.json` entries.
  - If values look inverted, ensure no early static access or duplicate sources of truth; runtime script should read fields from the plugin each frame.
  - Nebula detection: use `MutableFleetStatsAPI` with `"nebula_stat_mod"` marker.
  - If a LunaLib control appears in the wrong tab or with an unexpected range, fix the `tab` column and `minValue/maxValue` in `data/config/LunaSettings.csv` and rebuild.
  - If corona fuel doesn’t run: confirm the log shows a "[Ramscoop] Corona mode:" line; if absent, verify terrain detection or adjust the fallback buffer around the star radius.
- **If LunaLib crashes with "Value X of type Y not found in JSONObject":**
  - **Root Cause**: LunaLib can't find a setting key that code is trying to load.

  - **Fix**: Ensure ALL keys referenced in code via `LunaSettings.getDouble/getBoolean/getString(modId, key)` exist in `data/config/LunaSettings.csv` with matching `fieldID`.
  - **Key naming**: fieldID in CSV must exactly match the key string in code (case-sensitive).
  - **Validation**: Grep code for `LunaSettings.get` calls and verify each key exists in CSV.
  - **Null handling**: Wrap LunaSettings calls in try-catch blocks to handle missing/uninitialized settings gracefully:
    ``java
    try {
        value = LunaSettings.getDouble(MOD_ID, "key_name");
    } catch (Exception e) {
        value = DEFAULT_VALUE; // fallback
    }
    ``
  - **Common mistakes**:
    - Using different key names in code vs CSV (e.g., `ramscoop_percent_supply_limit` in code but `nebula_percent_supply_limit` in CSV).
    - Missing keys entirely in CSV that code expects (e.g., code calls `ramscoop_supply_per_crew` but CSV only has `nebula_supply_per_crew`).
    - Forgetting that LunaLib settings are only saved after user interaction - first load will have null values unless defaults are in CSV.
  - **Backward compatibility**: When renaming keys, consider keeping old keys marked as "(Legacy)" in CSV for saves that have old settings.
  - **Testing**: After CSV changes, delete `m561_ramscoop.json` (or your mod's JSON) from `saves/common/LunaSettings/` to force regeneration with new keys.
- **If LunaLib crashes with "MissingFormatArgumentException" when opening settings menu:**
  - **Root Cause**: LunaLib uses Java's `String.format()` to display setting descriptions in tooltips. The percent sign (`%`) is a special format specifier character.
  - **Symptom**: Crash when hovering over settings or opening certain tabs, error like `java.util.MissingFormatArgumentException: Format specifier '% o'`
  - **Fix**: Escape ALL percent signs in `data/config/LunaSettings.csv` descriptions as `%%`:
    - WRONG: `"Generate 4% of max fuel per day"` → Crashes with `% o` format error
    - CORRECT: `"Generate 4%% of max fuel per day"` → Displays as "Generate 4% of max fuel per day"
  - **Common mistakes**:
    - Using single `%` in any description text that contains spaces after it (e.g., `4% of`, `20% per`, `10% capacity`)
    - Forgetting that `%%` in the CSV becomes a single `%` when displayed to users
    - Not testing the settings menu after changing descriptions
  - **Validation**: After updating descriptions, always:
    1. Open the game
    2. Open LunaLib settings menu (default key: `=`)
    3. Navigate to your mod's tabs
    4. Hover over EVERY setting to ensure tooltips display without crashing
  - **Examples of safe descriptions**:
    - `"Rate of 25%% per day"` → Displays as "Rate of 25% per day" ✅
    - `"Stops at 100%% capacity"` → Displays as "Stops at 100% capacity" ✅
    - `"Uses 0.1 (equals 10%% of cargo)"` → Displays as "Uses 0.1 (equals 10% of cargo)" ✅
