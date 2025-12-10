-- ==================================================================
-- 考勤服务 - 轮班规则表 (12-t_attendance_rotation_rule.sql)
-- ==================================================================
-- 表名: t_attendance_rotation_rule
-- 说明: 存储和管理轮班制度的规则配置,支持三班倒、四班三倒、自定义轮班周期等复杂排班规则
-- 依赖: t_attendance_shift(班次配置表), t_attendance_shift_group(班次组配置表)
-- 作者: IOE-DREAM团队
-- 创建时间: 2025-12-08
-- 版本: v1.0.0
-- ==================================================================

USE `ioedream_attendance`;

-- ==================================================================
-- 删除已存在的表(如果存在)
-- ==================================================================
DROP TABLE IF EXISTS `t_attendance_rotation_rule`;

-- ==================================================================
-- 创建表: t_attendance_rotation_rule
-- ==================================================================
CREATE TABLE IF NOT EXISTS `t_attendance_rotation_rule` (
    -- ==============================================================
    -- 主键和唯一标识
    -- ==============================================================
    `rotation_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '轮班规则ID(主键)',
    `rotation_code` VARCHAR(100) NOT NULL COMMENT '轮班规则编码(唯一标识,如:ROTATION_001)',

    -- ==============================================================
    -- 基本信息
    -- ==============================================================
    `rotation_name` VARCHAR(200) NOT NULL COMMENT '轮班规则名称(如:三班两倒轮班规则)',
    `rotation_name_en` VARCHAR(200) COMMENT '轮班规则英文名称',
    `rotation_description` TEXT COMMENT '轮班规则描述',
    `rotation_type` VARCHAR(50) NOT NULL DEFAULT 'FIXED' COMMENT '轮班类型(FIXED-固定轮班,FLEXIBLE-弹性轮班,AUTO-自动轮班)',
    `rotation_mode` VARCHAR(50) NOT NULL DEFAULT 'THREE_SHIFT_TWO' COMMENT '轮班模式(THREE_SHIFT_TWO-三班两倒,FOUR_SHIFT_THREE-四班三倒,CUSTOM-自定义)',

    -- ==============================================================
    -- 轮班周期配置
    -- ==============================================================
    `cycle_type` VARCHAR(50) NOT NULL DEFAULT 'DAY' COMMENT '周期类型(DAY-天,WEEK-周,MONTH-月)',
    `cycle_length` INT NOT NULL DEFAULT 7 COMMENT '周期长度(如:7天一个周期)',
    `cycle_unit` VARCHAR(20) NOT NULL DEFAULT 'DAY' COMMENT '周期单位(DAY,WEEK,MONTH)',

    -- ==============================================================
    -- 班次配置
    -- ==============================================================
    `shift_count` INT NOT NULL DEFAULT 3 COMMENT '班次数量(如:三班倒为3)',
    `shift_ids` JSON NOT NULL COMMENT '班次ID集合(JSON数组,如:["101","102","103"])',
    `shift_sequence` JSON NOT NULL COMMENT '班次顺序(JSON数组,定义轮班顺序)',
    `shift_duration_hours` DECIMAL(10,2) NOT NULL DEFAULT 8.00 COMMENT '每班时长(小时)',

    -- ==============================================================
    -- 轮班组配置
    -- ==============================================================
    `group_count` INT NOT NULL DEFAULT 2 COMMENT '轮班组数(如:三班两倒需要2个组)',
    `shift_group_ids` JSON COMMENT '班次组ID集合(JSON数组)',
    `group_rotation_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用组间轮换(0-否,1-是)',
    `group_rotation_interval` INT NOT NULL DEFAULT 7 COMMENT '组间轮换间隔(天)',

    -- ==============================================================
    -- 轮班规则
    -- ==============================================================
    `rotation_pattern` JSON NOT NULL COMMENT '轮班模式(JSON数组,定义详细的轮班规则)',
    `start_shift_id` BIGINT COMMENT '起始班次ID',
    `rotation_direction` VARCHAR(20) NOT NULL DEFAULT 'FORWARD' COMMENT '轮班方向(FORWARD-正向,BACKWARD-反向,RANDOM-随机)',
    `auto_rotate` TINYINT NOT NULL DEFAULT 1 COMMENT '是否自动轮班(0-否,1-是)',
    `rotate_interval_days` INT NOT NULL DEFAULT 1 COMMENT '轮换间隔天数',

    -- ==============================================================
    -- 休息日配置
    -- ==============================================================
    `rest_days_per_cycle` INT NOT NULL DEFAULT 1 COMMENT '每周期休息天数',
    `rest_day_pattern` JSON COMMENT '休息日模式(JSON数组,定义休息日规则)',
    `continuous_work_days_max` INT NOT NULL DEFAULT 6 COMMENT '最大连续工作天数',
    `continuous_rest_days_min` INT NOT NULL DEFAULT 1 COMMENT '最小连续休息天数',

    -- ==============================================================
    -- 交接班配置
    -- ==============================================================
    `handover_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用交接班(0-否,1-是)',
    `handover_duration_minutes` INT NOT NULL DEFAULT 30 COMMENT '交接班时长(分钟)',
    `handover_overlap_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否允许班次重叠(0-否,1-是)',
    `handover_checklist` JSON COMMENT '交接班检查项(JSON数组)',

    -- ==============================================================
    -- 调班规则
    -- ==============================================================
    `allow_shift_swap` TINYINT NOT NULL DEFAULT 1 COMMENT '是否允许调班(0-否,1-是)',
    `swap_approval_required` TINYINT NOT NULL DEFAULT 1 COMMENT '调班是否需要审批(0-否,1-是)',
    `swap_notice_hours` INT NOT NULL DEFAULT 24 COMMENT '调班提前通知时长(小时)',
    `swap_limit_per_month` INT NOT NULL DEFAULT 3 COMMENT '每月调班次数限制',

    -- ==============================================================
    -- 加班规则
    -- ==============================================================
    `overtime_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否允许加班(0-否,1-是)',
    `overtime_rule_id` BIGINT COMMENT '加班规则ID',
    `max_overtime_hours_per_day` DECIMAL(10,2) NOT NULL DEFAULT 3.00 COMMENT '每天最大加班时长(小时)',
    `max_overtime_hours_per_month` DECIMAL(10,2) NOT NULL DEFAULT 36.00 COMMENT '每月最大加班时长(小时)',

    -- ==============================================================
    -- 节假日处理
    -- ==============================================================
    `holiday_adjustment_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用节假日调整(0-否,1-是)',
    `holiday_rotation_paused` TINYINT NOT NULL DEFAULT 1 COMMENT '节假日期间是否暂停轮班(0-否,1-是)',
    `holiday_makeup_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用节假日补班(0-否,1-是)',

    -- ==============================================================
    -- 适用范围
    -- ==============================================================
    `applicable_scope` JSON COMMENT '适用范围(JSON对象,包含部门、职位、员工类型等)',
    `department_ids` JSON COMMENT '适用部门ID集合(JSON数组)',
    `position_ids` JSON COMMENT '适用职位ID集合(JSON数组)',
    `employee_ids` JSON COMMENT '适用员工ID集合(JSON数组)',
    `employee_type_filter` JSON COMMENT '适用员工类型过滤(JSON数组)',

    -- ==============================================================
    -- 生效时间
    -- ==============================================================
    `effective_date` DATE NOT NULL COMMENT '生效日期',
    `expiry_date` DATE COMMENT '失效日期',
    `effective_time` TIME COMMENT '生效时间',
    `timezone` VARCHAR(50) NOT NULL DEFAULT 'Asia/Shanghai' COMMENT '时区',

    -- ==============================================================
    -- 排班生成配置
    -- ==============================================================
    `auto_generate_schedule` TINYINT NOT NULL DEFAULT 1 COMMENT '是否自动生成排班(0-否,1-是)',
    `generate_days_ahead` INT NOT NULL DEFAULT 30 COMMENT '提前生成排班天数',
    `schedule_template_id` BIGINT COMMENT '排班模板ID',
    `schedule_conflict_handling` VARCHAR(50) NOT NULL DEFAULT 'OVERRIDE' COMMENT '排班冲突处理(OVERRIDE-覆盖,MERGE-合并,SKIP-跳过)',

    -- ==============================================================
    -- 通知配置
    -- ==============================================================
    `notification_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用通知(0-否,1-是)',
    `notification_channels` JSON COMMENT '通知渠道(JSON数组,["短信","邮件","微信","App推送"])',
    `notify_before_shift_hours` INT NOT NULL DEFAULT 12 COMMENT '班次开始前通知时长(小时)',
    `notify_on_rotation_change` TINYINT NOT NULL DEFAULT 1 COMMENT '轮班变更时是否通知(0-否,1-是)',

    -- ==============================================================
    -- 优先级和排序
    -- ==============================================================
    `priority` INT NOT NULL DEFAULT 100 COMMENT '优先级(数值越大优先级越高)',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序序号',

    -- ==============================================================
    -- 状态和控制
    -- ==============================================================
    `rotation_status` VARCHAR(50) NOT NULL DEFAULT 'ACTIVE' COMMENT '轮班规则状态(DRAFT-草稿,ACTIVE-生效,PAUSED-暂停,EXPIRED-已过期)',
    `is_template` TINYINT NOT NULL DEFAULT 0 COMMENT '是否为模板(0-否,1-是)',
    `template_source_id` BIGINT COMMENT '模板源ID',

    -- ==============================================================
    -- 审批流程
    -- ==============================================================
    `require_approval` TINYINT NOT NULL DEFAULT 1 COMMENT '是否需要审批(0-否,1-是)',
    `approval_workflow_id` BIGINT COMMENT '审批工作流ID',
    `approval_status` VARCHAR(50) NOT NULL DEFAULT 'PENDING' COMMENT '审批状态(PENDING-待审批,APPROVED-已通过,REJECTED-已拒绝)',

    -- ==============================================================
    -- 统计信息
    -- ==============================================================
    `total_employees` INT NOT NULL DEFAULT 0 COMMENT '总员工数',
    `active_employees` INT NOT NULL DEFAULT 0 COMMENT '活跃员工数',
    `total_schedules_generated` INT NOT NULL DEFAULT 0 COMMENT '已生成排班总数',
    `last_rotation_time` DATETIME COMMENT '最后轮班时间',
    `next_rotation_time` DATETIME COMMENT '下次轮班时间',
    `rotation_count` INT NOT NULL DEFAULT 0 COMMENT '轮班次数统计',

    -- ==============================================================
    -- 扩展字段
    -- ==============================================================
    `extended_attributes` JSON COMMENT '扩展属性(JSON对象)',
    `metadata` JSON COMMENT '元数据(JSON对象)',
    `tags` JSON COMMENT '标签(JSON数组)',

    -- ==============================================================
    -- 备注和说明
    -- ==============================================================
    `remark` TEXT COMMENT '备注',
    `admin_note` TEXT COMMENT '管理员备注',

    -- ==============================================================
    -- 标准审计字段
    -- ==============================================================
    `version` INT NOT NULL DEFAULT 1 COMMENT '版本号(用于乐观锁)',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人用户ID',
    `update_user_id` BIGINT COMMENT '更新人用户ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记(0-未删除,1-已删除)',

    -- ==============================================================
    -- 主键约束
    -- ==============================================================
    PRIMARY KEY (`rotation_id`),

    -- ==============================================================
    -- 唯一索引
    -- ==============================================================
    UNIQUE KEY `uk_rotation_code` (`rotation_code`),

    -- ==============================================================
    -- 普通索引
    -- ==============================================================
    INDEX `idx_rotation_name` (`rotation_name`),
    INDEX `idx_rotation_type` (`rotation_type`),
    INDEX `idx_rotation_mode` (`rotation_mode`),
    INDEX `idx_rotation_status` (`rotation_status`),
    INDEX `idx_is_template` (`is_template`),
    INDEX `idx_effective_date` (`effective_date`),
    INDEX `idx_expiry_date` (`expiry_date`),

    -- ==============================================================
    -- 复合索引(优化常用查询)
    -- ==============================================================
    INDEX `idx_type_status` (`rotation_type`, `rotation_status`),
    INDEX `idx_mode_status` (`rotation_mode`, `rotation_status`),
    INDEX `idx_status_template` (`rotation_status`, `is_template`),
    INDEX `idx_date_status` (`effective_date`, `rotation_status`),

    -- ==============================================================
    -- 时间索引(降序,优化最新数据查询)
    -- ==============================================================
    INDEX `idx_create_time` (`create_time` DESC),
    INDEX `idx_update_time` (`update_time` DESC),

    -- ==============================================================
    -- 逻辑删除索引
    -- ==============================================================
    INDEX `idx_deleted_flag` (`deleted_flag`)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤服务-轮班规则表';

-- ==================================================================
-- 初始化数据: 轮班规则示例(8条企业级示例)
-- ==================================================================

-- 1. 客服部三班两倒轮班规则
INSERT INTO `t_attendance_rotation_rule` (
    `rotation_code`, `rotation_name`, `rotation_type`, `rotation_mode`,
    `cycle_type`, `cycle_length`, `shift_count`, `shift_ids`, `shift_sequence`,
    `shift_duration_hours`, `group_count`, `group_rotation_enabled`, `group_rotation_interval`,
    `rotation_pattern`, `rotation_direction`, `auto_rotate`, `rotate_interval_days`,
    `rest_days_per_cycle`, `continuous_work_days_max`, `continuous_rest_days_min`,
    `effective_date`, `auto_generate_schedule`, `generate_days_ahead`,
    `rotation_status`, `total_employees`, `remark`
) VALUES (
    'ROTATION_001', '客服部三班两倒', 'FIXED', 'THREE_SHIFT_TWO',
    'DAY', 7, 3, '[201,202,203]', '["早班","中班","晚班"]',
    8.00, 2, 1, 7,
    '{"pattern":"早班→中班→晚班→休息→早班","cycle_days":4}', 'FORWARD', 1, 1,
    1, 6, 1,
    '2025-01-01', 1, 30,
    'ACTIVE', 30, '客服部24小时轮班制度,三班两倒,每4天一个周期'
);

-- 2. 生产线四班三倒轮班规则
INSERT INTO `t_attendance_rotation_rule` (
    `rotation_code`, `rotation_name`, `rotation_type`, `rotation_mode`,
    `cycle_type`, `cycle_length`, `shift_count`, `shift_ids`, `shift_sequence`,
    `shift_duration_hours`, `group_count`, `group_rotation_enabled`, `group_rotation_interval`,
    `rotation_pattern`, `rotation_direction`, `auto_rotate`,
    `rest_days_per_cycle`, `continuous_work_days_max`, `continuous_rest_days_min`,
    `effective_date`, `auto_generate_schedule`, `generate_days_ahead`,
    `rotation_status`, `total_employees`, `remark`
) VALUES (
    'ROTATION_002', '生产线四班三倒', 'FIXED', 'FOUR_SHIFT_THREE',
    'DAY', 8, 4, '[201,202,203,204]', '["早班","中班","晚班","夜班"]',
    8.00, 3, 1, 8,
    '{"pattern":"早班→中班→晚班→夜班→休息→早班","cycle_days":5}', 'FORWARD', 1,
    1, 6, 1,
    '2025-01-01', 1, 30,
    'ACTIVE', 40, '生产线24小时不间断生产,四班三倒制度,每5天一个周期'
);

-- 3. 安保部两班倒轮班规则
INSERT INTO `t_attendance_rotation_rule` (
    `rotation_code`, `rotation_name`, `rotation_type`, `rotation_mode`,
    `cycle_type`, `cycle_length`, `shift_count`, `shift_ids`, `shift_sequence`,
    `shift_duration_hours`, `group_count`, `group_rotation_enabled`, `group_rotation_interval`,
    `rotation_pattern`, `rotation_direction`, `auto_rotate`, `rotate_interval_days`,
    `rest_days_per_cycle`, `continuous_work_days_max`,
    `effective_date`, `auto_generate_schedule`,
    `rotation_status`, `total_employees`, `remark`
) VALUES (
    'ROTATION_003', '安保部两班倒', 'FIXED', 'CUSTOM',
    'DAY', 7, 2, '[201,202]', '["白班","夜班"]',
    12.00, 2, 1, 7,
    '{"pattern":"白班→夜班→休息→白班","cycle_days":3}', 'FORWARD', 1, 1,
    1, 6,
    '2025-01-01', 1,
    'ACTIVE', 20, '安保部24小时值班,两班倒制度,每班12小时,每3天一个周期'
);

-- 4. 医院护士三班倒轮班规则
INSERT INTO `t_attendance_rotation_rule` (
    `rotation_code`, `rotation_name`, `rotation_type`, `rotation_mode`,
    `cycle_type`, `cycle_length`, `shift_count`, `shift_ids`, `shift_sequence`,
    `shift_duration_hours`, `group_count`, `group_rotation_enabled`, `group_rotation_interval`,
    `rotation_pattern`, `rotation_direction`, `auto_rotate`,
    `rest_days_per_cycle`, `continuous_work_days_max`, `continuous_rest_days_min`,
    `handover_enabled`, `handover_duration_minutes`,
    `effective_date`, `auto_generate_schedule`,
    `rotation_status`, `total_employees`, `remark`
) VALUES (
    'ROTATION_004', '医院护士三班倒', 'FIXED', 'THREE_SHIFT_TWO',
    'DAY', 7, 3, '[201,202,203]', '["白班","小夜班","大夜班"]',
    8.00, 2, 1, 7,
    '{"pattern":"白班→小夜班→大夜班→休息→白班","cycle_days":4}', 'FORWARD', 1,
    1, 6, 1,
    1, 30,
    '2025-01-01', 1,
    'ACTIVE', 50, '医院护士24小时值班,三班倒制度,每班8小时,交接班30分钟'
);

-- 5. 呼叫中心弹性轮班规则
INSERT INTO `t_attendance_rotation_rule` (
    `rotation_code`, `rotation_name`, `rotation_type`, `rotation_mode`,
    `cycle_type`, `cycle_length`, `shift_count`, `shift_ids`, `shift_sequence`,
    `shift_duration_hours`, `group_count`, `rotation_direction`, `auto_rotate`,
    `rest_days_per_cycle`, `allow_shift_swap`, `swap_approval_required`,
    `effective_date`, `auto_generate_schedule`, `generate_days_ahead`,
    `rotation_status`, `total_employees`, `remark`
) VALUES (
    'ROTATION_005', '呼叫中心弹性轮班', 'FLEXIBLE', 'CUSTOM',
    'WEEK', 1, 4, '[201,202,203,204]', '["早班","中班","晚班","夜班"]',
    6.00, 4, 'RANDOM', 1,
    2, 1, 1,
    '2025-01-01', 1, 30,
    'ACTIVE', 60, '呼叫中心弹性轮班制度,支持员工自主调班,每周轮换一次'
);

-- 6. 工厂夜班轮班规则
INSERT INTO `t_attendance_rotation_rule` (
    `rotation_code`, `rotation_name`, `rotation_type`, `rotation_mode`,
    `cycle_type`, `cycle_length`, `shift_count`, `shift_ids`, `shift_sequence`,
    `shift_duration_hours`, `group_count`, `group_rotation_enabled`, `group_rotation_interval`,
    `rotation_pattern`, `rotation_direction`, `auto_rotate`,
    `rest_days_per_cycle`, `continuous_work_days_max`,
    `overtime_enabled`, `max_overtime_hours_per_day`,
    `effective_date`, `auto_generate_schedule`,
    `rotation_status`, `total_employees`, `remark`
) VALUES (
    'ROTATION_006', '工厂夜班轮班', 'FIXED', 'CUSTOM',
    'DAY', 14, 2, '[201,202]', '["白班","夜班"]',
    10.00, 2, 1, 14,
    '{"pattern":"白班→夜班→休息→白班","cycle_days":3}', 'FORWARD', 1,
    2, 6,
    1, 3.00,
    '2025-01-01', 1,
    'ACTIVE', 35, '工厂夜班轮班制度,两班倒,每班10小时,每14天轮换一次'
);

-- 7. 物流中心周末轮班规则
INSERT INTO `t_attendance_rotation_rule` (
    `rotation_code`, `rotation_name`, `rotation_type`, `rotation_mode`,
    `cycle_type`, `cycle_length`, `shift_count`, `shift_ids`, `shift_sequence`,
    `shift_duration_hours`, `group_count`, `rotation_direction`, `auto_rotate`,
    `rest_day_pattern`, `continuous_work_days_max`,
    `holiday_adjustment_enabled`, `holiday_rotation_paused`,
    `effective_date`, `auto_generate_schedule`,
    `rotation_status`, `total_employees`, `remark`
) VALUES (
    'ROTATION_007', '物流中心周末轮班', 'FIXED', 'CUSTOM',
    'WEEK', 1, 2, '[201,202]', '["早班","晚班"]',
    8.00, 2, 'FORWARD', 1,
    '{"weekend_rotation":true,"pattern":"每两周轮换一次周末班"}', 6,
    1, 1,
    '2025-01-01', 1,
    'ACTIVE', 25, '物流中心周末也需要轮班,每两周轮换一次周末班'
);

-- 8. 轮班模板(可复用)
INSERT INTO `t_attendance_rotation_rule` (
    `rotation_code`, `rotation_name`, `rotation_type`, `rotation_mode`,
    `cycle_type`, `cycle_length`, `shift_count`, `shift_ids`, `shift_sequence`,
    `shift_duration_hours`, `group_count`, `rotation_direction`, `auto_rotate`,
    `rest_days_per_cycle`, `continuous_work_days_max`,
    `is_template`, `rotation_status`, `remark`
) VALUES (
    'TEMPLATE_001', '标准三班两倒模板', 'FIXED', 'THREE_SHIFT_TWO',
    'DAY', 7, 3, '[0,0,0]', '["早班","中班","晚班"]',
    8.00, 2, 'FORWARD', 1,
    1, 6,
    1, 'DRAFT', '标准三班两倒轮班模板,可以复制使用并配置具体的班次ID'
);

-- ==================================================================
-- 使用示例查询SQL
-- ==================================================================

-- 示例1: 查询所有生效的轮班规则
-- SELECT * FROM t_attendance_rotation_rule
-- WHERE rotation_status = 'ACTIVE'
--   AND deleted_flag = 0
--   AND effective_date <= CURDATE()
--   AND (expiry_date IS NULL OR expiry_date >= CURDATE())
-- ORDER BY priority DESC;

-- 示例2: 查询三班倒轮班规则
-- SELECT * FROM t_attendance_rotation_rule
-- WHERE rotation_mode = 'THREE_SHIFT_TWO'
--   AND rotation_status = 'ACTIVE'
--   AND deleted_flag = 0;

-- 示例3: 查询需要自动生成排班的轮班规则
-- SELECT * FROM t_attendance_rotation_rule
-- WHERE auto_generate_schedule = 1
--   AND rotation_status = 'ACTIVE'
--   AND deleted_flag = 0;

-- 示例4: 查询某部门适用的轮班规则
-- SELECT * FROM t_attendance_rotation_rule
-- WHERE (department_ids IS NULL OR JSON_CONTAINS(department_ids, '101'))
--   AND rotation_status = 'ACTIVE'
--   AND deleted_flag = 0;

-- 示例5: 查询需要发送通知的轮班规则
-- SELECT * FROM t_attendance_rotation_rule
-- WHERE notification_enabled = 1
--   AND notify_on_rotation_change = 1
--   AND rotation_status = 'ACTIVE'
--   AND deleted_flag = 0;

-- 示例6: 统计各轮班模式的使用情况
-- SELECT rotation_mode, COUNT(*) as rule_count, SUM(total_employees) as total_employees
-- FROM t_attendance_rotation_rule
-- WHERE rotation_status = 'ACTIVE'
--   AND deleted_flag = 0
-- GROUP BY rotation_mode;

-- 示例7: 查询轮班模板
-- SELECT * FROM t_attendance_rotation_rule
-- WHERE is_template = 1
--   AND deleted_flag = 0;

-- 示例8: 查询允许调班的轮班规则
-- SELECT * FROM t_attendance_rotation_rule
-- WHERE allow_shift_swap = 1
--   AND rotation_status = 'ACTIVE'
--   AND deleted_flag = 0;

-- ==================================================================
-- 维护建议
-- ==================================================================
-- 1. 定期检查轮班规则的有效性,及时调整不合理的配置
-- 2. 监控轮班规则的执行情况,统计员工满意度
-- 3. 优化轮班周期,平衡员工工作和休息
-- 4. 及时处理员工的调班申请,保证排班灵活性
-- 5. 定期审查轮班规则对考勤的影响,优化考勤管理

-- ==================================================================
-- 定时任务建议
-- ==================================================================
-- 1. 每天凌晨自动生成未来30天的轮班排班
-- 2. 每周统计轮班执行情况,生成报表
-- 3. 每月评估轮班规则的合理性,提供优化建议
-- 4. 实时监控轮班变更,发送通知给相关员工
-- 5. 定期检查轮班规则是否即将过期,提前预警
-- 6. 自动处理节假日期间的轮班调整

-- ==================================================================
-- 注意事项
-- ==================================================================
-- 1. 轮班规则的生效日期必须明确,避免排班混乱
-- 2. 轮班周期需要考虑员工的休息需求,避免过度疲劳
-- 3. 交接班时间需要合理安排,确保工作连续性
-- 4. 允许调班时需要设置合理的限制,避免频繁调班
-- 5. 节假日期间的轮班需要特殊处理,遵守劳动法规定
-- 6. 轮班规则变更需要提前通知员工,给予充分准备时间
-- 7. 自动生成排班时需要考虑员工的特殊情况(请假、出差等)
-- 8. 轮班规则需要与考勤规则、加班规则协同配置

-- ==================================================================
-- 版本历史
-- ==================================================================
-- v1.0.0 - 2025-12-08
--   - 初始版本
--   - 支持三班倒、四班三倒、自定义轮班模式
--   - 支持自动轮班和手动轮班
--   - 支持交接班配置
--   - 支持调班规则配置
--   - 支持节假日处理
--   - 支持排班自动生成
--   - 支持通知提醒功能
--   - 初始化8条企业级轮班规则示例

-- ==================================================================
-- 文件结束
-- ==================================================================
