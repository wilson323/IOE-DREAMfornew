-- =====================================================
-- IOE-DREAM 智慧园区一卡通管理平台 - 完整菜单初始化脚本
-- 版本: v1.0.0 企业级七微服务架构版
-- 创建日期: 2025-01-08
-- 说明:
-- - 严格按照七微服务架构设计菜单体系
-- - 前后端路由完全匹配，确保页面正确加载
-- - 企业级权限控制设计，支持RBAC权限模型
-- - 符合四层架构规范和项目编码规范
-- =====================================================

-- 清空现有菜单数据（仅用于开发环境）
-- DELETE FROM t_role_menu;
-- DELETE FROM t_menu;
-- ALTER TABLE t_menu AUTO_INCREMENT = 1;

-- =====================================================
-- 1. 基础系统模块菜单 (ioedream-common-service)
-- =====================================================

-- 1.1 一级目录：系统管理
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`,
  `component`, `perms_type`, `web_perms`, `icon`,
  `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES (
  '系统管理', 1, 0, 1, '/system', NULL, 1,
  'system:manage', 'SettingOutlined', 0, 1, 0, 0,
  1, NOW(), 1, NOW()
);

SET @system_menu_id = LAST_INSERT_ID();

-- 1.2 系统管理子菜单
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`,
  `component`, `web_perms`, `icon`, `cache_flag`,
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES
-- 用户管理
('用户管理', 2, @system_menu_id, 1, '/system/account',
 '/system/account/index.vue', 'system:account', 'UserOutlined', 1, 1, 0, 0, 1, NOW(), 1, NOW()),
-- 角色管理
('角色管理', 2, @system_menu_id, 2, '/system/role',
 '/system/role/index.vue', 'system:role', 'SafetyCertificateOutlined', 1, 1, 0, 0, 1, NOW(), 1, NOW()),
-- 菜单管理
('菜单管理', 2, @system_menu_id, 3, '/system/menu',
 '/system/menu/index.vue', 'system:menu', 'MenuOutlined', 1, 1, 0, 0, 1, NOW(), 1, NOW()),
-- 部门管理
('部门管理', 2, @system_menu_id, 4, '/system/department',
 '/system/department/index.vue', 'system:department', 'ApartmentOutlined', 1, 1, 0, 0, 1, NOW(), 1, NOW()),
-- 字典管理
('字典管理', 2, @system_menu_id, 5, '/system/dict',
 '/system/dict/index.vue', 'system:dict', 'OrderedListOutlined', 1, 1, 0, 0, 1, NOW(), 1, NOW()),
-- 操作日志
('操作日志', 2, @system_menu_id, 6, '/system/operate-log',
 '/system/operate-log/index.vue', 'system:log', 'FileTextOutlined', 0, 1, 0, 0, 1, NOW(), 1, NOW()),
-- 登录日志
('登录日志', 2, @system_menu_id, 7, '/system/login-log',
 '/system/login-log/index.vue', 'system:login-log', 'LoginOutlined', 0, 1, 0, 0, 1, NOW(), 1, NOW());

-- 获取系统管理子菜单ID用于功能点创建
SELECT @user_menu_id := menu_id FROM t_menu WHERE menu_name = '用户管理' LIMIT 1;
SELECT @role_menu_id := menu_id FROM t_menu WHERE menu_name = '角色管理' LIMIT 1;
SELECT @menu_mgmt_id := menu_id FROM t_menu WHERE menu_name = '菜单管理' LIMIT 1;
SELECT @dept_menu_id := menu_id FROM t_menu WHERE menu_name = '部门管理' LIMIT 1;
SELECT @dict_menu_id := menu_id FROM t_menu WHERE menu_name = '字典管理' LIMIT 1;

-- 1.3 用户管理功能点
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`,
  `api_perms`, `web_perms`,
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES
('用户新增', 3, @user_menu_id, 1, '/system/account/add', 'system:account:add', 1, 0, 0, 1, NOW(), 1, NOW()),
('用户更新', 3, @user_menu_id, 2, '/system/account/update', 'system:account:update', 1, 0, 0, 1, NOW(), 1, NOW()),
('用户删除', 3, @user_menu_id, 3, '/system/account/delete', 'system:account:delete', 1, 0, 0, 1, NOW(), 1, NOW()),
('用户查询', 3, @user_menu_id, 4, '/system/account/query', 'system:account:query', 1, 0, 0, 1, NOW(), 1, NOW()),
('重置密码', 3, @user_menu_id, 5, '/system/account/reset-password', 'system:account:reset-password', 1, 0, 0, 1, NOW(), 1, NOW()),
('状态变更', 3, @user_menu_id, 6, '/system/account/update-status', 'system:account:update-status', 1, 0, 0, 1, NOW(), 1, NOW());

