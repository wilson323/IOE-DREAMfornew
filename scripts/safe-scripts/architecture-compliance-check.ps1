# IOE-DREAM 架构符合性检查脚本 (PowerShell版本)
# 
# 功能：检查项目是否符合repowiki四层架构规范
# 规范基准：.qoder/repowiki 规范体系
#
# 使用方法：
#   .\scripts\architecture-compliance-check.ps1
#   .\scripts\architecture-compliance-check.ps1 -CheckPath "sa-admin\src\main\java\net\lab1024\sa\admin\module\consume"
#
# 作者：SmartAdmin规范治理委员会
# 创建时间：2025-11-20

param(
    [string]$CheckPath = "smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module"
)

$ErrorActionPreference = "Continue"

# 项目根目录
$ProjectRoot = Split-Path -Parent $PSScriptRoot
Set-Location $ProjectRoot

# 统计变量
$script:TotalViolations = 0
$script:TotalWarnings = 0
$script:TotalFilesChecked = 0

# 报告文件
$ReportFile = "docs\ARCHITECTURE_COMPLIANCE_REPORT_$(Get-Date -Format 'yyyyMMdd_HHmmss').md"
$TempReport = [System.IO.Path]::GetTempFileName()

function Write-Info {
    param([string]$Message)
    Write-Host "[INFO] $Message" -ForegroundColor Blue
    Add-Content -Path $TempReport -Value "[INFO] $Message"
}

function Write-Success {
    param([string]$Message)
    Write-Host "[SUCCESS] $Message" -ForegroundColor Green
    Add-Content -Path $TempReport -Value "[SUCCESS] $Message"
}

function Write-Warning {
    param([string]$Message)
    Write-Host "[WARNING] $Message" -ForegroundColor Yellow
    Add-Content -Path $TempReport -Value "[WARNING] $Message"
    $script:TotalWarnings++
}

function Write-Error {
    param([string]$Message)
    Write-Host "[ERROR] $Message" -ForegroundColor Red
    Add-Content -Path $TempReport -Value "[ERROR] $Message"
    $script:TotalViolations++
}

# 检查1: Controller层直接访问DAO
function Check-ControllerDaoAccess {
    Write-Info "检查1: Controller层直接访问DAO..."
    
    $controllerFiles = Get-ChildItem -Path $CheckPath -Filter "*Controller.java" -Recurse -ErrorAction SilentlyContinue
    
    if ($null -eq $controllerFiles -or $controllerFiles.Count -eq 0) {
        Write-Warning "未找到Controller文件"
        return
    }
    
    $violations = 0
    foreach ($file in $controllerFiles) {
        $content = Get-Content $file.FullName -Raw -ErrorAction SilentlyContinue
        if ($content -match "@Resource.*Dao|private.*Dao") {
            Write-Error "Controller直接访问DAO: $($file.FullName)"
            $violations++
        }
        $script:TotalFilesChecked++
    }
    
    if ($violations -eq 0) {
        Write-Success "Controller层无DAO访问违规"
    } else {
        Write-Error "发现 $violations 处Controller层DAO访问违规"
    }
}

# 检查2: Service层直接访问DAO
function Check-ServiceDaoAccess {
    Write-Info "检查2: Service层直接访问DAO（建议通过Manager）..."
    
    $serviceFiles = Get-ChildItem -Path $CheckPath -Filter "*ServiceImpl.java" -Recurse -ErrorAction SilentlyContinue
    
    if ($null -eq $serviceFiles -or $serviceFiles.Count -eq 0) {
        Write-Warning "未找到Service实现文件"
        return
    }
    
    $warnings = 0
    foreach ($file in $serviceFiles) {
        $content = Get-Content $file.FullName -Raw -ErrorAction SilentlyContinue
        # 排除继承ServiceImpl的情况
        if ($content -notmatch "extends ServiceImpl") {
            if ($content -match "@Resource.*Dao|private.*Dao") {
                Write-Warning "Service直接访问DAO（建议通过Manager）: $($file.FullName)"
                $warnings++
            }
        }
        $script:TotalFilesChecked++
    }
    
    if ($warnings -eq 0) {
        Write-Success "Service层DAO访问符合最佳实践"
    } else {
        Write-Warning "发现 $warnings 处Service层直接访问DAO（建议优化）"
    }
}

# 检查3: Engine层直接访问DAO
function Check-EngineDaoAccess {
    Write-Info "检查3: Engine层直接访问DAO..."
    
    $engineFiles = Get-ChildItem -Path $CheckPath -Filter "*.java" -Recurse -ErrorAction SilentlyContinue | 
        Where-Object { $_.FullName -match "\\engine\\" }
    
    if ($null -eq $engineFiles -or $engineFiles.Count -eq 0) {
        Write-Info "未找到Engine文件"
        return
    }
    
    $violations = 0
    foreach ($file in $engineFiles) {
        $content = Get-Content $file.FullName -Raw -ErrorAction SilentlyContinue
        if ($content -match "@Resource.*Dao|private.*Dao") {
            Write-Error "Engine直接访问DAO: $($file.FullName)"
            $violations++
        }
        $script:TotalFilesChecked++
    }
    
    if ($violations -eq 0) {
        Write-Success "Engine层无DAO访问违规"
    } else {
        Write-Error "发现 $violations 处Engine层DAO访问违规"
    }
}

