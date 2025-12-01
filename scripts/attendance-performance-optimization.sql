-- 考勤模块性能优化SQL脚本
-- 适用于大数据量场景下的数据库性能优化

-- ========================================
-- 1. 数据库索引优化
-- ========================================

-- 考勤记录表索引优化
-- 主键索引（已存在）
-- ALTER TABLE t_attendance_record ADD PRIMARY KEY (record_id);

-- 员工ID索引（高频查询字段）
CREATE INDEX idx_attendance_record_employee_id ON t_attendance_record(employee_id);

-- 考勤日期索引（高频查询字段）
CREATE INDEX idx_attendance_record_attendance_date ON t_attendance_record(attendance_date);

-- 复合索引：员工ID + 日期（最常用查询组合）
CREATE INDEX idx_attendance_record_emp_date ON t_attendance_record(employee_id, attendance_date);

-- 复合索引：日期 + 状态（统计查询）
CREATE INDEX idx_attendance_record_date_status ON t_attendance_record(attendance_date, attendance_status);

-- 创建时间索引（用于时间范围查询）
CREATE INDEX idx_attendance_record_create_time ON t_attendance_record(create_time);

-- 更新时间索引
CREATE INDEX idx_attendance_record_update_time ON t_attendance_record(update_time);

-- 部门ID索引（如果存在）
-- ALTER TABLE t_attendance_record ADD COLUMN department_id BIGINT;
-- CREATE INDEX idx_attendance_record_department_id ON t_attendance_record(department_id);

-- 排班表索引优化
CREATE INDEX idx_attendance_schedule_employee_id ON t_attendance_schedule(employee_id);
CREATE INDEX idx_attendance_schedule_schedule_date ON t_attendance_schedule(schedule_date);
CREATE INDEX idx_attendance_schedule_emp_date ON t_attendance_schedule(employee_id, schedule_date);
CREATE INDEX idx_attendance_schedule_date_type ON t_attendance_schedule(schedule_date, schedule_type);
CREATE INDEX idx_attendance_schedule_priority ON t_attendance_schedule(priority DESC);

-- 考勤规则表索引优化
CREATE INDEX idx_attendance_rule_enabled ON t_attendance_rule(enabled);
CREATE INDEX idx_attendance_rule_priority ON t_attendance_rule(priority DESC);
CREATE INDEX idx_attendance_rule_emp_type ON t_attendance_rule(employee_type);
CREATE INDEX idx_attendance_rule_effective ON t_attendance_rule(effective_date, expiry_date);

-- 考勤异常表索引优化
CREATE INDEX idx_attendance_exception_employee_id ON t_attendance_exception(employee_id);
CREATE INDEX idx_attendance_exception_date_type ON t_attendance_exception(exception_date, exception_type);
CREATE INDEX idx_attendance_exception_status ON t_attendance_exception(status);

-- 补卡申请表索引优化
CREATE INDEX idx_punch_correction_employee_id ON t_punch_correction(employee_id);
CREATE INDEX idx_punch_correction_status ON t_punch_correction(status);
CREATE INDEX idx_punch_correction_date ON t_punch_correction(correction_date);

-- ========================================
-- 2. 分区表设计（适用于超大数据量）
-- ========================================

-- 如果数据量极大（例如超过1000万记录），考虑按月份分区
-- 注意：以下为示例，实际使用时需要根据数据量和查询模式调整

-- 按月份分区考勤记录表（MySQL 8.0+）
-- ALTER TABLE t_attendance_record
-- PARTITION BY RANGE (TO_DAYS(attendance_date)) (
--     PARTITION p202401 VALUES LESS THAN (TO_DAYS('2024-02-01')),
--     PARTITION p202402 VALUES LESS THAN (TO_DAYS('2024-03-01')),
--     PARTITION p202403 VALUES LESS THAN (TO_DAYS('2024-04-01')),
--     PARTITION p202404 VALUES LESS THAN (TO_DAYS('2024-05-01')),
--     PARTITION p202405 VALUES LESS THAN (TO_DAYS('2024-06-01')),
--     PARTITION p202406 VALUES LESS THAN (TO_DAYS('2024-07-01')),
--     PARTITION p202407 VALUES LESS THAN (TO_DAYS('2024-08-01')),
--     PARTITION p202408 VALUES LESS THAN (TO_DAYS('2024-09-01')),
--     PARTITION p202409 VALUES LESS THAN (TO_DAYS('2024-10-01')),
--     PARTITION p202410 VALUES LESS THAN (TO_DAYS('2024-11-01')),
--     PARTITION p202411 VALUES LESS THAN (TO_DAYS('2024-12-01')),
--     PARTITION p202412 VALUES LESS THAN (TO_DAYS('2025-01-01')),
--     PARTITION pmax VALUES LESS THAN MAXVALUE
-- );

