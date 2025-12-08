-- ==================================================================
-- IOE-DREAM 菜单初始化简化版 (移除IF语句)
-- ==================================================================
-- 版本: v2.1.0
-- 创建日期: 2025-12-08
-- 说明: 移除IF语句的兼容版本，可以在MySQL命令行直接执行
-- 执行方式: type "01-init-menus-simplified.sql" | docker exec -i ioedream-mysql mysql -uroot -proot1234 ioedream
-- ==================================================================

USE ioedream;

-- 开启事务
START TRANSACTION;

-- ==================================================================
-- 第一部分: 顶级目录初始化
-- ==================================================================

-- 1. 业务管理（顶级目录）
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, 
  `perms_type`, `api_perms`, `web_perms`, `icon`, 
  `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, 
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES (
  '业务管理', 1, 0, 10, '/business', NULL, 
  1, NULL, NULL, 'AppstoreOutlined',
  NULL, 0, NULL, 0,
  1, 0, 0,
  1, NOW(), 1, NOW()
) ON DUPLICATE KEY UPDATE 
  menu_id = LAST_INSERT_ID(menu_id), menu_name = VALUES(menu_name), sort = VALUES(sort);

-- 2. 系统设置（顶级目录）
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, 
  `perms_type`, `api_perms`, `web_perms`, `icon`, 
  `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, 
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES (
  '系统设置', 1, 0, 90, '/system', NULL, 
  1, NULL, NULL, 'SettingOutlined',
  NULL, 0, NULL, 0,
  1, 0, 0,
  1, NOW(), 1, NOW()
) ON DUPLICATE KEY UPDATE 
  menu_id = LAST_INSERT_ID(menu_id), menu_name = VALUES(menu_name), sort = VALUES(sort);

