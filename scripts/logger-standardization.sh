#!/bin/bash

# Logger标准化自动化脚本
# 将所有使用@Slf4j注解的Java文件转换为标准Logger模板
# 作者: 代码质量保护专家
# 日期: 2025-11-23

set -e

# 项目根目录
PROJECT_ROOT="D:/IOE-DREAM/smart-admin-api-java17-springboot3"

# 日志文件
LOG_FILE="${PROJECT_ROOT}/logger-standardization.log"

echo "=== Logger标准化自动化脚本开始 ===" | tee "$LOG_FILE"
echo "时间: $(date)" | tee -a "$LOG_FILE"
echo "项目路径: $PROJECT_ROOT" | tee -a "$LOG_FILE"

# 切换到项目目录
cd "$PROJECT_ROOT" || {
    echo "❌ 无法切换到项目目录: $PROJECT_ROOT" | tee -a "$LOG_FILE"
    exit 1
}

# 查找所有使用@Slf4j的Java文件
echo "=== 步骤1: 查找所有使用@Slf4j的Java文件 ===" | tee -a "$LOG_FILE"
SLF4J_FILES=$(find . -name "*.java" -exec grep -l "@Slf4j" {} \;)

if [ -z "$SLF4J_FILES" ]; then
    echo "✅ 没有找到使用@Slf4j的文件，已经全部完成标准化。" | tee -a "$LOG_FILE"
    exit 0
fi

FILE_COUNT=$(echo "$SLF4J_FILES" | wc -l)
echo "找到 $FILE_COUNT 个使用@Slf4j的文件需要处理" | tee -a "$LOG_FILE"

# 统计计数器
PROCESSED_COUNT=0
SUCCESS_COUNT=0
ERROR_COUNT=0

echo "$SLF4J_FILES" | while read -r file; do
    ((PROCESSED_COUNT++))
    echo "=== 处理文件 $PROCESSED_COUNT/$FILE_COUNT: $file ===" | tee -a "$LOG_FILE"

    # 跳过已处理的文件（检查是否已经包含标准Logger导入）
    if grep -q "import org.slf4j.Logger;" "$file"; then
        echo "⚠️  文件已包含标准Logger导入，跳过处理" | tee -a "$LOG_FILE"
        continue
    fi

    # 获取类名
    CLASS_NAME=$(basename "$file" .java)

    echo "  类名: $CLASS_NAME" | tee -a "$LOG_FILE"

    # 备份原文件
    cp "$file" "$file.bak" || {
        echo "❌ 无法备份文件: $file" | tee -a "$LOG_FILE"
        ((ERROR_COUNT++))
        continue
    }

    # 执行Logger标准化转换
    if python3 -c "
import sys
import re

file_path = '$file'
class_name = '$CLASS_NAME'

try:
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    # 1. 删除 lombok.extern.slf4j.Slf4j 导入
    content = re.sub(r'import lombok\.extern\.slf4j\.Slf4j;\s*\n', '', content)

    # 2. 替换 @Slf4j 注解为注释
    content = re.sub(r'@Slf4j', '// @Slf4j - 手动添加log变量替代Lombok注解', content)

    # 3. 添加标准Logger导入（在package语句后）
    package_pattern = r'(package [^;]+;\s*\n)'
    if 'import org.slf4j.Logger;' not in content:
        content = re.sub(
            package_pattern,
            r'\1\nimport org.slf4j.Logger;\nimport org.slf4j.LoggerFactory;',
            content
        )

    # 4. 在类声明后添加Logger定义（在第一个注释或字段之前）
    class_pattern = r'(public\s+\w+\s+' + re.escape(class_name) + r'\s*\{[^}]*?)(\s*//|@\w+|private|public|protected|/\*)'

    def add_logger(match):
        class_content = match.group(1)
        rest = match.group(2)
        return class_content + '\n    private static final Logger log = LoggerFactory.getLogger(' + class_name + '.class);\n' + rest

    if 'private static final Logger log' not in content:
        # 尝试多种模式来插入Logger定义
        patterns = [
            # 在类声明后的第一个注释或注解前
            r'(public\s+\w+\s+' + re.escape(class_name) + r'\s*\{)(\s*//|@\w+|private|public|protected)',
            # 在类声明后的空行后
            r'(public\s+\w+\s+' + re.escape(class_name) + r'\s*\{\s*\n)(\s*\n)',
            # 在类声明后直接添加
            r'(public\s+\w+\s+' + re.escape(class_name) + r'\s*\{)(\s*)',
        ]

        for pattern in patterns:
            if re.search(pattern, content):
                content = re.sub(pattern, add_logger, content, count=1)
                break

    # 5. 写入修改后的内容
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(content)

    print(f'✅ 成功处理: {file_path}')

