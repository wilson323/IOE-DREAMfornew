-- ============================================================
-- Mock配置表
-- ============================================================
-- 功能: 存储规则测试的Mock配置
-- 作者: IOE-DREAM架构团队
-- 日期: 2025-12-26
-- 版本: 1.0.0
-- ============================================================

CREATE TABLE IF NOT EXISTS `t_attendance_mock_config` (
    -- 主键
    `config_id` BIGINT NOT NULL COMMENT '配置ID',

    -- 配置基本信息
    `config_name` VARCHAR(200) NOT NULL COMMENT '配置名称',
    `config_type` VARCHAR(50) NOT NULL COMMENT '配置类型：EMPLOYEE-员工数据/DEPARTMENT-部门数据/SHIFT-班次数据/ATTENDANCE-考勤数据',
    `mock_scenario` VARCHAR(50) NOT NULL COMMENT 'Mock场景：NORMAL-正常场景/EDGE_CASE-边界场景/ERROR-异常场景/PERFORMANCE-性能场景',

    -- Mock状态
    `mock_status` VARCHAR(20) NOT NULL DEFAULT 'ENABLED' COMMENT 'Mock状态：ENABLED-启用/DISABLED-禁用',

    -- 数据生成配置
    `generation_rules` TEXT COMMENT '数据生成规则（JSON格式）',
    `data_template` TEXT COMMENT 'Mock数据模板（JSON格式）',

    -- 性能模拟配置
    `response_delay_ms` BIGINT COMMENT '返回延迟（毫秒）',
    `error_rate` DECIMAL(5,2) COMMENT '错误率（百分比）',
    `timeout_rate` DECIMAL(5,2) COMMENT '超时率（百分比）',

    -- 随机延迟配置
    `enable_random_delay` TINYINT DEFAULT 0 COMMENT '是否启用随机延迟',
    `random_delay_min_ms` BIGINT COMMENT '随机延迟最小值（毫秒）',
    `random_delay_max_ms` BIGINT COMMENT '随机延迟最大值（毫秒）',

    -- 统计信息
    `usage_count` BIGINT NOT NULL DEFAULT 0 COMMENT '使用次数统计',
    `last_used_time` DATETIME COMMENT '最后使用时间',

    -- 描述信息
    `config_description` TEXT COMMENT '配置描述',

    -- 审计字段
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',

    PRIMARY KEY (`config_id`),
    KEY `idx_config_type` (`config_type`),
    KEY `idx_mock_scenario` (`mock_scenario`),
    KEY `idx_mock_status` (`mock_status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Mock配置表';

-- ============================================================
-- 插入示例数据
-- ============================================================
INSERT INTO `t_attendance_mock_config` (
    `config_id`,
    `config_name`,
    `config_type`,
    `mock_scenario`,
    `mock_status`,
    `generation_rules`,
    `response_delay_ms`,
    `error_rate`,
    `timeout_rate`,
    `enable_random_delay`,
    `random_delay_min_ms`,
    `random_delay_max_ms`,
    `config_description`
) VALUES
(
    1,
    '员工数据Mock-正常场景',
    'EMPLOYEE',
    'NORMAL',
    'ENABLED',
    '{"dataCount": 100, "randomSeed": 12345}',
    100,
    0.0,
    0.0,
    0,
    50,
    200,
    '生成员工正常测试数据'
),
(
    2,
    '员工数据Mock-边界场景',
    'EMPLOYEE',
    'EDGE_CASE',
    'ENABLED',
    '{"dataCount": 1, "randomSeed": 54321}',
    200,
    0.0,
    0.0,
    1,
    100,
    300,
    '生成员工边界测试数据（最小数据量）'
),
(
    3,
    '考勤数据Mock-正常场景',
    'ATTENDANCE',
    'NORMAL',
    'ENABLED',
    '{"dataCount": 500, "randomSeed": 99999}',
    50,
    0.0,
    0.0,
    0,
    NULL,
    NULL,
    '生成考勤正常测试数据'
),
(
    4,
    '班次数据Mock-正常场景',
    'SHIFT',
    'NORMAL',
    'ENABLED',
    '{"dataCount": 50, "randomSeed": 11111}',
    80,
    0.0,
    0.0,
    0,
    NULL,
    NULL,
    '生成班次正常测试数据'
);

-- ============================================================
-- 索引说明
-- ============================================================
-- idx_config_type: 按配置类型查询，方便按类型获取Mock配置
-- idx_mock_scenario: 按Mock场景查询，方便按场景筛选配置
-- idx_mock_status: 按Mock状态查询，方便查询启用的配置
-- idx_create_time: 按创建时间排序，查询最近的配置记录
