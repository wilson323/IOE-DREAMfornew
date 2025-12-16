# =====================================================
# IOE-DREAM Microservices Dynamic Verification Script
# Version: v1.0.0
# Description: Execute microservices dynamic verification (startup, Nacos registration, database connection, health check)
# Created: 2025-01-30
# =====================================================

param(
    [Parameter(Mandatory=$false)]
    [switch]$SkipStartup,

    [Parameter(Mandatory=$false)]
    [switch]$SkipNacos,

    [Parameter(Mandatory=$false)]
    [switch]$SkipDatabase,
    
    [Parameter(Mandatory=$false)]
    [switch]$SkipHealth,
    
    [Parameter(Mandatory=$false)]
    [string]$NacosServer = "127.0.0.1:8848",
    
    [Parameter(Mandatory=$false)]
    [string]$NacosUsername = "nacos",
    
    [Parameter(Mandatory=$false)]
    [string]$NacosPassword = "nacos",
    
    [Parameter(Mandatory=$false)]
    [switch]$GenerateReport
)

$ErrorActionPreference = "Stop"

# =====================================================
# Configuration
# =====================================================

$Services = @(
    @{ Name = "ioedream-gateway-service"; Port = 8080; Type = "Infrastructure"; HealthPath = "/actuator/health" },
    @{ Name = "ioedream-common-service"; Port = 8088; Type = "Core"; HealthPath = "/actuator/health" },
    @{ Name = "ioedream-device-comm-service"; Port = 8087; Type = "Core"; HealthPath = "/actuator/health" },
    @{ Name = "ioedream-oa-service"; Port = 8089; Type = "Core"; HealthPath = "/actuator/health" },
    @{ Name = "ioedream-access-service"; Port = 8090; Type = "Business"; HealthPath = "/actuator/health" },
    @{ Name = "ioedream-attendance-service"; Port = 8091; Type = "Business"; HealthPath = "/actuator/health" },
    @{ Name = "ioedream-video-service"; Port = 8092; Type = "Business"; HealthPath = "/actuator/health" },
    @{ Name = "ioedream-consume-service"; Port = 8094; Type = "Business"; HealthPath = "/actuator/health" },
    @{ Name = "ioedream-visitor-service"; Port = 8095; Type = "Business"; HealthPath = "/actuator/health" }
)

$InfrastructurePorts = @(
    @{ Name = "MySQL"; Port = 3306 },
    @{ Name = "Redis"; Port = 6379 },
    @{ Name = "Nacos"; Port = 8848 }
)

# =====================================================
# Utility Functions
# =====================================================

function Write-ColorOutput($ForegroundColor) {
    $fc = $host.UI.RawUI.ForegroundColor
    $host.UI.RawUI.ForegroundColor = $ForegroundColor
    if ($args) {
        Write-Output $args
    }
    $host.UI.RawUI.ForegroundColor = $fc
}

function Test-PortInUse {
    param([int]$Port)
    
    try {
        $tcpClient = New-Object System.Net.Sockets.TcpClient
        $asyncResult = $tcpClient.BeginConnect("127.0.0.1", $Port, $null, $null)
        $wait = $asyncResult.AsyncWaitHandle.WaitOne(1000, $false)
        
        if ($wait) {
            try {
                $tcpClient.EndConnect($asyncResult)
                $tcpClient.Close()
                return $true
            } catch {
                $tcpClient.Close()
                return $false
            }
        } else {
            $tcpClient.Close()
            return $false
        }
    } catch {
        return $false
    }
}

