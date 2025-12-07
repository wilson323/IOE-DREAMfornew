# ========================================
# IOE-DREAM 编译错误一键修复脚本
# ========================================
# 功能：系统性修复Lombok和MyBatis编译问题
# 作者：IOE-DREAM架构团队
# 日期：2025-12-02
# ========================================

Write-Host @"

╔══════════════════════════════════════════════════════════════╗
║                                                              ║
║        IOE-DREAM 编译错误一键修复工具                        ║
║                                                              ║
║        修复内容：                                             ║
║        1. MyBatis注解导入问题                                ║
║        2. Lombok @Slf4j注解缺失                              ║
║        3. 清理Maven缓存并重新编译                             ║
║                                                              ║
╚══════════════════════════════════════════════════════════════╝

"@ -ForegroundColor Cyan

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

# 执行步骤1
Write-Host "`n【步骤 1/3】 修复MyBatis注解导入`n" -ForegroundColor Yellow
& "$scriptDir\fix-compilation-step1.ps1"

# 执行步骤2
Write-Host "`n【步骤 2/3】 修复Lombok日志注解`n" -ForegroundColor Yellow
& "$scriptDir\fix-compilation-step2.ps1"

# 执行步骤3
Write-Host "`n【步骤 3/3】 清理并重新编译`n" -ForegroundColor Yellow
& "$scriptDir\fix-compilation-step3.ps1"

Write-Host "`n" -ForegroundColor White
Write-Host "========================================" -ForegroundColor Green
Write-Host "  所有修复步骤已完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host "`n后续建议操作：" -ForegroundColor Cyan
Write-Host "1. 如果编译成功，执行完整编译：mvn clean install -DskipTests" -ForegroundColor White
Write-Host "2. 如果仍有错误，检查IDE的Lombok插件是否已安装并启用" -ForegroundColor White
Write-Host "3. 在IDE中刷新Maven项目：右键项目 -> Maven -> Reimport" -ForegroundColor White
Write-Host ""

