#!/bin/bash

# =====================================================
# IOE-DREAM 数据库初始化脚本 (Linux/Unix)
# 版本: 1.0.0
# 说明: 自动化数据库初始化脚本
# =====================================================

set -e

# 配置变量
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DB_INIT_DIR="$(dirname "$SCRIPT_DIR")"
MYSQL_HOST=${MYSQL_HOST:-"localhost"}
MYSQL_PORT=${MYSQL_PORT:-3306}
MYSQL_USER=${MYSQL_USER:-"root"}
MYSQL_PASSWORD=${MYSQL_PASSWORD:-"123456"}
MYSQL_CHARSET=${MYSQL_CHARSET:-"utf8mb4"}

# 日志配置
LOG_FILE="$DB_INIT_DIR/init.log"
BACKUP_DIR="$DB_INIT_DIR/backup"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $1" | tee -a "$LOG_FILE"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $1" | tee -a "$LOG_FILE"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $1" | tee -a "$LOG_FILE"
}

log_step() {
    echo -e "${BLUE}[STEP]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $1" | tee -a "$LOG_FILE"
}

# 检查MySQL连接
check_mysql_connection() {
    log_info "检查MySQL连接..."

    if ! mysql -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" -e "SELECT 1;" > /dev/null 2>&1; then
        log_error "无法连接到MySQL服务器"
        log_error "请检查连接配置: Host=$MYSQL_HOST, Port=$MYSQL_PORT, User=$MYSQL_USER"
        exit 1
    fi

    log_info "MySQL连接成功"
}

