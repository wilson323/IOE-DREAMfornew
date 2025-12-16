# =============================================================
# IOE-DREAM Database Migration Unification Fix Script
#
# @Author: IOE-DREAM Architecture Team
# @Date: 2025-12-15
# @Description: Batch fix microservice Flyway configuration issues
# =============================================================

#Requires -Version 5.1

[CmdletBinding()]
param(
    [Parameter(Mandatory = $false)]
    [string]$ProjectRoot = "D:\IOE-DREAM",

    [Parameter(Mandatory = $false)]
    [switch]$DryRun,

    [Parameter(Mandatory = $false)]
    [switch]$Confirm
)

# Set error handling preference
$ErrorActionPreference = "Stop"

# Services that need Flyway configuration
$ServicesNeedingFlyway = @(
    "ioedream-gateway-service",
    "ioedream-oa-service",
    "ioedream-video-service",
    "ioedream-visitor-service"
)

# Standard Flyway configuration template
$FlywayConfigTemplate = @"

  # ==================== Flyway Database Migration Configuration ====================
  flyway:
    enabled: \${FLYWAY_ENABLED:true}
    baseline-on-migrate: \${FLYWAY_BASELINE_ON_MIGRATE:true}
    baseline-version: \${FLYWAY_BASELINE_VERSION:0}
    baseline-description: \${FLYWAY_BASELINE_DESCRIPTION:Initial schema for SERVICE_NAME}
    locations: \${FLYWAY_LOCATIONS:classpath:db/migration}
    table: \${FLYWAY_TABLE:flyway_schema_history}
    validate-on-migrate: \${FLYWAY_VALIDATE_ON_MIGRATE:true}
    clean-disabled: \${FLYWAY_CLEAN_DISABLED:true}
    placeholders:
      service.name: \${spring.application.name}
      environment: \${spring.profiles.active}
      schema.name: \${FLYWAY_SCHEMA_NAME:ioedream}
"@

function Write-SectionHeader {
    param([string]$Message)
    Write-Host "`n=== $Message ===" -ForegroundColor Cyan
}

function Write-ServiceStatus {
    param(
        [string]$Service,
        [string]$Status,
        [string]$Message = ""
    )

    $Color = switch ($Status) {
        "SUCCESS" { "Green" }
        "ERROR" { "Red" }
        "WARNING" { "Yellow" }
        "INFO" { "Cyan" }
        default { "White" }
    }

    $Icon = switch ($Status) {
        "SUCCESS" { "[OK]" }
        "ERROR" { "[FAIL]" }
        "WARNING" { "[WARN]" }
        "INFO" { "[INFO]" }
        default { "[*]" }
    }

    Write-Host "$Icon $Service" -NoNewline -ForegroundColor $Color
    if ($Message) {
        Write-Host " - $Message" -ForegroundColor $Color
    } else {
        Write-Host ""
    }
}

function Test-FlywayConfiguration {
    param([string]$ConfigPath)

    try {
        if (-not (Test-Path $ConfigPath)) {
            return $false
        }

        $Content = Get-Content $ConfigPath -Raw -Encoding UTF8
        return $Content -match "flyway:"
    }
    catch {
        Write-Warning "Failed to read config file: $ConfigPath - $($_.Exception.Message)"
        return $false
    }
}

function Add-FlywayConfiguration {
    param(
        [string]$ConfigPath,
        [string]$ServiceName
    )

    try {
        $Content = Get-Content $ConfigPath -Raw -Encoding UTF8

        # Find RabbitMQ configuration location and add Flyway config after it
        $RabbitMqPattern = '(?s)rabbitmq:.*?virtual-host:.*?ioedream'

        if ($Content -match $RabbitMqPattern) {
            $ServiceSpecificConfig = $FlywayConfigTemplate -replace "SERVICE_NAME", $ServiceName
            $NewContent = $Content -replace $RabbitMqPattern, "$($Matches[0])$ServiceSpecificConfig"

            if (-not $DryRun) {
                if ($Confirm) {
                    $Response = Read-Host "Modify config file for $ServiceName? (y/N)"
                    if ($Response -ne "y" -and $Response -ne "Y") {
                        return $false
                    }
                }

                # Backup original file
                $BackupPath = "$ConfigPath.backup.$(Get-Date -Format 'yyyyMMddHHmmss')"
                Copy-Item $ConfigPath $BackupPath
                Write-Host "  [BACKUP] Created: $BackupPath" -ForegroundColor Gray

                Set-Content -Path $ConfigPath -Value $NewContent -Encoding UTF8
                Write-Host "  [SUCCESS] Configuration updated" -ForegroundColor Green
            }
            return $true
        }
        else {
            Write-Warning "RabbitMQ configuration insertion point not found"
            return $false
        }
    }
    catch {
        Write-Error "Failed to modify configuration: $($_.Exception.Message)"
        return $false
    }
}

