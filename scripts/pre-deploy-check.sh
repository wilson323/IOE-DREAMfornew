#!/bin/bash

# =============================================================================
# 🚨【部署前强制检查机制】Pre-Deploy Check Script
# 作用：在Docker部署前强制检测所有潜在问题，确保0异常部署
# 使用：./scripts/pre-deploy-check.sh
# 作者：老王 - 专业技术保障
# =============================================================================

set -e  # 任何错误立即退出

echo "🔍【部署前强制检查】开始执行..."
echo "⚠️  重要提醒：此检查未通过前，禁止进行任何Docker部署操作！"
echo ""

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 错误计数器
ERRORS=0
WARNINGS=0

# 错误报告函数
error() {
    echo -e "${RED}❌ ERROR: $1${NC}"
    ((ERRORS++))
}

# 警告报告函数
warning() {
    echo -e "${YELLOW}⚠️  WARNING: $1${NC}"
    ((WARNINGS++))
}

# 成功报告函数
success() {
    echo -e "${GREEN}✅ $1${NC}"
}

# 信息报告函数
info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

# =============================================================================
# 第一阶段：环境基础检查
# =============================================================================
echo ""
echo "=========================================="
echo "🏗️  第一阶段：环境基础检查"
echo "=========================================="

# 检查必要工具是否存在
info "检查必要工具..."
command -v docker >/dev/null 2>&1 || error "Docker 未安装或不在PATH中"
command -v docker-compose >/dev/null 2>&1 || error "Docker Compose 未安装或不在PATH中"
command -v node >/dev/null 2>&1 || error "Node.js 未安装或不在PATH中"
command -v npm >/dev/null 2>&1 || error "NPM 未安装或不在PATH中"
command -v java >/dev/null 2>&1 || error "Java 未安装或不在PATH中"
command -v mvn >/dev/null 2>&1 || error "Maven 未安装或不在PATH中"

# 检查端口占用
info "检查端口占用..."
if netstat -tuln 2>/dev/null | grep -q ":1024 "; then
    error "端口 1024 已被占用，可能导致后端启动失败"
else
    success "端口 1024 可用"
fi

if netstat -tuln 2>/dev/null | grep -q ":8081 "; then
    warning "端口 8081 已被占用，可能影响前端启动"
else
    success "端口 8081 可用"
fi

# =============================================================================
# 第二阶段：后端代码质量检查
# =============================================================================
echo ""
echo "=========================================="
echo "☕  第二阶段：后端代码质量检查"
echo "=========================================="

cd smart-admin-api-java17-springboot3

# 检查 javax 包使用（应该为 0）
info "检查 javax 包使用情况..."
javax_count=$(find . -name "*.java" -exec grep -l "javax\." {} \; 2>/dev/null | wc -l)
if [ $javax_count -ne 0 ]; then
    error "发现 javax 包使用: $javax_count 个文件"
    find . -name "*.java" -exec grep -l "javax\." {} \; 2>/dev/null | head -5
else
    success "未发现 javax 包使用，符合 Spring Boot 3.x 规范"
fi

# 检查 @Autowired 使用（应该为 0）
info "检查 @Autowired 使用情况..."
autowired_count=$(find . -name "*.java" -exec grep -l "@Autowired" {} \; 2>/dev/null | wc -l)
if [ $autowired_count -ne 0 ]; then
    error "发现 @Autowired 使用: $autowired_count 个文件，应该使用 @Resource"
    find . -name "*.java" -exec grep -l "@Autowired" {} \; 2>/dev/null | head -5
else
    success "未发现 @Autowired 使用，符合规范"
fi

# 检查 System.out.println 使用（应该为 0）
info "检查 System.out.println 使用情况..."
sout_count=$(find . -name "*.java" -exec grep -l "System\.out\.println" {} \; 2>/dev/null | wc -l)
if [ $sout_count -ne 0 ]; then
    error "发现 System.out.println 使用: $sout_count 个文件，应该使用 SLF4J"
    find . -name "*.java" -exec grep -l "System\.out\.println" {} \; 2>/dev/null | head -5
else
    success "未发现 System.out.println 使用，符合日志规范"
fi

# Maven 编译检查
info "执行 Maven 编译检查..."
if mvn clean compile -q -DskipTests; then
    success "Maven 编译通过"
else
    error "Maven 编译失败，请检查代码错误"
fi

# =============================================================================
# 第三阶段：前端代码质量检查
# =============================================================================
echo ""
echo "=========================================="
echo "🌐 第三阶段：前端代码质量检查"
echo "=========================================="

cd ../smart-admin-web-javascript

# 检查 Vue 组件完整性
info "检查 Vue 组件完整性..."
required_components=(
    "src/components/realtime/functional/DeviceStatusChart.vue"
    "src/components/realtime/functional/EventStatisticsPanel.vue"
)

for component in "${required_components[@]}"; do
    if [ -f "$component" ]; then
        success "Vue组件存在: $component"
    else
        error "Vue组件缺失: $component"
    fi
done

# 检查 package.json 依赖
info "检查 package.json 依赖..."
if [ -f "package.json" ]; then
    if npm list --depth=0 >/dev/null 2>&1; then
        success "NPM 依赖完整"
    else
        error "NPM 依赖不完整，请运行 npm install"
    fi
else
    error "package.json 文件不存在"
fi

