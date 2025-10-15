# Runtime Log Verification Test
# Scans starsector.log for expected runtime logs after a game session
# This validates that the mod loads and behaves correctly in-game

param(
    [string]$LogPath = "$env:USERPROFILE\Documents\My Games\Starsector\starsector.log",
    [switch]$RequireVisualFeedback
)

Write-Host "=== Runtime Log Verification Test ===" -ForegroundColor Cyan
Write-Host "Log path: $LogPath" -ForegroundColor Gray

try {
    # Check if log file exists
    if (-not (Test-Path $LogPath)) {
        # Try alternative locations
        $altPaths = @(
            "G:\Games\Starsector\starsector-core\starsector.log",
            "C:\Program Files (x86)\Fractal Softworks\Starsector\starsector.log",
            "$env:ProgramFiles\Fractal Softworks\Starsector\starsector.log"
        )

        $found = $false
        foreach ($altPath in $altPaths) {
            if (Test-Path $altPath) {
                $LogPath = $altPath
                $found = $true
                break
            }
        }

        if (-not $found) {
            throw "Could not find starsector.log. Searched locations:`n" +
                  "  - $env:USERPROFILE\Documents\My Games\Starsector\starsector.log`n" +
                  "  - G:\Games\Starsector\starsector-core\starsector.log`n" +
                  "  - C:\Program Files (x86)\Fractal Softworks\Starsector\starsector.log`n" +
                  "Please specify the correct path with -LogPath parameter"
        }
    }

    Write-Host "Found log file: $LogPath" -ForegroundColor Gray

    # Read the log file
    Write-Host "1. Reading log file..." -ForegroundColor Yellow
    $logContent = Get-Content $LogPath -Raw

    if ([string]::IsNullOrEmpty($logContent)) {
        throw "Log file is empty"
    }

    $logSize = [math]::Round($logContent.Length / 1MB, 2)
    Write-Host "   Log size: $logSize MB" -ForegroundColor Gray

    # Check for mod loading
    Write-Host "2. Checking for mod loading..." -ForegroundColor Yellow
    $hasModLoad = $logContent -match "\[Ramscoop\] ModPlugin constructed"
    $hasGameLoad = $logContent -match "\[Ramscoop\] Snapshot onGameLoad"

    if (-not $hasModLoad) {
        throw "Mod did not load - missing ModPlugin construction log"
    }

    if (-not $hasGameLoad) {
        throw "Mod did not initialize on game load - missing settings snapshot log"
    }

    Write-Host "   Mod loaded successfully" -ForegroundColor Gray

    # Check for script addition
    Write-Host "3. Checking for script registration..." -ForegroundColor Yellow
    $hasScriptAdd = $logContent -match "ScriptStore.*Ramscoop"

    if (-not $hasScriptAdd) {
        Write-Host "   Warning: Ramscoop script registration not found in log" -ForegroundColor Yellow
        Write-Host "   This may be normal if the log was from before entering a game" -ForegroundColor Yellow
    } else {
        Write-Host "   Script registered successfully" -ForegroundColor Gray
    }

    # Check for visual feedback (if required)
    if ($RequireVisualFeedback) {
        Write-Host "4. Checking for visual feedback logs..." -ForegroundColor Yellow
        $hasVisualFeedback = $logContent -match "\[Ramscoop\] Visual feedback"

        if (-not $hasVisualFeedback) {
            throw "Visual feedback not found in log. Did you enter a nebula/corona during the test session?"
        }

        Write-Host "   Visual feedback logged" -ForegroundColor Gray
    } else {
        Write-Host "4. Skipping visual feedback check (use -RequireVisualFeedback to enable)..." -ForegroundColor Yellow
    }

    # Check for errors
    Write-Host "5. Checking for errors..." -ForegroundColor Yellow
    $ramscoopErrors = ($logContent -split "`n" | Select-String -Pattern "ERROR.*ramscoop" -CaseSensitive:$false).Count
    $ramscoopWarnings = ($logContent -split "`n" | Select-String -Pattern "WARN.*ramscoop" -CaseSensitive:$false).Count

    if ($ramscoopErrors -gt 0) {
        throw "Found $ramscoopErrors ramscoop-related errors in log"
    }

    if ($ramscoopWarnings -gt 0) {
        Write-Host "   Warning: Found $ramscoopWarnings ramscoop-related warnings" -ForegroundColor Yellow
    } else {
        Write-Host "   No ramscoop errors or warnings found" -ForegroundColor Gray
    }

    # Check for LunaLib integration
    Write-Host "6. Checking LunaLib integration..." -ForegroundColor Yellow
    $hasLunaLib = $logContent -match "LunaLib"

    if ($hasLunaLib) {
        Write-Host "   LunaLib detected in log" -ForegroundColor Gray
    } else {
        Write-Host "   Warning: LunaLib not detected in log (may not be installed)" -ForegroundColor Yellow
    }

    Write-Host ""
    Write-Host "✅ Runtime log verification test passed!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Mod loads correctly and shows expected runtime behavior." -ForegroundColor White
    Write-Host "To test visual feedback, run with -RequireVisualFeedback after entering a nebula." -ForegroundColor Gray

} catch {
    Write-Host ""
    Write-Host "❌ Runtime log verification test failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    Write-Host "Troubleshooting:" -ForegroundColor Yellow
    Write-Host "  - Ensure Starsector was run with the mod enabled" -ForegroundColor Gray
    Write-Host "  - Check that LunaLib is installed if using LunaLib settings" -ForegroundColor Gray
    Write-Host "  - For visual feedback test, enter a nebula during gameplay" -ForegroundColor Gray
    exit 1
}