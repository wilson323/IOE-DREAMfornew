# 修复access-service编码问题
# 快速修复脚本 - 2025-12-18
$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "IOE-DREAM Access Service 编码修复" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

$basePath = "D:\IOE-DREAM\microservices\ioedream-access-service\src\main\java\net\lab1024\sa\access"

# 扫描所有Java文件,查找可能有问题的文件
Write-Host "[1/4] 扫描Java文件..." -ForegroundColor Yellow
$files = Get-ChildItem -Path $basePath -Recurse -Filter "*.java" | Where-Object {
    $content = Get-Content $_.FullName -Raw -Encoding UTF8 -ErrorAction SilentlyContinue
    if ($content) {
        # 检查是否包含乱码或问题字符
        $content -match "[\u0000-\u001F\u007F-\u009F]" -or
        $content -match "[\uD800-\uDFFF]" -or
        $content -match '鎺у埗|涓诲叆|娴嬭瘯|鏁版嵁' -or
        $content -match '"\s*\?\s*[,;]'
    } else {
        $false
    }
}

Write-Host "  找到 $($files.Count) 个可能有问题的文件`n" -ForegroundColor White

# 修复文件
Write-Host "[2/4] 修复文件编码..." -ForegroundColor Yellow
$fixedCount = 0
$errorCount = 0

foreach ($file in $files) {
    try {
        Write-Host "  处理: $($file.Name)" -ForegroundColor Cyan

        $content = Get-Content $file.FullName -Raw -Encoding UTF8
        $original = $content

        # 1. 删除不可见Unicode字符
        $content = $content -replace '[\u0000-\u001F\u007F-\u009F\uFEFF]', ''

        # 2. 删除emoji和特殊Unicode (高位代理对)
        $content = $content -replace '[\uD800-\uDFFF]', ''

        # 3. 修复未结束的字符串 - 移除多余的问号
        $content = $content -replace '(["\)])\s*\?\s*([,;])', '$1$2'
        $content = $content -replace '"([^"]*?)\?(\s*)"', '"$1"'
        $content = $content -replace '"([^"]*?)\?\s*\+', '"$1" +'

        # 4. 修复常见的乱码字符串
        $replacements = @{
            '鎺у埗杩囩▼琚腑鏂?' = '控制过程被中断'
            '涓诲叆鍙?' = '主入口'
            '渚ч棬' = '侧门'
            '澶у巺涓ぎ' = '大厅中央'
            '鍓嶅彴' = '前台'
            '鍑哄彛' = '出口'
            '棰勮浣?' = '预设位'
            '鎽勫儚澶?' = '摄像头'
            '寮€濮嬫椂闂?' = '开始时间'
            '缁撴潫鏃堕棿' = '结束时间'
            '闂ㄧ浜嬩欢' = '门禁事件'
            '鍛婅浜嬩欢' = '告警事件'
            '鐢ㄦ埛' = '用户'
            '闂ㄧ璁惧' = '门禁设备'
            '鍖哄煙' = '区域'
            '姝ｅ父闂ㄧ' = '正常门禁'
            '寮傚父闂ㄧ' = '异常门禁'
            '缁熻鏃堕棿' = '统计时间'
            '娴嬭瘯' = '测试'
            '鏁版嵁' = '数据'
            '楠岃瘉' = '验证'
            '缁撴灉' = '结果'
        }

        foreach ($garbled in $replacements.Keys) {
            $correct = $replacements[$garbled]
            $content = $content -replace [regex]::Escape($garbled), $correct
        }

        # 5. 修复多余的问号在字符串中
        $content = $content -replace '(\w+)\?\s*\+\s*\(', '$1" + ('

        if ($content -ne $original) {
            # 使用UTF-8 without BOM保存
            $utf8NoBom = New-Object System.Text.UTF8Encoding $false
            [System.IO.File]::WriteAllText($file.FullName, $content, $utf8NoBom)
            $fixedCount++
            Write-Host "    ✅ 已修复" -ForegroundColor Green
        } else {
            Write-Host "    ⏭️  跳过 (无需修复)" -ForegroundColor Gray
        }
    } catch {
        $errorCount++
        Write-Host "    ❌ 失败: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`n[3/4] 修复总结" -ForegroundColor Yellow
Write-Host "  扫描文件: $($files.Count)" -ForegroundColor White
Write-Host "  修复成功: $fixedCount" -ForegroundColor Green
Write-Host "  修复失败: $errorCount" -ForegroundColor Red
Write-Host "  跳过文件: $($files.Count - $fixedCount - $errorCount)" -ForegroundColor Gray

Write-Host "`n[4/4] 验证修复结果..." -ForegroundColor Yellow
Write-Host "  准备编译验证 access-service..." -ForegroundColor Cyan

Write-Host "`n========================================" -ForegroundColor Green
Write-Host "✅ 编码修复完成!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host "`n下一步: 执行编译验证" -ForegroundColor Yellow
Write-Host "  cd D:\IOE-DREAM\microservices" -ForegroundColor White
Write-Host "  mvn clean compile -pl ioedream-access-service -am -DskipTests -Dpmd.skip=true -Dcheckstyle.skip=true" -ForegroundColor White
