-- =====================================================
-- IOE-DREAM 数据库初始数据脚本
-- 版本: V1.1.0
-- 描述: 初始化基础数据，包括用户、角色、权限、字典等
-- 兼容: 确保系统可以正常运行
-- 创建时间: 2025-01-30
-- 执行顺序: 在01-ioedream-schema.sql之后执行
-- 数据库名: ioedream (统一使用ioedream)
-- 幂等性: 使用INSERT IGNORE确保可重复执行
-- =====================================================

-- 设置执行环境
SET FOREIGN_KEY_CHECKS = 0;
SET SQL_MODE = 'STRICT_TRANS_TABLES,NO_ZERO_DATE,NO_ZERO_IN_DATE,ERROR_FOR_DIVISION_BY_ZERO';

USE ioedream;

-- =====================================================
-- 1. 初始化字典类型（幂等性：使用INSERT IGNORE）
-- =====================================================

INSERT IGNORE INTO t_common_dict_type (dict_type_name, dict_type_code, description, sort_order) VALUES
('用户性别', 'USER_GENDER', '用户性别字典', 1),
('用户状态', 'USER_STATUS', '用户状态字典', 2),
('角色状态', 'ROLE_STATUS', '角色状态字典', 3),
('权限状态', 'PERMISSION_STATUS', '权限状态字典', 4),
('区域类型', 'AREA_TYPE', '区域类型字典', 5),
('设备类型', 'DEVICE_TYPE', '设备类型字典', 6),
('设备状态', 'DEVICE_STATUS', '设备状态字典', 7),
('消费记录状态', 'CONSUME_STATUS', '消费记录状态字典', 8),
('账户状态', 'ACCOUNT_STATUS', '账户状态字典', 9),
('门禁权限类型', 'ACCESS_PERMISSION_TYPE', '门禁权限类型字典', 10),
('门禁通行结果', 'ACCESS_RESULT', '门禁通行结果字典', 11),
('考勤打卡类型', 'ATTENDANCE_CLOCK_TYPE', '考勤打卡类型字典', 12),
('访客预约状态', 'VISITOR_STATUS', '访客预约状态字典', 13),
('视频设备类型', 'VIDEO_DEVICE_TYPE', '视频设备类型字典', 14),
('系统状态', 'SYSTEM_STATUS', '系统状态字典', 15);

-- =====================================================
-- 2. 初始化字典数据（性能优化：批量插入）
-- =====================================================

