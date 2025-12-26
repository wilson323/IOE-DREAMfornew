-- ================================================================
-- 人员限制功能表
-- 功能: 特定人员权限限制（黑名单/白名单/时段限制）
-- 作者: IOE-DREAM Team
-- 日期: 2025-12-25
-- ================================================================

CREATE TABLE t_access_person_restriction (
    restriction_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '限制ID',
    restriction_name VARCHAR(100) NOT NULL COMMENT '限制名称',
    restriction_type VARCHAR(30) NOT NULL COMMENT '限制类型(BLACKLIST-黑名单, WHITELIST-白名单, TIME_LIMIT-时段限制)',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    user_name VARCHAR(100) COMMENT '用户姓名',
    area_ids TEXT COMMENT '适用区域ID列表(JSON数组)',
    time_ranges TEXT COMMENT '时段限制(JSON数组)',
    reason VARCHAR(500) COMMENT '限制原因',
    effective_time DATETIME COMMENT '生效时间',
    expiry_time DATETIME COMMENT '失效时间',
    enabled TINYINT DEFAULT 1 COMMENT '启用状态(0-禁用 1-启用)',
    description VARCHAR(500) COMMENT '限制描述',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_user_id(user_id),
    INDEX idx_enabled(enabled),
    INDEX idx_restriction_type(restriction_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='人员限制规则表';

INSERT INTO t_access_person_restriction (
    restriction_name, restriction_type, user_id, user_name, area_ids, reason, enabled, description
) VALUES
(
    '临时员工黑名单', 'BLACKLIST', 10001, '张三', '[1001, 1002]', '临时员工权限受限', 1, '临时员工禁止进入实验室区域'
),
(
    'VIP白名单', 'WHITELIST', 10002, '李四', '[]', 'VIP客户', 1, 'VIP客户无区域限制'
);
