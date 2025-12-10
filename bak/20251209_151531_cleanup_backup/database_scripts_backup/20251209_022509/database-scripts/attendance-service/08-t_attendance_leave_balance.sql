-- ==================================================================
-- 考勤服务 - 年假余额表初始化脚本
-- ==================================================================
-- 功能说明: 管理员工年假余额,包括年假额度、使用情况、结转等
-- 表名: t_attendance_leave_balance
-- 依赖表: t_common_user (用户表), t_attendance_leave_type (请假类型表)
-- 作者: IOE-DREAM团队
-- 创建时间: 2025-01-08
-- 版本: v1.0.0
-- ==================================================================

-- 设置字符集和时区
SET NAMES utf8mb4;
SET TIME_ZONE = '+08:00';

-- 如果表存在则删除(开发环境)
-- 生产环境请注释此行
DROP TABLE IF EXISTS `t_attendance_leave_balance`;

-- ==================================================================
-- 表结构定义
-- ==================================================================

CREATE TABLE IF NOT EXISTS `t_attendance_leave_balance` (
    -- ============================================================
    -- 主键和唯一标识
    -- ============================================================
    `balance_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '年假余额ID(主键)',
    `user_id` BIGINT NOT NULL COMMENT '员工用户ID(关联t_common_user.user_id)',
    `leave_type_id` BIGINT NOT NULL COMMENT '请假类型ID(关联t_attendance_leave_type.leave_type_id)',
    
    -- ============================================================
    -- 年度管理
    -- ============================================================
    `balance_year` INT NOT NULL COMMENT '年假年度(如2025)',
    `effective_date` DATE NOT NULL COMMENT '生效日期(该年度年假开始生效的日期)',
    `expiry_date` DATE NOT NULL COMMENT '失效日期(该年度年假过期的日期)',
    
    -- ============================================================
    -- 年假额度管理
    -- ============================================================
    `total_days` DECIMAL(8,2) NOT NULL DEFAULT 0.00 COMMENT '总额度天数(该年度可用的总年假天数)',
    `initial_days` DECIMAL(8,2) NOT NULL DEFAULT 0.00 COMMENT '初始额度天数(该年度系统分配的年假天数)',
    `carried_forward_days` DECIMAL(8,2) NOT NULL DEFAULT 0.00 COMMENT '结转天数(从上一年度结转的年假天数)',
    `adjustment_days` DECIMAL(8,2) NOT NULL DEFAULT 0.00 COMMENT '调整天数(手动调整的年假天数,可正可负)',
    `bonus_days` DECIMAL(8,2) NOT NULL DEFAULT 0.00 COMMENT '奖励天数(因优秀表现等原因奖励的年假天数)',
    
    -- ============================================================
    -- 年假使用情况
    -- ============================================================
    `used_days` DECIMAL(8,2) NOT NULL DEFAULT 0.00 COMMENT '已使用天数(已批准并使用的年假天数)',
    `pending_days` DECIMAL(8,2) NOT NULL DEFAULT 0.00 COMMENT '待审批天数(已申请但未批准的年假天数)',
    `frozen_days` DECIMAL(8,2) NOT NULL DEFAULT 0.00 COMMENT '冻结天数(因特殊情况冻结的年假天数)',
    `available_days` DECIMAL(8,2) NOT NULL DEFAULT 0.00 COMMENT '可用天数(当前可以申请的年假天数 = 总额度 - 已使用 - 待审批 - 冻结)',
    
    -- ============================================================
    -- 额度计算规则
    -- ============================================================
    `calculation_method` VARCHAR(50) NOT NULL DEFAULT 'SENIORITY' COMMENT '计算方式:SENIORITY-工龄计算,POSITION-职位计算,CONTRACT-合同约定,CUSTOM-自定义',
    `work_years` INT COMMENT '工龄(年)',
    `days_per_year` DECIMAL(5,2) COMMENT '每年天数(根据工龄每年可获得的年假天数)',
    `calculation_formula` VARCHAR(200) COMMENT '计算公式(年假天数计算公式描述)',
    
    -- ============================================================
    -- 结转规则
    -- ============================================================
    `carry_forward_enabled` TINYINT NOT NULL DEFAULT 0 COMMENT '是否允许结转:1-允许,0-不允许',
    `carry_forward_limit_days` DECIMAL(8,2) COMMENT '结转限额天数(最多允许结转的天数)',
    `carry_forward_expiry_months` INT COMMENT '结转有效期月数(结转年假的有效期)',
    `carry_forward_rule` VARCHAR(200) COMMENT '结转规则描述',
    
    -- ============================================================
    -- 使用限制规则
    -- ============================================================
    `min_apply_days` DECIMAL(5,2) NOT NULL DEFAULT 0.50 COMMENT '最小申请天数(每次最少申请的年假天数,如0.5天)',
    `max_apply_days` DECIMAL(5,2) NOT NULL DEFAULT 15.00 COMMENT '最大申请天数(每次最多申请的年假天数,如15天)',
    `max_consecutive_days` DECIMAL(5,2) COMMENT '最大连续天数(年假最多允许连续请多少天)',
    `max_times_per_year` INT COMMENT '年度最大请假次数(一年内最多可以请年假的次数)',
    `used_times` INT NOT NULL DEFAULT 0 COMMENT '已使用次数(当年已经请年假的次数)',
    
    -- ============================================================
    -- 状态管理
    -- ============================================================
    `balance_status` VARCHAR(50) NOT NULL DEFAULT 'ACTIVE' COMMENT '余额状态:ACTIVE-激活,FROZEN-冻结,EXPIRED-已过期,CANCELLED-已作废',
    `is_expired` TINYINT NOT NULL DEFAULT 0 COMMENT '是否过期:1-已过期,0-未过期',
    `expired_days` DECIMAL(8,2) NOT NULL DEFAULT 0.00 COMMENT '过期天数(已过期未使用的年假天数)',
    
    -- ============================================================
    -- 自动处理配置
    -- ============================================================
    `auto_carry_forward` TINYINT NOT NULL DEFAULT 0 COMMENT '是否自动结转:1-自动,0-手动',
    `auto_expiry_process` TINYINT NOT NULL DEFAULT 1 COMMENT '是否自动过期处理:1-自动,0-手动',
    `auto_calculation` TINYINT NOT NULL DEFAULT 1 COMMENT '是否自动计算:1-自动重新计算额度,0-手动',
    
    -- ============================================================
    -- 历史记录追踪
    -- ============================================================
    `last_used_date` DATE COMMENT '最后使用日期(最近一次使用年假的日期)',
    `last_adjustment_date` DATE COMMENT '最后调整日期(最近一次手动调整年假额度的日期)',
    `last_calculation_time` DATETIME COMMENT '最后计算时间(最近一次自动计算年假额度的时间)',
    
    -- ============================================================
    -- 审批信息
    -- ============================================================
    `approved_by` BIGINT COMMENT '审批人用户ID(额度调整的审批人)',
    `approved_time` DATETIME COMMENT '审批时间',
    `approval_remark` VARCHAR(500) COMMENT '审批备注',
    
    -- ============================================================
    -- 关联记录
    -- ============================================================
    `related_balance_ids` JSON COMMENT '关联余额记录ID(JSON数组):历史年度的余额记录ID',
    `related_exception_ids` JSON COMMENT '关联异常申请ID(JSON数组):使用该余额的异常申请记录ID',
    
    -- ============================================================
    -- 备注和扩展
    -- ============================================================
    `remark` VARCHAR(500) COMMENT '备注信息',
    `extended_attributes` JSON COMMENT '扩展属性(JSON格式):存储业务特定的额外字段',
    
    -- ============================================================
    -- 统计和分析
    -- ============================================================
    `usage_rate` DECIMAL(5,2) COMMENT '使用率(%):已使用天数/总额度天数*100',
    `carry_forward_rate` DECIMAL(5,2) COMMENT '结转率(%):结转天数/总额度天数*100',
    `expiry_rate` DECIMAL(5,2) COMMENT '过期率(%):过期天数/总额度天数*100',
    
    -- ============================================================
    -- 标准审计字段
    -- ============================================================
    `version` INT NOT NULL DEFAULT 1 COMMENT '版本号(用于乐观锁)',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人用户ID',
    `update_user_id` BIGINT COMMENT '更新人用户ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记:0-未删除,1-已删除',
    
    -- ============================================================
    -- 主键和索引定义
    -- ============================================================
    PRIMARY KEY (`balance_id`),
    
    -- 唯一索引:确保每个用户每年每种假期类型只有一条余额记录
    UNIQUE KEY `uk_user_year_type` (`user_id`, `balance_year`, `leave_type_id`, `deleted_flag`),
    
    -- 普通索引:支持高效查询
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_leave_type_id` (`leave_type_id`),
    INDEX `idx_balance_year` (`balance_year`),
    INDEX `idx_balance_status` (`balance_status`),
    INDEX `idx_is_expired` (`is_expired`),
    INDEX `idx_effective_date` (`effective_date`),
    INDEX `idx_expiry_date` (`expiry_date`),
    
    -- 复合索引:优化常用查询条件组合
    INDEX `idx_user_year_status` (`user_id`, `balance_year`, `balance_status`),
    INDEX `idx_type_year_status` (`leave_type_id`, `balance_year`, `balance_status`),
    INDEX `idx_year_status_expired` (`balance_year`, `balance_status`, `is_expired`),
    
    -- 降序索引:优化时间倒序查询
    INDEX `idx_create_time` (`create_time` DESC),
    INDEX `idx_update_time` (`update_time` DESC),
    INDEX `idx_last_used_date` (`last_used_date` DESC),
    
    -- 逻辑删除索引
    INDEX `idx_deleted_flag` (`deleted_flag`)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤服务-年假余额表';

-- ==================================================================
-- 初始化数据
-- ==================================================================

INSERT INTO `t_attendance_leave_balance` (
    `user_id`,
    `leave_type_id`,
    `balance_year`,
    `effective_date`,
    `expiry_date`,
    `total_days`,
    `initial_days`,
    `carried_forward_days`,
    `adjustment_days`,
    `bonus_days`,
    `used_days`,
    `pending_days`,
    `frozen_days`,
    `available_days`,
    `calculation_method`,
    `work_years`,
    `days_per_year`,
    `calculation_formula`,
    `carry_forward_enabled`,
    `carry_forward_limit_days`,
    `carry_forward_expiry_months`,
    `carry_forward_rule`,
    `min_apply_days`,
    `max_apply_days`,
    `max_consecutive_days`,
    `max_times_per_year`,
    `used_times`,
    `balance_status`,
    `is_expired`,
    `expired_days`,
    `auto_carry_forward`,
    `auto_expiry_process`,
    `auto_calculation`,
    `last_used_date`,
    `last_adjustment_date`,
    `last_calculation_time`,
    `usage_rate`,
    `carry_forward_rate`,
    `expiry_rate`,
    `remark`
) VALUES
-- 示例1: 张三的2025年年假余额(工作5年,初始10天)
(1, 1, 2025, '2025-01-01', '2025-12-31', 13.00, 10.00, 3.00, 0.00, 0.00,
 5.00, 2.00, 0.00, 6.00, 'SENIORITY', 5, 2.00, '工作1-5年每年10天,每增加1年增加2天',
 1, 5.00, 6, '上年度未使用年假可结转,最多5天,结转年假需在6个月内使用',
 0.50, 15.00, 10.00, 4, 2, 'ACTIVE', 0, 0.00, 1, 1, 1,
 '2025-06-15', NULL, '2025-01-01 00:00:00', 38.46, 23.08, 0.00, '工作5年员工,有结转年假3天'),

-- 示例2: 李四的2025年年假余额(工作2年,初始7天)
(2, 1, 2025, '2025-01-01', '2025-12-31', 7.00, 7.00, 0.00, 0.00, 0.00,
 2.50, 1.00, 0.00, 3.50, 'SENIORITY', 2, 3.50, '工作1-3年每年7天',
 0, 0.00, 0, '工作3年以下员工不允许年假结转',
 0.50, 7.00, 7.00, 3, 1, 'ACTIVE', 0, 0.00, 0, 1, 1,
 '2025-03-20', NULL, '2025-01-01 00:00:00', 35.71, 0.00, 0.00, '工作2年员工,无结转年假'),

-- 示例3: 王五的2025年年假余额(工作10年,高级管理层,额外奖励2天)
(3, 1, 2025, '2025-01-01', '2025-12-31', 22.00, 15.00, 5.00, 0.00, 2.00,
 8.00, 3.00, 0.00, 11.00, 'SENIORITY', 10, 1.50, '工作5-10年每年15天,每增加1年增加1.5天',
 1, 10.00, 12, '上年度未使用年假可结转,最多10天,结转年假需在12个月内使用',
 0.50, 20.00, 15.00, 5, 3, 'ACTIVE', 0, 0.00, 1, 1, 1,
 '2025-05-10', '2025-01-15', '2025-01-01 00:00:00', 36.36, 22.73, 0.00, '工作10年高级管理层,有奖励年假2天'),

-- 示例4: 赵六的2025年年假余额(新入职员工,按月折算)
(4, 1, 2025, '2025-03-01', '2025-12-31', 5.83, 5.83, 0.00, 0.00, 0.00,
 0.00, 0.00, 0.00, 5.83, 'CUSTOM', 0, 7.00, '入职当年按月折算:7天/12月×10月=5.83天',
 0, 0.00, 0, '新员工当年年假不允许结转',
 0.50, 5.83, 5.00, 2, 0, 'ACTIVE', 0, 0.00, 0, 1, 1,
 NULL, NULL, '2025-03-01 00:00:00', 0.00, 0.00, 0.00, '2025年3月入职,按月折算年假'),

-- 示例5: 孙七的2024年年假余额(已过期)
(5, 1, 2024, '2024-01-01', '2024-12-31', 10.00, 10.00, 0.00, 0.00, 0.00,
 7.00, 0.00, 0.00, 0.00, 'SENIORITY', 3, 3.33, '工作1-3年每年10天',
 0, 0.00, 0, '工作3年以下员工不允许年假结转',
 0.50, 10.00, 10.00, 3, 3, 'EXPIRED', 1, 3.00, 0, 1, 1,
 '2024-11-20', NULL, '2024-01-01 00:00:00', 70.00, 0.00, 30.00, '2024年年假已过期,剩余3天未使用'),

-- 示例6: 周八的2025年年假余额(特殊情况冻结2天)
(6, 1, 2025, '2025-01-01', '2025-12-31', 12.00, 10.00, 2.00, 0.00, 0.00,
 4.00, 1.50, 2.00, 4.50, 'SENIORITY', 6, 1.67, '工作5-10年每年12天',
 1, 5.00, 6, '上年度未使用年假可结转,最多5天,结转年假需在6个月内使用',
 0.50, 12.00, 10.00, 4, 2, 'ACTIVE', 0, 0.00, 1, 1, 1,
 '2025-04-10', NULL, '2025-01-01 00:00:00', 33.33, 16.67, 0.00, '因绩效考核未通过,冻结2天年假'),

-- 示例7: 吴九的2025年年假余额(合同约定特殊额度)
(7, 1, 2025, '2025-01-01', '2025-12-31', 20.00, 20.00, 0.00, 0.00, 0.00,
 6.00, 2.50, 0.00, 11.50, 'CONTRACT', 8, 2.50, '合同约定每年20天年假',
 1, 8.00, 12, '上年度未使用年假可结转,最多8天,结转年假需在12个月内使用',
 0.50, 15.00, 15.00, 6, 2, 'ACTIVE', 0, 0.00, 1, 1, 1,
 '2025-05-25', NULL, '2025-01-01 00:00:00', 30.00, 0.00, 0.00, '高级技术专家,合同约定特殊年假额度'),

-- 示例8: 郑十的2025年年假余额(手动调整增加3天)
(8, 1, 2025, '2025-01-01', '2025-12-31', 13.00, 10.00, 0.00, 3.00, 0.00,
 3.00, 1.00, 0.00, 9.00, 'SENIORITY', 4, 2.50, '工作1-5年每年10天',
 0, 0.00, 0, '工作5年以下员工不允许年假结转',
 0.50, 13.00, 10.00, 4, 1, 'ACTIVE', 0, 0.00, 0, 1, 1,
 '2025-02-15', '2025-01-20', '2025-01-01 00:00:00', 23.08, 0.00, 0.00, '因项目加班较多,手动调整增加3天年假'),

-- 示例9: 陈十一的2025年年假余额(职位计算-部门经理)
(9, 1, 2025, '2025-01-01', '2025-12-31', 15.00, 15.00, 0.00, 0.00, 0.00,
 5.50, 2.00, 0.00, 7.50, 'POSITION', 7, 2.14, '部门经理级别每年15天',
 1, 7.00, 12, '上年度未使用年假可结转,最多7天,结转年假需在12个月内使用',
 0.50, 15.00, 12.00, 5, 2, 'ACTIVE', 0, 0.00, 1, 1, 1,
 '2025-06-05', NULL, '2025-01-01 00:00:00', 36.67, 0.00, 0.00, '部门经理职位,按职位计算年假额度'),

-- 示例10: 林十二的2025年年假余额(结转额度达到上限)
(10, 1, 2025, '2025-01-01', '2025-12-31', 15.00, 10.00, 5.00, 0.00, 0.00,
 7.00, 1.50, 0.00, 6.50, 'SENIORITY', 5, 2.00, '工作1-5年每年10天,每增加1年增加2天',
 1, 5.00, 6, '上年度未使用年假可结转,最多5天,结转年假需在6个月内使用',
 0.50, 15.00, 10.00, 4, 3, 'ACTIVE', 0, 0.00, 1, 1, 1,
 '2025-07-10', NULL, '2025-01-01 00:00:00', 46.67, 33.33, 0.00, '上年度结转5天,达到结转上限'),

-- 示例11: 黄十三的2025年年假余额(临时工,自定义额度)
(11, 1, 2025, '2025-01-01', '2025-12-31', 5.00, 5.00, 0.00, 0.00, 0.00,
 2.00, 0.50, 0.00, 2.50, 'CUSTOM', 1, 5.00, '临时工每年5天年假',
 0, 0.00, 0, '临时工年假不允许结转',
 0.50, 5.00, 5.00, 2, 1, 'ACTIVE', 0, 0.00, 0, 1, 1,
 '2025-03-15', NULL, '2025-01-01 00:00:00', 40.00, 0.00, 0.00, '临时工自定义年假额度'),

-- 示例12: 刘十四的2025年年假余额(已全部使用完毕)
(12, 1, 2025, '2025-01-01', '2025-12-31', 10.00, 10.00, 0.00, 0.00, 0.00,
 10.00, 0.00, 0.00, 0.00, 'SENIORITY', 3, 3.33, '工作1-3年每年10天',
 0, 0.00, 0, '工作3年以下员工不允许年假结转',
 0.50, 10.00, 10.00, 3, 4, 'ACTIVE', 0, 0.00, 0, 1, 1,
 '2025-08-20', NULL, '2025-01-01 00:00:00', 100.00, 0.00, 0.00, '2025年年假已全部使用完毕');

-- ==================================================================
-- 索引优化说明
-- ==================================================================

-- 1. uk_user_year_type: 唯一索引,确保每个用户每年每种假期类型只有一条余额记录
--    - 防止数据重复
--    - 保证业务逻辑正确性
--    - 支持快速去重查询

-- 2. idx_user_id: 按用户ID查询
--    - 查询某个用户的所有年假余额记录
--    - 用户维度的年假统计分析

-- 3. idx_leave_type_id: 按请假类型查询
--    - 查询某种请假类型的所有余额记录
--    - 假期类型维度的统计分析

-- 4. idx_balance_year: 按年度查询
--    - 查询某年度的所有年假余额记录
--    - 年度维度的统计分析

-- 5. idx_balance_status: 按状态筛选
--    - 筛选激活/冻结/过期的年假余额
--    - 状态管理和监控

-- 6. idx_is_expired: 按过期状态筛选
--    - 筛选已过期/未过期的年假余额
--    - 过期处理和提醒

-- 7. idx_effective_date, idx_expiry_date: 日期范围查询
--    - 查询某日期范围内生效/失效的年假余额
--    - 时间维度的余额管理

-- 8. idx_user_year_status: 复合索引,优化多条件查询
--    - 查询某用户某年度某状态的年假余额
--    - 常用查询组合优化

-- 9. idx_type_year_status: 复合索引,优化多条件查询
--    - 查询某假期类型某年度某状态的余额记录
--    - 假期类型维度的统计分析

-- 10. idx_year_status_expired: 复合索引,优化多条件查询
--     - 查询某年度某状态是否过期的余额记录
--     - 年度过期余额批处理

-- 11. idx_create_time, idx_update_time: 降序索引,时间倒序查询
--     - 按创建时间或更新时间倒序查询
--     - 审计追踪和数据分析

-- 12. idx_last_used_date: 降序索引,最后使用日期倒序查询
--     - 按最后使用日期倒序查询
--     - 使用频率分析

-- 13. idx_deleted_flag: 逻辑删除索引
--     - 过滤已删除记录
--     - 提升查询性能

-- ==================================================================
-- 使用示例
-- ==================================================================

-- 示例1: 查询某用户2025年的年假余额
-- SELECT * FROM t_attendance_leave_balance
-- WHERE user_id = 1
--   AND balance_year = 2025
--   AND deleted_flag = 0;

-- 示例2: 查询所有激活状态的年假余额(可用天数>0)
-- SELECT user_id, balance_year, total_days, used_days, available_days
-- FROM t_attendance_leave_balance
-- WHERE balance_status = 'ACTIVE'
--   AND available_days > 0
--   AND deleted_flag = 0
-- ORDER BY available_days DESC;

-- 示例3: 查询即将过期的年假余额(有剩余且30天内过期)
-- SELECT user_id, balance_year, available_days, expiry_date
-- FROM t_attendance_leave_balance
-- WHERE balance_status = 'ACTIVE'
--   AND available_days > 0
--   AND expiry_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 30 DAY)
--   AND deleted_flag = 0
-- ORDER BY expiry_date ASC;

