-- =====================================================
-- IOE-DREAM 门禁服务 - 告警系统表创建脚本
-- 版本: V9
-- 描述: 创建设备告警、告警规则、通知记录三张表
-- 作者: IOE-DREAM Team
-- 日期: 2025-01-30
-- =====================================================

-- ----------------------------
-- 表1: 设备告警表
-- ----------------------------
DROP TABLE IF EXISTS `t_device_alert`;

CREATE TABLE `t_device_alert` (
    `alert_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '告警ID',
    `device_id` BIGINT NOT NULL COMMENT '设备ID',
    `device_code` VARCHAR(50) NOT NULL COMMENT '设备编码',
    `device_name` VARCHAR(100) NOT NULL COMMENT '设备名称',
    `device_type` TINYINT DEFAULT 1 COMMENT '设备类型 1-门禁 2-考勤 3-消费 4-视频 5-访客',

    -- 告警信息
    `alert_type` VARCHAR(50) NOT NULL COMMENT '告警类型 DEVICE_OFFLINE-设备离线 DEVICE_FAULT-设备故障 TEMP_HIGH-温度过高 NETWORK_ERROR-网络异常等',
    `alert_level` TINYINT NOT NULL DEFAULT 2 COMMENT '告警级别 1-低 2-中 3-高 4-紧急',
    `alert_title` VARCHAR(200) NOT NULL COMMENT '告警标题',
    `alert_message` TEXT COMMENT '告警详细内容',
    `alert_data` JSON COMMENT '告警相关数据（JSON格式）',

    -- 状态管理
    `alert_status` TINYINT NOT NULL DEFAULT 0 COMMENT '告警状态 0-未确认 1-已确认 2-已处理 3-已忽略',
    `confirmed_by` BIGINT COMMENT '确认人ID',
    `confirmed_time` DATETIME COMMENT '确认时间',
    `confirmed_remark` VARCHAR(500) COMMENT '确认备注',
    `handled_by` BIGINT COMMENT '处理人ID',
    `handled_time` DATETIME COMMENT '处理时间',
    `handled_result` VARCHAR(500) COMMENT '处理结果',

    -- 时间记录
    `alert_occurred_time` DATETIME NOT NULL COMMENT '告警发生时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',

    PRIMARY KEY (`alert_id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_device_code` (`device_code`),
    KEY `idx_alert_type` (`alert_type`),
    KEY `idx_alert_level` (`alert_level`),
    KEY `idx_alert_status` (`alert_status`),
    KEY `idx_alert_occurred_time` (`alert_occurred_time`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_device_level_status` (`device_id`, `alert_level`, `alert_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备告警表';

-- ----------------------------
-- 表2: 告警规则表
-- ----------------------------
DROP TABLE IF EXISTS `t_alert_rule`;

CREATE TABLE `t_alert_rule` (
    `rule_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '规则ID',
    `rule_name` VARCHAR(100) NOT NULL COMMENT '规则名称',
    `rule_code` VARCHAR(50) NOT NULL COMMENT '规则编码',
    `rule_type` VARCHAR(50) NOT NULL COMMENT '规则类型 DEVICE_OFFLINE-设备离线 DEVICE_FAULT-设备故障 TEMP_HIGH-温度异常 NETWORK_ERROR-网络异常',

    -- 触发条件
    `condition_type` TINYINT NOT NULL COMMENT '条件类型 1-简单条件 2-Aviator表达式 3-脚本条件',
    `condition_expression` TEXT COMMENT '触发条件表达式（Aviator格式）',
    `condition_config` JSON COMMENT '条件配置（JSON格式）',

    -- 告警配置
    `alert_level` TINYINT NOT NULL DEFAULT 2 COMMENT '告警级别 1-低 2-中 3-高 4-紧急',
    `alert_title_template` VARCHAR(200) COMMENT '告警标题模板',
    `alert_message_template` TEXT COMMENT '告警消息模板',

    -- 通知配置
    `notification_methods` VARCHAR(200) COMMENT '通知方式（逗号分隔） SMS-短信 EMAIL-邮件 PUSH-推送 WEBSOCKET-WebSocket',
    `notification_recipients` TEXT COMMENT '通知接收人列表（JSON数组）',
    `notification_template` TEXT COMMENT '通知内容模板',

    -- 高级配置
    `alert_aggregation_enabled` TINYINT DEFAULT 0 COMMENT '是否启用告警聚合 0-否 1-是',
    `aggregation_window_seconds` INT DEFAULT 300 COMMENT '聚合时间窗口（秒）',
    `alert_escalation_enabled` TINYINT DEFAULT 0 COMMENT '是否启用告警升级 0-否 1-是',
    `escalation_rules` JSON COMMENT '升级规则（JSON格式）',

    -- 状态管理
    `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '启用状态 0-禁用 1-启用',
    `priority` INT DEFAULT 0 COMMENT '优先级（数字越大优先级越高）',

    -- 审计字段
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',

    PRIMARY KEY (`rule_id`),
    UNIQUE KEY `uk_rule_code` (`rule_code`),
    KEY `idx_rule_type` (`rule_type`),
    KEY `idx_enabled` (`enabled`),
    KEY `idx_priority` (`priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='告警规则表';

-- ----------------------------
-- 表3: 告警通知记录表
-- ----------------------------
DROP TABLE IF EXISTS `t_alert_notification`;

CREATE TABLE `t_alert_notification` (
    `notification_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '通知ID',
    `alert_id` BIGINT NOT NULL COMMENT '告警ID',
    `rule_id` BIGINT COMMENT '规则ID',

    -- 通知信息
    `notification_method` VARCHAR(20) NOT NULL COMMENT '通知方式 SMS-短信 EMAIL-邮件 PUSH-推送 WEBSOCKET-WebSocket',
    `recipient_type` VARCHAR(20) COMMENT '接收人类型 USER-用户 ROLE-角色 DEPARTMENT-部门',
    `recipient_id` BIGINT COMMENT '接收人ID',
    `recipient_name` VARCHAR(100) COMMENT '接收人姓名',
    `recipient_contact` VARCHAR(200) COMMENT '接收人联系方式（手机号/邮箱/设备ID）',

    -- 通知内容
    `notification_title` VARCHAR(200) COMMENT '通知标题',
    `notification_content` TEXT COMMENT '通知内容',

    -- 发送状态
    `notification_status` TINYINT NOT NULL DEFAULT 0 COMMENT '通知状态 0-待发送 1-发送中 2-发送成功 3-发送失败',
    `retry_count` TINYINT DEFAULT 0 COMMENT '重试次数',
    `max_retry` TINYINT DEFAULT 3 COMMENT '最大重试次数',
    `error_message` VARCHAR(500) COMMENT '错误信息',
    `error_code` VARCHAR(50) COMMENT '错误代码',

    -- 时间记录
    `send_time` DATETIME COMMENT '发送时间',
    `received_time` DATETIME COMMENT '接收时间',
    `read_time` DATETIME COMMENT '阅读时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (`notification_id`),
    KEY `idx_alert_id` (`alert_id`),
    KEY `idx_rule_id` (`rule_id`),
    KEY `idx_notification_method` (`notification_method`),
    KEY `idx_notification_status` (`notification_status`),
    KEY `idx_recipient_id` (`recipient_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='告警通知记录表';

-- ----------------------------
-- 初始化默认告警规则
-- ----------------------------
INSERT INTO `t_alert_rule` (
    `rule_name`, `rule_code`, `rule_type`, `condition_type`, `condition_expression`,
    `alert_level`, `alert_title_template`, `notification_methods`, `enabled`, `priority`
) VALUES
('设备离线告警', 'DEVICE_OFFLINE', 'DEVICE_OFFLINE', 1, NULL,
 3, '设备离线告警：{deviceName}已离线超过{offlineMinutes}分钟', 'SMS,EMAIL,PUSH', 1, 100),

('设备故障告警', 'DEVICE_FAULT', 'DEVICE_FAULT', 1, NULL,
 4, '设备故障告警：{deviceName}发生故障', 'SMS,EMAIL,PUSH', 1, 90),

('设备温度异常', 'TEMP_HIGH', 'TEMP_HIGH', 2, 'temperature > threshold',
 2, '设备温度异常：{deviceName}温度为{temperature}℃', 'EMAIL,PUSH', 1, 70),

('网络异常告警', 'NETWORK_ERROR', 'NETWORK_ERROR', 1, NULL,
 2, '网络异常告警：{deviceName}网络连接异常', 'EMAIL,PUSH', 1, 60),

('设备存储空间不足', 'STORAGE_LOW', 'STORAGE_LOW', 2, 'storage_used_percent > 90',
 3, '存储空间不足：{deviceName}存储使用率达{storageUsedPercent}%', 'EMAIL,PUSH', 1, 50);
