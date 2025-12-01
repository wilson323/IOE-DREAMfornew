#!/bin/bash

# ===================================================================
# IOE-DREAM 微服务Kubernetes部署脚本
# 用于在Kubernetes集群中部署完整的微服务架构
#
# 使用方法:
#   ./k8s-deploy.sh [deploy|delete|status|logs|scale] [service_name]
#
# 参数说明:
#   deploy  - 部署所有服务到Kubernetes (默认)
#   delete  - 删除所有部署
#   status  - 查看部署状态
#   logs    - 查看服务日志
#   scale   - 扩容/缩容服务
# ===================================================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m'

# 配置变量
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
NAMESPACE="ioedream"
K8S_MANIFESTS_DIR="$SCRIPT_DIR"

# 微服务列表
MICROSERVICES=(
    "smart-gateway"
    "ioedream-auth-service"
    "ioedream-identity-service"
    "ioedream-device-service"
    "ioedream-access-service"
    "ioedream-consume-service"
    "ioedream-attendance-service"
    "ioedream-video-service"
    "ioedream-oa-service"
    "ioedream-system-service"
    "ioedream-monitor-service"
)

# 基础设施服务
INFRASTRUCTURE=(
    "mysql"
    "redis"
    "nacos"
    "prometheus"
    "grafana"
    "alertmanager"
)

# 日志记录函数
log() {
    local level=$1
    local message=$2
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')

    echo "[$timestamp] [$level] $message"

    case $level in
        "INFO")
            echo -e "${GREEN}[INFO]${NC} $message"
            ;;
        "WARN")
            echo -e "${YELLOW}[WARN]${NC} $message"
            ;;
        "ERROR")
            echo -e "${RED}[ERROR]${NC} $message"
            ;;
        "DEBUG")
            echo -e "${BLUE}[DEBUG]${NC} $message"
            ;;
    esac
}

# 打印分隔线
print_separator() {
    echo -e "${PURPLE}==================================================================${NC}"
}

# 打印标题
print_section() {
    echo ""
    print_separator
    echo -e "${CYAN}📋 $1${NC}"
    print_separator
}

# 检查Kubernetes环境
check_kubernetes() {
    print_section "🔍 检查Kubernetes环境"

    # 检查kubectl是否可用
    if ! command -v kubectl &> /dev/null; then
        log "ERROR" "kubectl未安装，请先安装kubectl"
        exit 1
    fi

    # 检查集群连接
    if ! kubectl cluster-info &> /dev/null; then
        log "ERROR" "无法连接到Kubernetes集群"
        exit 1
    fi

    # 检查集群节点状态
    local node_count=$(kubectl get nodes --no-headers | wc -l)
    local ready_nodes=$(kubectl get nodes --no-headers | grep -c "Ready")

    log "INFO" "Kubernetes集群节点: $ready_nodes/$node_count 就绪"

    if [ $ready_nodes -eq 0 ]; then
        log "ERROR" "没有可用的集群节点"
        exit 1
    fi

    # 检查命名空间
    if kubectl get namespace "$NAMESPACE" &> /dev/null; then
        log "INFO" "命名空间 $NAMESPACE 已存在"
    else
        log "INFO" "创建命名空间 $NAMESPACE"
        kubectl create namespace "$NAMESPACE"
    fi

    return 0
}

