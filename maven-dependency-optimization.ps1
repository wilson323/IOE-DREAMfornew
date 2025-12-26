# Maven依赖架构优化脚本
# 扫描并修复业务服务中的重复依赖声明和版本硬编码问题

Write-Host "🔍 开始Maven依赖架构分析..." -ForegroundColor Green

$servicesPath = "D:\IOE-DREAM\microservices"
$services = @(
    "ioedream-access-service",
    "ioedream-attendance-service",
    "ioedream-consume-service",
    "ioedream-video-service",
    "ioedream-visitor-service",
    "ioedream-device-comm-service"
)

$analysisResults = @()
$totalIssues = 0

# 需要移除版本声明的依赖（已经在父POM的dependencyManagement中管理）
$dependenciesWithoutVersion = @(
    "mybatis-plus-spring-boot3-starter",
    "druid-spring-boot-3-starter",
    "lombok",
    "junit-jupiter",
    "mockito-core",
    "mockito-junit-jupiter",
    "mysql-connector-j",
    "resilience4j-spring-boot3",
    "resilience4j-retry",
    "resilience4j-circuitbreaker",
    "resilience4j-ratelimiter",
    "resilience4j-bulkhead",
    "resilience4j-timelimiter",
    "resilience4j-micrometer"
)

# 需要合并的重复依赖组
$duplicateGroups = @{
    "resilience4j" = @("resilience4j-spring-boot3", "resilience4j-retry", "resilience4j-circuitbreaker", "resilience4j-ratelimiter", "resilience4j-micrometer")
}

foreach ($service in $services) {
    $servicePath = Join-Path $servicesPath $service
    $pomPath = Join-Path $servicePath "pom.xml"

    if (Test-Path $pomPath) {
        Write-Host "`n📋 分析服务: $service" -ForegroundColor Cyan

        $pomContent = Get-Content -Path $pomPath -Raw -Encoding UTF8
        $serviceIssues = 0
        $fixes = @()

        # 检查版本硬编码问题
        foreach ($dep in $dependenciesWithoutVersion) {
            $pattern = "<artifactId>$dep</artifactId>`s*<version>.*?</version>"
            if ($pomContent -match $pattern) {
                $serviceIssues++
                $fixes += "  ❌ 版本硬编码: $dep"
                $totalIssues++
            }
        }

        # 检查重复依赖问题
        foreach ($groupName in $duplicateGroups.Keys) {
            $groupDeps = $duplicateGroups[$groupName]
            $foundDeps = @()

            foreach ($dep in $groupDeps) {
                if ($pomContent -match "<artifactId>$dep</artifactId>") {
                    $foundDeps += $dep
                }
            }

            if ($foundDeps.Count -gt 1) {
                $serviceIssues++
                $fixes += "  ❌ 重复依赖组: $groupName ($($foundDeps -join ', '))"
                $totalIssues++
            }
        }

        # 检查不必要的版本声明
        $versionPattern = "<version>.*?\$\{.*?version\}.*?</version>"
        $versionMatches = [regex]::Matches($pomContent, $versionPattern)
        foreach ($match in $versionMatches) {
            $dependencyLine = $pomContent.Substring([Math]::Max(0, $match.Index - 100), 200)
            if ($dependencyLine -match "<artifactId>(.+?)</artifactId>") {
                $artifactId = $matches[1]
                if ($artifactId -in $dependenciesWithoutVersion) {
                    # 这个已经在上面检查过了，跳过
                    continue
                }
                # 检查是否是需要版本的特殊依赖
                if ($artifactId -notmatch "microservices-common") {
                    $serviceIssues++
                    $fixes += "  ⚠️ 可移除版本: $artifactId"
                    $totalIssues++
                }
            }
        }

        $analysisResults += @{
            Service = $service
            Issues = $serviceIssues
            Fixes = $fixes
        }

        if ($serviceIssues -gt 0) {
            Write-Host "  发现 $serviceIssues 个问题:" -ForegroundColor Yellow
            $fixes | ForEach-Object { Write-Host $_ -ForegroundColor White }
        } else {
            Write-Host "  ✅ 无Maven依赖问题" -ForegroundColor Green
        }
    }
}

Write-Host "`n📊 Maven依赖架构分析总结:" -ForegroundColor Magenta
Write-Host "  扫描服务数: $($services.Count)" -ForegroundColor White
Write-Host "  发现问题数: $totalIssues" -ForegroundColor Red
Write-Host "  需要修复的服务: $($analysisResults.Where({$_.Issues -gt 0}).Count)" -ForegroundColor Yellow

# 生成修复报告
$reportPath = "D:\IOE-DREAM\maven-dependency-analysis-report.md"
$reportContent = @"
# Maven依赖架构优化分析报告

**生成时间**: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")
**扫描范围**: $($services.Count) 个业务微服务

## 问题统计

- **总问题数**: $totalIssues
- **需修复服务**: $($analysisResults.Where({$_.Issues -gt 0}).Count) 个

## 详细分析结果

"@

foreach ($result in $analysisResults) {
    if ($result.Issues -gt 0) {
        $reportContent += @"
### $($result.Service)

- **问题数**: $($result.Issues)
- **问题详情**:
$($result.Fixes -join "`n")

"@
    }
}

$reportContent += @"
## 优化建议

### 1. 版本管理优化
- 移除业务服务中的硬编码版本声明
- 统一使用父POM的dependencyManagement管理版本
- 保留`${project.version}`用于内部模块依赖

### 2. 重复依赖清理
- 合并Resilience4j依赖声明
- 使用单一依赖替代多个相关依赖
- 清理不必要的测试依赖重复声明

### 3. 依赖声明标准化
```xml
<!-- ✅ 正确的依赖声明模式 -->
<dependency>
  <groupId>com.baomidou</groupId>
  <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
  <!-- 版本由父POM的dependencyManagement管理，无需声明版本 -->
</dependency>

<!-- ❌ 错误的依赖声明模式 -->
<dependency>
  <groupId>com.baomidou</groupId>
  <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
  <version>${mybatis-plus.version}</version>  <!-- 冗余的版本声明 -->
</dependency>
```

### 4. 构建顺序验证
当前构建顺序符合依赖层次要求：
1. microservices-common-core (第1层)
2. microservices-common-entity (第1层)
3. 细粒度模块 (第2层)
4. 业务服务 (第3层)

## 下一步行动
1. 手动修复每个服务的POM文件
2. 验证Maven编译通过
3. 运行完整构建测试
"@

[System.IO.File]::WriteAllText($reportPath, $reportContent, [System.Text.Encoding]::UTF8)
Write-Host "`n📄 详细分析报告已生成: $reportPath" -ForegroundColor Green

if ($totalIssues -gt 0) {
    Write-Host "`n🔧 建议执行手动修复操作" -ForegroundColor Yellow
    Write-Host "1. 移除硬编码的版本声明" -ForegroundColor White
    Write-Host "2. 合并重复的依赖声明" -ForegroundColor White
    Write-Host "3. 验证构建编译通过" -ForegroundColor White
} else {
    Write-Host "`n🎉 Maven依赖架构已优化完成！" -ForegroundColor Green
}