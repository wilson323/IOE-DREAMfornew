@echo off
chcp 65001 > nul
echo 开始编译项目...
cd /d "%~dp0"
mvn clean compile %*
echo 编译完成，请检查上方的输出结果。