# PowerShell build script for Ramscoop mod

# Configuration
$SS_DIR = "C:\Program Files (x86)\Fractal Softworks\Starsector"
$MOD_NAME = "Passive-Ramscoop"
$MOD_ID = "m561_ramscoop"
$SRC_DIR = "$PWD\Passive-Ramscoop\src"
$OUT_DIR = "$PWD\Passive-Ramscoop\jars"

# Create directories if they don't exist
if (-not (Test-Path -Path "$OUT_DIR")) {
    New-Item -ItemType Directory -Path "$OUT_DIR" -Force
}

# Find Java Home directory
$javaHome = $env:JAVA_HOME
if (-not $javaHome) {
    # Try to find Java home from registry if environment variable not set
    try {
        $javaHome = (Get-ItemProperty -Path 'HKLM:\SOFTWARE\JavaSoft\JDK' -ErrorAction SilentlyContinue).'JavaHome'
    } catch {
        # Continue even if registry key not found
    }
}

if (-not $javaHome) {
    # Try some common paths
    $commonPaths = @(
        "C:\Program Files\Java\jdk-24",
        "C:\Program Files\Java\jdk-17",
        "C:\Program Files\Java\jdk1.8.0_301",
        "C:\Program Files\Eclipse Adoptium\jdk-17.0.1.12-hotspot"
    )
    
    foreach ($path in $commonPaths) {
        if (Test-Path "$path\bin\jar.exe") {
            $javaHome = $path
            Write-Host "Found Java at: $javaHome" -ForegroundColor Green
            break
        }
    }
}

if (-not $javaHome) {
    Write-Host "ERROR: Cannot find Java JDK. Please ensure Java JDK is installed and JAVA_HOME is set." -ForegroundColor Red
    exit 1
}

# Show the Java Home being used
Write-Host "Using Java Home: $javaHome" -ForegroundColor Cyan

# Set up Java tools paths
$javac = "$javaHome\bin\javac.exe"
if (-not (Test-Path $javac)) {
    Write-Host "ERROR: Cannot find javac.exe at $javac" -ForegroundColor Red
    exit 1
}
Write-Host "Using Java compiler: $javac" -ForegroundColor Cyan

$jar = "$javaHome\bin\jar.exe"
if (-not (Test-Path $jar)) {
    Write-Host "ERROR: Cannot find jar.exe at $jar" -ForegroundColor Red
    exit 1
}
Write-Host "Using JAR utility: $jar" -ForegroundColor Cyan

# Set up classpath with all necessary libraries from Starsector
$CLASSPATH = @(
    "$SS_DIR\starsector-core\starfarer.api.jar",
    "$SS_DIR\starsector-core\starfarer_obf.jar",
    "$SS_DIR\starsector-core\janino.jar",
    "$SS_DIR\starsector-core\commons-compiler.jar",
    "$SS_DIR\starsector-core\commons-compiler-jdk.jar",
    "$SS_DIR\starsector-core\fs.common_obf.jar",
    "$SS_DIR\starsector-core\fs.sound_obf.jar",
    "$SS_DIR\starsector-core\lwjgl.jar",
    "$SS_DIR\starsector-core\lwjgl_util.jar",
    "$SS_DIR\starsector-core\json.jar",
    "$SS_DIR\starsector-core\log4j-1.2.9.jar"
)

# Build the classpath string
$CLASSPATH_STR = $CLASSPATH -join ";"

# Compile Java files
Write-Host "Compiling Java files..." -ForegroundColor Cyan
& $javac --release 8 -cp $CLASSPATH_STR -d "$SRC_DIR" "$SRC_DIR\ramscoop\*.java"
if ($LASTEXITCODE -ne 0) {
    Write-Host "Compilation failed!" -ForegroundColor Red
    exit $LASTEXITCODE
}

# Create JAR file
Write-Host "Creating JAR file..." -ForegroundColor Cyan
Push-Location $SRC_DIR
& $jar cf "$OUT_DIR\Ramscoop.jar" ramscoop\*.class
if ($LASTEXITCODE -ne 0) {
    Write-Host "JAR creation failed!" -ForegroundColor Red
    Pop-Location
    exit $LASTEXITCODE
}
Pop-Location

Write-Host "Build completed successfully!" -ForegroundColor Green
Write-Host "JAR file created at: $OUT_DIR\Ramscoop.jar" -ForegroundColor Green
