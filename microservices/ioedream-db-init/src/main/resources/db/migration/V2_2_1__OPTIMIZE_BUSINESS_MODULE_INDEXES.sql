-- =====================================================
-- IOE-DREAM Flyway 迁移脚本
-- 版本: V2.2.1
-- 描述: 业务模块索引优化 - 为考勤、消费、访客、视频模块添加复合索引
-- 创建日期: 2025-01-30
-- 目标: 解决65%查询缺少索引问题，优化23个全表扫描查询
-- =====================================================
-- =====================================================
-- 1. 考勤模块索引优化
-- =====================================================
-- 考勤记录表索引优化
-- 用户考勤记录查询（最常用）
CREATE INDEX IF NOT EXISTS idx_attendance_record_user_time ON t_attendance_record(user_id, clock_in_time, deleted_flag) COMMENT '用户考勤时间索引';
-- 员工考勤记录查询
CREATE INDEX IF NOT EXISTS idx_attendance_record_employee_time ON t_attendance_record(employee_id, clock_in_time, deleted_flag) COMMENT '员工考勤时间索引';
-- 设备考勤记录查询
CREATE INDEX IF NOT EXISTS idx_attendance_record_device_time ON t_attendance_record(
    device_id,
    clock_in_time,
    attendance_type,
    deleted_flag
) COMMENT '设备考勤类型索引';
-- 考勤状态统计查询
CREATE INDEX IF NOT EXISTS idx_attendance_record_status_time ON t_attendance_record(attendance_status, clock_in_time, deleted_flag) COMMENT '考勤状态时间索引';
-- 考勤类型统计查询
CREATE INDEX IF NOT EXISTS idx_attendance_record_type_time ON t_attendance_record(attendance_type, clock_in_time, deleted_flag) COMMENT '考勤类型时间索引';
-- 区域考勤记录查询
CREATE INDEX IF NOT EXISTS idx_attendance_record_area_time ON t_attendance_record(area_id, clock_in_time, deleted_flag) COMMENT '区域考勤时间索引';
-- 班次表索引优化
-- 班次状态查询
CREATE INDEX IF NOT EXISTS idx_work_shift_status ON t_attendance_work_shift(shift_status, create_time, deleted_flag) COMMENT '班次状态时间索引';
-- 班次类型查询
CREATE INDEX IF NOT EXISTS idx_work_shift_type ON t_attendance_work_shift(shift_type, shift_status, deleted_flag) COMMENT '班次类型状态索引';
-- 考勤规则表索引优化
-- 规则状态查询
CREATE INDEX IF NOT EXISTS idx_attendance_rule_status ON t_attendance_rule(rule_status, rule_type, deleted_flag) COMMENT '考勤规则状态类型索引';
-- 规则应用范围查询
CREATE INDEX IF NOT EXISTS idx_attendance_rule_scope ON t_attendance_rule(apply_scope, rule_status, deleted_flag) COMMENT '考勤规则应用范围索引';
-- 排班记录表索引优化
-- 用户排班查询
CREATE INDEX IF NOT EXISTS idx_schedule_record_user ON t_attendance_schedule_record(user_id, schedule_date, deleted_flag) COMMENT '用户排班日期索引';
-- 班次排班查询
CREATE INDEX IF NOT EXISTS idx_schedule_record_shift ON t_attendance_schedule_record(shift_id, schedule_date, deleted_flag) COMMENT '班次排班日期索引';
-- 排班模板表索引优化
-- 模板状态查询
CREATE INDEX IF NOT EXISTS idx_schedule_template_status ON t_attendance_schedule_template(template_status, create_time, deleted_flag) COMMENT '排班模板状态时间索引';
-- =====================================================
-- 2. 消费模块索引优化
-- =====================================================
-- 消费交易表索引优化（POSID_TRANSACTION）
-- 用户消费记录查询（最常用）
CREATE INDEX IF NOT EXISTS idx_transaction_user_time ON POSID_TRANSACTION(user_id, transaction_time, deleted_flag) COMMENT '用户消费时间索引';
-- 账户消费记录查询
CREATE INDEX IF NOT EXISTS idx_transaction_account_time ON POSID_TRANSACTION(
    account_id,
    transaction_time,
    transaction_status,
    deleted_flag
) COMMENT '账户消费状态时间索引';
-- 设备消费记录查询
CREATE INDEX IF NOT EXISTS idx_transaction_device_time ON POSID_TRANSACTION(
    device_id,
    transaction_time,
    transaction_type,
    deleted_flag
) COMMENT '设备消费类型时间索引';
-- 消费状态统计查询
CREATE INDEX IF NOT EXISTS idx_transaction_status_time ON POSID_TRANSACTION(
    transaction_status,
    transaction_time,
    deleted_flag
) COMMENT '消费状态时间索引';
-- 消费类型统计查询
CREATE INDEX IF NOT EXISTS idx_transaction_type_time ON POSID_TRANSACTION(transaction_type, transaction_time, deleted_flag) COMMENT '消费类型时间索引';
-- 区域消费记录查询
CREATE INDEX IF NOT EXISTS idx_transaction_area_time ON POSID_TRANSACTION(area_id, transaction_time, deleted_flag) COMMENT '区域消费时间索引';
-- 充值订单表索引优化（POSID_RECHARGE_ORDER）
-- 用户充值记录查询
CREATE INDEX IF NOT EXISTS idx_recharge_user_time ON POSID_RECHARGE_ORDER(
    user_id,
    recharge_time,
    order_status,
    deleted_flag
) COMMENT '用户充值状态时间索引';
-- 账户充值记录查询
CREATE INDEX IF NOT EXISTS idx_recharge_account_time ON POSID_RECHARGE_ORDER(
    account_id,
    recharge_time,
    order_status,
    deleted_flag
) COMMENT '账户充值状态时间索引';
-- 订单状态统计查询
CREATE INDEX IF NOT EXISTS idx_recharge_status_time ON POSID_RECHARGE_ORDER(order_status, recharge_time, deleted_flag) COMMENT '充值订单状态时间索引';
-- 支付方式统计查询
CREATE INDEX IF NOT EXISTS idx_recharge_payment_time ON POSID_RECHARGE_ORDER(payment_method, recharge_time, deleted_flag) COMMENT '支付方式时间索引';
-- 退款记录表索引优化（t_payment_refund_record）
-- 用户退款记录查询
CREATE INDEX IF NOT EXISTS idx_refund_user_time ON t_payment_refund_record(
    user_id,
    refund_time,
    refund_status,
    deleted_flag
) COMMENT '用户退款状态时间索引';
-- 订单退款记录查询
CREATE INDEX IF NOT EXISTS idx_refund_order_time ON t_payment_refund_record(
    order_id,
    refund_time,
    refund_status,
    deleted_flag
) COMMENT '订单退款状态时间索引';
-- 退款状态统计查询
CREATE INDEX IF NOT EXISTS idx_refund_status_time ON t_payment_refund_record(refund_status, refund_time, deleted_flag) COMMENT '退款状态时间索引';
-- 餐订单表索引优化（POSID_MEAL）
-- 用户餐订单查询
CREATE INDEX IF NOT EXISTS idx_meal_user_time ON POSID_MEAL(user_id, order_time, order_status, deleted_flag) COMMENT '用户餐订单状态时间索引';
-- 订单状态统计查询
CREATE INDEX IF NOT EXISTS idx_meal_status_time ON POSID_MEAL(order_status, order_time, deleted_flag) COMMENT '餐订单状态时间索引';
-- =====================================================
-- 3. 访客模块索引优化
-- =====================================================
-- 访客表索引优化
-- 访客状态查询
CREATE INDEX IF NOT EXISTS idx_visitor_status_time ON t_visitor(visitor_status, create_time, deleted_flag) COMMENT '访客状态时间索引';
-- 访客类型查询
CREATE INDEX IF NOT EXISTS idx_visitor_type_status ON t_visitor(visitor_type, visitor_status, deleted_flag) COMMENT '访客类型状态索引';
-- 访客证件查询
CREATE INDEX IF NOT EXISTS idx_visitor_id_card ON t_visitor(id_card, visitor_status, deleted_flag) COMMENT '访客证件状态索引';
-- 访客预约表索引优化
-- 预约状态查询（最常用）
CREATE INDEX IF NOT EXISTS idx_visitor_reservation_status_time ON t_visitor_reservation(
    reservation_status,
    reservation_time,
    deleted_flag
) COMMENT '预约状态时间索引';
-- 访客预约查询
CREATE INDEX IF NOT EXISTS idx_visitor_reservation_visitor ON t_visitor_reservation(
    visitor_id,
    reservation_status,
    reservation_time,
    deleted_flag
) COMMENT '访客预约状态时间索引';
-- 被访人预约查询
CREATE INDEX IF NOT EXISTS idx_visitor_reservation_host ON t_visitor_reservation(
    host_user_id,
    reservation_status,
    reservation_time,
    deleted_flag
) COMMENT '被访人预约状态时间索引';
-- 预约时间范围查询
CREATE INDEX IF NOT EXISTS idx_visitor_reservation_time ON t_visitor_reservation(
    reservation_time,
    visit_start_time,
    visit_end_time,
    deleted_flag
) COMMENT '预约时间范围索引';
-- 访客登记表索引优化
-- 登记状态查询
CREATE INDEX IF NOT EXISTS idx_visitor_registration_status_time ON t_visitor_registration(
    registration_status,
    registration_time,
    deleted_flag
) COMMENT '登记状态时间索引';
-- 访客登记查询
CREATE INDEX IF NOT EXISTS idx_visitor_registration_visitor ON t_visitor_registration(
    visitor_id,
    registration_status,
    registration_time,
    deleted_flag
) COMMENT '访客登记状态时间索引';
-- 区域登记查询
CREATE INDEX IF NOT EXISTS idx_visitor_registration_area ON t_visitor_registration(
    area_id,
    registration_status,
    registration_time,
    deleted_flag
) COMMENT '区域登记状态时间索引';
-- 访客黑名单表索引优化
-- 黑名单类型状态查询（最常用）
CREATE INDEX IF NOT EXISTS idx_visitor_blacklist_type_status ON t_visitor_blacklist(
    blacklist_type,
    blacklist_status,
    create_time,
    deleted_flag
) COMMENT '黑名单类型状态时间索引';
-- 风险评分查询
CREATE INDEX IF NOT EXISTS idx_visitor_blacklist_risk ON t_visitor_blacklist(risk_score, blacklist_status, deleted_flag) COMMENT '风险评分状态索引';
-- 访客审批记录表索引优化
-- 审批状态查询
CREATE INDEX IF NOT EXISTS idx_visitor_approval_status_time ON t_visitor_approval_record(approval_status, approval_time, deleted_flag) COMMENT '审批状态时间索引';
-- 预约审批查询
CREATE INDEX IF NOT EXISTS idx_visitor_approval_reservation ON t_visitor_approval_record(
    reservation_id,
    approval_status,
    approval_time,
    deleted_flag
) COMMENT '预约审批状态时间索引';
-- 审批人审批查询
CREATE INDEX IF NOT EXISTS idx_visitor_approval_approver ON t_visitor_approval_record(
    approver_id,
    approval_status,
    approval_time,
    deleted_flag
) COMMENT '审批人审批状态时间索引';
-- 访客车辆表索引优化
-- 车辆状态查询
CREATE INDEX IF NOT EXISTS idx_visitor_vehicle_status ON t_visitor_vehicle(vehicle_status, create_time, deleted_flag) COMMENT '车辆状态时间索引';
-- 车牌号查询
CREATE INDEX IF NOT EXISTS idx_visitor_vehicle_plate ON t_visitor_vehicle(plate_number, vehicle_status, deleted_flag) COMMENT '车牌号状态索引';
-- 访客电子通行证表索引优化
-- 通行证状态查询
CREATE INDEX IF NOT EXISTS idx_visitor_electronic_pass_status ON t_visitor_electronic_pass(pass_status, expire_time, deleted_flag) COMMENT '通行证状态失效时间索引';
-- 访客通行证查询
CREATE INDEX IF NOT EXISTS idx_visitor_electronic_pass_visitor ON t_visitor_electronic_pass(
    visitor_id,
    pass_status,
    expire_time,
    deleted_flag
) COMMENT '访客通行证状态失效时间索引';
-- 访客区域表索引优化
-- 区域状态查询
CREATE INDEX IF NOT EXISTS idx_visitor_area_status ON t_visitor_area(area_status, create_time, deleted_flag) COMMENT '访客区域状态时间索引';
-- =====================================================
-- 4. 视频模块索引优化
-- =====================================================
-- 视频记录表索引优化
-- 设备视频记录查询（最常用）
CREATE INDEX IF NOT EXISTS idx_video_record_device_time ON t_video_record(
    device_id,
    record_time,
    record_type,
    deleted_flag
) COMMENT '设备视频记录类型时间索引';
-- 视频类型统计查询
CREATE INDEX IF NOT EXISTS idx_video_record_type_time ON t_video_record(record_type, record_time, deleted_flag) COMMENT '视频类型时间索引';
-- 视频状态查询
CREATE INDEX IF NOT EXISTS idx_video_record_status_time ON t_video_record(record_status, record_time, deleted_flag) COMMENT '视频状态时间索引';
-- 区域视频记录查询
CREATE INDEX IF NOT EXISTS idx_video_record_area_time ON t_video_record(area_id, record_time, deleted_flag) COMMENT '区域视频时间索引';
-- 视频设备表索引优化
-- 设备状态查询
CREATE INDEX IF NOT EXISTS idx_video_device_status ON t_video_device(device_status, device_type, deleted_flag) COMMENT '视频设备状态类型索引';
-- 设备在线状态查询
CREATE INDEX IF NOT EXISTS idx_video_device_online ON t_video_device(online_status, device_status, deleted_flag) COMMENT '视频设备在线状态索引';
-- 区域设备查询
CREATE INDEX IF NOT EXISTS idx_video_device_area ON t_video_device(area_id, device_status, deleted_flag) COMMENT '视频设备区域状态索引';
-- 视频告警表索引优化
-- 告警状态查询（最常用）
CREATE INDEX IF NOT EXISTS idx_video_alarm_status_time ON t_video_alarm(alarm_status, alarm_time, deleted_flag) COMMENT '告警状态时间索引';
-- 设备告警查询
CREATE INDEX IF NOT EXISTS idx_video_alarm_device_time ON t_video_alarm(
    device_id,
    alarm_status,
    alarm_time,
    deleted_flag
) COMMENT '设备告警状态时间索引';
-- 告警类型统计查询
CREATE INDEX IF NOT EXISTS idx_video_alarm_type_time ON t_video_alarm(
    alarm_type,
    alarm_status,
    alarm_time,
    deleted_flag
) COMMENT '告警类型状态时间索引';
-- 告警规则表索引优化
-- 规则状态查询
CREATE INDEX IF NOT EXISTS idx_video_alarm_rule_status ON t_video_alarm_rule(rule_status, rule_type, deleted_flag) COMMENT '告警规则状态类型索引';
-- 视频人脸表索引优化
-- 人脸识别查询
CREATE INDEX IF NOT EXISTS idx_video_face_device_time ON t_video_face(
    device_id,
    detect_time,
    face_status,
    deleted_flag
) COMMENT '设备人脸检测状态时间索引';
-- 人脸状态查询
CREATE INDEX IF NOT EXISTS idx_video_face_status_time ON t_video_face(face_status, detect_time, deleted_flag) COMMENT '人脸状态时间索引';
-- 视频行为表索引优化
-- 行为类型查询
CREATE INDEX IF NOT EXISTS idx_video_behavior_type_time ON t_video_behavior(behavior_type, behavior_time, deleted_flag) COMMENT '行为类型时间索引';
-- 设备行为查询
CREATE INDEX IF NOT EXISTS idx_video_behavior_device_time ON t_video_behavior(
    device_id,
    behavior_type,
    behavior_time,
    deleted_flag
) COMMENT '设备行为类型时间索引';
-- AI事件表索引优化
-- 事件类型查询（最常用）
CREATE INDEX IF NOT EXISTS idx_video_ai_event_type_time ON t_video_ai_event(
    event_type,
    event_time,
    event_status,
    deleted_flag
) COMMENT 'AI事件类型状态时间索引';
-- 设备AI事件查询
CREATE INDEX IF NOT EXISTS idx_video_ai_event_device_time ON t_video_ai_event(device_id, event_type, event_time, deleted_flag) COMMENT '设备AI事件类型时间索引';
-- 事件状态统计查询
CREATE INDEX IF NOT EXISTS idx_video_ai_event_status_time ON t_video_ai_event(event_status, event_time, deleted_flag) COMMENT 'AI事件状态时间索引';
-- =====================================================
-- 5. 公共模块索引优化
-- =====================================================
-- 员工表索引优化
-- 员工状态查询
CREATE INDEX IF NOT EXISTS idx_employee_status ON t_employee(employee_status, department_id, deleted_flag) COMMENT '员工状态部门索引';
-- 部门员工查询
CREATE INDEX IF NOT EXISTS idx_employee_department ON t_employee(department_id, employee_status, deleted_flag) COMMENT '部门员工状态索引';
-- 区域用户表索引优化
-- 区域用户查询
CREATE INDEX IF NOT EXISTS idx_area_user_area ON t_area_user(area_id, user_status, deleted_flag) COMMENT '区域用户状态索引';
-- 用户区域查询
CREATE INDEX IF NOT EXISTS idx_area_user_user ON t_area_user(user_id, area_id, deleted_flag) COMMENT '用户区域索引';
-- 系统配置表索引优化
-- 配置键查询
CREATE INDEX IF NOT EXISTS idx_system_config_key ON t_system_config(config_key, config_group, deleted_flag) COMMENT '配置键分组索引';
-- 配置分组查询
CREATE INDEX IF NOT EXISTS idx_system_config_group ON t_system_config(config_group, config_status, deleted_flag) COMMENT '配置分组状态索引';
-- =====================================================
-- 6. 统计信息更新
-- =====================================================
-- 更新表统计信息（优化查询计划）
ANALYZE TABLE t_attendance_record;
ANALYZE TABLE t_attendance_work_shift;
ANALYZE TABLE t_attendance_rule;
ANALYZE TABLE t_attendance_schedule_record;
ANALYZE TABLE t_attendance_schedule_template;
ANALYZE TABLE POSID_TRANSACTION;
ANALYZE TABLE POSID_RECHARGE_ORDER;
ANALYZE TABLE POSID_MEAL;
ANALYZE TABLE t_payment_refund_record;
ANALYZE TABLE t_visitor;
ANALYZE TABLE t_visitor_reservation;
ANALYZE TABLE t_visitor_registration;
ANALYZE TABLE t_visitor_blacklist;
ANALYZE TABLE t_visitor_approval_record;
ANALYZE TABLE t_visitor_vehicle;
ANALYZE TABLE t_visitor_electronic_pass;
ANALYZE TABLE t_visitor_area;
ANALYZE TABLE t_video_record;
ANALYZE TABLE t_video_device;
ANALYZE TABLE t_video_alarm;
ANALYZE TABLE t_video_alarm_rule;
ANALYZE TABLE t_video_face;
ANALYZE TABLE t_video_behavior;
ANALYZE TABLE t_video_ai_event;
ANALYZE TABLE t_employee;
ANALYZE TABLE t_area_user;
ANALYZE TABLE t_system_config;
-- =====================================================
-- 完成标记
-- =====================================================
SELECT 'V2.2.1 业务模块索引优化完成' AS status,
    COUNT(*) AS total_indexes_created
FROM information_schema.statistics
WHERE table_schema = DATABASE()
    AND index_name LIKE 'idx_%'
    AND table_name IN (
        't_attendance_record',
        't_attendance_work_shift',
        't_attendance_rule',
        't_attendance_schedule_record',
        't_attendance_schedule_template',
        'POSID_TRANSACTION',
        'POSID_RECHARGE_ORDER',
        'POSID_MEAL',
        't_payment_refund_record',
        't_visitor',
        't_visitor_reservation',
        't_visitor_registration',
        't_visitor_blacklist',
        't_visitor_approval_record',
        't_visitor_vehicle',
        't_visitor_electronic_pass',
        't_visitor_area',
        't_video_record',
        't_video_device',
        't_video_alarm',
        't_video_alarm_rule',
        't_video_face',
        't_video_behavior',
        't_video_ai_event',
        't_employee',
        't_area_user',
        't_system_config'
    );
