-- ==================================================================
-- IOE-DREAM 全模块菜单目录完整初始化SQL脚本 v2.0
-- ==================================================================
-- 版本: v2.0.0 (企业级完整版)
-- 创建日期: 2025-12-08
-- 创建人: 架构团队
-- 说明: 完整初始化IOE-DREAM所有业务模块和系统模块的菜单树结构
-- 执行方式: type "完整菜单初始化_v2.0.sql" | docker exec -i ioedream-mysql mysql -uroot -proot1234 ioedream
-- ==================================================================

USE ioedream;

-- 开启事务
START TRANSACTION;

-- ==================================================================
-- 第一部分: 顶级目录初始化
-- ==================================================================

-- 1. 业务管理（顶级目录）- 用户主要工作区
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
  menu_name = VALUES(menu_name), sort = VALUES(sort);

SET @business_root_id = LAST_INSERT_ID();
IF @business_root_id = 0 THEN
  SELECT menu_id INTO @business_root_id FROM t_menu WHERE menu_name = '业务管理' AND parent_id = 0 AND deleted_flag = 0 LIMIT 1;
END IF;

-- 2. 系统设置（顶级目录）- 系统管理功能
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
  menu_name = VALUES(menu_name), sort = VALUES(sort);

SET @system_root_id = LAST_INSERT_ID();
IF @system_root_id = 0 THEN
  SELECT menu_id INTO @system_root_id FROM t_menu WHERE menu_name = '系统设置' AND parent_id = 0 AND deleted_flag = 0 LIMIT 1;
END IF;

-- 3. 支撑功能（顶级目录）- 支持功能
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
  menu_name = VALUES(menu_name), sort = VALUES(sort);

SET @support_root_id = LAST_INSERT_ID();
IF @support_root_id = 0 THEN
  SELECT menu_id INTO @support_root_id FROM t_menu WHERE menu_name = '支撑功能' AND parent_id = 0 AND deleted_flag = 0 LIMIT 1;
END IF;

-- ==================================================================
-- 第二部分: 业务管理模块
-- ==================================================================

-- ==================== 2.1 门禁管理模块 ====================
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
) ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id), sort = VALUES(sort);

SET @access_menu_id = LAST_INSERT_ID();
IF @access_menu_id = 0 THEN
  SELECT menu_id INTO @access_menu_id FROM t_menu WHERE menu_name = '门禁管理' AND parent_id = @business_root_id AND deleted_flag = 0 LIMIT 1;
END IF;

