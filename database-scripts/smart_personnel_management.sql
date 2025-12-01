-- ========================================
-- 智能人员管理系统数据库脚本
-- 支持人员基础信息、权限管理、统计分析、考勤管理等功能
-- ========================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS smart_admin_v3 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE smart_admin_v3;

-- ========================================
-- 1. 人员基础信息表 (t_personnel)
-- ========================================
CREATE TABLE IF NOT EXISTS t_personnel (
    personnel_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '人员ID',
    personnel_code VARCHAR(50) NOT NULL UNIQUE COMMENT '人员编码',
    personnel_name VARCHAR(100) NOT NULL COMMENT '人员姓名',
    personnel_type ENUM('EMPLOYEE', 'CONTRACTOR', 'VISITOR', 'TEMPORARY', 'INTERN') NOT NULL DEFAULT 'EMPLOYEE' COMMENT '人员类型',
    employee_id BIGINT COMMENT '关联员工ID',
    id_card_number VARCHAR(18) COMMENT '身份证号码',
    phone_number VARCHAR(20) COMMENT '手机号码',
    email VARCHAR(100) COMMENT '电子邮箱',

    -- 组织信息
    department_id BIGINT COMMENT '部门ID',
    department_name VARCHAR(100) COMMENT '部门名称',
    position_id BIGINT COMMENT '职位ID',
    position_name VARCHAR(100) COMMENT '职位名称',
    manager_id BIGINT COMMENT '直属上级ID',
    manager_name VARCHAR(100) COMMENT '直属上级姓名',

    -- 个人信息
    gender ENUM('MALE', 'FEMALE', 'UNKNOWN') COMMENT '性别',
    birth_date DATE COMMENT '出生日期',
    age INT COMMENT '年龄',
    education_level ENUM('HIGH_SCHOOL', 'COLLEGE', 'BACHELOR', 'MASTER', 'DOCTOR', 'OTHER') COMMENT '学历水平',
    major VARCHAR(100) COMMENT '专业',
    work_experience_years INT DEFAULT 0 COMMENT '工作年限',
    join_date DATE COMMENT '入职日期',
    leave_date DATE COMMENT '离职日期',
    employment_status ENUM('ACTIVE', 'INACTIVE', 'RESIGNED', 'RETIRED') DEFAULT 'ACTIVE' COMMENT '在职状态',

    -- 权限信息
    role_ids VARCHAR(500) COMMENT '角色ID列表(逗号分隔)',
    permission_group_id BIGINT COMMENT '权限组ID',
    access_level ENUM('LEVEL_1', 'LEVEL_2', 'LEVEL_3', 'LEVEL_4', 'LEVEL_5') DEFAULT 'LEVEL_3' COMMENT '访问级别',
    security_clearance ENUM('PUBLIC', 'CONFIDENTIAL', 'SECRET', 'TOP_SECRET') DEFAULT 'PUBLIC' COMMENT '安全级别',

    -- 地理信息
    work_area_id BIGINT COMMENT '工作区域ID',
    work_area_name VARCHAR(100) COMMENT '工作区域名称',
    home_address TEXT COMMENT '家庭住址',
    home_area_id BIGINT COMMENT '居住区域ID',
    home_area_name VARCHAR(100) COMMENT '居住区域名称',

    -- 生物特征
    fingerprint_template TEXT COMMENT '指纹模板数据',
    face_feature TEXT COMMENT '面部特征数据',
    iris_template TEXT COMMENT '虹膜模板数据',
    voice_print TEXT COMMENT '声纹数据',

    -- 系统字段
    avatar_url VARCHAR(500) COMMENT '头像URL',
    remark TEXT COMMENT '备注信息',
    sort_order INT DEFAULT 0 COMMENT '排序序号',
    status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED', 'DELETED') DEFAULT 'ACTIVE' COMMENT '状态',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    deleted_flag TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记(0:正常,1:删除)',
    extended_properties JSON COMMENT '扩展属性',

    INDEX idx_personnel_code (personnel_code),
    INDEX idx_employee_id (employee_id),
    INDEX idx_department_id (department_id),
    INDEX idx_position_id (position_id),
    INDEX idx_personnel_type (personnel_type),
    INDEX idx_employment_status (employment_status),
    INDEX idx_work_area_id (work_area_id),
    INDEX idx_access_level (access_level),
    INDEX idx_create_time (create_time),
    INDEX idx_deleted_flag (deleted_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='人员基础信息表';

-- ========================================
-- 2. 人员权限表 (t_personnel_permission)
-- ========================================
CREATE TABLE IF NOT EXISTS t_personnel_permission (
    permission_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '权限ID',
    personnel_id BIGINT NOT NULL COMMENT '人员ID',
    resource_type ENUM('MENU', 'BUTTON', 'API', 'DATA', 'AREA', 'DEVICE') NOT NULL COMMENT '资源类型',
    resource_id VARCHAR(100) COMMENT '资源ID',
    resource_name VARCHAR(200) COMMENT '资源名称',
    permission_code VARCHAR(100) NOT NULL COMMENT '权限代码',
    permission_name VARCHAR(200) NOT NULL COMMENT '权限名称',
    permission_type ENUM('ALLOW', 'DENY', 'INHERIT') DEFAULT 'ALLOW' COMMENT '权限类型',

    -- 时间限制
    start_time DATETIME COMMENT '生效开始时间',
    end_time DATETIME COMMENT '生效结束时间',
    time_restrictions JSON COMMENT '时间限制配置',

    -- 地域限制
    area_restrictions JSON COMMENT '地域限制配置',
    device_restrictions JSON COMMENT '设备限制配置',
    ip_restrictions JSON COMMENT 'IP限制配置',

    -- 其他限制
    max_attempts INT DEFAULT 0 COMMENT '最大尝试次数(0表示无限制)',
    attempt_period INT DEFAULT 3600 COMMENT '尝试周期(秒)',
    cooldown_period INT DEFAULT 300 COMMENT '冷却期(秒)',

    -- 系统字段
    grant_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '授权时间',
    grant_user_id BIGINT COMMENT '授权人ID',
    grant_user_name VARCHAR(100) COMMENT '授权人姓名',
    grant_reason VARCHAR(500) COMMENT '授权原因',
    last_used_time DATETIME COMMENT '最后使用时间',
    use_count INT DEFAULT 0 COMMENT '使用次数',

    status ENUM('ACTIVE', 'INACTIVE', 'EXPIRED', 'REVOKED') DEFAULT 'ACTIVE' COMMENT '状态',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记',

    UNIQUE KEY uk_personnel_permission (personnel_id, resource_type, resource_id, permission_code),
    INDEX idx_personnel_id (personnel_id),
    INDEX idx_permission_code (permission_code),
    INDEX idx_resource_type (resource_type),
    INDEX idx_resource_id (resource_id),
    INDEX idx_status (status),
    INDEX idx_start_end_time (start_time, end_time),
    INDEX idx_grant_time (grant_time),
    INDEX idx_deleted_flag (deleted_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='人员权限表';

-- ========================================
-- 3. 人员区域关联表 (t_personnel_area)
-- ========================================
CREATE TABLE IF NOT EXISTS t_personnel_area (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '关联ID',
    personnel_id BIGINT NOT NULL COMMENT '人员ID',
    area_id BIGINT NOT NULL COMMENT '区域ID',
    area_type ENUM('WORK', 'HOME', 'ACCESS', 'MANAGE') NOT NULL DEFAULT 'WORK' COMMENT '区域类型',
    access_level ENUM('READ', 'WRITE', 'ADMIN', 'SUPER') DEFAULT 'READ' COMMENT '访问级别',
    is_primary TINYINT(1) DEFAULT 0 COMMENT '是否主区域',

    -- 权限设置
    can_enter TINYINT(1) DEFAULT 1 COMMENT '可以进入',
    can_exit TINYINT(1) DEFAULT 1 COMMENT '可以离开',
    can_manage TINYINT(1) DEFAULT 0 COMMENT '可以管理',
    can_view_data TINYINT(1) DEFAULT 1 COMMENT '可以查看数据',
    can_control_device TINYINT(1) DEFAULT 0 COMMENT '可以控制设备',

    -- 时间限制
    access_time_rules JSON COMMENT '访问时间规则',
    effective_start_date DATE COMMENT '生效开始日期',
    effective_end_date DATE COMMENT '生效结束日期',

    -- 系统字段
    assign_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '分配时间',
    assign_user_id BIGINT COMMENT '分配人ID',
    assign_user_name VARCHAR(100) COMMENT '分配人姓名',
    assign_reason VARCHAR(500) COMMENT '分配原因',
    last_access_time DATETIME COMMENT '最后访问时间',
    access_count INT DEFAULT 0 COMMENT '访问次数',

    status ENUM('ACTIVE', 'INACTIVE', 'EXPIRED') DEFAULT 'ACTIVE' COMMENT '状态',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记',

    UNIQUE KEY uk_personnel_area (personnel_id, area_id, area_type),
    INDEX idx_personnel_id (personnel_id),
    INDEX idx_area_id (area_id),
    INDEX idx_area_type (area_type),
    INDEX idx_access_level (access_level),
    INDEX idx_status (status),
    INDEX idx_assign_time (assign_time),
    INDEX idx_deleted_flag (deleted_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='人员区域关联表';

-- ========================================
-- 4. 人员考勤记录表 (t_personnel_attendance)
-- ========================================
CREATE TABLE IF NOT EXISTS t_personnel_attendance (
    attendance_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '考勤记录ID',
    personnel_id BIGINT NOT NULL COMMENT '人员ID',
    attendance_date DATE NOT NULL COMMENT '考勤日期',
    work_day_type ENUM('WEEKDAY', 'WEEKEND', 'HOLIDAY') NOT NULL COMMENT '工作日类型',
    shift_type ENUM('MORNING', 'AFTERNOON', 'NIGHT', 'ROTATING', 'FLEXIBLE') COMMENT '班次类型',
    shift_start_time TIME COMMENT '班次开始时间',
    shift_end_time TIME COMMENT '班次结束时间',

    -- 打卡记录
    clock_in_time DATETIME COMMENT '上班打卡时间',
    clock_in_location VARCHAR(200) COMMENT '上班打卡位置',
    clock_in_device_id BIGINT COMMENT '上班打卡设备ID',
    clock_in_method ENUM('FACE', 'FINGERPRINT', 'CARD', 'PASSWORD', 'MOBILE', 'MANUAL') COMMENT '打卡方式',

    clock_out_time DATETIME COMMENT '下班打卡时间',
    clock_out_location VARCHAR(200) COMMENT '下班打卡位置',
    clock_out_device_id BIGINT COMMENT '下班打卡设备ID',
    clock_out_method ENUM('FACE', 'FINGERPRINT', 'CARD', 'PASSWORD', 'MOBILE', 'MANUAL') COMMENT '打卡方式',

    -- 异常记录
    late_minutes INT DEFAULT 0 COMMENT '迟到分钟数',
    early_leave_minutes INT DEFAULT 0 COMMENT '早退分钟数',
    overtime_minutes INT DEFAULT 0 COMMENT '加班分钟数',
    break_minutes INT DEFAULT 0 COMMENT '休息分钟数',

    -- 请假记录
    leave_type ENUM('SICK', 'PERSONAL', 'VACATION', 'MATERNITY', 'COMPASSIONATE', 'UNPAID') COMMENT '请假类型',
    leave_hours DECIMAL(5,2) DEFAULT 0.00 COMMENT '请假小时数',
    leave_start_time DATETIME COMMENT '请假开始时间',
    leave_end_time DATETIME COMMENT '请假结束时间',
    leave_reason VARCHAR(500) COMMENT '请假原因',
    leave_approver_id BIGINT COMMENT '请假审批人ID',

    -- 出差记录
    is_business_trip TINYINT(1) DEFAULT 0 COMMENT '是否出差',
    trip_location VARCHAR(200) COMMENT '出差地点',
    trip_start_time DATETIME COMMENT '出差开始时间',
    trip_end_time DATETIME COMMENT '出差结束时间',
    trip_approver_id BIGINT COMMENT '出差审批人ID',

    -- 考勤状态
    attendance_status ENUM('NORMAL', 'LATE', 'EARLY_LEAVE', 'ABSENTEEISM', 'LEAVE', 'BUSINESS_TRIP', 'HOLIDAY') DEFAULT 'NORMAL' COMMENT '考勤状态',
    work_hours DECIMAL(5,2) DEFAULT 0.00 COMMENT '工作小时数',
    effective_work_hours DECIMAL(5,2) DEFAULT 0.00 COMMENT '有效工作小时数',

    -- 系统字段
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    remark TEXT COMMENT '备注信息',

    UNIQUE KEY uk_personnel_date (personnel_id, attendance_date),
    INDEX idx_personnel_id (personnel_id),
    INDEX idx_attendance_date (attendance_date),
    INDEX idx_shift_type (shift_type),
    INDEX idx_attendance_status (attendance_status),
    INDEX idx_clock_in_time (clock_in_time),
    INDEX idx_clock_out_time (clock_out_time),
    INDEX idx_leave_type (leave_type),
    INDEX idx_is_business_trip (is_business_trip),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='人员考勤记录表';

-- ========================================
-- 5. 人员统计分析表 (t_personnel_statistics)
-- ========================================
CREATE TABLE IF NOT EXISTS t_personnel_statistics (
    stat_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '统计ID',
    personnel_id BIGINT NOT NULL COMMENT '人员ID',
    stat_date DATE NOT NULL COMMENT '统计日期',
    stat_type ENUM('DAILY', 'WEEKLY', 'MONTHLY', 'QUARTERLY', 'YEARLY') NOT NULL COMMENT '统计类型',
    stat_period VARCHAR(50) COMMENT '统计周期',

    -- 基础统计
    total_work_days INT DEFAULT 0 COMMENT '总工作天数',
    actual_work_days INT DEFAULT 0 COMMENT '实际工作天数',
    leave_days DECIMAL(5,2) DEFAULT 0.00 COMMENT '请假天数',
    business_trip_days DECIMAL(5,2) DEFAULT 0.00 COMMENT '出差天数',
    absent_days INT DEFAULT 0 COMMENT '缺勤天数',

    -- 时间统计
    total_work_hours DECIMAL(8,2) DEFAULT 0.00 COMMENT '总工作小时数',
    effective_work_hours DECIMAL(8,2) DEFAULT 0.00 COMMENT '有效工作小时数',
    overtime_hours DECIMAL(8,2) DEFAULT 0.00 COMMENT '加班小时数',
    late_count INT DEFAULT 0 COMMENT '迟到次数',
    early_leave_count INT DEFAULT 0 COMMENT '早退次数',
    absent_count INT DEFAULT 0 COMMENT '缺勤次数',

    -- 考勤质量
    attendance_rate DECIMAL(5,2) DEFAULT 0.00 COMMENT '出勤率',
    punctuality_rate DECIMAL(5,2) DEFAULT 0.00 COMMENT '准时率',
    work_efficiency DECIMAL(5,2) DEFAULT 0.00 COMMENT '工作效率评分',
    performance_score DECIMAL(5,2) DEFAULT 0.00 COMMENT '绩效评分',

    -- 访问统计
    area_access_count INT DEFAULT 0 COMMENT '区域访问次数',
    device_usage_count INT DEFAULT 0 COMMENT '设备使用次数',
    system_login_count INT DEFAULT 0 COMMENT '系统登录次数',

    -- 权限使用
    permission_use_count INT DEFAULT 0 COMMENT '权限使用次数',
    high_risk_operation_count INT DEFAULT 0 COMMENT '高风险操作次数',
    security_violation_count INT DEFAULT 0 COMMENT '安全违规次数',

    -- 系统字段
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    UNIQUE KEY uk_personnel_stat (personnel_id, stat_date, stat_type),
    INDEX idx_personnel_id (personnel_id),
    INDEX idx_stat_date (stat_date),
    INDEX idx_stat_type (stat_type),
    INDEX idx_stat_period (stat_period),
    INDEX idx_attendance_rate (attendance_rate),
    INDEX idx_performance_score (performance_score),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='人员统计分析表';

-- ========================================
-- 6. 人员生物特征表 (t_personnel_biometric)
-- ========================================
CREATE TABLE IF NOT EXISTS t_personnel_biometric (
    biometric_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '生物特征ID',
    personnel_id BIGINT NOT NULL COMMENT '人员ID',
    biometric_type ENUM('FINGERPRINT', 'FACE', 'IRIS', 'VOICE', 'PALM', 'SIGNATURE') NOT NULL COMMENT '生物特征类型',
    biometric_data TEXT NOT NULL COMMENT '生物特征数据',
    template_version VARCHAR(50) COMMENT '模板版本',
    quality_score DECIMAL(5,2) DEFAULT 0.00 COMMENT '质量评分',

    -- 采集信息
    capture_device_id BIGINT COMMENT '采集设备ID',
    capture_device_name VARCHAR(200) COMMENT '采集设备名称',
    capture_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '采集时间',
    capture_location VARCHAR(200) COMMENT '采集位置',
    capture_operator_id BIGINT COMMENT '采集操作员ID',

    -- 验证信息
    last_verify_time DATETIME COMMENT '最后验证时间',
    verify_count INT DEFAULT 0 COMMENT '验证次数',
    success_count INT DEFAULT 0 COMMENT '成功次数',
    failure_count INT DEFAULT 0 COMMENT '失败次数',
    false_acceptance_rate DECIMAL(8,6) DEFAULT 0.000000 COMMENT '误识率',
    false_rejection_rate DECIMAL(8,6) DEFAULT 0.000000 COMMENT '拒识率',

    -- 安全信息
    is_encrypted TINYINT(1) DEFAULT 1 COMMENT '是否加密存储',
    encryption_algorithm VARCHAR(50) COMMENT '加密算法',
    salt_value VARCHAR(100) COMMENT '盐值',
    expiry_date DATETIME COMMENT '过期时间',

    -- 系统字段
    status ENUM('ACTIVE', 'INACTIVE', 'EXPIRED', 'REVOKED') DEFAULT 'ACTIVE' COMMENT '状态',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    remark TEXT COMMENT '备注信息',

    UNIQUE KEY uk_personnel_biometric (personnel_id, biometric_type),
    INDEX idx_personnel_id (personnel_id),
    INDEX idx_biometric_type (biometric_type),
    INDEX idx_status (status),
    INDEX idx_capture_time (capture_time),
    INDEX idx_last_verify_time (last_verify_time),
    INDEX idx_quality_score (quality_score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='人员生物特征表';

-- ========================================
-- 7. 人员审批记录表 (t_personnel_approval)
-- ========================================
CREATE TABLE IF NOT EXISTS t_personnel_approval (
    approval_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '审批ID',
    personnel_id BIGINT NOT NULL COMMENT '申请人ID',
    approval_type ENUM('LEAVE', 'OVERTIME', 'BUSINESS_TRIP', 'PERMISSION_APPLY', 'AREA_ACCESS', 'DEVICE_CONTROL', 'DATA_EXPORT') NOT NULL COMMENT '审批类型',
    request_data JSON COMMENT '申请数据',
    request_reason VARCHAR(1000) COMMENT '申请原因',

    -- 时间信息
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    duration_hours DECIMAL(8,2) COMMENT '持续小时数',

    -- 审批流程
    approval_level INT DEFAULT 1 COMMENT '审批层级',
    current_approver_id BIGINT COMMENT '当前审批人ID',
    current_approver_name VARCHAR(100) COMMENT '当前审批人姓名',
    next_approver_id BIGINT COMMENT '下一审批人ID',

    -- 审批结果
    approval_status ENUM('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED', 'EXPIRED') DEFAULT 'PENDING' COMMENT '审批状态',
    approval_result VARCHAR(500) COMMENT '审批结果',
    approval_comment VARCHAR(1000) COMMENT '审批意见',
    approve_time DATETIME COMMENT '审批时间',
    approve_user_id BIGINT COMMENT '审批人ID',
    approve_user_name VARCHAR(100) COMMENT '审批人姓名',

    -- 紧急程度
    urgency_level ENUM('LOW', 'NORMAL', 'HIGH', 'URGENT') DEFAULT 'NORMAL' COMMENT '紧急程度',
    auto_approve TINYINT(1) DEFAULT 0 COMMENT '是否自动审批',

    -- 系统字段
    request_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',

    INDEX idx_personnel_id (personnel_id),
    INDEX idx_approval_type (approval_type),
    INDEX idx_approval_status (approval_status),
    INDEX idx_current_approver_id (current_approver_id),
    INDEX idx_request_time (request_time),
    INDEX idx_approve_time (approve_time),
    INDEX idx_urgency_level (urgency_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='人员审批记录表';

-- ========================================
-- 8. 人员访问日志表 (t_personnel_access_log)
-- ========================================
CREATE TABLE IF NOT EXISTS t_personnel_access_log (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    personnel_id BIGINT NOT NULL COMMENT '人员ID',
    access_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '访问时间',
    access_type ENUM('LOGIN', 'LOGOUT', 'AREA_ENTER', 'AREA_EXIT', 'DEVICE_USE', 'DATA_ACCESS', 'PERMISSION_USE') NOT NULL COMMENT '访问类型',

    -- 资源信息
    resource_type ENUM('SYSTEM', 'AREA', 'DEVICE', 'DATA', 'APPLICATION', 'API') COMMENT '资源类型',
    resource_id VARCHAR(100) COMMENT '资源ID',
    resource_name VARCHAR(200) COMMENT '资源名称',

    -- 位置信息
    access_location VARCHAR(200) COMMENT '访问位置',
    area_id BIGINT COMMENT '区域ID',
    area_name VARCHAR(100) COMMENT '区域名称',
    device_id BIGINT COMMENT '设备ID',
    device_name VARCHAR(200) COMMENT '设备名称',

    -- 认证信息
    auth_method ENUM('PASSWORD', 'FACE', 'FINGERPRINT', 'CARD', 'TOKEN', 'BIOMETRIC') COMMENT '认证方式',
    auth_result ENUM('SUCCESS', 'FAILURE', 'TIMEOUT', 'LOCKED', 'EXPIRED') COMMENT '认证结果',
    failure_reason VARCHAR(200) COMMENT '失败原因',

    -- 系统信息
    client_ip VARCHAR(50) COMMENT '客户端IP',
    user_agent VARCHAR(500) COMMENT '用户代理',
    session_id VARCHAR(100) COMMENT '会话ID',
    operation_id VARCHAR(100) COMMENT '操作ID',

    -- 安全信息
    risk_score DECIMAL(5,2) DEFAULT 0.00 COMMENT '风险评分',
    is_suspicious TINYINT(1) DEFAULT 0 COMMENT '是否可疑',
    security_alert TINYINT(1) DEFAULT 0 COMMENT '是否安全告警',

    -- 系统字段
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    extra_data JSON COMMENT '扩展数据',

    INDEX idx_personnel_id (personnel_id),
    INDEX idx_access_time (access_time),
    INDEX idx_access_type (access_type),
    INDEX idx_resource_type (resource_type),
    INDEX idx_resource_id (resource_id),
    INDEX idx_area_id (area_id),
    INDEX idx_device_id (device_id),
    INDEX idx_auth_result (auth_result),
    INDEX idx_client_ip (client_ip),
    INDEX idx_risk_score (risk_score),
    INDEX idx_is_suspicious (is_suspicious),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='人员访问日志表';

-- ========================================
-- 创建触发器：自动更新时间戳
-- ========================================
DELIMITER //

-- 人员基础信息表更新时间戳触发器
CREATE TRIGGER tr_personnel_update_time
BEFORE UPDATE ON t_personnel
FOR EACH ROW
BEGIN
    SET NEW.update_time = CURRENT_TIMESTAMP;
END//

-- 人员权限表更新时间戳触发器
CREATE TRIGGER tr_personnel_permission_update_time
BEFORE UPDATE ON t_personnel_permission
FOR EACH ROW
BEGIN
    SET NEW.update_time = CURRENT_TIMESTAMP;
END//

-- 人员区域关联表更新时间戳触发器
CREATE TRIGGER tr_personnel_area_update_time
BEFORE UPDATE ON t_personnel_area
FOR EACH ROW
BEGIN
    SET NEW.update_time = CURRENT_TIMESTAMP;
END//

-- 人员考勤记录表更新时间戳触发器
CREATE TRIGGER tr_personnel_attendance_update_time
BEFORE UPDATE ON t_personnel_attendance
FOR EACH ROW
BEGIN
    SET NEW.update_time = CURRENT_TIMESTAMP;
END//

-- 人员统计分析表更新时间戳触发器
CREATE TRIGGER tr_personnel_statistics_update_time
BEFORE UPDATE ON t_personnel_statistics
FOR EACH ROW
BEGIN
    SET NEW.update_time = CURRENT_TIMESTAMP;
END//

-- 人员生物特征表更新时间戳触发器
CREATE TRIGGER tr_personnel_biometric_update_time
BEFORE UPDATE ON t_personnel_biometric
FOR EACH ROW
BEGIN
    SET NEW.update_time = CURRENT_TIMESTAMP;
END//

-- 人员审批记录表更新时间戳触发器
CREATE TRIGGER tr_personnel_approval_update_time
BEFORE UPDATE ON t_personnel_approval
FOR EACH ROW
BEGIN
    SET NEW.update_time = CURRENT_TIMESTAMP;
END//

DELIMITER ;

-- ========================================
-- 初始化数据
-- ========================================

-- 插入示例数据（可选）
-- 这里可以根据需要插入一些初始的测试数据

-- ========================================
-- 性能优化建议
-- ========================================
-- 1. 定期清理过期的访问日志（建议保留1年）
-- 2. 对生物特征数据进行加密存储
-- 3. 定期备份重要的考勤和统计数据
-- 4. 为常用查询创建复合索引
-- 5. 考虑对大表进行分区（按时间分区）

-- ========================================
-- 数据库脚本创建完成
-- ========================================