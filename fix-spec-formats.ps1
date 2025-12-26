# 修复所有OpenSpec规格文件格式
$specFiles = @(
    "openspec/changes/p1-edge-ai-enhancement/specs/frontend-event-display/spec.md",
    "openspec/changes/p1-edge-ai-enhancement/specs/real-time-event-push/spec.md",
    "openspec/changes/p1-edge-ai-enhancement/specs/testing-improvement/spec.md"
)

$reqMapping = @{
    "frontend-event-display" = @{
        "设备AI事件展示页面" = "REQ-FRONTEND-EVENT-001"
        "告警管理页面" = "REQ-FRONTEND-EVENT-002"
        "实时监控面板" = "REQ-FRONTEND-EVENT-003"
        "数据导出" = "REQ-FRONTEND-EVENT-004"
    }
    "real-time-event-push" = @{
        "WebSocket连接管理" = "REQ-REALTIME-PUSH-001"
        "设备AI事件推送" = "REQ-REALTIME-PUSH-002"
        "消息队列和优化" = "REQ-REALTIME-PUSH-003"
        "推送权限和过滤" = "REQ-REALTIME-PUSH-004"
    }
    "testing-improvement" = @{
        "单元测试覆盖" = "REQ-TEST-001"
        "集成测试" = "REQ-TEST-002"
        "性能测试" = "REQ-TEST-003"
        "压力测试和稳定性" = "REQ-TEST-004"
    }
}

foreach ($file in $specFiles) {
    Write-Host "修复文件: $file"
    $content = Get-Content $file -Raw -Encoding UTF8

    $capability = [regex]::Match($file, "specs/([^/]+)/spec\.md").Groups[1].Value
    $mapping = $reqMapping[$capability]

    foreach ($reqName in $mapping.Keys) {
        $reqId = $mapping[$reqName]

        # 查找并替换### Requirement: 为### REQ-ID: Name
        $pattern = "### Requirement: ($([regex]::Escape($reqName)))"
        $replacement = "### $reqId`: `$1`**`n**`n**优先级**: P0"

        # 添加**场景**: 标题
        $replacement += "`n**场景**:`n`n"

        $content = $content -replace $pattern, $replacement
    }

    $content | Out-File $file -Encoding UTF8 -NoNewline
}

Write-Host "格式修复完成！"
