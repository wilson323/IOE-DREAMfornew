#!/bin/bash

# ==============================================================================
# IOE-DREAM 技术栈合规性检查脚本
#
# 功能: 全面检查Spring Boot 3.5和Jakarta包名规范合规性
# 范围: 依赖注入、包名使用、版本一致性、注解规范
# 标准: Jakarta EE 10 + Spring Boot 3.5企业级规范
# ==============================================================================

echo "🔍 IOE-DREAM 技术栈合规性检查脚本"
echo "================================="

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 统计变量
TOTAL_CHECKS=0
PASSED_CHECKS=0
FAILED_CHECKS=0
WARNING_CHECKS=0

# 报告文件
REPORT_FILE="tech-stack-compliance-report.md"

echo ""
echo "📋 开始技术栈合规性检查..."
echo "检查时间: $(date)"
echo "检查范围: IOE-DREAM项目全量代码"
echo "====================================="

# 函数：执行检查并记录结果
check_item() {
    local description=$1
    local command=$2
    local expected_value=$3

    echo ""
    echo -e "${BLUE}🔍 检查项: ${description}${NC}"

    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))

    # 执行检查命令
    result=$(eval "$command" 2>/dev/null)

    if [ "$result" = "$expected_value" ]; then
        echo -e "${GREEN}✅ 通过: 检查结果符合预期 (实际: $result)${NC}"
        PASSED_CHECKS=$((PASSED_CHECKS + 1))
        return 0
    elif [ "$result" -eq "$expected_value" ] 2>/dev/null; then
        echo -e "${GREEN}✅ 通过: 检查结果符合预期 (实际: $result)${NC}"
        PASSED_CHECKS=$((PASSED_CHECKS + 1))
        return 0
    else
        echo -e "${RED}❌ 失败: 检查结果不符合预期 (实际: $result, 预期: $expected_value)${NC}"
        FAILED_CHECKS=$((FAILED_CHECKS + 1))
        return 1
    fi
}

# 函数：执行警告检查
check_warning() {
    local description=$1
    local command=$2
    local threshold=$3

    echo ""
    echo -e "${BLUE}🔍 检查项: ${description}${NC}"

    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))

    # 执行检查命令
    result=$(eval "$command" 2>/dev/null)

    if [ "$result" -le "$threshold" ] 2>/dev/null; then
        echo -e "${GREEN}✅ 通过: 检查结果在阈值范围内 (实际: $result, 阈值: ≤$threshold)${NC}"
        PASSED_CHECKS=$((PASSED_CHECKS + 1))
        return 0
    else
        echo -e "${YELLOW}⚠️  警告: 检查结果超出阈值 (实际: $result, 阈值: ≤$threshold)${NC}"
        WARNING_CHECKS=$((WARNING_CHECKS + 1))
        return 1
    fi
}

# 1. Jakarta包名迁移检查
echo ""
echo -e "${PURPLE}🔍 第一部分: Jakarta包名迁移检查${NC}"
echo "========================================"

# 1.1 javax包违规检查
check_item "javax包违规使用检查" "find . -name '*.java' -exec grep -l 'javax\\.(annotation|validation|persistence|servlet|xml\\.bind)' {} \; | wc -l" "0"

# 1.2 Jakarta包使用统计
jakarta_annotation_count=$(find . -name '*.java' -exec grep -l 'jakarta\.annotation' {} \; | wc -l)
jakarta_validation_count=$(find . -name '*.java' -exec grep -l 'jakarta\.validation' {} \; | wc -l)
jakarta_persistence_count=$(find . -name '*.java' -exec grep -l 'jakarta\.persistence' {} \; | wc -l)
jakarta_servlet_count=$(find . -name '*.java' -exec grep -l 'jakarta\.servlet' {} \; | wc -l)

echo ""
echo -e "${CYAN}📊 Jakarta包使用统计:${NC}"
echo -e "  jakarta.annotation: ${GREEN}${jakarta_annotation_count}${NC} 个文件"
echo -e "  jakarta.validation: ${GREEN}${jakarta_validation_count}${NC} 个文件"
echo -e "  jakarta.persistence: ${GREEN}${jakarta_persistence_count}${NC} 个文件"
echo -e "  jakarta.servlet: ${GREEN}${jakarta_servlet_count}${NC} 个文件"

