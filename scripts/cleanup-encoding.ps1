Param(
  [string]$Root = "."
)

$ErrorActionPreference = "Stop"

Write-Host "Cleaning BOM and invisible characters under $Root ..." -ForegroundColor Cyan

$files = Get-ChildItem -Path $Root -Recurse -File -Include *.java,*.xml,*.md,*.js,*.ts,*.vue,*.json,*.yml,*.yaml,*.sh,*.ps1
$cleaned = 0

foreach ($f in $files) {
  $bytes = [System.IO.File]::ReadAllBytes($f.FullName)
  if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
    $bytes = $bytes[3..($bytes.Length-1)]
  }
  $text = [System.Text.Encoding]::UTF8.GetString($bytes)
  $orig = $text
  # remove zero-width & format chars
  $text = $text -replace "[\uFEFF\u200B\u200E\u200F]", ""
  if ($text -ne $orig) {
    [System.IO.File]::WriteAllText($f.FullName, $text, [System.Text.Encoding]::UTF8)
    $cleaned++
  }
}

Write-Host "Files cleaned: $cleaned" -ForegroundColor Green
exit 0


