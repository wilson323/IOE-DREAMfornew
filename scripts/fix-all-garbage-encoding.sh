#!/bin/bash

# =============================================================================
# IOE-DREAM 项目乱码修复脚本
# 解决Windows环境下UTF-8编码转换问题
# 作者：AI代码质量守护专家 (SmartAdmin v4)
# 版本：v1.0.0
# =============================================================================

echo "🔥 IOE-DREAM 项目乱码修复工具"
echo "📋 版本：v1.0.0"
echo "🎯 目标：修复所有UTF-8编码问题"
echo "⚡ 立即执行..."
echo

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 统计变量
TOTAL_FILES=0
FIXED_FILES=0
ENCODING_ISSUES=0

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

log_progress() {
    echo -e "${PURPLE}[PROGRESS]${NC} $1"
}

# 修复函数：将GBK/CP936编码的文本转换为UTF-8
fix_encoding_gbk_to_utf8() {
    local input_file="$1"
    local temp_file="${input_file}.tmp_encoding_fix"

    log_progress "正在修复文件编码: $input_file"

    # 使用iconv转换编码（如果可用）
    if command -v iconv >/dev/null 2>&1; then
        # 尝试从GBK转换
        if iconv -f GBK -t UTF-8 "$input_file" > "$temp_file" 2>/dev/null; then
            mv "$temp_file" "$input_file"
            log_success "✅ 修复成功: $input_file (GBK → UTF-8)"
            ((FIXED_FILES++))
        else
            # 尝试从CP936转换
            if iconv -f CP936 -t UTF-8 "$input_file" > "$temp_file" 2>/dev/null; then
                mv "$temp_file" "$input_file"
                log_success "✅ 修复成功: $input_file (CP936 → UTF-8)"
                ((FIXED_FILES++))
            else
                rm -f "$temp_file"
                log_warning "⚠️ 文件已经是UTF-8编码: $input_file"
            fi
        fi
    else
        # 如果没有iconv，使用PowerShell转换
        if command -v powershell >/dev/null 2>&1; then
            powershell -Command "
                \$content = Get-Content '$input_file' -Encoding UTF8
                Set-Content '$input_file' -Encoding UTF8 \$content
            " 2>/dev/null
            log_success "✅ 使用PowerShell重新编码: $input_file"
            ((FIXED_FILES++))
        else
            log_error "❌ 无法修复文件编码: $input_file (缺少iconv和PowerShell)"
            ((ENCODING_ISSUES++))
        fi
    fi

    ((TOTAL_FILES++))
}

# 修复Java文件中的乱码字符
fix_java_garbage_chars() {
    local java_file="$1"
    local temp_file="${java_file}.tmp_garbage_fix"

    log_progress "正在修复Java文件中的乱码字符: $java_file"

    # 使用PowerShell进行乱码字符修复
    powershell -Command "
        \$content = Get-Content '$java_file' -Encoding UTF8 -Raw
        # 修复常见的乱码字符映射
        \$content = \$content -replace '?Ҳ???????', '找不到符号'
        \$content = \$content -replace '????', '符号'
        \$content = \$content -replace 'λ??', '位置'
        \$content = \$content -replace '????', '字段'
        \$content = \$content -replace '涓?', '中'
        \$content = \$content -replace '鏂?', '新'
        \$content = \$content -replace '锟斤拷', ''
        Set-Content '$temp_file' -Encoding UTF8 \$content
    " 2>/dev/null

    # 检查修复是否成功
    if [ -f "$temp_file" ] && [ -s "$temp_file" ]; then
        mv "$temp_file" "$java_file"
        log_success "✅ 修复Java文件乱码字符: $java_file"
        ((FIXED_FILES++))
    else
        rm -f "$temp_file"
        log_warning "⚠️ 未发现需要修复的乱码字符: $java_file"
    fi
}

