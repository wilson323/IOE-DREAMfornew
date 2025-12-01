-- ========================================
-- 访客预登记系统数据库表结构
-- 版本: v1.0.0
-- 创建时间: 2025-11-25
-- 描述: 门禁系统访客预登记功能表结构
-- ========================================

-- 1. 访客预约表
CREATE TABLE `t_visitor_reservation` (
    `reservation_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '预约ID',
    `reservation_code` VARCHAR(32) NOT NULL COMMENT '预约编号',
    `visitor_name` VARCHAR(100) NOT NULL COMMENT '访客姓名',
    `visitor_phone` VARCHAR(20) NOT NULL COMMENT '访客手机号',
    `visitor_email` VARCHAR(100) DEFAULT NULL COMMENT '访客邮箱',
    `visitor_company` VARCHAR(200) DEFAULT NULL COMMENT '访客单位',
    `visitor_id_card` VARCHAR(20) DEFAULT NULL COMMENT '访客身份证号',
    `visitor_photo` VARCHAR(500) DEFAULT NULL COMMENT '访客照片URL',
    `visit_purpose` VARCHAR(500) NOT NULL COMMENT '来访事由',
    `visit_date` DATE NOT NULL COMMENT '来访日期',
    `visit_start_time` TIME NOT NULL COMMENT '来访开始时间',
    `visit_end_time` TIME NOT NULL COMMENT '来访结束时间',
    `visit_area_ids` TEXT COMMENT '访问区域ID列表(JSON格式)',
    `host_user_id` BIGINT NOT NULL COMMENT '接待人ID',
    `host_user_name` VARCHAR(100) NOT NULL COMMENT '接待人姓名',
    `host_department` VARCHAR(200) DEFAULT NULL COMMENT '接待人部门',
    `approval_status` TINYINT NOT NULL DEFAULT 0 COMMENT '审批状态:0-待审批,1-已通过,2-已拒绝',
    `approval_user_id` BIGINT DEFAULT NULL COMMENT '审批人ID',
    `approval_user_name` VARCHAR(100) DEFAULT NULL COMMENT '审批人姓名',
    `approval_time` DATETIME DEFAULT NULL COMMENT '审批时间',
    `approval_comment` TEXT COMMENT '审批意见',
    `qr_code` VARCHAR(500) DEFAULT NULL COMMENT '二维码内容',
    `qr_code_expire_time` DATETIME DEFAULT NULL COMMENT '二维码过期时间',
    `access_status` TINYINT NOT NULL DEFAULT 0 COMMENT '访问状态:0-未开始,1-进行中,2-已完成,3-已过期',
    `access_count` INT NOT NULL DEFAULT 0 COMMENT '访问次数',
    `max_access_count` INT NOT NULL DEFAULT 1 COMMENT '最大访问次数',
    `remarks` TEXT COMMENT '备注信息',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `update_user_id` BIGINT DEFAULT NULL COMMENT '更新人ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标识:0-正常,1-已删除',
    `version` INT NOT NULL DEFAULT 1 COMMENT '版本号',
    PRIMARY KEY (`reservation_id`),
    UNIQUE KEY `uk_reservation_code` (`reservation_code`),
    KEY `idx_visitor_phone` (`visitor_phone`),
    KEY `idx_host_user_id` (`host_user_id`),
    KEY `idx_visit_date` (`visit_date`),
    KEY `idx_approval_status` (`approval_status`),
    KEY `idx_access_status` (`access_status`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_deleted_flag` (`deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='访客预约表';

-- 2. 访客记录表
CREATE TABLE `t_visitor_record` (
    `record_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `reservation_id` BIGINT DEFAULT NULL COMMENT '预约ID',
    `visitor_name` VARCHAR(100) NOT NULL COMMENT '访客姓名',
    `visitor_phone` VARCHAR(20) NOT NULL COMMENT '访客手机号',
    `visit_area_id` BIGINT NOT NULL COMMENT '访问区域ID',
    `visit_area_name` VARCHAR(200) NOT NULL COMMENT '访问区域名称',
    `access_device_id` BIGINT DEFAULT NULL COMMENT '门禁设备ID',
    `access_device_name` VARCHAR(200) DEFAULT NULL COMMENT '门禁设备名称',
    `access_method` TINYINT NOT NULL COMMENT '通行方式:1-二维码,2-人脸,3-身份证,4-人工登记',
    `access_result` TINYINT NOT NULL COMMENT '通行结果:0-成功,1-失败',
    `fail_reason` VARCHAR(500) DEFAULT NULL COMMENT '失败原因',
    `access_time` DATETIME NOT NULL COMMENT '通行时间',
    `photo_path` VARCHAR(500) DEFAULT NULL COMMENT '现场照片路径',
    `temperature` DECIMAL(3,1) DEFAULT NULL COMMENT '体温测量值',
    `health_status` TINYINT DEFAULT NULL COMMENT '健康状态:0-正常,1-异常',
    `remarks` VARCHAR(500) DEFAULT NULL COMMENT '备注信息',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `update_user_id` BIGINT DEFAULT NULL COMMENT '更新人ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标识:0-正常,1-已删除',
    `version` INT NOT NULL DEFAULT 1 COMMENT '版本号',
    PRIMARY KEY (`record_id`),
    KEY `idx_reservation_id` (`reservation_id`),
    KEY `idx_visitor_phone` (`visitor_phone`),
    KEY `idx_visit_area_id` (`visit_area_id`),
    KEY `idx_access_device_id` (`access_device_id`),
    KEY `idx_access_time` (`access_time`),
    KEY `idx_access_result` (`access_result`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_deleted_flag` (`deleted_flag`),
    CONSTRAINT `fk_visitor_record_reservation` FOREIGN KEY (`reservation_id`) REFERENCES `t_visitor_reservation` (`reservation_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='访客记录表';

-- 3. 访客权限表
CREATE TABLE `t_visitor_permission` (
    `permission_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限ID',
    `reservation_id` BIGINT NOT NULL COMMENT '预约ID',
    `area_id` BIGINT NOT NULL COMMENT '区域ID',
    `area_name` VARCHAR(200) NOT NULL COMMENT '区域名称',
    `device_ids` TEXT COMMENT '可用设备ID列表(JSON格式)',
    `permission_start_time` DATETIME NOT NULL COMMENT '权限开始时间',
    `permission_end_time` DATETIME NOT NULL COMMENT '权限结束时间',
    `max_access_count` INT NOT NULL DEFAULT 1 COMMENT '最大访问次数',
    `used_access_count` INT NOT NULL DEFAULT 0 COMMENT '已使用访问次数',
    `time_slot_config` TEXT COMMENT '时间段配置(JSON格式)',
    `permission_status` TINYINT NOT NULL DEFAULT 1 COMMENT '权限状态:0-禁用,1-启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `update_user_id` BIGINT DEFAULT NULL COMMENT '更新人ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标识:0-正常,1-已删除',
    `version` INT NOT NULL DEFAULT 1 COMMENT '版本号',
    PRIMARY KEY (`permission_id`),
    UNIQUE KEY `uk_reservation_area` (`reservation_id`, `area_id`),
    KEY `idx_reservation_id` (`reservation_id`),
    KEY `idx_area_id` (`area_id`),
    KEY `idx_permission_time` (`permission_start_time`, `permission_end_time`),
    KEY `idx_permission_status` (`permission_status`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_deleted_flag` (`deleted_flag`),
    CONSTRAINT `fk_visitor_permission_reservation` FOREIGN KEY (`reservation_id`) REFERENCES `t_visitor_reservation` (`reservation_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='访客权限表';

-- 4. 访客审批流程表
CREATE TABLE `t_visitor_approval` (
    `approval_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '审批ID',
    `reservation_id` BIGINT NOT NULL COMMENT '预约ID',
    `step_order` INT NOT NULL COMMENT '审批步骤',
    `step_name` VARCHAR(100) NOT NULL COMMENT '步骤名称',
    `approver_id` BIGINT DEFAULT NULL COMMENT '审批人ID',
    `approver_name` VARCHAR(100) DEFAULT NULL COMMENT '审批人姓名',
    `approval_status` TINYINT NOT NULL DEFAULT 0 COMMENT '审批状态:0-待审批,1-已通过,2-已拒绝,3-已跳过',
    `approval_time` DATETIME DEFAULT NULL COMMENT '审批时间',
    `approval_comment` TEXT COMMENT '审批意见',
    `required` TINYINT NOT NULL DEFAULT 1 COMMENT '是否必须:0-否,1-是',
    `auto_approval` TINYINT NOT NULL DEFAULT 0 COMMENT '是否自动审批:0-否,1-是',
    `auto_approval_condition` TEXT COMMENT '自动审批条件',
    `timeout_minutes` INT DEFAULT NULL COMMENT '超时时间(分钟)',
    `timeout_action` TINYINT DEFAULT 0 COMMENT '超时处理:0-自动通过,1-自动拒绝',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `update_user_id` BIGINT DEFAULT NULL COMMENT '更新人ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标识:0-正常,1-已删除',
    `version` INT NOT NULL DEFAULT 1 COMMENT '版本号',
    PRIMARY KEY (`approval_id`),
    UNIQUE KEY `uk_reservation_step` (`reservation_id`, `step_order`),
    KEY `idx_reservation_id` (`reservation_id`),
    KEY `idx_approver_id` (`approver_id`),
    KEY `idx_approval_status` (`approval_status`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_deleted_flag` (`deleted_flag`),
    CONSTRAINT `fk_visitor_approval_reservation` FOREIGN KEY (`reservation_id`) REFERENCES `t_visitor_reservation` (`reservation_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='访客审批流程表';

-- 5. 访客黑名单表
CREATE TABLE `t_visitor_blacklist` (
    `blacklist_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '黑名单ID',
    `visitor_name` VARCHAR(100) NOT NULL COMMENT '访客姓名',
    `visitor_phone` VARCHAR(20) NOT NULL COMMENT '访客手机号',
    `visitor_id_card` VARCHAR(20) DEFAULT NULL COMMENT '访客身份证号',
    `visitor_photo` VARCHAR(500) DEFAULT NULL COMMENT '访客照片URL',
    `blacklist_type` TINYINT NOT NULL COMMENT '黑名单类型:1-安全风险,2-违规行为,3-其他原因',
    `blacklist_reason` TEXT NOT NULL COMMENT '列入黑名单原因',
    `start_time` DATETIME NOT NULL COMMENT '开始时间',
    `end_time` DATETIME DEFAULT NULL COMMENT '结束时间(为空表示永久)',
    `blacklist_level` TINYINT NOT NULL DEFAULT 1 COMMENT '风险等级:1-低,2-中,3-高',
    `remarks` TEXT COMMENT '备注信息',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `update_user_id` BIGINT DEFAULT NULL COMMENT '更新人ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标识:0-正常,1-已删除',
    `version` INT NOT NULL DEFAULT 1 COMMENT '版本号',
    PRIMARY KEY (`blacklist_id`),
    UNIQUE KEY `uk_visitor_phone` (`visitor_phone`),
    KEY `idx_visitor_id_card` (`visitor_id_card`),
    KEY `idx_blacklist_type` (`blacklist_type`),
    KEY `idx_blacklist_level` (`blacklist_level`),
    KEY `idx_start_time` (`start_time`),
    KEY `idx_end_time` (`end_time`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_deleted_flag` (`deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='访客黑名单表';

-- 初始化数据：插入审批流程模板
INSERT INTO `t_visitor_approval` (`reservation_id`, `step_order`, `step_name`, `approver_id`, `approver_name`, `approval_status`, `required`, `create_user_id`)
SELECT
    r.reservation_id,
    1 as step_order,
    '接待人审批' as step_name,
    r.host_user_id as approver_id,
    r.host_user_name as approver_name,
    0 as approval_status,
    1 as required,
    r.create_user_id
FROM `t_visitor_reservation` r
WHERE r.reservation_id NOT IN (SELECT DISTINCT reservation_id FROM `t_visitor_approval`);

-- 创建索引优化查询性能
CREATE INDEX idx_visitor_reservation_composite ON `t_visitor_reservation` (`visit_date`, `approval_status`, `access_status`);
CREATE INDEX idx_visitor_record_composite ON `t_visitor_record` (`access_time`, `access_result`, `visit_area_id`);
CREATE INDEX idx_visitor_permission_composite ON `t_visitor_permission` (`area_id`, `permission_status`, `permission_start_time`, `permission_end_time`);

-- 添加外键约束到区域表（假设区域表存在）
ALTER TABLE `t_visitor_permission` ADD CONSTRAINT `fk_visitor_permission_area`
FOREIGN KEY (`area_id`) REFERENCES `t_area` (`area_id`) ON DELETE CASCADE;

ALTER TABLE `t_visitor_record` ADD CONSTRAINT `fk_visitor_record_area`
FOREIGN KEY (`visit_area_id`) REFERENCES `t_area` (`area_id`) ON DELETE CASCADE;