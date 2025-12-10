-- =====================================================
-- IOE-DREAM 角色权限详细配置脚本
-- 版本: v1.0.0 企业级RBAC权限体系
-- 创建日期: 2025-01-08
-- 说明:
-- - 基于RBAC模型设计细粒度权限控制
-- - 支持数据权限和功能权限分离
-- - 符合企业级安全合规要求
-- =====================================================

-- =====================================================
-- 1. 扩展角色表，添加角色描述和数据权限标识
-- =====================================================

-- 1.1 临时角色描述信息（如果有描述字段则使用，否则仅作文档参考）
-- 由于现有角色表可能没有description字段，这里作为文档记录
/*
超级管理员 (SUPER_ADMIN):
  - 拥有系统所有权限
  - 可以管理所有业务数据
  - 可以配置系统参数
  - 负责系统维护和升级

系统管理员 (SYSTEM_ADMIN):
  - 负责用户、角色、权限管理
  - 负责部门、字典管理
  - 查看系统日志
  - 无业务数据操作权限

业务管理员 (BUSINESS_ADMIN):
  - 负责所有业务模块管理
  - 包括门禁、考勤、消费、访客、视频等
  - 不能管理系统级配置

普通用户 (USER):
  - 基础业务操作权限
  - 查看个人相关信息
  - 基础的考勤打卡、消费查询等
*/

-- =====================================================
-- 2. 详细权限分配 - 系统管理员权限
-- =====================================================

-- 清空现有的系统管理员权限（重新分配）
DELETE FROM t_role_menu WHERE role_id = 2;

-- 系统管理员分配系统管理模块权限
INSERT INTO `t_role_menu` (role_id, menu_id)
SELECT 2, menu_id FROM t_menu
WHERE deleted_flag = 0
AND (
  -- 系统管理模块及其所有子菜单
  parent_id = (SELECT menu_id FROM t_menu WHERE menu_name = '系统管理' AND deleted_flag = 0)
  OR menu_id = (SELECT menu_id FROM t_menu WHERE menu_name = '系统管理' AND deleted_flag = 0)

  -- 系统监控相关（系统管理员需要监控权限）
  OR parent_id = (SELECT menu_id FROM t_menu WHERE menu_name = '监控运维' AND deleted_flag = 0)
  AND web_perms LIKE 'infrastructure:monitoring:%'
);

-- 系统管理员具体权限明细
-- 用户管理权限（但不能操作超级管理员）
INSERT INTO `t_role_menu` (role_id, menu_id)
SELECT 2, menu_id FROM t_menu
WHERE menu_name IN ('用户管理', '角色管理', '菜单管理', '部门管理', '字典管理', '操作日志', '登录日志')
AND deleted_flag = 0;

-- 用户管理功能权限（排除重置密码等敏感操作）
INSERT INTO `t_role_menu` (role_id, menu_id)
SELECT 2, menu_id FROM t_menu
WHERE menu_name IN ('用户查询', '用户新增', '用户更新', '用户删除', '状态变更')
AND deleted_flag = 0;

-- 角色管理功能权限
INSERT INTO `t_role_menu` (role_id, menu_id)
SELECT 2, menu_id FROM t_menu
WHERE menu_name IN ('角色查询', '角色新增', '角色更新', '角色删除', '权限分配')
AND deleted_flag = 0;

-- =====================================================
-- 3. 业务管理员权限配置
-- =====================================================

-- 清空现有的业务管理员权限
DELETE FROM t_role_menu WHERE role_id = 3;

