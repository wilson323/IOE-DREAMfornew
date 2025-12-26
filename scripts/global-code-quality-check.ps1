# IOE-DREAM 全局代码质量检查脚本
# 用途：检查所有Java文件的类声明、Logger初始化等基础语法问题
# 作者: IOE-DREAM 架构委员会
# 日期: 2025-01-30

param(
    [string]$ServicePath = "microservices",
    [switch]$FixIssues = $false
)

Write-Host "=== IOE-DREAM 全局代码质量检查 ===" -ForegroundColor Cyan
Write-Host "检查路径: $ServicePath" -ForegroundColor Yellow
Write-Host "修复模式: $(if ($FixIssues) { '启用' } else { '仅检查' })" -ForegroundColor Yellow
Write-Host ""

$issues = @()
$fixedFiles = 0

# 获取所有Java文件
$javaFiles = Get-ChildItem -Path $ServicePath -Recurse -Filter "*.java" | Where-Object {
    $_.FullName -notmatch "\\target\\" -and
    $_.FullName -notmatch "\\test\\" -and
    $_.Name -notlike "*Test.java"
}

Write-Host "找到 $($javaFiles.Count) 个Java文件，开始检查..." -ForegroundColor Green
Write-Host ""

foreach ($file in $javaFiles) {
    $content = Get-Content -Path $file.FullName -Raw -Encoding UTF8
    if (-not $content) { continue }

    $fileIssues = @()
    $needsFix = $false
    $newContent = $content

    # 1. 检查类声明缺失
    if ($content -match '(?m)(@RestController|@Controller|@Service|@Component|@Configuration|@RestControllerAdvice|@ControllerAdvice|@Mapper)\s*\r?\n\s*\{') {
        $className = [System.IO.Path]::GetFileNameWithoutExtension($file.Name)
        $fileIssues += "类声明缺失: 在注解后缺少 public class $className {"

        if ($FixIssues) {
            # 修复类声明
            $pattern = '(@RestController|@Controller|@Service|@Component|@Configuration|@RestControllerAdvice|@ControllerAdvice|@Mapper)([^\r\n]*)\r?\n\s*\{'
            $replacement = "`$1`$2`r`npublic class $className {"
            $newContent = $newContent -replace $pattern, $replacement
            $needsFix = $true
        }
    }

    # 2. 检查Logger初始化使用错误的类名
    if ($content -match 'LoggerFactory\.getLogger\(SmartRequestUtil\.class\)') {
        $className = [System.IO.Path]::GetFileNameWithoutExtension($file.Name)
        $fileIssues += "Logger初始化错误: 使用了 SmartRequestUtil.class，应该是 $className.class"

        if ($FixIssues) {
            $newContent = $newContent -replace 'LoggerFactory\.getLogger\(SmartRequestUtil\.class\)', "LoggerFactory.getLogger($className.class)"
            $needsFix = $true
        }
    }

    # 3. 检查Logger声明不完整
    if ($content -match '(?m)private static final Logger\s*$') {
        $className = [System.IO.Path]::GetFileNameWithoutExtension($file.Name)
        $fileIssues += "Logger声明不完整: 缺少 = LoggerFactory.getLogger($className.class);"

        if ($FixIssues) {
            $pattern = '(?m)(private static final Logger)\s*$'
            $replacement = "`$1 log = LoggerFactory.getLogger($className.class);"
            $newContent = $newContent -replace $pattern, $replacement
            $needsFix = $true
        }
    }

    # 4. 检查UTF-8 BOM问题
    $bomChar = [char]0xFEFF
    if ($content.StartsWith($bomChar)) {
        $fileIssues += "UTF-8 BOM问题: 文件包含BOM标记"

        if ($FixIssues) {
            $newContent = $newContent.Substring(1)
            $needsFix = $true
        }
    }

    # 5. 检查缺少Logger导入
    if ($content -match 'LoggerFactory\.getLogger' -and $content -notmatch 'import org\.slf4j\.LoggerFactory') {
        $fileIssues += "缺少Logger导入: 使用了LoggerFactory但未导入"

        if ($FixIssues) {
            # 在package声明后添加导入
            if ($newContent -match '(package\s+[^;]+;)') {
                $pattern = '(package\s+[^;]+;)'
                $replacement = "`$1`r`n`r`nimport org.slf4j.Logger;`r`nimport org.slf4j.LoggerFactory;"
                $newContent = $newContent -replace $pattern, $replacement
                $needsFix = $true
            }
        }
    }

    # 记录问题
    if ($fileIssues.Count -gt 0) {
        $relativePath = $file.FullName.Replace((Resolve-Path $ServicePath).Path + "\", "")
        $issues += [PSCustomObject]@{
            File   = $relativePath
            Issues = $fileIssues
        }

        Write-Host "发现问题: $relativePath" -ForegroundColor Yellow
        foreach ($issue in $fileIssues) {
            Write-Host "  - $issue" -ForegroundColor Red
        }
    }

    # 修复文件
    if ($needsFix -and $FixIssues) {
        try {
            $utf8WithoutBom = New-Object System.Text.UTF8Encoding($false)
            [System.IO.File]::WriteAllText($file.FullName, $newContent, $utf8WithoutBom)
            $fixedFiles++
            Write-Host "  ✓ 已修复" -ForegroundColor Green
        }
        catch {
            Write-Host "  ✗ 修复失败: $($_.Exception.Message)" -ForegroundColor Red
        }
    }
}

Write-Host ""
Write-Host "=== 检查完成 ===" -ForegroundColor Cyan
Write-Host "发现问题文件: $($issues.Count)" -ForegroundColor $(if ($issues.Count -eq 0) { "Green" } else { "Yellow" })
Write-Host "修复文件数: $fixedFiles" -ForegroundColor $(if ($fixedFiles -gt 0) { "Green" } else { "Gray" })

# 生成报告
$reportPath = "documentation/technical/GLOBAL_CODE_QUALITY_CHECK_REPORT.md"
$reportDate = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
$checkMode = if ($FixIssues) { '检查并修复' } else { '仅检查' }

$reportContent = @"
# 全局代码质量检查报告

> **检查日期**: $reportDate
> **检查范围**: $ServicePath
> **检查模式**: $checkMode

---

## 检查结果概览

- **检查文件数**: $($javaFiles.Count)
- **发现问题文件**: $($issues.Count)
- **修复文件数**: $fixedFiles

---

## 问题详情

"@

if ($issues.Count -eq 0) {
    $reportContent += @"

✅ **未发现任何问题！**

所有Java文件都通过了基础语法检查：
- ✅ 类声明完整
- ✅ Logger初始化正确
- ✅ Logger声明完整
- ✅ 导入语句正确
- ✅ 无UTF-8 BOM问题

"@
}
else {
    foreach ($issue in $issues) {
        $reportContent += @"

### $($issue.File)

"@
        foreach ($item in $issue.Issues) {
            $reportContent += "- $item`n"
        }
    }
}

$reportContent += @"

---

## 检查项清单

- [x] 类声明完整性检查
- [x] Logger初始化类名检查
- [x] Logger声明完整性检查
- [x] UTF-8 BOM检查
- [x] Logger导入语句检查

---

## 后续建议

### 立即执行（P0级）

1. **修复所有发现的问题**
   ```powershell
   .\scripts\global-code-quality-check.ps1 -FixIssues
   ```

2. **验证编译通过**
   ```powershell
   mvn clean compile -DskipTests
   ```

### 短期措施（P1级 - 1周内）

- [ ] 配置CI/CD编译检查
- [ ] 配置Git Pre-commit Hook
- [ ] 建立代码审查流程

---

**生成时间**: $reportDate
**检查脚本**: `scripts/global-code-quality-check.ps1`
"@

$utf8WithoutBom = New-Object System.Text.UTF8Encoding($false)
[System.IO.File]::WriteAllText($reportPath, $reportContent, $utf8WithoutBom)

Write-Host ""
Write-Host "报告已生成: $reportPath" -ForegroundColor Green

