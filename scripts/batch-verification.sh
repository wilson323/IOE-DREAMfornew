#!/bin/bash

# =============================================================================
# IOE-DREAM 批量验证脚本
# 功能: 快速验证项目全局一致性和质量指标
# 作者: System Optimization Specialist
# 版本: v1.0.0
# 创建时间: 2025-11-18
# =============================================================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m'

# 项目根目录
PROJECT_ROOT="D:/IOE-DREAM/smart-admin-api-java17-springboot3"

# 打印标题
print_header() {
    echo -e "${BLUE}============================================================================${NC}"
    echo -e "${BLUE}🔍 IOE-DREAM 批量验证${NC}"
    echo -e "${BLUE}============================================================================${NC}"
    echo -e "${CYAN}验证时间: $(date)${NC}"
    echo -e "${CYAN}项目路径: $PROJECT_ROOT${NC}"
    echo -e "${BLUE}============================================================================${NC}"
}

# 计数函数
count_files() {
    local pattern=$1
    find "$PROJECT_ROOT" -name "*.java" -exec grep -l "$pattern" {} \; 2>/dev/null | wc -l
}

# 1. 包结构一致性验证
verify_package_consistency() {
    echo -e "${YELLOW}📦 1. 包结构一致性验证${NC}"

    local annotation_count=$(count_files "annoation")
    local javax_count=$(count_files "javax\.")
    local autowired_count=$(count_files "@Autowired")

    local package_score=$((100 - (annotation_count + javax_count + autowired_count) * 2))

    echo -e "   - 包名错误(annoation): ${RED}$annotation_count${NC} 个文件"
    echo -e "   - Jakarta未迁移(javax): ${RED}$javax_count${NC} 个文件"
    echo -e "   - 依赖注入不统一(@Autowired): ${RED}$autowired_count${NC} 个文件"
    echo -e "   - ${CYAN}包结构一致性评分: $package_score%${NC}"

    if [ $package_score -eq 100 ]; then
        echo -e "   - ${GREEN}✅ 包结构完全一致${NC}"
    elif [ $package_score -ge 90 ]; then
        echo -e "   - ${YELLOW}⚠️ 包结构基本一致，少量问题待修复${NC}"
    else
        echo -e "   - ${RED}❌ 包结构存在严重不一致问题${NC}"
    fi
}

# 2. 编译状态验证
verify_compilation_status() {
    echo -e "${YELLOW}🔧 2. 编译状态验证${NC}"

    cd "$PROJECT_ROOT" > /dev/null 2>&1 || {
        echo -e "${RED}❌ 无法访问项目目录${NC}"
        return 1
    }

    echo -e "   - ${CYAN}执行Maven编译测试...${NC}"

    if timeout 120 mvn clean compile -q > /dev/null 2>&1; then
        echo -e "   - ${GREEN}✅ 编译成功${NC}"
        local compilation_score=100
    else
        echo -e "   - ${RED}❌ 编译失败${NC}"
        local compilation_score=0
    fi

    echo -e "   - ${CYAN}编译状态评分: $compilation_score%${NC}"
}

# 3. 代码质量快速检查
verify_code_quality() {
    echo -e "${YELLOW}📊 3. 代码质量快速检查${NC}"

    # 检查空包
    local empty_packages=$(find "$PROJECT_ROOT/src" -type d -empty | wc -l)

    # 检查重复文件
    local duplicate_files=$(find "$PROJECT_ROOT" -name "*.java" -exec basename {} \; | sort | uniq -d | wc -l)
    local total_files=$(find "$PROJECT_ROOT" -name "*.java" | wc -l)

    local duplicate_rate=0
    if [ $total_files -gt 0 ]; then
        duplicate_rate=$(((total_files - duplicate_files) * 100 / total_files))
    fi

    echo -e "   - 总Java文件: $total_files 个"
    echo -e "   - 唯一命名文件: $duplicate_files 个"
    echo -e "   - 重复文件率: ${RED}$(100 - duplicate_rate)%${NC}"
    echo -e "   - 空包数量: ${RED}$empty_packages${NC} 个"

    # 质量评分
    local quality_score=100
    quality_score=$((quality_score - empty_packages))
    quality_score=$((quality_score - (100 - duplicate_rate)))

    echo -e "   - ${CYAN}代码质量评分: ${quality_score}%${NC}"

    if [ $quality_score -ge 90 ]; then
        echo -e "   - ${GREEN}✅ 代码质量优秀${NC}"
    elif [ $quality_score -ge 80 ]; then
        echo -e "   - ${YELLOW}⚠️ 代码质量良好，可进一步优化${NC}"
    else
        echo -e "   - ${RED}❌ 代码质量需要改进${NC}"
    fi
}

