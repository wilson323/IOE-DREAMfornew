# IOE-DREAM 项目根目录清理脚本
# 用途：自动清理根目录的过时文档、临时日志和一次性脚本
# 作者：项目架构委员会
# 日期：2025-12-26

param(
    [switch]$DryRun = $false,
    [switch]$Confirm = $false,
    [string]$Phase = "all"
)

# 颜色输出函数
function Write-ColorOutput {
    param(
        [string]$Message,
        [string]$Color = "White"
    )
    Write-Host $Message -ForegroundColor $Color
}

# 显示帮助信息
function Show-Help {
    Write-ColorOutput "
========================================
IOE-DREAM 项目清理脚本
========================================

用法:
  .\cleanup-project-root.ps1 [选项]

选项:
  -DryRun        预览模式，不实际删除/移动文件
  -Confirm       执行前需要确认每个操作
  -Phase <阶段>  指定执行的阶段 (1/2/3/4/5/all)

阶段说明:
  Phase 1: 归档历史报告（P0/P2/Phase系列）
  Phase 2: 归档重复分析文档
  Phase 3: 删除临时日志文件
  Phase 4: 清理一次性脚本
  Phase 5: 清理Python临时脚本
  all:     执行所有阶段（默认）

示例:
  .\cleanup-project-root.ps1 -DryRun              # 预览所有操作
  .\cleanup-project-root.ps1 -Phase 1             # 只执行阶段1
  .\cleanup-project-root.ps1 -Confirm             # 交互式确认执行
" "Cyan"
}

# 创建归档目录
function Initialize-ArchiveStructure {
    Write-ColorOutput "`n[1/5] 创建归档目录结构..." "Yellow"

    $archiveDirs = @(
        "archive/reports/p0-series",
        "archive/reports/p1-series",
        "archive/reports/p2-series",
        "archive/reports/phase-series",
        "archive/reports/attendance",
        "archive/reports/smart-schedule",
        "archive/reports/query-builder",
        "archive/reports/rule-config",
        "archive/reports/testing",
        "archive/reports/chinese",
        "archive/analysis",
        "archive/logs/build",
        "archive/logs/errors",
        "archive/scripts/bom-cleanup",
        "archive/scripts/encoding-fix",
        "archive/scripts/type-cast-fix",
        "archive/scripts/logging-fix",
        "archive/scripts/path-fix",
        "archive/scripts/test-fix"
    )

    foreach ($dir in $archiveDirs) {
        if (-not (Test-Path $dir)) {
            if (-not $DryRun) {
                New-Item -ItemType Directory -Path $dir -Force | Out-Null
                Write-ColorOutput "  ✓ 创建目录: $dir" "Green"
            } else {
                Write-ColorOutput "  [预览] 将创建目录: $dir" "Gray"
            }
        }
    }
}

