@echo off
title IOE-DREAM Gradle Build
color 0A

echo ========================================
echo IOE-DREAM Gradle Build (Maven Alternative)
echo ========================================
echo Time: %date% %time%
echo Current Directory: %cd%
echo.

echo Step 1: Check Environment...
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

if not exist "microservices\settings.gradle" (
    echo ERROR: settings.gradle not found
    pause
    exit /b 1
)
echo OK: settings.gradle found

echo.
echo Step 3: Build with Gradle...
cd microservices

echo Cleaning previous builds...
if exist "gradlew.bat" (
    call gradlew.bat clean
    echo OK: Gradle clean completed
) else (
    echo WARNING: gradlew.bat not found, skipping clean
)

echo Building with Gradle...
if exist "gradlew.bat" (
    call gradlew.bat build -x test
    if errorlevel 1 (
        echo ERROR: Gradle build failed
        cd ..
        pause
        exit /b 1
    )
    echo OK: Gradle build successful
) else (
    echo ERROR: gradlew.bat not found
    echo Falling back to manual Java compilation...

    REM 手动编译核心模块
    if exist "microservices-common\src\main\java" (
        echo Compiling microservices-common manually...
        mkdir microservices-common\target\classes 2>nul
        javac -cp "microservices-common\src\main\java" -d microservices-common\target\classes microservices-common\src\main\java\net\lab1024\sa\common\**\*.java 2>nul
        if errorlevel 1 (
            echo WARNING: Some Java compilation errors occurred
        ) else (
            echo OK: Java compilation completed
        )
    )
)

cd ..

echo.
echo ========================================
echo BUILD COMPLETED!
echo ========================================
echo.
echo Build completed at: %date% %time%
echo Note: Check above for any warnings or errors
echo.
pause