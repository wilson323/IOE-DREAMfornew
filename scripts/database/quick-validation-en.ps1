# ============================================================
# IOE-DREAM Database Migration Quick Validation
# Version: v1.0.0
# Author: IOE-DREAM Team
# Created: 2025-12-15
# Purpose: Quick validation of database migration setup
# ============================================================

# Set execution policy
Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

# Global variables
$ScriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = Split-Path -Parent (Split-Path -Parent $ScriptRoot)
$ValidationResults = @{
    Total = 0
    Passed = 0
    Failed = 0
    Warnings = 0
    Critical = @()
}

# Logging function
function Write-Result {
    param([string]$Message, [string]$Level = "INFO")

    $ValidationResults.Total++

    switch ($Level) {
        "SUCCESS" {
            $ValidationResults.Passed++
            Write-Host "SUCCESS: $Message" -ForegroundColor Green
        }
        "ERROR" {
            $ValidationResults.Failed++
            Write-Host "ERROR: $Message" -ForegroundColor Red
        }
        "WARNING" {
            $ValidationResults.Warnings++
            Write-Host "WARNING: $Message" -ForegroundColor Yellow
        }
        "CRITICAL" {
            $ValidationResults.Failed++
            $ValidationResults.CriticalIssues += $Message
            Write-Host "CRITICAL: $Message" -ForegroundColor Red
        }
        default {
            Write-Host "INFO: $Message" -ForegroundColor Cyan
        }
    }
}

# 1. Validate MySQL installation
function Test-MySQLInstallation {
    Write-Host "`n=== MySQL Installation Validation ===" -ForegroundColor Cyan

    $PossiblePaths = @(
        "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe",
        "C:\xampp\mysql\bin\mysql.exe",
        "C:\wamp64\bin\mysql\mysql8.0.30\bin\mysql.exe"
    )

    $MySQLFound = $false
    foreach ($Path in $PossiblePaths) {
        if (Test-Path $Path) {
            Write-Result "MySQL found at: $Path" -Level "SUCCESS"
            $MySQLFound = $true
            break
        }
    }

    if (-not $MySQLFound) {
        Write-Result "MySQL command-line tools not found in standard locations" -Level "WARNING"
    }
}

# 2. Validate project structure
function Test-ProjectStructure {
    Write-Host "`n=== Project Structure Validation ===" -ForegroundColor Cyan

    $RequiredPaths = @(
        "microservices",
        "microservices/microservices-common",
        "microservices/ioedream-db-init",
        "scripts/database",
        "scripts/database/config"
    )

    foreach ($RelativePath in $RequiredPaths) {
        $FullPath = Join-Path $ProjectRoot $RelativePath
        if (Test-Path $FullPath) {
            Write-Result "Directory exists: $RelativePath" -Level "SUCCESS"
        } else {
            Write-Result "Directory missing: $RelativePath" -Level "CRITICAL"
        }
    }
}

# 3. Validate Flyway configurations
function Test-FlywayConfigurations {
    Write-Host "`n=== Flyway Configuration Validation ===" -ForegroundColor Cyan

    $ServicesPath = Join-Path $ProjectRoot "microservices"
    $ExpectedServices = @(
        "ioedream-gateway-service",
        "ioedream-common-service",
        "ioedream-device-comm-service",
        "ioedream-oa-service",
        "ioedream-access-service",
        "ioedream-attendance-service",
        "ioedream-video-service",
        "ioedream-consume-service",
        "ioedream-visitor-service"
    )

    $ConfiguredCount = 0

    foreach ($Service in $ExpectedServices) {
        $ConfigPath = Join-Path $ServicesPath "$Service\src\main\resources\application.yml"

        if (Test-Path $ConfigPath) {
            $Content = Get-Content $ConfigPath -Raw
            if ($Content -match "flyway:") {
                $ConfiguredCount++
                Write-Result "Flyway configured: $Service" -Level "SUCCESS"
            } else {
                Write-Result "Flyway missing: $Service" -Level "ERROR"
            }
        } else {
            Write-Result "Config file missing: $Service" -Level "WARNING"
        }
    }

    $Coverage = [math]::Round($ConfiguredCount / $ExpectedServices.Count * 100, 1)
    Write-Result "Flyway coverage: $ConfiguredCount/$($ExpectedServices.Count) ($Coverage%)" -Level "INFO"
}

# 4. Validate migration scripts
function Test-MigrationScripts {
    Write-Host "`n=== Migration Scripts Validation ===" -ForegroundColor Cyan

    $MigrationPath = Join-Path $ProjectRoot "microservices\ioedream-db-init\src\main\resources\db\migration"

    if (Test-Path $MigrationPath) {
        $Scripts = Get-ChildItem -Path $MigrationPath -Filter "V*.sql" | Sort-Object Name
        $ScriptCount = $Scripts.Count

        Write-Result "Found $ScriptCount migration scripts" -Level "SUCCESS"

        # Check naming convention
        $NamingCorrect = 0
        foreach ($Script in $Scripts) {
            if ($Script.Name -match "^V\d+_\d+_\d+__.*\.sql$") {
                $NamingCorrect++
                Write-Result "Naming correct: $($Script.Name)" -Level "SUCCESS"
            } else {
                Write-Result "Naming incorrect: $($Script.Name)" -Level "WARNING"
            }
        }

        if ($NamingCorrect -eq $ScriptCount) {
            Write-Result "All scripts follow naming convention" -Level "SUCCESS"
        }

        # Check script content
        $ValidScripts = 0
        foreach ($Script in $Scripts) {
            $Content = Get-Content $Script.FullName -Raw
            if ($Content -match "CREATE TABLE" -or $Content -match "ALTER TABLE" -or $Content -match "INSERT INTO") {
                $ValidScripts++
            }
        }

        Write-Result "Valid SQL scripts: $ValidScripts/$ScriptCount" -Level "INFO"
    } else {
        Write-Result "Migration directory not found" -Level "CRITICAL"
    }
}

