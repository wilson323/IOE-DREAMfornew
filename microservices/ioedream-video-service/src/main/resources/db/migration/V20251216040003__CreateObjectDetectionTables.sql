-- ============================================================================
-- IOE-DREAM 视频微服务 - 目标检测智能分析功能数据库迁移脚本
-- 版本: V20251216040003
-- 创建时间: 2025-12-16
-- 描述: 创建视频目标检测相关的数据库表，支持多种目标类型的AI检测和分析
-- 功能: 目标检测、目标跟踪、轨迹分析、告警联动等
-- ============================================================================

-- 设置字符集和排序规则
SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建目标检测记录表
CREATE TABLE `t_video_object_detection` (
    `detection_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '检测记录ID',
    `device_id` BIGINT NOT NULL COMMENT '设备ID',
    `device_code` VARCHAR(64) NOT NULL COMMENT '设备编码',
    `channel_id` INT DEFAULT 1 COMMENT '通道ID',
    `detection_time` DATETIME(3) NOT NULL COMMENT '检测时间',
    `detection_algorithm` VARCHAR(64) DEFAULT NULL COMMENT '检测算法ID',

    -- 目标基本信息
    `object_type` INT NOT NULL COMMENT '目标类型 1-人员 2-车辆 3-物体 4-动物 5-人脸 6-车牌 7-行李 8-危险品 9-其他',
    `object_sub_type` INT DEFAULT NULL COMMENT '目标子类型',
    `object_type_desc` VARCHAR(100) DEFAULT NULL COMMENT '目标类型描述',
    `object_id` VARCHAR(64) DEFAULT NULL COMMENT '目标ID（跟踪ID）',
    `confidence_score` DECIMAL(5,4) DEFAULT NULL COMMENT '置信度',

    -- 目标位置和大小
    `bounding_box` TEXT DEFAULT NULL COMMENT '边界框坐标（JSON格式）{"x":100,"y":200,"width":150,"height":200}',
    `center_x` DECIMAL(10,2) DEFAULT NULL COMMENT '中心点坐标X',
    `center_y` DECIMAL(10,2) DEFAULT NULL COMMENT '中心点坐标Y',
    `object_width` DECIMAL(10,2) DEFAULT NULL COMMENT '目标宽度',
    `object_height` DECIMAL(10,2) DEFAULT NULL COMMENT '目标高度',
    `object_area` DECIMAL(12,2) DEFAULT NULL COMMENT '目标面积',
    `relative_size` DECIMAL(8,4) DEFAULT NULL COMMENT '相对大小（占画面比例）',

    -- 目标特征
    `color_features` TEXT DEFAULT NULL COMMENT '目标颜色特征（JSON格式）',
    `texture_features` VARCHAR(500) DEFAULT NULL COMMENT '目标纹理特征',
    `shape_features` VARCHAR(500) DEFAULT NULL COMMENT '目标形状特征',

    -- 运动信息
    `movement_direction` DECIMAL(8,2) DEFAULT NULL COMMENT '运动方向（角度）',
    `movement_speed` DECIMAL(10,2) DEFAULT NULL COMMENT '运动速度（像素/秒）',
    `movement_status` INT DEFAULT NULL COMMENT '运动状态 1-静止 2-慢速 3-中速 4-快速 5-变速',

    -- 距离和实际大小
    `object_distance` DECIMAL(10,2) DEFAULT NULL COMMENT '目标距离（米）',
    `actual_size` DECIMAL(8,2) DEFAULT NULL COMMENT '目标实际大小（基于标定）',

    -- 目标分类和属性
    `object_class` VARCHAR(100) DEFAULT NULL COMMENT '目标分类（详细）',
    `object_attributes` TEXT DEFAULT NULL COMMENT '目标属性（JSON格式）',
    `object_count` INT DEFAULT 1 COMMENT '目标数量（同一类别的数量）',
    `density_level` INT DEFAULT NULL COMMENT '密度等级 1-稀疏 2-正常 3-密集 4-拥挤',

    -- 区域信息
    `area_id` BIGINT DEFAULT NULL COMMENT '区域ID',
    `area_name` VARCHAR(100) DEFAULT NULL COMMENT '区域名称',
    `area_type` INT DEFAULT NULL COMMENT '区域类型 1-禁区 2-监控区 3-安全区 4-公共区 5-办公区 6-生产区',
    `in_restricted_area` TINYINT DEFAULT 0 COMMENT '是否在禁区内',

    -- 告警信息
    `alert_level` INT DEFAULT NULL COMMENT '告警级别 1-信息 2-提醒 3-警告 4-严重 5-紧急',
    `alert_triggered` TINYINT DEFAULT 0 COMMENT '是否触发告警',
    `alert_type` VARCHAR(100) DEFAULT NULL COMMENT '告警类型',
    `alert_description` VARCHAR(500) DEFAULT NULL COMMENT '告警描述',

    -- 关联信息
    `face_id` VARCHAR(64) DEFAULT NULL COMMENT '关联人脸ID',
    `person_id` BIGINT DEFAULT NULL COMMENT '关联人员ID',
    `person_name` VARCHAR(100) DEFAULT NULL COMMENT '关联人员姓名',
    `plate_number` VARCHAR(50) DEFAULT NULL COMMENT '关联车牌号',
    `vehicle_brand` VARCHAR(100) DEFAULT NULL COMMENT '关联车辆品牌',
    `vehicle_model` VARCHAR(100) DEFAULT NULL COMMENT '关联车辆型号',
    `vehicle_color` VARCHAR(50) DEFAULT NULL COMMENT '关联车辆颜色',

    -- 图像信息
    `object_image_url` VARCHAR(500) DEFAULT NULL COMMENT '目标图像URL',
    `thumbnail_url` VARCHAR(500) DEFAULT NULL COMMENT '目标缩略图URL',

    -- 模型和性能
    `model_version` VARCHAR(50) DEFAULT NULL COMMENT '检测模型版本',
    `process_status` INT DEFAULT 0 COMMENT '处理状态 0-未处理 1-处理中 2-已处理 3-已忽略',
    `verification_result` INT DEFAULT 0 COMMENT '验证结果 0-未验证 1-正确 2-错误 3-不确定',
    `verified_by` BIGINT DEFAULT NULL COMMENT '验证人员ID',
    `verification_time` DATETIME(3) DEFAULT NULL COMMENT '验证时间',
    `verification_note` VARCHAR(500) DEFAULT NULL COMMENT '验证备注',
    `data_source` INT DEFAULT 1 COMMENT '数据来源 1-实时检测 2-录像分析 3-图片分析 4-批量导入',
    `processing_time` INT DEFAULT NULL COMMENT '检测耗时（毫秒）',
    `hardware_acceleration` TINYINT DEFAULT 0 COMMENT '硬件加速',

    -- 扩展和备注
    `extended_attributes` TEXT DEFAULT NULL COMMENT '扩展属性（JSON格式）',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注信息',

    -- 审计字段
    `create_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `update_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    `create_user_id` BIGINT DEFAULT NULL COMMENT '创建用户ID',
    `update_user_id` BIGINT DEFAULT NULL COMMENT '更新用户ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',

    PRIMARY KEY (`detection_id`),
    INDEX `idx_detection_device_time` (`device_id`, `detection_time`),
    INDEX `idx_detection_object_type` (`object_type`, `detection_time`),
    INDEX `idx_detection_object_id` (`object_id`, `detection_time`),
    INDEX `idx_detection_alert` (`alert_triggered`, `alert_level`, `detection_time`),
    INDEX `idx_detection_area` (`area_id`, `detection_time`),
    INDEX `idx_detection_confidence` (`confidence_score`),
    INDEX `idx_detection_process` (`process_status`, `detection_time`),
    INDEX `idx_detection_person` (`person_id`, `detection_time`),
    INDEX `idx_detection_plate` (`plate_number`, `detection_time`),
    INDEX `idx_detection_face` (`face_id`, `detection_time`),
    INDEX `idx_detection_create_time` (`create_time`),
    INDEX `idx_detection_deleted` (`deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频目标检测记录表';

-- 创建目标跟踪记录表
CREATE TABLE `t_video_object_tracking` (
    `tracking_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '跟踪记录ID',
    `object_id` VARCHAR(64) NOT NULL COMMENT '目标ID（跨帧的唯一标识）',
    `device_id` BIGINT NOT NULL COMMENT '设备ID',
    `channel_id` INT DEFAULT 1 COMMENT '通道ID',

    -- 跟踪时间信息
    `tracking_start_time` DATETIME(3) NOT NULL COMMENT '跟踪开始时间',
    `tracking_end_time` DATETIME(3) DEFAULT NULL COMMENT '跟踪结束时间',
    `tracking_duration` BIGINT DEFAULT NULL COMMENT '跟踪持续时间（秒）',

    -- 目标基本信息
    `object_type` INT NOT NULL COMMENT '目标类型 1-人员 2-车辆 3-物体 4-动物 5-其他',
    `object_type_desc` VARCHAR(100) DEFAULT NULL COMMENT '目标类型描述',

    -- 位置信息
    `initial_x` DECIMAL(10,2) DEFAULT NULL COMMENT '初始位置X',
    `initial_y` DECIMAL(10,2) DEFAULT NULL COMMENT '初始位置Y',
    `final_x` DECIMAL(10,2) DEFAULT NULL COMMENT '最终位置X',
    `final_y` DECIMAL(10,2) DEFAULT NULL COMMENT '最终位置Y',

    -- 运动统计
    `total_distance` DECIMAL(12,2) DEFAULT NULL COMMENT '总位移距离（像素）',
    `average_speed` DECIMAL(10,2) DEFAULT NULL COMMENT '平均速度（像素/秒）',
    `max_speed` DECIMAL(10,2) DEFAULT NULL COMMENT '最大速度（像素/秒）',
    `main_direction` DECIMAL(8,2) DEFAULT NULL COMMENT '主要运动方向（角度）',

    -- 轨迹信息
    `trajectory_path` TEXT DEFAULT NULL COMMENT '运动轨迹（JSON格式）',
    `trajectory_points` INT DEFAULT 0 COMMENT '轨迹点数量',

    -- 跟踪质量
    `lost_count` INT DEFAULT 0 COMMENT '丢失次数',
    `reacquire_count` INT DEFAULT 0 COMMENT '重获次数',
    `tracking_quality_score` DECIMAL(5,4) DEFAULT NULL COMMENT '跟踪质量评分',
    `average_confidence` DECIMAL(5,4) DEFAULT NULL COMMENT '平均置信度',
    `min_confidence` DECIMAL(5,4) DEFAULT NULL COMMENT '最低置信度',
    `max_confidence` DECIMAL(5,4) DEFAULT NULL COMMENT '最高置信度',

    -- 区域行为
    `entered_areas` VARCHAR(1000) DEFAULT NULL COMMENT '进入区域列表（JSON数组）',
    `exited_areas` VARCHAR(1000) DEFAULT NULL COMMENT '离开区域列表（JSON数组）',
    `area_violations` INT DEFAULT 0 COMMENT '区域违规次数',
    `loitering_area_id` BIGINT DEFAULT NULL COMMENT '停留区域ID',
    `loitering_start_time` DATETIME(3) DEFAULT NULL COMMENT '停留开始时间',
    `loitering_duration` BIGINT DEFAULT NULL COMMENT '停留持续时间（秒）',

    -- 跟踪状态
    `tracking_status` INT DEFAULT 1 COMMENT '跟踪状态 1-活跃 2-丢失 3-完成 4-中断',
    `lost_reason` INT DEFAULT NULL COMMENT '丢失原因 1-遮挡 2-快速移动 3-离开画面 4-目标变形 5-光照变化',

    -- 统计信息
    `detection_count` INT DEFAULT 0 COMMENT '关联检测记录数量',
    `alert_count` INT DEFAULT 0 COMMENT '关联告警数量',

    -- 关联信息
    `associated_face_id` VARCHAR(64) DEFAULT NULL COMMENT '关联人脸ID（如果有）',
    `associated_person_id` BIGINT DEFAULT NULL COMMENT '关联人员ID（如果有）',
    `associated_plate_number` VARCHAR(50) DEFAULT NULL COMMENT '关联车牌号（如果有）',

    -- 分析结果
    `trajectory_analysis` TEXT DEFAULT NULL COMMENT '轨迹分析结果（JSON格式）',
    `behavior_prediction` TEXT DEFAULT NULL COMMENT '行为预测（JSON格式）',
    `anomaly_flag` INT DEFAULT 0 COMMENT '异常标记 0-正常 1-异常轨迹 2-可疑行为 3-重点关注',
    `anomaly_description` VARCHAR(500) DEFAULT NULL COMMENT '异常描述',

    -- 技术信息
    `tracking_algorithm_version` VARCHAR(50) DEFAULT NULL COMMENT '跟踪算法版本',
    `process_status` INT DEFAULT 0 COMMENT '处理状态 0-未处理 1-处理中 2-已处理 3-已忽略',
    `priority` INT DEFAULT 2 COMMENT '优先级 1-低 2-中 3-高 4-紧急',

    -- 审计字段
    `create_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `update_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    `create_user_id` BIGINT DEFAULT NULL COMMENT '创建用户ID',
    `update_user_id` BIGINT DEFAULT NULL COMMENT '更新用户ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注信息',

    PRIMARY KEY (`tracking_id`),
    UNIQUE KEY `uk_tracking_object_device_start` (`object_id`, `device_id`, `tracking_start_time`),
    INDEX `idx_tracking_object_time` (`object_id`, `tracking_start_time`),
    INDEX `idx_tracking_device_time` (`device_id`, `tracking_start_time`),
    INDEX `idx_tracking_status` (`tracking_status`, `tracking_start_time`),
    INDEX `idx_tracking_type` (`object_type`, `tracking_start_time`),
    INDEX `idx_tracking_quality` (`tracking_quality_score`),
    INDEX `idx_tracking_duration` (`tracking_duration`),
    INDEX `idx_tracking_area` (`loitering_area_id`, `tracking_start_time`),
    INDEX `idx_tracking_person` (`associated_person_id`, `tracking_start_time`),
    INDEX `idx_tracking_vehicle` (`associated_plate_number`, `tracking_start_time`),
    INDEX `idx_tracking_create_time` (`create_time`),
    INDEX `idx_tracking_deleted` (`deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频目标跟踪记录表';

-- 创建目标检测索引表（用于快速查询和分析）
CREATE TABLE `t_video_object_detection_index` (
    `index_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '索引ID',
    `detection_id` BIGINT NOT NULL COMMENT '检测记录ID',
    `device_id` BIGINT NOT NULL COMMENT '设备ID',
    `detection_time` DATETIME(3) NOT NULL COMMENT '检测时间',
    `object_type` INT NOT NULL COMMENT '目标类型',
    `confidence_score` DECIMAL(5,4) DEFAULT NULL COMMENT '置信度',
    `alert_triggered` TINYINT DEFAULT 0 COMMENT '是否触发告警',
    `process_status` INT DEFAULT 0 COMMENT '处理状态',
    `person_id` BIGINT DEFAULT NULL COMMENT '关联人员ID',
    `area_id` BIGINT DEFAULT NULL COMMENT '区域ID',
    `time_bucket` VARCHAR(20) NOT NULL COMMENT '时间桶（YYYYMMDDHH）',
    `type_confidence_bucket` VARCHAR(20) NOT NULL COMMENT '类型置信度桶',
    `alert_status_bucket` VARCHAR(20) NOT NULL COMMENT '告警状态桶',
    `create_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `update_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',

    PRIMARY KEY (`index_id`),
    UNIQUE KEY `uk_index_detection` (`detection_id`),
    INDEX `idx_index_device_time` (`device_id`, `detection_time`),
    INDEX `idx_index_time_bucket` (`time_bucket`),
    INDEX `idx_index_type_bucket` (`type_confidence_bucket`),
    INDEX `idx_index_alert_bucket` (`alert_status_bucket`),
    INDEX `idx_index_person` (`person_id`),
    INDEX `idx_index_area` (`area_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频目标检测索引表';

-- 创建目标检测配置表
CREATE TABLE `t_video_object_detection_config` (
    `config_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
    `config_value` TEXT NOT NULL COMMENT '配置值（JSON格式）',
    `config_desc` VARCHAR(500) DEFAULT NULL COMMENT '配置描述',
    `config_type` VARCHAR(50) NOT NULL COMMENT '配置类型 algorithm/model/threshold/parameter',
    `is_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
    `priority` INT DEFAULT 100 COMMENT '优先级',
    `version` VARCHAR(50) DEFAULT NULL COMMENT '配置版本',

    -- 审计字段
    `create_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `update_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    `create_user_id` BIGINT DEFAULT NULL COMMENT '创建用户ID',
    `update_user_id` BIGINT DEFAULT NULL COMMENT '更新用户ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',

    PRIMARY KEY (`config_id`),
    UNIQUE KEY `uk_config_key` (`config_key`),
    INDEX `idx_config_type` (`config_type`),
    INDEX `idx_config_enabled` (`is_enabled`),
    INDEX `idx_config_priority` (`priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频目标检测配置表';

-- 插入默认配置
INSERT INTO `t_video_object_detection_config` (`config_key`, `config_value`, `config_desc`, `config_type`, `is_enabled`, `priority`) VALUES
('object_detection_algorithms', '["YOLOv8", "YOLOv9", "Faster R-CNN", "SSD"]', '支持的目标检测算法列表', 'algorithm', 1, 100),
('default_confidence_threshold', '0.5', '默认置信度阈值', 'threshold', 1, 200),
('high_confidence_threshold', '0.8', '高置信度阈值', 'threshold', 1, 201),
('max_detection_objects', '100', '单次检测最大目标数量', 'parameter', 1, 300),
('enable_tracking', 'true', '是否启用目标跟踪', 'parameter', 1, 400),
('tracking_max_distance', '1000', '跟踪最大距离（像素）', 'parameter', 1, 500),
('enable_area_intrusion', 'true', '是否启用区域入侵检测', 'parameter', 1, 600),
('enable_real_time_alert', 'true', '是否启用实时告警', 'parameter', 1, 700),
('detection_interval', '100', '检测间隔（毫秒）', 'parameter', 1, 800),
('enable_gpu_acceleration', 'true', '是否启用GPU加速', 'parameter', 1, 900);

-- 创建存储过程：更新目标检测统计信息
DELIMITER //
CREATE PROCEDURE `sp_update_object_detection_statistics`(
    IN p_stat_date DATETIME,
    IN p_stat_type VARCHAR(20)
)
BEGIN
    DECLARE v_total_detections INT DEFAULT 0;
    DECLARE v_total_alerts INT DEFAULT 0;
    DECLARE v_avg_confidence DECIMAL(5,4) DEFAULT 0;
    DECLARE v_avg_processing_time DECIMAL(10,2) DEFAULT 0;

    -- 获取统计数据
    SELECT
        COUNT(*) INTO v_total_detections,
        SUM(CASE WHEN alert_triggered = 1 THEN 1 ELSE 0 END) INTO v_total_alerts,
        AVG(confidence_score) INTO v_avg_confidence,
        AVG(processing_time) INTO v_avg_processing_time
    FROM t_video_object_detection
    WHERE DATE(detection_time) = DATE(p_stat_date)
    AND deleted_flag = 0;

    -- 插入或更新统计记录
    INSERT INTO t_video_object_detection_statistics (
        stat_date,
        stat_type,
        total_detections,
        total_alerts,
        avg_confidence,
        avg_processing_time,
        create_time
    ) VALUES (
        DATE(p_stat_date),
        p_stat_type,
        v_total_detections,
        v_total_alerts,
        v_avg_confidence,
        v_avg_processing_time,
        NOW()
    )
    ON DUPLICATE KEY UPDATE
        total_detections = v_total_detections,
        total_alerts = v_total_alerts,
        avg_confidence = v_avg_confidence,
        avg_processing_time = v_avg_processing_time,
        update_time = NOW();

    SELECT
        v_total_detections as total_detections,
        v_total_alerts as total_alerts,
        v_avg_confidence as avg_confidence,
        v_avg_processing_time as avg_processing_time;
END //
DELIMITER ;

-- 创建统计表
CREATE TABLE `t_video_object_detection_statistics` (
    `stat_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '统计ID',
    `stat_date` DATE NOT NULL COMMENT '统计日期',
    `stat_type` VARCHAR(20) NOT NULL COMMENT '统计类型 daily/weekly/monthly',
    `total_detections` INT DEFAULT 0 COMMENT '总检测数量',
    `total_alerts` INT DEFAULT 0 COMMENT '总告警数量',
    `avg_confidence` DECIMAL(5,4) DEFAULT 0 COMMENT '平均置信度',
    `avg_processing_time` DECIMAL(10,2) DEFAULT 0 COMMENT '平均处理时间',
    `device_count` INT DEFAULT 0 COMMENT '参与设备数量',
    `object_type_stats` TEXT DEFAULT NULL COMMENT '目标类型统计（JSON）',
    `alert_level_stats` TEXT DEFAULT NULL COMMENT '告警级别统计（JSON）',
    `performance_metrics` TEXT DEFAULT NULL COMMENT '性能指标（JSON）',

    PRIMARY KEY (`stat_id`),
    UNIQUE KEY `uk_stats_date_type` (`stat_date`, `stat_type`),
    INDEX `idx_stats_date` (`stat_date`),
    INDEX `idx_stats_type` (`stat_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频目标检测统计表';

-- 创建触发器：自动更新索引表
DELIMITER //
CREATE TRIGGER `tr_video_object_detection_insert`
AFTER INSERT ON `t_video_object_detection`
FOR EACH ROW
BEGIN
    INSERT INTO `t_video_object_detection_index` (
        detection_id, device_id, detection_time, object_type, confidence_score,
        alert_triggered, process_status, person_id, area_id,
        time_bucket, type_confidence_bucket, alert_status_bucket
    ) VALUES (
        NEW.detection_id, NEW.device_id, NEW.detection_time, NEW.object_type, NEW.confidence_score,
        NEW.alert_triggered, NEW.process_status, NEW.person_id, NEW.area_id,
        DATE_FORMAT(NEW.detection_time, '%Y%m%d%H'),
        CONCAT(NEW.object_type, '_', IFNULL(NEW.confidence_score * 100, 0)),
        CONCAT(NEW.alert_triggered, '_', NEW.process_status)
    )
    ON DUPLICATE KEY UPDATE
        device_id = NEW.device_id,
        detection_time = NEW.detection_time,
        object_type = NEW.object_type,
        confidence_score = NEW.confidence_score,
        alert_triggered = NEW.alert_triggered,
        process_status = NEW.process_status,
        person_id = NEW.person_id,
        area_id = NEW.area_id,
        update_time = NOW();
END //
DELIMITER ;

-- 创建视图：目标检测概览
CREATE VIEW `v_object_detection_overview` AS
SELECT
    device_id,
    device_code,
    DATE(detection_time) as detection_date,
    object_type,
    object_type_desc,
    COUNT(*) as detection_count,
    AVG(confidence_score) as avg_confidence,
    MAX(confidence_score) as max_confidence,
    MIN(confidence_score) as min_confidence,
    SUM(CASE WHEN alert_triggered = 1 THEN 1 ELSE 0 END) as alert_count,
    AVG(processing_time) as avg_processing_time
FROM t_video_object_detection
WHERE deleted_flag = 0
GROUP BY device_id, device_code, DATE(detection_time), object_type, object_type_desc;

-- 创建视图：目标跟踪概览
CREATE VIEW `v_object_tracking_overview` AS
SELECT
    object_id,
    object_type,
    object_type_desc,
    COUNT(*) as tracking_count,
    SUM(tracking_duration) as total_duration,
    AVG(tracking_quality_score) as avg_quality,
    AVG(average_confidence) as avg_confidence,
    SUM(total_distance) as total_distance,
    SUM(detection_count) as total_detections,
    SUM(alert_count) as total_alerts,
    MAX(tracking_start_time) as last_seen,
    MIN(tracking_start_time) as first_seen
FROM t_video_object_tracking
WHERE deleted_flag = 0
GROUP BY object_id, object_type, object_type_desc;

-- 创建视图：实时检测状态
CREATE VIEW `v_real_time_detection_status` AS
SELECT
    d.device_id,
    d.device_code,
    d.object_type,
    d.object_type_desc,
    d.detection_time,
    d.confidence_score,
    d.alert_triggered,
    d.alert_level,
    t.tracking_status,
    t.tracking_duration,
    t.total_distance,
    t.average_speed
FROM t_video_object_detection d
LEFT JOIN t_video_object_tracking t ON d.object_id = t.object_id
    AND t.tracking_status = 1
WHERE d.deleted_flag = 0
    AND d.detection_time >= DATE_SUB(NOW(), INTERVAL 1 HOUR)
ORDER BY d.detection_time DESC;

-- 添加外键约束（可选，根据需要启用）
-- ALTER TABLE `t_video_object_detection` ADD CONSTRAINT `fk_detection_device`
--     FOREIGN KEY (`device_id`) REFERENCES `t_video_device` (`device_id`);

-- 创建分区表（针对大数据量场景）
-- ALTER TABLE `t_video_object_detection`
-- PARTITION BY RANGE (YEAR(detection_time)) (
--     PARTITION p2024 VALUES LESS THAN (2025),
--     PARTITION p2025 VALUES LESS THAN (2026),
--     PARTITION p_future VALUES LESS THAN MAXVALUE
-- );

-- 注释和文档
ALTER TABLE `t_video_object_detection` COMMENT = '视频目标检测记录表 - 支持多种目标类型的AI检测和分析';
ALTER TABLE `t_video_object_tracking` COMMENT = '视频目标跟踪记录表 - 记录目标在连续帧中的运动轨迹和跟踪信息';
ALTER TABLE `t_video_object_detection_index` COMMENT = '视频目标检测索引表 - 用于快速查询和分析';
ALTER TABLE `t_video_object_detection_config` COMMENT = '视频目标检测配置表 - 存储算法、阈值、参数等配置信息';
ALTER TABLE `t_video_object_detection_statistics` COMMENT = '视频目标检测统计表 - 存储按时间维度的统计数据';

-- 输出创建完成信息
SELECT 'Object Detection Tables Created Successfully' as status,
       NOW() as created_time,
       COUNT(*) as total_tables_created
FROM information_schema.tables
WHERE table_schema = DATABASE()
AND table_name IN ('t_video_object_detection', 't_video_object_tracking', 't_video_object_detection_index', 't_video_object_detection_config', 't_video_object_detection_statistics');