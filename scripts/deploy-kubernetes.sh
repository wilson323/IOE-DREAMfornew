#!/bin/bash

# =====================================================
# IOE-DREAM Kubernetes部署脚本
# 支持多环境部署和滚动更新
# 包含健康检查和回滚机制
# =====================================================

set -e

# 颜色输出
echo_color() {
    local color=$1
    local message=$2
    case $color in
        red)     echo -e "\033[31m$message\033[0m" ;;
        green)   echo -e "\033[32m$message\033[0m" ;;
        yellow)  echo -e "\033[33m$message\033[0m" ;;
        blue)    echo -e "\033[34m$message\033[0m" ;;
        purple)  echo -e "\033[35m$message\033[0m" ;;
        cyan)    echo -e "\033[36m$message\033[0m" ;;
        *)       echo "$message" ;;
    esac
}

# 打印使用说明
print_usage() {
    echo_color "blue" "===================================================="
    echo_color "blue" "🚀 IOE-DREAM Kubernetes部署脚本"
    echo_color "blue" "===================================================="
    echo "用法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  -e, --env ENVIRONMENT    部署环境 (staging|production) [默认: staging]"
    echo "  -s, --service SERVICE    指定服务 (all|gateway|common|device|oa|access|attendance|video|consume|visitor) [默认: all]"
    echo "  -n, --namespace NAMESPACE 命名空间 [默认: ioe-dream-ENVIRONMENT]"
    echo "  -i, --image IMAGE        镜像标签 [默认: latest]"
    echo "  -f, --file FILE          指定部署文件路径"
    echo "  -r, --replicas REPLICAS  副本数量 [默认: 环境默认值]"
    echo "  --dry-run               预览部署计划，不实际执行"
    echo "  --skip-helm             跳过Helm Chart部署"
    echo "  --verify                部署后验证"
    echo "  --timeout TIMEOUT       部署超时时间(秒) [默认: 600]"
    echo "  -h, --help              显示帮助信息"
    echo ""
    echo "示例:"
    echo "  $0 -e staging -s all                    # 部署所有服务到测试环境"
    echo "  $0 -e production -s common -i v1.2.0   # 部署指定服务到生产环境"
    echo "  $0 -e staging --dry-run               # 预览测试环境部署计划"
}

# 默认参数
ENVIRONMENT="staging"
SERVICE="all"
NAMESPACE=""
IMAGE_TAG="latest"
DEPLOYMENT_FILE=""
REPLICAS=""
DRY_RUN=false
SKIP_HELM=false
VERIFY=true
TIMEOUT=600

# 参数解析
while [[ $# -gt 0 ]]; do
    case $1 in
        -e|--env)
            ENVIRONMENT="$2"
            shift 2
            ;;
        -s|--service)
            SERVICE="$2"
            shift 2
            ;;
        -n|--namespace)
            NAMESPACE="$2"
            shift 2
            ;;
        -i|--image)
            IMAGE_TAG="$2"
            shift 2
            ;;
        -f|--file)
            DEPLOYMENT_FILE="$2"
            shift 2
            ;;
        -r|--replicas)
            REPLICAS="$2"
            shift 2
            ;;
        --dry-run)
            DRY_RUN=true
            shift
            ;;
        --skip-helm)
            SKIP_HELM=true
            shift
            ;;
        --verify)
            VERIFY=true
            shift
            ;;
        --timeout)
            TIMEOUT="$2"
            shift 2
            ;;
        -h|--help)
            print_usage
            exit 0
            ;;
        *)
            echo_color "red" "❌ 未知参数: $1"
            print_usage
            exit 1
            ;;
    esac
done

# 设置默认命名空间
if [[ -z "$NAMESPACE" ]]; then
    NAMESPACE="ioe-dream-${ENVIRONMENT}"
fi

# 验证环境参数
if [[ "$ENVIRONMENT" != "staging" && "$ENVIRONMENT" != "production" ]]; then
    echo_color "red" "❌ 无效的环境: $ENVIRONMENT (支持: staging|production)"
    exit 1
fi

# 验证服务参数
valid_services=("all" "gateway" "common" "device" "oa" "access" "attendance" "video" "consume" "visitor")
if [[ ! " ${valid_services[@]} " =~ " ${SERVICE} " ]]; then
    echo_color "red" "❌ 无效的服务: $SERVICE"
    echo_color "red" "支持的服务: ${valid_services[*]}"
    exit 1
