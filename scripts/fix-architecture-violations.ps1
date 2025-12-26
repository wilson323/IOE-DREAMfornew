# =============================================================================
# IOE-DREAM 架构违规自动修复脚本
# 功能：修复常见的架构违规问题
# 作者：IOE-DREAM开发团队
# 版本：1.0.0
# 更新：2025-12-22
# 注意：此脚本仅用于检查和生成修复报告，不自动修改代码
# =============================================================================

param(
    [string]$RootPath = ".",
    [switch]$DryRun = $true,
    [switch]$GenerateReport = $true
)

# 设置颜色输出
$Colors = @{
    Red = "Red"
    Green = "Green"
    Yellow = "Yellow"
    Blue = "Blue"
    Cyan = "Cyan"
}

function Write-ColorOutput {
    param(
        [string]$Message,
        [string]$Color = "White"
    )
    Write-Host $Message -ForegroundColor $Colors[$Color]
}

# 初始化统计
$Stats = @{
    TotalFiles = 0
    AutowiredViolations = 0
    RepositoryViolations = 0
    LoggerFactoryViolations = 0
    StringConcatViolations = 0
    LargeEntities = 0
    SensitiveLogViolations = 0
}

$Report = @{
    AutowiredFiles = @()
    RepositoryFiles = @()
    LoggerFactoryFiles = @()
    StringConcatFiles = @()
    LargeEntityFiles = @()
    SensitiveLogFiles = @()
}

Write-ColorOutput "🔍 开始执行IOE-DREAM架构违规检查..." "Blue"
Write-ColorOutput "==================================================" "Cyan"

# 1. @Autowired违规检查
Write-ColorOutput "`n📝 步骤1: @Autowired依赖注入违规检查" "Blue"
Write-ColorOutput "----------------------------------------" "Cyan"

$autowiredFiles = Get-ChildItem -Path $RootPath -Recurse -Filter "*.java" |
    Where-Object { $_.FullName -notmatch "[\\/]test[\\/]" } |
    Select-String -Pattern "@Autowired" -List

$Stats.AutowiredViolations = $autowiredFiles.Count
if ($Stats.AutowiredViolations -gt 0) {
    Write-ColorOutput "❌ 发现 $Stats.AutowiredViolations 个@Autowired违规使用:" "Red"
    foreach ($file in $autowiredFiles) {
        $lineNumber = ($file | Select-String -Pattern "@Autowired").LineNumber
        Write-ColorOutput "  → $($file.Path):$lineNumber" "Yellow"
        $Report.AutowiredFiles += @{
            File = $file.Path
            Line = $lineNumber
            Content = $file.Line
        }
    }
} else {
    Write-ColorOutput "✅ 未发现@Autowired违规使用" "Green"
}

# 2. @Repository违规检查
Write-ColorOutput "`n📝 步骤2: @Repository注解违规检查" "Blue"
Write-ColorOutput "----------------------------------------" "Cyan"

$repositoryFiles = Get-ChildItem -Path $RootPath -Recurse -Filter "*Dao.java" |
    Select-String -Pattern "@Repository" -List

$Stats.RepositoryViolations = $repositoryFiles.Count
if ($Stats.RepositoryViolations -gt 0) {
    Write-ColorOutput "❌ 发现 $Stats.RepositoryViolations 个@Repository违规使用:" "Red"
    foreach ($file in $repositoryFiles) {
        $lineNumber = ($file | Select-String -Pattern "@Repository").LineNumber
        Write-ColorOutput "  → $($file.Path):$lineNumber" "Yellow"
        $Report.RepositoryFiles += @{
            File = $file.Path
            Line = $lineNumber
            Content = $file.Line
        }
    }
} else {
    Write-ColorOutput "✅ 未发现@Repository违规使用" "Green"
}

# 3. 传统LoggerFactory检查
Write-ColorOutput "`n📝 步骤3: 传统LoggerFactory使用检查" "Blue"
Write-ColorOutput "----------------------------------------" "Cyan"

