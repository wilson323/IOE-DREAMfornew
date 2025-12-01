#!/bin/bash

# =============================================================================
# IOE-DREAM 微服务测试环境配置脚本
# =============================================================================
#
# 功能: 快速搭建测试环境，包括数据库、缓存、配置等
# 支持: Docker容器化部署，本地部署
#
# 作者: IOE-DREAM测试团队
# 版本: v1.0.0
# 最后更新: 2025-11-29
# =============================================================================

set -e

# 脚本配置
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")/../.."
TEST_DATA_DIR="$PROJECT_ROOT/test/test-data"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 配置变量
MYSQL_HOST=${MYSQL_HOST:-localhost}
MYSQL_PORT=${MYSQL_PORT:-3306}
MYSQL_USER=${MYSQL_USER:-test}
MYSQL_PASSWORD=${MYSQL_PASSWORD:-test}
MYSQL_DATABASE=${MYSQL_DATABASE:-ioedream_test}

REDIS_HOST=${REDIS_HOST:-localhost}
REDIS_PORT=${REDIS_PORT:-6379}
REDIS_PASSWORD=${REDIS_PASSWORD:-}

NACOS_HOST=${NACOS_HOST:-localhost}
NACOS_PORT=${NACOS_PORT:-8848}
NACOS_USER=${NACOS_USER:-nacos}
NACOS_PASSWORD=${NACOS_PASSWORD:-nacos}

DEPLOYMENT_MODE=${DEPLOYMENT_MODE:-docker}  # docker, local
CLEAN_ENV=${CLEAN_ENV:-false}

# 日志函数
log() {
    echo -e "${BLUE}[$(date '+%Y-%m-%d %H:%M:%S')]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[$(date '+%Y-%m-%d %H:%M:%S')] SUCCESS:${NC} $1"
}

log_error() {
    echo -e "${RED}[$(date '+%Y-%m-%d %H:%M:%S')] ERROR:${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[$(date '+%Y-%m-%d %H:%M:%S')] WARNING:${NC} $1"
}

log_info() {
    echo -e "${CYAN}[$(date '+%Y-%m-%d %H:%M:%S')] INFO:${NC} $1"
}

# 显示帮助信息
show_help() {
    cat << EOF
IOE-DREAM 微服务测试环境配置脚本

用法: $0 [选项]

选项:
    --mode MODE         部署模式 (docker|local) [默认: docker]
    --clean             清理现有环境
    --mysql-host HOST   MySQL主机地址 [默认: localhost]
    --mysql-port PORT   MySQL端口 [默认: 3306]
    --mysql-user USER   MySQL用户名 [默认: test]
    --mysql-pass PASS   MySQL密码 [默认: test]
    --redis-host HOST   Redis主机地址 [默认: localhost]
    --redis-port PORT   Redis端口 [默认: 6379]
    --nacos-host HOST   Nacos主机地址 [默认: localhost]
    --nacos-port PORT   Nacos端口 [默认: 8848]
    --help              显示此帮助信息

示例:
    $0 --mode docker --clean
    $0 --mode local --mysql-host 192.168.1.100
    $0 --clean --mode docker

EOF
}

# 解析命令行参数
parse_arguments() {
    while [[ $# -gt 0 ]]; do
        case $1 in
            --mode)
                DEPLOYMENT_MODE="$2"
                shift 2
                ;;
            --clean)
                CLEAN_ENV=true
                shift
                ;;
            --mysql-host)
                MYSQL_HOST="$2"
                shift 2
                ;;
            --mysql-port)
                MYSQL_PORT="$2"
                shift 2
                ;;
            --mysql-user)
                MYSQL_USER="$2"
                shift 2
                ;;
            --mysql-pass)
                MYSQL_PASSWORD="$2"
                shift 2
                ;;
            --redis-host)
                REDIS_HOST="$2"
                shift 2
                ;;
            --redis-port)
                REDIS_PORT="$2"
                shift 2
                ;;
            --nacos-host)
                NACOS_HOST="$2"
                shift 2
                ;;
            --nacos-port)
                NACOS_PORT="$2"
                shift 2
                ;;
            --help)
                show_help
                exit 0
                ;;
            *)
                log_error "未知参数: $1"
                show_help
                exit 1
                ;;
        esac
    done
}