-- 示例4: 统计2025年年假使用情况
-- SELECT 
--     COUNT(*) AS total_users,
--     SUM(total_days) AS total_quota,
--     SUM(used_days) AS total_used,
--     SUM(available_days) AS total_available,
--     AVG(usage_rate) AS avg_usage_rate,
--     SUM(expired_days) AS total_expired
-- FROM t_attendance_leave_balance
-- WHERE balance_year = 2025
--   AND deleted_flag = 0;

-- 示例5: 查询有结转年假的员工
-- SELECT user_id, balance_year, carried_forward_days, total_days, available_days
-- FROM t_attendance_leave_balance
-- WHERE carried_forward_days > 0
--   AND balance_year = 2025
--   AND deleted_flag = 0
-- ORDER BY carried_forward_days DESC;

-- ==================================================================
-- 维护建议
-- ==================================================================

-- 1. 定期检查过期年假余额,自动将is_expired设置为1
-- 2. 定期执行年假结转任务,从上一年度结转未使用的年假到新年度
-- 3. 定期重新计算available_days,确保数据准确性
-- 4. 每年初自动创建新年度的年假余额记录
-- 5. 定期清理过期年份的年假余额记录(保留3-5年历史数据)
-- 6. 监控年假使用率,分析员工年假使用情况
-- 7. 对于长期未使用年假的员工,发送提醒通知
-- 8. 定期备份年假余额数据,防止数据丢失

