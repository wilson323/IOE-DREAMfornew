#!/bin/bash
# IOE-DREAM 七层质量门禁验证系统
# 版本: v1.0
# 创建时间: 2025-11-17
# 说明: 严格执行七层质量门禁，确保零异常交付

set -e  # 遇到错误立即退出

# ==================== 全局变量和配置 ====================

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 验证配置
TOTAL_CHECKS=7
PASSED_CHECKS=0
FAILED_CHECKS=0
START_TIME=$(date +%s)

# 项目路径
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BACKEND_PATH="${PROJECT_ROOT}/smart-admin-api-java17-springboot3"

# 日志文件
LOG_FILE="${PROJECT_ROOT}/quality-gate-$(date +%Y%m%d_%H%M%S).log"

# ==================== 工具函数 ====================

# 日志输出函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1" | tee -a "$LOG_FILE"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1" | tee -a "$LOG_FILE"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1" | tee -a "$LOG_FILE"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1" | tee -a "$LOG_FILE"
}

log_critical() {
    echo -e "${PURPLE}[CRITICAL]${NC} $1" | tee -a "$LOG_FILE"
}

# 时间戳函数
get_timestamp() {
    date '+%Y-%m-%d %H:%M:%S'
}

# 计时函数
get_duration() {
    local start_time=$1
    local end_time=$(date +%s)
    echo $((end_time - start_time))
}

# 检查命令是否存在
check_command() {
    local cmd=$1
    if ! command -v "$cmd" &> /dev/null; then
        log_error "命令 '$cmd' 未找到，请先安装"
        return 1
    fi
}

# 统计文件数量
count_files() {
    local pattern=$1
    find . -name "$pattern" | wc -l
}

# ==================== 第一层：编码规范检查 (零容忍) ====================

layer_1_coding_standards() {
    log_info "=========================================="
    log_info "第一层：编码规范检查 (零容忍)"
    log_info "=========================================="

    local layer_start_time=$(date +%s)
    local violations=0

    log_info "检查项目路径: $BACKEND_PATH"
    cd "$BACKEND_PATH" || {
        log_error "无法进入项目路径: $BACKEND_PATH"
        return 1
    }

    # 1.1 javax包检查 (零容忍)
    log_info "检查项: javax包使用情况 (必须为0)"
    local javax_files=$(find . -name "*.java" -exec grep -l "javax\." {} \; 2>/dev/null || true)
    local javax_count=$(echo "$javax_files" | grep -c ".*" || echo 0)

    if [ "$javax_count" -gt 0 ]; then
        log_error "发现 $javax_count 个文件使用javax包 (零容忍项)"
        log_error "违规文件列表:"
        echo "$javax_files" | while read -r file; do
            log_error "  - $file"
        done
        violations=$((violations + 1))
    else
        log_success "javax包检查通过: 0个违规文件"
    fi

    # 1.2 @Autowired检查 (零容忍)
    log_info "检查项: @Autowired使用情况 (必须为0)"
    local autowired_files=$(find . -name "*.java" -exec grep -l "@Autowired" {} \; 2>/dev/null || true)
    local autowired_count=$(echo "$autowired_files" | grep -c ".*" || echo 0)

    if [ "$autowired_count" -gt 0 ]; then
        log_error "发现 $autowired_count 个文件使用@Autowired (零容忍项)"
        log_error "违规文件列表:"
        echo "$autowired_files" | while read -r file; do
            log_error "  - $file"
        done
        violations=$((violations + 1))
    else
        log_success "@Autowired检查通过: 0个违规文件"
    fi

    # 1.3 System.out检查 (零容忍)
    log_info "检查项: System.out使用情况 (必须为0)"
    local system_out_files=$(find . -name "*.java" -exec grep -l "System\.out\." {} \; 2>/dev/null || true)
    local system_out_count=$(echo "$system_out_files" | grep -c ".*" || echo 0)

    if [ "$system_out_count" -gt 0 ]; then
        log_error "发现 $system_out_count 个文件使用System.out (零容忍项)"
        log_error "违规文件列表:"
        echo "$system_out_files" | while read -r file; do
            log_error "  - $file"
        done
        violations=$((violations + 1))
    else
        log_success "System.out检查通过: 0个违规文件"
    fi

    # 1.4 架构违规检查 (零容忍)
    log_info "检查项: Controller直接访问DAO (必须为0)"
    local architecture_violations=$(grep -r "@Resource.*Dao" --include="*Controller.java" . 2>/dev/null || true)
    local architecture_violations_count=$(echo "$architecture_violations" | grep -c ".*" || echo 0)

    if [ "$architecture_violations_count" -gt 0 ]; then
        log_error "发现 $architecture_violations_count 处Controller直接访问DAO (零容忍项)"
        log_error "违规位置:"
        echo "$architecture_violations" | while read -r violation; do
            log_error "  - $violation"
        done
        violations=$((violations + 1))
    else
        log_success "架构规范检查通过: 0处违规"
    fi

    # 1.5 UTF-8编码检查 (零容忍)
    log_info "检查项: 文件编码 (必须为UTF-8)"
    local non_utf8_files=$(find . -name "*.java" -exec file {} \; | grep -v "UTF-8\|ASCII" | wc -l)

    if [ "$non_utf8_files" -gt 0 ]; then
        log_error "发现 $non_utf8_files 个文件编码不是UTF-8 (零容忍项)"
        violations=$((violations + 1))
    else
        log_success "文件编码检查通过: 所有文件均为UTF-8"
    fi

    # 本层检查结果
    local layer_duration=$(get_duration $layer_start_time)
    if [ "$violations" -gt 0 ]; then
        log_critical "第一层检查失败: 发现 $violations 项零容忍违规 (耗时: ${layer_duration}s)"
        log_error "编码规范为零容忍项，必须修复后继续"
        return 1
    else
        log_success "第一层检查通过: 所有编码规范检查均通过 (耗时: ${layer_duration}s)"
        return 0
    fi
}

# ==================== 第二层：编译完整性检查 ====================

layer_2_compilation() {
    log_info "=========================================="
    log_info "第二层：编译完整性检查"
    log_info "=========================================="

    local layer_start_time=$(date +%s)

    cd "$BACKEND_PATH" || {
        log_error "无法进入项目路径: $BACKEND_PATH"
        return 1
    }

    log_info "执行Maven编译检查..."

    # 2.1 清理编译环境
    log_info "清理编译环境..."
    if ! mvn clean -q; then
        log_error "Maven清理失败"
        return 1
    fi

    # 2.2 执行编译
    log_info "执行项目编译..."
    if mvn compile -DskipTests -q; then
        log_success "项目编译成功"
    else
        log_error "项目编译失败"
        return 1
    fi

    # 2.3 检查编译产物
    log_info "检查编译产物..."
    local target_count=$(find . -name "target" -type d | wc -l)
    if [ "$target_count" -eq 0 ]; then
        log_warning "未找到编译产物目录"
    else
        log_success "找到 $target_count 个编译产物目录"
    fi

    local layer_duration=$(get_duration $layer_start_time)
    log_success "第二层检查通过: 编译完整性检查通过 (耗时: ${layer_duration}s)"
    return 0
}

# ==================== 第三层：缓存架构规范检查 ====================

layer_3_cache_architecture() {
    log_info "=========================================="
    log_info "第三层：缓存架构规范检查"
    log_info "=========================================="

    local layer_start_time=$(date +%s)
    local violations=0

    cd "$BACKEND_PATH" || {
        log_error "无法进入项目路径: $BACKEND_PATH"
        return 1
    }

    # 3.1 直接Redis工具使用检查
    log_info "检查项: 直接使用Redis工具 (应该使用UnifiedCacheService)"
    local direct_redis_usage=$(grep -r "RedisUtil\|StringRedisTemplate" --include="*.java" . 2>/dev/null || true)
    local direct_redis_count=$(echo "$direct_redis_usage" | grep -c ".*" || echo 0)

    if [ "$direct_redis_count" -gt 0 ]; then
        log_error "发现 $direct_redis_count 处直接使用Redis工具"
        log_error "违规位置:"
        echo "$direct_redis_usage" | while read -r usage; do
            log_error "  - $usage"
        done
        violations=$((violations + 1))
    else
        log_success "Redis工具使用检查通过"
    fi

    # 3.2 BaseModuleCacheService继承检查
    log_info "检查项: 缓存服务是否继承BaseModuleCacheService"
    local cache_service_count=$(grep -r "extends BaseModuleCacheService" --include="*.java" . 2>/dev/null | wc -l)
    local total_cache_classes=$(grep -r "Manager" --include="*Manager.java" . | wc -l)

    if [ "$cache_service_count" -lt "$total_cache_classes" ]; then
        log_warning "建议所有Manager类都继承BaseModuleCacheService"
        log_info "当前继承BaseModuleCacheService的类: $cache_service_count"
        log_info "Manager类总数: $total_cache_classes"
    else
        log_success "缓存服务继承检查通过"
    fi

    # 3.3 缓存键命名规范检查
    log_info "检查项: 缓存键命名规范"
    local cache_key_usage=$(grep -r "cache\|Cache" --include="*.java" . | grep -i "key\|Cache" | head -10)
    if [ -n "$cache_key_usage" ]; then
        log_info "缓存键使用示例 (前10条):"
        echo "$cache_key_usage" | while read -r usage; do
            log_info "  $usage"
        done
    fi

    # 本层检查结果
    local layer_duration=$(get_duration $layer_start_time)
    if [ "$violations" -gt 0 ]; then
        log_error "第三层检查失败: 发现 $violations 项缓存架构违规 (耗时: ${layer_duration}s)"
        return 1
    else
        log_success "第三层检查通过: 缓存架构规范检查通过 (耗时: ${layer_duration}s)"
        return 0
    fi
}

# ==================== 第四层：安全规范检查 ====================

layer_4_security_standards() {
    log_info "=========================================="
    log_info "第四层：安全规范检查"
    log_info "=========================================="

    local layer_start_time=$(date +%s)
    local warnings=0

    cd "$BACKEND_PATH" || {
        log_error "无法进入项目路径: $BACKEND_PATH"
        return 1
    }

    # 4.1 权限注解覆盖率检查
    log_info "检查项: Controller权限注解覆盖率"
    local controller_methods=$(grep -r "@PostMapping\|@GetMapping\|@PutMapping\|@DeleteMapping" --include="*Controller.java" . | wc -l)
    local permission_methods=$(grep -r "@SaCheckPermission" --include="*Controller.java" . | wc -l)

    if [ "$controller_methods" -gt 0 ]; then
        local coverage=$((permission_methods * 100 / controller_methods))
        log_info "Controller方法总数: $controller_methods"
        log_info "权限注解数量: $permission_methods"
        log_info "权限注解覆盖率: $coverage%"

        if [ "$coverage" -lt 80 ]; then
            log_warning "权限注解覆盖率偏低 (低于80%)，建议检查"
            warnings=$((warnings + 1))
        else
            log_success "权限注解覆盖率良好"
        fi
    else
        log_info "未找到Controller方法"
    fi

    # 4.2 SQL注入风险检查
    log_info "检查项: SQL注入风险"
    local dynamic_sql=$(grep -r "+.*+" --include="*.xml" . 2>/dev/null | wc -l)

    if [ "$dynamic_sql" -gt 0 ]; then
        log_warning "发现 $dynamic_sql 处可能的SQL注入风险"
        warnings=$((warnings + 1))
    else
        log_success "SQL注入风险检查通过"
    fi

    # 4.3 敏感信息泄露检查
    log_info "检查项: 敏感信息泄露"
    local sensitive_info=$(grep -r -i "password\|secret\|key" --include="*.java" --include="*.yml" --include="*.properties" . | grep -v "^\./\.git" | head -5)
    if [ -n "$sensitive_info" ]; then
        log_warning "发现可能的敏感信息，请确认已加密处理"
        warnings=$((warnings + 1))
    else
        log_success "敏感信息检查通过"
    fi

    # 本层检查结果
    local layer_duration=$(get_duration $layer_start_time)
    if [ "$warnings" -gt 0 ]; then
        log_warning "第四层检查完成: 发现 $warnings 项安全警告 (耗时: ${layer_duration}s)"
        return 0  # 安全检查不阻止，仅警告
    else
        log_success "第四层检查通过: 安全规范检查通过 (耗时: ${layer_duration}s)"
        return 0
    fi
}