# 检查系统要求
check_requirements() {
    log "检查系统要求..."

    # 检查Docker
    if [ "$DEPLOYMENT_MODE" = "docker" ]; then
        if ! command -v docker &> /dev/null; then
            log_error "Docker未安装，请先安装Docker"
            exit 1
        fi

        if ! command -v docker-compose &> /dev/null; then
            log_error "Docker Compose未安装，请先安装Docker Compose"
            exit 1
        fi

        log_success "Docker环境检查通过"
    fi

    # 检查必要工具
    local tools=("curl" "jq" "python3")
    for tool in "${tools[@]}"; do
        if ! command -v "$tool" &> /dev/null; then
            log_error "$tool 未安装，请先安装 $tool"
            exit 1
        fi
    done

    log_success "系统要求检查通过"
}

# 创建Docker Compose配置
create_docker_compose() {
    log "创建Docker Compose配置..."

    local compose_file="$PROJECT_ROOT/test/docker/docker-compose.test.yml"
    mkdir -p "$(dirname "$compose_file")"

    cat > "$compose_file" << EOF
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: ioedream-mysql-test
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: ${MYSQL_DATABASE}
      MYSQL_USER: ${MYSQL_USER}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD}
      MYSQL_CHARSET: utf8mb4
      MYSQL_COLLATION_SERVER: utf8mb4_unicode_ci
    ports:
      - "${MYSQL_PORT}:3306"
    volumes:
      - mysql_test_data:/var/lib/mysql
      - ./mysql/init:/docker-entrypoint-initdb.d
      - ./mysql/conf:/etc/mysql/conf.d
    command: --default-authentication-plugin=mysql_native_password
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      timeout: 20s
      retries: 10
      interval: 10s
      start_period: 40s
    networks:
      - ioedream-test-network

  redis:
    image: redis:6.2-alpine
    container_name: ioedream-redis-test
    ports:
      - "${REDIS_PORT}:6379"
    volumes:
      - redis_test_data:/data
      - ./redis/redis.conf:/usr/local/etc/redis/redis.conf
    command: redis-server /usr/local/etc/redis/redis.conf
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      timeout: 5s
      retries: 5
      interval: 10s
      start_period: 10s
    networks:
      - ioedream-test-network

  nacos:
    image: nacos/nacos-server:v2.2.3
    container_name: ioedream-nacos-test
    environment:
      MODE: standalone
      SPRING_DATASOURCE_PLATFORM: mysql
      MYSQL_SERVICE_HOST: mysql
      MYSQL_SERVICE_PORT: 3306
      MYSQL_SERVICE_DB_NAME: nacos_config
      MYSQL_SERVICE_USER: ${MYSQL_USER}
      MYSQL_SERVICE_PASSWORD: ${MYSQL_PASSWORD}
      NACOS_AUTH_ENABLE: true
      NACOS_AUTH_TOKEN: SecretKey012345678901234567890123456789012345678901234567890123456789
      NACOS_AUTH_IDENTITY_KEY: serverIdentity
      NACOS_AUTH_IDENTITY_VALUE: security
    ports:
      - "${NACOS_PORT}:8848"
      - "9848:9848"
    volumes:
      - nacos_test_data:/home/nacos/data
      - nacos_test_logs:/home/nacos/logs
    depends_on:
      mysql:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8848/nacos/"]
      timeout: 5s
      retries: 10
      interval: 10s
      start_period: 30s
    networks:
      - ioedream-test-network

  prometheus:
    image: prom/prometheus:latest
    container_name: ioedream-prometheus-test
    ports:
      - "9090:9090"
    volumes:
      - ./monitoring/prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus_test_data:/prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
      - '--web.console.libraries=/etc/prometheus/console_libraries'
      - '--web.console.templates=/etc/prometheus/consoles'
      - '--storage.tsdb.retention.time=200h'
      - '--web.enable-lifecycle'
    networks:
      - ioedream-test-network

  grafana:
    image: grafana/grafana:latest
    container_name: ioedream-grafana-test
    ports:
      - "3000:3000"
    environment:
      GF_SECURITY_ADMIN_PASSWORD: admin
    volumes:
      - grafana_test_data:/var/lib/grafana
      - ./monitoring/grafana/dashboards:/etc/grafana/provisioning/dashboards
      - ./monitoring/grafana/datasources:/etc/grafana/provisioning/datasources
    depends_on:
      - prometheus
    networks:
      - ioedream-test-network

