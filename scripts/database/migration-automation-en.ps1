# ============================================================
# IOE-DREAM Database Migration Automation Tool
# Version: v1.0.0
# Tech Stack: PowerShell 5.1+ + Spring Boot 3.5.8 + Flyway 9.x
# Author: IOE-DREAM Architecture Team
# Created: 2025-12-15
# Purpose: Automated database migration, validation, backup and rollback
# ============================================================

# Set execution policy
Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

# Global variables
$ScriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$LogDirectory = Join-Path $ScriptRoot "logs"
$ConfigFile = Join-Path $ScriptRoot "config\migration-config.json"
$MigrationHistory = @()

# Ensure log directory exists
if (!(Test-Path $LogDirectory)) {
    New-Item -ItemType Directory -Path $LogDirectory -Force | Out-Null
}

# Logging function
function Write-Log {
    param(
        [Parameter(Mandatory=$true)]
        [string]$Message,

        [ValidateSet("INFO", "WARN", "ERROR", "SUCCESS")]
        [string]$Level = "INFO"
    )

    $Timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $LogEntry = "[$Timestamp] [$Level] $Message"

    # Write to log file
    $LogFile = Join-Path $LogDirectory "migration-$(Get-Date -Format 'yyyyMMdd').log"
    $LogEntry | Out-File -FilePath $LogFile -Append -Encoding UTF8

    # Output different colors based on level
    switch ($Level) {
        "INFO" { Write-Host $LogEntry -ForegroundColor White }
        "WARN" { Write-Host $LogEntry -ForegroundColor Yellow }
        "ERROR" { Write-Host $LogEntry -ForegroundColor Red }
        "SUCCESS" { Write-Host $LogEntry -ForegroundColor Green }
    }
}

# Load configuration
function Load-Configuration {
    Write-Log "Loading migration configuration..."

    if (!(Test-Path $ConfigFile)) {
        Write-Log "Configuration file not found, creating default configuration" -Level "WARN"
        # Create default configuration
        $DefaultConfig = @{
            project = @{
                name = "IOE-DREAM"
                version = "1.0.0"
                description = "Smart Campus Card Management Platform"
            }
            environment = @{
                dev = @{
                    mysql_host = "127.0.0.1"
                    mysql_port = "3306"
                    mysql_username = "root"
                    mysql_password = "123456"
                    mysql_database = "ioedream"
                }
                test = @{
                    mysql_host = "test-mysql"
                    mysql_port = "3306"
                    mysql_username = "root"
                    mysql_password = "test123456"
                    mysql_database = "ioedream_test"
                }
                prod = @{
                    mysql_host = "prod-mysql-cluster"
                    mysql_port = "3306"
                    mysql_username = "admin"
                    mysql_password = "" # Use environment variables
                    mysql_database = "ioedream_prod"
                }
            }
        }

        $DefaultConfig | ConvertTo-Json -Depth 4 | Out-File -FilePath $ConfigFile -Encoding UTF8
    }

    try {
        $ConfigContent = Get-Content $ConfigFile -Raw | ConvertFrom-Json
        Write-Log "Configuration loaded successfully"
        return $ConfigContent
    }
    catch {
        Write-Log "Configuration file parsing failed: $($_.Exception.Message)" -Level "ERROR"
        return $null
    }
}

# Database connection test
function Test-DatabaseConnection {
    param(
        [Parameter(Mandatory=$true)]
        [string]$Host,

        [Parameter(Mandatory=$true)]
        [string]$Port,

        [Parameter(Mandatory=$true)]
        [string]$Username,

        [Parameter(Mandatory=$true)]
        [string]$Password,

        [Parameter(Mandatory=$true)]
        [string]$Database
    )

    Write-Log "Testing database connection..."

    try {
        # Test using MySQL client
        $TestQuery = "SELECT 1 as connection_test;"
        $Result = & "mysql" -h $Host -P $Port -u $Username -p$Password -e $TestQuery $Database 2>&1

        if ($LASTEXITCODE -eq 0) {
            Write-Log "Database connection successful" -Level "SUCCESS"
            return $true
        }
        else {
            Write-Log "Database connection failed: $Result" -Level "ERROR"
            return $false
        }
    }
    catch {
        Write-Log "Database connection exception: $($_.Exception.Message)" -Level "ERROR"
        return $false
    }
}