function Invoke-HttpRequest {
    param(
        [string]$Url,
        [string]$Method = "GET",
        [hashtable]$Headers = @{},
        [int]$TimeoutSeconds = 5
    )
    
    try {
        $request = [System.Net.WebRequest]::Create($Url)
        $request.Method = $Method
        $request.Timeout = $TimeoutSeconds * 1000

        foreach ($key in $Headers.Keys) {
            $request.Headers.Add($key, $Headers[$key])
        }
        
        $response = $request.GetResponse()
        $statusCode = [int]$response.StatusCode
        $stream = $response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($stream)
        $content = $reader.ReadToEnd()
        $reader.Close()
        $response.Close()
        
        return @{
            Success = $true
            StatusCode = $statusCode
            Content = $content
        }
    } catch {
        return @{
            Success = $false
            StatusCode = 0
            Content = $_.Exception.Message
        }
    }
}

# =====================================================
# Verification Functions
# =====================================================

function Test-Infrastructure {
    Write-ColorOutput Cyan "`n========================================"
    Write-ColorOutput Cyan "  1. Infrastructure Verification"
    Write-ColorOutput Cyan "========================================`n"

    $allGood = $true
    $results = @{}

    foreach ($infra in $InfrastructurePorts) {
        $isRunning = Test-PortInUse -Port $infra.Port
        $results[$infra.Name] = $isRunning

        if ($isRunning) {
            $msg = "  [OK] $($infra.Name) (Port $($infra.Port)) - Running"
            Write-ColorOutput Green $msg
    } else {
            $msg = "  [XX] $($infra.Name) (Port $($infra.Port)) - Not running"
            Write-ColorOutput Red $msg
            $allGood = $false
        }
    }
    
    Write-Output ""
    
    if (-not $allGood) {
        $warnMsg = "  [WARN] Some infrastructure services are not running, may affect subsequent verification"
        Write-ColorOutput Yellow $warnMsg
        Write-ColorOutput Yellow "  Please start infrastructure services first:"
        Write-Output "    docker-compose -f docker-compose-all.yml up -d mysql redis nacos"
        Write-Output ""
    }
    
    return @{
        AllGood = $allGood
        Results = $results
    }
}

function Test-ServiceStartup {
    if ($SkipStartup) {
        $skipMsg = "`n[SKIP] Skip startup verification"
        Write-ColorOutput Yellow $skipMsg
        return @{ AllGood = $true; Results = @{} }
    }

    Write-ColorOutput Cyan "`n========================================"
    Write-ColorOutput Cyan "  2. Service Startup Verification (Port Listening)"
    Write-ColorOutput Cyan "========================================`n"
    
    $results = @{}
    $allGood = $true
    
    foreach ($service in $Services) {
        $isRunning = Test-PortInUse -Port $service.Port
        $results[$service.Name] = @{
            Port = $service.Port
            Running = $isRunning
        }

        if ($isRunning) {
            $msg = "  [OK] $($service.Name) (Port $($service.Port)) - Running"
            Write-ColorOutput Green $msg
        } else {
            $msg = "  [XX] $($service.Name) (Port $($service.Port)) - Not running"
            Write-ColorOutput Red $msg
            $allGood = $false
        }
    }

    Write-Output ""

    if (-not $allGood) {
        $warnMsg = "  [WARN] Some services are not running, please start services first"
        Write-ColorOutput Yellow $warnMsg
        Write-ColorOutput Yellow "  Startup command example:"
        Write-Output "    cd microservices\$($Services[0].Name)"
        Write-Output "    mvn spring-boot:run"
        Write-Output ""
    }
    
    return @{
        AllGood = $allGood
        Results = $results
    }
}

