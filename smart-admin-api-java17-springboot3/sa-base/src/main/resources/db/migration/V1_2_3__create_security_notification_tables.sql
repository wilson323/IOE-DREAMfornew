-- 创建安全通知相关表
-- Author: IOE-DREAM Team
-- Date: 2025-01-17
-- Description: 安全通知系统相关数据表

-- 1. 安全通知日志表
CREATE TABLE `t_security_notification_log` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `person_id` BIGINT(20) NOT NULL COMMENT '人员ID',
    `channel` VARCHAR(20) NOT NULL COMMENT '通知渠道（EMAIL/SMS/PUSH/WECHAT）',
    `notification_type` VARCHAR(50) NOT NULL COMMENT '通知类型',
    `content` TEXT NOT NULL COMMENT '通知内容',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '发送状态（SUCCESS/FAILED/SCHEDULED/CANCELLED）',
    `error_message` VARCHAR(500) DEFAULT NULL COMMENT '错误消息',
    `message_id` VARCHAR(100) DEFAULT NULL COMMENT '消息ID（第三方服务返回的消息ID）',
    `retry_count` INT(11) NOT NULL DEFAULT 0 COMMENT '重试次数',
    `retry_time` DATETIME DEFAULT NULL COMMENT '重试时间',
    `cancel_reason` VARCHAR(200) DEFAULT NULL COMMENT '取消原因',
    `extra_data` JSON DEFAULT NULL COMMENT '额外数据（JSON格式）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_person_id` (`person_id`),
    KEY `idx_channel` (`channel`),
    KEY `idx_notification_type` (`notification_type`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_message_id` (`message_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='安全通知日志表';

-- 2. 用户通知偏好表
CREATE TABLE `t_user_notification_preference` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `person_id` BIGINT(20) NOT NULL COMMENT '人员ID',
    `email_enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用邮件通知',
    `sms_enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用短信通知',
    `push_enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用推送通知',
    `wechat_enabled` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否启用微信通知',
    `notification_types` VARCHAR(500) DEFAULT NULL COMMENT '通知类型列表（逗号分隔）',
    `do_not_disturb_start_hour` INT(11) DEFAULT 22 COMMENT '免打扰时间段开始（小时，0-23）',
    `do_not_disturb_end_hour` INT(11) DEFAULT 8 COMMENT '免打扰时间段结束（小时，0-23）',
    `do_not_disturb_enabled` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否启用免打扰',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_person_id` (`person_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户通知偏好表';

-- 3. 系统通知配置表
CREATE TABLE `t_system_notification_config` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `email_enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用邮件服务',
    `sms_enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用短信服务',
    `push_enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用推送服务',
    `wechat_enabled` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否启用微信服务',
    `max_email_per_hour` INT(11) NOT NULL DEFAULT 10 COMMENT '每小时最大邮件发送数量',
    `max_sms_per_hour` INT(11) NOT NULL DEFAULT 20 COMMENT '每小时最大短信发送数量',
    `max_push_per_hour` INT(11) NOT NULL DEFAULT 50 COMMENT '每小时最大推送发送数量',
    `max_wechat_per_hour` INT(11) NOT NULL DEFAULT 30 COMMENT '每小时最大微信发送数量',
    `retry_attempts` INT(11) NOT NULL DEFAULT 3 COMMENT '重试尝试次数',
    `retry_interval` INT(11) NOT NULL DEFAULT 5 COMMENT '重试间隔（分钟）',
    `email_server_config` JSON DEFAULT NULL COMMENT '邮件服务器配置（JSON格式）',
    `sms_service_config` JSON DEFAULT NULL COMMENT '短信服务配置（JSON格式）',
    `push_service_config` JSON DEFAULT NULL COMMENT '推送服务配置（JSON格式）',
    `wechat_service_config` JSON DEFAULT NULL COMMENT '微信服务配置（JSON格式）',
    `rate_limit_enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用频率限制',
    `async_send_enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用异步发送',
    `async_thread_pool_size` INT(11) NOT NULL DEFAULT 5 COMMENT '异步线程池大小',
    `notification_templates` JSON DEFAULT NULL COMMENT '通知内容模板配置（JSON格式）',
    `logging_enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用通知日志',
    `log_retention_days` INT(11) NOT NULL DEFAULT 30 COMMENT '日志保留天数',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `updated_by` VARCHAR(50) DEFAULT NULL COMMENT '配置更新人',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统通知配置表';

-- 4. 设备推送注册表
CREATE TABLE `t_device_push_registration` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `person_id` BIGINT(20) NOT NULL COMMENT '人员ID',
    `device_token` VARCHAR(255) NOT NULL COMMENT '设备令牌',
    `device_platform` VARCHAR(20) NOT NULL COMMENT '设备平台（ANDROID/IOS）',
    `device_info` JSON DEFAULT NULL COMMENT '设备信息（JSON格式）',
    `app_version` VARCHAR(50) DEFAULT NULL COMMENT '应用版本',
    `os_version` VARCHAR(50) DEFAULT NULL COMMENT '操作系统版本',
    `is_active` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否活跃',
    `last_active_time` DATETIME DEFAULT NULL COMMENT '最后活跃时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_device_token` (`device_token`),
    KEY `idx_person_id` (`person_id`),
    KEY `idx_device_platform` (`device_platform`),
    KEY `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备推送注册表';

-- 5. 微信用户绑定表
CREATE TABLE `t_wechat_user_binding` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `person_id` BIGINT(20) NOT NULL COMMENT '人员ID',
    `openid` VARCHAR(100) NOT NULL COMMENT '微信OpenID',
    `unionid` VARCHAR(100) DEFAULT NULL COMMENT '微信UnionID',
    `nickname` VARCHAR(100) DEFAULT NULL COMMENT '微信昵称',
    `avatar_url` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `subscribe_status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '关注状态（0-未关注，1-已关注）',
    `subscribe_time` DATETIME DEFAULT NULL COMMENT '关注时间',
    `last_interaction_time` DATETIME DEFAULT NULL COMMENT '最后交互时间',
    `account_type` VARCHAR(20) NOT NULL DEFAULT 'SERVICE' COMMENT '账号类型（SERVICE/SUBSCRIPTION/ENTERPRISE）',
    `is_active` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否活跃',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_person_openid` (`person_id`, `openid`),
    KEY `idx_openid` (`openid`),
    KEY `idx_unionid` (`unionid`),
    KEY `idx_subscribe_status` (`subscribe_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='微信用户绑定表';

-- 6. 通知模板表
CREATE TABLE `t_notification_template` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `template_code` VARCHAR(50) NOT NULL COMMENT '模板代码',
    `template_name` VARCHAR(100) NOT NULL COMMENT '模板名称',
    `template_type` VARCHAR(20) NOT NULL COMMENT '模板类型（EMAIL/SMS/PUSH/WECHAT）',
    `subject_template` VARCHAR(200) DEFAULT NULL COMMENT '主题模板（邮件模板使用）',
    `content_template` TEXT NOT NULL COMMENT '内容模板',
    `template_params` JSON DEFAULT NULL COMMENT '模板参数定义（JSON格式）',
    `is_active` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '模板描述',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `updated_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_template_code` (`template_code`),
    KEY `idx_template_type` (`template_type`),
    KEY `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知模板表';

-- 插入默认系统配置
INSERT INTO `t_system_notification_config` (
    `email_enabled`, `sms_enabled`, `push_enabled`, `wechat_enabled`,
    `max_email_per_hour`, `max_sms_per_hour`, `max_push_per_hour`, `max_wechat_per_hour`,
    `retry_attempts`, `retry_interval`,
    `rate_limit_enabled`, `async_send_enabled`, `async_thread_pool_size`,
    `logging_enabled`, `log_retention_days`
) VALUES (
    1, 1, 1, 0,  -- 邮件、短信、推送启用，微信默认关闭
    10, 20, 50, 30,  -- 每小时发送限制
    3, 5,  -- 重试配置
    1, 1, 5,  -- 启用频率限制和异步发送
    1, 30  -- 启用日志，保留30天
);

-- 插入默认通知模板
INSERT INTO `t_notification_template` (`template_code`, `template_name`, `template_type`, `subject_template`, `content_template`, `description`) VALUES
('SECURITY_ALERT', '安全告警通知', 'EMAIL', '【智慧园区】安全告警', '尊敬的用户，检测到您的账户存在安全风险：\n\n风险类型：{{alertType}}\n风险详情：{{alertMessage}}\n发生时间：{{alertTime}}\n\n请立即登录系统查看详情并采取相应措施。如非本人操作，请联系客服。\n\n智慧园区安全团队', '安全事件告警邮件模板'),

('SECURITY_ALERT_SMS', '安全告警短信', 'SMS', NULL, '【智慧园区】安全提醒：{{alertType}}，{{alertMessage}}。请及时处理。', '安全事件告警短信模板'),

('SECURITY_ALERT_PUSH', '安全告警推送', 'PUSH', NULL, '安全提醒：检测到{{alertType}}，请立即查看', '安全事件告警推送模板'),

('ACCOUNT_STATUS_CHANGE', '账户状态变更通知', 'EMAIL', '【智慧园区】账户状态变更', '尊敬的用户，您的账户状态已发生变更：\n\n变更类型：{{changeType}}\n变更说明：{{changeDescription}}\n操作时间：{{changeTime}}\n操作人员：{{operatorName}}\n\n如有疑问，请联系客服。\n\n智慧园区管理团队', '账户状态变更邮件模板'),

('TRANSACTION_ANOMALY', '交易异常通知', 'EMAIL', '【智慧园区】交易异常提醒', '尊敬的用户，检测到异常交易：\n\n交易编号：{{transactionId}}\n异常类型：{{anomalyType}}\n交易金额：{{amount}}\n交易时间：{{transactionTime}}\n\n如非本人操作，请立即联系客服。\n\n智慧园区安全团队', '交易异常通知邮件模板'),

('LIMIT_REMINDER', '消费限额提醒', 'SMS', NULL, '【智慧园区】消费提醒：您的{{limitType}}消费已使用{{usedAmount}}，限额为{{limitAmount}}，使用率{{usageRate}}%。', '消费限额提醒短信模板'),

('LOGIN_ANOMALY', '登录异常通知', 'PUSH', NULL, '登录异常提醒：检测到异常登录，地点：{{location}}，设备：{{device}}', '登录异常提醒推送模板'),

('PASSWORD_RESET', '密码重置通知', 'EMAIL', '【智慧园区】密码重置通知', '尊敬的用户，您的{{passwordType}}已成功重置：\n\n重置时间：{{resetTime}}\n重置方式：{{resetMethod}}\n重置设备：{{resetDevice}}\n\n如非本人操作，请立即联系客服。\n\n智慧园区安全团队', '密码重置通知邮件模板');

-- 创建索引优化查询性能
CREATE INDEX idx_notification_log_person_type ON t_security_notification_log(person_id, notification_type);
CREATE INDEX idx_notification_log_time_status ON t_security_notification_log(create_time, status);
CREATE INDEX idx_device_push_person_active ON t_device_push_registration(person_id, is_active);
CREATE INDEX idx_wechat_binding_person_active ON t_wechat_user_binding(person_id, is_active);