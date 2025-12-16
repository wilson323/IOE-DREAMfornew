-- =====================================================
-- IOE-DREAM 访客服务增强 Flyway 迁移脚本
-- 版本: V20251216040003
-- 描述: 创建访客服务增强功能表（黑名单、审批记录等）
-- 实现P0级访客管理功能
-- =====================================================

-- 1. 访客黑名单表（支持多维度黑名单策略）
CREATE TABLE IF NOT EXISTS `t_visitor_blacklist` (
    `blacklist_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '黑名单ID',
    `visitor_id` BIGINT COMMENT '访客ID（可关联到t_visitor表）',
    `blacklist_type` TINYINT NOT NULL DEFAULT 1 COMMENT '黑名单类型：1-永久黑名单 2-临时黑名单 3-监控黑名单',
    `blacklist_level` TINYINT NOT NULL DEFAULT 1 COMMENT '黑名单级别：1-普通 2-重要 3-紧急',
    `blacklist_reason` VARCHAR(500) NOT NULL COMMENT '黑名单原因',
    `id_card` VARCHAR(18) DEFAULT NULL COMMENT '身份证号（索引字段）',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号（索引字段）',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱地址',
    `face_features` TEXT COMMENT '人脸特征数据（Base64编码）',
    `device_fingerprint` TEXT COMMENT '设备指纹信息',
    `risk_score` INT DEFAULT 50 COMMENT '风险评分（0-100）',
    `effective_time` DATETIME DEFAULT NULL COMMENT '生效时间',
    `expire_time` DATETIME DEFAULT NULL COMMENT '过期时间（NULL表示永久）',
    `blacklist_status` TINYINT NOT NULL DEFAULT 1 COMMENT '黑名单状态：1-有效 2-暂停 3-已解除 4-待审核',
    `verification_count` INT DEFAULT 0 COMMENT '违规次数',
    `last_violation_time` DATETIME DEFAULT NULL COMMENT '最后违规时间',
    `auto_clear_time` INT DEFAULT NULL COMMENT '自动清除时间（小时）',
    `notify_enabled` TINYINT DEFAULT 1 COMMENT '是否启用通知：0-否 1-是',
    `source` VARCHAR(50) DEFAULT 'MANUAL' COMMENT '来源：MANUAL-手动 SYSTEM-系统 AUTO-自动 AI-人工智能',
    `operator_id` BIGINT COMMENT '操作员ID',
    `operator_name` VARCHAR(100) DEFAULT NULL COMMENT '操作员姓名',
    `approval_required` TINYINT DEFAULT 0 COMMENT '是否需要审批：0-否 1-是',
    `approval_status` TINYINT DEFAULT 0 COMMENT '审批状态：0-待审批 1-已通过 2-已拒绝',
    `approver_id` BIGINT COMMENT '审批人ID',
    `approval_time` DATETIME DEFAULT NULL COMMENT '审批时间',
    `approval_remark` VARCHAR(500) DEFAULT NULL COMMENT '审批备注',
    `related_incidents` TEXT COMMENT '相关违规事件（JSON格式）',
    `block_devices` TEXT COMMENT '拦截设备列表（JSON格式）',
    `whitelist_devices` TEXT COMMENT '白名单设备（JSON格式）',
    `geographic_restrictions` TEXT COMMENT '地域限制（JSON格式）',
    `time_restrictions` TEXT COMMENT '时间限制（JSON格式）',
    `custom_attributes` TEXT COMMENT '自定义属性（JSON格式）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT DEFAULT 0 COMMENT '删除标识：0-未删除 1-已删除',
    PRIMARY KEY (`blacklist_id`),
    UNIQUE KEY `uk_visitor_card` (`visitor_id`, `id_card`, `deleted_flag`),
    UNIQUE KEY `uk_visitor_phone` (`visitor_id`, `phone`, `deleted_flag`),
    KEY `idx_blacklist_type` (`blacklist_type`, `blacklist_status`, `deleted_flag`),
    KEY `idx_risk_score` (`risk_score`, `deleted_flag`),
    KEY `idx_effective_expire` (`effective_time`, `expire_time`, `deleted_flag`),
    KEY `idx_source_status` (`source`, `approval_status`, `deleted_flag`),
    KEY `idx_operator` (`operator_id`, `deleted_flag`),
    KEY `idx_create_time` (`create_time`, `deleted_flag`),
    KEY `idx_violation_time` (`last_violation_time`, `deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='访客黑名单表';

-- 2. 访客审批记录表（支持多级审批流程）
CREATE TABLE IF NOT EXISTS `t_visitor_approval_record` (
    `approval_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '审批记录ID',
    `appointment_id` BIGINT NOT NULL COMMENT '预约ID（外键关联）',
    `visitor_id` BIGINT NOT NULL COMMENT '访客ID（外键关联）',
    `approval_level` TINYINT NOT NULL DEFAULT 1 COMMENT '审批级别：1-一级审批 2-二级审批 3-三级审批',
    `approval_step` TINYINT NOT NULL DEFAULT 1 COMMENT '审批步骤：第几步审批',
    `total_steps` TINYINT NOT NULL DEFAULT 1 COMMENT '总审批步骤数',
    `approval_role` VARCHAR(50) NOT NULL COMMENT '审批角色：RECEPTION-前台 SECURITY-安保 ADMIN-管理员 MANAGER-经理',
    `approver_id` BIGINT NOT NULL COMMENT '审批人ID',
    `approver_name` VARCHAR(100) NOT NULL COMMENT '审批人姓名',
    `department_id` BIGINT COMMENT '审批部门ID',
    `approval_decision` TINYINT NOT NULL COMMENT '审批决定：1-通过 2-拒绝 3-转交 4-待补充',
    `approval_result` TINYINT DEFAULT 0 COMMENT '审批结果：0-待定 1-成功 2-失败',
    `approval_time` DATETIME DEFAULT NULL COMMENT '审批时间',
    `processing_duration` INT DEFAULT NULL COMMENT '处理耗时（秒）',
    `approval_comment` TEXT COMMENT '审批意见',
    `rejection_reason` VARCHAR(500) DEFAULT NULL COMMENT '拒绝原因',
    `required_conditions` TEXT COMMENT '必要条件（JSON格式）',
    `checklist_items` TEXT COMMENT '检查清单项目（JSON格式）',
    `attachment_urls` TEXT COMMENT '附件URL（JSON格式）',
    `risk_assessment` TEXT COMMENT '风险评估（JSON格式）',
    `priority_level` TINYINT DEFAULT 1 COMMENT '优先级：1-普通 2-重要 3-紧急',
    `auto_approval` TINYINT DEFAULT 0 COMMENT '是否自动审批：0-否 1-是',
    `auto_approval_rules` TEXT COMMENT '自动审批规则（JSON格式）',
    `timeout_minutes` INT DEFAULT 720 COMMENT '超时时间（分钟）',
    `reminder_sent` TINYINT DEFAULT 0 COMMENT '是否已发送提醒：0-否 1-是',
    `next_approver_id` BIGINT DEFAULT NULL COMMENT '下一级审批人ID',
    `parallel_approval` TINYINT DEFAULT 0 COMMENT '是否并行审批：0-否 1-是',
    `approval_flow` VARCHAR(100) DEFAULT 'STANDARD' COMMENT '审批流程：STANDARD-标准 EMERGENCY-紧急 SIMPLIFIED-简化',
    `device_verification` TINYINT DEFAULT 0 COMMENT '是否设备验证：0-否 1-是',
    `biometric_verification` TINYINT DEFAULT 0 COMMENT '是否生物识别：0-否 1-是',
    `background_check` TINYINT DEFAULT 0 COMMENT '是否背景调查：0-否 1-是',
    `special_permissions` TEXT COMMENT '特殊权限（JSON格式）',
    `access_granted_areas` TEXT COMMENT '授权访问区域（JSON格式）',
    `access_duration` INT DEFAULT NULL COMMENT '访问时长（分钟）',
    `temporary_credentials` TEXT COMMENT '临时凭证信息（JSON格式）',
    `monitoring_required` TYNCHEINT DEFAULT 0 COMMENT '是否需要监控：0-否 1-是',
    `escalation_rules` TEXT COMMENT '升级规则（JSON格式）',
    `custom_workflow_data` TEXT COMMENT '自定义工作流数据（JSON格式）',
    `integration_data` TEXT COMMENT '集成数据（JSON格式）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT DEFAULT 0 COMMENT '删除标识：0-未删除 1-已删除',
    PRIMARY KEY (`approval_id`),
    UNIQUE KEY `uk_appointment_level_step` (`appointment_id`, `approval_level`, `approval_step`, `deleted_flag`),
    KEY `idx_visitor_approval` (`visitor_id`, `approval_decision`, `deleted_flag`),
    KEY `idx_approver_status` (`approver_id`, `approval_result`, `deleted_flag`),
    KEY `idx_approval_time` (`approval_time`, `deleted_flag`),
    KEY `idx_priority_time` (`priority_level`, `approval_time`, `deleted_flag`),
    KEY `idx_appointment` (`appointment_id`, `deleted_flag`),
    KEY `idx_create_time` (`create_time`, `deleted_flag`),
    KEY `idx_result_decision` (`approval_result`, `approval_decision`, `deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='访客审批记录表';

-- 3. 访客预约申请表（扩展原有表结构）
ALTER TABLE `t_visitor_appointment`
ADD COLUMN IF NOT EXISTS `approval_required` TINYINT DEFAULT 0 COMMENT '是否需要审批：0-否 1-是' AFTER `area_ids`,
ADD COLUMN IF NOT EXISTS `approval_workflow_id` BIGINT DEFAULT NULL COMMENT '审批工作流ID' AFTER `approval_required`,
ADD COLUMN IF NOT EXISTS `current_approval_level` TINYINT DEFAULT 0 COMMENT '当前审批级别' AFTER `approval_workflow_id`,
ADD COLUMN IF NOT EXISTS `approval_status` TINYINT DEFAULT 0 COMMENT '审批状态：0-待审批 1-已通过 2-已拒绝 3-进行中' AFTER `current_approval_level`,
ADD COLUMN IF NOT EXISTS `priority_level` TINYINT DEFAULT 1 COMMENT '优先级：1-普通 2-重要 3-紧急' AFTER `approval_status`,
ADD COLUMN IF NOT EXISTS `special_requirements` TEXT COMMENT '特殊要求（JSON格式）' AFTER `priority_level`,
ADD COLUMN IF NOT EXISTS `background_check_required` TINYINT DEFAULT 0 COMMENT '是否需要背景调查：0-否 1-是' AFTER `special_requirements`,
ADD COLUMN IF NOT EXISTS `security_level` TINYINT DEFAULT 1 COMMENT '安全等级：1-普通 2-高级 3-机密' AFTER `background_check_required`,
ADD COLUMN IF NOT EXISTS `access_permissions` TEXT COMMENT '访问权限（JSON格式）' AFTER `security_level`,
ADD COLUMN IF NOT EXISTS `escorts_required` TINYINT DEFAULT 0 COMMENT '是否需要陪同：0-否 1-是' AFTER `access_permissions`,
ADD COLUMN IF NOT EXISTS `escort_info` TEXT COMMENT '陪同人员信息（JSON格式）' AFTER `escorts_required`,
ADD COLUMN IF NOT EXISTS `auto_check_in` TINYINT DEFAULT 0 COMMENT '自动签到：0-否 1-是' AFTER `escort_info`,
ADD COLUMN IF NOT EXISTS `auto_check_out` TINYINT DEFAULT 0 COMMENT '自动签出：0-否 1-是' AFTER `auto_check_in`,
ADD COLUMN IF NOT EXISTS `access_card_issued` TINYINT DEFAULT 0 COMMENT '是否已发访客卡：0-否 1-是' AFTER `auto_check_out`,
ADD COLUMN IF NOT EXISTS `card_activation_time` DATETIME DEFAULT NULL COMMENT '访客卡激活时间' AFTER `access_card_issued`,
ADD COLUMN IF NOT EXISTS `temporary_credentials` TEXT COMMENT '临时凭证（JSON格式）' AFTER `card_activation_time`,
ADD COLUMN IF NOT EXISTS `biometric_enrolled` TINYINT DEFAULT 0 COMMENT '生物识别已注册：0-否 1-是' AFTER `temporary_credentials`,
ADD COLUMN IF NOT EXISTS `face_features` TEXT COMMENT '人脸特征数据（Base64编码）' AFTER `biometric_enrolled`,
ADD COLUMN IF NOT EXISTS `fingerprint_data` TEXT COMMENT '指纹数据（Base64编码）' AFTER `face_features`,
ADD COLUMN IF NOT EXISTS `verification_methods` TEXT COMMENT '验证方式（JSON格式）' AFTER `fingerprint_data`,
ADD COLUMN IF NOT EXISTS `visit_history` TEXT COMMENT '历史访问记录（JSON格式）' AFTER `verification_methods`,
ADD COLUMN IF NOT EXISTS `compliance_check` TINYINT DEFAULT 1 COMMENT '合规检查通过：0-否 1-是' AFTER `visit_history`,
ADD COLUMN IF NOT EXISTS `risk_assessment` TEXT COMMENT '风险评估（JSON格式）' AFTER `compliance_check`,
ADD COLUMN IF NOT EXISTS `emergency_contact` TEXT COMMENT '紧急联系人（JSON格式）' AFTER `risk_assessment`,
ADD COLUMN IF NOT EXISTS `health_status` VARCHAR(50) DEFAULT 'NORMAL' COMMENT '健康状态' AFTER `emergency_contact`,
ADD COLUMN IF NOT EXISTS `health_declaration` TEXT COMMENT '健康声明（JSON格式）' AFTER `health_status`,
ADD COLUMN IF NOT EXISTS `temperature_check` TINYINT DEFAULT 0 COMMENT '体温检测：0-否 1-是' AFTER `health_declaration`,
ADD COLUMN IF NOT EXISTS `temperature_value` DECIMAL(3,1) DEFAULT NULL COMMENT '体温值' AFTER `temperature_check`,
ADD COLUMN IF NOT EXISTS `mask_provided` TINYINT DEFAULT 0 COMMENT '是否提供口罩：0-否 1-是' AFTER `temperature_value`,
ADD COLUMN IF NOT EXISTS `sanitization_required` TINYINT DEFAULT 1 COMMENT '是否需要消毒：0-否 1-是' AFTER `mask_provided`,
ADD COLUMN IF NOT EXISTS `custom_fields` TEXT COMMENT '自定义字段（JSON格式）' AFTER `sanitization_required`;

-- 4. 访客车辆信息表（支持物流管理）
ALTER TABLE `t_visitor_vehicle`
ADD COLUMN IF NOT EXISTS `vehicle_type` VARCHAR(50) DEFAULT NULL COMMENT '车辆类型' AFTER `plate_no`,
ADD COLUMN IF NOT EXISTS `vehicle_color` VARCHAR(30) DEFAULT NULL COMMENT '车辆颜色' AFTER `vehicle_type`,
ADD NOT EXISTS `vehicle_model` VARCHAR(100) DEFAULT NULL COMMENT '车辆型号' AFTER `vehicle_color`,
ADD COLUMN IF NOT EXISTS `vin` VARCHAR(17) DEFAULT NULL COMMENT '车辆识别码' AFTER `vehicle_model`,
ADD COLUMN IF NOT EXISTS `insurance_policy` VARCHAR(50) DEFAULT NULL COMMENT '保险单号' AFTER `vin`,
ADD COLUMN IF NOT EXISTS `insurance_expire_date` DATE DEFAULT NULL COMMENT '保险到期日期' AFTER `insurance_policy`,
ADD NOT EXISTS `registration_date` DATE DEFAULT NULL COMMENT '注册日期' AFTER `insurance_policy`,
ADD COLUMN IF NOT EXISTS `vehicle_owner` VARCHAR(100) DEFAULT NULL COMMENT '车主姓名' AFTER `registration_date`,
ADD COLUMN IF NOT EXISTS `owner_phone` VARCHAR(20) DEFAULT NULL COMMENT '车主电话' AFTER `vehicle_owner`,
ADD COLUMN IF NOT EXISTS `owner_relation` VARCHAR(50) DEFAULT NULL COMMENT '与访客关系' AFTER `owner_phone`,
ADD COLUMN IF NOT EXISTS `transportation_purpose` VARCHAR(200) DEFAULT NULL COMMENT '运输目的' AFTER `owner_relation`,
ADD COLUMN IF NOT EXISTS `cargo_description` TEXT COMMENT '货物描述（JSON格式）' AFTER `transportation_purpose`,
ADD COLUMN IF NOT EXISTS `weight_limit` DECIMAL(10,2) DEFAULT NULL COMMENT '重量限制（公斤）' AFTER `cargo_description`,
ADD COLUMN IF NOT EXISTS `hazardous_materials` TEXT COMMENT '危险品信息（JSON格式）' AFTER `weight_limit`,
ADD COLUMN IF NOT EXISTS `transport_permit` VARCHAR(50) DEFAULT NULL COMMENT '运输许可证号' AFTER `hazardous_materials`,
ADD COLUMN IF NOT EXISTS `permit_valid_until` DATE DEFAULT NULL COMMENT '许可证有效期' AFTER `transport_permit`,
ADD COLUMN IF NOT EXISTS `parking_allowed` TINYINT DEFAULT 1 COMMENT '是否允许停车：0-否 1-是' AFTER `permit_valid_until`,
ADD COLUMN IF NOT EXISTS `parking_area` VARCHAR(100) DEFAULT NULL COMMENT '停车区域' AFTER `parking_allowed`,
ADD COLUMN IF NOT EXISTS `parking_spot` VARCHAR(50) DEFAULT NULL COMMENT '停车位置' AFTER `parking_area`,
ADD COLUMN IF NOT EXISTS `entrance_permission` TINYINT DEFAULT 1 COMMENT '入口权限：0-无 1-行人 2-车辆 3-混合' AFTER `parking_spot`,
ADD COLUMN IF NOT EXISTS `exit_permission` TINYINT DEFAULT 1 COMMENT '出口权限：0-无 1-行人 2-车辆 3-混合' AFTER `entrance_permission`,
ADD COLUMN IF NOT EXISTS `vehicle_images` TEXT COMMENT '车辆照片（JSON格式）' AFTER `exit_permission`,
ADD COLUMN IF NOT EXISTS `document_urls` TEXT COMMENT '证件URL（JSON格式）' AFTER `vehicle_images`,
ADD COLUMN IF NOT EXISTS `security_check` TINYINT DEFAULT 1 COMMENT '安全检查：0-未检查 1-已通过 2-未通过' AFTER `document_urls`,
ADD COLUMN IF NOT EXISTS `check_results` TEXT COMMENT '检查结果（JSON格式）' AFTER `security_check`,
ADD COLUMN IF NOT EXISTS `access_log` TEXT COMMENT '访问日志（JSON格式）' AFTER `check_results`;

-- 5. 创建索引以优化查询性能
CREATE INDEX IF NOT EXISTS `idx_visitor_blacklist_type_status` ON `t_visitor_blacklist` (`blacklist_type`, `blacklist_status`, `deleted_flag`);
CREATE INDEX IF NOT EXISTS `idx_visitor_blacklist_risk` ON `t_visitor_blacklist` (`risk_score`, `deleted_flag`);
CREATE INDEX IF NOT EXISTS `idx_visitor_blacklist_time` ON `t_visitor_blacklist` (`effective_time`, `expire_time`, `deleted_flag`);
CREATE INDEX IF NOT EXISTS `idx_visitor_blacklist_source` ON `t_visitor_blacklist` (`source`, `approval_status`, `deleted_flag`);
CREATE INDEX IF NOT EXISTS `idx_visitor_blacklist_operator` ON `t_visitor_blacklist` (`operator_id`, `deleted_flag`);

CREATE INDEX IF NOT EXISTS `idx_visitor_approval_appointment` ON `t_visitor_approval_record` (`appointment_id`, `deleted_flag`);
CREATE INDEX IF NOT EXISTS `idx_visitor_approval_visitor` ON `t_visitor_approval_record` (`visitor_id`, `deleted_flag`);
CREATE INDEX IF NOT EXISTS `idx_visitor_approval_approver` ON `t_visitor_record` (`approver_id`, `approval_result`, `deleted_flag`);
CREATE INDEX IF NOT EXISTS `idx_visitor_approval_time` ON `t_visitor_approval_record` (`approval_time`, `deleted_flag`);
CREATE INDEX IF NOT EXISTS `idx_visitor_approval_decision` ON `t_visitor_approval_record` (`approval_decision`, `approval_result`, `deleted_flag`);
CREATE INDEX IF NOT EXISTS `idx_visitor_approval_priority` ON `t_visitor_record` (`priority_level`, `approval_time`, `deleted_flag`);

-- 6. 插入初始数据
INSERT IGNORE INTO `t_visitor_blacklist` (
    `blacklist_type`, `blacklist_level`, `blacklist_reason`, `blacklist_status`, `operator_id`, `operator_name`, `source`, `approval_required`
) VALUES
    (2, 3, '临时访客违规示例', 2, 1, '系统管理员', 'SYSTEM', 0),
    (1, 2, '安全威胁访客', 1, 1, '安全主管', 'MANUAL', 1),
    (1, 3, '严重违规访客', 3, 1, '安全管理员', 'MANUAL', 1);

SELECT 'V20251216040003 访客服务增强表创建完成' AS status;