-- ==================================================================
-- 定时任务建议
-- ==================================================================

-- 1. 年假余额过期检查(每日凌晨2:00)
--    - 检查expiry_date < CURDATE()的记录
--    - 更新is_expired = 1, balance_status = 'EXPIRED'
--    - 计算expired_days = available_days

-- 2. 年假余额结转任务(每年1月1日凌晨3:00)
--    - 从上一年度结转未使用的年假到新年度
--    - 根据carry_forward_limit_days限制结转天数
--    - 创建新年度的年假余额记录

-- 3. 年假余额自动计算(每月1日凌晨4:00)
--    - 根据员工工龄、职位等重新计算年假额度
--    - 更新total_days, initial_days等字段
--    - 重新计算available_days

-- 4. 年假余额提醒通知(每日上午9:00)
--    - 提醒即将过期的年假(30天内)
--    - 提醒年假使用率低的员工(低于30%)
--    - 提醒年假额度不足的员工(少于2天)

-- 5. 年假余额统计报表(每周一上午10:00)
--    - 生成年假使用情况统计报表
--    - 分析年假结转情况
--    - 分析年假过期情况

-- 6. 年假余额数据清理(每季度第一天凌晨5:00)
--    - 清理5年前的年假余额记录
--    - 归档历史数据到备份表
--    - 优化索引和表结构

