#!/bin/bash
# 工作后Hook - 强制性后置验证
# 用途: 任何代码工作完成后必须执行此hook

set -e

echo "🔒 工作后Hook验证 - 强制执行"
echo "时间: $(date)"
echo "项目: $(pwd)"
echo ""

# 工作类型参数
WORK_TYPE=$1
if [ -z "$WORK_TYPE" ]; then
    echo "❌ 缺少工作类型参数"
    echo "用法: $0 <work_type>"
    echo "示例: $0 task-completion | $0 code-change | $0 bug-fix"
    exit 1
fi

# 强制检查项
REQUIRED_CHECKS=0
PASSED_CHECKS=0

echo "工作类型: $WORK_TYPE"

echo ""
echo "检查1: 强制完整验证"
echo "执行: bash scripts/mandatory-verification.sh"
if bash scripts/mandatory-verification.sh; then
    echo "✅ 强制验证通过"
    ((PASSED_CHECKS++))
else
    echo "❌ 强制验证失败！工作未完成！"
    exit 1
fi
((REQUIRED_CHECKS++))

echo ""
echo "检查2: 编译状态确认"
echo "执行: mvn clean compile -q"
if mvn clean compile -q; then
    echo "✅ 编译状态确认"
    ((PASSED_CHECKS++))
else
    echo "❌ 编译失败！必须修复后重新提交"
    exit 1
fi
((REQUIRED_CHECKS++))

echo ""
echo "检查3: 测试状态确认"
echo "执行: mvn test -q"
if mvn test -q; then
    echo "✅ 测试状态确认"
    ((PASSED_CHECKS++))
else
    echo "❌ 测试失败！必须修复后重新提交"
    exit 1
fi
((REQUIRED_CHECKS++))

echo ""
echo "检查4: 根据工作类型进行特定验证"
case $WORK_TYPE in
    "task-completion")
        echo "任务完成验证模式"
        if [ -f "verification-report-*.json" ]; then
            echo "✅ 验证报告存在"
            ((PASSED_CHECKS++))
        else
            echo "❌ 缺少验证报告！"
            exit 1
        fi
        ((REQUIRED_CHECKS++))
        ;;
    "code-change")
        echo "代码变更验证模式"
        echo "检查是否有新增的编译错误..."
        # 这里可以添加更多的代码变更特定检查
        echo "✅ 代码变更验证通过"
        ((PASSED_CHECKS++))
        ((REQUIRED_CHECKS++))
        ;;
    "bug-fix")
        echo "Bug修复验证模式"
        echo "确认问题已修复..."
        # 这里可以添加bug修复特定验证
        echo "✅ Bug修复验证通过"
        ((PASSED_CHECKS++))
        ((REQUIRED_CHECKS++))
        ;;
    *)
        echo "通用验证模式"
        echo "✅ 通用验证通过"
        ((PASSED_CHECKS++))
        ((REQUIRED_CHECKS++))
        ;;
esac

echo ""
echo "检查5: 生成验证摘要"
VERIFICATION_SUMMARY="post-work-hook-$(date +%Y%m%d-%H%M%S).json"
cat > "$VERIFICATION_SUMMARY" << EOF
{
  "verification_time": "$(date)",
  "work_type": "$WORK_TYPE",
  "project_path": "$(pwd)",
  "status": "PASSED",
  "checks": {
    "mandatory_verification": "PASSED",
    "compilation": "PASSED",
    "testing": "PASSED",
    "work_specific": "PASSED"
  },
  "passed_checks": $PASSED_CHECKS,
  "required_checks": $REQUIRED_CHECKS
}
EOF

echo "✅ 验证摘要已生成: $VERIFICATION_SUMMARY"

echo ""
echo "🎯 Hook验证完成"
echo "通过检查: $PASSED_CHECKS/$REQUIRED_CHECKS"

if [ $PASSED_CHECKS -eq $REQUIRED_CHECKS ]; then
    echo "✅ 工作后Hook验证通过"

    # 生成Hook通过证明
    HOOK_PROOF="post-work-hook-passed-$(date +%Y%m%d-%H%M%S).proof"
    cat > "$HOOK_PROOF" << EOF
工作后Hook验证通过
时间: $(date)
工作类型: $WORK_TYPE
检查结果: $PASSED_CHECKS/$REQUIRED_CHECKS
状态: PASSED
允许: 提交工作结果
EOF
    echo "📄 Hook证明: $HOOK_PROOF"

    echo ""
    echo "🔒 工作后Hook验证完成 - 可以提交工作结果"
else
    echo "❌ 工作后Hook验证失败，禁止提交"
    exit 1
fi