-- =====================================================
-- IOE-DREAM 数据库迁移脚本
-- 版本: V2.1.5
-- 描述: 批量补齐缺口表（第3批：访客/消费/门禁/考勤/工作流/审批）
-- =====================================================

USE ioedream;

-- =====================================================
-- 1. t_visitor_area（访客区域表）
-- =====================================================
CREATE TABLE IF NOT EXISTS t_visitor_area (
    area_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '区域ID',
    area_name VARCHAR(100) NOT NULL COMMENT '区域名称',
    area_code VARCHAR(50) COMMENT '区域编码',
    parent_id BIGINT DEFAULT 0 COMMENT '父区域ID',
    area_type VARCHAR(50) COMMENT '区域类型',
    status TINYINT DEFAULT 1 COMMENT '状态',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    KEY idx_parent_id (parent_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='访客区域表';

-- =====================================================
-- 2. t_visitor_driver（访客司机表）
-- =====================================================
CREATE TABLE IF NOT EXISTS t_visitor_driver (
    driver_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '司机ID',
    driver_name VARCHAR(100) NOT NULL COMMENT '司机姓名',
    phone VARCHAR(20) COMMENT '手机号',
    id_card VARCHAR(20) COMMENT '身份证号',
    license_no VARCHAR(50) COMMENT '驾驶证号',
    status TINYINT DEFAULT 1 COMMENT '状态',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    KEY idx_phone (phone),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='访客司机表';

-- =====================================================
-- 3. t_visitor_vehicle（访客车辆表）
-- =====================================================
CREATE TABLE IF NOT EXISTS t_visitor_vehicle (
    vehicle_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '车辆ID',
    plate_no VARCHAR(20) NOT NULL COMMENT '车牌号',
    vehicle_type VARCHAR(50) COMMENT '车辆类型',
    vehicle_color VARCHAR(20) COMMENT '车辆颜色',
    driver_id BIGINT COMMENT '司机ID',
    status TINYINT DEFAULT 1 COMMENT '状态',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    KEY idx_plate_no (plate_no),
    KEY idx_driver_id (driver_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='访客车辆表';

-- =====================================================
-- 4. t_visitor_electronic_pass（访客电子通行证表）
-- =====================================================
CREATE TABLE IF NOT EXISTS t_visitor_electronic_pass (
    pass_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '通行证ID',
    pass_no VARCHAR(50) NOT NULL COMMENT '通行证编号',
    visitor_id BIGINT NOT NULL COMMENT '访客ID',
    appointment_id BIGINT COMMENT '预约ID',
    qr_code VARCHAR(500) COMMENT '二维码',
    valid_start DATETIME COMMENT '有效开始时间',
    valid_end DATETIME COMMENT '有效结束时间',
    status TINYINT DEFAULT 1 COMMENT '状态：1-有效 2-已使用 3-已过期 4-已作废',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    UNIQUE KEY uk_pass_no (pass_no, deleted_flag),
    KEY idx_visitor_id (visitor_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='访客电子通行证表';

-- =====================================================
-- 5. t_access_area_ext（门禁区域扩展表）
-- =====================================================
CREATE TABLE IF NOT EXISTS t_access_area_ext (
    ext_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '扩展ID',
    area_id BIGINT NOT NULL COMMENT '区域ID',
    access_level TINYINT DEFAULT 1 COMMENT '门禁等级',
    require_face TINYINT DEFAULT 0 COMMENT '是否需要人脸：0-否 1-是',
    require_card TINYINT DEFAULT 1 COMMENT '是否需要刷卡：0-否 1-是',
    require_password TINYINT DEFAULT 0 COMMENT '是否需要密码：0-否 1-是',
    time_rule_id BIGINT COMMENT '时间规则ID',
    config_json TEXT COMMENT '配置JSON',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    UNIQUE KEY uk_area_id (area_id, deleted_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门禁区域扩展表';

-- =====================================================
-- 6. t_consume_area_ext（消费区域扩展表）
-- =====================================================
CREATE TABLE IF NOT EXISTS t_consume_area_ext (
    ext_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '扩展ID',
    area_id BIGINT NOT NULL COMMENT '区域ID',
    consume_type VARCHAR(50) COMMENT '消费类型',
    price_rule_id BIGINT COMMENT '价格规则ID',
    subsidy_rule_id BIGINT COMMENT '补贴规则ID',
    config_json TEXT COMMENT '配置JSON',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    UNIQUE KEY uk_area_id (area_id, deleted_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消费区域扩展表';

-- =====================================================
-- 7. t_consume_payment_record（消费支付记录表）
-- =====================================================
CREATE TABLE IF NOT EXISTS t_consume_payment_record (
    record_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    order_no VARCHAR(50) NOT NULL COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    amount DECIMAL(10,2) NOT NULL COMMENT '金额',
    payment_method VARCHAR(50) COMMENT '支付方式',
    payment_status TINYINT DEFAULT 0 COMMENT '支付状态：0-待支付 1-已支付 2-已退款',
    payment_time DATETIME COMMENT '支付时间',
    trade_no VARCHAR(100) COMMENT '交易号',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    UNIQUE KEY uk_order_no (order_no, deleted_flag),
    KEY idx_user_id (user_id),
    KEY idx_payment_status (payment_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消费支付记录表';

-- =====================================================
-- 8. t_consume_payment_refund_record（消费退款记录表）
-- =====================================================
CREATE TABLE IF NOT EXISTS t_consume_payment_refund_record (
    refund_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '退款ID',
    payment_record_id BIGINT NOT NULL COMMENT '支付记录ID',
    refund_no VARCHAR(50) NOT NULL COMMENT '退款单号',
    refund_amount DECIMAL(10,2) NOT NULL COMMENT '退款金额',
    refund_reason VARCHAR(500) COMMENT '退款原因',
    refund_status TINYINT DEFAULT 0 COMMENT '退款状态：0-待处理 1-已退款 2-退款失败',
    refund_time DATETIME COMMENT '退款时间',
    operator_id BIGINT COMMENT '操作人ID',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    UNIQUE KEY uk_refund_no (refund_no, deleted_flag),
    KEY idx_payment_record_id (payment_record_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消费退款记录表';

-- =====================================================
-- 9. t_consume_qrcode（消费二维码表）
-- =====================================================
CREATE TABLE IF NOT EXISTS t_consume_qrcode (
    qrcode_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '二维码ID',
    qrcode_no VARCHAR(50) NOT NULL COMMENT '二维码编号',
    qrcode_type VARCHAR(50) COMMENT '二维码类型',
    qrcode_content VARCHAR(500) COMMENT '二维码内容',
    user_id BIGINT COMMENT '用户ID',
    valid_start DATETIME COMMENT '有效开始时间',
    valid_end DATETIME COMMENT '有效结束时间',
    status TINYINT DEFAULT 1 COMMENT '状态',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    UNIQUE KEY uk_qrcode_no (qrcode_no, deleted_flag),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消费二维码表';

-- =====================================================
-- 10. t_consume_report_template（消费报表模板表）
-- =====================================================
CREATE TABLE IF NOT EXISTS t_consume_report_template (
    template_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '模板ID',
    template_code VARCHAR(50) NOT NULL COMMENT '模板编码',
    template_name VARCHAR(100) NOT NULL COMMENT '模板名称',
    template_type VARCHAR(50) COMMENT '模板类型',
    template_config TEXT COMMENT '模板配置JSON',
    status TINYINT DEFAULT 1 COMMENT '状态',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    UNIQUE KEY uk_template_code (template_code, deleted_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消费报表模板表';

-- =====================================================
-- 11. t_common_area_person（区域人员关联表）
-- =====================================================
CREATE TABLE IF NOT EXISTS t_common_area_person (
    relation_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '关联ID',
    area_id BIGINT NOT NULL COMMENT '区域ID',
    person_id BIGINT NOT NULL COMMENT '人员ID',
    person_type VARCHAR(50) COMMENT '人员类型',
    permission_type VARCHAR(50) COMMENT '权限类型',
    valid_start DATETIME COMMENT '有效开始时间',
    valid_end DATETIME COMMENT '有效结束时间',
    status TINYINT DEFAULT 1 COMMENT '状态',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    UNIQUE KEY uk_area_person (area_id, person_id, deleted_flag),
    KEY idx_person_id (person_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='区域人员关联表';

-- =====================================================
-- 12. t_common_audit_archive（审计归档表）
-- =====================================================
CREATE TABLE IF NOT EXISTS t_common_audit_archive (
    archive_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '归档ID',
    archive_date DATE NOT NULL COMMENT '归档日期',
    archive_type VARCHAR(50) COMMENT '归档类型',
    record_count BIGINT DEFAULT 0 COMMENT '记录数',
    file_path VARCHAR(500) COMMENT '归档文件路径',
    file_size BIGINT COMMENT '文件大小',
    status TINYINT DEFAULT 1 COMMENT '状态：1-成功 2-失败',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_archive_date (archive_date),
    KEY idx_archive_type (archive_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审计归档表';

-- =====================================================
-- 13. t_config_change_approval（配置变更审批表）
-- =====================================================
CREATE TABLE IF NOT EXISTS t_config_change_approval (
    approval_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '审批ID',
    change_id BIGINT NOT NULL COMMENT '变更ID',
    approver_id BIGINT NOT NULL COMMENT '审批人ID',
    approval_status TINYINT DEFAULT 0 COMMENT '审批状态：0-待审批 1-通过 2-拒绝',
    approval_time DATETIME COMMENT '审批时间',
    approval_remark VARCHAR(500) COMMENT '审批备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_change_id (change_id),
    KEY idx_approver_id (approver_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='配置变更审批表';

-- =====================================================
-- 14. t_config_change_rollback（配置变更回滚表）
-- =====================================================
CREATE TABLE IF NOT EXISTS t_config_change_rollback (
    rollback_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '回滚ID',
    change_id BIGINT NOT NULL COMMENT '变更ID',
    rollback_reason VARCHAR(500) COMMENT '回滚原因',
    rollback_status TINYINT DEFAULT 0 COMMENT '回滚状态：0-待回滚 1-已回滚 2-回滚失败',
    rollback_time DATETIME COMMENT '回滚时间',
    operator_id BIGINT COMMENT '操作人ID',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_change_id (change_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='配置变更回滚表';

SELECT 'V2.1.5 批量补齐第3批表完成' AS status;
