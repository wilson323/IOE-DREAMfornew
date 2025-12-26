# 全局BOM字符清理脚本
param(
    [string]$ProjectRoot = "D:\IOE-DREAM"
)

Write-Host "开始全局BOM字符清理..."
$bomFixedCount = 0

# 查找所有Java文件
$javaFiles = Get-ChildItem -Path "$ProjectRoot\microservices" -Recurse -Filter "*.java" -File

Write-Host "找到 $($javaFiles.Count) 个Java文件"

foreach ($file in $javaFiles) {
    # 检查文件是否包含BOM
    $bytes = [System.IO.File]::ReadAllBytes($file.FullName)

    if ($bytes.Length -ge 3 -and
        $bytes[0] -eq 0xEF -and
        $bytes[1] -eq 0xBB -and
        $bytes[2] -eq 0xBF) {

        Write-Host "移除BOM: $($file.FullName)"

        # 移除BOM并重写文件
        $bytesWithoutBom = $bytes[3..($bytes.Length-1)]
        [System.IO.File]::WriteAllBytes($file.FullName, $bytesWithoutBom)
        $bomFixedCount++
    }
}

Write-Host "`n全局BOM清理完成！"
Write-Host "总共清理了 $bomFixedCount 个文件的BOM字符"

# 验证结果
Write-Host "验证清理结果..."
$remainingBomFiles = Get-ChildItem -Path "$ProjectRoot\microservices" -Recurse -Filter "*.java" -File | ForEach-Object {
    $bytes = [System.IO.File]::ReadAllBytes($_.FullName)
    if ($bytes.Length -ge 3 -and
        $bytes[0] -eq 0xEF -and
        $bytes[1] -eq 0xBB -and
        $bytes[2] -eq 0xBF) {
        Write-Host "仍有BOM: $($_.FullName)"
    }
}

if ($remainingBomFiles.Count -eq 0) {
    Write-Host "✅ 所有文件的BOM字符已清理完毕！"
} else {
    Write-Host "⚠️ 还有 $($remainingBomFiles.Count) 个文件包含BOM字符"
}

Write-Host "`n建议接下来运行: mvn clean compile -pl microservices/ioedream-access-service -am -DskipTests"