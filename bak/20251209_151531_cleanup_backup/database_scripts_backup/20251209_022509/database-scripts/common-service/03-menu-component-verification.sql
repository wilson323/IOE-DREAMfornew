-- =====================================================
-- IOE-DREAM 前端组件路径验证与映射脚本
-- 版本: v1.0.0 前后端路由完整匹配版
-- 创建日期: 2025-01-08
-- 说明:
-- - 验证所有菜单的前端组件路径是否存在
-- - 生成前后端路由映射表
-- - 确保页面能正确加载和渲染
-- =====================================================

-- =====================================================
-- 1. 前端组件存在性检查（基于实际文件结构）
-- =====================================================

-- 创建临时表存储前端组件验证结果
CREATE TEMPORARY TABLE IF NOT EXISTS temp_component_check (
  menu_id BIGINT,
  menu_name VARCHAR(200),
  component_path VARCHAR(500),
  file_exists BOOLEAN DEFAULT FALSE,
  check_status VARCHAR(20) DEFAULT 'UNKNOWN',
  check_remark TEXT
);

-- 插入所有需要验证的菜单（菜单类型为2且有组件路径的）
INSERT INTO temp_component_check (menu_id, menu_name, component_path)
SELECT
  menu_id,
  menu_name,
  component
FROM t_menu
WHERE menu_type = 2
  AND component IS NOT NULL
  AND component != ''
  AND deleted_flag = 0;

-- 手动更新组件存在状态（基于实际存在的文件）
-- 注意：这里使用手动更新，因为无法直接通过SQL检查文件系统存在性

-- 系统管理模块组件（确认存在）
UPDATE temp_component_check
SET file_exists = TRUE, check_status = 'EXISTS', check_remark = '文件存在于 smart-admin-web-javascript/src/views/'
WHERE component_path IN (
  '/system/account/index.vue',
  '/system/role/index.vue',
  '/system/menu/index.vue',
  '/system/department/index.vue',
  '/system/dict/index.vue',
  '/system/operate-log/index.vue',
  '/system/login-log/index.vue'
);

-- 企业OA模块组件（确认存在）
UPDATE temp_component_check
SET file_exists = TRUE, check_status = 'EXISTS', check_remark = '文件存在于 smart-admin-web-javascript/src/views/business/oa/'
WHERE component_path IN (
  '/business/oa/enterprise/enterprise-list.vue',
  '/business/oa/notice/notice-list.vue',
  '/business/oa/workflow/definition/definition-list.vue',
  '/business/oa/workflow/instance/instance-list.vue',
  '/business/oa/workflow/instance/my-process-list.vue',
  '/business/oa/workflow/task/pending-task-list.vue',
  '/business/oa/workflow/task/completed-task-list.vue',
  '/business/oa/workflow/monitor/process-monitor.vue'
);

-- 门禁管理模块组件（确认存在）
UPDATE temp_component_check
SET file_exists = TRUE, check_status = 'EXISTS', check_remark = '文件存在于 smart-admin-web-javascript/src/views/'
WHERE component_path IN (
  '/access/AccessDashboard.vue',
  '/business/access/device/index.vue',
  '/business/access/record/index.vue',
  '/business/access/advanced/GlobalLinkageManagement.vue'
);

-- 考勤管理模块组件（部分存在，部分需要创建）
UPDATE temp_component_check
SET file_exists = TRUE, check_status = 'EXISTS', check_remark = '文件存在于 smart-admin-web-javascript/src/views/business/attendance/'
WHERE component_path = '/business/attendance/record/index.vue';

-- 标记需要创建的组件
UPDATE temp_component_check
SET file_exists = FALSE, check_status = 'TO_CREATE', check_remark = '需要创建前端组件文件'
WHERE file_exists = FALSE AND check_status = 'UNKNOWN';

-- 消费管理模块组件（确认存在）
UPDATE temp_component_check
SET file_exists = TRUE, check_status = 'EXISTS', check_remark = '文件存在于 smart-admin-web-javascript/src/views/'
WHERE component_path IN (
  '/business/consumption/dashboard/index.vue',
  '/business/consume/account/index.vue',
  '/business/consume/transaction/index.vue',
  '/business/consume/report/index.vue',
  '/business/consumption/region/index.vue',
  '/business/erp/goods/goods-list.vue',
  '/business/erp/catalog/goods-catalog.vue'
);

