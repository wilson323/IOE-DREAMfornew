#!/bin/bash
# ============================================================
# 消费模块数据库迁移执行脚本
# ============================================================
# 功能：自动执行数据库迁移并验证结果
# 执行方式: bash execute-migration.sh
# ============================================================

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 配置变量
MYSQL_HOST="${MYSQL_HOST:-127.0.0.1}"
MYSQL_PORT="${MYSQL_PORT:-3306}"
MYSQL_USER="${MYSQL_USER:-root}"
MYSQL_DATABASE="${MYSQL_DATABASE:-ioedream}"

# ============================================================
# 步骤1：检查MySQL连接
# ============================================================
check_mysql_connection() {
    log_info "步骤1：检查MySQL连接..."

    if ! command -v mysql &> /dev/null; then
        log_error "MySQL客户端未安装！"
        exit 1
    fi

    if ! mysql -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -p -e "SELECT VERSION();" &> /dev/null; then
        log_error "无法连接到MySQL服务器！请检查连接参数。"
        exit 1
    fi

    MYSQL_VERSION=$(mysql -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -p -e "SELECT VERSION();" 2>/dev/null | tail -1)
    log_info "MySQL版本: $MYSQL_VERSION"

    # 检查版本是否≥8.0
    VERSION_MAJOR=$(echo $MYSQL_VERSION | cut -d'.' -f1)
    if [ "$VERSION_MAJOR" -lt 8 ]; then
        log_error "MySQL版本必须≥8.0，当前版本: $MYSQL_VERSION"
        exit 1
    fi

    log_info "✅ MySQL连接检查通过"
}

# ============================================================
# 步骤2：检查数据库是否存在
# ============================================================
check_database_exists() {
    log_info "步骤2：检查数据库是否存在..."

    DB_EXISTS=$(mysql -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -p -e "SHOW DATABASES LIKE '$MYSQL_DATABASE';" 2>/dev/null | tail -1)

    if [ -z "$DB_EXISTS" ]; then
        log_error "数据库 $MYSQL_DATABASE 不存在！"
        log_info "请先创建数据库: CREATE DATABASE $MYSQL_DATABASE CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
        exit 1
    fi

    log_info "✅ 数据库 $MYSQL_DATABASE 存在"
}

