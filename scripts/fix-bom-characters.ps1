# 修复Java文件中的BOM字符
# 使用方法: .\scripts\fix-bom-characters.ps1

Write-Host "开始修复BOM字符..." -ForegroundColor Cyan

$attendanceServicePath = "D:\IOE-DREAM\microservices\ioedream-attendance-service\src\main\java"
$files = @(
    "net\lab1024\sa\attendance\controller\AttendanceRecordController.java",
    "net\lab1024\sa\attendance\decorator\impl\AntiCheatingDecorator.java",
    "net\lab1024\sa\attendance\decorator\impl\GPSValidationDecorator.java",
    "net\lab1024\sa\attendance\decorator\impl\LoggingDecorator.java",
    "net\lab1024\sa\attendance\engine\algorithm\impl\BacktrackAlgorithmImpl.java"
)

foreach ($file in $files) {
    $fullPath = Join-Path $attendanceServicePath $file
    if (Test-Path $fullPath) {
        try {
            # 读取文件内容（自动处理BOM）
            $content = Get-Content $fullPath -Raw -Encoding UTF8

            # 移除BOM字符
            if ($content.StartsWith([char]0xFEFF)) {
                $content = $content.Substring(1)
                Write-Host "修复BOM: $file" -ForegroundColor Yellow
            }

            # 移除所有BOM字符（包括中间可能存在的）
            $content = $content -replace [char]0xFEFF, ''

            # 保存文件（UTF8无BOM）
            $utf8NoBom = New-Object System.Text.UTF8Encoding $false
            [System.IO.File]::WriteAllText($fullPath, $content, $utf8NoBom)

            Write-Host "✓ 已修复: $file" -ForegroundColor Green
        }
        catch {
            Write-Host "✗ 修复失败: $file - $($_.Exception.Message)" -ForegroundColor Red
        }
    }
    else {
        Write-Host "✗ 文件不存在: $file" -ForegroundColor Red
    }
}

Write-Host "`nBOM字符修复完成！" -ForegroundColor Cyan