# 部署基础设施
deploy_infrastructure() {
    print_section "🏗️ 部署基础设施服务"

    local config_file="$K8S_MANIFESTS_DIR/configmaps.yaml"
    local secrets_file="$K8S_MANIFESTS_DIR/secrets.yaml"
    local infra_file="$K8S_MANIFESTS_DIR/infrastructure.yaml"

    # 部署ConfigMap
    if [ -f "$config_file" ]; then
        log "INFO" "部署ConfigMap配置..."
        kubectl apply -f "$config_file" -n "$NAMESPACE"
    else
        log "ERROR" "ConfigMap文件不存在: $config_file"
        return 1
    fi

    # 部署Secrets
    if [ -f "$secrets_file" ]; then
        log "INFO" "部署Secrets配置..."
        kubectl apply -f "$secrets_file" -n "$NAMESPACE"
    else
        log "ERROR" "Secrets文件不存在: $secrets_file"
        return 1
    fi

    # 部署基础设施服务
    if [ -f "$infra_file" ]; then
        log "INFO" "部署基础设施服务..."
        kubectl apply -f "$infra_file" -n "$NAMESPACE"
    else
        log "ERROR" "基础设施配置文件不存在: $infra_file"
        return 1
    fi

    # 等待基础设施服务启动
    log "INFO" "等待基础设施服务启动..."
    for service in "${INFRASTRUCTURE[@]}"; do
        log "INFO" "等待 $service 服务就绪..."
        kubectl wait --for=condition=available deployment -l app="$service" -n "$NAMESPACE" --timeout=300s
    done

    return 0
}

# 部署微服务
deploy_microservices() {
    print_section "🚀 部署微服务"

    local microservices_file="$K8S_MANIFESTS_DIR/microservices.yaml"
    local ingress_file="$K8S_MANIFESTS_DIR/ingress.yaml"

    # 部署微服务
    if [ -f "$microservices_file" ]; then
        log "INFO" "部署微服务..."
        kubectl apply -f "$microservices_file" -n "$NAMESPACE"
    else
        log "ERROR" "微服务配置文件不存在: $microservices_file"
        return 1
    fi

    # 部署Ingress
    if [ -f "$ingress_file" ]; then
        log "INFO" "部署Ingress配置..."
        kubectl apply -f "$ingress_file" -n "$NAMESPACE"
    else
        log "WARN" "Ingress配置文件不存在: $ingress_file"
    fi

    # 等待微服务启动
    log "INFO" "等待微服务启动..."
    for service in "${MICROSERVICES[@]}"; do
        log "INFO" "等待 $service 服务就绪..."
        kubectl wait --for=condition=available deployment -l app="$service" -n "$NAMESPACE" --timeout=600s || log "WARN" "$service 服务启动超时"
    done

    return 0
}

# 查看部署状态
show_deployment_status() {
    print_section "📊 查看部署状态"

    echo -e "${BLUE}基础设施服务状态:${NC}"
    kubectl get pods -n "$NAMESPACE" -l 'component in (database, cache, registry, monitoring)' -o wide

    echo ""
    echo -e "${BLUE}微服务状态:${NC}"
    kubectl get pods -n "$NAMESPACE" -l 'component in (gateway, authentication, identity, device, access-control, consumption, attendance, video, office, system)' -o wide

    echo ""
    echo -e "${BLUE}服务状态:${NC}"
    kubectl get services -n "$NAMESPACE"

    echo ""
    echo -e "${BLUE}Ingress状态:${NC}"
    kubectl get ingress -n "$NAMESPACE"

    echo ""
    echo -e "${BLUE}HPA状态:${NC}"
    kubectl get hpa -n "$NAMESPACE"
}

# 查看服务日志
show_service_logs() {
    local service_name=$1

    if [ -z "$service_name" ]; then
        log "ERROR" "请指定服务名称"
        echo "可用服务:"
        echo "基础设施: ${INFRASTRUCTURE[*]}"
        echo "微服务: ${MICROSERVICES[*]}"
        return 1
    fi

    print_section "📝 查看 $service_name 日志"

    local pod_name=$(kubectl get pods -n "$NAMESPACE" -l app="$service_name" -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || echo "")

    if [ -z "$pod_name" ]; then
        log "ERROR" "未找到 $service_name 的Pod"
        return 1
    fi

    log "INFO" "查看Pod: $pod_name"
    kubectl logs -f -n "$NAMESPACE" "$pod_name" --tail=100
}

