-- =====================================================
-- IOE-DREAM 公共数据库表结构脚本
-- 版本: 1.0.0
-- 说明: 创建公共数据库的所有表结构
-- =====================================================

USE ioedream_common_db;

-- 1. 用户表
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

-- 2. 角色表
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

-- 3. 权限表
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

-- 4. 用户角色关联表
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
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 5. 角色权限关联表
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
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- 6. 部门表
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
    INDEX idx_department_status (status),
    INDEX idx_level (level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门表';

-- 7. 区域表 (统一空间概念)
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
    INDEX idx_area_level (level),
    INDEX idx_manager_id (manager_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='区域表';

-- 8. 字典类型表
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
    INDEX idx_dict_type_status (status),
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典类型表';

-- 9. 字典数据表
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
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典数据表';

-- 10. 菜单表
DROP TABLE IF EXISTS t_menu;
CREATE TABLE t_menu (
    menu_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '菜单ID',
    menu_code VARCHAR(50) NOT NULL UNIQUE COMMENT '菜单编码',
    menu_name VARCHAR(100) NOT NULL COMMENT '菜单名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID',
    menu_type TINYINT DEFAULT 1 COMMENT '菜单类型 1-目录 2-菜单 3-按钮',
    menu_url VARCHAR(200) COMMENT '菜单URL',
    menu_icon VARCHAR(100) COMMENT '菜单图标',
    permission_code VARCHAR(100) COMMENT '权限编码',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',
    INDEX idx_menu_code (menu_code),
    INDEX idx_menu_parent (parent_id),
    INDEX idx_menu_type (menu_type),
    INDEX idx_permission_code (permission_code),
    INDEX idx_menu_status (status),
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜单表';

SELECT '公共数据库表结构创建完成' as message, COUNT(*) as table_count
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'ioedream_common_db' AND TABLE_TYPE = 'BASE TABLE';