#!/bin/bash

# IOE-DREAM 微服务启动验证脚本
# 用于验证所有微服务的启动和注册状态
# 作者: IOE-DREAM Team
# 创建时间: 2025-11-29

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
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

# 核心微服务列表
CORE_SERVICES=(
    "ioedream-auth-service:8091"
    "ioedream-identity-service:8092"
    "ioedream-system-service:8093"
    "ioedream-device-service:8094"
    "ioedream-notification-service:8095"
    "ioedream-audit-service:8096"
    "ioedream-access-service:8097"
    "ioedream-consume-service:8098"
    "ioedream-attendance-service:8099"
    "ioedream-video-service:8100"
    "ioedream-visitor-service:8101"
    "ioedream-monitor-service:8102"
    "ioedream-file-service:8103"
    "ioedream-report-service:8104"
    "ioedream-hr-service:8105"
    "ioedream-oa-service:8106"
    "ioedream-config-service:8107"
    "ioedream-smart-service:8108"
)

# 网关服务
GATEWAY_SERVICE="smart-gateway:8080"

# 检查Java进程
check_java_process() {
    local service=$1
    local port=$2

    log_info "检查 $service (端口 $port)..."

    # 检查端口是否被占用
    if netstat -tlnp 2>/dev/null | grep ":$port " >/dev/null; then
        log_success "$service 进程正在运行 (端口 $port)"

        # 尝试健康检查
        if curl -s --connect-timeout 5 http://localhost:$port/actuator/health >/dev/null 2>&1; then
            log_success "$service 健康检查通过"
            return 0
        else
            log_warning "$service 进程运行但健康检查失败"
            return 1
        fi
    else
        log_warning "$service 进程未运行 (端口 $port 未被占用)"
        return 2
    fi
}

# 检查Eureka注册状态
check_eureka_registration() {
    local service=$1

    log_info "检查 $service Eureka注册状态..."

    # 获取Eureka中的服务实例
    local eureka_url="http://localhost:8761/eureka/apps"

    if curl -s "$eureka_url" | grep -q "$service"; then
        log_success "$service 已在Eureka中注册"
        return 0
    else
        log_warning "$service 未在Eureka中注册"
        return 1
    fi
}

# 检查网关路由状态
check_gateway_routes() {
    log_info "检查网关路由状态..."

    local gateway_url="http://localhost:8080"

    # 获取网关路由
    if curl -s "$gateway_url/actuator/gateway/routes" | grep -q "id"; then
        local route_count=$(curl -s "$gateway_url/actuator/gateway/routes" | grep -o '"id":[^,]*' | wc -l)
        log_success "网关路由配置正常 (共 $route_count 个路由)"

        # 检查特定服务路由
        for service_info in "${CORE_SERVICES[@]}"; do
            local service_name=$(echo "$service_info" | cut -d: -f1)
            if curl -s "$gateway_url/actuator/gateway/routes" | grep -q "$service_name"; then
                log_success "  $service_name 路由配置正确"
            else
                log_warning "  $service_name 路由配置缺失"
            fi
        done

        return 0
    else
        log_error "网关路由配置异常"
        return 1
    fi
}

# 检查数据库连接
check_database() {
    log_info "检查数据库连接状态..."

    # 检查H2数据库（开发环境）
    if netstat -tlnp 2>/dev/null | grep ":9092 " >/dev/null; then
        log_success "H2数据库管理界面可访问 (http://localhost:9092)"
    else
        log_warning "H2数据库管理界面未启动"
    fi

    # 检查Redis连接
    if redis-cli ping >/dev/null 2>&1; then
        log_success "Redis连接正常"
    else
        log_warning "Redis连接失败"
    fi
}

