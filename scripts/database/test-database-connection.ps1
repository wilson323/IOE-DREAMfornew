# ============================================================
# IOE-DREAM Database Connection Test Script
# Version: v1.0.0
# Author: IOE-DREAM Team
# Created: 2025-12-15
# Purpose: Test database connection and utilities
# ============================================================

# Set execution policy
Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

# Import utilities
$ScriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$UtilitiesPath = Join-Path $ScriptRoot "database-utilities-en.ps1"

if (Test-Path $UtilitiesPath) {
    try {
        . $UtilitiesPath
        Write-Host "Database utilities loaded successfully" -ForegroundColor Green
    }
    catch {
        Write-Host "Failed to load database utilities: $($_.Exception.Message)" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "Database utilities file not found: $UtilitiesPath" -ForegroundColor Red
    exit 1
}

# Test database connection
function Test-Connection {
    Write-Host "`n=== Database Connection Test ===" -ForegroundColor Cyan

    # Default connection parameters (can be overridden by environment variables)
    $DbHost = if ($env:MYSQL_HOST) { $env:MYSQL_HOST } else { "127.0.0.1" }
    $DbPort = if ($env:MYSQL_PORT) { $env:MYSQL_PORT } else { "3306" }
    $DbUsername = if ($env:MYSQL_USERNAME) { $env:MYSQL_USERNAME } else { "root" }
    $DbPassword = if ($env:MYSQL_PASSWORD) { $env:MYSQL_PASSWORD } else { "123456" }
    $DbDatabase = if ($env:MYSQL_DATABASE) { $env:MYSQL_DATABASE } else { "ioedream" }

    Write-Host "Connection parameters:" -ForegroundColor Yellow
    Write-Host "  Host: $DbHost" -ForegroundColor White
    Write-Host "  Port: $DbPort" -ForegroundColor White
    Write-Host "  Database: $DbDatabase" -ForegroundColor White
    Write-Host "  Username: $DbUsername" -ForegroundColor White
    Write-Host ""

    # Test connection
    $ConnectionResult = Test-DatabaseConnection -DbHost $DbHost -DbPort $DbPort -DbUsername $DbUsername -DbPassword $DbPassword -DbDatabase $DbDatabase

    if ($ConnectionResult) {
        Write-Host "SUCCESS: Database connection test passed!" -ForegroundColor Green

        # Additional health checks
        Write-Host "`n=== Database Health Check ===" -ForegroundColor Cyan
        $HealthStatus = Test-DatabaseHealth -DbHost $DbHost -DbPort $DbPort -DbUsername $DbUsername -DbPassword $DbPassword -DbDatabase $DbDatabase

        Write-Host "Health Status Summary:" -ForegroundColor Yellow
        Write-Host "  Connection: $(if ($HealthStatus.Connection) { 'OK' } else { 'FAILED' })" -ForegroundColor $(if ($HealthStatus.Connection) { 'Green' } else { 'Red' })
        Write-Host "  Version: $($HealthStatus.Version)" -ForegroundColor White
        Write-Host "  Flyway Table: $(if ($HealthStatus.FlywayTable) { 'EXISTS' } else { 'NOT FOUND' })" -ForegroundColor $(if ($HealthStatus.FlywayTable) { 'Green' } else { 'Yellow' })

        return $true
    } else {
        Write-Host "FAILED: Database connection test failed!" -ForegroundColor Red
        Write-Host "`nTroubleshooting tips:" -ForegroundColor Yellow
        Write-Host "  1. Check if MySQL service is running" -ForegroundColor White
        Write-Host "  2. Verify database connection parameters" -ForegroundColor White
        Write-Host "  3. Ensure user has sufficient privileges" -ForegroundColor White
        Write-Host "  4. Check if database exists" -ForegroundColor White

        return $false
    }
}

# Test backup functionality
function Test-Backup {
    Write-Host "`n=== Database Backup Test ===" -ForegroundColor Cyan

    $DbHost = if ($env:MYSQL_HOST) { $env:MYSQL_HOST } else { "127.0.0.1" }
    $DbPort = if ($env:MYSQL_PORT) { $env:MYSQL_PORT } else { "3306" }
    $DbUsername = if ($env:MYSQL_USERNAME) { $env:MYSQL_USERNAME } else { "root" }
    $DbPassword = if ($env:MYSQL_PASSWORD) { $env:MYSQL_PASSWORD } else { "123456" }
    $DbDatabase = if ($env:MYSQL_DATABASE) { $env:MYSQL_DATABASE } else { "ioedream" }

    $BackupFile = "$ScriptRoot\test-backup-$(Get-Date -Format 'yyyyMMdd-HHmmss').sql"

    Write-Host "Testing backup functionality..." -ForegroundColor Yellow
    $BackupResult = Backup-Database -DbHost $DbHost -DbPort $DbPort -DbUsername $DbUsername -DbPassword $DbPassword -DbDatabase $DbDatabase -BackupFile $BackupFile

    if ($BackupResult) {
        Write-Host "SUCCESS: Database backup test passed!" -ForegroundColor Green
        Write-Host "Backup file: $BackupFile" -ForegroundColor White
    } else {
        Write-Host "INFO: Backup test created backup record file" -ForegroundColor Yellow
        Write-Host "Please manually execute the backup as indicated in the record file" -ForegroundColor White
    }
}

# Main execution
try {
    Write-Host "IOE-DREAM Database Connection Test" -ForegroundColor Green
    Write-Host "Started at: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Cyan
    Write-Host "Script root: $ScriptRoot" -ForegroundColor Cyan

    # Run tests
    $ConnectionSuccess = Test-Connection
    Test-Backup

    Write-Host "`n=== Test Summary ===" -ForegroundColor Cyan
    if ($ConnectionSuccess) {
        Write-Host "Database connection test: PASSED" -ForegroundColor Green
        Write-Host "Database migration tools are ready for use" -ForegroundColor Green
        exit 0
    } else {
        Write-Host "Database connection test: FAILED" -ForegroundColor Red
        Write-Host "Please resolve connection issues before running migration" -ForegroundColor Red
        exit 1
    }

} catch {
    Write-Host "Test failed with error: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Stack trace: $($_.ScriptStackTrace)" -ForegroundColor Red
    exit 2
}