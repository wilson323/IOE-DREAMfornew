-- =============================================
-- 配置变更审计表
-- 基于现代化UI/UX最佳实践的企业级配置变更审计系统
-- 针对单企业1000台设备、20000人规模优化
-- 创建时间：2025-12-09
-- =============================================

CREATE TABLE `t_config_change_audit` (
  -- 主键字段
  `audit_id` BIGINT NOT NULL COMMENT '审计ID',
  `change_batch_id` VARCHAR(64) DEFAULT NULL COMMENT '变更批次ID（批量操作的统一标识）',

  -- 变更基本信息
  `change_type` VARCHAR(20) NOT NULL COMMENT '变更类型：CREATE-新增, UPDATE-更新, DELETE-删除, BATCH_UPDATE-批量更新, BATCH_DELETE-批量删除, IMPORT-导入',
  `config_type` VARCHAR(50) NOT NULL COMMENT '配置类型：SYSTEM_CONFIG-系统配置, USER_THEME-用户主题, USER_PREFERENCE-用户偏好, I18N_RESOURCE-国际化资源, THEME_TEMPLATE-主题模板, WORKFLOW_CONFIG-工作流配置',
  `config_key` VARCHAR(200) NOT NULL COMMENT '配置键标识',
  `config_name` VARCHAR(200) DEFAULT NULL COMMENT '配置名称',
  `config_group` VARCHAR(100) DEFAULT NULL COMMENT '配置分组',

  -- 变更内容
  `old_value` LONGTEXT DEFAULT NULL COMMENT '变更前值（JSON格式）',
  `new_value` LONGTEXT DEFAULT NULL COMMENT '变更后值（JSON格式）',
  `changed_fields` TEXT DEFAULT NULL COMMENT '变更字段列表（JSON格式，记录具体变更的字段）',
  `change_summary` VARCHAR(500) DEFAULT NULL COMMENT '变更摘要（自动生成变更摘要）',
  `change_reason` TEXT DEFAULT NULL COMMENT '变更原因',
  `change_source` VARCHAR(20) DEFAULT 'WEB' COMMENT '变更来源：WEB-Web界面, API-API接口, BATCH-批量操作, IMPORT-数据导入, SYSTEM-系统自动, MIGRATION-数据迁移',

  -- 操作信息
  `operator_id` BIGINT NOT NULL COMMENT '操作用户ID',
  `operator_name` VARCHAR(100) NOT NULL COMMENT '操作用户名',
  `operator_role` VARCHAR(50) DEFAULT NULL COMMENT '操作用户角色',
  `client_ip` VARCHAR(50) DEFAULT NULL COMMENT '客户端IP地址',
  `user_agent` TEXT DEFAULT NULL COMMENT '用户代理信息',
  `session_id` VARCHAR(100) DEFAULT NULL COMMENT '会话ID',
  `request_id` VARCHAR(100) DEFAULT NULL COMMENT '请求ID（用于链路追踪）',

  -- 时间信息
  `change_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '变更时间',

  -- 影响范围
  `impact_scope` VARCHAR(20) DEFAULT 'SINGLE' COMMENT '影响范围：SINGLE-单个配置, MODULE-模块级别, SYSTEM-系统级别, GLOBAL-全局级别',
  `affected_users` INT DEFAULT 0 COMMENT '影响用户数',
  `affected_devices` INT DEFAULT 0 COMMENT '影响设备数',

  -- 风险评估
  `risk_level` VARCHAR(20) DEFAULT 'LOW' COMMENT '风险等级：LOW-低风险, MEDIUM-中风险, HIGH-高风险, CRITICAL-严重风险',
  `change_status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '变更状态：PENDING-待执行, EXECUTING-执行中, SUCCESS-执行成功, FAILED-执行失败, ROLLED_BACK-已回滚',
  `execution_time` BIGINT DEFAULT 0 COMMENT '执行时间（毫秒）',
  `error_message` TEXT DEFAULT NULL COMMENT '错误信息',
  `rollback_info` LONGTEXT DEFAULT NULL COMMENT '回滚信息（JSON格式）',

  -- 审批信息
  `require_approval` TINYINT DEFAULT 0 COMMENT '是否需要审批：0-否, 1-是',
  `approver_id` BIGINT DEFAULT NULL COMMENT '审批人ID',
  `approver_name` VARCHAR(100) DEFAULT NULL COMMENT '审批人姓名',
  `approval_time` DATETIME DEFAULT NULL COMMENT '审批时间',
  `approval_comment` TEXT DEFAULT NULL COMMENT '审批意见',

  -- 关联信息
  `related_config_id` BIGINT DEFAULT NULL COMMENT '关联的配置ID',
  `related_table_name` VARCHAR(100) DEFAULT NULL COMMENT '关联的配置表名',
  `change_environment` VARCHAR(20) DEFAULT 'PROD' COMMENT '变更环境：DEV-开发环境, TEST-测试环境, STAGING-预生产环境, PROD-生产环境',
  `business_module` VARCHAR(50) DEFAULT NULL COMMENT '业务模块',
  `change_tags` TEXT DEFAULT NULL COMMENT '变更标签（JSON数组格式）',
  `extended_attributes` LONGTEXT DEFAULT NULL COMMENT '扩展属性（JSON格式）',
  `version_number` VARCHAR(50) DEFAULT NULL COMMENT '版本号',

  -- 安全属性
  `is_sensitive` TINYINT DEFAULT 0 COMMENT '是否为敏感配置：0-否, 1-是',
  `compliance_check_result` TEXT DEFAULT NULL COMMENT '合规检查结果',
  `auto_test_result` TEXT DEFAULT NULL COMMENT '自动化测试结果',
  `performance_impact` TEXT DEFAULT NULL COMMENT '性能影响评估',
  `business_impact` TEXT DEFAULT NULL COMMENT '业务影响评估',

  -- 通知信息
  `notification_status` VARCHAR(20) DEFAULT 'NOT_SENT' COMMENT '通知状态：NOT_SENT-未发送, SENT-已发送, FAILED-发送失败, READ-已读',
  `notification_time` DATETIME DEFAULT NULL COMMENT '通知时间',
  `notification_channels` TEXT DEFAULT NULL COMMENT '通知渠道（JSON数组格式：EMAIL, SMS, WEBHOOK, IN_APP）',
  `notification_recipients` LONGTEXT DEFAULT NULL COMMENT '通知接收人列表（JSON格式）',

  -- 审计字段
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `update_user_id` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除, 1-已删除',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',

  PRIMARY KEY (`audit_id`),
  INDEX `idx_change_batch_id` (`change_batch_id`),
  INDEX `idx_config_key` (`config_key`),
  INDEX `idx_operator_id` (`operator_id`),
  INDEX `idx_change_time` (`change_time`),
  INDEX `idx_change_status` (`change_status`),
  INDEX `idx_risk_level` (`risk_level`),
  INDEX `idx_config_type` (`config_type`),
  INDEX `idx_require_approval` (`require_approval`),
  INDEX `idx_notification_status` (`notification_status`),
  INDEX `idx_change_type` (`change_type`),
  INDEX `idx_affected_users` (`affected_users`),
  INDEX `idx_client_ip` (`client_ip`),
  INDEX `idx_business_module` (`business_module`),
  INDEX `idx_composite_change_query` (`config_type`, `change_status`, `change_time`),
  INDEX `idx_composite_operator_query` (`operator_id`, `change_time`, `change_status`),
  INDEX `idx_composite_risk_query` (`risk_level`, `require_approval`, `change_time`),
  INDEX `idx_composite_notification_query` (`notification_status`, `change_time`),
  INDEX `idx_deleted_flag` (`deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='配置变更审计表';

-- 添加表注释
ALTER TABLE `t_config_change_audit` COMMENT = '配置变更审计表 - 记录所有配置变更的全生命周期信息，支持变更追踪、风险评估、审批流程和通知机制';

-- 初始化默认数据（示例数据）
INSERT INTO `t_config_change_audit` (
    `audit_id`, `config_type`, `config_key`, `config_name`, `config_group`,
    `old_value`, `new_value`, `changed_fields`, `change_summary`,
    `change_reason`, `change_type`, `operator_id`, `operator_name`, `operator_role`,
    `client_ip`, `impact_scope`, `affected_users`, `risk_level`, `change_status`,
    `require_approval`, `business_module`, `is_sensitive`
) VALUES
(1702100000000000001, 'SYSTEM_CONFIG', 'system.app.name', '应用名称', 'system',
    '"IOE-DREAM管理系统"', '"IOE-DREAM智能管理平台"', '["configValue"]', '更新系统配置 - 应用名称',
    '品牌升级，更新应用名称', 'UPDATE', 1001, '系统管理员', 'ADMIN',
    '192.168.1.100', 'GLOBAL', 20000, 'MEDIUM', 'SUCCESS', 0, 'system', 0),
(1702100000000000002, 'USER_THEME', 'theme.color.primary', '主色调', 'theme',
    '"#1890ff"', '"#52c41a"', '["themeColor"]', '更新用户主题 - 主色调',
    '用户个性化设置，更换主题色', 'UPDATE', 1002, '张三', 'USER',
    '192.168.1.101', 'SINGLE', 1, 'LOW', 'SUCCESS', 0, 'theme', 0),
(1702100000000000003, 'SYSTEM_CONFIG', 'security.oauth.enabled', 'OAuth认证开关', 'security',
    'false', 'true', '["configValue"]', '新增系统配置 - OAuth认证开关',
    '启用OAuth认证功能', 'CREATE', 1001, '系统管理员', 'ADMIN',
    '192.168.1.100', 'SYSTEM', 20000, 'HIGH', 'SUCCESS', 1, 'security', 1);

-- 创建分区表（按月分区，提高查询性能）
-- 注意：MySQL 8.0+ 支持原生分区，如果是MySQL 5.7需要谨慎使用
-- ALTER TABLE `t_config_change_audit`
-- PARTITION BY RANGE (YEAR(`change_time`) * 100 + MONTH(`change_time`)) (
--     PARTITION p202412 VALUES LESS THAN (202501),
--     PARTITION p202501 VALUES LESS THAN (202502),
--     PARTITION p202502 VALUES LESS THAN (202503),
--     PARTITION p202503 VALUES LESS THAN (202504),
--     PARTITION p_future VALUES LESS THAN MAXVALUE
-- );