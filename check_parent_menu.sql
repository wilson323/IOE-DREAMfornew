-- 查询所有一级菜单（没有父菜单的菜单）
SELECT
    menu_id,
    menu_name,
    menu_type,
    parent_id,
    sort,
    path,
    icon,
    visible_flag
FROM t_sys_menu
WHERE parent_id = 0
AND deleted_flag = 0
ORDER BY sort;

-- 查询"系统设置"相关菜单
SELECT
    menu_id,
    menu_name,
    menu_type,
    parent_id
FROM t_sys_menu
WHERE menu_name LIKE '%系统%'
AND deleted_flag = 0
ORDER BY parent_id, sort;

-- 查询"区域管理"菜单的配置
SELECT
    m.menu_id,
    m.menu_name,
    m.parent_id,
    p.menu_name as parent_menu_name,
    m.path,
    m.component,
    m.visible_flag
FROM t_sys_menu m
LEFT JOIN t_sys_menu p ON m.parent_id = p.menu_id
WHERE m.menu_name = '区域管理'
AND m.deleted_flag = 0;
