# 修复反引号字符编码问题 - 简化版本
Write-Host "开始修复反引号字符编码问题..." -ForegroundColor Green

$accessServicePath = "D:\IOE-DREAM\microservices\ioedream-access-service"
$fixedFiles = 0

# 获取所有Java文件
$javaFiles = Get-ChildItem -Path $accessServicePath -Recurse -Filter "*.java" -File

Write-Host "扫描 $($javaFiles.Count) 个Java文件..." -ForegroundColor Cyan

foreach ($file in $javaFiles) {
    try {
        $content = Get-Content -Path $file.FullName -Raw -Encoding UTF8
        $originalContent = $content

        # 修复反引号问题
        if ($content -match '`') {
            Write-Host "处理文件: $($file.Name)" -ForegroundColor Yellow

            # 移除所有反引号和相关字符
            $content = $content -replace '`', ''
            $content = $content -replace '`r`n', ''

            # 如果有修改，保存文件
            if ($content -ne $originalContent) {
                $utf8WithoutBOM = [System.Text.UTF8Encoding]::new($false)
                $bytes = $utf8WithoutBOM.GetBytes($content)
                [System.IO.File]::WriteAllBytes($file.FullName, $bytes)

                Write-Host "  修复完成" -ForegroundColor Green
                $fixedFiles++
            }
        }

    } catch {
        Write-Host "处理失败: $($file.Name) - $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "修复完成! 处理文件数: $fixedFiles" -ForegroundColor Cyan

# 验证编译
Write-Host "验证编译..." -ForegroundColor Green
Set-Location "D:\IOE-DREAM\microservices"

$compileResult = mvn clean compile -pl ioedream-access-service -q -DskipTests
if ($LASTEXITCODE -eq 0) {
    Write-Host "access-service编译成功!" -ForegroundColor Green
} else {
    Write-Host "access-service编译失败" -ForegroundColor Red
}