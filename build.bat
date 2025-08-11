@echo off
setlocal enabledelayedexpansion

:: Configuration
set SS_DIR=C:\Program Files (x86)\Fractal Softworks\Starsector
set MOD_NAME=Passive-Ramscoop
set MOD_ID=m561_ramscoop
set SRC_DIR=%cd%\src
set OUT_DIR=%cd%\Passive-Ramscoop\jars

:: Create directories if they don't exist
if not exist "%OUT_DIR%" mkdir "%OUT_DIR%"

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

:: Compile Java files
echo Compiling Java files...
javac --release 8 -cp %CLASSPATH% -d "%SRC_DIR%" "%SRC_DIR%\ramscoop\*.java"
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
if exist "%JAVA_BIN%jar.exe" (
    echo Using jar from: %JAVA_BIN%jar.exe
    "%JAVA_BIN%jar.exe" cf "%OUT_DIR%\Ramscoop.jar" ramscoop\*.class
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
