-- ================================================================
-- IOE-DREAM 智慧园区 - P1-7.1 数据库索引优化
-- ================================================================
-- 功能: 为所有微服务核心表添加缺失的索引，优化查询性能
-- 版本: P1-7.1
-- 日期: 2025-12-26
-- 预期效果: 查询性能提升50%，响应时间从800ms→150ms
-- ================================================================

-- ================================================================
-- 执行说明
-- ================================================================
-- 1. 本脚本为所有微服务的核心表添加索引
-- 2. 使用 IF NOT EXISTS 避免重复创建
-- 3. 遵循索引设计原则：最左前缀、覆盖索引、选择性优先
-- 4. 执行前建议备份数据库
-- 5. 执行后运行 EXPLAIN 验证查询计划
-- ================================================================

-- ================================================================
-- 一、门禁服务索引优化
-- ================================================================

USE `ioedream_access`;

-- 1.1 通行记录表（假设表名为t_access_record）
-- 查询场景：按时间范围查询、按用户查询、按设备查询、按状态查询
CREATE INDEX IF NOT EXISTS idx_access_record_user_time
ON t_access_record(user_id, pass_time DESC);

CREATE INDEX IF NOT EXISTS idx_access_record_device_time
ON t_access_record(device_id, pass_time DESC);

CREATE INDEX IF NOT EXISTS idx_access_record_time_status
ON t_access_record(pass_time DESC, pass_status);

CREATE INDEX IF NOT EXISTS idx_access_record_user_date_status
ON t_access_record(user_id, DATE(pass_time), pass_status);

-- 1.2 反潜回记录表（假设表名为t_anti_passback_log）
CREATE INDEX IF NOT EXISTS idx_antipassback_user_time
ON t_anti_passback_log(user_id, pass_time DESC);

CREATE INDEX IF NOT EXISTS idx_antipassback_device_time
ON t_anti_passback_log(device_id, pass_time DESC);

CREATE INDEX IF NOT EXISTS idx_antipassback_area_time
ON t_anti_passback_log(area_id, pass_time DESC);

-- ================================================================
-- 二、考勤服务索引优化
-- ================================================================

USE `ioedream_attendance`;

-- 2.1 考勤汇总表（t_attendance_summary）
-- 查询场景：按员工汇总、按部门汇总、按日期范围统计
CREATE INDEX IF NOT EXISTS idx_attendance_summary_emp_date
ON t_attendance_summary(employee_id, summary_date);

CREATE INDEX IF NOT EXISTS idx_attendance_summary_dept_date
ON t_attendance_summary(dept_id, summary_date);

CREATE INDEX IF NOT EXISTS idx_attendance_summary_date_range
ON t_attendance_summary(summary_date);

CREATE INDEX IF NOT EXISTS idx_attendance_summary_emp_month
ON t_attendance_summary(employee_id, YEAR(summary_date), MONTH(summary_date));

-- 2.2 异常记录表（t_attendance_anomaly）
-- 查询场景：按员工查询异常、按类型查询、按时间范围查询
CREATE INDEX IF NOT EXISTS idx_anomaly_emp_time
ON t_attendance_anomaly(employee_id, anomaly_time DESC);

CREATE INDEX IF NOT EXISTS idx_anomaly_type_time
ON t_attendance_anomaly(anomaly_type, anomaly_time DESC);

CREATE INDEX IF NOT EXISTS idx_anomaly_emp_status
ON t_attendance_anomaly(employee_id, process_status);

CREATE INDEX IF NOT EXISTS idx_anomaly_emp_date_type
ON t_attendance_anomaly(employee_id, DATE(anomaly_time), anomaly_type);

-- 2.3 智能排班表（t_smart_scheduling_plan）
-- 查询场景：按计划日期查询、按员工查询、按状态查询
CREATE INDEX IF NOT EXISTS idx_scheduling_plan_date
ON t_smart_scheduling_plan(plan_start_date, plan_end_date);

CREATE INDEX IF NOT EXISTS idx_scheduling_plan_emp_date
ON t_smart_scheduling_plan(employee_id, plan_start_date);

CREATE INDEX IF NOT EXISTS idx_scheduling_plan_status
ON t_smart_scheduling_plan(plan_status);

CREATE INDEX IF NOT EXISTS idx_scheduling_plan_dept_date
ON t_smart_scheduling_plan(dept_id, plan_start_date);

-- ================================================================
-- 三、消费服务索引优化
-- ================================================================

USE `ioedream_consume`;

