-- ============================================================
-- 规则覆盖率报告表
-- ============================================================
-- 功能: 存储规则覆盖率统计结果和趋势数据
-- 作者: IOE-DREAM架构团队
-- 日期: 2025-12-26
-- 版本: 1.0.0
-- ============================================================

CREATE TABLE IF NOT EXISTS `t_attendance_rule_coverage_report` (
    -- 主键
    `report_id` BIGINT NOT NULL COMMENT '报告ID',

    -- 报告基本信息
    `report_date` DATE NOT NULL COMMENT '报告日期',
    `report_type` VARCHAR(20) NOT NULL COMMENT '报告类型：DAILY-日报/WEEKLY-周报/MONTHLY-月报/CUSTOM-自定义',

    -- 覆盖率统计
    `total_rules` INT NOT NULL DEFAULT 0 COMMENT '总规则数量',
    `tested_rules` INT NOT NULL DEFAULT 0 COMMENT '已测试规则数量',
    `coverage_rate` DECIMAL(5,2) COMMENT '覆盖率（百分比）',

    -- 测试统计
    `total_tests` INT NOT NULL DEFAULT 0 COMMENT '测试总次数',
    `success_tests` INT NOT NULL DEFAULT 0 COMMENT '测试成功次数',
    `failed_tests` INT NOT NULL DEFAULT 0 COMMENT '测试失败次数',
    `success_rate` DECIMAL(5,2) COMMENT '测试成功率（百分比）',

    -- 详细数据（JSON格式）
    `coverage_details` TEXT COMMENT '覆盖率详情（JSON）- 每个规则的覆盖情况',
    `uncovered_rules` TEXT COMMENT '未覆盖规则列表（JSON）',
    `low_coverage_rules` TEXT COMMENT '低覆盖率规则列表（JSON）',

    -- 日期范围（用于周报、月报）
    `start_date` DATE COMMENT '开始日期',
    `end_date` DATE COMMENT '结束日期',

    -- 执行信息
    `report_status` VARCHAR(20) NOT NULL COMMENT '报告状态：GENERATING-生成中/COMPLETED-已完成/ERROR-错误',
    `generation_time_ms` BIGINT COMMENT '生成耗时（毫秒）',
    `error_message` TEXT COMMENT '错误信息',

    -- 审计字段
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',

    PRIMARY KEY (`report_id`),
    KEY `idx_report_date` (`report_date`),
    KEY `idx_report_type` (`report_type`),
    KEY `idx_report_status` (`report_status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='规则覆盖率报告表';

-- ============================================================
-- 插入示例数据
-- ============================================================
INSERT INTO `t_attendance_rule_coverage_report` (
    `report_id`,
    `report_date`,
    `report_type`,
    `total_rules`,
    `tested_rules`,
    `coverage_rate`,
    `total_tests`,
    `success_tests`,
    `failed_tests`,
    `success_rate`,
    `start_date`,
    `end_date`,
    `report_status`,
    `generation_time_ms`
) VALUES
(
    1,
    CURDATE() - INTERVAL 2 DAY,
    'DAILY',
    50,
    38,
    76.00,
    320,
    304,
    16,
    95.00,
    CURDATE() - INTERVAL 2 DAY,
    CURDATE() - INTERVAL 2 DAY,
    'COMPLETED',
    2100
),
(
    2,
    CURDATE() - INTERVAL 1 DAY,
    'DAILY',
    50,
    40,
    80.00,
    350,
    335,
    15,
    95.71,
    CURDATE() - INTERVAL 1 DAY,
    CURDATE() - INTERVAL 1 DAY,
    'COMPLETED',
    2250
),
(
    3,
    CURDATE(),
    'DAILY',
    50,
    42,
    84.00,
    380,
    361,
    19,
    95.00,
    CURDATE(),
    CURDATE(),
    'COMPLETED',
    2300
);

-- ============================================================
-- 索引说明
-- ============================================================
-- idx_report_date: 按报告日期查询，用于趋势分析
-- idx_report_type: 按报告类型查询，方便统计不同类型的报告
-- idx_report_status: 按报告状态查询，查询已完成的报告
-- idx_create_time: 按创建时间排序，查询最近的报告记录
