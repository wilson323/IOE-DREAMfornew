-- ============================================================================
-- IOE-DREAM 智慧园区 - 门禁服务
-- 反潜回功能数据库表创建脚本
-- 版本: V20251226
-- 作者: IOE-DREAM Team
-- 说明: 支持全局反潜回、区域反潜回、软反潜回、硬反潜回4种模式
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. 反潜回配置表 (t_anti_passback_config)
-- 说明: 存储反潜回规则配置，支持4种反潜回模式
-- ----------------------------------------------------------------------------
CREATE TABLE t_anti_passback_config (
    -- 主键
    config_id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '配置ID',

    -- 基础信息
    config_name            VARCHAR(100)    NOT NULL COMMENT '配置名称',
    config_code            VARCHAR(50)     NOT NULL COMMENT '配置编码（唯一标识）',

    -- 反潜回模式
    anti_passback_mode     TINYINT         NOT NULL DEFAULT 1 COMMENT '反潜回模式：1-全局反潜回 2-区域反潜回 3-软反潜回 4-硬反潜回',

    -- 区域和设备关联
    area_id                BIGINT                     DEFAULT NULL COMMENT '区域ID（区域反潜回时使用）',
    device_id              VARCHAR(50)                DEFAULT NULL COMMENT '设备ID（指定设备时使用）',
    device_ids             TEXT                       DEFAULT NULL COMMENT '设备列表（JSON数组，多设备反潜回）',

    -- 时间窗口配置（软反潜回使用）
    time_window_seconds    INT                        DEFAULT 60 COMMENT '时间窗口（秒），软反潜回允许时间内第二次通行',
    allow_first_exit       TINYINT         NOT NULL DEFAULT 1 COMMENT '是否允许先进后出：0-否 1-是',

    -- 硬反潜回配置
    block_duration_seconds INT                        DEFAULT 300 COMMENT '阻止时长（秒），硬反潜回触发后的阻止时长',
    alarm_on_trigger       TINYINT         NOT NULL DEFAULT 1 COMMENT '触发时是否告警：0-否 1-是',

    -- 用户范围限制
    user_scope             TINYINT         NOT NULL DEFAULT 1 COMMENT '用户范围：1-全部用户 2-指定用户组 3-指定用户',
    user_scope_value       TEXT                       DEFAULT NULL COMMENT '用户范围值（JSON：用户组ID列表或用户ID列表）',

    -- 时间限制
    effective_time         DATETIME        NOT NULL COMMENT '生效时间',
    expire_time            DATETIME                    DEFAULT NULL COMMENT '失效时间（NULL表示永久有效）',

    -- 优先级和状态
    priority               INT             NOT NULL DEFAULT 50 COMMENT '优先级（1-100，数值越大优先级越高）',
    status                 TINYINT         NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',

    -- 描述和备注
    description            VARCHAR(500)               DEFAULT NULL COMMENT '描述',
    remark                 VARCHAR(500)               DEFAULT NULL COMMENT '备注',

    -- 审计字段
    create_time            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id         BIGINT                     DEFAULT NULL COMMENT '创建人ID',
    update_user_id         BIGINT                     DEFAULT NULL COMMENT '更新人ID',
    deleted_flag           TINYINT         NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    version                INT             NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',

    -- 主键约束
    PRIMARY KEY (config_id),

    -- 唯一索引
    UNIQUE INDEX uk_anti_passback_config_code (config_code, deleted_flag),

    -- 普通索引
    INDEX idx_anti_passback_area (area_id),
    INDEX idx_anti_passback_device (device_id),
    INDEX idx_anti_passback_mode (anti_passback_mode),
    INDEX idx_anti_passback_status (status),
    INDEX idx_anti_passback_priority (priority),
    INDEX idx_anti_passback_effective_time (effective_time, expire_time),

    -- 联合索引（覆盖常用查询）
    INDEX idx_anti_passback_query (status, deleted_flag, anti_passback_mode),
    INDEX idx_anti_passback_area_device (area_id, device_id, status, deleted_flag)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='反潜回配置表';


-- ----------------------------------------------------------------------------
-- 2. 反潜回检测记录表 (t_anti_passback_record)
-- 说明: 记录反潜回检测日志，用于分析和监控
-- ----------------------------------------------------------------------------
CREATE TABLE t_anti_passback_record (
    -- 主键
    record_id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '记录ID',

    -- 关联信息
    config_id              BIGINT          NOT NULL COMMENT '配置ID',
    config_code            VARCHAR(50)     NOT NULL COMMENT '配置编码',

    -- 用户信息
    user_id                BIGINT          NOT NULL COMMENT '用户ID',
    username               VARCHAR(50)               DEFAULT NULL COMMENT '用户名',
    user_type              TINYINT                    DEFAULT NULL COMMENT '用户类型：1-员工 2-访客 3-黑名单',

    -- 设备和区域信息
    device_id              VARCHAR(50)     NOT NULL COMMENT '设备ID',
    device_name            VARCHAR(100)              DEFAULT NULL COMMENT '设备名称',
    area_id                BIGINT                     DEFAULT NULL COMMENT '区域ID',
    area_name              VARCHAR(100)              DEFAULT NULL COMMENT '区域名称',

    -- 通行信息
    access_time            DATETIME        NOT NULL COMMENT '通行时间',
    access_direction       TINYINT         NOT NULL DEFAULT 1 COMMENT '通行方向：1-进 2-出',
    access_result          TINYINT         NOT NULL DEFAULT 1 COMMENT '通行结果：0-失败（被阻止） 1-成功',
    access_card_no         VARCHAR(50)               DEFAULT NULL COMMENT '卡号',
    biometric_type         TINYINT                    DEFAULT NULL COMMENT '生物识别类型：1-人脸 2-指纹 3-掌纹',

    -- 反潜回检测信息
    anti_passback_mode     TINYINT         NOT NULL COMMENT '反潜回模式：1-全局 2-区域 3-软 4-硬',
    is_violation           TINYINT         NOT NULL DEFAULT 0 COMMENT '是否违规：0-否 1-是',
    violation_type         VARCHAR(50)                DEFAULT NULL COMMENT '违规类型：PASSBACK_ALREADY_IN-已在内 PASSBACK_TOO_SOON-过快 PASSBACK_BLOCKED-被阻止',
    violation_reason       TEXT                       DEFAULT NULL COMMENT '违规原因说明',

    -- 上一次通行信息（用于分析）
    last_access_time       DATETIME                    DEFAULT NULL COMMENT '上一次通行时间',
    last_access_device     VARCHAR(50)                DEFAULT NULL COMMENT '上一次通行设备ID',
    last_access_direction  TINYINT                    DEFAULT NULL COMMENT '上一次通行方向',
    time_diff_seconds      INT                        DEFAULT NULL COMMENT '与上次通行时间差（秒）',

    -- 告警信息
    is_alarm               TINYINT         NOT NULL DEFAULT 0 COMMENT '是否产生告警：0-否 1-是',
    alarm_level            TINYINT                    DEFAULT NULL COMMENT '告警级别：1-提示 2-警告 3-严重',
    alarm_message          VARCHAR(500)               DEFAULT NULL COMMENT '告警消息',

    -- 处理信息
    is_handled             TINYINT         NOT NULL DEFAULT 0 COMMENT '是否已处理：0-否 1-是',
    handle_user_id         BIGINT                     DEFAULT NULL COMMENT '处理人ID',
    handle_time            DATETIME                   DEFAULT NULL COMMENT '处理时间',
    handle_remark          VARCHAR(500)               DEFAULT NULL COMMENT '处理备注',

    -- 扩展信息
    extended_data          TEXT                       DEFAULT NULL COMMENT '扩展数据（JSON格式）',

    -- 审计字段
    create_time            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag           TINYINT         NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',

    -- 主键约束
    PRIMARY KEY (record_id),

    -- 普通索引
    INDEX idx_anti_passback_record_config (config_id),
    INDEX idx_anti_passback_record_user (user_id),
    INDEX idx_anti_passback_record_device (device_id),
    INDEX idx_anti_passback_record_area (area_id),
    INDEX idx_anti_passback_record_time (access_time),
    INDEX idx_anti_passback_record_violation (is_violation),
    INDEX idx_anti_passback_record_alarm (is_alarm),
    INDEX idx_anti_passback_record_handled (is_handled),

    -- 联合索引（覆盖常用查询）
    INDEX idx_anti_passback_record_user_time (user_id, access_time),
    INDEX idx_anti_passback_record_device_time (device_id, access_time),
    INDEX idx_anti_passback_record_violation_time (is_violation, access_time),
    INDEX idx_anti_passback_record_unhandled (is_handled, is_alarm, deleted_flag)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='反潜回检测记录表';


-- ----------------------------------------------------------------------------
-- 3. 初始化默认反潜回配置
-- ----------------------------------------------------------------------------
INSERT INTO t_anti_passback_config (
    config_name,
    config_code,
    anti_passback_mode,
    area_id,
    time_window_seconds,
    allow_first_exit,
    block_duration_seconds,
    alarm_on_trigger,
    user_scope,
    effective_time,
    priority,
    status,
    description,
    remark
) VALUES
(
    '全局软反潜回配置',
    'GLOBAL_SOFT_PASSBACK',
    3,  -- 软反潜回
    NULL,  -- 全局
    60,  -- 60秒时间窗口
    1,  -- 允许先进后出
    300,  -- 阻止5分钟
    1,  -- 触发时告警
    1,  -- 全部用户
    NOW(),
    100,  -- 最高优先级
    1,  -- 启用
    '全局软反潜回，60秒内同一用户不能重复通行',
    '默认启用，适用于所有门禁设备'
),
(
    '全局硬反潜回配置',
    'GLOBAL_HARD_PASSBACK',
    4,  -- 硬反潜回
    NULL,  -- 全局
    0,  -- 无时间窗口
    0,  -- 不允许先进后出
    600,  -- 阻止10分钟
    1,  -- 触发时告警
    1,  -- 全部用户
    NOW(),
    90,  -- 高优先级
    0,  -- 默认禁用（可根据需要启用）
    '全局硬反潜回，严格防止重复通行',
    '严格模式，适用于高安全区域'
);

-- ----------------------------------------------------------------------------
-- 4. 创建视图：反潜回违规统计视图
-- ----------------------------------------------------------------------------
CREATE OR REPLACE VIEW v_anti_passback_violation_stats AS
SELECT
    DATE(access_time) AS violation_date,
    config_code,
    anti_passback_mode,
    COUNT(*) AS violation_count,
    COUNT(DISTINCT user_id) AS unique_users,
    SUM(CASE WHEN is_alarm = 1 THEN 1 ELSE 0 END) AS alarm_count,
    SUM(CASE WHEN is_handled = 0 THEN 1 ELSE 0 END) AS unhandled_count
FROM t_anti_passback_record
WHERE deleted_flag = 0
  AND is_violation = 1
GROUP BY DATE(access_time), config_code, anti_passback_mode
ORDER BY violation_date DESC, violation_count DESC;

-- ============================================================================
-- 执行完成
-- ============================================================================
