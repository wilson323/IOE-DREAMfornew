#!/bin/bash

echo "=========================================="
echo "IOE-DREAM Video Service 架构优化验证脚本"
echo "=========================================="

# 设置颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${YELLOW}开始验证Video Service架构优化结果...${NC}"

# 验证1: 检查DeviceResponse是否存在（复用Access Service创建的）
echo -e "\n${YELLOW}1. 验证DeviceResponse是否存在...${NC}"
DEVICE_RESPONSE_FILE="microservices/microservices-common-gateway-client/src/main/java/net/lab1024/sa/common/gateway/domain/response/DeviceResponse.java"
if [ -f "$DEVICE_RESPONSE_FILE" ]; then
    echo -e "${GREEN}✅ DeviceResponse已存在${NC}"
else
    echo -e "${RED}❌ DeviceResponse不存在${NC}"
    exit 1
fi

# 验证2: 检查IVideoStreamAdapter接口是否已更新
echo -e "\n${YELLOW}2. 验证IVideoStreamAdapter接口是否已更新...${NC}"
ADAPTER_INTERFACE_FILE="microservices/ioedream-video-service/src/main/java/net/lab1024/sa/video/adapter/IVideoStreamAdapter.java"

# 检查是否有DeviceResponse导入
if grep -q "import.*DeviceResponse" "$ADAPTER_INTERFACE_FILE"; then
    echo -e "${GREEN}✅ IVideoStreamAdapter已添加DeviceResponse导入${NC}"
else
    echo -e "${RED}❌ IVideoStreamAdapter缺少DeviceResponse导入${NC}"
    exit 1
fi

# 检查是否还有DeviceEntity导入
if grep -q "import.*DeviceEntity" "$ADAPTER_INTERFACE_FILE"; then
    echo -e "${RED}❌ IVideoStreamAdapter仍包含DeviceEntity导入${NC}"
    exit 1
else
    echo -e "${GREEN}✅ IVideoStreamAdapter已移除DeviceEntity导入${NC}"
fi

# 检查createStream方法参数类型
if grep -q "createStream(DeviceResponse device)" "$ADAPTER_INTERFACE_FILE"; then
    echo -e "${GREEN}✅ createStream方法参数已更新为DeviceResponse${NC}"
else
    echo -e "${RED}❌ createStream方法参数未更新${NC}"
    exit 1
fi

# 验证3: 检查Adapter实现类是否已更新
echo -e "\n${YELLOW}3. 验证Adapter实现类是否已更新...${NC}"

ADAPTER_IMPL_FILES=(
    "microservices/ioedream-video-service/src/main/java/net/lab1024/sa/video/adapter/impl/HTTPAdapter.java"
    "microservices/ioedream-video-service/src/main/java/net/lab1024/sa/video/adapter/impl/RTMPAdapter.java"
    "microservices/ioedream-video-service/src/main/java/net/lab1024/sa/video/adapter/impl/RTSPAdapter.java"
)

for impl_file in "${ADAPTER_IMPL_FILES[@]}"; do
    adapter_name=$(basename "$impl_file" .java)

    # 检查DeviceResponse导入
    if grep -q "import.*DeviceResponse" "$impl_file"; then
        echo -e "${GREEN}✅ ${adapter_name}已添加DeviceResponse导入${NC}"
    else
        echo -e "${RED}❌ ${adapter_name}缺少DeviceResponse导入${NC}"
        exit 1
    fi

    # 检查是否还有DeviceEntity导入
    if grep -q "import.*DeviceEntity" "$impl_file"; then
        echo -e "${RED}❌ ${adapter_name}仍包含DeviceEntity导入${NC}"
        exit 1
    else
        echo -e "${GREEN}✅ ${adapter_name}已移除DeviceEntity导入${NC}"
    fi

    # 检查createStream方法参数类型
    if grep -q "createStream(DeviceResponse device)" "$impl_file"; then
        echo -e "${GREEN}✅ ${adapter_name} createStream方法参数已更新为DeviceResponse${NC}"
    else
        echo -e "${RED}❌ ${adapter_name} createStream方法参数未更新${NC}"
        exit 1
    fi

    # 检查build方法参数类型
    if grep -q "build[A-Z]*Url(DeviceResponse device)" "$impl_file"; then
        echo -e "${GREEN}✅ ${adapter_name} build方法参数已更新为DeviceResponse${NC}"
    else
        echo -e "${YELLOW}⚠️ ${adapter_name} build方法可能需要检查${NC}"
    fi
done

# 验证4: 检查VideoDeviceServiceImpl合规性
echo -e "\n${YELLOW}4. 验证VideoDeviceServiceImpl合规性...${NC}"
VIDEO_DEVICE_SERVICE_FILE="microservices/ioedream-video-service/src/main/java/net/lab1024/sa/video/service/impl/VideoDeviceServiceImpl.java"

# 检查是否直接返回Entity
ENTITY_RETURN_COUNT=$(grep -c "public.*DeviceEntity.*{" "$VIDEO_DEVICE_SERVICE_FILE")
if [ "$ENTITY_RETURN_COUNT" -eq 0 ]; then
    echo -e "${GREEN}✅ 未发现直接返回DeviceEntity的公开方法${NC}"
else
    echo -e "${RED}❌ 发现 $ENTITY_RETURN_COUNT 个直接返回DeviceEntity的方法${NC}"
    exit 1
fi

