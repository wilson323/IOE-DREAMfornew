#!/bin/bash
echo "开始为所有清理了Lombok的文件生成getter/setter方法..."

# 找到所有需要处理的Java文件
find . -name "*.java" -type f | while read file; do
    # 检查文件是否包含私有字段但没有对应的getter/setter
    if grep -q "private.*;" "$file"; then
        echo "处理文件: $file"
        # 这里可以添加具体的getter/setter生成逻辑
        # 暂时先记录需要处理的文件
        echo "$file" >> files_need_getters_setters.txt
    fi
done

echo "需要生成getter/setter的文件列表已保存到 files_need_getters_setters.txt"
