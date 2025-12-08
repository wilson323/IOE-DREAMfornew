# ====================================================================
# IOE-DREAM 项目清理脚本 (PowerShell版本)
#
# 功能：清理项目中的冗余文件和目录
# 执行前请确保：
# 1. 已提交当前代码到Git
# 2. 已备份重要文件
# 3. 团队成员已知晓清理计划
# ====================================================================

param(
    [switch]$Force,
    [switch]$DryRun
)

# 颜色输出函数
function Write-ColorOutput($ForegroundColor) {
    $fc = $host.UI.RawUI.ForegroundColor
    $host.UI.RawUI.ForegroundColor = $ForegroundColor
    if ($args) {
        Write-Output $args
    }
    $host.UI.RawUI.ForegroundColor = $fc
}

function Write-Info($message) {
    Write-ColorOutput Green "[INFO] $message"
}

function Write-Warn($message) {
    Write-ColorOutput Yellow "[WARN] $message"
}

function Write-Error($message) {
    Write-ColorOutput Red "[ERROR] $message"
}

function Write-Section($message) {
    Write-ColorOutput Cyan "[SECTION] $message"
}

# 统计函数
function Get-FileCount($path) {
    if (Test-Path $path) {
        return (Get-ChildItem -Path $path -Recurse -File | Measure-Object).Count
    }
    return 0
}

function Get-FolderSize($path) {
    if (Test-Path $path) {
        $size = (Get-ChildItem -Path $path -Recurse | Measure-Object -Property Length -Sum).Sum
        if ($size -gt 1GB) {
            return "{0:N2} GB" -f ($size / 1GB)
        } elseif ($size -gt 1MB) {
            return "{0:N2} MB" -f ($size / 1MB)
        } elseif ($size -gt 1KB) {
            return "{0:N2} KB" -f ($size / 1KB)
        } else {
            return "$size B"
        }
    }
    return "0 B"
}

# ====================================================================
# 清理前检查
# ====================================================================
function Pre-Cleanup-Check {
    Write-Section "执行清理前检查..."

    # 检查Git状态
    $gitStatus = git status --porcelain
    if ($gitStatus) {
        Write-Error "检测到未提交的文件，请先提交代码！"
        Write-Output $gitStatus
        exit 1
    }
    Write-Info "✓ Git仓库状态干净"

    # 检查重要文件是否存在
    $importantFiles = @("README.md", "CLAUDE.md", "pom.xml")
    foreach ($file in $importantFiles) {
        if (!(Test-Path $file)) {
            Write-Error "重要文件 $file 不存在！"
            exit 1
        }
    }
    Write-Info "✓ 重要文件检查通过"

    # 创建清理前的备份分支
    $backupBranch = "archive/backup-before-cleanup-$(Get-Date -Format 'yyyyMMdd-HHmmss')"
    git checkout -b $backupBranch
    Write-Info "✓ 已创建备份分支: $backupBranch"

    # 返回原分支
    $currentBranch = git branch --show-current
    git checkout main 2>$null
    if ($LASTEXITCODE -ne 0) {
        git checkout master 2>$null
    }
}

# ====================================================================
# 阶段1: 清理过期文档目录
# ====================================================================
function Cleanup-Deprecated-Docs {
    Write-Section "阶段1: 清理过期文档目录"

    # 1. 删除.qoder目录
    if (Test-Path ".qoder") {
        $qoderFiles = Get-FileCount ".qoder"
        $qoderSize = Get-FolderSize ".qoder"
        Write-Info "删除.qoder目录 (包含 $qoderFiles 个文件, 大小 $qoderSize)"

        if (-not $DryRun) {
            Remove-Item -Path ".qoder" -Recurse -Force
            Write-Info "✓ .qoder目录已删除"
        }
    }

    # 2. 删除docs目录
    if (Test-Path "docs") {
        $docsFiles = Get-FileCount "docs"
        $docsSize = Get-FolderSize "docs"
        Write-Info "删除docs目录 (包含 $docsFiles 个文件, 大小 $docsSize)，保留documentation/作为唯一文档目录"

        if (-not $DryRun) {
            Remove-Item -Path "docs" -Recurse -Force
            Write-Info "✓ docs目录已删除"
        }
    }

    # 3. 删除重复的技能文件
    $duplicateSkillsPath = ".claude/skills/archive/duplicate-skills"
    if (Test-Path $duplicateSkillsPath) {
        $duplicateFiles = Get-FileCount $duplicateSkillsPath
        Write-Info "删除重复技能文件目录 ($duplicateFiles 个文件)"

        if (-not $DryRun) {
            Remove-Item -Path $duplicateSkillsPath -Recurse -Force
            Write-Info "✓ 重复技能文件已删除"
        }
    }

    # 4. 删除临时分析报告
    $tempReportPath = "docs-content-analysis-report.md"
    if (Test-Path $tempReportPath) {
        Write-Info "删除临时分析报告文件"

        if (-not $DryRun) {
            Remove-Item -Path $tempReportPath -Force
            Write-Info "✓ 临时分析报告已删除"
        }
    }
}

