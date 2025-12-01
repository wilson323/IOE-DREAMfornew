#!/bin/bash

# 前端权限控制批量修复脚本
# 将后端@SaCheckPermission权限注解同步到前端v-permission指令

set -e

echo "🚀 开始前端权限控制批量修复..."

# 配置变量
BACKEND_DIR="smart-admin-api-java17-springboot3"
FRONTEND_DIR="smart-admin-web-javascript"
BACKUP_DIR="frontend_backup_$(date +%Y%m%d_%H%M%S)"

# 创建备份
echo "📦 创建前端文件备份..."
mkdir -p "$BACKUP_DIR"
cp -r "$FRONTEND_DIR/src/views" "$BACKUP_DIR/" || {
    echo "❌ 备份失败，退出"
    exit 1
}

# 第一步：提取所有后端权限标识
echo "🔍 提取后端权限标识..."
TEMP_PERMISSIONS_FILE="temp_backend_permissions.txt"

# 清理临时文件
> "$TEMP_PERMISSIONS_FILE"

# 提取所有权限标识和对应的Controller
find "$BACKEND_DIR" -name "*Controller.java" -exec grep -H "@SaCheckPermission" {} \; | while IFS=: read -r controller_file line; do
    # 提取模块名（从文件路径）
    module_name=$(echo "$controller_file" | sed -n 's|.*/\(module/[^/]*\).*|\1|p' | sed 's|module/||' | sed 's|/.*||')
    if [ -z "$module_name" ]; then
        module_name=$(basename "$(dirname "$controller_file")")
    fi

    # 提取权限标识
    permission=$(echo "$line" | grep -o '"[^"]*"' | sed 's/"//g')

    # 记录到临时文件
    echo "$module_name|$permission" >> "$TEMP_PERMISSIONS_FILE"
done

echo "✅ 后端权限标识提取完成"

# 第二步：生成权限映射表
echo "📋 生成权限映射表..."
declare -A permission_mappings

# 消费模块权限映射
permission_mappings["consume-record"]="consume:record"
permission_mappings["consume-account"]="consume:account"
permission_mappings["consume-report"]="consume:report"
permission_mappings["consume-recharge"]="consume:recharge"
permission_mappings["consume-refund"]="consume:refund"

# 考勤模块权限映射
permission_mappings["attendance"]="attendance"

# 设备模块权限映射
permission_mappings["device"]="device"
permission_mappings["unified-device"]="unified:device"

# 门禁模块权限映射
permission_mappings["access"]="access"
permission_mappings["smart-access"]="smart:access"

# 视频模块权限映射
permission_mappings["video"]="video"

# 缓存模块权限映射
permission_mappings["cache"]="cache"

# OA模块权限映射
permission_mappings["oa"]="oa"

# HR模块权限映射
permission_mappings["hr"]="hr"

echo "✅ 权限映射表生成完成"

# 第三步：批量修复Vue文件
echo "🔧 开始批量修复Vue文件..."

# 统计变量
total_vue_files=0
fixed_vue_files=0
added_permissions=0

# 遍历所有Vue文件
find "$FRONTEND_DIR/src/views" -name "*.vue" | while read vue_file; do
    ((total_vue_files++))

    # 确定文件所属模块
    relative_path=$(echo "$vue_file" | sed "s|$FRONTEND_DIR/src/views/||")
    module=$(echo "$relative_path" | cut -d'/' -f1)
    submodule=$(echo "$relative_path" | cut -d'/' -f2)

    # 确定权限前缀
    permission_prefix=""
    case "$module" in
        "business")
            case "$submodule" in
                "consume") permission_prefix="consume" ;;
                "attendance") permission_prefix="attendance" ;;
                "access") permission_prefix="access" ;;
                "smart-video") permission_prefix="video" ;;
                *) permission_prefix="" ;;
            esac
            ;;
        "support")
            case "$submodule" in
                "cache") permission_prefix="cache" ;;
                *) permission_prefix="" ;;
            esac
            ;;
        *) permission_prefix="" ;;
    esac

    if [ -z "$permission_prefix" ]; then
        continue
    fi

    # 检查文件是否已经有权限控制
    if grep -q "v-permission" "$vue_file"; then
        echo "⚠️  $relative_path 已有权限控制，跳过"
        continue
    fi

    # 修复文件
    file_modified=false

    # 临时文件
    temp_file=$(mktemp)
    cp "$vue_file" "$temp_file"

    # 添加导出权限控制
    if grep -q "导出\|export" "$temp_file"; then
        sed -i 's|<a-button[^>]*type="primary"[^>]*@click="[^"]*export[^"]*"|\0 v-permission="['\''$permission_prefix:report:export'\'']"|g' "$temp_file"
        sed -i 's|<a-button[^>]*@click="[^"]*export[^"]*"[^>]*type="primary"|\0 v-permission="['\''$permission_prefix:report:export'\'']"|g' "$temp_file"
        file_modified=true
        ((added_permissions++))
    fi

    # 添加新增权限控制
    if grep -q "新增\|添加\|add\|create" "$temp_file"; then
        sed -i 's|<a-button[^>]*type="primary"[^>]*@click="[^"]*add[^"]*"|\0 v-permission="['\''$permission_prefix:add'\'']"|g' "$temp_file"
        sed -i 's|<a-button[^>]*@click="[^"]*create[^"]*"[^>]*type="primary"|\0 v-permission="['\''$permission_prefix:create'\'']"|g' "$temp_file"
        file_modified=true
        ((added_permissions++))
    fi

    # 添加编辑权限控制
    if grep -q "编辑\|修改\|edit\|update" "$temp_file"; then
        sed -i 's|<a-button[^>]*@click="[^"]*edit[^"]*"|\0 v-permission="['\''$permission_prefix:update'\'']"|g' "$temp_file"
        sed -i 's|<a-button[^>]*@click="[^"]*update[^"]*"|\0 v-permission="['\''$permission_prefix:update'\'']"|g' "$temp_file"
        file_modified=true
        ((added_permissions++))
    fi

    # 添加删除权限控制
    if grep -q "删除\|delete\|remove" "$temp_file"; then
        sed -i 's|<a-button[^>]*@click="[^"]*delete[^"]*"|\0 v-permission="['\''$permission_prefix:delete'\'']"|g' "$temp_file"
        sed -i 's|<a-button[^>]*@click="[^"]*remove[^"]*"|\0 v-permission="['\''$permission_prefix:delete'\'']"|g' "$temp_file"
        file_modified=true
        ((added_permissions++))
    fi

    # 添加查看详情权限控制
    if grep -q "详情\|detail\|查看" "$temp_file"; then
        sed -i 's|<a-button[^>]*@click="[^"]*detail[^"]*"|\0 v-permission="['\''$permission_prefix:detail'\'']"|g' "$temp_file"
        sed -i 's|<a-button[^>]*@click="[^"]*view[^"]*"|\0 v-permission="['\''$permission_prefix:detail'\'']"|g' "$temp_file"
        file_modified=true
        ((added_permissions++))
    fi

    # 应用更改
    if [ "$file_modified" = true ]; then
        mv "$temp_file" "$vue_file"
        echo "✅ $relative_path 权限控制已添加"
        ((fixed_vue_files++))
    else
        rm "$temp_file"
    fi
