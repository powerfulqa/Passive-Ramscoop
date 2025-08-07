@echo off
setlocal enabledelayedexpansion

REM Set paths
set JAVA_HOME=C:\Program Files\Java\jdk-23
set PATH=%JAVA_HOME%\bin;%PATH%
set STARSECTOR_PATH=C:\Program Files (x86)\Fractal Softworks\Starsector

REM Set mod information
set MOD_NAME=Ramscoop
set MOD_VERSION=0.3
set MOD_PACKAGE=ramscoop

echo Using Java at: %JAVA_HOME%

REM Create directories
if not exist build mkdir build
if not exist build\classes mkdir build\classes
if not exist jars mkdir jars

REM Check if Java is available
javac -version
if %ERRORLEVEL% neq 0 (
    echo ERROR: Java compiler not found!
    echo Please install Java JDK or update the JAVA_HOME variable in this script.
    exit /b 1
)

REM Build classpath with all Starsector core jars
echo Building classpath...
set CP=.
for %%f in ("%STARSECTOR_PATH%\starsector-core\*.jar") do (
    set CP=!CP!;%%f
)

REM Compile Java files
echo Compiling Java files...
if exist src\%MOD_PACKAGE%\*.java (
    javac --release 8 -Xlint:deprecation -Xlint:unchecked -cp "!CP!" -d build\classes src\%MOD_PACKAGE%\*.java
    if %ERRORLEVEL% neq 0 (
        echo Compilation failed!
        echo Note: This is expected if you haven't yet extracted the Starsector API
        echo or if the source files are just placeholders.
        exit /b 1
    )
) else (
    echo No Java source files found in src\%MOD_PACKAGE%\
)

REM Create JAR file
echo Creating JAR file...
cd build\classes
if exist %MOD_PACKAGE%\*.class (
    jar cf "..\%MOD_NAME%.jar" %MOD_PACKAGE%\*.class
    cd ..\..
    copy /y "build\%MOD_NAME%.jar" "jars\%MOD_NAME%.jar"
    echo JAR file created and copied to jars directory.
) else (
    cd ..\..
    echo No class files found to create JAR.
)

REM Create release zip if specified
if "%1"=="release" (
    echo Creating release package...
    set RELEASE_DIR=release\%MOD_NAME%-%MOD_VERSION%
    if exist release rmdir /s /q release
    mkdir release
    mkdir %RELEASE_DIR%
    mkdir %RELEASE_DIR%\jars
    mkdir %RELEASE_DIR%\data
    
    REM Copy mod files to release directory
    copy mod_info.json %RELEASE_DIR%\
    copy README.md %RELEASE_DIR%\
    copy LICENSE %RELEASE_DIR%\
    copy jars\%MOD_NAME%.jar %RELEASE_DIR%\jars\
    
    REM Copy data directory if it exists
    if exist data xcopy /E /I data %RELEASE_DIR%\data
    
    REM Copy graphics directory if it exists
    if exist graphics xcopy /E /I graphics %RELEASE_DIR%\graphics
    
    REM Create zip file
    powershell Compress-Archive -Path release\%MOD_NAME%-%MOD_VERSION% -DestinationPath release\%MOD_NAME%-%MOD_VERSION%.zip -Force
    
    echo Release package created at release\%MOD_NAME%-%MOD_VERSION%.zip
)

echo Build process completed successfully!
endlocal
