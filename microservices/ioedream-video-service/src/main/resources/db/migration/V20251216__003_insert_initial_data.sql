-- ============================================================
-- IOE-DREAM 视频监控微服务数据库迁移脚本
-- 版本: V20251216.003
-- 描述: 插入视频监控初始化数据
-- 服务: ioedream-video-service
-- 技术栈: Spring Boot 3.5.8 + Spring Cloud Alibaba 2025.0.0 + Flyway 9.x
-- 数据库: MySQL 8.0+
-- 创建时间: 2025-12-16
-- 作者: IOE-DREAM Team
-- ============================================================

-- 设置字符集和排序规则
SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ============================================================
-- 插入初始化设备类型数据
-- ============================================================

-- 设备类型字典数据
INSERT INTO t_common_dict_type (type_code, type_name, type_desc, status, sort_order) VALUES
('VIDEO_DEVICE_TYPE', '视频设备类型', '视频监控设备类型分类', 1, 100),
('VIDEO_DEVICE_SUBTYPE', '视频设备子类型', '视频监控设备子类型分类', 1, 101),
('VIDEO_STREAM_TYPE', '视频流类型', '视频流类型分类', 1, 102),
('VIDEO_STREAM_PROTOCOL', '视频流协议', '视频流协议分类', 1, 103),
('VIDEO_RECORDING_TYPE', '录像类型', '录像类型分类', 1, 104),
('VIDEO_QUALITY', '视频质量', '视频质量等级分类', 1, 105),
('VIDEO_EVENT_TYPE', '视频事件类型', '视频智能分析事件类型', 1, 106),
('VIDEO_TASK_TYPE', '视频任务类型', '视频调度任务类型', 1, 107),
('VIDEO_ACCESS_LEVEL', '视频访问级别', '视频访问权限级别', 1, 108);

-- 设备类型数据
INSERT INTO t_common_dict_data (type_code, dict_code, dict_name, dict_desc, status, sort_order, extend_info) VALUES
('VIDEO_DEVICE_TYPE', '1', '摄像头', '网络摄像机设备', 1, 1, '{"support_ptz":false, "support_audio":true, "max_channels": 1}'),
('VIDEO_DEVICE_TYPE', '2', 'NVR', '网络视频录像机', 1, 2, '{"support_ptz":false, "support_audio":true, "max_channels": 32}'),
('VIDEO_DEVICE_TYPE', '3', 'DVR', '数字视频录像机', 1, 3, '{"support_ptz":false, "support_audio":true, "max_channels": 16}'),
('VIDEO_DEVICE_TYPE', '4', '编码器', '视频编码器设备', 1, 4, '{"support_ptz":false, "support_audio":false, "max_channels": 1}'),
('VIDEO_DEVICE_TYPE', '5', '解码器', '视频解码器设备', 1, 5, '{"support_ptz":false, "support_audio":true, "max_channels": 1}'),
('VIDEO_DEVICE_TYPE', '6', '视频矩阵', '视频矩阵切换设备', 1, 6, '{"support_ptz":false, "support_audio":false, "max_channels": 64}'),
('VIDEO_DEVICE_TYPE', '7', '智能分析设备', 'AI智能分析服务器', 1, 7, '{"support_ptz":false, "support_audio":true, "max_channels": 16}'),
('VIDEO_DEVICE_TYPE', '8', '其他', '其他视频设备', 1, 8, '{"support_ptz":false, "support_audio":true, "max_channels": 1}');

