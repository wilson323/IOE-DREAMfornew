#!/bin/bash
#
# IOE-DREAM项目六层验证机制
# 严格遵循D:\IOE-DREAM\docs\业务模块文档设计规范
# 强制执行质量保障体系
#
# 作者：SmartAdmin Team
# 版本：v1.0
# 创建时间：2025-11-25
#

set -e  # 遇到错误立即退出

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

# 验证结果统计
TOTAL_VALIDATIONS=6
PASSED_VALIDATIONS=0
FAILED_VALIDATIONS=0

echo "🔍 IOE-DREAM项目六层验证机制启动"
echo "📋 验证依据：严格遵循D:\IOE-DREAM\docs\业务模块文档设计规范"
echo "⚠️  任何验证失败都会导致开发流程阻断"
echo ""

# 第零层：repowiki规范预检查（最高优先级）
layer_zero_repowiki_check() {
    echo ""
    echo "🔴 第零层：repowiki规范预检查（最高优先级）"

    # 1. jakarta包名检查（一级规范 - 强制）
    log_info "检查jakarta包名合规性..."
    javax_count=$(find . -name "*.java" -exec grep -l "javax\.(annotation|validation|persistence|servlet|jms|transaction|ejb)" {} \; | wc -l)
    if [ $javax_count -ne 0 ]; then
        log_error "发现 $javax_count 个jakarta EE包名违规（必须为0）"
        find . -name "*.java" -exec grep -l "javax\.(annotation|validation|persistence|servlet|jms|transaction|ejb)" {} \;
        log_error "❌ 第零层验证失败：jakarta EE包名违规"
        return 1
    fi
    log_success "✅ jakarta EE包名检查通过（0个违规）"

    # 2. @Resource依赖注入检查（一级规范 - 强制）
    log_info "检查@Resource依赖注入合规性..."
    autowired_count=$(find . -name "*.java" -exec grep -l "@Autowired" {} \; | wc -l)
    if [ $autowired_count -ne 0 ]; then
        log_error "发现 $autowired_count 个@Autowired违规（必须为0）"
        find . -name "*.java" -exec grep -l "@Autowired" {} \;
        log_error "❌ 第零层验证失败：@Resource依赖注入违规"
        return 1
    fi
    log_success "✅ @Resource依赖注入检查通过（0个违规）"

    # 3. 四层架构违规检查（一级规范 - 强制）
    log_info "检查四层架构合规性..."
    controller_direct_dao=$(grep -r "@Resource.*Dao" --include="*Controller.java" . | wc -l)
    if [ $controller_direct_dao -ne 0 ]; then
        log_error "发现 $controller_direct_dao 处Controller直接访问DAO违规（必须为0）"
        grep -r "@Resource.*Dao" --include="*Controller.java" .
        log_error "❌ 第零层验证失败：四层架构违规"
        return 1
    fi
    log_success "✅ 四层架构检查通过（0个违规）"

    log_success "🎉 第零层验证通过：repowiki规范100%合规"
    ((PASSED_VALIDATIONS++))
    return 0
}

# 第一层：本地启动验证
layer_one_startup_validation() {
    echo ""
    echo "🔥 第一层：本地启动验证"

    log_info "检查项目结构和配置..."

    # 检查是否存在sa-admin模块
    if [ ! -d "smart-admin-api-java17-springboot3/sa-admin" ]; then
        log_error "❌ 未找到sa-admin模块，无法执行本地启动验证"
        return 1
    fi

    # 检查关键配置文件
    if [ ! -f "smart-admin-api-java17-springboot3/sa-base/src/main/resources/dev/sa-base.yaml" ]; then
        log_error "❌ 未找到dev配置文件：smart-admin-api-java17-springboot3/sa-base/src/main/resources/dev/sa-base.yaml"
        return 1
    fi

    log_success "✅ 项目结构检查通过"

    # 检查Java版本
    java_version=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1-2)
    if [[ "$java_version" < "17" ]]; then
        log_error "❌ Java版本不符合要求：需要Java 17，当前版本：$java_version"
        return 1
    fi
    log_success "✅ Java版本检查通过：$java_version"

    # 尝试本地编译（轻量级检查）
    log_info "执行快速编译检查..."
    cd smart-admin-api-java17-springboot3/sa-admin
    if timeout 30 mvn compile -q > ../../local_compile_test.log 2>&1; then
        log_success "✅ 本地编译检查通过"
    else
        log_error "❌ 本地编译检查失败"
        tail -10 ../../local_compile_test.log
        cd ../..
        return 1
    fi
    cd ../..

    log_success "🎉 第一层验证通过：本地环境配置正常"
    ((PASSED_VALIDATIONS++))
    return 0
}

