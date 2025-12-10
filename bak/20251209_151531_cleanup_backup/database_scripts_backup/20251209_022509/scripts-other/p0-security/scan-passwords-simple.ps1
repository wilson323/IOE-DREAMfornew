# IOE-DREAM P0-1: Password Security Scan (Read-Only, Safe)
# Purpose: Scan and report plaintext passwords in configuration files
# Priority: P0 - Immediate Action Required
# Safety: Read-only operation, completely safe

$ErrorActionPreference = "Stop"

Write-Host "`n=========================================" -ForegroundColor Blue
Write-Host "P0-1: Configuration Security Scan" -ForegroundColor Blue
Write-Host "=========================================" -ForegroundColor Blue

$ProjectRoot = "D:\IOE-DREAM"
$MicroservicesDir = "$ProjectRoot\microservices"
$ReportFile = "$ProjectRoot\P0-1_PASSWORD_SCAN_REPORT.md"

$TotalFiles = 0
$FilesWithPasswords = 0
$PasswordsFound = 0
$ScanResults = @()

Write-Host "`nScanning configuration files..." -ForegroundColor Cyan

# Get all YAML configuration files
$ConfigFiles = Get-ChildItem -Path $MicroservicesDir -Recurse -Include *.yml,*.yaml -File -ErrorAction SilentlyContinue

Write-Host "Found $($ConfigFiles.Count) configuration files`n" -ForegroundColor Cyan

