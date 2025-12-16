# =====================================================
# 技术栈一致性修复脚本
# 版本: v1.0.0
# 描述: 修复文档中的技术栈不一致问题
# 创建时间: 2025-01-30
# =====================================================

param(
    [Parameter(Mandatory=$false)]
    [switch]$DryRun,  # 仅显示将要修复的内容（移除默认值）

    [Parameter(Mandatory=$false)]
    [string]$TargetPath = "."
)

$ErrorActionPreference = "Stop"

# 技术栈标准配置（使用变量）
$script:techStackConfig = @{
    "Spring Boot" = "3.5.8"
    "Java" = "17"
    "Vue" = "3.4"
    "MyBatis-Plus" = "3.5.0"
}

# 修复文件中的技术栈信息
function Update-TechnologyStackInFile {
    param(
        [Parameter(Mandatory=$true)]
        [string]$FilePath
    )

    if (-not (Test-Path $FilePath)) {
        Write-Host "[ERROR] 文件不存在: $FilePath" -ForegroundColor Red
        return $false
    }

    try {
        # 读取文件内容
        $content = Get-Content $FilePath -Raw -Encoding UTF8

        # 使用不同的变量名避免与自动变量冲突
        $matchResults = [regex]::Matches($content, "Spring Boot\s+(\d+\.\d+\.\d+)")

        if ($matchResults.Count -gt 0) {
            Write-Host "[INFO] 在文件 $FilePath 中发现技术栈版本信息" -ForegroundColor Yellow

            # 修复逻辑
            $modified = $false
            foreach ($match in $matchResults) {
                $currentVersion = $match.Groups[1].Value
                if ($currentVersion -ne $script:techStackConfig["Spring Boot"]) {
                    $content = $content -replace "Spring Boot\s+$currentVersion", "Spring Boot $($script:techStackConfig['Spring Boot'])"
                    $modified = $true
                }
            }

            if ($modified) {
                if ($DryRun) {
                    Write-Host "[DRY-RUN] 将更新文件: $FilePath" -ForegroundColor Cyan
                } else {
                    # 使用 UTF-8 without BOM 编码保存
                    $utf8NoBom = New-Object System.Text.UTF8Encoding $false
                    [System.IO.File]::WriteAllText($FilePath, $content, $utf8NoBom)
                    Write-Host "[OK] 已更新文件: $FilePath" -ForegroundColor Green
                }
                return $true
            }
        }

        return $false
    } catch {
        # 使用不同的变量名避免与只读自动变量 error 冲突
        $errorMessage = $_.Exception.Message
        Write-Host "[ERROR] 处理文件失败: $FilePath, 错误: $errorMessage" -ForegroundColor Red
        return $false
    }
}

# 主函数
function Main {
    Write-Host "=====================================================" -ForegroundColor Green
    Write-Host "技术栈一致性修复工具" -ForegroundColor Green
    Write-Host "=====================================================" -ForegroundColor Green

    if ($DryRun) {
        Write-Host "[模式] 干运行模式 - 仅显示将要修复的内容" -ForegroundColor Yellow
    }

    # 查找所有 Markdown 文件
    $markdownFiles = Get-ChildItem -Path $TargetPath -Filter "*.md" -Recurse -ErrorAction SilentlyContinue

    $fixedCount = 0
    $totalCount = $markdownFiles.Count

    Write-Host "[INFO] 找到 $totalCount 个 Markdown 文件" -ForegroundColor Cyan

    foreach ($file in $markdownFiles) {
        if (Update-TechnologyStackInFile -FilePath $file.FullName) {
            $fixedCount++
        }
    }

    Write-Host "=====================================================" -ForegroundColor Green
    Write-Host "修复完成: $fixedCount / $totalCount 个文件" -ForegroundColor Green
    Write-Host "=====================================================" -ForegroundColor Green
}

# 执行主函数
Main