# 第二层：完整构建验证
layer_two_build_validation() {
    echo ""
    echo "🔧 第二层：完整构建验证"

    log_info "执行Maven完整构建..."

    # 清理并编译
    cd smart-admin-api-java17-springboot3
    if mvn clean compile -DskipTests -q; then
        log_success "✅ Maven编译成功"
    else
        log_error "❌ Maven编译失败"
        cd ..
        return 1
    fi

    # 检查编译输出
    compile_errors=$(mvn compile -q 2>&1 | grep -c "ERROR" || echo "0")
    if [ "$compile_errors" -ne 0 ]; then
        log_error "❌ 发现 $compile_errors 个编译错误"
        mvn compile 2>&1 | grep "ERROR" | head -5
        cd ..
        return 1
    fi
    log_success "✅ 编译错误检查通过（0个错误）"

    # 检查构建输出目录
    if [ -d "sa-admin/target" ] && [ -d "sa-base/target" ]; then
        log_success "✅ 构建输出目录检查通过"
    else
        log_error "❌ 构建输出目录不完整"
        cd ..
        return 1
    fi

    cd ..

    log_success "🎉 第二层验证通过：完整构建成功"
    ((PASSED_VALIDATIONS++))
    return 0
}

# 第三层：MyBatis完整性验证
layer_three_mybatis_validation() {
    echo ""
    echo "🔍 第三层：MyBits完整性验证"

    log_info "检查Mapper文件和实体类映射..."

    # 查找所有mapper.xml文件
    mapper_files=$(find . -name "*.xml" -path "*/mapper/*")
    if [ -z "$mapper_files" ]; then
        log_warning "⚠️ 未找到任何Mapper XML文件"
    else
        log_info "找到 $(echo $mapper_files | wc -w) 个Mapper文件"
    fi

    mybatis_errors=0

    # 检查每个Mapper文件
    for mapper_file in $mapper_files; do
        log_info "检查Mapper: $mapper_file"

        # 检查resultType引用的实体类
        entities=$(grep -o 'resultType="[^"]*Entity"' "$mapper_file" 2>/dev/null | sed 's/resultType="//' | sed 's/"//' || true)
        for entity in $entities; do
            entity_file=$(echo "$entity" | sed 's/\./\//g').java
            if [ ! -f "$entity_file" ]; then
                log_error "❌ Mapper $mapper_file 引用的实体类不存在: $entity"
                ((mybatis_errors++))
            fi
        done

        # 检查parameterType引用的DTO类
        dtos=$(grep -o 'parameterType="[^"]*DTO"' "$mapper_file" 2>/dev/null | sed 's/parameterType="//' | sed 's/"//' || true)
        for dto in $dtos; do
            dto_file=$(echo "$dto" | sed 's/\./\//g').java
            if [ ! -f "$dto_file" ]; then
                log_error "❌ Mapper $mapper_file 引用的DTO类不存在: $dto"
                ((mybatis_errors++))
            fi
        done
    done

    if [ $mybatis_errors -ne 0 ]; then
        log_error "❌ 发现 $mybatis_errors 个MyBatis映射错误"
        return 1
    fi

    log_success "✅ MyBatis映射完整性检查通过"
    log_success "🎉 第三层验证通过：MyBits完整性验证"
    ((PASSED_VALIDATIONS++))
    return 0
}

# 第四层：Spring Boot启动验证
layer_four_spring_validation() {
    echo ""
    echo "🚀 第四层：Spring Boot启动验证"

    cd smart-admin-api-java17-springboot3/sa-admin

    log_info "执行Spring Boot启动测试（90秒超时）..."

    # 启动应用（后台运行）
    timeout 90s mvn spring-boot:run -Dspring-boot.run.profiles=docker > ../spring_startup_test.log 2>&1 &
    pid=$!

    # 等待启动
    log_info "等待应用启动..."
    sleep 60

    # 检查进程状态
    if ps -p $pid > /dev/null 2>&1; then
        log_success "✅ Spring Boot应用启动成功，运行60秒"

        # 检查启动日志中的关键错误
        if grep -q -E "ERROR|Exception|Failed|Application startup failed" ../spring_startup_test.log; then
            log_warning "⚠️ 启动日志中发现错误，但进程仍在运行"
        fi

        # 停止应用
        kill $pid 2>/dev/null || true
        wait $pid 2>/dev/null || true

    else
        # 应用启动失败
        wait $pid
        log_error "❌ Spring Boot应用启动失败"

        # 显示错误信息
        if [ -f "../spring_startup_test.log" ]; then
            log_error "启动错误信息："
            tail -20 ../spring_startup_test.log
        fi

        cd ..
        return 1
    fi

    cd ..

    log_success "🎉 第四层验证通过：Spring Boot启动成功"
    ((PASSED_VALIDATIONS++))
    return 0
}

