@echo off
cd microservices
echo Phase 1.4: Building core modules...
echo.
echo Step 1: Building microservices-common-core...
call mvn clean install -pl microservices-common-core -am -DskipTests
if %ERRORLEVEL% NEQ 0 goto :error

echo.
echo Step 2: Building microservices-common-entity...
call mvn clean install -pl microservices-common-entity -am -DskipTests
if %ERRORLEVEL% NEQ 0 goto :error

echo.
echo Step 3: Building microservices-common-business...
call mvn clean install -pl microservices-common-business -am -DskipTests
if %ERRORLEVEL% NEQ 0 goto :error

echo.
echo Step 4: Building microservices-common-data...
call mvn clean install -pl microservices-common-data -am -DskipTests
if %ERRORLEVEL% NEQ 0 goto :error

echo.
echo Step 5: Building microservices-common-gateway-client...
call mvn clean install -pl microservices-common-gateway-client -am -DskipTests
if %ERRORLEVEL% NEQ 0 goto :error

echo.
echo ✅ Phase 1.4: Core modules build completed successfully!
goto :end

:error
echo.
echo ❌ Build failed!
exit /b 1

:end
pause
