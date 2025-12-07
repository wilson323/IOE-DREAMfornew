-- ============================================
-- IOE-DREAM 数据库索引优化执行脚本
-- 版本: 1.0.0
-- 日期: 2025-01-30
-- 说明: 执行所有模块的索引优化SQL
-- ============================================

-- ============================================
-- 执行前检查
-- ============================================
-- 1. 确认数据库连接正常
-- 2. 确认有执行权限
-- 3. 建议在非高峰期执行
-- 4. 建议先备份数据库

-- ============================================
-- 1. 消费模块索引优化
-- ============================================
-- 执行文件: microservices/ioedream-consume-service/src/main/resources/sql/consume_index_optimization.sql
-- 说明: 消费模块索引优化（已存在，需要确认是否已执行）

-- ============================================
-- 2. 门禁模块索引优化
-- ============================================
-- 执行文件: microservices/ioedream-access-service/src/main/resources/sql/access_index_optimization.sql
SOURCE microservices/ioedream-access-service/src/main/resources/sql/access_index_optimization.sql;

-- ============================================
-- 3. 考勤模块索引优化
-- ============================================
-- 执行文件: microservices/ioedream-attendance-service/src/main/resources/sql/attendance_index_optimization.sql
SOURCE microservices/ioedream-attendance-service/src/main/resources/sql/attendance_index_optimization.sql;

-- ============================================
-- 4. 访客模块索引优化
-- ============================================
-- 执行文件: microservices/ioedream-visitor-service/src/main/resources/sql/visitor_index_optimization.sql
SOURCE microservices/ioedream-visitor-service/src/main/resources/sql/visitor_index_optimization.sql;

-- ============================================
-- 5. 视频模块索引优化
-- ============================================
-- 执行文件: microservices/ioedream-video-service/src/main/resources/sql/video_index_optimization.sql
SOURCE microservices/ioedream-video-service/src/main/resources/sql/video_index_optimization.sql;

-- ============================================
-- 执行后验证
-- ============================================
-- 检查索引是否创建成功
SELECT 
    TABLE_SCHEMA,
    TABLE_NAME,
    INDEX_NAME,
    COLUMN_NAME,
    SEQ_IN_INDEX
FROM 
    INFORMATION_SCHEMA.STATISTICS
WHERE 
    TABLE_SCHEMA = DATABASE()
    AND INDEX_NAME LIKE 'idx_%'
ORDER BY 
    TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX;

-- ============================================
-- 性能验证
-- ============================================
-- 执行EXPLAIN分析查询计划，确认索引被使用
-- 示例：
-- EXPLAIN SELECT * FROM t_access_record WHERE user_id = 1001 AND access_time > '2025-01-01';

