# 修复类声明脚本

Write-Host "=== 修复类声明工具 ===" -ForegroundColor Cyan

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

            # 修复不正确的注释
            $content = $content -replace '// replaces LoggerFactory', ''

            # 使用更精确的正则表达式修复类声明
            # 查找 @RestController 或 @Controller 等注后面跟着的孤立大括号
            $content = $content -replace '(?m)(@RestController|@Controller|@Service|@Component|@Configuration|@RestControllerAdvice|@ControllerAdvice|@Repository|@Service)([^\r\n]*\r?\n[^\r\n]*)\s*\{', '$1$2public class ' + [System.IO.Path]::GetFileNameWithoutExtension($javaFile.Name) + ' {'

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

Write-Host "`n=== 类声明修复完成 ===" -ForegroundColor Green
Write-Host "总计修复文件数: $totalFilesFixed" -ForegroundColor Yellow