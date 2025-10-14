param(
    [switch]$Fix,
    [string]$Version
)

function Read-JsonAllowHashComments($path) {
    if (-not (Test-Path $path)) { throw "Missing file: $path" }
    $raw = Get-Content $path -Raw
    $clean = ($raw -split "`n" | Where-Object { $_ -notmatch '^\s*#' }) -join "`n"
    return $clean | ConvertFrom-Json -ErrorAction Stop
}

function Write-JsonPreserve($obj, $path) {
    $json = $obj | ConvertTo-Json -Depth 10
    Set-Content -LiteralPath $path -Value $json -Encoding UTF8
}

# Determine repo root reliably whether running locally or in GitHub Actions.
# Script lives at .github/scripts/check-versions.ps1, so repo root is two levels up.
$scriptDir = $PSScriptRoot
if (-not $scriptDir) {
    $scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
}
$repoRoot = Resolve-Path (Join-Path $scriptDir '..\..') | Select-Object -ExpandProperty Path
Set-Location $repoRoot

$paths = @{
    mod_info = "mod_info.json"
    version_json = "version.json"
    ramscoop = "Ramscoop.version"
}

try {
    $modInfo = Read-JsonAllowHashComments $paths.mod_info
    $versionJson = Read-JsonAllowHashComments $paths.version_json

    # Ramscoop.version may contain # comments; read-clean separately
    $rawR = Get-Content $paths.ramscoop -Raw
    $cleanR = ($rawR -split "`n" | Where-Object { $_ -notmatch '^\s*#' }) -join "`n"
    $ramscoop = $cleanR | ConvertFrom-Json

    $v_modinfo = ($modInfo.version).ToString().Trim()
    $v_versionjson = ($versionJson.version).ToString().Trim()
    $v_ramscoop = "{0}.{1}.{2}" -f $ramscoop.modVersion.major, $ramscoop.modVersion.minor, $ramscoop.modVersion.patch

    Write-Host "Found versions:"
    Write-Host " - mod_info.json: $v_modinfo"
    Write-Host " - version.json : $v_versionjson"
    Write-Host " - Ramscoop.version: $v_ramscoop"

    $all = @($v_modinfo, $v_versionjson, $v_ramscoop)
    $uniq = $all | Select-Object -Unique

    if ($uniq.Count -eq 1) {
        Write-Host "OK: All versions match ($($uniq[0]))."
        exit 0
    } else {
        Write-Error "Version mismatch detected."
        $pairs = @{
            "mod_info.json" = $v_modinfo
            "version.json" = $v_versionjson
            "Ramscoop.version" = $v_ramscoop
        }
        foreach ($k in $pairs.Keys) {
            Write-Error (" - {0}: {1}" -f $k, $pairs[$k])
        }

        if ($Fix -and $Version) {
            Write-Host "Applying fix: setting all versions to $Version"
            # Update mod_info.json
            $modInfo.version = $Version
            Write-JsonPreserve $modInfo $paths.mod_info

            # Update version.json
            $versionJson.version = $Version
            Write-JsonPreserve $versionJson $paths.version_json

            # Update Ramscoop.version (assumes semantic vX.Y.Z)
            $parts = $Version.Split('.')
            if ($parts.Count -ne 3) { throw "Version must be semantic MAJOR.MINOR.PATCH" }
            $ramscoop.modVersion.major = [int]$parts[0]
            $ramscoop.modVersion.minor = [int]$parts[1]
            $ramscoop.modVersion.patch = [int]$parts[2]

            # Preserve any comments by reconstructing file without commented lines + new JSON
            $newR = ($ramscoop | ConvertTo-Json -Depth 10)
            Set-Content -LiteralPath $paths.ramscoop -Value $newR -Encoding UTF8

            Write-Host "Versions updated. Please review changes, commit and push."
            exit 0
        }

        exit 2
    }
}
catch {
    Write-Error "Error while checking versions: $_"
    exit 3
}
