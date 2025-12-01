-- =====================================================
-- 统一生物特征管理数据库设计
-- 以人为中心的生物特征统一管理架构
-- =====================================================

-- 1. 人员生物特征主表（核心表）
CREATE TABLE `t_person_biometric` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `person_id` BIGINT NOT NULL COMMENT '人员ID（关联人员表）',
    `person_type` VARCHAR(20) NOT NULL COMMENT '人员类型 EMPLOYEE-员工 VISITOR-访客',
    `person_name` VARCHAR(100) NOT NULL COMMENT '人员姓名（冗余字段便于查询）',
    `person_code` VARCHAR(50) NOT NULL COMMENT '人员编号（冗余字段便于查询）',
    `face_count` INT DEFAULT 0 COMMENT '人脸特征数量',
    `fingerprint_count` INT DEFAULT 0 COMMENT '指纹特征数量',
    `iris_count` INT DEFAULT 0 COMMENT '虹膜特征数量',
    `palmprint_count` INT DEFAULT 0 COMMENT '掌纹特征数量',
    `voice_count` INT DEFAULT 0 COMMENT '声纹特征数量',
    `overall_quality_score` DECIMAL(5,2) DEFAULT 0.00 COMMENT '整体质量分数',
    `enrollment_status` VARCHAR(20) DEFAULT 'INCOMPLETE' COMMENT '注册状态 COMPLETE-完整 INCOMPLETE-未完成 EXPIRED-已过期',
    `last_update_time` DATETIME NOT NULL COMMENT '最后更新时间',
    `enable_status` TINYINT(1) DEFAULT 1 COMMENT '启用状态 0-禁用 1-启用',
    `version` INT DEFAULT 1 COMMENT '版本号（乐观锁）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_user_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_user_id` BIGINT DEFAULT NULL COMMENT '更新人ID',
    `deleted_flag` TINYINT(1) DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_person_biometric` (`person_id`, `person_type`),
    KEY `idx_person_code` (`person_code`),
    KEY `idx_person_name` (`person_name`),
    KEY `idx_enrollment_status` (`enrollment_status`),
    KEY `idx_enable_status` (`enable_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='人员生物特征主表';