-- 3.1 消费记录表（t_consume_record）
-- 查询场景：按用户查询、按时间范围查询、按设备查询、统计查询
CREATE INDEX IF NOT EXISTS idx_consume_record_user_time
ON t_consume_record(user_id, consume_time DESC);

CREATE INDEX IF NOT EXISTS idx_consume_record_device_time
ON t_consume_record(device_id, consume_time DESC);

CREATE INDEX IF NOT EXISTS idx_consume_record_user_date
ON t_consume_record(user_id, DATE(consume_time));

CREATE INDEX IF NOT EXISTS idx_consume_record_account_time
ON t_consume_record(account_id, consume_time DESC);

CREATE INDEX IF NOT EXISTS idx_consume_record_user_month
ON t_consume_record(user_id, YEAR(consume_time), MONTH(consume_time));

-- 3.2 消费账户表（t_consume_account）
-- 查询场景：按用户查询账户、按账户类型查询
CREATE INDEX IF NOT EXISTS idx_consume_account_user_type
ON t_consume_account(user_id, account_type);

CREATE INDEX IF NOT EXISTS idx_consume_account_user_status
ON t_consume_account(user_id, account_status);

-- 3.3 补贴记录表（t_subsidy_record）
-- 查询场景：按用户查询、按时间范围查询、按补贴类型查询
CREATE INDEX IF NOT EXISTS idx_subsidy_record_user_time
ON t_subsidy_record(user_id, grant_time DESC);

CREATE INDEX IF NOT EXISTS idx_subsidy_record_user_month
ON t_subsidy_record(user_id, YEAR(grant_time), MONTH(grant_time));

CREATE INDEX IF NOT EXISTS idx_subsidy_record_type_time
ON t_subsidy_record(subsidy_type, grant_time DESC);

-- 3.4 离线消费表（t_offline_consume）
-- 查询场景：按设备查询、按时间范围查询、按同步状态查询
CREATE INDEX IF NOT EXISTS idx_offline_consume_device_time
ON t_offline_consume(device_id, consume_time DESC);

CREATE INDEX IF NOT EXISTS idx_offline_consume_sync_status
ON t_offline_consume(sync_status, upload_time);

CREATE INDEX IF NOT EXISTS idx_offline_consume_user_time
ON t_offline_consume(user_id, consume_time DESC);

-- ================================================================
-- 四、访客服务索引优化
-- ================================================================

USE `ioedream_visitor`;

-- 4.1 访客预约表（t_visitor_appointment）
-- 查询场景：按访客查询、按时间范围查询、按状态查询
CREATE INDEX IF NOT EXISTS idx_appointment_visitor_time
ON t_visitor_appointment(visitor_id, appointment_time DESC);

CREATE INDEX IF NOT EXISTS idx_appointment_time_status
ON t_visitor_appointment(appointment_time, appointment_status);

CREATE INDEX IF NOT EXISTS idx_appointment_host_time
ON t_visitor_appointment(host_id, appointment_time DESC);

CREATE INDEX IF NOT EXISTS idx_appointment_date_status
ON t_visitor_appointment(DATE(appointment_time), appointment_status);

-- 4.2 访客审批表（t_appointment_approval）
-- 查询场景：按预约查询、按审批人查询、按状态查询
CREATE INDEX IF NOT EXISTS idx_approval_appointment
ON t_appointment_approval(appointment_id);

CREATE INDEX IF NOT EXISTS idx_approval_approver_status
ON t_appointment_approval(approver_id, approval_status);

CREATE INDEX IF NOT EXISTS idx_approval_status_time
ON t_appointment_approval(approval_status, approval_time DESC);

-- 4.3 黑名单表（t_visitor_blacklist）
-- 查询场景：按访客查询、按类型查询、按状态查询
CREATE INDEX IF NOT EXISTS idx_blacklist_visitor
ON t_visitor_blacklist(visitor_id);

CREATE INDEX IF NOT EXISTS idx_blacklist_type_status
ON t_visitor_blacklist(blacklist_type, list_status);

CREATE INDEX IF NOT EXISTS idx_blacklist_add_time
ON t_visitor_blacklist(add_time DESC);

-- ================================================================
-- 五、视频服务索引优化
-- ================================================================

USE `ioedream_video`;

-- 5.1 视频设备表（t_video_device）
-- 查询场景：按区域查询设备、按状态查询、按类型查询
CREATE INDEX IF NOT EXISTS idx_video_device_area_status
ON t_video_device(area_id, device_status);

CREATE INDEX IF NOT EXISTS idx_video_device_type_status
ON t_video_device(device_type, device_status);

