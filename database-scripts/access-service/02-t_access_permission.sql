-- ============================================================
-- 门禁权限表 - t_access_permission
-- 说明: 管理人员的门禁通行权限，支持临时权限、时间段控制、工作流审批
-- ============================================================

USE `ioedream_access_db`;

DROP TABLE IF EXISTS `t_access_permission`;

CREATE TABLE `t_access_permission` (
    -- 主键
    `permission_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限ID',
    
    -- 人员信息
    `person_id` BIGINT NOT NULL COMMENT '人员ID',
    `person_name` VARCHAR(100) NOT NULL COMMENT '人员姓名',
    `person_type` VARCHAR(50) NOT NULL COMMENT '人员类型：EMPLOYEE-员工, VISITOR-访客',
    
    -- 设备与区域
    `device_id` BIGINT COMMENT '设备ID',
    `area_id` BIGINT COMMENT '区域ID',
    
    -- 权限类型与时效
    `permission_type` VARCHAR(50) NOT NULL COMMENT '权限类型：PERMANENT-永久, TEMPORARY-临时',
    `start_time` DATETIME NOT NULL COMMENT '生效开始时间',
    `end_time` DATETIME NOT NULL COMMENT '生效结束时间',
    
    -- 每周通行权限配置
    `monday_access` TINYINT DEFAULT 1 COMMENT '周一通行权限：0-禁止, 1-允许',
    `tuesday_access` TINYINT DEFAULT 1 COMMENT '周二通行权限：0-禁止, 1-允许',
    `wednesday_access` TINYINT DEFAULT 1 COMMENT '周三通行权限：0-禁止, 1-允许',
    `thursday_access` TINYINT DEFAULT 1 COMMENT '周四通行权限：0-禁止, 1-允许',
    `friday_access` TINYINT DEFAULT 1 COMMENT '周五通行权限：0-禁止, 1-允许',
    `saturday_access` TINYINT DEFAULT 0 COMMENT '周六通行权限：0-禁止, 1-允许',
    `sunday_access` TINYINT DEFAULT 0 COMMENT '周日通行权限：0-禁止, 1-允许',
    
    -- 时间段配置（JSON格式）
    `time_config` JSON COMMENT '时间段配置：[{"start":"08:00", "end":"18:00"}]',
    
    -- 状态标记
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用, 1-启用',
    
    -- 工作流审批
    `workflow_instance_id` BIGINT COMMENT '工作流实例ID',
    `approve_user_id` BIGINT COMMENT '审批人ID',
    `approve_time` DATETIME COMMENT '审批时间',
    
    `remark` TEXT COMMENT '备注',
    
    -- 审计字段
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除, 1-已删除',
    
    -- 主键约束
    PRIMARY KEY (`permission_id`),
    
    -- 普通索引
    INDEX `idx_person_device` (`person_id`, `device_id`),
    INDEX `idx_time_range` (`start_time`, `end_time`),
    INDEX `idx_status` (`status`, `deleted_flag`),
    INDEX `idx_area_id` (`area_id`),
    INDEX `idx_workflow` (`workflow_instance_id`),
    INDEX `idx_create_time` (`create_time`)
    
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='门禁权限表';
