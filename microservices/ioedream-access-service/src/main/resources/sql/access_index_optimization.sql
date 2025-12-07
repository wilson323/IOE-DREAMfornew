-- ============================================
-- IOE-DREAM 门禁模块数据库索引优化SQL
-- 版本: 1.0.0
-- 日期: 2025-01-30
-- 说明: 基于性能优化需求，为门禁模块添加索引
-- ============================================

-- ============================================
-- 1. t_access_record 表索引优化
-- ============================================

-- 1.1 组合索引：用户ID + 访问时间（用户访问历史查询）
CREATE INDEX IF NOT EXISTS `idx_access_record_user_time`
ON `t_access_record` (`user_id`, `access_time`, `deleted_flag`);

-- 1.2 组合索引：设备ID + 访问时间（设备访问记录查询）
CREATE INDEX IF NOT EXISTS `idx_access_record_device_time`
ON `t_access_record` (`device_id`, `access_time`, `deleted_flag`);

-- 1.3 组合索引：区域ID + 访问时间（区域访问统计查询）
CREATE INDEX IF NOT EXISTS `idx_access_record_area_time`
ON `t_access_record` (`area_id`, `access_time`, `deleted_flag`);

-- 1.4 覆盖索引：访问时间 + 访问结果 + 访问类型（统计查询优化）
CREATE INDEX IF NOT EXISTS `idx_access_record_time_result_type`
ON `t_access_record` (`access_time`, `access_result`, `access_type`, `deleted_flag`);

-- 1.5 组合索引：访问时间范围查询（时间范围查询优化）
CREATE INDEX IF NOT EXISTS `idx_access_record_time_deleted`
ON `t_access_record` (`access_time`, `deleted_flag`);

-- ============================================
-- 2. t_access_device 表索引优化
-- ============================================

-- 2.1 组合索引：区域ID + 设备状态（区域设备查询）
CREATE INDEX IF NOT EXISTS `idx_access_device_area_status`
ON `t_access_device` (`area_id`, `device_status`, `deleted_flag`);

-- 2.2 组合索引：设备类型 + 设备状态（设备类型统计查询）
CREATE INDEX IF NOT EXISTS `idx_access_device_type_status`
ON `t_access_device` (`device_type`, `device_status`, `deleted_flag`);

-- 2.3 组合索引：在线状态 + 更新时间（在线设备查询）
CREATE INDEX IF NOT EXISTS `idx_access_device_online_update`
ON `t_access_device` (`online_status`, `update_time`, `deleted_flag`);

-- ============================================
-- 3. t_access_permission 表索引优化
-- ============================================

-- 3.1 组合索引：用户ID + 区域ID + 权限状态（用户权限查询）
CREATE INDEX IF NOT EXISTS `idx_access_permission_user_area_status`
ON `t_access_permission` (`user_id`, `area_id`, `permission_status`, `deleted_flag`);

-- 3.2 组合索引：区域ID + 权限状态（区域权限查询）
CREATE INDEX IF NOT EXISTS `idx_access_permission_area_status`
ON `t_access_permission` (`area_id`, `permission_status`, `deleted_flag`);

-- 3.3 组合索引：权限类型 + 权限状态（权限类型统计查询）
CREATE INDEX IF NOT EXISTS `idx_access_permission_type_status`
ON `t_access_permission` (`permission_type`, `permission_status`, `deleted_flag`);

-- ============================================
-- 4. 性能优化说明
-- ============================================

-- 4.1 索引使用原则
-- - 查询条件必须包含索引的第一列
-- - 避免在索引列上使用函数或表达式
-- - 合理使用覆盖索引，避免回表查询
-- - 定期分析慢查询日志，优化索引

-- 4.2 索引维护
-- - 定期执行 ANALYZE TABLE 更新索引统计信息
-- - 监控索引使用情况，删除未使用的索引
-- - 对于大表，考虑使用分区表 + 分区索引

-- 4.3 查询优化建议
-- - 查询时尽量使用索引列作为WHERE条件
-- - 避免SELECT *，只查询需要的字段
-- - 使用LIMIT限制返回结果数量
-- - 对于时间范围查询，使用游标分页替代深度分页