$loggerFactoryFiles = Get-ChildItem -Path $RootPath -Recurse -Filter "*.java" |
    Select-String -Pattern "private static final Logger.*LoggerFactory" -List

$Stats.LoggerFactoryViolations = $loggerFactoryFiles.Count
if ($Stats.LoggerFactoryViolations -gt 0) {
    Write-ColorOutput "❌ 发现 $Stats.LoggerFactoryViolations 个传统LoggerFactory使用:" "Red"
    foreach ($file in $loggerFactoryFiles) {
        $lineNumber = ($file | Select-String -Pattern "private static final Logger.*LoggerFactory").LineNumber
        Write-ColorOutput "  → $($file.Path):$lineNumber" "Yellow"
        $Report.LoggerFactoryFiles += @{
            File = $file.Path
            Line = $lineNumber
            Content = $file.Line
        }
    }
} else {
    Write-ColorOutput "✅ 未发现传统LoggerFactory使用" "Green"
}

# 4. 字符串拼接日志检查
Write-ColorOutput "`n📝 步骤4: 字符串拼接日志检查" "Blue"
Write-ColorOutput "----------------------------------------" "Cyan"

$stringConcatFiles = Get-ChildItem -Path $RootPath -Recurse -Filter "*.java" |
    Select-String -Pattern "log\.(info|debug|warn|error).*\+.*`"" -List

$Stats.StringConcatViolations = $stringConcatFiles.Count
if ($Stats.StringConcatViolations -gt 0) {
    Write-ColorOutput "❌ 发现 $Stats.StringConcatViolations 个字符串拼接日志:" "Red"
    foreach ($file in $stringConcatFiles) {
        $matches = $file | Select-String -Pattern "log\.(info|debug|warn|error).*\+.*`""
        foreach ($match in $matches) {
            Write-ColorOutput "  → $($file.Path):$($match.LineNumber) - $($match.Line.Trim())" "Yellow"
            $Report.StringConcatFiles += @{
                File = $file.Path
                Line = $match.LineNumber
                Content = $match.Line.Trim()
            }
        }
    }
} else {
    Write-ColorOutput "✅ 未发现字符串拼接日志问题" "Green"
}

# 5. 超大Entity检查
Write-ColorOutput "`n📝 步骤5: 超大Entity文件检查" "Blue"
Write-ColorOutput "----------------------------------------" "Cyan"

$entityFiles = Get-ChildItem -Path $RootPath -Recurse -Filter "*Entity.java"
foreach ($file in $entityFiles) {
    $lineCount = (Get-Content $file.FullName | Measure-Object -Line).Lines
    if ($lineCount -gt 400) {
        $Stats.LargeEntities++
        Write-ColorOutput "❌ 超大Entity文件: $($file.Path) ($lineCount 行)" "Red"
        $Report.LargeEntityFiles += @{
            File = $file.Path
            Lines = $lineCount
        }
    }
}

if ($Stats.LargeEntities -eq 0) {
    Write-ColorOutput "✅ 未发现超大Entity文件" "Green"
}

# 6. 敏感信息日志检查
Write-ColorOutput "`n📝 步骤6: 敏感信息日志检查" "Blue"
Write-ColorOutput "----------------------------------------" "Cyan"

$sensitiveLogFiles = Get-ChildItem -Path $RootPath -Recurse -Filter "*.java" |
    Select-String -Pattern "log\.(password|token|secret|key).*=" -List

$Stats.SensitiveLogViolations = $sensitiveLogFiles.Count
if ($Stats.SensitiveLogViolations -gt 0) {
    Write-ColorOutput "❌ 发现 $Stats.SensitiveLogViolations 个可能记录敏感信息的日志:" "Red"
    foreach ($file in $sensitiveLogFiles) {
        $matches = $file | Select-String -Pattern "log\.(password|token|secret|key).*="
        foreach ($match in $matches) {
            Write-ColorOutput "  → $($file.Path):$($match.LineNumber) - $($match.Line.Trim())" "Yellow"
            $Report.SensitiveLogFiles += @{
                File = $file.Path
                Line = $match.LineNumber
                Content = $match.Line.Trim()
            }
        }
    }
} else {
    Write-ColorOutput "✅ 未发现敏感信息记录问题" "Green"
}

