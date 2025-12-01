-- =====================================================
-- SmartAdmin v3 门禁系统模块测试数据初始化脚本
-- 创建时间: 2025-01-13
-- 说明: 插入门禁系统的测试数据，用于开发和测试
-- =====================================================

-- 设置SQL变量
SET @admin_user_id = 1;
SET @current_time = NOW();

-- =====================================================
-- 1. 插入门禁设备测试数据
-- =====================================================

INSERT INTO `smart_access_device` (
    `device_code`, `device_name`, `device_type`, `device_brand`, `device_model`,
    `area_id`, `area_name`, `location_desc`, `ip_address`, `port`,
    `mac_address`, `protocol_type`, `status`, `is_enabled`,
    `config_json`, `last_online_time`, `create_by`, `create_time`
) VALUES
-- 主入口门禁设备
('ACC001', '主入口门禁', 'door', '海康威视', 'DS-K2801', 1, '主楼大堂', '主楼一层大门入口', '192.168.1.101', 8000, 'AA:BB:CC:DD:EE:01', 'tcp', 1, 1,
 '{"timeout": 5000, "heartbeat_interval": 30, "retry_count": 3}', @current_time, @admin_user_id, @current_time),

-- 副入口门禁设备
('ACC002', '副入口门禁', 'door', '大华', 'DH-ASC1202C', 1, '主楼大堂', '主楼一层副门入口', '192.168.1.102', 8001, 'AA:BB:CC:DD:EE:02', 'tcp', 1, 1,
 '{"timeout": 5000, "heartbeat_interval": 30, "retry_count": 3}', @current_time, @admin_user_id, @current_time),

-- 地下车库入口闸机
('ACC003', '地下车库入口闸机', 'gate', '捷顺', 'JSMJ6180', 2, '地下车库', '地下一层车库入口', '192.168.1.103', 8002, 'AA:BB:CC:DD:EE:03', 'tcp', 1, 1,
 '{"timeout": 5000, "heartbeat_interval": 30, "retry_count": 3, "gate_type": "barrier"}', @current_time, @admin_user_id, @current_time),

-- 地下车库出口闸机
('ACC004', '地下车库出口闸机', 'gate', '捷顺', 'JSMJ6180', 2, '地下车库', '地下一层车库出口', '192.168.1.104', 8003, 'AA:BB:CC:DD:EE:04', 'tcp', 1, 1,
 '{"timeout": 5000, "heartbeat_interval": 30, "retry_count": 3, "gate_type": "barrier"}', @current_time, @admin_user_id, @current_time),

-- 办公区转闸
('ACC005', '办公区转闸', 'turnstile', '海康威视', 'DS-K6T8201', 3, '办公区', '主楼二层办公区入口', '192.168.1.105', 8004, 'AA:BB:CC:DD:EE:05', 'tcp', 1, 1,
 '{"timeout": 5000, "heartbeat_interval": 30, "retry_count": 3, "turnstile_type": "tripod"}', @current_time, @admin_user_id, @current_time),

-- 研发部门禁
('ACC006', '研发部门禁', 'door', '大华', 'DH-ASC1202C', 4, '研发部', '主楼三层研发部入口', '192.168.1.106', 8005, 'AA:BB:CC:DD:EE:06', 'tcp', 0, 1,
 '{"timeout": 5000, "heartbeat_interval": 30, "retry_count": 3}', DATE_SUB(@current_time, INTERVAL 1 HOUR), @admin_user_id, @current_time),

-- 服务器机房门禁
('ACC007', '服务器机房门禁', 'door', '海康威视', 'DS-K2801', 5, '服务器机房', '主楼四层服务器机房', '192.168.1.107', 8006, 'AA:BB:CC:DD:EE:07', 'tcp', 2, 1,
 '{"timeout": 5000, "heartbeat_interval": 30, "retry_count": 3, "security_level": "high"}', DATE_SUB(@current_time, INTERVAL 2 HOUR), @admin_user_id, @current_time);

