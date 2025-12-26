-- ==================== 数据迁移脚本：旧表到POSID新表 ====================
-- 消费模块完整企业级实现 - 数据迁移
-- 迁移方向：t_consume_* → POSID_*
-- 创建时间: 2025-12-23
-- 作者: IOE-DREAM架构团队
-- 版本: V1.0.0
--
-- 迁移策略：
-- 1. 先迁移账户数据（t_consume_account → POSID_ACCOUNT）
-- 2. 再迁移交易数据（t_consume_record → POSID_TRANSACTION）
-- 3. 最后迁移资金流水（t_consume_account_transaction → POSID_CAPITAL_FLOW）
--
-- 注意事项：
-- - 迁移前先备份原表数据
-- - 迁移过程中保持双写，确保数据一致性
-- - 迁移后进行数据验证，确保零数据丢失
-- - 验证通过后再删除旧表

-- ==================== 1. 账户数据迁移 ====================
-- 迁移 t_consume_account → POSID_ACCOUNT
INSERT INTO POSID_ACCOUNT (
    account_id,
    user_id,
    account_code,
    account_name,
    account_type,
    balance,
    frozen_amount,
    credit_limit,
    total_recharge,
    total_consume,
    account_status,
    password,
    enable_auto_recharge,
    auto_recharge_amount,
    auto_recharge_threshold,
    last_consume_time,
    last_recharge_time,
    version,
    create_time,
    update_time,
    create_user_id,
    update_user_id,
    deleted_flag
)
SELECT
    account_id,
    user_id,
    account_code,
    account_name,
    account_type,
    balance,
    frozen_amount,
    credit_limit,
    total_recharge,
    total_consume,
    account_status,
    password,
    enable_auto_recharge,
    auto_recharge_amount,
    auto_recharge_threshold,
    last_consume_time,
    last_recharge_time,
    version,
    create_time,
    update_time,
    create_user_id,
    update_user_id,
    deleted_flag
FROM t_consume_account
WHERE deleted_flag = 0;

-- 验证账户数据迁移
-- 迁移后运行以下验证，确保数据完整性
-- SELECT
--     (SELECT COUNT(*) FROM t_consume_account WHERE deleted_flag = 0) AS old_count,
--     (SELECT COUNT(*) FROM POSID_ACCOUNT) AS new_count,
--     (SELECT COUNT(*) FROM t_consume_account WHERE deleted_flag = 0) = (SELECT COUNT(*) FROM POSID_ACCOUNT) AS is_match;

-- ==================== 2. 交易数据迁移 ====================
-- 迁移 t_consume_record → POSID_TRANSACTION
-- 注意：新表增加了 consume_mode 字段，默认为 'FIXED_AMOUNT'
INSERT INTO POSID_TRANSACTION (
    transaction_id,
    account_id,
    user_id,
    user_name,
    device_id,
    device_name,
    merchant_id,
    merchant_name,
    amount,
    consume_mode,
    consume_type,
    consume_type_name,
    product_detail,
    payment_method,
    order_no,
    transaction_no,
    transaction_status,
    consume_status,
    consume_time,
    consume_location,
    refund_status,
    refund_amount,
    refund_time,
    refund_reason,
    offline_flag,
    sync_status,
    sync_time,
    remark,
    create_time,
    update_time,
    deleted_flag
)
SELECT
    record_id AS transaction_id,
    account_id,
    user_id,
    user_name,
    device_id,
    device_name,
    merchant_id,
    merchant_name,
    amount,
    'FIXED_AMOUNT' AS consume_mode,  -- 默认消费模式
    consume_type,
    consume_type_name,
    product_detail,
    payment_method,
    order_no,
    transaction_no,
    transaction_status,
    consume_status,
    consume_time,
    consume_location,
    refund_status,
    refund_amount,
    refund_time,
    refund_reason,
    offline_flag,
    sync_status,
    sync_time,
    remark,
    create_time,
    update_time,
    deleted_flag
FROM t_consume_record
WHERE deleted_flag = 0;

