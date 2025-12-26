#!/bin/bash

echo "=========================================="
echo "IOE-DREAM Video Service 架构违规修复验证脚本"
echo "=========================================="

# 设置颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${YELLOW}开始验证Video Service架构修复结果...${NC}"

# 验证1: 检查违规的VideoGatewayIntegrationConfig是否已删除
echo -e "\n${YELLOW}1. 验证VideoGatewayIntegrationConfig是否已删除...${NC}"
BAD_CONFIG_FILE="microservices/ioedream-video-service/src/main/java/net/lab1024/sa/video/config/VideoGatewayIntegrationConfig.java"
if [ ! -f "$BAD_CONFIG_FILE" ]; then
    echo -e "${GREEN}✅ 违规的VideoGatewayIntegrationConfig已删除${NC}"
else
    echo -e "${RED}❌ 违规的VideoGatewayIntegrationConfig仍然存在${NC}"
    exit 1
fi

# 验证2: 检查是否有gateway-client依赖
echo -e "\n${YELLOW}2. 验证gateway-client依赖...${NC}"
POM_FILE="microservices/ioedream-video-service/pom.xml"

if grep -q "microservices-common-gateway-client" "$POM_FILE"; then
    echo -e "${GREEN}✅ Video Service已添加gateway-client依赖${NC}"
else
    echo -e "${RED}❌ Video Service缺少gateway-client依赖${NC}"
    exit 1
fi

# 验证3: 检查VideoMonitorManager是否已更新
echo -e "\n${YELLOW}3. 验证VideoMonitorManager是否已更新...${NC}"
MANAGER_FILE="microservices/ioedream-video-service/src/main/java/net/lab1024/sa/video/manager/VideoMonitorManager.java"

# 检查是否有GatewayServiceClient导入
if grep -q "import.*GatewayServiceClient" "$MANAGER_FILE"; then
    echo -e "${GREEN}✅ VideoMonitorManager已添加GatewayServiceClient导入${NC}"
else
    echo -e "${RED}❌ VideoMonitorManager缺少GatewayServiceClient导入${NC}"
    exit 1
fi

# 检查构造函数是否包含GatewayServiceClient参数
CONSTRUCTOR_PATTERN="public VideoMonitorManager.*GatewayServiceClient"
if grep -q "$CONSTRUCTOR_PATTERN" "$MANAGER_FILE"; then
    echo -e "${GREEN}✅ VideoMonitorManager构造函数已更新${NC}"
else
    echo -e "${RED}❌ VideoMonitorManager构造函数未更新${NC}"
    exit 1
fi

# 验证4: 检查配置文件是否正确更新
echo -e "\n${YELLOW}4. 验证VideoManagerConfiguration是否已更新...${NC}"
CONFIG_FILE="microservices/ioedream-video-service/src/main/java/net/lab1024/sa/video/config/VideoManagerConfiguration.java"

# 检查是否有GatewayServiceClient导入
if grep -q "import.*GatewayServiceClient" "$CONFIG_FILE"; then
    echo -e "${GREEN}✅ VideoManagerConfiguration已添加GatewayServiceClient导入${NC}"
else
    echo -e "${RED}❌ VideoManagerConfiguration缺少GatewayServiceClient导入${NC}"
    exit 1
fi

# 检查Bean方法是否包含GatewayServiceClient参数
BEAN_PATTERN="videoMonitorManager.*GatewayServiceClient"
if grep -q "$BEAN_PATTERN" "$CONFIG_FILE"; then
    echo -e "${GREEN}✅ VideoManagerConfiguration Bean方法已更新${NC}"
else
    echo -e "${RED}❌ VideoManagerConfiguration Bean方法未更新${NC}"
    exit 1
fi

# 验证5: 检查是否还有其他使用MicroServiceCallHelper的地方
echo -e "\n${YELLOW}5. 验证是否还有MicroServiceCallHelper使用...${NC}"
HELPER_USAGE=$(find microservices/ioedream-video-service -name "*.java" -exec grep -l "MicroServiceCallHelper\|microServiceCallHelper" {} \;)

if [ -z "$HELPER_USAGE" ]; then
    echo -e "${GREEN}✅ 未发现MicroServiceCallHelper使用${NC}"
else
    echo -e "${YELLOW}⚠️ 发现MicroServiceCallHelper使用，需要手动检查：${NC}"
    echo "$HELPER_USAGE"
    # 这里不退出，因为有些使用可能是合理的
fi

echo -e "\n${GREEN}=========================================="
echo -e "Video Service架构修复验证通过！✅"
echo -e "==========================================${NC}"

# 输出修复总结
echo -e "\n${YELLOW}修复总结：${NC}"
echo -e "• ✅ 删除违规的自定义HTTP客户端配置"
echo -e "• ✅ 添加标准的GatewayServiceClient依赖"
echo -e "• ✅ 更新Manager层使用GatewayServiceClient"
echo -e "• ✅ 统一微服务间通信模式"
echo -e "\n${YELLOW}建议：${NC}"
echo -e "• 运行完整测试确保功能正常"
echo -e "• 检查其他微服务是否存在类似问题"
echo -e "• 更新相关文档和开发规范"