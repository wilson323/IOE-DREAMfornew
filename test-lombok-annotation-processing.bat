@echo off
echo Testing Lombok annotation processing...
cd microservices

echo 1. Testing Maven compile with Lombok annotation processing...
mvn compile -pl microservices-common-core -Dpmd.skip=true -Dspring-boot.repackage.skip=true -q

echo 2. Checking compiled classes for Lombok-generated methods...
if exist "microservices-common-core\target\classes\net\lab1024\sa\common\core\domain\entity\UserEntity.class" (
    echo Found compiled UserEntity, checking for Lombok methods...
    javap -cp "microservices-common-core\target\classes" net.lab1024.sa.common.core.domain.entity.UserEntity | findstr "getId\|setName\|getName" && echo "✅ Lombok methods found!" || echo "❌ Lombok methods not found"
) else (
    echo UserEntity class not found, checking any class...
    if exist "microservices-common-core\target\classes" (
        for /r "microservices-common-core\target\classes" %%f in (*.class) do (
            echo Checking %%f
            javap -cp "microservices-common-core\target\classes" %%f | findstr "get\|set" | head -3
        )
    )
)

echo 3. Testing annotation processor configuration...
mvn help:effective-pom -pl microservices-common-core -Doutput=properties | findstr "annotationProcessor" && echo "✅ Annotation processors configured!" || echo "❌ Annotation processors not found in config"

echo 4. Verifying JDK version in Maven...
mvn help:evaluate -pl microservices-common-core -Dexpression="${java.version}" -q -DforceStdout

echo.
echo Lombok annotation processing test completed.
pause