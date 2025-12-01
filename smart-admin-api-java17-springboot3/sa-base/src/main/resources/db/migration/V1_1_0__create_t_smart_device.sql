-- 创建智能设备表
-- 说明：与 SmartDeviceEntity 字段、BaseEntity 审计字段一致

CREATE TABLE IF NOT EXISTS `t_smart_device` (
  `device_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '设备ID',
  `device_code` VARCHAR(64) NOT NULL COMMENT '设备编码',
  `device_name` VARCHAR(128) NOT NULL COMMENT '设备名称',
  `device_type` VARCHAR(32) NOT NULL COMMENT '设备类型(CAMERA/ACCESS/CONSUME/ATTENDANCE)',
  `device_status` VARCHAR(32) NOT NULL DEFAULT 'OFFLINE' COMMENT '设备状态(ONLINE/OFFLINE/FAULT/MAINTAIN)',
  `ip_address` VARCHAR(64) NULL COMMENT 'IP地址',
  `port` INT NULL COMMENT '端口',
  `protocol_type` VARCHAR(32) NULL COMMENT '协议类型(TCP/UDP/HTTP/HTTPS/MQTT)',
  `location` VARCHAR(256) NULL COMMENT '位置',
  `description` VARCHAR(512) NULL COMMENT '描述',
  `manufacturer` VARCHAR(128) NULL COMMENT '制造商',
  `device_model` VARCHAR(128) NULL COMMENT '设备型号',
  `firmware_version` VARCHAR(64) NULL COMMENT '固件版本',
  `install_date` DATETIME NULL COMMENT '安装日期',
  `last_online_time` DATETIME NULL COMMENT '最后在线时间',
  `group_id` BIGINT NULL COMMENT '分组ID',
  `config_json` JSON NULL COMMENT '配置信息JSON',
  `enabled_flag` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用(0/1)',
  `remark` VARCHAR(512) NULL COMMENT '备注',
  -- BaseEntity 审计字段
  `create_time` DATETIME NULL,
  `update_time` DATETIME NULL,
  `create_user_id` BIGINT NULL,
  `update_user_id` BIGINT NULL,
  `deleted_flag` TINYINT NOT NULL DEFAULT 0,
  `version` INT NOT NULL DEFAULT 0,
  PRIMARY KEY (`device_id`),
  UNIQUE KEY `uk_device_code` (`device_code`),
  KEY `idx_device_type` (`device_type`),
  KEY `idx_device_status` (`device_status`),
  KEY `idx_last_online_time` (`last_online_time`),
  KEY `idx_group_id` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='智能设备表';


