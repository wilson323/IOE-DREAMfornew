-- JSON字段优化脚本
-- 创建时间: 2025-11-16
-- 作用: 评估和优化JSON字段使用，提高查询性能
-- 基于repowiki规范：优化JSON字段使用，避免过度嵌套

-- ================================
-- 1. JSON字段使用分析
-- ================================

-- 查看所有包含JSON字段的表
SELECT
    TABLE_NAME,
    COLUMN_NAME,
    DATA_TYPE,
    COLUMN_COMMENT,
    CHARACTER_MAXIMUM_LENGTH
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
    AND DATA_TYPE = 'json'
    AND TABLE_NAME NOT LIKE 'information_schema%'
ORDER BY TABLE_NAME, COLUMN_NAME;

-- ================================
-- 2. JSON字段性能优化建议
-- ================================

-- 2.1 为生物特征模板表添加JSON字段索引
-- t_biometric_templates.quality_metrics 质量指标索引
ALTER TABLE t_biometric_templates
ADD COLUMN quality_score DECIMAL(5,4) GENERATED ALWAYS AS (
    JSON_UNQUOTE(JSON_EXTRACT(quality_metrics, '$.overall_score'))
) STORED COMMENT '质量总分-生成列';

-- 为质量分数创建索引
CREATE INDEX idx_template_quality_score ON t_biometric_templates(quality_score);

-- 2.2 为生物识别记录表添加JSON字段索引
-- t_biometric_records.feature_vectors 特征向量索引
ALTER TABLE t_biometric_records
ADD COLUMN feature_count INT GENERATED ALWAYS AS (
    JSON_LENGTH(feature_vectors)
) STORED COMMENT '特征向量数量-生成列';

-- 为特征数量创建索引
CREATE INDEX idx_record_feature_count ON t_biometric_records(feature_count);

-- 2.3 为认证策略表添加JSON字段索引
-- t_authentication_strategies.strategy_config 策略配置索引
ALTER TABLE t_authentication_strategies
ADD COLUMN security_level_num TINYINT GENERATED ALWAYS AS (
    CASE
        WHEN JSON_EXTRACT(strategy_config, '$.security_level') = '"LOW"' THEN 1
        WHEN JSON_EXTRACT(strategy_config, '$.security_level') = '"MEDIUM"' THEN 2
        WHEN JSON_EXTRACT(strategy_config, '$.security_level') = '"HIGH"' THEN 3
        WHEN JSON_EXTRACT(strategy_config, '$.security_level') = '"CRITICAL"' THEN 4
        ELSE 0
    END
) STORED COMMENT '安全级别数字-生成列';

-- 为安全级别创建索引
CREATE INDEX idx_strategy_security_level ON t_authentication_strategies(security_level_num);

-- ================================
-- 3. JSON字段查询优化示例
-- ================================

-- 3.1 优化前的查询（全表扫描）
-- SELECT * FROM t_biometric_templates
-- WHERE JSON_EXTRACT(quality_metrics, '$.overall_score') > 0.95;

-- 3.2 优化后的查询（使用生成列索引）
-- SELECT * FROM t_biometric_templates
-- WHERE quality_score > 0.95;

-- 3.3 为设备配置表添加JSON解析函数
DELIMITER //
CREATE FUNCTION extract_biometric_types(config_json JSON)
RETURNS VARCHAR(200)
READS SQL DATA
DETERMINISTIC
BEGIN
    RETURN JSON_UNQUOTE(JSON_EXTRACT(config_json, '$.supported_types'));
END //
DELIMITER ;

-- ================================
-- 4. JSON字段数据完整性验证
-- ================================

-- 检查JSON字段格式完整性
SELECT
    't_biometric_templates' AS table_name,
    COUNT(*) AS total_records,
    COUNT(CASE WHEN JSON_VALID(quality_metrics) = 1 THEN 1 END) AS valid_json_count,
    COUNT(CASE WHEN JSON_VALID(security_metadata) = 1 THEN 1 END) AS valid_metadata_count,
    COUNT(CASE
        WHEN JSON_EXTRACT(quality_metrics, '$.overall_score') IS NOT NULL
        AND JSON_EXTRACT(quality_metrics, '$.overall_score') REGEXP '^[0-9]+(\\.[0-9]+)?$'
        THEN 1
    END) AS valid_score_count
FROM t_biometric_templates
WHERE deleted_flag = 0
UNION ALL
SELECT
    't_biometric_records' AS table_name,
    COUNT(*) AS total_records,
    COUNT(CASE WHEN JSON_VALID(feature_vectors) = 1 THEN 1 END) AS valid_json_count,
    COUNT(CASE WHEN JSON_VALID(verification_metadata) = 1 THEN 1 END) AS valid_metadata_count,
    0 AS valid_score_count
FROM t_biometric_records
WHERE deleted_flag = 0;

-- ================================
-- 5. JSON字段使用规范文档
-- ================================

/*
JSON字段使用规范 (基于repowiki):

1. **合理使用场景**:
   - 配置数据: 系统配置、策略参数
   - 元数据: 加密算法、版本信息
   - 质量指标: 图像质量、特征点数量
   - 变长数据: 特征向量、检测结果

2. **避免过度嵌套**:
   - JSON嵌套层数 ≤ 3层
   - 单个JSON字段大小 ≤ 16KB
   - 避免存储大量二进制数据

3. **性能优化**:
   - 为频繁查询的JSON属性创建生成列
   - 使用JSON_EXTRACT提取特定字段
   - 建立适当的索引

4. **数据完整性**:
   - 使用JSON_VALID()验证格式
   - 建立JSON Schema验证
   - 提供默认值处理

5. **监控和维护**:
   - 定期检查JSON字段格式
   - 监控JSON字段大小增长
   - 优化查询性能
*/

-- ================================
-- 6. 性能监控查询
-- ================================

-- JSON字段大小统计
SELECT
    TABLE_NAME,
    COLUMN_NAME,
    ROUND(AVG(LENGTH(COLUMN_NAME) / 1024.0), 2) AS avg_size_kb,
    ROUND(MAX(LENGTH(COLUMN_NAME) / 1024.0), 2) AS max_size_kb,
    ROUND(AVG(JSON_LENGTH(COLUMN_NAME)), 2) AS avg_element_count
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
    AND DATA_TYPE = 'json'
GROUP BY TABLE_NAME, COLUMN_NAME;

-- ================================
-- 脚本执行完成
-- ================================
-- 执行时间: 2025-11-16
-- 说明: JSON字段优化完成，包含生成列索引、查询优化和数据验证
-- 建议: 定期运行数据完整性验证查询