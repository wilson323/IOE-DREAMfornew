#!/bin/bash

################################################################################
# IOE-DREAM 编译错误检查脚本
# 
# 功能：全面检查项目编译错误，生成详细报告
# 规范基准：repowiki规范体系
# 
# 检查项：
# 1. Maven编译错误统计
# 2. 包名规范检查（javax vs jakarta）
# 3. 依赖注入规范检查（@Autowired vs @Resource）
# 4. 编码问题检查（UTF-8、BOM、乱码）
# 5. 类型定义检查
# 6. 方法签名检查
#
# 使用方法：
#   ./scripts/compile-error-check.sh
#   ./scripts/compile-error-check.sh [模块路径]
#
# 示例：
#   ./scripts/compile-error-check.sh
#   ./scripts/compile-error-check.sh sa-admin
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
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 项目根目录
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

# 检查模块（如果提供）
CHECK_MODULE="${1:-smart-admin-api-java17-springboot3}"
MODULE_PATH="$CHECK_MODULE"

# 统计变量
TOTAL_ERRORS=0
TOTAL_WARNINGS=0
JAVAX_COUNT=0
AUTOWIRED_COUNT=0
ENCODING_ISSUES=0

# 报告文件
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
REPORT_FILE="docs/COMPILE_ERROR_REPORT_${TIMESTAMP}.md"
TEMP_COMPILE_LOG="/tmp/compile_$$.log"
TEMP_ERROR_LOG="/tmp/errors_$$.log"

################################################################################
# 工具函数
################################################################################

log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
    ((TOTAL_WARNINGS++))
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
    ((TOTAL_ERRORS++))
}

################################################################################
# 检查1: Maven编译错误统计
################################################################################

check_maven_compilation() {
    log_info "检查1: Maven编译错误统计..."
    
    if [ ! -d "$MODULE_PATH" ]; then
        log_error "模块路径不存在: $MODULE_PATH"
        return 1
    fi
    
    cd "$MODULE_PATH"
    
    log_info "执行Maven编译检查..."
    if mvn clean compile -DskipTests > "$TEMP_COMPILE_LOG" 2>&1; then
        log_success "编译成功，无错误"
        cd "$PROJECT_ROOT"
        return 0
    else
        # 提取错误信息
        grep -E "\[ERROR\]|ERROR" "$TEMP_COMPILE_LOG" > "$TEMP_ERROR_LOG" || true
        
        local error_count=$(wc -l < "$TEMP_ERROR_LOG" 2>/dev/null || echo "0")
        log_error "编译失败，发现 $error_count 个错误"
        
        # 统计错误类型
        local package_errors=$(grep -c "package.*does not exist\|cannot find symbol" "$TEMP_ERROR_LOG" 2>/dev/null || echo "0")
        local type_errors=$(grep -c "cannot find symbol\|incompatible types" "$TEMP_ERROR_LOG" 2>/dev/null || echo "0")
        local method_errors=$(grep -c "method.*not found\|cannot resolve method" "$TEMP_ERROR_LOG" 2>/dev/null || echo "0")
        
        log_warning "   - 包导入错误: $package_errors"
        log_warning "   - 类型错误: $type_errors"
        log_warning "   - 方法错误: $method_errors"
        
        cd "$PROJECT_ROOT"
        return 1
    fi
}

################################################################################
# 检查2: 包名规范检查（javax vs jakarta）
################################################################################

check_package_naming() {
    log_info "检查2: 包名规范检查（javax vs jakarta）..."
    
    local java_files=$(find "$MODULE_PATH" -name "*.java" -type f 2>/dev/null | head -1000)
    if [ -z "$java_files" ]; then
        log_warning "未找到Java文件"
        return
    fi
    
    # 检查javax包使用（EE命名空间，必须迁移到jakarta）
    local javax_files=$(echo "$java_files" | xargs grep -l "import javax\.\(servlet\|validation\|persistence\|ejb\|jms\|xml\.bind\|jws\|jta\|annotation\)" 2>/dev/null | wc -l || echo "0")
    
    if [ "$javax_files" -gt 0 ]; then
        log_error "发现 $javax_files 个文件使用javax包（必须迁移到jakarta）"
        JAVAX_COUNT=$javax_files
        ((TOTAL_ERRORS+=javax_files))
        
        # 列出前5个文件
        echo "$java_files" | xargs grep -l "import javax\.\(servlet\|validation\|persistence\|ejb\|jms\|xml\.bind\|jws\|jta\|annotation\)" 2>/dev/null | head -5 | while read -r file; do
            log_warning "   - $file"
        done
    else
        log_success "包名规范检查通过（无javax包使用）"
    fi
}

