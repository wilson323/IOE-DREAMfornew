-- 区域管理菜单修复脚本 - 最终版本
-- 将区域管理菜单添加到"系统设置"(menu_id=50)下

USE `smart_admin_v3`;

-- 1. 先删除可能已经存在的区域管理菜单及其子菜单
SET @area_menu_id = (SELECT menu_id FROM t_sys_menu WHERE menu_name = '区域管理' AND deleted_flag = 0 LIMIT 1);

-- 删除子菜单（权限点）
DELETE FROM t_sys_menu WHERE parent_id = @area_menu_id;

-- 删除区域管理主菜单
DELETE FROM t_sys_menu WHERE menu_name = '区域管理';

SELECT '已清理旧的区域管理菜单' AS step1;

-- 2. 插入区域管理主菜单到"系统设置"(menu_id=50)下
INSERT INTO `t_sys_menu` (
    `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`,
    `perms`, `icon`, `frame_flag`, `frame_url`, `cache_flag`,
    `visible_flag`, `disabled_flag`, `deleted_flag`
) VALUES (
    '区域管理',           -- 菜单名称
    1,                   -- 菜单类型：1=菜单
    50,                  -- 父菜单ID：系统设置
    6,                   -- 排序
    'area',              -- 路由路径
    'system/area/index.vue',  -- 组件路径（不包含views/前缀）
    'area:page',         -- 权限标识
    'AreaChartOutlined', -- 图标
    0,                   -- 是否外链：0=否
    NULL,                -- 外链地址
    1,                   -- 是否缓存：1=是
    1,                   -- 是否可见：1=是
    0,                   -- 是否禁用：0=否
    0                    -- 删除标志：0=未删除
);

-- 获取新插入的区域管理菜单ID
SET @new_area_menu_id = LAST_INSERT_ID();

SELECT CONCAT('已创建区域管理菜单，menu_id = ', @new_area_menu_id) AS step2;

-- 3. 插入权限点（子菜单，menu_type=3）
INSERT INTO `t_sys_menu` (
    `menu_name`, `menu_type`, `parent_id`, `sort`, `path`, `component`,
    `perms`, `icon`, `frame_flag`, `frame_url`, `cache_flag`,
    `visible_flag`, `disabled_flag`, `deleted_flag`
) VALUES
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

SELECT '已创建12个权限点' AS step3;

-- 4. 验证插入结果
SELECT '=' AS separator, '菜单创建结果验证' AS title, '=' AS separator2;

SELECT
    m.menu_id,
    m.menu_name,
    m.menu_type AS 'type(1=菜单,3=权限)',
    m.parent_id,
    p.menu_name AS parent_name,
    m.sort,
    m.path,
    m.component,
    m.perms,
    m.visible_flag AS visible,
    m.disabled_flag AS disabled
FROM t_sys_menu m
LEFT JOIN t_sys_menu p ON m.parent_id = p.menu_id
WHERE (m.menu_name = '区域管理' OR m.parent_id = @new_area_menu_id)
AND m.deleted_flag = 0
ORDER BY m.parent_id, m.sort;

-- 5. 完成提示
SELECT
    '✅ 区域管理菜单创建完成！' AS message,
    '请重新登录系统，即可在"系统设置"菜单下看到"区域管理"' AS next_step;
