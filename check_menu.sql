-- 检查系统设置下的子菜单配置
SELECT
    m.menu_id,
    m.menu_name,
    m.menu_type,
    m.parent_id,
    m.path,
    m.component,
    m.perms,
    m.visible_flag,
    m.disabled_flag,
    p.menu_name as parent_menu_name
FROM t_sys_menu m
LEFT JOIN t_sys_menu p ON m.parent_id = p.menu_id
WHERE p.menu_name = '系统设置' AND m.deleted_flag = 0
ORDER BY m.sort;

-- 检查区域管理菜单
SELECT
    menu_id,
    menu_name,
    menu_type,
    parent_id,
    path,
    component,
    perms,
    visible_flag,
    disabled_flag
FROM t_sys_menu
WHERE menu_name = '区域管理' AND deleted_flag = 0;
