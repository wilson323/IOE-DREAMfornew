@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    https://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on
@REM an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF
@REM ANY KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper startup batch script, version 3.3.2
@REM
@REM Required ENV vars:
@REM ------------------
@REM   JAVA_HOME - location of a JDK home dir
@REM
@REM Optional ENV vars
@REM -----------------
@REM   MAVEN_OPTS - parameters passed to the Java VM when running Maven
@REM     e.g. to debug Maven itself, use
@REM       set MAVEN_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000
@REM   MAVEN_SKIP_RC - flag to disable loading of mavenrc files
@REM ----------------------------------------------------------------------------

@REM Begin all REM lines with '@' in case MAVEN_SKIP_RC is set
@echo off
setlocal enabledelayedexpansion

set ERROR_CODE=0

@REM set to one of the valid JAVA_HOME values or leave empty to use the default
@REM see: https://github.com/takari/maven-wrapper#java_home_requirements
set "JAVA_HOME="

@REM To isolate Maven from the system environment, use either
@REM   set MAVEN_CMD_OPTS=-Xverify:none -Xmx3G
@REM or
@REM   set MAVEN_PROJECT_BASEDIR=-Dproject.basedir=<path-to-project>
@REM See: https://cwiki.apache.org/confluence/display/MAVEN/Isolating+Maven+from+the+system+environment

if "%JAVA_HOME%"=="" set "JAVA_HOME=D:\opt\java"

@REM set LAUNCH_DIR to the location of mvnw.cmd or the base directory of the project
set "LAUNCH_DIR=%cd%"

if exist "%~f0\.mvn\wrapper\maven-wrapper.jar" (
    set "WRAPPER_JAR=%~f0\.mvn\wrapper\maven-wrapper.jar"
) else (
    set "WRAPPER_JAR=%LAUNCH_DIR%\.mvn\wrapper\maven-wrapper.jar"
)

set "WRAPPER_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar"

@REM Determine the Java command to use to start the JVM
if "%JAVA_HOME%"=="" (
    set "JAVA_CMD=java"
) else (
    set "JAVA_CMD=%JAVA_HOME%\bin\java"
)

@REM Execute Maven
"%JAVA_CMD%" ^
    %MAVEN_PROJECT_BASEDIR% ^
    -classpath "%WRAPPER_JAR%" ^
    "-Dmaven.multiModuleProjectDirectory=%LAUNCH_DIR%" ^
    "-Dmaven.home=%LAUNCH_DIR%\.mvn\wrapper" ^
    org.apache.maven.wrapper.MavenWrapperMain %MAVEN_CMD_OPTS% %MAVEN_CONFIG_OPTS% %*
if ERRORLEVEL 1 goto error
goto end

:error
set ERROR_CODE=1

:end
@endlocal & exit /b %ERROR_CODE%
