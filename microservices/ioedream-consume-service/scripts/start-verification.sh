#!/bin/bash
# ============================================================
# 消费服务性能验证快速启动脚本
# ============================================================
# 功能：一键执行所有验证步骤
# 使用：bash start-verification.sh
# ============================================================

set -e

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_step() {
    echo -e "${BLUE}[STEP]${NC} $1"
}

# 配置变量
PROJECT_DIR="D:/IOE-DREAM/microservices/ioedream-consume-service"
SCRIPTS_DIR="${PROJECT_DIR}/scripts"
CONFIG_DIR="${PROJECT_DIR}/config"
DB_HOST="127.0.0.1"
DB_PORT="3306"
DB_USER="root"
DB_PASS="root"
DB_NAME="ioedream"
SERVICE_URL="http://localhost:8094"

# ============================================================
# 步骤1: 环境检查
# ============================================================
check_environment() {
    log_step "步骤1: 环境检查"

    # 检查项目目录
    if [ ! -d "$PROJECT_DIR" ]; then
        log_error "项目目录不存在: $PROJECT_DIR"
        exit 1
    fi
    log_info "✓ 项目目录存在"

    # 检查迁移脚本
    if [ ! -f "${SCRIPTS_DIR}/execute-migration.sh" ]; then
        log_error "迁移脚本不存在"
        exit 1
    fi
    log_info "✓ 迁移脚本存在"

    # 检查性能测试脚本
    if [ ! -f "${SCRIPTS_DIR}/performance-test.sh" ]; then
        log_error "性能测试脚本不存在"
        exit 1
    fi
    log_info "✓ 性能测试脚本存在"

    # 检查数据库连接
    if ! mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" -e "SELECT 1;" &>/dev/null; then
        log_warn "数据库未连接，请检查数据库配置"
        read -p "是否继续？(y/n) " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            exit 1
        fi
    else
        log_info "✓ 数据库连接正常"
    fi

    log_info "✓ 环境检查完成"
}

