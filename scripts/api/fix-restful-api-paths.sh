#!/bin/bash
# ============================================================
# IOE-DREAM RESTful API路径规范修复脚本
# 修复不规范的API路径，添加版本前缀，统一接口设计
# ============================================================

echo "🔧 开始RESTful API路径规范修复..."
echo "修复时间: $(date)"
echo "=================================="

# 定义标准API路径规范
declare -A path_mappings=(
    # 登录相关接口
    ["/login"]="/api/v1/auth/login"
    ["/api/consume/account"]="/api/v1/consume/account"
    ["/api/consume/refund"]="/api/v1/consume/refund"
    ["/fallback"]="/api/v1/fallback"
    # 移动端接口保持现状（已符合规范）
)

# 查找需要修复的Controller文件
echo "🔍 搜索需要修复的Controller文件..."
files_to_fix=()
while IFS= read -r -d '' file; do
    # 检查是否有不规范的@RequestMapping
    if grep -q "@RequestMapping.*\".*\"" "$file" && ! grep -q "@RequestMapping.*/api/v[0-9]" "$file"; then
        # 排除actuator和已经规范的移动端接口
        if ! grep -q "@RequestMapping.*/actuator\|/api/mobile/v[0-9]" "$file"; then
            files_to_fix+=("$file")
        fi
    fi
done < <(find microservices -name "*Controller.java" -type f -print0)

echo "📊 发现 ${#files_to_fix[@]} 个Controller文件需要检查"

# 修复函数
fix_api_paths() {
    local file="$1"
    echo "📝 检查文件: $file"

    # 创建备份
    cp "$file" "${file}.backup.$(date +%Y%m%d_%H%M%S)"

    local fixed=false
    local original_path=""
    local new_path=""

    # 查找@RequestMapping注解
    while read -r line; do
        if [[ $line =~ @RequestMapping.*\"([^\"]+)\" ]]; then
            original_path="${BASH_REMATCH[1]}"

            # 检查是否需要修复
            if [[ ! "$original_path" =~ ^/api/v[0-9] ]] && [[ ! "$original_path" =~ ^/api/mobile/v[0-9] ]] && [[ ! "$original_path" =~ ^/actuator ]]; then
                # 确定修复后的路径
                case "$original_path" in
                    "/login")
                        new_path="/api/v1/auth/login"
                        ;;
                    "/api/consume/account")
                        new_path="/api/v1/consume/account"
                        ;;
                    "/api/consume/refund")
                        new_path="/api/v1/consume/refund"
                        ;;
                    "/fallback")
                        new_path="/api/v1/fallback"
                        ;;
                    *)
                        # 默认添加/api/v1前缀
                        if [[ "$original_path" =~ ^/api/ ]]; then
                            new_path="/api/v1${original_path:4}"
                        else
                            new_path="/api/v1$original_path"
                        fi
                        ;;
                esac

                echo "  🔧 修复路径: $original_path -> $new_path"
                sed -i "s|@RequestMapping(\"$original_path\")|@RequestMapping(\"$new_path\")|g" "$file"
                fixed=true
            fi
        fi
    done < <(grep "@RequestMapping" "$file")

    if [[ "$fixed" == "true" ]]; then
        echo "✅ 修复完成: $file"
    else
        echo "ℹ️ 无需修复: $file (已符合规范)"
    fi
}

# 执行修复
for file in "${files_to_fix[@]}"; do
    fix_api_paths "$file"
done

echo "=================================="
echo "✅ RESTful API路径规范修复完成！"
echo "=================================="

# 验证修复结果
echo "🔍 验证修复结果..."
non_compliant_count=0
while IFS= read -r -d '' file; do
    if grep -q "@RequestMapping" "$file"; then
        if grep -v "@RequestMapping.*/api/v[0-9]\|@RequestMapping.*/api/mobile/v[0-9]\|@RequestMapping.*/actuator" "$file" | grep -q "@RequestMapping"; then
            echo "⚠️ 仍有不规范的API路径: $file"
            ((non_compliant_count++))
        fi
    fi
done < <(find microservices -name "*Controller.java" -type f -print0)

if [[ $non_compliant_count -eq 0 ]]; then
    echo "🎉 所有API路径已符合RESTful规范！"
else
    echo "⚠️ 发现 $non_compliant_count 个Controller仍有不规范的API路径"
fi

# 统计修复结果
echo "📊 修复后统计："
total_controllers=$(find microservices -name "*Controller.java" -type f | wc -l)
echo "✅ Controller总数: $total_controllers"

versioned_paths=$(find microservices -name "*Controller.java" -exec grep -l "@RequestMapping.*/api/v[0-9]" {} \; | wc -l)
echo "✅ 使用版本前缀的Controller: $versioned_paths"

mobile_paths=$(find microservices -name "*Controller.java" -exec grep -l "@RequestMapping.*/api/mobile/v[0-9]" {} \; | wc -l)
echo "✅ 移动端Controller: $mobile_paths"

echo "=================================="
echo "🎯 建议后续步骤："
echo "1. 更新前端API调用路径"
echo "2. 更新移动端API调用路径"
echo "3. 更新API文档"
echo "4. 运行集成测试验证"
echo "=================================="