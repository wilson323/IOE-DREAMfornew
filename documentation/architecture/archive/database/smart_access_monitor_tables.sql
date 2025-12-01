-- =============================================
-- SmartAdmin v3 门禁系统 - 实时监控模块数据库表
-- 创建时间: 2025-11-14
-- 模块: smart-access 实时监控
-- =============================================

-- 设备告警表
CREATE TABLE `smart_access_alarm` (
    `alarm_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '告警ID',
    `device_id` BIGINT NOT NULL COMMENT '设备ID',
    `alarm_type` VARCHAR(50) NOT NULL COMMENT '告警类型: OFFLINE-离线, FAULT-故障, LOW_BATTERY-低电量, DOOR_OPEN-门常开, FORCED_OPEN-强制开门, ACCESS_DENIED-拒绝访问, COMMUNICATION_ERROR-通信错误, ABNORMAL_TRAFFIC-异常流量',
    `alarm_level` VARCHAR(20) NOT NULL COMMENT '告警级别: LOW-低, MEDIUM-中, HIGH-高, CRITICAL-严重',
    `alarm_title` VARCHAR(200) NOT NULL COMMENT '告警标题',
    `alarm_content` TEXT COMMENT '告警内容描述',
    `alarm_status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '告警状态: PENDING-待处理, PROCESSING-处理中, RESOLVED-已解决, IGNORED-已忽略',
    `alarm_source` VARCHAR(50) NOT NULL COMMENT '告警来源: DEVICE-设备, SYSTEM-系统, MANUAL-手动',
    `trigger_time` DATETIME NOT NULL COMMENT '触发时间',
    `resolve_time` DATETIME COMMENT '解决时间',
    `resolve_user_id` BIGINT COMMENT '解决人ID',
    `resolve_user_name` VARCHAR(100) COMMENT '解决人姓名',
    `resolve_method` VARCHAR(200) COMMENT '解决方式',
    `resolve_remark` VARCHAR(500) COMMENT '解决备注',
    `alarm_data` JSON COMMENT '告警相关数据(JSON格式)',
    `device_snapshot` JSON COMMENT '设备快照数据(JSON格式)',
    `notification_sent` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已发送通知: 0-否, 1-是',
    `notification_time` DATETIME COMMENT '通知发送时间',
    `notification_channels` VARCHAR(200) COMMENT '通知渠道: EMAIL, SMS, WECHAT, DINGTALK',
    `auto_resolve` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否自动解决: 0-否, 1-是',
    `auto_resolve_time` DATETIME COMMENT '自动解决时间',
    `retry_count` INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    `max_retry_count` INT NOT NULL DEFAULT 3 COMMENT '最大重试次数',
    `next_retry_time` DATETIME COMMENT '下次重试时间',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除: 0-否, 1-是',
    `remark` VARCHAR(500) COMMENT '备注信息',
    PRIMARY KEY (`alarm_id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_alarm_type` (`alarm_type`),
    KEY `idx_alarm_level` (`alarm_level`),
    KEY `idx_alarm_status` (`alarm_status`),
    KEY `idx_trigger_time` (`trigger_time`),
    KEY `idx_created_time` (`created_time`),
    KEY `idx_device_trigger_time` (`device_id`, `trigger_time`),
    CONSTRAINT `fk_smart_access_alarm_device` FOREIGN KEY (`device_id`) REFERENCES `smart_access_device` (`device_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备告警表';

-- 设备监控配置表
CREATE TABLE `smart_access_monitor_config` (
    `config_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    `device_id` BIGINT NOT NULL COMMENT '设备ID',
    `monitor_enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用监控: 0-否, 1-是',
    `heartbeat_interval` INT NOT NULL DEFAULT 60 COMMENT '心跳间隔(秒)',
    `offline_threshold` INT NOT NULL DEFAULT 180 COMMENT '离线阈值(秒)',
    `response_timeout` INT NOT NULL DEFAULT 30 COMMENT '响应超时(秒)',
    `retry_count` INT NOT NULL DEFAULT 3 COMMENT '重试次数',
    `alarm_rules` JSON COMMENT '告警规则配置(JSON格式)',
    `notification_settings` JSON COMMENT '通知设置(JSON格式)',
    `data_collection_enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用数据采集: 0-否, 1-是',
    `data_collection_interval` INT NOT NULL DEFAULT 300 COMMENT '数据采集间隔(秒)',
    `performance_monitoring` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否启用性能监控: 0-否, 1-是',
    `log_level` VARCHAR(20) NOT NULL DEFAULT 'INFO' COMMENT '日志级别: DEBUG, INFO, WARN, ERROR',
    `monitor_schedule` JSON COMMENT '监控调度配置(JSON格式)',
    `auto_recovery_enabled` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否启用自动恢复: 0-否, 1-是',
    `auto_recovery_actions` JSON COMMENT '自动恢复动作配置(JSON格式)',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除: 0-否, 1-是',
    `remark` VARCHAR(500) COMMENT '备注信息',
    PRIMARY KEY (`config_id`),
    UNIQUE KEY `uk_device_id` (`device_id`),
    KEY `idx_monitor_enabled` (`monitor_enabled`),
    KEY `idx_created_time` (`created_time`),
    CONSTRAINT `fk_smart_access_monitor_config_device` FOREIGN KEY (`device_id`) REFERENCES `smart_access_device` (`device_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备监控配置表';

-- 设备监控历史表
CREATE TABLE `smart_access_monitor_history` (
    `history_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '历史记录ID',
    `device_id` BIGINT NOT NULL COMMENT '设备ID',
    `monitor_type` VARCHAR(50) NOT NULL COMMENT '监控类型: STATUS-状态, PERFORMANCE-性能, NETWORK-网络, POWER-电源, ENVIRONMENT-环境',
    `monitor_data` JSON NOT NULL COMMENT '监控数据(JSON格式)',
    `data_timestamp` DATETIME NOT NULL COMMENT '数据时间戳',
    `collection_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '采集时间',
    `data_quality` VARCHAR(20) NOT NULL DEFAULT 'GOOD' COMMENT '数据质量: GOOD-良好, POOR-较差, INVALID-无效',
    `anomaly_detected` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否检测到异常: 0-否, 1-是',
    `anomaly_score` DECIMAL(5,2) COMMENT '异常评分(0-100)',
    `processing_status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '处理状态: PENDING-待处理, PROCESSED-已处理, IGNORED-已忽略',
    `batch_id` VARCHAR(50) COMMENT '批次ID',
    `source` VARCHAR(50) NOT NULL DEFAULT 'SYSTEM' COMMENT '数据来源: SYSTEM-系统, DEVICE-设备, MANUAL-手动',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`history_id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_monitor_type` (`monitor_type`),
    KEY `idx_data_timestamp` (`data_timestamp`),
    KEY `idx_collection_time` (`collection_time`),
    KEY `idx_device_monitor_type` (`device_id`, `monitor_type`),
    KEY `idx_device_timestamp` (`device_id`, `data_timestamp`),
    KEY `idx_anomaly_detected` (`anomaly_detected`),
    CONSTRAINT `fk_smart_access_monitor_history_device` FOREIGN KEY (`device_id`) REFERENCES `smart_access_device` (`device_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备监控历史表';

-- 实时事件表
CREATE TABLE `smart_access_realtime_event` (
    `event_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '事件ID',
    `event_type` VARCHAR(50) NOT NULL COMMENT '事件类型: ACCESS-通行, ALARM-告警, STATUS-状态变化, CONTROL-控制, ERROR-错误, HEARTBEAT-心跳',
    `event_level` VARCHAR(20) NOT NULL COMMENT '事件级别: INFO-信息, WARN-警告, ERROR-错误, CRITICAL-严重',
    `device_id` BIGINT COMMENT '设备ID',
    `user_id` BIGINT COMMENT '用户ID',
    `event_title` VARCHAR(200) NOT NULL COMMENT '事件标题',
    `event_content` TEXT COMMENT '事件内容',
    `event_data` JSON COMMENT '事件数据(JSON格式)',
    `event_time` DATETIME NOT NULL COMMENT '事件时间',
    `event_source` VARCHAR(50) NOT NULL COMMENT '事件来源: DEVICE-设备, SYSTEM-系统, USER-用户, API-接口',
    `processed` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已处理: 0-否, 1-是',
    `processed_time` DATETIME COMMENT '处理时间',
    `processed_by` VARCHAR(100) COMMENT '处理人',
    `websocket_pushed` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已推送WebSocket: 0-否, 1-是',
    `push_channels` VARCHAR(200) COMMENT '推送渠道: WEBSOCKET, EMAIL, SMS, WECHAT',
    `push_targets` JSON COMMENT '推送目标(JSON格式)',
    `retention_days` INT NOT NULL DEFAULT 30 COMMENT '保留天数',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`event_id`),
    KEY `idx_event_type` (`event_type`),
    KEY `idx_event_level` (`event_level`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_event_time` (`event_time`),
    KEY `idx_processed` (`processed`),
    KEY `idx_websocket_pushed` (`websocket_pushed`),
    KEY `idx_device_event_time` (`device_id`, `event_time`),
    KEY `idx_user_event_time` (`user_id`, `event_time`),
    CONSTRAINT `fk_smart_access_realtime_event_device` FOREIGN KEY (`device_id`) REFERENCES `smart_access_device` (`device_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='实时事件表';

-- 监控告警规则表
CREATE TABLE `smart_access_alarm_rule` (
    `rule_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '规则ID',
    `rule_name` VARCHAR(100) NOT NULL COMMENT '规则名称',
    `rule_code` VARCHAR(50) NOT NULL COMMENT '规则编码',
    `rule_type` VARCHAR(50) NOT NULL COMMENT '规则类型: THRESHOLD-阈值, ANOMALY-异常, PATTERN-模式, SCHEDULE-定时',
    `target_type` VARCHAR(50) NOT NULL COMMENT '目标类型: DEVICE-设备, USER-用户, SYSTEM-系统',
    `target_condition` JSON NOT NULL COMMENT '目标条件(JSON格式)',
    `trigger_condition` JSON NOT NULL COMMENT '触发条件(JSON格式)',
    `alarm_level` VARCHAR(20) NOT NULL COMMENT '告警级别: LOW-低, MEDIUM-中, HIGH-高, CRITICAL-严重',
    `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用: 0-否, 1-是',
    `priority` INT NOT NULL DEFAULT 5 COMMENT '优先级(1-10, 数字越大优先级越高)',
    `description` VARCHAR(500) COMMENT '规则描述',
    `notification_config` JSON COMMENT '通知配置(JSON格式)',
    `recovery_config` JSON COMMENT '恢复配置(JSON格式)',
    `escalation_config` JSON COMMENT '升级配置(JSON格式)',
    `schedule_config` JSON COMMENT '调度配置(JSON格式)',
    `tags` VARCHAR(200) COMMENT '标签(逗号分隔)',
    `created_by` BIGINT COMMENT '创建人ID',
    `updated_by` BIGINT COMMENT '更新人ID',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除: 0-否, 1-是',
    `remark` VARCHAR(500) COMMENT '备注信息',
    PRIMARY KEY (`rule_id`),
    UNIQUE KEY `uk_rule_code` (`rule_code`),
    KEY `idx_rule_type` (`rule_type`),
    KEY `idx_target_type` (`target_type`),
    KEY `idx_enabled` (`enabled`),
    KEY `idx_priority` (`priority`),
    KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='监控告警规则表';

-- 监控统计表
CREATE TABLE `smart_access_monitor_statistics` (
    `stat_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '统计ID',
    `stat_date` DATE NOT NULL COMMENT '统计日期',
    `stat_type` VARCHAR(50) NOT NULL COMMENT '统计类型: DAILY-日报, HOURLY-小时报, REALTIME-实时',
    `device_id` BIGINT COMMENT '设备ID',
    `metric_name` VARCHAR(100) NOT NULL COMMENT '指标名称',
    `metric_value` DECIMAL(20,4) NOT NULL COMMENT '指标值',
    `metric_unit` VARCHAR(20) COMMENT '指标单位',
    `metric_category` VARCHAR(50) NOT NULL COMMENT '指标分类: STATUS-状态, PERFORMANCE-性能, TRAFFIC-流量, ALARM-告警',
    `stat_period_start` DATETIME NOT NULL COMMENT '统计周期开始时间',
    `stat_period_end` DATETIME NOT NULL COMMENT '统计周期结束时间',
    `data_source` VARCHAR(50) NOT NULL DEFAULT 'SYSTEM' COMMENT '数据来源',
    `calculation_method` VARCHAR(50) COMMENT '计算方法: SUM-求和, AVG-平均, MAX-最大值, MIN-最小值, COUNT-计数',
    `quality_score` DECIMAL(5,2) COMMENT '数据质量评分',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`stat_id`),
    UNIQUE KEY `uk_stat_date_device_type_metric` (`stat_date`, `device_id`, `stat_type`, `metric_name`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_stat_type` (`stat_type`),
    KEY `idx_metric_category` (`metric_category`),
    KEY `idx_stat_date` (`stat_date`),
    KEY `idx_stat_period_start` (`stat_period_start`),
    CONSTRAINT `fk_smart_access_monitor_statistics_device` FOREIGN KEY (`device_id`) REFERENCES `smart_access_device` (`device_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='监控统计表';

-- 创建视图：设备告警统计视图
CREATE VIEW `v_smart_access_device_alarm_summary` AS
SELECT
    d.device_id,
    d.device_name,
    d.device_code,
    d.device_status,
    COUNT(a.alarm_id) as total_alarms,
    SUM(CASE WHEN a.alarm_level = 'CRITICAL' THEN 1 ELSE 0 END) as critical_alarms,
    SUM(CASE WHEN a.alarm_level = 'HIGH' THEN 1 ELSE 0 END) as high_alarms,
    SUM(CASE WHEN a.alarm_level = 'MEDIUM' THEN 1 ELSE 0 END) as medium_alarms,
    SUM(CASE WHEN a.alarm_level = 'LOW' THEN 1 ELSE 0 END) as low_alarms,
    SUM(CASE WHEN a.alarm_status = 'PENDING' THEN 1 ELSE 0 END) as pending_alarms,
    SUM(CASE WHEN a.alarm_status = 'PROCESSING' THEN 1 ELSE 0 END) as processing_alarms,
    SUM(CASE WHEN a.alarm_status = 'RESOLVED' THEN 1 ELSE 0 END) as resolved_alarms,
    MAX(a.trigger_time) as last_alarm_time
FROM smart_access_device d
LEFT JOIN smart_access_alarm a ON d.device_id = a.device_id AND a.deleted_flag = 0
WHERE d.deleted_flag = 0
GROUP BY d.device_id, d.device_name, d.device_code, d.device_status;

-- 创建视图：实时事件汇总视图
CREATE VIEW `v_smart_access_realtime_event_summary` AS
SELECT
    event_type,
    event_level,
    COUNT(*) as event_count,
    COUNT(DISTINCT device_id) as device_count,
    COUNT(DISTINCT user_id) as user_count,
    MAX(event_time) as latest_event_time,
    SUM(CASE WHEN processed = 0 THEN 1 ELSE 0 END) as unprocessed_count,
    SUM(CASE WHEN websocket_pushed = 0 THEN 1 ELSE 0 END) as unpushed_count
FROM smart_access_realtime_event
WHERE event_time >= DATE_SUB(NOW(), INTERVAL 24 HOUR)
  AND deleted_flag = 0
GROUP BY event_type, event_level;

-- 创建索引优化查询性能
CREATE INDEX idx_smart_access_alarm_device_level_status ON smart_access_alarm(device_id, alarm_level, alarm_status);
CREATE INDEX idx_smart_access_alarm_trigger_time_status ON smart_access_alarm(trigger_time, alarm_status);
CREATE INDEX idx_smart_access_realtime_event_device_level ON smart_access_realtime_event(device_id, event_level);
CREATE INDEX idx_smart_access_monitor_history_device_type ON smart_access_monitor_history(device_id, monitor_type);

-- 初始化数据
INSERT INTO smart_access_alarm_rule (rule_name, rule_code, rule_type, target_type, target_condition, trigger_condition, alarm_level, description) VALUES
('设备离线告警', 'DEVICE_OFFLINE', 'THRESHOLD', 'DEVICE', '{}', '{"offline_duration": 180}', 'HIGH', '设备离线超过3分钟时触发告警'),
('设备心跳异常', 'HEARTBEAT_ABNORMAL', 'ANOMALY', 'DEVICE', '{}', '{"missed_count": 3}', 'MEDIUM', '设备心跳连续失败3次时触发告警'),
('通信错误告警', 'COMMUNICATION_ERROR', 'PATTERN', 'DEVICE', '{}', '{"error_pattern": "connection.*failed", "count": 2}', 'HIGH', '设备通信连续失败时触发告警'),
('门常开告警', 'DOOR_OPEN_TOO_LONG', 'THRESHOLD', 'DEVICE', '{"door_status": "open"}', '{"duration": 300}', 'CRITICAL', '门打开超过5分钟时触发严重告警'),
('异常流量检测', 'ABNORMAL_TRAFFIC', 'ANOMALY', 'DEVICE', '{"traffic_type": "access"}', '{"threshold": 100, "time_window": 3600}', 'HIGH', '一小时内通行次数异常时触发告警');