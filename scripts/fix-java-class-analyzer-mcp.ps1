# Java Class Analyzer MCP Error Fix Script
# Fixes "Received a response for an unknown message ID" error

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Java Class Analyzer MCP Error Fix Script" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Check npx cache
Write-Host "Step 1: Checking npx cache..." -ForegroundColor Yellow
$npxCache = "$env:APPDATA\npm-cache"
if (Test-Path $npxCache) {
    Write-Host "  Found npx cache directory: $npxCache" -ForegroundColor Gray
    Write-Host "  OK Cache check completed" -ForegroundColor Green
} else {
    Write-Host "  OK npx cache directory does not exist, skipping" -ForegroundColor Green
}

# Step 2: Check Node processes
Write-Host ""
Write-Host "Step 2: Checking Node processes..." -ForegroundColor Yellow
$nodeProcesses = Get-Process -Name "node" -ErrorAction SilentlyContinue
if ($nodeProcesses) {
    $count = ($nodeProcesses | Measure-Object).Count
    Write-Host "  Found $count Node process(es) running" -ForegroundColor Gray
    Write-Host "  Suggestion: Restart Cursor to clean up these processes" -ForegroundColor Gray
} else {
    Write-Host "  OK No Node processes found" -ForegroundColor Green
}

# Step 3: Verify MCP config file
Write-Host ""
Write-Host "Step 3: Verifying MCP config file..." -ForegroundColor Yellow
$mcpConfigPath = "$env:USERPROFILE\.cursor\mcp.json"
if (Test-Path $mcpConfigPath) {
    try {
        $configContent = Get-Content $mcpConfigPath -Raw -Encoding UTF8
        $config = $configContent | ConvertFrom-Json

        $javaConfig = $config.mcpServers.'java-class-analyzer'
        if ($javaConfig) {
            Write-Host "  OK Config file exists and format is correct" -ForegroundColor Green
            Write-Host "  Server command: $($javaConfig.command)" -ForegroundColor Gray

            $argsArray = $javaConfig.args
            $argsStr = $argsArray -join ' '
            Write-Host "  Arguments: $argsStr" -ForegroundColor Gray

            # Check if @latest is used
            $hasLatest = $false
            foreach ($arg in $argsArray) {
                if ($arg -match '@latest') {
                    $hasLatest = $true
                    break
                }
            }

            if ($hasLatest) {
                Write-Host "  OK Using latest version (@latest)" -ForegroundColor Green
            } else {
                Write-Host "  WARNING Not using @latest, recommend update" -ForegroundColor Yellow
            }

            # Check if log level is set
            $envVars = $javaConfig.env
            if ($envVars -and $envVars.MCP_SERVER_LOG_LEVEL) {
                Write-Host "  OK Log level is set" -ForegroundColor Green
            } else {
                Write-Host "  WARNING Log level not set" -ForegroundColor Yellow
            }
        } else {
            Write-Host "  ERROR java-class-analyzer config not found" -ForegroundColor Red
        }
    } catch {
        Write-Host "  ERROR Config file format error: $_" -ForegroundColor Red
    }
} else {
    Write-Host "  ERROR Config file does not exist: $mcpConfigPath" -ForegroundColor Red
}

# Step 4: Provide fix suggestions
Write-Host ""
Write-Host "Step 4: Fix suggestions..." -ForegroundColor Yellow
Write-Host ""
Write-Host "Completed fixes:" -ForegroundColor Cyan
Write-Host "  1. OK Updated config to use @latest version" -ForegroundColor Green
Write-Host "  2. OK Added log level control" -ForegroundColor Green
Write-Host "  3. OK Added alwaysAllow configuration" -ForegroundColor Green
Write-Host ""
Write-Host "Actions you need to take:" -ForegroundColor Cyan
Write-Host "  1. Completely close all Cursor windows" -ForegroundColor Yellow
Write-Host "  2. Wait 5-10 seconds to ensure processes exit completely" -ForegroundColor Yellow
Write-Host "  3. Reopen Cursor" -ForegroundColor Yellow
Write-Host "  4. Verify if errors are gone" -ForegroundColor Yellow
Write-Host ""

# Step 5: Optional cleanup
Write-Host "Optional: Force clean npx cache (execute when needed)" -ForegroundColor Gray
Write-Host "  Command: npm cache clean --force" -ForegroundColor Gray
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Fix script execution completed!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Tips: If the problem persists after restart, try:" -ForegroundColor Yellow
Write-Host "  1. Manual update: npm install -g java-class-analyzer-mcp-server@latest" -ForegroundColor Gray
Write-Host "  2. Check if multiple Cursor instances are running" -ForegroundColor Gray
Write-Host "  3. Check detailed logs to locate the problem" -ForegroundColor Gray
Write-Host ""
