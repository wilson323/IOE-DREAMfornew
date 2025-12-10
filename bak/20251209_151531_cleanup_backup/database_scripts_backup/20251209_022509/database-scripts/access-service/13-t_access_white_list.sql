-- =============================================
-- IOE-DREAM 智慧园区一卡通管理平台
-- 门禁白名单表 (t_access_white_list)
-- =============================================
-- 功能说明: 门禁白名单表，管理优先通行和免验证的人员名单
-- 业务场景: 
--   1. VIP人员优先通行
--   2. 管理人员免验证通行
--   3. 应急人员快速通行
--   4. 特殊权限临时授予
-- 企业级特性:
--   - 支持多种白名单类型
--   - 支持白名单优先级管理
--   - 支持白名单有效期控制
--   - 支持白名单通行记录
-- =============================================

USE `ioedream_access_db`;

-- =============================================
-- 表结构定义
-- =============================================
CREATE TABLE IF NOT EXISTS `t_access_white_list` (
    -- 主键ID
    `white_list_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '白名单ID',
    
    -- 白名单基本信息
    `white_list_code` VARCHAR(100) NOT NULL COMMENT '白名单编码（唯一标识）',
    `white_list_name` VARCHAR(200) NOT NULL COMMENT '白名单名称',
    `white_list_type` VARCHAR(50) NOT NULL COMMENT '白名单类型：VIP-贵宾, MANAGER-管理人员, EMERGENCY-应急人员, SECURITY-安保人员, TEMPORARY-临时授权',
    `white_list_desc` VARCHAR(500) COMMENT '白名单描述',
    
    -- 人员信息
    `person_id` BIGINT NOT NULL COMMENT '人员ID',
    `person_name` VARCHAR(100) NOT NULL COMMENT '人员姓名',
    `person_type` VARCHAR(50) NOT NULL COMMENT '人员类型：EMPLOYEE-员工, VISITOR-访客, VIP-贵宾, CONTRACTOR-承包商, OTHER-其他',
    `identity_card` VARCHAR(50) COMMENT '身份证号',
    `phone` VARCHAR(20) COMMENT '手机号码',
    `department_id` BIGINT COMMENT '部门ID',
    `position` VARCHAR(100) COMMENT '职位',
    
    -- 权限范围
    `privilege_scope` VARCHAR(50) NOT NULL COMMENT '权限范围：GLOBAL-全局, AREA-指定区域, DEVICE-指定设备',
    `privilege_area_ids` TEXT COMMENT '授权区域ID列表（JSON数组）：["1", "2", "3"]',
    `privilege_device_ids` TEXT COMMENT '授权设备ID列表（JSON数组）：["1", "2", "3"]',
    `exclude_area_ids` TEXT COMMENT '排除区域ID列表（JSON数组）',
    
    -- 特权配置
    `skip_verification` TINYINT DEFAULT 0 COMMENT '跳过验证：0-正常验证, 1-免验证直接通行',
    `priority_level` INT DEFAULT 5 COMMENT '优先级：1-最低, 5-普通, 10-最高（用于排队优先）',
    `multi_door_allowed` TINYINT DEFAULT 0 COMMENT '允许多门同开：0-不允许, 1-允许（突破APB限制）',
    `overtime_allowed` TINYINT DEFAULT 1 COMMENT '允许超时通行：0-不允许, 1-允许（突破时间段限制）',
    `holiday_allowed` TINYINT DEFAULT 1 COMMENT '节假日通行：0-不允许, 1-允许',
    
    -- 授权原因
    `grant_reason` VARCHAR(50) NOT NULL COMMENT '授权原因：VIP_SERVICE-贵宾服务, MANAGEMENT-管理需要, EMERGENCY-应急处理, SECURITY-安保巡查, TEMPORARY-临时授权, OTHER-其他',
    `reason_detail` VARCHAR(500) NOT NULL COMMENT '原因详细说明',
    `authorization_files` TEXT COMMENT '授权文件列表（JSON数组）：[{"name":"file1.pdf", "url":"http://..."}]',
    
    -- 时间控制
    `start_time` DATETIME NOT NULL COMMENT '生效开始时间',
    `end_time` DATETIME COMMENT '生效结束时间（NULL表示永久）',
    `is_permanent` TINYINT DEFAULT 0 COMMENT '是否永久：0-临时, 1-永久',
    `auto_remove` TINYINT DEFAULT 1 COMMENT '自动解除：0-不自动, 1-到期自动解除',
    
    -- 时间段限制
    `time_period_id` BIGINT COMMENT '限制时间段ID（NULL表示不限制）',
    `week_days` VARCHAR(50) COMMENT '生效星期：1,2,3,4,5（NULL表示全周）',
    
    -- 审批信息
    `approval_status` VARCHAR(50) DEFAULT 'PENDING' COMMENT '审批状态：PENDING-待审批, APPROVED-已通过, REJECTED-已拒绝',
    `approver_id` BIGINT COMMENT '审批人ID',
    `approval_time` DATETIME COMMENT '审批时间',
    `approval_comment` VARCHAR(500) COMMENT '审批意见',
    `workflow_instance_id` BIGINT COMMENT '工作流实例ID',
    
    -- 通知配置
    `notify_enabled` TINYINT DEFAULT 1 COMMENT '通知启用：0-禁用, 1-启用（白名单通行时通知）',
    `notify_type` VARCHAR(50) DEFAULT 'NONE' COMMENT '通知类型：NONE-不通知, SMS-短信, EMAIL-邮件, APP-应用推送, ALL-全部',
    `notify_recipients` TEXT COMMENT '通知接收人ID列表（JSON数组）',
    `notify_on_entry` TINYINT DEFAULT 1 COMMENT '进入时通知：0-不通知, 1-通知',
    `notify_on_exit` TINYINT DEFAULT 0 COMMENT '离开时通知：0-不通知, 1-通知',
    
    -- 白名单状态
    `white_list_status` TINYINT DEFAULT 1 COMMENT '白名单状态：0-失效, 1-生效',
    `enabled_flag` TINYINT DEFAULT 1 COMMENT '启用标记：0-停用, 1-启用',
    
    -- 统计信息
    `usage_count` INT DEFAULT 0 COMMENT '使用次数统计',
    `last_usage_time` DATETIME COMMENT '最后使用时间',
    `last_usage_device_id` BIGINT COMMENT '最后使用设备ID',
    `last_usage_area_id` BIGINT COMMENT '最后使用区域ID',
    
    -- 解除信息
    `remove_time` DATETIME COMMENT '解除时间',
    `remove_user_id` BIGINT COMMENT '解除人ID',
    `remove_reason` VARCHAR(500) COMMENT '解除原因',
    
    -- 扩展字段
    `extend_json` JSON COMMENT '扩展配置（JSON格式）',
    `remark` VARCHAR(500) COMMENT '备注说明',
    
    -- 审计字段（强制标准）
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除, 1-已删除',
    
    -- 主键约束
    PRIMARY KEY (`white_list_id`),
    
    -- 唯一键约束
    UNIQUE KEY `uk_white_list_code` (`white_list_code`) USING BTREE,
    
    -- 普通索引
    INDEX `idx_person_id` (`person_id`, `white_list_status`) USING BTREE,
    INDEX `idx_person_type` (`person_type`, `white_list_type`) USING BTREE,
    INDEX `idx_identity_card` (`identity_card`) USING BTREE,
    INDEX `idx_phone` (`phone`) USING BTREE,
    INDEX `idx_privilege_scope` (`privilege_scope`, `white_list_status`) USING BTREE,
    INDEX `idx_priority_level` (`priority_level` DESC) USING BTREE,
    INDEX `idx_time_range` (`start_time`, `end_time`) USING BTREE,
    INDEX `idx_approval_status` (`approval_status`) USING BTREE,
    INDEX `idx_white_list_status` (`white_list_status`, `enabled_flag`) USING BTREE,
    INDEX `idx_create_time` (`create_time` DESC) USING BTREE
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门禁白名单表';

-- =============================================
-- 白名单使用记录表
-- =============================================
CREATE TABLE IF NOT EXISTS `t_access_white_list_record` (
    -- 主键ID
    `record_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    
    -- 关联信息
    `white_list_id` BIGINT NOT NULL COMMENT '白名单ID',
    `person_id` BIGINT NOT NULL COMMENT '人员ID',
    `person_name` VARCHAR(100) NOT NULL COMMENT '人员姓名',
    
    -- 通行信息
    `device_id` BIGINT NOT NULL COMMENT '设备ID',
    `device_name` VARCHAR(200) COMMENT '设备名称',
    `area_id` BIGINT COMMENT '区域ID',
    `area_name` VARCHAR(200) COMMENT '区域名称',
    `access_time` DATETIME NOT NULL COMMENT '通行时间',
    `access_direction` VARCHAR(50) COMMENT '通行方向：IN-进, OUT-出',
    
    -- 通行方式
    `access_method` VARCHAR(50) NOT NULL COMMENT '通行方式：CARD-刷卡, FACE-人脸, FINGERPRINT-指纹, QR_CODE-二维码, SKIP-免验证',
    `access_result` VARCHAR(50) NOT NULL COMMENT '通行结果：SUCCESS-成功, FAILED-失败',
    `skip_verification` TINYINT DEFAULT 0 COMMENT '是否跳过验证：0-正常验证, 1-免验证',
    
    -- 特权使用
    `privilege_used` VARCHAR(200) COMMENT '使用的特权：MULTI_DOOR-多门同开, OVERTIME-超时通行, HOLIDAY-节假日通行（多个用逗号分隔）',
    `priority_granted` TINYINT DEFAULT 0 COMMENT '是否享受优先：0-否, 1-是',
    
    -- 通知信息
    `notification_sent` TINYINT DEFAULT 0 COMMENT '通知已发送：0-未发送, 1-已发送',
    `notification_time` DATETIME COMMENT '通知发送时间',
    `notification_recipients` TEXT COMMENT '通知接收人列表（JSON数组）',
    
    -- 现场信息
    `photo_url` VARCHAR(500) COMMENT '现场照片URL',
    `video_url` VARCHAR(500) COMMENT '现场视频URL',
    `temperature` DECIMAL(4,1) COMMENT '体温（℃）',
    
    -- 扩展字段
    `extend_json` JSON COMMENT '扩展信息（JSON格式）',
    `remark` VARCHAR(500) COMMENT '备注',
    
    -- 审计字段
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除, 1-已删除',
    
    -- 主键约束
    PRIMARY KEY (`record_id`),
    
    -- 普通索引
    INDEX `idx_white_list_id` (`white_list_id`) USING BTREE,
    INDEX `idx_person_time` (`person_id`, `access_time` DESC) USING BTREE,
    INDEX `idx_device_time` (`device_id`, `access_time` DESC) USING BTREE,
    INDEX `idx_area_time` (`area_id`, `access_time` DESC) USING BTREE,
    INDEX `idx_access_result` (`access_result`) USING BTREE,
    INDEX `idx_access_time` (`access_time` DESC) USING BTREE
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='白名单使用记录表';

-- =============================================
-- 初始化数据（企业级默认配置）
-- =============================================

-- 1. VIP人员白名单示例（永久）
INSERT INTO `t_access_white_list` (
    `white_list_code`, `white_list_name`, `white_list_type`, `white_list_desc`,
    `person_id`, `person_name`, `person_type`, `identity_card`, `phone`,
    `privilege_scope`, `skip_verification`, `priority_level`,
    `multi_door_allowed`, `overtime_allowed`, `holiday_allowed`,
    `grant_reason`, `reason_detail`,
    `start_time`, `end_time`, `is_permanent`, `auto_remove`,
    `approval_status`, `approver_id`, `approval_time`,
    `notify_enabled`, `notify_type`, `notify_on_entry`, `notify_on_exit`,
    `white_list_status`, `enabled_flag`, `remark`
) VALUES (
    'WL_VIP_CHAIRMAN', 'VIP白名单-董事长', 'VIP', 'VIP级别最高权限白名单',
    1, '董事长（示例）', 'VIP', NULL, NULL,
    'GLOBAL', 1, 10,
    1, 1, 1,
    'VIP_SERVICE', '公司董事长，享有最高通行权限',
    '2024-01-01 00:00:00', NULL, 1, 0,
    'APPROVED', 1, '2024-01-01 10:00:00',
    1, 'APP', 1, 0,
    1, 1, 'VIP最高级白名单，免验证全局通行'
);

-- 2. 管理人员白名单示例（永久）
INSERT INTO `t_access_white_list` (
    `white_list_code`, `white_list_name`, `white_list_type`, `white_list_desc`,
    `person_id`, `person_name`, `person_type`, `phone`,
    `privilege_scope`, `skip_verification`, `priority_level`,
    `multi_door_allowed`, `overtime_allowed`, `holiday_allowed`,
    `grant_reason`, `reason_detail`,
    `start_time`, `end_time`, `is_permanent`, `auto_remove`,
    `approval_status`, `approver_id`, `approval_time`,
    `notify_enabled`, `notify_type`, `notify_on_entry`,
    `white_list_status`, `enabled_flag`, `remark`
) VALUES (
    'WL_MANAGER_001', '管理人员白名单-总经理', 'MANAGER', '管理层高级权限白名单',
    2, '总经理（示例）', 'EMPLOYEE', '13900139001',
    'GLOBAL', 0, 9,
    1, 1, 1,
    'MANAGEMENT', '总经理，管理需要全局通行权限',
    '2024-01-01 00:00:00', NULL, 1, 0,
    'APPROVED', 1, '2024-01-01 10:00:00',
    1, 'SMS', 1,
    1, 1, '管理层白名单，优先通行权限'
);

-- 3. 安保人员白名单示例（永久）
INSERT INTO `t_access_white_list` (
    `white_list_code`, `white_list_name`, `white_list_type`, `white_list_desc`,
    `person_id`, `person_name`, `person_type`, `phone`,
    `privilege_scope`, `skip_verification`, `priority_level`,
    `multi_door_allowed`, `overtime_allowed`, `holiday_allowed`,
    `grant_reason`, `reason_detail`,
    `start_time`, `end_time`, `is_permanent`, `auto_remove`,
    `approval_status`, `approver_id`, `approval_time`,
    `notify_enabled`, `notify_type`,
    `white_list_status`, `enabled_flag`, `remark`
) VALUES (
    'WL_SECURITY_001', '安保人员白名单-安保主管', 'SECURITY', '安保巡查专用白名单',
    3, '安保主管（示例）', 'EMPLOYEE', '13900139002',
    'GLOBAL', 0, 8,
    1, 1, 1,
    'SECURITY', '安保主管，需要24小时全区域巡查权限',
    '2024-01-01 00:00:00', NULL, 1, 0,
    'APPROVED', 1, '2024-01-01 10:00:00',
    0, 'NONE',
    1, 1, '安保人员白名单，24小时巡查权限'
);

-- 4. 应急人员白名单示例（永久）
INSERT INTO `t_access_white_list` (
    `white_list_code`, `white_list_name`, `white_list_type`, `white_list_desc`,
    `person_id`, `person_name`, `person_type`, `phone`,
    `privilege_scope`, `skip_verification`, `priority_level`,
    `multi_door_allowed`, `overtime_allowed`, `holiday_allowed`,
    `grant_reason`, `reason_detail`,
    `start_time`, `end_time`, `is_permanent`, `auto_remove`,
    `approval_status`, `approver_id`, `approval_time`,
    `notify_enabled`, `notify_type`, `notify_on_entry`,
    `white_list_status`, `enabled_flag`, `remark`
) VALUES (
    'WL_EMERGENCY_001', '应急人员白名单-消防队长', 'EMERGENCY', '应急处理专用白名单',
    4, '消防队长（示例）', 'EMPLOYEE', '13900139003',
    'GLOBAL', 1, 10,
    1, 1, 1,
    'EMERGENCY', '消防队长，应急情况需要快速通行全区域',
    '2024-01-01 00:00:00', NULL, 1, 0,
    'APPROVED', 1, '2024-01-01 10:00:00',
    1, 'ALL', 1,
    1, 1, '应急人员白名单，最高优先级免验证'
);

-- 5. 临时授权白名单示例（7天）
INSERT INTO `t_access_white_list` (
    `white_list_code`, `white_list_name`, `white_list_type`, `white_list_desc`,
    `person_id`, `person_name`, `person_type`, `phone`,
    `privilege_scope`, `privilege_area_ids`, `skip_verification`, `priority_level`,
    `multi_door_allowed`, `overtime_allowed`, `holiday_allowed`,
    `grant_reason`, `reason_detail`,
    `start_time`, `end_time`, `is_permanent`, `auto_remove`,
    `approval_status`, `approver_id`, `approval_time`,
    `notify_enabled`, `notify_type`, `notify_on_entry`, `notify_on_exit`,
    `white_list_status`, `enabled_flag`, `remark`
) VALUES (
    'WL_TEMP_001', '临时授权-项目经理', 'TEMPORARY', '临时项目授权白名单',
    5, '项目经理（示例）', 'VISITOR', '13900139004',
    'AREA', '["1", "2", "3"]', 0, 7,
    0, 1, 0,
    'TEMPORARY', '临时项目需要，授权指定区域通行7天',
    NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), 0, 1,
    'APPROVED', 1, NOW(),
    1, 'SMS', 1, 1,
    1, 1, '临时授权白名单，7天后自动解除'
);

