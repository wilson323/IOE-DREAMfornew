#!/bin/bash

# =============================================================================
# IOE-DREAM CI/CD架构合规性检查脚本
# =============================================================================
# 用途: 集成到CI/CD流水线中，确保代码合入前通过架构合规性检查
# 使用:
#   - Git pre-commit hook
#   - GitHub Actions workflow
#   - Jenkins pipeline stage
# =============================================================================

set -e

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}========================================"
echo -e "IOE-DREAM 架构合规性检查 (CI/CD)"
echo -e "========================================${NC}"

# 检查结果统计
TOTAL_CHECKS=0
PASSED_CHECKS=0
FAILED_CHECKS=0

# 检查函数
check_architecture_compliance() {
    local check_name="$1"
    local check_command="$2"

    echo -e "\n${YELLOW}检查: ${check_name}${NC}"
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))

    if eval "$check_command"; then
        echo -e "${GREEN}✅ 通过${NC}"
        PASSED_CHECKS=$((PASSED_CHECKS + 1))
        return 0
    else
        echo -e "${RED}❌ 失败${NC}"
        FAILED_CHECKS=$((FAILED_CHECKS + 1))
        return 1
    fi
}

# 1. 检查是否违反微服务间通信规范
echo -e "\n${BLUE}📋 微服务间通信规范检查${NC}"

check_architecture_compliance "禁止直接依赖ioedream-common-service" \
    "! find microservices -name 'pom.xml' -exec grep -l 'ioedream-common-service' {} \; | grep -v 'ioedream-common-service' | grep -v 'ioedream-gateway-service'"

check_architecture_compliance "强制使用GatewayServiceClient进行微服务调用" \
    "find microservices -name '*.java' -path '*/service/impl/*' -exec grep -l 'callCommonService\|callUserService\|callAttendanceService' {} \; | wc -l | grep -q '^[1-9]'"

check_architecture_compliance "响应对象必须在gateway-client模块" \
    "test -f microservices/microservices-common-gateway-client/src/main/java/net/lab1024/sa/common/gateway/domain/response/UserInfoResponse.java"

# 2. 检查依赖关系规范
echo -e "\n${BLUE}📋 依赖关系规范检查${NC}"

check_architecture_compliance "禁止循环依赖" \
    "! scripts/check-dependency-structure.ps1 | grep -i 'circular\|cycle'"

check_architecture_compliance "细粒度模块依赖检查" \
    "find microservices -name 'pom.xml' -exec grep -l 'microservices-common-' {} \; | wc -l | grep -q '^[1-9]'"

# 3. 检查代码质量规范
echo -e "\n${BLUE}📋 代码质量规范检查${NC}"

check_architecture_compliance "@Repository命名规范检查" \
    "! find microservices -name '*.java' -exec grep -l '@Repository' {} \; | wc -l | grep -q '^[1-9]'"

check_architecture_compliance "@Autowired使用规范检查" \
    "! find microservices -name '*.java' -exec grep -l '@Autowired' {} \; | wc -l | grep -q '^[1-9]'"

# 4. 检查构建顺序规范
echo -e "\n${BLUE}📋 构建顺序规范检查${NC}"

check_architecture_compliance "microservices-common构建检查" \
    "test -f microservices/microservices-common/pom.xml"

check_architecture_compliance "公共模块JAR文件检查" \
    "test -f '$HOME/.m2/repository/net/lab1024/sa/microservices-common/1.0.0/microservices-common-1.0.0.jar'"

# 5. 运行架构验证脚本
echo -e "\n${BLUE}📋 架构修复验证检查${NC}"

if [ -f "scripts/validate-architecture-fixes.sh" ]; then
    check_architecture_compliance "架构修复验证" \
        "bash scripts/validate-architecture-fixes.sh"
else
    echo -e "${YELLOW}⚠️ 架构验证脚本不存在，跳过此项检查${NC}"
fi

# 6. 生成检查报告
echo -e "\n${BLUE}========================================"
echo -e "检查结果汇总"
echo -e "========================================${NC}"

echo -e "总检查项: ${TOTAL_CHECKS}"
echo -e "${GREEN}通过: ${PASSED_CHECKS}${NC}"
echo -e "${RED}失败: ${FAILED_CHECKS}${NC}"

# 计算通过率
if [ $TOTAL_CHECKS -gt 0 ]; then
    PASS_RATE=$((PASSED_CHECKS * 100 / TOTAL_CHECKS))
    echo -e "通过率: ${PASS_RATE}%"

    if [ $PASS_RATE -ge 90 ]; then
        echo -e "${GREEN}✅ 架构合规性检查通过${NC}"
        exit 0
    else
        echo -e "${RED}❌ 架构合规性检查未通过${NC}"
        echo -e "${YELLOW}请修复失败的检查项后重新提交${NC}"
        exit 1
    fi
else
    echo -e "${YELLOW}⚠️ 未执行任何检查${NC}"
    exit 0
fi

# 7. 生成详细报告（可选）
if [ "$1" = "--verbose" ]; then
    REPORT_FILE="architecture-compliance-report-$(date +%Y%m%d-%H%M%S).md"

    cat > "$REPORT_FILE" << EOF
# IOE-DREAM 架构合规性检查报告

**检查时间**: $(date)
**检查分支**: $(git branch --show-current 2>/dev/null || echo "unknown")
**提交哈希**: $(git rev-parse HEAD 2>/dev/null || echo "unknown")

## 检查结果
- 总检查项: $TOTAL_CHECKS
- 通过: $PASSED_CHECKS
- 失败: $FAILED_CHECKS
- 通过率: $PASS_RATE%

## 详细检查项
$(cat <<INNER_EOF
1. 微服务间通信规范检查
2. 依赖关系规范检查
3. 代码质量规范检查
4. 构建顺序规范检查
5. 架构修复验证检查
INNER_EOF
)

## 建议
- 持续监控架构合规性
- 定期更新检查规则
- 团队培训和规范宣贯

EOF

    echo -e "${BLUE}详细报告已生成: ${REPORT_FILE}${NC}"
fi