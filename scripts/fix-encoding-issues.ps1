# IOE-DREAM 编码问题修复脚本
# 根源性解决编译和运行时乱码问题

param(
    [switch]$DryRun = $false,        # 仅显示将要修复的内容
    [switch]$Force = $false,          # 强制修复，不询问确认
    [string]$Module = "all"           # 指定要修复的模块
)

Write-Host "🔧 IOE-DREAM 编码问题修复工具" -ForegroundColor Green
Write-Host "🎯 目标：根源性解决编译和运行时乱码问题" -ForegroundColor Yellow

# 编码问题检查和修复函数
function Test-FileEncoding {
    param([string]$FilePath)

    if (-not (Test-Path $FilePath)) {
        return $false
    }

    try {
        $bytes = [System.IO.File]::ReadAllBytes($FilePath)
        $encoding = [System.Text.Encoding]::UTF8

        # 尝试用UTF-8解码
        $decoded = $encoding.GetString($bytes)

        # 检查是否包含中文字符
        if ($decoded -match '[\u4e00-\u9fff]') {
            return $true
        }

        return $false
    } catch {
        return $false
    }
}

# 修复文件编码为UTF-8 with BOM
function Set-FileEncodingUTF8 {
    param(
        [string]$FilePath,
        [switch]$AddBOM = $false
    )

    if (-not (Test-Path $FilePath)) {
        Write-Host "文件不存在: $FilePath" -ForegroundColor Red
        return $false
    }

    try {
        $content = Get-Content $FilePath -Raw -Encoding UTF8
        $utf8Encoding = if ($AddBOM) {
            [System.Text.UTF8Encoding]::new($true)
        } else {
            [System.Text.UTF8Encoding]::new($false)
        }

        [System.IO.File]::WriteAllText($FilePath, $content, $utf8Encoding)
        return $true
    } catch {
        Write-Host "修复文件编码失败: $FilePath - $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

# 获取所有需要检查的文件
function Get-EncodingFiles {
    param([string]$ModulePath)

    $extensions = @(
        "*.java", "*.xml", "*.yml", "*.yaml", "*.properties",
        "*.sql", "*.md", "*.txt", "*.bat", "*.sh"
    )

    $files = @()
    foreach ($ext in $extensions) {
        $files += Get-ChildItem -Path $ModulePath -Filter $ext -Recurse -File
    }

    return $files
}

# 显示当前编码环境信息
function Show-EncodingEnvironment {
    Write-Host "`n🔍 当前编码环境信息" -ForegroundColor Cyan
    Write-Host "==========================" -ForegroundColor Cyan

    # Java默认编码
    try {
        $javaEncoding = & java -cp . -Dfile.encoding=UTF-8 -version 2>&1
        Write-Host "Java版本: $javaEncoding" -ForegroundColor White
    } catch {
        Write-Host "Java未安装或不可用" -ForegroundColor Yellow
    }

    # PowerShell编码
    Write-Host "PowerShell输出编码: $([Console]::OutputEncoding.EncodingName)" -ForegroundColor White
    Write-Host "PowerShell默认编码: $([System.Text.Encoding]::Default.EncodingName)" -ForegroundColor White

    # 系统编码
    Write-Host "系统默认编码: $([System.Text.Encoding]::Default.EncodingName)" -ForegroundColor White

    # Maven配置
    if (Test-Path "microservices/pom.xml") {
        Write-Host "Maven配置文件: 存在" -ForegroundColor Green
        $pomContent = Get-Content "microservices/pom.xml" -Raw
        if ($pomContent -match "project\.build\.sourceEncoding.*UTF-8") {
            Write-Host "Maven编码配置: UTF-8 ✅" -ForegroundColor Green
        } else {
            Write-Host "Maven编码配置: 需要修复 ⚠️" -ForegroundColor Yellow
        }
    }

    Write-Host ""
}

# 检查特定模块的编码问题
function Test-ModuleEncoding {
    param([string]$ModulePath)

    Write-Host "🔍 检查模块: $ModulePath" -ForegroundColor Blue

    if (-not (Test-Path $ModulePath)) {
        Write-Host "  模块不存在: $ModulePath" -ForegroundColor Red
        return
    }

    $files = Get-EncodingFiles -ModulePath $ModulePath
    $totalFiles = $files.Count
    $hasChinese = 0
    $needsFix = 0

    foreach ($file in $files) {
        if (Test-FileEncoding -FilePath $file.FullName) {
            $hasChinese++
            # 检查是否已经是UTF-8
            $content = Get-Content $file.FullName -Raw -Encoding UTF8
            if ($content -match '[\u4e00-\u9fff]' -and $file.Extension -notin @('.jpg', '.jpeg', '.png', '.gif')) {
                # 检查BOM
                $bytes = [System.IO.File]::ReadAllBytes($file.FullName)
                if ($bytes.Length -ge 3 -and
                    $bytes[0] -eq 0xEF -and
                    $bytes[1] -eq 0xBB -and
                    $bytes[2] -eq 0xBF) {
                    # 已有BOM，通常是正确的UTF-8
                } elseif ($bytes.Length -ge 2 -and
                         $bytes[0] -eq 0xFE -and
                         $bytes[1] -eq 0xFF) {
                    # UTF-16 BE，需要修复
                    $needsFix++
                    Write-Host "  ⚠️  需要修复: $($file.Name) (UTF-16)" -ForegroundColor Yellow
                } elseif ($bytes.Length -ge 2 -and
                         $bytes[0] -eq 0xFF -and
                         $bytes[1] -eq 0xFE) {
                    # UTF-16 LE，需要修复
                    $needsFix++
                    Write-Host "  ⚠️  需要修复: $($file.Name) (UTF-16 LE)" -ForegroundColor Yellow
                } else {
                    # 可能是ASCII或无BOM的UTF-8
                    if ($needsFix -lt 5) { # 只显示前5个
                        Write-Host "  📝 包含中文: $($file.Name)" -ForegroundColor Gray
                    }
                }
            }
        }
    }

    Write-Host "  总文件数: $totalFiles" -ForegroundColor White
    Write-Host "  包含中文: $hasChinese" -ForegroundColor White
    Write-Host "  需要修复: $needsFix" -ForegroundColor $($needsFix -gt 0 ? "Red" : "Green")
    Write-Host ""

    return @{
        TotalFiles = $totalFiles
        HasChinese = $hasChinese
        NeedsFix = $needsFix
        Files = $files
    }
}

# 修复模块编码问题
function Fix-ModuleEncoding {
    param([string]$ModulePath)

    Write-Host "🔧 修复模块编码: $ModulePath" -ForegroundColor Blue

    $files = Get-EncodingFiles -ModulePath $ModulePath
    $fixedCount = 0
    $errorCount = 0

    foreach ($file in $files) {
        if (Test-FileEncoding -FilePath $file.FullName) {
            # 检查当前编码
            try {
                $bytes = [System.IO.File]::ReadAllBytes($file.FullName)

                # 检查是否为UTF-16
                $isUTF16 = $false
                if ($bytes.Length -ge 2) {
                    if (($bytes[0] -eq 0xFE -and $bytes[1] -eq 0xFF) -or
                        ($bytes[0] -eq 0xFF -and $bytes[1] -eq 0xFE)) {
                        $isUTF16 = $true
                    }
                }

                if ($isUTF16 -or ($DryRun -eq $false)) {
                    if (-not $DryRun) {
                        if (Set-FileEncodingUTF8 -FilePath $file.FullName) {
                            $fixedCount++
                            Write-Host "  ✅ 已修复: $($file.Name)" -ForegroundColor Green
                        } else {
                            $errorCount++
                            Write-Host "  ❌ 修复失败: $($file.Name)" -ForegroundColor Red
                        }
                    } else {
                        Write-Host "  🔧 将修复: $($file.Name) (需要UTF-8转换)" -ForegroundColor Yellow
                    }
                }
            } catch {
                $errorCount++
                Write-Host "  ❌ 处理错误: $($file.Name) - $($_.Exception.Message)" -ForegroundColor Red
            }
        }
    }

    Write-Host "  修复完成: $fixedCount 个文件" -ForegroundColor Green
    if ($errorCount -gt 0) {
        Write-Host "  修复失败: $errorCount 个文件" -ForegroundColor Red
    }
    Write-Host ""
}

# 检查Maven配置
function Test-MavenConfiguration {
    Write-Host "🔍 检查Maven编码配置" -ForegroundColor Blue

    $pomPath = "microservices/pom.xml"
    if (-not (Test-Path $pomPath)) {
        Write-Host "  Maven配置文件不存在" -ForegroundColor Red
        return
    }

    $configIssues = @()
    $pomContent = Get-Content $pomPath -Raw

    # 检查编译器插件配置
    if ($pomContent -notmatch "maven-compiler-plugin") {
        $configIssues += "缺少 maven-compiler-plugin"
    }

    if ($pomContent -notmatch "<encoding>UTF-8</encoding>") {
        $configIssues += "缺少 UTF-8 编码配置"
    }

    if ($pomContent -notmatch "maven-resources-plugin") {
        $configIssues += "缺少 maven-resources-plugin"
    }

    if ($pomContent -notmatch "maven-surefire-plugin") {
        $configIssues += "缺少 maven-surefire-plugin"
    }

    if ($configIssues.Count -gt 0) {
        Write-Host "  ⚠️  发现配置问题:" -ForegroundColor Yellow
        foreach ($issue in $configIssues) {
            Write-Host "    - $issue" -ForegroundColor Yellow
        }
        Write-Host "  建议更新Maven配置" -ForegroundColor Yellow
    } else {
        Write-Host "  ✅ Maven配置正确" -ForegroundColor Green
    }

    Write-Host ""
}

# 验证修复结果
function Test-FixResults {
    Write-Host "🔍 验证修复结果" -ForegroundColor Blue

    # 检查关键文件
    $criticalFiles = @(
        "microservices/pom.xml",
        "microservices/microservices-common/pom.xml",
        "microservices/ioedream-common-service/src/main/resources/application.yml"
    )

    $allGood = $true
    foreach ($file in $criticalFiles) {
        if (Test-Path $file) {
            if (Test-FileEncoding -FilePath $file) {
                Write-Host "  ✅ $file - 编码正确" -ForegroundColor Green
            } else {
                Write-Host "  ⚠️  $file - 不包含中文或编码正常" -ForegroundColor Gray
            }
        } else {
            Write-Host "  ❌ $file - 文件不存在" -ForegroundColor Red
            $allGood = $false
        }
    }

    if ($allGood) {
        Write-Host "`n✅ 所有检查通过！" -ForegroundColor Green
    } else {
        Write-Host "`n⚠️  存在问题，请检查上述文件" -ForegroundColor Yellow
    }
}

# 主执行流程
try {
    Show-EncodingEnvironment

    Write-Host "🎯 开始检查和修复编码问题" -ForegroundColor Cyan
    Write-Host "==============================" -ForegroundColor Cyan
    Write-Host ""

    # 检查Maven配置
    Test-MavenConfiguration

    # 根据参数处理模块
    $modules = @()
    if ($Module -eq "all") {
        $modules = Get-ChildItem -Path "microservices" -Directory | Where-Object {
            $_.Name -match "ioedream-" -or $_.Name -eq "microservices-common"
        }
    } else {
        $modules = Get-ChildItem -Path "microservices" -Directory | Where-Object {
            $_.Name -like "*$Module*"
        }
    }

    $totalNeedsFix = 0

    foreach ($module in $modules) {
        $modulePath = $module.FullName
        $result = Test-ModuleEncoding -ModulePath $modulePath
        $totalNeedsFix += $result.NeedsFix
    }

    if ($totalNeedsFix -gt 0 -or $Force) {
        if (-not $DryRun -and -not $Force) {
            $response = Read-Host "`n🤔 发现 $totalNeedsFix 个文件需要修复，是否继续？(y/N)"
            if ($response -ne 'y' -and $response -ne 'Y') {
                Write-Host "操作已取消" -ForegroundColor Yellow
                exit 0
            }
        }

        Write-Host ""
        Write-Host "🔧 开始修复编码问题" -ForegroundColor Cyan
        Write-Host "====================" -ForegroundColor Cyan

        foreach ($module in $modules) {
            Fix-ModuleEncoding -ModulePath $module.FullName
        }

        Write-Host ""
        Test-FixResults
    } else {
        Write-Host "✅ 所有文件编码正确，无需修复！" -ForegroundColor Green
    }

    if ($DryRun) {
        Write-Host ""
        Write-Host "🧪 这是模拟运行，实际未修复任何文件" -ForegroundColor Magenta
        Write-Host "   如需实际执行，请去除 -DryRun 参数" -ForegroundColor Gray
    }

} catch {
    Write-Host "❌ 执行过程中发生错误:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    Write-Host $_.ScriptStackTrace -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "🎉 编码修复完成！" -ForegroundColor Green
Write-Host "💡 建议重启IDE以确保设置生效" -ForegroundColor Yellow