-- 1.4 角色管理功能点
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`,
  `api_perms`, `web_perms`,
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES
('角色新增', 3, @role_menu_id, 1, '/system/role/add', 'system:role:add', 1, 0, 0, 1, NOW(), 1, NOW()),
('角色更新', 3, @role_menu_id, 2, '/system/role/update', 'system:role:update', 1, 0, 0, 1, NOW(), 1, NOW()),
('角色删除', 3, @role_menu_id, 3, '/system/role/delete', 'system:role:delete', 1, 0, 0, 1, NOW(), 1, NOW()),
('角色查询', 3, @role_menu_id, 4, '/system/role/query', 'system:role:query', 1, 0, 0, 1, NOW(), 1, NOW()),
('权限分配', 3, @role_menu_id, 5, '/system/role/grant-menu', 'system:role:grant-menu', 1, 0, 0, 1, NOW(), 1, NOW());

-- =====================================================
-- 2. 企业OA模块菜单 (ioedream-oa-service)
-- =====================================================

-- 2.1 一级目录：企业OA
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`,
  `component`, `perms_type`, `web_perms`, `icon`,
  `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES (
  '企业OA', 1, 0, 2, '/business/oa', NULL, 1,
  'business:oa', 'BankOutlined', 0, 1, 0, 0,
  1, NOW(), 1, NOW()
);

SET @oa_menu_id = LAST_INSERT_ID();

-- 2.2 企业OA子菜单
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`,
  `component`, `web_perms`, `icon`, `cache_flag`,
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES
-- 企业管理
('企业管理', 2, @oa_menu_id, 1, '/business/oa/enterprise',
 '/business/oa/enterprise/enterprise-list.vue', 'business:oa:enterprise', 'BuildingOutlined', 1, 1, 0, 0, 1, NOW(), 1, NOW()),
-- 通知公告
('通知公告', 2, @oa_menu_id, 2, '/business/oa/notice',
 '/business/oa/notice/notice-list.vue', 'business:oa:notice', 'NotificationOutlined', 1, 1, 0, 0, 1, NOW(), 1, NOW()),
-- 工作流管理
('工作流管理', 2, @oa_menu_id, 3, '/business/oa/workflow',
 NULL, 'business:oa:workflow', 'BranchesOutlined', 0, 1, 0, 0, 1, NOW(), 1, NOW());

-- 获取OA子菜单ID
SELECT @enterprise_menu_id := menu_id FROM t_menu WHERE menu_name = '企业管理' LIMIT 1;
SELECT @notice_menu_id := menu_id FROM t_menu WHERE menu_name = '通知公告' LIMIT 1;
SELECT @workflow_menu_id := menu_id FROM t_menu WHERE menu_name = '工作流管理' LIMIT 1;

