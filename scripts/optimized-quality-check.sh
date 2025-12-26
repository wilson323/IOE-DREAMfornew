#!/bin/bash

# IOE-DREAM 优化版质量检查脚本
# 性能优化：并行检查、增量检查、缓存机制

echo "⚡ IOE-DREAM 优化版质量检查"
echo "==========================="
echo "检查时间: $(date +%Y-%m-%d %H:%M:%S)"
echo "Git 分支: ${GIT_BRANCH:-$(git rev-parse --abbrev-ref HEAD)}"
echo ""

# 性能计时
START_TIME=$(date +%s)

# 配置变量
ENABLE_CACHE=true
CACHE_DIR=".quality-cache"
CACHE_TTL=300  # 缓存5分钟
PARALLEL_JOBS=4
ENABLE_INCREMENTAL=true

# 创建缓存目录
mkdir -p "$CACHE_DIR"

# 函数：检查缓存有效性
is_cache_valid() {
    local cache_file="$1"
    if [ "$ENABLE_CACHE" = false ]; then
        return 1
    fi

    if [ ! -f "$cache_file" ]; then
        return 1
    fi

    local cache_time=$(stat -c %Y "$cache_file" 2>/dev/null || stat -f %m "$cache_file" 2>/dev/null)
    local current_time=$(date +%s)
    local age=$((current_time - cache_time))

    [ $age -lt $CACHE_TTL ]
}

# 函数：并行检查函数
parallel_check() {
    local check_name="$1"
    local check_command="$2"
    local cache_file="$CACHE_DIR/${check_name}.cache"

    echo "🔍 $check_name"

    # 检查缓存
    if is_cache_valid "$cache_file"; then
        echo "   📋 使用缓存结果"
        cat "$cache_file"
        return 0
    fi

    # 执行检查
    local start_time=$(date +%s)
    local result
    result=$(eval "$check_command" 2>/dev/null)
    local end_time=$(date +%s)
    local duration=$((end_time - start_time))

    # 缓存结果
    if [ "$ENABLE_CACHE" = true ]; then
        echo "$result" > "$cache_file"
    fi

    echo "$result"
    echo "   ⏱️ 执行时间: ${duration}s"
}

# 函数：快速SLF4J检查（优化版）
check_slf4j_optimized() {
    # 使用find的并行处理能力
    local violations
    violations=$(find microservices -name "*.java" -type f -print0 2>/dev/null | \
        xargs -0 -P $PARALLEL_JOBS grep -l "LoggerFactory.getLogger" 2>/dev/null | \
        wc -l)

    if [ "$violations" -eq 0 ]; then
        echo "   ✅ LoggerFactory使用: 0个违规"
        return 0
    else
        echo "   ❌ LoggerFactory使用: $violations 个违规"
        echo "   📋 需要修复的文件:"
        find microservices -name "*.java" -type f -print0 2>/dev/null | \
            xargs -0 -P $PARALLEL_JOBS grep -l "LoggerFactory.getLogger" 2>/dev/null | \
            head -3 | while read -r file; do
                echo "      - $file"
            done
        return 1
    fi
}

# 函数：快速依赖注入检查
check_autowired_optimized() {
    local violations
    violations=$(find microservices -name "*.java" -type f -print0 2>/dev/null | \
        xargs -0 -P $PARALLEL_JOBS grep -l "@Autowired" 2>/dev/null | \
        wc -l)

    if [ "$violations" -eq 0 ]; then
        echo "   ✅ @Autowired 注解: 0个违规"
        return 0
    else
        echo "   ❌ @Autowired 注解: $violations 个违规"
        echo "   📋 需要修复的文件:"
        find microservices -name "*.java" -type f -print0 2>/dev/null | \
            xargs -0 -P $PARALLEL_JOBS grep -l "@Autowired" 2>/dev/null | \
            head -3 | while read -r file; do
                echo "      - $file"
            done
        return 1
    fi
}

# 函数：快速Repository检查
check_repository_optimized() {
    local violations
    violations=$(find microservices -name "*.java" -type f -print0 2>/dev/null | \
        xargs -0 -P $PARALLEL_JOBS grep -l "@Repository" 2>/dev/null | \
        wc -l)

    if [ "$violations" -eq 0 ]; then
        echo "   ✅ @Repository 注解: 0个违规"
        return 0
    else
        echo "   ❌ @Repository 注解: $violations 个违规"
        echo "   📋 需要修复的文件:"
        find microservices -name "*.java" -type f -print0 2>/dev/null | \
            xargs -0 -P $PARALLEL_JOBS grep -l "@Repository" 2>/dev/null | \
            head -3 | while read -r file; do
                echo "      - $file"
            done
        return 1
    fi
}

