-- =====================================================
-- IOE-DREAM 公共数据初始化脚本
-- 版本: 1.0.0
-- 说明: 初始化公共数据库的基础数据
-- =====================================================

USE ioedream_common_db;

-- 1. 默认用户数据
INSERT INTO t_user (user_no, username, real_name, phone, email, password, salt, status, create_user_id) VALUES
('admin', 'admin', '系统管理员', '13800138000', 'admin@ioedream.com', '$2a$10$EkI2jYa3dZ5KdJ/ZB5N3PehkwZsH5I4lNuO8KjRMKqMqMj8H.F9Xe', 'admin123', 1, 1),
('test001', 'test001', '测试用户', '13900139000', 'test@ioedream.com', '$2a$10$EkI2jYa3dZ5KdJ/ZB5N3PehkwZsH5I4lNuO8KjRMKqMqMj8H.F9Xe', 'test123', 1, 1);

-- 2. 默认角色数据
INSERT INTO t_role (role_code, role_name, description, sort_order, create_user_id) VALUES
('SUPER_ADMIN', '超级管理员', '系统超级管理员，拥有所有权限', 1, 1),
('ADMIN', '管理员', '系统管理员，拥有管理权限', 2, 1),
('USER', '普通用户', '普通用户，基础使用权限', 3, 1),
('ACCESS_ADMIN', '门禁管理员', '门禁系统管理员', 4, 1),
('ATTENDANCE_ADMIN', '考勤管理员', '考勤系统管理员', 5, 1),
('CONSUME_ADMIN', '消费管理员', '消费系统管理员', 6, 1),
('VISITOR_ADMIN', '访客管理员', '访客系统管理员', 7, 1),
('VIDEO_ADMIN', '视频管理员', '视频系统管理员', 8, 1);

-- 3. 默认权限数据
INSERT INTO t_permission (permission_code, permission_name, description, menu_url, menu_icon, parent_id, permission_type, sort_order) VALUES
-- 系统管理权限
('SYSTEM', '系统管理', '系统管理模块', '', 'system', 0, 1, 1),
('SYSTEM:USER', '用户管理', '用户管理页面', '/system/user', 'user', 1, 1, 1),
('SYSTEM:ROLE', '角色管理', '角色管理页面', '/system/role', 'role', 1, 1, 2),
('SYSTEM:PERMISSION', '权限管理', '权限管理页面', '/system/permission', 'permission', 1, 1, 3),
('SYSTEM:DEPARTMENT', '部门管理', '部门管理页面', '/system/department', 'department', 1, 1, 4),
('SYSTEM:AREA', '区域管理', '区域管理页面', '/system/area', 'area', 1, 1, 5),
('SYSTEM:DICT', '字典管理', '字典管理页面', '/system/dict', 'dict', 1, 1, 6),

-- 门禁管理权限
('ACCESS', '门禁管理', '门禁管理模块', '', 'access', 0, 1, 2),
('ACCESS:DEVICE', '门禁设备', '门禁设备管理', '/access/device', 'device', 9, 1, 1),
('ACCESS:RECORD', '通行记录', '门禁通行记录查询', '/access/record', 'record', 9, 1, 2),
('ACCESS:PERMISSION', '门禁权限', '门禁权限管理', '/access/permission', 'permission', 9, 1, 3),
('ACCESS:MONITOR', '实时监控', '门禁实时监控', '/access/monitor', 'monitor', 9, 1, 4),

-- 考勤管理权限
('ATTENDANCE', '考勤管理', '考勤管理模块', '', 'attendance', 0, 1, 3),
('ATTENDANCE:RECORD', '考勤记录', '考勤记录查询', '/attendance/record', 'clock', 10, 1, 1),
('ATTENDANCE:SCHEDULE', '排班管理', '排班管理页面', '/attendance/schedule', 'schedule', 10, 1, 2),
('ATTENDANCE:SHIFT', '班次管理', '班次管理页面', '/attendance/shift', 'shift', 10, 1, 3),
('ATTENDANCE:LEAVE', '请假管理', '请假管理页面', '/attendance/leave', 'leave', 10, 1, 4),

-- 消费管理权限
('CONSUME', '消费管理', '消费管理模块', '', 'consume', 0, 1, 4),
('CONSUME:ACCOUNT', '账户管理', '消费账户管理', '/consume/account', 'account', 15, 1, 1),
('CONSUME:RECORD', '消费记录', '消费记录查询', '/consume/record', 'payment', 15, 1, 2),
('CONSUME:DEVICE', '消费设备', '消费设备管理', '/consume/device', 'device', 15, 1, 3),
('CONSUME:RECHARGE', '充值管理', '充值管理页面', '/consume/recharge', 'recharge', 15, 1, 4),
('CONSUME:SUBSIDY', '补贴管理', '补贴管理页面', '/consume/subsidy', 'subsidy', 15, 1, 5),

