# 检查脚本语法
$errors = $null
$content = Get-Content 'D:\IOE-DREAM\scripts\start-all-complete.ps1' -Raw -Encoding UTF8
[System.Management.Automation.PSParser]::Tokenize($content, [ref]$errors)

if ($errors.Count -gt 0) {
    Write-Host "语法错误:" -ForegroundColor Red
    foreach ($err in $errors) {
        Write-Host "行 $($err.Token.StartLine): $($err.Message)" -ForegroundColor Red
    }
    exit 1
} else {
    Write-Host "语法检查通过" -ForegroundColor Green
    exit 0
}

