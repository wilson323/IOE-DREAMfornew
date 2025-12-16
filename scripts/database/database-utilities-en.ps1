# ============================================================
# IOE-DREAM Database Utilities Library
# Version: v1.0.0
# Author: IOE-DREAM Team
# Created: 2025-12-15
# Purpose: Core database utility functions for migration
# ============================================================

# Set execution policy
Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

# Global variables
$Global:MySQLPaths = @()
$Global:MySQLExecutable = $null
$Global:MysqldumpExecutable = $null

# Initialize MySQL path detection
function Initialize-MySQLPaths {
    Write-Host "Detecting MySQL installation paths..." -ForegroundColor Cyan

    # Common MySQL installation paths
    $PossiblePaths = @(
        "C:\Program Files\MySQL\MySQL Server 8.0\bin",
        "C:\Program Files\MySQL\MySQL Server 8.4\bin",
        "C:\Program Files\MySQL\MySQL Server 5.7\bin",
        "C:\Program Files (x86)\MySQL\MySQL Server 8.0\bin",
        "C:\xampp\mysql\bin",
        "C:\wamp64\bin\mysql\mysql8.0.30\bin",
        "C:\mysql\bin"
    )

    # Check environment variable PATH
    $env:PATH -split ';' | ForEach-Object {
        if ($_ -match "mysql" -and (Test-Path "$_\mysql.exe")) {
            $PossiblePaths += $_
        }
    }

    # Detect available MySQL paths
    foreach ($Path in $PossiblePaths) {
        if (Test-Path "$Path\mysql.exe") {
            $Global:MySQLExecutable = "$Path\mysql.exe"
            Write-Host "Found MySQL client: $Global:MySQLExecutable" -ForegroundColor Green
        }

        if (Test-Path "$Path\mysqldump.exe") {
            $Global:MysqldumpExecutable = "$Path\mysqldump.exe"
            Write-Host "Found mysqldump tool: $Global:MysqldumpExecutable" -ForegroundColor Green
        }
    }

    if (-not $Global:MySQLExecutable -and -not $Global:MysqldumpExecutable) {
        Write-Host "MySQL command-line tools not found, will use alternative solutions" -ForegroundColor Yellow
        return $false
    }

    return $true
}

