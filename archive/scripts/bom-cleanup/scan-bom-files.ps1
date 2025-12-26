# BOM字符扫描脚本
# 扫描microservices目录下所有Java文件的UTF-8 BOM字符

Write-Host "开始扫描Java文件中的BOM字符..." -ForegroundColor Green

$bomFiles = @()
$totalFiles = 0

try {
    # 递归扫描所有Java文件
    $javaFiles = Get-ChildItem -Path "microservices" -Filter "*.java" -Recurse
    $totalFiles = $javaFiles.Count

    Write-Host "发现 $totalFiles 个Java文件，正在检查BOM字符..." -ForegroundColor Yellow

    foreach ($file in $javaFiles) {
        try {
            # 读取文件字节
            $bytes = [System.IO.File]::ReadAllBytes($file.FullName)

            # 检查UTF-8 BOM (EF BB BF)
            if ($bytes.Length -ge 3 -and
                $bytes[0] -eq 0xEF -and
                $bytes[1] -eq 0xBB -and
                $bytes[2] -eq 0xBF) {

                $fileInfo = [PSCustomObject]@{
                    FileName = $file.Name
                    FullPath = $file.FullName
                    Size = $file.Length
                    LastModified = $file.LastWriteTime
                }
                $bomFiles += $fileInfo
                Write-Host "发现BOM文件: $($file.FullName)" -ForegroundColor Red
            }
        }
        catch {
            Write-Host "处理文件出错: $($file.FullName), 错误: $($_.Exception.Message)" -ForegroundColor DarkYellow
        }
    }

    # 生成报告
    $reportPath = "bom-files-report.txt"

    # 创建报告内容
    $reportContent = @"
BOM字符扫描报告
================

扫描时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
扫描目录: microservices
总文件数: $totalFiles
BOM文件数: $($bomFiles.Count)

BOM文件清单:
-----------
"@

    foreach ($bomFile in $bomFiles) {
        $reportContent += "`n$($bomFile.FullPath) (大小: $($bomFile.Size) 字节, 修改时间: $($bomFile.LastModified))"
    }

    if ($bomFiles.Count -eq 0) {
        $reportContent += "`n`n✅ 未发现包含BOM字符的Java文件！"
    } else {
        $reportContent += "`n`n⚠️ 发现 $($bomFiles.Count) 个包含BOM字符的文件需要清理。"
    }

    # 保存报告
    $reportContent | Out-File -FilePath $reportPath -Encoding UTF8

    Write-Host "`n扫描完成！" -ForegroundColor Green
    Write-Host "总扫描文件数: $totalFiles" -ForegroundColor Cyan
    Write-Host "发现BOM文件数: $($bomFiles.Count)" -ForegroundColor Cyan
    Write-Host "报告已保存到: $reportPath" -ForegroundColor Cyan

    # 按模块统计
    if ($bomFiles.Count -gt 0) {
        Write-Host "`n按模块分布:" -ForegroundColor Yellow
        $moduleStats = $bomFiles | Group-Object { $_.FullPath.Split('\')[1] }
        foreach ($stat in $moduleStats) {
            Write-Host "  $($stat.Name): $($stat.Count) 个文件" -ForegroundColor White
        }
    }

}
catch {
    Write-Host "扫描过程中发生错误: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "错误详情: $($_.Exception.StackTrace)" -ForegroundColor DarkRed
}

Write-Host "`n脚本执行完成。" -ForegroundColor Green