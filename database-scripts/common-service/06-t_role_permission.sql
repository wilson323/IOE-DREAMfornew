-- ============================================================
-- IOE-DREAM Common Service - Identity模块
-- 表名: t_role_permission (角色权限关联表)
-- 功能: 角色和权限的多对多关系
-- 创建时间: 2025-12-02
-- ============================================================

CREATE TABLE IF NOT EXISTS `t_role_permission` (
    `role_permission_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色权限ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `permission_id` BIGINT NOT NULL COMMENT '权限ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    PRIMARY KEY (`role_permission_id`),
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
    KEY `idx_role_id` (`role_id`),
    KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

