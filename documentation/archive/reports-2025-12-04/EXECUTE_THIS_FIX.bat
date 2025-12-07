@echo off
chcp 65001 > nul
cd /d D:\IOE-DREAM

echo.
echo ============================================================
echo   IOE-DREAM 编码问题一键修复工具
echo ============================================================
echo.
echo [1/3] 正在执行BOM移除和全角符号修复...
echo.

python remove_bom_and_fix_encoding.py
if errorlevel 1 (
    echo.
    echo ❌ Python脚本执行失败
    echo.
    echo 请确认:
    echo   1. Python已正确安装（python --version）
    echo   2. Python在系统PATH中
    echo   3. 使用CMD而非PowerShell执行
    echo.
    pause
    exit /b 1
)

echo.
echo ✅ 编码修复完成
echo.
echo [2/3] 正在重新编译项目...
echo.

mvn clean compile -rf :ioedream-common-service -DskipTests > compile_verification.txt 2>&1

echo.
echo [3/3] 正在分析编译结果...
echo.

findstr /i "BUILD SUCCESS" compile_verification.txt > nul
if errorlevel 1 (
    echo ❌ 编译失败，详细错误如下:
    echo.
    type compile_verification.txt | findstr /i /n "ERROR 错误" | findstr /v "Re-run Maven" | findstr /v "To see the full"
    echo.
    echo 完整编译日志保存在: compile_verification.txt
    echo.
    echo 建议:
    echo   1. 查看上述错误信息
    echo   2. 参考 COMPREHENSIVE_ENCODING_FIX_REPORT.md
    echo   3. 如需帮助，联系架构团队
) else (
    echo.
    echo ============================================================
    echo   ✅✅✅ 编译成功！所有编码问题已修复 ✅✅✅
    echo ============================================================
    echo.
    echo 下一步:
    echo   1. 运行完整构建: mvn clean install -DskipTests
    echo   2. 配置IDE使用UTF-8（无BOM）避免问题再次出现
    echo   3. 查看 COMPREHENSIVE_ENCODING_FIX_REPORT.md 了解预防措施
)

echo.
echo ============================================================
echo.
pause

