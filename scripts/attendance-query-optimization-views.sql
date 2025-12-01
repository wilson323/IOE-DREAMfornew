-- 考勤模块查询优化视图
-- 用于提高常用查询的性能和简化复杂查询

-- ========================================
-- 1. 员工月度考勤汇总视图
-- ========================================
CREATE OR REPLACE VIEW v_employee_monthly_attendance AS
SELECT
    ar.employee_id,
    e.employee_name,
    e.department_id,
    d.department_name,
    DATE_FORMAT(ar.attendance_date, '%Y-%m') as attendance_month,
    COUNT(*) as total_days,
    COUNT(CASE WHEN ar.attendance_status = 'NORMAL' THEN 1 END) as normal_days,
    COUNT(CASE WHEN ar.attendance_status = 'LATE' THEN 1 END) as late_days,
    COUNT(CASE WHEN ar.attendance_status = 'EARLY_LEAVE' THEN 1 END) as early_leave_days,
    COUNT(CASE WHEN ar.attendance_status = 'ABSENCE' THEN 1 END) as absence_days,
    COUNT(CASE WHEN ar.attendance_status = 'OVERTIME' THEN 1 END) as overtime_days,
    AVG(ar.work_hours) as avg_work_hours,
    SUM(ar.overtime_hours) as total_overtime_hours,
    ROUND(COUNT(CASE WHEN ar.attendance_status = 'NORMAL' THEN 1 END) * 100.0 / COUNT(*), 2) as attendance_rate,
    MAX(ar.update_time) as last_update_time
FROM t_attendance_record ar
JOIN t_employee e ON ar.employee_id = e.employee_id
JOIN t_department d ON e.department_id = d.department_id
WHERE ar.deleted_flag = 0 AND e.deleted_flag = 0
GROUP BY ar.employee_id, e.employee_name, e.department_id, d.department_name, DATE_FORMAT(ar.attendance_date, '%Y-%m');

-- ========================================
-- 2. 部门月度考勤统计视图
-- ========================================
CREATE OR REPLACE VIEW v_department_monthly_attendance AS
SELECT
    e.department_id,
    d.department_name,
    DATE_FORMAT(ar.attendance_date, '%Y-%m') as attendance_month,
    COUNT(DISTINCT ar.employee_id) as employee_count,
    COUNT(*) as total_records,
    COUNT(CASE WHEN ar.attendance_status = 'NORMAL' THEN 1 END) as normal_records,
    COUNT(CASE WHEN ar.attendance_status = 'LATE' THEN 1 END) as late_records,
    COUNT(CASE WHEN ar.attendance_status = 'EARLY_LEAVE' THEN 1 END) as early_leave_records,
    COUNT(CASE WHEN ar.attendance_status = 'ABSENCE' THEN 1 END) as absence_records,
    COUNT(CASE WHEN ar.attendance_status = 'OVERTIME' THEN 1 END) as overtime_records,
    AVG(ar.work_hours) as avg_work_hours,
    SUM(ar.overtime_hours) as total_overtime_hours,
    ROUND(COUNT(CASE WHEN ar.attendance_status = 'NORMAL' THEN 1 END) * 100.0 / COUNT(*), 2) as attendance_rate
FROM t_attendance_record ar
JOIN t_employee e ON ar.employee_id = e.employee_id
JOIN t_department d ON e.department_id = d.department_id
WHERE ar.deleted_flag = 0 AND e.deleted_flag = 0
GROUP BY e.department_id, d.department_name, DATE_FORMAT(ar.attendance_date, '%Y-%m');

-- ========================================
-- 3. 实时考勤状态视图
-- ========================================
CREATE OR REPLACE VIEW v_realtime_attendance_status AS
SELECT
    ar.employee_id,
    e.employee_name,
    e.department_id,
    d.department_name,
    ar.attendance_date,
    MAX(CASE WHEN ar.punch_type = 'IN' THEN ar.clock_time END) as punch_in_time,
    MAX(CASE WHEN ar.punch_type = 'IN' THEN ar.punch_in_location END) as punch_in_location,
    MAX(CASE WHEN ar.punch_type = 'IN' THEN ar.device_id END) as punch_in_device_id,
    MAX(CASE WHEN ar.punch_type = 'OUT' THEN ar.clock_time END) as punch_out_time,
    MAX(CASE WHEN ar.punch_type = 'OUT' THEN ar.punch_out_location END) as punch_out_location,
    MAX(CASE WHEN ar.punch_type = 'OUT' THEN ar.device_id END) as punch_out_device_id,
    CASE
        WHEN MAX(CASE WHEN ar.punch_type = 'IN' THEN 1 END) = 0 THEN '未上班'
        WHEN MAX(CASE WHEN ar.punch_type = 'OUT' THEN 1 END) = 0 THEN '未下班'
        ELSE '已完成'
    END as work_status,
    ar.attendance_status,
    ar.work_hours,
    ar.overtime_hours
