-- ==================================================================
-- Step 1: 插入顶级菜单
-- ==================================================================
USE ioedream;

INSERT INTO `t_menu` (menu_name, menu_type, parent_id, sort, path, icon, perms_type, visible_flag, disabled_flag, deleted_flag, create_user_id, create_time, update_user_id, update_time) 
VALUES 
  ('业务管理', 1, 0, 10, '/business', 'AppstoreOutlined', 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('系统设置', 1, 0, 90, '/system', 'SettingOutlined', 1, 1, 0, 0, 1, NOW(), 1, NOW()),
  ('支撑功能', 1, 0, 95, '/support', 'ToolOutlined', 1, 1, 0, 0, 1, NOW(), 1, NOW());

SELECT '✅ 顶级菜单创建完成' AS status;
SELECT menu_id, menu_name FROM t_menu WHERE parent_id = 0 ORDER BY sort;
