#!/bin/bash

# IOE-DREAM微服务集群部署脚本
# 支持：Kubernetes、Helm、监控、日志完整部署

set -e

# 脚本配置
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
K8S_DIR="$PROJECT_ROOT"
HELM_DIR="$K8S_DIR/helm"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 配置变量
ENVIRONMENT=${ENVIRONMENT:-"production"}
NAMESPACE=${NAMESPACE:-"ioedream-prod"}
HELM_RELEASE_NAME=${HELM_RELEASE_NAME:-"ioedream"}
DRY_RUN=${DRY_RUN:-false}
SKIP_DEPENDENCIES=${SKIP_DEPENDENCIES:-false}
SKIP_MONITORING=${SKIP_MONITORING:-false}
SKIP_TESTS=${SKIP_TESTS:-false}
TIMEOUT=${TIMEOUT:-"600s"}

# 日志函数
log() {
    echo -e "${CYAN}[$(date +'%Y-%m-%d %H:%M:%S')] $1${NC}"
}

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

log_header() {
    echo -e "${PURPLE}========================================${NC}"
    echo -e "${PURPLE}$1${NC}"
    echo -e "${PURPLE}========================================${NC}"
}

# 显示帮助信息
show_help() {
    cat << EOF
IOE-DREAM微服务集群部署脚本

用法: $0 [选项]

环境变量:
    ENVIRONMENT              部署环境 (production|staging) [默认: production]
    NAMESPACE                Kubernetes命名空间 [默认: ioedream-prod]
    HELM_RELEASE_NAME        Helm发布名称 [默认: ioedream]
    DRY_RUN                  是否干运行 [默认: false]
    SKIP_DEPENDENCIES        跳过依赖服务部署 [默认: false]
    SKIP_MONITORING          跳过监控组件部署 [默认: false]
    SKIP_TESTS               跳过部署后测试 [默认: false]
    TIMEOUT                  操作超时时间 [默认: 600s]

选项:
    -h, --help               显示帮助信息
    -e, --environment ENV    指定部署环境
    -n, --namespace NS       指定命名空间
    -r, --release NAME       指定Helm发布名称
    -d, --dry-run            干运行模式
    --skip-deps              跳过依赖服务
    --skip-monitoring        跳过监控组件
    --skip-tests             跳过测试
    --timeout TIMEOUT        设置超时时间
    --validate-only          仅验证配置
    --status                 查看部署状态
    --rollback               回滚部署
    --upgrade                升级部署
    --uninstall              卸载部署
    --logs                   查看日志
    --test                   运行测试

命令示例:
    $0                                           # 生产环境完整部署
    $0 -e staging -n ioedream-staging            # 预发布环境部署
    $0 --dry-run                                # 干运行验证
    $0 --status                                 # 查看部署状态
    $0 --rollback                               # 回滚到上一个版本
    $0 --upgrade                                # 升级部署

环境要求:
    - Kubernetes 1.24+
    - Helm 3.8+
    - kubectl configured
    - 足够的集群资源
    - 必要的RBAC权限

EOF
}

# 检查依赖
check_dependencies() {
    log_info "检查系统依赖..."

    # 检查kubectl
    if ! command -v kubectl &> /dev/null; then
        log_error "kubectl未安装，请先安装kubectl"
        exit 1
    fi

    # 检查Helm
    if ! command -v helm &> /dev/null; then
        log_error "Helm未安装，请先安装Helm"
        exit 1
    fi

    # 检查集群连接
    if ! kubectl cluster-info &> /dev/null; then
        log_error "无法连接到Kubernetes集群，请检查kubeconfig配置"
        exit 1
    fi

    # 检查Helm仓库
    log_info "更新Helm仓库..."
    helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
    helm repo add grafana https://grafana.github.io/helm-charts
    helm repo add bitnami https://charts.bitnami.com/bitnami
    helm repo update

    log_success "依赖检查完成"
}

