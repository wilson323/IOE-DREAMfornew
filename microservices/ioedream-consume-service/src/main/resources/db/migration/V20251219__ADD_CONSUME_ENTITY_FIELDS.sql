-- =====================================================
-- IOE-DREAM 消费模块实体字段补齐迁移脚本
-- 版本: V20251219
-- 描述: 根据chonggou.txt和业务模块文档要求，补齐实体类缺失字段
-- 创建时间: 2025-12-19
-- 数据库名: ioedream
-- =====================================================
SET FOREIGN_KEY_CHECKS = 0;
SET SQL_MODE = 'STRICT_TRANS_TABLES,NO_ZERO_DATE,NO_ZERO_IN_DATE,ERROR_FOR_DIVISION_BY_ZERO';
USE ioedream;
-- =====================================================
-- 1. 增强消费记录表 (t_consume_record)
-- =====================================================
-- 添加支付状态和支付方式字段（根据chonggou.txt要求）
ALTER TABLE t_consume_record
ADD COLUMN IF NOT EXISTS payment_status INT DEFAULT 1 COMMENT '支付状态：1-待支付 2-支付中 3-支付成功 4-支付失败 5-已退款 6-部分退款'
AFTER status,
    ADD COLUMN IF NOT EXISTS payment_method INT DEFAULT 1 COMMENT '支付方式：1-余额支付 2-微信支付 3-支付宝 4-银行卡 5-现金 6-二维码 7-NFC 8-生物识别'
AFTER payment_status;
-- 添加索引优化查询性能
CREATE INDEX IF NOT EXISTS idx_consume_record_payment_status ON t_consume_record(payment_status, consume_time);
CREATE INDEX IF NOT EXISTS idx_consume_record_payment_method ON t_consume_record(payment_method, consume_time);
-- =====================================================
-- 2. 增强支付记录表 (t_consume_payment_record)
-- =====================================================
-- 添加手续费字段（根据chonggou.txt要求）
ALTER TABLE t_consume_payment_record
ADD COLUMN IF NOT EXISTS fee_amount DECIMAL(15, 2) DEFAULT 0.00 COMMENT '手续费金额'
AFTER refund_amount;
-- 添加索引
CREATE INDEX IF NOT EXISTS idx_payment_record_fee ON t_consume_payment_record(fee_amount, payment_status);
-- =====================================================
-- 3. 增强支付退款记录表 (t_payment_refund_record)
-- =====================================================
-- 添加缺失字段（根据chonggou.txt要求）
ALTER TABLE t_payment_refund_record
ADD COLUMN IF NOT EXISTS refund_transaction_no VARCHAR(64) COMMENT '退款交易流水号'
AFTER refund_no,
    ADD COLUMN IF NOT EXISTS apply_time DATETIME COMMENT '申请时间'
AFTER refund_time,
    ADD COLUMN IF NOT EXISTS actual_refund_amount DECIMAL(15, 2) COMMENT '实际退款金额'
AFTER refund_amount,
    ADD COLUMN IF NOT EXISTS refund_type INT DEFAULT 1 COMMENT '退款类型：1-全额退款 2-部分退款 3-自动退款 4-手动退款'
AFTER actual_refund_amount;
-- 添加索引
CREATE INDEX IF NOT EXISTS idx_refund_record_transaction_no ON t_payment_refund_record(refund_transaction_no);
CREATE INDEX IF NOT EXISTS idx_refund_record_type ON t_payment_refund_record(refund_type, refund_status);
-- =====================================================
-- 4. 增强二维码表 (t_qr_code)
-- =====================================================
-- 添加业务模块字段（如果不存在）
ALTER TABLE t_qr_code
ADD COLUMN IF NOT EXISTS business_module VARCHAR(50) COMMENT '业务模块标识：consume/access/attendance/visitor'
AFTER qr_type,
    ADD COLUMN IF NOT EXISTS effective_time DATETIME COMMENT '生效时间'
AFTER valid_time,
    ADD COLUMN IF NOT EXISTS amount_limit DECIMAL(15, 2) COMMENT '金额限制'
AFTER usage_limit,
    ADD COLUMN IF NOT EXISTS require_biometric INT DEFAULT 0 COMMENT '是否需要生物识别：0-不需要 1-需要'
AFTER security_level,
    ADD COLUMN IF NOT EXISTS require_location INT DEFAULT 0 COMMENT '是否需要位置验证：0-不需要 1-需要'
AFTER require_biometric,
    ADD COLUMN IF NOT EXISTS qr_status INT DEFAULT 1 COMMENT '二维码状态：1-有效 2-已使用 3-已过期 4-已禁用'
AFTER status,
    ADD COLUMN IF NOT EXISTS qr_content TEXT COMMENT '二维码内容'
AFTER qr_code,
    ADD COLUMN IF NOT EXISTS extended_attributes TEXT COMMENT '扩展属性JSON'
