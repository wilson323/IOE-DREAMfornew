-- 批量设备导入功能数据库表
-- 作者: IOE-DREAM Team
-- 日期: 2025-01-30
-- 描述: 支持Excel批量导入设备，包含导入批次记录和错误详情

-- 导入批次记录表
CREATE TABLE IF NOT EXISTS `t_device_import_batch` (
    `batch_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '导入批次ID',
    `batch_name` VARCHAR(200) NOT NULL COMMENT '批次名称',
    `file_name` VARCHAR(200) NOT NULL COMMENT '导入文件名',
    `file_size` BIGINT NOT NULL DEFAULT 0 COMMENT '文件大小（字节）',
    `file_md5` VARCHAR(32) COMMENT '文件MD5值',

    -- 导入统计
    `total_count` INT NOT NULL DEFAULT 0 COMMENT '总记录数',
    `success_count` INT NOT NULL DEFAULT 0 COMMENT '成功数量',
    `failed_count` INT NOT NULL DEFAULT 0 COMMENT '失败数量',
    `skipped_count` INT NOT NULL DEFAULT 0 COMMENT '跳过数量（已存在）',

    -- 导入状态
    `import_status` TINYINT NOT NULL DEFAULT 0 COMMENT '导入状态: 0-处理中 1-成功 2-部分失败 3-全部失败',
    `status_message` VARCHAR(500) COMMENT '状态消息',
    `error_summary` TEXT COMMENT '错误摘要（JSON格式）',

    -- 时间信息
    `start_time` DATETIME NOT NULL COMMENT '开始时间',
    `end_time` DATETIME COMMENT '结束时间',
    `duration_ms` BIGINT COMMENT '耗时（毫秒）',

    -- 操作信息
    `operator_type` TINYINT COMMENT '操作人类型: 1-管理员 2-普通用户',
    `operator_id` BIGINT COMMENT '操作人ID',
    `operator_name` VARCHAR(100) COMMENT '操作人姓名',

    -- 审计字段
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记: 0-未删除 1-已删除',

    PRIMARY KEY (`batch_id`),
    KEY `idx_import_status` (`import_status`),
    KEY `idx_operator` (`operator_id`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_deleted` (`deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备导入批次记录表';

-- 导入错误明细表
CREATE TABLE IF NOT EXISTS `t_device_import_error` (
    `error_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '错误ID',
    `batch_id` BIGINT NOT NULL COMMENT '批次ID',
    `row_number` INT NOT NULL COMMENT '行号',

    -- 原始数据
    `raw_data` TEXT COMMENT '原始数据（JSON格式）',

    -- 错误信息
    `error_code` VARCHAR(50) COMMENT '错误码',
    `error_message` VARCHAR(500) COMMENT '错误消息',
    `error_field` VARCHAR(100) COMMENT '错误字段',

    -- 数据验证结果
    `validation_errors` TEXT COMMENT '验证错误详情（JSON格式）',

    -- 时间信息
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    PRIMARY KEY (`error_id`),
    KEY `idx_batch_id` (`batch_id`),
    KEY `idx_error_code` (`error_code`),
    CONSTRAINT `fk_import_error_batch` FOREIGN KEY (`batch_id`)
        REFERENCES `t_device_import_batch` (`batch_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备导入错误明细表';

-- 导入成功记录表（可选，用于追踪导入记录）
CREATE TABLE IF NOT EXISTS `t_device_import_success` (
    `success_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '成功ID',
    `batch_id` BIGINT NOT NULL COMMENT '批次ID',
    `device_id` BIGINT COMMENT '导入后的设备ID',
    `row_number` INT NOT NULL COMMENT '行号',

    -- 导入数据快照（用于追溯）
    `device_code` VARCHAR(50) NOT NULL COMMENT '设备编码',
    `device_name` VARCHAR(100) COMMENT '设备名称',
    `device_type` TINYINT COMMENT '设备类型',
    `imported_data` TEXT COMMENT '导入的数据（JSON格式）',

    -- 时间信息
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    PRIMARY KEY (`success_id`),
    KEY `idx_batch_id` (`batch_id`),
    KEY `idx_device_code` (`device_code`),
    KEY `idx_device_id` (`device_id`),
    CONSTRAINT `fk_import_success_batch` FOREIGN KEY (`batch_id`)
        REFERENCES `t_device_import_batch` (`batch_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备导入成功记录表';

-- 插入测试数据（可选）
INSERT INTO `t_device_import_batch`
    (`batch_name`, `file_name`, `total_count`, `success_count`, `import_status`, `start_time`, `end_time`, `duration_ms`, `operator_id`, `operator_name`)
VALUES
    ('测试导入批次', 'devices_20250130.xlsx', 10, 8, 2, NOW(), NOW(), 1500, 1, '管理员');
