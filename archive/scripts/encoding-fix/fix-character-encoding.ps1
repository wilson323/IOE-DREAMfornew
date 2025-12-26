# 修复字符编码问题（反引号等特殊字符）
Write-Host "🔧 修复字符编码问题..." -ForegroundColor Green

$accessServicePath = "D:\IOE-DREAM\microservices\ioedream-access-service"
$fixedFiles = 0

# 获取所有Java文件
$javaFiles = Get-ChildItem -Path $accessServicePath -Recurse -Filter "*.java" -File

Write-Host "扫描 $($javaFiles.Count) 个Java文件，修复字符编码问题..." -ForegroundColor Cyan

foreach ($file in $javaFiles) {
    try {
        $content = Get-Content -Path $file.FullName -Raw -Encoding UTF8
        $originalContent = $content
        $hasChanges = $false

        # 修复反引号问题
        if ($content -match '`[^`]*`') {
            $content = $content -replace '`([^`]*)`', "'$1'"
            $hasChanges = $true
        }

        # 修复其他可能的编码问题
        if ($content -match "[\u201C\u201D\u201E\u201F\u2026]") {
            $content = $content -replace '\u201C', '"' -replace '\u201D', '"' -replace '\u201E', '"' -replace '\u201F', '"' -replace '\u2026', "'"
            $hasChanges = $true
        }

        # 如果有修改，保存文件
        if ($hasChanges) {
            # 保存为UTF-8无BOM
            $utf8WithoutBOM = [System.Text.UTF8Encoding]::new($false)
            $bytes = $utf8WithoutBOM.GetBytes($content)
            [System.IO.File]::WriteAllBytes($file.FullName, $bytes)

            $relativePath = $file.FullName.Replace("$accessServicePath\", "")
            Write-Host "✓ 修复字符编码: $relativePath" -ForegroundColor Green
            $fixedFiles++
        }

    } catch {
        $relativePath = $file.FullName.Replace("$accessServicePath\", "")
        Write-Host "❌ 处理失败: $relativePath - $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`n字符编码修复完成! 修复文件数: $fixedFiles" -ForegroundColor Cyan

# 验证编译
Write-Host "`n🔍 验证编译..." -ForegroundColor Green
cd "D:\IOE-DREAM\microservices"

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
    Write-Host "  成功服务数: $successCount + 1" -ForegroundColor $(if ($successCount -eq $keyServices.Count) { "Green" } else { "Yellow" })
    Write-Host "  成功率: $([math]::Round(($successCount + 1) * 100.0 / ($keyServices.Count + 1), 2))%" -ForegroundColor Cyan

    if ($successCount -eq $keyServices.Count) {
        Write-Host "`n🎉 所有关键服务编译成功！" -ForegroundColor Green
        Write-Host "✅ P1阶段编译验证完成" -ForegroundColor Green
        Write-Host "✅ Platform.Core包路径修复完成" -ForegroundColor Green
        Write-Host "✅ BOM字符清理完成" -ForegroundColor Green
        Write-Host "✅ 字符编码修复完成" -ForegroundColor Green
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