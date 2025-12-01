-- 考勤模块数据库索引优化脚本
-- 基于现有表结构和查询模式进行优化

-- ========================================
-- 1. 考勤记录表索引优化 (t_attendance_record)
-- ========================================

-- 检查并删除重复或无效的索引
-- DROP INDEX IF EXISTS idx_employee_time ON t_attendance_record;

-- 基础索引
CREATE INDEX IF NOT EXISTS idx_attendance_record_employee_id ON t_attendance_record(employee_id);
CREATE INDEX IF NOT EXISTS idx_attendance_record_attendance_date ON t_attendance_record(attendance_date);
CREATE INDEX IF NOT EXISTS idx_attendance_record_status ON t_attendance_record(attendance_status);
CREATE INDEX IF NOT EXISTS idx_attendance_record_create_time ON t_attendance_record(create_time);
CREATE INDEX IF NOT EXISTS idx_attendance_record_update_time ON t_attendance_record(update_time);

-- 复合索引（高频查询组合）
CREATE INDEX IF NOT EXISTS idx_attendance_record_emp_date ON t_attendance_record(employee_id, attendance_date);
CREATE INDEX IF NOT EXISTS idx_attendance_record_date_status ON t_attendance_record(attendance_date, attendance_status);
CREATE INDEX IF NOT EXISTS idx_attendance_record_emp_status ON t_attendance_record(employee_id, attendance_status);
CREATE INDEX IF NOT EXISTS idx_attendance_record_date_create ON t_attendance_record(attendance_date, create_time);

-- 特殊字段索引
CREATE INDEX IF NOT EXISTS idx_attendance_record_punch_type ON t_attendance_record(punch_type);
CREATE INDEX IF NOT EXISTS idx_attendance_record_device_id ON t_attendance_record(device_id);
CREATE INDEX IF NOT EXISTS idx_attendance_record_exception_type ON t_attendance_record(exception_type);

-- ========================================
-- 2. 考勤规则表索引优化 (t_attendance_rule)
-- ========================================

CREATE INDEX IF NOT EXISTS idx_attendance_rule_company_id ON t_attendance_rule(company_id);
CREATE INDEX IF NOT EXISTS idx_attendance_rule_department_id ON t_attendance_rule(department_id);
CREATE INDEX IF NOT EXISTS idx_attendance_rule_status ON t_attendance_rule(status);
CREATE INDEX IF NOT EXISTS idx_attendance_rule_effective_date ON t_attendance_rule(effective_date);
CREATE INDEX IF NOT EXISTS idx_attendance_rule_expiry_date ON t_attendance_rule(expiry_date);
CREATE INDEX IF NOT EXISTS idx_attendance_rule_employee_type ON t_attendance_rule(employee_type);

-- 复合索引
CREATE INDEX IF NOT EXISTS idx_attendance_rule_company_status ON t_attendance_rule(company_id, status);
CREATE INDEX IF NOT EXISTS idx_attendance_rule_dept_status ON t_attendance_rule(department_id, status);

-- ========================================
-- 3. 排班表索引优化 (t_attendance_schedule)
-- ========================================

CREATE INDEX IF NOT EXISTS idx_attendance_schedule_employee_id ON t_attendance_schedule(employee_id);
CREATE INDEX IF NOT EXISTS idx_attendance_schedule_schedule_date ON t_attendance_schedule(schedule_date);
CREATE INDEX IF NOT EXISTS idx_attendance_schedule_shift_id ON t_attendance_schedule(shift_id);
CREATE INDEX IF NOT EXISTS idx_attendance_schedule_type ON t_attendance_schedule(schedule_type);
CREATE INDEX IF NOT EXISTS idx_attendance_schedule_create_time ON t_attendance_schedule(create_time);

-- 复合索引
CREATE INDEX IF NOT EXISTS idx_attendance_schedule_emp_date ON t_attendance_schedule(employee_id, schedule_date);
CREATE INDEX IF NOT EXISTS idx_attendance_schedule_date_type ON t_attendance_schedule(schedule_date, schedule_type);

-- ========================================
-- 4. 考勤异常表索引优化 (t_attendance_exception)
-- ========================================

