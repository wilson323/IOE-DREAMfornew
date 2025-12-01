#!/bin/bash
# SmartAdmin 严格包名分层检查脚本
# 基于路径严格分层：配置层允许javax，业务层禁止javax
# 版本: v1.0.0

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m'

# 项目路径
PROJECT_ROOT=$(cd "$(dirname "$0")/.." && pwd)
BACKEND_DIR="$PROJECT_ROOT/smart-admin-api-java17-springboot3"

# 输出函数
print_header() {
    echo -e "${BLUE}🔒 SmartAdmin 严格包名分层检查${NC}"
    echo -e "${CYAN}📅 检查时间: $(date '+%Y-%m-%d %H:%M:%S')${NC}"
    echo -e "${BLUE}$(printf '─%.0s' {1##60})${NC}"
}

print_section() {
    echo -e "\n${PURPLE}📋 $1${NC}"
    echo -e "${PURPLE}$(printf '─%.0s' {1##50})${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_info() {
    echo -e "${CYAN}ℹ️  $1${NC}"
}

# 错误计数
ERROR_COUNT=0

# 1. 检查配置层javax使用（允许）
check_config_layer_javax() {
    print_section "检查配置层javax使用（允许范围）"

    find "$BACKEND_DIR" -path "*/config/*Config*.java" -o -path "*/listener/*Listener*.java" -o -path "*/filter/*Filter*.java" -name "*.java" 2>/dev/null | while read -r file; do
        if [ -f "$file" ]; then
            local jakarta_count=$(grep -c "jakarta\." "$file" 2>/dev/null || echo "0")
            local javax_count=$(grep -c "javax\." "$file" 2>/dev/null || echo "0")

            if [ "$jakarta_count" -gt 0 ]; then
                print_info "  ${GREEN}  - $(basename "$file")${NC} (使用 jakarta.*: $jakarta_count)"
            fi

            if [ "$javax_count" -gt 0 ]; then
                # 检查是否为允许的javax使用
                local datasource_count=$(grep -c "javax\.sql\.DataSource" "$file" 2>/dev/null || echo "0")
                local validation_count=$(grep -c "javax\.validation" "$file" 2>/dev/null || echo "0")
                local servlet_count=$(grep -c "javax\.servlet" "$file" 2>/dev/null || echo "0")
                local allowed_count=$((datasource_count + validation_count + servlet_count))

                if [ "$javax_count" -eq "$allowed_count" ]; then
                    print_info "  ${YELLOW}  - $(basename "$file")${NC} (允许使用 $javax_count 个javax)"
                else
                    print_error "  ${RED}  - $(basename "$file")${NC} (发现违规 javax 使用)"
                    grep -n "javax\." "$file" | grep -v "javax\.sql\.DataSource\|javax\.validation\|javax\.servlet" | while read -r line; do
                        echo -e "    ${RED}    $line${NC}"
                    done
                    ERROR_COUNT=$((ERROR_COUNT + 1))
                fi
            fi
        fi
    done
}