except Exception as e:
    print(f'❌ 处理失败: {file_path}, 错误: {str(e)}')
    sys.exit(1)
" 2>> "$LOG_FILE"; then
        echo "✅ 成功处理: $file" | tee -a "$LOG_FILE"
        ((SUCCESS_COUNT++))
    else
        echo "❌ 处理失败: $file" | tee -a "$LOG_FILE"
        ((ERROR_COUNT++))
        # 恢复备份文件
        mv "$file.bak" "$file" 2>/dev/null || true
    fi

    # 每处理10个文件验证一次编译
    if [ $((PROCESSED_COUNT % 10)) -eq 0 ]; then
        echo "=== 中间验证: 已处理 $PROCESSED_COUNT 个文件，执行编译检查 ===" | tee -a "$LOG_FILE"
        if mvn clean compile -q -DskipTests >> "$LOG_FILE" 2>&1; then
            echo "✅ 编译检查通过" | tee -a "$LOG_FILE"
        else
            echo "❌ 编译检查失败，查看日志获取详细信息" | tee -a "$LOG_FILE"
            mvn clean compile -DskipTests 2>&1 | tail -20 | tee -a "$LOG_FILE"
        fi
    fi
done

echo "=== Logger标准化处理完成 ===" | tee -a "$LOG_FILE"
echo "处理文件总数: $FILE_COUNT" | tee -a "$LOG_FILE"
echo "成功处理数: $SUCCESS_COUNT" | tee -a "$LOG_FILE"
echo "处理失败数: $ERROR_COUNT" | tee -a "$LOG_FILE"

# 最终编译验证
echo "=== 最终编译验证 ===" | tee -a "$LOG_FILE"
if mvn clean compile -q -DskipTests >> "$LOG_FILE" 2>&1; then
    echo "🎉 Logger标准化完成，编译通过！" | tee -a "$LOG_FILE"

    # 验证没有遗漏的@Slf4j文件
    REMAINING_SLF4J=$(find . -name "*.java" -exec grep -l "@Slf4j" {} \; | wc -l)
    if [ "$REMAINING_SLF4J" -eq 0 ]; then
        echo "✅ 验证通过：所有@Slf4j文件已完成标准化" | tee -a "$LOG_FILE"
    else
        echo "⚠️  警告：仍有 $REMAINING_SLF4J 个文件使用@Slf4j注解" | tee -a "$LOG_FILE"
        find . -name "*.java" -exec grep -l "@Slf4j" {} \; | tee -a "$LOG_FILE"
    fi
else
    echo "❌ 编译失败，请检查错误并修复" | tee -a "$LOG_FILE"
    mvn clean compile -DskipTests 2>&1 | tail -50 | tee -a "$LOG_FILE"
    exit 1
fi

echo "=== Logger标准化自动化脚本结束 ===" | tee -a "$LOG_FILE"
echo "时间: $(date)" | tee -a "$LOG_FILE"

# 生成处理报告
echo "=== Logger标准化处理报告 ===" > "${PROJECT_ROOT}/logger-standardization-report.txt"
echo "处理时间: $(date)" >> "${PROJECT_ROOT}/logger-standardization-report.txt"
echo "处理文件总数: $FILE_COUNT" >> "${PROJECT_ROOT}/logger-standardization-report.txt"
echo "成功处理数: $SUCCESS_COUNT" >> "${PROJECT_ROOT}/logger-standardization-report.txt"
echo "处理失败数: $ERROR_COUNT" >> "${PROJECT_ROOT}/logger-standardization-report.txt"
echo "详细日志请查看: $LOG_FILE" >> "${PROJECT_ROOT}/logger-standardization-report.txt"

echo "📋 处理报告已生成: ${PROJECT_ROOT}/logger-standardization-report.txt"