# 验证集群资源
validate_cluster_resources() {
    log_info "验证集群资源..."

    # 检查可用节点
    local ready_nodes=$(kubectl get nodes --no-headers | grep " Ready " | wc -l)
    if [[ $ready_nodes -lt 2 ]]; then
        log_warning "可用节点较少: $ready_nodes，建议至少2个节点"
    fi

    # 检查集群资源
    local total_memory=$(kubectl describe nodes | grep "Memory:" | awk '{sum+=$2} END {print sum}' | sed 's/Ki//')
    local total_cpu=$(kubectl describe nodes | grep "CPU:" | awk '{sum+=$2} END {print sum}')

    log_info "集群资源: 内存 ${total_memory}Ki, CPU ${total_cpu}m"

    # 检查存储类
    local storage_classes=$(kubectl get storageclass --no-headers | wc -l)
    if [[ $storage_classes -eq 0 ]]; then
        log_warning "未找到存储类，持久卷可能无法正常工作"
    fi

    log_success "集群资源验证完成"
}

# 创建命名空间
create_namespace() {
    log_info "创建命名空间: $NAMESPACE"

    if ! kubectl get namespace "$NAMESPACE" &> /dev/null; then
        kubectl create namespace "$NAMESPACE"
        log_success "命名空间创建成功: $NAMESPACE"
    else
        log_info "命名空间已存在: $NAMESPACE"
    fi

    # 添加标签
    kubectl label namespace "$NAMESPACE" \
        environment="$ENVIRONMENT" \
        project="ioedream" \
        managed-by="helm" \
        --overwrite
}

# 创建必要的Secret
create_secrets() {
    log_info "创建必要的Secret..."

    # 镜像拉取密钥
    if ! kubectl get secret ioedream-registry-secret -n "$NAMESPACE" &> /dev/null; then
        log_warning "镜像拉取密钥不存在，请手动创建: kubectl create secret docker-registry ioedream-registry-secret --docker-server=<registry> --docker-username=<username> --docker-password=<password> -n $NAMESPACE"
    fi

    # 数据库密码
    if ! kubectl get secret ioedream-db-secrets -n "$NAMESPACE" &> /dev/null; then
        log_info "创建数据库密钥..."
        kubectl create secret generic ioedream-db-secrets \
            --from-literal=mysql-root-password="ioedream_root_password_$(date +%s)" \
            --from-literal=mysql-password="ioedream_password_$(date +%s)" \
            -n "$NAMESPACE"
    fi

    # JWT密钥
    if ! kubectl get secret ioedream-jwt-secrets -n "$NAMESPACE" &> /dev/null; then
        log_info "创建JWT密钥..."
        kubectl create secret generic ioedream-jwt-secrets \
            --from-literal=jwt-secret="ioedream_jwt_secret_$(openssl rand -base64 32)" \
            -n "$NAMESPACE"
    fi

    log_success "Secret创建完成"
}

# 部署基础设施
deploy_infrastructure() {
    log_header "部署基础设施组件"

    # MySQL数据库
    if [[ "$SKIP_DEPENDENCIES" != "true" ]]; then
        log_info "部署MySQL数据库..."
        helm upgrade --install mysql-ioedream bitnami/mysql \
            --namespace "$NAMESPACE" \
            --set auth.rootPassword="ioedream_root_password" \
            --set auth.database="ioedream_${ENVIRONMENT}" \
            --set primary.persistence.size="100Gi" \
            --set architecture="replication" \
            --set secondary.replicaCount=2 \
            --timeout "$TIMEOUT" \
            --wait \
            ${DRY_RUN:+--dry-run}

        log_success "MySQL数据库部署完成"
    fi

    # Redis缓存
    if [[ "$SKIP_DEPENDENCIES" != "true" ]]; then
        log_info "部署Redis缓存..."
        helm upgrade --install redis-ioedream bitnami/redis \
            --namespace "$NAMESPACE" \
            --set auth.password="ioedream_redis_password" \
            --set master.persistence.size="20Gi" \
            --set replica.replicaCount=3 \
            --set architecture="replication" \
            --timeout "$TIMEOUT" \
            --wait \
            ${DRY_RUN:+--dry-run}

        log_success "Redis缓存部署完成"
    fi

    # Nacos服务发现
    if [[ "$SKIP_DEPENDENCIES" != "true" ]]; then
        log_info "部署Nacos服务发现..."
        helm upgrade --install nacos-ioedream bitnami/nacos \
            --namespace "$NAMESPACE" \
            --set auth.enabled="true" \
            --set auth.adminUser="nacos" \
            --set auth.adminPassword="ioedream_nacos_password" \
            --set persistence.size="20Gi" \
            --set replicaCount=3 \
            --timeout "$TIMEOUT" \
            --wait \
            ${DRY_RUN:+--dry-run}

        log_success "Nacos服务发现部署完成"
    fi
}

