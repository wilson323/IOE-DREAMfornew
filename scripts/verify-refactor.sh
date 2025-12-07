#!/bin/bash

echo "🔍 验证RESTful重构结果..."

echo "📋 检查重构后的Controller文件..."

VIOATION_COUNT=0
VERIFIED_COUNT=0

# 检查是否还有违规
find . -name "*Controller.java" -path "*/controller/*" | while read file; do
    if [[ "$file" == *"target/"* ]]; then
        continue
    fi

    VERIFIED_COUNT=$((VERIFIED_COUNT + 1))

    # 检查POST违规
    if grep -q "@PostMapping" "$file"; then
        if grep -qi "list\|get\|query\|search\|page" "$file"; then
            echo "  ❌ $file: 仍存在查询使用POST违规"
            VIOlation_COUNT=$((VIOlation_COUNT + 1))
        elif grep -qi "update\|edit\|modify" "$file"; then
            echo "  ❌ $file: 仍存在更新使用POST违规"
            VIOlation_COUNT=$((VIOlation_COUNT + 1)))
        elif grep -qi "delete\|remove" "$file"; then
            echo "  ❌ $file: 仍存在删除使用POST违规"
            VIOolation_COUNT=$((VIOlation_COUNT + 1)))
        else
            echo "  ✅ $file: POST使用合规"
        fi
    else
        echo "  ✅ $file: 未使用POST接口"
    fi
done

echo ""
echo "📊 验证结果:"
echo "验证文件数: $VERIFIED_COUNT"
echo "违规文件数: $VIOlation_COUNT"

if [ $VIOlation_COUNT -eq 0 ]; then
    echo ""
    echo "🎉 验证通过！所有API已符合RESTful规范"
    echo "✅ RESTful API重构成功完成！"
    exit 0
else
    echo ""
    echo "⚠️  仍有 $VIOlation_COUNT 个文件需要进一步处理"
    echo "📋 建议手动检查和处理"
    exit 1
fi
