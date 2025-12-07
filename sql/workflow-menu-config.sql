-- ============================================
-- OA工作流模块菜单配置SQL脚本
-- 版本: v3.0.0
-- 日期: 2025-01-30
-- 说明: 用于初始化工作流模块的菜单和权限配置
-- ============================================

-- 注意：执行前请确认表结构是否存在，以及parent_id的值是否正确

-- 1. 工作流主菜单（目录）
INSERT INTO t_menu (
    menu_name, menu_type, parent_id, path, component, icon, cache_flag, 
    visible_flag, disabled_flag, sort, web_perms, api_perms, create_time, update_time
) VALUES (
    'OA工作流', 1, 0, '/oa/workflow', NULL, 'WorkflowOutlined', 0, 1, 0, 100, 
    'oa:workflow:view', NULL, NOW(), NOW()
);

SET @workflow_menu_id = LAST_INSERT_ID();

-- 2. 待办任务列表菜单
INSERT INTO t_menu (
    menu_name, menu_type, parent_id, path, component, icon, cache_flag, 
    visible_flag, disabled_flag, sort, web_perms, api_perms, create_time, update_time
) VALUES (
    '待办任务', 2, @workflow_menu_id, '/oa/workflow/task/pending-task-list', 
    '/business/oa/workflow/task/pending-task-list.vue', 'ClockCircleOutlined', 1, 1, 0, 1,
    'oa:workflow:task:query', '/api/v1/workflow/engine/task/my/pending', NOW(), NOW()
);

SET @pending_task_menu_id = LAST_INSERT_ID();

-- 3. 已办任务列表菜单
INSERT INTO t_menu (
    menu_name, menu_type, parent_id, path, component, icon, cache_flag, 
    visible_flag, disabled_flag, sort, web_perms, api_perms, create_time, update_time
) VALUES (
    '已办任务', 2, @workflow_menu_id, '/oa/workflow/task/completed-task-list', 
    '/business/oa/workflow/task/completed-task-list.vue', 'CheckCircleOutlined', 1, 1, 0, 2,
    'oa:workflow:task:completed:query', '/api/v1/workflow/engine/task/my/completed', NOW(), NOW()
);

-- 4. 流程实例列表菜单
INSERT INTO t_menu (
    menu_name, menu_type, parent_id, path, component, icon, cache_flag, 
    visible_flag, disabled_flag, sort, web_perms, api_perms, create_time, update_time
) VALUES (
    '流程实例', 2, @workflow_menu_id, '/oa/workflow/instance/instance-list', 
    '/business/oa/workflow/instance/instance-list.vue', 'DeploymentUnitOutlined', 1, 1, 0, 3,
    'oa:workflow:instance:query', '/api/v1/workflow/engine/instance/page', NOW(), NOW()
);

SET @instance_menu_id = LAST_INSERT_ID();

-- 5. 我发起的流程菜单
INSERT INTO t_menu (
    menu_name, menu_type, parent_id, path, component, icon, cache_flag, 
    visible_flag, disabled_flag, sort, web_perms, api_perms, create_time, update_time
) VALUES (
    '我发起的流程', 2, @workflow_menu_id, '/oa/workflow/instance/my-process-list', 
    '/business/oa/workflow/instance/my-process-list.vue', 'FileTextOutlined', 1, 1, 0, 4,
    'oa:workflow:instance:query', '/api/v1/workflow/engine/instance/my', NOW(), NOW()
);

SET @my_process_menu_id = LAST_INSERT_ID();

-- 6. 流程定义管理菜单
INSERT INTO t_menu (
    menu_name, menu_type, parent_id, path, component, icon, cache_flag, 
    visible_flag, disabled_flag, sort, web_perms, api_perms, create_time, update_time
) VALUES (
    '流程定义', 2, @workflow_menu_id, '/oa/workflow/definition/definition-list', 
    '/business/oa/workflow/definition/definition-list.vue', 'ApartmentOutlined', 1, 1, 0, 5,
    'oa:workflow:definition:query', '/api/v1/workflow/engine/definition/page', NOW(), NOW()
);

SET @definition_menu_id = LAST_INSERT_ID();

-- 7. 流程监控菜单
INSERT INTO t_menu (
    menu_name, menu_type, parent_id, path, component, icon, cache_flag, 
    visible_flag, disabled_flag, sort, web_perms, api_perms, create_time, update_time
) VALUES (
    '流程监控', 2, @workflow_menu_id, '/oa/workflow/monitor/process-monitor', 
    '/business/oa/workflow/monitor/process-monitor.vue', 'DashboardOutlined', 1, 1, 0, 6,
    'oa:workflow:monitor:query', '/api/v1/workflow/engine/statistics', NOW(), NOW()
);