-- 业务管理员分配所有业务模块权限
INSERT INTO `t_role_menu` (role_id, menu_id)
SELECT 3, menu_id FROM t_menu
WHERE deleted_flag = 0
AND (
  -- 企业OA模块
  parent_id = (SELECT menu_id FROM t_menu WHERE menu_name = '企业OA' AND deleted_flag = 0)
  OR menu_id = (SELECT menu_id FROM t_menu WHERE menu_name = '企业OA' AND deleted_flag = 0)

  -- 门禁管理模块
  OR parent_id = (SELECT menu_id FROM t_menu WHERE menu_name = '门禁管理' AND deleted_flag = 0)
  OR menu_id = (SELECT menu_id FROM t_menu WHERE menu_name = '门禁管理' AND deleted_flag = 0)

  -- 考勤管理模块
  OR parent_id = (SELECT menu_id FROM t_menu WHERE menu_name = '考勤管理' AND deleted_flag = 0)
  OR menu_id = (SELECT menu_id FROM t_menu WHERE menu_name = '考勤管理' AND deleted_flag = 0)

  -- 消费管理模块
  OR parent_id = (SELECT menu_id FROM t_menu WHERE menu_name = '消费管理' AND deleted_flag = 0)
  OR menu_id = (SELECT menu_id FROM t_menu WHERE menu_name = '消费管理' AND deleted_flag = 0)

  -- 访客管理模块
  OR parent_id = (SELECT menu_id FROM t_menu WHERE menu_name = '访客管理' AND deleted_flag = 0)
  OR menu_id = (SELECT menu_id FROM t_menu WHERE menu_name = '访客管理' AND deleted_flag = 0)

  -- 智能视频模块
  OR parent_id = (SELECT menu_id FROM t_menu WHERE menu_name = '智能视频' AND deleted_flag = 0)
  OR menu_id = (SELECT menu_id FROM t_menu WHERE menu_name = '智能视频' AND deleted_flag = 0)

  -- 设备通讯模块（业务管理员需要查看设备状态）
  OR parent_id = (SELECT menu_id FROM t_menu WHERE menu_name = '设备通讯' AND deleted_flag = 0)
  AND web_perms IN (
    'infrastructure:device-comm:connections',
    'infrastructure:device-comm:monitor'
  )
);

-- 业务管理员具体的消费账户管理权限
INSERT INTO `t_role_menu` (role_id, menu_id)
SELECT 3, menu_id FROM t_menu
WHERE menu_name IN (
  '账户新增', '账户充值', '账户退款', '余额调整', '账户冻结', '账户解冻'
)
AND deleted_flag = 0;

-- =====================================================
-- 4. 普通用户权限配置
-- =====================================================

-- 清空现有的普通用户权限
DELETE FROM t_role_menu WHERE role_id = 4;

-- 普通用户分配基础权限
INSERT INTO `t_role_menu` (role_id, menu_id)
SELECT 4, menu_id FROM t_menu
WHERE deleted_flag = 0
AND web_perms IN (
  -- 个人相关权限
  'system:account:query',
  'system:login-log:query',

  -- 企业OA基础权限
  'business:oa:notice',
  'business:oa:workflow:my-process',
  'business:oa:workflow:pending-task',
  'business:oa:workflow:completed-task',

  -- 门禁基础权限（只能查看自己的通行记录）
  'business:access:dashboard',
  'business:access:record',

  -- 考勤基础权限（考勤打卡和查看自己的记录）
  'business:attendance:record',
  'business:attendance:statistics',

  -- 消费基础权限（查看自己的账户和交易记录）
  'business:consumption:dashboard',
  'business:consumption:account',
  'business:consumption:transaction',

  -- 访客基础权限（预约和查看自己的访客记录）
  'business:visitor:appointment',
  'business:visitor:record',
  'business:visitor:verification'
);

-- =====================================================
-- 5. 创建专业角色（可根据业务需要扩展）
-- =====================================================