FROM t_attendance_record ar
JOIN t_employee e ON ar.employee_id = e.employee_id
JOIN t_department d ON e.department_id = d.department_id
WHERE ar.attendance_date = CURDATE()
  AND ar.deleted_flag = 0
  AND e.deleted_flag = 0
GROUP BY ar.employee_id, e.employee_name, e.department_id, d.department_name, ar.attendance_date, ar.attendance_status, ar.work_hours, ar.overtime_hours;

-- ========================================
-- 4. 考勤异常统计视图
-- ========================================
CREATE OR REPLACE VIEW v_attendance_exception_statistics AS
SELECT
    ae.employee_id,
    e.employee_name,
    e.department_id,
    d.department_name,
    ae.exception_type,
    COUNT(*) as exception_count,
    COUNT(CASE WHEN ae.is_processed = 1 THEN 1 END) as processed_count,
    COUNT(CASE WHEN ae.is_processed = 0 THEN 1 END) as unprocessed_count,
    MIN(ae.attendance_date) as first_exception_date,
    MAX(ae.attendance_date) as last_exception_date,
    MAX(ae.create_time) as last_create_time
FROM t_attendance_exception ae
JOIN t_employee e ON ae.employee_id = e.employee_id
JOIN t_department d ON e.department_id = d.department_id
WHERE ae.deleted_flag = 0 AND e.deleted_flag = 0
GROUP BY ae.employee_id, e.employee_name, e.department_id, d.department_name, ae.exception_type;

-- ========================================
-- 5. 排班执行情况视图
-- ========================================
CREATE OR REPLACE VIEW v_schedule_execution AS
SELECT
    asch.employee_id,
    e.employee_name,
    e.department_id,
    d.department_name,
    asch.schedule_date,
    asch.work_start_time,
    asch.work_end_time,
    ar.punch_in_time,
    ar.punch_out_time,
    CASE
        WHEN ar.punch_in_time IS NULL THEN '未打卡'
        WHEN ar.punch_in_time > ADDTIME(asch.work_start_time, '01:00:00') THEN '迟到'
        ELSE '正常'
    END as punch_in_status,
    CASE
        WHEN ar.punch_out_time IS NULL THEN '未打卡'
        WHEN ar.punch_out_time < SUBTIME(asch.work_end_time, '01:00:00') THEN '早退'
        ELSE '正常'
    END as punch_out_status,
    ar.work_hours,
    ar.overtime_hours
FROM t_attendance_schedule asch
JOIN t_employee e ON asch.employee_id = e.employee_id
JOIN t_department d ON e.department_id = d.department_id
LEFT JOIN t_attendance_record ar ON asch.employee_id = ar.employee_id
    AND asch.schedule_date = ar.attendance_date
    AND ar.record_type = 'PUNCH'
    AND ar.deleted_flag = 0
WHERE asch.deleted_flag = 0 AND e.deleted_flag = 0;

-- ========================================
-- 6. 考勤统计报告视图
-- ========================================
CREATE OR REPLACE VIEW v_attendance_statistics_report AS
SELECT
    ast.employee_id,
    e.employee_name,
    e.department_id,
    d.department_name,
    ast.statistics_type,
    ast.statistics_period,
    ast.period_value,
    ast.total_days,
    ast.present_days,
    ast.absent_days,
    ast.late_days,
    ast.early_leave_days,
    ast.leave_days,
    ast.overtime_hours,
    ast.work_hours,
    ast.attendance_rate,
    ast.statistics_date,
    CASE
        WHEN ast.attendance_rate >= 95 THEN '优秀'
        WHEN ast.attendance_rate >= 90 THEN '良好'
        WHEN ast.attendance_rate >= 80 THEN '合格'
        ELSE '需改进'
    END as performance_level
