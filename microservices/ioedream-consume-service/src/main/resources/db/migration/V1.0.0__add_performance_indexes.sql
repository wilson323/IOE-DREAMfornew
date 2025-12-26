-- =============================================
-- IOE-DREAM 消费服务数据库索引优化脚本
-- 版本: V1.0.0
-- 创建时间: 2025-12-27
-- 优化类型: P1性能优化 - 数据库索引
-- =============================================

-- =============================================
-- consume_product 表索引优化
-- =============================================

-- 1. 产品状态索引（用于查询上架产品）
-- 查询: SELECT * FROM consume_product WHERE status = 1
CREATE INDEX idx_product_status ON consume_product(status);

-- 2. 推荐产品索引（用于查询推荐产品）
-- 查询: SELECT * FROM consume_product WHERE is_recommended = 1
CREATE INDEX idx_product_recommended ON consume_product(is_recommended);

-- 3. 分类产品组合索引（用于按分类查询产品）
-- 查询: SELECT * FROM consume_product WHERE category_id = ? AND status = 1
CREATE INDEX idx_product_category_status ON consume_product(category_id, status);

-- 4. 热销产品组合索引（用于查询热销产品）
-- 查询: SELECT * FROM consume_product WHERE status = 1 ORDER BY sales_count DESC
CREATE INDEX idx_product_status_sales ON consume_product(status, sales_count DESC);

-- 5. 高评分产品组合索引（用于查询高评分产品）
-- 查询: SELECT * FROM consume_product WHERE status = 1 AND rating >= 4.0 ORDER BY rating DESC
CREATE INDEX idx_product_status_rating ON consume_product(status, rating DESC);

-- 6. 创建时间索引（用于按创建时间排序）
-- 查询: SELECT * FROM consume_product ORDER BY create_time DESC
CREATE INDEX idx_product_create_time ON consume_product(create_time DESC);

-- 7. 推荐排序组合索引（用于查询推荐产品并按推荐顺序排序）
-- 查询: SELECT * FROM consume_product WHERE is_recommended = 1 AND status = 1 ORDER BY recommend_order
CREATE INDEX idx_product_recommended_status_order ON consume_product(is_recommended, status, recommend_order);

-- 8. 产品名称全文索引（用于产品搜索）
-- 查询: SELECT * FROM consume_product WHERE product_name LIKE '%keyword%'
CREATE FULLTEXT INDEX idx_product_name_fulltext ON consume_product(product_name);

-- =============================================
-- consume_account 表索引优化
-- =============================================

-- 1. 用户ID索引（用于根据用户ID查询账户）
-- 查询: SELECT * FROM consume_account WHERE user_id = ?
CREATE INDEX idx_account_user_id ON consume_account(user_id);

-- 2. 账户编号唯一索引（用于根据账户编号查询账户）
-- 查询: SELECT * FROM consume_account WHERE account_code = ?
CREATE UNIQUE INDEX idx_account_code ON consume_account(account_code);

-- 3. 账户状态索引（用于查询活跃账户）
-- 查询: SELECT * FROM consume_account WHERE status = 1
CREATE INDEX idx_account_status ON consume_account(status);

-- 4. 账户类型索引（用于按账户类型查询）
-- 查询: SELECT * FROM consume_account WHERE account_type = ?
CREATE INDEX idx_account_type ON consume_account(account_type);

-- 5. 部门ID索引（用于按部门查询账户）
-- 查询: SELECT * FROM consume_account WHERE department_id = ?
CREATE INDEX idx_account_department ON consume_account(department_id);

-- 6. 用户状态组合索引（用于查询用户的活跃账户）
-- 查询: SELECT * FROM consume_account WHERE user_id = ? AND status = 1
CREATE INDEX idx_account_user_status ON consume_account(user_id, status);

-- 7. 账户类型状态组合索引（用于查询指定类型的活跃账户）
-- 查询: SELECT * FROM consume_account WHERE account_type = ? AND status = 1
CREATE INDEX idx_account_type_status ON consume_account(account_type, status);

-- 8. 最后交易时间索引（用于按交易时间排序）
-- 查询: SELECT * FROM consume_account ORDER BY last_transaction_time DESC
CREATE INDEX idx_account_last_transaction_time ON consume_account(last_transaction_time DESC);

