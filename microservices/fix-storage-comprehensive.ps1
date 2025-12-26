# Storage模块综合修复脚本 - 最终版本
# 解决所有编译错误：BOM字符、Import语句、Lombok依赖、类声明

Write-Host "=== Storage模块综合修复工具 (最终版本) ===" -ForegroundColor Cyan

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

        # 3. 移除Lombok依赖并替换为标准Logger
        if ($content -match 'import lombok\.extern\.slf4j\.Slf4j;') {
            $content = $content -replace 'import lombok\.extern\.slf4j\.Slf4j;\s*', ''
            $changed = $true
            Write-Host "  ✅ 移除Lombok @Slf4j导入" -ForegroundColor Green
        }

        # 4. 处理@Slf4j注解和Logger声明冲突
        if ($content -match '@Slf4j') {
            # 移除@Slf4j注解
            $content = $content -replace '@Slf4j\s*\n', ''

            # 如果存在重复的Logger声明，移除它
            $content = $content -replace 'private static final Logger log = LoggerFactory\.getLogger\([^)]+\);\s*\n', ''

            $changed = $true
            Write-Host "  ✅ 处理@Slf4j注解和Logger冲突" -ForegroundColor Green
        }

        # 5. 修复类声明 - 查找注解后跟孤立大括号的模式
        $lines = $content -split "`r`n"
        $newLines = @()
        $classAdded = $false

        for ($i = 0; $i -lt $lines.Count; $i++) {
            $line = $lines[$i].Trim()
            $originalLine = $lines[$i]

            # 检查是否是需要修复的注解行
            if ($line -match '@Configuration|@Component|@Service|@RestController|@Controller') {
                $newLines += $originalLine

                # 检查下一行是否是孤立的大括号
                if ($i + 1 -lt $lines.Count) {
                    $nextLine = $lines[$i + 1].Trim()

                    if ($nextLine -eq '{' -or $nextLine -match '^\s*\{\s*$') {
                        # 跳过大括号行，添加正确的类声明
                        $i++  # 跳过大括号行
                        $className = [System.IO.Path]::GetFileNameWithoutExtension($file.Name)
                        $classDeclaration = "public class $className {"
                        $newLines += $classDeclaration
                        $classAdded = $true
                        Write-Host "  ✅ 修复类声明: 添加 '$classDeclaration'" -ForegroundColor Green
                    } elseif ($line -match '\{$\s*$') {
                        # 当行末尾有大括号，替换为类声明
                        $className = [System.IO.Path]::GetFileNameWithoutExtension($file.Name)
                        $fixedLine = $originalLine -replace '\{\s*$', "public class $className {"
                        $newLines[-1] = $fixedLine  # 替换最后一行
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

        # 6. 如果没有类声明但有类内容，添加类声明
        if (-not $classAdded -and $content -match 'class\s+\w+|interface\s+\w+|enum\s+\w+') {
            # 已经有类声明，不需要处理
        } elseif (-not $classAdded -and $content.Contains('@')) {
            # 有注解但没有类声明，需要添加
            $className = [System.IO.Path]::GetFileNameWithoutExtension($file.Name)
            if ($content -match 'public\s+class\s+' + [regex]::Escape($className)) {
                # 已经有正确的类声明
            } else {
                # 在文件末尾添加类声明包装
                $lines = $content -split "`r`n"
                if (-not $content.EndsWith("{")) {
                    $content = $content + "`r`n`r`npublic class $className {`r`n`r`n}"
                    $changed = $true
                    Write-Host "  ✅ 添加完整类声明包装" -ForegroundColor Green
                }
            }
        }

        # 7. 确保有标准的Logger声明（如果没有@Slf4j）
        if ($content -not match '@Slf4j' -and $content -match 'log\.') {
            # 检查是否需要添加Logger声明
            if ($content -not match 'private static final Logger log = LoggerFactory\.getLogger\(') {
                # 在第一个{后添加Logger声明
                $braceIndex = $content.IndexOf('{')
                if ($braceIndex -gt 0) {
                    $insertPoint = $content.IndexOf("`n", $braceIndex) + 1
                    $loggerDeclaration = "`r`n    private static final Logger log = LoggerFactory.getLogger([System.IO.Path]::GetFileNameWithoutExtension($file.Name) + `.class`);`r`n"
                    $content = $content.Insert($insertPoint, $loggerDeclaration)
                    $changed = $true
                    Write-Host "  ✅ 添加标准Logger声明" -ForegroundColor Green
                }
            }
        }

        # 8. 最终清理：移除多余的空行和格式化
        $content = $content -replace '\n\s*\n\s*\n', "`n`n"  # 移除多余的空行
        $content = $content.Trim()

        # 只有当内容发生变化时才写入文件
        if ($changed -or $content -ne $originalContent) {
            $utf8WithoutBom = New-Object System.Text.UTF8Encoding($false)
            [System.IO.File]::WriteAllText($file.FullName, $content, $utf8WithoutBom)

            $fixedCount++
            Write-Host "  📝 文件已更新并保存" -ForegroundColor Cyan
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
    Write-Host "`n🎉 Storage模块修复完成！现在可以尝试编译测试。" -ForegroundColor Green
    Write-Host "建议执行: mvn clean compile -pl microservices-common-storage -am -DskipTests" -ForegroundColor Yellow
} else {
    Write-Host "`n⚠️  修复过程中遇到 $errorCount 个错误，请检查上述错误信息。" -ForegroundColor Yellow
}