-- =============================================
-- 索引优化说明
-- =============================================
-- 1. uk_white_list_code: 白名单编码唯一索引
-- 2. idx_person_id: 人员ID和状态联合索引，支持快速查询人员白名单状态
-- 3. idx_identity_card: 身份证号索引，支持身份证查询
-- 4. idx_phone: 手机号索引，支持手机号查询
-- 5. idx_privilege_scope: 权限范围和状态联合索引
-- 6. idx_priority_level: 优先级索引，支持优先级排序
-- 7. idx_time_range: 时间范围索引，支持有效期查询

-- =============================================
-- 使用示例
-- =============================================
-- 1. 检查人员是否在白名单中
-- SELECT * FROM t_access_white_list 
-- WHERE person_id = 1 
-- AND white_list_status = 1 
-- AND enabled_flag = 1 
-- AND deleted_flag = 0
-- AND (end_time IS NULL OR end_time > NOW());

-- 2. 查询指定区域的白名单人员
-- SELECT * FROM t_access_white_list 
-- WHERE privilege_scope = 'AREA' 
-- AND JSON_CONTAINS(privilege_area_ids, '1', '$')
-- AND white_list_status = 1 
-- AND deleted_flag = 0;

-- 3. 查询即将到期的临时白名单（7天内）
-- SELECT * FROM t_access_white_list 
-- WHERE is_permanent = 0 
-- AND end_time BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL 7 DAY)
-- AND white_list_status = 1 
-- AND deleted_flag = 0;

