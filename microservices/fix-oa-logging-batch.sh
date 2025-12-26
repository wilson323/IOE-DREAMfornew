#!/bin/bash
# OA服务日志规范快速修复脚本

echo "🔧 快速修复OA服务日志规范..."

# 查找所有使用log但缺少@Slf4j注解的文件
echo "🔍 查找需要修复的文件..."
PROBLEM_FILES=$(find ioedream-oa-service -name "*.java" -exec grep -l "log\." {} \; | xargs grep -L "@Slf4j" 2>/dev/null)

for file in $PROBLEM_FILES; do
    echo "🔄 修复文件: $file"

    # 检查是否已经有lombok import
    if ! grep -q "import lombok\.extern\.slf4j\.Slf4j" "$file"; then
        # 添加lombok import
        sed -i '/^package/a import lombok.extern.slf4j.Slf4j;' "$file"
        echo "  ✅ 添加import: lombok.extern.slf4j.Slf4j"
    fi

    # 在类声明前添加@Slf4j注解（在第一个public class或@Service/@Controller等注解后）
    # 查找第一个类声明或注解
    CLASS_LINE=$(grep -n -m1 "^@\|^public class" "$file" | cut -d: -f1)
    if [ -n "$CLASS_LINE" ]; then
        # 在类声明后添加@Slf4j注解
        sed -i "${CLASS_LINE}a @Slf4j" "$file"
        echo "  ✅ 添加@Slf4j注解"
    fi

    echo "  ✅ 已修复: $file"
done

echo ""
echo "📊 OA服务日志规范修复完成！"
echo "🚀 验证修复结果..."

# 验证结果
REMAINING=$(find ioedream-oa-service -name "*.java" -exec grep -l "log\." {} \; | xargs grep -L "@Slf4j" 2>/dev/null | wc -l)
echo "📈 修复统计:"
echo "  🔧 修复文件数: $(echo "$PROBLEM_FILES" | wc -l)"
echo "  📉 剩余问题文件: $REMAINING"

if [ $REMAINING -eq 0 ]; then
    echo "🎉 OA服务日志规范修复完成！"
else
    echo "⚠️ 仍有 $REMAINING 个文件需要手动检查"
fi