-- 2. 生物特征详细表（以特征类型为维度）
CREATE TABLE `t_biometric_template` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `person_id` BIGINT NOT NULL COMMENT '人员ID',
    `biometric_type` VARCHAR(20) NOT NULL COMMENT '生物特征类型 FACE-人脸 FINGERPRINT-指纹 IRIS-虹膜 PALMPRINT-掌纹 VOICE-声纹',
    `template_index` INT NOT NULL COMMENT '模板索引（同一类型的多个模板）',
    `template_data` LONGTEXT NOT NULL COMMENT '特征模板数据（SM4加密）',
    `template_version` VARCHAR(20) NOT NULL DEFAULT 'v1.0' COMMENT '模板版本',
    `algorithm_info` JSON NULL COMMENT '算法信息 {"algorithm":"FaceNet","version":"2.1","confidence":0.95}',
    `quality_score` DECIMAL(5,2) NOT NULL COMMENT '质量分数 0.00-1.00',
    `enrollment_device_id` BIGINT DEFAULT NULL COMMENT '注册设备ID',
    `enrollment_device_info` JSON NULL COMMENT '注册设备信息',
    `capture_time` DATETIME NOT NULL COMMENT '采集时间',
    `security_metadata` JSON NULL COMMENT '安全元数据 {"encryption":"SM4","keyVersion":"v1.0"}',
    `valid_from` DATETIME NOT NULL COMMENT '有效期开始时间',
    `valid_to` DATETIME NOT NULL COMMENT '有效期结束时间',
    `template_status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '模板状态 ACTIVE-活跃 INACTIVE-非活跃 EXPIRED-已过期 FROZEN-冻结',
    `usage_count` INT DEFAULT 0 COMMENT '使用次数',
    `last_used_time` DATETIME DEFAULT NULL COMMENT '最后使用时间',
    `failure_count` INT DEFAULT 0 COMMENT '失败次数',
    `is_primary` TINYINT(1) DEFAULT 0 COMMENT '是否主模板 0-否 1-是',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `version` INT DEFAULT 1 COMMENT '版本号（乐观锁）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_user_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_user_id` BIGINT DEFAULT NULL COMMENT '更新人ID',
    `deleted_flag` TINYINT(1) DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_biometric_template` (`person_id`, `biometric_type`, `template_index`),
    KEY `idx_person_biometric_type` (`person_id`, `biometric_type`),
    KEY `idx_template_status` (`template_status`),
    KEY `idx_validity_period` (`valid_from`, `valid_to`),
    KEY `idx_is_primary` (`is_primary`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='生物特征模板表';

-- 3. 设备生物特征关联表（定义设备需要哪些生物特征）
CREATE TABLE `t_device_biometric_mapping` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `device_id` BIGINT NOT NULL COMMENT '设备ID',
    `device_type` VARCHAR(20) NOT NULL COMMENT '设备类型 ACCESS-门禁 ATTENDANCE-考勤 CONSUME-消费 VIDEO-视频',
    `required_biometric_types` JSON NOT NULL COMMENT '需要的生物特征类型 ["FACE","FINGERPRINT"]',
    `biometric_quality_threshold` JSON NULL COMMENT '生物特征质量阈值 {"FACE":0.85,"FINGERPRINT":0.80}',
    `enable_fallback` TINYINT(1) DEFAULT 1 COMMENT '启用备选方案',
    `fallback_order` JSON NULL COMMENT '备选顺序 ["FACE","FINGERPRINT"]',
    `dispatch_status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '下发状态 PENDING-待下发 DISPATCHING-下发中 COMPLETED-已完成 FAILED-失败',
    `last_sync_time` DATETIME DEFAULT NULL COMMENT '最后同步时间',
    `sync_result` JSON NULL COMMENT '同步结果详情',
    `enable_status` TINYINT(1) DEFAULT 1 COMMENT '启用状态',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `version` INT DEFAULT 1 COMMENT '版本号（乐观锁）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_user_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_user_id` BIGINT DEFAULT NULL COMMENT '更新人ID',
    `deleted_flag` TINYINT(1) DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_device_biometric_mapping` (`device_id`),
    KEY `idx_device_type` (`device_type`),
    KEY `idx_dispatch_status` (`dispatch_status`),
    KEY `idx_enable_status` (`enable_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备生物特征关联表';

-- 4. 生物特征下发记录表（记录下发历史）
CREATE TABLE `t_biometric_dispatch_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `dispatch_id` VARCHAR(64) NOT NULL COMMENT '下发批次ID',
    `person_id` BIGINT NOT NULL COMMENT '人员ID',
    `device_id` BIGINT NOT NULL COMMENT '设备ID',
    `biometric_type` VARCHAR(20) NOT NULL COMMENT '生物特征类型',
    `template_ids` JSON NOT NULL COMMENT '下发的模板ID列表 [1,2,3]',
    `dispatch_mode` VARCHAR(20) NOT NULL COMMENT '下发模式 SINGLE-单条 BATCH-批量',
    `dispatch_status` VARCHAR(20) NOT NULL COMMENT '下发状态 PENDING-待下发 DISPATCHING-下发中 SUCCESS-成功 FAILED-失败',
    `dispatch_result` JSON NULL COMMENT '下发结果详情',
    `error_code` VARCHAR(50) DEFAULT NULL COMMENT '错误码',
    `error_message` TEXT DEFAULT NULL COMMENT '错误信息',
    `retry_count` INT DEFAULT 0 COMMENT '重试次数',
    `max_retry_count` INT DEFAULT 3 COMMENT '最大重试次数',
    `dispatch_time` DATETIME NOT NULL COMMENT '下发时间',
    `complete_time` DATETIME DEFAULT NULL COMMENT '完成时间',
    `execution_time_ms` INT DEFAULT NULL COMMENT '执行耗时（毫秒）',
    `business_type` VARCHAR(20) NOT NULL COMMENT '业务类型 ENROLLMENT-注册 UPDATE-更新 DELETE-删除',
    `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
    `operator_type` VARCHAR(20) DEFAULT 'SYSTEM' COMMENT '操作人类型 SYSTEM-系统 ADMIN-管理员',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dispatch_batch` (`dispatch_id`),
    KEY `idx_person_device` (`person_id`, `device_id`),
    KEY `idx_biometric_type` (`biometric_type`),
    KEY `idx_dispatch_status` (`dispatch_status`),
    KEY `idx_dispatch_time` (`dispatch_time`),
    KEY `idx_business_type` (`business_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='生物特征下发记录表';

