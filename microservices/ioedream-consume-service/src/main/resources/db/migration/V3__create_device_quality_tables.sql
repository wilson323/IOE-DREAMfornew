-- ====================================================
-- P0-6 设备质量诊断功能 - 数据库表创建脚本
-- 版本: V3__create_device_quality_tables.sql
-- 创建时间: 2025-12-26
-- 说明: 创建设备质量诊断相关的4张表
-- ====================================================

-- 1. 设备质量记录表
CREATE TABLE t_device_quality_record (
    record_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    device_id VARCHAR(64) NOT NULL COMMENT '设备ID',
    device_name VARCHAR(200) COMMENT '设备名称',
    device_type INT COMMENT '设备类型(1-门禁 2-考勤 3-消费 4-视频 5-访客)',
    health_score INT COMMENT '健康评分(0-100)',
    quality_level VARCHAR(20) COMMENT '质量等级(优秀/良好/合格/较差/危险)',
    diagnosis_result TEXT COMMENT '诊断结果(JSON格式)',
    alarm_level INT COMMENT '告警级别(0-无 1-低 2-中 3-高 4-紧急)',
    diagnosis_time DATETIME NOT NULL COMMENT '诊断时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_device_id (device_id),
    INDEX idx_diagnosis_time (diagnosis_time),
    INDEX idx_health_score (health_score),
    INDEX idx_device_type (device_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备质量诊断记录表';

-- 2. 设备健康指标表
CREATE TABLE t_device_health_metric (
    metric_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '指标ID',
    device_id VARCHAR(64) NOT NULL COMMENT '设备ID',
    metric_type VARCHAR(50) NOT NULL COMMENT '指标类型(cpu/memory/temperature/delay/packet_loss)',
    metric_value DECIMAL(10,2) NOT NULL COMMENT '指标值',
    metric_unit VARCHAR(20) COMMENT '指标单位(%,℃,ms,%)',
    collect_time DATETIME NOT NULL COMMENT '采集时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_device_id (device_id),
    INDEX idx_device_type_time (device_id, metric_type, collect_time),
    INDEX idx_collect_time (collect_time),
    INDEX idx_metric_type (metric_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备健康指标表';

-- 3. 质量诊断规则表
CREATE TABLE t_quality_diagnosis_rule (
    rule_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '规则ID',
    rule_name VARCHAR(200) NOT NULL COMMENT '规则名称',
    rule_code VARCHAR(100) NOT NULL COMMENT '规则编码',
    device_type INT COMMENT '设备类型(1-门禁 2-考勤 3-消费 4-视频 5-访客)',
    metric_type VARCHAR(50) COMMENT '指标类型',
    rule_expression VARCHAR(500) COMMENT '规则表达式',
    threshold_value DECIMAL(10,2) COMMENT '阈值',
    alarm_level INT COMMENT '告警级别(1-低 2-中 3-高 4-紧急)',
    rule_status TINYINT DEFAULT 1 COMMENT '规则状态(1-启用 0-禁用)',
    rule_description VARCHAR(500) COMMENT '规则描述',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE INDEX uk_rule_code (rule_code),
    INDEX idx_device_type (device_type),
    INDEX idx_rule_status (rule_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='质量诊断规则表';

-- 4. 质量告警表
CREATE TABLE t_quality_alarm (
    alarm_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '告警ID',
    device_id VARCHAR(64) NOT NULL COMMENT '设备ID',
    device_name VARCHAR(200) COMMENT '设备名称',
    rule_id BIGINT COMMENT '触发的规则ID',
    alarm_level INT COMMENT '告警级别(1-低 2-中 3-高 4-紧急)',
    alarm_title VARCHAR(200) NOT NULL COMMENT '告警标题',
    alarm_content TEXT COMMENT '告警内容',
    alarm_status TINYINT DEFAULT 1 COMMENT '告警状态(1-待处理 2-处理中 3-已处理)',
    handle_result TEXT COMMENT '处理结果',
    handle_user_id BIGINT COMMENT '处理人ID',
    handle_time DATETIME COMMENT '处理时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_device_id (device_id),
    INDEX idx_alarm_level (alarm_level),
    INDEX idx_alarm_status (alarm_status),
    INDEX idx_create_time (create_time),
    INDEX idx_handle_user (handle_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备质量告警表';

-- ====================================================
-- 初始化数据 - 诊断规则
-- ====================================================

-- 门禁设备诊断规则
INSERT INTO t_quality_diagnosis_rule (rule_name, rule_code, device_type, metric_type, rule_expression, threshold_value, alarm_level, rule_status, rule_description) VALUES
('门禁设备离线告警', 'ACCESS_OFFLINE', 1, 'online_status', 'eq', 0, 3, 1, '门禁设备离线超过30分钟'),
('门禁响应延迟告警', 'ACCESS_DELAY', 1, 'delay', 'gt', 3000, 2, 1, '门禁响应延迟超过3秒'),
('门禁温度告警', 'ACCESS_TEMP', 1, 'temperature', 'gt', 70, 3, 1, '门禁设备温度超过70℃');

-- 考勤设备诊断规则
INSERT INTO t_quality_diagnosis_rule (rule_name, rule_code, device_type, metric_type, rule_expression, threshold_value, alarm_level, rule_status, rule_description) VALUES
('考勤设备离线告警', 'ATTENDANCE_OFFLINE', 2, 'online_status', 'eq', 0, 3, 1, '考勤设备离线超过30分钟'),
('考勤识别成功率低', 'ATTENDANCE_SUCCESS_RATE', 2, 'success_rate', 'lt', 90, 2, 1, '考勤识别成功率低于90%'),
('考勤设备温度告警', 'ATTENDANCE_TEMP', 2, 'temperature', 'gt', 70, 3, 1, '考勤设备温度超过70℃');

-- 消费设备诊断规则
INSERT INTO t_quality_diagnosis_rule (rule_name, rule_code, device_type, metric_type, rule_expression, threshold_value, alarm_level, rule_status, rule_description) VALUES
('消费设备离线告警', 'CONSUME_OFFLINE', 3, 'online_status', 'eq', 0, 3, 1, '消费设备离线超过30分钟'),
('消费交易响应延迟', 'CONSUME_DELAY', 3, 'delay', 'gt', 5000, 2, 1, '消费交易响应延迟超过5秒'),
('消费设备温度告警', 'CONSUME_TEMP', 3, 'temperature', 'gt', 70, 3, 1, '消费设备温度超过70℃');

-- 视频设备诊断规则
INSERT INTO t_quality_diagnosis_rule (rule_name, rule_code, device_type, metric_type, rule_expression, threshold_value, alarm_level, rule_status, rule_description) VALUES
('视频设备离线告警', 'VIDEO_OFFLINE', 4, 'online_status', 'eq', 0, 3, 1, '视频设备离线超过30分钟'),
('视频码流异常', 'VIDEO_STREAM', 4, 'stream_status', 'eq', 0, 2, 1, '视频码流异常'),
('视频存储空间告警', 'VIDEO_STORAGE', 4, 'storage_usage', 'gt', 90, 3, 1, '视频存储空间使用率超过90%');

-- 访客设备诊断规则
INSERT INTO t_quality_diagnosis_rule (rule_name, rule_code, device_type, metric_type, rule_expression, threshold_value, alarm_level, rule_status, rule_description) VALUES
('访客设备离线告警', 'VISITOR_OFFLINE', 5, 'online_status', 'eq', 0, 3, 1, '访客设备离线超过30分钟'),
('访客识别成功率低', 'VISITOR_SUCCESS_RATE', 5, 'success_rate', 'lt', 90, 2, 1, '访客识别成功率低于90%'),
('访客设备温度告警', 'VISITOR_TEMP', 5, 'temperature', 'gt', 70, 3, 1, '访客设备温度超过70℃');
