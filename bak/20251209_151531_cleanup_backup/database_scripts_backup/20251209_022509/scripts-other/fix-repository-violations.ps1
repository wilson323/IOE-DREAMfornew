# 批量修复@Repository违规脚本
# 严格遵循CLAUDE.md规范要求

$ErrorActionPreference = "Stop"

# 扫描所有使用@Repository的Java文件
$files = Get-ChildItem -Path "D:\IOE-DREAM\microservices" -Filter "*.java" -Recurse |
    Where-Object { $_ | Select-String -Pattern "import org\.springframework\.stereotype\.Repository" -Quiet }

Write-Host "===== @Repository Violation Fix =====" -ForegroundColor Cyan
Write-Host "Found $($files.Count) files with violations`n" -ForegroundColor Yellow

$fixedCount = 0
$failedCount = 0

foreach ($file in $files) {
    try {
        Write-Host "Processing: $($file.FullName)" -ForegroundColor Gray

        $content = Get-Content $file.FullName -Raw -Encoding UTF8

        # Remove @Repository import
        $newContent = $content -replace 'import org\.springframework\.stereotype\.Repository;[\r\n]+', ''

        # Ensure @Mapper import exists
        if ($newContent -notmatch 'import org\.apache\.ibatis\.annotations\.Mapper;') {
            $newContent = $newContent -replace '(package .*?;)', "`$1`r`nimport org.apache.ibatis.annotations.Mapper;"
        }

        if ($content -ne $newContent) {
            Set-Content -Path $file.FullName -Value $newContent -Encoding UTF8 -NoNewline
            Write-Host "  Fixed" -ForegroundColor Green
            $fixedCount++
        } else {
            Write-Host "  No change needed" -ForegroundColor DarkGray
        }

    } catch {
        Write-Host "  Failed: $_" -ForegroundColor Red
        $failedCount++
    }
}

Write-Host "`n===== Fix Complete =====" -ForegroundColor Cyan
Write-Host "Fixed: $fixedCount files" -ForegroundColor Green
Write-Host "Failed: $failedCount files" -ForegroundColor Red
Write-Host "`nSuggestion: Run Maven compile to verify" -ForegroundColor Yellow
