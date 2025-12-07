# @Autowired违规扫描脚本
# 用途: 扫描项目中所有@Autowired注解使用情况
# 执行: powershell -ExecutionPolicy Bypass -File scan-autowired-violations.ps1

$projectRoot = "D:\IOE-DREAM"
$microservicesPath = "$projectRoot\microservices"
$outputFile = "$projectRoot\reports\autowired-violations-$(Get-Date -Format 'yyyyMMdd-HHmmss').txt"

Write-Host "🔍 开始扫描 @Autowired 违规..." -ForegroundColor Cyan
Write-Host "扫描路径: $microservicesPath" -ForegroundColor Gray

# 创建报告目录
New-Item -ItemType Directory -Force -Path "$projectRoot\reports" | Out-Null

# 初始化统计
$violationCount = 0
$violations = @()

# 扫描所有Java文件
Get-ChildItem -Path $microservicesPath -Recurse -Filter "*.java" | ForEach-Object {
    $file = $_
    $relativePath = $file.FullName.Replace($projectRoot + "\", "")
    
    # 读取文件内容
    $content = Get-Content $file.FullName -Raw
    
    # 检查是否包含@Autowired注解
    if ($content -match "@Autowired") {
        # 统计该文件中的@Autowired数量
        $matches = [regex]::Matches($content, "@Autowired")
        $count = $matches.Count
        
        $violationCount += $count
        
        # 记录违规信息
        $violations += [PSCustomObject]@{
            File = $relativePath
            FullPath = $file.FullName
            Count = $count
        }
        
        Write-Host "❌ 发现违规: $relativePath ($count处)" -ForegroundColor Red
    }
}

# 生成报告
$report = @"
# @Autowired违规扫描报告

**扫描时间**: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
**扫描路径**: $microservicesPath

## 扫描统计

| 指标 | 数量 |
|------|------|
| 违规文件数 | $($violations.Count) |
| 违规注解总数 | $violationCount |

## 违规详情

| 文件 | 违规数量 |
|------|---------|
"@

foreach ($violation in $violations) {
    $report += "`n| ``$($violation.File)`` | $($violation.Count) |"
}

$report += @"


## 违规文件列表

"@

foreach ($violation in $violations) {
    $report += @"

### ``$($violation.File)``
- **违规数量**: $($violation.Count)
- **完整路径**: ``$($violation.FullPath)``

"@
}

$report += @"

## 修复建议

1. **替换注解**:
   - 将 ``@Autowired`` 替换为 ``@Resource``
   - 更新import: ``import jakarta.annotation.Resource;``

2. **注意事项**:
   - 检查构造函数注入是否需要保留
   - 测试代码可能需要特殊处理
   - 配置类中的注入需要仔细检查

## 自动修复命令

```powershell
# 替换@Autowired为@Resource
Get-ChildItem -Path "$microservicesPath" -Recurse -Filter "*.java" | 
ForEach-Object {
    (Get-Content `$_.FullName) -replace '@Autowired', '@Resource' |
    Set-Content `$_.FullName
}

# 更新import语句
Get-ChildItem -Path "$microservicesPath" -Recurse -Filter "*.java" | 
ForEach-Object {
    (Get-Content `$_.FullName) -replace 'org.springframework.beans.factory.annotation.Autowired', 'jakarta.annotation.Resource' |
    Set-Content `$_.FullName
}

# 删除无用的import
Get-ChildItem -Path "$microservicesPath" -Recurse -Filter "*.java" | 
ForEach-Object {
    `$content = Get-Content `$_.FullName
    `$newContent = `$content | Where-Object { `$_ -notmatch 'import org.springframework.beans.factory.annotation.Autowired;' }
    `$newContent | Set-Content `$_.FullName
}
```

---
**报告生成**: IOE-DREAM 合规性检查系统
"@

# 保存报告
$report | Out-File -FilePath $outputFile -Encoding UTF8

Write-Host "`n✅ 扫描完成！" -ForegroundColor Green
Write-Host "📊 统计结果:" -ForegroundColor Cyan
Write-Host "   - 违规文件数: $($violations.Count)" -ForegroundColor $(if ($violations.Count -gt 0) { "Red" } else { "Green" })
Write-Host "   - 违规注解总数: $violationCount" -ForegroundColor $(if ($violationCount -gt 0) { "Red" } else { "Green" })
Write-Host "📄 报告已保存: $outputFile" -ForegroundColor Cyan