-- 2.3 工作流管理子菜单
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`,
  `component`, `web_perms`, `icon`, `cache_flag`,
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES
-- 流程定义
('流程定义', 2, @workflow_menu_id, 1, '/business/oa/workflow/definition',
 '/business/oa/workflow/definition/definition-list.vue', 'business:oa:workflow:definition', 'NodeIndexOutlined', 1, 1, 0, 0, 1, NOW(), 1, NOW()),
-- 流程实例
('流程实例', 2, @workflow_menu_id, 2, '/business/oa/workflow/instance',
 '/business/oa/workflow/instance/instance-list.vue', 'business:oa:workflow:instance', 'ApartmentOutlined', 1, 1, 0, 0, 1, NOW(), 1, NOW()),
-- 我的流程
('我的流程', 2, @workflow_menu_id, 3, '/business/oa/workflow/my-process',
 '/business/oa/workflow/instance/my-process-list.vue', 'business:oa:workflow:my-process', 'UserOutlined', 1, 1, 0, 0, 1, NOW(), 1, NOW()),
-- 待办任务
('待办任务', 2, @workflow_menu_id, 4, '/business/oa/workflow/pending-task',
 '/business/oa/workflow/task/pending-task-list.vue', 'business:oa:workflow:pending-task', 'ClockCircleOutlined', 1, 1, 0, 0, 1, NOW(), 1, NOW()),
-- 已办任务
('已办任务', 2, @workflow_menu_id, 5, '/business/oa/workflow/completed-task',
 '/business/oa/workflow/task/completed-task-list.vue', 'business:oa:workflow:completed-task', 'CheckCircleOutlined', 1, 1, 0, 0, 1, NOW(), 1, NOW()),
-- 流程监控
('流程监控', 2, @workflow_menu_id, 6, '/business/oa/workflow/monitor',
 '/business/oa/workflow/monitor/process-monitor.vue', 'business:oa:workflow:monitor', 'EyeOutlined', 1, 1, 0, 0, 1, NOW(), 1, NOW());

-- =====================================================
-- 3. 门禁管理模块菜单 (ioedream-access-service)
-- =====================================================

-- 3.1 一级目录：门禁管理
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`,
  `component`, `perms_type`, `web_perms`, `icon`,
  `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES (
  '门禁管理', 1, 0, 3, '/business/access', NULL, 1,
  'business:access', 'DoorEnterOutlined', 0, 1, 0, 0,
  1, NOW(), 1, NOW()
);

SET @access_menu_id = LAST_INSERT_ID();

-- 3.2 门禁管理子菜单
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`,
  `component`, `web_perms`, `icon`, `cache_flag`,
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES
-- 门禁概览
('门禁概览', 2, @access_menu_id, 1, '/business/access/dashboard',
 '/access/AccessDashboard.vue', 'business:access:dashboard', 'DashboardOutlined', 1, 1, 0, 0, 1, NOW(), 1, NOW()),
-- 设备管理
('设备管理', 2, @access_menu_id, 2, '/business/access/device',
 '/business/access/device/index.vue', 'business:access:device', 'MobileOutlined', 1, 1, 0, 0, 1, NOW(), 1, NOW()),
-- 通行记录
('通行记录', 2, @access_menu_id, 3, '/business/access/record',
 '/business/access/record/index.vue', 'business:access:record', 'HistoryOutlined', 0, 1, 0, 0, 1, NOW(), 1, NOW()),
-- 高级功能
('高级功能', 2, @access_menu_id, 4, '/business/access/advanced',
 NULL, 'business:access:advanced', 'SettingOutlined', 0, 1, 0, 0, 1, NOW(), 1, NOW());

-- 获取门禁子菜单ID
SELECT @access_device_menu_id := menu_id FROM t_menu WHERE menu_name = '设备管理' AND parent_id = @access_menu_id LIMIT 1;
SELECT @access_advanced_menu_id := menu_id FROM t_menu WHERE menu_name = '高级功能' AND parent_id = @access_menu_id LIMIT 1;

