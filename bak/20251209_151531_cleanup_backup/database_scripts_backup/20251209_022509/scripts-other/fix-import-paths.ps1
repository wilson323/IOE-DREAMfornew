# Fix incorrect import paths in Java files
# Replace old package names with correct ones

$ErrorActionPreference = "Stop"

Write-Host "===== Fix Import Paths =====" -ForegroundColor Cyan

# Define import path replacements
$replacements = @(
    @{
        Old = "net.lab1024.sa.common.dao.AreaDao"
        New = "net.lab1024.sa.common.organization.dao.AreaDao"
    },
    @{
        Old = "net.lab1024.sa.common.entity.AreaEntity"
        New = "net.lab1024.sa.common.organization.entity.AreaEntity"
    },
    @{
        Old = "net.lab1024.sa.common.dao.UserDao"
        New = "net.lab1024.sa.common.security.dao.UserDao"
    },
    @{
        Old = "net.lab1024.sa.common.dao.RoleDao"
        New = "net.lab1024.sa.common.security.dao.RoleDao"
    },
    @{
        Old = "net.lab1024.sa.common.dao.PersonDao"
        New = "net.lab1024.sa.common.security.dao.PersonDao"
    },
    @{
        Old = "net.lab1024.sa.common.entity.UserEntity"
        New = "net.lab1024.sa.common.security.entity.UserEntity"
    },
    @{
        Old = "net.lab1024.sa.common.entity.RoleEntity"
        New = "net.lab1024.sa.common.security.entity.RoleEntity"
    },
    @{
        Old = "net.lab1024.sa.common.entity.PersonEntity"
        New = "net.lab1024.sa.common.security.entity.PersonEntity"
    }
)

# Find all Java files in common module
$javaFiles = Get-ChildItem -Path "D:\IOE-DREAM\microservices\microservices-common\src\main\java" -Filter "*.java" -Recurse |
    Where-Object { $_.DirectoryName -notmatch "target" }

Write-Host "Found $($javaFiles.Count) Java files`n" -ForegroundColor Yellow

$fixedFiles = 0
$totalReplacements = 0

foreach ($file in $javaFiles) {
    $content = Get-Content $file.FullName -Raw -Encoding UTF8
    $originalContent = $content
    $fileChanged = $false

    foreach ($replacement in $replacements) {
        if ($content -match [regex]::Escape($replacement.Old)) {
            $content = $content -replace [regex]::Escape($replacement.Old), $replacement.New
            $fileChanged = $true
            $totalReplacements++
        }
    }

    if ($fileChanged) {
        $utf8NoBom = New-Object System.Text.UTF8Encoding $false
        [System.IO.File]::WriteAllText($file.FullName, $content, $utf8NoBom)
        Write-Host "Fixed: $($file.Name)" -ForegroundColor Green
        $fixedFiles++
    }
}

Write-Host "`n===== Fix Complete =====" -ForegroundColor Cyan
Write-Host "Fixed: $fixedFiles files" -ForegroundColor Green
Write-Host "Total replacements: $totalReplacements" -ForegroundColor Green
Write-Host "`nNext: Run 'mvn clean compile -DskipTests -T 4'" -ForegroundColor Yellow

