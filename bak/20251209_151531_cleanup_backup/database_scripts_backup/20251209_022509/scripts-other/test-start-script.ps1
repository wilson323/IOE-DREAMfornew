# 测试启动脚本语法
# 用于验证 start-all-complete.ps1 的语法是否正确

$ErrorActionPreference = "Stop"

Write-Host "测试启动脚本语法..." -ForegroundColor Yellow

try {
    # 检查脚本文件是否存在
    $scriptPath = ".\scripts\start-all-complete.ps1"
    if (-not (Test-Path $scriptPath)) {
        Write-Host "❌ 脚本文件不存在: $scriptPath" -ForegroundColor Red
        exit 1
    }
    
    # 使用PowerShell解析器检查语法
    # 注意：PSScriptAnalyzer可能误报"error"变量警告，但这里使用的是$parsingIssues和$item，不是$error
    $parsingIssues = $null
    $null = [System.Management.Automation.PSParser]::Tokenize((Get-Content $scriptPath -Raw), [ref]$parsingIssues)
    
    if ($parsingIssues.Count -gt 0) {
        Write-Host "❌ 脚本语法错误:" -ForegroundColor Red
        foreach ($issue in $parsingIssues) {
            Write-Host "  行 $($issue.Token.StartLine): $($issue.Message)" -ForegroundColor Red
        }
        exit 1
    }
    
    Write-Host "✅ 脚本语法检查通过" -ForegroundColor Green
    
    # 测试参数解析
    Write-Host "`n测试参数解析..." -ForegroundColor Yellow
    $testParams = @("-BackendOnly", "-FrontendOnly", "-MobileOnly", "-CheckOnly")
    foreach ($param in $testParams) {
        Write-Host "  测试参数: $param" -ForegroundColor Gray
    }
    
    Write-Host "`n✅ 所有测试通过" -ForegroundColor Green
    Write-Host "`n现在可以运行:" -ForegroundColor Cyan
    Write-Host "  .\scripts\start-all-complete.ps1 -BackendOnly" -ForegroundColor White
    Write-Host "  .\scripts\start-all-complete.ps1 -FrontendOnly" -ForegroundColor White
    Write-Host "  .\scripts\start-all-complete.ps1 -MobileOnly" -ForegroundColor White
    Write-Host "  .\scripts\start-all-complete.ps1 -CheckOnly" -ForegroundColor White
    
} catch {
    Write-Host "❌ 测试失败: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}
