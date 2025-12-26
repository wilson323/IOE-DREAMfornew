#!/bin/bash

echo "=========================================="
echo "IOE-DREAM 架构优化验证脚本"
echo "=========================================="

# 设置颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${YELLOW}开始验证架构优化结果...${NC}"

# 验证1: 检查UserInfoResponse新位置
echo -e "\n${YELLOW}1. 验证UserInfoResponse位置...${NC}"
USERINFO_FILE="microservices/microservices-common-gateway-client/src/main/java/net/lab1024/sa/common/gateway/domain/response/UserInfoResponse.java"
if [ -f "$USERINFO_FILE" ]; then
    echo -e "${GREEN}✅ UserInfoResponse已迁移至gateway-client模块${NC}"
else
    echo -e "${RED}❌ UserInfoResponse迁移失败${NC}"
    exit 1
fi

# 验证2: 检查SmartSchedulingEngine依赖
echo -e "\n${YELLOW}2. 验证SmartSchedulingEngine依赖...${NC}"
SMART_ENGINE_FILE="microservices/ioedream-attendance-service/src/main/java/net/lab1024/sa/attendance/manager/SmartSchedulingEngine.java"

# 检查是否还有活跃的EmployeeDao导入（忽略注释）
ACTIVE_EMPLOYEE_DAO=$(grep -v "^[[:space:]]*//" "$SMART_ENGINE_FILE" | grep -q "import.*EmployeeDao")
if [ $? -eq 0 ]; then
    echo -e "${RED}❌ SmartSchedulingEngine仍存在活跃的EmployeeDao依赖${NC}"
    grep -n "import.*EmployeeDao" "$SMART_ENGINE_FILE"
    exit 1
else
    echo -e "${GREEN}✅ SmartSchedulingEngine已移除活跃的EmployeeDao依赖${NC}"
fi

# 检查是否有GatewayServiceClient导入
if grep -q "import.*GatewayServiceClient" "$SMART_ENGINE_FILE"; then
    echo -e "${GREEN}✅ SmartSchedulingEngine已添加GatewayServiceClient依赖${NC}"
else
    echo -e "${RED}❌ SmartSchedulingEngine缺少GatewayServiceClient依赖${NC}"
    exit 1
fi

# 验证3: 检查配置文件
echo -e "\n${YELLOW}3. 验证配置文件...${NAMESPACE}"
CONFIG_FILE="microservices/ioedream-attendance-service/src/main/java/net/lab1024/sa/attendance/config/AttendanceManagerConfiguration.java"

# 检查SmartSchedulingEngine Bean是否已激活
if grep -q "@Bean" "$CONFIG_FILE" && grep -q "SmartSchedulingEngine" "$CONFIG_FILE"; then
    echo -e "${GREEN}✅ SmartSchedulingEngine Bean已激活${NC}"
else
    echo -e "${RED}❌ SmartSchedulingEngine Bean未激活${NC}"
    echo "检查配置文件: $CONFIG_FILE"
    echo "查找Bean注解和类名..."
    grep -n "@Bean\|SmartSchedulingEngine" "$CONFIG_FILE"
    exit 1
fi

# 验证4: 检查pom.xml依赖优化
echo -e "\n${YELLOW}4. 验证pom.xml依赖优化...${NC}"
POM_FILE="microservices/ioedream-attendance-service/pom.xml"

# 检查是否还有ioedream-common-service依赖（忽略注释）
ACTIVE_DEPENDENCY=$(grep -v "^[[:space:]]*<!--" "$POM_FILE" | grep -v "^[[:space:]]*-->" | grep -q "ioedream-common-service")
if [ $? -eq 0 ]; then
    echo -e "${RED}❌ 仍存在ioedream-common-service依赖${NC}"
    grep -n "ioedream-common-service" "$POM_FILE"
    exit 1
else
    echo -e "${GREEN}✅ 已移除ioedream-common-service依赖${NC}"
fi

# 检查是否有gateway-client依赖
if grep -q "microservices-common-gateway-client" "$POM_FILE"; then
    echo -e "${GREEN}✅ 已添加gateway-client依赖${NC}"
else
    echo -e "${RED}❌ 缺少gateway-client依赖${NC}"
    exit 1
fi

# 验证5: 检查import路径更新
echo -e "\n${YELLOW}5. 验证import路径更新...${NC}"
ATTENDANCE_MANAGER_FILE="microservices/ioedream-attendance-service/src/main/java/net/lab1024/sa/attendance/manager/AttendanceManager.java"

if grep -q "import.*common\.gateway\.domain\.response\.UserInfoResponse" "$ATTENDANCE_MANAGER_FILE"; then
    echo -e "${GREEN}✅ AttendanceManager已更新UserInfoResponse导入路径${NC}"
else
    echo -e "${RED}❌ AttendanceManagerUserInfoResponse导入路径未更新${NC}"
    exit 1
fi

echo -e "\n${GREEN}=========================================="
echo -e "所有架构优化验证通过！✅"
echo -e "==========================================${NC}"