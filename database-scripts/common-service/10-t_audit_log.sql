-- ============================================================
-- IOE-DREAM Common Service - Audit模块
-- 表名: t_audit_log (审计日志表)
-- 功能: 审计日志记录
-- 创建时间: 2025-12-02
-- ============================================================

CREATE TABLE IF NOT EXISTS `t_audit_log` (
    `log_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `user_name` VARCHAR(100) COMMENT '用户名',
    `module_name` VARCHAR(100) NOT NULL COMMENT '模块名称',
    `operation_type` TINYINT NOT NULL COMMENT '操作类型：1-查询 2-新增 3-修改 4-删除 5-导出 6-导入 7-登录 8-登出',
    `operation_desc` VARCHAR(500) COMMENT '操作描述',
    `resource_type` VARCHAR(100) COMMENT '资源类型',
    `resource_id` VARCHAR(100) COMMENT '资源ID',
    `request_method` VARCHAR(20) COMMENT '请求方法',
    `request_url` VARCHAR(500) COMMENT '请求URL',
    `request_params` TEXT COMMENT '请求参数（JSON格式）',
    `response_data` TEXT COMMENT '响应数据（JSON格式）',
    `result_status` TINYINT NOT NULL COMMENT '结果状态：1-成功 2-失败 3-异常',
    `error_message` TEXT COMMENT '错误信息',
    `risk_level` TINYINT NOT NULL DEFAULT 1 COMMENT '风险等级：1-低 2-中 3-高',
    `client_ip` VARCHAR(50) COMMENT '客户端IP',
    `user_agent` VARCHAR(500) COMMENT '用户代理',
    `trace_id` VARCHAR(100) COMMENT '追踪ID',
    `execution_time` BIGINT COMMENT '执行时间（毫秒）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`log_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_module_name` (`module_name`),
    KEY `idx_operation_type` (`operation_type`),
    KEY `idx_result_status` (`result_status`),
    KEY `idx_risk_level` (`risk_level`),
    KEY `idx_trace_id` (`trace_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审计日志表';

