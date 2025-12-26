-- ============================================================
-- 消费模块双写验证脚本
-- ============================================================
-- 功能：验证旧表（t_consume_*）和新表（POSID_*）的数据一致性
-- 执行频率：每10分钟执行一次
-- 验证周期：1-2周
-- ============================================================

USE ioedream;

-- ============================================================
-- 1. 数据行数对比验证
-- ============================================================

-- 1.1 账户表对比
SELECT
    'account' AS table_type,
    (SELECT COUNT(*) FROM t_consume_account WHERE deleted_flag = 0) AS old_count,
    (SELECT COUNT(*) FROM POSID_ACCOUNT WHERE deleted_flag = 0) AS new_count,
    CASE
        WHEN (SELECT COUNT(*) FROM t_consume_account WHERE deleted_flag = 0) =
             (SELECT COUNT(*) FROM POSID_ACCOUNT WHERE deleted_flag = 0)
        THEN 'PASS'
        ELSE 'FAIL'
    END AS status;

-- 1.2 消费记录对比
SELECT
    'consume_record' AS table_type,
    (SELECT COUNT(*) FROM t_consume_record WHERE deleted_flag = 0) AS old_count,
    (SELECT COUNT(*) FROM POSID_CONSUME_RECORD WHERE deleted_flag = 0) AS new_count,
    CASE
        WHEN (SELECT COUNT(*) FROM t_consume_record WHERE deleted_flag = 0) =
             (SELECT COUNT(*) FROM POSID_CONSUME_RECORD WHERE deleted_flag = 0)
        THEN 'PASS'
        ELSE 'FAIL'
    END AS status;

-- 1.3 交易流水对比
SELECT
    'transaction' AS table_type,
    (SELECT COUNT(*) FROM t_consume_account_transaction WHERE deleted_flag = 0) AS old_count,
    (SELECT COUNT(*) FROM POSID_TRANSACTION WHERE deleted_flag = 0) AS new_count,
    CASE
        WHEN (SELECT COUNT(*) FROM t_consume_account_transaction WHERE deleted_flag = 0) =
             (SELECT COUNT(*) FROM POSID_TRANSACTION WHERE deleted_flag = 0)
        THEN 'PASS'
        ELSE 'FAIL'
    END AS status;

-- ============================================================
-- 2. 数据内容对比验证（抽样100条）
-- ============================================================

-- 2.1 账户余额对比
SELECT
    COUNT(*) AS total_samples,
    SUM(CASE WHEN old.balance = new.balance THEN 1 ELSE 0 END) AS consistent_count,
    SUM(CASE WHEN old.balance != new.balance THEN 1 ELSE 0 END) AS inconsistent_count,
    ROUND(SUM(CASE WHEN old.balance = new.balance THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) AS consistency_rate
FROM (
    SELECT
        old.account_id,
        old.balance AS balance,
        new.balance AS balance
    FROM t_consume_account old
    INNER JOIN POSID_ACCOUNT new ON old.account_id = new.account_id
    WHERE old.deleted_flag = 0 AND new.deleted_flag = 0
    LIMIT 100
) AS samples;

-- 2.2 消费金额对比
SELECT
    COUNT(*) AS total_samples,
    SUM(CASE WHEN old.consume_amount = new.consume_amount THEN 1 ELSE 0 END) AS consistent_count,
    SUM(CASE WHEN old.consume_amount != new.consume_amount THEN 1 ELSE 0 END) AS inconsistent_count,
    ROUND(SUM(CASE WHEN old.consume_amount = new.consume_amount THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) AS consistency_rate
FROM (
    SELECT
        old.record_id,
        old.consume_amount,
        new.consume_amount
    FROM t_consume_record old
    INNER JOIN POSID_CONSUME_RECORD new ON old.record_id = new.record_id
    WHERE old.deleted_flag = 0 AND new.deleted_flag = 0
    LIMIT 100
) AS samples;

-- 2.3 交易金额对比
SELECT
    COUNT(*) AS total_samples,
    SUM(CASE WHEN ABS(old.amount - new.amount) < 0.01 THEN 1 ELSE 0 END) AS consistent_count,
    SUM(CASE WHEN ABS(old.amount - new.amount) >= 0.01 THEN 1 ELSE 0 END) AS inconsistent_count,
    ROUND(SUM(CASE WHEN ABS(old.amount - new.amount) < 0.01 THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) AS consistency_rate
FROM (
    SELECT
        old.transaction_id,
        old.amount,
        new.amount
    FROM t_consume_account_transaction old
    INNER JOIN POSID_TRANSACTION new ON old.transaction_id = new.transaction_id
    WHERE old.deleted_flag = 0 AND new.deleted_flag = 0
    LIMIT 100
) AS samples;

-- ============================================================
-- 3. 差异明细查询
-- ============================================================

-- 3.1 查找账户余额不一致的记录
SELECT
    old.account_id,
    old.user_id,
    old.balance AS old_balance,
    new.balance AS new_balance,
    ABS(old.balance - new.balance) AS difference
FROM t_consume_account old
INNER JOIN POSID_ACCOUNT new ON old.account_id = new.account_id
WHERE old.deleted_flag = 0
  AND new.deleted_flag = 0
  AND old.balance != new.balance
LIMIT 10;

-- 3.2 查找消费记录不一致的记录
SELECT
    old.record_id,
    old.user_id,
    old.consume_amount AS old_amount,
    new.consume_amount AS new_amount,
    ABS(old.consume_amount - new.consume_amount) AS difference
FROM t_consume_record old
INNER JOIN POSID_CONSUME_RECORD new ON old.record_id = new.record_id
WHERE old.deleted_flag = 0
  AND new.deleted_flag = 0
  AND old.consume_amount != new.consume_amount
LIMIT 10;

-- ============================================================
-- 4. 数据一致性汇总报告
-- ============================================================

SELECT
    'Validation Report' AS report_type,
    NOW() AS report_time,

    -- 账户表统计
    (SELECT COUNT(*) FROM t_consume_account WHERE deleted_flag = 0) AS old_account_count,
    (SELECT COUNT(*) FROM POSID_ACCOUNT WHERE deleted_flag = 0) AS new_account_count,

    -- 消费记录统计
    (SELECT COUNT(*) FROM t_consume_record WHERE deleted_flag = 0) AS old_record_count,
    (SELECT COUNT(*) FROM POSID_CONSUME_RECORD WHERE deleted_flag = 0) AS new_record_count,

    -- 交易流水统计
    (SELECT COUNT(*) FROM t_consume_account_transaction WHERE deleted_flag = 0) AS old_transaction_count,
    (SELECT COUNT(*) FROM POSID_TRANSACTION WHERE deleted_flag = 0) AS new_transaction_count;
