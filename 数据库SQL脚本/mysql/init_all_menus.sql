-- ==========================================
-- IOE-DREAM 全模块菜单目录初始化SQL脚本
-- ==========================================
-- 版本: v1.0.0
-- 创建日期: 2025-12-08
-- 说明: 初始化所有业务模块的菜单树结构
-- 执行方式: Get-Content init_all_menus.sql -Raw -Encoding UTF8 | docker exec -i mysql-ioe-dream mysql -uroot -proot1234 smart_admin_v3
-- ==========================================

USE smart_admin_v3;

-- 开启事务
START TRANSACTION;

-- ==========================================
-- 一、顶级目录初始化
-- ==========================================

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
  menu_name = VALUES(menu_name),
  sort = VALUES(sort);

SET @business_root_id = LAST_INSERT_ID();

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
  menu_name = VALUES(menu_name),
  sort = VALUES(sort);

SET @system_root_id = LAST_INSERT_ID();

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
  menu_name = VALUES(menu_name),
  sort = VALUES(sort);

SET @support_root_id = LAST_INSERT_ID();

-- ==========================================
-- 二、门禁管理模块（Access Module）
-- ==========================================

-- 2.1 一级菜单：门禁管理
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, 
  `perms_type`, `api_perms`, `web_perms`, `icon`, 
  `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, 
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES (
  '门禁管理', 1, @business_root_id, 10, '/business/access', NULL, 
  1, NULL, NULL, 'LockOutlined',
  NULL, 0, NULL, 0,
  1, 0, 0,
  1, NOW(), 1, NOW()
) ON DUPLICATE KEY UPDATE 
  parent_id = VALUES(parent_id),
  sort = VALUES(sort);

SET @access_menu_id = LAST_INSERT_ID();

-- 2.2 二级菜单：设备管理
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, 
  `perms_type`, `api_perms`, `web_perms`, `icon`, 
  `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, 
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES (
  '设备管理', 2, @access_menu_id, 1, '/business/access/device', '/business/access/device/index.vue', 
  1, NULL, 'business:access:device', 'VideoCameraOutlined',
  NULL, 0, NULL, 1,
  1, 0, 0,
  1, NOW(), 1, NOW()
) ON DUPLICATE KEY UPDATE 
  parent_id = VALUES(parent_id);

SET @access_device_menu_id = LAST_INSERT_ID();

-- 功能点：设备查询
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, 
  `perms_type`, `api_perms`, `web_perms`, `icon`, 
  `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, 
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES 
  ('设备查询', 3, @access_device_menu_id, 1, NULL, NULL, 1, '/access/device/query', 'business:access:device:query', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('设备新增', 3, @access_device_menu_id, 2, NULL, NULL, 1, '/access/device/add', 'business:access:device:add', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('设备编辑', 3, @access_device_menu_id, 3, NULL, NULL, 1, '/access/device/update', 'business:access:device:update', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('设备删除', 3, @access_device_menu_id, 4, NULL, NULL, 1, '/access/device/delete', 'business:access:device:delete', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('设备控制', 3, @access_device_menu_id, 5, NULL, NULL, 1, '/access/device/control', 'business:access:device:control', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 2.3 二级菜单：通行记录
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, 
  `perms_type`, `api_perms`, `web_perms`, `icon`, 
  `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, 
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES (
  '通行记录', 2, @access_menu_id, 2, '/business/access/record', '/business/access/record/index.vue', 
  1, NULL, 'business:access:record', 'HistoryOutlined',
  NULL, 0, NULL, 1,
  1, 0, 0,
  1, NOW(), 1, NOW()
) ON DUPLICATE KEY UPDATE 
  parent_id = VALUES(parent_id);

SET @access_record_menu_id = LAST_INSERT_ID();

