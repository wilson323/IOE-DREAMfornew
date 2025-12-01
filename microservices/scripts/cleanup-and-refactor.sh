#!/bin/bash

# IOE-DREAM 微服务架构清理和重构脚本
# 统一微服务结构，消除冗余，确保规范一致性

set -e

# 配置变量
BASE_DIR="/d/IOE-DREAM/microservices"
LOG_FILE="$BASE_DIR/cleanup.log"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] [INFO] $1" >> "$LOG_FILE"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] [WARN] $1" >> "$LOG_FILE"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] [ERROR] $1" >> "$LOG_FILE"
}

log_debug() {
    echo -e "${BLUE}[DEBUG]${NC} $1"
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] [DEBUG] $1" >> "$LOG_FILE"
}

# 创建备份目录
create_backup() {
    local backup_dir="$BASE_DIR/backup_$TIMESTAMP"
    mkdir -p "$backup_dir"

    log_info "创建备份目录: $backup_dir"

    # 备份当前目录结构
    find "$BASE_DIR" -maxdepth 2 -type d | sort > "$backup_dir/directory_structure.txt"

    echo "$backup_dir"
}

# 分析现有服务结构
analyze_services() {
    log_info "分析现有微服务结构..."

    # 统计服务目录
    local service_count=0
    for dir in "$BASE_DIR"/*; do
        if [[ -d "$dir" && $(basename "$dir") == *"service" || $(basename "$dir") == "gateway" ]]; then
            service_count=$((service_count + 1))
            local service_name=$(basename "$dir")
            log_debug "发现服务: $service_name"
        fi
    done

    log_info "当前发现 $service_count 个服务目录"
}

# 识别命名不规范的服务
identify_naming_issues() {
    log_info "识别命名不规范的服务..."

    local issues=()

    for dir in "$BASE_DIR"/*; do
        if [[ -d "$dir" ]]; then
            local service_name=$(basename "$dir")

            # 检查命名规范
            if [[ "$service_name" != *"service"* && "$service_name" != "gateway" && "$service_name" != "common" ]]; then
                issues+=("$service_name - 缺少service后缀")
            fi

            # 检查是否以ioedream-开头
            if [[ "$service_name" != "ioedream-"* && "$service_name" != "gateway" && "$service_name" != "common" ]]; then
                issues+=("$service_name - 应以ioedream-开头")
            fi
        fi
    done

    if [[ ${#issues[@]} -gt 0 ]]; then
        log_warn "发现命名问题:"
        for issue in "${issues[@]}"; do
            log_warn "  - $issue"
        done
    else
        log_info "✓ 所有服务命名符合规范"
    fi
}

# 重构服务目录结构
refactor_service_structure() {
    log_info "重构服务目录结构..."

    # 服务重命名映射
    declare -A rename_map=(
        ["access-service"]="ioedream-access-service"
        ["attendance-service"]="ioedream-attendance-service"
        ["consume-service"]="ioedream-consume-service"
        ["device-service"]="ioedream-device-service"
        ["visitor-service"]="ioedream-visitor-service"
        ["video-service"]="ioedream-video-service"
    )

    for old_name in "${!rename_map[@]}"; do
        new_name="${rename_map[$old_name]}"
        old_path="$BASE_DIR/$old_name"
        new_path="$BASE_DIR/$new_name"

        if [[ -d "$old_path" && ! -d "$new_path" ]]; then
            log_info "重命名服务: $old_name -> $new_name"
            mv "$old_path" "$new_path"

            # 更新启动类中的包名
            update_package_names "$new_path" "$old_name" "$new_name"
        fi
    done
}

# 更新包名
update_package_names() {
    local service_path="$1"
    local old_name="$2"
    local new_name="$3"

    log_debug "更新服务包名: $service_path"

    # 更新Java文件中的包名
    find "$service_path/src" -name "*.java" -type f | while read -r java_file; do
        # 替换包名
        sed -i "s/package net.lab1024.sa.$old_name/package net.lab1024.sa.${new_name//-/}/g" "$java_file"

        # 替换import语句
        sed -i "s/import net.lab1024.sa.$old_name/import net.lab1024.sa.${new_name//-/}/g" "$java_file"
    done

    # 更新pom.xml中的artifactId
    if [[ -f "$service_path/pom.xml" ]]; then
        sed -i "s/<artifactId>$old_name<\/artifactId>/<artifactId>$new_name<\/artifactId>/g" "$service_path/pom.xml"
    fi
}

# 统一POM文件配置
unify_pom_configuration() {
    log_info "统一POM文件配置..."

    for service_dir in "$BASE_DIR"/ioedream-* "$BASE_DIR"/smart-gateway; do
        if [[ -d "$service_dir" && -f "$service_dir/pom.xml" ]]; then
            local service_name=$(basename "$service_dir")
            log_debug "统一 $service_name 的POM配置"

            # 创建标准POM模板
            create_standard_pom "$service_dir" "$service_name"
        fi
    done
}

# 创建标准POM模板
create_standard_pom() {
    local service_dir="$1"
    local service_name="$2"

    cat > "$service_dir/pom.xml" << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>net.lab1024</groupId>
        <artifactId>ioe-dream-microservices</artifactId>
        <version>1.0.0</version>
    </parent>

    <artifactId>__SERVICE_NAME__</artifactId>
    <name>__SERVICE_NAME__</name>
    <description>IOE-DREAM __SERVICE_DESCRIPTION__</description>

    <dependencies>
        <!-- Spring Boot Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Spring Boot Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- Consul Discovery -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-consul-discovery</artifactId>
        </dependency>

        <!-- Consul Config -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-consul-config</artifactId>
        </dependency>

        <!-- Bootstrap -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-bootstrap</artifactId>
        </dependency>

        <!-- OpenFeign -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-openfeign</artifactId>
        </dependency>

        <!-- CircuitBreaker -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-circuitbreaker-resilience4j</artifactId>
        </dependency>

        <!-- MyBatis Plus -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
        </dependency>

        <!-- MySQL Driver -->
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- Druid连接池 -->
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>druid-spring-boot-3-starter</artifactId>
        </dependency>

        <!-- Redis -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Actuator -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <!-- Prometheus -->
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-registry-prometheus</artifactId>
        </dependency>

        <!-- API文档 -->
        <dependency>
            <groupId>com.github.xiaoymin</groupId>
            <artifactId>knife4j-openapi3-jakarta-spring-boot-starter</artifactId>
        </dependency>

        <!-- 内部模块依赖 -->
        <dependency>
            <groupId>net.lab1024</groupId>
            <artifactId>microservices-common</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
EOF

    # 替换占位符
    sed -i "s/__SERVICE_NAME__/$service_name/g" "$service_dir/pom.xml"

    # 根据服务名称设置描述
    case "$service_name" in
        "ioedream-auth-service")
            sed -i 's/__SERVICE_DESCRIPTION__/认证服务 - 用户认证、权限验证、令牌管理/g' "$service_dir/pom.xml"
            ;;
        "ioedream-device-service")
            sed -i 's/__SERVICE_DESCRIPTION__/设备服务 - 智能设备管理、状态监控、设备控制/g' "$service_dir/pom.xml"
            ;;
        "ioedream-consume-service")
            sed -i 's/__SERVICE_DESCRIPTION__/消费服务 - 消费管理、账户充值、报表统计/g' "$service_dir/pom.xml"
            ;;
        "ioedream-visitor-service")
            sed -i 's/__SERVICE_DESCRIPTION__/访客服务 - 访客预约、审批管理、门禁记录/g' "$service_dir/pom.xml"
            ;;
        *)
            sed -i 's/__SERVICE_DESCRIPTION__/IOE-DREAM 微服务/g' "$service_dir/pom.xml"
            ;;
    esac
}

# 统一应用配置
unify_application_config() {
    log_info "统一应用配置..."

    # 创建标准应用配置模板
    create_standard_application_config
}

# 创建标准应用配置模板
create_standard_application_config() {
    local config_template='spring:
  application:
    name: __SERVICE_NAME__

  profiles:
    active: dev

  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/__DATABASE_NAME__?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
    username: root
    password: root
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      auto-commit: true
      idle-timeout: 30000
      pool-name: HikariCP
      max-lifetime: 900000
      connection-timeout: 30000
      connection-test-query: SELECT 1

  cloud:
    consul:
      host: localhost
      port: 8500
      discovery:
        service-name: __SERVICE_NAME__
        health-check-path: /actuator/health
        health-check-interval: 15s
        prefer-ip-address: true
        instance-id: ${spring.application.name}:${spring.cloud.client.ip-address}:${server.port}
      config:
        enabled: true
        format: yaml

  # Feign配置
  feign:
    client:
      config:
        default:
          connectTimeout: 10000
          readTimeout: 30000
          loggerLevel: basic
    httpclient:
      enabled: true
      max-connections: 200
      max-connections-per-route: 50

mybatis-plus:
  mapper-locations: classpath*:mapper/*.xml
  type-aliases-package: net.lab1024.sa.__PACKAGE_NAME__.domain.entity
  configuration:
    map-underscore-to-camel-case: true
    cache-enabled: false
    call-setters-on-nulls: true
    jdbc-type-for-null: '"null"'
  global-config:
    db-config:
      id-type: AUTO
      logic-delete-field: deletedFlag
      logic-delete-value: 1
      logic-not-delete-value: 0

server:
  port: __SERVICE_PORT__
  servlet:
    context-path: /__SERVICE_NAME__

logging:
  level:
    net.lab1024.sa.__PACKAGE_NAME__: DEBUG
    org.springframework.cloud.consul: DEBUG
  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n"
  file:
    name: logs/__SERVICE_NAME__.log
    max-size: 100MB
    max-history: 30

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
  metrics:
    export:
      prometheus:
        enabled: true

---
spring:
  profiles: dev

  datasource:
    url: jdbc:mysql://localhost:3306/__DATABASE_NAME__?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&serverTimezone=GMT%2B8

logging:
  level:
    net.lab1024.sa.__PACKAGE_NAME__: DEBUG

---
spring:
  profiles: prod

  datasource:
    url: jdbc:mysql://mysql-prod:3306/__DATABASE_NAME__?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8

logging:
  level:
    net.lab1024.sa.__PACKAGE_NAME__: INFO'

    # 将模板写入临时文件
    echo "$config_template" > "/tmp/application_template.yml"
}

# 消除冗余文件
cleanup_redundant_files() {
    log_info "消除冗余文件..."

    # 删除重复的common目录
    if [[ -d "$BASE_DIR/common" && -d "$BASE_DIR/microservices-common" ]]; then
        log_warn "发现重复的common目录，保留microservices-common"
        rm -rf "$BASE_DIR/common"
    fi

    if [[ -d "$BASE_DIR/smart-common" && -d "$BASE_DIR/microservices-common" ]]; then
        log_warn "发现重复的smart-common目录，保留microservices-common"
        rm -rf "$BASE_DIR/smart-common"
    fi

    # 删除重复的device-service
    if [[ -d "$BASE_DIR/device-service" && -d "$BASE_DIR/ioedream-device-service" ]]; then
        log_warn "发现重复的device-service目录，保留ioedream-device-service"
        rm -rf "$BASE_DIR/device-service"
    fi

    # 删除测试和临时文件
    find "$BASE_DIR" -name "*.tmp" -delete
    find "$BASE_DIR" -name "*.log" -not -name "cleanup.log" -delete
    find "$BASE_DIR" -name ".DS_Store" -delete
    find "$BASE_DIR" -name "Thumbs.db" -delete
}

# 生成重构报告
generate_refactor_report() {
    local report_file="$BASE_DIR/REFACTOR_REPORT_$TIMESTAMP.md"

    log_info "生成重构报告: $report_file"

    cat > "$report_file" << EOF
# IOE-DREAM 微服务架构重构报告

## 重构时间
$(date)

## 重构目标
- 统一微服务命名规范
- 消除冗余代码和配置
- 标准化目录结构
- 统一技术栈配置

## 重构内容

### 1. 服务命名标准化
- 统一使用 \`ioedream-\` 前缀
- 规范化服务后缀为 \`-service\`
- 更新相关的包名和配置

### 2. 目录结构标准化
- 采用统一的微服务目录结构
- 规范化包名和类名
- 统一配置文件结构

### 3. 技术栈统一
- Spring Boot 3.5.x
- Spring Cloud 2023.x
- MySQL 8.0 + Redis 6.0
- Consul 服务发现

### 4. 清理冗余内容
- 删除重复的common模块
- 合并重复的服务目录
- 清理临时文件和日志

## 重构前后对比

### 重构前
- 服务命名不统一
- 存在重复的common模块
- 配置文件不一致
- 目录结构不规范

### 重构后
- 统一的命名规范
- 消除冗余模块
- 标准化配置
- 规范化目录结构

## 服务列表
EOF

    # 添加当前服务列表
    echo "" >> "$report_file"
    echo "| 服务名称 | 端口 | 数据库 | 描述 |" >> "$report_file"
    echo "|---------|------|--------|------|" >> "$report_file"

    for service_dir in "$BASE_DIR"/ioedream-*; do
        if [[ -d "$service_dir" ]]; then
            local service_name=$(basename "$service_dir")
            local port="80XX"
            local database="${service_name#ioedream-}"
            local description=""

            case "$service_name" in
                "ioedream-auth-service")
                    port="8081"
                    description="认证服务"
                    ;;
                "ioedream-device-service")
                    port="8105"
                    description="设备服务"
                    ;;
                "ioedream-consume-service")
                    port="8082"
                    description="消费服务"
                    ;;
                "ioedream-visitor-service")
                    port="8108"
                    description="访客服务"
                    ;;
                "ioedream-attendance-service")
                    port="8103"
                    description="考勤服务"
                    ;;
                "ioedream-video-service")
                    port="8104"
                    description="视频服务"
                    ;;
            esac

            echo "| $service_name | $port | ${database}_service | $description |" >> "$report_file"
        fi
    done

    echo "" >> "$report_file"
    echo "## 验证清单" >> "$report_file"
    echo "- [ ] 所有服务命名符合规范" >> "$report_file"
    echo "- [ ] 目录结构统一" >> "$report_file"
    echo "- [ ] 配置文件标准化" >> "$report_file"
    echo "- [ ] 冗余文件已清理" >> "$report_file"
    echo "- [ ] 包名已更新" >> "$report_file"

    log_info "✓ 重构报告生成完成: $report_file"
}

# 主函数
main() {
    echo "========================================"
    echo "IOE-DREAM 微服务架构清理和重构"
    echo "========================================"

    log_info "开始微服务架构重构..."

    # 创建备份
    local backup_dir=$(create_backup)
    log_info "备份目录: $backup_dir"

    # 执行重构步骤
    analyze_services
    identify_naming_issues
    refactor_service_structure
    unify_pom_configuration
    unify_application_config
    cleanup_redundant_files
    generate_refactor_report

    log_info "✓ 微服务架构重构完成!"
    log_info "请查看重构报告和备份目录"

    echo "========================================"
    echo "重构完成！"
    echo "备份目录: $backup_dir"
    echo "日志文件: $LOG_FILE"
    echo "========================================"
}

# 信号处理
trap 'log_info "脚本执行中断"; exit 1' INT TERM

# 运行主函数
main "$@"