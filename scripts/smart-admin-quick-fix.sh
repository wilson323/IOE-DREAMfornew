#!/bin/bash

# SmartAdmin快速修复脚本
# 自动修复常见的编译错误

echo "🔧 SmartAdmin快速修复开始..."
echo "=============================="

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
    echo -e "${BLUE}🔧 $1${NC}"
}

# 修复计数
fix_count=0

# 函数：执行修复并计数
fix_and_count() {
    local description="$1"
    local command="$2"

    print_info "修复: $description"
    if eval "$command" > /dev/null 2>&1; then
        print_success "  ✓ 修复完成"
        ((fix_count++))
    else
        print_warning "  ⚠ 未找到需要修复的内容"
    fi
}

echo ""
print_info "开始执行常见问题自动修复..."
echo ""

# 修复1：Entity字段名错误
echo "📝 修复1: Entity字段名错误"
echo "--------------------"

# 获取非Entity目录的Java文件
java_files=$(find . -name "*.java" -not -path "*/domain/entity/*" 2>/dev/null)

fix_and_count "getVideoDeviceId -> getDeviceId" \
    "find . -name '*.java' -not -path '*/domain/entity/*' -exec sed -i 's/getVideoDeviceId()/getDeviceId()/g' {} \;"

fix_and_count "setVideoDeviceId -> setDeviceId" \
    "find . -name '*.java' -not -path '*/domain/entity/*' -exec sed -i 's/setVideoDeviceId(/setDeviceId(/g' {} \;"

fix_and_count "getVideoRecordId -> getRecordId" \
    "find . -name '*.java' -not -path '*/domain/entity/*' -exec sed -i 's/getVideoRecordId()/getRecordId()/g' {} \;"

fix_and_count "setVideoRecordId -> setRecordId" \
    "find . -name '*.java' -not -path '*/domain/entity/*' -exec sed -i 's/setVideoRecordId(/setRecordId(/g' {} \;"

# 修复2：DAO方法名错误
echo ""
echo "📝 修复2: DAO方法名错误"
echo "-------------------"

fix_and_count "pageQuery -> selectPage" \
    "find . -name '*.java' -exec sed -i 's/\.pageQuery(/.selectPage(/g' {} \;"

# 修复3：导入路径错误
echo ""
echo "📝 修复3: 导入路径错误"
echo "-------------------"

fix_and_count "entity.BaseEntity -> domain.entity.BaseEntity" \
    "find . -name '*.java' -exec sed -i 's/import net\.lab1024\.sa\.base\.common\.entity\.BaseEntity/import net.lab1024.sa.base.common.domain.entity.BaseEntity/g' {} \;"

fix_and_count "entity.VideoDeviceEntity -> domain.entity.VideoDeviceEntity" \
    "find . -name '*.java' -exec sed -i 's/import net\.lab1024\.sa\.base\.common\.entity\.VideoDeviceEntity/import net.lab1024.sa.base.common.domain.entity.VideoDeviceEntity/g' {} \;"

fix_and_count "entity.VideoRecordEntity -> domain.entity.VideoRecordEntity" \
    "find . -name '*.java' -exec sed -i 's/import net\.lab1024\.sa\.base\.common\.entity\.VideoRecordEntity/import net.lab1024.sa.base.common.domain.entity.VideoRecordEntity/g' {} \;"

fix_and_count "entity.MonitorEventEntity -> domain.entity.MonitorEventEntity" \
    "find . -name '*.java' -exec sed -i 's/import net\.lab1024\.sa\.base\.common\.entity\.MonitorEventEntity/import net.lab1024.sa.base.common.domain.entity.MonitorEventEntity/g' {} \;"

# 修复4：SmartBeanUtil使用错误
echo ""
echo "📝 修复4: SmartBeanUtil使用错误"
echo "------------------------"

# 这个修复比较复杂，需要检查每个使用的上下文
print_info "检查SmartBeanUtil使用情况..."
smart_bean_files=$(find . -name "*.java" -exec grep -l "SmartBeanUtil\.copy.*\.class" {} \; 2>/dev/null)

