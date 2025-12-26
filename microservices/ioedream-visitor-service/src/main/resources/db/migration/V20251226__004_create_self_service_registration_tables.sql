-- ============================================================================
-- IOE-DREAM 访客服务自助登记表
-- Task 11: 自助登记管理
-- Version: V20251226__004
-- Author: IOE-DREAM Team
-- Date: 2025-12-26
-- ============================================================================

-- 创建自助访客登记表
CREATE TABLE IF NOT EXISTS `t_visitor_self_service_registration` (
    `registration_id`        BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '登记ID',
    `registration_code`      VARCHAR(30)     NOT NULL                 COMMENT '登记码',
    `visitor_code`           VARCHAR(30)     NOT NULL                 COMMENT '访客码',
    `visitor_name`           VARCHAR(50)     NOT NULL                 COMMENT '访客姓名',
    `id_card_type`           TINYINT         NOT NULL                 COMMENT '证件类型: 1-身份证 2-护照 3-其他',
    `id_card`                VARCHAR(50)     NOT NULL                 COMMENT '证件号码',
    `phone`                  VARCHAR(20)     NOT NULL                 COMMENT '手机号码',
    `visitor_type`           TINYINT         NOT NULL                 COMMENT '访客类型: 1-临时访客 2-常客 3-VIP访客',
    `visit_purpose`          VARCHAR(200)    NOT NULL                 COMMENT '来访目的',
    `interviewee_id`         BIGINT          NOT NULL                 COMMENT '被访人ID',
    `interviewee_name`       VARCHAR(50)     NOT NULL                 COMMENT '被访人姓名',
    `interviewee_department` VARCHAR(100)                             COMMENT '被访人部门',
    `visit_date`             DATE            NOT NULL                 COMMENT '访问日期',
    `expected_enter_time`    DATETIME        NOT NULL                 COMMENT '预计进入时间',
    `expected_leave_time`    DATETIME        NOT NULL                 COMMENT '预计离开时间',
    `face_photo_url`         VARCHAR(500)                             COMMENT '人脸照片URL',
    `terminal_id`            VARCHAR(50)     NOT NULL                 COMMENT '终端ID',
    `terminal_location`      VARCHAR(100)                             COMMENT '终端位置',
    `registration_status`    TINYINT         NOT NULL DEFAULT 0      COMMENT '登记状态: 0-待审批 1-审批通过 2-审批拒绝 3-已签到 4-已完成',
    `approver_id`            BIGINT                                      COMMENT '审批人ID',
    `approver_name`          VARCHAR(50)                                 COMMENT '审批人姓名',
    `approval_time`          DATETIME                                     COMMENT '审批时间',
    `approval_comment`       VARCHAR(500)                                COMMENT '审批意见',
    `check_in_time`          DATETIME                                     COMMENT '签到时间',
    `check_in_terminal`      VARCHAR(50)                                 COMMENT '签到终端',
    `check_out_time`         DATETIME                                     COMMENT '签离时间',
    `remark`                 VARCHAR(500)                                COMMENT '备注',
    `create_time`            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag`           TINYINT         NOT NULL DEFAULT 0      COMMENT '删除标记: 0-未删除 1-已删除',
    PRIMARY KEY (`registration_id`),
    UNIQUE KEY `uk_registration_code` (`registration_code`),
    UNIQUE KEY `uk_visitor_code` (`visitor_code`),
    INDEX `idx_registration_visitor` (`visitor_name`, `phone`),
    INDEX `idx_registration_interviewee` (`interviewee_id`),
    INDEX `idx_registration_date` (`visit_date`),
    INDEX `idx_registration_status` (`registration_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='自助访客登记表';
