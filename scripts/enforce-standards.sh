#!/bin/bash
# SmartAdmin 统一开发规范检查脚本
# 作者: 老王 - 专治各种代码不规范
# 版本: v1.0.0
# 更新: 2025-11-14

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 项目路径
PROJECT_ROOT=$(cd "$(dirname "$0")/.." && pwd)
BACKEND_DIR="$PROJECT_ROOT/smart-admin-api-java17-springboot3"
FRONTEND_DIR="$PROJECT_ROOT/smart-admin-web-javascript"

# 输出函数
print_header() {
    echo -e "${BLUE}🔧 SmartAdmin 统一开发规范检查${NC}"
    echo -e "${CYAN}📅 检查时间: $(date '+%Y-%m-%d %H:%M:%S')${NC}"
    echo -e "${CYAN}📂 项目路径: $PROJECT_ROOT${NC}"
    echo -e "${BLUE}$(printf '─%.0s' {1..60})${NC}"
}

print_section() {
    echo -e "\n${PURPLE}📋 $1${NC}"
    echo -e "${PURPLE}$(printf '─%.0s' {1..50})${NC}"
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
WARNING_COUNT=0

# 检查结果统计
declare -A CHECK_RESULTS

# ==================== 核心规范检查函数 ====================

# 1. 检查javax包使用（一级规范违规）
check_javax_packages() {
    print_section "检查 javax 包使用（一级规范违规）"

    local javax_files=$(find "$BACKEND_DIR" -name "*.java" -exec grep -l "javax\." {} \; 2>/dev/null || true)
    local violation_files=""

    if [ -n "$javax_files" ]; then
        while IFS= read -r file; do
            if [ -f "$file" ]; then
                # 检查是否为允许使用javax的特殊场景
                local is_allowed=false

                # 检查是否为DataSource配置
                if grep -q "javax\.sql\.DataSource" "$file"; then
                    is_allowed=true
                    print_info "  ${YELLOW}  - $file${NC} (允许使用 javax.sql.DataSource)"
                fi

                # 检查是否为验证注解
                if grep -q "javax\.validation" "$file"; then
                    is_allowed=true
                    print_info "  ${YELLOW}  - $file${NC} (允许使用 javax.validation)"
                fi

                # 检查是否在其他业务场景中使用
                if [ "$is_allowed" = false ]; then
                    if grep -v -q "javax\.sql\.DataSource\|javax\.validation" "$file"; then
                        violation_files="$violation_files $file"
                        print_error "  ${RED}  - $file${NC} (业务代码禁止使用javax包)"
                    fi
                fi
            fi
        done <<< "$javax_files"

        if [ -n "$violation_files" ]; then
            print_error "发现违规使用 javax 包的业务文件："
            echo "$violation_files" | while read -r file; do
                if [ -n "$file" ]; then
                    echo -e "  ${RED}  - $file${NC}"
                    # 显示具体的违规行
                    grep -n "javax\." "$file" | grep -v "javax\.sql\.DataSource\|javax\.validation" | while read -r line; do
                        echo -e "    ${RED}    $line${NC}"
                    done
                fi
            done
            ERROR_COUNT=$((ERROR_COUNT + 1))
            CHECK_RESULTS["javax_packages"]="FAILED"
            return 1
        else
            print_success "javax包使用检查通过，所有使用都在允许范围内"
            CHECK_RESULTS["javax_packages"]="PASSED"
            return 0
        fi
    else
        print_success "所有 Java 文件都符合 jakarta 包规范"
        CHECK_RESULTS["javax_packages"]="PASSED"
        return 0
    fi
}

# 2. 检查@Autowired使用（一级规范违规）
check_autowired() {
    print_section "检查 @Autowired 使用（一级规范违规）"

    local autowired_files=$(find "$BACKEND_DIR" -name "*.java" -exec grep -l "@Autowired" {} \; 2>/dev/null || true)

    if [ -n "$autowired_files" ]; then
        print_error "发现使用 @Autowired 的文件（必须改为 @Resource）："
        echo "$autowired_files" | while read -r file; do
            echo -e "  ${RED}  - $file${NC}"
            # 显示具体行号
            grep -n "@Autowired" "$file" | while read -r line; do
                echo -e "    ${RED}    $line${NC}"
            done
        done
        ERROR_COUNT=$((ERROR_COUNT + 1))
        CHECK_RESULTS["autowired"]="FAILED"
        return 1
    else
        print_success "所有文件都使用 @Resource 依赖注入"
        CHECK_RESULTS["autowired"]="PASSED"
        return 0
    fi
}

# 3. 检查System.out.println使用（一级规范违规）
check_system_out() {
    print_section "检查 System.out.println 使用（一级规范违规）"

    local system_out_files=$(find "$BACKEND_DIR" -name "*.java" -exec grep -l "System\.out\.println\|System\.err\.println" {} \; 2>/dev/null || true)

    if [ -n "$system_out_files" ]; then
        print_error "发现使用 System.out.println 的文件（必须使用日志框架）："
        echo "$system_out_files" | while read -r file; do
            echo -e "  ${RED}  - $file${NC}"
            grep -n "System\.out\.println\|System\.err\.println" "$file" | while read -r line; do
                echo -e "    ${RED}    $line${NC}"
            done
        done
        ERROR_COUNT=$((ERROR_COUNT + 1))
        CHECK_RESULTS["system_out"]="FAILED"
        return 1
    else
        print_success "所有文件都使用日志框架"
        CHECK_RESULTS["system_out"]="PASSED"
        return 0
    fi
}

# 4. 检查BaseEntity继承（一级规范违规）
check_base_entity() {
    print_section "检查实体类 BaseEntity 继承"

    local entity_files=$(find "$BACKEND_DIR" -name "*Entity.java" 2>/dev/null || true)
    local entity_violations=""

    while IFS= read -r -d '' file; do
        if [ -f "$file" ]; then
            # 只检查 sa-admin 目录下的业务Entity类（排除基础框架Entity）
            if [[ "$file" == *"/sa-admin/"* && "$file" != *"BaseEntity.java" ]]; then
                if ! grep -q "extends BaseEntity" "$file"; then
                    entity_violations="$entity_violations $file"
                fi
            fi
        fi
    done < <(find "$BACKEND_DIR" -name "*Entity.java" -print0 2>/dev/null)

    if [ -n "$entity_violations" ]; then
        print_error "发现未继承 BaseEntity 的实体类："
        echo "$entity_violations" | while read -r file; do
            if [ -n "$file" ]; then
                echo -e "  ${RED}  - $file${NC}"
            fi
        done
        print_info "正确示例: public class SmartDeviceEntity extends BaseEntity"
        ERROR_COUNT=$((ERROR_COUNT + 1))
        CHECK_RESULTS["base_entity"]="FAILED"
        return 1
    else
        print_success "所有实体类都继承了 BaseEntity"
        CHECK_RESULTS["base_entity"]="PASSED"
        return 0
    fi
}

# 5. 检查Maven依赖
check_maven_dependencies() {
    print_section "检查 Maven 依赖配置"

    local pom_file="$BACKEND_DIR/sa-base/pom.xml"

    if [ ! -f "$pom_file" ]; then
        print_error "找不到 pom.xml 文件: $pom_file"
        ERROR_COUNT=$((ERROR_COUNT + 1))
        return 1
    fi

    # 检查关键依赖
    local deps_ok=true

    # 检查 Lombok
    if ! grep -q "lombok" "$pom_file"; then
        print_error "缺少 Lombok 依赖"
        deps_ok=false
    fi

    # 检查 Spring Boot 版本
    if ! grep -q "3\." "$pom_file"; then
        print_warning "建议使用 Spring Boot 3.x 版本"
        WARNING_COUNT=$((WARNING_COUNT + 1))
    fi

    if [ "$deps_ok" = true ]; then
        print_success "Maven 依赖配置正常"
        CHECK_RESULTS["maven_deps"]="PASSED"
        return 0
    else
        ERROR_COUNT=$((ERROR_COUNT + 1))
        CHECK_RESULTS["maven_deps"]="FAILED"
        return 1
    fi
}

# 6. 检查编译状态
check_compilation() {
    print_section "检查项目编译状态"

    cd "$BACKEND_DIR"

    # 尝试编译
    if mvn clean compile -q -DskipTests 2>/dev/null; then
        print_success "项目编译成功"
        CHECK_RESULTS["compilation"]="PASSED"
        return 0
    else
        print_error "项目编译失败"
        print_info "运行以下命令查看详细错误："
        echo -e "  ${CYAN}  cd $BACKEND_DIR && mvn clean compile${NC}"
        ERROR_COUNT=$((ERROR_COUNT + 1))
        CHECK_RESULTS["compilation"]="FAILED"
        return 1
    fi
}

# 7. 检查代码复杂度（简化版）
check_code_complexity() {
    print_section "检查代码复杂度"

    local complex_files=""

    while IFS= read -r -d '' file; do
        if [ -f "$file" ]; then
            # 简单的行数检查作为复杂度指标
            local line_count=$(wc -l < "$file" 2>/dev/null || echo "0")
            if [ "$line_count" -gt 200 ]; then
                complex_files="$complex_files $file ($line_count 行)"
            fi
        fi
    done < <(find "$BACKEND_DIR" -name "*.java" -print0 2>/dev/null)

    if [ -n "$complex_files" ]; then
        print_warning "发现可能复杂的文件（超过200行）："
        echo "$complex_files" | while read -r info; do
            if [ -n "$info" ]; then
                echo -e "  ${YELLOW}  - $info${NC}"
            fi
        done
        print_info "建议：复杂方法应该拆分为更小的方法"
        WARNING_COUNT=$((WARNING_COUNT + 1))
        CHECK_RESULTS["complexity"]="WARNING"
    else
        print_success "代码复杂度检查通过"
        CHECK_RESULTS["complexity"]="PASSED"
    fi
}

# 8. 检查命名规范
check_naming_conventions() {
    print_section "检查命名规范"

    local naming_issues=""

    # 检查Controller类命名
    find "$BACKEND_DIR" -name "*Controller.java" 2>/dev/null | while read -r file; do
        if [ -f "$file" ]; then
            local basename=$(basename "$file" .java)
            if [[ ! "$basename" == *Controller ]]; then
                naming_issues="$naming_issues Controller类命名不规范: $basename (应该以Controller结尾)"
            fi
        fi
    done

    # 检查Service类命名
    find "$BACKEND_DIR" -name "*Service.java" 2>/dev/null | while read -r file; do
        if [ -f "$file" ]; then
            local basename=$(basename "$file" .java)
            if [[ ! "$basename" == *Service ]]; then
                naming_issues="$naming_issues Service类命名不规范: $basename (应该以Service结尾)"
            fi
        fi
    done

    if [ -n "$naming_issues" ]; then
        print_warning "发现命名规范问题："
        echo "$naming_issues" | while read -r issue; do
            if [ -n "$issue" ]; then
                echo -e "  ${YELLOW}  - $issue${NC}"
            fi
        done
        WARNING_COUNT=$((WARNING_COUNT + 1))
        CHECK_RESULTS["naming"]="WARNING"
    else
        print_success "命名规范检查通过"
        CHECK_RESULTS["naming"]="PASSED"
    fi
}

# ==================== 前端检查函数 ====================

# 9. 检查前端技术栈
check_frontend_stack() {
    if [ ! -d "$FRONTEND_DIR" ]; then
        print_info "前端目录不存在，跳过前端检查"
        return 0
    fi

    print_section "检查前端技术栈"

    cd "$FRONTEND_DIR"

    # 检查 package.json
    if [ -f "package.json" ]; then
        # 检查 Vue 3
        if grep -q "\"vue\": \"3\." package.json; then
            print_success "使用 Vue 3.x"
        else
            print_warning "建议使用 Vue 3.x"
            WARNING_COUNT=$((WARNING_COUNT + 1))
        fi

        # 检查 Vite
        if grep -q "\"vite\":" package.json; then
            print_success "使用 Vite 构建工具"
        else
            print_warning "建议使用 Vite 构建工具"
            WARNING_COUNT=$((WARNING_COUNT + 1))
        fi
    else
        print_warning "找不到 package.json 文件"
        WARNING_COUNT=$((WARNING_COUNT + 1))
    fi
}

# ==================== 结果输出函数 ====================

print_summary() {
    echo -e "\n${BLUE}📊 规范检查结果统计${NC}"
    echo -e "${BLUE}$(printf '─%.0s' {1..60})${NC}"

    echo -e "${RED}🔴 一级规范违规: $ERROR_COUNT 项${NC}"
    echo -e "${YELLOW}🟡 二级规范违规: $WARNING_COUNT 项${NC}"

    echo -e "\n${PURPLE}📋 详细检查结果:${NC}"
    for key in "${!CHECK_RESULTS[@]}"; do
        case "${CHECK_RESULTS[$key]}" in
            "PASSED")
                echo -e "  ${GREEN}✅ $key: 通过${NC}"
                ;;
            "FAILED")
                echo -e "  ${RED}❌ $key: 失败${NC}"
                ;;
            "WARNING")
                echo -e "  ${YELLOW}⚠️  $key: 警告${NC}"
                ;;
        esac
    done

    # 总体评估
    echo -e "\n${BLUE}🎯 总体评估:${NC}"
    if [ $ERROR_COUNT -eq 0 ]; then
        if [ $WARNING_COUNT -eq 0 ]; then
            echo -e "${GREEN}🎉 恭喜！代码完全符合规范标准${NC}"
            return 0
        else
            echo -e "${YELLOW}⚡ 代码基本符合规范，有少量建议改进项${NC}"
            return 0
        fi
    else
        echo -e "${RED}🚨 代码存在严重规范违规，必须立即修复！${NC}"
        echo -e "${RED}   老王我强调：违反一级规范的代码是写来玩的吗？！${NC}"
        return 1
    fi
}

