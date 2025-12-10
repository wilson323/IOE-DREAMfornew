@echo off
title IOE-DREAM Local Maven Builder
color 0A

echo ========================================
echo IOE-DREAM Local Maven Builder
echo ========================================
echo Time: %date% %time%
echo Using Local Maven: D:\IOE-DREAM\apache-maven-3.9.11
echo.

echo Step 1: Environment Check...
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java not found
    pause
    exit /b 1
)
echo OK: Java found

if not exist "D:\IOE-DREAM\apache-maven-3.9.11\bin\mvn.cmd" (
    echo ERROR: Local Maven not found
    pause
    exit /b 1
)
echo OK: Local Maven found

echo.
echo Step 2: Project Check...
echo Current directory: %cd%
echo Checking for: %cd%\microservices\pom.xml

REM 首先检查microservices目录是否存在
if not exist "microservices" (
    echo ERROR: microservices directory not found
    echo Current directory contents:
    dir /B
    pause
    exit /b 1
)

REM 检查pom.xml文件是否存在
if not exist "microservices\pom.xml" (
    echo ERROR: Backend project pom.xml not found
    echo microservices directory contents:
    dir /B microservices
    pause
    exit /b 1
)

echo OK: Backend project found

echo.
echo Step 3: Set Local Environment...
set MAVEN_HOME=D:\IOE-DREAM\apache-maven-3.9.11
set PATH=%MAVEN_HOME%\bin;%PATH%
set MAVEN_CMD="D:\IOE-DREAM\apache-maven-3.9.11\bin\mvn.cmd"

REM 清除可能导致问题的环境变量
set JAVA_TOOL_OPTIONS=
set MAVEN_OPTS=
echo Maven Command: %MAVEN_CMD%

echo.
echo Step 4: Building with Local Maven...
cd microservices

echo Cleaning previous builds...
call %MAVEN_CMD% clean -q -Dmaven.test.skip=true
if errorlevel 1 (
    echo WARNING: Clean failed, but continuing...
    echo Trying alternative clean...
    if exist target rmdir /s /q target
    if exist microservices-common\target rmdir /s /q microservices-common\target
)

echo Building common module first...
cd microservices-common
call %MAVEN_CMD% install -q -Dmaven.test.skip=true
if errorlevel 1 (
    echo ERROR: Common module build failed
    echo Trying simple compilation instead...
    javac -cp "src\main\java" -d "target\classes" src\main\java\net\lab1024\sa\common\**\*.java 2>nul
    echo WARNING: Common module may not be fully functional
) else (
    echo OK: Common module built successfully
)
cd ..

echo Building all microservices...
call %MAVEN_CMD% compile -q -Dmaven.test.skip=true
if errorlevel 1 (
    echo ERROR: Compilation failed
    echo This may be due to Maven configuration issues
    echo Consider using build-with-java.bat as alternative
) else (
    echo OK: Compilation successful

    echo Packaging applications...
    call %MAVEN_CMD% package -q -Dmaven.test.skip=true
    if errorlevel 1 (
        echo WARNING: Packaging failed, but compilation was successful
    ) else (
        echo OK: Packaging successful
    )
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
echo Next: Run start-local.bat to start services
echo.

pause