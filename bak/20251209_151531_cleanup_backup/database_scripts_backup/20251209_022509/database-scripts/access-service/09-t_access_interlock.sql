-- =============================================
-- IOE-DREAM 智慧园区一卡通管理平台
-- 互锁配置表 (t_access_interlock)
-- =============================================
-- 功能说明: 门禁互锁配置表，实现多门互锁控制，防止安全隐患
-- 业务场景: 
--   1. 安全通道互锁（防尾随门）
--   2. 消防通道互锁控制
--   3. 洁净室互锁管理
--   4. 多级安全区域控制
-- 企业级特性:
--   - 支持多门互锁组管理
--   - 支持互锁优先级控制
--   - 支持互锁超时自动解除
--   - 支持互锁异常告警
-- =============================================

USE `ioedream_access_db`;

-- =============================================
-- 互锁组表（定义互锁组）
-- =============================================
CREATE TABLE IF NOT EXISTS `t_access_interlock` (
    -- 主键ID
    `interlock_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '互锁ID',
    
    -- 互锁基本信息
    `interlock_code` VARCHAR(100) NOT NULL COMMENT '互锁编码（唯一标识）',
    `interlock_name` VARCHAR(200) NOT NULL COMMENT '互锁名称',
    `interlock_type` VARCHAR(50) NOT NULL COMMENT '互锁类型：SECURITY-安全互锁, FIRE-消防互锁, CLEAN-洁净互锁, CUSTOM-自定义',
    `interlock_desc` VARCHAR(500) COMMENT '互锁描述',
    
    -- 互锁模式配置
    `interlock_mode` VARCHAR(50) NOT NULL COMMENT '互锁模式：STRICT-严格（一开全闭）, SEQUENCE-顺序（按序开启）, FLEXIBLE-灵活（允许部分）',
    `max_open_count` INT DEFAULT 1 COMMENT '最大同时开启数量（1表示一次只能开一个门）',
    `sequence_required` TINYINT DEFAULT 0 COMMENT '要求顺序：0-不要求, 1-必须按顺序',
    `open_sequence` VARCHAR(500) COMMENT '开启顺序配置（JSON数组）：["device_1", "device_2", "device_3"]',
    
    -- 时间控制
    `open_timeout_seconds` INT DEFAULT 30 COMMENT '开门超时时间（秒，超时自动关闭）',
    `unlock_delay_seconds` INT DEFAULT 5 COMMENT '解锁延迟时间（秒，前一门关闭后延迟解锁下一门）',
    `auto_lock` TINYINT DEFAULT 1 COMMENT '自动上锁：0-不自动, 1-超时自动上锁',
    
    -- 异常处理
    `exception_mode` VARCHAR(50) DEFAULT 'ALARM' COMMENT '异常处理模式：ALARM-告警, FORCE_CLOSE-强制关闭, ALLOW-允许但记录',
    `alarm_enabled` TINYINT DEFAULT 1 COMMENT '告警启用：0-禁用, 1-启用',
    `alarm_delay_seconds` INT DEFAULT 10 COMMENT '告警延迟时间（秒）',
    
    -- 应用范围
    `area_id` BIGINT COMMENT '关联区域ID',
    `device_count` INT DEFAULT 0 COMMENT '互锁设备数量',
    
    -- 时间段控制
    `time_period_id` BIGINT COMMENT '生效时间段ID（NULL表示全天）',
    `week_days` VARCHAR(50) COMMENT '生效星期：1,2,3,4,5（NULL表示全周）',
    `holiday_mode` VARCHAR(50) DEFAULT 'NORMAL' COMMENT '节假日模式：NORMAL-正常, DISABLE-禁用, SPECIAL-特殊规则',
    
    -- 互锁状态
    `interlock_status` TINYINT DEFAULT 1 COMMENT '互锁状态：0-禁用, 1-启用',
    `enabled_flag` TINYINT DEFAULT 1 COMMENT '启用标记：0-停用, 1-启用',
    
    -- 统计信息
    `total_activations` INT DEFAULT 0 COMMENT '总激活次数',
    `violation_count` INT DEFAULT 0 COMMENT '违规次数统计',
    `last_activation_time` DATETIME COMMENT '最后激活时间',
    `last_violation_time` DATETIME COMMENT '最后违规时间',
    
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
    PRIMARY KEY (`interlock_id`),
    
    -- 唯一键约束
    UNIQUE KEY `uk_interlock_code` (`interlock_code`) USING BTREE,
    
    -- 普通索引
    INDEX `idx_interlock_type` (`interlock_type`) USING BTREE,
    INDEX `idx_interlock_mode` (`interlock_mode`, `interlock_status`) USING BTREE,
    INDEX `idx_area_id` (`area_id`) USING BTREE,
    INDEX `idx_status` (`interlock_status`, `enabled_flag`) USING BTREE,
    INDEX `idx_create_time` (`create_time` DESC) USING BTREE
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门禁互锁配置表';

-- =============================================
-- 互锁设备关联表（定义互锁组包含的设备）
-- =============================================
CREATE TABLE IF NOT EXISTS `t_access_interlock_device` (
    -- 主键ID
    `interlock_device_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '互锁设备关联ID',
    
    -- 关联信息
    `interlock_id` BIGINT NOT NULL COMMENT '互锁ID',
    `device_id` BIGINT NOT NULL COMMENT '设备ID',
    
    -- 设备配置
    `device_role` VARCHAR(50) NOT NULL COMMENT '设备角色：MASTER-主门, SLAVE-从门, NORMAL-普通门',
    `device_sequence` INT DEFAULT 0 COMMENT '设备顺序（用于顺序互锁）',
    `device_priority` INT DEFAULT 5 COMMENT '设备优先级：1-最低, 5-普通, 10-最高',
    
    -- 互锁规则
    `lock_when_open` TINYINT DEFAULT 1 COMMENT '其他门开启时上锁：0-否, 1-是',
    `unlock_when_close` TINYINT DEFAULT 1 COMMENT '其他门关闭时解锁：0-否, 1-是',
    `force_close_others` TINYINT DEFAULT 0 COMMENT '开启时强制关闭其他门：0-否, 1-是',
    
    -- 时间控制
    `max_open_duration` INT DEFAULT 30 COMMENT '最大开启时长（秒）',
    `min_close_duration` INT DEFAULT 3 COMMENT '最小关闭时长（秒，关闭后多久才能再次开启）',
    
    -- 状态信息
    `current_status` VARCHAR(50) DEFAULT 'CLOSED' COMMENT '当前状态：CLOSED-关闭, OPEN-开启, LOCKING-上锁中, UNLOCKING-解锁中',
    `last_status_change` DATETIME COMMENT '最后状态变更时间',
    
    -- 扩展字段
    `extend_json` JSON COMMENT '扩展配置（JSON格式）',
    `remark` VARCHAR(500) COMMENT '备注',
    
    -- 审计字段
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除, 1-已删除',
    
    -- 主键约束
    PRIMARY KEY (`interlock_device_id`),
    
    -- 唯一键约束
    UNIQUE KEY `uk_interlock_device` (`interlock_id`, `device_id`) USING BTREE,
    
    -- 普通索引
    INDEX `idx_interlock_id` (`interlock_id`) USING BTREE,
    INDEX `idx_device_id` (`device_id`) USING BTREE,
    INDEX `idx_device_sequence` (`interlock_id`, `device_sequence`) USING BTREE,
    INDEX `idx_device_priority` (`device_priority` DESC) USING BTREE,
    INDEX `idx_current_status` (`current_status`) USING BTREE
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='互锁设备关联表';

-- =============================================
-- 互锁记录表（记录每次互锁事件）
-- =============================================
CREATE TABLE IF NOT EXISTS `t_access_interlock_record` (
    -- 主键ID
    `interlock_record_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '互锁记录ID',
    
    -- 关联信息
    `interlock_id` BIGINT NOT NULL COMMENT '互锁ID',
    `device_id` BIGINT NOT NULL COMMENT '触发设备ID',
    `person_id` BIGINT COMMENT '触发人员ID',
    
    -- 互锁事件
    `event_type` VARCHAR(50) NOT NULL COMMENT '事件类型：OPEN-开门, CLOSE-关门, LOCK-上锁, UNLOCK-解锁, VIOLATION-违规',
    `event_time` DATETIME NOT NULL COMMENT '事件时间',
    `event_desc` VARCHAR(500) COMMENT '事件描述',
    
    -- 互锁状态
    `interlock_result` VARCHAR(50) NOT NULL COMMENT '互锁结果：PASS-通过, BLOCKED-阻止, VIOLATION-违规',
    `violation_reason` VARCHAR(500) COMMENT '违规原因',
    
    -- 处理结果
    `handle_action` VARCHAR(50) COMMENT '处理动作：ALLOW-允许, DENY-拒绝, ALARM-告警, FORCE_CLOSE-强制关闭',
    `handle_result` VARCHAR(500) COMMENT '处理结果描述',
    `handle_time` DATETIME COMMENT '处理时间',
    `handle_user_id` BIGINT COMMENT '处理人ID',
    
    -- 扩展字段
    `extend_json` JSON COMMENT '扩展信息（JSON格式）',
    `remark` VARCHAR(500) COMMENT '备注',
    
    -- 审计字段
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除, 1-已删除',
    
    -- 主键约束
    PRIMARY KEY (`interlock_record_id`),
    
    -- 普通索引
    INDEX `idx_interlock_id` (`interlock_id`) USING BTREE,
    INDEX `idx_device_time` (`device_id`, `event_time` DESC) USING BTREE,
    INDEX `idx_person_time` (`person_id`, `event_time` DESC) USING BTREE,
    INDEX `idx_event_type` (`event_type`, `interlock_result`) USING BTREE,
    INDEX `idx_event_time` (`event_time` DESC) USING BTREE
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='互锁记录表';

-- =============================================
-- 初始化数据（企业级默认配置）
-- =============================================

-- 1. 安全通道严格互锁配置
INSERT INTO `t_access_interlock` (
    `interlock_code`, `interlock_name`, `interlock_type`, `interlock_desc`,
    `interlock_mode`, `max_open_count`, `sequence_required`,
    `open_timeout_seconds`, `unlock_delay_seconds`, `auto_lock`,
    `exception_mode`, `alarm_enabled`, `alarm_delay_seconds`,
    `device_count`, `interlock_status`, `enabled_flag`, `remark`
) VALUES (
    'INTERLOCK_SECURITY_001', '主入口安全通道互锁', 'SECURITY', '主入口安全通道，严格互锁控制',
    'STRICT', 1, 0,
    30, 5, 1,
    'FORCE_CLOSE', 1, 10,
    2, 1, 1, '安全通道严格互锁，一次只能开一个门'
);

-- 2. 消防通道顺序互锁配置
INSERT INTO `t_access_interlock` (
    `interlock_code`, `interlock_name`, `interlock_type`, `interlock_desc`,
    `interlock_mode`, `max_open_count`, `sequence_required`, `open_sequence`,
    `open_timeout_seconds`, `unlock_delay_seconds`, `auto_lock`,
    `exception_mode`, `alarm_enabled`, `alarm_delay_seconds`,
    `device_count`, `week_days`, `interlock_status`, `enabled_flag`, `remark`
) VALUES (
    'INTERLOCK_FIRE_001', '消防通道顺序互锁', 'FIRE', '消防通道必须按顺序通行',
    'SEQUENCE', 1, 1, '["1", "2", "3"]',
    60, 3, 1,
    'ALARM', 1, 5,
    3, '1,2,3,4,5', 1, 1, '工作日消防通道按序通行'
);

-- 3. 洁净室灵活互锁配置
INSERT INTO `t_access_interlock` (
    `interlock_code`, `interlock_name`, `interlock_type`, `interlock_desc`,
    `interlock_mode`, `max_open_count`, `sequence_required`,
    `open_timeout_seconds`, `unlock_delay_seconds`, `auto_lock`,
    `exception_mode`, `alarm_enabled`, `alarm_delay_seconds`,
    `device_count`, `interlock_status`, `enabled_flag`, `remark`
) VALUES (
    'INTERLOCK_CLEAN_001', '洁净室互锁控制', 'CLEAN', '洁净室压差控制互锁',
    'FLEXIBLE', 2, 0,
    45, 10, 1,
    'ALARM', 1, 15,
    4, 1, 1, '洁净室允许最多2个门同时开启'
);

-- =============================================
-- 互锁设备关联初始化数据
-- =============================================

-- 安全通道互锁设备（2个设备）
INSERT INTO `t_access_interlock_device` (
    `interlock_id`, `device_id`, `device_role`, `device_sequence`, `device_priority`,
    `lock_when_open`, `unlock_when_close`, `force_close_others`,
    `max_open_duration`, `min_close_duration`, `remark`
) VALUES
(1, 1, 'MASTER', 1, 10, 1, 1, 1, 30, 3, '安全通道外门（主门）'),
(1, 2, 'SLAVE', 2, 8, 1, 1, 0, 30, 3, '安全通道内门（从门）');

-- 消防通道互锁设备（3个设备，按序通行）
INSERT INTO `t_access_interlock_device` (
    `interlock_id`, `device_id`, `device_role`, `device_sequence`, `device_priority`,
    `lock_when_open`, `unlock_when_close`, `force_close_others`,
    `max_open_duration`, `min_close_duration`, `remark`
) VALUES
(2, 3, 'NORMAL', 1, 9, 1, 1, 0, 60, 3, '消防通道第一道门'),
(2, 4, 'NORMAL', 2, 9, 1, 1, 0, 60, 3, '消防通道第二道门'),
(2, 5, 'NORMAL', 3, 9, 1, 1, 0, 60, 3, '消防通道第三道门');

-- =============================================
-- 索引优化说明
-- =============================================
-- 1. uk_interlock_code: 互锁编码唯一索引，确保互锁配置唯一性
-- 2. idx_interlock_type: 互锁类型索引，支持按类型查询
-- 3. idx_interlock_mode: 互锁模式和状态联合索引
-- 4. idx_interlock_device: 互锁和设备联合唯一索引，确保设备不重复
-- 5. idx_device_sequence: 设备顺序索引，支持顺序互锁查询
-- 6. idx_device_time: 设备和时间联合索引（记录表），支持设备互锁历史查询

-- =============================================
-- 使用示例
-- =============================================
-- 1. 查询所有启用的互锁配置
-- SELECT * FROM t_access_interlock 
-- WHERE interlock_status = 1 AND enabled_flag = 1 AND deleted_flag = 0;

-- 2. 查询指定互锁组的所有设备
-- SELECT * FROM t_access_interlock_device 
-- WHERE interlock_id = 1 AND deleted_flag = 0 
-- ORDER BY device_sequence ASC;

-- 3. 查询设备当前互锁状态
-- SELECT d.*, i.interlock_name, i.interlock_mode 
-- FROM t_access_interlock_device d
-- JOIN t_access_interlock i ON d.interlock_id = i.interlock_id
-- WHERE d.device_id = 1 AND d.deleted_flag = 0;

-- 4. 记录互锁事件
-- INSERT INTO t_access_interlock_record (interlock_id, device_id, person_id, event_type, event_time, interlock_result)
-- VALUES (1, 1, 1001, 'OPEN', NOW(), 'PASS');

-- 5. 统计互锁违规次数
-- SELECT interlock_id, COUNT(*) as violation_count 
-- FROM t_access_interlock_record 
-- WHERE interlock_result = 'VIOLATION' AND DATE(event_time) = CURDATE() 
-- GROUP BY interlock_id;

-- =============================================
-- 维护建议
-- =============================================
-- 1. 定期清理过期的互锁记录（根据数据保留策略）
-- 2. 监控互锁违规率，及时调整配置
-- 3. 定期测试互锁功能的可靠性
-- 4. 建立互锁异常告警机制
-- 5. 定期审查互锁设备的状态准确性
-- 6. 优化互锁判断逻辑的性能