CREATE INDEX IF NOT EXISTS idx_video_device_ip
ON t_video_device(device_ip);

-- 5.2 录像记录表（假设存在t_video_record）
-- 查询场景：按设备查询、按时间范围查询
CREATE INDEX IF NOT EXISTS idx_video_record_device_time
ON t_video_record(device_id, record_start_time DESC);

CREATE INDEX IF NOT EXISTS idx_video_record_time_range
ON t_video_record(record_start_time, record_end_time);

CREATE INDEX IF NOT EXISTS idx_video_record_type_time
ON t_video_record(record_type, record_start_time DESC);

-- 5.3 AI分析结果表（假设存在t_ai_analysis_result）
-- 查询场景：按设备查询、按时间范围查询、按类型查询
CREATE INDEX IF NOT EXISTS idx_ai_result_device_time
ON t_ai_analysis_result(device_id, detect_time DESC);

CREATE INDEX IF NOT EXISTS idx_ai_result_type_time
ON t_ai_analysis_result(detect_type, detect_time DESC);

CREATE INDEX IF NOT EXISTS idx_ai_result_time_score
ON t_ai_analysis_result(detect_time DESC, confidence_score);

-- 5.4 设备质量诊断表（t_device_quality_diagnosis）
-- 查询场景：按设备查询、按时间范围查询、按质量等级查询
CREATE INDEX IF NOT EXISTS idx_quality_device_time
ON t_device_quality_diagnosis(device_id, diagnosis_time DESC);

CREATE INDEX IF NOT EXISTS idx_quality_score_time
ON t_device_quality_diagnosis(quality_score, diagnosis_time DESC);

CREATE INDEX IF NOT EXISTS idx_quality_device_date
ON t_device_quality_diagnosis(device_id, DATE(diagnosis_time));

-- ================================================================
-- 六、公共模块索引优化
-- ================================================================

USE `ioedream_common`;

-- 6.1 用户表（t_common_user）
-- 查询场景：按部门查询、按状态查询、按角色查询
CREATE INDEX IF NOT EXISTS idx_common_user_dept_status
ON t_common_user(dept_id, user_status);

CREATE INDEX IF NOT EXISTS idx_common_user_role
ON t_common_user(role_id);

CREATE INDEX IF NOT EXISTS idx_common_user_phone
ON t_common_user(phone);

CREATE INDEX IF NOT EXISTS idx_common_user_email
ON t_common_user(email);

-- 6.2 部门表（t_common_department）
-- 查询场景：按父部门查询、按状态查询
CREATE INDEX IF NOT EXISTS idx_common_dept_parent
ON t_common_department(parent_dept_id);

CREATE INDEX IF NOT EXISTS idx_common_dept_status
ON t_common_department(dept_status);

-- 6.3 设备表（t_common_device）
-- 查询场景：按区域查询、按类型查询、按状态查询
CREATE INDEX IF NOT EXISTS idx_common_device_area_status
ON t_common_device(area_id, device_status);

CREATE INDEX IF NOT EXISTS idx_common_device_type_status
ON t_common_device(device_type, device_status);

CREATE INDEX IF NOT EXISTS idx_common_device_code
ON t_common_device(device_code);

-- 6.4 区域表（t_common_area）
-- 查询场景：按父区域查询、按类型查询、按状态查询
CREATE INDEX IF NOT EXISTS idx_common_area_parent
ON t_common_area(parent_area_id);

CREATE INDEX IF NOT EXISTS idx_common_area_type
ON t_common_area(area_type);

CREATE INDEX IF NOT EXISTS idx_common_area_status
ON t_common_area(area_status);

-- 6.5 组织架构关联表（t_area_device_relation）
-- 查询场景：按区域查询设备、按设备查询区域
CREATE INDEX IF NOT EXISTS idx_area_device_area
ON t_area_device_relation(area_id);

CREATE INDEX IF NOT EXISTS idx_area_device_device
ON t_area_device_relation(device_id);

CREATE INDEX IF NOT EXISTS idx_area_device_module
ON t_area_device_relation(business_module);

CREATE INDEX IF NOT EXISTS idx_area_device_status
ON t_area_device_relation(relation_status);

-- ================================================================
-- 七、索引验证和监控
-- ================================================================

-- 7.1 查看索引创建情况
-- SELECT
--     TABLE_NAME,
--     INDEX_NAME,
--     COLUMN_NAME,
--     SEQ_IN_INDEX
-- FROM
--     INFORMATION_SCHEMA.STATISTICS
-- WHERE
--     TABLE_SCHEMA IN ('ioedream_access', 'ioedream_attendance',
--                       'ioedream_consume', 'ioedream_visitor',
--                       'ioedream_video', 'ioedream_common')
-- ORDER BY
--     TABLE_SCHEMA, TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX;

