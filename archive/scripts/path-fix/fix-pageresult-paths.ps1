# 专门修复PageResult包路径脚本
Write-Host "开始修复PageResult包路径..." -ForegroundColor Green

$fixedFiles = 0

# 获取所有包含PageResult的Java文件
$javaFiles = Get-ChildItem -Path "microservices" -Filter "*.java" -Recurse | Where-Object { $_.FullName -match "ioedream-common-service" }

Write-Host "发现 $($javaFiles.Count) 个ioedream-common-service文件，正在修复PageResult包路径..." -ForegroundColor Yellow

foreach ($file in $javaFiles) {
    try {
        $content = Get-Content $file.FullName -Raw

        # 修复PageResult包路径
        if ($content -match 'net\.lab1024\.sa\.platform\.core\.dto\.PageResult') {
            $content = $content -replace 'net\.lab1024\.sa\.platform\.core\.dto\.PageResult', 'net.lab1024.sa.common.domain.PageResult'
            Set-Content -Path $file.FullName -Value $content -Encoding UTF8
            $fixedFiles++
            $relativePath = $file.FullName.Replace('D:\IOE-DREAM\', '')
            Write-Host "✅ 修复: $relativePath" -ForegroundColor Cyan
        }
    }
    catch {
        Write-Host "❌ 错误: 处理文件失败 - $($file.FullName)" -ForegroundColor Red
    }
}

Write-Host "`nPageResult包路径修复完成！" -ForegroundColor Green
Write-Host "修复文件数: $fixedFiles" -ForegroundColor Cyan