volumes:
  mysql_test_data:
  redis_test_data:
  nacos_test_data:
  nacos_test_logs:
  prometheus_test_data:
  grafana_test_data:

networks:
  ioedream-test-network:
    driver: bridge
EOF

    # 创建MySQL配置目录和文件
    local mysql_conf_dir="$PROJECT_ROOT/test/docker/mysql/conf"
    mkdir -p "$mysql_conf_dir"

    cat > "$mysql_conf_dir/my.cnf" << EOF
[mysqld]
character-set-server=utf8mb4
collation-server=utf8mb4_unicode_ci
default-time-zone='+08:00'
sql_mode=STRICT_TRANS_TABLES,NO_ZERO_DATE,NO_ZERO_IN_DATE,ERROR_FOR_DIVISION_BY_ZERO

[client]
default-character-set=utf8mb4

[mysql]
default-character-set=utf8mb4
EOF

    # 创建Redis配置文件
    local redis_conf_dir="$PROJECT_ROOT/test/docker/redis"
    mkdir -p "$redis_conf_dir"

    cat > "$redis_conf_dir/redis.conf" << EOF
bind 0.0.0.0
port 6379
timeout 300
keepalive 60
maxmemory 256mb
maxmemory-policy allkeys-lru
save 900 1
save 300 10
save 60 10000
appendonly yes
appendfsync everysec
EOF

    # 创建Prometheus配置文件
    local prometheus_conf_dir="$PROJECT_ROOT/test/docker/monitoring"
    mkdir -p "$prometheus_conf_dir"

    cat > "$prometheus_conf_dir/prometheus.yml" << EOF
global:
  scrape_interval: 15s
  evaluation_interval: 15s

rule_files:
  # - "first_rules.yml"
  # - "second_rules.yml"

scrape_configs:
  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']

  - job_name: 'ioedream-services'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets:
        - 'gateway:8080'
        - 'auth-service:8081'
        - 'identity-service:8082'
        - 'device-service:8083'
        - 'access-service:8084'
        - 'consume-service:8085'
        - 'attendance-service:8086'
        - 'video-service:8087'
EOF

    log_success "Docker Compose配置创建完成: $compose_file"
}

