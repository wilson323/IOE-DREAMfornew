#!/bin/bash

# =====================================================
# IOE-DREAM 数据库服务启动脚本
# 版本: 1.0.0
# 说明: 启动数据库初始化和同步服务
# =====================================================

set -e

# 服务配置
SERVICE_NAME="ioedream-database-service"
SERVICE_PORT="8889"
JAR_FILE="ioedream-database-service-1.0.0.jar"
APP_HOME="/opt/ioedream"
LOG_DIR="${APP_HOME}/logs"
PID_FILE="${APP_HOME}/${SERVICE_NAME}.pid"
JVM_OPTS="-Xms1g -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${LOG_DIR}"

# 环境变量配置
export SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-"dev"}
export NACOS_SERVER_ADDR=${NACOS_SERVER_ADDR:-"127.0.0.1:8848"}
export NACOS_NAMESPACE=${NACOS_NAMESPACE:-"dev"}
export NACOS_GROUP=${NACOS_GROUP:-"IOE-DATABASE"}

# 数据库连接配置
export DATABASE_URL=${DATABASE_URL:-"jdbc:mysql://127.0.0.1:3306/ioedream_database?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai"}
export DATABASE_USERNAME=${DATABASE_USERNAME:-"root"}
export DATABASE_PASSWORD=${DATABASE_PASSWORD:-"123456"}

# 应用配置
export DATABASE_SYNC_ENABLED=${DATABASE_SYNC_ENABLED:-"true"}
export DATABASE_SYNC_AUTO_STARTUP=${DATABASE_SYNC_AUTO_STARTUP:-"true"}
export DATABASE_SYNC_CHECK_INTERVAL=${DATABASE_SYNC_CHECK_INTERVAL:-"30000"}

echo "🚀 [启动脚本] 开始启动 ${SERVICE_NAME}..."
echo "📋 [启动脚本] 服务配置信息:"
echo "   - 服务名称: ${SERVICE_NAME}"
echo "   - 服务端口: ${SERVICE_PORT}"
echo "   - Spring环境: ${SPRING_PROFILES_ACTIVE}"
echo "   - Nacos地址: ${NACOS_SERVER_ADDR}"
echo "   - 数据库同步: ${DATABASE_SYNC_ENABLED}"
echo "   - 自动启动同步: ${DATABASE_SYNC_AUTO_STARTUP}"

# 创建必要的目录
mkdir -p ${LOG_DIR}
mkdir -p ${APP_HOME}/temp

# 检查JAR文件是否存在
if [ ! -f "${APP_HOME}/${JAR_FILE}" ]; then
    echo "❌ [启动脚本] JAR文件不存在: ${APP_HOME}/${JAR_FILE}"
    exit 1
fi

# 检查服务是否已经运行
if [ -f ${PID_FILE} ]; then
    PID=$(cat ${PID_FILE})
    if ps -p ${PID} > /dev/null 2>&1; then
        echo "⚠️ [启动脚本] 服务已在运行中，PID: ${PID}"
        exit 1
    else
        echo "🧹 [启动脚本] 清理旧的PID文件"
        rm -f ${PID_FILE}
    fi
fi

# 设置Java环境
if command -v java >/dev/null 2>&1; then
    JAVA_CMD="java"
else
    if [ -z "${JAVA_HOME}" ]; then
        echo "❌ [启动脚本] Java环境未配置，请设置JAVA_HOME或确保java在PATH中"
        exit 1
    fi
    JAVA_CMD="${JAVA_HOME}/bin/java"
fi

# 检查Java版本
JAVA_VERSION=$(${JAVA_CMD} -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "${JAVA_VERSION}" -lt "17" ]; then
    echo "❌ [启动脚本] 需要Java 17或更高版本，当前版本: ${JAVA_VERSION}"
    exit 1
fi

echo "✅ [启动脚本] 环境检查通过，开始启动服务..."

# 启动服务
echo "🔄 [启动脚本] 正在启动 ${SERVICE_NAME}..."

nohup ${JAVA_CMD} ${JVM_OPTS} \
    -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE} \
    -Dserver.port=${SERVICE_PORT} \
    -jar ${APP_HOME}/${JAR_FILE} \
    > ${LOG_DIR}/${SERVICE_NAME}.out 2>&1 &

# 获取进程ID
PID=$!
echo ${PID} > ${PID_FILE}

# 等待服务启动
echo "⏳ [启动脚本] 等待服务启动..."
sleep 10

# 检查服务是否启动成功
if ps -p ${PID} > /dev/null 2>&1; then
    echo "✅ [启动脚本] ${SERVICE_NAME} 启动成功，PID: ${PID}"
    echo "📊 [启动脚本] 服务信息:"
    echo "   - PID: ${PID}"
    echo "   - 端口: ${SERVICE_PORT}"
    echo "   - 日志文件: ${LOG_DIR}/${SERVICE_NAME}.out"
    echo "   - 健康检查: http://localhost:${SERVICE_PORT}/database/api/v1/database/sync/health"
    echo ""
    echo "🔍 [启动脚本] 查看日志: tail -f ${LOG_DIR}/${SERVICE_NAME}.out"
    echo "🛑 [启动脚本] 停止服务: kill \$(cat ${PID_FILE})"
else
    echo "❌ [启动脚本] ${SERVICE_NAME} 启动失败"
    echo "📄 [启动脚本] 请查看日志文件: ${LOG_DIR}/${SERVICE_NAME}.out"
    rm -f ${PID_FILE}
    exit 1
fi

echo "🎉 [启动脚本] ${SERVICE_NAME} 启动完成！"