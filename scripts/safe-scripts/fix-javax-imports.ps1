# Fix javax imports to jakarta imports
# Using UTF8 encoding

$rootPath = "d:\IOE-DREAM\smart-admin-api-java17-springboot3"

Write-Host "========== Start Fixing javax Imports =========="

# Find all Java files with javax imports
$javaFiles = Get-ChildItem -Path $rootPath -Recurse -Filter "*.java" | Where-Object {
    $content = Get-Content $_.FullName -Raw -Encoding UTF8
    $content -match 'import javax\.(annotation|servlet|validation)\.'
}

Write-Host "Found $($javaFiles.Count) files to fix"
Write-Host ""

$fixedCount = 0
$errorCount = 0

foreach ($file in $javaFiles) {
    try {
        $relativePath = $file.FullName.Replace($rootPath + "\", "")
        Write-Host "Fixing: $relativePath"
        
        # Read file content (UTF8)
        $content = Get-Content $file.FullName -Encoding UTF8 -Raw
        
        # Replace all javax packages with jakarta packages
        $originalContent = $content
        
        # javax.annotation -> jakarta.annotation
        $content = $content -replace 'import javax\.annotation\.Resource;', 'import jakarta.annotation.Resource;'
        $content = $content -replace 'import javax\.annotation\.', 'import jakarta.annotation.'
        
        # javax.servlet -> jakarta.servlet
        $content = $content -replace 'import javax\.servlet\.http\.HttpServletRequest;', 'import jakarta.servlet.http.HttpServletRequest;'
        $content = $content -replace 'import javax\.servlet\.http\.HttpServletResponse;', 'import jakarta.servlet.http.HttpServletResponse;'
        $content = $content -replace 'import javax\.servlet\.', 'import jakarta.servlet.'
        
        # javax.validation -> jakarta.validation
        $content = $content -replace 'import javax\.validation\.', 'import jakarta.validation.'
        
        # Only write if content changed
        if ($content -ne $originalContent) {
            # Write back (UTF8 without BOM)
            [System.IO.File]::WriteAllText($file.FullName, $content, [System.Text.UTF8Encoding]::new($false))
            $fixedCount++
            Write-Host "  [OK] Fixed" -ForegroundColor Green
        } else {
            Write-Host "  [-] No change needed" -ForegroundColor Gray
        }
    } catch {
        Write-Host "  [ERROR] Failed: $_" -ForegroundColor Red
        $errorCount++
    }
}

Write-Host ""
Write-Host "========== Fix Complete =========="
Write-Host "Successfully fixed: $fixedCount files" -ForegroundColor Green
if ($errorCount -gt 0) {
    Write-Host "Failed: $errorCount files" -ForegroundColor Red
} else {
    Write-Host "All succeeded, no errors" -ForegroundColor Green
}
