#!/bin/bash
# SmartAdmin Docker 清理脚本
# 用于清理Docker资源

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_header() {
    echo -e "${BLUE}🧹 SmartAdmin Docker 清理脚本${NC}"
    echo -e "${BLUE}$(printf '─%.0s' {1..50})${NC}"
}

# 停止并删除容器
cleanup_containers() {
    print_info "停止并删除SmartAdmin相关容器..."

    # 停止容器
    docker ps -a --filter "name=smart-admin" --format "{{.Names}}" | while read container; do
        if [ -n "$container" ]; then
            docker stop "$container" 2>/dev/null || true
            docker rm "$container" 2>/dev/null || true
            print_success "已删除容器: $container"
        fi
    done
}

# 删除镜像
cleanup_images() {
    print_info "删除SmartAdmin相关镜像..."

    docker images --filter "reference=smart-admin*" --format "{{.Repository}}:{{.Tag}}" | while read image; do
        if [ -n "$image" ]; then
            docker rmi "$image" 2>/dev/null || true
            print_success "已删除镜像: $image"
        fi
    done
}

# 清理网络
cleanup_networks() {
    print_info "清理Docker网络..."

    docker network ls --filter "name=smart-admin" --format "{{.Name}}" | while read network; do
        if [ -n "$network" ]; then
            docker network rm "$network" 2>/dev/null || true
            print_success "已删除网络: $network"
        fi
    done
}

# 清理卷
cleanup_volumes() {
    print_warning "清理Docker卷（将删除数据！）"
    read -p "确定要删除所有数据卷吗？[y/N]: " confirm

    if [[ $confirm =~ ^[Yy]$ ]]; then
        docker volume ls --filter "name=smart-admin" --format "{{.Name}}" | while read volume; do
            if [ -n "$volume" ]; then
                docker volume rm "$volume" 2>/dev/null || true
                print_success "已删除卷: $volume"
            fi
        done
    fi
}

# 清理日志
cleanup_logs() {
    print_info "清理Docker日志..."

    # 清理容器日志
    docker system prune -f

    # 清理本地日志文件
    if [ -d "logs" ]; then
        rm -rf logs/*
        print_success "已清理本地日志文件"
    fi
}

# 完全清理
full_cleanup() {
    print_warning "执行完全清理（将删除所有相关数据！）"
    read -p "确定要执行完全清理吗？[y/N]: " confirm

    if [[ $confirm =~ ^[Yy]$ ]]; then
        cleanup_containers
        cleanup_images
        cleanup_networks
        cleanup_volumes
        cleanup_logs
        print_success "完全清理完成"
    else
        print_info "取消完全清理"
    fi
}

# 选择清理级别
select_cleanup_level() {
    echo -e "${BLUE}请选择清理级别:${NC}"
    echo "1) 轻量清理（只清理停止的容器和未使用的镜像）"
    echo "2) 标准清理（清理容器、镜像和网络）"
    echo "3) 深度清理（清理容器、镜像、网络和数据卷）"
    echo "4) 完全清理（包括日志文件）"

    read -p "请输入选项 [1-4]: " level

    case $level in
        1)
            docker system prune -f
            print_success "轻量清理完成"
            ;;
        2)
            cleanup_containers
            cleanup_images
            cleanup_networks
            docker system prune -f
            print_success "标准清理完成"
            ;;
        3)
            cleanup_containers
            cleanup_images
            cleanup_networks
            cleanup_volumes
            docker system prune -f
            print_success "深度清理完成"
            ;;
        4)
            full_cleanup
            ;;
        *)
            print_warning "无效选项，执行轻量清理"
            docker system prune -f
            print_success "轻量清理完成"
            ;;
    esac
}

# 主函数
main() {
    print_header
    select_cleanup_level
}

# 脚本入口
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi