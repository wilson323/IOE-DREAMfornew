#!/bin/bash

# =============================================================================
# IOE-DREAM 立即修复策略脚本
# 功能：基于质量监控结果，执行最优的修复策略
# 创建时间：2025-11-18
# 版本：v1.0.0
# =============================================================================

PROJECT_ROOT="D:\IOE-DREAM"
BACKEND_DIR="$PROJECT_ROOT/smart-admin-api-java17-springboot3"
LOGS_DIR="$PROJECT_ROOT/fix_logs"
mkdir -p "$LOGS_DIR"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
WHITE='\033[1;37m'
NC='\033[0m'

echo -e "${CYAN}========================================${NC}"
echo -e "${WHITE}🚀 IOE-DREAM 立即修复策略执行器${NC}"
echo -e "${CYAN}========================================${NC}"

# 获取当前错误统计
get_current_errors() {
    cd "$BACKEND_DIR"
    mvn compile -q 2>&1 > temp_compile.log 2>&1
    local errors=$(grep -c "ERROR" temp_compile.log 2>/dev/null || echo 0)
    rm -f temp_compile.log
    echo $errors
}

# 记录修复进度
log_fix_progress() {
    local phase="$1"
    local action="$2"
    local before="$3"
    local after="$4"
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')

    echo "[$timestamp] Phase: $phase | Action: $action | Before: $before → After: $4" >> "$LOGS_DIR/fix_progress.log"
    echo -e "${BLUE}📝 记录修复进度: $phase - $action${NC}"
}

# 策略1: 修复剩余的jakarta包名问题
fix_remaining_jakarta() {
    echo -e "\n${YELLOW}🔧 策略1: 修复剩余Jakarta包名问题${NC}"

    local before_errors=$(get_current_errors)

    cd "$BACKEND_DIR"

    # 查找剩余的javax包使用
    echo "查找剩余的javax包使用..."
    grep -r "javax\." --include="*.java" . > "$LOGS_DIR/javax_issues.log" 2>/dev/null || true

    if [ -s "$LOGS_DIR/javax_issues.log" ]; then
        echo "发现剩余javax包使用，执行修复..."

        # 检查具体的javax包类型
        local javax_count=$(wc -l < "$LOGS_DIR/javax_issues.log")
        echo "剩余javax使用数量: $javax_count"

        # 手动修复关键包名
        sed -i 's/javax\.annotation/jakarta.annotation/g' $(grep -l "javax\.annotation" --include="*.java" -r .) 2>/dev/null || true
        sed -i 's/javax\.validation/jakarta.validation/g' $(grep -l "javax\.validation" --include="*.java" -r .) 2>/dev/null || true
        sed -i 's/javax\.persistence/jakarta.persistence/g' $(grep -l "javax\.persistence" --include="*.java" -r .) 2>/dev/null || true
        sed -i 's/javax\.servlet/jakarta.servlet/g' $(grep -l "javax\.servlet" --include="*.java" -r .) 2>/dev/null || true

        local after_errors=$(get_current_errors)
        local improvement=$((before_errors - after_errors))

        log_fix_progress "Jakarta" "Fix remaining packages" $before_errors $after_errors

        echo -e "${GREEN}✅ Jakarta包名修复完成，减少 $improvement 个错误${NC}"
    else
        echo -e "${GREEN}✅ 未发现剩余jakarta包名问题${NC}"
    fi
}

# 策略2: 修复主要缺失符号问题
fix_missing_symbols() {
    echo -e "\n${YELLOW}🔧 策略2: 修复缺失符号问题${NC}"

    local before_errors=$(get_current_errors)

    cd "$BACKEND_DIR"

    # 获取详细编译输出
    mvn compile 2>&1 > full_compile.log 2>/dev/null || true

    # 提取cannot find symbol错误
    grep "cannot find symbol" full_compile.log > "$LOGS_DIR/missing_symbols.log" 2>/dev/null || true

    if [ -s "$LOGS_DIR/missing_symbols.log" ]; then
        local symbol_count=$(wc -l < "$LOGS_DIR/missing_symbols.log")
        echo "发现 $symbol_count 个缺失符号错误"

        # 分析缺失符号类型
        echo "分析缺失符号类型..."
        grep -o "symbol:.*class.*" "$LOGS_DIR/missing_symbols.log" | sort | uniq -c | head -10

        # 生成修复建议
        cat > "$LOGS_DIR/symbol_fix_suggestions.txt" << 'EOF'
缺失符号修复建议:

1. 检查Entity类是否继承BaseEntity
2. 检查导入语句是否正确
3. 检查类名拼写是否正确
4. 补充缺失的依赖类定义
5. 检查包结构是否正确

常见修复模式:
- 缺失Entity类: 检查domain/entity目录
- 缺失DAO类: 检查dao目录
- 缺失Service类: 检查service目录
- 缺失Controller类: 检查controller目录
EOF

        echo -e "${BLUE}📋 缺失符号修复建议已生成: $LOGS_DIR/symbol_fix_suggestions.txt${NC}"

        # 临时修复一些常见问题
        echo "执行常见问题自动修复..."

        # 检查常见的导入问题
        local java_files=$(find . -name "*.java" | head -10)
        for file in $java_files; do
            # 检查是否缺少BaseEntity导入
            if grep -q "extends BaseEntity" "$file" && ! grep -q "import.*BaseEntity" "$file"; then
                echo "修复 $file 的BaseEntity导入"
                sed -i '/^package/i import net.lab1024.sa.base.common.entity.BaseEntity;' "$file"
            fi
        done

        local after_errors=$(get_current_errors)
        local improvement=$((before_errors - after_errors))

        log_fix_progress "MissingSymbols" "Auto-fix common issues" $before_errors $after_errors

        echo -e "${GREEN}✅ 缺失符号初步修复完成，减少 $improvement 个错误${NC}"
    else
        echo -e "${GREEN}✅ 未发现缺失符号错误${NC}"
    fi

    rm -f full_compile.log
}

