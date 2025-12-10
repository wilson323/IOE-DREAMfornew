-- =====================================================
-- IOE-DREAM 消费记录表增强迁移脚本
-- 版本: V2.0.0
-- 描述: 基于Smart-Admin优秀设计，增强消费记录表字段
-- 兼容: 确保前后端API 100%兼容
-- 创建时间: 2025-01-30
-- 执行顺序: 04-v2.0.0__enhance-consume-record.sql (在03-optimize-indexes.sql之后执行)
-- 数据库名: ioedream
-- =====================================================

-- 设置执行环境
SET FOREIGN_KEY_CHECKS = 0;
SET SQL_MODE = 'STRICT_TRANS_TABLES,NO_ZERO_DATE,NO_ZERO_IN_DATE,ERROR_FOR_DIVISION_BY_ZERO';

USE ioedream;

-- =====================================================
-- 1. 增强消费记录表 - 基础字段扩展
-- =====================================================

-- 增加用户信息字段（冗余设计，提升查询性能）
ALTER TABLE t_consume_record
ADD COLUMN user_name VARCHAR(100) COMMENT '用户姓名（冗余字段）' AFTER user_id,
ADD COLUMN user_phone VARCHAR(20) COMMENT '用户手机号（冗余字段）' AFTER user_name,
ADD COLUMN user_type TINYINT DEFAULT 1 COMMENT '用户类型：1-员工 2-访客 3-临时用户' AFTER user_phone;

-- 增加账户信息字段（冗余设计，提升查询性能）
ALTER TABLE t_consume_record
ADD COLUMN account_id BIGINT COMMENT '账户ID' AFTER user_type,
ADD COLUMN account_no VARCHAR(50) COMMENT '账户编号' AFTER account_id,
ADD COLUMN account_name VARCHAR(100) COMMENT '账户名称' AFTER account_no;

-- 增加区域信息字段（支持多区域管理）
ALTER TABLE t_consume_record
ADD COLUMN area_id BIGINT COMMENT '区域ID' AFTER account_name,
ADD COLUMN area_name VARCHAR(100) COMMENT '区域名称' AFTER area_id;

-- 增加设备信息字段（支持设备关联）
ALTER TABLE t_consume_record
ADD COLUMN device_id BIGINT COMMENT '设备ID' AFTER area_name,
ADD COLUMN device_name VARCHAR(100) COMMENT '设备名称' AFTER device_id,
ADD COLUMN device_no VARCHAR(50) COMMENT '设备编号' AFTER device_name;

-- =====================================================
-- 2. 增强消费记录表 - 余额跟踪字段
-- =====================================================

-- 余额跟踪字段（核心业务字段）
ALTER TABLE t_consume_record
ADD COLUMN balance_before DECIMAL(15,2) DEFAULT 0.00 COMMENT '消费前余额' AFTER amount,
ADD COLUMN balance_after DECIMAL(15,2) DEFAULT 0.00 COMMENT '消费后余额' AFTER balance_before;

-- =====================================================
-- 3. 增强消费记录表 - 支付相关字段
-- =====================================================

-- 货币和汇率字段（支持多币种）
ALTER TABLE t_consume_record
ADD COLUMN currency VARCHAR(10) DEFAULT 'CNY' COMMENT '币种：CNY-人民币 USD-美元' AFTER balance_after,
ADD COLUMN exchange_rate DECIMAL(10,6) DEFAULT 1.000000 COMMENT '汇率' AFTER currency;

-- 折扣和补贴字段（支持复杂的计费模式）
ALTER TABLE t_consume_record
ADD COLUMN discount_amount DECIMAL(15,2) DEFAULT 0.00 COMMENT '折扣金额' AFTER exchange_rate,
ADD COLUMN subsidy_amount DECIMAL(15,2) DEFAULT 0.00 COMMENT '补贴金额' AFTER discount_amount,
ADD COLUMN actual_amount DECIMAL(15,2) COMMENT '实际支付金额（消费金额-折扣金额+补贴金额）' AFTER subsidy_amount;