# 5. Validate configuration files
function Test-ConfigurationFiles {
    Write-Host "`n=== Configuration Files Validation ===" -ForegroundColor Cyan

    $ConfigFiles = @(
        "scripts/database/config/migration-config.json",
        "scripts/database/database-utilities-en.ps1"
    )

    foreach ($RelativeFile in $ConfigFiles) {
        $FullPath = Join-Path $ProjectRoot $RelativeFile
        if (Test-Path $FullPath) {
            Write-Result "Config file exists: $RelativeFile" -Level "SUCCESS"
        } else {
            Write-Result "Config file missing: $RelativeFile" -Level "ERROR"
        }
    }
}

# 6. Validate Entity classes
function Test-EntityClasses {
    Write-Host "`n=== Entity Classes Validation ===" -ForegroundColor Cyan

    $EntityPath = Join-Path $ProjectRoot "microservices\microservices-common\src\main\java\net\lab1024\sa\common\entity"

    if (Test-Path $EntityPath) {
        $Entities = Get-ChildItem -Path $EntityPath -Filter "*Entity.java" -Recurse
        $EntityCount = $Entities.Count

        Write-Result "Found $EntityCount entity classes" -Level "SUCCESS"

        # Check for critical entities
        $CriticalEntities = @("UserEntity.java", "RoleEntity.java", "MenuEntity.java", "DeviceEntity.java")

        foreach ($CriticalEntity in $CriticalEntities) {
            $Found = $Entities | Where-Object { $_.Name -eq $CriticalEntity }
            if ($Found) {
                Write-Result "Critical entity found: $CriticalEntity" -Level "SUCCESS"
            } else {
                Write-Result "Critical entity missing: $CriticalEntity" -Level "WARNING"
            }
        }

        # Check annotations
        $WithTableName = 0
        foreach ($Entity in $Entities) {
            $Content = Get-Content $Entity.FullName -Raw
            if ($Content -match "@TableName") {
                $WithTableName++
            }
        }

        Write-Result "Entities with @TableName: $WithTableName/$EntityCount" -Level "INFO"
    } else {
        Write-Result "Entity directory not found" -Level "WARNING"
    }
}

# 7. Generate summary report
function New-SummaryReport {
    Write-Host "`n=== Validation Summary ===" -ForegroundColor Cyan

    $SuccessRate = if ($ValidationResults.Total -gt 0) {
        [math]::Round($ValidationResults.Passed / $ValidationResults.Total * 100, 1)
    } else { 0 }

    Write-Host "Total checks: $($ValidationResults.Total)" -ForegroundColor White
    Write-Host "Passed: $($ValidationResults.Passed)" -ForegroundColor Green
    Write-Host "Failed: $($ValidationResults.Failed)" -ForegroundColor Red
    Write-Host "Warnings: $($ValidationResults.Warnings)" -ForegroundColor Yellow
    Write-Host "Success rate: $SuccessRate%" -ForegroundColor $(if ($SuccessRate -ge 90) { "Green" } elseif ($SuccessRate -ge 70) { "Yellow" } else { "Red" })

    if ($ValidationResults.Critical.Count -gt 0) {
        Write-Host "`nCRITICAL ISSUES:" -ForegroundColor Red
        foreach ($Issue in $ValidationResults.Critical) {
            Write-Host "  - $Issue" -ForegroundColor Red
        }
    }

    Write-Host "`nRECOMMENDATIONS:" -ForegroundColor Cyan

    if ($ValidationResults.Failed -eq 0) {
        Write-Host "  Database migration setup is ready for use" -ForegroundColor Green
        Write-Host "  You can safely run the migration automation script" -ForegroundColor Green
    } elseif ($ValidationResults.Critical.Count -eq 0) {
        Write-Host "  Some issues found, but migration is still possible" -ForegroundColor Yellow
        Write-Host "  Consider fixing the warnings for optimal experience" -ForegroundColor Yellow
    } else {
        Write-Host "  CRITICAL issues must be resolved before migration" -ForegroundColor Red
        Write-Host "  Please address the critical issues above" -ForegroundColor Red
    }
}

# Main execution
try {
    Write-Host "IOE-DREAM Database Migration Validation" -ForegroundColor Green
    Write-Host "Started at: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Cyan
    Write-Host "Project root: $ProjectRoot" -ForegroundColor Cyan

    Test-MySQLInstallation
    Test-ProjectStructure
    Test-FlywayConfigurations
    Test-MigrationScripts
    Test-ConfigurationFiles
    Test-EntityClasses
    New-SummaryReport

    Write-Host "`nValidation completed successfully!" -ForegroundColor Green

    # Set exit code based on results
    if ($ValidationResults.Critical.Count -gt 0) {
        exit 1  # Critical issues
    } elseif ($ValidationResults.Failed -gt 0) {
        exit 2  # Some failures
    } else {
        exit 0  # Success
    }

} catch {
    Write-Host "Validation failed with error: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Stack trace: $($_.ScriptStackTrace)" -ForegroundColor Red
    exit 3
}