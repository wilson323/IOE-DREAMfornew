# 批量修复剩余微服务的 LoggerFactory 违规
param(
    [string[]]$Services = @("oa-service", "video-service", "visitor-service", "device-comm-service", "biometric-service")
)

Write-Host "🚀 开始批量修复剩余微服务 LoggerFactory 违规..." -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Yellow

$servicesInfo = @{
    "oa-service"           = @{ path = "microservices/ioedream-oa-service"; violations = 48 }
    "video-service"        = @{ path = "microservices/ioedream-video-service"; violations = 55 }
    "visitor-service"      = @{ path = "microservices/ioedream-visitor-service"; violations = 21 }
    "device-comm-service"  = @{ path = "microservices/ioedream-device-comm-service"; violations = 43 }
    "biometric-service"    = @{ path = "microservices/ioedream-biometric-service"; violations = 12 }
}

$totalFixed = 0

foreach ($service in $Services) {
    $info = $servicesInfo[$service]
    $servicePath = $info.path
    $expectedViolations = $info.violations

    Write-Host ""
    Write-Host "🔧 修复服务: $service" -ForegroundColor Cyan
    Write-Host "   路径: $servicePath" -ForegroundColor Gray
    Write-Host "   预期违规: $expectedViolations" -ForegroundColor Gray

    # 检查服务是否存在
    if (-not (Test-Path $servicePath)) {
        Write-Host "   ❌ 服务不存在，跳过" -ForegroundColor Red
        continue
    }

    # 获取实际违规文件
    $violationFiles = Get-ChildItem -Path $servicePath -Filter "*.java" -Recurse |
        Select-String -Pattern "LoggerFactory.getLogger" |
        Select-Object -Unique Path

    if (-not $violationFiles) {
        Write-Host "   ✅ 没有发现 LoggerFactory 违规" -ForegroundColor Green
        continue
    }

    $actualViolations = $violationFiles.Count
    Write-Host "   📊 实际发现: $actualViolations 个违规文件" -ForegroundColor White

    # 处理每个文件
    $serviceFixed = 0
    foreach ($file in $violationFiles) {
        try {
            $content = Get-Content -Path $file.Path -Raw -Encoding UTF8

            # 检查是否已有 @Slf4j
            if ($content -match '@Slf4j') {
                # 移除 LoggerFactory 相关内容
                $content = $content -replace '(?m)^import org\.slf4j\.Logger;.*$\r?\n?', ''
                $content = $content -replace '(?m)^import org\.slf4j\.LoggerFactory;.*$\r?\n?', ''
                $content = $content -replace '(?m)^.*private static final Logger.*= LoggerFactory\.getLogger.*$\r?\n?', ''
            } else {
                # 添加 lombok.extern.slf4j.Slf4j 导入
                if ($content -match 'import lombok') {
                    $content = $content -replace '(import lombok.*\r?\n)', '$1import lombok.extern.slf4j.Slf4j;' + "`r`n"
                } else {
                    $content = "import lombok.extern.slf4j.Slf4j;`r`n" + $content
                }

                # 移除 LoggerFactory 相关内容
                $content = $content -replace '(?m)^import org\.slf4j\.Logger;.*$\r?\n?', ''
                $content = $content -replace '(?m)^import org\.slf4j\.LoggerFactory;.*$\r?\n?', ''
                $content = $content -replace '(?m)^.*private static final Logger.*= LoggerFactory\.getLogger.*$\r?\n?', ''

                # 在类声明前添加 @Slf4j
                $content = $content -replace '(?m)(^@\w+.*\r?\n)*(\r?\n)(public\s+class\s+\w+)', '@Slf4j' + "`r`n" + '$2$3'
            }

            # 清理多余空行
            $content = $content -replace '\r?\n\s*\r?\n\s*\r?\n', "`r`n`r`n"

            # 保存文件
            Set-Content -Path $file.Path -Value $content -NoNewline -Encoding UTF8
            $serviceFixed++

        } catch {
            Write-Host "   ❌ 修复失败: $($file.Path) - $($_.Exception.Message)" -ForegroundColor Red
        }
    }

    Write-Host "   ✅ 修复完成: $serviceFixed 个文件" -ForegroundColor Green
    $totalFixed += $serviceFixed

    # 验证修复结果
    Start-Sleep -Milliseconds 500
    $remainingViolations = Get-ChildItem -Path $servicePath -Filter "*.java" -Recurse |
        Select-String -Pattern "LoggerFactory.getLogger" |
        Measure-Object |
        Select-Object -ExpandProperty Count

    if ($remainingViolations -eq 0) {
        Write-Host "   ✅ $service LoggerFactory 违规已全部修复！" -ForegroundColor Green
    } else {
        Write-Host "   ⚠️  仍有 $remainingViolations 个违规需要手动处理" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "================================================" -ForegroundColor Yellow
Write-Host "🎉 批量修复完成！" -ForegroundColor Green
Write-Host "📊 总计修复: $totalFixed 个文件" -ForegroundColor Cyan
Write-Host ""
Write-Host "📋 后续步骤:" -ForegroundColor White
Write-Host "1. 运行完整扫描验证:" -ForegroundColor Gray
Write-Host "   bash scripts/scan-logger-violations.sh" -ForegroundColor Gray
Write-Host ""
Write-Host "2. 运行编译测试:" -ForegroundColor Gray
Write-Host "   ./scripts/build-all.ps1" -ForegroundColor Gray
Write-Host ""
Write-Host "3. 提交代码:" -ForegroundColor Gray
Write-Host "   git add . && git commit -m 'feat: 完成所有微服务 SLF4J 日志规范统一'" -ForegroundColor Gray