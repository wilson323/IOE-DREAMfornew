-- SmartAdmin v3 生物识别系统模块数据库建表脚本
-- 创建时间: 2025-01-15
-- 作用: 为智能门禁多模态生物识别系统创建所需的数据表

-- ================================
-- 1. 生物特征模板表 (t_biometric_templates)
-- ================================
DROP TABLE IF EXISTS `t_biometric_templates`;
CREATE TABLE `t_biometric_templates` (
    `template_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `employee_id` BIGINT(20) NOT NULL COMMENT '员工ID',
    `biometric_type` VARCHAR(50) NOT NULL COMMENT '生物识别类型: FACE-人脸, FINGERPRINT-指纹, PALMPRINT-掌纹, IRIS-虹膜',
    `template_version` VARCHAR(20) NOT NULL DEFAULT '1.0' COMMENT '模板版本号',
    `template_data` LONGTEXT NOT NULL COMMENT '模板数据（SM4加密存储）',
    `quality_metrics` JSON COMMENT '质量指标（图像质量、特征点数量等）',
    `enroll_date` DATE NOT NULL COMMENT '注册日期',
    `last_update_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新日期',
    `template_status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用，2-过期',
    `security_metadata` JSON COMMENT '安全元数据（加密算法、密钥版本等）',
    `create_user_id` BIGINT(20) NULL COMMENT '创建人ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0-正常，1-删除',
    PRIMARY KEY (`template_id`),
    UNIQUE KEY `uk_employee_biometric_type` (`employee_id`, `biometric_type`, `deleted_flag`),
    KEY `idx_employee_id` (`employee_id`),
    KEY `idx_biometric_type` (`biometric_type`),
    KEY `idx_template_status` (`template_status`),
    KEY `idx_enroll_date` (`enroll_date`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='生物特征模板表';

-- ================================
-- 2. 生物识别记录表 (t_biometric_records)
-- ================================
DROP TABLE IF EXISTS `t_biometric_records`;
CREATE TABLE `t_biometric_records` (
    `record_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `employee_id` BIGINT(20) NULL COMMENT '员工ID',
    `device_id` BIGINT(20) NOT NULL COMMENT '设备ID（关联smart_access_device）',
    `biometric_type` VARCHAR(50) NOT NULL COMMENT '生物识别类型: FACE-人脸, FINGERPRINT-指纹, PALMPRINT-掌纹, IRIS-虹膜',
    `verification_result` VARCHAR(50) NOT NULL COMMENT '验证结果：success-成功, failure-失败, timeout-超时, liveness_failed-活体检测失败',
    `confidence_score` DECIMAL(5,4) NULL COMMENT '置信度分数（0.0000-1.0000）',
    `processing_time` INT NULL COMMENT '处理时间(毫秒)',
    `feature_vectors` JSON COMMENT '特征向量（加密存储）',
    `verification_metadata` JSON COMMENT '验证元数据（算法版本、设备信息等）',
    `failure_reason` VARCHAR(500) NULL COMMENT '失败原因详细描述',
    `record_time` DATETIME NOT NULL COMMENT '记录时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0-正常，1-删除',
    PRIMARY KEY (`record_id`),
    KEY `idx_employee_device` (`employee_id`, `device_id`),
    KEY `idx_device_time` (`device_id`, `record_time`),
    KEY `idx_biometric_type_time` (`biometric_type`, `record_time`),
    KEY `idx_verification_result` (`verification_result`),
    KEY `idx_record_time` (`record_time`),
    KEY `idx_confidence_score` (`confidence_score`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='生物识别记录表';

-- ================================
-- 3. 认证策略配置表 (t_authentication_strategies)
-- ================================
DROP TABLE IF EXISTS `t_authentication_strategies`;
CREATE TABLE `t_authentication_strategies` (
    `strategy_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `strategy_name` VARCHAR(100) NOT NULL COMMENT '策略名称',
    `security_level` VARCHAR(50) NOT NULL COMMENT '安全级别：LOW-低, MEDIUM-中, HIGH-高, CRITICAL-关键',
    `strategy_config` JSON NOT NULL COMMENT '策略配置JSON（包含所需生物识别类型、阈值、备用方案等）',
    `device_types` VARCHAR(200) NULL COMMENT '适用设备类型（逗号分隔：DOOR,GATE,TURNSTILE）',
    `effective_start_time` TIME NULL COMMENT '生效开始时间',
    `effective_end_time` TIME NULL COMMENT '生效结束时间',
    `is_enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
    `description` TEXT NULL COMMENT '策略描述',
    `create_user_id` BIGINT(20) NULL COMMENT '创建人ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0-正常，1-删除',
    PRIMARY KEY (`strategy_id`),
    UNIQUE KEY `uk_strategy_name` (`strategy_name`, `deleted_flag`),
    KEY `idx_security_level` (`security_level`),
    KEY `idx_is_enabled` (`is_enabled`),
    KEY `idx_device_types` (`device_types`(191)),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='认证策略配置表';

-- ================================
-- 4. 设备生物识别配置表 (t_device_biometric_config)
-- ================================
DROP TABLE IF EXISTS `t_device_biometric_config`;
CREATE TABLE `t_device_biometric_config` (
    `config_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `device_id` BIGINT(20) NOT NULL COMMENT '设备ID（关联smart_access_device）',
    `supported_biometric_types` VARCHAR(200) NOT NULL COMMENT '支持的生物识别类型（逗号分隔）',
    `biometric_config` JSON NOT NULL COMMENT '生物识别配置JSON（算法参数、阈值设置等）',
    `liveness_detection_enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用活体检测：0-禁用，1-启用',
    `liveness_config` JSON NULL COMMENT '活体检测配置（检测级别、动作要求等）',
    `biometric_status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '生物识别状态：0-离线，1-在线，2-故障',
    `last_calibration_date` DATE NULL COMMENT '最后校准日期',
    `calibration_interval_days` INT NOT NULL DEFAULT 30 COMMENT '校准间隔天数',
    `accuracy_threshold` DECIMAL(5,4) NOT NULL DEFAULT 0.9500 COMMENT '准确率阈值',
    `max_retry_count` INT NOT NULL DEFAULT 3 COMMENT '最大重试次数',
    `timeout_seconds` INT NOT NULL DEFAULT 30 COMMENT '超时时间（秒）',
    `create_user_id` BIGINT(20) NULL COMMENT '创建人ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0-正常，1-删除',
    PRIMARY KEY (`config_id`),
    UNIQUE KEY `uk_device_id` (`device_id`, `deleted_flag`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_biometric_status` (`biometric_status`),
    KEY `idx_last_calibration_date` (`last_calibration_date`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备生物识别配置表';

-- ================================
-- 5. 多模态认证记录表 (t_multimodal_authentication_records)
-- ================================
DROP TABLE IF EXISTS `t_multimodal_authentication_records`;
CREATE TABLE `t_multimodal_authentication_records` (
    `multimodal_record_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `session_id` VARCHAR(100) NOT NULL COMMENT '多模态认证会话ID',
    `employee_id` BIGINT(20) NULL COMMENT '员工ID',
    `device_id` BIGINT(20) NOT NULL COMMENT '设备ID',
    `strategy_id` BIGINT(20) NOT NULL COMMENT '认证策略ID',
    `security_level` VARCHAR(50) NOT NULL COMMENT '安全级别：LOW-低, MEDIUM-中, HIGH-高, CRITICAL-关键',
    `required_biometric_types` VARCHAR(200) NOT NULL COMMENT '必需的生物识别类型',
    `actual_biometric_types` VARCHAR(200) NULL COMMENT '实际使用的生物识别类型',
    `overall_result` VARCHAR(50) NOT NULL COMMENT '整体认证结果：SUCCESS-成功, FAILURE-失败, TIMEOUT-超时',
    `fusion_confidence` DECIMAL(5,4) NULL COMMENT '融合置信度',
    `total_processing_time` INT NULL COMMENT '总处理时间(毫秒)',
    `strategy_execution_details` JSON NULL COMMENT '策略执行详情',
    `fallback_used` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否使用备用方案：0-否，1-是',
    `risk_score` DECIMAL(5,4) NULL COMMENT '风险评估分数',
    `authentication_start_time` DATETIME NOT NULL COMMENT '认证开始时间',
    `authentication_end_time` DATETIME NOT NULL COMMENT '认证结束时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记：0-正常，1-删除',
    PRIMARY KEY (`multimodal_record_id`),
    UNIQUE KEY `uk_session_id` (`session_id`),
    KEY `idx_employee_device` (`employee_id`, `device_id`),
    KEY `idx_strategy_id` (`strategy_id`),
    KEY `idx_security_level` (`security_level`),
    KEY `idx_overall_result` (`overall_result`),
    KEY `idx_authentication_time` (`authentication_start_time`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='多模态认证记录表';

-- ================================
-- 6. 多模态认证明细表 (t_multimodal_authentication_details)
-- ================================
DROP TABLE IF EXISTS `t_multimodal_authentication_details`;
CREATE TABLE `t_multimodal_authentication_details` (
    `detail_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `multimodal_record_id` BIGINT(20) NOT NULL COMMENT '多模态认证记录ID',
    `biometric_type` VARCHAR(50) NOT NULL COMMENT '生物识别类型',
    `individual_result` VARCHAR(50) NOT NULL COMMENT '单项认证结果：SUCCESS-成功, FAILURE-失败, SKIPPED-跳过',
    `confidence_score` DECIMAL(5,4) NULL COMMENT '置信度分数',
    `processing_time` INT NULL COMMENT '处理时间(毫秒)',
    `liveness_result` VARCHAR(50) NULL COMMENT '活体检测结果：PASS-通过, FAIL-失败, SKIPPED-跳过',
    `algorithm_version` VARCHAR(50) NULL COMMENT '算法版本',
    `template_version` VARCHAR(20) NULL COMMENT '使用的模板版本',
    `is_fallback` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否为备用方案：0-否，1-是',
    `failure_reason` VARCHAR(500) NULL COMMENT '失败原因',
    `feature_match_details` JSON NULL COMMENT '特征匹配详情',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`detail_id`),
    KEY `idx_multimodal_record_id` (`multimodal_record_id`),
    KEY `idx_biometric_type` (`biometric_type`),
    KEY `idx_individual_result` (`individual_result`),
    KEY `idx_liveness_result` (`liveness_result`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='多模态认证明细表';

-- ================================
-- 7. 外键约束
-- ================================
-- 设备生物识别配置表外键约束
ALTER TABLE `t_device_biometric_config`
ADD CONSTRAINT `fk_config_device_id` FOREIGN KEY (`device_id`) REFERENCES `smart_access_device` (`device_id`) ON DELETE CASCADE;

-- 多模态认证记录表外键约束
ALTER TABLE `t_multimodal_authentication_records`
ADD CONSTRAINT `fk_multimodal_strategy_id` FOREIGN KEY (`strategy_id`) REFERENCES `t_authentication_strategies` (`strategy_id`) ON DELETE CASCADE;

-- 多模态认证明细表外键约束
ALTER TABLE `t_multimodal_authentication_details`
ADD CONSTRAINT `fk_detail_multimodal_record_id` FOREIGN KEY (`multimodal_record_id`) REFERENCES `t_multimodal_authentication_records` (`multimodal_record_id`) ON DELETE CASCADE;

-- ================================
-- 8. 插入初始化数据
-- ================================
-- 插入默认认证策略
INSERT INTO `t_authentication_strategies`
(`strategy_name`, `security_level`, `strategy_config`, `device_types`, `description`, `create_user_id`) VALUES
('低安全级别策略', 'LOW', JSON_OBJECT(
    'required_biometric_types', JSON_ARRAY('FACE'),
    'confidence_threshold', 0.8000,
    'liveness_detection_required', false,
    'max_retry_count', 3,
    'timeout_seconds', 10,
    'fallback_types', JSON_ARRAY('FINGERPRINT')
), 'DOOR,GATE,TURNSTILE', '适用于一般区域，支持单一生物识别认证', 1),

('中安全级别策略', 'MEDIUM', JSON_OBJECT(
    'required_biometric_types', JSON_ARRAY('FACE'),
    'confidence_threshold', 0.9000,
    'liveness_detection_required', true,
    'max_retry_count', 3,
    'timeout_seconds', 20,
    'fallback_types', JSON_ARRAY('FINGERPRINT'),
    'liveness_config', JSON_OBJECT('actions', JSON_ARRAY('BLINK'), 'difficulty_level', 'MEDIUM')
), 'DOOR,GATE', '适用于办公区域，要求活体检测', 1),

('高安全级别策略', 'HIGH', JSON_OBJECT(
    'required_biometric_types', JSON_ARRAY('FACE', 'FINGERPRINT'),
    'confidence_threshold', 0.9500,
    'liveness_detection_required', true,
    'max_retry_count', 2,
    'timeout_seconds', 30,
    'fallback_types', JSON_ARRAY('PALMPRINT'),
    'fusion_strategy', 'WEIGHTED_AVERAGE',
    'liveness_config', JSON_OBJECT('actions', JSON_ARRAY('BLINK', 'HEAD_TURN'), 'difficulty_level', 'HIGH')
), 'DOOR,TURNSTILE', '适用于重要区域，要求双因子认证', 1),

('关键安全级别策略', 'CRITICAL', JSON_OBJECT(
    'required_biometric_types', JSON_ARRAY('FACE', 'FINGERPRINT', 'IRIS'),
    'confidence_threshold', 0.9900,
    'liveness_detection_required', true,
    'max_retry_count', 1,
    'timeout_seconds', 45,
    'fallback_types', JSON_ARRAY('PALMPRINT'),
    'fusion_strategy', 'CONSENSUS',
    'risk_assessment_enabled', true,
    'liveness_config', JSON_OBJECT('actions', JSON_ARRAY('BLINK', 'HEAD_TURN', 'MOUTH_MOVEMENT'), 'difficulty_level', 'CRITICAL')
), 'TURNSTILE', '适用于关键区域，要求三因子认证', 1);

-- 为现有门禁设备添加生物识别配置
INSERT INTO `t_device_biometric_config`
(`device_id`, `supported_biometric_types`, `biometric_config`, `liveness_detection_enabled`, `accuracy_threshold`, `create_user_id`)
SELECT
    device_id,
    CASE
        WHEN device_type = 'DOOR' THEN 'FACE,FINGERPRINT'
        WHEN device_type = 'GATE' THEN 'FACE'
        WHEN device_type = 'TURNSTILE' THEN 'FACE,FINGERPRINT,PALMPRINT'
        ELSE 'FACE'
    END,
    JSON_OBJECT(
        'face_algorithm', 'OPENCV_DNN',
        'fingerprint_algorithm', 'MINUTIAE_BASED',
        'palmprint_algorithm', 'GABOR_FILTER',
        'iris_algorithm', 'DAUGMAN',
        'feature_extraction_level', 'HIGH'
    ),
    CASE
        WHEN device_type = 'TURNSTILE' THEN 1
        ELSE 1
    END,
    CASE
        WHEN device_type = 'TURNSTILE' THEN 0.9900
        WHEN device_type = 'DOOR' THEN 0.9500
        ELSE 0.9000
    END,
    1
FROM `smart_access_device`
WHERE deleted_flag = 0;

-- ================================
-- 9. 创建索引优化查询性能
-- ================================
-- 复合索引优化
CREATE INDEX idx_template_employee_status_version ON `t_biometric_templates`(`employee_id`, `template_status`, `template_version`);
CREATE INDEX idx_record_device_result_time ON `t_biometric_records`(`device_id`, `verification_result`, `record_time`);
CREATE INDEX idx_strategy_level_enabled ON `t_authentication_strategies`(`security_level`, `is_enabled`);
CREATE INDEX idx_config_device_status ON `t_device_biometric_config`(`device_id`, `biometric_status`);
CREATE INDEX idx_multimodal_session_result ON `t_multimodal_authentication_records`(`session_id`, `overall_result`);
CREATE INDEX idx_multimodal_employee_time ON `t_multimodal_authentication_records`(`employee_id`, `authentication_start_time`);
CREATE INDEX idx_detail_multimodal_type_result ON `t_multimodal_authentication_details`(`multimodal_record_id`, `biometric_type`, `individual_result`);

-- ================================
-- 10. 创建视图便于查询
-- ================================
-- 生物识别统计视图
CREATE OR REPLACE VIEW `v_biometric_statistics` AS
SELECT
    biometric_type,
    COUNT(*) as total_records,
    COUNT(CASE WHEN verification_result = 'success' THEN 1 END) as success_count,
    COUNT(CASE WHEN verification_result = 'failure' THEN 1 END) as failure_count,
    COUNT(CASE WHEN verification_result = 'liveness_failed' THEN 1 END) as liveness_failure_count,
    ROUND(AVG(CASE WHEN confidence_score IS NOT NULL THEN confidence_score END), 4) as avg_confidence,
    ROUND(AVG(CASE WHEN processing_time IS NOT NULL THEN processing_time END), 2) as avg_processing_time,
    MIN(record_time) as earliest_record,
    MAX(record_time) as latest_record
FROM `t_biometric_records`
WHERE deleted_flag = 0
GROUP BY biometric_type;

-- 多模态认证统计视图
CREATE OR REPLACE VIEW `v_multimodal_authentication_summary` AS
SELECT
    security_level,
    COUNT(*) as total_attempts,
    COUNT(CASE WHEN overall_result = 'SUCCESS' THEN 1 END) as successful_authentications,
    COUNT(CASE WHEN overall_result = 'FAILURE' THEN 1 END) as failed_authentications,
    COUNT(CASE WHEN fallback_used = 1 THEN 1 END) as fallback_used_count,
    ROUND(AVG(CASE WHEN fusion_confidence IS NOT NULL THEN fusion_confidence END), 4) as avg_fusion_confidence,
    ROUND(AVG(CASE WHEN total_processing_time IS NOT NULL THEN total_processing_time END), 2) as avg_processing_time
FROM `t_multimodal_authentication_records`
WHERE deleted_flag = 0
GROUP BY security_level;

-- 设备生物识别能力概览视图
CREATE OR REPLACE VIEW `v_device_biometric_capability` AS
SELECT
    d.device_id,
    d.device_no,
    d.device_name,
    d.device_type,
    d.location,
    d.device_status,
    dbc.supported_biometric_types,
    dbc.liveness_detection_enabled,
    dbc.biometric_status,
    dbc.accuracy_threshold,
    dbc.last_calibration_date,
    CASE
        WHEN d.device_status = 'ONLINE' AND dbc.biometric_status = 1 THEN '正常工作'
        WHEN d.device_status = 'OFFLINE' THEN '设备离线'
        WHEN dbc.biometric_status = 2 THEN '生物识别故障'
        ELSE '异常状态'
    END AS capability_status
FROM `smart_access_device` d
LEFT JOIN `t_device_biometric_config` dbc ON d.device_id = dbc.device_id AND dbc.deleted_flag = 0
WHERE d.deleted_flag = 0;

-- ================================
-- 脚本执行完成
-- ================================
-- 执行时间: 2025-01-15
-- 说明: 智能门禁多模态生物识别系统数据库表结构创建完成
-- 包含: 生物特征模板、识别记录、认证策略、设备配置、多模态认证记录等完整功能