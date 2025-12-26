# 最终BOM清除脚本

$files = @(
    "D:\IOE-DREAM\microservices\microservices-common-storage\src\main\java\net\lab1024\sa\common\storage\impl\LocalFileStorageImpl.java",
    "D:\IOE-DREAM\microservices\microservices-common-storage\src\main\java\net\lab1024\sa\common\storage\impl\MinIOStorageImpl.java"
)

foreach ($file in $files) {
    $bytes = [System.IO.File]::ReadAllBytes($file)
    if ($bytes.Length -gt 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
        $bytesWithoutBom = $bytes[3..($bytes.Length-1)]
        [System.IO.File]::WriteAllBytes($file, $bytesWithoutBom)
        Write-Host "移除BOM: $file" -ForegroundColor Green
    }
}