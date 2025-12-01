# IOE-DREAM 批量乱码修复脚本
# 修复所有Java文件中的乱码问题

$ErrorActionPreference = "Continue"
$ProjectRoot = "D:\IOE-DREAM"
Set-Location $ProjectRoot

Write-Host "`n开始批量修复乱码..." -ForegroundColor Cyan

$javaFiles = Get-ChildItem -Path "smart-admin-api-java17-springboot3" -Recurse -Filter "*.java" -ErrorAction SilentlyContinue
$fixedCount = 0
$errorCount = 0

foreach ($file in $javaFiles) {
    try {
        # 读取文件（尝试UTF-8）
        $content = Get-Content $file.FullName -Raw -Encoding UTF8 -ErrorAction SilentlyContinue
        
        if ($null -eq $content) {
            # 尝试GBK
            try {
                $bytes = [System.IO.File]::ReadAllBytes($file.FullName)
                $content = [System.Text.Encoding]::GetEncoding("GBK").GetString($bytes)
            } catch {
                $errorCount++
                continue
            }
        }
        
        $original = $content
        $changed = $false
        
        # 修复常见乱码模式
        $fixes = @{
            "检查" = "检查"
            "结果" = "结果"
            "不一致" = "不一致"
            "时间" = "时间"
            "处理" = "处理"
            "不能为空" = "不能为空"
            "长度不能超过" = "长度不能超过"
            "格式：YYYY-MM）" = "格式：YYYY-MM）"
            "一致性" = "一致性"
            "完整性" = "完整性"
            "对账" = "对账"
            "并行处理" = "并行处理"
        }
        
        foreach ($key in $fixes.Keys) {
            if ($content.Contains($key)) {
                $content = $content.Replace($key, $fixes[$key])
                $changed = $true
            }
        }
        
        # 移除BOM
        if ($content.StartsWith([char]0xFEFF)) {
            $content = $content.Substring(1)
            $changed = $true
        }
        
        # 如果有修改，保存为UTF-8无BOM
        if ($changed) {
            $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
            [System.IO.File]::WriteAllText($file.FullName, $content, $utf8NoBom)
            $fixedCount++
            Write-Host "  [FIXED] $($file.FullName)" -ForegroundColor Green
        }
        
    } catch {
        $errorCount++
        Write-Host "  [ERROR] $($file.FullName): $_" -ForegroundColor Red
    }
}

Write-Host "`n修复完成: 修复 $fixedCount 个文件, 错误 $errorCount 个文件" -ForegroundColor Cyan

