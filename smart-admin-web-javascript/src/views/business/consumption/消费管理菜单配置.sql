-- 消费管理模块菜单配置 SQL
-- Docker执行: Get-Content 消费管理菜单配置.sql -Raw -Encoding UTF8 | docker exec -i mysql-ioe-dream mysql -uroot -proot1234 smart_admin_v3

-- 1. 一级目录：消费管理
INSERT INTO `t_menu` (
  `menu_name`, 
  `menu_type`, 
  `parent_id`, 
  `sort`, 
  `path`, 
  `component`, 
  `perms_type`, 
  `api_perms`, 
  `web_perms`, 
  `icon`, 
  `context_menu_id`, 
  `frame_flag`, 
  `frame_url`, 
  `cache_flag`, 
  `visible_flag`, 
  `disabled_flag`,
  `deleted_flag`,
  `create_user_id`,
  `create_time`,
  `update_user_id`,
  `update_time`
) VALUES (
  '消费管理',                    -- 菜单名称
  1,                            -- 菜单类型：1=目录
  0,                            -- 父级ID：0表示顶级目录
  100,                          -- 排序：数字越小越靠前
  '/business/consumption',      -- 路由路径：一级目录路径
  NULL,                         -- 组件路径：目录不需要
  1,                            -- 权限类型：1=Sa-Token
  NULL,                         -- 后端接口权限
  NULL,                         -- 前端权限标识
  'ShoppingOutlined',           -- 图标：Ant Design 图标名
  NULL,                         -- 右键菜单ID
  0,                            -- 是否外链：0=否
  NULL,                         -- 外链地址
  0,                            -- 是否缓存：0=否
  1,                            -- 是否可见：1=是
  0,                            -- 是否禁用：0=否
  0,                            -- 是否删除：0=否
  1,                            -- 创建人ID
  NOW(),                        -- 创建时间
  1,                            -- 更新人ID
  NOW()                         -- 更新时间
);

SET @consumption_menu_id = LAST_INSERT_ID();

-- 2. 二级菜单：数据总览
INSERT INTO `t_menu` (
  `menu_name`, 
  `menu_type`, 
  `parent_id`, 
  `sort`, 
  `path`, 
  `component`, 
  `perms_type`, 
  `api_perms`, 
  `web_perms`, 
  `icon`, 
  `context_menu_id`, 
  `frame_flag`, 
  `frame_url`, 
  `cache_flag`, 
  `visible_flag`, 
  `disabled_flag`,
  `deleted_flag`,
  `create_user_id`,
  `create_time`,
  `update_user_id`,
  `update_time`
) VALUES (
  '数据总览',                                                   -- 菜单名称
  2,                                                            -- 菜单类型：2=菜单
  @consumption_menu_id,                                         -- ⭐️ 父级ID：使用变量（消费管理目录ID）
  1,                                                            -- 排序
  '/business/consumption/dashboard',                            -- ⭐️ 路由路径（重要）
  '/business/consumption/dashboard/index.vue',                  -- ⭐️ 组件路径（重要）
  1,                                                            -- 权限类型
  NULL,                                                         -- 后端接口权限（可选）
  'business:consumption:dashboard',                             -- 前端权限标识
  'DashboardOutlined',                                          -- 图标
  NULL,                                                         -- 右键菜单ID
  0,                                                            -- 是否外链
  NULL,                                                         -- 外链地址
  1,                                                            -- ⭐️ 是否缓存：1=是（Dashboard建议缓存）
  1,                                                            -- 是否可见
  0,                                                            -- 是否禁用
  0,                                                            -- 是否删除
  1,                                                            -- 创建人ID
  NOW(),                                                        -- 创建时间
  1,                                                            -- 更新人ID
  NOW()                                                         -- 更新时间
);

SET @dashboard_menu_id = LAST_INSERT_ID();

-- 3. 功能点：查询统计数据
INSERT INTO `t_menu` (
  `menu_name`, 
  `menu_type`, 
  `parent_id`, 
  `sort`, 
  `path`, 
  `component`, 
  `perms_type`, 
  `api_perms`, 
  `web_perms`, 
  `icon`, 
  `context_menu_id`, 
  `frame_flag`, 
  `frame_url`, 
  `cache_flag`, 
  `visible_flag`, 
  `disabled_flag`,
  `deleted_flag`,
  `create_user_id`,
  `create_time`,
  `update_user_id`,
  `update_time`
) VALUES (
  '查询统计数据',                                               -- 功能点名称
  3,                                                            -- 菜单类型：3=功能点
  @dashboard_menu_id,                                           -- ⭐️ 父级ID：使用变量（数据总览菜单ID）
  1,                                                            -- 排序
  NULL,                                                         -- 路由路径：功能点不需要
  NULL,                                                         -- 组件路径：功能点不需要
  1,                                                            -- 权限类型
  '/consumption/dashboard/stats',                               -- ⭐️ 后端接口权限
  'business:consumption:dashboard:query',                       -- ⭐️ 前端权限标识
  NULL,                                                         -- 图标
  NULL,                                                         -- 右键菜单ID
  0,                                                            -- 是否外链
  NULL,                                                         -- 外链地址
  0,                                                            -- 是否缓存
  1,                                                            -- 是否可见
  0,                                                            -- 是否禁用
  0,                                                            -- 是否删除
  1,                                                            -- 创建人ID
  NOW(),                                                        -- 创建时间
  1,                                                            -- 更新人ID
  NOW()                                                         -- 更新时间
);

-- 4. 功能点：查询活动列表
INSERT INTO `t_menu` (
  `menu_name`, 
  `menu_type`, 
  `parent_id`, 
  `sort`, 
  `path`, 
  `component`, 
  `perms_type`, 
  `api_perms`, 
  `web_perms`, 
  `icon`, 
  `context_menu_id`, 
  `frame_flag`, 
  `frame_url`, 
  `cache_flag`, 
  `visible_flag`, 
  `disabled_flag`,
  `deleted_flag`,
  `create_user_id`,
  `create_time`,
  `update_user_id`,
  `update_time`
) VALUES (
  '查询活动列表',                                               -- 功能点名称
  3,                                                            -- 菜单类型：3=功能点
  @dashboard_menu_id,                                           -- ⭐️ 父级ID：使用变量（数据总览菜单ID）
  2,                                                            -- 排序
  NULL,                                                         -- 路由路径：功能点不需要
  NULL,                                                         -- 组件路径：功能点不需要
  1,                                                            -- 权限类型
  '/consumption/dashboard/activities',                          -- ⭐️ 后端接口权限
  'business:consumption:dashboard:activity',                    -- ⭐️ 前端权限标识
  NULL,                                                         -- 图标
  NULL,                                                         -- 右键菜单ID
  0,                                                            -- 是否外链
  NULL,                                                         -- 外链地址
  0,                                                            -- 是否缓存
  1,                                                            -- 是否可见
  0,                                                            -- 是否禁用
  0,                                                            -- 是否删除
  1,                                                            -- 创建人ID
  NOW(),                                                        -- 创建时间
  1,                                                            -- 更新人ID
  NOW()                                                         -- 更新时间
);

-- 5. 二级菜单：区域管理
INSERT INTO `t_menu` (
  `menu_name`, 
  `menu_type`, 
  `parent_id`, 
  `sort`, 
  `path`, 
  `component`, 
  `perms_type`, 
  `api_perms`, 
  `web_perms`, 
  `icon`, 
  `context_menu_id`, 
  `frame_flag`, 
  `frame_url`, 
  `cache_flag`, 
  `visible_flag`, 
  `disabled_flag`,
  `deleted_flag`,
  `create_user_id`,
  `create_time`,
  `update_user_id`,
  `update_time`
) VALUES (
  '区域管理',
  2,
  @consumption_menu_id,
  2,
  '/business/consumption/region',
  '/business/consumption/region/index.vue',
  1,
  NULL,
  'business:consumption:region',
  'ApartmentOutlined',
  NULL,
  0,
  NULL,
  1,
  1,
  0,
  0,
  1,
  NOW(),
  1,
  NOW()
);

SET @region_menu_id = LAST_INSERT_ID();

-- 6. 功能点：区域查询
INSERT INTO `t_menu` (
  `menu_name`, 
  `menu_type`, 
  `parent_id`, 
  `sort`, 
  `path`, 
  `component`, 
  `perms_type`, 
  `api_perms`, 
  `web_perms`, 
  `icon`, 
  `context_menu_id`, 
  `frame_flag`, 
  `frame_url`, 
  `cache_flag`, 
  `visible_flag`, 
  `disabled_flag`,
  `deleted_flag`,
  `create_user_id`,
  `create_time`,
  `update_user_id`,
  `update_time`
) VALUES (
  '区域查询',
  3,
  @region_menu_id,
  1,
  NULL,
  NULL,
  1,
  '/consumption/region/query',
  'business:consumption:region:query',
  NULL,
  NULL,
  0,
  NULL,
  0,
  1,
  0,
  0,
  1,
  NOW(),
  1,
  NOW()
);

