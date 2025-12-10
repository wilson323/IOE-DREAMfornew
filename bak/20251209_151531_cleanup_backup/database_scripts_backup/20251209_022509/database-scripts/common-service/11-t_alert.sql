-- ============================================================
-- IOE-DREAM Common Service - Monitor模块
-- 表名: t_alert (告警表)
-- 功能: 告警记录管理
-- 创建时间: 2025-12-02
-- ============================================================

CREATE TABLE IF NOT EXISTS `t_alert` (
    `alert_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '告警ID',
    `alert_level` VARCHAR(20) NOT NULL COMMENT '告警级别：INFO/WARNING/ERROR/CRITICAL',
    `alert_title` VARCHAR(200) NOT NULL COMMENT '告警标题',
    `alert_message` TEXT NOT NULL COMMENT '告警消息',
    `service_name` VARCHAR(100) COMMENT '服务名称',
    `instance_id` VARCHAR(100) COMMENT '实例ID',
    `metric_name` VARCHAR(100) COMMENT '监控指标名称',
    `metric_value` DOUBLE COMMENT '指标值',
    `threshold_value` DOUBLE COMMENT '阈值',
    `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/RESOLVED/SUPPRESSED',
    `resolution_notes` TEXT COMMENT '解决说明',
    `resolved_time` DATETIME COMMENT '解决时间',
    `resolved_user_id` BIGINT COMMENT '解决人ID',
    `rule_id` BIGINT COMMENT '关联告警规则ID',
    `tags` VARCHAR(500) COMMENT '标签',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    PRIMARY KEY (`alert_id`),
    KEY `idx_alert_level` (`alert_level`),
    KEY `idx_service_name` (`service_name`),
    KEY `idx_status` (`status`),
    KEY `idx_rule_id` (`rule_id`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_deleted_flag` (`deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='告警表';

