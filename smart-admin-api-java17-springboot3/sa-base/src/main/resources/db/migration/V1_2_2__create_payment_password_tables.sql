-- 创建支付密码表
-- 支付密码验证和管理功能
-- 创建时间: 2025-01-17

-- 支付密码表
CREATE TABLE `t_payment_password` (
    `password_id` BIGINT AUTO_INCREMENT COMMENT '主键ID',
    `person_id` BIGINT NOT NULL COMMENT '人员ID',
    `password_hash` VARCHAR(255) NOT NULL COMMENT '密码哈希值',
    `salt` VARCHAR(255) NOT NULL COMMENT '密码盐值',
    `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '密码状态（ACTIVE-正常，LOCKED-锁定，DISABLED-禁用）',
    `expired_time` DATETIME COMMENT '密码过期时间',
    `lock_time` DATETIME COMMENT '锁定时间',
    `lock_until` DATETIME COMMENT '锁定截止时间',
    `lock_reason` VARCHAR(500) COMMENT '锁定原因',
    `last_modify_time` DATETIME COMMENT '最后修改时间',
    `last_verify_time` DATETIME COMMENT '最后验证时间',
    `failure_count` INT DEFAULT 0 COMMENT '验证失败次数',
    `total_verify_count` INT DEFAULT 0 COMMENT '累计验证次数',
    `last_verify_ip` VARCHAR(50) COMMENT '最后验证IP',
    `last_verify_device` VARCHAR(100) COMMENT '最后验证设备',
    `enable_biometric` TINYINT(1) DEFAULT 0 COMMENT '是否启用生物特征验证',
    `biometric_types` VARCHAR(100) COMMENT '生物特征类型（FINGERPRINT-指纹，FACE-人脸，IRIS-虹膜）',
    `biometric_template` TEXT COMMENT '生物特征数据模板',
    `password_strength` VARCHAR(20) COMMENT '密码强度等级（WEAK/MEDIUM/STRONG/VERY_STRONG）',
    `password_score` INT COMMENT '密码分数（0-100）',
    `force_password_change` TINYINT(1) DEFAULT 0 COMMENT '是否需要强制更新密码',
    `security_question` VARCHAR(200) COMMENT '安全问题',
    `security_answer_hash` VARCHAR(255) COMMENT '安全问题答案（哈希存储）',
    `security_answer_salt` VARCHAR(255) COMMENT '安全问题盐值',
    `extend_data` TEXT COMMENT '扩展数据（JSON格式）',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_user_id` BIGINT COMMENT '创建用户ID',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_user_id` BIGINT COMMENT '更新用户ID',
    `deleted_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记',
    `version` INT NOT NULL DEFAULT 1 COMMENT '版本号',
    PRIMARY KEY (`password_id`),
    UNIQUE KEY `uk_person_id` (`person_id`, `deleted_flag`),
    KEY `idx_person_id` (`person_id`),
    KEY `idx_status` (`status`),
    KEY `idx_expired_time` (`expired_time`),
    KEY `idx_lock_until` (`lock_until`),
    KEY `idx_last_verify_time` (`last_verify_time`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_deleted_flag` (`deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付密码表';

-- 创建支付密码验证日志表
CREATE TABLE `t_payment_password_log` (
    `log_id` BIGINT AUTO_INCREMENT COMMENT '主键ID',
    `person_id` BIGINT NOT NULL COMMENT '人员ID',
    `password_id` BIGINT NOT NULL COMMENT '密码ID',
    `verify_time` DATETIME NOT NULL COMMENT '验证时间',
    `verify_method` VARCHAR(50) NOT NULL COMMENT '验证方式（PASSWORD/BIOMETRIC_FINGERPRINT/BIOMETRIC_FACE等）',
    `verify_result` VARCHAR(20) NOT NULL COMMENT '验证结果（SUCCESS/FAILED）',
    `failure_reason` VARCHAR(200) COMMENT '失败原因',
    `client_ip` VARCHAR(50) COMMENT '客户端IP地址',
    `device_id` BIGINT COMMENT '设备ID',
    `device_info` VARCHAR(100) COMMENT '设备信息',
    `location` VARCHAR(100) COMMENT '地理位置',
    `user_agent` VARCHAR(500) COMMENT '用户代理',
    `risk_level` VARCHAR(20) COMMENT '风险等级（SAFE/LOW/MEDIUM/HIGH/CRITICAL）',
    `is_abnormal` TINYINT(1) DEFAULT 0 COMMENT '是否为异常行为',
    `session_id` VARCHAR(100) COMMENT '会话ID',
    `order_no` VARCHAR(50) COMMENT '交易订单号',
    `verify_duration` BIGINT COMMENT '验证耗时（毫秒）',
    `extend_data` TEXT COMMENT '扩展数据（JSON格式）',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`log_id`),
    KEY `idx_person_id` (`person_id`),
    KEY `idx_password_id` (`password_id`),
    KEY `idx_verify_time` (`verify_time`),
    KEY `idx_verify_result` (`verify_result`),
    KEY `idx_client_ip` (`client_ip`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_risk_level` (`risk_level`),
    KEY `idx_is_abnormal` (`is_abnormal`),
    KEY `idx_order_no` (`order_no`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付密码验证日志表';

-- 创建支付密码策略配置表
CREATE TABLE `t_payment_password_policy` (
    `policy_id` BIGINT AUTO_INCREMENT COMMENT '主键ID',
    `policy_name` VARCHAR(100) NOT NULL COMMENT '策略名称',
    `policy_code` VARCHAR(50) NOT NULL COMMENT '策略编码',
    `min_length` INT NOT NULL DEFAULT 6 COMMENT '密码最小长度',
    `max_length` INT NOT NULL DEFAULT 20 COMMENT '密码最大长度',
    `require_complex` TINYINT(1) DEFAULT 1 COMMENT '是否要求复杂度（包含字母和数字）',
    `require_uppercase` TINYINT(1) DEFAULT 0 COMMENT '是否要求大写字母',
    `require_lowercase` TINYINT(1) DEFAULT 0 COMMENT '是否要求小写字母',
    `require_digit` TINYINT(1) DEFAULT 1 COMMENT '是否要求数字',
    `require_special` TINYINT(1) DEFAULT 0 COMMENT '是否要求特殊字符',
    `max_attempts` INT NOT NULL DEFAULT 5 COMMENT '最大尝试次数',
    `lock_duration_minutes` INT NOT NULL DEFAULT 30 COMMENT '锁定时长（分钟）',
    `password_expiry_days` INT COMMENT '密码有效期（天）',
    `require_password_history` TINYINT(1) DEFAULT 0 COMMENT '是否要求密码历史',
    `password_history_count` INT DEFAULT 5 COMMENT '密码历史记录数量',
    `support_biometric` TINYINT(1) DEFAULT 0 COMMENT '是否支持生物特征验证',
    `allowed_biometric_types` VARCHAR(200) COMMENT '允许的生物特征类型',
    `allow_weak_password` TINYINT(1) DEFAULT 0 COMMENT '是否允许常见弱密码',
    `strength_threshold` INT DEFAULT 60 COMMENT '强度阈值',
    `is_default` TINYINT(1) DEFAULT 0 COMMENT '是否为默认策略',
    `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态（ACTIVE-启用，INACTIVE-禁用）',
    `priority` INT DEFAULT 0 COMMENT '优先级',
    `description` TEXT COMMENT '策略描述',
    `extend_data` TEXT COMMENT '扩展数据（JSON格式）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_user_id` BIGINT COMMENT '创建用户ID',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_user_id` BIGINT COMMENT '更新用户ID',
    `deleted_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记',
    `version` INT NOT NULL DEFAULT 1 COMMENT '版本号',
    PRIMARY KEY (`policy_id`),
    UNIQUE KEY `uk_policy_code` (`policy_code`, `deleted_flag`),
    KEY `idx_policy_name` (`policy_name`),
    KEY `idx_status` (`status`),
    KEY `idx_priority` (`priority`),
    KEY `idx_is_default` (`is_default`),
    KEY `idx_deleted_flag` (`deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付密码策略配置表';

-- 插入默认密码策略配置
INSERT INTO `t_payment_password_policy` (
    `policy_name`, `policy_code`, `min_length`, `max_length`, `require_complex`,
    `require_uppercase`, `require_lowercase`, `require_digit`, `require_special`,
    `max_attempts`, `lock_duration_minutes`, `password_expiry_days`,
    `require_password_history`, `password_history_count`, `support_biometric`,
    `allowed_biometric_types`, `allow_weak_password`, `strength_threshold`,
    `is_default`, `status`, `priority`, `description`
) VALUES (
    '默认密码策略', 'DEFAULT', 6, 20, 1,
    0, 0, 1, 0,
    5, 30, 90,
    0, 5, 1,
    'FINGERPRINT,FACE,IRIS', 0, 60,
    1, 'ACTIVE', 100, '系统默认密码安全策略'
), (
    '高安全策略', 'HIGH_SECURITY', 8, 32, 1,
    1, 1, 1, 1,
    3, 60, 60,
    1, 10, 1,
    'FINGERPRINT,FACE,IRIS', 0, 80,
    0, 'ACTIVE', 90, '高安全级别密码策略'
), (
    '低安全策略', 'LOW_SECURITY', 4, 16, 0,
    0, 0, 1, 0,
    10, 15, 180,
    0, 0, 0,
    '', 1, 40,
    0, 'ACTIVE', 80, '低安全级别密码策略'
);

-- 添加索引优化查询性能
ALTER TABLE `t_payment_password` ADD INDEX `idx_person_status` (`person_id`, `status`);
ALTER TABLE `t_payment_password` ADD INDEX `idx_status_expired` (`status`, `expired_time`);
ALTER TABLE `t_payment_password_log` ADD INDEX `idx_person_verify_time` (`person_id`, `verify_time`);
ALTER TABLE `t_payment_password_log` ADD INDEX `idx_result_verify_time` (`verify_result`, `verify_time`);