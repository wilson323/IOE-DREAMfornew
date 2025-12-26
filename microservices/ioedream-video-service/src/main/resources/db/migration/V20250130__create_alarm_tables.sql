-- ============================================================
-- 视频服务 - 告警规则和告警记录表
-- 边缘计算架构：设备 AI 事件触发告警规则匹配和告警创建
-- ============================================================

-- 告警规则表
CREATE TABLE IF NOT EXISTS `t_video_alarm_rule` (
    `rule_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '规则ID',
    `rule_name` VARCHAR(128) NOT NULL COMMENT '规则名称',
    `rule_type` VARCHAR(64) NOT NULL COMMENT '规则类型: FALL_DETECTION-跌倒检测, LOITERING_DETECTION-徘徊检测, GATHERING_DETECTION-聚集检测, FIGHTING_DETECTION-打架检测, INTRUSION_DETECTION-入侵检测',
    `event_type` VARCHAR(64) NOT NULL COMMENT '事件类型',
    `confidence_threshold` DECIMAL(5,4) NOT NULL DEFAULT 0.8000 COMMENT '置信度阈值: 0.0000-1.0000',
    `area_id` BIGINT NULL COMMENT '区域ID（可选）',
    `device_id` VARCHAR(64) NULL COMMENT '设备ID（可选，空表示所有设备）',
    `rule_status` TINYINT NOT NULL DEFAULT 1 COMMENT '规则状态: 1-启用, 0-禁用',
    `effective_start_time` TIME NULL COMMENT '生效时间开始',
    `effective_end_time` TIME NULL COMMENT '生效时间结束',
    `alarm_level` TINYINT NOT NULL DEFAULT 2 COMMENT '告警级别: 1-低, 2-中, 3-高, 4-紧急',
    `push_notification` TINYINT NOT NULL DEFAULT 1 COMMENT '是否推送通知: 1-是, 0-否',
    `notification_methods` VARCHAR(256) NULL COMMENT '通知方式(JSON): {\"email\":true,\"sms\":false,\"websocket\":true}',
    `alarm_message_template` VARCHAR(512) NULL COMMENT '告警消息模板',
    `priority` INT NOT NULL DEFAULT 0 COMMENT '规则优先级（数字越大优先级越高）',
    `extended_config` TEXT NULL COMMENT '扩展配置(JSON格式)',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记: 0-未删除, 1-已删除',
    INDEX `idx_event_type` (`event_type`),
    INDEX `idx_rule_status` (`rule_status`),
    INDEX `idx_device_id` (`device_id`),
    INDEX `idx_area_id` (`area_id`),
    INDEX `idx_priority` (`priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='告警规则表（边缘计算架构）';

-- 告警记录表
CREATE TABLE IF NOT EXISTS `t_video_alarm_record` (
    `alarm_id` VARCHAR(64) PRIMARY KEY COMMENT '告警ID',
    `rule_id` BIGINT NOT NULL COMMENT '规则ID',
    `rule_name` VARCHAR(128) NOT NULL COMMENT '规则名称',
    `event_id` VARCHAR(64) NOT NULL COMMENT '事件ID',
    `device_id` VARCHAR(64) NOT NULL COMMENT '设备ID',
    `device_code` VARCHAR(128) NOT NULL COMMENT '设备编码',
    `event_type` VARCHAR(64) NOT NULL COMMENT '事件类型',
    `alarm_level` TINYINT NOT NULL COMMENT '告警级别: 1-低, 2-中, 3-高, 4-紧急',
    `alarm_status` TINYINT NOT NULL DEFAULT 0 COMMENT '告警状态: 0-待处理, 1-处理中, 2-已处理, 3-已忽略',
    `confidence` DECIMAL(5,4) NOT NULL COMMENT '置信度',
    `bbox` VARCHAR(256) NULL COMMENT '边界框(JSON格式)',
    `snapshot_url` VARCHAR(512) NULL COMMENT '抓拍图片URL',
    `alarm_message` VARCHAR(512) NOT NULL COMMENT '告警消息',
    `alarm_time` DATETIME NOT NULL COMMENT '告警时间',
    `handler_id` BIGINT NULL COMMENT '处理人ID',
    `handler_name` VARCHAR(64) NULL COMMENT '处理人姓名',
    `handle_time` DATETIME NULL COMMENT '处理时间',
    `handle_remark` VARCHAR(512) NULL COMMENT '处理备注',
    `notification_sent` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已推送通知: 1-是, 0-否',
    `notification_time` DATETIME NULL COMMENT '通知推送时间',
    `extended_attributes` TEXT NULL COMMENT '扩展属性(JSON格式)',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记: 0-未删除, 1-已删除',
    INDEX `idx_device_time` (`device_id`, `alarm_time`),
    INDEX `idx_event_type_time` (`event_type`, `alarm_time`),
    INDEX `idx_alarm_status` (`alarm_status`),
    INDEX `idx_alarm_level` (`alarm_level`),
    INDEX `idx_rule_id` (`rule_id`),
    INDEX `idx_event_id` (`event_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='告警记录表（边缘计算架构）';

-- ============================================================
-- 示例数据（可选）
-- ============================================================

-- 示例告警规则：跌倒检测（高优先级）
-- INSERT INTO `t_video_alarm_rule` VALUES (
--     NULL, '跌倒检测-高置信度', 'FALL_DETECTION', 'FALL_DETECTION', 0.9000,
--     NULL, NULL, 1,
--     '00:00:00', '23:59:59',
--     3, 1,
--     '{"email":false,"sms":false,"websocket":true}',
--     '检测到跌倒，置信度{confidence}，设备{deviceName}，请立即处理！',
--     100, NULL,
--     NOW(), NOW(), 0
-- );

-- 示例告警规则：徘徊检测（中优先级）
-- INSERT INTO `t_video_alarm_rule` VALUES (
--     NULL, '徘徊检测-中置信度', 'LOITERING_DETECTION', 'LOITERING_DETECTION', 0.7500,
--     NULL, NULL, 1,
--     '00:00:00', '23:59:59',
--     2, 1,
--     '{"email":false,"sms":false,"websocket":true}',
--     '检测到徘徊行为，置信度{confidence}，设备{deviceName}',
--     50, NULL,
--     NOW(), NOW(), 0
-- );