-- 3.3 高级功能子菜单
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`,
  `component`, `web_perms`, `icon`, `cache_flag`,
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES
-- 联动规则
('联动规则', 2, @access_advanced_menu_id, 1, '/business/access/advanced/linkage',
 '/business/access/advanced/GlobalLinkageManagement.vue', 'business:access:linkage', 'LinkOutlined', 1, 1, 0, 0, 1, NOW(), 1, NOW());

-- =====================================================
-- 4. 考勤管理模块菜单 (ioedream-attendance-service)
-- =====================================================

-- 4.1 一级目录：考勤管理
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`,
  `component`, `perms_type`, `web_perms`, `icon`,
  `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES (
  '考勤管理', 1, 0, 4, '/business/attendance', NULL, 1,
  'business:attendance', 'CalendarOutlined', 0, 1, 0, 0,
  1, NOW(), 1, NOW()
);

SET @attendance_menu_id = LAST_INSERT_ID();

-- 4.2 考勤管理子菜单
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`,
  `component`, `web_perms`, `icon`, `cache_flag`,
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES
-- 考勤记录
('考勤记录', 2, @attendance_menu_id, 1, '/business/attendance/record',
 '/business/attendance/record/index.vue', 'business:attendance:record', 'FileTextOutlined', 0, 1, 0, 0, 1, NOW(), 1, NOW()),
-- 班次管理
('班次管理', 2, @attendance_menu_id, 2, '/business/attendance/shift',
 '/business/attendance/shift/index.vue', 'business:attendance:shift', 'ScheduleOutlined', 1, 1, 0, 0, 1, NOW(), 1, NOW()),
-- 排班管理
('排班管理', 2, @attendance_menu_id, 3, '/business/attendance/schedule',
 '/business/attendance/schedule/index.vue', 'business:attendance:schedule', 'TableOutlined', 1, 1, 0, 0, 1, NOW(), 1, NOW()),
-- 考勤统计
('考勤统计', 2, @attendance_menu_id, 4, '/business/attendance/statistics',
 '/business/attendance/statistics/index.vue', 'business:attendance:statistics', 'BarChartOutlined', 1, 1, 0, 0, 1, NOW(), 1, NOW()),
-- 请假管理
('请假管理', 2, @attendance_menu_id, 5, '/business/attendance/leave',
 '/business/attendance/leave/index.vue', 'business:attendance:leave', 'FileProtectOutlined', 1, 1, 0, 0, 1, NOW(), 1, NOW());

-- =====================================================
-- 5. 消费管理模块菜单 (ioedream-consume-service)
-- =====================================================

-- 5.1 一级目录：消费管理
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`,
  `component`, `perms_type`, `web_perms`, `icon`,
  `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES (
  '消费管理', 1, 0, 5, '/business/consumption', NULL, 1,
  'business:consumption', 'ShoppingCartOutlined', 0, 1, 0, 0,
  1, NOW(), 1, NOW()
);

SET @consumption_menu_id = LAST_INSERT_ID();

