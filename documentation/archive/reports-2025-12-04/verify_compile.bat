@echo off
chcp 65001
cd /d D:\IOE-DREAM
echo ========================================
echo 开始编译验证...
echo ========================================
mvn clean compile -rf :ioedream-common-service -DskipTests > verify_result.txt 2>&1
echo.
echo 编译完成，检查结果...
echo.
type verify_result.txt | findstr /i "BUILD SUCCESS BUILD FAILURE ERROR 错误"
echo.
echo ========================================
echo 完整编译日志已保存到 verify_result.txt
echo ========================================