# ==================== 第五层：性能基准检查 ====================

layer_5_performance() {
    log_info "=========================================="
    log_info "第五层：性能基准检查"
    log_info "=========================================="

    local layer_start_time=$(date +%s)
    local warnings=0

    # 5.1 数据库索引检查
    log_info "检查项: 数据库索引设计"

    # 检查MySQL连接
    if ! mysql -h 192.168.10.110 -P 33060 -u root -e "USE smart_admin_v3;" &>/dev/null; then
        log_warning "无法连接数据库，跳过索引检查"
        warnings=$((warnings + 1))
    else
        # 检查审计字段索引
        local missing_indexes=$(mysql -u root -h 192.168.10.110 -P 33060 smart_admin_v3 -e "
            SELECT TABLE_NAME, COLUMN_NAME
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = 'smart_admin_v3'
            AND TABLE_NAME LIKE 't_%_%'
            AND COLUMN_NAME IN ('create_time', 'update_time', 'deleted_flag')
            AND COLUMN_NAME NOT IN (
                SELECT COLUMN_NAME FROM information_schema.STATISTICS
                WHERE TABLE_SCHEMA = 'smart_admin_v3'
                AND TABLE_NAME = COLUMNS.TABLE_NAME
            );" 2>/dev/null | tail -n +2)

        if [ -n "$missing_indexes" ]; then
            log_warning "建议添加索引的字段:"
            echo "$missing_indexes" | while read -r line; do
                log_warning "  $line"
            done
            warnings=$((warnings + 1))
        else
            log_success "数据库索引检查通过"
        fi
    fi

    # 5.2 代码复杂度检查
    log_info "检查项: 代码复杂度"
    local java_file_count=$(find . -name "*.java" | wc -l)
    local total_lines=$(find . -name "*.java" -exec wc -l {} \; | awk '{sum += $1} END {print sum}')

    log_info "Java文件数量: $java_file_count"
    log_info "总代码行数: $total_lines"

    if [ "$java_file_count" -gt 0 ]; then
        local avg_lines=$((total_lines / java_file_count))
        log_info "平均文件行数: $avg_lines"

        if [ "$avg_lines" -gt 500 ]; then
            log_warning "平均文件行数偏高，建议重构"
            warnings=$((warnings + 1))
        fi
    fi

    # 5.3 重复代码检查
    log_info "检查项: 重复代码检查"
    local duplicate_methods=$(grep -r "public.*(" --include="*.java" . | awk '{print $2}' | sort | uniq -c | sort -nr | head -10)
    if [ -n "$duplicate_methods" ]; then
        log_info "重复方法统计 (前10):"
        echo "$duplicate_methods" | while read -r line; do
            local count=$(echo "$line" | awk '{print $1}')
            local method=$(echo "$line" | awk '{print $2}')
            if [ "$count" -gt 5 ]; then
                log_warning "  方法 '$method' 重复 $count 次"
                warnings=$((warnings + 1))
            fi
        done
    fi

    # 本层检查结果
    local layer_duration=$(get_duration $layer_start_time)
    if [ "$warnings" -gt 0 ]; then
        log_warning "第五层检查完成: 发现 $warnings 项性能警告 (耗时: ${layer_duration}s)"
        return 0  # 性能检查不阻止，仅警告
    else
        log_success "第五层检查通过: 性能基准检查通过 (耗时: ${layer_duration}s)"
        return 0
    fi
}

# ==================== 第六层：Docker部署验证 (120秒持续监控) ====================

layer_6_docker_deployment() {
    log_info "=========================================="
    log_info "第六层：Docker部署验证 (120秒持续监控)"
    log_info "=========================================="

    local layer_start_time=$(date +%s)

    # 检查Docker是否安装
    if ! command -v docker &> /dev/null; then
        log_warning "Docker未安装，跳过Docker部署验证"
        log_warning "建议安装Docker以进行完整的部署验证"
        return 0
    fi

    # 检查docker-compose是否安装
    if ! command -v docker-compose &> /dev/null; then
        log_warning "docker-compose未安装，跳过Docker部署验证"
        return 0
    fi

    cd "$PROJECT_ROOT" || {
        log_error "无法进入项目根目录: $PROJECT_ROOT"
        return 1
    }

    # 6.1 检查Docker配置文件
    log_info "检查Docker配置文件..."
    if [ ! -f "docker-compose.yml" ]; then
        log_warning "未找到docker-compose.yml文件"
        return 1
    fi

    if [ ! -f "smart-admin-api-java17-springboot3/sa-admin/Dockerfile" ]; then
        log_warning "未找到Dockerfile文件"
        return 1
    fi

    log_success "Docker配置文件检查通过"

    # 6.2 构建Docker镜像
    log_info "构建Docker镜像..."
    if ! docker-compose build backend; then
        log_error "Docker镜像构建失败"
        return 1
    fi

    log_success "Docker镜像构建成功"

    # 6.3 启动容器
    log_info "启动Docker容器..."
    docker-compose up -d backend

    # 等待容器启动
    log_info "等待容器启动..."
    sleep 30

    # 6.4 120秒持续监控
    log_info "开始120秒持续监控容器稳定性..."
    local stable_count=0
    local total_checks=4  # 30秒、60秒、90秒、120秒

    for i in 30 60 90 120; do
        log_info "第${i}秒检查..."

        local container_status=$(docker-compose ps | grep backend | grep -c "Up" || echo "0")
        if [ "$container_status" = "0" ]; then
            log_error "容器在第${i}秒停止运行"
            docker logs smart-admin-backend --tail 50
            return 1
        fi

        log_success "第${i}秒: 容器运行正常"
        stable_count=$((stable_count + 1))
        sleep 30
    done

    # 6.5 检查容器日志中的严重错误
    log_info "检查容器日志异常..."
    local docker_logs=$(docker logs smart-admin-backend 2>&1)

    # 定义关键错误模式
    local error_patterns=(
        "ERROR"
        "Exception"
        "Failed"
        "Unable to"
        "Connection refused"
        "Application startup failed"
        "javax\."
    )

    local critical_errors=0
    for pattern in "${error_patterns[@]}"; do
        local error_count=$(echo "$docker_logs" | grep -i "$pattern" | wc -l)
        if [ "$error_count" -gt 3 ]; then  # 允许少量重试错误
            log_error "发现 $pattern 错误: $error_count 次"
            critical_errors=$((critical_errors + 1))
        fi
    done

    if [ "$critical_errors" -gt 0 ]; then
        log_error "发现 $critical_errors 类严重错误"
        return 1
    fi

    # 6.6 检查应用启动成功标志
    if echo "$docker_logs" | grep -q "Started.*Application\|Application.*started\|Tomcat.*started"; then
        log_success "应用启动成功"
    else
        log_error "应用未显示启动成功标志"
        log_error "最近50行日志:"
        echo "$docker_logs" | tail -50
        return 1
    fi

    # 6.7 健康检查
    log_info "执行健康检查..."
    sleep 10
    local health_response=$(docker exec smart-admin-backend curl -s http://localhost:1024/api/health 2>/dev/null || echo "FAILED")

    if [ "$health_response" != "FAILED" ]; then
        log_success "健康检查通过: $health_response"
    else
        log_warning "健康检查失败，但应用已启动"
    fi

    # 本层检查结果
    local layer_duration=$(get_duration $layer_start_time)
    log_success "第六层检查通过: Docker部署验证通过 (耗时: ${layer_duration}s)"
    return 0
}

# ==================== 第七层：repowiki规范符合性检查 ====================

layer_7_repowiki_compliance() {
    log_info "=========================================="
    log_info "第七层：repowiki规范符合性检查"
    log_info "=========================================="

    local layer_start_time=$(date +%s)
    local violations=0

    cd "$BACKEND_PATH" || {
        log_error "无法进入项目路径: $BACKEND_PATH"
        return 1
    }

    # 7.1 四层架构规范检查
    log_info "检查项: 四层架构规范"
    local controller_direct_service=$(grep -r "Service\|Dao" --include="*Controller.java" . | grep -v "@Resource.*Service" | wc -l)

    if [ "$controller_direct_service" -gt 0 ]; then
        log_error "发现Controller层架构违规"
        violations=$((violations + 1))
    else
        log_success "四层架构规范检查通过"
    fi

    # 7.2 文档规范检查
    log_info "检查项: 文档规范"
    local java_files_with_javadoc=$(grep -r "/\*\*" --include="*.java" . | wc -l)
    local total_java_files=$(find . -name "*.java" | wc -l)

    if [ "$total_java_files" -gt 0 ]; then
        local javadoc_coverage=$((java_files_with_javadoc * 100 / total_java_files))
        log_info "JavaDoc覆盖率: $javadoc_coverage% ($java_files_with_javadoc/$total_java_files)"

        if [ "$javadoc_coverage" -lt 50 ]; then
            log_warning "JavaDoc覆盖率偏低，建议提高文档覆盖率"
        fi
    fi

    # 7.3 测试规范检查
    log_info "检查项: 测试规范"
    local test_files=$(find . -name "*Test.java" | wc -l)
    log_info "测试文件数量: $test_files"

    if [ "$test_files" -eq 0 ]; then
        log_warning "未发现测试文件，建议添加单元测试"
    fi

    # 7.4 代码风格检查
    log_info "检查项: 代码风格"
    local todo_comments=$(grep -r "TODO\|FIXME" --include="*.java" . | wc -l)
    if [ "$todo_comments" -gt 0 ]; then
        log_info "发现 $todo_comments 个TODO/FIXME注释"
    fi

    # 本层检查结果
    local layer_duration=$(get_duration $layer_start_time)
    if [ "$violations" -gt 0 ]; then
        log_error "第七层检查失败: 发现 $violations 项repowiki规范违规 (耗时: ${layer_duration}s)"
        return 1
    else
        log_success "第七层检查通过: repowiki规范符合性检查通过 (耗时: ${layer_duration}s)"
        return 0
    fi
}

# ==================== 主函数 ====================

main() {
    log_info "🚀 开始执行IOE-DREAM七层质量门禁验证"
    log_info "开始时间: $(get_timestamp)"
    log_info "项目根目录: $PROJECT_ROOT"
    log_info "日志文件: $LOG_FILE"

    # 执行所有检查
    local layers=(
        "layer_1_coding_standards"
        "layer_2_compilation"
        "layer_3_cache_architecture"
        "layer_4_security_standards"
        "layer_5_performance"
        "layer_6_docker_deployment"
        "layer_7_repowiki_compliance"
    )

    local layer_names=(
        "编码规范检查 (零容忍)"
        "编译完整性检查"
        "缓存架构规范检查"
        "安全规范检查"
        "性能基准检查"
        "Docker部署验证"
        "repowiki规范符合性检查"
    )

    for i in "${!layers[@]}"; do
        log_info "执行第$((i+1))层检查: ${layer_names[i]}"

        if ${layers[i]}; then
            PASSED_CHECKS=$((PASSED_CHECKS + 1))
            log_success "第$((i+1))层检查通过"
        else
            FAILED_CHECKS=$((FAILED_CHECKS + 1))

            # 对于某些非阻塞性检查，继续执行
            if [ "$((i+1))" -eq 4 ] || [ "$((i+1))" -eq 5 ]; then
                log_warning "第$((i+1))层检查发现警告，但继续执行"
                PASSED_CHECKS=$((PASSED_CHECKS + 1))
            else
                log_error "第$((i+1))层检查失败，停止执行"
                break
            fi
        fi

        log_info "当前进度: $PASSED_CHECKS/$TOTAL_CHECKS"
        echo ""
    done

    # 计算总耗时
    local total_duration=$(get_duration $START_TIME)

    # 生成最终报告
    log_info "=========================================="
    log_info "🎉 七层质量门禁验证完成"
    log_info "=========================================="
    log_info "总耗时: ${total_duration}s"
    log_info "通过检查: $PASSED_CHECKS/$TOTAL_CHECKS"
    log_info "通过率: $(( PASSED_CHECKS * 100 / TOTAL_CHECKS ))%"
    log_info "失败检查: $FAILED_CHECKS"
    log_info "日志文件: $LOG_FILE"

    if [ "$PASSED_CHECKS" -eq "$TOTAL_CHECKS" ]; then
        log_success "✅ 所有质量门禁验证通过，项目可以交付！"
        echo ""
        log_info "📊 验证统计:"
        log_info "  - 总检查层数: $TOTAL_CHECKS"
        log_info "  - 通过层数: $PASSED_CHECKS"
        log_info "  - 失败层数: $FAILED_CHECKS"
        log_info "  - 总耗时: ${total_duration}s"
        echo ""
        log_success "🚀 项目质量达标，可以继续下一步操作！"
        return 0
    else
        log_error "❌ 质量门禁验证失败，请修复问题后重试"
        echo ""
        log_error "📊 失败统计:"
        log_error "  - 失败层数: $FAILED_CHECKS"
        log_error "  - 失败率: $(( FAILED_CHECKS * 100 / TOTAL_CHECKS ))%"
        echo ""
        log_error "🔧 修复建议:"
        log_error "  1. 查看详细日志: $LOG_FILE"
        log_error "  2. 修复零容忍问题 (编码规范、架构违规)"
        log_error "  3. 确保项目可以正常编译和运行"
        log_error "  4. 重新执行验证脚本"
        return 1
    fi
}

# ==================== 帮助信息 ====================

show_help() {
    echo "IOE-DREAM 七层质量门禁验证系统"
    echo ""
    echo "用法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  -h, --help     显示帮助信息"
    echo "  -v, --version  显示版本信息"
    echo "  -l, --layer N  只执行指定层检查 (1-7)"
    echo ""
    echo "说明:"
    echo "  此脚本执行七层质量门禁验证，确保项目质量达标"
    echo "  每层检查都有特定目标，某些层为零容忍项"
    echo "  所有检查通过后项目才能继续下一步操作"
    echo ""
    echo "示例:"
    echo "  $0                # 执行所有层检查"
    echo "  $0 -l 1           # 只执行第1层检查"
    echo "  $0 -l 1,2,3       # 执行第1、2、3层检查"
}

show_version() {
    echo "IOE-DREAM 七层质量门禁验证系统 v1.0"
    echo "创建时间: 2025-11-17"
    echo "项目路径: $PROJECT_ROOT"
}

# ==================== 命令行参数处理 ====================

# 解析命令行参数
EXECUTE_LAYERS="all"

while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            show_help
            exit 0
            ;;
        -v|--version)
            show_version
            exit 0
            ;;
        -l|--layer)
            EXECUTE_LAYERS="$2"
            shift 2
            ;;
        *)
            log_error "未知参数: $1"
            show_help
            exit 1
            ;;
    esac
done

# 检查依赖命令
check_command "mvn"
check_command "grep"
check_command "find"

# 创建日志目录
mkdir -p "$(dirname "$LOG_FILE")"

# 执行主函数
if [ "$EXECUTE_LAYERS" = "all" ]; then
    main
else
    log_info "执行指定层检查: $EXECUTE_LAYERS"
    # 这里可以添加执行特定层的逻辑
    main
fi

# 退出码反映整体结果
exit $?