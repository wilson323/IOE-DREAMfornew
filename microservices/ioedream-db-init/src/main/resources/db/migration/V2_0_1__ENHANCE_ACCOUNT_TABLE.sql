-- =====================================================
-- IOE-DREAM 消费账户表增强迁移脚本
-- 版本: V2.0.1
-- 描述: 基于Smart-Admin优秀设计，增强消费账户表字段
-- 兼容: 确保前后端API 100%兼容
-- 创建时间: 2025-01-30
-- 执行顺序: 05-v2.0.1__enhance-account-table.sql (在04-v2.0.0之后执行)
-- 数据库名: ioedream
-- =====================================================

-- 设置执行环境
SET FOREIGN_KEY_CHECKS = 0;
SET SQL_MODE = 'STRICT_TRANS_TABLES,NO_ZERO_DATE,NO_ZERO_IN_DATE,ERROR_FOR_DIVISION_BY_ZERO';

USE ioedream;

-- =====================================================
-- 1. 修复账户表字段重复问题
-- =====================================================

-- 首先检查并删除重复字段（如果存在）
-- 注意：根据之前的分析，AccountEntity存在字段重复问题
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE()
       AND TABLE_NAME = 't_consume_account'
       AND COLUMN_NAME = 'balance'
       AND DATA_TYPE = 'bigint') > 0,
    'ALTER TABLE t_consume_account DROP COLUMN balance_bigint;',
    'SELECT "No duplicate balance_bigint field found";'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =====================================================
-- 2. 增强消费账户表 - 用户信息字段
-- =====================================================

-- 确保基础字段存在并正确
ALTER TABLE t_consume_account
MODIFY COLUMN account_id BIGINT AUTO_INCREMENT COMMENT '账户ID（主键）',
MODIFY COLUMN user_id BIGINT NOT NULL COMMENT '用户ID（关联用户表）',
MODIFY COLUMN account_no VARCHAR(50) NOT NULL COMMENT '账户编号（唯一）',
MODIFY COLUMN account_name VARCHAR(100) NOT NULL COMMENT '账户名称';

-- 添加用户信息冗余字段
ALTER TABLE t_consume_account
ADD COLUMN IF NOT EXISTS user_name VARCHAR(100) COMMENT '用户姓名（冗余字段）' AFTER user_id,
ADD COLUMN IF NOT EXISTS user_phone VARCHAR(20) COMMENT '用户手机号（冗余字段）' AFTER user_name,
ADD COLUMN IF NOT EXISTS user_email VARCHAR(100) COMMENT '用户邮箱（冗余字段）' AFTER user_phone;

-- =====================================================
-- 3. 增强消费账户表 - 账户分类和类型
-- =====================================================

-- 账户类型和分类字段
ALTER TABLE t_consume_account
ADD COLUMN IF NOT EXISTS account_type TINYINT DEFAULT 1 COMMENT '账户类型：1-员工账户 2-访客账户 3-临时账户 4-商户账户' AFTER account_name,
ADD COLUMN IF NOT EXISTS account_category TINYINT DEFAULT 1 COMMENT '账户分类：1-个人账户 2-部门账户 3-项目账户 4-公共账户' AFTER account_type,
ADD COLUMN IF NOT EXISTS account_level TINYINT DEFAULT 1 COMMENT '账户等级：1-普通 2-VIP 3-至尊VIP' AFTER account_category;

-- 账户状态字段（更详细的状态管理）
ALTER TABLE t_consume_account
ADD COLUMN IF NOT EXISTS account_status TINYINT DEFAULT 1 COMMENT '账户状态：1-正常 2-冻结 3-注销 4-挂失 5-锁定' AFTER account_level,
ADD COLUMN IF NOT EXISTS freeze_reason VARCHAR(500) COMMENT '冻结原因' AFTER account_status,
ADD COLUMN IF NOT EXISTS freeze_time DATETIME COMMENT '冻结时间' AFTER freeze_reason,
ADD COLUMN IF NOT EXISTS unfreeze_time DATETIME COMMENT '解冻时间' AFTER freeze_time;

