# IOE-DREAM 架构违规检查与报告脚本
# 功能: 检查架构违规（@Autowired、@Repository）并生成修复报告
# 重要: 本脚本仅检查不修改代码，所有修复必须手动完成
# 作者: IOE-DREAM架构团队
# 日期: 2025-01-30

$ErrorActionPreference = "Stop"
$script:TotalFiles = 0
$script:Violations = @{
    Autowired = @()
    Repository = @()
    RepositoryNaming = @()
}

# 颜色输出函数
function Write-ColorOutput {
    param(
        [string]$Message,
        [string]$Color = "White"
    )
    Write-Host $Message -ForegroundColor $Color
}

# 检查@Autowired违规
function Test-AutowiredViolations {
    param([string]$FilePath)

    $content = Get-Content $FilePath -Raw -Encoding UTF8
    if ($content -match '@Autowired') {
        # 提取行号和上下文
        $lines = Get-Content $FilePath -Encoding UTF8
        $lineNumbers = @()
        for ($i = 0; $i -lt $lines.Count; $i++) {
            if ($lines[$i] -match '@Autowired') {
                $lineNumbers += ($i + 1)
            }
        }
        $script:Violations.Autowired += @{
            File = $FilePath
            Lines = $lineNumbers
        }
        return $true
    }
    return $false
}

# 检查@Repository违规
function Test-RepositoryViolations {
    param([string]$FilePath)

    $content = Get-Content $FilePath -Raw -Encoding UTF8
    if ($content -match '@Repository') {
        # 提取行号和上下文
        $lines = Get-Content $FilePath -Encoding UTF8
        $lineNumbers = @()
        for ($i = 0; $i -lt $lines.Count; $i++) {
            if ($lines[$i] -match '@Repository') {
                $lineNumbers += ($i + 1)
            }
        }
        $script:Violations.Repository += @{
            File = $FilePath
            Lines = $lineNumbers
        }
        return $true
    }
    return $false
}

# 检查Repository命名违规
function Test-RepositoryNamingViolations {
    param([string]$FilePath)

    $fileName = Split-Path $FilePath -Leaf
    if ($fileName -match 'Repository\.java$') {
        $script:Violations.RepositoryNaming += $FilePath
        return $true
    }
    return $false
}

