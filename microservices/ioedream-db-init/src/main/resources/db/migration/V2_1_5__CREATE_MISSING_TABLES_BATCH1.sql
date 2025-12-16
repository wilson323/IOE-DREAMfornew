-- =====================================================
-- IOE-DREAM Flyway 迁移脚本
-- 版本: V2.1.5
-- 描述: 批量补齐缺口表（第1批：RBAC核心字段对齐 + 字典/菜单/员工/会话/主题）
-- =====================================================

-- 1. t_user 字段对齐
ALTER TABLE t_user
    ADD COLUMN IF NOT EXISTS login_fail_count INT DEFAULT 0 COMMENT '登录失败次数' AFTER last_login_ip,
    ADD COLUMN IF NOT EXISTS account_locked TINYINT DEFAULT 0 COMMENT '账户锁定' AFTER login_fail_count,
    ADD COLUMN IF NOT EXISTS lock_time DATETIME COMMENT '锁定时间' AFTER account_locked,
    ADD COLUMN IF NOT EXISTS unlock_time DATETIME COMMENT '解锁时间' AFTER lock_time;

-- 2. t_role 字段对齐
ALTER TABLE t_role
    ADD COLUMN IF NOT EXISTS role_desc VARCHAR(500) COMMENT '角色描述' AFTER role_name,
    ADD COLUMN IF NOT EXISTS data_scope INT DEFAULT 1 COMMENT '数据范围' AFTER role_desc,
    ADD COLUMN IF NOT EXISTS is_system TINYINT DEFAULT 0 COMMENT '是否系统角色' AFTER data_scope;

-- 3. t_user_role 字段对齐
ALTER TABLE t_user_role
    ADD COLUMN IF NOT EXISTS user_role_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户角色关联ID' FIRST,
    ADD COLUMN IF NOT EXISTS status TINYINT DEFAULT 1 COMMENT '状态' AFTER role_id,
    ADD COLUMN IF NOT EXISTS update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER create_time,
    ADD COLUMN IF NOT EXISTS deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记' AFTER update_time,
    ADD COLUMN IF NOT EXISTS version INT DEFAULT 0 COMMENT '乐观锁版本号' AFTER deleted_flag;

-- 4. t_role_resource 字段对齐
ALTER TABLE t_role_resource
    ADD COLUMN IF NOT EXISTS role_resource_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '角色资源关联ID' FIRST,
    ADD COLUMN IF NOT EXISTS resource_id BIGINT COMMENT '资源ID' AFTER role_id,
    ADD COLUMN IF NOT EXISTS status TINYINT DEFAULT 1 COMMENT '状态' AFTER resource_id,
    ADD COLUMN IF NOT EXISTS update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER create_time,
    ADD COLUMN IF NOT EXISTS deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记' AFTER update_time,
    ADD COLUMN IF NOT EXISTS version INT DEFAULT 0 COMMENT '乐观锁版本号' AFTER deleted_flag;

UPDATE t_role_resource SET resource_id = permission_id WHERE resource_id IS NULL AND permission_id IS NOT NULL;

