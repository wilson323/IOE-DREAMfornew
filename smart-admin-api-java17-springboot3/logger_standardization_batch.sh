#!/bin/bash

# Logger标准化批量处理脚本
# 作者: Logger标准化专家
# 目的: 批量将所有使用@Slf4j的Java文件转换为标准Logger模板

echo "🚀 开始Logger标准化批量处理..."

# 标准Logger模板
STANDARD_LOGGER_IMPORT='import org.slf4j.Logger;
import org.slf4j.LoggerFactory;'

STANDARD_LOGGER_DECLARATION='// @Slf4j - 手动添加log变量替代Lombok注解'

LOGGER_FIELD_DECLARATION='private static final Logger log = LoggerFactory.getLogger(ClassName.class);'

# 找出所有使用@Slf4j的文件
echo "📋 扫描使用@Slf4j的Java文件..."
SLF4J_FILES=$(find . -name "*.java" -exec grep -l "@Slf4j" {} \;)

echo "📊 找到 $(echo "$SLF4J_FILES" | wc -l) 个使用@Slf4j的文件"

# 计数器
PROCESSED_COUNT=0
SUCCESS_COUNT=0
ERROR_COUNT=0

# 处理每个文件
for file in $SLF4J_FILES; do
    echo "🔧 处理文件: $file"
    PROCESSED_COUNT=$((PROCESSED_COUNT + 1))

    # 获取类名（从文件路径中提取）
    CLASS_NAME=$(basename "$file" .java)

    # 备份原文件
    cp "$file" "$file.backup"

    # 1. 删除 lombok.extern.slf4j.Slf4j import
    sed -i '/import lombok\.extern\.slf4j\.Slf4j;/d' "$file"

    # 2. 添加标准Logger imports（在package之后）
    if ! grep -q "import org.slf4j.Logger;" "$file"; then
        sed -i "/package/a\\
\\
import org.slf4j.Logger;\\
import org.slf4j.LoggerFactory;" "$file"
    fi

    # 3. 替换@Slf4j注解
    sed -i 's/@Slf4j/\/\/ @Slf4j - 手动添加log变量替代Lombok注解/g' "$file"

    # 4. 在类中添加Logger变量（在第一个类声明之后）
    if ! grep -q "private static final Logger log" "$file"; then
        # 查找类声明并添加Logger变量
        sed -i "/class ${CLASS_NAME}/a\\
    private static final Logger log = LoggerFactory.getLogger(${CLASS_NAME}.class);" "$file"
    fi

    # 验证处理结果
    if grep -q "import lombok.extern.slf4j.Slf4j" "$file"; then
        echo "❌ 错误: $file 仍然包含lombok slf4j import"
        ERROR_COUNT=$((ERROR_COUNT + 1))
        # 恢复备份
        mv "$file.backup" "$file"
    elif grep -q "private static final Logger log" "$file"; then
        echo "✅ 成功: $file Logger标准化完成"
        SUCCESS_COUNT=$((SUCCESS_COUNT + 1))
        # 删除备份
        rm "$file.backup"
    else
        echo "⚠️  警告: $file Logger定义可能有问题"
        ERROR_COUNT=$((ERROR_COUNT + 1))
        mv "$file.backup" "$file"
    fi

    echo "---"
done

echo ""
echo "📈 处理完成统计:"
echo "总文件数: $PROCESSED_COUNT"
echo "成功处理: $SUCCESS_COUNT"
echo "处理失败: $ERROR_COUNT"
echo "成功率: $(( SUCCESS_COUNT * 100 / PROCESSED_COUNT ))%"

# 验证结果
echo ""
echo "🔍 验证处理结果..."

REMAINING_SLF4J=$(find . -name "*.java" -exec grep -l "@Slf4j" {} \; | wc -l)
REMAINING_LOMBOK=$(find . -name "*.java" -exec grep -l "import lombok.extern.slf4j.Slf4j" {} \; | wc -l)
STANDARD_LOGGER_FILES=$(find . -name "*.java" -exec grep -l "private static final Logger log" {} \; | wc -l)

echo "剩余@Slf4j文件: $REMAINING_SLF4J"
echo "剩余lombok slf4j import: $REMAINING_LOMBOK"
echo "标准Logger文件数: $STANDARD_LOGGER_FILES"

if [ $REMAINING_SLF4J -eq 0 ] && [ $REMAINING_LOMBOK -eq 0 ]; then
    echo "🎉 Logger标准化批量处理成功完成！"
    exit 0
else
    echo "⚠️  仍有文件需要手动处理"
    exit 1
fi