-- ============================================================================
-- IOE-DREAM 智慧园区 - 门禁服务
-- 实时监控告警功能数据库表创建脚本
-- 版本: V20251226
-- 作者: IOE-DREAM Team
-- 说明: 支持设备告警、告警规则、告警通知的完整告警体系
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. 设备告警表 (t_device_alert)
-- 说明: 记录设备产生的所有告警信息
-- ----------------------------------------------------------------------------
CREATE TABLE t_device_alert (
    -- 主键
    alert_id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '告警ID',

    -- 关联信息
    device_id             VARCHAR(50)     NOT NULL COMMENT '设备ID',
    device_name           VARCHAR(100)              DEFAULT NULL COMMENT '设备名称',
    device_type           TINYINT                     DEFAULT NULL COMMENT '设备类型：1-门禁 2-考勤 3-消费 4-视频',
    area_id               BIGINT                      DEFAULT NULL COMMENT '区域ID',
    area_name             VARCHAR(100)                DEFAULT NULL COMMENT '区域名称',

    -- 告警信息
    alert_type            VARCHAR(50)     NOT NULL COMMENT '告警类型：OFFLINE-离线 ONLINE-上线 FAULT-故障 TAMPER-防破坏 LOW_BATTERY-低电量 BLOCKED-被阻止',
    alert_level           TINYINT         NOT NULL DEFAULT 2 COMMENT '告警级别：1-提示 2-警告 3-严重 4-紧急',
    alert_title           VARCHAR(200)    NOT NULL COMMENT '告警标题',
    alert_message         TEXT                       DEFAULT NULL COMMENT '告警详细信息',

    -- 告警状态
    alert_status          TINYINT         NOT NULL DEFAULT 1 COMMENT '告警状态：1-未处理 2-处理中 3-已处理 4-已忽略',
    handled_by            BIGINT                      DEFAULT NULL COMMENT '处理人ID',
    handled_time          DATETIME                   DEFAULT NULL COMMENT '处理时间',
    handle_remark         VARCHAR(500)               DEFAULT NULL COMMENT '处理备注',

    -- 触发信息
    rule_id               BIGINT                      DEFAULT NULL COMMENT '触发规则ID',
    rule_name             VARCHAR(100)               DEFAULT NULL COMMENT '触发规则名称',
    trigger_time          DATETIME        NOT NULL COMMENT '告警触发时间',
    trigger_value         VARCHAR(100)               DEFAULT NULL COMMENT '触发值',

    -- 恢复信息
    recovered_time         DATETIME                   DEFAULT NULL COMMENT '恢复时间',
    recovered_by          BIGINT                      DEFAULT NULL COMMENT '恢复操作人ID',
    is_recovered           TINYINT         NOT NULL DEFAULT 0 COMMENT '是否已恢复：0-未恢复 1-已恢复',

    -- 通知状态
    notification_sent     TINYINT         NOT NULL DEFAULT 0 COMMENT '是否已发送通知：0-否 1-是',
    notification_count    INT             NOT NULL DEFAULT 0 COMMENT '通知发送次数',
    last_notification_time DATETIME                  DEFAULT NULL COMMENT '最后一次通知时间',

    -- 扩展信息
    extended_data         TEXT                       DEFAULT NULL COMMENT '扩展数据（JSON格式）',

    -- 审计字段
    create_time           DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time           DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'UPDATE时间',
    deleted_flag          TINYINT         NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',

    -- 主键约束
    PRIMARY KEY (alert_id),

    -- 普通索引
    INDEX idx_device_alert_device (device_id),
    INDEX idx_device_alert_type (alert_type),
    INDEX idx_device_alert_level (alert_level),
    INDEX idx_device_alert_status (alert_status),
    INDEX idx_device_alert_trigger_time (trigger_time),
    INDEX idx_device_alert_recovered (is_recovered),

    -- 联合索引（覆盖常用查询）
    INDEX idx_device_alert_query (alert_status, trigger_time, alert_level),
    INDEX idx_device_alert_device_time (device_id, trigger_time),
    INDEX idx_device_alert_unhandled (alert_status, is_recovered, deleted_flag)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备告警表';


-- ----------------------------------------------------------------------------
-- 2. 告警规则表 (t_alert_rule)
-- 说明: 定义告警触发规则、条件、级别等
-- ----------------------------------------------------------------------------
CREATE TABLE t_alert_rule (
    -- 主键
    rule_id               BIGINT          NOT NULL AUTO_INCREMENT COMMENT '规则ID',

    -- 基础信息
    rule_name             VARCHAR(100)    NOT NULL COMMENT '规则名称',
    rule_code             VARCHAR(50)     NOT NULL COMMENT '规则编码（唯一标识）',
    rule_category         VARCHAR(50)     NOT NULL COMMENT '规则分类：DEVICE-设备 OFFLINE-离线 FAULT-故障 SECURITY-安全',
    rule_type             VARCHAR(50)     NOT NULL COMMENT '规则类型：THRESHOLD-阈值 CONDITION-条件 COMPOSITE-组合',

    -- 触发条件
    trigger_condition     TEXT           NOT NULL COMMENT '触发条件（JSON格式）',
    -- 示例：{"type":"THRESHOLD","field":"battery_level","operator":"<=","value":20}

    -- 告警级别
    alert_level           TINYINT         NOT NULL DEFAULT 2 COMMENT '告警级别：1-提示 2-警告 3-严重 4-紧急',

    -- 升级规则
    escalation_enabled    TINYINT         NOT NULL DEFAULT 0 COMMENT '是否启用升级：0-否 1-是',
    escalation_rules      TEXT                       DEFAULT NULL COMMENT '升级规则（JSON数组）',
    -- 示例：[{"level":3,"duration":300,"unit":"SECONDS"},{"level":4,"duration":600,"unit":"SECONDS"}]

    -- 聚合规则（防刷屏）
    aggregation_enabled   TINYINT         NOT NULL DEFAULT 1 COMMENT '是否启用聚合：0-否 1-是',
    aggregation_window    INT                        DEFAULT 60 COMMENT '聚合时间窗口（秒）',
    aggregation_threshold INT                        DEFAULT 5 COMMENT '聚合阈值（窗口内相同告警超过此数量才发送）',

    -- 通知配置
    notification_enabled  TINYINT         NOT NULL DEFAULT 1 COMMENT '是否启用通知：0-否 1-是',
    notification_methods VARCHAR(200)               DEFAULT NULL COMMENT '通知方式（逗号分隔）：EMAIL,SMS,WEBSOCKET',
    notification_template TEXT                       DEFAULT NULL COMMENT '通知模板',

    -- 生效条件
    device_scope          TINYINT         NOT NULL DEFAULT 1 COMMENT '设备范围：1-全部设备 2-指定设备类型 3-指定设备',
    device_scope_value     TEXT                       DEFAULT NULL COMMENT '设备范围值（JSON）',
    time_effective         TINYINT         NOT NULL DEFAULT 1 COMMENT '时间生效：1-全天 2-工作日 3-自定义时间段',
    time_effective_value   TEXT                       DEFAULT NULL COMMENT '时间生效值（JSON）',

    -- 状态和优先级
    status                TINYINT         NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    priority              INT             NOT NULL DEFAULT 50 COMMENT '优先级（1-100，数值越大优先级越高）',

    -- 描述和备注
    description           VARCHAR(500)               DEFAULT NULL COMMENT '规则描述',
    remark                VARCHAR(500)               DEFAULT NULL COMMENT '备注',

    -- 审计字段
    create_time           DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time           DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id        BIGINT                      DEFAULT NULL COMMENT '创建人ID',
    update_user_id        BIGINT                      DEFAULT NULL COMMENT '更新人ID',
    deleted_flag          TINYINT         NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    version               INT             NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',

    -- 主键约束
    PRIMARY KEY (rule_id),

    -- 唯一索引
    UNIQUE INDEX uk_alert_rule_code (rule_code, deleted_flag),

    -- 普通索引
    INDEX idx_alert_rule_category (rule_category),
    INDEX idx_alert_rule_type (rule_type),
    INDEX idx_alert_rule_level (alert_level),
    INDEX idx_alert_rule_status (status),
    INDEX idx_alert_rule_priority (priority),

    -- 联合索引
    INDEX idx_alert_rule_query (status, deleted_flag, rule_category)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='告警规则表';


-- ----------------------------------------------------------------------------
-- 3. 告警通知记录表 (t_alert_notification)
-- 说明: 记录告警通知的发送状态和结果
-- ----------------------------------------------------------------------------
CREATE TABLE t_alert_notification (
    -- 主键
    notification_id       BIGINT          NOT NULL AUTO_INCREMENT COMMENT '通知ID',

    -- 关联信息
    alert_id              BIGINT          NOT NULL COMMENT '告警ID',
    rule_id               BIGINT                      DEFAULT NULL COMMENT '规则ID',
    notification_batch_id VARCHAR(50)                DEFAULT NULL COMMENT '通知批次ID（批量通知时使用）',

    -- 通知信息
    notification_method   VARCHAR(20)     NOT NULL COMMENT '通知方式：EMAIL-邮件 SMS-短信 WEBSOCKET-WebSocket PUSH-推送',
    notification_target   VARCHAR(200)    NOT NULL COMMENT '通知目标（邮箱、手机号、用户ID等）',
    notification_content  TEXT           NOT NULL COMMENT '通知内容',

    -- 发送状态
    send_status           TINYINT         NOT NULL DEFAULT 0 COMMENT '发送状态：0-待发送 1-发送中 2-发送成功 3-发送失败',
    send_time             DATETIME                   DEFAULT NULL COMMENT '发送时间',
    send_result           TEXT                       DEFAULT NULL COMMENT '发送结果',
    error_message         VARCHAR(500)               DEFAULT NULL COMMENT '错误信息',

    -- 重试信息
    retry_count           INT             NOT NULL DEFAULT 0 COMMENT '重试次数',
    max_retry_count       INT             NOT NULL DEFAULT 3 COMMENT '最大重试次数',
    next_retry_time       DATETIME                   DEFAULT NULL COMMENT '下次重试时间',

    -- 通知确认
    is_read               TINYINT         NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读 1-已读',
    read_time             DATETIME                   DEFAULT NULL COMMENT '阅读时间',

    -- 审计字段
    create_time           DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time           DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag          TINYINT         NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',

    -- 主键约束
    PRIMARY KEY (notification_id),

    -- 普通索引
    INDEX idx_alert_notification_alert (alert_id),
    INDEX idx_alert_notification_batch (notification_batch_id),
    INDEX idx_alert_notification_method (notification_method),
    INDEX idx_alert_notification_status (send_status),
    INDEX idx_alert_notification_send_time (send_time),
    INDEX idx_alert_notification_retry (send_status, next_retry_time),

    -- 联合索引
    INDEX idx_alert_notification_query (alert_id, send_status),
    INDEX idx_alert_notification_unread (is_read, deleted_flag)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='告警通知记录表';


-- ----------------------------------------------------------------------------
-- 4. 初始化默认告警规则
-- ----------------------------------------------------------------------------
INSERT INTO t_alert_rule (
    rule_name,
    rule_code,
    rule_category,
    rule_type,
    trigger_condition,
    alert_level,
    aggregation_enabled,
    aggregation_window,
    aggregation_threshold,
    notification_enabled,
    notification_methods,
    status,
    priority,
    description
) VALUES
(
    '设备离线告警',
    'DEVICE_OFFLINE',
    'OFFLINE',
    'THRESHOLD',
    '{"type":"THRESHOLD","field":"last_heartbeat","operator":">","value":300}',
    3,  -- 严重
    1,  -- 启用聚合
    300,  -- 5分钟窗口
    1,  -- 超过1次就告警
    1,  -- 启用通知
    'WEBSOCKET,SMS',
    1,  -- 启用
    100,  -- 高优先级
    '设备超过5分钟无心跳信号时触发告警'
),
(
    '设备故障告警',
    'DEVICE_FAULT',
    'FAULT',
    'CONDITION',
    '{"type":"CONDITION","field":"fault_code","operator":"!=","value":null}',
    4,  -- 紧急
    1,  -- 启用聚合
    60,  -- 1分钟窗口
    1,
    1,  -- 启用通知
    'WEBSOCKET,SMS,EMAIL',
    1,  -- 启用
    100,  -- 高优先级
    '设备上报故障代码时立即触发告警'
),
(
    '设备防破坏告警',
    'DEVICE_TAMPER',
    'SECURITY',
    'CONDITION',
    '{"type":"CONDITION","field":"tamper_detected","operator":"==","value":true}',
    4,  -- 紧急
    0,  -- 不启用聚合（立即告警）
    0,
    0,
    1,  -- 启用通知
    'WEBSOCKET,SMS,EMAIL',
    1,  -- 启用
    100,  -- 最高优先级
    '设备检测到防破坏触发时立即告警'
),
(
    '设备低电量告警',
    'DEVICE_LOW_BATTERY',
    'DEVICE',
    'THRESHOLD',
    '{"type":"THRESHOLD","field":"battery_level","operator":"<=","value":20}',
    2,  -- 警告
    1,  -- 启用聚合
    3600,  -- 1小时窗口
    3,  -- 1小时内3次才告警
    1,  -- 启用通知
    'WEBSOCKET',
    1,  -- 启用
    80,  -- 中等优先级
    '设备电量低于20%时触发告警'
);

-- ============================================================================
-- 执行完成
-- ============================================================================
