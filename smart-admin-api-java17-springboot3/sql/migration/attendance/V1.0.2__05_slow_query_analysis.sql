-- 考勤模块慢查询分析脚本
-- 任务4.2：优化数据库查询和索引
-- 目标：响应时间提升80%，查询性能优化
-- 创建时间：2025-11-24
-- 功能：分析慢查询，识别性能瓶颈，提供优化建议

-- =====================================================
-- 1. 慢查询基础分析
-- =====================================================

-- 查看当前慢查询配置
SHOW VARIABLES LIKE 'slow_query_log%';
SHOW VARIABLES LIKE 'long_query_time';

-- 查看考勤模块相关慢查询（MySQL 5.7+）
-- 注意：需要开启慢查询日志才能使用
/*
SELECT
    start_time,
    user_host,
    query_time,
    lock_time,
    rows_sent,
    rows_examined,
    sql_text
FROM mysql.slow_log
WHERE sql_text LIKE '%t_attendance%'
    OR sql_text LIKE '%attendance%'
ORDER BY query_time DESC
LIMIT 50;
*/

-- =====================================================
-- 2. 考勤模块查询性能分析
-- =====================================================

-- 分析员工考勤记录查询性能
EXPLAIN FORMAT=JSON
SELECT
    ar.record_id,
    ar.employee_id,
    ar.attendance_date,
    ar.attendance_status,
    ar.punch_in_time,
    ar.punch_out_time
FROM t_attendance_record ar
WHERE ar.employee_id = 1
    AND ar.attendance_date >= '2025-01-01'
    AND ar.attendance_date <= '2025-01-31'
    AND ar.deleted_flag = 0
ORDER BY ar.attendance_date DESC;

-- 分析部门考勤统计查询性能
EXPLAIN FORMAT=JSON
SELECT
    e.department_id,
    COUNT(ar.record_id) as total_records,
    SUM(CASE WHEN ar.attendance_status = 'NORMAL' THEN 1 ELSE 0 END) as normal_count,
    SUM(CASE WHEN ar.attendance_status = 'LATE' THEN 1 ELSE 0 END) as late_count,
    SUM(CASE WHEN ar.attendance_status = 'EARLY_LEAVE' THEN 1 ELSE 0 END) as early_leave_count
FROM t_attendance_record ar
INNER JOIN t_employee e ON ar.employee_id = e.employee_id
WHERE ar.attendance_date >= '2025-01-01'
    AND ar.attendance_date <= '2025-01-31'
    AND ar.deleted_flag = 0
    AND e.deleted_flag = 0
GROUP BY e.department_id;

-- 分析考勤异常查询性能
EXPLAIN FORMAT=JSON
SELECT
    ae.application_id,
    ae.employee_id,
    ae.exception_type,
    ae.attendance_date,
    ae.application_status,
    ae.create_time
FROM t_attendance_exception ae
WHERE ae.exception_type = 'LATE'
    AND ae.application_status = 'PENDING'
    AND ae.deleted_flag = 0
ORDER BY ae.create_time DESC;

-- =====================================================
-- 3. 索引使用情况分析
-- =====================================================

-- 查看考勤相关表的索引使用情况
SELECT
    table_name,
    index_name,
    cardinality,
    sub_part,
    packed,
    nullable,
    index_type
FROM information_schema.statistics
WHERE table_schema = DATABASE()
    AND table_name LIKE 't_attendance_%'
ORDER BY table_name, seq_in_index;

-- 查看索引基数（数据分布均匀度）
SELECT
    table_name,
    index_name,
    cardinality,
    ROUND(cardinality / (
        SELECT table_rows
        FROM information_schema.tables
        WHERE table_schema = DATABASE()
            AND table_name = s.table_name
    ), 2) as selectivity_ratio
FROM information_schema.statistics s
WHERE table_schema = DATABASE()
    AND table_name LIKE 't_attendance_%'
    AND index_name != 'PRIMARY'
ORDER BY table_name, selectivity_ratio DESC;

-- =====================================================
-- 4. 表大小和存储分析
-- =====================================================

