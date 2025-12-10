-- ============================================================
-- 门禁规则表 - t_access_rule
-- 说明: 定义门禁控制的业务规则，如APB反潜回、互锁等
-- ============================================================

USE `ioedream_access_db`;

DROP TABLE IF EXISTS `t_access_rule`;

CREATE TABLE `t_access_rule` (
    -- 主键
    `rule_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '规则ID',
    
    -- 规则基本信息
    `rule_name` VARCHAR(200) NOT NULL COMMENT '规则名称',
    `rule_type` VARCHAR(50) NOT NULL COMMENT '规则类型：APB-反潜回, INTERLOCK-互锁, LINKAGE-联动',
    `rule_config` JSON NOT NULL COMMENT '规则配置（JSON格式）',
    
    -- 优先级与状态
    `priority` INT DEFAULT 0 COMMENT '优先级（数字越大优先级越高）',
    `enabled_flag` TINYINT DEFAULT 1 COMMENT '启用标记：0-禁用, 1-启用',
    
    -- 生效时间配置
    `effective_time` JSON COMMENT '生效时间配置',
    
    -- 审计字段
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除, 1-已删除',
    
    -- 主键约束
    PRIMARY KEY (`rule_id`),
    
    -- 普通索引
    INDEX `idx_rule_type` (`rule_type`),
    INDEX `idx_priority` (`priority` DESC),
    INDEX `idx_enabled_flag` (`enabled_flag`, `deleted_flag`)
    
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='门禁规则表';

-- 插入默认规则数据
INSERT INTO `t_access_rule` (`rule_name`, `rule_type`, `rule_config`, `enabled_flag`) VALUES
('APB反潜回', 'APB', '{"enabled": true, "check_area": true}', 1),
('互锁规则', 'INTERLOCK', '{"enabled": true, "max_concurrent": 1}', 1);
