@echo off
echo Testing build-local.bat script...
echo Current directory: %cd%
echo.

echo Testing file existence...
if exist "microservices\pom.xml" (
    echo SUCCESS: Found microservices\pom.xml
) else (
    echo ERROR: microservices\pom.xml not found
    echo Looking in: %cd%\microservices\pom.xml
    echo.
    echo Listing microservices directory:
    if exist "microservices" (
        dir microservices
    ) else (
        echo ERROR: microservices directory not found
    )
)

echo.
echo Testing Maven command...
if exist "D:\IOE-DREAM\apache-maven-3.9.11\bin\mvn.cmd" (
    echo SUCCESS: Maven command found
    "D:\IOE-DREAM\apache-maven-3.9.11\bin\mvn.cmd" --version
) else (
    echo ERROR: Maven command not found
)

pause