# ============================================================
# IOE-DREAM Git Commit Message验证脚本
#
# 功能：验证commit message格式是否符合规范
# 规范格式: <type>(<scope>): <subject>
# ============================================================

param(
    [string]$CommitMsgFile = $args[0]
)

$ErrorActionPreference = "Stop"

if (-not $CommitMsgFile) {
    Write-Error "未提供commit message文件路径"
    exit 1
}

if (-not (Test-Path $CommitMsgFile)) {
    Write-Error "Commit message文件不存在: $CommitMsgFile"
    exit 1
}

$commitMsg = Get-Content $CommitMsgFile -Raw
$commitMsg = $commitMsg.Trim()

# 读取第一行（主题行）
$lines = $commitMsg -split "`n"
$subject = $lines[0]

# 允许的类型
$allowedTypes = @("feat", "fix", "docs", "style", "refactor", "test", "chore", "perf", "ci", "build", "revert")

# 验证格式: <type>(<scope>): <subject>
$pattern = '^(\w+)\(([^)]*)\):\s+(.+)$'

if ($subject -notmatch $pattern) {
    Write-Host ""
    Write-Host "❌ Commit message格式不正确" -ForegroundColor Red
    Write-Host ""
    Write-Host "当前格式: $subject" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "正确格式: <type>(<scope>): <subject>" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "类型 (type):" -ForegroundColor Yellow
    Write-Host "  feat     - 新功能" -ForegroundColor Gray
    Write-Host "  fix      - 修复bug" -ForegroundColor Gray
    Write-Host "  docs     - 文档更新" -ForegroundColor Gray
    Write-Host "  style    - 代码格式（不影响代码运行的变动）" -ForegroundColor Gray
    Write-Host "  refactor - 重构（既不是新增功能，也不是修复bug）" -ForegroundColor Gray
    Write-Host "  test     - 测试相关" -ForegroundColor Gray
    Write-Host "  chore    - 构建过程或辅助工具的变动" -ForegroundColor Gray
    Write-Host "  perf     - 性能优化" -ForegroundColor Gray
    Write-Host "  ci       - CI配置修改" -ForegroundColor Gray
    Write-Host "  build    - 构建系统修改" -ForegroundColor Gray
    Write-Host "  revert   - 回滚提交" -ForegroundColor Gray
    Write-Host ""
    Write-Host "示例:" -ForegroundColor Yellow
    Write-Host "  feat(access): 添加门禁设备管理功能" -ForegroundColor Green
    Write-Host "  fix(consume): 修复消费记录查询bug" -ForegroundColor Green
    Write-Host "  docs: 更新API文档" -ForegroundColor Green
    Write-Host ""
    exit 1
}

$type = $matches[1]
$scope = $matches[2]
$msg = $matches[3]

# 验证类型
if ($allowedTypes -notcontains $type) {
    Write-Host ""
    Write-Host "❌ 无效的commit类型: $type" -ForegroundColor Red
    Write-Host ""
    Write-Host "允许的类型: $($allowedTypes -join ', ')" -ForegroundColor Yellow
    Write-Host ""
    exit 1
}

# 验证subject长度
if ($msg.Length -gt 50) {
    Write-Host ""
    Write-Host "⚠️  Commit message主题行过长 ($($msg.Length)字符)" -ForegroundColor Yellow
    Write-Host "建议: 主题行应≤50字符，详细说明写在正文中" -ForegroundColor Yellow
    Write-Host ""
}

# 验证subject不能以句号结尾
if ($msg.EndsWith('.')) {
    Write-Host ""
    Write-Host "⚠️  Commit message主题行不应以句号结尾" -ForegroundColor Yellow
    Write-Host ""
}

exit 0