-- 5.2 消费管理子菜单
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`,
  `component`, `web_perms`, `icon`, `cache_flag`,
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES
-- 数据总览
('数据总览', 2, @consumption_menu_id, 1, '/business/consumption/dashboard',
 '/business/consumption/dashboard/index.vue', 'business:consumption:dashboard', 'DashboardOutlined', 1, 1, 0, 0, 1, NOW(), 1, NOW()),
-- 账户管理
('账户管理', 2, @consumption_menu_id, 2, '/business/consumption/account',
 '/business/consume/account/index.vue', 'business:consumption:account', 'CreditCardOutlined', 1, 1, 0, 0, 1, NOW(), 1, NOW()),
-- 交易记录
('交易记录', 2, @consumption_menu_id, 3, '/business/consumption/transaction',
 '/business/consume/transaction/index.vue', 'business:consumption:transaction', 'SwapOutlined', 0, 1, 0, 0, 1, NOW(), 1, NOW()),
-- 消费报表
('消费报表', 2, @consumption_menu_id, 4, '/business/consumption/report',
 '/business/consume/report/index.vue', 'business:consumption:report', 'BarChartOutlined', 1, 1, 0, 0, 1, NOW(), 1, NOW()),
-- 区域管理
('区域管理', 2, @consumption_menu_id, 5, '/business/consumption/region',
 '/business/consumption/region/index.vue', 'business:consumption:region', 'GlobalOutlined', 1, 1, 0, 0, 1, NOW(), 1, NOW()),
-- 商品管理
('商品管理', 2, @consumption_menu_id, 6, '/business/consumption/goods',
 '/business/erp/goods/goods-list.vue', 'business:consumption:goods', 'ShopOutlined', 1, 1, 0, 0, 1, NOW(), 1, NOW()),
-- 商品分类
('商品分类', 2, @consumption_menu_id, 7, '/business/consumption/catalog',
 '/business/erp/catalog/goods-catalog.vue', 'business:consumption:catalog', 'AppstoreOutlined', 1, 1, 0, 0, 1, NOW(), 1, NOW());

-- 获取消费管理子菜单ID用于功能点
SELECT @consume_account_menu_id := menu_id FROM t_menu WHERE menu_name = '账户管理' AND parent_id = @consumption_menu_id LIMIT 1;

-- 5.3 账户管理功能点
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`,
  `api_perms`, `web_perms`,
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES
('账户新增', 3, @consume_account_menu_id, 1, '/consumption/account/add', 'business:consumption:account:add', 1, 0, 0, 1, NOW(), 1, NOW()),
('账户充值', 3, @consume_account_menu_id, 2, '/consumption/account/recharge', 'business:consumption:account:recharge', 1, 0, 0, 1, NOW(), 1, NOW()),
('账户退款', 3, @consume_account_menu_id, 3, '/consumption/account/refund', 'business:consumption:account:refund', 1, 0, 0, 1, NOW(), 1, NOW()),
('余额调整', 3, @consume_account_menu_id, 4, '/consumption/account/adjust-balance', 'business:consumption:account:adjust-balance', 1, 0, 0, 1, NOW(), 1, NOW()),
('账户冻结', 3, @consume_account_menu_id, 5, '/consumption/account/freeze', 'business:consumption:account:freeze', 1, 0, 0, 1, NOW(), 1, NOW()),
('账户解冻', 3, @consume_account_menu_id, 6, '/consumption/account/unfreeze', 'business:consumption:account:unfreeze', 1, 0, 0, 1, NOW(), 1, NOW());

-- =====================================================
-- 6. 访客管理模块菜单 (ioedream-visitor-service)
-- =====================================================

-- 6.1 一级目录：访客管理
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`,
  `component`, `perms_type`, `web_perms`, `icon`,
  `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES (
  '访客管理', 1, 0, 6, '/business/visitor', NULL, 1,
  'business:visitor', 'TeamOutlined', 0, 1, 0, 0,
  1, NOW(), 1, NOW()
);

SET @visitor_menu_id = LAST_INSERT_ID();

-- 6.2 访客管理子菜单
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`,
  `component`, `web_perms`, `icon`, `cache_flag`,
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES
-- 访客预约
('访客预约', 2, @visitor_menu_id, 1, '/business/visitor/appointment',
 '/business/visitor/appointment.vue', 'business:visitor:appointment', 'CalendarCheckOutlined', 1, 1, 0, 0, 1, NOW(), 1, NOW()),
-- 访客登记
('访客登记', 2, @visitor_menu_id, 2, '/business/visitor/registration',
 '/business/visitor/registration.vue', 'business:visitor:registration', 'EditOutlined', 1, 1, 0, 0, 1, NOW(), 1, NOW()),
-- 访客记录
('访客记录', 2, @visitor_menu_id, 3, '/business/visitor/record',
 '/business/visitor/record.vue', 'business:visitor:record', 'HistoryOutlined', 0, 1, 0, 0, 1, NOW(), 1, NOW()),
-- 访客统计
('访客统计', 2, @visitor_menu_id, 4, '/business/visitor/statistics',
 '/business/visitor/statistics.vue', 'business:visitor:statistics', 'BarChartOutlined', 1, 1, 0, 0, 1, NOW(), 1, NOW()),
-- 访客验证
('访客验证', 2, @visitor_menu_id, 5, '/business/visitor/verification',
 '/business/visitor/verification.vue', 'business:visitor:verification', 'SafetyCheckOutlined', 1, 1, 0, 0, 1, NOW(), 1, NOW()),
-- 黑名单管理
('黑名单管理', 2, @visitor_menu_id, 6, '/business/visitor/blacklist',
 '/business/visitor/blacklist.vue', 'business:visitor:blacklist', 'StopOutlined', 1, 1, 0, 0, 1, NOW(), 1, NOW());

-- =====================================================
-- 7. 智能视频模块菜单 (ioedream-video-service)
-- =====================================================

-- 7.1 一级目录：智能视频
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`,
  `component`, `perms_type`, `web_perms`, `icon`,
  `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES (
  '智能视频', 1, 0, 7, '/business/smart-video', NULL, 1,
  'business:video', 'VideoCameraOutlined', 0, 1, 0, 0,
  1, NOW(), 1, NOW()
);

SET @video_menu_id = LAST_INSERT_ID();

-- 7.2 智能视频子菜单
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`,
  `component`, `web_perms`, `icon`, `cache_flag`,
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES
-- 系统概览
('系统概览', 2, @video_menu_id, 1, '/business/smart-video/overview',
 '/business/smart-video/system-overview.vue', 'business:video:overview', 'DashboardOutlined', 1, 1, 0, 0, 1, NOW(), 1, NOW()),
