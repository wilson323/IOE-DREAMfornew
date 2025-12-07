-- ============================================================
-- IOE-DREAM Common Service - System模块
-- 表名: t_system_dict (系统字典表)
-- 功能: 系统字典数据管理
-- 创建时间: 2025-12-02
-- ============================================================

CREATE TABLE IF NOT EXISTS `t_system_dict` (
    `dict_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '字典ID',
    `dict_type` VARCHAR(50) NOT NULL COMMENT '字典类型',
    `dict_label` VARCHAR(100) NOT NULL COMMENT '字典标签',
    `dict_value` VARCHAR(100) NOT NULL COMMENT '字典值',
    `dict_sort` INT NOT NULL DEFAULT 0 COMMENT '字典排序',
    `css_class` VARCHAR(100) COMMENT 'CSS类名',
    `list_class` VARCHAR(100) COMMENT '列表样式',
    `is_default` TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认：0-否 1-是',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用 2-禁用',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    PRIMARY KEY (`dict_id`),
    KEY `idx_dict_type` (`dict_type`),
    KEY `idx_dict_value` (`dict_value`),
    KEY `idx_status` (`status`),
    KEY `idx_deleted_flag` (`deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统字典表';