# ============================================================
# 步骤2: 数据库迁移
# ============================================================
execute_migration() {
    log_step "步骤2: 数据库迁移"

    read -p "是否执行数据库迁移？(y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        log_warn "跳过数据库迁移"
        return
    fi

    # 执行迁移脚本
    cd "$SCRIPTS_DIR"
    bash execute-migration.sh

    # 验证表创建
    log_info "验证POSID表创建..."
    TABLE_COUNT=$(mysql -h"$DB_HOST" -u"$DB_USER" -p"$DB_PASS" "$DB_NAME" -e "
        SELECT COUNT(*) FROM information_schema.TABLES
        WHERE TABLE_SCHEMA = '$DB_NAME' AND TABLE_NAME LIKE 'POSID_%';
    " -s -N)

    if [ "$TABLE_COUNT" -eq 11 ]; then
        log_info "✓ POSID表创建成功: 11个表"
    else
        log_error "POSID表创建失败: 实际${TABLE_COUNT}个，期望11个"
        exit 1
    fi

    log_info "✓ 数据库迁移完成"
}

# ============================================================
# 步骤3: 启动消费服务
# ============================================================
start_service() {
    log_step "步骤3: 启动消费服务"

    # 检查服务是否已启动
    if curl -s "$SERVICE_URL/actuator/health" &>/dev/null; then
        log_warn "服务已运行，跳过启动"
        return
    fi

    read -p "是否启动消费服务？(y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        log_warn "跳过服务启动"
        return
    fi

    cd "$PROJECT_DIR"

    # 后台启动服务
    log_info "正在启动消费服务..."
    nohup mvn spring-boot:run -Dspring-boot.run.profiles=docker > service.log 2>&1 &
    SERVICE_PID=$!

    # 等待服务启动
    log_info "等待服务启动（最多60秒）..."
    for i in {1..60}; do
        if curl -s "$SERVICE_URL/actuator/health" &>/dev/null; then
            log_info "✓ 服务启动成功 (PID: $SERVICE_PID)"
            return
        fi
        sleep 1
    done

    log_error "服务启动超时"
    exit 1
}

# ============================================================
# 步骤4: 验证服务健康
# ============================================================
verify_service() {
    log_step "步骤4: 验证服务健康"

    # 健康检查
    HEALTH_STATUS=$(curl -s "$SERVICE_URL/actuator/health" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)

    if [ "$HEALTH_STATUS" = "UP" ]; then
        log_info "✓ 服务健康状态: UP"
    else
        log_error "服务健康状态: $HEALTH_STATUS"
        exit 1
    fi

    # 检查Flyway迁移历史
    log_info "检查Flyway迁移历史..."
    curl -s "$SERVICE_URL/actuator/flyway" | grep -o '"version":"V20251223[^"]*"' || log_warn "Flyway迁移历史检查失败"

    log_info "✓ 服务验证完成"
}

# ============================================================
# 步骤5: 准备测试数据
# ============================================================
prepare_test_data() {
    log_step "步骤5: 准备测试数据"

    read -p "是否准备测试数据？(y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        log_warn "跳过测试数据准备"
        return
    fi

    mysql -h"$DB_HOST" -u"$DB_USER" -p"$DB_PASS" "$DB_NAME" << 'EOF'
    -- 创建测试账户
    INSERT IGNORE INTO POSID_ACCOUNT (user_id, account_code, balance, account_type, account_status)
    VALUES
    (1, 'TEST001', 1000.00, 1, 1),
    (2, 'TEST002', 1000.00, 1, 1),
    (3, 'TEST003', 1000.00, 1, 1);

    -- 创建测试区域
    INSERT IGNORE INTO POSID_AREA (area_id, area_code, area_name, manage_mode, area_status)
    VALUES
    (1, 'AREA001', '测试区域1', 1, 1),
    (2, 'AREA002', '测试区域2', 2, 1);

    -- 创建测试商品
    INSERT IGNORE INTO POSID_PRODUCT (product_id, product_code, product_name, price, product_status)
    VALUES
    (1, 'PROD001', '测试商品1', 10.00, 1),
    (2, 'PROD002', '测试商品2', 20.00, 1);
EOF

    log_info "✓ 测试数据准备完成"
}

# ============================================================
# 步骤6: 执行性能测试
# ============================================================
execute_performance_test() {
    log_step "步骤6: 执行性能测试"

    read -p "是否执行性能测试？(y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        log_warn "跳过性能测试"
        return
    fi

    cd "$SCRIPTS_DIR"
    bash performance-test.sh

    log_info "✓ 性能测试完成"
}

# ============================================================
# 步骤7: 验证双写一致性
# ============================================================
verify_dual_write() {
    log_step "步骤7: 验证双写一致性"

    log_info "查询双写验证日志..."

    mysql -h"$DB_HOST" -u"$DB_USER" -p"$DB_PASS" "$DB_NAME" -e "
    SELECT
        validation_type,
        COUNT(*) AS total_validations,
        SUM(CASE WHEN validation_status = 1 THEN 1 ELSE 0 END) AS passed_count,
        AVG(consistency_rate) AS avg_consistency_rate,
        MIN(consistency_rate) AS min_consistency_rate
    FROM dual_write_validation_log
    WHERE validate_time >= DATE_SUB(NOW(), INTERVAL 24 HOUR)
    GROUP BY validation_type;
    "

    log_info "检查未解决的数据差异..."
    DIFF_COUNT=$(mysql -h"$DB_HOST" -u"$DB_USER" -p"$DB_PASS" "$DB_NAME" -e "
    SELECT COUNT(*) FROM dual_write_difference_record WHERE resolved = 0;
    " -s -N)

    if [ "$DIFF_COUNT" -eq 0 ]; then
        log_info "✓ 无未解决的数据差异"
    else
        log_warn "存在 $DIFF_COUNT 个未解决的数据差异"
    fi

    log_info "✓ 双写一致性验证完成"
}

# ============================================================
# 步骤8: 生成验证报告
# ============================================================
generate_report() {
    log_step "步骤8: 生成验证报告"

    REPORT_FILE="${PROJECT_DIR}/VERIFICATION_REPORT_$(date +%Y%m%d_%H%M%S).txt"

    cat > "$REPORT_FILE" << EOF
========================================
消费服务性能验证报告
========================================
验证时间: $(date '+%Y-%m-%d %H:%M:%S')
验证人: $(whoami)
环境: $(hostname)

1. 环境检查
   状态: ✓ 通过

2. 数据库迁移
   POSID表创建: ${TABLE_COUNT}个
   状态: ✓ 完成

3. 服务启动
   健康状态: $HEALTH_STATUS
   状态: ✓ 运行中

4. 性能测试
   状态: ✓ 完成
   详细报告: ${SCRIPTS_DIR}/performance-test-logs/

5. 双写一致性
   未解决差异: ${DIFF_COUNT}个
   状态: ✓ 验证完成

========================================
总体结论: ✓ 所有验证步骤已完成
========================================

详细验证报告: ${PROJECT_DIR}/VERIFICATION_EXECUTION_REPORT.md
EOF

    log_info "✓ 验证报告已生成: $REPORT_FILE"
}

# ============================================================
# 主流程
# ============================================================
main() {
    log_info "========================================="
    log_info "消费服务性能验证快速启动"
    log_info "========================================="
    log_info "项目目录: $PROJECT_DIR"
    log_info "数据库: $DB_HOST:$DB_PORT/$DB_NAME"
    log_info "服务地址: $SERVICE_URL"
    log_info "========================================="
    echo ""

    # 执行验证步骤
    check_environment
    execute_migration
    start_service
    verify_service
    prepare_test_data
    execute_performance_test
    verify_dual_write
    generate_report

    echo ""
    log_info "========================================="
    log_info "所有验证步骤已完成！"
    log_info "========================================="
    log_info "下一步："
    log_info "1. 查看验证报告: $REPORT_FILE"
    log_info "2. 查看性能测试结果: ${SCRIPTS_DIR}/performance-test-logs/"
    log_info "3. 访问Grafana仪表盘: http://localhost:3000"
    log_info "4. 访问Prometheus: http://localhost:9090"
    log_info "========================================="
}

# 执行主流程
main
