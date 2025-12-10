-- ============================================================
-- 区域配置表 - t_access_area
-- 说明: 管理门禁系统的区域结构，支持树形层级结构
-- ============================================================

USE `ioedream_access_db`;

DROP TABLE IF EXISTS `t_access_area`;

CREATE TABLE `t_access_area` (
    -- 主键
    `area_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '区域ID',
    
    -- 区域基本信息
    `area_code` VARCHAR(100) NOT NULL COMMENT '区域编码',
    `area_name` VARCHAR(200) NOT NULL COMMENT '区域名称',
    
    -- 树形结构
    `parent_id` BIGINT COMMENT '父区域ID',
    `area_type` VARCHAR(50) NOT NULL COMMENT '区域类型：BUILDING-建筑物, FLOOR-楼层, ROOM-房间',
    `area_level` INT NOT NULL COMMENT '区域层级：1-一级, 2-二级, 3-三级',
    `area_path` VARCHAR(500) COMMENT '区域路径（树形路径）',
    
    -- 安全配置
    `security_level` VARCHAR(50) DEFAULT 'MEDIUM' COMMENT '安全等级：LOW-低, MEDIUM-中, HIGH-高',
    `require_escort` TINYINT DEFAULT 0 COMMENT '是否需要陪同：0-否, 1-是',
    
    -- 容量管理
    `max_capacity` INT COMMENT '最大容量',
    `current_count` INT DEFAULT 0 COMMENT '当前人数',
    
    -- 位置信息（JSON格式）
    `location_info` JSON COMMENT '位置信息：{"longitude": 116.4074, "latitude": 39.9042}',
    
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
    PRIMARY KEY (`area_id`),
    
    -- 唯一约束
    UNIQUE KEY `uk_area_code` (`area_code`),
    
    -- 普通索引
    INDEX `idx_parent_id` (`parent_id`),
    INDEX `idx_tree_path` (`area_path`(255)),
    INDEX `idx_area_type` (`area_type`),
    INDEX `idx_enabled_flag` (`enabled_flag`, `deleted_flag`)
    
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='区域配置表';

-- 插入默认区域数据
INSERT INTO `t_access_area` (`area_id`, `area_code`, `area_name`, `parent_id`, `area_type`, `area_level`, `area_path`) VALUES
(1, 'ROOT', '根区域', NULL, 'ROOT', 1, '/1'),
(2, 'BUILDING_A', 'A栋办公楼', 1, 'BUILDING', 2, '/1/2'),
(3, 'BUILDING_A_1F', 'A栋1楼', 2, 'FLOOR', 3, '/1/2/3');
