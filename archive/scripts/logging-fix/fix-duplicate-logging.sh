#!/bin/bash

# 修复重复日志前缀的脚本
echo "开始修复重复的日志前缀..."

# 修复所有ServiceImpl文件中的重复日志前缀
for file in $(find microservices/ioedream-consume-service/src/main/java -name "*ServiceImpl.java" -type f); do
    echo "处理文件: $file"

    # 修复重复的模块名前缀，将 [模块名] [模块名] [模块名] 替换为 [模块名]
    sed -i 's/\(\[[^]]*\]\) *\1 *\1/\1/g' "$file"
    sed -i 's/\(\[[^]]*\]\) *\1/\1/g' "$file"

    echo "  ✓ 已修复重复前缀"
done

echo ""
echo "重复日志前缀修复完成！"