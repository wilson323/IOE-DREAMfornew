#!/bin/bash
# IOE-DREAM 四层架构违规修复脚本
# 作者: 四层架构守护专家
# 用途: 自动检查并生成架构违规修复报告

echo "🔍 IOE-DREAM 四层架构合规性检查开始..."
echo "============================================="

# 检查结果统计
TOTAL_VIOLATIONS=0
FIXED_VIOLATIONS=0

# 1. 检查Manager层事务管理违规
echo ""
echo "📋 检查1: Manager层事务管理违规"
echo "-----------------------------------"

MANAGER_TX_FILES=$(find microservices -name "*Manager*.java" -exec grep -l "@Transactional" {} \;)
if [ -n "$MANAGER_TX_FILES" ]; then
    echo "❌ 发现Manager层事务管理违规:"
    for file in $MANAGER_TX_FILES; do
        echo "  📄 $file"
        grep -n "@Transactional" "$file" | sed 's/^/    ➤ /'
        TOTAL_VIOLATIONS=$((TOTAL_VIOLATIONS + 1))
    done
    echo ""
    echo "💡 修复建议:"
    echo "  1. 移除Manager层的@Transactional注解"
    echo "  2. 将事务管理移到对应的Service层"
    echo "  3. Manager类应该是纯Java类，不使用Spring注解"
else
    echo "✅ Manager层事务管理合规"
fi

# 2. 检查包结构违规
echo ""
echo "📋 检查2: Manager类包结构违规"
echo "-----------------------------------"

PACKAGE_VIOLATIONS=$(find microservices -name "*Manager*.java" -path "*/service/impl/*")
if [ -n "$PACKAGE_VIOLATIONS" ]; then
    echo "❌ 发现Manager类包结构违规:"
    for file in $PACKAGE_VIOLATIONS; do
        echo "  📄 $file"
        echo "    ➤ 应该移动到 manager/impl/ 包下"
        TOTAL_VIOLATIONS=$((TOTAL_VIOLATIONS + 1))
    done
    echo ""
    echo "💡 修复建议:"
    echo "  1. 将Manager类移动到 manager/impl/ 包下"
    echo "  2. 更新package声明"
    echo "  3. 确保符合四层架构包结构规范"
else
    echo "✅ Manager类包结构合规"
fi

# 3. 检查依赖注入违规
echo ""
echo "📋 检查3: 依赖注入规范违规"
echo "-----------------------------------"

AUTOWIRED_VIOLATIONS=$(find microservices -name "*.java" -exec grep -l "@Autowired" {} \;)
if [ -n "$AUTOWIRED_VIOLATIONS" ]; then
    echo "❌ 发现@Autowired使用违规:"
    for file in $AUTOWIRED_VIOLATIONS; do
        # 排除注释中的使用
        REAL_VIOLATION=$(grep -v "// " "$file" | grep "@Autowired")
        if [ -n "$REAL_VIOLATION" ]; then
            echo "  📄 $file"
            grep -n "@Autowired" "$file" | grep -v "// " | sed 's/^/    ➤ /'
            TOTAL_VIOLATIONS=$((TOTAL_VIOLATIONS + 1))
        fi
    done
    echo ""
    echo "💡 修复建议:"
    echo "  1. 将@Autowired替换为@Resource"
    echo "  2. 统一使用@Resource依赖注入"
else
    echo "✅ 依赖注入规范合规"
fi

# 4. 检查DAO层规范违规
echo ""
echo "📋 检查4: DAO层规范违规"
echo "-----------------------------------"

REPOSITORY_VIOLATIONS=$(find microservices -name "*Dao.java" -exec grep -l "@Repository" {} \;)
if [ -n "$REPOSITORY_VIOLATIONS" ]; then
    echo "❌ 发现DAO层@Repository违规:"
    for file in $REPOSITORY_VIOLATIONS; do
        # 排除注释中的使用
        REAL_VIOLATION=$(grep -v "// " "$file" | grep "@Repository")
        if [ -n "$REAL_VIOLATION" ]; then
            echo "  📄 $file"
            grep -n "@Repository" "$file" | grep -v "// " | sed 's/^/    ➤ /'
            TOTAL_VIOLATIONS=$((TOTAL_VIOLATIONS + 1))
        fi
    done
    echo ""
    echo "💡 修复建议:"
    echo "  1. 将@Repository替换为@Mapper"
    echo "  2. 确保DAO接口继承BaseMapper<Entity>"
