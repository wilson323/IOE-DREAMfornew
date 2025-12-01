#!/bin/bash

# SmartAdmin开发前强制检查清单
# 在编写任何业务逻辑之前必须执行此脚本

echo "🔍 SmartAdmin开发前强制检查"
echo "=========================="
echo "⚠️  重要：只有全部检查通过后才能开始编码！"
echo ""

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
BOLD='\033[1m'
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

print_critical() {
    echo -e "${RED}${BOLD}🚫 CRITICAL: $1${NC}"
}

# 检查计数和强制退出
CHECKS_PASSED=0
TOTAL_CHECKS=0

# 函数：执行检查
run_check() {
    local check_name="$1"
    local check_command="$2"
    local is_critical="$3"  # 是否为关键检查（失败则强制退出）

    echo ""
    print_info "检查: $check_name"
    echo "------------------------"

    ((TOTAL_CHECKS++))

    if eval "$check_command" > /dev/null 2>&1; then
        print_success "$check_name - 通过 ✓"
        ((CHECKS_PASSED++))
        return 0
    else
        if [ "$is_critical" = "true" ]; then
            print_critical "$check_name - 失败！这是强制检查项，必须修复后才能继续开发"
            echo "执行失败的命令: $check_command"
            echo ""
            echo "💡 修复建议："
            case "$check_name" in
                "编译状态检查")
                    echo "  1. 运行 ./scripts/smart-admin-quick-fix.sh 自动修复"
                    echo "  2. 手动查看编译错误信息并逐一修复"
                    echo "  3. 确保所有Entity字段名正确"
                    ;;
                "Entity文件检查")
                    echo "  1. 确保Entity类继承BaseEntity"
                    echo "  2. 确保Entity类有@Data注解"
                    echo "  3. 确保Entity类有private字段定义"
                    ;;
                "DAO文件检查")
                    echo "  1. 确保DAO接口继承BaseMapper"
                    echo "  2. 确保DAO接口有@Mapper注解"
                    echo "  3. 确保DAO接口有正确的import语句"
                    ;;
                "依赖注入规范检查")
                    echo "  1. 全局搜索@Autowired并替换为@Resource"
                    echo "  2. find . -name '*.java' -exec sed -i 's/@Autowired/@Resource/g' {} \\;"
                    ;;
                "包名规范检查")
                    echo "  1. 全局搜索javax.并替换为jakarta."
                    echo "  2. 或者根据项目要求统一使用javax"
                    ;;
                "SmartBeanUtil使用检查")
                    echo "  1. 检查SmartBeanUtil.copy的参数类型"
                    echo "  2. 确保第一个参数是源对象，第二个是目标对象"
                    echo "  3. 不要使用Entity.class作为参数"
                    ;;
            esac
            echo ""
            echo "🚫 强制退出：必须修复此问题才能继续开发"
            exit 1
        else
            print_warning "$check_name - 失败（非关键项）"
            echo "建议修复，但可以继续开发"
            return 1
        fi
    fi
}

# 开始强制检查
echo "🚨 开始执行SmartAdmin开发前强制检查..."
echo ""

# 关键检查1：编译状态（最重要）
run_check "编译状态检查" "mvn clean compile -DskipTests -q" "true"

# 关键检查2：Entity文件存在性和正确性
run_check "Entity文件检查" \
    "find . -name '*Entity.java' -path '*/domain/entity/*' -exec grep -l 'extends BaseEntity' {} \; | wc -l | grep -q '^0'" false

# 关键检查3：DAO文件存在性和正确性
run_check "DAO文件检查" \
    "find . -name '*Dao.java' -path '*/dao/*' -exec grep -l 'extends BaseMapper' {} \; | wc -l | grep -q '^0'" false

# 关键检查4：依赖注入规范
run_check "依赖注入规范检查" \
    "find . -name '*.java' -exec grep -l '@Autowired' {} \; | wc -l | grep -q '^0'" "true"

