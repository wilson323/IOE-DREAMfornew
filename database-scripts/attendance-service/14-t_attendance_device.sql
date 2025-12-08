-- ==================================================================
-- 考勤服务 - 考勤设备表 (14-t_attendance_device.sql)
-- ==================================================================
-- 表名: t_attendance_device
-- 说明: 存储考勤设备信息,包括考勤机、人脸识别设备、指纹设备、移动打卡设备等
-- 依赖: 关联t_common_device(公共设备表),考勤服务特定配置
-- 作者: IOE-DREAM团队
-- 创建时间: 2025-12-08
-- 版本: v1.0.0
-- ==================================================================

USE `ioedream_attendance`;

-- ==================================================================
-- 删除已存在的表(如果存在)
-- ==================================================================
DROP TABLE IF EXISTS `t_attendance_device`;

-- ==================================================================
-- 创建表: t_attendance_device
-- ==================================================================
CREATE TABLE IF NOT EXISTS `t_attendance_device` (
    -- ==============================================================
    -- 主键和唯一标识
    -- ==============================================================
    `device_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '设备ID(主键)',
    `device_code` VARCHAR(100) NOT NULL COMMENT '设备编码(唯一标识,如:ATD_001)',
    `common_device_id` BIGINT COMMENT '公共设备表ID(关联t_common_device)',

    -- ==============================================================
    -- 基本信息
    -- ==============================================================
    `device_name` VARCHAR(200) NOT NULL COMMENT '设备名称(如:一楼前台考勤机)',
    `device_name_en` VARCHAR(200) COMMENT '设备英文名称',
    `device_type` VARCHAR(50) NOT NULL DEFAULT 'FINGERPRINT' COMMENT '设备类型(FINGERPRINT-指纹,FACE-人脸,IRIS-虹膜,CARD-刷卡,MOBILE-移动端,GPS-GPS定位,HYBRID-混合)',
    `device_category` VARCHAR(50) NOT NULL DEFAULT 'STANDALONE' COMMENT '设备分类(STANDALONE-独立设备,INTEGRATED-集成设备,VIRTUAL-虚拟设备)',

    -- ==============================================================
    -- 设备型号和厂商
    -- ==============================================================
    `manufacturer` VARCHAR(100) COMMENT '设备厂商(如:海康威视、大华)',
    `model` VARCHAR(100) COMMENT '设备型号(如:DS-K1T671M)',
    `serial_number` VARCHAR(100) COMMENT '设备序列号',
    `firmware_version` VARCHAR(50) COMMENT '固件版本',
    `software_version` VARCHAR(50) COMMENT '软件版本',

    -- ==============================================================
    -- 网络配置
    -- ==============================================================
    `ip_address` VARCHAR(50) COMMENT 'IP地址',
    `mac_address` VARCHAR(50) COMMENT 'MAC地址',
    `port` INT COMMENT '端口号',
    `network_type` VARCHAR(50) NOT NULL DEFAULT 'WIRED' COMMENT '网络类型(WIRED-有线,WIFI-无线,4G-4G网络,5G-5G网络)',
    `protocol_type` VARCHAR(50) COMMENT '协议类型(TCP,UDP,HTTP,MQTT)',
    `connection_mode` VARCHAR(50) NOT NULL DEFAULT 'ACTIVE' COMMENT '连接模式(ACTIVE-主动,PASSIVE-被动)',

    -- ==============================================================
    -- 安装位置
    -- ==============================================================
    `install_location` VARCHAR(500) COMMENT '安装位置(如:一楼前台大厅)',
    `install_location_detail` TEXT COMMENT '安装位置详情',
    `install_floor` VARCHAR(50) COMMENT '安装楼层',
    `install_area_id` BIGINT COMMENT '安装区域ID',
    `install_area_name` VARCHAR(200) COMMENT '安装区域名称',

    -- ==============================================================
    -- GPS位置(移动打卡设备)
    -- ==============================================================
    `has_gps` TINYINT NOT NULL DEFAULT 0 COMMENT '是否支持GPS(0-否,1-是)',
    `longitude` DECIMAL(12,8) COMMENT '经度',
    `latitude` DECIMAL(12,8) COMMENT '纬度',
    `gps_accuracy` INT COMMENT 'GPS精度(米)',
    `allow_gps_range` INT NOT NULL DEFAULT 100 COMMENT '允许GPS误差范围(米)',

    -- ==============================================================
    -- 设备功能配置
    -- ==============================================================
    `support_clock_in` TINYINT NOT NULL DEFAULT 1 COMMENT '是否支持上班打卡(0-否,1-是)',
    `support_clock_out` TINYINT NOT NULL DEFAULT 1 COMMENT '是否支持下班打卡(0-否,1-是)',
    `support_out_go` TINYINT NOT NULL DEFAULT 0 COMMENT '是否支持外出打卡(0-否,1-是)',
    `support_out_return` TINYINT NOT NULL DEFAULT 0 COMMENT '是否支持返回打卡(0-否,1-是)',

    `support_photo` TINYINT NOT NULL DEFAULT 0 COMMENT '是否支持拍照(0-否,1-是)',
    `photo_required` TINYINT NOT NULL DEFAULT 0 COMMENT '是否必须拍照(0-否,1-是)',
    `support_temperature` TINYINT NOT NULL DEFAULT 0 COMMENT '是否支持测温(0-否,1-是)',
    `temperature_required` TINYINT NOT NULL DEFAULT 0 COMMENT '是否必须测温(0-否,1-是)',

    -- ==============================================================
    -- 识别配置
    -- ==============================================================
    `recognition_threshold` DECIMAL(5,2) NOT NULL DEFAULT 85.00 COMMENT '识别阈值(百分比,如:85.00)',
    `recognition_timeout` INT NOT NULL DEFAULT 3000 COMMENT '识别超时时间(毫秒)',
    `max_retry_count` INT NOT NULL DEFAULT 3 COMMENT '最大重试次数',
    `allow_stranger` TINYINT NOT NULL DEFAULT 0 COMMENT '是否允许陌生人打卡(0-否,1-是)',

    -- ==============================================================
    -- 设备容量
    -- ==============================================================
    `max_user_count` INT COMMENT '最大用户数(设备支持的最大人员数)',
    `current_user_count` INT NOT NULL DEFAULT 0 COMMENT '当前用户数',
    `max_record_count` INT COMMENT '最大记录数(设备存储的最大记录数)',
    `current_record_count` INT NOT NULL DEFAULT 0 COMMENT '当前记录数',
    `storage_capacity` VARCHAR(50) COMMENT '存储容量(如:8GB)',
    `storage_used` VARCHAR(50) COMMENT '已用存储',

    -- ==============================================================
    -- 工作模式
    -- ==============================================================
    `work_mode` VARCHAR(50) NOT NULL DEFAULT 'ONLINE' COMMENT '工作模式(ONLINE-在线,OFFLINE-离线,HYBRID-混合)',
    `offline_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用离线模式(0-否,1-是)',
    `auto_sync_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否自动同步(0-否,1-是)',
    `sync_interval` INT NOT NULL DEFAULT 60 COMMENT '同步间隔(秒)',
    `last_sync_time` DATETIME COMMENT '最后同步时间',

    -- ==============================================================
    -- 适用范围
    -- ==============================================================
    `applicable_scope` JSON COMMENT '适用范围(JSON对象,包含部门、职位、员工等)',
    `department_ids` JSON COMMENT '适用部门ID集合(JSON数组,为空表示全部)',
    `position_ids` JSON COMMENT '适用职位ID集合(JSON数组)',
    `employee_ids` JSON COMMENT '指定员工ID集合(JSON数组)',
    `employee_type_filter` JSON COMMENT '员工类型过滤(JSON数组)',

    -- ==============================================================
    -- 工作时间
    -- ==============================================================
    `work_time_enabled` TINYINT NOT NULL DEFAULT 0 COMMENT '是否限制工作时间(0-否,1-是)',
    `work_start_time` TIME COMMENT '工作开始时间',
    `work_end_time` TIME COMMENT '工作结束时间',
    `work_days` JSON COMMENT '工作日配置(JSON数组,如:[1,2,3,4,5]表示周一到周五)',

    -- ==============================================================
    -- 设备状态
    -- ==============================================================
    `device_status` VARCHAR(50) NOT NULL DEFAULT 'OFFLINE' COMMENT '设备状态(ONLINE-在线,OFFLINE-离线,FAULT-故障,MAINTENANCE-维护中)',
    `online_status` TINYINT NOT NULL DEFAULT 0 COMMENT '在线状态(0-离线,1-在线)',
    `last_online_time` DATETIME COMMENT '最后在线时间',
    `last_offline_time` DATETIME COMMENT '最后离线时间',
    `online_duration` INT NOT NULL DEFAULT 0 COMMENT '在线时长(秒)',

    -- ==============================================================
    -- 健康检查
    -- ==============================================================
    `health_check_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用健康检查(0-否,1-是)',
    `health_check_interval` INT NOT NULL DEFAULT 300 COMMENT '健康检查间隔(秒)',
    `last_health_check_time` DATETIME COMMENT '最后健康检查时间',
    `health_status` VARCHAR(50) COMMENT '健康状态(HEALTHY-健康,WARNING-警告,ERROR-错误)',
    `health_score` INT COMMENT '健康评分(0-100)',

    -- ==============================================================
    -- 告警配置
    -- ==============================================================
    `alarm_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用告警(0-否,1-是)',
    `alarm_threshold` JSON COMMENT '告警阈值配置(JSON对象)',
    `alarm_contacts` JSON COMMENT '告警联系人(JSON数组)',
    `alarm_channels` JSON COMMENT '告警渠道(JSON数组,["短信","邮件","微信"])',

    -- ==============================================================
    -- 维护信息
    -- ==============================================================
    `maintenance_status` VARCHAR(50) NOT NULL DEFAULT 'NORMAL' COMMENT '维护状态(NORMAL-正常,SCHEDULED-计划维护,IN_PROGRESS-维护中)',
    `last_maintenance_time` DATETIME COMMENT '最后维护时间',
    `next_maintenance_time` DATETIME COMMENT '下次维护时间',
    `maintenance_interval_days` INT NOT NULL DEFAULT 90 COMMENT '维护间隔(天)',
    `maintenance_person` VARCHAR(100) COMMENT '维护负责人',

    -- ==============================================================
    -- 权限配置
    -- ==============================================================
    `require_authentication` TINYINT NOT NULL DEFAULT 1 COMMENT '是否需要认证(0-否,1-是)',
    `authentication_type` VARCHAR(50) COMMENT '认证类型(PASSWORD-密码,TOKEN-令牌,CERTIFICATE-证书)',
    `access_control_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用访问控制(0-否,1-是)',
    `admin_users` JSON COMMENT '管理员用户(JSON数组)',

    -- ==============================================================
    -- 统计信息
    -- ==============================================================
    `total_clock_count` INT NOT NULL DEFAULT 0 COMMENT '总打卡次数',
    `today_clock_count` INT NOT NULL DEFAULT 0 COMMENT '今日打卡次数',
    `success_count` INT NOT NULL DEFAULT 0 COMMENT '成功打卡次数',
    `fail_count` INT NOT NULL DEFAULT 0 COMMENT '失败打卡次数',
    `success_rate` DECIMAL(5,2) COMMENT '成功率(百分比)',
    `average_recognition_time` INT COMMENT '平均识别时间(毫秒)',

    -- ==============================================================
    -- 运行时间统计
    -- ==============================================================
    `total_runtime_hours` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '总运行时长(小时)',
    `uptime_rate` DECIMAL(5,2) COMMENT '在线率(百分比)',
    `fault_count` INT NOT NULL DEFAULT 0 COMMENT '故障次数',
    `last_fault_time` DATETIME COMMENT '最后故障时间',
    `last_fault_reason` TEXT COMMENT '最后故障原因',

    -- ==============================================================
    -- 扩展配置
    -- ==============================================================
    `extended_config` JSON COMMENT '扩展配置(JSON对象,设备特定配置)',
    `custom_fields` JSON COMMENT '自定义字段(JSON对象)',
    `metadata` JSON COMMENT '元数据(JSON对象)',
    `tags` JSON COMMENT '标签(JSON数组)',

    -- ==============================================================
    -- 备注和说明
    -- ==============================================================
    `remark` TEXT COMMENT '备注',
    `admin_note` TEXT COMMENT '管理员备注',
    `install_note` TEXT COMMENT '安装说明',
    `usage_guide` TEXT COMMENT '使用指南',

    -- ==============================================================
    -- 标准审计字段
    -- ==============================================================
    `version` INT NOT NULL DEFAULT 1 COMMENT '版本号(用于乐观锁)',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人用户ID',
    `update_user_id` BIGINT COMMENT '更新人用户ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记(0-未删除,1-已删除)',

    -- ==============================================================
    -- 主键约束
    -- ==============================================================
    PRIMARY KEY (`device_id`),

    -- ==============================================================
    -- 唯一索引
    -- ==============================================================
    UNIQUE KEY `uk_device_code` (`device_code`),
    UNIQUE KEY `uk_serial_number` (`serial_number`),

    -- ==============================================================
    -- 普通索引
    -- ==============================================================
    INDEX `idx_device_name` (`device_name`),
    INDEX `idx_device_type` (`device_type`),
    INDEX `idx_device_category` (`device_category`),
    INDEX `idx_device_status` (`device_status`),
    INDEX `idx_online_status` (`online_status`),
    INDEX `idx_install_area_id` (`install_area_id`),
    INDEX `idx_common_device_id` (`common_device_id`),
    INDEX `idx_ip_address` (`ip_address`),
    INDEX `idx_manufacturer` (`manufacturer`),

    -- ==============================================================
    -- 复合索引(优化常用查询)
    -- ==============================================================
    INDEX `idx_type_status` (`device_type`, `device_status`),
    INDEX `idx_category_status` (`device_category`, `device_status`),
    INDEX `idx_online_type` (`online_status`, `device_type`),
    INDEX `idx_area_status` (`install_area_id`, `device_status`),

    -- ==============================================================
    -- 时间索引(降序,优化最新数据查询)
    -- ==============================================================
    INDEX `idx_create_time` (`create_time` DESC),
    INDEX `idx_update_time` (`update_time` DESC),
    INDEX `idx_last_online_time` (`last_online_time` DESC),

    -- ==============================================================
    -- 逻辑删除索引
    -- ==============================================================
    INDEX `idx_deleted_flag` (`deleted_flag`)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤服务-考勤设备表';

-- ==================================================================
-- 初始化数据: 考勤设备示例(10条企业级示例)
-- ==================================================================

-- 1. 一楼前台人脸识别考勤机
INSERT INTO `t_attendance_device` (
    `device_code`, `device_name`, `device_type`, `device_category`,
    `manufacturer`, `model`, `serial_number`,
    `ip_address`, `mac_address`, `port`, `network_type`,
    `install_location`, `install_floor`, `install_area_name`,
    `support_clock_in`, `support_clock_out`, `support_photo`, `photo_required`,
    `recognition_threshold`, `max_user_count`, `current_user_count`,
    `work_mode`, `offline_enabled`, `auto_sync_enabled`, `sync_interval`,
    `device_status`, `online_status`, `last_online_time`,
    `health_check_enabled`, `health_status`, `health_score`,
    `total_clock_count`, `today_clock_count`, `success_count`, `success_rate`,
    `remark`
) VALUES (
    'ATD_001', '一楼前台人脸识别考勤机', 'FACE', 'STANDALONE',
    '海康威视', 'DS-K1T671M', 'HK20250001',
    '192.168.1.101', '00:11:22:33:44:55', 8000, 'WIRED',
    '一楼前台大厅', '1F', '前台区域',
    1, 1, 1, 1,
    85.00, 5000, 120,
    'ONLINE', 1, 1, 60,
    'ONLINE', 1, NOW(),
    1, 'HEALTHY', 95,
    15230, 58, 15100, 99.14,
    '主要用于员工上下班打卡,支持人脸识别和拍照功能'
);

-- 2. 二楼研发部指纹考勤机
INSERT INTO `t_attendance_device` (
    `device_code`, `device_name`, `device_type`, `device_category`,
    `manufacturer`, `model`, `serial_number`,
    `ip_address`, `mac_address`, `port`, `network_type`,
    `install_location`, `install_floor`, `install_area_name`,
    `support_clock_in`, `support_clock_out`,
    `recognition_threshold`, `max_user_count`, `current_user_count`,
    `work_mode`, `offline_enabled`, `auto_sync_enabled`,
    `device_status`, `online_status`, `last_online_time`,
    `health_check_enabled`, `health_status`, `health_score`,
    `total_clock_count`, `today_clock_count`, `success_count`, `success_rate`,
    `remark`
) VALUES (
    'ATD_002', '二楼研发部指纹考勤机', 'FINGERPRINT', 'STANDALONE',
    '中控智慧', 'ZK-F18', 'ZK20250002',
    '192.168.1.102', '00:11:22:33:44:56', 4370, 'WIRED',
    '二楼研发部入口', '2F', '研发区域',
    1, 1,
    80.00, 3000, 85,
    'ONLINE', 1, 1,
    'ONLINE', 1, NOW(),
    1, 'HEALTHY', 92,
    8750, 42, 8700, 99.43,
    '研发部专用指纹考勤机,支持离线打卡'
);

-- 3. 三楼销售部混合识别考勤机
INSERT INTO `t_attendance_device` (
    `device_code`, `device_name`, `device_type`, `device_category`,
    `manufacturer`, `model`, `serial_number`,
    `ip_address`, `mac_address`, `port`, `network_type`,
    `install_location`, `install_floor`, `install_area_name`,
    `support_clock_in`, `support_clock_out`, `support_photo`,
    `support_temperature`, `temperature_required`,
    `recognition_threshold`, `max_user_count`, `current_user_count`,
    `work_mode`, `auto_sync_enabled`,
    `device_status`, `online_status`, `last_online_time`,
    `health_status`, `health_score`,
    `total_clock_count`, `today_clock_count`, `success_count`,
    `remark`
) VALUES (
    'ATD_003', '三楼销售部混合识别考勤机', 'HYBRID', 'STANDALONE',
    '大华', 'DHI-ASI7213Y', 'DH20250003',
    '192.168.1.103', '00:11:22:33:44:57', 80, 'WIRED',
    '三楼销售部入口', '3F', '销售区域',
    1, 1, 1,
    1, 1,
    85.00, 2000, 65,
    'ONLINE', 1,
    'ONLINE', 1, NOW(),
    'HEALTHY', 98,
    6420, 35, 6380,
    '销售部专用,支持人脸+指纹双重识别,带测温功能'
);

-- 4. 移动端虚拟考勤设备(APP打卡)
INSERT INTO `t_attendance_device` (
    `device_code`, `device_name`, `device_type`, `device_category`,
    `install_location`,
    `support_clock_in`, `support_clock_out`, `support_out_go`, `support_out_return`,
    `support_photo`, `photo_required`,
    `has_gps`, `allow_gps_range`,
    `work_mode`, `offline_enabled`,
    `device_status`, `online_status`,
    `total_clock_count`, `today_clock_count`, `success_count`, `success_rate`,
    `remark`
) VALUES (
    'ATD_MOBILE_001', 'IOE-DREAM移动考勤系统', 'MOBILE', 'VIRTUAL',
    '移动端(全员可用)',
    1, 1, 1, 1,
    1, 1,
    1, 100,
    'ONLINE', 1,
    'ONLINE', 1,
    25680, 156, 25500, 99.30,
    '移动端虚拟考勤设备,支持GPS定位打卡和拍照'
);

-- 5. GPS定位打卡设备(外勤人员)
INSERT INTO `t_attendance_device` (
    `device_code`, `device_name`, `device_type`, `device_category`,
    `install_location`,
    `support_clock_in`, `support_clock_out`, `support_out_go`, `support_out_return`,
    `support_photo`, `photo_required`,
    `has_gps`, `longitude`, `latitude`, `gps_accuracy`, `allow_gps_range`,
    `work_mode`, `device_status`, `online_status`,
    `total_clock_count`, `today_clock_count`,
    `remark`
) VALUES (
    'ATD_GPS_001', 'GPS外勤打卡设备', 'GPS', 'VIRTUAL',
    '外勤区域(销售和服务人员)',
    1, 1, 1, 1,
    1, 1,
    1, 116.397458, 39.908816, 10, 200,
    'ONLINE', 'ONLINE', 1,
    3560, 28,
    'GPS定位打卡设备,用于外勤人员打卡,允许200米误差范围'
);

-- 6. 四楼客服部刷卡考勤机
INSERT INTO `t_attendance_device` (
    `device_code`, `device_name`, `device_type`, `device_category`,
    `manufacturer`, `model`, `serial_number`,
    `ip_address`, `mac_address`, `port`, `network_type`,
    `install_location`, `install_floor`, `install_area_name`,
    `support_clock_in`, `support_clock_out`,
    `max_user_count`, `current_user_count`,
    `work_mode`, `offline_enabled`, `auto_sync_enabled`,
    `device_status`, `online_status`, `last_online_time`,
    `health_status`, `health_score`,
    `total_clock_count`, `today_clock_count`,
    `remark`
) VALUES (
    'ATD_004', '四楼客服部刷卡考勤机', 'CARD', 'STANDALONE',
    '汉王', 'HW-C300', 'HW20250004',
    '192.168.1.104', '00:11:22:33:44:58', 8080, 'WIRED',
    '四楼客服部入口', '4F', '客服区域',
    1, 1,
    1500, 45,
    'ONLINE', 1, 1,
    'ONLINE', 1, NOW(),
    'HEALTHY', 90,
    5230, 32,
    '客服部IC卡打卡设备,支持离线打卡'
);

-- 7. 工厂车间虹膜识别考勤机
INSERT INTO `t_attendance_device` (
    `device_code`, `device_name`, `device_type`, `device_category`,
    `manufacturer`, `model`, `serial_number`,
    `ip_address`, `mac_address`, `port`, `network_type`,
    `install_location`, `install_area_name`,
    `support_clock_in`, `support_clock_out`,
    `recognition_threshold`, `max_user_count`, `current_user_count`,
    `work_mode`, `offline_enabled`,
    `device_status`, `online_status`, `last_online_time`,
    `health_status`, `health_score`,
    `total_clock_count`, `today_clock_count`, `success_count`,
    `remark`
) VALUES (
    'ATD_005', '工厂车间虹膜识别考勤机', 'IRIS', 'STANDALONE',
    '虹星科技', 'IS-A200', 'IS20250005',
    '192.168.2.101', '00:11:22:33:44:59', 8000, 'WIRED',
    '工厂一车间入口', '车间区域',
    1, 1,
    90.00, 1000, 280,
    'ONLINE', 1,
    'ONLINE', 1, NOW(),
    'HEALTHY', 96,
    12450, 68, 12400,
    '工厂车间高安全级别虹膜识别考勤机'
);

-- 8. 地下停车场人脸考勤机
INSERT INTO `t_attendance_device` (
    `device_code`, `device_name`, `device_type`, `device_category`,
    `manufacturer`, `model`, `serial_number`,
    `ip_address`, `mac_address`, `port`, `network_type`,
    `install_location`, `install_floor`, `install_area_name`,
    `support_clock_in`, `support_clock_out`,
    `work_mode`, `offline_enabled`, `auto_sync_enabled`,
    `device_status`, `online_status`, `last_online_time`,
    `health_status`,
    `total_clock_count`, `today_clock_count`,
    `remark`
) VALUES (
    'ATD_006', '地下停车场人脸考勤机', 'FACE', 'STANDALONE',
    '海康威视', 'DS-K1T606M', 'HK20250006',
    '192.168.3.101', '00:11:22:33:44:60', 8000, 'WIRED',
    '地下停车场入口', 'B1F', '停车场区域',
    1, 1,
    'ONLINE', 1, 1,
    'ONLINE', 1, NOW(),
    'HEALTHY',
    4520, 26,
    '停车场入口人脸识别考勤机,便于员工驾车到达后打卡'
);

-- 9. 五楼会议室临时打卡设备
INSERT INTO `t_attendance_device` (
    `device_code`, `device_name`, `device_type`, `device_category`,
    `manufacturer`, `model`, `serial_number`,
    `install_location`, `install_floor`, `install_area_name`,
    `support_clock_in`, `support_clock_out`,
    `work_mode`, `work_time_enabled`,
    `work_start_time`, `work_end_time`,
    `device_status`, `online_status`,
    `total_clock_count`, `today_clock_count`,
    `remark`
) VALUES (
    'ATD_007', '五楼会议室临时打卡设备', 'FACE', 'INTEGRATED',
    '海康威视', 'DS-K1T341AMF', 'HK20250007',
    '五楼大会议室', '5F', '会议区域',
    1, 1,
    'OFFLINE', 1,
    '08:00:00', '20:00:00',
    'OFFLINE', 0,
    1250, 0,
    '会议室临时打卡设备,仅在会议时间段使用'
);

-- 10. 维护中的备用考勤机
INSERT INTO `t_attendance_device` (
    `device_code`, `device_name`, `device_type`, `device_category`,
    `manufacturer`, `model`, `serial_number`,
    `install_location`,
    `support_clock_in`, `support_clock_out`,
    `work_mode`, `device_status`, `online_status`,
    `maintenance_status`, `last_maintenance_time`, `next_maintenance_time`,
    `total_clock_count`,
    `remark`
) VALUES (
    'ATD_BACKUP_001', '备用考勤机(维护中)', 'FINGERPRINT', 'STANDALONE',
    '中控智慧', 'ZK-F28', 'ZK20250008',
    '仓库备用',
    1, 1,
    'OFFLINE', 'MAINTENANCE', 0,
    'IN_PROGRESS', NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY),
    5680,
    '备用考勤机,当前正在进行维护保养'
);