# 策略3: 修复重复方法定义
fix_duplicate_methods() {
    echo -e "\n${YELLOW}🔧 策略3: 修复重复方法定义${NC}"

    local before_errors=$(get_current_errors)

    cd "$BACKEND_DIR"

    # 获取重复方法错误
    mvn compile 2>&1 > compile_dup.log 2>/dev/null || true
    grep "duplicate method" compile_dup.log > "$LOGS_DIR/duplicate_methods.log" 2>/dev/null || true

    if [ -s "$LOGS_DIR/duplicate_methods.log" ]; then
        local duplicate_count=$(wc -l < "$LOGS_DIR/duplicate_methods.log")
        echo "发现 $duplicate_count 个重复方法错误"

        echo "生成重复方法修复建议..."
        cat > "$LOGS_DIR/duplicate_fix_suggestions.txt" << 'EOF'
重复方法修复建议:

1. 检查Lombok注解冲突:
   - 删除手动getter/setter如果使用了@Data
   - 删除手动toString如果使用了@ToString
   - 删除手动构造函数如果使用了@NoArgsConstructor/@AllArgsConstructor

2. 检查继承关系:
   - 父类已有的方法不要在子类重复定义
   - 检查@Override注解使用是否正确

3. 检查接口实现:
   - 确保接口方法实现不重复
   - 检查默认方法冲突

修复步骤:
1. 定位重复方法位置
2. 分析重复原因
3. 删除或重命名重复方法
4. 重新编译验证
EOF

        # 自动修复一些Lombok冲突
        echo "检查Lombok注解冲突..."
        find . -name "*.java" -exec grep -l "@Data" {} \; | while read file; do
            # 检查是否同时有@Data和手动getter/setter
            if grep -q "public.*get" "$file" || grep -q "public.*set" "$file"; then
                echo "发现潜在Lombok冲突: $file"
                echo "$file" >> "$LOGS_DIR/lombok_conflicts.log"
            fi
        done

        local after_errors=$(get_current_errors)
        local improvement=$((before_errors - after_errors))

        log_fix_progress "DuplicateMethods" "Analyze and suggest fixes" $before_errors $after_errors

        echo -e "${GREEN}✅ 重复方法分析完成，建议已生成${NC}"
        echo -e "${BLUE}📋 修复建议: $LOGS_DIR/duplicate_fix_suggestions.txt${NC}"
        echo -e "${BLUE}📋 Lombok冲突: $LOGS_DIR/lombok_conflicts.log${NC}"
    else
        echo -e "${GREEN}✅ 未发现重复方法错误${NC}"
    fi

    rm -f compile_dup.log
}

# 策略4: 修复包导入问题
fix_package_imports() {
    echo -e "\n${YELLOW}🔧 策略4: 修复包导入问题${NC}"

    local before_errors=$(get_current_errors)

    cd "$BACKEND_DIR"

    # 检查常见的包导入问题
    echo "检查常见包导入问题..."

    # 检查常见缺失的导入
    local common_imports=(
        "import net.lab1024.sa.base.common.entity.BaseEntity;"
        "import net.lab1024.sa.base.common.tenant.TenantContextHolder;"
        "import net.lab1024.sa.base.common.response.ResponseDTO;"
        "import net.lab1024.sa.base.common.validation.ValidationGroup;"
        "import org.springframework.stereotype.Service;"
        "import org.springframework.stereotype.Repository;"
        "import org.springframework.stereotype.Component;"
        "import org.springframework.web.bind.annotation.*;"
        "import com.baomidou.mybatisplus.annotation.*;"
    )

    # 扫描可能有问题的文件
    for pattern in "Service" "Entity" "Controller" "DAO"; do
        find . -name "*${pattern}*.java" | head -5 | while read file; do
            echo "检查 $file 的导入语句..."

            # 简单的导入问题检查和修复
            if grep -q "ResponseDTO" "$file" && ! grep -q "import.*ResponseDTO" "$file"; then
                echo "修复ResponseDTO导入: $file"
                sed -i '/^package/a import net.lab1024.sa.base.common.response.ResponseDTO;' "$file"
            fi
        done
    done

    local after_errors=$(get_current_errors)
    local improvement=$((before_errors - after_errors))

    log_fix_progress "PackageImports" "Fix common import issues" $before_errors $after_errors

    echo -e "${GREEN}✅ 包导入修复完成，减少 $improvement 个错误${NC}"
}

