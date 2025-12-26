-- ============================================================================
-- IOE-DREAM 视频服务固件升级表
-- Task 9: 固件升级管理
-- Version: V20251226__002
-- Author: IOE-DREAM Team
-- Date: 2025-12-26
-- ============================================================================

-- 创建固件升级记录表
CREATE TABLE IF NOT EXISTS `t_video_firmware_upgrade` (
    `upgrade_id`             BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '升级ID',
    `device_id`              BIGINT          NOT NULL                 COMMENT '设备ID',
    `device_code`            VARCHAR(50)     NOT NULL                 COMMENT '设备编码',
    `device_name`            VARCHAR(100)    NOT NULL                 COMMENT '设备名称',
    `current_version`        VARCHAR(20)     NOT NULL                 COMMENT '当前版本',
    `target_version`         VARCHAR(20)     NOT NULL                 COMMENT '目标版本',
    `firmware_url`           VARCHAR(500)    NOT NULL                 COMMENT '固件文件URL',
    `file_size`              BIGINT          NOT NULL                 COMMENT '文件大小(字节)',
    `file_md5`               VARCHAR(32)                              COMMENT '文件MD5校验值',
    `upgrade_type`           TINYINT         NOT NULL DEFAULT 1      COMMENT '升级类型: 1-完整升级 2-增量升级 3-补丁升级',
    `upgrade_status`         TINYINT         NOT NULL DEFAULT 0      COMMENT '升级状态: 0-待升级 1-升级中 2-下载中 3-升级完成 4-升级失败',
    `progress`               INT             NOT NULL DEFAULT 0      COMMENT '升级进度(0-100)',
    `error_message`          VARCHAR(500)                             COMMENT '错误信息',
    `start_time`             DATETIME                                     COMMENT '开始时间',
    `end_time`               DATETIME                                     COMMENT '结束时间',
    `upgraded_by`            BIGINT                                      COMMENT '升级操作人ID',
    `remark`                 VARCHAR(500)                                COMMENT '备注',
    `create_time`            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag`           TINYINT         NOT NULL DEFAULT 0      COMMENT '删除标记: 0-未删除 1-已删除',
    PRIMARY KEY (`upgrade_id`),
    INDEX `idx_upgrade_device` (`device_id`),
    INDEX `idx_upgrade_status` (`upgrade_status`),
    INDEX `idx_upgrade_time` (`start_time`),
    INDEX `idx_upgrade_code` (`device_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='固件升级记录表';

-- 创建固件升级日志表（记录升级过程中的详细日志）
CREATE TABLE IF NOT EXISTS `t_video_firmware_upgrade_log` (
    `log_id`                 BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '日志ID',
    `upgrade_id`             BIGINT          NOT NULL                 COMMENT '升级ID',
    `log_level`              VARCHAR(10)     NOT NULL                 COMMENT '日志级别: INFO, WARN, ERROR',
    `log_message`            TEXT            NOT NULL                 COMMENT '日志内容',
    `log_time`               DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '日志时间',
    PRIMARY KEY (`log_id`),
    INDEX `idx_log_upgrade` (`upgrade_id`),
    INDEX `idx_log_time` (`log_time`),
    FOREIGN KEY (`upgrade_id`) REFERENCES `t_video_firmware_upgrade`(`upgrade_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='固件升级日志表';
