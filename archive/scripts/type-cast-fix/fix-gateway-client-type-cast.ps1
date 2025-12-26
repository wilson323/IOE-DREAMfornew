# 修复GatewayServiceClient类型转换问题的脚本
Write-Host "开始修复GatewayServiceClient类型转换问题..." -ForegroundColor Green

# 查找需要修复的文件
$filesToFix = @(
    "D:\IOE-DREAM\microservices\ioedream-oa-service\src\main\java\net\lab1024\sa\oa\manager\WorkflowEngineManager.java",
    "D:\IOE-DREAM\microservices\ioedream-oa-service\src\main\java\net\lab1024\sa\oa\workflow\function\CheckAreaPermissionFunction.java",
    "D:\IOE-DREAM\microservices\ioedream-oa-service\src\main\java\net\lab1024\sa\oa\workflow\job\WorkflowTimeoutReminderJob.java",
    "D:\IOE-DREAM\microservices\ioedream-oa-service\src\main\java\net\lab1024\sa\oa\workflow\listener\WorkflowApprovalResultListener.java"
)

$fixedFiles = 0

foreach ($filePath in $filesToFix) {
    if (Test-Path $filePath) {
        Write-Host "修复文件: $filePath"

        try {
            # 读取文件内容
            $content = Get-Content -Path $filePath -Raw -Encoding UTF8

            # 保存原始内容用于比较
            $originalContent = $content

            # 修复模式1: ResponseDTO<List<UserInfoVO>> response = gatewayServiceClient.callCommonService(...)
            $pattern1 = 'ResponseDTO<[^>]+>\s+response\s*=\s*gatewayServiceClient\.callCommonService\('
            $replacement1 = '@SuppressWarnings("unchecked")`r`n                ResponseDTO<List<UserInfoVO>> response = (ResponseDTO<List<UserInfoVO>>) gatewayServiceClient.callCommonService('
            $content = $content -replace $pattern1, $replacement1

            # 修复模式2: ResponseDTO<UserInfoVO> response = gatewayServiceClient.callCommonService(...)
            $pattern2 = 'ResponseDTO<UserInfoVO>\s+response\s*=\s*gatewayServiceClient\.callCommonService\('
            $replacement2 = '@SuppressWarnings("unchecked")`r`n                ResponseDTO<UserInfoVO> response = (ResponseDTO<UserInfoVO>) gatewayServiceClient.callCommonService('
            $content = $content -replace $pattern2, $replacement2

            # 修复模式3: ResponseDTO<Map<String,Object>> response = gatewayServiceClient.callCommonService(...)
            $pattern3 = 'ResponseDTO<[^>]*Map<[^>]*>\s+response\s*=\s*gatewayServiceClient\.callCommonService\('
            $replacement3 = '@SuppressWarnings("unchecked")`r`n                ResponseDTO<Map<String, Object>> response = (ResponseDTO<Map<String, Object>>) gatewayServiceClient.callCommonService('
            $content = $content -replace $pattern3, $replacement3

            # 修复模式4: ResponseDTO<List<Map<String,Object>>> response = gatewayServiceClient.callCommonService(...)
            $pattern4 = 'ResponseDTO<[^>]*List<[^>]*Map<[^>]*>>\s+response\s*=\s*gatewayServiceClient\.callCommonService\('
            $replacement4 = '@SuppressWarnings("unchecked")`r`n                ResponseDTO<List<Map<String, Object>>> response = (ResponseDTO<List<Map<String, Object>>>) gatewayServiceClient.callCommonService('
            $content = $content -replace $pattern4, $replacement4

            # 修复模式5: ResponseDTO<List<EmployeeVO>> response = gatewayServiceClient.callCommonService(...)
            $pattern5 = 'ResponseDTO<List<EmployeeVO>>\s+response\s*=\s*gatewayServiceClient\.callCommonService\('
            $replacement5 = '@SuppressWarnings("unchecked")`r`n                ResponseDTO<List<EmployeeVO>> response = (ResponseDTO<List<EmployeeVO>>) gatewayServiceClient.callCommonService('
            $content = $content -replace $pattern5, $replacement5

            # 修复模式6: ResponseDTO<Boolean> response = gatewayServiceClient.callCommonService(...)
            $pattern6 = 'ResponseDTO<Boolean>\s+response\s*=\s*gatewayServiceClient\.callCommonService\('
            $replacement6 = '@SuppressWarnings("unchecked")`r`n                ResponseDTO<Boolean> response = (ResponseDTO<Boolean>) gatewayServiceClient.callCommonService('
            $content = $content -replace $pattern6, $replacement6

            # 修复模式7: ResponseDTO<String> response = gatewayServiceClient.callCommonService(...)
            $pattern7 = 'ResponseDTO<String>\s+response\s*=\s*gatewayServiceClient\.callCommonService\('
            $replacement7 = '@SuppressWarnings("unchecked")`r`n                ResponseDTO<String> response = (ResponseDTO<String>) gatewayServiceClient.callCommonService('
            $content = $content -replace $pattern7, $replacement7

            # 修复模式8: ResponseDTO<Void> response = gatewayServiceClient.callCommonService(...)
            $pattern8 = 'ResponseDTO<Void>\s+response\s*=\s*gatewayServiceClient\.callCommonService\('
            $replacement8 = '@SuppressWarnings("unchecked")`r`n                ResponseDTO<Void> response = (ResponseDTO<Void>) gatewayServiceClient.callCommonService('
            $content = $content -replace $pattern8, $replacement8

            # 如果内容有变化，保存文件
            if ($content -ne $originalContent) {
                # 确保导入SuppressWarnings
                if ($content -notmatch 'import.*SuppressWarnings') {
                    # 在package语句后添加import
                    $content = $content -replace '(package\s+[^;]+;)', "`$1`r`n`r`nimport lombok.extern.slf4j.Slf4j;"
                    $content = $content -replace '(import\s+lombok\.extern\.slf4j\.Slf4j;)', "`$1`r`nimport java.util.ArrayList;"
                    $content = $content -replace '(import\s+lombok\.extern\.slf4j\.Slf4j;)', "`$1`r`nimport java.util.List;"
                }

                [System.IO.File]::WriteAllText($filePath, $content, [System.Text.Encoding]::UTF8)
                Write-Host "  ✓ 修复完成" -ForegroundColor Green
                $fixedFiles++
            } else {
                Write-Host "  ⚠️ 无需修复" -ForegroundColor Yellow
            }

        } catch {
            Write-Host "  ❌ 修复失败: $($_.Exception.Message)" -ForegroundColor Red
        }
    } else {
        Write-Host "  ❌ 文件不存在: $filePath" -ForegroundColor Red
    }
}

Write-Host "`n类型转换修复完成!" -ForegroundColor Cyan
Write-Host "修复文件数: $fixedFiles" -ForegroundColor White

# 验证修复
Write-Host "`n验证修复结果..." -ForegroundColor Green
$compilationResult = & mvn -f "D:\IOE-DREAM\microservices\pom.xml" clean compile -q -pl ioedream-oa-service 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ ioedream-oa-service 编译成功！" -ForegroundColor Green
} else {
    Write-Host "❌ ioedream-oa-service 仍有编译错误:" -ForegroundColor Red
    $compilationResult | Select-String "ERROR.*COMPILATION ERROR" -Context 2
}