# 检查 Vite 配置
# 检查前后端地址一致性（CORS预防）info "检查前后端地址一致性..."if [ -f "vite.config.js" ]; then    # 检查前端代理配置是否使用localhost    if grep -q "target:.*localhost:" vite.config.js; then        success "前端代理配置使用localhost"    else        error "前端代理配置未使用localhost，可能导致CORS问题"        echo "当前配置："        grep "target:" vite.config.js    fi    # 检查是否混用localhost和127.0.0.1    if grep -q "127.0.0.1" vite.config.js; then        error "前端配置中发现127.0.0.1，应统一使用localhost"    else        success "前端配置未发现127.0.0.1"    fielse    error "vite.config.js 文件不存在"fi
info "检查 Vite 配置..."
if [ -f "vite.config.js" ]; then
    success "Vite 配置文件存在"
else
    error "vite.config.js 文件不存在"
fi

# =============================================================================
# 第四阶段：本地启动测试
# =============================================================================
echo ""
echo "=========================================="
echo "🚀 第四阶段：本地启动测试"
echo "=========================================="

# 后端本地启动测试
info "执行后端本地启动测试..."
cd ../smart-admin-api-java17-springboot3/sa-admin

# 创建临时日志文件
temp_log="../temp_startup_test.log"

# 启动应用（后台运行，60秒超时）
timeout 90s mvn spring-boot:run -Dspring-boot.run.profiles=docker > "$temp_log" 2>&1 &
pid=$!

# 等待启动
info "等待应用启动..."
sleep 60

# 检查进程状态
if ps -p $pid > /dev/null 2>&1; then
    success "应用成功启动，持续运行60秒"
    kill $pid 2>/dev/null || true
    wait $pid 2>/dev/null || true
else
    wait $pid
    # 检查启动日志
    if grep -q "Application run failed\|ERROR\|Exception" "$temp_log"; then
        error "应用启动失败"
        tail -20 "$temp_log"
    else
        success "应用启动并正常退出"
    fi
fi

# 检查启动日志中的异常
info "分析启动日志异常..."
if [ -f "$temp_log" ]; then
    error_patterns=("ERROR" "Exception" "Failed" "Unable to" "Connection refused" "BeanCreationException")
    critical_errors=0

    for pattern in "${error_patterns[@]}"; do
        error_count=$(grep -i "$pattern" "$temp_log" | wc -l)
        if [ $error_count -gt 3 ]; then  # 允许少量重试错误
            error "启动日志发现 $pattern 错误: $error_count 次"
            grep -i "$pattern" "$temp_log" | head -3
            ((critical_errors++))
        fi
    done

    # 检查启动成功标志
    if grep -q "Started.*Application\|Application.*started\|Tomcat.*started" "$temp_log"; then
        success "应用显示启动成功标志"
    else
        error "应用未显示启动成功标志"
    fi

    rm -f "$temp_log"
fi

# =============================================================================
# 第五阶段：前端构建测试
# =============================================================================
echo ""
echo "=========================================="
echo "📦 第五阶段：前端构建测试"
echo "=========================================="

cd ../../smart-admin-web-javascript

# 清理缓存
info "清理前端缓存..."
rm -rf node_modules/.vite 2>/dev/null || true

# 执行构建测试
info "执行前端构建测试..."
if npm run build:test; then
    success "前端构建测试通过"
else
    error "前端构建测试失败"
fi

# =============================================================================
# 第六阶段：Docker 环境检查
# =============================================================================
echo ""
echo "=========================================="
echo "🐳 第六阶段：Docker 环境检查"
echo "=========================================="

cd ..

# 检查 Docker 服务状态
info "检查 Docker 服务状态..."
if docker info >/dev/null 2>&1; then
    success "Docker 服务正常"
else
    error "Docker 服务未运行或权限不足"
fi

# 检查 docker-compose 文件
info "检查 docker-compose 配置..."
if [ -f "docker-compose.yml" ]; then
    success "docker-compose.yml 文件存在"
    if docker-compose config >/dev/null 2>&1; then
        success "docker-compose 配置语法正确"
    else
        error "docker-compose 配置语法错误"
    fi
else
    error "docker-compose.yml 文件不存在"
fi

# 检查 Docker 镜像
info "检查基础镜像..."
required_images=("mysql:8.0" "redis:7.2-alpine")
for image in "${required_images[@]}"; do
    if docker images --format "table {{.Repository}}:{{.Tag}}" | grep -q "$image"; then
        success "基础镜像存在: $image"
    else
        warning "基础镜像不存在: $image (将在构建时下载)"
    fi
done

# =============================================================================
# 最终报告
# =============================================================================
echo ""
echo "=========================================="
echo "📋 最终检查报告"
echo "=========================================="

if [ $ERRORS -eq 0 ]; then
    echo -e "${GREEN}🎉 所有检查通过！可以进行 Docker 部署。${NC}"
    echo ""
    echo "✅ 后端代码质量：通过"
    echo "✅ 前端代码质量：通过"
    echo "✅ 本地启动测试：通过"
    echo "✅ 前端构建测试：通过"
    echo "✅ Docker 环境：通过"
    echo ""
    echo "🚀 现在可以安全执行以下命令进行部署："
    echo "   docker-compose build backend"
    echo "   docker-compose up -d"
    exit 0
else
    echo -e "${RED}❌ 发现 $ERRORS 个错误，$WARNINGS 个警告${NC}"
    echo ""
    echo "🚫 禁止进行 Docker 部署！"
    echo ""
    echo "请修复以下问题后重新运行检查："
    echo "1. 修复所有 ERROR 级别问题"
    echo "2. 建议：修复所有 WARNING 级别问题"
    echo "3. 重新运行: ./scripts/pre-deploy-check.sh"
    echo ""
    exit 1
fi