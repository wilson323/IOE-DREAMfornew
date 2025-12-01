@echo off
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"
echo Using Java:
java -version
echo.
echo Starting compilation (without clean)...
mvn compile -DskipTests
