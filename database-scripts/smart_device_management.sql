-- =============================================
-- SmartAdmin 统一设备管理模块数据库表结构
-- 创建时间: 2025-11-13
-- 版本: v1.0.0
-- 描述: 支持摄像头、门禁、消费终端、考勤机等设备的统一管理
-- =============================================

-- 1. 设备基础信息表
CREATE TABLE `t_smart_device` (
    `device_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '设备ID',
    `device_code` VARCHAR(64) NOT NULL COMMENT '设备编码，唯一标识',
    `device_name` VARCHAR(128) NOT NULL COMMENT '设备名称',
    `device_type` TINYINT NOT NULL COMMENT '设备类型：1-摄像头，2-门禁，3-消费终端，4-考勤机，5-其他',
    `device_category` VARCHAR(32) NOT NULL COMMENT '设备分类，如：网络摄像头、人脸识别门禁等',
    `brand` VARCHAR(64) COMMENT '设备品牌',
    `model` VARCHAR(64) COMMENT '设备型号',
    `serial_number` VARCHAR(128) COMMENT '设备序列号',
    `protocol_type` VARCHAR(32) NOT NULL COMMENT '通信协议类型：TCP, UDP, HTTP, HTTPS, WebSocket, MQTT等',
    `ip_address` VARCHAR(45) COMMENT '设备IP地址（支持IPv6）',
    `port` INT COMMENT '设备端口号',
    `mac_address` VARCHAR(17) COMMENT '设备MAC地址',
    `location_id` BIGINT COMMENT '所属位置ID',
    `area_id` BIGINT COMMENT '所属区域ID',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '设备状态：0-离线，1-在线，2-故障，3-维护中',
    `enabled_flag` TINYINT NOT NULL DEFAULT 1 COMMENT '启用状态：0-禁用，1-启用',
    `description` TEXT COMMENT '设备描述',
    `configuration` JSON COMMENT '设备配置信息（JSON格式）',
    `extended_properties` JSON COMMENT '设备扩展属性',
    `manufacturer_info` JSON COMMENT '设备厂商信息',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT NOT NULL COMMENT '创建人ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
    PRIMARY KEY (`device_id`),
    UNIQUE KEY `uk_device_code` (`device_code`),
    KEY `idx_device_type` (`device_type`),
    KEY `idx_device_status` (`status`),
    KEY `idx_location_area` (`location_id`, `area_id`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_deleted_flag` (`deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备基础信息表';

-- 2. 设备分组管理表
CREATE TABLE `t_smart_device_group` (
    `group_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分组ID',
    `group_code` VARCHAR(64) NOT NULL COMMENT '分组编码',
    `group_name` VARCHAR(128) NOT NULL COMMENT '分组名称',
    `parent_group_id` BIGINT DEFAULT 0 COMMENT '父分组ID，0表示根分组',
    `group_type` TINYINT NOT NULL COMMENT '分组类型：1-按功能分组，2-按位置分组，3-按品牌分组，4-自定义分组',
    `group_level` TINYINT NOT NULL DEFAULT 1 COMMENT '分组层级',
    `group_path` VARCHAR(500) COMMENT '分组路径，如：/根分组/子分组',
    `sort_order` INT DEFAULT 0 COMMENT '排序序号',
    `description` TEXT COMMENT '分组描述',
    `device_count` INT DEFAULT 0 COMMENT '设备数量',
    `enabled_flag` TINYINT NOT NULL DEFAULT 1 COMMENT '启用状态：0-禁用，1-启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT NOT NULL COMMENT '创建人ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
    PRIMARY KEY (`group_id`),
    UNIQUE KEY `uk_group_code` (`group_code`),
    KEY `idx_parent_group` (`parent_group_id`),
    KEY `idx_group_type` (`group_type`),
    KEY `idx_sort_order` (`sort_order`),
    KEY `idx_deleted_flag` (`deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备分组管理表';

-- 3. 设备分组关联表
CREATE TABLE `t_smart_device_group_relation` (
    `relation_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID',
    `device_id` BIGINT NOT NULL COMMENT '设备ID',
    `group_id` BIGINT NOT NULL COMMENT '分组ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_user_id` BIGINT NOT NULL COMMENT '创建人ID',
    PRIMARY KEY (`relation_id`),
    UNIQUE KEY `uk_device_group` (`device_id`, `group_id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_group_id` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备分组关联表';

-- 4. 设备协议适配器表
CREATE TABLE `t_smart_device_protocol_adapter` (
    `adapter_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '适配器ID',
    `adapter_name` VARCHAR(128) NOT NULL COMMENT '适配器名称',
    `protocol_type` VARCHAR(32) NOT NULL COMMENT '协议类型',
    `device_type` TINYINT COMMENT '适用设备类型',
    `adapter_class` VARCHAR(255) NOT NULL COMMENT '适配器实现类全路径',
    `config_schema` JSON COMMENT '配置参数模式（JSON Schema）',
    `supported_commands` JSON COMMENT '支持的命令列表',
    `version` VARCHAR(32) NOT NULL DEFAULT '1.0.0' COMMENT '适配器版本',
    `enabled_flag` TINYINT NOT NULL DEFAULT 1 COMMENT '启用状态：0-禁用，1-启用',
    `description` TEXT COMMENT '适配器描述',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT NOT NULL COMMENT '创建人ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
    PRIMARY KEY (`adapter_id`),
    UNIQUE KEY `uk_adapter_name` (`adapter_name`),
    KEY `idx_protocol_type` (`protocol_type`),
    KEY `idx_device_type` (`device_type`),
    KEY `idx_enabled_flag` (`enabled_flag`),
    KEY `idx_deleted_flag` (`deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备协议适配器表';

-- 5. 设备状态监控表
CREATE TABLE `t_smart_device_status_log` (
    `log_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `device_id` BIGINT NOT NULL COMMENT '设备ID',
    `old_status` TINYINT COMMENT '原状态',
    `new_status` TINYINT NOT NULL COMMENT '新状态',
    `status_reason` VARCHAR(255) COMMENT '状态变更原因',
    `cpu_usage` DECIMAL(5,2) COMMENT 'CPU使用率(%)',
    `memory_usage` DECIMAL(5,2) COMMENT '内存使用率(%)',
    `disk_usage` DECIMAL(5,2) COMMENT '磁盘使用率(%)',
    `network_status` TINYINT COMMENT '网络状态：0-断开，1-正常，2-异常',
    `last_heartbeat_time` DATETIME COMMENT '最后心跳时间',
    `response_time` INT COMMENT '响应时间(毫秒)',
    `error_count` INT DEFAULT 0 COMMENT '错误次数',
    `extended_metrics` JSON COMMENT '扩展监控指标',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`log_id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_status_change` (`old_status`, `new_status`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_last_heartbeat` (`last_heartbeat_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备状态监控日志表';

-- 6. 设备命令执行表
CREATE TABLE `t_smart_device_command` (
    `command_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '命令ID',
    `device_id` BIGINT NOT NULL COMMENT '设备ID',
    `command_type` VARCHAR(64) NOT NULL COMMENT '命令类型',
    `command_name` VARCHAR(128) NOT NULL COMMENT '命令名称',
    `command_parameters` JSON COMMENT '命令参数（JSON格式）',
    `command_status` TINYINT NOT NULL DEFAULT 0 COMMENT '命令状态：0-待执行，1-执行中，2-执行成功，3-执行失败，4-超时',
    `execution_result` JSON COMMENT '执行结果',
    `error_message` TEXT COMMENT '错误信息',
    `retry_count` INT DEFAULT 0 COMMENT '重试次数',
    `max_retry_count` INT DEFAULT 3 COMMENT '最大重试次数',
    `timeout_seconds` INT DEFAULT 30 COMMENT '超时时间（秒）',
    `priority` TINYINT DEFAULT 1 COMMENT '优先级：1-低，2-中，3-高，4-紧急',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `execute_time` DATETIME COMMENT '执行时间',
    `complete_time` DATETIME COMMENT '完成时间',
    `create_user_id` BIGINT NOT NULL COMMENT '创建人ID',
    `execute_user_id` BIGINT COMMENT '执行人ID',
    PRIMARY KEY (`command_id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_command_status` (`command_status`),
    KEY `idx_command_type` (`command_type`),
    KEY `idx_priority_create_time` (`priority`, `create_time`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备命令执行表';

-- 7. 设备维护记录表
CREATE TABLE `t_smart_device_maintenance` (
    `maintenance_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '维护ID',
    `device_id` BIGINT NOT NULL COMMENT '设备ID',
    `maintenance_type` TINYINT NOT NULL COMMENT '维护类型：1-日常维护，2-故障维修，3-升级更新，4-清洁检查',
    `maintenance_title` VARCHAR(255) NOT NULL COMMENT '维护标题',
    `maintenance_description` TEXT COMMENT '维护描述',
    `maintenance_start_time` DATETIME NOT NULL COMMENT '维护开始时间',
    `maintenance_end_time` DATETIME COMMENT '维护结束时间',
    `maintenance_duration` INT COMMENT '维护时长（分钟）',
    `maintenance_result` TINYINT COMMENT '维护结果：1-成功，2-部分成功，3-失败',
    `maintenance_cost` DECIMAL(10,2) COMMENT '维护费用',
    `maintenance_user` VARCHAR(128) COMMENT '维护人员',
    `maintenance_remark` TEXT COMMENT '维护备注',
    `attachments` JSON COMMENT '相关附件',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_user_id` BIGINT NOT NULL COMMENT '创建人ID',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_user_id` BIGINT COMMENT '更新人ID',
    PRIMARY KEY (`maintenance_id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_maintenance_type` (`maintenance_type`),
    KEY `idx_start_time` (`maintenance_start_time`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备维护记录表';

-- 8. 设备告警记录表
CREATE TABLE `t_smart_device_alarm` (
    `alarm_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '告警ID',
    `device_id` BIGINT NOT NULL COMMENT '设备ID',
    `alarm_type` VARCHAR(64) NOT NULL COMMENT '告警类型',
    `alarm_level` TINYINT NOT NULL COMMENT '告警级别：1-提示，2-警告，3-严重，4-紧急',
    `alarm_title` VARCHAR(255) NOT NULL COMMENT '告警标题',
    `alarm_description` TEXT COMMENT '告警描述',
    `alarm_value` VARCHAR(128) COMMENT '告警值',
    `threshold_value` VARCHAR(128) COMMENT '阈值',
    `alarm_status` TINYINT NOT NULL DEFAULT 1 COMMENT '告警状态：1-未处理，2-处理中，3-已处理，4-已忽略',
    `alarm_time` DATETIME NOT NULL COMMENT '告警时间',
    `handle_time` DATETIME COMMENT '处理时间',
    `handle_user_id` BIGINT COMMENT '处理人ID',
    `handle_remark` TEXT COMMENT '处理备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`alarm_id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_alarm_type` (`alarm_type`),
    KEY `idx_alarm_level` (`alarm_level`),
    KEY `idx_alarm_status` (`alarm_status`),
    KEY `idx_alarm_time` (`alarm_time`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备告警记录表';

-- 添加外键约束（可选，根据项目需要）
-- ALTER TABLE `t_smart_device` ADD CONSTRAINT `fk_device_location` FOREIGN KEY (`location_id`) REFERENCES `t_smart_location`(`location_id`);
-- ALTER TABLE `t_smart_device` ADD CONSTRAINT `fk_device_area` FOREIGN KEY (`area_id`) REFERENCES `t_smart_area`(`area_id`);

-- 初始化数据插入
INSERT INTO `t_smart_device_protocol_adapter` (`adapter_name`, `protocol_type`, `device_type`, `adapter_class`, `description`) VALUES
('HTTP摄像头适配器', 'HTTP', 1, 'net.lab1024.sa.base.module.device.adapter.HttpCameraAdapter', '支持标准HTTP协议的网络摄像头'),
('RTSP摄像头适配器', 'RTSP', 1, 'net.lab1024.sa.base.module.device.adapter.RtspCameraAdapter', '支持RTSP协议的摄像头'),
('WebSocket门禁适配器', 'WebSocket', 2, 'net.lab1024.sa.base.module.device.adapter.WebSocketAccessAdapter', '支持WebSocket协议的门禁设备'),
('TCP消费终端适配器', 'TCP', 3, 'net.lab1024.sa.base.module.device.adapter.TcpTerminalAdapter', '支持TCP协议的消费终端'),
('MQTT考勤设备适配器', 'MQTT', 4, 'net.lab1024.sa.base.module.device.adapter.MqttAttendanceAdapter', '支持MQTT协议的考勤设备');

-- 创建默认设备分组
INSERT INTO `t_smart_device_group` (`group_code`, `group_name`, `group_type`, `description`) VALUES
('CAMERA_GROUP', '摄像头设备', 1, '所有摄像头设备分组'),
('ACCESS_GROUP', '门禁设备', 1, '所有门禁设备分组'),
('TERMINAL_GROUP', '消费终端', 1, '所有消费终端设备分组'),
('ATTENDANCE_GROUP', '考勤设备', 1, '所有考勤设备分组');

-- 创建索引优化查询性能
CREATE INDEX `idx_device_composite` ON `t_smart_device` (`device_type`, `status`, `enabled_flag`, `deleted_flag`);
CREATE INDEX `idx_status_log_composite` ON `t_smart_device_status_log` (`device_id`, `create_time`);
CREATE INDEX `idx_command_composite` ON `t_smart_device_command` (`device_id`, `command_status`, `create_time`);