-- 5. 生物特征识别记录表（记录识别历史）
CREATE TABLE `t_biometric_recognition_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `person_id` BIGINT DEFAULT NULL COMMENT '人员ID（识别成功时记录）',
    `device_id` BIGINT NOT NULL COMMENT '设备ID',
    `biometric_type` VARCHAR(20) NOT NULL COMMENT '生物特征类型',
    `recognition_mode` VARCHAR(20) NOT NULL COMMENT '识别模式 VERIFY-验证 SEARCH-搜索 IDENTIFY-识别',
    `recognition_result` VARCHAR(20) NOT NULL COMMENT '识别结果 SUCCESS-成功 FAILED-失败 TIMEOUT-超时 ERROR-错误',
    `confidence_score` DECIMAL(5,2) DEFAULT NULL COMMENT '置信度分数',
    `match_template_id` BIGINT DEFAULT NULL COMMENT '匹配的模板ID（识别成功时记录）',
    `feature_vector` LONGTEXT DEFAULT NULL COMMENT '提取的特征向量（加密存储）',
    `recognition_metadata` JSON NULL COMMENT '识别元数据',
    `processing_time_ms` INT DEFAULT NULL COMMENT '处理耗时（毫秒）',
    `liveness_result` VARCHAR(20) DEFAULT NULL COMMENT '活体检测结果',
    `liveness_score` DECIMAL(5,2) DEFAULT NULL COMMENT '活体检测分数',
    `security_alert` JSON NULL COMMENT '安全告警信息',
    `session_id` VARCHAR(64) DEFAULT NULL COMMENT '会话ID',
    `client_info` JSON NULL COMMENT '客户端信息',
    `record_time` DATETIME NOT NULL COMMENT '记录时间',
    `business_type` VARCHAR(20) NOT NULL COMMENT '业务类型 ACCESS-门禁 ATTENDANCE-考勤 CONSUME-消费 VIDEO-视频',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_person_device_time` (`person_id`, `device_id`, `record_time`),
    KEY `idx_biometric_type` (`biometric_type`),
    KEY `idx_recognition_result` (`recognition_result`),
    KEY `idx_confidence_score` (`confidence_score`),
    KEY `idx_record_time` (`record_time`),
    KEY `idx_business_type` (`business_type`),
    KEY `idx_session_id` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='生物特征识别记录表';

