@echo off
:: IOE-DREAM Git钩子安装脚本
:: 设置Git pre-commit钩子以确保代码质量

echo === IOE-DREAM Git钩子安装 ===

:: 检查Git是否安装
git --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Git未安装或未在PATH中
    pause
    exit /b 1
)

echo ✅ Git环境正常

:: 创建钩子目录
if not exist ".git\hooks" (
    mkdir ".git\hooks"
    echo 📁 创建钩子目录
)

:: 复制pre-commit钩子
echo 📋 安装pre-commit钩子...
copy /Y ".git\hooks\pre-commit" ".git\hooks\pre-commit.bak" >nul 2>&1

:: 写入Windows兼容的pre-commit钩子
(
echo #!/bin/bash
echo.
echo # IOE-DREAM Git Pre-commit 钩子 ^(Windows版本^)
echo.
echo set -e
echo.
echo SCRIPT_DIR="%%~dp0"
echo PROJECT_ROOT="%%~dp0..\.."
echo.
echo echo "=== IOE-DREAM Git Pre-commit 质量检查 ==="
echo echo "项目根目录: %%PROJECT_ROOT%%"
echo.
echo cd "%%PROJECT_ROOT%%"
echo.
echo # 检查Java文件变更
echo JAVA_FILES_CHANGED=^$(git diff --cached --name-only --diff-filter=ACM ^| grep -E "\.java$" ^| wc -l^)
echo.
echo if [ "%%JAVA_FILES_CHANGED%%" -eq 0 ]; then
echo     echo "✅ 没有Java文件变更，跳过质量检查"
echo     exit 0
echo fi
echo.
echo echo "📝 发现 %%JAVA_FILES_CHANGED%% 个Java文件变更"
echo echo "🔍 执行质量检查..."
echo.
echo # 执行快速质量检查
echo if command -v pwsh ^&^> /dev/null; then
echo     echo "使用PowerShell执行质量检查..."
echo     pwsh -ExecutionPolicy Bypass -File "%%PROJECT_ROOT%%/ci-quality-gate.ps1" -BuildType "quick"
echo else
echo     echo "⚠️ PowerShell不可用，执行基础编译检查..."
echo     mvn compile -pl microservices-common-core,microservices-common,microservices-common-storage -q
echo     if [ $? -ne 0 ]; then
echo         echo "❌ 编译检查失败，请修复后重新提交"
echo         exit 1
echo     fi
echo     echo "✅ 编译检查通过"
echo fi
echo.
echo if [ $? -eq 0 ]; then
echo     echo ""
echo     echo "🎉 Pre-commit 质量检查通过！"
echo     echo "✅ 代码可以提交"
echo     exit 0
echo else
echo     echo ""
echo     echo "❌ Pre-commit 质量检查失败！"
echo     echo "🔧 请修复上述问题后重新提交"
echo     echo ""
echo     echo "💡 如果需要跳过检查，请使用:"
echo     echo "   git commit --no-verify"
echo     exit 1
echo fi
) > ".git\hooks\pre-commit"

:: 设置钩子可执行权限（在Windows下使用Git Bash）
echo 🔐 设置钩子权限...
cd .git\hooks
chmod +x pre-commit 2>nul
cd ..\..

echo ✅ Git钩子安装完成
echo.
echo 📋 已安装的钩子：
echo    - pre-commit: 提交前自动质量检查
echo.
echo 💡 使用说明：
echo    - 正常提交：git commit -m "message"
echo    - 跳过检查：git commit --no-verify -m "message"
echo.
echo 🎯 钩子功能：
echo    - 自动检查Java文件编译
echo    - 检查代码规范合规性
echo    - 阻止低质量代码提交
echo.
pause