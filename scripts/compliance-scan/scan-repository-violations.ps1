# @Repository违规扫描脚本
# 用途: 扫描项目中所有@Repository注解使用情况
# 执行: powershell -ExecutionPolicy Bypass -File scan-repository-violations.ps1

$projectRoot = "D:\IOE-DREAM"
$microservicesPath = "$projectRoot\microservices"
$outputFile = "$projectRoot\reports\repository-violations-$(Get-Date -Format 'yyyyMMdd-HHmmss').txt"

Write-Host "🔍 开始扫描 @Repository 违规..." -ForegroundColor Cyan
Write-Host "扫描路径: $microservicesPath" -ForegroundColor Gray

# 创建报告目录
New-Item -ItemType Directory -Force -Path "$projectRoot\reports" | Out-Null

# 初始化统计
$violationCount = 0
$fileCount = 0
$violations = @()

# 扫描所有Java文件
Get-ChildItem -Path $microservicesPath -Recurse -Filter "*.java" | ForEach-Object {
    $file = $_
    $relativePath = $file.FullName.Replace($projectRoot + "\", "")
    
    # 读取文件内容
    $content = Get-Content $file.FullName -Raw
    
    # 检查是否包含@Repository注解
    if ($content -match "@Repository") {
        $violationCount++
        
        # 记录违规信息
        $violations += [PSCustomObject]@{
            File = $relativePath
            FullPath = $file.FullName
            Type = "DAO注解违规"
        }
        
        Write-Host "❌ 发现违规: $relativePath" -ForegroundColor Red
    }
    
    # 检查是否使用Repository后缀命名
    if ($file.Name -like "*Repository.java" -and $file.Name -notlike "*RepositoryTest.java") {
        $fileCount++
        
        # 记录违规信息
        $violations += [PSCustomObject]@{
            File = $relativePath
            FullPath = $file.FullName
            Type = "命名规范违规"
        }
        
        Write-Host "❌ 发现命名违规: $relativePath" -ForegroundColor Yellow
    }
}

# 生成报告
$report = @"
# @Repository违规扫描报告

**扫描时间**: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
**扫描路径**: $microservicesPath

## 扫描统计

| 指标 | 数量 |
|------|------|
| @Repository注解违规 | $violationCount |
| Repository命名违规 | $fileCount |
| 总违规数 | $($violationCount + $fileCount) |

## 违规详情

"@

foreach ($violation in $violations) {
    $report += @"

### $($violation.Type)
- **文件**: ``$($violation.File)``
- **完整路径**: ``$($violation.FullPath)``

"@
}

$report += @"

## 修复建议

1. **@Repository注解**:
   - 替换为 ``@Mapper`` 注解
   - 更新import: ``import org.apache.ibatis.annotations.Mapper;``

2. **Repository命名**:
   - 将 ``*Repository.java`` 重命名为 ``*Dao.java``
   - 更新所有引用

## 自动修复命令

```powershell
# 替换@Repository为@Mapper
Get-ChildItem -Path "$microservicesPath" -Recurse -Filter "*.java" | 
ForEach-Object {
    (Get-Content `$_.FullName) -replace '@Repository', '@Mapper' |
    Set-Content `$_.FullName
}

# 更新import语句
Get-ChildItem -Path "$microservicesPath" -Recurse -Filter "*.java" | 
ForEach-Object {
    (Get-Content `$_.FullName) -replace 'org.springframework.stereotype.Repository', 'org.apache.ibatis.annotations.Mapper' |
    Set-Content `$_.FullName
}
```

---
**报告生成**: IOE-DREAM 合规性检查系统
"@

# 保存报告
$report | Out-File -FilePath $outputFile -Encoding UTF8

Write-Host "`n✅ 扫描完成！" -ForegroundColor Green
Write-Host "📊 统计结果:" -ForegroundColor Cyan
Write-Host "   - @Repository注解违规: $violationCount" -ForegroundColor $(if ($violationCount -gt 0) { "Red" } else { "Green" })
Write-Host "   - Repository命名违规: $fileCount" -ForegroundColor $(if ($fileCount -gt 0) { "Yellow" } else { "Green" })
Write-Host "   - 总违规数: $($violationCount + $fileCount)" -ForegroundColor $(if (($violationCount + $fileCount) -gt 0) { "Red" } else { "Green" })
Write-Host "📄 报告已保存: $outputFile" -ForegroundColor Cyan

