-- ==================================================================
-- 考勤服务 - 补卡记录表 (13-t_attendance_makeup_card.sql)
-- ==================================================================
-- 表名: t_attendance_makeup_card
-- 说明: 存储员工的补卡申请和审批记录,用于处理员工忘记打卡、漏打卡等异常情况
-- 依赖: t_attendance_record(考勤记录表), t_attendance_exception(异常申请表)
-- 作者: IOE-DREAM团队
-- 创建时间: 2025-12-08
-- 版本: v1.0.0
-- ==================================================================

USE `ioedream_attendance`;

-- ==================================================================
-- 删除已存在的表(如果存在)
-- ==================================================================
DROP TABLE IF EXISTS `t_attendance_makeup_card`;

-- ==================================================================
-- 创建表: t_attendance_makeup_card
-- ==================================================================
CREATE TABLE IF NOT EXISTS `t_attendance_makeup_card` (
    -- ==============================================================
    -- 主键和唯一标识
    -- ==============================================================
    `makeup_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '补卡ID(主键)',
    `makeup_code` VARCHAR(100) NOT NULL COMMENT '补卡编码(唯一标识,如:MAKEUP_2025001)',

    -- ==============================================================
    -- 员工信息
    -- ==============================================================
    `employee_id` BIGINT NOT NULL COMMENT '员工ID',
    `employee_name` VARCHAR(100) COMMENT '员工姓名(冗余字段,便于查询)',
    `employee_no` VARCHAR(50) COMMENT '员工工号(冗余字段)',
    `department_id` BIGINT COMMENT '部门ID',
    `department_name` VARCHAR(200) COMMENT '部门名称(冗余字段)',

    -- ==============================================================
    -- 补卡信息
    -- ==============================================================
    `makeup_date` DATE NOT NULL COMMENT '补卡日期(如:2025-01-15)',
    `makeup_time` TIME NOT NULL COMMENT '补卡时间(如:09:00:00)',
    `makeup_datetime` DATETIME NOT NULL COMMENT '补卡完整时间(makeup_date + makeup_time)',
    `card_type` VARCHAR(50) NOT NULL DEFAULT 'IN' COMMENT '补卡类型(IN-上班打卡,OUT-下班打卡,OUT_GO-外出打卡,OUT_RETURN-返回打卡)',

    -- ==============================================================
    -- 原因说明
    -- ==============================================================
    `makeup_reason` TEXT NOT NULL COMMENT '补卡原因(员工填写)',
    `reason_type` VARCHAR(50) COMMENT '原因类型(FORGET-忘记打卡,DEVICE_ERROR-设备故障,NETWORK_ERROR-网络故障,BUSINESS_TRIP-出差,OTHER-其他)',
    `reason_detail` TEXT COMMENT '原因详细说明',

    -- ==============================================================
    -- 证明材料
    -- ==============================================================
    `has_proof` TINYINT NOT NULL DEFAULT 0 COMMENT '是否有证明材料(0-否,1-是)',
    `proof_type` VARCHAR(50) COMMENT '证明材料类型(PHOTO-照片,EMAIL-邮件,DOCUMENT-文档,OTHER-其他)',
    `proof_urls` JSON COMMENT '证明材料URL集合(JSON数组)',
    `proof_description` TEXT COMMENT '证明材料描述',

    -- ==============================================================
    -- 关联信息
    -- ==============================================================
    `original_record_id` BIGINT COMMENT '原始考勤记录ID(如果存在)',
    `exception_id` BIGINT COMMENT '关联的异常申请ID',
    `shift_id` BIGINT COMMENT '班次ID',
    `shift_name` VARCHAR(100) COMMENT '班次名称(冗余字段)',

    -- ==============================================================
    -- 申请信息
    -- ==============================================================
    `application_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    `application_source` VARCHAR(50) NOT NULL DEFAULT 'WEB' COMMENT '申请来源(WEB-网页,APP-移动端,API-接口,ADMIN-管理员)',
    `application_ip` VARCHAR(50) COMMENT '申请IP地址',
    `application_device` VARCHAR(200) COMMENT '申请设备信息',

    -- ==============================================================
    -- 审批信息
    -- ==============================================================
    `approval_status` VARCHAR(50) NOT NULL DEFAULT 'PENDING' COMMENT '审批状态(PENDING-待审批,APPROVED-已通过,REJECTED-已拒绝,CANCELLED-已取消)',
    `approval_level` INT NOT NULL DEFAULT 1 COMMENT '审批层级(1-一级审批,2-二级审批)',
    `current_approver_id` BIGINT COMMENT '当前审批人ID',
    `current_approver_name` VARCHAR(100) COMMENT '当前审批人姓名',

    `first_approver_id` BIGINT COMMENT '一级审批人ID',
    `first_approver_name` VARCHAR(100) COMMENT '一级审批人姓名',
    `first_approval_time` DATETIME COMMENT '一级审批时间',
    `first_approval_result` VARCHAR(50) COMMENT '一级审批结果(APPROVED-通过,REJECTED-拒绝)',
    `first_approval_comment` TEXT COMMENT '一级审批意见',

    `second_approver_id` BIGINT COMMENT '二级审批人ID',
    `second_approver_name` VARCHAR(100) COMMENT '二级审批人姓名',
    `second_approval_time` DATETIME COMMENT '二级审批时间',
    `second_approval_result` VARCHAR(50) COMMENT '二级审批结果',
    `second_approval_comment` TEXT COMMENT '二级审批意见',

    `final_approval_time` DATETIME COMMENT '最终审批时间',
    `final_approval_comment` TEXT COMMENT '最终审批意见',

    -- ==============================================================
    -- 审批流程配置
    -- ==============================================================
    `require_approval` TINYINT NOT NULL DEFAULT 1 COMMENT '是否需要审批(0-否,1-是)',
    `approval_workflow_id` BIGINT COMMENT '审批工作流ID',
    `approval_deadline` DATETIME COMMENT '审批截止时间',
    `is_overdue` TINYINT NOT NULL DEFAULT 0 COMMENT '是否超时(0-否,1-是)',

    -- ==============================================================
    -- 处理结果
    -- ==============================================================
    `process_status` VARCHAR(50) NOT NULL DEFAULT 'PENDING' COMMENT '处理状态(PENDING-待处理,PROCESSING-处理中,COMPLETED-已完成,FAILED-处理失败)',
    `process_result` VARCHAR(50) COMMENT '处理结果(SUCCESS-成功,FAILED-失败)',
    `process_time` DATETIME COMMENT '处理时间',
    `process_comment` TEXT COMMENT '处理说明',

    `attendance_updated` TINYINT NOT NULL DEFAULT 0 COMMENT '考勤记录是否已更新(0-否,1-是)',
    `updated_record_id` BIGINT COMMENT '更新后的考勤记录ID',

    -- ==============================================================
    -- 规则验证
    -- ==============================================================
    `validation_passed` TINYINT NOT NULL DEFAULT 0 COMMENT '规则验证是否通过(0-否,1-是)',
    `validation_errors` JSON COMMENT '验证错误信息(JSON数组)',
    `validation_warnings` JSON COMMENT '验证警告信息(JSON数组)',

    `makeup_allowed` TINYINT NOT NULL DEFAULT 1 COMMENT '是否允许补卡(0-否,1-是)',
    `not_allowed_reason` VARCHAR(500) COMMENT '不允许补卡的原因',

    -- ==============================================================
    -- 限制规则
    -- ==============================================================
    `is_within_time_limit` TINYINT NOT NULL DEFAULT 1 COMMENT '是否在允许的时间范围内(0-否,1-是)',
    `max_days_allowed` INT NOT NULL DEFAULT 7 COMMENT '允许补卡的最大天数',
    `days_difference` INT COMMENT '距离补卡日期的天数',

    `monthly_count` INT NOT NULL DEFAULT 0 COMMENT '本月补卡次数',
    `max_monthly_count` INT NOT NULL DEFAULT 5 COMMENT '每月最大补卡次数',
    `is_exceed_limit` TINYINT NOT NULL DEFAULT 0 COMMENT '是否超过限制(0-否,1-是)',

    -- ==============================================================
    -- 通知信息
    -- ==============================================================
    `notification_sent` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已发送通知(0-否,1-是)',
    `notification_channels` JSON COMMENT '通知渠道(JSON数组,["短信","邮件","微信","App推送"])',
    `notification_time` DATETIME COMMENT '通知发送时间',
    `notification_status` VARCHAR(50) COMMENT '通知状态(SUCCESS-成功,FAILED-失败,PENDING-待发送)',

    -- ==============================================================
    -- 统计信息
    -- ==============================================================
    `retry_count` INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    `last_retry_time` DATETIME COMMENT '最后重试时间',
    `approval_duration_seconds` INT COMMENT '审批耗时(秒)',

    -- ==============================================================
    -- 扩展字段
    -- ==============================================================
    `extended_attributes` JSON COMMENT '扩展属性(JSON对象)',
    `metadata` JSON COMMENT '元数据(JSON对象)',
    `tags` JSON COMMENT '标签(JSON数组)',

    -- ==============================================================
    -- 备注和说明
    -- ==============================================================
    `remark` TEXT COMMENT '备注',
    `admin_note` TEXT COMMENT '管理员备注',
    `internal_note` TEXT COMMENT '内部备注(不对员工显示)',

    -- ==============================================================
    -- 标准审计字段
    -- ==============================================================
    `version` INT NOT NULL DEFAULT 1 COMMENT '版本号(用于乐观锁)',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人用户ID',
    `update_user_id` BIGINT COMMENT '更新人用户ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记(0-未删除,1-已删除)',

    -- ==============================================================
    -- 主键约束
    -- ==============================================================
    PRIMARY KEY (`makeup_id`),

    -- ==============================================================
    -- 唯一索引
    -- ==============================================================
    UNIQUE KEY `uk_makeup_code` (`makeup_code`),

    -- ==============================================================
    -- 普通索引
    -- ==============================================================
    INDEX `idx_employee_id` (`employee_id`),
    INDEX `idx_employee_no` (`employee_no`),
    INDEX `idx_department_id` (`department_id`),
    INDEX `idx_makeup_date` (`makeup_date`),
    INDEX `idx_makeup_datetime` (`makeup_datetime`),
    INDEX `idx_card_type` (`card_type`),
    INDEX `idx_approval_status` (`approval_status`),
    INDEX `idx_process_status` (`process_status`),
    INDEX `idx_application_time` (`application_time`),
    INDEX `idx_current_approver_id` (`current_approver_id`),
    INDEX `idx_exception_id` (`exception_id`),

    -- ==============================================================
    -- 复合索引(优化常用查询)
    -- ==============================================================
    INDEX `idx_employee_date` (`employee_id`, `makeup_date`),
    INDEX `idx_employee_status` (`employee_id`, `approval_status`),
    INDEX `idx_date_status` (`makeup_date`, `approval_status`),
    INDEX `idx_approver_status` (`current_approver_id`, `approval_status`),
    INDEX `idx_dept_date_status` (`department_id`, `makeup_date`, `approval_status`),

    -- ==============================================================
    -- 时间索引(降序,优化最新数据查询)
    -- ==============================================================
    INDEX `idx_create_time` (`create_time` DESC),
    INDEX `idx_update_time` (`update_time` DESC),

    -- ==============================================================
    -- 逻辑删除索引
    -- ==============================================================
    INDEX `idx_deleted_flag` (`deleted_flag`)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤服务-补卡记录表';

-- ==================================================================
-- 初始化数据: 补卡记录示例(10条企业级示例)
-- ==================================================================

-- 1. 正常补卡申请-已通过
INSERT INTO `t_attendance_makeup_card` (
    `makeup_code`, `employee_id`, `employee_name`, `employee_no`,
    `department_id`, `department_name`, `makeup_date`, `makeup_time`, `makeup_datetime`,
    `card_type`, `makeup_reason`, `reason_type`,
    `application_time`, `application_source`,
    `approval_status`, `approval_level`,
    `first_approver_id`, `first_approver_name`, `first_approval_time`,
    `first_approval_result`, `first_approval_comment`,
    `final_approval_time`, `process_status`, `process_result`,
    `attendance_updated`, `validation_passed`, `makeup_allowed`,
    `monthly_count`, `max_monthly_count`, `remark`
) VALUES (
    'MAKEUP_2025001', 1001, '张三', 'EMP001',
    101, '技术部', '2025-01-15', '09:00:00', '2025-01-15 09:00:00',
    'IN', '早上起床晚了,忘记打卡', 'FORGET',
    '2025-01-15 10:30:00', 'APP',
    'APPROVED', 1,
    2001, '李经理', '2025-01-15 11:00:00',
    'APPROVED', '情况属实,同意补卡',
    '2025-01-15 11:00:00', 'COMPLETED', 'SUCCESS',
    1, 1, 1,
    1, 5, '正常补卡申请,已审批通过'
);

-- 2. 忘记下班打卡-已通过
INSERT INTO `t_attendance_makeup_card` (
    `makeup_code`, `employee_id`, `employee_name`, `employee_no`,
    `department_id`, `department_name`, `makeup_date`, `makeup_time`, `makeup_datetime`,
    `card_type`, `makeup_reason`, `reason_type`,
    `has_proof`, `proof_type`, `proof_urls`,
    `application_time`, `application_source`,
    `approval_status`, `approval_level`,
    `first_approver_id`, `first_approver_name`, `first_approval_time`,
    `first_approval_result`, `first_approval_comment`,
    `final_approval_time`, `process_status`, `process_result`,
    `attendance_updated`, `monthly_count`, `remark`
) VALUES (
    'MAKEUP_2025002', 1002, '李四', 'EMP002',
    102, '销售部', '2025-01-16', '18:30:00', '2025-01-16 18:30:00',
    'OUT', '加班忘记下班打卡', 'FORGET',
    1, 'EMAIL', '["https://example.com/proof/email001.pdf"]',
    '2025-01-17 09:00:00', 'WEB',
    'APPROVED', 1,
    2002, '王经理', '2025-01-17 10:00:00',
    'APPROVED', '邮件证明有效,同意补卡',
    '2025-01-17 10:00:00', 'COMPLETED', 'SUCCESS',
    1, 1, '有加班邮件证明,补卡通过'
);

-- 3. 设备故障补卡-已通过
INSERT INTO `t_attendance_makeup_card` (
    `makeup_code`, `employee_id`, `employee_name`, `employee_no`,
    `department_id`, `department_name`, `makeup_date`, `makeup_time`, `makeup_datetime`,
    `card_type`, `makeup_reason`, `reason_type`,
    `has_proof`, `proof_type`, `proof_urls`,
    `application_time`, `approval_status`,
    `first_approver_id`, `first_approver_name`, `first_approval_time`,
    `first_approval_result`, `first_approval_comment`,
    `final_approval_time`, `process_status`, `process_result`,
    `attendance_updated`, `monthly_count`, `remark`
) VALUES (
    'MAKEUP_2025003', 1003, '王五', 'EMP003',
    101, '技术部', '2025-01-17', '09:15:00', '2025-01-17 09:15:00',
    'IN', '门禁机故障无法打卡', 'DEVICE_ERROR',
    1, 'PHOTO', '["https://example.com/proof/device_error.jpg"]',
    '2025-01-17 10:00:00', 'APPROVED',
    2001, '李经理', '2025-01-17 10:30:00',
    'APPROVED', '设备故障情况属实,已报修,同意补卡',
    '2025-01-17 10:30:00', 'COMPLETED', 'SUCCESS',
    1, 2, '设备故障导致无法打卡,已修复'
);

-- 4. 出差忘记打卡-已通过
INSERT INTO `t_attendance_makeup_card` (
    `makeup_code`, `employee_id`, `employee_name`, `employee_no`,
    `department_id`, `department_name`, `makeup_date`, `makeup_time`, `makeup_datetime`,
    `card_type`, `makeup_reason`, `reason_type`,
    `has_proof`, `proof_type`, `proof_urls`,
    `application_time`, `approval_status`,
    `first_approver_id`, `first_approver_name`, `first_approval_time`,
    `first_approval_result`, `first_approval_comment`,
    `final_approval_time`, `process_status`, `attendance_updated`,
    `monthly_count`, `remark`
) VALUES (
    'MAKEUP_2025004', 1004, '赵六', 'EMP004',
    102, '销售部', '2025-01-18', '09:00:00', '2025-01-18 09:00:00',
    'IN', '出差客户现场忘记打卡', 'BUSINESS_TRIP',
    1, 'DOCUMENT', '["https://example.com/proof/trip_order.pdf"]',
    '2025-01-19 09:00:00', 'APPROVED',
    2002, '王经理', '2025-01-19 10:00:00',
    'APPROVED', '出差单证明有效,同意补卡',
    '2025-01-19 10:00:00', 'COMPLETED', 1,
    1, '出差期间补卡,有出差单证明'
);

-- 5. 网络故障补卡-待审批
INSERT INTO `t_attendance_makeup_card` (
    `makeup_code`, `employee_id`, `employee_name`, `employee_no`,
    `department_id`, `department_name`, `makeup_date`, `makeup_time`, `makeup_datetime`,
    `card_type`, `makeup_reason`, `reason_type`,
    `application_time`, `application_source`,
    `approval_status`, `current_approver_id`, `current_approver_name`,
    `process_status`, `monthly_count`, `remark`
) VALUES (
    'MAKEUP_2025005', 1005, '孙七', 'EMP005',
    103, '客服部', '2025-01-19', '09:30:00', '2025-01-19 09:30:00',
    'IN', '手机APP网络故障无法打卡', 'NETWORK_ERROR',
    '2025-01-19 10:00:00', 'APP',
    'PENDING', 2003, '陈经理',
    'PENDING', 1, '待审批-网络故障补卡申请'
);

-- 6. 超过时间限制-已拒绝
INSERT INTO `t_attendance_makeup_card` (
    `makeup_code`, `employee_id`, `employee_name`, `employee_no`,
    `department_id`, `department_name`, `makeup_date`, `makeup_time`, `makeup_datetime`,
    `card_type`, `makeup_reason`, `reason_type`,
    `application_time`, `approval_status`,
    `first_approver_id`, `first_approver_name`, `first_approval_time`,
    `first_approval_result`, `first_approval_comment`,
    `final_approval_time`, `process_status`,
    `is_within_time_limit`, `days_difference`, `max_days_allowed`,
    `monthly_count`, `remark`
) VALUES (
    'MAKEUP_2025006', 1006, '周八', 'EMP006',
    101, '技术部', '2025-01-05', '09:00:00', '2025-01-05 09:00:00',
    'IN', '忘记打卡', 'FORGET',
    '2025-01-20 09:00:00', 'REJECTED',
    2001, '李经理', '2025-01-20 10:00:00',
    'REJECTED', '超过补卡时间限制(7天),申请被拒绝',
    '2025-01-20 10:00:00', 'COMPLETED',
    0, 15, 7,
    2, '补卡申请超时,已拒绝'
);

-- 7. 超过月度限制-已拒绝
INSERT INTO `t_attendance_makeup_card` (
    `makeup_code`, `employee_id`, `employee_name`, `employee_no`,
    `department_id`, `department_name`, `makeup_date`, `makeup_time`, `makeup_datetime`,
    `card_type`, `makeup_reason`, `reason_type`,
    `application_time`, `approval_status`,
    `first_approver_id`, `first_approver_name`, `first_approval_time`,
    `first_approval_result`, `first_approval_comment`,
    `final_approval_time`, `process_status`,
    `monthly_count`, `max_monthly_count`, `is_exceed_limit`,
    `remark`
) VALUES (
    'MAKEUP_2025007', 1007, '吴九', 'EMP007',
    102, '销售部', '2025-01-20', '09:00:00', '2025-01-20 09:00:00',
    'IN', '忘记打卡', 'FORGET',
    '2025-01-20 10:00:00', 'REJECTED',
    2002, '王经理', '2025-01-20 11:00:00',
    'REJECTED', '本月补卡次数已达上限(5次),申请被拒绝',
    '2025-01-20 11:00:00', 'COMPLETED',
    6, 5, 1,
    '补卡次数超限,已拒绝'
);

-- 8. 二级审批-一级通过,待二级审批
INSERT INTO `t_attendance_makeup_card` (
    `makeup_code`, `employee_id`, `employee_name`, `employee_no`,
    `department_id`, `department_name`, `makeup_date`, `makeup_time`, `makeup_datetime`,
    `card_type`, `makeup_reason`, `reason_type`,
    `application_time`, `approval_status`, `approval_level`,
    `first_approver_id`, `first_approver_name`, `first_approval_time`,
    `first_approval_result`, `first_approval_comment`,
    `current_approver_id`, `current_approver_name`,
    `process_status`, `monthly_count`, `remark`
) VALUES (
    'MAKEUP_2025008', 1008, '郑十', 'EMP008',
    101, '技术部', '2025-01-21', '09:00:00', '2025-01-21 09:00:00',
    'IN', '重要会议忘记打卡', 'OTHER',
    '2025-01-21 10:00:00', 'PENDING', 2,
    2001, '李经理', '2025-01-21 10:30:00',
    'APPROVED', '一级审批通过,提交二级审批',
    3001, '总监',
    'PENDING', 1, '待二级审批-重要会议补卡'
);

-- 9. 有证明材料-已通过
INSERT INTO `t_attendance_makeup_card` (
    `makeup_code`, `employee_id`, `employee_name`, `employee_no`,
    `department_id`, `department_name`, `makeup_date`, `makeup_time`, `makeup_datetime`,
    `card_type`, `makeup_reason`, `reason_type`,
    `has_proof`, `proof_type`, `proof_urls`, `proof_description`,
    `application_time`, `approval_status`,
    `first_approver_id`, `first_approver_name`, `first_approval_time`,
    `first_approval_result`, `first_approval_comment`,
    `final_approval_time`, `process_status`, `attendance_updated`,
    `monthly_count`, `remark`
) VALUES (
    'MAKEUP_2025009', 1009, '钱十一', 'EMP009',
    103, '客服部', '2025-01-22', '18:00:00', '2025-01-22 18:00:00',
    'OUT', '加班到很晚忘记下班打卡', 'FORGET',
    1, 'PHOTO', '["https://example.com/proof/overtime.jpg","https://example.com/proof/work_record.jpg"]',
    '加班工作记录截图和加班审批单',
    '2025-01-23 09:00:00', 'APPROVED',
    2003, '陈经理', '2025-01-23 09:30:00',
    'APPROVED', '加班证明材料齐全,同意补卡',
    '2025-01-23 09:30:00', 'COMPLETED', 1,
    2, '加班补卡,有完整证明材料'
);

-- 10. 外出打卡补卡-已通过
INSERT INTO `t_attendance_makeup_card` (
    `makeup_code`, `employee_id`, `employee_name`, `employee_no`,
    `department_id`, `department_name`, `makeup_date`, `makeup_time`, `makeup_datetime`,
    `card_type`, `makeup_reason`, `reason_type`,
    `has_proof`, `proof_type`, `proof_urls`,
    `application_time`, `approval_status`,
    `first_approver_id`, `first_approver_name`, `first_approval_time`,
    `first_approval_result`, `first_approval_comment`,
    `final_approval_time`, `process_status`, `attendance_updated`,
    `monthly_count`, `remark`
) VALUES (
    'MAKEUP_2025010', 1010, '孙十二', 'EMP010',
    102, '销售部', '2025-01-23', '14:00:00', '2025-01-23 14:00:00',
    'OUT_GO', '外出拜访客户忘记打卡', 'FORGET',
    1, 'DOCUMENT', '["https://example.com/proof/visit_plan.pdf"]',
    '2025-01-23 16:00:00', 'APPROVED',
    2002, '王经理', '2025-01-23 16:30:00',
    'APPROVED', '拜访计划证明有效,同意补卡',
    '2025-01-23 16:30:00', 'COMPLETED', 1,
    3, '外出打卡补卡,有拜访计划证明'
);

-- ==================================================================
-- 使用示例查询SQL
-- ==================================================================

-- 示例1: 查询待审批的补卡申请
-- SELECT * FROM t_attendance_makeup_card
-- WHERE approval_status = 'PENDING'
--   AND deleted_flag = 0
-- ORDER BY application_time ASC;

-- 示例2: 查询某员工的补卡记录
-- SELECT * FROM t_attendance_makeup_card
-- WHERE employee_id = 1001
--   AND deleted_flag = 0
-- ORDER BY makeup_date DESC;

-- 示例3: 查询某部门的补卡统计
-- SELECT department_name, COUNT(*) as makeup_count,
--        SUM(CASE WHEN approval_status='APPROVED' THEN 1 ELSE 0 END) as approved_count,
--        SUM(CASE WHEN approval_status='REJECTED' THEN 1 ELSE 0 END) as rejected_count
-- FROM t_attendance_makeup_card
-- WHERE department_id = 101
--   AND makeup_date >= '2025-01-01'
--   AND deleted_flag = 0
-- GROUP BY department_id, department_name;

-- 示例4: 查询某员工本月补卡次数
-- SELECT employee_id, employee_name, COUNT(*) as monthly_count
-- FROM t_attendance_makeup_card
-- WHERE employee_id = 1001
--   AND makeup_date >= DATE_FORMAT(CURDATE(), '%Y-%m-01')
--   AND deleted_flag = 0
-- GROUP BY employee_id, employee_name;

-- 示例5: 查询需要二级审批的补卡申请
-- SELECT * FROM t_attendance_makeup_card
-- WHERE approval_level = 2
--   AND first_approval_result = 'APPROVED'
--   AND approval_status = 'PENDING'
--   AND deleted_flag = 0;

-- 示例6: 查询超时未审批的补卡申请
-- SELECT * FROM t_attendance_makeup_card
-- WHERE approval_status = 'PENDING'
--   AND approval_deadline < NOW()
--   AND is_overdue = 0
--   AND deleted_flag = 0;

-- 示例7: 查询有证明材料的补卡申请
-- SELECT * FROM t_attendance_makeup_card
-- WHERE has_proof = 1
--   AND approval_status = 'APPROVED'
--   AND deleted_flag = 0;

-- 示例8: 查询补卡原因类型统计
-- SELECT reason_type, COUNT(*) as count
-- FROM t_attendance_makeup_card
-- WHERE deleted_flag = 0
-- GROUP BY reason_type
-- ORDER BY count DESC;

-- ==================================================================
-- 维护建议
-- ==================================================================
-- 1. 定期检查超时未审批的补卡申请,发送催办通知
-- 2. 监控补卡频率,识别异常补卡行为
-- 3. 定期审查补卡审批流程,优化审批效率
-- 4. 统计分析补卡原因,改进考勤系统和流程
-- 5. 清理过期的补卡记录(保留6个月)

-- ==================================================================
-- 定时任务建议
-- ==================================================================
-- 1. 每天上午检查待审批的补卡申请,发送审批提醒
-- 2. 每天检查超时未审批的申请,自动标记为超时
-- 3. 每周统计补卡情况,生成分析报表
-- 4. 每月初重置员工补卡次数统计
-- 5. 实时监控补卡审批进度,超时自动预警
-- 6. 自动更新考勤记录(审批通过后)

-- ==================================================================
-- 注意事项
-- ==================================================================
-- 1. 补卡申请必须在规定时间内提交(通常7天内)
-- 2. 每月补卡次数有限制(通常5次)
-- 3. 补卡需要提供合理的原因和证明材料
-- 4. 审批流程需要明确,避免审批延误
-- 5. 补卡审批通过后自动更新考勤记录
-- 6. 频繁补卡的员工需要特别关注和管理
-- 7. 补卡规则需要与考勤制度保持一致
-- 8. 补卡记录需要完整保存,用于审计追溯

-- ==================================================================
-- 版本历史
-- ==================================================================
-- v1.0.0 - 2025-12-08
--   - 初始版本
--   - 支持补卡申请提交
--   - 支持多级审批流程
--   - 支持证明材料上传
--   - 支持补卡规则验证(时间限制、次数限制)
--   - 支持自动更新考勤记录
--   - 支持补卡统计分析
--   - 初始化10条企业级补卡记录示例

-- ==================================================================
-- 文件结束
-- ==================================================================