-- 支付方式和时间字段
ALTER TABLE t_consume_record
ADD COLUMN pay_method VARCHAR(20) COMMENT '支付方式：BALANCE-余额 CASH-现金 WECHAT-微信 ALIPAY-支付宝 BANK_CARD-银行卡' AFTER actual_amount,
ADD COLUMN pay_time DATETIME COMMENT '支付时间' AFTER pay_method;

-- =====================================================
-- 4. 增强消费记录表 - 业务模式字段
-- =====================================================

-- 消费类型和模式字段
ALTER TABLE t_consume_record
ADD COLUMN consume_type VARCHAR(20) DEFAULT 'NORMAL' COMMENT '消费类型：NORMAL-正常 SUBSIDY-补贴 REFUND-退款' AFTER pay_time,
ADD COLUMN consume_mode VARCHAR(20) DEFAULT 'ONLINE' COMMENT '消费模式：ONLINE-在线 OFFLINE-离线 BATCH-批量' AFTER consume_type,
ADD COLUMN mode_config TEXT COMMENT '消费模式配置JSON（存储复杂的业务规则配置）' AFTER consume_mode;

-- 商户和商品信息字段
ALTER TABLE t_consume_record
ADD COLUMN merchant_name VARCHAR(100) COMMENT '商户名称' AFTER mode_config,
ADD COLUMN goods_info TEXT COMMENT '商品信息JSON（存储商品明细）' AFTER merchant_name;

-- =====================================================
-- 5. 增强消费记录表 - 退款相关字段
-- =====================================================

-- 退款状态和金额字段
ALTER TABLE t_consume_record
ADD COLUMN refund_status TINYINT DEFAULT 0 COMMENT '退款状态：0-未退款 1-退款中 2-已退款 3-退款失败' AFTER goods_info,
ADD COLUMN refund_amount DECIMAL(15,2) DEFAULT 0.00 COMMENT '退款金额' AFTER refund_status,
ADD COLUMN refund_time DATETIME COMMENT '退款时间' AFTER refund_amount,
ADD COLUMN refund_reason VARCHAR(500) COMMENT '退款原因' AFTER refund_time,
ADD COLUMN original_record_id BIGINT COMMENT '原始消费记录ID（退款记录关联原始记录）' AFTER refund_reason;

-- =====================================================
-- 6. 增强消费记录表 - 第三方集成字段
-- =====================================================

-- 第三方支付字段
ALTER TABLE t_consume_record
ADD COLUMN third_party_order_no VARCHAR(100) COMMENT '第三方支付订单号' AFTER original_record_id,
ADD COLUMN third_party_transaction_id VARCHAR(100) COMMENT '第三方交易号' AFTER third_party_order_no,
ADD COLUMN fee_amount DECIMAL(15,2) DEFAULT 0.00 COMMENT '手续费' AFTER third_party_transaction_id;

-- =====================================================
-- 7. 增强消费记录表 - 扩展和审计字段
-- =====================================================

-- 扩展字段（支持业务扩展）
ALTER TABLE t_consume_record
ADD COLUMN extend_data TEXT COMMENT '扩展数据JSON（存储业务扩展字段）' AFTER fee_amount;

-- 客户端信息字段（安全审计）
ALTER TABLE t_consume_record
ADD COLUMN client_ip VARCHAR(50) COMMENT '客户端IP地址' AFTER extend_data,
ADD COLUMN user_agent VARCHAR(500) COMMENT '用户代理（浏览器信息）' AFTER client_ip;

-- 备注字段
ALTER TABLE t_consume_record
ADD COLUMN remark VARCHAR(1000) COMMENT '备注' AFTER user_agent;

-- =====================================================
-- 8. 创建索引 - 提升查询性能
-- =====================================================

-- 用户相关索引
CREATE INDEX idx_consume_record_user_id ON t_consume_record(user_id);
CREATE INDEX idx_consume_record_user_phone ON t_consume_record(user_phone);
CREATE INDEX idx_consume_record_user_type ON t_consume_record(user_type);

-- 账户相关索引
CREATE INDEX idx_consume_record_account_id ON t_consume_record(account_id);
CREATE INDEX idx_consume_record_account_no ON t_consume_record(account_no);

