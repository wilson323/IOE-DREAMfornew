-- ============================================================
-- IOE-DREAM 视频监控微服务数据库迁移脚本
-- 版本: V20251216.002
-- 描述: 创建视频监控关联表和触发器
-- 服务: ioedream-video-service
-- 技术栈: Spring Boot 3.5.8 + Spring Cloud Alibaba 2025.0.0 + Flyway 9.x
-- 数据库: MySQL 8.0+
-- 创建时间: 2025-12-16
-- 作者: IOE-DREAM Team
-- ============================================================

-- 设置字符集和排序规则
SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ============================================================
-- 7. 区域设备关联表 (t_area_device_relation)
-- ============================================================
CREATE TABLE t_area_device_relation (
    -- 主键
    relation_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',

    -- 关联标识
    area_id BIGINT NOT NULL COMMENT '区域ID',
    device_id BIGINT NOT NULL COMMENT '设备ID',
    device_code VARCHAR(50) NOT NULL COMMENT '设备编码',
    device_name VARCHAR(100) NOT NULL COMMENT '设备名称',

    -- 设备分类
    device_type INT NOT NULL COMMENT '设备类型：1-门禁 2-考勤 3-消费 4-视频 5-访客',
    device_sub_type INT COMMENT '设备子类型',
    business_module VARCHAR(20) NOT NULL COMMENT '业务模块：access-attendance-consume-visitor-video',

    -- 业务属性
    business_attributes TEXT COMMENT '业务属性(JSON格式)',
    relation_status TINYINT DEFAULT 1 COMMENT '关联状态：1-正常 2-维护 3-故障 4-离线 5-停用',
    priority INT DEFAULT 2 COMMENT '优先级：1-主设备 2-辅助设备 3-备用设备',

    -- 权限配置
    access_permission TINYINT DEFAULT 1 COMMENT '访问权限：0-禁止 1-只读 2-读写 3-管理',
    view_permission TINYINT DEFAULT 1 COMMENT '查看权限：0-禁止 1-允许',
    control_permission TINYINT DEFAULT 0 COMMENT '控制权限：0-禁止 1-允许',
    config_permission TINYINT DEFAULT 0 COMMENT '配置权限：0-禁止 1-允许',

    -- 时间控制
    effective_time DATETIME COMMENT '生效时间',
    expire_time DATETIME COMMENT '失效时间',
    access_time_range TEXT COMMENT '访问时间范围(JSON格式)',
    access_days VARCHAR(20) COMMENT '允许访问日期：1-7表示周一到周日',

    -- 功能开关
    real_time_monitoring TINYINT DEFAULT 1 COMMENT '实时监控：0-禁用 1-启用',
    recording_enabled TINYINT DEFAULT 0 COMMENT '录制功能：0-禁用 1-启用',
    playback_enabled TINYINT DEFAULT 1 COMMENT '回放功能：0-禁用 1-启用',
    alarm_enabled TINYINT DEFAULT 1 COMMENT '报警功能：0-禁用 1-启用',
    ptz_control_enabled TINYINT DEFAULT 1 COMMENT 'PTZ控制：0-禁用 1-启用',

    -- 安全配置
    encryption_required TINYINT DEFAULT 0 COMMENT '是否需要加密：0-否 1-是',
    audit_log_enabled TINYINT DEFAULT 1 COMMENT '审计日志：0-禁用 1-启用',
    access_log_enabled TINYINT DEFAULT 1 COMMENT '访问日志：0-禁用 1-启用',

    -- 扩展配置
    extended_config TEXT COMMENT '扩展配置(JSON格式)',
    custom_settings TEXT COMMENT '自定义设置(JSON格式)',

    -- 审计字段
    create_user_id BIGINT COMMENT '创建人ID',
    create_user_name VARCHAR(50) COMMENT '创建人姓名',
    update_user_id BIGINT COMMENT '更新人ID',
    update_user_name VARCHAR(50) COMMENT '更新人姓名',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    version INT NOT NULL DEFAULT 1 COMMENT '版本号(乐观锁)',

    -- 索引
    INDEX idx_area_id (area_id),
    INDEX idx_device_id (device_id),
    INDEX idx_device_code (device_code),
    INDEX idx_business_module (business_module),
    INDEX idx_device_type (device_type),
    INDEX idx_relation_status (relation_status),
    INDEX idx_priority (priority),
    INDEX idx_access_permission (access_permission),
    INDEX idx_effective_time (effective_time),
    INDEX idx_expire_time (expire_time),
    INDEX idx_deleted_flag (deleted_flag),
    INDEX idx_create_time (create_time),

    -- 复合索引
    INDEX idx_area_device (area_id, device_id, deleted_flag),
    INDEX idx_area_type_status (area_id, device_type, relation_status, deleted_flag),
    INDEX idx_module_type (business_module, device_type, deleted_flag),
    INDEX idx_permission_status (access_permission, relation_status, deleted_flag),
    INDEX idx_time_status (effective_time, expire_time, relation_status, deleted_flag),

    -- 唯一索引
    UNIQUE INDEX uk_area_device (area_id, device_id, deleted_flag),

    -- 外键约束
    FOREIGN KEY (area_id) REFERENCES t_common_area(area_id) ON DELETE CASCADE,
    FOREIGN KEY (device_id) REFERENCES t_video_device(device_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='区域设备关联表';

-- ============================================================
-- 8. 设备用户权限表 (t_video_device_user_permission)
-- ============================================================
CREATE TABLE t_video_device_user_permission (
    -- 主键
    permission_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID',

    -- 关联信息
    device_id BIGINT NOT NULL COMMENT '设备ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    area_id BIGINT COMMENT '区域ID',
    role_id BIGINT COMMENT '角色ID',

    -- 用户信息
    user_name VARCHAR(50) NOT NULL COMMENT '用户姓名',
    user_department VARCHAR(100) COMMENT '用户部门',
    user_position VARCHAR(50) COMMENT '用户职位',
    user_ip VARCHAR(45) COMMENT '用户IP',

    -- 权限配置
    view_permission TINYINT DEFAULT 1 COMMENT '查看权限：0-禁止 1-允许',
    control_permission TINYINT DEFAULT 0 COMMENT '控制权限：0-禁止 1-允许',
    config_permission TINYINT DEFAULT 0 COMMENT '配置权限：0-禁止 1-允许',
    download_permission TINYINT DEFAULT 0 COMMENT '下载权限：0-禁止 1-允许',

    -- 功能权限
    real_time_monitoring TINYINT DEFAULT 1 COMMENT '实时监控：0-禁用 1-启用',
    recording_permission TINYINT DEFAULT 0 COMMENT '录制权限：0-禁用 1-启用',
    playback_permission TINYINT DEFAULT 1 COMMENT '回放权限：0-禁用 1-启用',
    snapshot_permission TINYINT DEFAULT 1 COMMENT '截图权限：0-禁用 1-启用',
    ptz_control_permission TINYINT DEFAULT 0 COMMENT 'PTZ控制权限：0-禁用 1-启用',

    -- 访问限制
    access_time_range TEXT COMMENT '访问时间范围(JSON格式)',
    access_days VARCHAR(20) COMMENT '允许访问日期',
    max_concurrent_sessions INT COMMENT '最大并发会话数',
    bandwidth_limit INT COMMENT '带宽限制(kbps)',

    -- 权限级别
    access_level TINYINT DEFAULT 1 COMMENT '访问权限级别：1-公开 2-内部 3-机密 4-秘密',
    permission_level TINYINT DEFAULT 1 COMMENT '权限等级：1-用户 2-操作员 3-管理员',

    -- 状态信息
    permission_status TINYINT DEFAULT 1 COMMENT '权限状态：0-禁用 1-启用 2-过期',
    effective_time DATETIME COMMENT '生效时间',
    expire_time DATETIME COMMENT '失效时间',
    last_access_time DATETIME COMMENT '最后访问时间',

    -- 授权信息
    grant_user_id BIGINT COMMENT '授权人ID',
    grant_user_name VARCHAR(50) COMMENT '授权人姓名',
    grant_reason VARCHAR(200) COMMENT '授权原因',
    grant_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '授权时间',

    -- 使用统计
    access_count INT DEFAULT 0 COMMENT '访问次数',
    control_count INT DEFAULT 0 COMMENT '控制次数',
    download_count INT DEFAULT 0 COMMENT '下载次数',
    total_usage_time INT DEFAULT 0 COMMENT '总使用时长(分钟)',

    -- 安全配置
    require_verification TINYINT DEFAULT 0 COMMENT '是否需要验证：0-否 1-是',
    verification_method VARCHAR(50) COMMENT '验证方式：password-fingerprint-face-sms',
    log_access TINYINT DEFAULT 1 COMMENT '记录访问日志：0-否 1-是',

    -- 扩展配置
    extended_config TEXT COMMENT '扩展配置(JSON格式)',
    custom_permissions TEXT COMMENT '自定义权限(JSON格式)',

    -- 审计字段
    create_user_id BIGINT COMMENT '创建人ID',
    create_user_name VARCHAR(50) COMMENT '创建人姓名',
    update_user_id BIGINT COMMENT '更新人ID',
    update_user_name VARCHAR(50) COMMENT '更新人姓名',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    version INT NOT NULL DEFAULT 1 COMMENT '版本号(乐观锁)',

    -- 索引
    INDEX idx_device_id (device_id),
    INDEX idx_user_id (user_id),
    INDEX idx_area_id (area_id),
    INDEX idx_role_id (role_id),
    INDEX idx_user_name (user_name),
    INDEX idx_permission_status (permission_status),
    INDEX idx_access_level (access_level),
    INDEX idx_permission_level (permission_level),
    INDEX idx_effective_time (effective_time),
    INDEX idx_expire_time (expire_time),
    INDEX idx_last_access_time (last_access_time),
    INDEX idx_grant_user_id (grant_user_id),
    INDEX idx_deleted_flag (deleted_flag),
    INDEX idx_create_time (create_time),

    -- 复合索引
    INDEX idx_device_user (device_id, user_id, deleted_flag),
    INDEX idx_user_device_time (user_id, device_id, effective_time, expire_time, deleted_flag),
    INDEX idx_area_user_time (area_id, user_id, effective_time, expire_time, deleted_flag),
    INDEX idx_status_time (permission_status, effective_time, expire_time, deleted_flag),
    INDEX idx_level_time (permission_level, effective_time, expire_time, deleted_flag),

    -- 唯一索引
    UNIQUE INDEX uk_device_user (device_id, user_id, deleted_flag),

    -- 外键约束
    FOREIGN KEY (device_id) REFERENCES t_video_device(device_id) ON DELETE CASCADE,
    FOREIGN KEY (area_id) REFERENCES t_common_area(area_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备用户权限表';

-- ============================================================
-- 9. 视频任务调度表 (t_video_task_schedule)
-- ============================================================
CREATE TABLE t_video_task_schedule (
    -- 主键
    task_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '任务ID',

    -- 任务基本信息
    task_name VARCHAR(100) NOT NULL COMMENT '任务名称',
    task_type VARCHAR(50) NOT NULL COMMENT '任务类型：recording-backup-cleanup-analysis-report',
    task_category VARCHAR(50) NOT NULL COMMENT '任务类别：manual-auto-scheduled',
    task_description TEXT COMMENT '任务描述',
    task_priority INT DEFAULT 2 COMMENT '任务优先级：1-高 2-中 3-低',

    -- 调度配置
    cron_expression VARCHAR(100) COMMENT 'Cron表达式',
    schedule_type VARCHAR(20) DEFAULT 'cron' COMMENT '调度类型：cron-fixed_rate-fixed_delay',
    schedule_interval INT COMMENT '调度间隔(秒)',
    schedule_delay INT COMMENT '调度延迟(秒)',
    timezone VARCHAR(50) DEFAULT 'Asia/Shanghai' COMMENT '时区',

    -- 执行时间配置
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    execute_once TINYINT DEFAULT 0 COMMENT '是否只执行一次：0-否 1-是',
    repeat_count INT DEFAULT 0 COMMENT '重复次数，0表示无限',
    retry_count INT DEFAULT 3 COMMENT '重试次数',
    retry_interval INT DEFAULT 60 COMMENT '重试间隔(秒)',

    -- 执行条件
    execute_condition TEXT COMMENT '执行条件(JSON格式)',
    device_filter TEXT COMMENT '设备过滤条件(JSON格式)',
    time_filter TEXT COMMENT '时间过滤条件(JSON格式)',

    -- 任务状态
    task_status VARCHAR(20) DEFAULT 'pending' COMMENT '任务状态：pending-running-completed-failed-cancelled-paused',
    execute_status VARCHAR(20) COMMENT '执行状态：idle-running-success-failed-timeout-cancelled',

    -- 执行统计
    execute_count INT DEFAULT 0 COMMENT '执行次数',
    success_count INT DEFAULT 0 COMMENT '成功次数',
    fail_count INT DEFAULT 0 COMMENT '失败次数',
    total_execute_time BIGINT DEFAULT 0 COMMENT '总执行时间(毫秒)',
    avg_execute_time BIGINT DEFAULT 0 COMMENT '平均执行时间(毫秒)',

    -- 最近执行信息
    last_execute_time DATETIME COMMENT '最后执行时间',
    last_success_time DATETIME COMMENT '最后成功时间',
    last_fail_time DATETIME COMMENT '最后失败时间',
    last_fail_message TEXT COMMENT '最后失败信息',

    -- 下次执行时间
    next_execute_time DATETIME COMMENT '下次执行时间',

    -- 用户信息
    create_user_id BIGINT COMMENT '创建人ID',
    create_user_name VARCHAR(50) COMMENT '创建人姓名',
    update_user_id BIGINT COMMENT '更新人ID',
    update_user_name VARCHAR(50) COMMENT '更新人姓名',

    -- 任务参数
    task_parameters TEXT COMMENT '任务参数(JSON格式)',
    execute_result TEXT COMMENT '执行结果(JSON格式)',
    error_message TEXT COMMENT '错误信息',

    -- 通知配置
    notification_enabled TINYINT DEFAULT 0 COMMENT '是否启用通知：0-否 1-是',
    notification_on_success TINYINT DEFAULT 0 COMMENT '成功时通知：0-否 1-是',
    notification_on_failure TINYINT DEFAULT 1 COMMENT '失败时通知：0-否 1-是',
    notification_recipients TEXT COMMENT '通知接收者(JSON格式)',

    -- 扩展配置
    extended_config TEXT COMMENT '扩展配置(JSON格式)',
    custom_settings TEXT COMMENT '自定义设置(JSON格式)',

    -- 审计字段
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    version INT NOT NULL DEFAULT 1 COMMENT '版本号(乐观锁)',

    -- 索引
    INDEX idx_task_name (task_name),
    INDEX idx_task_type (task_type),
    INDEX idx_task_category (task_category),
    INDEX idx_task_priority (task_priority),
    INDEX idx_cron_expression (cron_expression),
    INDEX idx_schedule_type (schedule_type),
    INDEX idx_task_status (task_status),
    INDEX idx_execute_status (execute_status),
    INDEX idx_start_time (start_time),
    INDEX idx_end_time (end_time),
    INDEX idx_next_execute_time (next_execute_time),
    INDEX idx_last_execute_time (last_execute_time),
    INDEX idx_create_user_id (create_user_id),
    INDEX idx_deleted_flag (deleted_flag),
    INDEX idx_create_time (create_time),

    -- 复合索引
    INDEX idx_type_status (task_type, task_status, deleted_flag),
    INDEX idx_status_time (task_status, next_execute_time, deleted_flag),
    INDEX idx_priority_status (task_priority, task_status, deleted_flag),
    INDEX idx_schedule_time (schedule_type, next_execute_time, deleted_flag),

    -- 全文索引
    FULLTEXT idx_task_content (task_name, task_description)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频任务调度表';

-- ============================================================
-- 10. 视频操作日志表 (t_video_operation_log)
-- ============================================================
CREATE TABLE t_video_operation_log (
    -- 主键
    log_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',

    -- 关联信息
    device_id BIGINT COMMENT '设备ID',
    device_code VARCHAR(50) COMMENT '设备编号',
    device_name VARCHAR(100) COMMENT '设备名称',
    channel_id BIGINT COMMENT '通道ID',
    channel_name VARCHAR(50) COMMENT '通道名称',
    stream_id BIGINT COMMENT '流ID',
    recording_id BIGINT COMMENT '录像ID',

    -- 用户信息
    user_id BIGINT NOT NULL COMMENT '用户ID',
    user_name VARCHAR(50) NOT NULL COMMENT '用户姓名',
    user_department VARCHAR(100) COMMENT '用户部门',
    user_role VARCHAR(50) COMMENT '用户角色',
    user_ip VARCHAR(45) COMMENT '用户IP',
    user_agent VARCHAR(500) COMMENT '用户代理',

    -- 操作信息
    operation_type VARCHAR(50) NOT NULL COMMENT '操作类型：start-stream-stop-stream-pause-stream-resume-stream-switch-quality-capture-recording-start-recording-stop-download-playback-delete-ptz-control',
    operation_action VARCHAR(50) NOT NULL COMMENT '操作动作',
    operation_description TEXT COMMENT '操作描述',
    operation_parameters TEXT COMMENT '操作参数(JSON格式)',
    operation_result TEXT COMMENT '操作结果(JSON格式)',

    -- 状态信息
    operation_status VARCHAR(20) DEFAULT 'success' COMMENT '操作状态：success-failed-timeout-cancelled-pending',
    error_code VARCHAR(50) COMMENT '错误码',
    error_message TEXT COMMENT '错误信息',
    http_status_code INT COMMENT 'HTTP状态码',

    -- 执行信息
    execute_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '执行时间',
    execute_duration INT COMMENT '执行时长(毫秒)',
    server_response_time INT COMMENT '服务器响应时间(毫秒)',

    -- 资源使用
    cpu_usage DECIMAL(5,2) COMMENT 'CPU使用率(%)',
    memory_usage DECIMAL(5,2) COMMENT '内存使用率(%)',
    network_usage BIGINT COMMENT '网络使用量(字节)',

    -- 安全信息
    access_token VARCHAR(500) COMMENT '访问令牌',
    session_id VARCHAR(100) COMMENT '会话ID',
    request_id VARCHAR(100) COMMENT '请求ID',
    trace_id VARCHAR(100) COMMENT '追踪ID',

    -- 地理位置
    client_country VARCHAR(50) COMMENT '客户端国家',
    client_region VARCHAR(50) COMMENT '客户端地区',
    client_city VARCHAR(50) COMMENT '客户端城市',
    client_location VARCHAR(200) COMMENT '客户端位置',

    -- 设备信息
    client_device_type VARCHAR(50) COMMENT '客户端设备类型',
    client_os VARCHAR(50) COMMENT '客户端操作系统',
    client_browser VARCHAR(100) COMMENT '客户端浏览器',

    -- 扩展信息
    extended_info TEXT COMMENT '扩展信息(JSON格式)',
    custom_fields TEXT COMMENT '自定义字段(JSON格式)',

    -- 索引
    INDEX idx_device_id (device_id),
    INDEX idx_device_code (device_code),
    INDEX idx_user_id (user_id),
    INDEX idx_user_name (user_name),
    INDEX idx_operation_type (operation_type),
    INDEX idx_operation_status (operation_status),
    INDEX idx_error_code (error_code),
    INDEX idx_execute_time (execute_time),
    INDEX idx_user_ip (user_ip),
    INDEX idx_session_id (session_id),
    INDEX idx_request_id (request_id),
    INDEX idx_trace_id (trace_id),

    -- 复合索引
    INDEX idx_device_user_time (device_id, user_id, execute_time),
    INDEX idx_user_type_time (user_id, operation_type, execute_time),
    INDEX idx_status_time (operation_status, execute_time),
    INDEX idx_ip_time (user_ip, execute_time),
    INDEX idx_device_operation_time (device_id, operation_type, execute_time),

    -- 分区索引（按时间分区）
    -- 需要配置自动分区

    -- 全文索引
    FULLTEXT idx_log_content (operation_description, error_message, custom_fields)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频操作日志表';

-- ============================================================
-- 创建触发器
-- ============================================================

-- 设备状态变更触发器
DELIMITER //
CREATE TRIGGER tr_video_device_status_update
AFTER UPDATE ON t_video_device
FOR EACH ROW
BEGIN
    -- 状态变更时记录日志
    IF OLD.device_status <> NEW.device_status THEN
        INSERT INTO t_video_operation_log (
            device_id, device_code, device_name, user_id, user_name,
            operation_type, operation_action, operation_description,
            operation_status, execute_time
        ) VALUES (
            NEW.device_id, NEW.device_code, NEW.device_name,
            NEW.update_user_id, NEW.update_user_name,
            'device-status-change', 'update',
            CONCAT('设备状态变更: ', OLD.device_status, ' -> ', NEW.device_status),
            'success', NOW()
        );
    END IF;
END//
DELIMITER ;

-- 录像重要标记变更触发器
DELIMITER //
CREATE TRIGGER tr_video_recording_important_update
AFTER UPDATE ON t_video_recording
FOR EACH ROW
BEGIN
    -- 重要标记变更时记录日志
    IF OLD.important <> NEW.important THEN
        INSERT INTO t_video_operation_log (
            device_id, recording_id, user_id, user_name,
            operation_type, operation_action, operation_description,
            operation_status, execute_time
        ) VALUES (
            NEW.device_id, NEW.recording_id,
            NEW.update_user_id, NEW.update_user_name,
            'recording-mark-important', 'update',
            CONCAT('录像重要标记变更: ', OLD.important, ' -> ', NEW.important),
            'success', NOW()
        );
    END IF;
END//
DELIMITER ;

-- 事件处理触发器
DELIMITER //
CREATE TRIGGER tr_video_event_process_update
AFTER UPDATE ON t_video_event
FOR EACH ROW
BEGIN
    -- 事件状态变更时记录日志
    IF OLD.event_status <> NEW.event_status THEN
        INSERT INTO t_video_operation_log (
            device_id, recording_id, user_id, user_name,
            operation_type, operation_action, operation_description,
            operation_status, execute_time
        ) VALUES (
            NEW.device_id, NEW.recording_id,
            NEW.process_user_id, NEW.process_user_name,
            'event-status-update', 'update',
            CONCAT('事件状态变更: ', OLD.event_status, ' -> ', NEW.event_status),
            'success', NOW()
        );
    END IF;
END//
DELIMITER ;

-- ============================================================
-- 创建视图
-- ============================================================

-- 设备状态统计视图
CREATE VIEW v_video_device_status_stats AS
SELECT
    device_type,
    device_status,
    COUNT(*) AS device_count,
    COUNT(CASE WHEN online_time IS NOT NULL THEN 1 END) AS online_count,
    AVG(cpu_usage) AS avg_cpu_usage,
    AVG(memory_usage) AS avg_memory_usage,
    AVG(disk_usage) AS avg_disk_usage
FROM t_video_device
WHERE deleted_flag = 0
GROUP BY device_type, device_status;

-- 录像统计视图
CREATE VIEW v_video_recording_stats AS
SELECT
    DATE(recording_start_time) AS recording_date,
    recording_type,
    recording_quality,
    COUNT(*) AS recording_count,
    SUM(file_size) AS total_size,
    AVG(recording_duration) AS avg_duration,
    SUM(CASE WHEN important = 1 THEN 1 ELSE 0 END) AS important_count,
    SUM(download_count) AS total_downloads,
    SUM(play_count) AS total_plays
FROM t_video_recording
WHERE deleted_flag = 0
GROUP BY DATE(recording_start_time), recording_type, recording_quality;

-- 事件统计视图
CREATE VIEW v_video_event_stats AS
SELECT
    DATE(event_time) AS event_date,
    event_type,
    event_level,
    event_status,
    COUNT(*) AS event_count,
    AVG(event_confidence) AS avg_confidence,
    COUNT(CASE WHEN alarm_triggered = 1 THEN 1 END) AS alarm_count
FROM t_video_event
WHERE deleted_flag = 0
GROUP BY DATE(event_time), event_type, event_level, event_status;

-- 用户访问统计视图
CREATE VIEW v_video_user_access_stats AS
SELECT
    DATE(execute_time) AS access_date,
    user_id,
    user_name,
    COUNT(*) AS access_count,
    COUNT(DISTINCT device_id) AS device_count,
    SUM(execute_duration) AS total_duration,
    AVG(execute_duration) AS avg_duration
FROM t_video_operation_log
WHERE operation_type IN ('start-stream', 'playback', 'download')
GROUP BY DATE(execute_time), user_id, user_name;

-- ============================================================
-- 表创建完成日志
-- ============================================================
INSERT INTO flyway_schema_history (
    installed_rank,
    version,
    description,
    type,
    script,
    installed_by,
    installed_on,
    execution_time,
    success
) VALUES (
    2,
    '20251216.002',
    'Create video service association tables and triggers',
    'SQL',
    'V20251216__002_create_association_tables.sql',
    'flyway',
    NOW(),
    0,
    1
);