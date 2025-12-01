#!/bin/bash

# IOE-DREAM 架构标准统一脚本
# 用于统一微服务命名、配置文件标准等

# 颜色定义
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

# 检查是否在项目根目录
check_project_root() {
    if [ ! -f "pom.xml" ] && [ ! -d "microservices" ]; then
        log_error "请在项目根目录执行此脚本"
        exit 1
    fi
    log_success "项目根目录验证通过"
}

# 备份原始配置
backup_configs() {
    log_info "备份原始配置文件..."
    BACKUP_DIR="backup-$(date +%Y%m%d-%H%M%S)"
    mkdir -p "$BACKUP_DIR"

    # 备份主要配置文件
    find microservices -name "application*.yml" -o -name "application*.yaml" | xargs -I {} cp --parents {} "$BACKUP_DIR/"
    find microservices -name "pom.xml" | xargs -I {} cp --parents {} "$BACKUP_DIR/"

    log_success "配置文件已备份到: $BACKUP_DIR"
}

# 1. 统一微服务命名
unify_service_naming() {
    log_info "开始统一微服务命名..."

    cd microservices

    # 重命名重复的服务
    declare -A SERVICE_RENAMES=(
        ["device-service"]="ioedream-device-service-legacy"
        ["hr-service"]="ioedream-hr-service-legacy"
        ["monitor"]="ioedream-monitor-service-legacy"
        ["analytics"]="ioedream-analytics-service"
        ["common"]="ioedream-common-service"
    )

    for old_name in "${!SERVICE_RENAMES[@]}"; do
        new_name="${SERVICE_RENAMES[$old_name]}"
        if [ -d "$old_name" ]; then
            log_info "重命名服务: $old_name -> $new_name"
            mv "$old_name" "$new_name"

            # 更新pom.xml中的artifactId
            if [ -f "$new_name/pom.xml" ]; then
                sed -i "s/<artifactId>$old_name<\/artifactId>/<artifactId>$new_name<\/artifactId>/g" "$new_name/pom.xml"
                sed -i "s/<name>$old_name<\/name>/<name>$new_name<\/name>/g" "$new_name/pom.xml"
            fi
        fi
    done

    cd ..
    log_success "微服务命名统一完成"
}

# 2. 统一配置文件标准
unify_config_standards() {
    log_info "开始统一配置文件标准..."

    # 创建标准配置模板
    create_standard_configs

    # 遍历所有服务并更新配置
    for service_dir in microservices/ioedream-*/; do
        if [ -d "$service_dir" ]; then
            service_name=$(basename "$service_dir")
            log_info "统一 $service_name 配置..."

            update_service_config "$service_dir" "$service_name"
        fi
    done

    log_success "配置文件标准统一完成"
}

# 创建标准配置模板
create_standard_configs() {
    log_info "创建标准配置模板..."

    # 标准应用配置模板
    cat > /tmp/standard-application.yml << 'EOF'
# IOE-DREAM 标准微服务配置
# @author IOE-DREAM Team
# @version 1.0.0

server:
  port: ${SERVER_PORT:8080}
  servlet:
    context-path: /
  tomcat:
    basedir: ${LOG_DIRECTORY:/tmp}/tomcat-logs
    accesslog:
      enabled: true
      max-days: 7
      pattern: "%t %{X-Forwarded-For}i %a %r %s (%D ms) %I (%B byte)"

spring:
  application:
    name: ${SERVICE_NAME:unknown-service}

  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}

  # 标准数据源配置
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:smart_admin_v3}?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&serverTimezone=GMT%2B8
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:root}
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      auto-commit: true
      idle-timeout: 30000
      pool-name: HikariCP-${spring.application.name}
      max-lifetime: 900000
      connection-timeout: 30000
      connection-test-query: SELECT 1

  # 标准Redis配置
  data:
    redis:
      database: ${REDIS_DATABASE:0}
      host: ${REDIS_HOST:127.0.0.1}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      timeout: 10000ms
      lettuce:
        pool:
          max-active: 10
          min-idle: 2
          max-idle: 8
          max-wait: 30000ms

  # 标准缓存配置
  cache:
    type: redis

  # 标准JSON配置
  jackson:
    serialization:
      write-enums-using-to-string: true
      write-dates-as-timestamps: false
    deserialization:
      read-enums-using-to-string: true
      fail-on-unknown-properties: false
    default-property-inclusion: always
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: GMT+8

  # 标准文件上传配置
  servlet:
    multipart:
      max-file-size: 20MB
      max-request-size: 10MB

  # Nacos服务注册与配置中心
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_SERVER:localhost:8848}
        namespace: ${NACOS_NAMESPACE:dev}
        group: ${NACOS_GROUP:DEFAULT_GROUP}
        service: ${spring.application.name}
        enabled: true
      config:
        server-addr: ${NACOS_SERVER:localhost:8848}
        namespace: ${NACOS_NAMESPACE:dev}
        group: ${NACOS_GROUP:DEFAULT_GROUP}
        file-extension: yml
        enabled: true
        refresh-enabled: true

