-- ==================================================================
-- Step 5: 添加所有功能点权限 (menu_type=3)
-- ==================================================================
USE ioedream;

-- 获取各个菜单的ID
SET @access_device_id = (SELECT menu_id FROM t_menu WHERE menu_name = '设备管理' AND path = '/business/access/device' LIMIT 1);
SET @access_record_id = (SELECT menu_id FROM t_menu WHERE menu_name = '通行记录' AND path = '/business/access/record' LIMIT 1);
SET @attendance_record_id = (SELECT menu_id FROM t_menu WHERE menu_name = '考勤记录' AND path = '/business/attendance/record' LIMIT 1);
SET @consume_account_id = (SELECT menu_id FROM t_menu WHERE menu_name = '账户管理' AND path = '/business/consume/account' LIMIT 1);
SET @consume_transaction_id = (SELECT menu_id FROM t_menu WHERE menu_name = '交易记录' AND path = '/business/consume/transaction' LIMIT 1);
SET @visitor_appointment_id = (SELECT menu_id FROM t_menu WHERE menu_name = '访客预约' LIMIT 1);
SET @visitor_registration_id = (SELECT menu_id FROM t_menu WHERE menu_name = '访客登记' LIMIT 1);
SET @employee_id = (SELECT menu_id FROM t_menu WHERE menu_name = '员工管理' LIMIT 1);
SET @department_id = (SELECT menu_id FROM t_menu WHERE menu_name = '部门管理' LIMIT 1);
SET @role_id = (SELECT menu_id FROM t_menu WHERE menu_name = '角色管理' LIMIT 1);
SET @menu_id = (SELECT menu_id FROM t_menu WHERE menu_name = '菜单管理' LIMIT 1);

-- ==================================================================
-- 门禁-设备管理功能点
-- ==================================================================
INSERT INTO `t_menu` (menu_name, menu_type, parent_id, sort, perms_type, api_perms, web_perms, visible_flag, disabled_flag, deleted_flag, create_user_id, create_time, update_user_id, update_time) 
VALUES 
  ('设备查询', 3, @access_device_id, 1, 1, '/access/device/query', 'business:access:device:query', 1, 0, 0, 1, NOW(), 1, NOW()),
  ('设备新增', 3, @access_device_id, 2, 1, '/access/device/add', 'business:access:device:add', 1, 0, 0, 1, NOW(), 1, NOW()),
  ('设备编辑', 3, @access_device_id, 3, 1, '/access/device/update', 'business:access:device:update', 1, 0, 0, 1, NOW(), 1, NOW()),
  ('设备删除', 3, @access_device_id, 4, 1, '/access/device/delete', 'business:access:device:delete', 1, 0, 0, 1, NOW(), 1, NOW()),
  ('设备控制', 3, @access_device_id, 5, 1, '/access/device/control', 'business:access:device:control', 1, 0, 0, 1, NOW(), 1, NOW()),
  ('设备导入', 3, @access_device_id, 6, 1, '/access/device/import', 'business:access:device:import', 1, 0, 0, 1, NOW(), 1, NOW()),
  ('设备导出', 3, @access_device_id, 7, 1, '/access/device/export', 'business:access:device:export', 1, 0, 0, 1, NOW(), 1, NOW());

-- ==================================================================
-- 门禁-通行记录功能点
-- ==================================================================
INSERT INTO `t_menu` (menu_name, menu_type, parent_id, sort, perms_type, api_perms, web_perms, visible_flag, disabled_flag, deleted_flag, create_user_id, create_time, update_user_id, update_time) 
VALUES 
  ('记录查询', 3, @access_record_id, 1, 1, '/access/record/query', 'business:access:record:query', 1, 0, 0, 1, NOW(), 1, NOW()),
  ('记录导出', 3, @access_record_id, 2, 1, '/access/record/export', 'business:access:record:export', 1, 0, 0, 1, NOW(), 1, NOW()),
  ('记录统计', 3, @access_record_id, 3, 1, '/access/record/statistics', 'business:access:record:statistics', 1, 0, 0, 1, NOW(), 1, NOW());

-- ==================================================================
-- 考勤-考勤记录功能点
-- ==================================================================
INSERT INTO `t_menu` (menu_name, menu_type, parent_id, sort, perms_type, api_perms, web_perms, visible_flag, disabled_flag, deleted_flag, create_user_id, create_time, update_user_id, update_time) 
VALUES 
  ('考勤查询', 3, @attendance_record_id, 1, 1, '/attendance/record/query', 'business:attendance:record:query', 1, 0, 0, 1, NOW(), 1, NOW()),
  ('考勤导出', 3, @attendance_record_id, 2, 1, '/attendance/record/export', 'business:attendance:record:export', 1, 0, 0, 1, NOW(), 1, NOW()),
  ('补卡管理', 3, @attendance_record_id, 3, 1, '/attendance/record/makeup', 'business:attendance:record:makeup', 1, 0, 0, 1, NOW(), 1, NOW()),
  ('考勤统计', 3, @attendance_record_id, 4, 1, '/attendance/record/statistics', 'business:attendance:record:statistics', 1, 0, 0, 1, NOW(), 1, NOW());

