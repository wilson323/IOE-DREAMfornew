#!/bin/bash
###############################################################################
# Spring Boot 3.5 + Jakarta规范快速检查脚本
#
# 用途: 快速验证技术栈一致性
# 作者: Spring Boot 3.5 + Jakarta规范守护专家
# 日期: 2025-12-26
###############################################################################

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 计数器
VIOLATIONS=0
WARNINGS=0
PASSES=0

# 打印函数
print_header() {
    echo -e "\n${BLUE}=============================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}=============================================${NC}\n"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
    ((PASSES++))
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
    ((VIOLATIONS++))
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
    ((WARNINGS++))
}

###############################################################################
# 1. 检查Spring Boot版本一致性
###############################################################################
print_header "步骤1: 检查Spring Boot版本一致性"

BOOT_VERSION=$(grep -oP '<spring-boot.version>\K[^<]+' microservices/pom.xml | head -1)
if [ "$BOOT_VERSION" = "3.5.8" ]; then
    print_success "Spring Boot版本: $BOOT_VERSION (符合要求)"
else
    print_error "Spring Boot版本: $BOOT_VERSION (应为3.5.8)"
fi

###############################################################################
# 2. 检查javax.*违规使用
###############################################################################
print_header "步骤2: 检查javax.*违规使用"

JAVAX_COUNT=$(find microservices/ -name "*.java" -type f -exec grep -l "import javax\.(annotation|validation|persistence|servlet|xml\.bind)\." {} \; 2>/dev/null | wc -l)

if [ "$JAVAX_COUNT" -eq 0 ]; then
    print_success "javax.*违规使用: 0次 (符合要求)"
else
    print_error "javax.*违规使用: $JAVAX_COUNT次文件"
    echo "违规文件列表:"
    find microservices/ -name "*.java" -type f -exec grep -l "import javax\.(annotation|validation|persistence|servlet|xml\.bind)\." {} \; 2>/dev/null | head -10
fi

###############################################################################
# 3. 检查Jakarta.*正确使用
###############################################################################
print_header "步骤3: 检查Jakarta.*正确使用"

JAKARTA_RESOURCE_COUNT=$(find microservices/ -name "*.java" -type f -exec grep -h "import jakarta.annotation.Resource" {} \; 2>/dev/null | wc -l)
if [ "$JAKARTA_RESOURCE_COUNT" -gt 0 ]; then
    print_success "jakarta.annotation.Resource使用: $JAKARTA_RESOURCE_COUNT次"
else
    print_warning "jakarta.annotation.Resource使用: 0次 (可能存在未迁移的@Autowired)"
fi

###############################################################################
# 4. 检查依赖注入注解规范
###############################################################################
print_header "步骤4: 检查依赖注入注解规范"

AUTOWIRED_COUNT=$(find microservices/ -name "*.java" -type f -exec grep -h "@Autowired" {} \; 2>/dev/null | wc -l)
RESOURCE_COUNT=$(find microservices/ -name "*.java" -type f -exec grep -h "@Resource" {} \; 2>/dev/null | wc -l)
TOTAL_INJECTION=$((AUTOWIRED_COUNT + RESOURCE_COUNT))

if [ "$TOTAL_INJECTION" -gt 0 ]; then
    RESOURCE_PERCENTAGE=$(awk "BEGIN {printf \"%.1f\", ($RESOURCE_COUNT / $TOTAL_INJECTION) * 100}")
    print_success "依赖注入统计: @Resource $RESOURCE_COUNT次 ($RESOURCE_PERCENTAGE%), @Autowired $AUTOWIRED_COUNT次"

    if [ "$AUTOWIRED_COUNT" -gt 0 ]; then
        print_warning "发现$AUTOWIRED_COUNT处@Autowired使用，建议统一为@Resource"
    fi
else
    print_warning "未发现依赖注入注解使用"
fi

###############################################################################
# 5. 检查OpenAPI规范遵循
###############################################################################
print_header "步骤5: 检查OpenAPI 3.0规范遵循"

REQUIRED_MODE_COUNT=$(find microservices/ -name "*.java" -type f -exec grep -h "requiredMode" {} \; 2>/dev/null | wc -l)

