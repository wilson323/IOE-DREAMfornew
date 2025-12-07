# 构建前检查脚本
# 确保 microservices-common 已正确构建，防止依赖解析问题
# 在构建任何服务前自动执行此检查

$ErrorActionPreference = "Stop"
$projectRoot = "D:\IOE-DREAM"

function Test-CommonModuleBuilt {
    $jarPath = "$env:USERPROFILE\.m2\repository\net\lab1024\sa\microservices-common\1.0.0\microservices-common-1.0.0.jar"

    if (-not (Test-Path $jarPath)) {
        return $false
    }

    # 检查JAR文件是否包含关键类
    $jarContent = & jar -tf $jarPath 2>&1
    $requiredClass = "net/lab1024/sa/common/organization/entity/DeviceEntity.class"

    return $jarContent -match [regex]::Escape($requiredClass)
}

function Build-CommonModule {
    Write-Host "  [自动修复] 正在构建 microservices-common..." -ForegroundColor Yellow
    Set-Location $projectRoot

    $result = & mvn clean install -pl microservices/microservices-common -am -DskipTests 2>&1

    if ($LASTEXITCODE -ne 0) {
        Write-Host "  ❌ microservices-common 构建失败！" -ForegroundColor Red
        Write-Host $result
        return $false
    }

    return $true
}

# 主检查逻辑
Write-Host "检查 microservices-common 构建状态..." -ForegroundColor Cyan

if (-not (Test-CommonModuleBuilt)) {
    Write-Host "  ⚠️ microservices-common 未构建或已过期" -ForegroundColor Yellow

    $response = Read-Host "  是否自动构建? (Y/N)"
    if ($response -eq "Y" -or $response -eq "y") {
        if (Build-CommonModule) {
            Write-Host "  ✓ microservices-common 构建成功" -ForegroundColor Green
        } else {
            Write-Host "  ❌ 构建失败，请手动执行: mvn clean install -pl microservices/microservices-common -am" -ForegroundColor Red
            exit 1
        }
    } else {
        Write-Host "  ⚠️ 请先构建 microservices-common: mvn clean install -pl microservices/microservices-common -am" -ForegroundColor Yellow
        exit 1
    }
} else {
    Write-Host "  ✓ microservices-common 已正确构建" -ForegroundColor Green
}

Write-Host "检查通过，可以继续构建" -ForegroundColor Green