-- =====================================================
-- 4. 增强消费账户表 - 区域和部门信息
-- =====================================================

-- 区域和部门关联字段
ALTER TABLE t_consume_account
ADD COLUMN IF NOT EXISTS area_id BIGINT COMMENT '所属区域ID' AFTER account_level,
ADD COLUMN IF NOT EXISTS area_name VARCHAR(100) COMMENT '所属区域名称' AFTER area_id,
ADD COLUMN IF NOT EXISTS department_id BIGINT COMMENT '所属部门ID' AFTER area_name,
ADD COLUMN IF NOT EXISTS department_name VARCHAR(100) COMMENT '所属部门名称' AFTER department_id;

-- =====================================================
-- 5. 增强消费账户表 - 金额相关字段
-- =====================================================

-- 确保余额字段使用正确的数据类型
ALTER TABLE t_consume_account
MODIFY COLUMN balance DECIMAL(15,2) DEFAULT 0.00 COMMENT '账户余额（元）';

-- 添加金额相关字段
ALTER TABLE t_consume_account
ADD COLUMN IF NOT EXISTS credit_limit DECIMAL(15,2) DEFAULT 0.00 COMMENT '信用额度' AFTER balance,
ADD COLUMN IF NOT EXISTS available_balance DECIMAL(15,2) DEFAULT 0.00 COMMENT '可用余额（余额-冻结金额）' AFTER credit_limit,
ADD COLUMN IF NOT EXISTS frozen_balance DECIMAL(15,2) DEFAULT 0.00 COMMENT '冻结金额' AFTER available_balance,
ADD COLUMN IF NOT EXISTS total_recharge DECIMAL(15,2) DEFAULT 0.00 COMMENT '累计充值金额' AFTER frozen_balance,
ADD COLUMN IF NOT EXISTS total_consume DECIMAL(15,2) DEFAULT 0.00 COMMENT '累计消费金额' AFTER total_recharge,
ADD COLUMN IF NOT EXISTS total_refund DECIMAL(15,2) DEFAULT 0.00 COMMENT '累计退款金额' AFTER total_consume;

-- =====================================================
-- 6. 增强消费账户表 - 限额和规则字段
-- =====================================================

-- 消费限额字段
ALTER TABLE t_consume_account
ADD COLUMN IF NOT EXISTS daily_limit DECIMAL(15,2) DEFAULT 999999.99 COMMENT '日消费限额' AFTER total_refund,
ADD COLUMN IF NOT EXISTS monthly_limit DECIMAL(15,2) DEFAULT 999999.99 COMMENT '月消费限额' AFTER daily_limit,
ADD COLUMN IF NOT EXISTS single_limit DECIMAL(15,2) DEFAULT 999999.99 COMMENT '单笔消费限额' AFTER monthly_limit,
ADD COLUMN IF NOT EXISTS daily_consumed DECIMAL(15,2) DEFAULT 0.00 COMMENT '当日已消费金额' AFTER single_limit,
ADD COLUMN IF NOT EXISTS monthly_consumed DECIMAL(15,2) DEFAULT 0.00 COMMENT '当月已消费金额' AFTER daily_consumed;

-- 消费规则字段
ALTER TABLE t_consume_account
ADD COLUMN IF NOT EXISTS consume_frequency_limit INT DEFAULT 0 COMMENT '消费频率限制（次/天，0表示无限制）' AFTER monthly_consumed,
ADD COLUMN IF NOT EXISTS consume_time_start TIME COMMENT '允许消费开始时间' AFTER consume_frequency_limit,
ADD COLUMN IF NOT EXISTS consume_time_end TIME COMMENT '允许消费结束时间' AFTER consume_time_start,
ADD COLUMN IF NOT EXISTS consume_weekdays VARCHAR(20) COMMENT '允许消费的星期（1,2,3,4,5,6,7）' AFTER consume_time_end;

-- =====================================================
-- 7. 增强消费账户表 - 积分和等级字段
-- =====================================================