# 启动缺失服务
start_missing_services() {
    local missing_services=()

    log_info "检查缺失的服务..."

    for service_info in "${CORE_SERVICES[@]}"; do
        local service=$(echo "$service_info" | cut -d: -f1)
        local port=$(echo "$service_info" | cut -d: -f2)

        if ! netstat -tlnp 2>/dev/null | grep ":$port " >/dev/null; then
            missing_services+=("$service")
        fi
    done

    if [ ${#missing_services[@]} -eq 0 ]; then
        log_success "所有核心服务都在运行"
        return 0
    fi

    log_warning "发现 ${#missing_services[@]} 个服务未运行:"
    for service in "${missing_services[@]}"; do
        echo "  - $service"
    done

    echo
    read -p "是否启动缺失的服务? (y/n): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        for service in "${missing_services[@]}"; do
            log_info "启动 $service..."
            cd "$PWD/$service"
            if [ -f "pom.xml" ]; then
                mvn spring-boot:run > "../logs/${service}-startup.log" 2>&1 &
                sleep 5
                log_info "$service 启动中..."
            else
                log_error "$service pom.xml 文件不存在"
            fi
            cd ..
        done

        log_info "等待服务启动完成..."
        sleep 30
    fi
}

# 性能基准测试
performance_benchmark() {
    log_info "执行性能基准测试..."

    # 测试认证服务响应时间
    if curl -s --connect-timeout 5 --max-time 10 http://localhost:8091/actuator/health >/dev/null; then
        local response_time=$(curl -o /dev/null -s -w "%{time_total}" http://localhost:8091/actuator/health)
        log_info "认证服务响应时间: ${response_time}s"

        if (( $(echo "$response_time < 1.0" | bc -l) )); then
            log_success "认证服务性能优秀 (< 1.0s)"
        elif (( $(echo "$response_time < 2.0" | bc -l) )); then
            log_warning "认证服务性能良好 (< 2.0s)"
        else
            log_error "认证服务性能较差 (> 2.0s)"
        fi
    fi

    # 测试网关响应时间
    if curl -s --connect-timeout 5 --max-time 10 http://localhost:8080/actuator/health >/dev/null; then
        local gateway_response_time=$(curl -o /dev/null -s -w "%{time_total}" http://localhost:8080/actuator/health)
        log_info "网关响应时间: ${gateway_response_time}s"

        if (( $(echo "$gateway_response_time < 0.5" | bc -l) )); then
            log_success "网关性能优秀 (< 0.5s)"
        elif (( $(echo "$gateway_response_time < 1.0" | bc -l) )); then
            log_warning "网关性能良好 (< 1.0s)"
        else
            log_error "网关性能较差 (> 1.0s)"
        fi
    fi
}

# 生成验证报告
generate_report() {
    local report_file="reports/microservice-verification-report-$(date +%Y%m%d).md"
    mkdir -p reports

    log_info "生成验证报告: $report_file"

    cat > "$report_file" << EOF
# IOE-DREAM 微服务验证报告

**验证时间**: $(date '+%Y-%m-%d %H:%M:%S')
**验证范围**: 核心微服务启动和注册状态

## 📊 验证结果总览

### 服务运行状态
\`\`\`
EOF

    local running_count=0
    local total_count=${#CORE_SERVICES[@]}

    for service_info in "${CORE_SERVICES[@]}"; do
        local service=$(echo "$service_info" | cut -d: -f1)
        local port=$(echo "$service_info" | cut -d: -f2)

        if netstat -tlnp 2>/dev/null | grep ":$port " >/dev/null; then
            echo "✅ $service - 运行正常 (端口: $port)"
            ((running_count++))
        else
            echo "❌ $service - 未运行 (端口: $port)"
        fi
    done

    cat >> "$report_file" << EOF
\`\`\`

**运行统计**: $running_count/$total_count 服务正常运行
**运行率**: $(( running_count * 100 / total_count ))%

### Eureka注册状态
EOF

    local registered_count=0
    for service_info in "${CORE_SERVICES[@]}"; do
        local service=$(echo "$service_info" | cut -d: -f1)

        if curl -s "http://localhost:8761/eureka/apps" 2>/dev/null | grep -q "$service"; then
            echo "✅ $service - 已注册"
            ((registered_count++))
        else
            echo "❌ $service - 未注册"
        fi
    done

    cat >> "$report_file" << EOF

**注册统计**: $registered_count/$total_count 服务已注册
**注册率**: $(( registered_count * 100 / total_count ))%

### 网关路由状态
EOF

    if curl -s "http://localhost:8080/actuator/gateway/routes" >/dev/null 2>&1; then
        local route_count=$(curl -s "http://localhost:8080/actuator/gateway/routes" | grep -o '"id":[^,]*' | wc -l)
        echo "网关路由总数: $route_count" >> "$report_file"
    else
        echo "网关路由配置异常" >> "$report_file"
    fi

    cat >> "$report_file" << EOF

## 🔧 系统环境信息

- **Java版本**: $(java -version 2>&1 | head -n1)
- **Maven版本**: $(mvn -version 2>&1 | head -n1)
- **操作系统**: $(uname -s)
- **系统时间**: $(date)

## 📋 后续建议

1. **服务监控**: 建议配置Prometheus + Grafana监控
2. **日志聚合**: 建议配置ELK Stack日志管理
3. **容错机制**: 建议配置Hystrix熔断器
4. **负载测试**: 建议使用JMeter进行压力测试
5. **CI/CD**: 建议配置Jenkins/GitLab CI流水线

---
**验证完成时间**: $(date '+%Y-%m-%d %H:%M:%S')
EOF

    log_success "验证报告已生成: $report_file"
}

# 主函数
main() {
    echo -e "${CYAN}"
    echo "╔══════════════════════════════════════════════════════════════╗"
    echo "║          IOE-DREAM 微服务启动验证系统 v1.0.0                     ║"
    echo "║          验证微服务启动、注册和健康状态                     ║"
    echo "╚════════════════════════════════════════════════════════════╝"
    echo -e "${NC}"

    log_info "开始微服务验证..."
    echo

    # 1. 检查核心服务运行状态
    log_info "=== 检查核心服务运行状态 ==="
    local running_count=0
    local total_count=${#CORE_SERVICES[@]}

    for service_info in "${CORE_SERVICES[@]}"; do
        local service=$(echo "$service_info" | cut -d: -f1)
        local port=$(echo "$service_info" | cut -d: -f2)

        if check_java_process "$service" "$port"; then
            ((running_count++))
        fi
        echo
    done

    # 2. 检查Eureka注册状态
    log_info "=== 检查Eureka注册状态 ==="
    local registered_count=0

    for service_info in "${CORE_SERVICES[@]}"; do
        local service=$(echo "$service_info" | cut -d: -f1)

        if check_eureka_registration "$service"; then
            ((registered_count++))
        fi
    done

    # 3. 检查网关路由
    log_info "=== 检查网关路由状态 ==="
    check_gateway_routes

    # 4. 检查基础服务
    log_info "=== 检查基础服务状态 ==="
    check_database

    # 5. 性能基准测试
    log_info "=== 性能基准测试 ==="
    performance_benchmark

    # 6. 总结报告
    log_info "=== 验证总结 ==="
    echo -e "${CYAN}服务运行率: ${GREEN}$running_count/$total_count ($(( running_count * 100 / total_count ))%)${NC}"
    echo -e "${CYAN}服务注册率: ${GREEN}$registered_count/$total_count ($(( registered_count * 100 / total_count ))%)${NC}"

    if [ $running_count -eq $total_count ] && [ $registered_count -eq $total_count ]; then
        log_success "🎉 所有微服务验证通过！"
    else
        log_warning "⚠️ 部分微服务需要检查"
        echo
        start_missing_services
    fi

    # 7. 生成报告
    generate_report

    log_info "验证完成！"
}

# 检查依赖
command -v curl >/dev/null 2>&1 || { log_error "curl 命令未找到，请安装curl"; exit 1; }
command -v netstat >/dev/null 2>&1 || { log_error "netstat 命令未找到，请安装net-tools"; exit 1; }

# 执行主函数
main "$@"