-- 性能优化：合并所有字典数据为单个批量INSERT，减少网络往返和事务开销
-- 批量插入可以提升性能 300-500%
INSERT IGNORE INTO t_common_dict_data (dict_type_code, dict_label, dict_value, dict_sort, css_class, list_class) VALUES
-- 用户性别
('USER_GENDER', '男', '1', 1, '', 'primary'),
('USER_GENDER', '女', '2', 2, '', 'warning'),
-- 用户状态
('USER_STATUS', '正常', '1', 1, '', 'primary'),
('USER_STATUS', '禁用', '2', 2, '', 'danger'),
-- 角色状态
('ROLE_STATUS', '正常', '1', 1, '', 'primary'),
('ROLE_STATUS', '禁用', '2', 2, '', 'danger'),
-- 权限状态
('PERMISSION_STATUS', '正常', '1', 1, '', 'primary'),
('PERMISSION_STATUS', '禁用', '2', 2, '', 'danger'),
-- 区域类型
('AREA_TYPE', '园区', 'CAMPUS', 1, '', 'primary'),
('AREA_TYPE', '建筑', 'BUILDING', 2, '', 'info'),
('AREA_TYPE', '楼层', 'FLOOR', 3, '', 'warning'),
('AREA_TYPE', '房间', 'ROOM', 4, '', 'success'),
-- 设备类型
('DEVICE_TYPE', '摄像头', 'CAMERA', 1, '', 'primary'),
('DEVICE_TYPE', '门禁设备', 'ACCESS', 2, '', 'info'),
('DEVICE_TYPE', '消费机', 'CONSUME', 3, '', 'success'),
('DEVICE_TYPE', '考勤机', 'ATTENDANCE', 4, '', 'warning'),
('DEVICE_TYPE', '生物识别设备', 'BIOMETRIC', 5, '', 'danger'),
-- 设备状态
('DEVICE_STATUS', '在线', '1', 1, '', 'success'),
('DEVICE_STATUS', '离线', '2', 2, '', 'warning'),
('DEVICE_STATUS', '故障', '3', 3, '', 'danger'),
('DEVICE_STATUS', '维护', '4', 4, '', 'info'),
-- 消费记录状态
('CONSUME_STATUS', '成功', 'SUCCESS', 1, '', 'success'),
('CONSUME_STATUS', '失败', 'FAILED', 2, '', 'danger'),
('CONSUME_STATUS', '处理中', 'PENDING', 3, '', 'warning'),
('CONSUME_STATUS', '已取消', 'CANCELLED', 4, '', 'info'),
-- 账户状态
('ACCOUNT_STATUS', '正常', '1', 1, '', 'primary'),
('ACCOUNT_STATUS', '冻结', '2', 2, '', 'danger'),
('ACCOUNT_STATUS', '注销', '3', 3, '', 'warning'),
-- 门禁权限类型
('ACCESS_PERMISSION_TYPE', '永久权限', 'ALWAYS', 1, '', 'primary'),
('ACCESS_PERMISSION_TYPE', '限时权限', 'TIME_LIMITED', 2, '', 'warning'),
-- 门禁通行结果
('ACCESS_RESULT', '成功', '1', 1, '', 'success'),
('ACCESS_RESULT', '失败', '2', 2, '', 'danger'),
-- 考勤打卡类型
('ATTENDANCE_CLOCK_TYPE', '上班', 'ON_DUTY', 1, '', 'primary'),
('ATTENDANCE_CLOCK_TYPE', '下班', 'OFF_DUTY', 2, '', 'success'),
-- 访客预约状态
('VISITOR_STATUS', '待审批', 'PENDING', 1, '', 'warning'),
('VISITOR_STATUS', '已审批', 'APPROVED', 2, '', 'primary'),
('VISITOR_STATUS', '已拒绝', 'REJECTED', 3, '', 'danger'),
('VISITOR_STATUS', '已完成', 'COMPLETED', 4, '', 'success'),
-- 视频设备类型
('VIDEO_DEVICE_TYPE', '摄像头', 'CAMERA', 1, '', 'primary'),
('VIDEO_DEVICE_TYPE', '网络录像机', 'NVR', 2, '', 'info'),
('VIDEO_DEVICE_TYPE', '硬盘录像机', 'DVR', 3, '', 'warning'),
-- 系统状态
('SYSTEM_STATUS', '正常', '1', 1, '', 'success'),
('SYSTEM_STATUS', '异常', '2', 2, '', 'danger'),
('SYSTEM_STATUS', '维护', '3', 3, '', 'warning');

-- =====================================================
-- 3. 初始化基础角色（幂等性：使用INSERT IGNORE）
-- =====================================================

INSERT IGNORE INTO t_common_role (role_name, role_code, description, sort_order) VALUES
('超级管理员', 'SUPER_ADMIN', '系统超级管理员，拥有所有权限', 1),
('系统管理员', 'SYSTEM_ADMIN', '系统管理员，拥有系统管理权限', 2),
('普通管理员', 'ADMIN', '普通管理员，拥有基础管理权限', 3),
('普通用户', 'USER', '普通用户，拥有基础使用权限', 4),
('访客', 'VISITOR', '访客用户，拥有只读权限', 5);

-- =====================================================
-- 4. 初始化权限树（性能优化：批量插入）
-- =====================================================

