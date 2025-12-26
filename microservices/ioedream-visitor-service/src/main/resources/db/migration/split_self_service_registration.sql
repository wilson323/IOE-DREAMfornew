-- ====================================================================
-- Entity拆分数据库迁移脚本
-- 原Entity: SelfServiceRegistrationEntity (451行, 36字段)
-- 拆分为: 6个Entity (遵循单一职责原则)
-- 作者: IOE-DREAM架构团队
-- 日期: 2025-12-26
-- ====================================================================

-- ====================================================================
-- Step 1: 创建新表
-- ====================================================================

-- 1. 访客生物识别信息表
CREATE TABLE IF NOT EXISTS t_visitor_biometric (
    biometric_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '生物识别ID',
    registration_id BIGINT NOT NULL COMMENT '关联的自助登记ID（外键）',
    face_photo_url VARCHAR(512) COMMENT '访客人脸照片URL',
    face_feature TEXT COMMENT '人脸特征向量（Base64编码）',
    id_card_photo_url VARCHAR(512) COMMENT '身份证照片URL',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
    INDEX idx_registration_id (registration_id),
    INDEX idx_deleted_flag (deleted_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='访客生物识别信息表';

-- 2. 访客审批流程表
CREATE TABLE IF NOT EXISTS t_visitor_approval (
    approval_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '审批ID',
    registration_id BIGINT NOT NULL COMMENT '关联的自助登记ID（外键）',
    approver_id BIGINT COMMENT '审批人ID',
    approver_name VARCHAR(100) COMMENT '审批人姓名',
    approval_time DATETIME COMMENT '审批时间',
    approval_comment VARCHAR(500) COMMENT '审批意见',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
    INDEX idx_registration_id (registration_id),
    INDEX idx_approver_id (approver_id),
    INDEX idx_deleted_flag (deleted_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='访客审批流程表';

-- 3. 访客访问记录表
CREATE TABLE IF NOT EXISTS t_visitor_visit_record (
    record_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '访问记录ID',
    registration_id BIGINT NOT NULL COMMENT '关联的自助登记ID（外键）',
    check_in_time DATETIME COMMENT '签到时间',
    check_out_time DATETIME COMMENT '签离时间',
    escort_required TINYINT DEFAULT 0 COMMENT '是否需要陪同 0-否 1-是',
    escort_user VARCHAR(100) COMMENT '陪同人姓名',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
    INDEX idx_registration_id (registration_id),
    INDEX idx_check_in_time (check_in_time),
    INDEX idx_deleted_flag (deleted_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='访客访问记录表';

-- 4. 访客终端信息表
CREATE TABLE IF NOT EXISTS t_visitor_terminal_info (
    terminal_info_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '终端信息ID',
    registration_id BIGINT NOT NULL COMMENT '关联的自助登记ID（外键）',
    terminal_id VARCHAR(100) COMMENT '终端ID',
    terminal_location VARCHAR(200) COMMENT '终端位置',
    visitor_card VARCHAR(100) COMMENT '访客卡号',
    card_print_status TINYINT DEFAULT 0 COMMENT '访客卡打印状态 0-未打印 1-打印中 2-已打印 3-打印失败',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
    INDEX idx_registration_id (registration_id),
    INDEX idx_terminal_id (terminal_id),
    INDEX idx_deleted_flag (deleted_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='访客终端信息表';

-- 5. 访客附加信息表
CREATE TABLE IF NOT EXISTS t_visitor_additional_info (
    additional_info_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '附加信息ID',
    registration_id BIGINT NOT NULL COMMENT '关联的自助登记ID（外键）',
    belongings TEXT COMMENT '携带物品（JSON格式）',
    license_plate VARCHAR(20) COMMENT '车牌号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
    INDEX idx_registration_id (registration_id),
    INDEX idx_deleted_flag (deleted_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='访客附加信息表';

-- ====================================================================
-- Step 2: 数据迁移
-- ====================================================================

-- 2.1 迁移生物识别信息
INSERT INTO t_visitor_biometric (
    registration_id,
    face_photo_url,
    face_feature,
    id_card_photo_url,
    create_time,
    update_time,
    deleted_flag
)
SELECT
    registration_id,
    face_photo_url,
    face_feature,
    id_card_photo_url,
    create_time,
    update_time,
    COALESCE(deleted_flag, 0)
FROM t_visitor_self_service_registration
WHERE face_photo_url IS NOT NULL
   OR face_feature IS NOT NULL
   OR id_card_photo_url IS NOT NULL;

-- 2.2 迁移审批流程信息
INSERT INTO t_visitor_approval (
    registration_id,
    approver_id,
    approver_name,
    approval_time,
    approval_comment,
    create_time,
    update_time,
    deleted_flag
)
SELECT
    registration_id,
    approver_id,
    approver_name,
    approval_time,
    approval_comment,
    create_time,
    update_time,
    COALESCE(deleted_flag, 0)
FROM t_visitor_self_service_registration
WHERE approver_id IS NOT NULL
   OR approver_name IS NOT NULL;

-- 2.3 迁移访问记录信息
INSERT INTO t_visitor_visit_record (
    registration_id,
    check_in_time,
    check_out_time,
    escort_required,
    escort_user,
    create_time,
    update_time,
    deleted_flag
)
SELECT
    registration_id,
    check_in_time,
    check_out_time,
    COALESCE(escort_required, 0),
    escort_user,
    create_time,
    update_time,
    COALESCE(deleted_flag, 0)
FROM t_visitor_self_service_registration
WHERE check_in_time IS NOT NULL
   OR check_out_time IS NOT NULL
   OR escort_required IS NOT NULL
   OR escort_user IS NOT NULL;

-- 2.4 迁移终端信息
INSERT INTO t_visitor_terminal_info (
    registration_id,
    terminal_id,
    terminal_location,
    visitor_card,
    card_print_status,
    create_time,
    update_time,
    deleted_flag
)
SELECT
    registration_id,
    terminal_id,
    terminal_location,
    visitor_card,
    COALESCE(card_print_status, 0),
    create_time,
    update_time,
    COALESCE(deleted_flag, 0)
FROM t_visitor_self_service_registration
WHERE terminal_id IS NOT NULL
   OR visitor_card IS NOT NULL;

-- 2.5 迁移附加信息
INSERT INTO t_visitor_additional_info (
    registration_id,
    belongings,
    license_plate,
    create_time,
    update_time,
    deleted_flag
)
SELECT
    registration_id,
    belongings,
    license_plate,
    create_time,
    update_time,
    COALESCE(deleted_flag, 0)
FROM t_visitor_self_service_registration
WHERE belongings IS NOT NULL
   OR license_plate IS NOT NULL;

-- ====================================================================
-- Step 3: 清理原表字段（可选，建议先保留作为备份）
-- ====================================================================

-- ⚠️ 警告：执行以下SQL将永久删除原表字段，建议先备份数据！
-- ⚠️ 建议在验证新表数据正确后再执行！

-- 3.1 备份原表（建议）
-- CREATE TABLE t_visitor_self_service_registration_backup AS SELECT * FROM t_visitor_self_service_registration;

-- 3.2 删除生物识别相关字段（可选）
-- ALTER TABLE t_visitor_self_service_registration DROP COLUMN face_photo_url;
-- ALTER TABLE t_visitor_self_service_registration DROP COLUMN face_feature;
-- ALTER TABLE t_visitor_self_service_registration DROP COLUMN id_card_photo_url;

-- 3.3 删除审批流程相关字段（可选）
-- ALTER TABLE t_visitor_self_service_registration DROP COLUMN approver_id;
-- ALTER TABLE t_visitor_self_service_registration DROP COLUMN approver_name;
-- ALTER TABLE t_visitor_self_service_registration DROP COLUMN approval_time;
-- ALTER TABLE t_visitor_self_service_registration DROP COLUMN approval_comment;

-- 3.4 删除访问记录相关字段（可选）
-- ALTER TABLE t_visitor_self_service_registration DROP COLUMN check_in_time;
-- ALTER TABLE t_visitor_self_service_registration DROP COLUMN check_out_time;
-- ALTER TABLE t_visitor_self_service_registration DROP COLUMN escort_required;
-- ALTER TABLE t_visitor_self_service_registration DROP COLUMN escort_user;

-- 3.5 删除终端信息相关字段（可选）
-- ALTER TABLE t_visitor_self_service_registration DROP COLUMN terminal_id;
-- ALTER TABLE t_visitor_self_service_registration DROP COLUMN terminal_location;
-- ALTER TABLE t_visitor_self_service_registration DROP COLUMN visitor_card;
-- ALTER TABLE t_visitor_self_service_registration DROP COLUMN card_print_status;

-- 3.6 删除附加信息相关字段（可选）
-- ALTER TABLE t_visitor_self_service_registration DROP COLUMN belongings;
-- ALTER TABLE t_visitor_self_service_registration DROP COLUMN license_plate;

-- ====================================================================
-- Step 4: 验证数据迁移
-- ====================================================================

-- 验证生物识别信息迁移
SELECT
    '生物识别信息' AS table_name,
    COUNT(*) AS migrated_count,
    COUNT(DISTINCT registration_id) AS unique_registrations
FROM t_visitor_biometric;

-- 验证审批流程信息迁移
SELECT
    '审批流程信息' AS table_name,
    COUNT(*) AS migrated_count,
    COUNT(DISTINCT registration_id) AS unique_registrations
FROM t_visitor_approval;

-- 验证访问记录信息迁移
SELECT
    '访问记录信息' AS table_name,
    COUNT(*) AS migrated_count,
    COUNT(DISTINCT registration_id) AS unique_registrations
FROM t_visitor_visit_record;

-- 验证终端信息迁移
SELECT
    '终端信息' AS table_name,
    COUNT(*) AS migrated_count,
    COUNT(DISTINCT registration_id) AS unique_registrations
FROM t_visitor_terminal_info;

-- 验证附加信息迁移
SELECT
    '附加信息' AS table_name,
    COUNT(*) AS migrated_count,
    COUNT(DISTINCT registration_id) AS unique_registrations
FROM t_visitor_additional_info;

-- ====================================================================
-- 迁移完成
-- ====================================================================
