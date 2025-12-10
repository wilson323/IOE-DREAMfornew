@echo off
REM 设置代码页为UTF-8
chcp 65001 >nul 2>&1

REM 使用-Command参数执行，确保编码设置正确传递
powershell -ExecutionPolicy Bypass -NoProfile -Command "$OutputEncoding = [System.Text.Encoding]::UTF8; [Console]::OutputEncoding = [System.Text.Encoding]::UTF8; [Console]::InputEncoding = [System.Text.Encoding]::UTF8; $PSDefaultParameterValues['*:Encoding'] = 'utf8'; chcp 65001 | Out-Null; & '%~dp0fix-attendance-build.ps1'"

pause