# Test database connection (supports multiple methods)
function Test-DatabaseConnection {
    param(
        [Parameter(Mandatory=$true)]
        [string]$DbHost,

        [Parameter(Mandatory=$true)]
        [string]$DbPort,

        [Parameter(Mandatory=$true)]
        [string]$DbUsername,

        [Parameter(Mandatory=$true)]
        [string]$DbPassword,

        [Parameter(Mandatory=$true)]
        [string]$DbDatabase
    )

    Write-Host "Testing database connection..." -ForegroundColor Cyan

    # Method 1: Use MySQL command-line tool
    if ($Global:MySQLExecutable) {
        try {
            $TestQuery = "SELECT 1 as connection_test;"
            $Result = & $Global:MySQLExecutable -h $DbHost -P $DbPort -u $DbUsername -p$DbPassword -e $TestQuery $DbDatabase 2>&1

            if ($LASTEXITCODE -eq 0) {
                Write-Host "Database connection successful (MySQL client test)" -ForegroundColor Green
                return $true
            }
        }
        catch {
            Write-Host "MySQL client test failed: $($_.Exception.Message)" -ForegroundColor Yellow
        }
    }

    # Method 2: Test network connectivity (port availability)
    try {
        $TcpClient = New-Object System.Net.Sockets.TcpClient
        $Connect = $TcpClient.BeginConnect($DbHost, $DbPort, $null, $null)
        $Wait = $Connect.AsyncWaitHandle.WaitOne(5000, $false)

        if ($Wait) {
            $TcpClient.EndConnect($Connect)
            $TcpClient.Close()
            Write-Host "Database port connection successful: ${DbHost}:${DbPort}" -ForegroundColor Green
            return $true
        }
        else {
            Write-Host "Database port connection timeout: ${DbHost}:${DbPort}" -ForegroundColor Red
            return $false
        }
    }
    catch {
        Write-Host "Database network connection failed: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }

    Write-Host "All connection tests failed" -ForegroundColor Red
    return $false
}

# Database backup (supports multiple methods)
function Backup-Database {
    param(
        [Parameter(Mandatory=$true)]
        [string]$DbHost,

        [Parameter(Mandatory=$true)]
        [string]$DbPort,

        [Parameter(Mandatory=$true)]
        [string]$DbUsername,

        [Parameter(Mandatory=$true)]
        [string]$DbPassword,

        [Parameter(Mandatory=$true)]
        [string]$DbDatabase,

        [Parameter(Mandatory=$true)]
        [string]$BackupFile
    )

    Write-Host "Starting database backup..." -ForegroundColor Cyan
    Write-Host "Backup file: $BackupFile" -ForegroundColor Yellow

    # Ensure backup directory exists
    $BackupDir = Split-Path -Parent $BackupFile
    if (!(Test-Path $BackupDir)) {
        New-Item -ItemType Directory -Path $BackupDir -Force | Out-Null
        Write-Host "Created backup directory: $BackupDir" -ForegroundColor Green
    }

    # Method 1: Use mysqldump tool
    if ($Global:MysqldumpExecutable) {
        try {
            $DumpArgs = @(
                "-h", $DbHost,
                "-P", $DbPort,
                "-u", $DbUsername,
                "-p$DbPassword",
                "--single-transaction",
                "--routines",
                "--triggers",
                "--events",
                "--add-drop-table",
                "--create-options",
                "--default-character-set=utf8mb4",
                $DbDatabase
            )

            Write-Host "Using mysqldump for backup..." -ForegroundColor Green
            & $Global:MysqldumpExecutable $DumpArgs | Out-File -FilePath $BackupFile -Encoding UTF8

            if ($LASTEXITCODE -eq 0) {
                $FileSize = (Get-Item $BackupFile).Length / 1MB
                Write-Host "Backup completed! File size: $($FileSize.ToString('F2'))MB" -ForegroundColor Green
                return $true
            }
        }
        catch {
            Write-Host "mysqldump backup failed: $($_.Exception.Message)" -ForegroundColor Red
        }
    }

    # Method 2: Create backup record file (for demonstration)
    try {
        Write-Host "Creating backup record file..." -ForegroundColor Yellow

        $BackupRecord = @"
# ============================================================
# IOE-DREAM Database Backup Record
# ============================================================
Backup Time: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
Backup Target: $DbDatabase
Database Server: ${DbHost}:${DbPort}
User: $DbUsername
Backup File: $BackupFile
Status: Backup request created (manual execution required)
# ============================================================
# MySQL command-line tools not detected in system PATH
# Please manually execute the following command:
# mysqldump -h $DbHost -P $DbPort -u $DbUsername -p$DbPassword --single-transaction --routines --triggers --events $DbDatabase > "$BackupFile"
# ============================================================
"@

        $BackupRecord | Out-File -FilePath "$BackupFile.backup-info.txt" -Encoding UTF8
        Write-Host "Created backup record file, please execute backup manually" -ForegroundColor Yellow
        return $false
    }
    catch {
        Write-Host "Failed to create backup record: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

# Execute SQL migration script
function Invoke-SqlMigration {
    param(
        [Parameter(Mandatory=$true)]
        [string]$DbHost,

        [Parameter(Mandatory=$true)]
        [string]$DbPort,

        [Parameter(Mandatory=$true)]
        [string]$DbUsername,

        [Parameter(Mandatory=$true)]
        [string]$DbPassword,

        [Parameter(Mandatory=$true)]
        [string]$DbDatabase,

        [Parameter(Mandatory=$true)]
        [string]$SqlFile
    )

    Write-Host "Executing SQL migration script..." -ForegroundColor Cyan
    Write-Host "Script file: $SqlFile" -ForegroundColor Yellow

    if (!(Test-Path $SqlFile)) {
        Write-Host "SQL file does not exist: $SqlFile" -ForegroundColor Red
        return $false
    }

    # Method 1: Use MySQL client to execute
    if ($Global:MySQLExecutable) {
        try {
            $SqlContent = Get-Content $SqlFile -Raw
          $Result = & $Global:MySQLExecutable -h $DbHost -P $DbPort -u $DbUsername -p$DbPassword $DbDatabase -e $SqlContent 2>&1

            if ($LASTEXITCODE -eq 0) {
                Write-Host "SQL script execution successful" -ForegroundColor Green
                return $true
            }
            else {
                Write-Host "SQL script execution failed: $Result" -ForegroundColor Red
                return $false
            }
        }
        catch {
            Write-Host "SQL script execution exception: $($_.Exception.Message)" -ForegroundColor Red
            return $false
        }
    }
    else {
        Write-Host "MySQL client not found, cannot execute SQL script" -ForegroundColor Yellow
        Write-Host "Please manually execute: mysql -h ${DbHost} -P ${DbPort} -u ${DbUsername} -p${DbPassword} ${DbDatabase} -e `"$(Get-Content $SqlFile -Raw)`"" -ForegroundColor Cyan
        return $false
    }
}

# Get database version information
function Get-DatabaseVersion {
    param(
        [Parameter(Mandatory=$true)]
        [string]$DbHost,

        [Parameter(Mandatory=$true)]
        [string]$DbPort,

        [Parameter(Mandatory=$true)]
        [string]$DbUsername,

        [Parameter(Mandatory=$true)]
        [string]$DbPassword,

        [string]$Database = "mysql"
    )

    Write-Host "Getting database version information..." -ForegroundColor Cyan

    if ($Global:MySQLExecutable) {
        try {
            $VersionQuery = "SELECT VERSION() as mysql_version;"
            $Result = & $Global:MySQLExecutable -h $DbHost -P $DbPort -u $DbUsername -p$DbPassword -e $VersionQuery $Database 2>&1

            if ($LASTEXITCODE -eq 0) {
                $Version = $Result -split '\n' | Select-Object -Skip 1 | Where-Object { $_.Trim() -ne "" }
                Write-Host "MySQL version: $($Version.Trim())" -ForegroundColor Green
                return $Version.Trim()
            }
        }
        catch {
            Write-Host "Failed to get database version: $($_.Exception.Message)" -ForegroundColor Yellow
        }
    }

    return "Unknown"
}

# Validate database health
function Test-DatabaseHealth {
    param(
        [Parameter(Mandatory=$true)]
        [string]$DbHost,

        [Parameter(Mandatory=$true)]
        [string]$DbPort,

        [Parameter(Mandatory=$true)]
        [string]$DbUsername,

        [Parameter(Mandatory=$true)]
        [string]$DbPassword,

        [Parameter(Mandatory=$true)]
        [string]$DbDatabase
    )

    Write-Host "Database health check..." -ForegroundColor Cyan

    $HealthStatus = @{
        Connection = $false
        Version = "Unknown"
        Tables = 0
        Size = "Unknown"
        FlywayTable = $false
    }

    # Test connection
    $HealthStatus.Connection = Test-DatabaseConnection -DbHost $DbHost -DbPort $DbPort -DbUsername $DbUsername -DbPassword $DbPassword -DbDatabase $DbDatabase

    if ($HealthStatus.Connection) {
        # Get version
        $HealthStatus.Version = Get-DatabaseVersion -DbHost $DbHost -DbPort $DbPort -DbUsername $DbUsername -DbPassword $DbPassword -Database $DbDatabase

        # Check Flyway table
        if ($Global:MySQLExecutable) {
            try {
                $FlywayCheck = & $Global:MySQLExecutable -h $DbHost -P $DbPort -u $DbUsername -p$DbPassword -e "SHOW TABLES LIKE 'flyway_schema_history'" $DbDatabase 2>&1
                if ($FlywayCheck -match "flyway_schema_history") {
                    $HealthStatus.FlywayTable = $true
                    Write-Host "Flyway history table exists" -ForegroundColor Green
                }
                else {
                    Write-Host "Flyway history table does not exist" -ForegroundColor Yellow
                }
            }
            catch {
                Write-Host "Cannot check Flyway table: $($_.Exception.Message)" -ForegroundColor Yellow
            }
        }
    }

    return $HealthStatus
}

# Auto-initialization (called when module is imported)
Initialize-MySQLPaths

# Note: Functions are available in current scope