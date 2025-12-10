-- ============================================
-- IOE-DREAM 全局数据库性能优化统一方案
-- 版本: 2.0.0 - P2级企业级优化
-- 日期: 2025-12-09
-- 说明: 统一各业务模块索引优化，确保全局一致性和高性能
-- 严格遵循CLAUDE.md全局架构规范
-- ============================================

-- ============================================
-- 1. 数据库连接池统一优化配置
-- ============================================

-- 1.1 Druid连接池优化（替换所有HikariCP）
-- 统一使用Druid连接池，符合CLAUDE.md规范

-- 连接池配置参数建议：
-- initial-size: 10（初始化连接数）
-- min-idle: 10（最小空闲连接数）
-- max-active: 50（最大活跃连接数）
-- max-wait: 60000（获取连接等待超时时间）
-- time-between-eviction-runs-millis: 60000（连接检测周期）
-- min-evictable-idle-time-millis: 300000（连接保持空闲时间）
-- validation-query: SELECT 1（连接有效性检测SQL）
-- test-while-idle: true（空闲时检测连接有效性）
-- test-on-borrow: false（获取连接时不检测，提高性能）
-- test-on-return: false（归还连接时不检测，提高性能）

-- ============================================
-- 2. 深度分页查询优化解决方案
-- ============================================

-- 2.1 问题说明：避免使用LIMIT offset, size的深度分页
-- 错误示例：SELECT * FROM table LIMIT 100000, 20;

-- 2.2 解决方案：基于ID的游标分页
-- 推荐模式：使用主键ID或时间戳作为游标

-- 2.3 统一分页查询模板：
-- 第一页查询：
-- SELECT * FROM table WHERE create_time >= ? ORDER BY id LIMIT 20;

-- 后续页查询：
-- SELECT * FROM table WHERE id > ? AND create_time >= ? ORDER BY id LIMIT 20;

-- ============================================
-- 3. 全局索引命名规范
-- ============================================

-- 3.1 索引命名标准化
-- 主键索引: pk_表名
-- 唯一索引: uk_表名_字段名
-- 普通索引: idx_表名_字段名
-- 联合索引: idx_表名_字段1_字段2_字段3
-- 全文索引: ft_表名_字段名

-- 3.2 字段命名标准化
-- 用户ID相关: user_id, user_id_time, user_id_status
-- 时间相关: create_time, update_time, access_time, consume_time
-- 状态相关: status, deleted_flag, permission_status
-- 区域相关: area_id, area_time, area_device
-- 设备相关: device_id, device_time, device_status

-- ============================================
-- 4. 消费模块 (consume) 索引优化增强版
-- ============================================

-- 4.1 consume_record 表索引优化
CREATE UNIQUE INDEX IF NOT EXISTS `uk_consume_record_transaction_no`
ON `consume_record` (`transaction_no`);

CREATE INDEX IF NOT EXISTS `idx_consume_record_user_time_status`
ON `consume_record` (`user_id`, `consume_time`, `status`, `deleted_flag`);

CREATE INDEX IF NOT EXISTS `idx_consume_record_device_time_amount`
ON `consume_record` (`device_id`, `consume_time`, `amount`, `deleted_flag`);

CREATE INDEX IF NOT EXISTS `idx_consume_record_area_time_deleted`
ON `consume_record` (`area_id`, `consume_time`, `deleted_flag`);

-- 覆盖索引：避免回表查询
CREATE INDEX IF NOT EXISTS `idx_consume_record_time_status_amount_deleted`
ON `consume_record` (`consume_time`, `status`, `amount`, `deleted_flag`);

-- 4.2 consume_transaction 表索引优化
CREATE UNIQUE INDEX IF NOT EXISTS `uk_consume_transaction_transaction_no`
ON `consume_transaction` (`transaction_no`);

CREATE INDEX IF NOT EXISTS `idx_consume_transaction_user_status_time`
ON `consume_transaction` (`user_id`, `transaction_status`, `transaction_time`, `deleted_flag`);

CREATE INDEX IF NOT EXISTS `idx_consume_transaction_account_status_time`
ON `consume_transaction` (`account_id`, `transaction_status`, `transaction_time`, `deleted_flag`);

