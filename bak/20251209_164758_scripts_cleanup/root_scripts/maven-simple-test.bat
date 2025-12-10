@echo off
echo Testing Maven with minimal configuration...

REM 完全重置环境变量
set JAVA_HOME=
set MAVEN_HOME=
set PATH=
set CLASSPATH=
set JAVA_TOOL_OPTIONS=
set MAVEN_OPTS=

REM 只设置必需的路径
set "PATH=C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot\bin;D:\IOE-DREAM\apache-maven-3.9.11\bin;%PATH%"

echo Java version:
java -version

echo.
echo Maven version:
"D:\IOE-DREAM\apache-maven-3.9.11\bin\mvn.cmd" --version

if errorlevel 1 (
    echo ERROR: Maven still failing
    echo This suggests a fundamental issue with Maven or Java installation
    echo Consider reinstalling Maven or using alternative build tools
) else (
    echo SUCCESS: Maven is working!
    echo Now you can run build-local.bat
)

pause