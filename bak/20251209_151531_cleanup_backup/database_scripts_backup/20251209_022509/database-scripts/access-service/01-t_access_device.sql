-- ============================================================
-- 门禁设备表 - t_access_device
-- 说明: 管理所有门禁设备信息，包括设备状态、配置、位置等
-- ============================================================

USE `ioedream_access_db`;

DROP TABLE IF EXISTS `t_access_device`;

CREATE TABLE `t_access_device` (
    -- 主键
    `device_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '设备ID',
    
    -- 设备基本信息
    `device_code` VARCHAR(100) NOT NULL COMMENT '设备编码',
    `device_name` VARCHAR(200) NOT NULL COMMENT '设备名称',
    `device_type` VARCHAR(50) NOT NULL COMMENT '设备类型：CARD-刷卡, FACE-人脸, FINGERPRINT-指纹',
    `ip_address` VARCHAR(50) COMMENT 'IP地址',
    `port` INT COMMENT '通讯端口',
    `area_id` BIGINT COMMENT '所属区域ID',
    
    -- 设备协议与通讯
    `protocol_type` VARCHAR(50) COMMENT '协议类型：Wiegand-韦根协议, TCP-TCP协议',
    `device_status` TINYINT DEFAULT 0 COMMENT '设备状态：0-离线, 1-在线',
    
    -- 设备详细信息
    `manufacturer` VARCHAR(100) COMMENT '制造商',
    `device_model` VARCHAR(100) COMMENT '设备型号',
    `firmware_version` VARCHAR(50) COMMENT '固件版本',
    `install_date` DATE COMMENT '安装日期',
    `last_online_time` DATETIME COMMENT '最后在线时间',
    
    -- 设备配置（JSON格式存储）
    `config_json` JSON COMMENT '设备配置（JSON格式）',
    
    -- 状态标记
    `enabled_flag` TINYINT DEFAULT 1 COMMENT '启用标记：0-禁用, 1-启用',
    `remark` VARCHAR(500) COMMENT '备注',
    
    -- 审计字段
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除, 1-已删除',
    
    -- 主键约束
    PRIMARY KEY (`device_id`),
    
    -- 唯一约束
    UNIQUE KEY `uk_device_code` (`device_code`),
    
    -- 普通索引
    INDEX `idx_area_id` (`area_id`),
    INDEX `idx_device_status` (`device_status`, `enabled_flag`),
    INDEX `idx_create_time` (`create_time`),
    INDEX `idx_deleted_flag` (`deleted_flag`)
    
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='门禁设备表';
