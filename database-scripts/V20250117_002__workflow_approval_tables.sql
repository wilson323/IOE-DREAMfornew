-- ============================================================
-- Flowable 7.2.0 升级 - 审批流程增强数据表
-- 实现钉钉审批流特性：会签/或签、撤回、催办、抄送
-- ============================================================

-- 审批记录表
CREATE TABLE IF NOT EXISTS t_workflow_approval_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    process_instance_id VARCHAR(64) NOT NULL COMMENT '流程实例ID',
    task_id VARCHAR(64) COMMENT '任务ID',
    approver_id BIGINT NOT NULL COMMENT '审批人ID',
    outcome TINYINT NOT NULL COMMENT '审批结果: 1-同意, 2-拒绝, 3-转办, 4-撤回',
    comment VARCHAR(2000) COMMENT '审批意见',
    approval_time DATETIME NOT NULL COMMENT '审批时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_process_instance_id (process_instance_id),
    INDEX idx_approver_id (approver_id),
    INDEX idx_approval_time (approval_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审批记录表';

-- 抄送记录表
CREATE TABLE IF NOT EXISTS t_workflow_carbon_copy (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    process_instance_id VARCHAR(64) NOT NULL COMMENT '流程实例ID',
    task_id VARCHAR(64) COMMENT '任务ID',
    cc_user_id BIGINT NOT NULL COMMENT '抄送人ID',
    sender_id BIGINT NOT NULL COMMENT '发送人ID',
    read_status TINYINT DEFAULT 0 COMMENT '阅读状态: 0-未读, 1-已读',
    cc_time DATETIME NOT NULL COMMENT '抄送时间',
    read_time DATETIME COMMENT '阅读时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_cc_user_id (cc_user_id),
    INDEX idx_process_instance_id (process_instance_id),
    INDEX idx_read_status (read_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='抄送记录表';

-- 催办记录表
CREATE TABLE IF NOT EXISTS t_workflow_urge_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    process_instance_id VARCHAR(64) NOT NULL COMMENT '流程实例ID',
    task_id VARCHAR(64) NOT NULL COMMENT '任务ID',
    urger_id BIGINT NOT NULL COMMENT '催办人ID',
    assignee_id BIGINT NOT NULL COMMENT '被催办人ID',
    message VARCHAR(500) COMMENT '催办消息',
    urge_time DATETIME NOT NULL COMMENT '催办时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_task_id (task_id),
    INDEX idx_assignee_id (assignee_id),
    INDEX idx_urge_time (urge_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='催办记录表';
