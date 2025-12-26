# 修复 attendance-service 的 LoggerFactory 违规
param(
    [string]$ServicePath = "microservices/ioedream-attendance-service"
)

Write-Host "🔧 开始修复 attendance-service LoggerFactory 违规..." -ForegroundColor Green
Write-Host "===============================================" -ForegroundColor Yellow

# 检查服务是否存在
if (-not (Test-Path $ServicePath)) {
    Write-Host "❌ 服务不存在: $ServicePath" -ForegroundColor Red
    exit 1
}

# 获取所有违规文件
$violationFiles = Get-ChildItem -Path $ServicePath -Filter "*.java" -Recurse |
    Select-String -Pattern "LoggerFactory.getLogger" |
    Select-Object -Unique Path

if (-not $violationFiles) {
    Write-Host "   ✅ 没有发现 LoggerFactory 违规" -ForegroundColor Green
    exit 0
}

$totalViolations = $violationFiles.Count
Write-Host "   📊 发现 $totalViolations 个违规文件" -ForegroundColor Cyan

# 处理每个文件
$fixedCount = 0
foreach ($file in $violationFiles) {
    Write-Host "   修复: $($file.Name)" -ForegroundColor White

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
        $fixedCount++

    } catch {
        Write-Host "   ❌ 修复失败: $($file.Path) - $($_.Exception.Message)" -ForegroundColor Red
    }

    # 显示进度
    if ($fixedCount % 10 -eq 0) {
        Write-Host "   进度: $fixedCount/$totalViolations" -ForegroundColor Cyan
    }
}

Write-Host "   ✅ 修复完成: $fixedCount 个文件" -ForegroundColor Green
Write-Host ""
Write-Host "📋 验证修复结果..." -ForegroundColor Yellow
Write-Host "===============================================" -ForegroundColor Yellow

# 再次检查是否还有违规
$remainingViolations = Get-ChildItem -Path $ServicePath -Filter "*.java" -Recurse |
    Select-String -Pattern "LoggerFactory.getLogger" |
    Select-Object -Unique Path

if (-not $remainingViolations) {
    Write-Host "   ✅ attendance-service LoggerFactory 违规已全部修复！" -ForegroundColor Green
    Write-Host ""
    Write-Host "📋 后续步骤:" -ForegroundColor Cyan
    Write-Host "1. 运行编译检查:" -ForegroundColor White
    Write-Host "   ./scripts/build-all.ps1 -Service $ServicePath" -ForegroundColor Gray
    Write-Host ""
    Write-Host "2. 运行验证检查:" -ForegroundColor White
    Write-Host "   find $ServicePath -name '*.java' -type f -exec grep -l 'LoggerFactory.getLogger' {} \;" -ForegroundColor Gray
} else {
    $remainingCount = $remainingViolations.Count
    Write-Host "   ⚠️  仍有 $remainingCount 个违规文件需要手动处理" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "📋 剩余违规文件:" -ForegroundColor Cyan
    $remainingViolations | Select-Object -First 10 | ForEach-Object {
        Write-Host "   - $($_.Path)" -ForegroundColor Gray
    }
}