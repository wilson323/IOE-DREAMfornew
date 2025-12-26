# 修复Controller类声明语法错误
# 解决注解后直接跟{而没有public class声明的问题

Write-Host "=== 修复Controller类声明语法错误 ===" -ForegroundColor Cyan

$files = @(
    "ioedream-visitor-service/src/main/java/net/lab1024/sa/visitor/controller/VisitorBlacklistController.java",
    "ioedream-visitor-service/src/main/java/net/lab1024/sa/visitor/controller/VisitorSecurityController.java"
)

$totalFixed = 0

foreach ($file in $files) {
    $fullPath = Join-Path "." $file

    if (-not (Test-Path $fullPath)) {
        Write-Host "❌ 文件不存在: $file" -ForegroundColor Red
        continue
    }

    Write-Host "修复文件: $file" -ForegroundColor Yellow

    try {
        $content = Get-Content -Path $fullPath -Raw -Encoding UTF8
        $originalContent = $content

        # 查找最后一个注解行和后面的{
        $lines = $content -split "`r?`n"
        $newLines = @()
        $fixed = $false

        for ($i = 0; $i -lt $lines.Length; $i++) {
            $line = $lines[$i]
            $newLines += $line

            # 如果这行是}，并且还没有找到public class声明，我们需要处理
            if ($line.Trim() -eq '{' -and $i -gt 0 -and -not $fixed) {
                # 检查前一行是否是注解
                $prevLine = $lines[$i-1].Trim()
                if ($prevLine.StartsWith('@')) {
                    # 获取文件名来确定类名
                    $fileName = [System.IO.Path]::GetFileNameWithoutExtension($fullPath)

                    # 替换{为public class声明
                    $newLines[$i] = "public class $fileName {"
                    $fixed = $true
                    $totalFixed++
                    Write-Host "✅ 修复类声明: $fileName" -ForegroundColor Green
                }
            }
        }

        if ($fixed) {
            $newContent = $newLines -join "`r`n"
            $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
            [System.IO.File]::WriteAllText($fullPath, $newContent, $utf8NoBom)
        } else {
            Write-Host "⚠️ 文件已正确，无需修复: $file" -ForegroundColor Yellow
        }

    } catch {
        Write-Host "❌ 修复失败: $file - $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`n=== 修复完成 ===" -ForegroundColor Cyan
Write-Host "总共修复了 $totalFixed 个文件" -ForegroundColor Green