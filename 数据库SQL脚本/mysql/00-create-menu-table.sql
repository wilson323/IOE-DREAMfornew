-- ==================================================================
-- IOE-DREAM 菜单表结构创建SQL脚本
-- ==================================================================
-- 版本: v1.0.0
-- 创建日期: 2025-12-08
-- 说明: 创建菜单管理所需的所有表结构
-- 执行方式: type "00-create-menu-table.sql" | docker exec -i ioedream-mysql mysql -uroot -proot1234 ioedream
-- ==================================================================

USE ioedream;

-- 删除已存在的表（谨慎使用）
-- DROP TABLE IF EXISTS `t_role_menu`;
-- DROP TABLE IF EXISTS `t_menu`;

-- ==================================================================
-- 表1: t_menu (菜单表)
-- ==================================================================
CREATE TABLE IF NOT EXISTS `t_menu` (
  `menu_id` bigint NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `menu_name` varchar(200) NOT NULL COMMENT '菜单名称',
  `menu_type` tinyint NOT NULL COMMENT '菜单类型 1-目录 2-菜单 3-功能点',
  `parent_id` bigint NOT NULL DEFAULT '0' COMMENT '父菜单ID',
  `sort` int NOT NULL DEFAULT '0' COMMENT '显示顺序',
  `path` varchar(255) DEFAULT NULL COMMENT '路由地址',
  `component` varchar(255) DEFAULT NULL COMMENT '组件路径',
  `perms_type` tinyint NOT NULL DEFAULT '1' COMMENT '权限类型 1-前端后端都有 2-仅后端',
  `api_perms` text COMMENT '后端权限字符串',
  `web_perms` varchar(255) DEFAULT NULL COMMENT '前端权限字符串',
  `icon` varchar(100) DEFAULT NULL COMMENT '菜单图标',
  `context_menu_id` bigint DEFAULT NULL COMMENT '右键菜单ID',
  `frame_flag` tinyint NOT NULL DEFAULT '0' COMMENT '是否为iframe 0-否 1-是',
  `frame_url` text COMMENT 'iframe地址',
  `cache_flag` tinyint NOT NULL DEFAULT '0' COMMENT '是否缓存 0-否 1-是',
  `visible_flag` tinyint NOT NULL DEFAULT '1' COMMENT '是否显示 0-隐藏 1-显示',
  `disabled_flag` tinyint NOT NULL DEFAULT '0' COMMENT '是否禁用 0-否 1-是',
  `deleted_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记 0-未删除 1-已删除',
  `create_user_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user_id` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`menu_id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_menu_type` (`menu_type`),
  KEY `idx_visible_flag` (`visible_flag`),
  KEY `idx_deleted_flag` (`deleted_flag`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜单表';

-- ==================================================================
-- 表2: t_role (角色表) - 如果不存在则创建
-- ==================================================================
CREATE TABLE IF NOT EXISTS `t_role` (
  `role_id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(100) NOT NULL COMMENT '角色名称',
  `role_code` varchar(50) DEFAULT NULL COMMENT '角色编码',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `deleted_flag` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记 0-未删除 1-已删除',
  `create_user_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user_id` bigint DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`role_id`),
  UNIQUE KEY `uk_role_code` (`role_code`),
  KEY `idx_deleted_flag` (`deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- ==================================================================
-- 表3: t_role_menu (角色菜单关联表)
-- ==================================================================
CREATE TABLE IF NOT EXISTS `t_role_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_menu` (`role_id`,`menu_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_menu_id` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色菜单关联表';

-- ==================================================================
-- 初始化管理员角色（如果不存在）
-- ==================================================================
INSERT INTO `t_role` (`role_id`, `role_name`, `role_code`, `remark`, `create_user_id`, `create_time`) 
VALUES (1, '超级管理员', 'ADMIN', '系统超级管理员，拥有所有权限', 1, NOW())
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name);

-- ==================================================================
-- 验证表创建
-- ==================================================================
SELECT '✅ 菜单表创建完成' AS status;
SELECT TABLE_NAME, TABLE_COMMENT, CREATE_TIME 
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'ioedream' 
  AND TABLE_NAME IN ('t_menu', 't_role', 't_role_menu')
ORDER BY TABLE_NAME;

SELECT '✅ 表结构创建成功！可以执行菜单数据初始化脚本了' AS message;
