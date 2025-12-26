-- ============================================================================
-- IOE-DREAM 消费服务对账表
-- Task 8: 消费记录对账功能
-- Version: V20251226__001
-- Author: IOE-DREAM Team
-- Date: 2025-12-26
-- ============================================================================

-- 创建对账记录表
CREATE TABLE IF NOT EXISTS `t_consume_reconciliation_record` (
    `reconciliation_id`       BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '对账ID',
    `start_date`              DATE            NOT NULL                 COMMENT '开始日期',
    `end_date`                DATE            NOT NULL                 COMMENT '结束日期',
    `reconciliation_status`   TINYINT         NOT NULL DEFAULT 0      COMMENT '对账状态: 0-对账中 1-部分对账 2-对账完成 3-对账异常',
    `system_transaction_count` INT             NOT NULL DEFAULT 0      COMMENT '系统端交易总数',
    `system_total_amount`     DECIMAL(12,2)   NOT NULL DEFAULT 0.00   COMMENT '系统端交易总额',
    `device_transaction_count` INT             NOT NULL DEFAULT 0      COMMENT '设备端交易总数',
    `device_total_amount`     DECIMAL(12,2)   NOT NULL DEFAULT 0.00   COMMENT '设备端交易总额',
    `count_difference`        INT             NOT NULL DEFAULT 0      COMMENT '数量差异',
    `amount_difference`       DECIMAL(12,2)   NOT NULL DEFAULT 0.00   COMMENT '金额差异',
    `has_discrepancy`         TINYINT         NOT NULL DEFAULT 0      COMMENT '是否有差异: 0-无差异 1-有差异',
    `discrepancy_details`     TEXT                                         COMMENT '差异详情(JSON格式)',
    `reconciled_by`           BIGINT                                        COMMENT '对账操作人ID',
    `reconciled_time`         DATETIME                                      COMMENT '对账完成时间',
    `remark`                  VARCHAR(500)                                  COMMENT '备注',
    `create_time`             DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`             DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag`            TINYINT         NOT NULL DEFAULT 0      COMMENT '删除标记: 0-未删除 1-已删除',
    PRIMARY KEY (`reconciliation_id`),
    INDEX `idx_reconciliation_date` (`start_date`, `end_date`),
    INDEX `idx_reconciliation_status` (`reconciliation_status`),
    INDEX `idx_reconciliation_time` (`reconciled_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消费记录对账表';

-- 创建对账明细表（记录每次对账的详细交易信息）
CREATE TABLE IF NOT EXISTS `t_consume_reconciliation_detail` (
    `detail_id`               BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '明细ID',
    `reconciliation_id`       BIGINT          NOT NULL                 COMMENT '对账ID',
    `transaction_date`        DATE            NOT NULL                 COMMENT '交易日期',
    `system_count`            INT             NOT NULL DEFAULT 0      COMMENT '系统端交易数量',
    `system_amount`           DECIMAL(12,2)   NOT NULL DEFAULT 0.00   COMMENT '系统端交易金额',
    `device_count`            INT             NOT NULL DEFAULT 0      COMMENT '设备端交易数量',
    `device_amount`           DECIMAL(12,2)   NOT NULL DEFAULT 0.00   COMMENT '设备端交易金额',
    `count_diff`              INT             NOT NULL DEFAULT 0      COMMENT '数量差异',
    `amount_diff`             DECIMAL(12,2)   NOT NULL DEFAULT 0.00   COMMENT '金额差异',
    `create_time`             DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`detail_id`),
    UNIQUE KEY `uk_reconciliation_date` (`reconciliation_id`, `transaction_date`),
    INDEX `idx_detail_date` (`transaction_date`),
    FOREIGN KEY (`reconciliation_id`) REFERENCES `t_consume_reconciliation_record`(`reconciliation_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='对账明细表';