-- 设备列表
('设备列表', 2, @video_menu_id, 2, '/business/smart-video/device',
 '/business/smart-video/device-list.vue', 'business:video:device', 'MobileOutlined', 0, 1, 0, 0, 1, NOW(), 1, NOW()),
-- 实时监控
('实时监控', 2, @video_menu_id, 3, '/business/smart-video/monitor',
 '/business/smart-video/monitor-preview.vue', 'business:video:monitor', 'EyeOutlined', 1, 1, 0, 0, 1, NOW(), 1, NOW()),
-- 视频回放
('视频回放', 2, @video_menu_id, 4, '/business/smart-video/playback',
 '/business/smart-video/video-playback.vue', 'business:video:playback', 'ReplayOutlined', 0, 1, 0, 0, 1, NOW(), 1, NOW()),
-- 解码器管理
('解码器管理', 2, @video_menu_id, 5, '/business/smart-video/decoder',
 '/business/smart-video/decoder-management.vue', 'business:video:decoder', 'SettingOutlined', 1, 1, 0, 0, 1, NOW(), 1, NOW()),
-- 电视墙
('电视墙', 2, @video_menu_id, 6, '/business/smart-video/tv-wall',
 '/business/smart-video/tv-wall.vue', 'business:video:tv-wall', 'DesktopOutlined', 1, 1, 0, 0, 1, NOW(), 1, NOW()),
-- 智能分析
('智能分析', 2, @video_menu_id, 7, '/business/smart-video/analysis',
 NULL, 'business:video:analysis', 'ExperimentOutlined', 0, 1, 0, 0, 1, NOW(), 1, NOW());

-- 获取智能视频子菜单ID
SELECT @video_analysis_menu_id := menu_id FROM t_menu WHERE menu_name = '智能分析' AND parent_id = @video_menu_id LIMIT 1;

