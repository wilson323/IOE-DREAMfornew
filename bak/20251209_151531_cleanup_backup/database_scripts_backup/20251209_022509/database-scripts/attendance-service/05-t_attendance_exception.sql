-- ============================================================
-- IOE-DREAM 考勤服务 - 异常申请表
-- 表名: t_attendance_exception
-- 说明: 管理考勤异常申请（请假、补卡、加班、出差等）
-- 版本: v1.0.0
-- 创建时间: 2025-12-08
-- ============================================================

USE `ioedream_attendance_db`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 异常申请表
-- ----------------------------
DROP TABLE IF EXISTS `t_attendance_exception`;

CREATE TABLE IF NOT EXISTS `t_attendance_exception` (
    -- 主键
    `exception_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '异常申请ID（主键）',
    `exception_code` VARCHAR(100) NOT NULL COMMENT '异常申请编码（唯一标识，如：EXC20250108001）',
    
    -- 申请人信息
    `user_id` BIGINT NOT NULL COMMENT '申请人用户ID（关联用户表）',
    `user_name` VARCHAR(100) NOT NULL COMMENT '申请人姓名',
    `employee_code` VARCHAR(50) NOT NULL COMMENT '申请人工号',
    `department_id` BIGINT NOT NULL COMMENT '申请人部门ID（关联部门表）',
    `department_name` VARCHAR(200) COMMENT '申请人部门名称',
    `position_name` VARCHAR(100) COMMENT '申请人岗位名称',
    
    -- 异常类型和分类
    `exception_type` VARCHAR(50) NOT NULL COMMENT '异常类型：LEAVE-请假, MAKEUP_CARD-补卡, OVERTIME-加班申请, BUSINESS_TRIP-出差, FIELD_WORK-外勤, LATE_EARLY-迟到早退说明',
    `exception_category` VARCHAR(50) COMMENT '异常分类（与类型对应）：
                                                -- 请假类型：ANNUAL-年假, SICK-病假, PERSONAL-事假, MATERNITY-产假, PATERNITY-陪产假, MARRIAGE-婚假, BEREAVEMENT-丧假, COMPENSATORY-调休
                                                -- 补卡原因：FORGET-忘记打卡, DEVICE_ERROR-设备故障, FIELD_WORK-外勤未打卡, OTHER-其他原因
                                                -- 加班类型：WEEKDAY-工作日加班, WEEKEND-周末加班, HOLIDAY-节假日加班
                                                -- 出差类型：LOCAL-市内, DOMESTIC-国内, INTERNATIONAL-国际',
    
    -- 时间信息
    `start_date` DATE NOT NULL COMMENT '开始日期',
    `end_date` DATE NOT NULL COMMENT '结束日期',
    `start_time` DATETIME COMMENT '开始时间（精确到时分，用于半天假、加班等）',
    `end_time` DATETIME COMMENT '结束时间（精确到时分）',
    `duration_days` DECIMAL(5,2) COMMENT '时长（天数，支持0.5天半天假）',
    `duration_hours` DECIMAL(5,2) COMMENT '时长（小时数，用于加班、外勤等）',
    `time_unit` VARCHAR(20) NOT NULL DEFAULT 'DAY' COMMENT '时间单位：DAY-天, HOUR-小时, MINUTE-分钟',
    
    -- 申请详情
    `reason` TEXT NOT NULL COMMENT '申请原因（必填，详细说明异常原因）',
    `destination` VARCHAR(500) COMMENT '目的地（出差、外勤使用）',
    `contact_phone` VARCHAR(50) COMMENT '联系电话（出差、外勤使用）',
    `attachment_urls` JSON COMMENT '附件URLs（JSON数组，如病假条、证明文件等）
                                    示例：["https://xxx.com/file1.pdf", "https://xxx.com/file2.jpg"]',
    `remark` TEXT COMMENT '备注说明',
    
    -- 审批流程信息
    `workflow_instance_id` BIGINT COMMENT '工作流实例ID（关联OA工作流）',
    `approval_status` VARCHAR(50) NOT NULL DEFAULT 'PENDING' COMMENT '审批状态：
                                                                       PENDING-待审批, 
                                                                       APPROVING-审批中, 
                                                                       APPROVED-已通过, 
                                                                       REJECTED-已拒绝, 
                                                                       CANCELLED-已撤销, 
                                                                       EXPIRED-已过期',
    `current_approver_id` BIGINT COMMENT '当前审批人ID',
    `current_approver_name` VARCHAR(100) COMMENT '当前审批人姓名',
    `approval_opinion` TEXT COMMENT '审批意见（最终审批意见汇总）',
    `reject_reason` VARCHAR(500) COMMENT '拒绝原因（审批拒绝时填写）',
    
    -- 审批时间
    `submit_time` DATETIME COMMENT '提交时间',
    `approve_time` DATETIME COMMENT '审批完成时间',
    `cancel_time` DATETIME COMMENT '撤销时间',
    
    -- 关联考勤记录
    `related_record_ids` JSON COMMENT '关联的考勤记录IDs（JSON数组）
                                        示例：[10001, 10002]（用于补卡、迟到早退说明等）',
    
    -- 影响统计
    `affect_attendance_days` INT DEFAULT 0 COMMENT '影响考勤天数（用于统计）',
    `deduct_salary` TINYINT DEFAULT 0 COMMENT '是否扣薪：0-不扣薪, 1-扣薪',
    `deduct_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '扣薪金额（元）',
    
    -- 状态标识
    `is_urgent` TINYINT DEFAULT 0 COMMENT '是否加急：0-否, 1-是',
    `is_auto_approved` TINYINT DEFAULT 0 COMMENT '是否自动审批：0-否, 1-是（符合自动审批规则）',
    `exception_status` VARCHAR(50) NOT NULL DEFAULT 'ACTIVE' COMMENT '异常状态：
                                                                       ACTIVE-生效中, 
                                                                       INACTIVE-已失效, 
                                                                       PROCESSING-处理中',
    
    -- 版本控制
    `version` INT NOT NULL DEFAULT 1 COMMENT '版本号（用于乐观锁）',
    
    -- 审计字段（标准字段）
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除, 1-已删除',
    
    -- 主键约束
    PRIMARY KEY (`exception_id`),
    
    -- 唯一约束
    UNIQUE KEY `uk_exception_code` (`exception_code`),
    
    -- 普通索引
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_department_id` (`department_id`),
    INDEX `idx_exception_type` (`exception_type`, `exception_category`),
    INDEX `idx_approval_status` (`approval_status`, `exception_status`),
    INDEX `idx_workflow_instance_id` (`workflow_instance_id`),
    INDEX `idx_date_range` (`start_date`, `end_date`),
    INDEX `idx_submit_time` (`submit_time` DESC),
    INDEX `idx_create_time` (`create_time` DESC),
    INDEX `idx_deleted_flag` (`deleted_flag`)
    
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='考勤服务-异常申请表';

-- ----------------------------
-- 初始化数据（示例数据）
-- ----------------------------

-- 示例1: 年假申请（2天）
INSERT INTO `t_attendance_exception` (
    `exception_code`, `user_id`, `user_name`, `employee_code`, `department_id`, `department_name`, `position_name`,
    `exception_type`, `exception_category`,
    `start_date`, `end_date`, `start_time`, `end_time`, `duration_days`, `duration_hours`, `time_unit`,
    `reason`, `attachment_urls`, `remark`,
    `workflow_instance_id`, `approval_status`, `current_approver_id`, `current_approver_name`, `approval_opinion`,
    `submit_time`, `approve_time`,
    `related_record_ids`, `affect_attendance_days`, `deduct_salary`, `deduct_amount`,
    `is_urgent`, `is_auto_approved`, `exception_status`,
    `version`, `create_user_id`, `deleted_flag`
) VALUES (
    'EXC20250108001', 1001, '张三', 'EMP001', 2001, '行政部', '行政专员',
    'LEAVE', 'ANNUAL',
    '2025-01-15', '2025-01-16', '2025-01-15 09:00:00', '2025-01-16 18:00:00', 2.00, NULL, 'DAY',
    '计划回老家处理家庭事务，申请年假2天。', 
    NULL, '已提前安排好工作交接，不影响部门正常运作。',
    10001, 'APPROVED', NULL, NULL, '同意，注意安排好工作交接。',
    '2025-01-08 10:30:00', '2025-01-08 15:20:00',
    NULL, 2, 0, 0.00,
    0, 0, 'ACTIVE',
    1, 1001, 0
);

-- 示例2: 病假申请（半天）
INSERT INTO `t_attendance_exception` (
    `exception_code`, `user_id`, `user_name`, `employee_code`, `department_id`, `department_name`, `position_name`,
    `exception_type`, `exception_category`,
    `start_date`, `end_date`, `start_time`, `end_time`, `duration_days`, `duration_hours`, `time_unit`,
    `reason`, `attachment_urls`, `remark`,
    `workflow_instance_id`, `approval_status`, `current_approver_id`, `current_approver_name`, `approval_opinion`,
    `submit_time`, `approve_time`,
    `related_record_ids`, `affect_attendance_days`, `deduct_salary`, `deduct_amount`,
    `is_urgent`, `is_auto_approved`, `exception_status`,
    `version`, `create_user_id`, `deleted_flag`
) VALUES (
    'EXC20250108002', 1002, '李四', 'EMP002', 2002, '技术部', '软件工程师',
    'LEAVE', 'SICK',
    '2025-01-10', '2025-01-10', '2025-01-10 09:00:00', '2025-01-10 13:00:00', 0.50, 4.00, 'DAY',
    '身体不适，需要就医，申请上午病假。', 
    '["https://oss.ioedream.com/attachments/medical_certificate_001.jpg"]', '已挂号市人民医院，预计下午返岗。',
    10002, 'APPROVED', NULL, NULL, '批准，注意休息。',
    '2025-01-09 20:15:00', '2025-01-09 21:05:00',
    NULL, 0, 0, 0.00,
    0, 0, 'ACTIVE',
    1, 1002, 0
);

-- 示例3: 事假申请（1天，扣薪）
INSERT INTO `t_attendance_exception` (
    `exception_code`, `user_id`, `user_name`, `employee_code`, `department_id`, `department_name`, `position_name`,
    `exception_type`, `exception_category`,
    `start_date`, `end_date`, `start_time`, `end_time`, `duration_days`, `duration_hours`, `time_unit`,
    `reason`, `attachment_urls`, `remark`,
    `workflow_instance_id`, `approval_status`, `current_approver_id`, `current_approver_name`, `approval_opinion`,
    `submit_time`, `approve_time`,
    `related_record_ids`, `affect_attendance_days`, `deduct_salary`, `deduct_amount`,
    `is_urgent`, `is_auto_approved`, `exception_status`,
    `version`, `create_user_id`, `deleted_flag`
) VALUES (
    'EXC20250108003', 1003, '王五', 'EMP003', 2003, '销售部', '销售代表',
    'LEAVE', 'PERSONAL',
    '2025-01-12', '2025-01-12', '2025-01-12 09:00:00', '2025-01-12 18:00:00', 1.00, NULL, 'DAY',
    '需要处理个人紧急事务，申请事假1天。', 
    NULL, '会在假期前完成手头工作。',
    10003, 'APPROVED', NULL, NULL, '同意，按规定扣除日薪。',
    '2025-01-09 09:20:00', '2025-01-09 14:30:00',
    NULL, 1, 1, 300.00,
    0, 0, 'ACTIVE',
    1, 1003, 0
);

-- 示例4: 补卡申请（忘记打卡）
INSERT INTO `t_attendance_exception` (
    `exception_code`, `user_id`, `user_name`, `employee_code`, `department_id`, `department_name`, `position_name`,
    `exception_type`, `exception_category`,
    `start_date`, `end_date`, `start_time`, `end_time`, `duration_days`, `duration_hours`, `time_unit`,
    `reason`, `attachment_urls`, `remark`,
    `workflow_instance_id`, `approval_status`, `current_approver_id`, `current_approver_name`, `approval_opinion`,
    `submit_time`, `approve_time`,
    `related_record_ids`, `affect_attendance_days`, `deduct_salary`, `deduct_amount`,
    `is_urgent`, `is_auto_approved`, `exception_status`,
    `version`, `create_user_id`, `deleted_flag`
) VALUES (
    'EXC20250108004', 1004, '赵六', 'EMP004', 2001, '行政部', '人力资源专员',
    'MAKEUP_CARD', 'FORGET',
    '2025-01-08', '2025-01-08', '2025-01-08 09:05:00', '2025-01-08 18:02:00', NULL, NULL, 'HOUR',
    '因参加紧急会议，忘记在考勤机打卡，现申请补卡。实际上班时间09:05，下班时间18:02。', 
    NULL, '会议记录可查，确实在公司正常办公。',
    10004, 'APPROVED', NULL, NULL, '经核实确实在岗，同意补卡。',
    '2025-01-08 19:00:00', '2025-01-08 19:30:00',
    '[20001, 20002]', 0, 0, 0.00,
    0, 0, 'ACTIVE',
    1, 1004, 0
);

-- 示例5: 加班申请（工作日加班3小时）
INSERT INTO `t_attendance_exception` (
    `exception_code`, `user_id`, `user_name`, `employee_code`, `department_id`, `department_name`, `position_name`,
    `exception_type`, `exception_category`,
    `start_date`, `end_date`, `start_time`, `end_time`, `duration_days`, `duration_hours`, `time_unit`,
    `reason`, `attachment_urls`, `remark`,
    `workflow_instance_id`, `approval_status`, `current_approver_id`, `current_approver_name`, `approval_opinion`,
    `submit_time`, `approve_time`,
    `related_record_ids`, `affect_attendance_days`, `deduct_salary`, `deduct_amount`,
    `is_urgent`, `is_auto_approved`, `exception_status`,
    `version`, `create_user_id`, `deleted_flag`
) VALUES (
    'EXC20250108005', 1002, '李四', 'EMP002', 2002, '技术部', '软件工程师',
    'OVERTIME', 'WEEKDAY',
    '2025-01-10', '2025-01-10', '2025-01-10 18:00:00', '2025-01-10 21:00:00', NULL, 3.00, 'HOUR',
    '因项目紧急上线，需要加班处理系统部署和测试工作。', 
    NULL, '已与项目经理沟通确认。',
    10005, 'APPROVED', NULL, NULL, '项目确实紧急，同意加班，按1.5倍计算加班费。',
    '2025-01-10 17:30:00', '2025-01-10 17:50:00',
    NULL, 0, 0, 0.00,
    1, 0, 'ACTIVE',
    1, 1002, 0
);

-- 示例6: 周末加班申请（全天）
INSERT INTO `t_attendance_exception` (
    `exception_code`, `user_id`, `user_name`, `employee_code`, `department_id`, `department_name`, `position_name`,
    `exception_type`, `exception_category`,
    `start_date`, `end_date`, `start_time`, `end_time`, `duration_days`, `duration_hours`, `time_unit`,
    `reason`, `attachment_urls`, `remark`,
    `workflow_instance_id`, `approval_status`, `current_approver_id`, `current_approver_name`, `approval_opinion`,
    `submit_time`, `approve_time`,
    `related_record_ids`, `affect_attendance_days`, `deduct_salary`, `deduct_amount`,
    `is_urgent`, `is_auto_approved`, `exception_status`,
    `version`, `create_user_id`, `deleted_flag`
) VALUES (
    'EXC20250108006', 1005, '孙七', 'EMP005', 2002, '技术部', '系统架构师',
    'OVERTIME', 'WEEKEND',
    '2025-01-11', '2025-01-11', '2025-01-11 09:00:00', '2025-01-11 18:00:00', 1.00, 8.00, 'DAY',
    '紧急系统故障修复，需要周末加班处理核心服务问题。', 
    NULL, '已申请IT运维支持配合。',
    10006, 'APPROVED', NULL, NULL, '故障影响生产环境，批准加班，按2倍计算加班费。',
    '2025-01-10 22:00:00', '2025-01-10 22:15:00',
    NULL, 0, 0, 0.00,
    1, 0, 'ACTIVE',
    1, 1005, 0
);

-- 示例7: 出差申请（国内3天）
INSERT INTO `t_attendance_exception` (
    `exception_code`, `user_id`, `user_name`, `employee_code`, `department_id`, `department_name`, `position_name`,
    `exception_type`, `exception_category`,
    `start_date`, `end_date`, `start_time`, `end_time`, `duration_days`, `duration_hours`, `time_unit`,
    `reason`, `destination`, `contact_phone`, `attachment_urls`, `remark`,
    `workflow_instance_id`, `approval_status`, `current_approver_id`, `current_approver_name`, `approval_opinion`,
    `submit_time`, `approve_time`,
    `related_record_ids`, `affect_attendance_days`, `deduct_salary`, `deduct_amount`,
    `is_urgent`, `is_auto_approved`, `exception_status`,
    `version`, `create_user_id`, `deleted_flag`
) VALUES (
    'EXC20250108007', 1003, '王五', 'EMP003', 2003, '销售部', '销售代表',
    'BUSINESS_TRIP', 'DOMESTIC',
    '2025-01-13', '2025-01-15', '2025-01-13 09:00:00', '2025-01-15 18:00:00', 3.00, NULL, 'DAY',
    '前往深圳参加重要客户洽谈会议，签订年度合作协议。',
    '广东省深圳市南山区科技园', '13800138003',
    '["https://oss.ioedream.com/attachments/meeting_invitation_001.pdf"]', '已预订往返机票和酒店。',
    10007, 'APPROVED', NULL, NULL, '重要客户，同意出差，注意安全。',
    '2025-01-08 14:00:00', '2025-01-08 16:30:00',
    NULL, 0, 0, 0.00,
    0, 0, 'ACTIVE',
    1, 1003, 0
);

-- 示例8: 外勤申请（半天）
INSERT INTO `t_attendance_exception` (
    `exception_code`, `user_id`, `user_name`, `employee_code`, `department_id`, `department_name`, `position_name`,
    `exception_type`, `exception_category`,
    `start_date`, `end_date`, `start_time`, `end_time`, `duration_days`, `duration_hours`, `time_unit`,
    `reason`, `destination`, `contact_phone`, `attachment_urls`, `remark`,
    `workflow_instance_id`, `approval_status`, `current_approver_id`, `current_approver_name`, `approval_opinion`,
    `submit_time`, `approve_time`,
    `related_record_ids`, `affect_attendance_days`, `deduct_salary`, `deduct_amount`,
    `is_urgent`, `is_auto_approved`, `exception_status`,
    `version`, `create_user_id`, `deleted_flag`
) VALUES (
    'EXC20250108008', 1006, '周八', 'EMP006', 2004, '运维部', '网络工程师',
    'FIELD_WORK', NULL,
    '2025-01-09', '2025-01-09', '2025-01-09 14:00:00', '2025-01-09 18:00:00', 0.50, 4.00, 'HOUR',
    '前往分公司机房处理网络故障，现场维护设备。',
    '市内分公司（距离总部15公里）', '13900139006',
    NULL, '已携带维修工具箱和备件。',
    10008, 'APPROVED', NULL, NULL, '批准外勤，注意设备安全。',
    '2025-01-09 13:30:00', '2025-01-09 13:45:00',
    NULL, 0, 0, 0.00,
    0, 0, 'ACTIVE',
    1, 1006, 0
);

-- 示例9: 迟到早退说明（未审批）
INSERT INTO `t_attendance_exception` (
    `exception_code`, `user_id`, `user_name`, `employee_code`, `department_id`, `department_name`, `position_name`,
    `exception_type`, `exception_category`,
    `start_date`, `end_date`, `start_time`, `end_time`, `duration_days`, `duration_hours`, `time_unit`,
    `reason`, `attachment_urls`, `remark`,
    `workflow_instance_id`, `approval_status`, `current_approver_id`, `current_approver_name`, `approval_opinion`,
    `submit_time`,
    `related_record_ids`, `affect_attendance_days`, `deduct_salary`, `deduct_amount`,
    `is_urgent`, `is_auto_approved`, `exception_status`,
    `version`, `create_user_id`, `deleted_flag`
) VALUES (
    'EXC20250108009', 1007, '吴九', 'EMP007', 2005, '财务部', '会计',
    'LATE_EARLY', NULL,
    '2025-01-09', '2025-01-09', '2025-01-09 09:15:00', '2025-01-09 17:30:00', NULL, NULL, 'MINUTE',
    '因地铁故障导致迟到15分钟，提前30分钟离开是因为需要去银行处理公司业务。',
    '["https://oss.ioedream.com/attachments/metro_delay_notice_001.jpg"]', '地铁官方发布延误公告，银行业务有凭证。',
    10009, 'PENDING', 3001, '财务经理', NULL,
    '2025-01-09 18:00:00',
    '[20010]', 0, 0, 0.00,
    0, 0, 'ACTIVE',
    1, 1007, 0
);

-- 示例10: 婚假申请（3天）
INSERT INTO `t_attendance_exception` (
    `exception_code`, `user_id`, `user_name`, `employee_code`, `department_id`, `department_name`, `position_name`,
    `exception_type`, `exception_category`,
    `start_date`, `end_date`, `start_time`, `end_time`, `duration_days`, `duration_hours`, `time_unit`,
    `reason`, `attachment_urls`, `remark`,
    `workflow_instance_id`, `approval_status`, `current_approver_id`, `current_approver_name`, `approval_opinion`,
    `submit_time`, `approve_time`,
    `related_record_ids`, `affect_attendance_days`, `deduct_salary`, `deduct_amount`,
    `is_urgent`, `is_auto_approved`, `exception_status`,
    `version`, `create_user_id`, `deleted_flag`
) VALUES (
    'EXC20250108010', 1008, '郑十', 'EMP008', 2002, '技术部', '测试工程师',
    'LEAVE', 'MARRIAGE',
    '2025-01-20', '2025-01-22', '2025-01-20 09:00:00', '2025-01-22 18:00:00', 3.00, NULL, 'DAY',
    '本人将于2025年1月举办婚礼，申请婚假3天。',
    '["https://oss.ioedream.com/attachments/marriage_certificate_001.pdf"]', '结婚证已办理，婚礼定于1月21日。',
    10010, 'APPROVED', NULL, NULL, '恭喜！批准婚假，祝新婚快乐。',
    '2025-01-08 11:00:00', '2025-01-08 11:30:00',
    NULL, 3, 0, 0.00,
    0, 0, 'ACTIVE',
    1, 1008, 0
);

-- 示例11: 调休申请（之前加班换调休）
INSERT INTO `t_attendance_exception` (
    `exception_code`, `user_id`, `user_name`, `employee_code`, `department_id`, `department_name`, `position_name`,
    `exception_type`, `exception_category`,
    `start_date`, `end_date`, `start_time`, `end_time`, `duration_days`, `duration_hours`, `time_unit`,
    `reason`, `attachment_urls`, `remark`,
    `workflow_instance_id`, `approval_status`, `current_approver_id`, `current_approver_name`, `approval_opinion`,
    `submit_time`, `approve_time`,
    `related_record_ids`, `affect_attendance_days`, `deduct_salary`, `deduct_amount`,
    `is_urgent`, `is_auto_approved`, `exception_status`,
    `version`, `create_user_id`, `deleted_flag`
) VALUES (
    'EXC20250108011', 1005, '孙七', 'EMP005', 2002, '技术部', '系统架构师',
    'LEAVE', 'COMPENSATORY',
    '2025-01-14', '2025-01-14', '2025-01-14 09:00:00', '2025-01-14 18:00:00', 1.00, NULL, 'DAY',
    '之前周末加班（EXC20250108006），现申请调休1天。',
    NULL, '加班记录可查，符合调休政策。',
    10011, 'APPROVED', NULL, NULL, '核实加班记录属实，同意调休。',
    '2025-01-13 10:00:00', '2025-01-13 14:00:00',
    NULL, 1, 0, 0.00,
    0, 1, 'ACTIVE',
    1, 1005, 0
);

-- 示例12: 申请已撤销（员工主动撤销）
INSERT INTO `t_attendance_exception` (
    `exception_code`, `user_id`, `user_name`, `employee_code`, `department_id`, `department_name`, `position_name`,
    `exception_type`, `exception_category`,
    `start_date`, `end_date`, `start_time`, `end_time`, `duration_days`, `duration_hours`, `time_unit`,
    `reason`, `attachment_urls`, `remark`,
    `workflow_instance_id`, `approval_status`, `current_approver_id`, `current_approver_name`,
    `submit_time`, `cancel_time`,
    `related_record_ids`, `affect_attendance_days`, `deduct_salary`, `deduct_amount`,
    `is_urgent`, `is_auto_approved`, `exception_status`,
    `version`, `create_user_id`, `deleted_flag`
) VALUES (
    'EXC20250108012', 1009, '冯十一', 'EMP009', 2003, '销售部', '销售经理',
    'LEAVE', 'ANNUAL',
    '2025-01-17', '2025-01-19', '2025-01-17 09:00:00', '2025-01-19 18:00:00', 3.00, NULL, 'DAY',
    '原计划年假，因客户临时要求改期，现申请撤销。',
    NULL, '已与部门沟通，延后至春节后休假。',
    10012, 'CANCELLED', NULL, NULL,
    '2025-01-08 16:00:00', '2025-01-10 09:30:00',
    NULL, 0, 0, 0.00,
    0, 0, 'INACTIVE',
    1, 1009, 0
);

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 索引说明
-- ============================================================
-- 1. uk_exception_code: 唯一索引，确保异常申请编码全局唯一
-- 2. idx_user_id: 快速查询员工的所有异常申请
-- 3. idx_department_id: 支持按部门统计异常申请
-- 4. idx_exception_type: 复合索引，支持按类型和分类筛选
-- 5. idx_approval_status: 复合索引，支持按审批状态和异常状态查询
-- 6. idx_workflow_instance_id: 关联工作流实例，支持审批流程追踪
-- 7. idx_date_range: 复合索引，支持按时间范围查询
-- 8. idx_submit_time: 降序索引，支持按提交时间倒序查询（最新优先）
-- 9. idx_create_time: 降序索引，支持数据审计追踪
-- 10. idx_deleted_flag: 支持逻辑删除查询优化

-- ============================================================
-- 使用示例
-- ============================================================

-- 1. 查询待审批的异常申请
-- SELECT * FROM t_attendance_exception 
-- WHERE approval_status = 'PENDING' 
--   AND deleted_flag = 0 
-- ORDER BY is_urgent DESC, submit_time DESC;

-- 2. 查询某员工的所有请假记录
-- SELECT * FROM t_attendance_exception 
-- WHERE user_id = 1001 
--   AND exception_type = 'LEAVE' 
--   AND deleted_flag = 0 
-- ORDER BY start_date DESC;

-- 3. 统计本月部门异常申请情况
-- SELECT 
--     exception_type,
--     exception_category,
--     approval_status,
--     COUNT(*) as count,
--     SUM(duration_days) as total_days
-- FROM t_attendance_exception 
-- WHERE department_id = 2002
--   AND DATE_FORMAT(start_date, '%Y-%m') = '2025-01'
--   AND deleted_flag = 0
-- GROUP BY exception_type, exception_category, approval_status;

-- 4. 查询加班申请及其关联的考勤记录
-- SELECT 
--     e.*,
--     r.clock_in_time,
--     r.clock_out_time
-- FROM t_attendance_exception e
-- LEFT JOIN t_attendance_record r ON JSON_CONTAINS(e.related_record_ids, CAST(r.record_id AS JSON))
-- WHERE e.exception_type = 'OVERTIME'
--   AND e.deleted_flag = 0;

-- 5. 查询需要扣薪的异常申请
-- SELECT 
--     user_name,
--     employee_code,
--     exception_type,
--     exception_category,
--     duration_days,
--     deduct_amount
-- FROM t_attendance_exception
-- WHERE deduct_salary = 1
--   AND approval_status = 'APPROVED'
--   AND DATE_FORMAT(start_date, '%Y-%m') = '2025-01'
--   AND deleted_flag = 0;

-- ============================================================
-- 维护建议
-- ============================================================
-- 1. 定期归档历史数据（建议保留2年内的异常申请）
-- 2. 定期清理已撤销或已拒绝的申请（保留3个月）
-- 3. 监控审批流程超时情况（超过3天未审批的申请）
-- 4. 定期统计异常申请趋势，优化考勤规则
-- 5. 定期检查附件文件是否有效，清理无效附件

-- ============================================================
-- 数据完整性约束建议
-- ============================================================
-- 1. 时间范围校验：end_date >= start_date, end_time >= start_time
-- 2. 时长计算校验：duration_days 和 duration_hours 需与时间范围匹配
-- 3. 审批状态流转：PENDING -> APPROVING -> APPROVED/REJECTED/CANCELLED
-- 4. 关联记录验证：related_record_ids 中的记录必须存在于 t_attendance_record
-- 5. 附件有效性：attachment_urls 中的URL需确保可访问

-- ============================================================
-- 性能优化建议
-- ============================================================
-- 1. 对于高频查询（按用户、按部门、按审批状态），已建立合适索引
-- 2. JSON字段（attachment_urls、related_record_ids）查询时注意性能
-- 3. 可考虑将历史数据分表存储（按年或按季度）
-- 4. 对于统计查询，可建立定时任务预先计算并缓存结果
-- 5. 审批流程状态变更时，使用消息队列异步处理

-- ============================================================
-- 定时任务建议
-- ============================================================
-- 1. 每日凌晨检查过期未审批的申请（超过3天），发送提醒通知
-- 2. 每周统计异常申请趋势，生成周报
-- 3. 每月月初统计上月异常申请汇总，推送给部门负责人
-- 4. 季度末归档历史异常申请数据
-- 5. 定期清理无效附件文件（关联记录已删除或已过保留期）
