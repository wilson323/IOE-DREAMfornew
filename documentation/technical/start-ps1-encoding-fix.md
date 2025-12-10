# start.ps1 Encoding Fix Guide

## Issue
PowerShell 5.1 requires UTF-8 with BOM encoding for scripts containing special characters. The current script works but shows a warning.

## Solution: Save as UTF-8 with BOM

### Method 1: VS Code (Recommended)
1. Open `start.ps1` in VS Code
2. Click the encoding indicator in the bottom-right corner (e.g., "UTF-8")
3. Select "Save with Encoding"
4. Choose "UTF-8 with BOM"
5. Save the file

### Method 2: PowerShell Script (Automated)
Run this command to fix encoding automatically:

```powershell
$content = Get-Content "D:\IOE-DREAM\start.ps1" -Raw -Encoding UTF8
$utf8WithBom = New-Object System.Text.UTF8Encoding $true
[System.IO.File]::WriteAllText("D:\IOE-DREAM\start.ps1", $content, $utf8WithBom)
```

### Method 3: Notepad++
1. Open `start.ps1` in Notepad++
2. Go to Encoding menu
3. Select "Convert to UTF-8-BOM"
4. Save the file

## Verification
After fixing, run the script again:
```powershell
powershell -ExecutionPolicy Bypass -File "D:\IOE-DREAM\start.ps1" -StatusOnly
```

The warning should disappear.

## Current Status
✅ Script is fully functional (all English text, no encoding issues)
⚠️ Warning appears but doesn't affect functionality
💡 Fix encoding to remove warning


