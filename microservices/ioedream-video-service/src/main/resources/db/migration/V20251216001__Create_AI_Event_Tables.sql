-- IOE-DREAM视频微服务AI事件相关表
-- 版本: 1.0.0
-- 创建时间: 2025-12-16
-- 描述: 创建AI智能分析事件相关的数据库表

-- 创建AI事件表
CREATE TABLE t_video_ai_event (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    event_id VARCHAR(100) NOT NULL UNIQUE COMMENT '事件ID（唯一标识）',
    device_id BIGINT NOT NULL COMMENT '设备ID',
    event_type VARCHAR(50) NOT NULL COMMENT '事件类型',
    event_sub_type VARCHAR(50) COMMENT '事件子类型',
    event_title VARCHAR(200) NOT NULL COMMENT '事件标题',
    event_description TEXT COMMENT '事件描述',
    priority INT NOT NULL DEFAULT 5 COMMENT '优先级(1-10)',
    severity INT NOT NULL DEFAULT 1 COMMENT '严重程度(1-低, 2-中, 3-高, 4-紧急)',
    confidence DECIMAL(5,4) NOT NULL COMMENT '置信度(0.0000-1.0000)',
    event_data JSON COMMENT '事件数据(JSON格式)',
    video_url VARCHAR(500) COMMENT '视频文件路径',
    image_url VARCHAR(500) COMMENT '图片文件路径',
    location VARCHAR(200) COMMENT '事件发生位置',
    longitude DECIMAL(10,7) COMMENT '经度',
    latitude DECIMAL(10,7) COMMENT '纬度',
    event_status INT NOT NULL DEFAULT 1 COMMENT '事件状态(1-待处理, 2-已处理, 3-已忽略)',
    process_result TEXT COMMENT '处理结果',
    process_time DATETIME COMMENT '处理时间',
    process_user_id BIGINT COMMENT '处理人ID',
    process_user_name VARCHAR(100) COMMENT '处理人姓名',
    process_times INT NOT NULL DEFAULT 0 COMMENT '处理次数',
    ai_model_id VARCHAR(50) COMMENT 'AI模型ID',
    ai_model_name VARCHAR(100) COMMENT 'AI模型名称',
    ai_model_version VARCHAR(20) COMMENT 'AI模型版本',
    analysis_duration_ms BIGINT COMMENT '分析耗时(毫秒)',
    need_manual_review BOOLEAN DEFAULT FALSE COMMENT '是否需要人工审核',
    manual_review_result VARCHAR(50) COMMENT '人工审核结果',
    manual_review_time DATETIME COMMENT '人工审核时间',
    manual_review_user_id BIGINT COMMENT '人工审核人ID',
    manual_review_user_name VARCHAR(100) COMMENT '人工审核人姓名',
    tags JSON COMMENT '标签',
    related_event_ids JSON COMMENT '关联事件ID列表',
    alert_level VARCHAR(20) COMMENT '预警级别',
    alert_description VARCHAR(500) COMMENT '预警描述',
    notification_sent BOOLEAN DEFAULT FALSE COMMENT '是否已通知',
    notification_time DATETIME COMMENT '通知时间',
    notification_methods JSON COMMENT '通知方式',
    forwarded BOOLEAN DEFAULT FALSE COMMENT '是否已转发',
    forward_target VARCHAR(100) COMMENT '转发目标',
    forward_time DATETIME COMMENT '转发时间',
    file_size BIGINT COMMENT '存储文件大小(字节)',
    storage_path VARCHAR(500) COMMENT '存储路径',
    remark TEXT COMMENT '备注',
    extended_attributes JSON COMMENT '扩展属性(JSON格式)',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记(0-未删除, 1-已删除)',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_event_id (event_id),
    INDEX idx_device_id (device_id),
    INDEX idx_event_type (event_type),
    INDEX idx_event_status (event_status),
    INDEX idx_priority (priority),
    INDEX idx_severity (severity),
    INDEX idx_create_time (create_time),
    INDEX idx_process_time (process_time),
    INDEX idx_need_manual_review (need_manual_review),
    INDEX idx_notification_sent (notification_sent),
    INDEX idx_deleted_flag (deleted_flag),
    INDEX idx_device_status_time (device_id, event_status, create_time),
    INDEX idx_type_priority_time (event_type, priority, create_time),
    INDEX idx_location_time (location, create_time),
    UNIQUE KEY uk_event_id (event_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI智能分析事件表';

-- 创建AI事件处理历史表
CREATE TABLE t_video_ai_event_process_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    event_id BIGINT NOT NULL COMMENT 'AI事件ID',
    process_type VARCHAR(50) NOT NULL COMMENT '处理类型',
    process_result TEXT COMMENT '处理结果',
    process_user_id BIGINT COMMENT '处理人ID',
    process_user_name VARCHAR(100) COMMENT '处理人姓名',
    process_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '处理时间',
    process_duration_ms BIGINT COMMENT '处理耗时(毫秒)',
    process_status INT NOT NULL DEFAULT 1 COMMENT '处理状态(1-成功, 2-失败)',
    error_message TEXT COMMENT '错误信息',
    remarks TEXT COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_event_id (event_id),
    INDEX idx_process_type (process_type),
    INDEX idx_process_time (process_time),
    INDEX idx_process_user_id (process_user_id),
    FOREIGN KEY fk_event_process_event_id (event_id) REFERENCES t_video_ai_event(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI事件处理历史表';

-- 创建AI事件统计表
CREATE TABLE t_video_ai_event_statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    stat_date DATE NOT NULL COMMENT '统计日期',
    device_id BIGINT COMMENT '设备ID(为空则表示全站统计)',
    event_type VARCHAR(50) COMMENT '事件类型(为空则表示全类型统计)',
    total_events INT NOT NULL DEFAULT 0 COMMENT '总事件数',
    processed_events INT NOT NULL DEFAULT 0 COMMENT '已处理事件数',
    unprocessed_events INT NOT NULL DEFAULT 0 COMMENT '未处理事件数',
    high_priority_events INT NOT NULL DEFAULT 0 COMMENT '高优先级事件数',
    urgent_events INT NOT NULL DEFAULT 0 COMMENT '紧急事件数',
    average_confidence DECIMAL(5,4) DEFAULT 0 COMMENT '平均置信度',
    average_process_time_minutes DECIMAL(10,2) DEFAULT 0 COMMENT '平均处理时间(分钟)',
    false_positive_count INT DEFAULT 0 COMMENT '误报数量',
    accuracy_rate DECIMAL(5,4) DEFAULT 0 COMMENT '准确率',
    ai_model_name VARCHAR(100) COMMENT 'AI模型名称',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_stat_date_device_type (stat_date, device_id, event_type),
    INDEX idx_stat_date (stat_date),
    INDEX idx_device_id (device_id),
    INDEX idx_event_type (event_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI事件统计表';

-- 创建AI模型性能监控表
CREATE TABLE t_video_ai_model_performance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    model_id VARCHAR(50) NOT NULL COMMENT '模型ID',
    model_name VARCHAR(100) NOT NULL COMMENT '模型名称',
    model_version VARCHAR(20) NOT NULL COMMENT '模型版本',
    event_type VARCHAR(50) NOT NULL COMMENT '适用事件类型',
    total_inferences INT NOT NULL DEFAULT 0 COMMENT '总推理次数',
    successful_inferences INT NOT NULL DEFAULT 0 COMMENT '成功推理次数',
    failed_inferences INT NOT NULL DEFAULT 0 COMMENT '失败推理次数',
    average_inference_time_ms DECIMAL(10,2) DEFAULT 0 COMMENT '平均推理时间(毫秒)',
    max_inference_time_ms BIGINT DEFAULT 0 COMMENT '最大推理时间(毫秒)',
    min_inference_time_ms BIGINT DEFAULT 0 COMMENT '最小推理时间(毫秒)',
    average_confidence DECIMAL(5,4) DEFAULT 0 COMMENT '平均置信度',
    accuracy_rate DECIMAL(5,4) DEFAULT 0 COMMENT '准确率',
    precision_rate DECIMAL(5,4) DEFAULT 0 COMMENT '精确率',
    recall_rate DECIMAL(5,4) DEFAULT 0 COMMENT '召回率',
    f1_score DECIMAL(5,4) DEFAULT 0 COMMENT 'F1分数',
    false_positive_rate DECIMAL(5,4) DEFAULT 0 COMMENT '误报率',
    false_negative_rate DECIMAL(5,4) DEFAULT 0 COMMENT '漏报率',
    cpu_usage_rate DECIMAL(5,4) DEFAULT 0 COMMENT 'CPU使用率',
    memory_usage_rate DECIMAL(5,4) DEFAULT 0 COMMENT '内存使用率',
    gpu_usage_rate DECIMAL(5,4) DEFAULT 0 COMMENT 'GPU使用率',
    last_inference_time DATETIME COMMENT '最后推理时间',
    model_status INT DEFAULT 1 COMMENT '模型状态(1-正常, 2-异常, 3-维护中)',
    deployment_info JSON COMMENT '部署信息',
    performance_metrics JSON COMMENT '性能指标详情',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_model_id_version (model_id, model_version),
    INDEX idx_model_name (model_name),
    INDEX idx_event_type (event_type),
    INDEX idx_model_status (model_status),
    INDEX idx_last_inference_time (last_inference_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI模型性能监控表';

-- 创建AI事件告警规则表
CREATE TABLE t_video_ai_alert_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    rule_name VARCHAR(100) NOT NULL COMMENT '规则名称',
    rule_code VARCHAR(50) NOT NULL UNIQUE COMMENT '规则编码',
    rule_type VARCHAR(50) NOT NULL COMMENT '规则类型',
    event_type VARCHAR(50) COMMENT '适用事件类型',
    priority_threshold INT COMMENT '优先级阈值',
    severity_threshold INT COMMENT '严重程度阈值',
    confidence_threshold DECIMAL(5,4) COMMENT '置信度阈值',
    time_window_minutes INT COMMENT '时间窗口(分钟)',
    event_count_threshold INT COMMENT '事件数量阈值',
    device_filter JSON COMMENT '设备过滤条件',
    location_filter JSON COMMENT '位置过滤条件',
    alert_level VARCHAR(20) NOT NULL COMMENT '告警级别',
    alert_message_template TEXT COMMENT '告警消息模板',
    notification_methods JSON COMMENT '通知方式',
    notification_targets JSON COMMENT '通知目标',
    escalation_rules JSON COMMENT '升级规则',
    suppression_rules JSON COMMENT '抑制规则',
    is_enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    rule_order INT DEFAULT 0 COMMENT '规则顺序',
    rule_description TEXT COMMENT '规则描述',
    create_user_id BIGINT COMMENT '创建人ID',
    create_user_name VARCHAR(100) COMMENT '创建人姓名',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_user_id BIGINT COMMENT '更新人ID',
    update_user_name VARCHAR(100) COMMENT '更新人姓名',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记(0-未删除, 1-已删除)',
    INDEX idx_rule_code (rule_code),
    INDEX idx_rule_type (rule_type),
    INDEX idx_event_type (event_type),
    INDEX idx_is_enabled (is_enabled),
    INDEX idx_deleted_flag (deleted_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI事件告警规则表';

-- 插入初始告警规则
INSERT INTO t_video_ai_alert_rule (rule_name, rule_code, rule_type, event_type, priority_threshold, severity_threshold, alert_level, notification_methods, is_enabled, rule_description) VALUES
('高优先级事件告警', 'HIGH_PRIORITY_ALERT', 'PRIORITY', NULL, 8, NULL, 'HIGH', '["email", "sms", "push"]', TRUE, '优先级大于等于8的AI事件触发高优先级告警'),
('紧急事件告警', 'URGENT_EVENT_ALERT', 'SEVERITY', NULL, NULL, 4, 'CRITICAL', '["email", "sms", "push", "phone"]', TRUE, '严重程度为紧急的AI事件触发紧急告警'),
('陌生人检测告警', 'STRANGER_DETECTION_ALERT', 'EVENT_TYPE', 'FACE_RECOGNITION', NULL, NULL, 'MEDIUM', '["email", "push"]', TRUE, '人脸识别中的陌生人检测事件触发告警'),
('异常行为告警', 'ABNORMAL_BEHAVIOR_ALERT', 'EVENT_TYPE', 'BEHAVIOR_ANALYSIS', NULL, NULL, 'HIGH', '["email", "sms", "push"]', TRUE, '行为分析中的异常行为检测事件触发告警'),
('大量事件聚集告警', 'EVENT_CLUSTER_ALERT', 'COUNT', NULL, NULL, NULL, 'MEDIUM', '["email", "push"]', TRUE, '短时间内大量AI事件聚集触发告警');

-- 创建表空间分区注释
ALTER TABLE t_video_ai_event COMMENT 'AI智能分析事件表 - 支持分区存储按月分区';
ALTER TABLE t_video_ai_event_process_history COMMENT 'AI事件处理历史表 - 支持分区存储按月分区';
ALTER TABLE t_video_ai_event_statistics COMMENT 'AI事件统计表 - 按日统计';

-- 创建视图：AI事件概览
CREATE VIEW v_ai_event_overview AS
SELECT
    DATE(create_time) as stat_date,
    event_type,
    COUNT(*) as total_events,
    SUM(CASE WHEN event_status = 1 THEN 1 ELSE 0 END) as pending_events,
    SUM(CASE WHEN event_status = 2 THEN 1 ELSE 0 END) as processed_events,
    SUM(CASE WHEN priority >= 8 THEN 1 ELSE 0 END) as high_priority_events,
    SUM(CASE WHEN severity = 4 THEN 1 ELSE 0 END) as urgent_events,
    AVG(confidence) as average_confidence,
    AVG(TIMESTAMPDIFF(MINUTE, create_time, COALESCE(process_time, NOW()))) as avg_process_minutes
FROM t_video_ai_event
WHERE deleted_flag = 0
GROUP BY DATE(create_time), event_type;

-- 创建视图：AI设备事件统计
CREATE VIEW v_device_ai_event_stats AS
SELECT
    device_id,
    COUNT(*) as total_events,
    SUM(CASE WHEN event_status = 1 THEN 1 ELSE 0 END) as pending_events,
    SUM(CASE WHEN event_status = 2 THEN 1 ELSE 0 END) as processed_events,
    AVG(confidence) as avg_confidence,
    MAX(create_time) as last_event_time,
    COUNT(DISTINCT event_type) as event_type_count
FROM t_video_ai_event
WHERE deleted_flag = 0
GROUP BY device_id;

-- 创建存储过程：清理过期AI事件
DELIMITER //
CREATE PROCEDURE CleanupExpiredAIEvents(
    IN p_days_old INT,
    IN p_batch_size INT,
    OUT p_deleted_count INT
)
BEGIN
    DECLARE v_cutoff_date DATETIME;
    DECLARE v_deleted_batch INT DEFAULT 0;
    DECLARE v_total_deleted INT DEFAULT 0;

    SET v_cutoff_date = DATE_SUB(NOW(), INTERVAL p_days_old DAY);

    -- 循环删除过期的已处理事件
    REPEAT
        DELETE FROM t_video_ai_event
        WHERE create_time < v_cutoff_date
          AND event_status = 2
          AND deleted_flag = 0
        LIMIT p_batch_size;

        SET v_deleted_batch = ROW_COUNT();
        SET v_total_deleted = v_total_deleted + v_deleted_batch;

        -- 短暂休息避免锁表
        IF v_deleted_batch > 0 THEN
            SELECT SLEEP(0.1);
        END IF;

    UNTIL v_deleted_batch = 0 END REPEAT;

    SET p_deleted_count = v_total_deleted;
END //
DELIMITER ;

-- 创建触发器：更新AI事件统计
DELIMITER //
CREATE TRIGGER tr_ai_event_statistics_update
AFTER INSERT ON t_video_ai_event
FOR EACH ROW
BEGIN
    INSERT INTO t_video_ai_event_statistics (
        stat_date,
        device_id,
        event_type,
        total_events,
        unprocessed_events,
        high_priority_events,
        urgent_events,
        average_confidence,
        ai_model_name
    ) VALUES (
        DATE(NEW.create_time),
        NEW.device_id,
        NEW.event_type,
        1,
        IF(NEW.event_status = 1, 1, 0),
        IF(NEW.priority >= 8, 1, 0),
        IF(NEW.severity = 4, 1, 0),
        NEW.confidence,
        NEW.ai_model_name
    )
    ON DUPLICATE KEY UPDATE
        total_events = total_events + 1,
        unprocessed_events = unprocessed_events + IF(NEW.event_status = 1, 1, 0),
        high_priority_events = high_priority_events + IF(NEW.priority >= 8, 1, 0),
        urgent_events = urgent_events + IF(NEW.severity = 4, 1, 0),
        average_confidence = (average_confidence * total_events + NEW.confidence) / (total_events + 1),
        update_time = NOW();
END //
DELIMITER ;