-- 2.1.1 门禁设备管理
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('设备管理', 2, @access_menu_id, 1, '/business/access/device', '/business/access/device/index.vue', 1, NULL, 'business:access:device', 'VideoCameraOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id);
SET @access_device_menu_id = LAST_INSERT_ID();
IF @access_device_menu_id = 0 THEN SELECT menu_id INTO @access_device_menu_id FROM t_menu WHERE menu_name = '设备管理' AND parent_id = @access_menu_id AND deleted_flag = 0 LIMIT 1; END IF;

-- 设备管理功能点
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) VALUES 
  ('设备查询', 3, @access_device_menu_id, 1, NULL, NULL, 1, '/access/device/query', 'business:access:device:query', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('设备新增', 3, @access_device_menu_id, 2, NULL, NULL, 1, '/access/device/add', 'business:access:device:add', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('设备编辑', 3, @access_device_menu_id, 3, NULL, NULL, 1, '/access/device/update', 'business:access:device:update', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('设备删除', 3, @access_device_menu_id, 4, NULL, NULL, 1, '/access/device/delete', 'business:access:device:delete', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('设备控制', 3, @access_device_menu_id, 5, NULL, NULL, 1, '/access/device/control', 'business:access:device:control', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 2.1.2 通行记录
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('通行记录', 2, @access_menu_id, 2, '/business/access/record', '/business/access/record/index.vue', 1, NULL, 'business:access:record', 'HistoryOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id);
SET @access_record_menu_id = LAST_INSERT_ID();
IF @access_record_menu_id = 0 THEN SELECT menu_id INTO @access_record_menu_id FROM t_menu WHERE menu_name = '通行记录' AND parent_id = @access_menu_id AND deleted_flag = 0 LIMIT 1; END IF;

INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) VALUES 
  ('记录查询', 3, @access_record_menu_id, 1, NULL, NULL, 1, '/access/record/query', 'business:access:record:query', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('记录导出', 3, @access_record_menu_id, 2, NULL, NULL, 1, '/access/record/export', 'business:access:record:export', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('记录统计', 3, @access_record_menu_id, 3, NULL, NULL, 1, '/access/record/statistics', 'business:access:record:statistics', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 2.1.3 全局联动管理
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('全局联动', 2, @access_menu_id, 3, '/business/access/advanced', '/business/access/advanced/GlobalLinkageManagement.vue', 1, NULL, 'business:access:linkage', 'ApiOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id);

-- ==================== 2.2 考勤管理模块 ====================
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('考勤管理', 1, @business_root_id, 20, '/business/attendance', NULL, 1, NULL, NULL, 'ClockCircleOutlined', NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id), sort = VALUES(sort);
SET @attendance_menu_id = LAST_INSERT_ID();
IF @attendance_menu_id = 0 THEN SELECT menu_id INTO @attendance_menu_id FROM t_menu WHERE menu_name = '考勤管理' AND parent_id = @business_root_id AND deleted_flag = 0 LIMIT 1; END IF;

-- 2.2.1 考勤记录
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('考勤记录', 2, @attendance_menu_id, 1, '/business/attendance/record', '/business/attendance/record/index.vue', 1, NULL, 'business:attendance:record', 'ReconciliationOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id);
SET @attendance_record_menu_id = LAST_INSERT_ID();
IF @attendance_record_menu_id = 0 THEN SELECT menu_id INTO @attendance_record_menu_id FROM t_menu WHERE menu_name = '考勤记录' AND parent_id = @attendance_menu_id AND deleted_flag = 0 LIMIT 1; END IF;

INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) VALUES 
  ('考勤查询', 3, @attendance_record_menu_id, 1, NULL, NULL, 1, '/attendance/record/query', 'business:attendance:record:query', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('考勤导出', 3, @attendance_record_menu_id, 2, NULL, NULL, 1, '/attendance/record/export', 'business:attendance:record:export', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('补卡管理', 3, @attendance_record_menu_id, 3, NULL, NULL, 1, '/attendance/record/makeup', 'business:attendance:record:makeup', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- ==================== 2.3 消费管理模块 ====================
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('消费管理', 1, @business_root_id, 30, '/business/consume', NULL, 1, NULL, NULL, 'ShoppingOutlined', NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id), sort = VALUES(sort);
SET @consume_menu_id = LAST_INSERT_ID();
IF @consume_menu_id = 0 THEN SELECT menu_id INTO @consume_menu_id FROM t_menu WHERE menu_name = '消费管理' AND parent_id = @business_root_id AND deleted_flag = 0 LIMIT 1; END IF;

-- 2.3.1 账户管理
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('账户管理', 2, @consume_menu_id, 1, '/business/consume/account', '/business/consume/account/index.vue', 1, NULL, 'business:consume:account', 'WalletOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id);
SET @consume_account_menu_id = LAST_INSERT_ID();
IF @consume_account_menu_id = 0 THEN SELECT menu_id INTO @consume_account_menu_id FROM t_menu WHERE menu_name = '账户管理' AND parent_id = @consume_menu_id AND deleted_flag = 0 LIMIT 1; END IF;

INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) VALUES 
  ('账户查询', 3, @consume_account_menu_id, 1, NULL, NULL, 1, '/consume/account/query', 'business:consume:account:query', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('账户充值', 3, @consume_account_menu_id, 2, NULL, NULL, 1, '/consume/account/recharge', 'business:consume:account:recharge', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('账户退款', 3, @consume_account_menu_id, 3, NULL, NULL, 1, '/consume/account/refund', 'business:consume:account:refund', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 2.3.2 交易记录
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('交易记录', 2, @consume_menu_id, 2, '/business/consume/transaction', '/business/consume/transaction/index.vue', 1, NULL, 'business:consume:transaction', 'TransactionOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id);
SET @consume_transaction_menu_id = LAST_INSERT_ID();
IF @consume_transaction_menu_id = 0 THEN SELECT menu_id INTO @consume_transaction_menu_id FROM t_menu WHERE menu_name = '交易记录' AND parent_id = @consume_menu_id AND deleted_flag = 0 LIMIT 1; END IF;

INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) VALUES 
  ('交易查询', 3, @consume_transaction_menu_id, 1, NULL, NULL, 1, '/consume/transaction/query', 'business:consume:transaction:query', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('交易导出', 3, @consume_transaction_menu_id, 2, NULL, NULL, 1, '/consume/transaction/export', 'business:consume:transaction:export', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 2.3.3 消费报表
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('消费报表', 2, @consume_menu_id, 3, '/business/consume/report', '/business/consume/report/index.vue', 1, NULL, 'business:consume:report', 'BarChartOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id);

-- ==================== 2.4 访客管理模块 ====================
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('访客管理', 1, @business_root_id, 40, '/business/visitor', NULL, 1, NULL, NULL, 'TeamOutlined', NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id), sort = VALUES(sort);
SET @visitor_menu_id = LAST_INSERT_ID();
IF @visitor_menu_id = 0 THEN SELECT menu_id INTO @visitor_menu_id FROM t_menu WHERE menu_name = '访客管理' AND parent_id = @business_root_id AND deleted_flag = 0 LIMIT 1; END IF;

-- 2.4.1 访客预约
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('访客预约', 2, @visitor_menu_id, 1, '/business/visitor/appointment', '/business/visitor/appointment.vue', 1, NULL, 'business:visitor:appointment', 'CalendarOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id);
SET @visitor_appointment_menu_id = LAST_INSERT_ID();
IF @visitor_appointment_menu_id = 0 THEN SELECT menu_id INTO @visitor_appointment_menu_id FROM t_menu WHERE menu_name = '访客预约' AND parent_id = @visitor_menu_id AND deleted_flag = 0 LIMIT 1; END IF;

INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) VALUES 
  ('预约查询', 3, @visitor_appointment_menu_id, 1, NULL, NULL, 1, '/visitor/appointment/query', 'business:visitor:appointment:query', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('预约新增', 3, @visitor_appointment_menu_id, 2, NULL, NULL, 1, '/visitor/appointment/add', 'business:visitor:appointment:add', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('预约审批', 3, @visitor_appointment_menu_id, 3, NULL, NULL, 1, '/visitor/appointment/approve', 'business:visitor:appointment:approve', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 2.4.2 访客登记
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('访客登记', 2, @visitor_menu_id, 2, '/business/visitor/registration', '/business/visitor/registration.vue', 1, NULL, 'business:visitor:registration', 'FormOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id);

-- 2.4.3 访客记录
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('访客记录', 2, @visitor_menu_id, 3, '/business/visitor/record', '/business/visitor/record.vue', 1, NULL, 'business:visitor:record', 'HistoryOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id);

-- 2.4.4 黑名单管理
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('黑名单', 2, @visitor_menu_id, 4, '/business/visitor/blacklist', '/business/visitor/blacklist.vue', 1, NULL, 'business:visitor:blacklist', 'StopOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id);

-- 2.4.5 物流访客
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('物流访客', 2, @visitor_menu_id, 5, '/business/visitor/logistics', '/business/visitor/logistics.vue', 1, NULL, 'business:visitor:logistics', 'CarOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id);

-- 2.4.6 访客统计
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('访客统计', 2, @visitor_menu_id, 6, '/business/visitor/statistics', '/business/visitor/statistics.vue', 1, NULL, 'business:visitor:statistics', 'FundOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id);

-- ==================== 2.5 智能视频模块 ====================
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('智能视频', 1, @business_root_id, 50, '/business/smart-video', NULL, 1, NULL, NULL, 'VideoCameraOutlined', NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id), sort = VALUES(sort);
SET @video_menu_id = LAST_INSERT_ID();
IF @video_menu_id = 0 THEN SELECT menu_id INTO @video_menu_id FROM t_menu WHERE menu_name = '智能视频' AND parent_id = @business_root_id AND deleted_flag = 0 LIMIT 1; END IF;

-- 2.5.1 系统总览
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('系统总览', 2, @video_menu_id, 1, '/business/smart-video/system-overview', '/business/smart-video/system-overview.vue', 1, NULL, 'business:video:overview', 'DashboardOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id);

-- 2.5.2 设备管理
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) VALUES
  ('设备列表', 2, @video_menu_id, 2, '/business/smart-video/device-list', '/business/smart-video/device-list.vue', 1, NULL, 'business:video:device', 'UnorderedListOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('设备分组', 2, @video_menu_id, 3, '/business/smart-video/device-group', '/business/smart-video/device-group.vue', 1, NULL, 'business:video:group', 'ApartmentOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id);

-- 2.5.3 实时监控
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) VALUES
  ('监控预览', 2, @video_menu_id, 4, '/business/smart-video/monitor-preview', '/business/smart-video/monitor-preview.vue', 1, NULL, 'business:video:preview', 'EyeOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('电视墙', 2, @video_menu_id, 5, '/business/smart-video/tv-wall', '/business/smart-video/tv-wall.vue', 1, NULL, 'business:video:tvwall', 'DesktopOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('大屏控制', 2, @video_menu_id, 6, '/business/smart-video/screen-control', '/business/smart-video/screen-control.vue', 1, NULL, 'business:video:screen', 'BorderOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id);

-- 2.5.4 录像回放
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('录像回放', 2, @video_menu_id, 7, '/business/smart-video/video-playback', '/business/smart-video/video-playback.vue', 1, NULL, 'business:video:playback', 'PlaySquareOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id);

-- 2.5.5 智能分析
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) VALUES
  ('行为分析', 2, @video_menu_id, 8, '/business/smart-video/behavior-analysis', '/business/smart-video/behavior-analysis.vue', 1, NULL, 'business:video:behavior', 'BulbOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('人群态势', 2, @video_menu_id, 9, '/business/smart-video/crowd-situation', '/business/smart-video/crowd-situation.vue', 1, NULL, 'business:video:crowd', 'UsergroupAddOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('热力统计', 2, @video_menu_id, 10, '/business/smart-video/heat-statistics', '/business/smart-video/heat-statistics.vue', 1, NULL, 'business:video:heat', 'HeatMapOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('目标检索', 2, @video_menu_id, 11, '/business/smart-video/target-search', '/business/smart-video/target-search.vue', 1, NULL, 'business:video:search', 'SearchOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id);

-- 2.5.6 告警管理
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) VALUES
  ('历史告警', 2, @video_menu_id, 12, '/business/smart-video/history-alarm', '/business/smart-video/history-alarm.vue', 1, NULL, 'business:video:alarm', 'AlertOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('联动设置', 2, @video_menu_id, 13, '/business/smart-video/linkage-settings', '/business/smart-video/linkage-settings.vue', 1, NULL, 'business:video:linkage:settings', 'LinkOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('联动记录', 2, @video_menu_id, 14, '/business/smart-video/linkage-records', '/business/smart-video/linkage-records.vue', 1, NULL, 'business:video:linkage:records', 'FieldTimeOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id);

-- ==================== 2.6 OA办公模块 ====================
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('OA办公', 1, @business_root_id, 60, '/business/oa', NULL, 1, NULL, NULL, 'FileTextOutlined', NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id), sort = VALUES(sort);
SET @oa_menu_id = LAST_INSERT_ID();
IF @oa_menu_id = 0 THEN SELECT menu_id INTO @oa_menu_id FROM t_menu WHERE menu_name = 'OA办公' AND parent_id = @business_root_id AND deleted_flag = 0 LIMIT 1; END IF;

-- 2.6.1 通知公告
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('通知公告', 2, @oa_menu_id, 1, '/business/oa/notice', '/business/oa/notice/notice-list.vue', 1, NULL, 'business:oa:notice', 'NotificationOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id);
SET @oa_notice_menu_id = LAST_INSERT_ID();
IF @oa_notice_menu_id = 0 THEN SELECT menu_id INTO @oa_notice_menu_id FROM t_menu WHERE menu_name = '通知公告' AND parent_id = @oa_menu_id AND deleted_flag = 0 LIMIT 1; END IF;

INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) VALUES 
  ('公告查询', 3, @oa_notice_menu_id, 1, NULL, NULL, 1, '/oa/notice/query', 'business:oa:notice:query', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('公告新增', 3, @oa_notice_menu_id, 2, NULL, NULL, 1, '/oa/notice/add', 'business:oa:notice:add', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('公告编辑', 3, @oa_notice_menu_id, 3, NULL, NULL, 1, '/oa/notice/update', 'business:oa:notice:update', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('公告删除', 3, @oa_notice_menu_id, 4, NULL, NULL, 1, '/oa/notice/delete', 'business:oa:notice:delete', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 2.6.2 企业管理
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('企业管理', 2, @oa_menu_id, 2, '/business/oa/enterprise', '/business/oa/enterprise/enterprise-list.vue', 1, NULL, 'business:oa:enterprise', 'BankOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id);

-- 2.6.3 工作流管理
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('工作流', 1, @oa_menu_id, 3, '/business/oa/workflow', NULL, 1, NULL, NULL, 'PartitionOutlined', NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id);
SET @oa_workflow_menu_id = LAST_INSERT_ID();
IF @oa_workflow_menu_id = 0 THEN SELECT menu_id INTO @oa_workflow_menu_id FROM t_menu WHERE menu_name = '工作流' AND parent_id = @oa_menu_id AND deleted_flag = 0 LIMIT 1; END IF;

INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) VALUES
  ('流程定义', 2, @oa_workflow_menu_id, 1, '/business/oa/workflow/definition', '/business/oa/workflow/definition/definition-list.vue', 1, NULL, 'business:oa:workflow:definition', 'AccountBookOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('流程实例', 2, @oa_workflow_menu_id, 2, '/business/oa/workflow/instance', '/business/oa/workflow/instance/instance-list.vue', 1, NULL, 'business:oa:workflow:instance', 'ProfileOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('待办任务', 2, @oa_workflow_menu_id, 3, '/business/oa/workflow/task', '/business/oa/workflow/task/pending-task-list.vue', 1, NULL, 'business:oa:workflow:task', 'AuditOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('流程监控', 2, @oa_workflow_menu_id, 4, '/business/oa/workflow/monitor', '/business/oa/workflow/monitor/process-monitor.vue', 1, NULL, 'business:oa:workflow:monitor', 'FundViewOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id);

-- ==================== 2.7 ERP管理模块 ====================
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('ERP管理', 1, @business_root_id, 70, '/business/erp', NULL, 1, NULL, NULL, 'ShopOutlined', NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id), sort = VALUES(sort);
SET @erp_menu_id = LAST_INSERT_ID();
IF @erp_menu_id = 0 THEN SELECT menu_id INTO @erp_menu_id FROM t_menu WHERE menu_name = 'ERP管理' AND parent_id = @business_root_id AND deleted_flag = 0 LIMIT 1; END IF;

-- 2.7.1 商品目录
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('商品目录', 2, @erp_menu_id, 1, '/business/erp/catalog', '/business/erp/catalog/goods-catalog.vue', 1, NULL, 'business:erp:catalog', 'AppstoreAddOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id);

-- 2.7.2 商品列表
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('商品列表', 2, @erp_menu_id, 2, '/business/erp/goods', '/business/erp/goods/goods-list.vue', 1, NULL, 'business:erp:goods', 'ShoppingCartOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id);

-- ==================================================================
-- 第三部分: 系统设置模块
-- ==================================================================

-- 3.1 首页
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('首页', 2, @system_root_id, 1, '/system/home', '/system/home/index.vue', 1, NULL, 'system:home', 'HomeOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id);

-- 3.2 员工管理
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('员工管理', 2, @system_root_id, 2, '/system/employee', '/system/employee/index.vue', 1, NULL, 'system:employee', 'UserOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id);
SET @system_employee_menu_id = LAST_INSERT_ID();
IF @system_employee_menu_id = 0 THEN SELECT menu_id INTO @system_employee_menu_id FROM t_menu WHERE menu_name = '员工管理' AND parent_id = @system_root_id AND deleted_flag = 0 LIMIT 1; END IF;

INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) VALUES 
  ('员工查询', 3, @system_employee_menu_id, 1, NULL, NULL, 1, '/system/employee/query', 'system:employee:query', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('员工新增', 3, @system_employee_menu_id, 2, NULL, NULL, 1, '/system/employee/add', 'system:employee:add', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('员工编辑', 3, @system_employee_menu_id, 3, NULL, NULL, 1, '/system/employee/update', 'system:employee:update', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('员工删除', 3, @system_employee_menu_id, 4, NULL, NULL, 1, '/system/employee/delete', 'system:employee:delete', NULL, NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 3.3 部门管理
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('部门管理', 2, @system_root_id, 3, '/system/department', '/system/department/department-list.vue', 1, NULL, 'system:department', 'ApartmentOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id);

-- 3.4 角色管理
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('角色管理', 2, @system_root_id, 4, '/system/role', '/system/role/index.vue', 1, NULL, 'system:role', 'SafetyCertificateOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id);

-- 3.5 菜单管理
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('菜单管理', 2, @system_root_id, 5, '/system/menu', '/system/menu/menu-list.vue', 1, NULL, 'system:menu', 'MenuOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id);

-- 3.6 区域管理
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('区域管理', 2, @system_root_id, 6, '/system/area', '/system/area/index.vue', 1, NULL, 'system:area', 'EnvironmentOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id);

-- 3.7 岗位管理
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('岗位管理', 2, @system_root_id, 7, '/system/position', '/system/position/position-list.vue', 1, NULL, 'system:position', 'IdcardOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id);

-- ==================================================================
-- 第四部分: 支撑功能模块
-- ==================================================================

-- 4.1 字典管理
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('字典管理', 2, @support_root_id, 1, '/support/dict', '/support/dict/index.vue', 1, NULL, 'support:dict', 'BookOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id);

-- 4.2 配置管理
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('配置管理', 2, @support_root_id, 2, '/support/config', '/support/config/config-list.vue', 1, NULL, 'support:config', 'ControlOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id);

-- 4.3 文件管理
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('文件管理', 2, @support_root_id, 3, '/support/file', '/support/file/file-list.vue', 1, NULL, 'support:file', 'FileOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id);

-- 4.4 操作日志
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('操作日志', 2, @support_root_id, 4, '/support/operate-log', '/support/operate-log/operate-log-list.vue', 1, NULL, 'support:operate-log', 'SolutionOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id);

-- 4.5 登录日志
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('登录日志', 2, @support_root_id, 5, '/support/login-log', '/support/login-log/login-log-list.vue', 1, NULL, 'support:login-log', 'LoginOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id);

-- 4.6 变更记录
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('变更记录', 2, @support_root_id, 6, '/support/change-log', '/support/change-log/change-log-list.vue', 1, NULL, 'support:change-log', 'HistoryOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id);

-- 4.7 帮助文档
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) VALUES
  ('帮助文档', 2, @support_root_id, 7, '/support/help-doc', '/support/help-doc/management/help-doc-manage-list.vue', 1, NULL, 'support:help-doc', 'QuestionCircleOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('文档中心', 2, @support_root_id, 8, '/support/help-doc/view', '/support/help-doc/user-view/help-doc-user-view.vue', 1, NULL, 'support:help-doc:view', 'ReadOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id);

-- 4.8 消息管理
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('消息管理', 2, @support_root_id, 9, '/support/message', '/support/message/message-list.vue', 1, NULL, 'support:message', 'MessageOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id);

-- 4.9 定时任务
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('定时任务', 2, @support_root_id, 10, '/support/job', '/support/job/job-list.vue', 1, NULL, 'support:job', 'ClockCircleOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id);

-- 4.10 序列号生成
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('序列号', 2, @support_root_id, 11, '/support/serial-number', '/support/serial-number/serial-number-list.vue', 1, NULL, 'support:serial-number', 'BarcodeOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id);

-- 4.11 心跳监控
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('心跳监控', 2, @support_root_id, 12, '/support/heart-beat', '/support/heart-beat/heart-beat-list.vue', 1, NULL, 'support:heart-beat', 'HeartOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id);

-- 4.12 缓存管理
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('缓存管理', 2, @support_root_id, 13, '/support/cache', '/support/cache/cache-list.vue', 1, NULL, 'support:cache', 'DatabaseOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id);

-- 4.13 代码生成器
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('代码生成', 2, @support_root_id, 14, '/support/code-generator', '/support/code-generator/code-generator-list.vue', 1, NULL, 'support:code-generator', 'CodeOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id);

-- 4.14 重载配置
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('重载配置', 2, @support_root_id, 15, '/support/reload', '/support/reload/reload-list.vue', 1, NULL, 'support:reload', 'ReloadOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id);

-- 4.15 等保配置
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) VALUES
  ('等保配置', 2, @support_root_id, 16, '/support/level3protect', '/support/level3protect/level3-protect-config-index.vue', 1, NULL, 'support:level3protect', 'SecurityScanOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('数据脱敏', 2, @support_root_id, 17, '/support/level3protect/masking', '/support/level3protect/data-masking-list.vue', 1, NULL, 'support:level3protect:masking', 'SafetyOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id);

-- 4.16 接口加密
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('接口加密', 2, @support_root_id, 18, '/support/api-encrypt', '/support/api-encrypt/api-encrypt-index.vue', 1, NULL, 'support:api-encrypt', 'LockOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id);

-- 4.17 登录失败记录
INSERT INTO `t_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms_type`, `api_perms`, `web_perms`, `icon`, `context_menu_id`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`, `create_user_id`, `create_time`, `update_user_id`, `update_time`) 
VALUES ('登录失败', 2, @support_root_id, 19, '/support/login-fail', '/support/login-fail/login-fail-list.vue', 1, NULL, 'support:login-fail', 'WarningOutlined', NULL, 0, NULL, 1, 1, 0, 0, 1, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE parent_id = VALUES(parent_id);

-- ==================================================================
-- 第五部分: 角色菜单关联（管理员默认拥有所有权限）
-- ==================================================================

-- 为管理员角色(role_id=1)分配所有顶级目录
INSERT INTO `t_role_menu` (`role_id`, `menu_id`) VALUES (1, @business_root_id) ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);
INSERT INTO `t_role_menu` (`role_id`, `menu_id`) VALUES (1, @system_root_id) ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);
INSERT INTO `t_role_menu` (`role_id`, `menu_id`) VALUES (1, @support_root_id) ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

-- 为管理员角色分配所有一级菜单
INSERT INTO `t_role_menu` (`role_id`, `menu_id`) 
SELECT 1, menu_id FROM t_menu 
WHERE parent_id IN (@business_root_id, @system_root_id, @support_root_id) 
AND menu_type = 1 
AND deleted_flag = 0
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

-- 为管理员角色分配所有二级菜单
INSERT INTO `t_role_menu` (`role_id`, `menu_id`) 
SELECT 1, menu_id FROM t_menu 
WHERE parent_id IN (
  SELECT menu_id FROM t_menu WHERE parent_id IN (@business_root_id, @system_root_id, @support_root_id) AND deleted_flag = 0
) 
AND menu_type = 2 
AND deleted_flag = 0
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

-- 为管理员角色分配所有功能点
INSERT INTO `t_role_menu` (`role_id`, `menu_id`) 
SELECT 1, menu_id FROM t_menu 
WHERE menu_type = 3 
AND deleted_flag = 0
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

-- ==================================================================
-- 第六部分: 数据验证查询
-- ==================================================================

-- 统计各模块菜单数量
SELECT 
  CASE 
    WHEN m1.menu_name IN ('业务管理','系统设置','支撑功能') THEN m1.menu_name
    ELSE CONCAT('└─ ', m2.menu_name)
  END as 模块名称,
  COUNT(DISTINCT m3.menu_id) as 菜单总数,
  SUM(CASE WHEN m3.menu_type = 1 THEN 1 ELSE 0 END) as 目录数,
  SUM(CASE WHEN m3.menu_type = 2 THEN 1 ELSE 0 END) as 菜单数,
  SUM(CASE WHEN m3.menu_type = 3 THEN 1 ELSE 0 END) as 功能点数
FROM t_menu m1
LEFT JOIN t_menu m2 ON m2.parent_id = m1.menu_id
LEFT JOIN t_menu m3 ON (m3.menu_id = m1.menu_id OR m3.menu_id = m2.menu_id OR m3.parent_id = m2.menu_id)
WHERE m1.parent_id = 0 AND m1.deleted_flag = 0
GROUP BY m1.menu_name, m2.menu_name
ORDER BY m1.sort, m2.sort;

-- 验证所有菜单路径完整性
SELECT 
  m.menu_id,
  m.menu_name,
  m.menu_type,
  m.path,
  m.component,
  CASE 
    WHEN m.menu_type = 1 THEN '目录'
    WHEN m.menu_type = 2 THEN '菜单'
    WHEN m.menu_type = 3 THEN '功能点'
  END as 菜单类型,
  CASE 
    WHEN m.menu_type IN (1,2) AND (m.path IS NULL OR m.path = '') THEN '❌路径缺失'
    WHEN m.menu_type = 2 AND (m.component IS NULL OR m.component = '') THEN '❌组件缺失'
    ELSE '✅完整'
  END as 检查结果
FROM t_menu m
WHERE m.deleted_flag = 0
ORDER BY m.menu_id;

-- 提交事务
COMMIT;

-- ==================================================================
-- 执行完成提示
-- ==================================================================
SELECT '✅ IOE-DREAM菜单初始化完成！' as 状态,
       COUNT(*) as 菜单总数
FROM t_menu 
WHERE deleted_flag = 0;

SELECT '📊 菜单统计：' as '统计信息',
       SUM(CASE WHEN menu_type = 1 THEN 1 ELSE 0 END) as 目录数,
       SUM(CASE WHEN menu_type = 2 THEN 1 ELSE 0 END) as 菜单数,
       SUM(CASE WHEN menu_type = 3 THEN 1 ELSE 0 END) as 功能点数
FROM t_menu 
WHERE deleted_flag = 0; 