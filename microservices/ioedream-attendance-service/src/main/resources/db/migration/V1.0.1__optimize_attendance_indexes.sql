-- ================================================
-- IOE-DREAM 考勤服务数据库索引优化脚本
-- ================================================
-- 功能: 优化移动端考勤服务的数据库查询性能
-- 版本: V1.0.1
-- 日期: 2025-01-30
-- ================================================

-- ================================================
-- 1. 考勤记录表索引优化
-- ================================================

-- 移动端打卡查询优化（用户ID + 日期）
CREATE INDEX IF NOT EXISTS idx_attendance_user_date
ON t_attendance_record(user_id, attendance_date DESC);

-- 移动端打卡列表查询优化（用户ID + 日期范围 + 状态）
CREATE INDEX IF NOT EXISTS idx_attendance_user_date_status
ON t_attendance_record(user_id, attendance_date DESC, attendance_status);

-- 移动端统计数据查询优化（用户ID + 日期范围）
CREATE INDEX IF NOT EXISTS idx_attendance_user_date_range
ON t_attendance_record(user_id, attendance_date DESC);

-- 移动端位置查询优化（用户ID + 位置信息）
CREATE INDEX IF NOT EXISTS idx_attendance_user_location
ON t_attendance_record(user_id, latitude, longitude);

-- 移动端生物识别查询优化（用户ID + 生物识别方式）
CREATE INDEX IF NOT EXISTS idx_attendance_user_biometric
ON t_attendance_record(user_id, biometric_type);

-- ================================================
-- 2. 排班记录表索引优化
-- ================================================

-- 移动端排班查询优化（员工ID + 日期范围）
CREATE INDEX IF NOT EXISTS idx_schedule_employee_date_range
ON t_attendance_schedule_record(employee_id, schedule_date ASC);

-- 移动端排班详情查询优化（员工ID + 日期 + 班次）
CREATE INDEX IF NOT EXISTS idx_schedule_employee_date_shift
ON t_attendance_schedule_record(employee_id, schedule_date ASC, shift_id);

-- 排班数据缓存预热查询优化（日期范围）
CREATE INDEX IF NOT EXISTS idx_schedule_date_range
ON t_attendance_schedule_record(schedule_date ASC);

-- ================================================
-- 3. 班次配置表索引优化
-- ================================================

-- 移动端班次查询优化（班次类型 + 状态）
CREATE INDEX IF NOT EXISTS idx_shift_type_status
ON t_attendance_work_shift(shift_type, deleted_flag);

-- ================================================
-- 4. 请假记录表索引优化（如果存在）
-- ================================================

-- 移动端请假查询优化（员工ID + 日期范围 + 状态）
CREATE INDEX IF NOT EXISTS idx_leave_employee_date_status
ON t_attendance_leave(employee_id, start_date DESC, leave_status);

-- ================================================
-- 5. 加班记录表索引优化（如果存在）
-- ================================================

-- 移动端加班查询优化（员工ID + 日期范围）
CREATE INDEX IF NOT EXISTS idx_overtime_employee_date
ON t_attendance_overtime(employee_id, overtime_date DESC);

-- ================================================
-- 6. 索引使用说明和验证
-- ================================================

-- 查看索引创建情况
-- SELECT
--     TABLE_NAME,
--     INDEX_NAME,
--     COLUMN_NAME,
--     SEQ_IN_INDEX,
--     INDEX_TYPE
-- FROM
--     INFORMATION_SCHEMA.STATISTICS
-- WHERE
--     TABLE_SCHEMA = DATABASE()
--     AND TABLE_NAME LIKE 't_attendance_%'
-- ORDER BY
--     TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX;

-- 查看索引使用情况（MySQL 5.7+）
-- SELECT
--     OBJECT_SCHEMA AS table_schema,
--     OBJECT_NAME AS table_name,
--     INDEX_NAME AS index_name,
--     COUNT_STAR AS count_rows,
--     COUNT_READ AS count_reads
-- FROM
--     performance_schema.table_io_waits_summary_by_index
-- WHERE
--     OBJECT_SCHEMA = DATABASE()
--     AND INDEX_NAME IS NOT NULL
-- ORDER BY
--     COUNT_READ DESC;

-- ================================================
-- 7. 索引效果验证SQL
-- ================================================

-- 验证移动端打卡查询性能
-- EXPLAIN SELECT *
-- FROM t_attendance_record
-- WHERE user_id = 1001
--   AND attendance_date >= '2025-01-01'
--   AND attendance_date <= '2025-01-31'
-- ORDER BY attendance_date DESC
-- LIMIT 20;

-- 验证移动端排班查询性能
-- EXPLAIN SELECT *
-- FROM t_attendance_schedule_record
-- WHERE employee_id = 1001
--   AND schedule_date >= '2025-01-01'
--   AND schedule_date <= '2025-01-31'
-- ORDER BY schedule_date ASC;

-- ================================================
-- 8. 索引维护建议
-- ================================================

-- 定期分析表以优化查询计划（建议每周执行一次）
-- ANALYZE TABLE t_attendance_record;
-- ANALYZE TABLE t_attendance_schedule_record;
-- ANALYZE TABLE t_attendance_work_shift;

-- 定期优化表以回收空间（建议每月执行一次，低峰期）
-- OPTIMIZE TABLE t_attendance_record;
-- OPTIMIZE TABLE t_attendance_schedule_record;
-- OPTIMIZE TABLE t_attendance_work_shift;

-- ================================================
-- 9. 复合索引设计说明
-- ================================================

-- 设计原则：
-- 1. 最左前缀原则：复合索引按照查询频率和选择性排列
-- 2. 覆盖索引：尽量使查询字段包含在索引中，避免回表
-- 3. 索引选择性：高选择性字段放在前面
-- 4. 索引数量：每个表索引不超过5个，避免影响写入性能

-- idx_attendance_user_date_status 使用说明：
-- - 可以支持：WHERE user_id = ? AND attendance_date >= ?
-- - 可以支持：WHERE user_id = ? AND attendance_date = ? AND attendance_status = ?
-- - 可以支持：WHERE user_id = ? ORDER BY attendance_date DESC
-- - 不能支持：WHERE attendance_status = ?（未使用最左前缀）

-- ================================================
-- 10. 监控和告警
-- ================================================

-- 创建索引使用率监控视图（可选）
-- CREATE OR REPLACE VIEW v_attendance_index_usage AS
-- SELECT
--     TABLE_NAME,
--     INDEX_NAME,
--     ROUND(COUNT_READ / NULLIF(COUNT_STAR, 0), 4) AS usage_ratio,
--     COUNT_READ AS read_count,
--     COUNT_STAR AS total_count
-- FROM
--     performance_schema.table_io_waits_summary_by_index
-- WHERE
--     OBJECT_SCHEMA = DATABASE()
--     AND INDEX_NAME IS NOT NULL
--     AND INDEX_NAME != 'PRIMARY'
-- ORDER BY
--     usage_ratio DESC;

-- 查询未使用的索引（可用于删除优化）
-- SELECT
--     TABLE_NAME,
--     INDEX_NAME,
--     ROUND(COUNT_READ / NULLIF(COUNT_STAR, 0), 4) AS usage_ratio
-- FROM
--     performance_schema.table_io_waits_summary_by_index
-- WHERE
--     OBJECT_SCHEMA = DATABASE()
--     AND INDEX_NAME IS NOT NULL
--     AND INDEX_NAME != 'PRIMARY'
--     AND COUNT_READ < 1000  -- 读取次数少于1000次视为低使用率
-- ORDER BY
--     usage_ratio ASC;
