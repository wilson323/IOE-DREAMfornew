# 代码冗余检查脚本
# 检查策略类中的重复代码模式

$ErrorActionPreference = "Stop"
$strategyPath = "microservices\ioedream-consume-service\src\main\java\net\lab1024\sa\consume\strategy\impl"

Write-Host "开始代码冗余检查..." -ForegroundColor Green

# 检查getModeConfig方法的重复
Write-Host "`n检查getModeConfig方法重复..." -ForegroundColor Yellow
$getModeConfigFiles = Get-ChildItem -Path $strategyPath -Filter "*Strategy.java" | Where-Object {
    $content = Get-Content $_.FullName -Raw
    $content -match "getModeConfig"
}

Write-Host "发现 $($getModeConfigFiles.Count) 个文件包含getModeConfig方法:" -ForegroundColor Cyan
foreach ($file in $getModeConfigFiles) {
    Write-Host "  - $($file.Name)" -ForegroundColor White
}

# 检查validateDailyLimit方法的重复
Write-Host "`n检查validateDailyLimit方法重复..." -ForegroundColor Yellow
$validateDailyLimitFiles = Get-ChildItem -Path $strategyPath -Filter "*Strategy.java" | Where-Object {
    $content = Get-Content $_.FullName -Raw
    $content -match "validateDailyLimit"
}

Write-Host "发现 $($validateDailyLimitFiles.Count) 个文件包含validateDailyLimit方法:" -ForegroundColor Cyan
foreach ($file in $validateDailyLimitFiles) {
    Write-Host "  - $($file.Name)" -ForegroundColor White
}

# 检查calculateAmount方法的重复
Write-Host "`n检查calculateAmount方法重复..." -ForegroundColor Yellow
$calculateAmountFiles = Get-ChildItem -Path $strategyPath -Filter "*Strategy.java" | Where-Object {
    $content = Get-Content $_.FullName -Raw
    $content -match "calculateAmount"
}

Write-Host "发现 $($calculateAmountFiles.Count) 个文件包含calculateAmount方法:" -ForegroundColor Cyan
foreach ($file in $calculateAmountFiles) {
    Write-Host "  - $($file.Name)" -ForegroundColor White
}

# 检查GatewayServiceClient注入的重复
Write-Host "`n检查GatewayServiceClient注入重复..." -ForegroundColor Yellow
$gatewayClientFiles = Get-ChildItem -Path $strategyPath -Filter "*Strategy.java" | Where-Object {
    $content = Get-Content $_.FullName -Raw
    $content -match "GatewayServiceClient"
}

Write-Host "发现 $($gatewayClientFiles.Count) 个文件注入GatewayServiceClient:" -ForegroundColor Cyan
foreach ($file in $gatewayClientFiles) {
    Write-Host "  - $($file.Name)" -ForegroundColor White
}

# 检查AccountManager注入的重复
Write-Host "`n检查AccountManager注入重复..." -ForegroundColor Yellow
$accountManagerFiles = Get-ChildItem -Path $strategyPath -Filter "*Strategy.java" | Where-Object {
    $content = Get-Content $_.FullName -Raw
    $content -match "@Resource.*AccountManager|AccountManager.*accountManager"
}

Write-Host "发现 $($accountManagerFiles.Count) 个文件注入AccountManager:" -ForegroundColor Cyan
foreach ($file in $accountManagerFiles) {
    Write-Host "  - $($file.Name)" -ForegroundColor White
}

Write-Host "`n代码冗余检查完成!" -ForegroundColor Green