# 标准监控配置
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus,metrics
  endpoint:
    health:
      show-details: always
  metrics:
    export:
      prometheus:
        enabled: true

# 标准日志配置
logging:
  level:
    root: INFO
    net.lab1024: DEBUG
    org.springframework.cloud: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/${spring.application.name}.log
    max-size: 100MB
    max-history: 30

# 跨域配置
access-control-allow-origin: '*'

# 标准OpenAPI配置
springdoc:
  swagger-ui:
    enabled: true
    doc-expansion: none
    tags-sorter: alpha
    server-base-url: /
  api-docs:
    enabled: true
  packages-to-scan: net.lab1024

knife4j:
  enable: true
  basic:
    enable: false
    username: api
    password: 1024
EOF

    # 网关配置模板
    cat > /tmp/gateway-application.yml << 'EOF'
# IOE-DREAM API网关标准配置
server:
  port: ${SERVER_PORT:8000}

spring:
  application:
    name: smart-gateway

  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
      routes:
        # 认证服务
        - id: ioedream-auth-service
          uri: lb://ioedream-auth-service
          predicates:
            - Path=/api/auth/**
          filters:
            - StripPrefix=2

        # 设备服务
        - id: ioedream-device-service
          uri: lb://ioedream-device-service
          predicates:
            - Path=/api/device/**
          filters:
            - StripPrefix=2

        # 其他服务路由...

    nacos:
      discovery:
        server-addr: ${NACOS_SERVER:localhost:8848}
        namespace: ${NACOS_NAMESPACE:dev}
        group: ${NACOS_GROUP:DEFAULT_GROUP}
EOF

    log_success "标准配置模板创建完成"
}

# 更新单个服务的配置
update_service_config() {
    local service_dir="$1"
    local service_name="$2"

    local config_file="$service_dir/src/main/resources/application.yml"

    # 备份原配置
    if [ -f "$config_file" ]; then
        cp "$config_file" "$config_file.backup"
    fi

    # 根据服务类型设置端口
    local port=""
    case $service_name in
        "ioedream-auth-service") port="8001" ;;
        "ioedream-identity-service") port="8002" ;;
        "ioedream-device-service") port="8003" ;;
        "ioedream-access-service") port="8004" ;;
        "ioedream-consume-service") port="8005" ;;
        "ioedream-attendance-service") port="8006" ;;
        "ioedream-video-service") port="8007" ;;
        "ioedream-config-service") port="8100" ;;
        "ioedream-monitor-service") port="8101" ;;
        "ioedream-report-service") port="8102" ;;
        "ioedream-file-service") port="8103" ;;
        *) port="8080" ;;
    esac

    # 创建新的配置文件
    mkdir -p "$service_dir/src/main/resources"

    # 使用标准模板并替换服务特定配置
    sed "s/\${SERVICE_NAME:unknown-service}/$service_name/g; s/\${SERVER_PORT:8080}/$port/g" /tmp/standard-application.yml > "$config_file"

    log_info "已更新 $service_name 配置文件"
}

# 3. 统一Maven依赖
unify_maven_dependencies() {
    log_info "开始统一Maven依赖..."

    # 创建标准父POM模板
    cat > /tmp/standard-parent-pom.xml << 'EOF'
    <!-- IOE-DREAM 标准依赖版本管理 -->
    <properties>
        <java.version>17</java.version>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>

        <!-- Spring Boot -->
        <spring-boot.version>3.2.0</spring-boot.version>
        <spring-cloud.version>2023.0.3</spring-cloud.version>
        <spring-cloud-alibaba.version>2022.0.0.0</spring-cloud-alibaba.version>

        <!-- Database -->
        <mysql.version>8.0.33</mysql.version>
        <druid.version>1.2.21</druid.version>
        <mybatis-plus.version>3.5.5</mybatis-plus.version>

        <!-- Redis -->
        <jedis.version>4.4.3</jedis.version>

        <!-- Utils -->
        <lombok.version>1.18.30</lombok.version>
        <hutool.version>5.8.22</hutool.version>
        <fastjson2.version>2.0.43</fastjson2.version>

        <!-- Swagger/OpenAPI -->
        <knife4j.version>4.3.0</knife4j.version>
        <springdoc.version>2.2.0</springdoc.version>

        <!-- Monitoring -->
        <micrometer.version>1.12.0</micrometer.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <!-- Spring Boot -->
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>${spring-boot.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>

            <!-- Spring Cloud -->
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>

            <!-- Spring Cloud Alibaba -->
            <dependency>
                <groupId>com.alibaba.cloud</groupId>
                <artifactId>spring-cloud-alibaba-dependencies</artifactId>
                <version>${spring-cloud-alibaba.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
EOF

    log_success "Maven依赖标准模板创建完成"
}

# 4. 生成服务端口映射文件
generate_port_mapping() {
    log_info "生成服务端口映射..."

    cat > microservices/PORT_MAPPING.md << 'EOF'
# IOE-DREAM 微服务端口映射

## 核心业务服务 (8000-8099)

| 服务名称 | 端口 | 描述 |
|---------|------|------|
| smart-gateway | 8000 | API网关 |
| ioedream-auth-service | 8001 | 认证服务 |
| ioedream-identity-service | 8002 | 身份服务 |
| ioedream-device-service | 8003 | 设备服务 |
| ioedream-access-service | 8004 | 门禁服务 |
| ioedream-consume-service | 8005 | 消费服务 |
| ioedream-attendance-service | 8006 | 考勤服务 |
| ioedream-video-service | 8007 | 视频服务 |

## 支撑服务 (8100-8199)

| 服务名称 | 端口 | 描述 |
|---------|------|------|
| ioedream-config-service | 8100 | 配置服务 |
| ioedream-monitor-service | 8101 | 监控服务 |
| ioedream-report-service | 8102 | 报表服务 |
| ioedream-file-service | 8103 | 文件服务 |
| ioedream-notification-service | 8104 | 通知服务 |

## 基础设施

| 服务名称 | 端口 | 描述 |
|---------|------|------|
| Nacos Server | 8848 | 服务注册与配置中心 |
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存数据库 |
| Prometheus | 9090 | 监控数据采集 |
| Grafana | 3000 | 监控数据展示 |
EOF

    log_success "服务端口映射文件生成完成"
}

# 5. 验证配置统一结果
verify_unification() {
    log_info "验证配置统一结果..."

    local error_count=0
    local success_count=0

    # 检查配置文件
    for service_dir in microservices/ioedream-*/; do
        if [ -d "$service_dir" ]; then
            local service_name=$(basename "$service_dir")
            local config_file="$service_dir/src/main/resources/application.yml"

            if [ -f "$config_file" ]; then
                # 检查关键配置项
                if grep -q "nacos:" "$config_file" && \
                   grep -q "datasource:" "$config_file" && \
                   grep -q "redis:" "$config_file" && \
                   grep -q "management:" "$config_file"; then
                    ((success_count++))
                    log_success "$service_name 配置验证通过"
                else
                    ((error_count++))
                    log_error "$service_name 配置验证失败"
                fi
            else
                ((error_count++))
                log_error "$service_name 配置文件不存在"
            fi
        fi
    done

    log_info "配置验证完成: 成功 $success_count 个，失败 $error_count 个"

    if [ $error_count -eq 0 ]; then
        log_success "所有服务配置验证通过！"
        return 0
    else
        log_error "存在 $error_count 个服务配置验证失败"
        return 1
    fi
}