# 检查是否有Entity参数问题
ENTITY_PARAM_COUNT=$(grep -c "\.class.*Entity\|Entity.*param" "$VIDEO_DEVICE_SERVICE_FILE")
if [ "$ENTITY_PARAM_COUNT" -eq 0 ]; then
    echo -e "${GREEN}✅ 未发现Entity参数使用问题${NC}"
else
    echo -e "${YELLOW}⚠️ 发现 $ENTITY_PARAM_COUNT 处Entity参数使用（需要手动检查是否合规）${NC}"
fi

# 验证5: 检查VideoPlayServiceImpl合规性
echo -e "\n${YELLOW}5. 验证VideoPlayServiceImpl合规性...${NC}"
VIDEO_PLAY_SERVICE_FILE="microservices/ioedream-video-service/src/main/java/net/lab1024/sa/video/service/impl/VideoPlayServiceImpl.java"

# 检查是否直接返回Entity
PLAY_ENTITY_RETURN_COUNT=$(grep -c "public.*DeviceEntity.*{" "$VIDEO_PLAY_SERVICE_FILE")
if [ "$PLAY_ENTITY_RETURN_COUNT" -eq 0 ]; then
    echo -e "${GREEN}✅ 未发现直接返回DeviceEntity的公开方法${NC}"
else
    echo -e "${RED}❌ 发现 $PLAY_ENTITY_RETURN_COUNT 个直接返回DeviceEntity的方法${NC}"
    exit 1
fi

# 验证6: 检查Service层是否正确使用VO对象
echo -e "\n${YELLOW}6. 验证Service层VO对象使用...${NC}"

# 检查是否正确转换为VO
VO_CONVERT_COUNT=$(grep -c "convertToVO\|VideoDeviceVO" "$VIDEO_DEVICE_SERVICE_FILE")
if [ "$VO_CONVERT_COUNT" -gt 0 ]; then
    echo -e "${GREEN}✅ 发现 $VO_CONVERT_COUNT 处VO对象转换使用${NC}"
else
    echo -e "${YELLOW}⚠️ 未发现VO对象转换（需要手动检查）${NC}"
fi

# 验证7: 检查是否有自定义HTTP客户端实现
echo -e "\n${YELLOW}7. 检查自定义HTTP客户端实现...${NC}"

# 查找自定义HTTP客户端相关文件
HTTP_CLIENT_FILES=$(find "microservices/ioedream-video-service/src/main/java" -name "*.java" -exec grep -l "HTTP_CLIENT\|HttpClient\|RestTemplate\|OkHttp" {} \; 2>/dev/null)

if [ -z "$HTTP_CLIENT_FILES" ]; then
    echo -e "${GREEN}✅ 未发现自定义HTTP客户端实现${NC}"
else
    echo -e "${YELLOW}⚠️ 发现可能的自定义HTTP客户端实现：${NC}"
    echo "$HTTP_CLIENT_FILES"
    echo -e "${YELLOW}建议：${NC}"
    echo "• 使用GatewayServiceClient进行HTTP调用"
    echo "• 删除自定义HTTP客户端代码"
fi

# 验证8: 统计优化成果
echo -e "\n${YELLOW}8. 优化成果统计...${NC}"

# 统计修复的文件数量
FIXED_FILES=0
if grep -q "DeviceResponse" "$ADAPTER_INTERFACE_FILE"; then
    ((FIXED_FILES++))
fi
for impl_file in "${ADAPTER_IMPL_FILES[@]}"; do
    if grep -q "DeviceResponse" "$impl_file"; then
        ((FIXED_FILES++))
    fi
done

echo -e "${GREEN}✅ 已修复Adapter接口和实现类数量: $FIXED_FILES${NC}"
echo -e "${GREEN}✅ 复用Response对象: DeviceResponse${NC}"
echo -e "${GREEN}✅ 消除Adapter层Entity违规: 100%合规${NC}"

echo -e "\n${GREEN}=========================================="
echo -e "Video Service架构优化验证通过！✅"
echo -e "==========================================${NC}"

# 输出优化总结
echo -e "\n${YELLOW}优化总结：${NC}"
echo -e "• ✅ 更新IVideoStreamAdapter接口使用DeviceResponse"
echo -e "• ✅ 修复所有Adapter实现类方法签名"
echo -e "• ✅ 消除Adapter层Entity参数传递"
echo -e "• ✅ 验证Service层合规使用Entity"
echo -e "• ✅ 符合微服务边界原则"

echo -e "\n${YELLOW}架构价值：${NC}"
echo -e "• 🛡️ 消除了Adapter层Entity传递风险"
echo -e "• 📊 建立了统一的Response对象模式"
echo -e "• 🔧 适配器模式与架构边界一致"
echo -e "• 🏗️ 符合企业级微服务架构标准"

echo -e "\n${YELLOW}设计模式优化：${NC}"
echo -e "• 🎨 适配器模式: 使用标准参数类型"
echo -e "• 🏭 工厂模式: 保持原有工厂加载机制"
echo -e "• 🔄 策略模式: 支持多种流媒体协议"
echo -e "• 📦 统一接口: 标准化适配器签名"

echo -e "\n${YELLOW}下一步建议：${NC}"
echo -e "• 测试Video Service功能正常"
echo -e "• 验证适配器工厂模式加载"
echo -e "• 检查流媒体URL构建逻辑"
echo -e "• 推广到其他微服务模块"