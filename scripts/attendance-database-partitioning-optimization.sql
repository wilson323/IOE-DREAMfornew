-- 考勤模块数据库分区优化方案
-- 适用于MySQL 8.0+版本，针对大数据量场景优化

-- ========================================
-- 1. 考勤记录表分区优化 (t_attendance_record)
-- ========================================

-- 注意：分区操作需要先备份数据，且会锁表较长时间
-- 建议在维护窗口期执行

-- 1.1 创建新的分区表结构
CREATE TABLE IF NOT EXISTS `t_attendance_record_partitioned` (
    `record_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `employee_id` BIGINT NOT NULL COMMENT '员工ID',
    `attendance_date` DATE NOT NULL COMMENT '考勤日期',
    `clock_time` DATETIME NOT NULL COMMENT '打卡时间',
    `punch_in_time` TIME DEFAULT NULL COMMENT '上班打卡时间',
    `punch_out_time` TIME DEFAULT NULL COMMENT '下班打卡时间',
    `clock_type` VARCHAR(16) NULL COMMENT '打卡类型(IN/OUT)',
    `record_type` VARCHAR(20) NOT NULL DEFAULT 'PUNCH' COMMENT '记录类型',
    `device_id` BIGINT NULL COMMENT '设备ID',
    `punch_in_location` VARCHAR(500) DEFAULT NULL COMMENT '上班打卡位置',
    `punch_out_location` VARCHAR(500) DEFAULT NULL COMMENT '下班打卡位置',
    `punch_in_device_id` BIGINT DEFAULT NULL COMMENT '上班打卡设备ID',
    `punch_out_device_id` BIGINT DEFAULT NULL COMMENT '下班打卡设备ID',
    `work_hours` DECIMAL(4,2) DEFAULT NULL COMMENT '工作时长(小时)',
    `overtime_hours` DECIMAL(4,2) DEFAULT NULL COMMENT '加班时长(小时)',
    `attendance_status` VARCHAR(20) DEFAULT 'NORMAL' COMMENT '考勤状态',
    `exception_type` VARCHAR(50) DEFAULT NULL COMMENT '异常类型',
    `exception_reason` VARCHAR(500) DEFAULT NULL COMMENT '异常原因',
    `is_processed` TINYINT(1) DEFAULT '0' COMMENT '是否已处理异常',
    `processed_by` BIGINT DEFAULT NULL COMMENT '异常处理人',
    `processed_time` DATETIME DEFAULT NULL COMMENT '异常处理时间',
    `remark` VARCHAR(512) NULL COMMENT '备注',
    -- 审计字段
    `create_time` DATETIME NULL,
    `update_time` DATETIME NULL,
    `create_user_id` BIGINT NULL,
    `update_user_id` BIGINT NULL,
    `deleted_flag` TINYINT NOT NULL DEFAULT 0,
    `version` INT NOT NULL DEFAULT 0,
    PRIMARY KEY (`record_id`, `attendance_date`),
    KEY `idx_employee_date` (`employee_id`, `attendance_date`),
    KEY `idx_attendance_date` (`attendance_date`),
    KEY `idx_status` (`attendance_status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
PARTITION BY RANGE (TO_DAYS(attendance_date)) (
    PARTITION p202301 VALUES LESS THAN (TO_DAYS('2023-02-01')) COMMENT = '2023年1月',
    PARTITION p202302 VALUES LESS THAN (TO_DAYS('2023-03-01')) COMMENT = '2023年2月',
    PARTITION p202303 VALUES LESS THAN (TO_DAYS('2023-04-01')) COMMENT = '2023年3月',
    PARTITION p202304 VALUES LESS THAN (TO_DAYS('2023-05-01')) COMMENT = '2023年4月',
    PARTITION p202305 VALUES LESS THAN (TO_DAYS('2023-06-01')) COMMENT = '2023年5月',
    PARTITION p202306 VALUES LESS THAN (TO_DAYS('2023-07-01')) COMMENT = '2023年6月',
    PARTITION p202307 VALUES LESS THAN (TO_DAYS('2023-08-01')) COMMENT = '2023年7月',
    PARTITION p202308 VALUES LESS THAN (TO_DAYS('2023-09-01')) COMMENT = '2023年8月',
    PARTITION p202309 VALUES LESS THAN (TO_DAYS('2023-10-01')) COMMENT = '2023年9月',
    PARTITION p202310 VALUES LESS THAN (TO_DAYS('2023-11-01')) COMMENT = '2023年10月',
    PARTITION p202311 VALUES LESS THAN (TO_DAYS('2023-12-01')) COMMENT = '2023年11月',
    PARTITION p202312 VALUES LESS THAN (TO_DAYS('2024-01-01')) COMMENT = '2023年12月',
    PARTITION p202401 VALUES LESS THAN (TO_DAYS('2024-02-01')) COMMENT = '2024年1月',
    PARTITION p202402 VALUES LESS THAN (TO_DAYS('2024-03-01')) COMMENT = '2024年2月',
    PARTITION p202403 VALUES LESS THAN (TO_DAYS('2024-04-01')) COMMENT = '2024年3月',
    PARTITION p202404 VALUES LESS THAN (TO_DAYS('2024-05-01')) COMMENT = '2024年4月',
    PARTITION p202405 VALUES LESS THAN (TO_DAYS('2024-06-01')) COMMENT = '2024年5月',
    PARTITION p202406 VALUES LESS THAN (TO_DAYS('2024-07-01')) COMMENT = '2024年6月',
    PARTITION p202407 VALUES LESS THAN (TO_DAYS('2024-08-01')) COMMENT = '2024年7月',
    PARTITION p202408 VALUES LESS THAN (TO_DAYS('2024-09-01')) COMMENT = '2024年8月',
    PARTITION p202409 VALUES LESS THAN (TO_DAYS('2024-10-01')) COMMENT = '2024年9月',
    PARTITION p202410 VALUES LESS THAN (TO_DAYS('2024-11-01')) COMMENT = '2024年10月',
    PARTITION p202411 VALUES LESS THAN (TO_DAYS('2024-12-01')) COMMENT = '2024年11月',
    PARTITION p202412 VALUES LESS THAN (TO_DAYS('2025-01-01')) COMMENT = '2024年12月',
    PARTITION p202501 VALUES LESS THAN (TO_DAYS('2025-02-01')) COMMENT = '2025年1月',
    PARTITION p202502 VALUES LESS THAN (TO_DAYS('2025-03-01')) COMMENT = '2025年2月',
    PARTITION p202503 VALUES LESS THAN (TO_DAYS('2025-04-01')) COMMENT = '2025年3月',
    PARTITION p202504 VALUES LESS THAN (TO_DAYS('2025-05-01')) COMMENT = '2025年4月',
    PARTITION p202505 VALUES LESS THAN (TO_DAYS('2025-06-01')) COMMENT = '2025年5月',
    PARTITION p202506 VALUES LESS THAN (TO_DAYS('2025-07-01')) COMMENT = '2025年6月',
    PARTITION p202507 VALUES LESS THAN (TO_DAYS('2025-08-01')) COMMENT = '2025年7月',
    PARTITION p202508 VALUES LESS THAN (TO_DAYS('2025-09-01')) COMMENT = '2025年8月',
    PARTITION p202509 VALUES LESS THAN (TO_DAYS('2025-10-01')) COMMENT = '2025年9月',
    PARTITION p202510 VALUES LESS THAN (TO_DAYS('2025-11-01')) COMMENT = '2025年10月',
    PARTITION p202511 VALUES LESS THAN (TO_DAYS('2025-12-01')) COMMENT = '2025年11月',
    PARTITION p202512 VALUES LESS THAN (TO_DAYS('2026-01-01')) COMMENT = '2025年12月',
    PARTITION p_future VALUES LESS THAN MAXVALUE COMMENT = '未来数据'
) COMMENT='考勤打卡记录分区表';

-- 1.2 迁移数据到分区表（需要在维护窗口期执行）
-- INSERT INTO t_attendance_record_partitioned SELECT * FROM t_attendance_record;

-- 1.3 验证数据完整性
-- SELECT COUNT(*) FROM t_attendance_record; -- 原表记录数
-- SELECT COUNT(*) FROM t_attendance_record_partitioned; -- 分区表记录数

-- 1.4 重命名表（谨慎操作）
-- RENAME TABLE t_attendance_record TO t_attendance_record_backup;
-- RENAME TABLE t_attendance_record_partitioned TO t_attendance_record;

-- ========================================
-- 2. 排班表分区优化 (t_attendance_schedule)
-- ========================================

-- 2.1 创建分区表结构
CREATE TABLE IF NOT EXISTS `t_attendance_schedule_partitioned` (
    `schedule_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '排班ID',
    `employee_id` BIGINT NOT NULL COMMENT '员工ID',
    `schedule_date` DATE NOT NULL COMMENT '排班日期',
    `shift_id` BIGINT NOT NULL COMMENT '班次ID',
    `work_start_time` TIME NOT NULL COMMENT '工作开始时间',
    `work_end_time` TIME NOT NULL COMMENT '工作结束时间',
    `break_start_time` TIME DEFAULT NULL COMMENT '休息开始时间',
    `break_end_time` TIME DEFAULT NULL COMMENT '休息结束时间',
    `work_hours` DECIMAL(4,2) NOT NULL COMMENT '工作时长(小时)',
    `is_holiday` TINYINT(1) DEFAULT 0 COMMENT '是否节假日',
    `is_overtime_day` TINYINT(1) DEFAULT 0 COMMENT '是否加班日',
    `schedule_type` VARCHAR(20) DEFAULT 'NORMAL' COMMENT '排班类型',
    `remarks` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记 0-正常 1-删除',
    `create_user_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `update_user_id` BIGINT DEFAULT NULL COMMENT '更新人ID',
    `version` INT NOT NULL DEFAULT 0 COMMENT '版本号（乐观锁）',
    PRIMARY KEY (`schedule_id`, `schedule_date`),
    UNIQUE KEY `uk_employee_date` (`employee_id`, `schedule_date`),
    KEY `idx_schedule_date` (`schedule_date`),
    KEY `idx_shift_id` (`shift_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
PARTITION BY RANGE (TO_DAYS(schedule_date)) (
    PARTITION p202301 VALUES LESS THAN (TO_DAYS('2023-02-01')) COMMENT = '2023年1月',
    PARTITION p202302 VALUES LESS THAN (TO_DAYS('2023-03-01')) COMMENT = '2023年2月',
    PARTITION p202303 VALUES LESS THAN (TO_DAYS('2023-04-01')) COMMENT = '2023年3月',
    PARTITION p202304 VALUES LESS THAN (TO_DAYS('2023-05-01')) COMMENT = '2023年4月',
    PARTITION p202305 VALUES LESS THAN (TO_DAYS('2023-06-01')) COMMENT = '2023年5月',
    PARTITION p202306 VALUES LESS THAN (TO_DAYS('2023-07-01')) COMMENT = '2023年6月',
    PARTITION p202307 VALUES LESS THAN (TO_DAYS('2023-08-01')) COMMENT = '2023年7月',
    PARTITION p202308 VALUES LESS THAN (TO_DAYS('2023-09-01')) COMMENT = '2023年8月',
    PARTITION p202309 VALUES LESS THAN (TO_DAYS('2023-10-01')) COMMENT = '2023年9月',
    PARTITION p202310 VALUES LESS THAN (TO_DAYS('2023-11-01')) COMMENT = '2023年10月',
    PARTITION p202311 VALUES LESS THAN (TO_DAYS('2023-12-01')) COMMENT = '2023年11月',
    PARTITION p202312 VALUES LESS THAN (TO_DAYS('2024-01-01')) COMMENT = '2023年12月',
    PARTITION p202401 VALUES LESS THAN (TO_DAYS('2024-02-01')) COMMENT = '2024年1月',
    PARTITION p202402 VALUES LESS THAN (TO_DAYS('2024-03-01')) COMMENT = '2024年2月',
    PARTITION p202403 VALUES LESS THAN (TO_DAYS('2024-04-01')) COMMENT = '2024年3月',
    PARTITION p202404 VALUES LESS THAN (TO_DAYS('2024-05-01')) COMMENT = '2024年4月',
    PARTITION p202405 VALUES LESS THAN (TO_DAYS('2024-06-01')) COMMENT = '2024年5月',
    PARTITION p202406 VALUES LESS THAN (TO_DAYS('2024-07-01')) COMMENT = '2024年6月',
    PARTITION p202407 VALUES LESS THAN (TO_DAYS('2024-08-01')) COMMENT = '2024年7月',
    PARTITION p202408 VALUES LESS THAN (TO_DAYS('2024-09-01')) COMMENT = '2024年8月',
    PARTITION p202409 VALUES LESS THAN (TO_DAYS('2024-10-01')) COMMENT = '2024年9月',
    PARTITION p202410 VALUES LESS THAN (TO_DAYS('2024-11-01')) COMMENT = '2024年10月',
    PARTITION p202411 VALUES LESS THAN (TO_DAYS('2024-12-01')) COMMENT = '2024年11月',
    PARTITION p202412 VALUES LESS THAN (TO_DAYS('2025-01-01')) COMMENT = '2024年12月',
    PARTITION p202501 VALUES LESS THAN (TO_DAYS('2025-02-01')) COMMENT = '2025年1月',
    PARTITION p202502 VALUES LESS THAN (TO_DAYS('2025-03-01')) COMMENT = '2025年2月',
    PARTITION p202503 VALUES LESS THAN (TO_DAYS('2025-04-01')) COMMENT = '2025年3月',
    PARTITION p202504 VALUES LESS THAN (TO_DAYS('2025-05-01')) COMMENT = '2025年4月',
    PARTITION p202505 VALUES LESS THAN (TO_DAYS('2025-06-01')) COMMENT = '2025年5月',
    PARTITION p202506 VALUES LESS THAN (TO_DAYS('2025-07-01')) COMMENT = '2025年6月',
    PARTITION p202507 VALUES LESS THAN (TO_DAYS('2025-08-01')) COMMENT = '2025年7月',
    PARTITION p202508 VALUES LESS THAN (TO_DAYS('2025-09-01')) COMMENT = '2025年8月',
    PARTITION p202509 VALUES LESS THAN (TO_DAYS('2025-10-01')) COMMENT = '2025年9月',
    PARTITION p202510 VALUES LESS THAN (TO_DAYS('2025-11-01')) COMMENT = '2025年10月',
    PARTITION p202511 VALUES LESS THAN (TO_DAYS('2025-12-01')) COMMENT = '2025年11月',
    PARTITION p202512 VALUES LESS THAN (TO_DAYS('2026-01-01')) COMMENT = '2025年12月',
    PARTITION p_future VALUES LESS THAN MAXVALUE COMMENT = '未来数据'
) COMMENT='排班管理分区表';

-- ========================================
-- 3. 考勤统计表分区优化 (t_attendance_statistics)
-- ========================================

-- 3.1 创建分区表结构
CREATE TABLE IF NOT EXISTS `t_attendance_statistics_partitioned` (
    `statistics_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '统计ID',
    `employee_id` BIGINT NOT NULL COMMENT '员工ID',
    `statistics_type` VARCHAR(20) NOT NULL COMMENT '统计类型',
    `statistics_period` VARCHAR(20) NOT NULL COMMENT '统计周期',
    `period_value` VARCHAR(20) NOT NULL COMMENT '周期值',
    `total_days` INT(11) NOT NULL DEFAULT 0 COMMENT '总天数',
    `present_days` INT(11) NOT NULL DEFAULT 0 COMMENT '出勤天数',
    `absent_days` INT(11) NOT NULL DEFAULT 0 COMMENT '缺勤天数',
    `late_days` INT(11) NOT NULL DEFAULT 0 COMMENT '迟到天数',
    `early_leave_days` INT(11) NOT NULL DEFAULT 0 COMMENT '早退天数',
    `leave_days` INT(11) NOT NULL DEFAULT 0 COMMENT '请假天数',
    `overtime_hours` DECIMAL(8,2) NOT NULL DEFAULT 0.00 COMMENT '加班时长(小时)',
    `work_hours` DECIMAL(8,2) NOT NULL DEFAULT 0.00 COMMENT '工作时长(小时)',
    `attendance_rate` DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '出勤率(%)',
    `statistics_date` DATE NOT NULL COMMENT '统计日期',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记 0-正常 1-删除',
    `create_user_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `update_user_id` BIGINT DEFAULT NULL COMMENT '更新人ID',
    `version` INT NOT NULL DEFAULT 0 COMMENT '版本号（乐观锁）',
    PRIMARY KEY (`statistics_id`, `statistics_date`),
    UNIQUE KEY `uk_employee_period` (`employee_id`, `statistics_type`, `period_value`),
    KEY `idx_statistics_date` (`statistics_date`),
    KEY `idx_statistics_type` (`statistics_type`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
PARTITION BY RANGE (TO_DAYS(statistics_date)) (
    PARTITION p202301 VALUES LESS THAN (TO_DAYS('2023-02-01')) COMMENT = '2023年1月',
    PARTITION p202302 VALUES LESS THAN (TO_DAYS('2023-03-01')) COMMENT = '2023年2月',
    PARTITION p202303 VALUES LESS THAN (TO_DAYS('2023-04-01')) COMMENT = '2023年3月',
    PARTITION p202304 VALUES LESS THAN (TO_DAYS('2023-05-01')) COMMENT = '2023年4月',
    PARTITION p202305 VALUES LESS THAN (TO_DAYS('2023-06-01')) COMMENT = '2023年5月',
    PARTITION p202306 VALUES LESS THAN (TO_DAYS('2023-07-01')) COMMENT = '2023年6月',
    PARTITION p202307 VALUES LESS THAN (TO_DAYS('2023-08-01')) COMMENT = '2023年7月',
    PARTITION p202308 VALUES LESS THAN (TO_DAYS('2023-09-01')) COMMENT = '2023年8月',
    PARTITION p202309 VALUES LESS THAN (TO_DAYS('2023-10-01')) COMMENT = '2023年9月',
    PARTITION p202310 VALUES LESS THAN (TO_DAYS('2023-11-01')) COMMENT = '2023年10月',
    PARTITION p202311 VALUES LESS THAN (TO_DAYS('2023-12-01')) COMMENT = '2023年11月',
    PARTITION p202312 VALUES LESS THAN (TO_DAYS('2024-01-01')) COMMENT = '2023年12月',
    PARTITION p202401 VALUES LESS THAN (TO_DAYS('2024-02-01')) COMMENT = '2024年1月',
    PARTITION p202402 VALUES LESS THAN (TO_DAYS('2024-03-01')) COMMENT = '2024年2月',
    PARTITION p202403 VALUES LESS THAN (TO_DAYS('2024-04-01')) COMMENT = '2024年3月',
    PARTITION p202404 VALUES LESS THAN (TO_DAYS('2024-05-01')) COMMENT = '2024年4月',
    PARTITION p202405 VALUES LESS THAN (TO_DAYS('2024-06-01')) COMMENT = '2024年5月',
    PARTITION p202406 VALUES LESS THAN (TO_DAYS('2024-07-01')) COMMENT = '2024年6月',
    PARTITION p202407 VALUES LESS THAN (TO_DAYS('2024-08-01')) COMMENT = '2024年7月',
    PARTITION p202408 VALUES LESS THAN (TO_DAYS('2024-09-01')) COMMENT = '2024年8月',
    PARTITION p202409 VALUES LESS THAN (TO_DAYS('2024-10-01')) COMMENT = '2024年9月',
    PARTITION p202410 VALUES LESS THAN (TO_DAYS('2024-11-01')) COMMENT = '2024年10月',
    PARTITION p202411 VALUES LESS THAN (TO_DAYS('2024-12-01')) COMMENT = '2024年11月',
    PARTITION p202412 VALUES LESS THAN (TO_DAYS('2025-01-01')) COMMENT = '2024年12月',
    PARTITION p202501 VALUES LESS THAN (TO_DAYS('2025-02-01')) COMMENT = '2025年1月',
    PARTITION p202502 VALUES LESS THAN (TO_DAYS('2025-03-01')) COMMENT = '2025年2月',
    PARTITION p202503 VALUES LESS THAN (TO_DAYS('2025-04-01')) COMMENT = '2025年3月',
    PARTITION p202504 VALUES LESS THAN (TO_DAYS('2025-05-01')) COMMENT = '2025年4月',
    PARTITION p202505 VALUES LESS THAN (TO_DAYS('2025-06-01')) COMMENT = '2025年5月',
    PARTITION p202506 VALUES LESS THAN (TO_DAYS('2025-07-01')) COMMENT = '2025年6月',
    PARTITION p202507 VALUES LESS THAN (TO_DAYS('2025-08-01')) COMMENT = '2025年7月',
    PARTITION p202508 VALUES LESS THAN (TO_DAYS('2025-09-01')) COMMENT = '2025年8月',
    PARTITION p202509 VALUES LESS THAN (TO_DAYS('2025-10-01')) COMMENT = '2025年9月',
    PARTITION p202510 VALUES LESS THAN (TO_DAYS('2025-11-01')) COMMENT = '2025年10月',
    PARTITION p202511 VALUES LESS THAN (TO_DAYS('2025-12-01')) COMMENT = '2025年11月',
    PARTITION p202512 VALUES LESS THAN (TO_DAYS('2026-01-01')) COMMENT = '2025年12月',
    PARTITION p_future VALUES LESS THAN MAXVALUE COMMENT = '未来数据'
) COMMENT='考勤统计分区表';

-- ========================================
-- 4. 分区维护和管理
-- ========================================

-- 4.1 添加新分区（每年年初执行）
-- ALTER TABLE t_attendance_record ADD PARTITION (
--     PARTITION p202601 VALUES LESS THAN (TO_DAYS('2026-02-01')) COMMENT = '2026年1月'
-- );

-- 4.2 删除旧分区（根据数据保留策略）
-- ALTER TABLE t_attendance_record DROP PARTITION p202301;

-- 4.3 重建分区（优化分区性能）
-- ALTER TABLE t_attendance_record REORGANIZE PARTITION;

-- ========================================
-- 5. 分区查询优化示例
-- ========================================

-- 5.1 查询特定月份数据（利用分区剪枝）
-- SELECT * FROM t_attendance_record
-- WHERE attendance_date >= '2024-01-01' AND attendance_date < '2024-02-01';

-- 5.2 跨月查询（可能涉及多个分区）
-- SELECT * FROM t_attendance_record
-- WHERE attendance_date >= '2024-01-01' AND attendance_date < '2024-04-01';

-- 5.3 统计查询（利用分区优势）
-- SELECT
--     DATE_FORMAT(attendance_date, '%Y-%m') as month,
--     COUNT(*) as record_count
-- FROM t_attendance_record
-- WHERE attendance_date >= '2024-01-01' AND attendance_date < '2025-01-01'
-- GROUP BY DATE_FORMAT(attendance_date, '%Y-%m')
-- ORDER BY month;

-- ========================================
-- 6. 分区性能监控
-- ========================================

-- 6.1 查看分区信息
-- SELECT
--     PARTITION_NAME,
--     PARTITION_METHOD,
--     PARTITION_EXPRESSION,
--     TABLE_ROWS
-- FROM information_schema.PARTITIONS
-- WHERE TABLE_NAME = 't_attendance_record'
-- AND TABLE_SCHEMA = DATABASE();

-- 6.2 查看分区使用情况
-- EXPLAIN PARTITIONS
-- SELECT * FROM t_attendance_record
-- WHERE attendance_date = '2024-01-15';

-- ========================================
-- 7. 注意事项和最佳实践
-- ========================================

-- 7.1 分区键选择
-- - 考勤日期是最佳分区键，因为大部分查询都基于时间范围
-- - 分区键必须包含在主键中

-- 7.2 分区数量
-- - 建议每个表不超过1024个分区
-- - 根据数据量和查询模式合理规划分区数量

-- 7.3 分区维护
-- - 定期添加新分区
-- - 根据数据保留策略删除旧分区
-- - 监控分区大小，避免单个分区过大

-- 7.4 性能考虑
-- - 分区剪枝可以显著提高查询性能
-- - 避免跨分区的更新操作
-- - 合理使用复合分区（如按月和按部门）

-- 7.5 备份和恢复
-- - 分区表备份与普通表相同
-- - 可以单独备份特定分区
-- - 恢复时注意分区结构一致性