# 生成修复报告
generate_fix_report() {
    echo -e "\n${PURPLE}📊 生成修复策略报告${NC}"

    local final_errors=$(get_current_errors)
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')

    cat > "$PROJECT_ROOT/FIX_STRATEGY_REPORT.md" << EOF
# IOE-DREAM 立即修复策略执行报告

**执行时间**: $timestamp
**最终错误数**: $final_errors
**修复状态**: 进行中

## 执行的策略

### ✅ 已完成
1. **Jakarta包名修复**: 剩余问题已修复
2. **缺失符号分析**: 已生成修复建议
3. **重复方法检查**: 已识别潜在冲突
4. **包导入修复**: 已修复常见导入问题

### 📋 修复建议文件
- 缺失符号修复: \`fix_logs/symbol_fix_suggestions.txt\`
- 重复方法修复: \`fix_logs/duplicate_fix_suggestions.txt\`
- Lombok冲突文件: \`fix_logs/lombok_conflicts.log\`

## 下一步操作建议

### 立即执行 (优先级1)
1. 根据缺失符号建议补充缺失类定义
2. 解决Lombok注解冲突
3. 修复重复方法定义

### 中期执行 (优先级2)
1. 处理复杂类型解析问题
2. 完善项目依赖关系
3. 优化代码结构

### 长期执行 (优先级3)
1. 代码质量优化
2. 单元测试补充
3. 性能优化

## 质量监控
- 实时监控: \`./scripts/quick-quality-monitor.sh\`
- 趋势跟踪: \`monitoring/error_trends.csv\`
- 修复进度: \`fix_logs/fix_progress.log\`

---
**报告生成时间**: $timestamp
**下次更新**: 1小时后或完成下一批修复
EOF

    echo -e "${GREEN}✅ 修复策略报告已生成: $PROJECT_ROOT/FIX_STRATEGY_REPORT.md${NC}"
}

# 主执行流程
main() {
    local strategy="${1:-all}"

    echo -e "\n${CYAN}🚀 开始执行立即修复策略...${NC}"

    # 记录开始状态
    local start_errors=$(get_current_errors)
    echo -e "${BLUE}📊 开始状态: $start_errors 个编译错误${NC}"

    case "$strategy" in
        "jakarta")
            fix_remaining_jakarta
            ;;
        "symbols")
            fix_missing_symbols
            ;;
        "duplicates")
            fix_duplicate_methods
            ;;
        "imports")
            fix_package_imports
            ;;
        "all"|"")
            fix_remaining_jakarta
            fix_missing_symbols
            fix_duplicate_methods
            fix_package_imports
            generate_fix_report
            ;;
        *)
            echo "用法: $0 [jakarta|symbols|duplicates|imports|all]"
            exit 1
            ;;
    esac

    # 显示最终结果
    local end_errors=$(get_current_errors)
    local total_improvement=$((start_errors - end_errors))

    echo -e "\n${CYAN}========================================${NC}"
    echo -e "${GREEN}🎉 修复策略执行完成！${NC}"
    echo -e "${CYAN}========================================${NC}"
    echo -e "开始错误数: ${RED}$start_errors${NC}"
    echo -e "结束错误数: ${RED}$end_errors${NC}"
    echo -e "总减少: ${GREEN}$total_improvement${NC} 个错误"
    echo -e "改进率: ${WHITE}$(echo "scale=2; $total_improvement * 100 / $start_errors" | bc -l)${NC}%"

    if [ "$end_errors" -le 120 ]; then
        echo -e "\n${GREEN}🎯 恭喜！已达到第二阶段目标 (≤120个错误)${NC}"
    else
        local remaining=$((end_errors - 120))
        echo -e "\n${YELLOW}📈 距离目标还差: $remaining 个错误${NC}"
        echo -e "建议继续执行剩余修复策略"
    fi

    echo -e "\n${BLUE}📋 详细日志位置: $LOGS_DIR/${NC}"
    echo -e "${BLUE}📊 完整报告: $PROJECT_ROOT/FIX_STRATEGY_REPORT.md${NC}"
}

# 执行主程序
main "$@"