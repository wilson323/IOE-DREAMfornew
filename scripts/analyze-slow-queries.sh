#!/bin/bash
# ================================================================
# IOE-DREAM 慢查询分析脚本
# ================================================================
# 功能: 分析MySQL慢查询日志,生成优化建议
# 使用方法: ./scripts/analyze-slow-queries.sh
# ================================================================

set -e

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

# 打印标题
print_header() {
    echo ""
    echo "============================================================"
    echo "  $1"
    echo "============================================================"
    echo ""
}

# 检查MySQL连接
check_mysql_connection() {
    print_header "检查MySQL连接"

    if ! command -v mysql &> /dev/null; then
        log_error "MySQL客户端未安装"
        exit 1
    fi

    # 尝试连接MySQL(需要根据实际配置修改)
    MYSQL_HOST=${MYSQL_HOST:-localhost}
    MYSQL_PORT=${MYSQL_PORT:-3306}
    MYSQL_USER=${MYSQL_USER:-root}
    MYSQL_PASS=${MYSQL_PASS:-}

    log_info "MySQL连接信息: ${MYSQL_USER}@${MYSQL_HOST}:${MYSQL_PORT}"

    if [ -z "$MYSQL_PASS" ]; then
        mysql -h$MYSQL_HOST -P$MYSQL_PORT -u$MYSQL_USER -e "SELECT 1" &> /dev/null
    else
        mysql -h$MYSQL_HOST -P$MYSQL_PORT -u$MYSQL_USER -p$MYSQL_PASS -e "SELECT 1" &> /dev/null
    fi

    if [ $? -eq 0 ]; then
        log_success "MySQL连接成功"
    else
        log_error "MySQL连接失败,请检查连接信息"
        exit 1
    fi
}

# 启用慢查询日志
enable_slow_query_log() {
    print_header "启用慢查询日志"

    log_info "设置慢查询阈值(0.5秒)..."
    mysql -h$MYSQL_HOST -P$MYSQL_PORT -u$MYSQL_USER ${MYSQL_PASS:+-p$MYSQL_PASS} -e "
        SET GLOBAL slow_query_log = 'ON';
        SET GLOBAL long_query_time = 0.5;
        SET GLOBAL log_queries_not_using_indexes = 'ON';
    "

    log_success "慢查询日志已启用"
    log_info "慢查询日志文件位置:"
    mysql -h$MYSQL_HOST -P$MYSQL_PORT -u$MYSQL_USER ${MYSQL_PASS:+-p$MYSQL_PASS} -e "SHOW VARIABLES LIKE 'slow_query_log_file';"
}

# 分析慢查询
analyze_slow_queries() {
    print_header "分析慢查询日志"

    SLOW_QUERY_FILE=$(mysql -h$MYSQL_HOST -P$MYSQL_PORT -u$MYSQL_USER ${MYSQL_PASS:+-p$MYSQL_PASS} -sN -e "SHOW VARIABLES LIKE 'slow_query_log_file'" | awk '{print $2}')

    if [ ! -f "$SLOW_QUERY_FILE" ]; then
        log_warning "慢查询日志文件不存在: $SLOW_QUERY_FILE"
        log_info "请等待一段时间后再次运行此脚本"
        return
    fi

    log_info "慢查询日志文件: $SLOW_QUERY_FILE"
    echo ""

    # 统计慢查询数量
    log_info "慢查询统计:"
    mysql -h$MYSQL_HOST -P$MYSQL_PORT -u$MYSQL_USER ${MYSQL_PASS:+-p$MYSQL_PASS} -e "
        SELECT
            COUNT(*) as '总慢查询数',
            ROUND(AVG(query_time), 2) as '平均耗时(秒)',
            ROUND(MAX(query_time), 2) as '最大耗时(秒)'
        FROM mysql.slow_log
        WHERE start_time > DATE_SUB(NOW(), INTERVAL 1 DAY);
    "
    echo ""

    # Top 20 最慢查询
    log_info "Top 20 最慢查询:"
    mysql -h$MYSQL_HOST -P$MYSQL_PORT -u$MYSQL_USER ${MYSQL_PASS:+-p$MYSQL_PASS} -e "
        SELECT
            ROUND(query_time, 2) as '耗时(秒)',
            ROUND(lock_time, 2) as '锁等待(秒)',
            rows_sent as '返回行数',
            rows_examined as '扫描行数',
            SUBSTRING(sql_text, 1, 100) as 'SQL语句'
        FROM mysql.slow_log
        WHERE start_time > DATE_SUB(NOW(), INTERVAL 1 DAY)
        ORDER BY query_time DESC
        LIMIT 20;
    "
    echo ""

    # 按SQL模式统计
    log_info "按SQL模式统计(重复最多的查询):"
    mysql -h$MYSQL_HOST -P$MYSQL_PORT -u$MYSQL_USER ${MYSQL_PASS:+-p$MYSQL_PASS} -e "
        SELECT
            COUNT(*) as '执行次数',
            ROUND(AVG(query_time), 2) as '平均耗时(秒)',
            SUBSTRING(sql_text, 1, 80) as 'SQL语句'
        FROM mysql.slow_log
        WHERE start_time > DATE_SUB(NOW(), INTERVAL 1 DAY)
        GROUP BY sql_text
        ORDER BY COUNT(*) DESC
        LIMIT 10;
    "
}

