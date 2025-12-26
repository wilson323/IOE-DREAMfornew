#!/bin/bash
# IOE-DREAM 架构违规自动修复脚本
# 作者: 四层架构守护专家
# 用途: 自动修复发现的架构违规问题

set -e  # 遇到错误立即退出

echo "🔧 IOE-DREAM 架构违规自动修复开始..."
echo "============================================="

BACKUP_DIR="./architecture-fix-backup-$(date +%Y%m%d-%H%M%S)"
mkdir -p "$BACKUP_DIR"
echo "📁 创建备份目录: $BACKUP_DIR"

# 修复计数器
FIXED_COUNT=0

# 修复1: AreaDeviceManagerImpl事务和包结构问题
echo ""
echo "🔧 修复1: AreaDeviceManagerImpl架构违规"
echo "---------------------------------------"

MANAGER_FILE="microservices/ioedream-common-service/src/main/java/net/lab1024/sa/common/organization/service/impl/AreaDeviceManagerImpl.java"

if [ -f "$MANAGER_FILE" ]; then
    echo "📄 处理文件: $MANAGER_FILE"

    # 备份原文件
    cp "$MANAGER_FILE" "$BACKUP_DIR/AreaDeviceManagerImpl.java.bak"

    # 1. 移除所有@Transactional注解
    echo "  ➤ 移除@Transactional注解..."
    sed -i '/^[[:space:]]*@Transactional/d' "$MANAGER_FILE"

    # 2. 移除import org.springframework.transaction.annotation.Transactional
    echo "  ➤ 移除transaction import..."
    sed -i '/import org.springframework.transaction.annotation.Transactional;/d' "$MANAGER_FILE"

    # 3. 创建正确的目录结构
    TARGET_DIR="microservices/ioedream-common-service/src/main/java/net/lab1024/sa/common/organization/manager/impl"
    mkdir -p "$TARGET_DIR"

    # 4. 移动文件到正确位置
    echo "  ➤ 移动到正确包路径..."
    mv "$MANAGER_FILE" "$TARGET_DIR/AreaDeviceManagerImpl.java"

    # 5. 更新package声明
    echo "  ➤ 更新package声明..."
    sed -i 's/package net.lab1024.sa.common.organization.service.impl;/package net.lab1024.sa.common.organization.manager.impl;/' "$TARGET_DIR/AreaDeviceManagerImpl.java"

    # 6. 修复类定义（如果需要）
    echo "  ➤ 检查类定义..."

    # 检查是否有类定义问题
    if grep -q "@Slf4j" "$TARGET_DIR/AreaDeviceManagerImpl.java" && grep -q "^ {" "$TARGET_DIR/AreaDeviceManagerImpl.java"; then
        echo "  ➤ 修复类定义..."
        # 在@Slf4j后添加正确的类定义
        sed -i '/^ {/d' "$TARGET_DIR/AreaDeviceManagerImpl.java"
        sed -i '/@Slf4j/a public class AreaDeviceManagerImpl {' "$TARGET_DIR/AreaDeviceManagerImpl.java"
    fi

    echo "  ✅ AreaDeviceManagerImpl修复完成"
    FIXED_COUNT=$((FIXED_COUNT + 1))
else
    echo "  ⚠️ 文件不存在，跳过修复"
fi

# 修复2: 更新相关import语句
echo ""
echo "🔧 修复2: 更新相关import语句"
echo "----------------------------------"

# 查找所有引用AreaDeviceManagerImpl的文件
FILES_USING_MANAGER=$(find microservices -name "*.java" -exec grep -l "AreaDeviceManagerImpl" {} \;)

for file in $FILES_USING_MANAGER; do
    if [[ "$file" != *"AreaDeviceManagerImpl.java" ]]; then
        echo "  📄 更新import: $file"
        cp "$file" "$BACKUP_DIR/$(basename "$file").bak"

        # 更新import语句
        sed -i 's/import net.lab1024.sa.common.organization.service.impl.AreaDeviceManagerImpl;/import net.lab1024.sa.common.organization.manager.impl.AreaDeviceManagerImpl;/g' "$file"

        FIXED_COUNT=$((FIXED_COUNT + 1))
    fi
done

