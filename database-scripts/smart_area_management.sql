-- 区域管理数据库表结构
-- SmartAdmin v3 - 区域管理模块
-- 创建时间: 2025-11-13

-- 区域基础信息表
CREATE TABLE IF NOT EXISTS `t_area` (
  `area_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '区域ID',
  `area_code` VARCHAR(50) NOT NULL COMMENT '区域编码',
  `area_name` VARCHAR(100) NOT NULL COMMENT '区域名称',
  `area_type` VARCHAR(20) NOT NULL DEFAULT 'REGION' COMMENT '区域类型：REGION-大区 PROVINCE-省 CITY-市 DISTRICT-区 BUILDING-楼 FLOOR-层 ROOM-房间',
  `parent_area_id` BIGINT COMMENT '父区域ID',
  `area_level` TINYINT NOT NULL DEFAULT 1 COMMENT '区域层级：1-大区 2-省 3-市 4-区 5-楼 6-层 7-房间',
  `area_path` VARCHAR(500) COMMENT '区域路径（用逗号分隔的ID序列）',
  `description` TEXT COMMENT '区域描述',
  `address` VARCHAR(255) COMMENT '详细地址',
  `center_latitude` DECIMAL(10, 6) COMMENT '中心纬度',
  `center_longitude` DECIMAL(10, 6) COMMENT '中心经度',
  `area_bounds` POLYGON COMMENT '区域边界坐标（多边形）',
  `area_radius` INT COMMENT '区域半径（米，用于圆形区域）',
  `manager_id` BIGINT COMMENT '区域管理员ID',
  `contact_person` VARCHAR(50) COMMENT '联系人',
  `contact_phone` VARCHAR(20) COMMENT '联系电话',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-启用 INACTIVE-停用 DELETED-删除',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序序号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user_id` BIGINT NOT NULL COMMENT '创建人ID',
  `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
  `extended_properties` JSON COMMENT '扩展属性（JSON格式）',
  PRIMARY KEY (`area_id`),
  UNIQUE KEY `uk_area_code` (`area_code`),
  KEY `idx_parent_area_id` (`parent_area_id`),
  KEY `idx_area_type` (`area_type`),
  KEY `idx_area_level` (`area_level`),
  KEY `idx_status` (`status`),
  KEY `idx_deleted_flag` (`deleted_flag`),
  KEY `idx_sort_order` (`sort_order`),
  KEY `idx_create_time` (`create_time`),
  SPATIAL INDEX `idx_area_bounds` (`area_bounds`),
  SPATIAL INDEX `idx_center_point` (`center_longitude`, `center_latitude`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='区域基础信息表';

-- 区域设备关联表
CREATE TABLE IF NOT EXISTS `t_area_device` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `area_id` BIGINT NOT NULL COMMENT '区域ID',
  `device_id` BIGINT NOT NULL COMMENT '设备ID',
  `device_type` VARCHAR(50) NOT NULL COMMENT '设备类型',
  `assign_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '分配时间',
  `assign_user_id` BIGINT COMMENT '分配人ID',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-正常 INACTIVE-异常 MAINTENANCE-维护',
  `remark` VARCHAR(255) COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_area_device` (`area_id`, `device_id`),
  KEY `idx_area_id` (`area_id`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_device_type` (`device_type`),
  KEY `idx_status` (`status`),
  KEY `idx_assign_time` (`assign_time`),
  KEY `idx_deleted_flag` (`deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='区域设备关联表';

-- 区域人员关联表
CREATE TABLE IF NOT EXISTS `t_area_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `area_id` BIGINT NOT NULL COMMENT '区域ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `user_name` VARCHAR(50) COMMENT '用户姓名',
  `role_type` VARCHAR(20) NOT NULL DEFAULT 'STAFF' COMMENT '角色类型：STAFF-员工 MANAGER-管理员 SECURITY-安保 VISITOR-访客',
  `access_level` TINYINT NOT NULL DEFAULT 1 COMMENT '访问级别：1-普通 2-重要 3-关键',
  `assign_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '分配时间',
  `assign_user_id` BIGINT COMMENT '分配人ID',
  `expire_time` DATETIME COMMENT '过期时间（NULL表示永不过期）',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-有效 INACTIVE-无效 EXPIRED-过期',
  `remark` VARCHAR(255) COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_area_user` (`area_id`, `user_id`),
  KEY `idx_area_id` (`area_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role_type` (`role_type`),
  KEY `idx_access_level` (`access_level`),
  KEY `idx_status` (`status`),
  KEY `idx_assign_time` (`assign_time`),
  KEY `idx_expire_time` (`expire_time`),
  KEY `idx_deleted_flag` (`deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='区域人员关联表';

-- 区域权限配置表
CREATE TABLE IF NOT EXISTS `t_area_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `area_id` BIGINT NOT NULL COMMENT '区域ID',
  `permission_type` VARCHAR(50) NOT NULL COMMENT '权限类型：ACCESS-访问 MANAGE-管理 CONFIG-配置 MONITOR-监控',
  `permission_code` VARCHAR(100) NOT NULL COMMENT '权限代码',
  `permission_name` VARCHAR(100) NOT NULL COMMENT '权限名称',
  `resource_type` VARCHAR(50) COMMENT '资源类型：DEVICE-设备 USER-人员 DOOR-门禁 CAMERA-摄像头',
  `resource_id` BIGINT COMMENT '资源ID（根据资源类型对应到具体表ID）',
  `permission_level` TINYINT NOT NULL DEFAULT 1 COMMENT '权限级别：1-查看 2-操作 3-管理',
  `allow_access` BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否允许访问',
  `time_restriction` JSON COMMENT '时间限制（JSON格式）',
  `ip_restriction` JSON COMMENT 'IP限制（JSON格式）',
  `description` TEXT COMMENT '权限描述',
  `grant_user_id` BIGINT COMMENT '授权人ID',
  `grant_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '授权时间',
  `expire_time` DATETIME COMMENT '过期时间（NULL表示永不过期）',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-有效 INACTIVE-无效 EXPIRED-过期',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_area_permission` (`area_id`, `permission_code`, `resource_type`, `resource_id`),
  KEY `idx_area_id` (`area_id`),
  KEY `idx_permission_type` (`permission_type`),
  KEY `idx_permission_code` (`permission_code`),
  KEY `idx_resource_type` (`resource_type`),
  KEY `idx_resource_id` (`resource_id`),
  KEY `idx_permission_level` (`permission_level`),
  KEY `idx_status` (`status`),
  KEY `idx_grant_time` (`grant_time`),
  KEY `idx_expire_time` (`expire_time`),
  KEY `idx_deleted_flag` (`deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='区域权限配置表';

-- 区域访问记录表
CREATE TABLE IF NOT EXISTS `t_area_access_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `area_id` BIGINT NOT NULL COMMENT '区域ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `access_type` VARCHAR(20) NOT NULL COMMENT '访问类型：ENTER-进入 EXIT-离开 ATTEMPT-尝试 DENY-拒绝',
  `access_method` VARCHAR(20) NOT NULL COMMENT '访问方式：CARD-卡 FACE-人脸 FINGERPRINT-指纹 PASSWORD-密码 QR-二维码',
  `access_point` VARCHAR(100) COMMENT '访问点',
  `device_id` BIGINT COMMENT '设备ID',
  `access_time` DATETIME NOT NULL COMMENT '访问时间',
  `access_result` VARCHAR(20) COMMENT '访问结果：SUCCESS-成功 FAILED-失败 PENDING-待处理',
  `failure_reason` VARCHAR(255) COMMENT '失败原因',
  `client_ip` VARCHAR(45) COMMENT '客户端IP',
  `user_agent` VARCHAR(255) COMMENT '用户代理',
  `location_info` JSON COMMENT '位置信息（JSON格式）',
  `verify_data` JSON COMMENT '验证数据（JSON格式）',
  `session_id` VARCHAR(100) COMMENT '会话ID',
  `remark` VARCHAR(255) COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_area_id` (`area_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_access_type` (`access_type`),
  KEY `idx_access_method` (`access_method`),
  KEY `idx_access_time` (`access_time`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_access_result` (`access_result`),
  KEY `idx_client_ip` (`client_ip`),
  KEY `idx_session_id` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='区域访问记录表';

-- 区域统计信息表
CREATE TABLE IF NOT EXISTS `t_area_statistics` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `area_id` BIGINT NOT NULL COMMENT '区域ID',
  `stat_date` DATE NOT NULL COMMENT '统计日期',
  `stat_type` VARCHAR(20) NOT NULL COMMENT '统计类型：DAILY-日 WEEKLY-周 MONTHLY-月 YEARLY-年',
  `total_visits` BIGINT NOT NULL DEFAULT 0 COMMENT '总访问次数',
  `unique_visitors` BIGINT NOT NULL DEFAULT 0 COMMENT '唯一访客数',
  `total_users` BIGINT NOT NULL DEFAULT 0 COMMENT '总用户数',
  `active_users` BIGINT NOT NULL DEFAULT 0 COMMENT '活跃用户数',
  `device_count` INT NOT NULL DEFAULT 0 COMMENT '设备数量',
  `online_devices` INT NOT NULL DEFAULT 0 COMMENT '在线设备数',
  `alert_count` INT NOT NULL DEFAULT 0 COMMENT '告警数量',
  `average_stay_time` INT COMMENT '平均停留时间（分钟）',
  `peak_hour` VARCHAR(5) COMMENT '高峰时段（HH:mm）',
  `peak_visits` INT NOT NULL DEFAULT 0 COMMENT '高峰访问数',
  `stat_details` JSON COMMENT '详细统计数据（JSON格式）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_area_stat` (`area_id`, `stat_date`, `stat_type`),
  KEY `idx_area_id` (`area_id`),
  KEY `idx_stat_date` (`stat_date`),
  KEY `idx_stat_type` (`stat_type`),
  KEY `idx_total_visits` (`total_visits`),
  KEY `idx_unique_visitors` (`unique_visitors`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='区域统计信息表';

-- 区域配置表
CREATE TABLE IF NOT EXISTS `t_area_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `area_id` BIGINT NOT NULL COMMENT '区域ID',
  `config_type` VARCHAR(50) NOT NULL COMMENT '配置类型：ACCESS_CONTROL-访问控制 ALARM-告警 SCHEDULE-排班 DISPLAY-显示 NOTIFICATION-通知',
  `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
  `config_value` TEXT NOT NULL COMMENT '配置值',
  `config_desc` VARCHAR(255) COMMENT '配置描述',
  `is_enabled` BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
  `config_group` VARCHAR(50) COMMENT '配置分组',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序序号',
  `validation_rule` JSON COMMENT '验证规则（JSON格式）',
  `default_value` TEXT COMMENT '默认值',
  `create_user_id` BIGINT COMMENT '创建人ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_area_config` (`area_id`, `config_type`, `config_key`),
  KEY `idx_area_id` (`area_id`),
  KEY `idx_config_type` (`config_type`),
  KEY `idx_config_key` (`config_key`),
  KEY `idx_is_enabled` (`is_enabled`),
  KEY `idx_config_group` (`config_group`),
  KEY `idx_sort_order` (`sort_order`),
  KEY `idx_deleted_flag` (`deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='区域配置表';

-- 区域历史变更表
CREATE TABLE IF NOT EXISTS `t_area_history` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `area_id` BIGINT NOT NULL COMMENT '区域ID',
  `operation_type` VARCHAR(20) NOT NULL COMMENT '操作类型：CREATE-创建 UPDATE-更新 DELETE-删除 STATUS_CHANGE-状态变化',
  `operation_desc` VARCHAR(255) COMMENT '操作描述',
  `old_values` JSON COMMENT '变更前值（JSON格式）',
  `new_values` JSON COMMENT '变更后值（JSON格式）',
  `change_fields` JSON COMMENT '变更字段列表（JSON格式）',
  `operator_id` BIGINT COMMENT '操作人ID',
  `operator_name` VARCHAR(50) COMMENT '操作人姓名',
  `operation_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `client_ip` VARCHAR(45) COMMENT '客户端IP',
  `user_agent` VARCHAR(255) COMMENT '用户代理',
  `remark` TEXT COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_area_id` (`area_id`),
  KEY `idx_operation_type` (`operation_type`),
  KEY `idx_operator_id` (`operator_id`),
  KEY `idx_operation_time` (`operation_time`),
  KEY `idx_client_ip` (`client_ip`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='区域历史变更表';

-- 添加外键约束（可选，根据需要启用）
-- ALTER TABLE `t_area` ADD CONSTRAINT `fk_area_parent` FOREIGN KEY (`parent_area_id`) REFERENCES `t_area` (`area_id`) ON DELETE SET NULL;
-- ALTER TABLE `t_area` ADD CONSTRAINT `fk_area_manager` FOREIGN KEY (`manager_id`) REFERENCES `t_employee` (`employee_id`) ON DELETE SET NULL;
-- ALTER TABLE `t_area_device` ADD CONSTRAINT `fk_area_device_area` FOREIGN KEY (`area_id`) REFERENCES `t_area` (`area_id`) ON DELETE CASCADE;
-- ALTER TABLE `t_area_device` ADD CONSTRAINT `fk_area_device_device` FOREIGN KEY (`device_id`) REFERENCES `t_device` (`device_id`) ON DELETE CASCADE;
-- ALTER TABLE `t_area_user` ADD CONSTRAINT `fk_area_user_area` FOREIGN KEY (`area_id`) REFERENCES `t_area` (`area_id`) ON DELETE CASCADE;
-- ALTER TABLE `t_area_user` ADD CONSTRAINT `fk_area_user_user` FOREIGN KEY (`user_id`) REFERENCES `t_user` (`user_id`) ON DELETE CASCADE;
-- ALTER TABLE `t_area_permission` ADD CONSTRAINT `fk_area_permission_area` FOREIGN KEY (`area_id`) REFERENCES `t_area` (`area_id`) ON DELETE CASCADE;
-- ALTER TABLE `t_area_access_log` ADD CONSTRAINT `fk_area_log_area` FOREIGN KEY (`area_id`) REFERENCES `t_area` (`area_id`) ON DELETE CASCADE;
-- ALTER TABLE `t_area_access_log` ADD CONSTRAINT `fk_area_log_user` FOREIGN KEY (`user_id`) REFERENCES `t_user` (`user_id`) ON DELETE CASCADE;
-- ALTER TABLE `t_area_statistics` ADD CONSTRAINT `fk_area_stats_area` FOREIGN KEY (`area_id`) REFERENCES `t_area` (`area_id`) ON DELETE CASCADE;
-- ALTER TABLE `t_area_config` ADD CONSTRAINT `fk_area_config_area` FOREIGN KEY (`area_id`) REFERENCES `t_area` (`area_id`) ON DELETE CASCADE;
-- ALTER TABLE `t_area_history` ADD CONSTRAINT `fk_area_history_area` FOREIGN KEY (`area_id`) REFERENCES `t_area` (`area_id`) ON DELETE CASCADE;

-- 插入初始数据
INSERT INTO `t_area` (`area_code`, `area_name`, `area_type`, `area_level`, `area_path`, `description`, `status`, `create_user_id`) VALUES
('ROOT', '根区域', 'ROOT', 0, '0', '系统根区域', 'ACTIVE', 1),
('CHINA', '中国', 'REGION', 1, '0,1', '中华人民共和国', 'ACTIVE', 1),
('BEIJING', '北京市', 'PROVINCE', 2, '0,1,2', '中华人民共和国首都', 'ACTIVE', 1),
('SHANGHAI', '上海市', 'PROVINCE', 2, '0,1,3', '中华人民共和国直辖市', 'ACTIVE', 1);

-- 创建分区表（如果数据量大，可以考虑按时间分区）
-- ALTER TABLE `t_area_access_log` PARTITION BY RANGE (TO_DAYS(access_time)) (
--   PARTITION p_2024 VALUES LESS THAN (TO_DAYS('2025-01-01')),
--   PARTITION p_2025 VALUES LESS THAN (TO_DAYS('2026-01-01')),
--   PARTITION p_future VALUES LESS THAN MAXVALUE
-- );

-- 创建触发器（自动更新区域路径）
DELIMITER //
CREATE TRIGGER `tr_area_path_update`
BEFORE INSERT ON `t_area`
FOR EACH ROW
BEGIN
    DECLARE v_parent_path VARCHAR(500);

    IF NEW.parent_area_id IS NOT NULL AND NEW.parent_area_id > 0 THEN
        SELECT area_path INTO v_parent_path FROM t_area WHERE area_id = NEW.parent_area_id;
        SET NEW.area_path = CONCAT(IFNULL(v_parent_path, ''), ',', NEW.parent_area_id);
    ELSE
        SET NEW.area_path = CONCAT('0,', NEW.area_id);
    END IF;

    SET NEW.sort_order = IFNULL(NEW.sort_order, NEW.area_id);
END//
DELIMITER ;

-- 插入示例数据
INSERT INTO `t_area_config` (`area_id`, `config_type`, `config_key`, `config_value`, `config_desc`, `is_enabled`) VALUES
(1, 'ACCESS_CONTROL', 'max_visitors_per_day', '100', '每日最大访客数限制', TRUE),
(1, 'ALARM', 'unauthorized_access_alert', 'true', '未授权访问告警', TRUE),
(1, 'NOTIFICATION', 'access_notification_enabled', 'true', '访问通知开关', TRUE);

-- 创建视图（方便查询）
CREATE OR REPLACE VIEW `v_area_tree` AS
SELECT
    a1.area_id,
    a1.area_code,
    a1.area_name,
    a1.area_type,
    a1.area_level,
    a1.parent_area_id,
    a1.description,
    a1.center_latitude,
    a1.center_longitude,
    a1.manager_id,
    a1.status,
    a1.sort_order,
    a2.area_name AS parent_area_name,
    (SELECT COUNT(*) FROM t_area a3 WHERE a3.parent_area_id = a1.area_id AND a3.deleted_flag = 0) AS child_count
FROM t_area a1
LEFT JOIN t_area a2 ON a1.parent_area_id = a2.area_id
WHERE a1.deleted_flag = 0
ORDER BY a1.area_path, a1.sort_order;

-- 创建存储过程（区域树递归查询）
DELIMITER //
CREATE PROCEDURE `sp_get_area_tree`(
    IN p_parent_id BIGINT,
    IN p_max_depth INT
)
BEGIN
    DECLARE v_current_depth INT DEFAULT 0;

    -- 临时表存储区域树
    DROP TEMPORARY TABLE IF EXISTS temp_area_tree;
    CREATE TEMPORARY TABLE temp_area_tree (
        area_id BIGINT,
        area_code VARCHAR(50),
        area_name VARCHAR(100),
        area_type VARCHAR(20),
        area_level INT,
        parent_area_id BIGINT,
        sort_order INT,
        depth INT,
        path VARCHAR(1000)
    );

    -- 插入根节点
    INSERT INTO temp_area_tree
    SELECT
        area_id, area_code, area_name, area_type, area_level,
        parent_area_id, sort_order, 0,
        CONCAT(area_name, ' (', area_id, ')')
    FROM t_area
    WHERE (p_parent_id IS NULL AND parent_area_id IS NULL)
       OR (p_parent_id IS NOT NULL AND parent_area_id = p_parent_id)
    AND deleted_flag = 0;

    -- 递归插入子节点
    SET v_current_depth = 1;
    WHILE v_current_depth <= p_max_depth DO
        INSERT INTO temp_area_tree
        SELECT
            a.area_id, a.area_code, a.area_name, a.area_type, a.area_level,
            a.parent_area_id, a.sort_order, v_current_depth,
            CONCAT(t.path, ' -> ', a.area_name, ' (', a.area_id, ')')
        FROM t_area a
        JOIN temp_area_tree t ON a.parent_area_id = t.area_id
        WHERE t.depth = v_current_depth - 1
        AND a.deleted_flag = 0;

        SET v_current_depth = v_current_depth + 1;

        IF ROW_COUNT() = 0 THEN
            LEAVE;
        END IF;
    END WHILE;

    -- 返回结果
    SELECT * FROM temp_area_tree ORDER BY path;
END//
DELIMITER ;

-- 设置数据库字符集
ALTER DATABASE `smart_admin_v3` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;