-- 覆盖索引：用于统计查询
CREATE INDEX IF NOT EXISTS `idx_consume_transaction_time_status_amount_deleted`
ON `consume_transaction` (`transaction_time`, `transaction_status`, `amount`, `deleted_flag`);

-- 4.3 consume_payment_record 表索引优化（新增）
CREATE UNIQUE INDEX IF NOT EXISTS `uk_consume_payment_payment_id`
ON `consume_payment_record` (`payment_id`);

CREATE UNIQUE INDEX IF NOT EXISTS `uk_consume_payment_order_no`
ON `consume_payment_record` (`order_no`);

CREATE INDEX IF NOT EXISTS `idx_consume_payment_user_time_status`
ON `consume_payment_record` (`user_id`, `create_time`, `payment_status`);

CREATE INDEX IF NOT EXISTS `idx_consume_payment_merchant_time`
ON `consume_payment_record` (`merchant_id`, `create_time`, `settlement_status`);

-- 覆盖索引：用于对账查询
CREATE INDEX IF NOT EXISTS `idx_consume_payment_time_status_amount_deleted`
ON `consume_payment_record` (`create_time`, `payment_status`, `payment_amount`, `deleted_flag`);

-- ============================================
-- 5. 门禁模块 (access) 索引优化增强版
-- ============================================

-- 5.1 t_access_record 表索引优化
CREATE INDEX IF NOT EXISTS `idx_access_record_user_time_result`
ON `t_access_record` (`user_id`, `access_time`, `access_result`, `deleted_flag`);

CREATE INDEX IF NOT EXISTS `idx_access_record_device_time_area`
ON `t_access_record` (`device_id`, `access_time`, `area_id`, `deleted_flag`);

CREATE INDEX IF NOT EXISTS `idx_access_record_area_time_type`
ON `t_access_record` (`area_id`, `access_time`, `access_type`, `deleted_flag`);

-- 覆盖索引：用于统计分析
CREATE INDEX IF NOT EXISTS `idx_access_record_time_result_type_deleted`
ON `t_access_record` (`access_time`, `access_result`, `access_type`, `deleted_flag`);

-- 5.2 t_access_device 表索引优化
CREATE INDEX IF NOT EXISTS `idx_access_device_area_status_online`
ON `t_access_device` (`area_id`, `device_status`, `online_status`, `deleted_flag`);

CREATE INDEX IF NOT EXISTS `idx_access_device_type_status_time`
ON `t_access_device` (`device_type`, `device_status`, `update_time`, `deleted_flag`);

-- 5.3 t_access_permission 表索引优化
CREATE INDEX IF NOT EXISTS `idx_access_permission_user_area_time`
ON `t_access_permission` (`user_id`, `area_id`, `permission_status`, `create_time`, `deleted_flag`);

CREATE INDEX IF NOT EXISTS `idx_access_permission_area_type_status`
ON `t_access_permission` (`area_id`, `permission_type`, `permission_status`, `deleted_flag`);

-- ============================================
-- 6. 考勤模块 (attendance) 索引优化增强版
-- ============================================

-- 6.1 t_attendance_record 表索引优化
CREATE INDEX IF NOT EXISTS `idx_attendance_record_user_time_status`
ON `t_attendance_record` (`user_id`, `attendance_time`, `attendance_status`, `deleted_flag`);

CREATE INDEX IF NOT EXISTS `idx_attendance_record_device_time_area`
ON `t_attendance_record` (`device_id`, `attendance_time`, `area_id`, `deleted_flag`);

CREATE INDEX IF NOT EXISTS `idx_attendance_record_shift_time`
ON `t_attendance_record` (`shift_id`, `attendance_time`, `deleted_flag`);

-- 覆盖索引：用于考勤统计
CREATE INDEX IF NOT EXISTS `idx_attendance_record_time_status_type_deleted`
ON `t_attendance_record` (`attendance_time`, `attendance_status`, `attendance_type`, `deleted_flag`);

-- 6.2 t_work_shift 表索引优化
CREATE INDEX IF NOT EXISTS `idx_work_shift_type_status_time`
ON `t_work_shift` (`shift_type`, `status`, `create_time`, `deleted_flag`);

CREATE INDEX IF NOT EXISTS `idx_work_shift_area_time`
ON `t_work_shift` (`area_id`, `start_time`, `end_time`, `deleted_flag`);

-- ============================================
-- 7. 访客模块 (visitor) 索引优化增强版
-- ============================================

-- 7.1 t_visitor_record 表索引优化
CREATE INDEX IF NOT EXISTS `idx_visitor_record_visitor_time_status`
ON `t_visitor_record` (`visitor_id`, `visit_time`, `visit_status`, `deleted_flag`);

CREATE INDEX IF NOT EXISTS `idx_visitor_record_host_time_area`
ON `t_visitor_record` (`host_user_id`, `visit_time`, `area_id`, `deleted_flag`);

CREATE INDEX IF NOT EXISTS `idx_visitor_record_appointment_time`
ON `t_visitor_record` (`appointment_id`, `visit_time`, `deleted_flag`);

-- 覆盖索引：用于访客统计
CREATE INDEX IF NOT EXISTS `idx_visitor_record_time_status_type_deleted`
ON `t_visitor_record` (`visit_time`, `visit_status`, `visit_type`, `deleted_flag`);

-- 7.2 t_visitor_appointment 表索引优化
CREATE INDEX IF NOT EXISTS `idx_visitor_appointment_visitor_time_status`
ON `t_visitor_appointment` (`visitor_id`, `appointment_time`, `appointment_status`, `deleted_flag`);

CREATE INDEX IF NOT EXISTS `idx_visitor_appointment_host_time_status`
ON `t_visitor_appointment` (`host_user_id`, `appointment_time`, `appointment_status`, `deleted_flag`);

-- ============================================
-- 8. 视频模块 (video) 索引优化增强版
-- ============================================

-- 8.1 t_video_device 表索引优化
CREATE INDEX IF NOT EXISTS `idx_video_device_area_status_online`
ON `t_video_device` (`area_id`, `device_status`, `online_status`, `deleted_flag`);

CREATE INDEX IF NOT EXISTS `idx_video_device_type_status_time`
ON `t_video_device` (`device_type`, `device_status`, `update_time`, `deleted_flag`);

-- 8.2 t_video_record 表索引优化
CREATE INDEX IF NOT EXISTS `idx_video_record_device_time_type`
ON `t_video_record` (`device_id`, `record_time`, `record_type`, `deleted_flag`);

CREATE INDEX IF NOT EXISTS `idx_video_record_area_time_size`
ON `t_video_record` (`area_id`, `record_time`, `file_size`, `deleted_flag`);

-- 覆盖索引：用于视频统计
CREATE INDEX IF NOT EXISTS `idx_video_record_time_type_size_deleted`
ON `t_video_record` (`record_time`, `record_type`, `file_size`, `deleted_flag`);

-- ============================================
-- 9. 公共模块 (common) 索引优化
-- ============================================

-- 9.1 t_common_user 表索引优化
CREATE UNIQUE INDEX IF NOT EXISTS `uk_common_user_username`
ON `t_common_user` (`username`);

CREATE UNIQUE INDEX IF NOT EXISTS `uk_common_user_phone`
ON `t_common_user` (`phone`);

CREATE INDEX IF NOT EXISTS `uk_common_user_email`
ON `t_common_user` (`email`);

CREATE INDEX IF NOT EXISTS `idx_common_user_status_time`
ON `t_common_user` (`status`, `create_time`, `deleted_flag`);

CREATE INDEX IF NOT EXISTS `idx_common_user_dept_time`
ON `t_common_user` (`dept_id`, `create_time`, `deleted_flag`);

-- 9.2 t_common_department 表索引优化
CREATE INDEX IF NOT EXISTS `idx_common_dept_parent_status`
ON `t_common_department` (`parent_id`, `status`, `deleted_flag`);

CREATE INDEX IF NOT EXISTS `idx_common_dept_level_time`
ON `t_common_department` (`dept_level`, `create_time`, `deleted_flag`);

-- 9.3 t_common_device 表索引优化
CREATE INDEX IF NOT EXISTS `idx_common_device_type_status_area`
ON `t_common_device` (`device_type`, `device_status`, `area_id`, `deleted_flag`);

CREATE INDEX IF NOT EXISTS `idx_common_device_code_time`
ON `t_common_device` (`device_code`, `update_time`, `deleted_flag`);

-- 9.4 t_area_device_relation 表索引优化（区域设备关联）
CREATE INDEX IF NOT EXISTS `idx_area_device_area_type_status`
ON `t_area_device_relation` (`area_id`, `device_type`, `relation_status`, `deleted_flag`);

