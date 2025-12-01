#!/bin/bash

# =============================================================================
# 🔍 增量编译错误监控脚本
# 实时监控Git变更文件的编译错误，基于404个错误修复经验
# 创建日期: 2025-11-22
# 用途: 开发过程中实时检查，防止编译错误累积
# =============================================================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 项目根目录
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

# 监控配置
MONITOR_INTERVAL=30  # 监控间隔(秒)
ERROR_THRESHOLD=5   # 错误阈值
MAX_DISPLAY_ERRORS=10 # 最大显示错误数

echo -e "${BLUE}🔍 增量编译错误监控启动${NC}"
echo -e "${CYAN}项目路径: $PROJECT_ROOT${NC}"
echo -e "${CYAN}监控间隔: ${MONITOR_INTERVAL}秒${NC}"
echo -e "${CYAN}错误阈值: ${ERROR_THRESHOLD}个${NC}"
echo -e "${CYAN}基于经验: 404→10编译错误修复实践${NC}"
echo -e "${BLUE}========================================${NC}"

# 获取变更文件
get_changed_files() {
    # 获取Git工作区变更的Java文件
    local changed_files=$(git diff --name-only HEAD 2>/dev/null | grep "\.java$" || true)
    if [ -z "$changed_files" ]; then
        # 如果没有Git变更，检查最近修改的文件
        changed_files=$(find "smart-admin-api-java17-springboot3" -name "*.java" -newer "$PROJECT_ROOT/.last-check" 2>/dev/null || true)
    fi
    echo "$changed_files"
}

# 检查单个文件的常见问题
check_file_issues() {
    local file="$1"
    local issues=0

    # Jakarta包名检查
    if grep -q "javax\.(annotation|validation|persistence|servlet)" "$file" 2>/dev/null; then
        echo -e "  ${RED}🚨 Jakarta包名违规${NC}"
        ((issues++))
    fi

    # @Autowired检查
    if grep -q "@Autowired" "$file" 2>/dev/null; then
        echo -e "  ${RED}🚨 @Autowired使用违规${NC}"
        ((issues++))
    fi

    # 缺失导入检查
    if grep -q "cannot find symbol\|找不到符号" "$file".log 2>/dev/null; then
        echo -e "  ${YELLOW}⚠️ 可能存在导入问题${NC}"
        ((issues++))
    fi

    return $issues
}

# 快速编译检查
quick_compile_check() {
    echo -e "\n${YELLOW}🔧 执行快速编译检查...${NC}"

    cd "smart-admin-api-java17-springboot3"

    # 执行编译并捕获输出
    if mvn compile -q -DskipTests > ../quick-compile.log 2>&1; then
        error_count=$(grep -c "ERROR" ../quick-compile.log 2>/dev/null || echo "0")

        if [ $error_count -eq 0 ]; then
            echo -e "${GREEN}✅ 编译成功 (0个错误)${NC}"
        elif [ $error_count -le $ERROR_THRESHOLD ]; then
            echo -e "${YELLOW}⚠️ 发现 $error_count 个编译错误 (≤${ERROR_THRESHOLD}可接受)${NC}"
            echo -e "${YELLOW}最近的错误:${NC}"
            grep "ERROR" ../quick-compile.log | head -3 | sed 's/^/   /'
        else
            echo -e "${RED}❌ 发现 $error_count 个编译错误 (>${ERROR_THRESHOLD}不可接受)${NC}"
            echo -e "${RED}错误详情:${NC}"
            grep "ERROR" ../quick-compile.log | head $MAX_DISPLAY_ERRORS | sed 's/^/   /'

            # 分析错误类型
            echo -e "\n${YELLOW}📊 错误类型分析:${NC}"
            grep "ERROR" ../quick-compile.log | awk '{print $NF}' | sort | uniq -c | sort -nr | head -5 | sed 's/^/   /'
        fi

        echo $error_count
    else
        echo -e "${RED}❌ 编译过程失败${NC}"
        tail -10 ../quick-compile.log | sed 's/^/   /'
        echo "999"  # 返回一个大数值表示编译失败
    fi

    rm -f ../quick-compile.log
}

