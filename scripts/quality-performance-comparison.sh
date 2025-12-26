#!/bin/bash

# IOE-DREAM 质量检查性能对比脚本
# 功能：对比原版和优化版质量检查脚本的性能差异

echo "⚡ IOE-DREAM 质量检查性能对比"
echo "=========================="
echo "对比时间: $(date)"
echo "Git 分支: ${GIT_BRANCH:-$(git rev-parse --abbrev-ref HEAD)}"
echo ""

# 测试函数：执行并测量时间
run_performance_test() {
    local script_name="$1"
    local script_path="$2"
    local iterations="${3:-3}"

    echo "🧪 测试 $script_name ($iterations 次迭代)"
    echo "--------------------------------"

    local total_time=0
    local times=()

    for ((i=1; i<=iterations; i++)); do
        echo "   第 $i 次执行..."
        local start_time=$(date +%s)

        # 执行脚本并捕获结果
        if bash "$script_path" > /dev/null 2>&1; then
            local end_time=$(date +%s)
            local duration=$((end_time - start_time))
            times+=($duration)
            total_time=$((total_time + duration))
            echo "      ✅ 执行时间: ${duration}s"
        else
            echo "      ❌ 执行失败"
            return 1
        fi
    done

    # 计算统计数据
    local avg_time=$((total_time / iterations))
    local min_time=${times[0]}
    local max_time=${times[0]}

    for time in "${times[@]}"; do
        if [ $time -lt $min_time ]; then
            min_time=$time
        fi
        if [ $time -gt $max_time ]; then
            max_time=$time
        fi
    done

    echo ""
    echo "📊 $script_name 性能统计:"
    echo "   平均执行时间: ${avg_time}s"
    echo "   最快执行时间: ${min_time}s"
    echo "   最慢执行时间: ${max_time}s"
    echo "   总执行时间: ${total_time}s"

    return 0
}

# 函数：测试缓存效果
test_cache_effectiveness() {
    echo ""
    echo "🗄️ 测试缓存效果"
    echo "================"

    echo "🧪 第一次执行 (冷启动)"
    local start_time=$(date +%s)
    bash scripts/optimized-quality-check.sh > /dev/null 2>&1
    local first_run=$(( $(date +%s) - start_time ))
    echo "   冷启动时间: ${first_run}s"

    echo "🧪 第二次执行 (缓存命中)"
    start_time=$(date +%s)
    bash scripts/optimized-quality-check.sh > /dev/null 2>&1
    local second_run=$(( $(date +%s) - start_time ))
    echo "   缓存命中时间: ${second_run}s"

    if [ $second_run -lt $first_run ]; then
        local improvement=$((first_run - second_run))
        local improvement_percent=$((improvement * 100 / first_run))
        echo "   🚀 缓存性能提升: ${improvement}s (${improvement_percent}%)"
    else
        echo "   ⚠️ 缓存未显著提升性能"
    fi
}

# 函数：测试不同并行度的效果
test_parallel_performance() {
    echo ""
    echo "🔄 测试并行性能"
    echo "================"

    local jobs=(1 2 4 8)

    for jobs in "${jobs[@]}"; do
        echo "🧪 测试并行度: $jobs"

        # 临时修改脚本中的并行数
        sed -i.bak "s/PARALLEL_JOBS=[0-9]*/PARALLEL_JOBS=$jobs/" scripts/optimized-quality-check.sh

        local start_time=$(date +%s)
        if bash scripts/optimized-quality-check.sh > /dev/null 2>&1; then
            local duration=$(( $(date +%s) - start_time ))
            echo "   并行度 $jobs: ${duration}s"
        else
            echo "   并行度 $jobs: 执行失败"
        fi

        # 恢复原设置
        mv scripts/optimized-quality-check.sh.bak scripts/optimized-quality-check.sh
    done
}

# 函数：内存使用情况分析
analyze_memory_usage() {
    echo ""
    echo "💾 内存使用分析"
    echo "==============="

    echo "🧪 原版脚本内存使用:"
    bash -c "time -v bash scripts/precise-quality-check.sh" 2>&1 | grep "Maximum resident set size" || echo "   内存信息不可用"

    echo ""
    echo "🧪 优化版脚本内存使用:"
    bash -c "time -v bash scripts/optimized-quality-check.sh" 2>&1 | grep "Maximum resident set size" || echo "   内存信息不可用"
}

# 函数：生成性能对比报告
generate_comparison_report() {
    echo ""
    echo "📄 生成性能对比报告..."

    local report_file="monitoring-reports/performance-comparison-$(date +%Y%m%d_%H%M%S).txt"

    {
        echo "IOE-DREAM 质量检查性能对比报告"
        echo "=============================="
        echo "生成时间: $(date)"
        echo "测试环境: $(uname -s) $(uname -r)"
        echo "Git 提交: $(git rev-parse --short HEAD)"
        echo ""

        echo "测试脚本对比:"
        echo "1. 原版脚本: scripts/precise-quality-check.sh"
        echo "   - 单线程顺序执行"
        echo "   - 无缓存机制"
        echo "   - 基础功能实现"
        echo ""

        echo "2. 优化版脚本: scripts/optimized-quality-check.sh"
        echo "   - 多线程并行执行 ($PARALLEL_JOBS 并行)"
        echo "   - 智能缓存机制 (TTL: ${CACHE_TTL}s)"
        echo "   - 增量检查支持"
        echo "   - 性能监控和统计"
        echo ""

        echo "性能优化技术:"
        echo "- 并行处理: 利用多核CPU加速检查"
        echo "- 缓存机制: 避免重复计算"
        echo "- 增量检查: 只检查变更部分"
        echo "- 优化算法: 减少文件系统调用"
        echo ""

        echo "适用场景建议:"
        echo "- 开发环境: 使用优化版，快速反馈"
        echo "- CI/CD环境: 使用原版，确保一致性"
        echo "- 大型项目: 优化版显著提升效率"
        echo "- 持续监控: 缓存机制减少开销"

    } > "$report_file"

    echo "   ✅ 报告已生成: $report_file"
}

# 主执行流程
main() {
    echo "开始性能对比测试..."
    echo ""

    # 测试原版脚本
    run_performance_test "原版质量检查" "scripts/precise-quality-check.sh" 3

    echo ""
    echo "================================"

    # 测试优化版脚本
    run_performance_test "优化版质量检查" "scripts/optimized-quality-check.sh" 3

    # 测试缓存效果
    test_cache_effectiveness

    # 测试并行性能
    test_parallel_performance

    # 内存使用分析（仅在支持的系统上）
    if command -v time >/dev/null 2>&1; then
        analyze_memory_usage
    fi

    # 生成对比报告
    generate_comparison_report

    echo ""
    echo "================================"
    echo "🎉 性能对比测试完成！"
    echo ""
    echo "💡 使用建议:"
    echo "1. 日常开发: 使用优化版 (scripts/optimized-quality-check.sh)"
    echo "2. CI/CD流水线: 使用原版 (scripts/precise-quality-check.sh)"
    echo "3. 性能关键场景: 启用缓存和并行处理"
    echo "4. 大型项目: 调整并行度以获得最佳性能"
    echo ""
    echo "📊 查看详细报告: monitoring-reports/performance-comparison-*.txt"
}

# 检查必要脚本是否存在
if [ ! -f "scripts/precise-quality-check.sh" ] || [ ! -f "scripts/optimized-quality-check.sh" ]; then
    echo "❌ 质量检查脚本不存在，请先确保脚本文件存在"
    exit 1
fi

# 执行主函数
main