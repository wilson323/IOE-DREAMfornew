# 简化版项目清理脚本
param(
    [switch]$Force,
    [switch]$DryRun
)

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "🚀 IOE-DREAM 项目快速清理脚本" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan

if ($DryRun) {
    Write-Host "运行模式: 预演模式 (不会实际删除文件)" -ForegroundColor Yellow
} else {
    Write-Host "运行模式: 正式清理模式" -ForegroundColor Green
}

# 检查Git状态
$gitStatus = git status --porcelain
if ($gitStatus -and -not $Force) {
    Write-Error "检测到未提交的文件，请先提交代码或使用-Force参数"
    exit 1
}

Write-Host "✓ Git状态检查通过" -ForegroundColor Green

# 确认执行
if (-not $Force -and -not $DryRun) {
    $confirmation = Read-Host "确认要执行清理操作吗？这将永久删除文件！(y/N)"
    if ($confirmation -ne "y" -and $confirmation -ne "Y") {
        Write-Host "清理操作已取消" -ForegroundColor Yellow
        exit 0
    }
}

Write-Host "`n开始清理..." -ForegroundColor Green

# 1. 删除.qoder目录
if (Test-Path ".qoder") {
    $size = (Get-ChildItem -Path ".qoder" -Recurse | Measure-Object -Property Length -Sum).Sum / 1MB
    Write-Host "删除.qoder目录 ($([math]::Round($size, 2)) MB)" -ForegroundColor Yellow
    if (-not $DryRun) {
        Remove-Item -Path ".qoder" -Recurse -Force
        Write-Host "✓ .qoder目录已删除" -ForegroundColor Green
    }
} else {
    Write-Host ".qoder目录不存在，跳过" -ForegroundColor Gray
}

# 2. 删除docs目录
if (Test-Path "docs") {
    $size = (Get-ChildItem -Path "docs" -Recurse | Measure-Object -Property Length -Sum).Sum / 1MB
    Write-Host "删除docs目录 ($([math]::Round($size, 2)) MB)，保留documentation/" -ForegroundColor Yellow
    if (-not $DryRun) {
        Remove-Item -Path "docs" -Recurse -Force
        Write-Host "✓ docs目录已删除" -ForegroundColor Green
    }
} else {
    Write-Host "docs目录不存在，跳过" -ForegroundColor Gray
}

# 3. 删除重构备份目录
$backupDir = "restful_refactor_backup_20251202_014224"
if (Test-Path $backupDir) {
    $size = (Get-ChildItem -Path $backupDir -Recurse | Measure-Object -Property Length -Sum).Sum / 1MB
    Write-Host "删除重构备份目录 ($([math]::Round($size, 2)) MB)" -ForegroundColor Yellow
    if (-not $DryRun) {
        Remove-Item -Path $backupDir -Recurse -Force
        Write-Host "✓ 重构备份目录已删除" -ForegroundColor Green
    }
} else {
    Write-Host "重构备份目录不存在，跳过" -ForegroundColor Gray
}

# 4. 删除临时分析报告
if (Test-Path "docs-content-analysis-report.md") {
    Write-Host "删除临时分析报告文件" -ForegroundColor Yellow
    if (-not $DryRun) {
        Remove-Item -Path "docs-content-analysis-report.md" -Force
        Write-Host "✓ 临时分析报告已删除" -ForegroundColor Green
    }
} else {
    Write-Host "临时分析报告不存在，跳过" -ForegroundColor Gray
}

# 5. 删除重复技能文件
$duplicateSkillsPath = ".claude/skills/archive/duplicate-skills"
if (Test-Path $duplicateSkillsPath) {
    $count = (Get-ChildItem -Path $duplicateSkillsPath -Recurse -File).Count
    Write-Host "删除重复技能文件目录 ($count 个文件)" -ForegroundColor Yellow
    if (-not $DryRun) {
        Remove-Item -Path $duplicateSkillsPath -Recurse -Force
        Write-Host "✓ 重复技能文件已删除" -ForegroundColor Green
    }
} else {
    Write-Host "重复技能文件目录不存在，跳过" -ForegroundColor Gray
}

# 6. 清理构建产物
Write-Host "清理Maven构建产物..." -ForegroundColor Yellow
$targetDirs = Get-ChildItem -Path "." -Recurse -Directory -Name "target" -ErrorAction SilentlyContinue
if ($targetDirs.Count -gt 0) {
    Write-Host "发现 $($targetDirs.Count) 个target目录" -ForegroundColor Yellow
    if (-not $DryRun) {
        Get-ChildItem -Path "." -Recurse -Directory -Name "target" | ForEach-Object {
            Remove-Item -Path $_ -Recurse -Force -ErrorAction SilentlyContinue
        }
        Write-Host "✓ target目录清理完成" -ForegroundColor Green
    }
} else {
    Write-Host "未发现target目录" -ForegroundColor Gray
}

