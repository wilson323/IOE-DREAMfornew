Param(
  [string]$Root = "smart-admin-api-java17-springboot3"
)

$ErrorActionPreference = "Stop"

Write-Host "Migrating EE javax imports to jakarta (annotation/validation/servlet) under $Root ..." -ForegroundColor Cyan

$javaFiles = Get-ChildItem -Path $Root -Recurse -File -Filter *.java
$changed = 0

foreach ($f in $javaFiles) {
  $src = Get-Content $f.FullName -Raw
  $dst = $src
  # Only import lines; keep javax.crypto & javax.xml untouched
  $dst = [Regex]::Replace($dst,'(^[ \t]*import\s+)javax\.annotation\.', '$1jakarta.annotation.', 'Multiline')
  $dst = [Regex]::Replace($dst,'(^[ \t]*import\s+)javax\.validation\.', '$1jakarta.validation.', 'Multiline')
  $dst = [Regex]::Replace($dst,'(^[ \t]*import\s+)javax\.servlet\.',   '$1jakarta.servlet.',   'Multiline')

  if ($dst -ne $src) {
    Set-Content -Path $f.FullName -Value $dst -Encoding UTF8
    $changed++
  }
}

Write-Host "Files migrated: $changed" -ForegroundColor Green
exit 0