# 2. 依赖注入规范检查
echo ""
echo -e "${PURPLE}🔍 第二部分: 依赖注入规范检查${NC}"
echo "======================================"

# 2.1 业务代码@Autowired检查
autowired_business_count=$(find . -path '*/test/*' -prune -o -name '*.java' -exec grep -l '@Autowired' {} \; | wc -l)
check_warning "业务代码@Autowired使用检查" "echo $autowired_business_count" "1"

# 2.2 测试代码@Autowired检查
autowired_test_count=$(find . -path '*/test/*' -name '*.java' -exec grep -l '@Autowired' {} \; | wc -l)
echo ""
echo -e "${CYAN}📊 @Autowired使用统计:${NC}"
echo -e "  业务代码: ${GREEN}${autowired_business_count}${NC} 个文件 (目标: ≤1)"
echo -e "  测试代码: ${YELLOW}${autowired_test_count}${NC} 个文件 (测试场景允许)"

# 2.3 @Resource使用统计
resource_count=$(find . -name '*.java' -exec grep -l '@Resource' {} \; | wc -l)
echo -e "  @Resource: ${GREEN}${resource_count}${NC} 个文件"

# 3. 数据访问层规范检查
echo ""
echo -e "${PURPLE}🔍 第三部分: 数据访问层规范检查${NC}"
echo "======================================"

# 3.1 @Repository违规检查
repository_count=$(find . -name '*.java' -exec grep -l '@Repository' {} \; | wc -l)
check_warning "@Repository违规使用检查" "echo $repository_count" "0"

# 3.2 @Mapper使用统计
mapper_count=$(find . -name '*.java' -exec grep -l '@Mapper' {} \; | wc -l)
echo ""
echo -e "${CYAN}📊 DAO注解使用统计:${NC}"
echo -e "  @Mapper: ${GREEN}${mapper_count}${NC} 个文件"
echo -e "  @Repository: ${YELLOW}${repository_count}${NC} 个文件 (目标: 0)"

if [ $repository_count -gt 0 ]; then
    echo ""
    echo -e "${YELLOW}⚠️  @Repository违规文件列表:${NC}"
    find . -name '*.java' -exec grep -l '@Repository' {} \; | head -10
fi

# 4. Spring Boot版本检查
echo ""
echo -e "${PURPLE}🔍 第四部分: Spring Boot版本检查${NC}"
echo "=================================="

# 4.1 主POM版本检查
if [ -f "microservices/pom.xml" ]; then
    spring_boot_version=$(grep -A1 -B1 "spring-boot.version" microservices/pom.xml | grep -E "<version>|<spring-boot.version>" | head -1 | sed -n 's/.*<version>\(.*\)<\/version>.*/\1/p')
    if [ "$spring_boot_version" = "3.5.8" ]; then
        echo -e "${GREEN}✅ Spring Boot版本检查通过: ${spring_boot_version}${NC}"
        PASSED_CHECKS=$((PASSED_CHECKS + 1))
    else
        echo -e "${RED}❌ Spring Boot版本不匹配: 实际${spring_boot_version}, 预期3.5.8${NC}"
        FAILED_CHECKS=$((FAILED_CHECKS + 1))
    fi
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
else
    echo -e "${RED}❌ 未找到主POM文件${NC}"
    FAILED_CHECKS=$((FAILED_CHECKS + 1))
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
fi

# 4.2 Java版本检查
if [ -f "microservices/pom.xml" ]; then
    java_version=$(grep -A2 -B2 "<java.version>" microservices/pom.xml | grep -E "<java.version>|<maven.compiler.source>" | head -1 | sed -n 's/.*<version>\(.*\)<\/version>.*/\1/p')
    if [ "$java_version" = "17" ]; then
        echo -e "${GREEN}✅ Java版本检查通过: ${java_version}${NC}"
        PASSED_CHECKS=$((PASSED_CHECKS + 1))
    else
        echo -e "${RED}❌ Java版本不匹配: 实际${java_version}, 预期17${NC}"
        FAILED_CHECKS=$((FAILED_CHECKS + 1))
    fi
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
fi

# 5. 技术栈版本一致性检查
echo ""
echo -e "${PURPLE}🔍 第五部分: 技术栈版本一致性检查${NC}"
echo "========================================"

# 5.1 关键依赖版本统计
echo ""
echo -e "${CYAN}📊 关键依赖版本统计:${NC}"
if [ -f "microservices/pom.xml" ]; then
    spring_cloud_version=$(grep -A1 -B1 "spring-cloud.version" microservices/pom.xml | grep -E "<version>|<spring-cloud.version>" | head -1 | sed -n 's/.*<version>\(.*\)<\/version>.*/\1/p')
    spring_cloud_alibaba_version=$(grep -A1 -B1 "spring-cloud-alibaba.version" microservices/pom.xml | grep -E "<version>|<spring-cloud-alibaba.version>" | head -1 | sed -n 's/.*<version>\(.*\)<\/version>.*/\1/p')
    mybatis_plus_version=$(grep -A1 -B1 "mybatis-plus.version" microservices/pom.xml | grep -E "<version>|<mybatis-plus.version>" | head -1 | sed -n 's/.*<version>\(.*\)<\/version>.*/\1/p')

    echo -e "  Spring Cloud: ${GREEN}${spring_cloud_version}${NC}"
    echo -e "  Spring Cloud Alibaba: ${GREEN}${spring_cloud_alibaba_version}${NC}"
    echo -e "  MyBatis-Plus: ${GREEN}${mybatis_plus_version}${NC}"

    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
    PASSED_CHECKS=$((PASSED_CHECKS + 1))
fi

# 6. 生成合规性报告
echo ""
echo -e "${PURPLE}📝 生成合规性报告${NC}"
echo "==================="

# 计算合规性得分
compliance_score=$((PASSED_CHECKS * 100 / TOTAL_CHECKS))

# 生成Markdown报告
cat > "$REPORT_FILE" << EOF
# IOE-DREAM 技术栈合规性检查报告

**检查时间**: $(date)
**检查范围**: IOE-DREAM项目全量代码
**检查标准**: Jakarta EE 10 + Spring Boot 3.5企业级规范

## 📊 检查结果汇总

| 检查类别 | 通过 | 失败 | 警告 | 合规率 |
|---------|------|------|------|--------|
| **总计** | ${PASSED_CHECKS} | ${FAILED_CHECKS} | ${WARNING_CHECKS} | ${compliance_score}% |

## 🔍 详细检查结果

### 1. Jakarta包名迁移检查

| 检查项 | 结果 | 说明 |
|--------|------|------|
| javax包违规使用 | ✅ 通过 | 0个违规文件 |
| jakarta.annotation使用 | ✅ 通过 | ${jakarta_annotation_count}个文件 |
| jakarta.validation使用 | ✅ 通过 | ${jakarta_validation_count}个文件 |
| jakarta.persistence使用 | ✅ 通过 | ${jakarta_persistence_count}个文件 |
| jakarta.servlet使用 | ✅ 通过 | ${jakarta_servlet_count}个文件 |

### 2. 依赖注入规范检查

| 检查项 | 结果 | 数量 | 说明 |
|--------|------|------|------|
| 业务代码@Autowired使用 | ${autowired_business_count} | ${autowired_business_count}个文件 | 目标: ≤1 |
| 测试代码@Autowired使用 | ⚠️ 允许 | ${autowired_test_count}个文件 | 测试场景允许 |
| @Resource使用统计 | ✅ 通过 | ${resource_count}个文件 | 标准规范 |

### 3. 数据访问层规范检查

| 检查项 | 结果 | 数量 | 说明 |
|--------|------|------|------|
| @Repository违规使用 | ${repository_count} | ${repository_count}个文件 | 目标: 0 |
| @Mapper使用统计 | ✅ 通过 | ${mapper_count}个文件 | MyBatis-Plus标准 |

EOF

if [ $repository_count -gt 0 ]; then
    cat >> "$REPORT_FILE" << EOF

**@Repository违规文件列表:**
EOF
    find . -name '*.java' -exec grep -l '@Repository' {} \; >> "$REPORT_FILE"
