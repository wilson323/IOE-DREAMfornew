@echo off
title IOE-DREAM Maven Diagnostics
color 0B

echo ========================================
echo IOE-DREAM Maven Diagnostics
echo ========================================
echo Time: %date% %time%
echo.

echo Step 1: Java Environment Check...
java -version
echo.

echo Step 2: Maven Environment Check...
where mvn
mvn -version
echo.

echo Step 3: Environment Variables...
echo JAVA_HOME: %JAVA_HOME%
echo MAVEN_HOME: %MAVEN_HOME%
echo MAVEN_OPTS: %MAVEN_OPTS%
echo.

echo Step 4: Project Structure Check...
if exist "microservices\pom.xml" (
    echo OK: microservices\pom.xml found
) else (
    echo ERROR: microservices\pom.xml not found
)

if exist "microservices\microservices-common\pom.xml" (
    echo OK: microservices-common\pom.xml found
) else (
    echo ERROR: microservices-common\pom.xml not found
)
echo.

echo Step 5: Maven Repository Check...
if exist "%USERPROFILE%\.m2\repository" (
    echo Maven repository found at: %USERPROFILE%\.m2\repository
) else (
    echo Maven repository not found
)
echo.

echo Step 6: Maven Configuration Check...
if exist "%USERPROFILE%\.m2\settings.xml" (
    echo Maven settings.xml found
    echo Location: %USERPROFILE%\.m2\settings.xml
) else (
    echo No custom Maven settings.xml found, using defaults
)
echo.

echo Step 7: Testing Simple Maven Command...
cd microservices
echo Testing: mvn help:effective-pom
call mvn help:effective-pom -q >nul 2>&1
if errorlevel 1 (
    echo ERROR: Maven command failed
    echo Possible causes:
    echo   1. JAVA_HOME not set correctly
    echo   2. MAVEN_HOME not set correctly
    echo   3. Network connectivity issues
    echo   4. Maven repository corruption
) else (
    echo OK: Maven basic functionality working
)
cd ..

echo.
echo ========================================
echo DIAGNOSTICS COMPLETE
echo ========================================
echo.

echo Step 8: Recommendations...
echo.
echo If Maven commands are failing, try these solutions:
echo.
echo 1. Set Environment Variables:
echo    set JAVA_HOME=C:\Program Files\Java\jdk-17
echo    set MAVEN_HOME=C:\Program Files\Apache\Maven
echo    set PATH=%MAVEN_HOME%\bin;%PATH%
echo.
echo 2. Clean Maven Repository:
echo    rmdir /s /q "%USERPROFILE%\.m2\repository"
echo.
echo 3. Update Maven Configuration:
echo    Create %%USERPROFILE%%\.m2\settings.xml with UTF-8 encoding
echo.
echo 4. Use Build-Simple Script:
echo    run build-simple.bat (uses optimized settings)
echo.

echo Press any key to exit...
pause