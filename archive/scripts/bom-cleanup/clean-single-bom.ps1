# 清理单个文件的BOM字符
$filePath = "D:\IOE-DREAM\microservices\ioedream-oa-service\src\main\java\net\lab1024\sa\oa\manager\WorkflowEngineManager.java"

Write-Host "清理BOM字符: $filePath"

if (Test-Path $filePath) {
    $bytes = [System.IO.File]::ReadAllBytes($filePath)
    if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
        $bytesWithoutBom = $bytes[3..($bytes.Length-1)]
        [System.IO.File]::WriteAllBytes($filePath, $bytesWithoutBom)
        Write-Host "✓ BOM字符已移除" -ForegroundColor Green
    } else {
        Write-Host "⚠️ 未检测到BOM字符" -ForegroundColor Yellow
    }
} else {
    Write-Host "❌ 文件不存在" -ForegroundColor Red
}