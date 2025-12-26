-- ============================================================
-- IOE-DREAM 测试数据库初始化脚本
-- 步骤5: 插入测试数据
-- ============================================================

USE ioedream_test;

-- ============================================================
-- 插入测试部门
-- ============================================================
INSERT INTO t_common_department (department_id, department_name, parent_id, department_level, sort_order, status) VALUES
  (1, 'IOE-DREAM公司', 0, 1, 1, 1),
  (2, '技术部', 1, 2, 1, 1),
  (3, '市场部', 1, 2, 2, 1),
  (4, '人事部', 1, 2, 3, 1),
  (5, '研发组', 2, 3, 1, 1);

-- ============================================================
-- 插入测试用户
-- 密码均为: 123456 (BCrypt加密)
-- ============================================================
INSERT INTO t_common_user (user_id, username, password, phone, email, real_name, gender, dept_id, status) VALUES
  (1001, 'test_user_001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EQCbKYaLJHc2FJWNNJqK4', '13800000001', 'test001@example.com', '测试用户001', 1, 2, 1),
  (1002, 'test_user_002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EQCbKYaLJHc2FJWNNJqK4', '13800000002', 'test002@example.com', '测试用户002', 2, 2, 1),
  (1003, 'test_user_003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EQCbKYaLJHc2FJWNNJqK4', '13800000003', 'test003@example.com', '测试用户003', 1, 3, 1);

-- ============================================================
-- 插入测试区域
-- ============================================================
INSERT INTO t_common_area (area_id, area_name, area_type, parent_id, area_level, sort_order, status) VALUES
  (1, 'A栋办公楼', 1, 0, 1, 1, 1),
  (2, 'A栋1楼', 2, 1, 2, 1, 1),
  (3, 'A栋2楼', 2, 1, 2, 2, 1),
  (4, '会议室A', 3, 2, 3, 1, 1),
  (5, '办公区', 3, 3, 3, 1, 1);

-- ============================================================
-- 插入测试设备
-- ============================================================
INSERT INTO t_common_device (device_id, device_code, device_name, device_type, device_sub_type, area_id, ip_address, port, status) VALUES
  ('DEV001', 'ACCESS_CTRL_001', '主入口门禁', 1, 11, 2, '192.168.1.101', 8000, 1),
  ('DEV002', 'ACCESS_CTRL_002', '会议室门禁', 1, 11, 4, '192.168.1.102', 8000, 1),
  ('DEV003', 'ATTEND_001', '1楼考勤机', 2, 21, 2, '192.168.1.103', 8000, 1),
  ('DEV004', 'FACE_REC_001', '人脸识别机', 1, 12, 2, '192.168.1.104', 8000, 1),
  ('DEV005', 'CONSUME_POS_001', '餐厅POS机', 3, 31, 2, '192.168.1.105', 8000, 1);

-- ============================================================
-- 插入门禁设备特定数据
-- ============================================================
INSERT INTO t_access_device (device_id, device_code, device_name, device_sub_type, area_id, ip_address, port, access_mode, anti_passback_enabled, open_duration, status) VALUES
  ('DEV001', 'ACCESS_CTRL_001', '主入口门禁', 11, 2, '192.168.1.101', 8000, 1, 1, 3000, 1),
  ('DEV002', 'ACCESS_CTRL_002', '会议室门禁', 11, 4, '192.168.1.102', 8000, 1, 1, 3000, 1),
  ('DEV004', 'FACE_REC_001', '人脸识别机', 12, 2, '192.168.1.104', 8000, 1, 1, 3000, 1);

-- ============================================================
-- 插入考勤班次
-- ============================================================
INSERT INTO t_attendance_work_shift (shift_id, shift_name, shift_type, work_start_time, work_end_time, work_duration, late_tolerance, early_tolerance, min_overtime_duration, status) VALUES
  (1, '正常班', 1, '09:00:00', '18:00:00', 540, 15, 15, 60, 1),
  (2, '早班', 3, '06:00:00', '14:00:00', 480, 15, 15, 60, 1),
  (3, '晚班', 3, '14:00:00', '22:00:00', 480, 15, 15, 60, 1),
  (4, '夜班', 3, '22:00:00', '06:00:00', 480, 15, 15, 60, 1),
  (5, '弹性班', 2, '09:00:00', '18:00:00', 480, 0, 0, 60, 1);

UPDATE t_attendance_work_shift SET
  flex_start_earliest = '07:00:00',
  flex_start_latest = '10:00:00',
  flex_end_earliest = '16:00:00',
  flex_end_latest = '20:00:00'
WHERE shift_id = 5;

-- ============================================================
-- 插入区域设备关联
-- ============================================================
INSERT INTO t_area_device_relation (area_id, device_id, device_code, device_name, device_type, device_sub_type, business_module, relation_status, priority) VALUES
  (2, 'DEV001', 'ACCESS_CTRL_001', '主入口门禁', 1, 11, 'access', 1, 1),
  (4, 'DEV002', 'ACCESS_CTRL_002', '会议室门禁', 1, 11, 'access', 1, 2),
  (2, 'DEV003', 'ATTEND_001', '1楼考勤机', 2, 21, 'attendance', 1, 1),
  (2, 'DEV004', 'FACE_REC_001', '人脸识别机', 1, 12, 'access', 1, 2);

SELECT 'Test data inserted successfully' AS status;
SELECT COUNT(*) AS user_count FROM t_common_user;
SELECT COUNT(*) AS department_count FROM t_common_department;
SELECT COUNT(*) AS area_count FROM t_common_area;
SELECT COUNT(*) AS device_count FROM t_common_device;
SELECT COUNT(*) AS shift_count FROM t_attendance_work_shift;
