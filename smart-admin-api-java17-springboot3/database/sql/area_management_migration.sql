-- =====================================================
-- 区域管理重构数据迁移脚本
-- 创建统一的基础区域表，支持各业务模块的区域管理需求
--
-- 版本: 1.0.0
-- 创建时间: 2025-11-24
-- 作者: SmartAdmin Team
-- =====================================================

-- 1. 创建基础区域表
DROP TABLE IF EXISTS `t_area`;

CREATE TABLE `t_area` (
  `area_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '区域ID',
  `area_code` VARCHAR(32) NOT NULL COMMENT '区域编码',
  `area_name` VARCHAR(100) NOT NULL COMMENT '区域名称',
  `area_type` INT NOT NULL COMMENT '区域类型(1:园区 2:建筑 3:楼层 4:房间 5:区域 6:其他)',
  `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '上级区域ID(0表示根区域)',
  `path` VARCHAR(500) COMMENT '区域路径(如: 园区 > 建筑 > 楼层)',
  `level` INT NOT NULL DEFAULT 1 COMMENT '层级深度(1-10)',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号(同层级排序)',
  `status` INT NOT NULL DEFAULT 1 COMMENT '状态(0:停用 1:正常 2:维护中)',
  `longitude` DECIMAL(10,7) COMMENT '经度坐标',
  `latitude` DECIMAL(10,7) COMMENT '纬度坐标',
  `area_size` DECIMAL(12,2) COMMENT '区域面积(平方米)',
  `capacity` INT COMMENT '容纳人数',
  `description` VARCHAR(500) COMMENT '区域描述',
  `map_image` VARCHAR(255) COMMENT '区域平面图路径',
  `remark` VARCHAR(1000) COMMENT '备注信息',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_user_id` BIGINT COMMENT '创建人ID',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_user_id` BIGINT COMMENT '更新人ID',
  `deleted_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标识(0:未删除 1:已删除)',
  `version` INT NOT NULL DEFAULT 1 COMMENT '版本号(乐观锁)',
  PRIMARY KEY (`area_id`),
  UNIQUE KEY `uk_area_code` (`area_code`, `deleted_flag`),
  KEY `idx_parent_id` (`parent_id`, `deleted_flag`),
  KEY `idx_area_type` (`area_type`, `deleted_flag`),
  KEY `idx_status` (`status`, `deleted_flag`),
  KEY `idx_level` (`level`, `deleted_flag`),
  KEY `idx_sort_order` (`sort_order`, `deleted_flag`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_update_time` (`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='基础区域表';

-- 2. 插入默认的区域类型数据
INSERT INTO `t_area` (`area_code`, `area_name`, `area_type`, `parent_id`, `level`, `sort_order`, `status`, `description`, `create_user_id`, `update_user_id`) VALUES
('ROOT', '根区域', 6, 0, 1, 0, 1, '系统根区域，不可删除', 1, 1),
('DEMO_CAMPUS', '示例园区', 1, 1, 2, 1, 1, '演示用的园区数据', 1, 1),
('DEMO_BUILDING_A', 'A栋', 2, 2, 3, 1, 1, '演示用的A栋建筑', 1, 1),
('DEMO_BUILDING_B', 'B栋', 2, 2, 3, 2, 1, '演示用的B栋建筑', 1, 1),
('DEMO_FLOOR_A1', 'A栋1楼', 3, 3, 4, 1, 1, '演示用的A栋1楼', 1, 1),
('DEMO_FLOOR_A2', 'A栋2楼', 3, 3, 4, 2, 1, '演示用的A栋2楼', 1, 1),
('DEMO_FLOOR_B1', 'B栋1楼', 3, 4, 4, 1, 1, '演示用的B栋1楼', 1, 1),
('DEMO_ROOM_A101', 'A栋101室', 4, 5, 5, 1, 1, '演示用的房间', 1, 1),
('DEMO_ROOM_A102', 'A栋102室', 4, 5, 5, 2, 1, '演示用的房间', 1, 1),
('DEMO_ROOM_A201', 'A栋201室', 4, 6, 5, 1, 1, '演示用的房间', 1, 1),
('DEMO_ROOM_B101', 'B栋101室', 4, 7, 5, 1, 1, '演示用的房间', 1, 1),
('DEMO_AREA_ENTRANCE', '入口区域', 5, 2, 3, 10, 1, '园区入口区域', 1, 1),
('DEMO_AREA_PARKING', '停车场', 5, 2, 3, 11, 1, '园区停车场', 1, 1);

-- 3. 更新区域路径信息
UPDATE `t_area` SET `path` = `area_name` WHERE `parent_id` = 1;
UPDATE `t_area` SET `path` = (SELECT `path` FROM (SELECT `path` FROM `t_area` WHERE `area_id` = 2) t) || ' > ' || `area_name` WHERE `parent_id` = 2;
UPDATE `t_area` SET `path` = (SELECT `path` FROM (SELECT `path` FROM `t_area` WHERE `area_id` = 3) t) || ' > ' || `area_name` WHERE `parent_id` IN (3, 4);
UPDATE `t_area` SET `path` = (SELECT `path` FROM (SELECT `path` FROM `t_area` WHERE `area_id` = 5) t) || ' > ' || `area_name` WHERE `parent_id` = 5;
UPDATE `t_area` SET `path` = (SELECT `path` FROM (SELECT `path` FROM `t_area` WHERE `area_id` = 6) t) || ' > ' || `area_name` WHERE `parent_id` = 6;
UPDATE `t_area` SET `path` = (SELECT `path` FROM (SELECT `path` FROM `t_area` WHERE `area_id` = 7) t) || ' > ' || `area_name` WHERE `parent_id` = 7;

-- 4. 创建门禁区域扩展表
DROP TABLE IF EXISTS `t_access_area_ext`;

CREATE TABLE `t_access_area_ext` (
  `ext_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '扩展ID',
  `area_id` BIGINT NOT NULL COMMENT '基础区域ID',
  `access_level` INT NOT NULL DEFAULT 1 COMMENT '门禁级别(1:普通 2:重要 3:核心)',
  `access_mode` VARCHAR(50) COMMENT '门禁模式(卡/指纹/人脸/二维码)',
  `device_count` INT NOT NULL DEFAULT 0 COMMENT '关联设备数量',
  `guard_required` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否需要安保人员(0:否 1:是)',
  `time_restrictions` TEXT COMMENT '时间限制配置(JSON格式)',
  `visitor_allowed` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否允许访客(0:否 1:是)',
  `emergency_access` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否为紧急通道(0:否 1:是)',
  `monitoring_enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用监控(0:否 1:是)',
  `alert_config` TEXT COMMENT '告警配置(JSON格式)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_user_id` BIGINT COMMENT '创建人ID',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_user_id` BIGINT COMMENT '更新人ID',
  `deleted_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标识(0:未删除 1:已删除)',
  `version` INT NOT NULL DEFAULT 1 COMMENT '版本号(乐观锁)',
  PRIMARY KEY (`ext_id`),
  UNIQUE KEY `uk_area_id` (`area_id`, `deleted_flag`),
  KEY `idx_access_level` (`access_level`, `deleted_flag`),
  KEY `idx_device_count` (`device_count`, `deleted_flag`),
  KEY `idx_guard_required` (`guard_required`, `deleted_flag`),
  KEY `idx_visitor_allowed` (`visitor_allowed`, `deleted_flag`),
  KEY `idx_emergency_access` (`emergency_access`, `deleted_flag`),
  KEY `idx_monitoring_enabled` (`monitoring_enabled`, `deleted_flag`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_update_time` (`update_time`),
  CONSTRAINT `fk_access_area_ext_area_id` FOREIGN KEY (`area_id`) REFERENCES `t_area` (`area_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门禁区域扩展表';

-- 5. 创建消费区域扩展表
DROP TABLE IF EXISTS `t_consume_area_ext`;

CREATE TABLE `t_consume_area_ext` (
  `ext_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '扩展ID',
  `area_id` BIGINT NOT NULL COMMENT '基础区域ID',
  `business_type` VARCHAR(50) NOT NULL COMMENT '业务类型(餐厅/超市/咖啡厅/便利店等)',
  `device_count` INT NOT NULL DEFAULT 0 COMMENT '关联设备数量',
  `opening_hours` VARCHAR(200) COMMENT '营业时间',
  `max_capacity` INT COMMENT '最大容纳人数',
  `avg_consumption` DECIMAL(10,2) COMMENT '平均消费金额',
  `peak_hours` VARCHAR(200) COMMENT '高峰时段',
  `service_items` TEXT COMMENT '服务项目(JSON格式)',
  `discount_enabled` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否启用折扣(0:否 1:是)',
  `discount_config` TEXT COMMENT '折扣配置(JSON格式)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_user_id` BIGINT COMMENT '创建人ID',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_user_id` BIGINT COMMENT '更新人ID',
  `deleted_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标识(0:未删除 1:已删除)',
  `version` INT NOT NULL DEFAULT 1 COMMENT '版本号(乐观锁)',
  PRIMARY KEY (`ext_id`),
  UNIQUE KEY `uk_area_id` (`area_id`, `deleted_flag`),
  KEY `idx_business_type` (`business_type`, `deleted_flag`),
  KEY `idx_device_count` (`device_count`, `deleted_flag`),
  KEY `idx_discount_enabled` (`discount_enabled`, `deleted_flag`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_update_time` (`update_time`),
  CONSTRAINT `fk_consume_area_ext_area_id` FOREIGN KEY (`area_id`) REFERENCES `t_area` (`area_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消费区域扩展表';

-- 6. 创建考勤区域扩展表
DROP TABLE IF EXISTS `t_attendance_area_ext`;

CREATE TABLE `t_attendance_area_ext` (
  `ext_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '扩展ID',
  `area_id` BIGINT NOT NULL COMMENT '基础区域ID',
  `attendance_type` VARCHAR(50) NOT NULL COMMENT '考勤类型(入口/出口/工作区/休息区等)',
  `device_count` INT NOT NULL DEFAULT 0 COMMENT '关联设备数量',
  `check_required` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否必须考勤(0:否 1:是)',
  `working_hours` VARCHAR(200) COMMENT '工作时间',
  `grace_period` INT COMMENT '宽限时间(分钟)',
  `auto_check` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否自动考勤(0:否 1:是)',
  `location_validation` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用位置验证(0:否 1:是)',
  `attendance_rules` TEXT COMMENT '考勤规则(JSON格式)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_user_id` BIGINT COMMENT '创建人ID',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_user_id` BIGINT COMMENT '更新人ID',
  `deleted_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标识(0:未删除 1:已删除)',
  `version` INT NOT NULL DEFAULT 1 COMMENT '版本号(乐观锁)',
  PRIMARY KEY (`ext_id`),
  UNIQUE KEY `uk_area_id` (`area_id`, `deleted_flag`),
  KEY `idx_attendance_type` (`attendance_type`, `deleted_flag`),
  KEY `idx_device_count` (`device_count`, `deleted_flag`),
  KEY `idx_check_required` (`check_required`, `deleted_flag`),
  KEY `idx_auto_check` (`auto_check`, `deleted_flag`),
  KEY `idx_location_validation` (`location_validation`, `deleted_flag`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_update_time` (`update_time`),
  CONSTRAINT `fk_attendance_area_ext_area_id` FOREIGN KEY (`area_id`) REFERENCES `t_area` (`area_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤区域扩展表';

-- 7. 创建视频监控区域扩展表
DROP TABLE IF EXISTS `t_video_area_ext`;

CREATE TABLE `t_video_area_ext` (
  `ext_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '扩展ID',
  `area_id` BIGINT NOT NULL COMMENT '基础区域ID',
  `monitor_type` VARCHAR(50) NOT NULL COMMENT '监控类型(普通/重点/禁区/公共区域等)',
  `device_count` INT NOT NULL DEFAULT 0 COMMENT '关联设备数量',
  `recording_enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用录像(0:否 1:是)',
  `recording_days` INT NOT NULL DEFAULT 7 COMMENT '录像保存天数',
  `resolution` VARCHAR(20) COMMENT '视频分辨率(1080P/4K等)',
  `night_vision` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否启用夜视(0:否 1:是)',
  `motion_detection` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用移动侦测(0:否 1:是)',
  `alert_config` TEXT COMMENT '告警配置(JSON格式)',
  `coverage_radius` DECIMAL(8,2) COMMENT '覆盖半径(米)',
  `blind_spots` TEXT COMMENT '监控盲点(JSON格式)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_user_id` BIGINT COMMENT '创建人ID',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_user_id` BIGINT COMMENT '更新人ID',
  `deleted_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标识(0:未删除 1:已删除)',
  `version` INT NOT NULL DEFAULT 1 COMMENT '版本号(乐观锁)',
  PRIMARY KEY (`ext_id`),
  UNIQUE KEY `uk_area_id` (`area_id`, `deleted_flag`),
  KEY `idx_monitor_type` (`monitor_type`, `deleted_flag`),
  KEY `idx_device_count` (`device_count`, `deleted_flag`),
  KEY `idx_recording_enabled` (`recording_enabled`, `deleted_flag`),
  KEY `idx_night_vision` (`night_vision`, `deleted_flag`),
  KEY `idx_motion_detection` (`motion_detection`, `deleted_flag`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_update_time` (`update_time`),
  CONSTRAINT `fk_video_area_ext_area_id` FOREIGN KEY (`area_id`) REFERENCES `t_area` (`area_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频监控区域扩展表';

-- 8. 插入演示用的扩展数据
-- 门禁区域扩展数据
INSERT INTO `t_access_area_ext` (`area_id`, `access_level`, `access_mode`, `device_count`, `guard_required`, `visitor_allowed`, `emergency_access`, `monitoring_enabled`) VALUES
(8, 1, '卡,指纹,人脸', 2, 0, 1, 0, 1),  -- A栋101室
(9, 1, '卡,指纹', 2, 0, 1, 0, 1),        -- A栋102室
(10, 2, '卡,指纹,人脸,二维码', 3, 1, 0, 0, 1),  -- A栋201室
(11, 1, '卡,二维码', 2, 0, 1, 0, 1),     -- B栋101室
(12, 3, '卡,指纹,人脸,掌纹,虹膜', 5, 1, 0, 1, 1); -- B栋101室(核心区域)

-- 消费区域扩展数据
INSERT INTO `t_consume_area_ext` (`area_id`, `business_type`, `device_count`, `opening_hours`, `max_capacity`, `avg_consumption`, `discount_enabled`) VALUES
(8, '餐厅', 1, '08:00-20:00', 50, 15.00, 1),  -- A栋101室 餐厅
(9, '便利店', 1, '07:00-22:00', 20, 8.50, 1),  -- A栋102室 便利店
(11, '咖啡厅', 1, '09:00-18:00', 30, 25.00, 1); -- B栋101室 咖啡厅

-- 考勤区域扩展数据
INSERT INTO `t_attendance_area_ext` (`area_id`, `attendance_type`, `device_count`, `check_required`, `working_hours`, `grace_period`, `auto_check`) VALUES
(13, '入口', 2, 1, '08:30-17:30', 10, 1),  -- 入口区域
(12, '工作区', 1, 1, '09:00-18:00', 15, 0); -- B栋101室 工作区

-- 视频监控区域扩展数据
INSERT INTO `t_video_area_ext` (`area_id`, `monitor_type`, `device_count`, `recording_enabled`, `recording_days`, `resolution`, `night_vision`, `motion_detection`) VALUES
(13, '公共区域', 4, 1, 7, '1080P', 0, 1),  -- 入口区域
(12, '重点区域', 3, 1, 15, '4K', 1, 1),   -- B栋101室 重点区域
(14, '停车场', 6, 1, 3, '1080P', 1, 1);  -- 停车场

-- 9. 创建索引优化查询性能
-- 为递归查询创建优化索引
ALTER TABLE `t_area` ADD INDEX `idx_parent_deleted` (`parent_id`, `deleted_flag`, `area_id`);
ALTER TABLE `t_area` ADD INDEX `idx_level_deleted` (`level`, `deleted_flag`, `sort_order`);
ALTER TABLE `t_area` ADD INDEX `idx_type_status_deleted` (`area_type`, `status`, `deleted_flag`);

-- 10. 创建视图便于查询
-- 创建区域层级视图
CREATE OR REPLACE VIEW `v_area_hierarchy` AS
WITH RECURSIVE area_tree AS (
    SELECT
        area_id,
        area_code,
        area_name,
        area_type,
        parent_id,
        level,
        sort_order,
        status,
        path,
        CAST(area_name AS CHAR(1000)) as full_path,
        CAST(area_id AS CHAR(1000)) as path_ids
    FROM t_area
    WHERE parent_id = 1 AND deleted_flag = 0

    UNION ALL

    SELECT
        a.area_id,
        a.area_code,
        a.area_name,
        a.area_type,
        a.parent_id,
        a.level,
        a.sort_order,
        a.status,
        a.path,
        CONCAT(at.full_path, ' > ', a.area_name) as full_path,
        CONCAT(at.path_ids, ',', a.area_id) as path_ids
    FROM t_area a
    INNER JOIN area_tree at ON a.parent_id = at.area_id
    WHERE a.deleted_flag = 0
)
SELECT
    area_id,
    area_code,
    area_name,
    area_type,
    parent_id,
    level,
    sort_order,
    status,
    path,
    full_path,
    path_ids
FROM area_tree
ORDER BY level, sort_order, area_id;

-- 创建区域统计视图
CREATE OR REPLACE VIEW `v_area_statistics` AS
SELECT
    COUNT(*) as total_count,
    SUM(CASE WHEN area_type = 1 THEN 1 ELSE 0 END) as campus_count,
    SUM(CASE WHEN area_type = 2 THEN 1 ELSE 0 END) as building_count,
    SUM(CASE WHEN area_type = 3 THEN 1 ELSE 0 END) as floor_count,
    SUM(CASE WHEN area_type = 4 THEN 1 ELSE 0 END) as room_count,
    SUM(CASE WHEN area_type = 5 THEN 1 ELSE 0 END) as area_count,
    SUM(CASE WHEN area_type = 6 THEN 1 ELSE 0 END) as other_count,
    SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as enabled_count,
    SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) as disabled_count,
    SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) as maintenance_count,
    MAX(level) as max_level,
    COUNT(CASE WHEN parent_id = 1 THEN 1 END) as root_count
FROM t_area
WHERE deleted_flag = 0;

-- 11. 数据迁移完成，添加版本记录
CREATE TABLE IF NOT EXISTS `migration_version` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `version` VARCHAR(50) NOT NULL,
  `description` TEXT,
  `executed_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `migration_version` (`version`, `description`) VALUES
('1.0.0', '创建统一区域管理表结构，支持各业务模块的区域管理需求');

-- 12. 性能优化提示
/*
性能优化建议：
1. 对于大量数据的区域树查询，建议使用分页查询
2. 频繁查询的区域数据建议使用缓存
3. 定期分析查询性能，优化慢查询
4. 对于复杂的层级统计，可考虑使用物化视图
5. 在高并发场景下，考虑使用读写分离
*/

-- 13. 使用说明
/*
使用说明：
1. t_area: 基础区域表，存储所有区域的通用信息
2. t_*_area_ext: 各业务模块的扩展表，存储业务特有的字段
3. v_area_hierarchy: 区域层级视图，便于查询完整的层级关系
4. v_area_statistics: 区域统计视图，便于获取统计数据
5. 通过area_id关联基础表和扩展表
6. 删除区域时，扩展表会通过外键级联删除
7. 建议在应用层实现区域缓存机制，提升查询性能
*/

SELECT '区域管理数据迁移脚本执行完成' as message;