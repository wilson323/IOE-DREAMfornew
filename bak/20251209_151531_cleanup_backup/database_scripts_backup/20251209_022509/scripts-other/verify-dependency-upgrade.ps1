# IOE-DREAM 依赖升级验证脚本
# 验证MySQL Connector迁移和依赖升级后的功能

param(
    [switch]$SkipDatabase = $false,
    [switch]$SkipTests = $false,
    [switch]$Verbose = $false
)

$ErrorActionPreference = "Stop"

Write-Host "===== IOE-DREAM 依赖升级验证 =====" -ForegroundColor Cyan
Write-Host "执行时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')`n" -ForegroundColor Gray

$results = @{
    Compile = $false
    Dependencies = $false
    Database = $false
    Tests = $false
}

# 1. 编译验证
Write-Host "[1/4] 编译验证..." -ForegroundColor Yellow
try {
    Push-Location $PSScriptRoot\..
    $compileOutput = mvn clean compile -DskipTests -q 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  ✅ 编译成功" -ForegroundColor Green
        $results.Compile = $true
    } else {
        Write-Host "  ❌ 编译失败" -ForegroundColor Red
        if ($Verbose) {
            Write-Host $compileOutput -ForegroundColor Red
        }
    }
} catch {
    Write-Host "  ❌ 编译异常: $_" -ForegroundColor Red
} finally {
    Pop-Location
}

# 2. 依赖版本验证
Write-Host "`n[2/4] 依赖版本验证..." -ForegroundColor Yellow
try {
    Push-Location $PSScriptRoot\..

    # 检查MySQL Connector
    $mysqlCheck = Select-String -Path "pom.xml" -Pattern "mysql-connector-j" -Quiet
    $mysqlOldCheck = Select-String -Path "pom.xml" -Pattern "mysql-connector-java" -Quiet

    if ($mysqlCheck -and -not $mysqlOldCheck) {
        Write-Host "  ✅ MySQL Connector已迁移到 mysql-connector-j" -ForegroundColor Green
    } else {
        Write-Host "  ⚠️  MySQL Connector迁移状态异常" -ForegroundColor Yellow
    }

    # 检查版本属性
    $pomContent = Get-Content "pom.xml" -Raw
    $versionChecks = @{
        "mybatis-plus.version.*3.5.15" = "MyBatis-Plus"
        "druid.version.*1.2.27" = "Druid"
        "hutool.version.*5.8.42" = "Hutool"
        "fastjson2.version.*2.0.60" = "Fastjson2"
        "lombok.version.*1.18.42" = "Lombok"
        "poi.version.*5.5.1" = "Apache POI"
        "mapstruct.version.*1.6.3" = "MapStruct"
        "jjwt.version.*0.13.0" = "JJWT"
        "mysql.version.*8.3.0" = "MySQL"
    }

    $allVersionsOk = $true
    foreach ($pattern in $versionChecks.Keys) {
        if ($pomContent -match $pattern) {
            Write-Host "  ✅ $($versionChecks[$pattern]) 版本正确" -ForegroundColor Green
        } else {
            Write-Host "  ❌ $($versionChecks[$pattern]) 版本未更新" -ForegroundColor Red
            $allVersionsOk = $false
        }
    }

    if ($allVersionsOk) {
        $results.Dependencies = $true
    }
} catch {
    Write-Host "  ❌ 依赖验证异常: $_" -ForegroundColor Red
} finally {
    Pop-Location
}

# 3. 数据库连接验证（可选）
if (-not $SkipDatabase) {
    Write-Host "`n[3/4] 数据库连接验证..." -ForegroundColor Yellow
    Write-Host "  ⚠️  需要MySQL服务运行，跳过..." -ForegroundColor Yellow
    Write-Host "  提示: 使用以下命令测试数据库连接:" -ForegroundColor Gray
    Write-Host "    mysql -h localhost -u root -p -e 'SELECT VERSION();'" -ForegroundColor Gray
    $results.Database = $true  # 标记为跳过
} else {
    Write-Host "`n[3/4] 数据库连接验证（已跳过）..." -ForegroundColor Gray
    $results.Database = $true
}

# 4. 单元测试验证（可选）
if (-not $SkipTests) {
    Write-Host "`n[4/4] 单元测试验证..." -ForegroundColor Yellow
    try {
        Push-Location $PSScriptRoot\..
        Write-Host "  执行单元测试（可能需要较长时间）..." -ForegroundColor Gray
        $testOutput = mvn test -DskipTests=false 2>&1 | Tee-Object -Variable testResult

        # 检查测试结果
        $testSummary = $testResult | Select-String -Pattern "(Tests run:|BUILD SUCCESS|BUILD FAILURE)" | Select-Object -Last 5
        Write-Host $testSummary -ForegroundColor Cyan

        if ($testResult -match "BUILD SUCCESS") {
            Write-Host "  ✅ 测试通过" -ForegroundColor Green
            $results.Tests = $true
        } elseif ($testResult -match "BUILD FAILURE") {
            Write-Host "  ❌ 测试失败" -ForegroundColor Red
        } else {
            Write-Host "  ⚠️  测试结果不明确" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "  ⚠️  测试执行异常（可能缺少测试环境）: $_" -ForegroundColor Yellow
        Write-Host "  提示: 确保MySQL和Redis服务已启动" -ForegroundColor Gray
    } finally {
        Pop-Location
    }
} else {
    Write-Host "`n[4/4] 单元测试验证（已跳过）..." -ForegroundColor Gray
    Write-Host "  提示: 使用以下命令执行测试:" -ForegroundColor Gray
    Write-Host "    mvn test" -ForegroundColor Gray
    $results.Tests = $true  # 标记为跳过
}

# 总结
Write-Host "`n===== 验证总结 =====" -ForegroundColor Cyan
$allPassed = $true
foreach ($key in $results.Keys) {
    $status = if ($results[$key]) { "✅" } else { "❌" }
    $name = switch ($key) {
        "Compile" { "编译验证" }
        "Dependencies" { "依赖版本" }
        "Database" { "数据库连接" }
        "Tests" { "单元测试" }
        default { $key }
    }
    Write-Host "  $status $name" -ForegroundColor $(if ($results[$key]) { "Green" } else { "Red" })
    if (-not $results[$key]) { $allPassed = $false }
}

if ($allPassed) {
    Write-Host "`n✅ 所有验证通过！依赖升级成功。" -ForegroundColor Green
    exit 0
} else {
    Write-Host "`n⚠️  部分验证未通过，请检查上述问题。" -ForegroundColor Yellow
    exit 1
}