# 2. 检查业务层javax使用（严格禁止）
check_business_layer_javax() {
    print_section "检查业务层javax使用（严格禁止）"

    local business_patterns=(
        "*/controller/*.java"
        "*/service/*.java"
        "*/manager/*.java"
        "*/dao/*.java"
        "*/module/**/*.java"
    )

    local violation_files=()

    for pattern in "${business_patterns[@]}"; do
        find "$BACKEND_DIR" -path "$pattern" -name "*.java" 2>/dev/null | while read -r file; do
            if [ -f "$file" ]; then
                # 排除配置文件
                if [[ "$file" == *"/config/"* || "$file" == *"/Config"* ]]; then
                    continue
                fi

                local javax_count=$(grep -c "javax\." "$file" 2>/dev/null || echo "0")
                if [ "$javax_count" -gt 0 ]; then
                    print_error "  ${RED}  - $(basename "$file")${NC} (业务层禁止使用javax，发现 $javax_count 处)"

                    # 显示具体违规行
                    grep -n "javax\." "$file" | while read -r line; do
                        echo -e "    ${RED}    $line${NC}"
                    done

                    violation_files+=("$file")
                    ERROR_COUNT=$((ERROR_COUNT + 1))
                fi
            fi
        done
    done

    if [ ${#violation_files[@]} -eq 0 ]; then
        print_success "业务层javax使用检查通过，未发现违规使用"
    else
        print_error "业务层发现 ${#violation_files[@]} 个违规文件，必须修复"
    fi
}

# 3. 生成违规修复建议
generate_fix_suggestions() {
    if [ $ERROR_COUNT -gt 0 ]; then
        print_section "违规修复建议"

        echo -e "${CYAN}📝 自动修复建议:${NC}"
        echo -e "${CYAN}# 1. 业务层 javax → jakarta 批量修复${NC}"
        echo -e "${YELLOW}find $BACKEND_DIR/src/main/java/net/lab1024/sa/admin/module -name '*.java' -exec sed -i 's/javax\\./jakarta\\./g' {} \\;${NC}"
        echo ""
        echo -e "${CYAN}# 2. Controller层 javax → jakarta 修复${NC}"
        echo -e "${YELLOW}find $BACKEND_DIR/src/main/java/net/lab1024/sa/admin/controller -name '*.java' -exec sed -i 's/javax\\./jakarta\\./g' {} \\;${NC}"
        echo ""
        echo -e "${CYAN}# 3. Service层 javax → jakarta 修复${NC}"
        echo -e "${YELLOW}find $BACKEND_DIR/src/main/java/net/lab1024/sa/admin/service -name '*.java' -exec sed -i 's/javax\\./jakarta\\./g' {} \\;${NC}"
        echo ""

        echo -e "${CYAN}⚠️  手动检查注意事项:${NC}"
        echo -e "${YELLOW}1. 配置层的 javax.sql.DataSource 保持不变${NC}"
        echo -e "${YELLOW}2. 配置层的 javax.validation 相关保持不变${NC}"
        echo -e "${YELLOW}3. 修复后运行 ./scripts/strict-package-check.sh 验证${NC}"
        echo ""
    fi
}

# 4. 输出统计信息
print_statistics() {
    print_section "检查统计"

    local total_java_files=$(find "$BACKEND_DIR" -name "*.java" | wc -l)
    local jakarta_files=$(find "$BACKEND_DIR" -name "*.java" -exec grep -l "jakarta\." {} \; 2>/dev/null | wc -l)
    local javax_files=$(find "$BACKEND_DIR" -name "*.java" -exec grep -l "javax\." {} \; 2>/dev/null | wc -l)

    echo -e "${CYAN}📊 文件统计:${NC}"
    echo -e "  总Java文件数: $total_java_files"
    echo -e "  使用jakarta.*的文件: $jakarta_files"
    echo -e "  使用javax.*的文件: $javax_files"
    echo ""

    if [ "$jakarta_files" -gt "$javax_files" ]; then
        echo -e "${GREEN}✅ jakarta包迁移进度良好${NC}"
    else
        echo -e "${YELLOW}⚠️  jakarta包迁移需要继续推进${NC}"
    fi
}

# 主函数
main() {
    print_header

    # 执行检查
    check_config_layer_javax
    check_business_layer_javax
    generate_fix_suggestions
    print_statistics

    # 输出结果
    echo -e "\n${BLUE}🏁 严格包名分层检查完成${NC}"
    echo -e "${BLUE}$(printf '─%.0s' {1##60})${NC}"

    if [ $ERROR_COUNT -eq 0 ]; then
        echo -e "${GREEN}🎉 检查通过！包名分层使用规范${NC}"
        exit 0
    else
        echo -e "${RED}🚨 发现 $ERROR_COUNT 个违规，必须修复${NC}"
        exit 1
    fi
}

# 脚本入口
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi