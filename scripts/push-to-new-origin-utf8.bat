@echo off
chcp 65001 >nul 2>&1
echo ==========================================
echo 推送代码到 new-origin 远程仓库
echo ==========================================
echo.

cd /d D:\IOE-DREAM

echo 当前分支:
git branch --show-current
echo.

echo 远程仓库配置:
git remote -v
echo.

echo 当前 Git 状态:
git status --short
echo.

echo 正在推送代码到 new-origin/main...
echo.

git push new-origin main

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ==========================================
    echo 推送成功！
    echo ==========================================
    echo 远程仓库: https://github.com/wilson323/IOE-DREAMfornew.git
    echo 分支: main
) else (
    echo.
    echo ==========================================
    echo 推送失败，错误代码: %ERRORLEVEL%
    echo ==========================================
    echo 请检查错误信息并参考 PUSH_TO_NEW_ORIGIN_GUIDE.md
)

echo.
pause
