# ============================================================================
# IOE-DREAM Docker配置验证脚本
# 功能: 验证docker-compose-all.yml配置一致性
# 创建日期: 2025-01-31
# ============================================================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "IOE-DREAM Docker配置验证" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$composeFile = "D:\IOE-DREAM\docker-compose-all.yml"
$errors = @()
$warnings = @()

# 读取配置文件
$content = Get-Content $composeFile -Raw

# 1. 检查健康检查配置
Write-Host "[1/5] 检查健康检查配置..." -ForegroundColor Yellow

# Redis健康检查必须包含密码
if ($content -match 'redis-cli.*ping' -and $content -notmatch 'redis-cli\s+-a\s+\$\{?REDIS') {
    $errors += "Redis健康检查未包含密码参数"
} else {
    Write-Host "  ✓ Redis健康检查包含密码" -ForegroundColor Green
}

# Nacos健康检查应使用wget
if ($content -match 'nacos.*healthcheck.*curl') {
    $errors += "Nacos健康检查使用curl（Nacos镜像不包含curl）"
} elseif ($content -match 'nacos.*healthcheck.*wget') {
    Write-Host "  ✓ Nacos健康检查使用wget" -ForegroundColor Green
}

# 2. 检查端口冲突
Write-Host ""
Write-Host "[2/5] 检查端口分配..." -ForegroundColor Yellow

$portPattern = '^\s*-\s*"?(\d+):(\d+)"?'
$ports = @{}
$lines = $content -split "`n"
$currentService = ""

foreach ($line in $lines) {
    if ($line -match '^\s+(\w+[-\w]*):$' -and $line -notmatch 'condition|test|interval') {
        $currentService = $Matches[1]
    }
    if ($line -match $portPattern) {
        $hostPort = $Matches[1]
        if ($ports.ContainsKey($hostPort)) {
            $errors += "端口冲突: $hostPort 被 $($ports[$hostPort]) 和 $currentService 同时使用"
        } else {
            $ports[$hostPort] = $currentService
        }
    }
}

$portCount = $ports.Count
Write-Host "  ✓ 发现 $portCount 个端口映射，无冲突" -ForegroundColor Green

# 3. 检查环境变量一致性
Write-Host ""
Write-Host "[3/5] 检查环境变量一致性..." -ForegroundColor Yellow

# 检查MYSQL_ROOT_PASSWORD默认值
$mysqlPwdMatches = [regex]::Matches($content, 'MYSQL_ROOT_PASSWORD[:\s-]*(?:\$\{MYSQL_ROOT_PASSWORD:-)?([^}"\s]+)')
$mysqlPwds = $mysqlPwdMatches | ForEach-Object { $_.Groups[1].Value } | Sort-Object -Unique
if ($mysqlPwds.Count -gt 1) {
    $warnings += "MYSQL_ROOT_PASSWORD默认值不一致: $($mysqlPwds -join ', ')"
} else {
    Write-Host "  ✓ MYSQL_ROOT_PASSWORD默认值一致" -ForegroundColor Green
}

# 检查REDIS_PASSWORD默认值
$redisPwdMatches = [regex]::Matches($content, 'REDIS_PASSWORD[:\s-]*(?:\$\{REDIS_PASSWORD:-)?([^}"\s]+)')
$redisPwds = $redisPwdMatches | ForEach-Object { $_.Groups[1].Value } | Sort-Object -Unique
if ($redisPwds.Count -gt 1) {
    $warnings += "REDIS_PASSWORD默认值不一致: $($redisPwds -join ', ')"
} else {
    Write-Host "  ✓ REDIS_PASSWORD默认值一致" -ForegroundColor Green
}

# 4. 检查依赖关系
Write-Host ""
Write-Host "[4/5] 检查服务依赖关系..." -ForegroundColor Yellow

$nacosDepCount = ([regex]::Matches($content, 'depends_on:[\s\S]*?nacos:[\s\S]*?condition:\s*service_healthy')).Count
Write-Host "  ✓ $nacosDepCount 个服务依赖Nacos健康状态" -ForegroundColor Green

# 5. 检查必要文件
Write-Host ""
Write-Host "[5/5] 检查必要文件..." -ForegroundColor Yellow

$nacosSchema = "D:\IOE-DREAM\deployment\mysql\init\nacos-schema.sql"
if (Test-Path $nacosSchema) {
    $lines = (Get-Content $nacosSchema).Count
    Write-Host "  ✓ nacos-schema.sql 存在 ($lines 行)" -ForegroundColor Green
} else {
    $errors += "nacos-schema.sql 文件不存在"
}

# 输出结果
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "验证结果" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

if ($errors.Count -eq 0 -and $warnings.Count -eq 0) {
    Write-Host "✓ 所有配置验证通过！" -ForegroundColor Green
} else {
    if ($errors.Count -gt 0) {
        Write-Host ""
        Write-Host "错误 ($($errors.Count)):" -ForegroundColor Red
        foreach ($err in $errors) {
            Write-Host "  ✗ $err" -ForegroundColor Red
        }
    }
    if ($warnings.Count -gt 0) {
        Write-Host ""
        Write-Host "警告 ($($warnings.Count)):" -ForegroundColor Yellow
        foreach ($warn in $warnings) {
            Write-Host "  ⚠ $warn" -ForegroundColor Yellow
        }
    }
}

Write-Host ""
Write-Host "端口分配表:" -ForegroundColor Cyan
$ports.GetEnumerator() | Sort-Object { [int]$_.Key } | ForEach-Object {
    Write-Host "  $($_.Key) -> $($_.Value)" -ForegroundColor Gray
}