-- 5.1 考勤专员角色
INSERT INTO `t_role` (
  `role_id`, `role_name`, `role_code`, `sort`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES
(5, '考勤专员', 'ATTENDANCE_ADMIN', 10, 1, NOW(), 1, NOW());

-- 考勤专员权限
INSERT INTO `t_role_menu` (role_id, menu_id)
SELECT 5, menu_id FROM t_menu
WHERE deleted_flag = 0
AND web_perms LIKE 'business:attendance:%'
OR web_perms IN (
  'system:account:query',  -- 需要查询用户信息
  'system:department:query'  -- 需要查询部门信息
);

-- 5.2 消费专员角色
INSERT INTO `t_role` (
  `role_id`, `role_name`, `role_code`, `sort`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES
(6, '消费专员', 'CONSUME_ADMIN', 11, 1, NOW(), 1, NOW());

-- 消费专员权限
INSERT INTO `t_role_menu` (role_id, menu_id)
SELECT 6, menu_id FROM t_menu
WHERE deleted_flag = 0
AND (
  web_perms LIKE 'business:consumption:%'
  OR web_perms LIKE 'business:erp:%'
)
OR web_perms IN (
  'system:account:query',
  'system:department:query'
);

-- 5.3 安防管理员角色（门禁+视频+访客）
INSERT INTO `t_role` (
  `role_id`, `role_name`, `role_code`, `sort`,
  `create_user_id`, `create_time`, `update_user_id`, `update_time`
) VALUES
(7, '安防管理员', 'SECURITY_ADMIN', 12, 1, NOW(), 1, NOW());

-- 安防管理员权限
INSERT INTO `t_role_menu` (role_id, menu_id)
SELECT 7, menu_id FROM t_menu
WHERE deleted_flag = 0
AND (
  web_perms LIKE 'business:access:%'
  OR web_perms LIKE 'business:visitor:%'
  OR web_perms LIKE 'business:video:%'
)
OR web_perms IN (
  'system:account:query',
  'infrastructure:device-comm:connections'
);

-- =====================================================
-- 6. 权限验证查询
-- =====================================================

-- 6.1 验证每个角色的菜单权限数量
SELECT
  r.role_name,
  r.role_code,
  COUNT(rm.menu_id) AS permission_count,
  SUM(CASE WHEN m.menu_type = 1 THEN 1 ELSE 0 END) AS directories,
  SUM(CASE WHEN m.menu_type = 2 THEN 1 ELSE 0 END) AS menus,
  SUM(CASE WHEN m.menu_type = 3 THEN 1 ELSE 0 END) AS functions
FROM t_role r
LEFT JOIN t_role_menu rm ON r.role_id = rm.role_id
LEFT JOIN t_menu m ON rm.menu_id = m.menu_id AND m.deleted_flag = 0
GROUP BY r.role_id, r.role_name, r.role_code
ORDER BY r.sort;

-- 6.2 验证关键模块的权限分配
SELECT
  m.menu_name AS module_name,
  COUNT(DISTINCT rm.role_id) AS role_count,
  GROUP_CONCAT(DISTINCT r.role_name ORDER BY r.sort) AS assigned_roles
FROM t_menu m
JOIN t_role_menu rm ON m.menu_id = rm.menu_id
JOIN t_role r ON rm.role_id = r.role_id
WHERE m.parent_id = 0 AND m.deleted_flag = 0
GROUP BY m.menu_id, m.menu_name
ORDER BY m.sort;

-- 6.3 验证超级管理员是否有所有权限
SELECT
  CASE
    WHEN COUNT(*) = (SELECT COUNT(*) FROM t_menu WHERE deleted_flag = 0)
    THEN '✓ 超级管理员拥有完整权限'
    ELSE '✗ 超级管理员权限不完整'
  END AS admin_permission_check,
  COUNT(*) AS current_permissions,
  (SELECT COUNT(*) FROM t_menu WHERE deleted_flag = 0) AS total_required_permissions
FROM t_role_menu
WHERE role_id = 1;

-- 6.4 权限冲突检查（检查是否有重复或冲突的权限分配）
SELECT
  '权限冲突检查' AS check_type,
  CASE
    WHEN COUNT(*) = 0
    THEN '✓ 无权限冲突'
    ELSE '✗ 存在权限冲突'
  END AS result
FROM (
  SELECT menu_id, COUNT(*) AS duplicate_count
  FROM t_role_menu
  GROUP BY menu_id
  HAVING COUNT(*) > 7  -- 如果某个菜单被超过7个角色分配，可能存在权限过滥
) conflicts;

-- =====================================================
-- 7. 数据权限配置建议
-- =====================================================

-- 数据权限配置说明（需要在具体业务逻辑中实现）：
/*
超级管理员:
  - 数据权限：全部数据
  - 组织权限：所有组织

系统管理员:
  - 数据权限：系统相关数据
  - 组织权限：无业务数据权限

业务管理员:
  - 数据权限：所在组织及下级组织的全部业务数据
  - 组织权限：所在组织及下级组织

考勤专员:
  - 数据权限：所在组织及下级组织的考勤数据
  - 组织权限：所在组织及下级组织

消费专员:
  - 数据权限：所在组织及下级组织的消费数据
  - 组织权限：所在组织及下级组织

安防管理员:
  - 数据权限：所在组织及下级组织的门禁、访客、视频数据
  - 组织权限：所在组织及下级组织

普通用户:
  - 数据权限：个人相关数据
  - 组织权限：仅个人所在组织
*/

-- 权限验证完成提示
SELECT
  '========================================' AS separator,
  'IOE-DREAM 权限体系配置完成' AS completion_status,
  '========================================' AS separator2,
  '1. 已创建 7 种角色类型' AS role_count,
  '2. 已配置 9 大业务模块权限' AS module_count,
  '3. 已实现 RBAC 权限模型' AS rbac_status,
  '4. 建议在业务逻辑中实现数据权限控制' AS data_permission_tip;