#!/bin/bash

# =============================================================================
# 工作前验证脚本 - 强制验证上次工作完整性并明确当前任务规范
# =============================================================================
# 使用方法: source ./scripts/pre-work-validation.sh [任务名称] [任务类型]

echo "🔍 执行工作前强制验证..."

# 参数检查
if [ $# -lt 2 ]; then
    echo "❌ 缺少必要参数"
    echo "用法: $0 <任务名称> <任务类型>"
    echo "任务类型: docker-build, maven-compile, npm-build, openspec-task, frontend-fix, system-deploy"
    exit 1
fi

TASK_NAME="$1"
TASK_TYPE="$2"

# 验证函数
validate_previous_work() {
    echo "=========================================="
    echo "🔍 步骤1: 验证上次工作完整性"
    echo "=========================================="

    local failed=0

    # 1. 检查是否有未完成任务
    if [ -f ".todo-lock" ]; then
        local current_task=$(cat ".todo-lock")
        echo "⚠️  检测到未完成任务: $current_task"
        echo "📅 锁定时间: $(stat -c %y ".todo-lock" 2>/dev/null || echo "未知")"

        # 检查任务是否真的在运行
        local process_count=$(pgrep -f "$(get_process_pattern "$current_task")" | wc -l)
        if [ "$process_count" -eq 0 ]; then
            echo "❌ 锁定任务但无对应进程运行，可能存在异常"
            echo "🤔 请检查任务是否意外终止"
            read -p "是否强制解锁并继续? (y/N): " -n 1 -r
            echo
            if [[ $REPLY =~ ^[Yy]$ ]]; then
                rm ".todo-lock"
                echo "✅ 已强制解锁"
            else
                echo "❌ 建议先完成或修复当前任务"
                return 1
            fi
        else
            echo "✅ 任务进程正在运行: $process_count个"
        fi
    else
        echo "✅ 无未完成任务"
    fi

    # 2. 检查最近的编译/构建状态
    echo ""
    echo "🔍 检查最近的工作状态..."

    # 检查Maven编译
    local maven_last_build=$(find . -name "*.class" -newer ".last-validation" 2>/dev/null | wc -l)
    if [ "$maven_last_build" -gt 0 ] && [ -f "pom.xml" ]; then
        echo "✅ Maven编译状态: 正常 (最近有编译成功)"
    elif [ -f "pom.xml" ]; then
        echo "⚠️ Maven编译状态: 可能需要重新编译"
        echo "📊 建议: 执行 'mvn clean compile -DskipTests'"
    fi

    # 检查Docker镜像
    local docker_last_build=$(docker images --format "table {{.Repository}}:{{.Tag}}\t{{.CreatedAt}}" 2>/dev/null | grep "ioe-dream" | head -1)
    if [ -n "$docker_last_build" ]; then
        echo "✅ Docker镜像状态: 正常"
        echo "📦 最新镜像: $docker_last_build"
    else
        echo "⚠️ Docker镜像状态: 可能需要重新构建"
        echo "📊 建议: 执行 'docker-compose build'"
    fi

    # 3. 检查代码变更状态
    echo ""
    echo "🔍 检查代码变更状态..."
    if [ -d ".git" ]; then
        local git_status=$(git status --porcelain 2>/dev/null | wc -l)
        if [ "$git_status" -gt 0 ]; then
            echo "⚠️ 发现 $git_status 个未提交的代码变更"
            echo "📊 建议: 提交前先运行测试验证"
            git status --short
        else
            echo "✅ Git状态: 干净无变更"
        fi
    fi

    # 4. 系统资源检查
    echo ""
    echo "🔍 检查系统资源状态..."

    local cpu_usage=$(top -bn1 | grep "Cpu(s)" | awk '{print $2}' | cut -d'%' -f1 2>/dev/null || echo "0")
    local mem_usage=$(free | grep Mem | awk '{printf "%.1f", $3/$2 * 100.0}' 2>/dev/null || echo "0")
    local disk_usage=$(df . | tail -1 | awk '{print $5}' | sed 's/%//')

    echo "💻 CPU使用率: ${cpu_usage}%"
    echo "💾 内存使用率: ${mem_usage}%"
    echo "💿 磁盘使用率: ${disk_usage}%"

    # 资源警告
    if (( $(echo "$cpu_usage > 80" | bc -l) 2>/dev/null)); then
        echo "⚠️ CPU使用率较高: ${cpu_usage}%"
    fi
    if (( $(echo "$mem_usage > 80" | bc -l) 2>/dev/null)); then
        echo "⚠️ 内存使用率较高: ${mem_usage}%"
    fi
    if [ "${disk_usage%.*}" -gt 90 ]; then
        echo "⚠️ 磁盘使用率较高: ${disk_usage}%"
    fi

    # 5. 环境依赖检查
    echo ""
    echo "🔍 检查关键环境依赖..."

    # Java版本
    if command -v java >/dev/null 2>&1; then
        local java_version=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1)
        echo "✅ Java版本: $java_version"
    else
        echo "❌ Java未安装或不可用"
        failed=1
    fi

    # Docker
    if command -v docker >/dev/null 2>&1; then
        echo "✅ Docker: 可用"
        if docker info >/dev/null 2>&1; then
            echo "✅ Docker服务: 正常"
        else
            echo "❌ Docker服务: 异常"
            failed=1
        fi
    else
        echo "❌ Docker: 未安装"
        failed=1
    fi

    # Git
    if command -v git >/dev/null 2>&1; then
        echo "✅ Git: 可用"
    else
        echo "❌ Git: 未安装"
        failed=1
    fi

    echo "=========================================="
    if [ $failed -eq 0 ]; then
        echo "✅ 上次工作验证通过 - 可以开始新任务"
        return 0
    else
        echo "❌ 上次工作验证失败 - 请先解决上述问题"
        return 1
    fi
}

