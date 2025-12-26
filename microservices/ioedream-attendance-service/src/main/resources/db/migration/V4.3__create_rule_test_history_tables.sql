-- ============================================================
-- P2-1: 规则测试历史记录功能 - 数据库表结构
-- 版本: V4.3
-- 创建时间: 2025-12-26
-- 说明: 测试历史记录表，用于保存和查询规则测试执行历史
-- ============================================================

-- ============================================================
-- 表1: 规则测试历史表 (t_attendance_rule_test_history)
-- 说明: 存储规则测试的执行历史记录，支持结果追溯和对比分析
-- ============================================================
CREATE TABLE IF NOT EXISTS t_attendance_rule_test_history (
    history_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '历史ID',
    test_id VARCHAR(100) NOT NULL COMMENT '测试ID (唯一标识，如test-20251226-001)',

    -- 规则信息
    rule_id BIGINT NOT NULL COMMENT '规则ID',
    rule_name VARCHAR(200) NOT NULL COMMENT '规则名称',
    rule_condition TEXT COMMENT '规则条件 (JSON格式)',
    rule_action TEXT COMMENT '规则动作 (JSON格式)',

    -- 测试结果
    test_result VARCHAR(50) NOT NULL COMMENT '测试结果: MATCH-匹配, NOT_MATCH-不匹配, ERROR-错误',
    result_message VARCHAR(500) COMMENT '测试结果描述',
    condition_matched TINYINT COMMENT '条件是否匹配 0-否 1-是',
    execution_time BIGINT COMMENT '执行时长(毫秒)',

    -- 执行详情 (JSON格式)
    executed_actions TEXT COMMENT '执行的动作列表 (JSON数组)',
    execution_logs TEXT COMMENT '执行日志列表 (JSON数组)',
    error_message TEXT COMMENT '错误信息',

    -- 测试数据 (JSON格式)
    test_input_data TEXT COMMENT '测试输入数据 (JSON对象)',
    test_output_data TEXT COMMENT '测试输出数据 (JSON对象)',

    -- 测试场景
    test_scenario VARCHAR(50) COMMENT '测试场景: LATE-迟到, EARLY-早退, OVERTIME-加班, ABSENT-缺勤, NORMAL-正常',

    -- 测试用户
    test_user_id BIGINT COMMENT '测试用户ID',
    test_user_name VARCHAR(100) COMMENT '测试用户名',

    -- 时间戳
    test_timestamp DATETIME NOT NULL COMMENT '测试时间戳',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    -- 索引
    INDEX idx_test_id (test_id),
    INDEX idx_rule_id (rule_id),
    INDEX idx_test_user (test_user_id),
    INDEX idx_test_result (test_result),
    INDEX idx_test_timestamp (test_timestamp),
    INDEX idx_rule_timestamp (rule_id, test_timestamp),
    INDEX idx_user_timestamp (test_user_id, test_timestamp)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='规则测试历史表';

-- ============================================================
-- 初始化数据: 示例测试历史记录（可选）
-- ============================================================

-- 示例1: 迟到规则测试成功记录
INSERT INTO t_attendance_rule_test_history (
    test_id,
    rule_id,
    rule_name,
    rule_condition,
    rule_action,
    test_result,
    result_message,
    condition_matched,
    execution_time,
    executed_actions,
    execution_logs,
    test_input_data,
    test_output_data,
    test_scenario,
    test_user_id,
    test_user_name,
    test_timestamp
) VALUES (
    'test-20251226-001',
    1,
    '迟到扣款规则',
    '{"lateMinutes": 5}',
    '{"deductAmount": 50}',
    'MATCH',
    '规则匹配成功，迟到10分钟，扣款50元',
    1,
    150,
    '[{"actionName":"deductAmount","actionValue":50,"executionStatus":"SUCCESS","executionMessage":"扣款50元","executionTimestamp":"2025-12-26T08:35:10"}]',
    '[{"logLevel":"INFO","logMessage":"开始评估规则条件","logTimestamp":"2025-12-26T08:35:09"},{"logLevel":"INFO","logMessage":"条件匹配成功: lateMinutes=10","logTimestamp":"2025-12-26T08:35:09"},{"logLevel":"INFO","logMessage":"执行扣款动作","logTimestamp":"2025-12-26T08:35:10"}]',
    '{"userId":1001,"punchTime":"2025-12-26T08:40:00","shiftStartTime":"08:30:00"}',
    '{"matched":true,"lateMinutes":10,"deductAmount":50}',
    'LATE',
    1,
    'admin',
    '2025-12-26 08:35:10'
);

-- 示例2: 正常打卡测试不匹配记录
INSERT INTO t_attendance_rule_test_history (
    test_id,
    rule_id,
    rule_name,
    rule_condition,
    rule_action,
    test_result,
    result_message,
    condition_matched,
    execution_time,
    executed_actions,
    execution_logs,
    test_input_data,
    test_output_data,
    test_scenario,
    test_user_id,
    test_user_name,
    test_timestamp
) VALUES (
    'test-20251226-002',
    1,
    '迟到扣款规则',
    '{"lateMinutes": 5}',
    '{"deductAmount": 50}',
    'NOT_MATCH',
    '条件不匹配，正常打卡，未迟到',
    0,
    80,
    '[]',
    '[{"logLevel":"INFO","logMessage":"开始评估规则条件","logTimestamp":"2025-12-26T08:25:00"},{"logLevel":"INFO","logMessage":"条件不匹配: lateMinutes=0","logTimestamp":"2025-12-26T08:25:00"}]',
    '{"userId":1002,"punchTime":"2025-12-26T08:28:00","shiftStartTime":"08:30:00"}',
    '{"matched":false,"lateMinutes":0}',
    'NORMAL',
    1,
    'admin',
    '2025-12-26 08:25:00'
);

-- 示例3: 加班规则测试记录
INSERT INTO t_attendance_rule_test_history (
    test_id,
    rule_id,
    rule_name,
    rule_condition,
    rule_action,
    test_result,
    result_message,
    condition_matched,
    execution_time,
    executed_actions,
    execution_logs,
    test_input_data,
    test_output_data,
    test_scenario,
    test_user_id,
    test_user_name,
    test_timestamp
) VALUES (
    'test-20251226-003',
    2,
    '加班补贴规则',
    '{"overtimeHours": 1}',
    '{"allowanceAmount": 100}',
    'MATCH',
    '规则匹配成功，加班2小时，补贴200元',
    1,
    120,
    '[{"actionName":"allowanceAmount","actionValue":200,"executionStatus":"SUCCESS","executionMessage":"发放加班补贴200元","executionTimestamp":"2025-12-26T20:05:00"}]',
    '[{"logLevel":"INFO","logMessage":"开始评估规则条件","logTimestamp":"2025-12-26T20:04:58"},{"logLevel":"INFO","logMessage":"条件匹配成功: overtimeHours=2","logTimestamp":"2025-12-26T20:04:58"},{"logLevel":"INFO","logMessage":"执行补贴动作","logTimestamp":"2025-12-26T20:05:00"}]',
    '{"userId":1001,"punchOutTime":"2025-12-26T20:00:00","shiftEndTime":"18:00:00"}',
    '{"matched":true,"overtimeHours":2,"allowanceAmount":200}',
    'OVERTIME',
    1,
    'admin',
    '2025-12-26 20:05:00'
);

-- ============================================================
-- 性能优化说明
-- ============================================================
-- 1. 已为常用查询字段创建索引：rule_id, test_user_id, test_result, test_timestamp
-- 2. 已为组合查询创建复合索引：(rule_id, test_timestamp), (test_user_id, test_timestamp)
-- 3. test_id使用VARCHAR(100)支持自定义测试ID格式
-- 4. JSON字段使用TEXT类型存储，可扩展性强
-- 5. 执行时间使用BIGINT(毫秒)，提高精度
-- 6. 建议定期清理过期历史数据(如保留90天)
-- ============================================================
