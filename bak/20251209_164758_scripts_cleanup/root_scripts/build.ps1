# IOE-DREAM PowerShell Builder
# Uses PowerShell for better UTF-8 support

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "IOE-DREAM PowerShell Builder" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Time: $(Get-Date)" -ForegroundColor Gray
Write-Host ""

# Step 1: Environment Check
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

try {
    $mavenVersion = & mvn -version 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "OK: Maven Environment" -ForegroundColor Green
    } else {
        Write-Host "ERROR: Maven not found" -ForegroundColor Red
        Read-Host "Press Enter to exit"
        exit 1
    }
} catch {
    Write-Host "ERROR: Maven not found" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

# Step 2: Project Check
Write-Host "`nStep 2: Project Check..." -ForegroundColor Yellow

if (-not (Test-Path "microservices\pom.xml")) {
    Write-Host "ERROR: Backend project not found" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}
Write-Host "OK: Backend project found" -ForegroundColor Green

if (-not (Test-Path "microservices\microservices-common\pom.xml")) {
    Write-Host "ERROR: Common module not found" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}
Write-Host "OK: Common module found" -ForegroundColor Green

# Step 3: Set UTF-8 Environment
Write-Host "`nStep 3: Setting UTF-8 Environment..." -ForegroundColor Yellow
$env:JAVA_TOOL_OPTIONS = "-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8"
$env:MAVEN_OPTS = "-Xmx1024m -Dfile.encoding=UTF-8"
$env:LC_ALL = "en_US.UTF-8"
$env:LANG = "en_US.UTF-8"

Write-Host "Environment variables set for UTF-8" -ForegroundColor Green

# Step 4: Build Process
Write-Host "`nStep 4: Building..." -ForegroundColor Yellow

try {
    Set-Location "microservices"

    Write-Host "Cleaning previous builds..." -ForegroundColor Blue
    & mvn clean -q -Duser.language=en -Duser.country=US
    if ($LASTEXITCODE -ne 0) {
        throw "Maven clean failed"
    }
    Write-Host "OK: Clean completed" -ForegroundColor Green

    Write-Host "Building common module first..." -ForegroundColor Blue
    Set-Location "microservices-common"
    & mvn install -DskipTests -q -Duser.language=en -Duser.country=US
    if ($LASTEXITCODE -ne 0) {
        throw "Common module build failed"
    }
    Write-Host "OK: Common module built successfully" -ForegroundColor Green
    Set-Location ".."

    Write-Host "Building all microservices..." -ForegroundColor Blue
    & mvn compile -DskipTests -q -Duser.language=en -Duser.country=US
    if ($LASTEXITCODE -ne 0) {
        throw "Compilation failed"
    }
    Write-Host "OK: Compilation successful" -ForegroundColor Green

    Write-Host "Packaging applications..." -ForegroundColor Blue
    & mvn package -DskipTests -q -Duser.language=en -Duser.country=US
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

# Success
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
Write-Host "Next: Run start.ps1 to start services" -ForegroundColor Gray
Write-Host ""

Read-Host "Press Enter to exit"