function Test-NacosRegistration {
    if ($SkipNacos) {
        $skipMsg = "`n[SKIP] Skip Nacos registration verification"
        Write-ColorOutput Yellow $skipMsg
        return @{ AllGood = $true; Results = @{} }
    }

    Write-ColorOutput Cyan "`n========================================"
    Write-ColorOutput Cyan "  3. Nacos Registration Verification"
    Write-ColorOutput Cyan "========================================`n"

    # Check if Nacos is running
    $nacosRunning = Test-PortInUse -Port 8848
    if (-not $nacosRunning) {
        $errMsg = "  [XX] Nacos is not running, cannot verify service registration"
        Write-ColorOutput Red $errMsg
        Write-Output ""
        return @{
            AllGood = $false
            Results = @{}
            Error = "Nacos not running"
        }
    }

    # Get Nacos access token
    $tokenUrl = 'http://' + $NacosServer + '/nacos/v1/auth/login'
    $ampersand = [char]38
    $tokenBody = 'username=' + $NacosUsername + $ampersand + 'password=' + $NacosPassword

    try {
        $tokenRequest = [System.Net.WebRequest]::Create($tokenUrl)
        $tokenRequest.Method = 'POST'
        $tokenRequest.ContentType = 'application/x-www-form-urlencoded'
        $tokenRequest.Timeout = 5000

        $tokenStream = $tokenRequest.GetRequestStream()
        $tokenBytes = [System.Text.Encoding]::UTF8.GetBytes($tokenBody)
        $tokenStream.Write($tokenBytes, 0, $tokenBytes.Length)
        $tokenStream.Close()

        $tokenResponse = $tokenRequest.GetResponse()
        $tokenStream = $tokenResponse.GetResponseStream()
        $tokenReader = New-Object System.IO.StreamReader($tokenStream)
        $tokenContent = $tokenReader.ReadToEnd()
        $tokenReader.Close()
        $tokenResponse.Close()

        # Parse token (Nacos response format: accessToken=xxx)
        $accessToken = ''
        $quote = [char]34
        $tokenPattern1Str = 'accessToken=([^' + $ampersand + ']+)'
        $tokenPattern1 = [regex]::new($tokenPattern1Str)
        $tokenPattern2Str = $quote + 'accessToken' + $quote + ':' + $quote + '([^' + $quote + ']+)' + $quote
        $tokenPattern2 = [regex]::new($tokenPattern2Str)

        $match1 = $tokenPattern1.Match($tokenContent)
        if ($match1.Success) {
            $accessToken = $match1.Groups[1].Value
        } else {
            $match2 = $tokenPattern2.Match($tokenContent)
            if ($match2.Success) {
                $accessToken = $match2.Groups[1].Value
            }
        }

        if ([string]::IsNullOrEmpty($accessToken)) {
            $warnMsg = '  [WARN] Cannot get Nacos access token, try unauthenticated access'
            Write-ColorOutput Yellow $warnMsg
        }
    } catch {
        $warnMsg = '  [WARN] Nacos authentication failed, try unauthenticated access: ' + $_.Exception.Message
        Write-ColorOutput Yellow $warnMsg
        $accessToken = ""
    }

    # Query service list
    $results = @{}
    $allGood = $true
    
    foreach ($service in $Services) {
        $serviceName = $service.Name
        $nacosUrl = 'http://' + $NacosServer + '/nacos/v1/ns/instance/list?serviceName=' + $serviceName + $ampersand + 'namespaceId=dev'

        if (-not [string]::IsNullOrEmpty($accessToken)) {
            $nacosUrl += $ampersand + 'accessToken=' + $accessToken
        }

        $response = Invoke-HttpRequest -Url $nacosUrl -TimeoutSeconds 5

        if ($response.Success -and $response.StatusCode -eq 200) {
            # Check if response contains service instances
            $hostsPattern = [regex]::new($quote + 'hosts' + $quote)
            $ipPattern = [regex]::new($quote + 'ip' + $quote)

            if ($hostsPattern.IsMatch($response.Content)) {
                # Try to parse JSON (simple check)
                if ($ipPattern.IsMatch($response.Content)) {
                    $okMsg = "  [OK] $serviceName - Registered to Nacos"
                    Write-ColorOutput Green $okMsg
                    $results[$serviceName] = @{ Registered = $true; Instances = 1 }
                } else {
                    $warnMsg = "  [WARN] $serviceName - Nacos response abnormal"
                    Write-ColorOutput Yellow $warnMsg
                    $results[$serviceName] = @{ Registered = $false; Instances = 0 }
                    $allGood = $false
                }
            } else {
                $errMsg = "  [XX] $serviceName - Not registered to Nacos"
                Write-ColorOutput Red $errMsg
                $results[$serviceName] = @{ Registered = $false; Instances = 0 }
                $allGood = $false
            }
        } else {
            $warnMsg = "  [WARN] $serviceName - Cannot query Nacos (Status: $($response.StatusCode))"
            Write-ColorOutput Yellow $warnMsg
            $results[$serviceName] = @{ Registered = $false; Instances = 0; Error = $response.Content }
            $allGood = $false
        }
    }

    Write-Output ""

    if (-not $allGood) {
        $warnMsg = "  [WARN] Some services are not registered to Nacos"
        Write-ColorOutput Yellow $warnMsg
        Write-ColorOutput Yellow "  Please check:"
        Write-Output "    1. Are services started?"
        Write-Output "    2. Is Nacos configuration correct?"
        Write-Output "    3. Is network connection normal?"
        Write-Output ""
    }
    
    return @{
        AllGood = $allGood
        Results = $results
    }
}