# 部署监控组件
deploy_monitoring() {
    if [[ "$SKIP_MONITORING" == "true" ]]; then
        log_info "跳过监控组件部署"
        return
    fi

    log_header "部署监控组件"

    # Prometheus
    log_info "部署Prometheus..."
    helm upgrade --install prometheus-ioedream prometheus-community/kube-prometheus-stack \
        --namespace "$NAMESPACE" \
        --set prometheus.prometheusSpec.retention="30d" \
        --set prometheus.prometheusSpec.storageSpec.volumeClaimTemplate.spec.resources.requests.storage="50Gi" \
        --set grafana.adminPassword="ioedream_grafana_password" \
        --set grafana.persistence.size="10Gi" \
        --timeout "$TIMEOUT" \
        --wait \
        ${DRY_RUN:+--dry-run}

    log_success "Prometheus和Grafana部署完成"

    # ELK Stack (可选)
    if [[ "$ENVIRONMENT" == "production" ]]; then
        log_info "部署ELK Stack..."
        # 这里可以添加ELK部署逻辑
        log_warning "ELK Stack部署待完善"
    fi
}

# 部署应用服务
deploy_application() {
    log_header "部署IOE-DREAM微服务应用"

    cd "$HELM_DIR/ioedream"

    # 环境特定的values文件
    local values_file="values.yaml"
    if [[ -f "values-${ENVIRONMENT}.yaml" ]]; then
        values_file="values-${ENVIRONMENT}.yaml"
    fi

    log_info "使用配置文件: $values_file"

    # 部署或升级应用
    local helm_action="upgrade"
    if ! helm status "$HELM_RELEASE_NAME" -n "$NAMESPACE" &> /dev/null; then
        helm_action="install"
    fi

    helm "$helm_action" "$HELM_RELEASE_NAME" . \
        --namespace "$NAMESPACE" \
        --values "$values_file" \
        --set global.environment="$ENVIRONMENT" \
        --set global.namespace="$NAMESPACE" \
        --timeout "$TIMEOUT" \
        --wait \
        ${DRY_RUN:+--dry-run}

    log_success "IOE-DREAM微服务应用部署完成"
}

# 运行部署后测试
run_tests() {
    if [[ "$SKIP_TESTS" == "true" ]]; then
        log_info "跳过部署后测试"
        return
    fi

    log_header "运行部署后测试"

    # 等待Pod就绪
    log_info "等待所有Pod就绪..."
    kubectl wait --for=condition=ready pod \
        -l app.kubernetes.io/instance="$HELM_RELEASE_NAME" \
        -n "$NAMESPACE" \
        --timeout=300s

    # 健康检查
    log_info "执行健康检查..."

    # 网关健康检查
    local gateway_url=$(kubectl get ingress -l app.kubernetes.io/name=smart-gateway -n "$NAMESPACE" -o jsonpath='{.items[0].spec.rules[0].host}' 2>/dev/null)
    if [[ -n "$gateway_url" ]]; then
        if curl -f -s "https://$gateway_url/health" &> /dev/null; then
            log_success "网关健康检查通过: $gateway_url"
        else
            log_error "网关健康检查失败: $gateway_url"
        fi
    fi

    # 服务连通性测试
    log_info "测试服务连通性..."
    local services=("smart-gateway" "ioedream-auth-service" "ioedream-consume-service")
    for service in "${services[@]}"; do
        if kubectl get pod -l app="$service" -n "$NAMESPACE" --no-headers | grep -q "Running"; then
            log_success "服务 $service 运行正常"
        else
            log_error "服务 $service 未正常运行"
        fi
    done

    log_success "部署后测试完成"
}

# 显示部署状态
show_status() {
    log_header "部署状态信息"

    # Helm发布状态
    log_info "Helm发布状态:"
    helm list -n "$NAMESPACE"

    # Pod状态
    log_info "Pod状态:"
    kubectl get pods -n "$NAMESPACE" -l app.kubernetes.io/instance="$HELM_RELEASE_NAME" -o wide

    # Service状态
    log_info "Service状态:"
    kubectl get services -n "$NAMESPACE" -l app.kubernetes.io/instance="$HELM_RELEASE_NAME"

    # Ingress状态
    log_info "Ingress状态:"
    kubectl get ingress -n "$NAMESPACE" -l app.kubernetes.io/instance="$HELM_RELEASE_NAME"

    # HPA状态
    log_info "HPA状态:"
    kubectl get hpa -n "$NAMESPACE" -l app.kubernetes.io/instance="$HELM_RELEASE_NAME"

    # 事件信息
    log_info "最近事件:"
    kubectl get events -n "$NAMESPACE" --sort-by='.lastTimestamp' | tail -10
}

# 回滚部署
rollback_deployment() {
    log_warning "回滚部署..."

    # 检查历史版本
    local history=$(helm history "$HELM_RELEASE_NAME" -n "$NAMESPACE")
    if [[ -z "$history" ]]; then
        log_error "未找到部署历史，无法回滚"
        exit 1
    fi

    echo "$history"
    read -p "请输入要回滚到的版本号: " revision

    if [[ -n "$revision" ]]; then
        helm rollback "$HELM_RELEASE_NAME" "$revision" -n "$NAMESPACE" --timeout "$TIMEOUT"
        log_success "回滚完成，版本: $revision"
    else
        log_error "无效的版本号"
    fi
}

# 升级部署
upgrade_deployment() {
    log_info "升级部署..."

    cd "$HELM_DIR/ioedream"

    helm upgrade "$HELM_RELEASE_NAME" . \
        --namespace "$NAMESPACE" \
        --values "values-${ENVIRONMENT}.yaml" \
        --timeout "$TIMEOUT" \
        --wait

    log_success "部署升级完成"
}

# 卸载部署
uninstall_deployment() {
    log_warning "卸载IOE-DREAM微服务集群..."

    read -p "确认卸载部署? 这将删除所有相关资源 [y/N]: " confirm
    if [[ "$confirm" != "y" && "$confirm" != "Y" ]]; then
        log_info "取消卸载操作"
        exit 0
    fi

    # 卸载应用
    if helm status "$HELM_RELEASE_NAME" -n "$NAMESPACE" &> /dev/null; then
        helm uninstall "$HELM_RELEASE_NAME" -n "$NAMESPACE"
        log_success "应用卸载完成"
    fi

    # 卸载监控组件
    if ! $SKIP_MONITORING; then
        helm uninstall prometheus-ioedream -n "$NAMESPACE" 2>/dev/null || true
        log_success "监控组件卸载完成"
    fi

    # 卸载基础设施
    if ! $SKIP_DEPENDENCIES; then
        helm uninstall mysql-ioedream -n "$NAMESPACE" 2>/dev/null || true
        helm uninstall redis-ioedream -n "$NAMESPACE" 2>/dev/null || true
        helm uninstall nacos-ioedream -n "$NAMESPACE" 2>/dev/null || true
        log_success "基础设施卸载完成"
    fi

    # 删除PVC (可选)
    read -p "是否删除持久卷数据? [y/N]: " delete_pvc
    if [[ "$delete_pvc" == "y" || "$delete_pvc" == "Y" ]]; then
        kubectl delete pvc --all -n "$NAMESPACE" --timeout=60s
        log_warning "持久卷数据已删除"
    fi

    log_success "集群卸载完成"
}

# 查看日志
show_logs() {
    log_header "查看服务日志"

    local service=""
    read -p "请输入服务名称 (默认: gateway): " service
    service=${service:-"smart-gateway"}

    log_info "查看 $service 日志..."
    kubectl logs -l app="$service" -n "$NAMESPACE" --tail=100 -f
}