-- ==================================================================
-- 消费-账户管理功能点
-- ==================================================================
INSERT INTO `t_menu` (menu_name, menu_type, parent_id, sort, perms_type, api_perms, web_perms, visible_flag, disabled_flag, deleted_flag, create_user_id, create_time, update_user_id, update_time) 
VALUES 
  ('账户查询', 3, @consume_account_id, 1, 1, '/consume/account/query', 'business:consume:account:query', 1, 0, 0, 1, NOW(), 1, NOW()),
  ('账户充值', 3, @consume_account_id, 2, 1, '/consume/account/recharge', 'business:consume:account:recharge', 1, 0, 0, 1, NOW(), 1, NOW()),
  ('账户退款', 3, @consume_account_id, 3, 1, '/consume/account/refund', 'business:consume:account:refund', 1, 0, 0, 1, NOW(), 1, NOW()),
  ('账户冻结', 3, @consume_account_id, 4, 1, '/consume/account/freeze', 'business:consume:account:freeze', 1, 0, 0, 1, NOW(), 1, NOW()),
  ('账户解冻', 3, @consume_account_id, 5, 1, '/consume/account/unfreeze', 'business:consume:account:unfreeze', 1, 0, 0, 1, NOW(), 1, NOW());

-- ==================================================================
-- 消费-交易记录功能点
-- ==================================================================
INSERT INTO `t_menu` (menu_name, menu_type, parent_id, sort, perms_type, api_perms, web_perms, visible_flag, disabled_flag, deleted_flag, create_user_id, create_time, update_user_id, update_time) 
VALUES 
  ('交易查询', 3, @consume_transaction_id, 1, 1, '/consume/transaction/query', 'business:consume:transaction:query', 1, 0, 0, 1, NOW(), 1, NOW()),
  ('交易导出', 3, @consume_transaction_id, 2, 1, '/consume/transaction/export', 'business:consume:transaction:export', 1, 0, 0, 1, NOW(), 1, NOW()),
  ('交易统计', 3, @consume_transaction_id, 3, 1, '/consume/transaction/statistics', 'business:consume:transaction:statistics', 1, 0, 0, 1, NOW(), 1, NOW());

-- ==================================================================
-- 访客-访客预约功能点
-- ==================================================================
INSERT INTO `t_menu` (menu_name, menu_type, parent_id, sort, perms_type, api_perms, web_perms, visible_flag, disabled_flag, deleted_flag, create_user_id, create_time, update_user_id, update_time) 
VALUES 
  ('预约查询', 3, @visitor_appointment_id, 1, 1, '/visitor/appointment/query', 'business:visitor:appointment:query', 1, 0, 0, 1, NOW(), 1, NOW()),
  ('预约新增', 3, @visitor_appointment_id, 2, 1, '/visitor/appointment/add', 'business:visitor:appointment:add', 1, 0, 0, 1, NOW(), 1, NOW()),
  ('预约编辑', 3, @visitor_appointment_id, 3, 1, '/visitor/appointment/update', 'business:visitor:appointment:update', 1, 0, 0, 1, NOW(), 1, NOW()),
  ('预约取消', 3, @visitor_appointment_id, 4, 1, '/visitor/appointment/cancel', 'business:visitor:appointment:cancel', 1, 0, 0, 1, NOW(), 1, NOW()),
  ('预约审批', 3, @visitor_appointment_id, 5, 1, '/visitor/appointment/approve', 'business:visitor:appointment:approve', 1, 0, 0, 1, NOW(), 1, NOW());

-- ==================================================================
-- 访客-访客登记功能点
-- ==================================================================
INSERT INTO `t_menu` (menu_name, menu_type, parent_id, sort, perms_type, api_perms, web_perms, visible_flag, disabled_flag, deleted_flag, create_user_id, create_time, update_user_id, update_time) 
VALUES 
  ('登记查询', 3, @visitor_registration_id, 1, 1, '/visitor/registration/query', 'business:visitor:registration:query', 1, 0, 0, 1, NOW(), 1, NOW()),
  ('登记新增', 3, @visitor_registration_id, 2, 1, '/visitor/registration/add', 'business:visitor:registration:add', 1, 0, 0, 1, NOW(), 1, NOW()),
  ('登记签出', 3, @visitor_registration_id, 3, 1, '/visitor/registration/checkout', 'business:visitor:registration:checkout', 1, 0, 0, 1, NOW(), 1, NOW());