function Test-DatabaseConnection {
    if ($SkipDatabase) {
        $skipMsg = "`n[SKIP] Skip database connection verification"
        Write-ColorOutput Yellow $skipMsg
        return @{ AllGood = $true; Results = @{} }
    }

    Write-ColorOutput Cyan "`n========================================"
    Write-ColorOutput Cyan "  4. Database Connection Verification"
    Write-ColorOutput Cyan "========================================`n"

    # Check if MySQL is running
    $mysqlRunning = Test-PortInUse -Port 3306
    if (-not $mysqlRunning) {
        $errMsg = "  [XX] MySQL is not running, cannot verify database connection"
        Write-ColorOutput Red $errMsg
        Write-Output ""
        return @{
            AllGood = $false
            Results = @{}
            Error = "MySQL not running"
        }
    }
    
    # Verify database connection through health check endpoint
    $results = @{}
    $allGood = $true
    $quote = [char]34
    
    foreach ($service in $Services) {
        if ($service.Name -eq "ioedream-gateway-service") {
            # Gateway service does not connect to database
            $skipMsg = "  [SKIP] $($service.Name) - Gateway service, no database connection"
            Write-ColorOutput Gray $skipMsg
            $results[$service.Name] = @{ Connected = $true; Reason = "Gateway service has no database" }
            continue
        }
        
        $healthUrl = "http://127.0.0.1:$($service.Port)$($service.HealthPath)"
        $response = Invoke-HttpRequest -Url $healthUrl -TimeoutSeconds 5
            
        if ($response.Success -and $response.StatusCode -eq 200) {
            # Check if health check response contains database status
            $dbPatternStr = $quote + 'db' + $quote + '.*' + $quote + 'status' + $quote + '.*' + $quote + 'UP' + $quote
            $dbPattern = [regex]::new($dbPatternStr)
            $statusPatternStr = $quote + 'status' + $quote + '.*' + $quote + 'UP' + $quote
            $statusPattern = [regex]::new($statusPatternStr)

            if ($dbPattern.IsMatch($response.Content)) {
                $okMsg = "  [OK] $($service.Name) - Database connection OK"
                Write-ColorOutput Green $okMsg
                $results[$service.Name] = @{ Connected = $true }
            } elseif ($statusPattern.IsMatch($response.Content)) {
                # Health check is UP, but may not have detailed database status
                $okMsg = "  [OK] $($service.Name) - Service healthy (database status unclear)"
                Write-ColorOutput Green $okMsg
                $results[$service.Name] = @{ Connected = $true; Note = "Health check is UP" }
            } else {
                $warnMsg = "  [WARN] $($service.Name) - Database status unknown"
                Write-ColorOutput Yellow $warnMsg
                $results[$service.Name] = @{ Connected = $false; Note = "Health check response abnormal" }
                $allGood = $false
            }
        } else {
            $errMsg = "  [XX] $($service.Name) - Cannot access health check endpoint"
            Write-ColorOutput Red $errMsg
            $results[$service.Name] = @{ Connected = $false; Error = "Cannot access health check" }
            $allGood = $false
        }
    }

    Write-Output ""

    if (-not $allGood) {
        $warnMsg = "  [WARN] Some services have database connection issues"
        Write-ColorOutput Yellow $warnMsg
        Write-ColorOutput Yellow "  Please check:"
        Write-Output "    1. Is database configuration correct?"
        Write-Output "    2. Are database user permissions correct?"
        Write-Output "    3. Is database initialized?"
        Write-Output ""
    }
    
    return @{
        AllGood = $allGood
        Results = $results
    }
}

