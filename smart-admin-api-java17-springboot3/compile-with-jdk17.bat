@echo off
REM 编译错误修复脚本 - 使用JDK 17编译
REM 根据设计文档 compile-error-handling.md 创建

echo ====================================
echo Maven编译错误修复脚本
echo 使用JDK 17进行编译
echo ====================================
echo.

REM 设置JDK 17路径
set "JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.17.10-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo 当前使用的JDK版本:
java -version
echo.

echo 开始清理项目...
call mvn clean
if %ERRORLEVEL% NEQ 0 (
    echo 清理失败！
    exit /b 1
)
echo.

echo 开始编译项目...
call mvn compile -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo 编译失败！
    exit /b 1
)
echo.

echo ====================================
echo 编译成功完成！
echo ====================================
