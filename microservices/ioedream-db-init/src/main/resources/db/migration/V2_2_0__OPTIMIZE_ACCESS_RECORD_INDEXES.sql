-- =====================================================
-- IOE-DREAM Flyway 迁移脚本
-- 版本: V2.2.0
-- 描述: 门禁记录表索引优化 - 添加复合索引提升查询性能
-- 创建日期: 2025-01-30
-- =====================================================

-- =====================================================
-- 1. 优化 t_access_record 表索引
-- =====================================================

-- 添加复合索引：用于批量上传幂等性检查（userId + deviceId + accessTime）
CREATE INDEX IF NOT EXISTS idx_user_device_time ON t_access_record(user_id, device_id, access_time, deleted_flag);

-- 添加复合索引：用于按区域和时间范围查询
CREATE INDEX IF NOT EXISTS idx_area_time ON t_access_record(area_id, access_time, deleted_flag);

-- 添加复合索引：用于按设备和时间范围查询
CREATE INDEX IF NOT EXISTS idx_device_time_result ON t_access_record(device_id, access_time, access_result, deleted_flag);

-- 添加复合索引：用于按用户和时间范围查询
CREATE INDEX IF NOT EXISTS idx_user_time_result ON t_access_record(user_id, access_time, access_result, deleted_flag);

-- =====================================================
-- 2. 优化 t_access_anti_passback_record 表索引
-- =====================================================

-- 确保复合索引存在（用于反潜验证查询）
-- 注意：V2.1.9已创建，这里确保存在
CREATE INDEX IF NOT EXISTS idx_user_device_time ON t_access_anti_passback_record(user_id, device_id, record_time, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_user_area_time ON t_access_anti_passback_record(user_id, area_id, record_time, deleted_flag);

-- =====================================================
-- 3. 优化 t_access_interlock_record 表索引
-- =====================================================

-- 添加复合索引：用于互锁验证查询
CREATE INDEX IF NOT EXISTS idx_device_group_status ON t_access_interlock_record(device_id, interlock_group_id, lock_status, deleted_flag);

-- =====================================================
-- 4. 优化 t_access_multi_person_record 表索引
-- =====================================================

-- 确保复合索引存在（用于多人验证查询）
-- 注意：V2.1.9已创建，这里确保存在
CREATE INDEX IF NOT EXISTS idx_area_device_status ON t_access_multi_person_record(area_id, device_id, status, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_status_expire_time ON t_access_multi_person_record(status, expire_time, deleted_flag);

-- =====================================================
-- 完成标记
-- =====================================================

SELECT 'V2.2.0 门禁记录表索引优化完成' AS status;
