# 批量修复已弃用的BigDecimal API
# 将 BigDecimal.ROUND_HALF_UP 替换为 RoundingMode.HALF_UP

Write-Host "开始批量修复已弃用的BigDecimal API..." -ForegroundColor Cyan

$projectRoot = "D:\IOE-DREAM\smart-admin-api-java17-springboot3"
$javaFiles = Get-ChildItem -Path $projectRoot -Filter "*.java" -Recurse | Where-Object { $_.FullName -notmatch "\\target\\" -and $_.FullName -notmatch "\\.backup" }

$fixedCount = 0
$totalFiles = $javaFiles.Count

foreach ($file in $javaFiles) {
    $content = Get-Content -Path $file.FullName -Raw -Encoding UTF8
    $originalContent = $content
    $needsImport = $false

    # 检查是否需要添加RoundingMode导入
    if ($content -match "BigDecimal\.ROUND_HALF_UP") {
        $needsImport = $true
    }

    # 替换 divide(BigDecimal, int, BigDecimal.ROUND_HALF_UP)
    $content = $content -replace '\.divide\(([^,]+),\s*(\d+),\s*BigDecimal\.ROUND_HALF_UP\)', '.divide($1, $2, RoundingMode.HALF_UP)'

    # 替换 setScale(int, BigDecimal.ROUND_HALF_UP)
    $content = $content -replace '\.setScale\((\d+),\s*BigDecimal\.ROUND_HALF_UP\)', '.setScale($1, RoundingMode.HALF_UP)'

    # 如果内容有变化，添加RoundingMode导入
    if ($content -ne $originalContent -and $needsImport) {
        # 检查是否已有RoundingMode导入
        if ($content -notmatch "import\s+java\.math\.RoundingMode;") {
            # 在BigDecimal导入后添加RoundingMode导入
            if ($content -match "(import\s+java\.math\.BigDecimal;)") {
                $content = $content -replace "(import\s+java\.math\.BigDecimal;)", "`$1`nimport java.math.RoundingMode;"
            } else {
                # 如果没有BigDecimal导入，在package声明后添加
                if ($content -match "(package\s+[^;]+;)") {
                    $content = $content -replace "(package\s+[^;]+;)", "`$1`n`nimport java.math.RoundingMode;"
                }
            }
        }

        # 保存文件
        [System.IO.File]::WriteAllText($file.FullName, $content, [System.Text.UTF8Encoding]::new($false))
        $fixedCount++
        Write-Host "Fixed: $($file.Name)" -ForegroundColor Green
    }
}

Write-Host ""
Write-Host "修复完成！共修复 $fixedCount / $totalFiles 个文件" -ForegroundColor Green
