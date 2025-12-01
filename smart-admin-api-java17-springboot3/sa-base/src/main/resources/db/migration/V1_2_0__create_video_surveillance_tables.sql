-- 智能视频监控系统数据库表
-- 创建时间: 2025-11-16
-- 说明: 智能视频监控相关数据表

-- 1. 视频设备表
DROP TABLE IF EXISTS t_video_device;
CREATE TABLE t_video_device (
    video_device_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '视频设备ID',
    device_code VARCHAR(50) NOT NULL UNIQUE COMMENT '设备编码',
    device_name VARCHAR(100) NOT NULL COMMENT '设备名称',
    device_type VARCHAR(20) NOT NULL COMMENT '设备类型(CAMERA-摄像头,NVR-录像机)',
    device_ip VARCHAR(45) NOT NULL COMMENT '设备IP地址',
    device_port INT NOT NULL DEFAULT 554 COMMENT '设备端口',
    stream_port INT DEFAULT 554 COMMENT '流媒体端口',
    device_status VARCHAR(20) DEFAULT 'OFFLINE' COMMENT '设备状态(ONLINE-在线,OFFLINE-离线,FAULT-故障)',
    device_location VARCHAR(200) COMMENT '设备位置',
    area_id BIGINT COMMENT '所属区域ID',
    rtsp_url VARCHAR(500) COMMENT 'RTSP流地址',
    username VARCHAR(50) COMMENT '登录用户名',
    password VARCHAR(100) COMMENT '登录密码(加密存储)',
    ptz_enabled TINYINT DEFAULT 0 COMMENT '云台控制启用(0-否,1-是)',
    record_enabled TINYINT DEFAULT 1 COMMENT '录像启用(0-关闭,1-开启)',
    resolution VARCHAR(20) DEFAULT '1080P' COMMENT '分辨率',
    frame_rate INT DEFAULT 25 COMMENT '帧率',
    video_format VARCHAR(10) DEFAULT 'PAL' COMMENT '视频制式(PAL,NTSC)',
    manufacturer VARCHAR(50) COMMENT '制造商',
    model_number VARCHAR(50) COMMENT '型号',
    firmware_version VARCHAR(50) COMMENT '固件版本',
    install_date DATETIME COMMENT '安装日期',
    last_online_time DATETIME COMMENT '最后在线时间',
    device_description TEXT COMMENT '设备描述',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记(0-正常,1-删除)',
    version INT DEFAULT 1 COMMENT '版本号',

    INDEX idx_device_code (device_code),
    INDEX idx_device_type (device_type),
    INDEX idx_device_status (device_status),
    INDEX idx_area_id (area_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频设备信息表';

-- 2. 录像文件表
DROP TABLE IF EXISTS t_video_record;
CREATE TABLE t_video_record (
    video_record_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '录像记录ID',
    device_id BIGINT NOT NULL COMMENT '设备ID',
    record_type VARCHAR(20) NOT NULL COMMENT '录像类型(MANUAL-手动,SCHEDULE-定时,EVENT-事件)',
    record_start_time DATETIME NOT NULL COMMENT '录像开始时间',
    record_end_time DATETIME COMMENT '录像结束时间',
    record_duration INT COMMENT '录像时长(秒)',
    record_file_path VARCHAR(500) COMMENT '录像文件路径',
    record_file_name VARCHAR(200) COMMENT '录像文件名',
    record_file_size BIGINT COMMENT '录像文件大小(字节)',
    record_quality VARCHAR(20) DEFAULT 'HIGH' COMMENT '录像质量(HIGH-高,MEDIUM-中,LOW-低)',
    video_codec VARCHAR(20) COMMENT '视频编码格式',
    audio_codec VARCHAR(20) COMMENT '音频编码格式',
    event_type VARCHAR(50) COMMENT '事件类型',
    event_description TEXT COMMENT '事件描述',
    is_backed_up TINYINT DEFAULT 0 COMMENT '是否已备份(0-未备份,1-已备份)',
    backup_time DATETIME COMMENT '备份时间',
    backup_path VARCHAR(500) COMMENT '备份路径',
    is_encrypted TINYINT DEFAULT 0 COMMENT '是否加密(0-未加密,1-已加密)',
    thumbnail_path VARCHAR(500) COMMENT '缩略图路径',
    record_status VARCHAR(20) DEFAULT 'COMPLETED' COMMENT '录像状态(RECORDING-录制中,COMPLETED-已完成,FAILED-失败)',
    remark TEXT COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记(0-正常,1-删除)',
    version INT DEFAULT 1 COMMENT '版本号',

    INDEX idx_device_id (device_id),
    INDEX idx_record_type (record_type),
    INDEX idx_record_start_time (record_start_time),
    INDEX idx_record_end_time (record_end_time),
    INDEX idx_event_type (event_type),
    INDEX idx_create_time (create_time),
    FOREIGN KEY (device_id) REFERENCES t_video_device(video_device_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频录像记录表';

-- 3. 人脸特征库表
DROP TABLE IF EXISTS t_face_feature;
CREATE TABLE t_face_feature (
    face_feature_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '人脸特征ID',
    person_id BIGINT NOT NULL COMMENT '关联人员ID',
    face_image_path VARCHAR(500) NOT NULL COMMENT '人脸照片路径',
    face_feature_data LONGBLOB NOT NULL COMMENT '人脸特征数据(二进制)',
    face_image_url VARCHAR(500) COMMENT '人脸照片URL',
    face_quality_score DECIMAL(5,2) COMMENT '人脸质量评分(0-100)',
    face_rect VARCHAR(100) COMMENT '人脸位置信息(x,y,w,h)',
    capture_device_id BIGINT COMMENT '采集设备ID',
    capture_time DATETIME COMMENT '采集时间',
    feature_algorithm VARCHAR(50) DEFAULT 'ARCFACE' COMMENT '特征算法',
    feature_version VARCHAR(20) COMMENT '特征版本',
    feature_accuracy DECIMAL(5,2) COMMENT '特征准确度',
    is_enabled TINYINT DEFAULT 1 COMMENT '是否启用(0-禁用,1-启用)',
    last_match_time DATETIME COMMENT '最后匹配时间',
    match_count INT DEFAULT 0 COMMENT '匹配次数',
    remark TEXT COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记(0-正常,1-删除)',
    version INT DEFAULT 1 COMMENT '版本号',

    INDEX idx_person_id (person_id),
    INDEX idx_capture_device_id (capture_device_id),
    INDEX idx_is_enabled (is_enabled),
    INDEX idx_capture_time (capture_time),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='人脸特征库表';

-- 4. 监控事件表
DROP TABLE IF EXISTS t_monitor_event;
CREATE TABLE t_monitor_event (
    monitor_event_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '监控事件ID',
    event_type VARCHAR(50) NOT NULL COMMENT '事件类型',
    event_level VARCHAR(20) NOT NULL DEFAULT 'INFO' COMMENT '事件等级(INFO-信息,WARNING-警告,ERROR-错误,CRITICAL-严重)',
    device_id BIGINT NOT NULL COMMENT '设备ID',
    area_id BIGINT COMMENT '区域ID',
    event_title VARCHAR(200) NOT NULL COMMENT '事件标题',
    event_description TEXT COMMENT '事件描述',
    event_image_url VARCHAR(500) COMMENT '事件截图URL',
    event_video_url VARCHAR(500) COMMENT '事件录像URL',
    event_data JSON COMMENT '事件详细数据(JSON格式)',
    detection_confidence DECIMAL(5,2) COMMENT '检测置信度(0-100)',
    target_count INT DEFAULT 0 COMMENT '目标数量',
    target_info JSON COMMENT '目标信息(JSON格式)',
    event_location VARCHAR(200) COMMENT '事件位置',
    event_status VARCHAR(20) DEFAULT 'NEW' COMMENT '事件状态(NEW-新建,PROCESSING-处理中,RESOLVED-已解决,IGNORED-已忽略)',
    is_alert TINYINT DEFAULT 0 COMMENT '是否告警(0-否,1-是)',
    alert_time DATETIME COMMENT '告警时间',
    process_user_id BIGINT COMMENT '处理人ID',
    process_time DATETIME COMMENT '处理时间',
    process_result TEXT COMMENT '处理结果',
    is_verified TINYINT DEFAULT 0 COMMENT '是否已核实(0-未核实,1-已核实)',
    verify_user_id BIGINT COMMENT '核实人ID',
    verify_time DATETIME COMMENT '核实时间',
    verify_result TEXT COMMENT '核实结果',
    remark TEXT COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记(0-正常,1-删除)',
    version INT DEFAULT 1 COMMENT '版本号',

    INDEX idx_event_type (event_type),
    INDEX idx_event_level (event_level),
    INDEX idx_device_id (device_id),
    INDEX idx_area_id (area_id),
    INDEX idx_event_status (event_status),
    INDEX idx_is_alert (is_alert),
    INDEX idx_create_time (create_time),
    INDEX idx_alert_time (alert_time),
    FOREIGN KEY (device_id) REFERENCES t_video_device(video_device_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='监控事件表';

-- 5. 设备状态日志表
DROP TABLE IF EXISTS t_device_status_log;
CREATE TABLE t_device_status_log (
    status_log_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '状态日志ID',
    device_id BIGINT NOT NULL COMMENT '设备ID',
    old_status VARCHAR(20) COMMENT '原状态',
    new_status VARCHAR(20) NOT NULL COMMENT '新状态',
    status_change_reason VARCHAR(200) COMMENT '状态变更原因',
    cpu_usage DECIMAL(5,2) COMMENT 'CPU使用率(%)',
    memory_usage DECIMAL(5,2) COMMENT '内存使用率(%)',
    disk_usage DECIMAL(5,2) COMMENT '磁盘使用率(%)',
    network_status VARCHAR(20) COMMENT '网络状态',
    stream_status VARCHAR(20) COMMENT '流媒体状态',
    error_code VARCHAR(50) COMMENT '错误代码',
    error_message TEXT COMMENT '错误信息',
    log_data JSON COMMENT '日志详细数据(JSON格式)',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    INDEX idx_device_id (device_id),
    INDEX idx_new_status (new_status),
    INDEX idx_create_time (create_time),
    FOREIGN KEY (device_id) REFERENCES t_video_device(video_device_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备状态日志表';

-- 添加外键约束
ALTER TABLE t_face_feature ADD CONSTRAINT fk_face_feature_person FOREIGN KEY (person_id) REFERENCES t_employee(employee_id) ON DELETE CASCADE;
ALTER TABLE t_face_feature ADD CONSTRAINT fk_face_feature_device FOREIGN KEY (capture_device_id) REFERENCES t_video_device(video_device_id) ON DELETE SET NULL;