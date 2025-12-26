# 移除Java文件中的UTF-8 BOM
# 修复BOM编码问题

$files = @(
    "microservices\ioedream-biometric-service\src\main\java\net\lab1024\sa\biometric\controller\BiometricTemplateController.java",
    "microservices\ioedream-biometric-service\src\main\java\net\lab1024\sa\biometric\service\BiometricTemplateService.java",
    "microservices\ioedream-biometric-service\src\main\java\net\lab1024\sa\biometric\service\impl\BiometricTemplateServiceImpl.java"
)

foreach ($file in $files) {
    if (Test-Path $file) {
        Write-Host "移除BOM: $file"
        $content = Get-Content -Path $file -Raw -Encoding UTF8
        # 移除BOM并重新保存
        $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
        [System.IO.File]::WriteAllText((Resolve-Path $file).Path, $content, $utf8NoBom)
        Write-Host "✅ 完成: $file"
    } else {
        Write-Host "❌ 文件不存在: $file"
    }
}

Write-Host "BOM移除完成！"