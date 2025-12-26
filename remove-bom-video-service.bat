@echo off
setlocal enabledelayedexpansion

echo ========================================
echo   Removing BOM characters from Java files
echo ========================================
echo.

set "count=0"

for /r "microservices\ioedream-video-service\src\main\java" %%f in (*.java) do (
    set "file=%%f"

    rem 检查是否包含BOM
    powershell.exe -Command "
        try {
            $bytes = [System.IO.File]::ReadAllBytes('!file!')
            if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
                Write-Host 'Processing: !file!'
                $content = [System.IO.File]::ReadAllText('!file!', [System.Text.Encoding]::UTF8)
                $content = $content.Substring(1)
                [System.IO.File]::WriteAllText('!file!', $content, [System.Text.Encoding]::UTF8)
                Write-Host 'BOM removed from: !file!'
            }
        } catch {
            Write-Host \"Error processing !file!: \$_\"
        }
    "

    set /a count+=1
)

echo.
echo ========================================
echo   BOM removal completed!
echo   Processed !count! files
echo ========================================

exit /b 0