-- 7.3 智能分析子菜单
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`,
  `component`, `web_perms`, `icon`, `cache_flag`,
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES
-- 行为分析
('行为分析', 2, @video_analysis_menu_id, 1, '/business/smart-video/behavior',
 '/business/smart-video/behavior-analysis.vue', 'business:video:behavior', 'UsergroupDeleteOutlined', 1, 1, 0, 0, 1, NOW(), 1, NOW()),
-- 人流量统计
('人流量统计', 2, @video_analysis_menu_id, 2, '/business/smart-video/crowd',
 '/business/smart-video/crowd-situation.vue', 'business:video:crowd', 'TeamOutlined', 1, 1, 0, 0, 1, NOW(), 1, NOW()),
-- 热力图
('热力图', 2, @video_analysis_menu_id, 3, '/business/smart-video/heatmap',
 '/business/smart-video/heatmap.vue', 'business:video:heatmap', 'HeatMapOutlined', 1, 1, 0, 0, 1, NOW(), 1, NOW()),
-- 目标检索
('目标检索', 2, @video_analysis_menu_id, 4, '/business/smart-video/search',
 '/business/smart-video/target-search.vue', 'business:video:search', 'SearchOutlined', 1, 1, 0, 0, 1, NOW(), 1, NOW()),
-- 目标智能
('目标智能', 2, @video_analysis_menu_id, 5, '/business/smart-video/intelligence',
 '/business/smart-video/target-intelligence.vue', 'business:video:intelligence', 'RobotOutlined', 1, 1, 0, 0, 1, NOW(), 1, NOW()),
-- 联动记录
('联动记录', 2, @video_analysis_menu_id, 6, '/business/smart-video/linkage',
 '/business/smart-video/linkage-records.vue', 'business:video:linkage', 'LinkOutlined', 0, 1, 0, 0, 1, NOW(), 1, NOW()),
-- 报警记录
('报警记录', 2, @video_analysis_menu_id, 7, '/business/smart-video/alarm',
 '/business/smart-video/history-alarm.vue', 'business:video:alarm', 'AlertOutlined', 0, 1, 0, 0, 1, NOW(), 1, NOW());

-- =====================================================
-- 8. 设备通讯管理 (ioedream-device-comm-service)
-- =====================================================

-- 8.1 一级目录：设备通讯
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`,
  `component`, `perms_type`, `web_perms`, `icon`,
  `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES (
  '设备通讯', 1, 0, 8, '/infrastructure/device-comm', NULL, 1,
  'infrastructure:device-comm', 'ApiOutlined', 0, 1, 0, 0,
  1, NOW(), 1, NOW()
);

SET @device_comm_menu_id = LAST_INSERT_ID();

-- 8.2 设备通讯子菜单
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`,
  `component`, `web_perms`, `icon`, `cache_flag`,
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES
-- 连接管理
('连接管理', 2, @device_comm_menu_id, 1, '/infrastructure/device-comm/connections',
 '/infrastructure/device-comm/connections.vue', 'infrastructure:device-comm:connections', 'LinkOutlined', 1, 1, 0, 0, 1, NOW(), 1, NOW()),
-- 协议管理
('协议管理', 2, @device_comm_menu_id, 2, '/infrastructure/device-comm/protocols',
 '/infrastructure/device-comm/protocols.vue', 'infrastructure:device-comm:protocols', 'FileTextOutlined', 1, 1, 0, 0, 1, NOW(), 1, NOW()),
-- 通讯监控
('通讯监控', 2, @device_comm_menu_id, 3, '/infrastructure/device-comm/monitor',
 '/infrastructure/device-comm/monitor.vue', 'infrastructure:device-comm:monitor', 'EyeOutlined', 0, 1, 0, 0, 1, NOW(), 1, NOW());

-- =====================================================
-- 9. 监控运维模块菜单 (基于common-service)
-- =====================================================

-- 9.1 一级目录：监控运维
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`,
  `component`, `perms_type`, `web_perms`, `icon`,
  `cache_flag`, `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES (
  '监控运维', 1, 0, 9, '/infrastructure/monitoring', NULL, 1,
  'infrastructure:monitoring', 'MonitorOutlined', 0, 1, 0, 0,
  1, NOW(), 1, NOW()
);

SET @monitoring_menu_id = LAST_INSERT_ID();

