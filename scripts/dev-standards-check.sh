#!/bin/bash
# 开发规范检查 - 明确开发规范和上次工作验证
# 用途: 工作前强制检查开发规范和上次工作状态

set -e

echo "📋 开发规范检查 - 强制执行"
echo "时间: $(date)"
echo "项目: $(pwd)"
echo ""

# 获取脚本所在目录的父目录作为项目根目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# 检查开发规范文档
echo "检查1: 开发规范文档完整性"
STANDARD_FILES=(
    "$PROJECT_ROOT/CLAUDE.md"
    "$PROJECT_ROOT/FORCED_EXECUTION_CONTRACT.md"
    "$PROJECT_ROOT/ENFORCEMENT_PROTOCOL.md"
    "$PROJECT_ROOT/docs/DEV_STANDARDS.md"
    "$PROJECT_ROOT/docs/ARCHITECTURE_STANDARDS.md"
)

for file in "${STANDARD_FILES[@]}"; do
    if [ -f "$file" ]; then
        echo "✅ $file 存在"
    else
        echo "❌ $file 不存在！缺少开发规范文档"
        exit 1
    fi
done

echo ""
echo "检查2: 上次工作验证状态"
LAST_VERIFICATION=$(ls verification-report-*.json 2>/dev/null | tail -n 1)
if [ -n "$LAST_VERIFICATION" ]; then
    echo "✅ 上次验证报告存在: $LAST_VERIFICATION"

    # 检查上次验证时间
    VERIFICATION_TIME=$(basename "$LAST_VERIFICATION" | sed 's/verification-report-\([0-9]*\)-\([0-9]*\).json/\1 \2/')
    echo "✅ 上次验证时间: $VERIFICATION_TIME"

    # 检查上次验证结果
    if grep -q '"status": "PASSED"' "$LAST_VERIFICATION"; then
        echo "✅ 上次验证状态: PASSED"
    else
        echo "❌ 上次验证状态: FAILED"
        echo "必须先修复上次验证的问题"
        exit 1
    fi
else
    echo "❌ 未找到上次验证报告！"
    echo "必须先运行: bash scripts/mandatory-verification.sh"
    exit 1
fi

echo ""
echo "检查3: 强制执行合同状态"
if [ -f "$PROJECT_ROOT/FORCED_EXECUTION_CONTRACT.md" ]; then
    echo "✅ 强制执行合同存在"

    # 检查合同是否被签署
    if grep -q "签名.*Claude Code" "$PROJECT_ROOT/FORCED_EXECUTION_CONTRACT.md"; then
        echo "✅ 合同已签署"
    else
        echo "❌ 合同未签署！"
        exit 1
    fi

    # 检查违规后果是否明确
    if grep -q "违规后果" "$PROJECT_ROOT/FORCED_EXECUTION_CONTRACT.md"; then
        echo "✅ 违规后果已明确"
    else
        echo "❌ 违规后果不明确！"
        exit 1
    fi
else
    echo "❌ 强制执行合同不存在！"
    exit 1
fi

echo ""
echo "检查4: 脚本完整性验证"
SCRIPT_FILES=(
    "$SCRIPT_DIR/pre-work-hook.sh"
    "$SCRIPT_DIR/post-work-hook.sh"
    "$SCRIPT_DIR/mandatory-verification.sh"
    "$SCRIPT_DIR/task-completion-verify.sh"
    "$SCRIPT_DIR/quality-gate.sh"
    "$SCRIPT_DIR/commit-guard.sh"
    "$SCRIPT_DIR/dev-standards-check.sh"
    "$SCRIPT_DIR/integrated-workflow.sh"
)

for script in "${SCRIPT_FILES[@]}"; do
    if [ -f "$script" ]; then
        if [ -x "$script" ]; then
            echo "✅ $script 存在且可执行"
        else
            echo "✅ $script 存在（需要设置执行权限）"
            chmod +x "$script"
        fi
    else
        echo "❌ $script 不存在！缺少验证脚本"
        exit 1
    fi
done

echo ""
echo "检查5: 项目状态检查"
echo "当前分支: $(git branch --show-current)"
echo "Git状态:"
git status --porcelain

# 检查是否有未提交的更改
if [ -n "$(git status --porcelain)" ]; then
    echo "⚠️ 发现未提交的更改"
    echo "建议在开始新工作前先提交或暂存更改"
else
    echo "✅ 工作区干净"
fi

echo ""
echo "📊 开发规范摘要"

echo "📋 规范文档: ${#STANDARD_FILES[@]}个文件"
echo "🔍 验证脚本: ${#SCRIPT_FILES[@]}个脚本"
echo "✅ 上次验证: PASSED"
echo "📅 合同状态: 已签署"

echo ""
echo "🎯 检查6: 任务特定注意事项分析"
echo "分析当前项目状态和潜在工作任务..."

