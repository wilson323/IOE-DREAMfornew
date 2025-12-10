# 全局配置一致性验证脚本
# 用途：检查所有微服务的配置参数是否一致

Write-Host "=== 全局配置一致性验证 ===" -ForegroundColor Cyan
Write-Host ""

$projectRoot = Get-Location
Set-Location $projectRoot

$errors = @()
$warnings = @()
$successCount = 0

# 1. 定义标准配置值
$standardConfig = @{
    NACOS_GROUP = "IOE-DREAM"
    NACOS_USERNAME = "nacos"
    NACOS_PASSWORD = "nacos"
    REDIS_PASSWORD = "redis123"
    REDIS_DATABASE = "0"
    REDIS_PORT = "6379"
    MYSQL_DATABASE = "ioedream"
    MYSQL_USERNAME = "root"
    MYSQL_PASSWORD = "root1234"
    MYSQL_PORT = "3306"
}

# 2. 定义服务端口映射
$servicePorts = @{
    "ioedream-gateway-service" = "8080"
    "ioedream-device-comm-service" = "8087"
    "ioedream-common-service" = "8088"
    "ioedream-oa-service" = "8089"
    "ioedream-access-service" = "8090"
    "ioedream-attendance-service" = "8091"
    "ioedream-video-service" = "8092"
    "ioedream-consume-service" = "8094"
    "ioedream-visitor-service" = "8095"
}

# 3. 检查所有微服务的application.yml
Write-Host "1. 检查微服务配置文件..." -ForegroundColor Yellow

$services = @(
    "ioedream-gateway-service",
    "ioedream-device-comm-service",
    "ioedream-common-service",
    "ioedream-oa-service",
    "ioedream-access-service",
    "ioedream-attendance-service",
    "ioedream-video-service",
    "ioedream-consume-service",
    "ioedream-visitor-service"
)

foreach ($service in $services) {
    $configFile = "microservices\$service\src\main\resources\application.yml"
    
    if (-not (Test-Path $configFile)) {
        $errors += "$service: 配置文件不存在 - $configFile"
        Write-Host "  ✗ $service : 配置文件不存在" -ForegroundColor Red
        continue
    }
    
    Write-Host "  ✓ 检查 $service..." -ForegroundColor Gray
    
    $content = Get-Content $configFile -Raw
    
    # 检查Nacos配置
    if ($content -match "group:\s*\$\{NACOS_GROUP:([^}]+)\}") {
        $group = $matches[1]
        if ($group -ne $standardConfig.NACOS_GROUP) {
            $errors += "$service: NACOS_GROUP不一致 - 期望: $($standardConfig.NACOS_GROUP), 实际: $group"
        } else {
            $successCount++
        }
    }
    
    if ($content -match "username:\s*\$\{NACOS_USERNAME:([^}]+)\}") {
        $username = $matches[1]
        if ($username -ne $standardConfig.NACOS_USERNAME) {
            $errors += "$service: NACOS_USERNAME不一致 - 期望: $($standardConfig.NACOS_USERNAME), 实际: $username"
        } else {
            $successCount++
        }
    }
    
    if ($content -match "password:\s*\$\{NACOS_PASSWORD:([^}]+)\}") {
        $password = $matches[1]
        if ($password -ne $standardConfig.NACOS_PASSWORD) {
            $errors += "$service: NACOS_PASSWORD不一致 - 期望: $($standardConfig.NACOS_PASSWORD), 实际: $password"
        } else {
            $successCount++
        }
    }
    
    # 检查服务端口
    if ($content -match "port:\s*(\d+)") {
        $port = $matches[1]
        $expectedPort = $servicePorts[$service]
        if ($port -ne $expectedPort) {
            $errors += "$service: 服务端口不一致 - 期望: $expectedPort, 实际: $port"
        } else {
            $successCount++
        }
    }
}

# 4. 检查docker-compose-all.yml
Write-Host "`n2. 检查docker-compose-all.yml..." -ForegroundColor Yellow