# 检查4: 依赖注入规范
function Check-DependencyInjection {
    Write-Info "检查4: 依赖注入规范（@Resource vs @Autowired）..."
    
    $javaFiles = Get-ChildItem -Path $CheckPath -Filter "*.java" -Recurse -ErrorAction SilentlyContinue
    
    if ($null -eq $javaFiles -or $javaFiles.Count -eq 0) {
        Write-Warning "未找到Java文件"
        return
    }
    
    $violations = 0
    foreach ($file in $javaFiles) {
        $content = Get-Content $file.FullName -Raw -ErrorAction SilentlyContinue
        if ($content -match "@Autowired") {
            Write-Error "使用@Autowired（应使用@Resource）: $($file.FullName)"
            $violations++
        }
        $script:TotalFilesChecked++
    }
    
    if ($violations -eq 0) {
        Write-Success "依赖注入规范符合要求（全部使用@Resource）"
    } else {
        Write-Error "发现 $violations 处使用@Autowired（应改为@Resource）"
    }
}

# 检查5: 冗余文件
function Check-RedundantFiles {
    Write-Info "检查5: 冗余文件（.backup, .bak等）..."
    
    $redundantFiles = Get-ChildItem -Path $CheckPath -Include *.backup,*.bak,*.old,*.tmp -Recurse -ErrorAction SilentlyContinue
    
    if ($null -eq $redundantFiles -or $redundantFiles.Count -eq 0) {
        Write-Success "未发现冗余文件"
    } else {
        Write-Warning "发现 $($redundantFiles.Count) 个冗余文件:"
        $redundantFiles | Select-Object -First 10 | ForEach-Object {
            Write-Warning "  - $($_.FullName)"
        }
        if ($redundantFiles.Count -gt 10) {
            Write-Warning "  ... 还有 $($redundantFiles.Count - 10) 个文件"
        }
    }
}

# 生成报告
function Generate-Report {
    Write-Info "生成架构符合性检查报告..."
    
    $reportContent = @"
# IOE-DREAM 架构符合性检查报告

> **检查时间**: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')  
> **检查路径**: $CheckPath  
> **检查状态**: $(if ($script:TotalViolations -eq 0) { "✅ 通过" } else { "❌ 发现问题" })

---

## 📊 检查结果汇总

### 总体统计
- **检查文件数**: $script:TotalFilesChecked
- **违规数量**: $script:TotalViolations
- **警告数量**: $script:TotalWarnings
- **符合性**: $(if ($script:TotalViolations -eq 0) { "100% ✅" } else { "$(100 - $script:TotalViolations * 10)% ⚠️" })

---

## 📋 详细检查结果

$(Get-Content $TempReport -Raw)

---

## ✅ 检查项清单

- [$(if ($script:TotalViolations -eq 0) { 'x' } else { ' ' })] Controller层无DAO访问违规
- [$(if ($script:TotalWarnings -eq 0) { 'x' } else { ' ' })] Service层DAO访问符合最佳实践
- [$(if ($script:TotalViolations -eq 0) { 'x' } else { ' ' })] Engine层无DAO访问违规
- [$(if ($script:TotalViolations -eq 0) { 'x' } else { ' ' })] 依赖注入规范符合要求
- [$(if ($script:TotalWarnings -eq 0) { 'x' } else { ' ' })] 无冗余文件

---

## 🎯 修复建议

$(if ($script:TotalViolations -gt 0 -or $script:TotalWarnings -gt 0) {
    @"
### 需要修复的问题

1. **架构违规**: 修复Controller/Engine层直接访问DAO的问题
2. **依赖注入**: 将@Autowired改为@Resource
3. **冗余文件**: 清理备份文件
4. **重复代码**: 统一重复类定义
5. **编码问题**: 修复BOM字符问题
"@
} else {
    "✅ 所有检查项均通过，无需修复"
})

---

**报告生成时间**: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')  
**检查脚本**: scripts/architecture-compliance-check.ps1
"@

    Set-Content -Path $ReportFile -Value $reportContent -Encoding UTF8
    Write-Success "报告已生成: $ReportFile"
}

# 主函数
function Main {
    Write-Host "==========================================" -ForegroundColor Cyan
    Write-Host "  IOE-DREAM 架构符合性检查" -ForegroundColor Cyan
    Write-Host "==========================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "检查路径: $CheckPath"
    Write-Host "报告文件: $ReportFile"
    Write-Host ""
    
    # 执行各项检查
    Check-ControllerDaoAccess
    Write-Host ""
    
    Check-ServiceDaoAccess
    Write-Host ""
    
    Check-EngineDaoAccess
    Write-Host ""
    
    Check-DependencyInjection
    Write-Host ""
    
    Check-RedundantFiles
    Write-Host ""
    
    # 生成报告
    Generate-Report
    
    # 输出总结
    Write-Host "==========================================" -ForegroundColor Cyan
    Write-Host "  检查完成" -ForegroundColor Cyan
    Write-Host "==========================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "检查文件数: $script:TotalFilesChecked"
    Write-Host "违规数量: $script:TotalViolations"
    Write-Host "警告数量: $script:TotalWarnings"
    Write-Host ""
    
    if ($script:TotalViolations -eq 0 -and $script:TotalWarnings -eq 0) {
        Write-Host "✅ 架构符合性检查通过！" -ForegroundColor Green
        exit 0
    } elseif ($script:TotalViolations -eq 0) {
        Write-Host "⚠️  发现 $script:TotalWarnings 个警告，建议优化" -ForegroundColor Yellow
        exit 0
    } else {
        Write-Host "❌ 发现 $script:TotalViolations 个违规，需要修复" -ForegroundColor Red
        exit 1
    }
}

# 执行主函数
Main

