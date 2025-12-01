@echo off
rem 解决Maven编译中文乱码问题的专用脚本
rem 设置UTF-8编码环境变量，确保中文错误信息正确显示

echo ===============================================
echo Maven编译专用脚本 - 解决中文乱码问题
echo ===============================================

rem 设置Java系统属性，强制UTF-8编码
set JAVA_OPTS=-Dfile.encoding=UTF-8 -Duser.language=zh -Duser.country=CN -Duser.timezone=Asia/Shanghai

rem 设置Maven_OPTS
set MAVEN_OPTS=-Dfile.encoding=UTF-8 -Duser.language=zh -Duser.country=CN -Duser.timezone=Asia/Shanghai

rem 设置控制台代码页为UTF-8 (Windows)
chcp 65001 >nul 2>&1

echo 环境变量设置完成：
echo   JAVA_OPTS=%JAVA_OPTS%
echo   MAVEN_OPTS=%MAVEN_OPTS%
echo   控制台编码: UTF-8 (chcp 65001)
echo.

rem 执行传入的Maven命令
mvn %*

echo.
echo Maven命令执行完成
pause