if [ "$REQUIRED_MODE_COUNT" -eq 0 ]; then
    print_success "OpenAPI 3.1违规(requiredMode): 0次 (符合要求)"
else
    print_error "OpenAPI 3.1违规(requiredMode): $REQUIRED_MODE_COUNT次"
fi

###############################################################################
# 6. 检查Java版本
###############################################################################
print_header "步骤6: 检查Java版本配置"

JAVA_VERSION=$(grep -oP '<java.version>\K[^<]+' microservices/pom.xml)
MAVEN_COMPILER_SOURCE=$(grep -oP '<maven.compiler.source>\K[^<]+' microservices/pom.xml)

if [ "$JAVA_VERSION" = "17" ] && [ "$MAVEN_COMPILER_SOURCE" = "17" ]; then
    print_success "Java版本配置: $JAVA_VERSION (符合要求)"
else
    print_error "Java版本配置: source=$MAVEN_COMPILER_SOURCE (应为17)"
fi

###############################################################################
# 7. 检查父POM统一性
###############################################################################
print_header "步骤7: 检查父POM统一性"

TOTAL_POMS=$(find microservices/ -name "pom.xml" -type f | wc -l)
UNIFIED_PARENT_POMS=$(find microservices/ -name "pom.xml" -type f -exec grep -l "ioedream-microservices-parent" {} \; | wc -l)

if [ "$TOTAL_POMS" -eq "$UNIFIED_PARENT_POMS" ]; then
    print_success "父POM统一性: $UNIFIED_PARENT_POMS/$TOTAL_POMS (100%)"
else
    print_error "父POM统一性: $UNIFIED_PARENT_POMS/$TOTAL_POMS (应全部使用统一父POM)"
fi

###############################################################################
# 8. 检查Maven模块结构
###############################################################################
print_header "步骤8: 检查Maven模块结构"

MODULE_COUNT=$(grep -h "<module>" microservices/pom.xml | grep -v "^$" | wc -l)
print_success "Maven模块总数: $MODULE_COUNT"

COMMON_MODULES=$(grep -h "<module>" microservices/pom.xml | grep "microservices-common" | wc -l)
SERVICE_MODULES=$(grep -h "<module>" microservices/pom.xml | grep "ioedream-.*-service" | wc -l)

print_success "公共库模块: $COMMON_MODULES个"
print_success "业务微服务: $SERVICE_MODULES个"

###############################################################################
# 9. 生成健康度评分
###############################################################################
print_header "技术栈健康度评分"

# 计算健康度 (简单算法)
HEALTH_SCORE=100

if [ "$JAVAX_COUNT" -gt 0 ]; then
    HEALTH_SCORE=$((HEALTH_SCORE - 20))
fi

if [ "$REQUIRED_MODE_COUNT" -gt 0 ]; then
    HEALTH_SCORE=$((HEALTH_SCORE - 10))
fi

if [ "$AUTOWIRED_COUNT" -gt 50 ]; then
    HEALTH_SCORE=$((HEALTH_SCORE - 5))
fi

if [ "$BOOT_VERSION" != "3.5.8" ]; then
    HEALTH_SCORE=$((HEALTH_SCORE - 30))
fi

if [ "$HEALTH_SCORE" -ge 95 ]; then
    print_success "健康度评分: $HEALTH_SCORE/100 (企业级优秀)"
elif [ "$HEALTH_SCORE" -ge 80 ]; then
    print_warning "健康度评分: $HEALTH_SCORE/100 (良好)"
else
    print_error "健康度评分: $HEALTH_SCORE/100 (需要改进)"
fi

###############################################################################
# 10. 总结报告
###############################################################################
print_header "验证总结"

echo -e "通过项: ${GREEN}$PASSES${NC}"
echo -e "警告项: ${YELLOW}$WARNINGS${NC}"
echo -e "错误项: ${RED}$VIOLATIONS${NC}"

if [ "$VIOLATIONS" -eq 0 ]; then
    echo -e "\n${GREEN}🎉 技术栈验证通过！${NC}\n"
    exit 0
else
    echo -e "\n${RED}❌ 技术栈验证失败，请修复错误项${NC}\n"
    exit 1
fi
