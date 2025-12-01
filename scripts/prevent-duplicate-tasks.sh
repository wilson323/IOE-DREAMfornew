#!/bin/bash

# =============================================================================
# 强制性防重复任务检查脚本 - 必须在执行重要任务前运行
# =============================================================================
# 使用方法: source ./scripts/prevent-duplicate-tasks.sh

echo "🔍 执行强制性任务冲突检查..."

# 检查函数
check_docker_building() {
    if pgrep -f "docker-compose.*build" > /dev/null 2>&1; then
        echo "❌ 检测到Docker构建任务正在运行!"
        echo "   PID: $(pgrep -f 'docker-compose.*build')"
        echo "   请等待当前任务完成或手动终止"
        return 1
    fi
    echo "✅ 无Docker构建冲突"
    return 0
}

check_maven_building() {
    if pgrep -f "mvn.*compile\|mvn.*build" > /dev/null 2>&1; then
        echo "❌ 检测到Maven编译任务正在运行!"
        echo "   PID: $(pgrep -f 'mvn.*compile\|mvn.*build')"
        return 1
    fi
    echo "✅ 无Maven编译冲突"
    return 0
}

check_npm_building() {
    if pgrep -f "npm.*build\|npm.*run" > /dev/null 2>&1; then
        echo "❌ 检测到NPM构建任务正在运行!"
        return 1
    fi
    echo "✅ 无NPM构建冲突"
    return 0
}

check_system_resources() {
    CPU_USAGE=$(top -bn1 | grep "Cpu(s)" | awk '{print $2}' | cut -d'%' -f1)
    MEM_USAGE=$(free | grep Mem | awk '{printf "%.1f", $3/$2 * 100.0}')

    if (( $(echo "$CPU_USAGE > 80" | bc -l) )); then
        echo "⚠️ CPU使用率过高: ${CPU_USAGE}%"
    fi

    if (( $(echo "$MEM_USAGE > 80" | bc -l) )); then
        echo "⚠️ 内存使用率过高: ${MEM_USAGE}%"
    fi

    echo "✅ 系统资源检查完成"
}

# 执行所有检查
main_check() {
    echo "=========================================="
    echo "🛡️ 防重复任务检查 - $(date)"
    echo "=========================================="

    local failed=0

    check_docker_building || failed=1
    check_maven_building || failed=1
    check_npm_building || failed=1
    check_system_resources

    echo "=========================================="
    if [ $failed -eq 0 ]; then
        echo "✅ 检查通过 - 可以安全执行新任务"
        return 0
    else
        echo "❌ 检查失败 - 存在任务冲突，请先解决"
        echo "💡 强制清理命令: pkill -f 'docker-compose.*build'"
        return 1
    fi
}

# 强制执行检查
main_check