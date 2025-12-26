#!/bin/bash
# IOE-DREAM 技术栈版本统一验证脚本
# 作者：IOE-DREAM架构委员会
# 版本：v1.0.0
# 更新：2025-01-30（企业级统一计划实施）

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

print_header() {
    echo -e "${CYAN}🔧 IOE-DREAM 技术栈版本统一验证脚本${NC}"
    echo -e "${CYAN}=====================================${NC}"
    echo -e "${GREEN}验证时间: $(date '+%Y-%m-%d %H:%M:%S')${NC}"
    echo -e "${GREEN}验证范围: IOE-DREAM项目所有微服务${NC}"
    echo -e "${CYAN}=====================================${NC}"
}

# 技术栈标准版本
declare -A STANDARD_VERSIONS=(
    ["java"]="17"
    ["spring-boot"]="3.5.8"
    ["spring-cloud"]="2025.0.0"
    ["spring-cloud-alibaba"]="2025.0.0.0"
    ["mysql"]="8.0.35"
    ["mybatis-plus"]="3.5.15"
    ["druid"]="1.2.25"
    ["lombok"]="1.18.42"
    ["jackson"]="2.18.2"
    ["jwt"]="0.12.6"
    ["micrometer"]="1.13.6"
    ["caffeine"]="3.1.8"
    ["resilience4j"]="2.1.0"
    ["seata"]="2.0.0"
    ["jakarta-platform"]="10.0.0"
    ["jakarta-servlet"]="6.1.0"
    ["jakarta-validation"]="3.0.2"
)

# 验证结果
TOTAL_MISMATCHES=0

# 获取父POM中的版本
get_parent_version() {
    local component=$1
    if [ -f "microservices/pom.xml" ]; then
        grep "${component}.version" microservices/pom.xml | head -1 | sed 's/.*<'"${component}"'.version>\(.*\)<\/.*>/\1/' | tr -d ' '
    fi
}

# 验证父POM版本
print_header
echo -e "${BLUE}📋 第1项: 父POM技术栈版本验证${NC}"

for component in "${!STANDARD_VERSIONS[@]}"; do
    parent_version=$(get_parent_version "$component")
    standard_version="${STANDARD_VERSIONS[$component]}"

    if [ "$parent_version" = "$standard_version" ]; then
        echo -e "${GREEN}✅ $component: $parent_version (符合标准)${NC}"
    else
        echo -e "${RED}❌ $component: $parent_version (标准: $standard_version)${NC}"
        TOTAL_MISMATCHES=$((TOTAL_MISMATCHES + 1))
    fi
done

# 验证微服务是否正确使用变量
echo -e "\n${BLUE}📋 第2项: 微服务依赖管理验证${NC}"

service_count=0
variable_violations=0
for service_dir in microservices/ioedream-* microservices/microservices-common-*; do
    if [ -d "$service_dir" ]; then
        service_count=$((service_count + 1))
        pom_file="$service_dir/pom.xml"

        if [ -f "$pom_file" ]; then
            # 检查是否有硬编码版本号
            hardcoded_versions=$(grep -E "<version>[0-9]+\.[0-9]+\.[0-9]+</version>" "$pom_file" | grep -v "\${.*\}" | wc -l)

            if [ "$hardcoded_versions" -gt 0 ]; then
                echo -e "${RED}❌ $(basename $service_dir): 发现 $hardcoded_versions 个硬编码版本号${NC}"
                variable_violations=$((variable_violations + 1))
                TOTAL_MISMATCHES=$((TOTAL_MISMATCHES + hardcoded_versions))
            else
                echo -e "${GREEN}✅ $(basename $service_dir): 正确使用变量管理版本${NC}"
            fi
        fi
    fi
done

echo -e "${BLUE}📊 服务统计: $service_count 个微服务${NC}"

# 验证关键组件一致性
echo -e "\n${BLUE}📋 第3项: 关键组件一致性验证${NC}"

# 检查Java版本一致性
java_version_mismatches=0
for service_dir in microservices/ioedream-*; do
    if [ -d "$service_dir" ]; then
        pom_file="$service_dir/pom.xml"
        if [ -f "$pom_file" ]; then
            # 检查Java源版本
            source_version=$(grep "<maven.compiler.source>" "$pom_file" 2>/dev/null | head -1 | sed 's/.*<maven.compiler.source>\(.*\)<\/.*>/\1/' || echo "unknown")
            target_version=$(grep "<maven.compiler.target>" "$pom_file" 2>/dev/null | head -1 | sed 's/.*<maven.compiler.target>\(.*\)<\/.*>/\1/' || echo "unknown")

            if [ "$source_version" != "17" ] || [ "$target_version" != "17" ]; then
                echo -e "${RED}❌ $(basename $service_dir): Java版本不匹配 (source: $source_version, target: $target_version)${NC}"
                java_version_mismatches=$((java_version_mismatches + 1))
            fi
        fi
    fi
