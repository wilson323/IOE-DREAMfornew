-- ============================================
-- IOE-DREAM 消费模块数据库索引优化SQL
-- 版本: 1.0.0
-- 日期: 2025-01-30
-- 说明: 基于性能设计文档15-消费流水数据准确性与性能设计.md
-- ============================================

-- ============================================
-- 1. consume_record 表索引优化
-- ============================================

-- 1.1 主键索引（已存在，无需创建）
-- PRIMARY KEY (id)

-- 1.2 唯一索引：交易流水号
CREATE UNIQUE INDEX IF NOT EXISTS `uk_consume_record_transaction_no`
ON `consume_record` (`transaction_no`);

-- 1.3 组合索引：设备ID + 时间范围（高频查询）
-- 用于：getRealtimeStatistics方法中的区域消费记录查询
CREATE INDEX IF NOT EXISTS `idx_consume_record_device_time`
ON `consume_record` (`device_id`, `consume_time`, `deleted`);

-- 1.4 组合索引：用户ID + 时间范围（用户消费历史查询）
CREATE INDEX IF NOT EXISTS `idx_consume_record_user_time`
ON `consume_record` (`user_id`, `consume_time`, `deleted`);

-- 1.5 组合索引：账户ID + 时间范围（账户对账查询）
CREATE INDEX IF NOT EXISTS `idx_consume_record_account_time`
ON `consume_record` (`account_id`, `consume_time`, `deleted`);

-- 1.6 组合索引：区域ID + 时间范围（区域统计查询）
CREATE INDEX IF NOT EXISTS `idx_consume_record_area_time`
ON `consume_record` (`area_id`, `consume_time`, `deleted`);

-- 1.7 覆盖索引：时间范围 + 状态 + 金额（统计查询优化）
-- 用于：报表生成、统计查询，避免回表
CREATE INDEX IF NOT EXISTS `idx_consume_record_time_status_amount`
ON `consume_record` (`consume_time`, `status`, `amount`, `deleted`);

-- 1.8 组合索引：设备ID列表 + 时间范围（区域设备查询优化）
-- 用于：selectByDeviceIdsAndTimeRange方法
CREATE INDEX IF NOT EXISTS `idx_consume_record_device_time_deleted`
ON `consume_record` (`device_id`, `consume_time`, `deleted`);

-- ============================================
-- 2. consume_transaction 表索引优化
-- ============================================

-- 2.1 主键索引（已存在，无需创建）
-- PRIMARY KEY (id)

-- 2.2 唯一索引：交易流水号
CREATE UNIQUE INDEX IF NOT EXISTS `uk_consume_transaction_no`
ON `consume_transaction` (`transaction_no`);

-- 2.3 组合索引：用户ID + 交易状态 + 时间范围（用户交易查询）
CREATE INDEX IF NOT EXISTS `idx_consume_transaction_user_status_time`
ON `consume_transaction` (`user_id`, `transaction_status`, `transaction_time`, `deleted_flag`);

-- 2.4 组合索引：账户ID + 交易状态 + 时间范围（账户交易查询）
CREATE INDEX IF NOT EXISTS `idx_consume_transaction_account_status_time`
ON `consume_transaction` (`account_id`, `transaction_status`, `transaction_time`, `deleted_flag`);

-- 2.5 组合索引：区域ID + 交易状态 + 时间范围（区域交易查询）
CREATE INDEX IF NOT EXISTS `idx_consume_transaction_area_status_time`
ON `consume_transaction` (`area_id`, `transaction_status`, `transaction_time`, `deleted_flag`);

-- 2.6 组合索引：设备ID + 交易状态 + 时间范围（设备交易查询）
CREATE INDEX IF NOT EXISTS `idx_consume_transaction_device_status_time`
ON `consume_transaction` (`device_id`, `transaction_status`, `transaction_time`, `deleted_flag`);