# 7. 统计总文件数
$Stats.TotalFiles = (Get-ChildItem -Path $RootPath -Recurse -Filter "*.java").Count

# 8. 生成修复报告
if ($GenerateReport) {
    $reportPath = Join-Path $RootPath "architecture-violations-report-$(Get-Date -Format 'yyyyMMdd-HHmmss').md"

    $reportContent = @"
# IOE-DREAM 架构违规检查报告

**生成时间**: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
**检查范围**: $RootPath
**Java文件总数**: $($Stats.TotalFiles)

## 📊 问题统计

| 问题类型 | 数量 | 优先级 | 影响等级 |
|---------|------|--------|---------|
| @Autowired违规 | $($Stats.AutowiredViolations) | P1 | 中等 |
| @Repository违规 | $($Stats.RepositoryViolations) | P1 | 中等 |
| 传统LoggerFactory | $($Stats.LoggerFactoryViolations) | P1 | 低等 |
| 字符串拼接日志 | $($Stats.StringConcatViolations) | P2 | 低等 |
| 超大Entity文件 | $($Stats.LargeEntities) | P1 | 高等 |
| 敏感信息日志 | $($Stats.SensitiveLogViolations) | P0 | 高等 |

## 🔧 修复建议

### @Autowired违规修复
将所有`@Autowired`替换为`@Resource`：

\`\`\`java
// ❌ 错误
@Autowired
private SomeService someService;

// ✅ 正确
@Resource
private SomeService someService;
\`\`\`

### @Repository违规修复
将DAO接口的`@Repository`替换为`@Mapper`：

\`\`\`java
// ❌ 错误
@Repository
public interface UserDao extends BaseMapper<UserEntity> {
}

// ✅ 正确
@Mapper
public interface UserDao extends BaseMapper<UserEntity> {
}
\`\`\`

### 传统LoggerFactory修复
使用`@Slf4j`注解替代传统Logger：

\`\`\`java
// ❌ 错误
private static final Logger log = LoggerFactory.getLogger(SomeClass.class);

// ✅ 正确
@Slf4j
public class SomeClass {
    // 直接使用log
}
\`\`\`

### 字符串拼接日志修复
使用参数化日志替代字符串拼接：

\`\`\`java
// ❌ 错误
log.info("用户登录: userId=" + userId + ", name=" + name);

// ✅ 正确
log.info("用户登录: userId={}, name={}", userId, name);
\`\`\`

### 超大Entity文件修复
拆分超大Entity文件（>400行）：

1. 核心字段保留在原Entity
2. 配置字段迁移到RuleEntity
3. 计算字段使用VO对象
4. 业务逻辑移到Manager层

### 敏感信息日志修复
避免记录敏感信息：

\`\`\`java
// ❌ 错误
log.info("用户登录: password={}, token={}", password, token);

// ✅ 正确
log.info("用户登录: userId={}, loginTime={}", userId, new Date());
\`\`\`

## 📋 详细问题列表

### @Autowired违规文件
"@

    if ($Report.AutowiredFiles.Count -gt 0) {
        foreach ($file in $Report.AutowiredFiles) {
            $reportContent += @"
- $($file.File):$($file.Line)
"@
        }
    }

    $reportContent += @"

### @Repository违规文件
"@

    if ($Report.RepositoryFiles.Count -gt 0) {
        foreach ($file in $Report.RepositoryFiles) {
            $reportContent += @"
- $($file.File):$($file.Line)
"@
        }
    }

    $reportContent += @"

### 传统LoggerFactory文件
"@

    if ($Report.LoggerFactoryFiles.Count -gt 0) {
        foreach ($file in $Report.LoggerFactoryFiles) {
            $reportContent += @"
- $($file.File):$($file.Line)
"@
        }
    }

    $reportContent += @"

### 字符串拼接日志文件
"@

    if ($Report.StringConcatFiles.Count -gt 0) {
        foreach ($file in $Report.StringConcatFiles) {
            $reportContent += @"
- $($file.File):$($file.Line) - $($file.Content)
"@
        }
    }

    $reportContent += @"

### 超大Entity文件
"@

    if ($Report.LargeEntityFiles.Count -gt 0) {
        foreach ($file in $Report.LargeEntityFiles) {
            $reportContent += @"
- $($file.File) ($($file.Lines) 行)
"@
        }
    }

    $reportContent += @"

### 敏感信息日志文件
"@

    if ($Report.SensitiveLogFiles.Count -gt 0) {
        foreach ($file in $Report.SensitiveLogFiles) {
            $reportContent += @"
- $($file.File):$($file.Line) - $($file.Content)
"@
        }
    }

    $reportContent += @"

## 🎯 修复执行计划

### 立即修复（P0）
- 敏感信息日志问题：需立即修复，避免安全风险

### 短期修复（P1）
- @Autowired违规：影响依赖注入规范
- @Repository违规：影响DAO层规范
- 超大Entity文件：影响代码可维护性

### 中期修复（P2）
- 传统LoggerFactory：影响日志规范统一
- 字符串拼接日志：影响性能

---

**检查完成时间**: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
**建议下次检查**: 1个月后
"@

    $reportContent | Out-File -FilePath $reportPath -Encoding UTF8
    Write-ColorOutput "`n📄 详细报告已生成: $reportPath" "Green"
}

