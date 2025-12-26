#!/bin/bash
# =====================================================
# IOE-DREAM 标准Maven构建脚本
# 版本: v1.0.0
# 描述: 使用标准Maven命令按顺序构建所有模块
# =====================================================

set -e

echo "========================================"
echo "  IOE-DREAM 标准Maven构建"
echo "========================================"
echo ""

# 阶段1: 安装父POM
echo "[1/5] 安装父POM..."
mvn clean install -N -DskipTests
if [ $? -ne 0 ]; then
    echo "[FAIL] 父POM安装失败"
    exit 1
fi
echo "[OK] 父POM安装成功"
echo ""

# 阶段2: 构建核心模块
echo "[2/5] 构建核心模块..."
mvn clean install -pl microservices-common-core -am -DskipTests
if [ $? -ne 0 ]; then
    echo "[FAIL] 核心模块构建失败"
    exit 1
fi
echo "[OK] 核心模块构建成功"
echo ""

# 阶段3: 构建功能模块
echo "[3/5] 构建功能模块..."
mvn clean install -pl microservices-common-entity,microservices-common-storage,microservices-common-security,microservices-common-data,microservices-common-cache,microservices-common-monitor,microservices-common-export,microservices-common-workflow,microservices-common-permission -am -DskipTests
if [ $? -ne 0 ]; then
    echo "[FAIL] 功能模块构建失败"
    exit 1
fi
echo "[OK] 功能模块构建成功"
echo ""

# 阶段4: 构建业务模块
echo "[4/5] 构建业务模块..."
mvn clean install -pl microservices-common-business,microservices-common,ioedream-db-init -am -DskipTests
if [ $? -ne 0 ]; then
    echo "[FAIL] 业务模块构建失败"
    exit 1
fi
echo "[OK] 业务模块构建成功"
echo ""

# 阶段5: 构建业务服务
echo "[5/5] 构建业务服务..."
mvn clean install -pl ioedream-gateway-service,ioedream-common-service,ioedream-device-comm-service,ioedream-oa-service,ioedream-access-service,ioedream-attendance-service,ioedream-video-service,ioedream-consume-service,ioedream-visitor-service,ioedream-database-service,ioedream-biometric-service -am -DskipTests
if [ $? -ne 0 ]; then
    echo "[FAIL] 业务服务构建失败"
    exit 1
fi
echo "[OK] 业务服务构建成功"
echo ""

# 最后: 完整项目构建验证
echo "[完成] 完整项目构建验证..."
mvn clean install -DskipTests
if [ $? -ne 0 ]; then
    echo "[FAIL] 完整项目构建失败"
    exit 1
fi
echo "[OK] 完整项目构建成功"
echo ""

echo "========================================"
echo "  构建完成！"
echo "========================================"