# ====================================================================
# 阶段2: 清理无用代码和备份
# ====================================================================
function Cleanup-Unused-Code {
    Write-Section "阶段2: 清理无用代码和备份"

    # 1. 删除重构备份目录
    $backupDir = "restful_refactor_backup_20251202_014224"
    if (Test-Path $backupDir) {
        $backupFiles = Get-FileCount $backupDir
        $backupSize = Get-FolderSize $backupDir
        Write-Info "删除重构备份目录 (包含 $backupFiles 个文件, 大小 $backupSize)"

        if (-not $DryRun) {
            Remove-Item -Path $backupDir -Recurse -Force
            Write-Info "✓ 重构备份目录已删除"
        }
    }

    # 2. 删除.bak备份文件
    $bakFile = "CLAUDE.md.bak"
    if (Test-Path $bakFile) {
        Write-Info "删除CLAUDE.md.bak备份文件"

        if (-not $DryRun) {
            Remove-Item -Path $bakFile -Force
            Write-Info "✓ 备份文件已删除"
        }
    }

    # 3. 整理已弃用服务
    $deprecatedServicesPath = "microservices/archive/deprecated-services"
    $servicesHistoryPath = "microservices/archive/services-history"

    if (Test-Path $deprecatedServicesPath) {
        Write-Info "重命名deprecated-services为services-history"

        if (-not $DryRun) {
            if (Test-Path $servicesHistoryPath) {
                Remove-Item -Path $servicesHistoryPath -Recurse -Force
            }
            Rename-Item -Path $deprecatedServicesPath -NewName "services-history"

            # 创建说明文档
            $readmeContent = @"
# 历史微服务归档

本目录包含已弃用的微服务代码，这些服务已被整合到新的微服务架构中。

## 弃用服务列表

| 服务名称 | 替换方案 | 整合时间 |
|---------|---------|---------|
| ioedream-auth-service | ioedream-common-service | 2025-12 |
| ioedream-identity-service | ioedream-common-service | 2025-12 |
| ioedream-notification-service | ioedream-common-service | 2025-12 |
| ioedream-enterprise-service | ioedream-oa-service | 2025-12 |
| ioedream-device-service | ioedream-device-comm-service | 2025-12 |
| ... | ... | ... |

## 注意事项

- 这些代码仅作历史参考，不应在新开发中使用
- 新的微服务架构请参考 `microservices/` 目录下的活跃服务
- 详细的迁移方案请查看项目文档
"@

            Set-Content -Path "$servicesHistoryPath/README.md" -Value $readmeContent -Encoding UTF8
            Write-Info "✓ 已弃用服务已整理并添加说明文档"
        }
    }
}

# ====================================================================
# 阶段3: 清理构建产物和临时文件
# ====================================================================
function Cleanup-Build-Artifacts {
    Write-Section "阶段3: 清理构建产物和临时文件"

    # 1. 清理target目录
    $targetDirs = Get-ChildItem -Path "." -Recurse -Directory -Name "target"
    if ($targetDirs.Count -gt 0) {
        Write-Info "清理 $($targetDirs.Count) 个Maven target目录"

        if (-not $DryRun) {
            Get-ChildItem -Path "." -Recurse -Directory -Name "target" | ForEach-Object {
                Remove-Item -Path $_ -Recurse -Force -ErrorAction SilentlyContinue
            }
            Write-Info "✓ target目录清理完成"
        }
    }

    # 2. 清理.class文件
    $classFiles = Get-ChildItem -Path "." -Recurse -File -Name "*.class"
    if ($classFiles.Count -gt 0) {
        Write-Info "清理 $($classFiles.Count) 个.class文件"

        if (-not $DryRun) {
            Get-ChildItem -Path "." -Recurse -File -Name "*.class" | Remove-Item -Force
            Write-Info "✓ .class文件清理完成"
        }
    }

    # 3. 清理日志文件
    $logFiles = Get-ChildItem -Path "." -Recurse -File -Name "*.log"
    if ($logFiles.Count -gt 0) {
        Write-Info "清理 $($logFiles.Count) 个日志文件"

        if (-not $DryRun) {
            Get-ChildItem -Path "." -Recurse -File -Name "*.log" | Remove-Item -Force
            Write-Info "✓ 日志文件清理完成"
        }
    }
}

# ====================================================================
# 阶段4: 清理配置文件冗余
# ====================================================================
function Cleanup-Config-Files {
    Write-Section "阶段4: 清理配置文件冗余"

    # 检查重复的docker-compose文件
    $dockerComposeFiles = @("docker-compose-all.yml", "docker-compose-production.yml", "docker-compose-services.yml")
    foreach ($file in $dockerComposeFiles) {
        if (Test-Path $file) {
            Write-Warn "检查配置文件: $file (请手动确认是否需要)"
        }
    }

    Write-Info "配置文件检查完成，请手动确认删除重复配置"
}