# 扩容/缩容服务
scale_service() {
    local service_name=$1
    local replica_count=$2

    if [ -z "$service_name" ] || [ -z "$replica_count" ]; then
        log "ERROR" "请指定服务名称和副本数量"
        echo "用法: $0 scale <service_name> <replica_count>"
        return 1
    fi

    print_section "📈 扩容/缩容 $service_name 到 $replica_count 个副本"

    if ! kubectl scale deployment "$service_name" -n "$NAMESPACE" --replicas="$replica_count"; then
        log "ERROR" "扩容/缩容失败"
        return 1
    fi

    log "INFO" "等待扩容/缩容完成..."
    kubectl rollout status deployment/"$service_name" -n "$NAMESPACE" --timeout=300s

    log "INFO" "扩容/缩容完成"
    show_deployment_status
}

# 删除所有部署
delete_deployment() {
    print_section "🗑️ 删除IOE-DREAM部署"

    read -p "确认要删除所有IOE-DREAM相关部署吗？(y/N): " -n 1 -r
    echo

    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        log "INFO" "取消删除操作"
        return 0
    fi

    log "INFO" "删除微服务..."
    if [ -f "$K8S_MANIFESTS_DIR/microservices.yaml" ]; then
        kubectl delete -f "$K8S_MANIFESTS_DIR/microservices.yaml" -n "$NAMESPACE" --ignore-not-found=true
    fi

    log "INFO" "删除Ingress配置..."
    if [ -f "$K8S_MANIFESTS_DIR/ingress.yaml" ]; then
        kubectl delete -f "$K8S_MANIFESTS_DIR/ingress.yaml" -n "$NAMESPACE" --ignore-not-found=true
    fi

    log "INFO" "删除基础设施服务..."
    if [ -f "$K8S_MANIFESTS_DIR/infrastructure.yaml" ]; then
        kubectl delete -f "$K8S_MANIFESTS_DIR/infrastructure.yaml" -n "$NAMESPACE" --ignore-not-found=true
    fi

    log "INFO" "删除ConfigMap和Secrets..."
    if [ -f "$K8S_MANIFESTS_DIR/configmaps.yaml" ]; then
        kubectl delete -f "$K8S_MANIFESTS_DIR/configmaps.yaml" -n "$NAMESPACE" --ignore-not-found=true
    fi
    if [ -f "$K8S_MANIFESTS_DIR/secrets.yaml" ]; then
        kubectl delete -f "$K8S_MANIFESTS_DIR/secrets.yaml" -n "$NAMESPACE" --ignore-not-found=true
    fi

    log "INFO" "删除命名空间 $NAMESPACE..."
    kubectl delete namespace "$NAMESPACE" --ignore-not-found=true

    log "INFO" "删除完成"
}

# 等待所有服务就绪
wait_for_ready() {
    print_section "⏳ 等待所有服务就绪"

    local max_wait=1800  # 30分钟
    local wait_interval=30
    local elapsed=0

    while [ $elapsed -lt $max_wait ]; do
        local ready_pods=$(kubectl get pods -n "$NAMESPACE" --field-selector=status.phase=Running --no-headers | wc -l)
        local total_pods=$(kubectl get pods -n "$NAMESPACE" --no-headers | wc -l)
        local ready_percentage=0

        if [ $total_pods -gt 0 ]; then
            ready_percentage=$((ready_pods * 100 / total_pods))
        fi

        echo -e "\r等待中... ${ready_percentage}% ($ready_pods/$total_pods Pods 就绪) - ${elapsed}s"

        if [ "$ready_pods" -eq "$total_pods" ] && [ $total_pods -gt 0 ]; then
            echo ""
            log "INFO" "所有Pod已就绪！"
            return 0
        fi

        sleep $wait_interval
        elapsed=$((elapsed + wait_interval))
    done

    echo ""
    log "ERROR" "等待超时，部分服务未能正常启动"
    return 1
}

