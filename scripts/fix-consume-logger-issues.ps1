# 批量修复consume-service Logger问题脚本
# 作者: IOE-DREAM Team
# 用途: 系统性修复consume-service中所有Logger相关问题

param(
    [string]$ServicePath = "microservices/ioedream-consume-service"
)

Write-Host "=== 开始修复consume-service Logger问题 ===" -ForegroundColor Green

$serviceFullPath = Join-Path $PSScriptRoot ".." $ServicePath
if (-not (Test-Path $serviceFullPath)) {
    Write-Host "错误: 找不到服务路径 $serviceFullPath" -ForegroundColor Red
    exit 1
}

# 获取所有Java文件
$javaFiles = Get-ChildItem -Path $serviceFullPath -Recurse -Filter "*.java" -Exclude "*Test.java"

Write-Host "找到 $($javaFiles.Count) 个Java文件，开始检查Logger问题..." -ForegroundColor Yellow

$fixedFiles = 0
$skippedFiles = 0

foreach ($file in $javaFiles) {
    $content = Get-Content -Path $file.FullName -Raw
    if (-not $content) { continue }

    $originalContent = $content
    $needsFix = $false

    # 检查是否使用了log变量但没有Logger定义
    if ($content -match '\blog\.') {
        # 检查是否有@Slf4j注解但缺少import
        if ($content -match '@Slf4j' -and $content -notmatch 'import lombok\.extern\.slf4j\.Slf4j;') {
            Write-Host "修复 $($file.Name): 添加lombok import" -ForegroundColor Cyan
            $content = $content -replace '(package [^;]+;)', "`$1`r`n`r`nimport lombok.extern.slf4j.Slf4j;"
            $needsFix = $true
        }
        # 检查是否有@Slf4j注解但没有import的情况
        elseif ($content -match '@Slf4j' -and $content -notmatch 'import lombok') {
            Write-Host "修复 $($file.Name): 添加lombok import" -ForegroundColor Cyan
            $content = $content -replace '(package [^;]+;)', "`$1`r`n`r`nimport lombok.extern.slf4j.Slf4j;"
            $needsFix = $true
        }
        # 检查是否使用了传统Logger但没有定义
        elseif ($content -match '\blog\.' -and $content -notmatch 'private static final Logger log') {
            Write-Host "修复 $($file.Name): 添加传统Logger定义" -ForegroundColor Cyan
            # 添加Logger import
            if ($content -notmatch 'import org\.slf4j\.Logger;') {
                $content = $content -replace '(package [^;]+;)', "`$1`r`n`r`nimport org.slf4j.Logger;`r`nimport org.slf4j.LoggerFactory;"
            }
            # 在类中添加Logger定义
            $className = [System.IO.Path]::GetFileNameWithoutExtension($file.Name)
            $classPattern = "public\s+(?:class|interface|enum)\s+$className\b"
            if ($content -match $classPattern) {
                $content = $content -replace ($classPattern + "([^{]*\{)"), "`$1`r`n`r`n    private static final Logger log = LoggerFactory.getLogger($className.class);"
            }
            $needsFix = $true
        }
        # 检查是否有@Slf4j但没有使用的情况
        elseif ($content -match '@Slf4j' -and $content -match 'import lombok\.extern\.slf4j\.Slf4j;' -and $content -notmatch '\blog\.') {
            Write-Host "跳过 $($file.Name): 已有@Slf4j但未使用log" -ForegroundColor Gray
            $skippedFiles++
            continue
        }
    }

    if ($needsFix -and $originalContent -ne $content) {
        # 确保文件以换行符结尾
        if (-not $content.EndsWith("`n")) {
            $content += "`n"
        }

        Set-Content -Path $file.FullName -Value $content -NoNewline -Encoding UTF8
        Write-Host "✓ 已修复: $($file.Name)" -ForegroundColor Green
        $fixedFiles++
    }
    elseif (-not $needsFix) {
        $skippedFiles++
    }
}

Write-Host "`n=== Logger修复完成 ===" -ForegroundColor Green
Write-Host "修复文件数: $fixedFiles" -ForegroundColor Green
Write-Host "跳过文件数: $skippedFiles" -ForegroundColor Yellow

# 编译检查
Write-Host "`n正在编译检查..." -ForegroundColor Yellow
Set-Location $serviceFullPath
$compileResult = mvn compile -q

if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ 编译成功！所有Logger问题已修复" -ForegroundColor Green
} else {
    Write-Host "⚠ 编译仍有问题，请检查剩余错误" -ForegroundColor Yellow
    mvn compile
}

Write-Host "=== 脚本执行完成 ===" -ForegroundColor Green