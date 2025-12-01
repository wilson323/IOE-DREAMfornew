-- ====================================================================
-- 门禁系统数据库迁移脚本
-- 版本: 1.2.0
-- 创建时间: 2025-11-16
-- 描述: 创建门禁系统相关表结构，包括区域、设备、事件等核心表
-- ====================================================================

-- 门禁区域表
CREATE TABLE IF NOT EXISTS t_smart_access_area (
    area_id BIGINT AUTO_INCREMENT COMMENT '区域ID（主键）',
    area_code VARCHAR(32) NOT NULL COMMENT '区域编码（系统唯一）',
    area_name VARCHAR(100) NOT NULL COMMENT '区域名称',
    area_type TINYINT NOT NULL DEFAULT 1 COMMENT '区域类型（1:园区 2:建筑 3:楼层 4:房间 5:区域 6:其他）',
    parent_id BIGINT DEFAULT 0 COMMENT '上级区域ID（0表示根区域）',
    path VARCHAR(500) DEFAULT NULL COMMENT '层级路径（用逗号分隔的ID链，如：0,1,2,3）',
    level TINYINT DEFAULT 0 COMMENT '层级深度（根区域为0）',
    sort_order INT DEFAULT 0 COMMENT '排序号（同层级排序）',
    description VARCHAR(500) DEFAULT NULL COMMENT '区域描述',
    building_id BIGINT DEFAULT NULL COMMENT '所在建筑ID',
    floor_id BIGINT DEFAULT NULL COMMENT '所在楼层ID',
    area DECIMAL(10,2) DEFAULT NULL COMMENT '区域面积（平方米）',
    capacity INT DEFAULT NULL COMMENT '容纳人数',
    access_enabled TINYINT DEFAULT 1 COMMENT '是否启用门禁（0:禁用 1:启用）',
    access_level TINYINT DEFAULT 1 COMMENT '访问权限级别（数字越大权限要求越高）',
    special_auth_required TINYINT DEFAULT 0 COMMENT '是否需要特殊授权（0:不需要 1:需要）',
    valid_time_start VARCHAR(5) DEFAULT NULL COMMENT '有效时间段开始（HH:mm格式）',
    valid_time_end VARCHAR(5) DEFAULT NULL COMMENT '有效时间段结束（HH:mm格式）',
    valid_weekdays VARCHAR(20) DEFAULT NULL COMMENT '有效星期（逗号分隔，1-7代表周一到周日）',
    status TINYINT DEFAULT 1 COMMENT '区域状态（0:停用 1:正常 2:维护中）',
    longitude DECIMAL(10,7) DEFAULT NULL COMMENT '经度坐标',
    latitude DECIMAL(10,7) DEFAULT NULL COMMENT '纬度坐标',
    map_image VARCHAR(500) DEFAULT NULL COMMENT '区域平面图路径',
    remark VARCHAR(1000) DEFAULT NULL COMMENT '备注信息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT DEFAULT NULL COMMENT '创建人ID',
    update_user_id BIGINT DEFAULT NULL COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记（0：未删除，1：已删除）',
    version INT DEFAULT 1 COMMENT '版本号（乐观锁）',
    PRIMARY KEY (area_id),
    UNIQUE KEY uk_area_code (area_code),
    KEY idx_parent_id (parent_id),
    KEY idx_path (path(255)),
    KEY idx_level (level),
    KEY idx_area_type (area_type),
    KEY idx_status (status),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门禁区域表';

-- 门禁设备表
CREATE TABLE IF NOT EXISTS t_smart_access_device (
    access_device_id BIGINT AUTO_INCREMENT COMMENT '门禁设备ID（主键）',
    device_id BIGINT DEFAULT NULL COMMENT '设备ID（关联SmartDeviceEntity）',
    area_id BIGINT NOT NULL COMMENT '所属区域ID',
    access_device_type TINYINT NOT NULL DEFAULT 1 COMMENT '门禁设备类型（1:门禁机 2:读卡器 3:指纹机 4:人脸识别机 5:密码键盘 6:三辊闸 7:翼闸 8:摆闸 9:其他）',
    manufacturer VARCHAR(50) DEFAULT NULL COMMENT '设备厂商',
    device_model VARCHAR(100) DEFAULT NULL COMMENT '设备型号',
    serial_number VARCHAR(100) DEFAULT NULL COMMENT '设备序列号',
    protocol VARCHAR(20) DEFAULT NULL COMMENT '通信协议（TCP/UDP/HTTP/HTTPS/MQTT）',
    ip_address VARCHAR(45) DEFAULT NULL COMMENT 'IP地址',
    port INT DEFAULT NULL COMMENT '端口号',
    comm_key VARCHAR(100) DEFAULT NULL COMMENT '通信密钥',
    direction TINYINT DEFAULT 2 COMMENT '设备方向（0:单向进入 1:单向外出 2:双向）',
    open_method TINYINT DEFAULT 1 COMMENT '开门方式（1:刷卡 2:密码 3:指纹 4:人脸 5:二维码 6:组合方式）',
    open_delay INT DEFAULT 3 COMMENT '开门延时时间（秒）',
    valid_time INT DEFAULT 5 COMMENT '有效开门时间（秒）',
    remote_open_enabled TINYINT DEFAULT 0 COMMENT '是否支持远程开门（0:不支持 1:支持）',
    anti_passback_enabled TINYINT DEFAULT 0 COMMENT '是否支持反潜回（0:不支持 1:支持）',
    multi_person_enabled TINYINT DEFAULT 0 COMMENT '是否支持多人同时进入（0:不支持 1:支持）',
    door_sensor_enabled TINYINT DEFAULT 0 COMMENT '是否支持门磁检测（0:不支持 1:支持）',
    door_sensor_status TINYINT DEFAULT 0 COMMENT '门磁状态（0:关闭 1:打开）',
    online_status TINYINT DEFAULT 0 COMMENT '设备在线状态（0:离线 1:在线）',
    last_comm_time DATETIME DEFAULT NULL COMMENT '最后通信时间',
    work_mode TINYINT DEFAULT 1 COMMENT '设备工作模式（1:正常模式 2:维护模式 3:紧急模式 4:锁闭模式）',
    firmware_version VARCHAR(50) DEFAULT NULL COMMENT '设备版本信息',
    hardware_version VARCHAR(50) DEFAULT NULL COMMENT '硬件版本信息',
    heartbeat_interval INT DEFAULT 60 COMMENT '心跳间隔（秒）',
    last_heartbeat_time DATETIME DEFAULT NULL COMMENT '最后心跳时间',
    enabled TINYINT DEFAULT 1 COMMENT '是否启用（0:禁用 1:启用）',
    install_location VARCHAR(200) DEFAULT NULL COMMENT '安装位置描述',
    longitude DECIMAL(10,7) DEFAULT NULL COMMENT '经度坐标',
    latitude DECIMAL(10,7) DEFAULT NULL COMMENT '纬度坐标',
    device_photo VARCHAR(500) DEFAULT NULL COMMENT '设备照片路径',
    maintenance_person VARCHAR(50) DEFAULT NULL COMMENT '维护人员',
    maintenance_phone VARCHAR(20) DEFAULT NULL COMMENT '维护联系电话',
    last_maintenance_time DATETIME DEFAULT NULL COMMENT '上次维护时间',
    next_maintenance_time DATETIME DEFAULT NULL COMMENT '下次维护时间',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT DEFAULT NULL COMMENT '创建人ID',
    update_user_id BIGINT DEFAULT NULL COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记（0：未删除，1：已删除）',
    version INT DEFAULT 1 COMMENT '版本号（乐观锁）',
    PRIMARY KEY (access_device_id),
    KEY idx_device_id (device_id),
    KEY idx_area_id (area_id),
    KEY idx_access_device_type (access_device_type),
    KEY idx_online_status (online_status),
    KEY idx_enabled (enabled),
    KEY idx_create_time (create_time),
    CONSTRAINT fk_access_device_area FOREIGN KEY (area_id) REFERENCES t_smart_access_area (area_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门禁设备表';

-- 门禁通行事件表
CREATE TABLE IF NOT EXISTS t_smart_access_event (
    event_id BIGINT AUTO_INCREMENT COMMENT '事件ID（主键）',
    event_no VARCHAR(64) NOT NULL COMMENT '事件编号（系统生成唯一编号）',
    user_id BIGINT DEFAULT NULL COMMENT '用户ID',
    user_code VARCHAR(50) DEFAULT NULL COMMENT '用户工号',
    user_name VARCHAR(100) DEFAULT NULL COMMENT '用户姓名',
    department_id BIGINT DEFAULT NULL COMMENT '部门ID',
    department_name VARCHAR(100) DEFAULT NULL COMMENT '部门名称',
    device_id BIGINT NOT NULL COMMENT '设备ID',
    device_code VARCHAR(50) DEFAULT NULL COMMENT '设备编码',
    device_name VARCHAR(100) DEFAULT NULL COMMENT '设备名称',
    area_id BIGINT NOT NULL COMMENT '区域ID',
    area_name VARCHAR(100) DEFAULT NULL COMMENT '区域名称',
    verify_method TINYINT NOT NULL DEFAULT 1 COMMENT '验证方式（1:刷卡 2:密码 3:指纹 4:人脸 5:二维码 6:IC卡+密码 7:其他）',
    verify_result TINYINT NOT NULL DEFAULT 0 COMMENT '验证结果（0:成功 1:失败 2:超时 3:取消 4:其他）',
    fail_reason VARCHAR(200) DEFAULT NULL COMMENT '失败原因',
    card_no VARCHAR(50) DEFAULT NULL COMMENT '卡号',
    biometric_id BIGINT DEFAULT NULL COMMENT '生物特征ID',
    fingerprint_id VARCHAR(50) DEFAULT NULL COMMENT '指纹模板ID',
    face_id VARCHAR(50) DEFAULT NULL COMMENT '人脸特征ID',
    direction TINYINT DEFAULT 0 COMMENT '通行方向（0:进入 1:外出 2:未知）',
    event_time DATETIME NOT NULL COMMENT '通行时间',
    door_open_time DATETIME DEFAULT NULL COMMENT '门锁动作时间（门锁实际打开时间）',
    door_close_time DATETIME DEFAULT NULL COMMENT '门锁关闭时间',
    duration INT DEFAULT NULL COMMENT '通行时长（秒）',
    door_opened TINYINT DEFAULT 0 COMMENT '是否开门成功（0:失败 1:成功）',
    photo_path VARCHAR(500) DEFAULT NULL COMMENT '拍照路径（通行时拍摄的照片）',
    thumbnail_path VARCHAR(500) DEFAULT NULL COMMENT '拍照缩略图路径',
    temperature DECIMAL(3,1) DEFAULT NULL COMMENT '温度检测值（摄氏度）',
    temperature_abnormal TINYINT DEFAULT 0 COMMENT '是否体温异常（0:正常 1:异常）',
    mask_status TINYINT DEFAULT 2 COMMENT '口罩佩戴状态（0:未佩戴 1:佩戴 2:未检测）',
    visitor_id BIGINT DEFAULT NULL COMMENT '访客ID（临时访客记录）',
    visitor_name VARCHAR(100) DEFAULT NULL COMMENT '访客姓名',
    visitor_id_card VARCHAR(50) DEFAULT NULL COMMENT '访客证件号',
    visitee_id BIGINT DEFAULT NULL COMMENT '被访人ID',
    visitee_name VARCHAR(100) DEFAULT NULL COMMENT '被访人姓名',
    valid_start_time DATETIME DEFAULT NULL COMMENT '有效期开始时间',
    valid_end_time DATETIME DEFAULT NULL COMMENT '有效期结束时间',
    event_level TINYINT DEFAULT 0 COMMENT '事件级别（0:普通 1:重要 2:紧急 3:异常）',
    blacklist_event TINYINT DEFAULT 0 COMMENT '是否黑名单事件（0:否 1:是）',
    alarm_type TINYINT DEFAULT 0 COMMENT '报警类型（0:无报警 1:非法闯入 2:尾随 3:胁迫 4:其他）',
    alarm_desc VARCHAR(200) DEFAULT NULL COMMENT '报警描述',
    handle_status TINYINT DEFAULT 0 COMMENT '处理状态（0:未处理 1:已处理 2:忽略）',
    handler_id BIGINT DEFAULT NULL COMMENT '处理人ID',
    handler_name VARCHAR(100) DEFAULT NULL COMMENT '处理人姓名',
    handle_time DATETIME DEFAULT NULL COMMENT '处理时间',
    handle_remark VARCHAR(500) DEFAULT NULL COMMENT '处理备注',
    data_source TINYINT DEFAULT 0 COMMENT '数据来源（0:设备上报 1:手动录入 2:第三方系统 3:其他）',
    third_party_id VARCHAR(100) DEFAULT NULL COMMENT '第三方系统ID',
    raw_message TEXT DEFAULT NULL COMMENT '原始报文（设备发送的原始数据）',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注信息',
    PRIMARY KEY (event_id),
    UNIQUE KEY uk_event_no (event_no),
    KEY idx_user_id (user_id),
    KEY idx_device_id (device_id),
    KEY idx_area_id (area_id),
    KEY idx_event_time (event_time),
    KEY idx_verify_result (verify_result),
    KEY idx_verify_method (verify_method),
    KEY idx_direction (direction),
    KEY idx_event_level (event_level),
    KEY idx_handle_status (handle_status),
    KEY idx_card_no (card_no),
    KEY idx_create_time (event_time),
    CONSTRAINT fk_access_event_device FOREIGN KEY (device_id) REFERENCES t_smart_access_device (access_device_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_access_event_area FOREIGN KEY (area_id) REFERENCES t_smart_access_area (area_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门禁通行事件表';

-- 创建索引优化查询性能
CREATE INDEX idx_access_event_composite ON t_smart_access_event (event_time, verify_result, area_id);
CREATE INDEX idx_access_device_composite ON t_smart_access_device (area_id, enabled, online_status);
CREATE INDEX idx_access_area_composite ON t_smart_access_area (parent_id, status, access_enabled);

-- 插入默认数据
INSERT INTO t_smart_access_area (area_code, area_name, area_type, parent_id, path, level, sort_order, access_enabled, status, create_user_id) VALUES
('ROOT', '根区域', 1, 0, '0', 0, 0, 1, 1, 1),
('AREA_001', '主园区', 1, 1, '0,1', 1, 1, 1, 1, 1),
('AREA_002', '研发楼', 2, 2, '0,1,2', 2, 1, 1, 1, 1),
('AREA_003', '办公楼', 2, 2, '0,1,2', 2, 2, 1, 1, 1);

-- 创建分区表（可选，根据数据量决定是否启用）
-- 按月分区事件表以提高查询性能
-- ALTER TABLE t_smart_access_event PARTITION BY RANGE (YEAR(event_time) * 100 + MONTH(event_time)) (
--     PARTITION p202511 VALUES LESS THAN (202512),
--     PARTITION p202512 VALUES LESS THAN (202601),
--     PARTITION p202601 VALUES LESS THAN (202602),
--     PARTITION p_future VALUES LESS THAN MAXVALUE
-- );

-- ====================================================================
-- 创建视图方便查询
-- ====================================================================

-- 区域树视图
CREATE OR REPLACE VIEW v_access_area_tree AS
SELECT
    area_id,
    area_code,
    area_name,
    area_type,
    parent_id,
    path,
    level,
    sort_order,
    description,
    status,
    access_enabled,
    create_time,
    (SELECT COUNT(*) FROM t_smart_access_area child WHERE child.parent_id = a.area_id AND child.deleted_flag = 0) as children_count
FROM t_smart_access_area a
WHERE a.deleted_flag = 0;

-- 设备统计视图
CREATE OR REPLACE VIEW v_access_device_stats AS
SELECT
    d.access_device_id,
    d.device_code,
    d.device_name,
    d.area_id,
    a.area_name,
    d.access_device_type,
    d.online_status,
    d.enabled,
    COUNT(e.event_id) as total_events,
    COUNT(CASE WHEN e.event_time >= CURDATE() THEN 1 END) as today_events,
    COUNT(CASE WHEN e.verify_result = 0 THEN 1 END) as success_events,
    COUNT(CASE WHEN e.verify_result = 1 THEN 1 END) as fail_events
FROM t_smart_access_device d
LEFT JOIN t_smart_access_area a ON d.area_id = a.area_id
LEFT JOIN t_smart_access_event e ON d.access_device_id = e.device_id
WHERE d.deleted_flag = 0
GROUP BY d.access_device_id, d.device_code, d.device_name, d.area_id, a.area_name, d.access_device_type, d.online_status, d.enabled;

-- ====================================================================
-- 创建存储过程（可选）
-- ====================================================================

-- 计算区域路径的存储过程
DELIMITER //
CREATE PROCEDURE sp_calculate_area_path(IN p_area_id BIGINT)
BEGIN
    DECLARE v_parent_id BIGINT;
    DECLARE v_path VARCHAR(500);

    -- 获取父级ID
    SELECT parent_id INTO v_parent_id FROM t_smart_access_area WHERE area_id = p_area_id;

    -- 如果是根区域
    IF v_parent_id = 0 THEN
        SET v_path = CONCAT('0,', p_area_id);
    ELSE
        -- 递归计算路径
        CALL sp_calculate_area_path(v_parent_id);
        SELECT CONCAT(path, ',', p_area_id) INTO v_path
        FROM t_smart_access_area
        WHERE area_id = v_parent_id;
    END IF;

    -- 更新路径
    UPDATE t_smart_access_area
    SET path = v_path, level = (CHAR_LENGTH(v_path) - CHAR_LENGTH(REPLACE(v_path, ',', '')) - 1)
    WHERE area_id = p_area_id;
END //
DELIMITER ;

-- ====================================================================
-- 权限表（可选，用于细粒度权限控制）
-- ====================================================================

-- 用户区域权限表
CREATE TABLE IF NOT EXISTS t_user_area_permission (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    area_id BIGINT NOT NULL COMMENT '区域ID',
    permission_type TINYINT DEFAULT 1 COMMENT '权限类型（1:只读 2:读写 3:管理）',
    valid_start_time DATETIME DEFAULT NULL COMMENT '权限有效开始时间',
    valid_end_time DATETIME DEFAULT NULL COMMENT '权限有效结束时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_user_id BIGINT DEFAULT NULL COMMENT '创建人ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_area (user_id, area_id),
    KEY idx_user_id (user_id),
    KEY idx_area_id (area_id),
    CONSTRAINT fk_user_area_user FOREIGN KEY (user_id) REFERENCES t_employee (employee_id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_user_area_area FOREIGN KEY (area_id) REFERENCES t_smart_access_area (area_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户区域权限表';

-- 用户设备权限表
CREATE TABLE IF NOT EXISTS t_user_device_permission (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    device_id BIGINT NOT NULL COMMENT '设备ID',
    permission_type TINYINT DEFAULT 1 COMMENT '权限类型（1:只读 2:控制 3:管理）',
    valid_start_time DATETIME DEFAULT NULL COMMENT '权限有效开始时间',
    valid_end_time DATETIME DEFAULT NULL COMMENT '权限有效结束时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_user_id BIGINT DEFAULT NULL COMMENT '创建人ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_device (user_id, device_id),
    KEY idx_user_id (user_id),
    KEY idx_device_id (device_id),
    CONSTRAINT fk_user_device_user FOREIGN KEY (user_id) REFERENCES t_employee (employee_id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_user_device_device FOREIGN KEY (device_id) REFERENCES t_smart_access_device (access_device_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户设备权限表';

-- ====================================================================
-- 完成迁移
-- ====================================================================