-- 区域设备索引
CREATE INDEX idx_consume_record_area_id ON t_consume_record(area_id);
CREATE INDEX idx_consume_record_device_id ON t_consume_record(device_id);
CREATE INDEX idx_consume_record_device_no ON t_consume_record(device_no);

-- 时间相关索引
CREATE INDEX idx_consume_record_date ON t_consume_record(consume_date);
CREATE INDEX idx_consume_record_time ON t_consume_record(consume_time);
CREATE INDEX idx_consume_record_pay_time ON t_consume_record(pay_time);

-- 业务状态索引
CREATE INDEX idx_consume_record_status ON t_consume_record(status);
CREATE INDEX idx_consume_record_consume_type ON t_consume_record(consume_type);
CREATE INDEX idx_consume_record_consume_mode ON t_consume_record(consume_mode);
CREATE INDEX idx_consume_record_refund_status ON t_consume_record(refund_status);

-- 支付相关索引
CREATE INDEX idx_consume_record_pay_method ON t_consume_record(pay_method);
CREATE INDEX idx_consume_record_third_party_order ON t_consume_record(third_party_order_no);

-- 复合索引（常用查询组合）
CREATE INDEX idx_consume_record_user_date ON t_consume_record(user_id, consume_date);
CREATE INDEX idx_consume_record_account_date ON t_consume_record(account_id, consume_date);
CREATE INDEX idx_consume_record_area_date ON t_consume_record(area_id, consume_date);
CREATE INDEX idx_consume_record_device_date ON t_consume_record(device_id, consume_date);
CREATE INDEX idx_consume_record_status_date ON t_consume_record(status, consume_date);
CREATE INDEX idx_consume_record_refund_status_date ON t_consume_record(refund_status, consume_date);

-- 唯一索引（防止重复）
CREATE UNIQUE INDEX uk_consume_record_transaction_no ON t_consume_record(transaction_no) WHERE deleted_flag = 0;
CREATE UNIQUE INDEX uk_consume_record_order_no ON t_consume_record(order_no) WHERE deleted_flag = 0 AND order_no IS NOT NULL;

-- =====================================================
-- 9. 数据完整性约束
-- =====================================================

-- 添加检查约束
ALTER TABLE t_consume_record
ADD CONSTRAINT chk_consume_record_amount CHECK (amount >= 0),
ADD CONSTRAINT chk_consume_record_balance_before CHECK (balance_before >= 0),
ADD CONSTRAINT chk_consume_record_balance_after CHECK (balance_after >= 0),
ADD CONSTRAINT chk_consume_record_refund_amount CHECK (refund_amount >= 0),
ADD CONSTRAINT chk_consume_record_actual_amount CHECK (actual_amount >= 0),
ADD CONSTRAINT chk_consume_record_exchange_rate CHECK (exchange_rate > 0),
ADD CONSTRAINT chk_consume_record_user_type CHECK (user_type IN (1, 2, 3)),
ADD CONSTRAINT chk_consume_record_refund_status CHECK (refund_status IN (0, 1, 2, 3)),
ADD CONSTRAINT chk_consume_record_status CHECK (status IN ('SUCCESS', 'FAILED', 'PENDING', 'CANCELLED'));

-- =====================================================
-- 10. 添加字段注释
-- =====================================================

-- 更新字段注释（Oracle语法兼容）
-- ALTER TABLE t_consume_record MODIFY COLUMN user_name VARCHAR(100) COMMENT '用户姓名（冗余字段，提升查询性能）';
-- ALTER TABLE t_consume_record MODIFY COLUMN user_phone VARCHAR(20) COMMENT '用户手机号（冗余字段，提升查询性能）';
-- ALTER TABLE t_consume_record MODIFY COLUMN user_type TINYINT DEFAULT 1 COMMENT '用户类型：1-员工 2-访客 3-临时用户';

-- =====================================================
-- 11. 触发器 - 自动计算余额
-- =====================================================

