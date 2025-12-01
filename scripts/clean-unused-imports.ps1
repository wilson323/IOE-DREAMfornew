# PowerShell脚本：批量清理未使用的Java导入
# 适用于Windows环境，无需额外安装软件
# 作者：IOE-DREAM项目组
# 版本：v1.0
# 日期：2025-11-16

param(
    [string]$ProjectPath = "D:\IOE-DREAM\smart-admin-api-java17-springboot3",
    [switch]$DryRun = $false,
    [switch]$Verbose = $false
)

$ErrorActionPreference = "Stop"
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$reportPath = "D:\IOE-DREAM\scripts\reports\import-cleanup-report-$timestamp.md"

# 创建报告目录
$reportDir = Split-Path $reportPath -Parent
if (!(Test-Path $reportDir)) {
    New-Item -ItemType Directory -Path $reportDir -Force | Out-Null
}

# 初始化统计
$stats = @{
    TotalFiles = 0
    FilesWithIssues = 0
    UnusedImports = 0
    FixedImports = 0
    Errors = 0
}

Write-Host "🚀 IOE-DREAM Java导入清理工具" -ForegroundColor Cyan
Write-Host "=" * 60 -ForegroundColor Cyan
Write-Host "📂 项目路径: $ProjectPath" -ForegroundColor Yellow
Write-Host "📄 报告路径: $reportPath" -ForegroundColor Yellow
Write-Host "🔧 运行模式: $(if ($DryRun) { '模拟运行(不修改文件)' } else { '实际修改' })" -ForegroundColor Yellow
Write-Host "=" * 60 -ForegroundColor Cyan
Write-Host ""

# 开始报告
$report = @"
# Java导入清理报告

**生成时间**: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")
**项目路径**: $ProjectPath
**运行模式**: $(if ($DryRun) { '模拟运行' } else { '实际修改' })

## 📊 统计摘要

| 指标 | 数量 |
|------|------|

"@

# 查找所有Java文件
Write-Host "🔍 扫描Java文件..." -ForegroundColor Green
$javaFiles = Get-ChildItem -Path $ProjectPath -Recurse -Filter "*.java" -File
$stats.TotalFiles = $javaFiles.Count

Write-Host "找到 $($stats.TotalFiles) 个Java文件" -ForegroundColor Green
Write-Host ""

# 问题文件列表
$problemFiles = @()

