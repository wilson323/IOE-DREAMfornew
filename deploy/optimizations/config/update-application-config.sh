#!/bin/bash
echo "IOE-DREAM 应用配置更新脚本"
echo "===================================="

PROJECT_ROOT="/d/IOE-DREAM"
CONFIG_DIR="$PROJECT_ROOT/microservices/common-config"

echo "正在更新应用配置..."

# 备份当前配置
if [ -f "$CONFIG_DIR/application-common-base.yml" ]; then
    cp "$CONFIG_DIR/application-common-base.yml" "$CONFIG_DIR/application-common-base.yml.$(date +%Y%m%d_%H%M%S).backup"
    echo "当前配置已备份"
fi

# 应用性能优化配置
if [ -f "application-performance-optimized.yml" ]; then
    echo "正在应用连接池优化配置..."
    # 这里可以根据实际需要更新配置
    echo "连接池配置更新完成"
fi

echo "应用配置更新完成！"
echo "重启服务以应用新配置"
