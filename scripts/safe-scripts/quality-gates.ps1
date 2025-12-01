Param(
    [switch]$SkipTests
)

Write-Host "=== Quality Gates (Windows PowerShell) ==="

$ErrorActionPreference = "Stop"

function Assert-LastExit($msg) {
    if ($LASTEXITCODE -ne 0) {
        Write-Error $msg
        exit 1
    }
}

# 1) Maven compile (skip tests optional)
Push-Location "smart-admin-api-java17-springboot3"
if ($SkipTests) {
    mvn clean compile -DskipTests | Out-Host
} else {
    mvn clean compile | Out-Host
}
Assert-LastExit "Maven compile failed."
Pop-Location

# 2) Forbidden patterns: javax.*, @Autowired
Write-Host "Scan forbidden patterns (javax.*, @Autowired)..."
$codeRoot = "smart-admin-api-java17-springboot3"
$javaxFiles = Get-ChildItem -Path $codeRoot -Recurse -Include *.java | Select-String -Pattern "javax\."
$autowiredFiles = Get-ChildItem -Path $codeRoot -Recurse -Include *.java | Select-String -Pattern "@Autowired"

if ($javaxFiles) {
    Write-Error "Found javax.* usages:"
    $javaxFiles | ForEach-Object { Write-Host $_.Path ":" $_.Line }
    exit 1
}
if ($autowiredFiles) {
    Write-Error "Found @Autowired usages:"
    $autowiredFiles | ForEach-Object { Write-Host $_.Path ":" $_.Line }
    exit 1
}

# 3) Basic controller permission coverage check (@RequireResource)
Write-Host "Check @RequireResource coverage in controllers..."
$controllers = Get-ChildItem -Path "$codeRoot\sa-admin\src\main\java" -Recurse -Include *Controller.java
$missing = @()
foreach ($c in $controllers) {
    $text = Get-Content $c.FullName -Raw
    if ($text -match "@(GetMapping|PostMapping|PutMapping|DeleteMapping)" -and ($text -notmatch "@RequireResource")) {
        $missing += $c.FullName
    }
}
if ($missing.Count -gt 0) {
    Write-Warning "Controllers with endpoints missing @RequireResource (manual review recommended):"
    $missing | ForEach-Object { Write-Host $_ }
}

Write-Host "Quality Gates PASSED."
exit 0


