-- ============================================================
-- IOE-DREAM Common Service - Scheduler模块
-- 表名: t_job_execution_log (任务执行日志表)
-- 功能: 任务执行记录
-- 创建时间: 2025-12-02
-- ============================================================

CREATE TABLE IF NOT EXISTS `t_job_execution_log` (
    `log_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `job_id` BIGINT NOT NULL COMMENT '任务ID',
    `job_name` VARCHAR(100) NOT NULL COMMENT '任务名称',
    `job_group` VARCHAR(50) NOT NULL COMMENT '任务分组',
    `execution_status` TINYINT NOT NULL COMMENT '执行状态：1-成功 2-失败 3-超时 4-取消',
    `start_time` DATETIME NOT NULL COMMENT '开始时间',
    `end_time` DATETIME COMMENT '结束时间',
    `execution_time` BIGINT COMMENT '执行时长（毫秒）',
    `retry_count` INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    `error_message` TEXT COMMENT '错误信息',
    `stack_trace` TEXT COMMENT '堆栈信息',
    `execution_result` TEXT COMMENT '执行结果（JSON格式）',
    `server_ip` VARCHAR(50) COMMENT '执行服务器IP',
    `server_hostname` VARCHAR(100) COMMENT '执行服务器主机名',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`log_id`),
    KEY `idx_job_id` (`job_id`),
    KEY `idx_execution_status` (`execution_status`),
    KEY `idx_start_time` (`start_time`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务执行日志表';

