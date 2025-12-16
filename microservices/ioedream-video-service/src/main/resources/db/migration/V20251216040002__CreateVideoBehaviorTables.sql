-- 视频行为检测相关表
-- IOE-DREAM Phase 3.2: 实现行为检测智能分析

-- 创建行为检测记录表
CREATE TABLE `t_video_behavior` (
  `behavior_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '行为ID',
  `device_id` BIGINT NOT NULL COMMENT '设备ID',
  `device_code` VARCHAR(50) NOT NULL COMMENT '设备编码',
  `device_name` VARCHAR(100) DEFAULT NULL COMMENT '设备名称',
  `channel_id` BIGINT DEFAULT NULL COMMENT '通道ID',
  `channel_name` VARCHAR(100) DEFAULT NULL COMMENT '通道名称',

  `detection_time` DATETIME(3) NOT NULL COMMENT '检测时间',
  `behavior_type` INT NOT NULL COMMENT '行为类型 1-人员检测 2-车辆检测 3-物体检测 4-人脸检测 5-异常行为 6-正常行为 7-其他行为',
  `behavior_sub_type` INT DEFAULT NULL COMMENT '行为子类型',
  `severity_level` INT DEFAULT 2 COMMENT '行为严重程度 1-低风险 2-中风险 3-高风险 4-极高风险',
  `confidence_score` DECIMAL(5,2) DEFAULT NULL COMMENT '置信度(0-100)',
  `target_count` INT DEFAULT NULL COMMENT '检测目标数量',
  `target_ids` TEXT DEFAULT NULL COMMENT '目标ID列表(JSON格式)',

  `person_id` BIGINT DEFAULT NULL COMMENT '人员ID',
  `person_code` VARCHAR(50) DEFAULT NULL COMMENT '人员编号',
  `person_name` VARCHAR(100) DEFAULT NULL COMMENT '人员姓名',
  `behavior_description` TEXT DEFAULT NULL COMMENT '行为描述',
  `behavior_regions` TEXT DEFAULT NULL COMMENT '行为区域坐标(JSON格式)',
  `duration_seconds` BIGINT DEFAULT NULL COMMENT '持续时间(秒)',
  `start_time` DATETIME(3) DEFAULT NULL COMMENT '行为开始时间',
  `end_time` DATETIME(3) DEFAULT NULL COMMENT '行为结束时间',

  `scene_image_url` VARCHAR(500) DEFAULT NULL COMMENT '场景图片URL',
  `scene_thumbnail_url` VARCHAR(500) DEFAULT NULL COMMENT '场景缩略图URL',
  `snapshot_urls` TEXT DEFAULT NULL COMMENT '行为快照URL列表(逗号分隔)',
  `video_segment_url` VARCHAR(500) DEFAULT NULL COMMENT '视频片段URL',
  `video_thumbnail_url` VARCHAR(500) DEFAULT NULL COMMENT '视频片段缩略图URL',

  `detection_algorithm` INT DEFAULT NULL COMMENT '检测算法类型 1-YOLOv5 2-YOLOv7 3-SSD 4-ResNet 5-Mask R-CNN 6-自定义算法',
  `detection_model` VARCHAR(100) DEFAULT NULL COMMENT '检测模型版本',
  `detection_duration` BIGINT DEFAULT NULL COMMENT '检测耗时(毫秒)',

  `alarm_triggered` INT DEFAULT 0 COMMENT '告警标志 0-未触发 1-已触发',
  `alarm_types` VARCHAR(500) DEFAULT NULL COMMENT '告警类型(逗号分隔)',
  `alarm_level` INT DEFAULT NULL COMMENT '告警级别 1-提示 2-重要 3-紧急 4-严重',
  `need_manual_confirm` INT DEFAULT 0 COMMENT '是否需要人工确认 0-不需要 1-需要',

  `process_status` INT DEFAULT 0 COMMENT '处理状态 0-未处理 1-处理中 2-已处理 3-已忽略 4-已转交',
  `process_time` DATETIME(3) DEFAULT NULL COMMENT '处理时间',
  `process_duration_minutes` BIGINT DEFAULT NULL COMMENT '处理耗时(分钟)',
  `process_user_id` BIGINT DEFAULT NULL COMMENT '处理人ID',
  `process_user_name` VARCHAR(100) DEFAULT NULL COMMENT '处理人姓名',
  `process_remark` TEXT DEFAULT NULL COMMENT '处理备注',

  `related_event_ids` VARCHAR(500) DEFAULT NULL COMMENT '关联事件ID列表(JSON格式)',
  `behavior_tags` VARCHAR(500) DEFAULT NULL COMMENT '行为标签(逗号分隔)',
  `impact_level` INT DEFAULT NULL COMMENT '影响等级 1-轻微影响 2-一般影响 3-重要影响 4-严重影响',
  `risk_level` INT DEFAULT 3 COMMENT '风险等级 1-低风险 2-中风险 3-高风险 4-极高风险',
  `process_priority` INT DEFAULT 2 COMMENT '处理优先级 1-低 2-中 3-高 4-紧急',

  `environment_info` TEXT DEFAULT NULL COMMENT '环境信息(JSON格式)',
  `lighting_condition` VARCHAR(50) DEFAULT NULL COMMENT '光照条件',
  `weather_condition` VARCHAR(50) DEFAULT NULL COMMENT '天气状况',
  `temperature` INT DEFAULT NULL COMMENT '温度(摄氏度)',

  `extended_attributes` TEXT DEFAULT NULL COMMENT '扩展属性(JSON格式)',
  `create_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  `last_modified_by_id` BIGINT DEFAULT NULL COMMENT '最后修改人ID',
  `last_modified_by_name` VARCHAR(100) DEFAULT NULL COMMENT '最后修改人姓名',

  `is_read` BOOLEAN DEFAULT FALSE COMMENT '是否已读',
  `read_time` DATETIME(3) DEFAULT NULL COMMENT '阅读时间',
  `is_archived` BOOLEAN DEFAULT FALSE COMMENT '是否已归档',
  `archived_time` DATETIME(3) DEFAULT NULL COMMENT '归档时间',
  `is_exported` BOOLEAN DEFAULT FALSE COMMENT '是否已导出',
  `exported_time` DATETIME(3) DEFAULT NULL COMMENT '导出时间',

  `ai_model_id` VARCHAR(100) DEFAULT NULL COMMENT '关联AI模型ID',
  `ai_model_name` VARCHAR(200) DEFAULT NULL COMMENT '关联AI模型名称',

  `deleted_flag` TINYINT DEFAULT 0 COMMENT '删除标志 0-未删除 1-已删除',
  `version` INT DEFAULT 0 COMMENT '乐观锁版本号',

  PRIMARY KEY (`behavior_id`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_detection_time` (`detection_time`),
  KEY `idx_behavior_type` (`behavior_type`),
  KEY `idx_severity_level` (`severity_level`),
  KEY `idx_process_status` (`process_status`),
  KEY `idx_alarm_triggered` (`alarm_triggered`),
  KEY `idx_need_manual_confirm` (`need_manual_confirm`),
  KEY `idx_person_id` (`person_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_deleted_flag` (`deleted_flag`),
  KEY `idx_device_time_type` (`device_id`, `detection_time`, `behavior_type`),
  KEY `idx_severity_status` (`severity_level`, `process_status`),

  UNIQUE KEY `uk_behavior_detection` (`device_id`, `detection_time`, `behavior_type`, `deleted_flag`),

  CONSTRAINT `fk_behavior_device` FOREIGN KEY (`device_id`) REFERENCES `t_video_device` (`device_id`) ON DELETE CASCADE,

  CHECK (`behavior_type` BETWEEN 1 AND 7),
  CHECK (`severity_level` BETWEEN 1 AND 4),
  CHECK (`confidence_score` BETWEEN 0 AND 100),
  CHECK (`target_count` >= 0),
  CHECK (`duration_seconds` >= 0),
  CHECK (`process_status` BETWEEN 0 AND 4),
  CHECK (`alarm_triggered` IN (0, 1)),
  CHECK (`need_manual_confirm` IN (0, 1)),
  CHECK (`process_duration_minutes` >= 0),
  CHECK (`impact_level` BETWEEN 1 AND 4),
  CHECK (`risk_level` BETWEEN 1 AND 4),
  CHECK (`process_priority` BETWEEN 1 AND 4),
  CHECK (`deleted_flag` IN (0, 1)),
  CHECK (`version` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频行为检测记录表';

-- 创建行为模式表
CREATE TABLE `t_video_behavior_pattern` (
  `pattern_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '模式ID',
  `pattern_name` VARCHAR(100) NOT NULL COMMENT '模式名称',
  `pattern_type` INT NOT NULL COMMENT '模式类型 1-时间模式 2-空间模式 3-行为模式 4-异常模式 5-预测模式 6-自定义模式',
  `pattern_description` TEXT DEFAULT NULL COMMENT '模式描述',

  `behavior_type` INT NOT NULL COMMENT '适用行为类型',
  `behavior_sub_type` INT DEFAULT NULL COMMENT '适用行为子类型',
  `pattern_rules` TEXT NOT NULL COMMENT '模式规则(JSON格式)',
  `trigger_conditions` TEXT DEFAULT NULL COMMENT '触发条件(JSON格式)',
  `threshold_settings` TEXT DEFAULT NULL COMMENT '阈值设置(JSON格式)',

  `pattern_status` INT DEFAULT 1 COMMENT '模式状态 1-启用 2-禁用 3-测试 4-维护',
  `pattern_priority` INT DEFAULT 2 COMMENT '模式优先级 1-最高 2-高 3-中 4-低',
  `algorithm_model_id` VARCHAR(100) DEFAULT NULL COMMENT '算法模型ID',

  `training_samples` BIGINT DEFAULT 0 COMMENT '训练数据样本数',
  `training_accuracy` DECIMAL(5,2) DEFAULT NULL COMMENT '训练准确率(0-100)',
  `validation_accuracy` DECIMAL(5,2) DEFAULT NULL COMMENT '验证准确率(0-100)',
  `false_positive_rate` DECIMAL(5,2) DEFAULT NULL COMMENT '误报率(0-100)',
  `false_negative_rate` DECIMAL(5,2) DEFAULT NULL COMMENT '漏报率(0-100)',

  `last_training_time` DATETIME(3) DEFAULT NULL COMMENT '最后训练时间',
  `next_training_time` DATETIME(3) DEFAULT NULL COMMENT '下次训练时间',
  `training_interval_days` INT DEFAULT 30 COMMENT '训练间隔天数',
  `pattern_version` VARCHAR(50) DEFAULT '1.0.0' COMMENT '模式版本',

  `applicable_device_types` TEXT DEFAULT NULL COMMENT '适用设备类型列表(JSON格式)',
  `applicable_areas` TEXT DEFAULT NULL COMMENT '适用区域ID列表(JSON格式)',
  `applicable_time_ranges` TEXT DEFAULT NULL COMMENT '适用时间段列表(JSON格式)',
  `valid_start_time` DATETIME(3) DEFAULT NULL COMMENT '有效期开始时间',
  `valid_end_time` DATETIME(3) DEFAULT NULL COMMENT '有效期结束时间',

  `maintained_by` BIGINT DEFAULT NULL COMMENT '维护人ID',
  `maintained_by_name` VARCHAR(100) DEFAULT NULL COMMENT '维护人姓名',
  `pattern_tags` VARCHAR(500) DEFAULT NULL COMMENT '模式标签(逗号分隔)',
  `performance_metrics` TEXT DEFAULT NULL COMMENT '性能指标(JSON格式)',
  `usage_statistics` TEXT DEFAULT NULL COMMENT '使用统计(JSON格式)',

  `auto_training` BOOLEAN DEFAULT FALSE COMMENT '是否自动训练',
  `real_time_monitoring` BOOLEAN DEFAULT TRUE COMMENT '是否实时监控',
  `enable_alarm` BOOLEAN DEFAULT TRUE COMMENT '是否启用告警',
  `alarm_threshold` DECIMAL(5,2) DEFAULT 80.00 COMMENT '告警阈值',
  `generate_reports` BOOLEAN DEFAULT FALSE COMMENT '是否生成报告',
  `report_generation_cycle` INT DEFAULT 7 COMMENT '报告生成周期(天)',

  `complexity_level` VARCHAR(20) DEFAULT '中等' COMMENT '模式复杂度 简单|中等|复杂|极复杂',
  `test_cases` TEXT DEFAULT NULL COMMENT '测试用例',
  `version_description` VARCHAR(500) DEFAULT NULL COMMENT '版本说明',
  `remark` TEXT DEFAULT NULL COMMENT '备注信息',

  `create_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  `created_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `updated_by` BIGINT DEFAULT NULL COMMENT '更新人ID',

  `deleted_flag` TINYINT DEFAULT 0 COMMENT '删除标志 0-未删除 1-已删除',
  `version` INT DEFAULT 0 COMMENT '乐观锁版本号',

  PRIMARY KEY (`pattern_id`),
  KEY `idx_pattern_name` (`pattern_name`),
  KEY `idx_pattern_type` (`pattern_type`),
  KEY `idx_behavior_type` (`behavior_type`),
  KEY `idx_pattern_status` (`pattern_status`),
  KEY `idx_pattern_priority` (`pattern_priority`),
  KEY `idx_algorithm_model_id` (`algorithm_model_id`),
  KEY `idx_next_training_time` (`next_training_time`),
  KEY `idx_valid_time_range` (`valid_start_time`, `valid_end_time`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_deleted_flag` (`deleted_flag`),
  KEY `idx_status_priority` (`pattern_status`, `pattern_priority`),
  KEY `idx_type_status` (`pattern_type`, `pattern_status`),

  UNIQUE KEY `uk_pattern_name` (`pattern_name`, `deleted_flag`),

  CHECK (`pattern_type` BETWEEN 1 AND 6),
  CHECK (`behavior_type` BETWEEN 1 AND 7),
  CHECK (`pattern_status` BETWEEN 1 AND 4),
  CHECK (`pattern_priority` BETWEEN 1 AND 4),
  CHECK (`training_samples` >= 0),
  CHECK (`training_accuracy` BETWEEN 0 AND 100),
  CHECK (`validation_accuracy` BETWEEN 0 AND 100),
  CHECK (`false_positive_rate` BETWEEN 0 AND 100),
  CHECK (`false_negative_rate` BETWEEN 0 AND 100),
  CHECK (`training_interval_days` > 0),
  CHECK (`report_generation_cycle` > 0),
  CHECK (`deleted_flag` IN (0, 1)),
  CHECK (`version` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频行为模式表';

-- 创建行为检测统计表
CREATE TABLE `t_video_behavior_statistics` (
  `stat_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '统计ID',
  `stat_date` DATE NOT NULL COMMENT '统计日期',
  `stat_type` VARCHAR(20) NOT NULL COMMENT '统计类型 daily|weekly|monthly',
  `device_id` BIGINT DEFAULT NULL COMMENT '设备ID(空表示全设备)',
  `behavior_type` INT DEFAULT NULL COMMENT '行为类型(空表示全部)',

  `total_count` BIGINT DEFAULT 0 COMMENT '总检测数',
  `alarm_count` BIGINT DEFAULT 0 COMMENT '告警数量',
  `high_risk_count` BIGINT DEFAULT 0 COMMENT '高风险数量',
  `processed_count` BIGINT DEFAULT 0 COMMENT '已处理数量',
  `pending_count` BIGINT DEFAULT 0 COMMENT '待处理数量',

  `avg_confidence_score` DECIMAL(5,2) DEFAULT NULL COMMENT '平均置信度',
  `avg_duration_seconds` BIGINT DEFAULT NULL COMMENT '平均持续时间(秒)',
  `max_confidence_score` DECIMAL(5,2) DEFAULT NULL COMMENT '最高置信度',
  `min_confidence_score` DECIMAL(5,2) DEFAULT NULL COMMENT '最低置信度',

  `pattern_match_count` BIGINT DEFAULT 0 COMMENT '模式匹配数量',
  `false_positive_count` BIGINT DEFAULT 0 COMMENT '误报数量',
  `false_negative_count` BIGINT DEFAULT 0 COMMENT '漏报数量',

  `detection_efficiency` DECIMAL(5,2) DEFAULT NULL COMMENT '检测效率',
  `processing_efficiency` DECIMAL(5,2) DEFAULT NULL COMMENT '处理效率',
  `accuracy_rate` DECIMAL(5,2) DEFAULT NULL COMMENT '准确率',

  `additional_metrics` TEXT DEFAULT NULL COMMENT '其他指标(JSON格式)',
  `create_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `update_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',

  PRIMARY KEY (`stat_id`),
  UNIQUE KEY `uk_statistics` (`stat_date`, `stat_type`, `device_id`, `behavior_type`),
  KEY `idx_stat_date` (`stat_date`),
  KEY `idx_stat_type` (`stat_type`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_behavior_type` (`behavior_type`),
  KEY `idx_create_time` (`create_time`),

  CHECK (`total_count` >= 0),
  CHECK (`alarm_count` >= 0),
  CHECK (`high_risk_count` >= 0),
  CHECK (`processed_count` >= 0),
  CHECK (`pending_count` >= 0),
  CHECK (`avg_confidence_score` BETWEEN 0 AND 100),
  CHECK (`avg_duration_seconds` >= 0),
  CHECK (`max_confidence_score` BETWEEN 0 AND 100),
  CHECK (`min_confidence_score` BETWEEN 0 AND 100),
  CHECK (`pattern_match_count` >= 0),
  CHECK (`false_positive_count` >= 0),
  CHECK (`false_negative_count` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频行为检测统计表';

-- 创建行为检测索引表（用于优化查询性能）
CREATE TABLE `t_video_behavior_index` (
  `index_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '索引ID',
  `behavior_id` BIGINT NOT NULL COMMENT '行为ID',
  `device_id` BIGINT NOT NULL COMMENT '设备ID',
  `detection_time` DATETIME(3) NOT NULL COMMENT '检测时间',
  `behavior_type` INT NOT NULL COMMENT '行为类型',
  `severity_level` INT NOT NULL COMMENT '严重程度',
  `confidence_score` DECIMAL(5,2) NOT NULL COMMENT '置信度',
  `alarm_triggered` INT NOT NULL COMMENT '告警标志',
  `process_status` INT NOT NULL COMMENT '处理状态',
  `person_id` BIGINT DEFAULT NULL COMMENT '人员ID',

  `time_bucket` VARCHAR(20) NOT NULL COMMENT '时间桶(YYYYMMDDHH)',
  `type_severity_bucket` VARCHAR(20) NOT NULL COMMENT '类型严重程度桶',
  `alarm_status_bucket` VARCHAR(20) NOT NULL COMMENT '告警状态桶',

  `create_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',

  PRIMARY KEY (`index_id`),
  KEY `idx_behavior_id` (`behavior_id`),
  KEY `idx_device_time` (`device_id`, `detection_time`),
  KEY `idx_time_bucket` (`time_bucket`),
  KEY `idx_type_severity_bucket` (`type_severity_bucket`),
  KEY `idx_alarm_status_bucket` (`alarm_status_bucket`),
  KEY `idx_person_id` (`person_id`),
  KEY `idx_severity_level` (`severity_level`),
  KEY `idx_process_status` (`process_status`),
  KEY `idx_confidence_score` (`confidence_score`),

  UNIQUE KEY `uk_behavior_index` (`behavior_id`),

  FOREIGN KEY (`behavior_id`) REFERENCES `t_video_behavior` (`behavior_id`) ON DELETE CASCADE,
  FOREIGN KEY (`device_id`) REFERENCES `t_video_device` (`device_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频行为检测索引表';

-- 插入行为类型基础数据
INSERT INTO `t_system_dict_type` (`type_code`, `type_name`, `type_desc`, `status`, `sort`, `deleted_flag`, `create_time`, `update_time`) VALUES
('VIDEO_BEHAVIOR_TYPE', '视频行为类型', '视频检测的行为类型分类', 1, 10, 0, NOW(), NOW());

INSERT INTO `t_system_dict_data` (`type_code`, `dict_code`, `dict_name`, `dict_desc`, `dict_value`, `status`, `sort`, `deleted_flag`, `create_time`, `update_time`) VALUES
('VIDEO_BEHAVIOR_TYPE', 'PERSON_DETECTION', '人员检测', '检测到人员出现', '1', 1, 1, 0, NOW(), NOW()),
('VIDEO_BEHAVIOR_TYPE', 'VEHICLE_DETECTION', '车辆检测', '检测到车辆出现', '2', 2, 1, 0, NOW(), NOW()),
('VIDEO_BEHAVIOR_TYPE', 'OBJECT_DETECTION', '物体检测', '检测到物体出现', '3', 3, 1, 0, NOW(), NOW()),
('VIDEO_BEHAVIOR_TYPE', 'FACE_DETECTION', '人脸检测', '检测到人脸出现', '4', 4, 1, 0, NOW(), NOW()),
('VIDEO_BEHAVIOR_TYPE', 'ABNORMAL_BEHAVIOR', '异常行为', '检测到异常行为', '5', 5, 1, 0, NOW(), NOW()),
('VIDEO_BEHAVIOR_TYPE', 'NORMAL_BEHAVIOR', '正常行为', '检测到正常行为', '6', 6, 1, 0, NOW(), NOW()),
('VIDEO_BEHAVIOR_TYPE', 'OTHER_BEHAVIOR', '其他行为', '其他类型的行为', '7', 7, 1, 0, NOW(), NOW());

-- 插入异常行为子类型基础数据
INSERT INTO `t_system_dict_type` (`type_code`, `type_name`, `type_desc`, `status`, `sort`, `deleted_flag`, `create_time`, `update_time`) VALUES
('ABNORMAL_BEHAVIOR_SUBTYPE', '异常行为子类型', '异常行为的具体子分类', 1, 11, 0, NOW(), NOW());

INSERT INTO `t_system_dict_data` (`type_code`, `dict_code`, `dict_name`, `dict_desc`, `dict_value`, `status`, `sort`, `deleted_flag`, `create_time`, `update_time`) VALUES
('ABNORMAL_BEHAVIOR_SUBTYPE', 'LOITERING', '人员徘徊', '人员在特定区域徘徊', '1', 1, 1, 0, NOW(), NOW()),
('ABNORMAL_BEHAVIOR_SUBTYPE', 'ABNORMAL_GATHERING', '异常聚集', '人员异常聚集', '2', 2, 1, 0, NOW(), NOW()),
('ABNORMAL_BEHAVIOR_SUBTYPE', 'RETROGRADE', '逆行检测', '人员逆行移动', '3', 3, 1, 0, NOW(), NOW()),
('ABNORMAL_BEHAVIOR_SUBTYPE', 'AREA_INTRUSION', '区域入侵', '人员进入限制区域', '4', 4, 1, 0, NOW(), NOW()),
('ABNORMAL_BEHAVIOR_SUBTYPE', 'ABANDONED_OBJECT', '物品遗留', '检测到遗留物品', '5', 5, 1, 0, NOW(), NOW()),
('ABNORMAL_BEHAVIOR_SUBTYPE', 'FIGHTING', '打架斗殴', '检测到打架斗殴', '6', 6, 1, 0, NOW(), NOW()),
('ABNORMAL_BEHAVIOR_SUBTYPE', 'FALL_DOWN', '倒地检测', '人员倒地检测', '7', 7, 1, 0, NOW(), NOW()),
('ABNORMAL_BEHAVIOR_SUBTYPE', 'FAST_RUNNING', '快速奔跑', '人员快速奔跑', '8', 8, 1, 0, NOW(), NOW());

-- 插入严重程度基础数据
INSERT INTO `t_system_dict_type` (`type_code`, `type_name`, `type_desc`, `status`, `sort`, `deleted_flag`, `create_time`, `update_time`) VALUES
('BEHAVIOR_SEVERITY', '行为严重程度', '行为检测的严重程度分类', 1, 12, 0, NOW(), NOW());

INSERT INTO `t_system_dict_data` (`type_code`, `dict_code`, `dict_name`, `dict_desc`, `dict_value`, `status`, `sort`, `deleted_flag`, `create_time`, `update_time`) VALUES
('BEHAVIOR_SEVERITY', 'LOW_RISK', '低风险', '低风险行为', '1', 1, 1, 0, NOW(), NOW()),
('BEHAVIOR_SEVERITY', 'MEDIUM_RISK', '中风险', '中风险行为', '2', 2, 1, 0, NOW(), NOW()),
('BEHAVIOR_SEVERITY', 'HIGH_RISK', '高风险', '高风险行为', '3', 3, 1, 0, NOW(), NOW()),
('BEHAVIOR_SEVERITY', 'EXTREME_RISK', '极高风险', '极高风险行为', '4', 4, 1, 0, NOW(), NOW());

-- 插入检测算法类型基础数据
INSERT INTO `t_system_dict_type` (`type_code`, `type_name`, `type_desc`, `status`, `sort`, `deleted_flag`, `create_time`, `update_time`) VALUES
('DETECTION_ALGORITHM', '检测算法类型', '视频行为检测使用的算法类型', 1, 13, 0, NOW(), NOW());

INSERT INTO `t_system_dict_data` (`type_code`, `dict_code`, `dict_name`, `dict_desc`, `dict_value`, `status`, `sort`, `deleted_flag`, `create_time`, `update_time`) VALUES
('DETECTION_ALGORITHM', 'YOLOV5', 'YOLOv5', 'YOLOv5目标检测算法', '1', 1, 1, 0, NOW(), NOW()),
('DETECTION_ALGORITHM', 'YOLOV7', 'YOLOv7', 'YOLOv7目标检测算法', '2', 2, 1, 0, NOW(), NOW()),
('DETECTION_ALGORITHM', 'SSD', 'SSD', 'SSD目标检测算法', '3', 3, 1, 0, NOW(), NOW()),
('DETECTION_ALGORITHM', 'RESNET', 'ResNet', 'ResNet深度学习算法', '4', 4, 1, 0, NOW(), NOW()),
('DETECTION_ALGORITHM', 'MASK_RCNN', 'Mask R-CNN', 'Mask R-CNN实例分割算法', '5', 5, 1, 0, NOW(), NOW()),
('DETECTION_ALGORITHM', 'CUSTOM', '自定义算法', '用户自定义的检测算法', '6', 6, 1, 0, NOW(), NOW());

-- 插入处理状态基础数据
INSERT INTO `t_system_dict_type` (`type_code`, `type_name`, `type_desc`, `status`, `sort`, `deleted_flag`, `create_time`, `update_time`) VALUES
('BEHAVIOR_PROCESS_STATUS', '行为处理状态', '行为检测记录的处理状态', 1, 14, 0, NOW(), NOW());

INSERT INTO `t_system_dict_data` (`type_code`, `dict_code`, `dict_name`, `dict_desc`, `dict_value`, `status`, `sort`, `deleted_flag`, `create_time`, `update_time`) VALUES
('BEHAVIOR_PROCESS_STATUS', 'UNPROCESSED', '未处理', '尚未处理的行为记录', '0', 1, 1, 0, NOW(), NOW()),
('BEHAVIOR_PROCESS_STATUS', 'PROCESSING', '处理中', '正在处理的行为记录', '1', 2, 1, 0, NOW(), NOW()),
('BEHAVIOR_PROCESS_STATUS', 'PROCESSED', '已处理', '已经处理完成的行为记录', '2', 3, 1, 0, NOW(), NOW()),
('BEHAVIOR_PROCESS_STATUS', 'IGNORED', '已忽略', '被忽略的行为记录', '3', 4, 1, 0, NOW(), NOW()),
('BEHAVIOR_PROCESS_STATUS', 'TRANSFERRED', '已转交', '已转交给其他人员处理', '4', 5, 1, 0, NOW(), NOW());

-- 添加分区表支持（如果MySQL版本支持）
-- 按月分区t_video_behavior表，提高查询性能
-- ALTER TABLE `t_video_behavior`
-- PARTITION BY RANGE (TO_DAYS(detection_time)) (
--   PARTITION p_202512 VALUES LESS THAN (TO_DAYS('2026-01-01')),
--   PARTITION p_202601 VALUES LESS THAN (TO_DAYS('2026-02-01')),
--   PARTITION p_202602 VALUES LESS THAN (TO_DAYS('2026-03-01')),
--   PARTITION p_202603 VALUES LESS THAN (TO_DAYS('2026-04-01')),
--   PARTITION p_future VALUES LESS THAN MAXVALUE
-- );

-- 创建存储过程：自动统计数据
DELIMITER //
CREATE PROCEDURE `sp_update_behavior_statistics`(
    IN p_stat_date DATE,
    IN p_stat_type VARCHAR(20)
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    -- 删除已存在的统计记录
    DELETE FROM `t_video_behavior_statistics`
    WHERE `stat_date` = p_stat_date AND `stat_type` = p_stat_type;

    -- 插入全设备统计数据
    INSERT INTO `t_video_behavior_statistics`
    (`stat_date`, `stat_type`, `device_id`, `behavior_type`,
     `total_count`, `alarm_count`, `high_risk_count`, `processed_count`, `pending_count`,
     `avg_confidence_score`, `avg_duration_seconds`)
    SELECT
        p_stat_date,
        p_stat_type,
        NULL as device_id,
        NULL as behavior_type,
        COUNT(*) as total_count,
        SUM(CASE WHEN alarm_triggered = 1 THEN 1 ELSE 0 END) as alarm_count,
        SUM(CASE WHEN severity_level >= 3 THEN 1 ELSE 0 END) as high_risk_count,
        SUM(CASE WHEN process_status = 2 THEN 1 ELSE 0 END) as processed_count,
        SUM(CASE WHEN process_status = 0 THEN 1 ELSE 0 END) as pending_count,
        ROUND(AVG(confidence_score), 2) as avg_confidence_score,
        ROUND(AVG(duration_seconds), 0) as avg_duration_seconds
    FROM `t_video_behavior`
    WHERE DATE(detection_time) = p_stat_date
      AND deleted_flag = 0
    GROUP BY DATE(detection_time);

    -- 插入按设备分类的统计数据
    INSERT INTO `t_video_behavior_statistics`
    (`stat_date`, `stat_type`, `device_id`, `behavior_type`,
     `total_count`, `alarm_count`, `high_risk_count`)
    SELECT
        p_stat_date,
        p_stat_type,
        device_id,
        NULL as behavior_type,
        COUNT(*) as total_count,
        SUM(CASE WHEN alarm_triggered = 1 THEN 1 ELSE 0 END) as alarm_count,
        SUM(CASE WHEN severity_level >= 3 THEN 1 ELSE 0 END) as high_risk_count
    FROM `t_video_behavior`
    WHERE DATE(detection_time) = p_stat_date
      AND deleted_flag = 0
    GROUP BY device_id, DATE(detection_time);

    -- 插入按行为类型分类的统计数据
    INSERT INTO `t_video_behavior_statistics`
    (`stat_date`, `stat_type`, `device_id`, `behavior_type`,
     `total_count`, `avg_confidence_score`)
    SELECT
        p_stat_date,
        p_stat_type,
        NULL as device_id,
        behavior_type,
        COUNT(*) as total_count,
        ROUND(AVG(confidence_score), 2) as avg_confidence_score
    FROM `t_video_behavior`
    WHERE DATE(detection_time) = p_stat_date
      AND deleted_flag = 0
    GROUP BY behavior_type, DATE(detection_time);

    COMMIT;
END //
DELIMITER ;

-- 创建定时任务：每天凌晨1点更新前一天的行为统计
-- 注意：这需要配合定时任务框架使用，如Spring Task、Quartz等
-- 示例cron表达式：0 0 1 * * ?