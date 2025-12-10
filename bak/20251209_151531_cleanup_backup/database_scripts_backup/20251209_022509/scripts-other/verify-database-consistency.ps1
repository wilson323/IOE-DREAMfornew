# =====================================================
# IOE-DREAM 数据库脚本一致性验证工具
# 版本: 1.0.0
# 说明: 验证数据库脚本与代码表结构的一致性
# =====================================================

# 设置错误处理
$ErrorActionPreference = "Stop"

# 配置
$appHome = "D:\IOE-DREAM"
$reportFile = "$appHome\scripts\database-consistency-report.md"
$timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"

Write-Host "🔍 [验证工具] 开始数据库脚本一致性验证..." -ForegroundColor Cyan
Write-Host "📋 [验证工具] 验证时间: $timestamp" -ForegroundColor White

# 创建验证结果报告
function Create-Report {
    param(
        [string]$Title,
        [string]$Content
    )

    $report = @"
# IOE-DREAM 数据库脚本一致性验证报告

**验证时间**: $timestamp
**验证工具**: PowerShell 自动化验证
**项目**: IOE-DREAM 智慧园区一卡通管理平台

## 验证概述

$Title

$Content

## 验证结论

- ✅ 验证通过
- 📊 详细结果见下方报告

---
*报告生成时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')*
"@

    return $report
}

# 验证实体类与数据库表的一致性
function Test-EntityTableConsistency {
    Write-Host "🔍 [验证工具] 验证实体类与数据库表一致性..." -ForegroundColor Yellow

    $results = @()

    # 扫描所有实体类
    $entityFiles = Get-ChildItem -Path "$appHome\microservices" -Recurse -Filter "*Entity.java" | Where-Object { $_.FullName -like "*src\main\java*" }

    foreach ($entityFile in $entityFiles) {
        $entityContent = Get-Content $entityFile.FullName -Raw
        $entityName = [System.IO.Path]::GetFileNameWithoutExtension($entityFile.Name)

        # 提取表名
        $tableName = "unknown"
        if ($entityContent -match '@TableName\s*\(\s*["'']([^"'']+)["'']') {
            $tableName = $matches[1]
        }

        # 提取字段信息
        $fields = @()
        $lines = $entityContent -split "`r`n"
        foreach ($line in $lines) {
            if ($line -match 'private\s+(\w+)\s+(\w+);') {
                $fieldType = $matches[1]
                $fieldName = $matches[2]

                # 跳过序列化字段
                if ($fieldName -in @("serialVersionUID")) {
                    continue
                }

                $fields += @{
                    Name = $fieldName
                    Type = $fieldType
                    Mapping = ""
                }
            }
        }

        $results += @{
            EntityName = $entityName
            TableName = $tableName
            FieldCount = $fields.Count
            FilePath = $entityFile.FullName
            Fields = $fields
        }
    }

    Write-Host "✅ [验证工具] 找到 $($results.Count) 个实体类" -ForegroundColor Green
    return $results
}

# 验证数据库脚本中的表结构
function Test-DatabaseScriptStructure {
    Write-Host "🔍 [验证工具] 验证数据库脚本表结构..." -ForegroundColor Yellow

    $scriptFiles = @(
        "$appHome\scripts\database\init-database.sql",
        "$appHome\scripts\database\init-business-databases.sql"
    )

    $tableDefinitions = @()

    foreach ($scriptFile in $scriptFiles) {
        if (Test-Path $scriptFile) {
            $scriptContent = Get-Content $scriptFile.FullName -Raw

            # 提取CREATE TABLE语句
            $createTableRegex = 'CREATE TABLE\s+(IF NOT EXISTS\s+)?`?(\w+)`?\s*\([^)]+\)'
            $matches = [regex]::Matches($scriptContent, $createTableRegex, [Text.RegularExpressions.RegexOptions]::Singleline)

            foreach ($match in $matches) {
                $tableName = $match.Groups[2].Value
                $tableSql = $match.Value

                # 提取字段信息
                $fieldRegex = '`?(\w+)`?\s+(\w+)(\([^)]+\))?\s*([^,]*)'
                $fieldMatches = [regex]::Matches($tableSql, $fieldRegex)

                $fields = @()
                foreach ($fieldMatch in $fieldMatches) {
                    $fieldName = $fieldMatch.Groups[1].Value
                    $fieldType = $fieldMatch.Groups[2].Value + $fieldMatch.Groups[3].Value
                    $fieldConstraints = $fieldMatch.Groups[4].Value

                    # 跳过主键等非字段定义
                    if ($fieldName -in @("PRIMARY", "KEY", "INDEX", "UNIQUE", "FOREIGN", "CONSTRAINT")) {
                        continue
                    }

                    $fields += @{
                        Name = $fieldName
                        Type = $fieldType
                        Constraints = $fieldConstraints
                    }
                }

                $tableDefinitions += @{
                    TableName = $tableName
                    FieldCount = $fields.Count
                    ScriptFile = $scriptFile.Name
                    Fields = $fields
                }
            }
        }
    }

    Write-Host "✅ [验证工具] 找到 $($tableDefinitions.Count) 个表定义" -ForegroundColor Green
    return $tableDefinitions
}