done

# 第四步：验证修复结果
echo "🔍 验证修复结果..."

# 计算修复后的覆盖率
vue_files_with_permission=$(find "$FRONTEND_DIR/src/views" -name "*.vue" -exec grep -l "v-permission" {} \; | wc -l)
total_vue_files=$(find "$FRONTEND_DIR/src/views" -name "*.vue" | wc -l)
coverage_rate=$(echo "scale=2; $vue_files_with_permission * 100 / $total_vue_files" | bc)

echo ""
echo "📊 修复结果统计："
echo "   总Vue文件数: $total_vue_files"
echo "   已修复文件数: $fixed_vue_files"
echo "   有权限控制文件数: $vue_files_with_permission"
echo "   权限控制覆盖率: $coverage_rate%"
echo "   添加权限指令数: $added_permissions"

# 第五步：生成修复报告
echo "📄 生成修复报告..."
report_file="frontend_permission_fix_report_$(date +%Y%m%d_%H%M%S).md"

cat > "$report_file" << EOF
# 前端权限控制修复报告

**修复时间**: $(date)
**修复脚本**: fix-frontend-permissions.sh

## 修复统计

- **总Vue文件数**: $total_vue_files
- **已修复文件数**: $fixed_vue_files
- **权限控制覆盖率**: $coverage_rate%
- **新增权限指令数**: $added_permissions

## 修复详情

### 权限映射规则

| 模块 | 权限前缀 | 操作类型 |
|------|----------|----------|
| consume | consume | 消费相关操作 |
| attendance | attendance | 考勤相关操作 |
| access | access | 门禁相关操作 |
| video | video | 视频相关操作 |
| cache | cache | 缓存相关操作 |
| hr | hr | 人事相关操作 |
| oa | oa | 办公相关操作 |

### 添加的权限类型

| 权限类型 | 权限标识 | 说明 |
|----------|----------|------|
| 新增 | {module}:add | 新增记录权限 |
| 编辑 | {module}:update | 编辑记录权限 |
| 删除 | {module}:delete | 删除记录权限 |
| 查看 | {module}:detail | 查看详情权限 |
| 导出 | {module}:export | 导出数据权限 |

### 验证方法

使用以下命令验证修复结果：

\`\`\`bash
# 检查权限控制覆盖率
find smart-admin-web-javascript/src/views -name "*.vue" -exec grep -l "v-permission" {} \; | wc -l

# 检查具体文件的权限控制
grep -r "v-permission" smart-admin-web-javascript/src/views/
\`\`\`

## 注意事项

1. **备份位置**: \`$BACKUP_DIR\`
2. **权限标识**: 确保与后端 \`@SaCheckPermission\` 注解一致
3. **测试要求**: 修复后需要测试各个功能模块的权限控制是否生效

## 后续优化建议

1. **自动化同步**: 建立前后端权限标识自动同步机制
2. **权限验证**: 添加权限控制覆盖率的CI检查
3. **文档完善**: 更新权限控制开发文档
4. **测试覆盖**: 增加权限控制的自动化测试

---

**报告生成者**: 自动化修复脚本
**验证状态**: 待验证
**下一步**: 执行功能测试和权限验证
EOF

echo "✅ 修复报告已生成: $report_file"

# 清理临时文件
rm -f "$TEMP_PERMISSIONS_FILE"

echo ""
echo "🎉 前端权限控制批量修复完成！"
echo ""
echo "📋 下一步操作："
echo "1. 检查修复结果: grep -r 'v-permission' $FRONTEND_DIR/src/views/"
echo "2. 运行前端测试: cd $FRONTEND_DIR && npm run dev"
echo "3. 验证权限功能: 测试各个模块的权限控制是否生效"
echo "4. 查看详细报告: cat $report_file"
echo ""
echo "⚠️  注意：如有问题可使用备份恢复: cp -r $BACKUP_DIR/views $FRONTEND_DIR/src/"