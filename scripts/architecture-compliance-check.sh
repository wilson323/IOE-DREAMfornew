#!/bin/bash

################################################################################
# IOE-DREAM 架构符合性检查脚本
# 
# 功能：检查项目是否符合repowiki四层架构规范
# 规范基准：.qoder/repowiki 规范体系
# 
# 检查项：
# 1. Controller层是否直接访问DAO（禁止）
# 2. Service层是否直接访问DAO（允许，但建议通过Manager）
# 3. Engine层是否直接访问DAO（禁止）
# 4. 命名规范是否符合
# 5. 依赖注入是否使用@Resource（禁止@Autowired）
# 6. 是否存在冗余文件（.backup, .bak等）
# 7. 是否存在重复类定义
#
# 使用方法：
#   ./scripts/architecture-compliance-check.sh [模块路径]
#
# 示例：
#   ./scripts/architecture-compliance-check.sh
#   ./scripts/architecture-compliance-check.sh sa-admin/src/main/java/net/lab1024/sa/admin/module/consume
#
# 作者：SmartAdmin规范治理委员会
# 创建时间：2025-11-20
################################################################################

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 项目根目录
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

# 检查路径（如果提供）
CHECK_PATH="${1:-smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module}"

# 统计变量
TOTAL_VIOLATIONS=0
TOTAL_WARNINGS=0
TOTAL_FILES_CHECKED=0

# 结果文件
REPORT_FILE="docs/ARCHITECTURE_COMPLIANCE_REPORT_$(date +%Y%m%d_%H%M%S).md"
TEMP_REPORT="/tmp/arch_check_$$.txt"

echo "" > "$TEMP_REPORT"

################################################################################
# 工具函数
################################################################################

log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
    echo "[INFO] $1" >> "$TEMP_REPORT"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
    echo "[SUCCESS] $1" >> "$TEMP_REPORT"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
    echo "[WARNING] $1" >> "$TEMP_REPORT"
    ((TOTAL_WARNINGS++))
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
    echo "[ERROR] $1" >> "$TEMP_REPORT"
    ((TOTAL_VIOLATIONS++))
}

################################################################################
# 检查1: Controller层直接访问DAO（禁止）
################################################################################

check_controller_dao_access() {
    log_info "检查1: Controller层直接访问DAO..."
    
    local violations=0
    local controller_files=$(find "$CHECK_PATH" -name "*Controller.java" -type f 2>/dev/null || true)
    
    if [ -z "$controller_files" ]; then
        log_warning "未找到Controller文件"
        return
    fi
    
    while IFS= read -r file; do
        if grep -q "@Resource.*Dao\|private.*Dao" "$file" 2>/dev/null; then
            log_error "Controller直接访问DAO: $file"
            grep -n "@Resource.*Dao\|private.*Dao" "$file" | head -5 >> "$TEMP_REPORT" || true
            ((violations++))
        fi
        ((TOTAL_FILES_CHECKED++))
    done <<< "$controller_files"
    
    if [ $violations -eq 0 ]; then
        log_success "Controller层无DAO访问违规"
    else
        log_error "发现 $violations 处Controller层DAO访问违规"
    fi
}

################################################################################
# 检查2: Service层直接访问DAO（允许，但建议通过Manager）
################################################################################

check_service_dao_access() {
    log_info "检查2: Service层直接访问DAO（建议通过Manager）..."
    
    local warnings=0
    local service_files=$(find "$CHECK_PATH" -name "*ServiceImpl.java" -type f 2>/dev/null || true)
    
    if [ -z "$service_files" ]; then
        log_warning "未找到Service实现文件"
        return
    fi
    
    while IFS= read -r file; do
        # 排除继承ServiceImpl的情况（这是MyBatis-Plus标准用法）
        if grep -q "extends ServiceImpl" "$file" 2>/dev/null; then
            continue
        fi
        
        if grep -q "@Resource.*Dao\|private.*Dao" "$file" 2>/dev/null; then
            log_warning "Service直接访问DAO（建议通过Manager）: $file"
            ((warnings++))
        fi
        ((TOTAL_FILES_CHECKED++))
    done <<< "$service_files"
    
    if [ $warnings -eq 0 ]; then
        log_success "Service层DAO访问符合最佳实践"
    else
        log_warning "发现 $warnings 处Service层直接访问DAO（建议优化）"
    fi
}

################################################################################
# 检查3: Engine层直接访问DAO（禁止）
################################################################################

check_engine_dao_access() {
    log_info "检查3: Engine层直接访问DAO..."
    
    local violations=0
    local engine_files=$(find "$CHECK_PATH" -path "*/engine/*" -name "*.java" -type f 2>/dev/null || true)
    
    if [ -z "$engine_files" ]; then
        log_info "未找到Engine文件"
        return
    fi
    
    while IFS= read -r file; do
        if grep -q "@Resource.*Dao\|private.*Dao" "$file" 2>/dev/null; then
            log_error "Engine直接访问DAO: $file"
            grep -n "@Resource.*Dao\|private.*Dao" "$file" | head -5 >> "$TEMP_REPORT" || true
            ((violations++))
        fi
        ((TOTAL_FILES_CHECKED++))
    done <<< "$engine_files"
    
    if [ $violations -eq 0 ]; then
        log_success "Engine层无DAO访问违规"
    else
        log_error "发现 $violations 处Engine层DAO访问违规"
    fi
}

################################################################################
# 检查4: 依赖注入规范（必须使用@Resource，禁止@Autowired）
################################################################################

check_dependency_injection() {
    log_info "检查4: 依赖注入规范（@Resource vs @Autowired）..."
    
    local violations=0
    local java_files=$(find "$CHECK_PATH" -name "*.java" -type f 2>/dev/null || true)
    
    if [ -z "$java_files" ]; then
        log_warning "未找到Java文件"
        return
    fi
    
    while IFS= read -r file; do
        if grep -q "@Autowired" "$file" 2>/dev/null; then
            log_error "使用@Autowired（应使用@Resource）: $file"
            grep -n "@Autowired" "$file" | head -3 >> "$TEMP_REPORT" || true
            ((violations++))
        fi
        ((TOTAL_FILES_CHECKED++))
    done <<< "$java_files"
    
    if [ $violations -eq 0 ]; then
        log_success "依赖注入规范符合要求（全部使用@Resource）"
    else
        log_error "发现 $violations 处使用@Autowired（应改为@Resource）"
    fi
}

################################################################################
# 检查5: 命名规范
################################################################################

check_naming_convention() {
    log_info "检查5: 命名规范..."
    
    local violations=0
    
    # 检查Controller命名
    local controllers=$(find "$CHECK_PATH" -name "*Controller.java" -type f 2>/dev/null || true)
    while IFS= read -r file; do
        local basename=$(basename "$file" .java)
        if [[ ! "$basename" =~ Controller$ ]]; then
            log_error "Controller命名不规范: $file (应为*Controller.java)"
            ((violations++))
        fi
    done <<< "$controllers"
    
    # 检查Service命名
    local services=$(find "$CHECK_PATH" -name "*Service*.java" -type f 2>/dev/null || true)
    while IFS= read -r file; do
        local basename=$(basename "$file" .java)
        if [[ ! "$basename" =~ Service$ ]] && [[ ! "$basename" =~ ServiceImpl$ ]]; then
            log_warning "Service命名可能不规范: $file"
        fi
    done <<< "$services"
    
    # 检查Manager命名
    local managers=$(find "$CHECK_PATH" -name "*Manager.java" -type f 2>/dev/null || true)
    while IFS= read -r file; do
        local basename=$(basename "$file" .java)
        if [[ ! "$basename" =~ Manager$ ]]; then
            log_warning "Manager命名可能不规范: $file"
        fi
    done <<< "$managers"
    
    # 检查DAO命名
    local daos=$(find "$CHECK_PATH" -name "*Dao.java" -type f 2>/dev/null || true)
    while IFS= read -r file; do
        local basename=$(basename "$file" .java)
        if [[ ! "$basename" =~ Dao$ ]]; then
            log_warning "DAO命名可能不规范: $file"
        fi
    done <<< "$managers"
    
    if [ $violations -eq 0 ]; then
        log_success "命名规范检查通过"
    else
        log_error "发现 $violations 处命名规范违规"
    fi
}

