# 批量修复Java文件中的乱码
# 此脚本会修复所有包含乱码的Java源文件（排除临时文件）

$baseDir = "smart-admin-api-java17-springboot3"
$fixedCount = 0

# 定义乱码替换规则
$replacements = @{
    '1024鍒涙柊瀹為獙瀹?' = '1024创新实验室'
    '涓讳换: 鍗撳ぇ' = '主任: 张大'
    '缃椾紛' = '卓大'
    '鍒涘' = '创始'
    '锛孲ince' = '，since'
    '璁块棶浠ょ墝' = '访问令牌'
    '鐧诲綍鍛樺伐淇℃伅' = '登录员工信息'
    '鑾峰彇姣忎釜方法鐨勮姹傝矾寰查' = '获取每个方法的请求路径'
    '鑾峰彇鏃犻渶鐧诲綍鍙互鍖垮悕璁块棶鐨剈rl信息' = '获取无需登录可以匿名访问的url信息'
    '鏍规嵁token鍜岃姹傝幏鍙栫櫥褰曞憳宸ヤ俊鎭?' = '根据token和请求获取登录员工信息'
    '涓夌骇绛変繚配置鍒濆初始化鏈€浣庢椿璺冮鐜囧叏灞€配置' = '三级等保配置初始化后最低活跃频率全局配置'
    '姝ら厤缃細瑕嗙洊 sa-base.yaml 涓殑配置' = '此配置会覆盖 sa-base.yaml 中的配置'
    '文件key进行序列化对象 *' = '文件key进行序列化对象'
}

# 查找所有Java文件（排除临时文件）
$javaFiles = Get-ChildItem -Path $baseDir -Recurse -Filter "*.java" |
    Where-Object {
        $_.FullName -notmatch '\.(utf8|disabled|bak|tmp)$' -and
        $_.FullName -notmatch '\\target\\' -and
        $_.FullName -notmatch '\\\.git\\'
    }

Write-Host "找到 $($javaFiles.Count) 个Java文件需要检查..."

foreach ($file in $javaFiles) {
    $content = Get-Content -Path $file.FullName -Raw -Encoding UTF8
    $originalContent = $content
    $modified = $false

    # 应用所有替换规则
    foreach ($key in $replacements.Keys) {
        if ($content -match [regex]::Escape($key)) {
            $content = $content -replace [regex]::Escape($key), $replacements[$key]
            $modified = $true
        }
    }

    # 如果有修改，保存文件
    if ($modified) {
        # 确保文件是UTF-8编码（无BOM）
        $utf8NoBom = New-Object System.Text.UTF8Encoding $false
        [System.IO.File]::WriteAllText($file.FullName, $content, $utf8NoBom)
        Write-Host "已修复: $($file.FullName)"
        $fixedCount++
    }
}

Write-Host "`n修复完成！共修复 $fixedCount 个文件。"

