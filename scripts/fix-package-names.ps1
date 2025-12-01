# 修复微服务common模块中的包名
$CommonDir = "D:\IOE-DREAM\microservices\microservices-common\src\main\java\net\lab1024\sa\common"

Write-Host "开始修复包名..." -ForegroundColor Green

# 递归查找所有Java文件
$javaFiles = Get-ChildItem -Path $CommonDir -Filter "*.java" -Recurse

foreach ($file in $javaFiles) {
    $content = Get-Content $file.FullName -Raw -Encoding UTF8

    # 修复包名
    $oldContent = $content
    $content = $content -replace 'package net\.lab1024\.sa\.base\.common', 'package net.lab1024.sa.common'

    # 修复import语句
    $content = $content -replace 'import net\.lab1024\.sa\.base\.common\.domain\.', 'import net.lab1024.sa.common.domain.'
    $content = $content -replace 'import net\.lab1024\.sa\.base\.common\.util\.', 'import net.lab1024.sa.common.util.'
    $content = $content -replace 'import net\.lab1024\.sa\.base\.common\.cache\.', 'import net.lab1024.sa.common.cache.'
    $content = $content -replace 'import net\.lab1024\.sa\.base\.common\.exception\.', 'import net.lab1024.sa.common.exception.'
    $content = $content -replace 'import net\.lab1024\.sa\.base\.common\.code\.', 'import net.lab1024.sa.common.code.'
    $content = $content -replace 'import net\.lab1024\.sa\.base\.common\.enumeration\.', 'import net.lab1024.sa.common.enumeration.'
    $content = $content -replace 'import net\.lab1024\.sa\.base\.common\.annotation\.', 'import net.lab1024.sa.common.annotation.'
    $content = $content -replace 'import net\.lab1024\.sa\.base\.common\.json\.', 'import net.lab1024.sa.common.json.'
    $content = $content -replace 'import net\.lab1024\.sa\.base\.common\.swagger\.', 'import net.lab1024.sa.common.swagger.'
    $content = $content -replace 'import net\.lab1024\.sa\.base\.common\.controller\.', 'import net.lab1024.sa.common.controller.'
    $content = $content -replace 'import net\.lab1024\.sa\.base\.common\.page\.', 'import net.lab1024.sa.common.page.'

    # 如果内容有变化，则写回文件
    if ($content -ne $oldContent) {
        Set-Content -Path $file.FullName -Value $content -Encoding UTF8
        Write-Host "✓ 修复: $($file.FullName.Replace($CommonDir, ''))" -ForegroundColor Green
    }
}

Write-Host "包名修复完成!" -ForegroundColor Cyan

# 验证修复结果
$remainingErrors = Get-ChildItem -Path $CommonDir -Filter "*.java" -Recurse | Select-String -Pattern "package net\.lab1024\.sa\.base\.common"
if ($remainingErrors) {
    Write-Host "警告: 仍有以下文件包含未修复的包名:" -ForegroundColor Red
    $remainingErrors | ForEach-Object { Write-Host "  $($_.Path)" -ForegroundColor Yellow }
} else {
    Write-Host "✓ 所有包名已成功修复!" -ForegroundColor Green
}