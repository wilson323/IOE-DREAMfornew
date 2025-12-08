-- ============================================================
-- 通行记录表 - t_access_record
-- 说明: 记录所有门禁通行事件，包括成功和失败的通行记录
-- ============================================================

USE `ioedream_access_db`;

DROP TABLE IF EXISTS `t_access_record`;

CREATE TABLE `t_access_record` (
    -- 主键
    `record_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    
    -- 设备与人员
    `device_id` BIGINT NOT NULL COMMENT '设备ID',
    `person_id` BIGINT COMMENT '人员ID',
    `person_name` VARCHAR(100) COMMENT '人员姓名',
    `person_type` VARCHAR(50) COMMENT '人员类型：EMPLOYEE-员工, VISITOR-访客',
    
    -- 通行方式与验证
    `access_method` VARCHAR(50) NOT NULL COMMENT '通行方式：CARD-刷卡, FACE-人脸, FINGERPRINT-指纹',
    `credential_info` JSON COMMENT '凭证信息（JSON格式）',
    `verification_result` VARCHAR(50) NOT NULL COMMENT '验证结果：SUCCESS-成功, FAILED-失败',
    `match_score` DECIMAL(5,2) COMMENT '匹配分数（0-100）',
    
    -- 通行信息
    `access_direction` VARCHAR(20) NOT NULL COMMENT '通行方向：IN-进入, OUT-外出',
    `access_time` DATETIME NOT NULL COMMENT '通行时间',
    `area_id` BIGINT COMMENT '区域ID',
    
    -- 体温检测
    `temperature` DECIMAL(4,1) COMMENT '体温（℃）',
    
    -- 照片与视频
    `photo_url` VARCHAR(500) COMMENT '现场照片URL',
    `video_url` VARCHAR(500) COMMENT '视频URL',
    
    -- 事件类型
    `event_type` VARCHAR(50) DEFAULT 'NORMAL' COMMENT '事件类型：NORMAL-正常, FORCE-强行, ALARM-告警',
    `abnormal_reason` VARCHAR(500) COMMENT '异常原因',
    
    -- 审计字段
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除, 1-已删除',
    
    -- 主键约束
    PRIMARY KEY (`record_id`),
    
    -- 普通索引
    INDEX `idx_person_time` (`person_id`, `access_time` DESC),
    INDEX `idx_device_time` (`device_id`, `access_time` DESC),
    INDEX `idx_area_time` (`area_id`, `access_time` DESC),
    INDEX `idx_event_type` (`event_type`, `verification_result`),
    INDEX `idx_access_time` (`access_time` DESC),
    INDEX `idx_deleted_flag` (`deleted_flag`)
    
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='通行记录表';
