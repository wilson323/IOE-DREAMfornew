-- 考勤相关表

CREATE TABLE IF NOT EXISTS `t_attendance_record` (
  `record_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `employee_id` BIGINT NOT NULL COMMENT '员工ID',
  `clock_time` DATETIME NOT NULL COMMENT '打卡时间',
  `clock_type` VARCHAR(16) NOT NULL COMMENT '类型 IN/OUT',
  `device_id` BIGINT NULL COMMENT '设备ID',
  `remark` VARCHAR(512) NULL COMMENT '备注',
  -- 审计
  `create_time` DATETIME NULL,
  `update_time` DATETIME NULL,
  `create_user_id` BIGINT NULL,
  `update_user_id` BIGINT NULL,
  `deleted_flag` TINYINT NOT NULL DEFAULT 0,
  `version` INT NOT NULL DEFAULT 0,
  PRIMARY KEY (`record_id`),
  KEY `idx_employee_time` (`employee_id`,`clock_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤打卡记录';