-- 访客管理模块组件（确认存在）
UPDATE temp_component_check
SET file_exists = TRUE, check_status = 'EXISTS', check_remark = '文件存在于 smart-admin-web-javascript/src/views/business/visitor/'
WHERE component_path IN (
  '/business/visitor/appointment.vue',
  '/business/visitor/registration.vue',
  '/business/visitor/record.vue',
  '/business/visitor/statistics.vue',
  '/business/visitor/verification.vue',
  '/business/visitor/blacklist.vue'
);

-- 智能视频模块组件（确认存在）
UPDATE temp_component_check
SET file_exists = TRUE, check_status = 'EXISTS', check_remark = '文件存在于 smart-admin-web-javascript/src/views/business/smart-video/'
WHERE component_path IN (
  '/business/smart-video/system-overview.vue',
  '/business/smart-video/device-list.vue',
  '/business/smart-video/monitor-preview.vue',
  '/business/smart-video/video-playback.vue',
  '/business/smart-video/decoder-management.vue',
  '/business/smart-video/tv-wall.vue',
  '/business/smart-video/behavior-analysis.vue',
  '/business/smart-video/crowd-situation.vue',
  '/business/smart-video/heatmap.vue',
  '/business/smart-video/target-search.vue',
  '/business/smart-video/target-intelligence.vue',
  '/business/smart-video/linkage-records.vue',
  '/business/smart-video/history-alarm.vue'
);

-- =====================================================
-- 2. 组件验证结果统计
-- =====================================================

SELECT
  '前端组件验证结果统计' AS report_title,
  COUNT(*) AS total_menus_with_components,
  SUM(CASE WHEN file_exists = TRUE THEN 1 ELSE 0 END) AS existing_components,
  SUM(CASE WHEN file_exists = FALSE THEN 1 ELSE 0 END) AS missing_components,
  CONCAT(ROUND(SUM(CASE WHEN file_exists = TRUE THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2), '%') AS completion_rate
FROM temp_component_check;

-- 需要创建的组件清单
SELECT
  '需要创建的前端组件清单' AS checklist_title,
  menu_name,
  component_path,
  check_remark
FROM temp_component_check
WHERE file_exists = FALSE
ORDER BY menu_name;

-- =====================================================
-- 3. 生成前后端路由映射表
-- =====================================================

-- 创建前后端路由映射表
CREATE TEMPORARY TABLE IF NOT EXISTS temp_route_mapping (
  menu_id BIGINT,
  menu_name VARCHAR(200),
  route_path VARCHAR(500),
  component_path VARCHAR(500),
  route_name VARCHAR(100),
  vue_component_name VARCHAR(100),
  menu_level INT,
  parent_menu_name VARCHAR(200)
);

-- 生成路由映射数据
INSERT INTO temp_route_mapping
SELECT
  m.menu_id,
  m.menu_name,
  m.path AS route_path,
  m.component AS component_path,
  CAST(m.menu_id AS CHAR) AS route_name,
  CASE
    WHEN m.component LIKE '%/index.vue' THEN
      CONCAT(SUBSTRING_INDEX(SUBSTRING_INDEX(m.component, '/', -2), '/', 1), 'Index')
    WHEN m.component LIKE '%/list.vue' THEN
      CONCAT(SUBSTRING_INDEX(SUBSTRING_INDEX(m.component, '/', -2), '/', 1), 'List')
    WHEN m.component LIKE '%/%.vue' THEN
      REPLACE(SUBSTRING_INDEX(m.component, '/', -1), '.vue', '')
    ELSE 'UnknownComponent'
  END AS vue_component_name,
  (CASE
    WHEN m.parent_id = 0 THEN 1
    WHEN EXISTS (SELECT 1 FROM t_menu p WHERE p.menu_id = m.parent_id AND p.parent_id = 0) THEN 2
    ELSE 3
  END) AS menu_level,
  CASE
    WHEN m.parent_id = 0 THEN NULL
    ELSE (SELECT p.menu_name FROM t_menu p WHERE p.menu_id = m.parent_id)
  END AS parent_menu_name
FROM t_menu m
WHERE m.menu_type = 2
  AND m.component IS NOT NULL
  AND m.component != ''
  AND m.deleted_flag = 0
ORDER BY m.sort;

-- 显示路由映射表
SELECT
  '前后端路由映射表' AS mapping_title,
  menu_name AS '菜单名称',
  route_path AS '路由路径',
  component_path AS '组件路径',
  route_name AS '路由名称(name)',
  vue_component_name AS 'Vue组件名称',
  menu_level AS '菜单级别',
  parent_menu_name AS '父级菜单'
FROM temp_route_mapping
ORDER BY menu_level, menu_name;

-- =====================================================
-- 4. 生成路由配置代码片段
-- =====================================================

SELECT
  'Vue Router 配置代码片段' AS code_section,
  '--- 请复制以下代码到前端路由配置文件中 ---' AS instruction;

-- 生成Vue路由配置代码
SELECT
  CONCAT(
    '  {',
    CHAR(10),
    '    path: ''', route_path, ''',',
    CHAR(10),
    '    name: ''', route_name, ''',',
    CHAR(10),
    '    component: () => import(''@/views', component_path, '''),',
    CHAR(10),
    '    meta: {',
    CHAR(10),
    '      id: ''', route_name, ''',',
    CHAR(10),
    '      title: ''', menu_name, ''',',
    CHAR(10),
    '      keepAlive: true',
    CHAR(10),
    '    }',
    CHAR(10),
    '  },'
  ) AS vue_router_config
FROM temp_route_mapping
WHERE file_exists = TRUE
ORDER BY menu_level, menu_name;

-- =====================================================
-- 5. 检查路由冲突
-- =====================================================

-- 检查路径重复
SELECT
  '路由路径冲突检查' AS conflict_check,
  CASE
    WHEN COUNT(*) = 0 THEN '✓ 无路由路径冲突'
    ELSE CONCAT('✗ 发现 ', COUNT(*), ' 个路由路径冲突')
  END AS conflict_result
FROM (
  SELECT route_path, COUNT(*) AS path_count
  FROM temp_route_mapping
  GROUP BY route_path
  HAVING COUNT(*) > 1
) path_conflicts;

-- 检查路由名称重复
SELECT
  '路由名称冲突检查' AS conflict_check,
  CASE
    WHEN COUNT(*) = 0 THEN '✓ 无路由名称冲突'
    ELSE CONCAT('✗ 发现 ', COUNT(*), ' 个路由名称冲突')
  END AS conflict_result
FROM (
  SELECT route_name, COUNT(*) AS name_count
  FROM temp_route_mapping
  GROUP BY route_name
  HAVING COUNT(*) > 1
) name_conflicts;

-- 显示具体的路径冲突（如果存在）
SELECT
  route_path AS '冲突路径',
  GROUP_CONCAT(menu_name ORDER BY menu_name) AS '冲突菜单'
FROM temp_route_mapping
GROUP BY route_path
HAVING COUNT(*) > 1;

-- =====================================================
-- 6. 生成组件创建建议
-- =====================================================

SELECT
  '前端组件创建建议' AS creation_guide,
  menu_name AS '菜单名称',
  component_path AS '建议组件路径',
  CONCAT(
    'src/views',
    REPLACE(component_path, '/business/', '/business/'),
    ' (需要创建)'
  ) AS '完整文件路径'
FROM temp_component_check
WHERE file_exists = FALSE
ORDER BY menu_name;

-- =====================================================
-- 7. 菜单与API接口映射关系
-- =====================================================

SELECT
  '菜单与API接口映射关系' AS api_mapping_title,
  menu_name AS '菜单名称',
  api_perms AS 'API权限标识',
  CASE
    WHEN api_perms IS NOT NULL AND api_perms != '' THEN '有API权限控制'
    ELSE '无API权限控制'
  END AS api_status
FROM t_menu
WHERE menu_type IN (2, 3)
  AND deleted_flag = 0
  AND api_perms IS NOT NULL
ORDER BY menu_name;

-- =====================================================
-- 8. 最终验证总结
-- =====================================================

SELECT
  '========================================' AS separator,
  'IOE-DREAM 前端组件验证完成' AS completion_title,
  '========================================' AS separator2,
  (
    SELECT COUNT(*)
    FROM temp_component_check
    WHERE file_exists = TRUE
  ) AS existing_components_count,
  (
    SELECT COUNT(*)
    FROM temp_component_check
    WHERE file_exists = FALSE
  ) AS missing_components_count,
  (
    SELECT COUNT(*)
    FROM temp_route_mapping
  ) AS total_routes_count,
  '请根据需要创建缺失的组件文件' AS action_required;

-- 清理临时表
DROP TEMPORARY TABLE IF EXISTS temp_component_check;
DROP TEMPORARY TABLE IF EXISTS temp_route_mapping;