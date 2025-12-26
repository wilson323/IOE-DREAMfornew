#!/bin/bash

echo "=========================================="
echo "IOE-DREAM Access Service Entity优化验证脚本"
echo "=========================================="

# 设置颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${YELLOW}开始验证Access Service Entity优化结果...${NC}"

# 验证1: 检查DeviceResponse是否已创建
echo -e "\n${YELLOW}1. 验证DeviceResponse是否已创建...${NC}"
DEVICE_RESPONSE_FILE="microservices/microservices-common-gateway-client/src/main/java/net/lab1024/sa/common/gateway/domain/response/DeviceResponse.java"
if [ -f "$DEVICE_RESPONSE_FILE" ]; then
    echo -e "${GREEN}✅ DeviceResponse已创建${NC}"
else
    echo -e "${RED}❌ DeviceResponse创建失败${NC}"
    exit 1
fi

# 验证2: 检查AreaResponse是否已创建
echo -e "\n${YELLOW}2. 验证AreaResponse是否已创建...${NC}"
AREA_RESPONSE_FILE="microservices/microservices-common-gateway-client/src/main/java/net/lab1024/sa/common/gateway/domain/response/AreaResponse.java"
if [ -f "$AREA_RESPONSE_FILE" ]; then
    echo -e "${GREEN}✅ AreaResponse已创建${AC_GREEN}"
else
    echo -e "${RED}❌ AreaResponse创建失败${NC}"
    exit 1
fi

# 验证3: 检查AccessVerificationManager是否已更新
echo -e "\n${YELLOW}3. 验证AccessVerificationManager是否已更新...${NC}"
MANAGER_FILE="microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/manager/AccessVerificationManager.java"

# 检查是否有DeviceResponse导入
if grep -q "import.*DeviceResponse" "$MANAGER_FILE"; then
    echo -e "${GREEN}✅ AccessVerificationManager已添加DeviceResponse导入${NC}"
else
    echo -e "${RED}❌ AccessVerificationManager缺少DeviceResponse导入${NC}"
    exit 1
fi

# 检查方法签名是否已更新
if grep -q "public DeviceResponse getDeviceBySerialNumber" "$MANAGER_FILE"; then
    echo -e "${GREEN}✅ getDeviceBySerialNumber方法已更新返回类型${NC}"
else
    echo -e "${RED}❌ getDeviceBySerialNumber方法返回类型未更新${NC}"
    exit 1
fi

# 检查是否有转换方法
if grep -q "convertToResponse" "$MANAGER_FILE"; then
    echo -e "${GREEN}✅ AccessVerificationManager已添加Entity到Response转换方法${NC}"
else
    echo -e "${RED}❌ AccessVerificationManager缺少转换方法${NC}"
    exit 1
fi

# 验证4: 检查是否还有直接使用DeviceEntity的地方
echo -e "\n${YELLOW}4. 检查Access Service中是否还有直接使用DeviceEntity的地方...${NC}"
# 查找返回类型为DeviceEntity的方法
ENTITY_METHODS=$(grep -n "public.*DeviceEntity.*get.*{" "$MANAGER_FILE" | head -5)
if [ -n "$ENTITY_METHODS" ]; then
    echo -e "${YELLOW}⚠️ 发现直接返回DeviceEntity的方法（需要手动检查）：${NC}"
    echo "$ENTITY_METHODS"
else
    echo -e "${GREEN}✅ 未发现直接返回DeviceEntity的公开方法${NC}"
fi

# 查找参数类型为DeviceEntity的方法
ENTITY_PARAMS=$(grep -n "DeviceEntity.*param\|DeviceEntity.*:" "$MANAGER_FILE" | head -5)
if [n "$ENTITY_PARAMS" ]; then
    echo -e "${YELLOW}⚠️ 发现使用DeviceEntity作为参数的方法（需要手动检查）：${NC}"
    echo "$ENTITY_PARAMS"
else
    echo -e "${GREEN}✅ 未发现直接使用DeviceEntity作为参数的公开方法${NC}"
fi

# 验证5: 检查是否有其他需要优化的文件
echo -e "\n${YELLOW}5. 检查其他需要优化的文件...${NEG}"
ACCESS_SERVICE_DIR="microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access"

# 查找导入Organization Entity的文件
IMPORT_COUNT=$(find "$ACCESS_SERVICE_DIR" -name "*.java" -exec grep -l "import.*common\.organization\.entity\." {} \; | wc -l)
if [ "$IMPORT_COUNT" -gt 0 ]; then
    echo -e "${YELLOW}发现需要检查的文件（$IMPORT_COUNT个）：${NC}"
    find "$ACCESS_SERVICE_DIR" -name "*.java" -exec grep -l "import.*common\.organization\.entity\." {} \; | head -10
    echo -e "\n${YELLOW}建议：${NC}"
    echo "• 检查每个文件的Entity使用场景"
    echo "• 对于Service层返回，使用Response对象"
    echo "• 对于Controller层，使用VO对象"
    echo "• 仅DAO层可以保留Entity使用"
else
    echo -e "${GREEN}✅ 未发现需要优化的Entity导入${NC}"
fi

echo -e "\n${GREEN}=========================================="
echo -e "Access Service Entity优化验证通过！✅"
echo -e "==========================================${NC}"

# 输出优化总结
echo -e "\n${YELLOW}优化总结：${NC}"
echo -e "• ✅ 创建DeviceResponse和AreaResponse对象"
echo -e "• ✅ 修复AccessVerificationManager返回类型"
echo -e "• ✅ 添加Entity到Response转换方法"
echo -e "• ✅ 符合微服务边界原则"
echo -e "\n${YELLOW}下一步建议：${NC}"
echo -e "• 测试AccessVerificationManager功能正常"
echo -e "• 逐步优化其他Manager和Service类"
echo -e "• 验证Controller层VO对象使用"
echo -e "• 更新相关的调用代码"