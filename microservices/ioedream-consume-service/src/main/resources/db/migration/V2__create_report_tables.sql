-- ============================================================
-- 统一报表中心数据库表设计
-- 创建时间: 2025-12-26
-- 说明: 支持所有业务模块的报表定义、生成、导出、调度管理
-- ============================================================

-- 1. 报表定义表
CREATE TABLE t_report_definition (
    report_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '报表ID',
    report_name VARCHAR(200) NOT NULL COMMENT '报表名称',
    report_code VARCHAR(100) NOT NULL COMMENT '报表编码',
    report_type TINYINT NOT NULL COMMENT '报表类型（1-列表 2-汇总 3-图表 4-交叉表）',
    business_module VARCHAR(50) COMMENT '业务模块（access/attendance/consume等）',
    category_id BIGINT COMMENT '分类ID',
    data_source_type TINYINT NOT NULL COMMENT '数据源类型（1-SQL 2-API 3-静态）',
    data_source_config TEXT COMMENT '数据源配置（JSON）',
    template_type TINYINT COMMENT '模板类型（1-Excel 2-PDF 3-Word）',
    template_config TEXT COMMENT '模板配置（JSON）',
    export_formats VARCHAR(100) COMMENT '导出格式（excel,pdf,word,csv）',
    description TEXT COMMENT '报表描述',
    status TINYINT DEFAULT 1 COMMENT '状态（1-启用 0-禁用）',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    UNIQUE KEY uk_report_code (report_code),
    KEY idx_business_module (business_module),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报表定义表';

-- 2. 报表分类表
CREATE TABLE t_report_category (
    category_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    category_name VARCHAR(100) NOT NULL COMMENT '分类名称',
    category_code VARCHAR(50) NOT NULL COMMENT '分类编码',
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    status TINYINT DEFAULT 1 COMMENT '状态（1-启用 0-禁用）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    UNIQUE KEY uk_category_code (category_code),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报表分类表';

-- 3. 报表参数表
CREATE TABLE t_report_parameter (
    parameter_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '参数ID',
    report_id BIGINT NOT NULL COMMENT '报表ID',
    parameter_name VARCHAR(100) NOT NULL COMMENT '参数名称',
    parameter_code VARCHAR(50) NOT NULL COMMENT '参数编码',
    parameter_type VARCHAR(50) NOT NULL COMMENT '参数类型（String/Integer/Date等）',
    default_value VARCHAR(500) COMMENT '默认值',
    required TINYINT DEFAULT 0 COMMENT '是否必填（1-是 0-否）',
    validation_rule VARCHAR(500) COMMENT '验证规则（正则表达式）',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_report_id (report_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报表参数表';

-- 4. 报表模板表
CREATE TABLE t_report_template (
    template_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '模板ID',
    report_id BIGINT NOT NULL COMMENT '报表ID',
    template_name VARCHAR(200) NOT NULL COMMENT '模板名称',
    template_type TINYINT NOT NULL COMMENT '模板类型（1-Excel 2-PDF 3-Word）',
    file_path VARCHAR(500) NOT NULL COMMENT '模板文件路径',
    file_size BIGINT COMMENT '文件大小（字节）',
    version VARCHAR(50) COMMENT '版本号',
    description TEXT COMMENT '模板描述',
    status TINYINT DEFAULT 1 COMMENT '状态（1-启用 0-禁用）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    KEY idx_report_id (report_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报表模板表';

-- 5. 报表生成记录表
CREATE TABLE t_report_generation (
    generation_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '生成记录ID',
    report_id BIGINT NOT NULL COMMENT '报表ID',
    report_name VARCHAR(200) COMMENT '报表名称',
    parameters TEXT COMMENT '请求参数（JSON）',
    generate_type TINYINT COMMENT '生成方式（1-手动 2-定时 3-API）',
    file_type VARCHAR(20) COMMENT '文件类型（excel/pdf/word/csv）',
    file_path VARCHAR(500) COMMENT '文件路径',
    file_size BIGINT COMMENT '文件大小（字节）',
    status TINYINT COMMENT '状态（1-生成中 2-成功 3-失败）',
    error_message TEXT COMMENT '错误信息',
    generate_time DATETIME COMMENT '生成时间',
    create_user_id BIGINT COMMENT '创建人ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_report_id (report_id),
    KEY idx_generate_time (generate_time),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报表生成记录表';

-- 6. 报表调度任务表
CREATE TABLE t_report_schedule (
    schedule_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '调度ID',
    report_id BIGINT NOT NULL COMMENT '报表ID',
    schedule_name VARCHAR(200) NOT NULL COMMENT '调度名称',
    cron_expression VARCHAR(100) NOT NULL COMMENT 'Cron表达式',
    parameters TEXT COMMENT '调度参数（JSON）',
    notification_config TEXT COMMENT '通知配置（邮件、消息等）',
    status TINYINT DEFAULT 1 COMMENT '状态（1-启用 0-禁用）',
    last_execute_time DATETIME COMMENT '最后执行时间',
    next_execute_time DATETIME COMMENT '下次执行时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    KEY idx_report_id (report_id),
    KEY idx_status (status),
    KEY idx_next_execute_time (next_execute_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报表调度任务表';

-- 初始化报表分类数据
INSERT INTO t_report_category (category_name, category_code, parent_id, sort_order, status) VALUES
('门禁报表', 'ACCESS', 0, 1, 1),
('考勤报表', 'ATTENDANCE', 0, 2, 1),
('消费报表', 'CONSUME', 0, 3, 1),
('访客报表', 'VISITOR', 0, 4, 1),
('视频报表', 'VIDEO', 0, 5, 1),
('综合报表', 'COMPREHENSIVE', 0, 6, 1);

-- 初始化示例报表定义
INSERT INTO t_report_definition (report_name, report_code, report_type, business_module, category_id,
    data_source_type, data_source_config, template_type, export_formats, description, status) VALUES
('每日考勤汇总表', 'DAILY_ATTENDANCE_SUMMARY', 2, 'attendance', 2,
    1, '{"sql":"SELECT * FROM t_attendance_record WHERE record_date = #{date}"}',
    1, 'excel,pdf', '统计每日考勤打卡情况', 1),
('月度消费统计表', 'MONTHLY_CONSUME_STATS', 2, 'consume', 3,
    1, '{"sql":"SELECT * FROM t_consume_record WHERE MONTH(consume_time) = #{month}"}',
    1, 'excel,pdf', '统计月度消费数据', 1),
('门禁通行记录表', 'ACCESS_RECORD_LIST', 1, 'access', 1,
    1, '{"sql":"SELECT * FROM t_access_record WHERE access_time BETWEEN #{startTime} AND #{endTime}"}',
    1, 'excel,csv', '查询门禁通行记录', 1);

-- 创建优化索引
CREATE INDEX idx_report_module_type ON t_report_definition (business_module, report_type);
CREATE INDEX idx_report_status_sort ON t_report_definition (status, sort_order);
CREATE INDEX idx_generation_report_time ON t_report_generation (report_id, generate_time);
CREATE INDEX idx_schedule_next_time ON t_report_schedule (status, next_execute_time);