-- ==================================================================
-- 使用示例查询SQL
-- ==================================================================

-- 示例1: 查询所有在线设备
-- SELECT * FROM t_attendance_device
-- WHERE online_status = 1
--   AND device_status = 'ONLINE'
--   AND deleted_flag = 0
-- ORDER BY device_name ASC;

-- 示例2: 查询人脸识别类设备
-- SELECT * FROM t_attendance_device
-- WHERE device_type IN ('FACE','HYBRID')
--   AND device_status = 'ONLINE'
--   AND deleted_flag = 0;

-- 示例3: 查询需要健康检查的设备
-- SELECT * FROM t_attendance_device
-- WHERE health_check_enabled = 1
--   AND (last_health_check_time IS NULL
--        OR last_health_check_time < DATE_SUB(NOW(), INTERVAL health_check_interval SECOND))
--   AND deleted_flag = 0;

-- 示例4: 查询某区域的设备
-- SELECT * FROM t_attendance_device
-- WHERE install_area_id = 101
--   AND deleted_flag = 0
-- ORDER BY install_location ASC;

-- 示例5: 查询设备使用统计
-- SELECT device_name, device_type, total_clock_count, today_clock_count,
--        success_rate, health_score
-- FROM t_attendance_device
-- WHERE device_status = 'ONLINE'
--   AND deleted_flag = 0
-- ORDER BY today_clock_count DESC;

