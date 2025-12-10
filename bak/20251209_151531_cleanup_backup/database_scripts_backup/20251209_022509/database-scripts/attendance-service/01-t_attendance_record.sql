-- ====================================================
-- IOE-DREAM 智慧园区一卡通管理平台
-- 考勤服务 - 考勤记录表
-- ====================================================
-- 文件: 01-t_attendance_record.sql
-- 版本: v1.0.0
-- 创建日期: 2025-12-08
-- 描述: 考勤打卡记录表，记录所有考勤打卡行为和考勤结果
-- ====================================================

USE `ioedream_attendance_db`;

-- ====================================================
-- 表: t_attendance_record - 考勤记录表
-- ====================================================
-- 功能说明:
-- 1. 记录所有考勤打卡行为（签到、签退）
-- 2. 支持多种打卡方式（人脸、指纹、刷卡、手机）
-- 3. 自动识别考勤状态（正常、迟到、早退、缺卡、旷工）
-- 4. 与门禁通行记录关联，支持联动分析
-- 5. 支持补卡、调班、请假等特殊考勤处理
-- ====================================================

CREATE TABLE IF NOT EXISTS `t_attendance_record` (
    -- ====================================================
    -- 主键和编码
    -- ====================================================
    `record_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '考勤记录ID',
    `record_code` VARCHAR(100) NOT NULL COMMENT '考勤记录编码，格式：ATT_YYYYMMDD_序号',
    
    -- ====================================================
    -- 人员信息
    -- ====================================================
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `user_name` VARCHAR(100) NOT NULL COMMENT '用户姓名',
    `employee_code` VARCHAR(50) NOT NULL COMMENT '工号',
    `department_id` BIGINT NOT NULL COMMENT '部门ID',
    `department_name` VARCHAR(200) NOT NULL COMMENT '部门名称',
    
    -- ====================================================
    -- 考勤时间信息
    -- ====================================================
    `attendance_date` DATE NOT NULL COMMENT '考勤日期',
    `attendance_time` DATETIME NOT NULL COMMENT '实际打卡时间',
    `attendance_type` VARCHAR(50) NOT NULL COMMENT '考勤类型：SIGN_IN-签到, SIGN_OUT-签退, OVERTIME_IN-加班签到, OVERTIME_OUT-加班签退',
    
    -- ====================================================
    -- 班次信息
    -- ====================================================
    `shift_id` BIGINT COMMENT '班次ID',
    `shift_name` VARCHAR(100) COMMENT '班次名称',
    `shift_start_time` TIME COMMENT '班次开始时间',
    `shift_end_time` TIME COMMENT '班次结束时间',
    `scheduled_time` DATETIME COMMENT '应打卡时间（根据班次计算）',
    
    -- ====================================================
    -- 考勤状态
    -- ====================================================
    `attendance_status` VARCHAR(50) NOT NULL COMMENT '考勤状态：NORMAL-正常, LATE-迟到, EARLY_LEAVE-早退, ABSENT-缺卡, ABSENTEEISM-旷工, MAKEUP-补卡, LEAVE-请假, BUSINESS_TRIP-出差, OVERTIME-加班',
    `time_difference` INT DEFAULT 0 COMMENT '时间差（分钟），正数迟到，负数早退',
    `is_abnormal` TINYINT DEFAULT 0 COMMENT '是否异常：0-正常, 1-异常（迟到/早退/缺卡）',
    
    -- ====================================================
    -- 打卡设备信息
    -- ====================================================
    `device_id` BIGINT COMMENT '考勤设备ID',
    `device_code` VARCHAR(100) COMMENT '考勤设备编码',
    `device_name` VARCHAR(200) COMMENT '考勤设备名称',
    `device_location` VARCHAR(200) COMMENT '设备位置',
    
    -- ====================================================
    -- 打卡方式
    -- ====================================================
    `attendance_method` VARCHAR(50) NOT NULL COMMENT '打卡方式：FACE-人脸识别, FINGERPRINT-指纹, CARD-刷卡, QR_CODE-二维码, MOBILE-手机APP, MANUAL-手动录入',
    `recognition_score` DECIMAL(5,2) COMMENT '识别置信度（%），人脸/指纹识别有效',
    
    -- ====================================================
    -- 位置信息（移动打卡）
    -- ====================================================
    `gps_longitude` DECIMAL(10,6) COMMENT 'GPS经度',
    `gps_latitude` DECIMAL(10,6) COMMENT 'GPS纬度',
    `gps_address` VARCHAR(500) COMMENT 'GPS地址',
    `gps_distance` INT COMMENT 'GPS距离（米），与打卡点的距离',
    `ip_address` VARCHAR(50) COMMENT 'IP地址',
    
    -- ====================================================
    -- 关联信息
    -- ====================================================
    `access_record_id` BIGINT COMMENT '关联门禁记录ID（如果通过门禁打卡）',
    `access_device_id` BIGINT COMMENT '关联门禁设备ID',
    `area_id` BIGINT COMMENT '区域ID',
    `area_name` VARCHAR(200) COMMENT '区域名称',
    
    -- ====================================================
    -- 异常处理
    -- ====================================================
    `exception_type` VARCHAR(50) COMMENT '异常类型：LATE-迟到, EARLY_LEAVE-早退, ABSENT-缺卡, ABSENTEEISM-旷工',
    `exception_reason` VARCHAR(500) COMMENT '异常原因',
    `exception_handler_id` BIGINT COMMENT '异常处理人ID',
    `exception_handler_name` VARCHAR(100) COMMENT '异常处理人姓名',
    `exception_handle_time` DATETIME COMMENT '异常处理时间',
    `exception_remark` VARCHAR(500) COMMENT '异常处理备注',
    
    -- ====================================================
    -- 补卡信息
    -- ====================================================
    `is_makeup` TINYINT DEFAULT 0 COMMENT '是否补卡：0-正常打卡, 1-补卡',
    `makeup_apply_id` BIGINT COMMENT '补卡申请ID',
    `makeup_reason` VARCHAR(500) COMMENT '补卡原因',
    `makeup_approver_id` BIGINT COMMENT '补卡审批人ID',
    `makeup_approver_name` VARCHAR(100) COMMENT '补卡审批人姓名',
    `makeup_approve_time` DATETIME COMMENT '补卡审批时间',
    
    -- ====================================================
    -- 请假/出差信息
    -- ====================================================
    `leave_apply_id` BIGINT COMMENT '请假申请ID',
    `leave_type` VARCHAR(50) COMMENT '请假类型：ANNUAL-年假, SICK-病假, PERSONAL-事假, MARRIAGE-婚假, MATERNITY-产假, PATERNITY-陪产假',
    `business_trip_id` BIGINT COMMENT '出差申请ID',
    
    -- ====================================================
    -- 加班信息
    -- ====================================================
    `is_overtime` TINYINT DEFAULT 0 COMMENT '是否加班：0-否, 1-是',
    `overtime_apply_id` BIGINT COMMENT '加班申请ID',
    `overtime_type` VARCHAR(50) COMMENT '加班类型：WEEKDAY-工作日, WEEKEND-周末, HOLIDAY-节假日',
    `overtime_hours` DECIMAL(5,2) COMMENT '加班时长（小时）',
    
    -- ====================================================
    -- 数据完整性
    -- ====================================================
    `photo_url` VARCHAR(500) COMMENT '打卡照片URL（人脸识别抓拍）',
    `temperature` DECIMAL(4,1) COMMENT '体温（℃），疫情防控使用',
    `health_status` VARCHAR(50) COMMENT '健康状态：NORMAL-正常, FEVER-发热, ABNORMAL-异常',
    
    -- ====================================================
    -- 统计信息
    -- ====================================================
    `work_hours` DECIMAL(5,2) COMMENT '工作时长（小时），当天累计',
    `effective_hours` DECIMAL(5,2) COMMENT '有效工时（小时），扣除迟到早退',
    
    -- ====================================================
    -- 扩展字段
    -- ====================================================
    `extended_attributes` JSON COMMENT '扩展属性（JSON格式）：天气、心率、步数等',
    `remark` VARCHAR(500) COMMENT '备注',
    
    -- ====================================================
    -- 审计字段（标准字段，所有表必须包含）
    -- ====================================================
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除, 1-已删除',
    
    -- ====================================================
    -- 索引定义
    -- ====================================================
    PRIMARY KEY (`record_id`),
    UNIQUE KEY `uk_record_code` (`record_code`),
    INDEX `idx_user_date` (`user_id`, `attendance_date`),
    INDEX `idx_user_time` (`user_id`, `attendance_time`),
    INDEX `idx_department_date` (`department_id`, `attendance_date`),
    INDEX `idx_status` (`attendance_status`),
    INDEX `idx_date_status` (`attendance_date`, `attendance_status`),
    INDEX `idx_device` (`device_id`, `attendance_time`),
    INDEX `idx_shift` (`shift_id`, `attendance_date`),
    INDEX `idx_access_record` (`access_record_id`),
    INDEX `idx_abnormal` (`is_abnormal`, `attendance_date`),
    INDEX `idx_create_time` (`create_time`),
    INDEX `idx_deleted` (`deleted_flag`)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤记录表';

-- ====================================================
-- 索引优化说明
-- ====================================================
-- 1. uk_record_code: 考勤记录编码唯一索引，防止重复记录
-- 2. idx_user_date: 用户+日期联合索引，覆盖个人考勤查询（最常用）
-- 3. idx_user_time: 用户+时间索引，覆盖时间范围查询
-- 4. idx_department_date: 部门+日期索引，覆盖部门考勤统计
-- 5. idx_status: 考勤状态索引，查询异常考勤
-- 6. idx_date_status: 日期+状态联合索引，日报表查询
-- 7. idx_device: 设备+时间索引，设备使用统计
-- 8. idx_shift: 班次+日期索引，班次考勤分析
-- 9. idx_access_record: 门禁记录索引，联动分析
-- 10. idx_abnormal: 异常标记索引，异常考勤筛选

-- ====================================================
-- 初始化数据：示例考勤记录
-- ====================================================

-- 示例1：正常签到记录（人脸识别）
INSERT INTO `t_attendance_record` (
    `record_code`, `user_id`, `user_name`, `employee_code`, `department_id`, `department_name`,
    `attendance_date`, `attendance_time`, `attendance_type`,
    `shift_id`, `shift_name`, `shift_start_time`, `shift_end_time`, `scheduled_time`,
    `attendance_status`, `time_difference`, `is_abnormal`,
    `device_id`, `device_code`, `device_name`, `device_location`,
    `attendance_method`, `recognition_score`,
    `area_id`, `area_name`,
    `photo_url`, `temperature`, `health_status`,
    `work_hours`, `effective_hours`,
    `create_user_id`, `deleted_flag`
) VALUES (
    'ATT_20250108_0001', 1, '张三', 'EMP001', 1, '技术部',
    '2025-01-08', '2025-01-08 08:28:00', 'SIGN_IN',
    1, '标准工作日班次', '08:30:00', '17:30:00', '2025-01-08 08:30:00',
    'NORMAL', -2, 0,
    1, 'ATT_DEVICE_001', '技术部考勤机', '1号楼1层大厅',
    'FACE', 98.50,
    1, '技术部办公区',
    '/uploads/attendance/face/20250108/082800_001.jpg', 36.5, 'NORMAL',
    0.00, 0.00,
    1, 0
);

-- 示例2：迟到记录（刷卡）
INSERT INTO `t_attendance_record` (
    `record_code`, `user_id`, `user_name`, `employee_code`, `department_id`, `department_name`,
    `attendance_date`, `attendance_time`, `attendance_type`,
    `shift_id`, `shift_name`, `shift_start_time`, `shift_end_time`, `scheduled_time`,
    `attendance_status`, `time_difference`, `is_abnormal`,
    `device_id`, `device_code`, `device_name`, `device_location`,
    `attendance_method`,
    `exception_type`, `exception_reason`,
    `area_id`, `area_name`,
    `temperature`, `health_status`,
    `create_user_id`, `deleted_flag`
) VALUES (
    'ATT_20250108_0002', 2, '李四', 'EMP002', 1, '技术部',
    '2025-01-08', '2025-01-08 09:15:00', 'SIGN_IN',
    1, '标准工作日班次', '08:30:00', '17:30:00', '2025-01-08 08:30:00',
    'LATE', 45, 1,
    1, 'ATT_DEVICE_001', '技术部考勤机', '1号楼1层大厅',
    'CARD',
    'LATE', '地铁延误',
    1, '技术部办公区',
    36.3, 'NORMAL',
    1, 0
);

-- 示例3：正常签退记录（指纹）
INSERT INTO `t_attendance_record` (
    `record_code`, `user_id`, `user_name`, `employee_code`, `department_id`, `department_name`,
    `attendance_date`, `attendance_time`, `attendance_type`,
    `shift_id`, `shift_name`, `shift_start_time`, `shift_end_time`, `scheduled_time`,
    `attendance_status`, `time_difference`, `is_abnormal`,
    `device_id`, `device_code`, `device_name`, `device_location`,
    `attendance_method`, `recognition_score`,
    `work_hours`, `effective_hours`,
    `create_user_id`, `deleted_flag`
) VALUES (
    'ATT_20250108_0003', 1, '张三', 'EMP001', 1, '技术部',
    '2025-01-08', '2025-01-08 17:35:00', 'SIGN_OUT',
    1, '标准工作日班次', '08:30:00', '17:30:00', '2025-01-08 17:30:00',
    'NORMAL', 5, 0,
    1, 'ATT_DEVICE_001', '技术部考勤机', '1号楼1层大厅',
    'FINGERPRINT', 99.20,
    9.12, 9.12,
    1, 0
);

-- 示例4：早退记录
INSERT INTO `t_attendance_record` (
    `record_code`, `user_id`, `user_name`, `employee_code`, `department_id`, `department_name`,
    `attendance_date`, `attendance_time`, `attendance_type`,
    `shift_id`, `shift_name`, `shift_start_time`, `shift_end_time`, `scheduled_time`,
    `attendance_status`, `time_difference`, `is_abnormal`,
    `device_id`, `device_code`, `device_name`, `device_location`,
    `attendance_method`,
    `exception_type`, `exception_reason`,
    `work_hours`, `effective_hours`,
    `create_user_id`, `deleted_flag`
) VALUES (
    'ATT_20250108_0004', 3, '王五', 'EMP003', 2, '市场部',
    '2025-01-08', '2025-01-08 16:00:00', 'SIGN_OUT',
    1, '标准工作日班次', '08:30:00', '17:30:00', '2025-01-08 17:30:00',
    'EARLY_LEAVE', -90, 1,
    2, 'ATT_DEVICE_002', '市场部考勤机', '2号楼2层',
    'FACE',
    'EARLY_LEAVE', '身体不适提前下班',
    7.50, 7.00,
    1, 0
);

-- 示例5：补卡记录
INSERT INTO `t_attendance_record` (
    `record_code`, `user_id`, `user_name`, `employee_code`, `department_id`, `department_name`,
    `attendance_date`, `attendance_time`, `attendance_type`,
    `shift_id`, `shift_name`, `shift_start_time`, `shift_end_time`, `scheduled_time`,
    `attendance_status`, `time_difference`, `is_abnormal`,
    `device_id`, `device_code`, `device_name`, `device_location`,
    `attendance_method`,
    `is_makeup`, `makeup_apply_id`, `makeup_reason`,
    `makeup_approver_id`, `makeup_approver_name`, `makeup_approve_time`,
    `create_user_id`, `deleted_flag`
) VALUES (
    'ATT_20250108_0005', 4, '赵六', 'EMP004', 1, '技术部',
    '2025-01-08', '2025-01-08 08:30:00', 'SIGN_IN',
    1, '标准工作日班次', '08:30:00', '17:30:00', '2025-01-08 08:30:00',
    'MAKEUP', 0, 1,
    NULL, NULL, NULL, NULL,
    'MANUAL',
    1, 1, '忘记打卡，现申请补卡',
    10, '部门经理', '2025-01-08 10:00:00',
    1, 0
);

-- 示例6：移动打卡记录（手机APP）
INSERT INTO `t_attendance_record` (
    `record_code`, `user_id`, `user_name`, `employee_code`, `department_id`, `department_name`,
    `attendance_date`, `attendance_time`, `attendance_type`,
    `shift_id`, `shift_name`, `shift_start_time`, `shift_end_time`, `scheduled_time`,
    `attendance_status`, `time_difference`, `is_abnormal`,
    `attendance_method`,
    `gps_longitude`, `gps_latitude`, `gps_address`, `gps_distance`, `ip_address`,
    `photo_url`,
    `create_user_id`, `deleted_flag`
) VALUES (
    'ATT_20250108_0006', 5, '孙七', 'EMP005', 3, '销售部',
    '2025-01-08', '2025-01-08 08:25:00', 'SIGN_IN',
    1, '标准工作日班次', '08:30:00', '17:30:00', '2025-01-08 08:30:00',
    'NORMAL', -5, 0,
    'MOBILE',
    116.397428, 39.909200, '北京市朝阳区建国路88号', 50, '192.168.1.100',
    '/uploads/attendance/mobile/20250108/082500_005.jpg',
    1, 0
);

-- 示例7：加班打卡记录
INSERT INTO `t_attendance_record` (
    `record_code`, `user_id`, `user_name`, `employee_code`, `department_id`, `department_name`,
    `attendance_date`, `attendance_time`, `attendance_type`,
    `attendance_status`, `time_difference`, `is_abnormal`,
    `device_id`, `device_code`, `device_name`, `device_location`,
    `attendance_method`,
    `is_overtime`, `overtime_apply_id`, `overtime_type`, `overtime_hours`,
    `create_user_id`, `deleted_flag`
) VALUES (
    'ATT_20250108_0007', 1, '张三', 'EMP001', 1, '技术部',
    '2025-01-08', '2025-01-08 20:30:00', 'OVERTIME_OUT',
    'OVERTIME', 0, 0,
    1, 'ATT_DEVICE_001', '技术部考勤机', '1号楼1层大厅',
    'FACE',
    1, 1, 'WEEKDAY', 3.00,
    1, 0
);

-- ====================================================
-- 使用示例
-- ====================================================

-- 查询1：查询某用户某月的考勤记录
-- SELECT * FROM t_attendance_record
-- WHERE user_id = 1
--   AND attendance_date BETWEEN '2025-01-01' AND '2025-01-31'
--   AND deleted_flag = 0
-- ORDER BY attendance_time DESC;

-- 查询2：查询部门异常考勤（迟到、早退、缺卡）
-- SELECT user_name, attendance_date, attendance_time, attendance_status, time_difference
-- FROM t_attendance_record
-- WHERE department_id = 1
--   AND attendance_date = '2025-01-08'
--   AND is_abnormal = 1
--   AND deleted_flag = 0
-- ORDER BY time_difference DESC;

-- 查询3：统计用户当月出勤天数
-- SELECT user_id, user_name, COUNT(DISTINCT attendance_date) AS attendance_days
-- FROM t_attendance_record
-- WHERE user_id = 1
--   AND attendance_date BETWEEN '2025-01-01' AND '2025-01-31'
--   AND attendance_type = 'SIGN_IN'
--   AND attendance_status IN ('NORMAL', 'LATE')
--   AND deleted_flag = 0
-- GROUP BY user_id, user_name;

-- 查询4：查询设备使用统计（每天打卡次数）
-- SELECT device_code, device_name, DATE(attendance_time) AS date, COUNT(*) AS count
-- FROM t_attendance_record
-- WHERE device_id = 1
--   AND attendance_time BETWEEN '2025-01-01 00:00:00' AND '2025-01-31 23:59:59'
--   AND deleted_flag = 0
-- GROUP BY device_id, device_code, device_name, DATE(attendance_time)
-- ORDER BY date DESC;

-- 查询5：查询需要补卡的记录
-- SELECT * FROM t_attendance_record
-- WHERE is_makeup = 1
--   AND attendance_date = '2025-01-08'
--   AND deleted_flag = 0
-- ORDER BY attendance_time;

-- 查询6：计算用户当月工作时长
-- SELECT user_id, user_name, SUM(work_hours) AS total_work_hours, SUM(effective_hours) AS total_effective_hours
-- FROM t_attendance_record
-- WHERE user_id = 1
--   AND attendance_date BETWEEN '2025-01-01' AND '2025-01-31'
--   AND attendance_type = 'SIGN_OUT'
--   AND deleted_flag = 0
-- GROUP BY user_id, user_name;

-- 查询7：查询加班记录
-- SELECT * FROM t_attendance_record
-- WHERE is_overtime = 1
--   AND attendance_date BETWEEN '2025-01-01' AND '2025-01-31'
--   AND deleted_flag = 0
-- ORDER BY attendance_time DESC;

-- ====================================================
-- 维护建议
-- ====================================================
-- 1. 定期归档历史数据（建议每季度归档3个月前的数据）
-- 2. 定期清理已删除数据（deleted_flag = 1）
-- 3. 监控表数据量，及时进行分表处理
-- 4. 定期分析索引使用情况，优化慢查询
-- 5. 定期备份考勤数据，确保数据安全
-- 6. 建立考勤数据质量检查机制（如：漏打卡、重复打卡检测）

-- ====================================================
-- 定时任务建议
-- ====================================================
-- 1. 每日凌晨：统计前一天考勤数据，生成考勤日报
-- 2. 每周一凌晨：统计上周考勤数据，生成考勤周报
-- 3. 每月1日凌晨：统计上月考勤数据，生成考勤月报
-- 4. 每日8:00：检测未打卡人员，发送提醒通知
-- 5. 每日18:00：检测未签退人员，发送提醒通知
-- 6. 每季度第一天：归档历史考勤数据

-- ====================================================
-- 文件结束
-- ====================================================