-- ========================================
-- 3. 查询优化视图
-- ========================================

-- 员工月度考勤汇总视图
CREATE OR REPLACE VIEW v_employee_monthly_attendance AS
SELECT
    employee_id,
    DATE_FORMAT(attendance_date, '%Y-%m') as month,
    COUNT(*) as total_days,
    COUNT(CASE WHEN attendance_status = 'NORMAL' THEN 1 END) as normal_days,
    COUNT(CASE WHEN attendance_status = 'LATE' THEN 1 END) as late_count,
    COUNT(CASE WHEN attendance_status = 'EARLY_LEAVE' THEN 1 END) as early_leave_count,
    COUNT(CASE WHEN attendance_status = 'ABSENCE' THEN 1 END) as absence_count,
    AVG(work_hours) as avg_work_hours,
    SUM(overtime_hours) as total_overtime_hours,
    MAX(update_time) as last_update
FROM t_attendance_record
WHERE deleted_flag = false
GROUP BY employee_id, DATE_FORMAT(attendance_date, '%Y-%m');

-- 部门月度考勤统计视图
CREATE OR REPLACE VIEW v_department_monthly_attendance AS
SELECT
    e.department_id,
    d.department_name,
    DATE_FORMAT(ar.attendance_date, '%Y-%m') as month,
    COUNT(DISTINCT ar.employee_id) as employee_count,
    COUNT(*) as total_records,
    COUNT(CASE WHEN ar.attendance_status = 'NORMAL' THEN 1 END) as normal_records,
    COUNT(CASE WHEN ar.attendance_status = 'LATE' THEN 1 END) as late_records,
    COUNT(CASE WHEN ar.attendance_status = 'EARLY_LEAVE' THEN 1 END) as early_leave_records,
    AVG(ar.work_hours) as avg_work_hours
FROM t_attendance_record ar
JOIN t_employee e ON ar.employee_id = e.employee_id
JOIN t_department d ON e.department_id = d.department_id
WHERE ar.deleted_flag = false AND e.deleted_flag = false
GROUP BY e.department_id, d.department_name, DATE_FORMAT(ar.attendance_date, '%Y-%m');

-- 今日考勤状态视图（实时查询优化）
CREATE OR REPLACE VIEW v_today_attendance_status AS
SELECT
    employee_id,
    employee_name,
    department_id,
    department_name,
    MAX(CASE WHEN punch_type = '上班' THEN punch_time END) as punch_in_time,
    MAX(CASE WHEN punch_type = '下班' THEN punch_time END) as punch_out_time,
    MAX(CASE WHEN punch_type = '上班' THEN location END) as punch_in_location,
    MAX(CASE WHEN punch_type = '下班' THEN location END) as punch_out_location,
    CASE
        WHEN MAX(CASE WHEN punch_type = '上班' THEN 1 END) = 0 THEN '未上班'
        WHEN MAX(CASE WHEN punch_type = '下班' THEN 1 END) = 0 THEN '未下班'
        ELSE '已完成'
    END as status
FROM (
    SELECT
        ar.employee_id,
        e.employee_name,
        e.department_id,
        d.department_name,
        SUBSTRING(ar.punch_time, 12, 5) as punch_time,
        CASE
            WHEN ar.punch_type = 1 THEN '上班'
            WHEN ar.punch_type = 2 THEN '下班'
        END as punch_type,
        ar.location
    FROM t_attendance_record ar
    JOIN t_employee e ON ar.employee_id = e.employee_id
    JOIN t_department d ON e.department_id = d.department_id
    WHERE ar.attendance_date = CURDATE()
    AND ar.deleted_flag = false
) t
GROUP BY employee_id, employee_name, department_id, department_name;

-- ========================================
-- 4. 性能监控存储过程
-- ========================================

