-- ============================================================
-- 工作流相关表
-- 模块: oa-service
-- 作者: IOE-DREAM Team
-- 创建日期: 2025-12-14
-- ============================================================

-- 工作流定义表
CREATE TABLE IF NOT EXISTS `t_workflow_definition` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '流程定义ID',
    `code` VARCHAR(64) NOT NULL COMMENT '流程编码',
    `name` VARCHAR(100) NOT NULL COMMENT '流程名称',
    `type` VARCHAR(20) NOT NULL DEFAULT 'APPROVAL' COMMENT '流程类型：APPROVAL-审批流程, NOTIFY-通知流程',
    `category` VARCHAR(50) NOT NULL COMMENT '流程分类：ACCESS-门禁, VISITOR-访客, LEAVE-请假, EXPENSE-报销',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '流程描述',
    `definition_json` TEXT NOT NULL COMMENT '流程定义JSON（节点配置）',
    `version` INT NOT NULL DEFAULT 1 COMMENT '版本号',
    `status` VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT-草稿, ACTIVE-启用, DISABLED-禁用',
    `creator_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code_version` (`code`, `version`),
    KEY `idx_category` (`category`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工作流定义表';

-- 工作流实例表
CREATE TABLE IF NOT EXISTS `t_workflow_instance` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '实例ID',
    `instance_no` VARCHAR(32) NOT NULL COMMENT '实例编号',
    `definition_id` BIGINT NOT NULL COMMENT '流程定义ID',
    `definition_code` VARCHAR(64) NOT NULL COMMENT '流程定义编码',
    `title` VARCHAR(200) NOT NULL COMMENT '流程标题',
    `business_type` VARCHAR(50) DEFAULT NULL COMMENT '业务类型',
    `business_id` BIGINT DEFAULT NULL COMMENT '业务ID',
    `initiator_id` BIGINT NOT NULL COMMENT '发起人ID',
    `initiator_name` VARCHAR(50) DEFAULT NULL COMMENT '发起人姓名',
    `current_node_id` VARCHAR(64) DEFAULT NULL COMMENT '当前节点ID',
    `current_node_name` VARCHAR(100) DEFAULT NULL COMMENT '当前节点名称',
    `status` VARCHAR(20) NOT NULL DEFAULT 'RUNNING' COMMENT '实例状态：RUNNING-运行中, APPROVED-已通过, REJECTED-已拒绝, CANCELLED-已取消, TIMEOUT-已超时',
    `start_time` DATETIME NOT NULL COMMENT '发起时间',
    `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
    `form_data_json` TEXT DEFAULT NULL COMMENT '表单数据JSON',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_instance_no` (`instance_no`),
    KEY `idx_definition_id` (`definition_id`),
    KEY `idx_initiator_id` (`initiator_id`),
    KEY `idx_status` (`status`),
    KEY `idx_business` (`business_type`, `business_id`),
    KEY `idx_start_time` (`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工作流实例表';

-- 工作流任务表
CREATE TABLE IF NOT EXISTS `t_workflow_task` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '任务ID',
    `instance_id` BIGINT NOT NULL COMMENT '流程实例ID',
    `node_id` VARCHAR(64) NOT NULL COMMENT '节点ID',
    `node_name` VARCHAR(100) DEFAULT NULL COMMENT '节点名称',
    `node_type` VARCHAR(20) NOT NULL COMMENT '节点类型：START-开始, APPROVAL-审批, COUNTERSIGN-会签, OR_SIGN-或签, END-结束',
    `assignee_id` BIGINT NOT NULL COMMENT '审批人ID',
    `assignee_name` VARCHAR(50) DEFAULT NULL COMMENT '审批人姓名',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '任务状态：PENDING-待处理, APPROVED-已通过, REJECTED-已拒绝, TRANSFERRED-已转办, TIMEOUT-已超时',
    `comment` VARCHAR(500) DEFAULT NULL COMMENT '审批意见',
    `approve_time` DATETIME DEFAULT NULL COMMENT '审批时间',
    `due_time` DATETIME DEFAULT NULL COMMENT '截止时间',
    `transfer_to_id` BIGINT DEFAULT NULL COMMENT '转办人ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_instance_id` (`instance_id`),
    KEY `idx_assignee_id` (`assignee_id`),
    KEY `idx_status` (`status`),
    KEY `idx_due_time` (`due_time`),
    CONSTRAINT `fk_workflow_task_instance` FOREIGN KEY (`instance_id`) REFERENCES `t_workflow_instance` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工作流任务表';

-- 添加复合索引优化查询
CREATE INDEX idx_task_assignee_status ON t_workflow_task(assignee_id, status);
CREATE INDEX idx_instance_status_time ON t_workflow_instance(status, start_time);