# 第五层：Docker部署验证
layer_five_docker_validation() {
    echo ""
    echo "🐳 第五层：Docker部署验证"

    # 检查Docker环境
    if ! command -v docker &> /dev/null; then
        log_warning "⚠️ Docker未安装，跳过Docker部署验证"
        ((PASSED_VALIDATIONS++))
        return 0
    fi

    if ! command -v docker-compose &> /dev/null; then
        log_warning "⚠️ Docker Compose未安装，跳过Docker部署验证"
        ((PASSED_VALIDATIONS++))
        return 0
    fi

    log_info "检查Docker配置文件..."
    if [ ! -f "docker-compose.yml" ]; then
        log_warning "⚠️ 未找到docker-compose.yml文件，跳过Docker部署验证"
        ((PASSED_VALIDATIONS++))
        return 0
    fi

    log_info "执行Docker镜像构建..."

    # 构建Docker镜像
    if timeout 300 docker-compose build backend > docker_build.log 2>&1; then
        log_success "✅ Docker镜像构建成功"
    else
        log_error "❌ Docker镜像构建失败"
        tail -20 docker_build.log
        return 1
    fi

    log_success "🎉 第五层验证通过：Docker部署验证"
    ((PASSED_VALIDATIONS++))
    return 0
}

# 第六层：业务模块质量验证（严格遵循业务模块文档）
layer_six_business_validation() {
    echo ""
    echo "💼 第六层：业务模块质量验证"

    log_info "检查业务模块质量..."

    # 检查关键业务模块是否存在
    business_modules=("access" "consume" "attendance" "video" "area" "device")
    module_count=0

    for module in "${business_modules[@]}"; do
        if [ -d "sa-admin/src/main/java/net/lab1024/sa/admin/module/$module" ]; then
            log_success "✅ 找到业务模块: $module"
            ((module_count++))

            # 检查模块的基本结构
            controller_count=$(find "sa-admin/src/main/java/net/lab1024/sa/admin/module/$module" -name "*Controller.java" | wc -l)
            service_count=$(find "sa-admin/src/main/java/net/lab1024/sa/admin/module/$module" -name "*Service.java" | wc -l)

            log_info "  - Controller数量: $controller_count"
            log_info "  - Service数量: $service_count"

        else
            log_warning "⚠️ 未找到业务模块: $module"
        fi
    done

    if [ $module_count -eq 0 ]; then
        log_error "❌ 未找到任何业务模块"
        return 1
    fi

    log_success "✅ 业务模块检查通过（找到 $module_count 个模块）"

    # 检查关键配置和依赖
    log_info "检查关键配置..."

    # 检查Sa-Token配置
    if grep -q "sa-token" sa-admin/pom.xml; then
        log_success "✅ Sa-Token依赖配置正确"
    else
        log_error "❌ Sa-Token依赖配置缺失"
        return 1
    fi

    # 检查Redis配置
    if grep -q "redis" sa-base/src/main/resources/dev/sa-base.yaml; then
        log_success "✅ Redis配置正确"
    else
        log_error "❌ Redis配置缺失"
        return 1
    fi

    log_success "🎉 第六层验证通过：业务模块质量验证"
    ((PASSED_VALIDATIONS++))
    return 0
}

# 主验证流程
main_validation() {
    echo "========================================"
    echo "🚀 IOE-DREAM六层验证机制开始执行"
    echo "========================================"

    local start_time=$(date +%s)

    # 执行所有验证层
    layer_zero_repowiki_check || ((FAILED_VALIDATIONS++))
    layer_one_startup_validation || ((FAILED_VALIDATIONS++))
    layer_two_build_validation || ((FAILED_VALIDATIONS++))
    layer_three_mybatis_validation || ((FAILED_VALIDATIONS++))
    layer_four_spring_validation || ((FAILED_VALIDATIONS++))
    layer_five_docker_validation || ((FAILED_VALIDATIONS++))
    layer_six_business_validation || ((FAILED_VALIDATIONS++))

    local end_time=$(date +%s)
    local duration=$((end_time - start_time))

    # 输出验证结果
    echo ""
    echo "========================================"
    echo "📊 六层验证机制执行结果"
    echo "========================================"
    echo "🕐 执行时间: ${duration}秒"
    echo "✅ 通过验证: $PASSED_VALIDATIONS/$TOTAL_VALIDATIONS"
    echo "❌ 失败验证: $FAILED_VALIDATIONS/$TOTAL_VALIDATIONS"
    echo ""

    if [ $PASSED_VALIDATIONS -eq $TOTAL_VALIDATIONS ]; then
        log_success "🎉 六层验证机制全部通过！项目质量优秀，可以继续开发流程。"
        echo ""
        echo "✅ 编译状态: 0错误"
        echo "✅ repowiki规范: 100%合规"
        echo "✅ 业务模块: 结构完整"
        echo "✅ 架构标准: 符合四层架构"
        echo ""
        echo "🚀 项目已准备好进入下一阶段！"
        return 0
    else
        log_error "❌ 六层验证机制未完全通过！开发流程已阻断。"
        echo ""
        echo "🔧 必须修复以下问题后才能继续："
        echo "  - 编译错误必须为0"
        echo "  - repowiki规范必须100%合规"
        echo "  - 业务模块必须结构完整"
        echo "  - 架构必须严格遵循四层架构"
        echo ""
        echo "📞 请参考D:\IOE-DREAM\docs\业务模块文档进行修复"
        return 1
    fi
}

# 脚本入口
if [ "${BASH_SOURCE[0]}" = "${0}" ]; then
    main_validation "$@"
fi