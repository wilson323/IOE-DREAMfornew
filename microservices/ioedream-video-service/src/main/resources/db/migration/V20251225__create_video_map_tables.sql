-- ============================================================
-- 视频监控地图数据库表
-- 创建日期: 2025-12-25
-- 说明: 支持视频设备在地图上的可视化展示与交互
-- ============================================================

-- ------------------------------------------------------------
-- 1. 视频设备地图表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t_video_device_map (
    -- 主键
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '地图ID',

    -- 设备关联
    device_id BIGINT NOT NULL COMMENT '设备ID',
    device_code VARCHAR(64) NOT NULL COMMENT '设备编码',
    device_name VARCHAR(128) NOT NULL COMMENT '设备名称',

    -- 地图坐标
    map_type TINYINT NOT NULL DEFAULT 1 COMMENT '地图类型 1-平面图 2-3D地图 3-BIM地图',
    coordinate_type TINYINT NOT NULL DEFAULT 1 COMMENT '坐标类型 1-像素坐标 2-地理坐标(经纬度)',
    x_coordinate DECIMAL(12, 6) NOT NULL COMMENT 'X坐标(像素或经度)',
    y_coordinate DECIMAL(12, 6) NOT NULL COMMENT 'Y坐标(像素或纬度)',
    z_coordinate DECIMAL(8, 2) COMMENT 'Z坐标(高度，3D/BIM使用)',

    -- 区域关联
    area_id BIGINT NOT NULL COMMENT '所属区域ID',
    floor_level TINYINT COMMENT '楼层(0-地下层 1-N楼层)',
    zone_code VARCHAR(64) COMMENT '区域编码',

    -- 显示配置
    marker_type VARCHAR(32) DEFAULT 'default' COMMENT '标记类型 default/camera/ptz/dome',
    marker_color VARCHAR(16) DEFAULT '#FF5722' COMMENT '标记颜色',
    marker_size TINYINT DEFAULT 3 COMMENT '标记大小 1-5',
    icon_url VARCHAR(512) COMMENT '自定义图标URL',

    -- 地图关联
    map_image_id BIGINT COMMENT '平面图图片ID',
    map_name VARCHAR(128) COMMENT '地图名称',
    map_scale DECIMAL(6, 3) DEFAULT 1.0 COMMENT '地图缩放比例',

    -- 状态
    display_status TINYINT DEFAULT 1 COMMENT '显示状态 0-隐藏 1-显示 2-维护',
    click_action TINYINT DEFAULT 1 COMMENT '点击行为 1-播放视频 2-查看详情 3-云台控制',
    enable_popup TINYINT DEFAULT 1 COMMENT '是否启用弹窗 0-否 1-是',

    -- 扩展字段
    extended_attributes TEXT COMMENT '扩展属性(JSON格式)',

    -- 审计字段
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',

    -- 索引
    INDEX idx_device_id (device_id),
    INDEX idx_area_id (area_id),
    INDEX idx_map_image (map_image_id),
    INDEX idx_display_status (display_status, deleted_flag),
    INDEX idx_coordinate (map_type, x_coordinate, y_coordinate)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频设备地图表';

-- ------------------------------------------------------------
-- 2. 地图图片表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t_video_map_image (
    -- 主键
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '图片ID',
    image_uuid VARCHAR(64) NOT NULL UNIQUE COMMENT '图片UUID',

    -- 基本信息
    image_name VARCHAR(128) NOT NULL COMMENT '图片名称',
    image_type VARCHAR(32) NOT NULL COMMENT '图片类型 floor/birdview/panorama',
    image_url VARCHAR(512) NOT NULL COMMENT '图片URL',
    thumbnail_url VARCHAR(512) COMMENT '缩略图URL',

    -- 地图信息
    area_id BIGINT NOT NULL COMMENT '所属区域ID',
    floor_level TINYINT COMMENT '楼层',
    map_width INT COMMENT '地图宽度(像素)',
    map_height INT COMMENT '地图高度(像素)',

    -- 坐标系统
    coordinate_system VARCHAR(32) DEFAULT 'pixel' COMMENT '坐标系统 pixel/geographic',
    origin_x DECIMAL(12, 6) DEFAULT 0 COMMENT '原点X坐标',
    origin_y DECIMAL(12, 6) DEFAULT 0 COMMENT '原点Y坐标',
    pixels_per_meter DECIMAL(8, 3) COMMENT '每米像素数',

    -- 状态
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认地图',

    -- 扩展
    description VARCHAR(500) COMMENT '描述',

    -- 审计字段
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',

    -- 索引
    INDEX idx_area_id (area_id),
    INDEX idx_status (status, deleted_flag),
    INDEX idx_image_type (image_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频地图图片表';

-- ------------------------------------------------------------
-- 3. 地图热点表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t_video_map_hotspot (
    -- 主键
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '热点ID',
    hotspot_uuid VARCHAR(64) NOT NULL UNIQUE COMMENT '热点UUID',

    -- 关联
    map_image_id BIGINT NOT NULL COMMENT '所属地图ID',

    -- 基本信息
    hotspot_name VARCHAR(128) NOT NULL COMMENT '热点名称',
    hotspot_type VARCHAR(32) NOT NULL COMMENT '热点类型 camera/zone/entrance/exit/landmark',

    -- 坐标位置
    x_coordinate DECIMAL(12, 6) NOT NULL COMMENT 'X坐标',
    y_coordinate DECIMAL(12, 6) NOT NULL COMMENT 'Y坐标',

    -- 显示配置
    icon_url VARCHAR(512) COMMENT '图标URL',
    icon_size TINYINT DEFAULT 3 COMMENT '图标大小 1-5',
    color VARCHAR(16) COMMENT '颜色',

    -- 关联设备
    device_id BIGINT COMMENT '关联设备ID',

    -- 热区范围（多边形）
    area_coordinates TEXT COMMENT '区域坐标(JSON数组: [[x1,y1],[x2,y2],...])',

    -- 交互配置
    click_action TINYINT DEFAULT 1 COMMENT '点击行为 1-播放视频 2-查看设备 3-跳转链接',
    action_data VARCHAR(512) COMMENT '行为数据(URL或设备ID等)',
    tooltip_text VARCHAR(256) COMMENT '提示文本',

    -- 状态
    display_status TINYINT DEFAULT 1 COMMENT '显示状态 0-隐藏 1-显示',

    -- 审计字段
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',

    -- 索引
    INDEX idx_map_image (map_image_id),
    INDEX idx_device_id (device_id),
    INDEX idx_hotspot_type (hotspot_type),
    INDEX idx_position (map_image_id, x_coordinate, y_coordinate)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地图热点表';

-- ------------------------------------------------------------
-- 4. 视频设备地图配置表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t_video_map_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    config_key VARCHAR(64) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT NOT NULL COMMENT '配置值',
    config_type VARCHAR(32) DEFAULT 'string' COMMENT '配置类型 string/number/boolean/json',
    description VARCHAR(500) COMMENT '配置描述',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频地图配置表';

-- ------------------------------------------------------------
-- 初始化默认配置
-- ------------------------------------------------------------
INSERT INTO t_video_map_config (config_key, config_value, config_type, description) VALUES
('map.default_zoom_level', '3', 'number', '默认缩放级别'),
('map.min_zoom_level', '1', 'number', '最小缩放级别'),
('map.max_zoom_level', '5', 'number', '最大缩放级别'),
('map.enable_3d_view', 'true', 'boolean', '是否启用3D视图'),
('map.auto_refresh_interval', '30', 'number', '自动刷新间隔(秒)'),
('map.show_device_label', 'true', 'boolean', '是否显示设备标签'),
('map.device_marker_size', '3', 'number', '设备标记大小'),
('map.enable_cluster', 'true', 'boolean', '是否启用设备聚合'),
('map.cluster_max_zoom', '4', 'number', '聚合显示的最大缩放级别'),
('map.animation_duration', '300', 'number', '动画持续时间(毫秒)')
ON DUPLICATE KEY UPDATE update_time = CURRENT_TIMESTAMP;

-- ------------------------------------------------------------
-- 初始化示例平面图
-- ------------------------------------------------------------
INSERT INTO t_video_map_image (image_uuid, image_name, image_type, image_url, area_id, floor_level, map_width, map_height, status, is_default, description) VALUES
('MAP-FLOOR-1-001', '一楼平面图', 'floor', '/static/map/floor1.png', 1, 1, 1920, 1080, 1, 1, '一楼监控平面图'),
('MAP-FLOOR-2-001', '二楼平面图', 'floor', '/static/map/floor2.png', 1, 2, 1920, 1080, 1, 0, '二楼监控平面图'),
('MAP-BIRDVIEW-001', '鸟瞰图', 'birdview', '/static/map/birdview.png', 1, NULL, 3840, 2160, 1, 0, '园区鸟瞰图')
ON DUPLICATE KEY UPDATE update_time = CURRENT_TIMESTAMP;
