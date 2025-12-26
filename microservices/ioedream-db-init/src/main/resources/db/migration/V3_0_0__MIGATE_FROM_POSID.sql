-- ================================================================================
-- IOE-DREAM 消费模块数据迁移脚本
-- ================================================================================
-- 版本: V3.0.0
-- 描述: 从POSID遗留表迁移数据到t_consume新表
-- 作者: IOE-DREAM架构团队
-- 日期: 2025-12-26
-- ================================================================================
-- 迁移范围:
--   1. 账户类别: POSID_ACCOUNTKIND → t_consume_account_kind
--   2. 补贴账户: POSID_SUBSIDY_ACCOUNT → t_consume_subsidy_account
--   3. 补贴类型: POSID_SUBSIDY_TYPE → t_consume_subsidy_type
-- ================================================================================

-- ================================================================================
-- 1. 账户类别表迁移
-- ================================================================================

-- 1.1 创建新表（如果不存在）
CREATE TABLE IF NOT EXISTS t_consume_account_kind (
    kind_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '类别ID',
    kind_code VARCHAR(50) NOT NULL UNIQUE COMMENT '类别编码',
    kind_name VARCHAR(100) NOT NULL COMMENT '类别名称',
    kind_type INT DEFAULT 1 COMMENT '类别类型(1-员工 2-访客 3-临时)',

    -- 消费模式配置
    consume_mode VARCHAR(50) DEFAULT 'FIXED_AMOUNT' COMMENT '消费模式',
    mode_config TEXT COMMENT '模式配置JSON',

    -- 折扣配置
    discount_type INT DEFAULT 0 COMMENT '折扣类型(0-无折扣 1-固定折扣 2-阶梯折扣)',
    discount_value DECIMAL(10,2) COMMENT '折扣值',

    -- 限额配置
    date_max_money DECIMAL(10,2) COMMENT '每日最大消费金额',
    date_max_count INT COMMENT '每日最大消费次数',
    month_max_money DECIMAL(10,2) COMMENT '每月最大消费金额',
    month_max_count INT COMMENT '每月最大消费次数',

    -- 其他配置
    priority INT DEFAULT 0 COMMENT '优先级',
    enabled INT DEFAULT 1 COMMENT '是否启用(0-禁用 1-启用)',
    sort_order INT DEFAULT 0 COMMENT '排序',
    remark VARCHAR(500) COMMENT '备注',

    -- 审计字段
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',

    INDEX idx_kind_code(kind_code),
    INDEX idx_enabled(enabled),
    INDEX idx_kind_type(kind_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消费账户类别表';

-- 1.2 迁移数据
INSERT INTO t_consume_account_kind (
    kind_id,
    kind_code,
    kind_name,
    kind_type,
    consume_mode,
    mode_config,
    discount_type,
    discount_value,
    date_max_money,
    date_max_count,
    month_max_money,
    month_max_count,
    priority,
    enabled,
    sort_order,
    remark,
    create_time,
    update_time,
    create_user_id,
    update_user_id,
    deleted_flag
)
SELECT
    id AS kind_id,
    code AS kind_code,
    name AS kind_name,
    type AS kind_type,
    'FIXED_AMOUNT' AS consume_mode,
    NULL AS mode_config,
    discount_type,
    discount_value,
    date_max_money,
    date_max_count,
    month_max_money,
    month_max_count,
    priority,
    available AS enabled,
    sort_order,
    remark,
    create_time,
    update_time,
    NULL AS create_user_id,
    NULL AS update_user_id,
    0 AS deleted_flag
FROM POSID_ACCOUNTKIND
WHERE NOT EXISTS (
    SELECT 1 FROM t_consume_account_kind
    WHERE t_consume_account_kind.kind_code = POSID_ACCOUNTKIND.code
);

-- 1.3 验证数据迁移
SELECT
    '账户类别迁移验证' AS check_item,
    (SELECT COUNT(*) FROM POSID_ACCOUNTKIND) AS old_count,
    (SELECT COUNT(*) FROM t_consume_account_kind) AS new_count,
    CASE
        WHEN (SELECT COUNT(*) FROM POSID_ACCOUNTKIND) = (SELECT COUNT(*) FROM t_consume_account_kind)
        THEN '✅ 数据一致'
        ELSE '❌ 数据不一致'
    END AS status;

-- ================================================================================
-- 2. 补贴类型表迁移
-- ================================================================================

-- 2.1 创建新表（如果不存在）
CREATE TABLE IF NOT EXISTS t_consume_subsidy_type (
    subsidy_type_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '补贴类型ID',
    type_code VARCHAR(50) NOT NULL UNIQUE COMMENT '类型编码',
    type_name VARCHAR(100) NOT NULL COMMENT '类型名称',
    description VARCHAR(500) COMMENT '描述',
    priority INT DEFAULT 0 COMMENT '优先级',
    accumulative INT DEFAULT 0 COMMENT '是否可累计(0-不可累计 1-可累计)',
    transferable INT DEFAULT 0 COMMENT '是否可转让(0-不可转让 1-可转让)',
    enabled INT DEFAULT 1 COMMENT '是否启用(0-禁用 1-启用)',
    sort_order INT DEFAULT 0 COMMENT '排序',
    remark VARCHAR(500) COMMENT '备注',

    -- 审计字段
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',

    INDEX idx_type_code(type_code),
    INDEX idx_enabled(enabled),
    INDEX idx_priority(priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消费补贴类型表';

-- 2.2 迁移数据
INSERT INTO t_consume_subsidy_type (
    subsidy_type_id,
    type_code,
    type_name,
    description,
    priority,
    accumulative,
    transferable,
    enabled,
    sort_order,
    remark,
    create_time,
    update_time,
    create_user_id,
    update_user_id,
    deleted_flag
)
SELECT
    id AS subsidy_type_id,
    code AS type_code,
    name AS type_name,
    description,
    priority,
    accumulative,
    transferable,
    available AS enabled,
    0 AS sort_order,
    remark,
    create_time,
    update_time,
    NULL AS create_user_id,
    NULL AS update_user_id,
    0 AS deleted_flag
FROM POSID_SUBSIDY_TYPE
WHERE NOT EXISTS (
    SELECT 1 FROM t_consume_subsidy_type
    WHERE t_consume_subsidy_type.type_code = POSID_SUBSIDY_TYPE.code
);

-- 2.3 验证数据迁移
SELECT
    '补贴类型迁移验证' AS check_item,
    (SELECT COUNT(*) FROM POSID_SUBSIDY_TYPE) AS old_count,
    (SELECT COUNT(*) FROM t_consume_subsidy_type) AS new_count,
    CASE
        WHEN (SELECT COUNT(*) FROM POSID_SUBSIDY_TYPE) = (SELECT COUNT(*) FROM t_consume_subsidy_type)
        THEN '✅ 数据一致'
        ELSE '❌ 数据不一致'
    END AS status;

-- ================================================================================
-- 3. 补贴账户表迁移
-- ================================================================================

-- 3.1 创建新表（如果不存在）
CREATE TABLE IF NOT EXISTS t_consume_subsidy_account (
    subsidy_account_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '补贴账户ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    subsidy_type_id BIGINT NOT NULL COMMENT '补贴类型ID',
    account_code VARCHAR(50) NOT NULL COMMENT '账户编码',
    account_name VARCHAR(100) COMMENT '账户名称',

    -- 余额信息
    balance DECIMAL(10,2) DEFAULT 0.00 COMMENT '补贴余额',
    frozen_amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '冻结金额',
    initial_amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '初始金额',

    -- 统计信息
    total_granted DECIMAL(10,2) DEFAULT 0.00 COMMENT '累计发放金额',
    total_used DECIMAL(10,2) DEFAULT 0.00 COMMENT '累计使用金额',

    -- 时间信息
    expire_time DATETIME COMMENT '过期时间',
    grant_time DATETIME COMMENT '发放时间',
    clear_time DATETIME COMMENT '清零时间',

    -- 状态信息
    account_status INT DEFAULT 1 COMMENT '账户状态(1-正常 2-冻结 3-已过期 4-已清零)',
    account_status_desc VARCHAR(50) COMMENT '账户状态描述',

    -- 发放信息
    grant_batch_no VARCHAR(50) COMMENT '发放批次号',
    grant_user_id BIGINT COMMENT '发放人ID',
    grant_user_name VARCHAR(100) COMMENT '发放人姓名',

    remark VARCHAR(500) COMMENT '备注',

    -- 审计字段
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',

    UNIQUE KEY uk_account_code (account_code, deleted_flag),
    INDEX idx_user_id (user_id),
    INDEX idx_subsidy_type_id (subsidy_type_id),
    INDEX idx_account_status (account_status),
    INDEX idx_expire_time (expire_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消费补贴账户表';

-- 3.2 迁移数据
INSERT INTO t_consume_subsidy_account (
    subsidy_account_id,
    user_id,
    subsidy_type_id,
    account_code,
    account_name,
    balance,
    frozen_amount,
    initial_amount,
    total_granted,
    total_used,
    expire_time,
    account_status,
    grant_batch_no,
    grant_user_id,
    grant_time,
    clear_time,
    remark,
    create_time,
    update_time,
    create_user_id,
    update_user_id,
    deleted_flag
)
SELECT
    subsidy_account_id,
    user_id,
    subsidy_type_id,
    account_code,
    account_name,
    balance,
    frozen_amount,
    initial_amount,
    total_granted,
    total_used,
    expire_time,
    account_status,
    grant_batch_no,
    grant_user_id,
    grant_time,
    NULL AS clear_time,
    remark,
    create_time,
    update_time,
    create_user_id,
    update_user_id,
    0 AS deleted_flag
FROM POSID_SUBSIDY_ACCOUNT
WHERE NOT EXISTS (
    SELECT 1 FROM t_consume_subsidy_account
    WHERE t_consume_subsidy_account.account_code = POSID_SUBSIDY_ACCOUNT.account_code
);

-- 3.3 验证数据迁移
SELECT
    '补贴账户迁移验证' AS check_item,
    (SELECT COUNT(*) FROM POSID_SUBSIDY_ACCOUNT) AS old_count,
    (SELECT COUNT(*) FROM t_consume_subsidy_account) AS new_count,
    CASE
        WHEN (SELECT COUNT(*) FROM POSID_SUBSIDY_ACCOUNT) = (SELECT COUNT(*) FROM t_consume_subsidy_account)
        THEN '✅ 数据一致'
        ELSE '❌ 数据不一致'
    END AS status;

-- ================================================================================
-- 4. 迁移完成后创建备份
-- ================================================================================

-- 4.1 备份旧表（可选，建议先备份）
-- CREATE TABLE POSID_ACCOUNTKIND_BACKUP AS SELECT * FROM POSID_ACCOUNTKIND;
-- CREATE TABLE POSID_SUBSIDY_TYPE_BACKUP AS SELECT * FROM POSID_SUBSIDY_TYPE;
-- CREATE TABLE POSID_SUBSIDY_ACCOUNT_BACKUP AS SELECT * FROM POSID_SUBSIDY_ACCOUNT;

-- 4.2 迁移总结报告
SELECT
    '=== 数据迁移总结报告 ===' AS report_title,
    '1. 账户类别表迁移' AS item_1,
    CONCAT('旧表: ', (SELECT COUNT(*) FROM POSID_ACCOUNTKIND), ' 条 → 新表: ', (SELECT COUNT(*) FROM t_consume_account_kind), ' 条') AS result_1,
    '2. 补贴类型表迁移' AS item_2,
    CONCAT('旧表: ', (SELECT COUNT(*) FROM POSID_SUBSIDY_TYPE), ' 条 → 新表: ', (SELECT COUNT(*) FROM t_consume_subsidy_type), ' 条') AS result_2,
    '3. 补贴账户表迁移' AS item_3,
    CONCAT('旧表: ', (SELECT COUNT(*) FROM POSID_SUBSIDY_ACCOUNT), ' 条 → 新表: ', (SELECT COUNT(*) FROM t_consume_subsidy_account), ' 条') AS result_3,
    '迁移完成时间: ' AS complete_time,
    NOW() AS complete_datetime;

-- ================================================================================
-- 注意事项
-- ================================================================================
-- 1. 本脚本支持重复执行（使用 NOT EXISTS 避免重复插入）
-- 2. 建议在执行前先备份旧表数据
-- 3. 执行后请验证数据一致性
-- 4. 验证通过后，旧表（POSID_*）可以重命名为 _BACKUP 后期删除
-- 5. 相关代码需要同步更新（SubsidyDeductionManager等）
-- ================================================================================
