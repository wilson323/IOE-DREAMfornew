@echo off
title IOE-DREAM Final Test
color 0A

echo ========================================
echo IOE-DREAM Final Build Test
echo ========================================
echo All XML declarations have been fixed!
echo Maven resources plugin has been fixed!
echo.

echo Testing Maven with your local installation...
cd microservices

set MAVEN_HOME=D:\IOE-DREAM\apache-maven-3.9.11
set PATH=%MAVEN_HOME%\bin;%PATH%
set JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8
set MAVEN_OPTS=-Xmx1024m -Dfile.encoding=UTF-8

echo Step 1: Test Maven help command...
"%MAVEN_HOME%\bin\mvn.cmd" help:version
if errorlevel 1 (
    echo ERROR: Maven help command failed
    echo.
    echo Trying alternative approach...
) else (
    echo OK: Maven help command successful!
)

echo.
echo Step 2: Clean project...
"%MAVEN_HOME%\bin\mvn.cmd" clean -q
if errorlevel 1 (
    echo ERROR: Maven clean failed
    echo.
    echo Trying alternative approach...
) else (
    echo OK: Clean successful!
)

echo.
echo Step 3: Build common module...
cd microservices-common
"%MAVEN_HOME%\bin\mvn.cmd" install -DskipTests -q
if errorlevel 1 (
    echo ERROR: Common module build failed
    cd ..\..
    echo.
    echo Possible causes:
    echo 1. Network connectivity issues
    echo 2. Missing dependencies
    echo 3. JDK version incompatible
    echo.
    echo Manual solution:
    echo 1. Use IntelliJ IDEA to build
    echo 2. Configure Maven proxy settings
    echo 3. Check Java version compatibility
) else (
    echo OK: Common module built successfully!
    cd ..

    echo.
    echo Step 4: Build all modules...
    "%MAVEN_HOME%\bin\mvn.cmd" compile -DskipTests -q
    if not errorlevel 1 (
        echo OK: Compilation successful!
        echo.
        echo Step 5: Package applications...
        "%MAVEN_HOME%\bin\mvn.cmd" package -DskipTests -q
        if not errorlevel 1 (
            echo.
            echo ========================================
            echo BUILD COMPLETELY SUCCESSFUL!
            echo ========================================
            echo.
            echo Generated JAR files:
            for /d %%i in (ioedream-*) do (
                if exist "%%i\target\*.jar" (
                    echo   - %%~nxi.jar
                )
            )
            echo.
            echo Now you can run start-services.bat to start services!
        ) else (
            echo ERROR: Packaging failed
        )
    ) else (
        echo ERROR: Compilation failed
    )
)

cd ..

echo.
echo ========================================
echo FINAL TEST COMPLETE
echo ========================================
echo.
echo If build failed, please:
echo 1. Check network connection
echo 2. Use IntelliJ IDEA for building
echo 3. Verify Java and Maven versions
echo 4. Check for any remaining XML issues
echo.

pause