# 修复import语句
Write-Host "修复import语句..." -ForegroundColor Green

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

        # 修复 lombok.extern.slf4j -> lombok.extern.slf4j
        if ($content -match 'lombok\.exte\.slf4j') {
            $content = $content -replace 'lombok\.exte\.slf4j', 'lombok.extern.slf4j'
            $hasChanges = $true
            Write-Host "修复lombok import: $($file.Name)" -ForegroundColor Yellow
        }

        # 修复其他可能的import问题
        if ($content -match 'import.*\.impl\.AreaPermissionServiceImpl') {
            $content = $content -replace 'import.*\.impl\.AreaPermissionServiceImpl', 'import net.lab1024.sa.common.organization.service.impl.AreaPermissionServiceImpl'
            $hasChanges = $true
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

    # 测试其他关键服务
    Write-Host "测试其他关键服务..." -ForegroundColor Cyan
    $services = @("ioedream-common-service", "ioedream-attendance-service", "ioedream-consume-service")
    $successCount = 0

    foreach ($service in $services) {
        $result = mvn clean compile -pl $service -q -DskipTests
        if ($LASTEXITCODE -eq 0) {
            Write-Host "  ✅ $service 编译成功" -ForegroundColor Green
            $successCount++
        } else {
            Write-Host "  ❌ $service 编译失败" -ForegroundColor Red
        }
    }

    $totalServices = $services.Count + 1
    $successRate = [math]::Round(($successCount + 1) * 100.0 / $totalServices, 2)
    Write-Host "编译成功率: $successRate% ($($successCount + 1)/$totalServices)" -ForegroundColor Cyan

    if ($successCount -eq $services.Count) {
        Write-Host "🎉 所有关键服务编译成功!" -ForegroundColor Green
        Write-Host "✅ P1阶段字符编码修复完成!" -ForegroundColor Green
    }
} else {
    Write-Host "❌ access-service编译失败，显示前10个错误:" -ForegroundColor Red
    mvn clean compile -pl ioedream-access-service -DskipTests 2>&1 | Select-String "ERROR" | Select-Object -First 10
}