# 运行测试
run_integration_tests() {
    log_header "运行集成测试"

    # 创建测试Job
    cat <<EOF | kubectl apply -f -
apiVersion: batch/v1
kind: Job
metadata:
  name: ioedream-integration-test
  namespace: $NAMESPACE
spec:
  template:
    spec:
      restartPolicy: Never
      containers:
      - name: test
        image: curlimages/curl:latest
        command:
        - /bin/sh
        - -c
        - |
          echo "开始集成测试..."

          # 测试网关健康检查
          if curl -f http://smart-gateway:8080/actuator/health; then
            echo "✓ 网关健康检查通过"
          else
            echo "✗ 网关健康检查失败"
            exit 1
          fi

          # 测试认证服务
          if curl -f http://ioedream-auth-service:8081/actuator/health; then
            echo "✓ 认证服务健康检查通过"
          else
            echo "✗ 认证服务健康检查失败"
            exit 1
          fi

          echo "所有集成测试通过!"
EOF

    # 等待测试完成
    kubectl wait --for=condition=complete job/ioedream-integration-test -n "$NAMESPACE" --timeout=300s

    # 显示测试结果
    kubectl logs job/ioedream-integration-test -n "$NAMESPACE"

    # 清理测试Job
    kubectl delete job ioedream-integration-test -n "$NAMESPACE"

    log_success "集成测试完成"
}

# 验证配置
validate_configuration() {
    log_header "验证配置文件"

    cd "$HELM_DIR/ioedream"

    # 验证Helm Chart
    log_info "验证Helm Chart..."
    helm lint . --values "values-${ENVIRONMENT}.yaml"

    # 模拟渲染
    log_info "模拟渲染模板..."
    helm template "$HELM_RELEASE_NAME" . \
        --namespace "$NAMESPACE" \
        --values "values-${ENVIRONMENT}.yaml" \
        --dry-run

    log_success "配置验证完成"
}

# 主函数
main() {
    log_header "IOE-DREAM微服务集群部署工具"
    log "环境: $ENVIRONMENT"
    log "命名空间: $NAMESPACE"
    log "Helm发布: $HELM_RELEASE_NAME"

    # 解析命令行参数
    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--help)
                show_help
                exit 0
                ;;
            -e|--environment)
                ENVIRONMENT="$2"
                NAMESPACE="ioedream-${ENVIRONMENT}"
                shift 2
                ;;
            -n|--namespace)
                NAMESPACE="$2"
                shift 2
                ;;
            -r|--release)
                HELM_RELEASE_NAME="$2"
                shift 2
                ;;
            -d|--dry-run)
                DRY_RUN=true
                shift
                ;;
            --skip-deps)
                SKIP_DEPENDENCIES=true
                shift
                ;;
            --skip-monitoring)
                SKIP_MONITORING=true
                shift
                ;;
            --skip-tests)
                SKIP_TESTS=true
                shift
                ;;
            --timeout)
                TIMEOUT="$2"
                shift 2
                ;;
            --validate-only)
                validate_configuration
                exit 0
                ;;
            --status)
                show_status
                exit 0
                ;;
            --rollback)
                rollback_deployment
                exit 0
                ;;
            --upgrade)
                upgrade_deployment
                exit 0
                ;;
            --uninstall)
                uninstall_deployment
                exit 0
                ;;
            --logs)
                show_logs
                exit 0
                ;;
            --test)
                run_integration_tests
                exit 0
                ;;
            *)
                log_error "未知参数: $1"
                show_help
                exit 1
                ;;
        esac
    done

    # 干运行模式
    if [[ "$DRY_RUN" == "true" ]]; then
        log_warning "干运行模式 - 不会实际部署资源"
    fi

    # 完整部署流程
    log_info "开始部署流程..."

    check_dependencies
    validate_cluster_resources
    create_namespace
    create_secrets
    deploy_infrastructure
    deploy_monitoring
    deploy_application
    run_tests
    show_status

    log_header "部署完成"
    log_success "IOE-DREAM微服务集群部署成功!"

    if [[ "$DRY_RUN" != "true" ]]; then
        log_info "访问地址:"
        log "  - 网关: https://api.ioedream.com"
        log "  - Grafana: https://monitor.ioedream.com/grafana"
        log "  - 管理后台: https://admin.ioedream.com"
        log ""
        log_info "管理命令:"
        log "  - 查看状态: $0 --status"
        log "  - 查看日志: $0 --logs"
        log "  - 回滚部署: $0 --rollback"
        log "  - 升级部署: $0 --upgrade"
        log "  - 卸载集群: $0 --uninstall"
    fi
}

# 执行主函数
main "$@"