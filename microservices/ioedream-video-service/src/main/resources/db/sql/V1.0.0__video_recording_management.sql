-- =============================================
-- IOE-DREAM 视频录像管理数据库初始化脚本
-- 版本: v1.0.0
-- 创建时间: 2025-01-30
-- 描述: 视频录像计划和录像任务表
-- =============================================

-- ---------------------------------------------
-- 表1: 视频录像计划表
-- ---------------------------------------------
DROP TABLE IF EXISTS `t_video_recording_plan`;

CREATE TABLE `t_video_recording_plan` (
    `plan_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '计划ID',
    `plan_name` VARCHAR(100) NOT NULL COMMENT '计划名称',
    `plan_type` TINYINT NOT NULL DEFAULT 1 COMMENT '计划类型: 1-定时录像 2-事件录像 3-手动录像',
    `device_id` VARCHAR(50) NOT NULL COMMENT '设备ID',
    `channel_id` INT DEFAULT 1 COMMENT '通道ID',
    `recording_type` TINYINT NOT NULL DEFAULT 1 COMMENT '录像类型: 1-全天录像 2-定时录像 3-事件触发录像',
    `quality` TINYINT NOT NULL DEFAULT 3 COMMENT '录像质量: 1-低质量 2-中等质量 3-高质量 4-超清质量',
    `start_time` DATETIME COMMENT '开始时间',
    `end_time` DATETIME COMMENT '结束时间',
    `weekdays` VARCHAR(20) COMMENT '星期设置: 1,2,3,4,5,6,7 逗号分隔',
    `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用: 0-禁用 1-启用',
    `priority` INT NOT NULL DEFAULT 1 COMMENT '优先级: 1-最高',
    `pre_record_seconds` INT DEFAULT 0 COMMENT '前置录像时长(秒)',
    `post_record_seconds` INT DEFAULT 0 COMMENT '后置录像时长(秒)',
    `event_types` VARCHAR(500) COMMENT '事件类型(JSON数组): ["MOTION_DETECTED", "FACE_DETECTED"]',
    `storage_location` VARCHAR(200) COMMENT '存储位置',
    `max_duration_minutes` INT COMMENT '最大录像时长(分钟)',
    `loop_recording` TINYINT DEFAULT 0 COMMENT '循环录像: 0-否 1-是',
    `detection_area` TEXT COMMENT '检测区域(JSON格式)',
    `remarks` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记: 0-未删除 1-已删除',
    PRIMARY KEY (`plan_id`),
    UNIQUE KEY `uk_plan_name` (`plan_name`, `deleted_flag`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_enabled` (`enabled`),
    KEY `idx_plan_type` (`plan_type`),
    KEY `idx_start_time` (`start_time`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频录像计划表';

-- ---------------------------------------------
-- 表2: 视频录像任务表
-- ---------------------------------------------
DROP TABLE IF EXISTS `t_video_recording_task`;

CREATE TABLE `t_video_recording_task` (
    `task_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '任务ID',
    `plan_id` BIGINT COMMENT '计划ID',
    `device_id` VARCHAR(50) NOT NULL COMMENT '设备ID',
    `channel_id` INT DEFAULT 1 COMMENT '通道ID',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '任务状态: 0-待执行 1-录像中 2-已停止 3-失败 4-已完成',
    `file_path` VARCHAR(500) COMMENT '录像文件路径',
    `file_size` BIGINT COMMENT '文件大小(字节)',
    `start_time` DATETIME COMMENT '录像开始时间',
    `end_time` DATETIME COMMENT '录像结束时间',
    `duration_seconds` INT COMMENT '录像时长(秒)',
    `trigger_type` TINYINT NOT NULL DEFAULT 1 COMMENT '触发类型: 1-定时 2-手动 3-事件',
    `event_trigger_type` VARCHAR(50) COMMENT '触发事件类型',
    `quality` TINYINT NOT NULL DEFAULT 3 COMMENT '录像质量: 1-低质量 2-中等质量 3-高质量 4-超清质量',
    `error_message` TEXT COMMENT '错误信息',
    `retry_count` INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    `max_retry_count` INT NOT NULL DEFAULT 5 COMMENT '最大重试次数',
    `completed_time` DATETIME COMMENT '完成时间',
    `remarks` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记: 0-未删除 1-已删除',
    PRIMARY KEY (`task_id`),
    KEY `idx_plan_id` (`plan_id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_status` (`status`),
    KEY `idx_trigger_type` (`trigger_type`),
    KEY `idx_start_time` (`start_time`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_device_status` (`device_id`, `status`),
    CONSTRAINT `fk_task_plan` FOREIGN KEY (`plan_id`) REFERENCES `t_video_recording_plan` (`plan_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频录像任务表';

-- ---------------------------------------------
-- 初始化数据
-- ---------------------------------------------

-- 插入示例录像计划
INSERT INTO `t_video_recording_plan` (
    `plan_name`, `plan_type`, `device_id`, `channel_id`, `recording_type`, `quality`,
    `start_time`, `end_time`, `weekdays`, `enabled`, `priority`, `storage_location`,
    `max_duration_minutes`, `loop_recording`, `remarks`
) VALUES
('主入口全天录像', 1, 'CAM001', 1, 1, 3,
 '2025-01-30 00:00:00', '2025-12-31 23:59:59', '1,2,3,4,5', 1, 1,
 '/recordings/cam001/', 480, 1, '主入口全天候录像'),
('会议室定时录像', 1, 'CAM002', 1, 2, 3,
 '2025-01-30 08:00:00', '2025-12-31 18:00:00', '1,2,3,4,5', 1, 2,
 '/recordings/cam002/', 120, 0, '会议室工作时间录像'),
('移动侦测录像', 2, 'CAM003', 1, 3, 2,
 '2025-01-30 00:00:00', '2025-12-31 23:59:59', '1,2,3,4,5,6,7', 1, 3,
 '/recordings/cam003/events/', 60, 1, '移动侦测事件录像');

-- ---------------------------------------------
-- 索引说明
-- ---------------------------------------------
-- 1. 计划表索引
--    - uk_plan_name: 计划名称唯一索引（包含软删除标记）
--    - idx_device_id: 设备ID索引，用于查询设备的计划
--    - idx_enabled: 启用状态索引，用于查询启用的计划
--    - idx_plan_type: 计划类型索引，用于查询特定类型的计划
--    - idx_start_time: 开始时间索引，用于时间范围查询
--
-- 2. 任务表索引
--    - idx_plan_id: 计划ID索引，用于查询计划的任务
--    - idx_device_id: 设备ID索引，用于查询设备的任务
--    - idx_status: 任务状态索引，用于查询特定状态的任务
--    - idx_trigger_type: 触发类型索引，用于查询特定触发类型的任务
--    - idx_device_status: 设备+状态复合索引，用于查询设备运行中的任务
--
-- 3. 外键约束
--    - fk_task_plan: 任务表的plan_id外键关联计划表，计划删除时设置为NULL

-- ---------------------------------------------
-- 使用说明
-- ---------------------------------------------
-- 1. 录像计划类型 (plan_type)
--    1: 定时录像 - 按时间表自动录像
--    2: 事件录像 - 检测到事件时录像（移动侦测、人脸识别等）
--    3: 手动录像 - 用户手动控制录像
--
-- 2. 录像类型 (recording_type)
--    1: 全天录像 - 24小时连续录像
--    2: 定时录像 - 指定时间段录像
--    3: 事件触发录像 - 仅在事件触发时录像
--
-- 3. 录像质量 (quality)
--    1: 低质量 (360p, 500Kbps)
--    2: 中等质量 (720p, 1.5Mbps)
--    3: 高质量 (1080p, 3Mbps)
--    4: 超清质量 (4K, 8Mbps)
--
-- 4. 任务状态 (status)
--    0: 待执行 - 任务已创建，等待执行
--    1: 录像中 - 正在录像
--    2: 已停止 - 录像被手动停止
--    3: 失败 - 录像失败，可以重试
--    4: 已完成 - 录像正常完成
--
-- 5. 触发类型 (trigger_type)
--    1: 定时触发 - 按计划时间触发
--    2: 手动触发 - 用户手动触发
--    3: 事件触发 - AI事件触发
