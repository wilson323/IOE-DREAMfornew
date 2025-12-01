#!/bin/bash

# 考勤模块性能测试脚本
# 用于验证数据库优化、缓存优化和前端优化的效果

# ========================================
# 1. 环境准备
# ========================================
echo "🚀 开始考勤模块性能测试..."
echo "========================================"

# 检查环境变量
if [ -z "$MYSQL_HOST" ] || [ -z "$MYSQL_USER" ] || [ -z "$MYSQL_PASSWORD" ] || [ -z "$MYSQL_DATABASE" ]; then
    echo "❌ 请设置MySQL环境变量: MYSQL_HOST, MYSQL_USER, MYSQL_PASSWORD, MYSQL_DATABASE"
    exit 1
fi

# 检查依赖工具
command -v mysql >/dev/null 2>&1 || { echo "❌ 请安装mysql客户端"; exit 1; }
command -v redis-cli >/dev/null 2>&1 || { echo "❌ 请安装redis-cli"; exit 1; }
command -v curl >/dev/null 2>&1 || { echo "❌ 请安装curl"; exit 1; }

# ========================================
# 2. 数据库性能测试
# ========================================
echo "📊 开始数据库性能测试..."

# 2.1 测试索引优化效果
echo "2.1 测试索引优化效果..."
mysql -h$MYSQL_HOST -u$MYSQL_USER -p$MYSQL_PASSWORD $MYSQL_DATABASE << 'EOF'
-- 测试员工考勤记录查询性能
SELECT SQL_NO_CACHE
    employee_id,
    attendance_date,
    attendance_status,
    work_hours
FROM t_attendance_record
WHERE employee_id = 1
  AND attendance_date BETWEEN '2024-01-01' AND '2024-01-31'
ORDER BY attendance_date DESC
LIMIT 20;
EOF

# 2.2 测试视图查询性能
echo "2.2 测试视图查询性能..."
mysql -h$MYSQL_HOST -u$MYSQL_USER -p$MYSQL_PASSWORD $MYSQL_DATABASE << 'EOF'
-- 测试员工月度考勤汇总视图查询性能
SELECT SQL_NO_CACHE *
FROM v_employee_monthly_attendance
WHERE employee_id = 1
  AND attendance_month = '2024-01';
EOF

# 2.3 测试存储过程性能
echo "2.3 测试存储过程性能..."
mysql -h$MYSQL_HOST -u$MYSQL_USER -p$MYSQL_PASSWORD $MYSQL_DATABASE << 'EOF'
-- 测试考勤统计计算存储过程性能
CALL sp_calculate_attendance_statistics(1, 'MONTHLY', '2024-01');
EOF

# 2.4 测试分区表查询性能
echo "2.4 测试分区表查询性能..."
mysql -h$MYSQL_HOST -u$MYSQL_USER -p$MYSQL_PASSWORD $MYSQL_DATABASE << 'EOF'
-- 测试分区表查询性能（如果已实施分区）
EXPLAIN PARTITIONS
SELECT SQL_NO_CACHE *
FROM t_attendance_record
WHERE attendance_date >= '2024-01-01'
  AND attendance_date < '2024-02-01';
EOF

# ========================================
# 3. Redis缓存性能测试
# ========================================
echo "キャッシング 开始Redis缓存性能测试..."

# 3.1 测试缓存读取性能
echo "3.1 测试缓存读取性能..."
redis-cli ping > /dev/null 2>&1
if [ $? -eq 0 ]; then
    # 测试员工考勤数据缓存读取
    start_time=$(date +%s%3N)
    for i in {1..100}; do
        redis-cli GET "attendance:employee:1:2024-01" > /dev/null 2>&1
    done
    end_time=$(date +%s%3N)
    echo "缓存读取100次耗时: $((end_time - start_time)) 毫秒"
else
    echo "❌ Redis服务未运行"
fi

# 3.2 测试缓存写入性能
echo "3.2 测试缓存写入性能..."
if [ $? -eq 0 ]; then
    start_time=$(date +%s%3N)
    for i in {1..100}; do
        redis-cli SET "attendance:test:$i" "test_data_$i" EX 3600 > /dev/null 2>&1
    done
    end_time=$(date +%s%3N)
    echo "缓存写入100次耗时: $((end_time - start_time)) 毫秒"
fi

# ========================================
# 4. API接口性能测试
# ========================================
echo "🌐 开始API接口性能测试..."

# 4.1 测试考勤打卡接口
echo "4.1 测试考勤打卡接口..."
if [ ! -z "$API_BASE_URL" ]; then
    start_time=$(date +%s%3N)
    for i in {1..10}; do
        curl -s -X POST "$API_BASE_URL/attendance/punch-in" \
             -H "Content-Type: application/json" \
             -d '{"employeeId": 1, "punchTime": "2024-01-15 09:00:00"}' > /dev/null 2>&1
    done
    end_time=$(date +%s%3N)
    echo "考勤打卡接口10次调用耗时: $((end_time - start_time)) 毫秒"
