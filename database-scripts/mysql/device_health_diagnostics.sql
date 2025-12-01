-- 设备健康诊断与预测维护系统数据库脚本
-- 创建时间: 2025-11-25
-- 版本: v1.0
-- 说明: 智能设备健康监控、预警和预测维护功能

-- 1. 设备健康快照表
CREATE TABLE `t_device_health_snapshot` (
    `snapshot_id`           BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '快照ID',
    `device_id`             BIGINT NOT NULL COMMENT '设备ID',
    `score_value`           DECIMAL(5,2) NOT NULL COMMENT '健康评分(0-100)',
    `level_code`            VARCHAR(20) NOT NULL COMMENT '健康等级(healthy/warning/critical)',
    `heartbeat_latency_ms`  INT COMMENT '心跳延迟(毫秒)',
    `cpu_usage_pct`         DECIMAL(5,2) COMMENT 'CPU使用率(百分比)',
    `temperature_celsius`   DECIMAL(5,2) COMMENT '设备温度(摄氏度)',
    `command_success_ratio` DECIMAL(5,2) COMMENT '指令成功率(百分比)',
    `alarm_count`           INT DEFAULT 0 COMMENT '告警数量',
    `uptime_hours`          DECIMAL(7,2) COMMENT '连续运行时间(小时)',
    `maintenance_delay_days` DECIMAL(5,2) COMMENT '维护延迟天数',
    `snapshot_time`         DATETIME NOT NULL COMMENT '快照时间',
    `create_time`           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id`        BIGINT NOT NULL COMMENT '创建用户ID',
    `deleted_flag`          TINYINT DEFAULT 0 COMMENT '删除标志(0:正常 1:删除)',
    INDEX `idx_device_time` (`device_id`, `snapshot_time`),
    INDEX `idx_score` (`score_value`),
    INDEX `idx_level` (`level_code`),
    INDEX `idx_snapshot_time` (`snapshot_time`),
    CONSTRAINT `fk_health_snapshot_device` FOREIGN KEY (`device_id`) REFERENCES `t_access_device` (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备健康快照表';

-- 2. 设备健康指标表
CREATE TABLE `t_device_health_metric` (
    `metric_id`       BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '指标ID',
    `device_id`       BIGINT NOT NULL COMMENT '设备ID',
    `metric_code`     VARCHAR(50) NOT NULL COMMENT '指标代码',
    `metric_value`    DECIMAL(10,4) NOT NULL COMMENT '指标值',
    `metric_time`     DATETIME NOT NULL COMMENT '指标时间',
    `source_type`     VARCHAR(20) NOT NULL COMMENT '数据源类型(heartbeat/snmp/sensor/log)',
    `unit`            VARCHAR(20) COMMENT '单位',
    `threshold_min`   DECIMAL(10,4) COMMENT '最小阈值',
    `threshold_max`   DECIMAL(10,4) COMMENT '最大阈值',
    `create_time`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id`  BIGINT NOT NULL COMMENT '创建用户ID',
    `deleted_flag`    TINYINT DEFAULT 0 COMMENT '删除标志(0:正常 1:删除)',
    INDEX `idx_device_metric` (`device_id`, `metric_code`, `metric_time`),
    INDEX `idx_metric_code` (`metric_code`),
    INDEX `idx_metric_time` (`metric_time`),
    CONSTRAINT `fk_health_metric_device` FOREIGN KEY (`device_id`) REFERENCES `t_access_device` (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备健康指标表';

-- 3. 设备维护计划表
CREATE TABLE `t_device_maintenance_plan` (
    `plan_id`           BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '计划ID',
    `device_id`         BIGINT NOT NULL COMMENT '设备ID',
    `plan_status`       VARCHAR(20) NOT NULL COMMENT '计划状态(pending/in-progress/done/cancelled)',
    `trigger_reason`    VARCHAR(100) NOT NULL COMMENT '触发原因(low-score/trend-alert/manual/schedule)',
    `plan_type`         VARCHAR(20) NOT NULL COMMENT '计划类型(preventive/corrective/emergency)',
    `priority_level`    TINYINT DEFAULT 2 COMMENT '优先级(1:高 2:中 3:低)',
    `score_on_create`   DECIMAL(5,2) COMMENT '创建时健康评分',
    `assigned_to`       BIGINT COMMENT '指派给用户ID',
    `schedule_start`    DATETIME COMMENT '计划开始时间',
    `schedule_end`      DATETIME COMMENT '计划结束时间',
    `actual_start`      DATETIME COMMENT '实际开始时间',
    `actual_end`        DATETIME COMMENT '实际结束时间',
    `estimated_duration` INT COMMENT '预计耗时(分钟)',
    `description`       VARCHAR(500) COMMENT '维护描述',
    `result_note`       VARCHAR(1000) COMMENT '维护结果备注',
    `cost_amount`       DECIMAL(10,2) COMMENT '维护费用',
    `parts_used`        VARCHAR(200) COMMENT '使用的零件',
    `completion_rate`   DECIMAL(5,2) DEFAULT 0 COMMENT '完成率(百分比)',
    `create_time`       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id`    BIGINT NOT NULL COMMENT '创建用户ID',
    `update_user_id`    BIGINT COMMENT '更新用户ID',
    `deleted_flag`      TINYINT DEFAULT 0 COMMENT '删除标志(0:正常 1:删除)',
    INDEX `idx_device_status` (`device_id`, `plan_status`),
    INDEX `idx_priority` (`priority_level`),
    INDEX `idx_schedule_start` (`schedule_start`),
    INDEX `idx_assigned_to` (`assigned_to`),
    CONSTRAINT `fk_maintenance_plan_device` FOREIGN KEY (`device_id`) REFERENCES `t_access_device` (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备维护计划表';

-- 4. 设备告警记录表
CREATE TABLE `t_device_health_alert` (
    `alert_id`         BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '告警ID',
    `device_id`        BIGINT NOT NULL COMMENT '设备ID',
    `alert_type`       VARCHAR(30) NOT NULL COMMENT '告警类型(health/score/metric/availability)',
    `alert_level`      VARCHAR(20) NOT NULL COMMENT '告警级别(info/warning/critical)',
    `alert_code`       VARCHAR(50) NOT NULL COMMENT '告警代码',
    `alert_title`      VARCHAR(200) NOT NULL COMMENT '告警标题',
    `alert_message`    TEXT COMMENT '告警详细消息',
    `current_value`    DECIMAL(10,4) COMMENT '当前值',
    `threshold_value`  DECIMAL(10,4) COMMENT '阈值',
    `alert_time`       DATETIME NOT NULL COMMENT '告警时间',
    `acknowledged`     TINYINT DEFAULT 0 COMMENT '是否已确认(0:未确认 1:已确认)',
    `acknowledged_by`  BIGINT COMMENT '确认人ID',
    `acknowledged_time` DATETIME COMMENT '确认时间',
    `resolved`         TINYINT DEFAULT 0 COMMENT '是否已解决(0:未解决 1:已解决)',
    `resolved_by`      BIGINT COMMENT '解决人ID',
    `resolved_time`    DATETIME COMMENT '解决时间',
    `resolution`       VARCHAR(500) COMMENT '解决方案',
    `maintenance_plan_id` BIGINT COMMENT '关联的维护计划ID',
    `create_time`       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_device_alert` (`device_id`, `alert_time`),
    INDEX `idx_alert_level` (`alert_level`),
    INDEX `idx_alert_type` (`alert_type`),
    INDEX `idx_acknowledged` (`acknowledged`),
    INDEX `idx_resolved` (`resolved`),
    CONSTRAINT `fk_health_alert_device` FOREIGN KEY (`device_id`) REFERENCES `t_access_device` (`device_id`),
    CONSTRAINT `fk_alert_maintenance_plan` FOREIGN KEY (`maintenance_plan_id`) REFERENCES `t_device_maintenance_plan` (`plan_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备告警记录表';

-- 5. 设备健康配置表
CREATE TABLE `t_device_health_config` (
    `config_id`        BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    `device_id`        BIGINT COMMENT '设备ID(为空表示全局配置)',
    `metric_code`      VARCHAR(50) NOT NULL COMMENT '指标代码',
    `weight_value`     DECIMAL(5,4) NOT NULL COMMENT '权重值',
    `threshold_min`    DECIMAL(10,4) COMMENT '最小阈值',
    `threshold_max`    DECIMAL(10,4) COMMENT '最大阈值',
    `alert_enabled`    TINYINT DEFAULT 1 COMMENT '是否启用告警(0:禁用 1:启用)',
    `alert_threshold`  DECIMAL(10,4) COMMENT '告警阈值',
    `calculation_formula` VARCHAR(500) COMMENT '计算公式',
    `data_source`      VARCHAR(50) COMMENT '数据源',
    `collection_interval` INT DEFAULT 300 COMMENT '采集间隔(秒)',
    `retention_days`   INT DEFAULT 90 COMMENT '数据保留天数',
    `enabled_flag`     TINYINT DEFAULT 1 COMMENT '是否启用(0:禁用 1:启用)',
    `create_time`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id`   BIGINT NOT NULL COMMENT '创建用户ID',
    `update_user_id`   BIGINT COMMENT '更新用户ID',
    `deleted_flag`     TINYINT DEFAULT 0 COMMENT '删除标志(0:正常 1:删除)',
    UNIQUE KEY `uk_device_metric` (`device_id`, `metric_code`),
    INDEX `idx_metric_code` (`metric_code`),
    INDEX `idx_enabled` (`enabled_flag`),
    CONSTRAINT `fk_health_config_device` FOREIGN KEY (`device_id`) REFERENCES `t_access_device` (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备健康配置表';

-- 6. 设备健康趋势分析表
CREATE TABLE `t_device_health_trend` (
    `trend_id`         BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '趋势ID',
    `device_id`        BIGINT NOT NULL COMMENT '设备ID',
    `metric_code`      VARCHAR(50) NOT NULL COMMENT '指标代码',
    `trend_type`       VARCHAR(20) NOT NULL COMMENT '趋势类型(increasing/decreasing/stable)',
    `trend_strength`   DECIMAL(5,2) COMMENT '趋势强度(0-1)',
    `change_rate`      DECIMAL(10,6) COMMENT '变化率',
    `confidence_level` DECIMAL(5,2) COMMENT '置信度(0-100)',
    `prediction_value` DECIMAL(10,4) COMMENT '预测值',
    `prediction_time`  DATETIME COMMENT '预测时间',
    `analysis_period`  INT COMMENT '分析周期(小时)',
    `data_points`      INT COMMENT '数据点数量',
    `create_time`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_device_metric` (`device_id`, `metric_code`),
    INDEX `idx_trend_type` (`trend_type`),
    INDEX `idx_prediction_time` (`prediction_time`),
    CONSTRAINT `fk_health_trend_device` FOREIGN KEY (`device_id`) REFERENCES `t_access_device` (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备健康趋势分析表';

-- 插入默认健康配置数据
INSERT INTO `t_device_health_config` (`device_id`, `metric_code`, `weight_value`, `threshold_min`, `threshold_max`, `alert_threshold`, `collection_interval`) VALUES
(NULL, 'heartbeat_latency_ms', 0.20, 0, 5000, 3000, 60),      -- 心跳延迟，权重20%，阈值5秒，告警3秒
(NULL, 'cpu_usage_pct', 0.15, 0, 100, 85, 300),                -- CPU使用率，权重15%，阈值100%，告警85%
(NULL, 'temperature_celsius', 0.10, -40, 85, 75, 300),          -- 温度，权重10%，阈值85℃，告警75℃
(NULL, 'command_success_ratio', 0.25, 0, 100, 90, 300),        -- 指令成功率，权重25%，阈值100%，告警90%
(NULL, 'alarm_count', 0.15, 0, 100, 10, 300),                   -- 告警数量，权重15%，阈值100，告警10
(NULL, 'uptime_hours', 0.05, 0, 999999, 24, 1800),              -- 运行时间，权重5%，阈值无限，告警24小时
(NULL, 'maintenance_delay_days', 0.10, 0, 365, 30, 86400);     -- 维护延迟，权重10%，阈值365天，告警30天

-- 创建视图：设备健康状态总览
CREATE VIEW `v_device_health_overview` AS
SELECT
    d.device_id,
    d.device_name,
    d.device_sn,
    d.device_status,
    d.ip_address,
    COALESCE(hs.score_value, 0) as current_health_score,
    COALESCE(hs.level_code, 'unknown') as health_level,
    hs.snapshot_time as last_health_check,
    COALESCE(alert_count.unresolved_alerts, 0) as unresolved_alerts,
    COALESCE(maintenance_count.pending_maintenance, 0) as pending_maintenance,
    COALESCE(maintenance_count.in_progress_maintenance, 0) as in_progress_maintenance
FROM t_access_device d
LEFT JOIN (
    SELECT
        device_id,
        score_value,
        level_code,
        snapshot_time,
        ROW_NUMBER() OVER (PARTITION BY device_id ORDER BY snapshot_time DESC) as rn
    FROM t_device_health_snapshot
    WHERE deleted_flag = 0
) hs ON d.device_id = hs.device_id AND hs.rn = 1
LEFT JOIN (
    SELECT
        device_id,
        SUM(CASE WHEN resolved = 0 THEN 1 ELSE 0 END) as unresolved_alerts
    FROM t_device_health_alert
    WHERE deleted_flag = 0
    GROUP BY device_id
) alert_count ON d.device_id = alert_count.device_id
LEFT JOIN (
    SELECT
        device_id,
        SUM(CASE WHEN plan_status = 'pending' THEN 1 ELSE 0 END) as pending_maintenance,
        SUM(CASE WHEN plan_status = 'in-progress' THEN 1 ELSE 0 END) as in_progress_maintenance
    FROM t_device_maintenance_plan
    WHERE deleted_flag = 0
    GROUP BY device_id
) maintenance_count ON d.device_id = maintenance_count.device_id
WHERE d.deleted_flag = 0;

-- 创建索引优化查询性能
CREATE INDEX `idx_health_snapshot_device_time_score` ON `t_device_health_snapshot` (`device_id`, `snapshot_time` DESC, `score_value`);
CREATE INDEX `idx_health_metric_device_code_time` ON `t_device_health_metric` (`device_id`, `metric_code`, `metric_time` DESC);
CREATE INDEX `idx_alert_device_level_resolved` ON `t_device_health_alert` (`device_id`, `alert_level`, `resolved`, `alert_time` DESC);
CREATE INDEX `idx_maintenance_device_status_priority` ON `t_device_maintenance_plan` (`device_id`, `plan_status`, `priority_level`, `schedule_start`);