################################################################################
# 检查6: 冗余文件（.backup, .bak等）
################################################################################

check_redundant_files() {
    log_info "检查6: 冗余文件（.backup, .bak等）..."
    
    local redundant_files=$(find "$CHECK_PATH" -type f \( -name "*.backup" -o -name "*.bak" -o -name "*.old" -o -name "*.tmp" \) 2>/dev/null || true)
    
    if [ -z "$redundant_files" ]; then
        log_success "未发现冗余文件"
    else
        local count=$(echo "$redundant_files" | wc -l)
        log_warning "发现 $count 个冗余文件:"
        echo "$redundant_files" | head -10 | while IFS= read -r file; do
            log_warning "  - $file"
        done
        if [ $count -gt 10 ]; then
            log_warning "  ... 还有 $((count - 10)) 个文件"
        fi
    fi
}

################################################################################
# 检查7: 重复类定义
################################################################################

check_duplicate_classes() {
    log_info "检查7: 重复类定义..."
    
    local class_names=$(find "$CHECK_PATH" -name "*.java" -type f -exec basename {} \; 2>/dev/null | sort | uniq -d || true)
    
    if [ -z "$class_names" ]; then
        log_success "未发现重复类定义"
    else
        local count=$(echo "$class_names" | wc -l)
        log_warning "发现 $count 个可能的重复类定义:"
        echo "$class_names" | while IFS= read -r class_name; do
            log_warning "  - $class_name"
            find "$CHECK_PATH" -name "$class_name" -type f 2>/dev/null | while IFS= read -r file; do
                log_warning "    -> $file"
            done
        done
    fi
}

################################################################################
# 检查8: 编码问题（BOM字符）
################################################################################

check_encoding_issues() {
    log_info "检查8: 编码问题（BOM字符）..."
    
    local violations=0
    local java_files=$(find "$CHECK_PATH" -name "*.java" -type f 2>/dev/null || true)
    
    if [ -z "$java_files" ]; then
        log_warning "未找到Java文件"
        return
    fi
    
    while IFS= read -r file; do
        # 检查文件开头是否有BOM字符
        if [ -f "$file" ]; then
            local first_bytes=$(head -c 3 "$file" 2>/dev/null | od -An -tx1 | tr -d ' \n' || echo "")
            if [ "$first_bytes" = "efbbbf" ]; then
                log_error "文件包含BOM字符: $file"
                ((violations++))
            fi
        fi
    done <<< "$java_files"
    
    if [ $violations -eq 0 ]; then
        log_success "未发现编码问题"
    else
        log_error "发现 $violations 个文件包含BOM字符"
    fi
}

################################################################################
# 生成报告
################################################################################