FROM t_attendance_statistics ast
JOIN t_employee e ON ast.employee_id = e.employee_id
JOIN t_department d ON e.department_id = d.department_id
WHERE ast.deleted_flag = 0 AND e.deleted_flag = 0;

-- ========================================
-- 7. 高频查询优化视图
-- ========================================
CREATE OR REPLACE VIEW v_attendance_quick_query AS
SELECT
    ar.record_id,
    ar.employee_id,
    e.employee_name,
    e.department_id,
    d.department_name,
    ar.attendance_date,
    ar.clock_time,
    ar.punch_type,
    ar.attendance_status,
    ar.work_hours,
    ar.overtime_hours,
    ar.device_id,
    ar.create_time,
    ar.update_time
FROM t_attendance_record ar
JOIN t_employee e ON ar.employee_id = e.employee_id
JOIN t_department d ON e.department_id = d.department_id
WHERE ar.deleted_flag = 0
  AND e.deleted_flag = 0
  AND ar.attendance_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY);

-- ========================================
-- 8. 性能监控视图
-- ========================================
CREATE OR REPLACE VIEW v_attendance_performance_monitor AS
SELECT
    't_attendance_record' as table_name,
    COUNT(*) as record_count,
    COUNT(DISTINCT employee_id) as employee_count,
    MIN(attendance_date) as earliest_date,
    MAX(attendance_date) as latest_date,
    COUNT(DISTINCT DATE_FORMAT(attendance_date, '%Y-%m')) as month_count
FROM t_attendance_record
WHERE deleted_flag = 0

UNION ALL

SELECT
    't_attendance_schedule' as table_name,
    COUNT(*) as record_count,
    COUNT(DISTINCT employee_id) as employee_count,
    MIN(schedule_date) as earliest_date,
    MAX(schedule_date) as latest_date,
    COUNT(DISTINCT DATE_FORMAT(schedule_date, '%Y-%m')) as month_count
FROM t_attendance_schedule
WHERE deleted_flag = 0

UNION ALL

SELECT
    't_attendance_exception' as table_name,
    COUNT(*) as record_count,
    COUNT(DISTINCT employee_id) as employee_count,
    MIN(attendance_date) as earliest_date,
    MAX(attendance_date) as latest_date,
    COUNT(DISTINCT DATE_FORMAT(attendance_date, '%Y-%m')) as month_count
FROM t_attendance_exception
WHERE deleted_flag = 0;

-- ========================================
-- 9. 使用示例和查询优化建议
-- ========================================

-- 9.1 查询员工月度考勤情况
-- SELECT * FROM v_employee_monthly_attendance
-- WHERE employee_id = 1 AND attendance_month = '2024-01';

-- 9.2 查询部门月度考勤统计
-- SELECT * FROM v_department_monthly_attendance
-- WHERE department_id = 1 AND attendance_month = '2024-01';

-- 9.3 查询今日实时考勤状态
-- SELECT * FROM v_realtime_attendance_status
-- WHERE department_id = 1;

-- 9.4 查询考勤异常统计
-- SELECT * FROM v_attendance_exception_statistics
-- WHERE employee_id = 1 AND exception_type = 'LATE';

-- 9.5 查询排班执行情况
-- SELECT * FROM v_schedule_execution
-- WHERE employee_id = 1 AND schedule_date = '2024-01-15';

-- 9.6 查询考勤统计报告
-- SELECT * FROM v_attendance_statistics_report
-- WHERE employee_id = 1 AND statistics_type = 'MONTHLY' AND period_value = '2024-01';

-- ========================================
-- 10. 视图维护和优化建议
-- ========================================

-- 10.1 定期刷新视图
-- CREATE EVENT refresh_attendance_views
-- ON SCHEDULE EVERY 1 HOUR
-- DO
-- BEGIN
--     -- 刷新相关视图
-- END;

-- 10.2 监控视图性能
-- EXPLAIN SELECT * FROM v_employee_monthly_attendance WHERE employee_id = 1;

-- 10.3 优化建议
-- - 根据实际查询需求调整视图结构
-- - 定期分析视图性能，必要时重构
-- - 注意视图中的JOIN操作对性能的影响
-- - 考虑为视图创建适当的索引