# Main execution logic
try {
    Write-SectionHeader "IOE-DREAM Database Migration Unification Fix"
    Write-Host "Project Root: $ProjectRoot" -ForegroundColor Cyan
    Write-Host "Services to fix: $($ServicesNeedingFlyway.Count)" -ForegroundColor Yellow

    if ($DryRun) {
        Write-Host "DRY RUN MODE - No files will be modified" -ForegroundColor Magenta
    }

    $SuccessCount = 0
    $FailureCount = 0

    foreach ($Service in $ServicesNeedingFlyway) {
        Write-SectionHeader "Processing Service: $Service"

        $ServicePath = Join-Path $ProjectRoot "microservices\$Service"
        $ConfigFile = Join-Path $ServicePath "src\main\resources\application.yml"

        if (-not (Test-Path $ConfigFile)) {
            Write-ServiceStatus $Service "ERROR" "Config file not found: $ConfigFile"
            $FailureCount++
            continue
        }

        # Check if Flyway configuration already exists
        if (Test-FlywayConfiguration $ConfigFile) {
            Write-ServiceStatus $Service "SUCCESS" "Already contains Flyway config, skipping"
            $SuccessCount++
            continue
        }

        # Add Flyway configuration
        if (Add-FlywayConfiguration $ConfigFile $Service) {
            if ($DryRun) {
                Write-ServiceStatus $Service "INFO" "DRY RUN - Would add Flyway configuration"
            }
            $SuccessCount++
        } else {
            Write-ServiceStatus $Service "ERROR" "Failed to add Flyway configuration"
            $FailureCount++
        }
    }

    # Results summary
    Write-SectionHeader "Fix Results Summary"
    Write-Host "Successfully processed: $SuccessCount services" -ForegroundColor Green
    Write-Host "Failed to process: $FailureCount services" -ForegroundColor Red

    if ($FailureCount -eq 0 -and -not $DryRun) {
        Write-Host "`nAll services' Flyway configurations have been successfully unified!" -ForegroundColor Green
        Write-Host "Recommend restarting services to verify configuration takes effect" -ForegroundColor Cyan
    }
    elseif ($DryRun) {
        Write-Host "`nDRY RUN completed - To execute, remove -DryRun parameter" -ForegroundColor Magenta
    }
    else {
        Write-Host "`nSome services failed to fix, please check error messages" -ForegroundColor Yellow
    }

    # Verify fix results
    if (-not $DryRun -and $SuccessCount -gt 0) {
        Write-SectionHeader "Verifying Fix Results"

        foreach ($Service in $ServicesNeedingFlyway) {
            $ConfigFile = Join-Path $ProjectRoot "microservices\$Service\src\main\resources\application.yml"
            if (Test-Path $ConfigFile) {
                if (Test-FlywayConfiguration $ConfigFile) {
                    Write-ServiceStatus $Service "SUCCESS" "Flyway configuration ready"
                } else {
                    Write-ServiceStatus $Service "ERROR" "Flyway configuration missing"
                }
            }
        }
    }

    Write-SectionHeader "Script Execution Completed"
}
catch {
    Write-Error "Error occurred during script execution: $($_.Exception.Message)"
    Write-Error "Error location: Line $($_.InvocationInfo.ScriptLineNumber):$($_.InvocationInfo.OffsetInLine)"
    exit 1
}