# ============================================================
# 步骤3：检查旧表是否存在
# ============================================================
check_old_tables() {
    log_info "步骤3：检查旧表是否存在..."

    OLD_TABLE_COUNT=$(mysql -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -p -D"$MYSQL_DATABASE" -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '$MYSQL_DATABASE' AND table_name LIKE 't_consume%';" 2>/dev/null | tail -1)

    if [ "$OLD_TABLE_COUNT" -eq 0 ]; then
        log_warn "未找到旧表（t_consume_*），跳过数据迁移验证"
    else
        log_info "找到 $OLD_TABLE_COUNT 个旧表"
    fi
}

# ============================================================
# 步骤4：提示用户备份数据
# ============================================================
prompt_backup() {
    log_warn "⚠️  生产环境必须先备份数据！"
    echo ""
    echo "备份命令示例："
    echo "  mysqldump -h$MYSQL_HOST -u$MYSQL_USER -p $MYSQL_DATABASE > backup_\$(date +%Y%m%d_%H%M%S).sql"
    echo ""
    read -p "是否已完成数据备份？(y/n): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        log_error "请先完成数据备份后再执行迁移！"
        exit 1
    fi
}

# ============================================================
# 步骤5：启动消费服务执行Flyway迁移
# ============================================================
start_consume_service() {
    log_info "步骤5：启动消费服务执行Flyway迁移..."

    cd /d/IOE-DREAM/microservices/ioedream-consume-service

    # 检查pom.xml是否存在
    if [ ! -f "pom.xml" ]; then
        log_error "未找到pom.xml文件！"
        exit 1
    fi

    log_info "启动消费服务（Flyway将自动执行迁移）..."

    # 后台启动服务
    nohup mvn spring-boot:run -Dspring-boot.run.profiles=docker > /tmp/consume-service.log 2>&1 &
    SERVICE_PID=$!

    log_info "服务已启动，PID: $SERVICE_PID"
    log_info "日志文件: /tmp/consume-service.log"

    # 等待Flyway迁移完成（最多等待60秒）
    log_info "等待Flyway迁移完成..."
    sleep 10

    for i in {1..12}; do
        if grep -q "Successfully applied.*migrations" /tmp/consume-service.log; then
            log_info "✅ Flyway迁移成功！"
            break
        fi

        if grep -q "Migration.*failed" /tmp/consume-service.log; then
            log_error "❌ Flyway迁移失败！"
            log_info "请查看日志: /tmp/consume-service.log"
            kill $SERVICE_PID 2>/dev/null
            exit 1
        fi

        sleep 5
        log_info "等待中... ($i/12)"
    done

    log_info "Flyway迁移执行完成"
}

# ============================================================
# 步骤6：验证新表创建
# ============================================================
verify_new_tables() {
    log_info "步骤6：验证新表创建..."

    NEW_TABLE_COUNT=$(mysql -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -p -D"$MYSQL_DATABASE" -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '$MYSQL_DATABASE' AND table_name LIKE 'POSID%';" 2>/dev/null | tail -1)

    if [ "$NEW_TABLE_COUNT" -lt 11 ]; then
        log_error "❌ 新表创建失败！期望11个POSID表，实际创建: $NEW_TABLE_COUNT"
        mysql -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -p -D"$MYSQL_DATABASE" -e "SHOW TABLES LIKE 'POSID%';" 2>/dev/null
        exit 1
    fi

    log_info "✅ 成功创建 $NEW_TABLE_COUNT 个POSID表"

    # 显示所有新表
    log_info "新表列表："
    mysql -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -p -D"$MYSQL_DATABASE" -e "SHOW TABLES LIKE 'POSID%';" 2>/dev/null
}

# ============================================================
# 步骤7：验证数据迁移
# ============================================================
verify_data_migration() {
    log_info "步骤7：验证数据迁移..."

    # 检查是否有旧表数据
    OLD_TABLE_COUNT=$(mysql -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -p -D"$MYSQL_DATABASE" -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '$MYSQL_DATABASE' AND table_name LIKE 't_consume%';" 2>/dev/null | tail -1)

    if [ "$OLD_TABLE_COUNT" -eq 0 ]; then
        log_info "无旧表数据，跳过数据迁移验证"
        return
    fi

    log_info "旧表与新表数据量对比："
    mysql -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -p -D"$MYSQL_DATABASE" <<EOF 2>/dev/null
SELECT
    '旧表数据量' AS label,
    (SELECT COUNT(*) FROM t_consume_account WHERE deleted_flag = 0) AS account_count,
    (SELECT COUNT(*) FROM t_consume_record WHERE deleted_flag = 0) AS record_count,
    (SELECT COUNT(*) FROM t_consume_account_transaction WHERE deleted_flag = 0) AS transaction_count
UNION ALL
SELECT
    '新表数据量' AS label,
    (SELECT COUNT(*) FROM POSID_ACCOUNT WHERE deleted_flag = 0) AS account_count,
    (SELECT COUNT(*) FROM POSID_CONSUME_RECORD WHERE deleted_flag = 0) AS record_count,
    (SELECT COUNT(*) FROM POSID_TRANSACTION WHERE deleted_flag = 0) AS transaction_count;
EOF

    log_info "✅ 数据迁移验证完成"
}

# ============================================================
# 步骤8：验证Flyway迁移历史
# ============================================================
verify_flyway_history() {
    log_info "步骤8：验证Flyway迁移历史..."

    mysql -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -p -D"$MYSQL_DATABASE" -e "SELECT installed_rank, version, script, success, execution_time FROM flyway_schema_history WHERE script LIKE '%POSID%' ORDER BY installed_rank DESC;" 2>/dev/null

    SUCCESS_COUNT=$(mysql -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -p -D"$MYSQL_DATABASE" -e "SELECT COUNT(*) FROM flyway_schema_history WHERE script LIKE '%POSID%' AND success = 1;" 2>/dev/null | tail -1)

    if [ "$SUCCESS_COUNT" -lt 2 ]; then
        log_error "❌ Flyway迁移历史验证失败！"
        exit 1
    fi

    log_info "✅ Flyway迁移历史验证通过（$SUCCESS_COUNT 个脚本执行成功）"
}

# ============================================================
# 主流程
# ============================================================
main() {
    log_info "=================================="
    log_info "消费模块数据库迁移执行脚本"
    log_info "=================================="
    log_info "MySQL Host: $MYSQL_HOST"
    log_info "MySQL Port: $MYSQL_PORT"
    log_info "MySQL User: $MYSQL_USER"
    log_info "Database: $MYSQL_DATABASE"
    log_info "=================================="
    echo ""

    # 执行检查
    check_mysql_connection
    check_database_exists
    check_old_tables

    # 提示备份
    prompt_backup

    # 执行迁移
    start_consume_service

    # 验证结果
    verify_new_tables
    verify_data_migration
    verify_flyway_history

    log_info "=================================="
    log_info "✅ 数据库迁移执行完成！"
    log_info "=================================="
    log_info "下一步："
    log_info "1. 启动双写验证服务（1-2周）"
    log_info "2. 定期执行验证SQL: bash /d/IOE-DREAM/scripts/validate-dual-write.sql"
    log_info "3. 查看迁移执行清单: cat MIGRATION_EXECUTION_CHECKLIST.md"
    log_info "=================================="

    # 停止服务
    log_info "停止消费服务..."
    kill $SERVICE_PID 2>/dev/null
}

# 执行主流程
main
