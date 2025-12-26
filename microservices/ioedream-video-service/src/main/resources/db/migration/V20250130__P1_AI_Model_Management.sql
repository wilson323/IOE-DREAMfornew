-- ============================================
-- P1阶段：AI模型管理模块 - 数据库表创建
-- 创建日期: 2025-01-30
-- 说明: 创建AI模型表和设备模型同步表
-- ============================================

-- 1. AI模型表
CREATE TABLE IF NOT EXISTS `t_video_ai_model` (
    `model_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '模型ID',
    `model_name` VARCHAR(200) NOT NULL COMMENT '模型名称',
    `model_version` VARCHAR(50) NOT NULL COMMENT '模型版本号（语义化版本）',
    `model_type` VARCHAR(50) NOT NULL COMMENT '模型类型（FACE_DETECTION/FALL_DETECTION/BEHAVIOR_DETECTION）',
    `file_path` VARCHAR(500) NULL COMMENT 'MinIO文件路径',
    `file_size` BIGINT NULL COMMENT '文件大小（字节）',
    `file_md5` VARCHAR(32) NULL COMMENT '文件MD5值',
    `model_status` TINYINT NOT NULL DEFAULT 0 COMMENT '模型状态（0-草稿 1-已发布 2-已废弃）',
    `supported_events` VARCHAR(500) NULL COMMENT '支持的事件类型（JSON数组）',
    `model_metadata` TEXT NULL COMMENT '模型元数据（JSON格式）',
    `accuracy_rate` DECIMAL(5,4) NULL COMMENT '模型准确率',
    `publish_time` DATETIME NULL COMMENT '发布时间',
    `published_by` BIGINT NULL COMMENT '发布人ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记（0-未删除 1-已删除）',
    PRIMARY KEY (`model_id`),
    UNIQUE KEY `uk_model_version` (`model_name`, `model_version`, `deleted_flag`),
    KEY `idx_model_type` (`model_type`),
    KEY `idx_model_status` (`model_status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI模型表';

-- 2. 设备模型同步表
CREATE TABLE IF NOT EXISTS `t_video_device_model_sync` (
    `sync_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '同步ID',
    `model_id` BIGINT NOT NULL COMMENT '模型ID',
    `device_id` VARCHAR(100) NOT NULL COMMENT '设备ID',
    `sync_status` TINYINT NOT NULL DEFAULT 0 COMMENT '同步状态（0-待同步 1-同步中 2-成功 3-失败）',
    `sync_progress` INT NULL DEFAULT 0 COMMENT '同步进度（0-100）',
    `sync_start_time` DATETIME NULL COMMENT '同步开始时间',
    `sync_end_time` DATETIME NULL COMMENT '同步结束时间',
    `error_message` VARCHAR(1000) NULL COMMENT '错误信息',
    `retry_count` INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`sync_id`),
    UNIQUE KEY `uk_model_device` (`model_id`, `device_id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_sync_status` (`sync_status`),
    KEY `idx_create_time` (`create_time`),
    CONSTRAINT `fk_sync_model` FOREIGN KEY (`model_id`) REFERENCES `t_video_ai_model` (`model_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备模型同步表';

-- 3. 创建索引优化查询性能
CREATE INDEX `idx_model_type_status` ON `t_video_ai_model` (`model_type`, `model_status`);
CREATE INDEX `idx_device_status_time` ON `t_video_device_model_sync` (`device_id`, `sync_status`, `create_time`);

-- 4. 插入初始测试数据（可选）
INSERT INTO `t_video_ai_model`
    (`model_name`, `model_version`, `model_type`, `model_status`, `supported_events`, `accuracy_rate`)
VALUES
    ('跌倒检测模型', '1.0.0', 'FALL_DETECTION', 1, '["FALL_DETECTION"]', 0.9200),
    ('人脸检测模型', '1.5.0', 'FACE_DETECTION', 1, '["FACE_DETECTION"]', 0.9500),
    ('徘徊检测模型', '1.0.0', 'LOITERING_DETECTION', 0, '["LOITERING_DETECTION"]', 0.8800);
