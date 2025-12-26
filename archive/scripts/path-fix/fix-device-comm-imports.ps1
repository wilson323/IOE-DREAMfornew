# 修复device-comm-service导入路径脚本
# 将platform.core包替换为正确的包路径

Write-Host "开始修复device-comm-service的导入路径..." -ForegroundColor Green

# 获取所有Java文件
$javaFiles = Get-ChildItem -Path "microservices\ioedream-device-comm-service" -Filter "*.java" -Recurse

Write-Host "发现 $($javaFiles.Count) 个Java文件需要处理" -ForegroundColor Yellow

$fixedCount = 0

foreach ($file in $javaFiles) {
    $content = Get-Content -Path $file.FullName -Raw
    $originalContent = $content

    # 替换导入路径
    $content = $content -replace 'net\.lab1024\.sa\.platform\.core\.dto\.ResponseDTO', 'net.lab1024.sa.platform.core.dto.ResponseDTO'
    $content = $content -replace 'net\.lab1024\.sa\.platform\.core\.exception\.', 'net.lab1024.sa.common.exception.'
    $content = $content -replace 'net\.lab1024\.sa\.platform\.core\.dto\.', 'net.lab1024.sa.platform.core.dto.'

    # 如果内容有变化，则写入文件
    if ($content -ne $originalContent) {
        Set-Content -Path $file.FullName -Value $content -Encoding UTF8
        Write-Host "修复文件: $($file.FullName)" -ForegroundColor Cyan
        $fixedCount++
    }
}

Write-Host "导入路径修复完成！" -ForegroundColor Green
Write-Host "修复的文件数量: $fixedCount" -ForegroundColor Green