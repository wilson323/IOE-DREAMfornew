-- ============================================================
-- 切换到新表脚本
-- ============================================================
-- 功能：从双写模式切换到只写新表模式
-- 版本：2025-12-23
-- 作者：IOE-DREAM架构团队
-- ============================================================

USE ioedream;

-- ============================================================
-- 执行前检查清单
-- ============================================================

-- 1. 检查双写验证日志（最近24小时）
SELECT
    validation_type,
    COUNT(*) AS total_validations,
    SUM(CASE WHEN validation_status = 1 THEN 1 ELSE 0 END) AS passed_count,
    SUM(CASE WHEN validation_status = 2 THEN 1 ELSE 0 END) AS failed_count,
    AVG(consistency_rate) AS avg_consistency_rate,
    MIN(consistency_rate) AS min_consistency_rate
FROM dual_write_validation_log
WHERE validate_time >= DATE_SUB(NOW(), INTERVAL 24 HOUR)
GROUP BY validation_type;

-- 期望结果：
-- - passed_count = total_validations（全部通过）
-- - avg_consistency_rate >= 0.999（平均一致性≥99.9%）
-- - min_consistency_rate >= 0.999（最小一致性≥99.9%）

-- 2. 检查未解决的数据差异
SELECT
    data_type,
    COUNT(*) AS unresolved_count
FROM dual_write_difference_record
WHERE resolved = 0
GROUP BY data_type;

-- 期望结果：0行（无未解决差异）

-- 3. 检查新旧表数据量对比
SELECT
    '旧表账户' AS label,
    COUNT(*) AS count
FROM t_consume_account
WHERE deleted_flag = 0
UNION ALL
SELECT
    '新表账户' AS label,
    COUNT(*) AS count
FROM POSID_ACCOUNT
WHERE deleted_flag = 0
UNION ALL
SELECT
    '旧表消费记录' AS label,
    COUNT(*) AS count
FROM t_consume_record
WHERE deleted_flag = 0
UNION ALL
SELECT
    '新表消费记录' AS label,
    COUNT(*) AS count
FROM POSID_CONSUME_RECORD
WHERE deleted_flag = 0;

-- 期望结果：旧表和新表数据量相等

-- ============================================================
-- 切换操作（仅在以上检查全部通过后执行！）
-- ============================================================

-- ⚠️ 重要：只有当所有检查都通过时才能执行以下操作！

-- 1. 修改应用配置（通过配置中心或修改application.yml）
-- consume:
--   write:
--     mode: new  # 从 dual 改为 new

-- 2. 重启消费服务
-- mvn spring-boot:run -Dspring-boot.run.profiles=docker

-- 3. 验证切换后功能（执行测试消费）
-- INSERT INTO POSID_CONSUME_RECORD (...)
-- SELECT COUNT(*) FROM POSID_CONSUME_RECORD WHERE create_time >= NOW() - INTERVAL 1 HOUR;

-- ============================================================
-- 切换后验证（运行1周）
-- ============================================================

-- 4. 每天检查新表数据写入情况
SELECT
    DATE(create_time) AS date,
    COUNT(*) AS new_records_count
FROM POSID_CONSUME_RECORD
WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
GROUP BY DATE(create_time)
ORDER BY date DESC;

-- 期望结果：每天有新数据写入

-- 5. 检查旧表是否还有新数据写入（应该为0）
SELECT
    COUNT(*) AS old_records_count
FROM t_consume_record
WHERE create_time >= DATE_SUB(NOW(), INTERVAL 1 DAY);

-- 期望结果：0（切换后旧表不再有新数据）

-- ============================================================
-- 回滚方案（如果切换后发现严重问题）
-- ============================================================

-- ⚠️ 只有在切换失败或发现严重问题时才执行回滚！

-- 1. 修改应用配置恢复双写模式
-- consume:
--   write:
--     mode: dual  # 从 new 改回 dual

-- 2. 重启消费服务
-- mvn spring-boot:run -Dspring-boot.run.profiles=docker

-- 3. 同步新表数据到旧表（如果有数据不一致）
-- INSERT INTO t_consume_account (...)
-- SELECT ... FROM POSID_ACCOUNT
-- WHERE ...;

-- ============================================================
-- 归档旧表（切换成功并稳定运行1周后）
-- ============================================================

-- ⚠️ 只有在切换成功并稳定运行1周后才能执行！

-- 1. 重命名旧表（归档）
RENAME TABLE t_consume_account TO t_consume_account_backup_20251223;
RENAME TABLE t_consume_record TO t_consume_record_backup_20251223;
RENAME TABLE t_consume_account_transaction TO t_consume_account_transaction_backup_20251223;

-- 2. 清理Flyway历史（可选）
-- DELETE FROM flyway_schema_history WHERE script LIKE '%V20251219%';

-- 3. 验证新表功能正常
SELECT COUNT(*) FROM POSID_CONSUME_RECORD WHERE create_time >= NOW() - INTERVAL 1 HOUR;

-- ============================================================
-- 完成标记
-- ============================================================

-- 执行完成
SELECT 'Switch to new tables script completed' AS status;
