-- ============================================================
-- Flowable 7.2.0 升级 - 表单引擎数据表
-- 替代Flowable已移除的Form引擎，使用JSON Schema存储表单定义
-- ============================================================

-- 工作流表单定义表
CREATE TABLE IF NOT EXISTS t_workflow_form_definition (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    form_key VARCHAR(100) NOT NULL COMMENT '表单标识',
    form_name VARCHAR(200) NOT NULL COMMENT '表单名称',
    form_schema JSON NOT NULL COMMENT 'JSON Schema定义',
    version INT DEFAULT 1 COMMENT '版本号',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-启用, 0-禁用',
    category VARCHAR(50) COMMENT '表单分类',
    description VARCHAR(500) COMMENT '表单描述',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记: 0-未删除, 1-已删除',
    UNIQUE KEY uk_form_key_version (form_key, version),
    INDEX idx_form_category (category),
    INDEX idx_form_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工作流表单定义表';

-- 工作流表单实例表
CREATE TABLE IF NOT EXISTS t_workflow_form_instance (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    form_schema_id BIGINT NOT NULL COMMENT '表单Schema定义ID',
    form_key VARCHAR(100) NOT NULL COMMENT '表单标识',
    process_instance_id VARCHAR(64) COMMENT '流程实例ID',
    task_id VARCHAR(64) COMMENT '任务ID',
    form_data JSON COMMENT '表单数据(JSON格式)',
    submitter_id BIGINT COMMENT '提交者ID',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-进行中, 2-已提交, 3-已作废',
    submit_time DATETIME COMMENT '提交时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记: 0-未删除, 1-已删除',
    INDEX idx_form_schema_id (form_schema_id),
    INDEX idx_process_instance_id (process_instance_id),
    INDEX idx_task_id (task_id),
    INDEX idx_submitter_id (submitter_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工作流表单实例表';

-- 流程模板表(低代码配置存储)
CREATE TABLE IF NOT EXISTS t_workflow_process_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    template_key VARCHAR(100) NOT NULL COMMENT '模板标识',
    template_name VARCHAR(200) NOT NULL COMMENT '模板名称',
    bpmn_xml LONGTEXT NOT NULL COMMENT 'BPMN XML定义',
    form_key VARCHAR(100) COMMENT '关联表单',
    category VARCHAR(50) COMMENT '分类',
    icon VARCHAR(100) COMMENT '图标',
    description VARCHAR(500) COMMENT '描述',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-启用, 0-禁用',
    version INT DEFAULT 1 COMMENT '版本号',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    UNIQUE KEY uk_template_key_version (template_key, version),
    INDEX idx_template_category (category),
    INDEX idx_template_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工作流流程模板表';
