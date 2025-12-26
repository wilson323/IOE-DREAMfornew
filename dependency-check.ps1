$ErrorActionPreference = "Stop"

function Check-Dependencies {
    param (
        [string]$Path
    )

    Write-Host "Checking dependencies for $Path..." -ForegroundColor Cyan
    
    # Run maven dependency:analyze
    # Note: We skip compilation to be faster, assuming it's already compiled or we just want static analysis
    # But dependency:analyze usually requires classes.
    # So we do "compile dependency:analyze"
    
    try {
        Push-Location $Path
        
        Write-Host "Running mvn dependency:analyze..."
        $output = cmd /c "mvn dependency:analyze -DignoreNonCompile=true -DoutputXML=true 2>&1"
        
        if ($LASTEXITCODE -ne 0) {
            Write-Host "Maven command failed." -ForegroundColor Red
            # Write-Host $output
        } else {
            # Parse output for "Unused declared dependencies"
            $unused = $output | Select-String "Unused declared dependencies found" -Context 0, 10
            if ($unused) {
                Write-Host "WARNING: Unused dependencies found!" -ForegroundColor Yellow
                $unused | Out-String | Write-Host
            } else {
                Write-Host "No unused dependencies found." -ForegroundColor Green
            }
            
            # Parse output for "Used undeclared dependencies"
            $undeclared = $output | Select-String "Used undeclared dependencies found" -Context 0, 10
            if ($undeclared) {
                Write-Host "WARNING: Used undeclared dependencies found!" -ForegroundColor Red
                $undeclared | Out-String | Write-Host
            } else {
                Write-Host "No undeclared dependencies found." -ForegroundColor Green
            }
        }
    } catch {
        Write-Host "Error checking $Path : $_" -ForegroundColor Red
    } finally {
        Pop-Location
    }
}

# Main execution
$root = Get-Location
$modules = Get-ChildItem -Recurse -Filter "pom.xml" | Select-Object -ExpandProperty DirectoryName | Select-Object -Unique

foreach ($module in $modules) {
    if ($module -like "*target*") { continue }
    
    Check-Dependencies -Path $module
    Write-Host "----------------------------------------"
}
