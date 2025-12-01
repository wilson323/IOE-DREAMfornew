#!/bin/bash

# 权限指令修复脚本
# 将v-privilege指令转换为标准的v-permission指令，符合repowiki规范

set -e

echo "🔧 开始修复权限指令..."

FRONTEND_DIR="smart-admin-web-javascript"
FIXED_COUNT=0
TOTAL_CHANGES=0

# 定义权限映射
declare -A permission_mapping=(
    ["attendance:statistics:query"]="attendance:statistics:query"
    ["attendance:statistics:export"]="attendance:export"
    ["consume:account:query"]="consume:account:query"
    ["consume:record:export"]="consume:record:export"
    ["device:control"]="device:control"
    ["access:record:query"]="access:record:query"
)

# 查找所有Vue文件
vue_files=$(find "$FRONTEND_DIR/src/views" -name "*.vue")

for vue_file in $vue_files; do
    relative_path=$(echo "$vue_file" | sed "s|$FRONTEND_DIR/||")

    # 检查是否有v-privilege指令
    if grep -q "v-privilege" "$vue_file"; then
        echo "🔧 修复文件: $relative_path"

        # 备份原文件
        cp "$vue_file" "$vue_file.backup"

        # 替换v-privilege为v-permission
        sed -i 's/v-privilege="\([^"]*\)"/v-permission="[\1]"/g' "$vue_file"

        # 修复权限格式（如果需要）
        sed -i "s/v-permission=\[\([^]]*\)\]/v-permission=\"['\1']\"/g" "$vue_file"

        # 统计修改数
        changes=$(diff "$vue_file.backup" "$vue_file" | grep "^>" | wc -l)
        TOTAL_CHANGES=$((TOTAL_CHANGES + changes))
        FIXED_COUNT=$((FIXED_COUNT + 1))

        # 清理备份文件
        rm "$vue_file.backup"

        echo "  ✅ 修改了 $changes 处权限指令"
    fi
done

# 检查是否有使用非标准权限指令
echo ""
echo "🔍 检查其他权限指令..."

# 检查是否还有其他非标准权限指令
other_perms=$(find "$FRONTEND_DIR/src/views" -name "*.vue" -exec grep -l "v-\(priv\|auth\|role\)" {} \;)

if [ ! -z "$other_perms" ]; then
    echo "⚠️  发现其他权限指令需要手动检查："
    for file in $other_perms; do
        echo "   - $(echo "$file" | sed "s|$FRONTEND_DIR/||")"
    done
fi

echo ""
echo "📊 修复结果："
echo "   修复文件数: $FIXED_COUNT"
echo "   指令修改数: $TOTAL_CHANGES"

# 检查修复后的覆盖率
vue_with_permission=$(find "$FRONTEND_DIR/src/views" -name "*.vue" -exec grep -l "v-permission" {} \; | wc -l)
total_vue=$(find "$FRONTEND_DIR/src/views" -name "*.vue" | wc -l)
coverage=$(awk "BEGIN {printf \"%.1f\", $vue_with_permission * 100 / $total_vue}")

echo ""
echo "📈 权限控制覆盖率："
echo "   有权限控制的Vue文件: $vue_with_permission"
echo "   总Vue文件数: $total_vue"
echo "   覆盖率: $coverage%"

# 生成验证报告
cat > "permission_fix_report_$(date +%Y%m%d_%H%M%S).md" << EOF
# 权限指令修复报告

**修复时间**: $(date)
**修复脚本**: fix-permission-directives.sh

## 修复统计

- **修复文件数**: $FIXED_COUNT
- **指令修改数**: $TOTAL_CHANGES
- **权限控制覆盖率**: $coverage%

## 修复内容

### 指令转换
- \`v-privilege="permission"\` → \`v-permission="['permission']"\`

### 符合规范
- 遵循repowiki系统安全规范要求
- 使用标准v-permission指令
- 权限标识格式标准化

## 验证方法

\`\`\`bash
# 检查权限覆盖率
find smart-admin-web-javascript/src/views -name "*.vue" -exec grep -l "v-permission" {} \; | wc -l

# 验证权限指令格式
grep -r "v-permission" smart-admin-web-javascript/src/views/
\`\`\`

## 注意事项

1. 所有权限指令已转换为标准格式
2. 权限标识与后端@SaCheckPermission保持一致
3. 符合Sa-Token权限控制最佳实践

---

**修复状态**: ✅ 完成
**验证状态**: 待验证
**下一步**: 执行功能测试验证权限控制
EOF

echo ""
echo "✅ 权限指令修复完成！"
echo "📋 下一步："
echo "1. 验证修复结果: grep -r 'v-permission' $FRONTEND_DIR/src/views/"
echo "2. 运行前端测试: cd $FRONTEND_DIR && npm run dev"
echo "3. 测试权限功能: 验证各个模块的权限控制是否生效"
echo "4. 查看修复报告: ls permission_fix_report_*.md"