fi

# 打印部署信息
print_deployment_info() {
    echo_color "blue" "===================================================="
    echo_color "blue" "🚀 开始部署 IOE-DREAM 应用"
    echo_color "blue" "===================================================="
    echo_color "cyan" "环境: $ENVIRONMENT"
    echo_color "cyan" "命名空间: $NAMESPACE"
    echo_color "cyan" "服务: $SERVICE"
    echo_color "cyan" "镜像标签: $IMAGE_TAG"
    echo_color "cyan" "副本数量: ${REPLICAS:-默认}"
    echo_color "cyan" "部署模式: $([ "$DRY_RUN" = true ] && echo "预览模式" || echo "实际部署")"
    echo_color "blue" "===================================================="
}

# 检查依赖
check_dependencies() {
    echo_color "yellow" "🔍 检查部署依赖..."

    # 检查kubectl
    if ! command -v kubectl &> /dev/null; then
        echo_color "red" "❌ kubectl未安装或不在PATH中"
        exit 1
    fi

    # 检查集群连接
    if ! kubectl cluster-info &> /dev/null; then
        echo_color "red" "❌ 无法连接到Kubernetes集群"
        exit 1
    fi

    # 检查Helm
    if [[ "$SKIP_HELM" = false ]]; then
        if ! command -v helm &> /dev/null; then
            echo_color "red" "❌ Helm未安装或不在PATH中"
            exit 1
        fi
    fi

    # 检查命名空间
    if ! kubectl get namespace "$NAMESPACE" &> /dev/null; then
        echo_color "yellow" "⚠️  命名空间 $NAMESPACE 不存在，将自动创建"
        if [[ "$DRY_RUN" = false ]]; then
            kubectl create namespace "$NAMESPACE"
            echo_color "green" "✅ 命名空间 $NAMESPACE 创建成功"
        fi
    fi

    echo_color "green" "✅ 依赖检查完成"
}

# 创建命名空间和资源
create_namespaces() {
    echo_color "yellow" "🔧 创建命名空间和基础资源..."

    if [[ "$DRY_RUN" = true ]]; then
        echo_color "cyan" "[预览] 将创建以下命名空间:"
        echo "  - ioe-dream-staging"
        echo "  - ioe-dream-production"
        echo "  - ioe-dream-monitoring"
        return
    fi

    # 应用命名空间配置
    if [[ -f "deployment/kubernetes/namespace.yaml" ]]; then
        kubectl apply -f deployment/kubernetes/namespace.yaml
        echo_color "green" "✅ 命名空间配置应用成功"
    else
        echo_color "yellow" "⚠️  namespace.yaml文件不存在，跳过命名空间配置"
    fi
}

# 应用ConfigMap和Secret
apply_configs() {
    echo_color "yellow" "🔧 应用配置文件..."

    if [[ "$DRY_RUN" = true ]]; then
        echo_color "cyan" "[预览] 将应用以下配置:"
        echo "  - ConfigMap: ioe-dream-config"
        echo "  - Secrets: mysql-credentials, redis-credentials, nacos-credentials"
        echo "  - Service: ${SERVICE}-service"
        return
    fi

    # 应用ConfigMap
    if [[ -f "deployment/kubernetes/configmap.yaml" ]]; then
        kubectl apply -f deployment/kubernetes/configmap.yaml -n "$NAMESPACE"
        echo_color "green" "✅ ConfigMap应用成功"
    fi

    # 应用Secrets
    if [[ -f "deployment/kubernetes/secrets.yaml" ]]; then
        kubectl apply -f deployment/kubernetes/secrets.yaml -n "$NAMESPACE"
        echo_color "green" "✅ Secrets应用成功"
    fi
}