-- 8. 功能权限点配置（功能点菜单）
-- 待办任务功能权限点
INSERT INTO t_menu (
    menu_name, menu_type, parent_id, path, component, icon, cache_flag, 
    visible_flag, disabled_flag, sort, web_perms, api_perms, create_time, update_time
) VALUES 
('任务受理', 3, @pending_task_menu_id, NULL, NULL, NULL, 0, 0, 0, 1, 'oa:workflow:task:claim', '/api/v1/workflow/engine/task/{taskId}/claim', NOW(), NOW()),
('任务转办', 3, @pending_task_menu_id, NULL, NULL, NULL, 0, 0, 0, 2, 'oa:workflow:task:transfer', '/api/v1/workflow/engine/task/{taskId}/transfer', NOW(), NOW()),
('任务委派', 3, @pending_task_menu_id, NULL, NULL, NULL, 0, 0, 0, 3, 'oa:workflow:task:delegate', '/api/v1/workflow/engine/task/{taskId}/delegate', NOW(), NOW()),
('批量受理', 3, @pending_task_menu_id, NULL, NULL, NULL, 0, 0, 0, 4, 'oa:workflow:task:batch:claim', '/api/v1/workflow/engine/task/batch/claim', NOW(), NOW()),
('批量转办', 3, @pending_task_menu_id, NULL, NULL, NULL, 0, 0, 0, 5, 'oa:workflow:task:batch:transfer', '/api/v1/workflow/engine/task/batch/transfer', NOW(), NOW());

-- 流程定义功能权限点
INSERT INTO t_menu (
    menu_name, menu_type, parent_id, path, component, icon, cache_flag, 
    visible_flag, disabled_flag, sort, web_perms, api_perms, create_time, update_time
) VALUES 
('流程部署', 3, @definition_menu_id, NULL, NULL, NULL, 0, 0, 0, 1, 'oa:workflow:definition:deploy', '/api/v1/workflow/engine/definition/deploy', NOW(), NOW()),
('流程激活', 3, @definition_menu_id, NULL, NULL, NULL, 0, 0, 0, 2, 'oa:workflow:definition:activate', '/api/v1/workflow/engine/definition/{definitionId}/activate', NOW(), NOW()),
('流程禁用', 3, @definition_menu_id, NULL, NULL, NULL, 0, 0, 0, 3, 'oa:workflow:definition:disable', '/api/v1/workflow/engine/definition/{definitionId}/disable', NOW(), NOW()),
('流程删除', 3, @definition_menu_id, NULL, NULL, NULL, 0, 0, 0, 4, 'oa:workflow:definition:delete', '/api/v1/workflow/engine/definition/{definitionId}/delete', NOW(), NOW());

-- 流程实例功能权限点
INSERT INTO t_menu (
    menu_name, menu_type, parent_id, path, component, icon, cache_flag, 
    visible_flag, disabled_flag, sort, web_perms, api_perms, create_time, update_time
) VALUES 
('流程挂起', 3, @instance_menu_id, NULL, NULL, NULL, 0, 0, 0, 1, 'oa:workflow:instance:suspend', '/api/v1/workflow/engine/instance/{instanceId}/suspend', NOW(), NOW()),
('流程激活', 3, @instance_menu_id, NULL, NULL, NULL, 0, 0, 0, 2, 'oa:workflow:instance:activate', '/api/v1/workflow/engine/instance/{instanceId}/activate', NOW(), NOW()),
('流程终止', 3, @instance_menu_id, NULL, NULL, NULL, 0, 0, 0, 3, 'oa:workflow:instance:terminate', '/api/v1/workflow/engine/instance/{instanceId}/terminate', NOW(), NOW());

-- 我发起的流程功能权限点
INSERT INTO t_menu (
    menu_name, menu_type, parent_id, path, component, icon, cache_flag, 
    visible_flag, disabled_flag, sort, web_perms, api_perms, create_time, update_time
) VALUES 
('流程撤销', 3, @my_process_menu_id, NULL, NULL, NULL, 0, 0, 0, 1, 'oa:workflow:instance:revoke', '/api/v1/workflow/engine/instance/{instanceId}/revoke', NOW(), NOW());

-- 查询插入结果（验证）
SELECT 
    menu_id,
    menu_name,
    menu_type,
    parent_id,
    path,
    component,
    web_perms,
    api_perms
FROM t_menu
WHERE menu_name LIKE '%工作流%' OR parent_id = @workflow_menu_id
ORDER BY menu_type, sort;

-- ============================================
-- 说明：
-- 1. menu_type: 1=目录，2=菜单，3=功能点
-- 2. parent_id: 父级菜单ID，0表示顶级菜单
-- 3. visible_flag: 1=显示，0=隐藏
-- 4. disabled_flag: 0=启用，1=禁用
-- 5. cache_flag: 0=不缓存，1=缓存
-- 6. 执行后需要给相应的角色分配这些菜单权限
-- ============================================