# 获取任务进程模式
get_process_pattern() {
    local task="$1"
    case "$task" in
        *"Docker"*|*"docker"*)
            echo "docker-compose.*build\|docker.*build"
            ;;
        *"Maven"*|*"maven"*|*"编译"*)
            echo "mvn.*compile\|mvn.*build"
            ;;
        *"NPM"*|*"npm"*|*"前端"*)
            echo "npm.*build\|npm.*run"
            ;;
        *)
            echo "$task"
            ;;
    esac
}

# 明确当前任务规范
clarify_task_requirements() {
    echo "=========================================="
    echo "📋 步骤2: 明确当前任务规范"
    echo "=========================================="
    echo "任务名称: $TASK_NAME"
    echo "任务类型: $TASK_TYPE"

    echo ""
    echo "📚 任务规范要求:"

    case "$TASK_TYPE" in
        "docker-build")
            echo "Docker构建任务规范:"
            echo "- ✅ 确保Docker服务运行正常"
            echo "- ✅ 清理之前的构建缓存"
            echo "- ✅ 检查Docker Compose配置"
            echo "- ✅ 验证镜像构建成功"
            echo "- ✅ 确保容器启动健康"
            echo "- 🚫 不允许同时运行多个构建任务"
            ;;
        "maven-compile")
            echo "Maven编译任务规范:"
            echo "- ✅ 确保Java版本兼容 (Java 17+)"
            echo "- ✅ 清理target目录"
            echo "- ✅ 检查依赖下载状态"
            echo "- ✅ 验证编译无错误"
            echo "- ✅ 运行单元测试通过"
            echo "- 🚫 不允许在编译错误时继续"
            ;;
        "npm-build")
            echo "NPM构建任务规范:"
            echo "- ✅ 确保Node.js版本兼容"
            echo "- ✅ 清理node_modules和dist目录"
            echo "- ✅ 检查package.json依赖"
            echo "- ✅ 验证构建无错误"
            echo "- 🚫 不允许在构建警告时继续"
            ;;
        "openspec-task")
            echo "OpenSpec任务规范:"
            echo "- ✅ 读取相关proposal.md、design.md、tasks.md"
            echo "- ✅ 遵循OpenSpec三阶段工作流"
            echo "- ✅ 验证变更内容"
            echo "- ✅ 更新checklist状态"
            echo "- ✅ 运行openspec validate --strict"
            echo "- 🚫 不允许跳过验证步骤"
            ;;
        "frontend-fix")
            echo "前端修复任务规范:"
            echo "- ✅ 明确问题描述和复现步骤"
            echo "- ✅ 检查依赖版本兼容性"
            echo "- ✅ 创建修复前后的对比"
            echo "- ✅ 验证修复效果"
            echo "- ✅ 更新相关文档"
            ;;
        "system-deploy")
            echo "系统部署任务规范:"
            echo "- ✅ 验证所有组件正常"
            echo "- ✅ 执行完整集成测试"
            echo "- ✅ 检查部署健康状态"
            echo "- ✅ 验证性能指标"
            echo "- ✅ 创建部署回滚计划"
            ;;
        *)
            echo "通用任务规范:"
            echo "- ✅ 明确任务目标和范围"
            echo "- ✅ 预估所需时间和资源"
            echo "- ✅ 识别潜在风险"
            echo "- ✅ 制定验收标准"
            ;;
    esac

    echo ""
    echo "🚨 关键约束条件:"
    echo "- 必须在资源充足时执行"
    echo "- 发现问题必须立即停止"
    echo "- 需要人工干预时主动汇报"
    echo "- 保持任务原子性，中途切换需重新验证"
}

# 创建任务锁
create_task_lock() {
    echo "🔒 创建任务锁..."

    # 更新验证时间戳
    date > ".last-validation"

    # 创建任务锁
    echo "$TASK_NAME" > ".todo-lock"

    # 记录任务开始日志
    echo "[$(date)] 开始任务: $TASK_NAME (类型: $TASK_TYPE)" >> ".task-history"

    echo "✅ 任务已锁定: $TASK_NAME"
    echo "📅 锁定时间: $(date)"

    return 0
}

# 主函数
main() {
    echo "🎯 工作前强制验证系统"
    echo "=========================================="
    echo "任务: $TASK_NAME"
    echo "类型: $TASK_TYPE"
    echo "时间: $(date)"
    echo "=========================================="

    # 步骤1: 验证上次工作完整性
    if ! validate_previous_work; then
        echo ""
        echo "❌ 上次工作验证失败，无法开始新任务"
        echo "💡 请先解决上述问题，或使用 --force 参数强制执行"
        if [ "${3}" = "--force" ]; then
            echo "🚨 强制模式已启用，跳过验证"
        else
            exit 1
        fi
    fi

    echo ""

    # 步骤2: 明确当前任务规范
    clarify_task_requirements

    echo ""

    # 步骤3: 用户确认
    read -p "🤔 确认是否开始执行此任务? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "❌ 用户取消任务"
        exit 1
    fi

    echo ""

    # 步骤4: 创建任务锁
    if ! create_task_lock; then
        echo "❌ 无法创建任务锁，可能存在并发问题"
        exit 1
    fi

    echo ""
    echo "🚀 验证通过，可以开始执行任务!"
    echo "📝 任务规范已明确，资源状态良好"
    echo "🔒 任务已锁定，防止重复执行"

    return 0
}

# 执行主函数
main "$@" "$2" "$3"