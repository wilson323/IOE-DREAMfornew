-- =====================================================
-- IOE-DREAM 数据库迁移脚本
-- 版本: V2.1.5
-- 描述: 批量补齐缺口表（第2批：告警/通知/监控/日志/区域关联）
-- =====================================================

USE ioedream;

-- =====================================================
-- 1. t_alert（告警表）
-- =====================================================
CREATE TABLE IF NOT EXISTS t_alert (
    alert_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '告警ID',
    alert_type VARCHAR(50) NOT NULL COMMENT '告警类型',
    alert_level TINYINT DEFAULT 1 COMMENT '告警级别：1-低 2-中 3-高 4-紧急',
    alert_title VARCHAR(200) NOT NULL COMMENT '告警标题',
    alert_content TEXT COMMENT '告警内容',
    source VARCHAR(100) COMMENT '告警来源',
    source_id VARCHAR(100) COMMENT '来源ID',
    status TINYINT DEFAULT 1 COMMENT '状态：1-未处理 2-处理中 3-已处理 4-已忽略',
    handler_id BIGINT COMMENT '处理人ID',
    handle_time DATETIME COMMENT '处理时间',
    handle_remark VARCHAR(500) COMMENT '处理备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    KEY idx_alert_type (alert_type),
    KEY idx_alert_level (alert_level),
    KEY idx_status (status),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='告警表';

-- =====================================================
-- 2. t_alert_rule（告警规则表）
-- =====================================================
CREATE TABLE IF NOT EXISTS t_alert_rule (
    rule_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '规则ID',
    rule_name VARCHAR(100) NOT NULL COMMENT '规则名称',
    rule_type VARCHAR(50) NOT NULL COMMENT '规则类型',
    rule_config TEXT COMMENT '规则配置JSON',
    alert_level TINYINT DEFAULT 2 COMMENT '告警级别',
    notify_channels VARCHAR(200) COMMENT '通知渠道',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用 2-禁用',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    KEY idx_rule_type (rule_type),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='告警规则表';

-- =====================================================
-- 3. t_notification_config（通知配置表）
-- =====================================================
CREATE TABLE IF NOT EXISTS t_notification_config (
    config_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '配置ID',
    config_type VARCHAR(50) NOT NULL COMMENT '配置类型：EMAIL/SMS/WECHAT/DINGTALK',
    config_name VARCHAR(100) NOT NULL COMMENT '配置名称',
    config_json TEXT COMMENT '配置JSON',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用 2-禁用',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    KEY idx_config_type (config_type),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知配置表';

-- =====================================================
-- 4. t_notification_template（通知模板表）
-- =====================================================
CREATE TABLE IF NOT EXISTS t_notification_template (
    template_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '模板ID',
    template_code VARCHAR(100) NOT NULL COMMENT '模板编码',
    template_name VARCHAR(100) NOT NULL COMMENT '模板名称',
    template_type VARCHAR(50) NOT NULL COMMENT '模板类型',
    template_content TEXT COMMENT '模板内容',
    params_config TEXT COMMENT '参数配置JSON',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用 2-禁用',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    UNIQUE KEY uk_template_code (template_code, deleted_flag),
    KEY idx_template_type (template_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知模板表';

-- =====================================================
-- 5. t_monitor_notification（监控通知表）
-- =====================================================
CREATE TABLE IF NOT EXISTS t_monitor_notification (
    notification_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '通知ID',
    notification_type VARCHAR(50) NOT NULL COMMENT '通知类型',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT COMMENT '内容',
    receiver_id BIGINT COMMENT '接收人ID',
    receiver_type VARCHAR(50) COMMENT '接收人类型',
    channel VARCHAR(50) COMMENT '发送渠道',
    send_status TINYINT DEFAULT 0 COMMENT '发送状态：0-待发送 1-已发送 2-发送失败',
    send_time DATETIME COMMENT '发送时间',
    read_status TINYINT DEFAULT 0 COMMENT '阅读状态：0-未读 1-已读',
    read_time DATETIME COMMENT '阅读时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    KEY idx_receiver_id (receiver_id),
    KEY idx_send_status (send_status),
    KEY idx_read_status (read_status),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='监控通知表';

-- =====================================================
-- 6. t_system_log（系统日志表）
-- =====================================================
CREATE TABLE IF NOT EXISTS t_system_log (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    log_type VARCHAR(50) NOT NULL COMMENT '日志类型',
    log_level VARCHAR(20) DEFAULT 'INFO' COMMENT '日志级别',
    module VARCHAR(100) COMMENT '模块',
    operation VARCHAR(200) COMMENT '操作',
    content TEXT COMMENT '日志内容',
    user_id BIGINT COMMENT '用户ID',
    username VARCHAR(100) COMMENT '用户名',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    user_agent VARCHAR(500) COMMENT '用户代理',
    request_url VARCHAR(500) COMMENT '请求URL',
    request_method VARCHAR(20) COMMENT '请求方法',
    request_params TEXT COMMENT '请求参数',
    response_data TEXT COMMENT '响应数据',
    execution_time BIGINT COMMENT '执行时间(毫秒)',
    exception_info TEXT COMMENT '异常信息',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_log_type (log_type),
    KEY idx_log_level (log_level),
    KEY idx_user_id (user_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统日志表';

-- =====================================================
-- 7. t_system_monitor（系统监控表）
-- =====================================================
CREATE TABLE IF NOT EXISTS t_system_monitor (
    monitor_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '监控ID',
    monitor_type VARCHAR(50) NOT NULL COMMENT '监控类型',
    monitor_name VARCHAR(100) NOT NULL COMMENT '监控名称',
    metric_name VARCHAR(100) COMMENT '指标名称',
    metric_value DECIMAL(20,4) COMMENT '指标值',
    metric_unit VARCHAR(20) COMMENT '指标单位',
    threshold_min DECIMAL(20,4) COMMENT '阈值下限',
    threshold_max DECIMAL(20,4) COMMENT '阈值上限',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常 2-警告 3-异常',
    collect_time DATETIME NOT NULL COMMENT '采集时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_monitor_type (monitor_type),
    KEY idx_status (status),
    KEY idx_collect_time (collect_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统监控表';

-- =====================================================
-- 8. t_area_device_relation（区域设备关联表）
-- =====================================================
CREATE TABLE IF NOT EXISTS t_area_device_relation (
    relation_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '关联ID',
    area_id BIGINT NOT NULL COMMENT '区域ID',
    device_id BIGINT NOT NULL COMMENT '设备ID',
    relation_type VARCHAR(50) COMMENT '关联类型',
    status TINYINT DEFAULT 1 COMMENT '状态',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    UNIQUE KEY uk_area_device (area_id, device_id, deleted_flag),
    KEY idx_area_id (area_id),
    KEY idx_device_id (device_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='区域设备关联表';

-- =====================================================
-- 9. t_area_user_relation（区域用户关联表）
-- =====================================================
CREATE TABLE IF NOT EXISTS t_area_user_relation (
    relation_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '关联ID',
    area_id BIGINT NOT NULL COMMENT '区域ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    relation_type VARCHAR(50) COMMENT '关联类型',
    permission_level TINYINT DEFAULT 1 COMMENT '权限级别',
    status TINYINT DEFAULT 1 COMMENT '状态',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    UNIQUE KEY uk_area_user (area_id, user_id, deleted_flag),
    KEY idx_area_id (area_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='区域用户关联表';

SELECT 'V2.1.5 批量补齐第2批表完成' AS status;