generate_report() {
    log_info "生成架构符合性检查报告..."
    
    cat > "$REPORT_FILE" << EOF
# IOE-DREAM 架构符合性检查报告

> **检查时间**: $(date '+%Y-%m-%d %H:%M:%S')  
> **检查路径**: $CHECK_PATH  
> **检查状态**: $([ $TOTAL_VIOLATIONS -eq 0 ] && echo "✅ 通过" || echo "❌ 发现问题")

---

## 📊 检查结果汇总

### 总体统计
- **检查文件数**: $TOTAL_FILES_CHECKED
- **违规数量**: $TOTAL_VIOLATIONS
- **警告数量**: $TOTAL_WARNINGS
- **符合性**: $([ $TOTAL_VIOLATIONS -eq 0 ] && echo "100% ✅" || echo "$((100 - TOTAL_VIOLATIONS * 10))% ⚠️")

---

## 📋 详细检查结果

$(cat "$TEMP_REPORT")

---

## ✅ 检查项清单

- [$( [ $TOTAL_VIOLATIONS -eq 0 ] && echo 'x' || echo ' ' )] Controller层无DAO访问违规
- [$( [ $TOTAL_WARNINGS -eq 0 ] && echo 'x' || echo ' ' )] Service层DAO访问符合最佳实践
- [$( [ $TOTAL_VIOLATIONS -eq 0 ] && echo 'x' || echo ' ' )] Engine层无DAO访问违规
- [$( [ $TOTAL_VIOLATIONS -eq 0 ] && echo 'x' || echo ' ' )] 依赖注入规范符合要求
- [$( [ $TOTAL_VIOLATIONS -eq 0 ] && echo 'x' || echo ' ' )] 命名规范符合要求
- [$( [ $TOTAL_WARNINGS -eq 0 ] && echo 'x' || echo ' ' )] 无冗余文件
- [$( [ $TOTAL_WARNINGS -eq 0 ] && echo 'x' || echo ' ' )] 无重复类定义
- [$( [ $TOTAL_VIOLATIONS -eq 0 ] && echo 'x' || echo ' ' )] 无编码问题

---

## 🎯 修复建议

$(if [ $TOTAL_VIOLATIONS -gt 0 ] || [ $TOTAL_WARNINGS -gt 0 ]; then
    echo "### 需要修复的问题"
    echo ""
    echo "1. **架构违规**: 修复Controller/Engine层直接访问DAO的问题"
    echo "2. **依赖注入**: 将@Autowired改为@Resource"
    echo "3. **冗余文件**: 清理备份文件"
    echo "4. **重复代码**: 统一重复类定义"
    echo "5. **编码问题**: 修复BOM字符问题"
else
    echo "✅ 所有检查项均通过，无需修复"
fi)

---

**报告生成时间**: $(date '+%Y-%m-%d %H:%M:%S')  
**检查脚本**: scripts/architecture-compliance-check.sh

EOF

    log_success "报告已生成: $REPORT_FILE"
}

################################################################################
# 主函数
################################################################################

main() {
    echo "=========================================="
    echo "  IOE-DREAM 架构符合性检查"
    echo "=========================================="
    echo ""
    echo "检查路径: $CHECK_PATH"
    echo "报告文件: $REPORT_FILE"
    echo ""
    
    # 执行各项检查
    check_controller_dao_access
    echo ""
    
    check_service_dao_access
    echo ""
    
    check_engine_dao_access
    echo ""
    
    check_dependency_injection
    echo ""
    
    check_naming_convention
    echo ""
    
    check_redundant_files
    echo ""
    
    check_duplicate_classes
    echo ""
    
    check_encoding_issues
    echo ""
    
    # 生成报告
    generate_report
    
    # 输出总结
    echo "=========================================="
    echo "  检查完成"
    echo "=========================================="
    echo ""
    echo "检查文件数: $TOTAL_FILES_CHECKED"
    echo "违规数量: $TOTAL_VIOLATIONS"
    echo "警告数量: $TOTAL_WARNINGS"
    echo ""
    
    if [ $TOTAL_VIOLATIONS -eq 0 ] && [ $TOTAL_WARNINGS -eq 0 ]; then
        echo -e "${GREEN}✅ 架构符合性检查通过！${NC}"
        exit 0
    elif [ $TOTAL_VIOLATIONS -eq 0 ]; then
        echo -e "${YELLOW}⚠️  发现 $TOTAL_WARNINGS 个警告，建议优化${NC}"
        exit 0
    else
        echo -e "${RED}❌ 发现 $TOTAL_VIOLATIONS 个违规，需要修复${NC}"
        exit 1
    fi
}

# 执行主函数
main

