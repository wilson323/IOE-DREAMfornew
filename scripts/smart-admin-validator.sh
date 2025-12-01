#!/bin/bash

# SmartAdmin四层架构验证脚本
# 作者：老王
# 用途：在开发新功能前验证Entity、DAO、导入路径等关键组件

echo "🔍 SmartAdmin四层架构验证开始..."
echo "=================================================="

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 错误计数
ERROR_COUNT=0

# 打印函数
print_error() {
    echo -e "${RED}❌ $1${NC}"
    ((ERROR_COUNT++))
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_info() {
    echo -e "🔍 $1"
}

# Phase 1: Entity层验证
echo ""
print_info "Phase 1: Entity层验证"
echo "------------------------"

entity_files=$(find . -name "*Entity.java" -path "*/domain/entity/*" 2>/dev/null)
if [ -z "$entity_files" ]; then
    print_error "未找到任何Entity文件"
else
    print_info "找到Entity文件数量: $(echo "$entity_files" | wc -l)"

    for entity_file in $entity_files; do
        entity_name=$(basename "$entity_file" .java)
        print_info "检查Entity: $entity_name"

        # 检查是否继承BaseEntity
        if grep -q "extends BaseEntity" "$entity_file"; then
            print_success "  - 继承BaseEntity ✓"
        else
            print_error "  - 未继承BaseEntity: $entity_file"
        fi

        # 检查@Data注解
        if grep -q "@Data" "$entity_file"; then
            print_success "  - @Data注解存在 ✓"
        else
            print_error "  - 缺少@Data注解: $entity_file"
        fi

        # 检查private字段
        field_count=$(grep -c "private.*;" "$entity_file")
        if [ $field_count -gt 0 ]; then
            print_success "  - 字段定义: $field_count个 ✓"
        else
            print_warning "  - 未发现字段定义: $entity_file"
        fi

        # 检查导入路径
        if grep -q "import net.lab1024.sa.base.common.entity.BaseEntity" "$entity_file"; then
            print_success "  - BaseEntity导入正确 ✓"
        else
            print_warning "  - BaseEntity导入可能有问题"
        fi
    done
fi

# Phase 2: DAO层验证
echo ""
print_info "Phase 2: DAO层验证"
echo "---------------------"

dao_files=$(find . -name "*Dao.java" -path "*/dao/*" 2>/dev/null)
if [ -z "$dao_files" ]; then
    print_error "未找到任何DAO文件"
else
    print_info "找到DAO文件数量: $(echo "$dao_files" | wc -l)"

    for dao_file in $dao_files; do
        dao_name=$(basename "$dao_file" .java)
        print_info "检查DAO: $dao_name"

        # 检查是否继承BaseMapper
        if grep -q "extends BaseMapper" "$dao_file"; then
            print_success "  - 继承BaseMapper ✓"
        else
            print_error "  - 未继承BaseMapper: $dao_file"
        fi

        # 检查@Mapper注解
        if grep -q "@Mapper" "$dao_file"; then
            print_success "  - @Mapper注解存在 ✓"
        else
            print_error "  - 缺少@Mapper注解: $dao_file"
        fi

        # 检查import语句
        import_count=$(grep -c "import.*" "$dao_file")
        if [ $import_count -gt 0 ]; then
            print_success "  - 导入语句: $import_count个 ✓"
        else
            print_warning "  - 缺少导入语句: $dao_file"
        fi
    done
fi

# Phase 3: 导入路径验证
echo ""
print_info "Phase 3: 导入路径验证"
echo "---------------------"

# 检查javax包名使用
javax_files=$(find . -name "*.java" -exec grep -l "javax\." {} \; 2>/dev/null)
javax_count=$(echo "$javax_files" | wc -l)
if [ $javax_count -eq 0 ]; then
    print_success "javax包名使用: 0个 ✓"
else
    print_error "javax包名使用: $javax_count个 (应该为0)"
    if [ $javax_count -le 10 ]; then
        echo "涉及的文件:"
        echo "$javax_files" | head -10
    fi
fi

# 检查@Autowired使用
autowired_files=$(find . -name "*.java" -exec grep -l "@Autowired" {} \; 2>/dev/null)
autowired_count=$(echo "$autowired_files" | wc -l)
if [ $autowired_count -eq 0 ]; then
    print_success "@Autowired使用: 0个 ✓"
else
    print_error "@Autowired使用: $autowired_count个 (应该为0)"
    if [ $autowired_count -le 10 ]; then
        echo "涉及的文件:"
        echo "$autowired_files" | head -10
    fi
fi

# Phase 4: 编译验证
echo ""
print_info "Phase 4: 编译验证"
echo "------------------"

print_info "开始编译验证..."
mvn_output=$(mvn clean compile -DskipTests -q 2>&1)
mvn_exit_code=$?

if [ $mvn_exit_code -eq 0 ]; then
    print_success "Maven编译通过 ✓"
else
    print_error "Maven编译失败"

    # 显示前20行错误信息
    echo "编译错误信息(前20行):"
    echo "$mvn_output" | head -20
fi

# Phase 5: SmartAdmin特有问题检查
echo ""
print_info "Phase 5: SmartAdmin特有问题检查"
echo "---------------------------------"

# 检查Entity字段名常见错误
print_info "检查Entity字段名常见错误..."

# 检查是否使用了错误的字段名
error_patterns=(
    "getVideoDeviceId"
    "getVideoRecordId"
    "setVideoDeviceId"
    "setVideoRecordId"
    "getVideoId"
    "setVideoId"
)

for pattern in "${error_patterns[@]}"; do
    pattern_files=$(find . -name "*.java" -exec grep -l "$pattern" {} \; 2>/dev/null)
    pattern_count=$(echo "$pattern_files" | wc -l)
    if [ $pattern_count -gt 0 ]; then
        print_warning "发现可疑模式 '$pattern': $pattern_count个文件"
        if [ $pattern_count -le 5 ]; then
            echo "$pattern_files"
        fi
    fi
done

# 总结
echo ""
echo "=================================================="
echo "🔍 SmartAdmin四层架构验证完成"

if [ $ERROR_COUNT -eq 0 ] && [ $mvn_exit_code -eq 0 ]; then
    print_success "🎉 所有验证通过！可以开始业务逻辑开发"
    echo ""
    echo "✅ Entity层验证通过"
    echo "✅ DAO层验证通过"
    echo "✅ 导入路径验证通过"
    echo "✅ 编译验证通过"
    echo "✅ SmartAdmin特有问题检查通过"
    exit 0
else
    print_error "发现 $ERROR_COUNT 个错误，编译状态: $([ $mvn_exit_code -eq 0 ] && echo "通过" || echo "失败")"
    echo ""
    echo "❌ 请修复上述错误后再进行开发"
    echo "💡 建议："
    echo "   1. 检查Entity字段名是否正确"
    echo "   2. 验证DAO方法是否存在"
    echo "   3. 确认导入路径是否正确"
    echo "   4. 使用@Resource而非@Autowired"
    echo "   5. 使用jakarta包名而非javax"
    exit 1
fi