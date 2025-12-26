-- ============================================================================
-- IOE-DREAM 访客服务自助签离表
-- Task 12: 自助签离管理
-- Version: V20251226__005
-- Author: IOE-DREAM Team
-- Date: 2025-12-26
-- ============================================================================

-- 创建自助签离记录表
CREATE TABLE IF NOT EXISTS `t_visitor_self_check_out` (
    `check_out_id`           BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '签离记录ID',
    `registration_id`        BIGINT          NOT NULL                 COMMENT '登记ID',
    `visitor_code`           VARCHAR(30)     NOT NULL                 COMMENT '访客码',
    `visitor_name`           VARCHAR(50)     NOT NULL                 COMMENT '访客姓名',
    `check_out_time`         DATETIME        NOT NULL                 COMMENT '签离时间',
    `visit_duration`         INT             NOT NULL                 COMMENT '访问时长(分钟)',
    `is_overtime`            TINYINT         NOT NULL DEFAULT 0      COMMENT '是否超时: 0-否 1-是',
    `overtime_duration`      INT             NOT NULL DEFAULT 0      COMMENT '超时时长(分钟)',
    `check_out_method`       TINYINT         NOT NULL                 COMMENT '签离方式: 1-自助签离 2-人工签离',
    `check_out_status`       TINYINT         NOT NULL DEFAULT 0      COMMENT '签离状态: 0-待签离 1-已完成 2-已取消',
    `terminal_id`            VARCHAR(50)     NOT NULL                 COMMENT '签离终端ID',
    `terminal_location`      VARCHAR(100)                             COMMENT '签离终端位置',
    `card_return_status`     TINYINT         NOT NULL                 COMMENT '卡归还状态: 0-未归还 1-已归还 2-卡遗失',
    `visitor_card`           VARCHAR(50)                                 COMMENT '访客卡号',
    `operator_id`            BIGINT                                      COMMENT '操作人ID(人工签离时)',
    `operator_name`          VARCHAR(50)                                 COMMENT '操作人姓名(人工签离时)',
    `manual_reason`          VARCHAR(500)                                COMMENT '人工签离原因',
    `satisfaction_score`     TINYINT                                      COMMENT '满意度评分(1-5分)',
    `visitor_feedback`       TEXT                                         COMMENT '访客反馈',
    `feedback_time`          DATETIME                                     COMMENT '反馈时间',
    `remark`                 VARCHAR(500)                                COMMENT '备注',
    `create_time`            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag`           TINYINT         NOT NULL DEFAULT 0      COMMENT '删除标记: 0-未删除 1-已删除',
    PRIMARY KEY (`check_out_id`),
    UNIQUE KEY `uk_checkout_visitor_code` (`visitor_code`),
    INDEX `idx_checkout_registration` (`registration_id`),
    INDEX `idx_checkout_time` (`check_out_time`),
    INDEX `idx_checkout_status` (`check_out_status`),
    INDEX `idx_checkout_overtime` (`is_overtime`),
    FOREIGN KEY (`registration_id`) REFERENCES `t_visitor_self_service_registration`(`registration_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='自助签离记录表';

-- 创建访客满意度统计表
CREATE TABLE IF NOT EXISTS `t_visitor_satisfaction_statistics` (
    `statistics_id`          BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '统计ID',
    `stat_date`              DATE            NOT NULL                 COMMENT '统计日期',
    `total_count`            INT             NOT NULL DEFAULT 0      COMMENT '总评价数',
    `average_score`          DECIMAL(3,2)    NOT NULL DEFAULT 0.00   COMMENT '平均评分',
    `score_5_count`          INT             NOT NULL DEFAULT 0      COMMENT '5分评价数',
    `score_4_count`          INT             NOT NULL DEFAULT 0      COMMENT '4分评价数',
    `score_3_count`          INT             NOT NULL DEFAULT 0      COMMENT '3分评价数',
    `score_2_count`          INT             NOT NULL DEFAULT 0      COMMENT '2分评价数',
    `score_1_count`          INT             NOT NULL DEFAULT 0      COMMENT '1分评价数',
    `satisfaction_rate`      DECIMAL(5,2)    NOT NULL DEFAULT 0.00   COMMENT '满意度(%)',
    `create_time`            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`statistics_id`),
    UNIQUE KEY `uk_stat_date` (`stat_date`),
    INDEX `idx_stat_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='访客满意度统计表';

-- 创建访问时长统计表
CREATE TABLE IF NOT EXISTS `t_visitor_duration_statistics` (
    `statistics_id`          BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '统计ID',
    `stat_date`              DATE            NOT NULL                 COMMENT '统计日期',
    `total_visitors`         INT             NOT NULL DEFAULT 0      COMMENT '总访客数',
    `average_duration`       INT             NOT NULL DEFAULT 0      COMMENT '平均访问时长(分钟)',
    `min_duration`           INT             NOT NULL DEFAULT 0      COMMENT '最短访问时长(分钟)',
    `max_duration`           INT             NOT NULL DEFAULT 0      COMMENT '最长访问时长(分钟)',
    `overtime_count`         INT             NOT NULL DEFAULT 0      COMMENT '超时访客数',
    `overtime_rate`          DECIMAL(5,2)    NOT NULL DEFAULT 0.00   COMMENT '超时率(%)',
    `create_time`            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`statistics_id`),
    UNIQUE KEY `uk_duration_stat_date` (`stat_date`),
    INDEX `idx_duration_stat_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='访问时长统计表';
