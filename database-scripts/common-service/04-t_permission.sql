-- ============================================================
-- IOE-DREAM Common Service - Identity模块
-- 表名: t_permission (权限表)
-- 功能: 权限管理
-- 创建时间: 2025-12-02
-- ============================================================

CREATE TABLE IF NOT EXISTS `t_permission` (
    `permission_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限ID',
    `permission_code` VARCHAR(100) NOT NULL COMMENT '权限编码',
    `permission_name` VARCHAR(100) NOT NULL COMMENT '权限名称',
    `permission_type` TINYINT NOT NULL COMMENT '权限类型：1-菜单 2-按钮 3-接口 4-数据',
    `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父权限ID',
    `path` VARCHAR(200) COMMENT '路由路径',
    `component` VARCHAR(200) COMMENT '组件路径',
    `icon` VARCHAR(100) COMMENT '图标',
    `api_path` VARCHAR(200) COMMENT 'API路径',
    `api_method` VARCHAR(20) COMMENT 'API方法：GET/POST/PUT/DELETE',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
    `is_external` TINYINT NOT NULL DEFAULT 0 COMMENT '是否外链：0-否 1-是',
    `is_cache` TINYINT NOT NULL DEFAULT 1 COMMENT '是否缓存：0-否 1-是',
    `is_hidden` TINYINT NOT NULL DEFAULT 0 COMMENT '是否隐藏：0-否 1-是',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用 2-禁用',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    PRIMARY KEY (`permission_id`),
    UNIQUE KEY `uk_permission_code` (`permission_code`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_permission_type` (`permission_type`),
    KEY `idx_status` (`status`),
    KEY `idx_deleted_flag` (`deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

