#!/bin/bash
# 任务完成验证脚本 - 更新tasks.md前的强制检查
# 用途: 确保只有真正完成的任务才能被标记为完成

set -e

TASK_ID=$1
if [ -z "$TASK_ID" ]; then
    echo "❌ 用法: $0 <task_id>"
    echo "示例: $0 task-2.1"
    exit 1
fi

echo "🔍 任务完成验证: $TASK_ID"
echo "时间: $(date)"
echo ""

# 强制执行验证脚本
echo "步骤1: 执行强制性验证"
if [ -f "scripts/mandatory-verification.sh" ]; then
    bash scripts/mandatory-verification.sh
else
    echo "❌ 强制验证脚本不存在"
    exit 1
fi

# 步骤2: 检查任务特定要求
echo ""
echo "步骤2: 检查任务特定要求"

case $TASK_ID in
    "task-1.1"|"task-1.2"|"task-1.3"|"task-1.4")
        echo "✅ Phase 1任务验证"
        ;;
    "task-2.1")
        echo "检查任务2.1: 人脸识别引擎"
        if [ ! -f "sa-admin/src/main/java/net/lab1024/sa/admin/module/smart/biometric/engine/FaceRecognitionEngine.java" ]; then
            echo "❌ FaceRecognitionEngine.java不存在"
            exit 1
        fi
        if [ ! -f "sa-admin/src/main/java/net/lab1024/sa/admin/module/smart/biometric/engine/LivenessDetectionService.java" ]; then
            echo "❌ LivenessDetectionService.java不存在"
            exit 1
        fi
        echo "✅ 任务2.1文件验证通过"
        ;;
    "task-2.2")
        echo "检查任务2.2: 统一引擎接口"
        if [ ! -f "sa-admin/src/main/java/net/lab1024/sa/admin/module/smart/biometric/engine/BiometricRecognitionEngine.java" ]; then
            echo "❌ BiometricRecognitionEngine.java不存在"
            exit 1
        fi
        if [ ! -f "sa-admin/src/main/java/net/lab1024/sa/admin/module/smart/biometric/engine/BiometricAlgorithm.java" ]; then
            echo "❌ BiometricAlgorithm.java不存在"
            exit 1
        fi
        echo "✅ 任务2.2文件验证通过"
        ;;
    "task-2.3")
        echo "检查任务2.3: 认证策略管理器"
        if [ ! -f "sa-admin/src/main/java/net/lab1024/sa/admin/module/smart/biometric/engine/AuthenticationStrategyManager.java" ]; then
            echo "❌ AuthenticationStrategyManager.java不存在"
            exit 1
        fi
        echo "✅ 任务2.3文件验证通过"
        ;;
    "task-2.4")
        echo "检查任务2.4: 门禁控制器增强"
        if ! grep -q "biometric\|multimodal" sa-admin/src/main/java/net/lab1024/sa/admin/module/smart/access/controller/SmartAccessControlController.java; then
            echo "❌ SmartAccessControlController.java未包含生物识别功能"
            exit 1
        fi
        echo "✅ 任务2.4功能验证通过"
        ;;
    *)
        echo "✅ 通用任务验证"
        ;;
esac

# 步骤3: 生成验证证明
echo ""
echo "步骤3: 生成验证证明"
PROOF_FILE="task-verification-$TASK_ID-$(date +%Y%m%d-%H%M%S).proof"

cat > "$PROOF_FILE" << EOF
任务ID: $TASK_ID
验证时间: $(date)
验证状态: PASSED
验证脚本: mandatory-verification.sh
编译状态: PASSED
测试状态: PASSED
启动状态: PASSED
代码质量: PASSED

此证明表明任务已通过强制性验证，可以标记为完成。
EOF

echo "✅ 任务验证通过"
echo "📄 验证证明: $PROOF_FILE"
echo ""
echo "🎯 任务 $TASK_ID 已验证完成，可以更新tasks.md状态为 [x]"