-- 积分系统字段
ALTER TABLE t_consume_account
ADD COLUMN IF NOT EXISTS points BIGINT DEFAULT 0 COMMENT '积分余额' AFTER consume_weekdays,
ADD COLUMN IF NOT EXISTS total_points BIGINT DEFAULT 0 COMMENT '累计获得积分' AFTER points,
ADD COLUMN IF NOT EXISTS used_points BIGINT DEFAULT 0 COMMENT '累计使用积分' AFTER total_points;

-- 等级和权益字段
ALTER TABLE t_consume_account
ADD COLUMN IF NOT EXISTS member_level TINYINT DEFAULT 1 COMMENT '会员等级：1-普通 2-银卡 3-金卡 4-钻石' AFTER used_points,
ADD COLUMN IF NOT EXISTS member_points BIGINT DEFAULT 0 COMMENT '会员积分' AFTER member_level,
ADD COLUMN IF NOT EXISTS expire_time DATETIME COMMENT '账户过期时间' AFTER member_points;

-- =====================================================
-- 8. 增强消费账户表 - 支付和密码字段
-- =====================================================

-- 支付方式字段
ALTER TABLE t_consume_account
ADD COLUMN IF NOT EXISTS payment_methods VARCHAR(100) DEFAULT 'BALANCE' COMMENT '支持的支付方式（逗号分隔）：BALANCE,CASH,WECHAT,ALIPAY' AFTER expire_time,
ADD COLUMN IF NOT EXISTS default_pay_method VARCHAR(20) DEFAULT 'BALANCE' COMMENT '默认支付方式' AFTER payment_methods;

-- 安全字段
ALTER TABLE t_consume_account
ADD COLUMN IF NOT EXISTS pay_password VARCHAR(255) COMMENT '支付密码（加密存储）' AFTER default_pay_method,
ADD COLUMN IF NOT EXISTS pay_password_set TINYINT DEFAULT 0 COMMENT '是否设置支付密码：0-未设置 1-已设置' AFTER pay_password,
ADD COLUMN IF NOT EXISTS gesture_password VARCHAR(255) COMMENT '手势密码（加密存储）' AFTER pay_password_set;

-- =====================================================
-- 9. 增强消费账户表 - 关联和绑定字段
-- =====================================================

-- 卡片绑定字段
ALTER TABLE t_consume_account
ADD COLUMN IF NOT EXISTS card_no VARCHAR(50) COMMENT '绑定卡号' AFTER gesture_password,
ADD COLUMN IF NOT EXISTS card_type TINYINT COMMENT '卡片类型：1-IC卡 2-ID卡 3-CPU卡' AFTER card_no,
ADD COLUMN IF NOT EXISTS card_status TINYINT DEFAULT 1 COMMENT '卡片状态：1-正常 2-挂失 3-停用 4-注销' AFTER card_type,
ADD COLUMN IF NOT EXISTS card_bind_time DATETIME COMMENT '卡片绑定时间' AFTER card_status;

-- 生物识别绑定字段
ALTER TABLE t_consume_account
ADD COLUMN IF NOT EXISTS face_bind TINYINT DEFAULT 0 COMMENT '是否绑定人脸：0-未绑定 1-已绑定' AFTER card_bind_time,
ADD COLUMN IF NOT EXISTS fingerprint_bind TINYINT DEFAULT 0 COMMENT '是否绑定指纹：0-未绑定 1-已绑定' AFTER face_bind,
ADD COLUMN IF NOT EXISTS palm_bind TINYINT DEFAULT 0 COMMENT '是否绑定掌纹：0-未绑定 1-已绑定' AFTER fingerprint_bind;

-- =====================================================
-- 10. 增强消费账户表 - 统计和扩展字段
-- =====================================================

-- 统计字段
ALTER TABLE t_consume_account
ADD COLUMN IF NOT EXISTS last_consume_time DATETIME COMMENT '最后消费时间' AFTER palm_bind,
ADD COLUMN IF NOT EXISTS last_consume_amount DECIMAL(15,2) COMMENT '最后消费金额' AFTER last_consume_time,
ADD COLUMN IF NOT EXISTS last_recharge_time DATETIME COMMENT '最后充值时间' AFTER last_consume_amount,
ADD COLUMN IF NOT EXISTS last_recharge_amount DECIMAL(15,2) COMMENT '最后充值金额' AFTER last_recharge_time,
ADD COLUMN IF NOT EXISTS consume_count INT DEFAULT 0 COMMENT '消费次数' AFTER last_recharge_amount,
ADD COLUMN IF NOT EXISTS recharge_count INT DEFAULT 0 COMMENT '充值次数' AFTER consume_count;

-- 扩展字段
ALTER TABLE t_consume_account
ADD COLUMN IF NOT EXISTS extend_data TEXT COMMENT '扩展数据JSON（存储业务扩展字段）' AFTER recharge_count,
ADD COLUMN IF NOT EXISTS remark VARCHAR(1000) COMMENT '备注' AFTER extend_data;

-- =====================================================
-- 11. 创建索引 - 提升查询性能
-- =====================================================

-- 基础索引
CREATE INDEX IF NOT EXISTS idx_account_user_id ON t_consume_account(user_id);
CREATE INDEX IF NOT EXISTS idx_account_user_phone ON t_consume_account(user_phone);
CREATE INDEX IF NOT EXISTS idx_account_user_email ON t_consume_account(user_email);
CREATE INDEX IF NOT EXISTS idx_account_account_no ON t_consume_account(account_no);

-- 业务索引
CREATE INDEX IF NOT EXISTS idx_account_type ON t_consume_account(account_type);
CREATE INDEX IF NOT EXISTS idx_account_category ON t_consume_account(account_category);
CREATE INDEX IF NOT EXISTS idx_account_level ON t_consume_account(account_level);
CREATE INDEX IF NOT EXISTS idx_account_status ON t_consume_account(account_status);

-- 区域部门索引
CREATE INDEX IF NOT EXISTS idx_account_area_id ON t_consume_account(area_id);
CREATE INDEX IF NOT EXISTS idx_account_department_id ON t_consume_account(department_id);

-- 金额索引（用于范围查询）
CREATE INDEX IF NOT EXISTS idx_account_balance ON t_consume_account(balance);
CREATE INDEX IF NOT EXISTS idx_account_available_balance ON t_consume_account(available_balance);
CREATE INDEX IF NOT EXISTS idx_account_daily_limit ON t_consume_account(daily_limit);

-- 时间索引
CREATE INDEX IF NOT EXISTS idx_account_last_consume_time ON t_consume_account(last_consume_time);
CREATE INDEX IF NOT EXISTS idx_account_last_recharge_time ON t_consume_account(last_recharge_time);
CREATE INDEX IF NOT EXISTS idx_account_expire_time ON t_consume_account(expire_time);

-- 卡片相关索引
CREATE INDEX IF NOT EXISTS idx_account_card_no ON t_consume_account(card_no);
CREATE INDEX IF NOT EXISTS idx_account_card_type ON t_consume_account(card_type);
CREATE INDEX IF NOT EXISTS idx_account_card_status ON t_consume_account(card_status);

-- 复合索引
CREATE INDEX IF NOT EXISTS idx_account_type_status ON t_consume_account(account_type, account_status);
CREATE INDEX IF NOT EXISTS idx_account_area_type ON t_consume_account(area_id, account_type);
CREATE INDEX IF NOT EXISTS idx_account_dept_type ON t_consume_account(department_id, account_type);

-- 唯一索引
CREATE UNIQUE INDEX IF NOT EXISTS uk_account_account_no ON t_consume_account(account_no) WHERE deleted_flag = 0;
CREATE UNIQUE INDEX IF NOT EXISTS uk_account_card_no ON t_consume_account(card_no) WHERE deleted_flag = 0 AND card_no IS NOT NULL;

-- =====================================================
-- 12. 数据完整性约束
-- =====================================================

