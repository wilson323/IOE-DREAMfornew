-- =====================================================
-- IOE-DREAM 数据库迁移脚本
-- 版本: V2.1.5
-- 描述: 批量补齐缺口表（第1批：RBAC核心字段对齐 + 字典/菜单/员工/会话/主题）
-- =====================================================

USE ioedream;

-- =====================================================
-- 1. t_user 字段对齐（从 t_common_user rename 而来，补齐实体字段）
-- =====================================================
ALTER TABLE t_user
    ADD COLUMN IF NOT EXISTS login_fail_count INT DEFAULT 0 COMMENT '登录失败次数' AFTER last_login_ip,
    ADD COLUMN IF NOT EXISTS account_locked TINYINT DEFAULT 0 COMMENT '账户锁定：0-未锁定 1-已锁定' AFTER login_fail_count,
    ADD COLUMN IF NOT EXISTS lock_time DATETIME COMMENT '锁定时间' AFTER account_locked,
    ADD COLUMN IF NOT EXISTS unlock_time DATETIME COMMENT '解锁时间' AFTER lock_time;

-- =====================================================
-- 2. t_role 字段对齐（从 t_common_role rename 而来，补齐实体字段）
-- =====================================================
ALTER TABLE t_role
    ADD COLUMN IF NOT EXISTS role_desc VARCHAR(500) COMMENT '角色描述' AFTER role_name,
    ADD COLUMN IF NOT EXISTS data_scope INT DEFAULT 1 COMMENT '数据范围：1-全部 2-自定义 3-本部门 4-本部门及子部门 5-仅本人' AFTER role_desc,
    ADD COLUMN IF NOT EXISTS is_system TINYINT DEFAULT 0 COMMENT '是否系统角色：0-否 1-是' AFTER data_scope;

-- =====================================================
-- 3. t_user_role 字段对齐（从 t_common_user_role rename 而来，补齐主键/字段）
-- =====================================================
ALTER TABLE t_user_role
    ADD COLUMN IF NOT EXISTS user_role_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户角色关联ID' FIRST,
    ADD COLUMN IF NOT EXISTS status TINYINT DEFAULT 1 COMMENT '状态' AFTER role_id,
    ADD COLUMN IF NOT EXISTS update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER create_time,
    ADD COLUMN IF NOT EXISTS deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记' AFTER update_time,
    ADD COLUMN IF NOT EXISTS version INT DEFAULT 0 COMMENT '乐观锁版本号' AFTER deleted_flag;

-- =====================================================
-- 4. t_role_resource 字段对齐（从 t_common_role_permission rename 而来）
-- =====================================================
ALTER TABLE t_role_resource
    ADD COLUMN IF NOT EXISTS role_resource_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '角色资源关联ID' FIRST,
    ADD COLUMN IF NOT EXISTS resource_id BIGINT COMMENT '资源ID' AFTER role_id,
    ADD COLUMN IF NOT EXISTS status TINYINT DEFAULT 1 COMMENT '状态' AFTER resource_id,
    ADD COLUMN IF NOT EXISTS update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER create_time,
    ADD COLUMN IF NOT EXISTS deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记' AFTER update_time,
    ADD COLUMN IF NOT EXISTS version INT DEFAULT 0 COMMENT '乐观锁版本号' AFTER deleted_flag;

-- 数据迁移：permission_id -> resource_id
UPDATE t_role_resource SET resource_id = permission_id WHERE resource_id IS NULL AND permission_id IS NOT NULL;