# 4. 架构合规性检查
verify_architecture_compliance() {
    echo -e "${YELLOW}🏗️ 4. 架构合规性检查${NC}"

    # 检查四层架构文件
    local controller_count=$(find "$PROJECT_ROOT" -name "*Controller.java" | wc -l)
    local service_count=$(find "$PROJECT_ROOT" -name "*Service*.java" | wc -l)
    local manager_count=$(find "$PROJECT_ROOT" -name "*Manager.java" | wc -l)
    local dao_count=$(find "$PROJECT_ROOT" -name "*Dao.java" | wc -l)

    echo -e "   - Controller层文件: $controller_count 个"
    echo -e "   - Service层文件: $service_count 个"
    echo -e "   - Manager层文件: $manager_count 个"
    echo -e "   - DAO层文件: $dao_count 个"

    # 检查架构完整性
    local architecture_score=25  # 基础分
    [ $controller_count -gt 0 ] && architecture_score=$((architecture_score + 25))
    [ $service_count -gt 0 ] && architecture_score=$((architecture_score + 25))
    [ $manager_count -gt 0 ] && architecture_score=$((architecture_score + 12))
    [ $dao_count -gt 0 ] && architecture_score=$((architecture_score + 13))

    echo -e "   - ${CYAN}架构完整性评分: $architecture_score/100${NC}"

    # 检查跨层访问违规
    local violations=$(count_files "@Resource.*Dao")
    echo -e "   - 跨层访问违规: ${RED}$violations${NC} 个文件"

    if [ $architecture_score -ge 90 ]; then
        echo -e "   - ${GREEN}✅ 架构完整性优秀${NC}"
    elif [ $architecture_score -ge 70 ]; then
        echo -e "   - ${YELLOW}⚠️ 架构基本完整${NC}"
    else
        echo -e "   - ${RED}❌ 架构不完整${NC}"
    fi

    if [ $violations -eq 0 ]; then
        echo -e "   - ${GREEN}✅ 无跨层访问违规${NC}"
    else
        echo -e "   - ${YELLOW}⚠️ 存在跨层访问违规，需要修复${NC}"
    fi
}

# 5. 依赖管理检查
verify_dependency_management() {
    echo -e "${YELLOW}📋 5. 依赖管理检查${NC}"

    local pom_file="$PROJECT_ROOT/pom.xml"
    if [ -f "$pom_file" ]; then
        # 检查重复依赖
        local duplicate_deps=$(grep -c "<dependency>" "$pom_file" 2>/dev/null || echo "0")

        # 检查版本统一性
        local spring_boot_versions=$(grep -o "spring-boot-starter[^<]*" "$pom_file" | sed 's/<[^>]*>//g' | sort -u | wc -l)

        echo -e "   - 依赖项总数: $duplicate_deps"
        echo -e "   - Spring Boot版本种类: $spring_boot_versions"

        local dep_score=100
        [ $spring_boot_versions -eq 1 ] && dep_score=100
        [ $spring_boot_versions -gt 1 ] && dep_score=80

        echo -e "   - ${CYAN}依赖管理评分: $dep_score%${NC}"

        if [ $dep_score -eq 100 ]; then
            echo -e "   - ${GREEN}✅ 依赖管理优秀${NC}"
        else
            echo -e "   - ${YELLOW}⚠️ 依赖版本不统一${NC}"
        fi
    else
        echo -e "   - ${RED}❌ 未找到pom.xml文件${NC}"
    fi
}

