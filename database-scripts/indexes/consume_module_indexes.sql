-- =====================================================
-- 消费模块数据库索引优化脚本
-- 创建日期: 2025-11-17
-- 版本: v1.0
-- 描述: 为消费模块相关表创建性能优化索引
-- =====================================================

-- 1. 消费记录表 (t_consume_record) 索引优化
-- =====================================================

-- 基础查询索引
CREATE INDEX IF NOT EXISTS idx_consume_record_person_id ON t_consume_record(person_id, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_consume_record_device_id ON t_consume_record(device_id, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_consume_record_region_id ON t_consume_record(region_id, deleted_flag);

-- 时间查询优化索引
CREATE INDEX IF NOT EXISTS idx_consume_record_create_time ON t_consume_record(create_time, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_consume_record_consume_date ON t_consume_record(consume_date, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_consume_record_pay_time ON t_consume_record(pay_time, deleted_flag);

-- 状态查询优化索引
CREATE INDEX IF NOT EXISTS idx_consume_record_status ON t_consume_record(status, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_consume_record_consume_mode ON t_consume_record(consumption_mode, deleted_flag);

-- 复合查询索引（报表统计使用）
CREATE INDEX IF NOT EXISTS idx_consume_record_person_time ON t_consume_record(person_id, create_time, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_consume_record_device_time ON t_consume_record(device_id, create_time, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_consume_record_status_time ON t_consume_record(status, create_time, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_consume_record_mode_time ON t_consume_record(consumption_mode, create_time, deleted_flag);

-- 订单查询优化
CREATE INDEX IF NOT EXISTS idx_consume_record_order_no ON t_consume_record(order_no, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_consume_record_transaction_id ON t_consume_record(transaction_id, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_consume_record_third_party_order_no ON t_consume_record(third_party_order_no, deleted_flag);

-- 地理位置查询优化
CREATE INDEX IF NOT EXISTS idx_consume_record_region_name ON t_consume_record(region_name, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_consume_record_client_ip ON t_consume_record(client_ip, deleted_flag);

-- 金额范围查询优化
CREATE INDEX IF NOT EXISTS idx_consume_record_amount ON t_consume_record(amount, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_consume_record_amount_time ON t_consume_record(amount, create_time, deleted_flag);

-- 账户类型查询优化
CREATE INDEX IF NOT EXISTS idx_consume_record_account_type ON t_consume_record(account_type, deleted_flag);

-- 退款相关查询优化
CREATE INDEX IF NOT EXISTS idx_consume_record_original_record ON t_consume_record(original_record_id, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_consume_record_refund_time ON t_consume_record(refund_time, deleted_flag);

-- 2. 账户表 (t_consume_account) 索引优化
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_account_person_id ON t_consume_account(person_id, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_account_account_type ON t_consume_account(account_type, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_account_status ON t_consume_account(status, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_account_create_time ON t_consume_account(create_time, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_account_balance_amount ON t_consume_account(balance_amount, deleted_flag);

-- 3. 账户余额表 (t_consume_account_balance) 索引优化
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_account_balance_account_id ON t_consume_account_balance(account_id, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_account_balance_update_time ON t_consume_account_balance(update_time, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_account_balance_amount ON t_consume_account_balance(balance_amount, deleted_flag);

-- 4. 支付密码表 (t_consume_payment_password) 索引优化
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_payment_password_person_id ON t_consume_payment_password(person_id, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_payment_password_status ON t_consume_payment_password(status, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_payment_password_lock_until ON t_consume_payment_password(lock_until, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_payment_password_expired_time ON t_consume_payment_password(expired_time, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_payment_password_update_time ON t_consume_payment_password(update_time, deleted_flag);

-- 5. 消费限额配置表 (t_consume_limit_config) 索引优化
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_limit_config_target_type ON t_consume_limit_config(target_type, target_id, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_limit_config_priority ON t_consume_limit_config(priority, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_limit_config_enabled ON t_consume_limit_config(enabled, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_limit_config_effective_time ON t_consume_limit_config(effective_time, expire_time, deleted_flag);

-- 6. 设备配置表 (t_consume_device_config) 索引优化
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_device_config_device_id ON t_consume_device_config(device_id, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_device_config_device_type ON t_consume_device_config(device_type, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_device_config_status ON t_consume_device_config(status, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_device_config_region_id ON t_consume_device_config(region_id, deleted_flag);

-- 7. 支付记录表 (t_consume_payment_record) 索引优化
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_payment_record_person_id ON t_consume_payment_record(person_id, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_payment_record_device_id ON t_consume_payment_record(device_id, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_payment_record_payment_time ON t_consume_payment_record(payment_time, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_payment_record_payment_method ON t_consume_payment_record(payment_method, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_payment_record_payment_status ON t_consume_payment_record(payment_status, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_payment_record_order_no ON t_consume_payment_record(order_no, deleted_flag);

-- 8. 退款记录表 (t_consume_refund_record) 索引优化
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_refund_record_person_id ON t_consume_refund_record(person_id, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_refund_record_original_record_id ON t_consume_refund_record(original_record_id, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_refund_record_refund_time ON t_consume_refund_record(refund_time, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_refund_record_refund_status ON t_consume_refund_record(refund_status, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_refund_record_order_no ON t_consume_refund_record(order_no, deleted_flag);

-- 9. 安全通知日志表 (t_consume_security_notification_log) 索引优化
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_security_notification_person_id ON t_consume_security_notification_log(person_id, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_security_notification_type ON t_consume_security_notification_log(notification_type, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_security_notification_channel ON t_consume_security_notification_log(notification_channel, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_security_notification_send_time ON t_consume_security_notification_log(send_time, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_security_notification_status ON t_consume_security_notification_log(send_status, deleted_flag);

-- 10. 用户通知偏好表 (t_consume_user_notification_preference) 索引优化
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_user_notification_preference_person_id ON t_consume_user_notification_preference(person_id, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_user_notification_preference_channel ON t_consume_user_notification_preference(notification_channel, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_user_notification_preference_enabled ON t_consume_user_notification_preference(is_enabled, deleted_flag);

-- 11. 异常检测规则表 (t_consume_abnormal_detection_rule) 索引优化
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_abnormal_rule_rule_type ON t_consume_abnormal_detection_rule(rule_type, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_abnormal_rule_priority ON t_consume_abnormal_detection_rule(priority, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_abnormal_rule_enabled ON t_consume_abnormal_detection_rule(is_enabled, deleted_flag);

-- 12. 用户行为基线表 (t_consume_user_behavior_baseline) 索引优化
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_behavior_baseline_person_id ON t_consume_user_behavior_baseline(person_id, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_behavior_baseline_update_time ON t_consume_user_behavior_baseline(update_time, deleted_flag);

-- 13. 异常操作日志表 (t_consume_abnormal_operation_log) 索引优化
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_abnormal_log_person_id ON t_consume_abnormal_operation_log(person_id, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_abnormal_log_device_id ON t_consume_abnormal_operation_log(device_id, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_abnormal_log_operation_time ON t_consume_abnormal_operation_log(operation_time, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_abnormal_log_risk_level ON t_consume_abnormal_operation_log(risk_level, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_abnormal_log_anomaly_type ON t_consume_abnormal_operation_log(anomaly_type, deleted_flag);

-- =====================================================
-- 分区表建议（针对大数据量场景）
-- =====================================================

-- 建议对消费记录表按时间进行分区，提高查询性能
-- 消费记录表分区建议（按月分区）
--
-- ALTER TABLE t_consume_record PARTITION BY RANGE (YEAR(create_time)*100 + MONTH(create_time)) (
--     PARTITION p202501 VALUES LESS THAN (202502),
--     PARTITION p202502 VALUES LESS THAN (202503),
--     PARTITION p202503 VALUES LESS THAN (202504),
--     PARTITION p202504 VALUES LESS THAN (202505),
--     PARTITION p202505 VALUES LESS THAN (202506),
--     PARTITION p202506 VALUES LESS THAN (202507),
--     PARTITION p202507 VALUES LESS THAN (202508),
--     PARTITION p202508 VALUES LESS THAN (202509),
--     PARTITION p202509 VALUES LESS THAN (202510),
--     PARTITION p202510 VALUES LESS THAN (202511),
--     PARTITION p202511 VALUES LESS THAN (202512),
--     PARTITION p202512 VALUES LESS THAN (202601),
--     PARTITION p_future VALUES LESS THAN MAXVALUE
-- );

-- =====================================================
-- 性能监控查询
-- =====================================================

-- 查看索引使用情况
-- SELECT
--     table_name,
--     index_name,
--     cardinality,
--     sub_part,
--     packed,
--     nullable,
--     index_type
-- FROM information_schema.statistics
-- WHERE table_schema = DATABASE()
-- AND table_name LIKE 't_consume_%'
-- ORDER BY table_name, index_name;

-- 分析表统计信息
-- ANALYZE TABLE t_consume_record;
-- ANALYZE TABLE t_consume_account;
-- ANALYZE TABLE t_consume_payment_password;

-- 查看慢查询（建议启用慢查询日志）
-- SET GLOBAL slow_query_log = 'ON';
-- SET GLOBAL long_query_time = 1;

-- =====================================================
-- 索引维护建议
-- =====================================================

-- 1. 定期分析表统计信息
--    - 每周执行一次 ANALYZE TABLE 更新统计信息
--    - 在大批量数据导入后立即执行

-- 2. 监控索引使用情况
--    - 定期检查未使用的索引，考虑删除以节省空间
--    - 监控查询性能，发现性能瓶颈及时优化

-- 3. 定期优化表
--    - 每月执行一次 OPTIMIZE TABLE 整理碎片
--    - 在大量删除操作后执行

-- 4. 备份策略
--    - 在创建新索引前备份数据
--    - 记录所有索引变更，便于回滚

-- =====================================================
-- 执行完成确认
-- =====================================================

-- 执行完成后，请运行以下SQL确认索引创建情况：
SELECT
    COUNT(*) as created_indexes,
    table_name,
    GROUP_CONCAT(index_name ORDER BY index_name) as indexes
FROM information_schema.statistics
WHERE table_schema = DATABASE()
AND table_name LIKE 't_consume_%'
AND index_name LIKE 'idx_%'
GROUP BY table_name
ORDER BY table_name;

-- 脚本执行完成！