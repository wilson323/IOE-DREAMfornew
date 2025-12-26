# 修复Manager类声明脚本

Write-Host "=== 修复Manager类声明工具 ===" -ForegroundColor Cyan

# 获取所有微服务目录
$serviceDirs = Get-ChildItem -Directory | Where-Object { $_.Name -match "^ioedream-" }

$totalFilesFixed = 0

foreach ($serviceDir in $serviceDirs) {
    $serviceName = $serviceDir.Name
    Write-Host "`n处理服务: $serviceName" -ForegroundColor Green

    # 获取所有Manager类文件
    $managerFiles = Get-ChildItem -Path $serviceDir.FullName -Recurse -Filter "*Manager.java"
    $filesFixed = 0

    foreach ($managerFile in $managerFiles) {
        try {
            $content = Get-Content -Path $managerFile.FullName -Raw -Encoding UTF8
            $originalContent = $content

            # 修复Manager类声明问题
            # 查找以@Slf4j或其他注解开头，后跟孤立大括号的模式
            $content = $content -replace '(?m)(@Slf4j|@Component|@Service)([^\r\n]*)\r?\n\{[^\r\n]*', '$1$2`r`npublic class ' + [System.IO.Path]::GetFileNameWithoutExtension($managerFile.Name) + ' {'

            # 移除重复的Logger导入
            $content = $content -replace 'import lombok\.exteriorn\.slf4j\.Slf4j;\s*', ''

            # 移除多余的Logger声明（如果有@Slf4j注解）
            if ($content -match '@Slf4j') {
                $content = $content -replace 'private static final Logger log = LoggerFactory\.getLogger\([^)]+\);\s*', ''
            }

            # 只有当内容发生变化时才写入文件
            if ($content -ne $originalContent) {
                $utf8WithoutBom = New-Object System.Text.UTF8Encoding($false)
                [System.IO.File]::WriteAllText($managerFile.FullName, $content, $utf8WithoutBom)
                $filesFixed++
            }
        }
        catch {
            Write-Warning "处理文件失败: $($managerFile.FullName) - $($_.Exception.Message)"
        }
    }

    Write-Host "  ✓ 修复了 $filesFixed 个Manager文件" -ForegroundColor Cyan
    $totalFilesFixed += $filesFixed
}

Write-Host "`n=== Manager类修复完成 ===" -ForegroundColor Green
Write-Host "总计修复文件数: $totalFilesFixed" -ForegroundColor Yellow