@echo off
title IOE-DREAM Service Stopper
color 0C

echo ========================================
echo IOE-DREAM Service Stopper
echo ========================================
echo Time: %date% %time%
echo.

echo Stopping all IOE-DREAM services...

echo.
echo Step 1: Stopping Backend Services...
tasklist | findstr "java.exe" >nul
if errorlevel 1 (
    echo No Java processes found
) else (
    echo Found Java processes, stopping...
    taskkill /F /IM java.exe >nul 2>&1
    if errorlevel 1 (
        echo Some backend services may still be running
    ) else (
        echo Backend services stopped successfully
    )
)

echo.
echo Step 2: Stopping Frontend Services...
tasklist | findstr "node.exe" >nul
if errorlevel 1 (
    echo No Node.js processes found
) else (
    echo Found Node.js processes, stopping...
    taskkill /F /IM node.exe >nul 2>&1
    if errorlevel 1 (
        echo Some frontend services may still be running
    ) else (
        echo Frontend services stopped successfully
    )
)

echo.
echo Step 3: Checking Specific Ports...
for %%p in (3000 8081 8080 8087 8088 8089 8090 8091 8092 8094 8095) do (
    echo Checking port %%p...
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%%p" ^| findstr "LISTENING" 2^>nul') do (
        if not "%%a"=="" (
            echo Stopping process %%a on port %%p
            taskkill /F /PID %%a >nul 2>&1
        )
    )
)

echo.
echo Step 4: Cleaning Temporary Files...
if exist "%TEMP%\ioe-dream-*" (
    del /q "%TEMP%\ioe-dream-*" >nul 2>&1
    echo Temporary files cleaned
)

echo.
echo ========================================
echo ALL SERVICES STOPPED
echo ========================================
echo.

echo Status Check:
echo Java Processes:
tasklist | findstr "java.exe" >nul 2>&1
if errorlevel 1 (
    echo   Status: NONE
) else (
    echo   Status: STILL RUNNING
    tasklist | findstr "java.exe"
)

echo Node.js Processes:
tasklist | findstr "node.exe" >nul 2>&1
if errorlevel 1 (
    echo   Status: NONE
) else (
    echo   Status: STILL RUNNING
    tasklist | findstr "node.exe"
)

echo.
echo Next: Run start-simple.bat to restart services
echo.

pause