# 生成优化建议
generate_optimization_suggestions() {
    print_header "生成优化建议"

    log_info "分析常见的慢查询模式..."

    # 检查全表扫描查询
    log_info "检查全表扫描查询:"
    mysql -h$MYSQL_HOST -P$MYSQL_PORT -u$MYSQL_USER ${MYSQL_PASS:+-p$MYSQL_PASS} -e "
        SELECT
            SUBSTRING(sql_text, 1, 100) as 'SQL语句',
            rows_examined as '扫描行数',
            ROUND(query_time, 2) as '耗时(秒)'
        FROM mysql.slow_log
        WHERE start_time > DATE_SUB(NOW(), INTERVAL 1 DAY)
          AND rows_examined > 10000
        ORDER BY query_time DESC
        LIMIT 10;
    "
    echo ""

    # 检查未使用索引的查询
    log_info "检查未使用索引的查询:"
    mysql -h$MYSQL_HOST -P$MYSQL_PORT -u$MYSQL_USER ${MYSQL_PASS:+-p$MYSQL_PASS} -e "
        SELECT
            SUBSTRING(sql_text, 1, 100) as 'SQL语句',
            ROUND(query_time, 2) as '耗时(秒)'
        FROM mysql.slow_log
        WHERE start_time > DATE_SUB(NOW(), INTERVAL 1 DAY)
          AND sql_text NOT LIKE '%USE INDEX%'
        ORDER BY query_time DESC
        LIMIT 10;
    "
    echo ""

    # 优化建议
    log_info "优化建议:"
    echo ""
    echo "1. 为常用查询条件添加索引"
    echo "2. 避免SELECT *,只查询需要的字段"
    echo "3. 优化子查询为JOIN"
    echo "4. 使用EXPLAIN分析查询计划"
    echo "5. 考虑使用查询缓存"
    echo "6. 优化WHERE条件,避免函数计算"
    echo "7. 使用LIMIT限制返回行数"
    echo ""
}

# 导出分析报告
export_report() {
    print_header "导出分析报告"

    REPORT_FILE="slow-query-report-$(date +%Y%m%d-%H%M%S).txt"

    log_info "生成分析报告: $REPORT_FILE"

    {
        echo "IOE-DREAM 慢查询分析报告"
        echo "生成时间: $(date '+%Y-%m-%d %H:%M:%S')"
        echo ""
        echo "============================================================"
        echo ""

        echo "1. 慢查询统计"
        echo "============================================================"
        mysql -h$MYSQL_HOST -P$MYSQL_PORT -u$MYSQL_USER ${MYSQL_PASS:+-p$MYSQL_PASS} -e "
            SELECT
                COUNT(*) as '总慢查询数',
                ROUND(AVG(query_time), 2) as '平均耗时(秒)',
                ROUND(MAX(query_time), 2) as '最大耗时(秒)'
            FROM mysql.slow_log
            WHERE start_time > DATE_SUB(NOW(), INTERVAL 1 DAY);
        "
        echo ""

        echo "2. Top 20 最慢查询"
        echo "============================================================"
        mysql -h$MYSQL_HOST -P$MYSQL_PORT -u$MYSQL_USER ${MYSQL_PASS:+-p$MYSQL_PASS} -e "
            SELECT
                ROUND(query_time, 2) as '耗时(秒)',
                SUBSTRING(sql_text, 1, 150) as 'SQL语句'
            FROM mysql.slow_log
            WHERE start_time > DATE_SUB(NOW(), INTERVAL 1 DAY)
            ORDER BY query_time DESC
            LIMIT 20;
        "
        echo ""

        echo "3. 优化建议"
        echo "============================================================"
        echo "请参考SQL优化实施指南: openspec/changes/performance-optimization-phase1/SQL_OPTIMIZATION_IMPLEMENTATION_GUIDE.md"
        echo ""

    } | tee $REPORT_FILE

    log_success "分析报告已导出: $REPORT_FILE"
}

# 主函数
main() {
    print_header "IOE-DREAM 慢查询分析工具"

    # 检查MySQL连接
    check_mysql_connection

    # 启用慢查询日志
    enable_slow_query_log

    # 分析慢查询
    analyze_slow_queries

    # 生成优化建议
    generate_optimization_suggestions

    # 导出报告
    export_report

    print_header "分析完成"
    log_success "慢查询分析完成!"
    log_info "下一步: 参考分析结果优化慢查询"
    echo ""

    log_info "常用优化命令:"
    echo "1. 查看表索引: SHOW INDEX FROM table_name;"
    echo "2. 分析查询计划: EXPLAIN SELECT ...;"
    echo "3. 创建索引: CREATE INDEX idx_name ON table_name(column);"
    echo ""
}

# 执行主函数
main "$@"