-- 验证交易数据迁移
-- 迁移后运行以下验证，确保数据完整性
-- SELECT
--     (SELECT COUNT(*) FROM t_consume_record WHERE deleted_flag = 0) AS old_count,
--     (SELECT COUNT(*) FROM POSID_TRANSACTION) AS new_count,
--     (SELECT COUNT(*) FROM t_consume_record WHERE deleted_flag = 0) = (SELECT COUNT(*) FROM POSID_TRANSACTION) AS is_match;

-- ==================== 3. 资金流水迁移 ====================
-- 迁移 t_consume_account_transaction → POSID_CAPITAL_FLOW
-- 注意：字段映射关系需要调整
INSERT INTO POSID_CAPITAL_FLOW (
    flow_id,
    account_id,
    flow_type,
    amount,
    balance_before,
    balance_after,
    related_record_id,
    related_order_no,
    transaction_no,
    remark,
    create_time
)
SELECT
    transaction_id AS flow_id,
    account_id,
    transaction_type AS flow_type,
    amount,
    balance_before,
    balance_after,
    related_record_id,
    related_order_no,
    transaction_no,
    remark,
    transaction_time AS create_time
FROM t_consume_account_transaction
WHERE deleted_flag = 0;

-- 验证资金流水迁移
-- 迁移后运行以下验证，确保数据完整性
-- SELECT
--     (SELECT COUNT(*) FROM t_consume_account_transaction WHERE deleted_flag = 0) AS old_count,
--     (SELECT COUNT(*) FROM POSID_CAPITAL_FLOW) AS new_count,
--     (SELECT COUNT(*) FROM t_consume_account_transaction WHERE deleted_flag = 0) = (SELECT COUNT(*) FROM POSID_CAPITAL_FLOW) AS is_match;

-- ==================== 迁移完成验证 ====================
-- 运行以下SQL验证所有数据迁移是否成功
SELECT
    '账户数据' AS data_type,
    (SELECT COUNT(*) FROM t_consume_account WHERE deleted_flag = 0) AS old_count,
    (SELECT COUNT(*) FROM POSID_ACCOUNT) AS new_count,
    CASE
        WHEN (SELECT COUNT(*) FROM t_consume_account WHERE deleted_flag = 0) = (SELECT COUNT(*) FROM POSID_ACCOUNT)
        THEN '✓ 数据一致'
        ELSE '✗ 数据不一致'
    END AS validation_result
UNION ALL
SELECT
    '交易数据' AS data_type,
    (SELECT COUNT(*) FROM t_consume_record WHERE deleted_flag = 0) AS old_count,
    (SELECT COUNT(*) FROM POSID_TRANSACTION) AS new_count,
    CASE
        WHEN (SELECT COUNT(*) FROM t_consume_record WHERE deleted_flag = 0) = (SELECT COUNT(*) FROM POSID_TRANSACTION)
        THEN '✓ 数据一致'
        ELSE '✗ 数据不一致'
    END AS validation_result
UNION ALL
SELECT
    '资金流水' AS data_type,
    (SELECT COUNT(*) FROM t_consume_account_transaction WHERE deleted_flag = 0) AS old_count,
    (SELECT COUNT(*) FROM POSID_CAPITAL_FLOW) AS new_count,
    CASE
        WHEN (SELECT COUNT(*) FROM t_consume_account_transaction WHERE deleted_flag = 0) = (SELECT COUNT(*) FROM POSID_CAPITAL_FLOW)
        THEN '✓ 数据一致'
        ELSE '✗ 数据不一致'
    END AS validation_result;

-- ==================== 备份旧表（可选）====================
-- 数据验证通过后，可以选择重命名旧表作为备份
-- RENAME TABLE t_consume_account TO t_consume_account_backup_20251223;
-- RENAME TABLE t_consume_record TO t_consume_record_backup_20251223;
-- RENAME TABLE t_consume_account_transaction TO t_consume_account_transaction_backup_20251223;

-- ==================== 删除旧表（谨慎操作）====================
-- ⚠️ 警告：仅在确认数据迁移成功且业务稳定运行1-2周后才删除旧表
-- DROP TABLE IF EXISTS t_consume_account;
-- DROP TABLE IF EXISTS t_consume_record;
-- DROP TABLE IF EXISTS t_consume_account_transaction;
