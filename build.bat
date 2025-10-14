@echo off
setlocal enabledelayedexpansion

:: Configuration
:: Try to detect Starsector installation. Prefer Program Files (x86), fall back to G:\Starsector
set "SS_DIR="
:: Read ProgramFiles(x86) directly — avoid for/failure when the value contains spaces
set "PF86=%ProgramFiles(x86)%"
if defined PF86 set "DEFAULT_SS_DIR=%PF86%\Fractal Softworks\Starsector"
if not defined PF86 set "DEFAULT_SS_DIR=C:\Program Files (x86)\Fractal Softworks\Starsector"
if exist "%DEFAULT_SS_DIR%" (
    set "SS_DIR=!DEFAULT_SS_DIR!"
    echo Using Starsector directory: !SS_DIR!
) else (
    if exist "G:\Starsector" (
        set "SS_DIR=G:\Starsector"
        echo Using Starsector directory: !SS_DIR!
    ) else (
        rem Neither path found; default to DEFAULT_SS_DIR and warn
        set "SS_DIR=!DEFAULT_SS_DIR!"
        echo WARNING: Could not find Starsector installation in !DEFAULT_SS_DIR! or G:\Starsector
        echo Please edit build.bat and set SS_DIR to your Starsector installation path.
    )
)
set MOD_NAME=Passive-Ramscoop
set MOD_ID=m561_ramscoop
set SRC_DIR=%cd%\src
set OUT_DIR=%cd%\jars
set OUT_CLASSES=%cd%\build\classes

:: Create directories if they don't exist
if not exist "%OUT_DIR%" mkdir "%OUT_DIR%"
if not exist "%OUT_CLASSES%" mkdir "%OUT_CLASSES%"

:: Set up classpath with all necessary libraries from Starsector
set CLASSPATH="%SS_DIR%\starsector-core\starfarer.api.jar"
set CLASSPATH=%CLASSPATH%;"%SS_DIR%\starsector-core\starfarer_obf.jar"
set CLASSPATH=%CLASSPATH%;"%SS_DIR%\starsector-core\janino.jar"
set CLASSPATH=%CLASSPATH%;"%SS_DIR%\starsector-core\commons-compiler.jar"
set CLASSPATH=%CLASSPATH%;"%SS_DIR%\starsector-core\commons-compiler-jdk.jar"
set CLASSPATH=%CLASSPATH%;"%SS_DIR%\starsector-core\fs.common_obf.jar"
set CLASSPATH=%CLASSPATH%;"%SS_DIR%\starsector-core\fs.sound_obf.jar"
set CLASSPATH=%CLASSPATH%;"%SS_DIR%\starsector-core\lwjgl.jar"
set CLASSPATH=%CLASSPATH%;"%SS_DIR%\starsector-core\lwjgl_util.jar"
set CLASSPATH=%CLASSPATH%;"%SS_DIR%\starsector-core\json.jar"
set CLASSPATH=%CLASSPATH%;"%SS_DIR%\starsector-core\log4j-1.2.9.jar"

:: Include any prebuilt classes (for example the lunalib stubs under build\classes)
set CLASSPATH=%CLASSPATH%;"%cd%\build\classes"

:: Compile Java files
echo Compiling Java files...
javac --release 8 -cp %CLASSPATH% -d "%OUT_CLASSES%" %SRC_DIR%\ramscoop\*.java
if %ERRORLEVEL% NEQ 0 (
    echo Compilation failed!
    exit /b %ERRORLEVEL%
)

:: Create JAR file
echo Creating JAR file...
cd "%SRC_DIR%"

:: Find Java installation to use its jar command
set JAVA_HOME=""
for /f "tokens=*" %%i in ('where java') do (
    set JAVA_PATH=%%i
    set JAVA_BIN=%%~dpi
)

:: Use jar from Java installation or fall back to jar command if in PATH
cd "%OUT_CLASSES%"
set "JAR_EXE=%JAVA_BIN%jar.exe"
if exist "%JAR_EXE%" (
    echo Using jar from: "%JAR_EXE%"
    "%JAR_EXE%" cf "%OUT_DIR%\Ramscoop.jar" ramscoop\*.class
) else (
    echo Attempting to use system jar command...
    jar cf "%OUT_DIR%\Ramscoop.jar" ramscoop\*.class
)

if %ERRORLEVEL% NEQ 0 (
    echo JAR creation failed!
    exit /b %ERRORLEVEL%
)

echo Build completed successfully!
echo JAR file created at: %OUT_DIR%\Ramscoop.jar