-- 清理历史数据的存储过程
DELIMITER //
CREATE PROCEDURE CleanupAttendanceRecords(
    IN months_ago INT,
    IN batch_size INT
)
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE affected_rows INT DEFAULT 0;
    DECLARE total_deleted INT DEFAULT 0;

    -- 创建临时表存储要删除的记录ID
    CREATE TEMPORARY TABLE IF NOT EXISTS temp_delete_records (
        record_id BIGINT PRIMARY KEY
    ) ENGINE=MEMORY;

    -- 获取要删除的记录ID
    INSERT INTO temp_delete_records
    SELECT record_id
    FROM t_attendance_record
    WHERE attendance_date < DATE_SUB(CURDATE(), INTERVAL months_ago MONTH)
      AND deleted_flag = true
    LIMIT batch_size;

    -- 批量删除记录
    WHILE NOT done DO
        DELETE FROM t_attendance_record
        WHERE record_id IN (SELECT record_id FROM temp_delete_records LIMIT 1000);

        SET affected_rows = ROW_COUNT();
        SET total_deleted = total_deleted + affected_rows;

        -- 清空临时表并重新填充
        DELETE FROM temp_delete_records;
        INSERT INTO temp_delete_records
        SELECT record_id
        FROM t_attendance_record
        WHERE attendance_date < DATE_SUB(CURDATE(), INTERVAL months_ago MONTH)
          AND deleted_flag = true
          AND record_id NOT IN (
              SELECT record_id FROM t_attendance_record
              WHERE attendance_date < DATE_SUB(CURDATE(), INTERVAL months_ago MONTH)
                AND deleted_flag = false
          )
        LIMIT batch_size;

        -- 检查是否还有记录需要删除
        IF (SELECT COUNT(*) FROM temp_delete_records) = 0 THEN
            SET done = TRUE;
        END IF;

        -- 短暂休眠避免锁表
        DO SLEEP(0.1);
    END WHILE;

    -- 删除临时表
    DROP TEMPORARY TABLE temp_delete_records;

    SELECT total_deleted as deleted_count;
END //
DELIMITER ;

-- 性能统计存储过程
DELIMITER //
CREATE PROCEDURE GetAttendancePerformanceStats(
    IN date_from DATE,
    IN date_to DATE
)
BEGIN
    SELECT
        '总打卡记录数' as metric_name,
        COUNT(*) as count_value,
        '条' as unit
    FROM t_attendance_record
    WHERE attendance_date BETWEEN date_from AND date_to
      AND deleted_flag = false

    UNION ALL

    SELECT
        '平均响应时间(模拟)' as metric_name,
        ROUND(AVG(UNIX_TIMESTAMP(update_time) - UNIX_TIMESTAMP(create_time)), 2) as count_value,
        '秒' as unit
    FROM t_attendance_record
    WHERE attendance_date BETWEEN date_from AND date_to
      AND deleted_flag = false

    UNION ALL

    SELECT
        '异常记录占比' as metric_name,
        ROUND(COUNT(CASE WHEN attendance_status != 'NORMAL' THEN 1 END) * 100.0 / COUNT(*), 2) as count_value,
        '%' as unit
    FROM t_attendance_record
    WHERE attendance_date BETWEEN date_from AND date_to
      AND deleted_flag = false;
END //
DELIMITER ;

-- ========================================
-- 5. 缓存优化建议
-- ========================================

