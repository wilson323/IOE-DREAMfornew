#!/bin/bash

echo "========================================="
echo "系统性检查和修复微服务编译错误"
echo "========================================="

# 设置颜色
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 创建编译结果文件
COMPILE_RESULTS="compile_results_$(date +%Y%m%d_%H%M%S).log"
echo "编译开始时间: $(date)" > $COMPILE_RESULTS

# 微服务列表
services=(
    "ioedream-access-service"
    "ioedream-attendance-service"
    "ioedream-audit-service"
    "ioedream-auth-service"
    "ioedream-config-service"
    "ioedream-consume-service"
    "ioedream-device-service"
    "ioedream-enterprise-service"
    "ioedream-gateway-service"
    "ioedream-identity-service"
    "ioedream-infrastructure-service"
    "ioedream-integration-service"
    "ioedream-monitor-service"
    "ioedream-notification-service"
    "ioedream-oa-service"
    "ioedream-report-service"
    "ioedream-scheduler-service"
    "ioedream-system-service"
    "ioedream-video-service"
    "ioedream-visitor-service"
)

success_count=0
failure_count=0
total_count=${#services[@]}

echo "总服务数量: $total_count"
echo "开始编译检查..."
echo "" | tee -a $COMPILE_RESULTS

# 检查每个服务的编译状态
for service in "${services[@]}"; do
    echo "========================================"
    echo "检查服务: $service"
    echo "========================================" | tee -a $COMPILE_RESULTS

    if [ -d "$service" ]; then
        cd "$service"

        # 尝试编译
        echo "正在编译 $service..." | tee -a ../$COMPILE_RESULTS

        if mvn clean compile -q >> ../$COMPILE_RESULTS 2>&1; then
            echo -e "${GREEN}✓ $service 编译成功${NC}"
            echo "SUCCESS: $service 编译成功" >> ../$COMPILE_RESULTS
            ((success_count++))
        else
            echo -e "${RED}✗ $service 编译失败${NC}"
            echo "FAILURE: $service 编译失败" >> ../$COMPILE_RESULTS

            # 显示具体错误
            echo -e "${YELLOW}编译错误详情:${NC}"
            mvn compile 2>&1 | head -20

            echo "" | tee -a ../$COMPILE_RESULTS
            echo "详细错误信息:" >> ../$COMPILE_RESULTS
            mvn compile >> ../$COMPILE_RESULTS 2>&1
            echo "" >> ../$COMPILE_RESULTS

            ((failure_count++))
        fi

        cd ..
        echo "" | tee -a $COMPILE_RESULTS
    else
        echo -e "${YELLOW}⚠ 服务目录不存在: $service${NC}"
        echo "MISSING: 服务目录不存在 $service" >> $COMPILE_RESULTS
        ((failure_count++))
    fi
done

echo "========================================"
echo "编译结果统计"
echo "========================================" | tee -a $COMPILE_RESULTS
echo -e "${GREEN}编译成功: $success_count${NC}"
echo -e "${RED}编译失败: $failure_count${NC}"
echo -e "${YELLOW}总计: $total_count${NC}"
echo "成功率: $((success_count * 100 / total_count))%" | tee -a $COMPILE_RESULTS

echo "编译完成时间: $(date)" >> $COMPILE_RESULTS
echo "详细日志保存在: $COMPILE_RESULTS"

if [ $failure_count -eq 0 ]; then
    echo -e "${GREEN}🎉 所有服务编译成功！${NC}"
else
    echo -e "${RED}⚠️  有 $failure_count 个服务需要修复${NC}"
fi