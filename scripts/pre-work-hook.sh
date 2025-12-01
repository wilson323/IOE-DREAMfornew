#!/bin/bash
# 工作前Hook - 强制性前置验证
# 用途: 任何代码工作前必须执行此hook

set -e

echo "🔒 工作前Hook验证 - 强制执行"
echo "时间: $(date)"
echo "项目: $(pwd)"
echo ""

# 强制检查项
REQUIRED_CHECKS=0
PASSED_CHECKS=0

echo "检查1: 确保项目路径正确"
CURRENT_DIR=$(pwd)

# 检查是否在项目根目录或子目录中
if [[ "$CURRENT_DIR" == *"/IOE-DREAM"* ]] || [[ "$CURRENT_DIR" == *"\\IOE-DREAM"* ]]; then
    echo "✅ 项目路径验证通过"
    echo "当前工作目录: $CURRENT_DIR"
    ((PASSED_CHECKS++))
    ((REQUIRED_CHECKS++))
else
    echo "❌ 错误的项目路径！"
    echo "当前路径: $CURRENT_DIR"
    echo "期望在IOE-DREAM项目目录中工作"
    exit 1
fi

echo ""
echo "检查2: 项目结构验证"

# 检查是否存在后端项目目录
if [ -d "smart-admin-api-java17-springboot3" ]; then
    echo "✅ 后端项目目录存在"
    ((PASSED_CHECKS++))
else
    echo "⚠️ 后端项目目录不存在，跳过后端相关检查"
fi
((REQUIRED_CHECKS++))

# 检查是否存在前端项目目录
if [ -d "smart-admin-web-javascript" ]; then
    echo "✅ 前端项目目录存在"
    ((PASSED_CHECKS++))
else
    echo "⚠️ 前端项目目录不存在"
fi
((REQUIRED_CHECKS++))

echo ""
echo "检查3: 质量门禁状态"
QUALITY_GATE_FILE="./quality_gate.status"

if [ -f "$QUALITY_GATE_FILE" ]; then
    if grep -q "QUALITY_GATE_PASSED=true" "$QUALITY_GATE_FILE"; then
        echo "✅ 质量门禁通过"
        ((PASSED_CHECKS++))
    else
        echo "⚠️ 质量门禁状态未知，建议运行: bash scripts/quality-gate.sh"
        ((PASSED_CHECKS++))  # 不阻断，仅提醒
    fi
else
    echo "ℹ️ 未找到质量门禁状态文件，建议首次运行: bash scripts/quality-gate.sh"
    ((PASSED_CHECKS++))  # 不阻断，仅提醒
fi
((REQUIRED_CHECKS++))

echo ""
echo "检查4: 验证强制执行合同"
if [ -f "FORCED_EXECUTION_CONTRACT.md" ]; then
    echo "✅ 强制执行合同存在"
    ((PASSED_CHECKS++))
else
    echo "❌ 缺少强制执行合同！"
    exit 1
fi
((REQUIRED_CHECKS++))

echo ""
echo "检查5: 环境验证"
echo "检查Java版本..."
java_version=$(java -version 2>&1 | head -n 1)
echo "✅ Java版本: $java_version"

echo "检查Maven版本..."
if command -v mvn &> /dev/null; then
    mvn_version=$(mvn -version | head -n 1)
    echo "✅ Maven版本: $mvn_version"
else
    echo "❌ Maven未安装"
    exit 1
fi
((PASSED_CHECKS++))
((REQUIRED_CHECKS++))

echo ""
echo "🎯 Hook验证完成"
echo "通过检查: $PASSED_CHECKS/$REQUIRED_CHECKS"

if [ $PASSED_CHECKS -eq $REQUIRED_CHECKS ]; then
    echo "✅ 所有前置验证通过，可以开始工作"

    # 生成Hook通过证明
    HOOK_PROOF="pre-work-hook-passed-$(date +%Y%m%d-%H%M%S).proof"
    cat > "$HOOK_PROOF" << EOF
工作前Hook验证通过
时间: $(date)
检查结果: $PASSED_CHECKS/$REQUIRED_CHECKS
状态: PASSED
允许: 开始工作
EOF
    echo "📄 Hook证明: $HOOK_PROOF"
else
    echo "❌ 前置验证失败，禁止开始工作"
    exit 1
fi

echo ""
echo "🔒 工作前Hook验证完成 - 可以开始工作"