-- =====================================================
-- 5. t_dict_type（字典类型表）
-- =====================================================
CREATE TABLE IF NOT EXISTS t_dict_type (
    dict_type_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '字典类型ID',
    dict_type_code VARCHAR(100) NOT NULL COMMENT '字典类型编码',
    dict_type_name VARCHAR(200) NOT NULL COMMENT '字典类型名称',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常 2-禁用',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',
    UNIQUE KEY uk_dict_type_code (dict_type_code, deleted_flag),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典类型表';

-- =====================================================
-- 6. t_dict_data（字典数据表）
-- =====================================================
CREATE TABLE IF NOT EXISTS t_dict_data (
    dict_data_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '字典数据ID',
    dict_type_id BIGINT NOT NULL COMMENT '字典类型ID',
    dict_label VARCHAR(200) NOT NULL COMMENT '字典标签',
    dict_value VARCHAR(200) NOT NULL COMMENT '字典值',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常 2-禁用',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',
    KEY idx_dict_type_id (dict_type_id),
    KEY idx_status (status),
    KEY idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典数据表';

-- =====================================================
-- 7. t_menu（菜单表）
-- =====================================================
CREATE TABLE IF NOT EXISTS t_menu (
    menu_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '菜单ID',
    menu_name VARCHAR(100) NOT NULL COMMENT '菜单名称',
    menu_code VARCHAR(100) COMMENT '菜单编码',
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID',
    menu_type TINYINT DEFAULT 1 COMMENT '菜单类型：1-目录 2-菜单 3-按钮',
    path VARCHAR(200) COMMENT '路由路径',
    component VARCHAR(200) COMMENT '组件路径',
    icon VARCHAR(100) COMMENT '图标',
    sort_order INT DEFAULT 0 COMMENT '排序',
    visible TINYINT DEFAULT 1 COMMENT '是否可见：0-隐藏 1-显示',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常 2-禁用',
    permission VARCHAR(100) COMMENT '权限标识',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',
    KEY idx_parent_id (parent_id),
    KEY idx_menu_type (menu_type),
    KEY idx_status (status),
    KEY idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜单表';

-- =====================================================
-- 8. t_employee（员工表）
-- =====================================================
CREATE TABLE IF NOT EXISTS t_employee (
    employee_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '员工ID',
    user_id BIGINT COMMENT '关联用户ID',
    employee_no VARCHAR(50) NOT NULL COMMENT '员工编号',
    employee_name VARCHAR(100) NOT NULL COMMENT '员工姓名',
    department_id BIGINT COMMENT '部门ID',
    position VARCHAR(100) COMMENT '职位',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    gender TINYINT DEFAULT 1 COMMENT '性别：1-男 2-女',
    birthday DATE COMMENT '生日',
    entry_date DATE COMMENT '入职日期',
    status TINYINT DEFAULT 1 COMMENT '状态：1-在职 2-离职',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',
    UNIQUE KEY uk_employee_no (employee_no, deleted_flag),
    KEY idx_user_id (user_id),
    KEY idx_department_id (department_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='员工表';

-- =====================================================
-- 9. t_user_session（用户会话表）
-- =====================================================
CREATE TABLE IF NOT EXISTS t_user_session (
    session_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '会话ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    token VARCHAR(500) NOT NULL COMMENT '令牌',
    device_type VARCHAR(50) COMMENT '设备类型',
    device_id VARCHAR(200) COMMENT '设备ID',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    user_agent VARCHAR(500) COMMENT '用户代理',
    login_time DATETIME NOT NULL COMMENT '登录时间',
    expire_time DATETIME NOT NULL COMMENT '过期时间',
    status TINYINT DEFAULT 1 COMMENT '状态：1-有效 2-已注销',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    KEY idx_user_id (user_id),
    KEY idx_token (token(100)),
    KEY idx_expire_time (expire_time),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户会话表';

-- =====================================================
-- 10. t_user_theme_config（用户主题配置表）
-- =====================================================
CREATE TABLE IF NOT EXISTS t_user_theme_config (
    config_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '配置ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    theme_code VARCHAR(50) COMMENT '主题编码',
    primary_color VARCHAR(20) COMMENT '主色调',
    layout_mode VARCHAR(50) COMMENT '布局模式',
    dark_mode TINYINT DEFAULT 0 COMMENT '暗黑模式：0-关闭 1-开启',
    config_json TEXT COMMENT '配置JSON',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    UNIQUE KEY uk_user_id (user_id, deleted_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户主题配置表';

-- =====================================================
-- 11. t_theme_template（主题模板表）
-- =====================================================
CREATE TABLE IF NOT EXISTS t_theme_template (
    template_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '模板ID',
    template_code VARCHAR(50) NOT NULL COMMENT '模板编码',
    template_name VARCHAR(100) NOT NULL COMMENT '模板名称',
    primary_color VARCHAR(20) COMMENT '主色调',
    config_json TEXT COMMENT '配置JSON',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认：0-否 1-是',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常 2-禁用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    UNIQUE KEY uk_template_code (template_code, deleted_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='主题模板表';

-- =====================================================
-- 12. t_i18n_resource（国际化资源表）
-- =====================================================
CREATE TABLE IF NOT EXISTS t_i18n_resource (
    resource_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '资源ID',
    resource_key VARCHAR(200) NOT NULL COMMENT '资源键',
    locale VARCHAR(20) NOT NULL COMMENT '语言区域',
    resource_value TEXT NOT NULL COMMENT '资源值',
    module VARCHAR(100) COMMENT '模块',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    UNIQUE KEY uk_key_locale (resource_key, locale, deleted_flag),
    KEY idx_module (module)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='国际化资源表';

-- =====================================================
-- 13. t_sys_config（系统配置表）
-- =====================================================
CREATE TABLE IF NOT EXISTS t_sys_config (
    config_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '配置ID',
    config_key VARCHAR(100) NOT NULL COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    config_name VARCHAR(200) COMMENT '配置名称',
    config_type VARCHAR(50) COMMENT '配置类型',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    UNIQUE KEY uk_config_key (config_key, deleted_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- =====================================================
-- 14. t_sys_dict_data（系统字典数据表，兼容旧命名）
-- =====================================================
CREATE TABLE IF NOT EXISTS t_sys_dict_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    dict_type VARCHAR(100) NOT NULL COMMENT '字典类型',
    dict_label VARCHAR(200) NOT NULL COMMENT '字典标签',
    dict_value VARCHAR(200) NOT NULL COMMENT '字典值',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    KEY idx_dict_type (dict_type),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统字典数据表';

-- =====================================================
-- 15. t_scheduled_job（定时任务表）
-- =====================================================
CREATE TABLE IF NOT EXISTS t_scheduled_job (
    job_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '任务ID',
    job_name VARCHAR(100) NOT NULL COMMENT '任务名称',
    job_group VARCHAR(100) DEFAULT 'DEFAULT' COMMENT '任务组',
    job_class VARCHAR(200) NOT NULL COMMENT '任务类',
    cron_expression VARCHAR(100) NOT NULL COMMENT 'Cron表达式',
    job_params TEXT COMMENT '任务参数',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常 2-暂停',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    UNIQUE KEY uk_job_name_group (job_name, job_group, deleted_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='定时任务表';

SELECT 'V2.1.5 批量补齐第1批表完成' AS status;