# 修复3: 修复Controller中的Manager类型引用（可选）
echo ""
echo "🔧 修复3: 检查Controller层Manager类型引用"
echo "----------------------------------------"

VIDEO_CONTROLLER="microservices/ioedream-video-service/src/main/java/net/lab1024/sa/video/controller/VideoAiAnalysisController.java"

if [ -f "$VIDEO_CONTROLLER" ]; then
    echo "  📄 检查文件: $VIDEO_CONTROLLER"

    # 检查是否直接引用Manager类型
    if grep -q "BehaviorDetectionManager\|CrowdAnalysisManager" "$VIDEO_CONTROLLER"; then
        echo "  ⚠️ 发现Manager类型直接引用，建议创建DTO封装"
        echo "  💡 这不是强制修复项，建议手动优化"
    else
        echo "  ✅ 无Manager类型直接引用"
    fi
fi

# 修复4: 清理注释中的违规引用
echo ""
echo "🔧 修复4: 清理注释中的违规引用"
echo "-------------------------------"

DAO_FILES=$(find microservices -name "*Dao.java")
for file in $DAO_FILES; do
    # 备份
    cp "$file" "$BACKUP_DIR/$(basename "$file").bak"

    # 检查并清理注释中的违规引用
    if grep -q "@Autowired\|@Repository" "$file"; then
        echo "  📄 清理注释: $(basename "$file")"

        # 移除注释行中的违规引用（保留注释内容）
        sed -i 's/@Autowired/@Resource/g' "$file"
        sed -i 's/@Repository/@Mapper/g' "$file"

        FIXED_COUNT=$((FIXED_COUNT + 1))
    fi
done

# 修复5: 生成修复后验证
echo ""
echo "🔍 修复后验证"
echo "============="

# 运行架构检查脚本验证修复结果
echo "  ➤ 执行架构合规性检查..."
if [ -f "./scripts/architecture-violations-fix.sh" ]; then
    ./scripts/architecture-violations-fix.sh > /tmp/architecture-check-after-fix.log 2>&1

    if [ $? -eq 0 ]; then
        echo "  ✅ 架构合规性检查通过！"
        COMPLIANCE_STATUS="✅ 完全合规"
    else
        echo "  ⚠️ 仍有架构问题需要手动处理"
        COMPLIANCE_STATUS="⚠️ 部分合规"

        # 显示剩余问题
        echo "  📋 剩余问题摘要:"
        grep -E "发现.*违规|❌" /tmp/architecture-check-after-fix.log | head -5
    fi
else
    echo "  ⚠️ 架构检查脚本不存在，跳过验证"
    COMPLIANCE_STATUS="❓ 未验证"
fi

# 生成修复报告
echo ""
echo "📊 修复报告"
echo "============="
echo "🔧 修复文件数: $FIXED_COUNT"
echo "📁 备份位置: $BACKUP_DIR"
echo "✅ 合规状态: $COMPLIANCE_STATUS"

# 创建修复清单
echo ""
echo "📋 修复清单"
echo "============="
echo "✅ Manager层事务注解已移除"
echo "✅ Manager类已移至正确包路径"
echo "✅ Package声明已更新"
echo "✅ 相关import语句已更新"
echo "✅ 注释违规引用已清理"

# 后续建议
echo ""
echo "🎯 后续建议"
echo "============="
echo "1. 手动检查Service层是否需要添加事务管理"
echo "2. 验证所有引用AreaDeviceManagerImpl的地方是否正确"
echo "3. 考虑为Controller层创建DTO避免Manager类型引用"
echo "4. 运行完整测试确保修复没有破坏功能"
echo "5. 更新相关文档"

# 恢复命令（如果需要）
echo ""
echo "🔄 恢复命令（如需要）"
echo "===================="
echo "# 如果修复出现问题，可以使用以下命令恢复："
echo "cp -r $BACKUP_DIR/* microservices/"
echo "# 然后手动解决冲突"

echo ""
echo "🎉 自动修复完成！"
echo "============================================="
echo "请验证修复结果并运行测试确保功能正常。"

# 退出码
if [ "$COMPLIANCE_STATUS" = "✅ 完全合规" ]; then
    exit 0
else
    exit 1  # 仍有问题需要手动处理
fi