# 6. 生成启动脚本
generate_startup_scripts() {
    log_info "生成服务启动脚本..."

    # 生成统一启动脚本
    cat > microservices/start-all-services.sh << 'EOF'
#!/bin/bash

# IOE-DREAM 微服务统一启动脚本

SERVICES=(
    "smart-gateway"
    "ioedream-auth-service"
    "ioedream-identity-service"
    "ioedream-device-service"
    "ioedream-access-service"
    "ioedream-consume-service"
    "ioedream-attendance-service"
    "ioedream-video-service"
    "ioedream-config-service"
    "ioedream-monitor-service"
)

echo "启动IOE-DREAM微服务..."

for service in "${SERVICES[@]}"; do
    if [ -d "$service" ]; then
        echo "启动服务: $service"
        cd "$service"
        nohup mvn spring-boot:run > "../logs/$service.log" 2>&1 &
        cd ..
        echo "$service 启动中..."
        sleep 5
    else
        echo "服务目录不存在: $service"
    fi
done

echo "所有服务启动完成，请检查日志确认启动状态。"
EOF

    chmod +x microservices/start-all-services.sh

    # 生成停止脚本
    cat > microservices/stop-all-services.sh << 'EOF'
#!/bin/bash

# IOE-DREAM 微服务统一停止脚本

echo "停止IOE-DREAM微服务..."

# 查找并终止Java进程
pkill -f "spring-boot:run"
pkill -f "java.*spring-boot"

echo "所有服务已停止。"
EOF

    chmod +x microservices/stop-all-services.sh

    log_success "启动脚本生成完成"
}

# 主函数
main() {
    log_info "开始执行IOE-DREAM架构标准统一..."

    check_project_root
    backup_configs

    # 执行统一步骤
    unify_service_naming
    unify_config_standards
    unify_maven_dependencies
    generate_port_mapping
    generate_startup_scripts

    # 验证结果
    if verify_unification; then
        log_success "🎉 架构标准统一完成！"
        log_info "请查看报告: docs/ARCHITECTURE_STANDARDS_UNIFICATION_REPORT.md"
        log_info "备份文件位于: backup-$(date +%Y%m%d-%H%M%S)/"
    else
        log_error "架构标准统一存在问题，请检查错误日志"
        exit 1
    fi
}

# 执行主函数
main "$@"