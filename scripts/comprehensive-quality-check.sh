#!/bin/bash

# IOE-DREAM 综合质量检查脚本
# 扩展检查维度：代码质量 + 性能 + 安全 + 可维护性

echo "🔍 IOE-DREAM 综合质量检查"
echo "========================"
echo "检查时间: $(date +%Y-%m-%d %H:%M:%S)"
echo "Git 分支: ${GIT_BRANCH:-$(git rev-parse --abbrev-ref HEAD)}"
echo ""

# 初始化检查结果变量
TOTAL_SCORE=100
VIOLATION_DETAILS=()
CHECK_RESULTS=()

# 函数：代码复杂度检查
check_code_complexity() {
    echo "📊 代码复杂度检查"
    echo "------------------"

    local complexity_issues=0
    local total_methods=0
    local high_complexity_methods=0

    # 查找Java文件并分析复杂度
    while IFS= read -r -d '' java_file; do
        # 简单的复杂度估算：统计方法中的控制语句数量
        local method_count=$(grep -c "public\|private\|protected" "$java_file" 2>/dev/null || echo 0)
        local if_count=$(grep -c "if " "$java_file" 2>/dev/null || echo 0)
        local for_count=$(grep -c "for " "$java_file" 2>/dev/null || echo 0)
        local while_count=$(grep -c "while " "$java_file" 2>/dev/null || echo 0)
        local switch_count=$(grep -c "switch " "$java_file" 2>/dev/null || echo 0)

        local complexity=$((if_count + for_count + while_count + switch_count + 1))
        total_methods=$((total_methods + method_count))

        if [ $complexity -gt 10 ]; then
            high_complexity_methods=$((high_complexity_methods + 1))
            VIOLATION_DETAILS+=("复杂度过高: $java_file (复杂度: $complexity)")
        fi
    done < <(find microservices -name "*.java" -type f -print0 2>/dev/null)

    if [ $high_complexity_methods -eq 0 ]; then
        echo "   ✅ 代码复杂度: 优秀 (0个高复杂度方法)"
        CHECK_RESULTS+=("代码复杂度:✅")
        return 0
    else
        echo "   ⚠️ 代码复杂度: 发现 $high_complexity_methods 个高复杂度方法"
        echo "   📋 高复杂度方法总数: $high_complexity_methods"
        CHECK_RESULTS+=("代码复杂度:⚠️")
        return 1
    fi
}

# 函数：代码重复度检查
check_code_duplication() {
    echo "🔄 代码重复度检查"
    echo "------------------"

    # 查找可能的重复代码块
    local duplicate_blocks=0
    local temp_file=$(mktemp)

    # 提取所有Java代码行（去除注释和空行）
    find microservices -name "*.java" -type f -exec grep -v "^\s*//" {} \; -exec grep -v "^\s*\*" {} \; -exec grep -v "^\s*$" {} \; > "$temp_file" 2>/dev/null

    # 简单的重复检查：查找相同行
    local duplicate_lines=$(sort "$temp_file" | uniq -d | wc -l)
    local total_lines=$(wc -l < "$temp_file")

    if [ $total_lines -gt 0 ]; then
        local duplication_rate=$((duplicate_lines * 100 / total_lines))

        if [ $duplication_rate -lt 5 ]; then
            echo "   ✅ 代码重复度: 优秀 (${duplication_rate}%)"
            CHECK_RESULTS+=("代码重复度:✅")
        elif [ $duplication_rate -lt 10 ]; then
            echo "   ⚠️ 代码重复度: 一般 (${duplication_rate}%)"
            CHECK_RESULTS+=("代码重复度:⚠️")
            VIOLATION_DETAILS+=("代码重复度偏高: ${duplication_rate}%")
        else
            echo "   ❌ 代码重复度: 需要改进 (${duplication_rate}%)"
            CHECK_RESULTS+=("代码重复度:❌")
            VIOLATION_DETAILS+=("代码重复度过高: ${duplication_rate}%")
        fi
    else
        echo "   ✅ 代码重复度: 无代码可分析"
        CHECK_RESULTS+=("代码重复度:✅")
    fi

    rm -f "$temp_file"
}

# 函数：测试覆盖率检查
check_test_coverage() {
    echo "🧪 测试覆盖率检查"
    echo "------------------"

    local test_dirs=0
    local total_classes=0
    local test_classes=0

    # 统计测试目录
    while IFS= read -r -d '' test_dir; do
        if [[ "$test_dir" == *"test"* ]]; then
            test_dirs=$((test_dirs + 1))
            test_classes=$(find "$test_dir" -name "*Test.java" -type f | wc -l)
        fi
    done < <(find microservices -type d -name "*test*" -print0 2>/dev/null)

    # 统计源码类
    total_classes=$(find microservices -path "*/src/main/java/*" -name "*.java" -type f | grep -v Test | wc -l)

    if [ $total_classes -gt 0 ]; then
        local coverage_rate=$((test_classes * 100 / total_classes))

        if [ $coverage_rate -ge 80 ]; then
            echo "   ✅ 测试覆盖率: 优秀 (${coverage_rate}%)"
            CHECK_RESULTS+=("测试覆盖率:✅")
        elif [ $coverage_rate -ge 60 ]; then
            echo "   ⚠️ 测试覆盖率: 一般 (${coverage_rate}%)"
            CHECK_RESULTS+=("测试覆盖率:⚠️")
            VIOLATION_DETAILS+=("测试覆盖率偏低: ${coverage_rate}%")
        else
            echo "   ❌ 测试覆盖率: 需要改进 (${coverage_rate}%)"
            CHECK_RESULTS+=("测试覆盖率:❌")
            VIOLATION_DETAILS+=("测试覆盖度过低: ${coverage_rate}%")
        fi
    else
        echo "   ⚠️ 测试覆盖率: 无源码可统计"
        CHECK_RESULTS+=("测试覆盖率:⚠️")
    fi

    echo "   📋 测试目录数: $test_dirs"
    echo "   📋 测试类数: $test_classes"
    echo "   📋 源码类数: $total_classes"
}

# 函数：配置文件安全检查
check_config_security() {
    echo "🔒 配置文件安全检查"
    echo "------------------"

    local security_issues=0
    local config_files=0

    # 检查配置文件中的敏感信息
    while IFS= read -r -d '' config_file; do
        config_files=$((config_files + 1))

        # 检查明文密码
        if grep -q "password.*=[^$]*[^ENC(]" "$config_file" 2>/dev/null; then
            local passwords=$(grep "password.*=[^$]*[^ENC(]" "$config_file" 2>/dev/null | wc -l)
            security_issues=$((security_issues + passwords))
            VIOLATION_DETAILS+=("配置安全: $config_file 包含 $passwords 个明文密码")
        fi

        # 检查硬编码的API密钥
        if grep -qi "api.*key.*=\w" "$config_file" 2>/dev/null; then
            security_issues=$((security_issues + 1))
            VIOLATION_DETAILS+=("配置安全: $config_file 包含硬编码API密钥")
        fi

        # 检查数据库连接信息
        if grep -q "jdbc.*://.*@.*:" "$config_file" 2>/dev/null; then
            local db_connections=$(grep "jdbc.*://.*@.*:" "$config_file" 2>/dev/null | wc -l)
            if ! grep -q "ENC(" "$config_file" 2>/dev/null; then
                security_issues=$((security_issues + db_connections))
                VIOLATION_DETAILS+=("配置安全: $config_file 包含未加密的数据库连接")
            fi
        fi

    done < <(find microservices -name "*.yml" -o -name "*.properties" -o -name "*.yaml" -print0 2>/dev/null)

    if [ $security_issues -eq 0 ]; then
        echo "   ✅ 配置安全: 优秀 (0个安全问题)"
        CHECK_RESULTS+=("配置安全:✅")
    else
        echo "   ❌ 配置安全: 发现 $security_issues 个安全问题"
        CHECK_RESULTS+=("配置安全:❌")
    fi

    echo "   📋 检查配置文件数: $config_files"
}

# 函数：依赖版本安全检查
check_dependency_security() {
    echo "📦 依赖版本安全检查"
    echo "------------------"

    local outdated_deps=0
    local vulnerable_deps=0
    local total_deps=0

    # 检查主要的pom.xml文件
    while IFS= read -r -d '' pom_file; do
        if [ -f "$pom_file" ]; then
            # 检查Spring Boot版本（示例：检查是否使用了较新版本）
            local spring_boot_version=$(grep -o '<spring-boot-starter-parent[^>]*>[^<]*' "$pom_file" 2>/dev/null | grep -o '>[^<]*' | sed 's/[>]<//g' || echo "")

            if [ -n "$spring_boot_version" ]; then
                total_deps=$((total_deps + 1))

                # 简单版本检查：检查是否使用较新版本
                if [[ "$spring_boot_version" < "3.0.0" ]]; then
                    outdated_deps=$((outdated_deps + 1))
                    VIOLATION_DETAILS+=("依赖安全: $pom_file 使用旧版Spring Boot ($spring_boot_version)")
                fi
            fi

            # 检查其他关键依赖版本
            local mysql_version=$(grep -o '<mysql.*version>[^<]*' "$pom_file" 2>/dev/null | grep -o '>[^<]*' | sed 's/[>]<//g' || echo "")
            if [ -n "$mysql_version" ]; then
                if [[ "$mysql_version" < "8.0.0" ]]; then
                    outdated_deps=$((outdated_deps + 1))
                    VIOLATION_DETAILS+=("依赖安全: $pom_file 使用旧版MySQL ($mysql_version)")
                fi
            fi
        fi
    done < <(find microservices -name "pom.xml" -print0 2>/dev/null)

    if [ $outdated_deps -eq 0 ] && [ $vulnerable_deps -eq 0 ]; then
        echo "   ✅ 依赖安全: 优秀 (0个安全问题)"
        CHECK_RESULTS+=("依赖安全:✅")
    else
        echo "   ⚠️ 依赖安全: 发现 $((outdated_deps + vulnerable_deps)) 个安全问题"
        CHECK_RESULTS+=("依赖安全:⚠️")
    fi

    echo "   📋 检查依赖总数: $total_deps"
    echo "   📋 过期依赖: $outdated_deps"
    echo "   📋 漏洞依赖: $vulnerable_deps"
}

