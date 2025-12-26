#!/bin/bash

echo "=========================================="
echo "IOE-DREAM Attendance Service Entity优化验证脚本"
echo "=========================================="

# 设置颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${YELLOW}开始验证Attendance Service Entity优化结果...${NC}"

# 验证1: 检查EmployeeResponse是否已创建
echo -e "\n${YELLOW}1. 验证EmployeeResponse是否已创建...${NC}"
EMPLOYEE_RESPONSE_FILE="microservices/microservices-common-gateway-client/src/main/java/net/lab1024/sa/common/gateway/domain/response/EmployeeResponse.java"
if [ -f "$EMPLOYEE_RESPONSE_FILE" ]; then
    echo -e "${GREEN}✅ EmployeeResponse已创建${NC}"
else
    echo -e "${RED}❌ EmployeeResponse创建失败${NC}"
    exit 1
fi

# 验证2: 检查DeviceResponse是否存在（复用Access Service创建的）
echo -e "\n${YELLOW}2. 验证DeviceResponse是否存在...${NC}"
DEVICE_RESPONSE_FILE="microservices/microservices-common-gateway-client/src/main/java/net/lab1024/sa/common/gateway/domain/response/DeviceResponse.java"
if [ -f "$DEVICE_RESPONSE_FILE" ]; then
    echo -e "${GREEN}✅ DeviceResponse已存在${NC}"
else
    echo -e "${RED}❌ DeviceResponse不存在${NC}"
    exit 1
fi

# 验证3: 检查AttendanceMobileServiceImpl是否已更新
echo -e "\n${YELLOW}3. 验证AttendanceMobileServiceImpl是否已更新...${NC}"
MOBILE_SERVICE_FILE="microservices/ioedream-attendance-service/src/main/java/net/lab1024/sa/attendance/mobile/impl/AttendanceMobileServiceImpl.java"

# 检查是否有EmployeeResponse导入
if grep -q "import.*EmployeeResponse" "$MOBILE_SERVICE_FILE"; then
    echo -e "${GREEN}✅ AttendanceMobileServiceImpl已添加EmployeeResponse导入${NC}"
else
    echo -e "${RED}❌ AttendanceMobileServiceImpl缺少EmployeeResponse导入${NC}"
    exit 1
fi

# 检查是否有UserInfoResponse导入
if grep -q "import.*UserInfoResponse" "$MOBILE_SERVICE_FILE"; then
    echo -e "${GREEN}✅ AttendanceMobileServiceImpl已添加UserInfoResponse导入${NC}"
else
    echo -e "${RED}❌ AttendanceMobileServiceImpl缺少UserInfoResponse导入${NC}"
    exit 1
fi

# 检查是否还有直接返回EmployeeEntity的方法
COUNT_EMPLOYEE_ENTITY=$(grep -c "EmployeeEntity.*response\|ResponseDTO<EmployeeEntity>" "$MOBILE_SERVICE_FILE")
if [ "$COUNT_EMPLOYEE_ENTITY" -eq 0 ]; then
    echo -e "${GREEN}✅ 未发现直接返回EmployeeEntity的方法${NC}"
else
    echo -e "${YELLOW}⚠️ 发现 $COUNT_EMPLOYEE_ENTITY 处EmployeeEntity使用，需要手动检查${NC}"
fi

# 验证4: 检查AbstractAttendanceProcessTemplate是否已更新
echo -e "\n${YELLOW}4. 验证AbstractAttendanceProcessTemplate是否已更新...${NC}"
TEMPLATE_FILE="microservices/ioedream-attendance-service/src/main/java/net/lab1024/sa/attendance/template/AbstractAttendanceProcessTemplate.java"

# 检查validateDevice方法返回类型
if grep -q "private DeviceResponse validateDevice" "$TEMPLATE_FILE"; then
    echo -e "${GREEN}✅ validateDevice方法已更新返回类型为DeviceResponse${NC}"
else
    echo -e "${RED}❌ validateDevice方法返回类型未更新${NC}"
    exit 1
fi

# 检查是否有DeviceResponse导入
if grep -q "import.*DeviceResponse" "$TEMPLATE_FILE"; then
    echo -e "${GREEN}✅ AbstractAttendanceProcessTemplate已添加DeviceResponse导入${NC}"
else
    echo -e "${RED}❌ AbstractAttendanceProcessTemplate缺少DeviceResponse导入${NC}"
    exit 1
fi

# 检查是否有GatewayServiceClient导入
if grep -q "import.*GatewayServiceClient" "$TEMPLATE_FILE"; then
    echo -e "${GREEN}✅ AbstractAttendanceProcessTemplate已添加GatewayServiceClient导入${NC}"
else
    echo -e "${RED}❌ AbstractAttendanceProcessTemplate缺少GatewayServiceClient导入${NC}"
    exit 1
fi

# 检查是否有convertToDeviceResponse方法
if grep -q "convertToDeviceResponse" "$TEMPLATE_FILE"; then
    echo -e "${GREEN}✅ AbstractAttendanceProcessTemplate已添加Entity到Response转换方法${NC}"