# 关键检查5：包名规范
run_check "包名规范检查" \
    "find . -name '*.java' -exec grep -l 'javax\.' {} \; | wc -l | grep -q '^0'" false

# 检查6：SmartBeanUtil使用规范
run_check "SmartBeanUtil使用检查" \
    "find . -name '*.java' -exec grep -l 'SmartBeanUtil\.copy.*\.class' {} \; | wc -l | grep -q '^0'" false

# 检查7：日志规范
run_check "日志规范检查" \
    "find . -name '*.java' -exec grep -l 'System\.out\.println' {} \; | wc -l | grep -q '^0'" false

# 检查8：权限注解检查（Controller层）
run_check "权限注解检查" \
    "find . -name '*Controller.java' -exec grep -l '@PostMapping\|@GetMapping\|@PutMapping\|@DeleteMapping' {} \; | head -5 | xargs grep -l '@SaCheckPermission' | wc -l | grep -q '^0'" false

# 检查9：异常处理规范
run_check "异常处理规范检查" \
    "find . -name '*Service*.java' -exec grep -l 'throws Exception' {} \; | wc -l | grep -q '^0'" false

# 检查10：事务注解检查（Service层）
run_check "事务注解检查" \
    "find . -name '*Service*.java' -exec grep -l '@Transactional' {} \; | wc -l | grep -q '^0'" false

# 生成检查报告
echo ""
echo "=========================="
echo "📊 检查结果统计"
echo "=========================="
echo "总检查项: $TOTAL_CHECKS"
echo "通过检查: $CHECKS_PASSED"
echo "失败检查: $((TOTAL_CHECKS - CHECKS_PASSED))"

if [ $CHECKS_PASSED -eq $TOTAL_CHECKS ]; then
    echo ""
    print_success "🎉 所有检查通过！可以开始安全开发！"
    echo ""
    echo "✅ 开发前检查清单："
    echo "  ✓ 编译状态正常"
    echo "  ✓ Entity文件规范"
    echo "  ✓ DAO文件规范"
    echo "  ✓ 依赖注入规范（@Resource）"
    echo "  ✓ 包名规范"
    echo "  ✓ SmartBeanUtil使用规范"
    echo "  ✓ 日志规范（SLF4J）"
    echo "  ✓ 权限注解规范"
    echo "  ✓ 异常处理规范"
    echo "  ✓ 事务注解规范"
    echo ""
    echo "🚀 现在可以开始开发了！建议遵循以下开发顺序："
    echo "1. Controller层 - 定义API接口"
    echo "2. Service层 - 实现业务逻辑"
    echo "3. Manager层 - 处理复杂业务"
    echo "4. DAO层 - 数据访问"
    echo "5. 单元测试 - 验证功能"
    echo ""
    echo "💡 开发过程中建议："
    echo "  - 每完成一层立即编译验证"
    echo "  - 使用./scripts/entity-field-validator.sh检查字段名"
    echo "  - 遇到编译错误时运行./scripts/smart-admin-quick-fix.sh"
else
    echo ""
    print_critical "❌ 检查未全部通过！存在 $((TOTAL_CHECKS - CHECKS_PASSED)) 个问题"
    echo ""
    echo "🚨 必须修复所有关键检查项后才能继续开发！"
    echo ""
    echo "📋 修复建议："
    echo "1. 运行 ./scripts/smart-admin-quick-fix.sh 自动修复常见问题"
    echo "2. 手动修复编译错误"
    echo "3. 确保遵循SmartAdmin框架规范"
    echo "4. 修复后重新运行此检查脚本"
    echo ""
    echo "🔄 修复完成后重新运行："
    echo "  ./scripts/pre-development-check.sh"
fi

echo ""
echo "=========================="
echo "🔍 SmartAdmin开发前检查完成"

# 返回适当的退出码
if [ $CHECKS_PASSED -eq $TOTAL_CHECKS ]; then
    exit 0
else
    exit 1
fi