-- =====================================================
-- IOE-DREAM 数据库初始架构脚本
-- 版本: V1.0.0
-- 描述: 创建所有基础表结构，为V2.0.x版本提供基础
-- 兼容: 确保所有业务模块基础表结构完整
-- 创建时间: 2025-01-30
-- 执行顺序: 第一批执行的迁移脚本
-- =====================================================

-- 设置执行环境
SET FOREIGN_KEY_CHECKS = 0;
SET SQL_MODE = 'STRICT_TRANS_TABLES,NO_ZERO_DATE,NO_ZERO_IN_DATE,ERROR_FOR_DIVISION_BY_ZERO';

-- =====================================================
-- 1. 创建数据库和基础配置
-- =====================================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS ioedream
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_unicode_ci;

USE ioedream;

-- 创建迁移历史表
CREATE TABLE IF NOT EXISTS t_migration_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    version VARCHAR(50) NOT NULL COMMENT '版本号',
    description TEXT COMMENT '版本描述',
    script_name VARCHAR(200) COMMENT '脚本文件名',
    status VARCHAR(20) DEFAULT 'SUCCESS' COMMENT '执行状态',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE INDEX uk_version (version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据库迁移历史表';

-- =====================================================
-- 2. 公共模块基础表
-- =====================================================

-- 用户表
CREATE TABLE IF NOT EXISTS t_common_user (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（加密）',
    real_name VARCHAR(100) COMMENT '真实姓名',
    nickname VARCHAR(50) COMMENT '昵称',
    gender TINYINT DEFAULT 1 COMMENT '性别：1-男 2-女',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    avatar VARCHAR(500) COMMENT '头像URL',
    birthday DATE COMMENT '生日',
    address VARCHAR(500) COMMENT '地址',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常 2-禁用',
    department_id BIGINT COMMENT '部门ID',
    position VARCHAR(100) COMMENT '职位',
    employee_no VARCHAR(50) COMMENT '员工编号',
    last_login_time DATETIME COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) COMMENT '最后登录IP',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',

    UNIQUE INDEX uk_username (username, deleted_flag),
    INDEX idx_phone (phone),
    INDEX idx_email (email),
    INDEX idx_department_id (department_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS t_common_role (
    role_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '角色ID',
    role_name VARCHAR(100) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
    description TEXT COMMENT '角色描述',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常 2-禁用',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',

    UNIQUE INDEX uk_role_code (role_code, deleted_flag),
    INDEX idx_role_name (role_name),
    INDEX idx_status (status),
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 权限表
CREATE TABLE IF NOT EXISTS t_common_permission (
    permission_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '权限ID',
    permission_name VARCHAR(100) NOT NULL COMMENT '权限名称',
    permission_code VARCHAR(100) NOT NULL COMMENT '权限编码',
    resource_type VARCHAR(20) DEFAULT 'MENU' COMMENT '资源类型：MENU-菜单 BUTTON-按钮 API-接口',
    resource_url VARCHAR(500) COMMENT '资源URL',
    resource_method VARCHAR(10) COMMENT 'HTTP方法',
    parent_id BIGINT DEFAULT 0 COMMENT '父权限ID',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常 2-禁用',
    icon VARCHAR(100) COMMENT '图标',
    component VARCHAR(200) COMMENT '组件路径',
    is_external TINYINT DEFAULT 0 COMMENT '是否外部链接：0-否 1-是',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',

    UNIQUE INDEX uk_permission_code (permission_code, deleted_flag),
    INDEX idx_permission_name (permission_name),
    INDEX idx_parent_id (parent_id),
    INDEX idx_resource_type (resource_type),
    INDEX idx_status (status),
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS t_common_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_user_id BIGINT COMMENT '创建人ID',

    UNIQUE INDEX uk_user_role (user_id, role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS t_common_role_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_user_id BIGINT COMMENT '创建人ID',

    UNIQUE INDEX uk_role_permission (role_id, permission_id),
    INDEX idx_role_id (role_id),
    INDEX idx_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- 部门表
CREATE TABLE IF NOT EXISTS t_common_department (
    department_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '部门ID',
    department_name VARCHAR(200) NOT NULL COMMENT '部门名称',
    department_code VARCHAR(50) COMMENT '部门编码',
    parent_id BIGINT DEFAULT 0 COMMENT '父部门ID',
    level INT DEFAULT 1 COMMENT '层级',
    sort_order INT DEFAULT 0 COMMENT '排序',
    leader_id BIGINT COMMENT '负责人ID',
    phone VARCHAR(20) COMMENT '联系电话',
    email VARCHAR(100) COMMENT '邮箱',
    description TEXT COMMENT '描述',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常 2-禁用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',

    UNIQUE INDEX uk_department_code (department_code, deleted_flag),
    INDEX idx_department_name (department_name),
    INDEX idx_parent_id (parent_id),
    INDEX idx_leader_id (leader_id),
    INDEX idx_status (status),
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门表';

-- 区域表
CREATE TABLE IF NOT EXISTS t_common_area (
    area_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '区域ID',
    area_name VARCHAR(200) NOT NULL COMMENT '区域名称',
    area_code VARCHAR(50) COMMENT '区域编码',
    area_type VARCHAR(20) DEFAULT 'BUILDING' COMMENT '区域类型：CAMPUS-园区 BUILDING-建筑 FLOOR-楼层 ROOM-房间',
    parent_id BIGINT DEFAULT 0 COMMENT '父区域ID',
    level INT DEFAULT 1 COMMENT '层级',
    sort_order INT DEFAULT 0 COMMENT '排序',
    manager_id BIGINT COMMENT '管理员ID',
    capacity INT COMMENT '容量',
    description TEXT COMMENT '描述',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常 2-禁用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',

    UNIQUE INDEX uk_area_code (area_code, deleted_flag),
    INDEX idx_area_name (area_name),
    INDEX idx_area_type (area_type),
    INDEX idx_parent_id (parent_id),
    INDEX idx_manager_id (manager_id),
    INDEX idx_status (status),
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='区域表';

-- 设备表（统一设备管理）
CREATE TABLE IF NOT EXISTS t_common_device (
    device_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '设备ID',
    device_no VARCHAR(100) NOT NULL COMMENT '设备编号',
    device_name VARCHAR(200) COMMENT '设备名称',
    device_type VARCHAR(50) NOT NULL COMMENT '设备类型：CAMERA-摄像头 ACCESS-门禁 CONSUME-消费机 ATTENDANCE-考勤机 BIOMETRIC-生物识别',
    device_model VARCHAR(100) COMMENT '设备型号',
    manufacturer VARCHAR(100) COMMENT '制造商',
    serial_number VARCHAR(100) COMMENT '序列号',
    area_id BIGINT COMMENT '所在区域ID',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    port INT COMMENT '端口',
    status TINYINT DEFAULT 1 COMMENT '状态：1-在线 2-离线 3-故障 4-维护',
    install_time DATETIME COMMENT '安装时间',
    last_active_time DATETIME COMMENT '最后活跃时间',
    description TEXT COMMENT '设备描述',
    extended_attributes TEXT COMMENT '扩展属性JSON',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',

    UNIQUE INDEX uk_device_no (device_no, deleted_flag),
    INDEX idx_device_name (device_name),
    INDEX idx_device_type (device_type),
    INDEX idx_area_id (area_id),
    INDEX idx_ip_address (ip_address),
    INDEX idx_status (status),
    INDEX idx_last_active_time (last_active_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备表';

-- 字典表
CREATE TABLE IF NOT EXISTS t_common_dict_type (
    dict_type_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '字典类型ID',
    dict_type_name VARCHAR(100) NOT NULL COMMENT '字典类型名称',
    dict_type_code VARCHAR(50) NOT NULL COMMENT '字典类型编码',
    description TEXT COMMENT '描述',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常 2-禁用',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',

    UNIQUE INDEX uk_dict_type_code (dict_type_code, deleted_flag),
    INDEX idx_dict_type_name (dict_type_name),
    INDEX idx_status (status),
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典类型表';

-- 字典数据表
CREATE TABLE IF NOT EXISTS t_common_dict_data (
    dict_data_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '字典数据ID',
    dict_type_code VARCHAR(50) NOT NULL COMMENT '字典类型编码',
    dict_label VARCHAR(100) NOT NULL COMMENT '字典标签',
    dict_value VARCHAR(100) NOT NULL COMMENT '字典值',
    dict_sort INT DEFAULT 0 COMMENT '排序',
    css_class VARCHAR(100) COMMENT '样式类',
    list_class VARCHAR(100) COMMENT '列表样式',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认：0-否 1-是',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常 2-禁用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',

    UNIQUE INDEX uk_dict_type_value (dict_type_code, dict_value, deleted_flag),
    INDEX idx_dict_type_code (dict_type_code),
    INDEX idx_dict_label (dict_label),
    INDEX idx_dict_sort (dict_sort),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典数据表';

-- =====================================================
-- 3. 消费模块基础表
-- =====================================================

-- 消费账户表（基础版本，V2.0.1会增强）
CREATE TABLE IF NOT EXISTS t_consume_account (
    account_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '账户ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    account_no VARCHAR(50) NOT NULL COMMENT '账户编号',
    account_name VARCHAR(100) NOT NULL COMMENT '账户名称',
    balance DECIMAL(15,2) DEFAULT 0.00 COMMENT '账户余额（元）',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常 2-冻结 3-注销',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',

    UNIQUE INDEX uk_account_no (account_no, deleted_flag),
    INDEX idx_user_id (user_id),
    INDEX idx_balance (balance),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消费账户表';

-- 消费记录表（基础版本，V2.0.0会增强）
CREATE TABLE IF NOT EXISTS t_consume_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    transaction_no VARCHAR(50) NOT NULL COMMENT '交易流水号',
    order_no VARCHAR(50) COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    account_id BIGINT NOT NULL COMMENT '账户ID',
    amount DECIMAL(15,2) NOT NULL COMMENT '消费金额',
    consume_date DATE NOT NULL COMMENT '消费日期',
    consume_time DATETIME NOT NULL COMMENT '消费时间',
    merchant_name VARCHAR(200) COMMENT '商户名称',
    status VARCHAR(20) DEFAULT 'SUCCESS' COMMENT '状态：SUCCESS-成功 FAILED-失败',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',

    UNIQUE INDEX uk_transaction_no (transaction_no, deleted_flag),
    INDEX idx_user_id (user_id),
    INDEX idx_account_id (account_id),
    INDEX idx_consume_date (consume_date),
    INDEX idx_consume_time (consume_time),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消费记录表';

-- =====================================================
-- 4. 门禁模块基础表
-- =====================================================

-- 门禁权限表
CREATE TABLE IF NOT EXISTS t_access_permission (
    permission_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '权限ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    area_id BIGINT NOT NULL COMMENT '区域ID',
    device_id BIGINT COMMENT '设备ID',
    permission_type VARCHAR(20) DEFAULT 'ALWAYS' COMMENT '权限类型：ALWAYS-永久 TIME_LIMITED-限时',
    start_time DATETIME COMMENT '生效开始时间',
    end_time DATETIME COMMENT '生效结束时间',
    access_times VARCHAR(500) COMMENT '通行时间段（JSON）',
    status TINYINT DEFAULT 1 COMMENT '状态：1-有效 2-失效',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',

    INDEX idx_user_id (user_id),
    INDEX idx_area_id (area_id),
    INDEX idx_device_id (device_id),
    INDEX idx_permission_type (permission_type),
    INDEX idx_status (status),
    INDEX idx_start_end_time (start_time, end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门禁权限表';

-- 门禁记录表
CREATE TABLE IF NOT EXISTS t_access_record (
    record_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    device_id BIGINT NOT NULL COMMENT '设备ID',
    area_id BIGINT COMMENT '区域ID',
    access_result TINYINT NOT NULL COMMENT '通行结果：1-成功 2-失败',
    access_time DATETIME NOT NULL COMMENT '通行时间',
    access_type VARCHAR(20) DEFAULT 'IN' COMMENT '通行类型：IN-进入 OUT-离开',
    verify_method VARCHAR(50) COMMENT '验证方式：FACE-人脸 CARD-刷卡 FINGERPRINT-指纹',
    photo_path VARCHAR(500) COMMENT '照片路径',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    INDEX idx_user_id (user_id),
    INDEX idx_device_id (device_id),
    INDEX idx_area_id (area_id),
    INDEX idx_access_result (access_result),
    INDEX idx_access_time (access_time),
    INDEX idx_access_type (access_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门禁记录表';

-- =====================================================
-- 5. 考勤模块基础表
-- =====================================================

-- 考勤记录表
CREATE TABLE IF NOT EXISTS t_attendance_record (
    record_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    device_id BIGINT NOT NULL COMMENT '设备ID',
    attendance_date DATE NOT NULL COMMENT '考勤日期',
    clock_time DATETIME NOT NULL COMMENT '打卡时间',
    clock_type VARCHAR(20) NOT NULL COMMENT '打卡类型：ON_DUTY-上班 OFF_DUTY-下班',
    verify_method VARCHAR(50) COMMENT '验证方式',
    location VARCHAR(200) COMMENT '打卡位置',
    photo_path VARCHAR(500) COMMENT '照片路径',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    INDEX idx_user_id (user_id),
    INDEX idx_device_id (device_id),
    INDEX idx_attendance_date (attendance_date),
    INDEX idx_clock_time (clock_time),
    INDEX idx_clock_type (clock_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤记录表';

-- 考勤班次表
CREATE TABLE IF NOT EXISTS t_attendance_shift (
    shift_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '班次ID',
    shift_name VARCHAR(100) NOT NULL COMMENT '班次名称',
    work_start_time TIME NOT NULL COMMENT '上班时间',
    work_end_time TIME NOT NULL COMMENT '下班时间',
    break_start_time TIME COMMENT '休息开始时间',
    break_end_time TIME COMMENT '休息结束时间',
    work_days VARCHAR(20) DEFAULT '1,2,3,4,5' COMMENT '工作日（1-7表示周一到周日）',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常 2-禁用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',

    INDEX idx_shift_name (shift_name),
    INDEX idx_work_times (work_start_time, work_end_time),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤班次表';

-- =====================================================
-- 6. 访客模块基础表
-- =====================================================

-- 访客预约表
CREATE TABLE IF NOT EXISTS t_visitor_appointment (
    appointment_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '预约ID',
    visitor_name VARCHAR(100) NOT NULL COMMENT '访客姓名',
    visitor_phone VARCHAR(20) NOT NULL COMMENT '访客手机号',
    visitor_id_card VARCHAR(50) COMMENT '访客身份证号',
    company VARCHAR(200) COMMENT '访客单位',
    visit_reason VARCHAR(500) COMMENT '来访事由',
    visit_date DATE NOT NULL COMMENT '来访日期',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    interviewee_id BIGINT NOT NULL COMMENT '被访人ID',
    interviewee_name VARCHAR(100) NOT NULL COMMENT '被访人姓名',
    area_id BIGINT COMMENT '访问区域ID',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING-待审批 APPROVED-已审批 REJECTED-已拒绝 COMPLETED-已完成',
    approve_time DATETIME COMMENT '审批时间',
    approver_id BIGINT COMMENT '审批人ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',

    INDEX idx_visitor_phone (visitor_phone),
    INDEX idx_visitor_id_card (visitor_id_card),
    INDEX idx_interviewee_id (interviewee_id),
    INDEX idx_visit_date (visit_date),
    INDEX idx_status (status),
    INDEX idx_approver_id (approver_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='访客预约表';

-- 访客记录表
CREATE TABLE IF NOT EXISTS t_visitor_record (
    record_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    appointment_id BIGINT COMMENT '预约ID',
    visitor_name VARCHAR(100) NOT NULL COMMENT '访客姓名',
    visitor_phone VARCHAR(20) NOT NULL COMMENT '访客手机号',
    interviewee_id BIGINT NOT NULL COMMENT '被访人ID',
    interviewee_name VARCHAR(100) NOT NULL COMMENT '被访人姓名',
    area_id BIGINT COMMENT '访问区域ID',
    actual_arrive_time DATETIME COMMENT '实际到达时间',
    actual_leave_time DATETIME COMMENT '实际离开时间',
    access_card_no VARCHAR(50) COMMENT '访问卡号',
    access_result TINYINT DEFAULT 1 COMMENT '访问结果：1-成功 2-失败',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    INDEX idx_appointment_id (appointment_id),
    INDEX idx_visitor_phone (visitor_phone),
    INDEX idx_interviewee_id (interviewee_id),
    INDEX idx_actual_arrive_time (actual_arrive_time),
    INDEX idx_access_result (access_result)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='访客记录表';

-- =====================================================
-- 7. 视频监控模块基础表
-- =====================================================

-- 视频设备表
CREATE TABLE IF NOT EXISTS t_video_device (
    device_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '设备ID',
    device_no VARCHAR(100) NOT NULL COMMENT '设备编号',
    device_name VARCHAR(200) COMMENT '设备名称',
    device_type VARCHAR(50) NOT NULL COMMENT '设备类型：CAMERA-摄像头 NVR-录像机 DVR-硬盘录像机',
    area_id BIGINT COMMENT '所在区域ID',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    port INT COMMENT '端口',
    username VARCHAR(100) COMMENT '用户名',
    password VARCHAR(255) COMMENT '密码（加密）',
    rtsp_url VARCHAR(500) COMMENT 'RTSP地址',
    hls_url VARCHAR(500) COMMENT 'HLS地址',
    status TINYINT DEFAULT 1 COMMENT '状态：1-在线 2-离线 3-故障',
    resolution VARCHAR(20) COMMENT '分辨率',
    frame_rate INT COMMENT '帧率',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',

    UNIQUE INDEX uk_device_no (device_no, deleted_flag),
    INDEX idx_device_name (device_name),
    INDEX idx_device_type (device_type),
    INDEX idx_area_id (area_id),
    INDEX idx_ip_address (ip_address),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频设备表';

-- 视频录像表
CREATE TABLE IF NOT EXISTS t_video_record (
    record_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '录像ID',
    device_id BIGINT NOT NULL COMMENT '设备ID',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    record_type VARCHAR(20) DEFAULT 'CONTINUOUS' COMMENT '录像类型：CONTINUOUS-连续 MOTION-动检 MANUAL-手动',
    file_path VARCHAR(500) COMMENT '文件路径',
    file_size BIGINT COMMENT '文件大小（字节）',
    duration INT COMMENT '录像时长（秒）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    INDEX idx_device_id (device_id),
    INDEX idx_start_end_time (start_time, end_time),
    INDEX idx_record_type (record_type),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频录像表';

-- =====================================================
-- 8. 系统基础表
-- =====================================================

-- 操作日志表
CREATE TABLE IF NOT EXISTS t_audit_log (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    user_id BIGINT COMMENT '用户ID',
    username VARCHAR(100) COMMENT '用户名',
    operation VARCHAR(100) NOT NULL COMMENT '操作类型',
    method VARCHAR(200) COMMENT '请求方法',
    params TEXT COMMENT '请求参数',
    time BIGINT COMMENT '执行时长（毫秒）',
    ip VARCHAR(50) COMMENT 'IP地址',
    user_agent VARCHAR(500) COMMENT '用户代理',
    result TINYINT COMMENT '操作结果：1-成功 0-失败',
    error_msg TEXT COMMENT '错误信息',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    INDEX idx_user_id (user_id),
    INDEX idx_username (username),
    INDEX idx_operation (operation),
    INDEX idx_create_time (create_time),
    INDEX idx_ip (ip)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- 系统配置表
CREATE TABLE IF NOT EXISTS t_system_config (
    config_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '配置ID',
    config_key VARCHAR(100) NOT NULL COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    config_name VARCHAR(200) COMMENT '配置名称',
    description TEXT COMMENT '描述',
    config_type VARCHAR(20) DEFAULT 'SYSTEM' COMMENT '配置类型：SYSTEM-系统 BUSINESS-业务',
    is_encrypted TINYINT DEFAULT 0 COMMENT '是否加密：0-否 1-是',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',

    UNIQUE INDEX uk_config_key (config_key, deleted_flag),
    INDEX idx_config_name (config_name),
    INDEX idx_config_type (config_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- =====================================================
-- 9. 恢复环境设置
-- =====================================================

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 10. 记录迁移历史
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
    'V1.0.0',
    '数据库初始架构 - 创建所有基础表结构',
    'V1_0_0__INITIAL_SCHEMA.sql',
    'SUCCESS',
    NOW(),
    NOW(),
    NOW()
);

COMMIT;

-- =====================================================
-- 11. 输出执行完成信息
-- =====================================================

SELECT 'V1.0.0 数据库初始架构脚本执行完成！' AS migration_status,
       '创建基础表 23个，创建索引 80+' AS migration_summary,
       NOW() AS completed_time;

-- =====================================================
-- 脚本结束
-- =====================================================