function Test-HealthCheck {
    if ($SkipHealth) {
        $skipMsg = "`n[SKIP] Skip health check verification"
        Write-ColorOutput Yellow $skipMsg
        return @{ AllGood = $true; Results = @{} }
    }

    Write-ColorOutput Cyan "`n========================================"
    Write-ColorOutput Cyan "  5. Health Check Verification"
    Write-ColorOutput Cyan "========================================`n"
    
    $results = @{}
    $allGood = $true
    $quote = [char]34
    
    foreach ($service in $Services) {
        $healthUrl = "http://127.0.0.1:$($service.Port)$($service.HealthPath)"
        $response = Invoke-HttpRequest -Url $healthUrl -TimeoutSeconds 5
        
        if ($response.Success -and $response.StatusCode -eq 200) {
            # Check health status
            $upPatternStr = $quote + 'status' + $quote + '.*' + $quote + 'UP' + $quote
            $upPattern = [regex]::new($upPatternStr)
            $downPatternStr = $quote + 'status' + $quote + '.*' + $quote + 'DOWN' + $quote
            $downPattern = [regex]::new($downPatternStr)

            if ($upPattern.IsMatch($response.Content)) {
                $okMsg = "  [OK] $($service.Name) - Health status: UP"
                Write-ColorOutput Green $okMsg
                $results[$service.Name] = @{ Status = "UP"; StatusCode = $response.StatusCode }
            } elseif ($downPattern.IsMatch($response.Content)) {
                $errMsg = "  [XX] $($service.Name) - Health status: DOWN"
                Write-ColorOutput Red $errMsg
                $results[$service.Name] = @{ Status = "DOWN"; StatusCode = $response.StatusCode }
                $allGood = $false
                } else {
                $warnMsg = "  [WARN] $($service.Name) - Health status unknown"
                Write-ColorOutput Yellow $warnMsg
                $results[$service.Name] = @{ Status = "UNKNOWN"; StatusCode = $response.StatusCode }
                $allGood = $false
            }
        } else {
            $errMsg = "  [XX] $($service.Name) - Cannot access health check endpoint (Status: $($response.StatusCode))"
            Write-ColorOutput Red $errMsg
            $results[$service.Name] = @{ Status = "ERROR"; StatusCode = $response.StatusCode; Error = $response.Content }
            $allGood = $false
        }
    }

    Write-Output ""

    if (-not $allGood) {
        $warnMsg = "  [WARN] Some services failed health check"
        Write-ColorOutput Yellow $warnMsg
        Write-ColorOutput Yellow "  Please check:"
        Write-Output "    1. Are services started?"
        Write-Output "    2. Is health check endpoint enabled?"
        Write-Output "    3. Are there any errors in service logs?"
        Write-Output ""
    }
    
    return @{
        AllGood = $allGood
        Results = $results
    }
}

# =====================================================
# Main Program
# =====================================================

