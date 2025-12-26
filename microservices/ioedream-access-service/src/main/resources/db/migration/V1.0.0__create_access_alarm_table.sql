-- ============================================================================
-- 门禁报警表
-- ============================================================================
-- 功能说明: 记录门禁系统的各种报警事件
-- 作者: IOE-DREAM Team
-- 创建时间: 2025-01-30
-- ============================================================================

-- 创建门禁报警表
CREATE TABLE IF NOT EXISTS `t_access_alarm` (
    -- 报警基本信息
    `alarm_id` BIGINT NOT NULL COMMENT '报警ID（主键）',
    `alarm_type` VARCHAR(50) NOT NULL COMMENT '报警类型: DEVICE_OFFLINE-设备离线, ILLEGAL_ENTRY-非法闯入, DOOR_TIMEOUT-门超时未关, FORCED_ENTRY-强力破门, DURESS_ALARM-胁迫报警, CARD_ABNORMAL-卡片异常',
    `alarm_level` INT NOT NULL DEFAULT 2 COMMENT '报警级别: 1-低级（信息提示）, 2-中级（需要注意）, 3-高级（严重警告）, 4-紧急（立即处理）, 5-严重（危急）',

    -- 设备信息
    `device_id` BIGINT COMMENT '设备ID',
    `device_code` VARCHAR(100) COMMENT '设备编码',
    `device_name` VARCHAR(200) COMMENT '设备名称',

    -- 区域信息
    `area_id` BIGINT COMMENT '区域ID',
    `area_name` VARCHAR(200) COMMENT '区域名称',

    -- 用户信息
    `user_id` BIGINT COMMENT '用户ID（可能为空，如设备离线报警）',
    `user_name` VARCHAR(100) COMMENT '用户姓名',

    -- 关联信息
    `access_record_id` BIGINT COMMENT '通行记录ID（可能为空）',

    -- 报警时间信息
    `alarm_time` DATETIME NOT NULL COMMENT '报警时间',
    `alarm_date` DATE NOT NULL COMMENT '报警日期（用于按日期查询）',

    -- 报警描述和详情
    `alarm_description` VARCHAR(500) COMMENT '报警描述',
    `alarm_detail` TEXT COMMENT '报警详情（JSON格式）',
    `alarm_image_url` VARCHAR(500) COMMENT '报警图片URL（如果有）',
    `alarm_video_url` VARCHAR(500) COMMENT '报警视频片段URL（如果有）',

    -- 处理状态
    `process_status` INT NOT NULL DEFAULT 0 COMMENT '处理状态: 0-未处理, 1-处理中, 2-已处理, 3-已忽略',
    `processed` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已处理: 0-未处理, 1-已处理',
    `handler_id` BIGINT COMMENT '处理人ID',
    `handler_name` VARCHAR(100) COMMENT '处理人姓名',
    `handle_time` DATETIME COMMENT '处理时间',
    `handle_remark` VARCHAR(500) COMMENT '处理备注',
    `handle_result` VARCHAR(200) COMMENT '处理结果',

    -- 确认信息
    `confirmed` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已确认: 0-未确认, 1-已确认',
    `confirmer_id` BIGINT COMMENT '确认人ID',
    `confirm_time` DATETIME COMMENT '确认时间',

    -- 通知信息
    `notification_sent` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已发送通知: 0-未发送, 1-已发送',
    `notification_time` DATETIME COMMENT '通知发送时间',
    `notification_recipients` TEXT COMMENT '通知接收人列表（JSON格式）',

    -- 报警源和状态
    `alarm_source` VARCHAR(50) NOT NULL DEFAULT 'SYSTEM' COMMENT '报警源: SYSTEM-系统自动检测, MANUAL-人工上报, DEVICE-设备上报',
    `alarm_status` VARCHAR(50) NOT NULL DEFAULT 'ACTIVE' COMMENT '报警状态: ACTIVE-活跃（未处理）, ACKNOWLEDGED-已确认, PROCESSING-处理中, RESOLVED-已解决, CLOSED-已关闭, IGNORED-已忽略',
    `priority` INT NOT NULL DEFAULT 5 COMMENT '优先级（用于排序，数值越大优先级越高）',

    -- 重复报警检测
    `repeat_count` INT NOT NULL DEFAULT 0 COMMENT '重复次数（同一类报警的重复次数）',
    `first_alarm_time` DATETIME COMMENT '首次报警时间（用于重复报警）',
    `last_alarm_time` DATETIME COMMENT '最后报警时间（用于重复报警）',

    -- 扩展属性
    `extended_attributes` TEXT COMMENT '扩展属性（JSON格式，存储额外信息）',

    -- 审计字段
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标记: 0-未删除, 1-已删除',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',

    -- 主键索引
    PRIMARY KEY (`alarm_id`),

    -- 业务索引
    INDEX `idx_alarm_time` (`alarm_time` DESC) COMMENT '报警时间索引',
    INDEX `idx_alarm_date` (`alarm_date`) COMMENT '报警日期索引',
    INDEX `idx_alarm_level` (`alarm_level`) COMMENT '报警级别索引',
    INDEX `idx_alarm_type` (`alarm_type`) COMMENT '报警类型索引',
    INDEX `idx_device_id` (`device_id`) COMMENT '设备ID索引',
    INDEX `idx_area_id` (`area_id`) COMMENT '区域ID索引',
    INDEX `idx_user_id` (`user_id`) COMMENT '用户ID索引',
    INDEX `idx_processed` (`processed`) COMMENT '处理状态索引',
    INDEX `idx_alarm_status` (`alarm_status`) COMMENT '报警状态索引',
    INDEX `idx_process_status` (`process_status`) COMMENT '处理状态索引',
    INDEX `idx_create_time` (`create_time` DESC) COMMENT '创建时间索引',

    -- 组合索引（用于常见查询）
    INDEX `idx_device_status_time` (`device_id`, `alarm_status`, `alarm_time` DESC) COMMENT '设备状态时间组合索引',
    INDEX `idx_area_level_time` (`area_id`, `alarm_level`, `alarm_time` DESC) COMMENT '区域级别时间组合索引',
    INDEX `idx_processed_time` (`processed`, `alarm_time` DESC) COMMENT '处理状态时间组合索引',
    INDEX `idx_alarm_date_level` (`alarm_date`, `alarm_level`) COMMENT '日期级别组合索引'

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门禁报警表';

-- ============================================================================
-- 插入初始数据（示例数据，用于测试）
-- ============================================================================

-- 示例报警记录
INSERT INTO `t_access_alarm`
    (`alarm_id`, `alarm_type`, `alarm_level`, `device_id`, `device_code`, `device_name`,
     `area_id`, `area_name`, `alarm_time`, `alarm_date`, `alarm_description`,
     `processed`, `alarm_status`, `priority`)
VALUES
    (1, 'DEVICE_OFFLINE', 3, 1001, 'ACCESS_CTRL_001', '主入口门禁',
     101, 'A栋1楼大厅', NOW(), CURDATE(), '设备离线超过5分钟',
     0, 'ACTIVE', 7),

    (2, 'DOOR_TIMEOUT', 2, 1002, 'ACCESS_CTRL_002', '侧门门禁',
     102, 'A栋2楼办公区', NOW(), CURDATE(), '门超时未关超过30秒',
     0, 'ACTIVE', 5),

    (3, 'ILLEGAL_ENTRY', 4, 1003, 'ACCESS_CTRL_003', '机房门禁',
     103, 'A栋3楼机房', NOW(), CURDATE(), '检测到非法闯入尝试',
     0, 'ACTIVE', 9);

-- ============================================================================
-- 性能优化建议
-- ============================================================================

-- 1. 对于高频查询场景，建议使用覆盖索引
-- 例如: SELECT alarm_id, alarm_type, alarm_level, alarm_time FROM t_access_alarm
--       WHERE device_id = ? AND alarm_status = 'ACTIVE' ORDER BY alarm_time DESC LIMIT 10
-- 可以创建索引: CREATE INDEX idx_device_active_time_cover ON t_access_alarm(device_id, alarm_status, alarm_time DESC, alarm_id, alarm_type, alarm_level);

-- 2. 对于按日期统计的查询，建议分区表
-- ALTER TABLE t_access_alarm PARTITION BY RANGE (TO_DAYS(alarm_date)) (
--     PARTITION p202501 VALUES LESS THAN (TO_DAYS('2025-02-01')),
--     PARTITION p202502 VALUES LESS THAN (TO_DAYS('2025-03-01')),
--     ...
-- );

-- 3. 对于历史数据归档，建议定期迁移旧数据到历史表
-- CREATE TABLE t_access_alarm_history LIKE t_access_alarm;
-- INSERT INTO t_access_alarm_history SELECT * FROM t_access_alarm WHERE alarm_time < DATE_SUB(NOW(), INTERVAL 6 MONTH);
-- DELETE FROM t_access_alarm WHERE alarm_time < DATE_SUB(NOW(), INTERVAL 6 MONTH);