-- 示例6: 查询故障设备
-- SELECT * FROM t_attendance_device
-- WHERE device_status = 'FAULT'
--   OR health_status = 'ERROR'
--   AND deleted_flag = 0;

-- 示例7: 查询需要维护的设备
-- SELECT * FROM t_attendance_device
-- WHERE next_maintenance_time <= DATE_ADD(CURDATE(), INTERVAL 7 DAY)
--   AND maintenance_status != 'IN_PROGRESS'
--   AND deleted_flag = 0;

-- 示例8: 统计各类型设备数量
-- SELECT device_type, COUNT(*) as device_count,
--        SUM(CASE WHEN online_status=1 THEN 1 ELSE 0 END) as online_count
-- FROM t_attendance_device
-- WHERE deleted_flag = 0
-- GROUP BY device_type;

-- ==================================================================
-- 维护建议
-- ==================================================================
-- 1. 定期检查设备在线状态,及时处理离线设备
-- 2. 监控设备健康评分,低于80分需要检修
-- 3. 定期清理设备存储,避免存储满导致故障
-- 4. 及时同步设备数据,避免离线数据丢失
-- 5. 定期更新设备固件和软件版本

-- ==================================================================
-- 定时任务建议
-- ==================================================================
-- 1. 每5分钟检查设备在线状态,离线超过10分钟发送告警
-- 2. 每小时执行设备健康检查,记录健康状态
-- 3. 每天凌晨同步设备打卡数据到中心数据库
-- 4. 每周统计设备使用情况,生成分析报告
-- 5. 每月检查设备维护计划,提前预警需要维护的设备
-- 6. 实时监控设备故障,自动切换备用设备

-- ==================================================================
-- 注意事项
-- ==================================================================
-- 1. 设备在线状态需要实时监控,及时发现故障
-- 2. 设备数据需要定期同步,避免数据丢失
-- 3. 设备容量需要及时清理,避免存储满
-- 4. 设备固件需要定期更新,修复已知问题
-- 5. 设备维护需要提前计划,避免影响正常使用
-- 6. 设备权限需要严格管理,防止未授权访问
-- 7. 移动打卡需要验证GPS位置,防止异地打卡
-- 8. 设备告警需要及时处理,避免影响考勤准确性

-- ==================================================================
-- 版本历史
-- ==================================================================
-- v1.0.0 - 2025-12-08
--   - 初始版本
--   - 支持多种设备类型(人脸、指纹、虹膜、刷卡、移动端、GPS)
--   - 支持设备在线状态监控
--   - 支持设备健康检查
--   - 支持设备维护管理
--   - 支持设备统计分析
--   - 支持设备告警配置
--   - 初始化10条企业级设备示例

-- ==================================================================
-- 文件结束
-- ==================================================================
