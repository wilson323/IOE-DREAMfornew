-- =====================================================
-- 考勤模块扩展表 - 区域配置扩展表
-- 基于消费模块模式的考勤系统功能完善
-- 创建时间: 2025-11-25
-- 创建目的: 扩展基础AreaEntity的考勤功能，通过独立扩展表添加考勤配置
-- =====================================================

-- 创建考勤区域配置扩展表
CREATE TABLE t_attendance_area_config (
    -- 主键配置
    config_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',

    -- 关联基础区域表
    area_id BIGINT NOT NULL COMMENT '区域ID（关联t_area.area_id）',

    -- 考勤要求配置
    punch_in_required TINYINT DEFAULT 1 COMMENT '上班打卡要求（0-不需要，1-需要）',
    punch_out_required TINYINT DEFAULT 1 COMMENT '下班打卡要求（0-不需要，1-需要）',

    -- 验证方式配置
    gps_validation_enabled TINYINT DEFAULT 0 COMMENT 'GPS验证开关（0-关闭，1-开启）',
    photo_required TINYINT DEFAULT 0 COMMENT '拍照验证开关（0-关闭，1-开启）',
    geofence_enabled TINYINT DEFAULT 0 COMMENT '电子围栏开关（0-关闭，1-开启）',
    geofence_radius INT DEFAULT 100 COMMENT '围栏半径（米）',

    -- 考勤时间配置
    work_start_time TIME COMMENT '标准上班时间',
    work_end_time TIME COMMENT '标准下班时间',
    lunch_break_enabled TINYINT DEFAULT 0 COMMENT '午休启用（0-关闭，1-开启）',
    lunch_start_time TIME COMMENT '午休开始时间',
    lunch_end_time TIME COMMENT '午休结束时间',
    flexible_time_enabled TINYINT DEFAULT 0 COMMENT '弹性时间启用（0-关闭，1-开启）',
    flexible_grace_period INT DEFAULT 0 COMMENT '弹性宽限时间（分钟）',

    -- 业务配置（JSON格式存储）
    attendance_point_ids TEXT COMMENT '考勤点ID列表（JSON格式）',
    work_time_config TEXT COMMENT '工作时间配置（JSON格式）',
    attendance_rule_ids TEXT COMMENT '考勤规则ID列表（JSON格式）',
    device_config TEXT COMMENT '设备配置（JSON格式）',

    -- 高级配置
    overtime_auto_enabled TINYINT DEFAULT 0 COMMENT '加班自动计算（0-关闭，1-开启）',
    holiday_work_enabled TINYINT DEFAULT 0 COMMENT '节假日上班处理（0-关闭，1-开启）',
    weekend_work_enabled TINYINT DEFAULT 0 COMMENT '周末上班处理（0-关闭，1-开启）',

    -- 基础审计字段（继承BaseEntity）
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记（0-正常，1-删除）',
    version INT DEFAULT 1 COMMENT '版本号',

    -- 外键约束
    FOREIGN KEY (area_id) REFERENCES t_area(area_id) ON DELETE CASCADE,

    -- 索引优化
    INDEX idx_area_id (area_id),
    INDEX idx_deleted_flag (deleted_flag),
    INDEX idx_create_time (create_time),
    INDEX idx_area_config (area_id, deleted_flag),
    INDEX idx_gps_enabled (gps_validation_enabled),
    INDEX idx_geofence_enabled (geofence_enabled)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤区域配置扩展表';

-- 创建默认配置视图（简化查询）
CREATE VIEW v_attendance_area_full AS
SELECT
    a.area_id,
    a.area_code,
    a.area_name,
    a.area_type,
    a.parent_id,
    a.path,
    a.level,
    a.sort_order,
    a.location_desc,
    a.enable_flag,
    c.config_id,
    c.punch_in_required,
    c.punch_out_required,
    c.gps_validation_enabled,
    c.photo_required,
    c.geofence_enabled,
    c.geofence_radius,
    c.work_start_time,
    c.work_end_time,
    c.lunch_break_enabled,
    c.overtime_auto_enabled,
    c.create_time as config_create_time,
    c.update_time as config_update_time
FROM t_area a
LEFT JOIN t_attendance_area_config c ON a.area_id = c.area_id AND c.deleted_flag = 0
WHERE a.deleted_flag = 0;

-- 插入示例数据（如果需要）
-- INSERT INTO t_attendance_area_config (
--     area_id, punch_in_required, punch_out_required,
--     work_start_time, work_end_time, create_user_id
-- )
-- SELECT area_id, 1, 1, '09:00:00', '18:00:00', 1
-- FROM t_area
-- WHERE area_type = 'OFFICE' AND deleted_flag = 0
-- LIMIT 10;