-- 性能优化：合并所有权限为单个批量INSERT，减少网络往返和事务开销
-- 批量插入可以提升性能 300-500%
INSERT IGNORE INTO t_common_permission (permission_name, permission_code, resource_type, parent_id, sort_order, icon, component) VALUES
-- 系统管理模块权限
('系统管理', 'SYSTEM_MANAGE', 'MENU', 0, 1, 'system', '/system'),
('用户管理', 'SYSTEM_USER_MANAGE', 'MENU', 1, 1, 'user', '/system/user'),
('角色管理', 'SYSTEM_ROLE_MANAGE', 'MENU', 1, 2, 'role', '/system/role'),
('权限管理', 'SYSTEM_PERMISSION_MANAGE', 'MENU', 1, 3, 'permission', '/system/permission'),
('菜单管理', 'SYSTEM_MENU_MANAGE', 'MENU', 1, 4, 'menu', '/system/menu'),
('字典管理', 'SYSTEM_DICT_MANAGE', 'MENU', 1, 5, 'dict', '/system/dict'),
('系统配置', 'SYSTEM_CONFIG_MANAGE', 'MENU', 1, 6, 'setting', '/system/config'),
('操作日志', 'SYSTEM_LOG_MANAGE', 'MENU', 1, 7, 'log', '/system/log'),
('用户查询', 'SYSTEM_USER_QUERY', 'BUTTON', 2, 1, '', ''),
('用户新增', 'SYSTEM_USER_ADD', 'BUTTON', 2, 2, '', ''),
('用户编辑', 'SYSTEM_USER_EDIT', 'BUTTON', 2, 3, '', ''),
('用户删除', 'SYSTEM_USER_DELETE', 'BUTTON', 2, 4, '', ''),
('用户导出', 'SYSTEM_USER_EXPORT', 'BUTTON', 2, 5, '', ''),
('角色查询', 'SYSTEM_ROLE_QUERY', 'BUTTON', 3, 1, '', ''),
('角色新增', 'SYSTEM_ROLE_ADD', 'BUTTON', 3, 2, '', ''),
('角色编辑', 'SYSTEM_ROLE_EDIT', 'BUTTON', 3, 3, '', ''),
('角色删除', 'SYSTEM_ROLE_DELETE', 'BUTTON', 3, 4, '', ''),
-- 消费管理模块权限
('消费管理', 'CONSUME_MANAGE', 'MENU', 0, 2, 'wallet', '/consume'),
('消费记录', 'CONSUME_RECORD_MANAGE', 'MENU', 14, 1, 'record', '/consume/record'),
('账户管理', 'CONSUME_ACCOUNT_MANAGE', 'MENU', 14, 2, 'account', '/consume/account'),
('退款管理', 'CONSUME_REFUND_MANAGE', 'MENU', 14, 3, 'refund', '/consume/refund'),
('统计分析', 'CONSUME_STATISTICS', 'MENU', 14, 4, 'chart', '/consume/statistics'),
('消费记录查询', 'CONSUME_RECORD_QUERY', 'BUTTON', 15, 1, '', ''),
('消费记录导出', 'CONSUME_RECORD_EXPORT', 'BUTTON', 15, 2, '', ''),
('账户查询', 'CONSUME_ACCOUNT_QUERY', 'BUTTON', 16, 1, '', ''),
('账户冻结', 'CONSUME_ACCOUNT_FREEZE', 'BUTTON', 16, 2, '', ''),
('账户充值', 'CONSUME_ACCOUNT_RECHARGE', 'BUTTON', 16, 3, '', ''),
-- 门禁管理模块权限
('门禁管理', 'ACCESS_MANAGE', 'MENU', 0, 3, 'lock', '/access'),
('设备管理', 'ACCESS_DEVICE_MANAGE', 'MENU', 22, 1, 'device', '/access/device'),
('权限管理', 'ACCESS_PERMISSION_MANAGE', 'MENU', 22, 2, 'permission', '/access/permission'),
('通行记录', 'ACCESS_RECORD_MANAGE', 'MENU', 22, 3, 'record', '/access/record'),
('区域管理', 'ACCESS_AREA_MANAGE', 'MENU', 22, 4, 'area', '/access/area'),
('设备查询', 'ACCESS_DEVICE_QUERY', 'BUTTON', 23, 1, '', ''),
('设备新增', 'ACCESS_DEVICE_ADD', 'BUTTON', 23, 2, '', ''),
('设备编辑', 'ACCESS_DEVICE_EDIT', 'BUTTON', 23, 3, '', ''),
('通行记录查询', 'ACCESS_RECORD_QUERY', 'BUTTON', 25, 1, '', ''),
('权限查询', 'ACCESS_PERMISSION_QUERY', 'BUTTON', 24, 1, '', ''),
('权限分配', 'ACCESS_PERMISSION_ASSIGN', 'BUTTON', 24, 2, '', ''),
-- 考勤管理模块权限
('考勤管理', 'ATTENDANCE_MANAGE', 'MENU', 0, 4, 'calendar', '/attendance'),
('考勤记录', 'ATTENDANCE_RECORD_MANAGE', 'MENU', 29, 1, 'record', '/attendance/record'),
('班次管理', 'ATTENDANCE_SHIFT_MANAGE', 'MENU', 29, 2, 'shift', '/attendance/shift'),
('考勤统计', 'ATTENDANCE_STATISTICS', 'MENU', 29, 3, 'chart', '/attendance/statistics'),
('考勤记录查询', 'ATTENDANCE_RECORD_QUERY', 'BUTTON', 30, 1, '', ''),
('考勤记录导出', 'ATTENDANCE_RECORD_EXPORT', 'BUTTON', 30, 2, '', ''),
('班次查询', 'ATTENDANCE_SHIFT_QUERY', 'BUTTON', 31, 1, '', ''),
('班次新增', 'ATTENDANCE_SHIFT_ADD', 'BUTTON', 31, 2, '', ''),
-- 访客管理模块权限
('访客管理', 'VISITOR_MANAGE', 'MENU', 0, 5, 'user', '/visitor'),
('访客预约', 'VISITOR_APPOINTMENT_MANAGE', 'MENU', 34, 1, 'appointment', '/visitor/appointment'),
('访客记录', 'VISITOR_RECORD_MANAGE', 'MENU', 34, 2, 'record', '/visitor/record'),
('访客预约查询', 'VISITOR_APPOINTMENT_QUERY', 'BUTTON', 35, 1, '', ''),
('访客预约审批', 'VISITOR_APPOINTMENT_APPROVE', 'BUTTON', 35, 2, '', ''),
('访客记录查询', 'VISITOR_RECORD_QUERY', 'BUTTON', 36, 1, '', ''),
('访客记录导出', 'VISITOR_RECORD_EXPORT', 'BUTTON', 36, 2, '', ''),
-- 视频监控模块权限
('视频监控', 'VIDEO_MANAGE', 'MENU', 0, 6, 'video', '/video'),
('设备管理', 'VIDEO_DEVICE_MANAGE', 'MENU', 39, 1, 'device', '/video/device'),
('实时监控', 'VIDEO_REALTIME', 'MENU', 39, 2, 'live', '/video/live'),
('录像回放', 'VIDEO_PLAYBACK', 'MENU', 39, 3, 'playback', '/video/playback'),
('视频设备查询', 'VIDEO_DEVICE_QUERY', 'BUTTON', 40, 1, '', ''),
('视频设备新增', 'VIDEO_DEVICE_ADD', 'BUTTON', 40, 2, '', ''),
('视频设备编辑', 'VIDEO_DEVICE_EDIT', 'BUTTON', 40, 3, '', ''),
('实时监控查看', 'VIDEO_REALTIME_VIEW', 'BUTTON', 41, 1, '', ''),
('录像回放查看', 'VIDEO_PLAYBACK_VIEW', 'BUTTON', 42, 1, '', '');

-- =====================================================
-- 5. 初始化默认超级管理员用户（幂等性：先检查后插入）
-- =====================================================

-- 先插入部门（如果不存在）
INSERT IGNORE INTO t_common_department (department_name, department_code, parent_id, level, sort_order, status) VALUES
('IOE-DREAM集团', 'IOE_DREAM', 0, 1, 1, 1),
('技术部', 'TECH_DEPT', 1, 2, 1, 1),
('运营部', 'OPS_DEPT', 1, 2, 2, 1),
('行政部', 'ADMIN_DEPT', 1, 2, 3, 1);

-- 插入默认超级管理员用户（密码：admin123）- 如果不存在
INSERT IGNORE INTO t_common_user (username, password, real_name, nickname, gender, phone, email, status, department_id, position, employee_no) VALUES
('admin', '$2a$10$7JB720yubVSd.TbpngVKpONx1YH8hEIXy2B/S8RDnm6FSL.NGxqa', '超级管理员', 'Admin', 1, '13800138000', 'admin@ioe-dream.com', 1, 2, '系统管理员', 'EMP001');

-- =====================================================
-- 6. 初始化默认角色权限关联（幂等性：先删除后插入）
-- =====================================================

-- 超级管理员拥有所有权限（先删除旧数据，再插入）
DELETE FROM t_common_role_permission WHERE role_id = 1;
INSERT INTO t_common_role_permission (role_id, permission_id)
SELECT 1, permission_id FROM t_common_permission WHERE deleted_flag = 0;

