# PowerShell Script: Clean Unused Java Imports
# Version: v1.0 Simplified
# Project: IOE-DREAM
# Date: 2025-11-16

param(
    [string]$ProjectPath = "D:\IOE-DREAM\smart-admin-api-java17-springboot3",
    [switch]$DryRun = $false
)

$ErrorActionPreference = "Stop"
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$reportPath = "D:\IOE-DREAM\scripts\reports\import-cleanup-report-$timestamp.txt"

# Create report directory
$reportDir = Split-Path $reportPath -Parent
if (!(Test-Path $reportDir)) {
    New-Item -ItemType Directory -Path $reportDir -Force | Out-Null
}

# Initialize statistics
$totalFiles = 0
$filesWithIssues = 0
$unusedCount = 0
$fixedCount = 0

Write-Host "=================================="
Write-Host "Java Import Cleanup Tool"
Write-Host "=================================="
Write-Host "Project: $ProjectPath"
Write-Host "Mode: $(if ($DryRun) { 'DRY RUN (No changes)' } else { 'ACTUAL RUN' })"
Write-Host "=================================="
Write-Host ""

# Find all Java files
Write-Host "Scanning Java files..."
$javaFiles = Get-ChildItem -Path $ProjectPath -Recurse -Filter "*.java" -File
$totalFiles = $javaFiles.Count

Write-Host "Found $totalFiles Java files"
Write-Host ""

# Problem files list
$problemFiles = @()

# Process each file
$fileIndex = 0
foreach ($file in $javaFiles) {
    $fileIndex++
    $progress = [math]::Round(($fileIndex / $totalFiles) * 100, 2)
    
    Write-Progress -Activity "Analyzing Java Files" -Status "Progress: $progress% ($fileIndex/$totalFiles)" -PercentComplete $progress
    
    try {
        $content = Get-Content -Path $file.FullName -Encoding UTF8 -Raw
        $lines = Get-Content -Path $file.FullName -Encoding UTF8
        
        # Find all import statements
        $imports = @()
        for ($i = 0; $i -lt $lines.Count; $i++) {
            $line = $lines[$i]
            if ($line -match '^\s*import\s+(?:static\s+)?([a-zA-Z0-9._*]+)\s*;') {
                $importClass = $matches[1]
                $imports += @{
                    Line = $i + 1
                    Full = $line
                    Class = $importClass
                }
            }
        }
        
        if ($imports.Count -eq 0) {
            continue
        }
        
        # Check each import if used
        $unusedImports = @()
        foreach ($import in $imports) {
            $className = $import.Class
            
            # Skip wildcard imports
            if ($className -match '\*$') {
                continue
            }
            
            # Get simple class name
            $simpleClassName = $className.Split('.')[-1]
            
            # Check if class name is used in code
            $codeWithoutImports = $content -replace 'import\s+.*?;', ''
            
            # Usage check patterns
            $patterns = @(
                "\b$simpleClassName\b",
                "\b$simpleClassName\s*<",
                "\b$simpleClassName\s*\[",
                "new\s+$simpleClassName\s*\(",
                "@$simpleClassName",
                "extends\s+$simpleClassName",
                "implements\s+$simpleClassName"
            )
            
            $used = $false
            foreach ($pattern in $patterns) {
                if ($codeWithoutImports -match $pattern) {
                    $used = $true
                    break
                }
            }
            
            if (-not $used) {
                $unusedImports += $import
                $unusedCount++
            }
        }
        
        if ($unusedImports.Count -gt 0) {
            $filesWithIssues++
            
            $problemFiles += @{
                Path = $file.FullName
                RelativePath = $file.FullName.Replace($ProjectPath, "").TrimStart('\')
                UnusedImports = $unusedImports
            }
            
            Write-Host "WARNING: $($file.Name)"
            Write-Host "  Unused imports: $($unusedImports.Count)"
            
            foreach ($unused in $unusedImports) {
                Write-Host "  - Line $($unused.Line): $($unused.Class)"
            }
            
            # If not dry run, remove unused imports
            if (-not $DryRun) {
                $newLines = @()
                for ($i = 0; $i -lt $lines.Count; $i++) {
                    $shouldRemove = $false
                    foreach ($unused in $unusedImports) {
                        if ($i -eq ($unused.Line - 1)) {
                            $shouldRemove = $true
                            break
                        }
                    }
                    
                    if (-not $shouldRemove) {
                        $newLines += $lines[$i]
                    } else {
                        $fixedCount++
                    }
                }
                
                # Save modified file
                $newContent = $newLines -join "`r`n"
                Set-Content -Path $file.FullName -Value $newContent -Encoding UTF8 -NoNewline
                
                Write-Host "  FIXED: Cleaned $($unusedImports.Count) unused imports"
            }
            
            Write-Host ""
        }
        
    } catch {
        Write-Host "ERROR: Failed to process $($file.FullName)"
        Write-Host "  $($_.Exception.Message)"
        Write-Host ""
    }
}

Write-Progress -Activity "Analyzing Java Files" -Completed

# Generate report
$report = @"
Java Import Cleanup Report
==========================

Generated: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")
Project: $ProjectPath
Mode: $(if ($DryRun) { 'DRY RUN' } else { 'ACTUAL RUN' })

Statistics
----------
Total Files: $totalFiles
Files with Issues: $filesWithIssues
Unused Imports: $unusedCount
Fixed Imports: $fixedCount

Problem Files
-------------
"@

if ($problemFiles.Count -eq 0) {
    $report += "`nNo unused imports found!`n"
} else {
    foreach ($problem in $problemFiles) {
        $report += "`n$($problem.RelativePath)`n"
        $report += "Unused Imports ($($problem.UnusedImports.Count)):`n"
        foreach ($unused in $problem.UnusedImports) {
            $report += "  - Line $($unused.Line): $($unused.Class)`n"
        }
    }
}

if ($DryRun) {
    $report += @"

NOTE: This was a DRY RUN. No files were modified.
To actually clean imports, run:
    .\clean-unused-imports-simple.ps1 -DryRun:`$false

"@
} else {
    $report += @"

COMPLETED: Fixed $fixedCount unused imports.

Next Steps:
1. Compile: mvn clean compile -DskipTests
2. Test: mvn test
3. Commit: git add . && git commit -m "chore: clean unused imports"

"@
}

# Save report
Set-Content -Path $reportPath -Value $report -Encoding UTF8

# Display summary
Write-Host ""
Write-Host "=================================="
Write-Host "Cleanup Summary"
Write-Host "=================================="
Write-Host "Total Files:        $totalFiles"
Write-Host "Files with Issues:  $filesWithIssues"
Write-Host "Unused Imports:     $unusedCount"
Write-Host "Fixed Imports:      $fixedCount"
Write-Host "=================================="
Write-Host ""
Write-Host "Report saved: $reportPath"
Write-Host ""

if ($DryRun) {
    Write-Host "NOTE: DRY RUN mode - No files were modified"
    Write-Host "To actually clean imports, run:"
    Write-Host "  .\clean-unused-imports-simple.ps1 -DryRun:`$false"
} else {
    Write-Host "Suggested next steps:"
    Write-Host "  1. mvn clean compile -DskipTests"
    Write-Host "  2. mvn test"
    Write-Host "  3. git add . && git commit -m 'chore: clean unused imports'"
}

Write-Host ""

exit 0

