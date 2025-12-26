# 精确修复特定编码问题
Write-Host "精确修复特定编码问题..." -ForegroundColor Green

$accessServicePath = "D:\IOE-DREAM\microservices\ioedream-access-service"
$fixedFiles = 0

# 获取所有Java文件
$javaFiles = Get-ChildItem -Path $accessServicePath -Recurse -Filter "*.java" -File

Write-Host "检查 $($javaFiles.Count) 个Java文件..." -ForegroundColor Cyan

foreach ($file in $javaFiles) {
    try {
        $content = Get-Content -Path $file.FullName -Raw -Encoding UTF8
        $originalContent = $content
        $hasChanges = $false

        # 只修复反引号相关的特定问题
        # 1. 修复 @SuppressWarnings("unchecked")`r`n 模式
        if ($content -match '@SuppressWarnings\("unchecked"\)`r`n') {
            $content = $content -replace '@SuppressWarnings\("unchecked"\)`r`n', '@SuppressWarnings("unchecked")'
            $hasChanges = $true
            Write-Host "修复SuppressWarnings+r`n: $($file.Name)" -ForegroundColor Yellow
        }

        # 2. 修复重复的gatewayServiceClient.call模式
        if ($content -match 'ResponseDTO<(\w+Entity)> (\w+)Entity = \(ResponseDTO<\1>\) gatewayServiceClient\.call\s+ResponseDTO<\1> (\w+)Response = gatewayServiceClient\.callCommonService\(') {
            $content = $content -replace 'ResponseDTO<(\w+Entity)> (\w+)Entity = \(ResponseDTO<\1>\) gatewayServiceClient\.call\s+ResponseDTO<\1> (\w+)Response = gatewayServiceClient\.callCommonService\(', 'ResponseDTO<$1> $2Response = gatewayServiceClient.callCommonService('
            $hasChanges = $true
            Write-Host "修复重复声明: $($file.Name)" -ForegroundColor Yellow
        }

        # 如果有修改，保存文件
        if ($hasChanges) {
            $utf8WithoutBOM = [System.Text.UTF8Encoding]::new($false)
            $bytes = $utf8WithoutBOM.GetBytes($content)
            [System.IO.File]::WriteAllBytes($file.FullName, $bytes)

            Write-Host "  修复完成" -ForegroundColor Green
            $fixedFiles++
        }

    } catch {
        Write-Host "处理失败: $($file.Name) - $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "修复完成! 处理文件数: $fixedFiles" -ForegroundColor Cyan

# 验证编译
Write-Host "验证编译..." -ForegroundColor Green
Set-Location "D:\IOE-DREAM\microservices"

$compileResult = mvn clean compile -pl ioedream-access-service -q -DskipTests
if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ access-service编译成功!" -ForegroundColor Green
} else {
    Write-Host "❌ access-service编译失败，显示错误:" -ForegroundColor Red
    mvn clean compile -pl ioedream-access-service -DskipTests 2>&1 | Select-String "ERROR" | Select-Object -First 10
}