-- =====================================================
-- 2. 插入设备状态数据
-- =====================================================

INSERT INTO `smart_access_device_status` (
    `device_id`, `door_status`, `lock_status`, `power_status`, `network_status`,
    `last_heartbeat`, `cpu_usage`, `memory_usage`, `storage_usage`,
    `temperature`, `error_count`, `create_time`
) VALUES
-- 主入口门禁状态
(1, 'closed', 'locked', 'normal', 'online', @current_time, 15.5, 32.8, 45.2, 38.5, 0, @current_time),

-- 副入口门禁状态
(2, 'closed', 'locked', 'normal', 'online', @current_time, 18.2, 35.6, 48.1, 39.1, 0, @current_time),

-- 地下车库入口闸机状态
(3, 'closed', 'locked', 'normal', 'online', @current_time, 12.3, 28.9, 35.7, 36.8, 0, @current_time),

-- 地下车库出口闸机状态
(4, 'closed', 'locked', 'normal', 'online', @current_time, 11.8, 27.4, 33.2, 37.2, 0, @current_time),

-- 办公区转闸状态
(5, 'closed', 'locked', 'normal', 'online', @current_time, 20.1, 38.5, 52.3, 40.1, 0, @current_time),

-- 研发部门禁状态（离线）
(6, 'unknown', 'unknown', 'offline', 'offline', DATE_SUB(@current_time, INTERVAL 1 HOUR), 0, 0, 0, 0, 5, @current_time),

-- 服务器机房门禁状态（故障）
(7, 'open', 'unlocked', 'low', 'online', DATE_SUB(@current_time, INTERVAL 2 HOUR), 45.8, 68.9, 78.5, 55.2, 12, @current_time);

-- =====================================================
-- 3. 插入区域权限组数据
-- =====================================================

INSERT INTO `smart_access_area_group` (
    `group_name`, `group_code`, `description`, `area_ids`, `device_ids`,
    `default_start_time`, `default_end_time`, `default_weekdays`,
    `status`, `create_by`, `create_time`
) VALUES
-- 全区域权限组
('全区域权限组', 'ALL_AREA', '可访问所有区域的权限组', '1,2,3,4,5', '1,2,3,4,5,6,7', '08:00:00', '22:00:00', '1,2,3,4,5', 1, @admin_user_id, @current_time),

-- 办公区域权限组
('办公区域权限组', 'OFFICE_AREA', '仅可访问办公区域的权限组', '1,3', '1,2,5', '09:00:00', '18:00:00', '1,2,3,4,5', 1, @admin_user_id, @current_time),

-- 研发区域权限组
('研发区域权限组', 'DEV_AREA', '仅可访问研发区域的权限组', '4', '6', '09:00:00', '21:00:00', '1,2,3,4,5', 1, @admin_user_id, @current_time),

-- 机房权限组
('机房权限组', 'SERVER_ROOM', '可访问服务器机房的权限组', '5', '7', '08:00:00', '20:00:00', '1,2,3,4,5', 1, @admin_user_id, @current_time),

-- 临时访客权限组
('临时访客权限组', 'TEMP_VISITOR', '临时访客权限组', '1', '1,2', '09:00:00', '17:00:00', '1,2,3,4,5', 1, @admin_user_id, @current_time);

-- =====================================================
-- 4. 插入门禁权限测试数据
-- =====================================================

INSERT INTO `smart_access_permission` (
    `person_id`, `person_name`, `person_type`, `device_id`, `device_name`,
    `area_id`, `area_name`, `permission_type`, `start_time`, `end_time`,
    `monday_access`, `tuesday_access`, `wednesday_access`, `thursday_access`,
    `friday_access`, `saturday_access`, `sunday_access`, `time_config`,
    `status`, `business_code`, `create_by`, `create_time`
) VALUES
-- 管理员权限（全区域）
(1, '系统管理员', 'employee', 1, '主入口门禁', 1, '主楼大堂', 'permanent', '2024-01-01 00:00:00', '2030-12-31 23:59:59', 1, 1, 1, 1, 1, 1, 1,
 '[{"start_time": "00:00", "end_time": "23:59"}]', 1, 'ACCESS_AUTH_1642051200000', @admin_user_id, @current_time),

(1, '系统管理员', 'employee', 2, '副入口门禁', 1, '主楼大堂', 'permanent', '2024-01-01 00:00:00', '2030-12-31 23:59:59', 1, 1, 1, 1, 1, 1, 1,
 '[{"start_time": "00:00", "end_time": "23:59"}]', 1, 'ACCESS_AUTH_1642051200001', @admin_user_id, @current_time),

(1, '系统管理员', 'employee', 3, '地下车库入口闸机', 2, '地下车库', 'permanent', '2024-01-01 00:00:00', '2030-12-31 23:59:59', 1, 1, 1, 1, 1, 1, 1,
 '[{"start_time": "00:00", "end_time": "23:59"}]', 1, 'ACCESS_AUTH_1642051200002', @admin_user_id, @current_time),

(1, '系统管理员', 'employee', 4, '地下车库出口闸机', 2, '地下车库', 'permanent', '2024-01-01 00:00:00', '2030-12-31 23:59:59', 1, 1, 1, 1, 1, 1, 1,
 '[{"start_time": "00:00", "end_time": "23:59"}]', 1, 'ACCESS_AUTH_1642051200003', @admin_user_id, @current_time),

(1, '系统管理员', 'employee', 5, '办公区转闸', 3, '办公区', 'permanent', '2024-01-01 00:00:00', '2030-12-31 23:59:59', 1, 1, 1, 1, 1, 1, 1,
 '[{"start_time": "00:00", "end_time": "23:59"}]', 1, 'ACCESS_AUTH_1642051200004', @admin_user_id, @current_time),

(1, '系统管理员', 'employee', 6, '研发部门禁', 4, '研发部', 'permanent', '2024-01-01 00:00:00', '2030-12-31 23:59:59', 1, 1, 1, 1, 1, 1, 1,
 '[{"start_time": "00:00", "end_time": "23:59"}]', 1, 'ACCESS_AUTH_1642051200005', @admin_user_id, @current_time),

(1, '系统管理员', 'employee', 7, '服务器机房门禁', 5, '服务器机房', 'permanent', '2024-01-01 00:00:00', '2030-12-31 23:59:59', 1, 1, 1, 1, 1, 1, 1,
 '[{"start_time": "00:00", "end_time": "23:59"}]', 1, 'ACCESS_AUTH_1642051200006', @admin_user_id, @current_time),

-- 普通员工权限（办公区域）
(2, '张三', 'employee', 1, '主入口门禁', 1, '主楼大堂', 'permanent', '2024-01-01 00:00:00', '2025-12-31 23:59:59', 1, 1, 1, 1, 1, 0, 0,
 '[{"start_time": "08:30", "end_time": "18:30"}]', 1, 'ACCESS_AUTH_1642051200010', @admin_user_id, @current_time),

(2, '张三', 'employee', 2, '副入口门禁', 1, '主楼大堂', 'permanent', '2024-01-01 00:00:00', '2025-12-31 23:59:59', 1, 1, 1, 1, 1, 0, 0,
 '[{"start_time": "08:30", "end_time": "18:30"}]', 1, 'ACCESS_AUTH_1642051200011', @admin_user_id, @current_time),

(2, '张三', 'employee', 5, '办公区转闸', 3, '办公区', 'permanent', '2024-01-01 00:00:00', '2025-12-31 23:59:59', 1, 1, 1, 1, 1, 0, 0,
 '[{"start_time": "08:30", "end_time": "18:30"}]', 1, 'ACCESS_AUTH_1642051200012', @admin_user_id, @current_time),

-- 研发人员权限（包括研发区域）
(3, '李四', 'employee', 1, '主入口门禁', 1, '主楼大堂', 'permanent', '2024-01-01 00:00:00', '2025-12-31 23:59:59', 1, 1, 1, 1, 1, 0, 0,
 '[{"start_time": "08:00", "end_time": "21:00"}]', 1, 'ACCESS_AUTH_1642051200020', @admin_user_id, @current_time),

(3, '李四', 'employee', 5, '办公区转闸', 3, '办公区', 'permanent', '2024-01-01 00:00:00', '2025-12-31 23:59:59', 1, 1, 1, 1, 1, 0, 0,
 '[{"start_time": "08:00", "end_time": "21:00"}]', 1, 'ACCESS_AUTH_1642051200021', @admin_user_id, @current_time),

(3, '李四', 'employee', 6, '研发部门禁', 4, '研发部', 'permanent', '2024-01-01 00:00:00', '2025-12-31 23:59:59', 1, 1, 1, 1, 1, 0, 0,
 '[{"start_time": "08:00", "end_time": "21:00"}]', 1, 'ACCESS_AUTH_1642051200022', @admin_user_id, @current_time),

-- 临时访客权限
(4, '王五', 'visitor', 1, '主入口门禁', 1, '主楼大堂', 'temporary', @current_time, DATE_ADD(@current_time, INTERVAL 1 DAY), 1, 1, 1, 1, 1, 0, 0,
 '[{"start_time": "09:00", "end_time": "17:00"}]', 2, 'ACCESS_AUTH_1642051200030', @admin_user_id, @current_time),

(4, '王五', 'visitor', 2, '副入口门禁', 1, '主楼大堂', 'temporary', @current_time, DATE_ADD(@current_time, INTERVAL 1 DAY), 1, 1, 1, 1, 1, 0, 0,
 '[{"start_time": "09:00", "end_time": "17:00"}]', 2, 'ACCESS_AUTH_1642051200031', @admin_user_id, @current_time),

-- 承包商权限（地下车库）
(5, '赵六', 'contractor', 3, '地下车库入口闸机', 2, '地下车库', 'scheduled', @current_time, DATE_ADD(@current_time, INTERVAL 30 DAY), 1, 1, 1, 1, 1, 1, 0,
 '[{"start_time": "06:00", "end_time": "20:00"}]', 1, 'ACCESS_AUTH_1642051200040', @admin_user_id, @current_time),

(5, '赵六', 'contractor', 4, '地下车库出口闸机', 2, '地下车库', 'scheduled', @current_time, DATE_ADD(@current_time, INTERVAL 30 DAY), 1, 1, 1, 1, 1, 1, 0,
 '[{"start_time": "06:00", "end_time": "20:00"}]', 1, 'ACCESS_AUTH_1642051200041', @admin_user_id, @current_time);

-- =====================================================
-- 5. 插入通行记录测试数据
-- =====================================================

INSERT INTO `smart_access_record` (
    `device_id`, `device_name`, `device_code`, `person_id`, `person_name`,
    `person_type`, `card_number`, `face_image_url`, `access_result`,
    `fail_reason`, `direction`, `access_time`, `temperature`,
    `mask_detected`, `image_urls`, `event_type`, `is_abnormal`
) VALUES
-- 正常通行记录
(1, '主入口门禁', 'ACC001', 1, '系统管理员', 'employee', '1000001', '/images/face/admin_001.jpg', 1, NULL, 'in', DATE_SUB(@current_time, INTERVAL 30 MINUTE), 36.5, 1,
 '["/images/record/access_001_1.jpg", "/images/record/access_001_2.jpg"]', 'normal', 0),

(1, '主入口门禁', 'ACC001', 2, '张三', 'employee', '1000002', '/images/face/zhangsan_001.jpg', 1, NULL, 'in', DATE_SUB(@current_time, INTERVAL 25 MINUTE), 36.8, 1,
 '["/images/record/access_002_1.jpg"]', 'normal', 0),

(2, '副入口门禁', 'ACC002', 3, '李四', 'employee', '1000003', '/images/face/lisi_001.jpg', 1, NULL, 'in', DATE_SUB(@current_time, INTERVAL 20 MINUTE), 37.1, 0,
 '["/images/record/access_003_1.jpg"]', 'normal', 0),

(5, '办公区转闸', 'ACC005', 2, '张三', 'employee', '1000002', '/images/face/zhangsan_002.jpg', 1, NULL, 'in', DATE_SUB(@current_time, INTERVAL 15 MINUTE), 36.9, 1,
 '["/images/record/access_004_1.jpg"]', 'normal', 0),

(6, '研发部门禁', 'ACC006', 3, '李四', 'employee', '1000003', '/images/face/lisi_002.jpg', 1, NULL, 'in', DATE_SUB(@current_time, INTERVAL 10 MINUTE), 36.7, 1,
 '["/images/record/access_005_1.jpg"]', 'normal', 0),

-- 异常通行记录
(1, '主入口门禁', 'ACC001', 999, '未知人员', 'unknown', '9999999', NULL, 0, '权限验证失败', 'in', DATE_SUB(@current_time, INTERVAL 5 MINUTE), 37.2, 0,
 '["/images/record/access_006_1.jpg"]', 'abnormal', 1),

(7, '服务器机房门禁', 'ACC007', 4, '王五', 'visitor', '1000004', '/images/face/wangwu_001.jpg', 0, '权限不足', 'in', DATE_SUB(@current_time, INTERVAL 3 MINUTE), 36.8, 1,
 '["/images/record/access_007_1.jpg"]', 'abnormal', 1),

-- 强制开门记录
(2, '副入口门禁', 'ACC002', NULL, '系统', 'system', NULL, NULL, 1, NULL, 'in', DATE_SUB(@current_time, INTERVAL 1 MINUTE), NULL, NULL,
 '["/images/record/access_008_1.jpg"]', 'forced', 1);

-- =====================================================
-- 6. 插入设备指令测试数据
-- =====================================================

INSERT INTO `smart_access_command` (
    `device_id`, `command_type`, `command_content`, `command_status`,
    `send_time`, `execute_time`, `response_content`, `error_message`,
    `retry_count`, `create_by`, `create_time`
) VALUES
-- 成功执行的指令
(1, 'open', '{"user_id": 1, "reason": "远程开门测试"}', 2,
 DATE_SUB(@current_time, INTERVAL 10 MINUTE), DATE_SUB(@current_time, INTERVAL 10 MINUTE),
 '{"status": "success", "message": "开门成功"}', NULL, 0, @admin_user_id, DATE_SUB(@current_time, INTERVAL 10 MINUTE)),

(2, 'close', '{"user_id": 1, "reason": "远程关门测试"}', 2,
 DATE_SUB(@current_time, INTERVAL 8 MINUTE), DATE_SUB(@current_time, INTERVAL 8 MINUTE),
 '{"status": "success", "message": "关门成功"}', NULL, 0, @admin_user_id, DATE_SUB(@current_time, INTERVAL 8 MINUTE)),

(3, 'restart', '{"user_id": 1, "reason": "设备重启测试"}', 2,
 DATE_SUB(@current_time, INTERVAL 6 MINUTE), DATE_SUB(@current_time, INTERVAL 6 MINUTE),
 '{"status": "success", "message": "重启成功"}', NULL, 0, @admin_user_id, DATE_SUB(@current_time, INTERVAL 6 MINUTE)),