# 创建备份目录
create_backup() {
    log_step "创建数据库备份..."

    mkdir -p "$BACKUP_DIR"

    # 备份现有数据库（如果存在）
    databases=$(mysql -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" -e "SHOW DATABASES LIKE 'ioedream_%';" | grep -v "Database")

    for db in $databases; do
        if [[ -n "$db" ]]; then
            backup_file="$BACKUP_DIR/${db}_$(date +%Y%m%d_%H%M%S).sql"
            log_info "备份数据库: $db -> $backup_file"
            mysqldump -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" \
                --single-transaction --routines --triggers --events \
                --default-character-set="$MYSQL_CHARSET" "$db" > "$backup_file"

            if [[ $? -eq 0 ]]; then
                log_info "数据库 $db 备份成功"
            else
                log_warn "数据库 $db 备份失败，继续执行初始化"
            fi
        fi
    done
}

# 执行SQL脚本
execute_sql_script() {
    local script_file="$1"
    local description="$2"

    if [[ ! -f "$script_file" ]]; then
        log_error "SQL脚本文件不存在: $script_file"
        return 1
    fi

    log_step "执行: $description"
    log_info "脚本文件: $script_file"

    # 记录执行开始时间
    start_time=$(date +%s)

    # 执行SQL脚本
    if mysql -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" \
        --default-character-set="$MYSQL_CHARSET" < "$script_file" 2>&1 | tee -a "$LOG_FILE"; then

        end_time=$(date +%s)
        duration=$((end_time - start_time))
        log_info "$description 执行成功 (耗时: ${duration}s)"
        return 0
    else
        log_error "$description 执行失败"
        return 1
    fi
}

# 验证初始化结果
verify_initialization() {
    log_step "验证初始化结果..."

    local success_count=0
    local total_count=7
    local databases=("ioedream_database" "ioedream_common_db" "ioedream_access_db"
                    "ioedream_attendance_db" "ioedream_consume_db" "ioedream_visitor_db"
                    "ioedream_video_db" "ioedream_device_db")

    for db in "${databases[@]}"; do
        if mysql -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" \
            -e "USE $db; SELECT COUNT(*) as table_count FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='$db';" > /dev/null 2>&1; then

            table_count=$(mysql -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" \
                -e "USE $db; SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='$db';" | tail -n 1)

            log_info "✓ 数据库 $db 初始化成功 ($table_count 个表)"
            ((success_count++))
        else
            log_error "✗ 数据库 $db 初始化失败或无法访问"
        fi
    done

    log_info "初始化验证完成: $success_count/$total_count 个数据库初始化成功"

    if [[ $success_count -eq $total_count ]]; then
        log_info "🎉 所有数据库初始化成功！"
        return 0
    else
        log_error "部分数据库初始化失败，请检查日志"
        return 1
    fi
}

# 显示初始化摘要
show_summary() {
    log_step "生成初始化摘要..."

    echo "================================================"
    echo "IOE-DREAM 数据库初始化摘要"
    echo "================================================"
    echo "初始化时间: $(date '+%Y-%m-%d %H:%M:%S')"
    echo "MySQL服务器: $MYSQL_HOST:$MYSQL_PORT"
    echo "字符集: $MYSQL_CHARSET"
    echo ""
    echo "数据库清单:"

    databases=("ioedream_database" "ioedream_common_db" "ioedream_access_db"
              "ioedream_attendance_db" "ioedream_consume_db" "ioedream_visitor_db"
              "ioedream_video_db" "ioedream_device_db")

    for db in "${databases[@]}"; do
        if mysql -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" \
            -e "USE $db; SELECT COUNT(*) as table_count FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='$db';" > /dev/null 2>&1; then

            table_count=$(mysql -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" \
                -e "USE $db; SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='$db';" | tail -n 1)

            if [[ "$db" == "ioedream_common_db" ]]; then
                user_count=$(mysql -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" \
                    -e "USE $db; SELECT COUNT(*) FROM t_user;" 2>/dev/null | tail -n 1 || echo "0")
                echo "  ✓ $db: $table_count 个表, $user_count 个用户"
            else
                echo "  ✓ $db: $table_count 个表"
            fi
        else
            echo "  ✗ $db: 初始化失败"
        fi
    done

    echo ""
    echo "日志文件: $LOG_FILE"
    echo "备份目录: $BACKUP_DIR"
    echo "================================================"
}

# 主函数
main() {
    log_info "开始 IOE-DREAM 数据库初始化..."
    log_info "初始化目录: $DB_INIT_DIR"

    # 检查环境
    check_mysql_connection

    # 创建备份
    create_backup

    # 记录初始化开始
    echo "$(date '+%Y-%m-%d %H:%M:%S') - 开始数据库初始化" > "$LOG_FILE"

    # 执行初始化脚本
    local scripts=(
        "$DB_INIT_DIR/sql/01-create-databases.sql:创建数据库"
        "$DB_INIT_DIR/sql/02-common-schema.sql:创建公共表结构"
        "$DB_INIT_DIR/sql/03-business-schema.sql:创建业务表结构"
        "$DB_INIT_DIR/sql/99-flyway-schema.sql:创建Flyway表"
        "$DB_INIT_DIR/data/common-data.sql:初始化公共数据"
        "$DB_INIT_DIR/data/business-data.sql:初始化业务数据"
    )

    local failed_scripts=()

    for script_info in "${scripts[@]}"; do
        IFS=':' read -r script_file description <<< "$script_info"

        if ! execute_sql_script "$script_file" "$description"; then
            failed_scripts+=("$description")
        fi

        # 在脚本之间添加短暂延迟
        sleep 1
    done

    # 验证初始化结果
    if verify_initialization; then
        log_info "数据库初始化成功完成！"
        show_summary
        exit 0
    else
        log_error "数据库初始化失败！"

        if [[ ${#failed_scripts[@]} -gt 0 ]]; then
            log_error "失败的脚本:"
            for failed_script in "${failed_scripts[@]}"; do
                log_error "  - $failed_script"
            done
        fi

        exit 1
    fi
}

# 显示帮助信息
show_help() {
    echo "IOE-DREAM 数据库初始化脚本"
    echo ""
    echo "用法: $0 [选项]"
    echo ""
    echo "环境变量:"
    echo "  MYSQL_HOST     MySQL服务器地址 (默认: localhost)"
    echo "  MYSQL_PORT     MySQL端口 (默认: 3306)"
    echo "  MYSQL_USER     MySQL用户名 (默认: root)"
    echo "  MYSQL_PASSWORD MySQL密码 (默认: 123456)"
    echo "  MYSQL_CHARSET  字符集 (默认: utf8mb4)"
    echo ""
    echo "示例:"
    echo "  # 使用默认配置"
    echo "  $0"
    echo ""
    echo "  # 自定义配置"
    echo "  MYSQL_HOST=192.168.1.100 MYSQL_PASSWORD=mypass $0"
}

# 参数处理
case "${1:-}" in
    -h|--help)
        show_help
        exit 0
        ;;
    "")
        main
        ;;
    *)
        echo "未知参数: $1"
        echo "使用 -h 或 --help 查看帮助信息"
        exit 1
        ;;
esac