-- 访客管理权限
('VISITOR', '访客管理', '访客管理模块', '', 'visitor', 0, 1, 5),
('VISITOR:APPOINTMENT', '访客预约', '访客预约管理', '/visitor/appointment', 'appointment', 18, 1, 1),
('VISITOR:RECORD', '访客记录', '访客记录查询', '/visitor/record', 'visitor', 18, 1, 2),
('VISITOR:PERMISSION', '访客权限', '访客权限管理', '/visitor/permission', 'permission', 18, 1, 3),

-- 视频监控权限
('VIDEO', '视频监控', '视频监控模块', '', 'video', 0, 1, 6),
('VIDEO:DEVICE', '视频设备', '视频设备管理', '/video/device', 'video-camera', 21, 1, 1),
('VIDEO:LIVE', '实时监控', '实时监控页面', '/video/live', 'video', 21, 1, 2),
('VIDEO:RECORD', '录像回放', '录像回放页面', '/video/record', 'record', 21, 1, 3),
('VIDEO:ALARM', '报警管理', '报警管理页面', '/video/alarm', 'alarm', 21, 1, 4),

-- 设备管理权限
('DEVICE', '设备管理', '设备管理模块', '', 'device', 0, 1, 7),
('DEVICE:LIST', '设备列表', '设备列表页面', '/device/list', 'devices', 24, 1, 1),
('DEVICE:MAINTENANCE', '设备维护', '设备维护管理', '/device/maintenance', 'maintenance', 24, 1, 2),
('DEVICE:MONITOR', '设备监控', '设备监控页面', '/device/monitor', 'monitor', 24, 1, 3),

-- 数据统计权限
('REPORT', '数据统计', '数据统计模块', '', 'chart', 0, 1, 8),
('REPORT:ACCESS', '门禁统计', '门禁数据统计', '/report/access', 'chart', 27, 1, 1),
('REPORT:ATTENDANCE', '考勤统计', '考勤数据统计', '/report/attendance', 'chart', 27, 1, 2),
('REPORT:CONSUME', '消费统计', '消费数据统计', '/report/consume', 'chart', 27, 1, 3),
('REPORT:VISITOR', '访客统计', '访客数据统计', '/report/visitor', 'chart', 27, 1, 4);

-- 4. 用户角色关联
INSERT INTO t_user_role (user_id, role_id, create_user_id) VALUES
(1, 1, 1), -- admin - 超级管理员
(2, 3, 1); -- test001 - 普通用户

-- 5. 角色权限关联 (超级管理员拥有所有权限)
INSERT INTO t_role_permission (role_id, permission_id, create_user_id)
SELECT 1, permission_id, 1 FROM t_permission;

-- 管理员权限分配
INSERT INTO t_role_permission (role_id, permission_id, create_user_id) VALUES
-- 门禁管理员权限
(4, 10), (4, 11), (4, 12), (4, 13), -- 门禁相关权限
-- 考勤管理员权限
(5, 14), (5, 15), (5, 16), (5, 17), -- 考勤相关权限
-- 消费管理员权限
(6, 18), (6, 19), (6, 20), (6, 21), (6, 22), -- 消费相关权限
-- 访客管理员权限
(7, 23), (7, 24), (7, 25), -- 访客相关权限
-- 视频管理员权限
(8, 26), (8, 27), (8, 28), (8, 29), -- 视频相关权限
(4, 1), (4, 2), (4, 3), (4, 4), -- 系统查看权限
(5, 1), (5, 2), (5, 3), (5, 4), -- 系统查看权限
(6, 1), (6, 2), (6, 3), (6, 4), -- 系统查看权限
(7, 1), (7, 2), (7, 3), (7, 4), -- 系统查看权限
(8, 1), (8, 2), (8, 3), (8, 4); -- 系统查看权限

-- 普通用户基础权限
INSERT INTO t_role_permission (role_id, permission_id, create_user_id) VALUES
(3, 14), -- 考勤记录查询
(3, 18), -- 消费记录查询
(3, 23), -- 访客记录查询
(3, 26), -- 实时监控查看
(3, 1),  -- 系统首页
(3, 30); -- 数据统计查看

-- 6. 默认部门数据
INSERT INTO t_department (department_code, department_name, parent_id, level, sort_order, description, create_user_id) VALUES
('ROOT', 'IOE-DREAM集团', 0, 1, 1, 'IOE-DREAM智慧园区管理集团', 1),
('IT', '信息技术部', 1, 2, 1, '负责系统开发和维护', 1),
('HR', '人力资源部', 1, 2, 2, '负责人力资源管理', 1),
('ADMIN', '行政管理部', 1, 2, 3, '负责园区行政管理', 1),
('SECURITY', '安全保卫部', 1, 2, 4, '负责园区安全保卫', 1),
('FACILITY', '设施维护部', 1, 2, 5, '负责设施设备维护', 1);

-- 7. 默认区域数据
INSERT INTO t_area (area_code, area_name, parent_id, level, path, area_type, sort_order, description, manager_id, create_user_id) VALUES
('ROOT', 'IOE-DREAM智慧园区', 0, 1, '/ROOT', 1, 1, 'IOE-DREAM智慧园区整体区域', 1, 1),
('BUILD_A', 'A栋办公楼', 1, 2, '/ROOT/BUILD_A', 2, 1, 'A栋办公楼，包含办公区域和会议室', 1, 1),
('BUILD_B', 'B栋研发中心', 1, 2, '/ROOT/BUILD_B', 2, 2, 'B栋研发中心，技术研发部门所在地', 1, 1),
('BUILD_C', 'C栋生产中心', 1, 2, '/ROOT/BUILD_C', 2, 3, 'C栋生产中心，生产制造区域', 1, 1),
('CANTEEN_A', 'A栋餐厅', 1, 2, '/ROOT/CANTEEN_A', 5, 4, 'A栋员工餐厅，提供餐饮服务', 1, 1),
('CANTEEN_B', 'B栋餐厅', 1, 2, '/ROOT/CANTEEN_B', 5, 5, 'B栋员工餐厅，提供餐饮服务', 1, 1),
('PARKING_A', 'A区停车场', 1, 2, '/ROOT/PARKING_A', 5, 6, 'A区停车场，员工和访客停车区域', 1, 1),
('PARKING_B', 'B区停车场', 1, 2, '/ROOT/PARKING_B', 5, 7, 'B区停车场，员工和访客停车区域', 1, 1),
('RECEPTION', '前台接待区', 1, 2, '/ROOT/RECEPTION', 4, 8, '园区前台接待和访客登记区域', 1, 1),
('CONFERENCE', '会议中心', 1, 2, '/ROOT/CONFERENCE', 4, 9, '园区会议中心，包含多个会议室', 1, 1);

-- 8. 默认字典类型
INSERT INTO t_dict_type (dict_type_code, dict_type_name, description, sort_order, create_user_id) VALUES
('GENDER', '性别', '性别字典类型', 1, 1),
('USER_STATUS', '用户状态', '用户状态字典类型', 2, 1),
('AREA_TYPE', '区域类型', '区域类型字典类型', 3, 1),
('DEVICE_TYPE', '设备类型', '设备类型字典类型', 4, 1),
('CONSUME_TYPE', '消费类型', '消费类型字典类型', 5, 1),
('VISIT_TYPE', '访问类型', '访问类型字典类型', 6, 1),
('ATTENDANCE_TYPE', '考勤类型', '考勤类型字典类型', 7, 1),
('PAYMENT_METHOD', '支付方式', '支付方式字典类型', 8, 1);

-- 9. 默认字典数据
INSERT INTO t_dict_data (dict_type_code, dict_code, dict_value, description, sort_order, create_user_id) VALUES
-- 性别
('GENDER', '1', '男', '男性', 1, 1),
('GENDER', '2', '女', '女性', 2, 1),

-- 用户状态
('USER_STATUS', '1', '启用', '用户启用状态', 1, 1),
('USER_STATUS', '0', '禁用', '用户禁用状态', 2, 1),
('USER_STATUS', '2', '锁定', '用户锁定状态', 3, 1),

-- 区域类型
('AREA_TYPE', '1', '园区', '园区级别区域', 1, 1),
('AREA_TYPE', '2', '建筑', '建筑物级别区域', 2, 1),
('AREA_TYPE', '3', '楼层', '楼层级别区域', 3, 1),
('AREA_TYPE', '4', '房间', '房间级别区域', 4, 1),
('AREA_TYPE', '5', '区域', '功能区域级别', 5, 1),

