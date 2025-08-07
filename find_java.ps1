# Java 8 Finder and Installer Helper
# This script helps locate Java 8 installations or guides you to install one

Write-Host "`n===== Java 8 JDK Finder and Installation Helper =====" -ForegroundColor Cyan

# Try to find existing Java installations
Write-Host "`nLooking for Java installations..." -ForegroundColor Yellow

$javaLocations = @(
    "C:\Program Files\Java\jdk*",
    "C:\Program Files (x86)\Java\jdk*",
    "C:\Program Files\Eclipse Adoptium\jdk*",
    "C:\Program Files\Eclipse Foundation\*",
    "C:\Program Files\AdoptOpenJDK\*",
    "C:\Program Files\Amazon Corretto\*"
)

$foundJava = $false
$jdk8Found = $false
$jdk8Path = ""

foreach ($location in $javaLocations) {
    $jdks = Get-ChildItem -Path $location -ErrorAction SilentlyContinue
    
    foreach ($jdk in $jdks) {
        $javacPath = Join-Path $jdk.FullName "bin\javac.exe"
        if (Test-Path $javacPath) {
            $foundJava = $true
            
            # Run Java version check
            $versionOutput = & $javacPath -version 2>&1
            Write-Host "Found: $($jdk.FullName)" -ForegroundColor Green
            Write-Host "  Version: $versionOutput"
            
            # Check if it's Java 8
            if ($versionOutput -match "1\.8\.") {
                $jdk8Found = $true
                $jdk8Path = $jdk.FullName
                Write-Host "  ✓ This is Java 8 (recommended for Starsector modding)" -ForegroundColor Green
            }
        }
    }
}

if (-not $foundJava) {
    Write-Host "`nNo Java installations found on this system." -ForegroundColor Red
} elseif (-not $jdk8Found) {
    Write-Host "`nJava was found, but no Java 8 JDK installations were detected." -ForegroundColor Yellow
    Write-Host "Starsector modding works best with Java 8." -ForegroundColor Yellow
}

# Provide installation instructions
Write-Host "`n===== Java 8 JDK Installation Instructions =====" -ForegroundColor Cyan

if ($jdk8Found) {
    Write-Host "`nGood news! You already have Java 8 JDK installed at:" -ForegroundColor Green
    Write-Host $jdk8Path -ForegroundColor Green
    Write-Host "`nTo update your build.bat, edit it and set JAVA_HOME to:" -ForegroundColor Yellow
    Write-Host "SET `"JAVA_HOME=$jdk8Path`"" -ForegroundColor White
} else {
    Write-Host "`nTo install Java 8 JDK:" -ForegroundColor Yellow
    Write-Host "1. Download from one of these sources:" -ForegroundColor White
    Write-Host "   - Eclipse Temurin (recommended): https://adoptium.net/" -ForegroundColor White
    Write-Host "     Select version 8 (LTS) when downloading" -ForegroundColor White
    Write-Host "   - Amazon Corretto 8: https://aws.amazon.com/corretto/" -ForegroundColor White
    Write-Host "`n2. Run the installer and note the installation directory" -ForegroundColor White
    Write-Host "`n3. Edit build.bat and update the JAVA_HOME variable with your installation path" -ForegroundColor White
    Write-Host "   Example: SET `"JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-8.0.302.8-hotspot`"" -ForegroundColor White
}

Write-Host "`n===== Next Steps =====" -ForegroundColor Cyan
if ($jdk8Found) {
    Write-Host "1. Update your build.bat with the Java path shown above" -ForegroundColor White
    Write-Host "2. Run build.bat to compile your mod" -ForegroundColor White
} else {
    Write-Host "1. Install Java 8 JDK using the instructions above" -ForegroundColor White
    Write-Host "2. Update your build.bat with the new Java path" -ForegroundColor White
    Write-Host "3. Run build.bat to compile your mod" -ForegroundColor White
}
Write-Host "" -ForegroundColor White

# Pause at the end
Write-Host "Press any key to exit..." -ForegroundColor Cyan
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