# 函数：代码风格一致性检查
check_code_consistency() {
    echo "🎨 代码风格一致性检查"
    echo "--------------------"

    local style_issues=0
    local total_files=0

    while IFS= read -r -d '' java_file; do
        total_files=$((total_files + 1))

        # 检查缩进一致性
        if grep -q $'\t' "$java_file" 2>/dev/null; then
            style_issues=$((style_issues + 1))
        fi

        # 检查行尾空格
        if grep -q '[[:space:]]$' "$java_file" 2>/dev/null; then
            style_issues=$((style_issues + 1))
        fi

        # 检查大括号风格（简单检查）
        local open_braces=$(grep -c '{' "$java_file" 2>/dev/null || echo 0)
        local close_braces=$(grep -c '}' "$java_file" 2>/dev/null || echo 0)
        if [ $open_braces -ne $close_braces ]; then
            style_issues=$((style_issues + 1))
        fi

    done < <(find microservices -name "*.java" -type f -print0 2>/dev/null)

    if [ $total_files -gt 0 ]; then
        local consistency_rate=$(( (total_files - style_issues) * 100 / total_files ))

        if [ $consistency_rate -ge 95 ]; then
            echo "   ✅ 代码风格一致性: 优秀 (${consistency_rate}%)"
            CHECK_RESULTS+=("代码风格:✅")
        elif [ $consistency_rate -ge 85 ]; then
            echo "   ⚠️ 代码风格一致性: 一般 (${consistency_rate}%)"
            CHECK_RESULTS+=("代码风格:⚠️")
            VIOLATION_DETAILS+=("代码风格不一致: ${consistency_rate}%")
        else
            echo "   ❌ 代码风格一致性: 需要改进 (${consistency_rate}%)"
            CHECK_RESULTS+=("代码风格:❌")
            VIOLATION_DETAILS+=("代码风格严重不一致: ${consistency_rate}%")
        fi
    else
        echo "   ✅ 代码风格一致性: 无文件可检查"
        CHECK_RESULTS+=("代码风格:✅")
    fi

    echo "   📋 检查文件数: $total_files"
    echo "   📋 风格问题: $style_issues"
}

# 函数：性能热点检查
check_performance_hotspots() {
    echo "🔥 性能热点检查"
    echo "--------------"

    local performance_issues=0
    local suspicious_patterns=0

    while IFS= read -r -d '' java_file; do
        # 检查可能的性能问题模式

        # 检查循环中的数据库查询
        if grep -q "for.*\(.*select\|.*query\|.*find" "$java_file" 2>/dev/null; then
            suspicious_patterns=$((suspicious_patterns + 1))
            VIOLATION_DETAILS+=("性能热点: $java_file 可能存在循环中的数据库查询")
        fi

        # 检查大量字符串拼接
        if grep -q "+.*\+.*\+" "$java_file" 2>/dev/null; then
            suspicious_patterns=$((suspicious_patterns + 1))
        fi

        # 检查同步方法
        local sync_methods=$(grep -c "synchronized.*(" "$java_file" 2>/dev/null || echo 0)
        if [ $sync_methods -gt 5 ]; then
            performance_issues=$((performance_issues + 1))
            VIOLATION_DETAILS+=("性能热点: $java_file 包含过多同步方法 ($sync_methods)")
        fi

    done < <(find microservices -name "*.java" -type f -print0 2>/dev/null)

    if [ $suspicious_patterns -eq 0 ] && [ $performance_issues -eq 0 ]; then
        echo "   ✅ 性能热点: 优秀 (无性能热点)"
        CHECK_RESULTS+=("性能热点:✅")
    else
        echo "   ⚠️ 性能热点: 发现 $((suspicious_patterns + performance_issues)) 个潜在问题"
        CHECK_RESULTS+=("性能热点:⚠️")
    fi

    echo "   📋 疑似性能模式: $suspicious_patterns"
    echo "   📋 确认性能问题: $performance_issues"
}

