# 启动脚本 - 先设置编码再执行主脚本
# 使用方法: powershell -ExecutionPolicy Bypass -File "D:\IOE-DREAM\scripts\run-fix-attendance-build.ps1"

# 设置控制台代码页为UTF-8
chcp 65001 | Out-Null

# 设置PowerShell编码
$PSDefaultParameterValues['*:Encoding'] = 'utf8'
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::InputEncoding = [System.Text.Encoding]::UTF8

# 执行主脚本
& "D:\IOE-DREAM\scripts\fix-attendance-build.ps1"