-- 6. 生物特征模板版本表（支持模板升级）
CREATE TABLE `t_biometric_template_version` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `person_id` BIGINT NOT NULL COMMENT '人员ID',
    `biometric_type` VARCHAR(20) NOT NULL COMMENT '生物特征类型',
    `template_index` INT NOT NULL COMMENT '模板索引',
    `old_template_id` BIGINT NOT NULL COMMENT '旧模板ID',
    `new_template_id` BIGINT NOT NULL COMMENT '新模板ID',
    `upgrade_reason` VARCHAR(200) NOT NULL COMMENT '升级原因',
    `upgrade_algorithm` JSON NOT NULL COMMENT '升级算法信息',
    `upgrade_quality_delta` DECIMAL(5,2) DEFAULT NULL COMMENT '质量提升幅度',
    `upgrade_time` DATETIME NOT NULL COMMENT '升级时间',
    `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
    `operator_type` VARCHAR(20) DEFAULT 'SYSTEM' COMMENT '操作人类型',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_person_biometric` (`person_id`, `biometric_type`, `template_index`),
    KEY `idx_old_template_id` (`old_template_id`),
    KEY `idx_new_template_id` (`new_template_id`),
    KEY `idx_upgrade_time` (`upgrade_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='生物特征模板版本表';

-- 创建索引优化查询性能
CREATE INDEX idx_person_biometric_composite ON t_person_biometric (person_id, person_type, enrollment_status, enable_status);
CREATE INDEX idx_biometric_template_composite ON t_biometric_template (person_id, biometric_type, template_status, valid_to);
CREATE INDEX idx_device_biometric_composite ON t_device_biometric_mapping (device_type, dispatch_status, enable_status);

-- 添加外键约束（可选，根据实际需要）
-- ALTER TABLE `t_person_biometric` ADD CONSTRAINT `fk_person_biometric_person` FOREIGN KEY (`person_id`) REFERENCES `t_person`(`person_id`);
-- ALTER TABLE `t_biometric_template` ADD CONSTRAINT `fk_biometric_template_person` FOREIGN KEY (`person_id`) REFERENCES `t_person_biometric`(`person_id`);
-- ALTER TABLE `t_device_biometric_mapping` ADD CONSTRAINT `fk_device_biometric_device` FOREIGN KEY (`device_id`) REFERENCES `t_smart_device`(`device_id`);
-- ALTER TABLE `t_biometric_dispatch_record` ADD CONSTRAINT `fk_dispatch_person` FOREIGN KEY (`person_id`) REFERENCES `t_person_biometric`(`person_id`);
-- ALTER TABLE `t_biometric_dispatch_record` ADD CONSTRAINT `fk_dispatch_device` FOREIGN KEY (`device_id`) REFERENCES `t_device_biometric_mapping`(`device_id`);
-- ALTER TABLE `t_biometric_recognition_record` ADD CONSTRAINT `fk_recognition_person` FOREIGN KEY (`person_id`) REFERENCES `t_person_biometric`(`person_id`);
-- ALTER TABLE `t_biometric_recognition_record` ADD CONSTRAINT `fk_recognition_device` FOREIGN KEY (`device_id`) REFERENCES `t_smart_device`(`device_id`);

-- 创建视图用于快速查询
CREATE VIEW `v_person_biometric_summary` AS
SELECT
    pb.person_id,
    pb.person_type,
    pb.person_name,
    pb.person_code,
    pb.face_count,
    pb.fingerprint_count,
    pb.iris_count,
    pb.palmprint_count,
    pb.voice_count,
    pb.overall_quality_score,
    pb.enrollment_status,
    pb.enable_status,
    pb.last_update_time,
    -- 最新的人脸特征质量
    (SELECT MAX(quality_score) FROM t_biometric_template
     WHERE person_id = pb.person_id AND biometric_type = 'FACE' AND deleted_flag = 0) AS latest_face_quality,
    -- 最新的指纹特征质量
    (SELECT MAX(quality_score) FROM t_biometric_template
     WHERE person_id = pb.person_id AND biometric_type = 'FINGERPRINT' AND deleted_flag = 0) AS latest_fingerprint_quality
FROM t_person_biometric pb
WHERE pb.deleted_flag = 0;

-- 创建存储过程用于统计更新
DELIMITER $$
CREATE PROCEDURE `update_person_biometric_counts`(
    IN p_person_id BIGINT,
    IN p_person_type VARCHAR(20)
)
BEGIN
    -- 更新人员生物特征数量统计
    UPDATE t_person_biometric SET
        face_count = (
            SELECT COUNT(*) FROM t_biometric_template
            WHERE person_id = p_person_id AND biometric_type = 'FACE'
            AND template_status = 'ACTIVE' AND deleted_flag = 0
        ),
        fingerprint_count = (
            SELECT COUNT(*) FROM t_biometric_template
            WHERE person_id = p_person_id AND biometric_type = 'FINGERPRINT'
            AND template_status = 'ACTIVE' AND deleted_flag = 0
        ),
        iris_count = (
            SELECT COUNT(*) FROM t_biometric_template
            WHERE person_id = p_person_id AND biometric_type = 'IRIS'
            AND template_status = 'ACTIVE' AND deleted_flag = 0
        ),
        palmprint_count = (
            SELECT COUNT(*) FROM t_biometric_template
            WHERE person_id = p_person_id AND biometric_type = 'PALMPRINT'
            AND template_status = 'ACTIVE' AND deleted_flag = 0
        ),
        voice_count = (
            SELECT COUNT(*) FROM t_biometric_template
            WHERE person_id = p_person_id AND biometric_type = 'VOICE'
            AND template_status = 'ACTIVE' AND deleted_flag = 0
        ),
        overall_quality_score = (
            SELECT AVG(quality_score) FROM t_biometric_template
            WHERE person_id = p_person_id AND deleted_flag = 0
        ),
        enrollment_status = CASE
            WHEN (
                (SELECT COUNT(*) FROM t_biometric_template
                 WHERE person_id = p_person_id AND biometric_type IN ('FACE', 'FINGERPRINT')
                 AND template_status = 'ACTIVE' AND deleted_flag = 0) >= 1
            ) THEN 'COMPLETE'
            WHEN (
                (SELECT COUNT(*) FROM t_biometric_template
                 WHERE person_id = p_person_id AND deleted_flag = 0) > 0
            ) THEN 'INCOMPLETE'
            ELSE 'EMPTY'
        END,
        last_update_time = CURRENT_TIMESTAMP,
        update_time = CURRENT_TIMESTAMP
    WHERE person_id = p_person_id AND person_type = p_person_type AND deleted_flag = 0;
END$$
DELIMITER ;

-- 插入示例数据（用于测试）
INSERT INTO t_person_biometric (person_id, person_type, person_name, person_code, enrollment_status, create_time) VALUES
(1001, 'EMPLOYEE', '张三', 'EMP001', 'INCOMPLETE', NOW()),
(1002, 'EMPLOYEE', '李四', 'EMP002', 'COMPLETE', NOW()),
(1003, 'VISITOR', '王五', 'VIS001', 'INCOMPLETE', NOW());

INSERT INTO t_biometric_template (person_id, biometric_type, template_index, template_data, template_version, quality_score, enrollment_device_id, capture_time, valid_from, valid_to, template_status, create_time) VALUES
(1001, 'FACE', 1, 'ENCRYPTED_FACE_TEMPLATE_DATA_v1', 'v1.0', 0.92, 1001, NOW(), NOW(), DATE_ADD(NOW(), INTERVAL 2 YEAR), 'ACTIVE', NOW()),
(1001, 'FINGERPRINT', 1, 'ENCRYPTED_FINGERPRINT_TEMPLATE_DATA_v1', 'v1.0', 0.88, 1001, NOW(), NOW(), DATE_ADD(NOW(), INTERVAL 2 YEAR), 'ACTIVE', NOW()),
(1001, 'FINGERPRINT', 2, 'ENCRYPTED_FINGERPRINT_TEMPLATE_DATA_v2', 'v1.0', 0.85, 1001, NOW(), NOW(), DATE_ADD(NOW(), INTERVAL 2 YEAR), 'ACTIVE', NOW()),
(1002, 'FACE', 1, 'ENCRYPTED_FACE_TEMPLATE_DATA_v1', 'v1.0', 0.95, 1002, NOW(), NOW(), DATE_ADD(NOW(), INTERVAL 2 YEAR), 'ACTIVE', NOW()),
(1002, 'FACE', 2, 'ENCRYPTED_FACE_TEMPLATE_DATA_v2', 'v1.0', 0.90, 1002, NOW(), NOW(), DATE_ADD(NOW(), INTERVAL 2 YEAR), 'ACTIVE', NOW()),
(1002, 'FINGERPRINT', 1, 'ENCRYPTED_FINGERPRINT_TEMPLATE_DATA_v1', 'v1.0', 0.91, 1002, NOW(), NOW(), DATE_ADD(NOW(), INTERVAL 2 YEAR), 'ACTIVE', NOW());

-- 调用存储过程更新统计
CALL update_person_biometric_counts(1001, 'EMPLOYEE');
CALL update_person_biometric_counts(1002, 'EMPLOYEE');
CALL update_person_biometric_counts(1003, 'VISITOR');

COMMIT;