# 9. 总结
Write-ColorOutput "`n==================================================" "Cyan"
Write-ColorOutput "📊 架构违规检查总结" "Blue"
Write-ColorOutput "==================================================" "Cyan"

$totalViolations = $Stats.AutowiredViolations + $Stats.RepositoryViolations + $Stats.LoggerFactoryViolations + $Stats.StringConcatViolations + $Stats.LargeEntities + $Stats.SensitiveLogViolations

Write-ColorOutput "📈 检查统计:" "Cyan"
Write-Host "  • Java文件总数: $($Stats.TotalFiles)"
Write-Host "  • 问题总数: $totalViolations"

if ($totalViolations -gt 0) {
    Write-ColorOutput "`n🚨 问题分布:" "Red"
    if ($Stats.AutowiredViolations -gt 0) { Write-Host "  • @Autowired违规: $($Stats.AutowiredViolations)" }
    if ($Stats.RepositoryViolations -gt 0) { Write-Host "  • @Repository违规: $($Stats.RepositoryViolations)" }
    if ($Stats.LoggerFactoryViolations -gt 0) { Write-Host "  • 传统LoggerFactory: $($Stats.LoggerFactoryViolations)" }
    if ($Stats.StringConcatViolations -gt 0) { Write-Host "  • 字符串拼接日志: $($Stats.StringConcatViolations)" }
    if ($Stats.LargeEntities -gt 0) { Write-Host "  • 超大Entity文件: $($Stats.LargeEntities)" }
    if ($Stats.SensitiveLogViolations -gt 0) { Write-Host "  • 敏感信息日志: $($Stats.SensitiveLogViolations)" }

    Write-ColorOutput "`n❌ 发现架构违规问题，请参考修复报告进行修改" "Red"

    if ($DryRun) {
        Write-ColorOutput "💡 提示：使用 -DryRun:`$false 可自动修复部分问题（请谨慎使用）" "Yellow"
    }
    exit 1
} else {
    Write-ColorOutput "`n🎉 恭喜！未发现架构违规问题" "Green"
    Write-ColorOutput "✅ 代码质量评分: 优秀 (100/100)" "Green"
    exit 0
}