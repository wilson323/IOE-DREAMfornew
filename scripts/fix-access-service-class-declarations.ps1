# 批量修复ioedream-access-service类声明缺失问题
param(
    [string]$ServicePath = "D:\IOE-DREAM\microservices\ioedream-access-service"
)

Write-Host "开始批量修复ioedream-access-service类声明缺失问题..." -ForegroundColor Green

# 获取所有Java文件
$javaFiles = Get-ChildItem -Path "$ServicePath\src\main\java" -Recurse -Filter "*.java" -File
$fixedCount = 0

Write-Host "找到 $($javaFiles.Count) 个Java文件"

foreach ($file in $javaFiles) {
    $content = Get-Content -Path $file.FullName -Raw -Encoding UTF8

    # 检查是否有"需要 class、interface、enum 或 record"类似的语法错误
    # 查找可能的问题模式：
    # 1. @Slf4j 后直接跟 { 而没有类声明
    # 2. 只有注解没有类声明
    # 3. 类声明语法错误

    $originalContent = $content
    $wasFixed = $false

    # 模式1: 修复 @Slf4j { -> public class Xxx { + Logger声明
    if ($content -match '@Slf4j\s*\{\s*') {
        Write-Host "修复 @Slf4j 类声明问题: $($file.Name)"

        # 提取类名
        $className = [System.IO.Path]::GetFileNameWithoutExtension($file.Name)

        # 替换 @Slf4j { 为正确的类声明
        $content = $content -replace '@Slf4j\s*\{', "public class $className {`n`n    private static final Logger log = LoggerFactory.getLogger($className.class);"

        # 添加Logger导入（如果没有的话）
        if ($content -notmatch 'import org\.slf4j\.Logger;') {
            $content = $content -replace '(package [\w\.]+;)', "`$1`n`nimport org.slf4j.Logger;`nimport org.slf4j.LoggerFactory;"
        }

        $wasFixed = $true
    }

    # 模式2: 修复其他注解后直接跟 { 的情况
    if ($content -match '(@\w+)\s*\{\s*' -and $content -notmatch 'public\s+class\s+\w+') {
        Write-Host "修复注解后类声明缺失问题: $($file.Name)"

        # 提取类名
        $className = [System.IO.Path]::GetFileNameWithoutExtension($file.Name)

        # 查找最后一个注解
        $lastAnnotation = [regex]::Match($content, '(@\w+)\s*\{').Groups[1].Value

        # 替换为正确的类声明
        $content = $content -replace "$lastAnnotation\s*\{", "$lastAnnotation`n`npublic class $className {"

        $wasFixed = $true
    }

    # 模式3: 检查文件是否完全没有类声明但有注解
    if ($content -match '@\w+' -and $content -notmatch 'public\s+class\s+\w+' -and $content -notmatch 'abstract\s+class\s+\w+' -and $content -notmatch 'interface\s+\w+' -and $content -notmatch 'enum\s+\w+') {
        Write-Host "修复完全缺失类声明问题: $($file.Name)"

        # 提取类名
        $className = [System.IO.Path]::GetFileNameWithoutExtension($file.Name)

        # 找到合适的插入位置（最后一个导入语句后）
        if ($content -match '(.*?import\s+[\w\.]+;)(.+)') {
            $imports = $matches[1]
            $rest = $matches[2]

            # 插入类声明
            $content = $imports + "`n`n/**`n * $className`n */`npublic class $className {`n" + $rest

            # 如果有@Slf4j注解，添加Logger
            if ($content -match '@Slf4j') {
                if ($content -notmatch 'import org\.slf4j\.Logger;') {
                    $content = $content -replace '(package [\w\.]+;)', "`$1`n`nimport org.slf4j.Logger;`nimport org.slf4j.LoggerFactory;"
                }

                # 移除@Slf4j注解并添加Logger声明
                $content = $content -replace '@Slf4j\s*', ''
                $content = $content -replace 'public class $className \{', "public class $className {`n`n    private static final Logger log = LoggerFactory.getLogger($className.class);"
            }
        }

        $wasFixed = $true
    }

    # 如果有修改，写回文件
    if ($wasFixed -and $content -ne $originalContent) {
        # 备份原文件
        $backupPath = $file.FullName + ".backup"
        Copy-Item -Path $file.FullName -Destination $backupPath -Force

        # 写入修复后的内容
        [System.IO.File]::WriteAllText($file.FullName, $content, [System.Text.Encoding]::UTF8)
        $fixedCount++

        Write-Host "  ✅ 已修复: $($file.Name)" -ForegroundColor Yellow
    }
}

Write-Host "`n批量修复完成！"
Write-Host "总共修复了 $fixedCount 个文件的类声明问题"

# 验证修复结果
Write-Host "`n验证修复结果..."
$errorFiles = @()

foreach ($file in $javaFiles) {
    $content = Get-Content -Path $file.FullName -Raw -Encoding UTF8

    # 检查是否还有类声明问题
    if (($content -match '@Slf4j\s*\{' -or $content -match '@\w+\s*\{' -or ($content -match '@\w+' -and $content -notmatch 'public\s+class\s+\w+' -and $content -notmatch 'abstract\s+class\s+\w+' -and $content -notmatch 'interface\s+\w+' -and $content -notmatch 'enum\s+\w+'))) {
        $errorFiles += $file.Name
    }
}

if ($errorFiles.Count -eq 0) {
    Write-Host "✅ 所有文件的类声明问题已修复完毕！" -ForegroundColor Green
} else {
    Write-Host "⚠️ 还有 $($errorFiles.Count) 个文件可能需要手动检查:" -ForegroundColor Yellow
    $errorFiles | ForEach-Object { Write-Host "  - $_" }
}

Write-Host "`n建议接下来运行: mvn clean compile -DskipTests"