# Phase 1: 归档历史报告
function Invoke-Phase1 {
    Write-ColorOutput "`n[Phase 1/5] 归档历史报告..." "Yellow"

    $patterns = @(
        @{Pattern = "P0_*.md"; Target = "archive/reports/p0-series"},
        @{Pattern = "P1_*.md"; Target = "archive/reports/p1-series"},
        @{Pattern = "P2_*.md"; Target = "archive/reports/p2-series"},
        @{Pattern = "PHASE_*.md"; Target = "archive/reports/phase-series"},
        @{Pattern = "*考勤*.md"; Target = "archive/reports/attendance"},
        @{Pattern = "*SCHEDULE*.md"; Target = "archive/reports/smart-schedule"},
        @{Pattern = "*QUERYBUILDER*.md"; Target = "archive/reports/query-builder"},
        @{Pattern = "RULE_*.md"; Target = "archive/reports/rule-config"},
        @{Pattern = "*_实施报告.md"; Target = "archive/reports/chinese"}
    )

    foreach ($item in $patterns) {
        $files = Get-ChildItem -Path $item.Pattern -ErrorAction SilentlyContinue
        if ($files) {
            Write-ColorOutput "  处理文件组: $($item.Pattern)" "Cyan"
            foreach ($file in $files) {
                $targetPath = Join-Path $item.Target $file.Name
                if ($Confirm) {
                    $response = Read-Host "  移动 $($file.Name) 到 $($item.Target)? (y/n)"
                    if ($response -eq 'y') {
                        if (-not $DryRun) {
                            Move-Item -Path $file.FullName -Destination $targetPath -Force
                            Write-ColorOutput "    ✓ 已移动: $($file.Name)" "Green"
                        } else {
                            Write-ColorOutput "    [预览] 将移动: $($file.Name)" "Gray"
                        }
                    }
                } else {
                    if (-not $DryRun) {
                        Move-Item -Path $file.FullName -Destination $targetPath -Force
                        Write-ColorOutput "    ✓ 已移动: $($file.Name)" "Green"
                    } else {
                        Write-ColorOutput "    [预览] 将移动: $($file.Name)" "Gray"
                    }
                }
            }
        }
    }

    # 移动测试报告
    $testReports = @(
        "*TEST*.md",
        "*COMPLETION_REPORT.md",
        "*ACCEPTANCE_REPORT.md"
    )

    foreach ($pattern in $testReports) {
        $files = Get-ChildItem -Path $pattern -ErrorAction SilentlyContinue | Where-Object { $_.Name -ne "CLAUDE.md" }
        if ($files) {
            Write-ColorOutput "  处理测试报告: $pattern" "Cyan"
            foreach ($file in $files) {
                $targetPath = Join-Path "archive/reports/testing" $file.Name
                if (-not $DryRun) {
                    Move-Item -Path $file.FullName -Destination $targetPath -Force
                    Write-ColorOutput "    ✓ 已移动: $($file.Name)" "Green"
                } else {
                    Write-ColorOutput "    [预览] 将移动: $($file.Name)" "Gray"
                }
            }
        }
    }
}

# Phase 2: 归档重复分析文档
function Invoke-Phase2 {
    Write-ColorOutput "`n[Phase 2/5] 归档重复分析文档..." "Yellow"

    $patterns = @(
        "GLOBAL_*_ANALYSIS*.md",
        "GLOBAL_*_REPORT.md",
        "GLOBAL_*_SUMMARY.md",
        "GLOBAL_*_PLAN.md",
        "ENTERPRISE_*_REPORT.md",
        "ENTERPRISE_*_ANALYSIS.md",
        "*_ANALYSIS.md",
        "*_GUIDE.md",
        "dependency-analysis.md",
        "exception-handling-report.md",
        "microservices-common-analysis-report.md",
        "type-reference-fix-report.md"
    )

    foreach ($pattern in $patterns) {
        $files = Get-ChildItem -Path $pattern -ErrorAction SilentlyContinue | Where-Object {
            $_.Name -notin @("CLAUDE.md", "README.md", "AGENTS.md", "PROJECT_STATUS_CURRENT.md")
        }
        if ($files) {
            Write-ColorOutput "  处理: $pattern" "Cyan"
            foreach ($file in $files) {
                $targetPath = Join-Path "archive/analysis" $file.Name
                if (-not $DryRun) {
                    Move-Item -Path $file.FullName -Destination $targetPath -Force
                    Write-ColorOutput "    ✓ 已归档: $($file.Name)" "Green"
                } else {
                    Write-ColorOutput "    [预览] 将归档: $($file.Name)" "Gray"
                }
            }
        }
    }
}

