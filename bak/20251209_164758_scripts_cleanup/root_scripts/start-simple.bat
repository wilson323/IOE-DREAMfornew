@echo off
title IOE-DREAM Launcher
color 0E

echo ========================================
echo IOE-DREAM Project Launcher
echo ========================================
echo Time: %date% %time%
echo.

echo Step 1: Environment Check...
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java not found. Please install JDK 17+
    pause
    exit /b 1
)
echo OK: Java Environment

node --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Node.js not found. Please install Node.js 16+
    pause
    exit /b 1
)
echo OK: Node.js Environment

echo.
echo Step 2: Project Check...
if not exist "microservices\pom.xml" (
    echo ERROR: Backend project not found
    pause
    exit /b 1
)
if not exist "smart-admin-web-javascript\package.json" (
    echo ERROR: Frontend project not found
    pause
    exit /b 1
)
echo OK: Project structure valid

echo.
echo ========================================
echo LAUNCH OPTIONS
echo ========================================
echo.
echo 1. Build Backend Only
echo 2. Start Frontend Only
echo 3. Start Backend Only
echo 4. Start Full Project (Recommended)
echo 5. Stop All Services
echo 6. Check Status
echo.
set /p choice=Select option (1-6):

if "%choice%"=="1" goto BUILD_BACKEND
if "%choice%"=="2" goto START_FRONTEND
if "%choice%"=="3" goto START_BACKEND
if "%choice%"=="4" goto START_ALL
if "%choice%"=="5" goto STOP_ALL
if "%choice%"=="6" goto CHECK_STATUS

echo Invalid choice, starting full project
goto START_ALL

:BUILD_BACKEND
echo.
echo Building Backend...
call build-simple.bat
goto END

:START_FRONTEND
echo.
echo Starting Frontend...
cd smart-admin-web-javascript
if not exist "node_modules" (
    echo Installing frontend dependencies...
    call npm install --registry=https://registry.npmmirror.com
)
echo Starting frontend server...
start "Frontend Server" cmd /k "title Frontend - 3000 && npm run dev"
echo Frontend started at: http://localhost:3000
goto END

:START_BACKEND
echo.
echo Starting Backend...
cd microservices
echo Checking if backend is built...
set /p compiled=Is backend compiled? (Y/N):
if /i not "%compiled%"=="Y" (
    echo Building backend first...
    call ..\build-simple.bat
    if errorlevel 1 (
        echo Backend build failed
        pause
        exit /b 1
    )
)
echo Starting microservices...
start "Backend Services" cmd /k "title Backend Services && call start-services.bat"
echo Backend services starting...
goto END

:START_ALL
echo.
echo Starting Full Project...

echo Step 1: Backend Preparation...
cd microservices
set /p compiled=Is backend compiled? (Y/N):
if /i not "%compiled%"=="Y" (
    echo Building backend...
    call ..\build-simple.bat
    if errorlevel 1 (
        echo Backend build failed
        pause
        exit /b 1
    )
)

echo Step 2: Starting Backend Services...
start "Backend Services" cmd /k "title Backend Services && call start-services.bat"

echo Step 3: Starting Frontend...
cd ..\smart-admin-web-javascript
if not exist "node_modules" (
    echo Installing frontend dependencies...
    call npm install --registry=https://registry.npmmirror.com
)
start "Frontend Server" cmd /k "title Frontend - 3000 && npm run dev"

echo Step 4: Starting Mobile (if exists)...
cd ..\
if exist "smart-app\src\manifest.json" (
    cd smart-app
    if not exist "node_modules" (
        echo Installing mobile dependencies...
        call npm install --registry=https://registry.npmmirror.com
    )
    start "Mobile Server" cmd /k "title Mobile - 8081 && npm run dev:h5"
    cd ..
)

echo.
echo ========================================
echo FULL PROJECT STARTED!
echo ========================================
echo.
echo Service URLs:
echo   - Frontend: http://localhost:3000
echo   - Mobile: http://localhost:8081 (if available)
echo   - Backend Gateway: http://localhost:8080
echo   - API Docs: http://localhost:8080/doc.html
echo.
echo Login: admin / 123456
echo.
echo Waiting for services to start (30 seconds)...
timeout /t 30 /nobreak >nul
goto END

:STOP_ALL
echo.
echo Stopping All Services...
echo Stopping Java processes...
taskkill /F /IM java.exe >nul 2>&1
echo Stopping Node.js processes...
taskkill /F /IM node.exe >nul 2>&1
echo All services stopped
goto END

:CHECK_STATUS
echo.
echo Checking Service Status...
echo.
echo Frontend (3000):
netstat -an | findstr ":3000" >nul
if errorlevel 1 (
    echo   Status: STOPPED
) else (
    echo   Status: RUNNING
)

echo Backend Gateway (8080):
netstat -an | findstr ":8080" >nul
if errorlevel 1 (
    echo   Status: STOPPED
) else (
    echo   Status: RUNNING
)

echo Mobile (8081):
netstat -an | findstr ":8081" >nul
if errorlevel 1 (
    echo   Status: STOPPED
) else (
    echo   Status: RUNNING
)

echo.
echo Java Processes:
tasklist | findstr "java.exe" >nul 2>&1
if errorlevel 1 (
    echo   Status: NONE
) else (
    echo   Status: RUNNING
    tasklist | findstr "java.exe"
)

echo Node.js Processes:
tasklist | findstr "node.exe" >nul 2>&1
if errorlevel 1 (
    echo   Status: NONE
) else (
    echo   Status: RUNNING
    tasklist | findstr "node.exe"
)
goto END

:END
echo.
echo ========================================
echo OPERATION COMPLETE
echo ========================================
echo.
echo Quick Commands:
echo   - Build: build-simple.bat
echo   - Start: start-simple.bat
echo   - Stop: start-simple.bat (option 5)
echo.

pause