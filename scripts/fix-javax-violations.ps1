# 批量修复javax包名违规脚本
# 严格遵循CLAUDE.md规范：统一使用jakarta包名

$ErrorActionPreference = "Stop"

# 定义替换映射
$replacements = @{
    'javax.validation' = 'jakarta.validation'
    'javax.sql' = 'javax.sql'  # javax.sql保留，不替换
    'javax.annotation' = 'jakarta.annotation'
    'javax.persistence' = 'jakarta.persistence'
    'javax.servlet' = 'jakarta.servlet'
    'javax.transaction' = 'jakarta.transaction'
}

# 扫描使用javax包的Java文件
$files = Get-ChildItem -Path "D:\IOE-DREAM\microservices" -Filter "*.java" -Recurse |
    Where-Object { $_ | Select-String -Pattern "import javax\." -Quiet }

Write-Host "===== javax Package Violation Fix =====" -ForegroundColor Cyan
Write-Host "Found $($files.Count) files with javax imports`n" -ForegroundColor Yellow

$fixedCount = 0
$failedCount = 0

foreach ($file in $files) {
    try {
        Write-Host "Processing: $($file.Name)" -ForegroundColor Gray

        $content = Get-Content $file.FullName -Raw -Encoding UTF8
        $originalContent = $content

        # 执行替换（除了javax.sql）
        $content = $content -replace 'import javax\.validation', 'import jakarta.validation'
        $content = $content -replace 'import javax\.annotation', 'import jakarta.annotation'
        $content = $content -replace 'import javax\.persistence', 'import jakarta.persistence'
        $content = $content -replace 'import javax\.servlet', 'import jakarta.servlet'
        $content = $content -replace 'import javax\.transaction', 'import jakarta.transaction'

        if ($originalContent -ne $content) {
            Set-Content -Path $file.FullName -Value $content -Encoding UTF8 -NoNewline
            Write-Host "  Fixed" -ForegroundColor Green
            $fixedCount++
        } else {
            Write-Host "  No change needed (javax.sql is acceptable)" -ForegroundColor DarkGray
        }

    } catch {
        Write-Host "  Failed: $_" -ForegroundColor Red
        $failedCount++
    }
}

Write-Host "`n===== Fix Complete =====" -ForegroundColor Cyan
Write-Host "Fixed: $fixedCount files" -ForegroundColor Green
Write-Host "Failed: $failedCount files" -ForegroundColor Red

