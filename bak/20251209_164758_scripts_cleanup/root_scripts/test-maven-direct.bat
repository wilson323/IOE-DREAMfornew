@echo off
echo Testing Maven directly...
echo Current directory: %cd%
echo.

REM 设置环境变量
set MAVEN_HOME=D:\IOE-DREAM\apache-maven-3.9.11
set PATH=%MAVEN_HOME%\bin;%PATH%
set JAVA_TOOL_OPTIONS=
set MAVEN_OPTS=

REM 测试Maven命令
echo Testing Maven version...
"D:\IOE-DREAM\apache-maven-3.9.11\bin\mvn.cmd" --version

if errorlevel 1 (
    echo ERROR: Maven command failed
    pause
    exit /b 1
)

echo.
echo Maven test completed successfully!
pause