-- 修复区域管理菜单脚本
-- 用于解决菜单不显示的问题

USE `smart_admin_v3`;

-- 1. 先删除可能已经存在的区域管理菜单及其子菜单
SET @area_menu_id = (SELECT menu_id FROM t_sys_menu WHERE menu_name = '区域管理' AND deleted_flag = 0 LIMIT 1);

-- 删除子菜单（权限点）
DELETE FROM t_sys_menu WHERE parent_id = @area_menu_id;

-- 删除区域管理主菜单
DELETE FROM t_sys_menu WHERE menu_name = '区域管理';

-- 2. 重新插入正确的菜单数据
-- 获取"系统设置"菜单的ID
SET @system_setting_menu_id = (SELECT menu_id FROM t_sys_menu WHERE menu_name = '系统设置' AND deleted_flag = 0 LIMIT 1);

-- 插入区域管理主菜单（注意：component路径不包含views/前缀）
INSERT INTO `t_sys_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms`, `icon`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`) VALUES
('区域管理', 1, @system_setting_menu_id, 6, 'area', 'system/area/index.vue', 'area:page', 'AreaChartOutlined', 0, NULL, 1, 1, 0, 0);

-- 获取新插入的区域管理菜单ID
SET @new_area_menu_id = LAST_INSERT_ID();

-- 插入权限点
INSERT INTO `t_sys_menu` (`menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`, `perms`, `icon`, `frame_flag`, `frame_url`, `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`) VALUES
('查询区域', 3, @new_area_menu_id, 1, NULL, NULL, 'area:page', NULL, 0, NULL, 0, 1, 0, 0),
('区域详情', 3, @new_area_menu_id, 2, NULL, NULL, 'area:detail', NULL, 0, NULL, 0, 1, 0, 0),
('新增区域', 3, @new_area_menu_id, 3, NULL, NULL, 'area:add', NULL, 0, NULL, 0, 1, 0, 0),
('修改区域', 3, @new_area_menu_id, 4, NULL, NULL, 'area:update', NULL, 0, NULL, 0, 1, 0, 0),
('删除区域', 3, @new_area_menu_id, 5, NULL, NULL, 'area:delete', NULL, 0, NULL, 0, 1, 0, 0),
('区域树', 3, @new_area_menu_id, 6, NULL, NULL, 'area:tree', NULL, 0, NULL, 0, 1, 0, 0),
('绑定设备', 3, @new_area_menu_id, 7, NULL, NULL, 'area:device:bind', NULL, 0, NULL, 0, 1, 0, 0),
('解绑设备', 3, @new_area_menu_id, 8, NULL, NULL, 'area:device:unbind', NULL, 0, NULL, 0, 1, 0, 0),
('授予权限', 3, @new_area_menu_id, 9, NULL, NULL, 'area:user:grant', NULL, 0, NULL, 0, 1, 0, 0),
('撤销权限', 3, @new_area_menu_id, 10, NULL, NULL, 'area:user:revoke', NULL, 0, NULL, 0, 1, 0, 0),
('查看配置', 3, @new_area_menu_id, 11, NULL, NULL, 'area:config:view', NULL, 0, NULL, 0, 1, 0, 0),
('更新配置', 3, @new_area_menu_id, 12, NULL, NULL, 'area:config:update', NULL, 0, NULL, 0, 1, 0, 0);

-- 3. 显示结果
SELECT '区域管理菜单修复完成！' AS message;

-- 验证插入结果
SELECT
    m.menu_id,
    m.menu_name,
    m.menu_type,
    m.parent_id,
    m.path,
    m.component,
    m.perms,
    m.visible_flag,
    p.menu_name as parent_menu_name
FROM t_sys_menu m
LEFT JOIN t_sys_menu p ON m.parent_id = p.menu_id
WHERE m.menu_name = '区域管理' OR m.parent_id = @new_area_menu_id
ORDER BY m.sort;
