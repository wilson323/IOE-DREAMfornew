-- 考勤模块数据库索引优化脚本
-- 任务4.2：优化数据库查询和索引
-- 目标：响应时间提升80%，查询性能优化
-- 创建时间：2025-11-24
-- 遵循：不能修改核心表结构，只优化查询和索引

-- =====================================================
-- 1. 考勤记录表(t_attendance_record)索引优化
-- =====================================================

-- 分析：现有索引已经满足基本查询需求
-- 优化：增加复合索引覆盖更多查询场景

-- 员工当日考勤查询优化（高频查询）
CREATE INDEX IF NOT EXISTS `idx_employee_date_status`
ON `t_attendance_record` (`employee_id`, `attendance_date`, `attendance_status`);

-- 日期范围查询优化（报表查询）
CREATE INDEX IF NOT EXISTS `idx_attendance_date_create_time`
ON `t_attendance_record` (`attendance_date`, `create_time`);

-- 设备考勤记录查询优化（设备管理）
CREATE INDEX IF NOT EXISTS `idx_device_punch_time`
ON `t_attendance_record` (`punch_in_device_id`, `attendance_date`);

-- 考勤状态统计查询优化（统计分析）
CREATE INDEX IF NOT EXISTS `idx_status_attendance_date`
ON `t_attendance_record` (`attendance_status`, `attendance_date`);

-- 异常记录查询优化（异常处理）
CREATE INDEX IF NOT EXISTS `idx_exception_type_date`
ON `t_attendance_record` (`exception_type`, `attendance_date`);

-- 异常处理状态查询优化（异常管理）
CREATE INDEX IF NOT EXISTS `idx_is_processed_date`
ON `t_attendance_record` (`is_processed`, `attendance_date`);

-- 全文搜索索引（备注搜索）
-- 注意：MySQL 5.7+ 支持全文索引
-- ALTER TABLE `t_attendance_record` ADD FULLTEXT INDEX `ft_remark` (`remark`);

-- =====================================================
-- 2. 考勤规则表(t_attendance_rule)索引优化
-- =====================================================

-- 部门规则查询优化（部门规则）
CREATE INDEX IF NOT EXISTS `idx_dept_status_effective`
ON `t_attendance_rule` (`department_id`, `status`, `effective_date`);

-- 规则状态和有效期查询优化（规则管理）
CREATE INDEX IF NOT EXISTS `idx_status_effective_expiry`
ON `t_attendance_rule` (`status`, `effective_date`, `expiry_date`);

-- 规则编码快速查找优化
CREATE INDEX IF NOT EXISTS `idx_rule_code_status`
ON `t_attendance_rule` (`rule_code`, `status`);

-- GPS验证规则查询优化
CREATE INDEX IF NOT EXISTS `idx_gps_validation_range`
ON `t_attendance_rule` (`gps_validation`, `gps_range`);

-- =====================================================
-- 3. 排班管理表(t_attendance_schedule)索引优化
-- =====================================================

-- 员工排班查询优化（员工排班查询）
CREATE INDEX IF NOT EXISTS `idx_employee_date_shift`
ON `t_attendance_schedule` (`employee_id`, `schedule_date`, `shift_id`);

-- 排班日期范围查询优化（排班报表）
CREATE INDEX IF NOT EXISTS `idx_schedule_date_create`
ON `t_attendance_schedule` (`schedule_date`, `create_time`);

-- 班次排班查询优化（班次管理）
CREATE INDEX IF NOT EXISTS `idx_shift_date_schedule`
ON `t_attendance_schedule` (`shift_id`, `schedule_date`);

-- 排班类型查询优化（特殊排班统计）
CREATE INDEX IF NOT EXISTS `idx_schedule_type_date`
ON `t_attendance_schedule` (`schedule_type`, `schedule_date`);

-- 加班日排班查询优化（加班管理）
CREATE INDEX IF NOT EXISTS `idx_overtime_date`
ON `t_attendance_schedule` (`is_overtime_day`, `schedule_date`);

-- =====================================================
-- 4. 考勤异常表(t_attendance_exception)索引优化
-- =====================================================

-- 员工异常查询优化（员工异常管理）
CREATE INDEX IF NOT EXISTS `idx_employee_date_type`
ON `t_attendance_exception` (`employee_id`, `attendance_date`, `exception_type`);

-- 异常类型和状态查询优化（异常统计）
CREATE INDEX IF NOT EXISTS `idx_type_processed_date`
ON `t_attendance_exception` (`exception_type`, `is_processed`, `attendance_date`);

-- 异常级别查询优化（异常分级处理）
CREATE INDEX IF NOT EXISTS `idx_level_processed`
ON `t_attendance_exception` (`exception_level`, `is_processed`);

-- 自动检测异常查询优化（系统检测）
CREATE INDEX IF NOT EXISTS `idx_auto_detected`
ON `t_attendance_exception` (`auto_detected`, `is_processed`);

-- 异常处理查询优化（处理记录）
CREATE INDEX IF NOT EXISTS `idx_processed_by_time`
ON `t_attendance_exception` (`processed_by`, `processed_time`);

-- =====================================================
-- 5. 考勤统计表(t_attendance_statistics)索引优化
-- =====================================================

-- 员工统计查询优化（个人统计）
CREATE INDEX IF NOT EXISTS `idx_employee_type_period`
ON `t_attendance_statistics` (`employee_id`, `statistics_type`, `period_value`);

-- 统计类型和周期查询优化（综合统计）
CREATE IF NOT EXISTS `idx_type_period_date`
ON `t_attendance_statistics` (`statistics_type`, `period_value`, `statistics_date`);

