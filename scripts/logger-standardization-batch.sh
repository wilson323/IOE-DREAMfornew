#!/bin/bash

# Logger标准化批量处理脚本 - 简化版本
# 分批次处理，提高成功率

set -e

PROJECT_ROOT="D:/IOE-DREAM/smart-admin-api-java17-springboot3"
LOG_FILE="${PROJECT_ROOT}/logger-batch.log"

echo "=== Logger批量处理开始 ===" | tee "$LOG_FILE"
echo "时间: $(date)" | tee -a "$LOG_FILE"

cd "$PROJECT_ROOT" || exit 1

# 批量处理函数
process_file() {
    local file="$1"
    local class_name=$(basename "$file" .java)

    echo "处理: $file (类名: $class_name)" | tee -a "$LOG_FILE"

    # 检查是否已经处理过
    if grep -q "import org.slf4j.Logger;" "$file"; then
        echo "  已处理，跳过" | tee -a "$LOG_FILE"
        return 0
    fi

    # 备份
    cp "$file" "$file.bak" || return 1

    # 使用sed进行基础替换
    {
        # 1. 删除 lombok.extern.slf4j.Slf4j 导入
        sed -i '/import lombok\.extern\.slf4j\.Slf4j;/d' "$file"

        # 2. 替换 @Slf4j 注解
        sed -i 's/@Slf4j/\/\/ @Slf4j - 手动添加log变量替代Lombok注解/' "$file"

        # 3. 在package后添加Logger导入
        sed -i '/^package /a\\nimport org.slf4j.Logger;\nimport org.slf4j.LoggerFactory;' "$file"

        # 4. 在类定义后添加Logger变量（简单的行匹配）
        sed -i "/^public.*$class_name.*{/,/^}/{ /^public.*$class_name.*{/{ N; a\\\n    private static final Logger log = LoggerFactory.getLogger($class_name.class);\n
} }" "$file"

    } 2>> "$LOG_FILE"

    echo "  处理完成" | tee -a "$LOG_FILE"
    return 0
}

# 处理单个文件测试
echo "=== 测试单个文件处理 ===" | tee -a "$LOG_FILE"
test_file=$(find . -name "*.java" -exec grep -l "@Slf4j" {} \; | head -1)

if [ -n "$test_file" ]; then
    echo "测试文件: $test_file" | tee -a "$LOG_FILE"
    if process_file "$test_file"; then
        echo "✅ 测试成功" | tee -a "$LOG_FILE"
    else
        echo "❌ 测试失败" | tee -a "$LOG_FILE"
    fi
else
    echo "❌ 没有找到测试文件" | tee -a "$LOG_FILE"
fi

echo "=== 脚本执行完成 ===" | tee -a "$LOG_FILE"