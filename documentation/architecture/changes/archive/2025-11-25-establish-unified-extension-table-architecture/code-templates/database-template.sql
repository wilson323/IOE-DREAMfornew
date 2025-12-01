-- ====================================================================
-- {base_entity_comment}{module_name}扩展表建表脚本模板
-- 基于现有数据库设计最佳实践
-- 创建时间: {create_date}
-- 描述: 为{base_entity_comment}模块创建{module_name}功能的扩展表支持
-- ====================================================================

-- 1. {base_entity_comment}{module_name}扩展表
-- 基于扩展表机制实现{module_name}功能的特定需求
-- 避免在基础表中添加模块特定字段，保持基础表的通用性

DROP TABLE IF EXISTS t_{base_table}_{module}_ext;

CREATE TABLE t_{base_table}_{module}_ext (
    -- 主键字段
    ext_id BIGINT AUTO_INCREMENT COMMENT '扩展ID（主键）',

    -- 关联字段（与基础表主键保持一致）
    {base_table}_id BIGINT NOT NULL COMMENT '关联{base_entity_comment}ID',

    -- 业务特有字段
    {module}_level INT DEFAULT 1 COMMENT '{module_name}等级：1-基础，2-高级，3-高级',
    {module}_mode VARCHAR(100) DEFAULT NULL COMMENT '{module_name}模式（JSON数组，支持多模式）',
    special_required TINYINT(1) DEFAULT 0 COMMENT '是否需要特殊处理：0-否，1-是',
    priority INT DEFAULT 1 COMMENT '优先级（数字越大优先级越高）',
    {module}_status TINYINT(1) DEFAULT 1 COMMENT '{module_name}状态：0-禁用，1-启用，2-维护中',

    -- JSON配置字段（避免字段冗余）
    time_restrictions TEXT COMMENT '时间限制配置（JSON格式）',
    location_rules TEXT COMMENT '位置限制配置（JSON格式）',
    alert_config TEXT COMMENT '告警配置（JSON格式）',
    device_linkage_rules TEXT COMMENT '设备联动配置（JSON格式）',
    extension_config TEXT COMMENT '扩展配置（JSON格式，用于未来扩展）',

    -- 审计字段（统一继承，无需重复定义 createTime, updateTime, deleted_flag 等）

    PRIMARY KEY (ext_id),

    -- 基础索引
    KEY idx_{base_table}_{module}_id ({base_table}_id, deleted_flag),
    KEY idx_{module}_level ({module}_level, deleted_flag),
    KEY idx_{module}_status ({module}_status, deleted_flag),
    KEY idx_priority (priority, deleted_flag),
    KEY idx_special_required (special_required, deleted_flag),

    -- 复合索引（基于现有查询模式优化）
    KEY idx_{base_table}_level_status ({base_table}_id, {module}_level, {module}_status, deleted_flag),
    KEY idx_priority_status ({module}_status, priority, deleted_flag),

    -- JSON配置索引（MySQL 5.7+支持）
    KEY idx_time_restrictions ((CAST(time_restrictions AS CHAR(255)))),
    KEY idx_alert_config ((CAST(alert_config AS CHAR(255))))

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='{base_entity_comment}{module_name}扩展表';

-- 2. 外键关联（可选，根据业务需要决定是否启用）
-- 基于现有项目实践，通常使用应用层关联检查而非数据库外键约束
-- 如需启用，请取消注释以下语句：

-- ALTER TABLE t_{base_table}_{module}_ext
-- ADD CONSTRAINT fk_{base_table}_{module}_ext_{base_table}
-- FOREIGN KEY ({base_table}_id) REFERENCES t_{base_table}({base_table}_id)
-- ON DELETE CASCADE ON UPDATE CASCADE;

-- 3. 初始化默认配置（基于现有最佳实践）
-- 为高频使用的{base_entity_comment}创建默认扩展配置

-- 插入默认配置示例（根据实际业务需求调整）
INSERT INTO t_{base_table}_{module}_ext (
    {base_table}_id, {module}_level, {module}_mode, special_required,
    priority, {module}_status, time_restrictions, alert_config,
    create_time, update_time, create_user_id, update_user_id, deleted_flag, version
)
SELECT
    {base_table}_id,
    1, -- 默认基础等级
    '["basic"]', -- 默认基础模式
    0, -- 默认不需要特殊处理
    1, -- 默认优先级
    1, -- 默认启用状态
    '{"workdays": ["00:00-23:59"], "weekends": ["00:00-23:59"]}', -- 默认全天候
    '{"enabled": false, "threshold": 3}', -- 默认告警关闭
    UNIX_TIMESTAMP()*1000, -- 创建时间
    UNIX_TIMESTAMP()*1000, -- 更新时间
    1, -- 系统用户ID
    1, -- 系统用户ID
    0, -- 未删除
    1  -- 版本号
FROM t_{base_table}
WHERE deleted_flag = 0
AND status = 1 -- 只为启用的{base_entity_comment}创建默认配置
LIMIT 10; -- 限制数量，避免性能问题

-- 4. 性能优化查询（基于现有查询分析）
-- 创建常用查询的视图，简化复杂查询

CREATE OR REPLACE VIEW v_{base_table}_{module}_full AS
SELECT
    -- 基础字段
    base.{base_table}_id,
    base.{entity_code_field},
    base.{entity_name_field},
    base.status as base_status,
    base.parent_id,
    base.path,
    base.level,

    -- 扩展字段
    ext.ext_id,
    ext.{module}_level,
    ext.{module}_mode,
    ext.special_required,
    ext.priority,
    ext.{module}_status as ext_status,
    ext.time_restrictions,
    ext.location_rules,
    ext.alert_config,
    ext.device_linkage_rules,
    ext.extension_config,

    -- 计算字段
    CASE
        WHEN ext.{module}_level >= 2 THEN 'high'
        WHEN ext.{module}_level >= 1 THEN 'medium'
        ELSE 'low'
    END as complexity_level,

    CASE
        WHEN ext.{module}_status = 1 THEN 'enabled'
        WHEN ext.{module}_status = 2 THEN 'maintenance'
        ELSE 'disabled'
    END as status_text,

    -- 时间字段
    ext.create_time as ext_create_time,
    ext.update_time as ext_update_time

FROM t_{base_table} base
LEFT JOIN t_{base_table}_{module}_ext ext ON base.{base_table}_id = ext.{base_table}_id
WHERE base.deleted_flag = 0;

-- 5. 统计和维护视图（基于现有统计需求）

CREATE OR REPLACE VIEW v_{base_table}_{module}_statistics AS
SELECT
    -- 基础统计
    COUNT(*) as total_count,
    COUNT(CASE WHEN ext.ext_id IS NOT NULL THEN 1 END) as extension_count,
    COUNT(CASE WHEN ext.{module}_level >= 2 THEN 1 END) as high_level_count,
    COUNT(CASE WHEN ext.special_required = 1 THEN 1 END) as special_required_count,

    -- 状态分布
    COUNT(CASE WHEN ext.{module}_status = 1 THEN 1 END) as enabled_count,
    COUNT(CASE WHEN ext.{module}_status = 0 THEN 1 END) as disabled_count,
    COUNT(CASE WHEN ext.{module}_status = 2 THEN 1 END) as maintenance_count,

    -- 配置复杂度分布
    COUNT(CASE WHEN ext.time_restrictions IS NOT NULL AND ext.location_rules IS NOT NULL AND ext.alert_config IS NOT NULL THEN 1 END) as high_complexity,
    COUNT(CASE WHEN (ext.time_restrictions IS NOT NULL OR ext.location_rules IS NOT NULL OR ext.alert_config IS NOT NULL) AND
                     (ext.time_restrictions IS NULL OR ext.location_rules IS NULL OR ext.alert_config IS NULL) THEN 1 END) as medium_complexity,
    COUNT(CASE WHEN ext.time_restrictions IS NULL AND ext.location_rules IS NULL AND ext.alert_config IS NULL THEN 1 END) as low_complexity

FROM t_{base_table} base
LEFT JOIN t_{base_table}_{module}_ext ext ON base.{base_table}_id = ext.{base_table}_id
WHERE base.deleted_flag = 0;

-- 6. 索引性能验证查询
-- 用于验证索引使用情况和查询性能

-- 验证基础索引
EXPLAIN SELECT * FROM t_{base_table}_{module}_ext
WHERE {base_table}_id = 1 AND deleted_flag = 0;

-- 验证等级查询索引
EXPLAIN SELECT * FROM t_{base_table}_{module}_ext
WHERE {module}_level = 2 AND deleted_flag = 0;

-- 验证状态查询索引
EXPLAIN SELECT * FROM t_{base_table}_{module}_ext
WHERE {module}_status = 1 AND priority > 5 AND deleted_flag = 0;

-- 验证关联查询优化
EXPLAIN SELECT base.{base_table}_id, base.{entity_name_field}, ext.{module}_level
FROM t_{base_table} base
INNER JOIN t_{base_table}_{module}_ext ext ON base.{base_table}_id = ext.{base_table}_id
WHERE base.deleted_flag = 0 AND ext.{module}_status = 1;

-- 7. 数据清理和维护脚本
-- 定期清理孤立记录和优化表结构

-- 清理孤立的扩展记录
DELETE ext FROM t_{base_table}_{module}_ext ext
LEFT JOIN t_{base_table} base ON ext.{base_table}_id = base.{base_table}_id
WHERE base.{base_table}_id IS NULL;

-- 更新统计信息
ANALYZE TABLE t_{base_table}_{module}_ext;

-- 优化表结构
OPTIMIZE TABLE t_{base_table}_{module}_ext;

-- 8. 配置示例和测试数据
-- 为开发和测试提供示例配置

-- 示例时间限制配置
UPDATE t_{base_table}_{module}_ext
SET time_restrictions = JSON_OBJECT(
    'workdays', JSON_ARRAY('07:00-09:00', '17:00-19:00'),
    'weekends', JSON_ARRAY('09:00-21:00'),
    'holidays', JSON_ARRAY('全天候')
)
WHERE {base_table}_id IN (SELECT {base_table}_id FROM t_{base_table} LIMIT 3);

-- 示例位置限制配置
UPDATE t_{base_table}_{module}_ext
SET location_rules = JSON_OBJECT(
    'latitude', 39.9042,
    'longitude', 116.4074,
    'radius', 100,
    'unit', 'meter'
)
WHERE {base_table}_id IN (SELECT {base_table}_id FROM t_{base_table} LIMIT 3);

-- 示例告警配置
UPDATE t_{base_table}_{module}_ext
SET alert_config = JSON_OBJECT(
    'enabled', true,
    'threshold', 3,
    'notification', JSON_ARRAY('email', 'sms', 'system'),
    'cooldown', 300
)
WHERE {base_table}_id IN (SELECT {base_table}_id FROM t_{base_table} LIMIT 3);

-- 示例设备联动配置
UPDATE t_{base_table}_{module}_ext
SET device_linkage_rules = JSON_OBJECT(
    'primaryDevice', 'camera',
    'validationOrder', JSON_ARRAY(1, 2, 3),
    'requiredDevices', JSON_ARRAY('sensor', 'reader', 'controller'),
    'timeout', 5000
)
WHERE {base_table}_id IN (SELECT {base_table}_id FROM t_{base_table} LIMIT 3);

-- 9. 性能基准测试
-- 用于验证数据库性能和索引效果

-- 基础查询性能测试（期望 < 5ms）
-- SELECT COUNT(*) FROM t_{base_table}_{module}_ext WHERE {base_table}_id = ? AND deleted_flag = 0;

-- 批量查询性能测试（期望 < 50ms）
-- SELECT * FROM t_{base_table}_{module}_ext WHERE {base_table}_id IN (?, ?, ?, ?) AND deleted_flag = 0;

-- 复杂关联查询性能测试（期望 < 100ms）
-- SELECT base.{base_table}_id, ext.{module}_level
-- FROM t_{base_table} base
-- INNER JOIN t_{base_table}_{module}_ext ext ON base.{base_table}_id = ext.{base_table}_id
-- WHERE base.deleted_flag = 0 AND ext.{module}_level >= 2;

-- 10. 监控和维护建议
-- 基于现有运维经验的监控指标

-- 表大小监控
SELECT
    table_name,
    ROUND(data_length/1024/1024, 2) AS 'Data Size(MB)',
    ROUND(index_length/1024/1024, 2) AS 'Index Size(MB)',
    ROUND((data_length + index_length)/1024/1024, 2) AS 'Total Size(MB)'
FROM information_schema.TABLES
WHERE table_schema = DATABASE()
AND table_name = 't_{base_table}_{module}_ext';

-- 索引使用情况监控
SELECT
    TABLE_NAME,
    INDEX_NAME,
    CARDINALITY,
    SUB_PART,
    NULLABLE,
    INDEX_TYPE
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME = 't_{base_table}_{module}_ext'
ORDER BY SEQ_IN_INDEX;

-- 查询性能监控
SELECT
    DIGEST_TEXT,
    COUNT_STAR,
    AVG_TIMER_WAIT/1000000000 AS avg_time_seconds,
    MAX_TIMER_WAIT/1000000000 AS max_time_seconds
FROM performance_schema.events_statements_summary_by_digest
WHERE DIGEST_TEXT LIKE '%t_{base_table}_{module}_ext%'
ORDER BY COUNT_STAR DESC
LIMIT 10;

COMMIT;