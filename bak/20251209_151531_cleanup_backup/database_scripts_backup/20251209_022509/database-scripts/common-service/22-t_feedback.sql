-- ============================================
-- 用户反馈表 - t_feedback
-- 版本: 1.0.0
-- 日期: 2025-12-08
-- 说明: 存储用户反馈和建议
-- ============================================

CREATE TABLE IF NOT EXISTS `t_feedback` (
    `feedback_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '反馈ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `username` VARCHAR(100) COMMENT '用户名',
    `feedback_type` VARCHAR(50) NOT NULL COMMENT '反馈类型：BUG-问题反馈，FEATURE-功能建议，IMPROVEMENT-改进建议，OTHER-其他',
    `title` VARCHAR(255) NOT NULL COMMENT '反馈标题',
    `content` TEXT NOT NULL COMMENT '反馈内容',
    `contact` VARCHAR(100) COMMENT '联系方式（邮箱/电话）',
    `priority` TINYINT NOT NULL DEFAULT 2 COMMENT '优先级：1-高 2-中 3-低',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '处理状态：0-待处理 1-处理中 2-已完成 3-已关闭',
    `reply_content` TEXT COMMENT '回复内容',
    `reply_time` DATETIME COMMENT '回复时间',
    `reply_user_id` BIGINT COMMENT '回复人ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    PRIMARY KEY (`feedback_id`),
    KEY `idx_feedback_user` (`user_id`),
    KEY `idx_feedback_type` (`feedback_type`),
    KEY `idx_feedback_status` (`status`),
    KEY `idx_feedback_priority` (`priority`),
    KEY `idx_feedback_time` (`create_time`),
    KEY `idx_feedback_deleted` (`deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户反馈表';

-- 插入示例反馈数据（可选）
INSERT INTO `t_feedback` (`user_id`, `username`, `feedback_type`, `title`, `content`, `contact`, `priority`, `status`, `create_user_id`) VALUES
(1, 'admin', 'FEATURE', '希望增加移动端支持', '建议开发移动端APP，方便员工随时随地使用系统功能，特别是考勤打卡和消费查询。', 'admin@example.com', 1, 0, 1),
(1, 'admin', 'IMPROVEMENT', '优化登录验证码', '当前验证码有时不太清晰，建议优化验证码生成算法，提升用户体验。', 'admin@example.com', 2, 1, 1)
ON DUPLICATE KEY UPDATE `title`=VALUES(`title`);
