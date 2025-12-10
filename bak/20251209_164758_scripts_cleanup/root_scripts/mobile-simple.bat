@echo off
title IOE-DREAM Mobile Builder
color 0D

echo ========================================
echo IOE-DREAM Mobile Builder
echo ========================================
echo Time: %date% %time%
echo.

echo Step 1: Environment Check...
node --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Node.js not found. Please install Node.js 16+
    pause
    exit /b 1
)
echo OK: Node.js Environment

if not exist "smart-app\src\manifest.json" (
    echo ERROR: Mobile project not found
    pause
    exit /b 1
)
echo OK: Mobile project found

cd smart-app
echo Current directory: %CD%

echo.
echo ========================================
echo BUILD OPTIONS
echo ========================================
echo.
echo 1. Development Mode (H5)
echo 2. Production Build
echo 3. WeChat Mini Program
echo 4. Alipay Mini Program
echo 5. Install Dependencies
echo 6. Clean Project
echo.
set /p choice=Select option (1-6):

if "%choice%"=="1" goto DEV_H5
if "%choice%"=="2" goto BUILD_PROD
if "%choice%"=="3" goto BUILD_WECHAT
if "%choice%"=="4" goto BUILD_ALIPAY
if "%choice%"=="5" goto INSTALL_DEPS
if "%choice%"=="6" goto CLEAN_PROJECT

echo Invalid choice, defaulting to development mode
goto DEV_H5

:INSTALL_DEPS
echo.
echo Installing dependencies...
call npm install --registry=https://registry.npmmirror.com
if errorlevel 1 (
    echo ERROR: Dependency installation failed
    pause
    exit /b 1
)
echo Dependencies installed successfully
goto END

:CLEAN_PROJECT
echo.
echo Cleaning project...
if exist "node_modules" rmdir /s /q node_modules 2>nul
if exist "dist" rmdir /s /q dist 2>nul
if exist "unpackage" rmdir /s /q unpackage 2>nul
echo Project cleaned successfully
goto END

:DEV_H5
echo.
echo Starting H5 development mode...
if not exist "node_modules" (
    echo Installing dependencies first...
    call npm install --registry=https://registry.npmmirror.com
    if errorlevel 1 (
        echo ERROR: Dependency installation failed
        pause
        exit /b 1
    )
)
echo Starting H5 development server...
start "Mobile H5 Server" cmd /k "title Mobile H5 - 8081 && npm run dev:h5"
echo.
echo ========================================
echo H5 DEVELOPMENT SERVER STARTED!
echo ========================================
echo.
echo Server URL: http://localhost:8081
echo Hot reload: ENABLED
echo Debug tools: ENABLED
echo.
echo Usage:
echo - Code changes auto-refresh page
echo - Use browser dev tools for debugging
echo.
goto END

:BUILD_PROD
echo.
echo Building H5 production version...
if not exist "node_modules" (
    echo Installing dependencies...
    call npm install --registry=https://registry.npmmirror.com
)
call npm run build:h5
if errorlevel 1 (
    echo ERROR: Build failed
    pause
    exit /b 1
)
echo H5 production version built successfully
echo Output: dist\build\h5
goto END

:BUILD_WECHAT
echo.
echo Building WeChat mini program...
if not exist "node_modules" (
    echo Installing dependencies...
    call npm install --registry=https://registry.npmmirror.com
)
call npm run build:mp-weixin
if errorlevel 1 (
    echo ERROR: Build failed
    pause
    exit /b 1
)
echo WeChat mini program built successfully
echo Output: dist\build\mp-weixin
echo Import this directory into WeChat Developer Tools
goto END

:BUILD_ALIPAY
echo.
echo Building Alipay mini program...
if not exist "node_modules" (
    echo Installing dependencies...
    call npm install --registry=https://registry.npmmirror.com
)
call npm run build:mp-alipay
if errorlevel 1 (
    echo ERROR: Build failed
    pause
    exit /b 1
)
echo Alipay mini program built successfully
echo Output: dist\build\mp-alipay
echo Import this directory into Alipay Developer Tools
goto END

:END
echo.
echo ========================================
echo BUILD COMPLETE
echo ========================================
echo.
echo Documentation:
echo - UniApp: https://uniapp.dcloud.io/
echo - HBuilderX: https://www.dcloud.io/hbuilderx.html
echo.

pause