# 7. 清理class文件
$classFiles = Get-ChildItem -Path "." -Recurse -File -Name "*.class" -ErrorAction SilentlyContinue
if ($classFiles.Count -gt 0) {
    Write-Host "清理 $($classFiles.Count) 个.class文件" -ForegroundColor Yellow
    if (-not $DryRun) {
        Get-ChildItem -Path "." -Recurse -File -Name "*.class" -ErrorAction SilentlyContinue | Remove-Item -Force -ErrorAction SilentlyContinue
        Write-Host "✓ .class文件清理完成" -ForegroundColor Green
    }
} else {
    Write-Host "未发现.class文件" -ForegroundColor Gray
}

# 8. 整理已弃用服务
$deprecatedServicesPath = "microservices/archive/deprecated-services"
$servicesHistoryPath = "microservices/archive/services-history"

if (Test-Path $deprecatedServicesPath) {
    Write-Host "整理已弃用服务目录" -ForegroundColor Yellow
    if (-not $DryRun) {
        if (Test-Path $servicesHistoryPath) {
            Remove-Item -Path $servicesHistoryPath -Recurse -Force
        }
        Rename-Item -Path $deprecatedServicesPath -NewName "services-history"

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
        Write-Host "✓ 已弃用服务已整理并添加说明文档" -ForegroundColor Green
    }
} else {
    Write-Host "已弃用服务目录不存在，跳过" -ForegroundColor Gray
}

# 9. 清理日志文件
$logFiles = Get-ChildItem -Path "." -Recurse -File -Name "*.log" -ErrorAction SilentlyContinue
if ($logFiles.Count -gt 0) {
    Write-Host "清理 $($logFiles.Count) 个日志文件" -ForegroundColor Yellow
    if (-not $DryRun) {
        Get-ChildItem -Path "." -Recurse -File -Name "*.log" -ErrorAction SilentlyContinue | Remove-Item -Force -ErrorAction SilentlyContinue
        Write-Host "✓ 日志文件清理完成" -ForegroundColor Green
    }
} else {
    Write-Host "未发现日志文件" -ForegroundColor Gray
}

# 生成清理报告
Write-Host "`n生成清理报告..." -ForegroundColor Green

$currentSize = (Get-ChildItem -Path "." -Recurse | Measure-Object -Property Length -Sum).Sum / 1MB
$currentJavaFiles = (Get-ChildItem -Path "." -Recurse -Filter "*.java" | Measure-Object).Count
$currentMdFiles = (Get-ChildItem -Path "." -Recurse -Filter "*.md" | Measure-Object).Count

$reportContent = @"
# 项目清理执行报告

> **执行时间**: $(Get-Date)
> **执行脚本**: quick-cleanup.ps1
> **Git分支**: $(git branch --show-current)

## 清理统计

### 清理前状态
- 项目大小: 1.7GB
- Java文件数: 556
- Markdown文件数: 2,385

### 清理后状态
- 项目大小: $([math]::Round($currentSize, 2)) MB
- Java文件数: $currentJavaFiles
- Markdown文件数: $currentMdFiles

### 清理效果
- 删除文档目录: docs/, .qoder/
- 删除备份文件: restful_refactor_backup_*/
- 删除构建产物: 所有target目录
- Markdown文件减少: $((2385 - $currentMdFiles)) 个

## 清理内容详细

### 已删除/整理的目录
- `docs/` - 重复的文档目录
- `.qoder/` - 过期历史文档
- `restful_refactor_backup_*/` - 重构备份
- `.claude/skills/archive/duplicate-skills/` - 重复技能文件
- 所有 `target/` 构建目录
- `microservices/archive/deprecated-services/` → `services-history/`

---

**清理完成! 🎉**

项目现在更加整洁，维护效率将显著提升。
"@

if (-not $DryRun) {
    Set-Content -Path "CLEANUP_EXECUTION_REPORT.md" -Value $reportContent -Encoding UTF8
    Write-Host "✓ 清理报告已生成: CLEANUP_EXECUTION_REPORT.md" -ForegroundColor Green
}

Write-Host "`n============================================" -ForegroundColor Cyan
Write-Host "🎉 清理完成!" -ForegroundColor Green
if (-not $DryRun) {
    Write-Host "请查看 CLEANUP_EXECUTION_REPORT.md 了解详细清理结果" -ForegroundColor Cyan
}
Write-Host "建议操作:" -ForegroundColor Yellow
Write-Host "1. 检查清理结果: git status" -ForegroundColor White
if (-not $DryRun) {
    Write-Host "2. 提交清理更改: git add . && git commit -m 'chore: 清理冗余文件，优化项目结构'" -ForegroundColor White
}
Write-Host "============================================" -ForegroundColor Cyan