-- ==================================================================
-- 系统-员工管理功能点
-- ==================================================================
INSERT INTO `t_menu` (menu_name, menu_type, parent_id, sort, perms_type, api_perms, web_perms, visible_flag, disabled_flag, deleted_flag, create_user_id, create_time, update_user_id, update_time) 
VALUES 
  ('员工查询', 3, @employee_id, 1, 1, '/employee/query', 'system:employee:query', 1, 0, 0, 1, NOW(), 1, NOW()),
  ('员工新增', 3, @employee_id, 2, 1, '/employee/add', 'system:employee:add', 1, 0, 0, 1, NOW(), 1, NOW()),
  ('员工编辑', 3, @employee_id, 3, 1, '/employee/update', 'system:employee:update', 1, 0, 0, 1, NOW(), 1, NOW()),
  ('员工删除', 3, @employee_id, 4, 1, '/employee/delete', 'system:employee:delete', 1, 0, 0, 1, NOW(), 1, NOW()),
  ('员工导入', 3, @employee_id, 5, 1, '/employee/import', 'system:employee:import', 1, 0, 0, 1, NOW(), 1, NOW()),
  ('员工导出', 3, @employee_id, 6, 1, '/employee/export', 'system:employee:export', 1, 0, 0, 1, NOW(), 1, NOW()),
  ('重置密码', 3, @employee_id, 7, 1, '/employee/resetPassword', 'system:employee:resetPassword', 1, 0, 0, 1, NOW(), 1, NOW());

-- ==================================================================
-- 系统-部门管理功能点
-- ==================================================================
INSERT INTO `t_menu` (menu_name, menu_type, parent_id, sort, perms_type, api_perms, web_perms, visible_flag, disabled_flag, deleted_flag, create_user_id, create_time, update_user_id, update_time) 
VALUES 
  ('部门查询', 3, @department_id, 1, 1, '/department/query', 'system:department:query', 1, 0, 0, 1, NOW(), 1, NOW()),
  ('部门新增', 3, @department_id, 2, 1, '/department/add', 'system:department:add', 1, 0, 0, 1, NOW(), 1, NOW()),
  ('部门编辑', 3, @department_id, 3, 1, '/department/update', 'system:department:update', 1, 0, 0, 1, NOW(), 1, NOW()),
  ('部门删除', 3, @department_id, 4, 1, '/department/delete', 'system:department:delete', 1, 0, 0, 1, NOW(), 1, NOW());

-- ==================================================================
-- 系统-角色管理功能点
-- ==================================================================
INSERT INTO `t_menu` (menu_name, menu_type, parent_id, sort, perms_type, api_perms, web_perms, visible_flag, disabled_flag, deleted_flag, create_user_id, create_time, update_user_id, update_time) 
VALUES 
  ('角色查询', 3, @role_id, 1, 1, '/role/query', 'system:role:query', 1, 0, 0, 1, NOW(), 1, NOW()),
  ('角色新增', 3, @role_id, 2, 1, '/role/add', 'system:role:add', 1, 0, 0, 1, NOW(), 1, NOW()),
  ('角色编辑', 3, @role_id, 3, 1, '/role/update', 'system:role:update', 1, 0, 0, 1, NOW(), 1, NOW()),
  ('角色删除', 3, @role_id, 4, 1, '/role/delete', 'system:role:delete', 1, 0, 0, 1, NOW(), 1, NOW()),
  ('分配权限', 3, @role_id, 5, 1, '/role/dataScope', 'system:role:dataScope', 1, 0, 0, 1, NOW(), 1, NOW());

-- ==================================================================
-- 系统-菜单管理功能点
-- ==================================================================
INSERT INTO `t_menu` (menu_name, menu_type, parent_id, sort, perms_type, api_perms, web_perms, visible_flag, disabled_flag, deleted_flag, create_user_id, create_time, update_user_id, update_time) 
VALUES 
  ('菜单查询', 3, @menu_id, 1, 1, '/menu/query', 'system:menu:query', 1, 0, 0, 1, NOW(), 1, NOW()),
  ('菜单新增', 3, @menu_id, 2, 1, '/menu/add', 'system:menu:add', 1, 0, 0, 1, NOW(), 1, NOW()),
  ('菜单编辑', 3, @menu_id, 3, 1, '/menu/update', 'system:menu:update', 1, 0, 0, 1, NOW(), 1, NOW()),
  ('菜单删除', 3, @menu_id, 4, 1, '/menu/delete', 'system:menu:delete', 1, 0, 0, 1, NOW(), 1, NOW());

-- 更新角色菜单关联
INSERT INTO `t_role_menu` (role_id, menu_id) 
SELECT 1, menu_id FROM t_menu WHERE deleted_flag = 0
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

-- 最终验证
SELECT 'All permissions added successfully!' AS status;
SELECT 
  menu_type,
  CASE menu_type 
    WHEN 1 THEN 'Directory'
    WHEN 2 THEN 'Menu' 
    WHEN 3 THEN 'Permission'
  END AS type_name,
  COUNT(*) AS count
FROM t_menu 
WHERE deleted_flag = 0
GROUP BY menu_type
ORDER BY menu_type;

SELECT COUNT(*) AS total_menu_count FROM t_menu WHERE deleted_flag = 0;
