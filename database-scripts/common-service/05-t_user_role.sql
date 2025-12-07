-- ============================================================
-- IOE-DREAM Common Service - Identity模块
-- 表名: t_user_role (用户角色关联表)
-- 功能: 用户和角色的多对多关系
-- 创建时间: 2025-12-02
-- ============================================================

CREATE TABLE IF NOT EXISTS `t_user_role` (
    `user_role_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户角色ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    PRIMARY KEY (`user_role_id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

