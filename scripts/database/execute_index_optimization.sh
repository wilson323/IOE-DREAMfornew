#!/bin/bash
# ============================================
# IOE-DREAM 数据库索引优化执行脚本
# 版本: 1.0.0
# 日期: 2025-01-30
# 说明: 执行所有模块的索引优化SQL
# ============================================

# 配置变量
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-3306}"
DB_USER="${DB_USER:-root}"
DB_PASSWORD="${DB_PASSWORD:-}"
DB_NAME="${DB_NAME:-ioedream}"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 打印信息
info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查MySQL连接
check_mysql_connection() {
    info "检查MySQL连接..."
    mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" -e "SELECT 1;" "$DB_NAME" > /dev/null 2>&1
    if [ $? -ne 0 ]; then
        error "MySQL连接失败，请检查配置"
        exit 1
    fi
    info "MySQL连接成功"
}

# 执行SQL文件
execute_sql_file() {
    local sql_file=$1
    local module_name=$2
    
    if [ ! -f "$sql_file" ]; then
        warn "SQL文件不存在: $sql_file"
        return 1
    fi
    
    info "执行 $module_name 索引优化SQL: $sql_file"
    mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" < "$sql_file"
    
    if [ $? -eq 0 ]; then
        info "$module_name 索引优化执行成功"
        return 0
    else
        error "$module_name 索引优化执行失败"
        return 1
    fi
}

# 主函数
main() {
    info "开始执行数据库索引优化..."
    
    # 检查MySQL连接
    check_mysql_connection
    
    # 获取脚本所在目录
    SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
    PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
    
    # 执行各模块索引优化
    execute_sql_file "$PROJECT_ROOT/microservices/ioedream-access-service/src/main/resources/sql/access_index_optimization.sql" "门禁模块"
    execute_sql_file "$PROJECT_ROOT/microservices/ioedream-attendance-service/src/main/resources/sql/attendance_index_optimization.sql" "考勤模块"
    execute_sql_file "$PROJECT_ROOT/microservices/ioedream-visitor-service/src/main/resources/sql/visitor_index_optimization.sql" "访客模块"
    execute_sql_file "$PROJECT_ROOT/microservices/ioedream-video-service/src/main/resources/sql/video_index_optimization.sql" "视频模块"
    execute_sql_file "$PROJECT_ROOT/microservices/ioedream-consume-service/src/main/resources/sql/consume_index_optimization.sql" "消费模块"
    
    info "数据库索引优化执行完成"
    
    # 验证索引
    info "验证索引创建情况..."
    mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" -e "
        SELECT 
            TABLE_NAME,
            INDEX_NAME,
            GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) AS COLUMNS
        FROM 
            INFORMATION_SCHEMA.STATISTICS
        WHERE 
            TABLE_SCHEMA = '$DB_NAME'
            AND INDEX_NAME LIKE 'idx_%'
        GROUP BY 
            TABLE_NAME, INDEX_NAME
        ORDER BY 
            TABLE_NAME, INDEX_NAME;
    "
}

# 执行主函数
main "$@"

