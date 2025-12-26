# 快速修复ioedream-access-service类声明问题
param(
    [string]$ServicePath = "D:\IOE-DREAM\microservices\ioedream-access-service"
)

Write-Host "快速修复ioedream-access-service类声明问题..." -ForegroundColor Green

# 需要修复的文件列表
$filesToFix = @(
    "src\main\java\net\lab1024\sa\access\config\AccessCacheConstants.java",
    "src\main\java\net\lab1024\sa\access\monitor\AccessVerificationMetrics.java",
    "src\main\java\net\lab1024\sa\access\service\impl\AccessBackendAuthServiceImpl.java"
)

foreach ($relativePath in $filesToFix) {
    $fullPath = Join-Path $ServicePath $relativePath

    if (Test-Path $fullPath) {
        Write-Host "修复: $($relativePath)"

        $content = Get-Content -Path $fullPath -Raw -Encoding UTF8
        $className = [System.IO.Path]::GetFileNameWithoutExtension($fullPath)

        # 查找最后一个import语句的位置
        $importsEnd = $content.LastIndexOf("import")
        if ($importsEnd -ne -1) {
            # 找到import语句的结束位置
            $semicolonPos = $content.IndexOf(";", $importsEnd)
            if ($semicolonPos -ne -1) {
                $insertPos = $content.IndexOf("`n", $semicolonPos) + 1

                # 插入类声明
                $classDeclaration = "`n`n/**`n * $className`n */`npublic class $className {`n"
                if ($content -match '@Slf4j') {
                    $classDeclaration += "`n    private static final Logger log = LoggerFactory.getLogger($className.class);"
                }

                # 移除可能存在的错误类声明和孤立的 {
                $content = $content -replace '@Slf4j\s*\{', ''
                $content = $content -replace 'public\s+class\s*\{', "public class $className {"
                $content = $content -replace '^\s*\{', ''

                # 如果没有类声明，在导入后添加
                if ($content -notmatch 'public\s+class\s+\w+' -and $content -notmatch 'interface\s+\w+' -and $content -notmatch 'enum\s+\w+') {
                    $content = $content.Insert($insertPos, $classDeclaration)
                }

                # 添加必要的Logger导入
                if ($content -match 'LoggerFactory' -and $content -notmatch 'import org\.slf4j\.Logger;') {
                    $content = $content -replace '(package [\w\.]+;)', "`$1`n`nimport org.slf4j.Logger;`nimport org.slf4j.LoggerFactory;"
                }

                # 写回文件
                [System.IO.File]::WriteAllText($fullPath, $content, [System.Text.Encoding]::UTF8)
                Write-Host "  ✅ 已修复: $className" -ForegroundColor Yellow
            }
        }
    } else {
        Write-Host "文件不存在: $fullPath" -ForegroundColor Red
    }
}

Write-Host "`n快速修复完成！"
Write-Host "建议接下来运行: mvn clean compile -DskipTests"