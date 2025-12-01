# 修复GBK编码乱码脚本
$ErrorActionPreference = "Continue"
$ProjectRoot = "D:\IOE-DREAM\smart-admin-api-java17-springboot3"
Set-Location $ProjectRoot

Write-Host "开始修复GBK编码乱码..." -ForegroundColor Cyan

$files = Get-ChildItem -Path "sa-admin\src\main\java\net\lab1024\sa\admin\module\attendance" -Recurse -Filter "*.java"
$fixed = 0
$total = $files.Count

foreach ($file in $files) {
    try {
        $bytes = [System.IO.File]::ReadAllBytes($file.FullName)
        $content = [System.Text.Encoding]::GetEncoding("GBK").GetString($bytes)
        $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
        [System.IO.File]::WriteAllText($file.FullName, $content, $utf8NoBom)
        $fixed++
        if ($fixed % 5 -eq 0) {
            Write-Host "已修复 $fixed / $total 个文件..." -ForegroundColor Green
        }
    } catch {
        Write-Host "错误: $($file.FullName)" -ForegroundColor Red
    }
}

Write-Host "总共修复 $fixed / $total 个文件" -ForegroundColor Cyan