# 函数：生成综合评分
generate_comprehensive_score() {
    echo ""
    echo "📊 综合质量评分计算"
    echo "=================="

    local passed_checks=0
    local total_checks=${#CHECK_RESULTS[@]}

    for result in "${CHECK_RESULTS[@]}"; do
        if [[ "$result" == *"✅"* ]]; then
            passed_checks=$((passed_checks + 1))
        fi
    done

    # 基础架构规范检查（核心权重40%）
    local base_score=$(bash scripts/precise-quality-check.sh 2>/dev/null | grep "质量评分:" | sed 's/.*质量评分: \([0-9]*\)\/100.*/\1/' || echo 100)
    local base_weighted_score=$((base_score * 40 / 100))

    # 扩展检查（权重60%）
    local extended_passed=$((passed_checks * 100 / total_checks))
    local extended_weighted_score=$((extended_passed * 60 / 100))

    # 综合评分
    TOTAL_SCORE=$((base_weighted_score + extended_weighted_score))

    echo "   📋 架构规范评分: $base_score/100 (权重40%)"
    echo "   📋 扩展维度评分: $extended_passed/100 (权重60%)"
    echo "   📋 综合质量评分: $TOTAL_SCORE/100"

    # 等级评定
    local grade
    local status
    if [ $TOTAL_SCORE -ge 95 ]; then
        grade="A+"
        status="✅ 优秀"
    elif [ $TOTAL_SCORE -ge 85 ]; then
        grade="A"
        status="✅ 良好"
    elif [ $TOTAL_SCORE -ge 75 ]; then
        grade="B"
        status="⚠️ 一般"
    elif [ $TOTAL_SCORE -ge 60 ]; then
        grade="C"
        status="❌ 需改进"
    else
        grade="D"
        status="❌ 较差"
    fi

    echo "   📊 质量等级: $grade ($status)"
}

# 函数：生成改进建议
generate_improvement_suggestions() {
    echo ""
    echo "💡 综合质量改进建议"
    echo "=================="

    if [ ${#VIOLATION_DETAILS[@]} -eq 0 ]; then
        echo "🎉 恭喜！所有质量检查均通过，代码质量达到优秀水平"
        echo ""
        echo "🚀 持续改进建议:"
        echo "1. 保持当前质量标准"
        echo "2. 定期执行质量检查"
        echo "3. 关注新的质量标准和技术演进"
        echo "4. 建立团队质量分享机制"
    else
        echo "📋 发现的质量问题:"
        for issue in "${VIOLATION_DETAILS[@]}"; do
            echo "   - $issue"
        done
        echo ""
        echo "🔧 改进措施:"
        echo "1. 针对上述问题制定改进计划"
        echo "2. 优先处理高优先级问题"
        echo "3. 建立定期质量回顾机制"
        echo "4. 加强团队质量培训"
    fi
}

# 主执行流程
main() {
    echo "开始执行综合质量检查..."
    echo ""

    # 基础架构检查
    echo "🏗️ 基础架构规范检查"
    bash scripts/precise-quality-check.sh
    echo ""

    # 扩展维度检查
    check_code_complexity
    check_code_duplication
    check_test_coverage
    check_config_security
    check_dependency_security
    check_code_consistency
    check_performance_hotspots

    # 生成综合评分
    generate_comprehensive_score

    # 生成改进建议
    generate_improvement_suggestions

    # 生成综合报告
    local report_file="monitoring-reports/comprehensive-quality-$(date +%Y%m%d_%H%M%S).txt"
    {
        echo "IOE-DREAM 综合质量检查报告"
        echo "=========================="
        echo "检查时间: $(date)"
        echo "Git 分支: ${GIT_BRANCH:-$(git rev-parse --abbrev-ref HEAD)}"
        echo "综合评分: $TOTAL_SCORE/100"
        echo ""
        echo "检查结果详情:"
        for result in "${CHECK_RESULTS[@]}"; do
            echo "  $result"
        done
        echo ""
        if [ ${#VIOLATION_DETAILS[@]} -gt 0 ]; then
            echo "问题详情:"
            for issue in "${VIOLATION_DETAILS[@]}"; do
                echo "  - $issue"
            done
        fi
    } > "$report_file"

    echo ""
    echo "=========================="
    if [ $TOTAL_SCORE -ge 85 ]; then
        echo "🎉 综合质量检查通过！"
        echo "✅ 代码质量达到良好及以上水平"
        echo "📁 详细报告: $report_file"
        exit 0
    else
        echo "⚠️ 综合质量检查需要改进"
        echo "📁 详细报告: $report_file"
        exit 1
    fi
}

# 执行主函数
main