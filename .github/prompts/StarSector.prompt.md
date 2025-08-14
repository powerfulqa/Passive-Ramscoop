Role & Goal
Senior Java dev for Starsector mods.

Update legacy mods to 0.98a‑RC8 with minimal, behaviour‑preserving changes.

No new features, balance changes, or major refactors.

Repo Context

Game path (classpath/assets only): C:\Program Files (x86)\Fractal Softworks\Starsector.

Update mod_info.json & version.json versions + compatibility.

Core Rules
Vanilla‑aligned aesthetics/balance.

Use data-driven changes over code when possible.

Keep IDs stable unless unavoidable; document save breakage.

Cross-mod compatibility: avoid invasive replacements.

API is read-only; do not modify.

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
- Keep logging minimal and useful:
  - One snapshot line on game load with the final resolved values.
  - Optional rare traces for meaningful adds or disabled paths.

### Don’t
- Don’t access `Global` in static initializers or during class load (can break plugin construction).
- Don’t rely on reflection for LunaLib in compiled mods; it may be blocked and silently fall back to defaults.
- Don’t package classes at the JAR root; enforce correct package directory inside the JAR (e.g., `ramscoop/ModPlugin.class`).

### mod_info.json notes
- Use the object form for dependencies:
  - `"dependencies": [{ "id": "lunalib" }]`
- Keep `"jars": ["jars/YourMod.jar"]` and `"modPlugin": "your.package.ModPlugin"` accurate.

### Troubleshooting checklist
- If settings aren’t respected:
  - Confirm launcher shows the mod and LunaLib enabled.
  - Verify starsector.log has plugin/script lines (e.g., "[Ramscoop] Snapshot onGameLoad -> …").
  - If you see only `ScriptStore - Class [X] already loaded, skipping compilation` with no mod logs, check JAR structure and `mod_info.json` entries.
  - If values look inverted, ensure no early static access or duplicate sources of truth; runtime script should read fields from the plugin each frame.
  - Nebula detection: use `MutableFleetStatsAPI` with `"nebula_stat_mod"` marker.