-- 9.2 监控运维子菜单
INSERT INTO `t_menu` (
  `menu_name`, `menu_type`, `parent_id`, `sort`, `path`,
  `component`, `web_perms`, `icon`, `cache_flag`,
  `visible_flag`, `disabled_flag`, `deleted_flag`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES
-- 服务监控
('服务监控', 2, @monitoring_menu_id, 1, '/infrastructure/monitoring/services',
 '/infrastructure/monitoring/services.vue', 'infrastructure:monitoring:services', 'ClusterOutlined', 1, 1, 0, 0, 1, NOW(), 1, NOW()),
-- 性能监控
('性能监控', 2, @monitoring_menu_id, 2, '/infrastructure/monitoring/performance',
 '/infrastructure/monitoring/performance.vue', 'infrastructure:monitoring:performance', 'LineChartOutlined', 1, 1, 0, 0, 1, NOW(), 1, NOW()),
-- 告警管理
('告警管理', 2, @monitoring_menu_id, 3, '/infrastructure/monitoring/alerts',
 '/infrastructure/monitoring/alerts.vue', 'infrastructure:monitoring:alerts', 'AlertOutlined', 1, 1, 0, 0, 1, NOW(), 1, NOW()),
-- 日志分析
('日志分析', 2, @monitoring_menu_id, 4, '/infrastructure/monitoring/logs',
 '/infrastructure/monitoring/logs.vue', 'infrastructure:monitoring:logs', 'FileTextOutlined', 0, 1, 0, 0, 1, NOW(), 1, NOW());

-- =====================================================
-- 10. 创建超级管理员角色并分配所有菜单权限
-- =====================================================

-- 10.1 创建超级管理员角色
INSERT INTO `t_role` (
  `role_id`, `role_name`, `role_code`, `sort`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES
(1, '超级管理员', 'SUPER_ADMIN', 1, 1, NOW(), 1, NOW()),
(2, '系统管理员', 'SYSTEM_ADMIN', 2, 1, NOW(), 1, NOW()),
(3, '业务管理员', 'BUSINESS_ADMIN', 3, 1, NOW(), 1, NOW()),
(4, '普通用户', 'USER', 4, 1, NOW(), 1, NOW());

-- 10.2 为超级管理员角色分配所有菜单权限
INSERT INTO `t_role_menu` (role_id, menu_id)
SELECT 1, menu_id FROM t_menu WHERE deleted_flag = 0;

-- 10.3 为系统管理员分配系统管理相关权限
INSERT INTO `t_role_menu` (role_id, menu_id)
SELECT 2, menu_id FROM t_menu
WHERE deleted_flag = 0
AND (
  parent_id = @system_menu_id
  OR menu_id IN (SELECT menu_id FROM t_menu WHERE parent_id = @system_menu_id)
  OR web_perms LIKE 'system:%'
);

-- =====================================================
-- 初始化完成统计
-- =====================================================

-- 输出初始化统计信息
SELECT
  '菜单初始化完成' AS message,
  COUNT(*) AS total_menus,
  SUM(CASE WHEN menu_type = 1 THEN 1 ELSE 0 END) AS directories,
  SUM(CASE WHEN menu_type = 2 THEN 1 ELSE 0 END) AS menus,
  SUM(CASE WHEN menu_type = 3 THEN 1 ELSE 0 END) AS functions
FROM t_menu
WHERE deleted_flag = 0;

SELECT
  '角色菜单权限分配完成' AS message,
  r.role_name,
  COUNT(rm.menu_id) AS menu_count
FROM t_role r
LEFT JOIN t_role_menu rm ON r.role_id = rm.role_id
GROUP BY r.role_id, r.role_name
ORDER BY r.role_id;

-- =====================================================
-- 验证脚本执行结果
-- =====================================================

-- 验证核心模块菜单是否存在
SELECT
  module_name,
  CASE
    WHEN EXISTS(SELECT 1 FROM t_menu WHERE menu_name = module_name AND deleted_flag = 0)
    THEN '✓ 已创建'
    ELSE '✗ 未创建'
  END AS status
FROM (
  SELECT '系统管理' AS module_name
  UNION SELECT '企业OA'
  UNION SELECT '门禁管理'
  UNION SELECT '考勤管理'
  UNION SELECT '消费管理'
  UNION SELECT '访客管理'
  UNION SELECT '智能视频'
  UNION SELECT '设备通讯'
  UNION SELECT '监控运维'
) AS modules;

-- 验证每个模块的子菜单数量
SELECT
  parent.menu_name AS module_name,
  COUNT(child.menu_id) AS submenu_count,
  GROUP_CONCAT(child.menu_name ORDER BY child.sort) AS submenus
FROM t_menu parent
LEFT JOIN t_menu child ON child.parent_id = parent.menu_id AND child.deleted_flag = 0
WHERE parent.parent_id = 0 AND parent.deleted_flag = 0
GROUP BY parent.menu_id, parent.menu_name
ORDER BY parent.sort;