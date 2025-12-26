# 修复语法错误脚本

Write-Host "=== 修复语法错误工具 ===" -ForegroundColor Cyan

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

            # 修复重复的import问题
            $content = $content -replace 'import org\.slf4j\.Logger;\s*iimport org\.slf4j\.LoggerFactory;', 'import org.slf4j.Logger;'
            $content = $content -replace '(?m)^iimport', 'import'

            # 修复类声明问题 - 查找孤立的大括号
            $patterns = @(
                '@PermissionCheck\([^)]+\)\s*\{\s*$',
                '@Tag\([^)]+\)\s*\{\s*$',
                '@RequestMapping\([^)]+\)\s*\{\s*$',
                '@RestController\s*\{\s*$',
                '@Configuration\s*\{\s*$',
                '@Service\s*\{\s*$',
                '@Component\s*\{\s*$'
            )

            foreach ($pattern in $patterns) {
                # 找到匹配的行
                $lines = $content -split "`r`n"
                for ($i = 0; $i -lt $lines.Count; $i++) {
                    if ($lines[$i] -match $pattern) {
                        # 提取类名（从文件名）
                        $className = [System.IO.Path]::GetFileNameWithoutExtension($javaFile.Name)

                        # 修复类声明
                        $lines[$i] = $lines[$i] -replace '\{\s*$', "public class $className {"
                    }
                }
                $content = $lines -join "`r`n"
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

Write-Host "`n=== 语法错误修复完成 ===" -ForegroundColor Green
Write-Host "总计修复文件数: $totalFilesFixed" -ForegroundColor Yellow