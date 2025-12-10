-- =============================================
-- IOE-DREAM 智慧园区一卡通管理平台
-- APB反潜回配置表 (t_access_apb_config)
-- =============================================
-- 功能说明: APB（Anti-Passback）反潜回配置表，防止同一人员短时间内重复通行
-- 业务场景: 
--   1. 防止一人刷卡多人进入（跟随进入）
--   2. 防止借卡给他人使用
--   3. 强制进出配对验证
--   4. 区域进出人数统计
-- 企业级特性:
--   - 支持软APB和硬APB两种模式
--   - 支持全局APB和区域APB
--   - 支持APB超时自动解除
--   - 支持异常APB记录和告警
-- =============================================

USE `ioedream_access_db`;

-- =============================================
-- 表结构定义
-- =============================================
CREATE TABLE IF NOT EXISTS `t_access_apb_config` (
    -- 主键ID
    `apb_config_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'APB配置ID',
    
    -- APB配置基本信息
    `apb_code` VARCHAR(100) NOT NULL COMMENT 'APB配置编码（唯一标识）',
    `apb_name` VARCHAR(200) NOT NULL COMMENT 'APB配置名称',
    `apb_type` VARCHAR(50) NOT NULL COMMENT 'APB类型：SOFT-软APB（警告）, HARD-硬APB（禁止）',
    `apb_scope` VARCHAR(50) NOT NULL COMMENT 'APB范围：GLOBAL-全局, AREA-区域, DEVICE-设备',
    `apb_desc` VARCHAR(500) COMMENT 'APB描述',
    
    -- 关联配置
    `area_id` BIGINT COMMENT '关联区域ID（apb_scope=AREA时必填）',
    `device_id` BIGINT COMMENT '关联设备ID（apb_scope=DEVICE时必填）',
    `device_group_id` BIGINT COMMENT '关联设备组ID（可选）',
    
    -- APB规则配置
    `apb_mode` VARCHAR(50) NOT NULL COMMENT 'APB模式：STANDARD-标准（进出配对）, STRICT-严格（强制按顺序）, FLEXIBLE-灵活（允许异常）',
    `check_direction` TINYINT DEFAULT 1 COMMENT '检查方向：0-不检查, 1-进出双向检查',
    `require_pair` TINYINT DEFAULT 1 COMMENT '要求配对：0-不要求, 1-进必出/出必进',
    
    -- 时间控制
    `timeout_minutes` INT DEFAULT 0 COMMENT 'APB超时时间（分钟，0表示永不超时）',
    `auto_clear` TINYINT DEFAULT 1 COMMENT '自动清除：0-不自动清除, 1-超时自动清除',
    `clear_time` TIME COMMENT '每日清除时间（如：23:59:00，NULL表示不定时清除）',
    
    -- 异常处理
    `exception_mode` VARCHAR(50) DEFAULT 'ALARM' COMMENT '异常处理模式：ALARM-告警, BLOCK-阻止, ALLOW-允许但记录',
    `max_violations` INT DEFAULT 3 COMMENT '最大违规次数（超过后执行异常处理）',
    `violation_timeout` INT DEFAULT 60 COMMENT '违规超时时间（分钟，超时后计数重置）',
    
    -- 应用对象
    `apply_person_type` VARCHAR(200) COMMENT '适用人员类型：EMPLOYEE-员工, VISITOR-访客, VIP-贵宾（多个用逗号分隔，NULL表示全部）',
    `exclude_person_ids` TEXT COMMENT '排除人员ID列表（JSON数组）：["1001", "1002"]',
    `exclude_role_ids` TEXT COMMENT '排除角色ID列表（JSON数组）：["ADMIN", "SECURITY"]',
    
    -- 时间段控制
    `time_period_id` BIGINT COMMENT '生效时间段ID（NULL表示全天）',
    `week_days` VARCHAR(50) COMMENT '生效星期：1,2,3,4,5（NULL表示全周）',
    `holiday_mode` VARCHAR(50) DEFAULT 'NORMAL' COMMENT '节假日模式：NORMAL-正常, DISABLE-禁用, SPECIAL-特殊规则',
    
    -- APB状态
    `apb_status` TINYINT DEFAULT 1 COMMENT 'APB状态：0-禁用, 1-启用',
    `enabled_flag` TINYINT DEFAULT 1 COMMENT '启用标记：0-停用, 1-启用',
    
    -- 统计信息
    `total_checks` INT DEFAULT 0 COMMENT '总检查次数',
    `violation_count` INT DEFAULT 0 COMMENT '违规次数统计',
    `block_count` INT DEFAULT 0 COMMENT '阻止次数统计',
    `last_check_time` DATETIME COMMENT '最后检查时间',
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
    PRIMARY KEY (`apb_config_id`),
    
    -- 唯一键约束
    UNIQUE KEY `uk_apb_code` (`apb_code`) USING BTREE,
    
    -- 普通索引
    INDEX `idx_apb_type_scope` (`apb_type`, `apb_scope`, `apb_status`) USING BTREE,
    INDEX `idx_area_id` (`area_id`) USING BTREE,
    INDEX `idx_device_id` (`device_id`) USING BTREE,
    INDEX `idx_apb_status` (`apb_status`, `enabled_flag`) USING BTREE,
    INDEX `idx_create_time` (`create_time` DESC) USING BTREE
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='APB反潜回配置表';

-- =============================================
-- APB记录表（记录每次APB检查结果）
-- =============================================
CREATE TABLE IF NOT EXISTS `t_access_apb_record` (
    -- 主键ID
    `apb_record_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'APB记录ID',
    
    -- 关联信息
    `apb_config_id` BIGINT NOT NULL COMMENT 'APB配置ID',
    `person_id` BIGINT NOT NULL COMMENT '人员ID',
    `device_id` BIGINT NOT NULL COMMENT '设备ID',
    `area_id` BIGINT COMMENT '区域ID',
    
    -- 通行信息
    `access_direction` VARCHAR(50) NOT NULL COMMENT '通行方向：IN-进, OUT-出',
    `access_time` DATETIME NOT NULL COMMENT '通行时间',
    `access_record_id` BIGINT COMMENT '关联通行记录ID',
    
    -- APB检查结果
    `apb_status` VARCHAR(50) NOT NULL COMMENT 'APB状态：PASS-通过, VIOLATION-违规, BLOCKED-阻止',
    `violation_type` VARCHAR(50) COMMENT '违规类型：NO_ENTRY-未入先出, NO_EXIT-未出再入, TIMEOUT-超时',
    `violation_desc` VARCHAR(500) COMMENT '违规描述',
    
    -- 处理结果
    `handle_action` VARCHAR(50) COMMENT '处理动作：ALLOW-允许, DENY-拒绝, ALARM-告警',
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
    PRIMARY KEY (`apb_record_id`),
    
    -- 普通索引
    INDEX `idx_config_id` (`apb_config_id`) USING BTREE,
    INDEX `idx_person_time` (`person_id`, `access_time` DESC) USING BTREE,
    INDEX `idx_device_time` (`device_id`, `access_time` DESC) USING BTREE,
    INDEX `idx_apb_status` (`apb_status`, `violation_type`) USING BTREE,
    INDEX `idx_access_time` (`access_time` DESC) USING BTREE
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='APB检查记录表';

-- =============================================
-- 初始化数据（企业级默认配置）
-- =============================================

-- 1. 全局标准APB配置（软APB）
INSERT INTO `t_access_apb_config` (
    `apb_code`, `apb_name`, `apb_type`, `apb_scope`, `apb_desc`,
    `apb_mode`, `check_direction`, `require_pair`,
    `timeout_minutes`, `auto_clear`, `clear_time`,
    `exception_mode`, `max_violations`, `violation_timeout`,
    `apply_person_type`, `apb_status`, `enabled_flag`, `remark`
) VALUES (
    'APB_GLOBAL_SOFT', '全局软APB配置', 'SOFT', 'GLOBAL', '全局软APB，违规时告警但允许通行',
    'STANDARD', 1, 1,
    480, 1, '23:59:00',
    'ALARM', 3, 60,
    'EMPLOYEE,VISITOR', 1, 1, '标准APB配置，8小时超时自动清除'
);

-- 2. 核心区域硬APB配置（严格模式）
INSERT INTO `t_access_apb_config` (
    `apb_code`, `apb_name`, `apb_type`, `apb_scope`, `apb_desc`,
    `area_id`, `apb_mode`, `check_direction`, `require_pair`,
    `timeout_minutes`, `auto_clear`, `clear_time`,
    `exception_mode`, `max_violations`, `violation_timeout`,
    `apply_person_type`, `week_days`, `apb_status`, `enabled_flag`, `remark`
) VALUES (
    'APB_CORE_HARD', '核心区域硬APB', 'HARD', 'AREA', '核心区域严格APB，违规时阻止通行',
    1, 'STRICT', 1, 1,
    0, 0, NULL,
    'BLOCK', 1, 30,
    'EMPLOYEE,VISITOR,VIP', '1,2,3,4,5', 1, 1, '核心区域严格控制，工作日启用'
);

-- 3. 访客专用APB配置（灵活模式）
INSERT INTO `t_access_apb_config` (
    `apb_code`, `apb_name`, `apb_type`, `apb_scope`, `apb_desc`,
    `apb_mode`, `check_direction`, `require_pair`,
    `timeout_minutes`, `auto_clear`, `clear_time`,
    `exception_mode`, `max_violations`, `violation_timeout`,
    `apply_person_type`, `apb_status`, `enabled_flag`, `remark`
) VALUES (
    'APB_VISITOR_FLEX', '访客APB配置', 'SOFT', 'GLOBAL', '访客专用APB，灵活处理异常情况',
    'FLEXIBLE', 1, 0,
    240, 1, '18:00:00',
    'ALARM', 5, 120,
    'VISITOR', 1, 1, '访客APB，4小时超时，允许少量异常'
);

-- 4. 夜间严格APB配置
INSERT INTO `t_access_apb_config` (
    `apb_code`, `apb_name`, `apb_type`, `apb_scope`, `apb_desc`,
    `apb_mode`, `check_direction`, `require_pair`,
    `timeout_minutes`, `auto_clear`, `clear_time`,
    `exception_mode`, `max_violations`, `violation_timeout`,
    `apply_person_type`, `week_days`, `apb_status`, `enabled_flag`, `remark`
) VALUES (
    'APB_NIGHT_STRICT', '夜间严格APB', 'HARD', 'GLOBAL', '夜间时段严格APB控制',
    'STRICT', 1, 1,
    0, 1, '06:00:00',
    'BLOCK', 1, 15,
    NULL, '1,2,3,4,5,6,7', 1, 1, '夜间严格控制，清晨6点自动清除'
);

-- =============================================
-- 索引优化说明
-- =============================================
-- 1. uk_apb_code: APB配置编码唯一索引，确保配置唯一性
-- 2. idx_apb_type_scope: APB类型和范围联合索引，支持快速查询
-- 3. idx_area_id: 区域索引，支持区域级APB查询
-- 4. idx_device_id: 设备索引，支持设备级APB查询
-- 5. idx_apb_status: APB状态索引，支持状态过滤
-- 6. idx_person_time: 人员和时间联合索引（APB记录表），支持个人APB历史查询
-- 7. idx_device_time: 设备和时间联合索引（APB记录表），支持设备APB查询

-- =============================================
-- 使用示例
-- =============================================
-- 1. 查询所有启用的APB配置
-- SELECT * FROM t_access_apb_config 
-- WHERE apb_status = 1 AND enabled_flag = 1 AND deleted_flag = 0;

-- 2. 查询指定区域的APB配置
-- SELECT * FROM t_access_apb_config 
-- WHERE area_id = 1 AND deleted_flag = 0;

-- 3. 检查人员APB状态（最后一次进出记录）
-- SELECT * FROM t_access_apb_record 
-- WHERE person_id = 1001 AND deleted_flag = 0 
-- ORDER BY access_time DESC LIMIT 1;

-- 4. 统计APB违规次数
-- SELECT apb_config_id, COUNT(*) as violation_count 
-- FROM t_access_apb_record 
-- WHERE apb_status = 'VIOLATION' AND DATE(access_time) = CURDATE() 
-- GROUP BY apb_config_id;

-- 5. 更新APB统计信息
-- UPDATE t_access_apb_config 
-- SET total_checks = total_checks + 1, 
--     violation_count = violation_count + 1,
--     last_check_time = NOW(),
--     last_violation_time = NOW() 
-- WHERE apb_config_id = 1;

-- =============================================
-- 维护建议
-- =============================================
-- 1. 定期清理过期的APB记录（根据数据保留策略）
-- 2. 监控APB违规率，及时调整配置参数
-- 3. 定期审查APB配置的合理性和有效性
-- 4. 优化APB检查算法的性能
-- 5. 建立APB异常告警机制
-- 6. 定期备份APB配置和关键记录数据
