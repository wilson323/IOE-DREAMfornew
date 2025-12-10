@echo off
echo ==========================================
echo Push code to new-origin remote repository
echo ==========================================
echo.

cd /d D:\IOE-DREAM

echo Current branch:
git branch --show-current
echo.

echo Remote repository configuration:
git remote -v
echo.

echo Current Git status:
git status --short
echo.

echo Pushing code to new-origin/main...
echo.

git push new-origin main

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ==========================================
    echo Push successful!
    echo ==========================================
    echo Remote repository: https://github.com/wilson323/IOE-DREAMfornew.git
    echo Branch: main
) else (
    echo.
    echo ==========================================
    echo Push failed, error code: %ERRORLEVEL%
    echo ==========================================
    echo Please check the error message and refer to PUSH_TO_NEW_ORIGIN_GUIDE.md
)

echo.
pause