-- 设备子类型数据
INSERT INTO t_common_dict_data (type_code, dict_code, dict_name, dict_desc, status, sort_order, extend_info) VALUES
('VIDEO_DEVICE_SUBTYPE', '11', '球形摄像机', '360度全景摄像头', 1, 1, '{"resolution_max":"4K", "night_vision":true}'),
('VIDEO_DEVICE_SUBTYPE', '12', '枪型摄像机', '固定视角摄像头', 1, 2, '{"resolution_max":"4K", "night_vision":true}'),
('VIDEO_DEVICE_SUBTYPE', '13', '半球摄像机', '半球形摄像头', 1, 3, '{"resolution_max":"4K", "night_vision":true}'),
('VIDEO_DEVICE_SUBTYPE', '14', '云台摄像机', '支持PTZ控制摄像头', 1, 4, '{"resolution_max":"4K", "ptz_support":true}'),
('VIDEO_DEVICE_SUBTYPE', '21', '嵌入式NVR', '一体化NVR设备', 1, 11, '{"channels":"4-8", "storage_max":"8TB"}'),
('VIDEO_DEVICE_SUBTYPE', '22', '机架式NVR', '机架式NVR设备', 1, 12, '{"channels":"16-32", "storage_max":"64TB"}'),
('VIDEO_DEVICE_SUBTYPE', '23', '高清NVR', '高清NVR设备', 1, 13, '{"channels":"8-16", "storage_max":"32TB"}'),
('VIDEO_DEVICE_SUBTYPE', '31', '嵌入式DVR', '一体化DVR设备', 1, 21, '{"channels":"4-8", "storage_max":"4TB"}'),
('VIDEO_DEVICE_SUBTYPE', '32', '机架式DVR', '机架式DVR设备', 1, 22, '{"channels":"8-16", "storage_max":"16TB"}'),
('VIDEO_DEVICE_SUBTYPE', '33', '网络DVR', '网络DVR设备', 1, 23, '{"channels":"16-32", "storage_max":"32TB"}'),
('VIDEO_DEVICE_SUBTYPE', '41', 'H.264编码器', 'H.264视频编码器', 1, 31, '{"max_resolution":"1080P", "max_fps":60}'),
('VIDEO_DEVICE_SUBTYPE', '42', 'H.265编码器', 'H.265视频编码器', 1, 32, '{"max_resolution":"4K", "max_fps":60}'),
('VIDEO_SUBTYPE', '51', 'H.264解码器', 'H.264视频解码器', 1, 41, '{"max_resolution":"1080P", "max_channels":1}'),
('VIDEO_SUBTYPE', '52', 'H.265解码器', 'H.265视频解码器', 1, 42, '{"max_resolution":"4K", "max_channels":1}'),
('VIDEO_SUBTYPE', '53', '多路解码器', '多路视频解码器', 1, 43, '{"max_channels":16, "max_resolution":"4K"}'),
('VIDEO_SUBTYPE', '61', '模拟矩阵', '模拟视频矩阵设备', 1, 51, '{"max_inputs":32, "max_outputs":32}'),
('VIDEO_SUBTYPE', '62', '数字矩阵', '数字视频矩阵设备', 1, 52, '{"max_inputs":64, "max_outputs":64}'),
('VIDEO_SUBTYPE', '63', '混合矩阵', '模拟数字混合矩阵', 1, 53, '{"max_analog_inputs":16, "max_digital_inputs":32, "max_outputs":48}'),
('VIDEO_SUBTYPE', '71', '边缘计算设备', '边缘AI分析设备', 1, 61, '{"ai_capabilities":["face", "object", "behavior"], "max_channels":16}'),
('VIDEO_SUBTYPE', '72', 'GPU分析服务器', 'GPU智能分析服务器', 1, 62, '{"gpu_count":1, "ai_capabilities":["face", "object", "behavior", "traffic"], "max_channels":64}'),
('VIDEO_SUBTYPE', '73', '智能分析盒', '便携式智能分析设备', 1, 63, '{"ai_capabilities":["face", "object"], "max_channels":4}');