-- 考勤相关表大小统计
SELECT
    table_name,
    ROUND(((data_length + index_length) / 1024 / 1024), 2) AS table_size_mb,
    ROUND((data_length / 1024 / 1024), 2) AS data_size_mb,
    ROUND((index_length / 1024 / 1024), 2) AS index_size_mb,
    table_rows,
    ROUND((index_length / (data_length + index_length)) * 100, 2) AS index_ratio_percent
FROM information_schema.tables
WHERE table_schema = DATABASE()
    AND table_name LIKE 't_attendance_%'
ORDER BY (data_length + index_length) DESC;

-- 表碎片率分析
SELECT
    table_name,
    ROUND(data_free / 1024 / 1024, 2) AS free_space_mb,
    ROUND(data_free / (data_length + index_length) * 100, 2) AS fragmentation_percent
FROM information_schema.tables
WHERE table_schema = DATABASE()
    AND table_name LIKE 't_attendance_%'
    AND data_free > 0
ORDER BY fragmentation_percent DESC;

-- =====================================================
-- 5. 查询执行计划分析
-- =====================================================

-- 高频查询执行计划分析
-- 1. 员工当日考勤查询
EXPLAIN FORMAT=JSON
SELECT record_id, employee_id, attendance_date, attendance_status, punch_in_time, punch_out_time
FROM t_attendance_record
WHERE employee_id = 1
    AND attendance_date = CURDATE()
    AND deleted_flag = 0;

-- 2. 考勤统计查询
EXPLAIN FORMAT=JSON
SELECT
    employee_id,
    COUNT(*) as total_days,
    SUM(CASE WHEN attendance_status = 'NORMAL' THEN 1 ELSE 0 END) as normal_days
FROM t_attendance_record
WHERE attendance_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
    AND deleted_flag = 0
GROUP BY employee_id;

-- 3. 排班查询
EXPLAIN FORMAT=JSON
SELECT schedule_id, employee_id, schedule_date, shift_id
FROM t_attendance_schedule
WHERE employee_id = 1
    AND schedule_date >= CURDATE()
    AND schedule_date <= DATE_ADD(CURDATE(), INTERVAL 7 DAY)
    AND deleted_flag = 0
ORDER BY schedule_date;

-- =====================================================
-- 6. 性能优化建议查询
-- =====================================================

-- 查找未使用索引的查询（模拟）
SELECT
    'Missing Index on employee_id+attendance_date' as issue,
    't_attendance_record' as table_name,
    'CREATE INDEX idx_employee_date ON t_attendance_record(employee_id, attendance_date)' as suggestion
UNION ALL
SELECT
    'Missing Index on exception_type+status' as issue,
    't_attendance_exception' as table_name,
    'CREATE INDEX idx_type_status ON t_attendance_exception(exception_type, application_status)' as suggestion
UNION ALL
SELECT
    'Missing Index on schedule_date_range' as issue,
    't_attendance_schedule' as table_name,
    'CREATE INDEX idx_schedule_date_range ON t_attendance_schedule(schedule_date, employee_id)' as suggestion;

-- 查找重复索引
SELECT
    table_name,
    index_name,
    GROUP_CONCAT(column_name ORDER BY seq_in_index) as columns
FROM information_schema.statistics
WHERE table_schema = DATABASE()
    AND table_name LIKE 't_attendance_%'
    AND index_name != 'PRIMARY'
GROUP BY table_name, index_name
HAVING COUNT(*) > 1;

-- =====================================================
-- 7. 数据库配置优化建议
-- =====================================================

-- 查看当前数据库配置
SHOW VARIABLES LIKE 'innodb_buffer_pool_size%';
SHOW VARIABLES LIKE 'innodb_log_file_size%';
SHOW VARIABLES LIKE 'innodb_flush_log_at_trx_commit%';
SHOW VARIABLES LIKE 'query_cache_size%';
SHOW VARIABLES LIKE 'tmp_table_size%';
SHOW VARIABLES LIKE 'max_heap_table_size%';

