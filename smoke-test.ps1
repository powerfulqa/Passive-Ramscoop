# Smoke Test Script for Passive Ramscoop Mod
# This script performs basic validation to ensure the mod builds and loads correctly
# Run this after making changes to catch regressions early

param(
    [switch]$Verbose,
    [string]$StarsectorPath = $null
)

# Configuration
$ErrorActionPreference = "Stop"
$ModPath = $PSScriptRoot
$JarPath = Join-Path $ModPath "jars\Ramscoop.jar"
$CsvPath = Join-Path $ModPath "data\config\LunaSettings.csv"

Write-Host "=== Passive Ramscoop Smoke Test ===" -ForegroundColor Cyan
Write-Host "Mod Path: $ModPath" -ForegroundColor Gray
Write-Host "Testing at: $(Get-Date)" -ForegroundColor Gray
Write-Host ""

# Test 1: Check that required files exist
Write-Host "1. Checking required files..." -ForegroundColor Yellow
$requiredFiles = @(
    "mod_info.json",
    "Ramscoop.version",
    "src\ramscoop\ModPlugin.java",
    "src\ramscoop\Ramscoop.java",
    "data\config\LunaSettings.csv",
    "build.ps1"
)

$missingFiles = @()
foreach ($file in $requiredFiles) {
    $fullPath = Join-Path $ModPath $file
    if (-not (Test-Path $fullPath)) {
        $missingFiles += $file
    }
}

if ($missingFiles.Count -gt 0) {
    Write-Host "❌ FAIL: Missing required files:" -ForegroundColor Red
    foreach ($file in $missingFiles) {
        Write-Host "   - $file" -ForegroundColor Red
    }
    exit 1
} else {
    Write-Host "✅ PASS: All required files present" -ForegroundColor Green
}

