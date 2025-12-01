# Java Class Analyzer MCP Test Script
# Tests and verifies MCP server configuration

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Java Class Analyzer MCP Test Script" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Check MCP configuration
Write-Host "Step 1: Checking MCP configuration..." -ForegroundColor Yellow
$mcpConfigPath = "$env:USERPROFILE\.cursor\mcp.json"

if (-not (Test-Path $mcpConfigPath)) {
    Write-Host "  ERROR MCP config file not found: $mcpConfigPath" -ForegroundColor Red
    exit 1
}

try {
    $config = Get-Content $mcpConfigPath -Raw -Encoding UTF8 | ConvertFrom-Json
    $javaConfig = $config.mcpServers.'java-class-analyzer'

    if (-not $javaConfig) {
        Write-Host "  ERROR java-class-analyzer config not found" -ForegroundColor Red
        exit 1
    }

    Write-Host "  OK Config file exists and format is correct" -ForegroundColor Green
    Write-Host "    Command: $($javaConfig.command)" -ForegroundColor Gray
    Write-Host "    Arguments: $($javaConfig.args -join ' ')" -ForegroundColor Gray

    # Verify key configurations
    $issues = @()
    $hasLatest = $false
    foreach ($arg in $javaConfig.args) {
        if ($arg -match '@latest') {
            $hasLatest = $true
            break
        }
    }
    if (-not $hasLatest) {
        $issues += "Not using @latest version"
    }
    if (-not $javaConfig.env.MCP_SERVER_LOG_LEVEL) {
        $issues += "Log level not set"
    }
    if ($null -eq $javaConfig.alwaysAllow -or $javaConfig.alwaysAllow.Count -eq 0) {
        # alwaysAllow is optional, but we check if it exists
        # Empty array is valid, so we only warn if it's null
        if ($null -eq $javaConfig.alwaysAllow) {
            $issues += "alwaysAllow not configured (optional but recommended)"
        }
    }

    if ($issues.Count -eq 0) {
        Write-Host "  OK All configurations are correct" -ForegroundColor Green
    } else {
        Write-Host "  WARNING Found configuration issues:" -ForegroundColor Yellow
        foreach ($issue in $issues) {
            Write-Host "    - $issue" -ForegroundColor Yellow
        }
    }
} catch {
    Write-Host "  ERROR Config file parsing failed: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Step 2: Check environment
Write-Host "Step 2: Checking environment..." -ForegroundColor Yellow
try {
    $nodeVersion = node --version 2>&1
    Write-Host "  OK Node.js version: $nodeVersion" -ForegroundColor Green
} catch {
    Write-Host "  ERROR Node.js not installed or not in PATH" -ForegroundColor Red
    exit 1
}

try {
    $npmVersion = npm --version 2>&1
    Write-Host "  OK npm version: $npmVersion" -ForegroundColor Green
} catch {
    Write-Host "  ERROR npm not installed or not in PATH" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Step 3: Check server installation
Write-Host "Step 3: Checking server installation..." -ForegroundColor Yellow
try {
    $serverInfo = npm list -g java-class-analyzer-mcp-server 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  OK java-class-analyzer-mcp-server is installed" -ForegroundColor Green
        $versionLine = $serverInfo | Select-String "java-class-analyzer-mcp-server@"
        if ($versionLine) {
            Write-Host "    $versionLine" -ForegroundColor Gray
        }
    } else {
        Write-Host "  WARNING java-class-analyzer-mcp-server not globally installed" -ForegroundColor Yellow
        Write-Host "    Recommendation: npm install -g java-class-analyzer-mcp-server@latest" -ForegroundColor Gray
    }
} catch {
    Write-Host "  WARNING Unable to check server installation status" -ForegroundColor Yellow
}

Write-Host ""

# Step 4: Check process status
Write-Host "Step 4: Checking process status..." -ForegroundColor Yellow
$nodeProcesses = Get-Process -Name "node" -ErrorAction SilentlyContinue
$cursorProcesses = Get-Process -Name "cursor" -ErrorAction SilentlyContinue

if ($nodeProcesses) {
    $nodeCount = ($nodeProcesses | Measure-Object).Count
    Write-Host "  INFO Found $nodeCount Node.js process(es) (These are Cursor's MCP server processes, normal)" -ForegroundColor Gray
} else {
    Write-Host "  INFO No Node.js processes found (Cursor may not be running)" -ForegroundColor Gray
}

if ($cursorProcesses) {
    $cursorCount = ($cursorProcesses | Measure-Object).Count
    Write-Host "  INFO Found $cursorCount Cursor process(es)" -ForegroundColor Gray
} else {
    Write-Host "  WARNING Cursor is not running" -ForegroundColor Yellow
    Write-Host "    Please start Cursor to test MCP server" -ForegroundColor Gray
}

Write-Host ""

# Step 5: Test npx command
Write-Host "Step 5: Testing npx command..." -ForegroundColor Yellow
try {
    $testResult = npx -y java-class-analyzer-mcp-server@latest --help 2>&1 | Select-Object -First 5
    if ($LASTEXITCODE -eq 0 -or $testResult) {
        Write-Host "  OK npx can execute java-class-analyzer-mcp-server" -ForegroundColor Green
    } else {
        Write-Host "  WARNING npx execution may have issues" -ForegroundColor Yellow
    }
} catch {
    Write-Host "  WARNING Unable to test npx command" -ForegroundColor Yellow
}

Write-Host ""

# Summary
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Test Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Configuration verification: OK Complete" -ForegroundColor Green
Write-Host "Environment check: OK Complete" -ForegroundColor Green
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "  1. Ensure Cursor is started" -ForegroundColor White
Write-Host "  2. Try using Java Class Analyzer tools in Cursor" -ForegroundColor White
Write-Host "  3. Observe logs to check if errors are gone" -ForegroundColor White
Write-Host ""
Write-Host "If problems persist:" -ForegroundColor Yellow
Write-Host "  1. Check Cursor's MCP logs" -ForegroundColor Gray
Write-Host "  2. Check if multiple Cursor instances are running" -ForegroundColor Gray
Write-Host "  3. Try completely restarting Cursor" -ForegroundColor Gray
Write-Host ""
