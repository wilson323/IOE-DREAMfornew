# 清理Java文件的BOM标记
$CommonDir = "D:\IOE-DREAM\microservices\microservices-common\src\main\java\net\lab1024\sa\common"

Write-Host "开始清理BOM标记..." -ForegroundColor Green

# 获取所有Java文件
$javaFiles = Get-ChildItem -Path $CommonDir -Filter "*.java" -Recurse

foreach ($file in $javaFiles) {
    try {
        # 读取文件内容
        $content = [System.IO.File]::ReadAllText($file.FullName, [System.Text.UTF8Encoding]::$false)

        # 检查是否有BOM标记
        if ($content.StartsWith([char]0xFEFF)) {
            Write-Host "✓ 发现BOM标记，正在清理: $($file.FullName.Replace($CommonDir, ''))" -ForegroundColor Yellow

            # 移除BOM标记
            $content = $content.Substring(1)

            # 写回文件（无BOM的UTF8）
            [System.IO.File]::WriteAllText($file.FullName, $content, [System.Text.UTF8Encoding]::$false)

            Write-Host "  已清理BOM标记" -ForegroundColor Green
        }
    } catch {
        Write-Host "✗ 处理文件失败: $($file.FullName) - $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "BOM标记清理完成!" -ForegroundColor Cyan

# 验证清理结果
Write-Host "验证清理结果..." -ForegroundColor Yellow
$filesWithBom = 0

foreach ($file in $javaFiles) {
    try {
        $bytes = [System.IO.File]::ReadAllBytes($file.FullName)
        if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
            $filesWithBom++
            Write-Host "✗ 仍有BOM标记: $($file.FullName)" -ForegroundColor Red
        }
    } catch {
        Write-Host "✗ 读取文件失败: $($file.FullName)" -ForegroundColor Red
    }
}

if ($filesWithBom -eq 0) {
    Write-Host "✓ 所有文件BOM标记已清理!" -ForegroundColor Green
} else {
    Write-Host "✗ 还有 $filesWithBom 个文件包含BOM标记" -ForegroundColor Red
}