-- 2.7 覆盖索引：时间范围 + 状态 + 金额（统计查询优化）
-- 用于：报表生成、对账查询，避免回表
CREATE INDEX IF NOT EXISTS `idx_consume_transaction_time_status_amount`
ON `consume_transaction` (`transaction_time`, `transaction_status`, `amount`, `deleted_flag`);

-- 2.8 组合索引：用户ID + 时间段 + 金额（推荐服务历史消费金额查询）
-- 用于：ConsumeRecommendService.loadUserHistoricalAmounts方法
CREATE INDEX IF NOT EXISTS `idx_consume_transaction_user_time_amount`
ON `consume_transaction` (`user_id`, `transaction_time`, `amount`, `transaction_status`);

-- 2.9 组合索引：商品ID + 时间范围（商品销售统计）
-- 用于：商品销售排行榜、商品热度统计
CREATE INDEX IF NOT EXISTS `idx_consume_transaction_product_time`
ON `consume_transaction` (`product_id`, `transaction_time`, `transaction_status`, `deleted_flag`);

-- ============================================
-- 3. account 表索引优化（账户对账相关）
-- ============================================

-- 3.1 组合索引：账户ID + 版本号（乐观锁优化）
CREATE INDEX IF NOT EXISTS `idx_account_id_version`
ON `account` (`account_id`, `version`);

-- 3.2 组合索引：用户ID + 账户状态（用户账户查询）
CREATE INDEX IF NOT EXISTS `idx_account_user_status`
ON `account` (`person_id`, `status`, `deleted_flag`);

-- 3.3 组合索引：账户类别ID + 状态（账户类别统计）
CREATE INDEX IF NOT EXISTS `idx_account_kind_status`
ON `account` (`account_kind_id`, `status`, `deleted_flag`);

-- ============================================
-- 4. recharge_record 表索引优化（充值对账相关）
-- ============================================

-- 4.1 唯一索引：充值订单号
CREATE UNIQUE INDEX IF NOT EXISTS `uk_recharge_record_order_no`
ON `recharge_record` (`order_no`);

-- 4.2 组合索引：账户ID + 充值状态 + 时间范围（账户充值查询）
CREATE INDEX IF NOT EXISTS `idx_recharge_record_account_status_time`
ON `recharge_record` (`account_id`, `recharge_status`, `create_time`, `deleted_flag`);

-- 4.3 组合索引：用户ID + 充值状态 + 时间范围（用户充值查询）
CREATE INDEX IF NOT EXISTS `idx_recharge_record_user_status_time`
ON `recharge_record` (`user_id`, `recharge_status`, `create_time`, `deleted_flag`);

-- ============================================
-- 5. 性能优化说明
-- ============================================

-- 5.1 索引使用原则
-- - 查询条件必须包含索引的第一列
-- - 避免在索引列上使用函数或表达式
-- - 合理使用覆盖索引，避免回表查询
-- - 定期分析慢查询日志，优化索引

-- 5.2 索引维护
-- - 定期执行 ANALYZE TABLE 更新索引统计信息
-- - 监控索引使用情况，删除未使用的索引
-- - 对于大表，考虑使用分区表 + 分区索引

-- 5.3 查询优化建议
-- - 查询时尽量使用索引列作为WHERE条件
-- - 避免SELECT *，只查询需要的字段
-- - 使用LIMIT限制返回结果数量
-- - 对于时间范围查询，使用分区键（如果启用分区）

-- ============================================
-- 6. 索引创建验证
-- ============================================

-- 验证索引是否创建成功
-- SELECT
--     TABLE_NAME,
--     INDEX_NAME,
--     COLUMN_NAME,
--     SEQ_IN_INDEX,
--     CARDINALITY
-- FROM
--     INFORMATION_SCHEMA.STATISTICS
-- WHERE
--     TABLE_SCHEMA = DATABASE()
--     AND TABLE_NAME IN ('consume_record', 'consume_transaction', 'account', 'recharge_record')
-- ORDER BY
--     TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX;
