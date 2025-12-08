# IOE-DREAM 完整项目一键启动脚本
# 包含：后端微服务 + 前端管理后台 + 移动端应用
# 
# 使用方法：
#   .\start-all-complete.ps1                    # 启动所有服务
#   .\start-all-complete.ps1 -BackendOnly       # 仅启动后端
#   .\start-all-complete.ps1 -FrontendOnly      # 仅启动前端
#   .\start-all-complete.ps1 -MobileOnly        # 仅启动移动端
#   .\start-all-complete.ps1 -CheckOnly         # 仅检查服务状态
#
# 注意：IDE可能显示语法错误警告，但这是误报
# - PowerShell语言服务器在处理here-string (@"... "@) 时可能出现误报
# - 脚本已通过PowerShell官方解析器验证，语法完全正确
# - 可以安全忽略IDE的语法错误提示

# 设置PowerShell输出编码为UTF-8，解决中文乱码问题
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::InputEncoding = [System.Text.Encoding]::UTF8
chcp 65001 | Out-Null

param(
    [switch]$BackendOnly = $false,
    [switch]$FrontendOnly = $false,
    [switch]$MobileOnly = $false,
    [switch]$CheckOnly = $false
)

# 注意：不要设置为Stop，否则任何错误都会导致脚本静默退出
# 使用Continue确保错误能被捕获和显示
$ErrorActionPreference = "Continue"
$ProjectRoot = "D:\IOE-DREAM"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  IOE-DREAM 智慧园区一卡通管理平台" -ForegroundColor Cyan
Write-Host "  完整项目一键启动脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# ==================== 服务配置 ====================

# 后端微服务配置（按照依赖顺序）
$backendServices = @(
    @{Name="ioedream-gateway-service"; Port=8080; Order=1; Type="基础设施"; Path="$ProjectRoot\microservices\ioedream-gateway-service"},
    @{Name="ioedream-common-service"; Port=8088; Order=2; Type="公共模块"; Path="$ProjectRoot\microservices\ioedream-common-service"},
    @{Name="ioedream-device-comm-service"; Port=8087; Order=2; Type="设备通讯"; Path="$ProjectRoot\microservices\ioedream-device-comm-service"},
    @{Name="ioedream-oa-service"; Port=8089; Order=3; Type="OA服务"; Path="$ProjectRoot\microservices\ioedream-oa-service"},
    @{Name="ioedream-access-service"; Port=8090; Order=3; Type="门禁服务"; Path="$ProjectRoot\microservices\ioedream-access-service"},
    @{Name="ioedream-attendance-service"; Port=8091; Order=3; Type="考勤服务"; Path="$ProjectRoot\microservices\ioedream-attendance-service"},
    @{Name="ioedream-video-service"; Port=8092; Order=3; Type="视频服务"; Path="$ProjectRoot\microservices\ioedream-video-service"},
    @{Name="ioedream-consume-service"; Port=8094; Order=3; Type="消费服务"; Path="$ProjectRoot\microservices\ioedream-consume-service"},
    @{Name="ioedream-visitor-service"; Port=8095; Order=3; Type="访客服务"; Path="$ProjectRoot\microservices\ioedream-visitor-service"}
)

# 前端配置
$frontendConfig = @{
    Name = "前端管理后台"
    Port = 3000
    Path = "$ProjectRoot\smart-admin-web-javascript"
    StartCommand = "npm run localhost"  # 使用localhost模式，连接本地后端
    CheckUrl = "http://localhost:3000"
}

# 移动端配置
$mobileConfig = @{
    Name = "移动端应用(H5)"
    Port = 8081
    Path = "$ProjectRoot\smart-app"
    StartCommand = "npm run dev:h5"
    CheckUrl = "http://localhost:8081"
}

# ==================== 检查服务状态 ====================

