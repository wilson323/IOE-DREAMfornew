-- =====================================================
-- IOE-DREAM 智慧园区一卡通管理平台 - 数据库初始化脚本
-- 版本: 1.0.0
-- 创建时间: 2025-12-08
-- 说明: 统一初始化所有数据库和基础数据
-- =====================================================

-- 1. 创建所有业务数据库
CREATE DATABASE IF NOT EXISTS ioedream_common_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '公共数据库-用户权限区域管理';
CREATE DATABASE IF NOT EXISTS ioedream_access_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '门禁管理数据库';
CREATE DATABASE IF NOT EXISTS ioedream_attendance_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '考勤管理数据库';
CREATE DATABASE IF NOT EXISTS ioedream_consume_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '消费管理数据库';
CREATE DATABASE IF NOT EXISTS ioedream_visitor_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '访客管理数据库';
CREATE DATABASE IF NOT EXISTS ioedream_video_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '视频监控数据库';
CREATE DATABASE IF NOT EXISTS ioedream_device_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '设备管理数据库';
CREATE DATABASE IF NOT EXISTS ioedream_database CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '数据库管理服务数据库';

-- =====================================================
-- 2. 公共数据库表结构 (ioedream_common_db)
-- =====================================================

USE ioedream_common_db;

-- 2.1 用户表
DROP TABLE IF EXISTS t_user;
CREATE TABLE t_user (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    user_no VARCHAR(50) NOT NULL UNIQUE COMMENT '用户编号',
    username VARCHAR(100) NOT NULL COMMENT '用户名',
    phone VARCHAR(20) UNIQUE COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    real_name VARCHAR(100) COMMENT '真实姓名',
    gender TINYINT DEFAULT 1 COMMENT '性别 1-男 2-女',
    avatar VARCHAR(500) COMMENT '头像',
    password VARCHAR(255) COMMENT '密码',
    salt VARCHAR(50) COMMENT '密码盐值',
    status TINYINT DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
    department_id BIGINT COMMENT '部门ID',
    position_id BIGINT COMMENT '职位ID',
    login_time DATETIME COMMENT '最后登录时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',
    INDEX idx_user_username (username),
    INDEX idx_user_phone (phone),
    INDEX idx_user_department (department_id),
    INDEX idx_user_status (status),
    INDEX idx_user_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 2.2 角色表
DROP TABLE IF EXISTS t_role;
CREATE TABLE t_role (
    role_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    role_name VARCHAR(100) NOT NULL COMMENT '角色名称',
    description VARCHAR(500) COMMENT '角色描述',
    status TINYINT DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',
    INDEX idx_role_code (role_code),
    INDEX idx_role_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 2.3 权限表
DROP TABLE IF EXISTS t_permission;
CREATE TABLE t_permission (
    permission_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID',
    permission_code VARCHAR(100) NOT NULL UNIQUE COMMENT '权限编码',
    permission_name VARCHAR(100) NOT NULL COMMENT '权限名称',
    description VARCHAR(500) COMMENT '权限描述',
    menu_url VARCHAR(200) COMMENT '菜单URL',
    menu_icon VARCHAR(100) COMMENT '菜单图标',
    parent_id BIGINT DEFAULT 0 COMMENT '父权限ID',
    permission_type TINYINT DEFAULT 1 COMMENT '权限类型 1-菜单 2-按钮 3-接口',
    status TINYINT DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',
    INDEX idx_permission_code (permission_code),
    INDEX idx_permission_parent (parent_id),
    INDEX idx_permission_type (permission_type),
    INDEX idx_permission_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- 2.4 用户角色关联表
DROP TABLE IF EXISTS t_user_role;
CREATE TABLE t_user_role (
    user_role_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户角色ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_user_id BIGINT COMMENT '创建人ID',
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id),
    FOREIGN KEY (user_id) REFERENCES t_user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES t_role(role_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 2.5 角色权限关联表
DROP TABLE IF EXISTS t_role_permission;
CREATE TABLE t_role_permission (
    role_permission_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色权限ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_user_id BIGINT COMMENT '创建人ID',
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    INDEX idx_role_id (role_id),
    INDEX idx_permission_id (permission_id),
    FOREIGN KEY (role_id) REFERENCES t_role(role_id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES t_permission(permission_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- 2.6 部门表
DROP TABLE IF EXISTS t_department;
CREATE TABLE t_department (
    department_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '部门ID',
    department_code VARCHAR(50) NOT NULL UNIQUE COMMENT '部门编码',
    department_name VARCHAR(100) NOT NULL COMMENT '部门名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父部门ID',
    level INT DEFAULT 1 COMMENT '层级',
    sort_order INT DEFAULT 0 COMMENT '排序',
    leader_id BIGINT COMMENT '负责人ID',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    description VARCHAR(500) COMMENT '部门描述',
    status TINYINT DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',
    INDEX idx_department_code (department_code),
    INDEX idx_department_parent (parent_id),
    INDEX idx_department_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门表';

-- 2.7 区域表 (统一空间概念)
DROP TABLE IF EXISTS t_area;
CREATE TABLE t_area (
    area_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '区域ID',
    area_code VARCHAR(50) NOT NULL UNIQUE COMMENT '区域编码',
    area_name VARCHAR(100) NOT NULL COMMENT '区域名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父区域ID',
    level INT DEFAULT 1 COMMENT '层级',
    path VARCHAR(500) COMMENT '路径',
    area_type TINYINT DEFAULT 1 COMMENT '区域类型 1-园区 2-建筑 3-楼层 4-房间 5-区域',
    status TINYINT DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
    sort_order INT DEFAULT 0 COMMENT '排序',
    description VARCHAR(500) COMMENT '区域描述',
    coordinates JSON COMMENT '地理坐标',
    manager_id BIGINT COMMENT '管理员ID',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    capacity INT COMMENT '容量',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',
    INDEX idx_area_code (area_code),
    INDEX idx_area_parent (parent_id),
    INDEX idx_area_type (area_type),
    INDEX idx_area_status (status),
    INDEX idx_area_level (level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='区域表';

-- 2.8 字典类型表
DROP TABLE IF EXISTS t_dict_type;
CREATE TABLE t_dict_type (
    dict_type_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '字典类型ID',
    dict_type_code VARCHAR(50) NOT NULL UNIQUE COMMENT '字典类型编码',
    dict_type_name VARCHAR(100) NOT NULL COMMENT '字典类型名称',
    description VARCHAR(500) COMMENT '描述',
    status TINYINT DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',
    INDEX idx_dict_type_code (dict_type_code),
    INDEX idx_dict_type_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典类型表';

-- 2.9 字典数据表
DROP TABLE IF EXISTS t_dict_data;
CREATE TABLE t_dict_data (
    dict_data_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '字典数据ID',
    dict_type_code VARCHAR(50) NOT NULL COMMENT '字典类型编码',
    dict_code VARCHAR(50) NOT NULL COMMENT '字典编码',
    dict_value VARCHAR(100) NOT NULL COMMENT '字典值',
    description VARCHAR(500) COMMENT '描述',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',
    UNIQUE KEY uk_dict_data (dict_type_code, dict_code),
    INDEX idx_dict_type_code (dict_type_code),
    INDEX idx_dict_code (dict_code),
    INDEX idx_dict_status (status),
    FOREIGN KEY (dict_type_code) REFERENCES t_dict_type(dict_type_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典数据表';

-- =====================================================
-- 3. 数据库管理服务表结构 (ioedream_database)
-- =====================================================

USE ioedream_database;

-- 3.1 数据库版本管理表
DROP TABLE IF EXISTS database_version;
CREATE TABLE database_version (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    db_name VARCHAR(100) NOT NULL COMMENT '数据库名称',
    version VARCHAR(50) NOT NULL DEFAULT '1.0.0' COMMENT '版本号',
    status VARCHAR(20) NOT NULL DEFAULT 'INIT' COMMENT '状态 INIT-初始化 SYNCING-同步中 SUCCESS-同步成功 FAILED-同步失败',
    description TEXT COMMENT '版本描述',
    last_sync_time DATETIME COMMENT '最后同步时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_db_name (db_name),
    INDEX idx_db_status (status),
    INDEX idx_last_sync_time (last_sync_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据库版本管理表';

-- 3.2 同步任务记录表
DROP TABLE IF EXISTS sync_task_record;
CREATE TABLE sync_task_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    task_id VARCHAR(50) NOT NULL UNIQUE COMMENT '任务ID',
    db_name VARCHAR(100) NOT NULL COMMENT '数据库名称',
    task_type VARCHAR(20) NOT NULL COMMENT '任务类型 INIT-初始化 SYNC-同步 MIGRATE-迁移',
    status VARCHAR(20) NOT NULL DEFAULT 'RUNNING' COMMENT '任务状态 RUNNING-运行中 SUCCESS-成功 FAILED-失败',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    duration BIGINT COMMENT '执行时长(毫秒)',
    result TEXT COMMENT '执行结果',
    error_message TEXT COMMENT '错误信息',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_task_db_name (db_name),
    INDEX idx_task_status (status),
    INDEX idx_task_start_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='同步任务记录表';

-- =====================================================
-- 4. 插入基础数据
-- =====================================================

USE ioedream_common_db;

-- 4.1 默认用户数据
INSERT INTO t_user (user_no, username, real_name, phone, email, password, salt, status, create_user_id) VALUES
('admin', 'admin', '系统管理员', '13800138000', 'admin@ioedream.com', '$2a$10$EkI2jYa3dZ5KdJ/ZB5N3PehkwZsH5I4lNuO8KjRMKqMqMj8H.F9Xe', 'admin123', 1, 1),
('test001', 'test001', '测试用户', '13900139000', 'test@ioedream.com', '$2a$10$EkI2jYa3dZ5KdJ/ZB5N3PehkwZsH5I4lNuO8KjRMKqMqMj8H.F9Xe', 'test123', 1, 1);

-- 4.2 默认角色数据
INSERT INTO t_role (role_code, role_name, description, sort_order, create_user_id) VALUES
('SUPER_ADMIN', '超级管理员', '系统超级管理员，拥有所有权限', 1, 1),
('ADMIN', '管理员', '系统管理员，拥有管理权限', 2, 1),
('USER', '普通用户', '普通用户，基础使用权限', 3, 1);

-- 4.3 默认权限数据
INSERT INTO t_permission (permission_code, permission_name, description, menu_url, menu_icon, parent_id, permission_type, sort_order) VALUES
-- 系统管理
('system', '系统管理', '系统管理模块', '', 'system', 0, 1, 1),
('system:user', '用户管理', '用户管理页面', '/system/user', 'user', 1, 1, 1),
('system:role', '角色管理', '角色管理页面', '/system/role', 'role', 1, 1, 2),
('system:permission', '权限管理', '权限管理页面', '/system/permission', 'permission', 1, 1, 3),
('system:department', '部门管理', '部门管理页面', '/system/department', 'department', 1, 1, 4),
('system:area', '区域管理', '区域管理页面', '/system/area', 'area', 1, 1, 5),
('system:dict', '字典管理', '字典管理页面', '/system/dict', 'dict', 1, 1, 6),

-- 业务模块
('access', '门禁管理', '门禁管理模块', '', 'access', 0, 1, 2),
('access:record', '通行记录', '门禁通行记录查询', '/access/record', 'record', 8, 1, 1),
('access:device', '门禁设备', '门禁设备管理', '/access/device', 'device', 8, 1, 2),

('attendance', '考勤管理', '考勤管理模块', '', 'attendance', 0, 1, 3),
('attendance:record', '考勤记录', '考勤记录查询', '/attendance/record', 'clock', 9, 1, 1),
('attendance:schedule', '排班管理', '排班管理页面', '/attendance/schedule', 'schedule', 9, 1, 2),

('consume', '消费管理', '消费管理模块', '', 'consume', 0, 1, 4),
('consume:record', '消费记录', '消费记录查询', '/consume/record', 'payment', 10, 1, 1),
('consume:account', '账户管理', '账户管理页面', '/consume/account', 'account', 10, 1, 2),

('visitor', '访客管理', '访客管理模块', '', 'visitor', 0, 1, 5),
('visitor:record', '访客记录', '访客记录查询', '/visitor/record', 'visitor', 11, 1, 1),
('visitor:appointment', '访客预约', '访客预约管理', '/visitor/appointment', 'appointment', 11, 1, 2),

('video', '视频监控', '视频监控模块', '', 'video', 0, 1, 6),
('video:live', '实时监控', '实时监控页面', '/video/live', 'video-camera', 12, 1, 1),
('video:record', '录像回放', '录像回放页面', '/video/record', 'video', 12, 1, 2);

-- 4.4 用户角色关联
INSERT INTO t_user_role (user_id, role_id, create_user_id) VALUES
(1, 1, 1), -- admin - 超级管理员
(2, 3, 1); -- test001 - 普通用户

-- 4.5 角色权限关联 (超级管理员拥有所有权限)
INSERT INTO t_role_permission (role_id, permission_id, create_user_id)
SELECT 1, permission_id, 1 FROM t_permission;

-- 普通用户基础权限
INSERT INTO t_role_permission (role_id, permission_id, create_user_id) VALUES
(3, 10, 1), -- 考勤记录查询
(3, 14, 1), -- 消费记录查询
(3, 18, 1), -- 访客记录查询
(3, 22, 1); -- 实时监控查看

-- 4.6 默认部门数据
INSERT INTO t_department (department_code, department_name, parent_id, level, sort_order, description, create_user_id) VALUES
('ROOT', 'IOE-DREAM集团', 0, 1, 1, 'IOE-DREAM智慧园区管理集团', 1),
('IT', '信息技术部', 1, 2, 1, '负责系统开发和维护', 1),
('HR', '人力资源部', 1, 2, 2, '负责人力资源管理', 1),
('ADMIN', '行政管理部', 1, 2, 3, '负责园区行政管理', 1);

-- 4.7 默认区域数据
INSERT INTO t_area (area_code, area_name, parent_id, level, path, area_type, sort_order, description, manager_id, create_user_id) VALUES
('ROOT', 'IOE-DREAM智慧园区', 0, 1, '/ROOT', 1, 1, 'IOE-DREAM智慧园区整体区域', 1, 1),
('BUILD_A', 'A栋办公楼', 1, 2, '/ROOT/BUILD_A', 2, 1, 'A栋办公楼，包含办公区域和会议室', 1, 1),
('BUILD_B', 'B栋研发中心', 1, 2, '/ROOT/BUILD_B', 2, 2, 'B栋研发中心，技术研发部门所在地', 1, 1),
('AREA_CANTEEN', '员工餐厅', 1, 2, '/ROOT/AREA_CANTEEN', 5, 3, '员工餐厅，提供餐饮服务', 1, 1),
('AREA_PARKING', '停车场', 1, 2, '/ROOT/AREA_PARKING', 5, 4, '园区停车场，员工和访客停车区域', 1, 1);

-- 4.8 默认字典类型
INSERT INTO t_dict_type (dict_type_code, dict_type_name, description, sort_order, create_user_id) VALUES
('GENDER', '性别', '性别字典类型', 1, 1),
('USER_STATUS', '用户状态', '用户状态字典类型', 2, 1),
('AREA_TYPE', '区域类型', '区域类型字典类型', 3, 1),
('DEVICE_TYPE', '设备类型', '设备类型字典类型', 4, 1),
('CONSUME_TYPE', '消费类型', '消费类型字典类型', 5, 1);

-- 4.9 默认字典数据
INSERT INTO t_dict_data (dict_type_code, dict_code, dict_value, description, sort_order, create_user_id) VALUES
-- 性别
('GENDER', '1', '男', '男性', 1, 1),
('GENDER', '2', '女', '女性', 2, 1),

-- 用户状态
('USER_STATUS', '1', '启用', '用户启用状态', 1, 1),
('USER_STATUS', '0', '禁用', '用户禁用状态', 2, 1),

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

-- 消费类型
('CONSUME_TYPE', 'MEAL', '餐饮消费', '餐厅餐饮消费', 1, 1),
('CONSUME_TYPE', 'SHOP', '超市消费', '超市购物消费', 2, 1),
('CONSUME_TYPE', 'OTHER', '其他消费', '其他类型消费', 3, 1);

-- =====================================================
-- 5. 数据库版本记录初始化
-- =====================================================

USE ioedream_database;

INSERT INTO database_version (db_name, version, status, description) VALUES
('ioedream_common_db', '1.0.0', 'SUCCESS', '公共数据库初始化完成'),
('ioedream_access_db', '1.0.0', 'INIT', '门禁数据库待初始化'),
('ioedream_attendance_db', '1.0.0', 'INIT', '考勤数据库待初始化'),
('ioedream_consume_db', '1.0.0', 'INIT', '消费数据库待初始化'),
('ioedream_visitor_db', '1.0.0', 'INIT', '访客数据库待初始化'),
('ioedream_video_db', '1.0.0', 'INIT', '视频数据库待初始化'),
('ioedream_device_db', '1.0.0', 'INIT', '设备数据库待初始化');

-- =====================================================
-- 6. 创建 Flyway 表结构（用于版本管理）
-- =====================================================

-- 为每个业务数据库创建 Flyway 表
USE ioedream_common_db;

CREATE TABLE IF NOT EXISTS flyway_schema_history (
    installed_rank INT NOT NULL,
    version VARCHAR(50),
    description VARCHAR(200) NOT NULL,
    type VARCHAR(20) NOT NULL,
    script VARCHAR(1000) NOT NULL,
    checksum INT,
    installed_by VARCHAR(100) NOT NULL,
    installed_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    execution_time INT NOT NULL,
    success TINYINYINT NOT NULL,
    PRIMARY KEY (installed_rank),
    INDEX flyway_schema_history_s_idx (success)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 为其他数据库创建 Flyway 表
USE ioedream_access_db;
CREATE TABLE IF NOT EXISTS flyway_schema_history (
    installed_rank INT NOT NULL,
    version VARCHAR(50),
    description VARCHAR(200) NOT NULL,
    type VARCHAR(20) NOT NULL,
    script VARCHAR(1000) NOT NULL,
    checksum INT,
    installed_by VARCHAR(100) NOT NULL,
    installed_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    execution_time INT NOT NULL,
    success TINYINT NOT NULL,
    PRIMARY KEY (installed_rank),
    INDEX flyway_schema_history_s_idx (success)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

USE ioedream_attendance_db;
CREATE TABLE IF NOT EXISTS flyway_schema_history (
    installed_rank INT NOT NULL,
    version VARCHAR(50),
    description VARCHAR(200) NOT NULL,
    type VARCHAR(20) NOT NULL,
    script VARCHAR(1000) NOT NULL,
    checksum INT,
    installed_by VARCHAR(100) NOT NULL,
    installed_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    execution_time INT NOT NULL,
    success TINYINT NOT NULL,
    PRIMARY KEY (installed_rank),
    INDEX flyway_schema_history_s_idx (success)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

USE ioedream_consume_db;
CREATE TABLE IF NOT EXISTS flyway_schema_history (
    installed_rank INT NOT NULL,
    version VARCHAR(50),
    description VARCHAR(200) NOT NULL,
    type VARCHAR(20) NOT NULL,
    script VARCHAR(1000) NOT NULL,
    checksum INT,
    installed_by VARCHAR(100) NOT NULL,
    installed_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    execution_time INT NOT NULL,
    success TINYINT NOT NULL,
    PRIMARY KEY (installed_rank),
    INDEX flyway_schema_history_s_idx (success)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

USE ioedream_visitor_db;
CREATE TABLE IF NOT EXISTS flyway_schema_history (
    installed_rank INT NOT NULL,
    version VARCHAR(50),
    description VARCHAR(200) NOT NULL,
    type VARCHAR(20) NOT NULL,
    script VARCHAR(1000) NOT NULL,
    checksum INT,
    installed_by VARCHAR(100) NOT NULL,
    installed_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    execution_time INT NOT NULL,
    success TINYINT NOT NULL,
    PRIMARY KEY (installed_rank),
    INDEX flyway_schema_history_s_idx (success)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

USE ioedream_video_db;
CREATE TABLE IF NOT EXISTS flyway_schema_history (
    installed_rank INT NOT NULL,
    version VARCHAR(50),
    description VARCHAR(200) NOT NULL,
    type VARCHAR(20) NOT NULL,
    script VARCHAR(1000) NOT NULL,
    checksum INT,
    installed_by VARCHAR(100) NOT NULL,
    installed_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    execution_time INT NOT NULL,
    success TINYINT NOT NULL,
    PRIMARY KEY (installed_rank),
    INDEX flyway_schema_history_s_idx (success)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

USE ioedream_device_db;
CREATE TABLE IF NOT EXISTS flyway_schema_history (
    installed_rank INT NOT NULL,
    version VARCHAR(50),
    description VARCHAR(200) NOT NULL,
    type VARCHAR(20) NOT NULL,
    script VARCHAR(1000) NOT NULL,
    checksum INT,
    installed_by VARCHAR(100) NOT NULL,
    installed_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    execution_time INT NOT NULL,
    success TINYINT NOT NULL,
    PRIMARY KEY (installed_rank),
    INDEX flyway_schema_history_s_idx (success)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 初始化完成
-- =====================================================

SELECT 'IOE-DREAM 数据库初始化完成!' as message,
       NOW() as init_time,
       '所有基础数据库和表结构已创建，基础数据已插入' as description;