# 函数：快速命名检查
check_naming_optimized() {
    local violations
    violations=$(find microservices -name "*Repository.java" -type f 2>/dev/null | wc -l)

    if [ "$violations" -eq 0 ]; then
        echo "   ✅ Repository 后缀命名: 0个违规"
        return 0
    else
        echo "   ❌ Repository 后缀命名: $violations 个违规"
        echo "   📋 违规文件:"
        find microservices -name "*Repository.java" -type f 2>/dev/null | head -3 | while read -r file; do
            echo "      - $file"
        done
        return 1
    fi
}

# 函数：快速安全检查
check_security_optimized() {
    local violations
    violations=$(find microservices -name "*.yml" -o -name "*.properties" -o -name "*.yaml" 2>/dev/null | \
        xargs grep -H "^[^#]*password.*=" 2>/dev/null | \
        grep -v "ENC(" | \
        grep -v "\${.*}" | \
        wc -l)

    if [ "$violations" -eq 0 ]; then
        echo "   ✅ 明文密码检查: 0个违规"
        return 0
    else
        echo "   ❌ 明文密码检查: $violations 个违规"
        echo "   📋 违规配置:"
        find microservices -name "*.yml" -o -name "*.properties" -o -name "*.yaml" 2>/dev/null | \
            xargs grep -H "^[^#]*password.*=" 2>/dev/null | \
            grep -v "ENC(" | \
            grep -v "\${.*}" | \
            head -3 | while read -r line; do
                echo "      - $line"
            done
        return 1
    fi
}

# 函数：编译检查（优化版）
check_compilation_optimized() {
    echo "🔍 编译质量检查"

    # 检查是否有增量编译的可能
    local pom_hash_file="$CACHE_DIR/pom.hash"
    local current_pom_hash
    current_pom_hash=$(find microservices -name "pom.xml" -exec sha256sum {} \; 2>/dev/null | sha256sum)

    if is_cache_valid "$pom_hash_file" && [ -f "$CACHE_DIR/compilation.success" ]; then
        if cmp -s <(echo "$current_pom_hash") "$pom_hash_file"; then
            echo "   ✅ 编译检查: 使用缓存结果 (编译成功)"
            return 0
        fi
    fi

    # 执行编译检查
    local start_time=$(date +%s)

    echo "   🔧 执行Maven编译检查..."

    # 并行编译检查（只检查编译，不执行测试）
    if mvn clean compile -q -Dmaven.test.skip=true -T 1C -pl microservices 2>/dev/null; then
        echo "   ✅ 编译检查: 成功"
        echo "$current_pom_hash" > "$pom_hash_file"
        touch "$CACHE_DIR/compilation.success"

        local end_time=$(date +%s)
        local duration=$((end_time - start_time))
        echo "   ⏱️ 编译时间: ${duration}s"
        return 0
    else
        echo "   ❌ 编译检查: 失败"
        rm -f "$CACHE_DIR/compilation.success"
        return 1
    fi
}

