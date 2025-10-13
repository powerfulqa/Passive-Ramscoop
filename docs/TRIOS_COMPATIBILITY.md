TriOS compatibility checklist for Passive Ramscoop

This short guide lists the things required by TriOS (TriOS mod manager) to accept a mod release and the validation steps to confirm those requirements are met.

Checklist
- [ ] Semantic versioning: ensure `Ramscoop.version` contains numeric `major`, `minor`, and `patch` fields (e.g. `{"major":0,"minor":7,"patch":0}`) and matches other version files.
- [ ] `changelog.txt` must be plain text and include headers of the form: `Version X.X.X` (no markdown headings). TriOS reads the plain-text changelog directly.
- [ ] `mod_info.json` must have a `directDownloadURL` and `changelogURL` that point to the release assets (case-sensitive paths).
- [ ] Include `jars/Ramscoop.jar` in the release assets uploaded to the host used by TriOS (or make sure `directDownloadURL` points to a downloadable jar).
- [ ] `Ramscoop.version` and `version.json` should be kept in sync with `mod_info.json` to prevent TriOS version mismatches.
- [ ] Use a Git tag with `vMAJOR.MINOR.PATCH` (for example `v0.7.0`) when creating the release so TriOS and CI workflows can find the correct version.

Validation steps (quick)
1. Verify `Ramscoop.version` contains numeric fields and matches `mod_info.json`:
   - Open `Ramscoop.version` and check `major`, `minor`, `patch` values.
   - Open `mod_info.json` and `version.json` and ensure the `version` string matches `major.minor.patch`.

2. Verify `changelog.txt` formatting:
   - Ensure it is a plain-text file and contains a line starting with `Version ` followed by the version number and a newline before the content.

3. Verify `directDownloadURL` and `changelogURL`:
   - Check `mod_info.json` for the `directDownloadURL` and `changelogURL` fields.
   - Ensure the URLs are correct, reachable, and case-sensitive matches of the files you upload.

4. Package test:
   - Build with `.uild.ps1` to produce `jars/Ramscoop.jar`.
   - Locally host the `jar` (or place it where your release tooling expects) and confirm the `directDownloadURL` points to a working download.

5. Tag & release:
   - Create git tag `vMAJOR.MINOR.PATCH` and push tags: `git tag v0.7.0; git push --tags`.

Notes & tips
- TriOS expects plain text and exact path names. Double-check file case, especially on case-sensitive hosts.
- `Ramscoop.version` is specifically used by TriOS: keep it machine-friendly (plain JSON with numeric fields) and bump the `patch` field when publishing bugfixes.
- If you automate releases (CI), ensure your workflow uploads `jars/Ramscoop.jar` and `changelog.txt` and updates `mod_info.json` `directDownloadURL` to point at the uploaded asset.
