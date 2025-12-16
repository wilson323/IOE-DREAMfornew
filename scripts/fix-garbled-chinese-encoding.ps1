# =====================================================
# 批量修复乱码中文编码问题
# 版本: v1.0.0
# 描述: 修复测试文件中的乱码中文注释
# 创建时间: 2025-01-30
# =====================================================

$ErrorActionPreference = "Stop"

# 乱码映射表
$garbledMap = @{
    "鍗曞厓娴嬭瘯" = "单元测试"
    "鐩爣瑕嗙洊鐜" = "目标覆盖率"
    "娴嬭瘯鑼冨洿" = "测试范围"
    "鏍稿績API鏂规硶" = "核心API方法"
    "鏍稿績涓氬姟鏂规硶" = "核心业务方法"
    "鍑嗗娴嬭瘯鏁版嵁" = "准备测试数据"
    "娴嬭瘯绀轰緥-鎴愬姛鍦烘櫙" = "测试示例-成功场景"
    "TODO: 鍑嗗璇锋眰鏁版嵁" = "TODO: 准备请求数据"
    "TODO: 鎵цController鏂规硶" = "TODO: 执行Controller方法"
    "TODO: 楠岃瘉缁撴灉" = "TODO: 验证结果"
    "娴嬭瘯" = "测试"
    "鏍稿績" = "核心"
    "涓氬姟" = "业务"
    "鏂规硶" = "方法"
    "鍑嗗" = "准备"
    "鏁版嵁" = "数据"
    "绀轰緥" = "示例"
    "鎴愬姛" = "成功"
    "鍦烘櫙" = "场景"
    "璇锋眰" = "请求"
    "鎵ц" = "执行"
    "楠岃瘉" = "验证"
    "缁撴灉" = "结果"
}

# 获取所有包含乱码的测试文件
$testFiles = Get-ChildItem -Path "microservices" -Recurse -Filter "*Test.java" | Where-Object {
    $content = Get-Content $_.FullName -Raw -Encoding UTF8
    $hasGarbled = $false
    foreach ($key in $garbledMap.Keys) {
        if ($content -match [regex]::Escape($key)) {
            $hasGarbled = $true
            break
        }
    }
    return $hasGarbled
}

Write-Host "[INFO] 找到 $($testFiles.Count) 个包含乱码的测试文件" -ForegroundColor Yellow

$fixedCount = 0
$errorCount = 0

foreach ($file in $testFiles) {
    try {
        Write-Host "[处理] $($file.FullName)" -ForegroundColor Cyan
        
        # 读取文件内容
        $content = Get-Content $file.FullName -Raw -Encoding UTF8
        
        # 替换所有乱码
        $originalContent = $content
        foreach ($garbled in $garbledMap.Keys) {
            $correct = $garbledMap[$garbled]
            $content = $content -replace [regex]::Escape($garbled), $correct
        }
        
        # 如果内容有变化，保存文件
        if ($content -ne $originalContent) {
            # 确保使用UTF-8 without BOM编码保存
            $utf8NoBom = New-Object System.Text.UTF8Encoding $false
            [System.IO.File]::WriteAllText($file.FullName, $content, $utf8NoBom)
            $fixedCount++
            Write-Host "[OK] 已修复: $($file.Name)" -ForegroundColor Green
        } else {
            Write-Host "[SKIP] 无需修复: $($file.Name)" -ForegroundColor Gray
        }
    } catch {
        $errorCount++
        Write-Host "[ERROR] 修复失败: $($file.FullName) - $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`n[总结]" -ForegroundColor Yellow
Write-Host "  总文件数: $($testFiles.Count)" -ForegroundColor White
Write-Host "  修复成功: $fixedCount" -ForegroundColor Green
Write-Host "  修复失败: $errorCount" -ForegroundColor Red
Write-Host "  跳过文件: $($testFiles.Count - $fixedCount - $errorCount)" -ForegroundColor Gray

