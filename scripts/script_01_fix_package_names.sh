#!/bin/bash
# =================================================================
# repowiki规范修复脚本 - 第一阶段：包名和基础规范修复
# 目标：修复annoation→annotation、javax→jakarta、@Autowired→@Resource
# 版本：v1.0
# 创建时间：2025-11-18
# =================================================================

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查是否在正确的目录
check_directory() {
    if [ ! -f "pom.xml" ]; then
        log_error "请确保在项目根目录（包含pom.xml的目录）执行此脚本"
        exit 1
    fi

    if [ ! -d "smart-admin-api-java17-springboot3" ]; then
        log_error "未找到smart-admin-api-java17-springboot3目录"
        exit 1
    fi

    log_success "目录检查通过"
}

# 创建备份
create_backup() {
    log_info "创建修复前备份..."

    BACKUP_DIR="backup_$(date +%Y%m%d_%H%M%S)"
    mkdir -p "$BACKUP_DIR"

    # 备份关键文件
    find smart-admin-api-java17-springboot3 -name "*.java" -type f | head -50 | xargs -I {} cp --parents {} "$BACKUP_DIR/"

    # 记录当前状态
    find smart-admin-api-java17-springboot3 -name "*.java" -exec grep -l "annoation\|javax\.\|@Autowired" {} \; > "$BACKUP_DIR/backup_issues.txt"

    log_success "备份创建完成: $BACKUP_DIR"
}

# 显示当前状态
show_current_status() {
    log_info "=== 修复前状态检查 ==="

    # 统计各类问题数量
    javax_count=$(find smart-admin-api-java17-springboot3 -name "*.java" -exec grep -l "javax\." {} \; | wc -l)
    autowired_count=$(find smart-admin-api-java17-springboot3 -name "*.java" -exec grep -l "@Autowired" {} \; | wc -l)
    annoation_count=$(find smart-admin-api-java17-springboot3 -name "*.java" -exec grep -l "annoation" {} \; | wc -l)

    echo "javax包使用数量: $javax_count"
    echo "@Autowired使用数量: $autowired_count"
    echo "包名错误(annoation)数量: $annoation_count"

    # 编译错误检查
    log_info "检查当前编译状态..."
    cd smart-admin-api-java17-springboot3
    error_count=$(mvn clean compile -q 2>&1 | grep -c "ERROR" || echo "0")
    cd ..
    echo "当前编译错误数量: $error_count"
}

# 修复包名错误 annoation → annotation
fix_annoation_packages() {
    log_info "=== 修复包名错误 annoation → annotation ==="

    # 检查目录是否存在
    if [ -d "smart-admin-api-java17-springboot3/sa-base/src/main/java/net/lab1024/sa/base/common/annoation" ]; then
        log_info "发现错误的包目录，进行重命名..."
        mv smart-admin-api-java17-springboot3/sa-base/src/main/java/net/lab1024/sa/base/common/annoation \
           smart-admin-api-java17-springboot3/sa-base/src/main/java/net/lab1024/sa/base/common/annotation
        log_success "包目录重命名完成"
    fi

    # 批量更新import语句
    log_info "批量更新import语句..."
    find smart-admin-api-java17-springboot3 -name "*.java" -type f -exec sed -i 's/net\.lab1024\.sa\.base\.common\.annoation/net.lab1024.sa.base.common.annotation/g' {} \;

    # 验证修复效果
    annoation_after=$(find smart-admin-api-java17-springboot3 -name "*.java" -exec grep -l "annoation" {} \; | wc -l)
    log_success "包名修复完成，剩余annoation错误: $annoation_after"

    if [ $annoation_after -eq 0 ]; then
        log_success "✅ 包名错误修复成功！"
    else
        log_warning "⚠️ 仍有 $annoation_after 个文件存在annoation错误"
    fi
}

# 修复javax包名 jakarta
fix_javax_packages() {
    log_info "=== 修复javax包名 → jakarta ==="

    # 显示需要修复的文件
    log_info "需要修复javax包名的文件："
    find smart-admin-api-java17-springboot3 -name "*.java" -exec grep -l "javax\." {} \; | head -5

    # 批量替换javax为jakarta
    log_info "批量替换javax为jakarta..."
    find smart-admin-api-java17-springboot3 -name "*.java" -exec sed -i 's/javax\./jakarta\./g' {} \;

    # 验证修复效果
    javax_after=$(find smart-admin-api-java17-springboot3 -name "*.java" -exec grep -l "javax\." {} \; | wc -l)
    log_success "javax包名修复完成，剩余javax使用: $javax_after"

    if [ $javax_after -eq 0 ]; then
        log_success "✅ javax包名修复成功！"
    else
        log_warning "⚠️ 仍有 $javax_after 个文件使用javax包"
    fi
}

