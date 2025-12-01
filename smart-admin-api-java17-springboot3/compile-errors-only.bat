@echo off
chcp 65001 > nul
echo 检查编译错误...
cd /d "%~dp0"
mvn compile -q 2>&1 | findstr /C:"ERROR" /C:"BUILD"
pause