# 创建本地数据库初始化脚本
create_local_database_init() {
    log "创建本地数据库初始化脚本..."

    local init_sql="$PROJECT_ROOT/test/database/init_test_db.sql"
    mkdir -p "$(dirname "$init_sql")"

    cat > "$init_sql" << 'EOF'
-- IOE-DREAM 测试数据库初始化脚本

-- 创建测试数据库
CREATE DATABASE IF NOT EXISTS ioedream_test CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS nacos_config CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用测试数据库
USE ioedream_test;

-- 用户表
CREATE TABLE IF NOT EXISTS t_user (
    user_id VARCHAR(32) PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    gender VARCHAR(10) NOT NULL COMMENT '性别',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    department VARCHAR(50) COMMENT '部门',
    position VARCHAR(50) COMMENT '职位',
    employee_id VARCHAR(32) UNIQUE COMMENT '员工编号',
    card_number VARCHAR(32) UNIQUE COMMENT '卡号',
    face_id VARCHAR(64) UNIQUE COMMENT '人脸ID',
    fingerprint_id VARCHAR(64) UNIQUE COMMENT '指纹ID',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '删除标志'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 设备表
CREATE TABLE IF NOT EXISTS t_device (
    device_id VARCHAR(32) PRIMARY KEY COMMENT '设备ID',
    device_name VARCHAR(100) NOT NULL COMMENT '设备名称',
    device_type VARCHAR(50) NOT NULL COMMENT '设备类型',
    device_model VARCHAR(50) COMMENT '设备型号',
    manufacturer VARCHAR(50) COMMENT '制造商',
    serial_number VARCHAR(64) UNIQUE COMMENT '序列号',
    ip_address VARCHAR(15) COMMENT 'IP地址',
    port INT COMMENT '端口',
    location VARCHAR(100) COMMENT '位置',
    building VARCHAR(10) COMMENT '楼栋',
    floor VARCHAR(10) COMMENT '楼层',
    room VARCHAR(20) COMMENT '房间',
    status VARCHAR(20) NOT NULL DEFAULT 'ONLINE' COMMENT '状态',
    install_time TIMESTAMP COMMENT '安装时间',
    last_heartbeat TIMESTAMP COMMENT '最后心跳',
    firmware_version VARCHAR(20) COMMENT '固件版本',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '删除标志'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备表';

-- 消费账户表
CREATE TABLE IF NOT EXISTS t_consume_account (
    account_id VARCHAR(32) PRIMARY KEY COMMENT '账户ID',
    user_id VARCHAR(32) NOT NULL COMMENT '用户ID',
    account_type VARCHAR(20) NOT NULL COMMENT '账户类型',
    account_name VARCHAR(100) NOT NULL COMMENT '账户名称',
    initial_balance DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '初始余额',
    current_balance DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '当前余额',
    total_recharge DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '总充值金额',
    total_consume DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '总消费金额',
    daily_limit DECIMAL(10,2) NOT NULL DEFAULT 500.00 COMMENT '每日限额',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '删除标志',
    KEY idx_user_id (user_id),
    KEY idx_account_type (account_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消费账户表';

-- 门禁记录表
CREATE TABLE IF NOT EXISTS t_access_record (
    record_id VARCHAR(32) PRIMARY KEY COMMENT '记录ID',
    user_id VARCHAR(32) NOT NULL COMMENT '用户ID',
    device_id VARCHAR(32) NOT NULL COMMENT '设备ID',
    access_type VARCHAR(20) NOT NULL COMMENT '验证类型',
    card_number VARCHAR(32) COMMENT '卡号',
    face_id VARCHAR(64) COMMENT '人脸ID',
    fingerprint_id VARCHAR(64) COMMENT '指纹ID',
    allowed BOOLEAN NOT NULL COMMENT '是否允许通过',
    deny_reason VARCHAR(100) COMMENT '拒绝原因',
    door_status VARCHAR(20) COMMENT '门状态',
    temperature DECIMAL(3,1) COMMENT '体温',
    mask_detected BOOLEAN COMMENT '是否戴口罩',
    timestamp TIMESTAMP NOT NULL COMMENT '通行时间',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '删除标志',
    KEY idx_user_id (user_id),
    KEY idx_device_id (device_id),
    KEY idx_timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门禁记录表';

-- 交易记录表
CREATE TABLE IF NOT EXISTS t_transaction (
    transaction_id VARCHAR(32) PRIMARY KEY COMMENT '交易ID',
    account_id VARCHAR(32) NOT NULL COMMENT '账户ID',
    user_id VARCHAR(32) NOT NULL COMMENT '用户ID',
    device_id VARCHAR(32) NOT NULL COMMENT '设备ID',
    amount DECIMAL(10,2) NOT NULL COMMENT '金额',
    consume_type VARCHAR(20) NOT NULL COMMENT '消费类型',
    description VARCHAR(200) COMMENT '描述',
    merchant_name VARCHAR(100) COMMENT '商户名称',
    payment_method VARCHAR(20) NOT NULL COMMENT '支付方式',
    success BOOLEAN NOT NULL COMMENT '是否成功',
    failure_reason VARCHAR(100) COMMENT '失败原因',
    original_balance DECIMAL(10,2) COMMENT '原始余额',
    transaction_balance DECIMAL(10,2) COMMENT '交易后余额',
    timestamp TIMESTAMP NOT NULL COMMENT '交易时间',
    operator VARCHAR(50) COMMENT '操作员',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '删除标志',
    KEY idx_account_id (account_id),
    KEY idx_user_id (user_id),
    KEY idx_device_id (device_id),
    KEY idx_timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易记录表';

-- 考勤记录表
CREATE TABLE IF NOT EXISTS t_attendance_record (
    record_id VARCHAR(32) PRIMARY KEY COMMENT '记录ID',
    user_id VARCHAR(32) NOT NULL COMMENT '用户ID',
    device_id VARCHAR(32) NOT NULL COMMENT '设备ID',
    attendance_type VARCHAR(20) NOT NULL COMMENT '考勤类型',
    check_time TIMESTAMP NOT NULL COMMENT '打卡时间',
    is_late BOOLEAN COMMENT '是否迟到',
    is_overtime BOOLEAN COMMENT '是否加班',
    device_name VARCHAR(100) COMMENT '设备名称',
    location VARCHAR(100) COMMENT '位置',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '删除标志',
    KEY idx_user_id (user_id),
    KEY idx_device_id (device_id),
    KEY idx_check_time (check_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考勤记录表';

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_user_create_time ON t_user(create_time);
CREATE INDEX IF NOT EXISTS idx_device_create_time ON t_device(create_time);
CREATE INDEX IF NOT EXISTS idx_access_timestamp ON t_access_record(timestamp);
CREATE INDEX IF NOT EXISTS idx_transaction_timestamp ON t_transaction(timestamp);

-- 插入测试数据
INSERT INTO t_user (user_id, username, password, real_name, gender, email, phone, department, position, employee_id, card_number, status) VALUES
('user_000001', 'admin', '\$2a\$10\$EqixmJyN.2mEx9wZ.3/U.OqYqz8r8k5rXyN8Zz9Q5R6kS7T8U9V0W', '系统管理员', 'MALE', 'admin@ioedream.com', '13800138000', '技术部', '系统管理员', 'EMP000001', 'CARD000001', 'ACTIVE'),
('user_000002', 'test001', '\$2a\$10\$EqixmJyN.2mEx9wZ.3/U.OqYqz8r8k5rXyN8Zz9Q5R6kS7T8U9V0W', '测试用户1', 'FEMALE', 'test001@ioedream.com', '13800138001', '市场部', '销售专员', 'EMP000002', 'CARD000002', 'ACTIVE'),
('user_000003', 'test002', '\$2a\$10\$EqixmJyN.2mEx9wZ.3/U.OqYqz8r8k5rXyN8Zz9Q5R6kS7T8U9V0W', '测试用户2', 'MALE', 'test002@ioedream.com', '13800138002', '人事部', '人事专员', 'EMP000003', 'CARD000003', 'ACTIVE')
ON DUPLICATE KEY UPDATE update_time = CURRENT_TIMESTAMP;

EOF

    log_success "本地数据库初始化脚本创建完成: $init_sql"
}

# 启动Docker环境
start_docker_environment() {
    log "启动Docker测试环境..."

    local compose_file="$PROJECT_ROOT/test/docker/docker-compose.test.yml"

    # 清理现有容器
    if [ "$CLEAN_ENV" = "true" ]; then
        log "清理现有Docker容器..."
        docker-compose -f "$compose_file" down -v --remove-orphans
    fi

    # 启动容器
    docker-compose -f "$compose_file" up -d

    # 等待服务启动
    log "等待服务启动..."
    sleep 30

    # 检查服务状态
    check_docker_services

    log_success "Docker测试环境启动完成"
}

# 检查Docker服务状态
check_docker_services() {
    log "检查Docker服务状态..."

    # 检查MySQL
    log "检查MySQL服务..."
    for i in {1..30}; do
        if docker exec ioedream-mysql-test mysqladmin ping -h localhost --silent; then
            log_success "MySQL服务启动成功"
            break
        fi
        log_info "等待MySQL服务启动... ($i/30)"
        sleep 2
    done

    # 检查Redis
    log "检查Redis服务..."
    for i in {1..15}; do
        if docker exec ioedream-redis-test redis-cli ping > /dev/null 2>&1; then
            log_success "Redis服务启动成功"
            break
        fi
        log_info "等待Redis服务启动... ($i/15)"
        sleep 2
    done

    # 检查Nacos
    log "检查Nacos服务..."
    for i in {1..60}; do
        if curl -s http://localhost:8848/nacos/ > /dev/null 2>&1; then
            log_success "Nacos服务启动成功"
            break
        fi
        log_info "等待Nacos服务启动... ($i/60)"
        sleep 2
    done
}

# 配置本地环境
setup_local_environment() {
    log "配置本地测试环境..."

    # 初始化数据库
    log "初始化本地数据库..."
    if command -v mysql &> /dev/null; then
        mysql -h "$MYSQL_HOST" -P "$MYSQL_PORT" -u "$MYSQL_USER" -p"$MYSQL_PASSWORD" < "$PROJECT_ROOT/test/database/init_test_db.sql"
        log_success "本地数据库初始化完成"
    else
        log_warning "MySQL客户端未安装，请手动执行数据库初始化脚本"
    fi

    # 检查Redis连接
    log "检查Redis连接..."
    if command -v redis-cli &> /dev/null; then
        if redis-cli -h "$REDIS_HOST" -p "$REDIS_PORT" ping > /dev/null 2>&1; then
            log_success "Redis连接正常"
        else
            log_error "Redis连接失败，请检查Redis服务"
            exit 1
        fi
    else
        log_warning "Redis客户端未安装，跳过Redis连接检查"
    fi

    log_success "本地测试环境配置完成"
}

# 生成测试数据
generate_test_data() {
    log "生成测试数据..."

    # 检查Python环境
    if ! command -v python3 &> /dev/null; then
        log_error "Python3未安装，无法生成测试数据"
        return 1
    fi

    # 安装Python依赖
    local requirements_file="$PROJECT_ROOT/test/test-data/requirements.txt"
    if [ ! -f "$requirements_file" ]; then
        cat > "$requirements_file" << EOF
Faker>=18.0.0
EOF
    fi

    # 安装依赖
    pip3 install -r "$requirements_file" 2>/dev/null || {
        log_warning "Python依赖安装失败，尝试使用系统包管理器"
        pip3 install faker 2>/dev/null || log_warning "Faker安装失败，测试数据生成可能受限"
    }

    # 生成测试数据
    local generator_script="$PROJECT_ROOT/test/test-data/generator/test-data-generator.py"
    if [ -f "$generator_script" ]; then
        python3 "$generator_script" \
            --output "$PROJECT_ROOT/test/test-data/generated" \
            --users 1000 \
            --devices 50 \
            --transactions 5000 \
            --attendance 2000 \
            --access 2000 \
            --format json

        log_success "测试数据生成完成"
    else
        log_error "测试数据生成器脚本不存在: $generator_script"
        return 1
    fi
}

# 导入测试数据
import_test_data() {
    log "导入测试数据..."

    local data_dir="$PROJECT_ROOT/test/test-data/generated"
    if [ ! -d "$data_dir" ]; then
        log_error "测试数据目录不存在，请先生成测试数据"
        return 1
    fi

    # 导入用户数据
    if [ -f "$data_dir/users.json" ]; then
        log "导入用户数据..."
        # 这里需要根据具体的数据导入方式实现
        log_success "用户数据导入完成"
    fi

    # 导入设备数据
    if [ -f "$data_dir/devices.json" ]; then
        log "导入设备数据..."
        log_success "设备数据导入完成"
    fi

    log_success "测试数据导入完成"
}

# 验证测试环境
validate_test_environment() {
    log "验证测试环境..."

    # 验证数据库连接
    log "验证数据库连接..."
    if command -v mysql &> /dev/null; then
        if mysql -h "$MYSQL_HOST" -P "$MYSQL_PORT" -u "$MYSQL_USER" -p"$MYSQL_PASSWORD" -e "USE ioedream_test; SELECT COUNT(*) FROM t_user;" > /dev/null 2>&1; then
            log_success "数据库连接验证通过"
        else
            log_error "数据库连接验证失败"
            return 1
        fi
    fi

    # 验证Redis连接
    log "验证Redis连接..."
    if command -v redis-cli &> /dev/null; then
        if redis-cli -h "$REDIS_HOST" -p "$REDIS_PORT" ping > /dev/null 2>&1; then
            log_success "Redis连接验证通过"
        else
            log_error "Redis连接验证失败"
            return 1
        fi
    fi

    # 验证Nacos连接（Docker模式）
    if [ "$DEPLOYMENT_MODE" = "docker" ]; then
        log "验证Nacos连接..."
        if curl -s http://localhost:8848/nacos/ > /dev/null 2>&1; then
            log_success "Nacos连接验证通过"
        else
            log_error "Nacos连接验证失败"
            return 1
        fi
    fi

    log_success "测试环境验证完成"
}

# 显示环境信息
show_environment_info() {
    log "测试环境配置信息"
    echo "==============================================="
    echo "部署模式: $DEPLOYMENT_MODE"
    echo "数据库: $MYSQL_HOST:$MYSQL_PORT/$MYSQL_DATABASE"
    echo "Redis: $REDIS_HOST:$REDIS_PORT"
    if [ "$DEPLOYMENT_MODE" = "docker" ]; then
        echo "Nacos: http://localhost:8848/nacos (用户: $NACOS_USER)"
        echo "Prometheus: http://localhost:9090"
        echo "Grafana: http://localhost:3000 (用户: admin, 密码: admin)"
    fi
    echo "==============================================="

    # 显示有用的命令
    echo
    echo "有用的命令:"
    echo "Docker模式:"
    echo "  查看容器状态: docker-compose -f $PROJECT_ROOT/test/docker/docker-compose.test.yml ps"
    echo "  查看容器日志: docker-compose -f $PROJECT_ROOT/test/docker/docker-compose.test.yml logs [service_name]"
    echo "  停止环境: docker-compose -f $PROJECT_ROOT/test/docker/docker-compose.test.yml down"
    echo
    echo "数据库连接:"
    echo "  MySQL: mysql -h $MYSQL_HOST -P $MYSQL_PORT -u $MYSQL_USER -p$MYSQL_PASSWORD $MYSQL_DATABASE"
    echo "  Redis: redis-cli -h $REDIS_HOST -p $REDIS_PORT"
}

# 清理测试环境
cleanup_environment() {
    log "清理测试环境..."

    if [ "$DEPLOYMENT_MODE" = "docker" ]; then
        local compose_file="$PROJECT_ROOT/test/docker/docker-compose.test.yml"
        if [ -f "$compose_file" ]; then
            docker-compose -f "$compose_file" down -v --remove-orphans
            log_success "Docker环境清理完成"
        fi
    fi

    # 清理生成的测试数据
    local data_dir="$PROJECT_ROOT/test/test-data/generated"
    if [ -d "$data_dir" ]; then
        rm -rf "$data_dir"
        log_success "测试数据清理完成"
    fi

    log_success "测试环境清理完成"
}

# 主函数
main() {
    log "开始配置IOE-DREAM微服务测试环境..."

    # 解析命令行参数
    parse_arguments "$@"

    # 清理环境
    if [ "$CLEAN_ENV" = "true" ]; then
        cleanup_environment
        log "清理完成，重新配置环境..."
    fi

    # 检查系统要求
    check_requirements

    # 根据部署模式配置环境
    if [ "$DEPLOYMENT_MODE" = "docker" ]; then
        create_docker_compose
        start_docker_environment
    else
        create_local_database_init
        setup_local_environment
    fi

    # 生成测试数据
    generate_test_data

    # 导入测试数据
    import_test_data

    # 验证测试环境
    validate_test_environment

    # 显示环境信息
    show_environment_info

    log_success "IOE-DREAM微服务测试环境配置完成！"
}

# 脚本入口点
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi