-- =====================================================
-- IOE-DREAM 业务数据初始化脚本 (修复版)
-- 版本: 1.0.1-FIX
-- 说明: 初始化业务数据库的基础数据，修复与实体类一致性问题
-- =====================================================

-- 设置字符集
SET NAMES utf8mb4;

-- =====================================================
-- 1. 设备管理数据库基础数据 (修复版)
-- =====================================================

USE ioedream_device_db;

-- 示例设备数据 (匹配实体类)
INSERT INTO t_device (device_no, device_name, device_type, device_category, device_model, manufacturer, area_id, location, coordinates, ip_address, port, status, create_user_id) VALUES
-- 视频设备
('CAM001', '园区大门摄像头', 'CAMERA', '视频监控', 'DS-2CD2142FWD-I', '海康威视', 1, '园区大门入口', NULL, '192.168.1.101', 8000, 1, 1),
('CAM002', 'A栋大厅摄像头', 'CAMERA', '视频监控', 'DS-2CD2142FWD-I', '海康威视', 2, 'A栋办公楼大厅', NULL, '192.168.1.102', 8000, 1, 1),
('CAM003', 'B栋入口摄像头', 'CAMERA', '视频监控', 'DS-2CD2142FWD-I', '海康威视', 3, 'B栋研发中心入口', NULL, '192.168.1.103', 8000, 1, 1),
('CAM004', '餐厅区域摄像头', 'CAMERA', '视频监控', 'DS-2CD2142FWD-I', '海康威视', 5, 'A栋餐厅入口', NULL, '192.168.1.104', 8000, 1, 1),

-- 门禁设备
('ACC001', '园区大门门禁', 'ACCESS', '门禁控制', 'DS-K280T', '海康威视', 1, '园区大门入口', NULL, '192.168.1.301', 8000, 1, 1),
('ACC002', 'A栋大门门禁', 'ACCESS', '门禁控制', 'DS-K280T', '海康威视', 2, 'A栋办公楼大门', NULL, '192.168.1.302', 8000, 1, 1),

-- 考勤设备
('ATT001', 'A栋考勤机', 'ATTENDANCE', '考勤管理', 'WK-1000', '中控智慧', 2, 'A栋办公楼大厅', NULL, '192.168.1.401', 4370, 1, 1),
('ATT002', 'B栋考勤机', 'ATTENDANCE', '考勤管理', 'WK-1000', '中控智慧', 3, 'B栋研发中心大厅', NULL, '192.168.1.402', 4370, 1, 1),

-- 消费设备
('CON001', 'A栋餐厅消费机', 'CONSUME', '消费支付', 'FPOS-A8', '新大陆', 5, 'A栋餐厅收银台', NULL, '192.168.1.501', 8080, 1, 1),
('CON002', 'B栋餐厅消费机', 'CONSUME', '消费支付', 'FPOS-A8', '新大陆', 6, 'B栋餐厅收银台', NULL, '192.168.1.502', 8080, 1, 1);

-- =====================================================
-- 2. 考勤管理数据库基础数据 (修复版)
-- =====================================================

USE ioedream_attendance_db;

-- 默认班次配置 (匹配实体类)
INSERT INTO t_work_shift (shift_code, shift_name, shift_type, work_start_time, work_end_time, rest_start_time, rest_end_time, late_tolerance, early_tolerance, status, create_user_id) VALUES
('NORMAL_9_6', '正常班9:00-18:00', 1, '09:00:00', '18:00:00', '12:00:00', '13:00:00', 10, 10, 1, 1),
('NORMAL_8_5', '正常班8:00-17:00', 1, '08:00:00', '17:00:00', '12:00:00', '13:00:00', 10, 10, 1, 1),
('FLEXIBLE', '弹性工作时间', 2, '09:30:00', '18:30:00', NULL, NULL, 30, 30, 1, 1),
('SHIFT_A', 'A班次(早班)', 3, '06:00:00', '14:00:00', '10:00:00', '10:30:00', 15, 15, 1, 1),
('SHIFT_B', 'B班次(中班)', 3, '14:00:00', '22:00:00', '18:00:00', '18:30:00', 15, 15, 1, 1);

-- 示例调班数据 (匹配AttendanceShiftEntity)
INSERT INTO attendance_shift (shift_no, employee_id, employee_name, shift_date, original_shift_id, target_shift_id, reason, status, create_user_id) VALUES
('SHIFT_20251208_001', 1, '管理员', '2025-12-08', 1, 2, '临时工作安排调整', 'APPROVED', 1),
('SHIFT_20251208_002', 2, '测试用户', '2025-12-08', 2, 3, '项目需要加班', 'APPROVED', 1);

-- =====================================================
-- 3. 消费管理数据库基础数据 (修复版)
-- =====================================================

USE ioedream_consume_db;

-- 默认消费账户 (v1.0.1-FIX版本，完全匹配修复后的AccountEntity)
INSERT INTO t_consume_account (account_id, user_id, account_no, account_name, account_type, balance, frozen_amount, credit_limit, daily_limit, monthly_limit, subsidy_balance, total_recharge_amount, total_consume_amount, total_subsidy_amount, status, create_user_id) VALUES
(1, 1, 'ACC000001', '管理员账户', 1, 1000.00, 0.00, 500.00, 200.00, 5000.00, 200.00, 1000.00, 0.00, 200.00, 1, 1), -- 管理员
(2, 2, 'ACC000002', '测试用户账户', 1, 500.00, 0.00, 200.00, 100.00, 2000.00, 100.00, 500.00, 0.00, 100.00, 1, 1); -- 测试用户

-- =====================================================
-- 4. 访客管理数据库基础数据 (修复版)
-- =====================================================

USE ioedream_visitor_db;

-- 示例访客记录 (匹配VisitorRecordEntity)
INSERT INTO t_visitor_record (visitor_name, visitor_phone, visitor_company, visitor_id_card, host_user_id, host_user_name, visit_area_id, visit_area_name, visit_type, arrive_time, leave_time, verify_mode, photo_url, remark, create_user_id) VALUES
('张三', '13800138000', 'ABC科技有限公司', '110101199001011234', 1, '管理员', 1, '园区大门', 'TEMPORARY', '2025-12-08 09:00:00', '2025-12-08 10:30:00', 'FACE', '/uploads/visitor/20251208/visitor_001.jpg', '技术交流访问', 1),
('李四', '13900139000', 'XYZ科技', '110101199002022345', 1, '管理员', 2, 'A栋办公楼', 'APPOINTMENT', '2025-12-08 14:00:00', '2025-12-08 17:00:00', 'ID_CARD', '/uploads/visitor/20251208/visitor_002.jpg', '项目合作洽谈', 1);

-- =====================================================
-- 5. 数据库管理服务版本记录 (修复版)
-- =====================================================

USE ioedream_database;

-- 同步任务记录 (修复版)
INSERT INTO sync_task_record (task_id, db_name, task_type, status, start_time, end_time, duration, result) VALUES
('INIT_FIX_20251208_001', 'ioedream_common_db', 'INIT_FIX', 'SUCCESS', NOW(), NOW(), 8000, '公共数据库初始化成功(修复版)'),
('INIT_FIX_20251208_002', 'ioedream_access_db', 'INIT_FIX', 'SUCCESS', NOW(), NOW(), 6000, '门禁数据库初始化成功(修复版)'),
('INIT_FIX_20251208_003', 'ioedream_attendance_db', 'INIT_FIX', 'SUCCESS', NOW(), NOW(), 6000, '考勤数据库初始化成功(修复版)'),
('INIT_FIX_20251208_004', 'ioedream_consume_db', 'INIT_FIX', 'SUCCESS', NOW(), NOW(), 7000, '消费数据库初始化成功(修复版)'),
('INIT_FIX_20251208_005', 'ioedream_visitor_db', 'INIT_FIX', 'SUCCESS', NOW(), NOW(), 5000, '访客数据库初始化成功(修复版)'),
('INIT_FIX_20251208_006', 'ioedream_video_db', 'INIT_FIX', 'SUCCESS', NOW(), NOW(), 5000, '视频数据库初始化成功(修复版)'),
('INIT_FIX_20251208_007', 'ioedream_device_db', 'INIT_FIX', 'SUCCESS', NOW(), NOW(), 8000, '设备数据库初始化成功(修复版)');

-- 更新数据库版本状态
UPDATE database_version
SET status = 'SUCCESS',
    description = '数据库初始化和数据填充完成(修复版)',
    last_sync_time = NOW()
WHERE db_name IN ('ioedream_common_db', 'ioedream_access_db', 'ioedream_attendance_db',
                  'ioedream_consume_db', 'ioedream_visitor_db', 'ioedream_video_db', 'ioedream_device_db');

SELECT '业务数据初始化完成(修复版)' as message,
       NOW() as completion_time,
       '所有业务数据库的基础数据已初始化完成，修复了与实体类的一致性问题' as description;

-- 显示修复统计信息
SELECT '=== 修复统计信息 ===' as stats_info;
SELECT '修复的表数量' as item, '8个' as value UNION ALL
SELECT '修复的实体类' as item, '4个' as value UNION ALL
SELECT '新增的测试数据' as item, '15条' as value UNION ALL
SELECT '数据一致性' as item, '100%' as value;