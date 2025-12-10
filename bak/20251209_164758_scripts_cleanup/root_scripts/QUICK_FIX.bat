@echo off
title IOE-DREAM Quick Fix
color 0C

echo ========================================
echo IOE-DREAM Maven Quick Fix
echo ========================================
echo Problem: java.lang.ClassNotFoundException: #
echo Root Cause: BOM characters in XML files + GBK encoding
echo Solution: Use system-compatible approach
echo.

echo Step 1: Create Maven settings file...
if not exist "%USERPROFILE%\.m2" mkdir "%USERPROFILE%\.m2"

echo Creating settings.xml with GBK compatibility...
(
echo ^<?xml version="1.0" encoding="GBK"?^>
echo ^<settings^>
echo     ^<profiles^>
echo         ^<profile^>
echo             ^<id^>gbk^</id^>
echo             ^<properties^>
echo                 ^<project.build.sourceEncoding^>GBK^</project.build.sourceEncoding^>
echo                 ^<project.reporting.outputEncoding^>GBK^</project.reporting.outputEncoding^>
echo                 ^<maven.compiler.encoding^>GBK^</maven.compiler.encoding^>
echo             ^</properties^>
echo         ^</profile^>
echo     ^</profiles^>
echo     ^<activeProfiles^>
echo         ^<activeProfile^>gbk^</activeProfile^>
echo     ^</activeProfiles^>
echo ^</settings^>
) > "%USERPROFILE%\.m2\settings.xml"

echo Step 2: Set UTF-8 environment...
set JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8
set MAVEN_OPTS=-Xmx1024m -Dfile.encoding=UTF-8
set MAVEN_HOME=D:\IOE-DREAM\apache-maven-3.9.11
set PATH=%MAVEN_HOME%\bin;%PATH%

echo Step 3: Test Maven...
cd microservices

echo Testing Maven with GBK settings...
"%MAVEN_HOME%\bin\mvn.cmd" help:version
if errorlevel 1 (
    echo ERROR: Maven still failing
    echo.
    echo FINAL SOLUTION - Manual Commands:
    echo 1. Open Command Prompt as Administrator
    echo 2. Run these commands exactly:
    echo.
    echo cd /d "D:\IOE-DREAM\microservices"
    echo set JAVA_TOOL_OPTIONS=-Dfile.encoding=GBK
    echo set MAVEN_OPTS=-Xmx1024m -Dfile.encoding=GBK
    echo cd microservices-common
    echo mvn install -DskipTests
    echo cd ..
    echo mvn clean package -DskipTests
    echo.
) else (
    echo SUCCESS: Maven working with GBK settings
    echo.
    echo Step 4: Build project...
    echo Building common module...
    cd microservices-common
    "%MAVEN_HOME%\bin\mvn.cmd" install -DskipTests
    if not errorlevel 1 (
        echo Building all modules...
        cd ..
        "%MAVEN_HOME%\bin\mvn.cmd" clean package -DskipTests
        if not errorlevel 1 (
            echo BUILD SUCCESSFUL!
        )
    )
)

cd ..

echo.
echo ========================================
echo QUICK FIX COMPLETE
echo ========================================
echo.
echo If Maven still fails, please:
echo 1. Use the manual commands shown above
echo 2. Or use IDE (IntelliJ IDEA) to build
echo 3. The issue is system-level encoding conflict
echo.

pause