# 部署单个服务
deploy_service() {
    local service_name="$1"
    local deployment_file="$2"

    echo_color "yellow" "🚀 部署服务: $service_name"

    if [[ "$DRY_RUN" = true ]]; then
        echo_color "cyan" "[预览] 将部署服务: $service_name"
        if [[ -n "$deployment_file" && -f "$deployment_file" ]]; then
            echo "  部署文件: $deployment_file"
        else
            echo "  部署文件: deployment/kubernetes/services/${service_name}-service.yaml"
        fi
        return
    fi

    local actual_deployment_file="$deployment_file"
    if [[ -z "$actual_deployment_file" ]]; then
        actual_deployment_file="deployment/kubernetes/services/${service_name}-service.yaml"
    fi

    if [[ ! -f "$actual_deployment_file" ]]; then
        echo_color "red" "❌ 部署文件不存在: $actual_deployment_file"
        return 1
    fi

    # 更新镜像标签
    if [[ "$IMAGE_TAG" != "latest" ]]; then
        sed -i.bak "s|image: .*:latest|image: .*:$IMAGE_TAG|g" "$actual_deployment_file"
    fi

    # 更新副本数量
    if [[ -n "$REPLICAS" ]]; then
        sed -i.bak "s|replicas: .*|replicas: $REPLICAS|g" "$actual_deployment_file"
    fi

    # 应用部署配置
    kubectl apply -f "$actual_deployment_file" -n "$NAMESPACE"

    # 恢复备份文件
    if [[ -f "$actual_deployment_file.bak" ]]; then
        mv "$actual_deployment_file.bak" "$actual_deployment_file"
    fi

    echo_color "green" "✅ 服务 $service_name 部署成功"
}

# 部署所有服务
deploy_all_services() {
    local services=("gateway-service" "common-service" "device-comm-service" "oa-service" "access-service" "attendance-service" "video-service" "consume-service" "visitor-service")

    for service in "${services[@]}"; do
        if [[ -f "deployment/kubernetes/services/${service}.yaml" ]]; then
            deploy_service "$service" "deployment/kubernetes/services/${service}.yaml"
        else
            echo_color "yellow" "⚠️  服务配置文件不存在: $service"
        fi
    done
}

# 等待部署完成
wait_for_deployment() {
    if [[ "$DRY_RUN" = true ]]; then
        echo_color "cyan" "[预览] 将等待部署完成..."
        return
    fi

    echo_color "yellow" "⏳ 等待部署完成..."

    local services_to_wait=()

    if [[ "$SERVICE" = "all" ]]; then
        services_to_wait=("ioedream-gateway-service" "ioedream-common-service" "ioedream-device-comm-service" "ioedream-oa-service" "ioedream-access-service" "ioedream-attendance-service" "ioedream-video-service" "ioedream-consume-service" "ioedream-visitor-service")
    else
        services_to_wait=("ioedream-${SERVICE}")
    fi

    for service in "${services_to_wait[@]}"; do
        echo_color "cyan" "等待服务: $service"
        if kubectl rollout status deployment/"$service" -n "$NAMESPACE" --timeout="$TIMEOUT"s; then
            echo_color "green" "✅ 服务 $service 部署成功"
        else
            echo_color "red" "❌ 服务 $service 部署失败"
            return 1
        fi
    done
}

# 健康检查
health_check() {
    if [[ "$DRY_RUN" = true ]]; then
        echo_color "cyan" "[预览] 将执行健康检查..."
        return
    fi

    echo_color "yellow" "🔍 执行健康检查..."

    local services_to_check=()

    if [[ "$SERVICE" = "all" ]]; then
        services_to_check=("ioedream-gateway-service" "ioedream-common-service" "ioedream-device-comm-service" "ioedream-oa-service" "ioedream-access-service" "ioedream-attendance-service" "ioedream-video-service" "ioedream-consume-service" "ioedream-visitor-service")
    else
        services_to_check=("ioedream-${SERVICE}")
    fi

    for service in "${services_to_check[@]}"; do
        # 检查Pod状态
        local pod_status=$(kubectl get pods -n "$NAMESPACE" -l app="$service" -o jsonpath='{.items[*].status.phase}')
        if [[ "$pod_status" == *"Running"* ]]; then
            echo_color "green" "✅ $service Pod运行正常"
        else
            echo_color "red" "❌ $service Pod状态异常: $pod_status"
            return 1
        fi

        # 检查服务端点
        local service_port=""
        case "$service" in
            "ioedream-gateway-service") service_port="8080" ;;
            "ioedream-common-service") service_port="8088" ;;
            "ioedream-device-comm-service") service_port="8087" ;;
            "ioedream-oa-service") service_port="8089" ;;
            "ioedream-access-service") service_port="8090" ;;
            "ioedream-attendance-service") service_port="8091" ;;
            "ioedream-video-service") service_port="8092" ;;
            "ioedream-consume-service") service_port="8094" ;;
            "ioedream-visitor-service") service_port="8095" ;;
        esac

        # 获取服务ClusterIP
        local service_ip=$(kubectl get service "$service" -n "$NAMESPACE" -o jsonpath='{.spec.clusterIP}' 2>/dev/null || echo "")

        if [[ -n "$service_ip" && "$service_ip" != "None" ]]; then
            # 等待服务启动
            sleep 30

            # 尝试健康检查
            for i in {1..10}; do
                if curl -f -s --connect-timeout 5 "http://${service_ip}:${service_port}/actuator/health" > /dev/null 2>&1; then
                    echo_color "green" "✅ $service 健康检查通过"
                    break
                elif [[ $i -eq 10 ]]; then
                    echo_color "red" "❌ $service 健康检查失败"
                    return 1
                else
                    echo_color "yellow" "⏳ $service 健康检查重试 $i/10"
                    sleep 10
                fi
            done
        else
            echo_color "yellow" "⚠️  $service 服务未找到或无ClusterIP，跳过健康检查"
        fi
    done
}

