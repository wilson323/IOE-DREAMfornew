# 批量更新Entity导入路径脚本
# 用途：将所有Java文件中的Entity导入路径从旧路径更新到统一路径

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "批量更新Entity导入路径" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$baseDir = "D:\IOE-DREAM\microservices"

# 定义导入路径映射
$importMappings = @{
    "net.lab1024.sa.access.entity." = "net.lab1024.sa.common.entity.access.";
    "net.lab1024.sa.attendance.entity." = "net.lab1024.sa.common.entity.attendance.";
    "net.lab1024.sa.visitor.entity." = "net.lab1024.sa.common.entity.visitor.";
    "net.lab1024.sa.biometric.entity." = "net.lab1024.sa.common.entity.biometric.";
    "net.lab1024.sa.consume.entity." = "net.lab1024.sa.common.entity.consume.";
    "net.lab1024.sa.device.entity." = "net.lab1024.sa.common.entity.device.";
    "net.lab1024.sa.video.entity." = "net.lab1024.sa.common.entity.video.";
}

$totalFiles = 0
$totalUpdated = 0
$totalErrors = 0

# 获取所有需要处理的业务服务
$services = Get-ChildItem -Path $baseDir -Directory | Where-Object { $_.Name -match "^ioedream-.*-service$" }

Write-Host "📦 发现 $($services.Count) 个业务服务" -ForegroundColor Cyan
Write-Host ""

foreach ($service in $services) {
    $serviceName = $service.Name
    $servicePath = $service.FullName
    $javaFiles = Get-ChildItem -Path $servicePath -Recurse -Filter "*.java" -File

    Write-Host "【$serviceName】处理中..." -ForegroundColor Yellow
    Write-Host "  📄 发现 $($javaFiles.Count) 个Java文件" -ForegroundColor Cyan

    $serviceUpdated = 0
    $serviceErrors = 0

    foreach ($javaFile in $javaFiles) {
        try {
            # 读取文件内容
            $content = Get-Content $javaFile.FullName -Raw -Encoding UTF8
            $originalContent = $content
            $fileUpdated = $false

            # 应用所有导入路径映射
            foreach ($oldImport in $importMappings.Keys) {
                $newImport = $importMappings[$oldImport]

                if ($content -match [regex]::Escape($oldImport)) {
                    $content = $content -replace [regex]::Escape($oldImport), $newImport
                    $fileUpdated = $true
                }
            }

            # 如果文件被更新，写回磁盘
            if ($fileUpdated) {
                $content | Out-File -FilePath $javaFile.FullName -Encoding UTF8 -NoNewline
                $serviceUpdated++
                $totalUpdated++

                # 显示更新的文件（仅显示前5个）
                if ($serviceUpdated -le 5) {
                    $relativePath = $javaFile.FullName.Substring($baseDir.Length + 1)
                    Write-Host "    ✅ $relativePath" -ForegroundColor Green
                }
            }

            $totalFiles++
        } catch {
            Write-Host "    ❌ 处理文件失败: $($javaFile.Name) - $($_.Exception.Message)" -ForegroundColor Red
            $serviceErrors++
            $totalErrors++
        }
    }

    if ($serviceUpdated -gt 5) {
        Write-Host "    ... 还有 $($serviceUpdated - 5) 个文件已更新" -ForegroundColor Gray
    }

    Write-Host "  📊 $serviceName`: $serviceUpdated 个文件更新, $serviceErrors 个错误" -ForegroundColor Cyan
    Write-Host ""
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "更新完成统计" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "📄 处理文件总数: $totalFiles" -ForegroundColor Cyan
Write-Host "✅ 更新文件数: $totalUpdated" -ForegroundColor Green
Write-Host "❌ 错误文件数: $totalErrors" -ForegroundColor Red
Write-Host ""

if ($totalErrors -eq 0) {
    Write-Host "🎉 所有导入路径更新成功！" -ForegroundColor Green
    exit 0
} else {
    Write-Host "⚠️  部分文件更新失败，请检查错误信息" -ForegroundColor Yellow
    exit 1
}
