-- ============================================================================
-- IOE-DREAM 视频服务设备健康检查表
-- Task 10: 设备健康管理
-- Version: V20251226__003
-- Author: IOE-DREAM Team
-- Date: 2025-12-26
-- ============================================================================

-- 创建设备健康检查记录表
CREATE TABLE IF NOT EXISTS `t_video_device_health` (
    `health_id`              BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '健康检查ID',
    `device_id`              BIGINT          NOT NULL                 COMMENT '设备ID',
    `device_code`            VARCHAR(50)     NOT NULL                 COMMENT '设备编码',
    `device_name`            VARCHAR(100)    NOT NULL                 COMMENT '设备名称',
    `cpu_usage`              DECIMAL(5,2)    NOT NULL                 COMMENT 'CPU使用率(%)',
    `memory_usage`           DECIMAL(5,2)    NOT NULL                 COMMENT '内存使用率(%)',
    `disk_usage`             DECIMAL(5,2)    NOT NULL                 COMMENT '磁盘使用率(%)',
    `network_latency`        INT             NOT NULL                 COMMENT '网络延迟(ms)',
    `packet_loss`            DECIMAL(5,2)    NOT NULL                 COMMENT '丢包率(%)',
    `frame_rate`             INT             NOT NULL                 COMMENT '帧率(fps)',
    `temperature`            INT             NOT NULL                 COMMENT '设备温度(℃)',
    `uptime`                 INT             NOT NULL                 COMMENT '运行时间(分钟)',
    `health_score`           INT             NOT NULL                 COMMENT '健康评分(0-100)',
    `health_status`          TINYINT         NOT NULL                 COMMENT '健康状态: 0-健康 1-亚健康 2-不健康 3-严重故障',
    `alarm_level`            TINYINT         NOT NULL DEFAULT 0      COMMENT '告警级别: 0-正常 1-提示 2-警告 3-严重',
    `alarm_message`          VARCHAR(500)                             COMMENT '告警信息',
    `check_time`             DATETIME        NOT NULL                 COMMENT '检查时间',
    `create_time`            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag`           TINYINT         NOT NULL DEFAULT 0      COMMENT '删除标记: 0-未删除 1-已删除',
    PRIMARY KEY (`health_id`),
    INDEX `idx_health_device` (`device_id`),
    INDEX `idx_health_time` (`check_time`),
    INDEX `idx_health_status` (`health_status`),
    INDEX `idx_health_alarm` (`alarm_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备健康检查记录表' COMMENT='设备健康检查记录表';

-- 创建设备告警记录表
CREATE TABLE IF NOT EXISTS `t_video_device_health_alarm` (
    `alarm_id`               BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '告警ID',
    `device_id`              BIGINT          NOT NULL                 COMMENT '设备ID',
    `device_code`            VARCHAR(50)     NOT NULL                 COMMENT '设备编码',
    `device_name`            VARCHAR(100)    NOT NULL                 COMMENT '设备名称',
    `alarm_type`             VARCHAR(50)     NOT NULL                 COMMENT '告警类型: CPU_HIGH, MEMORY_HIGH, DISK_FULL等',
    `alarm_level`            TINYINT         NOT NULL                 COMMENT '告警级别: 1-提示 2-警告 3-严重',
    `alarm_title`            VARCHAR(200)    NOT NULL                 COMMENT '告警标题',
    `alarm_message`          TEXT            NOT NULL                 COMMENT '告警详情',
    `alarm_value`            VARCHAR(100)                             COMMENT '告警值',
    `threshold_value`        VARCHAR(100)                             COMMENT '阈值',
    `alarm_time`             DATETIME        NOT NULL                 COMMENT '告警时间',
    `handled_by`             BIGINT                                      COMMENT '处理人ID',
    `handled_time`           DATETIME                                     COMMENT '处理时间',
    `handle_result`          VARCHAR(500)                                COMMENT '处理结果',
    `alarm_status`           TINYINT         NOT NULL DEFAULT 0      COMMENT '告警状态: 0-未处理 1-处理中 2-已处理 3-已忽略',
    `remark`                 VARCHAR(500)                                COMMENT '备注',
    `create_time`            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag`           TINYINT         NOT NULL DEFAULT 0      COMMENT '删除标记: 0-未删除 1-已删除',
    PRIMARY KEY (`alarm_id`),
    INDEX `idx_alarm_device` (`device_id`),
    INDEX `idx_alarm_time` (`alarm_time`),
    INDEX `idx_alarm_level` (`alarm_level`),
    INDEX `idx_alarm_status` (`alarm_status`),
    INDEX `idx_alarm_type` (`alarm_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备告警记录表';
