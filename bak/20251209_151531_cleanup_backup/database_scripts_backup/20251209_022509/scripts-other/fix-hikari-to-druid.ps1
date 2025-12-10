# 批量修复HikariCP连接池配置脚本
# 严格遵循CLAUDE.md规范：统一使用Druid连接池

$ErrorActionPreference = "Stop"

# 扫描使用HikariCP的yml文件
$files = Get-ChildItem -Path "D:\IOE-DREAM\microservices" -Filter "application*.yml" -Recurse |
    Where-Object { $_ | Select-String -Pattern "hikari" -Quiet }

Write-Host "===== HikariCP to Druid Fix =====" -ForegroundColor Cyan
Write-Host "Found $($files.Count) files with HikariCP config`n" -ForegroundColor Yellow

$fixedCount = 0
$failedCount = 0

foreach ($file in $files) {
    if ($file.Name -match '\.backup$') {
        Write-Host "Skipping backup file: $($file.Name)" -ForegroundColor DarkGray
        continue
    }

    try {
        Write-Host "Processing: $($file.FullName)" -ForegroundColor Gray

        $content = Get-Content $file.FullName -Raw -Encoding UTF8

        # 检查是否包含hikari配置
        if ($content -notmatch 'hikari[:|\s]') {
            Write-Host "  No HikariCP config found" -ForegroundColor DarkGray
            continue
        }

        # 替换type配置
        $content = $content -replace 'type:\s*com\.zaxxer\.hikari\.HikariDataSource', 'type: com.alibaba.druid.pool.DruidDataSource'

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
      pool-prepared-statements: false
"@

        # 移除hikari配置块
        $content = $content -replace '(?ms)    hikari:.*?(?=\n  [a-z]|\n\n|\z)', $druidConfig

        Set-Content -Path $file.FullName -Value $content -Encoding UTF8 -NoNewline
        Write-Host "  Fixed - Replaced HikariCP with Druid" -ForegroundColor Green
        $fixedCount++

    } catch {
        Write-Host "  Failed: $_" -ForegroundColor Red
        $failedCount++
    }
}

Write-Host "`n===== Fix Complete =====" -ForegroundColor Cyan
Write-Host "Fixed: $fixedCount files" -ForegroundColor Green
Write-Host "Failed: $failedCount files" -ForegroundColor Red
Write-Host "`nNote: Please verify pom.xml dependencies include druid-spring-boot-starter" -ForegroundColor Yellow

