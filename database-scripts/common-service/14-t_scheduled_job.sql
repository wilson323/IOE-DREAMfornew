-- ============================================================
-- IOE-DREAM Common Service - Scheduler模块
-- 表名: t_scheduled_job (定时任务表)
-- 功能: 定时任务配置
-- 创建时间: 2025-12-02
-- ============================================================

CREATE TABLE IF NOT EXISTS `t_scheduled_job` (
    `job_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '任务ID',
    `job_name` VARCHAR(100) NOT NULL COMMENT '任务名称',
    `job_group` VARCHAR(50) NOT NULL DEFAULT 'DEFAULT' COMMENT '任务分组',
    `job_class` VARCHAR(500) NOT NULL COMMENT '任务执行类',
    `cron_expression` VARCHAR(100) NOT NULL COMMENT 'Cron表达式',
    `job_params` TEXT COMMENT '任务参数（JSON格式）',
    `job_description` VARCHAR(500) COMMENT '任务描述',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用 2-暂停 3-停止',
    `priority` INT NOT NULL DEFAULT 0 COMMENT '优先级',
    `max_retry` INT NOT NULL DEFAULT 3 COMMENT '最大重试次数',
    `retry_interval` INT NOT NULL DEFAULT 300 COMMENT '重试间隔（秒）',
    `timeout` INT COMMENT '超时时间（秒）',
    `concurrent` TINYINT NOT NULL DEFAULT 0 COMMENT '是否允许并发：0-否 1-是',
    `misfire_policy` TINYINT NOT NULL DEFAULT 1 COMMENT '错过执行策略：1-立即执行 2-执行一次 3-放弃执行',
    `last_execution_time` DATETIME COMMENT '最后执行时间',
    `next_execution_time` DATETIME COMMENT '下次执行时间',
    `execution_count` BIGINT NOT NULL DEFAULT 0 COMMENT '执行次数',
    `failure_count` BIGINT NOT NULL DEFAULT 0 COMMENT '失败次数',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    PRIMARY KEY (`job_id`),
    UNIQUE KEY `uk_job_name_group` (`job_name`, `job_group`),
    KEY `idx_status` (`status`),
    KEY `idx_next_execution_time` (`next_execution_time`),
    KEY `idx_deleted_flag` (`deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='定时任务表';