if [ ! -z "$smart_bean_files" ]; then
    print_warning "发现SmartBeanUtil.class使用，需要手动修复:"
    for file in $smart_bean_files; do
        echo "  - $file"
    done
    print_info "建议的修复方式："
    echo "  SmartBeanUtil.copy(form, entity) 而不是 SmartBeanUtil.copy(form, Entity.class)"
else
    print_success "SmartBeanUtil使用正确 ✓"
fi

# 修复5：@Autowired -> @Resource
echo ""
echo "📝 修复5: 依赖注入注解"
echo "------------------"

autowired_count=$(find . -name "*.java" -exec grep -l "@Autowired" {} \; 2>/dev/null | wc -l)
if [ $autowired_count -gt 0 ]; then
    fix_and_count "@Autowired -> @Resource" \
        "find . -name '*.java' -exec sed -i 's/@Autowired/@Resource/g' {} \;"
else
    print_success "@Resource使用正确 ✓"
fi

# 修复6：javax -> jakarta
echo ""
echo "📝 修复6: 包名规范"
echo "---------------"

javax_count=$(find . -name "*.java" -exec grep -l "javax\." {} \; 2>/dev/null | wc -l)
if [ $javax_count -gt 0 ]; then
    print_warning "发现javax包名使用，但用户要求统一使用javax，跳过修复"
else
    print_success "包名规范正确 ✓"
fi

# 修复7：Commons Lang3导入
echo ""
echo "📝 修复7: Commons Lang3导入"
echo "----------------------"

fix_and_count "添加Commons Lang3导入" \
    "find . -name '*.java' -exec grep -l 'StringUtils\.' {} \; | xargs -I {} sh -c 'if ! grep -q \"org.apache.commons.lang3\" \"{}\"; then sed -i \"/import.*;/a import org.apache.commons.lang3.StringUtils;\" \"{}\"; fi'"

# 修复8：Logger导入
echo ""
echo "📝 修复8: Logger导入"
echo "-----------------"

fix_and_count "Logger导入" \
    "find . -name '*.java' -exec grep -l 'private.*Logger.*log' {} \; | xargs -I {} sh -c 'if ! grep -q \"import org.slf4j\" \"{}\"; then sed -i \"/import.*;/a import org.slf4j.Logger;\" \"{}\"; fi'"

fix_and_count "Logger注解导入" \
    "find . -name '*.java' -exec grep -l '@Slf4j' {} \; | xargs -I {} sh -c 'if ! grep -q \"import lombok.extern.slf4j.Slf4j\" \"{}\"; then sed -i \"/import.*;/a import lombok.extern.slf4j.Slf4j;\" \"{}\"; fi'"

# 编译测试
echo ""
echo "📝 修复完成，进行编译测试"
echo "===================="

print_info "执行Maven编译..."
mvn_output=$(mvn clean compile -DskipTests 2>&1)
mvn_exit_code=$?

if [ $mvn_exit_code -eq 0 ]; then
    print_success "🎉 编译通过！所有修复成功！"
else
    print_error "编译仍有错误，显示前20行错误信息："
    echo "$mvn_output" | head -20

    echo ""
    print_info "可能需要手动修复的问题："
    echo "1. Entity字段名仍不匹配"
    echo "2. DAO方法不存在"
    echo "3. SmartBeanUtil参数类型错误"
    echo "4. 缺少依赖或配置问题"
fi

# 总结
echo ""
echo "=============================="
echo "🔧 SmartAdmin快速修复完成"
echo "自动修复数量: $fix_count"

if [ $mvn_exit_code -eq 0 ]; then
    print_success "✅ 项目编译成功，可以继续开发"
    echo ""
    echo "下一步建议："
    echo "1. 运行测试: mvn test"
    echo "2. 启动应用: mvn spring-boot:run"
    echo "3. 检查日志输出确认无异常"
else
    print_warning "⚠ 仍有编译错误，需要手动处理"
    echo ""
    echo "手动修复建议："
    echo "1. 逐一修复编译错误"
    echo "2. 运行 ./scripts/entity-field-validator.sh 检查字段名"
    echo "3. 运行 ./scripts/smart-admin-validator.sh 完整验证"
fi

exit $mvn_exit_code