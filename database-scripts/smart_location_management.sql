-- =============================================
-- SmartAdmin 地理位置管理模块数据库表结构
-- 创建时间: 2025-11-13
-- 版本: v1.0.0
-- 描述: 实现GPS定位、地理围栏、位置验证等功能
-- =============================================

-- 1. 位置基础信息表
CREATE TABLE `t_location` (
    `location_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '位置ID',
    `location_code` VARCHAR(64) NOT NULL COMMENT '位置编码',
    `location_name` VARCHAR(256) NOT NULL COMMENT '位置名称',
    `location_type` VARCHAR(32) NOT NULL COMMENT '位置类型：BUILDING-建筑，AREA-区域，ROOM-房间，POINT-点位',
    `parent_location_id` BIGINT DEFAULT 0 COMMENT '父位置ID',
    `location_level` INT NOT NULL DEFAULT 1 COMMENT '位置层级',
    `location_path` VARCHAR(1000) COMMENT '位置路径，如：/根节点/子节点',
    `area_id` BIGINT COMMENT '所属区域ID',
    `area_name` VARCHAR(128) COMMENT '所属区域名称',
    `latitude` DECIMAL(10, 8) COMMENT '纬度',
    `longitude` DECIMAL(11, 8) COMMENT '经度',
    `altitude` DECIMAL(8, 2) COMMENT '海拔高度（米）',
    `address` VARCHAR(512) COMMENT '详细地址',
    `description` TEXT COMMENT '位置描述',
    `floor_number` VARCHAR(32) COMMENT '楼层号',
    `room_number` VARCHAR(64) COMMENT '房间号',
    `capacity` INT COMMENT '容纳人数',
    `is_indoor` TINYINT DEFAULT 1 COMMENT '是否室内：0-室外，1-室内',
    `access_level` VARCHAR(32) DEFAULT 'PUBLIC' COMMENT '访问级别：PUBLIC-公开，STAFF-员工，SECURE-安全，RESTRICTED-受限',
    `qr_code` VARCHAR(255) COMMENT '二维码',
    `rfid_tag` VARCHAR(128) COMMENT 'RFID标签',
    `beacon_uuid` VARCHAR(128) COMMENT '蓝牙信标UUID',
    `wifi_ssid` VARCHAR(128) COMMENT 'WiFi SSID',
    `wifi_password` VARCHAR(128) COMMENT 'WiFi密码',
    `device_info` JSON COMMENT '设备信息（摄像头数量、传感器等）',
    `coordinates` JSON COMMENT '坐标信息（多边形、圆形等）',
    `map_info` JSON COMMENT '地图信息',
    `is_enabled` TINYINT DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
    `sort_order` INT DEFAULT 0 COMMENT '排序序号',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT NOT NULL COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-正常，1-删除',
    `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
    PRIMARY KEY (`location_id`),
    UNIQUE KEY `uk_location_code` (`location_code`),
    KEY `idx_location_type` (`location_type`),
    KEY `idx_parent_location_id` (`parent_location_id`),
    KEY `idx_location_level` (`location_level`),
    KEY `idx_area_id` (`area_id`),
    KEY `idx_is_indoor` (`is_indoor`),
    KEY `idx_access_level` (`access_level`),
    KEY `idx_is_enabled` (`is_enabled`),
    KEY `idx_sort_order` (`sort_order`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_deleted_flag` (`deleted_flag`),
    SPATIAL INDEX `idx_location_coordinates` (`latitude`, `longitude`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='位置基础信息表';

-- 2. 地理围栏表
CREATE TABLE `t_geo_fence` (
    `fence_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '围栏ID',
    `fence_code` VARCHAR(64) NOT NULL COMMENT '围栏编码',
    `fence_name` VARCHAR(256) NOT NULL COMMENT '围栏名称',
    `fence_type` VARCHAR(32) NOT NULL COMMENT '围栏类型：CIRCLE-圆形，POLYGON-多边形，RECTANGLE-矩形，LINE-线段',
    `fence_shape` JSON NOT NULL COMMENT '围栏形状坐标数据',
    `fence_center_lat` DECIMAL(10, 8) COMMENT '围栏中心纬度（圆形围栏使用）',
    `fence_center_lng` DECIMAL(11, 8) COMMENT '围栏中心经度（圆形围栏使用）',
    `fence_radius` DECIMAL(10, 2) COMMENT '围栏半径（米）',
    `area_id` BIGINT COMMENT '所属区域ID',
    `area_name` VARCHAR(128) COMMENT '所属区域名称',
    `location_ids` JSON COMMENT '关联位置ID列表',
    `fence_level` VARCHAR(32) DEFAULT 'INFO' COMMENT '围栏级别：INFO-信息，WARNING-警告，DANGER-危险，FORBIDDEN-禁止',
    `trigger_action` VARCHAR(64) NOT NULL COMMENT '触发动作：ALERT-告警，BLOCK-阻止，LOG-记录',
    `trigger_condition` VARCHAR(128) COMMENT '触发条件：ENTER-进入，EXIT-离开，DWELL-逗留，SPEED-超速',
    `is_enabled` TINYINT DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
    `effective_time` DATETIME NOT NULL COMMENT '生效时间',
    `expire_time` DATETIME COMMENT '失效时间',
    `notification_config` JSON COMMENT '通知配置',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT NOT NULL COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-正常，1-删除',
    `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
    PRIMARY KEY (`fence_id`),
    UNIQUE KEY `uk_fence_code` (`fence_code`),
    KEY `idx_fence_type` (`fence_type`),
    KEY `idx_fence_level` (`fence_level`),
    KEY `idx_area_id` (`area_id`),
    KEY `idx_is_enabled` (`is_enabled`),
    KEY `idx_effective_expire_time` (`effective_time`, `expire_time`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_deleted_flag` (`deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='地理围栏表';

-- 3. 用户位置记录表
CREATE TABLE `t_user_location` (
    `record_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `user_name` VARCHAR(128) NOT NULL COMMENT '用户姓名',
    `user_type` VARCHAR(32) NOT NULL COMMENT '用户类型：EMPLOYEE-员工，VISITOR-访客，CONTRACTOR-承包商',
    `location_id` BIGINT COMMENT '位置ID',
    `location_name` VARCHAR(256) COMMENT '位置名称',
    `area_id` BIGINT COMMENT '区域ID',
    `area_name` VARCHAR(128) COMMENT '区域名称',
    `latitude` DECIMAL(10, 8) NOT NULL COMMENT '纬度',
    `longitude` DECIMAL(11, 8) NOT NULL COMMENT '经度',
    `altitude` DECIMAL(8, 2) COMMENT '海拔高度（米）',
    `accuracy` DECIMAL(6, 2) COMMENT '定位精度（米）',
    `location_method` VARCHAR(32) NOT NULL COMMENT '定位方法：GPS-全球定位系统，WIFI-WiFi定位，CELLULAR-基站定位，BEACON-蓝牙信标，RFID-RFID定位，MANUAL-手动输入',
    `device_info` JSON COMMENT '定位设备信息',
    `signal_strength` INT COMMENT '信号强度',
    `satellite_count` INT COMMENT '卫星数量（GPS定位时使用）',
    `hdop` DECIMAL(6, 2) COMMENT '水平精度因子',
    `speed` DECIMAL(8, 2) COMMENT '移动速度（米/秒）',
    `direction` DECIMAL(6, 2) COMMENT '移动方向（度）',
    `is_indoor` TINYINT COMMENT '是否室内：0-室外，1-室内，2-未知',
    `floor_detect` TINYINT COMMENT '楼层检测：0-未检测，1-检测到',
    `floor_number` VARCHAR(32) COMMENT '检测到的楼层',
    `building_detect` TINYINT COMMENT '建筑物检测：0-未检测，1-检测到',
    `weather_condition` VARCHAR(64) COMMENT '天气条件',
    `record_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`record_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_user_type` (`user_type`),
    KEY `idx_location_id` (`location_id`),
    KEY `idx_area_id` (`area_id`),
    KEY `idx_location_method` (`location_method`),
    KEY `idx_is_indoor` (`is_indoor`),
    KEY `idx_record_time` (`record_time`),
    KEY `idx_create_time` (`create_time`),
    SPATIAL INDEX `idx_user_location_coordinates` (`latitude`, `longitude`),
    SPATIAL INDEX `idx_user_location_time_coordinates` (`record_time`, `latitude`, `longitude`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户位置记录表';

-- 4. 地理围栏触发记录表
CREATE TABLE `t_geo_fence_trigger` (
    `trigger_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '触发ID',
    `fence_id` BIGINT NOT NULL COMMENT '围栏ID',
    `fence_code` VARCHAR(64) NOT NULL COMMENT '围栏编码',
    `fence_name` VARCHAR(256) NOT NULL COMMENT '围栏名称',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `user_name` VARCHAR(128) NOT NULL COMMENT '用户姓名',
    `user_type` VARCHAR(32) NOT NULL COMMENT '用户类型',
    `trigger_type` VARCHAR(32) NOT NULL COMMENT '触发类型：ENTER-进入，EXIT-离开，DWELL-逗留，SPEED-超速，OFFLINE-离线',
    `trigger_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '触发时间',
    `trigger_latitude` DECIMAL(10, 8) NOT NULL COMMENT '触发位置纬度',
    `trigger_longitude` DECIMAL(11, 8) NOT NULL COMMENT '触发位置经度',
    `trigger_location_id` BIGINT COMMENT '触发位置ID',
    `trigger_location_name` VARCHAR(256) COMMENT '触发位置名称',
    `distance_from_fence` DECIMAL(10, 2) COMMENT '距离围栏的距离（米）',
    `speed` DECIMAL(8, 2) COMMENT '触发时速度（米/秒）',
    `action_taken` VARCHAR(64) COMMENT '采取的动作',
    `notification_sent` TINYINT DEFAULT 0 COMMENT '是否已发送通知：0-未发送，1-已发送',
    `alert_level` VARCHAR(32) DEFAULT 'INFO' COMMENT '告警级别',
    `alert_message` TEXT COMMENT '告警消息',
    `image_capture` VARCHAR(512) COMMENT '现场图片',
    `video_capture` VARCHAR(512) COMMENT '现场视频',
    `additional_data` JSON COMMENT '附加数据',
    `is_handled` TINYINT DEFAULT 0 COMMENT '是否已处理：0-未处理，1-已处理',
    `handle_time` DATETIME COMMENT '处理时间',
    `handle_user_id` BIGINT COMMENT '处理人ID',
    `handle_user_name` VARCHAR(128) COMMENT '处理人姓名',
    `handle_remark` TEXT COMMENT '处理备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`trigger_id`),
    KEY `idx_fence_id` (`fence_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_trigger_type` (`trigger_type`),
    KEY `idx_fence_level` (`fence_level`),
    `idx_alert_level` (`alert_level`),
    KEY `idx_is_handled` (`is_handled`),
    KEY `idx_trigger_time` (`trigger_time`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='地理围栏触发记录表';

-- 5. 位置轨迹表
CREATE TABLE `t_location_trajectory` (
    `trajectory_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '轨迹ID',
    `trajectory_code` VARCHAR(64) NOT NULL COMMENT '轨迹编码',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `user_name` VARCHAR(128) NOT NULL COMMENT '用户姓名',
    `trajectory_type` VARCHAR(32) NOT NULL COMMENT '轨迹类型：WORK-工作轨迹，VISIT-访问轨迹，DELIVERY-配送轨迹，SECURITY-安全轨迹',
    `start_time` DATETIME NOT NULL COMMENT '开始时间',
    `end_time` DATETIME COMMENT '结束时间',
    `start_latitude` DECIMAL(10, 8) NOT NULL COMMENT '起始纬度',
    `start_longitude` DECIMAL(11, 8) NOT NULL COMMENT '起始经度',
    `end_latitude` DECIMAL(10, 8) COMMENT '结束纬度',
    `end_longitude` DECIMAL(11, 8) COMMENT '结束经度',
    `total_distance` DECIMAL(12, 2) COMMENT '总距离（米）',
    `total_duration` INT COMMENT '总时长（秒）',
    `average_speed` DECIMAL(8, 2) COMMENT '平均速度（米/秒）',
    `max_speed` DECIMAL(8, 2) COMMENT '最大速度（米/秒）',
    `waypoint_count` INT DEFAULT 0 COMMENT '路径点数量',
    `area_ids` JSON COMMENT '经过的区域ID列表',
    `location_count` INT DEFAULT 0 COMMENT '经过的位置数量',
    `trajectory_data` JSON COMMENT '轨迹数据（详细路径点列表）',
    `map_image` VARCHAR(512) COMMENT '轨迹地图图片',
    `statistics` JSON COMMENT '轨迹统计信息',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT NOT NULL COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-正常，1-删除',
    `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
    PRIMARY KEY (`trajectory_id`),
    UNIQUE KEY `uk_trajectory_code` (`trajectory_code`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_trajectory_type` (`trajectory_type`),
    KEY `idx_start_time` (`start_time`),
    KEY `idx_end_time` (`end_time`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_deleted_flag` (`deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='位置轨迹表';

-- 6. 位置统计分析表
CREATE TABLE `t_location_statistics` (
    `stat_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '统计ID',
    `stat_date` DATE NOT NULL COMMENT '统计日期',
    `stat_type` VARCHAR(32) NOT NULL COMMENT '统计类型：HOURLY-小时，DAILY-日，WEEKLY-周，MONTHLY-月',
    `area_id` BIGINT COMMENT '区域ID',
    `area_name` VARCHAR(128) COMMENT '区域名称',
    `location_id` BIGINT COMMENT '位置ID',
    `location_name` VARCHAR(256) COMMENT '位置名称',
    `user_type` VARCHAR(32) COMMENT '用户类型',
    `total_visits` INT DEFAULT 0 COMMENT '总访问次数',
    `unique_visitors` INT DEFAULT 0 COMMENT '独立访客数',
    `avg_stay_duration` INT DEFAULT 0 COMMENT '平均停留时长（分钟）',
    `peak_visit_time` TIME COMMENT '高峰访问时间',
    `off_peak_visits` INT DEFAULT 0 COMMENT '非高峰访问次数',
    `indoor_visits` INT DEFAULT 0 COMMENT '室内访问次数',
    `outdoor_visits` INT DEFAULT 0 COMMENT '室外访问次数',
    `device_distribution` JSON COMMENT '设备分布统计',
    `time_distribution` JSON COMMENT '时间分布统计',
    `location_method_stats` JSON COMMENT '定位方式统计',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`stat_id`),
    UNIQUE KEY `uk_stat_date_type_area_location` (`stat_date`, `stat_type`, `area_id`, `location_id`),
    KEY `idx_area_id` (`area_id`),
    KEY `idx_location_id` (`location_id`),
    KEY `idx_user_type` (`user_type`),
    KEY `idx_stat_type` (`stat_type`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='位置统计分析表';

-- 初始化地理位置基础数据
INSERT INTO `t_location` (`location_code`, `location_name`, `location_type`, `parent_location_id`, `location_level`, `latitude`, `longitude`, `is_indoor`, `access_level`, `description`) VALUES
('ROOT', '根位置', 'ROOT', 0, 1, 39.9042, 116.4074, 1, 'PUBLIC', '根节点位置'),
('BUILDING_A', 'A栋', 'BUILDING', 1, 2, 39.9042, 116.4074, 1, 'STAFF', 'A栋办公楼'),
('BUILDING_B', 'B栋', 'BUILDING', 1, 2, 39.9052, 116.4084, 1, 'STAFF', 'B栋办公楼'),
('BUILDING_A_F1', 'A栋1楼', 'FLOOR', 2, 3, 39.9042, 116.4074, 1, 'STAFF', 'A栋1楼'),
('BUILDING_A_F2', 'A栋2楼', 'FLOOR', 2, 4, 39.9042, 116.4074, 1, 'STAFF', 'A栋2楼'),
('BUILDING_A_F3', 'A栋3楼', 'FLOOR', 2, 5, 39.9042, 116.4074, 1, 'STAFF', 'A栋3楼'),
('BUILDING_B_F1', 'B栋1楼', 'FLOOR', 2, 3, 39.9052, 116.4084, 1, 'STAFF', 'B栋1楼'),
('OUTDOOR_AREA', '室外区域', 'AREA', 1, 1, 39.9042, 116.4074, 0, 'PUBLIC', '园区室外区域');

-- 创建索引优化查询性能
CREATE INDEX idx_user_location_user_time ON t_user_location(user_id, record_time DESC);
CREATE INDEX idx_geo_fence_trigger_fence_time ON t_geo_fence_trigger(fence_id, trigger_time DESC);
CREATE INDEX idx_location_trajectory_user_time ON t_location_trajectory(user_id, start_time DESC);
CREATE INDEX idx_location_statistics_date_type ON t_location_statistics(stat_date, stat_type);

-- 创建视图简化复杂查询
CREATE VIEW `v_user_latest_location` AS
SELECT
    ul.user_id,
    ul.user_name,
    ul.user_type,
    ul.location_id,
    ul.location_name,
    ul.latitude,
    ul.longitude,
    ul.altitude,
    ul.accuracy,
    ul.record_time
FROM t_user_location ul
INNER JOIN (
    SELECT user_id, MAX(record_time) as max_time
    FROM t_user_location
    GROUP BY user_id
) latest ON ul.user_id = latest.user_id AND ul.record_time = latest.max_time;

-- 创建视图：围栏状态概览
CREATE VIEW `v_geo_fence_status` AS
SELECT
    f.fence_id,
    f.fence_code,
    f.fence_name,
    f.fence_type,
    f.fence_level,
    f.is_enabled,
    CASE
        WHEN f.expire_time IS NULL THEN 1
        WHEN f.expire_time > NOW() THEN 1
        ELSE 0
    END as is_active,
    (SELECT COUNT(*) FROM t_geo_fence_trigger gt WHERE gt.fence_id = f.fence_id AND gt.trigger_time >= DATE_SUB(NOW(), INTERVAL 1 HOUR)) as recent_triggers
FROM t_geo_fence f;