-- 流类型数据
INSERT INTO t_common_dict_data (type_code, dict_code, dict_name, dict_desc, status, sort_order, extend_info) VALUES
('VIDEO_STREAM_TYPE', 'main', '主流', '主视频流，高画质', 1, 1, '{"quality":"high", "resolution":"1080P", "bitrate":"2048-4096"}'),
('VIDEO_STREAM_TYPE', 'sub', '子流', '子视频流，低码率', 1, 2, '{"quality":"medium", "resolution":"720P", "bitrate":"512-1024"}'),
('VIDEO_STREAM_TYPE', 'mobile', '移动流', '移动设备优化流', 1, 3, '{"quality":"low", "resolution":"480P", "bitrate":"256-512"}');

-- 流协议数据
INSERT INTO t_common_dict_data (type_code, dict_code, dict_name, dict_desc, status, sort_order, extend_info) VALUES
('VIDEO_STREAM_PROTOCOL', '1', 'RTSP', 'Real-Time Streaming Protocol实时流协议', 1, 1, '{"port":554, "transport":"TCP/UDP", "authentication":"Basic/Digest"}'),
('VIDEO_STREAM_PROTOCOL', '2', 'RTMP', 'Real-Time Messaging Protocol实时消息传输协议', 1, 2, '{"port":1935, "transport":"TCP", "authentication":"None"}'),
('VIDEO_STREAM_PROTOCOL', '3', 'HLS', 'HTTP Live Streaming基于HTTP的自适应比特率流媒体传输协议', 1, 3, '{"port":80, "transport":"HTTP", "segment_duration":10}'),
('VIDEO_STREAM_PROTOCOL', '4', 'WebRTC', 'Web Real-Time Communication网页实时通信技术', 1, 4, '{"port":443, "transport":"HTTPS", "encryption":"SRTP"}'),
('VIDEO_STREAM_PROTOCOL', '5', 'HTTP-FLV', 'HTTP-based Flash Video基于HTTP的Flash视频流协议', 1, 5, '{"port":80, "transport":"HTTP", "flv_format":true}');

-- 录像类型数据
INSERT INTO t_common_dict_data (type_code, dict_code, dict_name, dict_desc, status, sort_order, extend_info) VALUES
('VIDEO_RECORDING_TYPE', 'manual', '手动录像', '用户手动触发的录像', 1, 1, '{"trigger":"user_action", "priority":"medium"}'),
('VIDEO_RECORDING_TYPE', 'scheduled', '定时录像', '按预定计划自动录像', 1, 2, '{"trigger":"schedule", "priority":"medium"}'),
('VIDEO_RECORDING_TYPE', 'event', '事件录像', '事件触发的录像', 1, 3, '{"trigger":"event_detection", "priority":"high"}'),
('VIDEO_RECORDING_TYPE', 'alarm', '报警录像', '报警触发的录像', 1, 4, '{"trigger":"alarm", "priority":"highest"}');

-- 视频质量数据
INSERT INTO t_common_dict_data (type_code, dict_code, dict_name, dict_desc, status, sort_order, extend_info) VALUES
('VIDEO_QUALITY', 'high', '高清', '1080P高清视频质量', 1, 1, '{"resolution":"1920x1080", "bitrate":"4096-8192", "fps":30}'),
('VIDEO_QUALITY', 'medium', '标清', '720P标清视频质量', 1, 2, '{"resolution":"1280x720", "bitrate":"2048-4096", "fps":25}'),
('VIDEO_QUALITY', 'low', '流畅', '480P流畅视频质量', 1, 3, '{"resolution":"854x480", "bitrate":"512-1024", "fps":15}');

