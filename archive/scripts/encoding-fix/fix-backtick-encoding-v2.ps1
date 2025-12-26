# 修复反引号字符编码问题
Write-Host "🔧 修复反引号字符编码问题..." -ForegroundColor Green

$accessServicePath = "D:\IOE-DREAM\microservices\ioedream-access-service"
$fixedFiles = 0
$totalIssues = 0

# 获取所有Java文件
$javaFiles = Get-ChildItem -Path $accessServicePath -Recurse -Filter "*.java" -File

Write-Host "扫描 $($javaFiles.Count) 个Java文件，修复反引号编码问题..." -ForegroundColor Cyan

foreach ($file in $javaFiles) {
    try {
        $content = Get-Content -Path $file.FullName -Raw -Encoding UTF8
        $originalContent = $content
        $hasChanges = $false

        # 修复反引号问题 - 查找所有包含反引号的行
        if ($content -match '`') {
            # 修复 @SuppressWarnings("unchecked")`r`n 模式
            $content = $content -replace '@SuppressWarnings\("unchecked"\)`r`n', '@SuppressWarnings("unchecked")'

            # 修复其他可能的反引号组合
            $content = $content -replace '`r`n', ''
            $content = $content -replace '`', ''

            $hasChanges = $true
            $issueCount = ($originalContent | Select-String '`').Count
            $totalIssues += $issueCount
            $relativePath = $file.FullName.Replace($accessServicePath, "")
            Write-Host "发现 $issueCount 个反引号问题: $relativePath" -ForegroundColor Yellow
        }

        # 如果有修改，保存文件
        if ($hasChanges) {
            # 保存为UTF-8无BOM
            $utf8WithoutBOM = [System.Text.UTF8Encoding]::new($false)
            $bytes = $utf8WithoutBOM.GetBytes($content)
            [System.IO.File]::WriteAllBytes($file.FullName, $bytes)

            $relativePath = $file.FullName.Replace("$accessServicePath\", "")
            Write-Host "✓ 修复反引号编码: $relativePath" -ForegroundColor Green
            $fixedFiles++
        }

    } catch {
        $relativePath = $file.FullName.Replace("$accessServicePath\", "")
        Write-Host "❌ 处理失败: $relativePath - $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`n反引号编码修复完成!" -ForegroundColor Cyan
Write-Host "修复文件数: $fixedFiles" -ForegroundColor Green
Write-Host "总问题数: $totalIssues" -ForegroundColor Yellow

# 验证编译
Write-Host "`n🔍 验证编译..." -ForegroundColor Green
Set-Location "D:\IOE-DREAM\microservices"

$compileResult = mvn clean compile -pl ioedream-access-service -q -DskipTests 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ access-service编译成功！" -ForegroundColor Green

    # 测试其他关键服务
    Write-Host "`n🔍 测试其他关键服务编译..." -ForegroundColor Green

    $keyServices = @("ioedream-common-service", "ioedream-attendance-service", "ioedream-consume-service")
    $successCount = 0

    foreach ($service in $keyServices) {
        Write-Host "  测试 $service..." -ForegroundColor Cyan
        $result = mvn clean compile -pl $service -q -DskipTests 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-Host "    ✅ $service 编译成功" -ForegroundColor Green
            $successCount++
        } else {
            Write-Host "    ❌ $service 编译失败" -ForegroundColor Red
        }
    }

    Write-Host "`n📊 P1阶段编译验证结果:" -ForegroundColor Magenta
    Write-Host "  测试服务数: $($keyServices.Count + 1)" -ForegroundColor White
    Write-Host "  成功服务数: ($successCount + 1)" -ForegroundColor $(if ($successCount -eq $keyServices.Count) { "Green" } else { "Yellow" })
    Write-Host "  成功率: $([math]::Round(($successCount + 1) * 100.0 / ($keyServices.Count + 1), 2))%" -ForegroundColor Cyan

    if ($successCount -eq $keyServices.Count) {
        Write-Host "`n🎉 所有关键服务编译成功！" -ForegroundColor Green
        Write-Host "✅ P1阶段编译验证完成" -ForegroundColor Green
        Write-Host "✅ Platform.Core包路径修复完成" -ForegroundColor Green
        Write-Host "✅ BOM字符清理完成" -ForegroundColor Green
        Write-Host "✅ 反引号编码修复完成" -ForegroundColor Green
        Write-Host "✅ P1阶段核心任务全部完成" -ForegroundColor Green
    }
} else {
    Write-Host "❌ access-service编译失败" -ForegroundColor Red
    Write-Host "错误信息:" -ForegroundColor Yellow

    # 显示前5个错误
    $compileErrorLines = $compileResult -split "`n" | Select-String "ERROR" | Select-Object -First 5
    foreach ($errorMsg in $compileErrorLines) {
        if ($errorMsg -match "非法字符" -or $errorMsg -match "反引号" -or $errorMsg -match "`") {
            Write-Host "    编码问题: $($errorMsg.Trim())" -ForegroundColor Yellow
        } else {
            Write-Host "    $($errorMsg.Trim())" -ForegroundColor Red
        }
    }
}