-- ============================================
-- 数据字典表 - t_dict_type 和 t_dict_data
-- 版本: 1.0.0
-- 日期: 2025-12-08
-- 说明: 存储系统字典数据（类型和数据）
-- ============================================

-- 1. 创建字典类型表
CREATE TABLE IF NOT EXISTS `t_dict_type` (
    `type_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '字典类型ID',
    `type_code` VARCHAR(50) NOT NULL COMMENT '字典类型编码（唯一）',
    `type_name` VARCHAR(100) NOT NULL COMMENT '字典类型名称',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序号',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    PRIMARY KEY (`type_id`),
    UNIQUE KEY `uk_dict_type_code` (`type_code`),
    KEY `idx_dict_type_deleted` (`deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据字典类型表';

-- 2. 创建字典数据表
CREATE TABLE IF NOT EXISTS `t_dict_data` (
    `data_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '字典数据ID',
    `type_code` VARCHAR(50) NOT NULL COMMENT '字典类型编码',
    `data_code` VARCHAR(50) NOT NULL COMMENT '字典数据编码',
    `data_value` VARCHAR(255) NOT NULL COMMENT '字典数据值',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序号',
    `is_default` TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认：0-否 1-是',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    PRIMARY KEY (`data_id`),
    UNIQUE KEY `uk_dict_data_type_code` (`type_code`, `data_code`, `deleted_flag`),
    KEY `idx_dict_data_type` (`type_code`),
    KEY `idx_dict_data_status` (`status`),
    KEY `idx_dict_data_deleted` (`deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据字典数据表';

-- 3. 插入初始字典类型数据
INSERT INTO `t_dict_type` (`type_code`, `type_name`, `sort`, `remark`, `create_user_id`) VALUES
('gender', '性别', 1, '用户性别字典', 1),
('status', '状态', 2, '通用状态字典', 1),
('employee_status', '员工状态', 3, '员工在职状态', 1),
('department_type', '部门类型', 4, '部门分类', 1),
('notification_type', '通知类型', 5, '系统通知类型', 1),
('device_type', '设备类型', 6, '智能设备类型', 1),
('access_type', '门禁类型', 7, '门禁设备类型', 1),
('attendance_type', '考勤类型', 8, '考勤打卡类型', 1),
('consume_type', '消费类型', 9, '消费业务类型', 1),
('visitor_type', '访客类型', 10, '访客分类', 1)
ON DUPLICATE KEY UPDATE `type_name`=VALUES(`type_name`);

-- 4. 插入初始字典数据
INSERT INTO `t_dict_data` (`type_code`, `data_code`, `data_value`, `sort`, `is_default`, `status`, `create_user_id`) VALUES
-- 性别字典
('gender', 'MALE', '男', 1, 1, 1, 1),
('gender', 'FEMALE', '女', 2, 0, 1, 1),
('gender', 'UNKNOWN', '未知', 3, 0, 1, 1),

-- 状态字典
('status', 'ENABLED', '启用', 1, 1, 1, 1),
('status', 'DISABLED', '禁用', 2, 0, 1, 1),

-- 员工状态字典
('employee_status', 'ACTIVE', '在职', 1, 1, 1, 1),
('employee_status', 'RESIGN', '离职', 2, 0, 1, 1),
('employee_status', 'SUSPEND', '停职', 3, 0, 1, 1),

-- 部门类型字典
('department_type', 'COMPANY', '公司', 1, 0, 1, 1),
('department_type', 'DEPARTMENT', '部门', 2, 1, 1, 1),
('department_type', 'GROUP', '小组', 3, 0, 1, 1),

-- 通知类型字典
('notification_type', 'SYSTEM', '系统通知', 1, 0, 1, 1),
('notification_type', 'ALARM', '告警通知', 2, 0, 1, 1),
('notification_type', 'APPROVAL', '审批通知', 3, 0, 1, 1),
('notification_type', 'ANNOUNCEMENT', '公告通知', 4, 0, 1, 1),

-- 设备类型字典
('device_type', 'CAMERA', '摄像头', 1, 0, 1, 1),
('device_type', 'ACCESS', '门禁设备', 2, 0, 1, 1),
('device_type', 'ATTENDANCE', '考勤机', 3, 0, 1, 1),
('device_type', 'CONSUME', '消费机', 4, 0, 1, 1),

-- 门禁类型字典
('access_type', 'CARD', '刷卡门禁', 1, 1, 1, 1),
('access_type', 'FACE', '人脸识别', 2, 0, 1, 1),
('access_type', 'FINGERPRINT', '指纹识别', 3, 0, 1, 1),

-- 考勤类型字典
('attendance_type', 'CLOCK_IN', '上班打卡', 1, 0, 1, 1),
('attendance_type', 'CLOCK_OUT', '下班打卡', 2, 0, 1, 1),
('attendance_type', 'OVERTIME', '加班打卡', 3, 0, 1, 1),

-- 消费类型字典
('consume_type', 'DINING', '餐饮消费', 1, 1, 1, 1),
('consume_type', 'SHOPPING', '购物消费', 2, 0, 1, 1),
('consume_type', 'RECHARGE', '账户充值', 3, 0, 1, 1),

-- 访客类型字典
('visitor_type', 'BUSINESS', '商务访客', 1, 1, 1, 1),
('visitor_type', 'INTERVIEW', '面试访客', 2, 0, 1, 1),
('visitor_type', 'PERSONAL', '个人访客', 3, 0, 1, 1)
ON DUPLICATE KEY UPDATE `data_value`=VALUES(`data_value`);

-- 5. 验证数据插入
SELECT COUNT(*) AS type_count FROM t_dict_type WHERE deleted_flag = 0;
SELECT COUNT(*) AS data_count FROM t_dict_data WHERE deleted_flag = 0;
