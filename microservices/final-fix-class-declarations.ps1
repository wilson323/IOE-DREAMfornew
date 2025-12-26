# 最终修复类声明脚本

Write-Host "=== 最终修复类声明工具 ===" -ForegroundColor Cyan

# 获取所有微服务目录
$serviceDirs = Get-ChildItem -Directory | Where-Object { $_.Name -match "^ioedream-" }

$totalFilesFixed = 0

foreach ($serviceDir in $serviceDirs) {
    $serviceName = $serviceDir.Name
    Write-Host "`n处理服务: $serviceName" -ForegroundColor Green

    # 获取所有Java文件
    $javaFiles = Get-ChildItem -Path $serviceDir.FullName -Recurse -Filter "*.java"
    $filesFixed = 0

    foreach ($javaFile in $javaFiles) {
        try {
            $content = Get-Content -Path $javaFile.FullName -Raw -Encoding UTF8
            $originalContent = $content

            # 修复Logger导入问题
            $content = $content -replace '// replaces LoggerFactory', ''
            $content = $content -replace 'import org\.slf4j\.Logger;\s*// replaces LoggerFactory', 'import org.slf4j.Logger;'

            # 修复类声明 - 查找缺失的类声明
            # 模式：注解行 + 空行 + 只有{的行
            $pattern = '(?m)(@RestController|@Controller|@Service|@Component|@Configuration|@RestControllerAdvice|@ControllerAdvice|@Repository)([^\r\n]*)\r?\n\ {[^\r\n]*'

            if ($content -match $pattern) {
                # 获取类名（从文件名）
                $className = [System.IO.Path]::GetFileNameWithoutExtension($javaFile.Name)

                # 逐行处理
                $lines = $content -split "`r`n"
                $newLines = @()

                for ($i = 0; $i -lt $lines.Count; $i++) {
                    $line = $lines[$i]

                    # 检查是否是包含注解的行且下一行是孤立的大括号
                    if ($line -match '@RestController|@Controller|@Service|@Component|@Configuration|@RestControllerAdvice|@ControllerAdvice|@Repository') {
                        $newLines += $line

                        # 检查下一行
                        if ($i + 1 -lt $lines.Count -and ($lines[$i + 1].Trim() -eq '{' -or $lines[$i + 1] -match '^\s*\{\s*$')) {
                            # 跳过大括号行，添加类声明
                            $i++  # 跳过大括号行
                            $newLines += "public class $className {"
                        } elseif ($line -match '\{$') {
                            # 当前行末尾有大括号，替换为类声明
                            $line = $line -replace '\{\s*$', "public class $className {"
                            $newLines += $line
                        } else {
                            $newLines += $line
                        }
                    } else {
                        $newLines += $line
                    }
                }

                $content = $newLines -join "`r`n"
            }

            # 只有当内容发生变化时才写入文件
            if ($content -ne $originalContent) {
                $utf8WithoutBom = New-Object System.Text.UTF8Encoding($false)
                [System.IO.File]::WriteAllText($javaFile.FullName, $content, $utf8WithoutBom)
                $filesFixed++
            }
        }
        catch {
            Write-Warning "处理文件失败: $($javaFile.FullName) - $($_.Exception.Message)"
        }
    }

    Write-Host "  ✓ 修复了 $filesFixed 个文件" -ForegroundColor Cyan
    $totalFilesFixed += $filesFixed
}

Write-Host "`n=== 最终修复完成 ===" -ForegroundColor Green
Write-Host "总计修复文件数: $totalFilesFixed" -ForegroundColor Yellow