# 显示访问信息
show_access_info() {
    print_section "🌐 访问信息"

    echo -e "${BLUE}基础服务访问地址:${NC}"

    # 获取Ingress IP或主机名
    local ingress_ip=$(kubectl get ingress -n "$NAMESPACE" ioedream-ingress -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null || echo "localhost")
    local ingress_host=$(kubectl get ingress -n "$NAMESPACE" ioedream-ingress -o jsonpath='{.spec.rules[0].host}' 2>/dev/null || echo "api.ioedream.local")

    if [ "$ingress_ip" = "localhost" ]; then
        # 检查NodePort
        local node_port=$(kubectl get svc -n "$NAMESPACE" -l app=ingress-nginx-controller --no-headers 2>/dev/null | awk '{print $6}' | cut -d':' -f2 | cut -d'/' -f1 || echo "30080")
        echo -e "API网关:      ${GREEN}http://localhost:$node_port${NC}"
    else
        echo -e "API网关:      ${GREEN}http://$ingress_ip${NC}"
        echo -e "API网关域名:  ${GREEN}http://$ingress_host${NC}"
    fi

    echo ""
    echo -e "${BLUE}服务端口映射:${NC}"
    echo -e "smart-gateway:       $(kubectl get svc -n "$NAMESPACE" smart-gateway -o jsonpath='{.spec.ports[0].nodePort}' 2>/dev/null || echo "未配置NodePort")"
    echo -e "ioedream-auth:        $(kubectl get svc -n "$NAMESPACE" ioedream-auth-service -o jsonpath='{.spec.ports[0].nodePort}' 2>/dev/null || echo "未配置NodePort")"
    echo -e "nacos:                $(kubectl get svc -n "$NAMESPACE" nacos -o jsonpath='{.spec.ports[0].nodePort}' 2>/dev/null || echo "未配置NodePort")"
    echo -e "prometheus:           $(kubectl get svc -n "$NAMESPACE" prometheus -o jsonpath='{.spec.ports[0].nodePort}' 2>/dev/null || echo "未配置NodePort")"
    echo -e "grafana:              $(kubectl get svc -n "$NAMESPACE" grafana -o jsonpath='{.spec.ports[0].nodePort}' 2>/dev/null || echo "未配置NodePort")"

    echo ""
    echo -e "${BLUE}kubectl命令示例:${NC}"
    echo -e "查看Pod:      kubectl get pods -n $NAMESPACE"
    echo -e "查看服务:     kubectl get services -n $NAMESPACE"
    echo -e "查看日志:     kubectl logs -f -n $NAMESPACE <pod-name>"
    echo -e "端口转发:     kubectl port-forward -n $NAMESPACE svc/<service-name> <local-port>:<service-port>"
}

# 主函数
main() {
    local command=${1:-"deploy"}
    local service_name=${2:-""}
    local replica_count=${3:-""}

    case $command in
        "deploy")
            print_section "🚀 开始IOE-DREAM Kubernetes部署"
            check_kubernetes
            deploy_infrastructure
            deploy_microservices
            wait_for_ready
            show_access_info
            log "INFO" "IOE-DREAM部署完成！"
            ;;
        "delete")
            delete_deployment
            ;;
        "status")
            show_deployment_status
            ;;
        "logs")
            show_service_logs "$service_name"
            ;;
        "scale")
            scale_service "$service_name" "$replica_count"
            ;;
        "access")
            show_access_info
            ;;
        "help"|"--help"|"-h")
            echo "IOE-DREAM 微服务Kubernetes部署工具"
            echo ""
            echo "使用方法:"
            echo "  $0 [命令] [参数]"
            echo ""
            echo "命令:"
            echo "  deploy  - 部署所有服务到Kubernetes (默认)"
            echo "  delete  - 删除所有部署"
            echo "  status  - 查看部署状态"
            echo "  logs    - 查看服务日志"
            echo "  scale   - 扩容/缩容服务"
            echo "  access  - 显示访问信息"
            echo "  help    - 显示帮助信息"
            echo ""
            echo "示例:"
            echo "  $0 deploy                     # 部署所有服务"
            echo "  $0 status                      # 查看部署状态"
            echo "  $0 logs smart-gateway          # 查看网关日志"
            echo "  $0 scale ioedream-auth 5       # 扩容认证服务到5个副本"
            echo "  $0 access                      # 显示访问信息"
            ;;
        *)
            log "ERROR" "未知命令: $command"
            echo "使用 '$0 help' 查看帮助"
            exit 1
            ;;
    esac
}

# 脚本入口点
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi