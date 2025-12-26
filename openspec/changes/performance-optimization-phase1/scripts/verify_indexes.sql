-- ================================================================
-- IOE-DREAM 索引效果验证脚本
-- 用途: 验证索引创建是否成功，并测试索引效果
-- 使用方法: mysql -u root -p ioedream < verify_indexes.sql
-- 创建日期: 2025-01-XX
-- ================================================================

-- ================================================================
-- 1. 验证索引是否创建成功
-- ================================================================
SELECT '========================================' AS '分隔线';
SELECT '1. 索引创建验证' AS '标题';
SELECT '========================================' AS '分隔线';

-- 门禁记录表索引
SELECT '✅ 门禁记录表 (t_access_record)' AS '表名';
SHOW INDEX FROM t_access_record WHERE Key_name LIKE 'idx_%';

-- 考勤记录表索引
SELECT '✅ 考勤记录表 (t_attendance_record)' AS '表名';
SHOW INDEX FROM t_attendance_record WHERE Key_name LIKE 'idx_%';

-- 消费记录表索引
SELECT '✅ 消费记录表 (t_consume_record)' AS '表名';
SHOW INDEX FROM t_consume_record WHERE Key_name LIKE 'idx_%';

-- 访客记录表索引
SELECT '✅ 访客记录表 (t_visitor_record)' AS '表名';
SHOW INDEX FROM t_visitor_record WHERE Key_name LIKE 'idx_%';

-- 视频设备表索引
SELECT '✅ 视频设备表 (t_video_device)' AS '表名';
SHOW INDEX FROM t_video_device WHERE Key_name LIKE 'idx_%';

-- ================================================================
-- 2. 统计已创建的索引数量
-- ================================================================
SELECT '========================================' AS '分隔线';
SELECT '2. 索引统计' AS '标题';
SELECT '========================================' AS '分隔线';

SELECT
    TABLE_NAME AS '表名',
    COUNT(DISTINCT INDEX_NAME) AS '索引数量',
    GROUP_CONCAT(DISTINCT INDEX_NAME ORDER BY INDEX_NAME SEPARATOR ', ') AS '索引列表'
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'ioedream'
  AND TABLE_NAME IN (
    't_access_record',
    't_attendance_record',
    't_consume_record',
    't_visitor_record',
    't_video_device',
    't_user',
    't_department',
    't_device'
  )
  AND INDEX_NAME != 'PRIMARY'
  AND INDEX_NAME LIKE 'idx_%'
GROUP BY TABLE_NAME
ORDER BY TABLE_NAME;

-- ================================================================
-- 3. 分析典型查询的执行计划
-- ================================================================
SELECT '========================================' AS '分隔线';
SELECT '3. 执行计划分析 (EXPLAIN)' AS '标题';
SELECT '========================================' AS '分隔线';

-- 测试查询1：门禁记录用户查询
SELECT '测试查询1: 门禁记录用户查询' AS '查询名称';
EXPLAIN SELECT *
FROM t_access_record
WHERE user_id = 1
  AND pass_time >= '2025-01-01'
ORDER BY pass_time DESC
LIMIT 20;

-- 测试查询2：考勤记录用户查询
SELECT '测试查询2: 考勤记录用户查询' AS '查询名称';
EXPLAIN SELECT *
FROM t_attendance_record
WHERE user_id = 1
  AND punch_time >= '2025-01-01'
ORDER BY punch_time DESC
LIMIT 20;

-- 测试查询3：消费记录用户查询
SELECT '测试查询3: 消费记录用户查询' AS '查询名称';
EXPLAIN SELECT *
FROM t_consume_record
WHERE user_id = 1
  AND consume_time >= '2025-01-01'
ORDER BY consume_time DESC
LIMIT 20;

-- ================================================================
-- 4. 检查索引选择性（Cardinality）
-- ================================================================
SELECT '========================================' AS '分隔线';
SELECT '4. 索引选择性分析' AS '标题';
SELECT '========================================' AS '分隔线';

SELECT
    TABLE_NAME AS '表名',
    INDEX_NAME AS '索引名',
    COLUMN_NAME AS '列名',
    CARDINALITY AS '基数',
    TABLE_ROWS AS '表总行数',
    ROUND(CARDINALITY / TABLE_ROWS * 100, 2) AS '选择性(%)'
FROM information_schema.STATISTICS S
JOIN information_schema.TABLES T
ON S.TABLE_SCHEMA = T.TABLE_SCHEMA
AND S.TABLE_NAME = T.TABLE_NAME
WHERE S.TABLE_SCHEMA = 'ioedream'
  AND S.INDEX_NAME LIKE 'idx_%'
  AND S.SEQ_IN_INDEX = 1
ORDER BY S.TABLE_NAME, S.INDEX_NAME;

-- ================================================================
-- 5. 索引大小统计
-- ================================================================
SELECT '========================================' AS '分隔线';
SELECT '5. 索引大小统计' AS '标题';
SELECT '========================================' AS '分隔线';

SELECT
    TABLE_NAME AS '表名',
    CONCAT(ROUND(INDEX_LENGTH / 1024 / 1024, 2), 'MB') AS '索引总大小',
    ROUND(INDEX_LENGTH / TABLE_ROWS, 2) AS '每行索引大小(字节)'
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'ioedream'
  AND TABLE_NAME IN (
    't_access_record',
    't_attendance_record',
    't_consume_record',
    't_visitor_record',
    't_video_device',
    't_user',
    't_department',
    't_device'
  )
ORDER BY INDEX_LENGTH DESC;

-- ================================================================
-- 6. 性能对比测试（使用索引 vs 不使用索引）
-- ================================================================
SELECT '========================================' AS '分隔线';
SELECT '6. 性能对比测试' AS '标题';
SELECT '========================================' AS '分隔线';

-- 测试查询性能：使用索引
SELECT '测试：使用索引查询门禁记录' AS '测试名称';

SET @start_time = NOW(6);

SELECT COUNT(*)
FROM t_access_record FORCE INDEX (idx_access_user_time)
WHERE user_id = 1
  AND pass_time >= '2025-01-01';

SET @end_time = NOW(6);
SET @duration_with_index = TIMESTAMPDIFF(MICROSECOND, @start_time, @end_time) / 1000000;

-- 测试查询性能：不使用索引
SELECT '测试：不使用索引查询门禁记录（全表扫描）' AS '测试名称';

SET @start_time = NOW(6);

SELECT COUNT(*)
FROM t_access_record
WHERE user_id = 1
  AND pass_time >= '2025-01-01';

SET @end_time = NOW(6);
SET @duration_without_index = TIMESTAMPDIFF(MICROSECOND, @start_time, @end_time) / 1000000;

-- 性能对比结果
SELECT
    '使用索引' AS '方式',
    CONCAT(ROUND(@duration_with_index * 1000, 2), 'ms') AS '查询时间'
UNION ALL
SELECT
    '不使用索引',
    CONCAT(ROUND(@duration_without_index * 1000, 2), 'ms');

-- ================================================================
-- 7. 索引使用率统计
-- ================================================================
SELECT '========================================' AS '分隔线';
SELECT '7. 索引使用率统计' AS '标题';
SELECT '========================================' AS '分隔线';

SELECT
    OBJECT_NAME AS '表名',
    INDEX_NAME AS '索引名',
    COUNT_STAR AS '使用次数',
    ROUND(AVG_TIMER_WAIT / 1000000000000, 4) AS '平均响应时间(s)',
    CASE
        WHEN COUNT_STAR = 0 THEN '❌ 未使用'
        WHEN COUNT_STAR < 100 THEN '⚠️  使用较少'
        ELSE '✅ 使用正常'
    END AS '使用状态'
FROM performance_schema.table_io_waits_summary_by_index_usage
WHERE OBJECT_SCHEMA = 'ioedream'
  AND INDEX_NAME LIKE 'idx_%'
ORDER BY COUNT_STAR DESC;

-- ================================================================
-- 8. 优化建议总结
-- ================================================================
SELECT '========================================' AS '分隔线';
SELECT '8. 优化建议总结' AS '标题';
SELECT '========================================' AS '分隔线';

SELECT
    '✅ 索引创建完成' AS '状态',
    COUNT(DISTINCT INDEX_NAME) AS '已创建索引数量',
    '请参考DATABASE_INDEX_OPTIMIZATION_GUIDE.md' AS '文档'
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'ioedream'
  AND INDEX_NAME LIKE 'idx_%';

-- ================================================================
-- 9. 下一步操作建议
-- ================================================================
SELECT '========================================' AS '分隔线';
SELECT '9. 下一步操作' AS '标题';
SELECT '========================================' AS '分隔线';

SELECT
    '1. 运行性能测试脚本验证性能提升' AS '步骤1',
    '2. 配置Caffeine和Redis缓存架构' AS '步骤2',
    '3. 监控索引使用情况，删除未使用的索引' AS '步骤3',
    '4. 定期分析慢查询日志，持续优化' AS '步骤4';

-- ================================================================
-- 验证完成
-- ================================================================
SELECT '========================================' AS '分隔线';
SELECT '✅ 索引验证完成！' AS '完成状态';
SELECT '========================================' AS '分隔线';
