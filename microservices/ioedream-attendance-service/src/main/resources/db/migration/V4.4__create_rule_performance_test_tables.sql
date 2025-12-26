-- ============================================================
-- 规则性能测试表
-- ============================================================
-- 功能: 存储规则性能测试执行记录和统计结果
-- 作者: IOE-DREAM架构团队
-- 日期: 2025-12-26
-- 版本: 1.0.0
-- ============================================================

CREATE TABLE IF NOT EXISTS `t_attendance_rule_performance_test` (
    -- 主键
    `test_id` BIGINT NOT NULL COMMENT '测试ID',

    -- 测试基本信息
    `test_name` VARCHAR(200) NOT NULL COMMENT '测试名称',
    `test_type` VARCHAR(20) NOT NULL COMMENT '测试类型：SINGLE-单规则/BATCH-批量规则/CONCURRENT-并发测试',

    -- 测试配置
    `rule_ids` TEXT COMMENT '测试的规则ID列表（JSON）',
    `rule_count` INT NOT NULL DEFAULT 0 COMMENT '规则数量',
    `concurrent_users` INT NOT NULL DEFAULT 1 COMMENT '并发用户数',

    -- 请求统计
    `total_requests` INT NOT NULL DEFAULT 0 COMMENT '总请求数',
    `success_requests` INT NOT NULL DEFAULT 0 COMMENT '成功请求数',
    `failed_requests` INT NOT NULL DEFAULT 0 COMMENT '失败请求数',
    `success_rate` DECIMAL(5,2) COMMENT '成功率（百分比）',

    -- 响应时间统计（毫秒）
    `min_response_time` BIGINT COMMENT '最小响应时间（毫秒）',
    `max_response_time` BIGINT COMMENT '最大响应时间（毫秒）',
    `avg_response_time` DECIMAL(10,2) COMMENT '平均响应时间（毫秒）',
    `p50_response_time` BIGINT COMMENT 'P50响应时间（中位数，毫秒）',
    `p95_response_time` BIGINT COMMENT 'P95响应时间（95%请求，毫秒）',
    `p99_response_time` BIGINT COMMENT 'P99响应时间（99%请求，毫秒）',

    -- 吞吐量统计
    `throughput` DECIMAL(10,2) COMMENT '吞吐量（TPS-每秒事务数）',
    `qps` DECIMAL(10,2) COMMENT 'QPS（每秒查询数）',

    -- 执行信息
    `start_time` DATETIME COMMENT '测试开始时间',
    `end_time` DATETIME COMMENT '测试结束时间',
    `duration_seconds` BIGINT COMMENT '测试时长（秒）',
    `test_status` VARCHAR(20) NOT NULL COMMENT '测试状态：RUNNING-运行中/COMPLETED-已完成/CANCELLED-已取消/ERROR-错误',
    `error_message` TEXT COMMENT '错误信息',

    -- 详细数据（JSON）
    `response_time_distribution` TEXT COMMENT '响应时间分布（JSON）',
    `slow_requests` TEXT COMMENT '慢请求列表（JSON）',
    `performance_metrics` TEXT COMMENT '性能指标详情（JSON）',

    -- 审计字段
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
    `created_by` BIGINT COMMENT '创建人ID',
    `created_by_name` VARCHAR(100) COMMENT '创建人姓名',

    PRIMARY KEY (`test_id`),
    KEY `idx_test_type` (`test_type`),
    KEY `idx_test_status` (`test_status`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_created_by` (`created_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='规则性能测试表';

-- ============================================================
-- 插入示例数据
-- ============================================================
INSERT INTO `t_attendance_rule_performance_test` (
    `test_id`,
    `test_name`,
    `test_type`,
    `rule_ids`,
    `rule_count`,
    `concurrent_users`,
    `total_requests`,
    `success_requests`,
    `failed_requests`,
    `success_rate`,
    `min_response_time`,
    `max_response_time`,
    `avg_response_time`,
    `p50_response_time`,
    `p95_response_time`,
    `p99_response_time`,
    `throughput`,
    `qps`,
    `start_time`,
    `end_time`,
    `duration_seconds`,
    `test_status`,
    `created_by`,
    `created_by_name`
) VALUES
(
    1,
    '早高峰考勤规则性能测试',
    'CONCURRENT',
    '[1, 2, 3]',
    3,
    10,
    1000,
    950,
    50,
    95.00,
    15,
    250,
    65.5,
    60,
    150,
    200,
    95.0,
    100.0,
    DATE_SUB(NOW(), INTERVAL 1 HOUR),
    DATE_SUB(NOW(), INTERVAL 59 MINUTE),
    60,
    'COMPLETED',
    1,
    'admin'
),
(
    2,
    '批量规则压力测试',
    'BATCH',
    '[4, 5, 6, 7, 8]',
    5,
    20,
    2000,
    1900,
    100,
    95.00,
    20,
    300,
    78.3,
    70,
    180,
    250,
    190.0,
    200.0,
    DATE_SUB(NOW(), INTERVAL 2 HOUR),
    DATE_SUB(NOW(), INTERVAL 1 HOUR 57 MINUTE),
    120,
    'COMPLETED',
    1,
    'admin'
);

-- ============================================================
-- 索引说明
-- ============================================================
-- idx_test_type: 按测试类型查询，方便统计不同类型的性能测试
-- idx_test_status: 按测试状态查询，方便查询正在运行的测试
-- idx_create_time: 按创建时间排序，查询最近的测试记录
-- idx_created_by: 按创建人查询，查看某个用户的所有测试