################################################################################
# 检查3: 依赖注入规范检查（@Autowired vs @Resource）
################################################################################

check_dependency_injection() {
    log_info "检查3: 依赖注入规范检查（@Autowired vs @Resource）..."
    
    local java_files=$(find "$MODULE_PATH" -name "*.java" -type f 2>/dev/null | head -1000)
    if [ -z "$java_files" ]; then
        log_warning "未找到Java文件"
        return
    fi
    
    # 检查@Autowired使用
    local autowired_files=$(echo "$java_files" | xargs grep -l "@Autowired" 2>/dev/null | wc -l || echo "0")
    
    if [ "$autowired_files" -gt 0 ]; then
        log_error "发现 $autowired_files 个文件使用@Autowired（必须使用@Resource）"
        AUTOWIRED_COUNT=$autowired_files
        ((TOTAL_ERRORS+=autowired_files))
        
        # 列出前5个文件
        echo "$java_files" | xargs grep -l "@Autowired" 2>/dev/null | head -5 | while read -r file; do
            log_warning "   - $file"
        done
    else
        log_success "依赖注入规范检查通过（无@Autowired使用）"
    fi
}

################################################################################
# 检查4: 编码问题检查
################################################################################

check_encoding_issues() {
    log_info "检查4: 编码问题检查（UTF-8、BOM、乱码）..."
    
    local java_files=$(find "$MODULE_PATH" -name "*.java" -type f 2>/dev/null | head -500)
    if [ -z "$java_files" ]; then
        log_warning "未找到Java文件"
        return
    fi
    
    local bom_count=0
    local garbled_count=0
    
    while IFS= read -r file; do
        # 检查BOM标记
        if head -c 3 "$file" | od -An -tx1 | grep -q "ef bb bf"; then
            ((bom_count++))
            log_warning "发现BOM标记: $file"
        fi
        
        # 检查乱码字符
        if grep -q "????\|涓\|鏂\|锟斤拷" "$file" 2>/dev/null; then
            ((garbled_count++))
            log_warning "发现乱码字符: $file"
        fi
    done <<< "$java_files"
    
    ENCODING_ISSUES=$((bom_count + garbled_count))
    
    if [ "$ENCODING_ISSUES" -gt 0 ]; then
        log_error "发现 $ENCODING_ISSUES 个编码问题（BOM: $bom_count, 乱码: $garbled_count）"
        ((TOTAL_ERRORS+=ENCODING_ISSUES))
    else
        log_success "编码问题检查通过"
    fi
}

################################################################################
# 生成报告
################################################################################