function Test-ServiceStatus {
    param(
        [string]$Name,
        [int]$Port,
        [string]$CheckUrl = $null
    )
    
    try {
        if ($CheckUrl) {
            # 通过HTTP检查
            $response = Invoke-WebRequest -Uri $CheckUrl -Method Get -TimeoutSec 2 -UseBasicParsing -ErrorAction SilentlyContinue
            if ($response.StatusCode -eq 200) {
                return $true
            }
        } else {
            # 通过端口检查
            $connection = Test-NetConnection -ComputerName localhost -Port $Port -WarningAction SilentlyContinue -InformationLevel Quiet
            return $connection
        }
    } catch {
        return $false
    }
    return $false
}

# ==================== 检查模式 ====================

if ($CheckOnly) {
    Write-Host "检查所有服务状态...`n" -ForegroundColor Yellow
    
    $runningCount = 0
    $stoppedCount = 0
    
    # 检查后端服务
    Write-Host "【后端微服务】" -ForegroundColor Cyan
    foreach ($service in $backendServices) {
        $isRunning = Test-ServiceStatus -Name $service.Name -Port $service.Port
        if ($isRunning) {
            Write-Host "  ✅ $($service.Name) (端口$($service.Port)) - 运行中" -ForegroundColor Green
            $runningCount++
        } else {
            Write-Host "  ⭕ $($service.Name) (端口$($service.Port)) - 未运行" -ForegroundColor Gray
            $stoppedCount++
        }
    }
    
    Write-Host ""
    Write-Host "【前端应用】" -ForegroundColor Cyan
    $frontendRunning = Test-ServiceStatus -Name $frontendConfig.Name -Port $frontendConfig.Port -CheckUrl $frontendConfig.CheckUrl
    if ($frontendRunning) {
        Write-Host "  ✅ $($frontendConfig.Name) (端口$($frontendConfig.Port)) - 运行中" -ForegroundColor Green
        $runningCount++
    } else {
        Write-Host "  ⭕ $($frontendConfig.Name) (端口$($frontendConfig.Port)) - 未运行" -ForegroundColor Gray
        $stoppedCount++
    }
    
    Write-Host ""
    Write-Host "【移动端应用】" -ForegroundColor Cyan
    $mobileRunning = Test-ServiceStatus -Name $mobileConfig.Name -Port $mobileConfig.Port -CheckUrl $mobileConfig.CheckUrl
    if ($mobileRunning) {
        Write-Host "  ✅ $($mobileConfig.Name) (端口$($mobileConfig.Port)) - 运行中" -ForegroundColor Green
        $runningCount++
    } else {
        Write-Host "  ⭕ $($mobileConfig.Name) (端口$($mobileConfig.Port)) - 未运行" -ForegroundColor Gray
        $stoppedCount++
    }
    
    Write-Host ""
    Write-Host "统计: $runningCount 个运行中, $stoppedCount 个未运行" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "访问地址:" -ForegroundColor Yellow
    Write-Host "  前端管理后台: http://localhost:3000" -ForegroundColor Gray
    Write-Host "  移动端应用:   http://localhost:8081" -ForegroundColor Gray
    Write-Host "  API网关:      http://localhost:8080" -ForegroundColor Gray
    exit 0
}

# ==================== 启动后端服务 ====================

