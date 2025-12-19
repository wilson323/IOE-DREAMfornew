#!/bin/bash
# Pre-commit Hook - IOE-DREAM 架构合规性检查
# 在Git commit前自动执行P0-P4检查

set -euo pipefail

echo "🔍 IOE-DREAM Pre-commit 架构检查开始..."

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志文件
LOG_FILE=".git/hooks/pre-commit.log"
echo "IOE-DREAM Pre-commit 检查报告" > "$LOG_FILE"
echo "检查时间: $(date -u +"%Y-%m-%d %H:%M:%S UTC")" >> "$LOG_FILE"
echo "分支: $(git branch --show-current)" >> "$LOG_FILE"
echo "======================================" >> "$LOG_FILE"

VIOLATION_COUNT=0

# 检查函数
run_check() {
    local check_name="$1"
    local check_script="$2"
    local max_violations="${3:-0}"

    echo -e "${BLUE}🔍 执行 $check_name 检查...${NC}"

    if [ -f "$check_script" ]; then
        chmod +x "$check_script"
        if bash "$check_script" >> "$LOG_FILE" 2>&1; then
            echo -e "${GREEN}✅ $check_name 检查通过${NC}"
            return 0
        else
            echo -e "${RED}❌ $check_name 检查失败${NC}"
            local violations=$(tail -5 "$LOG_FILE" | grep "发现问题数量" | awk '{print $NF}' || echo "1")
            VIOLATION_COUNT=$((VIOLATION_COUNT + violations))
            return 1
        fi
    else
        echo -e "${YELLOW}⚠️  跳过 $check_name：检查脚本不存在${NC}"
        return 0
    fi
}

# 获取变更的文件
get_changed_files() {
    # 获取暂存区的Java文件
    git diff --cached --name-only --diff-filter=ACM | grep -E "\.java$" || true
}

# 快速检查（仅检查变更的文件）
quick_structure_check() {
    echo -e "${BLUE}🔍 执行快速结构检查（仅检查变更文件）...${NC}"

    local changed_files=$(get_changed_files)
    if [ -z "$changed_files" ]; then
        echo -e "${YELLOW}⚠️  没有Java文件变更，跳过结构检查${NC}"
        return 0
    fi

    local file_violations=0

    while read -r file; do
        if [ -n "$file" ] && [ -f "$file" ]; then
            # 简单的包结构检查
            if [[ "$file" == *"src/main/java"* ]]; then
                rel_path=$(echo "$file" | sed 's|.*src/main/java/||')
                expected_pkg=$(echo "$rel_path" | sed 's|/[^/]*\.java$||; s|/|.|g')

                package_line=$(grep -m1 "^package " "$file" | head -1 || echo "")
                if [ -n "$package_line" ]; then
                    actual_pkg=$(echo "$package_line" | sed 's/package //; s/;//; s/[[:space:]]//g')

                    if [ "$actual_pkg" != "$expected_pkg" ]; then
                        echo -e "${RED}❌ 包结构不一致：$file${NC}"
                        echo "   声明包: $actual_pkg"
                        echo "   预期包: $expected_pkg"
                        echo "❌ 包结构不一致：$file" >> "$LOG_FILE"
                        echo "   声明包: $actual_pkg" >> "$LOG_FILE"
                        echo "   预期包: $expected_pkg" >> "$LOG_FILE"
                        ((file_violations++))
                    fi
                fi
            fi

            # 检查@Autowired使用
            if grep -q -E "@Autowired" "$file"; then
                echo -e "${RED}❌ 使用@Autowired（应该使用@Resource）：$file${NC}"
                echo "❌ 使用@Autowired：$file" >> "$LOG_FILE"
                ((file_violations++))
            fi

            # 检查@Repository使用
            if grep -q -E "@Repository" "$file"; then
                echo -e "${RED}❌ 使用@Repository（应该使用@Mapper）：$file${NC}"
                echo "❌ 使用@Repository：$file" >> "$LOG_FILE"
                ((file_violations++))
            fi
        fi
    done <<< "$changed_files"

    if [ $file_violations -gt 0 ]; then
        VIOLATION_COUNT=$((VIOLATION_COUNT + file_violations))
        echo -e "${RED}❌ 快速结构检查失败：发现 $file_violations 个违规${NC}"
        return 1
    else
        echo -e "${GREEN}✅ 快速结构检查通过${NC}"
        return 0
    fi
}

# P0检查：快速编译检查
quick_p0_check() {
    echo -e "${BLUE}🔍 执行P0快速编译检查...${NC}"

    # 检查是否有microservices-common的变更
    local changed_common=$(git diff --cached --name-only | grep -E "microservices-common" || true)

    if [ -n "$changed_common" ]; then
        echo -e "${YELLOW}⚠️  检测到microservices-common变更，执行完整编译检查${NC}"
        cd microservices
        if mvn compile -pl microservices-common -am -q -DskipTests; then
            echo -e "${GREEN}✅ P0编译检查通过${NC}"
        else
            echo -e "${RED}❌ P0编译检查失败${NC}"
            echo "❌ P0编译检查失败" >> "$LOG_FILE"
            ((VIOLATION_COUNT++))
            cd ..
            return 1
        fi
        cd ..
    else
        echo -e "${GREEN}✅ 无microservices-common变更，跳过P0检查${NC}"
    fi

    return 0
}

# 执行检查序列
echo "开始Pre-commit架构检查..." | tee -a "$LOG_FILE"

# 1. 快速P0检查
if ! quick_p0_check; then
    echo -e "${RED}🚫 Pre-commit检查失败：P0编译检查未通过${NC}"
    echo "请修复编译问题后重新提交" | tee -a "$LOG_FILE"
    echo ""
    echo "详细日志见: $LOG_FILE"
    exit 1
fi

# 2. 快速结构检查
quick_structure_check

# 3. 如果有较多变更，执行完整检查
changed_count=$(get_changed_files | wc -l)
if [ "$changed_count" -gt 5 ]; then
    echo -e "${YELLOW}⚠️  变更文件较多（$changed_count个），执行完整检查...${NC}"

    # 执行完整检查脚本
    run_check "P1结构一致性" ".github/scripts/check-structure-consistency.sh"
    run_check "P2 API契约" ".github/scripts/check-api-contract.sh"
    run_check "P4工程治理" ".github/scripts/check-governance.sh"
fi

# 生成检查结果报告
echo "======================================" >> "$LOG_FILE"
echo "检查完成时间: $(date -u +"%Y-%m-%d %H:%M:%S UTC")" >> "$LOG_FILE"
echo "总问题数量: $VIOLATION_COUNT" >> "$LOG_FILE"

# 输出结果
echo ""
echo -e "${BLUE}📊 Pre-commit检查结果总结：${NC}"
echo -e "🔍 检查文件: $changed_count 个Java文件"
echo -e "❌ 发现问题: $VIOLATION_COUNT 个"

if [ $VIOLATION_COUNT -gt 0 ]; then
    echo ""
    echo -e "${RED}🚫 Pre-commit检查失败！${NC}"
    echo -e "${RED}请修复上述问题后重新提交${NC}"
    echo ""
    echo -e "${YELLOW}💡 提示：${NC}"
    echo -e "  1. 查看详细日志: cat $LOG_FILE"
    echo -e "  2. 使用 'git commit --no-verify' 跳过检查（不推荐）"
    echo ""
    exit 1
else
    echo ""
    echo -e "${GREEN}🎉 Pre-commit检查通过！${NC}"
    echo -e "${GREEN}代码符合IOE-DREAM架构规范${NC}"
    echo ""
    exit 0
fi