-- 7. 年假余额一致性检查(每周日凌晨6:00)
--    - 检查available_days = total_days - used_days - pending_days - frozen_days
--    - 检查total_days = initial_days + carried_forward_days + adjustment_days + bonus_days
--    - 发现不一致数据自动修复或告警

-- ==================================================================
-- 数据完整性约束建议
-- ==================================================================

-- 1. total_days = initial_days + carried_forward_days + adjustment_days + bonus_days
-- 2. available_days = total_days - used_days - pending_days - frozen_days
-- 3. used_days >= 0 AND used_days <= total_days
-- 4. pending_days >= 0 AND pending_days <= (total_days - used_days)
-- 5. frozen_days >= 0 AND frozen_days <= (total_days - used_days)
-- 6. available_days >= 0 AND available_days <= total_days
-- 7. carried_forward_days <= carry_forward_limit_days (如果有限制)
-- 8. expiry_date > effective_date
-- 9. balance_year >= YEAR(effective_date) AND balance_year <= YEAR(expiry_date)

-- ==================================================================
-- 性能优化建议
-- ==================================================================

-- 1. 使用分区表按balance_year分区,提升查询性能
--    ALTER TABLE t_attendance_leave_balance 
--    PARTITION BY RANGE (balance_year) (
--        PARTITION p2024 VALUES LESS THAN (2025),
--        PARTITION p2025 VALUES LESS THAN (2026),
--        PARTITION p2026 VALUES LESS THAN (2027),
--        PARTITION p_future VALUES LESS THAN MAXVALUE
--    );

-- 2. 对于高频查询字段,考虑使用覆盖索引减少回表
--    CREATE INDEX idx_cover_user_year ON t_attendance_leave_balance
--    (user_id, balance_year, balance_status, available_days);

-- 3. 定期分析表和索引使用情况,优化索引
--    ANALYZE TABLE t_attendance_leave_balance;

-- 4. 对于历史数据(3年前),考虑归档到历史表
--    CREATE TABLE t_attendance_leave_balance_history LIKE t_attendance_leave_balance;

-- 5. 使用Redis缓存热点数据(当前年度的年假余额)
--    缓存key: leave_balance:user:{user_id}:year:{year}
--    缓存过期时间: 24小时

-- ==================================================================
-- 脚本结束
-- ==================================================================