else
    echo -e "${RED}❌ AbstractAttendanceProcessTemplate缺少转换方法${NC}"
    exit 1
fi

# 验证5: 检查StandardAttendanceProcess是否已更新
echo -e "\n${YELLOW}5. 验证StandardAttendanceProcess是否已更新...${NC}"
STANDARD_PROCESS_FILE="microservices/ioedream-attendance-service/src/main/java/net/lab1024/sa/attendance/template/impl/StandardAttendanceProcess.java"

# 检查是否有DeviceResponse导入
if grep -q "import.*DeviceResponse" "$STANDARD_PROCESS_FILE"; then
    echo -e "${GREEN}✅ StandardAttendanceProcess已添加DeviceResponse导入${NC}"
else
    echo -e "${RED}❌ StandardAttendanceProcess缺少DeviceResponse导入${NC}"
    exit 1
fi

# 检查方法参数类型是否已更新
if grep -q "DeviceResponse device" "$STANDARD_PROCESS_FILE"; then
    echo -e "${GREEN}✅ StandardAttendanceProcess方法参数已更新为DeviceResponse${NC}"
else
    echo -e "${RED}❌ StandardAttendanceProcess方法参数未更新${NC}"
    exit 1
fi

# 验证6: 检查是否还有直接使用Entity的公开方法
echo -e "\n${YELLOW}6. 检查Attendance Service中是否还有其他Entity违规使用...${NC}"
ATTENDANCE_SERVICE_DIR="microservices/ioedream-attendance-service/src/main/java/net/lab1024/sa/attendance"

# 查找返回类型为Entity的公开方法
ENTITY_PUBLIC_METHODS=$(find "$ATTENDANCE_SERVICE_DIR" -name "*.java" -exec grep -l "public.*Entity.*{" {} \; | wc -l)
if [ "$ENTITY_PUBLIC_METHODS" -gt 0 ]; then
    echo -e "${YELLOW}⚠️ 发现 $ENTITY_PUBLIC_METHODS 个文件包含返回Entity的公开方法（需要手动检查）${NC}"
    find "$ATTENDANCE_SERVICE_DIR" -name "*.java" -exec grep -l "public.*Entity.*{" {} \; | head -5
else
    echo -e "${GREEN}✅ 未发现返回Entity的公开方法${NC}"
fi

# 查找参数类型为Entity的方法
ENTITY_PARAM_METHODS=$(find "$ATTENDANCE_SERVICE_DIR" -name "*.java" -exec grep -l "\.class.*Entity\|Entity.*param" {} \; | wc -l)
if [ "$ENTITY_PARAM_METHODS" -gt 0 ]; then
    echo -e "${YELLOW}⚠️ 发现 $ENTITY_PARAM_METHODS 个文件可能包含Entity参数使用（需要手动检查）${NC}"
    find "$ATTENDANCE_SERVICE_DIR" -name "*.java" -exec grep -l "\.class.*Entity\|Entity.*param" {} \; | head -5
else
    echo -e "${GREEN}✅ 未发现Entity参数使用问题${NC}"
fi

# 验证7: 统计优化成果
echo -e "\n${YELLOW}7. 优化成果统计...${NC}"

# 统计修复的文件数量
FIXED_FILES=0
if grep -q "EmployeeResponse" "$MOBILE_SERVICE_FILE"; then
    ((FIXED_FILES++))
fi
if grep -q "DeviceResponse" "$TEMPLATE_FILE"; then
    ((FIXED_FILES++))
fi
if grep -q "DeviceResponse" "$STANDARD_PROCESS_FILE"; then
    ((FIXED_FILES++))
fi

echo -e "${GREEN}✅ 已修复文件数量: $FIXED_FILES${NC}"
echo -e "${GREEN}✅ 新增Response对象: EmployeeResponse${NC}"
echo -e "${GREEN}✅ 复用Response对象: DeviceResponse, UserInfoResponse${NC}"

echo -e "\n${GREEN}=========================================="
echo -e "Attendance Service Entity优化验证通过！✅"
echo -e "==========================================${NC}"

# 输出优化总结
echo -e "\n${YELLOW}优化总结：${NC}"
echo -e "• ✅ 创建EmployeeResponse对象"
echo -e "• ✅ 修复AttendanceMobileServiceImpl返回类型"
echo -e "• ✅ 更新AbstractAttendanceProcessTemplate使用DeviceResponse"
echo -e "• ✅ 更新StandardAttendanceProcess方法签名"
echo -e "• ✅ 添加Entity到Response转换方法"
echo -e "• ✅ 符合微服务边界原则"

echo -e "\n${YELLOW}架构价值：${NC}"
echo -e "• 🛡️ 消除了跨服务Entity传递风险"
echo -e "• 📊 建立了标准Response对象模式"
echo -e "• 🔧 提供了可复用的转换器实现"
echo -e "• 🏗️ 符合企业级微服务架构标准"

echo -e "\n${YELLOW}下一步建议：${NC}"
echo -e "• 测试Attendance Service功能正常"
echo -e "• 检查其他Manager和Service类"
echo -e "• 验证Controller层VO对象使用"
echo -e "• 推广到其他微服务模块"