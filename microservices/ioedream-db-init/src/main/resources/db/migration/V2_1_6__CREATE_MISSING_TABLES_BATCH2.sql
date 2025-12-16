-- =====================================================
-- IOE-DREAM Flyway 迁移脚本
-- 版本: V2.1.6
-- 描述: 批量补齐缺口表（第2批：告警/通知/监控/日志/区域关联）
-- =====================================================

CREATE TABLE IF NOT EXISTS t_alert (
    alert_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    alert_type VARCHAR(50) NOT NULL,
    alert_level TINYINT DEFAULT 1,
    alert_title VARCHAR(200) NOT NULL,
    alert_content TEXT,
    source VARCHAR(100),
    source_id VARCHAR(100),
    status TINYINT DEFAULT 1,
    handler_id BIGINT,
    handle_time DATETIME,
    handle_remark VARCHAR(500),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_flag TINYINT DEFAULT 0,
    KEY idx_alert_type (alert_type),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS t_alert_rule (
    rule_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_name VARCHAR(100) NOT NULL,
    rule_type VARCHAR(50) NOT NULL,
    rule_config TEXT,
    alert_level TINYINT DEFAULT 2,
    notify_channels VARCHAR(200),
    status TINYINT DEFAULT 1,
    remark VARCHAR(500),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_flag TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS t_notification_config (
    config_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_type VARCHAR(50) NOT NULL,
    config_name VARCHAR(100) NOT NULL,
    config_json TEXT,
    status TINYINT DEFAULT 1,
    remark VARCHAR(500),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_flag TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS t_notification_template (
    template_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_code VARCHAR(100) NOT NULL,
    template_name VARCHAR(100) NOT NULL,
    template_type VARCHAR(50) NOT NULL,
    template_content TEXT,
    params_config TEXT,
    status TINYINT DEFAULT 1,
    remark VARCHAR(500),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_flag TINYINT DEFAULT 0,
    UNIQUE KEY uk_template_code (template_code, deleted_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS t_monitor_notification (
    notification_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    notification_type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    receiver_id BIGINT,
    receiver_type VARCHAR(50),
    channel VARCHAR(50),
    send_status TINYINT DEFAULT 0,
    send_time DATETIME,
    read_status TINYINT DEFAULT 0,
    read_time DATETIME,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_flag TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS t_system_log (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    log_type VARCHAR(50) NOT NULL,
    log_level VARCHAR(20) DEFAULT 'INFO',
    module VARCHAR(100),
    operation VARCHAR(200),
    content TEXT,
    user_id BIGINT,
    username VARCHAR(100),
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    request_url VARCHAR(500),
    request_method VARCHAR(20),
    request_params TEXT,
    response_data TEXT,
    execution_time BIGINT,
    exception_info TEXT,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_log_type (log_type),
    KEY idx_user_id (user_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS t_system_monitor (
    monitor_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    monitor_type VARCHAR(50) NOT NULL,
    monitor_name VARCHAR(100) NOT NULL,
    metric_name VARCHAR(100),
    metric_value DECIMAL(20,4),
    metric_unit VARCHAR(20),
    threshold_min DECIMAL(20,4),
    threshold_max DECIMAL(20,4),
    status TINYINT DEFAULT 1,
    collect_time DATETIME NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS t_area_device_relation (
    relation_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    area_id BIGINT NOT NULL,
    device_id BIGINT NOT NULL,
    relation_type VARCHAR(50),
    status TINYINT DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_flag TINYINT DEFAULT 0,
    UNIQUE KEY uk_area_device (area_id, device_id, deleted_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS t_area_user_relation (
    relation_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    area_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    relation_type VARCHAR(50),
    permission_level TINYINT DEFAULT 1,
    status TINYINT DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_flag TINYINT DEFAULT 0,
    UNIQUE KEY uk_area_user (area_id, user_id, deleted_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SELECT 'V2.1.6 Flyway Batch2 完成' AS status;
