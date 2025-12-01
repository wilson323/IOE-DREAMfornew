#!/bin/bash
# IOE-DREAM Device Service 部署脚本
# 基于现有项目部署模式重构，包含MQTT服务
#
# @author IOE-DREAM Team
# @since 2025-11-27

set -e

# 配置变量
PROJECT_NAME="ioedream-device-service"
DOCKER_REGISTRY="localhost"
DOCKER_TAG="latest"
NAMESPACE="ioedream"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查依赖
check_dependencies() {
    log_info "检查部署依赖..."

    # 检查Docker
    if ! command -v docker &> /dev/null; then
        log_error "Docker未安装或未在PATH中"
        exit 1
    fi

    # 检查Docker Compose
    if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
        log_error "Docker Compose未安装或未在PATH中"
        exit 1
    fi

    # 检查Maven
    if ! command -v mvn &> /dev/null; then
        log_error "Maven未安装或未在PATH中"
        exit 1
    fi

    # 检查mosquitto-clients（MQTT客户端工具）
    if ! command -v mosquitto_pub &> /dev/null; then
        log_warning "mosquitto-clients未安装，将无法进行MQTT功能测试"
    fi

    log_success "依赖检查通过"
}

# 构建项目
build_project() {
    log_info "构建Device Service项目..."

    # 检查POM文件
    if [ ! -f "pom.xml" ]; then
        log_error "未找到pom.xml文件"
        exit 1
    fi

    # 编译项目
    mvn clean package -DskipTests -q
    if [ $? -ne 0 ]; then
        log_error "项目构建失败"
        exit 1
    fi

    # 检查JAR文件
    if [ ! -f "target/*.jar" ]; then
        log_error "未找到编译后的JAR文件"
        exit 1
    fi

    log_success "项目构建完成"
}

# 构建Docker镜像
build_docker_image() {
    log_info "构建Docker镜像..."

    # 构建镜像
    docker build -t ${PROJECT_NAME}:${DOCKER_TAG} .
    if [ $? -ne 0 ]; then
        log_error "Docker镜像构建失败"
        exit 1
    fi

    # 标记镜像
    docker tag ${PROJECT_NAME}:${DOCKER_TAG} ${DOCKER_REGISTRY}/${PROJECT_NAME}:${DOCKER_TAG}

    log_success "Docker镜像构建完成"
}

# 启动服务
start_services() {
    log_info "启动Device Service服务..."

    # 创建必要的目录
    mkdir -p logs config/device config/mysql config/redis config/prometheus config/grafana config/mqtt

    # 创建配置文件
    create_config_files

    # 启动服务
    if command -v docker-compose &> /dev/null; then
        docker-compose up -d
    else
        docker compose up -d
    fi

    if [ $? -ne 0 ]; then
        log_error "服务启动失败"
        exit 1
    fi

    log_success "服务启动完成"
}

# 创建配置文件
create_config_files() {
    log_info "创建配置文件..."

    # MQTT配置文件
    cat > config/mqtt/mosquitto.conf << 'EOF'
# MQTT Broker配置
listener 1883
allow_anonymous false
password_file /mosquitto/config/passwd

# 持久化配置
persistence true
persistence_location /mosquitto/data/

# 日志配置
log_dest file /mosquitto/log/mosquitto.log
log_type error
log_type warning
log_type notice
log_type information

# 连接配置
max_connections 1000
max_inflight_messages 100

# WebSocket支持
listener 9001
protocol websockets
EOF

    # MQTT用户密码文件
    echo "device-service:device-service-password" > config/mqtt/passwd

    # Prometheus设备规则
    cat > config/prometheus/device-rules.yml << 'EOF'
groups:
  - name: device.rules
    rules:
      - alert: DeviceOfflineCount
        expr: device_offline_count > 10
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "设备离线数量过多"
          description: "离线设备数量为 {{ $value }}，超过阈值10"

      - alert: DeviceFaultCount
        expr: device_fault_count > 5
        for: 2m
        labels:
          severity: critical
        annotations:
          summary: "设备故障数量过多"
          description: "故障设备数量为 {{ $value }}，超过阈值5"

      - alert: DeviceHeartbeatTimeout
        expr: device_heartbeat_timeout_count > 20
        for: 3m
        labels:
          severity: warning
        annotations:
          summary: "设备心跳超时数量过多"
          description: "心跳超时设备数量为 {{ $value }}，超过阈值20"
EOF

    # Grafana设备仪表板配置
    cat > config/grafana/device-dashboard.json << 'EOF'
{
  "dashboard": {
    "id": null,
    "title": "IOE-DREAM 设备监控",
    "tags": ["device", "ioedream"],
    "timezone": "browser",
    "panels": [
      {
        "id": "device-status",
        "title": "设备状态统计",
        "type": "stat",
        "targets": [
          {
            "expr": "device_online_count",
            "refId": "device_online_count"
          },
          {
            "expr": "device_offline_count",
            "refId": "device_offline_count"
          },
          {
            "expr": "device_fault_count",
            "refId": "device_fault_count"
          }
        ],
        "fieldConfig": {
          "defaults": {
            "unit": "台"
          }
        }
      }
    ],
    "time": {
      "from": "now-1h",
      "to": "now"
    },
    "refresh": "5s"
  }
}
EOF

    log_success "配置文件创建完成"
}

