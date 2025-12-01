#!/bin/bash
# SmartAdmin Docker 部署脚本
# 作者: SmartAdmin团队
# 版本: v1.0.0
# 更新: 2025-11-14

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m'

# 项目路径
PROJECT_ROOT=$(cd "$(dirname "$0")/.." && pwd)

# 输出函数
print_header() {
    echo -e "${BLUE}🐳 SmartAdmin Docker 部署脚本${NC}"
    echo -e "${CYAN}📅 部署时间: $(date '+%Y-%m-%d %H:%M:%S')${NC}"
    echo -e "${CYAN}📂 项目路径: $PROJECT_ROOT${NC}"
    echo -e "${BLUE}$(printf '─%.0s' {1..60})${NC}"
}

print_section() {
    echo -e "\n${PURPLE}📋 $1${NC}"
    echo -e "${PURPLE}$(printf '─%.0s' {1..50})${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_info() {
    echo -e "${CYAN}ℹ️  $1${NC}"
}

# 检查前置条件
check_prerequisites() {
    print_section "检查前置条件"

    # 检查Docker
    if ! command -v docker &> /dev/null; then
        print_error "Docker 未安装，请先安装 Docker"
        exit 1
    fi
    print_success "Docker 已安装: $(docker --version)"

    # 检查Docker Compose
    if ! command -v docker-compose &> /dev/null; then
        print_error "Docker Compose 未安装，请先安装 Docker Compose"
        exit 1
    fi
    print_success "Docker Compose 已安装: $(docker-compose --version)"

    # 检查Docker服务状态
    if ! docker info &> /dev/null; then
        print_error "Docker 服务未运行，请启动 Docker 服务"
        exit 1
    fi
    print_success "Docker 服务正在运行"

    # 检查可用内存
    total_mem=$(free -m | awk 'NR==2{print $2}')
    if [ "$total_mem" -lt 4096 ]; then
        print_warning "系统内存不足4GB，可能影响部署性能"
    else
        print_success "系统内存充足: ${total_mem}MB"
    fi

    # 检查磁盘空间
    available_space=$(df -m . | awk 'NR==2{print $4}')
    if [ "$available_space" -lt 10240 ]; then
        print_warning "磁盘空间不足10GB，可能影响部署"
    else
        print_success "磁盘空间充足: ${available_space}MB"
    fi
}

# 选择部署环境
select_environment() {
    print_section "选择部署环境"

    echo -e "${CYAN}请选择部署环境:${NC}"
    echo "1) 开发环境 (包含热重载)"
    echo "2) 生产环境"
    echo "3) 自定义配置"

    read -p "请输入选项 [1-3]: " env_choice

    case $env_choice in
        1)
            ENV_FILE="docker-compose.dev.yml"
            ENV_NAME="开发环境"
            ;;
        2)
            ENV_FILE="docker-compose.yml"
            ENV_NAME="生产环境"
            ;;
        3)
            read -p "请输入自定义配置文件名: " custom_file
            ENV_FILE=$custom_file
            ENV_NAME="自定义环境"
            ;;
        *)
            print_error "无效选项，使用默认生产环境"
            ENV_FILE="docker-compose.yml"
            ENV_NAME="生产环境"
            ;;
    esac

    print_success "已选择: $ENV_NAME"
    print_info "配置文件: $ENV_FILE"
}

# 准备部署环境
prepare_deployment() {
    print_section "准备部署环境"

    cd "$PROJECT_ROOT"

    # 检查配置文件
    if [ ! -f "$ENV_FILE" ]; then
        print_error "配置文件不存在: $ENV_FILE"
        exit 1
    fi
    print_success "配置文件存在: $ENV_FILE"

    # 创建必要的目录
    mkdir -p logs/backend logs/nginx docker/mysql/conf.d docker/redis docker/nginx/ssl

    # 检查数据库脚本
    if [ ! -d "数据库SQL脚本/mysql" ]; then
        print_warning "数据库脚本目录不存在，将跳过数据库初始化"
    else
        print_success "数据库脚本目录存在"
    fi

    # 停止现有服务
    print_info "停止现有服务..."
    docker-compose -f "$ENV_FILE" down 2>/dev/null || true

    # 清理未使用的镜像和容器
    print_info "清理Docker资源..."
    docker system prune -f
}

