# =====================================================
# IOE-DREAM Unified Environment Variable Loader
# Version: v1.0.0
# Description: Load all environment variables from .env file in project root
# Created: 2025-12-15
# Requirement: All scripts must use this script to load environment variables
# =====================================================

param(
    [Parameter(Mandatory=$false)]
    [string]$EnvFile = ".env",

    [Parameter(Mandatory=$false)]
    [switch]$Silent
)

$ErrorActionPreference = "Stop"

# =====================================================
# Get Project Root Directory
# =====================================================

$scriptPath = $PSScriptRoot
if (-not $scriptPath -or $scriptPath -eq "") {
    $scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
}

# Check if .env exists in current directory
$projectRoot = if (Test-Path (Join-Path (Get-Location).Path ".env")) {
    (Get-Location).Path
} else {
    # Search upward for project root (directory containing .env)
    $currentPath = $scriptPath
    while ($currentPath -and $currentPath -ne (Split-Path -Parent $currentPath)) {
        if (Test-Path (Join-Path $currentPath ".env")) {
            $projectRoot = $currentPath
            break
        }
        $currentPath = Split-Path -Parent $currentPath
    }

    if (-not $projectRoot) {
        # If not found, use parent of script directory
        $projectRoot = Split-Path -Parent $scriptPath
    }

    $projectRoot
}

$envFilePath = Join-Path $projectRoot $EnvFile

# =====================================================
# Load Environment Variables
# =====================================================

function Load-EnvironmentVariables {
    if (-not (Test-Path $envFilePath)) {
        if (-not $Silent) {
            Write-Host "[WARN] Environment file not found: $envFilePath" -ForegroundColor Yellow
            Write-Host "[INFO] Using default configuration values..." -ForegroundColor Yellow
        }

        Set-DefaultEnvironmentVariables
        return $false
    }

    if (-not $Silent) {
        Write-Host "[INFO] Loading environment variables from: $envFilePath" -ForegroundColor Cyan
    }

    try {
        # Read environment file (UTF-8 encoding)
        $lines = Get-Content $envFilePath -Encoding UTF8 -ErrorAction Stop

        $loadedCount = 0
        foreach ($line in $lines) {
            # Skip empty lines and comments
            $trimmedLine = $line.Trim()
            if ([string]::IsNullOrWhiteSpace($trimmedLine) -or $trimmedLine.StartsWith("#")) {
                continue
            }

            # Parse KEY=VALUE format
            if ($trimmedLine -match '^([^=]+)=(.*)$') {
                $key = $matches[1].Trim()
                $value = $matches[2].Trim()

                # Remove quotes if present
                if ($value.StartsWith('"') -and $value.EndsWith('"')) {
                    $value = $value.Substring(1, $value.Length - 2)
                } elseif ($value.StartsWith("'") -and $value.EndsWith("'")) {
                    $value = $value.Substring(1, $value.Length - 2)
                }

                # Set environment variable
                Set-Item -Path "env:$key" -Value $value -ErrorAction Stop
                $loadedCount++

                if (-not $Silent) {
                    $displayValue = if ($key -match 'PASSWORD|TOKEN|SECRET|KEY') { '[SET]' } else { $value }
                    Write-Host "  [SET] $key = $displayValue" -ForegroundColor Gray
                }
            }
        }

        if (-not $Silent) {
            Write-Host "[OK] Loaded $loadedCount environment variables" -ForegroundColor Green
        }

        return $true
    } catch {
        if (-not $Silent) {
            Write-Host "[ERROR] Failed to load environment variables: $($_.Exception.Message)" -ForegroundColor Red
        }
        Set-DefaultEnvironmentVariables
        return $false
    }
}

