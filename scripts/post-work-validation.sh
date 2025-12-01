#!/bin/bash

# =============================================================================
# 工作后验证脚本 - 验证工作完成质量并更新状态
# =============================================================================
# 使用方法: source ./scripts/post-work-validation.sh [任务名称] [任务状态: success|failed|partial]

echo "🔍 执行工作后质量验证..."

# 参数检查
if [ $# -lt 2 ]; then
    echo "❌ 缺少必要参数"
    echo "用法: $0 <任务名称> <任务状态>"
    echo "任务状态: success, failed, partial"
    exit 1
fi

TASK_NAME="$1"
TASK_STATUS="$2"

# 验证工作完成质量
validate_work_quality() {
    echo "=========================================="
    echo "🔍 步骤1: 验证工作完成质量"
    echo "=========================================="
    echo "任务: $TASK_NAME"
    echo "状态: $TASK_STATUS"

    local failed=0

    # 1. 基于任务类型进行特定验证
    echo ""
    echo "🔍 执行任务质量检查..."

    if [[ "$TASK_NAME" == *"Docker"* ]] || [[ "$TASK_TYPE" == *"docker"* ]]; then
        echo "Docker构建任务质量检查:"

        # 检查镜像构建状态
        if docker images | grep -q "ioe-dream"; then
            echo "✅ Docker镜像: 已构建"
        else
            echo "❌ Docker镜像: 未找到"
            failed=1
        fi

        # 检查容器运行状态
        if docker-compose ps | grep -q "Up.*healthy\|Up.*starting"; then
            echo "✅ 容器状态: 正常"
        else
            echo "⚠️ 容器状态: 可能需要检查"
        fi

    elif [[ "$TASK_NAME" == *"编译"* ]] || [[ "$TASK_TYPE" == *"maven"* ]] || [[ "$TASK_TYPE" == *"compile"* ]]; then
        echo "Maven编译任务质量检查:"

        # 检查编译输出
        if [ -d "target/classes" ]; then
            local class_count=$(find target/classes -name "*.class" | wc -l)
            echo "✅ 编译结果: $class_count 个class文件"
        else
            echo "❌ 编译结果: 无输出"
            failed=1
        fi

        # 检查编译错误
        if find . -name "*.java" -exec grep -l "javax\." {} \; >/dev/null 2>&1; then
            echo "❌ 包名检查: 仍存在javax使用"
            failed=1
        else
            echo "✅ 包名检查: 符合jakarta规范"
        fi

    elif [[ "$TASK_NAME" == *"OpenSpec"* ]] || [[ "$TASK_TYPE" == *"openspec"* ]]; then
        echo "OpenSpec任务质量检查:"

        # 检查OpenSpec状态
        if openspec validate smart-access-multimodal-biometric >/dev/null 2>&1; then
            echo "✅ OpenSpec验证: 通过"
        else
            echo "❌ OpenSpec验证: 失败"
            failed=1
        fi

        # 检查任务状态更新
        if grep -q "$TASK_NAME.*\[x\]" openspec/changes/*/tasks.md; then
            echo "✅ 任务状态: 已更新为完成"
        else
            echo "⚠️ 任务状态: 可能需要更新checklist"
        fi

    elif [[ "$TASK_NAME" == *"前端"* ]] || [[ "$TASK_TYPE" == *"npm"* ]] || [[ "$TASK_TYPE" == *"frontend"* ]]; then
        echo "前端构建任务质量检查:"

        # 检查构建输出
        if [ -d "dist" ]; then
            echo "✅ 构建输出: dist目录存在"
        else
            echo "❌ 构建输出: 无dist目录"
            failed=1
        fi

        # 检查package.json
        if [ -f "package.json" ]; then
            echo "✅ 依赖文件: package.json正常"
        else
            echo "⚠️ 依赖文件: 可能存在问题"
        fi
    fi

    # 2. 通用质量检查
    echo ""
    echo "🔍 执行通用质量检查..."

    # 检查错误日志
    if [ -f "application.log" ] || [ -f "logs/error.log" ]; then
        local error_count=$(grep -c "ERROR\|Exception\|Failed" application.log logs/error.log 2>/dev/null || echo "0")
        if [ "$error_count" -gt 0 ]; then
            echo "⚠️ 发现 $error_count 个错误日志条目"
        else
            echo "✅ 错误日志: 无新错误"
        fi
    fi

    # 检查性能指标
    echo "💰 性能指标:"

    # 内存使用
    if command -v free >/dev/null 2>&1; then
        local mem_usage=$(free | grep Mem | awk '{printf "%.1f", $3/$2 * 100.0}')
        echo "   内存使用率: ${mem_usage}%"
        if (( $(echo "$mem_usage > 85" | bc -l 2>/dev/null)); then
            echo "   ⚠️ 内存使用率较高"
        fi
    fi

    echo "=========================================="
    if [ $failed -eq 0 ]; then
        echo "✅ 工作质量验证通过"
        return 0
    else
        echo "❌ 工作质量验证失败"
        return 1
    fi
}

# 更新任务历史
update_task_history() {
    echo "=========================================="
    echo "📋 步骤2: 更新任务历史记录"
    echo "=========================================="

    # 记录任务完成
    echo "[$(date)] 任务完成: $TASK_NAME (状态: $TASK_STATUS)" >> ".task-history"

    # 如果是成功完成，记录统计
    if [ "$TASK_STATUS" = "success" ]; then
        echo "[$(date)] 统计: 已完成 $(grep -c "任务完成.*状态: success" ".task-history") 个任务" >> ".task-stats"
    elif [ "$TASK_STATUS" = "failed" ]; then
        echo "[$(date)] 统计: 已失败 $(grep -c "任务完成.*状态: failed" ".task-history") 个任务" >> ".task-stats"
    fi

    # 显示最近任务历史
    echo ""
    echo "📊 最近任务历史 (最近5条):"
    tail -5 ".task-history" 2>/dev/null || echo "暂无任务历史"

    echo ""
    echo "📊 任务统计:"
    if [ -f ".task-stats" ]; then
        cat ".task-stats"
    else
        echo "暂无统计数据"
    fi

    echo "=========================================="
}

# 释放任务锁
release_task_lock() {
    echo "🔓 释放任务锁..."

    if [ -f ".todo-lock" ]; then
        local locked_task=$(cat ".todo-lock")
        rm ".todo-lock"
        echo "✅ 任务锁已释放: $locked_task"
    else
        echo "✅ 无任务锁需要释放"
    fi

    # 更新完成时间戳
    date > ".last-completion"
}

# 生成工作报告
generate_work_report() {
    echo "=========================================="
    echo "📄 步骤3: 生成工作完成报告"
    echo "=========================================="

    local report_file="reports/work-report-$(date +%Y%m%d-%H%M%S).md"

    # 确保报告目录存在
    mkdir -p "reports"

    # 生成报告内容
    cat > "$report_file" << EOF
# 工作完成报告

## 基本信息
- **任务名称**: $TASK_NAME
- **任务类型**: $TASK_TYPE
- **完成状态**: $TASK_STATUS
- **完成时间**: $(date)
- **执行时长**: $(calculate_duration)

## 验证结果
$(validate_work_quality false)

## 系统状态
$(get_system_status)

## 下次建议
$(get_next_recommendations)

---
*报告由工作后验证脚本自动生成*
EOF

    echo "✅ 工作报告已生成: $report_file"
}

# 计算任务持续时间
calculate_duration() {
    if [ -f ".last-validation" ]; then
        local start_time=$(stat -c %Y .last-validation)
        local end_time=$(stat -c %Y)
        local duration=$((end_time - start_time))
        echo "${duration}秒"
    else
        echo "未知"
    fi
}

# 获取系统状态
get_system_status() {
    echo "### 系统资源状态"
    echo ""
    if command -v free >/dev/null 2>&1; then
        local mem_info=$(free -h)
        echo "#### 内存状态"
        echo "\`\`\n$mem_info\n\`\`"
    fi

    if command -v df >/dev/null 2>&1; then
        local disk_info=$(df -h .)
        echo "#### 磁盘状态"
        echo "\`\`\n$disk_info\n\`\`"
    fi

    if command -v docker >/dev/null 2>&1; then
        echo "#### Docker状态"
        echo "- 运行中容器: $(docker ps -q | wc -l)"
        echo "- 停止容器: $(docker ps -a -q --filter "status=exited" | wc -l)"
        echo "- 镜像数量: $(docker images -q | wc -l)"
    fi
}

# 获取下次建议
get_next_recommendations() {
    echo "### 下次工作建议"
    echo ""

    # 基于当前状态提供建议
    if [ "$TASK_STATUS" = "failed" ]; then
        echo "1. 🔧 优先解决当前失败问题"
        echo "2. 📋 分析失败原因并制定改进计划"
        echo "3. 🧪 重新测试确保问题解决"
        echo "4. 📝 更新相关文档和规范"
    elif [ "$TASK_STATUS" = "partial" ]; then
        echo "1. 🔍 识别未完成的部分"
        echo "2. 📋 补充缺失的工作内容"
        echo "3. ✅ 完善测试覆盖率"
        echo "4. 📊 验证整体功能"
    else
        echo "1. ✅ 验证所有依赖组件正常"
        echo "2. 🔍 检查是否有后续任务需要处理"
       3. 📊 评估性能影响和优化空间"
        echo "4. 📝 更新项目文档和用户指南"
    fi
}

# 主函数
main() {
    echo "🎯 工作后质量验证系统"
    echo "=========================================="
    echo "任务: $TASK_NAME"
    echo "状态: $TASK_STATUS"
    echo "时间: $(date)"
    echo "=========================================="

    # 步骤1: 验证工作完成质量
    if ! validate_work_quality; then
        echo ""
        echo "❌ 工作质量验证失败"
        echo "💡 请先解决问题并重新运行验证"
        exit 1
    fi

    echo ""

    # 步骤2: 更新任务历史
    update_task_history

    echo ""

    # 步骤3: 释放任务锁
    release_task_lock

    echo ""

    # 步骤4: 生成工作报告
    generate_work_report

    echo ""
    echo "🎉 工作后验证完成!"
    echo "✅ 质量验证: 通过"
    echo "✅ 状态更新: 已完成"
    echo "✅ 锁定释放: 已解锁"
    echo "✅ 报告生成: 已保存"

    return 0
}

# 执行主函数
main "$@"