# 生成修复报告
function Export-FixReport {
    $reportPath = "$PSScriptRoot\..\documentation\technical\ARCHITECTURE_VIOLATIONS_FIX_REPORT.md"
    $reportDir = Split-Path $reportPath -Parent
    if (-not (Test-Path $reportDir)) {
        New-Item -ItemType Directory -Path $reportDir -Force | Out-Null
    }

    $report = @"
# IOE-DREAM 架构违规修复报告

**生成时间**: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
**检查范围**: 全项目微服务代码
**重要提示**: ⚠️ **所有修复必须手动完成，禁止使用脚本自动修改代码**

---

## 📊 违规统计

| 违规类型 | 文件数 | 总违规数 | 优先级 |
|---------|-------|---------|--------|
| @Autowired违规 | $($script:Violations.Autowired.Count) | $(($script:Violations.Autowired | ForEach-Object { $_.Lines.Count } | Measure-Object -Sum).Sum) | 🔴 P0 |
| @Repository违规 | $($script:Violations.Repository.Count) | $(($script:Violations.Repository | ForEach-Object { $_.Lines.Count } | Measure-Object -Sum).Sum) | 🔴 P0 |
| Repository命名违规 | $($script:Violations.RepositoryNaming.Count) | $($script:Violations.RepositoryNaming.Count) | 🔴 P0 |

---

## 🔴 @Autowired违规详情

**修复规范**: 必须手动将 `@Autowired` 替换为 `@Resource`，并更新import语句

"@

    foreach ($violation in $script:Violations.Autowired) {
        $report += @"

### $(Split-Path $violation.File -Leaf)

**文件路径**: `$($violation.File)`
**违规行号**: $($violation.Lines -join ', ')

**修复步骤**:
1. 打开文件: `$($violation.File)`
2. 找到第 $($violation.Lines[0]) 行（及后续违规行）
3. 将 `@Autowired` 替换为 `@Resource`
4. 检查import语句，确保使用 `import jakarta.annotation.Resource`
5. 删除旧的import: `import org.springframework.beans.factory.annotation.Autowired`

**修复示例**:
\`\`\`java
// ❌ 错误示例
import org.springframework.beans.factory.annotation.Autowired;

@Autowired
private UserService userService;

// ✅ 正确示例
import jakarta.annotation.Resource;

@Resource
private UserService userService;
\`\`\`

"@
    }

    $report += @"

---

## 🔴 @Repository违规详情

**修复规范**: 必须手动将 `@Repository` 替换为 `@Mapper`，并更新import语句

"@

    foreach ($violation in $script:Violations.Repository) {
        $report += @"

### $(Split-Path $violation.File -Leaf)

**文件路径**: `$($violation.File)`
**违规行号**: $($violation.Lines -join ', ')

**修复步骤**:
1. 打开文件: `$($violation.File)`
2. 找到第 $($violation.Lines[0]) 行（及后续违规行）
3. 将 `@Repository` 替换为 `@Mapper`
4. 检查import语句，确保使用 `import org.apache.ibatis.annotations.Mapper`
5. 删除旧的import: `import org.springframework.stereotype.Repository`

**修复示例**:
\`\`\`java
// ❌ 错误示例
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends BaseMapper<UserEntity> {
}

// ✅ 正确示例
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserDao extends BaseMapper<UserEntity> {
}
\`\`\`

"@
    }

    $report += @"

---

## 🔴 Repository命名违规详情

**修复规范**: 必须手动重命名文件，将 `Repository` 后缀改为 `Dao`

"@

    foreach ($file in $script:Violations.RepositoryNaming) {
        $report += @"

### $(Split-Path $file -Leaf)

**文件路径**: `$file`

**修复步骤**:
1. 重命名文件: 将 `*Repository.java` 改为 `*Dao.java`
2. 更新类名: 将类名中的 `Repository` 改为 `Dao`
3. 更新所有引用: 搜索项目中所有引用此文件的地方并更新

**修复示例**:
\`\`\`java
// ❌ 错误示例
// 文件名: UserRepository.java
@Mapper
public interface UserRepository extends BaseMapper<UserEntity> {
}

// ✅ 正确示例
// 文件名: UserDao.java
@Mapper
public interface UserDao extends BaseMapper<UserEntity> {
}
\`\`\`

"@
    }

    $report += @"

---

## ✅ 修复验证清单

修复完成后，请确认：

- [ ] 所有@Autowired已替换为@Resource
- [ ] 所有@Repository已替换为@Mapper
- [ ] 所有Repository命名已改为Dao
- [ ] import语句已正确更新
- [ ] 编译通过: `mvn clean compile`
- [ ] 单元测试通过: `mvn test`
- [ ] 架构合规性检查通过: `.\scripts\architecture-compliance-check.ps1`

---

## 📚 相关文档

- [架构规范文档](../../CLAUDE.md)
- [全局深度分析报告](./GLOBAL_DEEP_ANALYSIS_ROOT_CAUSE_SOLUTION.md)
- [手动修复指南](./MANUAL_FIX_GUIDE.md)

---

**报告生成时间**: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
**维护团队**: IOE-DREAM架构委员会
"@

    [System.IO.File]::WriteAllText($reportPath, $report, [System.Text.Encoding]::UTF8)
    Write-ColorOutput "📄 修复报告已生成: $reportPath" "Green"
    return $reportPath
}

# 主函数
function Main {
    Write-ColorOutput "========================================" "Cyan"
    Write-ColorOutput "IOE-DREAM 架构违规检查脚本" "Cyan"
    Write-ColorOutput "========================================" "Cyan"
    Write-Host ""
    Write-ColorOutput "⚠️  重要提示: 本脚本仅检查不修改代码" "Yellow"
    Write-ColorOutput "   所有修复必须手动完成，确保代码质量" "Yellow"
    Write-Host ""

    # 检查项目根目录
    $projectRoot = $PSScriptRoot
    if (-not (Test-Path "$projectRoot\..\microservices")) {
        Write-ColorOutput "❌ 错误: 未找到microservices目录，请确保在项目根目录执行脚本" "Red"
        exit 1
    }

    $microservicesPath = "$projectRoot\..\microservices"

    Write-ColorOutput "📁 扫描目录: $microservicesPath" "Yellow"
    Write-Host ""

    # 扫描所有Java文件
    $javaFiles = Get-ChildItem -Path $microservicesPath -Filter "*.java" -Recurse | Where-Object {
        $_.FullName -notmatch '\\target\\' -and
        $_.FullName -notmatch '\\test\\' -and
        $_.FullName -notmatch '\\archive\\'
    }

    $script:TotalFiles = $javaFiles.Count
    Write-ColorOutput "📊 发现 $($script:TotalFiles) 个Java文件" "Yellow"
    Write-Host ""

    # 检查@Autowired违规
    Write-ColorOutput "🔍 检查@Autowired违规..." "Yellow"
    foreach ($file in $javaFiles) {
        Test-AutowiredViolations $file.FullName | Out-Null
    }
    Write-ColorOutput "   发现 $($script:Violations.Autowired.Count) 个文件存在@Autowired违规" "Cyan"
    Write-Host ""

    # 检查@Repository违规
    Write-ColorOutput "🔍 检查@Repository违规..." "Yellow"
    foreach ($file in $javaFiles) {
        Test-RepositoryViolations $file.FullName | Out-Null
    }
    Write-ColorOutput "   发现 $($script:Violations.Repository.Count) 个文件存在@Repository违规" "Cyan"
    Write-Host ""

    # 检查Repository命名违规
    Write-ColorOutput "🔍 检查Repository命名违规..." "Yellow"
    foreach ($file in $javaFiles) {
        Test-RepositoryNamingViolations $file.FullName | Out-Null
    }
    Write-ColorOutput "   发现 $($script:Violations.RepositoryNaming.Count) 个文件存在Repository命名违规" "Cyan"
    Write-Host ""

    # 生成修复报告
    Write-ColorOutput "📄 生成修复报告..." "Yellow"
    $reportPath = Export-FixReport

    # 输出统计信息
    Write-ColorOutput "========================================" "Cyan"
    Write-ColorOutput "检查完成统计" "Cyan"
    Write-ColorOutput "========================================" "Cyan"
    Write-ColorOutput "总文件数: $($script:TotalFiles)" "White"
    Write-ColorOutput "@Autowired违规: $($script:Violations.Autowired.Count) 个文件" "Red"
    Write-ColorOutput "@Repository违规: $($script:Violations.Repository.Count) 个文件" "Red"
    Write-ColorOutput "Repository命名违规: $($script:Violations.RepositoryNaming.Count) 个文件" "Red"
    Write-Host ""
    Write-ColorOutput "📄 详细修复报告: $reportPath" "Green"
    Write-Host ""
    Write-ColorOutput "📝 下一步操作:" "Yellow"
    Write-ColorOutput "   1. 查看修复报告: $reportPath" "White"
    Write-ColorOutput "   2. 按照报告手动修复所有违规" "White"
    Write-ColorOutput "   3. 修复后运行验证: .\scripts\architecture-compliance-check.ps1" "White"
}

# 执行主函数
try {
    Main
}
catch {
    Write-ColorOutput "❌ 脚本执行失败: $_" "Red"
    Write-ColorOutput "   堆栈跟踪: $($_.ScriptStackTrace)" "Red"
    exit 1
}
