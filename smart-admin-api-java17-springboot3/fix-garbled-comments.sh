#!/bin/bash

# UTF-8编码修复脚本
# 用于修复Java文件中的乱码注释

echo "🔧 开始修复Java文件乱码问题..."

# 乱码字符映射表
declare -A GARBLE_MAP=(
    ["鍒涙柊瀹為獙瀹?"="创新实验室"]
    ["搴忓垪鍖栧"]="序列化"]
    ["搴忓垪"]="序列化"]
    ["涓讳换: 鍗撳ぇ"]="设计模式"]
    ["闂撮殧"]="心跳"
    ["娑堣":"消费"]
    ["璁哄"]="访问"]
    ["璁哄涓讳换"]="访问控制"]
    ["搴撴崯"]="统计"
    ["鏍锋鏋"]="智能"
    ["鎵愭崐"]="设备"
    ["杩戞":"系统"]
    ["搴掓忓"]="导出"]
    ["搴撳崯瀹椤挓"]="统计分析"
    ["搴戞娉"]="系统管理"
)

# 遍历所有Java文件
find . -name "*.java" | while read -r file; do
    # 检查文件是否包含乱码
    if grep -q "鍒涙柊瀹為獙瀹\|搴忓垪鍖栧\|搴忓垪\|涓讳换.*鍗撳ぇ" "$file"; then
        echo "修复文件: $file"

        # 备份原文件
        cp "$file" "$file.backup"

        # 逐个替换乱码
        for garbled in "${!GARBLE_MAP[@]}"; do
            clean="${GARBLE_MAP[$garbled]}"
            sed -i "s/$garbled/$clean/g" "$file"
        done

        echo "✅ 修复完成: $file"
    fi
done

echo "🎉 乱码修复完成！"