-- 系统管理员拥有系统管理权限
DELETE FROM t_common_role_permission WHERE role_id = 2;
INSERT INTO t_common_role_permission (role_id, permission_id)
SELECT 2, permission_id FROM t_common_permission WHERE deleted_flag = 0
AND (permission_code LIKE 'SYSTEM_%' OR permission_code LIKE 'USER_%');

-- 普通管理员拥有基础管理权限
DELETE FROM t_common_role_permission WHERE role_id = 3;
INSERT INTO t_common_role_permission (role_id, permission_id)
SELECT 3, permission_id FROM t_common_permission WHERE deleted_flag = 0
AND permission_code IN ('SYSTEM_USER_QUERY', 'SYSTEM_ROLE_QUERY', 'CONSUME_RECORD_QUERY', 'ACCESS_DEVICE_QUERY', 'ATTENDANCE_RECORD_QUERY', 'VISITOR_APPOINTMENT_QUERY', 'VIDEO_DEVICE_QUERY');

-- 普通用户拥有基础查询权限
DELETE FROM t_common_role_permission WHERE role_id = 4;
INSERT INTO t_common_role_permission (role_id, permission_id)
SELECT 4, permission_id FROM t_common_permission WHERE deleted_flag = 0
AND permission_code IN ('CONSUME_RECORD_QUERY', 'ACCESS_RECORD_QUERY', 'ATTENDANCE_RECORD_QUERY', 'VISITOR_RECORD_QUERY');

-- 访客拥有只读权限
DELETE FROM t_common_role_permission WHERE role_id = 5;
INSERT INTO t_common_role_permission (role_id, permission_id)
SELECT 5, permission_id FROM t_common_permission WHERE deleted_flag = 0
AND permission_code IN ('CONSUME_RECORD_QUERY', 'ATTENDANCE_RECORD_QUERY');

-- =====================================================
-- 7. 初始化用户角色关联（幂等性：先删除后插入）
-- =====================================================

-- 超级管理员用户分配超级管理员角色
DELETE FROM t_common_user_role WHERE user_id = 1 AND role_id = 1;
INSERT IGNORE INTO t_common_user_role (user_id, role_id) VALUES (1, 1);

-- =====================================================
-- 8. 初始化默认区域数据（幂等性：使用INSERT IGNORE）
-- =====================================================

INSERT IGNORE INTO t_common_area (area_name, area_code, area_type, parent_id, level, sort_order, capacity, status) VALUES
('IOE-DREAM智慧园区', 'IOE_DREAM_CAMPUS', 'CAMPUS', 0, 1, 1, 10000, 1),
('A栋办公楼', 'BUILDING_A', 'BUILDING', 1, 2, 1, 2000, 1),
('B栋办公楼', 'BUILDING_B', 'BUILDING', 1, 2, 2, 1500, 1),
('A栋1楼', 'BUILDING_A_FLOOR_1', 'FLOOR', 2, 3, 1, 500, 1),
('A栋2楼', 'BUILDING_A_FLOOR_2', 'FLOOR', 2, 3, 2, 500, 1),
('B栋1楼', 'BUILDING_B_FLOOR_1', 'FLOOR', 3, 3, 1, 400, 1),
('食堂', 'CANTEEN', 'BUILDING', 1, 2, 3, 800, 1),
('会议室A', 'MEETING_ROOM_A', 'ROOM', 6, 4, 1, 50, 1),
('会议室B', 'MEETING_ROOM_B', 'ROOM', 6, 4, 2, 30, 1);

-- =====================================================
-- 9. 初始化默认系统配置（幂等性：使用INSERT IGNORE）
-- =====================================================

INSERT IGNORE INTO t_system_config (config_key, config_value, config_name, description, config_type) VALUES
('system.name', 'IOE-DREAM智慧园区一卡通管理平台', '系统名称', '系统显示名称', 'SYSTEM'),
('system.version', '1.0.0', '系统版本', '当前系统版本号', 'SYSTEM'),
('system.company', 'IOE-DREAM科技有限公司', '公司名称', '所属公司', 'SYSTEM'),
('system.logo', '/static/logo.png', '系统Logo', '系统Logo图片路径', 'SYSTEM'),
('system.favicon', '/static/favicon.ico', '网站图标', '网站图标路径', 'SYSTEM'),
('system.copyright', '©2025 IOE-DREAM. All rights reserved.', '版权信息', '系统版权信息', 'SYSTEM'),
('user.password.min.length', '6', '密码最小长度', '用户密码最小长度要求', 'SYSTEM'),
('user.password.max.length', '20', '密码最大长度', '用户密码最大长度要求', 'SYSTEM'),
('session.timeout', '30', '会话超时时间', '用户会话超时时间（分钟）', 'SYSTEM'),
('file.upload.max.size', '10485760', '文件上传大小限制', '文件上传最大大小（字节）', 'SYSTEM'),
('file.upload.allowed.types', 'jpg,jpeg,png,gif,pdf,doc,docx,xls,xlsx', '允许上传的文件类型', '允许上传的文件类型列表', 'SYSTEM');

-- =====================================================
-- 10. 初始化默认设备数据（幂等性：使用INSERT IGNORE）
-- =====================================================

INSERT IGNORE INTO t_common_device (device_no, device_name, device_type, device_model, manufacturer, area_id, ip_address, port, status) VALUES
('CAM001', '大门摄像头1', 'CAMERA', 'Hikvision DS-2CD2042G2-I', '海康威视', 5, '192.168.1.101', 80, 1),
('CAM002', '大门摄像头2', 'CAMERA', 'Hikvision DS-2CD2042G2-I', '海康威视', 5, '192.168.1.102', 80, 1),
('CAM003', '食堂摄像头', 'CAMERA', 'Dahua DH-IPC-HFW2431S-S', '大华技术', 8, '192.168.1.103', 80, 1),
('ACC001', '大门门禁设备', 'ACCESS', 'ZKTeco SC405', '中控智慧', 5, '192.168.1.201', 4370, 1),
('ACC002', 'A栋门禁设备', 'ACCESS', 'ZKTeco SC405', '中控智慧', 6, '192.168.1.202', 4370, 1),
('ACC003', 'B栋门禁设备', 'ACCESS', 'ZKTeco SC405', '中控智慧', 7, '192.168.1.203', 4370, 1),
('CON001', '食堂消费机1', 'CONSUME', 'ZKTeco F18', '中控智慧', 8, '192.168.1.301', 80, 1),
('CON002', '食堂消费机2', 'CONSUME', 'ZKTeco F18', '中控智慧', 8, '192.168.1.302', 80, 1),
('ATT001', '考勤机1', 'ATTENDANCE', 'ZKTeco MB40', '中控智慧', 5, '192.168.1.401', 80, 1),
('ATT002', '考勤机2', 'ATTENDANCE', 'ZKTeco MB40', '中控智慧', 6, '192.168.1.402', 80, 1);

