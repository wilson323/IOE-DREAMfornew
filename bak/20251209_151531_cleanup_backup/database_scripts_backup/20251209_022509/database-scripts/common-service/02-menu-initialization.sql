-- =====================================================
-- IOE-DREAM 智慧园区一卡通管理平台
-- 菜单系统初始化SQL脚本
--
-- 基于SmartAdmin最佳实践
-- 支持零配置自动初始化
--
-- 创建时间: 2025-12-08
-- 版本: v1.0.0
-- =====================================================

-- 删除已存在的菜单表（如果存在强制重建）
-- DROP TABLE IF EXISTS `t_menu`;

-- 创建菜单表
CREATE TABLE IF NOT EXISTS `t_menu` (
    `menu_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
    `menu_name` VARCHAR(100) NOT NULL COMMENT '菜单名称',
    `menu_type` TINYINT NOT NULL COMMENT '菜单类型：1-目录 2-菜单 3-功能',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父菜单ID',
    `sort_order` INT DEFAULT 0 COMMENT '菜单排序',
    `menu_path` VARCHAR(200) DEFAULT NULL COMMENT '菜单路径',
    `component_path` VARCHAR(200) DEFAULT NULL COMMENT '组件路径',
    `menu_icon` VARCHAR(100) DEFAULT NULL COMMENT '菜单图标',
    `permission` VARCHAR(200) DEFAULT NULL COMMENT '权限标识',
    `route_params` TEXT DEFAULT NULL COMMENT '路由参数（JSON格式）',
    `is_external` TINYINT DEFAULT 0 COMMENT '是否外链：0-否 1-是',
    `is_cache` TINYINT DEFAULT 1 COMMENT '是否缓存：0-否 1-是',
    `is_visible` TINYINT DEFAULT 1 COMMENT '是否显示：0-否 1-是',
    `is_disabled` TINYINT DEFAULT 0 COMMENT '是否禁用：0-否 1-是',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `menu_level` TINYINT DEFAULT 1 COMMENT '菜单等级',
    `menu_code` VARCHAR(50) DEFAULT NULL COMMENT '菜单编码',
    `module` VARCHAR(50) DEFAULT NULL COMMENT '业务模块',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_user_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_user_id` BIGINT DEFAULT NULL COMMENT '更新人ID',
    `deleted_flag` TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    `version` INT DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`menu_id`),
    UNIQUE KEY `uk_menu_code` (`menu_code`),
    KEY `idx_menu_parent_id` (`parent_id`),
    KEY `idx_menu_type` (`menu_type`),
    KEY `idx_menu_sort` (`sort_order`),
    KEY `idx_menu_deleted_flag` (`deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜单表';

-- 清空现有数据（如果要重新初始化）
-- DELETE FROM `t_menu`;
-- ALTER TABLE `t_menu` AUTO_INCREMENT = 1;

-- =====================================================
-- 1. 系统管理模块菜单
-- =====================================================

-- 系统管理目录
INSERT INTO `t_menu` (
    `menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort_order`,
    `menu_path`, `menu_icon`, `menu_level`, `menu_code`, `module`,
    `is_visible`, `is_disabled`
) VALUES (
    1, '系统管理', 1, 0, 1,
    '/system', 'SettingOutlined', 1, 'SYSTEM_MANAGEMENT', 'system',
    1, 0
);

-- 用户管理
INSERT INTO `t_menu` (
    `menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort_order`,
    `menu_path`, `component_path`, `menu_icon`, `permission`,
    `menu_level`, `menu_code`, `module`,
    `is_visible`, `is_disabled`
) VALUES (
    2, '用户管理', 2, 1, 1,
    '/system/user', 'system/user/index', 'UserOutlined', 'system:user:list',
    2, 'SYSTEM_USER_MANAGE', 'system',
    1, 0
);

-- 用户管理功能权限
INSERT INTO `t_menu` (
    `menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort_order`,
    `permission`, `menu_level`, `menu_code`, `module`,
    `is_visible`, `is_disabled`
) VALUES
(21, '用户新增', 3, 2, 1, 'system:user:add', 3, 'SYSTEM_USER_ADD', 'system', 0, 0),
(22, '用户编辑', 3, 2, 2, 'system:user:edit', 3, 'SYSTEM_USER_EDIT', 'system', 0, 0),
(23, '用户删除', 3, 2, 3, 'system:user:delete', 3, 'SYSTEM_USER_DELETE', 'system', 0, 0),
(24, '用户导出', 3, 2, 4, 'system:user:export', 3, 'SYSTEM_USER_EXPORT', 'system', 0, 0);

-- 角色管理
INSERT INTO `t_menu` (
    `menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort_order`,
    `menu_path`, `component_path`, `menu_icon`, `permission`,
    `menu_level`, `menu_code`, `module`,
    `is_visible`, `is_disabled`
) VALUES (
    3, '角色管理', 2, 1, 2,
    '/system/role', 'system/role/index', 'TeamOutlined', 'system:role:list',
    2, 'SYSTEM_ROLE_MANAGE', 'system',
    1, 0
);

-- 角色管理功能权限
INSERT INTO `t_menu` (
    `menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort_order`,
    `permission`, `menu_level`, `menu_code`, `module`,
    `is_visible`, `is_disabled`
) VALUES
(31, '角色新增', 3, 3, 1, 'system:role:add', 3, 'SYSTEM_ROLE_ADD', 'system', 0, 0),
(32, '角色编辑', 3, 3, 2, 'system:role:edit', 3, 'SYSTEM_ROLE_EDIT', 'system', 0, 0),
(33, '角色删除', 3, 3, 3, 'system:role:delete', 3, 'SYSTEM_ROLE_DELETE', 'system', 0, 0),
(34, '角色权限设置', 3, 3, 4, 'system:role:permission', 3, 'SYSTEM_ROLE_PERMISSION', 'system', 0, 0);

-- 菜单管理
INSERT INTO `t_menu` (
    `menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort_order`,
    `menu_path`, `component_path`, `menu_icon`, `permission`,
    `menu_level`, `menu_code`, `module`,
    `is_visible`, `is_disabled`
) VALUES (
    4, '菜单管理', 2, 1, 3,
    '/system/menu', 'system/menu/index', 'MenuOutlined', 'system:menu:list',
    2, 'SYSTEM_MENU_MANAGE', 'system',
    1, 0
);

-- 菜单管理功能权限
INSERT INTO `t_menu` (
    `menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort_order`,
    `permission`, `menu_level`, `menu_code`, `module`,
    `is_visible`, `is_disabled`
) VALUES
(41, '菜单新增', 3, 4, 1, 'system:menu:add', 3, 'SYSTEM_MENU_ADD', 'system', 0, 0),
(42, '菜单编辑', 3, 4, 2, 'system:menu:edit', 3, 'SYSTEM_MENU_EDIT', 'system', 0, 0),
(43, '菜单删除', 3, 4, 3, 'system:menu:delete', 3, 'SYSTEM_MENU_DELETE', 'system', 0, 0);

-- =====================================================
-- 2. 门禁管理模块菜单
-- =====================================================

-- 门禁管理目录
INSERT INTO `t_menu` (
    `menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort_order`,
    `menu_path`, `menu_icon`, `menu_level`, `menu_code`, `module`,
    `is_visible`, `is_disabled`
) VALUES (
    10, '门禁管理', 1, 0, 2,
    '/access', 'SafetyOutlined', 1, 'ACCESS_CONTROL', 'access',
    1, 0
);

-- 通行记录
INSERT INTO `t_menu` (
    `menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort_order`,
    `menu_path`, `component_path`, `menu_icon`, `permission`,
    `menu_level`, `menu_code`, `module`,
    `is_visible`, `is_disabled`
) VALUES (
    11, '通行记录', 2, 10, 1,
    '/access/record', 'access/record/index', 'FileSearchOutlined', 'access:record:list',
    2, 'ACCESS_RECORD', 'access',
    1, 0
);

-- 通行记录功能权限
INSERT INTO `t_menu` (
    `menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort_order`,
    `permission`, `menu_level`, `menu_code`, `module`,
    `is_visible`, `is_disabled`
) VALUES
(111, '通行记录查询', 3, 11, 1, 'access:record:query', 3, 'ACCESS_RECORD_QUERY', 'access', 0, 0),
(112, '通行记录导出', 3, 11, 2, 'access:record:export', 3, 'ACCESS_RECORD_EXPORT', 'access', 0, 0);

-- 门禁设备管理
INSERT INTO `t_menu` (
    `menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort_order`,
    `menu_path`, `component_path`, `menu_icon`, `permission`,
    `menu_level`, `menu_code`, `module`,
    `is_visible`, `is_disabled`
) VALUES (
    12, '门禁设备', 2, 10, 2,
    '/access/device', 'access/device/index', 'LaptopOutlined', 'access:device:list',
    2, 'ACCESS_DEVICE', 'access',
    1, 0
);

-- 门禁设备功能权限
INSERT INTO `t_menu` (
    `menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort_order`,
    `permission`, `menu_level`, `menu_code`, `module`,
    `is_visible`, `is_disabled`
) VALUES
(121, '门禁设备新增', 3, 12, 1, 'access:device:add', 3, 'ACCESS_DEVICE_ADD', 'access', 0, 0),
(122, '门禁设备编辑', 3, 12, 2, 'access:device:edit', 3, 'ACCESS_DEVICE_EDIT', 'access', 0, 0),
(123, '门禁设备删除', 3, 12, 3, 'access:device:delete', 3, 'ACCESS_DEVICE_DELETE', 'access', 0, 0);

-- =====================================================
-- 3. 考勤管理模块菜单
-- =====================================================

-- 考勤管理目录
INSERT INTO `t_menu` (
    `menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort_order`,
    `menu_path`, `menu_icon`, `menu_level`, `menu_code`, `module`,
    `is_visible`, `is_disabled`
) VALUES (
    20, '考勤管理', 1, 0, 3,
    '/attendance', 'CalendarOutlined', 1, 'ATTENDANCE', 'attendance',
    1, 0
);

-- 考勤记录
INSERT INTO `t_menu` (
    `menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort_order`,
    `menu_path`, `component_path`, `menu_icon`, `permission`,
    `menu_level`, `menu_code`, `module`,
    `is_visible`, `is_disabled`
) VALUES (
    21, '考勤记录', 2, 20, 1,
    '/attendance/record', 'attendance/record/index', 'HistoryOutlined', 'attendance:record:list',
    2, 'ATTENDANCE_RECORD', 'attendance',
    1, 0
);

-- 考勤记录功能权限
INSERT INTO `t_menu` (
    `menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort_order`,
    `permission`, `menu_level`, `menu_code`, `module`,
    `is_visible`, `is_disabled`
) VALUES
(211, '考勤记录查询', 3, 21, 1, 'attendance:record:query', 3, 'ATTENDANCE_RECORD_QUERY', 'attendance', 0, 0),
(212, '考勤记录导出', 3, 21, 2, 'attendance:record:export', 3, 'ATTENDANCE_RECORD_EXPORT', 'attendance', 0, 0),
(213, '考勤补录', 3, 21, 3, 'attendance:record:supplement', 3, 'ATTENDANCE_RECORD_SUPPLEMENT', 'attendance', 0, 0);

-- 排班管理
INSERT INTO `t_menu` (
    `menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort_order`,
    `menu_path`, `component_path`, `menu_icon`, `permission`,
    `menu_level`, `menu_code`, `module`,
    `is_visible`, `is_disabled`
) VALUES (
    22, '排班管理', 2, 20, 2,
    '/attendance/shift', 'attendance/shift/index', 'ScheduleOutlined', 'attendance:shift:list',
    2, 'ATTENDANCE_SHIFT', 'attendance',
    1, 0
);

-- =====================================================
-- 4. 消费管理模块菜单
-- =====================================================

-- 消费管理目录
INSERT INTO `t_menu` (
    `menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort_order`,
    `menu_path`, `menu_icon`, `menu_level`, `menu_code`, `module`,
    `is_visible`, `is_disabled`
) VALUES (
    30, '消费管理', 1, 0, 4,
    '/consume', 'WalletOutlined', 1, 'CONSUME', 'consume',
    1, 0
);

-- 消费记录
INSERT INTO `t_menu` (
    `menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort_order`,
    `menu_path`, `component_path`, `menu_icon`, `permission`,
    `menu_level`, `menu_code`, `module`,
    `is_visible`, `is_disabled`
) VALUES (
    31, '消费记录', 2, 30, 1,
    '/consume/record', 'consume/record/index', 'ShoppingOutlined', 'consume:record:list',
    2, 'CONSUME_RECORD', 'consume',
    1, 0
);

-- 消费记录功能权限
INSERT INTO `t_menu` (
    `menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort_order`,
    `permission`, `menu_level`, `menu_code`, `module`,
    `is_visible`, `is_disabled`
) VALUES
(311, '消费记录查询', 3, 31, 1, 'consume:record:query', 3, 'CONSUME_RECORD_QUERY', 'consume', 0, 0),
(312, '消费记录导出', 3, 31, 2, 'consume:record:export', 3, 'CONSUME_RECORD_EXPORT', 'consume', 0, 0),
(313, '消费退款', 3, 31, 3, 'consume:record:refund', 3, 'CONSUME_RECORD_REFUND', 'consume', 0, 0);

-- 账户管理
INSERT INTO `t_menu` (
    `menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort_order`,
    `menu_path`, `component_path`, `menu_icon`, `permission`,
    `menu_level`, `menu_code`, `module`,
    `is_visible`, `is_disabled`
) VALUES (
    32, '账户管理', 2, 30, 2,
    '/consume/account', 'consume/account/index', 'CreditCardOutlined', 'consume:account:list',
    2, 'CONSUME_ACCOUNT', 'consume',
    1, 0
);

-- 充值管理
INSERT INTO `t_menu` (
    `menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort_order`,
    `menu_path`, `component_path`, `menu_icon`, `permission`,
    `menu_level`, `menu_code`, `module`,
    `is_visible`, `is_disabled`
) VALUES (
    33, '充值管理', 2, 30, 3,
    '/consume/recharge', 'consume/recharge/index', 'MoneyCollectOutlined', 'consume:recharge:list',
    2, 'CONSUME_RECHARGE', 'consume',
    1, 0
);

-- =====================================================
-- 5. 访客管理模块菜单
-- =====================================================

-- 访客管理目录
INSERT INTO `t_menu` (
    `menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort_order`,
    `menu_path`, `menu_icon`, `menu_level`, `menu_code`, `module`,
    `is_visible`, `is_disabled`
) VALUES (
    40, '访客管理', 1, 0, 5,
    '/visitor', 'UsergroupAddOutlined', 1, 'VISITOR', 'visitor',
    1, 0
);

-- 访客记录
INSERT INTO `t_menu` (
    `menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort_order`,
    `menu_path`, `component_path`, `menu_icon`, `permission`,
    `menu_level`, `menu_code`, `module`,
    `is_visible`, `is_disabled`
) VALUES (
    41, '访客记录', 2, 40, 1,
    '/visitor/record', 'visitor/record/index', 'UserSwitchOutlined', 'visitor:record:list',
    2, 'VISITOR_RECORD', 'visitor',
    1, 0
);

-- 访客预约
INSERT INTO `t_menu` (
    `menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort_order`,
    `menu_path`, `component_path`, `menu_icon`, `permission`,
    `menu_level`, `menu_code`, `module`,
    `is_visible`, `is_disabled`
) VALUES (
    42, '访客预约', 2, 40, 2,
    '/visitor/appointment', 'visitor/appointment/index', 'CalendarCheckOutlined', 'visitor:appointment:list',
    2, 'VISITOR_APPOINTMENT', 'visitor',
    1, 0
);

-- =====================================================
-- 6. 视频监控模块菜单
-- =====================================================

-- 视频监控目录
INSERT INTO `t_menu` (
    `menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort_order`,
    `menu_path`, `menu_icon`, `menu_level`, `menu_code`, `module`,
    `is_visible`, `is_disabled`
) VALUES (
    50, '视频监控', 1, 0, 6,
    '/video', 'VideoCameraOutlined', 1, 'VIDEO_MONITOR', 'video',
    1, 0
);

-- 实时监控
INSERT INTO `t_menu` (
    `menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort_order`,
    `menu_path`, `component_path`, `menu_icon`, `permission`,
    `menu_level`, `menu_code`, `module`,
    `is_visible`, `is_disabled`
) VALUES (
    51, '实时监控', 2, 50, 1,
    '/video/monitor', 'video/monitor/index', 'EyeOutlined', 'video:monitor:view',
    2, 'VIDEO_MONITOR', 'video',
    1, 0
);

-- 录像回放
INSERT INTO `t_menu` (
    `menu_id`, `menu_name`, `menu_type`, `parent_id`, `sort_order`,
    `menu_path`, `component_path`, `menu_icon`, `permission`,
    `menu_level`, `menu_code`, `module`,
    `is_visible`, `is_disabled`
) VALUES (
    52, '录像回放', 2, 50, 2,
    '/video/playback', 'video/playback/index', 'PlayCircleOutlined', 'video:playback:view',
    2, 'VIDEO_PLAYBACK', 'video',
    1, 0
);

-- =====================================================
-- 初始化角色菜单关联（给管理员角色分配所有菜单权限）
-- =====================================================

-- 假设已存在管理员角色ID为1
INSERT IGNORE INTO `t_role_menu` (`role_id`, `menu_id`, `create_time`, `update_time`)
SELECT 1, menu_id, NOW(), NOW() FROM `t_menu` WHERE deleted_flag = 0;

-- =====================================================
-- 验证初始化结果
-- =====================================================

SELECT
    module AS '业务模块',
    menu_type AS '菜单类型',
    COUNT(*) AS '菜单数量',
    GROUP_CONCAT(menu_name ORDER BY sort_order) AS '菜单列表'
FROM `t_menu`
WHERE deleted_flag = 0
GROUP BY module, menu_type
ORDER BY module, menu_type, sort_order;

-- 显示完整的菜单树结构
SELECT
    CONCAT(REPEAT('--', menu_level), menu_name) AS '菜单树',
    menu_path AS '路径',
    permission AS '权限标识'
FROM `t_menu`
WHERE deleted_flag = 0 AND is_visible = 1
ORDER BY sort_order, menu_id;

-- 统计信息
SELECT
    '初始化完成' AS '状态',
    COUNT(*) AS '总菜单数',
    SUM(CASE WHEN menu_type = 1 THEN 1 ELSE 0 END) AS '目录数',
    SUM(CASE WHEN menu_type = 2 THEN 1 ELSE 0 END) AS '菜单数',
    SUM(CASE WHEN menu_type = 3 THEN 1 ELSE 0 END) AS '功能数',
    SUM(CASE WHEN is_visible = 1 THEN 1 ELSE 0 END) AS '显示数',
    SUM(CASE WHEN is_disabled = 0 THEN 1 ELSE 0 END) AS '启用数'
FROM `t_menu`
WHERE deleted_flag = 0;

COMMIT;