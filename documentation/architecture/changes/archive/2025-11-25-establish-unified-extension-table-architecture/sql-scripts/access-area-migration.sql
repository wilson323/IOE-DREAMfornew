-- =====================================================
-- IOE-DREAM 项目 AccessAreaEntity 架构修正数据迁移脚本
-- 修正时间：2025-11-25
-- 修正目标：将 AccessAreaEntity 重构为严格扩展表架构
-- 架构模式：AreaEntity (基础表) + AccessAreaExtEntity (扩展表)
-- =====================================================

-- 1. 备份现有数据（重要！）
-- -------------------------------------------------
CREATE TABLE IF NOT EXISTS t_access_area_backup AS
SELECT * FROM t_access_area;

-- 2. 创建门禁区域扩展表（如果不存在）
-- -------------------------------------------------
CREATE TABLE IF NOT EXISTS t_access_area_ext (
    ext_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '扩展ID',
    area_id BIGINT NOT NULL COMMENT '基础区域ID',
    access_enabled INT DEFAULT 1 COMMENT '是否启用门禁(0:禁用 1:启用)',
    access_level INT DEFAULT 1 COMMENT '门禁级别(1:普通 2:重要 3:核心)',
    access_mode VARCHAR(100) DEFAULT '卡' COMMENT '门禁模式',
    special_auth_required INT DEFAULT 0 COMMENT '是否需要特殊授权(0:不需要 1:需要)',
    valid_time_start VARCHAR(5) COMMENT '有效时间段开始(HH:mm格式)',
    valid_time_end VARCHAR(5) COMMENT '有效时间段结束(HH:mm格式)',
    valid_weekdays VARCHAR(20) COMMENT '有效星期(逗号分隔,1-7代表周一到周日)',
    device_count INT DEFAULT 0 COMMENT '关联设备数量',
    guard_required BOOLEAN DEFAULT FALSE COMMENT '是否需要安保人员',
    time_restrictions TEXT COMMENT '时间限制配置(JSON)',
    visitor_allowed BOOLEAN DEFAULT TRUE COMMENT '是否允许访客',
    emergency_access BOOLEAN DEFAULT FALSE COMMENT '是否为紧急通道',
    monitoring_enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用监控',
    alert_config TEXT COMMENT '告警配置(JSON)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建用户ID',
    update_user_id BIGINT COMMENT '更新用户ID',
    deleted_flag BOOLEAN DEFAULT FALSE COMMENT '删除标记',
    version INT DEFAULT 1 COMMENT '版本号',

    UNIQUE KEY uk_area_id (area_id),
    KEY idx_access_level (access_level),
    KEY idx_access_enabled (access_enabled),
    KEY idx_deleted_flag (deleted_flag),
    KEY idx_area_id (area_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门禁区域扩展表';

-- 3. 数据迁移步骤1：将现有门禁区域数据迁移到基础区域表
-- -------------------------------------------------
INSERT IGNORE INTO t_area (
    area_id, area_code, area_name, area_type, parent_id, path, level,
    sort_order, status, longitude, latitude, area_size, capacity,
    description, map_image, remark,
    create_time, update_time, create_user_id, update_user_id, deleted_flag, version
)
SELECT
    area_id,
    area_code,
    area_name,
    area_type,
    parent_id,
    path,
    level,
    sort_order,
    status,
    CASE
        WHEN longitude IS NOT NULL THEN CAST(longitude AS DECIMAL(10,6))
        ELSE NULL
    END as longitude,
    CASE
        WHEN latitude IS NOT NULL THEN CAST(latitude AS DECIMAL(10,6))
        ELSE NULL
    END as latitude,
    CASE
        WHEN area IS NOT NULL AND area > 0 THEN CAST(area AS DECIMAL(12,2))
        ELSE NULL
    END as area_size,
    capacity,
    description,
    map_image,
    remark,
    create_time,
    update_time,
    create_user_id,
    update_user_id,
    deleted_flag,
    version
FROM t_access_area
WHERE deleted_flag = 0;

-- 4. 数据迁移步骤2：将门禁特有数据迁移到扩展表
-- -------------------------------------------------
INSERT INTO t_access_area_ext (
    area_id, access_enabled, access_level, access_mode, special_auth_required,
    valid_time_start, valid_time_end, valid_weekdays,
    guard_required, visitor_allowed, emergency_access, monitoring_enabled,
    create_time, update_time, create_user_id, update_user_id
)
SELECT
    area_id,
    COALESCE(access_enabled, 1) as access_enabled,
    COALESCE(access_level, 1) as access_level,
    COALESCE(
        CASE
            WHEN access_mode IS NOT NULL AND access_mode != '' THEN access_mode
            ELSE '卡'
        END,
        '卡'
    ) as access_mode,
    COALESCE(special_auth_required, 0) as special_auth_required,
    valid_time_start,
    valid_time_end,
    valid_weekdays,
    CASE
        WHEN COALESCE(special_auth_required, 0) = 1 THEN TRUE
        ELSE FALSE
    END as guard_required,
    TRUE as visitor_allowed,  -- 默认允许访客
    FALSE as emergency_access,
    TRUE as monitoring_enabled,
    create_time,
    update_time,
    create_user_id,
    update_user_id
FROM t_access_area
WHERE deleted_flag = 0
ON DUPLICATE KEY UPDATE
    access_enabled = VALUES(access_enabled),
    access_level = VALUES(access_level),
    access_mode = VALUES(access_mode),
    special_auth_required = VALUES(special_auth_required),
    valid_time_start = VALUES(valid_time_start),
    valid_time_end = VALUES(valid_time_end),
    valid_weekdays = VALUES(valid_weekdays),
    guard_required = VALUES(guard_required),
    update_time = VALUES(update_time),
    update_user_id = VALUES(update_user_id);

-- 5. 数据一致性验证
-- -------------------------------------------------
SELECT '=== 数据迁移一致性验证 ===' as info;

-- 验证基础区域表数据
SELECT
    '基础区域表' as table_name,
    COUNT(*) as total_count,
    COUNT(CASE WHEN deleted_flag = 0 THEN 1 END) as active_count
FROM t_area;

-- 验证扩展表数据
SELECT
    '门禁区域扩展表' as table_name,
    COUNT(*) as total_count,
    COUNT(CASE WHEN deleted_flag = 0 THEN 1 END) as active_count
FROM t_access_area_ext;

-- 验证数据完整性
SELECT
    '数据完整性检查' as check_type,
    COUNT(a.area_id) as total_areas,
    COUNT(e.ext_id) as total_extensions,
    (COUNT(a.area_id) - COUNT(e.ext_id)) as missing_extensions
FROM t_area a
LEFT JOIN t_access_area_ext e ON a.area_id = e.area_id
WHERE a.deleted_flag = 0;

-- 6. 性能优化索引
-- -------------------------------------------------
-- 为联合查询添加复合索引
ALTER TABLE t_access_area_ext ADD INDEX idx_area_access_enabled (area_id, access_enabled);
ALTER TABLE t_access_area_ext ADD INDEX idx_area_access_level (area_id, access_level);
ALTER TABLE t_area ADD INDEX idx_parent_deleted_status (parent_id, deleted_flag, status);

-- 7. 数据完整性约束
-- -------------------------------------------------
-- 添加外键约束确保数据一致性
-- 注意：如果已有外键约束，请先删除再添加

-- 删除可能存在的外键约束
-- ALTER TABLE t_access_area_ext DROP FOREIGN KEY IF EXISTS fk_access_area_ext_area;

-- 添加外键约束
ALTER TABLE t_access_area_ext
ADD CONSTRAINT fk_access_area_ext_area
FOREIGN KEY (area_id) REFERENCES t_area(area_id)
ON DELETE CASCADE
ON UPDATE CASCADE;

-- 8. 创建数据一致性检查触发器
-- -------------------------------------------------
DELIMITER //

-- 检查插入数据的一致性
CREATE TRIGGER check_access_area_ext_insert
BEFORE INSERT ON t_access_area_ext
FOR EACH ROW
BEGIN
    DECLARE area_exists INT;
    SELECT COUNT(*) INTO area_exists
    FROM t_area
    WHERE area_id = NEW.area_id AND deleted_flag = 0;

    IF area_exists = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = '对应的基础区域不存在或已删除';
    END IF;
END//

-- 检查更新数据的一致性
CREATE TRIGGER check_access_area_ext_update
BEFORE UPDATE ON t_access_area_ext
FOR EACH ROW
BEGIN
    DECLARE area_exists INT;
    SELECT COUNT(*) INTO area_exists
    FROM t_area
    WHERE area_id = NEW.area_id AND deleted_flag = 0;

    IF area_exists = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = '对应的基础区域不存在或已删除';
    END IF;
END//

DELIMITER ;

-- 9. 迁移后清理建议（谨慎执行！）
-- =====================================================
-- 以下为可选的清理步骤，请在充分测试后执行
--
-- -- 重命名旧表作为备份
-- ALTER TABLE t_access_area RENAME TO t_access_area_old;
--
-- -- 创建视图以保持向后兼容（可选）
-- CREATE OR REPLACE VIEW v_access_area AS
-- SELECT
--     a.*, e.access_enabled, e.access_level, e.access_mode, e.special_auth_required,
--     e.valid_time_start, e.valid_time_end, e.valid_weekdays, e.device_count,
--     e.guard_required, e.time_restrictions, e.visitor_allowed, e.emergency_access,
--     e.monitoring_enabled, e.alert_config
-- FROM t_area a
-- LEFT JOIN t_access_area_ext e ON a.area_id = e.area_id;

-- =====================================================
-- 迁移完成！
-- =====================================================

SELECT '=== AccessAreaEntity 架构修正迁移完成 ===' as info;
SELECT '迁移时间：' AS info, NOW() AS migration_time;
SELECT '注意事项：' AS info,
       '1. 请验证应用程序功能正常' AS note1,
       '2. 请确认数据完整性' AS note2,
       '3. 如有问题，可从 t_access_area_backup 表恢复' AS note3;