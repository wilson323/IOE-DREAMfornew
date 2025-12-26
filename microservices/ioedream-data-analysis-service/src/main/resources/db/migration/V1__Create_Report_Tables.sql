-- =====================================================
-- IOE-DREAM数据分析服务 - 数据报表表
-- 版本: V1
-- 描述: 创建报表、仪表板、导出任务相关表
-- 作者: IOE-DREAM Team
-- 日期: 2025-12-26
-- =====================================================

-- =====================================================
-- 报表配置表
-- =====================================================
CREATE TABLE IF NOT EXISTS `t_data_report` (
    `report_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '报表ID',
    `report_name` VARCHAR(100) NOT NULL COMMENT '报表名称',
    `report_code` VARCHAR(50) NOT NULL UNIQUE COMMENT '报表编码',
    `report_type` VARCHAR(20) NOT NULL COMMENT '报表类型:list/summary/chart/dashboard',
    `business_module` VARCHAR(20) NOT NULL COMMENT '业务模块:attendance/consume/access/visitor/video',

    -- 数据源配置
    `source_type` VARCHAR(20) NOT NULL COMMENT '数据源类型:database/api/elasticsearch/redis',
    `source_name` VARCHAR(100) COMMENT '表名或API路径',
    `source_config` JSON COMMENT '数据源连接配置',
    `field_mapping` JSON COMMENT '字段映射配置',

    -- 查询配置
    `query_config` JSON COMMENT '查询配置',

    -- 布局配置
    `layout_config` JSON COMMENT '布局配置',

    -- 权限配置
    `permission_config` JSON COMMENT '权限配置',

    -- 基础字段
    `creator_id` BIGINT NOT NULL COMMENT '创建人ID',
    `creator_name` VARCHAR(50) COMMENT '创建人姓名',
    `status` VARCHAR(20) NOT NULL DEFAULT 'draft' COMMENT '状态:draft/active/archived',
    `description` TEXT COMMENT '报表描述',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',

    INDEX `idx_report_code` (`report_code`),
    INDEX `idx_business_module` (`business_module`),
    INDEX `idx_status` (`status`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据报表配置表';

-- =====================================================
-- 仪表板配置表
-- =====================================================
CREATE TABLE IF NOT EXISTS `t_data_dashboard` (
    `dashboard_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '仪表板ID',
    `dashboard_name` VARCHAR(100) NOT NULL COMMENT '仪表板名称',
    `dashboard_code` VARCHAR(50) NOT NULL UNIQUE COMMENT '仪表板编码',

    -- 布局配置
    `layout_config` JSON NOT NULL COMMENT '仪表板布局配置',

    -- 权限配置
    `permission_config` JSON COMMENT '权限配置',

    -- 基础字段
    `creator_id` BIGINT NOT NULL COMMENT '创建人ID',
    `creator_name` VARCHAR(50) COMMENT '创建人姓名',
    `status` VARCHAR(20) NOT NULL DEFAULT 'draft' COMMENT '状态:draft/active/archived',
    `description` TEXT COMMENT '仪表板描述',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `refresh_time` DATETIME COMMENT '最后刷新时间',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',

    INDEX `idx_dashboard_code` (`dashboard_code`),
    INDEX `idx_status` (`status`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据仪表板配置表';

-- =====================================================
-- 导出任务表
-- =====================================================
CREATE TABLE IF NOT EXISTS `t_data_export_task` (
    `export_task_id` VARCHAR(64) PRIMARY KEY COMMENT '导出任务ID',
    `report_id` BIGINT NOT NULL COMMENT '报表ID',
    `export_format` VARCHAR(10) NOT NULL COMMENT '导出格式:excel/pdf/csv',
    `file_name` VARCHAR(255) COMMENT '文件名',
    `file_url` VARCHAR(500) COMMENT '文件URL',
    `file_size` BIGINT COMMENT '文件大小（字节）',
    `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态:pending/processing/completed/failed',
    `error_message` TEXT COMMENT '错误信息',
    `request_params` JSON COMMENT '请求参数',

    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `complete_time` DATETIME COMMENT '完成时间',

    INDEX `idx_report_id` (`report_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据导出任务表';

-- =====================================================
-- 初始化报表模板数据
-- =====================================================
INSERT INTO `t_data_report` (`report_name`, `report_code`, `report_type`, `business_module`, `source_type`, `source_name`, `creator_id`, `creator_name`, `status`, `description`) VALUES
('考勤打卡日报', 'ATTENDANCE_DAILY_REPORT', 'list', 'attendance', 'database', 't_attendance_record', 1, '系统管理员', 'active', '每日考勤打卡记录报表'),
('考勤月度汇总', 'ATTENDANCE_MONTHLY_SUMMARY', 'summary', 'attendance', 'database', 't_attendance_summary', 1, '系统管理员', 'active', '月度考勤数据统计汇总'),
('消费记录明细', 'CONSUME_RECORD_LIST', 'list', 'consume', 'database', 't_consume_record', 1, '系统管理员', 'active', '消费交易明细记录'),
('门禁通行记录', 'ACCESS_RECORD_LIST', 'list', 'access', 'database', 't_access_record', 1, '系统管理员', 'active', '门禁通行记录查询');

-- =====================================================
-- 初始化仪表板模板数据
-- =====================================================
INSERT INTO `t_data_dashboard` (`dashboard_name`, `dashboard_code`, `layout_config`, `creator_id`, `creator_name`, `status`, `description`) VALUES
('智慧园区运营中心', 'SMART_PARK_OPERATION', '{"components":[{"type":"stat-card","position":{"x":0,"y":0,"w":3,"h":2},"dataSource":{"type":"api","url":"/api/v1/statistics/overview"}},{"type":"chart","position":{"x":3,"y":0,"w":6,"h":4},"chartType":"line","dataSource":{"type":"api","url":"/api/v1/statistics/trend"}}]}', 1, '系统管理员', 'active', '智慧园区综合运营数据大屏'),
('考勤数据分析', 'ATTENDANCE_ANALYSIS', '{"components":[{"type":"stat-card","title":"今日打卡","position":{"x":0,"y":0,"w":3,"h":2}},{"type":"chart","chartType":"bar","title":"部门出勤率","position":{"x":0,"y":2,"w":6,"h":4}}]}', 1, '系统管理员', 'active', '考勤数据分析仪表板');
