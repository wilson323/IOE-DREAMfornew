-- ============================================
-- 系统变更日志表 - t_change_log
-- 版本: 1.0.0
-- 日期: 2025-12-08
-- 说明: 存储系统版本更新和变更记录
-- ============================================

CREATE TABLE IF NOT EXISTS `t_change_log` (
    `log_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `version` VARCHAR(50) NOT NULL COMMENT '版本号',
    `publish_date` DATETIME NOT NULL COMMENT '发布日期',
    `change_type` VARCHAR(50) NOT NULL COMMENT '变更类型：NEW_FEATURE-新功能，IMPROVEMENT-改进，BUG_FIX-修复',
    `title` VARCHAR(255) NOT NULL COMMENT '变更标题',
    `description` TEXT NOT NULL COMMENT '变更描述',
    `author` VARCHAR(100) COMMENT '作者',
    `is_important` TINYINT NOT NULL DEFAULT 0 COMMENT '是否重要：1-是 0-否',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序号',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    PRIMARY KEY (`log_id`),
    KEY `idx_change_log_version` (`version`),
    KEY `idx_change_log_type` (`change_type`),
    KEY `idx_change_log_date` (`publish_date`),
    KEY `idx_change_log_deleted` (`deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统变更日志表';

-- 插入初始变更日志
INSERT INTO `t_change_log` (`version`, `publish_date`, `change_type`, `title`, `description`, `author`, `is_important`, `sort`, `create_user_id`) VALUES
('1.0.0', '2025-12-08 00:00:00', 'NEW_FEATURE', '🎉 IOE-DREAM智慧园区平台正式发布', '# 核心功能\n- ✅ 多模态生物识别（人脸、指纹、虹膜）\n- ✅ 智能门禁管理\n- ✅ 自动考勤系统\n- ✅ 无感消费结算\n- ✅ 智能访客管理\n- ✅ 视频监控联动', 'IOE-DREAM Team', 1, 1, 1),
('1.0.0', '2025-12-08 00:00:00', 'NEW_FEATURE', '🔐 企业级安全认证体系', '# 安全特性\n- ✅ Sa-Token权限认证\n- ✅ 接口加解密\n- ✅ 数据脱敏\n- ✅ 操作审计\n- ✅ 三级等保合规', 'IOE-DREAM Team', 1, 2, 1),
('1.0.0', '2025-12-08 00:00:00', 'NEW_FEATURE', '🏗️ 微服务架构设计', '# 架构优势\n- ✅ Spring Boot 3.5.8\n- ✅ Java 17 LTS\n- ✅ Vue3 + Vite5\n- ✅ 多级缓存（L1+L2+L3）\n- ✅ SAGA分布式事务\n- ✅ Druid连接池', 'IOE-DREAM Team', 0, 3, 1)
ON DUPLICATE KEY UPDATE `title`=VALUES(`title`);
