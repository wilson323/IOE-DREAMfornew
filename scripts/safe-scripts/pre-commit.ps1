Param()

# Fail on first error
$ErrorActionPreference = "Stop"

Write-Host "Running pre-commit checks (encoding/package rules)..." -ForegroundColor Cyan

# 1) Block UTF-8 BOM in text files
$bomFiles = git diff --cached --name-only | ForEach-Object {
  if (Test-Path $_) {
    $bytes = [System.IO.File]::ReadAllBytes($_)
    if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) { $_ }
  }
} | Where-Object { $_ -match '\.(java|xml|md|js|ts|vue|json|yml|yaml|sh|ps1)$' }

if ($bomFiles) {
  Write-Error "Found UTF-8 BOM in files:`n$($bomFiles -join "`n")`nPlease remove BOM (UTF-8 without BOM)."
  exit 1
}

# 2) Block invisible control/format characters
$badCharFiles = git diff --cached --name-only | ForEach-Object {
  if (Test-Path $_) {
    $content = Get-Content $_ -Raw
    if ($content -match "[\uFEFF\u200B\u200E\u200F]") { $_ }
  }
} | Where-Object { $_ -match '\.(java|xml|md|js|ts|vue|json|yml|yaml|sh|ps1)$' }

if ($badCharFiles) {
  Write-Error "Found invisible control/format chars in:`n$($badCharFiles -join "`n")`nPlease clean them."
  exit 1
}

# 3) Package name rules: EE迁移->仅 import 行允许 jakarta.*；白名单保留 javax.crypto / javax.xml
$changedJava = git diff --cached --name-only -- '*.java'

# 禁止 import javax.annotation/validation/servlet 残留
$javaxEe = @()
foreach ($f in $changedJava) {
  if (Test-Path $f) {
    $hits = Select-String -Path $f -Pattern '^\s*import\s+javax\.(annotation|validation|servlet)\.' -CaseSensitive
    if ($hits) { $javaxEe += $f }
  }
}
if ($javaxEe.Count -gt 0) {
  Write-Error "Found EE javax.* imports (should be jakarta.*):`n$($javaxEe | Sort-Object -Unique | Out-String)"
  exit 1
}

# 禁止误把 javax.crypto 或 javax.xml 改为 jakarta.*
$wrongJakarta = @()
foreach ($f in $changedJava) {
  if (Test-Path $f) {
    $hits = Select-String -Path $f -Pattern '^\s*import\s+jakarta\.(crypto|xml)\.' -CaseSensitive
    if ($hits) { $wrongJakarta += $f }
  }
}
if ($wrongJakarta.Count -gt 0) {
  Write-Error "Found wrong jakarta.crypto/xml imports (should remain javax.*):`n$($wrongJakarta | Sort-Object -Unique | Out-String)"
  exit 1
}

Write-Host "pre-commit passed." -ForegroundColor Green
exit 0


