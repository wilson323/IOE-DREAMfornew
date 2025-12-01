-- 完善考勤模块表结构
-- 基于design.md的完整考勤管理表设计

-- 1. 考勤规则表
CREATE TABLE IF NOT EXISTS `t_attendance_rule` (
    `rule_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '规则ID',
    `rule_name` VARCHAR(100) NOT NULL COMMENT '规则名称',
    `rule_code` VARCHAR(50) NOT NULL COMMENT '规则编码',
    `company_id` BIGINT NOT NULL COMMENT '公司ID',
    `department_id` BIGINT NULL COMMENT '部门ID',
    `employee_type` VARCHAR(50) DEFAULT NULL COMMENT '适用员工类型',
    `work_schedule` JSON NOT NULL COMMENT '工作排班配置',
    `late_tolerance` INT(11) DEFAULT 0 COMMENT '迟到容忍分钟数',
    `early_tolerance` INT(11) DEFAULT 0 COMMENT '早退容忍分钟数',
    `overtime_rules` JSON DEFAULT NULL COMMENT '加班规则配置',
    `holiday_rules` JSON DEFAULT NULL COMMENT '节假日规则配置',
    `gps_validation` TINYINT(1) DEFAULT 0 COMMENT '是否启用GPS验证',
    `gps_range` INT(11) DEFAULT 100 COMMENT 'GPS验证范围(米)',
    `photo_required` TINYINT(1) DEFAULT 0 COMMENT '是否需要拍照打卡',
    `status` VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '规则状态',
    `effective_date` DATE NOT NULL COMMENT '生效日期',
    `expiry_date` DATE DEFAULT NULL COMMENT '失效日期',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记 0-正常 1-删除',
    `create_user_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `update_user_id` BIGINT DEFAULT NULL COMMENT '更新人ID',
    `version` INT NOT NULL DEFAULT 0 COMMENT '版本号（乐观锁）',
    PRIMARY KEY (`rule_id`),
    UNIQUE KEY `uk_rule_code` (`rule_code`),
    KEY `idx_company_id` (`company_id`),
    KEY `idx_department_id` (`department_id`),
    KEY `idx_status` (`status`),
    KEY `idx_effective_date` (`effective_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤规则表';

-- 2. 排班管理表
CREATE TABLE IF NOT EXISTS `t_attendance_schedule` (
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
    PRIMARY KEY (`schedule_id`),
    UNIQUE KEY `uk_employee_date` (`employee_id`, `schedule_date`),
    KEY `idx_schedule_date` (`schedule_date`),
    KEY `idx_shift_id` (`shift_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='排班管理表';

-- 3. 考勤异常表
CREATE TABLE IF NOT EXISTS `t_attendance_exception` (
    `exception_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '异常ID',
    `employee_id` BIGINT NOT NULL COMMENT '员工ID',
    `attendance_date` DATE NOT NULL COMMENT '考勤日期',
    `exception_type` VARCHAR(50) NOT NULL COMMENT '异常类型',
    `exception_reason` VARCHAR(500) NOT NULL COMMENT '异常原因',
    `exception_level` VARCHAR(20) DEFAULT 'WARNING' COMMENT '异常级别',
    `auto_detected` TINYINT(1) DEFAULT 1 COMMENT '是否自动检测',
    `is_processed` TINYINT(1) DEFAULT 0 COMMENT '是否已处理异常',
    `process_type` VARCHAR(50) DEFAULT NULL COMMENT '处理类型',
    `process_reason` VARCHAR(500) DEFAULT NULL COMMENT '处理原因',
    `processed_by` BIGINT DEFAULT NULL COMMENT '异常处理人',
    `processed_time` DATETIME DEFAULT NULL COMMENT '异常处理时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记 0-正常 1-删除',
    `create_user_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `update_user_id` BIGINT DEFAULT NULL COMMENT '更新人ID',
    `version` INT NOT NULL DEFAULT 0 COMMENT '版本号（乐观锁）',
    PRIMARY KEY (`exception_id`),
    KEY `idx_employee_date` (`employee_id`, `attendance_date`),
    KEY `idx_exception_type` (`exception_type`),
    KEY `idx_is_processed` (`is_processed`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤异常表';

-- 4. 考勤统计表
CREATE TABLE IF NOT EXISTS `t_attendance_statistics` (
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
    PRIMARY KEY (`statistics_id`),
    UNIQUE KEY `uk_employee_period` (`employee_id`, `statistics_type`, `period_value`),
    KEY `idx_statistics_date` (`statistics_date`),
    KEY `idx_statistics_type` (`statistics_type`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤统计表';

-- 5. 增强版考勤记录表（修改现有表结构）
ALTER TABLE `t_attendance_record`
ADD COLUMN `attendance_date` DATE NOT NULL COMMENT '考勤日期' AFTER `employee_id`,
ADD COLUMN `punch_in_time` TIME DEFAULT NULL COMMENT '上班打卡时间' AFTER `clock_time`,
ADD COLUMN `punch_out_time` TIME DEFAULT NULL COMMENT '下班打卡时间' AFTER `punch_in_time`,
ADD COLUMN `punch_in_location` VARCHAR(500) DEFAULT NULL COMMENT '上班打卡位置' AFTER `device_id`,
ADD COLUMN `punch_out_location` VARCHAR(500) DEFAULT NULL COMMENT '下班打卡位置' AFTER `punch_in_location`,
ADD COLUMN `punch_in_device_id` BIGINT DEFAULT NULL COMMENT '上班打卡设备ID' AFTER `punch_out_location`,
ADD COLUMN `punch_out_device_id` BIGINT DEFAULT NULL COMMENT '下班打卡设备ID' AFTER `punch_in_device_id`,
ADD COLUMN `work_hours` DECIMAL(4,2) DEFAULT NULL COMMENT '工作时长(小时)' AFTER `punch_out_device_id`,
ADD COLUMN `overtime_hours` DECIMAL(4,2) DEFAULT NULL COMMENT '加班时长(小时)' AFTER `work_hours`,
ADD COLUMN `attendance_status` VARCHAR(20) DEFAULT 'NORMAL' COMMENT '考勤状态' AFTER `overtime_hours`,
ADD COLUMN `exception_type` VARCHAR(50) DEFAULT NULL COMMENT '异常类型' AFTER `attendance_status`,
ADD COLUMN `exception_reason` VARCHAR(500) DEFAULT NULL COMMENT '异常原因' AFTER `exception_type`,
ADD COLUMN `is_processed` TINYINT(1) DEFAULT '0' COMMENT '是否已处理异常' AFTER `exception_reason`,
ADD COLUMN `processed_by` BIGINT DEFAULT NULL COMMENT '异常处理人' AFTER `is_processed`,
ADD COLUMN `processed_time` DATETIME DEFAULT NULL COMMENT '异常处理时间' AFTER `processed_by`,
MODIFY COLUMN `clock_type` VARCHAR(16) NULL COMMENT '打卡类型(IN/OUT)',
ADD COLUMN `record_type` VARCHAR(20) NOT NULL DEFAULT 'PUNCH' COMMENT '记录类型',
DROP INDEX `idx_employee_time`,
ADD INDEX `idx_employee_date` (`employee_id`, `attendance_date`),
ADD INDEX `idx_attendance_date` (`attendance_date`),
ADD INDEX `idx_status` (`attendance_status`),
ADD INDEX `idx_create_time` (`create_time`);

-- 6. 插入默认考勤规则数据
INSERT INTO `t_attendance_rule` (`rule_name`, `rule_code`, `company_id`, `work_schedule`, `late_tolerance`, `early_tolerance`, `gps_validation`, `gps_range`, `photo_required`, `status`, `effective_date`, `create_time`, `update_time`, `create_user_id`, `update_user_id`)
VALUES
('标准考勤规则', 'STANDARD_RULE', 1, '{"workDays": ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"], "workTime": "09:00-18:00", "breakTime": "12:00-13:00"}', 10, 10, 0, 100, 0, 'ACTIVE', CURDATE(), NOW(), NOW(), 1, 1),
('弹性考勤规则', 'FLEXIBLE_RULE', 1, '{"workDays": ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"], "workTime": "08:30-17:30", "breakTime": "12:00-13:00", "flexibleTime": 30}', 15, 15, 1, 200, 1, 'ACTIVE', CURDATE(), NOW(), NOW(), 1, 1);

-- 7. 创建外键约束（如果需要）
-- ALTER TABLE `t_attendance_record` ADD CONSTRAINT `fk_attendance_record_employee` FOREIGN KEY (`employee_id`) REFERENCES `t_employee`(`employee_id`);
-- ALTER TABLE `t_attendance_exception` ADD CONSTRAINT `fk_attendance_exception_employee` FOREIGN KEY (`employee_id`) REFERENCES `t_employee`(`employee_id`);
-- ALTER TABLE `t_attendance_statistics` ADD CONSTRAINT `fk_attendance_statistics_employee` FOREIGN KEY (`employee_id`) REFERENCES `t_employee`(`employee_id`);