-- 5-15. 创建缺失表
CREATE TABLE IF NOT EXISTS t_dict_type (
    dict_type_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dict_type_code VARCHAR(100) NOT NULL,
    dict_type_name VARCHAR(200) NOT NULL,
    status TINYINT DEFAULT 1,
    remark VARCHAR(500),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_user_id BIGINT,
    update_user_id BIGINT,
    deleted_flag TINYINT DEFAULT 0,
    version INT DEFAULT 0,
    UNIQUE KEY uk_dict_type_code (dict_type_code, deleted_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS t_dict_data (
    dict_data_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dict_type_id BIGINT NOT NULL,
    dict_label VARCHAR(200) NOT NULL,
    dict_value VARCHAR(200) NOT NULL,
    sort_order INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    remark VARCHAR(500),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_user_id BIGINT,
    update_user_id BIGINT,
    deleted_flag TINYINT DEFAULT 0,
    version INT DEFAULT 0,
    KEY idx_dict_type_id (dict_type_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS t_menu (
    menu_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    menu_name VARCHAR(100) NOT NULL,
    menu_code VARCHAR(100),
    parent_id BIGINT DEFAULT 0,
    menu_type TINYINT DEFAULT 1,
    path VARCHAR(200),
    component VARCHAR(200),
    icon VARCHAR(100),
    sort_order INT DEFAULT 0,
    visible TINYINT DEFAULT 1,
    status TINYINT DEFAULT 1,
    permission VARCHAR(100),
    remark VARCHAR(500),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_user_id BIGINT,
    update_user_id BIGINT,
    deleted_flag TINYINT DEFAULT 0,
    version INT DEFAULT 0,
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS t_employee (
    employee_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    employee_no VARCHAR(50) NOT NULL,
    employee_name VARCHAR(100) NOT NULL,
    department_id BIGINT,
    position VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(100),
    gender TINYINT DEFAULT 1,
    birthday DATE,
    entry_date DATE,
    status TINYINT DEFAULT 1,
    remark VARCHAR(500),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_user_id BIGINT,
    update_user_id BIGINT,
    deleted_flag TINYINT DEFAULT 0,
    version INT DEFAULT 0,
    UNIQUE KEY uk_employee_no (employee_no, deleted_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS t_user_session (
    session_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(500) NOT NULL,
    device_type VARCHAR(50),
    device_id VARCHAR(200),
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    login_time DATETIME NOT NULL,
    expire_time DATETIME NOT NULL,
    status TINYINT DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_flag TINYINT DEFAULT 0,
    KEY idx_user_id (user_id),
    KEY idx_token (token(100))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS t_user_theme_config (
    config_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    theme_code VARCHAR(50),
    primary_color VARCHAR(20),
    layout_mode VARCHAR(50),
    dark_mode TINYINT DEFAULT 0,
    config_json TEXT,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_flag TINYINT DEFAULT 0,
    UNIQUE KEY uk_user_id (user_id, deleted_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS t_theme_template (
    template_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_code VARCHAR(50) NOT NULL,
    template_name VARCHAR(100) NOT NULL,
    primary_color VARCHAR(20),
    config_json TEXT,
    is_default TINYINT DEFAULT 0,
    status TINYINT DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_flag TINYINT DEFAULT 0,
    UNIQUE KEY uk_template_code (template_code, deleted_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS t_i18n_resource (
    resource_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resource_key VARCHAR(200) NOT NULL,
    locale VARCHAR(20) NOT NULL,
    resource_value TEXT NOT NULL,
    module VARCHAR(100),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_flag TINYINT DEFAULT 0,
    UNIQUE KEY uk_key_locale (resource_key, locale, deleted_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS t_sys_config (
    config_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL,
    config_value TEXT,
    config_name VARCHAR(200),
    config_type VARCHAR(50),
    remark VARCHAR(500),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_flag TINYINT DEFAULT 0,
    UNIQUE KEY uk_config_key (config_key, deleted_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS t_sys_dict_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dict_type VARCHAR(100) NOT NULL,
    dict_label VARCHAR(200) NOT NULL,
    dict_value VARCHAR(200) NOT NULL,
    sort_order INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    remark VARCHAR(500),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_flag TINYINT DEFAULT 0,
    KEY idx_dict_type (dict_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS t_scheduled_job (
    job_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_name VARCHAR(100) NOT NULL,
    job_group VARCHAR(100) DEFAULT 'DEFAULT',
    job_class VARCHAR(200) NOT NULL,
    cron_expression VARCHAR(100) NOT NULL,
    job_params TEXT,
    status TINYINT DEFAULT 1,
    remark VARCHAR(500),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_flag TINYINT DEFAULT 0,
    UNIQUE KEY uk_job_name_group (job_name, job_group, deleted_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SELECT 'V2.1.5 Flyway Batch1 完成' AS status;
