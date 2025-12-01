-- 创建人事员工表
CREATE TABLE IF NOT EXISTS `t_hr_employee` (
  `employee_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '员工ID',
  `employee_name` VARCHAR(64) NOT NULL COMMENT '姓名',
  `gender` TINYINT NULL COMMENT '性别 1男 2女',
  `email` VARCHAR(128) NULL,
  `phone` VARCHAR(32) NULL,
  `department_id` BIGINT NULL COMMENT '部门ID',
  `position` VARCHAR(64) NULL COMMENT '职位',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0禁用 1启用',
  `id_card` VARCHAR(32) NULL COMMENT '身份证号',
  `salary` DECIMAL(12,2) NULL COMMENT '薪资',
  `join_date` DATE NULL COMMENT '入职日期',
  `address` VARCHAR(256) NULL COMMENT '地址',
  `remark` VARCHAR(512) NULL COMMENT '备注',
  -- BaseEntity 审计字段
  `create_time` DATETIME NULL,
  `update_time` DATETIME NULL,
  `create_user_id` BIGINT NULL,
  `update_user_id` BIGINT NULL,
  `deleted_flag` TINYINT NOT NULL DEFAULT 0,
  `version` INT NOT NULL DEFAULT 0,
  PRIMARY KEY (`employee_id`),
  KEY `idx_department_id` (`department_id`),
  KEY `idx_employee_name` (`employee_name`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='员工表';


