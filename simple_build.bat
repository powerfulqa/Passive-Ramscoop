@echo off
setlocal

REM Set paths
set JAVA_HOME=C:\Program Files\Java\jdk-23
set PATH=%JAVA_HOME%\bin;%PATH%
set STARSECTOR_PATH=C:\Program Files (x86)\Fractal Softworks\Starsector

echo Using Java at: %JAVA_HOME%

REM Create directories
if not exist build mkdir build
if not exist build\classes mkdir build\classes
if not exist jars mkdir jars

REM Check Java
javac -version
if %ERRORLEVEL% neq 0 (
    echo Java compiler not found!
    goto :EOF
)

REM Build classpath
set CP=.
for %%f in ("%STARSECTOR_PATH%\starsector-core\*.jar") do (
    set CP=!CP!;%%f
)

REM Compile
echo Compiling Java files...
if exist src\ramscoop\*.java (
    REM Use --release 8 for Java 8 compatibility with JDK 23
    javac --release 8 -cp "%CP%" -d build\classes src\ramscoop\*.java
    if %ERRORLEVEL% neq 0 (
        echo Compilation failed!
        echo Note: This is expected if you haven't yet set up the Starsector API JARs or
        echo if the source files are just placeholders.
        goto :EOF
    )
) else (
    echo No Java source files found in src\ramscoop\
)

REM Create JAR
echo Creating JAR file...
if exist build\classes\ramscoop\*.class (
    cd build\classes
    jar cf ..\Ramscoop.jar ramscoop\*.class
    cd ..\..
    copy /y build\Ramscoop.jar jars\Ramscoop.jar
    echo JAR file created and copied to jars directory.
) else (
    echo No class files found to create JAR.
)

echo Build process completed!
endlocal