# 修复依赖注入 @Autowired → @Resource
fix_dependency_injection() {
    log_info "=== 修复依赖注入 @Autowired → @Resource ==="

    # 显示需要修复的文件
    log_info "需要修复@Autowired的文件："
    find smart-admin-api-java17-springboot3 -name "*.java" -exec grep -l "@Autowired" {} \; | head -5

    # 批量替换@Autowired为@Resource
    log_info "批量替换@Autowired为@Resource..."
    find smart-admin-api-java17-springboot3 -name "*.java" -exec sed -i 's/@Autowired/@Resource/g' {} \;

    # 验证修复效果
    autowired_after=$(find smart-admin-api-java17-springboot3 -name "*.java" -exec grep -l "@Autowired" {} \; | wc -l)
    log_success "依赖注入修复完成，剩余@Autowired使用: $autowired_after"

    if [ $autowired_after -eq 0 ]; then
        log_success "✅ 依赖注入修复成功！"
    else
        log_warning "⚠️ 仍有 $autowired_after 个文件使用@Autowired"
    fi
}

# 验证修复结果
verify_fixes() {
    log_info "=== 验证修复结果 ==="

    # 重新统计各类问题数量
    javax_count=$(find smart-admin-api-java17-springboot3 -name "*.java" -exec grep -l "javax\." {} \; | wc -l)
    autowired_count=$(find smart-admin-api-java17-springboot3 -name "*.java" -exec grep -l "@Autowired" {} \; | wc -l)
    annoation_count=$(find smart-admin-api-java17-springboot3 -name "*.java" -exec grep -l "annoation" {} \; | wc -l)

    echo "=== 修复后状态报告 ==="
    echo "javax包使用数量: $javax_count (目标: 0)"
    echo "@Autowired使用数量: $autowired_count (目标: 0)"
    echo "包名错误(annoation)数量: $annoation_count (目标: 0)"

    # 编译测试
    log_info "执行编译验证..."
    cd smart-admin-api-java17-springboot3

    # 先清理
    mvn clean -q > /dev/null 2>&1 || true

    # 编译检查
    compile_output=$(mvn compile -q 2>&1 || echo "COMPILE_FAILED")
    error_count=$(echo "$compile_output" | grep -c "ERROR" || echo "0")

    cd ..

    echo "编译错误数量: $error_count (目标: 0)"

    # 生成修复报告
    if [ $javax_count -eq 0 ] && [ $autowired_count -eq 0 ] && [ $annoation_count -eq 0 ]; then
        log_success "🎉 repowiki基础规范修复完成！所有规范问题已解决！"
        return 0
    else
        log_warning "⚠️ 仍有部分规范问题需要手动处理"
        return 1
    fi
}

# 生成修复报告
generate_report() {
    local exit_code=$1
    local report_file="repowiki_fix_report_$(date +%Y%m%d_%H%M%S).md"

    cat > "$report_file" << EOF
# repowiki规范修复报告

**修复时间**: $(date)
**脚本版本**: v1.0
**修复状态**: $([ $exit_code -eq 0 ] && echo "✅ 成功" || echo "⚠️ 部分成功")

## 修复内容

### 1. 包名错误修复 (annoation → annotation)
- **修复前**: $(grep -c "annoation" "$BACKUP_DIR/backup_issues.txt" 2>/dev/null || echo "0") 个文件
- **修复后**: $(find smart-admin-api-java17-springboot3 -name "*.java" -exec grep -l "annoation" {} \; | wc -l) 个文件

### 2. Jakarta包名修复 (javax → jakarta)
- **修复前**: $(grep -c "javax\." "$BACKUP_DIR/backup_issues.txt" 2>/dev/null || echo "0") 个文件
- **修复后**: $(find smart-admin-api-java17-springboot3 -name "*.java" -exec grep -l "javax\." {} \; | wc -l) 个文件

### 3. 依赖注入修复 (@Autowired → @Resource)
- **修复前**: $(grep -c "@Autowired" "$BACKUP_DIR/backup_issues.txt" 2>/dev/null || echo "0") 个文件
- **修复后**: $(find smart-admin-api-java17-springboot3 -name "*.java" -exec grep -l "@Autowired" {} \; | wc -l) 个文件

## 编译状态

- **编译错误数量**: $(cd smart-admin-api-java17-springboot3 && mvn compile -q 2>&1 | grep -c "ERROR" || echo "0") 个

## 备份信息

- **备份目录**: $BACKUP_DIR
- **备份文件**: $(ls -1 "$BACKUP_DIR" | wc -l) 个

## 下一步行动

1. 检查编译结果，如有错误请手动修复
2. 运行下一阶段修复脚本
3. 执行完整的功能测试

---
**报告生成时间**: $(date)
**基于**: repowiki开发规范体系 v1.1
EOF

    log_success "修复报告已生成: $report_file"
}

# 主函数
main() {
    echo "========================================"
    echo "  repowiki规范修复脚本 - 第一阶段"
    echo "  版本: v1.0"
    echo "  目标: 包名和基础规范修复"
    echo "========================================"
    echo

    # 执行修复步骤
    check_directory
    show_current_status
    create_backup

    echo
    log_info "开始执行修复..."
    fix_annoation_packages
    fix_javax_packages
    fix_dependency_injection

    echo
    log_info "验证修复结果..."
    if verify_fixes; then
        generate_report 0
        log_success "🎉 第一阶段修复完成！可以继续执行下一阶段。"
    else
        generate_report 1
        log_warning "⚠️ 第一阶段部分完成，请检查剩余问题后继续。"
    fi

    echo
    echo "========================================"
    echo "  修复完成！"
    echo "  下一步: 运行 script_02_create_missing_managers.sh"
    echo "========================================"
}

# 执行主函数
main "$@"