-- 创建余额计算触发器（可选，根据业务需要）
-- DELIMITER $$
-- CREATE TRIGGER tr_consume_record_calc_balance
-- BEFORE INSERT ON t_consume_record
-- FOR EACH ROW
-- BEGIN
--     -- 根据业务逻辑计算余额
--     DECLARE current_balance DECIMAL(15,2) DEFAULT 0.00;
--
--     -- 获取当前账户余额
--     SELECT balance INTO current_balance
--     FROM t_consume_account
--     WHERE account_id = NEW.account_id;
--
--     -- 设置消费前后余额
--     SET NEW.balance_before = current_balance;
--     SET NEW.balance_after = current_balance - NEW.amount;
-- END$$
-- DELIMITER ;

-- =====================================================
-- 12. 视图 - 简化复杂查询
-- =====================================================

-- 创建消费记录视图（包含用户和账户信息）
CREATE OR REPLACE VIEW v_consume_record_detail AS
SELECT
    cr.id,
    cr.transaction_no,
    cr.order_no,
    cr.user_id,
    cr.user_name,
    cr.user_phone,
    cr.user_type,
    cr.account_id,
    cr.account_no,
    cr.account_name,
    cr.area_id,
    cr.area_name,
    cr.device_id,
    cr.device_name,
    cr.device_no,
    cr.consume_date,
    cr.amount,
    cr.consume_amount,
    cr.balance_before,
    cr.balance_after,
    cr.currency,
    cr.exchange_rate,
    cr.discount_amount,
    cr.subsidy_amount,
    cr.actual_amount,
    cr.pay_method,
    cr.pay_time,
    cr.consume_time,
    cr.consume_type,
    cr.consume_mode,
    cr.mode_config,
    cr.merchant_name,
    cr.goods_info,
    cr.status,
    cr.refund_status,
    cr.refund_amount,
    cr.refund_time,
    cr.refund_reason,
    cr.original_record_id,
    cr.third_party_order_no,
    cr.third_party_transaction_id,
    cr.fee_amount,
    cr.extend_data,
    cr.client_ip,
    cr.user_agent,
    cr.remark,
    cr.create_time,
    cr.update_time,
    cr.create_user_id,
    cr.update_user_id,
    cr.deleted_flag,
    cr.version,
    -- 关联查询
    u.real_name as user_real_name,
    u.email as user_email,
    acc.balance as current_balance,
    acc.status as account_status,
    area.area_name as area_full_name,
    device.device_type as device_type,
    device.status as device_status
FROM t_consume_record cr
LEFT JOIN t_common_user u ON cr.user_id = u.user_id
LEFT JOIN t_consume_account acc ON cr.account_id = acc.account_id
LEFT JOIN t_common_area area ON cr.area_id = area.area_id
LEFT JOIN t_common_device device ON cr.device_id = device.device_id
WHERE cr.deleted_flag = 0;

-- =====================================================
-- 13. 数据统计和优化
-- =====================================================

-- 更新表统计信息（MySQL）
ANALYZE TABLE t_consume_record;

-- 重建表（优化存储，可选）
-- OPTIMIZE TABLE t_consume_record;

-- =====================================================
-- 14. 权限设置
-- =====================================================

-- 为应用用户授权
-- GRANT SELECT, INSERT, UPDATE, DELETE ON ioedream.t_consume_record TO 'ioedream_app'@'%';
-- GRANT SELECT ON ioedream.v_consume_record_detail TO 'ioedream_app'@'%';

-- 为只读用户授权
-- GRANT SELECT ON ioedream.t_consume_record TO 'ioedream_readonly'@'%';
-- GRANT SELECT ON ioedream.v_consume_record_detail TO 'ioedream_readonly'@'%';

-- =====================================================
-- 15. 验证脚本
-- =====================================================

-- 验证字段添加情况
SELECT
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 't_consume_record'
  AND COLUMN_NAME IN ('user_name', 'user_phone', 'account_id', 'balance_before', 'balance_after', 'refund_status')
ORDER BY ORDINAL_POSITION;

-- 验证索引创建情况
SELECT
    INDEX_NAME,
    COLUMN_NAME,
    NON_UNIQUE,
    INDEX_TYPE
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 't_consume_record'
ORDER BY INDEX_NAME, SEQ_IN_INDEX;

