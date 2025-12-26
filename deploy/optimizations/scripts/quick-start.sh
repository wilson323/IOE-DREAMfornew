#!/bin/bash
echo "IOE-DREAM 优化服务快速启动器"
echo "===================================="

read -p "请输入服务名 (如: ioedream-gateway-service): " SERVICE_NAME
read -p "请输入内存配置 (small/medium/large): " MEMORY_CONFIG

if [ -z "$SERVICE_NAME" ]; then
    SERVICE_NAME="ioedream-gateway-service"
fi

if [ -z "$MEMORY_CONFIG" ]; then
    MEMORY_CONFIG="medium"
fi

echo
echo "启动配置:"
echo "服务名: $SERVICE_NAME"
echo "内存配置: $MEMORY_CONFIG"
echo

# 执行优化启动脚本
EXEC_SCRIPT_DIR=$(dirname "$0")
"$EXEC_SCRIPT_DIR/start-service-optimized.sh" "$SERVICE_NAME" "$MEMORY_CONFIG" start

echo
echo "服务启动命令:"
echo "java -jar /opt/ioedream/$SERVICE_NAME.jar"
echo
echo "监控地址:"
echo "健康检查: http://localhost:8080/actuator/health"
echo "性能指标: http://localhost:8080/actuator/metrics"
echo "连接池监控: http://localhost:8080/druid/"
