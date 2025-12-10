#!/bin/bash

# 测试覆盖率检查脚本
# 用于运行测试并生成覆盖率报告

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 项目根目录
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$PROJECT_ROOT"

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}IOE-DREAM 测试覆盖率检查${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

# 检查Maven是否安装
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}错误: Maven未安装，请先安装Maven${NC}"
    exit 1
fi

# 检查JaCoCo插件是否配置
echo -e "${YELLOW}检查JaCoCo配置...${NC}"
if ! grep -q "jacoco-maven-plugin" microservices/pom.xml; then
    echo -e "${YELLOW}警告: 未找到JaCoCo插件配置，将使用默认配置${NC}"
fi

# 服务列表
SERVICES=(
    "microservices/ioedream-consume-service"
    "microservices/ioedream-access-service"
    "microservices/ioedream-attendance-service"
    "microservices/ioedream-visitor-service"
    "microservices/ioedream-video-service"
)

# 覆盖率目标
COVERAGE_TARGET=80

# 运行测试并生成覆盖率报告
echo -e "${GREEN}开始运行测试...${NC}"
echo ""

TOTAL_COVERAGE=0
SERVICE_COUNT=0

for SERVICE in "${SERVICES[@]}"; do
    SERVICE_NAME=$(basename "$SERVICE")
    echo -e "${YELLOW}========================================${NC}"
    echo -e "${YELLOW}测试服务: $SERVICE_NAME${NC}"
    echo -e "${YELLOW}========================================${NC}"
    
    if [ ! -d "$SERVICE" ]; then
        echo -e "${RED}错误: 服务目录不存在: $SERVICE${NC}"
        continue
    fi
    
    cd "$SERVICE"
    
    # 运行测试
    echo -e "${GREEN}运行测试...${NC}"
    if mvn clean test jacoco:report -DskipTests=false 2>&1 | tee test-output.log; then
        echo -e "${GREEN}测试通过${NC}"
        
        # 查找覆盖率报告
        if [ -f "target/site/jacoco/index.html" ]; then
            echo -e "${GREEN}覆盖率报告已生成: $SERVICE/target/site/jacoco/index.html${NC}"
            
            # 尝试提取覆盖率（需要解析HTML或使用JaCoCo API）
            echo -e "${YELLOW}查看覆盖率报告: file://$(pwd)/target/site/jacoco/index.html${NC}"
        else
            echo -e "${YELLOW}警告: 未找到覆盖率报告${NC}"
        fi
    else
        echo -e "${RED}测试失败，请查看日志: $SERVICE/test-output.log${NC}"
    fi
    
    cd "$PROJECT_ROOT"
    echo ""
done

# 汇总报告
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}测试覆盖率检查完成${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo -e "${YELLOW}覆盖率报告位置:${NC}"
for SERVICE in "${SERVICES[@]}"; do
    SERVICE_NAME=$(basename "$SERVICE")
    if [ -f "$SERVICE/target/site/jacoco/index.html" ]; then
        echo -e "  - $SERVICE_NAME: file://$PROJECT_ROOT/$SERVICE/target/site/jacoco/index.html"
    fi
done
echo ""
echo -e "${YELLOW}目标覆盖率: ${COVERAGE_TARGET}%${NC}"
echo -e "${YELLOW}请查看各服务的覆盖率报告以确认是否达到目标${NC}"