-- 事件类型数据
INSERT INTO t_common_dict_data (type_code, dict_code, dict_name, dict_desc, status, sort_order, extend_info) VALUES
('VIDEO_EVENT_TYPE', 'motion', '运动检测', '检测画面中的运动物体', 1, 1, '{"sensitivity":"adjustable", "min_object_size":"person"}'),
('VIDEO_EVENT_TYPE', 'movement', '移动检测', '检测物体移动轨迹', 1, 2, '{"sensitivity":"adjustable", "track_objects":true}'),
('VIDEO_EVENT_TYPE', 'face', '人脸检测', '检测人脸特征', 1, 3, '{"accuracy":"high", "face_recognition":false}'),
('VIDEO_EVENT_TYPE', 'face-recognition', '人脸识别', '识别已知人脸', 1, 4, '{"accuracy":"very_high", "database_required":true}'),
('VIDEO_EVENT_TYPE', 'vehicle', '车辆检测', '检测车辆特征', 1, 5, '{"vehicle_types":["car","truck","bus","motorcycle"], "license_plate":false}'),
('VIDEO_EVENT_TYPE', 'vehicle-license', '车牌识别', '识别车牌号码', 1, 6, '{"accuracy":"high", "regions_support":"CN"}'),
('VIDEO_EVENT_TYPE', 'cross-line', '越界检测', '检测跨越虚拟线', 1, 7, '{"line_direction":"both", "detection_zones":4}'),
('VIDEO_EVENT_TYPE', 'intrusion', '入侵检测', '检测进入禁止区域', 1, 8, '{"zone_type":"polygon", "min_intrusion_time":3}'),
('VIDEO_EVENT_TYPE', 'loitering', '徘徊检测', '检测长时间停留', 1, 9, '{"min_loiter_time":30, "area_type":"polygon"}'),
('VIDEO_EVENT_TYPE', 'crowd', '人群聚集', '检测人群密度', 1, 10, '{"density_threshold":"medium", "count_people":true}'),
('VIDEO_EVENT_TYPE', 'violence', '暴力检测', '检测暴力行为', 1, 11, '{"violence_types":["fighting","assault"], "accuracy":"medium"}'),
('VIDEO_EVENT_TYPE', 'fire', '火灾检测', '检测火焰和烟雾', 1, 12, '{"detection_types":["fire","smoke"], "sensitivity":"high"}'),
('VIDEO_EVENT_TYPE', 'smoke', '烟雾检测', '检测烟雾特征', 1, 13, '{"color_analysis":true, "sensitivity":"high"}'),
('VIDEO_EVENT_TYPE', 'object-detection', '物体检测', '检测通用物体', 1, 14, '{"object_categories":80, "custom_training":false}');

-- 任务类型数据
INSERT INTO t_common_dict_data (type_code, dict_code, dict_name, dict_desc, status, sort_order, extend_info) VALUES
('VIDEO_TASK_TYPE', 'recording', '录像任务', '视频录像相关任务', 1, 1, '{"default_retry":3, "timeout":300}'),
('VIDEO_TASK_TYPE', 'backup', '备份任务', '视频备份相关任务', 1, 2, '{"default_retry":3, "timeout":600}'),
('VIDEO_TASK_TYPE', 'cleanup', '清理任务', '过期文件清理任务', 1, 3, '{"default_retry":1, "timeout":120}'),
('VIDEO_TASK_TYPE', 'analysis', '分析任务', '视频智能分析任务', 1, 4, '{"default_retry":3, "timeout":1800}'),
('VIDEO_TASK_TYPE', 'report', '报告任务', '统计分析报告任务', 1, 5, '{"default_retry":3, "timeout":300}');

-- 访问级别数据
INSERT INTO t_common_dict_data (type_code, dict_code, dict_name, dict_desc, status, sort_order, extend_info) VALUES
('VIDEO_ACCESS_LEVEL', '1', '公开', '公开级别，无限制访问', 1, 1, '{"color":"green", "description":"所有用户可访问"}'),
('VIDEO_ACCESS_LEVEL', '2', '内部', '内部级别，需要认证', 1, 2, '{"color":"blue", "description":"需要登录认证的用户可访问"}'),
('VIDEO_ACCESS_LEVEL', '3', '机密', '机密级别，需要授权', 1, 3, '{"color":"yellow", "description":"需要特定权限的用户可访问"}'),
('VIDEO_ACCESS_LEVEL', '4', '秘密', '秘密级别，需要审批', 1, 4, '{"color":"red", "description":"需要审批的高权限用户可访问"}');

-- ============================================================
-- 插入示例设备数据
-- ============================================================

