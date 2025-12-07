-- ============================================
-- IOE-DREAM 审批系统数据库表结构
-- 版本: v1.0.0
-- 创建时间: 2025-01-30
-- 说明: 比钉钉更完善的审批系统数据库设计
-- ============================================

-- ============================================
-- 1. 审批配置表
-- ============================================
CREATE TABLE `t_common_approval_config` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    `business_type` varchar(100) NOT NULL COMMENT '业务类型（唯一标识）',
    `business_type_name` varchar(200) NOT NULL COMMENT '业务类型名称',
    `module` varchar(100) NOT NULL COMMENT '所属模块',
    `definition_id` bigint DEFAULT NULL COMMENT '流程定义ID',
    `process_key` varchar(100) DEFAULT NULL COMMENT '流程定义Key（备用）',
    `approval_rules` text COMMENT '审批规则配置（JSON格式）',
    `post_approval_handler` text COMMENT '审批后处理配置（JSON格式）',
    `timeout_config` text COMMENT '超时配置（JSON格式）',
    `notification_config` text COMMENT '通知配置（JSON格式）',
    `applicable_scope` text COMMENT '适用范围配置（JSON格式）',
    `status` varchar(20) NOT NULL DEFAULT 'ENABLED' COMMENT '状态（ENABLED-启用 DISABLED-禁用）',
    `sort_order` int DEFAULT 0 COMMENT '排序号',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    `effective_time` datetime DEFAULT NULL COMMENT '生效时间',
    `expire_time` datetime DEFAULT NULL COMMENT '失效时间（null表示永久有效）',
    `template_id` bigint DEFAULT NULL COMMENT '审批模板ID',
    `allow_print` tinyint DEFAULT 1 COMMENT '是否允许打印（0-不允许 1-允许）',
    `allow_export` tinyint DEFAULT 1 COMMENT '是否允许导出（0-不允许 1-允许）',
    `enable_statistics` tinyint DEFAULT 1 COMMENT '是否启用统计（0-不启用 1-启用）',
    `process_design` text COMMENT '审批流程设计（JSON格式）',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人ID',
    `update_user_id` bigint DEFAULT NULL COMMENT '更新人ID',
    `deleted_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-未删除 1-已删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_business_type` (`business_type`, `deleted_flag`),
    KEY `idx_module` (`module`),
    KEY `idx_status` (`status`),
    KEY `idx_definition_id` (`definition_id`),
    KEY `idx_template_id` (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批配置表（比钉钉更完善）';

-- ============================================
-- 2. 审批节点配置表（比钉钉更完善的节点配置）
-- ============================================
CREATE TABLE `t_common_approval_node_config` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '节点配置ID',
    `approval_config_id` bigint NOT NULL COMMENT '审批配置ID',
    `node_name` varchar(200) NOT NULL COMMENT '节点名称',
    `node_order` int NOT NULL COMMENT '节点顺序（从1开始）',
    `node_type` varchar(50) NOT NULL COMMENT '节点类型（SERIAL-串行 PARALLEL-并行 COUNTERSIGN-会签 OR_SIGN-或签 CONDITION-条件分支 AUTO-自动）',
    `approver_type` varchar(50) NOT NULL COMMENT '审批人设置方式（SPECIFIED_USER-指定人员 DIRECT_MANAGER-直属上级 DEPARTMENT_MANAGER-部门负责人 ROLE-角色 INITIATOR_SELECT-发起人自选 CONTINUOUS_MANAGERS-连续多级主管 FORM_FIELD-表单字段 DYNAMIC_RULE-动态规则）',
    `approver_config` text COMMENT '审批人配置（JSON格式）',
    `approver_count_limit` int DEFAULT NULL COMMENT '审批人数量限制',
    `pass_condition` varchar(50) DEFAULT 'ALL' COMMENT '审批通过条件（ALL-全部通过 ANY-任意一人通过 MAJORITY-多数通过 PERCENTAGE-按比例通过）',
    `pass_percentage` int DEFAULT NULL COMMENT '通过比例（0-100）',
    `condition_config` text COMMENT '条件分支配置（JSON格式）',
    `proxy_config` text COMMENT '审批代理配置（JSON格式）',
    `cc_config` text COMMENT '审批抄送配置（JSON格式）',
    `timeout_config` text COMMENT '超时配置（JSON格式）',
    `comment_required` varchar(50) DEFAULT 'OPTIONAL' COMMENT '审批意见要求（NONE-不要求 OPTIONAL-可选 REQUIRED-必填 REQUIRED_ON_REJECT-驳回时必填）',
    `attachment_required` varchar(50) DEFAULT 'OPTIONAL' COMMENT '附件要求（NONE-不要求 OPTIONAL-可选 REQUIRED-必填）',
    `attachment_config` text COMMENT '附件配置（JSON格式）',
    `data_permission_config` text COMMENT '数据权限配置（JSON格式）',
    `allow_withdraw` tinyint DEFAULT 1 COMMENT '是否允许撤回（0-不允许 1-允许）',
    `withdraw_config` text COMMENT '撤回条件配置（JSON格式）',
    `allow_urge` tinyint DEFAULT 1 COMMENT '是否允许催办（0-不允许 1-允许）',
    `urge_config` text COMMENT '催办配置（JSON格式）',
    `allow_comment` tinyint DEFAULT 1 COMMENT '是否允许评论（0-不允许 1-允许）',
    `comment_config` text COMMENT '评论配置（JSON格式）',
    `status` varchar(20) NOT NULL DEFAULT 'ENABLED' COMMENT '状态（ENABLED-启用 DISABLED-禁用）',
    `sort_order` int DEFAULT 0 COMMENT '排序号',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人ID',
    `update_user_id` bigint DEFAULT NULL COMMENT '更新人ID',
    `deleted_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-未删除 1-已删除）',
    PRIMARY KEY (`id`),
    KEY `idx_approval_config_id` (`approval_config_id`),
    KEY `idx_node_order` (`approval_config_id`, `node_order`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批节点配置表（比钉钉更完善的节点配置）';

-- ============================================
-- 3. 审批模板表（比钉钉更完善的模板管理）
-- ============================================
CREATE TABLE `t_common_approval_template` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '模板ID',
    `template_name` varchar(200) NOT NULL COMMENT '模板名称',
    `template_code` varchar(100) NOT NULL COMMENT '模板编码（唯一标识）',
    `category` varchar(100) DEFAULT NULL COMMENT '模板分类',
    `description` varchar(500) DEFAULT NULL COMMENT '模板描述',
    `icon` varchar(200) DEFAULT NULL COMMENT '模板图标',
    `template_config` text COMMENT '模板配置（JSON格式）',
    `form_design` text COMMENT '表单设计（JSON格式）',
    `usage_count` int DEFAULT 0 COMMENT '使用次数',
    `is_public` tinyint DEFAULT 0 COMMENT '是否公开（0-私有 1-公开）',
    `status` varchar(20) NOT NULL DEFAULT 'ENABLED' COMMENT '状态（ENABLED-启用 DISABLED-禁用）',
    `sort_order` int DEFAULT 0 COMMENT '排序号',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人ID',
    `update_user_id` bigint DEFAULT NULL COMMENT '更新人ID',
    `deleted_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-未删除 1-已删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_template_code` (`template_code`, `deleted_flag`),
    KEY `idx_category` (`category`),
    KEY `idx_status` (`status`),
    KEY `idx_is_public` (`is_public`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批模板表（比钉钉更完善的模板管理）';

-- ============================================
-- 4. 审批统计表（比钉钉更完善的统计功能）
-- ============================================
CREATE TABLE `t_common_approval_statistics` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '统计ID',
    `business_type` varchar(100) NOT NULL COMMENT '业务类型',
    `statistics_date` date NOT NULL COMMENT '统计日期',
    `statistics_dimension` varchar(20) NOT NULL COMMENT '统计维度（DAILY-按日 WEEKLY-按周 MONTHLY-按月 YEARLY-按年）',
    `total_count` int DEFAULT 0 COMMENT '总申请数',
    `approved_count` int DEFAULT 0 COMMENT '已通过数',
    `rejected_count` int DEFAULT 0 COMMENT '已驳回数',
    `processing_count` int DEFAULT 0 COMMENT '进行中数',
    `withdrawn_count` int DEFAULT 0 COMMENT '已撤销数',
    `approval_rate` decimal(5,2) DEFAULT NULL COMMENT '通过率（百分比，0-100）',
    `avg_approval_hours` decimal(10,2) DEFAULT NULL COMMENT '平均审批时长（小时）',
    `max_approval_hours` decimal(10,2) DEFAULT NULL COMMENT '最长审批时长（小时）',
    `min_approval_hours` decimal(10,2) DEFAULT NULL COMMENT '最短审批时长（小时）',
    `avg_node_count` decimal(5,2) DEFAULT NULL COMMENT '平均节点数',
    `timeout_count` int DEFAULT 0 COMMENT '超时审批数',
    `timeout_rate` decimal(5,2) DEFAULT NULL COMMENT '超时率（百分比，0-100）',
    `approver_statistics` text COMMENT '审批人统计（JSON格式）',
    `department_statistics` text COMMENT '部门统计（JSON格式）',
    `statistics_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '统计时间',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人ID',
    `update_user_id` bigint DEFAULT NULL COMMENT '更新人ID',
    `deleted_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-未删除 1-已删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_business_type_date_dimension` (`business_type`, `statistics_date`, `statistics_dimension`, `deleted_flag`),
    KEY `idx_statistics_date` (`statistics_date`),
    KEY `idx_statistics_dimension` (`statistics_dimension`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批统计表（比钉钉更完善的统计功能）';

-- ============================================
-- 5. 审批代理表（比钉钉更完善的代理功能）
-- ============================================
CREATE TABLE `t_common_approval_proxy` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '代理ID',
    `approver_id` bigint NOT NULL COMMENT '审批人ID',
    `proxy_user_id` bigint NOT NULL COMMENT '代理用户ID',
    `business_type` varchar(100) DEFAULT NULL COMMENT '业务类型（null表示所有类型）',
    `start_time` datetime NOT NULL COMMENT '代理开始时间',
    `end_time` datetime NOT NULL COMMENT '代理结束时间',
    `proxy_type` varchar(50) DEFAULT 'MANUAL' COMMENT '代理类型（MANUAL-手动代理 AUTO-自动代理）',
    `proxy_condition` varchar(200) DEFAULT NULL COMMENT '代理条件（APPROVER_ABSENT-审批人不在 APPROVER_BUSY-审批人忙碌）',
    `status` varchar(20) NOT NULL DEFAULT 'ENABLED' COMMENT '状态（ENABLED-启用 DISABLED-禁用）',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人ID',
    `update_user_id` bigint DEFAULT NULL COMMENT '更新人ID',
    `deleted_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-未删除 1-已删除）',
    PRIMARY KEY (`id`),
    KEY `idx_approver_id` (`approver_id`),
    KEY `idx_proxy_user_id` (`proxy_user_id`),
    KEY `idx_business_type` (`business_type`),
    KEY `idx_time_range` (`start_time`, `end_time`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批代理表（比钉钉更完善的代理功能）';

-- ============================================
-- 6. 审批评论表（比钉钉更完善的评论功能）
-- ============================================
CREATE TABLE `t_common_approval_comment` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    `instance_id` bigint NOT NULL COMMENT '流程实例ID',
    `task_id` bigint DEFAULT NULL COMMENT '任务ID',
    `user_id` bigint NOT NULL COMMENT '评论人ID',
    `user_name` varchar(100) DEFAULT NULL COMMENT '评论人姓名',
    `comment_content` text NOT NULL COMMENT '评论内容',
    `comment_type` varchar(50) DEFAULT 'COMMENT' COMMENT '评论类型（COMMENT-普通评论 REPLY-回复评论）',
    `parent_comment_id` bigint DEFAULT NULL COMMENT '父评论ID（回复时使用）',
    `visible_scope` varchar(50) DEFAULT 'ALL' COMMENT '可见范围（ALL-所有人 APPROVER-仅审批人 APPLICANT-仅申请人）',
    `status` varchar(20) NOT NULL DEFAULT 'ENABLED' COMMENT '状态（ENABLED-启用 DISABLED-禁用）',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人ID',
    `update_user_id` bigint DEFAULT NULL COMMENT '更新人ID',
    `deleted_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-未删除 1-已删除）',
    PRIMARY KEY (`id`),
    KEY `idx_instance_id` (`instance_id`),
    KEY `idx_task_id` (`task_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_parent_comment_id` (`parent_comment_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批评论表（比钉钉更完善的评论功能）';

-- ============================================
-- 7. 审批附件表（比钉钉更完善的附件管理）
-- ============================================
CREATE TABLE `t_common_approval_attachment` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '附件ID',
    `instance_id` bigint NOT NULL COMMENT '流程实例ID',
    `task_id` bigint DEFAULT NULL COMMENT '任务ID',
    `file_name` varchar(500) NOT NULL COMMENT '文件名称',
    `file_path` varchar(1000) NOT NULL COMMENT '文件路径',
    `file_size` bigint NOT NULL COMMENT '文件大小（字节）',
    `file_type` varchar(100) DEFAULT NULL COMMENT '文件类型',
    `file_extension` varchar(20) DEFAULT NULL COMMENT '文件扩展名',
    `upload_user_id` bigint NOT NULL COMMENT '上传人ID',
    `upload_user_name` varchar(100) DEFAULT NULL COMMENT '上传人姓名',
    `attachment_type` varchar(50) DEFAULT 'APPLICATION' COMMENT '附件类型（APPLICATION-申请附件 APPROVAL-审批附件 COMMENT-评论附件）',
    `download_count` int DEFAULT 0 COMMENT '下载次数',
    `status` varchar(20) NOT NULL DEFAULT 'ENABLED' COMMENT '状态（ENABLED-启用 DISABLED-禁用）',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人ID',
    `update_user_id` bigint DEFAULT NULL COMMENT '更新人ID',
    `deleted_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-未删除 1-已删除）',
    PRIMARY KEY (`id`),
    KEY `idx_instance_id` (`instance_id`),
    KEY `idx_task_id` (`task_id`),
    KEY `idx_upload_user_id` (`upload_user_id`),
    KEY `idx_attachment_type` (`attachment_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批附件表（比钉钉更完善的附件管理）';

-- ============================================
-- 8. 审批催办记录表（比钉钉更完善的催办功能）
-- ============================================
CREATE TABLE `t_common_approval_urge` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '催办ID',
    `instance_id` bigint NOT NULL COMMENT '流程实例ID',
    `task_id` bigint NOT NULL COMMENT '任务ID',
    `urge_user_id` bigint NOT NULL COMMENT '催办人ID',
    `urge_user_name` varchar(100) DEFAULT NULL COMMENT '催办人姓名',
    `approver_id` bigint NOT NULL COMMENT '被催办人ID',
    `urge_content` varchar(500) DEFAULT NULL COMMENT '催办内容',
    `urge_count` int DEFAULT 1 COMMENT '催办次数',
    `status` varchar(20) NOT NULL DEFAULT 'ENABLED' COMMENT '状态（ENABLED-启用 DISABLED-禁用）',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人ID',
    `update_user_id` bigint DEFAULT NULL COMMENT '更新人ID',
    `deleted_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-未删除 1-已删除）',
    PRIMARY KEY (`id`),
    KEY `idx_instance_id` (`instance_id`),
    KEY `idx_task_id` (`task_id`),
    KEY `idx_urge_user_id` (`urge_user_id`),
    KEY `idx_approver_id` (`approver_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批催办记录表（比钉钉更完善的催办功能）';