# 显示部署状态
show_deployment_status() {
    echo_color "blue" "===================================================="
    echo_color "blue" "📊 部署状态概览"
    echo_color "blue" "===================================================="

    if [[ "$DRY_RUN" = true ]]; then
        echo_color "cyan" "[预览] 将显示部署状态..."
        return
    fi

    echo_color "cyan" "命名空间: $NAMESPACE"
    echo_color "cyan" "环境: $ENVIRONMENT"
    echo ""

    # 显示Pod状态
    echo_color "yellow" "📦 Pod状态:"
    kubectl get pods -n "$NAMESPACE" -o wide

    echo ""

    # 显示Service状态
    echo_color "yellow" "🌐 Service状态:"
    kubectl get services -n "$NAMESPACE"

    echo ""

    # 显示HPA状态
    echo_color "yellow" "📈 HPA状态:"
    kubectl get hpa -n "$NAMESPACE" 2>/dev/null || echo "无HPA配置"

    echo ""

    # 显示Deployment状态
    echo_color "yellow" "🚀 Deployment状态:"
    kubectl get deployments -n "$NAMESPACE"
}

# 回滚函数
rollback_deployment() {
    if [[ "$DRY_RUN" = true ]]; then
        echo_color "cyan" "[预览] 将执行回滚..."
        return
    fi

    echo_color "red" "🔄 开始回滚部署..."

    local services_to_rollback=()

    if [[ "$SERVICE" = "all" ]]; then
        services_to_rollback=("ioedream-gateway-service" "ioedream-common-service" "ioedream-device-comm-service" "ioedream-oa-service" "ioedream-access-service" "ioedream-attendance-service" "ioedream-video-service" "ioedream-consume-service" "ioedream-visitor-service")
    else
        services_to_rollback=("ioedream-${SERVICE}")
    fi

    for service in "${services_to_rollback[@]}"; do
        echo_color "yellow" "回滚服务: $service"
        kubectl rollout undo deployment/"$service" -n "$NAMESPACE"

        if kubectl rollout status deployment/"$service" -n "$NAMESPACE" --timeout=300s; then
            echo_color "green" "✅ $service 回滚成功"
        else
            echo_color "red" "❌ $service 回滚失败"
        fi
    done
}

# 清理函数
cleanup() {
    echo_color "cyan" "🧹 清理临时资源..."
    # 清理临时文件等
}

# 错误处理
error_handler() {
    local exit_code=$?
    if [[ $exit_code -ne 0 ]]; then
        echo_color "red" "❌ 部署过程中发生错误"
        echo_color "yellow" "是否需要回滚? (y/N)"
        read -r response
        if [[ "$response" =~ ^[Yy]$ ]]; then
            rollback_deployment
        fi
    fi
    cleanup
    exit $exit_code
}

# 设置错误处理
trap error_handler ERR

# 主执行流程
main() {
    # 打印部署信息
    print_deployment_info

    # 检查依赖
    check_dependencies

    # 创建命名空间
    create_namespaces

    # 应用配置
    apply_configs

    # 部署服务
    if [[ "$SERVICE" = "all" ]]; then
        deploy_all_services
    else
        deploy_service "$SERVICE" "$DEPLOYMENT_FILE"
    fi

    # 等待部署完成
    wait_for_deployment

    # 健康检查
    if [[ "$VERIFY" = true ]]; then
        health_check
    fi

    # 显示部署状态
    show_deployment_status

    echo_color "green" "🎉 部署完成!"
}

# 执行主函数
main "$@"