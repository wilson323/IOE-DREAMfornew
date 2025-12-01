# Fix Compilation Environment Script - Windows PowerShell
# Purpose: Fix Maven compilation environment to ensure UTF-8 encoding

$ErrorActionPreference = "Stop"
$ProjectRoot = "D:\IOE-DREAM"
$BackendPath = "$ProjectRoot\smart-admin-api-java17-springboot3"

Write-Host ""
Write-Host "=== Fix Compilation Environment ===" -ForegroundColor Cyan
Write-Host "Time: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Cyan
Write-Host ""

# Step 1: Check Java environment
Write-Host "Step 1: Check Java environment..." -ForegroundColor Blue
try {
    $javaOutput = & java -version 2>&1 | Out-String
    if ($LASTEXITCODE -eq 0 -or $javaOutput -match "version") {
        $javaVersionMatch = $javaOutput | Select-String "version\s+[\d\.]+"
        if ($javaVersionMatch) {
            $javaVersion = $javaVersionMatch.Matches[0].Value
            Write-Host "  [PASS] Java: $javaVersion" -ForegroundColor Green
        } else {
            Write-Host "  [PASS] Java: Installed (version check skipped)" -ForegroundColor Green
        }
    } else {
        throw "Java not installed or not in PATH"
    }
} catch {
    Write-Host "  [FAIL] Java environment check failed: $_" -ForegroundColor Red
    exit 1
}

# Step 2: Check Maven environment
Write-Host ""
Write-Host "Step 2: Check Maven environment..." -ForegroundColor Blue
try {
    $mavenOutput = mvn -version 2>&1 | Out-String
    if ($mavenOutput -match "Apache Maven") {
        $mavenVersion = ($mavenOutput | Select-String "Apache Maven \d+\.\d+\.\d+").Matches[0].Value
        Write-Host "  [PASS] Maven: $mavenVersion" -ForegroundColor Green
    } else {
        throw "Maven not installed or not in PATH"
    }
} catch {
    Write-Host "  [FAIL] Maven environment check failed: $_" -ForegroundColor Red
    exit 1
}

# Step 3: Set UTF-8 encoding environment variables
Write-Host ""
Write-Host "Step 3: Set UTF-8 encoding environment variables..." -ForegroundColor Blue
$env:MAVEN_OPTS = "-Dfile.encoding=UTF-8 -Duser.language=zh -Duser.region=CN"
$env:JAVA_TOOL_OPTIONS = "-Dfile.encoding=UTF-8"
$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
Write-Host "  [DONE] UTF-8 encoding environment variables set" -ForegroundColor Green
Write-Host "    MAVEN_OPTS: $env:MAVEN_OPTS" -ForegroundColor Gray
Write-Host "    JAVA_TOOL_OPTIONS: $env:JAVA_TOOL_OPTIONS" -ForegroundColor Gray

# Step 4: Try compilation test
Write-Host ""
Write-Host "Step 4: Try compilation test (sa-base module only)..." -ForegroundColor Blue
Set-Location $BackendPath
try {
    # Clean first
    Write-Host "  Executing: mvn clean -pl sa-base -am" -ForegroundColor Gray
    $null = mvn clean -pl sa-base -am -Dfile.encoding=UTF-8 2>&1
    
    # Try compile
    Write-Host "  Executing: mvn compile -pl sa-base -am" -ForegroundColor Gray
    $compileOutput = mvn compile -pl sa-base -am -Dfile.encoding=UTF-8 2>&1 | Out-String
    
    if ($compileOutput -match "BUILD SUCCESS") {
        Write-Host "  [PASS] Compilation successful!" -ForegroundColor Green
        $compileSuccess = $true
    } elseif ($compileOutput -match "BUILD FAILURE" -or $compileOutput -match "ERROR") {
        Write-Host "  [FAIL] Compilation failed, error messages:" -ForegroundColor Red
        # Extract error messages (first 10 lines)
        $errors = $compileOutput -split "`n" | Where-Object { $_ -match "ERROR" } | Select-Object -First 10
        $errors | ForEach-Object { Write-Host "    $_" -ForegroundColor Red }
        $compileSuccess = $false
    } else {
        Write-Host "  [WARN] Compilation result uncertain, please check manually" -ForegroundColor Yellow
        $compileSuccess = $false
    }
} catch {
    Write-Host "  [FAIL] Compilation process error: $_" -ForegroundColor Red
    $compileSuccess = $false
}

# Step 5: Generate environment configuration documentation
Write-Host ""
Write-Host "Step 5: Generate environment configuration documentation..." -ForegroundColor Blue
$envConfigContent = @"
# Maven Compilation Environment Configuration
# Generated: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')

## Windows PowerShell Environment Variable Setup

Execute the following commands in PowerShell to set UTF-8 encoding:

```powershell
# Set Maven compilation encoding
`$env:MAVEN_OPTS = "-Dfile.encoding=UTF-8 -Duser.language=zh -Duser.region=CN"

# Set Java compilation encoding
`$env:JAVA_TOOL_OPTIONS = "-Dfile.encoding=UTF-8"

# Set console output encoding
`$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
```

## Compilation Command

Use the following command to compile the project (ensure correct encoding):

```powershell
cd D:\IOE-DREAM\smart-admin-api-java17-springboot3
mvn clean compile -Dfile.encoding=UTF-8
```

## Permanent Environment Variable Setup (Optional)

If you need to set permanently, add to system environment variables:
- `MAVEN_OPTS` = `-Dfile.encoding=UTF-8 -Duser.language=zh -Duser.region=CN`
- `JAVA_TOOL_OPTIONS` = `-Dfile.encoding=UTF-8`

Or create `%USERPROFILE%\.m2\settings.xml` file:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
          http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <profiles>
        <profile>
            <id>utf-8</id>
            <properties>
                <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
                <maven.compiler.encoding>UTF-8</maven.compiler.encoding>
            </properties>
        </profile>
    </profiles>
    <activeProfiles>
        <activeProfile>utf-8</activeProfile>
    </activeProfiles>
</settings>
```
"@

$envConfigFile = "$ProjectRoot\docs\COMPILATION_ENV_SETUP.md"
$envConfigDir = Split-Path $envConfigFile -Parent
if (-not (Test-Path $envConfigDir)) {
    New-Item -ItemType Directory -Path $envConfigDir -Force | Out-Null
}
$envConfigContent | Out-File -FilePath $envConfigFile -Encoding UTF8
Write-Host "  [DONE] Environment configuration documentation saved to: $envConfigFile" -ForegroundColor Green

# Summary
Write-Host ""
Write-Host "=== Fix Complete ===" -ForegroundColor Cyan
if ($compileSuccess) {
    Write-Host "Compilation Status: SUCCESS" -ForegroundColor Green
    Write-Host ""
    Write-Host "Tip: To ensure subsequent compilations work correctly, execute the following commands before each compilation:" -ForegroundColor Yellow
    Write-Host "  `$env:MAVEN_OPTS = `"-Dfile.encoding=UTF-8`"" -ForegroundColor Gray
    Write-Host "  [Console]::OutputEncoding = [System.Text.Encoding]::UTF8" -ForegroundColor Gray
} else {
    Write-Host "Compilation Status: FAILED (please check error messages)" -ForegroundColor Red
    Write-Host ""
    Write-Host "Tip: Compilation failure may be due to code errors, please check specific compilation error messages" -ForegroundColor Yellow
    Write-Host "Environment variables have been set, please try recompiling" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Detailed configuration documentation: $envConfigFile" -ForegroundColor Cyan
Write-Host ""

exit $(if ($compileSuccess) { 0 } else { 1 })

