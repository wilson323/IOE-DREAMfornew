-- ============================================================
-- IOE-DREAM Attendance Service 扩展测试数据
--
-- 功能：提供集成测试所需的扩展数据（异常申请、加班申请等）
-- 使用：@Sql(scripts = "/sql/02-test-extended-data.sql")
-- 依赖：01-test-basic-data.sql
-- ============================================================

-- ============================================================
-- 1. 考勤异常申请测试数据
-- ============================================================

INSERT INTO t_attendance_anomaly_apply (
    apply_id, apply_no, applicant_id, applicant_name,
    department_id, department_name, anomaly_id, anomaly_type,
    apply_type, punch_type, apply_reason, apply_time,
    approver_id, approver_name, approval_time, approval_comment,
    apply_status, status, deleted_flag, create_time, update_time
) VALUES
(
    1,  -- 缺卡申请
    'APP20250127001',
    1, '张三',
    101, '研发部',
    1,  -- 关联到anomaly_id=1
    'MISSING_CARD',
    'SUPPLEMENT_CARD',  -- 补卡申请
    'IN',               -- 上班缺卡
    '忘记打卡，申请补卡',
    '2025-01-27 09:00:00',
    NULL, NULL, NULL, NULL,
    'PENDING',  -- 待审批
    1,
    0,
    '2025-01-27 09:00:00',
    '2025-01-27 09:00:00'
),
(
    2,  -- 迟到申诉
    'APP20250127002',
    2, '李四',
    102, '产品部',
    NULL,  -- 没有异常记录（申诉场景）
    'LATE',
    'APPEAL',  -- 申诉
    NULL,
    '因客户会议迟到，申请不扣款',
    '2025-01-27 10:00:00',
    NULL, NULL, NULL, NULL,
    'PENDING',
    1,
    0,
    '2025-01-27 10:00:00',
    '2025-01-27 10:00:00'
),
(
    3,  -- 早退申诉
    'APP20250127003',
    3, '王五',
    103, '测试部',
    NULL,
    'EARLY',
    'APPEAL',
    NULL,
    '因紧急私事早退，申请不扣款',
    '2025-01-27 17:00:00',
    NULL, NULL, NULL, NULL,
    'PENDING',
    1,
    0,
    '2025-01-27 17:00:00',
    '2025-01-27 17:00:00'
);

-- ============================================================
-- 2. 加班申请测试数据
-- ============================================================

INSERT INTO t_attendance_overtime_apply (
    apply_id, apply_no, user_id, user_name,
    department_id, department_name,
    overtime_date, overtime_type, overtime_hours,
    start_time, end_time,
    overtime_reason, apply_time,
    approver_id, approver_name, approval_time, approval_comment,
    apply_status, status, deleted_flag, create_time, update_time
) VALUES
(
    1,  -- 工作日加班
    'OT20250127001',
    1, '张三',
    101, '研发部',
    '2025-01-27',
    'WORKDAY',  -- 工作日加班
    2.5,       -- 加班2.5小时
    '17:30:00',
    '20:00:00',
    '项目紧急上线',
    '2025-01-27 14:00:00',
    NULL, NULL, NULL, NULL,
    'PENDING',
    1,
    0,
    '2025-01-27 14:00:00',
    '2025-01-27 14:00:00'
),
(
    2,  -- 周末加班
    'OT20250128001',
    2, '李四',
    102, '产品部',
    '2025-01-28',  -- 周一（假设为工作日）
    'WEEKEND',  -- 周末加班
    8.0,
    '09:00:00',
    '18:00:00',
    '周末产品培训',
    '2025-01-27 15:00:00',
    NULL, NULL, NULL, NULL,
    'PENDING',
    1,
    0,
    '2025-01-27 15:00:00',
    '2025-01-27 15:00:00'
);

-- ============================================================
-- 3. 考勤汇总测试数据
-- ============================================================

INSERT INTO t_attendance_summary (
    summary_id, employee_id, employee_name, department_id, department_name,
    summary_month,
    work_days, actual_days, absent_days,
    late_count, early_count,
    overtime_hours, weekend_overtime_hours,
    leave_days, attendance_rate,
    status, deleted_flag, create_time, update_time
) VALUES
(
    1,  -- 张三的月度汇总
    1, '张三',
    101, '研发部',
    '2025-01',
    22,  -- 工作日22天
    20,  -- 实际出勤20天
    0,   -- 无旷工
    2,   -- 迟到2次
    1,   -- 早退1次
    2.5, -- 加班2.5小时
    0,   -- 周末加班0小时
    0,   -- 请假0天
    0.9091,  -- 出勤率90.91%
    1,
    0,
    '2025-01-27 23:00:00',
    '2025-01-27 23:00:00'
),
(
    2,  -- 李四的月度汇总
    2, '李四',
    102, '产品部',
    '2025-01',
    22,
    21,
    0,
    5,   -- 迟到5次
    0,
    8.0, -- 加班8小时
    0,
    0,
    0.9545,  -- 出勤率95.45%
    1,
    0,
    '2025-01-27 23:00:00',
    '2025-01-27 23:00:00'
);

-- ============================================================
-- 4. 部门统计测试数据
-- ============================================================

INSERT INTO t_department_statistics (
    statistics_id, department_id, department_name, statistics_month,
    total_employees, present_employees, absent_employees,
    late_employees, attendance_rate,
    avg_work_hours, total_overtime_hours, exception_count,
    status, deleted_flag, create_time, update_time
) VALUES
(
    1,  -- 研发部月度统计
    101, '研发部',
    '2025-01',
    50,  -- 总员工50人
    48,  -- 实际出勤48人
    2,   -- 缺勤2人
    10,  -- 迟到10人
    0.96, -- 平均出勤率96%
    8.0,  -- 平均工作8小时
    100.0, -- 总加班100小时
    5,     -- 异常5次
    1,
    0,
    '2025-01-27 23:00:00',
    '2025-01-27 23:00:00'
),
(
    2,  -- 产品部月度统计
    102, '产品部',
    '2025-01',
    30,
    29,
    1,
    8,
    0.9667,
    8.0,
    80.0,
    3,
    1,
    0,
    '2025-01-27 23:00:00',
    '2025-01-27 23:00:00'
);

-- ============================================================
-- 5. 排班记录测试数据（智能排班模块）
-- ============================================================

INSERT INTO t_schedule_record (
    record_id, employee_id, employee_name, department_id,
    shift_id, shift_name, schedule_date,
    schedule_type, schedule_status,
    status, deleted_flag, create_time, update_time
) VALUES
(
    1,
    1, '张三',
    101,
    1, '正常班',
    '2025-01-27',
    'AUTO',    -- 自动排班
    'ACTIVE',  -- 生效
    1,
    0,
    '2025-01-27 00:00:00',
    '2025-01-27 00:00:00'
),
(
    2,
    2, '李四',
    102,
    2, '早班',
    '2025-01-27',
    'AUTO',
    'ACTIVE',
    1,
    0,
    '2025-01-27 00:00:00',
    '2025-01-27 00:00:00'
),
(
    3,
    3, '王五',
    103,
    3, '晚班',
    '2025-01-27',
    'MANUAL',  -- 手动排班
    'ACTIVE',
    1,
    0,
    '2025-01-27 00:00:00',
    '2025-01-27 00:00:00'
);

-- ============================================================
-- 6. 请假记录测试数据
-- ============================================================

INSERT INTO t_attendance_leave (
    leave_id, user_id, user_name, department_id, department_name,
    leave_type, start_date, end_date, leave_days,
    leave_reason, approver_id, approver_name,
    apply_time, approval_time,
    leave_status, status, deleted_flag, create_time, update_time
) VALUES
(
    1,
    1, '张三',
    101, '研发部',
    'ANNUAL',  -- 年假
    '2025-02-01',
    '2025-02-03',
    3,  -- 请假3天
    '年假休息',
    NULL, NULL,  -- 未审批
    '2025-01-27 16:00:00',
    NULL,
    'PENDING',
    1,
    0,
    '2025-01-27 16:00:00',
    '2025-01-27 16:00:00'
),
(
    2,
    2, '李四',
    102, '产品部',
    'SICK',  -- 病假
    '2025-01-28',
    '2025-01-29',
    2,  -- 请假2天
    '身体不适，医院检查',
    NULL, NULL,
    '2025-01-27 16:00:00',
    NULL,
    'PENDING',
    1,
    0,
    '2025-01-27 16:00:00',
    '2025-01-27 16:00:00'
);

-- ============================================================
-- 数据插入完成提示
-- ============================================================

SELECT '扩展测试数据插入完成' AS message;
SELECT COUNT(*) AS apply_count FROM t_attendance_anomaly_apply;
SELECT COUNT(*) AS overtime_apply_count FROM t_attendance_overtime_apply;
SELECT COUNT(*) AS summary_count FROM t_attendance_summary;
SELECT COUNT(*) AS statistics_count FROM t_department_statistics;
SELECT COUNT(*) AS schedule_count FROM t_schedule_record;
SELECT COUNT(*) AS leave_count FROM t_attendance_leave;