(4, 'sync', '{"user_id": 1, "time": "2025-01-13 10:00:00"}', 2,
 DATE_SUB(@current_time, INTERVAL 4 MINUTE), DATE_SUB(@current_time, INTERVAL 4 MINUTE),
 '{"status": "success", "message": "时间同步成功"}', NULL, 0, @admin_user_id, DATE_SUB(@current_time, INTERVAL 4 MINUTE)),

-- 执行失败的指令
(6, 'open', '{"user_id": 1, "reason": "离线设备测试"}', 3,
 DATE_SUB(@current_time, INTERVAL 2 MINUTE), NULL,
 NULL, '设备离线，无法发送指令', 3, @admin_user_id, DATE_SUB(@current_time, INTERVAL 2 MINUTE)),

-- 待发送的指令
(7, 'open', '{"user_id": 1, "reason": "故障设备测试"}', 0,
 NULL, NULL, NULL, NULL, 0, @admin_user_id, @current_time);

-- =====================================================
-- 7. 插入设备告警测试数据
-- =====================================================

INSERT INTO `smart_access_alarm` (
    `device_id`, `alarm_type`, `alarm_level`, `alarm_title`, `alarm_content`,
    `alarm_time`, `record_id`, `person_id`, `person_name`,
    `is_handled`, `handled_by`, `handled_time`, `handle_remark`
) VALUES
-- 设备离线告警
(6, 'offline', 'high', '设备离线告警', '研发部门禁设备已离线超过30分钟', DATE_SUB(@current_time, INTERVAL 1 HOUR), NULL, NULL, NULL,
 1, @admin_user_id, DATE_SUB(@current_time, INTERVAL 30 MINUTE), '已通知运维人员处理'),

-- 设备故障告警
(7, 'error', 'critical', '设备故障告警', '服务器机房门禁设备出现硬件故障', DATE_SUB(@current_time, INTERVAL 2 HOUR), NULL, NULL, NULL,
 0, NULL, NULL, NULL),

-- 强制开门告警
(2, 'forced', 'medium', '强制开门告警', '副入口门禁被强制开门', DATE_SUB(@current_time, INTERVAL 1 MINUTE), 8, NULL, '系统',
 0, NULL, NULL, NULL),

-- 低电量告警
(7, 'low_power', 'medium', '低电量告警', '服务器机房门禁设备电量低于20%', DATE_SUB(@current_time, INTERVAL 30 MINUTE), NULL, NULL, NULL,
 1, @admin_user_id, DATE_SUB(@current_time, INTERVAL 10 MINUTE), '已安排更换电池'),

-- 门超时未关告警
(7, 'door_open', 'low', '门超时未关告警', '服务器机房门开门超过5分钟未关闭', DATE_SUB(@current_time, INTERVAL 45 MINUTE), NULL, NULL, NULL,
 1, @admin_user_id, DATE_SUB(@current_time, INTERVAL 20 MINUTE), '已检查确认安全');

-- =====================================================
-- 8. 更新权限状态为已审批
-- =====================================================

UPDATE `smart_access_permission`
SET `status` = 1,
    `approve_user_id` = @admin_user_id,
    `approve_time` = @current_time,
    `approve_remark` = '系统管理员审批通过'
WHERE `status` = 1 AND `person_type` IN ('employee', 'contractor');

-- 审批通过管理员权限
UPDATE `smart_access_permission`
SET `status` = 1,
    `approve_user_id` = @admin_user_id,
    `approve_time` = @current_time,
    `approve_remark` = '超级管理员权限自动审批'
WHERE `person_id` = 1;

-- 访客权限保持待审批状态
UPDATE `smart_access_permission`
SET `status` = 2,
    `approve_remark` = '临时访客权限，需要人工审批'
WHERE `person_type` = 'visitor';

-- =====================================================
-- 初始化完成
-- =====================================================

-- 输出初始化完成信息
SELECT 'SmartAccess 模块测试数据初始化完成！' AS message,
       COUNT(*) AS device_count,
       @current_time AS init_time
FROM `smart_access_device`;