-- =====================================================
-- IOE-DREAM 业务数据库表结构脚本 (修复版本)
-- 版本: 1.0.1-FIX
-- 说明: 修复与实体类一致性的业务数据库表结构
-- =====================================================

-- 设置字符集
SET NAMES utf8mb4;

-- =====================================================
-- 1. 门禁管理数据库 (ioedream_access_db)
-- =====================================================

USE ioedream_access_db;

-- 1.1 通行记录表 (匹配实体类)
DROP TABLE IF EXISTS t_access_record;
CREATE TABLE t_access_record (
    record_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    user_id BIGINT COMMENT '用户ID',
    card_no VARCHAR(50) COMMENT '卡号',
    device_id BIGINT NOT NULL COMMENT '设备ID',
    area_id BIGINT COMMENT '区域ID',
    access_type VARCHAR(20) NOT NULL COMMENT '通行类型 IN-进入 OUT-离开',
    verify_mode VARCHAR(20) COMMENT '验证方式 FACE-人脸 CARD-刷卡 FINGERPRINT-指纹 PASSWORD-密码',
    verify_result TINYINT DEFAULT 1 COMMENT '验证结果 1-成功 0-失败',
    fail_reason VARCHAR(100) COMMENT '失败原因',
    access_time DATETIME NOT NULL COMMENT '通行时间',
    photo_url VARCHAR(500) COMMENT '照片URL',
    temperature DECIMAL(3,1) COMMENT '体温(摄氏度)',
    mask_detect TINYINT DEFAULT 1 COMMENT '口罩检测 1-佩戴 0-未佩戴',

    -- 审计字段 (继承自BaseEntity)
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',

    INDEX idx_user_id (user_id),
    INDEX idx_device_id (device_id),
    INDEX idx_area_id (area_id),
    INDEX idx_access_time (access_time),
    INDEX idx_access_type (access_type),
    INDEX idx_verify_result (verify_result),
    INDEX idx_card_no (card_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通行记录表';

-- =====================================================
-- 2. 考勤管理数据库 (ioedream_attendance_db)
-- =====================================================

USE ioedream_attendance_db;

-- 2.1 考勤记录表 (匹配实体类)
DROP TABLE IF EXISTS t_attendance_record;
CREATE TABLE t_attendance_record (
    record_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    device_id BIGINT COMMENT '设备ID',
    attendance_type VARCHAR(20) NOT NULL COMMENT '考勤类型 ON-上班 OFF-下班 OVERTIME_IN-加班上班 OVERTIME_OUT-加班下班',
    attendance_time DATETIME NOT NULL COMMENT '考勤时间',
    verify_mode VARCHAR(20) COMMENT '验证方式 FACE-人脸 CARD-刷卡 FINGERPRINT-指纹 PASSWORD-密码',
    photo_url VARCHAR(500) COMMENT '照片URL',
    location VARCHAR(200) COMMENT '考勤地点',
    is_abnormal TINYINT DEFAULT 0 COMMENT '是否异常 0-正常 1-异常',
    abnormal_type VARCHAR(20) COMMENT '异常类型 LATE-迟到 EARLY-早退 ABSENCE-旷工',
    process_status TINYINT DEFAULT 0 COMMENT '处理状态 0-待处理 1-已处理 2-已忽略',
    process_remark VARCHAR(500) COMMENT '处理备注',

    -- 审计字段
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',

    INDEX idx_user_id (user_id),
    INDEX idx_device_id (device_id),
    INDEX idx_attendance_time (attendance_time),
    INDEX idx_attendance_type (attendance_type),
    INDEX idx_is_abnormal (is_abnormal),
    INDEX idx_process_status (process_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤记录表';

-- 2.2 班次调班表 (匹配AttendanceShiftEntity)
DROP TABLE IF EXISTS attendance_shift;
CREATE TABLE attendance_shift (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '调班ID',
    shift_no VARCHAR(50) COMMENT '调班编号',
    employee_id BIGINT COMMENT '员工ID',
    employee_name VARCHAR(100) COMMENT '员工姓名',
    shift_date DATE COMMENT '调班日期',
    original_shift_id BIGINT COMMENT '原班次ID',
    target_shift_id BIGINT COMMENT '目标班次ID',
    reason VARCHAR(500) COMMENT '调班原因',
    status VARCHAR(20) COMMENT '状态',
    approval_comment VARCHAR(500) COMMENT '审批意见',
    approval_time DATETIME COMMENT '审批时间',
    remark VARCHAR(1000) COMMENT '备注',
    workflow_instance_id BIGINT COMMENT '工作流实例ID',

    -- 审计字段
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',

    INDEX idx_employee_id (employee_id),
    INDEX idx_shift_date (shift_date),
    INDEX idx_status (status),
    INDEX idx_original_shift (original_shift_id),
    INDEX idx_target_shift (target_shift_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤调班表';

-- =====================================================
-- 3. 消费管理数据库 (ioedream_consume_db)
-- =====================================================

USE ioedream_consume_db;

-- 3.1 消费账户表 (修复AccountEntity字段问题)
DROP TABLE IF EXISTS t_consume_account;
CREATE TABLE t_consume_account (
    account_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '账户ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    account_no VARCHAR(50) NOT NULL UNIQUE COMMENT '账户编号',
    account_name VARCHAR(100) NOT NULL COMMENT '账户名称',
    account_type TINYINT DEFAULT 1 COMMENT '账户类型 1-个人账户 2-团体账户 3-临时账户',

    -- 金额字段统一使用DECIMAL(12,2)
    balance DECIMAL(12,2) DEFAULT 0.00 COMMENT '账户余额',
    frozen_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '冻结金额',
    credit_limit DECIMAL(12,2) DEFAULT 0.00 COMMENT '信用额度',
    daily_limit DECIMAL(12,2) DEFAULT 999999.99 COMMENT '日消费限额',
    monthly_limit DECIMAL(12,2) DEFAULT 999999.99 COMMENT '月消费限额',
    subsidy_balance DECIMAL(12,2) DEFAULT 0.00 COMMENT '补贴余额',
    total_recharge_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '累计充值金额',
    total_consume_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '累计消费金额',
    total_subsidy_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '累计补贴金额',

    status TINYINT DEFAULT 1 COMMENT '状态 1-正常 2-冻结 3-注销',
    last_use_time DATETIME COMMENT '最后使用时间',

    -- 审计字段
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',

    INDEX idx_user_id (user_id),
    INDEX idx_account_no (account_no),
    INDEX idx_account_type (account_type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消费账户表';

-- 3.2 消费记录表 (匹配ConsumeRecordEntity)
DROP TABLE IF EXISTS t_consume_record;
CREATE TABLE t_consume_record (
    record_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '订单号',
    account_id BIGINT NOT NULL COMMENT '账户ID',
    user_id BIGINT COMMENT '用户ID',
    device_id BIGINT COMMENT '设备ID',
    area_id BIGINT COMMENT '区域ID',
    consume_type VARCHAR(20) NOT NULL COMMENT '消费类型 MEAL-餐饮 SHOP-购物 OTHER-其他',
    amount DECIMAL(12,2) NOT NULL COMMENT '消费金额',
    discount_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '折扣金额',
    subsidy_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '补贴金额',
    actual_amount DECIMAL(12,2) NOT NULL COMMENT '实际支付金额',
    payment_method VARCHAR(20) NOT NULL COMMENT '支付方式 BALANCE-余额 CARD-银行卡 WECHAT-微信 ALIPAY-支付宝',
    consume_time DATETIME NOT NULL COMMENT '消费时间',
    merchant_name VARCHAR(100) COMMENT '商户名称',
    goods_info JSON COMMENT '商品信息',
    remark VARCHAR(500) COMMENT '备注',
    refund_status TINYINT DEFAULT 0 COMMENT '退款状态 0-未退款 1-部分退款 2-全额退款',
    refund_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '退款金额',

    -- 审计字段
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',

    INDEX idx_order_no (order_no),
    INDEX idx_account_id (account_id),
    INDEX idx_user_id (user_id),
    INDEX idx_device_id (device_id),
    INDEX idx_consume_time (consume_time),
    INDEX idx_consume_type (consume_type),
    INDEX idx_payment_method (payment_method),
    INDEX idx_refund_status (refund_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消费记录表';

-- =====================================================
-- 4. 访客管理数据库 (ioedream_visitor_db)
-- =====================================================

USE ioedream_visitor_db;

-- 4.1 访客记录表 (匹配VisitorRecordEntity)
DROP TABLE IF EXISTS t_visitor_record;
CREATE TABLE t_visitor_record (
    record_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    appointment_id BIGINT COMMENT '预约ID',
    visitor_name VARCHAR(100) NOT NULL COMMENT '访客姓名',
    visitor_phone VARCHAR(20) COMMENT '访客手机号',
    visitor_company VARCHAR(200) COMMENT '访客单位',
    visitor_id_card VARCHAR(20) COMMENT '访客身份证号',
    host_user_id BIGINT COMMENT '接待人ID',
    host_user_name VARCHAR(100) COMMENT '接待人姓名',
    visit_area_id BIGINT COMMENT '访问区域ID',
    visit_area_name VARCHAR(100) COMMENT '访问区域名称',
    visit_type VARCHAR(20) DEFAULT 'APPOINTMENT' COMMENT '访问类型 APPOINTMENT-预约 TEMPORARY-临时',
    arrive_time DATETIME COMMENT '到达时间',
    leave_time DATETIME COMMENT '离开时间',
    access_device_id BIGINT COMMENT '通行设备ID',
    verify_mode VARCHAR(20) COMMENT '验证方式 FACE-人脸 ID_CARD-身份证 QR_CODE-二维码',
    photo_url VARCHAR(500) COMMENT '访客照片',
    id_card_image_url VARCHAR(500) COMMENT '身份证照片URL',
    remark VARCHAR(500) COMMENT '备注',

    -- 审计字段
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',

    INDEX idx_appointment_id (appointment_id),
    INDEX idx_visitor_phone (visitor_phone),
    INDEX idx_host_user_id (host_user_id),
    INDEX idx_visit_area_id (visit_area_id),
    INDEX idx_arrive_time (arrive_time),
    INDEX idx_visit_type (visit_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='访客记录表';

SELECT '业务数据库表结构修复完成' as message,
       COUNT(*) as table_count
FROM (
    SELECT TABLE_SCHEMA as db_name, COUNT(*) as table_count
    FROM INFORMATION_SCHEMA.TABLES
    WHERE TABLE_SCHEMA IN ('ioedream_access_db', 'ioedream_attendance_db', 'ioedream_consume_db', 'ioedream_visitor_db', 'ioedream_video_db', 'ioedream_device_db')
      AND TABLE_TYPE = 'BASE TABLE'
    GROUP BY TABLE_SCHEMA
) as summary;