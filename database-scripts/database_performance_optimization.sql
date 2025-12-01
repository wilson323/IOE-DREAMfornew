-- ====================================================================
-- 区域权限查询性能优化索引脚本
-- 创建时间: 2025-11-25
-- 描述: 为area-permission-enhancement提案的Task 3.2优化区域权限查询性能
-- 基于AreaPersonDao.xml中的查询模式分析
-- ====================================================================

-- 1) 分析现有查询模式并优化索引

-- 现有索引（来自people_area_permission_upgrade_v2.sql）
-- CREATE INDEX IF NOT EXISTS idx_area_person_composite ON t_area_person(area_id, data_scope, status);

-- 基于AreaPersonDao.xml分析，需要优化以下查询模式：

-- Pattern 1: 按person_id查询 (最频繁)
-- SELECT area_id FROM t_area_person WHERE person_id = ? AND status = 1 AND deleted_flag = 0...
CREATE INDEX IF NOT EXISTS idx_area_person_person_active ON t_area_person(person_id, status, deleted_flag, effective_time, expire_time);

-- Pattern 2: 按area_id批量查询
-- SELECT * FROM t_area_person WHERE area_id IN (?, ?, ?) AND status = 1 AND deleted_flag = 0...
CREATE INDEX IF NOT EXISTS idx_area_person_area_active ON t_area_person(area_id, status, deleted_flag, effective_time, expire_time);

-- Pattern 3: data_scope查询
-- SELECT area_id FROM t_area_person WHERE person_id = ? AND data_scope = ? AND status = 1...
CREATE INDEX IF NOT EXISTS idx_area_person_data_scope ON t_area_person(person_id, data_scope, status, deleted_flag);

-- Pattern 4: 时间范围查询
-- SELECT * FROM t_area_person WHERE person_id = ? AND (effective_time <= ? AND (expire_time IS NULL OR expire_time >= ?))...
CREATE INDEX IF NOT EXISTS idx_area_person_time_range ON t_area_person(person_id, effective_time, expire_time, status);

-- Pattern 5: 复合查询（AreaPersonDao中的复杂查询）
-- 涉及t_area表连接的查询需要区域表索引

-- 2) 为t_area表添加区域层次查询优化索引

-- 区域层次结构路径查询优化（用于getAreaIdsByPathPrefix）
CREATE INDEX IF NOT EXISTS idx_area_path_lookup ON t_area(path, deleted_flag, status);

-- 区域父子关系查询优化
CREATE INDEX IF NOT EXISTS idx_area_parent_lookup ON t_area(parent_id, deleted_flag, status);

-- 区域类型和状态查询优化
CREATE INDEX IF NOT EXISTS idx_area_type_status ON t_area(area_type, status, deleted_flag);

-- 区域排序优化
CREATE INDEX IF NOT EXISTS idx_area_sort_order ON t_area(parent_id, sort_order, deleted_flag);

-- 区域完整路径查询优化（支持批量层次查询）
CREATE INDEX IF NOT EXISTS idx_area_full_path ON t_area(path, level, parent_id, deleted_flag);

-- 3) 批量操作性能优化索引

-- 批量权限检查优化（支持batchCheckAreaPermissions）
CREATE INDEX IF NOT EXISTS idx_area_person_batch_check ON t_area_person(person_id, area_id, status, deleted_flag, effective_time, expire_time);

-- 批量用户权限查询优化（支持batchGetUserAuthorizedAreaIds）
CREATE INDEX IF NOT EXISTS idx_area_person_user_areas ON t_area_person(person_id, status, deleted_flag, expire_time, area_id);

-- 批量权限状态查询优化（支持batchGetPermissionStatus）
CREATE INDEX IF NOT EXISTS idx_area_person_status_batch ON t_area_person(status, deleted_flag, person_id, area_id, data_scope);

-- 4) 为权限相关表添加优化索引

-- t_rbac_resource表优化
CREATE INDEX IF NOT EXISTS idx_rbac_resource_lookup ON t_rbac_resource(module, resource_type, resource_id, status);

-- t_rbac_role_resource表优化
CREATE INDEX IF NOT EXISTS idx_rbac_role_resource_composite ON t_rbac_role_resource(role_id, action, status, resource_id);

-- 4) 为统计查询添加覆盖索引

-- 区域权限统计查询优化
CREATE INDEX IF NOT EXISTS idx_area_person_stats ON t_area_person(area_id, person_id, status, deleted_flag);

-- 权限检查查询优化
CREATE INDEX IF NOT EXISTS idx_area_permission_check ON t_area_person(person_id, area_id, status, deleted_flag);

-- 5) 性能监控查询（用于验证优化效果）

-- 查询执行计划分析查询
EXPLAIN SELECT area_id FROM t_area_person WHERE person_id = 1 AND status = 1 AND deleted_flag = 0;
EXPLAIN SELECT ap.area_id FROM t_area_person ap INNER JOIN t_area a ON ap.area_id = a.area_id WHERE ap.person_id = 1 AND ap.status = 1 AND a.path LIKE '1,%' AND a.deleted_flag = 0;
EXPLAIN SELECT * FROM t_area_person WHERE person_id IN (1,2,3) AND area_id IN (10,20,30) AND status = 1 AND deleted_flag = 0;

-- 6) 批量操作优化（用于解决N+1查询问题）

-- 批量权限检查优化
CREATE TEMPORARY TABLE IF NOT EXISTS temp_area_permission_check (
    person_id BIGINT,
    area_id BIGINT,
    INDEX (person_id, area_id)
);

-- 7) 缓存预热建议

-- 预热高频查询的缓存键模式：
-- area:permission:person:{person_id}:*
-- area:permission:area:{area_id}:*
-- area:hierarchy:path:{area_id}
-- area:hierarchy:children:{parent_area_id}

-- 8) 性能基准测试建议

-- 测试单条查询性能（期望 < 5ms）
-- SELECT area_id FROM t_area_person WHERE person_id = ? AND status = 1 AND deleted_flag = 0 LIMIT 1;

-- 测试批量查询性能（期望 < 50ms）
-- SELECT area_id FROM t_area_person WHERE person_id IN (/* 100个ID */) AND status = 1 AND deleted_flag = 0;

-- 测试层次查询性能（期望 < 100ms）
-- SELECT ap.area_id FROM t_area_person ap INNER JOIN t_area a ON ap.area_id = a.area_id WHERE ap.person_id = ? AND a.path LIKE CONCAT(?, '%') AND a.deleted_flag = 0;

-- 9) 索引维护建议

-- 定期清理过期的权限记录
-- UPDATE t_area_person SET status = 0 WHERE expire_time < NOW() AND status = 1;

-- 定期更新统计信息
-- 分析表以评估索引使用情况和查询性能

COMMIT;