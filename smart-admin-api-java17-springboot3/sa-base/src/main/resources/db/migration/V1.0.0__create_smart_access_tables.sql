-- =====================================================
-- SmartAdmin v3 门禁系统模块数据库表结构
-- 创建时间: 2025-01-13
-- 版本: v1.0.0
-- 说明: 创建门禁系统相关的7个核心数据表
-- =====================================================

-- 1. 门禁设备表
CREATE TABLE `smart_access_device` (
    `device_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '设备ID',
    `device_code` VARCHAR(100) NOT NULL COMMENT '设备编码',
    `device_name` VARCHAR(200) NOT NULL COMMENT '设备名称',
    `device_type` VARCHAR(50) NOT NULL COMMENT '设备类型：door-门禁，gate-闸机，barrier-道闸，turnstile-转闸',
    `device_brand` VARCHAR(100) COMMENT '设备品牌',
    `device_model` VARCHAR(100) COMMENT '设备型号',
    `area_id` BIGINT COMMENT '所属区域ID',
    `area_name` VARCHAR(100) COMMENT '所属区域名称',
    `location_desc` VARCHAR(500) COMMENT '位置描述',
    `ip_address` VARCHAR(50) COMMENT 'IP地址',
    `port` INT COMMENT '端口号',
    `mac_address` VARCHAR(50) COMMENT 'MAC地址',
    `protocol_type` VARCHAR(50) COMMENT '通信协议：tcp, udp, http, websocket',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-离线，1-在线，2-故障',
    `is_enabled` TINYINT DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
    `config_json` JSON COMMENT '设备配置信息',
    `last_online_time` DATETIME COMMENT '最后在线时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT NOT NULL COMMENT '创建人ID',
    `update_by` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT DEFAULT 0 COMMENT '删除标记：0-正常，1-删除',
    `version` INT DEFAULT 1 COMMENT '版本号',

    -- 索引
    INDEX `idx_device_code` (`device_code`),
    INDEX `idx_device_type` (`device_type`),
    INDEX `idx_area_id` (`area_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_ip_address` (`ip_address`),
    INDEX `idx_create_time` (`create_time`),
    INDEX `idx_deleted_flag` (`deleted_flag`),
    UNIQUE KEY `uk_device_code` (`device_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门禁设备表';

-- 2. 门禁权限表
CREATE TABLE `smart_access_permission` (
    `permission_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID',
    `person_id` BIGINT NOT NULL COMMENT '人员ID',
    `person_name` VARCHAR(100) NOT NULL COMMENT '人员姓名',
    `person_type` VARCHAR(50) NOT NULL COMMENT '人员类型：employee-员工，visitor-访客，contractor-承包商',
    `device_id` BIGINT NOT NULL COMMENT '设备ID',
    `device_name` VARCHAR(200) NOT NULL COMMENT '设备名称',
    `area_id` BIGINT COMMENT '区域ID',
    `area_name` VARCHAR(100) COMMENT '区域名称',
    `permission_type` VARCHAR(50) NOT NULL COMMENT '权限类型：permanent-永久，temporary-临时，scheduled-定时',
    `start_time` DATETIME NOT NULL COMMENT '生效开始时间',
    `end_time` DATETIME NOT NULL COMMENT '生效结束时间',
    `monday_access` TINYINT DEFAULT 1 COMMENT '周一通行权限：0-禁止，1-允许',
    `tuesday_access` TINYINT DEFAULT 1 COMMENT '周二通行权限：0-禁止，1-允许',
    `wednesday_access` TINYINT DEFAULT 1 COMMENT '周三通行权限：0-禁止，1-允许',
    `thursday_access` TINYINT DEFAULT 1 COMMENT '周四通行权限：0-禁止，1-允许',
    `friday_access` TINYINT DEFAULT 1 COMMENT '周五通行权限：0-禁止，1-允许',
    `saturday_access` TINYINT DEFAULT 0 COMMENT '周六通行权限：0-禁止，1-允许',
    `sunday_access` TINYINT DEFAULT 0 COMMENT '周日通行权限：0-禁止，1-允许',
    `time_config` JSON COMMENT '时间段配置',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用，2-待审批，3-已过期',
    `business_code` VARCHAR(100) COMMENT '业务编码',
    `approve_user_id` BIGINT COMMENT '审批人ID',
    `approve_time` DATETIME COMMENT '审批时间',
    `approve_remark` TEXT COMMENT '审批备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT NOT NULL COMMENT '创建人ID',
    `update_by` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT DEFAULT 0 COMMENT '删除标记：0-正常，1-删除',
    `version` INT DEFAULT 1 COMMENT '版本号',

    -- 索引
    INDEX `idx_person_id` (`person_id`),
    INDEX `idx_device_id` (`device_id`),
    INDEX `idx_area_id` (`area_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_business_code` (`business_code`),
    INDEX `idx_start_end_time` (`start_time`, `end_time`),
    INDEX `idx_person_device` (`person_id`, `device_id`),
    INDEX `idx_create_time` (`create_time`),
    INDEX `idx_deleted_flag` (`deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门禁权限表';

-- 3. 通行记录表
CREATE TABLE `smart_access_record` (
    `record_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    `device_id` BIGINT NOT NULL COMMENT '设备ID',
    `device_name` VARCHAR(200) NOT NULL COMMENT '设备名称',
    `device_code` VARCHAR(100) NOT NULL COMMENT '设备编码',
    `person_id` BIGINT NOT NULL COMMENT '人员ID',
    `person_name` VARCHAR(100) NOT NULL COMMENT '人员姓名',
    `person_type` VARCHAR(50) NOT NULL COMMENT '人员类型',
    `card_number` VARCHAR(100) COMMENT '卡号',
    `face_image_url` VARCHAR(500) COMMENT '人脸图片URL',
    `fingerprint_template` VARCHAR(500) COMMENT '指纹模板',
    `access_result` TINYINT NOT NULL COMMENT '通行结果：0-失败，1-成功',
    `fail_reason` VARCHAR(200) COMMENT '失败原因',
    `direction` VARCHAR(20) COMMENT '通行方向：in-进入，out-外出',
    `access_time` DATETIME NOT NULL COMMENT '通行时间',
    `temperature` DECIMAL(4,1) COMMENT '体温（摄氏度）',
    `mask_detected` TINYINT COMMENT '是否检测到口罩：0-未检测，1-检测到',
    `image_urls` JSON COMMENT '通行图片URLs',
    `event_type` VARCHAR(50) COMMENT '事件类型：normal-正常，abnormal-异常，forced-强制',
    `is_abnormal` TINYINT DEFAULT 0 COMMENT '是否异常：0-正常，1-异常',
    `processed` TINYINT DEFAULT 0 COMMENT '是否已处理：0-未处理，1-已处理',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    -- 索引
    INDEX `idx_device_id` (`device_id`),
    INDEX `idx_person_id` (`person_id`),
    INDEX `idx_access_time` (`access_time`),
    INDEX `idx_access_result` (`access_result`),
    INDEX `idx_person_device_time` (`person_id`, `device_id`, `access_time`),
    INDEX `idx_is_abnormal` (`is_abnormal`),
    INDEX `idx_event_type` (`event_type`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通行记录表';

-- 4. 设备指令表
CREATE TABLE `smart_access_command` (
    `command_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '指令ID',
    `device_id` BIGINT NOT NULL COMMENT '设备ID',
    `command_type` VARCHAR(50) NOT NULL COMMENT '指令类型：open-开门，close-关门，restart-重启，sync-同步',
    `command_content` JSON COMMENT '指令内容',
    `command_status` TINYINT NOT NULL DEFAULT 0 COMMENT '指令状态：0-待发送，1-已发送，2-执行成功，3-执行失败',
    `send_time` DATETIME COMMENT '发送时间',
    `execute_time` DATETIME COMMENT '执行时间',
    `response_content` JSON COMMENT '设备响应内容',
    `error_message` TEXT COMMENT '错误信息',
    `retry_count` INT DEFAULT 0 COMMENT '重试次数',
    `max_retry` INT DEFAULT 3 COMMENT '最大重试次数',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` BIGINT NOT NULL COMMENT '创建人ID',

    -- 索引
    INDEX `idx_device_id` (`device_id`),
    INDEX `idx_command_type` (`command_type`),
    INDEX `idx_command_status` (`command_status`),
    INDEX `idx_send_time` (`send_time`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备指令表';

-- 5. 设备状态表
CREATE TABLE `smart_access_device_status` (
    `status_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '状态ID',
    `device_id` BIGINT NOT NULL COMMENT '设备ID',
    `door_status` VARCHAR(20) COMMENT '门状态：open-打开，closed-关闭，unknown-未知',
    `lock_status` VARCHAR(20) COMMENT '锁状态：locked-锁定，unlocked-解锁，unknown-未知',
    `power_status` VARCHAR(20) COMMENT '电源状态：normal-正常，low-低电量，offline-离线',
    `network_status` VARCHAR(20) COMMENT '网络状态：online-在线，offline-离线',
    `last_heartbeat` DATETIME COMMENT '最后心跳时间',
    `cpu_usage` DECIMAL(5,2) COMMENT 'CPU使用率',
    `memory_usage` DECIMAL(5,2) COMMENT '内存使用率',
    `storage_usage` DECIMAL(5,2) COMMENT '存储使用率',
    `temperature` DECIMAL(4,1) COMMENT '设备温度',
    `error_count` INT DEFAULT 0 COMMENT '错误次数',
    `last_error_time` DATETIME COMMENT '最后错误时间',
    `last_error_message` TEXT COMMENT '最后错误信息',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    -- 索引
    INDEX `idx_device_id` (`device_id`),
    INDEX `idx_last_heartbeat` (`last_heartbeat`),
    INDEX `idx_create_time` (`create_time`),
    UNIQUE KEY `uk_device_id` (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备状态表';

-- 6. 区域权限组表
CREATE TABLE `smart_access_area_group` (
    `group_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限组ID',
    `group_name` VARCHAR(200) NOT NULL COMMENT '权限组名称',
    `group_code` VARCHAR(100) NOT NULL COMMENT '权限组编码',
    `description` TEXT COMMENT '描述',
    `area_ids` TEXT COMMENT '区域ID列表',
    `device_ids` TEXT COMMENT '设备ID列表',
    `default_start_time` TIME COMMENT '默认开始时间',
    `default_end_time` TIME COMMENT '默认结束时间',
    `default_weekdays` VARCHAR(20) DEFAULT '1,2,3,4,5' COMMENT '默认通行日期',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` BIGINT NOT NULL COMMENT '创建人ID',
    `update_by` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT DEFAULT 0 COMMENT '删除标记：0-正常，1-删除',
    `version` INT DEFAULT 1 COMMENT '版本号',

    -- 索引
    INDEX `idx_group_code` (`group_code`),
    INDEX `idx_status` (`status`),
    INDEX `idx_create_time` (`create_time`),
    INDEX `idx_deleted_flag` (`deleted_flag`),
    UNIQUE KEY `uk_group_code` (`group_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='区域权限组表';

-- 7. 设备告警表
CREATE TABLE `smart_access_alarm` (
    `alarm_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '告警ID',
    `device_id` BIGINT NOT NULL COMMENT '设备ID',
    `alarm_type` VARCHAR(50) NOT NULL COMMENT '告警类型：offline-离线，forced-强制开门，door_open-门超时未关，low_power-低电量，error-设备故障',
    `alarm_level` VARCHAR(20) NOT NULL COMMENT '告警级别：low-低，medium-中，high-高，critical-严重',
    `alarm_title` VARCHAR(200) NOT NULL COMMENT '告警标题',
    `alarm_content` TEXT COMMENT '告警内容',
    `alarm_time` DATETIME NOT NULL COMMENT '告警时间',
    `record_id` BIGINT COMMENT '关联通行记录ID',
    `person_id` BIGINT COMMENT '关联人员ID',
    `person_name` VARCHAR(100) COMMENT '关联人员姓名',
    `is_handled` TINYINT DEFAULT 0 COMMENT '是否已处理：0-未处理，1-已处理',
    `handled_by` BIGINT COMMENT '处理人ID',
    `handled_time` DATETIME COMMENT '处理时间',
    `handle_remark` TEXT COMMENT '处理备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    -- 索引
    INDEX `idx_device_id` (`device_id`),
    INDEX `idx_alarm_type` (`alarm_type`),
    INDEX `idx_alarm_level` (`alarm_level`),
    INDEX `idx_alarm_time` (`alarm_time`),
    INDEX `idx_is_handled` (`is_handled`),
    INDEX `idx_person_id` (`person_id`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备告警表';

-- =====================================================
-- 添加外键约束（可选，根据实际需要）
-- =====================================================

-- 设备权限表外键
-- ALTER TABLE `smart_access_permission` ADD CONSTRAINT `fk_permission_device`
-- FOREIGN KEY (`device_id`) REFERENCES `smart_access_device` (`device_id`);

-- 通行记录表外键
-- ALTER TABLE `smart_access_record` ADD CONSTRAINT `fk_record_device`
-- FOREIGN KEY (`device_id`) REFERENCES `smart_access_device` (`device_id`);

-- 设备指令表外键
-- ALTER TABLE `smart_access_command` ADD CONSTRAINT `fk_command_device`
-- FOREIGN KEY (`device_id`) REFERENCES `smart_access_device` (`device_id`);

-- 设备状态表外键
-- ALTER TABLE `smart_access_device_status` ADD CONSTRAINT `fk_status_device`
-- FOREIGN KEY (`device_id`) REFERENCES `smart_access_device` (`device_id`);

-- 设备告警表外键
-- ALTER TABLE `smart_access_alarm` ADD CONSTRAINT `fk_alarm_device`
-- FOREIGN KEY (`device_id`) REFERENCES `smart_access_device` (`device_id`);

-- =====================================================
-- 创建视图（可选，用于常用查询）
-- =====================================================

-- 设备状态汇总视图
CREATE OR REPLACE VIEW `v_access_device_summary` AS
SELECT
    d.device_id,
    d.device_code,
    d.device_name,
    d.device_type,
    d.area_name,
    d.ip_address,
    d.status,
    ds.door_status,
    ds.lock_status,
    ds.last_heartbeat,
    ds.cpu_usage,
    ds.memory_usage,
    CASE
        WHEN d.status = 1 AND ds.last_heartbeat > DATE_SUB(NOW(), INTERVAL 5 MINUTE) THEN '在线'
        WHEN d.status = 2 THEN '故障'
        ELSE '离线'
    END AS connection_status
FROM `smart_access_device` d
LEFT JOIN `smart_access_device_status` ds ON d.device_id = ds.device_id
WHERE d.deleted_flag = 0;

-- 通行统计视图
CREATE OR REPLACE VIEW `v_access_statistics` AS
SELECT
    DATE(access_time) as access_date,
    device_id,
    device_name,
    access_result,
    COUNT(*) as access_count,
    COUNT(CASE WHEN is_abnormal = 1 THEN 1 END) as abnormal_count
FROM `smart_access_record`
WHERE access_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY DATE(access_time), device_id, device_name, access_result;

-- 权限汇总视图
CREATE OR REPLACE VIEW `v_permission_summary` AS
SELECT
    p.permission_id,
    p.person_id,
    p.person_name,
    p.person_type,
    p.device_id,
    p.device_name,
    p.area_name,
    p.permission_type,
    p.start_time,
    p.end_time,
    p.status,
    d.device_code,
    d.device_type,
    CASE
        WHEN p.status = 1 AND p.start_time <= NOW() AND p.end_time >= NOW() THEN '有效'
        WHEN p.status = 2 THEN '待审批'
        WHEN p.status = 3 THEN '已过期'
        WHEN p.status = 0 THEN '已禁用'
        ELSE '未知'
    END AS effective_status
FROM `smart_access_permission` p
LEFT JOIN `smart_access_device` d ON p.device_id = d.device_id
WHERE p.deleted_flag = 0;