-- 考勤模块优化建议配置
/*
-- 针对考勤模块的MySQL配置建议
-- 在my.cnf中添加以下配置：

[mysqld]
# 针对考勤模块的优化配置
innodb_buffer_pool_size = 2G          # 设置为可用内存的70-80%
innodb_log_file_size = 256M           # 增大日志文件，提高写入性能
innodb_flush_log_at_trx_commit = 2    # 平衡性能和安全性
query_cache_size = 256M               # 启用查询缓存
query_cache_type = 1                  # 启用查询缓存

# 慢查询日志配置
slow_query_log = 1                    # 启用慢查询日志
slow_query_log_file = /var/log/mysql/slow.log
long_query_time = 2                   # 记录执行时间超过2秒的查询

# 连接数配置
max_connections = 500                 # 最大连接数
max_connect_errors = 10000           # 最大连接错误数

# 临时表配置
tmp_table_size = 256M                # 临时表大小
max_heap_table_size = 256M           # 内存表大小
*/

-- =====================================================
-- 8. 监控和维护查询
-- =====================================================

-- 考勤模块实时查询监控
SELECT
    digest_text,
    count_star,
    avg_timer_wait/1000000000 AS avg_exec_time_sec,
    max_timer_wait/1000000000 AS max_exec_time_sec,
    sum_rows_examined/count_star AS avg_rows_examined
FROM performance_schema.events_statements_summary_by_digest
WHERE digest_text LIKE '%t_attendance%'
    OR digest_text LIKE '%attendance%'
ORDER BY avg_timer_wait DESC
LIMIT 20;

-- 锁等待监控
SELECT
    r.trx_id waiting_trx_id,
    r.trx_mysql_thread_id waiting_thread,
    r.trx_query waiting_query,
    b.trx_id blocking_trx_id,
    b.trx_mysql_thread_id blocking_thread,
    b.trx_query blocking_query
FROM information_schema.innodb_lock_waits w
INNER JOIN information_schema.innodb_trx b ON b.trx_id = w.blocking_trx_id
INNER JOIN information_schema.innodb_trx r ON r.trx_id = w.requesting_trx_id;

-- 表空间使用监控
SELECT
    tablespace_name,
    file_name,
    tablespace_type,
    engine,
    table_name,
    ROUND(file_size/1024/1024, 2) AS file_size_mb
FROM information_schema.files
WHERE tablespace_name LIKE '%attendance%'
ORDER BY file_size DESC;

-- =====================================================
-- 9. 性能测试查询模板
-- =====================================================

-- 基准性能测试查询
-- 注意：这些查询需要在不同数据量下执行以测试性能

-- 测试1：员工考勤记录查询性能
SELECT
    COUNT(*) as total_records,
    AVG(TIMESTAMPDIFF(MICROSECOND, NOW(), NOW())) as fake_execution_time
FROM t_attendance_record
WHERE employee_id IN (1, 2, 3, 4, 5)
    AND attendance_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
    AND deleted_flag = 0;

-- 测试2：考勤统计查询性能
SELECT
    employee_id,
    COUNT(*) as attendance_days,
    SUM(CASE WHEN attendance_status = 'NORMAL' THEN 1 ELSE 0 END) as normal_days
FROM t_attendance_record
WHERE attendance_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
    AND deleted_flag = 0
GROUP BY employee_id
ORDER BY employee_id;

-- 测试3：复杂关联查询性能
SELECT
    e.employee_id,
    e.employee_name,
    d.department_name,
    COUNT(ar.record_id) as total_attendance,
    COUNT(ae.application_id) as exception_count
FROM t_employee e
LEFT JOIN t_department d ON e.department_id = d.department_id
LEFT JOIN t_attendance_record ar ON e.employee_id = ar.employee_id
    AND ar.attendance_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
    AND ar.deleted_flag = 0
LEFT JOIN t_attendance_exception ae ON e.employee_id = ae.employee_id
    AND ae.create_time >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
    AND ae.deleted_flag = 0
WHERE e.deleted_flag = 0
    AND d.deleted_flag = 0
