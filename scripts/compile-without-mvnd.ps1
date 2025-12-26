# 使用标准Maven编译（禁用Maven Daemon）
$env:MAVEN_OPTS = ""
$env:MVND_DISABLED = "true"

Write-Host "使用标准Maven编译..." -ForegroundColor Cyan

cd "D:\IOE-DREAM\microservices"
mvn clean compile -pl microservices-common-entity -am -DskipTests

Write-Host "编译完成" -ForegroundColor Green
