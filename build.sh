#!/usr/bin/env bash
# Linux/macOS build: compiles src/ramscoop against the game + LunaLib jars into jars/Ramscoop.jar.
# Override paths with SS_DIR (folder holding starfarer.api.jar) and LUNALIB_JAR.
set -euo pipefail
cd "$(dirname "$0")"

SS_DIR="${SS_DIR:-}"
if [[ -z "$SS_DIR" ]]; then
    for d in /mnt/Games/starsector "$HOME/starsector" "$HOME/Games/starsector" /opt/starsector; do
        [[ -f "$d/starfarer.api.jar" ]] && SS_DIR="$d" && break
    done
fi
[[ -f "$SS_DIR/starfarer.api.jar" ]] || { echo "starfarer.api.jar not found; set SS_DIR" >&2; exit 1; }

LUNALIB_JAR="${LUNALIB_JAR:-$(find "$SS_DIR/mods" "$(dirname "$PWD")" -maxdepth 4 -path '*LunaLib*/jars/LunaLib.jar' 2>/dev/null | head -n1)}"
[[ -f "$LUNALIB_JAR" ]] || { echo "LunaLib.jar not found; set LUNALIB_JAR" >&2; exit 1; }

CP="$SS_DIR/starfarer.api.jar:$SS_DIR/log4j-1.2.9.jar:$SS_DIR/lwjgl_util.jar:$SS_DIR/json.jar:$LUNALIB_JAR"
OUT="build/classes"
rm -rf "$OUT" && mkdir -p "$OUT" jars

javac --release 17 -nowarn -cp "$CP" -d "$OUT" src/ramscoop/*.java
jar cf jars/Ramscoop.jar -C "$OUT" ramscoop

javac --release 17 -cp "$CP:$OUT" -d build/test src/test/game/ramscoop/*.java
java -cp "$CP:$OUT:build/test" ramscoop.FuelMathCheck

echo "Built jars/Ramscoop.jar (SS_DIR=$SS_DIR, LunaLib=$LUNALIB_JAR)"
