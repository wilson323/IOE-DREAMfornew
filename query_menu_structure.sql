-- 查询系统中所有一级菜单（顶级菜单）
SELECT
    menu_id,
    menu_name,
    menu_type,
    sort,
    icon,
    visible_flag
FROM t_sys_menu
WHERE parent_id = 0
AND deleted_flag = 0
ORDER BY sort;

-- 查询所有包含"系统"的菜单
SELECT
    m.menu_id,
    m.menu_name,
    m.menu_type,
    m.parent_id,
    p.menu_name as parent_menu_name,
    m.sort
FROM t_sys_menu m
LEFT JOIN t_sys_menu p ON m.parent_id = p.menu_id
WHERE m.menu_name LIKE '%系统%'
AND m.deleted_flag = 0
ORDER BY m.parent_id, m.sort;

-- 查询"区域管理"当前的配置
SELECT
    m.menu_id,
    m.menu_name,
    m.parent_id,
    p.menu_name as parent_menu_name,
    m.path,
    m.component,
    m.visible_flag,
    m.disabled_flag
FROM t_sys_menu m
LEFT JOIN t_sys_menu p ON m.parent_id = p.menu_id
WHERE m.menu_name = '区域管理'
AND m.deleted_flag = 0;