# 构建和部署
deploy_services() {
    print_section "构建和部署服务"

    print_info "开始构建镜像..."
    if docker-compose -f "$ENV_FILE" build --no-cache; then
        print_success "镜像构建成功"
    else
        print_error "镜像构建失败"
        exit 1
    fi

    print_info "启动服务..."
    if docker-compose -f "$ENV_FILE" up -d; then
        print_success "服务启动成功"
    else
        print_error "服务启动失败"
        exit 1
    fi
}

# 等待服务就绪
wait_for_services() {
    print_section "等待服务就绪"

    print_info "等待数据库服务启动..."
    sleep 30

    # 检查服务状态
    max_attempts=30
    attempt=1

    while [ $attempt -le $max_attempts ]; do
        print_info "检查服务状态... (尝试 $attempt/$max_attempts)"

        # 检查MySQL
        if docker-compose -f "$ENV_FILE" ps mysql | grep -q "Up"; then
            print_success "MySQL 服务运行正常"
        else
            print_warning "MySQL 服务未就绪"
        fi

        # 检查Redis
        if docker-compose -f "$ENV_FILE" ps redis | grep -q "Up"; then
            print_success "Redis 服务运行正常"
        else
            print_warning "Redis 服务未就绪"
        fi

        # 检查后端
        if docker-compose -f "$ENV_FILE" ps backend | grep -q "healthy"; then
            print_success "后端服务健康检查通过"
        else
            print_warning "后端服务健康检查中..."
        fi

        # 检查前端
        if docker-compose -f "$ENV_FILE" ps frontend | grep -q "healthy"; then
            print_success "前端服务健康检查通过"
            break
        else
            print_warning "前端服务健康检查中..."
        fi

        sleep 10
        attempt=$((attempt + 1))
    done

    if [ $attempt -gt $max_attempts ]; then
        print_warning "部分服务可能未完全就绪，请检查日志"
    fi
}

# 验证部署
verify_deployment() {
    print_section "验证部署"

    # 显示服务状态
    print_info "服务状态:"
    docker-compose -f "$ENV_FILE" ps

    # 测试网络连接
    print_info "测试网络连接..."

    # 获取后端服务状态
    if curl -f -s http://localhost:1024/api/health > /dev/null 2>&1; then
        print_success "后端API可访问"
    else
        print_warning "后端API可能需要更多时间启动"
    fi

    # 获取前端服务状态
    if curl -f -s http://localhost:8080 > /dev/null 2>&1; then
        print_success "前端服务可访问"
    elif curl -f -s http://localhost > /dev/null 2>&1; then
        print_success "前端服务可访问（生产端口）"
    else
        print_warning "前端服务可能需要更多时间启动"
    fi
}

# 显示访问信息
show_access_info() {
    print_section "访问信息"

    echo -e "${CYAN}🌐 应用访问地址:${NC}"
    echo -e "  前端应用: ${GREEN}http://localhost:8080${NC} (开发环境)"
    echo -e "           ${GREEN}http://localhost${NC} (生产环境)"
    echo -e "  后端API:  ${GREEN}http://localhost:1024/api${NC}"
    echo -e "  API文档:  ${GREEN}http://localhost:1024/doc.html${NC}"

    echo -e "\n${CYAN}🗄️ 数据库连接:${NC}"
    echo -e "  MySQL:   ${GREEN}localhost:3306${NC}"
    echo -e "           用户: root / smartadmin"
    echo -e "           密码: root1234 / smartadmin123"
    echo -e "  Redis:   ${GREEN}localhost:6379${NC}"
    echo -e "           密码: zkteco3100"

    echo -e "\n${CYAN}📋 常用命令:${NC}"
    echo -e "  查看日志: ${YELLOW}docker-compose -f $ENV_FILE logs -f${NC}"
    echo -e "  停止服务: ${YELLOW}docker-compose -f $ENV_FILE down${NC}"
    echo -e "  重启服务: ${YELLOW}docker-compose -f $ENV_FILE restart${NC}"
    echo -e "  查看状态: ${YELLOW}docker-compose -f $ENV_FILE ps${NC}"
}

# 主函数
main() {
    print_header

    check_prerequisites
    select_environment
    prepare_deployment
    deploy_services
    wait_for_services
    verify_deployment
    show_access_info

    echo -e "\n${GREEN}🎉 部署完成！${NC}"
    echo -e "${BLUE}$(printf '─%.0s' {1..60})${NC}"

    # 询问是否查看日志
    read -p "是否查看实时日志？[y/N]: " view_logs
    if [[ $view_logs =~ ^[Yy]$ ]]; then
        docker-compose -f "$ENV_FILE" logs -f
    fi
}

# 脚本入口
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi