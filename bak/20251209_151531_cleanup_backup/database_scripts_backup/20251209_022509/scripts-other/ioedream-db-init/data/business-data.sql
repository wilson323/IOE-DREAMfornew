-- =====================================================
-- IOE-DREAM 业务数据初始化脚本
-- 版本: 1.0.0
-- 说明: 初始化业务数据库的基础数据
-- =====================================================

-- 设置字符集
SET NAMES utf8mb4;

-- =====================================================
-- 1. 设备管理数据库基础数据
-- =====================================================

USE ioedream_device_db;

-- 示例设备数据
INSERT INTO t_device (device_no, device_name, device_type, device_category, device_model, manufacturer, area_id, location, ip_address, port, status, create_user_id) VALUES
-- 视频设备
('CAM001', '园区大门摄像头', 'CAMERA', '视频监控', 'DS-2CD2142FWD-I', '海康威视', 1, '园区大门入口', '192.168.1.101', 8000, 1, 1),
('CAM002', 'A栋大厅摄像头', 'CAMERA', '视频监控', 'DS-2CD2142FWD-I', '海康威视', 2, 'A栋办公楼大厅', '192.168.1.102', 8000, 1, 1),
('CAM003', 'B栋入口摄像头', 'CAMERA', '视频监控', 'DS-2CD2142FWD-I', '海康威视', 3, 'B栋研发中心入口', '192.168.1.103', 8000, 1, 1),
('CAM004', '餐厅区域摄像头', 'CAMERA', '视频监控', 'DS-2CD2142FWD-I', '海康威视', 5, 'A栋餐厅入口', '192.168.1.104', 8000, 1, 1),
('CAM005', '停车场摄像头', 'CAMERA', '视频监控', 'DS-2CD2142FWD-I', '海康威视', 7, 'A区停车场', '192.168.1.105', 8000, 1, 1),

-- NVR设备
('NVR001', '园区NVR录像机', 'NVR', '视频存储', 'DS-7816N-K2/16P', '海康威视', 1, '监控中心机房', '192.168.1.201', 37777, 1, 1),

-- 门禁设备
('ACC001', '园区大门门禁', 'ACCESS', '门禁控制', 'DS-K280T', '海康威视', 1, '园区大门入口', '192.168.1.301', 8000, 1, 1),
('ACC002', 'A栋大门门禁', 'ACCESS', '门禁控制', 'DS-K280T', '海康威视', 2, 'A栋办公楼大门', '192.168.1.302', 8000, 1, 1),
('ACC003', 'B栋门禁', 'ACCESS', '门禁控制', 'DS-K280T', '海康威视', 3, 'B栋研发中心入口', '192.168.1.303', 8000, 1, 1),

-- 考勤设备
('ATT001', 'A栋考勤机', 'ATTENDANCE', '考勤管理', 'WK-1000', '中控智慧', 2, 'A栋办公楼大厅', '192.168.1.401', 4370, 1, 1),
('ATT002', 'B栋考勤机', 'ATTENDANCE', '考勤管理', 'WK-1000', '中控智慧', 3, 'B栋研发中心大厅', '192.168.1.402', 4370, 1, 1),

-- 消费设备
('CON001', 'A栋餐厅消费机', 'CONSUME', '消费支付', 'FPOS-A8', '新大陆', 5, 'A栋餐厅收银台', '192.168.1.501', 8080, 1, 1),
('CON002', 'B栋餐厅消费机', 'CONSUME', '消费支付', 'FPOS-A8', '新大陆', 6, 'B栋餐厅收银台', '192.168.1.502', 8080, 1, 1),
('CON003', '园区超市消费机', 'CONSUME', '消费支付', 'FPOS-A8', '新大陆', 9, '园区超市收银台', '192.168.1.503', 8080, 1, 1);

-- =====================================================
-- 2. 门禁管理数据库基础数据
-- =====================================================

USE ioedream_access_db;

-- 门禁权限配置（示例数据）
INSERT INTO t_access_permission (user_id, area_id, permission_type, valid_start_time, valid_end_time, status, create_user_id) VALUES
(1, 1, 'ALL', '2025-01-01 00:00:00', '2030-12-31 23:59:59', 1, 1), -- 管理员全区域权限
(2, 2, 'TIME', '2025-01-01 07:00:00', '2025-12-31 20:00:00', 1, 1), -- 普通用户A栋权限
(2, 3, 'TIME', '2025-01-01 08:00:00', '2025-12-31 19:00:00', 1, 1); -- 普通用户B栋权限

-- =====================================================
-- 3. 考勤管理数据库基础数据
-- =====================================================

USE ioedream_attendance_db;

-- 默认班次配置
INSERT INTO t_work_shift (shift_code, shift_name, shift_type, work_start_time, work_end_time, rest_start_time, rest_end_time, late_tolerance, early_tolerance, status, create_user_id) VALUES
('NORMAL_9_6', '正常班9:00-18:00', 1, '09:00:00', '18:00:00', '12:00:00', '13:00:00', 10, 10, 1, 1),
('NORMAL_8_5', '正常班8:00-17:00', 1, '08:00:00', '17:00:00', '12:00:00', '13:00:00', 10, 10, 1, 1),
('FLEXIBLE', '弹性工作时间', 2, '09:30:00', '18:30:00', NULL, NULL, 30, 30, 1, 1),
('SHIFT_A', 'A班次(早班)', 3, '06:00:00', '14:00:00', '10:00:00', '10:30:00', 15, 15, 1, 1),
('SHIFT_B', 'B班次(中班)', 3, '14:00:00', '22:00:00', '18:00:00', '18:30:00', 15, 15, 1, 1),
('SHIFT_C', 'C班次(夜班)', 3, '22:00:00', '06:00:00', '02:00:00', '02:30:00', 15, 15, 1, 1);

-- =====================================================
-- 4. 消费管理数据库基础数据
-- =====================================================

USE ioedream_consume_db;

-- 默认消费账户（为已有用户创建）
INSERT INTO t_consume_account (user_id, account_no, account_name, account_type, balance, daily_limit, monthly_limit, status, create_user_id) VALUES
(1, 'ACC000001', '管理员账户', 1, 1000.00, 200.00, 5000.00, 1, 1), -- 管理员
(2, 'ACC000002', '测试用户账户', 1, 500.00, 100.00, 2000.00, 1, 1); -- 测试用户

-- =====================================================
-- 5. 视频监控数据库基础数据
-- =====================================================

USE ioedream_video_db;

-- 视频设备配置（从设备库同步或配置）
INSERT INTO t_video_device (device_id, device_no, device_name, device_type, area_id, location, ip_address, port, username, password, rtsp_url, status, create_user_id) VALUES
(1, 'CAM001', '园区大门摄像头', 'CAMERA', 1, '园区大门入口', '192.168.1.101', 8000, 'admin', 'admin123', 'rtsp://192.168.1.101:554/stream1', 1, 1),
(2, 'CAM002', 'A栋大厅摄像头', 'CAMERA', 2, 'A栋办公楼大厅', '192.168.1.102', 8000, 'admin', 'admin123', 'rtsp://192.168.1.102:554/stream1', 1, 1),
(3, 'CAM003', 'B栋入口摄像头', 'CAMERA', 3, 'B栋研发中心入口', '192.168.1.103', 8000, 'admin', 'admin123', 'rtsp://192.168.1.103:554/stream1', 1, 1),
(4, 'CAM004', '餐厅区域摄像头', 'CAMERA', 5, 'A栋餐厅入口', '192.168.1.104', 8000, 'admin', 'admin123', 'rtsp://192.168.1.104:554/stream1', 1, 1),
(5, 'CAM005', '停车场摄像头', 'CAMERA', 7, 'A区停车场', '192.168.1.105', 8000, 'admin', 'admin123', 'rtsp://192.168.1.105:554/stream1', 1, 1);

-- =====================================================
-- 6. 数据库管理服务版本记录
-- =====================================================

USE ioedream_database;

-- 同步任务记录
INSERT INTO sync_task_record (task_id, db_name, task_type, status, start_time, end_time, duration, result) VALUES
('INIT_20251208_001', 'ioedream_common_db', 'INIT', 'SUCCESS', NOW(), NOW(), 5000, '公共数据库初始化成功'),
('INIT_20251208_002', 'ioedream_access_db', 'INIT', 'SUCCESS', NOW(), NOW(), 3000, '门禁数据库初始化成功'),
('INIT_20251208_003', 'ioedream_attendance_db', 'INIT', 'SUCCESS', NOW(), NOW(), 3000, '考勤数据库初始化成功'),
('INIT_20251208_004', 'ioedream_consume_db', 'INIT', 'SUCCESS', NOW(), NOW(), 3000, '消费数据库初始化成功'),
('INIT_20251208_005', 'ioedream_visitor_db', 'INIT', 'SUCCESS', NOW(), NOW(), 2000, '访客数据库初始化成功'),
('INIT_20251208_006', 'ioedream_video_db', 'INIT', 'SUCCESS', NOW(), NOW(), 3000, '视频数据库初始化成功'),
('INIT_20251208_007', 'ioedream_device_db', 'INIT', 'SUCCESS', NOW(), NOW(), 4000, '设备数据库初始化成功');

-- 更新数据库版本状态
UPDATE database_version
SET status = 'SUCCESS',
    description = '数据库初始化和数据填充完成',
    last_sync_time = NOW()
WHERE db_name IN ('ioedream_common_db', 'ioedream_access_db', 'ioedream_attendance_db',
                  'ioedream_consume_db', 'ioedream_visitor_db', 'ioedream_video_db', 'ioedream_device_db');

SELECT '业务数据初始化完成' as message,
       NOW() as completion_time,
       '所有业务数据库的基础数据已初始化完成' as description;