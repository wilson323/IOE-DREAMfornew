-- =====================================================
-- IOE-DREAM 数据库迁移脚本
-- 版本: V2.1.5
-- 描述: 批量补齐缺口表（第4批：工作流/审批/考勤/门禁申请）
-- =====================================================

USE ioedream;

-- =====================================================
-- 1. t_common_workflow_definition（工作流定义表）
-- =====================================================
CREATE TABLE IF NOT EXISTS t_common_workflow_definition (
    definition_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '定义ID',
    workflow_code VARCHAR(50) NOT NULL COMMENT '工作流编码',
    workflow_name VARCHAR(100) NOT NULL COMMENT '工作流名称',
    workflow_type VARCHAR(50) COMMENT '工作流类型',
    definition_json TEXT COMMENT '定义JSON',
    version INT DEFAULT 1 COMMENT '版本号',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用 2-禁用',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    UNIQUE KEY uk_workflow_code_version (workflow_code, version, deleted_flag),
    KEY idx_workflow_type (workflow_type),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工作流定义表';

-- =====================================================
-- 2. t_common_workflow_instance（工作流实例表）
-- =====================================================
CREATE TABLE IF NOT EXISTS t_common_workflow_instance (
    instance_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '实例ID',
    definition_id BIGINT NOT NULL COMMENT '定义ID',
    business_key VARCHAR(100) COMMENT '业务键',
    business_type VARCHAR(50) COMMENT '业务类型',
    initiator_id BIGINT NOT NULL COMMENT '发起人ID',
    current_node VARCHAR(100) COMMENT '当前节点',
    instance_status TINYINT DEFAULT 1 COMMENT '实例状态：1-进行中 2-已完成 3-已取消 4-已拒绝',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    variables_json TEXT COMMENT '变量JSON',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    KEY idx_definition_id (definition_id),
    KEY idx_business_key (business_key),
    KEY idx_initiator_id (initiator_id),
    KEY idx_instance_status (instance_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工作流实例表';

-- =====================================================
-- 3. t_common_workflow_task（工作流任务表）
-- =====================================================
CREATE TABLE IF NOT EXISTS t_common_workflow_task (
    task_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '任务ID',
    instance_id BIGINT NOT NULL COMMENT '实例ID',
    task_name VARCHAR(100) COMMENT '任务名称',
    task_node VARCHAR(100) COMMENT '任务节点',
    assignee_id BIGINT COMMENT '处理人ID',
    task_status TINYINT DEFAULT 1 COMMENT '任务状态：1-待处理 2-已处理 3-已转办 4-已委托',
    action VARCHAR(50) COMMENT '处理动作',
    comment VARCHAR(500) COMMENT '处理意见',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    due_time DATETIME COMMENT '截止时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    KEY idx_instance_id (instance_id),
    KEY idx_assignee_id (assignee_id),
    KEY idx_task_status (task_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工作流任务表';

-- =====================================================
-- 4. t_common_approval_config（审批配置表）
-- =====================================================
CREATE TABLE IF NOT EXISTS t_common_approval_config (
    config_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '配置ID',
    approval_type VARCHAR(50) NOT NULL COMMENT '审批类型',
    config_name VARCHAR(100) NOT NULL COMMENT '配置名称',
    config_json TEXT COMMENT '配置JSON',
    status TINYINT DEFAULT 1 COMMENT '状态',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    KEY idx_approval_type (approval_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审批配置表';

-- =====================================================
-- 5. t_common_approval_node_config（审批节点配置表）
-- =====================================================
CREATE TABLE IF NOT EXISTS t_common_approval_node_config (
    node_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '节点ID',
    config_id BIGINT NOT NULL COMMENT '配置ID',
    node_name VARCHAR(100) NOT NULL COMMENT '节点名称',
    node_type VARCHAR(50) COMMENT '节点类型',
    approver_type VARCHAR(50) COMMENT '审批人类型',
    approver_ids VARCHAR(500) COMMENT '审批人ID列表',
    node_order INT DEFAULT 0 COMMENT '节点顺序',
    config_json TEXT COMMENT '节点配置JSON',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    KEY idx_config_id (config_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审批节点配置表';

-- =====================================================
-- 6. t_common_approval_template（审批模板表）
-- =====================================================
CREATE TABLE IF NOT EXISTS t_common_approval_template (
    template_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '模板ID',
    template_code VARCHAR(50) NOT NULL COMMENT '模板编码',
    template_name VARCHAR(100) NOT NULL COMMENT '模板名称',
    template_type VARCHAR(50) COMMENT '模板类型',
    form_config TEXT COMMENT '表单配置JSON',
    flow_config TEXT COMMENT '流程配置JSON',
    status TINYINT DEFAULT 1 COMMENT '状态',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    UNIQUE KEY uk_template_code (template_code, deleted_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审批模板表';

-- =====================================================
-- 7. t_common_approval_statistics（审批统计表）
-- =====================================================
CREATE TABLE IF NOT EXISTS t_common_approval_statistics (
    stat_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '统计ID',
    stat_date DATE NOT NULL COMMENT '统计日期',
    approval_type VARCHAR(50) COMMENT '审批类型',
    total_count INT DEFAULT 0 COMMENT '总数',
    pending_count INT DEFAULT 0 COMMENT '待处理数',
    approved_count INT DEFAULT 0 COMMENT '已通过数',
    rejected_count INT DEFAULT 0 COMMENT '已拒绝数',
    avg_process_time BIGINT COMMENT '平均处理时间(秒)',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_stat_date (stat_date),
    KEY idx_approval_type (approval_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审批统计表';

-- =====================================================
-- 8. access_approval_process（门禁审批流程表，无t_前缀）
-- =====================================================
CREATE TABLE IF NOT EXISTS access_approval_process (
    process_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '流程ID',
    apply_id BIGINT NOT NULL COMMENT '申请ID',
    approver_id BIGINT NOT NULL COMMENT '审批人ID',
    approval_order INT DEFAULT 0 COMMENT '审批顺序',
    approval_status TINYINT DEFAULT 0 COMMENT '审批状态',
    approval_time DATETIME COMMENT '审批时间',
    approval_remark VARCHAR(500) COMMENT '审批备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_apply_id (apply_id),
    KEY idx_approver_id (approver_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门禁审批流程表';

-- =====================================================
-- 9. access_permission_apply（门禁权限申请表，无t_前缀）
-- =====================================================
CREATE TABLE IF NOT EXISTS access_permission_apply (
    apply_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '申请ID',
    applicant_id BIGINT NOT NULL COMMENT '申请人ID',
    apply_type VARCHAR(50) COMMENT '申请类型',
    area_ids VARCHAR(500) COMMENT '区域ID列表',
    valid_start DATETIME COMMENT '有效开始时间',
    valid_end DATETIME COMMENT '有效结束时间',
    apply_reason VARCHAR(500) COMMENT '申请原因',
    apply_status TINYINT DEFAULT 0 COMMENT '申请状态：0-待审批 1-已通过 2-已拒绝',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    KEY idx_applicant_id (applicant_id),
    KEY idx_apply_status (apply_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门禁权限申请表';

-- =====================================================
-- 10. visitor_appointment（访客预约表，无t_前缀）
-- =====================================================
CREATE TABLE IF NOT EXISTS visitor_appointment (
    appointment_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '预约ID',
    visitor_name VARCHAR(100) NOT NULL COMMENT '访客姓名',
    visitor_phone VARCHAR(20) COMMENT '访客电话',
    visitor_company VARCHAR(200) COMMENT '访客公司',
    host_id BIGINT COMMENT '被访人ID',
    host_name VARCHAR(100) COMMENT '被访人姓名',
    visit_purpose VARCHAR(500) COMMENT '来访目的',
    visit_start DATETIME COMMENT '预计到访时间',
    visit_end DATETIME COMMENT '预计离开时间',
    area_ids VARCHAR(500) COMMENT '访问区域',
    appointment_status TINYINT DEFAULT 0 COMMENT '预约状态：0-待审批 1-已通过 2-已拒绝 3-已取消',
    check_in_time DATETIME COMMENT '签到时间',
    check_out_time DATETIME COMMENT '签退时间',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    KEY idx_host_id (host_id),
    KEY idx_appointment_status (appointment_status),
    KEY idx_visit_start (visit_start)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='访客预约表';

-- =====================================================
-- 11. attendance_leave（考勤请假表，无t_前缀）
-- =====================================================
CREATE TABLE IF NOT EXISTS attendance_leave (
    leave_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '请假ID',
    employee_id BIGINT NOT NULL COMMENT '员工ID',
    leave_type VARCHAR(50) NOT NULL COMMENT '请假类型',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    duration DECIMAL(10,2) COMMENT '时长(小时)',
    reason VARCHAR(500) COMMENT '请假原因',
    approval_status TINYINT DEFAULT 0 COMMENT '审批状态',
    approver_id BIGINT COMMENT '审批人ID',
    approval_time DATETIME COMMENT '审批时间',
    approval_remark VARCHAR(500) COMMENT '审批备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    KEY idx_employee_id (employee_id),
    KEY idx_leave_type (leave_type),
    KEY idx_approval_status (approval_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤请假表';

-- =====================================================
-- 12. attendance_overtime（考勤加班表，无t_前缀）
-- =====================================================
CREATE TABLE IF NOT EXISTS attendance_overtime (
    overtime_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '加班ID',
    employee_id BIGINT NOT NULL COMMENT '员工ID',
    overtime_type VARCHAR(50) COMMENT '加班类型',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    duration DECIMAL(10,2) COMMENT '时长(小时)',
    reason VARCHAR(500) COMMENT '加班原因',
    approval_status TINYINT DEFAULT 0 COMMENT '审批状态',
    approver_id BIGINT COMMENT '审批人ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    KEY idx_employee_id (employee_id),
    KEY idx_approval_status (approval_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤加班表';

-- =====================================================
-- 13. attendance_shift（考勤班次表，无t_前缀）
-- =====================================================
CREATE TABLE IF NOT EXISTS attendance_shift (
    shift_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '班次ID',
    shift_name VARCHAR(100) NOT NULL COMMENT '班次名称',
    shift_code VARCHAR(50) COMMENT '班次编码',
    work_start TIME COMMENT '上班时间',
    work_end TIME COMMENT '下班时间',
    break_start TIME COMMENT '休息开始时间',
    break_end TIME COMMENT '休息结束时间',
    work_hours DECIMAL(4,2) COMMENT '工作时长',
    is_flexible TINYINT DEFAULT 0 COMMENT '是否弹性：0-否 1-是',
    status TINYINT DEFAULT 1 COMMENT '状态',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    UNIQUE KEY uk_shift_code (shift_code, deleted_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤班次表';

-- =====================================================
-- 14. attendance_supplement（考勤补卡表，无t_前缀）
-- =====================================================
CREATE TABLE IF NOT EXISTS attendance_supplement (
    supplement_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '补卡ID',
    employee_id BIGINT NOT NULL COMMENT '员工ID',
    supplement_date DATE NOT NULL COMMENT '补卡日期',
    supplement_type VARCHAR(50) COMMENT '补卡类型',
    supplement_time DATETIME COMMENT '补卡时间',
    reason VARCHAR(500) COMMENT '补卡原因',
    approval_status TINYINT DEFAULT 0 COMMENT '审批状态',
    approver_id BIGINT COMMENT '审批人ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    KEY idx_employee_id (employee_id),
    KEY idx_supplement_date (supplement_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤补卡表';

-- =====================================================
-- 15. attendance_travel（考勤出差表，无t_前缀）
-- =====================================================
CREATE TABLE IF NOT EXISTS attendance_travel (
    travel_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '出差ID',
    employee_id BIGINT NOT NULL COMMENT '员工ID',
    destination VARCHAR(200) COMMENT '目的地',
    start_date DATE NOT NULL COMMENT '开始日期',
    end_date DATE NOT NULL COMMENT '结束日期',
    days INT COMMENT '出差天数',
    reason VARCHAR(500) COMMENT '出差原因',
    approval_status TINYINT DEFAULT 0 COMMENT '审批状态',
    approver_id BIGINT COMMENT '审批人ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    KEY idx_employee_id (employee_id),
    KEY idx_start_date (start_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤出差表';

-- =====================================================
-- 16. consume_product（消费商品表，无t_前缀）
-- =====================================================
CREATE TABLE IF NOT EXISTS consume_product (
    product_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '商品ID',
    product_code VARCHAR(50) NOT NULL COMMENT '商品编码',
    product_name VARCHAR(200) NOT NULL COMMENT '商品名称',
    category_id BIGINT COMMENT '分类ID',
    price DECIMAL(10,2) NOT NULL COMMENT '价格',
    unit VARCHAR(20) COMMENT '单位',
    stock INT DEFAULT 0 COMMENT '库存',
    status TINYINT DEFAULT 1 COMMENT '状态',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    UNIQUE KEY uk_product_code (product_code, deleted_flag),
    KEY idx_category_id (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消费商品表';

-- =====================================================
-- 17. consume_subsidy_issue_record（消费补贴发放记录表，无t_前缀）
-- =====================================================
CREATE TABLE IF NOT EXISTS consume_subsidy_issue_record (
    record_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    subsidy_rule_id BIGINT COMMENT '补贴规则ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    amount DECIMAL(10,2) NOT NULL COMMENT '发放金额',
    issue_date DATE NOT NULL COMMENT '发放日期',
    issue_type VARCHAR(50) COMMENT '发放类型',
    status TINYINT DEFAULT 1 COMMENT '状态',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_user_id (user_id),
    KEY idx_issue_date (issue_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消费补贴发放记录表';

-- =====================================================
-- 18. consume_transaction（消费交易表，无t_前缀）
-- =====================================================
CREATE TABLE IF NOT EXISTS consume_transaction (
    transaction_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '交易ID',
    transaction_no VARCHAR(50) NOT NULL COMMENT '交易号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    account_id BIGINT COMMENT '账户ID',
    transaction_type VARCHAR(50) NOT NULL COMMENT '交易类型',
    amount DECIMAL(10,2) NOT NULL COMMENT '金额',
    balance_before DECIMAL(10,2) COMMENT '交易前余额',
    balance_after DECIMAL(10,2) COMMENT '交易后余额',
    device_id BIGINT COMMENT '设备ID',
    area_id BIGINT COMMENT '区域ID',
    status TINYINT DEFAULT 1 COMMENT '状态',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_transaction_no (transaction_no),
    KEY idx_user_id (user_id),
    KEY idx_account_id (account_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消费交易表';

-- =====================================================
-- 19. payment_record（支付记录表，无t_前缀）
-- =====================================================
CREATE TABLE IF NOT EXISTS payment_record (
    record_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    order_no VARCHAR(50) NOT NULL COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    amount DECIMAL(10,2) NOT NULL COMMENT '金额',
    payment_method VARCHAR(50) COMMENT '支付方式',
    payment_status TINYINT DEFAULT 0 COMMENT '支付状态',
    payment_time DATETIME COMMENT '支付时间',
    trade_no VARCHAR(100) COMMENT '交易号',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    UNIQUE KEY uk_order_no (order_no, deleted_flag),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付记录表';

-- =====================================================
-- 20. refund_application（退款申请表，无t_前缀）
-- =====================================================
CREATE TABLE IF NOT EXISTS refund_application (
    application_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '申请ID',
    order_no VARCHAR(50) NOT NULL COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    refund_amount DECIMAL(10,2) NOT NULL COMMENT '退款金额',
    refund_reason VARCHAR(500) COMMENT '退款原因',
    refund_status TINYINT DEFAULT 0 COMMENT '退款状态',
    approver_id BIGINT COMMENT '审批人ID',
    approval_time DATETIME COMMENT '审批时间',
    refund_time DATETIME COMMENT '退款时间',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    KEY idx_order_no (order_no),
    KEY idx_user_id (user_id),
    KEY idx_refund_status (refund_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='退款申请表';

-- =====================================================
-- 21. reimbursement_application（报销申请表，无t_前缀）
-- =====================================================
CREATE TABLE IF NOT EXISTS reimbursement_application (
    application_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '申请ID',
    applicant_id BIGINT NOT NULL COMMENT '申请人ID',
    reimbursement_type VARCHAR(50) COMMENT '报销类型',
    amount DECIMAL(10,2) NOT NULL COMMENT '报销金额',
    description VARCHAR(500) COMMENT '报销说明',
    attachments TEXT COMMENT '附件列表JSON',
    approval_status TINYINT DEFAULT 0 COMMENT '审批状态',
    approver_id BIGINT COMMENT '审批人ID',
    approval_time DATETIME COMMENT '审批时间',
    payment_status TINYINT DEFAULT 0 COMMENT '付款状态',
    payment_time DATETIME COMMENT '付款时间',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    KEY idx_applicant_id (applicant_id),
    KEY idx_approval_status (approval_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报销申请表';

SELECT 'V2.1.5 批量补齐第4批表完成' AS status;