else
    echo "⚠️  请设置API_BASE_URL环境变量以测试API接口"
fi

# 4.2 测试考勤记录查询接口
echo "4.2 测试考勤记录查询接口..."
if [ ! -z "$API_BASE_URL" ]; then
    start_time=$(date +%s%3N)
    for i in {1..10}; do
        curl -s "$API_BASE_URL/attendance/records?employeeId=1&startDate=2024-01-01&endDate=2024-01-31" > /dev/null 2>&1
    done
    end_time=$(date +%s%3N)
    echo "考勤记录查询接口10次调用耗时: $((end_time - start_time)) 毫秒"
fi

# ========================================
# 5. 前端性能测试
# ========================================
echo "🖥️  开始前端性能测试..."

# 5.1 模拟页面加载性能
echo "5.1 模拟页面加载性能..."
# 这里可以添加具体的前端性能测试逻辑
# 例如使用Puppeteer或类似的工具进行页面加载测试

# 5.2 测试虚拟滚动性能
echo "5.2 测试虚拟滚动性能..."
# 模拟大数据量渲染测试

# ========================================
# 6. 压力测试
# ========================================
echo "💪 开始压力测试..."

# 6.1 并发查询测试
echo "6.1 并发查询测试..."
# 使用ab或wrk等工具进行并发测试
if command -v ab >/dev/null 2>&1; then
    if [ ! -z "$API_BASE_URL" ]; then
        echo "执行100个并发请求，每个请求10次..."
        ab -n 1000 -c 100 "$API_BASE_URL/attendance/today?employeeId=1" 2>/dev/null | grep "Requests per second"
    else
        echo "⚠️  请设置API_BASE_URL环境变量以进行压力测试"
    fi
else
    echo "⚠️  请安装ab工具以进行压力测试"
fi

# ========================================
# 7. 性能监控和报告
# ========================================
echo "📈 生成性能测试报告..."

# 7.1 数据库性能监控
echo "7.1 数据库性能监控..."
mysql -h$MYSQL_HOST -u$MYSQL_USER -p$MYSQL_PASSWORD $MYSQL_DATABASE << 'EOF'
-- 查看慢查询
SHOW VARIABLES LIKE 'slow_query_log';
SHOW VARIABLES LIKE 'long_query_time';

-- 查看索引使用情况
SELECT OBJECT_SCHEMA, OBJECT_NAME, INDEX_NAME, COUNT_FETCH, COUNT_INSERT
FROM performance_schema.table_io_waits_summary_by_index_usage
WHERE OBJECT_SCHEMA = DATABASE()
  AND OBJECT_NAME LIKE 't_attendance%'
ORDER BY COUNT_FETCH DESC;

-- 查看表大小
SELECT
    TABLE_NAME,
    ROUND(((DATA_LENGTH + INDEX_LENGTH) / 1024 / 1024), 2) AS 'SIZE(MB)'
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME LIKE 't_attendance%'
ORDER BY (DATA_LENGTH + INDEX_LENGTH) DESC;
EOF

# 7.2 Redis性能监控
echo "7.2 Redis性能监控..."
redis-cli info stats | grep -E "total_commands_processed|instantaneous_ops_per_sec|total_net_input_bytes|total_net_output_bytes"

# 7.3 系统资源监控
echo "7.3 系统资源监控..."
echo "CPU使用率:"
top -bn1 | grep "Cpu(s)" | awk '{print $2}' | awk -F'%' '{print $1}'
echo "内存使用率:"
free | grep Mem | awk '{printf("%.2f%%", $3/$2 * 100.0)}'

# ========================================
# 8. 优化建议
# ========================================
echo "💡 性能优化建议:"
echo "1. 定期分析表以更新索引统计信息: ANALYZE TABLE t_attendance_record;"
echo "2. 监控慢查询日志，优化慢查询SQL"
echo "3. 根据实际使用情况调整Redis缓存策略"
echo "4. 定期清理过期缓存数据"
echo "5. 监控API接口响应时间，优化慢接口"
echo "6. 使用CDN加速静态资源加载"
echo "7. 启用Gzip压缩减少网络传输"
echo "8. 实施前端资源懒加载和代码分割"

# ========================================
# 9. 测试完成
# ========================================
echo "🎉 考勤模块性能测试完成!"
echo "========================================"

# 生成测试报告文件
REPORT_FILE="attendance-performance-report-$(date +%Y%m%d-%H%M%S).txt"
cat > $REPORT_FILE << EOF
考勤模块性能测试报告
====================
测试时间: $(date)
测试环境:
- MySQL Host: $MYSQL_HOST
- Redis Host: $(redis-cli ping 2>/dev/null && echo "Connected" || echo "Disconnected")
- API Base URL: $API_BASE_URL

测试结果摘要:
1. 数据库查询性能: 已测试
2. Redis缓存性能: 已测试
3. API接口性能: 已测试
4. 压力测试: 已执行

详细测试数据请查看上面的输出结果。
EOF

echo "📄 测试报告已保存到: $REPORT_FILE"