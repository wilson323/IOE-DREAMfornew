# ============================================================
# IOE-DREAM 架构合规性全面修复脚本
#
# 功能：全面搜索并修复所有架构违规项
# - @Repository违规 → @Mapper
# - @Autowired违规 → @Resource
# - javax包名违规 → jakarta包名
# - HikariCP配置 → Druid配置
#
# @Author: IOE-DREAM Team
# @Date: 2025-01-30
# ============================================================

$ErrorActionPreference = "Stop"

Write-Host "===== IOE-DREAM 架构合规性全面修复 =====" -ForegroundColor Cyan
Write-Host ""

# 统计变量
$stats = @{
    RepositoryFixed = 0
    AutowiredFixed = 0
    JakartaFixed = 0
    HikariFixed = 0
    TotalFiles = 0
}

# ==================== 1. 修复@Repository违规 ====================
Write-Host "[1/4] 检查@Repository违规..." -ForegroundColor Yellow

$repositoryFiles = Get-ChildItem -Path "microservices" -Recurse -Filter "*.java" |
    Select-String -Pattern '@Repository' |
    Where-Object {
        $_.Line -notmatch '禁止|禁止使用|禁止@Repository|//.*@Repository|/\*.*@Repository' -and
        $_.Line -match '^\s*@Repository'
    } |
    Select-Object -Unique Path

foreach ($file in $repositoryFiles) {
    $filePath = $file.Path
    Write-Host "  修复: $filePath" -ForegroundColor Gray

    $content = Get-Content $filePath -Raw
    $originalContent = $content

    # 替换@Repository为@Mapper
    $content = $content -replace '@Repository', '@Mapper'

    # 更新import语句
    $content = $content -replace 'import org\.springframework\.stereotype\.Repository;', 'import org.apache.ibatis.annotations.Mapper;'

    if ($content -ne $originalContent) {
        Set-Content -Path $filePath -Value $content -NoNewline
        $stats.RepositoryFixed++
        Write-Host "    [OK] 已修复" -ForegroundColor Green
    }
}

Write-Host "  [完成] @Repository修复: $($stats.RepositoryFixed) 个文件" -ForegroundColor Green
Write-Host ""

# ==================== 2. 修复@Autowired违规 ====================
Write-Host "[2/4] 检查@Autowired违规..." -ForegroundColor Yellow

$autowiredFiles = Get-ChildItem -Path "microservices" -Recurse -Filter "*.java" |
    Select-String -Pattern '@Autowired' |
    Where-Object {
        $_.Line -notmatch '禁止|禁止使用|禁止@Autowired|//.*@Autowired|/\*.*@Autowired' -and
        $_.Line -match '^\s*@Autowired'
    } |
    Select-Object -Unique Path

foreach ($file in $autowiredFiles) {
    $filePath = $file.Path
    Write-Host "  修复: $filePath" -ForegroundColor Gray

    $content = Get-Content $filePath -Raw
    $originalContent = $content

    # 替换@Autowired为@Resource
    $content = $content -replace '@Autowired(\([^)]*\))?', '@Resource'

    # 更新import语句
    $content = $content -replace 'import org\.springframework\.beans\.factory\.annotation\.Autowired;', 'import jakarta.annotation.Resource;'

    # 如果文件中已经有jakarta.annotation.Resource，确保不重复
    if ($content -match 'jakarta\.annotation\.Resource' -and $content -match 'org\.springframework\.beans\.factory\.annotation\.Autowired') {
        $content = $content -replace 'import org\.springframework\.beans\.factory\.annotation\.Autowired;', ''
    }

    if ($content -ne $originalContent) {
        Set-Content -Path $filePath -Value $content -NoNewline
        $stats.AutowiredFixed++
        Write-Host "    [OK] 已修复" -ForegroundColor Green
    }
}

Write-Host "  [完成] @Autowired修复: $($stats.AutowiredFixed) 个文件" -ForegroundColor Green
Write-Host ""

# ==================== 3. 修复javax包名违规 ====================
Write-Host "[3/4] 检查javax包名违规..." -ForegroundColor Yellow

