# 架构违规扫描脚本
# 用途: 扫描Controller层直接注入DAO/Manager的违规情况
# 执行: powershell -ExecutionPolicy Bypass -File scan-architecture-violations.ps1

$projectRoot = "D:\IOE-DREAM"
$microservicesPath = "$projectRoot\microservices"
$outputFile = "$projectRoot\reports\architecture-violations-$(Get-Date -Format 'yyyyMMdd-HHmmss').txt"

Write-Host "🔍 开始扫描架构违规..." -ForegroundColor Cyan
Write-Host "扫描路径: $microservicesPath" -ForegroundColor Gray

# 创建报告目录
New-Item -ItemType Directory -Force -Path "$projectRoot\reports" | Out-Null

# 初始化统计
$daoViolations = @()
$managerViolations = @()

# 扫描所有Controller文件
Get-ChildItem -Path $microservicesPath -Recurse -Filter "*Controller.java" | ForEach-Object {
    $file = $_
    $relativePath = $file.FullName.Replace($projectRoot + "\", "")
    
    # 读取文件内容
    $content = Get-Content $file.FullName -Raw
    
    # 检查是否为RestController
    if ($content -match "@RestController" -or $content -match "@Controller") {
        
        # 检查是否直接注入DAO
        if ($content -match "@Resource\s+private\s+\w+Dao\s+\w+;") {
            $matches = [regex]::Matches($content, "@Resource\s+private\s+(\w+Dao)\s+(\w+);")
            
            foreach ($match in $matches) {
                $daoViolations += [PSCustomObject]@{
                    File = $relativePath
                    FullPath = $file.FullName
                    DaoType = $match.Groups[1].Value
                    FieldName = $match.Groups[2].Value
                }
                
                Write-Host "❌ Controller直接注入DAO: $relativePath -> $($match.Groups[1].Value)" -ForegroundColor Red
            }
        }
        
        # 检查是否直接注入Manager
        if ($content -match "@Resource\s+private\s+\w+Manager\s+\w+;") {
            $matches = [regex]::Matches($content, "@Resource\s+private\s+(\w+Manager)\s+(\w+);")
            
            foreach ($match in $matches) {
                $managerViolations += [PSCustomObject]@{
                    File = $relativePath
                    FullPath = $file.FullName
                    ManagerType = $match.Groups[1].Value
                    FieldName = $match.Groups[2].Value
                }
                
                Write-Host "❌ Controller直接注入Manager: $relativePath -> $($match.Groups[1].Value)" -ForegroundColor Yellow
            }
        }
    }
}

# 生成报告
$report = @"
# 架构违规扫描报告

**扫描时间**: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
**扫描路径**: $microservicesPath

## 扫描统计

| 违规类型 | 数量 |
|---------|------|
| Controller直接注入DAO | $($daoViolations.Count) |
| Controller直接注入Manager | $($managerViolations.Count) |
| 总违规数 | $($daoViolations.Count + $managerViolations.Count) |

## Controller直接注入DAO违规

"@

if ($daoViolations.Count -gt 0) {
    foreach ($violation in $daoViolations) {
        $report += @"

### ``$($violation.File)``
- **注入类型**: ``$($violation.DaoType)``
- **字段名称**: ``$($violation.FieldName)``
- **完整路径**: ``$($violation.FullPath)``

"@
    }
} else {
    $report += "`n✅ 未发现Controller直接注入DAO违规`n"
}

$report += @"

## Controller直接注入Manager违规

"@

if ($managerViolations.Count -gt 0) {
    foreach ($violation in $managerViolations) {
        $report += @"

### ``$($violation.File)``
- **注入类型**: ``$($violation.ManagerType)``
- **字段名称**: ``$($violation.FieldName)``
- **完整路径**: ``$($violation.FullPath)``

"@
    }
} else {
    $report += "`n✅ 未发现Controller直接注入Manager违规`n"
}

$report += @"

## 架构规范要求

### 四层架构
``````
Controller → Service → Manager → DAO
``````

### 职责划分

1. **Controller层**:
   - ✅ 只能注入Service层
   - ❌ 禁止直接注入DAO层
   - ❌ 禁止直接注入Manager层
   - 职责: 接收请求、参数验证、返回响应

2. **Service层**:
   - ✅ 可以注入DAO层
   - ✅ 可以注入Manager层
   - 职责: 核心业务逻辑、事务管理

3. **Manager层**:
   - ✅ 可以注入DAO层
   - 职责: 复杂流程编排、缓存管理

4. **DAO层**:
   - 职责: 数据库访问

## 修复建议

对于Controller直接注入DAO/Manager的情况，需要：

1. 创建对应的Service接口和实现类
2. 将业务逻辑从Controller移至Service
3. Controller改为注入Service
4. Service层调用DAO/Manager

---
**报告生成**: IOE-DREAM 合规性检查系统
"@

# 保存报告
$report | Out-File -FilePath $outputFile -Encoding UTF8

Write-Host "`n✅ 扫描完成！" -ForegroundColor Green
Write-Host "📊 统计结果:" -ForegroundColor Cyan
Write-Host "   - Controller直接注入DAO: $($daoViolations.Count)" -ForegroundColor $(if ($daoViolations.Count -gt 0) { "Red" } else { "Green" })
Write-Host "   - Controller直接注入Manager: $($managerViolations.Count)" -ForegroundColor $(if ($managerViolations.Count -gt 0) { "Yellow" } else { "Green" })
Write-Host "   - 总违规数: $($daoViolations.Count + $managerViolations.Count)" -ForegroundColor $(if (($daoViolations.Count + $managerViolations.Count) -gt 0) { "Red" } else { "Green" })
Write-Host "📄 报告已保存: $outputFile" -ForegroundColor Cyan

