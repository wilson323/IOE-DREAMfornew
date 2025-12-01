-- ========================================
-- SmartAdmin 统一设备管理模块数据库表结构
-- 版本: v1.0.0
-- 创建时间: 2025-01-13
-- 说明: 统一设备管理模块的数据库表结构
-- ========================================

-- 1. 设备基础信息表
CREATE TABLE `t_smart_device` (
    `device_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '设备ID',
    `device_code` VARCHAR(100) NOT NULL COMMENT '设备编码',
    `device_name` VARCHAR(200) NOT NULL COMMENT '设备名称',
    `device_type` VARCHAR(50) NOT NULL COMMENT '设备类型：CAMERA-摄像头,ACCESS_CONTROLLER-门禁控制器,CONSUMPTION_TERMINAL-消费终端,ATTENDANCE_MACHINE-考勤机',
    `device_type_name` VARCHAR(100) NOT NULL COMMENT '设备类型名称',
    `device_brand` VARCHAR(100) COMMENT '设备品牌',
    `device_model` VARCHAR(100) COMMENT '设备型号',
    `device_serial` VARCHAR(100) COMMENT '设备序列号',
    `area_id` BIGINT COMMENT '所属区域ID',
    `area_name` VARCHAR(100) COMMENT '所属区域名称',
    `location_desc` VARCHAR(500) COMMENT '位置描述',
    `ip_address` VARCHAR(50) COMMENT 'IP地址',
    `port` INT COMMENT '端口号',
    `mac_address` VARCHAR(50) COMMENT 'MAC地址',
    `protocol_type` VARCHAR(50) COMMENT '通信协议：TCP,UDP,HTTP,WEBSOCKET,MODBUS',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-离线,1-在线,2-故障,3-维护中',
    `is_enabled` TINYINT DEFAULT 1 COMMENT '是否启用：0-禁用,1-启用',
    `group_id` BIGINT COMMENT '设备分组ID',
    `group_name` VARCHAR(100) COMMENT '设备分组名称',
    `config_json` JSON COMMENT '设备配置信息',
    `extend_info` JSON COMMENT '扩展信息',
    `last_online_time` DATETIME COMMENT '最后在线时间',
    `last_heartbeat_time` DATETIME COMMENT '最后心跳时间',
    `install_time` DATETIME COMMENT '安装时间',
    `warranty_end_time` DATETIME COMMENT '保修结束时间',
    `vendor_info` VARCHAR(500) COMMENT '供应商信息',
    `contact_person` VARCHAR(100) COMMENT '联系人',
    `contact_phone` VARCHAR(50) COMMENT '联系电话',

    -- 审计字段
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-正常,1-删除',
    `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',

    PRIMARY KEY (`device_id`),
    UNIQUE KEY `uk_device_code` (`device_code`),
    KEY `idx_device_type` (`device_type`),
    KEY `idx_area_id` (`area_id`),
    KEY `idx_group_id` (`group_id`),
    KEY `idx_status` (`status`),
    KEY `idx_is_enabled` (`is_enabled`),
    KEY `idx_ip_address` (`ip_address`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_last_online_time` (`last_online_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='设备基础信息表';

-- 2. 设备分组表
CREATE TABLE `t_smart_device_group` (
    `group_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分组ID',
    `group_code` VARCHAR(100) NOT NULL COMMENT '分组编码',
    `group_name` VARCHAR(200) NOT NULL COMMENT '分组名称',
    `group_type` VARCHAR(50) NOT NULL COMMENT '分组类型：AREA-区域分组,TYPE-类型分组,BUSINESS-业务分组',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父级分组ID',
    `group_level` INT DEFAULT 1 COMMENT '分组层级',
    `sort_order` INT DEFAULT 0 COMMENT '排序值',
    `description` VARCHAR(500) COMMENT '分组描述',
    `device_count` INT DEFAULT 0 COMMENT '设备数量',
    `is_enabled` TINYINT DEFAULT 1 COMMENT '是否启用：0-禁用,1-启用',

    -- 审计字段
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-正常,1-删除',
    `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',

    PRIMARY KEY (`group_id`),
    UNIQUE KEY `uk_group_code` (`group_code`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_group_type` (`group_type`),
    KEY `idx_sort_order` (`sort_order`),
    KEY `idx_is_enabled` (`is_enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='设备分组表';

-- 3. 设备状态监控表
CREATE TABLE `t_smart_device_status` (
    `status_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '状态ID',
    `device_id` BIGINT NOT NULL COMMENT '设备ID',
    `device_code` VARCHAR(100) NOT NULL COMMENT '设备编码',
    `status_type` VARCHAR(50) NOT NULL COMMENT '状态类型：ONLINE-在线,OFFLINE-离线,FAULT-故障,MAINTENANCE-维护',
    `status_value` VARCHAR(200) COMMENT '状态值',
    `status_desc` VARCHAR(500) COMMENT '状态描述',
    `cpu_usage` DECIMAL(5,2) COMMENT 'CPU使用率',
    `memory_usage` DECIMAL(5,2) COMMENT '内存使用率',
    `disk_usage` DECIMAL(5,2) COMMENT '磁盘使用率',
    `network_status` VARCHAR(50) COMMENT '网络状态',
    `error_count` INT DEFAULT 0 COMMENT '错误次数',
    `last_error_time` DATETIME COMMENT '最后错误时间',
    `last_error_msg` TEXT COMMENT '最后错误信息',
    `status_data` JSON COMMENT '状态数据',

    -- 时间戳
    `record_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '记录时间',

    PRIMARY KEY (`status_id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_device_code` (`device_code`),
    KEY `idx_status_type` (`status_type`),
    KEY `idx_record_time` (`record_time`),
    KEY `idx_device_status` (`device_id`, `status_type`, `record_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='设备状态监控表';

-- 4. 设备操作日志表
CREATE TABLE `t_smart_device_operation_log` (
    `log_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `device_id` BIGINT NOT NULL COMMENT '设备ID',
    `device_code` VARCHAR(100) NOT NULL COMMENT '设备编码',
    `operation_type` VARCHAR(50) NOT NULL COMMENT '操作类型：CONTROL-控制,CONFIG-配置,RESTART-重启,UPDATE-升级',
    `operation_desc` VARCHAR(500) COMMENT '操作描述',
    `operation_data` JSON COMMENT '操作数据',
    `operation_result` VARCHAR(50) COMMENT '操作结果：SUCCESS-成功,FAILED-失败,TIMEOUT-超时',
    `result_msg` VARCHAR(1000) COMMENT '结果消息',
    `execute_time` INT COMMENT '执行耗时(毫秒)',
    `operator_id` BIGINT COMMENT '操作人ID',
    `operator_name` VARCHAR(100) COMMENT '操作人姓名',
    `client_ip` VARCHAR(50) COMMENT '客户端IP',
    `user_agent` VARCHAR(500) COMMENT '用户代理',

    -- 审计字段
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',

    PRIMARY KEY (`log_id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_device_code` (`device_code`),
    KEY `idx_operation_type` (`operation_type`),
    KEY `idx_operation_result` (`operation_result`),
    KEY `idx_operator_id` (`operator_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='设备操作日志表';

-- 5. 设备配置模板表
CREATE TABLE `t_smart_device_config_template` (
    `template_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '模板ID',
    `template_code` VARCHAR(100) NOT NULL COMMENT '模板编码',
    `template_name` VARCHAR(200) NOT NULL COMMENT '模板名称',
    `device_type` VARCHAR(50) NOT NULL COMMENT '适用设备类型',
    `template_desc` VARCHAR(500) COMMENT '模板描述',
    `config_schema` JSON COMMENT '配置模式',
    `default_config` JSON COMMENT '默认配置',
    `is_system` TINYINT DEFAULT 0 COMMENT '是否系统模板：0-用户模板,1-系统模板',
    `use_count` INT DEFAULT 0 COMMENT '使用次数',
    `is_enabled` TINYINT DEFAULT 1 COMMENT '是否启用：0-禁用,1-启用',

    -- 审计字段
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-正常,1-删除',
    `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',

    PRIMARY KEY (`template_id`),
    UNIQUE KEY `uk_template_code` (`template_code`),
    KEY `idx_device_type` (`device_type`),
    KEY `idx_is_system` (`is_system`),
    KEY `idx_is_enabled` (`is_enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='设备配置模板表';

-- 6. 设备协议配置表
CREATE TABLE `t_smart_device_protocol` (
    `protocol_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '协议ID',
    `protocol_code` VARCHAR(100) NOT NULL COMMENT '协议编码',
    `protocol_name` VARCHAR(200) NOT NULL COMMENT '协议名称',
    `protocol_type` VARCHAR(50) NOT NULL COMMENT '协议类型：TCP,UDP,HTTP,WEBSOCKET,MODBUS,RTSP',
    `device_type` VARCHAR(50) NOT NULL COMMENT '适用设备类型',
    `protocol_desc` VARCHAR(500) COMMENT '协议描述',
    `connection_config` JSON COMMENT '连接配置',
    `command_config` JSON COMMENT '指令配置',
    `response_config` JSON COMMENT '响应配置',
    `heartbeat_config` JSON COMMENT '心跳配置',
    `timeout_config` JSON COMMENT '超时配置',
    `is_system` TINYINT DEFAULT 0 COMMENT '是否系统协议：0-用户协议,1-系统协议',
    `is_enabled` TINYINT DEFAULT 1 COMMENT '是否启用：0-禁用,1-启用',

    -- 审计字段
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-正常,1-删除',
    `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',

    PRIMARY KEY (`protocol_id`),
    UNIQUE KEY `uk_protocol_code` (`protocol_code`),
    KEY `idx_protocol_type` (`protocol_type`),
    KEY `idx_device_type` (`device_type`),
    KEY `idx_is_system` (`is_system`),
    KEY `idx_is_enabled` (`is_enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='设备协议配置表';

-- 7. 设备维护记录表
CREATE TABLE `t_smart_device_maintenance` (
    `maintenance_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '维护ID',
    `device_id` BIGINT NOT NULL COMMENT '设备ID',
    `device_code` VARCHAR(100) NOT NULL COMMENT '设备编码',
    `maintenance_type` VARCHAR(50) NOT NULL COMMENT '维护类型：ROUTINE-例行维护,REPAIR-故障维修,UPGRADE-升级,INSPECTION-巡检',
    `maintenance_title` VARCHAR(200) NOT NULL COMMENT '维护标题',
    `maintenance_desc` TEXT COMMENT '维护描述',
    `maintenance_result` VARCHAR(50) COMMENT '维护结果：SUCCESS-成功,FAILED-失败,PARTIAL-部分成功',
    `maintenance_cost` DECIMAL(12,2) COMMENT '维护费用',
    `maintenance_time` DATETIME COMMENT '维护时间',
    `completion_time` DATETIME COMMENT '完成时间',
    `next_maintenance_time` DATETIME COMMENT '下次维护时间',
    `maintenance_user_id` BIGINT COMMENT '维护人ID',
    `maintenance_user_name` VARCHAR(100) COMMENT '维护人姓名',
    `maintenance_data` JSON COMMENT '维护数据',

    -- 审计字段
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-正常,1-删除',
    `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',

    PRIMARY KEY (`maintenance_id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_device_code` (`device_code`),
    KEY `idx_maintenance_type` (`maintenance_type`),
    KEY `idx_maintenance_time` (`maintenance_time`),
    KEY `idx_next_maintenance_time` (`next_maintenance_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='设备维护记录表';

-- 8. 设备统计数据表
CREATE TABLE `t_smart_device_statistics` (
    `stat_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '统计ID',
    `device_id` BIGINT NOT NULL COMMENT '设备ID',
    `device_code` VARCHAR(100) NOT NULL COMMENT '设备编码',
    `device_type` VARCHAR(50) NOT NULL COMMENT '设备类型',
    `area_id` BIGINT COMMENT '区域ID',
    `group_id` BIGINT COMMENT '分组ID',
    `stat_date` DATE NOT NULL COMMENT '统计日期',
    `stat_type` VARCHAR(50) NOT NULL COMMENT '统计类型：DAY-日,WEEK-周,MONTH-月',
    `online_duration` INT DEFAULT 0 COMMENT '在线时长(分钟)',
    `offline_duration` INT DEFAULT 0 COMMENT '离线时长(分钟)',
    `fault_duration` INT DEFAULT 0 COMMENT '故障时长(分钟)',
    `operation_count` INT DEFAULT 0 COMMENT '操作次数',
    `success_count` INT DEFAULT 0 COMMENT '成功次数',
    `failed_count` INT DEFAULT 0 COMMENT '失败次数',
    `data_volume` BIGINT DEFAULT 0 COMMENT '数据流量(字节)',
    `stat_data` JSON COMMENT '统计数据',

    -- 审计字段
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',

    PRIMARY KEY (`stat_id`),
    UNIQUE KEY `uk_device_stat` (`device_id`, `stat_date`, `stat_type`),
    KEY `idx_device_code` (`device_code`),
    KEY `idx_device_type` (`device_type`),
    KEY `idx_area_id` (`area_id`),
    KEY `idx_group_id` (`group_id`),
    KEY `idx_stat_date` (`stat_date`),
    KEY `idx_stat_type` (`stat_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='设备统计数据表';

-- ========================================
-- 初始化基础数据
-- ========================================

-- 初始化设备分组
INSERT INTO `t_smart_device_group` (`group_code`, `group_name`, `group_type`, `parent_id`, `group_level`, `sort_order`, `description`, `create_user_id`) VALUES
('ROOT', '根分组', 'AREA', 0, 1, 0, '设备根分组', 1),
('CAMERA_GROUP', '摄像头设备', 'TYPE', 0, 1, 1, '摄像头设备分组', 1),
('ACCESS_GROUP', '门禁设备', 'TYPE', 0, 1, 2, '门禁设备分组', 1),
('CONSUMPTION_GROUP', '消费终端', 'TYPE', 0, 1, 3, '消费终端分组', 1),
('ATTENDANCE_GROUP', '考勤设备', 'TYPE', 0, 1, 4, '考勤设备分组', 1);

-- 初始化设备协议配置
INSERT INTO `t_smart_device_protocol` (`protocol_code`, `protocol_name`, `protocol_type`, `device_type`, `protocol_desc`, `connection_config`, `is_system`, `is_enabled`, `create_user_id`) VALUES
('TCP_DEFAULT', 'TCP默认协议', 'TCP', 'ALL', 'TCP协议默认配置', '{"timeout": 5000, "retryTimes": 3, "keepAlive": true}', 1, 1, 1),
('HTTP_REST', 'HTTP REST协议', 'HTTP', 'ALL', 'HTTP REST API协议配置', '{"timeout": 10000, "retryTimes": 2, "headers": {}}', 1, 1, 1),
('WEBSOCKET_JSON', 'WebSocket JSON协议', 'WEBSOCKET', 'ALL', 'WebSocket JSON协议配置', '{"heartbeatInterval": 30, "reconnectInterval": 5}', 1, 1, 1),
('RTSP_STREAM', 'RTSP流媒体协议', 'RTSP', 'CAMERA', 'RTSP视频流协议配置', '{"timeout": 10000, "transport": "TCP"}', 1, 1, 1);

-- 初始化设备配置模板
INSERT INTO `t_smart_device_config_template` (`template_code`, `template_name`, `device_type`, `template_desc`, `config_schema`, `default_config`, `is_system`, `is_enabled`, `create_user_id`) VALUES
('CAMERA_DEFAULT', '摄像头默认配置', 'CAMERA', '摄像头设备默认配置模板', '{"resolution": {"type": "string", "enum": ["1080P", "720P", "4K"]}, "frameRate": {"type": "number", "min": 1, "max": 60}}', '{"resolution": "1080P", "frameRate": 25, "bitrate": 2048}', 1, 1, 1),
('ACCESS_DEFAULT', '门禁默认配置', 'ACCESS_CONTROLLER', '门禁设备默认配置模板', '{"openMethod": {"type": "string", "enum": ["CARD", "FACE", "FINGER", "PASSWORD"]}, "openTime": {"type": "number", "min": 1, "max": 30}}', '{"openMethod": "CARD", "openTime": 5, "verificationMode": "SINGLE"}', 1, 1, 1);