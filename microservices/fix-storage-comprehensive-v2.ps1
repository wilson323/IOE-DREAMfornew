# Storage模块综合修复脚本 - 语法修复版本

Write-Host "=== Storage模块综合修复工具 (v2) ===" -ForegroundColor Cyan

$storagePath = "D:\IOE-DREAM\microservices\microservices-common-storage"
$fixedCount = 0
$errorCount = 0

Get-ChildItem -Path $storagePath -Recurse -Filter "*.java" | ForEach-Object {
    try {
        $file = $_
        $content = Get-Content $file.FullName -Raw -Encoding UTF8
        $originalContent = $content
        $changed = $false

        Write-Host "`n处理文件: $($file.Name)" -ForegroundColor Yellow

        # 1. 移除BOM字符
        if ($content.StartsWith("﻿")) {
            $content = $content.Substring(1)
            $changed = $true
            Write-Host "  ✅ 移除BOM字符" -ForegroundColor Green
        }

        # 2. 修复Import语句
        $importFixes = @{
            'iimport ' = 'import '
            'iimport' = 'import'
        }

        foreach ($fix in $importFixes.GetEnumerator()) {
            if ($content -match $fix.Key) {
                $content = $content -replace $fix.Key, $fix.Value
                $changed = $true
                Write-Host "  ✅ 修复Import语句: $($fix.Key) → $($fix.Value)" -ForegroundColor Green
            }
        }

        # 3. 移除Lombok依赖
        if ($content -match 'import lombok\.extern\.slf4j\.Slf4j;') {
            $content = $content -replace 'import lombok\.extern\.slf4j\.Slf4j;\s*', ''
            $changed = $true
            Write-Host "  ✅ 移除Lombok @Slf4j导入" -ForegroundColor Green
        }

        # 4. 处理@Slf4j注解
        if ($content -match '@Slf4j') {
            # 移除@Slf4j注解
            $content = $content -replace '@Slf4j\s*\n', ''
            # 移除重复的Logger声明
            $content = $content -replace 'private static final Logger log = LoggerFactory\.getLogger\([^)]+\);\s*\n', ''
            $changed = $true
            Write-Host "  ✅ 处理@Slf4j注解和Logger冲突" -ForegroundColor Green
        }

        # 5. 修复类声明
        $lines = $content -split "`r`n"
        $newLines = @()
        $classAdded = $false

        for ($i = 0; $i -lt $lines.Count; $i++) {
            $line = $lines[$i].Trim()
            $originalLine = $lines[$i]

            if ($line -match '@Configuration|@Component|@Service|@RestController|@Controller') {
                $newLines += $originalLine

                if ($i + 1 -lt $lines.Count) {
                    $nextLine = $lines[$i + 1].Trim()
                    if ($nextLine -eq '{' -or $nextLine -match '^\s*\{\s*$') {
                        $i++  # 跳过大括号行
                        $className = [System.IO.Path]::GetFileNameWithoutExtension($file.Name)
                        $classDeclaration = "public class $className {"
                        $newLines += $classDeclaration
                        $classAdded = $true
                        Write-Host "  ✅ 修复类声明: 添加 '$classDeclaration'" -ForegroundColor Green
                    } elseif ($line -match '\{$\s*$') {
                        $className = [System.IO.Path]::GetFileNameWithoutExtension($file.Name)
                        $fixedLine = $originalLine -replace '\{\s*$', "public class $className {"
                        $newLines[-1] = $fixedLine
                        $classAdded = $true
                        Write-Host "  ✅ 修复行内类声明: $className" -ForegroundColor Green
                    } else {
                        $newLines += $originalLine
                    }
                } else {
                    $newLines += $originalLine
                }
            } else {
                $newLines += $originalLine
            }
        }

        if ($classAdded) {
            $content = $newLines -join "`r`n"
            $changed = $true
        }

        # 6. 添加Logger声明（如果需要且没有）
        if ($content.Contains('log.') -and $content -notmatch 'private static final Logger log = LoggerFactory\.getLogger\(') {
            $lines = $content -split "`r`n"
            for ($i = 0; $i -lt $lines.Count; $i++) {
                if ($lines[$i].Trim() -eq '{') {
                    $className = [System.IO.Path]::GetFileNameWithoutExtension($file.Name)
                    $loggerLine = "    private static final Logger log = LoggerFactory.getLogger($className.class);"
                    $lines[$i + 1] = $loggerLine + "`r`n" + $lines[$i + 1]
                    $content = $lines -join "`r`n"
                    $changed = $true
                    Write-Host "  ✅ 添加Logger声明" -ForegroundColor Green
                    break
                }
            }
        }

        # 7. 最终清理
        $content = $content -replace '\n\s*\n\s*\n', "`n`n"
        $content = $content.Trim()

        if ($changed -or $content -ne $originalContent) {
            $utf8WithoutBom = New-Object System.Text.UTF8Encoding($false)
            [System.IO.File]::WriteAllText($file.FullName, $content, $utf8WithoutBom)
            $fixedCount++
            Write-Host "  📝 文件已更新" -ForegroundColor Cyan
        } else {
            Write-Host "  ℹ️  文件无需修改" -ForegroundColor Gray
        }

    } catch {
        $errorCount++
        Write-Warning "处理文件失败: $($file.FullName) - $($_.Exception.Message)"
    }
}

Write-Host "`n=== 修复完成统计 ===" -ForegroundColor Magenta
Write-Host "✅ 成功修复文件数: $fixedCount" -ForegroundColor Green
Write-Host "❌ 处理失败文件数: $errorCount" -ForegroundColor Red

if ($errorCount -eq 0) {
    Write-Host "`n🎉 Storage模块修复完成！" -ForegroundColor Green
} else {
    Write-Host "`n⚠️  修复过程中遇到 $errorCount 个错误" -ForegroundColor Yellow
}