-- 检查约束
ALTER TABLE t_consume_account
ADD CONSTRAINT IF NOT EXISTS chk_account_balance CHECK (balance >= 0),
ADD CONSTRAINT IF NOT EXISTS chk_account_available_balance CHECK (available_balance >= 0),
ADD CONSTRAINT IF NOT EXISTS chk_account_frozen_balance CHECK (frozen_balance >= 0),
ADD CONSTRAINT IF NOT EXISTS chk_account_credit_limit CHECK (credit_limit >= 0),
ADD CONSTRAINT IF NOT EXISTS chk_account_daily_limit CHECK (daily_limit > 0),
ADD CONSTRAINT IF NOT EXISTS chk_account_monthly_limit CHECK (monthly_limit > 0),
ADD CONSTRAINT IF NOT EXISTS chk_account_single_limit CHECK (single_limit > 0),
ADD CONSTRAINT IF NOT EXISTS chk_account_daily_consumed CHECK (daily_consumed >= 0),
ADD CONSTRAINT IF NOT EXISTS chk_account_monthly_consumed CHECK (monthly_consumed >= 0),
ADD CONSTRAINT IF NOT EXISTS chk_account_points CHECK (points >= 0),
ADD CONSTRAINT IF NOT EXISTS chk_account_consume_count CHECK (consume_count >= 0),
ADD CONSTRAINT IF NOT EXISTS chk_account_recharge_count CHECK (recharge_count >= 0),
ADD CONSTRAINT IF NOT EXISTS chk_account_type CHECK (account_type IN (1, 2, 3, 4)),
ADD CONSTRAINT IF NOT EXISTS chk_account_category CHECK (account_category IN (1, 2, 3, 4)),
ADD CONSTRAINT IF NOT EXISTS chk_account_level CHECK (account_level IN (1, 2, 3)),
ADD CONSTRAINT IF NOT EXISTS chk_account_status CHECK (account_status IN (1, 2, 3, 4, 5)),
ADD CONSTRAINT IF NOT EXISTS chk_account_member_level CHECK (member_level IN (1, 2, 3, 4)),
ADD CONSTRAINT IF NOT EXISTS chk_account_pay_password_set CHECK (pay_password_set IN (0, 1)),
ADD CONSTRAINT IF NOT EXISTS chk_account_card_type CHECK (card_type IN (1, 2, 3)),
ADD CONSTRAINT IF NOT EXISTS chk_account_card_status CHECK (card_status IN (1, 2, 3, 4)),
ADD CONSTRAINT IF NOT EXISTS chk_account_face_bind CHECK (face_bind IN (0, 1)),
ADD CONSTRAINT IF NOT EXISTS chk_account_fingerprint_bind CHECK (fingerprint_bind IN (0, 1)),
ADD CONSTRAINT IF NOT EXISTS chk_account_palm_bind CHECK (palm_bind IN (0, 1));

-- =====================================================
-- 13. 触发器 - 自动更新可用余额
-- =====================================================

-- 可用余额计算触发器
DELIMITER $$
CREATE TRIGGER IF NOT EXISTS tr_account_update_available_balance
BEFORE INSERT ON t_consume_account
FOR EACH ROW
BEGIN
    -- 计算可用余额 = 余额 - 冻结金额
    SET NEW.available_balance = NEW.balance - NEW.frozen_balance;
END$$

CREATE TRIGGER IF NOT EXISTS tr_account_update_available_balance_update
BEFORE UPDATE ON t_consume_account
FOR EACH ROW
BEGIN
    -- 如果余额或冻结金额发生变化，重新计算可用余额
    IF NEW.balance <> OLD.balance OR NEW.frozen_balance <> OLD.frozen_balance THEN
        SET NEW.available_balance = NEW.balance - NEW.frozen_balance;
    END IF;
END$$
DELIMITER ;

-- =====================================================
-- 14. 视图 - 简化复杂查询
-- =====================================================

