# IOE-DREAM 综合BOM字符清理脚本
# 确保彻底移除所有UTF-8 BOM字符

Write-Host "=== IOE-DREAM 综合BOM字符清理脚本 ===" -ForegroundColor Cyan
Write-Host "目标: 彻底移除所有Java文件的UTF-8 BOM字符" -ForegroundColor Green

# 1. 清理Maven和IDE缓存
Write-Host "`n[步骤1] 清理构建和IDE缓存..." -ForegroundColor Yellow

# 清理Maven缓存
if (Test-Path "D:\IOE-DREAM\target") {
    Write-Host "清理target目录..."
    Remove-Item -Path "D:\IOE-DREAM\target" -Recurse -Force -ErrorAction SilentlyContinue
}

# 清理IDE缓存
if (Test-Path "D:\IOE-DREAM\.idea") {
    Write-Host "清理IDE缓存..."
    Remove-Item -Path "D:\IOE-DREAM\.idea" -Recurse -Force -ErrorAction SilentlyContinue
}

if (Test-Path "D:\IOE-DREAM\.vs") {
    Remove-Item -Path "D:\IOE-DREAM\.vs" -Recurse -Force -ErrorAction SilentlyContinue
}

# 2. 检测和修复BOM字符
Write-Host "`n[步骤2] 检测和修复BOM字符..." -ForegroundColor Yellow

$bomBytes = @(0xEF, 0xBB, 0xBF)
$fixedFiles = @()
$checkedFiles = 0

# 获取所有Java文件
$javaFiles = Get-ChildItem -Path "D:\IOE-DREAM\microservices" -Recurse -Filter "*.java"
Write-Host "找到 $($javaFiles.Count) 个Java文件，开始检查BOM字符..."

foreach ($file in $javaFiles) {
    $checkedFiles++

    # 显示进度
    if ($checkedFiles % 100 -eq 0) {
        Write-Host "已检查 $checkedFiles 个文件..."
    }

    try {
        # 读取文件字节
        $bytes = [System.IO.File]::ReadAllBytes($file.FullName)

        if ($bytes.Length -ge 3) {
            # 检查是否有BOM
            $hasBom = $bytes[0] -eq $bomBytes[0] -and $bytes[1] -eq $bomBytes[1] -and $bytes[2] -eq $bomBytes[2]

            if ($hasBom) {
                Write-Host "发现BOM字符: $($file.FullName)" -ForegroundColor Red

                # 备份原文件
                $backupPath = $file.FullName + ".bom.backup"
                [System.IO.File]::Copy($file.FullName, $backupPath, $true)

                # 移除BOM并重写文件
                $contentWithoutBom = $bytes[3..($bytes.Length-1)]
                [System.IO.File]::WriteAllBytes($file.FullName, $contentWithoutBom)

                $fixedFiles += $file.FullName
                Write-Host "  ✓ 已修复并备份到 $backupPath" -ForegroundColor Green
            }
        }
    }
    catch {
        Write-Host "处理文件失败: $($file.FullName) - $($_.Exception.Message)" -ForegroundColor Red
    }
}

# 3. 验证修复结果
Write-Host "`n[步骤3] 验证修复结果..." -ForegroundColor Yellow

$verificationFailed = @()
foreach ($fixedFile in $fixedFiles) {
    try {
        $bytes = [System.IO.File]::ReadAllBytes($fixedFile)
        if ($bytes.Length -ge 3 -and $bytes[0] -eq $bomBytes[0] -and $bytes[1] -eq $bomBytes[1] -and $bytes[2] -eq $bomBytes[2]) {
            $verificationFailed += $fixedFile
        }
    }
    catch {
        Write-Host "验证失败: $fixedFile" -ForegroundColor Red
    }
}

# 4. 报告结果
Write-Host "`n=== 修复结果报告 ===" -ForegroundColor Cyan
Write-Host "总共检查: $checkedFiles 个Java文件" -ForegroundColor White
Write-Host "发现BOM字符: $($fixedFiles.Count) 个文件" -ForegroundColor White
Write-Host "成功修复: $($fixedFiles.Count - $verificationFailed.Count) 个文件" -ForegroundColor Green

if ($verificationFailed.Count -gt 0) {
    Write-Host "修复失败: $($verificationFailed.Count) 个文件" -ForegroundColor Red
    Write-Host "修复失败的文件:" -ForegroundColor Red
    foreach ($file in $verificationFailed) {
        Write-Host "  - $file" -ForegroundColor Red
    }
}

# 5. 特别检查用户提到的文件
Write-Host "`n[步骤4] 特别检查用户报告的问题文件..." -ForegroundColor Yellow

$problemFiles = @(
    "D:\IOE-DREAM\microservices\ioedream-common-service\src\main\java\net\lab1024\sa\common\organization\service\impl\SpaceCapacityServiceImpl.java",
    "D:\IOE-DREAM\microservices\ioedream-common-service\src\main\java\net\lab1024\sa\common\organization\service\impl\RegionalHierarchyServiceImpl.java",
    "D:\IOE-DREAM\microservices\ioedream-common-service\src\main\java\net\lab1024\sa\common\organization\service\impl\AreaDeviceServiceImpl.java",
    "D:\IOE-DREAM\microservices\ioedream-common-service\src\main\java\net\lab1024\sa\common\organization\controller\RegionalHierarchyController.java",
    "D:\IOE-DREAM\microservices\ioedream-common-service\src\main\java\net\lab1024\sa\common\organization\controller\AreaPermissionManageController.java"
)

foreach ($problemFile in $problemFiles) {
    if (Test-Path $problemFile) {
        try {
            $bytes = [System.IO.File]::ReadAllBytes($problemFile)
            $hasBom = $bytes.Length -ge 3 -and $bytes[0] -eq $bomBytes[0] -and $bytes[1] -eq $bomBytes[1] -and $bytes[2] -eq $bomBytes[2]

            if ($hasBom) {
                Write-Host "⚠️  仍有BOM字符: $problemFile" -ForegroundColor Red

                # 强制修复
                Write-Host "  强制修复中..." -ForegroundColor Yellow
                $backupPath = $problemFile + ".forced.backup"
                [System.IO.File]::Copy($problemFile, $backupPath, $true)

                # 读取UTF-8内容，重新写入
                $content = Get-Content -Path $problemFile -Encoding UTF8
                Set-Content -Path $problemFile -Value $content -Encoding UTF8 -NoNewline

                Write-Host "  ✓ 已强制修复，备份: $backupPath" -ForegroundColor Green
            } else {
                Write-Host "✓ 无BOM字符: $problemFile" -ForegroundColor Green
            }
        }
        catch {
            Write-Host "❌ 检查失败: $problemFile - $($_.Exception.Message)" -ForegroundColor Red
        }
    } else {
        Write-Host "⚠️  文件不存在: $problemFile" -ForegroundColor Yellow
    }
}

# 6. 建议
Write-Host "`n=== 建议操作 ===" -ForegroundColor Cyan
Write-Host "1. 重新启动IDE以清除缓存" -ForegroundColor White
Write-Host "2. 执行 Maven 清理构建: mvn clean compile -DskipTests" -ForegroundColor White
Write-Host "3. 如果仍有问题，检查文件编码设置" -ForegroundColor White

Write-Host "`n脚本执行完成!" -ForegroundColor Green
Write-Host "修复的文件已备份，如有问题可从备份文件恢复" -ForegroundColor Yellow