if (Test-Path "docker-compose-all.yml") {
    $dockerCompose = Get-Content "docker-compose-all.yml" -Raw
    
    # 检查Redis密码
    if ($dockerCompose -match "REDIS_PASSWORD:\s*\$\{REDIS_PASSWORD:-([^}]+)\}") {
        $redisPassword = $matches[1]
        if ($redisPassword -ne $standardConfig.REDIS_PASSWORD) {
            $errors += "docker-compose: REDIS_PASSWORD不一致 - 期望: $($standardConfig.REDIS_PASSWORD), 实际: $redisPassword"
        } else {
            $successCount++
        }
    }
    
    # 检查MySQL密码
    if ($dockerCompose -match "MYSQL_ROOT_PASSWORD:\s*\$\{MYSQL_ROOT_PASSWORD:-([^}]+)\}") {
        $mysqlPassword = $matches[1]
        if ($mysqlPassword -ne $standardConfig.MYSQL_PASSWORD) {
            $errors += "docker-compose: MYSQL_ROOT_PASSWORD不一致 - 期望: $($standardConfig.MYSQL_PASSWORD), 实际: $mysqlPassword"
        } else {
            $successCount++
        }
    }
    
    # 检查Nacos配置
    if ($dockerCompose -match "NACOS_GROUP:\s*([^\s]+)") {
        $nacosGroup = $matches[1]
        if ($nacosGroup -ne $standardConfig.NACOS_GROUP) {
            $errors += "docker-compose: NACOS_GROUP不一致 - 期望: $($standardConfig.NACOS_GROUP), 实际: $nacosGroup"
        } else {
            $successCount++
        }
    }
    
    # 检查服务端口映射
    foreach ($service in $services) {
        $serviceName = $service -replace "ioedream-", "" -replace "-service", ""
        $expectedPort = $servicePorts[$service]
        
        if ($dockerCompose -match "$serviceName:.*ports:.*- `"(\d+):(\d+)`"") {
            $hostPort = $matches[1]
            $containerPort = $matches[2]
            
            if ($hostPort -ne $expectedPort -or $containerPort -ne $expectedPort) {
                $errors += "docker-compose: $service 端口映射不一致 - 期望: $expectedPort:$expectedPort, 实际: $hostPort:$containerPort"
            } else {
                $successCount++
            }
        }
    }
    
    Write-Host "  ✓ docker-compose-all.yml 检查完成" -ForegroundColor Green
} else {
    $errors += "docker-compose-all.yml 文件不存在"
    Write-Host "  ✗ docker-compose-all.yml 文件不存在" -ForegroundColor Red
}

# 5. 检查环境变量文件
Write-Host "`n3. 检查环境变量文件..." -ForegroundColor Yellow

$envFiles = @(".env", ".env.docker", ".env.template")
foreach ($envFile in $envFiles) {
    if (Test-Path $envFile) {
        Write-Host "  ✓ 检查 $envFile..." -ForegroundColor Gray
        $content = Get-Content $envFile -Raw
        
        # 检查关键环境变量
        foreach ($key in $standardConfig.Keys) {
            if ($content -match "$key\s*=\s*([^\s]+)") {
                $value = $matches[1]
                if ($value -ne $standardConfig[$key]) {
                    $warnings += "$envFile: $key 不一致 - 期望: $($standardConfig[$key]), 实际: $value"
                }
            }
        }
    }
}

# 6. 生成报告
Write-Host "`n=== 验证结果 ===" -ForegroundColor Cyan
Write-Host "成功检查项: $successCount" -ForegroundColor Green

if ($errors.Count -gt 0) {
    Write-Host "`n✗ 发现 $($errors.Count) 个错误:" -ForegroundColor Red
    foreach ($error in $errors) {
        Write-Host "  - $error" -ForegroundColor Red
    }
}

if ($warnings.Count -gt 0) {
    Write-Host "`n⚠ 发现 $($warnings.Count) 个警告:" -ForegroundColor Yellow
    foreach ($warning in $warnings) {
        Write-Host "  - $warning" -ForegroundColor Yellow
    }
}

if ($errors.Count -eq 0 -and $warnings.Count -eq 0) {
    Write-Host "`n✓ 所有配置参数一致！" -ForegroundColor Green
    exit 0
} else {
    Write-Host "`n请修复上述问题后重新验证" -ForegroundColor Yellow
    Write-Host "详细标准: documentation/technical/GLOBAL_CONFIGURATION_STANDARDS.md" -ForegroundColor Cyan
    exit 1
}