else
    echo "✅ DAO层规范合规"
fi

# 5. 检查跨层访问违规
echo ""
echo "📋 检查5: 跨层访问违规"
echo "-----------------------------------"

# Controller直接调用Manager
CONTROLLER_MANAGER_VIOLATIONS=$(find microservices -name "*Controller.java" -exec grep -l "Manager.*manager" {} \;)
if [ -n "$CONTROLLER_MANAGER_VIOLATIONS" ]; then
    echo "❌ 发现Controller直接调用Manager违规:"
    for file in $CONTROLLER_MANAGER_VIOLATIONS; do
        # 排除注释中的使用
        REAL_VIOLATION=$(grep -v "// " "$file" | grep -E "Manager.*manager|@Resource.*Manager")
        if [ -n "$REAL_VIOLATION" ]; then
            echo "  📄 $file"
            grep -n -E "Manager.*manager|@Resource.*Manager" "$file" | grep -v "// " | sed 's/^/    ➤ /'
            TOTAL_VIOLATIONS=$((TOTAL_VIOLATIONS + 1))
        fi
    done
    echo ""
    echo "💡 修复建议:"
    echo "  1. Controller应该只调用Service层"
    echo "  2. 通过Service层调用Manager层"
    echo "  3. 遵循Controller→Service→Manager→DAO调用链"
else
    echo "✅ 跨层访问规范合规"
fi

# 6. 检查Jakarta EE包名违规
echo ""
echo "📋 检查6: Jakarta EE包名违规"
echo "-----------------------------------"

JAVAX_VIOLATIONS=$(find microservices -name "*.java" -exec grep -l "import javax\.(annotation|validation|persistence|servlet|transaction)" {} \;)
if [ -n "$JAVAX_VIOLATIONS" ]; then
    echo "❌ 发现javax包名违规:"
    for file in $JAVAX_VIOLATIONS; do
        echo "  📄 $file"
        grep -n "import javax\.(annotation|validation|persistence|servlet|transaction)" "$file" | sed 's/^/    ➤ /'
        TOTAL_VIOLATIONS=$((TOTAL_VIOLATIONS + 1))
    done
    echo ""
    echo "💡 修复建议:"
    echo "  1. 将javax包名替换为jakarta"
    echo "  2. javax.annotation→jakarta.annotation"
    echo "  3. javax.validation→jakarta.validation"
else
    echo "✅ Jakarta EE包名规范合规"
fi

# 生成合规性评分
echo ""
echo "📊 架构合规性评估"
echo "============================================="

if [ $TOTAL_VIOLATIONS -eq 0 ]; then
    COMPLIANCE_SCORE=100
    GRADE="🟢 优秀"
elif [ $TOTAL_VIOLATIONS -le 2 ]; then
    COMPLIANCE_SCORE=90
    GRADE="🟡 良好"
elif [ $TOTAL_VIOLATIONS -le 5 ]; then
    COMPLIANCE_SCORE=75
    GRADE="🟠 中等"
else
    COMPLIANCE_SCORE=60
    GRADE="🔴 需要改进"
fi

echo "📈 合规性评分: $COMPLIANCE_SCORE/100 ($GRADE)"
echo "🔍 发现违规: $TOTAL_VIOLATIONS 处"
echo "✅ 已修复违规: $FIXED_VIOLATIONS 处"

# 生成修复优先级建议
echo ""
echo "🎯 修复优先级建议"
echo "============================================="

if [ $TOTAL_VIOLATIONS -gt 0 ]; then
    echo "🔴 P1级 (立即修复):"
    echo "  - Manager层事务管理违规"
    echo "  - 跨层访问违规"
    echo ""
    echo "🟡 P2级 (建议修复):"
    echo "  - 包结构违规"
    echo "  - 依赖注入规范违规"
    echo ""
    echo "🟢 P3级 (持续改进):"
    echo "  - 代码注释规范"
    echo "  - 架构文档更新"
else
    echo "🎉 恭喜！未发现架构违规问题"
fi

echo ""
echo "🔗 相关文档:"
echo "  - CLAUDE.md - 全局架构规范"
echo "  - FOUR_LAYER_ARCHITECTURE_COMPLIANCE_REPORT.md - 详细合规性报告"
echo ""
echo "✨ 检查完成！请根据建议进行修复。"
echo "============================================="

# 退出码
if [ $TOTAL_VIOLATIONS -gt 0 ]; then
    exit 1  # 有违规问题
else
    exit 0  # 完全合规
fi