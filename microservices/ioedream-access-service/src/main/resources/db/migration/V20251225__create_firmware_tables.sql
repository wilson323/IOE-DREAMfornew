-- ================================================================================
-- IOE-DREAM 智慧园区一卡通管理平台
-- 门禁管理模块 - 固件升级管理功能
-- ================================================================================
-- 功能说明：
--   1. 设备固件版本管理（上传、存储、下载）
--   2. 固件升级任务管理（批量升级、进度跟踪）
--   3. 升级历史记录（成功/失败、回滚支持）
--
-- 作者：IOE-DREAM Team
-- 日期：2025-12-25
-- 版本：1.0.0
-- ================================================================================

-- ================================================================================
-- 1. 设备固件信息表
-- ================================================================================
CREATE TABLE IF NOT EXISTS `t_device_firmware` (
    `firmware_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '固件ID（主键）',
    `firmware_name` VARCHAR(200) NOT NULL COMMENT '固件名称',
    `firmware_version` VARCHAR(50) NOT NULL COMMENT '固件版本号（如：v1.0.0）',
    `device_type` INT NOT NULL COMMENT '设备类型：1-门禁 2-考勤 3-消费 4-视频 5-访客',
    `device_model` VARCHAR(100) DEFAULT NULL COMMENT '适用设备型号（如：AC-2000）',
    `brand` VARCHAR(100) DEFAULT NULL COMMENT '设备品牌（如：Hikvision）',
    `firmware_file_path` VARCHAR(500) NOT NULL COMMENT '固件文件存储路径',
    `firmware_file_name` VARCHAR(200) NOT NULL COMMENT '固件文件名称',
    `firmware_file_size` BIGINT NOT NULL COMMENT '固件文件大小（字节）',
    `firmware_file_md5` VARCHAR(32) NOT NULL COMMENT '固件文件MD5校验值',
    `release_notes` TEXT DEFAULT NULL COMMENT '版本更新说明',
    `min_version` VARCHAR(50) DEFAULT NULL COMMENT '最低可升级版本（空表示无限制）',
    `max_version` VARCHAR(50) DEFAULT NULL COMMENT '最高可升级版本（空表示无限制）',
    `is_force` TINYINT DEFAULT 0 COMMENT '是否强制升级：0-否 1-是',
    `is_enabled` TINYINT DEFAULT 1 COMMENT '是否启用：0-禁用 1-启用',
    `upload_time` DATETIME NOT NULL COMMENT '上传时间',
    `uploader_id` BIGINT NOT NULL COMMENT '上传人ID',
    `uploader_name` VARCHAR(100) NOT NULL COMMENT '上传人姓名',
    `download_count` INT DEFAULT 0 COMMENT '下载次数',
    `upgrade_count` INT DEFAULT 0 COMMENT '被升级次数',
    `firmware_status` TINYINT DEFAULT 1 COMMENT '固件状态：1-测试中 2-正式发布 3-已废弃',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`firmware_id`),
    UNIQUE KEY `uk_firmware_version` (`device_type`, `device_model`, `firmware_version`),
    KEY `idx_device_type` (`device_type`),
    KEY `idx_brand_model` (`brand`, `device_model`),
    KEY `idx_firmware_status` (`firmware_status`),
    KEY `idx_upload_time` (`upload_time`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备固件信息表';

-- ================================================================================
-- 2. 固件升级任务表
-- ================================================================================
CREATE TABLE IF NOT EXISTS `t_firmware_upgrade_task` (
    `task_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '任务ID（主键）',
    `task_name` VARCHAR(200) NOT NULL COMMENT '任务名称',
    `task_no` VARCHAR(50) NOT NULL COMMENT '任务编号（如：UPG202512250001）',
    `firmware_id` BIGINT NOT NULL COMMENT '固件ID',
    `firmware_version` VARCHAR(50) NOT NULL COMMENT '固件版本号',
    `upgrade_strategy` TINYINT NOT NULL DEFAULT 1 COMMENT '升级策略：1-立即升级 2-定时升级 3-分批升级',
    `schedule_time` DATETIME DEFAULT NULL COMMENT '定时升级时间（strategy=2时使用）',
    `batch_size` INT DEFAULT 10 COMMENT '分批大小（strategy=3时使用）',
    `batch_interval` INT DEFAULT 300 COMMENT '分批间隔（秒）',
    `target_device_count` INT NOT NULL DEFAULT 0 COMMENT '目标设备总数',
    `success_count` INT NOT NULL DEFAULT 0 COMMENT '升级成功数量',
    `failed_count` INT NOT NULL DEFAULT 0 COMMENT '升级失败数量',
    `pending_count` INT NOT NULL DEFAULT 0 COMMENT '待升级数量',
    `upgrade_progress` DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '升级进度（百分比）',
    `task_status` TINYINT NOT NULL DEFAULT 1 COMMENT '任务状态：1-待执行 2-执行中 3-已暂停 4-已完成 5-已失败',
    `start_time` DATETIME DEFAULT NULL COMMENT '开始执行时间',
    `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
    `duration_seconds` INT DEFAULT NULL COMMENT '执行耗时（秒）',
    `operator_id` BIGINT NOT NULL COMMENT '操作人ID',
    `operator_name` VARCHAR(100) NOT NULL COMMENT '操作人姓名',
    `rollback_supported` TINYINT DEFAULT 0 COMMENT '是否支持回滚：0-否 1-是',
    `rollback_task_id` BIGINT DEFAULT NULL COMMENT '回滚任务ID',
    `error_message` TEXT DEFAULT NULL COMMENT '错误信息',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`task_id`),
    UNIQUE KEY `uk_task_no` (`task_no`),
    KEY `idx_firmware_id` (`firmware_id`),
    KEY `idx_task_status` (`task_status`),
    KEY `idx_schedule_time` (`schedule_time`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_operator_id` (`operator_id`),
    CONSTRAINT `fk_firmware_upgrade_firmware` FOREIGN KEY (`firmware_id`) REFERENCES `t_device_firmware` (`firmware_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='固件升级任务表';

-- ================================================================================
-- 3. 固件升级设备明细表
-- ================================================================================
CREATE TABLE IF NOT EXISTS `t_firmware_upgrade_device` (
    `detail_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '明细ID（主键）',
    `task_id` BIGINT NOT NULL COMMENT '任务ID',
    `device_id` BIGINT NOT NULL COMMENT '设备ID',
    `device_code` VARCHAR(100) NOT NULL COMMENT '设备编码',
    `device_name` VARCHAR(200) NOT NULL COMMENT '设备名称',
    `device_ip` VARCHAR(50) NOT NULL COMMENT '设备IP地址',
    `current_version` VARCHAR(50) DEFAULT NULL COMMENT '当前固件版本',
    `target_version` VARCHAR(50) NOT NULL COMMENT '目标固件版本',
    `upgrade_status` TINYINT NOT NULL DEFAULT 1 COMMENT '升级状态：1-待升级 2-升级中 3-升级成功 4-升级失败 5-已回滚',
    `start_time` DATETIME DEFAULT NULL COMMENT '开始升级时间',
    `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
    `duration_seconds` INT DEFAULT NULL COMMENT '耗时（秒）',
    `retry_count` INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    `max_retry` INT NOT NULL DEFAULT 3 COMMENT '最大重试次数',
    `error_code` VARCHAR(50) DEFAULT NULL COMMENT '错误代码',
    `error_message` TEXT DEFAULT NULL COMMENT '错误信息',
    `upgrade_log` TEXT DEFAULT NULL COMMENT '升级日志（JSON格式）',
    `is_rollback` TINYINT DEFAULT 0 COMMENT '是否已回滚：0-否 1-是',
    `rollback_time` DATETIME DEFAULT NULL COMMENT '回滚时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`detail_id`),
    KEY `idx_task_id` (`task_id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_upgrade_status` (`upgrade_status`),
    KEY `idx_start_time` (`start_time`),
    CONSTRAINT `fk_firmware_upgrade_task` FOREIGN KEY (`task_id`) REFERENCES `t_firmware_upgrade_task` (`task_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='固件升级设备明细表';

-- ================================================================================
-- 4. 创建索引优化查询性能
-- ================================================================================
-- 固件升级设备明细表复合索引
CREATE INDEX `idx_task_status_time` ON `t_firmware_upgrade_device` (`task_id`, `upgrade_status`, `start_time`);
CREATE INDEX `idx_device_status` ON `t_firmware_upgrade_device` (`device_id`, `upgrade_status`);

-- ================================================================================
-- 5. 初始化数据（可选）
-- ================================================================================
-- 示例：插入一个测试固件记录
INSERT INTO `t_device_firmware` (
    `firmware_name`, `firmware_version`, `device_type`, `device_model`, `brand`,
    `firmware_file_path`, `firmware_file_name`, `firmware_file_size`, `firmware_file_md5`,
    `release_notes`, `is_force`, `is_enabled`, `upload_time`, `uploader_id`, `uploader_name`
) VALUES (
    'AC-2000固件v1.0.0', 'v1.0.0', 1, 'AC-2000', 'Hikvision',
    '/firmware/ac2000_v1.0.0.bin', 'ac2000_v1.0.0.bin', 1024000, '5d41402abc4b2a76b9719d911017c592',
    '首次发布版本，修复多个已知问题', 0, 1, NOW(), 1, '系统管理员'
);
