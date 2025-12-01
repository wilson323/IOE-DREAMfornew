#!/bin/bash

# 安全的Logger标准化脚本 - 分批处理，确保不影响编译
# 只处理已经存在的文件，不修复其他编译问题

set -e

PROJECT_ROOT="D:/IOE-DREAM/smart-admin-api-java17-springboot3"
LOG_FILE="${PROJECT_ROOT}/safe-logger-process.log"

echo "=== 安全Logger标准化脚本 ===" | tee "$LOG_FILE"
echo "时间: $(date)" | tee -a "$LOG_FILE"

cd "$PROJECT_ROOT" || exit 1

# 只处理sa-base模块的配置文件（安全区域）
echo "=== 处理sa-base模块配置文件 ===" | tee -a "$LOG_FILE"

CONFIG_FILES=(
    "./sa-base/src/main/java/net/lab1024/sa/base/config/CacheAnnotationConfig.java"
    "./sa-base/src/main/java/net/lab1024/sa/base/config/CacheConfig.java"
    "./sa-base/src/main/java/net/lab1024/sa/base/config/DataSourceConfig.java"
    "./sa-base/src/main/java/net/lab1024/sa/base/config/ScheduleConfig.java"
    "./sa-base/src/main/java/net/lab1024/sa/base/config/SwaggerConfig.java"
    "./sa-base/src/main/java/net/lab1024/sa/base/config/UnifiedCacheConfig.java"
    "./sa-base/src/main/java/net/lab1024/sa/base/config/UrlConfig.java"
    "./sa-base/src/main/java/net/lab1024/sa/base/config/YamlProcessor.java"
)

for file in "${CONFIG_FILES[@]}"; do
    if [ -f "$file" ]; then
        if grep -q "@Slf4j" "$file"; then
            echo "处理: $file" | tee -a "$LOG_FILE"

            # 获取类名
            class_name=$(basename "$file" .java)

            # 备份
            cp "$file" "$file.logger-backup" 2>/dev/null || true

            # 安全处理 - 只修改Logger相关内容
            python3 -c "
import re

file_path = '$file'
class_name = '$class_name'

try:
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    original_content = content

    # 1. 删除 lombok.extern.slf4j.Slf4j 导入
    content = re.sub(r'import lombok\.extern\.slf4j\.Slf4j;\s*\n', '', content)

    # 2. 替换 @Slf4j 注解为注释
    content = re.sub(r'@Slf4j', '// @Slf4j - 手动添加log变量替代Lombok注解', content)

    # 3. 在package后添加Logger导入（如果不存在）
    if 'import org.slf4j.Logger;' not in content:
        content = re.sub(
            r'(package [^;]+;\s*\n)',
            r'\1\nimport org.slf4j.Logger;\nimport org.slf4j.LoggerFactory;\n',
            content
        )

    # 4. 在类定义后添加Logger定义（如果不存在）
    if 'private static final Logger log' not in content:
        # 查找类定义并添加Logger变量
        class_pattern = r'(public\s+\w+\s+' + re.escape(class_name) + r'\s*\{)'
        if re.search(class_pattern, content):
            content = re.sub(
                class_pattern,
                r'\1\n\n    private static final Logger log = LoggerFactory.getLogger(' + class_name + '.class);',
                content
            )

    # 只在有变化时写入
    if content != original_content:
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f'  ✅ 成功修改: {file_path}')
    else:
        print(f'  ℹ️  无需修改: {file_path}')

except Exception as e:
    print(f'  ❌ 处理失败: {file_path}, 错误: {str(e)}')
" 2>> "$LOG_FILE"

            echo "  完成" | tee -a "$LOG_FILE"
        else
            echo "跳过: $file (不包含@Slf4j)" | tee -a "$LOG_FILE"
        fi
    else
        echo "文件不存在: $file" | tee -a "$LOG_FILE"
    fi
done

echo "=== 验证处理结果 ===" | tee -a "$LOG_FILE"

# 检查已处理的文件
processed_count=0
for file in "${CONFIG_FILES[@]}"; do
    if [ -f "$file" ]; then
        if grep -q "import org.slf4j.Logger;" "$file"; then
            echo "✅ 已标准化: $file" | tee -a "$LOG_FILE"
            ((processed_count++))
        elif grep -q "@Slf4j" "$file"; then
            echo "⚠️  仍需处理: $file" | tee -a "$LOG_FILE"
        fi
    fi
done

echo "处理文件数: $processed_count" | tee -a "$LOG_FILE"

# 简单编译检查（只检查语法，不依赖其他模块）
echo "=== 语法检查 ===" | tee -a "$LOG_FILE"
cd sa-base
if javac -cp "$(find . -name "*.jar" | tr '\n' ':')" -sourcepath src/main/java src/main/java/net/lab1024/sa/base/config/*.java 2>> "$LOG_FILE"; then
    echo "✅ 语法检查通过" | tee -a "$LOG_FILE"
else
    echo "❌ 语法检查失败" | tee -a "$LOG_FILE"
fi

echo "=== 安全Logger标准化完成 ===" | tee -a "$LOG_FILE"
echo "详细日志: $LOG_FILE" | tee -a "$LOG_FILE"