-- 功能点：记录查询
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, 
  `perms_type`, `api_perms`, `web_perms`, `icon`, 
  `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, 
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES 
  ('记录查询', 3, @access_record_menu_id, 1, NULL, NULL, 1, '/access/record/query', 'business:access:record:query', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('记录导出', 3, @access_record_menu_id, 2, NULL, NULL, 1, '/access/record/export', 'business:access:record:export', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('记录统计', 3, @access_record_menu_id, 3, NULL, NULL, 1, '/access/record/statistics', 'business:access:record:statistics', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 2.4 二级菜单：权限授权
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, 
  `perms_type`, `api_perms`, `web_perms`, `icon`, 
  `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, 
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES (
  '权限授权', 2, @access_menu_id, 3, '/business/access/auth', '/business/access/auth/index.vue', 
  1, NULL, 'business:access:auth', 'SafetyCertificateOutlined',
  NULL, 0, NULL, 1,
  1, 0, 0,
  1, NOW(), 1, NOW()
) ON DUPLICATE KEY UPDATE 
  parent_id = VALUES(parent_id);

SET @access_auth_menu_id = LAST_INSERT_ID();

-- 功能点：权限授权
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, 
  `perms_type`, `api_perms`, `web_perms`, `icon`, 
  `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, 
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES 
  ('权限查询', 3, @access_auth_menu_id, 1, NULL, NULL, 1, '/access/auth/query', 'business:access:auth:query', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('权限新增', 3, @access_auth_menu_id, 2, NULL, NULL, 1, '/access/auth/add', 'business:access:auth:add', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('权限编辑', 3, @access_auth_menu_id, 3, NULL, NULL, 1, '/access/auth/update', 'business:access:auth:update', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('权限删除', 3, @access_auth_menu_id, 4, NULL, NULL, 1, '/access/auth/delete', 'business:access:auth:delete', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('权限审批', 3, @access_auth_menu_id, 5, NULL, NULL, 1, '/access/auth/approve', 'business:access:auth:approve', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- ==========================================
-- 三、考勤管理模块（Attendance Module）
-- ==========================================

-- 3.1 一级菜单：考勤管理
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, 
  `perms_type`, `api_perms`, `web_perms`, `icon`, 
  `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, 
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES (
  '考勤管理', 1, @business_root_id, 20, '/business/attendance', NULL, 
  1, NULL, NULL, 'ClockCircleOutlined',
  NULL, 0, NULL, 0,
  1, 0, 0,
  1, NOW(), 1, NOW()
) ON DUPLICATE KEY UPDATE 
  parent_id = VALUES(parent_id),
  sort = VALUES(sort);

SET @attendance_menu_id = LAST_INSERT_ID();

