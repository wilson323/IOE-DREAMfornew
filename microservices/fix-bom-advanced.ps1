# 高级BOM修复脚本 - 使用.NET框架确保彻底清除BOM

Add-Type -AssemblyName System.Text

function Remove-BomFromFile {
    param(
        [string]$FilePath
    )

    try {
        # 读取文件的字节内容
        $bytes = [System.IO.File]::ReadAllBytes($FilePath)

        # 检查并移除BOM
        if ($bytes.Length -ge 3 -and
            $bytes[0] -eq 0xEF -and
            $bytes[1] -eq 0xBB -and
            $bytes[2] -eq 0xBF) {

            # 移除BOM并重写文件
            $contentWithoutBom = $bytes[3..($bytes.Length-1)]
            [System.IO.File]::WriteAllBytes($FilePath, $contentWithoutBom)
            return $true
        }
        return $false
    }
    catch {
        Write-Warning "处理文件失败: $FilePath - $($_.Exception.Message)"
        return $false
    }
}

function Fix-JavaFile {
    param(
        [string]$FilePath
    )

    try {
        # 读取文件内容（无BOM）
        $content = [System.IO.File]::ReadAllText($FilePath, [System.Text.Encoding]::UTF8)

        # 修复package声明
        if ($content.StartsWith("ackage")) {
            $content = "p" + $content
        }

        # 修复import声明
        $content = $content -replace '(?m)^iimport', 'import'

        # 写回文件（确保无BOM）
        $utf8WithoutBom = New-Object System.Text.UTF8Encoding($false)
        [System.IO.File]::WriteAllText($FilePath, $content, $utf8WithoutBom)

        return $true
    }
    catch {
        Write-Warning "修复Java文件失败: $FilePath - $($_.Exception.Message)"
        return $false
    }
}

Write-Host "=== 高级BOM修复工具 ===" -ForegroundColor Cyan
Write-Host "正在扫描并修复所有微服务中的BOM字符问题..." -ForegroundColor Yellow

# 获取所有微服务目录
$serviceDirs = Get-ChildItem -Path "microservices" -Directory | Where-Object { $_.Name -match "^ioedream-" }

$totalFilesFixed = 0
$totalServices = $serviceDirs.Count

foreach ($serviceDir in $serviceDirs) {
    $serviceName = $serviceDir.Name
    Write-Host "`n处理服务: $serviceName" -ForegroundColor Green

    # 获取所有Java文件
    $javaFiles = Get-ChildItem -Path $serviceDir.FullName -Recurse -Filter "*.java"
    $filesFixed = 0

    foreach ($javaFile in $javaFiles) {
        $removedBom = Remove-BomFromFile -FilePath $javaFile.FullName
        if ($removedBom) {
            $filesFixed++
        }

        $fixedContent = Fix-JavaFile -FilePath $javaFile.FullName
        if ($fixedContent) {
            $filesFixed++
        }
    }

    Write-Host "  ✓ 修复了 $filesFixed 个文件" -ForegroundColor Cyan
    $totalFilesFixed += $filesFixed
}

Write-Host "`n=== 修复完成 ===" -ForegroundColor Green
Write-Host "总计修复文件数: $totalFilesFixed" -ForegroundColor Yellow
Write-Host "处理服务数: $totalServices" -ForegroundColor Yellow