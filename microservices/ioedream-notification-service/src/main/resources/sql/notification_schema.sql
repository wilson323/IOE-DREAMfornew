-- IOE-DREAM 通知服务数据库表结构
-- 创建时间: 2025-11-29
-- 版本: 1.0.0

-- 创建通知消息表
CREATE TABLE IF NOT EXISTS `t_notification_message` (
    `message_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    `subject` VARCHAR(500) DEFAULT NULL COMMENT '消息主题',
    `content` LONGTEXT DEFAULT NULL COMMENT '消息内容',
    `message_type` INT NOT NULL COMMENT '消息类型：1-系统通知 2-业务通知 3-告警通知 4-营销通知 5-验证码',
    `channel` INT NOT NULL COMMENT '发送渠道：1-邮件 2-短信 3-微信 4-站内信 5-推送 6-语音',
    `send_status` INT NOT NULL DEFAULT 0 COMMENT '发送状态：0-待发送 1-发送中 2-发送成功 3-发送失败 4-已撤销',
    `priority` INT NOT NULL DEFAULT 2 COMMENT '优先级：1-低 2-普通 3-高 4-紧急',
    `recipient_user_id` BIGINT DEFAULT NULL COMMENT '接收用户ID',
    `recipient_identifier` VARCHAR(200) DEFAULT NULL COMMENT '接收人标识',
    `recipient_name` VARCHAR(100) DEFAULT NULL COMMENT '接收人姓名',
    `sender_user_id` BIGINT DEFAULT NULL COMMENT '发送用户ID',
    `sender_name` VARCHAR(100) DEFAULT NULL COMMENT '发送人姓名',
    `business_type` VARCHAR(100) DEFAULT NULL COMMENT '业务类型',
    `business_id` VARCHAR(100) DEFAULT NULL COMMENT '业务ID',
    `template_id` VARCHAR(50) DEFAULT NULL COMMENT '模板ID',
    `template_params` LONGTEXT DEFAULT NULL COMMENT '模板参数（JSON格式）',
    `attachments` LONGTEXT DEFAULT NULL COMMENT '附件信息（JSON格式）',
    `schedule_time` DATETIME DEFAULT NULL COMMENT '计划发送时间',
    `sent_time` DATETIME DEFAULT NULL COMMENT '实际发送时间',
    `retry_count` INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    `max_retry_count` INT NOT NULL DEFAULT 3 COMMENT '最大重试次数',
    `failure_reason` TEXT DEFAULT NULL COMMENT '失败原因',
    `external_message_id` VARCHAR(200) DEFAULT NULL COMMENT '外部消息ID',
    `send_duration` BIGINT DEFAULT NULL COMMENT '发送耗时（毫秒）',
    `read_status` INT NOT NULL DEFAULT 0 COMMENT '已读状态（仅站内信有效）：0-未读 1-已读',
    `read_time` DATETIME DEFAULT NULL COMMENT '阅读时间',
    `extensions` LONGTEXT DEFAULT NULL COMMENT '扩展属性（JSON格式）',
    `create_user_id` BIGINT DEFAULT NULL COMMENT '创建用户ID',
    `update_user_id` BIGINT DEFAULT NULL COMMENT '更新用户ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0-正常 1-删除',
    PRIMARY KEY (`message_id`),
    KEY `idx_recipient_user_id` (`recipient_user_id`),
    KEY `idx_sender_user_id` (`sender_user_id`),
    KEY `idx_channel` (`channel`),
    KEY `idx_message_type` (`message_type`),
    KEY `idx_send_status` (`send_status`),
    KEY `idx_priority` (`priority`),
    KEY `idx_business_type` (`business_type`),
    KEY `idx_business_id` (`business_id`),
    KEY `idx_template_id` (`template_id`),
    KEY `idx_schedule_time` (`schedule_time`),
    KEY `idx_sent_time` (`sent_time`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_channel_status` (`channel`, `send_status`),
    KEY `idx_user_channel_status` (`recipient_user_id`, `channel`, `send_status`),
    KEY `idx_read_status` (`read_status`),
    KEY `idx_user_read_status` (`recipient_user_id`, `read_status`),
    KEY `idx_deleted_flag` (`deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知消息表';

-- 创建通知模板表
CREATE TABLE IF NOT EXISTS `t_notification_template` (
    `template_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '模板ID',
    `template_code` VARCHAR(100) NOT NULL COMMENT '模板编码（唯一标识）',
    `template_name` VARCHAR(200) NOT NULL COMMENT '模板名称',
    `channel` INT NOT NULL COMMENT '适用渠道：1-邮件 2-短信 3-微信 4-站内信 5-推送 6-语音',
    `message_type` INT NOT NULL COMMENT '消息类型：1-系统通知 2-业务通知 3-告警通知 4-营销通知 5-验证码',
    `subject` VARCHAR(500) DEFAULT NULL COMMENT '模板主题',
    `content` LONGTEXT DEFAULT NULL COMMENT '模板内容（支持变量占位符）',
    `description` TEXT DEFAULT NULL COMMENT '模板描述',
    `param_definition` LONGTEXT DEFAULT NULL COMMENT '模板参数定义（JSON格式）',
    `status` INT NOT NULL DEFAULT 1 COMMENT '模板状态：1-启用 0-禁用',
    `is_default` INT NOT NULL DEFAULT 0 COMMENT '是否为默认模板：1-是 0-否',
    `version` VARCHAR(20) NOT NULL DEFAULT '1.0' COMMENT '版本号',
    `priority` INT NOT NULL DEFAULT 0 COMMENT '优先级（数值越大优先级越高）',
    `tags` VARCHAR(500) DEFAULT NULL COMMENT '标签（用于分类和搜索）',
    `language` VARCHAR(10) NOT NULL DEFAULT 'zh-CN' COMMENT '语言类型：zh-CN、en-US等',
    `usage_count` BIGINT NOT NULL DEFAULT 0 COMMENT '使用次数统计',
    `last_used_time` DATETIME DEFAULT NULL COMMENT '最后使用时间',
    `approval_status` INT NOT NULL DEFAULT 2 COMMENT '审批状态：0-草稿 1-待审批 2-已审批 3-已拒绝',
    `approval_by` BIGINT DEFAULT NULL COMMENT '审批人ID',
    `approval_time` DATETIME DEFAULT NULL COMMENT '审批时间',
    `approval_comment` TEXT DEFAULT NULL COMMENT '审批意见',
    `extensions` LONGTEXT DEFAULT NULL COMMENT '扩展配置（JSON格式）',
    `create_user_id` BIGINT DEFAULT NULL COMMENT '创建用户ID',
    `update_user_id` BIGINT DEFAULT NULL COMMENT '更新用户ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0-正常 1-删除',
    PRIMARY KEY (`template_id`),
    UNIQUE KEY `uk_template_code` (`template_code`),
    KEY `idx_channel` (`channel`),
    KEY `idx_message_type` (`message_type`),
    KEY `idx_status` (`status`),
    KEY `idx_is_default` (`is_default`),
    KEY `idx_approval_status` (`approval_status`),
    KEY `idx_language` (`language`),
    KEY `idx_tags` (`tags`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_channel_type_status` (`channel`, `message_type`, `status`),
    KEY `idx_deleted_flag` (`deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知模板表';

-- 创建通知配置表
CREATE TABLE IF NOT EXISTS `t_notification_config` (
    `config_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '用户ID（用户级配置时使用）',
    `config_type` INT NOT NULL COMMENT '配置类型：1-系统配置 2-用户配置',
    `config_key` VARCHAR(200) NOT NULL COMMENT '配置键',
    `config_value` LONGTEXT DEFAULT NULL COMMENT '配置值',
    `config_name` VARCHAR(200) DEFAULT NULL COMMENT '配置名称',
    `description` TEXT DEFAULT NULL COMMENT '配置描述',
    `channel` INT DEFAULT NULL COMMENT '适用渠道：1-邮件 2-短信 3-微信 4-站内信 5-推送 6-语音，0表示所有渠道',
    `message_type` INT DEFAULT NULL COMMENT '适用消息类型：1-系统通知 2-业务通知 3-告警通知 4-营销通知 5-验证码，0表示所有类型',
    `status` INT NOT NULL DEFAULT 1 COMMENT '配置状态：1-启用 0-禁用',
    `is_default` INT NOT NULL DEFAULT 0 COMMENT '是否为默认配置：1-是 0-否',
    `category` VARCHAR(100) DEFAULT NULL COMMENT '配置分类',
    `data_type` VARCHAR(20) NOT NULL DEFAULT 'string' COMMENT '数据类型：string、number、boolean、json',
    `validation_rules` LONGTEXT DEFAULT NULL COMMENT '验证规则（JSON格式）',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序权重',
    `readonly` INT NOT NULL DEFAULT 0 COMMENT '是否只读：1-是 0-否',
    `extensions` LONGTEXT DEFAULT NULL COMMENT '扩展属性（JSON格式）',
    `create_user_id` BIGINT DEFAULT NULL COMMENT '创建用户ID',
    `update_user_id` BIGINT DEFAULT NULL COMMENT '更新用户ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0-正常 1-删除',
    PRIMARY KEY (`config_id`),
    UNIQUE KEY `uk_user_config_key` (`user_id`, `config_key`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_config_type` (`config_type`),
    KEY `idx_config_key` (`config_key`),
    KEY `idx_channel` (`channel`),
    KEY `idx_message_type` (`message_type`),
    KEY `idx_status` (`status`),
    KEY `idx_category` (`category`),
    KEY `idx_sort_order` (`sort_order`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_deleted_flag` (`deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知配置表';

-- 创建通知发送记录表
CREATE TABLE IF NOT EXISTS `t_notification_record` (
    `record_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `message_id` BIGINT DEFAULT NULL COMMENT '消息ID',
    `batch_id` VARCHAR(50) DEFAULT NULL COMMENT '批次ID（用于批量发送的跟踪）',
    `channel` INT NOT NULL COMMENT '发送渠道：1-邮件 2-短信 3-微信 4-站内信 5-推送 6-语音',
    `message_type` INT NOT NULL COMMENT '消息类型：1-系统通知 2-业务通知 3-告警通知 4-营销通知 5-验证码',
    `send_status` INT NOT NULL COMMENT '发送状态：1-成功 2-失败 3-超时 4-撤销',
    `priority` INT NOT NULL DEFAULT 2 COMMENT '优先级：1-低 2-普通 3-高 4-紧急',
    `recipient_user_id` BIGINT DEFAULT NULL COMMENT '接收用户ID',
    `recipient_identifier` VARCHAR(200) DEFAULT NULL COMMENT '接收人标识',
    `sender_user_id` BIGINT DEFAULT NULL COMMENT '发送用户ID',
    `business_type` VARCHAR(100) DEFAULT NULL COMMENT '业务类型',
    `business_id` VARCHAR(100) DEFAULT NULL COMMENT '业务ID',
    `template_id` VARCHAR(50) DEFAULT NULL COMMENT '模板ID',
    `send_start_time` DATETIME DEFAULT NULL COMMENT '发送开始时间',
    `send_end_time` DATETIME DEFAULT NULL COMMENT '发送结束时间',
    `send_duration` BIGINT DEFAULT NULL COMMENT '发送耗时（毫秒）',
    `retry_count` INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    `external_message_id` VARCHAR(200) DEFAULT NULL COMMENT '外部消息ID',
    `response_code` VARCHAR(100) DEFAULT NULL COMMENT '响应码',
    `response_message` TEXT DEFAULT NULL COMMENT '响应消息',
    `error_detail` LONGTEXT DEFAULT NULL COMMENT '错误详情',
    `send_content` LONGTEXT DEFAULT NULL COMMENT '发送内容（实际发送的内容）',
    `send_subject` VARCHAR(1000) DEFAULT NULL COMMENT '发送主题',
    `service_provider` VARCHAR(100) DEFAULT NULL COMMENT '发送服务商（如阿里云、腾讯云等）',
    `send_cost` INT DEFAULT NULL COMMENT '发送成本（分）',
    `delivery_status` INT DEFAULT NULL COMMENT '配送状态（仅推送和邮件有效）：1-已送达 2-已读 3-已点击 4-已退订',
    `delivery_time` DATETIME DEFAULT NULL COMMENT '配送时间',
    `last_track_time` DATETIME DEFAULT NULL COMMENT '最后跟踪时间',
    `user_action` VARCHAR(100) DEFAULT NULL COMMENT '用户行为（点击、打开等）',
    `user_action_time` DATETIME DEFAULT NULL COMMENT '用户行为时间',
    `location_info` VARCHAR(500) DEFAULT NULL COMMENT '地理位置信息',
    `device_info` VARCHAR(500) DEFAULT NULL COMMENT '设备信息',
    `extensions` LONGTEXT DEFAULT NULL COMMENT '扩展数据（JSON格式）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`record_id`),
    KEY `idx_message_id` (`message_id`),
    KEY `idx_batch_id` (`batch_id`),
    KEY `idx_channel` (`channel`),
    KEY `idx_message_type` (`message_type`),
    KEY `idx_send_status` (`send_status`),
    KEY `idx_priority` (`priority`),
    KEY `idx_recipient_user_id` (`recipient_user_id`),
    KEY `idx_sender_user_id` (`sender_user_id`),
    KEY `idx_business_type` (`business_type`),
    KEY `idx_business_id` (`business_id`),
    KEY `idx_template_id` (`template_id`),
    KEY `idx_send_start_time` (`send_start_time`),
    KEY `idx_send_end_time` (`send_end_time`),
    KEY `idx_service_provider` (`service_provider`),
    KEY `idx_delivery_status` (`delivery_status`),
    KEY `idx_delivery_time` (`delivery_time`),
    KEY `idx_last_track_time` (`last_track_time`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_channel_status_time` (`channel`, `send_status`, `create_time`),
    KEY `idx_user_channel_time` (`recipient_user_id`, `channel`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知发送记录表';

-- 插入默认系统配置
INSERT INTO `t_notification_config` (`config_key`, `config_value`, `config_name`, `description`, `config_type`, `category`, `data_type`, `is_default`) VALUES
('notification.email.enabled', 'false', '邮件通知启用', '是否启用邮件通知功能', 1, 'email', 'boolean', 1),
('notification.email.smtp.host', 'smtp.gmail.com', 'SMTP服务器地址', '邮件发送服务器地址', 1, 'email', 'string', 1),
('notification.email.smtp.port', '587', 'SMTP服务器端口', '邮件发送服务器端口', 1, 'email', 'number', 1),
('notification.email.from.address', 'noreply@ioedream.com', '发件人邮箱地址', '系统发送邮件的默认发件人地址', 1, 'email', 'string', 1),
('notification.email.from.name', 'IOE-DREAM系统', '发件人姓名', '系统发送邮件的默认发件人姓名', 1, 'email', 'string', 1),

('notification.sms.enabled', 'false', '短信通知启用', '是否启用短信通知功能', 1, 'sms', 'boolean', 1),
('notification.sms.provider', 'aliyun', '短信服务商', '短信发送服务商：aliyun、tencent', 1, 'sms', 'string', 1),
('notification.sms.sign.name', 'IOE-DREAM', '短信签名', '短信发送的签名', 1, 'sms', 'string', 1),

('notification.wechat.enabled', 'false', '微信通知启用', '是否启用微信通知功能', 1, 'wechat', 'boolean', 1),
('notification.wechat.app.id', '', '微信应用ID', '微信应用的AppID', 1, 'wechat', 'string', 1),
('notification.wechat.app.secret', '', '微信应用密钥', '微信应用的AppSecret', 1, 'wechat', 'string', 1),

('notification.push.enabled', 'false', '推送通知启用', '是否启用推送通知功能', 1, 'push', 'boolean', 1),
('notification.push.provider', 'jpush', '推送服务商', '推送服务商：jpush、umeng', 1, 'push', 'string', 1),
('notification.push.app.key', '', '推送应用Key', '推送服务的应用Key', 1, 'push', 'string', 1),

('notification.voice.enabled', 'false', '语音通知启用', '是否启用语音通知功能', 1, 'voice', 'boolean', 1),
('notification.voice.provider', 'aliyun', '语音服务商', '语音通知服务商：aliyun', 1, 'voice', 'string', 1),

('notification.rate.limit.enabled', 'true', '频率限制启用', '是否启用发送频率限制', 1, 'rate_limit', 'boolean', 1),
('notification.rate.limit.daily', '1000', '每日发送限制', '每日最大发送数量', 1, 'rate_limit', 'number', 1),
('notification.rate.limit.hourly', '100', '每小时发送限制', '每小时最大发送数量', 1, 'rate_limit', 'number', 1),

('notification.batch.send.size', '100', '批量发送大小', '批量发送时的批次大小', 1, 'batch_config', 'number', 1),
('notification.async.enabled', 'true', '异步发送启用', '是否启用异步发送', 1, 'system', 'boolean', 1),
('notification.retry.enabled', 'true', '重试机制启用', '是否启用失败重试机制', 1, 'retry_config', 'boolean', 1),
('notification.retry.max.count', '3', '最大重试次数', '发送失败时的最大重试次数', 1, 'retry_config', 'number', 1),

('notification.internal.expire.days', '30', '站内信过期天数', '站内信的保留天数', 1, 'retention_config', 'number', 1),
('notification.record.retention.days', '90', '发送记录保留天数', '发送记录的保留天数', 1, 'retention_config', 'number', 1),
('notification.statistics.cache.hours', '24', '统计数据缓存时间', '统计数据的缓存时间（小时）', 1, 'system', 'number', 1);

-- 插入默认通知模板
INSERT INTO `t_notification_template` (`template_code`, `template_name`, `channel`, `message_type`, `subject`, `content`, `description`, `language`, `status`, `approval_status`, `is_default`) VALUES
('EMAIL_WELCOME', '欢迎邮件', 1, 1, '欢迎加入IOE-DREAM系统', '亲爱的${userName}，欢迎您加入IOE-DREAM智能管理系统！', '用户注册成功后的欢迎邮件', 'zh-CN', 1, 2, 1),
('EMAIL_VERIFICATION', '邮件验证码', 1, 5, '验证码', '您的验证码是：${verificationCode}，有效期${expireMinutes}分钟。', '邮件验证码模板', 'zh-CN', 1, 2, 1),
('EMAIL_PASSWORD_RESET', '密码重置', 1, 1, '密码重置通知', '您的密码已重置成功，新密码为：${newPassword}，请及时修改。', '密码重置成功通知邮件', 'zh-CN', 1, 2, 1),
('EMAIL_LOGIN_ALERT', '登录提醒', 1, 3, '登录安全提醒', '您的账号在${loginTime}从${loginLocation}登录，如非本人操作请及时修改密码。', '异常登录提醒邮件', 'zh-CN', 1, 2, 1),
('EMAIL_SYSTEM_MAINTENANCE', '系统维护', 1, 1, '系统维护通知', '系统将于${maintenanceTime}进行维护，预计持续${duration}小时，请提前做好准备。', '系统维护通知邮件', 'zh-CN', 1, 2, 1),

('SMS_VERIFICATION', '短信验证码', 2, 5, '验证码', '【IOE-DREAM】您的验证码是${verificationCode}，有效期${expireMinutes}分钟。', '短信验证码模板', 'zh-CN', 1, 2, 1),
('SMS_LOGIN_CODE', '登录验证码', 2, 5, '登录验证码', '【IOE-DREAM】登录验证码：${verificationCode}，有效期${expireMinutes}分钟。', '登录短信验证码模板', 'zh-CN', 1, 2, 1),
('SMS_ORDER_NOTICE', '订单通知', 2, 2, '订单状态变更', '【IOE-DREAM】您的订单${orderNo}已${orderStatus}，请注意查收。', '订单状态变更短信通知', 'zh-CN', 1, 2, 1),
('SMS_APPOINTMENT_REMINDER', '预约提醒', 2, 2, '预约提醒', '【IOE-DREAM】您有${appointmentCount}个预约将在${reminderTime}开始，请准时参加。', '预约提醒短信', 'zh-CN', 1, 2, 1),

('INTERNAL_WELCOME', '站内信欢迎', 4, 1, '欢迎加入', '欢迎您加入IOE-DREAM系统！系统为您提供智能门禁、消费管理、考勤打卡等服务，祝您使用愉快！', '新用户注册站内信欢迎消息', 'zh-CN', 1, 2, 1),
('INTERNAL_SYSTEM_ANNOUNCEMENT', '系统公告', 4, 1, '系统公告', '${announcementTitle}\n\n${announcementContent}', '系统公告模板', 'zh-CN', 1, 2, 1),
('INTERNAL_TASK_ASSIGNMENT', '任务分配', 4, 2, '新任务分配', '您有新的待处理任务：${taskTitle}\n任务描述：${taskDescription}\n截止时间：${deadline}', '任务分配通知模板', 'zh-CN', 1, 2, 1),
('INTERNAL_APPROVAL_RESULT', '审批结果', 4, 2, '审批结果通知', '您提交的${approvalType}申请已${approvalResult}。\n申请详情：${approvalDetails}\n审批意见：${approvalComment}', '审批结果通知模板', 'zh-CN', 1, 2, 1);

-- 创建索引优化查询性能
CREATE INDEX IF NOT EXISTS `idx_notification_message_composite1` ON `t_notification_message` (`recipient_user_id`, `channel`, `send_status`, `create_time`);
CREATE INDEX IF NOT EXISTS `idx_notification_message_composite2` ON `t_notification_message` (`send_status`, `schedule_time`, `priority`);
CREATE INDEX IF NOT EXISTS `idx_notification_message_composite3` ON `t_notification_message` (`business_type`, `business_id`, `create_time`);

CREATE INDEX IF NOT EXISTS `idx_notification_template_composite1` ON `t_notification_template` (`channel`, `message_type`, `status`, `approval_status`);
CREATE INDEX IF NOT EXISTS `idx_notification_template_composite2` ON `t_notification_template` (`language`, `status`, `is_default`);
CREATE INDEX IF NOT EXISTS `idx_notification_template_composite3` ON `t_notification_template` (`tags`, `status`, `usage_count`);

CREATE INDEX IF NOT EXISTS `idx_notification_config_composite1` ON `t_notification_config` (`config_type`, `status`, `category`);
CREATE INDEX IF NOT EXISTS `idx_notification_config_composite2` ON `t_notification_config` (`user_id`, `config_type`, `status`);
CREATE INDEX IF NOT EXISTS `idx_notification_config_composite3` ON `t_notification_config` (`channel`, `message_type`, `status`);

CREATE INDEX IF NOT EXISTS `idx_notification_record_composite1` ON `t_notification_record` (`message_id`, `channel`, `send_status`);
CREATE INDEX IF NOT EXISTS `idx_notification_record_composite2` ON `t_notification_record` (`batch_id`, `send_status`, `create_time`);
CREATE INDEX IF NOT EXISTS `idx_notification_record_composite3` ON `t_notification_record` (`recipient_user_id`, `channel`, `delivery_status`, `create_time`);

-- 添加表注释
ALTER TABLE `t_notification_message` COMMENT = '通知消息表';
ALTER TABLE `t_notification_template` COMMENT = '通知模板表';
ALTER TABLE `t_notification_config` COMMENT = '通知配置表';
ALTER TABLE `t_notification_record` COMMENT = '通知发送记录表';