CREATE INDEX IF NOT EXISTS idx_attendance_exception_employee_id ON t_attendance_exception(employee_id);
CREATE INDEX IF NOT EXISTS idx_attendance_exception_attendance_date ON t_attendance_exception(attendance_date);
CREATE INDEX IF NOT EXISTS idx_attendance_exception_type ON t_attendance_exception(exception_type);
CREATE INDEX IF NOT EXISTS idx_attendance_exception_is_processed ON t_attendance_exception(is_processed);
CREATE INDEX IF NOT EXISTS idx_attendance_exception_create_time ON t_attendance_exception(create_time);
CREATE INDEX IF NOT EXISTS idx_attendance_exception_level ON t_attendance_exception(exception_level);

-- 复合索引
CREATE INDEX IF NOT EXISTS idx_attendance_exception_emp_date ON t_attendance_exception(employee_id, attendance_date);
CREATE INDEX IF NOT EXISTS idx_attendance_exception_type_processed ON t_attendance_exception(exception_type, is_processed);

-- ========================================
-- 5. 考勤统计表索引优化 (t_attendance_statistics)
-- ========================================

CREATE INDEX IF NOT EXISTS idx_attendance_statistics_employee_id ON t_attendance_statistics(employee_id);
CREATE INDEX IF NOT EXISTS idx_attendance_statistics_type ON t_attendance_statistics(statistics_type);
CREATE INDEX IF NOT EXISTS idx_attendance_statistics_period ON t_attendance_statistics(statistics_period);
CREATE INDEX IF NOT EXISTS idx_attendance_statistics_date ON t_attendance_statistics(statistics_date);
CREATE INDEX IF NOT EXISTS idx_attendance_statistics_create_time ON t_attendance_statistics(create_time);

-- 复合索引
CREATE INDEX IF NOT EXISTS idx_attendance_statistics_emp_type ON t_attendance_statistics(employee_id, statistics_type);
CREATE INDEX IF NOT EXISTS idx_attendance_statistics_type_period ON t_attendance_statistics(statistics_type, statistics_period);
CREATE INDEX IF NOT EXISTS idx_attendance_statistics_date_type ON t_attendance_statistics(statistics_date, statistics_type);

-- ========================================
-- 6. 性能监控和分析查询
-- ========================================

-- 查看索引使用情况
-- SELECT TABLE_NAME, INDEX_NAME, CARDINALITY FROM information_schema.STATISTICS
-- WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME LIKE 't_attendance%'
-- ORDER BY TABLE_NAME, INDEX_NAME;

-- 查看表大小和索引大小
-- SELECT
--     TABLE_NAME,
--     ROUND(((DATA_LENGTH + INDEX_LENGTH) / 1024 / 1024), 2) AS total_size_mb,
--     ROUND((DATA_LENGTH / 1024 / 1024), 2) AS data_size_mb,
--     ROUND((INDEX_LENGTH / 1024 / 1024), 2) AS index_size_mb
-- FROM information_schema.TABLES
-- WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME LIKE 't_attendance%'
-- ORDER BY total_size_mb DESC;

-- 查看慢查询（需要开启慢查询日志）
-- SELECT * FROM mysql.slow_log WHERE sql_text LIKE '%t_attendance%' ORDER BY start_time DESC LIMIT 10;

-- ========================================
-- 7. 索引维护建议
-- ========================================

-- 定期分析表以更新索引统计信息
-- ANALYZE TABLE t_attendance_record;
-- ANALYZE TABLE t_attendance_rule;
-- ANALYZE TABLE t_attendance_schedule;
-- ANALYZE TABLE t_attendance_exception;
-- ANALYZE TABLE t_attendance_statistics;

-- 定期优化表（适用于MyISAM，InnoDB通常不需要）
-- OPTIMIZE TABLE t_attendance_record;

-- ========================================
-- 8. 使用说明和注意事项
-- ========================================

-- 1. 执行前请备份数据库
-- 2. 在低峰期执行索引创建操作
-- 3. 监控索引创建过程中的性能影响
-- 4. 定期检查索引使用情况，删除未使用的索引
-- 5. 根据实际查询模式调整索引策略
-- 6. 大表索引创建建议分批进行
-- 7. 注意索引维护成本，避免过度索引
