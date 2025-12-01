#!/bin/bash

# 批量修复@Slf4j注解为标准Logger定义的脚本
# 作者: IOE-DREAM代码质量守护专家
# 创建时间: 2025-11-23

echo "🔧 开始系统性修复@Slf4j注解..."
echo "📊 总计需要修复的文件数量: 173"

# 统计变量
total_files=173
fixed_count=0
error_count=0

# 创建临时目录
temp_dir="temp_logger_fix"
mkdir -p "$temp_dir"

# 读取所有需要修复的文件列表
files=$(find "D:\IOE-DREAM\smart-admin-api-java17-springboot3" -name "*.java" -exec grep -l "@Slf4j" {} \;)

for file_path in $files; do
    echo ""
    echo "🔄 正在处理: $file_path"

    # 获取相对路径
    relative_path=${file_path#D:\\IOE-DREAM\\}

    # 获取类名
    class_name=$(basename "$file_path" .java)

    echo "📝 类名: $class_name"

    # 创建临时文件
    temp_file="$temp_dir/${class_name}_temp.java"

    # 开始修复文件
    echo "🔨 执行修复操作..."

    # 1. 读取原文件内容
    original_content=$(cat "$file_path")

    # 2. 移除lombok.extern.slf4j import
    modified_content=$(echo "$original_content" | sed '/import lombok\.extern\.slf4j\.Slf4j;/d')

    # 3. 替换@Slf4j注解为注释
    modified_content=$(echo "$modified_content" | sed 's/@Slf4j/\/\/ @Slf4j - 手动添加log变量替代Lombok注解/g')

    # 4. 添加SLF4J imports（在package声明之后）
    package_line=$(echo "$modified_content" | grep -n "^package " | cut -d: -f1)
    if [ -n "$package_line" ]; then
        # 插入import语句在package之后
        modified_content=$(echo "$modified_content" | sed "${package_line}a\\
\\
import org.slf4j.Logger;\\
import org.slf4j.LoggerFactory;")
    fi

    # 5. 添加Logger定义（在类声明之后）
    class_line=$(echo "$modified_content" | grep -n "public class $class_name" | cut -d: -f1)
    if [ -n "$class_line" ]; then
        # 查找类定义的下一行（跳过可能的注解）
        next_line=$((class_line + 1))

        # 在类声明后添加Logger定义
        logger_declaration="    // @Slf4j - 手动添加log变量替代Lombok注解\
    private static final Logger log = LoggerFactory.getLogger(${class_name}.class);"

        modified_content=$(echo "$modified_content" | sed "${class_line}a\\
\\
${logger_declaration}")
    fi

    # 6. 检查是否已经存在Logger定义（避免重复）
    if echo "$modified_content" | grep -q "LoggerFactory.getLogger.*class"; then
        echo "⚠️ 警告: 文件中已存在Logger定义，跳过添加"
        # 如果已经存在，则不添加新的Logger定义
        modified_content=$(echo "$original_content" | sed '/import lombok\.extern\.slf4j\.Slf4j;/d' | sed 's/@Slf4j/\/\/ @Slf4j - 手动添加log变量替代Lombok注解/g')
    fi

    # 7. 写入修复后的内容
    echo "$modified_content" > "$file_path"

    # 8. 验证修复结果
    echo "✅ 验证修复结果:"

    # 检查是否移除了@Slf4j import
    if echo "$modified_content" | grep -q "import lombok.extern.slf4j.Slf4j;"; then
        echo "❌ 错误: 未成功移除@Slf4j import"
        ((error_count++))
    else
        echo "✅ 成功移除@Slf4j import"
    fi

    # 检查是否添加了SLF4J imports
    if echo "$modified_content" | grep -q "import org.slf4j.Logger;"; then
        echo "✅ 成功添加Logger import"
    else
        echo "❌ 错误: 未添加Logger import"
        ((error_count++))
    fi

    if echo "$modified_content" | grep -q "import org.slf4j.LoggerFactory;"; then
        echo "✅ 成功添加LoggerFactory import"
    else
        echo "❌ 错误: 未添加LoggerFactory import"
        ((error_count++))
    fi

    # 检查是否替换了@Slf4j注解
    if echo "$modified_content" | grep -q "@Slf4j"; then
        echo "❌ 错误: 未成功替换@Slf4j注解"
        ((error_count++))
    else
        echo "✅ 成功替换@Slf4j注解"
    fi

    # 检查是否添加了Logger定义
    if echo "$modified_content" | grep -q "LoggerFactory.getLogger.*class"; then
        echo "✅ 成功添加Logger定义"
    else
        echo "⚠️ 警告: 未检测到Logger定义（可能已存在）"
    fi

    ((fixed_count++))
    echo "📊 进度: $fixed_count/$total_files 文件已处理"
done

# 清理临时文件
rm -rf "$temp_dir"

echo ""
echo "🎉 @Slf4j修复完成！"
echo "📊 最终统计:"
echo "  ✅ 成功修复: $fixed_count 个文件"
echo "  ❌ 错误数量: $error_count 个"
echo "  📈 成功率: $(( (fixed_count * 100) / total_files ))%"

if [ $error_count -eq 0 ]; then
    echo "🎊 所有文件修复成功，0错误！"
    exit 0
else
    echo "⚠️ 发现 $error_count 个错误，请检查上述日志"
    exit 1
fi