AFTER qr_content,
    ADD COLUMN IF NOT EXISTS create_method INT DEFAULT 1 COMMENT '创建方式：1-系统生成 2-手动创建 3-批量导入'
AFTER extended_attributes,
    ADD COLUMN IF NOT EXISTS generate_reason VARCHAR(255) COMMENT '生成原因'
AFTER create_method;
-- 修改字段类型（如果字段已存在但类型不匹配）
-- require_biometric, require_location, security_level, create_method 应该是INT类型
-- 如果已经是INT类型则不需要修改
-- 添加索引
CREATE INDEX IF NOT EXISTS idx_qr_code_business_module ON t_qr_code(business_module, qr_status);
CREATE INDEX IF NOT EXISTS idx_qr_code_effective_time ON t_qr_code(effective_time, expire_time);
CREATE INDEX IF NOT EXISTS idx_qr_code_area_device ON t_qr_code(area_id, device_id);
-- =====================================================
-- 5. 增强消费产品表 (t_consume_product)
-- =====================================================
-- 添加缺失字段（根据chonggou.txt要求）
ALTER TABLE t_consume_product
ADD COLUMN IF NOT EXISTS available INT DEFAULT 1 COMMENT '是否可用：0-不可用 1-可用'
AFTER status,
    ADD COLUMN IF NOT EXISTS area_ids TEXT COMMENT '关联区域ID列表（JSON格式）'
AFTER available;
-- 添加索引
CREATE INDEX IF NOT EXISTS idx_consume_product_available ON t_consume_product(available, status);
-- =====================================================
-- 6. 增强消费区域表 (t_consume_area)
-- =====================================================
-- 确保manage_mode字段存在（根据chonggou.txt要求）
ALTER TABLE t_consume_area
ADD COLUMN IF NOT EXISTS manage_mode INT DEFAULT 1 COMMENT '管理模式：1-餐别制 2-超市制 3-混合模式'
AFTER area_type;
-- 添加索引
CREATE INDEX IF NOT EXISTS idx_consume_area_manage_mode ON t_consume_area(manage_mode, status);
-- =====================================================
-- 7. 数据迁移和兼容性处理
-- =====================================================
-- 为现有记录设置默认值
UPDATE t_consume_record
SET payment_status = CASE
        WHEN status = 'SUCCESS' THEN 3
        WHEN status = 'FAILED' THEN 4
        WHEN status = 'REFUNDED' THEN 5
        ELSE 1
    END,
    payment_method = CASE
        WHEN pay_method = 'WECHAT' THEN 2
        WHEN pay_method = 'ALIPAY' THEN 3
        WHEN pay_method = 'BANK' THEN 4
        ELSE 1
    END
WHERE payment_status IS NULL
    OR payment_method IS NULL;
-- 为二维码表设置默认值
UPDATE t_qr_code
SET business_module = CASE
        WHEN qr_type = 'CONSUME' THEN 'consume'
        WHEN qr_type = 'ACCESS' THEN 'access'
        WHEN qr_type = 'ATTENDANCE' THEN 'attendance'
        WHEN qr_type = 'VISITOR' THEN 'visitor'
        ELSE LOWER(qr_type)
    END,
    effective_time = COALESCE(effective_time, valid_time),
    qr_content = COALESCE(qr_content, qr_code),
    qr_status = COALESCE(qr_status, status),
    create_method = COALESCE(create_method, 1)
WHERE business_module IS NULL
    OR effective_time IS NULL;
-- =====================================================
-- 8. 增强离线消费记录表 (t_offline_consume_record)
-- =====================================================
-- 根据chonggou.txt要求，确保所有字段存在
-- 注意：表可能已存在，使用IF NOT EXISTS避免重复添加
ALTER TABLE t_offline_consume_record
ADD COLUMN IF NOT EXISTS sync_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '同步状态：PENDING-待同步, SYNCED-已同步, CONFLICT-冲突, FAILED-失败'
AFTER consume_time,
    ADD COLUMN IF NOT EXISTS retry_count INT NOT NULL DEFAULT 0 COMMENT '重试次数'
AFTER sync_message,
    ADD COLUMN IF NOT EXISTS offline_trans_no VARCHAR(64) NOT NULL COMMENT '离线交易编号（设备端生成）'
AFTER id,
    ADD COLUMN IF NOT EXISTS account_id BIGINT NOT NULL COMMENT '账户ID'
AFTER user_id;
-- 添加索引（如果不存在）
CREATE INDEX IF NOT EXISTS idx_offline_sync_status ON t_offline_consume_record(sync_status, retry_count, consume_time);
CREATE INDEX IF NOT EXISTS idx_offline_account_id ON t_offline_consume_record(account_id);
-- =====================================================
-- 9. 恢复设置
-- =====================================================
SET FOREIGN_KEY_CHECKS = 1;
-- =====================================================
-- 迁移完成
-- =====================================================