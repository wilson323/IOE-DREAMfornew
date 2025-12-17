#!/usr/bin/env pwsh

# =============================================================================
# IOE-DREAM Manager类Spring注解修复脚本
#
# 功能：批量修复Manager类中的Spring注解违规问题
# 规范：Manager类应该是纯Java类，通过配置类注册为Spring Bean
# =============================================================================

Write-Host "🔧 开始修复Manager类Spring注解违规问题..." -ForegroundColor Green

# 1. 查找所有使用Spring注解的Manager类
Write-Host "`n📋 第一步：查找违规的Manager类..." -ForegroundColor Cyan

$managerFiles = @(
    "microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/manager/AntiPassbackManager.java",
    "microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/manager/BiometricTemplateManager.java",
    "microservices/ioedream-consume-service/src/main/java/net/lab1024/sa/consume/manager/ConsumeTransactionManager.java",
    "microservices/ioedream-device-comm-service/src/main/java/net/lab1024/sa/devicecomm/biometric/BiometricDataManager.java",
    "microservices/ioedream-oa-service/src/main/java/net/lab1024/sa/oa/workflow/cache/WorkflowCacheManager.java",
    "microservices/ioedream-oa-service/src/main/java/net/lab1024/sa/oa/workflow/performance/WorkflowCacheManager.java",
    "microservices/ioedream-video-service/src/main/java/net/lab1024/sa/video/manager/AIEventManager.java",
    "microservices/ioedream-video-service/src/main/java/net/lab1024/sa/video/manager/VideoSystemIntegrationManager.java",
    "microservices/microservices-common/src/main/java/net/lab1024/sa/common/organization/manager/RegionalHierarchyManager.java",
    "microservices/microservices-common/src/main/java/net/lab1024/sa/common/transaction/SeataTransactionManager.java",
    "microservices/microservices-common-business/src/main/java/net/lab1024/sa/common/organization/manager/AreaUserManager.java",
    "microservices/microservices-common-business/src/main/java/net/lab1024/sa/common/video/manager/VideoObjectDetectionManager.java",
    "microservices/microservices-common-business/src/main/java/net/lab1024/sa/common/visitor/manager/LogisticsReservationManager.java",
    "microservices/microservices-common-core/src/main/java/net/lab1024/sa/common/config/QueryOptimizationManager.java",
    "microservices/microservices-common-permission/src/main/java/net/lab1024/sa/common/permission/alert/PermissionAlertManager.java"
)

$fixedFiles = @()
$skippedFiles = @()

# 2. 逐个修复Manager类
Write-Host "`n🔧 第二步：修复Manager类注解..." -ForegroundColor Cyan

foreach ($file in $managerFiles) {
    if (Test-Path $file) {
        Write-Host "  处理文件: $file" -ForegroundColor Yellow

        try {
            # 读取文件内容
            $content = Get-Content $file -Raw -Encoding UTF8

            # 检查是否包含Spring注解
            if ($content -match '@Component|@Service|@Repository') {

                # 移除Spring注解导入
                $content = $content -replace 'import org\.springframework\.stereotype\.Component;\s*\n', ''
                $content = $content -replace 'import org\.springframework\.stereotype\.Service;\s*\n', ''
                $content = $content -replace 'import org\.springframework\.stereotype\.Repository;\s*\n', ''

                # 移除Spring注解使用
                $content = $content -replace '@Component\s*\n', ''
                $content = $content -replace '@Service\s*\n', ''
                $content = $content -replace '@Repository\s*\n', ''

                # 更新注释说明
                $oldComment = '通过构造函数注入依赖，不使用Spring注解'
                $newComment = '纯Java类，不使用Spring注解（@Component, @Service等）\n * - 通过构造函数注入依赖'
                $content = $content -replace [regex]::Escape($oldComment), $newComment

                # 写回文件
                Set-Content $file $content -Encoding UTF8

                $fixedFiles += $file
                Write-Host "    ✅ 修复成功" -ForegroundColor Green
            } else {
                $skippedFiles += $file
                Write-Host "    ℹ️  无需修复" -ForegroundColor Gray
            }
        } catch {
            Write-Host "    ❌ 修复失败: $($_.Exception.Message)" -ForegroundColor Red
            $skippedFiles += $file
        }
    } else {
        Write-Host "  文件不存在: $file" -ForegroundColor Red
        $skippedFiles += $file
    }
}

# 3. 生成修复报告
Write-Host "`n📊 第三步：生成修复报告..." -ForegroundColor Cyan

$report = @"
# IOE-DREAM Manager类Spring注解修复报告

## 修复统计
- 修复时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
- 总文件数: $($managerFiles.Count)
- 成功修复: $($fixedFiles.Count)
- 跳过文件: $($skippedFiles.Count)
- 成功率: $([math]::Round(($fixedFiles.Count / $managerFiles.Count) * 100, 2))%

## 修复详情

### 成功修复的文件
"@

foreach ($file in $fixedFiles) {
    $report += "`n- ✅ $file"
}

if ($skippedFiles.Count -gt 0) {
    $report += "`n`n### 跳过的文件"
    foreach ($file in $skippedFiles) {
        $report += "`n- ⚠️ $file"
    }
}

$report += @"

## 修复内容
1. 移除了所有Spring注解（@Component, @Service, @Repository）
2. 移除了相关import语句
3. 更新了类注释，明确说明为纯Java类
4. 保持构造函数注入模式

## 下一步行动
1. 在对应微服务的配置类中添加Bean注册
2. 更新Service层的依赖注入方式
3. 运行单元测试验证修复效果

## 架构规范说明
根据CLAUDE.md规范：
- microservices-common中的Manager类：纯Java类，不使用Spring注解
- 业务微服务中的Manager类：通过配置类注册为Spring Bean
- 统一使用构造函数注入依赖
- 避免直接在Manager类上使用Spring注解
"@

# 保存报告
$reportPath = "scripts/manager-fix-report.md"
Set-Content $reportPath $report -Encoding UTF8

Write-Host "`n✨ 修复完成！" -ForegroundColor Green
Write-Host "📁 修复报告已保存到: $reportPath" -ForegroundColor Cyan
Write-Host "`n📊 修复统计:" -ForegroundColor White
Write-Host "  成功修复: $($fixedFiles.Count) 个文件" -ForegroundColor Green
Write-Host "  跳过文件: $($skippedFiles.Count) 个文件" -ForegroundColor Gray
Write-Host "  成功率: $([math]::Round(($fixedFiles.Count / $managerFiles.Count) * 100, 2))%" -ForegroundColor Cyan

# 4. 验证修复结果
Write-Host "`n🔍 第四步：验证修复结果..." -ForegroundColor Cyan

$remainingViolations = 0
foreach ($file in $managerFiles) {
    if (Test-Path $file) {
        $content = Get-Content $file -Raw -Encoding UTF8
        if ($content -match '@Component|@Service|@Repository') {
            $remainingViolations++
            Write-Host "  ❌ 仍有违规: $file" -ForegroundColor Red
        }
    }
}

if ($remainingViolations -eq 0) {
    Write-Host "  ✅ 所有Manager类修复成功！" -ForegroundColor Green
} else {
    Write-Host "  ⚠️  仍有 $remainingViolations 个文件需要手动检查" -ForegroundColor Yellow
}

Write-Host "`n🎉 Manager类Spring注解修复脚本执行完成！" -ForegroundColor Green