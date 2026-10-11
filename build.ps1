# Windows build (also runs under pwsh elsewhere): compiles src/ramscoop against the game + LunaLib jars into jars/Ramscoop.jar.
# Mirrors build.sh. Override paths with $env:SS_DIR (folder holding starfarer.api.jar) and $env:LUNALIB_JAR.
# Needs a JDK 17+ (javac/jar/java) on PATH. -Install also copies the mod into the game as mods/Ramscoop.
param([switch]$Install)
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot

$ssDir = $env:SS_DIR
if (-not $ssDir) {
    $ssDir = @(
        'C:\Program Files (x86)\Fractal Softworks\Starsector\starsector-core',
        'C:\Program Files\Fractal Softworks\Starsector\starsector-core',
        'G:\Starsector\starsector-core'
    ) | Where-Object { Test-Path (Join-Path $_ starfarer.api.jar) } | Select-Object -First 1
}
if (-not $ssDir -or -not (Test-Path (Join-Path $ssDir starfarer.api.jar))) { throw 'starfarer.api.jar not found; set SS_DIR' }

$luna = $env:LUNALIB_JAR
if (-not $luna) {
    $luna = Get-ChildItem (Join-Path $ssDir mods), (Join-Path $ssDir ../mods), .. -Recurse -Depth 3 -Filter LunaLib.jar -ErrorAction SilentlyContinue |
        Select-Object -First 1 -ExpandProperty FullName
}
if (-not $luna -or -not (Test-Path $luna)) { throw 'LunaLib.jar not found; set LUNALIB_JAR' }

$sep = [IO.Path]::PathSeparator
$cp = (@('starfarer.api.jar', 'log4j-1.2.9.jar', 'lwjgl_util.jar', 'json.jar') | ForEach-Object { Join-Path $ssDir $_ }) + $luna -join $sep
$out = Join-Path build classes
$test = Join-Path build test
Remove-Item -Recurse -Force $out -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force $out, jars | Out-Null

javac --release 17 -nowarn -cp $cp -d $out (Get-ChildItem src/ramscoop/*.java).FullName
if ($LASTEXITCODE) { exit $LASTEXITCODE }
jar cf jars/Ramscoop.jar -C $out ramscoop
if ($LASTEXITCODE) { exit $LASTEXITCODE }

javac --release 17 -cp "$cp$sep$out" -d $test (Get-ChildItem src/test/game/ramscoop/*.java).FullName
if ($LASTEXITCODE) { exit $LASTEXITCODE }
java -cp "$cp$sep$out$sep$test" ramscoop.FuelMathCheck
if ($LASTEXITCODE) { exit $LASTEXITCODE }

Write-Host "Built jars/Ramscoop.jar (SS_DIR=$ssDir, LunaLib=$luna)"

if ($Install) {
    # mods/ sits beside starsector-core on Windows, next to starfarer.api.jar on Linux/macOS.
    $mods = Join-Path $ssDir mods
    if (-not (Test-Path $mods)) { $mods = Join-Path $ssDir ../mods }
    $dest = Join-Path $mods Ramscoop
    # Same file list as the release zip (.github/workflows/release.yml).
    Remove-Item -Recurse -Force $dest -ErrorAction SilentlyContinue
    New-Item -ItemType Directory -Force $dest | Out-Null
    Copy-Item -Recurse jars, data, mod_info.json, Ramscoop.version, changelog.txt, README.md, LICENSE.txt $dest
    Write-Host "Installed to $dest (restart Starsector to load it)"
}