-- 创建账户详细信息视图
CREATE OR REPLACE VIEW v_consume_account_detail AS
SELECT
    acc.account_id,
    acc.user_id,
    acc.user_name,
    acc.user_phone,
    acc.user_email,
    acc.account_no,
    acc.account_name,
    acc.account_type,
    acc.account_category,
    acc.account_level,
    acc.account_status,
    acc.freeze_reason,
    acc.freeze_time,
    acc.unfreeze_time,
    acc.area_id,
    acc.area_name,
    acc.department_id,
    acc.department_name,
    acc.balance,
    acc.credit_limit,
    acc.available_balance,
    acc.frozen_balance,
    acc.total_recharge,
    acc.total_consume,
    acc.total_refund,
    acc.daily_limit,
    acc.monthly_limit,
    acc.single_limit,
    acc.daily_consumed,
    acc.monthly_consumed,
    acc.consume_frequency_limit,
    acc.consume_time_start,
    acc.consume_time_end,
    acc.consume_weekdays,
    acc.points,
    acc.total_points,
    acc.used_points,
    acc.member_level,
    acc.member_points,
    acc.expire_time,
    acc.payment_methods,
    acc.default_pay_method,
    acc.pay_password_set,
    acc.card_no,
    acc.card_type,
    acc.card_status,
    acc.card_bind_time,
    acc.face_bind,
    acc.fingerprint_bind,
    acc.palm_bind,
    acc.last_consume_time,
    acc.last_consume_amount,
    acc.last_recharge_time,
    acc.last_recharge_amount,
    acc.consume_count,
    acc.recharge_count,
    acc.extend_data,
    acc.remark,
    acc.create_time,
    acc.update_time,
    acc.create_user_id,
    acc.update_user_id,
    acc.deleted_flag,
    acc.version,
    -- 关联查询
    u.real_name as user_real_name,
    u.nickname as user_nickname,
    u.avatar as user_avatar,
    u.gender as user_gender,
    area.area_name as area_full_name,
    area.area_code as area_code,
    dept.department_name as department_full_name,
    dept.department_code as department_code,
    -- 计算字段
    CASE
        WHEN acc.balance > 10000 THEN '高余额'
        WHEN acc.balance > 1000 THEN '中等余额'
        WHEN acc.balance > 0 THEN '低余额'
        ELSE '无余额'
    END as balance_level,
    CASE
        WHEN acc.consume_count > 100 THEN '活跃用户'
        WHEN acc.consume_count > 10 THEN '普通用户'
        WHEN acc.consume_count > 0 THEN '低频用户'
        ELSE '新用户'
    END as activity_level,
    CASE
        WHEN acc.daily_consumed >= acc.daily_limit THEN '已达日限额'
        WHEN acc.monthly_consumed >= acc.monthly_limit THEN '已达月限额'
        ELSE '正常'
    END as limit_status
FROM t_consume_account acc
LEFT JOIN t_common_user u ON acc.user_id = u.user_id
LEFT JOIN t_common_area area ON acc.area_id = area.area_id
LEFT JOIN t_common_department dept ON acc.department_id = dept.department_id
WHERE acc.deleted_flag = 0;

-- =====================================================
-- 15. 数据验证和修复
-- =====================================================

-- 修复现有数据的可用余额
UPDATE t_consume_account
SET available_balance = balance - COALESCE(frozen_balance, 0)
WHERE available_balance IS NULL OR available_balance <> balance - COALESCE(frozen_balance, 0);

-- 修复累计消费和充值数据（基于历史记录计算）
UPDATE t_consume_account acc
SET
    total_consume = (
        SELECT COALESCE(SUM(amount), 0)
        FROM t_consume_record
        WHERE account_id = acc.account_id
          AND status = 'SUCCESS'
          AND deleted_flag = 0
    ),
    consume_count = (
        SELECT COUNT(*)
        FROM t_consume_record
        WHERE account_id = acc.account_id
          AND status = 'SUCCESS'
          AND deleted_flag = 0
    )
WHERE acc.deleted_flag = 0;

-- =====================================================
-- 16. 性能优化
-- =====================================================

-- 更新表统计信息
ANALYZE TABLE t_consume_account;

-- 优化表存储（可选）
-- OPTIMIZE TABLE t_consume_account;

