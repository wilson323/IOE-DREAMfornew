-- =============================================
-- IOE-DREAM 智慧园区一卡通管理平台
-- 门禁黑名单表 (t_access_black_list)
-- =============================================
-- 功能说明: 门禁黑名单表，管理禁止通行的人员名单
-- 业务场景: 
--   1. 违规人员限制通行
--   2. 离职人员权限冻结
--   3. 安全风险人员管控
--   4. 临时禁止通行管理
-- 企业级特性:
--   - 支持多种黑名单类型
--   - 支持黑名单有效期管理
--   - 支持黑名单自动解除
--   - 支持黑名单告警联动
-- =============================================

USE `ioedream_access_db`;

-- =============================================
-- 表结构定义
-- =============================================
CREATE TABLE IF NOT EXISTS `t_access_black_list` (
    -- 主键ID
    `black_list_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '黑名单ID',
    
    -- 黑名单基本信息
    `black_list_code` VARCHAR(100) NOT NULL COMMENT '黑名单编码（唯一标识）',
    `black_list_name` VARCHAR(200) NOT NULL COMMENT '黑名单名称',
    `black_list_type` VARCHAR(50) NOT NULL COMMENT '黑名单类型：PERMANENT-永久, TEMPORARY-临时, VIOLATION-违规, RESIGNED-离职, SECURITY-安全风险',
    `black_list_desc` VARCHAR(500) COMMENT '黑名单描述',
    
    -- 人员信息
    `person_id` BIGINT NOT NULL COMMENT '人员ID',
    `person_name` VARCHAR(100) NOT NULL COMMENT '人员姓名',
    `person_type` VARCHAR(50) NOT NULL COMMENT '人员类型：EMPLOYEE-员工, VISITOR-访客, CONTRACTOR-承包商, OTHER-其他',
    `identity_card` VARCHAR(50) COMMENT '身份证号',
    `phone` VARCHAR(20) COMMENT '手机号码',
    
    -- 限制范围
    `restrict_scope` VARCHAR(50) NOT NULL COMMENT '限制范围：GLOBAL-全局, AREA-指定区域, DEVICE-指定设备',
    `restrict_area_ids` TEXT COMMENT '限制区域ID列表（JSON数组）：["1", "2", "3"]',
    `restrict_device_ids` TEXT COMMENT '限制设备ID列表（JSON数组）：["1", "2", "3"]',
    `allow_area_ids` TEXT COMMENT '允许区域ID列表（JSON数组，用于部分限制）',
    
    -- 限制原因
    `restrict_reason` VARCHAR(50) NOT NULL COMMENT '限制原因：VIOLATION-违规违纪, RESIGNED-离职, SECURITY-安全风险, OVERDUE-欠费, TEMPORARY-临时限制, OTHER-其他',
    `reason_detail` VARCHAR(500) NOT NULL COMMENT '原因详细说明',
    `evidence_files` TEXT COMMENT '证据文件列表（JSON数组）：[{"name":"file1.jpg", "url":"http://..."}]',
    
    -- 时间控制
    `start_time` DATETIME NOT NULL COMMENT '限制开始时间',
    `end_time` DATETIME COMMENT '限制结束时间（NULL表示永久）',
    `is_permanent` TINYINT DEFAULT 0 COMMENT '是否永久：0-临时, 1-永久',
    `auto_remove` TINYINT DEFAULT 1 COMMENT '自动解除：0-不自动, 1-到期自动解除',
    
    -- 审批信息
    `approval_status` VARCHAR(50) DEFAULT 'PENDING' COMMENT '审批状态：PENDING-待审批, APPROVED-已通过, REJECTED-已拒绝',
    `approver_id` BIGINT COMMENT '审批人ID',
    `approval_time` DATETIME COMMENT '审批时间',
    `approval_comment` VARCHAR(500) COMMENT '审批意见',
    `workflow_instance_id` BIGINT COMMENT '工作流实例ID',
    
    -- 告警配置
    `alarm_enabled` TINYINT DEFAULT 1 COMMENT '告警启用：0-禁用, 1-启用（黑名单人员尝试通行时告警）',
    `alarm_level` VARCHAR(50) DEFAULT 'WARNING' COMMENT '告警级别：INFO-信息, WARNING-警告, ERROR-错误, CRITICAL-严重',
    `notify_security` TINYINT DEFAULT 1 COMMENT '通知安保：0-不通知, 1-通知安保人员',
    `notify_admin` TINYINT DEFAULT 1 COMMENT '通知管理员：0-不通知, 1-通知管理员',
    
    -- 黑名单状态
    `black_list_status` TINYINT DEFAULT 1 COMMENT '黑名单状态：0-失效, 1-生效',
    `enabled_flag` TINYINT DEFAULT 1 COMMENT '启用标记：0-停用, 1-启用',
    
    -- 统计信息
    `block_count` INT DEFAULT 0 COMMENT '拦截次数统计',
    `last_block_time` DATETIME COMMENT '最后拦截时间',
    `last_block_device_id` BIGINT COMMENT '最后拦截设备ID',
    `last_block_area_id` BIGINT COMMENT '最后拦截区域ID',
    
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
    PRIMARY KEY (`black_list_id`),
    
    -- 唯一键约束
    UNIQUE KEY `uk_black_list_code` (`black_list_code`) USING BTREE,
    
    -- 普通索引
    INDEX `idx_person_id` (`person_id`, `black_list_status`) USING BTREE,
    INDEX `idx_person_type` (`person_type`, `black_list_type`) USING BTREE,
    INDEX `idx_identity_card` (`identity_card`) USING BTREE,
    INDEX `idx_phone` (`phone`) USING BTREE,
    INDEX `idx_restrict_scope` (`restrict_scope`, `black_list_status`) USING BTREE,
    INDEX `idx_time_range` (`start_time`, `end_time`) USING BTREE,
    INDEX `idx_approval_status` (`approval_status`) USING BTREE,
    INDEX `idx_black_list_status` (`black_list_status`, `enabled_flag`) USING BTREE,
    INDEX `idx_create_time` (`create_time` DESC) USING BTREE
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门禁黑名单表';

-- =============================================
-- 黑名单拦截记录表
-- =============================================
CREATE TABLE IF NOT EXISTS `t_access_black_list_record` (
    -- 主键ID
    `record_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    
    -- 关联信息
    `black_list_id` BIGINT NOT NULL COMMENT '黑名单ID',
    `person_id` BIGINT NOT NULL COMMENT '人员ID',
    `person_name` VARCHAR(100) NOT NULL COMMENT '人员姓名',
    
    -- 拦截信息
    `device_id` BIGINT NOT NULL COMMENT '设备ID',
    `device_name` VARCHAR(200) COMMENT '设备名称',
    `area_id` BIGINT COMMENT '区域ID',
    `area_name` VARCHAR(200) COMMENT '区域名称',
    `block_time` DATETIME NOT NULL COMMENT '拦截时间',
    
    -- 拦截方式
    `block_method` VARCHAR(50) NOT NULL COMMENT '拦截方式：CARD-刷卡, FACE-人脸, FINGERPRINT-指纹, QR_CODE-二维码',
    `block_result` VARCHAR(50) NOT NULL COMMENT '拦截结果：BLOCKED-已拦截, WARNED-已警告, PASSED-已放行（特殊情况）',
    `block_reason` VARCHAR(500) COMMENT '拦截原因描述',
    
    -- 告警信息
    `alarm_sent` TINYINT DEFAULT 0 COMMENT '告警已发送：0-未发送, 1-已发送',
    `alarm_time` DATETIME COMMENT '告警发送时间',
    `alarm_recipients` TEXT COMMENT '告警接收人列表（JSON数组）',
    
    -- 处理信息
    `handle_status` VARCHAR(50) DEFAULT 'PENDING' COMMENT '处理状态：PENDING-待处理, HANDLED-已处理, IGNORED-已忽略',
    `handle_action` VARCHAR(500) COMMENT '处理动作',
    `handle_user_id` BIGINT COMMENT '处理人ID',
    `handle_time` DATETIME COMMENT '处理时间',
    `handle_comment` VARCHAR(500) COMMENT '处理备注',
    
    -- 现场信息
    `photo_url` VARCHAR(500) COMMENT '现场照片URL',
    `video_url` VARCHAR(500) COMMENT '现场视频URL',
    `location_info` VARCHAR(200) COMMENT '位置信息',
    
    -- 扩展字段
    `extend_json` JSON COMMENT '扩展信息（JSON格式）',
    `remark` VARCHAR(500) COMMENT '备注',
    
    -- 审计字段
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除, 1-已删除',
    
    -- 主键约束
    PRIMARY KEY (`record_id`),
    
    -- 普通索引
    INDEX `idx_black_list_id` (`black_list_id`) USING BTREE,
    INDEX `idx_person_time` (`person_id`, `block_time` DESC) USING BTREE,
    INDEX `idx_device_time` (`device_id`, `block_time` DESC) USING BTREE,
    INDEX `idx_area_time` (`area_id`, `block_time` DESC) USING BTREE,
    INDEX `idx_block_result` (`block_result`, `handle_status`) USING BTREE,
    INDEX `idx_block_time` (`block_time` DESC) USING BTREE
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='黑名单拦截记录表';

-- =============================================
-- 初始化数据（示例数据）
-- =============================================

-- 注意：实际生产环境不应有默认黑名单数据，此处仅作示例

-- 1. 离职人员黑名单示例（永久）
INSERT INTO `t_access_black_list` (
    `black_list_code`, `black_list_name`, `black_list_type`, `black_list_desc`,
    `person_id`, `person_name`, `person_type`, `identity_card`, `phone`,
    `restrict_scope`, `restrict_reason`, `reason_detail`,
    `start_time`, `end_time`, `is_permanent`, `auto_remove`,
    `approval_status`, `approver_id`, `approval_time`,
    `alarm_enabled`, `alarm_level`, `notify_security`, `notify_admin`,
    `black_list_status`, `enabled_flag`, `remark`
) VALUES (
    'BL_RESIGNED_001', '离职人员黑名单-张三', 'RESIGNED', '离职人员自动加入黑名单',
    9999, '张三（示例）', 'EMPLOYEE', '110101199001011234', '13800138000',
    'GLOBAL', 'RESIGNED', '员工已于2024-12-01离职，自动加入黑名单',
    '2024-12-01 00:00:00', NULL, 1, 0,
    'APPROVED', 1, '2024-12-01 10:00:00',
    1, 'WARNING', 1, 1,
    1, 1, '离职人员示例数据，实际环境应删除'
);

-- 2. 临时黑名单示例（7天）
INSERT INTO `t_access_black_list` (
    `black_list_code`, `black_list_name`, `black_list_type`, `black_list_desc`,
    `person_id`, `person_name`, `person_type`, `identity_card`, `phone`,
    `restrict_scope`, `restrict_area_ids`, `restrict_reason`, `reason_detail`,
    `start_time`, `end_time`, `is_permanent`, `auto_remove`,
    `approval_status`, `approver_id`, `approval_time`,
    `alarm_enabled`, `alarm_level`, `notify_security`, `notify_admin`,
    `black_list_status`, `enabled_flag`, `remark`
) VALUES (
    'BL_TEMP_001', '临时限制-李四', 'TEMPORARY', '临时限制通行7天',
    9998, '李四（示例）', 'VISITOR', '110101199002021234', '13900139000',
    'AREA', '["1", "2"]', 'TEMPORARY', '访客违规，临时限制核心区域通行',
    NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), 0, 1,
    'APPROVED', 1, NOW(),
    1, 'WARNING', 1, 0,
    1, 1, '临时限制示例数据，7天后自动解除'
);