# Database backup
function Backup-Database {
    param(
        [Parameter(Mandatory=$true)]
        [hashtable]$Config,

        [string]$Environment = "dev"
    )

    Write-Log "Starting database backup..."

    $DbConfig = $Config.environment.$Environment
    $BackupFile = Join-Path $ScriptRoot "backups\ioedream-backup-$(Get-Date -Format 'yyyyMMdd-HHmmss').sql"

    # Ensure backup directory exists
    $BackupDir = Split-Path -Parent $BackupFile
    if (!(Test-Path $BackupDir)) {
        New-Item -ItemType Directory -Path $BackupDir -Force | Out-Null
    }

    try {
        $BackupCommand = "mysqldump -h $($DbConfig.mysql_host) -P $($DbConfig.mysql_port) -u $($DbConfig.mysql_username) -p$($DbConfig.mysql_password) --single-transaction --routines --triggers --events $($DbConfig.mysql_database) > `"$BackupFile`""

        Write-Log "Executing backup command: $BackupCommand"
        Invoke-Expression $BackupCommand

        if ($LASTEXITCODE -eq 0) {
            $FileSize = (Get-Item $BackupFile).Length / 1MB
            Write-Log "Backup completed successfully! File size: $($FileSize.ToString('F2'))MB" -Level "SUCCESS"
            return $BackupFile
        }
        else {
            Write-Log "Backup failed" -Level "ERROR"
            return $null
        }
    }
    catch {
        Write-Log "Backup exception: $($_.Exception.Message)" -Level "ERROR"
        return $null
    }
}

# Flyway migration execution
function Invoke-FlywayMigration {
    param(
        [Parameter(Mandatory=$true)]
        [hashtable]$Config,

        [string]$Environment = "dev",

        [string]$ServiceName = ""
    )

    Write-Log "Starting Flyway migration for service: $ServiceName"

    $DbConfig = $Config.environment.$Environment

    # Find migration scripts directory
    $MigrationPath = Join-Path $ScriptRoot "..\..\microservices\ioedream-db-init\src\main\resources\db\migration"

    if (!(Test-Path $MigrationPath)) {
        Write-Log "Migration scripts directory not found: $MigrationPath" -Level "ERROR"
        return $false
    }

    try {
        # Build Flyway command
        $FlywayCommand = "flyway migrate"
        $FlywayCommand += " -url=jdbc:mysql://$($DbConfig.mysql_host):$($DbConfig.mysql_port)/$($DbConfig.mysql_database)?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
        $FlywayCommand += " -user=$($DbConfig.mysql_username)"
        $FlywayCommand += " -password=$($DbConfig.mysql_password)"
        $FlywayCommand += " -locations=filesystem:$MigrationPath"
        $FlywayCommand += " -baselineOnMigrate=true"
        $FlywayCommand += " -baselineVersion=0"
        $FlywayCommand += " -baselineDescription=`"Initial schema for IOE-DREAM`""
        $FlywayCommand += " -table=flyway_schema_history"
        $FlywayCommand += " -validateOnMigrate=false"

        Write-Log "Executing Flyway command: $FlywayCommand"
        $Result = Invoke-Expression $FlywayCommand

        if ($LASTEXITCODE -eq 0) {
            Write-Log "Flyway migration completed successfully" -Level "SUCCESS"

            # Record migration history
            $MigrationHistory += @{
                timestamp = Get-Date
                service = $ServiceName
                environment = $Environment
                status = "SUCCESS"
                scripts = (Get-ChildItem $MigrationPath -Filter "V*.sql").Count
            }

            return $true
        }
        else {
            Write-Log "Flyway migration failed" -Level "ERROR"
            return $false
        }
    }
    catch {
        Write-Log "Flyway migration exception: $($_.Exception.Message)" -Level "ERROR"
        return $false
    }
}

# Migration validation
function Test-MigrationValidation {
    param(
        [Parameter(Mandatory=$true)]
        [hashtable]$Config,

        [string]$Environment = "dev"
    )

    Write-Log "Starting migration validation..."

    $DbConfig = $Config.environment.$Environment

    try {
        # Check Flyway schema history table
        $FlywayCheck = & "mysql" -h $DbConfig.mysql_host -P $DbConfig.mysql_port -u $DbConfig.mysql_username -p$DbConfig.mysql_password -e "SHOW TABLES LIKE 'flyway_schema_history'" $DbConfig.mysql_database 2>&1

        if ($FlywayCheck -match "flyway_schema_history") {
            Write-Log "Flyway schema history table exists" -Level "SUCCESS"

            # Check migration records
            $MigrationCount = & "mysql" -h $DbConfig.mysql_host -P $DbConfig.mysql_port -u $DbConfig.mysql_username -p$DbConfig.mysql_password -e "SELECT COUNT(*) as count FROM flyway_schema_history WHERE success = 1" $DbConfig.mysql_database 2>&1

            Write-Log "Successful migration count: $MigrationCount" -Level "SUCCESS"

            return $true
        }
        else {
            Write-Log "Flyway schema history table not found" -Level "WARN"
            return $false
        }
    }
    catch {
        Write-Log "Migration validation exception: $($_.Exception.Message)" -Level "ERROR"
        return $false
    }
}

