-- =====================================================
-- IOE-DREAM 数据库初始数据脚本 - 开发环境
-- 版本: V1.1.0-DEV
-- 描述: 开发环境初始数据，包含测试数据
-- 执行条件: ENVIRONMENT=dev 或未设置环境变量
-- 数据库名: ioedream
-- 幂等性: 使用INSERT IGNORE确保可重复执行
-- =====================================================

-- 设置执行环境
SET FOREIGN_KEY_CHECKS = 0;
SET SQL_MODE = 'STRICT_TRANS_TABLES,NO_ZERO_DATE,NO_ZERO_IN_DATE,ERROR_FOR_DIVISION_BY_ZERO';

USE ioedream;

-- =====================================================
-- 开发环境：包含测试数据
-- =====================================================

-- 注意：开发环境数据脚本继承自 02-ioedream-data.sql
-- 这里只添加开发环境特有的测试数据

-- 创建测试用户（开发环境专用）
INSERT IGNORE INTO t_common_user (username, password, real_name, nickname, gender, phone, email, status, department_id, position, employee_no) VALUES
('test_user1', '$2a$10$7JB720yubVSd.TbpngVKpONx1YH8hEIXy2B/S8RDnm6FSL.NGxqa', '测试用户1', 'Test1', 1, '13800138001', 'test1@ioe-dream.com', 1, 2, '测试工程师', 'TEST001'),
('test_user2', '$2a$10$7JB720yubVSd.TbpngVKpONx1YH8hEIXy2B/S8RDnm6FSL.NGxqa', '测试用户2', 'Test2', 2, '13800138002', 'test2@ioe-dream.com', 1, 2, '测试工程师', 'TEST002'),
('dev_user', '$2a$10$7JB720yubVSd.TbpngVKpONx1YH8hEIXy2B/S8RDnm6FSL.NGxqa', '开发用户', 'Dev', 1, '13800138003', 'dev@ioe-dream.com', 1, 2, '开发工程师', 'DEV001');

-- 创建测试消费账户
INSERT IGNORE INTO t_consume_account (user_id, account_no, account_name, balance, status) VALUES
(2, 'ACC000002', '测试用户1账户', 500.00, 1),
(3, 'ACC000003', '测试用户2账户', 300.00, 1),
(4, 'ACC000004', '开发用户账户', 1000.00, 1);

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
    'V1.1.0-DEV',
    '开发环境初始数据 - 包含测试用户和测试数据',
    '02-ioedream-data-dev.sql',
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

SELECT 'V1.1.0-DEV 开发环境数据脚本执行完成！' AS migration_status,
       '添加测试用户 3个，测试账户 3个' AS migration_summary,
       NOW() AS completed_time;