-- 7. 功能点：区域新增
INSERT INTO `t_menu` (
  `menu_name`, 
  `menu_type`, 
  `parent_id`, 
  `sort`, 
  `path`, 
  `component`, 
  `perms_type`, 
  `api_perms`, 
  `web_perms`, 
  `icon`, 
  `context_menu_id`, 
  `frame_flag`, 
  `frame_url`, 
  `cache_flag`, 
  `visible_flag`, 
  `disabled_flag`,
  `deleted_flag`,
  `create_user_id`,
  `create_time`,
  `update_user_id`,
  `update_time`
) VALUES (
  '区域新增',
  3,
  @region_menu_id,
  2,
  NULL,
  NULL,
  1,
  '/consumption/region/add',
  'business:consumption:region:add',
  NULL,
  NULL,
  0,
  NULL,
  0,
  1,
  0,
  0,
  1,
  NOW(),
  1,
  NOW()
);

-- 8. 功能点：区域编辑
INSERT INTO `t_menu` (
  `menu_name`, 
  `menu_type`, 
  `parent_id`, 
  `sort`, 
  `path`, 
  `component`, 
  `perms_type`, 
  `api_perms`, 
  `web_perms`, 
  `icon`, 
  `context_menu_id`, 
  `frame_flag`, 
  `frame_url`, 
  `cache_flag`, 
  `visible_flag`, 
  `disabled_flag`,
  `deleted_flag`,
  `create_user_id`,
  `create_time`,
  `update_user_id`,
  `update_time`
) VALUES (
  '区域编辑',
  3,
  @region_menu_id,
  3,
  NULL,
  NULL,
  1,
  '/consumption/region/update',
  'business:consumption:region:update',
  NULL,
  NULL,
  0,
  NULL,
  0,
  1,
  0,
  0,
  1,
  NOW(),
  1,
  NOW()
);

-- 9. 功能点：区域删除
INSERT INTO `t_menu` (
  `menu_name`, 
  `menu_type`, 
  `parent_id`, 
  `sort`, 
  `path`, 
  `component`, 
  `perms_type`, 
  `api_perms`, 
  `web_perms`, 
  `icon`, 
  `context_menu_id`, 
  `frame_flag`, 
  `frame_url`, 
  `cache_flag`, 
  `visible_flag`, 
  `disabled_flag`,
  `deleted_flag`,
  `create_user_id`,
  `create_time`,
  `update_user_id`,
  `update_time`
) VALUES (
  '区域删除',
  3,
  @region_menu_id,
  4,
  NULL,
  NULL,
  1,
  '/consumption/region/delete',
  'business:consumption:region:delete',
  NULL,
  NULL,
  0,
  NULL,
  0,
  1,
  0,
  0,
  1,
  NOW(),
  1,
  NOW()
);

-- 10. 角色菜单关联
INSERT INTO `t_role_menu` (`role_id`, `menu_id`) VALUES (1, @consumption_menu_id);
INSERT INTO `t_role_menu` (`role_id`, `menu_id`) VALUES (1, @dashboard_menu_id);
INSERT INTO `t_role_menu` (`role_id`, `menu_id`) SELECT 1, menu_id FROM t_menu WHERE parent_id = @dashboard_menu_id AND web_perms = 'business:consumption:dashboard:query';
INSERT INTO `t_role_menu` (`role_id`, `menu_id`) SELECT 1, menu_id FROM t_menu WHERE parent_id = @dashboard_menu_id AND web_perms = 'business:consumption:dashboard:activity';
INSERT INTO `t_role_menu` (`role_id`, `menu_id`) VALUES (1, @region_menu_id);
INSERT INTO `t_role_menu` (`role_id`, `menu_id`) SELECT 1, menu_id FROM t_menu WHERE parent_id = @region_menu_id;

-- 验证查询
SELECT menu_id, menu_name, menu_type, parent_id, path FROM t_menu WHERE menu_name LIKE '%消费%' OR parent_id IN (SELECT menu_id FROM t_menu WHERE menu_name = '消费管理') ORDER BY parent_id, sort;