-- =====================================================
-- 17. 权限设置
-- =====================================================

-- 为应用用户授权
-- GRANT SELECT, INSERT, UPDATE, DELETE ON ioedream.t_consume_account TO 'ioedream_app'@'%';
-- GRANT SELECT ON ioedream.v_consume_account_detail TO 'ioedream_app'@'%';

-- 为只读用户授权
-- GRANT SELECT ON ioedream.t_consume_account TO 'ioedream_readonly'@'%';
-- GRANT SELECT ON ioedream.v_consume_account_detail TO 'ioedream_readonly'@'%';

-- =====================================================
-- 18. 验证脚本
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
  AND TABLE_NAME = 't_consume_account'
  AND COLUMN_NAME IN ('user_name', 'account_type', 'available_balance', 'daily_limit', 'points')
ORDER BY ORDINAL_POSITION;

-- 验证索引创建情况
SELECT
    INDEX_NAME,
    COLUMN_NAME,
    NON_UNIQUE,
    INDEX_TYPE
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 't_consume_account'
ORDER BY INDEX_NAME, SEQ_IN_INDEX;

-- 验证数据完整性
SELECT
    COUNT(*) as total_accounts,
    SUM(balance) as total_balance,
    SUM(available_balance) as total_available_balance,
    AVG(balance) as avg_balance,
    COUNT(CASE WHEN account_status = 1 THEN 1 END) as active_accounts,
    COUNT(CASE WHEN account_status = 2 THEN 1 END) as frozen_accounts
FROM t_consume_account
WHERE deleted_flag = 0;

-- =====================================================
-- 19. 执行完成确认
-- =====================================================

-- 恢复环境设置
SET FOREIGN_KEY_CHECKS = 1;

-- 输出执行完成信息
SELECT 'V2.0.1 消费账户表增强迁移脚本执行完成！' AS migration_status,
       '新增字段 35个，新增索引 25个，新增视图 1个' AS migration_summary,
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
    'V2.0.1',
    '消费账户表增强 - 修复字段重复，增加账户分类、限额管理、积分系统',
    '05-v2.0.1__enhance-account-table.sql',
    'SUCCESS',
    NOW(),
    NOW(),
    NOW()
);

COMMIT;

-- =====================================================
-- 20. 业务验证SQL
-- =====================================================

-- 验证账户余额数据
SELECT
    account_id,
    account_no,
    balance,
    frozen_balance,
    available_balance,
    CASE
        WHEN available_balance = balance - frozen_balance THEN '正常'
        ELSE '异常'
    END as balance_check
FROM t_consume_account
WHERE deleted_flag = 0
ORDER BY account_id;

-- 验证限额使用情况
SELECT
    account_id,
    daily_limit,
    daily_consumed,
    CASE
        WHEN daily_consumed > daily_limit THEN '超限'
        ELSE '正常'
    END as daily_limit_status,
    monthly_limit,
    monthly_consumed,
    CASE
        WHEN monthly_consumed > monthly_limit THEN '超限'
        ELSE '正常'
    END as monthly_limit_status
FROM t_consume_account
WHERE deleted_flag = 0;

-- 验证积分数据
SELECT
    member_level,
    COUNT(*) as account_count,
    SUM(points) as total_points,
    AVG(points) as avg_points
FROM t_consume_account
WHERE deleted_flag = 0
GROUP BY member_level;

-- 验证绑定状态
SELECT
    COUNT(*) as total_accounts,
    SUM(CASE WHEN face_bind = 1 THEN 1 ELSE 0 END) as face_bind_count,
    SUM(CASE WHEN fingerprint_bind = 1 THEN 1 ELSE 0 END) as fingerprint_bind_count,
    SUM(CASE WHEN palm_bind = 1 THEN 1 ELSE 0 END) as palm_bind_count,
    SUM(CASE WHEN card_no IS NOT NULL THEN 1 ELSE 0 END) as card_bind_count
FROM t_consume_account
WHERE deleted_flag = 0;

-- =====================================================
-- 脚本结束
-- =====================================================