-- 示例摄像头设备
INSERT INTO t_video_device (
    device_code, device_name, device_type, device_sub_type, manufacturer, model,
    ip_address, port, username, password, area_id, area_name,
    longitude, latitude, altitude, resolution, max_frame_rate, max_bitrate,
    video_codec, audio_codec, audio_enabled, ptz_enabled, ptz_preset_count,
    ptz_cruise_enabled, ai_enabled, ai_features, smart_detection,
    face_detection, motion_detection, device_status, online_time,
    recording_enabled, recording_mode, access_level, view_permission,
    control_permission, config_permission, create_user_id, create_user_name,
    create_time, update_time, deleted_flag, version
) VALUES
('CAM_001', '主入口摄像头', 1, 14, '海康威视', 'DS-2CD2043G0-I',
    '192.168.1.101', 80, 'admin', 'encrypted_password', 1, '主入口',
    116.397128, 39.916527, 50.0, '1920x1080', 30, 4096,
    'H.264', 'AAC', 1, 1, 8,
    1, 1, '[{"type":"motion","enabled":true},{"type":"cross_line","enabled":true}]',
    1, 0, 1, 1, 1, '2025-12-16 10:00:00',
    1, 0, 2, 1, 0, 0,
    1, '系统管理员', NOW(), NOW(), 0, 1),

('CAM_002', '停车场摄像头', 1, 13, '大华股份', 'DH-IPC-HFW5831E-SE',
    '192.168.1.102', 80, 'admin', 'encrypted_password', 1, '停车场',
    116.397129, 39.916529, 45.0, '1920x1080', 25, 3072,
    'H.265', 'AAC', 1, 0, 0,
    0, 1, '[{"type":"motion","enabled":true},{"type":"vehicle","enabled":true}]',
    0, 1, 1, 0, 0, '2025-12-16 10:05:00',
    1, 2, 2, 1, 0, 0,
    1, '系统管理员', NOW(), NOW(), 0, 1),

('CAM_003', '办公区摄像头', 1, 14, '宇视科技', 'IPC-B3244R-ZS',
    '192.168.1.103', 80, 'admin', 'encrypted_password', 2, '办公区',
    116.397130, 39.916530, 48.0, '1280x720', 30, 2048,
    'H.264', 'AAC', 1, 1, 6,
    1, 1, '[{"type":"motion","enabled":true},{"type":"face","enabled":false}]',
    1, 0, 0, 1, 0, '2025-12-16 10:10:00',
    1, 1, 1, 1, 0, 0,
    1, '系统管理员', NOW(), NOW(), 0, 1);

-- 示例NVR设备
INSERT INTO t_video_device (
    device_code, device_name, device_type, device_sub_type, manufacturer, model,
    ip_address, port, username, password, area_id, area_name,
    longitude, latitude, altitude, recording_mode, max_bitrate,
    video_codec, audio_codec, audio_enabled, recording_enabled,
    access_level, view_permission, control_permission, config_permission,
    create_user_id, create_user_name, create_time, update_time, deleted_flag, version
) VALUES
('NVR_001', '主NVR设备', 2, 21, '海康威视', 'DS-8632N-R16',
    '192.168.1.200', 80, 'admin', 'encrypted_password', 1, '监控中心',
    116.397127, 39.916527, 52.0, 1, 8192,
    'H.264', 'AAC', 1, 1, 1,
    2, 1, 0, 0,
    1, '系统管理员', NOW(), NOW(), 0, 1);

-- ============================================================
-- 插入示例区域设备关联数据
-- ============================================================

-- 假设区域ID为1-5（从公共模块中获取）
-- 如果区域表不存在，可以使用以下SQL创建
-- INSERT IGNORE INTO t_common_area (area_id, area_name, area_code, parent_id, level, status, create_time, update_time) VALUES
-- (1, '总部大楼', 'HQ_BUILDING', 0, 1, 1, NOW(), NOW()),
-- (2, '1楼大厅', 'FLOOR_1', 1, 2, 1, NOW(), NOW()),
-- (3, '2楼办公区', 'FLOOR_2', 1, 2, 1, NOW(), NOW()),
-- (4, '3楼会议区', 'FLOOR_3', 1, 2, 1, NOW(), NOW()),
-- (5, '4楼技术中心', 'FLOOR_4', 1, 2, 1, NOW(), NOW());

-- 区域设备关联数据
INSERT INTO t_area_device_relation (
    area_id, device_id, device_code, device_name, device_type, business_module,
    business_attributes, relation_status, priority, access_permission,
    view_permission, control_permission, config_permission,
    real_time_monitoring, recording_enabled, playback_enabled,
    alarm_enabled, ptz_control_enabled, effective_time,
    create_user_id, create_user_name, create_time, update_time,
    deleted_flag, version
) VALUES
-- 主楼大厅区域设备
(1, 1, 'CAM_001', '主入口摄像头', 4, 'video',
    '{"stream_type":"main","quality":"high","ptz_control":true}', 1, 1,
    1, 1, 0, 0, 1, 1, 1, 1, 1,
    NULL, 1, '系统管理员', NOW(), NOW(), 0, 1),
(1, 2, 'CAM_002', '停车场摄像头', 4, 'video',
    '{"stream_type":"sub","quality":"medium","recording":"24h"}', 1, 2,
    1, 1, 0, 0, 1, 1, 1, 0, 1,
    NULL, 1, '系统管理员', NOW(), NOW(), 0, 1),

-- 办公区区域设备
(3, 3, 'CAM_003', '办公区摄像头', 4, 'video',
    '{"stream_type":"main","quality":"high","ptz_control":true,"recording":"business_hours"}', 1, 1,
    1, 1, 0, 0, 1, 1, 1, 1, 1,
    NULL, 1, '系统管理员', NOW(), NOW(), 0, 1),

-- 监控中心区域设备
(1, 4, 'NVR_001', '主NVR设备', 4, 'video',
    '{"capacity":"16通道","storage_capacity":"8TB"}', 1, 1,
    1, 1, 1, 1, 1, 1, 1, 1, 0,
    NULL, 1, '系统管理员', NOW(), NOW(), 0, 1);

-- ============================================================
-- 插入示例任务调度数据
-- ============================================================

-- 录像调度任务
INSERT INTO t_video_task_schedule (
    task_name, task_type, task_category, task_description, task_priority,
    cron_expression, schedule_type, schedule_interval, start_time, end_time,
    execute_once, retry_count, retry_interval, task_status,
    notification_enabled, notification_on_success, notification_on_failure,
    create_time, update_time, deleted_flag, version
) VALUES
('定时录像任务', 'recording', 'scheduled', '按计划执行定时录像任务', 2,
    '0 0 18 * * ?', 'cron', 0, NULL, NULL, 0,
    3, 60, 'pending',
    1, 0, 1, NOW(), NOW(), 0, 1),

('录像备份任务', 'backup', 'scheduled', '定期备份录像文件', 3,
    '0 0 2 * * ?', 'cron', 0, NULL, NULL, 0,
    3, 1800, 'pending',
    1, 0, 1, NOW(), NOW(), 0, 1),

('过期录像清理', 'cleanup', 'scheduled', '清理30天前的过期录像', 3,
    '0 0 3 * * ?', 'cron', 0, NULL, NULL, 0,
    1, 300, 'pending',
    1, 0, 1, NOW(), NOW(), 0, 1);

-- ============================================================
-- 插入示例用户权限数据
-- ============================================================