-- 统计日期查询优化（日报查询）
CREATE INDEX IF NOT EXISTS `idx_statistics_date`
ON `t_attendance_statistics` (`statistics_date`, `create_time`);

-- 出勤率查询优化（出勤分析）
CREATE INDEX IF NOT EXISTS `idx_attendance_rate`
ON `t_attendance_statistics` (`attendance_rate`, `statistics_date`);

-- 工时统计查询优化（工时分析）
CREATE IF NOT EXISTS `idx_work_hours_overtime`
ON `t_attendance_statistics` (`work_hours`, `overtime_hours`);

-- =====================================================
-- 6. 考勤区域配置扩展表索引优化
-- =====================================================

-- 区域配置查询优化
CREATE INDEX IF NOT EXISTS `idx_area_config_status`
ON `t_attendance_area_config` (`area_id`, `status`);

-- 区域类型查询优化
CREATE INDEX IF NOT EXISTS `idx_area_type_create`
ON `t_attendance_area_config` (`area_type`, `create_time`);

-- =====================================================
-- 7. 考勤规则配置扩展表索引优化
-- =====================================================

-- 规则配置查询优化
CREATE INDEX IF NOT EXISTS `idx_rule_config_status`
ON `t_attendance_rule` (`rule_id`, `status`, `effective_date`);

-- 规则优先级查询优化
CREATE INDEX IF NOT EXISTS `idx_rule_priority`
ON `t_attendance_rule` (`priority`, `effective_date`);

-- =====================================================
-- 8. 性能监控索引
-- =====================================================

-- 审计字段统计索引（数据清理）
CREATE INDEX IF NOT EXISTS `idx_deleted_flag` ON `t_attendance_record` (`deleted_flag`, `create_time`);
CREATE INDEX IF NOT EXISTS `idx_deleted_flag_schedule` ON `t_attendance_schedule` (`deleted_flag`, `create_time`);
CREATE INDEX IF NOT EXISTS `idx_deleted_flag_exception` ON `t_attendance_exception` (`deleted_flag`, `create_time`);
CREATE INDEX IF NOT EXISTS `idx_deleted_flag_statistics` ON `t_attendance_statistics` (`deleted_flag`, `create_time`);
CREATE INDEX IF NOT EXISTS `idx_deleted_flag_rule` ON `t_attendance_rule` (`deleted_flag`, `create_time`);

-- 版本号查询优化（乐观锁）
CREATE INDEX IF NOT EXISTS `idx_version_update` ON `t_attendance_record` (`version`, `update_time`);
CREATE INDEX IF NOT EXISTS `idx_version_schedule` ON `t_attendance_schedule` (`version`, `update_time`);
CREATE IF NOT EXISTS `idx_version_exception` ON `t_attendance_exception` (`version`, `update_time`);
CREATE IF NOT EXISTS `idx_version_statistics` ON `t_attendance_statistics` (`version`, `update_time`);
CREATE IF NOT EXISTS `idx_version_rule` ON `t_attendance_rule` (`version`, `update_time`);

-- =====================================================
-- 9. 慢查询优化和分区提示
-- =====================================================

-- 慢查询分析结果
-- 主要高频查询模式：
-- 1. 员工当日考勤记录：employee_id + attendance_date
-- 2. 部门考勤统计：department_id + date_range
-- 3. 设备考勤记录：device_id + date_range
-- 4. 异常申请查询：employee_id + status + date_range
-- 5. 排班查询：employee_id + date_range

-- 优化建议：
-- 1. 定期执行 ANALYZE TABLE 更新统计信息
-- 2. 对于大数据量表考虑分区（按月或按年分区）
-- 3. 监控慢查询日志，针对性优化
-- 4. 考虑使用Redis缓存热点数据

-- 分区建议（适用于大数据量场景）：
-- -- t_attendance_record 按月分区
-- -- t_attendance_statistics 按季度分区
-- -- t_attendance_exception 按月分区

-- 执行验证查询：
EXPLAIN SELECT * FROM t_attendance_record
WHERE employee_id = 1 AND attendance_date = '2025-01-01' AND attendance_status = 'NORMAL';

EXPLAIN SELECT COUNT(*) FROM t_attendance_statistics
WHERE statistics_type = 'DAILY' AND period_value = '2025-01-01';

-- =====================================================
-- 10. 索引使用统计和分析
-- =====================================================

-- 查看索引使用情况（MySQL 5.7+）
-- SELECT * FROM sys.schema_index_statistics
-- WHERE table_schema = DATABASE() AND table_name = 't_attendance_record';

-- 查看表大小和索引大小
-- SELECT
--     table_name,
--     ROUND(((data_length + index_length) / 1024 / 1024), 2) AS table_size_mb,
--     ROUND((index_length / 1024 / 1024), 2) AS index_size_mb,
--     ROUND((index_length / (data_length + index_length)) * 100, 2) AS index_ratio_percent
-- FROM information_schema.tables
-- WHERE table_schema = DATABASE()
--     AND table_name LIKE 't_attendance_%'
-- ORDER BY (data_length + index_length) DESC;

-- =====================================================
-- 11. 性能监控和维护
-- =====================================================

-- 定期清理统计（可选）
-- 保留最近6个月的数据，删除过期数据
-- 注意：谨慎使用，确保业务允许
-- DELETE FROM t_attendance_record WHERE create_time < DATE_SUB(NOW(), INTERVAL 6 MONTH);

-- 更新表统计信息
-- ANALYZE TABLE t_attendance_record;
-- ANALYZE TABLE t_attendance_rule;
-- ANALYZE TABLE t_attendance_schedule;
-- ANALYZE TABLE t_attendance_exception;
-- ANALYZE TABLE t_attendance_statistics;