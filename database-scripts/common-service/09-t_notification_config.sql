-- ============================================================
-- IOE-DREAM Common Service - Notification模块
-- 表名: t_notification_config (通知配置表)
-- 功能: 通知渠道配置管理
-- 创建时间: 2025-12-02
-- ============================================================

CREATE TABLE IF NOT EXISTS `t_notification_config` (
    `config_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
    `config_value` TEXT NOT NULL COMMENT '配置值',
    `config_type` VARCHAR(50) NOT NULL COMMENT '配置类型：EMAIL/SMS/WECHAT/PUSH',
    `config_desc` VARCHAR(500) COMMENT '配置描述',
    `is_encrypted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否加密：0-否 1-是',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用 2-禁用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    PRIMARY KEY (`config_id`),
    UNIQUE KEY `uk_config_key` (`config_key`),
    KEY `idx_config_type` (`config_type`),
    KEY `idx_status` (`status`),
    KEY `idx_deleted_flag` (`deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知配置表';

