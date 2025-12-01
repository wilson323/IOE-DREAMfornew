-- 统一设备管理表
-- 严格遵循repowiki数据库设计规范：
-- - 表命名格式：t_{business}_{entity}
-- - 主键格式：{table}_id
-- - 必须包含审计字段：create_time, update_time, create_user_id, update_user_id, deleted_flag, version
-- - 使用utf8mb4字符集，InnoDB存储引擎
-- - 软删除使用deleted_flag字段

-- 创建统一设备表
CREATE TABLE `t_unified_device` (
  `device_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '设备ID',
  `device_code` VARCHAR(64) NOT NULL COMMENT '设备编号（设备唯一标识）',
  `device_name` VARCHAR(128) NOT NULL COMMENT '设备名称',
  `device_type` VARCHAR(32) NOT NULL COMMENT '设备类型：ACCESS-门禁设备,VIDEO-视频设备,CONSUME-消费设备,ATTENDANCE-考勤设备,SMART-智能设备',
  `device_model` VARCHAR(64) DEFAULT NULL COMMENT '设备型号',
  `manufacturer` VARCHAR(64) DEFAULT NULL COMMENT '设备厂商',
  `serial_number` VARCHAR(64) DEFAULT NULL COMMENT '设备序列号',
  `ip_address` VARCHAR(45) DEFAULT NULL COMMENT '设备IP地址',
  `port` INT DEFAULT NULL COMMENT '设备端口号',
  `mac_address` VARCHAR(17) DEFAULT NULL COMMENT '设备MAC地址',
  `area_id` BIGINT DEFAULT NULL COMMENT '所属区域ID（主要用于门禁设备）',
  `area_name` VARCHAR(128) DEFAULT NULL COMMENT '所属区域名称',
  `location` VARCHAR(255) DEFAULT NULL COMMENT '设备位置描述',
  `online_status` TINYINT DEFAULT 0 COMMENT '在线状态：0-离线，1-在线',
  `enabled` TINYINT DEFAULT 1 COMMENT '启用状态：0-禁用，1-启用',
  `work_mode` TINYINT DEFAULT 0 COMMENT '工作模式（主要用于门禁设备）：0-普通模式，1-刷卡模式，2-人脸模式，3-指纹模式，4-混合模式',
  `device_status` VARCHAR(32) DEFAULT 'NORMAL' COMMENT '设备状态：NORMAL-正常，FAULT-故障，MAINTENANCE-维护中，OFFLINE-离线',
  `last_heartbeat_time` DATETIME DEFAULT NULL COMMENT '最后心跳时间',
  `heartbeat_interval` INT DEFAULT 60 COMMENT '心跳间隔（秒）',

  -- 视频设备专用字段
  `stream_url` VARCHAR(512) DEFAULT NULL COMMENT '视频流地址（视频设备专用）',
  `video_format` VARCHAR(16) DEFAULT NULL COMMENT '视频格式（视频设备专用）',
  `resolution` VARCHAR(32) DEFAULT NULL COMMENT '分辨率（视频设备专用）',
  `frame_rate` INT DEFAULT NULL COMMENT '帧率（视频设备专用）',
  `bitrate` INT DEFAULT NULL COMMENT '码率(kbps)（视频设备专用）',
  `support_ptz` TINYINT DEFAULT 0 COMMENT '是否支持云台控制（视频设备专用）',
  `support_recording` TINYINT DEFAULT 0 COMMENT '是否支持录像（视频设备专用）',

  -- 门禁设备专用字段
  `support_remote_open` TINYINT DEFAULT 0 COMMENT '是否支持远程开门（门禁设备专用）',
  `support_face_recognition` TINYINT DEFAULT 0 COMMENT '是否支持人脸识别（门禁设备专用）',
  `support_fingerprint` TINYINT DEFAULT 0 COMMENT '是否支持指纹识别（门禁设备专用）',
  `support_card` TINYINT DEFAULT 1 COMMENT '是否支持刷卡（门禁设备专用）',

  -- 设备配置信息（JSON格式存储）
  `device_config` TEXT DEFAULT NULL COMMENT '设备配置信息（JSON格式）',
  `extend_properties` TEXT DEFAULT NULL COMMENT '设备扩展属性（JSON格式）',

  -- 维护信息
  `install_time` DATETIME DEFAULT NULL COMMENT '安装时间',
  `last_maintenance_time` DATETIME DEFAULT NULL COMMENT '最后维护时间',
  `maintenance_cycle` INT DEFAULT NULL COMMENT '维护周期（天）',
  `remark` TEXT DEFAULT NULL COMMENT '设备备注',

  -- 审计字段
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user_id` BIGINT DEFAULT NULL COMMENT '创建用户ID',
  `update_user_id` BIGINT DEFAULT NULL COMMENT '更新用户ID',
  `deleted_flag` TINYINT DEFAULT 0 COMMENT '删除标志：0-正常，1-删除',
  `version` INT DEFAULT 1 COMMENT '版本号（乐观锁）',

  PRIMARY KEY (`device_id`),
  UNIQUE KEY `uk_device_code` (`device_code`),
  KEY `idx_device_type` (`device_type`),
  KEY `idx_online_status` (`online_status`),
  KEY `idx_enabled` (`enabled`),
  KEY `idx_device_status` (`device_status`),
  KEY `idx_area_id` (`area_id`),
  KEY `idx_ip_port` (`ip_address`, `port`),
  KEY `idx_mac_address` (`mac_address`),
  KEY `idx_last_heartbeat_time` (`last_heartbeat_time`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_deleted_flag` (`deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='统一设备管理表';

-- 创建设备状态变更历史表
CREATE TABLE `t_device_status_history` (
  `history_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '历史记录ID',
  `device_id` BIGINT NOT NULL COMMENT '设备ID',
  `old_status` VARCHAR(32) DEFAULT NULL COMMENT '旧状态',
  `new_status` VARCHAR(32) NOT NULL COMMENT '新状态',
  `change_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '变更时间',
  `change_reason` VARCHAR(255) DEFAULT NULL COMMENT '变更原因',
  `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
  `operator_name` VARCHAR(64) DEFAULT NULL COMMENT '操作人姓名',

  -- 审计字段
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user_id` BIGINT DEFAULT NULL COMMENT '创建用户ID',
  `update_user_id` BIGINT DEFAULT NULL COMMENT '更新用户ID',
  `deleted_flag` TINYINT DEFAULT 0 COMMENT '删除标志：0-正常，1-删除',
  `version` INT DEFAULT 1 COMMENT '版本号（乐观锁）',

  PRIMARY KEY (`history_id`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_change_time` (`change_time`),
  KEY `idx_new_status` (`new_status`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_deleted_flag` (`deleted_flag`),

  CONSTRAINT `fk_device_status_history_device` FOREIGN KEY (`device_id`) REFERENCES `t_unified_device` (`device_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备状态变更历史表';

-- 创建设备通信日志表
CREATE TABLE `t_device_communication_log` (
  `log_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `device_id` BIGINT NOT NULL COMMENT '设备ID',
  `command` VARCHAR(64) NOT NULL COMMENT '命令',
  `request_data` TEXT DEFAULT NULL COMMENT '请求数据（JSON格式）',
  `response_data` TEXT DEFAULT NULL COMMENT '响应数据（JSON格式）',
  `communication_status` VARCHAR(16) NOT NULL COMMENT '通信状态：SUCCESS-成功，FAILED-失败，TIMEOUT-超时',
  `communication_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '通信时间',
  `response_time` INT DEFAULT NULL COMMENT '响应时间（毫秒）',
  `error_message` TEXT DEFAULT NULL COMMENT '错误信息',
  `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
  `operator_name` VARCHAR(64) DEFAULT NULL COMMENT '操作人姓名',

  -- 审计字段
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user_id` BIGINT DEFAULT NULL COMMENT '创建用户ID',
  `update_user_id` BIGINT DEFAULT NULL COMMENT '更新用户ID',
  `deleted_flag` TINYINT DEFAULT 0 COMMENT '删除标志：0-正常，1-删除',
  `version` INT DEFAULT 1 COMMENT '版本号（乐观锁）',

  PRIMARY KEY (`log_id`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_communication_time` (`communication_time`),
  KEY `idx_communication_status` (`communication_status`),
  KEY `idx_command` (`command`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_deleted_flag` (`deleted_flag`),

  CONSTRAINT `fk_device_communication_log_device` FOREIGN KEY (`device_id`) REFERENCES `t_unified_device` (`device_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备通信日志表';

-- 创建设备配置变更历史表
CREATE TABLE `t_device_config_history` (
  `history_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '历史记录ID',
  `device_id` BIGINT NOT NULL COMMENT '设备ID',
  `old_config` TEXT DEFAULT NULL COMMENT '旧配置（JSON格式）',
  `new_config` TEXT DEFAULT NULL COMMENT '新配置（JSON格式）',
  `change_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '变更时间',
  `change_reason` VARCHAR(255) DEFAULT NULL COMMENT '变更原因',
  `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
  `operator_name` VARCHAR(64) DEFAULT NULL COMMENT '操作人姓名',

  -- 审计字段
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user_id` BIGINT DEFAULT NULL COMMENT '创建用户ID',
  `update_user_id` BIGINT DEFAULT NULL COMMENT '更新用户ID',
  `deleted_flag` TINYINT DEFAULT 0 COMMENT '删除标志：0-正常，1-删除',
  `version` INT DEFAULT 1 COMMENT '版本号（乐观锁）',

  PRIMARY KEY (`history_id`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_change_time` (`change_time`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_deleted_flag` (`deleted_flag`),

  CONSTRAINT `fk_device_config_history_device` FOREIGN KEY (`device_id`) REFERENCES `t_unified_device` (`device_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备配置变更历史表';

-- 创建设备维修记录表
CREATE TABLE `t_device_maintenance_record` (
  `record_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '维修记录ID',
  `device_id` BIGINT NOT NULL COMMENT '设备ID',
  `maintenance_type` VARCHAR(32) NOT NULL COMMENT '维修类型：ROUTINE-例行维护，FAULT-故障维修，UPGRADE-升级维护',
  `maintenance_description` TEXT NOT NULL COMMENT '维修描述',
  `maintenance_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '维修时间',
  `maintenance_duration` INT DEFAULT NULL COMMENT '维修时长（分钟）',
  `maintenance_cost` DECIMAL(10,2) DEFAULT NULL COMMENT '维修费用',
  `parts_replaced` TEXT DEFAULT NULL COMMENT '更换部件（JSON格式）',
  `maintenance_result` VARCHAR(32) NOT NULL COMMENT '维修结果：SUCCESS-成功，PARTIAL-部分成功，FAILED-失败',
  `next_maintenance_time` DATETIME DEFAULT NULL COMMENT '下次维护时间',
  `maintenance_user_id` BIGINT DEFAULT NULL COMMENT '维修人员ID',
  `maintenance_user_name` VARCHAR(64) DEFAULT NULL COMMENT '维修人员姓名',
  `remark` TEXT DEFAULT NULL COMMENT '备注',

  -- 审计字段
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user_id` BIGINT DEFAULT NULL COMMENT '创建用户ID',
  `update_user_id` BIGINT DEFAULT NULL COMMENT '更新用户ID',
  `deleted_flag` TINYINT DEFAULT 0 COMMENT '删除标志：0-正常，1-删除',
  `version` INT DEFAULT 1 COMMENT '版本号（乐观锁）',

  PRIMARY KEY (`record_id`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_maintenance_time` (`maintenance_time`),
  KEY `idx_maintenance_type` (`maintenance_type`),
  KEY `idx_maintenance_result` (`maintenance_result`),
  KEY `idx_next_maintenance_time` (`next_maintenance_time`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_deleted_flag` (`deleted_flag`),

  CONSTRAINT `fk_device_maintenance_record_device` FOREIGN KEY (`device_id`) REFERENCES `t_unified_device` (`device_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备维修记录表';

-- 插入一些基础配置数据
INSERT INTO `t_unified_device` (
  `device_code`, `device_name`, `device_type`, `device_model`, `manufacturer`,
  `ip_address`, `port`, `location`, `device_status`, `online_status`, `enabled`,
  `support_remote_open`, `support_face_recognition`, `support_ptz`, `support_recording`
) VALUES
('DEV001', '主门门禁设备', 'ACCESS', 'ZKTeco-Pro', 'ZKTeco', '192.168.1.100', 4370, '主入口', 'NORMAL', 1, 1, 1, 1, 0, 0),
('DEV002', '侧门门禁设备', 'ACCESS', 'ZKTeco-Pro', 'ZKTeco', '192.168.1.101', 4370, '侧入口', 'NORMAL', 1, 1, 1, 0, 0, 0),
('CAM001', '大厅监控摄像头', 'VIDEO', 'Hikvision-DS2CD', 'Hikvision', '192.168.1.200', 554, '大厅中央', 'NORMAL', 1, 1, 0, 0, 1, 1),
('CAM002', '入口监控摄像头', 'VIDEO', 'Hikvision-DS2CD', 'Hikvision', '192.168.1.201', 554, '主入口上方', 'NORMAL', 1, 1, 0, 0, 1, 1);

-- 创建索引优化查询性能
CREATE INDEX `idx_unified_device_composite` ON `t_unified_device` (`device_type`, `online_status`, `enabled`);
CREATE INDEX `idx_unified_device_maintenance` ON `t_unified_device` (`maintenance_cycle`, `last_maintenance_time`);
CREATE INDEX `idx_unified_device_heartbeat` ON `t_unified_device` (`heartbeat_interval`, `last_heartbeat_time`);

-- 添加表注释
ALTER TABLE `t_unified_device` COMMENT = '统一设备管理表 - 整合门禁、视频、消费、考勤等所有设备类型，遵循repowiki数据库设计规范';
ALTER TABLE `t_device_status_history` COMMENT = '设备状态变更历史表 - 记录设备状态变更历史，支持问题追踪和数据分析';
ALTER TABLE `t_device_communication_log` COMMENT = '设备通信日志表 - 记录设备通信日志，支持通信监控和故障分析';
ALTER TABLE `t_device_config_history` COMMENT = '设备配置变更历史表 - 记录设备配置变更历史，支持配置回滚和审计';
ALTER TABLE `t_device_maintenance_record` COMMENT = '设备维修记录表 - 记录设备维修保养记录，支持维护计划制定和成本分析';