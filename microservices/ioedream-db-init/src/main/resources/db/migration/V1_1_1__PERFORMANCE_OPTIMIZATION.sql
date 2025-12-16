-- =====================================================
-- IOE-DREAM 数据库性能优化脚本
-- 版本: V1.1.1
-- 描述: 为门禁相关表添加性能优化索引，提升查询效率
-- 创建时间: 2025-01-30
-- 执行顺序: 在V1_0_0之后执行
-- =====================================================

-- 设置执行环境
SET FOREIGN_KEY_CHECKS = 0;
SET SQL_MODE = 'STRICT_TRANS_TABLES,NO_ZERO_DATE,NO_ZERO_IN_DATE,ERROR_FOR_DIVISION_BY_ZERO';

USE ioedream;

-- =====================================================
-- 1. 门禁记录表性能优化索引
-- =====================================================

-- 用户访问记录复合索引（最常用查询）
CREATE INDEX idx_access_record_user_time ON t_access_record(user_id, access_time, deleted_flag) COMMENT '用户访问记录时间索引';

-- 设备访问记录复合索引（设备统计分析）
CREATE INDEX idx_access_record_device_time ON t_access_record(device_id, access_time, access_result, deleted_flag) COMMENT '设备访问记录时间索引';

-- 区域访问记录复合索引（区域统计分析）
CREATE INDEX idx_access_record_area_time ON t_access_record(area_id, access_time, deleted_flag) COMMENT '区域访问记录时间索引';

-- 访问结果统计复合索引（成功率分析）
CREATE INDEX idx_access_record_result_time ON t_access_record(access_result, access_time, deleted_flag) COMMENT '访问结果时间索引';

-- 访问类型统计复合索引（访问方式分析）
CREATE INDEX idx_access_record_type_time ON t_access_record(access_type, access_time, deleted_flag) COMMENT '访问类型时间索引';

-- 综合统计分析复合索引（多维分析）
CREATE INDEX idx_access_record_composite ON t_access_record(user_id, device_id, area_id, access_time, deleted_flag) COMMENT '访问记录综合分析索引';

-- =====================================================
-- 2. 门禁权限表性能优化索引
-- =====================================================

-- 用户权限复合索引（权限验证）
CREATE INDEX idx_access_permission_user_area ON t_access_permission(user_id, area_id, status, deleted_flag) COMMENT '用户权限区域索引';

-- 权限有效期复合索引（权限过期检查）
CREATE INDEX idx_access_permission_user_time ON t_access_permission(user_id, start_time, end_time, status, deleted_flag) COMMENT '用户权限时间索引';

-- 权限类型统计复合索引（权限类型分析）
CREATE INDEX idx_access_permission_type_status ON t_access_permission(permission_type, status, deleted_flag) COMMENT '权限类型状态索引';

-- 权限状态复合索引（状态查询）
CREATE INDEX idx_access_permission_status_time ON t_access_permission(status, create_time, deleted_flag) COMMENT '权限状态时间索引';

-- =====================================================
-- 3. 设备管理表性能优化索引
-- =====================================================

-- 设备状态复合索引（状态监控）
CREATE INDEX idx_common_device_status ON t_common_device(device_status, device_type, deleted_flag) COMMENT '设备状态类型索引';

-- 设备区域关联复合索引（区域设备管理）
CREATE INDEX idx_common_device_area ON t_common_device(area_id, device_type, device_status, deleted_flag) COMMENT '设备区域类型索引';

-- 设备在线状态复合索引（在线监控）
CREATE INDEX idx_common_device_online ON t_common_device(online_status, device_status, deleted_flag) COMMENT '设备在线状态索引';

-- 设备最后在线时间复合索引（离线检测）
CREATE INDEX idx_common_device_last_online ON t_common_device(last_online_time, online_status, deleted_flag) COMMENT '设备最后在线时间索引';

-- 设备搜索复合索引（设备搜索）
CREATE INDEX idx_common_device_search ON t_common_device(device_name, device_code, device_type, deleted_flag) COMMENT '设备搜索索引';

-- =====================================================
-- 4. 区域设备关联表性能优化索引
-- =====================================================

-- 区域设备复合索引（区域设备查询）
CREATE INDEX idx_area_device_area ON t_area_device_relation(area_id, relation_status, priority, deleted_flag) COMMENT '区域设备状态索引';

-- 设备区域复合索引（设备区域查询）
CREATE INDEX idx_area_device_device ON t_area_device_relation(device_id, relation_status, priority, deleted_flag) COMMENT '设备区域状态索引';

-- 设备业务模块复合索引（业务模块查询）
CREATE INDEX idx_area_device_module ON t_area_device_relation(business_module, device_type, relation_status, deleted_flag) COMMENT '设备业务模块索引';

-- 关联状态优先级复合索引（状态管理）
CREATE INDEX idx_area_device_status ON t_area_device_relation(relation_status, priority, create_time, deleted_flag) COMMENT '关联状态优先级索引';

-- 生效失效时间复合索引（时间管理）
CREATE INDEX idx_area_device_effective ON t_area_device_relation(effective_time, expire_time, relation_status, deleted_flag) COMMENT '生效失效时间索引';

