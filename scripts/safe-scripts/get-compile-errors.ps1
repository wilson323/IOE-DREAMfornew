# 获取详细编译错误
cd D:\IOE-DREAM\smart-admin-api-java17-springboot3\sa-base
$output = mvn compile -DskipTests 2>&1 | Out-String
$output | Out-File -FilePath ..\compile-errors-detailed.txt -Encoding UTF8

# 提取错误信息
$errors = $output | Select-String -Pattern "\[ERROR\].*\.java:\[.*\]" -AllMatches
$errors.Matches | ForEach-Object { $_.Value } | Out-File -FilePath ..\compile-errors-list.txt -Encoding UTF8

Write-Host "编译错误已保存到 compile-errors-detailed.txt 和 compile-errors-list.txt"

