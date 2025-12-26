# IOE-DREAM 包路径统一化更新脚本 (PowerShell版本)
# 用途：批量更新所有导入路径到新的platform包结构
# 作者：IOE-DREAM 架构委员会
# 日期：2025-12-22

param(
    [switch]$WhatIf = $false,
    [switch]$Confirm = $true
)

Write-Host "🔧 IOE-DREAM 包路径统一化更新脚本" -ForegroundColor Blue
Write-Host "======================================" -ForegroundColor Blue
if ($WhatIf) {
    Write-Host "⚠️  预览模式 - 不会实际修改文件" -ForegroundColor Yellow
} else {
    Write-Host "⚠️  此脚本将更新所有导入路径，请确认已备份代码" -ForegroundColor Yellow
}
Write-Host ""

# 检查当前目录
if (-not (Test-Path "microservices")) {
    Write-Host "❌ 请在项目根目录执行此脚本" -ForegroundColor Red
    exit 1
}

# 统计并更新函数
function Update-ImportPaths {
    param(
        [string]$Pattern,
        [string]$Replacement,
        [string]$Description
    )

    Write-Host "正在处理: $Description" -ForegroundColor Blue

    # 查找需要更新的文件
    $files = Get-ChildItem -Path "microservices" -Recurse -Filter "*.java" | Select-String -Pattern $Pattern | Select-Object -Unique Path
    $count = $files.Count

    if ($count -eq 0) {
        Write-Host "  ✓ 没有找到需要更新的文件" -ForegroundColor Green
        return
    }

    Write-Host "  找到 $count 个文件需要更新" -ForegroundColor Yellow

    # 显示前几个文件示例
    $files | Select-Object -First 3 | ForEach-Object {
        Write-Host "    - $($_.Path)" -ForegroundColor Gray
    }

    if ($count -gt 3) {
        Write-Host "    ... 还有 $($count - 3) 个文件" -ForegroundColor Gray
    }

    # 移除确认逻辑，直接执行更新

    # 批量更新
    $updated = 0
    foreach ($file in $files) {
        try {
            $content = Get-Content -Path $file.Path -Raw -Encoding UTF8
            $newContent = $content -replace $Pattern, $Replacement

            if ($content -ne $newContent) {
                if (-not $WhatIf) {
                    Set-Content -Path $file.Path -Value $newContent -Encoding UTF8 -NoNewline
                }
                $updated++
                Write-Host "    ✓ $($file.Path)" -ForegroundColor Green
            }
        } catch {
            Write-Host "    ❌ 更新失败: $($file.Path) - $($_.Exception.Message)" -ForegroundColor Red
        }
    }

    Write-Host "  ✓ 已更新 $updated/$count 个文件的导入路径" -ForegroundColor Green
    Write-Host ""
}

# Phase 1: 更新ResponseDTO导入路径
Write-Host "Phase 1: 更新ResponseDTO导入路径" -ForegroundColor Cyan
Update-ImportPaths -Pattern "net\.lab1024\.sa\.common\.(?:dto\.|domain\.)?ResponseDTO" -Replacement "net.lab1024.sa.platform.core.dto.ResponseDTO" -Description "ResponseDTO导入路径统一化"

# Phase 2: 更新异常类导入路径
Write-Host "Phase 2: 更新异常类导入路径" -ForegroundColor Cyan
Update-ImportPaths -Pattern "net\.lab1024\.sa\.common\.exception\." -Replacement "net.lab1024.sa.platform.core.exception." -Description "异常类导入路径统一化"

# Phase 3: 更新PageResult导入路径
Write-Host "Phase 3: 更新PageResult导入路径" -ForegroundColor Cyan
Update-ImportPaths -Pattern "net\.lab1024\.sa\.common\.domain\.PageResult" -Replacement "net.lab1024.sa.platform.core.dto.PageResult" -Description "PageResult导入路径统一化"

# Phase 4: 更新工具类导入路径
Write-Host "Phase 4: 更新工具类导入路径" -ForegroundColor Cyan
Update-ImportPaths -Pattern "net\.lab1024\.sa\.common\.util\." -Replacement "net.lab1024.sa.platform.core.util." -Description "工具类导入路径统一化"

# 生成验证报告
Write-Host "Phase 5: 生成验证报告" -ForegroundColor Cyan
$reportFile = "IMPORT_PATH_UPDATE_REPORT_$(Get-Date -Format 'yyyyMMdd_HHmmss').md"

$reportContent = @"
# IOE-DREAM 导入路径更新报告

## 更新时间
- 开始时间: $(Get-Date)
- 执行脚本: scripts/update-import-paths.ps1

## 更新统计

### ResponseDTO 导入更新
- 更新前: \`import net.lab1024.sa.common.domain.ResponseDTO\`
- 更新后: \`import net.lab1024.sa.platform.core.dto.ResponseDTO\`

### 异常类 导入更新
- 更新前: \`import net.lab1024.sa.common.exception.*\`
- 更新后: \`import net.lab1024.sa.platform.core.exception.*\`

### PageResult 导入更新
- 更新前: \`import net.lab1024.sa.common.domain.PageResult\`
- 更新后: \`import net.lab1024.sa.platform.core.dto.PageResult\`

### 工具类 导入更新
- 更新前: \`import net.lab1024.sa.common.util.*\`
- 更新后: \`import net.lab1024.sa.platform.core.util.*\`

## 验证检查清单

### 编译验证
- [ ] mvn clean compile -DskipTests
- [ ] 所有服务编译成功
- [ ] 零编译错误

### 运行时验证
- [ ] 各服务启动正常
- [ ] API接口响应正常
- [ ] 异常处理正常

### 功能验证
- [ ] 核心业务功能测试通过
- [ ] 单元测试通过
- [ ] 集成测试通过

## 回滚说明

如需回滚，请执行：
\`\`\`bash
git checkout -- microservices/
git status
\`\`\`

---

报告生成时间: $(Get-Date)
"@

Set-Content -Path $reportFile -Value $reportContent -Encoding UTF8

Write-Host "验证报告已生成: $reportFile" -ForegroundColor Green
Write-Host ""

if ($WhatIf) {
    Write-Host "🔍 预览模式完成！要执行实际更新，请运行：" -ForegroundColor Blue
    Write-Host "  .\scripts\update-import-paths.ps1" -ForegroundColor Gray
} else {
    Write-Host "🎉 导入路径统一化完成！" -ForegroundColor Green
    Write-Host ""
    Write-Host "下一步操作:" -ForegroundColor Blue
    Write-Host "1. 运行编译验证: mvn clean compile -DskipTests" -ForegroundColor Gray
    Write-Host "2. 查看详细报告: $reportFile" -ForegroundColor Gray
    Write-Host "3. 如有问题可使用 git checkout -- microservices/ 回滚" -ForegroundColor Gray
}
Write-Host ""
Write-Host "⚠️  请务必验证编译和运行状态！" -ForegroundColor Yellow