# Phase 3: 删除临时日志文件
function Invoke-Phase3 {
    Write-ColorOutput "`n[Phase 3/5] 删除临时日志文件..." "Yellow"

    $logPatterns = @(
        "compile*.log",
        "compile*.txt",
        "build-*.log",
        "build-*.txt",
        "*_compile-log.txt",
        "*_compile.log",
        "*-errors.txt",
        "*-errors-detail.txt",
        "erro.txt",
        "error_categories.txt",
        "chonggou.txt",
        "garbled-files-list*.txt",
        "fixed_files.txt",
        "*_report.txt",
        "*_scanned.txt"
    )

    $totalSize = 0
    $totalFiles = 0

    foreach ($pattern in $logPatterns) {
        $files = Get-ChildItem -Path $pattern -ErrorAction SilentlyContinue
        if ($files) {
            Write-ColorOutput "  处理日志组: $pattern ($($files.Count) 个文件)" "Cyan"
            foreach ($file in $files) {
                $totalSize += $file.Length
                $totalFiles += 1

                if ($Confirm) {
                    $response = Read-Host "  删除 $($file.Name)? (y/n)"
                    if ($response -eq 'y') {
                        if (-not $DryRun) {
                            Remove-Item -Path $file.FullName -Force
                            Write-ColorOutput "    ✓ 已删除: $($file.Name)" "Green"
                        } else {
                            Write-ColorOutput "    [预览] 将删除: $($file.Name) ($([math]::Round($file.Length/1KB, 2)) KB)" "Gray"
                        }
                    }
                } else {
                    if (-not $DryRun) {
                        Remove-Item -Path $file.FullName -Force
                        Write-ColorOutput "    ✓ 已删除: $($file.Name) ($([math]::Round($file.Length/1KB, 2)) KB)" "Green"
                    } else {
                        Write-ColorOutput "    [预览] 将删除: $($file.Name) ($([math]::Round($file.Length/1KB, 2)) KB)" "Gray"
                    }
                }
            }
        }
    }

    Write-ColorOutput "`n  统计: 将删除 $totalFiles 个日志文件, 释放 $([math]::Round($totalSize/1MB, 2)) MB 空间" "Yellow"
}

# Phase 4: 清理一次性脚本
function Invoke-Phase4 {
    Write-ColorOutput "`n[Phase 4/5] 清理一次性脚本..." "Yellow"

    $scriptCategories = @{
        "bom-cleanup" = @(
            "*bom*.ps1",
            "*bom*.sh"
        )
        "encoding-fix" = @(
            "*encoding*.ps1",
            "*encoding*.sh",
            "*_garbled_files.sh"
        )
        "type-cast-fix" = @(
            "*cast*.ps1",
            "*gateway*.ps1"
        )
        "logging-fix" = @(
            "*logging*.sh",
            "*logger*.sh",
            "*-logging.sh"
        )
        "path-fix" = @(
            "*package-paths*.ps1",
            "*pageresult*.ps1",
            "*platform*.ps1",
            "*import*.ps1"
        )
        "test-fix" = @(
            "fix_*_test*.py",
            "*_dao_mocks*.py",
            "add_*.py"
        )
        "other-fix" = @(
            "fix-*.ps1",
            "fix-*.sh",
            "fix-*.py",
            "clean-*.ps1",
            "remove-*.ps1",
            "remove-*.bat",
            "*-cleanup.ps1",
            "*-optimization*.ps1"
        )
    }

    foreach ($category in $scriptCategories.Keys) {
        $patterns = $scriptCategories[$category]
        Write-ColorOutput "  处理脚本类别: $category" "Cyan"

        foreach ($pattern in $patterns) {
            $files = Get-ChildItem -Path $pattern -ErrorAction SilentlyContinue | Where-Object {
                # 排除常用脚本
                $_.Name -notin @(
                    "pre-commit-hook.sh",
                    "quick-quality-check.sh",
                    "generate-quality-report.sh"
                )
            }

            if ($files) {
                foreach ($file in $files) {
                    $targetPath = Join-Path "archive/scripts/$category" $file.Name
                    if (-not $DryRun) {
                        Move-Item -Path $file.FullName -Destination $targetPath -Force
                        Write-ColorOutput "    ✓ 已归档: $($file.Name)" "Green"
                    } else {
                        Write-ColorOutput "    [预览] 将归档: $($file.Name)" "Gray"
                    }
                }
            }
        }
    }
}