generate_report() {
    log_info "生成检查报告..."
    
    cat > "$REPORT_FILE" << EOF
# 编译错误检查报告

> **生成时间**: $(date '+%Y-%m-%d %H:%M:%S')  
> **检查模块**: $MODULE_PATH  
> **检查脚本**: scripts/compile-error-check.sh

---

## 📊 检查结果摘要

| 检查项 | 状态 | 数量 |
|--------|------|------|
| Maven编译 | $( [ $TOTAL_ERRORS -eq 0 ] && echo '✅ 通过' || echo '❌ 失败' ) | $TOTAL_ERRORS 个错误 |
| 包名规范 | $( [ $JAVAX_COUNT -eq 0 ] && echo '✅ 通过' || echo '❌ 违规' ) | $JAVAX_COUNT 个文件 |
| 依赖注入 | $( [ $AUTOWIRED_COUNT -eq 0 ] && echo '✅ 通过' || echo '❌ 违规' ) | $AUTOWIRED_COUNT 个文件 |
| 编码问题 | $( [ $ENCODING_ISSUES -eq 0 ] && echo '✅ 通过' || echo '❌ 违规' ) | $ENCODING_ISSUES 个问题 |

---

## 🔍 详细检查结果

### 1. Maven编译错误

$(if [ -f "$TEMP_ERROR_LOG" ] && [ -s "$TEMP_ERROR_LOG" ]; then
    echo "发现以下编译错误："
    echo ""
    head -20 "$TEMP_ERROR_LOG" | sed 's/^/    /'
    echo ""
    echo "完整错误日志请查看: $TEMP_COMPILE_LOG"
else
    echo "✅ 编译成功，无错误"
fi)

### 2. 包名规范检查

$(if [ $JAVAX_COUNT -gt 0 ]; then
    echo "❌ 发现 $JAVAX_COUNT 个文件使用javax包（必须迁移到jakarta）"
    echo ""
    echo "违规文件列表："
    find "$MODULE_PATH" -name "*.java" -type f 2>/dev/null | xargs grep -l "import javax\.\(servlet\|validation\|persistence\|ejb\|jms\|xml\.bind\|jws\|jta\|annotation\)" 2>/dev/null | head -10 | sed 's/^/    - /'
else
    echo "✅ 包名规范检查通过"
fi)

### 3. 依赖注入规范检查

$(if [ $AUTOWIRED_COUNT -gt 0 ]; then
    echo "❌ 发现 $AUTOWIRED_COUNT 个文件使用@Autowired（必须使用@Resource）"
    echo ""
    echo "违规文件列表："
    find "$MODULE_PATH" -name "*.java" -type f 2>/dev/null | xargs grep -l "@Autowired" 2>/dev/null | head -10 | sed 's/^/    - /'
else
    echo "✅ 依赖注入规范检查通过"
fi)

### 4. 编码问题检查

$(if [ $ENCODING_ISSUES -gt 0 ]; then
    echo "❌ 发现 $ENCODING_ISSUES 个编码问题"
    echo ""
    echo "建议执行以下命令修复："
    echo "    ./scripts/ultimate-encoding-fix-fixed.sh"
    echo "    ./scripts/zero-garbage-encoding-fix.sh"
else
    echo "✅ 编码问题检查通过"
fi)

---

## 🎯 修复建议

$(if [ $TOTAL_ERRORS -gt 0 ] || [ $TOTAL_WARNINGS -gt 0 ]; then
    echo "### 需要修复的问题"
    echo ""
    [ $JAVAX_COUNT -gt 0 ] && echo "1. **包名规范**: 将javax包迁移到jakarta包"
    [ $AUTOWIRED_COUNT -gt 0 ] && echo "2. **依赖注入**: 将@Autowired改为@Resource"
    [ $ENCODING_ISSUES -gt 0 ] && echo "3. **编码问题**: 执行编码修复脚本"
    [ -f "$TEMP_ERROR_LOG" ] && [ -s "$TEMP_ERROR_LOG" ] && echo "4. **编译错误**: 修复Maven编译错误"
    echo ""
    echo "### 修复命令"
    echo ""
    echo "\`\`\`bash"
    [ $JAVAX_COUNT -gt 0 ] && echo "# 修复javax包名"
    [ $JAVAX_COUNT -gt 0 ] && echo "find $MODULE_PATH -name \"*.java\" -exec sed -i 's/javax\\.servlet/jakarta.servlet/g' {} \\;"
    [ $AUTOWIRED_COUNT -gt 0 ] && echo "# 修复@Autowired"
    [ $AUTOWIRED_COUNT -gt 0 ] && echo "find $MODULE_PATH -name \"*.java\" -exec sed -i 's/@Autowired/@Resource/g' {} \\;"
    [ $ENCODING_ISSUES -gt 0 ] && echo "# 修复编码问题"
    [ $ENCODING_ISSUES -gt 0 ] && echo "./scripts/ultimate-encoding-fix-fixed.sh"
    echo "\`\`\`"
else
    echo "✅ 所有检查项均通过，无需修复"
fi)

---

**报告生成时间**: $(date '+%Y-%m-%d %H:%M:%S')  
**检查脚本**: scripts/compile-error-check.sh

EOF

    log_success "报告已生成: $REPORT_FILE"
}

################################################################################
# 主函数
################################################################################

main() {
    echo "=========================================="
    echo "  IOE-DREAM 编译错误检查"
    echo "=========================================="
    echo ""
    echo "检查模块: $MODULE_PATH"
    echo "报告文件: $REPORT_FILE"
    echo ""
    
    # 执行各项检查
    check_maven_compilation
    echo ""
    
    check_package_naming
    echo ""
    
    check_dependency_injection
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
    echo "总错误数: $TOTAL_ERRORS"
    echo "总警告数: $TOTAL_WARNINGS"
    echo ""
    
    if [ $TOTAL_ERRORS -eq 0 ] && [ $TOTAL_WARNINGS -eq 0 ]; then
        echo -e "${GREEN}✅ 编译错误检查通过！${NC}"
        exit 0
    elif [ $TOTAL_ERRORS -eq 0 ]; then
        echo -e "${YELLOW}⚠️  发现 $TOTAL_WARNINGS 个警告，建议优化${NC}"
        exit 0
    else
        echo -e "${RED}❌ 发现 $TOTAL_ERRORS 个错误，需要修复${NC}"
        exit 1
    fi
}

# 执行主函数
main

