-- ============================================
-- IOE-DREAM 考勤模块数据库索引优化SQL
-- 版本: 1.0.0
-- 日期: 2025-01-30
-- 说明: 基于性能优化需求，为考勤模块添加索引
-- ============================================

-- ============================================
-- 1. t_attendance_record 表索引优化
-- ============================================

-- 1.1 组合索引：用户ID + 考勤日期（用户考勤历史查询）
CREATE INDEX IF NOT EXISTS `idx_attendance_record_user_date`
ON `t_attendance_record` (`user_id`, `attendance_date`, `deleted_flag`);

-- 1.2 组合索引：班次ID + 考勤日期（班次考勤统计查询）
CREATE INDEX IF NOT EXISTS `idx_attendance_record_shift_date`
ON `t_attendance_record` (`shift_id`, `attendance_date`, `deleted_flag`);

-- 1.3 组合索引：部门ID + 考勤日期（部门考勤统计查询）
CREATE INDEX IF NOT EXISTS `idx_attendance_record_dept_date`
ON `t_attendance_record` (`department_id`, `attendance_date`, `deleted_flag`);

-- 1.4 覆盖索引：考勤日期 + 考勤状态 + 考勤类型（统计查询优化）
CREATE INDEX IF NOT EXISTS `idx_attendance_record_date_status_type`
ON `t_attendance_record` (`attendance_date`, `attendance_status`, `attendance_type`, `deleted_flag`);

-- 1.5 组合索引：打卡时间范围查询（时间范围查询优化）
CREATE INDEX IF NOT EXISTS `idx_attendance_record_time_deleted`
ON `t_attendance_record` (`punch_time`, `deleted_flag`);

-- ============================================
-- 2. t_attendance_shift 表索引优化
-- ============================================

-- 2.1 组合索引：班次状态 + 更新时间（有效班次查询）
CREATE INDEX IF NOT EXISTS `idx_attendance_shift_status_update`
ON `t_attendance_shift` (`shift_status`, `update_time`, `deleted_flag`);

-- 2.2 组合索引：班次类型 + 班次状态（班次类型统计查询）
CREATE INDEX IF NOT EXISTS `idx_attendance_shift_type_status`
ON `t_attendance_shift` (`shift_type`, `shift_status`, `deleted_flag`);

-- ============================================
-- 3. 性能优化说明
-- ============================================

-- 3.1 索引使用原则
-- - 查询条件必须包含索引的第一列
-- - 避免在索引列上使用函数或表达式
-- - 合理使用覆盖索引，避免回表查询
-- - 定期分析慢查询日志，优化索引

-- 3.2 索引维护
-- - 定期执行 ANALYZE TABLE 更新索引统计信息
-- - 监控索引使用情况，删除未使用的索引
-- - 对于大表，考虑使用分区表 + 分区索引

-- 3.3 查询优化建议
-- - 查询时尽量使用索引列作为WHERE条件
-- - 避免SELECT *，只查询需要的字段
-- - 使用LIMIT限制返回结果数量
-- - 对于时间范围查询，使用游标分页替代深度分页