# Phase 5: 清理Python临时脚本
function Invoke-Phase5 {
    Write-ColorOutput "`n[Phase 5/5] 清理Python临时脚本..." "Yellow"

    $pythonScripts = Get-ChildItem -Path "*.py" -ErrorAction SilentlyContinue

    if ($pythonScripts) {
        Write-ColorOutput "  发现 $($pythonScripts.Count) 个Python脚本" "Cyan"
        foreach ($script in $pythonScripts) {
            $targetPath = Join-Path "archive/scripts/test-fix" $script.Name
            if (-not $DryRun) {
                Move-Item -Path $script.FullName -Destination $targetPath -Force
                Write-ColorOutput "    ✓ 已归档: $($script.Name)" "Green"
            } else {
                Write-ColorOutput "    [预览] 将归档: $($script.Name)" "Gray"
            }
        }
    } else {
        Write-ColorOutput "  未发现Python脚本" "Gray"
    }
}

# 生成清理报告
function New-CleanupReport {
    $reportPath = "cleanup-report-$(Get-Date -Format 'yyyyMMdd-HHmmss').txt"

    if ($DryRun) {
        Write-ColorOutput "`n[预览模式] 未生成实际报告文件" "Yellow"
        Write-ColorOutput "使用 -Confirm 参数执行实际清理操作" "Yellow"
    } else {
        @"
========================================
IOE-DREAM 项目清理报告
========================================
清理时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
执行模式: $(if ($Confirm) { '交互式确认' } else { '自动执行' })

清理统计:
- 归档目录已创建: archive/
- 历史报告已归档: archive/reports/
- 分析文档已归档: archive/analysis/
- 临时日志已清理
- 一次性脚本已归档: archive/scripts/

后续操作:
1. 检查归档目录确认文件正确
2. 更新 README.md 中的文档链接
3. 提交Git更改
4. 定期重复此清理流程

保留的核心文档:
- CLAUDE.md (项目核心规范)
- README.md (项目说明)
- AGENTS.md (Agent指南)
- PROJECT_STATUS_CURRENT.md (当前状态)

========================================
"@ | Out-File -FilePath $reportPath -Encoding UTF8

        Write-ColorOutput "`n✓ 清理报告已生成: $reportPath" "Green"
    }
}

# 主函数
function Main {
    Write-ColorOutput "
========================================
IOE-DREAM 项目根目录清理工具
========================================
" "Cyan"

    if ($DryRun) {
        Write-ColorOutput "⚠️  预览模式：不会实际修改文件" "Yellow"
        Write-ColorOutput ""
    }

    # 执行清理
    Initialize-ArchiveStructure

    switch ($Phase) {
        "1" { Invoke-Phase1 }
        "2" { Invoke-Phase2 }
        "3" { Invoke-Phase3 }
        "4" { Invoke-Phase4 }
        "5" { Invoke-Phase5 }
        "all" {
            Invoke-Phase1
            Invoke-Phase2
            Invoke-Phase3
            Invoke-Phase4
            Invoke-Phase5
        }
        default {
            Write-ColorOutput "❌ 无效的阶段参数: $Phase" "Red"
            Show-Help
            exit 1
        }
    }

    # 生成报告
    New-CleanupReport

    Write-ColorOutput "`n✅ 清理完成！" "Green"
    Write-ColorOutput "`n建议后续操作:" "Yellow"
    Write-ColorOutput "1. 检查归档目录: archive/" "White"
    Write-ColorOutput "2. 更新 README.md 文档链接" "White"
    Write-ColorOutput "3. Git提交更改: git add . && git commit -m 'chore: 清理根目录过时文档和临时文件'" "White"
    Write-ColorOutput "4. 查看 PROJECT_CLEANUP_ANALYSIS_REPORT.md 了解详情" "White"
}

# 执行主函数
if ($args -contains '-?' -or $args -contains '-help') {
    Show-Help
} else {
    Main
}
