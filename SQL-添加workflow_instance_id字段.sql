-- ============================================================
-- 工作流审批Service层集成 - 数据库迁移脚本
-- 
-- 功能: 为各业务模块的表添加workflow_instance_id字段
-- 日期: 2025-01-30
-- ============================================================

-- ==================== 消费模块 ====================

-- 支付记录表（退款申请使用）
ALTER TABLE payment_record 
ADD COLUMN workflow_instance_id BIGINT NULL COMMENT '工作流实例ID' AFTER id;

-- 报销申请表（需确认表名，如不存在需创建）
-- ALTER TABLE reimbursement_application 
-- ADD COLUMN workflow_instance_id BIGINT NULL COMMENT '工作流实例ID' AFTER id;

-- ==================== 访客模块 ====================

-- 访客预约表（需确认表名）
-- ALTER TABLE visitor_appointment 
-- ADD COLUMN workflow_instance_id BIGINT NULL COMMENT '工作流实例ID' AFTER id;

-- ==================== 门禁模块 ====================

-- 权限申请表（需确认表名）
-- ALTER TABLE access_permission_application 
-- ADD COLUMN workflow_instance_id BIGINT NULL COMMENT '工作流实例ID' AFTER id;

-- ==================== 考勤模块 ====================

-- 异常申请表
ALTER TABLE exception_applications 
ADD COLUMN workflow_instance_id BIGINT NULL COMMENT '工作流实例ID' AFTER id;

-- ==================== 添加索引 ====================

-- 为workflow_instance_id字段添加索引，提升查询性能
CREATE INDEX idx_payment_record_workflow_instance_id ON payment_record(workflow_instance_id);
-- CREATE INDEX idx_reimbursement_workflow_instance_id ON reimbursement_application(workflow_instance_id);
-- CREATE INDEX idx_visitor_appointment_workflow_instance_id ON visitor_appointment(workflow_instance_id);
-- CREATE INDEX idx_access_permission_workflow_instance_id ON access_permission_application(workflow_instance_id);
CREATE INDEX idx_exception_applications_workflow_instance_id ON exception_applications(workflow_instance_id);

-- ==================== 注意事项 ====================
-- 
-- 1. 执行前请备份数据库
-- 2. 根据实际表名调整SQL语句
-- 3. 如果表不存在，需要先创建表结构
-- 4. 索引创建可能较慢，建议在业务低峰期执行

