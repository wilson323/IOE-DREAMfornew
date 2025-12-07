-- ============================================================
-- IOE-DREAM Common Service - Notification模块
-- 表名: t_notification_template (通知模板表)
-- 功能: 通知模板管理
-- 创建时间: 2025-12-02
-- ============================================================

CREATE TABLE IF NOT EXISTS `t_notification_template` (
    `template_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '模板ID',
    `template_code` VARCHAR(50) NOT NULL COMMENT '模板编码',
    `template_name` VARCHAR(100) NOT NULL COMMENT '模板名称',
    `template_type` TINYINT NOT NULL COMMENT '模板类型：1-邮件 2-短信 3-微信 4-站内信 5-推送',
    `subject` VARCHAR(200) COMMENT '主题（邮件用）',
    `content` TEXT NOT NULL COMMENT '模板内容',
    `variables` TEXT COMMENT '变量列表（JSON数组）',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用 2-禁用',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    PRIMARY KEY (`template_id`),
    UNIQUE KEY `uk_template_code` (`template_code`),
    KEY `idx_template_type` (`template_type`),
    KEY `idx_status` (`status`),
    KEY `idx_deleted_flag` (`deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知模板表';

