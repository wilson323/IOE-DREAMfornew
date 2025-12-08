-- ==================================================================
-- Step 3: Insert Level 2 Business Menus
-- ==================================================================
USE ioedream;

SET @access_id = (SELECT menu_id FROM t_menu WHERE menu_name = '门禁管理' AND deleted_flag = 0 LIMIT 1);
SET @attendance_id = (SELECT menu_id FROM t_menu WHERE menu_name = '考勤管理' AND deleted_flag = 0 LIMIT 1);
SET @consume_id = (SELECT menu_id FROM t_menu WHERE menu_name = '消费管理' AND deleted_flag = 0 LIMIT 1);
SET @visitor_id = (SELECT menu_id FROM t_menu WHERE menu_name = '访客管理' AND deleted_flag = 0 LIMIT 1);

-- Access Management Menus
INSERT INTO `t_menu` (menu_name, menu_type, parent_id, sort, path, component, web_perms, icon, perms_type, cache_flag, visible_flag, disabled_flag, deleted_flag, create_user_id, create_time, update_user_id, update_time) 
VALUES 
  ('设备管理', 2, @access_id, 1, '/business/access/device', '/business/access/device/index.vue', 'business:access:device', 'VideoCameraOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('通行记录', 2, @access_id, 2, '/business/access/record', '/business/access/record/index.vue', 'business:access:record', 'HistoryOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('全局联动', 2, @access_id, 3, '/business/access/advanced', '/business/access/advanced/GlobalLinkageManagement.vue', 'business:access:linkage', 'ApiOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW());

-- Attendance Management Menus
INSERT INTO `t_menu` (menu_name, menu_type, parent_id, sort, path, component, web_perms, icon, perms_type, cache_flag, visible_flag, disabled_flag, deleted_flag, create_user_id, create_time, update_user_id, update_time) 
VALUES 
  ('考勤记录', 2, @attendance_id, 1, '/business/attendance/record', '/business/attendance/record/index.vue', 'business:attendance:record', 'ReconciliationOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW());

-- Consumption Management Menus
INSERT INTO `t_menu` (menu_name, menu_type, parent_id, sort, path, component, web_perms, icon, perms_type, cache_flag, visible_flag, disabled_flag, deleted_flag, create_user_id, create_time, update_user_id, update_time) 
VALUES 
  ('账户管理', 2, @consume_id, 1, '/business/consume/account', '/business/consume/account/index.vue', 'business:consume:account', 'WalletOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('交易记录', 2, @consume_id, 2, '/business/consume/transaction', '/business/consume/transaction/index.vue', 'business:consume:transaction', 'TransactionOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('消费报表', 2, @consume_id, 3, '/business/consume/report', '/business/consume/report/index.vue', 'business:consume:report', 'BarChartOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW());

-- Visitor Management Menus
INSERT INTO `t_menu` (menu_name, menu_type, parent_id, sort, path, component, web_perms, icon, perms_type, cache_flag, visible_flag, disabled_flag, deleted_flag, create_user_id, create_time, update_user_id, update_time) 
VALUES 
  ('访客预约', 2, @visitor_id, 1, '/business/visitor/appointment', '/business/visitor/appointment.vue', 'business:visitor:appointment', 'CalendarOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('访客登记', 2, @visitor_id, 2, '/business/visitor/registration', '/business/visitor/registration.vue', 'business:visitor:registration', 'FormOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('访客记录', 2, @visitor_id, 3, '/business/visitor/record', '/business/visitor/record.vue', 'business:visitor:record', 'HistoryOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('黑名单', 2, @visitor_id, 4, '/business/visitor/blacklist', '/business/visitor/blacklist.vue', 'business:visitor:blacklist', 'StopOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW());

-- Role Menu Association
INSERT INTO `t_role_menu` (role_id, menu_id) 
SELECT 1, menu_id FROM t_menu WHERE deleted_flag = 0
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

SELECT 'menu initialization completed' AS status;
SELECT COUNT(*) AS total_menus FROM t_menu WHERE deleted_flag = 0;