# 等待服务就绪
wait_for_services() {
    log_info "等待服务就绪..."

    # 等待MySQL启动
    log_info "等待MySQL启动..."
    timeout 60 bash -c 'until docker exec ioedream-mysql mysqladmin ping -h localhost --silent; do sleep 2; done'

    # 等待Redis启动
    log_info "等待Redis启动..."
    timeout 30 bash -c 'until docker exec ioedream-redis redis-cli ping; do sleep 2; done'

    # 等待Nacos启动
    log_info "等待Nacos启动..."
    timeout 120 bash -c 'until curl -f http://localhost:8848/nacos; do sleep 5; done'

    # 等待MQTT启动
    log_info "等待MQTT启动..."
    timeout 30 bash -c 'until docker exec ioedream-mqtt mosquitto_pub -h localhost -t test -m "mqtt-test" > /dev/null 2>&1; do sleep 2; done'

    # 等待Device Service启动
    log_info "等待Device Service启动..."
    timeout 120 bash -c 'until curl -f http://localhost:8082/api/device/health; do sleep 5; done'

    log_success "所有服务已就绪"
}

# MQTT功能测试
test_mqtt_functionality() {
    log_info "测试MQTT功能..."

    if command -v mosquitto_pub &> /dev/null; then
        # 测试MQTT连接
        mosquitto_pub -h localhost -t "test/device" -m "Device Service MQTT Test" -u device-service -P device-service-password
        log_info "MQTT连接测试完成"

        # 测试MQTT订阅
        mosquitto_sub -h localhost -t "device/+/status" -C 1 -u device-service -P device-service-password &
        MQTT_SUB_PID=$!
        sleep 2
        kill $MQTT_SUB_PID 2>/dev/null || true
        log_info "MQTT订阅测试完成"
    else
        log_warning "跳过MQTT功能测试（mosquitto-clients未安装）"
    fi

    log_success "MQTT功能测试完成"
}

# 健康检查
health_check() {
    log_info "执行健康检查..."

    # 检查Device Service健康状态
    health_response=$(curl -s -w "%{http_code}" http://localhost:8082/api/device/health)
    http_code="${health_response: -3}"

    if [ "$http_code" = "200" ]; then
        log_success "Device Service健康检查通过"
    else
        log_error "Device Service健康检查失败，HTTP状态码: $http_code"
        exit 1
    fi

    # 检查MQTT连接状态
    if docker exec ioedream-mqtt mosquitto_pub -h localhost -t "health-check" -m "test" -u device-service -P device-service-password > /dev/null 2>&1; then
        log_success "MQTT服务健康检查通过"
    else
        log_error "MQTT服务健康检查失败"
    fi

    # 检查服务状态
    if command -v docker-compose &> /dev/null; then
        docker-compose ps
    else
        docker compose ps
    fi

    log_success "健康检查完成"
}

# 显示服务信息
show_service_info() {
    log_success "Device Service部署成功！"
    echo ""
    echo "服务访问地址:"
    echo "  Device Service: http://localhost:8082"
    echo "  API文档: http://localhost:8082/doc.html"
    echo "  Nacos控制台: http://localhost:8848/nacos"
    echo "  MQTT Broker: tcp://localhost:1883"
    echo "  MQTT Web UI: http://localhost:9000 (device-service/device-service-password)"
    echo "  Prometheus: http://localhost:9090"
    echo "  Grafana: http://localhost:3000 (admin/admin123)"
    echo ""
    echo "健康检查:"
    echo "  Device Service: curl http://localhost:8082/api/device/health"
    echo "  MQTT连接测试: mosquitto_pub -h localhost -t test -m \"test\" -u device-service -P device-service-password"
    echo ""
    echo "设备监控:"
    echo "  设备状态API: curl http://localhost:8082/api/device/statistics/status"
    echo "  在线设备列表: curl http://localhost:8082/api/device/online"
    echo "  离线设备列表: curl http://localhost:8082/api/device/offline"
    echo ""
    echo "MQTT主题示例:"
    echo "  设备状态主题: device/+/status"
    echo "  设备心跳主题: device/+/heartbeat"
    echo "  设备故障主题: device/+/fault"
    echo "  设备命令主题: device/+/command"
    echo ""
    echo "停止服务:"
    echo "  ./scripts/stop.sh"
    echo ""
    echo "日志查看:"
    echo "  Device Service: docker logs -f ioedream-device-service"
    echo "  MQTT Broker: docker logs -f ioedream-mqtt"
    echo ""
    echo "设备管理接口:"
    echo "  获取设备列表: curl http://localhost:8082/api/device/page"
    echo "  设备状态统计: curl http://localhost:8082/api/device/statistics/health"
}

# 主函数
main() {
    echo "========================================"
    echo "IOE-DREAM Device Service 自动部署"
    echo "========================================"
    echo ""

    # 检查当前目录
    if [ ! -f "pom.xml" ] || [ ! -f "Dockerfile" ]; then
        log_error "请在项目根目录执行此脚本"
        exit 1
    fi

    # 执行部署步骤
    check_dependencies
    build_project
    build_docker_image
    start_services
    wait_for_services
    test_mqtt_functionality
    health_check
    show_service_info
}

# 处理中断信号
trap 'log_warning "部署被中断"; exit 1' INT TERM

# 执行主函数
main "$@"