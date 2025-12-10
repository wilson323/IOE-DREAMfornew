-- ============================================================
-- IOE-DREAM Common Service - Notification模块
-- 表名: t_notification_message (通知消息表)
-- 功能: 通知消息管理
-- 创建时间: 2025-12-02
-- ============================================================

CREATE TABLE IF NOT EXISTS `t_notification_message` (
    `message_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    `message_type` TINYINT NOT NULL COMMENT '消息类型：1-系统通知 2-业务通知 3-告警通知',
    `title` VARCHAR(200) NOT NULL COMMENT '消息标题',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `sender_id` BIGINT COMMENT '发送人ID',
    `sender_name` VARCHAR(100) COMMENT '发送人姓名',
    `recipient_type` TINYINT NOT NULL COMMENT '接收人类型：1-指定用户 2-角色 3-部门 4-全体',
    `recipient_ids` TEXT COMMENT '接收人ID列表（JSON数组）',
    `channel` TINYINT NOT NULL COMMENT '发送渠道：1-站内信 2-邮件 3-短信 4-微信 5-推送',
    `priority` TINYINT NOT NULL DEFAULT 2 COMMENT '优先级：1-低 2-普通 3-高 4-紧急',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-待发送 2-发送中 3-已发送 4-发送失败',
    `send_time` DATETIME COMMENT '发送时间',
    `read_count` INT NOT NULL DEFAULT 0 COMMENT '已读数量',
    `total_count` INT NOT NULL DEFAULT 0 COMMENT '总接收数量',
    `business_type` VARCHAR(50) COMMENT '业务类型',
    `business_id` VARCHAR(100) COMMENT '业务ID',
    `template_id` VARCHAR(50) COMMENT '模板ID',
    `template_params` TEXT COMMENT '模板参数（JSON格式）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    PRIMARY KEY (`message_id`),
    KEY `idx_message_type` (`message_type`),
    KEY `idx_sender_id` (`sender_id`),
    KEY `idx_status` (`status`),
    KEY `idx_send_time` (`send_time`),
    KEY `idx_business` (`business_type`, `business_id`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_deleted_flag` (`deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知消息表';

