# 修复重复内容的脚本

Write-Host "=== 修复重复内容工具 ===" -ForegroundColor Cyan

# 获取所有微服务目录
$serviceDirs = Get-ChildItem -Directory | Where-Object { $_.Name -match "^ioedream-" }

$totalFilesFixed = 0

foreach ($serviceDir in $serviceDirs) {
    $serviceName = $serviceDir.Name
    Write-Host "`n处理服务: $serviceName" -ForegroundColor Green

    # 获取所有Java文件
    $javaFiles = Get-ChildItem -Path $serviceDir.FullName -Recurse -Filter "*.java"
    $filesFixed = 0

    foreach ($javaFile in $javaFiles) {
        try {
            $content = Get-Content -Path $javaFile.FullName -Raw -Encoding UTF8

            # 检查是否有重复的package声明
            if ($content -match "package\s+[\w.]+;[\s\S]*package\s+[\w.]+;") {
                Write-Host "  发现重复内容: $($javaFile.Name)" -ForegroundColor Yellow

                # 找到第一个package声明之后的所有内容
                $firstPackageEnd = $content.IndexOf(";", $content.IndexOf("package"))
                if ($firstPackageEnd -gt 0) {
                    # 查找第二个package声明
                    $secondPackageStart = $content.IndexOf("package", $firstPackageEnd + 1)

                    if ($secondPackageStart -gt 0) {
                        # 截取到第二个package开始之前的内容
                        $cleanContent = $content.Substring(0, $secondPackageStart).Trim()

                        # 写回文件
                        $utf8WithoutBom = New-Object System.Text.UTF8Encoding($false)
                        [System.IO.File]::WriteAllText($javaFile.FullName, $cleanContent, $utf8WithoutBom)
                        $filesFixed++
                    }
                }
            }
        }
        catch {
            Write-Warning "处理文件失败: $($javaFile.FullName) - $($_.Exception.Message)"
        }
    }

    Write-Host "  ✓ 修复了 $filesFixed 个文件" -ForegroundColor Cyan
    $totalFilesFixed += $filesFixed
}

Write-Host "`n=== 重复内容修复完成 ===" -ForegroundColor Green
Write-Host "总计修复文件数: $totalFilesFixed" -ForegroundColor Yellow