# 处理每个文件
$fileIndex = 0
foreach ($file in $javaFiles) {
    $fileIndex++
    $progress = [math]::Round(($fileIndex / $stats.TotalFiles) * 100, 2)
    
    Write-Progress -Activity "分析Java文件" -Status "进度: $progress% ($fileIndex/$($stats.TotalFiles))" -PercentComplete $progress
    
    if ($Verbose) {
        Write-Host "[$fileIndex/$($stats.TotalFiles)] 处理: $($file.FullName)" -ForegroundColor Gray
    }
    
    try {
        $content = Get-Content -Path $file.FullName -Encoding UTF8 -Raw
        $lines = Get-Content -Path $file.FullName -Encoding UTF8
        
        # 查找所有import语句
        $imports = @()
        $importLines = @()
        for ($i = 0; $i -lt $lines.Count; $i++) {
            $line = $lines[$i]
            if ($line -match '^\s*import\s+(?:static\s+)?([a-zA-Z0-9._*]+)\s*;') {
                $importClass = $matches[1]
                $imports += @{
                    Line = $i + 1
                    Full = $line
                    Class = $importClass
                    Used = $false
                }
                $importLines += $i
            }
        }
        
        if ($imports.Count -eq 0) {
            continue
        }
        
        # 检查每个import是否被使用
        $unusedImports = @()
        foreach ($import in $imports) {
            $className = $import.Class
            
            # 跳过通配符导入
            if ($className -match '\*$') {
                continue
            }
            
            # 获取简单类名
            $simpleClassName = $className.Split('.')[-1]
            
            # 检查类名是否在代码中使用
            $codeWithoutImports = $content -replace 'import\s+.*?;', ''
            
            # 更精确的使用检查
            $patterns = @(
                "\b$simpleClassName\b",                    # 直接使用类名
                "\b$simpleClassName\s*<",                  # 泛型使用
                "\b$simpleClassName\s*\[",                 # 数组使用
                "new\s+$simpleClassName\s*\(",            # 实例化
                "@$simpleClassName",                       # 注解使用
                "extends\s+$simpleClassName",              # 继承
                "implements\s+$simpleClassName"            # 实现
            )
            
            $used = $false
            foreach ($pattern in $patterns) {
                if ($codeWithoutImports -match $pattern) {
                    $used = $true
                    break
                }
            }
            
            if (-not $used) {
                $unusedImports += $import
                $stats.UnusedImports++
            }
        }
        
        if ($unusedImports.Count -gt 0) {
            $stats.FilesWithIssues++
            
            $problemFiles += @{
                Path = $file.FullName
                RelativePath = $file.FullName.Replace($ProjectPath, "").TrimStart('\')
                UnusedImports = $unusedImports
            }
            
            Write-Host "⚠️  发现问题: $($file.Name)" -ForegroundColor Yellow
            Write-Host "   未使用的导入: $($unusedImports.Count) 个" -ForegroundColor Yellow
            
            foreach ($unused in $unusedImports) {
                Write-Host "   - 第 $($unused.Line) 行: $($unused.Class)" -ForegroundColor DarkYellow
            }
            
            # 如果不是模拟运行,则删除未使用的导入
            if (-not $DryRun) {
                $newLines = @()
                for ($i = 0; $i -lt $lines.Count; $i++) {
                    $shouldRemove = $false
                    foreach ($unused in $unusedImports) {
                        if ($i -eq ($unused.Line - 1)) {
                            $shouldRemove = $true
                            break
                        }
                    }
                    
                    if (-not $shouldRemove) {
                        $newLines += $lines[$i]
                    } else {
                        $stats.FixedImports++
                    }
                }
                
                # 保存修改后的文件
                $newContent = $newLines -join "`r`n"
                Set-Content -Path $file.FullName -Value $newContent -Encoding UTF8 -NoNewline
                
                Write-Host "   ✅ 已清理 $($unusedImports.Count) 个未使用的导入" -ForegroundColor Green
            }
            
            Write-Host ""
        }
        
    } catch {
        $stats.Errors++
        Write-Host "❌ 处理文件失败: $($file.FullName)" -ForegroundColor Red
        Write-Host "   错误: $($_.Exception.Message)" -ForegroundColor Red
        Write-Host ""
    }
}

Write-Progress -Activity "分析Java文件" -Completed

# 生成详细报告
$report += "| 总文件数 | $($stats.TotalFiles) |`n"
$report += "| 存在问题的文件 | $($stats.FilesWithIssues) |`n"
$report += "| 未使用的导入总数 | $($stats.UnusedImports) |`n"
$report += "| 已清理的导入 | $($stats.FixedImports) |`n"
$report += "| 处理错误数 | $($stats.Errors) |`n"
$report += "`n## 📝 问题文件清单`n`n"

if ($problemFiles.Count -eq 0) {
    $report += "✅ **太棒了！没有发现未使用的导入。**`n`n"
} else {
    foreach ($problem in $problemFiles) {
        $report += "### 📄 $($problem.RelativePath)`n`n"
        $report += "**未使用的导入** ($($problem.UnusedImports.Count)个)：`n`n"
        foreach ($unused in $problem.UnusedImports) {
            $report += "- 第 $($unused.Line) 行: ``$($unused.Class)```n"
        }
        $report += "`n"
    }
}

$report += @"

## 🔧 建议操作

"@

if ($DryRun) {
    $report += @"
**当前为模拟运行模式**，未实际修改文件。

如需实际清理，请执行：
``````powershell
.\clean-unused-imports.ps1 -DryRun:`$false
``````

"@
} else {
    $report += @"
✅ 已完成实际清理，共修复 **$($stats.FixedImports)** 个未使用的导入。

**后续步骤**：
1. 运行编译检查：``mvn clean compile -DskipTests``
2. 运行单元测试：``mvn test``
3. 提交代码到Git：``git add . && git commit -m "chore: 清理未使用的导入"``

"@
}

$report += @"

## 📚 相关文档

- [Java编码规范](D:\IOE-DREAM\docs\repowiki\zh\content\开发规范体系\核心规范\Java编码规范.md)
- [IDEA批量优化指南](D:\IOE-DREAM\scripts\idea-batch-optimize-imports.md)

---

**脚本版本**: v1.0
**生成时间**: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")
"@

# 保存报告
Set-Content -Path $reportPath -Value $report -Encoding UTF8

# 显示摘要
Write-Host ""
Write-Host "=" * 60 -ForegroundColor Cyan
Write-Host "📊 清理完成 - 统计摘要" -ForegroundColor Cyan
Write-Host "=" * 60 -ForegroundColor Cyan
Write-Host "✅ 总文件数:        $($stats.TotalFiles)" -ForegroundColor Green
Write-Host "⚠️  存在问题的文件:  $($stats.FilesWithIssues)" -ForegroundColor Yellow
Write-Host "🔍 未使用的导入:    $($stats.UnusedImports)" -ForegroundColor Yellow
Write-Host "🔧 已清理的导入:    $($stats.FixedImports)" -ForegroundColor Green
Write-Host "❌ 处理错误:        $($stats.Errors)" -ForegroundColor Red
Write-Host "=" * 60 -ForegroundColor Cyan
Write-Host ""
Write-Host "📄 详细报告已生成: $reportPath" -ForegroundColor Cyan
Write-Host ""

if ($DryRun) {
    Write-Host "💡 提示: 当前为模拟运行模式，未修改任何文件。" -ForegroundColor Yellow
    Write-Host "   如需实际清理，请运行: .\clean-unused-imports.ps1 -DryRun:`$false" -ForegroundColor Yellow
} else {
    Write-Host "✨ 建议后续操作:" -ForegroundColor Green
    Write-Host "   1. 运行编译: mvn clean compile -DskipTests" -ForegroundColor White
    Write-Host "   2. 运行测试: mvn test" -ForegroundColor White
    Write-Host "   3. 提交代码: git add . && git commit -m 'chore: 清理未使用的导入'" -ForegroundColor White
}

Write-Host ""

# 如果有错误,返回非零退出码
if ($stats.Errors -gt 0) {
    exit 1
}

exit 0