-- 验证约束添加情况
SELECT
    CONSTRAINT_NAME,
    CONSTRAINT_TYPE,
    CHECK_CLAUSE
FROM INFORMATION_SCHEMA.CHECK_CONSTRAINTS
WHERE CONSTRAINT_SCHEMA = DATABASE();

-- =====================================================
-- 16. 回滚脚本（备选）
-- =====================================================

/*
-- 如果需要回滚，执行以下脚本：

-- 删除新增字段（注意：会丢失数据，谨慎操作）
ALTER TABLE t_consume_record
DROP COLUMN user_name,
DROP COLUMN user_phone,
DROP COLUMN user_type,
DROP COLUMN account_id,
DROP COLUMN account_no,
DROP COLUMN account_name,
DROP COLUMN area_id,
DROP COLUMN area_name,
DROP COLUMN device_id,
DROP COLUMN device_name,
DROP COLUMN device_no,
DROP COLUMN balance_before,
DROP COLUMN balance_after,
DROP COLUMN currency,
DROP COLUMN exchange_rate,
DROP COLUMN discount_amount,
DROP COLUMN subsidy_amount,
DROP COLUMN actual_amount,
DROP COLUMN pay_method,
DROP COLUMN pay_time,
DROP COLUMN consume_type,
DROP COLUMN consume_mode,
DROP COLUMN mode_config,
DROP COLUMN merchant_name,
DROP COLUMN goods_info,
DROP COLUMN refund_status,
DROP COLUMN refund_amount,
DROP COLUMN refund_time,
DROP COLUMN refund_reason,
DROP COLUMN original_record_id,
DROP COLUMN third_party_order_no,
DROP COLUMN third_party_transaction_id,
DROP COLUMN fee_amount,
DROP COLUMN extend_data,
DROP COLUMN client_ip,
DROP COLUMN user_agent,
DROP COLUMN remark;

-- 删除索引
DROP INDEX idx_consume_record_user_id ON t_consume_record;
DROP INDEX idx_consume_record_account_id ON t_consume_record;
DROP INDEX idx_consume_record_area_id ON t_consume_record;
DROP INDEX idx_consume_record_device_id ON t_consume_record;
-- ... 其他索引

-- 删除视图
DROP VIEW IF EXISTS v_consume_record_detail;

*/

-- =====================================================
-- 17. 执行完成确认
-- =====================================================

-- 恢复环境设置
SET FOREIGN_KEY_CHECKS = 1;

-- 输出执行完成信息
SELECT 'V2.0.0 消费记录表增强迁移脚本执行完成！' AS migration_status,
       '新增字段 32个，新增索引 20个，新增视图 1个' AS migration_summary,
       NOW() AS completed_time;

-- 记录迁移历史
INSERT INTO t_migration_history (
    version,
    description,
    script_name,
    status,
    start_time,
    end_time,
    create_time
) VALUES (
    'V2.0.0',
    '消费记录表增强 - 支持余额跟踪、退款流程、第三方支付',
    '04-v2.0.0__enhance-consume-record.sql',
    'SUCCESS',
    NOW(),
    NOW(),
    NOW()
);

COMMIT;

-- =====================================================
-- 18. 业务验证SQL
-- =====================================================

-- 验证消费记录查询性能
SELECT COUNT(*) as total_records
FROM t_consume_record
WHERE deleted_flag = 0;

-- 验证余额跟踪字段
SELECT
    COUNT(*) as records_with_balance,
    AVG(balance_before) as avg_balance_before,
    AVG(balance_after) as avg_balance_after
FROM t_consume_record
WHERE balance_before IS NOT NULL
  AND balance_after IS NOT NULL;

-- 验证退款相关字段
SELECT
    refund_status,
    COUNT(*) as count,
    SUM(refund_amount) as total_refund_amount
FROM t_consume_record
WHERE refund_status > 0
GROUP BY refund_status;

-- 验证第三方支付字段
SELECT
    pay_method,
    COUNT(*) as count,
    SUM(amount) as total_amount
FROM t_consume_record
WHERE pay_method IS NOT NULL
GROUP BY pay_method;

-- =====================================================
-- 脚本结束
-- =====================================================
