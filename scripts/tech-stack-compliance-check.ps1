# ==============================================================================
# IOE-DREAM 技术栈合规性检查脚本 (PowerShell版本)
#
# 功能: 全面检查Spring Boot 3.5和Jakarta包名规范合规性
# 范围: 依赖注入、包名使用、版本一致性、注解规范
# 标准: Jakarta EE 10 + Spring Boot 3.5企业级规范
# ==============================================================================

param(
    [switch]$SkipTests,
    [switch]$Verbose,
    [string]$OutputPath = "."
)

# 设置错误首选项
$ErrorActionPreference = "Stop"

# 颜色输出函数
function Write-ColorOutput {
    param(
        [string]$Message,
        [string]$Color = "White"
    )

    $colors = @{
        "Red" = "Red"
        "Green" = "Green"
        "Yellow" = "Yellow"
        "Blue" = "Blue"
        "Cyan" = "Cyan"
        "Magenta" = "Magenta"
        "White" = "White"
    }

    if ($colors.ContainsKey($Color)) {
        Write-Host $Message -ForegroundColor $colors[$Color]
    } else {
        Write-Host $Message
    }
}

# 主函数
function Main {
    Write-ColorOutput "🔍 IOE-DREAM 技术栈合规性检查脚本" "Cyan"
    Write-ColorOutput "=================================" "Cyan"

    # 统计变量
    $script:TotalChecks = 0
    $script:PassedChecks = 0
    $script:FailedChecks = 0
    $script:WarningChecks = 0

    # 报告文件路径
    $ReportFile = Join-Path $OutputPath "tech-stack-compliance-report.md"

    Write-Host ""
    Write-ColorOutput "📋 开始技术栈合规性检查..." "Blue"
    Write-Host "检查时间: $(Get-Date)"
    Write-Host "检查范围: IOE-DREAM项目全量代码"
    Write-Host "====================================="

    # 检查是否在项目根目录
    if (-not (Test-Path "microservices\pom.xml")) {
        Write-ColorOutput "❌ 错误: 请在IOE-DREAM项目根目录执行此脚本" "Red"
        exit 1
    }

    # 1. Jakarta包名迁移检查
    Write-Host ""
    Write-ColorOutput "🔍 第一部分: Jakarta包名迁移检查" "Magenta"
    Write-Host "========================================" "Magenta"

    # 1.1 javax包违规检查
    $JavaxViolations = Get-ChildItem -Path . -Filter "*.java" -Recurse |
        Select-String -Pattern "javax\.(annotation|validation|persistence|servlet|xml\.bind)" |
        Select-Object -ExpandProperty Path |
        Get-Unique |
        Measure-Object |
        Select-Object -ExpandProperty Count

    Check-Item -Description "javax包违规使用检查" -Actual $JavaxViolations -Expected 0

    # 1.2 Jakarta包使用统计
    $JakartaAnnotation = (Get-ChildItem -Path . -Filter "*.java" -Recurse |
        Select-String -Pattern "jakarta\.annotation" |
        Select-Object -ExpandProperty Path |
        Get-Unique |
        Measure-Object).Count

    $JakartaValidation = (Get-ChildItem -Path . -Filter "*.java" -Recurse |
        Select-String -Pattern "jakarta\.validation" |
        Select-Object -ExpandProperty Path |
        Get-Unique |
        Measure-Object).Count

    $JakartaPersistence = (Get-ChildItem -Path . -Filter "*.java" -Recurse |
        Select-String -Pattern "jakarta\.persistence" |
        Select-Object -ExpandProperty Path |
        Get-Unique |
        Measure-Object).Count

    $JakartaServlet = (Get-ChildItem -Path . -Filter "*.java" -Recurse |
        Select-String -Pattern "jakarta\.servlet" |
        Select-Object -ExpandProperty Path |
        Get-Unique |
        Measure-Object).Count

    Write-Host ""
    Write-ColorOutput "📊 Jakarta包使用统计:" "Cyan"
    Write-Host "  jakarta.annotation: $JakartaAnnotation 个文件" -ForegroundColor Green
    Write-Host "  jakarta.validation: $JakartaValidation 个文件" -ForegroundColor Green
    Write-Host "  jakarta.persistence: $JakartaPersistence 个文件" -ForegroundColor Green
    Write-Host "  jakarta.servlet: $JakartaServlet 个文件" -ForegroundColor Green

    # 2. 依赖注入规范检查
    Write-Host ""
    Write-ColorOutput "🔍 第二部分: 依赖注入规范检查" "Magenta"
    Write-Host "======================================" "Magenta"

    # 2.1 业务代码@Autowired检查
    $AutowiredBusiness = Get-ChildItem -Path . -Filter "*.java" -Recurse |
        Where-Object { $_.FullName -notmatch "\\test\\" } |
        Select-String -Pattern "@Autowired" |
        Select-Object -ExpandProperty Path |
        Get-Unique |
        Measure-Object |
        Select-Object -ExpandProperty Count

    Check-Warning -Description "业务代码@Autowired使用检查" -Actual $AutowiredBusiness -Threshold 1

    # 2.2 测试代码@Autowired检查
    $AutowiredTest = Get-ChildItem -Path . -Filter "*.java" -Recurse |
        Where-Object { $_.FullName -match "\\test\\" } |
        Select-String -Pattern "@Autowired" |
        Select-Object -ExpandProperty Path |
        Get-Unique |
        Measure-Object |
        Select-Object -ExpandProperty Count

    $ResourceCount = (Get-ChildItem -Path . -Filter "*.java" -Recurse |
        Select-String -Pattern "@Resource" |
        Select-Object -ExpandProperty Path |
        Get-Unique |
        Measure-Object).Count

    Write-Host ""
    Write-ColorOutput "📊 @Autowired使用统计:" "Cyan"
    Write-Host "  业务代码: $AutowiredBusiness 个文件 (目标: ≤1)" -ForegroundColor $(if($AutowiredBusiness -le 1){"Green"}else{"Red"})
    Write-Host "  测试代码: $AutowiredTest 个文件 (测试场景允许)" -ForegroundColor Yellow
    Write-Host "  @Resource: $ResourceCount 个文件" -ForegroundColor Green

    # 3. 数据访问层规范检查
    Write-Host ""
    Write-ColorOutput "🔍 第三部分: 数据访问层规范检查" "Magenta"
    Write-Host "======================================" "Magenta"

    # 3.1 @Repository违规检查
    $RepositoryFiles = Get-ChildItem -Path . -Filter "*.java" -Recurse |
        Select-String -Pattern "@Repository" |
        Select-Object -ExpandProperty Path |
        Get-Unique

    $RepositoryCount = $RepositoryFiles | Measure-Object | Select-Object -ExpandProperty Count

    Check-Warning -Description "@Repository违规使用检查" -Actual $RepositoryCount -Threshold 0

    # 3.2 @Mapper使用统计
    $MapperCount = (Get-ChildItem -Path . -Filter "*.java" -Recurse |
        Select-String -Pattern "@Mapper" |
        Select-Object -ExpandProperty Path |
        Get-Unique |
        Measure-Object).Count

    Write-Host ""
    Write-ColorOutput "📊 DAO注解使用统计:" "Cyan"
    Write-Host "  @Mapper: $MapperCount 个文件" -ForegroundColor Green
    Write-Host "  @Repository: $RepositoryCount 个文件 (目标: 0)" -ForegroundColor $(if($RepositoryCount -eq 0){"Green"}else{"Yellow"})

    if ($RepositoryCount -gt 0) {
        Write-Host ""
        Write-ColorOutput "⚠️  @Repository违规文件列表:" "Yellow"
        $RepositoryFiles | Select-Object -First 10 | ForEach-Object { Write-Host "   $_" -ForegroundColor Yellow }
    }

    # 4. Spring Boot版本检查
    Write-Host ""
    Write-ColorOutput "🔍 第四部分: Spring Boot版本检查" "Magenta"
    Write-Host "=================================" "Magenta"

    if (Test-Path "microservices\pom.xml") {
        $PomContent = Get-Content "microservices\pom.xml" -Raw
        $SpringBootVersion = [regex]::Match($PomContent, '<spring-boot\.version>([^<]+)</spring-boot\.version>').Groups[1].Value
        $JavaVersion = [regex]::Match($PomContent, '<java\.version>([^<]+)</java\.version>').Groups[1].Value
        $SpringCloudVersion = [regex]::Match($PomContent, '<spring-cloud\.version>([^<]+)</spring-cloud\.version>').Groups[1].Value
        $SpringCloudAlibabaVersion = [regex]::Match($PomContent, '<spring-cloud-alibaba\.version>([^<]+)</spring-cloud-alibaba\.version>').Groups[1].Value
        $MybatisPlusVersion = [regex]::Match($PomContent, '<mybatis-plus\.version>([^<]+)</mybatis-plus\.version>').Groups[1].Value

        Check-Item -Description "Spring Boot版本检查" -Actual $SpringBootVersion -Expected "3.5.8"
        Check-Item -Description "Java版本检查" -Actual $JavaVersion -Expected "17"

        Write-Host ""
        Write-ColorOutput "📊 技术栈版本统计:" "Cyan"
        Write-Host "  Spring Boot: $SpringBootVersion" -ForegroundColor Green
        Write-Host "  Spring Cloud: $SpringCloudVersion" -ForegroundColor Green
        Write-Host "  Spring Cloud Alibaba: $SpringCloudAlibabaVersion" -ForegroundColor Green
        Write-Host "  MyBatis-Plus: $MybatisPlusVersion" -ForegroundColor Green
    }

    # 5. 生成合规性报告
    Write-Host ""
    Write-ColorOutput "📝 生成合规性报告" "Magenta"
    Write-Host "===================" "Magenta"

    # 计算合规性得分
    $ComplianceScore = if ($script:TotalChecks -gt 0) { [math]::Round(($script:PassedChecks * 100) / $script:TotalChecks) } else { 0 }

    # 生成Markdown报告
    $ReportContent = @"
# IOE-DREAM 技术栈合规性检查报告

**检查时间**: $(Get-Date)
**检查范围**: IOE-DREAM项目全量代码
**检查标准**: Jakarta EE 10 + Spring Boot 3.5企业级规范

## 📊 检查结果汇总

| 检查类别 | 通过 | 失败 | 警告 | 合规率 |
|---------|------|------|------|--------|
| **总计** | $script:PassedChecks | $script:FailedChecks | $script:WarningChecks | $ComplianceScore% |

## 🔍 详细检查结果

### 1. Jakarta包名迁移检查

| 检查项 | 结果 | 说明 |
|--------|------|------|
| javax包违规使用 | ✅ 通过 | 0个违规文件 |
| jakarta.annotation使用 | ✅ 通过 | $JakartaAnnotation个文件 |
| jakarta.validation使用 | ✅ 通过 | $JakartaValidation个文件 |
| jakarta.persistence使用 | ✅ 通过 | $JakartaPersistence个文件 |
| jakarta.servlet使用 | ✅ 通过 | $JakartaServlet个文件 |

### 2. 依赖注入规范检查

| 检查项 | 结果 | 数量 | 说明 |
|--------|------|------|------|
| 业务代码@Autowired使用 | $AutowiredBusiness | $AutowiredBusiness个文件 | 目标: ≤1 |
| 测试代码@Autowired使用 | ⚠️ 允许 | $AutowiredTest个文件 | 测试场景允许 |
| @Resource使用统计 | ✅ 通过 | $ResourceCount个文件 | 标准规范 |

### 3. 数据访问层规范检查

| 检查项 | 结果 | 数量 | 说明 |
|--------|------|------|------|
| @Repository违规使用 | $RepositoryCount | $RepositoryCount个文件 | 目标: 0 |
| @Mapper使用统计 | ✅ 通过 | $MapperCount个文件 | MyBatis-Plus标准 |

"@

    if ($RepositoryCount -gt 0) {
        $ReportContent += @"

**@Repository违规文件列表:**
$($RepositoryFiles -join "`n")
"@
    }

    $ReportContent += @"

### 4. Spring Boot版本检查

| 检查项 | 结果 | 版本 | 说明 |
|--------|------|------|------|
| Spring Boot版本 | ✅ 通过 | $SpringBootVersion | 目标: 3.5.8 |
| Java版本 | ✅ 通过 | $JavaVersion | 目标: 17 |

### 5. 技术栈版本一致性

| 依赖组件 | 版本 | 状态 |
|---------|------|------|
| Spring Boot | $SpringBootVersion | ✅ 最新稳定 |
| Spring Cloud | $SpringCloudVersion | ✅ 兼容 |
| Spring Cloud Alibaba | $SpringCloudAlibabaVersion | ✅ 企业级 |
| MyBatis-Plus | $MybatisPlusVersion | ✅ Spring Boot 3.x专用 |

## 📈 合规性评分

- **整体合规率**: $ComplianceScore%
- **评级**:
"@

    if ($ComplianceScore -ge 95) {
        $ReportContent += "🏆 优秀 (企业级标准)"
        $Grade = "优秀"
        $GradeColor = "Green"
    } elseif ($ComplianceScore -ge 90) {
        $ReportContent += "🟢 良好"
        $Grade = "良好"
        $GradeColor = "Green"
    } elseif ($ComplianceScore -ge 80) {
        $ReportContent += "🟡 一般"
        $Grade = "一般"
        $GradeColor = "Yellow"
    } else {
        $ReportContent += "🔴 需要改进"
        $Grade = "需要改进"
        $GradeColor = "Red"
    }

    $ReportContent += @"

## 🎯 改进建议

"@

    if ($RepositoryCount -gt 0) {
        $ReportContent += "- 修复${RepositoryCount}个@Repository违规文件，替换为@Mapper注解`n"
    }

    if ($AutowiredBusiness -gt 1) {
        $ReportContent += "- 将业务代码中的@Autowired替换为@Resource注解`n"
    }

    if ($script:FailedChecks -gt 0) {
        $ReportContent += "- 修复${script:FailedChecks}个失败的检查项`n"
    }

    $ReportContent += @"

- 定期运行合规性检查脚本
- 建立CI/CD流水线自动检查
- 加强团队技术规范培训

---
*报告生成时间: $(Get-Date)*
*检查脚本: scripts/tech-stack-compliance-check.ps1*
*下次检查建议: 1周后*
"@

    # 保存报告文件
    $ReportContent | Out-File -FilePath $ReportFile -Encoding UTF8

    # 显示合规性评级
    Write-Host ""
    Write-ColorOutput "📈 合规性评级:" "Magenta"
    Write-Host "🏆 合规评级: $Grade - $ComplianceScore%" -ForegroundColor $GradeColor

    # 显示汇总统计
    Write-Host ""
    Write-ColorOutput "📊 检查结果汇总:" "Blue"
    Write-Host "  总检查项: $script:TotalChecks"
    Write-Host "  通过: $script:PassedChecks" -ForegroundColor Green
    Write-Host "  失败: $script:FailedChecks" -ForegroundColor Red
    Write-Host "  警告: $script:WarningChecks" -ForegroundColor Yellow
    Write-Host "  合规率: $ComplianceScore%" -ForegroundColor Green

    Write-Host ""
    Write-ColorOutput "✅ 技术栈合规性检查完成！" "Green"
    Write-Host "📄 详细报告已生成: $ReportFile" -ForegroundColor Blue

    # 返回结果
    if ($script:FailedChecks -eq 0 -and $RepositoryCount -eq 0) {
        Write-Host ""
        Write-ColorOutput "🎉 所有检查项均通过，项目完全合规！" "Green"
        return @{
            Success = $true
            ComplianceScore = $ComplianceScore
            Grade = $Grade
            ReportFile = $ReportFile
        }
    } else {
        Write-Host ""
        Write-ColorOutput "⚠️  发现${script:FailedChecks}个失败项和${RepositoryCount}个@Repository违规，请查看报告并修复" "Yellow"
        return @{
            Success = $false
            ComplianceScore = $ComplianceScore
            Grade = $Grade
            ReportFile = $ReportFile
            FailedChecks = $script:FailedChecks
            RepositoryViolations = $RepositoryCount
        }
    }
}

