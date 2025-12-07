-- ============================================================
-- IOE-DREAM Common Service - Monitor模块
-- 表名: t_alert_rule (告警规则表)
-- 功能: 告警规则配置
-- 创建时间: 2025-12-02
-- ============================================================

CREATE TABLE IF NOT EXISTS `t_alert_rule` (
    `rule_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '规则ID',
    `rule_name` VARCHAR(100) NOT NULL COMMENT '规则名称',
    `rule_description` VARCHAR(500) COMMENT '规则描述',
    `metric_name` VARCHAR(100) NOT NULL COMMENT '监控指标',
    `monitor_type` VARCHAR(50) COMMENT '监控类型',
    `condition_operator` VARCHAR(20) NOT NULL COMMENT '告警条件：GT/GTE/LT/LTE/EQ/NEQ',
    `threshold_value` DOUBLE NOT NULL COMMENT '告警阈值',
    `alert_level` VARCHAR(20) NOT NULL COMMENT '告警级别：INFO/WARNING/ERROR/CRITICAL',
    `applicable_services` VARCHAR(500) COMMENT '适用服务（逗号分隔）',
    `applicable_environments` VARCHAR(200) COMMENT '适用环境（逗号分隔）',
    `status` VARCHAR(20) NOT NULL DEFAULT 'ENABLED' COMMENT '规则状态：ENABLED/DISABLED',
    `duration_minutes` INT COMMENT '持续时间（分钟）',
    `notification_channels` VARCHAR(200) COMMENT '通知方式（逗号分隔）',
    `notification_users` VARCHAR(500) COMMENT '通知人员（逗号分隔）',
    `notification_interval` INT COMMENT '通知频率（分钟）',
    `suppression_duration` INT COMMENT '抑制时间（分钟）',
    `rule_expression` TEXT COMMENT '规则表达式',
    `priority` INT NOT NULL DEFAULT 0 COMMENT '规则优先级',
    `tags` VARCHAR(500) COMMENT '标签',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    PRIMARY KEY (`rule_id`),
    KEY `idx_metric_name` (`metric_name`),
    KEY `idx_status` (`status`),
    KEY `idx_alert_level` (`alert_level`),
    KEY `idx_deleted_flag` (`deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='告警规则表';

