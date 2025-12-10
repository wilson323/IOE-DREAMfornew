-- =====================================================
-- IOE-DREAM 业务数据库表结构脚本
-- 版本: 1.0.0
-- 说明: 创建所有业务数据库的表结构
-- =====================================================

-- 设置字符集
SET NAMES utf8mb4;

-- =====================================================
-- 1. 门禁管理数据库 (ioedream_access_db)
-- =====================================================

USE ioedream_access_db;

-- 1.1 门禁设备表
DROP TABLE IF EXISTS t_access_device;
CREATE TABLE t_access_device (
    device_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '设备ID',
    device_no VARCHAR(50) NOT NULL UNIQUE COMMENT '设备编号',
    device_name VARCHAR(100) NOT NULL COMMENT '设备名称',
    device_type VARCHAR(20) NOT NULL COMMENT '设备类型 ACCESS_DOOR-门禁 ACCESS_TURNSTILE-闸机 ACCESS_BARRIER-道闸',
    area_id BIGINT NOT NULL COMMENT '所属区域ID',
    location VARCHAR(200) COMMENT '设备位置',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    port INT COMMENT '端口',
    protocol VARCHAR(20) DEFAULT 'TCP' COMMENT '通讯协议',
    status TINYINT DEFAULT 1 COMMENT '状态 1-在线 0-离线',
    last_online_time DATETIME COMMENT '最后在线时间',
    config JSON COMMENT '设备配置信息',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',
    INDEX idx_device_no (device_no),
    INDEX idx_area_id (area_id),
    INDEX idx_device_type (device_type),
    INDEX idx_device_status (status),
    INDEX idx_ip_address (ip_address)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门禁设备表';

-- 1.2 通行记录表
DROP TABLE IF EXISTS t_access_record;
CREATE TABLE t_access_record (
    record_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    user_id BIGINT COMMENT '用户ID',
    card_no VARCHAR(50) COMMENT '卡号',
    device_id BIGINT NOT NULL COMMENT '设备ID',
    area_id BIGINT COMMENT '区域ID',
    access_type VARCHAR(20) NOT NULL COMMENT '通行类型 IN-进入 OUT-离开',
    verify_mode VARCHAR(20) COMMENT '验证方式 FACE-人脸 CARD-刷卡 FINGERPRINT-指纹 PASSWORD-密码',
    verify_result TINYINT DEFAULT 1 COMMENT '验证结果 1-成功 0-失败',
    fail_reason VARCHAR(100) COMMENT '失败原因',
    access_time DATETIME NOT NULL COMMENT '通行时间',
    photo_url VARCHAR(500) COMMENT '照片URL',
    temperature DECIMAL(3,1) COMMENT '体温(摄氏度)',
    mask_detect TINYINT DEFAULT 1 COMMENT '口罩检测 1-佩戴 0-未佩戴',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_device_id (device_id),
    INDEX idx_area_id (area_id),
    INDEX idx_access_time (access_time),
    INDEX idx_access_type (access_type),
    INDEX idx_verify_result (verify_result),
    INDEX idx_card_no (card_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通行记录表';

-- =====================================================
-- 2. 考勤管理数据库 (ioedream_attendance_db)
-- =====================================================

USE ioedream_attendance_db;

-- 2.1 考勤机设备表
DROP TABLE IF EXISTS t_attendance_device;
CREATE TABLE t_attendance_device (
    device_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '设备ID',
    device_no VARCHAR(50) NOT NULL UNIQUE COMMENT '设备编号',
    device_name VARCHAR(100) NOT NULL COMMENT '设备名称',
    area_id BIGINT NOT NULL COMMENT '所属区域ID',
    location VARCHAR(200) COMMENT '设备位置',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    port INT COMMENT '端口',
    protocol VARCHAR(20) DEFAULT 'TCP' COMMENT '通讯协议',
    status TINYINT DEFAULT 1 COMMENT '状态 1-在线 0-离线',
    last_online_time DATETIME COMMENT '最后在线时间',
    config JSON COMMENT '设备配置信息',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',
    INDEX idx_device_no (device_no),
    INDEX idx_area_id (area_id),
    INDEX idx_device_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤机设备表';

-- 2.2 班次表
DROP TABLE IF EXISTS t_work_shift;
CREATE TABLE t_work_shift (
    shift_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '班次ID',
    shift_code VARCHAR(50) NOT NULL UNIQUE COMMENT '班次编码',
    shift_name VARCHAR(100) NOT NULL COMMENT '班次名称',
    shift_type TINYINT DEFAULT 1 COMMENT '班次类型 1-固定班次 2-弹性班次 3-轮班班次',
    work_start_time TIME NOT NULL COMMENT '上班时间',
    work_end_time TIME NOT NULL COMMENT '下班时间',
    rest_start_time TIME COMMENT '休息开始时间',
    rest_end_time TIME COMMENT '休息结束时间',
    late_tolerance INT DEFAULT 0 COMMENT '迟到容忍时间(分钟)',
    early_tolerance INT DEFAULT 0 COMMENT '早退容忍时间(分钟)',
    overtime_rule TINYINT DEFAULT 1 COMMENT '加班规则 1-按小时 2-按时间段',
    weekend_work TINYINT DEFAULT 0 COMMENT '周末是否工作 0-否 1-是',
    status TINYINT DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',
    INDEX idx_shift_code (shift_code),
    INDEX idx_shift_type (shift_type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='班次表';

-- 2.3 考勤记录表
DROP TABLE IF EXISTS t_attendance_record;
CREATE TABLE t_attendance_record (
    record_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    device_id BIGINT COMMENT '设备ID',
    attendance_type VARCHAR(20) NOT NULL COMMENT '考勤类型 ON-上班 OFF-下班 OVERTIME_IN-加班上班 OVERTIME_OUT-加班下班',
    attendance_time DATETIME NOT NULL COMMENT '考勤时间',
    verify_mode VARCHAR(20) COMMENT '验证方式 FACE-人脸 CARD-刷卡 FINGERPRINT-指纹 PASSWORD-密码',
    photo_url VARCHAR(500) COMMENT '照片URL',
    location VARCHAR(200) COMMENT '考勤地点',
    is_abnormal TINYINT DEFAULT 0 COMMENT '是否异常 0-正常 1-异常',
    abnormal_type VARCHAR(20) COMMENT '异常类型 LATE-迟到 EARLY-早退 ABSENCE-旷工',
    process_status TINYINT DEFAULT 0 COMMENT '处理状态 0-待处理 1-已处理 2-已忽略',
    process_remark VARCHAR(500) COMMENT '处理备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_device_id (device_id),
    INDEX idx_attendance_time (attendance_time),
    INDEX idx_attendance_type (attendance_type),
    INDEX idx_is_abnormal (is_abnormal),
    INDEX idx_process_status (process_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤记录表';

-- =====================================================
-- 3. 消费管理数据库 (ioedream_consume_db)
-- =====================================================

USE ioedream_consume_db;

-- 3.1 消费账户表
DROP TABLE IF EXISTS t_consume_account;
CREATE TABLE t_consume_account (
    account_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '账户ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    account_no VARCHAR(50) NOT NULL UNIQUE COMMENT '账户编号',
    account_name VARCHAR(100) NOT NULL COMMENT '账户名称',
    account_type TINYINT DEFAULT 1 COMMENT '账户类型 1-个人账户 2-团体账户 3-临时账户',
    balance DECIMAL(12,2) DEFAULT 0.00 COMMENT '账户余额',
    freeze_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '冻结金额',
    credit_limit DECIMAL(12,2) DEFAULT 0.00 COMMENT '信用额度',
    daily_limit DECIMAL(12,2) DEFAULT 999999.99 COMMENT '日消费限额',
    monthly_limit DECIMAL(12,2) DEFAULT 999999.99 COMMENT '月消费限额',
    subsidy_balance DECIMAL(12,2) DEFAULT 0.00 COMMENT '补贴余额',
    status TINYINT DEFAULT 1 COMMENT '状态 1-正常 0-冻结 2-注销',
    last_use_time DATETIME COMMENT '最后使用时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',
    INDEX idx_user_id (user_id),
    INDEX idx_account_no (account_no),
    INDEX idx_account_type (account_type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消费账户表';

-- 3.2 消费记录表
DROP TABLE IF EXISTS t_consume_record;
CREATE TABLE t_consume_record (
    record_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '订单号',
    account_id BIGINT NOT NULL COMMENT '账户ID',
    user_id BIGINT COMMENT '用户ID',
    device_id BIGINT COMMENT '设备ID',
    area_id BIGINT COMMENT '区域ID',
    consume_type VARCHAR(20) NOT NULL COMMENT '消费类型 MEAL-餐饮 SHOP-购物 OTHER-其他',
    amount DECIMAL(12,2) NOT NULL COMMENT '消费金额',
    discount_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '折扣金额',
    subsidy_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '补贴金额',
    actual_amount DECIMAL(12,2) NOT NULL COMMENT '实际支付金额',
    payment_method VARCHAR(20) NOT NULL COMMENT '支付方式 BALANCE-余额 CARD-银行卡 WECHAT-微信 ALIPAY-支付宝',
    consume_time DATETIME NOT NULL COMMENT '消费时间',
    merchant_name VARCHAR(100) COMMENT '商户名称',
    goods_info JSON COMMENT '商品信息',
    remark VARCHAR(500) COMMENT '备注',
    refund_status TINYINT DEFAULT 0 COMMENT '退款状态 0-未退款 1-部分退款 2-全额退款',
    refund_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '退款金额',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_order_no (order_no),
    INDEX idx_account_id (account_id),
    INDEX idx_user_id (user_id),
    INDEX idx_device_id (device_id),
    INDEX idx_consume_time (consume_time),
    INDEX idx_consume_type (consume_type),
    INDEX idx_payment_method (payment_method),
    INDEX idx_refund_status (refund_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消费记录表';

-- =====================================================
-- 4. 访客管理数据库 (ioedream_visitor_db)
-- =====================================================

USE ioedream_visitor_db;

-- 4.1 访客预约表
DROP TABLE IF EXISTS t_visitor_appointment;
CREATE TABLE t_visitor_appointment (
    appointment_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '预约ID',
    appointment_no VARCHAR(50) NOT NULL UNIQUE COMMENT '预约编号',
    visitor_name VARCHAR(100) NOT NULL COMMENT '访客姓名',
    visitor_phone VARCHAR(20) NOT NULL COMMENT '访客手机号',
    visitor_company VARCHAR(200) COMMENT '访客单位',
    visitor_id_card VARCHAR(20) COMMENT '访客身份证号',
    visitor_email VARCHAR(100) COMMENT '访客邮箱',
    visit_purpose VARCHAR(500) COMMENT '来访事由',
    host_user_id BIGINT NOT NULL COMMENT '接待人ID',
    host_user_name VARCHAR(100) COMMENT '接待人姓名',
    host_department VARCHAR(100) COMMENT '接待人部门',
    visit_area_id BIGINT NOT NULL COMMENT '访问区域ID',
    visit_area_name VARCHAR(100) COMMENT '访问区域名称',
    visit_start_time DATETIME NOT NULL COMMENT '访问开始时间',
    visit_end_time DATETIME NOT NULL COMMENT '访问结束时间',
    visitor_count INT DEFAULT 1 COMMENT '访客人数',
    vehicle_info JSON COMMENT '车辆信息',
    approve_status TINYINT DEFAULT 1 COMMENT '审批状态 1-待审批 2-已通过 3-已驳回',
    approve_time DATETIME COMMENT '审批时间',
    approve_user_id BIGINT COMMENT '审批人ID',
    approve_remark VARCHAR(500) COMMENT '审批备注',
    status TINYINT DEFAULT 1 COMMENT '状态 1-有效 2-已完成 3-已取消',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',
    INDEX idx_appointment_no (appointment_no),
    INDEX idx_visitor_phone (visitor_phone),
    INDEX idx_host_user_id (host_user_id),
    INDEX idx_visit_area_id (visit_area_id),
    INDEX idx_visit_start_time (visit_start_time),
    INDEX idx_approve_status (approve_status),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='访客预约表';

-- 4.2 访客记录表
DROP TABLE IF EXISTS t_visitor_record;
CREATE TABLE t_visitor_record (
    record_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    appointment_id BIGINT COMMENT '预约ID',
    visitor_name VARCHAR(100) NOT NULL COMMENT '访客姓名',
    visitor_phone VARCHAR(20) COMMENT '访客手机号',
    visitor_company VARCHAR(200) COMMENT '访客单位',
    visitor_id_card VARCHAR(20) COMMENT '访客身份证号',
    host_user_id BIGINT COMMENT '接待人ID',
    host_user_name VARCHAR(100) COMMENT '接待人姓名',
    visit_area_id BIGINT COMMENT '访问区域ID',
    visit_area_name VARCHAR(100) COMMENT '访问区域名称',
    visit_type VARCHAR(20) DEFAULT 'APPOINTMENT' COMMENT '访问类型 APPOINTMENT-预约 TEMPORARY-临时',
    arrive_time DATETIME COMMENT '到达时间',
    leave_time DATETIME COMMENT '离开时间',
    access_device_id BIGINT COMMENT '通行设备ID',
    verify_mode VARCHAR(20) COMMENT '验证方式 FACE-人脸 ID_CARD-身份证 QR_CODE-二维码',
    photo_url VARCHAR(500) COMMENT '访客照片',
    id_card_image_url VARCHAR(500) COMMENT '身份证照片URL',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_appointment_id (appointment_id),
    INDEX idx_visitor_phone (visitor_phone),
    INDEX idx_host_user_id (host_user_id),
    INDEX idx_visit_area_id (visit_area_id),
    INDEX idx_arrive_time (arrive_time),
    INDEX idx_visit_type (visit_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='访客记录表';

-- =====================================================
-- 5. 视频监控数据库 (ioedream_video_db)
-- =====================================================

USE ioedream_video_db;

-- 5.1 视频设备表
DROP TABLE IF EXISTS t_video_device;
CREATE TABLE t_video_device (
    device_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '设备ID',
    device_no VARCHAR(50) NOT NULL UNIQUE COMMENT '设备编号',
    device_name VARCHAR(100) NOT NULL COMMENT '设备名称',
    device_type VARCHAR(20) NOT NULL COMMENT '设备类型 CAMERA-摄像头 NVR-录像机 DVR-硬盘录像机',
    area_id BIGINT NOT NULL COMMENT '所属区域ID',
    location VARCHAR(200) COMMENT '设备位置',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    port INT COMMENT '端口',
    username VARCHAR(50) COMMENT '设备用户名',
    password VARCHAR(100) COMMENT '设备密码',
    rtsp_url VARCHAR(500) COMMENT 'RTSP流地址',
    hls_url VARCHAR(500) COMMENT 'HLS流地址',
    flv_url VARCHAR(500) COMMENT 'FLV流地址',
    status TINYINT DEFAULT 1 COMMENT '状态 1-在线 0-离线',
    last_online_time DATETIME COMMENT '最后在线时间',
    resolution VARCHAR(20) COMMENT '分辨率',
    fps INT COMMENT '帧率',
    bitrate INT COMMENT '码率',
    config JSON COMMENT '设备配置信息',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',
    INDEX idx_device_no (device_no),
    INDEX idx_area_id (area_id),
    INDEX idx_device_type (device_type),
    INDEX idx_status (status),
    INDEX idx_ip_address (ip_address)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频设备表';

-- =====================================================
-- 6. 设备管理数据库 (ioedream_device_db)
-- =====================================================

USE ioedream_device_db;

-- 6.1 统一设备表
DROP TABLE IF EXISTS t_device;
CREATE TABLE t_device (
    device_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '设备ID',
    device_no VARCHAR(50) NOT NULL UNIQUE COMMENT '设备编号',
    device_name VARCHAR(100) NOT NULL COMMENT '设备名称',
    device_type VARCHAR(20) NOT NULL COMMENT '设备类型 CAMERA-摄像头 ACCESS-门禁 CONSUME-消费机 ATTENDANCE-考勤机 BIOMETRIC-生物识别 INTERCOM-对讲机 ALARM-报警器 SENSOR-传感器',
    device_category VARCHAR(20) COMMENT '设备大类',
    device_model VARCHAR(50) COMMENT '设备型号',
    manufacturer VARCHAR(100) COMMENT '厂商',
    area_id BIGINT NOT NULL COMMENT '所属区域ID',
    location VARCHAR(200) COMMENT '设备位置',
    coordinates JSON COMMENT '地理坐标',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    port INT COMMENT '端口',
    mac_address VARCHAR(20) COMMENT 'MAC地址',
    serial_no VARCHAR(50) COMMENT '序列号',
    install_date DATE COMMENT '安装日期',
    warranty_end_date DATE COMMENT '保修结束日期',
    responsible_user_id BIGINT COMMENT '负责人ID',
    responsible_department VARCHAR(100) COMMENT '负责部门',
    status TINYINT DEFAULT 1 COMMENT '状态 1-正常 2-故障 3-维护 0-停用',
    online_status TINYINT DEFAULT 1 COMMENT '在线状态 1-在线 0-离线',
    last_online_time DATETIME COMMENT '最后在线时间',
    health_score INT DEFAULT 100 COMMENT '健康评分 0-100',
    config JSON COMMENT '设备配置信息',
    extended_attributes JSON COMMENT '扩展属性',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',
    INDEX idx_device_no (device_no),
    INDEX idx_device_type (device_type),
    INDEX idx_area_id (area_id),
    INDEX idx_status (status),
    INDEX idx_online_status (online_status),
    INDEX idx_responsible_user (responsible_user_id),
    INDEX idx_manufacturer (manufacturer),
    INDEX idx_ip_address (ip_address)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='统一设备表';

SELECT '所有业务数据库表结构创建完成' as message,
       COUNT(*) as table_count
FROM (
    SELECT TABLE_SCHEMA as db_name, COUNT(*) as table_count
    FROM INFORMATION_SCHEMA.TABLES
    WHERE TABLE_SCHEMA IN ('ioedream_access_db', 'ioedream_attendance_db', 'ioedream_consume_db', 'ioedream_visitor_db', 'ioedream_video_db', 'ioedream_device_db')
      AND TABLE_TYPE = 'BASE TABLE'
    GROUP BY TABLE_SCHEMA
) as summary;