-- ============================================================
-- 视频服务 - 设备AI事件表
-- 边缘计算架构：设备端AI分析，服务器接收结构化事件
-- ============================================================

CREATE TABLE IF NOT EXISTS `t_video_device_ai_event` (
    `event_id` VARCHAR(64) PRIMARY KEY COMMENT '事件ID',
    `device_id` VARCHAR(64) NOT NULL COMMENT '设备ID',
    `device_code` VARCHAR(128) NOT NULL COMMENT '设备编码',
    `event_type` VARCHAR(64) NOT NULL COMMENT '事件类型: FALL_DETECTION-跌倒检测, LOITERING_DETECTION-徘徊检测, GATHERING_DETECTION-聚集检测, FIGHTING_DETECTION-打架检测, RUNNING_DETECTION-奔跑检测, CLIMBING_DETECTION-攀爬检测, FACE_DETECTION-人脸检测, INTRUSION_DETECTION-入侵检测',
    `confidence` DECIMAL(5,4) NOT NULL COMMENT '置信度: 0.0000-1.0000',
    `bbox` VARCHAR(256) NULL COMMENT '边界框(JSON格式): {"x":100,"y":150,"width":200,"height":300}',
    `snapshot` LONGBLOB NULL COMMENT '抓拍图片',
    `event_time` DATETIME NOT NULL COMMENT '事件时间',
    `extended_attributes` TEXT NULL COMMENT '扩展属性(JSON格式)',
    `event_status` TINYINT NOT NULL DEFAULT 0 COMMENT '事件状态: 0-待处理, 1-已处理, 2-已忽略',
    `process_time` DATETIME NULL COMMENT '处理时间',
    `alarm_id` VARCHAR(64) NULL COMMENT '告警ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记: 0-未删除, 1-已删除',
    INDEX `idx_device_time` (`device_id`, `event_time`),
    INDEX `idx_event_type_time` (`event_type`, `event_time`),
    INDEX `idx_event_status` (`event_status`),
    INDEX `idx_device_code` (`device_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备AI事件表（边缘计算架构）';

-- ============================================================
-- 示例数据（可选）
-- ============================================================

-- INSERT INTO `t_video_device_ai_event` VALUES (
--     'evt_20250130_001', 'CAM001', 'camera_001', 'FALL_DETECTION', 0.9500,
--     '{"x":100,"y":150,"width":200,"height":300}',
--     NULL, '2025-01-30 10:30:00', '{"duration":5}',
--     0, NULL, NULL, NOW(), NOW(), 0
-- );
