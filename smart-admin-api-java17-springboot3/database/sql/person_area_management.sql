-- =====================================================
-- 人员与区域关联管理数据表结构
-- 支持人员归属区域管理和智能设备下发策略
--
-- 版本: 1.0.0
-- 创建时间: 2025-11-24
-- 作者: SmartAdmin Team
-- 依赖: area_management_migration.sql
-- =====================================================

-- 1. 创建人员区域关联表
DROP TABLE IF EXISTS `t_person_area_relation`;

CREATE TABLE `t_person_area_relation` (
  `relation_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `person_id` BIGINT NOT NULL COMMENT '人员ID',
  `person_type` VARCHAR(32) NOT NULL COMMENT '人员类型(EMPLOYEE-员工, VISITOR-访客, CONTRACTOR-外包)',
  `person_name` VARCHAR(100) COMMENT '人员姓名(冗余字段)',
  `person_code` VARCHAR(50) COMMENT '人员编号(冗余字段)',
  `area_id` BIGINT NOT NULL COMMENT '区域ID',
  `relation_type` VARCHAR(32) NOT NULL COMMENT '关联类型(PRIMARY-主归属, SECONDARY-次要归属, TEMPORARY-临时)',
  `effective_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生效时间',
  `expire_time` DATETIME NULL COMMENT '失效时间(NULL表示永久有效)',
  `sync_status` TINYINT NOT NULL DEFAULT 0 COMMENT '同步状态(0-待同步, 1-同步中, 2-同步完成, 3-同步失败)',
  `last_sync_time` DATETIME NULL COMMENT '最后同步时间',
  `sync_device_types` JSON NULL COMMENT '需要同步的设备类型列表',
  `sync_config` JSON NULL COMMENT '同步配置(包含各设备类型的特定配置)',
  `priority_level` INT NOT NULL DEFAULT 5 COMMENT '优先级(1-10,数字越小优先级越高)',
  `auto_renew` TINYINT NOT NULL DEFAULT 0 COMMENT '是否自动续期(0-否, 1-是)',
  `renew_days` INT NULL COMMENT '自动续期天数',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态(0-停用, 1-启用)',
  `remark` VARCHAR(500) COMMENT '备注信息',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_user_id` BIGINT COMMENT '创建人ID',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_user_id` BIGINT COMMENT '更新人ID',
  `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记(0-未删除, 1-已删除)',
  `version` INT NOT NULL DEFAULT 1 COMMENT '版本号(乐观锁)',
  PRIMARY KEY (`relation_id`),
  UNIQUE KEY `uk_person_area_type` (`person_id`, `area_id`, `relation_type`, `deleted_flag`),
  KEY `idx_person_id` (`person_id`, `deleted_flag`),
  KEY `idx_area_id` (`area_id`, `deleted_flag`),
  KEY `idx_person_type` (`person_type`, `deleted_flag`),
  KEY `idx_relation_type` (`relation_type`, `deleted_flag`),
  KEY `idx_effective_time` (`effective_time`, `deleted_flag`),
  KEY `idx_expire_time` (`expire_time`, `deleted_flag`),
  KEY `idx_sync_status` (`sync_status`, `deleted_flag`),
  KEY `idx_status` (`status`, `deleted_flag`),
  KEY `idx_priority` (`priority_level`, `deleted_flag`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_person_area_area_id` FOREIGN KEY (`area_id`) REFERENCES `t_area` (`area_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='人员区域关联表';

-- 2. 创建设备下发策略表
DROP TABLE IF EXISTS `t_device_dispatch_strategy`;

CREATE TABLE `t_device_dispatch_strategy` (
  `strategy_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '策略ID',
  `strategy_name` VARCHAR(100) NOT NULL COMMENT '策略名称',
  `strategy_code` VARCHAR(50) NOT NULL COMMENT '策略编码(系统内唯一)',
  `device_type` VARCHAR(32) NOT NULL COMMENT '设备类型(ACCESS-门禁, ATTENDANCE-考勤, CONSUME-消费, VIDEO-视频)',
  `device_category` VARCHAR(32) NULL COMMENT '设备细分类别',
  `manufacturer` VARCHAR(50) NULL COMMENT '设备厂商',
  `protocol_type` VARCHAR(32) NULL COMMENT '协议类型',
  `dispatch_condition` JSON NOT NULL COMMENT '下发条件配置(包含人员类型、区域类型、时间段等条件)',
  `dispatch_config` JSON NOT NULL COMMENT '下发配置(包含下发数据格式、协议参数等)',
  `priority_level` INT NOT NULL DEFAULT 5 COMMENT '优先级(1-10,数字越小优先级越高)',
  `retry_count` INT NOT NULL DEFAULT 3 COMMENT '重试次数',
  `retry_interval` INT NOT NULL DEFAULT 60 COMMENT '重试间隔(秒)',
  `timeout_seconds` INT NOT NULL DEFAULT 30 COMMENT '超时时间(秒)',
  `enabled_flag` TINYINT NOT NULL DEFAULT 1 COMMENT '启用标记(0-停用, 1-启用)',
  `description` VARCHAR(500) COMMENT '策略描述',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_user_id` BIGINT COMMENT '创建人ID',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_user_id` BIGINT COMMENT '更新人ID',
  `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记(0-未删除, 1-已删除)',
  `version` INT NOT NULL DEFAULT 1 COMMENT '版本号(乐观锁)',
  PRIMARY KEY (`strategy_id`),
  UNIQUE KEY `uk_strategy_code` (`strategy_code`, `deleted_flag`),
  KEY `idx_device_type` (`device_type`, `deleted_flag`),
  KEY `idx_device_category` (`device_category`, `deleted_flag`),
  KEY `idx_manufacturer` (`manufacturer`, `deleted_flag`),
  KEY `idx_protocol_type` (`protocol_type`, `deleted_flag`),
  KEY `idx_priority` (`priority_level`, `deleted_flag`),
  KEY `idx_enabled` (`enabled_flag`, `deleted_flag`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备下发策略表';

-- 3. 创建设备下发执行记录表
DROP TABLE IF EXISTS `t_device_dispatch_record`;

CREATE TABLE `t_device_dispatch_record` (
  `record_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `relation_id` BIGINT NOT NULL COMMENT '关联ID',
  `device_id` BIGINT NOT NULL COMMENT '设备ID',
  `device_code` VARCHAR(50) COMMENT '设备编码(冗余字段)',
  `device_name` VARCHAR(100) COMMENT '设备名称(冗余字段)',
  `strategy_id` BIGINT NOT NULL COMMENT '策略ID',
  `dispatch_type` VARCHAR(32) NOT NULL COMMENT '下发类型(ADD-新增, UPDATE-更新, DELETE-删除, SYNC-同步)',
  `dispatch_status` VARCHAR(32) NOT NULL COMMENT '下发状态(PENDING-待下发, DISPATCHING-下发中, SUCCESS-成功, FAILED-失败, TIMEOUT-超时)',
  `request_data` JSON NULL COMMENT '下发请求数据',
  `response_data` JSON NULL COMMENT '下发响应数据',
  `error_message` TEXT NULL COMMENT '错误信息',
  `error_code` VARCHAR(50) NULL COMMENT '错误代码',
  `retry_count` INT NOT NULL DEFAULT 0 COMMENT '重试次数',
  `max_retry` INT NOT NULL DEFAULT 3 COMMENT '最大重试次数',
  `dispatch_time` DATETIME NULL COMMENT '下发时间',
  `complete_time` DATETIME NULL COMMENT '完成时间',
  `duration_ms` INT NULL COMMENT '执行耗时(毫秒)',
  `operator_id` BIGINT COMMENT '操作人ID',
  `operator_name` VARCHAR(50) COMMENT '操作人姓名',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`record_id`),
  KEY `idx_relation_device` (`relation_id`, `device_id`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_strategy_id` (`strategy_id`),
  KEY `idx_status_time` (`dispatch_status`, `create_time`),
  KEY `idx_dispatch_time` (`dispatch_time`),
  KEY `idx_retry_count` (`retry_count`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_dispatch_record_relation` FOREIGN KEY (`relation_id`) REFERENCES `t_person_area_relation` (`relation_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备下发执行记录表';

-- 4. 创建设备下发配置模板表
DROP TABLE IF EXISTS `t_device_dispatch_template`;

CREATE TABLE `t_device_dispatch_template` (
  `template_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  `template_name` VARCHAR(100) NOT NULL COMMENT '模板名称',
  `template_code` VARCHAR(50) NOT NULL COMMENT '模板编码',
  `device_type` VARCHAR(32) NOT NULL COMMENT '设备类型',
  `template_type` VARCHAR(32) NOT NULL COMMENT '模板类型(PERSON_DATA-人员数据, CONFIG-配置, COMMAND-指令)',
  `template_content` JSON NOT NULL COMMENT '模板内容(JSON格式)',
  `variables_definition` JSON NULL COMMENT '变量定义(用于动态替换)',
  `description` VARCHAR(500) COMMENT '模板描述',
  `enabled_flag` TINYINT NOT NULL DEFAULT 1 COMMENT '启用标记',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_user_id` BIGINT COMMENT '创建人ID',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_user_id` BIGINT COMMENT '更新人ID',
  `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
  `version` INT NOT NULL DEFAULT 1 COMMENT '版本号',
  PRIMARY KEY (`template_id`),
  UNIQUE KEY `uk_template_code` (`template_code`, `deleted_flag`),
  KEY `idx_device_type` (`device_type`, `deleted_flag`),
  KEY `idx_template_type` (`template_type`, `deleted_flag`),
  KEY `idx_enabled` (`enabled_flag`, `deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备下发配置模板表';

-- 5. 插入默认的设备下发策略
INSERT INTO `t_device_dispatch_strategy` (
    `strategy_name`, `strategy_code`, `device_type`, `device_category`,
    `dispatch_condition`, `dispatch_config`, `priority_level`, `enabled_flag`, `description`
) VALUES
-- 门禁设备策略
('门禁设备-员工默认策略', 'ACCESS_EMPLOYEE_DEFAULT', 'ACCESS', NULL,
 JSON_OBJECT(
    'personTypes', JSON_ARRAY('EMPLOYEE'),
    'areaTypes', JSON_ARRAY(2, 3, 4),  -- 建筑、楼层、房间
    'timeRestriction', JSON_OBJECT('enabled', false)
 ),
 JSON_OBJECT(
    'protocol', 'HTTP',
    'endpoint', '/api/person/add',
    'dataFormat', JSON_OBJECT(
        'personId', '{{personId}}',
        'personName', '{{personName}}',
        'personCode', '{{personCode}}',
        'cardNo', '{{cardNo}}',
        'fingerprint', '{{fingerprint}}',
        'face', '{{face}}',
        'validFrom', '{{effectiveTime}}',
        'validTo', '{{expireTime}}',
        'accessLevel', '{{accessLevel}}',
        'timezones', JSON_ARRAY()
    ),
    'authType', 'Bearer',
    'timeout', 30
 ),
 1, 1, '员工在门禁设备的默认下发策略'),

('门禁设备-访客策略', 'ACCESS_VISITOR', 'ACCESS', NULL,
 JSON_OBJECT(
    'personTypes', JSON_ARRAY('VISITOR'),
    'areaTypes', JSON_ARRAY(2, 3, 4, 5),  -- 建筑、楼层、房间、区域
    'timeRestriction', JSON_OBJECT('enabled', true, 'allowedHours', '08:00-18:00')
 ),
 JSON_OBJECT(
    'protocol', 'HTTP',
    'endpoint', '/api/visitor/add',
    'dataFormat', JSON_OBJECT(
        'personId', '{{personId}}',
        'personName', '{{personName}}',
        'personCode', '{{personCode}}',
        'cardNo', '{{cardNo}}',
        'validFrom', '{{effectiveTime}}',
        'validTo', '{{expireTime}}',
        'accessLevel', 1,
        'timezones', JSON_ARRAY(
            JSON_OBJECT('weekday', '1-5', 'startTime', '08:00', 'endTime', '18:00')
        )
    ),
    'authType', 'Bearer',
    'timeout', 30
 ),
 3, 1, '访客在门禁设备的下发策略'),

('门禁设备-核心区域策略', 'ACCESS_CORE_AREA', 'ACCESS', NULL,
 JSON_OBJECT(
    'personTypes', JSON_ARRAY('EMPLOYEE'),
    'areaTypes', JSON_ARRAY(4),  -- 仅房间
    'areaLevel', JSON_ARRAY(3),  -- 核心区域
    'timeRestriction', JSON_OBJECT('enabled', false)
 ),
 JSON_OBJECT(
    'protocol', 'HTTP',
    'endpoint', '/api/person/add',
    'dataFormat', JSON_OBJECT(
        'personId', '{{personId}}',
        'personName', '{{personName}}',
        'cardNo', '{{cardNo}}',
        'fingerprint', '{{fingerprint}}',
        'face', '{{face}}',
        'iris', '{{iris}}',
        'validFrom', '{{effectiveTime}}',
        'validTo', '{{expireTime}}',
        'accessLevel', 3,
        'multiFactor', true
    ),
    'authType', 'Bearer',
    'timeout', 30
 ),
 1, 1, '核心区域门禁设备的增强安全策略'),

-- 考勤设备策略
('考勤设备-员工策略', 'ATTENDANCE_EMPLOYEE', 'ATTENDANCE', NULL,
 JSON_OBJECT(
    'personTypes', JSON_ARRAY('EMPLOYEE'),
    'areaTypes', JSON_ARRAY(2, 3, 4),
    'timeRestriction', JSON_OBJECT('enabled', false)
 ),
 JSON_OBJECT(
    'protocol', 'TCP',
    'port', 8000,
    'dataFormat', JSON_OBJECT(
        'command', 'ADD_PERSON',
        'personId', '{{personId}}',
        'personName', '{{personName}}',
        'personCode', '{{personCode}}',
        'department', '{{department}}',
        'shift', '{{shift}}'
    ),
    'timeout', 15
 ),
 2, 1, '员工考勤设备下发策略'),

-- 消费设备策略
('消费设备-员工策略', 'CONSUME_EMPLOYEE', 'CONSUME', NULL,
 JSON_OBJECT(
    'personTypes', JSON_ARRAY('EMPLOYEE'),
    'areaTypes', JSON_ARRAY(4, 5),  -- 房间、区域
    'deviceCategories', JSON_ARRAY('POS', 'VENDING')
 ),
 JSON_OBJECT(
    'protocol', 'HTTP',
    'endpoint', '/api/account/add',
    'dataFormat', JSON_OBJECT(
        'personId', '{{personId}}',
        'personName', '{{personName}}',
        'personCode', '{{personCode}}',
        'cardNo', '{{cardNo}}',
        'balance', 0.00,
        'creditLimit', 100.00,
        'permissions', JSON_ARRAY('PURCHASE', 'REFUND')
    ),
    'timeout', 20
 ),
 3, 1, '员工消费设备下发策略'),

-- 视频监控设备策略
('视频设备-人员布控策略', 'VIDEO_PERSON_CONTROL', 'VIDEO', NULL,
 JSON_OBJECT(
    'personTypes', JSON_ARRAY('EMPLOYEE', 'VISITOR'),
    'areaTypes', JSON_ARRAY(2, 3, 4),
    'deviceCategories', JSON_ARRAY('CAMERA', 'NVR')
 ),
 JSON_OBJECT(
    'protocol', 'HTTP',
    'endpoint', '/api/face/add',
    'dataFormat', JSON_OBJECT(
        'personId', '{{personId}}',
        'personName', '{{personName}}',
        'faceImage', '{{faceImage}}',
        'similarityThreshold', 0.8,
        'alertLevel', '{{alertLevel}}',
        'validPeriod', 30
    ),
    'timeout', 45
 ),
 4, 1, '视频监控人员布控下发策略');

-- 6. 插入设备下发配置模板
INSERT INTO `t_device_dispatch_template` (
    `template_name`, `template_code`, `device_type`, `template_type`,
    `template_content`, `variables_definition`, `description`
) VALUES
-- 门禁人员数据模板
('门禁-人员基本信息模板', 'ACCESS_PERSON_BASE', 'ACCESS', 'PERSON_DATA',
 JSON_OBJECT(
    'personId', '{{personId}}',
    'personName', '{{personName}}',
    'personCode', '{{personCode}}',
    'cardNo', '{{cardNo}}',
    'validFrom', '{{effectiveTime}}',
    'validTo', '{{expireTime}}',
    'accessLevel', '{{accessLevel}}'
 ),
 JSON_OBJECT(
    'personId', '人员ID',
    'personName', '人员姓名',
    'personCode', '人员编号',
    'cardNo', '卡号',
    'effectiveTime', '生效时间',
    'expireTime', '失效时间',
    'accessLevel', '门禁级别'
 ),
 '门禁设备人员基本信息模板'),

('门禁-生物特征模板', 'ACCESS_BIOMETRIC', 'ACCESS', 'PERSON_DATA',
 JSON_OBJECT(
    'personId', '{{personId}}',
    'fingerprint', '{{fingerprint}}',
    'face', '{{face}}',
    'iris', '{{iris}}',
    'palm', '{{palm}}'
 ),
 JSON_OBJECT(
    'personId', '人员ID',
    'fingerprint', '指纹数据(base64)',
    'face', '人脸数据(base64)',
    'iris', '虹膜数据(base64)',
    'palm', '掌纹数据(base64)'
 ),
 '门禁设备生物特征模板'),

-- 考勤人员数据模板
('考勤-员工信息模板', 'ATTENDANCE_EMPLOYEE', 'ATTENDANCE', 'PERSON_DATA',
 JSON_OBJECT(
    'command', 'ADD_PERSON',
    'personId', '{{personId}}',
    'personName', '{{personName}}',
    'personCode', '{{personCode}}',
    'department', '{{department}}',
    'shift', '{{shift}}',
    'workDays', '{{workDays}}'
 ),
 JSON_OBJECT(
    'personId', '人员ID',
    'personName', '人员姓名',
    'personCode', '人员编号',
    'department', '部门',
    'shift', '班次',
    'workDays', '工作日'
 ),
 '考勤设备员工信息模板'),

-- 消费账户模板
('消费-账户信息模板', 'CONSUME_ACCOUNT', 'CONSUME', 'PERSON_DATA',
 JSON_OBJECT(
    'personId', '{{personId}}',
    'personName', '{{personName}}',
    'personCode', '{{personCode}}',
    'cardNo', '{{cardNo}}',
    'balance', '{{balance}}',
    'creditLimit', '{{creditLimit}}',
    'permissions', '{{permissions}}'
 ),
 JSON_OBJECT(
    'personId', '人员ID',
    'personName', '人员姓名',
    'personCode', '人员编号',
    'cardNo', '卡号',
    'balance', '账户余额',
    'creditLimit', '信用额度',
    'permissions', '权限列表'
 ),
 '消费设备账户信息模板'),

-- 视频人脸模板
('视频-人脸识别模板', 'VIDEO_FACE', 'VIDEO', 'PERSON_DATA',
 JSON_OBJECT(
    'personId', '{{personId}}',
    'personName', '{{personName}}',
    'faceImage', '{{faceImage}}',
    'similarityThreshold', 0.8,
    'alertLevel', '{{alertLevel}}',
    'validPeriod', 30
 ),
 JSON_OBJECT(
    'personId', '人员ID',
    'personName', '人员姓名',
    'faceImage', '人脸图片(base64)',
    'similarityThreshold', '相似度阈值',
    'alertLevel', '告警级别',
    'validPeriod', '有效期(天)'
 ),
 '视频监控人脸识别模板');

-- 7. 创建视图便于查询
-- 人员区域关联详情视图
CREATE OR REPLACE VIEW `v_person_area_relation_detail` AS
SELECT
    par.relation_id,
    par.person_id,
    par.person_type,
    par.person_name,
    par.person_code,
    par.area_id,
    a.area_code,
    a.area_name,
    a.area_type,
    par.relation_type,
    par.effective_time,
    par.expire_time,
    par.sync_status,
    par.last_sync_time,
    par.sync_device_types,
    par.priority_level,
    par.auto_renew,
    par.renew_days,
    par.status,
    par.create_time,
    CASE
        WHEN par.expire_time IS NULL THEN '永久'
        WHEN par.expire_time < NOW() THEN '已过期'
        WHEN DATEDIFF(par.expire_time, NOW()) <= 7 THEN '即将过期'
        ELSE '正常'
    END as status_desc,
    CASE
        WHEN par.sync_status = 0 THEN '待同步'
        WHEN par.sync_status = 1 THEN '同步中'
        WHEN par.sync_status = 2 THEN '同步完成'
        WHEN par.sync_status = 3 THEN '同步失败'
        ELSE '未知'
    END as sync_status_desc,
    a.path as area_path
FROM t_person_area_relation par
LEFT JOIN t_area a ON par.area_id = a.area_id
WHERE par.deleted_flag = 0;

-- 设备下发统计视图
CREATE OR REPLACE VIEW `v_device_dispatch_statistics` AS
SELECT
    DATE(ddr.create_time) as dispatch_date,
    dds.device_type,
    dds.device_category,
    dds.strategy_name,
    COUNT(*) as total_count,
    SUM(CASE WHEN ddr.dispatch_status = 'SUCCESS' THEN 1 ELSE 0 END) as success_count,
    SUM(CASE WHEN ddr.dispatch_status = 'FAILED' THEN 1 ELSE 0 END) as failed_count,
    SUM(CASE WHEN ddr.dispatch_status = 'TIMEOUT' THEN 1 ELSE 0 END) as timeout_count,
    ROUND(AVG(ddr.duration_ms), 2) as avg_duration_ms,
    MAX(ddr.duration_ms) as max_duration_ms,
    MIN(ddr.duration_ms) as min_duration_ms
FROM t_device_dispatch_record ddr
LEFT JOIN t_device_dispatch_strategy dds ON ddr.strategy_id = dds.strategy_id
GROUP BY DATE(ddr.create_time), dds.device_type, dds.device_category, dds.strategy_name
ORDER BY dispatch_date DESC, dds.device_type;

-- 8. 创建索引优化查询性能
-- 为人员区域关联表创建复合索引
ALTER TABLE `t_person_area_relation` ADD INDEX `idx_person_type_status` (`person_type`, `status`, `deleted_flag`);
ALTER TABLE `t_person_area_relation` ADD INDEX `idx_area_priority` (`area_id`, `priority_level`, `deleted_flag`);
ALTER TABLE `t_person_area_relation` ADD INDEX `idx_sync_priority` (`sync_status`, `priority_level`, `create_time`);
ALTER TABLE `t_person_area_relation` ADD INDEX `idx_effective_expire` (`effective_time`, `expire_time`, `deleted_flag`);

-- 为设备下发记录表创建复合索引
ALTER TABLE `t_device_dispatch_record` ADD INDEX `idx_device_status_time` (`device_id`, `dispatch_status`, `create_time`);
ALTER TABLE `t_device_dispatch_record` ADD INDEX `idx_strategy_status` (`strategy_id`, `dispatch_status`);
ALTER TABLE `t_device_dispatch_record` ADD INDEX `idx_type_status_retry` (`dispatch_type`, `dispatch_status`, `retry_count`);

-- 9. 数据迁移完成，添加版本记录
INSERT INTO `migration_version` (`version`, `description`) VALUES
('1.1.0', '创建人员与区域关联管理表结构，支持智能设备下发策略');

-- 10. 性能优化提示
/*
性能优化建议：
1. 对于大量人员区域关联数据，建议使用分页查询
2. 定期清理过期的下发记录，避免表过大
3. 对频繁查询的关联数据使用缓存
4. 设备下发操作建议异步执行，避免阻塞主流程
5. 对于视频等大文件传输，考虑使用CDN或文件服务器
6. 在高并发场景下，考虑使用消息队列处理下发任务
7. 建议配置合适的数据库连接池参数
*/

-- 11. 使用说明
/*
使用说明：
1. t_person_area_relation: 人员区域关联表，管理人员的区域归属
2. t_device_dispatch_strategy: 设备下发策略表，配置不同设备的下发规则
3. t_device_dispatch_record: 下发记录表，追踪每次下发的执行情况
4. t_device_dispatch_template: 配置模板表，管理下发数据的模板格式
5. 使用策略模式支持不同设备类型的下发规则
6. 支持异步下发和重试机制，保证数据一致性
7. 提供完整的日志记录，便于问题排查
8. 支持基于模板的数据格式化，简化配置工作
*/

SELECT '人员与区域关联管理数据表结构创建完成' as message;