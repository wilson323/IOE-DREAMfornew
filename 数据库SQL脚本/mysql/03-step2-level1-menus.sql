-- ==================================================================
-- Step 2: 插入一级业务菜单
-- ==================================================================
USE ioedream;

-- 获取业务管理ID (假设是第一个顶级菜单)
SET @business_id = (SELECT menu_id FROM t_menu WHERE menu_name = '业务管理' AND parent_id = 0 LIMIT 1);
SET @system_id = (SELECT menu_id FROM t_menu WHERE menu_name = '系统设置' AND parent_id = 0 LIMIT 1);
SET @support_id = (SELECT menu_id FROM t_menu WHERE menu_name = '支撑功能' AND parent_id = 0 LIMIT 1);

-- 插入业务管理下的一级菜单
INSERT INTO `t_menu` (menu_name, menu_type, parent_id, sort, path, icon, perms_type, visible_flag, disabled_flag, deleted_flag, create_user_id, create_time, update_user_id, update_time) 
VALUES 
  ('门禁管理', 1, @business_id, 10, '/business/access', 'LockOutlined', 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('考勤管理', 1, @business_id, 20, '/business/attendance', 'ClockCircleOutlined', 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('消费管理', 1, @business_id, 30, '/business/consume', 'ShoppingOutlined', 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('访客管理', 1, @business_id, 40, '/business/visitor', 'TeamOutlined', 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('智能视频', 1, @business_id, 50, '/business/smart-video', 'VideoCameraOutlined', 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('OA办公', 1, @business_id, 60, '/business/oa', 'FileTextOutlined', 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('ERP管理', 1, @business_id, 70, '/business/erp', 'ShopOutlined', 1, 1, 0, 0, 1, NOW(), 1, NOW());

-- 插入系统设置下的菜单
INSERT INTO `t_menu` (menu_name, menu_type, parent_id, sort, path, component, web_perms, icon, perms_type, cache_flag, visible_flag, disabled_flag, deleted_flag, create_user_id, create_time, update_user_id, update_time) 
VALUES 
  ('首页', 2, @system_id, 5, '/system/home', '/system/home/index.vue', 'system:home', 'HomeOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('员工管理', 2, @system_id, 10, '/system/employee', '/system/employee/index.vue', 'system:employee', 'UserOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('部门管理', 2, @system_id, 20, '/system/department', '/system/department/index.vue', 'system:department', 'ApartmentOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('角色管理', 2, @system_id, 30, '/system/role', '/system/role/index.vue', 'system:role', 'TeamOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('菜单管理', 2, @system_id, 40, '/system/menu', '/system/menu/index.vue', 'system:menu', 'MenuOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('区域管理', 2, @system_id, 50, '/system/area', '/system/area/index.vue', 'system:area', 'EnvironmentOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('岗位管理', 2, @system_id, 60, '/system/position', '/system/position/index.vue', 'system:position', 'SolutionOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW());

-- 插入支撑功能下的菜单
INSERT INTO `t_menu` (menu_name, menu_type, parent_id, sort, path, component, web_perms, icon, perms_type, cache_flag, visible_flag, disabled_flag, deleted_flag, create_user_id, create_time, update_user_id, update_time) 
VALUES 
  ('数据字典', 2, @support_id, 10, '/support/dict', '/support/dict/index.vue', 'support:dict', 'BookOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('系统配置', 2, @support_id, 20, '/support/config', '/support/config/index.vue', 'support:config', 'SettingOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('文件服务', 2, @support_id, 30, '/support/file', '/support/file/index.vue', 'support:file', 'FileOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('操作日志', 2, @support_id, 40, '/support/operate-log', '/support/operate-log/index.vue', 'support:operateLog', 'ProfileOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('登录日志', 2, @support_id, 50, '/support/login-log', '/support/login-log/index.vue', 'support:loginLog', 'LoginOutlined', 1, 1, 1, 0, 0, 1, NOW(), 1, NOW());

SELECT '✅ 一级菜单创建完成' AS status;
SELECT m1.menu_name AS 顶级菜单, COUNT(m2.menu_id) AS 子菜单数 
FROM t_menu m1 
LEFT JOIN t_menu m2 ON m2.parent_id = m1.menu_id AND m2.deleted_flag = 0
WHERE m1.parent_id = 0 AND m1.deleted_flag = 0 
GROUP BY m1.menu_id, m1.menu_name 
ORDER BY m1.sort;
