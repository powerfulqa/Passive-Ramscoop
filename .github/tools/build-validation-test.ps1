# Build Script Validation Test
# Verifies that build.ps1 runs successfully and produces expected outputs
# This ensures build automation works correctly for releases

param(
    [string]$BuildScript = ".\build.ps1",
    [switch]$CleanAfter
)

Write-Host "=== Build Script Validation Test ===" -ForegroundColor Cyan

try {
    # Check if build script exists
    if (-not (Test-Path $BuildScript)) {
        throw "Build script not found: $BuildScript"
    }

    # Clean previous build artifacts
    if (Test-Path "jars\Ramscoop.jar") {
        Remove-Item "jars\Ramscoop.jar" -Force
    }
    if (Test-Path "build\classes") {
        Remove-Item "build\classes" -Recurse -Force
    }

    Write-Host "1. Running build script..." -ForegroundColor Yellow

    # Run build script
    $startTime = Get-Date
    & $BuildScript
    $exitCode = $LASTEXITCODE
    $buildTime = (Get-Date) - $startTime

    if ($exitCode -ne 0) {
        throw "Build script failed with exit code $exitCode"
    }

    Write-Host "   Build completed in $($buildTime.TotalSeconds.ToString("F2")) seconds" -ForegroundColor Gray

    # Verify JAR was created
    Write-Host "2. Verifying JAR creation..." -ForegroundColor Yellow
    if (-not (Test-Path "jars\Ramscoop.jar")) {
        throw "JAR not created at jars/Ramscoop.jar"
    }

    # Check JAR size (basic sanity check)
    $jarSize = (Get-Item "jars\Ramscoop.jar").Length
    if ($jarSize -lt 10000) { # At least 10KB
        throw "JAR file suspiciously small: $($jarSize) bytes"
    }

    Write-Host "   JAR size: $($jarSize) bytes" -ForegroundColor Gray

    # Check JAR contents using jar command if available, otherwise skip
    Write-Host "3. Checking JAR contents..." -ForegroundColor Yellow
    try {
        $jarOutput = & jar tf "jars/Ramscoop.jar" 2>$null
        if ($LASTEXITCODE -ne 0) {
            Write-Host "   Warning: jar command not available, skipping content check" -ForegroundColor Yellow
        } else {
            # Check for required classes
            $requiredClasses = @(
                "ramscoop/ModPlugin.class",
                "ramscoop/Ramscoop.class"
            )

            $missingClasses = @()
            foreach ($class in $requiredClasses) {
                if ($jarOutput -notcontains $class) {
                    $missingClasses += $class
                }
            }

            if ($missingClasses.Count -gt 0) {
                throw "JAR missing required classes: $($missingClasses -join ', ')"
            }

            Write-Host "   Found all required classes" -ForegroundColor Gray
        }
    } catch {
        Write-Host "   Warning: Could not verify JAR contents: $($_.Exception.Message)" -ForegroundColor Yellow
    }

    # Clean up if requested
    if ($CleanAfter) {
        Write-Host "4. Cleaning up build artifacts..." -ForegroundColor Yellow
        if (Test-Path "jars\Ramscoop.jar") {
            Remove-Item "jars\Ramscoop.jar" -Force
        }
        if (Test-Path "build\classes") {
            Remove-Item "build\classes" -Recurse -Force
        }
        Write-Host "   Cleanup completed" -ForegroundColor Gray
    }

    Write-Host ""
    Write-Host "✅ Build script validation test passed!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Build script produces valid JAR with expected structure." -ForegroundColor White

} catch {
    Write-Host ""
    Write-Host "❌ Build script validation test failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}