CREATE INDEX IF NOT EXISTS `idx_area_device_device_priority`
ON `t_area_device_relation` (`device_id`, `priority`, `effective_time`);

-- ============================================
-- 10. 性能监控和优化建议
-- ============================================

-- 10.1 慢查询监控SQL
-- 启用MySQL慢查询日志：
-- SET GLOBAL slow_query_log = 'ON';
-- SET GLOBAL long_query_time = 1; -- 记录执行超过1秒的查询
-- SET GLOBAL slow_query_log_file = '/var/log/mysql/mysql-slow.log';

-- 10.2 索引使用情况分析
-- 查看索引使用情况：
-- SELECT
--     TABLE_SCHEMA,
--     TABLE_NAME,
--     INDEX_NAME,
--     CARDINALITY,
--     SUB_PART,
--     NULLABLE,
--     INDEX_TYPE
-- FROM INFORMATION_SCHEMA.STATISTICS
-- WHERE TABLE_SCHEMA = DATABASE()
-- ORDER BY TABLE_NAME, INDEX_NAME;

-- 10.3 表统计信息更新
-- 定期更新表统计信息，优化查询执行计划：
-- ANALYZE TABLE table_name;

-- 10.4 索引重建和优化
-- 定期重建索引，提高查询性能：
-- OPTIMIZE TABLE table_name;

-- ============================================
-- 11. 企业级数据库配置建议
-- ============================================

-- 11.1 MySQL配置优化建议（my.cnf）
-- [mysqld]
-- # 连接数配置
-- max_connections = 1000
-- max_connect_errors = 10000

-- # InnoDB引擎优化
-- innodb_buffer_pool_size = 2G  -- 设置为物理内存的70-80%
-- innodb_log_file_size = 256M
-- innodb_log_buffer_size = 16M
-- innodb_flush_log_at_trx_commit = 2  -- 平衡性能和安全性

-- # 查询缓存
-- query_cache_type = 1
-- query_cache_size = 256M

-- # 临时表配置
-- tmp_table_size = 256M
-- max_heap_table_size = 256M

-- # 排序缓冲区
-- sort_buffer_size = 2M
-- join_buffer_size = 2M

-- 11.2 Redis缓存配置优化
-- # 内存配置
-- maxmemory 2G
-- maxmemory-policy allkeys-lru

-- # 持久化配置
-- save 900 1
-- save 300 10
-- save 60 10000

-- ============================================
-- 12. 执行验证脚本
-- ============================================

-- 验证所有索引是否创建成功
SELECT
    s.TABLE_SCHEMA,
    s.TABLE_NAME,
    s.INDEX_NAME,
    s.COLUMN_NAME,
    s.SEQ_IN_INDEX,
    s.CARDINALITY,
    t.TABLE_COMMENT
FROM
    INFORMATION_SCHEMA.STATISTICS s
    JOIN INFORMATION_SCHEMA.TABLES t ON s.TABLE_SCHEMA = t.TABLE_SCHEMA AND s.TABLE_NAME = t.TABLE_NAME
WHERE
    s.TABLE_SCHEMA = DATABASE()
    AND (
        s.TABLE_NAME LIKE 'consume_%'
        OR s.TABLE_NAME LIKE 't_access_%'
        OR s.TABLE_NAME LIKE 't_attendance_%'
        OR s.TABLE_NAME LIKE 't_visitor_%'
        OR s.TABLE_NAME LIKE 't_video_%'
        OR s.TABLE_NAME LIKE 't_common_%'
        OR s.TABLE_NAME = 't_area_device_relation'
    )
ORDER BY
    s.TABLE_NAME,
    s.INDEX_NAME,
    s.SEQ_IN_INDEX;

-- ============================================
-- 13. 性能测试基准
-- ============================================

-- 基准测试目标：
-- 1. 查询响应时间：简单查询 < 50ms，复杂查询 < 200ms
-- 2. 并发处理能力：支持1000+ TPS
-- 3. 索引命中率：> 95%
-- 4. 慢查询比例：< 0.1%

-- 性能监控指标：
-- 1. QPS（每秒查询数）
-- 2. TPS（每秒事务数）
-- 3. 连接池使用率
-- 4. 缓存命中率
-- 5. 磁盘I/O使用率