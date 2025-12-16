-- ============================================================
-- 用户偏好设置表
-- 支持用户个性化偏好配置，针对单企业1000台设备、20000人规模优化
-- ============================================================

-- 创建表
CREATE TABLE IF NOT EXISTS `t_user_preference` (
    `preference_id` BIGINT NOT NULL COMMENT '偏好设置ID（主键）',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `preference_category` VARCHAR(50) NOT NULL COMMENT '偏好类别：interface-界面偏好, behavior-行为偏好, notification-通知偏好, dashboard-仪表盘偏好',
    `preference_key` VARCHAR(100) NOT NULL COMMENT '偏好键',
    `preference_value` TEXT COMMENT '偏好值',
    `preference_type` VARCHAR(20) DEFAULT 'string' COMMENT '偏好类型：string-字符串, number-数字, boolean-布尔, json-JSON对象',
    `default_value` VARCHAR(500) COMMENT '默认值',
    `preference_desc` VARCHAR(500) COMMENT '偏好描述',
    `is_system` TINYINT DEFAULT 0 COMMENT '是否系统偏好：0-用户自定义, 1-系统预设',
    `is_visible` TINYINT DEFAULT 1 COMMENT '是否可见：0-隐藏, 1-可见',
    `is_editable` TINYINT DEFAULT 1 COMMENT '是否可编辑：0-只读, 1-可编辑',
    `sort_order` INT DEFAULT 0 COMMENT '排序号',
    `validation_rule` TEXT COMMENT '验证规则 (JSON格式)',
    `options` TEXT COMMENT '选项列表 (JSON格式，用于枚举类型)',
    `preference_group` VARCHAR(50) COMMENT '偏好分组',
    `device_type` VARCHAR(20) DEFAULT 'all' COMMENT '设备类型：web-网页端, mobile-移动端, all-通用',
    `last_update_time` DATETIME COMMENT '最后更新时间',
    `update_count` INT DEFAULT 0 COMMENT '更新次数',
    `preference_weight` INT DEFAULT 1 COMMENT '偏好权重 (用于推荐算法)',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用, 1-启用',
    `extended_attributes` TEXT COMMENT '扩展属性 (JSON格式)',
    -- 审计字段
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除, 1-已删除',
    `version` INT DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`preference_id`),
    UNIQUE KEY `uk_user_category_key` (`user_id`, `preference_category`, `preference_key`, `deleted_flag`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_category` (`preference_category`),
    KEY `idx_preference_key` (`preference_key`),
    KEY `idx_status` (`status`),
    KEY `idx_device_type` (`device_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户偏好设置表';

-- 插入系统默认偏好设置模板
INSERT INTO `t_user_preference` (`preference_id`, `user_id`, `preference_category`, `preference_key`, `preference_value`, `preference_type`, `default_value`, `preference_desc`, `is_system`, `is_visible`, `is_editable`, `sort_order`, `preference_group`, `device_type`, `status`) VALUES
-- 界面偏好
(1, 0, 'interface', 'language', 'zh_CN', 'string', 'zh_CN', '界面语言', 1, 1, 1, 1, 'basic', 'all', 1),
(2, 0, 'interface', 'timezone', 'Asia/Shanghai', 'string', 'Asia/Shanghai', '时区设置', 1, 1, 1, 2, 'basic', 'all', 1),
(3, 0, 'interface', 'dateFormat', 'yyyy-MM-dd', 'string', 'yyyy-MM-dd', '日期格式', 1, 1, 1, 3, 'basic', 'all', 1),
(4, 0, 'interface', 'timeFormat', 'HH:mm:ss', 'string', 'HH:mm:ss', '时间格式', 1, 1, 1, 4, 'basic', 'all', 1),
(5, 0, 'interface', 'theme', 'light', 'string', 'light', '主题模式：light-浅色, dark-深色', 1, 1, 1, 5, 'appearance', 'web', 1),
(6, 0, 'interface', 'sidebarCollapsed', 'false', 'boolean', 'false', '侧边栏是否折叠', 1, 1, 1, 6, 'appearance', 'web', 1),
-- 通知偏好
(10, 0, 'notification', 'enableNotification', 'true', 'boolean', 'true', '是否启用通知', 1, 1, 1, 1, 'notification', 'all', 1),
(11, 0, 'notification', 'enableEmail', 'true', 'boolean', 'true', '是否启用邮件通知', 1, 1, 1, 2, 'notification', 'all', 1),
(12, 0, 'notification', 'enableSms', 'false', 'boolean', 'false', '是否启用短信通知', 1, 1, 1, 3, 'notification', 'all', 1),
(13, 0, 'notification', 'enablePush', 'true', 'boolean', 'true', '是否启用推送通知', 1, 1, 1, 4, 'notification', 'mobile', 1),
-- 行为偏好
(20, 0, 'behavior', 'pageSize', '20', 'number', '20', '每页显示条数', 1, 1, 1, 1, 'pagination', 'all', 1),
(21, 0, 'behavior', 'autoRefreshInterval', '30', 'number', '30', '自动刷新间隔（秒）', 1, 1, 1, 2, 'refresh', 'web', 1),
(22, 0, 'behavior', 'rememberLastPage', 'true', 'boolean', 'true', '是否记住上次访问页面', 1, 1, 1, 3, 'navigation', 'web', 1),
-- 仪表盘偏好
(30, 0, 'dashboard', 'dashboardLayout', '{"columns":3,"widgets":[]}', 'json', '{"columns":3,"widgets":[]}', '仪表盘布局配置', 1, 1, 1, 1, 'layout', 'web', 1),
(31, 0, 'dashboard', 'defaultDashboard', 'overview', 'string', 'overview', '默认仪表盘', 1, 1, 1, 2, 'layout', 'web', 1);
