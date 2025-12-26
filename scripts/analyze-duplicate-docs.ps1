# IOE-DREAM 重复文档分析脚本

Write-Host "=== README.md 文件分析 ===" -ForegroundColor Cyan
Write-Host ""

$readmeFiles = Get-ChildItem -Path . -Recurse -Filter "README.md" -File -ErrorAction SilentlyContinue |
                Where-Object { $_.FullName -notmatch 'node_modules|\.git|archive|\.m2|target' }

Write-Host "总计: $($readmeFiles.Count) 个README.md文件" -ForegroundColor Yellow
Write-Host ""

# 按目录分组
$categories = @{}

foreach ($file in $readmeFiles) {
    $relativePath = $file.FullName.Substring((Get-Location).Path.Length + 1)
    $dirName = $file.DirectoryName

    # 分类
    if ($dirName -eq (Get-Location).Path) {
        $category = "根目录"
    } elseif ($dirName -match "\\.claude\\") {
        $category = ".claude/"
    } elseif ($dirName -match "\\.spec-workflow\\") {
        $category = ".spec-workflow/"
    } elseif ($dirName -match "\\documentation\\") {
        $category = "documentation/"
    } elseif ($dirName -match "\\deployment\\") {
        $category = "deployment/"
    } elseif ($dirName -match "\\training\\") {
        $category = "training/"
    } elseif ($dirName -match "\\backup\\") {
        $category = "backup/"
    } elseif ($dirName -match "\\microservices\\") {
        $category = "microservices/"
    } else {
        $category = "其他"
    }

    if (-not $categories.ContainsKey($category)) {
        $categories[$category] = @()
    }
    $categories[$category] += $file
}

# 显示分类结果
foreach ($category in $categories.Keys | Sort-Object) {
    $files = $categories[$category]
    Write-Host "[$category] $($files.Count)个文件" -ForegroundColor White

    $files | Select-Object -First 5 | ForEach-Object {
        $relativePath = $_.FullName.Substring((Get-Location).Path.Length + 1)
        $size = [math]::Round($_.Length / 1KB, 2)
        Write-Host "  - $relativePath ($size KB)" -ForegroundColor Gray
    }

    if ($files.Count -gt 5) {
        Write-Host "  ... 还有 $($files.Count - 5) 个文件" -ForegroundColor Gray
    }
    Write-Host ""
}

Write-Host "=== CLAUDE.md 文件分析 ===" -ForegroundColor Cyan
Write-Host ""

$claudeFiles = Get-ChildItem -Path . -Recurse -Filter "CLAUDE.md" -File -ErrorAction SilentlyContinue |
                Where-Object { $_.FullName -notmatch 'node_modules|\.git|archive|\.m2|target' }

Write-Host "总计: $($claudeFiles.Count) 个CLAUDE.md文件" -ForegroundColor Yellow
Write-Host ""

foreach ($file in $claudeFiles) {
    $relativePath = $file.FullName.Substring((Get-Location).Path.Length + 1)
    $size = [math]::Round($file.Length / 1KB, 2)
    Write-Host "  - $relativePath ($size KB)" -ForegroundColor Gray
}