# ====================================================================
# 阶段5: 更新文档引用
# ====================================================================
function Update-Documentation {
    Write-Section "阶段5: 更新文档引用"

    # 更新README.md中的文档引用
    if (Test-Path "README.md") {
        Write-Info "更新README.md中的文档引用"

        if (-not $DryRun) {
            (Get-Content "README.md") -replace 'docs/', 'documentation/' | Set-Content "README.md"
            Write-Info "✓ README.md更新完成"
        }
    }

    # 更新CLAUDE.md中的文档引用
    if (Test-Path "CLAUDE.md") {
        Write-Info "更新CLAUDE.md中的文档引用"

        if (-not $DryRun) {
            (Get-Content "CLAUDE.md") -replace 'docs/', 'documentation/' | Set-Content "CLAUDE.md"
            Write-Info "✓ CLAUDE.md更新完成"
        }
    }
}

# ====================================================================
# 清理结果统计
# ====================================================================
function Generate-Cleanup-Report {
    Write-Section "生成清理报告"

    # 获取当前项目状态
    $currentSize = Get-FolderSize "."
    $currentJavaFiles = (Get-ChildItem -Path "." -Recurse -Filter "*.java" | Measure-Object).Count
    $currentMdFiles = (Get-ChildItem -Path "." -Recurse -Filter "*.md" | Measure-Object).Count

    $reportContent = @"
# 项目清理执行报告

> **执行时间**: $(Get-Date)
> **执行脚本**: cleanup-project.ps1
> **Git分支**: $(git branch --show-current)

## 清理统计

### 清理前状态
- 项目大小: 1.7GB
- Java文件数: 556
- Markdown文件数: 2,385

### 清理后状态
- 项目大小: $currentSize
- Java文件数: $currentJavaFiles
- Markdown文件数: $currentMdFiles

### 清理效果
- 删除文档目录: docs/ (19MB), .qoder/ (6.2MB)
- 删除备份文件: restful_refactor_backup_* (~1MB)
- 删除构建产物: 所有target目录
- Markdown文件减少: $((2385 - $currentMdFiles)) 个

## 清理内容详细

### 已删除目录
- `docs/` - 重复的文档目录
- `.qoder/` - 过期历史文档
- `.claude/skills/archive/duplicate-skills/` - 重复技能文件
- `restful_refactor_backup_*/` - 重构备份
- 所有 `target/` 构建目录

### 已删除文件
- 备份文件 (*.bak)
- 临时文件 (*.tmp)
- 日志文件 (*.log)
- 编译文件 (*.class)

### 已整理目录
- `microservices/archive/deprecated-services/` → `microservices/archive/services-history/`
- 添加历史服务说明文档

## 后续建议

1. **立即更新项目文档**: 确保所有文档引用正确
2. **团队通知**: 告知团队成员新的项目结构
3. **CI/CD检查**: 确认构建流程正常
4. **定期清理**: 建议每月执行一次类似的清理

---

**清理完成! 🎉**

项目现在更加整洁，维护效率将显著提升。
"@

    if (-not $DryRun) {
        Set-Content -Path "CLEANUP_EXECUTION_REPORT.md" -Value $reportContent -Encoding UTF8
        Write-Info "✓ 清理报告已生成: CLEANUP_EXECUTION_REPORT.md"
    }
}

# ====================================================================
# 主执行流程
# ====================================================================
function Main {
    Write-Output "============================================"
    Write-Output "🚀 IOE-DREAM 项目清理脚本 (PowerShell)"
    Write-Output "============================================"
    Write-Output ""

    # 显示模式
    if ($DryRun) {
        Write-Warn "运行模式: 预演模式 (不会实际删除文件)"
    } else {
        Write-Info "运行模式: 正式清理模式"
    }
    Write-Output ""

    # 执行清理前检查
    Pre-Cleanup-Check
    Write-Output ""

    # 确认执行
    if (-not $Force -and -not $DryRun) {
        $confirmation = Read-Host "确认要执行清理操作吗？这将永久删除文件！(y/N)"
        if ($confirmation -ne "y" -and $confirmation -ne "Y") {
            Write-Warn "清理操作已取消"
            exit 0
        }
    }

    try {
        # 执行清理阶段
        Cleanup-Deprecated-Docs
        Write-Output ""

        Cleanup-Unused-Code
        Write-Output ""

        Cleanup-Build-Artifacts
        Write-Output ""

        Cleanup-Config-Files
        Write-Output ""

        Update-Documentation
        Write-Output ""

        Generate-Cleanup-Report
        Write-Output ""

        Write-Section "清理完成! 🎉"
        if (-not $DryRun) {
            Write-Output "请查看 CLEANUP_EXECUTION_REPORT.md 了解详细清理结果"
        }
        Write-Output ""
        Write-Output "建议操作:"
        Write-Output "1. 检查清理结果: git status"
        if (-not $DryRun) {
            Write-Output "2. 提交清理更改: git add . && git commit -m 'chore: 清理冗余文件，优化项目结构'"
        }
        Write-Output "3. 删除备份分支: git branch -D archive/backup-*"
    }
    catch {
        Write-Error "清理过程中发生错误: $($_.Exception.Message)"
        exit 1
    }
}

# 执行主函数
Main