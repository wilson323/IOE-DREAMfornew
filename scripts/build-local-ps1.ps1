# IOE-DREAM Local Maven PowerShell Builder
# 使用您的本地Maven: D:\IOE-DREAM\apache-maven-3.9.11

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "IOE-DREAM Local Maven PowerShell Builder" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Time: $(Get-Date)" -ForegroundColor Gray
Write-Host "Using Local Maven: D:\IOE-DREAM\apache-maven-3.9.11" -ForegroundColor Yellow
Write-Host ""

# 设置本地Maven路径
$env:MAVEN_HOME = "D:\IOE-DREAM\apache-maven-3.9.11"
$env:PATH = "$env:MAVEN_HOME\bin;$env:PATH"
$localMaven = "D:\IOE-DREAM\apache-maven-3.9.11\bin\mvn.cmd"

Write-Host "Step 1: Environment Check..." -ForegroundColor Yellow

try {
    $javaVersion = & java -version 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "OK: Java Environment" -ForegroundColor Green
    } else {
        Write-Host "ERROR: Java not found" -ForegroundColor Red
        Read-Host "Press Enter to exit"
        exit 1
    }
} catch {
    Write-Host "ERROR: Java not found" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

if (-not (Test-Path $localMaven)) {
    Write-Host "ERROR: Local Maven not found at $localMaven" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}
Write-Host "OK: Local Maven found" -ForegroundColor Green

Write-Host "`nStep 2: Project Check..." -ForegroundColor Yellow

if (-not (Test-Path "microservices\pom.xml")) {
    Write-Host "ERROR: Backend project not found" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}
Write-Host "OK: Backend project found" -ForegroundColor Green

Write-Host "`nStep 3: Using Local Maven Environment..." -ForegroundColor Yellow
Write-Host "Maven Command: $localMaven" -ForegroundColor Gray
Write-Host "Maven Home: $env:MAVEN_HOME" -ForegroundColor Gray

try {
    Set-Location "microservices"

    Write-Host "Cleaning previous builds..." -ForegroundColor Blue
    & $localMaven clean
    if ($LASTEXITCODE -ne 0) {
        throw "Local Maven clean failed"
    }
    Write-Host "OK: Clean completed" -ForegroundColor Green

    Write-Host "Building common module first..." -ForegroundColor Blue
    Set-Location "microservices-common"
    & $localMaven install -DskipTests
    if ($LASTEXITCODE -ne 0) {
        throw "Common module build failed"
    }
    Write-Host "OK: Common module built successfully" -ForegroundColor Green
    Set-Location ".."

    Write-Host "Building all microservices..." -ForegroundColor Blue
    & $localMaven compile -DskipTests
    if ($LASTEXITCODE -ne 0) {
        throw "Compilation failed"
    }
    Write-Host "OK: Compilation successful" -ForegroundColor Green

    Write-Host "Packaging applications..." -ForegroundColor Blue
    & $localMaven package -DskipTests
    if ($LASTEXITCODE -ne 0) {
        throw "Packaging failed"
    }
    Write-Host "OK: Packaging successful" -ForegroundColor Green

    Set-Location ".."

} catch {
    Write-Host "BUILD ERROR: $($_.Exception.Message)" -ForegroundColor Red
    Set-Location ".."
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host "`n========================================" -ForegroundColor Green
Write-Host "BUILD SUCCESSFUL!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

Write-Host "Generated JAR files:" -ForegroundColor Yellow
Get-ChildItem "microservices\ioedream-*" -Directory | ForEach-Object {
    $jarFiles = Get-ChildItem "$($_.FullName)\target\*.jar" -ErrorAction SilentlyContinue
    if ($jarFiles) {
        Write-Host "  - $($_.Name).jar" -ForegroundColor Gray
    }
}

Write-Host "`nBuild completed at: $(Get-Date)" -ForegroundColor Gray
Write-Host "Next: Run start-local.bat to start services" -ForegroundColor Gray
Write-Host ""

Read-Host "Press Enter to exit"