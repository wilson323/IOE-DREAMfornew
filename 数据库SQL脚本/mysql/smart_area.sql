-- ============================================================================
-- SmartAdmin 区域管理模块数据库脚本
-- 创建时间: 2025-01-10
-- 描述: 区域管理公共模块,提供统一的区域层级管理、设备分组、人员区域归属等功能
-- ============================================================================

-- 1. 区域表 (t_area)
CREATE TABLE IF NOT EXISTS `t_area` (
    `area_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '区域ID',
    `area_code` VARCHAR(100) NOT NULL COMMENT '区域编码',
    `area_name` VARCHAR(200) NOT NULL COMMENT '区域名称',
    `area_type` VARCHAR(50) NOT NULL COMMENT '区域类型',
    `area_level` INT DEFAULT 1 COMMENT '区域层级',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父区域ID',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `area_config` JSON COMMENT '区域配置JSON',
    `area_desc` TEXT COMMENT '区域描述',
    `manager_id` BIGINT COMMENT '区域负责人ID',
    `contact_phone` VARCHAR(20) COMMENT '联系电话',
    `address` TEXT COMMENT '详细地址',
    `longitude` DECIMAL(11, 8) COMMENT '经度',
    `latitude` DECIMAL(10, 8) COMMENT '纬度',
    `status` TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
    `version` INT DEFAULT 1 COMMENT '版本号（乐观锁）',
    PRIMARY KEY (`area_id`),
    UNIQUE KEY `uk_area_code` (`area_code`),
    INDEX `idx_parent_id` (`parent_id`),
    INDEX `idx_area_type` (`area_type`),
    INDEX `idx_area_level` (`area_level`),
    INDEX `idx_status` (`status`),
    INDEX `idx_manager_id` (`manager_id`),
    INDEX `idx_location` (`longitude`, `latitude`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='区域表';

-- 2. 区域设备关联表 (t_area_device)
CREATE TABLE IF NOT EXISTS `t_area_device` (
    `relation_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID',
    `area_id` BIGINT NOT NULL COMMENT '区域ID',
    `device_id` BIGINT NOT NULL COMMENT '设备ID',
    `device_type` VARCHAR(50) NOT NULL COMMENT '设备类型',
    `bind_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
    `bind_user_id` BIGINT COMMENT '绑定人ID',
    `unbind_time` DATETIME COMMENT '解绑时间',
    `unbind_user_id` BIGINT COMMENT '解绑人ID',
    `bind_remark` TEXT COMMENT '绑定备注',
    `status` TINYINT DEFAULT 1 COMMENT '状态：1-绑定，0-解绑',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag` TINYINT DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
    PRIMARY KEY (`relation_id`),
    INDEX `idx_area_id` (`area_id`),
    INDEX `idx_device_id` (`device_id`),
    INDEX `idx_device_type` (`device_type`),
    INDEX `idx_bind_time` (`bind_time`),
    INDEX `idx_status` (`status`),
    UNIQUE KEY `uk_area_device` (`area_id`, `device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='区域设备关联表';

-- 3. 区域人员关联表 (t_area_user)
CREATE TABLE IF NOT EXISTS `t_area_user` (
    `relation_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID',
    `area_id` BIGINT NOT NULL COMMENT '区域ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `user_type` VARCHAR(20) NOT NULL COMMENT '用户类型',
    `relation_type` VARCHAR(20) NOT NULL COMMENT '关联类型',
    `access_level` TINYINT DEFAULT 1 COMMENT '访问级别',
    `access_time_config` JSON COMMENT '访问时间配置JSON',
    `valid_start_time` DATETIME COMMENT '有效开始时间',
    `valid_end_time` DATETIME COMMENT '有效结束时间',
    `grant_user_id` BIGINT COMMENT '授权人ID',
    `grant_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '授权时间',
    `revoke_user_id` BIGINT COMMENT '撤销人ID',
    `revoke_time` DATETIME COMMENT '撤销时间',
    `grant_remark` TEXT COMMENT '授权备注',
    `status` TINYINT DEFAULT 1 COMMENT '状态：1-有效，0-已撤销',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag` TINYINT DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
    PRIMARY KEY (`relation_id`),
    INDEX `idx_area_id` (`area_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_user_type` (`user_type`),
    INDEX `idx_relation_type` (`relation_type`),
    INDEX `idx_access_level` (`access_level`),
    INDEX `idx_valid_time` (`valid_start_time`, `valid_end_time`),
    INDEX `idx_status` (`status`),
    INDEX `idx_grant_time` (`grant_time`),
    UNIQUE KEY `uk_area_user` (`area_id`, `user_id`, `relation_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='区域人员关联表';

-- 4. 区域配置表 (t_area_config)
CREATE TABLE IF NOT EXISTS `t_area_config` (
    `config_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    `area_id` BIGINT NOT NULL COMMENT '区域ID',
    `config_type` VARCHAR(50) NOT NULL COMMENT '配置类型',
    `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
    `config_value` TEXT COMMENT '配置值',
    `config_desc` TEXT COMMENT '配置描述',
    `is_encrypted` TINYINT DEFAULT 0 COMMENT '是否加密：1-是，0-否',
    `is_default` TINYINT DEFAULT 0 COMMENT '是否默认：1-是，0-否',
    `version` INT DEFAULT 1 COMMENT '配置版本',
    `effective_time` DATETIME COMMENT '生效时间',
    `expire_time` DATETIME COMMENT '过期时间',
    `status` TINYINT DEFAULT 1 COMMENT '状态：1-生效，0-失效',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    `deleted_flag` TINYINT DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
    PRIMARY KEY (`config_id`),
    INDEX `idx_area_id` (`area_id`),
    INDEX `idx_config_type` (`config_type`),
    INDEX `idx_config_key` (`config_key`),
    INDEX `idx_status` (`status`),
    INDEX `idx_effective_time` (`effective_time`),
    INDEX `idx_expire_time` (`expire_time`),
    UNIQUE KEY `uk_area_config` (`area_id`, `config_key`, `version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='区域配置表';

-- 初始化区域类型字典数据
INSERT INTO `t_sys_dict` (`dict_type`, `dict_key`, `dict_value`, `sort_order`, `remark`) VALUES
('AREA_TYPE', 'CAMPUS', '园区', 1, '整个园区'),
('AREA_TYPE', 'BUILDING', '楼栋', 2, '园区内的楼栋'),
('AREA_TYPE', 'FLOOR', '楼层', 3, '楼栋内的楼层'),
('AREA_TYPE', 'ROOM', '房间', 4, '楼层内的房间'),
('AREA_TYPE', 'OUTDOOR', '室外', 5, '室外区域'),
('AREA_TYPE', 'PARKING', '停车场', 6, '停车场'),
('AREA_TYPE', 'ENTRANCE', '出入口', 7, '出入口区域')
ON DUPLICATE KEY UPDATE `dict_value` = VALUES(`dict_value`);

-- 初始化用户类型字典数据
INSERT INTO `t_sys_dict` (`dict_type`, `dict_key`, `dict_value`, `sort_order`, `remark`) VALUES
('USER_TYPE', 'EMPLOYEE', '员工', 1, '内部员工'),
('USER_TYPE', 'VISITOR', '访客', 2, '外部访客'),
('USER_TYPE', 'CONTRACTOR', '承包商', 3, '外部承包商'),
('USER_TYPE', 'SECURITY', '安保', 4, '安保人员')
ON DUPLICATE KEY UPDATE `dict_value` = VALUES(`dict_value`);

-- 初始化关联类型字典数据
INSERT INTO `t_sys_dict` (`dict_type`, `dict_key`, `dict_value`, `sort_order`, `remark`) VALUES
('RELATION_TYPE', 'ACCESS', '访问权限', 1, '可访问该区域'),
('RELATION_TYPE', 'MANAGE', '管理权限', 2, '可管理该区域'),
('RELATION_TYPE', 'MONITOR', '监控权限', 3, '可监控该区域')
ON DUPLICATE KEY UPDATE `dict_value` = VALUES(`dict_value`);

-- 默认区域配置数据
INSERT INTO `t_area_config` (`area_id`, `config_type`, `config_key`, `config_value`, `config_desc`, `is_default`) VALUES
(0, 'AREA_VISIBILITY', 'show_empty_areas', 'true', '是否显示空区域', 1),
(0, 'DEVICE_MANAGEMENT', 'auto_bind_device', 'false', '自动绑定区域内的设备', 1),
(0, 'USER_MANAGEMENT', 'auto_grant_access', 'false', '自动授予区域访问权限', 1),
(0, 'SECURITY_CONFIG', 'access_control_level', '1', '访问控制级别', 1),
(0, 'NOTIFICATION_CONFIG', 'area_entry_notification', 'true', '区域进入通知', 1)
ON DUPLICATE KEY UPDATE `config_value` = VALUES(`config_value`);

-- 插入菜单数据
-- 插入区域管理菜单到"系统设置"(menu_id=50)下
INSERT INTO `t_sys_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms`, `icon`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`) VALUES
('区域管理', 1, 50, 6, 'area', 'system/area/index.vue', 'area:page', 'AreaChartOutlined', 0, NULL, 1, 1, 0, 0)
ON DUPLICATE KEY UPDATE
    menu_type = VALUES(menu_type),
    parent_id = VALUES(parent_id),
    sort = VALUES(sort),
    path = VALUES(path),
    component = VALUES(component),
    perms = VALUES(perms),
    icon = VALUES(icon),
    visible_flag = VALUES(visible_flag);

-- 获取区域管理菜单的ID（如果是重复键更新，LAST_INSERT_ID()会返回0，需要查询）
SET @area_menu_id = LAST_INSERT_ID();
SET @area_menu_id = CASE WHEN @area_menu_id = 0
    THEN (SELECT menu_id FROM t_sys_menu WHERE menu_name = '区域管理' AND deleted_flag = 0 LIMIT 1)
    ELSE @area_menu_id
END;

INSERT INTO `t_sys_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms`, `icon`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`) VALUES
('查询区域', 3, @area_menu_id, 1, NULL, NULL, 'area:page', NULL, 0, NULL, 0, 1, 0, 0),
('区域详情', 3, @area_menu_id, 2, NULL, NULL, 'area:detail', NULL, 0, NULL, 0, 1, 0, 0),
('新增区域', 3, @area_menu_id, 3, NULL, NULL, 'area:add', NULL, 0, NULL, 0, 1, 0, 0),
('修改区域', 3, @area_menu_id, 4, NULL, NULL, 'area:update', NULL, 0, NULL, 0, 1, 0, 0),
('删除区域', 3, @area_menu_id, 5, NULL, NULL, 'area:delete', NULL, 0, NULL, 0, 1, 0, 0),
('区域树', 3, @area_menu_id, 6, NULL, NULL, 'area:tree', NULL, 0, NULL, 0, 1, 0, 0),
('绑定设备', 3, @area_menu_id, 7, NULL, NULL, 'area:device:bind', NULL, 0, NULL, 0, 1, 0, 0),
('解绑设备', 3, @area_menu_id, 8, NULL, NULL, 'area:device:unbind', NULL, 0, NULL, 0, 1, 0, 0),
('授予权限', 3, @area_menu_id, 9, NULL, NULL, 'area:user:grant', NULL, 0, NULL, 0, 1, 0, 0),
('撤销权限', 3, @area_menu_id, 10, NULL, NULL, 'area:user:revoke', NULL, 0, NULL, 0, 1, 0, 0),
('查看配置', 3, @area_menu_id, 11, NULL, NULL, 'area:config:view', NULL, 0, NULL, 0, 1, 0, 0),
('更新配置', 3, @area_menu_id, 12, NULL, NULL, 'area:config:update', NULL, 0, NULL, 0, 1, 0, 0)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 脚本执行完成
SELECT '区域管理模块数据库脚本执行完成!' AS message;
