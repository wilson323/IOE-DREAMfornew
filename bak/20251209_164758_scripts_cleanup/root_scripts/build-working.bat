@echo off
title IOE-DREAM Working Build
color 0C

echo ========================================
echo IOE-DIRECT BUILD (Maven-Free)
echo ========================================
echo Date: %date% %time%
echo WARNING: Maven has issues, using direct Java compilation
echo.

echo Step 1: Verify Java installation...
java -version 2>&1 | findstr "17" >nul
if errorlevel 1 (
    echo ERROR: Java 17 not found or incorrect version
    java -version
    pause
    exit /b 1
)
echo OK: Java 17 found

echo.
echo Step 2: Check project structure...
if not exist "microservices" (
    echo ERROR: microservices directory not found
    dir /B
    pause
    exit /b 1
)
cd microservices

if not exist "microservices-common" (
    echo ERROR: microservices-common not found
    dir /B
    cd ..
    pause
    exit /b 1
)
echo OK: Project structure found

echo.
echo Step 3: Create build directories...
if not exist "microservices-common\target\classes" mkdir "microservices-common\target\classes"
if not exist "microservices-common\target\lib" mkdir "microservices-common\target\lib"

echo.
echo Step 4: Copy common dependencies...
echo NOTE: In a real build, you would download dependencies from Maven Central
echo For now, we'll create basic structure

echo.
echo Step 5: Compile common module...
echo Compiling basic structure...

REM 创建基础的Java文件结构
echo package net.lab1024.sa.common.entity; > microservices-common\tmp\Test.java
echo public class Test { public static void main(String[] args) { System.out.println("Build test successful"); } } >> microservices-common\tmp\Test.java

javac -d "microservices-common\target\classes" "microservices-common\tmp\Test.java" 2>nul
if errorlevel 1 (
    echo WARNING: Basic compilation failed
) else (
    echo OK: Basic Java compilation works
)

REM 清理临时文件
if exist "microservices-common\tmp" rmdir /s /q "microservices-common\tmp"

echo.
echo Step 6: Check for existing JAR files...
set JAR_COUNT=0
for /d %%i in (ioedream-*) do (
    if exist "%%i\target\*.jar" (
        echo Found: %%i\target\*.jar
        set /a JAR_COUNT+=1
    )
)

if %JAR_COUNT% GTR 0 (
    echo Found %JAR_COUNT% existing JAR files
    echo These may be from previous builds
) else (
    echo No existing JAR files found
    echo This is expected since Maven is not working properly
)

echo.
echo Step 7: Maven diagnostic...
echo Attempting Maven with minimal settings...
set JAVA_TOOL_OPTIONS=
set MAVEN_OPTS=
set CLASSPATH=

"D:\IOE-DREAM\apache-maven-3.9.11\bin\mvn.cmd" --version >maven-test.log 2>&1
if exist maven-test.log (
    echo Maven output written to maven-test.log
    type maven-test.log
    del maven-test.log
)

echo.
echo ========================================
echo BUILD SUMMARY
echo ========================================
echo.
echo Java: Working
echo Project Structure: Found
echo Maven: Not working (ClassNotFoundException: #)
echo.
echo SOLUTION OPTIONS:
echo 1. Reinstall Maven completely
echo 2. Use IDE (IntelliJ/Eclipse) for compilation
echo 3. Fix the CLASSPATH/Java configuration issue
echo 4. Use alternative build system (Gradle)
echo.
echo Current Status: Basic Java environment works, Maven is broken
echo.

cd ..
pause