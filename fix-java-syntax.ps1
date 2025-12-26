# 修复Java文件中的PowerShell语法错误
Write-Host "修复Java文件中的PowerShell语法错误..." -ForegroundColor Green

$filesToFix = @(
    "D:\IOE-DREAM\microservices\ioedream-oa-service\src\main\java\net\lab1024\sa\oa\manager\WorkflowEngineManager.java",
    "D:\IOE-DREAM\microservices\ioedream-oa-service\src\main\java\net\lab1024\sa\oa\workflow\function\CheckAreaPermissionFunction.java"
)

foreach ($filePath in $filesToFix) {
    if (Test-Path $filePath) {
        Write-Host "修复文件: $filePath"

        try {
            # 读取文件内容
            $content = Get-Content -Path $filePath -Raw -Encoding UTF8

            # 保存原始内容用于比较
            $originalContent = $content

            # 移除PowerShell的 `r`n 字符序列，替换为正确的换行
            $content = $content -replace '`r`n', "`r`n"

            # 移除PowerShell的 ` 字符
            $content = $content -replace '`', ''

            # 修复 @SuppressWarnings 位置
            $content = $content -replace '@SuppressWarnings\("unchecked"\)\s*\r?\n\s*', '@SuppressWarnings("unchecked") '

            # 保存文件
            [System.IO.File]::WriteAllText($filePath, $content, [System.Text.Encoding]::UTF8)
            Write-Host "  ✓ 修复完成" -ForegroundColor Green

        } catch {
            Write-Host "  ❌ 修复失败: $($_.Exception.Message)" -ForegroundColor Red
        }
    } else {
        Write-Host "  ❌ 文件不存在: $filePath" -ForegroundColor Red
    }
}

# 清理CheckAreaPermissionFunction的BOM
$checkAreaFile = "D:\IOE-DREAM\microservices\ioedream-oa-service\src\main\java\net\lab1024\sa\oa\workflow\function\CheckAreaPermissionFunction.java"
if (Test-Path $checkAreaFile) {
    $bytes = [System.IO.File]::ReadAllBytes($checkAreaFile)
    if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
        $bytesWithoutBom = $bytes[3..($bytes.Length-1)]
        [System.IO.File]::WriteAllBytes($checkAreaFile, $bytesWithoutBom)
        Write-Host "✓ CheckAreaPermissionFunction BOM字符已移除" -ForegroundColor Green
    }
}

Write-Host "`nJava语法修复完成！" -ForegroundColor Cyan