-- 设备类型
('DEVICE_TYPE', 'CAMERA', '摄像头', '视频监控摄像头设备', 1, 1),
('DEVICE_TYPE', 'ACCESS', '门禁设备', '门禁控制设备', 2, 1),
('DEVICE_TYPE', 'CONSUME', '消费机', '消费支付终端设备', 3, 1),
('DEVICE_TYPE', 'ATTENDANCE', '考勤机', '考勤打卡设备', 4, 1),
('DEVICE_TYPE', 'NVR', '录像机', '网络视频录像机', 5, 1),
('DEVICE_TYPE', 'DVR', '硬盘录像机', '数字视频录像机', 6, 1),
('DEVICE_TYPE', 'INTERCOM', '对讲机', '可视对讲设备', 7, 1),
('DEVICE_TYPE', 'ALARM', '报警器', '安防报警设备', 8, 1),
('DEVICE_TYPE', 'SENSOR', '传感器', '环境传感器设备', 9, 1),

-- 消费类型
('CONSUME_TYPE', 'MEAL', '餐饮消费', '餐厅餐饮消费', 1, 1),
('CONSUME_TYPE', 'SHOP', '超市消费', '超市购物消费', 2, 1),
('CONSUME_TYPE', 'OTHER', '其他消费', '其他类型消费', 3, 1),

-- 访问类型
('VISIT_TYPE', 'APPOINTMENT', '预约访问', '提前预约的访问', 1, 1),
('VISIT_TYPE', 'TEMPORARY', '临时访问', '临时性访问', 2, 1),
('VISIT_TYPE', 'INTERVIEW', '面试访问', '招聘面试访问', 3, 1),
('VISIT_TYPE', 'DELIVERY', '配送访问', '快递配送访问', 4, 1),

-- 考勤类型
('ATTENDANCE_TYPE', 'ON', '上班打卡', '上班时间打卡', 1, 1),
('ATTENDANCE_TYPE', 'OFF', '下班打卡', '下班时间打卡', 2, 1),
('ATTENDANCE_TYPE', 'OVERTIME_IN', '加班上班', '加班开始打卡', 3, 1),
('ATTENDANCE_TYPE', 'OVERTIME_OUT', '加班下班', '加班结束打卡', 4, 1),

-- 支付方式
('PAYMENT_METHOD', 'BALANCE', '余额支付', '使用账户余额支付', 1, 1),
('PAYMENT_METHOD', 'WECHAT', '微信支付', '使用微信支付', 2, 1),
('PAYMENT_METHOD', 'ALIPAY', '支付宝', '使用支付宝支付', 3, 1),
('PAYMENT_METHOD', 'CARD', '银行卡', '使用银行卡支付', 4, 1),
('PAYMENT_METHOD', 'CASH', '现金', '使用现金支付', 5, 1);

-- 10. 菜单数据
INSERT INTO t_menu (menu_code, menu_name, parent_id, menu_type, menu_url, menu_icon, permission_code, sort_order) VALUES
('SYSTEM_MGMT', '系统管理', 0, 1, '/system', 'system', 'SYSTEM', 1),
('USER_MGMT', '用户管理', 1, 2, '/system/user', 'user', 'SYSTEM:USER', 1),
('ROLE_MGMT', '角色管理', 1, 2, '/system/role', 'role', 'SYSTEM:ROLE', 2),
('PERMISSION_MGMT', '权限管理', 1, 2, '/system/permission', 'permission', 'SYSTEM:PERMISSION', 3),
('DEPT_MGMT', '部门管理', 1, 2, '/system/department', 'department', 'SYSTEM:DEPARTMENT', 4),
('AREA_MGMT', '区域管理', 1, 2, '/system/area', 'area', 'SYSTEM:AREA', 5),
('DICT_MGMT', '字典管理', 1, 2, '/system/dict', 'dict', 'SYSTEM:DICT', 6),

('ACCESS_MGMT', '门禁管理', 0, 1, '/access', 'access', 'ACCESS', 2),
('ACCESS_DEVICE', '门禁设备', 9, 2, '/access/device', 'device', 'ACCESS:DEVICE', 1),
('ACCESS_RECORD', '通行记录', 9, 2, '/access/record', 'record', 'ACCESS:RECORD', 2),
('ACCESS_PERMISSION', '门禁权限', 9, 2, '/access/permission', 'permission', 'ACCESS:PERMISSION', 3),
('ACCESS_MONITOR', '实时监控', 9, 2, '/access/monitor', 'monitor', 'ACCESS:MONITOR', 4),

('ATTENDANCE_MGMT', '考勤管理', 0, 1, '/attendance', 'attendance', 'ATTENDANCE', 3),
('ATTENDANCE_RECORD', '考勤记录', 10, 2, '/attendance/record', 'clock', 'ATTENDANCE:RECORD', 1),
('ATTENDANCE_SCHEDULE', '排班管理', 10, 2, '/attendance/schedule', 'schedule', 'ATTENDANCE:SCHEDULE', 2),
('ATTENDANCE_SHIFT', '班次管理', 10, 2, '/attendance/shift', 'shift', 'ATTENDANCE:SHIFT', 3),
('ATTENDANCE_LEAVE', '请假管理', 10, 2, '/attendance/leave', 'leave', 'ATTENDANCE:LEAVE', 4),

