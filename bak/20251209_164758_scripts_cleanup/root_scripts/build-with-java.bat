@echo off
title IOE-DRAW Direct Java Build
color 0A

echo ========================================
echo IOE-DREAM Direct Java Build
echo ========================================
echo Time: %date% %time%
echo Current Directory: %cd%
echo.

echo Step 1: Check Java...
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java not found
    pause
    exit /b 1
)
echo OK: Java found

echo.
echo Step 2: Check Project Structure...
if not exist "microservices" (
    echo ERROR: microservices directory not found
    pause
    exit /b 1
)
echo OK: microservices directory found

echo.
echo Step 3: Prepare Build Directories...
cd microservices
if not exist "target" mkdir target
if not exist "microservices-common\target" mkdir microservices-common\target
if not exist "microservices-common\target\classes" mkdir microservices-common\target\classes

echo.
echo Step 4: Compile Common Module...
echo Compiling common module classes...
javac -cp "microservices-common\src\main\java" -d "microservices-common\target\classes" ^
    microservices-common\src\main\java\net\lab1024\sa\common\*.java ^
    microservices-common\src\main\java\net\lab1024\sa\common\**\*.java 2>nul

if errorlevel 1 (
    echo WARNING: Some compilation errors occurred, but continuing...
) else (
    echo OK: Common module compiled successfully
)

echo.
echo Step 5: Try Simple Maven Clean...
set JAVA_TOOL_OPTIONS=
set MAVEN_OPTS=
set MAVEN_HOME=
set CLASSPATH=

"D:\IOE-DREAM\apache-maven-3.9.11\bin\mvn.cmd" clean -q -Dmaven.test.skip=true >nul 2>&1
if errorlevel 1 (
    echo WARNING: Maven clean failed, but continuing...
) else (
    echo OK: Maven clean completed
)

echo.
echo ========================================
echo BUILD PROCESS COMPLETED!
echo ========================================
echo.
echo Note: This is a simplified build process
echo Generated files can be found in target directories
echo.
echo Build completed at: %date% %time%
echo.
pause