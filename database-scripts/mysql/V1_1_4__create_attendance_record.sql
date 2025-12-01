-- 考勤记录表
CREATE TABLE `t_attendance_record` (
  `record_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `employee_id` bigint(20) NOT NULL COMMENT '员工ID',
  `attendance_date` date NOT NULL COMMENT '考勤日期',
  `punch_in_time` time DEFAULT NULL COMMENT '上班打卡时间',
  `punch_out_time` time DEFAULT NULL COMMENT '下班打卡时间',
  `punch_in_location` varchar(500) DEFAULT NULL COMMENT '上班打卡位置(JSON格式)',
  `punch_out_location` varchar(500) DEFAULT NULL COMMENT '下班打卡位置(JSON格式)',
  `punch_in_device_id` bigint(20) DEFAULT NULL COMMENT '上班打卡设备ID',
  `punch_out_device_id` bigint(20) DEFAULT NULL COMMENT '下班打卡设备ID',
  `punch_in_photo` varchar(255) DEFAULT NULL COMMENT '上班打卡照片',
  `punch_out_photo` varchar(255) DEFAULT NULL COMMENT '下班打卡照片',
  `work_hours` decimal(4,2) DEFAULT NULL COMMENT '工作时长(小时)',
  `overtime_hours` decimal(4,2) DEFAULT NULL COMMENT '加班时长(小时)',
  `attendance_status` varchar(20) DEFAULT 'NORMAL' COMMENT '考勤状态: NORMAL-正常, LATE-迟到, EARLY_LEAVE-早退, ABSENT-旷工, LEAVE-请假',
  `exception_type` varchar(50) DEFAULT NULL COMMENT '异常类型: LATE-迟到, EARLY_LEAVE-早退, ABSENTEEISM-旷工, FORGET_PUNCH-忘打卡',
  `exception_reason` varchar(500) DEFAULT NULL COMMENT '异常原因',
  `is_processed` tinyint(1) DEFAULT '0' COMMENT '是否已处理异常: 0-未处理, 1-已处理',
  `processed_by` bigint(20) DEFAULT NULL COMMENT '异常处理人ID',
  `processed_time` datetime DEFAULT NULL COMMENT '异常处理时间',
  `process_remark` varchar(500) DEFAULT NULL COMMENT '处理备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除标记 0-正常 1-删除',
  `create_user_id` bigint(20) DEFAULT NULL COMMENT '创建人ID',
  `update_user_id` bigint(20) DEFAULT NULL COMMENT '更新人ID',
  `version` int(11) NOT NULL DEFAULT '0' COMMENT '版本号（乐观锁）',
  PRIMARY KEY (`record_id`),
  UNIQUE KEY `uk_employee_date` (`employee_id`, `attendance_date`),
  KEY `idx_employee_id` (`employee_id`),
  KEY `idx_attendance_date` (`attendance_date`),
  KEY `idx_attendance_status` (`attendance_status`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_company_id` (`create_user_id`),
  CONSTRAINT `fk_attendance_record_employee` FOREIGN KEY (`employee_id`) REFERENCES `t_hr_employee` (`employee_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤记录表';

-- 添加外键索引
ALTER TABLE `t_attendance_record` ADD INDEX `idx_punch_in_device` (`punch_in_device_id`);
ALTER TABLE `t_attendance_record` ADD INDEX `idx_punch_out_device` (`punch_out_device_id`);
ALTER TABLE `t_attendance_record` ADD INDEX `idx_processed_by` (`processed_by`);