-- ============================================================
-- 门禁事件表 - t_access_event
-- 说明: 记录所有门禁系统的事件日志，包括告警、故障、维护等
-- ============================================================

USE `ioedream_access_db`;

DROP TABLE IF EXISTS `t_access_event`;

CREATE TABLE `t_access_event` (
    -- 主键
    `event_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '事件ID',
    
    -- 事件基本信息
    `event_type` VARCHAR(50) NOT NULL COMMENT '事件类型：ALARM-告警, FORCE-强行, TAMPER-篡改, MAINTENANCE-维护',
    `device_id` BIGINT COMMENT '设备ID',
    `area_id` BIGINT COMMENT '区域ID',
    `person_id` BIGINT COMMENT '相关人员ID',
    `person_name` VARCHAR(100) COMMENT '相关人员姓名',
    
    -- 事件级别与内容
    `event_level` VARCHAR(50) NOT NULL COMMENT '事件级别：INFO-信息, WARNING-警告, ERROR-错误, CRITICAL-严重',
    `event_content` TEXT NOT NULL COMMENT '事件内容',
    `event_time` DATETIME NOT NULL COMMENT '事件时间',
    
    -- 附加信息
    `snapshot_url` VARCHAR(500) COMMENT '快照URL',
    `video_url` VARCHAR(500) COMMENT '视频URL',
    `ai_result` JSON COMMENT 'AI分析结果',
    `confidence` DECIMAL(5,2) COMMENT '置信度',
    
    -- 处理状态
    `handle_status` VARCHAR(50) DEFAULT 'PENDING' COMMENT '处理状态：PENDING-待处理, HANDLED-已处理, IGNORED-已忽略',
    `handle_user_id` BIGINT COMMENT '处理人ID',
    `handle_time` DATETIME COMMENT '处理时间',
    `handle_remark` TEXT COMMENT '处理备注',
    `is_false_alarm` TINYINT DEFAULT 0 COMMENT '是否误报：0-否, 1-是',
    
    -- 审计字段
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除, 1-已删除',
    
    -- 主键约束
    PRIMARY KEY (`event_id`),
    
    -- 普通索引
    INDEX `idx_device_time` (`device_id`, `event_time` DESC),
    INDEX `idx_area_time` (`area_id`, `event_time` DESC),
    INDEX `idx_event_type` (`event_type`, `event_level`),
    INDEX `idx_handle_status` (`handle_status`, `deleted_flag`),
    INDEX `idx_event_time` (`event_time` DESC),
    INDEX `idx_person_id` (`person_id`, `event_time` DESC)
    
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='门禁事件表';
