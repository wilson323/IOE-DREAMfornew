-- =============================================
-- 用户会话表
-- 表名：t_user_session
-- 功能：记录用户登录会话信息，支持并发登录控制
-- =============================================

CREATE TABLE IF NOT EXISTS `t_user_session` (
    `session_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '会话ID（主键）',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `token` VARCHAR(500) NOT NULL COMMENT '访问令牌',
    `refresh_token` VARCHAR(500) NULL COMMENT '刷新令牌',
    `device_info` VARCHAR(200) NULL COMMENT '设备信息',
    `login_ip` VARCHAR(50) NULL COMMENT '登录IP',
    `login_time` DATETIME NOT NULL COMMENT '登录时间',
    `last_access_time` DATETIME NOT NULL COMMENT '最后访问时间',
    `expiry_time` DATETIME NOT NULL COMMENT '过期时间',
    `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态（1-活跃，0-已登出，2-已过期）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user` BIGINT(20) NULL COMMENT '创建人',
    `update_user` BIGINT(20) NULL COMMENT '更新人',
    `deleted_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记（0-未删除，1-已删除）',
    PRIMARY KEY (`session_id`),
    UNIQUE KEY `uk_token` (`token`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status_expiry` (`status`, `expiry_time`),
    KEY `idx_login_time` (`login_time`),
    KEY `idx_deleted_flag` (`deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户会话表';

-- 索引说明：
-- 1. uk_token: 令牌唯一索引，用于快速查询会话
-- 2. idx_user_id: 用户ID索引，用于查询用户所有会话
-- 3. idx_status_expiry: 复合索引，用于查询活跃会话和清理过期会话
-- 4. idx_login_time: 登录时间索引，用于按时间排序查询
-- 5. idx_deleted_flag: 删除标记索引，用于逻辑删除查询

-- 性能优化：
-- - 使用InnoDB引擎支持事务
-- - 使用utf8mb4字符集支持emoji等特殊字符
-- - 合理的索引设计提升查询性能
-- - 逻辑删除保留历史数据

