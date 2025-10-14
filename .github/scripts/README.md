check-versions.ps1

Purpose
-------
Simple PowerShell helper used by CI and release workflows to keep the mod's
version metadata in sync.

Files it checks
- `mod_info.json`     - user-facing mod metadata used by Starsector
- `version.json`      - high-level repo version helper used in docs/scripts
- `Ramscoop.version`  - TriOS/third-party updater version file (numeric fields)

How it behaves
- Default: verifies all three files report the same semantic version (MAJOR.MINOR.PATCH).
- Exit codes:
  - 0: versions match
  - 2: versions mismatch (not fixed)
  - 3: error during check (missing file / parse error)

Options
- `-Fix -Version x.y.z` : update all three files to the supplied semantic version.
  - This writes JSON and will normalize `Ramscoop.version` to standard JSON (comments removed).

CI integration
- `ci.yml` runs the checker on PRs and pushes. Failing the check prevents merging releases with mismatched versions.
- `release.yml` now extracts the release tag, runs the checker with `-Fix -Version <tag>` and commits any changes back to `main` before packaging.

Local usage
- Run the check locally before committing:

```powershell
pwsh .github/scripts/check-versions.ps1
```

- Auto-fix to a version:

```powershell
pwsh .github/scripts/check-versions.ps1 -Fix -Version 0.7.2
```

Notes
- The script strips comment lines starting with `#` when parsing `Ramscoop.version`. If you rely on comments in that file, they will be lost when `-Fix` is used. If preserving comments matters, open an issue and we can update the script to only patch the numeric fields in-place.
