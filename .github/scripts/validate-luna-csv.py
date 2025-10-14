#!/usr/bin/env python3
"""
Validate LunaSettings.csv files to prevent LunaLib parsing errors.

Checks performed:
- Header matches the expected LunaLib header exactly.
- Each non-empty row has exactly 9 columns when parsed as CSV.
- For Radio fields, the secondaryValue must not contain pipe characters ('|').
- Descriptions must not contain lone '%' characters; they must be escaped as '%%'.

Exit code 1 on any error; prints helpful diagnostics.
"""
import csv
import sys
from pathlib import Path

EXPECTED_HEADER = [
    'fieldID', 'fieldName', 'fieldType', 'defaultValue', 'secondaryValue',
    'fieldDescription', 'minValue', 'maxValue', 'tab'
]

errors = []

repo_root = Path(__file__).resolve().parents[2]
files = list(repo_root.rglob('LunaSettings.csv'))
if not files:
    print('No LunaSettings.csv files found in repo (ok if intentional)')
    sys.exit(0)

for p in files:
    rel = p.relative_to(repo_root)
    with p.open(newline='') as fh:
        raw = fh.read().splitlines()
    if not raw:
        errors.append(f'{rel}: file is empty')
        continue

    # Skip leading blank lines and comments to find header
    header_line = None
    header_idx = None
    for i, line in enumerate(raw):
        s = line.strip()
        if not s or s.startswith('#'):
            continue
        header_line = line
        header_idx = i
        break

    if header_line is None:
        errors.append(f'{rel}: no header found')
        continue

    # Parse header the same way CSV will parse it
    try:
        hdr = next(csv.reader([header_line]))
    except Exception as e:
        errors.append(f'{rel}: failed to parse header line: {e}')
        continue

    if hdr != EXPECTED_HEADER:
        errors.append(f'{rel}: header mismatch. Expected: {EXPECTED_HEADER} Got: {hdr}')

    # Now parse whole file robustly with csv.reader, but skip comment/blank lines
    reader = csv.reader([ln for ln in raw if ln.strip() and not ln.strip().startswith('#')])
    for idx, row in enumerate(reader, start=1):
        # row corresponds to (header_idx + idx - 1) line number in file
        lineno = header_idx + idx
        if len(row) != len(EXPECTED_HEADER):
            errors.append(f'{rel} : line {lineno} : expected {len(EXPECTED_HEADER)} columns, found {len(row)}')
            continue

        fieldID, fieldName, fieldType, defaultValue, secondaryValue, fieldDescription, minValue, maxValue, tab = row

        # Radio secondaryValue should use comma-separated options and not pipe separators
        if fieldType.strip().lower() == 'radio' and '|' in secondaryValue:
            errors.append(f"{rel} : line {lineno} : Radio field '{fieldID}' contains '|' in secondaryValue (use comma-separated options and quote the field)")

    # Descriptions must not include lone '%' characters; LunaLib uses String.format -> escape as '%%'
        desc = fieldDescription or ''
        i = 0
        while True:
            idxp = desc.find('%', i)
            if idxp == -1:
                break
            # If next char exists and is also '%', skip both
            if idxp + 1 < len(desc) and desc[idxp + 1] == '%':
                i = idxp + 2
                continue
            errors.append(f"{rel} : line {lineno} : description contains unescaped '%' (use '%%') for field '{fieldID}'")
            break

        # Non-description fields should not contain stray quotes (they'll be unquoted by csv.reader, but still check)
        for col_name, val in [('fieldID', fieldID), ('fieldName', fieldName), ('fieldType', fieldType), ('defaultValue', defaultValue), ('secondaryValue', secondaryValue), ('minValue', minValue), ('maxValue', maxValue), ('tab', tab)]:
            if '"' in val:
                errors.append(f"{rel} : line {lineno} : unexpected '\"' in {col_name} for field '{fieldID}'")

        # Legacy rows (intentionally labeled in the CSV) should not be present. Detect by
        # fieldName containing the word 'legacy' (case-insensitive). This prevents
        # reintroducing old compatibility keys into the LunaLib UI which make the menu
        # messy or confusing.
        if fieldName and 'legacy' in fieldName.lower():
            errors.append(f"{rel} : line {lineno} : legacy field detected ('{fieldID}' / '{fieldName}'). Remove legacy keys from LunaSettings.csv")

if errors:
    print('LunaSettings.csv validation failed with the following problems:')
    for e in errors:
        print(' -', e)
    sys.exit(1)

print('LunaSettings.csv validation passed for', len(files), 'file(s)')
sys.exit(0)
