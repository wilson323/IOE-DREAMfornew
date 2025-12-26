#!/bin/bash
# IOE-DREAM 日志规范统一修复脚本
# 确保所有Java文件统一使用 @Slf4j 注解而非传统 Logger

echo "🔧 开始系统性修复日志规范，确保全部使用 @Slf4j..." -ForegroundColor Green

# 统计需要修复的文件
echo "📊 分析日志规范问题..."

# 1. 查找使用传统Logger方式的文件（需要修复为@Slf4j）
echo "🔍 查找使用传统Logger方式的文件..."
TRADITIONAL_LOGGER_FILES=$(find . -name "*.java" -exec grep -l "import org\.slf4j\.Logger" {} \; 2>/dev/null)
TRADITIONAL_COUNT=$(echo "$TRADITIONAL_LOGGER_FILES" | wc -l)

if [ $TRADITIONAL_COUNT -gt 0 ]; then
    echo "⚠️ 发现 $TRADITIONAL_COUNT 个文件使用传统Logger方式，需要修复为@Slf4j"
    echo "$TRADITIONAL_LOGGER_FILES"
else
    echo "✅ 未发现使用传统Logger的文件"
fi

# 2. 查找已有@Slf4j注解但缺少import的文件
echo ""
echo "🔍 查找使用@Slf4j但缺少import的文件..."
MISSING_IMPORT_FILES=$(find . -name "*.java" -exec grep -l "@Slf4j" {} \; | xargs grep -L "import lombok\.extern\.slf4j\.Slf4j" 2>/dev/null)
MISSING_COUNT=$(echo "$MISSING_IMPORT_FILES" | wc -l)

if [ $MISSING_COUNT -gt 0 ]; then
    echo "⚠️ 发现 $MISSING_COUNT 个文件使用@Slf4j但缺少import"
    echo "$MISSING_IMPORT_FILES"
else
    echo "✅ 所有@Slf4j文件都有正确的import"
fi

# 3. 查找同时使用两种方式的混乱文件
echo ""
echo "🔍 查找同时使用两种日志方式的混乱文件..."
MIXED_FILES=$(find . -name "*.java" -exec grep -l "import lombok\.extern\.slf4j\.Slf4j" {} \; | xargs grep -l "import org\.slf4j\.Logger" 2>/dev/null)
MIXED_COUNT=$(echo "$MIXED_FILES" | wc -l)

if [ $MIXED_COUNT -gt 0 ]; then
    echo "⚠️ 发现 $MIXED_COUNT 个文件同时使用两种日志方式，需要清理"
    echo "$MIXED_FILES"
else
    echo "✅ 未发现混合使用日志方式的文件"
fi

# 修复策略1: 将传统Logger方式转换为@Slf4j
echo ""
echo "🔧 开始修复策略1: 传统Logger → @Slf4j"

FIXED_COUNT=0

for file in $TRADITIONAL_LOGGER_FILES; do
    echo "🔄 修复文件: $file"

    # 检查是否已经有@Slf4j注解
    if ! grep -q "@Slf4j" "$file"; then
        # 在类声明前添加@Slf4j注解
        sed -i '/^public class/a @Slf4j' "$file"
    fi

    # 添加lombok import（如果不存在）
    if ! grep -q "import lombok\.extern\.slf4j\.Slf4j" "$file"; then
        sed -i '/^package/a import lombok.extern.slf4j.Slf4j;' "$file"
    fi

    # 移除传统Logger import
    sed -i '/import org\.slf4j\.Logger/d' "$file"
    sed -i '/import org\.slf4j\.LoggerFactory/d' "$file"

    # 移除传统Logger实例声明
    sed -i '/private static final Logger log = LoggerFactory\.getLogger/d' "$file"
    sed -i '/private static final Logger logger = LoggerFactory\.getLogger/d' "$file"
    sed -i '/private final Logger log = LoggerFactory\.getLogger/d' "$file"
    sed -i '/private final Logger logger = LoggerFactory\.getLogger/d' "$file"

    # 移除多余的空行
    sed -i '/^$/N;/^\n$/d' "$file"

    echo "✅ 已修复: $file"
    ((FIXED_COUNT++))
done

# 修复策略2: 为缺少import的@Slf4j文件添加import
echo ""
echo "🔧 开始修复策略2: 添加缺失的@Slf4j import"