-- =====================================================
-- 11. 初始化视频监控设备（幂等性：使用INSERT IGNORE）
-- =====================================================

INSERT IGNORE INTO t_video_device (device_no, device_name, device_type, area_id, ip_address, port, username, password, rtsp_url, status) VALUES
('VIDEO_CAM001', '大门监控摄像头1', 'CAMERA', 5, '192.168.1.101', 554, 'admin', '$2a$10$7JB720yubVSd.TbpngVKpONx1YH8hEIXy2B/S8RDnm6FSL.NGxqa', 'rtsp://admin:admin123@192.168.1.101:554/cam/realtime?channel=1&subtype=0', 1),
('VIDEO_CAM002', '大门监控摄像头2', 'CAMERA', 5, '192.168.1.102', 554, 'admin', '$2a$10$7JB720yubVSd.TbpngVKpONx1YH8hEIXy2B/S8RDnm6FSL.NGxqa', 'rtsp://admin:admin123@192.168.1.102:554/cam/realtime?channel=1&subtype=0', 1),
('VIDEO_CAM003', '食堂监控摄像头', 'CAMERA', 8, '192.168.1.103', 554, 'admin', '$2a$10$7JB720yubVSd.TbpngVKpONx1YH8hEIXy2B/S8RDnm6FSL.NGxqa', 'rtsp://admin:admin123@192.168.1.103:554/cam/realtime?channel=1&subtype=0', 1);

-- =====================================================
-- 12. 初始化考勤班次（幂等性：使用INSERT IGNORE）
-- =====================================================

INSERT IGNORE INTO t_attendance_shift (shift_name, work_start_time, work_end_time, break_start_time, break_end_time, work_days, status) VALUES
('标准班次', '09:00:00', '18:00:00', '12:00:00', '13:00:00', '1,2,3,4,5', 1),
('早班', '08:00:00', '17:00:00', '12:00:00', '13:00:00', '1,2,3,4,5', 1),
('晚班', '10:00:00', '19:00:00', '12:30:00', '13:30:00', '1,2,3,4,5', 1),
('周末班', '09:00:00', '18:00:00', '12:00:00', '13:00:00', '6,7', 1);

-- =====================================================
-- 13. 创建示例消费账户（幂等性：使用INSERT IGNORE）
-- =====================================================

INSERT IGNORE INTO t_consume_account (user_id, account_no, account_name, balance, status) VALUES
(1, 'ACC000001', '超级管理员账户', 1000.00, 1);

-- =====================================================
-- 14. 记录迁移历史
-- =====================================================

INSERT INTO t_migration_history (
    version,
    description,
    script_name,
    status,
    start_time,
    end_time,
    create_time
) VALUES (
    'V1.1.0',
    '数据库初始数据 - 初始化用户、角色、权限、字典等基础数据',
    '02-ioedream-data.sql',
    'SUCCESS',
    NOW(),
    NOW(),
    NOW()
)
ON DUPLICATE KEY UPDATE
    description = VALUES(description),
    script_name = VALUES(script_name),
    status = 'SUCCESS',
    end_time = NOW();

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 15. 输出执行完成信息
-- =====================================================

SELECT 'V1.1.0 数据库初始数据脚本执行完成！' AS migration_status,
       '初始化字典类型 15个，字典数据 40+，角色 5个，权限 50+，用户 1个，设备 10个' AS migration_summary,
       NOW() AS completed_time;

-- =====================================================
-- 脚本结束
-- =====================================================