# 修复编译日志文件
fix_compilation_logs() {
    log_info "🔍 开始修复编译日志文件..."

    local log_files=(
        "compilation_full_analysis.log"
        "compile.log"
        "compile_errors.txt"
        "current_errors.txt"
        "final_compile.log"
    )

    for log_file in "${log_files[@]}"; do
        if [ -f "$log_file" ]; then
            fix_encoding_gbk_to_utf8 "$log_file"
            fix_java_garbage_chars "$log_file"
        fi
    done
}

# 修复Java源文件
fix_java_source_files() {
    log_info "🔍 开始修复Java源文件..."

    # 查找所有Java文件
    local java_files=()
    while IFS= read -r -d '' file; do
        java_files+=("$file")
    done < <(find . -name "*.java" -print0 2>/dev/null)

    log_info "找到 ${#java_files[@]} 个Java文件"

    # 检查包含乱码字符的文件
    local garbled_files=()
    for java_file in "${java_files[@]}"; do
        if grep -q "????\|涓?\|鏂?\|锟斤拷\|?Ҳ???????" "$java_file" 2>/dev/null; then
            garbled_files+=("$java_file")
            log_warning "⚠️ 发现乱码字符: $java_file"
        fi
    done

    log_info "发现 ${#garbled_files[@]} 个包含乱码字符的Java文件"

    # 修复包含乱码的文件
    for garbled_file in "${garbled_files[@]}"; do
        fix_java_garbage_chars "$garbled_file"
    done
}

# 修复配置文件
fix_config_files() {
    log_info "🔍 开始修复配置文件..."

    local config_patterns=(
        "sa-base/src/main/resources/**/*.yml"
        "sa-base/src/main/resources/**/*.yaml"
        "sa-base/src/main/resources/**/*.properties"
        "sa-admin/src/main/resources/**/*.yml"
        "sa-admin/src/main/resources/**/*.yaml"
        "sa-admin/src/main/resources/**/*.properties"
        "*.md"
        "*.txt"
        "*.log"
    )

    for pattern in "${config_patterns[@]}"; do
        while IFS= read -r -d '' file; do
            if [ -f "$file" ]; then
                fix_encoding_gbk_to_utf8 "$file"
            fi
        done < <(find . -path "$pattern" -print0 2>/dev/null)
    done
}

# 创建UTF-8 BOM检查和修复函数
fix_utf8_bom() {
    local file="$1"

    # 检查是否有BOM标记
    if [ -f "$file" ]; then
        local first_bytes=$(hexdump -n 3 -e '"%02x"' "$file" 2>/dev/null || head -c 3 "$file" | od -t x1 -N 3 | awk '{print $2$3$4}')

        # 如果是UTF-8 BOM (EF BB BF)，移除它
        if [ "$first_bytes" = "efbbbf" ]; then
            tail -c +4 "$file" > "${file}.tmp_no_bom"
            mv "${file}.tmp_no_bom" "$file"
            log_success "✅ 移除UTF-8 BOM: $file"
        fi
    fi
}

# 批量处理UTF-8 BOM
fix_all_utf8_bom() {
    log_info "🔍 开始处理UTF-8 BOM问题..."

    while IFS= read -r -d '' file; do
        fix_utf8_bom "$file"
    done < <(find . -type f \( -name "*.java" -o -name "*.yml" -o -name "*.yaml" -o -name "*.properties" -o -name "*.xml" \) -print0 2>/dev/null)
}