Write-ColorOutput Cyan "========================================"
Write-ColorOutput Cyan "  IOE-DREAM Microservices Dynamic Verification"
Write-ColorOutput Cyan "========================================"
Write-Output ""
$verificationTime = Get-Date -Format 'yyyy-MM-dd HH:mm:ss'
Write-Output "Verification Time: $verificationTime"
Write-Output "Verification Scope: 9 Core Microservices"
Write-Output ""

$verificationResults = @{
    Infrastructure = $null
    Startup = $null
    Nacos = $null
    Database = $null
    Health = $null
}

# 1. Infrastructure verification
$verificationResults.Infrastructure = Test-Infrastructure

# 2. Service startup verification
$verificationResults.Startup = Test-ServiceStartup

# 3. Nacos registration verification
$verificationResults.Nacos = Test-NacosRegistration

# 4. Database connection verification
$verificationResults.Database = Test-DatabaseConnection

# 5. Health check verification
$verificationResults.Health = Test-HealthCheck

# =====================================================
# Verification Results Summary
# =====================================================

Write-ColorOutput Cyan "`n========================================"
Write-ColorOutput Cyan "  Verification Results Summary"
Write-ColorOutput Cyan "========================================`n"

$totalChecks = 0
$passedChecks = 0

if ($verificationResults.Infrastructure) {
    $totalChecks++
    if ($verificationResults.Infrastructure.AllGood) {
        $passedChecks++
        $okMsg = "  [OK] Infrastructure verification - PASSED"
        Write-ColorOutput Green $okMsg
    } else {
        $errMsg = "  [XX] Infrastructure verification - FAILED"
        Write-ColorOutput Red $errMsg
    }
}

if ($verificationResults.Startup) {
    $totalChecks++
    if ($verificationResults.Startup.AllGood) {
        $passedChecks++
        $okMsg = "  [OK] Service startup verification - PASSED"
        Write-ColorOutput Green $okMsg
    } else {
        $errMsg = "  [XX] Service startup verification - FAILED"
        Write-ColorOutput Red $errMsg
    }
}

if ($verificationResults.Nacos) {
    $totalChecks++
    if ($verificationResults.Nacos.AllGood) {
        $passedChecks++
        $okMsg = "  [OK] Nacos registration verification - PASSED"
        Write-ColorOutput Green $okMsg
    } else {
        $errMsg = "  [XX] Nacos registration verification - FAILED"
        Write-ColorOutput Red $errMsg
    }
}

if ($verificationResults.Database) {
    $totalChecks++
    if ($verificationResults.Database.AllGood) {
        $passedChecks++
        $okMsg = "  [OK] Database connection verification - PASSED"
        Write-ColorOutput Green $okMsg
    } else {
        $errMsg = "  [XX] Database connection verification - FAILED"
        Write-ColorOutput Red $errMsg
    }
}

if ($verificationResults.Health) {
    $totalChecks++
    if ($verificationResults.Health.AllGood) {
        $passedChecks++
        $okMsg = "  [OK] Health check verification - PASSED"
        Write-ColorOutput Green $okMsg
    } else {
        $errMsg = "  [XX] Health check verification - FAILED"
        Write-ColorOutput Red $errMsg
    }
}

Write-Output ""
Write-Output "Total checks: $totalChecks"
Write-Output "Passed: $passedChecks"
Write-Output "Failed: $($totalChecks - $passedChecks)"
if ($totalChecks -gt 0) {
    $passRate = [math]::Round($passedChecks / $totalChecks * 100, 2)
    Write-Output "Pass rate: $passRate%"
}
Write-Output ""

if ($passedChecks -eq $totalChecks -and $totalChecks -gt 0) {
    Write-ColorOutput Green "========================================"
    Write-ColorOutput Green "  All dynamic verifications PASSED!"
    Write-ColorOutput Green "========================================`n"
    exit 0
} else {
    Write-ColorOutput Yellow "========================================"
    Write-ColorOutput Yellow "  Some verifications FAILED, please check the issues above"
    Write-ColorOutput Yellow "========================================`n"
    exit 1
}