# 定义需要替换的包名映射
$jakartaMappings = @{
    'javax.annotation' = 'jakarta.annotation'
    'javax.validation' = 'jakarta.validation'
    'javax.persistence' = 'jakarta.persistence'
    'javax.servlet' = 'jakarta.servlet'
    'javax.transaction' = 'jakarta.transaction'
    'javax.inject' = 'jakarta.inject'
}

$javaxFiles = Get-ChildItem -Path "microservices" -Recurse -Filter "*.java" |
    Select-String -Pattern '^import\s+javax\.(annotation|validation|persistence|servlet|transaction|inject)' |
    Select-Object -Unique Path

foreach ($file in $javaxFiles) {
    $filePath = $file.Path
    Write-Host "  修复: $filePath" -ForegroundColor Gray

    $content = Get-Content $filePath -Raw
    $originalContent = $content

    # 替换所有javax包名为jakarta
    foreach ($mapping in $jakartaMappings.GetEnumerator()) {
        $content = $content -replace "import $($mapping.Key)\.", "import $($mapping.Value)."
        $content = $content -replace "$($mapping.Key)\.", "$($mapping.Value)."
    }

    if ($content -ne $originalContent) {
        Set-Content -Path $filePath -Value $content -NoNewline
        $stats.JakartaFixed++
        Write-Host "    [OK] 已修复" -ForegroundColor Green
    }
}

Write-Host "  [完成] javax包名修复: $($stats.JakartaFixed) 个文件" -ForegroundColor Green
Write-Host ""

# ==================== 4. 修复HikariCP配置 ====================
Write-Host "[4/4] 检查HikariCP配置违规..." -ForegroundColor Yellow

$hikariFiles = Get-ChildItem -Path "microservices" -Recurse -Filter "application*.yml" |
    Select-String -Pattern 'hikari:|HikariDataSource|type:\s*.*hikari' |
    Where-Object {
        $_.Line -notmatch '禁止|禁止使用|#.*hikari|LOG_LEVEL_HIKARI'
    } |
    Select-Object -Unique Path

foreach ($file in $hikariFiles) {
    $filePath = $file.Path
    Write-Host "  修复: $filePath" -ForegroundColor Gray

    $content = Get-Content $filePath -Raw
    $originalContent = $content

    # 替换HikariDataSource为DruidDataSource
    $content = $content -replace 'type:\s*com\.zaxxer\.hikari\.HikariDataSource', 'type: com.alibaba.druid.pool.DruidDataSource'
    $content = $content -replace 'type:\s*.*[Hh]ikari.*', 'type: com.alibaba.druid.pool.DruidDataSource'

    # 替换hikari配置块为druid配置块
    $druidConfig = @"
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20
      max-wait: 60000
      validation-query: SELECT 1
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false
"@

    # 移除hikari配置块
    $content = $content -replace '(?ms)\s+hikari:.*?(?=\n\s+[a-z]|\n\n|\z)', $druidConfig

    if ($content -ne $originalContent) {
        Set-Content -Path $filePath -Value $content -NoNewline
        $stats.HikariFixed++
        Write-Host "    [OK] 已修复" -ForegroundColor Green
    }
}

Write-Host "  [完成] HikariCP修复: $($stats.HikariFixed) 个文件" -ForegroundColor Green
Write-Host ""

# ==================== 修复总结 ====================
Write-Host "===== 修复完成 =====" -ForegroundColor Cyan
Write-Host "  @Repository修复: $($stats.RepositoryFixed) 个文件" -ForegroundColor Green
Write-Host "  @Autowired修复: $($stats.AutowiredFixed) 个文件" -ForegroundColor Green
Write-Host "  javax包名修复: $($stats.JakartaFixed) 个文件" -ForegroundColor Green
Write-Host "  HikariCP修复: $($stats.HikariFixed) 个文件" -ForegroundColor Green
Write-Host ""
Write-Host "总计修复: $($stats.RepositoryFixed + $stats.AutowiredFixed + $stats.JakartaFixed + $stats.HikariFixed) 个文件" -ForegroundColor Cyan
Write-Host ""