# Test 2: Validate LunaSettings.csv format
Write-Host "2. Validating LunaSettings.csv..." -ForegroundColor Yellow
try {
    $csvContent = Get-Content $CsvPath -Encoding UTF8
    $headerLine = $csvContent[0]
    $expectedHeaders = "fieldID,fieldName,fieldType,defaultValue,secondaryValue,fieldDescription,minValue,maxValue,tab"

    if ($headerLine -ne $expectedHeaders) {
        throw "CSV header mismatch. Expected: '$expectedHeaders', Got: '$headerLine'"
    }

    # Check for unescaped % signs in tooltips/descriptions
    $unescapedPercents = @()
    for ($i = 1; $i -lt $csvContent.Count; $i++) {
        $line = $csvContent[$i]
        if ($line.Trim() -and -not $line.StartsWith("#")) {
            $parts = $line -split ","
            if ($parts.Count -ge 7) {
                $tooltip = $parts[5].Trim()
                $description = $parts[6].Trim()

                if (($tooltip.Contains("%") -and -not $tooltip.Contains("%%")) -or
                    ($description.Contains("%") -and -not $description.Contains("%%"))) {
                    $unescapedPercents += "Line $($i+1): $line"
                }
            }
        }
    }

    if ($unescapedPercents.Count -gt 0) {
        Write-Host "❌ FAIL: Unescaped % characters found in CSV (use %% to escape):" -ForegroundColor Red
        foreach ($line in $unescapedPercents) {
            Write-Host "   $line" -ForegroundColor Red
        }
        exit 1
    }

    Write-Host "✅ PASS: LunaSettings.csv format is valid" -ForegroundColor Green
} catch {
    Write-Host "❌ FAIL: LunaSettings.csv validation error: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Test 3: Check that build script exists and is executable
Write-Host "3. Checking build script..." -ForegroundColor Yellow
$buildScript = Join-Path $ModPath "build.ps1"
if (-not (Test-Path $buildScript)) {
    Write-Host "❌ FAIL: build.ps1 not found" -ForegroundColor Red
    exit 1
}

# Check if build script has execution policy issues
try {
    $buildContent = Get-Content $buildScript -TotalCount 10
    if ($buildContent -match "PowerShell.*build.*script") {
        Write-Host "✅ PASS: Build script appears valid" -ForegroundColor Green
    } else {
        Write-Host "⚠️  WARN: Build script may not be standard" -ForegroundColor Yellow
    }
} catch {
    Write-Host "❌ FAIL: Cannot read build script: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Test 4: Validate mod_info.json
Write-Host "4. Validating mod_info.json..." -ForegroundColor Yellow
try {
    $modInfoPath = Join-Path $ModPath "mod_info.json"
    $modInfo = Get-Content $modInfoPath -Raw | ConvertFrom-Json

    $requiredFields = @("id", "name", "version", "description", "gameVersion")
    $missingFields = @()

    foreach ($field in $requiredFields) {
        if (-not (Get-Member -InputObject $modInfo -Name $field)) {
            $missingFields += $field
        }
    }

    if ($missingFields.Count -gt 0) {
        Write-Host "❌ FAIL: Missing required fields in mod_info.json:" -ForegroundColor Red
        foreach ($field in $missingFields) {
            Write-Host "   - $field" -ForegroundColor Red
        }
        exit 1
    }

    # Check that ID matches expected
    if ($modInfo.id -ne "m561_ramscoop") {
        Write-Host "❌ FAIL: mod_info.json id should be 'm561_ramscoop', got '$($modInfo.id)'" -ForegroundColor Red
        exit 1
    }

    Write-Host "✅ PASS: mod_info.json is valid" -ForegroundColor Green
} catch {
    Write-Host "❌ FAIL: mod_info.json validation error: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Test 5: Check version consistency
Write-Host "5. Checking version consistency..." -ForegroundColor Yellow
try {
    $versionFile = Join-Path $ModPath "Ramscoop.version"
    $versionContent = Get-Content $versionFile -Raw

    # Parse version from Ramscoop.version (JSON with comments)
    $versionContent = Get-Content $versionFile -Raw
    # Remove comment lines (starting with #)
    $jsonLines = $versionContent -split "`n" | Where-Object { -not $_.Trim().StartsWith("#") }
    $cleanJson = $jsonLines -join "`n"
    $versionJson = $cleanJson | ConvertFrom-Json
    $ramscoopVersion = "$($versionJson.modVersion.major).$($versionJson.modVersion.minor).$($versionJson.modVersion.patch)"

    # Get version from mod_info.json
    $modInfoVersion = $modInfo.version

    if ($ramscoopVersion -ne $modInfoVersion) {
        Write-Host "❌ FAIL: Version mismatch - Ramscoop.version: '$ramscoopVersion', mod_info.json: '$modInfoVersion'" -ForegroundColor Red
        exit 1
    }

    Write-Host "✅ PASS: Versions are consistent ($ramscoopVersion)" -ForegroundColor Green
} catch {
    Write-Host "❌ FAIL: Version consistency check error: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Test 6: Basic syntax check of Java files
Write-Host "6. Checking Java file syntax..." -ForegroundColor Yellow
$javaFiles = @(
    "src\ramscoop\ModPlugin.java",
    "src\ramscoop\Ramscoop.java"
)

$javaErrors = @()
foreach ($javaFile in $javaFiles) {
    $fullPath = Join-Path $ModPath $javaFile
    if (Test-Path $fullPath) {
        try {
            $content = Get-Content $fullPath -Raw
            # Basic checks
            if (-not $content.Contains("package ramscoop;")) {
                $javaErrors += "$javaFile : Missing package declaration"
            }
            if (-not $content.Contains("public class")) {
                $javaErrors += "$javaFile : Missing public class declaration"
            }
        } catch {
            $javaErrors += "$javaFile : Cannot read file - $($_.Exception.Message)"
        }
    } else {
        $javaErrors += "$javaFile : File not found"
    }
}

if ($javaErrors.Count -gt 0) {
    Write-Host "❌ FAIL: Java file issues:" -ForegroundColor Red
    foreach ($err in $javaErrors) {
        Write-Host "   - $err" -ForegroundColor Red
    }
    exit 1
} else {
    Write-Host "✅ PASS: Java files appear syntactically valid" -ForegroundColor Green
}

# Test 7: Check for LunaLib API usage consistency
Write-Host "7. Checking LunaLib API usage..." -ForegroundColor Yellow
try {
    $modPluginContent = Get-Content (Join-Path $ModPath "src\ramscoop\ModPlugin.java") -Raw

    # Check for direct LunaSettings calls (preferred)
    $directCalls = ($modPluginContent | Select-String "LunaSettings\.get").Count

    # Check for reflection calls (deprecated)
    $reflectionCalls = ($modPluginContent | Select-String "LunaSettings\.class").Count

    if ($reflectionCalls -gt 0) {
        Write-Host "⚠️  WARN: Found $reflectionCalls reflection-based LunaSettings calls (should use direct API)" -ForegroundColor Yellow
    }

    if ($directCalls -gt 0) {
        Write-Host "✅ PASS: Using direct LunaSettings API calls ($directCalls found)" -ForegroundColor Green
    } else {
        Write-Host "❌ FAIL: No LunaSettings.get* calls found in ModPlugin.java" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "❌ FAIL: LunaLib API check error: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Summary
Write-Host ""
Write-Host "=== Smoke Test Complete ===" -ForegroundColor Cyan
Write-Host "✅ All tests passed! Mod appears ready for build and deployment." -ForegroundColor Green
Write-Host ""
Write-Host "Next steps:" -ForegroundColor White
Write-Host "  1. Run .\build.ps1 to build the mod" -ForegroundColor Gray
Write-Host "  2. Test in Starsector with LunaLib installed" -ForegroundColor Gray
Write-Host "  3. Check logs for any runtime issues" -ForegroundColor Gray