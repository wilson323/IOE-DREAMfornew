# 精确修复ioedream-oa-service中GatewayServiceClient类型转换问题
Write-Host "开始精确修复ioedream-oa-service中的GatewayServiceClient类型转换问题..." -ForegroundColor Green

# 需要修复的文件路径
$filePath = "D:\IOE-DREAM\microservices\ioedream-oa-service\src\main\java\net\lab1024\sa\oa\manager\WorkflowEngineManager.java"

if (Test-Path $filePath) {
    Write-Host "修复文件: $filePath"

    try {
        # 读取文件内容
        $content = Get-Content -Path $filePath -Raw -Encoding UTF8

        # 保存原始内容用于比较
        $originalContent = $content

        # 修复模式1: ResponseDTO<List<UserInfoVO>> response = gatewayServiceClient.callCommonService(...)
        $pattern1 = '(\s+)ResponseDTO<List<UserInfoVO>> response = gatewayServiceClient\.callCommonService\('
        $replacement1 = '$1@SuppressWarnings("unchecked")`r`n$1ResponseDTO<List<UserInfoVO>> response = (ResponseDTO<List<UserInfoVO>>) gatewayServiceClient.callCommonService('
        $content = $content -replace $pattern1, $replacement1

        # 修复模式2: ResponseDTO<UserInfoVO> response = gatewayServiceClient.callCommonService(...)
        $pattern2 = '(\s+)ResponseDTO<UserInfoVO> response = gatewayServiceClient\.callCommonService\('
        $replacement2 = '$1@SuppressWarnings("unchecked")`r`n$1ResponseDTO<UserInfoVO> response = (ResponseDTO<UserInfoVO>) gatewayServiceClient.callCommonService('
        $content = $content -replace $pattern2, $replacement2

        # 修复模式3: ResponseDTO<Map<String,Object>> response = gatewayServiceClient.callCommonService(...)
        $pattern3 = '(\s+)ResponseDTO<[^>]*Map<[^>]*Object[^>]*>\s+response\s*=\s*gatewayServiceClient\.callCommonService\('
        $replacement3 = '$1@SuppressWarnings("unchecked")`r`n$1ResponseDTO<Map<String, Object>> response = (ResponseDTO<Map<String, Object>>) gatewayServiceClient.callCommonService('
        $content = $content -replace $pattern3, $replacement3

        # 修复模式4: ResponseDTO<List<Map<String,Object>>> response = gatewayServiceClient.callCommonService(...)
        $pattern4 = '(\s+)ResponseDTO<[^>]*List<[^>]*Map<[^>]*Object[^>]*>>\s+response\s*=\s*gatewayServiceClient\.callCommonService\('
        $replacement4 = '$1@SuppressWarnings("unchecked")`r`n$1ResponseDTO<List<Map<String, Object>>> response = (ResponseDTO<List<Map<String, Object>>>) gatewayServiceClient.callCommonService('
        $content = $content -replace $pattern4, $replacement4

        # 修复模式5: ResponseDTO<List<EmployeeVO>> response = gatewayServiceClient.callCommonService(...)
        $pattern5 = '(\s+)ResponseDTO<List<EmployeeVO>> response = gatewayServiceClient\.callCommonService\('
        $replacement5 = '$1@SuppressWarnings("unchecked")`r`n$1ResponseDTO<List<EmployeeVO>> response = (ResponseDTO<List<EmployeeVO>>) gatewayServiceClient.callCommonService('
        $content = $content -replace $pattern5, $replacement5

        # 保存文件
        [System.IO.File]::WriteAllText($filePath, $content, [System.Text.Encoding]::UTF8)
        Write-Host "  ✓ WorkflowEngineManager.java 修复完成" -ForegroundColor Green

    } catch {
        Write-Host "  ❌ 修复失败: $($_.Exception.Message)" -ForegroundColor Red
    }
} else {
    Write-Host "  ❌ 文件不存在: $filePath" -ForegroundColor Red
}

# 修复其他文件
$otherFiles = @(
    "D:\IOE-DREAM\microservices\ioedream-oa-service\src\main\java\net\lab1024\sa\oa\workflow\function\CheckAreaPermissionFunction.java",
    "D:\IOE-DREAM\microservices\ioedream-oa-service\src\main\java\net\lab1024\sa\oa\workflow\job\WorkflowTimeoutReminderJob.java",
    "D:\IOE-DREAM\microservices\ioedream-oa-service\src\main\java\net\lab1024\sa\oa\workflow\listener\WorkflowApprovalResultListener.java"
)

foreach ($file in $otherFiles) {
    if (Test-Path $file) {
        Write-Host "修复文件: $file"

        try {
            $content = Get-Content -Path $file -Raw -Encoding UTF8
            $originalContent = $content

            # 通用修复模式
            $content = $content -replace '(\s+)ResponseDTO<([^>]+)>\s+response\s*=\s*gatewayServiceClient\.callCommonService\(', '$1@SuppressWarnings("unchecked")`r`n$1ResponseDTO<$2> response = (ResponseDTO<$2>) gatewayServiceClient.callCommonService('

            if ($content -ne $originalContent) {
                [System.IO.File]::WriteAllText($file, $content, [System.Text.Encoding]::UTF8)
                Write-Host "  ✓ 修复完成: $((Split-Path $file -Leaf))" -ForegroundColor Green
            } else {
                Write-Host "  ⚠️ 无需修复: $((Split-Path $file -Leaf))" -ForegroundColor Yellow
            }

        } catch {
            Write-Host "  ❌ 修复失败: $($_.Exception.Message)" -ForegroundColor Red
        }
    }
}

Write-Host "`n类型转换修复完成！" -ForegroundColor Cyan
Write-Host "开始验证编译结果..." -ForegroundColor Green

# 验证编译
cd "D:\IOE-DREAM\microservices"
$compilationOutput = mvn clean compile -q 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ 所有服务编译成功！" -ForegroundColor Green
} else {
    Write-Host "❌ 仍有编译错误，继续分析..." -ForegroundColor Red
    $compilationOutput | Select-String "ERROR.*COMPILATION ERROR" -Context 0,1 | Select-Object -First 10
}