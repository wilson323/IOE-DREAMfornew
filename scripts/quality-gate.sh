#!/bin/bash
# 质量门禁 - 任何代码变更前必须通过
# 用途: 强制执行，不可跳过

set -e

echo "🔒 质量门禁检查 - 强制执行"
echo "时间: $(date)"
echo ""

# 如果失败就阻止任何操作
QUALITY_GATE_PASSED=false

# 检查编译
echo "检查编译..."
if mvn clean compile -q; then
    echo "✅ 编译通过"
else
    echo "❌ 编译失败！禁止任何操作！"
    exit 1
fi

# 检查测试
echo "检查测试..."
if mvn test -q; then
    echo "✅ 测试通过"
else
    echo "❌ 测试失败！禁止任何操作！"
    exit 1
fi

# 生成质量门禁文件
echo "QUALITY_GATE_PASSED=true" > /tmp/quality_gate.status
echo "$(date)" >> /tmp/quality_gate.status

echo "✅ 质量门禁通过"
echo "门禁状态文件: /tmp/quality_gate.status"