-- =====================================================
-- 5. 工作流相关表性能优化索引
-- =====================================================

-- 工作流实例用户复合索引（用户工作流查询）
CREATE INDEX idx_workflow_instance_user ON t_workflow_instance(initiator_id, business_type, instance_status, create_time) COMMENT '工作流实例用户索引';

-- 业务键工作流复合索引（业务关联查询）
CREATE INDEX idx_workflow_instance_business ON t_workflow_instance(business_key, business_type, instance_status) COMMENT '业务工作流索引';

-- 工作流任务分配复合索引（任务分配查询）
CREATE INDEX idx_workflow_task_assignee ON t_workflow_task(assignee_id, task_status, create_time) COMMENT '工作流任务分配索引';

-- 工作流任务状态复合索引（任务状态管理）
CREATE INDEX idx_workflow_task_status ON t_workflow_task(task_status, create_time, due_time) COMMENT '工作流任务状态索引';

-- =====================================================
-- 6. 审计日志表性能优化索引
-- =====================================================

-- 用户操作日志复合索引（用户操作查询）
CREATE INDEX idx_audit_log_user ON t_audit_log(user_id, action_type, create_time) COMMENT '用户操作日志索引';

-- 资源操作日志复合索引（资源操作查询）
CREATE INDEX idx_audit_log_resource ON t_audit_log(resource_type, resource_id, action_type, create_time) COMMENT '资源操作日志索引';

-- IP地址操作日志复合索引（IP地址查询）
CREATE INDEX idx_audit_log_ip ON t_audit_log(client_ip, action_type, create_time) COMMENT 'IP地址操作日志索引';

-- 操作类型时间复合索引（操作类型统计）
CREATE INDEX idx_audit_log_action ON t_audit_log(action_type, create_time) COMMENT '操作类型时间索引';

-- =====================================================
-- 7. 分区表优化（针对大数据量表）
-- =====================================================

-- 访问记录表按月分区（提升查询性能）
ALTER TABLE t_access_record
PARTITION BY RANGE (YEAR(access_time)) (
    PARTITION p2024 VALUES LESS THAN (2025),
    PARTITION p2025 VALUES LESS THAN (2026),
    PARTITION p2026 VALUES LESS THAN (2027),
    PARTITION p_future VALUES LESS THAN MAXVALUE
);

-- 审计日志表按月分区（提升查询性能）
ALTER TABLE t_audit_log
PARTITION BY RANGE (YEAR(create_time)) (
    PARTITION p2024 VALUES LESS THAN (2025),
    PARTITION p2025 VALUES LESS THAN (2026),
    PARTITION p2026 VALUES LESS THAN (2027),
    PARTITION p_future VALUES LESS THAN MAXVALUE
);

-- =====================================================
-- 8. 查询优化提示和配置
-- =====================================================

-- 设置查询缓存
SET GLOBAL query_cache_size = 268435456;  -- 256MB

-- 设置排序缓冲区
SET GLOBAL sort_buffer_size = 2097152;  -- 2MB

-- 设置连接缓冲区
SET GLOBAL read_buffer_size = 131072;  -- 128KB

-- 设置随机读取缓冲区
SET GLOBAL read_rnd_buffer_size = 262144;  -- 256KB

-- =====================================================
-- 9. 统计信息更新
-- =====================================================

-- 更新表统计信息（优化查询计划）
ANALYZE TABLE t_access_record;
ANALYZE TABLE t_access_permission;
ANALYZE TABLE t_common_device;
ANALYZE TABLE t_area_device_relation;
ANALYZE TABLE t_workflow_instance;
ANALYZE TABLE t_workflow_task;
ANALYZE TABLE t_audit_log;

-- 检查表优化建议
-- SELECT * FROM sys.schema_index_statistics
-- WHERE table_schema = 'ioedream'
-- AND table_name IN ('t_access_record', 't_access_permission', 't_common_device')
-- ORDER BY cardinality DESC;

-- =====================================================
-- 10. 性能监控查询
-- =====================================================

-- 慢查询日志配置（开发环境启用）
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 2;  -- 2秒以上的查询记录
SET GLOBAL log_queries_not_using_indexes = 'ON';

-- =====================================================
-- 执行完成
-- =====================================================

SET FOREIGN_KEY_CHECKS = 1;

-- 记录迁移执行历史
INSERT INTO t_migration_history (version, description, script_name, status, start_time, end_time)
VALUES ('V1.1.1', '数据库性能优化索引创建', 'V1_1_1__PERFORMANCE_OPTIMIZATION.sql', 'SUCCESS', NOW(), NOW())
ON DUPLICATE KEY UPDATE
    description = VALUES(description),
    script_name = VALUES(script_name),
    status = VALUES(status),
    end_time = VALUES(end_time);

-- 记录优化结果
SELECT 'Performance optimization completed successfully' AS message,
       COUNT(*) AS total_indexes_created
FROM information_schema.statistics
WHERE table_schema = 'ioedream'
AND table_name IN ('t_access_record', 't_access_permission', 't_common_device', 't_area_device_relation');

COMMIT;