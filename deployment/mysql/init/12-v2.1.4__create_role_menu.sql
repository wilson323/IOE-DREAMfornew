-- =====================================================
-- IOE-DREAM 数据库迁移脚本
-- 版本: V2.1.4
-- 描述: 创建角色菜单关联表 t_role_menu（与RoleMenuEntity一致）
-- =====================================================

USE ioedream;

CREATE TABLE IF NOT EXISTS t_role_menu (
    role_menu_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '关联ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    UNIQUE KEY uk_role_menu (role_id, menu_id, deleted_flag),
    KEY idx_role_id (role_id),
    KEY idx_menu_id (menu_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色菜单关联表';

SELECT 'V2.1.4 t_role_menu 创建完成' AS status;
