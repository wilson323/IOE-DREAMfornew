# 移除BOM字符脚本
# 用于批量移除Java文件中的UTF-8 BOM字符

Write-Host "开始移除Java文件中的BOM字符..." -ForegroundColor Green

# 获取所有包含BOM的Java文件
$bomFiles = @()
Get-ChildItem -Path "microservices\ioedream-device-comm-service" -Filter "*.java" -Recurse | ForEach-Object {
    $content = [System.IO.File]::ReadAllBytes($_.FullName)
    if ($content.Length -ge 3 -and $content[0] -eq 0xEF -and $content[1] -eq 0xBB -and $content[2] -eq 0xBF) {
        $bomFiles += $_
    }
}

Write-Host "发现 $($bomFiles.Count) 个包含BOM字符的文件：" -ForegroundColor Yellow

# 移除BOM字符
foreach ($file in $bomFiles) {
    Write-Host "处理文件: $($file.FullName)" -ForegroundColor Cyan

    # 读取文件内容（跳过BOM）
    $content = Get-Content -Path $file.FullName -Encoding UTF8
    # 重新写入（不包含BOM）
    $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText($file.FullName, ($content -join "`n"), $utf8NoBom)
}

Write-Host "BOM字符移除完成！" -ForegroundColor Green
Write-Host "处理的文件数量: $($bomFiles.Count)" -ForegroundColor Green