# 6. 生成验证报告
generate_verification_report() {
    local report_file="./reports/verification_report_$(date +%Y%m%d_%H%M%S).md"

    mkdir -p ./reports

    cat > "$report_file" << EOF
# IOE-DREAM 项目验证报告

> **验证时间**: $(date '+%Y-%m-%d %H:%M:%S')
> **验证版本**: v1.0.0
> **验证引擎**: System Optimization Specialist

## 📊 验证结果摘要

### 包结构一致性
- 包名错误: $annotation_count 个文件
- Jakarta未迁移: $javax_count 个文件
- 依赖注入不统一: $autowired_count 个文件
- **一致性评分**: $package_score%

### 编译状态
- 编译结果: $([ $compilation_score -eq 100 ] && echo "✅ 成功" || echo "❌ 失败")
- **评分**: $compilation_score%

### 代码质量
- 总文件数: $total_files
- 重复文件率: $(100 - duplicate_rate)%
- 空包数量: $empty_packages
- **质量评分**: ${quality_score}%

### 架构合规性
- Controller层: $controller_count 个文件
- Service层: $service_count 个文件
- Manager层: $manager_count 个文件
- DAO层: $dao_count 个文件
- **完整性评分**: $architecture_score/100
- 跨层违规: $violations 个

### 依赖管理
- 依赖项总数: $duplicate_deps
- 版本统一性: $dep_score%
- Spring Boot版本: $spring_boot_versions 种

## 🎯 整体评估

EOF

    # 计算总分
    local overall_score=$((package_score * 25 + compilation_score * 25 + quality_score * 25 + architecture_score * 25))
    overall_score=$((overall_score / 100))

    echo -e "${GREEN}✅ 验证报告已生成: $report_file${NC}"

    # 添加整体评估
    cat >> "$report_file" << EOF

| 维度 | 评分 | 权重 | 加权分数 |
|------|------|------|----------|
| 包结构一致性 | $package_score% | 25% | $((package_score * 25 / 100)) |
| 编译状态 | $compilation_score% | 25% | $((compilation_score * 25 / 100)) |
| 代码质量 | ${quality_score}% | 25% | $((quality_score * 25 / 100)) |
| 架构合规性 | $architecture_score/100 | 25% | $((architecture_score / 4)) |
| **总体评分** | **$overall_score/100** | **100%** | **$overall_score/100** |

### 🏆 质量评级
EOF

    if [ $overall_score -ge 90 ]; then
        echo -e "🏆 **A级 (优秀)** - 项目质量极高，技术债务极少" >> "$report_file"
    elif [ $overall_score -ge 80 ]; then
        echo -e "🥇 **B级 (良好)** - 项目质量良好，存在少量技术债务" >> "$report_file"
    elif [ $overall_score -ge 70 ]; then
        echo -e "🥈 **C级 (一般)** - 项目质量一般，需要关注技术债务" >> "$report_file"
    elif [ $overall_score -ge 60 ]; then
        echo -e "🥉 **D级 (需改进)** - 项目质量需要改进，技术债务较重" >> "$report_file"
    else
        echo -e "💣 **E级 (严重)** - 项目质量严重，技术债务极重，需要立即处理" >> "$report_file"
    fi

    echo "" >> "$report_file"
}

# 7. 生成最终总结
generate_final_summary() {
    echo ""
    echo -e "${BLUE}============================================================================${NC}"
    echo -e "${BLUE}📊 验证结果汇总${NC}"
    echo -e "${BLUE}============================================================================${NC}"
    echo -e "${CYAN}包结构一致性: $package_score%${NC}"
    echo -e "${CYAN}编译状态: $compilation_score%${NC}"
    echo -e "${CYAN}代码质量: ${quality_score}%${NC}"
    echo -e "${CYAN}架构合规性: $architecture_score/100${NC}"
    echo -e "${CYAN}依赖管理: $dep_score%${NC}"
    echo -e "${CYAN}总体评分: $overall_score/100${NC}"
    echo -e "${BLUE}============================================================================${NC}"

    # 给出建议
    if [ $overall_score -ge 85 ]; then
        echo -e "${GREEN}🎉 恭喜！项目质量优秀，可以继续开发${NC}"
    elif [ $overall_score -ge 70 ]; then
        echo -e "${YELLOW}💡 项目质量良好，建议继续优化${NC}"
    else
        echo -e "${RED}⚠️ 项目质量需要重点改进，建议执行系统性优化${NC}"
    fi

    echo -e ""
    echo -e "${PURPLE}💡 建议执行 'scripts/global-consistency-fix.sh' 进行系统性修复${NC}"
}

# 获取验证统计（从上述验证中获取）
annotation_count=$(count_files "annoation")
javax_count=$(count_files "javax\.")
autowired_count=$(count_files "@Autowired")

total_files=$(find "$PROJECT_ROOT" -name "*.java" | wc -l)
duplicate_files=$(find "$PROJECT_ROOT" -name "*.java" -exec basename {} \; | sort | uniq -d | wc -l)
empty_packages=$(find "$PROJECT_ROOT/src" -type d -empty | wc -l)

controller_count=$(find "$PROJECT_ROOT" -name "*Controller.java" | wc -l)
service_count=$(find "$PROJECT_ROOT" -name "*Service*.java" | wc -l)
manager_count=$(find "$PROJECT_ROOT" -name "*Manager.java" | wc -l)
dao_count=$(find "$PROJECT_ROOT" -name "*Dao.java" | wc -l)

violations=$(count_files "@Resource.*Dao")

quality_score=100
quality_score=$((quality_score - empty_packages))
quality_score=$((quality_score - (total_files - duplicate_files) * 100 / total_files))

architecture_score=25
[ $controller_count -gt 0 ] && architecture_score=$((architecture_score + 25))
[ $service_count -gt 0 ] && architecture_score=$((architecture_score + 25))
[ $manager_count -gt 0 ] && architecture_score=$((architecture_score + 12))
[ $dao_count -gt 0 ] && architecture_score=$((architecture_score + 13))

# 主执行流程
main() {
    print_header
    verify_package_consistency
    verify_compilation_status
    verify_code_quality
    verify_architecture_compliance
    verify_dependency_management
    generate_verification_report
    generate_final_summary
}

# 执行主函数
main "$@"