-- =============================================
-- 索引优化说明
-- =============================================
-- 1. uk_black_list_code: 黑名单编码唯一索引
-- 2. idx_person_id: 人员ID和状态联合索引，支持快速查询人员黑名单状态
-- 3. idx_identity_card: 身份证号索引，支持身份证查询
-- 4. idx_phone: 手机号索引，支持手机号查询
-- 5. idx_restrict_scope: 限制范围和状态联合索引
-- 6. idx_time_range: 时间范围索引，支持有效期查询
-- 7. idx_person_time: 人员和时间联合索引（记录表），支持个人拦截历史查询

-- =============================================
-- 使用示例
-- =============================================
-- 1. 检查人员是否在黑名单中
-- SELECT * FROM t_access_black_list 
-- WHERE person_id = 9999 
-- AND black_list_status = 1 
-- AND enabled_flag = 1 
-- AND deleted_flag = 0
-- AND (end_time IS NULL OR end_time > NOW());

-- 2. 查询指定区域的黑名单人员
-- SELECT * FROM t_access_black_list 
-- WHERE restrict_scope = 'AREA' 
-- AND JSON_CONTAINS(restrict_area_ids, '1', '$')
-- AND black_list_status = 1 
-- AND deleted_flag = 0;

-- 3. 查询即将到期的临时黑名单（7天内）
-- SELECT * FROM t_access_black_list 
-- WHERE is_permanent = 0 
-- AND end_time BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL 7 DAY)
-- AND black_list_status = 1 
-- AND deleted_flag = 0;