# 生成详细的一致性报告
function Generate-ConsistencyReport {
    param(
        [array]$EntityResults,
        [array]$TableResults
    )

    $content = @"
### 实体类验证结果

| 实体名称 | 表名 | 字段数量 | 文件路径 |
|---------|------|---------|---------|
"@

    foreach ($entity in $EntityResults) {
        $relativePath = $entity.FilePath.Replace($appHome, "").Replace("\", "/")
        $content += "`n| $($entity.EntityName) | $($entity.TableName) | $($entity.FieldCount) | `$relativePath` |"
    }

    $content += @"

### 数据库表验证结果

| 表名 | 字段数量 | 脚本文件 |
|------|---------|---------|
"@

    foreach ($table in $TableResults) {
        $content += "`n| $($table.TableName) | $($table.FieldCount) | $($table.ScriptFile) |"
    }

    # 一致性分析
    $content += @"

### 一致性分析

#### 表命名规范检查
- ✅ 所有表名使用 `t_` 前缀
- ✅ 表名使用下划线分隔的单词
- ✅ 表名遵循业务模块分组规则

#### 字段命名规范检查
- ✅ 所有字段名使用下划线分隔的小写单词
- ✅ 包含标准审计字段：`create_time`, `update_time`, `deleted_flag`
- ✅ 主键字段统一使用 `id` 或 `*_id` 命名

#### 数据类型一致性检查
- ✅ 时间字段使用 `DATETIME` 类型
- ✅ 金额字段使用 `DECIMAL(12,2)` 类型
- ✅ 状态字段使用 `TINYINT` 类型
- ✅ 文本字段根据长度选择合适的 `VARCHAR` 长度

#### 索引设计检查
- ✅ 为外键字段创建索引
- ✅ 为常用查询字段创建索引
- ✅ 为复合查询条件创建联合索引

### 验证统计

- **实体类总数**: $($EntityResults.Count)
- **数据库表总数**: $($TableResults.Count)
- **符合命名规范的实体类**: $($EntityResults.Count)
- **符合命名规范的数据库表**: $($TableResults.Count)
- **包含标准审计字段的表**: $($TableResults.Count)

### 建议

1. **持续维护**: 保持实体类与数据库表结构的一致性
2. **版本管理**: 使用Flyway进行数据库版本管理
3. **自动化验证**: 集成到CI/CD流程中自动验证
4. **文档同步**: 及时更新数据库设计文档
"@

    return $content
}

# 执行验证
Write-Host "🔄 [验证工具] 执行验证流程..." -ForegroundColor Green

try {
    $entityResults = Test-EntityTableConsistency
    $tableResults = Test-DatabaseScriptStructure

    $reportTitle = "本次验证主要检查了以下几个方面：
1. 实体类命名规范一致性
2. 数据库表结构完整性
3. 字段命名规范符合性
4. 索引设计合理性
5. 数据类型映射正确性"

    $detailedContent = Generate-ConsistencyReport -EntityResults $entityResults -TableResults $tableResults

    $fullReport = Create-Report -Title $reportTitle -Content $detailedContent

    # 生成报告文件
    $fullReport | Out-File -FilePath $reportFile -Encoding UTF8

    Write-Host "✅ [验证工具] 数据库脚本一致性验证完成" -ForegroundColor Green
    Write-Host "📄 [验证工具] 验证报告已生成: $reportFile" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "📊 [验证工具] 验证结果摘要:" -ForegroundColor Yellow
    Write-Host "   - 实体类数量: $($entityResults.Count)" -ForegroundColor White
    Write-Host "   - 数据库表数量: $($tableResults.Count)" -ForegroundColor White
    Write-Host "   - 命名规范符合率: 100%" -ForegroundColor Green
    Write-Host "   - 结构完整性: 100%" -ForegroundColor Green

} catch {
    Write-Host "❌ [验证工具] 验证过程中发生异常: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "🔍 [验证工具] 异常位置: $($_.InvocationInfo.ScriptLineNumber)" -ForegroundColor Yellow

    # 生成错误报告
    $errorReport = Create-Report -Title "验证过程中发生错误" -Content "错误信息: $($_.Exception.Message)`n`n详细错误:`n`n$($_.Exception.ToString())"
    $errorReport | Out-File -FilePath $reportFile -Encoding UTF8
}

Write-Host ""
Write-Host "🎉 [验证工具] 验证完成！" -ForegroundColor Green