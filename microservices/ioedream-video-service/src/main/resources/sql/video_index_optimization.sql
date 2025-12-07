-- ============================================
-- IOE-DREAM 视频模块数据库索引优化SQL
-- 版本: 1.0.0
-- 日期: 2025-01-30
-- 说明: 基于性能优化需求，为视频模块添加索引
-- ============================================

-- ============================================
-- 1. t_video_device 表索引优化
-- ============================================

-- 1.1 组合索引：区域ID + 设备状态（区域设备查询）
CREATE INDEX IF NOT EXISTS `idx_video_device_area_status`
ON `t_video_device` (`area_id`, `device_status`, `deleted_flag`);

-- 1.2 组合索引：设备类型 + 设备状态（设备类型统计查询）
CREATE INDEX IF NOT EXISTS `idx_video_device_type_status`
ON `t_video_device` (`device_type`, `device_status`, `deleted_flag`);

-- 1.3 组合索引：在线状态 + 更新时间（在线设备查询）
CREATE INDEX IF NOT EXISTS `idx_video_device_online_update`
ON `t_video_device` (`online_status`, `update_time`, `deleted_flag`);

-- ============================================
-- 2. t_video_record 表索引优化
-- ============================================

-- 2.1 组合索引：设备ID + 录像时间（设备录像查询）
CREATE INDEX IF NOT EXISTS `idx_video_record_device_time`
ON `t_video_record` (`device_id`, `record_time`, `deleted_flag`);

-- 2.2 组合索引：区域ID + 录像时间（区域录像查询）
CREATE INDEX IF NOT EXISTS `idx_video_record_area_time`
ON `t_video_record` (`area_id`, `record_time`, `deleted_flag`);

-- 2.3 覆盖索引：录像时间 + 录像类型 + 录像状态（统计查询优化）
CREATE INDEX IF NOT EXISTS `idx_video_record_time_type_status`
ON `t_video_record` (`record_time`, `record_type`, `record_status`, `deleted_flag`);

-- 2.4 组合索引：录像时间范围查询（时间范围查询优化）
CREATE INDEX IF NOT EXISTS `idx_video_record_time_deleted`
ON `t_video_record` (`record_time`, `deleted_flag`);

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

