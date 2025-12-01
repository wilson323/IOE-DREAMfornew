#!/bin/bash

# =============================================================================
# 强制性TodoList管理器 - 防止重复任务
# =============================================================================

TODO_FILE="D:\IOE-DREAM\.current-todo.json"
LOCK_FILE="D:\IOE-DREAM\.todo-lock"

# 检查是否有锁文件（表示有任务正在进行）
check_lock() {
    if [ -f "$LOCK_FILE" ]; then
        local current_task=$(cat "$LOCK_FILE")
        echo "⚠️ 检测到任务正在进行: $current_task"
        echo "📅 锁定时间: $(stat -c %y "$LOCK_FILE" 2>/dev/null || echo "未知")"
        return 1
    fi
    return 0
}

# 创建锁文件
create_lock() {
    local task_name="$1"
    echo "$task_name" > "$LOCK_FILE"
    echo "🔒 任务已锁定: $task_name"
}

# 释放锁文件
release_lock() {
    if [ -f "$LOCK_FILE" ]; then
        local completed_task=$(cat "$LOCK_FILE")
        rm "$LOCK_FILE"
        echo "✅ 任务已完成并解锁: $completed_task"
    fi
}

# 强制清理锁（紧急情况）
force_unlock() {
    if [ -f "$LOCK_FILE" ]; then
        local stuck_task=$(cat "$LOCK_FILE")
        rm "$LOCK_FILE"
        echo "🚨 强制解锁卡住的任务: $stuck_task"
    fi
}

# 检查任务状态
check_task_status() {
    local task_type="$1"

    case "$task_type" in
        "docker-build")
            check_lock && return 0 || return 1
            ;;
        "maven-compile")
            check_lock && return 0 || return 1
            ;;
        "npm-build")
            check_lock && return 0 || return 1
            ;;
        *)
            echo "❌ 未知任务类型: $task_type"
            return 1
            ;;
    esac
}

# 启动任务（带锁检查）
start_task() {
    local task_name="$1"
    local task_type="$2"

    echo "🚀 尝试启动任务: $task_name"

    if ! check_task_status "$task_type"; then
        echo "❌ 任务冲突，无法启动"
        return 1
    fi

    create_lock "$task_name"
    return 0
}

# 完成任务
complete_task() {
    local task_name="$1"
    echo "🏁 完成任务: $task_name"
    release_lock
}

# 显示当前状态
show_status() {
    echo "================================"
    echo "📋 当前任务状态"
    echo "================================"

    if check_lock; then
        echo "✅ 无任务正在进行"
    else
        echo "⏳ 有任务正在进行中"
    fi

    echo "🔍 系统进程检查:"
    echo "Docker构建: $(pgrep -f 'docker-compose.*build' > /dev/null 2>&1 && echo "运行中" || echo "无")"
    echo "Maven编译: $(pgrep -f 'mvn.*compile\|mvn.*build' > /dev/null 2>&1 && echo "运行中" || echo "无")"
    echo "NPM构建: $(pgrep -f 'npm.*build\|npm.*run' > /dev/null 2>&1 && echo "运行中" || echo "无")"
    echo "================================"
}

# 主函数
case "$1" in
    "start")
        start_task "$2" "$3"
        ;;
    "complete")
        complete_task "$2"
        ;;
    "status")
        show_status
        ;;
    "force-unlock")
        force_unlock
        ;;
    *)
        echo "用法: $0 {start|complete|status|force-unlock} [task_name] [task_type]"
        echo "任务类型: docker-build, maven-compile, npm-build"
        exit 1
        ;;
esac