done

if [ "$java_version_mismatches" -eq 0 ]; then
    echo -e "${GREEN}✅ 所有服务统一使用Java 17${NC}"
else
    echo -e "${RED}❌ 发现 $java_version_mismatches 个Java版本不匹配${NC}"
fi

TOTAL_MISMATCHES=$((TOTAL_MISMATCHES + java_version_mismatches))

# 检查Spring Boot配置一致性
echo -e "\n${BLUE}📋 第4项: Spring Boot配置一致性验证${NC}"

spring_config_mismatches=0
application_files=$(find microservices -name "application*.yml" -o -name "application*.yaml" -o -name "application*.properties" | grep -v test | wc -l)

if [ "$application_files" -gt 0 ]; then
    echo -e "${BLUE}📁 找到 $application_files 个配置文件${NC}"

    # 检查是否使用了过时的配置
    deprecated_configs=$(find microservices -name "application*.yml" -o -name "application*.yaml" -o -name "application*.properties" | grep -v test | xargs grep -l "server.servlet.context-path\|management.endpoints.web.exposure" 2>/dev/null | wc -l)

    if [ "$deprecated_configs" -gt 0 ]; then
        echo -e "${YELLOW}⚠️ 发现 $deprecated_configs 个配置文件包含过时配置（Spring Boot 3.x）${NC}"
        spring_config_mismatches=$((spring_config_mismatches + deprecated_configs))
    else
        echo -e "${GREEN}✅ 配置文件检查通过（基础检查）${NC}"
    fi
else
    echo -e "${GREEN}✅ 未发现配置文件，使用默认配置${NC}"
fi

# 验证依赖安全性
echo -e "\n${BLUE}📋 第5项: 依赖安全性验证${NC}"

security_issues=0

# 检查是否有快照版本
snapshot_deps=$(find microservices -name "pom.xml" -exec grep -l "SNAPSHOT" {} \; | wc -l)
if [ "$snapshot_deps" -gt 0 ]; then
    echo -e "${YELLOW}⚠️ 发现 $snapshot_deps 个项目使用SNAPSHOT版本依赖${NC}"
    security_issues=$((security_issues + snapshot_deps))
else
    echo -e "${GREEN}✅ 无SNAPSHOT版本依赖${NC}"
fi

# 检查依赖冲突检查工具可用性
if command -v mvn >/dev/null 2>&1; then
    echo -e "${GREEN}✅ Maven工具可用${NC}"
else
    echo -e "${RED}❌ Maven工具不可用${NC}"
    security_issues=$((security_issues + 1))
fi

# 验证总结
echo -e "\n${CYAN}📊 技术栈验证总结${NC}"
echo -e "${CYAN}=========================${NC}"

echo -e "${BLUE}📈 验证统计:${NC}"
echo -e "  - 版本不匹配: $([ "$TOTAL_MISMATCHES" -gt 0 ] && echo "${RED}$TOTAL_MISMATCHES${NC}" || echo "${GREEN}$TOTAL_MISMATCHES${NC}") 个"
echo -e "  - 变量管理违规: $([ "$variable_violations" -gt 0 ] && echo "${YELLOW}$variable_violations${NC}" || echo "${GREEN}$variable_violations${NC}") 个"
echo -e "  - Java版本不匹配: $([ "$java_version_mismatches" -gt 0 ] && echo "${RED}$java_version_mismatches${NC}" || echo "${GREEN}$java_version_mismatches${NC}") 个"
echo -e "  - 配置过时: $([ "$spring_config_mismatches" -gt 0 ] && echo "${YELLOW}$spring_config_mismatches${NC}" || echo "${GREEN}$spring_config_mismatches${NC}") 个"
echo -e "  - 安全问题: $([ "$security_issues" -gt 0 ] && echo "${YELLOW}$security_issues${NC}" || echo "${GREEN}$security_issues${NC}") 个"

# 计算合规性评分
compliance_score=$((100 - (TOTAL_MISMATCHES * 2)))
if [ "$compliance_score" -lt 0 ]; then
    compliance_score=0
fi

echo -e "\n${BLUE}🎯 技术栈合规性评分: $compliance_score/100${NC}"

# 生成验证报告
report_file="tech-stack-validation-report-$(date '+%Y%m%d_%H%M%S').md"

cat > "$report_file" << EOF
# IOE-DREAM 技术栈版本统一验证报告

**验证时间**: $(date '+%Y-%m-%d %H:%M:%S')
**验证范围**: IOE-DREAM项目所有微服务
**验证工具**: tech-stack-validation.sh v1.0

