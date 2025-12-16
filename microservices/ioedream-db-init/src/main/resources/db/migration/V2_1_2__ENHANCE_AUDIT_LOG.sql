-- =====================================================
-- IOE-DREAM Flyway 迁移脚本
-- 版本: V2.1.2
-- 描述: 对齐 t_audit_log 表结构到 AuditLogEntity（增量补字段/索引）
-- =====================================================

-- 确保表存在
CREATE TABLE IF NOT EXISTS t_audit_log (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    user_id BIGINT COMMENT '用户ID',
    username VARCHAR(100) COMMENT '用户名',
    operation VARCHAR(100) NOT NULL COMMENT '操作类型',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

ALTER TABLE t_audit_log
    ADD COLUMN IF NOT EXISTS module_name VARCHAR(100) COMMENT '模块名称' AFTER username,
    ADD COLUMN IF NOT EXISTS operation_type INT COMMENT '操作类型：1-查询 2-新增 3-修改 4-删除 5-导出 6-导入 7-登录 8-登出' AFTER module_name,
    ADD COLUMN IF NOT EXISTS operation_desc VARCHAR(255) COMMENT '操作描述' AFTER operation_type,
    ADD COLUMN IF NOT EXISTS resource_type VARCHAR(100) COMMENT '资源类型' AFTER operation_desc,
    ADD COLUMN IF NOT EXISTS resource_id VARCHAR(100) COMMENT '资源ID' AFTER resource_type,
    ADD COLUMN IF NOT EXISTS request_method VARCHAR(20) COMMENT '请求方法' AFTER resource_id,
    ADD COLUMN IF NOT EXISTS request_url VARCHAR(500) COMMENT '请求URL' AFTER request_method,
    ADD COLUMN IF NOT EXISTS request_params LONGTEXT COMMENT '请求参数(JSON)' AFTER request_url,
    ADD COLUMN IF NOT EXISTS response_data LONGTEXT COMMENT '响应数据(JSON)' AFTER request_params,
    ADD COLUMN IF NOT EXISTS result_status INT COMMENT '结果状态：1-成功 2-失败 3-异常' AFTER response_data,
    ADD COLUMN IF NOT EXISTS error_message LONGTEXT COMMENT '错误信息' AFTER result_status,
    ADD COLUMN IF NOT EXISTS risk_level INT COMMENT '风险等级：1-低 2-中 3-高' AFTER error_message,
    ADD COLUMN IF NOT EXISTS client_ip VARCHAR(50) COMMENT '客户端IP' AFTER risk_level,
    ADD COLUMN IF NOT EXISTS user_agent VARCHAR(500) COMMENT '用户代理' AFTER client_ip,
    ADD COLUMN IF NOT EXISTS trace_id VARCHAR(64) COMMENT '追踪ID' AFTER user_agent,
    ADD COLUMN IF NOT EXISTS execution_time BIGINT COMMENT '执行时间(毫秒)' AFTER trace_id,
    ADD COLUMN IF NOT EXISTS update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER create_time,
    ADD COLUMN IF NOT EXISTS create_user_id BIGINT COMMENT '创建人ID' AFTER update_time,
    ADD COLUMN IF NOT EXISTS update_user_id BIGINT COMMENT '更新人ID' AFTER create_user_id,
    ADD COLUMN IF NOT EXISTS deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除' AFTER update_user_id,
    ADD COLUMN IF NOT EXISTS version INT DEFAULT 0 COMMENT '乐观锁版本号' AFTER deleted_flag;

CREATE INDEX IF NOT EXISTS idx_audit_user_id ON t_audit_log (user_id);
CREATE INDEX IF NOT EXISTS idx_audit_username ON t_audit_log (username);
CREATE INDEX IF NOT EXISTS idx_audit_module_name ON t_audit_log (module_name);
CREATE INDEX IF NOT EXISTS idx_audit_operation_type ON t_audit_log (operation_type);
CREATE INDEX IF NOT EXISTS idx_audit_result_status ON t_audit_log (result_status);
CREATE INDEX IF NOT EXISTS idx_audit_client_ip ON t_audit_log (client_ip);
CREATE INDEX IF NOT EXISTS idx_audit_trace_id ON t_audit_log (trace_id);
CREATE INDEX IF NOT EXISTS idx_audit_create_time ON t_audit_log (create_time);