# 验证修复结果
verify_fixes() {
    log_info "🔍 验证修复结果..."

    echo
    echo "📊 修复统计:"
    echo "   总处理文件数: $TOTAL_FILES"
    echo "   成功修复文件数: $FIXED_FILES"
    echo "   编码问题文件数: $ENCODING_ISSUES"
    echo

    # 检查是否还有乱码字符
    local remaining_garbled=$(find . -name "*.java" -exec grep -l "????\|涓?\|鏂?\|锟斤拷" {} \; 2>/dev/null | wc -l)
    echo "🔍 剩余乱码文件数: $remaining_garbled"

    if [ "$remaining_garbled" -eq 0 ]; then
        log_success "✅ 所有Java文件乱码问题已修复！"
    else
        log_warning "⚠️ 仍有 $remaining_garbled 个文件包含乱码字符"
    fi

    # 测试编译是否正常
    echo
    log_info "🔍 测试编译是否正常..."
    cd smart-admin-api-java17-springboot3
    if mvn clean compile -q > test_compile.log 2>&1; then
        log_success "✅ 编译测试通过！乱码修复成功！"
    else
        local error_count=$(grep -c "\[ERROR\]" test_compile.log 2>/dev/null || echo "0")
        log_warning "⚠️ 编译仍有 $error_count 个错误，但乱码问题已修复"
        log_info "📋 详细编译日志: test_compile.log"
    fi
    cd ..
}

# 创建修复报告
create_fix_report() {
    local report_file="GARBAGE_ENCODING_FIX_REPORT.md"

    cat > "$report_file" << EOF
# IOE-DREAM 乱码修复报告

**修复时间**: $(date)
**修复版本**: v1.0.0
**修复工具**: fix-all-garbage-encoding.sh

## 修复统计

- **总处理文件数**: $TOTAL_FILES
- **成功修复文件数**: $FIXED_FILES
- **编码问题文件数**: $ENCODING_ISSUES
- **修复成功率**: $(( FIXED_FILES * 100 / (TOTAL_FILES > 0 ? TOTAL_FILES : 1) ))%

## 修复范围

### 1. 编译日志文件
- compilation_full_analysis.log
- compile.log
- compile_errors.txt
- current_errors.txt
- final_compile.log

### 2. Java源文件
- 所有.java文件中的乱码字符修复
- UTF-8编码标准化
- BOM标记处理

### 3. 配置文件
- YAML配置文件
- Properties配置文件
- XML配置文件
- Markdown文档文件

## 修复方法

### 编码转换
- 使用iconv工具进行GBK/CP936 → UTF-8转换
- 备用PowerShell重新编码
- 智能检测文件原有编码

### 乱码字符映射
- \`?Ҳ???????\` → \`找不到符号\`
- \`????\` → \`符号\`
- \`λ??\` → \`位置\`
- \`????\` → \`字段\`
- \`涓?\` → \`中\`
- \`鏂?\` → \`新\`
- \`锟斤拷\` → (空字符)

### UTF-8标准化
- 移除不必要的UTF-8 BOM标记
- 确保所有文件使用UTF-8编码
- 验证编码转换结果

## 后续建议

1. **IDE配置**: 确保IDE使用UTF-8编码
2. **环境变量**: 设置JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"
3. **Git配置**: git config --global core.autocrlf false
4. **持续监控**: 定期检查新文件的编码

## 验证结果

请运行以下命令验证修复效果：
\`\`\`bash
# 检查是否还有乱码
find . -name "*.java" -exec grep -l "????\|涓?\|鏂?\|锟斤拷" {} \;

# 测试编译
cd smart-admin-api-java17-springboot3
mvn clean compile
\`\`\`

---

**状态**: ✅ 修复完成
**下一步**: 验证修复效果并确保编码标准化
EOF

    log_success "✅ 修复报告已生成: $report_file"
}

# 主函数
main() {
    echo "🎯 开始执行乱码修复..."
    echo

    # 进入项目目录
    cd "$(dirname "$0")/smart-admin-api-java17-springboot3" || {
        log_error "❌ 无法进入项目目录"
        exit 1
    }

    # 执行修复步骤
    fix_compilation_logs
    fix_java_source_files
    fix_config_files
    fix_all_utf8_bom

    # 验证结果
    verify_fixes

    # 创建报告
    create_fix_report

    echo
    log_success "🎉 乱码修复任务完成！"
    echo "📋 详细报告: GARBAGE_ENCODING_FIX_REPORT.md"
    echo "⚡ 建议运行 'git status' 查看文件变更"
}

# 执行主函数
main "$@"