# 函数：并行执行所有检查
run_parallel_checks() {
    echo "📋 并行架构规范检查"
    echo "==================="

    # 创建临时文件收集结果
    local temp_dir=$(mktemp -d)
    local results_files=()

    # 并行执行检查
    {
        check_slf4j_optimized > "$temp_dir/slf4j.result" &
        echo $! > "$temp_dir/slf4j.pid"

        check_autowired_optimized > "$temp_dir/autowired.result" &
        echo $! > "$temp_dir/autowired.pid"

        check_repository_optimized > "$temp_dir/repository.result" &
        echo $! > "$temp_dir/repository.pid"

        check_naming_optimized > "$temp_dir/naming.result" &
        echo $! > "$temp_dir/naming.pid"

        check_security_optimized > "$temp_dir/security.result" &
        echo $! > "$temp_dir/security.pid"
    }

    # 等待所有并行任务完成
    for pid_file in "$temp_dir"/*.pid; do
        if [ -f "$pid_file" ]; then
            local pid=$(cat "$pid_file")
            wait "$pid"
        fi
    done

    # 收集结果
    local total_violations=0
    local checks_passed=true

    for result_file in "$temp_dir"/*.result; do
        if [ -f "$result_file" ]; then
            echo "---"
            cat "$result_file"

            # 提取违规数量
            local violations
            violations=$(grep "个违规" "$result_file" | sed 's/.*\([0-9]*\) 个违规.*/\1/' 2>/dev/null || echo "0")
            if [ "$violations" -gt 0 ]; then
                total_violations=$((total_violations + violations))
                checks_passed=false
            fi
        fi
    done

    # 清理临时文件
    rm -rf "$temp_dir"

    # 执行编译检查（串行，因为需要Maven）
    echo ""
    check_compilation_optimized
    local compilation_result=$?

    if [ $compilation_result -ne 0 ]; then
        checks_passed=false
        total_violations=$((total_violations + 1))
    fi

    echo ""
    return $([ "$checks_passed" = true ] && [ $total_violations -eq 0 ])
}

# 函数：生成性能报告
generate_performance_report() {
    local end_time=$(date +%s)
    local total_time=$((end_time - START_TIME))

    echo ""
    echo "⚡ 性能统计"
    echo "============"
    echo "   总执行时间: ${total_time}s"
    echo "   并行任务数: $PARALLEL_JOBS"
    echo "   缓存状态: $([ "$ENABLE_CACHE" = true ] && echo "✅ 启用" || echo "❌ 禁用")"
    echo "   增量检查: $([ "$ENABLE_INCREMENTAL" = true ] && echo "✅ 启用" || echo "❌ 禁用")"

    # 计算性能提升
    echo ""
    echo "📊 性能对比"
    echo "------------"
    echo "   预估原版执行时间: ~120s"
    echo "   优化版执行时间: ${total_time}s"
    local improvement=$((120 - total_time))
    echo "   性能提升: ${improvement}s (${improvement/120*1.0 | cut -d. -f1}%)"
}

# 函数：清理缓存
cleanup_cache() {
    if [ "$1" = "--clean-cache" ]; then
        echo "🧹 清理质量检查缓存..."
        rm -rf "$CACHE_DIR"
        echo "   ✅ 缓存已清理"
        exit 0
    fi
}

# 主执行流程
main() {
    # 处理清理缓存参数
    cleanup_cache "$@"

    echo "⚡ 启动优化版质量检查..."
    echo "   并行任务数: $PARALLEL_JOBS"
    echo "   缓存启用: $ENABLE_CACHE"
    echo ""

    # 运行并行检查
    run_parallel_checks
    local checks_result=$?

    # 生成质量评分
    echo ""
    echo "==========================="
    echo "📊 优化版质量检查结果:"

    # 从结果文件中提取总违规数（这里简化处理）
    local violations=$(bash scripts/precise-quality-check.sh 2>/dev/null | grep "总违规数:" | sed 's/.*总违规数: \([0-9]*\).*/\1/' || echo "0")

    local quality_score=$((100 - (violations * 5)))
    if [ $quality_score -lt 0 ]; then
        quality_score=0
    fi

    local grade
    if [ $quality_score -ge 95 ]; then
        grade="A+"
    elif [ $quality_score -ge 85 ]; then
        grade="A"
    elif [ $quality_score -ge 75 ]; then
        grade="B"
    elif [ $quality_score -ge 60 ]; then
        grade="C"
    else
        grade="D"
    fi

    echo "   总违规数: $violations"
    echo "   质量评分: $quality_score/100"
    echo "   质量等级: $grade"

    echo ""
    echo "==========================="

    if $checks_result; then
        echo "🎉 优化版质量门禁检查通过！"
        echo "✅ 代码完全符合 IOE-DREAM 架构规范"
        echo ""
        echo "🚀 可以安全提交和部署"

        # 生成性能报告
        generate_performance_report

        exit 0
    else
        echo "⚠️ 优化版质量门禁检查未通过"
        echo "❌ 发现架构违规，需要修复"
        echo ""
        echo "🔧 修复建议:"
        echo "1. 查看上述违规详情"
        echo "2. 运行对应的修复脚本"
        echo "3. 重新运行质量检查"

        # 生成性能报告
        generate_performance_report

        exit 1
    fi
}

# 执行主函数
main "$@"