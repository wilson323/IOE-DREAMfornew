# Storage模块最终修复脚本

Write-Host "=== Storage模块最终修复工具 ===" -ForegroundColor Cyan

Get-ChildItem -Path "D:\IOE-DREAM\microservices\microservices-common-storage" -Recurse -Filter "*.java" | ForEach-Object {
    try {
        $content = Get-Content $_.FullName -Raw -Encoding UTF8
        $originalContent = $content

        # 修复import语句
        $content = $content -replace '^iimport ', 'import '

        # 移除Lombok依赖
        $content = $content -replace 'import lombok\.exteriorn\.slf4j\.Slf4j;\s*', ''

        # 修复@Slf4j导致的Logger重复声明问题
        if ($content -match '@Slf4j') {
            $content = $content -replace 'private static final Logger log = LoggerFactory\.getLogger\([^)]+\);\s*', ''
        }

        # 修复类声明 - 查找@Configuration或@Configuration等注解后面跟着孤立大括号的模式
        $lines = $content -split "`r`n"
        $newLines = @()

        for ($i = 0; $i -lt $lines.Count; $i++) {
            $line = $lines[$i]

            # 检查是否是包含注解但缺少类声明的行
            if ($line -match '@Configuration|@Component|@Service|@RestController|@Controller') {
                $newLines += $line

                # 检查下一行是否是大括号
                if ($i + 1 -lt $lines.Count -and ($lines[$i + 1].Trim() -eq '{' -or $lines[$i + 1] -match '^\s*\{\s*$')) {
                    # 跳过大括号行，添加类声明
                    $i++  # 跳过大括号行
                    $className = [System.IO.Path]::GetFileNameWithoutExtension($_.Name)
                    $newLines += "public class $className {"
                } elseif ($line -match '\{$') {
                    # 当前行末尾有大括号，替换为类声明
                    $line = $line -replace '\{\s*$', "public class " + [System.IO.Path]::GetFileNameWithoutExtension($_.Name) + " {"
                    $newLines += $line
                } else {
                    $newLines += $line
                }
            } else {
                $newLines += $line
            }
        }

        $content = $newLines -join "`r`n"

        # 只有当内容发生变化时才写入文件
        if ($content -ne $originalContent) {
            $utf8WithoutBom = New-Object System.Text.UTF8Encoding($false)
            [System.IO.File]::WriteAllText($_.FullName, $content, $utf8WithoutBom)
            Write-Host "修复: $($_.Name)" -ForegroundColor Green
        }
    } catch {
        Write-Warning "处理文件失败: $($_.FullName) - $($_.Exception.Message)"
    }
}

Write-Host "`n=== Storage模块修复完成 ===" -ForegroundColor Green