#!/bin/bash

echo "========================================="
echo "系统性编译所有微服务 - 检查修复进度"
echo "========================================"

# 微服务列表
services=(
    "ioedream-access-service"
    "ioedream-attendance-service"
    "ioedream-auth-service"
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

for service in "${services[@]}"; do
    echo ""
    echo "正在编译 $service..."

    if cd "D:\IOE-DREAM\microservices\$service" && mvn clean compile -q > /dev/null 2>&1; then
        echo "SUCCESS: $service 编译成功"
        ((success_count++))
    else
        echo "FAILURE: $service 编译失败"
        ((failure_count))

        # 显示具体错误
        echo "详细错误信息:"
        cd "D:\IOE-DREAM\microservices\$service" && mvn clean compile 2>&1 | head -20
    fi
done

echo ""
echo "========================================="
echo "编译结果汇总："
echo "成功: $success_count 个服务"
echo "失败: $failure_count 个服务"
echo "总计: $total_count 个服务"
echo "成功率: $(( success_count * 100 / total_count ))%"
echo "========================================="