# Generate migration report
function New-MigrationReport {
    Write-Log "Generating migration report..."

    $ReportFile = Join-Path $ScriptRoot "reports\migration-report-$(Get-Date -Format 'yyyyMMdd-HHmmss').html"

    # Ensure reports directory exists
    $ReportDir = Split-Path -Parent $ReportFile
    if (!(Test-Path $ReportDir)) {
        New-Item -ItemType Directory -Path $ReportDir -Force | Out-Null
    }

    $HtmlContent = @"
<!DOCTYPE html>
<html>
<head>
    <title>IOE-DREAM Database Migration Report</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .header { background-color: #f0f0f0; padding: 20px; border-radius: 5px; }
        .success { color: green; font-weight: bold; }
        .error { color: red; font-weight: bold; }
        .warning { color: orange; font-weight: bold; }
        table { border-collapse: collapse; width: 100%; margin-top: 20px; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #f2f2f2; }
    </style>
</head>
<body>
    <div class="header">
        <h1>IOE-DREAM Database Migration Report</h1>
        <p>Generated: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')</p>
        <p>Total migrations: $($MigrationHistory.Count)</p>
    </div>

    <h2>Migration History</h2>
    <table>
        <tr>
            <th>Timestamp</th>
            <th>Service</th>
            <th>Environment</th>
            <th>Status</th>
            <th>Scripts</th>
        </tr>
"@

    foreach ($Migration in $MigrationHistory) {
        $StatusClass = if ($Migration.status -eq "SUCCESS") { "success" } else { "error" }
        $HtmlContent += @"
        <tr>
            <td>$($Migration.timestamp)</td>
            <td>$($Migration.service)</td>
            <td>$($Migration.environment)</td>
            <td class="$StatusClass">$($Migration.status)</td>
            <td>$($Migration.scripts)</td>
        </tr>
"@
    }

    $HtmlContent += @"
    </table>
</body>
</html>
"@

    $HtmlContent | Out-File -FilePath $ReportFile -Encoding UTF8
    Write-Log "Migration report generated: $ReportFile" -Level "SUCCESS"

    return $ReportFile
}

# Main execution function
function Start-Migration {
    param(
        [string]$Environment = "dev",
        [switch]$Backup,
        [switch]$Validate,
        [switch]$Report,
        [string]$Service = ""
    )

    Write-Log "Starting IOE-DREAM database migration automation..." -Level "SUCCESS"
    Write-Log "Environment: $Environment"
    Write-Log "Service: $Service"

    # Load configuration
    $Config = Load-Configuration
    if (-not $Config) {
        Write-Log "Failed to load configuration, exiting" -Level "ERROR"
        exit 1
    }

    # Test database connection
    $DbConfig = $Config.environment.$Environment
    $ConnectionResult = Test-DatabaseConnection -Host $DbConfig.mysql_host -Port $DbConfig.mysql_port -Username $DbConfig.mysql_username -Password $DbConfig.mysql_password -Database $DbConfig.mysql_database

    if (-not $ConnectionResult) {
        Write-Log "Database connection failed, exiting" -Level "ERROR"
        exit 1
    }

    # Backup database if requested
    if ($Backup) {
        $BackupFile = Backup-Database -Config $Config -Environment $Environment
        if (-not $BackupFile) {
            Write-Log "Database backup failed, but continuing with migration" -Level "WARN"
        }
    }

    # Execute Flyway migration
    $MigrationResult = Invoke-FlywayMigration -Config $Config -Environment $Environment -ServiceName $Service

    if (-not $MigrationResult) {
        Write-Log "Migration failed" -Level "ERROR"
        exit 1
    }

    # Validate migration if requested
    if ($Validate) {
        $ValidationResult = Test-MigrationValidation -Config $Config -Environment $Environment
        if (-not $ValidationResult) {
            Write-Log "Migration validation failed" -Level "WARN"
        }
    }

    # Generate report if requested
    if ($Report) {
        $ReportFile = New-MigrationReport
        Write-Log "Migration report available at: $ReportFile"
    }

    Write-Log "IOE-DREAM database migration completed successfully!" -Level "SUCCESS"
}

# Command line interface
if ($args.Count -eq 0) {
    Write-Host "IOE-DREAM Database Migration Automation Tool" -ForegroundColor Green
    Write-Host ""
    Write-Host "Usage:"
    Write-Host "  .\migration-automation-en.ps1 [Environment] [Options]"
    Write-Host ""
    Write-Host "Parameters:"
    Write-Host "  Environment    Target environment (dev/test/prod, default: dev)"
    Write-Host ""
    Write-Host "Options:"
    Write-Host "  -Backup        Backup database before migration"
    Write-Host "  -Validate      Validate migration after execution"
    Write-Host "  -Report        Generate migration report"
    Write-Host "  -Service       Specify service name"
    Write-Host ""
    Write-Host "Examples:"
    Write-Host "  .\migration-automation-en.ps1 dev -Backup -Validate -Report"
    Write-Host "  .\migration-automation-en.ps1 prod -Backup"
    exit 0
}

# Parse command line arguments
$Environment = "dev"
$BackupSwitch = $false
$ValidateSwitch = $false
$ReportSwitch = $false
$ServiceName = ""

foreach ($Arg in $args) {
    switch ($Arg) {
        "dev" { $Environment = "dev" }
        "test" { $Environment = "test" }
        "prod" { $Environment = "prod" }
        "-Backup" { $BackupSwitch = $true }
        "-Validate" { $ValidateSwitch = $true }
        "-Report" { $ReportSwitch = $true }
        default {
            if ($Arg.StartsWith("-Service:")) {
                $ServiceName = $Arg.Substring(9)
            }
        }
    }
}

# Start migration
Start-Migration -Environment $Environment -Backup:$BackupSwitch -Validate:$ValidateSwitch -Report:$ReportSwitch -Service $ServiceName