print_repair_guide() {
    if [ $ERROR_COUNT -gt 0 ]; then
        echo -e "\n${BLUE}🔧 快速修复指南:${NC}"
        echo -e "${CYAN}1. javax → jakarta 包名修复:${NC}"
        echo -e "   find $BACKEND_DIR -name '*.java' -exec sed -i 's/javax\\./jakarta\\./g' {} \\;"
        echo -e ""
        echo -e "${CYAN}2. @Autowired → @Resource 修复:${NC}"
        echo -e "   find $BACKEND_DIR -name '*.java' -exec sed -i 's/@Autowired/@Resource/g' {} \\;"
        echo -e ""
        echo -e "${CYAN}3. 添加 Lombok 依赖:${NC}"
        echo -e "   在 $BACKEND_DIR/sa-base/pom.xml 中添加 lombok 依赖"
        echo -e ""
        echo -e "${CYAN}4. 运行编译测试:${NC}"
        echo -e "   cd $BACKEND_DIR && mvn clean compile -DskipTests"
    fi
}

# ==================== 主函数 ====================

main() {
    print_header

    # 检查项目目录
    if [ ! -d "$BACKEND_DIR" ]; then
        print_error "找不到后端项目目录: $BACKEND_DIR"
        exit 1
    fi

    # 执行所有检查
    check_javax_packages || true
    check_autowired || true
    check_system_out || true
    check_base_entity || true
    check_maven_dependencies || true
    check_compilation || true
    check_code_complexity || true
    check_naming_conventions || true
    check_frontend_stack || true

    # 输出结果
    print_summary
    print_repair_guide

    # 根据结果决定退出码
    if [ $ERROR_COUNT -gt 0 ]; then
        exit 1
    else
        exit 0
    fi
}

# 脚本入口
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi