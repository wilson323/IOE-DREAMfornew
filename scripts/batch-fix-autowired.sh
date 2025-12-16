#!/bin/bash
# 批量修复@Autowired违规问题脚本 (Linux/WSL版本)
# 作者: IOE-DREAM Team
# 版本: 1.0.0
# 日期: 2025-12-16

set -e

# 统计变量
TOTAL_FILES=0
FIXED_FILES=0
ERRORS=0

echo "===================================="
echo "Autowired 违规修复工具"
echo "===================================="

# 查找所有包含@Autowired的Java文件
echo "正在搜索包含@Autowired的Java文件..."

# 创建临时文件存储需要修复的文件列表
TEMP_FILE=$(mktemp)
find . -name "*.java" -type f \
    -not -path "*/target/*" \
    -not -path "*/.git/*" \
    -not -path "*/node_modules/*" \
    -exec grep -l "@Autowired" {} \; > "$TEMP_FILE"

TOTAL_FILES=$(wc -l < "$TEMP_FILE")
echo "找到 $TOTAL_FILES 个包含@Autowired的Java文件"

if [ $TOTAL_FILES -eq 0 ]; then
    echo "没有找到包含@Autowired的文件，脚本执行完成。"
    rm -f "$TEMP_FILE"
    exit 0
fi

# 处理每个文件
while IFS= read -r file; do
    echo "处理文件: $file"

    # 检查文件是否已经包含@Resource
    if grep -q "@Resource" "$file"; then
        echo "  跳过: 文件已经包含@Resource注解"
        continue
    fi

    # 检查是否使用正确的javax.annotation.Resource
    if grep -q "javax\.annotation\.Resource" "$file"; then
        echo "  跳过: 文件已经使用正确的javax.annotation.Resource"
        continue
    fi

    # 统计@Autowired数量
    AUTOWIRED_COUNT=$(grep -c "@Autowired" "$file" || true)
    echo "  发现 $AUTOWIRED_COUNT 个@Autowired违规"

    # 备份原文件
    cp "$file" "$file.backup"

    # 执行修复
    if sed -i 's/import org\.springframework\.beans\.factory\.annotation\.Autowired;/import jakarta.annotation.Resource;/g' "$file" &&
       sed -i 's/@Autowired/@Resource/g' "$file"; then

        # 验证修复结果
        NEW_AUTOWIRED_COUNT=$(grep -c "@Autowired" "$file" || true)
        RESOURCE_COUNT=$(grep -c "@Resource" "$file" || true)

        if [ $NEW_AUTOWIRED_COUNT -eq 0 ] && [ $RESOURCE_COUNT -gt 0 ]; then
            echo "  ✓ 成功修复: $AUTOWIRED_COUNT 个@Autowired → @Resource"
            FIXED_FILES=$((FIXED_FILES + 1))
            rm -f "$file.backup"
        else
            echo "  ✗ 修复失败: 仍有 $NEW_AUTOWIRED_COUNT 个@Autowired"
            ERRORS=$((ERRORS + 1))
            mv "$file.backup" "$file"
        fi
    else
        echo "  ✗ sed命令执行失败"
        ERRORS=$((ERRORS + 1))
        mv "$file.backup" "$file"
    fi

done < "$TEMP_FILE"

# 清理临时文件
rm -f "$TEMP_FILE"

# 输出统计结果
echo ""
echo "===================================="
echo "修复完成统计"
echo "===================================="
echo "总文件数: $TOTAL_FILES"
echo "已修复: $FIXED_FILES"
echo "错误数: $ERRORS"

if [ $TOTAL_FILES -gt 0 ]; then
    SUCCESS_RATE=$(echo "scale=2; $FIXED_FILES * 100 / $TOTAL_FILES" | bc)
    echo "成功率: $SUCCESS_RATE%"
fi

if [ $ERRORS -gt 0 ]; then
    echo ""
    echo "⚠️  发现 $ERRORS 个错误，请检查上述错误信息"
    exit 1
elif [ $FIXED_FILES -gt 0 ]; then
    echo ""
    echo "🎉 成功修复了 $FIXED_FILES 个文件！"
else
    echo ""
    echo "✅ 没有需要修复的文件"
fi

echo ""
echo "建议后续操作:"
echo "1. 运行 'mvn clean compile' 验证编译"
echo "2. 运行单元测试确保功能正常"
echo "3. 提交代码变更"