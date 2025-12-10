@echo off
title IOE-DREAM Maven BOM Fix
color 0C

echo ========================================
echo IOE-DREAM Maven BOM Fix Tool
echo ========================================
echo Time: %date% %time%
echo.

echo PROBLEM IDENTIFIED:
echo Maven XML files contain BOM characters
echo This causes: java.lang.ClassNotFoundException: #
echo.

echo Step 1: Fixing all pom.xml files...
cd microservices

echo Removing BOM characters from all pom.xml files...
for /r %%f in (pom.xml) do (
    echo Processing: %%f
    powershell -Command "$content = Get-Content '%%f' -Raw; $content = $content -replace '^[^\w<]', ''; Set-Content '%%f' -Value $content -Encoding UTF8"
)

echo Step 2: Verifying fixes...
echo Checking main pom.xml...
powershell -Command "$bytes = [System.IO.File]::ReadAllBytes('pom.xml'); if ($bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) { Write-Host 'BOM still found in main pom.xml' -ForegroundColor Red } else { Write-Host 'BOM removed from main pom.xml' -ForegroundColor Green }"

echo.
echo Step 3: Testing Maven...
echo Testing Maven clean command...
call mvn clean -q
if errorlevel 1 (
    echo ERROR: Maven still failing
    echo.
    echo ALTERNATIVE MANUAL SOLUTION:
    echo 1. Use a text editor that supports UTF-8 without BOM
    echo 2. Save all pom.xml files without BOM
    echo 3. Or use the following manual commands:
    echo.
    echo cd microservices\microservices-common
    echo mvn clean install -DskipTests
    echo cd ..
    echo mvn clean package -DskipTests
    echo.
) else (
    echo SUCCESS: Maven clean command working!
    echo.
    echo Step 4: Building project...
    call mvn compile -DskipTests -q
    if not errorlevel 1 (
        echo SUCCESS: Maven compilation working!
        call mvn package -DskipTests -q
        if not errorlevel 1 (
            echo SUCCESS: All Maven operations working!
            echo.
            echo You can now use build.ps1 or build-simple.bat
        )
    )
)

cd ..

echo.
echo ========================================
echo BOM FIX COMPLETE
echo ========================================
echo.

echo If Maven still fails, please:
echo 1. Check for any remaining BOM characters in XML files
echo 2. Verify Java and Maven installations
echo 3. Use manual compilation commands shown above
echo.

pause