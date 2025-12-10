@echo off
REM Set code page to UTF-8
chcp 65001 >nul 2>&1

REM Execute PowerShell script using -Command to ensure encoding is set correctly
powershell -ExecutionPolicy Bypass -NoProfile -File "%~dp0fix-attendance-build-en.ps1"

pause
