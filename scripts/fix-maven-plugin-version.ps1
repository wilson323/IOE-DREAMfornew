# 批量修复Maven插件版本号脚本
# 严格遵循CLAUDE.md规范要求

$ErrorActionPreference = "Stop"

# 扫描所有微服务的pom.xml
$files = Get-ChildItem -Path "D:\IOE-DREAM\microservices" -Filter "pom.xml" -Recurse |
    Where-Object { $_.DirectoryName -notmatch "target" }

Write-Host "===== Maven Plugin Version Fix =====" -ForegroundColor Cyan
Write-Host "Found $($files.Count) pom.xml files`n" -ForegroundColor Yellow

$fixedCount = 0
$skippedCount = 0

foreach ($file in $files) {
    try {
        Write-Host "Processing: $($file.FullName)" -ForegroundColor Gray

        $content = Get-Content $file.FullName -Raw -Encoding UTF8
        $originalContent = $content

        # 检查是否需要修复spring-boot-maven-plugin版本
        if ($content -match '<artifactId>spring-boot-maven-plugin</artifactId>' -and
            $content -notmatch '<artifactId>spring-boot-maven-plugin</artifactId>\s*<version>') {

            # 添加版本号
            $content = $content -replace `
                '(<artifactId>spring-boot-maven-plugin</artifactId>)', `
                "`$1`r`n                    <version>`${spring-boot.version}</version>"

            Write-Host "  Fixed spring-boot-maven-plugin version" -ForegroundColor Green
        }

        if ($originalContent -ne $content) {
            Set-Content -Path $file.FullName -Value $content -Encoding UTF8 -NoNewline
            $fixedCount++
        } else {
            Write-Host "  No fix needed" -ForegroundColor DarkGray
            $skippedCount++
        }

    } catch {
        Write-Host "  Failed: $_" -ForegroundColor Red
    }
}

Write-Host "`n===== Fix Complete =====" -ForegroundColor Cyan
Write-Host "Fixed: $fixedCount files" -ForegroundColor Green
Write-Host "Skipped: $skippedCount files" -ForegroundColor Gray