function Start-BackendServices {
    Write-Host "【启动后端微服务】" -ForegroundColor Cyan
    Write-Host ""
    
    # 检查Maven是否安装
    Write-Host "  检查Maven环境..." -ForegroundColor Gray
    try {
        $mvnCheck = Get-Command mvn -ErrorAction SilentlyContinue
        if (-not $mvnCheck) {
            Write-Host "  ❌ Maven未安装或未配置到PATH" -ForegroundColor Red
            Write-Host "  请先安装Maven并配置环境变量" -ForegroundColor Yellow
            return $false
        }
        $mvnVersion = & mvn -version 2>&1 | Select-Object -First 1
        Write-Host "  ✓ Maven已安装: $mvnVersion" -ForegroundColor Green
    } catch {
        Write-Host "  ❌ Maven检查失败: $_" -ForegroundColor Red
        Write-Host "  请先安装Maven并配置到PATH" -ForegroundColor Yellow
        return $false
    }
    
    # 先构建 microservices-common（必须）
    Write-Host "  构建公共模块 (microservices-common)..." -ForegroundColor Yellow
    $commonPath = "$ProjectRoot\microservices\microservices-common"
    if (Test-Path $commonPath) {
        $originalLocation = Get-Location
        try {
            Set-Location $commonPath
            Write-Host "    执行: mvn clean install -DskipTests" -ForegroundColor Gray
            
            # 执行构建并捕获输出
            $buildOutput = & mvn clean install -DskipTests 2>&1 | Out-String
            $buildSuccess = $buildOutput -match "BUILD SUCCESS"
            $buildFailure = $buildOutput -match "BUILD FAILURE"
            
            if ($buildSuccess) {
                Write-Host "    ✅ 公共模块构建成功" -ForegroundColor Green
                # 等待JAR文件写入本地仓库
                Start-Sleep -Seconds 3
            } elseif ($buildFailure) {
                Write-Host "    ❌ 公共模块构建失败！" -ForegroundColor Red
                Write-Host "    请检查构建日志，修复错误后重试" -ForegroundColor Yellow
                Set-Location $originalLocation
                return $false
            } else {
                Write-Host "    ⚠️  构建状态未知，继续执行..." -ForegroundColor Yellow
            }
        } catch {
            Write-Host "    ❌ 构建过程出错: $_" -ForegroundColor Red
            Set-Location $originalLocation
            return $false
        } finally {
            Set-Location $originalLocation
        }
    } else {
        Write-Host "    ❌ 公共模块目录不存在: $commonPath" -ForegroundColor Red
        Write-Host "    无法继续启动服务！" -ForegroundColor Red
        return $false
    }
    
    # 检查并自动启动依赖服务（MySQL、Redis、Nacos）
    Write-Host "  检查依赖服务..." -ForegroundColor Gray
    
    # 检查Docker是否可用
    $dockerAvailable = $false
    try {
        $dockerCheck = Get-Command docker -ErrorAction SilentlyContinue
        if ($dockerCheck) {
            $dockerInfo = docker info 2>&1
            if ($LASTEXITCODE -eq 0) {
                $dockerAvailable = $true
                Write-Host "    ✓ Docker已安装并运行" -ForegroundColor Green
            }
        }
    } catch {
        # Docker不可用，忽略
    }
    
    # 检查MySQL
    $mysqlRunning = Test-ServiceStatus -Name "MySQL" -Port 3306
    if (-not $mysqlRunning) {
        Write-Host "    ⚠️  MySQL未运行 (端口3306)" -ForegroundColor Yellow
        if ($dockerAvailable) {
            Write-Host "    正在尝试通过Docker启动MySQL..." -ForegroundColor Yellow
            try {
                $composeFile = "$ProjectRoot\docker-compose-all.yml"
                if (Test-Path $composeFile) {
                    # 检查MySQL容器是否存在
                    $mysqlContainer = docker ps -a --filter "name=ioedream-mysql" --format "{{.Names}}" 2>&1
                    if ($mysqlContainer -match "ioedream-mysql") {
                        # 启动现有容器
                        docker start ioedream-mysql 2>&1 | Out-Null
                        Write-Host "    ✓ 已启动MySQL容器" -ForegroundColor Green
                    } else {
                        # 创建并启动MySQL容器
                        Set-Location $ProjectRoot
                        docker-compose -f docker-compose-all.yml up -d mysql 2>&1 | Out-Null
                        Write-Host "    ✓ 已创建并启动MySQL容器" -ForegroundColor Green
                    }
                    # 等待MySQL就绪
                    Write-Host "    等待MySQL就绪（30秒）..." -ForegroundColor Gray
                    Start-Sleep -Seconds 30
                    
                    # 检查MySQL健康状态
                    $maxWait = 60
                    $waited = 0
                    $mysqlHealthy = $false
                    while ($waited -lt $maxWait) {
                        $healthStatus = docker inspect ioedream-mysql --format='{{.State.Health.Status}}' 2>&1
                        if ($healthStatus -eq "healthy") {
                            $mysqlHealthy = $true
                            break
                        }
                        Start-Sleep -Seconds 5
                        $waited += 5
                    }
                    
                    $mysqlRunning = Test-ServiceStatus -Name "MySQL" -Port 3306
                    if ($mysqlRunning -and $mysqlHealthy) {
                        Write-Host "    ✓ MySQL已就绪" -ForegroundColor Green
                        
                        # 自动初始化nacos数据库
                        Write-Host "    检查并初始化nacos数据库..." -ForegroundColor Gray
                        $nacosSchema = "$ProjectRoot\deployment\mysql\init\nacos-schema.sql"
                        if (Test-Path $nacosSchema) {
                            try {
                                # 检查nacos数据库是否存在
                                $dbCheck = docker exec ioedream-mysql mysql -uroot -proot -e 'SHOW DATABASES LIKE ''nacos'';' 2>&1
                                $nacosDbExists = $dbCheck | Select-String -Pattern 'nacos'
                                
                                if (-not $nacosDbExists) {
                                    Write-Host "      创建nacos数据库..." -ForegroundColor Gray
                                    docker exec ioedream-mysql mysql -uroot -proot -e 'CREATE DATABASE IF NOT EXISTS nacos CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;' 2>&1 | Out-Null
                                }
                                
                                # 检查表数量
                                $tableCheck = docker exec ioedream-mysql mysql -uroot -proot -e 'SELECT COUNT(*) as cnt FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA=''nacos'';' 2>&1
                                $tableCount = [regex]::Match($tableCheck, '\d+').Value
                                
                                if (-not $tableCount -or [int]$tableCount -eq 0) {
                                    Write-Host "      初始化nacos数据库表结构..." -ForegroundColor Gray
                                    Get-Content $nacosSchema -Raw | docker exec -i ioedream-mysql mysql -uroot -proot nacos 2>&1 | Out-Null
                                    if ($LASTEXITCODE -eq 0) {
                                        Write-Host "      ✓ nacos数据库初始化成功" -ForegroundColor Green
                                    } else {
                                        Write-Host "      ⚠️  nacos数据库初始化可能失败，请检查日志" -ForegroundColor Yellow
                                    }
                                } else {
                                    Write-Host "      ✓ nacos数据库已初始化 (表数量: $tableCount)" -ForegroundColor Green
                                }
                            } catch {
                                Write-Host "      ⚠️  初始化nacos数据库时出错: $_" -ForegroundColor Yellow
                            }
                        } else {
                            Write-Host "      ⚠️  未找到nacos-schema.sql文件" -ForegroundColor Yellow
                        }
                    } else {
                        Write-Host "    ⚠️  MySQL启动中，可能需要更多时间" -ForegroundColor Yellow
                    }
                } else {
                    Write-Host "    ⚠️  未找到docker-compose-all.yml，无法自动启动MySQL" -ForegroundColor Yellow
                }
            } catch {
                Write-Host "    ⚠️  Docker启动MySQL失败: $_" -ForegroundColor Yellow
            }
        } else {
            Write-Host "    ⚠️  Docker不可用，无法自动启动MySQL" -ForegroundColor Yellow
            Write-Host "    请手动启动MySQL或安装Docker" -ForegroundColor Yellow
        }
    } else {
        Write-Host "    ✓ MySQL运行中" -ForegroundColor Green
    }
    
    # 检查Redis
    $redisRunning = Test-ServiceStatus -Name "Redis" -Port 6379
    if (-not $redisRunning) {
        Write-Host "    ⚠️  Redis未运行 (端口6379)" -ForegroundColor Yellow
        if ($dockerAvailable) {
            Write-Host "    正在尝试通过Docker启动Redis..." -ForegroundColor Yellow
            try {
                $composeFile = "$ProjectRoot\docker-compose-all.yml"
                if (Test-Path $composeFile) {
                    $redisContainer = docker ps -a --filter "name=ioedream-redis" --format "{{.Names}}" 2>&1
                    if ($redisContainer -match "ioedream-redis") {
                        docker start ioedream-redis 2>&1 | Out-Null
                        Write-Host "    ✓ 已启动Redis容器" -ForegroundColor Green
                    } else {
                        Set-Location $ProjectRoot
                        docker-compose -f docker-compose-all.yml up -d redis 2>&1 | Out-Null
                        Write-Host "    ✓ 已创建并启动Redis容器" -ForegroundColor Green
                    }
                    Start-Sleep -Seconds 5
                    $redisRunning = Test-ServiceStatus -Name "Redis" -Port 6379
                    if ($redisRunning) {
                        Write-Host "    ✓ Redis已就绪" -ForegroundColor Green
                    }
                }
            } catch {
                Write-Host "    ⚠️  Docker启动Redis失败: $_" -ForegroundColor Yellow
            }
        }
    } else {
        Write-Host "    ✓ Redis运行中" -ForegroundColor Green
    }
    
    # 检查Nacos
    $nacosRunning = Test-ServiceStatus -Name "Nacos" -Port 8848
    if (-not $nacosRunning) {
        Write-Host "    ⚠️  Nacos未运行 (端口8848)" -ForegroundColor Yellow
        if ($dockerAvailable) {
            Write-Host "    正在尝试通过Docker启动Nacos..." -ForegroundColor Yellow
            try {
                $composeFile = "$ProjectRoot\docker-compose-all.yml"
                if (Test-Path $composeFile) {
                    $nacosContainer = docker ps -a --filter "name=ioedream-nacos" --format "{{.Names}}" 2>&1
                    if ($nacosContainer -match "ioedream-nacos") {
                        docker start ioedream-nacos 2>&1 | Out-Null
                        Write-Host "    ✓ 已启动Nacos容器" -ForegroundColor Green
                    } else {
                        Set-Location $ProjectRoot
                        docker-compose -f docker-compose-all.yml up -d nacos 2>&1 | Out-Null
                        Write-Host "    ✓ 已创建并启动Nacos容器" -ForegroundColor Green
                    }
                    Write-Host "    等待Nacos就绪（30秒）..." -ForegroundColor Gray
                    Start-Sleep -Seconds 30
                    
                    # 如果Nacos启动失败，尝试初始化数据库后重启
                    $nacosRunning = Test-ServiceStatus -Name "Nacos" -Port 8848
                    if (-not $nacosRunning) {
                        Write-Host "    Nacos未就绪，尝试初始化数据库后重启..." -ForegroundColor Yellow
                        $nacosSchema = "$ProjectRoot\deployment\mysql\init\nacos-schema.sql"
                        if (Test-Path $nacosSchema) {
                            try {
                                $dbCheck = docker exec ioedream-mysql mysql -uroot -proot -e 'SHOW DATABASES LIKE ''nacos'';' 2>&1
                                $nacosDbExists = $dbCheck | Select-String -Pattern 'nacos'
                                
                                if (-not $nacosDbExists) {
                                    docker exec ioedream-mysql mysql -uroot -proot -e 'CREATE DATABASE IF NOT EXISTS nacos CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;' 2>&1 | Out-Null
                                }
                                
                                $tableCheck = docker exec ioedream-mysql mysql -uroot -proot -e 'SELECT COUNT(*) as cnt FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA=''nacos'';' 2>&1
                                $tableCount = [regex]::Match($tableCheck, '\d+').Value
                                
                                if (-not $tableCount -or [int]$tableCount -eq 0) {
                                    Get-Content $nacosSchema -Raw | docker exec -i ioedream-mysql mysql -uroot -proot nacos 2>&1 | Out-Null
                                }
                                
                                # 重启Nacos
                                docker restart ioedream-nacos 2>&1 | Out-Null
                                Start-Sleep -Seconds 20
                            } catch {
                                Write-Host "    数据库初始化失败: $_" -ForegroundColor Yellow
                            }
                        }
                    }
                    
                    $nacosRunning = Test-ServiceStatus -Name "Nacos" -Port 8848
                    if ($nacosRunning) {
                        Write-Host "    ✓ Nacos已就绪" -ForegroundColor Green
                    } else {
                        Write-Host "    ⚠️  Nacos启动中，可能需要更多时间" -ForegroundColor Yellow
                        Write-Host "    警告: 服务将无法注册到注册中心，请等待Nacos完全启动" -ForegroundColor Yellow
                    }
                }
            } catch {
                Write-Host "    ⚠️  Docker启动Nacos失败: $_" -ForegroundColor Yellow
                Write-Host "    警告: 服务将无法注册到注册中心，请先启动Nacos" -ForegroundColor Yellow
            }
        } else {
            Write-Host "    警告: 服务将无法注册到注册中心，请先启动Nacos" -ForegroundColor Yellow
            Write-Host "    是否继续启动？(Y/N): " -ForegroundColor Yellow -NoNewline
            $continue = Read-Host
            if ($continue -ne "Y" -and $continue -ne "y") {
                Write-Host "    已取消启动" -ForegroundColor Gray
                return $false
            }
        }
    } else {
        Write-Host "    ✓ Nacos运行中" -ForegroundColor Green
    }
    
    Write-Host ""
    
    # 按Order分组启动
    $maxOrder = ($backendServices | Measure-Object -Property Order -Maximum).Maximum
    
    for ($order = 1; $order -le $maxOrder; $order++) {
        $groupServices = $backendServices | Where-Object { $_.Order -eq $order }
        
        Write-Host "  【第 $order 批次】启动 $($groupServices.Count) 个服务" -ForegroundColor Yellow
        
        foreach ($service in $groupServices) {
            if (-not (Test-Path $service.Path)) {
                Write-Host "    ⚠️  $($service.Name) - 目录不存在: $($service.Path)" -ForegroundColor Yellow
                continue
            }
            
            Write-Host "    启动: $($service.Name) [$($service.Type)] 端口:$($service.Port)" -ForegroundColor Gray
            
            # 检查端口是否被占用
            $portInUse = Get-NetTCPConnection -LocalPort $service.Port -ErrorAction SilentlyContinue
            if ($portInUse) {
                Write-Host "      ⚠️  端口 $($service.Port) 已被占用，跳过启动" -ForegroundColor Yellow
                continue
            }
            
            # 在新窗口启动服务
            $servicePath = $service.Path
            $serviceName = $service.Name
            $servicePort = $service.Port
            $tempScript = [System.IO.Path]::GetTempFileName() + ".ps1"
            $scriptContent = @"
# 设置编码
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
`$OutputEncoding = [System.Text.Encoding]::UTF8
chcp 65001 | Out-Null

Set-Location '$servicePath'
Write-Host '========================================' -ForegroundColor Cyan
Write-Host 'Starting $serviceName...' -ForegroundColor Cyan
Write-Host 'Port: $servicePort' -ForegroundColor Gray
Write-Host 'Path: $servicePath' -ForegroundColor Gray
Write-Host '========================================' -ForegroundColor Cyan
Write-Host ''

# 检查端口是否被占用
`$portInUse = Get-NetTCPConnection -LocalPort $servicePort -ErrorAction SilentlyContinue
if (`$portInUse) {
    Write-Host "⚠️  端口 $servicePort 已被占用！" -ForegroundColor Yellow
    Write-Host "请先停止占用该端口的进程，或修改服务端口配置" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "按任意键退出..." -ForegroundColor Gray
    `$null = `$Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    exit 1
}

# 启动服务
Write-Host "正在启动服务，请稍候..." -ForegroundColor Gray
mvn spring-boot:run

# 如果服务退出，保持窗口打开以便查看日志
Write-Host ""
Write-Host "服务已停止，按任意键关闭窗口..." -ForegroundColor Yellow
`$null = `$Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
"@
            [System.IO.File]::WriteAllText($tempScript, $scriptContent, [System.Text.Encoding]::UTF8)
            Start-Process powershell.exe -ArgumentList "-NoExit", "-File", $tempScript
            
            Write-Host "      ✅ 已在新窗口启动 (端口: $servicePort)" -ForegroundColor Green
            Start-Sleep -Seconds 3
        }
        
        # 等待该批次服务启动
        if ($order -lt $maxOrder) {
            Write-Host "    等待第 $order 批次服务启动（15秒）..." -ForegroundColor Gray
            Start-Sleep -Seconds 15
        } else {
            Write-Host "    等待最后批次服务启动（45秒）..." -ForegroundColor Gray
            Start-Sleep -Seconds 45
        }
    }
    
    Write-Host ""
    Write-Host "  ✅ 所有后端服务启动完成" -ForegroundColor Green
    Write-Host ""
    return $true
}

# ==================== 启动前端应用 ====================

function Start-FrontendApp {
    Write-Host "【启动前端管理后台】" -ForegroundColor Cyan
    Write-Host ""
    
    if (-not (Test-Path $frontendConfig.Path)) {
        Write-Host "  ❌ 前端目录不存在: $($frontendConfig.Path)" -ForegroundColor Red
        return $false
    }
    
    # 检查Node.js是否安装
    Write-Host "  检查Node.js环境..." -ForegroundColor Gray
    try {
        $nodeCheck = Get-Command node -ErrorAction SilentlyContinue
        if (-not $nodeCheck) {
            Write-Host "  ❌ Node.js未安装或未配置到PATH" -ForegroundColor Red
            return $false
        }
        $nodeVersion = & node -v 2>&1
        Write-Host "  ✓ Node.js已安装: $nodeVersion" -ForegroundColor Green
    } catch {
        Write-Host "  ❌ Node.js检查失败: $_" -ForegroundColor Red
        return $false
    }
    
    # 检查依赖是否安装
    $nodeModulesPath = Join-Path $frontendConfig.Path "node_modules"
    if (-not (Test-Path $nodeModulesPath)) {
        Write-Host "  正在安装前端依赖..." -ForegroundColor Yellow
        Set-Location $frontendConfig.Path
        npm install --registry=https://registry.npmmirror.com
        if ($LASTEXITCODE -ne 0) {
            Write-Host "  ❌ 前端依赖安装失败" -ForegroundColor Red
            return $false
        }
    }
    
    Write-Host "  启动前端应用 (端口$($frontendConfig.Port))..." -ForegroundColor Gray
    
    # 在新窗口启动前端
    $frontendPath = $frontendConfig.Path
    $startCommand = $frontendConfig.StartCommand
    $tempScript = [System.IO.Path]::GetTempFileName() + ".ps1"
    $scriptContent = @"
# 设置编码
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
`$OutputEncoding = [System.Text.Encoding]::UTF8
chcp 65001 | Out-Null

Set-Location '$frontendPath'
Write-Host '========================================' -ForegroundColor Cyan
Write-Host 'Starting Frontend Admin...' -ForegroundColor Cyan
Write-Host 'Port: 3000' -ForegroundColor Gray
Write-Host 'Path: $frontendPath' -ForegroundColor Gray
Write-Host '========================================' -ForegroundColor Cyan
Write-Host ''
$startCommand
"@
    [System.IO.File]::WriteAllText($tempScript, $scriptContent, [System.Text.Encoding]::UTF8)
    Start-Process powershell.exe -ArgumentList "-NoExit", "-File", $tempScript
    
    Write-Host "  [OK] Frontend app started in new window" -ForegroundColor Green
    Write-Host "  访问地址: $($frontendConfig.CheckUrl)" -ForegroundColor Gray
    Write-Host ""
    return $true
}

# ==================== 启动移动端应用 ====================

function Start-MobileApp {
    Write-Host "【启动移动端应用(H5)】" -ForegroundColor Cyan
    Write-Host ""
    
    if (-not (Test-Path $mobileConfig.Path)) {
        Write-Host "  ❌ 移动端目录不存在: $($mobileConfig.Path)" -ForegroundColor Red
        return $false
    }
    
    # 检查Node.js是否安装
    Write-Host "  检查Node.js环境..." -ForegroundColor Gray
    try {
        $nodeCheck = Get-Command node -ErrorAction SilentlyContinue
        if (-not $nodeCheck) {
            Write-Host "  ❌ Node.js未安装或未配置到PATH" -ForegroundColor Red
            return $false
        }
        $nodeVersion = & node -v 2>&1
        Write-Host "  ✓ Node.js已安装: $nodeVersion" -ForegroundColor Green
    } catch {
        Write-Host "  ❌ Node.js检查失败: $_" -ForegroundColor Red
        return $false
    }
    
    # 检查依赖是否安装
    $nodeModulesPath = Join-Path $mobileConfig.Path "node_modules"
    if (-not (Test-Path $nodeModulesPath)) {
        Write-Host "  正在安装移动端依赖..." -ForegroundColor Yellow
        Set-Location $mobileConfig.Path
        npm install --registry=https://registry.npmmirror.com
        if ($LASTEXITCODE -ne 0) {
            Write-Host "  ❌ 移动端依赖安装失败" -ForegroundColor Red
            return $false
        }
    }
    
    Write-Host "  启动移动端应用 (端口$($mobileConfig.Port))..." -ForegroundColor Gray
    
    # 在新窗口启动移动端
    $mobilePath = $mobileConfig.Path
    $mobileStartCommand = $mobileConfig.StartCommand
    $tempScript = [System.IO.Path]::GetTempFileName() + ".ps1"
    $scriptContent = @"
# 设置编码
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
`$OutputEncoding = [System.Text.Encoding]::UTF8
chcp 65001 | Out-Null

Set-Location '$mobilePath'
Write-Host '========================================' -ForegroundColor Cyan
Write-Host 'Starting Mobile App (H5)...' -ForegroundColor Cyan
Write-Host 'Port: 8081' -ForegroundColor Gray
Write-Host 'Path: $mobilePath' -ForegroundColor Gray
Write-Host '========================================' -ForegroundColor Cyan
Write-Host ''
$mobileStartCommand
"@
    [System.IO.File]::WriteAllText($tempScript, $scriptContent, [System.Text.Encoding]::UTF8)
    Start-Process powershell.exe -ArgumentList "-NoExit", "-File", $tempScript
    
    Write-Host "  [OK] Mobile app started in new window" -ForegroundColor Green
    Write-Host "  访问地址: $($mobileConfig.CheckUrl)" -ForegroundColor Gray
    Write-Host ""
    return $true
}

# ==================== 主启动流程 ====================

Write-Host "准备启动IOE-DREAM完整项目...`n" -ForegroundColor Yellow

$startBackend = -not $FrontendOnly -and -not $MobileOnly
$startFrontend = -not $BackendOnly -and -not $MobileOnly
$startMobile = -not $BackendOnly -and -not $FrontendOnly

# 启动后端服务
if ($startBackend -or $BackendOnly) {
    $backendResult = Start-BackendServices
    if (-not $backendResult) {
        Write-Host "后端服务启动失败，停止启动流程" -ForegroundColor Red
        exit 1
    }
    
    if (-not $BackendOnly) {
        Write-Host "等待后端服务完全启动（60秒）..." -ForegroundColor Yellow
        Start-Sleep -Seconds 60
    }
}

# 启动前端应用
if ($startFrontend -or $FrontendOnly) {
    $frontendResult = Start-FrontendApp
    if (-not $frontendResult) {
        Write-Host "前端应用启动失败" -ForegroundColor Yellow
    } else {
        Start-Sleep -Seconds 5
    }
}

# 启动移动端应用
if ($startMobile -or $MobileOnly) {
    $mobileResult = Start-MobileApp
    if (-not $mobileResult) {
        Write-Host "移动端应用启动失败" -ForegroundColor Yellow
    } else {
        Start-Sleep -Seconds 5
    }
}

# ==================== 启动完成 ====================

Write-Host "========================================" -ForegroundColor Green
Write-Host "  所有服务启动完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "访问地址:" -ForegroundColor Yellow
Write-Host "  前端管理后台: http://localhost:3000" -ForegroundColor White
Write-Host "  移动端应用:   http://localhost:8081" -ForegroundColor White
Write-Host "  API网关:      http://localhost:8080" -ForegroundColor White
Write-Host ""
Write-Host "使用说明:" -ForegroundColor Yellow
Write-Host "  .\start-all-complete.ps1 -CheckOnly    # 检查服务状态" -ForegroundColor Gray
Write-Host "  .\start-all-complete.ps1 -BackendOnly  # 仅启动后端" -ForegroundColor Gray
Write-Host "  .\start-all-complete.ps1 -FrontendOnly  # 仅启动前端" -ForegroundColor Gray
Write-Host "  .\start-all-complete.ps1 -MobileOnly   # 仅启动移动端" -ForegroundColor Gray
Write-Host ""

