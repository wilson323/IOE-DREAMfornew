-- IOE-DREAM 审计服务数据库表结构
-- 创建时间: 2025-11-29
-- 版本: 1.0.0

-- 创建审计日志表
CREATE TABLE IF NOT EXISTS `t_audit_log` (
    `audit_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '审计日志ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '操作用户ID',
    `username` VARCHAR(100) DEFAULT NULL COMMENT '用户名',
    `operation_type` INT NOT NULL COMMENT '操作类型：1-登录 2-登出 3-增 4-删 5-改 6-查 7-导出 8-导入 9-配置 10-其他',
    `module_name` VARCHAR(100) DEFAULT NULL COMMENT '操作模块',
    `function_name` VARCHAR(200) DEFAULT NULL COMMENT '操作功能',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '操作描述',
    `request_method` VARCHAR(20) DEFAULT NULL COMMENT '请求方法',
    `request_url` VARCHAR(1000) DEFAULT NULL COMMENT '请求URL',
    `request_params` LONGTEXT DEFAULT NULL COMMENT '请求参数',
    `response_data` LONGTEXT DEFAULT NULL COMMENT '响应结果',
    `result_status` INT NOT NULL DEFAULT 3 COMMENT '操作结果：1-成功 2-失败 3-异常',
    `error_code` VARCHAR(100) DEFAULT NULL COMMENT '错误码',
    `error_message` LONGTEXT DEFAULT NULL COMMENT '错误信息',
    `execution_time` BIGINT DEFAULT NULL COMMENT '执行时长（毫秒）',
    `client_ip` VARCHAR(100) DEFAULT NULL COMMENT '客户端IP',
    `user_agent` VARCHAR(1000) DEFAULT NULL COMMENT '用户代理',
    `device_info` VARCHAR(500) DEFAULT NULL COMMENT '设备信息',
    `session_id` VARCHAR(100) DEFAULT NULL COMMENT '会话ID',
    `request_id` VARCHAR(100) DEFAULT NULL COMMENT '请求ID',
    `operation_time` DATETIME NOT NULL COMMENT '操作时间',
    `data_change` LONGTEXT DEFAULT NULL COMMENT '操作前后数据对比',
    `business_id` VARCHAR(100) DEFAULT NULL COMMENT '业务ID',
    `business_type` VARCHAR(100) DEFAULT NULL COMMENT '业务类型',
    `risk_level` INT DEFAULT 1 COMMENT '风险等级：1-低 2-中 3-高',
    `audit_tags` VARCHAR(500) DEFAULT NULL COMMENT '审计标签',
    `extensions` LONGTEXT DEFAULT NULL COMMENT '扩展属性',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`audit_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_operation_time` (`operation_time`),
    KEY `idx_module_name` (`module_name`),
    KEY `idx_operation_type` (`operation_type`),
    KEY `idx_result_status` (`result_status`),
    KEY `idx_client_ip` (`client_ip`),
    KEY `idx_session_id` (`session_id`),
    KEY `idx_request_id` (`request_id`),
    KEY `idx_risk_level` (`risk_level`),
    KEY `idx_business_id` (`business_id`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_user_time` (`user_id`, `operation_time`),
    KEY `idx_module_time` (`module_name`, `operation_time`),
    KEY `idx_type_time` (`operation_type`, `operation_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审计日志表';

-- 创建审计配置表
CREATE TABLE IF NOT EXISTS `t_audit_config` (
    `config_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
    `config_value` TEXT DEFAULT NULL COMMENT '配置值',
    `config_type` VARCHAR(50) NOT NULL COMMENT '配置类型',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '配置描述',
    `is_enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用：0-禁用 1-启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`config_id`),
    UNIQUE KEY `uk_config_key` (`config_key`),
    KEY `idx_config_type` (`config_type`),
    KEY `idx_is_enabled` (`is_enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审计配置表';

-- 创建审计规则表
CREATE TABLE IF NOT EXISTS `t_audit_rule` (
    `rule_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '规则ID',
    `rule_name` VARCHAR(200) NOT NULL COMMENT '规则名称',
    `rule_code` VARCHAR(100) NOT NULL COMMENT '规则代码',
    `rule_type` INT NOT NULL COMMENT '规则类型：1-实时监控 2-批量检查 3-定时任务',
    `rule_condition` TEXT DEFAULT NULL COMMENT '规则条件',
    `rule_expression` TEXT DEFAULT NULL COMMENT '规则表达式',
    `action_type` INT NOT NULL COMMENT '动作类型：1-记录日志 2-发送告警 3-拦截操作',
    `risk_level` INT NOT NULL DEFAULT 1 COMMENT '风险等级：1-低 2-中 3-高',
    `priority` INT NOT NULL DEFAULT 0 COMMENT '优先级',
    `is_enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用：0-禁用 1-启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(100) DEFAULT NULL COMMENT '创建人',
    `update_by` VARCHAR(100) DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`rule_id`),
    UNIQUE KEY `uk_rule_code` (`rule_code`),
    KEY `idx_rule_type` (`rule_type`),
    KEY `idx_is_enabled` (`is_enabled`),
    KEY `idx_priority` (`priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审计规则表';

-- 创建审计告警记录表
CREATE TABLE IF NOT EXISTS `t_audit_alert` (
    `alert_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '告警ID',
    `rule_id` BIGINT DEFAULT NULL COMMENT '规则ID',
    `alert_type` VARCHAR(50) NOT NULL COMMENT '告警类型',
    `alert_level` VARCHAR(20) NOT NULL COMMENT '告警级别',
    `alert_title` VARCHAR(500) NOT NULL COMMENT '告警标题',
    `alert_content` TEXT DEFAULT NULL COMMENT '告警内容',
    `alert_data` LONGTEXT DEFAULT NULL COMMENT '告警数据',
    `audit_log_id` BIGINT DEFAULT NULL COMMENT '关联审计日志ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '相关用户ID',
    `ip_address` VARCHAR(100) DEFAULT NULL COMMENT 'IP地址',
    `status` INT NOT NULL DEFAULT 1 COMMENT '告警状态：1-待处理 2-处理中 3-已处理 4-已忽略',
    `process_result` TEXT DEFAULT NULL COMMENT '处理结果',
    `process_time` DATETIME DEFAULT NULL COMMENT '处理时间',
    `process_by` VARCHAR(100) DEFAULT NULL COMMENT '处理人',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`alert_id`),
    KEY `idx_rule_id` (`rule_id`),
    KEY `idx_audit_log_id` (`audit_log_id`),
    KEY `idx_alert_type` (`alert_type`),
    KEY `idx_alert_level` (`alert_level`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审计告警记录表';

-- 创建审计报表表
CREATE TABLE IF NOT EXISTS `t_audit_report` (
    `report_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '报表ID',
    `report_name` VARCHAR(200) NOT NULL COMMENT '报表名称',
    `report_type` VARCHAR(50) NOT NULL COMMENT '报表类型',
    `report_period` VARCHAR(100) DEFAULT NULL COMMENT '报表周期',
    `report_data` LONGTEXT DEFAULT NULL COMMENT '报表数据',
    `file_path` VARCHAR(1000) DEFAULT NULL COMMENT '报表文件路径',
    `file_size` BIGINT DEFAULT NULL COMMENT '文件大小',
    `file_format` VARCHAR(20) DEFAULT NULL COMMENT '文件格式',
    `status` INT NOT NULL DEFAULT 1 COMMENT '报表状态：1-生成中 2-已完成 3-生成失败',
    `generate_time` DATETIME DEFAULT NULL COMMENT '生成时间',
    `expire_time` DATETIME DEFAULT NULL COMMENT '过期时间',
    `create_user_id` BIGINT DEFAULT NULL COMMENT '创建用户ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`report_id`),
    KEY `idx_report_type` (`report_type`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_create_user_id` (`create_user_id`),
    KEY `idx_generate_time` (`generate_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审计报表表';

-- 插入默认配置数据
INSERT INTO `t_audit_config` (`config_key`, `config_value`, `config_type`, `description`, `is_enabled`) VALUES
('audit.log.retention.days', '365', 'retention', '审计日志保留天数', 1),
('audit.log.batch.size', '1000', 'performance', '批量插入审计日志的最大数量', 1),
('audit.alert.enabled', 'true', 'alert', '是否启用审计告警', 1),
('audit.risk.high.threshold', '3', 'risk', '高风险操作的阈值等级', 1),
('audit.export.max.rows', '10000', 'export', '导出审计日志的最大行数', 1),
('audit.statistics.cache.expire', '30', 'cache', '统计数据缓存过期时间（分钟）', 1);

-- 插入默认审计规则
INSERT INTO `t_audit_rule` (`rule_name`, `rule_code`, `rule_type`, `rule_condition`, `action_type`, `risk_level`, `priority`, `description`, `is_enabled`) VALUES
('多次登录失败监控', 'MULTIPLE_LOGIN_FAILURE', 1, 'COUNT(*) >= 5 WHERE operation_type = 1 AND result_status != 1 GROUP BY user_id, client_ip HAVING COUNT(*) >= 5', 2, 3, 100, '监控用户多次登录失败的情况', 1),
('高风险操作监控', 'HIGH_RISK_OPERATION', 1, 'risk_level = 3 AND result_status = 1', 2, 3, 90, '监控所有高风险操作', 1),
('数据批量删除监控', 'BATCH_DELETE_OPERATION', 1, 'operation_type = 4 AND COUNT(*) >= 10', 2, 3, 95, '监控批量数据删除操作', 1),
('异常访问时间监控', 'ABNORMAL_ACCESS_TIME', 1, 'HOUR(operation_time) NOT BETWEEN 8 AND 18 AND operation_type IN (3,4,5)', 2, 2, 80, '监控非工作时间的敏感操作', 1),
('管理员操作监控', 'ADMIN_OPERATION', 1, 'user_id IN (SELECT user_id FROM t_user WHERE user_type = 1)', 2, 2, 70, '监控管理员的所有操作', 1);

-- 创建索引优化查询性能
CREATE INDEX IF NOT EXISTS `idx_audit_log_composite1` ON `t_audit_log` (`user_id`, `operation_time`, `result_status`);
CREATE INDEX IF NOT EXISTS `idx_audit_log_composite2` ON `t_audit_log` (`module_name`, `operation_time`, `operation_type`);
CREATE INDEX IF NOT EXISTS `idx_audit_log_composite3` ON `t_audit_log` (`risk_level`, `operation_time`, `result_status`);

-- 添加表注释
ALTER TABLE `t_audit_log` COMMENT = '审计日志表';
ALTER TABLE `t_audit_config` COMMENT = '审计配置表';
ALTER TABLE `t_audit_rule` COMMENT = '审计规则表';
ALTER TABLE `t_audit_alert` COMMENT = '审计告警记录表';
ALTER TABLE `t_audit_report` COMMENT = '审计报表表';