## 📊 技术栈标准

| 技术组件 | 标准版本 | 父POM版本 | 状态 |
|---------|---------|-----------|------|
$(for component in "${!STANDARD_VERSIONS[@]}"; do
    parent_version=$(get_parent_version "$component")
    status=$([ "$parent_version" = "${STANDARD_VERSIONS[$component]}" ] && echo "✅ 通过" || echo "❌ 不匹配")
    echo "| $component | ${STANDARD_VERSIONS[$component]} | $parent_version | $status |"
done)

## 📋 验证结果详情

### 1. 版本一致性
- **检查项目**: 父POM版本标准符合性
- **发现问题**: $TOTAL_MISMATCHES 个版本不匹配

### 2. 依赖管理
- **检查项目**: 微服务依赖变量管理
- **服务数量**: $service_count 个微服务
- **变量管理违规**: $variable_violations 个

### 3. Java版本一致性
- **标准版本**: Java 17
- **发现问题**: $java_version_mismatches 个版本不匹配

### 4. Spring Boot配置
- **配置文件数量**: $application_files 个
- **过时配置**: $spring_config_mismatches 个

### 5. 安全性检查
- **SNAPSHOT依赖**: $snapshot_deps 个
- **工具可用性**: $([ -x "$(command -v mvn)" ] && echo "✅" || echo "❌")

## 🎯 合规性评估

**技术栈合规性评分**: $compliance_score/100

$([ "$compliance_score" -ge 95 ] && echo "🟢 **优秀** (95-100分): 技术栈版本完全符合企业级标准" || \
   [ "$compliance_score" -ge 85 ] && echo "🟡 **良好** (85-94分): 技术栈版本基本符合标准，有少量改进空间" || \
   [ "$compliance_score" -ge 70 ] && echo "🟠 **一般** (70-84分): 技术栈版本存在一些问题，需要改进" || \
   echo "🔴 **需要改进** (<70分): 技术栈版本存在严重问题，需要立即修复")

## 📋 标准技术栈

### 核心技术栈
- **Java**: 17.0.9 (LTS)
- **Spring Boot**: 3.5.8
- **Spring Cloud**: 2025.0.0
- **Spring Cloud Alibaba**: 2025.0.0.0

### 数据库技术栈
- **MySQL**: 8.0.35
- **MyBatis-Plus**: 3.5.15
- **Druid**: 1.2.25
- **Seata**: 2.0.0

### 监控与观测
- **Micrometer**: 1.13.6
- **Caffeine**: 3.1.8
- **Resilience4j**: 2.1.0

### 开发工具
- **Lombok**: 1.18.42
- **JUnit**: 5.11.0
- **Mockito**: 5.15.2

## 🔧 修复建议

### 版本不匹配修复
1. 检查父POM中的版本定义
2. 更新为标准版本
3. 使用变量引用而非硬编码

### 依赖管理优化
1. 移除所有硬编码版本号
2. 使用\`${variable.name}\`方式引用
3. 统一在父POM中管理

### 配置现代化
1. 更新Spring Boot 3.x的配置方式
2. 移除过时的配置项
3. 使用新的配置属性

## 📈 后续行动计划

1. **立即修复**: 版本不匹配问题
2. **代码审查**: 建立技术栈版本审查机制
3. **持续监控**: 定期验证技术栈一致性
4. **自动化**: 集成到CI/CD流水线

---

**报告生成**: $(date)
**验证工具**: IOE-DREAM Tech Stack Validator v1.0
EOF

echo -e "${GREEN}📄 详细验证报告已生成: $report_file${NC}"

# 退出状态
if [ "$TOTAL_MISMATCHES" -eq 0 ] && [ "$security_issues" -eq 0 ]; then
    echo -e "${GREEN}🎉 恭喜！技术栈版本完全统一！${NC}"
    echo -e "\n${GREEN}✅ 所有组件版本符合企业级标准${NC}"
    echo -e "${GREEN}✅ 依赖管理规范正确${NC}"
    echo -e "${GREEN}✅ 安全性检查通过${NC}"
    exit 0
elif [ "$TOTAL_MISMATCHES" -eq 0 ] && [ "$security_issues" -le 5 ]; then
    echo -e "${YELLOW}⚠️ 技术栈版本基本统一，有少量改进空间${NC}"
    exit 1
else
    echo -e "${RED}❌ 发现技术栈版本不统一问题，需要修复${NC}"
    echo -e "\n${YELLOW}🔧 修复建议:${NC}"
    echo -e "  1. 更新版本不匹配的组件"
    echo -e "  2. 修复变量管理违规"
    echo -e " 3. 检查并更新过时配置"
    exit 2
fi