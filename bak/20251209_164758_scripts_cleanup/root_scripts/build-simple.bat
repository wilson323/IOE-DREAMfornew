@echo off
title IOE-DREAM Builder
color 0A

echo ========================================
echo IOE-DREAM Microservices Builder
echo ========================================
echo Time: %date% %time%
echo.

echo Step 1: Environment Check...
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java not found. Please install JDK 17+
    pause
    exit /b 1
)
echo OK: Java found

where mvn >nul 2>&1
if errorlevel 1 (
    echo ERROR: Maven not found. Please install Maven 3.6+
    pause
    exit /b 1
)
echo OK: Maven found

echo.
echo Step 2: Project Check...
if not exist "microservices\pom.xml" (
    echo ERROR: Backend project not found
    pause
    exit /b 1
)
echo OK: Backend project found

if not exist "microservices\microservices-common\pom.xml" (
    echo ERROR: Common module not found
    pause
    exit /b 1
)
echo OK: Common module found

echo.
echo Step 3: Setting Environment...
set MAVEN_OPTS=-Xmx1024m -Dfile.encoding=UTF-8
set JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8
echo Environment variables set

echo.
echo Step 4: Building...

cd microservices

echo Cleaning previous builds...
call mvn clean -q
if errorlevel 1 (
    echo ERROR: Clean failed
    cd ..
    pause
    exit /b 1
)

echo Building common module first...
cd microservices-common
call mvn install -DskipTests -q
if errorlevel 1 (
    echo ERROR: Common module build failed
    cd ..\..
    pause
    exit /b 1
)
echo Common module built successfully
cd ..

echo Building all microservices...
call mvn compile -DskipTests -q
if errorlevel 1 (
    echo ERROR: Compilation failed
    cd ..
    pause
    exit /b 1
)

echo Packaging applications...
call mvn package -DskipTests -q
if errorlevel 1 (
    echo ERROR: Packaging failed
    cd ..
    pause
    exit /b 1
)

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
echo Next: Run start-services.bat to start services
echo.

pause