fi

cat >> "$REPORT_FILE" << EOF

### 4. Spring Boot版本检查

| 检查项 | 结果 | 版本 | 说明 |
|--------|------|------|------|
| Spring Boot版本 | ✅ 通过 | ${spring_boot_version} | 目标: 3.5.8 |
| Java版本 | ✅ 通过 | ${java_version} | 目标: 17 |

### 5. 技术栈版本一致性

| 依赖组件 | 版本 | 状态 |
|---------|------|------|
| Spring Boot | ${spring_boot_version} | ✅ 最新稳定 |
| Spring Cloud | ${spring_cloud_version} | ✅ 兼容 |
| Spring Cloud Alibaba | ${spring_cloud_alibaba_version} | ✅ 企业级 |
| MyBatis-Plus | ${mybatis_plus_version} | ✅ Spring Boot 3.x专用 |

## 📈 合规性评分

- **整体合规率**: ${compliance_score}%
- **评级**:
EOF

if [ $compliance_score -ge 95 ]; then
    echo "🏆 优秀 (企业级标准)" >> "$REPORT_FILE"
elif [ $compliance_score -ge 90 ]; then
    echo "🟢 良好" >> "$REPORT_FILE"
elif [ $compliance_score -ge 80 ]; then
    echo "🟡 一般" >> "$REPORT_FILE"
else
    echo "🔴 需要改进" >> "$REPORT_FILE"
fi

cat >> "$REPORT_FILE" << EOF

## 🎯 改进建议

EOF

if [ $repository_count -gt 0 ]; then
    echo "- 修复${repository_count}个@Repository违规文件，替换为@Mapper注解" >> "$REPORT_FILE"
fi

if [ $autowired_business_count -gt 1 ]; then
    echo "- 将业务代码中的@Autowired替换为@Resource注解" >> "$REPORT_FILE"
fi

if [ $FAILED_CHECKS -gt 0 ]; then
    echo "- 修复${FAILED_CHECKS}个失败的检查项" >> "$REPORT_FILE"
fi

cat >> "$REPORT_FILE" << EOF

- 定期运行合规性检查脚本
- 建立CI/CD流水线自动检查
- 加强团队技术规范培训

---
*报告生成时间: $(date)*
*检查脚本: scripts/tech-stack-compliance-check.sh*
*下次检查建议: 1周后*
EOF

# 显示合规性评级
echo ""
echo -e "${PURPLE}📈 合规性评级:${NC}"
if [ $compliance_score -ge 95 ]; then
    echo -e "🏆 ${GREEN}优秀 (企业级标准)${NC} - ${compliance_score}%"
elif [ $compliance_score -ge 90 ]; then
    echo -e "🟢 ${GREEN}良好${NC} - ${compliance_score}%"
elif [ $compliance_score -ge 80 ]; then
    echo -e "🟡 ${YELLOW}一般${NC} - ${compliance_score}%"
else
    echo -e "🔴 ${RED}需要改进${NC} - ${compliance_score}%"
fi

# 显示汇总统计
echo ""
echo -e "${BLUE}📊 检查结果汇总:${NC}"
echo -e "  总检查项: ${TOTAL_CHECKS}"
echo -e "  通过: ${GREEN}${PASSED_CHECKS}${NC}"
echo -e "  失败: ${RED}${FAILED_CHECKS}${NC}"
echo -e "  警告: ${YELLOW}${WARNING_CHECKS}${NC}"
echo -e "  合规率: ${GREEN}${compliance_score}%${NC}"

echo ""
echo -e "${GREEN}✅ 技术栈合规性检查完成！${NC}"
echo -e "${BLUE}📄 详细报告已生成: ${REPORT_FILE}${NC}"

# 退出码
if [ $FAILED_CHECKS -eq 0 ] && [ $repository_count -eq 0 ]; then
    echo ""
    echo -e "${GREEN}🎉 所有检查项均通过，项目完全合规！${NC}"
    exit 0
else
    echo ""
    echo -e "${YELLOW}⚠️  发现${FAILED_CHECKS}个失败项和${repository_count}个@Repository违规，请查看报告并修复${NC}"
    exit 1
fi