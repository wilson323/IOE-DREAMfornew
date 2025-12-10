@echo off
title XML Declaration Fix
color 0C

echo ========================================
echo XML Declaration Fix Tool
echo ========================================
echo Problem: All pom.xml files missing <?xml in first line
echo Solution: Add <?xml to beginning of all pom.xml files
echo.

cd microservices

echo Fixing all pom.xml files...
echo.

for /r %%f in (pom.xml) do (
    echo Processing: %%f
    powershell -Command "$content = Get-Content '%%f' -Raw; if ($content.StartsWith('ml version=')) { Set-Content '%%f' -Value ('<?xml ' + $content) -Force }"
)

echo.
echo Verification:
echo Checking all pom.xml files...
for /r %%f in (pom.xml) do (
    echo %%f:
    powershell -Command "Get-Content '%%f' -TotalCount 1"
)

echo.
echo ========================================
echo XML FIX COMPLETE
echo ========================================
echo.
echo Now try running: build.bat or build.ps1
echo.

pause