@echo off
title IOE-DREAM Simple Maven Build
color 0A

echo ========================================
echo IOE-DREAM Simple Maven Build
echo ========================================
echo Time: %date% %time%
echo Current Directory: %cd%
echo.

echo Step 1: Check Environment...
if not exist "D:\IOE-DREAM\apache-maven-3.9.11\bin\mvn.cmd" (
    echo ERROR: Maven not found at D:\IOE-DREAM\apache-maven-3.9.11\bin\mvn.cmd
    pause
    exit /b 1
)
echo OK: Maven found

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
    echo Current contents:
    dir /B
    pause
    exit /b 1
)
echo OK: microservices directory found

if not exist "microservices\pom.xml" (
    echo ERROR: microservices\pom.xml not found
    echo microservices contents:
    dir /B microservices
    pause
    exit /b 1
)
echo OK: microservices\pom.xml found

echo.
echo Step 3: Clean Environment...
set JAVA_TOOL_OPTIONS=
set MAVEN_OPTS=
set MAVEN_HOME=D:\IOE-DREAM\apache-maven-3.9.11
set PATH=%MAVEN_HOME%\bin;%PATH%

echo.
echo Step 4: Build Project...
cd microservices

echo Cleaning previous builds...
call "D:\IOE-DREAM\apache-maven-3.9.11\bin\mvn.cmd" clean -q
if errorlevel 1 (
    echo ERROR: Clean failed
    cd ..
    pause
    exit /b 1
)
echo OK: Clean completed

echo Building common module...
cd microservices-common
call "D:\IOE-DREAM\apache-maven-3.9.11\bin\mvn.cmd" install -q -DskipTests
if errorlevel 1 (
    echo ERROR: Common module build failed
    cd ..\..
    pause
    exit /b 1
)
echo OK: Common module built successfully
cd ..

echo Building all modules...
call "D:\IOE-DREAM\apache-maven-3.9.11\bin\mvn.cmd" compile -q -DskipTests
if errorlevel 1 (
    echo ERROR: Compilation failed
    cd ..
    pause
    exit /b 1
)
echo OK: Compilation successful

call "D:\IOE-DREAM\apache-maven-3.9.11\bin\mvn.cmd" package -q -DskipTests
if errorlevel 1 (
    echo ERROR: Packaging failed
    cd ..
    pause
    exit /b 1
)
echo OK: Packaging successful

cd ..

echo.
echo ========================================
echo BUILD SUCCESSFUL!
echo ========================================
echo.

echo Generated JAR files:
for /d %%i in (microservices\ioedream-*) do (
    if exist "%%i\target\*.jar" (
        echo   - %%~nxi.jar
    )
)

echo.
echo Build completed at: %date% %time%
echo.
pause