-- 3.2 二级菜单：排班管理
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, 
  `perms_type`, `api_perms`, `web_perms`, `icon`, 
  `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, 
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES (
  '排班管理', 2, @attendance_menu_id, 1, '/business/attendance/schedule', '/business/attendance/schedule/index.vue', 
  1, NULL, 'business:attendance:schedule', 'CalendarOutlined',
  NULL, 0, NULL, 1,
  1, 0, 0,
  1, NOW(), 1, NOW()
) ON DUPLICATE KEY UPDATE 
  parent_id = VALUES(parent_id);

SET @attendance_schedule_menu_id = LAST_INSERT_ID();

-- 功能点：排班管理
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, 
  `perms_type`, `api_perms`, `web_perms`, `icon`, 
  `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, 
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES 
  ('排班查询', 3, @attendance_schedule_menu_id, 1, NULL, NULL, 1, '/attendance/schedule/query', 'business:attendance:schedule:query', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('排班新增', 3, @attendance_schedule_menu_id, 2, NULL, NULL, 1, '/attendance/schedule/add', 'business:attendance:schedule:add', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('排班编辑', 3, @attendance_schedule_menu_id, 3, NULL, NULL, 1, '/attendance/schedule/update', 'business:attendance:schedule:update', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('排班删除', 3, @attendance_schedule_menu_id, 4, NULL, NULL, 1, '/attendance/schedule/delete', 'business:attendance:schedule:delete', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 3.3 二级菜单：打卡记录
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, 
  `perms_type`, `api_perms`, `web_perms`, `icon`, 
  `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, 
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES (
  '打卡记录', 2, @attendance_menu_id, 2, '/business/attendance/record', '/business/attendance/record/index.vue', 
  1, NULL, 'business:attendance:record', 'CheckCircleOutlined',
  NULL, 0, NULL, 1,
  1, 0, 0,
  1, NOW(), 1, NOW()
) ON DUPLICATE KEY UPDATE 
  parent_id = VALUES(parent_id);

SET @attendance_record_menu_id = LAST_INSERT_ID();

-- 功能点：打卡记录
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, 
  `perms_type`, `api_perms`, `web_perms`, `icon`, 
  `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, 
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES 
  ('记录查询', 3, @attendance_record_menu_id, 1, NULL, NULL, 1, '/attendance/record/query', 'business:attendance:record:query', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('记录导出', 3, @attendance_record_menu_id, 2, NULL, NULL, 1, '/attendance/record/export', 'business:attendance:record:export', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('记录统计', 3, @attendance_record_menu_id, 3, NULL, NULL, 1, '/attendance/record/statistics', 'business:attendance:record:statistics', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 3.4 二级菜单：请假管理
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, 
  `perms_type`, `api_perms`, `web_perms`, `icon`, 
  `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, 
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES (
  '请假管理', 2, @attendance_menu_id, 3, '/business/attendance/leave', '/business/attendance/leave/index.vue', 
  1, NULL, 'business:attendance:leave', 'FileProtectOutlined',
  NULL, 0, NULL, 1,
  1, 0, 0,
  1, NOW(), 1, NOW()
) ON DUPLICATE KEY UPDATE 
  parent_id = VALUES(parent_id);

SET @attendance_leave_menu_id = LAST_INSERT_ID();

-- 功能点：请假管理
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, 
  `perms_type`, `api_perms`, `web_perms`, `icon`, 
  `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, 
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES 
  ('请假查询', 3, @attendance_leave_menu_id, 1, NULL, NULL, 1, '/attendance/leave/query', 'business:attendance:leave:query', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('请假申请', 3, @attendance_leave_menu_id, 2, NULL, NULL, 1, '/attendance/leave/add', 'business:attendance:leave:add', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('请假编辑', 3, @attendance_leave_menu_id, 3, NULL, NULL, 1, '/attendance/leave/update', 'business:attendance:leave:update', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('请假审批', 3, @attendance_leave_menu_id, 4, NULL, NULL, 1, '/attendance/leave/approve', 'business:attendance:leave:approve', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('请假取消', 3, @attendance_leave_menu_id, 5, NULL, NULL, 1, '/attendance/leave/cancel', 'business:attendance:leave:cancel', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- ==========================================
-- 四、消费管理模块（Consumption Module）
-- ==========================================

-- 4.1 一级菜单：消费管理
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, 
  `perms_type`, `api_perms`, `web_perms`, `icon`, 
  `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, 
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES (
  '消费管理', 1, @business_root_id, 30, '/business/consumption', NULL, 
  1, NULL, NULL, 'ShoppingOutlined',
  NULL, 0, NULL, 0,
  1, 0, 0,
  1, NOW(), 1, NOW()
) ON DUPLICATE KEY UPDATE 
  parent_id = VALUES(parent_id),
  sort = VALUES(sort);

SET @consumption_menu_id = LAST_INSERT_ID();

-- 4.2 二级菜单：数据总览
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, 
  `perms_type`, `api_perms`, `web_perms`, `icon`, 
  `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, 
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES (
  '数据总览', 2, @consumption_menu_id, 1, '/business/consumption/dashboard', '/business/consumption/dashboard/index.vue', 
  1, NULL, 'business:consumption:dashboard', 'DashboardOutlined',
  NULL, 0, NULL, 1,
  1, 0, 0,
  1, NOW(), 1, NOW()
) ON DUPLICATE KEY UPDATE 
  parent_id = VALUES(parent_id);

SET @consumption_dashboard_menu_id = LAST_INSERT_ID();

-- 功能点：数据总览
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, 
  `perms_type`, `api_perms`, `web_perms`, `icon`, 
  `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, 
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES 
  ('查询统计数据', 3, @consumption_dashboard_menu_id, 1, NULL, NULL, 1, '/consumption/dashboard/stats', 'business:consumption:dashboard:query', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('查询活动列表', 3, @consumption_dashboard_menu_id, 2, NULL, NULL, 1, '/consumption/dashboard/activities', 'business:consumption:dashboard:activity', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 4.3 二级菜单：消费记录
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, 
  `perms_type`, `api_perms`, `web_perms`, `icon`, 
  `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, 
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES (
  '消费记录', 2, @consumption_menu_id, 2, '/business/consumption/record', '/business/consumption/record/index.vue', 
  1, NULL, 'business:consumption:record', 'TransactionOutlined',
  NULL, 0, NULL, 1,
  1, 0, 0,
  1, NOW(), 1, NOW()
) ON DUPLICATE KEY UPDATE 
  parent_id = VALUES(parent_id);

SET @consumption_record_menu_id = LAST_INSERT_ID();

-- 功能点：消费记录
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, 
  `perms_type`, `api_perms`, `web_perms`, `icon`, 
  `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, 
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES 
  ('记录查询', 3, @consumption_record_menu_id, 1, NULL, NULL, 1, '/consumption/record/query', 'business:consumption:record:query', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('记录导出', 3, @consumption_record_menu_id, 2, NULL, NULL, 1, '/consumption/record/export', 'business:consumption:record:export', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('记录统计', 3, @consumption_record_menu_id, 3, NULL, NULL, 1, '/consumption/record/statistics', 'business:consumption:record:statistics', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 4.4 二级菜单：账户管理
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, 
  `perms_type`, `api_perms`, `web_perms`, `icon`, 
  `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, 
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES (
  '账户管理', 2, @consumption_menu_id, 3, '/business/consumption/account', '/business/consumption/account/index.vue', 
  1, NULL, 'business:consumption:account', 'WalletOutlined',
  NULL, 0, NULL, 1,
  1, 0, 0,
  1, NOW(), 1, NOW()
) ON DUPLICATE KEY UPDATE 
  parent_id = VALUES(parent_id);

SET @consumption_account_menu_id = LAST_INSERT_ID();

-- 功能点：账户管理
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, 
  `perms_type`, `api_perms`, `web_perms`, `icon`, 
  `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, 
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES 
  ('账户查询', 3, @consumption_account_menu_id, 1, NULL, NULL, 1, '/consumption/account/query', 'business:consumption:account:query', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('账户充值', 3, @consumption_account_menu_id, 2, NULL, NULL, 1, '/consumption/account/recharge', 'business:consumption:account:recharge', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('账户退款', 3, @consumption_account_menu_id, 3, NULL, NULL, 1, '/consumption/account/refund', 'business:consumption:account:refund', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('账户冻结', 3, @consumption_account_menu_id, 4, NULL, NULL, 1, '/consumption/account/freeze', 'business:consumption:account:freeze', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- ==========================================
-- 五、视频监控模块（Video Module）
-- ==========================================

-- 5.1 一级菜单：视频监控
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, 
  `perms_type`, `api_perms`, `web_perms`, `icon`, 
  `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, 
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES (
  '视频监控', 1, @business_root_id, 40, '/business/video', NULL, 
  1, NULL, NULL, 'CameraOutlined',
  NULL, 0, NULL, 0,
  1, 0, 0,
  1, NOW(), 1, NOW()
) ON DUPLICATE KEY UPDATE 
  parent_id = VALUES(parent_id),
  sort = VALUES(sort);

SET @video_menu_id = LAST_INSERT_ID();

-- 5.2 二级菜单：实时监控
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, 
  `perms_type`, `api_perms`, `web_perms`, `icon`, 
  `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, 
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES (
  '实时监控', 2, @video_menu_id, 1, '/business/video/live', '/business/video/live/index.vue', 
  1, NULL, 'business:video:live', 'PlayCircleOutlined',
  NULL, 0, NULL, 1,
  1, 0, 0,
  1, NOW(), 1, NOW()
) ON DUPLICATE KEY UPDATE 
  parent_id = VALUES(parent_id);

SET @video_live_menu_id = LAST_INSERT_ID();

-- 功能点：实时监控
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, 
  `perms_type`, `api_perms`, `web_perms`, `icon`, 
  `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, 
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES 
  ('监控查看', 3, @video_live_menu_id, 1, NULL, NULL, 1, '/video/live/view', 'business:video:live:view', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('监控控制', 3, @video_live_menu_id, 2, NULL, NULL, 1, '/video/live/control', 'business:video:live:control', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('监控截图', 3, @video_live_menu_id, 3, NULL, NULL, 1, '/video/live/snapshot', 'business:video:live:snapshot', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 5.3 二级菜单：录像回放
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, 
  `perms_type`, `api_perms`, `web_perms`, `icon`, 
  `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, 
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES (
  '录像回放', 2, @video_menu_id, 2, '/business/video/playback', '/business/video/playback/index.vue', 
  1, NULL, 'business:video:playback', 'VideoCameraAddOutlined',
  NULL, 0, NULL, 1,
  1, 0, 0,
  1, NOW(), 1, NOW()
) ON DUPLICATE KEY UPDATE 
  parent_id = VALUES(parent_id);

SET @video_playback_menu_id = LAST_INSERT_ID();

-- 功能点：录像回放
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, 
  `perms_type`, `api_perms`, `web_perms`, `icon`, 
  `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, 
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES 
  ('录像查询', 3, @video_playback_menu_id, 1, NULL, NULL, 1, '/video/playback/query', 'business:video:playback:query', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('录像播放', 3, @video_playback_menu_id, 2, NULL, NULL, 1, '/video/playback/play', 'business:video:playback:play', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('录像下载', 3, @video_playback_menu_id, 3, NULL, NULL, 1, '/video/playback/download', 'business:video:playback:download', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 5.4 二级菜单：设备管理
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, 
  `perms_type`, `api_perms`, `web_perms`, `icon`, 
  `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, 
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES (
  '设备管理', 2, @video_menu_id, 3, '/business/video/device', '/business/video/device/index.vue', 
  1, NULL, 'business:video:device', 'VideoCameraOutlined',
  NULL, 0, NULL, 1,
  1, 0, 0,
  1, NOW(), 1, NOW()
) ON DUPLICATE KEY UPDATE 
  parent_id = VALUES(parent_id);

SET @video_device_menu_id = LAST_INSERT_ID();

-- 功能点：设备管理
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, 
  `perms_type`, `api_perms`, `web_perms`, `icon`, 
  `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, 
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES 
  ('设备查询', 3, @video_device_menu_id, 1, NULL, NULL, 1, '/video/device/query', 'business:video:device:query', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('设备新增', 3, @video_device_menu_id, 2, NULL, NULL, 1, '/video/device/add', 'business:video:device:add', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('设备编辑', 3, @video_device_menu_id, 3, NULL, NULL, 1, '/video/device/update', 'business:video:device:update', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('设备删除', 3, @video_device_menu_id, 4, NULL, NULL, 1, '/video/device/delete', 'business:video:device:delete', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- ==========================================
-- 六、访客管理模块（Visitor Module）
-- ==========================================

-- 6.1 一级菜单：访客管理
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, 
  `perms_type`, `api_perms`, `web_perms`, `icon`, 
  `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, 
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES (
  '访客管理', 1, @business_root_id, 50, '/business/visitor', NULL, 
  1, NULL, NULL, 'UserAddOutlined',
  NULL, 0, NULL, 0,
  1, 0, 0,
  1, NOW(), 1, NOW()
) ON DUPLICATE KEY UPDATE 
  parent_id = VALUES(parent_id),
  sort = VALUES(sort);

SET @visitor_menu_id = LAST_INSERT_ID();

-- 6.2 二级菜单：访客预约
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, 
  `perms_type`, `api_perms`, `web_perms`, `icon`, 
  `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, 
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES (
  '访客预约', 2, @visitor_menu_id, 1, '/business/visitor/appointment', '/business/visitor/appointment/index.vue', 
  1, NULL, 'business:visitor:appointment', 'ScheduleOutlined',
  NULL, 0, NULL, 1,
  1, 0, 0,
  1, NOW(), 1, NOW()
) ON DUPLICATE KEY UPDATE 
  parent_id = VALUES(parent_id);

SET @visitor_appointment_menu_id = LAST_INSERT_ID();

-- 功能点：访客预约
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, 
  `perms_type`, `api_perms`, `web_perms`, `icon`, 
  `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, 
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES 
  ('预约查询', 3, @visitor_appointment_menu_id, 1, NULL, NULL, 1, '/visitor/appointment/query', 'business:visitor:appointment:query', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('预约新增', 3, @visitor_appointment_menu_id, 2, NULL, NULL, 1, '/visitor/appointment/add', 'business:visitor:appointment:add', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('预约编辑', 3, @visitor_appointment_menu_id, 3, NULL, NULL, 1, '/visitor/appointment/update', 'business:visitor:appointment:update', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('预约审批', 3, @visitor_appointment_menu_id, 4, NULL, NULL, 1, '/visitor/appointment/approve', 'business:visitor:appointment:approve', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('预约取消', 3, @visitor_appointment_menu_id, 5, NULL, NULL, 1, '/visitor/appointment/cancel', 'business:visitor:appointment:cancel', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 6.3 二级菜单：访客登记
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, 
  `perms_type`, `api_perms`, `web_perms`, `icon`, 
  `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, 
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES (
  '访客登记', 2, @visitor_menu_id, 2, '/business/visitor/register', '/business/visitor/register/index.vue', 
  1, NULL, 'business:visitor:register', 'FormOutlined',
  NULL, 0, NULL, 1,
  1, 0, 0,
  1, NOW(), 1, NOW()
) ON DUPLICATE KEY UPDATE 
  parent_id = VALUES(parent_id);

SET @visitor_register_menu_id = LAST_INSERT_ID();

-- 功能点：访客登记
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, 
  `perms_type`, `api_perms`, `web_perms`, `icon`, 
  `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, 
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES 
  ('登记查询', 3, @visitor_register_menu_id, 1, NULL, NULL, 1, '/visitor/register/query', 'business:visitor:register:query', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('登记录入', 3, @visitor_register_menu_id, 2, NULL, NULL, 1, '/visitor/register/add', 'business:visitor:register:add', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('登记编辑', 3, @visitor_register_menu_id, 3, NULL, NULL, 1, '/visitor/register/update', 'business:visitor:register:update', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('访客离场', 3, @visitor_register_menu_id, 4, NULL, NULL, 1, '/visitor/register/checkout', 'business:visitor:register:checkout', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 6.4 二级菜单：访问记录
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, 
  `perms_type`, `api_perms`, `web_perms`, `icon`, 
  `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, 
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES (
  '访问记录', 2, @visitor_menu_id, 3, '/business/visitor/record', '/business/visitor/record/index.vue', 
  1, NULL, 'business:visitor:record', 'HistoryOutlined',
  NULL, 0, NULL, 1,
  1, 0, 0,
  1, NOW(), 1, NOW()
) ON DUPLICATE KEY UPDATE 
  parent_id = VALUES(parent_id);

SET @visitor_record_menu_id = LAST_INSERT_ID();

-- 功能点：访问记录
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, 
  `perms_type`, `api_perms`, `web_perms`, `icon`, 
  `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, 
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES 
  ('记录查询', 3, @visitor_record_menu_id, 1, NULL, NULL, 1, '/visitor/record/query', 'business:visitor:record:query', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('记录导出', 3, @visitor_record_menu_id, 2, NULL, NULL, 1, '/visitor/record/export', 'business:visitor:record:export', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('记录统计', 3, @visitor_record_menu_id, 3, NULL, NULL, 1, '/visitor/record/statistics', 'business:visitor:record:statistics', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- ==========================================
-- 七、系统设置模块（System Module）
-- ==========================================

-- 7.1 二级菜单：用户管理
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, 
  `perms_type`, `api_perms`, `web_perms`, `icon`, 
  `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, 
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES (
  '用户管理', 2, @system_root_id, 1, '/system/user', '/system/user/index.vue', 
  1, NULL, 'system:user', 'UserOutlined',
  NULL, 0, NULL, 1,
  1, 0, 0,
  1, NOW(), 1, NOW()
) ON DUPLICATE KEY UPDATE 
  parent_id = VALUES(parent_id);

SET @system_user_menu_id = LAST_INSERT_ID();

-- 功能点：用户管理
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, 
  `perms_type`, `api_perms`, `web_perms`, `icon`, 
  `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, 
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES 
  ('用户查询', 3, @system_user_menu_id, 1, NULL, NULL, 1, '/user/query', 'system:user:query', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('用户新增', 3, @system_user_menu_id, 2, NULL, NULL, 1, '/user/add', 'system:user:add', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('用户编辑', 3, @system_user_menu_id, 3, NULL, NULL, 1, '/user/update', 'system:user:update', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('用户删除', 3, @system_user_menu_id, 4, NULL, NULL, 1, '/user/delete', 'system:user:delete', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('重置密码', 3, @system_user_menu_id, 5, NULL, NULL, 1, '/user/reset-password', 'system:user:resetPassword', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 7.2 二级菜单：角色管理
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, 
  `perms_type`, `api_perms`, `web_perms`, `icon`, 
  `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, 
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES (
  '角色管理', 2, @system_root_id, 2, '/system/role', '/system/role/index.vue', 
  1, NULL, 'system:role', 'SafetyCertificateOutlined',
  NULL, 0, NULL, 1,
  1, 0, 0,
  1, NOW(), 1, NOW()
) ON DUPLICATE KEY UPDATE 
  parent_id = VALUES(parent_id);

SET @system_role_menu_id = LAST_INSERT_ID();

-- 功能点：角色管理
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, 
  `perms_type`, `api_perms`, `web_perms`, `icon`, 
  `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, 
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES 
  ('角色查询', 3, @system_role_menu_id, 1, NULL, NULL, 1, '/role/query', 'system:role:query', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('角色新增', 3, @system_role_menu_id, 2, NULL, NULL, 1, '/role/add', 'system:role:add', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('角色编辑', 3, @system_role_menu_id, 3, NULL, NULL, 1, '/role/update', 'system:role:update', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('角色删除', 3, @system_role_menu_id, 4, NULL, NULL, 1, '/role/delete', 'system:role:delete', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('权限配置', 3, @system_role_menu_id, 5, NULL, NULL, 1, '/role/permission', 'system:role:permission', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 7.3 二级菜单：菜单管理
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, 
  `perms_type`, `api_perms`, `web_perms`, `icon`, 
  `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, 
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES (
  '菜单管理', 2, @system_root_id, 3, '/system/menu', '/system/menu/menu-list.vue', 
  1, NULL, 'system:menu', 'MenuOutlined',
  NULL, 0, NULL, 1,
  1, 0, 0,
  1, NOW(), 1, NOW()
) ON DUPLICATE KEY UPDATE 
  parent_id = VALUES(parent_id);

SET @system_menu_menu_id = LAST_INSERT_ID();

-- 功能点：菜单管理
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, 
  `perms_type`, `api_perms`, `web_perms`, `icon`, 
  `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, 
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES 
  ('菜单查询', 3, @system_menu_menu_id, 1, NULL, NULL, 1, '/menu/query', 'system:menu:query', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('菜单新增', 3, @system_menu_menu_id, 2, NULL, NULL, 1, '/menu/add', 'system:menu:add', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('菜单编辑', 3, @system_menu_menu_id, 3, NULL, NULL, 1, '/menu/update', 'system:menu:update', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('菜单删除', 3, @system_menu_menu_id, 4, NULL, NULL, 1, '/menu/batchDelete', 'system:menu:delete', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 7.4 二级菜单：部门管理
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, 
  `perms_type`, `api_perms`, `web_perms`, `icon`, 
  `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, 
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES (
  '部门管理', 2, @system_root_id, 4, '/system/department', '/system/department/index.vue', 
  1, NULL, 'system:department', 'ApartmentOutlined',
  NULL, 0, NULL, 1,
  1, 0, 0,
  1, NOW(), 1, NOW()
) ON DUPLICATE KEY UPDATE 
  parent_id = VALUES(parent_id);

SET @system_dept_menu_id = LAST_INSERT_ID();

-- 功能点：部门管理
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, 
  `perms_type`, `api_perms`, `web_perms`, `icon`, 
  `context