-- 3. 支撑功能（顶级目录）
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, 
  `perms_type`, `api_perms`, `web_perms`, `icon`, 
  `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, 
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES (
  '支撑功能', 1, 0, 95, '/support', NULL, 
  1, NULL, NULL, 'ToolOutlined',
  NULL, 0, NULL, 0,
  1, 0, 0,
  1, NOW(), 1, NOW()
) ON DUPLICATE KEY UPDATE 
  menu_id = LAST_INSERT_ID(menu_id), menu_name = VALUES(menu_name), sort = VALUES(sort);

-- ==================================================================
-- 第二部分: 业务管理模块 (简化)
-- ==================================================================

-- 2.1 门禁管理
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('门禁管理', 1, (SELECT menu_id FROM t_menu WHERE menu_name = '业务管理' AND parent_id = 0 AND deleted_flag = 0 LIMIT 1), 10, '/business/access', NULL, 1, NULL, NULL, 'LockOutlined', NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE menu_id = LAST_INSERT_ID(menu_id), parent_id = VALUES(parent_id), sort = VALUES(sort);

INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('设备管理', 2, (SELECT menu_id FROM t_menu WHERE menu_name = '门禁管理' AND deleted_flag = 0 LIMIT 1), 1, '/business/access/device', '/business/access/device/index.vue', 1, NULL, 'business:access:device', 'VideoCameraOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE menu_id = LAST_INSERT_ID(menu_id), parent_id = VALUES(parent_id);

INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('通行记录', 2, (SELECT menu_id FROM t_menu WHERE menu_name = '门禁管理' AND deleted_flag = 0 LIMIT 1), 2, '/business/access/record', '/business/access/record/index.vue', 1, NULL, 'business:access:record', 'HistoryOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE menu_id = LAST_INSERT_ID(menu_id), parent_id = VALUES(parent_id);

-- 2.2 考勤管理
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('考勤管理', 1, (SELECT menu_id FROM t_menu WHERE menu_name = '业务管理' AND parent_id = 0 AND deleted_flag = 0 LIMIT 1), 20, '/business/attendance', NULL, 1, NULL, NULL, 'ClockCircleOutlined', NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE menu_id = LAST_INSERT_ID(menu_id), parent_id = VALUES(parent_id), sort = VALUES(sort);

INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('考勤记录', 2, (SELECT menu_id FROM t_menu WHERE menu_name = '考勤管理' AND deleted_flag = 0 LIMIT 1), 1, '/business/attendance/record', '/business/attendance/record/index.vue', 1, NULL, 'business:attendance:record', 'ReconciliationOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE menu_id = LAST_INSERT_ID(menu_id), parent_id = VALUES(parent_id);

-- 2.3 消费管理
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('消费管理', 1, (SELECT menu_id FROM t_menu WHERE menu_name = '业务管理' AND parent_id = 0 AND deleted_flag = 0 LIMIT 1), 30, '/business/consume', NULL, 1, NULL, NULL, 'ShoppingOutlined', NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE menu_id = LAST_INSERT_ID(menu_id), parent_id = VALUES(parent_id), sort = VALUES(sort);

INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('账户管理', 2, (SELECT menu_id FROM t_menu WHERE menu_name = '消费管理' AND deleted_flag = 0 LIMIT 1), 1, '/business/consume/account', '/business/consume/account/index.vue', 1, NULL, 'business:consume:account', 'WalletOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE menu_id = LAST_INSERT_ID(menu_id), parent_id = VALUES(parent_id);

INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('交易记录', 2, (SELECT menu_id FROM t_menu WHERE menu_name = '消费管理' AND deleted_flag = 0 LIMIT 1), 2, '/business/consume/transaction', '/business/consume/transaction/index.vue', 1, NULL, 'business:consume:transaction', 'TransactionOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE menu_id = LAST_INSERT_ID(menu_id), parent_id = VALUES(parent_id);

INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('消费报表', 2, (SELECT menu_id FROM t_menu WHERE menu_name = '消费管理' AND deleted_flag = 0 LIMIT 1), 3, '/business/consume/report', '/business/consume/report/index.vue', 1, NULL, 'business:consume:report', 'BarChartOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE menu_id = LAST_INSERT_ID(menu_id), parent_id = VALUES(parent_id);

-- 2.4 访客管理
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('访客管理', 1, (SELECT menu_id FROM t_menu WHERE menu_name = '业务管理' AND parent_id = 0 AND deleted_flag = 0 LIMIT 1), 40, '/business/visitor', NULL, 1, NULL, NULL, 'TeamOutlined', NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE menu_id = LAST_INSERT_ID(menu_id), parent_id = VALUES(parent_id), sort = VALUES(sort);

INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('访客预约', 2, (SELECT menu_id FROM t_menu WHERE menu_name = '访客管理' AND deleted_flag = 0 LIMIT 1), 1, '/business/visitor/appointment', '/business/visitor/appointment.vue', 1, NULL, 'business:visitor:appointment', 'CalendarOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE menu_id = LAST_INSERT_ID(menu_id), parent_id = VALUES(parent_id);

INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('访客登记', 2, (SELECT menu_id FROM t_menu WHERE menu_name = '访客管理' AND deleted_flag = 0 LIMIT 1), 2, '/business/visitor/registration', '/business/visitor/registration.vue', 1, NULL, 'business:visitor:registration', 'FormOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE menu_id = LAST_INSERT_ID(menu_id), parent_id = VALUES(parent_id);

INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('访客记录', 2, (SELECT menu_id FROM t_menu WHERE menu_name = '访客管理' AND deleted_flag = 0 LIMIT 1), 3, '/business/visitor/record', '/business/visitor/record.vue', 1, NULL, 'business:visitor:record', 'HistoryOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE menu_id = LAST_INSERT_ID(menu_id), parent_id = VALUES(parent_id);

-- 2.5 智能视频
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('智能视频', 1, (SELECT menu_id FROM t_menu WHERE menu_name = '业务管理' AND parent_id = 0 AND deleted_flag = 0 LIMIT 1), 50, '/business/smart-video', NULL, 1, NULL, NULL, 'VideoCameraOutlined', NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE menu_id = LAST_INSERT_ID(menu_id), parent_id = VALUES(parent_id), sort = VALUES(sort);

-- 2.6 OA办公
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('OA办公', 1, (SELECT menu_id FROM t_menu WHERE menu_name = '业务管理' AND parent_id = 0 AND deleted_flag = 0 LIMIT 1), 60, '/business/oa', NULL, 1, NULL, NULL, 'FileTextOutlined', NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE menu_id = LAST_INSERT_ID(menu_id), parent_id = VALUES(parent_id), sort = VALUES(sort);

-- 2.7 ERP管理
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('ERP管理', 1, (SELECT menu_id FROM t_menu WHERE menu_name = '业务管理' AND parent_id = 0 AND deleted_flag = 0 LIMIT 1), 70, '/business/erp', NULL, 1, NULL, NULL, 'ShopOutlined', NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE menu_id = LAST_INSERT_ID(menu_id), parent_id = VALUES(parent_id), sort = VALUES(sort);

-- ==================================================================
-- 第三部分: 系统设置模块
-- ==================================================================

INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('员工管理', 2, (SELECT menu_id FROM t_menu WHERE menu_name = '系统设置' AND parent_id = 0 AND deleted_flag = 0 LIMIT 1), 10, '/system/employee', '/system/employee/index.vue', 1, NULL, 'system:employee', 'UserOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE menu_id = LAST_INSERT_ID(menu_id), parent_id = VALUES(parent_id);

INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('部门管理', 2, (SELECT menu_id FROM t_menu WHERE menu_name = '系统设置' AND parent_id = 0 AND deleted_flag = 0 LIMIT 1), 20, '/system/department', '/system/department/index.vue', 1, NULL, 'system:department', 'ApartmentOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE menu_id = LAST_INSERT_ID(menu_id), parent_id = VALUES(parent_id);

INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('角色管理', 2, (SELECT menu_id FROM t_menu WHERE menu_name = '系统设置' AND parent_id = 0 AND deleted_flag = 0 LIMIT 1), 30, '/system/role', '/system/role/index.vue', 1, NULL, 'system:role', 'TeamOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE menu_id = LAST_INSERT_ID(menu_id), parent_id = VALUES(parent_id);

INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('菜单管理', 2, (SELECT menu_id FROM t_menu WHERE menu_name = '系统设置' AND parent_id = 0 AND deleted_flag = 0 LIMIT 1), 40, '/system/menu', '/system/menu/index.vue', 1, NULL, 'system:menu', 'MenuOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE menu_id = LAST_INSERT_ID(menu_id), parent_id = VALUES(parent_id);

-- ==================================================================
-- 第四部分: 支撑功能模块
-- ==================================================================

INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('数据字典', 2, (SELECT menu_id FROM t_menu WHERE menu_name = '支撑功能' AND parent_id = 0 AND deleted_flag = 0 LIMIT 1), 10, '/support/dict', '/support/dict/index.vue', 1, NULL, 'support:dict', 'BookOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE menu_id = LAST_INSERT_ID(menu_id), parent_id = VALUES(parent_id);

INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('系统配置', 2, (SELECT menu_id FROM t_menu WHERE menu_name = '支撑功能' AND parent_id = 0 AND deleted_flag = 0 LIMIT 1), 20, '/support/config', '/support/config/index.vue', 1, NULL, 'support:config', 'SettingOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE menu_id = LAST_INSERT_ID(menu_id), parent_id = VALUES(parent_id);

INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('文件服务', 2, (SELECT menu_id FROM t_menu WHERE menu_name = '支撑功能' AND parent_id = 0 AND deleted_flag = 0 LIMIT 1), 30, '/support/file', '/support/file/index.vue', 1, NULL, 'support:file', 'FileOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE menu_id = LAST_INSERT_ID(menu_id), parent_id = VALUES(parent_id);

-- ==================================================================
-- 第五部分: 角色菜单关联
-- ==================================================================

-- 为管理员角色分配所有菜单
INSERT INTO `t_role_menu` (`role_id`, `menu_id`) 
SELECT 1, menu_id FROM t_menu WHERE deleted_flag = 0
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

-- 提交事务
COMMIT;

-- ==================================================================
-- 验证数据
-- ==================================================================
SELECT '✅ 菜单初始化完成！' AS status;
SELECT COUNT(*) AS 总菜单数 FROM t_menu WHERE deleted_flag = 0;
SELECT 
  menu_name AS 顶级菜单, 
  COUNT(*) AS 子菜单数 
FROM t_menu m1 
LEFT JOIN t_menu m2 ON m2.parent_id = m1.menu_id AND m2.deleted_flag = 0
WHERE m1.parent_id = 0 AND m1.deleted_flag = 0 
GROUP BY m1.menu_id, m1.menu_name 
ORDER BY m1.sort;