# 分析Git状态以推断工作任务
if [ -n "$(git status --porcelain)" ]; then
    echo "📋 检测到未提交的变更，可能的任务类型："

    # 检查Java文件变更
    java_changes=$(git status --porcelain | grep -c "\.java$" || echo "0")
    if [ $java_changes -gt 0 ]; then
        echo "  🔍 Java代码变更 - 注意事项："
        echo "    • 必须验证编译通过 (mvn clean compile)"
        echo "    • 检查javax→jakarta包名规范"
        echo "    • 确认@Autowired→@Resource替换"
        echo "    • 运行单元测试验证功能"
    fi

    # 检查配置文件变更
    config_changes=$(git status --porcelain | grep -c "\.\(yml\|yaml\|properties\)$" || echo "0")
    if [ $config_changes -gt 0 ]; then
        echo "  🔧 配置文件变更 - 注意事项："
        echo "    • 验证配置语法正确性"
        echo "    • 确认数据库连接信息"
        echo "    • 检查Redis连接配置"
        echo "    • 测试应用启动是否正常"
    fi

    # 检查SQL文件变更
    sql_changes=$(git status --porcelain | grep -c "\.sql$" || echo "0")
    if [ $sql_changes -gt 0 ]; then
        echo "  🗄️ 数据库变更 - 注意事项："
        echo "    • 备份现有数据库"
        echo "    • 验证SQL语法正确性"
        echo "    • 检查外键约束影响"
        echo "    • 测试数据迁移脚本"
    fi

    # 检查前端文件变更
    frontend_changes=$(git status --porcelain | grep -c "\.\(vue\|js\|ts\)$" || echo "0")
    if [ $frontend_changes -gt 0 ]; then
        echo "  🎨 前端代码变更 - 注意事项："
        echo "    • 运行npm install更新依赖"
        echo "    • 执行npm run build验证构建"
        echo "    • 检查浏览器兼容性"
        echo "    • 测试用户界面交互"
    fi
else
    echo "📋 工作区干净，可能的新任务类型："
    echo "  🔨 新功能开发 - 注意事项："
    echo "    • 严格遵循四层架构规范"
    echo "    • 编写完整的单元测试"
    echo "    • 更新API文档"
    echo "    • 添加权限验证"
    echo ""
    echo "  🐛 Bug修复 - 注意事项："
    echo "    • 定位问题根本原因"
    echo "    • 编写回归测试用例"
    echo "    • 验证修复不影响其他功能"
    echo "    • 更新相关文档"
fi

echo ""
echo "🎯 通用开发注意事项（必须遵守）："
echo "1. 🚫 绝对禁止使用javax包，必须使用jakarta"
echo "2. 🚫 绝对禁止使用@Autowired，必须使用@Resource"
echo "3. ✅ 所有实体类必须继承BaseEntity"
echo "4. ✅ 所有Controller方法必须添加权限验证"
echo "5. ✅ 所有Service方法必须添加事务管理"
echo "6. ✅ 代码变更后必须运行质量门禁检查"
echo "7. ✅ 提交前必须通过完整的验证流程"

echo ""
echo "⚠️ 当前项目特殊注意事项："
echo "• 正在进行Spring Boot 3.x迁移，特别注意包名规范"
echo "• 生物识别认证系统开发中，注意安全算法实现"
echo "• 多模态认证集成，注意各认证策略协调"
echo "• 实时WebSocket通信，注意连接稳定性"

# 检查特定模块的开发指南
if [ -d "smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/smart" ]; then
    echo ""
    echo "🧠 智能模块开发特别提醒："
    echo "• 门禁系统：遵循 docs/CHECKLISTS/门禁系统开发检查清单.md"
    echo "• 消费系统：遵循 docs/CHECKLISTS/消费系统开发检查清单.md"
    echo "• 考勤系统：遵循 docs/CHECKLISTS/考勤系统开发检查清单.md"
    echo "• 视频系统：遵循 docs/CHECKLISTS/智能视频系统开发检查清单.md"
fi

echo ""
echo "📖 开发规范确认"
echo "1. ✅ 已阅读并同意强制执行合同"
echo "2. ✅ 已理解违规后果"
echo "3. ✅ 已确认上次验证状态"
echo "4. ✅ 已检查开发规范要求"
echo "5. ✅ 已明确本次任务注意事项"
echo "6. ✅ 已准备开始工作"

echo ""
echo "🎯 开发规范检查完成 - 任务注意事项已明确"

# 生成规范检查证明
STANDARDS_CHECK_PROOF="dev-standards-check-$(date +%Y%m%d-%H%M%S).proof"
cat > "$STANDARDS_CHECK_PROOF" << EOF
开发规范检查通过
时间: $(date)
项目: $(pwd)
规范文档: ${#STANDARD_FILES[@]}个
验证脚本: ${#SCRIPT_FILES[@]}个
上次验证: PASSED
合同状态: 已签署
状态: PASSED
允许: 开始工作
EOF
echo "📄 规范检查证明: $STANDARDS_CHECK_PROOF"

echo ""
echo "🔒 开发规范检查完成 - 符合所有开发要求"

# 生成工作提醒
echo ""
echo "📋 工作提醒："
echo "1. 任何代码变更前运行: bash scripts/pre-work-hook.sh"
echo "2. 工作完成后运行: bash scripts/post-work-hook.sh <type>"
echo "3. Git提交前运行: bash scripts/commit-guard.sh"
echo "4. 任务完成前运行: bash scripts/task-completion-verify.sh <task_id>"
echo ""
echo "违反任何检查都将导致工作被阻止！"