# 变更文件检查
check_changed_files() {
    local changed_files=$(get_changed_files)

    if [ -z "$changed_files" ]; then
        echo -e "${GREEN}✅ 没有检测到Java文件变更${NC}"
        return 0
    fi

    echo -e "\n${CYAN}📝 检查变更文件:${NC}"

    local file_count=0
    local issue_count=0

    for file in $changed_files; do
        if [ -f "$file" ]; then
            echo -e "${BLUE}检查: $file${NC}"
            check_file_issues "$file"
            local file_issues=$?
            issue_count=$((issue_count + file_issues))
            file_count=$((file_count + 1))
        fi
    done

    echo -e "\n${CYAN}变更文件统计: $file_count 个文件，$issue_count 个潜在问题${NC}"
    return $issue_count
}

# 主监控循环
monitor_loop() {
    local check_count=0

    while true; do
        check_count=$((check_count + 1))

        echo -e "\n${BLUE}========================================${NC}"
        echo -e "${BLUE}📊 第 $check_count 次检查 ($(date '+%H:%M:%S'))${NC}"
        echo -e "${BLUE}========================================${NC}"

        # 检查变更文件
        check_changed_files
        local change_issues=$?

        # 执行快速编译
        local compile_errors=$(quick_compile_check)

        # 综合评估
        local total_issues=$((change_issues + compile_errors))

        echo -e "\n${CYAN}📈 综合评估:${NC}"
        echo -e "  变更文件问题: $change_issues 个"
        echo -e "  编译错误: $compile_errors 个"
        echo -e "  总体问题: $total_issues 个"

        # 根据问题严重程度给出建议
        if [ $compile_errors -gt $ERROR_THRESHOLD ]; then
            echo -e "\n${RED}🚨 立即行动建议:${NC}"
            echo -e "${RED}  1. 停止当前开发，修复编译错误${NC}"
            echo -e "${RED}  2. 运行: Skill('compilation-error-prevention-specialist')${NC}"
            echo -e "${RED}  3. 参考: 404→10编译错误修复经验${NC}"
        elif [ $total_issues -gt 0 ]; then
            echo -e "\n${YELLOW}⚠️ 建议关注:${NC}"
            echo -e "${YELLOW}  1. 注意变更文件的规范问题${NC}"
            echo -e "${YELLOW}  2. 及时修复小问题，防止累积${NC}"
        else
            echo -e "\n${GREEN}✅ 状态良好:${NC}"
            echo -e "${GREEN}  1. 可以继续开发${NC}"
            echo -e "${GREEN}  2. 符合零编译错误目标${NC}"
        fi

        # 记录检查时间戳
        touch "$PROJECT_ROOT/.last-check"

        # 等待下次检查
        echo -e "\n${CYAN}⏰ 等待 ${MONITOR_INTERVAL} 秒后进行下次检查... (Ctrl+C 退出)${NC}"
        sleep $MONITOR_INTERVAL
    done
}

# 信号处理
cleanup() {
    echo -e "\n${BLUE}🛑 监控停止${NC}"
    echo -e "${GREEN}感谢使用增量编译错误监控！${NC}"
    exit 0
}

trap cleanup SIGINT SIGTERM

# 检查是否在正确的目录
if [ ! -d "smart-admin-api-java17-springboot3" ]; then
    echo -e "${RED}❌ 错误: 未找到smart-admin-api-java17-springboot3目录${NC}"
    echo -e "${RED}请确保在项目根目录运行此脚本${NC}"
    exit 1
fi

# 开始监控
echo -e "${GREEN}🚀 开始增量编译错误监控...${NC}"
monitor_loop