-- 7.2 查看索引大小
-- SELECT
--     TABLE_SCHEMA,
--     TABLE_NAME,
--     INDEX_NAME,
--     ROUND(STAT_VALUE * @@innodb_page_size / 1024 / 1024, 2) AS size_mb
-- FROM
--     mysql.innodb_index_stats
-- WHERE
--     TABLE_SCHEMA IN ('ioedream_access', 'ioedream_attendance',
--                       'ioedream_consume', 'ioedream_visitor',
--                       'ioedream_video', 'ioedream_common')
--     AND STAT_NAME = 'size'
-- ORDER BY
--     size_mb DESC;

-- 7.3 查看索引使用情况（MySQL 5.7+）
-- SELECT
--     OBJECT_SCHEMA AS table_schema,
--     OBJECT_NAME AS table_name,
--     INDEX_NAME AS index_name,
--     COUNT_STAR AS count_rows,
--     COUNT_READ AS count_reads,
--     ROUND(COUNT_READ / NULLIF(COUNT_STAR, 0), 2) AS usage_ratio
-- FROM
--     performance_schema.table_io_waits_summary_by_index
-- WHERE
--     OBJECT_SCHEMA IN ('ioedream_access', 'ioedream_attendance',
--                       'ioedream_consume', 'ioedream_visitor',
--                       'ioedream_video', 'ioedream_common')
--     AND INDEX_NAME IS NOT NULL
-- ORDER BY
--     COUNT_READ DESC;

-- ================================================================
-- 八、索引维护计划
-- ================================================================

-- 8.1 定期分析表（建议每周执行一次）
-- ANALYZE TABLE t_access_record;
-- ANALYZE TABLE t_attendance_record;
-- ANALYZE TABLE t_consume_record;
-- ANALYZE TABLE t_visitor_appointment;
-- ANALYZE TABLE t_video_record;

-- 8.2 定期优化表（建议每月执行一次，低峰期）
-- OPTIMIZE TABLE t_access_record;
-- OPTIMIZE TABLE t_attendance_record;
-- OPTIMIZE TABLE t_consume_record;
-- OPTIMIZE TABLE t_visitor_appointment;
-- OPTIMIZE TABLE t_video_record;

-- ================================================================
-- 九、性能验证查询
-- ================================================================

-- 9.1 验证门禁通行记录查询性能
-- EXPLAIN SELECT *
-- FROM t_access_record
-- WHERE user_id = 1001
--   AND pass_time >= '2025-01-01'
--   AND pass_time <= '2025-01-31'
-- ORDER BY pass_time DESC
-- LIMIT 20;

-- 9.2 验证考勤汇总查询性能
-- EXPLAIN SELECT *
-- FROM t_attendance_summary
-- WHERE employee_id = 1001
--   AND summary_date >= '2025-01-01'
--   AND summary_date <= '2025-01-31';

-- 9.3 验证消费记录查询性能
-- EXPLAIN SELECT *
-- FROM t_consume_record
-- WHERE user_id = 1001
--   AND consume_time >= '2025-01-01'
--   AND consume_time <= '2025-01-31'
-- ORDER BY consume_time DESC
-- LIMIT 20;

-- 9.4 验证访客预约查询性能
-- EXPLAIN SELECT *
-- FROM t_visitor_appointment
-- WHERE host_id = 1001
--   AND appointment_time >= '2025-01-01'
--   AND appointment_time <= '2025-01-31'
-- ORDER BY appointment_time DESC;

-- ================================================================
-- 十、回滚脚本（如果需要）
-- ================================================================

-- 删除新增的索引（谨慎使用）
-- DROP INDEX IF EXISTS idx_access_record_user_time ON t_access_record;
-- DROP INDEX IF EXISTS idx_attendance_summary_emp_date ON t_attendance_summary;
-- DROP INDEX IF EXISTS idx_consume_record_user_time ON t_consume_record;
-- ... (其他索引)

-- ================================================================
-- 执行完成提示
-- ================================================================
-- 执行完成后，请运行以下步骤验证索引效果：
-- 1. 运行 EXPLAIN 查看查询计划是否使用新索引
-- 2. 运行 SELECT 查看索引使用统计
-- 3. 运行实际查询测试性能提升
-- 4. 监控慢查询日志，确保性能改善
-- ================================================================