-- 假设用户ID为1-3（从公共模块中获取）
-- 如果用户表不存在，可以使用以下SQL创建
-- INSERT IGNORE INTO t_common_user (user_id, user_name, actual_name, password, phone, email, status, create_time, update_time) VALUES
-- (1, 'admin', '系统管理员', 'encrypted_password', '13800138000', 'admin@ioe-dream.com', 1, NOW(), NOW()),
-- (2, 'operator', '视频操作员', 'encrypted_password', '13800138001', 'operator@ioe-dream.com', 1, NOW(), NOW()),
-- (3, 'viewer', '视频查看员', 'encrypted_password', '13800138002', 'viewer@ioe-dream.com', 1, NOW(), NOW());

-- 用户权限数据
INSERT INTO t_video_device_user_permission (
    device_id, user_id, user_name, view_permission, control_permission,
    config_permission, real_time_monitoring, recording_permission, playback_permission,
    snapshot_permission, ptz_control_permission, access_level, permission_level,
    permission_status, effective_time, expire_time, grant_user_id, grant_user_name,
    grant_time, create_time, update_time, deleted_flag, version
) VALUES
-- 管理员权限（所有设备）
(1, 1, '系统管理员', 1, 1, 1, 1, 1, 1, 1, 1, 4, 3,
    1, NULL, NULL, 1, '系统管理员', '2025-12-16 10:00:00', NOW(), NOW(), 0, 1),
(2, 1, '系统管理员', 1, 1, 1, 1, 1, 1, 1, 1, 4, 3,
    1, NULL, NULL, 1, '系统管理员', '2025-12-16 10:00:00', NOW(), NOW(), 0, 1),
(3, 1, '系统管理员', 1, 1, 1, 1, 1, 1, 1, 1, 4, 3,
    1, NULL, NULL, 1, '系统管理员', '2025-12-16 10:00:00', NOW(), NOW(), 0, 1),

-- 操作员权限（部分设备）
(1, 1, '系统管理员', 1, 1, 0, 1, 1, 1, 1, 0, 2, 2,
    1, NULL, NULL, 1, '系统管理员', '2025-12-16 10:00:00', NOW(), NOW(), 0, 1),
(2, 1, '系统管理员', 1, 1, 0, 1, 1, 1, 1, 0, 2, 2,
    1, NULL, NULL, 1, '系统管理员', '2025-12-16 10:00:00', NOW(), NOW(), 0, 1),
(3, 1, '系统管理员', 1, 1, 0, 1, 1, 1, 1, 0, 2, 2,
    1, NULL, NULL, 1, '系统管理员', '2025-12-16 10:00:00', NOW(), NOW(), 0, 1),

(1, 2, '系统管理员', 1, 1, 0, 1, 1, 1, 1, 0, 2, 2,
    1, NULL, NULL, 1, '系统管理员', '2025-12-16 10:00:00', NOW(), NOW(), 0, 1),
(2, 2, '系统管理员', 1, 1, 0, 1, 1, 1, 1, 0, 2, 2,
    1, NULL, NULL, 1, '系统管理员', '2025-12-16 10:00:00', NOW(), NOW(), 0, 1),
(3, 2, '系统管理员', 1, 1, 0, 1, 1, 1, 1, 0, 2, 2,
    1, NULL, NULL, 1, '系统管理员', '2025-12-16 10:00:00', NOW(), NOW(), 0, 1),

(1, 3, '系统管理员', 1, 1, 0, 1, 1, 1, 1, 0, 2, 2,
    1, NULL, NULL, 1, '系统管理员', '2025-12-16 10:00:00', NOW(), NOW(), 0, 1),
(2, 3, '系统管理员', 1, 1, 0, 1, 1, 1, 1, 0, 2, 2,
    1, NULL, NULL, 1, '系统管理员', '2025-12-16 10:00:00', NOW(), NOW(), 0, 1),
(3, 3, '系统管理员', 1, 1, 0, 1, 1, 1, 1, 0, 2, 2,
    1, NULL, NULL, 1, '系统管理员', '2025-12-16 10:00:00', NOW(), NOW(), 0, 1),

(1, 4, '系统管理员', 1, 1, 1, 1, 1, 1, 1, 0, 4, 3,
    1, NULL, NULL, 1, '系统管理员', '2025-12-16 10:00:00', NOW(), NOW(), 0, 1),