function Set-DefaultEnvironmentVariables {
    # MySQL Configuration
    if (-not $env:MYSQL_HOST) { $env:MYSQL_HOST = "127.0.0.1" }
    if (-not $env:MYSQL_PORT) { $env:MYSQL_PORT = "3306" }
    if (-not $env:MYSQL_DATABASE) { $env:MYSQL_DATABASE = "ioedream" }
    if (-not $env:MYSQL_USERNAME) { $env:MYSQL_USERNAME = "root" }
    if (-not $env:MYSQL_PASSWORD) { $env:MYSQL_PASSWORD = "123456" }
    if (-not $env:MYSQL_ROOT_PASSWORD) { $env:MYSQL_ROOT_PASSWORD = "123456" }

    # Redis Configuration
    if (-not $env:REDIS_HOST) { $env:REDIS_HOST = "127.0.0.1" }
    if (-not $env:REDIS_PORT) { $env:REDIS_PORT = "6379" }
    if (-not $env:REDIS_PASSWORD) { $env:REDIS_PASSWORD = "redis123" }
    if (-not $env:REDIS_DATABASE) { $env:REDIS_DATABASE = "0" }
    if (-not $env:REDIS_TIMEOUT) { $env:REDIS_TIMEOUT = "3000" }

    # Nacos Configuration
    if (-not $env:NACOS_SERVER_ADDR) { $env:NACOS_SERVER_ADDR = "127.0.0.1:8848" }
    if (-not $env:NACOS_NAMESPACE) { $env:NACOS_NAMESPACE = "dev" }
    if (-not $env:NACOS_GROUP) { $env:NACOS_GROUP = "IOE-DREAM" }
    if (-not $env:NACOS_USERNAME) { $env:NACOS_USERNAME = "nacos" }
    if (-not $env:NACOS_PASSWORD) { $env:NACOS_PASSWORD = "nacos" }
    if (-not $env:NACOS_AUTH_TOKEN) {
        $env:NACOS_AUTH_TOKEN = "SU9FLURSRUFNLU5hY29zLVNlY3JldC1LZXktMjAyNC1TZWN1cml0eQ=="
    }

    # RabbitMQ Configuration
    if (-not $env:RABBITMQ_HOST) { $env:RABBITMQ_HOST = "127.0.0.1" }
    if (-not $env:RABBITMQ_PORT) { $env:RABBITMQ_PORT = "5672" }
    if (-not $env:RABBITMQ_USERNAME) { $env:RABBITMQ_USERNAME = "guest" }
    if (-not $env:RABBITMQ_PASSWORD) { $env:RABBITMQ_PASSWORD = "guest" }
    if (-not $env:RABBITMQ_VIRTUAL_HOST) { $env:RABBITMQ_VIRTUAL_HOST = "/" }

    # Jasypt Encryption Configuration
    if (-not $env:JASYPT_PASSWORD) { $env:JASYPT_PASSWORD = "IOE-DREAM-Jasypt-Secret-2024" }

    # JWT Security Configuration
    if (-not $env:JWT_SECRET) { $env:JWT_SECRET = "IOE-DREAM-JWT-Secret-Key-2024-Security" }
    if (-not $env:JWT_EXPIRATION) { $env:JWT_EXPIRATION = "86400" }

    # Spring Configuration
    if (-not $env:SPRING_PROFILES_ACTIVE) { $env:SPRING_PROFILES_ACTIVE = "dev" }
    if (-not $env:GATEWAY_URL) { $env:GATEWAY_URL = "http://localhost:8080" }

    # JVM Configuration
    if (-not $env:MAVEN_OPTS) {
        $env:MAVEN_OPTS = "-Xms1024m -Xmx2048m -XX:MaxMetaspaceSize=512m"
    }
}

# =====================================================
# Execute Loading
# =====================================================

if (-not $Silent) {
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "  IOE-DREAM Environment Variables" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""
}

$loaded = Load-EnvironmentVariables

if (-not $Silent) {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    if ($loaded) {
        Write-Host "[SUCCESS] Environment variables loaded" -ForegroundColor Green
    } else {
        Write-Host "[WARN] Using default environment variables" -ForegroundColor Yellow
    }
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""
}