-- =============================================
-- 消费记录表索引优化（补充）
-- =============================================

-- 假设存在 consume_record 表，添加常用索引
-- CREATE INDEX idx_record_user_id ON consume_record(user_id);
-- CREATE INDEX idx_record_account_id ON consume_record(account_id);
-- CREATE INDEX idx_record_create_time ON consume_record(create_time DESC);

-- =============================================
-- 索引优化说明
-- =============================================

-- 单列索引:
-- - 适合单条件查询
-- - 占用空间小
-- - 维护成本低

-- 组合索引:
-- - 遵循最左前缀原则
-- - 适合多条件查询
-- - 注意列的顺序（高频列在前）

-- 全文索引:
-- - 用于LIKE '%keyword%'查询
-- - 适合产品名称、描述搜索
-- - 占用空间较大

-- UNIQUE索引:
-- - 保证数据唯一性
-- - 查询性能更好
-- - 插入时需要唯一性检查

-- =============================================
-- 验证索引创建结果
-- =============================================

-- 查看 consume_product 表的所有索引
-- SHOW INDEX FROM consume_product;

-- 查看 consume_account 表的所有索引
-- SHOW INDEX FROM consume_account;

-- 分析索引使用情况
-- SELECT
--     TABLE_NAME,
--     INDEX_NAME,
--     SEQ_IN_INDEX,
--     COLUMN_NAME,
--     CARDINALITY
-- FROM information_schema.STATISTICS
-- WHERE TABLE_SCHEMA = 'ioe_dream'
--   AND TABLE_NAME IN ('consume_product', 'consume_account')
-- ORDER BY TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX;

-- =============================================
-- 索引性能测试
-- =============================================

-- 测试前：关闭查询缓存
-- SET SESSION query_cache_type = OFF;

-- 测试查询1: 查询上架产品
-- EXPLAIN SELECT * FROM consume_product WHERE status = 1;

-- 测试查询2: 按分类查询产品
-- EXPLAIN SELECT * FROM consume_product WHERE category_id = 1 AND status = 1;

-- 测试查询3: 查询用户账户
-- EXPLAIN SELECT * FROM consume_account WHERE user_id = 123;

-- 测试查询4: 查询活跃账户
-- EXPLAIN SELECT * FROM consume_account WHERE status = 1;

-- =============================================
-- 索引维护建议
-- =============================================

-- 1. 定期分析表
-- ANALYZE TABLE consume_product;
-- ANALYZE TABLE consume_account;

-- 2. 定期优化表
-- OPTIMIZE TABLE consume_product;
-- OPTIMIZE TABLE consume_account;

-- 3. 监控索引使用情况
-- SELECT * FROM sys.schema_unused_indexes WHERE object_schema = 'ioe_dream';

-- 4. 删除未使用的索引
-- DROP INDEX idx_xxx ON consume_product;

-- =============================================
-- 回滚脚本（如需删除索引）
-- =============================================

-- DROP INDEX idx_product_status ON consume_product;
-- DROP INDEX idx_product_recommended ON consume_product;
-- DROP INDEX idx_product_category_status ON consume_product;
-- DROP INDEX idx_product_status_sales ON consume_product;
-- DROP INDEX idx_product_status_rating ON consume_product;
-- DROP INDEX idx_product_create_time ON consume_product;
-- DROP INDEX idx_product_recommended_status_order ON consume_product;
-- DROP INDEX idx_product_name_fulltext ON consume_product;

-- DROP INDEX idx_account_user_id ON consume_account;
-- DROP INDEX idx_account_code ON consume_account;
-- DROP INDEX idx_account_status ON consume_account;
-- DROP INDEX idx_account_type ON consume_account;
-- DROP INDEX idx_account_department ON consume_account;
-- DROP INDEX idx_account_user_status ON consume_account;
-- DROP INDEX idx_account_type_status ON consume_account;
-- DROP INDEX idx_account_last_transaction_time ON consume_account;

-- =============================================
-- 执行验证
-- =============================================

-- 检查索引是否创建成功
SELECT
    '索引创建完成' AS status,
    COUNT(*) AS total_indexes
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME IN ('consume_product', 'consume_account')
  AND INDEX_NAME LIKE 'idx_%';
