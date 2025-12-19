#!/usr/bin/env pwsh

# Fix empty package declarations in Java files
$files = Get-ChildItem -Recurse -Include "*.java" | Where-Object {
    $_.FullName -like "*src/main/java*"
}

foreach ($file in $files) {
    $content = Get-Content $file.FullName -Raw

    # Skip if already has package declaration
    if ($content -match "^package\s+") {
        continue
    }

    # Skip empty files
    if ([string]::IsNullOrWhiteSpace($content)) {
        continue
    }

    # Extract package from file path
    $path = $file.FullName
    if ($path -match "src/main/java/(.+)/[^/]+\.java$") {
        $packagePath = $matches[1].Replace('/', '.').Replace('\', '.')

        # Add package declaration at the beginning
        $packageDeclaration = "package $packagePath;"

        # Check if file already starts with package
        if ($content -notmatch "^package\s+") {
            $newContent = "$packageDeclaration`n`n$content"
            Set-Content $file.FullName $newContent -NoNewline
            Write-Host "Fixed package for: $($file.FullName)" -ForegroundColor Green
            Write-Host "  Package: $packagePath" -ForegroundColor Yellow
        }
    }
}

Write-Host "Package declaration fix completed!" -ForegroundColor Cyan