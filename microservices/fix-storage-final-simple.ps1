# Storage模块最终简单修复脚本

Write-Host "=== Storage模块最终简单修复 ===" -ForegroundColor Cyan

$storagePath = "D:\IOE-DREAM\microservices\microservices-common-storage"

Get-ChildItem -Path $storagePath -Recurse -Filter "*.java" | ForEach-Object {
    try {
        $content = Get-Content $_.FullName -Raw -Encoding UTF8

        # 修复package声明
        if ($content -match '^ackage ') {
            $content = $content -replace '^ackage ', 'package '
            $utf8WithoutBom = New-Object System.Text.UTF8Encoding($false)
            [System.IO.File]::WriteAllText($_.FullName, $content, $utf8WithoutBom)
            Write-Host "修复: $($_.Name) - package声明" -ForegroundColor Green
        }
    } catch {
        Write-Warning "处理文件失败: $($_.FullName) - $($_.Exception.Message)"
    }
}

Write-Host "`n=== 修复完成 ===" -ForegroundColor Green