-- 创建缓存表（Redis/Memcached替代方案）
CREATE TABLE IF NOT EXISTS t_attendance_cache (
    cache_key VARCHAR(255) PRIMARY KEY,
    cache_value LONGTEXT,
    cache_type VARCHAR(50) NOT NULL,
    expire_time DATETIME NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_cache_expire (expire_time),
    INDEX idx_cache_type (cache_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 缓存清理存储过程
DELIMITER //
CREATE PROCEDURE CleanupExpiredCache()
BEGIN
    DELETE FROM t_attendance_cache
    WHERE expire_time < NOW();

    SELECT ROW_COUNT() as cleaned_records;
END //
DELIMITER ;

-- ========================================
-- 6. 分页优化查询示例
-- ========================================

-- 优化的分页查询（使用游标分页）
-- 第一页查询
SELECT
    ar.record_id,
    ar.employee_id,
    e.employee_name,
    d.department_name,
    ar.attendance_date,
    ar.attendance_status,
    ar.work_hours
FROM t_attendance_record ar
JOIN t_employee e ON ar.employee_id = e.employee_id
JOIN t_department d ON e.department_id = d.department_id
WHERE ar.deleted_flag = false
  AND ar.attendance_date >= '2024-01-01'
ORDER BY ar.attendance_date DESC, ar.record_id DESC
LIMIT 20;

-- 后续页查询（使用WHERE条件和ID避免OFFSET）
SELECT
    ar.record_id,
    ar.employee_id,
    e.employee_name,
    d.department_name,
    ar.attendance_date,
    ar.attendance_status,
    ar.work_hours
FROM t_attendance_record ar
JOIN t_employee e ON ar.employee_id = e.employee_id
JOIN t_department d ON e.department_id = d.department_id
WHERE ar.deleted_flag = false
  AND ar.attendance_date >= '2024-01-01'
  AND (ar.attendance_date < '2024-01-15' OR
       (ar.attendance_date = '2024-01-15' AND ar.record_id < last_seen_id))
ORDER BY ar.attendance_date DESC, ar.record_id DESC
LIMIT 20;

-- ========================================
-- 7. 监控和诊断查询
-- ========================================

-- 查看慢查询日志
-- SHOW VARIABLES LIKE 'slow_query_log%';
-- SHOW VARIABLES LIKE 'long_query_time';

-- 查看索引使用情况
SELECT
    TABLE_NAME,
    INDEX_NAME,
    CARDINALITY,
    INDEX_LENGTH,
    COMMENT
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME LIKE 't_attendance%'
ORDER BY TABLE_NAME, INDEX_NAME;

-- 查看表大小
SELECT
    TABLE_NAME,
    ROUND(((DATA_LENGTH + INDEX_LENGTH) / 1024 / 1024), 2) AS table_size_mb,
    ROUND((DATA_LENGTH / 1024 / 1024), 2) AS data_size_mb,
    ROUND((INDEX_LENGTH / 1024 / 1024), 2) AS index_size_mb
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME LIKE 't_attendance%'
ORDER BY table_size_mb DESC;

-- 查看数据分布
SELECT
    DATE_FORMAT(attendance_date, '%Y-%m') as month,
    COUNT(*) as record_count,
    COUNT(DISTINCT employee_id) as employee_count
FROM t_attendance_record
WHERE deleted_flag = false
GROUP BY DATE_FORMAT(attendance_date, '%Y-%m')
ORDER BY month DESC;

-- ========================================
-- 8. 执行计划分析示例
-- ========================================

-- 分析查询执行计划（实际使用时取消注释）
-- EXPLAIN
-- SELECT * FROM t_attendance_record
-- WHERE employee_id = 1
--   AND attendance_date BETWEEN '2024-01-01' AND '2024-01-31'
-- ORDER BY attendance_date DESC;

-- EXPLAIN
-- SELECT
--     e.employee_id,
--     e.employee_name,
--     COUNT(*) as record_count
-- FROM t_employee e
-- LEFT JOIN t_attendance_record ar ON e.employee_id = ar.employee_id
--   AND ar.deleted_flag = false
-- GROUP BY e.employee_id, e.employee_name
-- ORDER BY record_count DESC;

-- ========================================
-- 9. 配置优化建议
-- ========================================

-- 性能相关配置建议（需要在my.cnf中设置）
/*
[mysqld]
# 查询缓存
query_cache_type = 1
query_cache_size = 256M

# 连接数
max_connections = 500
max_connect_errors = 1000

# InnoDB配置
innodb_buffer_pool_size = 1G
innodb_log_file_size = 256M
innodb_flush_log_at_trx_commit = 2
innodb_flush_method = O_DIRECT

# 慢查询日志
slow_query_log = 1
slow_query_log_file = /var/log/mysql/slow.log
long_query_time = 2

# 二进制日志
log-bin = mysql-bin
binlog_format = ROW
expire_logs_days = 7
*/

-- ========================================
-- 10. 使用说明
-- ========================================

-- 1. 执行索引优化前，先评估当前查询性能
-- 2. 在生产环境执行前，先在测试环境验证
-- 3. 定期执行数据清理存储过程
-- 4. 监控慢查询日志，及时优化
-- 5. 根据实际数据量调整分区策略
-- 6. 定期分析执行计划，优化查询语句