('CONSUME_MGMT', '消费管理', 0, 1, '/consume', 'consume', 'CONSUME', 4),
('CONSUME_ACCOUNT', '账户管理', 15, 2, '/consume/account', 'account', 'CONSUME:ACCOUNT', 1),
('CONSUME_RECORD', '消费记录', 15, 2, '/consume/record', 'payment', 'CONSUME:RECORD', 2),
('CONSUME_DEVICE', '消费设备', 15, 2, '/consume/device', 'device', 'CONSUME:DEVICE', 3),
('CONSUME_RECHARGE', '充值管理', 15, 2, '/consume/recharge', 'recharge', 'CONSUME:RECHARGE', 4),
('CONSUME_SUBSIDY', '补贴管理', 15, 2, '/consume/subsidy', 'subsidy', 'CONSUME:SUBSIDY', 5),

('VISITOR_MGMT', '访客管理', 0, 1, '/visitor', 'visitor', 'VISITOR', 5),
('VISITOR_APPOINTMENT', '访客预约', 18, 2, '/visitor/appointment', 'appointment', 'VISITOR:APPOINTMENT', 1),
('VISITOR_RECORD', '访客记录', 18, 2, '/visitor/record', 'visitor', 'VISITOR:RECORD', 2),
('VISITOR_PERMISSION', '访客权限', 18, 2, '/visitor/permission', 'permission', 'VISITOR:PERMISSION', 3),

('VIDEO_MGMT', '视频监控', 0, 1, '/video', 'video', 'VIDEO', 6),
('VIDEO_DEVICE', '视频设备', 21, 2, '/video/device', 'video-camera', 'VIDEO:DEVICE', 1),
('VIDEO_LIVE', '实时监控', 21, 2, '/video/live', 'video', 'VIDEO:LIVE', 2),
('VIDEO_RECORD', '录像回放', 21, 2, '/video/record', 'record', 'VIDEO:RECORD', 3),
('VIDEO_ALARM', '报警管理', 21, 2, '/video/alarm', 'alarm', 'VIDEO:ALARM', 4),

('DEVICE_MGMT', '设备管理', 0, 1, '/device', 'device', 'DEVICE', 7),
('DEVICE_LIST', '设备列表', 24, 2, '/device/list', 'devices', 'DEVICE:LIST', 1),
('DEVICE_MAINTENANCE', '设备维护', 24, 2, '/device/maintenance', 'maintenance', 'DEVICE:MAINTENANCE', 2),
('DEVICE_MONITOR', '设备监控', 24, 2, '/device/monitor', 'monitor', 'DEVICE:MONITOR', 3),

('REPORT_MGMT', '数据统计', 0, 1, '/report', 'chart', 'REPORT', 8),
('REPORT_ACCESS', '门禁统计', 27, 2, '/report/access', 'chart', 'REPORT:ACCESS', 1),
('REPORT_ATTENDANCE', '考勤统计', 27, 2, '/report/attendance', 'chart', 'REPORT:ATTENDANCE', 2),
('REPORT_CONSUME', '消费统计', 27, 2, '/report/consume', 'chart', 'REPORT:CONSUME', 3),
('REPORT_VISITOR', '访客统计', 27, 2, '/report/visitor', 'chart', 'REPORT:VISITOR', 4);

SELECT '公共数据初始化完成' as message,
       COUNT(*) as initialized_count
FROM (
    SELECT 'users' as type, COUNT(*) as count FROM t_user
    UNION ALL
    SELECT 'roles', COUNT(*) FROM t_role
    UNION ALL
    SELECT 'permissions', COUNT(*) FROM t_permission
    UNION ALL
    SELECT 'user_roles', COUNT(*) FROM t_user_role
    UNION ALL
    SELECT 'role_permissions', COUNT(*) FROM t_role_permission
    UNION ALL
    SELECT 'departments', COUNT(*) FROM t_department
    UNION ALL
    SELECT 'areas', COUNT(*) FROM t_area
    UNION ALL
    SELECT 'dict_types', COUNT(*) FROM t_dict_type
    UNION ALL
    SELECT 'dict_data', COUNT(*) FROM t_dict_data
    UNION ALL
    SELECT 'menus', COUNT(*) FROM t_menu
) as summary;