-- 查看员权限（只读权限）
(1, 1, '系统管理员', 1, 0, 0, 1, 0, 1, 1, 0, 1, 1,
    1, NULL, NULL, 1, '系统管理员', '2025-12-16 10:00:00', NOW(), NOW(), 0, 1),
(2, 1, '系统管理员', 1, 0, 0, 1, 0, 1, 1, 0, 1, 1,
    1, NULL, NULL, 1, '系统管理员', '2025-12-16 10:00:00', NOW(), NOW(), 0, 1),
(3, 1, '系统管理员', 1, 0, 0, 1, 0, 1, 1, 0, 1, 1,
    1, NULL, NULL, 1, '系统管理员', '2025-12-16 10:00:00', NOW(), NOW(), 0, 1),

(1, 2, '系统管理员', 1, 0, 0, 1, 0, 1, 1, 0, 1, 1,
    1, NULL, NULL, 1, '系统管理员', '2025-12-16 10:00:00', NOW(), NOW(), 0, 1),
(2, 2, '系统管理员', 1, 0, 0, 1, 0, 1, 1, 0, 1, 1,
    1, NULL, NULL, 1, '系统管理员', '2025-12-16 10:00:00', NOW(), NOW(), 0, 1),
(3, 2, '系统管理员', 1, 0, 0, 1, 0, 1, 1, 0, 1, 1,
    1, NULL, NULL, 1, '系统管理员', '2025-12-16 10:00:00', NOW(), NOW(), 0, 1),

(1, 3, '系统管理员', 1, 0, 0, 1, 0, 1, 1, 0, 1, 1,
    1, NULL, NULL, 1, '系统管理员', '2025-12-16 10:00:00', NOW(), NOW(), 0, 1),
(2, 3, '系统管理员', 1, 0, 0, 1, 0, 1, 1, 0, 1, 1,
    1, NULL, NULL, 1, '系统管理员', '2025-12-16 10:00:00', NOW(), NOW(), 0, 1),
(3, 3, '系统管理员', 1, 0, 0, 1, 0, 1, 1, 0, 1, 1,
    1, NULL, NULL, 1, '系统管理员', '2025-12-16 10:00:00', NOW(), NOW(), 0, 1);

-- ============================================================
-- 创建索引优化查询性能
-- ============================================================

-- 为大表创建复合索引
ALTER TABLE t_video_device ADD INDEX idx_device_composite (device_type, device_status, deleted_flag, create_time);
ALTER TABLE t_video_stream ADD INDEX idx_stream_composite (device_id, stream_status, deleted_flag, create_time);
ALTER TABLE t_video_recording ADD INDEX idx_recording_composite (device_id, recording_type, recording_start_time, deleted_flag);
ALTER TABLE t_video_event ADD INDEX idx_event_composite (device_id, event_type, event_time, deleted_flag);
ALTER TABLE t_video_operation_log ADD INDEX idx_log_composite (device_id, operation_type, execute_time, user_id);

-- 为时间字段创建索引以优化时间范围查询
ALTER TABLE t_video_stream ADD INDEX idx_time_range (start_time, end_time);
ALTER TABLE t_video_recording ADD INDEX idx_time_range (recording_start_time, recording_end_time);
ALTER TABLE t_video_event ADD INDEX idx_time_range (event_time, detection_time);
ALTER TABLE t_video_operation_log ADD INDEX idx_time_range (execute_time);

-- ============================================================
-- 表创建完成日志
-- ============================================================
INSERT INTO flyway_schema_history (
    installed_rank,
    version,
    description,
    type,
    script,
    installed_by,
    installed_on,
    execution_time,
    success
) VALUES (
    3,
    '20251216.003',
    'Insert initial data for video service',
    'SQL',
    'V20251216__003_insert_initial_data.sql',
    'flyway',
    NOW(),
    0,
    1
);