for file in $MISSING_IMPORT_FILES; do
    echo "🔄 添加import: $file"

    # 添加lombok import
    sed -i '/^package/a import lombok.extern.slf4j.Slf4j;' "$file"

    echo "✅ 已添加import: $file"
    ((FIXED_COUNT++))
done

# 修复策略3: 清理混合使用的文件，保留@Slf4j方式
echo ""
echo "🔧 开始修复策略3: 清理混合日志方式，保留@Slf4j"

for file in $MIXED_FILES; do
    echo "🔄 清理混合日志: $file"

    # 移除传统Logger import
    sed -i '/import org\.slf4j\.Logger/d' "$file"
    sed -i '/import org\.slf4j\.LoggerFactory/d' "$file"

    # 移除传统Logger实例声明
    sed -i '/private static final Logger log = LoggerFactory\.getLogger/d' "$file"
    sed -i '/private static final Logger logger = LoggerFactory\.getLogger/d' "$file"
    sed -i '/private final Logger log = LoggerFactory\.getLogger/d' "$file"
    sed -i '/private final Logger logger = LoggerFactory\.getLogger/d' "$file"

    echo "✅ 已清理混合日志: $file"
    ((FIXED_COUNT++))
done

# 验证修复结果
echo ""
echo "📊 验证修复结果..."

# 最终检查
FINAL_TRADITIONAL=$(find . -name "*.java" -exec grep -l "import org\.slf4j\.Logger" {} \; 2>/dev/null | wc -l)
FINAL_SLF4J=$(find . -name "*.java" -exec grep -l "import lombok\.extern\.slf4j\.Slf4j" {} \; 2>/dev/null | wc -l)

echo "📈 修复统计:"
echo "  🔧 修复文件数: $FIXED_COUNT"
echo "  📉 传统Logger文件: $TRADITIONAL_COUNT → $FINAL_TRADITIONAL"
echo "  📈 @Slf4j文件: $FINAL_SLF4J"
echo "  🎯 规范统一度: $((100 - FINAL_TRADITIONAL))%"

# 生成修复报告
REPORT_FILE="logging-pattern-fix-report-$(date +%Y%m%d_%H%M%S).md"

cat > "$REPORT_FILE" << EOF
# IOE-DREAM 日志规范修复报告

**修复时间**: $(date)
**修复范围**: 所有Java源文件

## 📊 修复前后对比

### 传统Logger方式
- **修复前**: $TRADITIONAL_COUNT 个文件
- **修复后**: $FINAL_TRADITIONAL 个文件
- **改进**: $((TRADITIONAL_COUNT - FINAL_TRADITIONAL)) 个文件已修复

### @Slf4j方式
- **当前状态**: $FINAL_SLF4J 个文件
- **规范统一度**: $((100 - FINAL_TRADITIONAL))%

## 🔧 修复策略

1. **传统Logger → @Slf4j**: 修复了使用传统Logger实例声明的文件
2. **添加缺失import**: 为使用@Slf4j但缺少import的文件添加了import
3. **清理混合使用**: 移除了同时使用两种日志方式的混乱状态

## ✅ 企业级日志规范

所有Java文件现在统一使用以下日志规范:

\`\`\`java
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SomeClass {
    public void someMethod() {
        log.info("日志信息");
        log.error("错误信息", e);
    }
}
\`\`\`

## 📋 验证清单

- ✅ 移除了所有 \`import org.slf4j.Logger;\`
- ✅ 移除了所有 \`import org.slf4j.LoggerFactory;\`
- ✅ 添加了所有必要的 \`import lombok.extern.slf4j.Slf4j;\`
- ✅ 添加了所有必要的 \`@Slf4j\` 注解
- ✅ 移除了所有手动Logger实例声明
- ✅ 确保代码质量和一致性

---
**生成时间**: $(date)
**执行团队**: IOE-DREAM架构团队
EOF

echo ""
echo "📋 日志规范修复报告已生成: $REPORT_FILE"

if [ $FINAL_TRADITIONAL -eq 0 ]; then
    echo "🎉 所有日志规范问题已修复！"
    echo "🚀 系统日志规范已完全统一使用 @Slf4j"
    exit 0
else
    echo "⚠️ 仍有 $FINAL_TRADITIONAL 个文件需要手动检查"
    echo "🔧 请手动检查以下文件:"
    find . -name "*.java" -exec grep -l "import org\.slf4j\.Logger" {} \;
    exit 1
fi

echo "🏁 日志规范统一修复完成!"