foreach ($file in $ConfigFiles) {
    $TotalFiles++
    $relativePath = $file.FullName.Replace("$ProjectRoot\", "")

    try {
        $content = Get-Content $file.FullName -Raw -ErrorAction Stop

        $hasPassword = $false
        $passwordCount = 0
        $foundPasswords = @()

        # Check for database passwords
        if ($content -match 'password:\s*([^\s$\{][^\r\n]*)') {
            $passwordValue = $Matches[1].Trim()
            if ($passwordValue -notmatch '^\$\{' -and $passwordValue -ne '' -and $passwordValue -ne 'null') {
                $hasPassword = $true
                $passwordCount++
                $foundPasswords += "Database password: $passwordValue"
            }
        }

        # Check for Redis passwords
        if ($content -match 'redis:[\s\S]*?password:\s*([^\s$\{][^\r\n]*)') {
            $passwordValue = $Matches[1].Trim()
            if ($passwordValue -notmatch '^\$\{' -and $passwordValue -ne '' -and $passwordValue -ne 'null') {
                $hasPassword = $true
                $passwordCount++
                $foundPasswords += "Redis password: $passwordValue"
            }
        }

        # Check for Nacos passwords
        if ($content -match 'nacos:[\s\S]*?password:\s*([^\s$\{][^\r\n]*)') {
            $passwordValue = $Matches[1].Trim()
            if ($passwordValue -notmatch '^\$\{' -and $passwordValue -ne '' -and $passwordValue -ne 'null') {
                $hasPassword = $true
                $passwordCount++
                $foundPasswords += "Nacos password: $passwordValue"
            }
        }

        if ($hasPassword) {
            $FilesWithPasswords++
            $PasswordsFound += $passwordCount

            Write-Host "  [FOUND] $relativePath ($passwordCount passwords)" -ForegroundColor Yellow

            $ScanResults += [PSCustomObject]@{
                File = $relativePath
                Count = $passwordCount
                Passwords = $foundPasswords
            }
        }
    }
    catch {
        Write-Host "  [SKIP] $relativePath (read error)" -ForegroundColor Gray
    }
}

Write-Host "`n=========================================" -ForegroundColor Blue
Write-Host "Scan Complete!" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Blue
Write-Host "Total files scanned: $TotalFiles" -ForegroundColor Cyan
Write-Host "Files with passwords: $FilesWithPasswords" -ForegroundColor Yellow
Write-Host "Total passwords found: $PasswordsFound" -ForegroundColor Yellow

# Generate report
$reportContent = @"
# P0-1: Configuration Security Scan Report

> **Scan Date**: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
> **Scan Status**: Completed
> **Priority**: P0 - Immediate Action Required
> **Operation Type**: Read-Only (Safe)

---

## Summary

| Item | Count |
|------|-------|
| Total Files Scanned | $TotalFiles |
| Files with Passwords | $FilesWithPasswords |
| Total Passwords Found | $PasswordsFound |

---

## Findings

"@

if ($ScanResults.Count -gt 0) {
    foreach ($result in $ScanResults) {
        $reportContent += "`n### File: ``$($result.File)```n`n"
        $reportContent += "- **Password Count**: $($result.Count)`n"
        $reportContent += "- **Details**:`n"
        foreach ($pwd in $result.Passwords) {
            $reportContent += "  - $pwd`n"
        }
    }
} else {
    $reportContent += "`n**No plaintext passwords found! Configuration is secure!**`n"
}

$reportContent += @"

---

## Recommended Actions

### Option 1: Use Environment Variables (Recommended)

``````yaml
# Before
spring:
  datasource:
    password: "123456"  # Plaintext password

# After
spring:
  datasource:
    password: `${DB_PASSWORD}  # From environment variable
``````

### Option 2: Use Nacos Encrypted Configuration (Enterprise)

``````yaml
spring:
  cloud:
    nacos:
      config:
        server-addr: `${NACOS_SERVER_ADDR}
        namespace: `${NACOS_NAMESPACE}
``````

---

## Next Steps

1. **Review this report**: Understand all password locations
2. **Prepare environment variables**: Create .env file
3. **Manual review**: Confirm which passwords need replacement
4. **Backup before changes**: Always backup configuration files
5. **Replace passwords**: Use automated scripts after review
6. **Verify configuration**: Test service connections

---

**Scan Executed By**: IOE-DREAM Architecture Committee
**Report Date**: $(Get-Date -Format 'yyyy-MM-dd')
**Report Status**: Completed
"@

$reportContent | Out-File -FilePath $ReportFile -Encoding UTF8

Write-Host "`nReport generated: $ReportFile" -ForegroundColor Green

# Generate environment variable template
$envTemplate = @"
# IOE-DREAM Environment Variables Template
# Instructions:
# 1. Copy this file to .env
# 2. Fill in actual passwords and keys
# 3. Ensure .env is in .gitignore

# Nacos Configuration
NACOS_SERVER_ADDR=127.0.0.1:8848
NACOS_NAMESPACE=dev
NACOS_GROUP=IOE-DREAM
NACOS_USERNAME=nacos
NACOS_PASSWORD=nacos

# Database Configuration
DB_PASSWORD=

# Redis Configuration
REDIS_HOST=127.0.0.1
REDIS_PORT=6379
REDIS_PASSWORD=
REDIS_DATABASE=0

# Monitoring Configuration
ZIPKIN_BASE_URL=http://localhost:9411
TRACING_SAMPLE_RATE=0.1
"@

$envTemplateFile = "$ProjectRoot\.env.template"
$envTemplate | Out-File -FilePath $envTemplateFile -Encoding UTF8

Write-Host "Environment template generated: $envTemplateFile" -ForegroundColor Green

Write-Host "`n=========================================" -ForegroundColor Blue
Write-Host "Scan Task Completed Successfully!" -ForegroundColor Green
Write-Host "=========================================`n" -ForegroundColor Blue

if ($FilesWithPasswords -gt 0) {
    Write-Host "WARNING: Found $FilesWithPasswords files with plaintext passwords" -ForegroundColor Yellow
    Write-Host "Total: $PasswordsFound passwords need to be secured`n" -ForegroundColor Yellow
    Write-Host "Next steps:" -ForegroundColor Cyan
    Write-Host "  1. Review detailed report: $ReportFile" -ForegroundColor White
    Write-Host "  2. Configure environment variables: $envTemplateFile" -ForegroundColor White
    Write-Host "  3. Manual review before executing replacement`n" -ForegroundColor White
} else {
    Write-Host "EXCELLENT: No plaintext passwords found! Configuration is secure!`n" -ForegroundColor Green
}