GROUP BY e.employee_id, e.employee_name, d.department_name
ORDER BY e.employee_id;

-- =====================================================
-- 10. 自动化监控脚本生成
-- =====================================================

-- 生成性能监控存储过程
DELIMITER $$
CREATE PROCEDURE IF NOT EXISTS sp_attendance_performance_monitor()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE table_name VARCHAR(255);
    DECLARE record_count INT;
    DECLARE table_size DECIMAL(10,2);

    -- 创建临时结果表
    CREATE TEMPORARY TABLE IF NOT EXISTS temp_performance_report (
        table_name VARCHAR(255),
        record_count INT,
        table_size_mb DECIMAL(10,2),
        check_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

    -- 遍历考勤相关表
    BEGIN
        DECLARE cur CURSOR FOR
            SELECT table_name
            FROM information_schema.tables
            WHERE table_schema = DATABASE()
                AND table_name LIKE 't_attendance_%';
        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

        OPEN cur;
        read_loop: LOOP
            FETCH cur INTO table_name;
            IF done THEN
                LEAVE read_loop;
            END IF;

            SET @sql = CONCAT('INSERT INTO temp_performance_report (table_name, record_count, table_size_mb)
                             SELECT ''', table_name, ''', COUNT(*), ROUND(((data_length + index_length) / 1024 / 1024), 2)
                             FROM ', table_name);
            PREPARE stmt FROM @sql;
            EXECUTE stmt;
            DEALLOCATE PREPARE stmt;

        END LOOP;
        CLOSE cur;
    END;

    -- 输出性能报告
    SELECT
        table_name,
        record_count,
        table_size_mb,
        check_time,
        CASE
            WHEN record_count > 100000 THEN 'Large Table - Consider Partitioning'
            WHEN table_size_mb > 1000 THEN 'Large Size - Consider Optimization'
            ELSE 'Normal'
        END as status_recommendation
    FROM temp_performance_report
    ORDER BY table_size_mb DESC;

    DROP TEMPORARY TABLE temp_performance_report;
END$$
DELIMITER ;

-- 使用性能监控存储过程
-- CALL sp_attendance_performance_monitor();

-- =====================================================
-- 11. 定期维护任务
-- =====================================================

-- 分析表以更新统计信息
ANALYZE TABLE t_attendance_record;
ANALYZE TABLE t_attendance_rule;
ANALYZE TABLE t_attendance_schedule;
ANALYZE TABLE t_attendance_exception;
ANALYZE TABLE t_attendance_statistics;

-- 检查表是否有优化空间
SELECT
    table_name,
    data_free,
    ROUND(data_free/1024/1024, 2) AS free_space_mb,
    CASE
        WHEN data_free > 10485760 THEN 'Needs Optimization'
        ELSE 'OK'
    END as optimization_status
FROM information_schema.tables
WHERE table_schema = DATABASE()
    AND table_name LIKE 't_attendance_%'
ORDER BY data_free DESC;

-- 表优化建议（谨慎使用，会锁定表）
-- OPTIMIZE TABLE t_attendance_record;
-- OPTIMIZE TABLE t_attendance_rule;
-- OPTIMIZE TABLE t_attendance_schedule;
-- OPTIMIZE TABLE t_attendance_exception;
-- OPTIMIZE TABLE t_attendance_statistics;

-- =====================================================
-- 执行建议
-- =====================================================

-- 1. 首次执行建议：
--    a) 执行索引创建脚本 V1.0.2__04_optimize_attendance_indexes.sql
--    b) 运行 ANALYZE TABLE 更新统计信息
--    c) 执行性能基准测试查询记录初始性能数据

-- 2. 定期监控任务：
--    a) 每周执行 CALL sp_attendance_performance_monitor()
--    b) 每月检查表碎片率，必要时执行 OPTIMIZE TABLE
--    c) 监控慢查询日志，及时优化新出现的慢查询

-- 3. 性能验证：
--    a) 执行基准性能测试查询
--    b) 对比优化前后的执行时间
--    c) 确认达到响应时间提升80%的目标

COMMIT;