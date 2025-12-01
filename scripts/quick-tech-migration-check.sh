#!/bin/bash

# =============================================================================
# ⚡ 快速技术迁移检查脚本
# 基于404个编译错误修复经验的轻量级检查工具
# 创建日期: 2025-11-22
# 用途: 日常开发快速验证，无需完整编译
# =============================================================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

echo -e "${BLUE}⚡ 快速技术迁移检查 (基于404→10错误修复经验)${NC}"
echo -e "${BLUE}========================================${NC}"

# 项目根目录
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

# 检查结果统计
ISSUES_FOUND=0
PASSED_CHECKS=0

echo -e "${CYAN}项目路径: $PROJECT_ROOT${NC}"
echo -e "${CYAN}检查时间: $(date '+%H:%M:%S')${NC}"

# =============================================================================
# 快速检查函数
# =============================================================================

# 1. Jakarta包名违规检查
check_jakarta_compliance() {
    echo -e "\n${YELLOW}1. Jakarta EE包名合规检查...${NC}"

    local javax_count=$(find "smart-admin-api-java17-springboot3" -name "*.java" -exec grep -l "javax\.(annotation|validation|persistence|servlet)" {} \; 2>/dev/null | wc -l)

    if [ $javax_count -eq 0 ]; then
        echo -e "${GREEN}✅ Jakarta包名合规 (0个违规)${NC}"
        ((PASSED_CHECKS++))
    else
        echo -e "${RED}❌ 发现 $javax_count 个Jakarta包名违规${NC}"
        find "smart-admin-api-java17-springboot3" -name "*.java" -exec grep -l "javax\.(annotation|validation|persistence|servlet)" {} \; 2>/dev/null | head -3 | sed 's/^/   /'
        ((ISSUES_FOUND += javax_count))
    fi
}

# 2. @Autowired使用检查
check_autowired_usage() {
    echo -e "\n${YELLOW}2. 依赖注入合规检查 (@Autowired)...${NC}"

    local autowired_count=$(find "smart-admin-api-java17-springboot3" -name "*.java" -exec grep -l "@Autowired" {} \; 2>/dev/null | wc -l)

    if [ $autowired_count -eq 0 ]; then
        echo -e "${GREEN}✅ 依赖注入合规 (0个@Autowired)${NC}"
        ((PASSED_CHECKS++))
    else
        echo -e "${RED}❌ 发现 $autowired_count 个@Autowired违规${NC}"
        find "smart-admin-api-java17-springboot3" -name "*.java" -exec grep -l "@Autowired" {} \; 2>/dev/null | head -3 | sed 's/^/   /'
        ((ISSUES_FOUND += autowired_count))
    fi
}

# 3. 架构违规检查
check_architecture_compliance() {
    echo -e "\n${YELLOW}3. 架构合规检查 (四层架构)...${NC}"

    local arch_violations=$(grep -r "@Resource.*Dao" --include="*Controller.java" "smart-admin-api-java17-springboot3" 2>/dev/null | wc -l)

    if [ $arch_violations -eq 0 ]; then
        echo -e "${GREEN}✅ 架构合规 (0个违规)${NC}"
        ((PASSED_CHECKS++))
    else
        echo -e "${RED}❌ 发现 $arch_violations 个架构违规${NC}"
        grep -r "@Resource.*Dao" --include="*Controller.java" "smart-admin-api-java17-springboot3" 2>/dev/null | head -3 | sed 's/^/   /'
        ((ISSUES_FOUND += arch_violations))
    fi
}

# 4. 关键文件存在性检查
check_key_files() {
    echo -e "\n${YELLOW}4. 关键文件检查...${NC}"

    local missing_files=0

    # 检查关键的配置文件
    local key_files=(
        "smart-admin-api-java17-springboot3/pom.xml"
        "smart-admin-api-java17-springboot3/sa-base/pom.xml"
        "smart-admin-api-java17-springboot3/sa-admin/pom.xml"
    )

    for file in "${key_files[@]}"; do
        if [ ! -f "$file" ]; then
            echo -e "${RED}❌ 缺少关键文件: $file${NC}"
            ((missing_files++))
        fi
    done

    if [ $missing_files -eq 0 ]; then
        echo -e "${GREEN}✅ 所有关键文件存在${NC}"
        ((PASSED_CHECKS++))
    else
        echo -e "${RED}❌ 发现 $missing_files 个缺失文件${NC}"
        ((ISSUES_FOUND += missing_files))
    fi
}

