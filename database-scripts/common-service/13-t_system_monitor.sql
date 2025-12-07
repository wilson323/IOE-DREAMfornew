-- ============================================================
-- IOE-DREAM Common Service - Monitor模块
-- 表名: t_system_monitor (系统监控表)
-- 功能: 系统监控数据记录
-- 创建时间: 2025-12-02
-- ============================================================

CREATE TABLE IF NOT EXISTS `t_system_monitor` (
    `monitor_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '监控ID',
    `service_name` VARCHAR(100) NOT NULL COMMENT '服务名称',
    `instance_id` VARCHAR(100) COMMENT '服务实例ID',
    `monitor_type` VARCHAR(50) NOT NULL COMMENT '监控类型：CPU/MEMORY/DISK/NETWORK/HEALTH',
    `metric_name` VARCHAR(100) NOT NULL COMMENT '监控指标名称',
    `metric_value` DOUBLE NOT NULL COMMENT '监控值',
    `metric_unit` VARCHAR(20) COMMENT '监控单位',
    `status` VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT '状态：NORMAL/WARNING/CRITICAL',
    `alert_threshold` DOUBLE COMMENT '告警阈值',
    `monitor_time` DATETIME NOT NULL COMMENT '监控时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    PRIMARY KEY (`monitor_id`),
    KEY `idx_service_name` (`service_name`),
    KEY `idx_monitor_type` (`monitor_type`),
    KEY `idx_metric_name` (`metric_name`),
    KEY `idx_status` (`status`),
    KEY `idx_monitor_time` (`monitor_time`),
    KEY `idx_deleted_flag` (`deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统监控表';