-- 4. 记录白名单使用
-- INSERT INTO t_access_white_list_record (
--     white_list_id, person_id, person_name, device_id, access_time, 
--     access_method, access_result, skip_verification
-- ) VALUES (
--     1, 1, '董事长', 1, NOW(), 'SKIP', 'SUCCESS', 1
-- );

-- 5. 更新白名单使用统计
-- UPDATE t_access_white_list 
-- SET usage_count = usage_count + 1, 
--     last_usage_time = NOW(),
--     last_usage_device_id = 1,
--     last_usage_area_id = 1
-- WHERE white_list_id = 1;

-- 6. 自动解除到期白名单
-- UPDATE t_access_white_list 
-- SET white_list_status = 0, 
--     remove_time = NOW(),
--     remove_reason = '自动解除（到期）'
-- WHERE is_permanent = 0 
-- AND auto_remove = 1 
-- AND end_time < NOW() 
-- AND white_list_status = 1;

-- =============================================
-- 定时任务建议
-- =============================================
-- 1. 每小时执行自动解除到期白名单
-- 2. 每天统计白名单使用情况并生成报告
-- 3. 每周审查临时白名单的必要性
-- 4. 每月清理过期的白名单使用记录

-- =============================================
-- 维护建议
-- =============================================
-- 1. 建立白名单审批流程，避免权限滥用
-- 2. 定期审查永久白名单的必要性
-- 3. 监控白名单使用频率，发现异常情况
-- 4. 建立白名单权限分级管理机制
-- 5. 定期清理已删除人员的白名单记录
-- 6. 确保白名单通知及时送达相关人员
-- 7. 定期备份白名单数据，防止误删
-- 8. 建立白名单与黑名单冲突检测机制
