-- ============================================================
-- IOE-DREAM Common Service - Audit模块
-- 表名: t_common_audit_archive (审计归档记录表)
-- 功能: 审计日志归档记录管理
-- 创建时间: 2025-01-30
-- ============================================================

CREATE TABLE IF NOT EXISTS `t_common_audit_archive` (
    `archive_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '归档记录ID',
    `archive_code` VARCHAR(64) NOT NULL COMMENT '归档编号（系统唯一）',
    `archive_time_point` DATETIME NOT NULL COMMENT '归档时间点（归档此时间点之前的日志）',
    `archive_count` INT NOT NULL COMMENT '归档日志数量',
    `archive_file_path` VARCHAR(500) NOT NULL COMMENT '归档文件路径',
    `archive_file_size` BIGINT DEFAULT 0 COMMENT '归档文件大小（字节）',
    `archive_file_format` VARCHAR(20) DEFAULT 'ZIP' COMMENT '归档文件格式（ZIP/CSV/EXCEL等）',
    `archive_status` TINYINT NOT NULL DEFAULT 1 COMMENT '归档状态：1-进行中 2-成功 3-失败',
    `archive_status_desc` VARCHAR(200) DEFAULT NULL COMMENT '归档状态描述',
    `archive_start_time` DATETIME DEFAULT NULL COMMENT '归档开始时间',
    `archive_end_time` DATETIME DEFAULT NULL COMMENT '归档结束时间',
    `archive_duration` BIGINT DEFAULT NULL COMMENT '归档耗时（毫秒）',
    `archive_user_id` BIGINT DEFAULT NULL COMMENT '归档操作人ID',
    `archive_user_name` VARCHAR(100) DEFAULT NULL COMMENT '归档操作人姓名',
    `archive_remark` VARCHAR(500) DEFAULT NULL COMMENT '归档备注',
    `archive_extensions` TEXT DEFAULT NULL COMMENT '归档扩展信息（JSON格式）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `update_user_id` BIGINT DEFAULT NULL COMMENT '更新人ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    PRIMARY KEY (`archive_id`),
    UNIQUE KEY `uk_archive_code` (`archive_code`),
    KEY `idx_archive_time_point` (`archive_time_point`),
    KEY `idx_archive_status` (`archive_status`),
    KEY `idx_archive_start_time` (`archive_start_time`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_archive_user_id` (`archive_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审计归档记录表';
