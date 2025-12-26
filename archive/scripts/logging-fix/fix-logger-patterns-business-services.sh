#!/bin/bash

# 批量修复业务服务模块的日志模式
# 作者: IOE-DREAM Team
# 版本: 2.0.0

echo "=== 批量修复业务服务模块日志模式 ==="
echo "将传统Logger转换为@Slf4j注解"

# 业务服务模块列表
service_modules=(
    "ioedream-access-service"
    "ioedream-attendance-service"
    "ioedream-consume-service"
    "ioedream-video-service"
    "ioedream-visitor-service"
    "ioedream-biometric-service"
    "ioedream-oa-service"
    "ioedream-device-comm-service"
)

# 统计信息
total_files=0
fixed_count=0

# 处理每个模块
for module in "${service_modules[@]}"; do
    echo ""
    echo "=== 处理模块: $module ==="

    module_path="microservices/$module"

    if [ ! -d "$module_path" ]; then
        echo "✗ 模块目录不存在: $module_path"
        continue
    fi

    # 查找需要修复的文件
    files_to_fix=()
    while IFS= read -r -d '' file; do
        files_to_fix+=("$file")
    done < <(find "$module_path" -name "*.java" -type f -exec grep -l "import org.slf4j.Logger" {} \; -print0)

    module_files_count=${#files_to_fix[@]}
    echo "找到 $module_files_count 个需要修复的文件"

    # 修复文件
    for file in "${files_to_fix[@]}"; do
        echo "修复: $file"

        # 1. 替换import语句
        sed -i.bak 's/import org\.slf4j\.Logger;/import lombok.extern.slf4j.Slf4j;/g' "$file"
        sed -i.bak '/import org\.slf4j\.LoggerFactory;/d' "$file"

        # 2. 删除Logger声明行
        sed -i.bak '/private static final Logger log = LoggerFactory\.getLogger.*\.class);/d' "$file"

        # 3. 在类声明前添加@Slf4j注解（考虑可能有多个类声明）
        awk -v class_pattern="^(public|protected|private)?(\s+abstract)?(\s+final)?\s+class\s+" '
        {
            # 记录当前行
            line = $0

            # 检查是否是类声明
            if (match(line, class_pattern)) {
                # 获取缩进
                match_indent = match(line, /^[[:space:]]*/)
                indent = substr(line, 1, match_indent)

                # 检查前面几行是否已经有@Slf4j注解
                has_slf4j = 0
                for (i = NR-5; i <= NR-1; i++) {
                    if (i > 0 && lines[i] ~ /@Slf4j/) {
                        has_slf4j = 1
                        break
                    }
                }

                # 如果没有@Slf4j，则在类声明前添加
                if (!has_slf4j) {
                    print indent "@Slf4j"
                }
            }

            # 打印当前行
            print line

            # 保存行记录
            lines[NR] = line
        }
        ' "$file" > "$file.tmp" && mv "$file.tmp" "$file"

        # 删除备份文件
        rm -f "$file.bak"

        ((fixed_count++))
    done

    ((total_files += module_files_count))
done

echo ""
echo "=== 修复完成 ==="
echo "总共修复的文件数量: $fixed_count/$total_files"
echo ""

# 验证修复结果
echo "=== 验证修复结果 ==="
error_count=0

for module in "${service_modules[@]}"; do
    module_path="microservices/$module"

    if [ ! -d "$module_path" ]; then
        continue
    fi

    echo "验证模块: $module"

    # 检查是否还有未修复的Logger导入
    remaining_files=$(find "$module_path" -name "*.java" -type f -exec grep -l "import org.slf4j.Logger" {} \;)

    if [ -n "$remaining_files" ]; then
        echo "  ✗ 仍有Logger导入的文件:"
        echo "$remaining_files" | sed 's/^/    /'
        error_count=$(echo "$remaining_files" | wc -l)
        ((error_count += error_count))
    else
        echo "  ✓ 没有发现Logger导入"
    fi

    # 检查是否有LoggerFactory使用
    factory_files=$(find "$module_path" -name "*.java" -type f -exec grep -l "LoggerFactory.getLogger" {} \;)

    if [ -n "$factory_files" ]; then
        echo "  ✗ 仍有LoggerFactory使用的文件:"
        echo "$factory_files" | sed 's/^/    /'
        error_count=$(echo "$factory_files" | wc -l)
        ((error_count += error_count))
    else
        echo "  ✓ 没有发现LoggerFactory使用"
    fi
done

echo ""
if [ $error_count -eq 0 ]; then
    echo "✓ 所有文件验证通过！日志模式修复完成！"
else
    echo "✗ 发现 $error_count 个问题需要手动修复"
fi

echo ""
echo "=== 修复统计 ==="
for module in "${service_modules[@]}"; do
    module_path="microservices/$module"

    if [ ! -d "$module_path" ]; then
        continue
    fi

    slf4j_count=$(find "$module_path" -name "*.java" -type f -exec grep -l "@Slf4j" {} \; | wc -l)
    echo "$module: $slf4j 个文件使用 @Slf4j"
done