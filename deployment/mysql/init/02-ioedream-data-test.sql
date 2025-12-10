-- =====================================================
-- IOE-DREAM 数据库初始数据脚本 - 测试环境
-- 版本: V1.1.0-TEST
-- 描述: 测试环境初始数据，包含脱敏测试数据
-- 执行条件: ENVIRONMENT=test
-- 数据库名: ioedream
-- 幂等性: 使用INSERT IGNORE确保可重复执行
-- =====================================================

-- 设置执行环境
SET FOREIGN_KEY_CHECKS = 0;
SET SQL_MODE = 'STRICT_TRANS_TABLES,NO_ZERO_DATE,NO_ZERO_IN_DATE,ERROR_FOR_DIVISION_BY_ZERO';

USE ioedream;

-- =====================================================
-- 测试环境：包含脱敏测试数据
-- =====================================================

-- 注意：测试环境数据脚本继承自 02-ioedream-data.sql
-- 这里只添加测试环境特有的脱敏数据

-- 创建测试用户（测试环境，数据已脱敏）
INSERT IGNORE INTO t_common_user (username, password, real_name, nickname, gender, phone, email, status, department_id, position, employee_no) VALUES
('qa_user1', '$2a$10$7JB720yubVSd.TbpngVKpONx1YH8hEIXy2B/S8RDnm6FSL.NGxqa', '测试用户A', 'QATest1', 1, '138****8001', 'qa1@test.com', 1, 2, 'QA工程师', 'QA001'),
('qa_user2', '$2a$10$7JB720yubVSd.TbpngVKpONx1YH8hEIXy2B/S8RDnm6FSL.NGxqa', '测试用户B', 'QATest2', 2, '138****8002', 'qa2@test.com', 1, 2, 'QA工程师', 'QA002');

-- 创建测试消费账户
INSERT IGNORE INTO t_consume_account (user_id, account_no, account_name, balance, status) VALUES
(2, 'ACC000002', '测试账户A', 500.00, 1),
(3, 'ACC000003', '测试账户B', 300.00, 1);

-- 记录迁移历史
INSERT INTO t_migration_history (
    version,
    description,
    script_name,
    status,
    start_time,
    end_time,
    create_time
) VALUES (
    'V1.1.0-TEST',
    '测试环境初始数据 - 包含脱敏测试数据',
    '02-ioedream-data-test.sql',
    'SUCCESS',
    NOW(),
    NOW(),
    NOW()
)
ON DUPLICATE KEY UPDATE
    description = VALUES(description),
    script_name = VALUES(script_name),
    status = 'SUCCESS',
    end_time = NOW();

SET FOREIGN_KEY_CHECKS = 1;

SELECT 'V1.1.0-TEST 测试环境数据脚本执行完成！' AS migration_status,
       '添加测试用户 2个（数据已脱敏），测试账户 2个' AS migration_summary,
       NOW() AS completed_time;

