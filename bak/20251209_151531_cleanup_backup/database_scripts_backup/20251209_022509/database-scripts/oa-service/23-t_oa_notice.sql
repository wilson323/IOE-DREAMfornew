-- =====================================================
-- IOE-DREAM OA通知公告表初始化脚本
-- 功能：创建通知公告相关表并初始化示例数据
-- 作者：IOE-DREAM Team
-- 日期：2025-12-08
-- 版本：1.0.0
-- =====================================================

-- 1. 通知公告类型表
CREATE TABLE IF NOT EXISTS `t_oa_notice_type` (
    `notice_type_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '通知类型ID（主键）',
    `type_name` VARCHAR(100) NOT NULL COMMENT '类型名称',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序号',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
    PRIMARY KEY (`notice_type_id`),
    UNIQUE KEY `uk_notice_type_name` (`type_name`, `deleted_flag`),
    KEY `idx_notice_type_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='OA通知公告类型表';

-- 2. 通知公告表
CREATE TABLE IF NOT EXISTS `t_oa_notice` (
    `notice_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '通知ID（主键）',
    `notice_type_id` BIGINT NOT NULL COMMENT '通知类型ID',
    `title` VARCHAR(255) NOT NULL COMMENT '通知标题',
    `content` TEXT NOT NULL COMMENT '通知内容（HTML格式）',
    `attachment_url` VARCHAR(500) COMMENT '附件URL',
    `publish_status` TINYINT NOT NULL DEFAULT 0 COMMENT '发布状态 0-草稿 1-已发布 2-已撤回',
    `publish_time` DATETIME COMMENT '发布时间',
    `author` VARCHAR(100) NOT NULL COMMENT '作者',
    `author_user_id` BIGINT NOT NULL COMMENT '作者用户ID',
    `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览次数',
    `is_top` TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶 1-是 0-否',
    `is_important` TINYINT NOT NULL DEFAULT 0 COMMENT '是否重要 1-是 0-否',
    `effective_start_time` DATETIME COMMENT '生效开始时间',
    `effective_end_time` DATETIME COMMENT '生效结束时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
    PRIMARY KEY (`notice_id`),
    KEY `idx_notice_type` (`notice_type_id`),
    KEY `idx_notice_status_time` (`publish_status`, `publish_time`),
    KEY `idx_notice_author` (`author_user_id`),
    KEY `idx_notice_top_important` (`is_top`, `is_important`),
    KEY `idx_notice_effective_time` (`effective_start_time`, `effective_end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='OA通知公告表';

-- 3. 通知公告阅读记录表
CREATE TABLE IF NOT EXISTS `t_oa_notice_view_record` (
    `record_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID（主键）',
    `notice_id` BIGINT NOT NULL COMMENT '通知ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `user_name` VARCHAR(100) NOT NULL COMMENT '用户名',
    `view_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '阅读时间',
    `view_duration` INT COMMENT '阅读时长（秒）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`record_id`),
    UNIQUE KEY `uk_notice_user` (`notice_id`, `user_id`),
    KEY `idx_notice_id` (`notice_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_view_time` (`view_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='OA通知公告阅读记录表';

-- =====================================================
-- 初始化通知类型数据
-- =====================================================

INSERT INTO `t_oa_notice_type` (`notice_type_id`, `type_name`, `sort`, `create_user_id`, `update_user_id`, `deleted_flag`) VALUES
(1, '系统公告', 1, 1, 1, 0),
(2, '企业动态', 2, 1, 1, 0),
(3, '制度规范', 3, 1, 1, 0),
(4, '活动通知', 4, 1, 1, 0),
(5, '重要通知', 5, 1, 1, 0),
(6, '紧急通知', 6, 1, 1, 0)
ON DUPLICATE KEY UPDATE 
    `type_name` = VALUES(`type_name`),
    `sort` = VALUES(`sort`),
    `update_time` = CURRENT_TIMESTAMP;

-- =====================================================
-- 初始化通知公告示例数据
-- =====================================================

INSERT INTO `t_oa_notice` (
    `notice_type_id`, `title`, `content`, `publish_status`, `publish_time`,
    `author`, `author_user_id`, `view_count`, `is_top`, `is_important`,
    `effective_start_time`, `effective_end_time`, `create_user_id`, `update_user_id`, `deleted_flag`
) VALUES
(
    1,
    '🎉 IOE-DREAM智慧园区一卡通管理平台正式上线',
    '<h3>系统上线公告</h3>
    <p>尊敬的各位用户：</p>
    <p>经过团队的不懈努力，<strong>IOE-DREAM智慧园区一卡通管理平台</strong>现已正式上线运营！</p>
    <h4>核心功能亮点：</h4>
    <ul>
        <li>✅ <strong>多模态生物识别</strong>：支持人脸、指纹、掌纹、虹膜等多种识别方式</li>
        <li>✅ <strong>智能门禁控制</strong>：7x24小时实时监控，秒级识别响应</li>
        <li>✅ <strong>无感消费结算</strong>：刷脸/刷卡即可完成支付，支持离线模式</li>
        <li>✅ <strong>智能考勤管理</strong>：自动统计出勤、迟到、早退、加班等</li>
        <li>✅ <strong>访客智能管理</strong>：在线预约、审批、登记、授权全流程自动化</li>
        <li>✅ <strong>视频监控联动</strong>：AI智能分析，异常行为实时告警</li>
    </ul>
    <p>感谢大家的支持！</p>
    <p><strong>IOE-DREAM Team</strong></p>
    <p>2025年12月8日</p>',
    1,
    NOW(),
    'IOE-DREAM管理员',
    1,
    156,
    1,
    1,
    NOW(),
    DATE_ADD(NOW(), INTERVAL 1 YEAR),
    1,
    1,
    0
),
(
    2,
    '🔐 系统安全升级完成通知',
    '<h3>安全升级公告</h3>
    <p>各位用户：</p>
    <p>系统已完成安全升级，主要更新内容如下：</p>
    <h4>安全增强：</h4>
    <ul>
        <li>🔒 <strong>数据加密</strong>：敏感数据全部采用AES-256加密存储</li>
        <li>🔒 <strong>接口安全</strong>：实现接口加解密、防刷、限流机制</li>
        <li>🔒 <strong>权限增强</strong>：基于RBAC的细粒度权限控制</li>
        <li>🔒 <strong>审计追踪</strong>：完整的操作审计日志，满足三级等保要求</li>
    </ul>
    <p>请各位用户及时更新密码，确保账户安全！</p>
    <p><strong>技术部</strong></p>',
    1,
    NOW(),
    '技术部',
    1,
    89,
    0,
    1,
    NOW(),
    DATE_ADD(NOW(), INTERVAL 6 MONTH),
    1,
    1,
    0
),
(
    3,
    '📋 员工考勤管理制度（试行版）',
    '<h3>考勤管理制度</h3>
    <h4>一、考勤方式</h4>
    <ol>
        <li>员工需使用生物识别方式（人脸识别或指纹识别）进行打卡</li>
        <li>支持移动端APP打卡，但需在园区范围内</li>
        <li>特殊情况需要补卡的，需提交申请并经部门负责人审批</li>
    </ol>
    <h4>二、工作时间</h4>
    <ol>
        <li>标准工作时间：周一至周五 09:00-18:00</li>
        <li>弹性工作时间：可选择08:00-10:00签到，17:00-19:00签退</li>
        <li>午休时间：12:00-13:00</li>
    </ol>
    <h4>三、考勤统计</h4>
    <ol>
        <li>迟到：超过规定上班时间10分钟以上</li>
        <li>早退：未到规定下班时间离开</li>
        <li>加班：需提前申请并经审批后方可计入</li>
    </ol>
    <p>本制度自2025年12月8日起试行，试行期3个月。</p>
    <p><strong>人力资源部</strong></p>',
    1,
    NOW(),
    '人力资源部',
    1,
    234,
    0,
    0,
    NOW(),
    NULL,
    1,
    1,
    0
),
(
    4,
    '🎊 2026年元旦放假安排',
    '<h3>元旦放假通知</h3>
    <p>各部门：</p>
    <p>根据国家法定节假日安排，现将2026年元旦放假事宜通知如下：</p>
    <h4>放假时间：</h4>
    <ul>
        <li>2026年1月1日至1月3日，共3天</li>
        <li>1月4日（周一）正常上班</li>
    </ul>
    <h4>注意事项：</h4>
    <ol>
        <li>请各部门做好值班安排，并将值班表于12月25日前提交至行政部</li>
        <li>假期期间，门禁系统正常运行，值班人员需刷卡签到</li>
        <li>紧急事项请联系值班人员</li>
    </ol>
    <p>祝大家节日愉快！</p>
    <p><strong>行政部</strong></p>
    <p>2025年12月8日</p>',
    1,
    NOW(),
    '行政部',
    1,
    412,
    1,
    0,
    NOW(),
    '2026-01-03 23:59:59',
    1,
    1,
    0
),
(
    5,
    '🚨 【紧急】系统维护通知',
    '<h3>紧急维护通知</h3>
    <p><strong style="color: red;">紧急通知：</strong></p>
    <p>为提升系统性能和稳定性，我们将于<strong>2025年12月15日（周日）凌晨00:00-06:00</strong>进行系统维护。</p>
    <h4>影响范围：</h4>
    <ul>
        <li>❌ 门禁系统（暂停服务）</li>
        <li>❌ 考勤系统（暂停服务）</li>
        <li>❌ 消费系统（暂停服务）</li>
        <li>✅ 视频监控（正常运行）</li>
    </ul>
    <h4>应急措施：</h4>
    <ol>
        <li>维护期间，门禁将切换至手动开启模式</li>
        <li>维护结束后，系统将自动恢复</li>
        <li>如有问题，请联系技术支持：400-1024-1024</li>
    </ol>
    <p>给您带来的不便，敬请谅解！</p>
    <p><strong>技术部</strong></p>',
    1,
    NOW(),
    '技术部',
    1,
    678,
    1,
    1,
    NOW(),
    '2025-12-15 06:00:00',
    1,
    1,
    0
)
ON DUPLICATE KEY UPDATE 
    `title` = VALUES(`title`),
    `content` = VALUES(`content`),
    `update_time` = CURRENT_TIMESTAMP;

-- =====================================================
-- 初始化阅读记录示例数据
-- =====================================================

-- 为第一条通知添加阅读记录（模拟156次浏览）
INSERT INTO `t_oa_notice_view_record` (`notice_id`, `user_id`, `user_name`, `view_time`, `view_duration`) VALUES
(1, 1, 'admin', NOW(), 120),
(1, 2, 'user001', DATE_SUB(NOW(), INTERVAL 1 HOUR), 90),
(1, 3, 'user002', DATE_SUB(NOW(), INTERVAL 2 HOUR), 150)
ON DUPLICATE KEY UPDATE 
    `view_time` = VALUES(`view_time`),
    `view_duration` = VALUES(`view_duration`);

-- =====================================================
-- 验证数据插入
-- =====================================================

SELECT '=== 通知类型统计 ===' AS info;
SELECT COUNT(*) AS notice_type_count FROM t_oa_notice_type WHERE deleted_flag = 0;

SELECT '=== 通知公告统计 ===' AS info;
SELECT COUNT(*) AS notice_count FROM t_oa_notice WHERE deleted_flag = 0;

SELECT '=== 阅读记录统计 ===' AS info;
SELECT COUNT(*) AS view_record_count FROM t_oa_notice_view_record;
