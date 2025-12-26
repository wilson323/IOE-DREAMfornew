-- ================================================================
-- IOE-DREAM 慢查询分析脚本
-- 用途: 分析数据库慢查询，识别需要优化的查询
-- 使用方法: mysql -u root -p ioedream < analyze_slow_queries.sql
-- 创建日期: 2025-01-XX
-- ================================================================

-- ================================================================
-- 1. 查看慢查询配置
-- ================================================================
SELECT
    VARIABLE_NAME,
    VARIABLE_VALUE
FROM performance_schema.variables
WHERE VARIABLE_NAME IN (
    'slow_query_log',
    'slow_query_log_file',
    'long_query_time',
    'log_queries_not_using_indexes'
);

-- ================================================================
-- 2. 分析最近的慢查询（从Performance Schema）
-- ================================================================
SELECT
    DIGEST_TEXT AS 'SQL语句',
    COUNT_STAR AS '执行次数',
    ROUND(AVG_TIMER_WAIT / 1000000000000, 2) AS '平均响应时间(s)',
    ROUND(MAX_TIMER_WAIT / 1000000000000, 2) AS '最大响应时间(s)',
    SUM_LOCK_TIME / 1000000000000 AS '总锁等待时间(s)',
    SUM_ROWS_EXAMINED AS '扫描行数',
    SUM_ROWS_SENT AS '返回行数',
    ROUND(SUM_ROWS_SENT / SUM_ROWS_EXAMINED * 100, 2) AS '效率(%)'
FROM performance_schema.events_statements_summary_by_digest
WHERE DIGEST_TEXT IS NOT NULL
  AND DIGEST_TEXT NOT LIKE '%performance_schema%'
ORDER BY AVG_TIMER_WAIT DESC
LIMIT 20;

-- ================================================================
-- 3. 查看全表扫描的查询（缺少索引）
-- ================================================================
SELECT
    DIGEST_TEXT AS 'SQL语句',
    COUNT_STAR AS '执行次数',
    ROUND(AVG_TIMER_WAIT / 1000000000000, 2) AS '平均响应时间(s)',
    SUM_ROWS_EXAMINED AS '扫描行数',
    SUM_ROWS_SENT AS '返回行数',
    ROUND(SUM_ROWS_SENT / SUM_ROWS_EXAMINED * 100, 2) AS '效率(%)'
FROM performance_schema.events_statements_summary_by_digest
WHERE DIGEST_TEXT IS NOT NULL
  AND SUM_ROWS_EXAMINED > 10000  -- 扫描超过1万行
  AND SUM_ROWS_SENT < SUM_ROWS_EXAMINED * 0.1  -- 返回少于10%
ORDER BY SUM_ROWS_EXAMINED DESC
LIMIT 20;

-- ================================================================
-- 4. 按表统计慢查询
-- ================================================================
SELECT
    OBJECT_SCHEMA AS '数据库',
    OBJECT_NAME AS '表名',
    COUNT_STAR AS '慢查询次数',
    ROUND(AVG_TIMER_WAIT / 1000000000000, 2) AS '平均响应时间(s)',
    SUM_ROWS_EXAMINED AS '总扫描行数',
    SUM_LOCK_TIME / 1000000000000 AS '总锁等待时间(s)'
FROM performance_schema.events_statements_summary_by_digest
WHERE OBJECT_SCHEMA = 'ioedream'
  AND OBJECT_NAME IS NOT NULL
GROUP BY OBJECT_SCHEMA, OBJECT_NAME
ORDER BY COUNT_STAR DESC
LIMIT 20;

-- ================================================================
-- 5. 查看当前正在执行的慢查询
-- ================================================================
SELECT
    ID AS '进程ID',
    USER AS '用户',
    HOST AS '主机',
    DB AS '数据库',
    COMMAND AS '命令类型',
    TIME AS '执行时长(s)',
    STATE AS '状态',
    LEFT(INFO, 100) AS 'SQL语句(前100字符)'
FROM information_schema.PROCESSLIST
WHERE COMMAND != 'Sleep'
  AND TIME > 1  -- 执行超过1秒
ORDER BY TIME DESC
LIMIT 20;

-- ================================================================
-- 6. 检查表缺失索引的情况
-- ================================================================
SELECT
    TABLES.TABLE_SCHEMA AS '数据库',
    TABLES.TABLE_NAME AS '表名',
    TABLES.TABLE_ROWS AS '行数',
    INDEX_STATISTICS.INDEX_COUNT AS '索引数量',
    CASE
        WHEN INDEX_STATISTICS.INDEX_COUNT = 0 THEN '❌ 无索引'
        WHEN INDEX_STATISTICS.INDEX_COUNT < 3 THEN '⚠️  索引不足'
        ELSE '✅ 索引正常'
    END AS '索引状态'
FROM information_schema.TABLES
LEFT JOIN (
    SELECT
        TABLE_SCHEMA,
        TABLE_NAME,
        COUNT(DISTINCT INDEX_NAME) AS INDEX_COUNT
    FROM information_schema.STATISTICS
    WHERE INDEX_NAME != 'PRIMARY'
    GROUP BY TABLE_SCHEMA, TABLE_NAME
) INDEX_STATISTICS
ON TABLES.TABLE_SCHEMA = INDEX_STATISTICS.TABLE_SCHEMA
AND TABLES.TABLE_NAME = INDEX_STATISTICS.TABLE_NAME
WHERE TABLES.TABLE_SCHEMA = 'ioedream'
  AND TABLES.TABLE_TYPE = 'BASE TABLE'
ORDER BY INDEX_STATISTICS.INDEX_COUNT ASC, TABLES.TABLE_ROWS DESC;

-- ================================================================
-- 7. 查看索引使用情况（识别未使用的索引）
-- ================================================================
SELECT
    OBJECT_SCHEMA AS '数据库',
    OBJECT_NAME AS '表名',
    INDEX_NAME AS '索引名',
    COUNT_STAR AS '使用次数',
    ROUND(AVG_TIMER_WAIT / 1000000000000, 2) AS '平均响应时间(s)'
FROM performance_schema.table_io_waits_summary_by_index_usage
WHERE OBJECT_SCHEMA = 'ioedream'
  AND INDEX_NAME IS NOT NULL
  AND INDEX_NAME != 'PRIMARY'
  AND COUNT_STAR > 0
ORDER BY COUNT_STAR DESC
LIMIT 50;

-- ================================================================
-- 8. 查看从未使用的索引（可考虑删除）
-- ================================================================
SELECT
    STATISTICS.TABLE_SCHEMA AS '数据库',
    STATISTICS.TABLE_NAME AS '表名',
    STATISTICS.INDEX_NAME AS '索引名',
    GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) AS '索引列'
FROM information_schema.STATISTICS
LEFT JOIN performance_schema.table_io_waits_summary_by_index_usage USAGE
ON STATISTICS.TABLE_SCHEMA = USAGE.OBJECT_SCHEMA
AND STATISTICS.TABLE_NAME = USAGE.OBJECT_NAME
AND STATISTICS.INDEX_NAME = USAGE.INDEX_NAME
WHERE STATISTICS.TABLE_SCHEMA = 'ioedream'
  AND STATISTICS.INDEX_NAME != 'PRIMARY'
  AND USAGE.INDEX_NAME IS NULL  -- 从未使用
GROUP BY STATISTICS.TABLE_SCHEMA, STATISTICS.TABLE_NAME, STATISTICS.INDEX_NAME
ORDER BY STATISTICS.TABLE_NAME, STATISTICS.INDEX_NAME;

-- ================================================================
-- 9. 分析表大小和碎片
-- ================================================================
SELECT
    TABLE_SCHEMA AS '数据库',
    TABLE_NAME AS '表名',
    ROUND(((DATA_LENGTH + INDEX_LENGTH) / 1024 / 1024), 2) AS '总大小(MB)',
    ROUND((DATA_LENGTH / 1024 / 1024), 2) AS '数据大小(MB)',
    ROUND((INDEX_LENGTH / 1024 / 1024), 2) AS '索引大小(MB)',
    TABLE_ROWS AS '行数',
    ROUND(DATA_LENGTH / TABLE_ROWS, 2) AS '平均行长度(字节)',
    ROUND(DATA_FREE / 1024 / 1024, 2) AS '碎片大小(MB)'
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'ioedream'
  AND TABLE_TYPE = 'BASE TABLE'
ORDER BY (DATA_LENGTH + INDEX_LENGTH) DESC
LIMIT 20;

-- ================================================================
-- 10. 生成索引优化建议
-- ================================================================
-- 查询需要添加索引的表（基于慢查询分析）
SELECT
    '建议添加索引' AS '优化建议',
    OBJECT_NAME AS '表名',
    DIGEST_TEXT AS 'SQL语句示例',
    ROUND(AVG_TIMER_WAIT / 1000000000000, 2) AS '平均响应时间(s)',
    '请查看DATABASE_INDEX_OPTIMIZATION_GUIDE.md' AS '参考文档'
FROM performance_schema.events_statements_summary_by_digest
WHERE OBJECT_SCHEMA = 'ioedream'
  AND OBJECT_NAME IS NOT NULL
  AND SUM_ROWS_EXAMINED > 10000
  AND SUM_ROWS_SENT < SUM_ROWS_EXAMINED * 0.1
ORDER BY AVG_TIMER_WAIT DESC
LIMIT 10;

-- ================================================================
-- 分析完成
-- ================================================================
SELECT '✅ 慢查询分析完成' AS '状态',
       '请查看上述结果并根据DATABASE_INDEX_OPTIMIZATION_GUIDE.md创建索引' AS '下一步操作';