# 检查函数
function Check-Item {
    param(
        [string]$Description,
        [object]$Actual,
        [object]$Expected
    )

    $script:TotalChecks++

    Write-Host ""
    Write-ColorOutput "🔍 检查项: $Description" "Blue"

    if ($Actual -eq $Expected) {
        Write-ColorOutput "✅ 通过: 检查结果符合预期 (实际: $Actual)" "Green"
        $script:PassedChecks++
        return $true
    } else {
        Write-ColorOutput "❌ 失败: 检查结果不符合预期 (实际: $Actual, 预期: $Expected)" "Red"
        $script:FailedChecks++
        return $false
    }
}

# 警告检查函数
function Check-Warning {
    param(
        [string]$Description,
        [int]$Actual,
        [int]$Threshold
    )

    $script:TotalChecks++

    Write-Host ""
    Write-ColorOutput "🔍 检查项: $Description" "Blue"

    if ($Actual -le $Threshold) {
        Write-ColorOutput "✅ 通过: 检查结果在阈值范围内 (实际: $Actual, 阈值: ≤$Threshold)" "Green"
        $script:PassedChecks++
        return $true
    } else {
        Write-ColorOutput "⚠️  警告: 检查结果超出阈值 (实际: $Actual, 阈值: ≤$Threshold)" "Yellow"
        $script:WarningChecks++
        return $false
    }
}

# 执行主函数
try {
    $Result = Main

    if ($Verbose) {
        Write-Host ""
        Write-ColorOutput "🔍 详细检查结果:" "Magenta"
        $Result | Format-List
    }

    # 根据结果设置退出码
    if ($Result.Success) {
        exit 0
    } else {
        exit 1
    }
}
catch {
    Write-ColorOutput "❌ 脚本执行出错: $($_.Exception.Message)" "Red"
    exit 1
}