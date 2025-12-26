#!/bin/bash
# IOE-DREAM 企业级Import语句修复脚本
# 修复所有不完整的junit import语句

echo "🚀 开始企业级Import语句修复..."
echo "📊 发现 $(find . -name "*.java" -exec grep -l "import static org\.ju" {} \; | wc -l) 个需要修复的文件"

# 统计修复前的文件数量
BEFORE_COUNT=$(find . -name "*.java" -exec grep -l "import static org\.ju" {} \; | wc -l)
echo "📈 修复前问题文件数: $BEFORE_COUNT"

# 修复策略1: 修复截断的junit import
echo "🔧 正在修复截断的junit import语句..."
find . -name "*.java" -type f -exec sed -i.bak 's/^import static org\.ju\s*$/import static org.junit.jupiter.api.Assertions.*;/g' {} \;

# 修复策略2: 修复不完整的Assertions导入
echo "🔧 正在修复不完整的Assertions导入语句..."
find . -name "*.java" -type f -exec sed -i 's/^import static org\.junit\.jupiter\.api\.Assertions\s*$/import static org.junit.jupiter.api.Assertions.*;/g' {} \;

# 修复策略3: 删除重复的Assertions导入
echo "🔧 正在删除重复的Assertions导入..."
find . -name "*.java" -type f -exec sed -i '/import static org\.junit\.jupiter\.api\.Assertions\.\*;/{
N
/import static org\.junit\.jupiter\.api\.Assertions\.\*;/d
}' {} \;

# 清理备份文件
echo "🧹 清理临时备份文件..."
find . -name "*.bak" -delete

# 统计修复后的文件数量
AFTER_COUNT=$(find . -name "*.java" -exec grep -l "import static org\.ju" {} \; 2>/dev/null | wc -l)
echo "📉 修复后问题文件数: $AFTER_COUNT"
echo "✅ 修复完成! 修复了 $((BEFORE_COUNT - AFTER_COUNT)) 个文件"

# 验证结果
if [ $AFTER_COUNT -eq 0 ]; then
    echo "🎉 所有问题文件已修复!"
else
    echo "⚠️  仍有 $AFTER_COUNT 个文件需要手动检查:"
    find . -name "*.java" -exec grep -l "import static org\.ju" {} \; 2>/dev/null
fi

echo "📋 修复报告生成完成: $(date)"