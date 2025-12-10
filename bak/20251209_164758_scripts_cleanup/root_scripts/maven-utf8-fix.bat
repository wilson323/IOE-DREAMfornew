@echo off
title IOE-DREAM Maven UTF-8 Fix
color 0C

echo ========================================
echo IOE-DREAM Maven UTF-8 Fix
echo ========================================
echo Time: %date% %time%
echo.

echo PROBLEM IDENTIFIED:
echo Platform encoding: GBK
echo Required encoding: UTF-8
echo.

echo Step 1: Setting UTF-8 Environment Variables...
set JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8 -Duser.language=en -Duser.country=US
set MAVEN_OPTS=-Xmx1024m -Dfile.encoding=UTF-8
set JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.17.10-hotspot
set MAVEN_HOME=D:\IOE-DREAM\apache-maven-3.9.11
set PATH=%MAVEN_HOME%\bin;%PATH%

echo JAVA_TOOL_OPTIONS: %JAVA_TOOL_OPTIONS%
echo MAVEN_OPTS: %MAVEN_OPTS%
echo.

echo Step 2: Testing Maven with UTF-8...
cd microservices

echo Testing Maven help command...
call mvn -version -Duser.language=en -Duser.country=US
echo.

echo Testing effective POM...
call mvn help:effective-pom -Duser.language=en -Duser.country=US -q >nul 2>&1
if errorlevel 1 (
    echo ERROR: Maven still failing with UTF-8 settings
) else (
    echo OK: Maven working with UTF-8 settings
)

echo.
echo Step 3: Attempting Clean Build...
echo Cleaning with UTF-8 settings...
call mvn clean -Duser.language=en -Duser.country=US -q
if errorlevel 1 (
    echo ERROR: Clean still failing
    echo.
    echo ALTERNATIVE SOLUTION:
    echo 1. Create %%USERPROFILE%%\.m2\settings.xml with UTF-8 encoding
    echo 2. Set system locale to English
    echo 3. Use manual compilation commands
) else (
    echo OK: Clean successful with UTF-8 settings
)

cd ..

echo.
echo ========================================
echo UTF-8 FIX ATTEMPTED
echo ========================================
echo.

echo If Maven still fails, use these manual commands:
echo.
echo cd microservices
echo set JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8 -Duser.language=en -Duser.country=US
echo cd microservices-common
echo mvn clean install -DskipTests -Duser.language=en -Duser.country=US
echo cd ..
echo mvn clean package -DskipTests -Duser.language=en -Duser.country=US
echo.

echo Press any key to exit...
pause