-- 4. 记录黑名单拦截
-- INSERT INTO t_access_black_list_record (
--     black_list_id, person_id, person_name, device_id, block_time, block_method, block_result
-- ) VALUES (
--     1, 9999, '张三', 1, NOW(), 'CARD', 'BLOCKED'
-- );

-- 5. 更新黑名单拦截统计
-- UPDATE t_access_black_list 
-- SET block_count = block_count + 1, 
--     last_block_time = NOW(),
--     last_block_device_id = 1,
--     last_block_area_id = 1
-- WHERE black_list_id = 1;

-- 6. 自动解除到期黑名单
-- UPDATE t_access_black_list 
-- SET black_list_status = 0, 
--     remove_time = NOW(),
--     remove_reason = '自动解除（到期）'
-- WHERE is_permanent = 0 
-- AND auto_remove = 1 
-- AND end_time < NOW() 
-- AND black_list_status = 1;

-- =============================================
-- 定时任务建议
-- =============================================
-- 1. 每小时执行自动解除到期黑名单
-- 2. 每天统计黑名单拦截情况并生成报告
-- 3. 每周审查长期黑名单的合理性
-- 4. 每月清理过期的黑名单拦截记录

-- =============================================
-- 维护建议
-- =============================================
-- 1. 建立黑名单审批流程，避免误加入
-- 2. 定期审查永久黑名单的必要性
-- 3. 监控黑名单拦截频率，发现异常情况
-- 4. 建立黑名单解除申诉机制
-- 5. 定期清理已删除人员的黑名单记录
-- 6. 确保黑名单告警及时送达相关人员
-- 7. 定期备份黑名单数据，防止误删
