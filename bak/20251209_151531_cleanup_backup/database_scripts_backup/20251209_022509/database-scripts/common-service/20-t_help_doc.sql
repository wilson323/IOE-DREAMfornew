-- ============================================
-- 帮助文档表 - t_help_doc
-- 版本: 1.0.0
-- 日期: 2025-12-08
-- 说明: 存储系统帮助文档
-- ============================================

CREATE TABLE IF NOT EXISTS `t_help_doc` (
    `doc_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '文档ID',
    `relation_id` BIGINT NOT NULL DEFAULT 0 COMMENT '关联ID（页面ID、功能ID等）',
    `relation_type` VARCHAR(50) NOT NULL DEFAULT 'GLOBAL' COMMENT '关联类型：GLOBAL-全局，PAGE-页面，FEATURE-功能',
    `title` VARCHAR(255) NOT NULL COMMENT '文档标题',
    `content` TEXT NOT NULL COMMENT '文档内容（Markdown格式）',
    `author` VARCHAR(100) COMMENT '作者',
    `version` VARCHAR(20) NOT NULL DEFAULT '1.0.0' COMMENT '文档版本',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序号',
    `view_count` INT NOT NULL DEFAULT 0 COMMENT '查看次数',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-发布 0-草稿',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    PRIMARY KEY (`doc_id`),
    KEY `idx_help_doc_relation` (`relation_id`, `relation_type`),
    KEY `idx_help_doc_status` (`status`),
    KEY `idx_help_doc_deleted` (`deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帮助文档表';

-- 插入初始帮助文档
INSERT INTO `t_help_doc` (`relation_id`, `relation_type`, `title`, `content`, `author`, `version`, `sort`, `status`, `create_user_id`) VALUES
(0, 'GLOBAL', '系统快速开始指南', '# 欢迎使用IOE-DREAM智慧园区管理平台\n\n## 系统简介\nIOE-DREAM是企业级智慧园区一卡通管理平台，提供门禁、考勤、消费、访客等完整解决方案。\n\n## 快速开始\n1. 使用管理员账号登录系统\n2. 完成基础信息配置\n3. 添加员工和设备\n4. 开始使用各功能模块', 'IOE-DREAM Team', '1.0.0', 1, 1, 1),
(0, 'GLOBAL', '常见问题解答（FAQ）', '# 常见问题解答\n\n## 登录问题\n**Q: 忘记密码怎么办？**\nA: 请联系系统管理员重置密码。\n\n## 功能使用\n**Q: 如何添加新员工？**\nA: 进入"员工管理"页面，点击"添加员工"按钮，填写必要信息后提交。', 'IOE-DREAM Team', '1.0.0', 2, 1, 1),
(0, 'GLOBAL', '联系技术支持', '# 技术支持\n\n## 联系方式\n- 技术支持邮箱：support@ioedream.com\n- 客服热线：400-xxx-xxxx\n- 工作时间：周一至周五 9:00-18:00', 'IOE-DREAM Team', '1.0.0', 3, 1, 1)
ON DUPLICATE KEY UPDATE `title`=VALUES(`title`);
