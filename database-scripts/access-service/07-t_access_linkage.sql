-- =============================================
-- IOE-DREAM 智慧园区一卡通管理平台
-- 门禁联动配置表 (t_access_linkage)
-- =============================================
-- 功能说明: 门禁联动表，定义门禁事件触发的联动动作配置
-- 业务场景: 
--   1. 门禁事件联动视频录像
--   2. 门禁事件联动报警输出
--   3. 门禁事件联动其他设备动作
--   4. 门禁区域联动控制
-- 企业级特性:
--   - 支持多种联动类型（视频、报警、设备控制）
--   - 支持联动条件配置（时间段、区域、人员类型）
--   - 支持联动动作延迟执行
--   - 支持联动优先级和冲突处理
-- =============================================

USE `ioedream_access_db`;

-- =============================================
-- 表结构定义
-- =============================================
CREATE TABLE IF NOT EXISTS `t_access_linkage` (
    -- 主键ID
    `linkage_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '联动ID',
    
    -- 联动基本信息
    `linkage_code` VARCHAR(100) NOT NULL COMMENT '联动编码（唯一标识）',
    `linkage_name` VARCHAR(200) NOT NULL COMMENT '联动名称',
    `linkage_type` VARCHAR(50) NOT NULL COMMENT '联动类型：VIDEO-视频联动, ALARM-报警联动, DEVICE-设备联动, AREA-区域联动',
    `linkage_desc` VARCHAR(500) COMMENT '联动描述',
    
    -- 触发条件配置
    `trigger_event` VARCHAR(50) NOT NULL COMMENT '触发事件类型：OPEN-开门, CLOSE-关门, ALARM-报警, FORCE-强行, TIMEOUT-超时',
    `trigger_device_id` BIGINT COMMENT '触发设备ID（NULL表示全部设备）',
    `trigger_area_id` BIGINT COMMENT '触发区域ID（NULL表示全部区域）',
    `trigger_person_type` VARCHAR(50) COMMENT '触发人员类型：EMPLOYEE-员工, VISITOR-访客, VIP-贵宾（NULL表示全部）',
    
    -- 联动动作配置
    `action_type` VARCHAR(50) NOT NULL COMMENT '联动动作类型：RECORD_VIDEO-录像, ALARM_OUTPUT-报警输出, LOCK_DEVICE-锁定设备, NOTIFY-通知',
    `action_target_id` BIGINT NOT NULL COMMENT '动作目标ID（设备ID、摄像头ID等）',
    `action_target_type` VARCHAR(50) NOT NULL COMMENT '动作目标类型：CAMERA-摄像头, ALARM-报警器, ACCESS-门禁, NOTIFY-通知',
    `action_params` JSON COMMENT '动作参数（JSON格式）：{"duration":60, "channel":1, "volume":80}',
    
    -- 联动控制参数
    `delay_seconds` INT DEFAULT 0 COMMENT '延迟执行秒数（0表示立即执行）',
    `duration_seconds` INT COMMENT '持续时间秒数（NULL表示无限制）',
    `priority_level` INT DEFAULT 5 COMMENT '优先级：1-最低, 5-普通, 10-最高',
    
    -- 联动条件
    `time_period_id` BIGINT COMMENT '时间段ID（NULL表示全天）',
    `week_days` VARCHAR(50) COMMENT '生效星期：1,2,3,4,5（NULL表示全周）',
    `condition_expression` VARCHAR(500) COMMENT '条件表达式（支持复杂条件判断）',
    
    -- 联动状态
    `linkage_status` TINYINT DEFAULT 1 COMMENT '联动状态：0-禁用, 1-启用',
    `enabled_flag` TINYINT DEFAULT 1 COMMENT '启用标记：0-停用, 1-启用',
    
    -- 统计信息
    `trigger_count` INT DEFAULT 0 COMMENT '触发次数统计',
    `last_trigger_time` DATETIME COMMENT '最后触发时间',
    `success_count` INT DEFAULT 0 COMMENT '成功执行次数',
    `fail_count` INT DEFAULT 0 COMMENT '失败执行次数',
    
    -- 扩展字段
    `extend_json` JSON COMMENT '扩展配置（JSON格式）',
    `remark` VARCHAR(500) COMMENT '备注说明',
    
    -- 审计字段（强制标准）
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除, 1-已删除',
    
    -- 主键约束
    PRIMARY KEY (`linkage_id`),
    
    -- 唯一键约束
    UNIQUE KEY `uk_linkage_code` (`linkage_code`) USING BTREE,
    
    -- 普通索引
    INDEX `idx_linkage_type` (`linkage_type`) USING BTREE,
    INDEX `idx_trigger_event` (`trigger_event`, `linkage_status`) USING BTREE,
    INDEX `idx_trigger_device` (`trigger_device_id`) USING BTREE,
    INDEX `idx_trigger_area` (`trigger_area_id`) USING BTREE,
    INDEX `idx_action_target` (`action_target_id`, `action_target_type`) USING BTREE,
    INDEX `idx_priority_status` (`priority_level` DESC, `linkage_status`) USING BTREE,
    INDEX `idx_create_time` (`create_time` DESC) USING BTREE
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门禁联动配置表';

-- =============================================
-- 初始化数据（企业级默认配置）
-- =============================================

-- 1. 视频联动配置：门禁开门触发视频录像
INSERT INTO `t_access_linkage` (
    `linkage_code`, `linkage_name`, `linkage_type`, `linkage_desc`,
    `trigger_event`, `trigger_device_id`, `trigger_area_id`, `trigger_person_type`,
    `action_type`, `action_target_id`, `action_target_type`, `action_params`,
    `delay_seconds`, `duration_seconds`, `priority_level`,
    `linkage_status`, `enabled_flag`, `remark`
) VALUES (
    'VIDEO_LINK_001', '开门联动视频录像', 'VIDEO', '门禁开门时自动触发摄像头录像',
    'OPEN', NULL, NULL, NULL,
    'RECORD_VIDEO', 1, 'CAMERA', '{"duration":60, "channel":1, "resolution":"1080P"}',
    0, 60, 8,
    1, 1, '标准视频联动配置'
);

-- 2. 报警联动配置：强行开门触发报警输出
INSERT INTO `t_access_linkage` (
    `linkage_code`, `linkage_name`, `linkage_type`, `linkage_desc`,
    `trigger_event`, `trigger_device_id`, `trigger_area_id`, `trigger_person_type`,
    `action_type`, `action_target_id`, `action_target_type`, `action_params`,
    `delay_seconds`, `duration_seconds`, `priority_level`,
    `linkage_status`, `enabled_flag`, `remark`
) VALUES (
    'ALARM_LINK_001', '强行开门联动报警', 'ALARM', '强行开门时触发报警器和通知',
    'FORCE', NULL, NULL, NULL,
    'ALARM_OUTPUT', 1, 'ALARM', '{"volume":100, "mode":"continuous", "duration":30}',
    0, 30, 10,
    1, 1, '强行开门立即报警'
);

-- 3. 设备联动配置：门禁报警触发区域锁定
INSERT INTO `t_access_linkage` (
    `linkage_code`, `linkage_name`, `linkage_type`, `linkage_desc`,
    `trigger_event`, `trigger_device_id`, `trigger_area_id`, `trigger_person_type`,
    `action_type`, `action_target_id`, `action_target_type`, `action_params`,
    `delay_seconds`, `duration_seconds`, `priority_level`,
    `linkage_status`, `enabled_flag`, `remark`
) VALUES (
    'DEVICE_LINK_001', '报警联动区域锁定', 'DEVICE', '门禁报警时自动锁定整个区域',
    'ALARM', NULL, 1, NULL,
    'LOCK_DEVICE', 1, 'ACCESS', '{"lock_mode":"all", "unlock_delay":300}',
    0, NULL, 9,
    1, 1, '报警后锁定区域5分钟'
);

-- 4. 通知联动配置：VIP开门通知管理员
INSERT INTO `t_access_linkage` (
    `linkage_code`, `linkage_name`, `linkage_type`, `linkage_desc`,
    `trigger_event`, `trigger_device_id`, `trigger_area_id`, `trigger_person_type`,
    `action_type`, `action_target_id`, `action_target_type`, `action_params`,
    `delay_seconds`, `duration_seconds`, `priority_level`,
    `linkage_status`, `enabled_flag`, `remark`
) VALUES (
    'NOTIFY_LINK_001', 'VIP通行通知', 'NOTIFY', 'VIP人员通行时通知管理员',
    'OPEN', NULL, NULL, 'VIP',
    'NOTIFY', 1, 'NOTIFY', '{"notify_type":"SMS", "template":"VIP_ENTRY", "recipients":["admin"]}',
    0, NULL, 7,
    1, 1, 'VIP人员通行实时通知'
);

-- 5. 视频联动配置：访客开门录像保存
INSERT INTO `t_access_linkage` (
    `linkage_code`, `linkage_name`, `linkage_type`, `linkage_desc`,
    `trigger_event`, `trigger_device_id`, `trigger_area_id`, `trigger_person_type`,
    `action_type`, `action_target_id`, `action_target_type`, `action_params`,
    `delay_seconds`, `duration_seconds`, `priority_level`,
    `linkage_status`, `enabled_flag`, `remark`
) VALUES (
    'VIDEO_LINK_002', '访客通行视频录像', 'VIDEO', '访客通行时自动录像并保存',
    'OPEN', NULL, NULL, 'VISITOR',
    'RECORD_VIDEO', 1, 'CAMERA', '{"duration":120, "channel":1, "save_days":30, "resolution":"1080P"}',
    0, 120, 8,
    1, 1, '访客通行录像保存30天'
);

-- 6. 区域联动配置：夜间开门加强监控
INSERT INTO `t_access_linkage` (
    `linkage_code`, `linkage_name`, `linkage_type`, `linkage_desc`,
    `trigger_event`, `trigger_device_id`, `trigger_area_id`, `trigger_person_type`,
    `action_type`, `action_target_id`, `action_target_type`, `action_params`,
    `delay_seconds`, `duration_seconds`, `priority_level`,
    `week_days`, `linkage_status`, `enabled_flag`, `remark`
) VALUES (
    'AREA_LINK_001', '夜间通行加强监控', 'AREA', '夜间时段通行加强视频监控',
    'OPEN', NULL, NULL, NULL,
    'RECORD_VIDEO', 1, 'CAMERA', '{"duration":180, "channel":"all", "night_mode":true, "alert":true}',
    0, 180, 9,
    '1,2,3,4,5,6,7', 1, 1, '夜间通行加强监控（全周）'
);

-- =============================================
-- 索引优化说明
-- =============================================
-- 1. uk_linkage_code: 联动编码唯一索引，确保联动配置唯一性
-- 2. idx_linkage_type: 联动类型索引，支持按类型快速查询
-- 3. idx_trigger_event: 触发事件联合索引，支持事件触发快速匹配
-- 4. idx_trigger_device: 触发设备索引，支持设备级联动查询
-- 5. idx_trigger_area: 触发区域索引，支持区域级联动查询
-- 6. idx_action_target: 动作目标联合索引，支持联动目标查询
-- 7. idx_priority_status: 优先级和状态联合索引，支持优先级排序
-- 8. idx_create_time: 创建时间索引，支持时间范围查询

-- =============================================
-- 使用示例
-- =============================================
-- 1. 查询所有启用的视频联动配置
-- SELECT * FROM t_access_linkage 
-- WHERE linkage_type = 'VIDEO' AND linkage_status = 1 AND deleted_flag = 0;

-- 2. 查询指定设备的所有联动配置
-- SELECT * FROM t_access_linkage 
-- WHERE trigger_device_id = 1 AND deleted_flag = 0 
-- ORDER BY priority_level DESC;

-- 3. 查询指定触发事件的联动配置（按优先级排序）
-- SELECT * FROM t_access_linkage 
-- WHERE trigger_event = 'FORCE' AND linkage_status = 1 AND deleted_flag = 0 
-- ORDER BY priority_level DESC, linkage_id ASC;

-- 4. 更新联动触发统计
-- UPDATE t_access_linkage 
-- SET trigger_count = trigger_count + 1, 
--     success_count = success_count + 1,
--     last_trigger_time = NOW() 
-- WHERE linkage_id = 1;

-- =============================================
-- 维护建议
-- =============================================
-- 1. 定期清理已删除的联动配置（deleted_flag = 1）
-- 2. 监控联动触发频率和成功率（trigger_count, success_count, fail_count）
-- 3. 定期审查高优先级联动配置的合理性
-- 4. 优化联动条件表达式的执行性能
-- 5. 定期检查联动目标设备的在线状态
