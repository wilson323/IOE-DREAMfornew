-- ============================================================
-- IOE-DREAM 视频监控微服务数据库迁移脚本
-- 版本: V20251216.001
-- 描述: 创建视频监控相关核心表结构
-- 服务: ioedream-video-service
-- 技术栈: Spring Boot 3.5.8 + Spring Cloud Alibaba 2025.0.0 + Flyway 9.x
-- 数据库: MySQL 8.0+
-- 创建时间: 2025-12-16
-- 作者: IOE-DREAM Team
-- ============================================================

-- 设置字符集和排序规则
SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ============================================================
-- 1. 视频设备表 (t_video_device)
-- ============================================================
CREATE TABLE t_video_device (
    -- 主键
    device_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '设备ID',

    -- 基础信息
    device_code VARCHAR(50) NOT NULL UNIQUE COMMENT '设备编号',
    device_name VARCHAR(100) NOT NULL COMMENT '设备名称',
    device_type INT NOT NULL COMMENT '设备类型：1-摄像头 2-NVR 3-DVR 4-编码器 5-解码器 6-视频矩阵 7-智能分析设备 8-其他',
    device_sub_type INT COMMENT '设备子类型',
    manufacturer VARCHAR(50) COMMENT '厂商',
    model VARCHAR(100) COMMENT '型号',
    serial_number VARCHAR(100) COMMENT '序列号',
    firmware_version VARCHAR(50) COMMENT '固件版本',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    port INT COMMENT '端口号',
    username VARCHAR(50) COMMENT '用户名',
    password VARCHAR(200) COMMENT '密码(加密存储)',

    -- 位置信息
    area_id BIGINT COMMENT '区域ID',
    area_name VARCHAR(100) COMMENT '区域名称',
    location_description VARCHAR(500) COMMENT '位置描述',
    longitude DECIMAL(10,6) COMMENT '经度',
    latitude DECIMAL(10,6) COMMENT '纬度',
    altitude DECIMAL(8,2) COMMENT '海拔高度(米)',

    -- 技术参数
    resolution VARCHAR(20) COMMENT '分辨率',
    max_frame_rate INT COMMENT '最大帧率(fps)',
    max_bitrate INT COMMENT '最大码率(kbps)',
    video_codec VARCHAR(20) COMMENT '视频编码格式',
    audio_codec VARCHAR(20) COMMENT '音频编码格式',
    audio_enabled TINYINT DEFAULT 0 COMMENT '是否启用音频：0-否 1-是',

    -- PTZ控制
    ptz_enabled TINYINT DEFAULT 0 COMMENT '是否支持PTZ：0-否 1-是',
    ptz_preset_count INT DEFAULT 0 COMMENT '预置位数量',
    ptz_cruise_enabled TINYINT DEFAULT 0 COMMENT '是否支持巡航：0-否 1-是',
    ptz_speed_level INT DEFAULT 0 COMMENT 'PTZ速度等级',

    -- 智能功能
    ai_enabled TINYINT DEFAULT 0 COMMENT '是否启用AI分析：0-否 1-是',
    ai_features TEXT COMMENT 'AI功能列表(JSON格式)',
    smart_detection TINYINT DEFAULT 0 COMMENT '智能检测：0-否 1-是',
    face_detection TINYINT DEFAULT 0 COMMENT '人脸检测：0-否 1-是',
    motion_detection TINYINT DEFAULT 0 COMMENT '运动检测：0-否 1-是',
    cross_line_detection TINYINT DEFAULT 0 COMMENT '越界检测：0-否 1-是',

    -- 状态信息
    device_status TINYINT DEFAULT 1 COMMENT '设备状态：0-离线 1-在线 2-故障 3-维护',
    online_time DATETIME COMMENT '上线时间',
    offline_time DATETIME COMMENT '离线时间',
    last_heartbeat_time DATETIME COMMENT '最后心跳时间',
    cpu_usage DECIMAL(5,2) COMMENT 'CPU使用率(%)',
    memory_usage DECIMAL(5,2) COMMENT '内存使用率(%)',
    disk_usage DECIMAL(5,2) COMMENT '磁盘使用率(%)',
    network_quality TINYINT COMMENT '网络质量：1-优秀 2-良好 3-一般 4-较差',

    -- 录像功能
    recording_enabled TINYINT DEFAULT 0 COMMENT '是否启用录像：0-否 1-是',
    recording_mode TINYINT DEFAULT 0 COMMENT '录像模式：0-手动 1-定时 2-移动侦测 3-报警',
    recording_schedule TEXT COMMENT '录像计划(JSON格式)',
    pre_record_duration INT DEFAULT 0 COMMENT '预录时长(秒)',
    post_record_duration INT DEFAULT 0 COMMENT '后录时长(秒)',

    -- 权限和安全
    access_level TINYINT DEFAULT 1 COMMENT '访问权限级别：1-公开 2-内部 3-机密 4-秘密',
    view_permission TINYINT DEFAULT 1 COMMENT '查看权限：0-禁止 1-允许',
    control_permission TINYINT DEFAULT 0 COMMENT '控制权限：0-禁止 1-允许',
    config_permission TINYINT DEFAULT 0 COMMENT '配置权限：0-禁止 1-允许',

    -- 扩展属性
    extended_attributes TEXT COMMENT '扩展属性(JSON格式)',
    custom_fields TEXT COMMENT '自定义字段(JSON格式)',

    -- 审计字段
    create_user_id BIGINT COMMENT '创建人ID',
    create_user_name VARCHAR(50) COMMENT '创建人姓名',
    update_user_id BIGINT COMMENT '更新人ID',
    update_user_name VARCHAR(50) COMMENT '更新人姓名',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    version INT NOT NULL DEFAULT 1 COMMENT '版本号(乐观锁)',

    -- 索引
    INDEX idx_device_code (device_code),
    INDEX idx_device_type (device_type),
    INDEX idx_area_id (area_id),
    INDEX idx_device_status (device_status),
    INDEX idx_ip_address (ip_address),
    INDEX idx_deleted_flag (deleted_flag),
    INDEX idx_create_time (create_time),
    INDEX idx_device_name (device_name),
    INDEX idx_manufacturer (manufacturer),

    -- 唯一索引
    UNIQUE INDEX uk_device_serial_number (serial_number),
    UNIQUE INDEX uk_device_code (device_code),

    -- 复合索引
    INDEX idx_device_type_status (device_type, device_status, deleted_flag),
    INDEX idx_area_status_time (area_id, device_status, create_time),
    INDEX idx_online_status_time (device_status, online_time, deleted_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频设备表';

-- ============================================================
-- 2. 视频流表 (t_video_stream)
-- ============================================================
CREATE TABLE t_video_stream (
    -- 主键
    stream_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '流ID',

    -- 关联信息
    device_id BIGINT NOT NULL COMMENT '设备ID',
    device_code VARCHAR(50) NOT NULL COMMENT '设备编号',
    device_name VARCHAR(100) NOT NULL COMMENT '设备名称',
    channel_id BIGINT COMMENT '通道ID',
    channel_name VARCHAR(50) COMMENT '通道名称',

    -- 流信息
    stream_key VARCHAR(100) NOT NULL UNIQUE COMMENT '流标识',
    stream_type VARCHAR(20) NOT NULL COMMENT '流类型：main-主流 sub-子流 mobile-移动流',
    protocol INT NOT NULL COMMENT '流协议：1-RTSP 2-RTMP 3-HLS 4-WebRTC 5-HTTP-FLV',
    stream_status TINYINT DEFAULT 1 COMMENT '流状态：1-活跃 2-暂停 3-停止 4-错误',

    -- 质量参数
    quality VARCHAR(20) DEFAULT 'medium' COMMENT '视频质量：high-高清 medium-标清 low-流畅',
    resolution VARCHAR(20) COMMENT '分辨率',
    frame_rate INT COMMENT '帧率(fps)',
    bitrate INT COMMENT '码率(kbps)',
    codec VARCHAR(20) COMMENT '编码格式',

    -- 音频参数
    audio_enabled TINYINT DEFAULT 0 COMMENT '是否启用音频：0-否 1-是',
    audio_codec VARCHAR(20) COMMENT '音频编码',
    audio_channels INT DEFAULT 1 COMMENT '音频声道数',
    audio_sample_rate INT COMMENT '音频采样率(Hz)',

    -- 地址信息
    rtsp_url VARCHAR(500) COMMENT 'RTSP地址',
    rtmp_url VARCHAR(500) COMMENT 'RTMP地址',
    hls_url VARCHAR(500) COMMENT 'HLS地址',
    webrtc_url VARCHAR(500) COMMENT 'WebRTC地址',
    flv_url VARCHAR(500) COMMENT 'HTTP-FLV地址',
    stream_urls TEXT COMMENT '流地址列表(JSON格式)',

    -- 录制信息
    recording TINYINT DEFAULT 0 COMMENT '是否录制中：0-未录制 1-录制中',
    record_enabled TINYINT DEFAULT 0 COMMENT '是否启用录制：0-否 1-是',
    record_file_path VARCHAR(500) COMMENT '录制文件路径',
    record_start_time DATETIME COMMENT '录制开始时间',
    record_duration INT COMMENT '录制时长(秒)',

    -- 会话信息
    session_id VARCHAR(100) COMMENT '会话ID',
    session_status VARCHAR(20) COMMENT '会话状态：active-paused-closed-error',
    client_ip VARCHAR(45) COMMENT '客户端IP',
    user_agent VARCHAR(500) COMMENT '用户代理',
    session_timeout INT COMMENT '会话超时时间(分钟)',

    -- 性能统计
    viewer_count INT DEFAULT 0 COMMENT '观看人数',
    max_viewer_count INT DEFAULT 0 COMMENT '最大观看人数',
    dynamic_bitrate TINYINT DEFAULT 0 COMMENT '是否启用动态码率：0-否 1-是',
    current_bitrate INT COMMENT '当前码率(kbps)',
    network_quality VARCHAR(20) COMMENT '网络质量：excellent-good-fair-poor',
    latency INT COMMENT '网络延迟(毫秒)',
    packet_loss DECIMAL(5,2) COMMENT '丢包率(%)',
    jitter INT COMMENT '抖动(毫秒)',

    -- 时间信息
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    last_access_time DATETIME COMMENT '最后访问时间',

    -- 扩展参数
    extended_params TEXT COMMENT '扩展参数(JSON格式)',

    -- 审计字段
    create_user_id BIGINT COMMENT '创建人ID',
    create_user_name VARCHAR(50) COMMENT '创建人姓名',
    update_user_id BIGINT COMMENT '更新人ID',
    update_user_name VARCHAR(50) COMMENT '更新人姓名',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    version INT NOT NULL DEFAULT 1 COMMENT '版本号(乐观锁)',

    -- 索引
    INDEX idx_stream_key (stream_key),
    INDEX idx_device_id (device_id),
    INDEX idx_device_channel (device_id, channel_id),
    INDEX idx_stream_type (stream_type),
    INDEX idx_protocol (protocol),
    INDEX idx_stream_status (stream_status),
    INDEX idx_recording (recording),
    INDEX idx_quality (quality),
    INDEX idx_deleted_flag (deleted_flag),
    INDEX idx_create_time (create_time),
    INDEX idx_start_time (start_time),

    -- 复合索引
    INDEX idx_device_status (device_id, stream_status, deleted_flag),
    INDEX idx_device_type_status (device_id, stream_type, stream_status),
    INDEX idx_protocol_status (protocol, stream_status, deleted_flag),
    INDEX idx_recording_time (recording, start_time, deleted_flag),

    -- 外键约束
    FOREIGN KEY (device_id) REFERENCES t_video_device(device_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频流表';

-- ============================================================
-- 3. 视频监控表 (t_video_monitor)
-- ============================================================
CREATE TABLE t_video_monitor (
    -- 主键
    monitor_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '监控ID',

    -- 监控会话信息
    session_id VARCHAR(100) NOT NULL UNIQUE COMMENT '会话ID',
    session_name VARCHAR(100) COMMENT '会话名称',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    user_name VARCHAR(50) NOT NULL COMMENT '用户姓名',
    user_ip VARCHAR(45) COMMENT '用户IP',
    user_agent VARCHAR(500) COMMENT '用户代理',

    -- 监控配置
    screen_layout INT DEFAULT 1 COMMENT '屏幕布局：1-单屏 2-四分屏 3-九分屏 4-十六分屏 5-二十五分屏',
    monitor_mode TINYINT DEFAULT 1 COMMENT '监控模式：1-实时 2-回放 3-混合',
    auto_switch TINYINT DEFAULT 0 COMMENT '是否自动切换：0-否 1-是',
    switch_interval INT COMMENT '切换间隔(秒)',

    -- 监控设备列表
    device_list TEXT COMMENT '监控设备列表(JSON格式)',
    channel_list TEXT COMMENT '监控通道列表(JSON格式)',
    stream_list TEXT COMMENT '监控流列表(JSON格式)',

    -- 显示设置
    show_device_name TINYINT DEFAULT 1 COMMENT '是否显示设备名称：0-否 1-是',
    show_time TINYINT DEFAULT 1 COMMENT '是否显示时间：0-否 1-是',
    show_status TINYINT DEFAULT 1 COMMENT '是否显示状态：0-否 1-是',
    show_toolbar TINYINT DEFAULT 1 COMMENT '是否显示工具栏：0-否 1-是',
    theme_mode VARCHAR(20) DEFAULT 'light' COMMENT '主题模式：light-dark',

    -- 功能设置
    audio_enabled TINYINT DEFAULT 0 COMMENT '是否启用音频：0-否 1-是',
    record_enabled TINYINT DEFAULT 0 COMMENT '是否启用录制：0-否 1-是',
    snapshot_enabled TINYINT DEFAULT 1 COMMENT '是否启用截图：0-否 1-是',
    ptz_control_enabled TINYINT DEFAULT 1 COMMENT '是否启用PTZ控制：0-否 1-是',

    -- 性能统计
    cpu_usage DECIMAL(5,2) COMMENT 'CPU使用率(%)',
    memory_usage DECIMAL(5,2) COMMENT '内存使用率(%)',
    network_usage DECIMAL(10,2) COMMENT '网络使用量(KB)',
    display_fps DECIMAL(5,2) COMMENT '显示帧率(fps)',

    -- 网络状态
    network_status TINYINT DEFAULT 1 COMMENT '网络状态：1-优秀 2-良好 3-一般 4-较差',
    bandwidth_usage DECIMAL(10,2) COMMENT '带宽使用量(kbps)',
    packet_loss_rate DECIMAL(5,2) COMMENT '丢包率(%)',
    latency INT COMMENT '延迟(毫秒)',

    -- 会话状态
    session_status VARCHAR(20) DEFAULT 'active' COMMENT '会话状态：active-paused-closed-error',
    start_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    duration INT COMMENT '持续时间(秒)',
    last_active_time DATETIME COMMENT '最后活跃时间',

    -- 权限信息
    access_level TINYINT DEFAULT 1 COMMENT '访问权限级别：1-公开 2-内部 3-机密 4-秘密',
    max_concurrent_sessions INT COMMENT '最大并发会话数',

    -- 扩展配置
    extended_config TEXT COMMENT '扩展配置(JSON格式)',
    custom_settings TEXT COMMENT '自定义设置(JSON格式)',

    -- 审计字段
    create_user_id BIGINT COMMENT '创建人ID',
    create_user_name VARCHAR(50) COMMENT '创建人姓名',
    update_user_id BIGINT COMMENT '更新人ID',
    update_user_name VARCHAR(50) COMMENT '更新人姓名',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    version INT NOT NULL DEFAULT 1 COMMENT '版本号(乐观锁)',

    -- 索引
    INDEX idx_session_id (session_id),
    INDEX idx_user_id (user_id),
    INDEX idx_user_ip (user_ip),
    INDEX idx_session_status (session_status),
    INDEX idx_screen_layout (screen_layout),
    INDEX idx_deleted_flag (deleted_flag),
    INDEX idx_create_time (create_time),
    INDEX idx_start_time (start_time),
    INDEX idx_last_active_time (last_active_time),

    -- 复合索引
    INDEX idx_user_status (user_id, session_status, deleted_flag),
    INDEX idx_status_time (session_status, start_time, deleted_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频监控表';

-- ============================================================
-- 4. 视频PTZ控制表 (t_video_ptz)
-- ============================================================
CREATE TABLE t_video_ptz (
    -- 主键
    ptz_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'PTZ控制ID',

    -- 关联信息
    device_id BIGINT NOT NULL COMMENT '设备ID',
    device_code VARCHAR(50) NOT NULL COMMENT '设备编号',
    device_name VARCHAR(100) NOT NULL COMMENT '设备名称',
    channel_id BIGINT COMMENT '通道ID',
    channel_name VARCHAR(50) COMMENT '通道名称',

    -- 用户信息
    user_id BIGINT NOT NULL COMMENT '用户ID',
    user_name VARCHAR(50) NOT NULL COMMENT '用户姓名',
    user_ip VARCHAR(45) COMMENT '用户IP',
    user_agent VARCHAR(500) COMMENT '用户代理',

    -- 控制信息
    control_command VARCHAR(50) NOT NULL COMMENT '控制命令：UP/DOWN/LEFT/RIGHT/ZOOM_IN/ZOOM_OUT/FOCUS_NEAR/FOCUS_FAR/GOTO_PRESET/SET_PRESET/START_CRUISE/STOP_CRUISE',
    control_parameters TEXT COMMENT '控制参数(JSON格式)',
    control_speed INT COMMENT '控制速度等级',

    -- 位置信息
    pan_angle DECIMAL(6,2) COMMENT '水平角度(度)',
    tilt_angle DECIMAL(6,2) COMMENT '垂直角度(度)',
    zoom_level INT COMMENT '变焦级别',
    focus_level INT COMMENT '聚焦级别',

    -- 预置位信息
    preset_id INT COMMENT '预置位ID',
    preset_name VARCHAR(100) COMMENT '预置位名称',
    preset_enabled TINYINT DEFAULT 1 COMMENT '是否启用预置位：0-否 1-是',

    -- 巡航信息
    cruise_id VARCHAR(50) COMMENT '巡航ID',
    cruise_name VARCHAR(100) COMMENT '巡航名称',
    cruise_enabled TINYINT DEFAULT 0 COMMENT '是否启用巡航：0-否 1是',
    cruise_speed INT COMMENT '巡航速度',
    cruise_duration INT COMMENT '巡航时长(秒)',

    -- 执行结果
    execute_status VARCHAR(20) DEFAULT 'pending' COMMENT '执行状态：pending-processing-completed-failed-timeout',
    execute_result TEXT COMMENT '执行结果(JSON格式)',
    execute_time DATETIME COMMENT '执行时间',
    execute_duration INT COMMENT '执行耗时(毫秒)',

    -- 操作记录
    operation_type VARCHAR(50) COMMENT '操作类型：manual-auto-schedule',
    operation_source VARCHAR(50) COMMENT '操作来源：web-mobile-api',
    operation_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',

    -- 权限信息
    access_level TINYINT DEFAULT 1 COMMENT '访问权限级别：1-公开 2-内部 3-机密 4-秘密',
    permission_granted TINYINT DEFAULT 1 COMMENT '是否有权限：0-否 1-是',

    -- 扩展参数
    extended_params TEXT COMMENT '扩展参数(JSON格式)',
    custom_fields TEXT COMMENT '自定义字段(JSON格式)',

    -- 审计字段
    create_user_id BIGINT COMMENT '创建人ID',
    create_user_name VARCHAR(50) COMMENT '创建人姓名',
    update_user_id BIGINT COMMENT '更新人ID',
    update_user_name VARCHAR(50) COMMENT '更新人姓名',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    version INT NOT NULL DEFAULT 1 COMMENT '版本号(乐观锁)',

    -- 索引
    INDEX idx_device_id (device_id),
    INDEX idx_device_channel (device_id, channel_id),
    INDEX idx_user_id (user_id),
    INDEX idx_control_command (control_command),
    INDEX idx_execute_status (execute_status),
    INDEX idx_operation_time (operation_time),
    INDEX idx_deleted_flag (deleted_flag),

    -- 复合索引
    INDEX idx_device_command (device_id, control_command, deleted_flag),
    INDEX idx_user_command_time (user_id, control_command, operation_time, deleted_flag),
    INDEX idx_status_time (execute_status, execute_time, deleted_flag),

    -- 外键约束
    FOREIGN KEY (device_id) REFERENCES t_video_device(device_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频PTZ控制表';

-- ============================================================
-- 5. 视频录像表 (t_video_recording)
-- ============================================================
CREATE TABLE t_video_recording (
    -- 主键
    recording_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '录像ID',

    -- 关联信息
    device_id BIGINT NOT NULL COMMENT '设备ID',
    device_code VARCHAR(50) NOT NULL COMMENT '设备编号',
    device_name VARCHAR(100) NOT NULL COMMENT '设备名称',
    channel_id BIGINT COMMENT '通道ID',
    channel_name VARCHAR(50) COMMENT '通道名称',

    -- 录像文件信息
    file_name VARCHAR(255) NOT NULL COMMENT '录像文件名',
    file_path VARCHAR(500) NOT NULL COMMENT '录像文件路径',
    file_size BIGINT NOT NULL COMMENT '文件大小(字节)',
    file_format VARCHAR(10) NOT NULL COMMENT '文件格式：mp4/avi/flv/mkv',
    file_hash VARCHAR(64) COMMENT '文件哈希值(MD5)',

    -- 录像信息
    recording_type VARCHAR(20) NOT NULL COMMENT '录像类型：manual-manual scheduled-timed event-event alarm-alarm',
    recording_quality VARCHAR(20) DEFAULT 'medium' COMMENT '录像质量：high-高清 medium-标清 low-流畅',
    recording_trigger VARCHAR(50) COMMENT '录像触发方式：manual-timer-motion-alarm-event',
    recording_trigger_reason VARCHAR(200) COMMENT '录像触发原因',

    -- 视频参数
    resolution VARCHAR(20) COMMENT '分辨率',
    frame_rate INT COMMENT '帧率(fps)',
    bitrate INT COMMENT '码率(kbps)',
    video_codec VARCHAR(20) COMMENT '视频编码格式',
    audio_codec VARCHAR(20) COMMENT '音频编码格式',
    audio_enabled TINYINT DEFAULT 0 COMMENT '是否包含音频：0-否 1-是',

    -- 录像时间
    recording_start_time DATETIME NOT NULL COMMENT '录制开始时间',
    recording_end_time DATETIME COMMENT '录制结束时间',
    recording_duration INT NOT NULL COMMENT '录制时长(秒)',

    -- 存储信息
    storage_server VARCHAR(100) COMMENT '存储服务器',
    storage_location VARCHAR(200) COMMENT '存储位置',
    storage_path VARCHAR(500) COMMENT '存储路径',
    backup_status TINYINT DEFAULT 0 COMMENT '备份状态：0-未备份 1-备份中 2-已备份 3-备份失败',
    backup_time DATETIME COMMENT '备份时间',
    backup_path VARCHAR(500) COMMENT '备份路径',

    -- 重要标记
    important TINYINT DEFAULT 0 COMMENT '是否重要录像：0-否 1-是',
    important_time DATETIME COMMENT '标记重要时间',
    important_remark VARCHAR(500) COMMENT '重要备注',
    marker_id BIGINT COMMENT '标记人ID',
    marker_name VARCHAR(50) COMMENT '标记人姓名',

    -- 事件信息
    has_event TINYINT DEFAULT 0 COMMENT '是否包含事件：0-否 1-是',
    event_type VARCHAR(50) COMMENT '事件类型',
    event_description TEXT COMMENT '事件描述',
    event_time DATETIME COMMENT '事件发生时间',

    -- 完整性检查
    integrity_status TINYINT DEFAULT 0 COMMENT '完整性状态：0-未检查 1-完整 2-损坏 3-修复中 4-已修复',
    integrity_check_time DATETIME COMMENT '完整性检查时间',
    checksum_verified VARCHAR(64) COMMENT '验证的校验和',

    -- 访问统计
    download_count INT DEFAULT 0 COMMENT '下载次数',
    play_count INT DEFAULT 0 COMMENT '播放次数',
    last_play_time DATETIME COMMENT '最后播放时间',
    last_download_time DATETIME COMMENT '最后下载时间',

    -- 转码信息
    transcode_status TINYINT DEFAULT 0 COMMENT '转码状态：0-未转码 1-转码中 2-已转码 3-转码失败',
    transcode_formats TEXT COMMENT '转码格式列表(JSON格式)',
    transcode_time DATETIME COMMENT '转码时间',

    -- 播放信息
    thumbnail_path VARCHAR(500) COMMENT '缩略图路径',
    preview_path VARCHAR(500) COMMENT '预览视频路径',
    poster_url VARCHAR(500) COMMENT '海报URL',

    -- 权限信息
    access_level TINYINT DEFAULT 1 COMMENT '访问权限级别：1-公开 2-内部 3-机密 4-秘密',
    encrypted TINYINT DEFAULT 0 COMMENT '是否加密：0-否 1-是',
    encryption_algorithm VARCHAR(50) COMMENT '加密算法',
    watermark_info TEXT COMMENT '水印信息',

    -- 元数据
    metadata TEXT COMMENT '元数据(JSON格式)',
    tags TEXT COMMENT '标签(JSON数组)',
    categories TEXT COMMENT '分类(JSON数组)',
    search_keywords TEXT COMMENT '检索关键词(JSON数组)',

    -- 数据版本和同步
    data_version INT DEFAULT 1 COMMENT '数据版本',
    sync_status TINYINT DEFAULT 1 COMMENT '同步状态：0-未同步 1-已同步 2-同步失败',
    last_sync_time DATETIME COMMENT '最后同步时间',

    -- 生命周期管理
    storage_retention_days INT COMMENT '存储保留天数',
    expire_time DATETIME COMMENT '过期时间',
    auto_delete_time DATETIME COMMENT '自动删除时间',
    archive_status TINYINT DEFAULT 0 COMMENT '归档状态：0-未归档 1-归档中 2-已归档',

    -- 扩展属性
    extended_attributes TEXT COMMENT '扩展属性(JSON格式)',
    custom_fields TEXT COMMENT '自定义字段(JSON格式)',

    -- 审计字段
    create_user_id BIGINT COMMENT '创建人ID',
    create_user_name VARCHAR(50) COMMENT '创建人姓名',
    update_user_id BIGINT COMMENT '更新人ID',
    update_user_name VARCHAR(50) COMMENT '更新人姓名',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    version INT NOT NULL DEFAULT 1 COMMENT '版本号(乐观锁)',

    -- 索引
    INDEX idx_device_id (device_id),
    INDEX idx_device_channel (device_id, channel_id),
    INDEX idx_file_name (file_name),
    INDEX idx_recording_type (recording_type),
    INDEX idx_recording_quality (recording_quality),
    INDEX idx_recording_start_time (recording_start_time),
    INDEX idx_recording_end_time (recording_end_time),
    INDEX idx_recording_duration (recording_duration),
    INDEX idx_file_size (file_size),
    INDEX idx_file_format (file_format),
    INDEX idx_important (important),
    INDEX idx_has_event (has_event),
    INDEX idx_event_type (event_type),
    INDEX idx_backup_status (backup_status),
    INDEX idx_integrity_status (integrity_status),
    INDEX idx_transcode_status (transcode_status),
    INDEX idx_access_level (access_level),
    INDEX idx_encrypted (encrypted),
    INDEX idx_deleted_flag (deleted_flag),
    INDEX idx_create_time (create_time),
    INDEX idx_expire_time (expire_time),

    -- 复合索引
    INDEX idx_device_type_time (device_id, recording_type, recording_start_time, deleted_flag),
    INDEX idx_device_quality_time (device_id, recording_quality, recording_start_time, deleted_flag),
    INDEX idx_important_time (important, important_time, deleted_flag),
    INDEX idx_event_time (has_event, event_time, deleted_flag),
    INDEX idx_backup_time (backup_status, backup_time, deleted_flag),
    INDEX idx_expire_status (expire_time, deleted_flag),

    -- 全文索引
    FULLTEXT idx_file_content (file_name, important_remark, event_description),

    -- 外键约束
    FOREIGN KEY (device_id) REFERENCES t_video_device(device_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频录像表';

-- ============================================================
-- 6. 视频事件表 (t_video_event)
-- ============================================================
CREATE TABLE t_video_event (
    -- 主键
    event_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '事件ID',

    -- 关联信息
    device_id BIGINT NOT NULL COMMENT '设备ID',
    device_code VARCHAR(50) NOT NULL COMMENT '设备编号',
    device_name VARCHAR(100) NOT NULL COMMENT '设备名称',
    channel_id BIGINT COMMENT '通道ID',
    channel_name VARCHAR(50) COMMENT '通道名称',
    recording_id BIGINT COMMENT '关联录像ID',
    stream_id BIGINT COMMENT '关联流ID',

    -- 事件基本信息
    event_type VARCHAR(50) NOT NULL COMMENT '事件类型：motion-movement face-detection vehicle-detection cross-line intrusion loitering crowd violence fire smoke object-detection',
    event_level TINYINT DEFAULT 1 COMMENT '事件级别：1-低 2-中 3-高 4-紧急',
    event_status VARCHAR(20) DEFAULT 'pending' COMMENT '事件状态：pending-processing-confirmed-false-alarm-ignored',
    event_description TEXT COMMENT '事件描述',
    event_confidence DECIMAL(5,2) COMMENT '事件置信度(%)',

    -- 时间信息
    event_time DATETIME NOT NULL COMMENT '事件发生时间',
    detection_time DATETIME NOT NULL COMMENT '检测时间',
    report_time DATETIME COMMENT '上报时间',
    process_time DATETIME COMMENT '处理时间',
    resolve_time DATETIME COMMENT '解决时间',

    -- 位置信息
    detection_region TEXT COMMENT '检测区域(JSON格式)',
    coordinates TEXT COMMENT '坐标信息(JSON格式)',
    confidence_map TEXT COMMENT '置信度地图(JSON格式)',

    -- 目标信息
    target_type VARCHAR(50) COMMENT '目标类型：person-vehicle-animal-object',
    target_count INT DEFAULT 0 COMMENT '目标数量',
    target_attributes TEXT COMMENT '目标属性(JSON格式)',
    target_track_id VARCHAR(50) COMMENT '目标跟踪ID',

    -- 智能分析结果
    ai_analysis_result TEXT COMMENT 'AI分析结果(JSON格式)',
    analysis_confidence DECIMAL(5,2) COMMENT '分析置信度(%)',
    feature_vectors TEXT COMMENT '特征向量(JSON格式)',
    similarity_score DECIMAL(5,2) COMMENT '相似度得分(%)',

    -- 处理信息
    process_user_id BIGINT COMMENT '处理人ID',
    process_user_name VARCHAR(50) COMMENT '处理人姓名',
    process_action VARCHAR(50) COMMENT '处理动作：ignore-confirm-forward-police',
    process_remark TEXT COMMENT '处理备注',
    process_attachments TEXT COMMENT '处理附件(JSON格式)',

    -- 通知信息
    notification_sent TINYINT DEFAULT 0 COMMENT '是否已发送通知：0-否 1-是',
    notification_time DATETIME COMMENT '通知发送时间',
    notification_recipients TEXT COMMENT '通知接收者(JSON格式)',
    alarm_triggered TINYINT DEFAULT 0 COMMENT '是否触发报警：0-否 1-是',
    alarm_time DATETIME COMMENT '报警时间',

    -- 关联信息
    related_events TEXT COMMENT '关联事件ID列表(JSON数组)',
    related_recordings TEXT COMMENT '关联录像ID列表(JSON数组)',
    related_snapshots TEXT COMMENT '关联快照ID列表(JSON数组)',

    -- 媒体资源
    snapshot_path VARCHAR(500) COMMENT '事件快照路径',
    video_clip_path VARCHAR(500) COMMENT '事件视频片段路径',
    thumbnail_path VARCHAR(500) COMMENT '缩略图路径',
    evidence_files TEXT COMMENT '证据文件列表(JSON格式)',

    -- 权限信息
    access_level TINYINT DEFAULT 1 COMMENT '访问权限级别：1-公开 2-内部 3-机密 4-秘密',
    visibility TINYINT DEFAULT 1 COMMENT '可见性：0-私有 1-公开',

    -- 扩展属性
    extended_attributes TEXT COMMENT '扩展属性(JSON格式)',
    custom_fields TEXT COMMENT '自定义字段(JSON格式)',

    -- 审计字段
    create_user_id BIGINT COMMENT '创建人ID',
    create_user_name VARCHAR(50) COMMENT '创建人姓名',
    update_user_id BIGINT COMMENT '更新人ID',
    update_user_name VARCHAR(50) COMMENT '更新人姓名',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    version INT NOT NULL DEFAULT 1 COMMENT '版本号(乐观锁)',

    -- 索引
    INDEX idx_device_id (device_id),
    INDEX idx_device_channel (device_id, channel_id),
    INDEX idx_recording_id (recording_id),
    INDEX idx_stream_id (stream_id),
    INDEX idx_event_type (event_type),
    INDEX idx_event_level (event_level),
    INDEX idx_event_status (event_status),
    INDEX idx_event_time (event_time),
    INDEX idx_detection_time (detection_time),
    INDEX idx_target_type (target_type),
    INDEX idx_process_user_id (process_user_id),
    INDEX idx_notification_sent (notification_sent),
    INDEX idx_alarm_triggered (alarm_triggered),
    INDEX idx_access_level (access_level),
    INDEX idx_visibility (visibility),
    INDEX idx_deleted_flag (deleted_flag),
    INDEX idx_create_time (create_time),

    -- 复合索引
    INDEX idx_device_type_time (device_id, event_type, event_time, deleted_flag),
    INDEX idx_device_level_time (device_id, event_level, event_time, deleted_flag),
    INDEX idx_status_time (event_status, event_time, deleted_flag),
    INDEX idx_process_user_time (process_user_id, process_time, deleted_flag),
    INDEX idx_alarm_time (alarm_triggered, alarm_time, deleted_flag),

    -- 全文索引
    FULLTEXT idx_event_content (event_description, process_remark),

    -- 外键约束
    FOREIGN KEY (device_id) REFERENCES t_video_device(device_id) ON DELETE CASCADE,
    FOREIGN KEY (recording_id) REFERENCES t_video_recording(recording_id) ON DELETE SET NULL,
    FOREIGN KEY (stream_id) REFERENCES t_video_stream(stream_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频事件表';

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
    1,
    '20251216.001',
    'Create video service tables',
    'SQL',
    'V20251216__001_create_video_tables.sql',
    'flyway',
    NOW(),
    0,
    1
);