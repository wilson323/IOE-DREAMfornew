# 修复PageResult导入路径脚本
# 将错误的导入路径 net.lab1024.sa.common.openapi.domain.response.PageResult
# 修复为正确的路径 net.lab1024.sa.common.domain.PageResult

$files = Get-ChildItem -Path "microservices" -Filter "*.java" -Recurse

foreach ($file in $files) {
    $content = Get-Content $file.FullName -Raw -ErrorAction SilentlyContinue
    if ($content -and $content -match 'import net\.lab1024\.sa\.common\.openapi\.domain\.response\.PageResult;') {
        Write-Host "修复文件: $($file.FullName)"
        $newContent = $content -replace 'import net\.lab1024\.sa\.common\.openapi\.domain\.response\.PageResult;', 'import net.lab1024.sa.common.domain.PageResult;'
        Set-Content -Path $file.FullName -Value $newContent -NoNewline -Encoding UTF8
    }
}

Write-Host "修复完成！"

