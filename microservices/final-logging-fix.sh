#!/bin/bash
# 最终日志规范修复脚本

echo "🎯 执行最终日志规范修复..."

# 修复所有缺少@Slf4j的文件
echo "🔧 修复缺少@Slf4j注解的文件..."

find . -name "*.java" -exec grep -l "log\." {} \; | xargs grep -L "@Slf4j" 2>/dev/null | while read file; do
    echo "🔄 修复文件: $file"

    # 添加import（如果不存在）
    if ! grep -q "import lombok\.extern\.slf4j\.Slf4j" "$file"; then
        sed -i '/^package/a import lombok.extern.slf4j.Slf4j;' "$file"
    fi

    # 添加@Slf4j注解
    CLASS_LINE=$(grep -n -m1 "^@\|^public class" "$file" | cut -d: -f1)
    if [ -n "$CLASS_LINE" ]; then
        sed -i "${CLASS_LINE}a @Slf4j" "$file"
    fi

    echo "  ✅ 已修复"
done

echo ""
echo "🎉 最终日志规范修复完成！"

# 验证结果
echo "📊 验证修复结果..."
REMAINING=$(find . -name "*.java" -exec grep -l "log\." {} \; | xargs grep -L "@Slf4j" 2>/dev/null | wc -l)
echo "📈 统计结果:"
echo "  📉 剩余问题文件: $REMAINING"

if [ $REMAINING -eq 0 ]; then
    echo "🎉 所有日志规范问题已修复！"
else
    echo "⚠️ 仍有 $REMAINING 个文件需要检查:"
    find . -name "*.java" -exec grep -l "log\." {} \; | xargs grep -L "@Slf4j" 2>/dev/null
fi