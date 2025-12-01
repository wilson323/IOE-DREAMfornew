#!/bin/bash

# Entity字段名验证脚本
# 专门用于检查Entity字段名是否被正确使用

echo "🔍 Entity字段名验证开始..."
echo "================================"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_info() {
    echo -e "${BLUE}🔍 $1${NC}"
}

# 步骤1：提取所有Entity的字段信息
echo ""
print_info "步骤1：提取Entity字段信息"
echo "------------------------"

entity_fields_file="/tmp/entity_fields.txt"
> "$entity_fields_file"

entity_files=$(find . -name "*Entity.java" -path "*/domain/entity/*" 2>/dev/null)

for entity_file in $entity_files; do
    entity_name=$(basename "$entity_file" .java)
    echo "=== $entity_name ===" >> "$entity_fields_file"

    # 提取private字段名
    grep "private.*;" "$entity_file" | while read line; do
        # 提取字段名 (去掉类型和修饰符)
        field_name=$(echo "$line" | sed -n 's/.*private.*[[:space:]]\([a-zA-Z_][a-zA-Z0-9_]*\)[[:space:]]*;.*/\1/p')
        if [ ! -z "$field_name" ]; then
            echo "  FIELD: $field_name" >> "$entity_fields_file"
            # 推断getter方法名
            getter_name="get$(echo ${field_name:0:1} | tr '[:lower:]' '[:upper:]')${field_name:1}"
            echo "  GETTER: $getter_name" >> "$entity_fields_file"
            # 推断setter方法名
            setter_name="set$(echo ${field_name:0:1} | tr '[:lower:]' '[:upper:]')${field_name:1}"
            echo "  SETTER: $setter_name" >> "$entity_fields_file"
        fi
    done
    echo "" >> "$entity_fields_file"
done

print_success "Entity字段信息提取完成，保存到: $entity_fields_file"

# 步骤2：检查代码中使用的字段名
echo ""
print_info "步骤2：检查代码中的字段名使用"
echo "---------------------------"

# 常见错误模式
error_patterns=(
    "getVideoDeviceId"   # 应该是 getDeviceId
    "setVideoDeviceId"   # 应该是 setDeviceId
    "getVideoRecordId"   # 应该是 getRecordId
    "setVideoRecordId"   # 应该是 setRecordId
    "getVideoId"         # 可能的错误
    "setVideoId"         # 可能的错误
    "pageQuery"          # 应该是 selectPage
)

total_errors=0

for pattern in "${error_patterns[@]}"; do
    echo ""
    print_info "检查模式: $pattern"

    # 查找使用该模式的文件
    pattern_files=$(find . -name "*.java" -exec grep -l "$pattern" {} \; 2>/dev/null)
    pattern_count=$(echo "$pattern_files" | wc -l)

    if [ $pattern_count -gt 0 ]; then
        print_error "发现错误模式 '$pattern' 在 $pattern_count 个文件中:"

        # 显示具体位置
        find . -name "*.java" -exec grep -Hn "$pattern" {} \; 2>/dev/null | while read line; do
            file_path=$(echo "$line" | cut -d: -f1)
            line_num=$(echo "$line" | cut -d: -f2)
            content=$(echo "$line" | cut -d: -f3-)

            # 只显示非Entity文件中的错误（Entity文件中这些可能是正确的）
            if [[ ! "$file_path" =~ "domain/entity" ]]; then
                print_error "  $file_path:$line_num: $content"
                ((total_errors++))
            fi
        done
    else
        print_success "未发现错误模式 '$pattern' ✓"
    fi
done

# 步骤3：检查可能的正确替代方案
echo ""
print_info "步骤3：建议的正确替代方案"
echo "------------------------"

suggestions=(
    "getVideoDeviceId -> getDeviceId"
    "setVideoDeviceId -> setDeviceId"
    "getVideoRecordId -> getRecordId"
    "setVideoRecordId -> setRecordId"
    "pageQuery -> selectPage"
    "videoDeviceDao.selectPage -> videoDeviceDao.selectPage"
    "SmartBeanUtil.copy(form, Entity.class) -> SmartBeanUtil.copy(form, entity)"
)

for suggestion in "${suggestions[@]}"; do
    print_info "💡 $suggestion"
done

# 步骤4：生成修复建议
echo ""
print_info "步骤4：生成修复建议"
echo "----------------"

if [ $total_errors -gt 0 ]; then
    print_error "发现 $total_errors 个潜在的字段名错误"
    echo ""
    print_info "自动修复建议："

    # 生成sed修复命令
    echo "# 可以尝试以下sed命令进行批量修复："
    echo "find . -name '*.java' -not -path '*/domain/entity/*' -exec sed -i 's/getVideoDeviceId()/getDeviceId()/g' {} \;"
    echo "find . -name '*.java' -not -path '*/domain/entity/*' -exec sed -i 's/setVideoDeviceId(/setDeviceId(/g' {} \;"
    echo "find . -name '*.java' -not -path '*/domain/entity/*' -exec sed -i 's/getVideoRecordId()/getRecordId()/g' {} \;"
    echo "find . -name '*.java' -not -path '*/domain/entity/*' -exec sed -i 's/setVideoRecordId(/setRecordId(/g' {} \;"
    echo ""

    # 手动检查建议
    print_warning "建议手动检查以下内容："
    echo "1. 确认Entity类中的实际字段名"
    echo "2. 检查Service和Manager类中的方法调用"
    echo "3. 验证DAO接口中定义的方法名"
    echo "4. 确认Controller中的参数传递"

else
    print_success "🎉 未发现明显的字段名错误！"
    echo ""
    print_info "建议："
    echo "1. 运行完整编译验证: mvn clean compile -DskipTests"
    echo "2. 如果仍有编译错误，检查具体的错误信息"
    echo "3. 确认所有import语句正确"
fi

echo ""
echo "================================"
echo "🔍 Entity字段名验证完成"

# 显示Entity字段信息
if [ -f "$entity_fields_file" ]; then
    echo ""
    print_info "Entity字段信息参考："
    echo "====================="
    cat "$entity_fields_file"
fi

exit $total_errors