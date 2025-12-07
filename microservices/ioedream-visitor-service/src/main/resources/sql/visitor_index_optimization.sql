-- ============================================
-- IOE-DREAM 访客模块数据库索引优化SQL
-- 版本: 1.0.0
-- 日期: 2025-01-30
-- 说明: 基于性能优化需求，为访客模块添加索引
-- ============================================

-- ============================================
-- 1. t_visitor_record 表索引优化
-- ============================================

-- 1.1 组合索引：访客ID + 访问时间（访客访问历史查询）
CREATE INDEX IF NOT EXISTS `idx_visitor_record_visitor_time`
ON `t_visitor_record` (`visitor_id`, `visit_time`, `deleted_flag`);

-- 1.2 组合索引：被访人ID + 访问时间（被访人访问记录查询）
CREATE INDEX IF NOT EXISTS `idx_visitor_record_host_time`
ON `t_visitor_record` (`host_user_id`, `visit_time`, `deleted_flag`);

-- 1.3 组合索引：区域ID + 访问时间（区域访客统计查询）
CREATE INDEX IF NOT EXISTS `idx_visitor_record_area_time`
ON `t_visitor_record` (`area_id`, `visit_time`, `deleted_flag`);

-- 1.4 覆盖索引：访问时间 + 访问状态 + 访问类型（统计查询优化）
CREATE INDEX IF NOT EXISTS `idx_visitor_record_time_status_type`
ON `t_visitor_record` (`visit_time`, `visit_status`, `visit_type`, `deleted_flag`);

-- 1.5 组合索引：访问时间范围查询（时间范围查询优化）
CREATE INDEX IF NOT EXISTS `idx_visitor_record_time_deleted`
ON `t_visitor_record` (`visit_time`, `deleted_flag`);

-- ============================================
-- 2. t_visitor_appointment 表索引优化
-- ============================================

-- 2.1 组合索引：访客ID + 预约时间（访客预约查询）
CREATE INDEX IF NOT EXISTS `idx_visitor_appointment_visitor_time`
ON `t_visitor_appointment` (`visitor_id`, `appointment_time`, `deleted_flag`);

-- 2.2 组合索引：被访人ID + 预约时间（被访人预约查询）
CREATE INDEX IF NOT EXISTS `idx_visitor_appointment_host_time`
ON `t_visitor_appointment` (`host_user_id`, `appointment_time`, `deleted_flag`);

-- 2.3 组合索引：预约状态 + 预约时间（预约状态查询）
CREATE INDEX IF NOT EXISTS `idx_visitor_appointment_status_time`
ON `t_visitor_appointment` (`appointment_status`, `appointment_time`, `deleted_flag`);

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