# 5. 包结构检查
check_package_structure() {
    echo -e "\n${YELLOW}5. 包结构检查...${NC}"

    # 检查关键包结构是否存在
    local structure_issues=0

    local key_packages=(
        "smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin"
        "smart-admin-api-java17-springboot3/sa-base/src/main/java/net/lab1024/sa/base"
    )

    for package in "${key_packages[@]}"; do
        if [ ! -d "$package" ]; then
            echo -e "${RED}❌ 缺少关键包: $package${NC}"
            ((structure_issues++))
        fi
    done

    if [ $structure_issues -eq 0 ]; then
        echo -e "${GREEN}✅ 包结构正常${NC}"
        ((PASSED_CHECKS++))
    else
        echo -e "${RED}❌ 发现 $structure_issues 个包结构问题${NC}"
        ((ISSUES_FOUND += structure_issues))
    fi
}

# 6. 重复类定义检查
check_duplicate_classes() {
    echo -e "\n${YELLOW}6. 重复类定义检查...${NC}"

    local duplicate_count=$(find "smart-admin-api-java17-springboot3" -name "*.java" -exec basename {} .java \; | sort | uniq -d | wc -l)

    if [ $duplicate_count -eq 0 ]; then
        echo -e "${GREEN}✅ 无重复类定义${NC}"
        ((PASSED_CHECKS++))
    else
        echo -e "${YELLOW}⚠️ 发现 $duplicate_count 个重复类名${NC}"
        find "smart-admin-api-java17-springboot3" -name "*.java" -exec basename {} .java \; | sort | uniq -d | head -3 | sed 's/^/   /'
        ((ISSUES_FOUND += duplicate_count))
    fi
}

# 7. System.out使用检查
check_system_out_usage() {
    echo -e "\n${YELLOW}7. 日志规范检查 (System.out.println)...${NC}"

    local systemout_count=$(find "smart-admin-api-java17-springboot3" -name "*.java" -exec grep -l "System\.out\.println" {} \; 2>/dev/null | wc -l)

    if [ $systemout_count -eq 0 ]; then
        echo -e "${GREEN}✅ 无System.out.println使用${NC}"
        ((PASSED_CHECKS++))
    elif [ $systemout_count -le 5 ]; then
        echo -e "${YELLOW}⚠️ 发现 $systemout_count 个System.out.println使用 (建议修复)${NC}"
        ((ISSUES_FOUND += systemout_count))
    else
        echo -e "${RED}❌ 发现 $systemout_count 个System.out.println使用 (过多)${NC}"
        ((ISSUES_FOUND += systemout_count))
    fi
}

# =============================================================================
# 执行检查
# =============================================================================

echo -e "\n${BLUE}开始执行快速检查...${NC}"

check_jakarta_compliance
check_autowired_usage
check_architecture_compliance
check_key_files
check_package_structure
check_duplicate_classes
check_system_out_usage

# =============================================================================
# 结果汇总
# =============================================================================

echo -e "\n${BLUE}========================================${NC}"
echo -e "${BLUE}📊 快速检查结果汇总${NC}"
echo -e "${BLUE}========================================${NC}"
echo -e "检查通过: $PASSED_CHECKS/7"
echo -e "发现问题: $ISSUES_FOUND 个"

if [ $ISSUES_FOUND -eq 0 ]; then
    echo -e "\n${GREEN}🎉 恭喜！快速检查完全通过！${NC}"
    echo -e "${GREEN}✅ 技术迁移合规性: 100%${NC}"
    echo -e "${GREEN}✅ 符合404→10编译错误修复标准${NC}"
    echo -e "${GREEN}✅ 建议继续开发${NC}"
    exit 0
elif [ $ISSUES_FOUND -le 5 ]; then
    echo -e "\n${YELLOW}⚠️ 发现少量问题，建议关注${NC}"
    echo -e "${YELLOW}💡 可以继续开发，但建议尽快修复${NC}"
    exit 0
else
    echo -e "\n${RED}❌ 发现较多问题，建议先修复${NC}"
    echo -e "${RED}🔧 建议执行完整检查: ./scripts/technology-migration-zero-tolerance-check.sh${NC}"
    echo -e "${RED}💡 或调用专家技能: Skill('compilation-error-prevention-specialist')${NC}"
    exit 1
fi