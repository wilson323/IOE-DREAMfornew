# 编译错误修复脚本
# 修复 sa-base 模块的编译错误

Write-Host "开始修复编译错误..." -ForegroundColor Green

$baseDir = "D:\IOE-DREAM\smart-admin-api-java17-springboot3"
$saBaseDir = Join-Path $baseDir "sa-base"

# 1. 修复 DataSourceConfig.java 编码问题
Write-Host "步骤1: 修复 DataSourceConfig.java 编码问题..." -ForegroundColor Yellow
$dataSourceConfigFile = Join-Path $saBaseDir "src\main\java\net\lab1024\sa\base\config\DataSourceConfig.java"
if (Test-Path $dataSourceConfigFile) {
    $content = Get-Content $dataSourceConfigFile -Raw -Encoding UTF8
    # 确保使用 javax.sql 而非 jakarta.sql
    $content = $content -replace "jakarta\.sql", "javax.sql"
    # 移除BOM标记
    $utf8NoBom = New-Object System.Text.UTF8Encoding $false
    [System.IO.File]::WriteAllText($dataSourceConfigFile, $content, $utf8NoBom)
    Write-Host "  ✓ DataSourceConfig.java 已修复" -ForegroundColor Green
} else {
    Write-Host "  ✗ 文件不存在: $dataSourceConfigFile" -ForegroundColor Red
}

# 2. 清理编译缓存
Write-Host "步骤2: 清理编译缓存..." -ForegroundColor Yellow
$targetDir = Join-Path $saBaseDir "target"
if (Test-Path $targetDir) {
    Remove-Item -Path $targetDir -Recurse -Force -ErrorAction SilentlyContinue
    Write-Host "  ✓ 编译缓存已清理" -ForegroundColor Green
}

# 3. 重新编译
Write-Host "步骤3: 重新编译 sa-base 模块..." -ForegroundColor Yellow
Set-Location $baseDir
$compileResult = mvn clean compile -DskipTests -pl sa-base -am 2>&1

# 检查编译结果
if ($LASTEXITCODE -eq 0) {
    Write-Host "  ✓ 编译成功！" -ForegroundColor Green
} else {
    Write-Host "  ✗ 编译失败，错误信息：" -ForegroundColor Red
    $compileResult | Select-String -Pattern "ERROR" | Select-Object -First 20
}

Write-Host "`n修复完成！" -ForegroundColor Green
