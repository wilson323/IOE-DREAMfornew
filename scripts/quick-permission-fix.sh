#!/bin/bash

# 快速权限控制修复脚本 - 重点修复关键文件

set -e

echo "⚡ 开始快速权限控制修复..."

FRONTEND_DIR="smart-admin-web-javascript"
TARGET_FILES=(
    "src/views/business/consume/account/index.vue"
    "src/views/business/consume/device/index.vue"
    "src/views/business/consume/report/index.vue"
    "src/views/business/attendance/components/schedule-detail-drawer.vue"
    "src/views/business/access/area/index.vue"
    "src/views/business/access/config/index.vue"
    "src/views/business/access/record/index.vue"
    "src/views/business/smart-video/device-list.vue"
    "src/views/support/cache/cache-list.vue"
    "src/views/support/config/config-list.vue"
)

# 权限映射
declare -A permission_map=(
    ["consume:account"]='["consume:account:add","consume:account:update","consume:account:delete","consume:account:recharge","consume:account:freeze"]'
    ["consume:device"]='["consume:device:add","consume:device:update","consume:device:delete"]'
    ["consume:report"]='["consume:report:export","consume:report:view"]'
    ["attendance"]='["attendance:punch:in","attendance:punch:out","attendance:makeup:apply","attendance:export"]'
    ["access:area"]='["smart:access:area:add","smart:access:area:update","smart:access:area:delete"]'
    ["access:config"]='["access:config:update"]'
    ["access:record"]='["access:record:query","access:record:export"]'
    ["video:device"]='["video:device:add","video:device:update","video:device:delete"]'
    ["cache"]='["cache:operate:get","cache:operate:set","cache:manage:clear"]'
    ["config"]='["config:update"]'
)

for file in "${TARGET_FILES[@]}"; do
    if [ ! -f "$FRONTEND_DIR/$file" ]; then
        echo "⚠️  文件不存在: $file"
        continue
    fi

    echo "🔧 修复: $file"

    # 确定模块类型
    module_type=""
    case "$file" in
        *"consume:account"*) module_type="consume:account" ;;
        *"consume:device"*) module_type="consume:device" ;;
        *"consume:report"*) module_type="consume:report" ;;
        *"attendance"*) module_type="attendance" ;;
        *"access:area"*) module_type="access:area" ;;
        *"access:config"*) module_type="access:config" ;;
        *"access:record"*) module_type="access:record" ;;
        *"smart-video"*"device"*) module_type="video:device" ;;
        *"cache"*) module_type="cache" ;;
        *"config"*) module_type="config" ;;
    esac

    if [ -z "$module_type" ]; then
        echo "⚠️  无法确定模块类型，跳过: $file"
        continue
    fi

    # 快速添加关键权限控制
    case "$module_type" in
        "consume:account")
            sed -i 's|@click="showCreateAccountModal"|& v-permission="['\''consume:account:create'\'"]"|g' "$FRONTEND_DIR/$file"
            sed -i 's|@click="batchRecharge"|& v-permission="['\''consume:account:recharge'\'"]"|g' "$FRONTEND_DIR/$file"
            sed -i 's|@click="freezeAccount"|& v-permission="['\''consume:account:freeze'\'"]"|g' "$FRONTEND_DIR/$file"
            sed -i 's|@click="unfreezeAccount"|& v-permission="['\''consume:account:unfreeze'\'"]"|g' "$FRONTEND_DIR/$file"
            ;;
        "attendance")
            sed -i 's|@click=".*[Pp]unch.*"|& v-permission="['\''attendance:punch:in'\'','\''attendance:punch:out'\'']"|g' "$FRONTEND_DIR/$file"
            sed -i 's|@click=".*[Ee]xport.*"|& v-permission="['\''attendance:export'\'"]"|g' "$FRONTEND_DIR/$file"
            ;;
        "video:device")
            sed -i 's|@click="addDevice"|& v-permission="['\''video:device:add'\'"]"|g' "$FRONTEND_DIR/$file"
            sed -i 's|@click="editDevice"|& v-permission="['\''video:device:update'\'"]"|g' "$FRONTEND_DIR/$file"
            sed -i 's|@click="deleteDevice"|& v-permission="['\''video:device:delete'\'"]"|g' "$FRONTEND_DIR/$file"
            ;;
        "cache")
            sed -i 's|@click="refreshCache"|& v-permission="['\''cache:operate:get'\'']"|g' "$FRONTEND_DIR/$file"
            sed -i 's|@click="clearCache"|& v-permission="['\''cache:manage:clear'\'']"|g' "$FRONTEND_DIR/$file"
            ;;
    esac

    echo "✅ 修复完成: $file"
done

echo ""
echo "🔍 检查修复结果..."
vue_with_permission=$(find "$FRONTEND_DIR/src/views" -name "*.vue" -exec grep -l "v-permission" {} \; | wc -l)
total_vue=$(find "$FRONTEND_DIR/src/views" -name "*.vue" | wc -l)
coverage=$(echo "scale=1; $vue_with_permission * 100 / $total_vue" | bc)

echo "📊 修复结果："
echo "   有权限控制的Vue文件: $vue_with_permission"
echo "   总Vue文件数: $total_vue"
echo "   权限控制覆盖率: $coverage%"

echo ""
echo "🎉 快速权限控制修复完成！"
echo "📋 下一步：运行完整的项目验证"
echo "   ./scripts/comprehensive-validation.sh"