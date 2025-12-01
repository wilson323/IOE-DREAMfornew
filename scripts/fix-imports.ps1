# 修复fastjson import问题
$CommonDir = "D:\IOE-DREAM\microservices\microservices-common\src\main\java\net\lab1024\sa\common"

Write-Host "开始修复import语句..." -ForegroundColor Green

# 获取所有Java文件
$javaFiles = Get-ChildItem -Path $CommonDir -Filter "*.java" -Recurse

foreach ($file in $javaFiles) {
    $content = Get-Content $file.FullName -Raw -Encoding UTF8
    $oldContent = $content

    # 修复fastjson相关的import
    $content = $content -replace 'import com\.alibaba\.fastjson\.', 'import com.alibaba.fastjson2.'

    # 修复Google Guava的import
    $content = $content -replace 'import com\.google\.common\.base\.', 'import com.google.common.base.'
    $content = $content -replace 'import com\.google\.common\.collect\.', 'import com.google.common.collect.'

    # 如果内容有变化，则写回文件
    if ($content -ne $oldContent) {
        Set-Content -Path $file.FullName -Value $content -Encoding UTF8 -NoNewline
        Write-Host "✓ 修复: $($file.FullName.Replace($CommonDir, ''))" -ForegroundColor Green
    }
}

Write-Host "Import修复完成!" -ForegroundColor Cyan