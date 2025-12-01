-- SmartAdmin v3 门禁系统模块数据库建表脚本
-- 创建时间: 2025-11-14
-- 作用: 创建门禁管理系统所需的所有数据表

-- ================================
-- 1. 门禁设备表 (smart_access_device)
-- ================================
DROP TABLE IF EXISTS `smart_access_device`;
CREATE TABLE `smart_access_device` (
    `device_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '设备ID',
    `device_no` VARCHAR(50) NOT NULL COMMENT '设备编号',
    `device_name` VARCHAR(100) NOT NULL COMMENT '设备名称',
    `device_type` VARCHAR(20) NOT NULL COMMENT '设备类型: DOOR-门禁, GATE-闸机, TURNSTILE-转闸',
    `location` VARCHAR(200) NOT NULL COMMENT '设备位置',
    `ip_address` VARCHAR(15) NOT NULL COMMENT 'IP地址',
    `port` INT NOT NULL COMMENT '端口号',
    `protocol_type` VARCHAR(20) NOT NULL COMMENT '协议类型: TCP, UDP, HTTP, WEBSOCKET',
    `device_status` VARCHAR(20) NOT NULL DEFAULT 'OFFLINE' COMMENT '设备状态: ONLINE-在线, OFFLINE-离线, FAULT-故障',
    `last_comm_time` DATETIME NULL COMMENT '最后通信时间',
    `device_config` TEXT COMMENT '设备配置(JSON格式)',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT(20) NULL COMMENT '创建人ID',
    `deleted_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标志: 0-正常, 1-已删除',
    `remark` VARCHAR(500) COMMENT '备注信息',
    PRIMARY KEY (`device_id`),
    UNIQUE KEY `uk_device_no` (`device_no`, `deleted_flag`),
    KEY `idx_device_type` (`device_type`),
    KEY `idx_device_status` (`device_status`),
    KEY `idx_location` (`location`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门禁设备表';

-- ================================
-- 2. 门禁权限表 (smart_access_permission)
-- ================================
DROP TABLE IF EXISTS `smart_access_permission`;
CREATE TABLE `smart_access_permission` (
    `permission_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '权限ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `device_id` BIGINT(20) NOT NULL COMMENT '设备ID',
    `permission_type` VARCHAR(20) NOT NULL COMMENT '权限类型: TEMPORARY-临时, PERMANENT-永久',
    `start_time` DATETIME NOT NULL COMMENT '有效开始时间',
    `end_time` DATETIME NOT NULL COMMENT '有效结束时间',
    `permission_status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '权限状态: PENDING-待审批, APPROVED-已批准, REJECTED-已拒绝, EXPIRED-已过期',
    `apply_reason` TEXT COMMENT '申请原因',
    `approver_id` BIGINT(20) NULL COMMENT '审批人ID',
    `approve_time` DATETIME NULL COMMENT '审批时间',
    `approve_remark` VARCHAR(500) COMMENT '审批意见',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT(20) NULL COMMENT '创建人ID',
    `deleted_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标志: 0-正常, 1-已删除',
    `remark` VARCHAR(500) COMMENT '备注信息',
    PRIMARY KEY (`permission_id`),
    UNIQUE KEY `uk_user_device` (`user_id`, `device_id`, `deleted_flag`),
    KEY `idx_permission_status` (`permission_status`),
    KEY `idx_permission_type` (`permission_type`),
    KEY `idx_start_end_time` (`start_time`, `end_time`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门禁权限表';

-- ================================
-- 3. 门禁通行记录表 (smart_access_record)
-- ================================
DROP TABLE IF EXISTS `smart_access_record`;
CREATE TABLE `smart_access_record` (
    `record_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `user_id` BIGINT(20) NULL COMMENT '用户ID',
    `device_id` BIGINT(20) NOT NULL COMMENT '设备ID',
    `card_no` VARCHAR(50) COMMENT '卡片号',
    `access_type` VARCHAR(20) NOT NULL COMMENT '通行类型: IN-进入, OUT-退出, UNKNOWN-未知',
    `access_result` VARCHAR(20) NOT NULL COMMENT '通行结果: SUCCESS-成功, FAILED-失败, DENIED-拒绝',
    `access_time` DATETIME NOT NULL COMMENT '通行时间',
    `permission_check` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '权限验证结果: 0-失败, 1-成功',
    `photo_url` VARCHAR(500) COMMENT '照片URL',
    `device_snapshot` TEXT COMMENT '设备快照',
    `error_message` VARCHAR(500) COMMENT '异常信息',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标志: 0-正常, 1-已删除',
    `remark` VARCHAR(500) COMMENT '备注',
    PRIMARY KEY (`record_id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_access_time` (`access_time`),
    KEY `idx_access_result` (`access_result`),
    KEY `idx_card_no` (`card_no`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门禁通行记录表';

-- ================================
-- 4. 门禁设备指令表 (smart_access_command)
-- ================================
DROP TABLE IF EXISTS `smart_access_command`;
CREATE TABLE `smart_access_command` (
    `command_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '指令ID',
    `device_id` BIGINT(20) NOT NULL COMMENT '设备ID',
    `command_type` VARCHAR(50) NOT NULL COMMENT '指令类型: OPEN-开门, CLOSE-关门, RESTART-重启, SYNC_TIME-同步时间, CONFIG_UPDATE-配置更新',
    `command_content` TEXT COMMENT '指令内容(JSON格式)',
    `command_status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '指令状态: PENDING-待执行, EXECUTING-执行中, SUCCESS-成功, FAILED-失败, TIMEOUT-超时',
    `retry_count` INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    `max_retry_count` INT NOT NULL DEFAULT 3 COMMENT '最大重试次数',
    `execute_time` DATETIME NULL COMMENT '执行时间',
    `complete_time` DATETIME NULL COMMENT '完成时间',
    `execute_result` TEXT COMMENT '执行结果',
    `error_message` VARCHAR(1000) COMMENT '错误信息',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT(20) NULL COMMENT '创建人ID',
    `deleted_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标志: 0-正常, 1-已删除',
    `remark` VARCHAR(500) COMMENT '备注信息',
    PRIMARY KEY (`command_id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_command_status` (`command_status`),
    KEY `idx_command_type` (`command_type`),
    KEY `idx_execute_time` (`execute_time`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门禁设备指令表';

-- ================================
-- 5. 门禁设备状态表 (smart_access_device_status)
-- ================================
DROP TABLE IF EXISTS `smart_access_device_status`;
CREATE TABLE `smart_access_device_status` (
    `status_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '状态ID',
    `device_id` BIGINT(20) NOT NULL COMMENT '设备ID',
    `device_status` VARCHAR(20) NOT NULL COMMENT '设备状态: ONLINE-在线, OFFLINE-离线, FAULT-故障, MAINTENANCE-维护中',
    `cpu_usage` DECIMAL(5,2) COMMENT 'CPU使用率(百分比)',
    `memory_usage` DECIMAL(5,2) COMMENT '内存使用率(百分比)',
    `disk_usage` DECIMAL(5,2) COMMENT '磁盘使用率(百分比)',
    `network_latency` INT COMMENT '网络延迟(毫秒)',
    `temperature` DECIMAL(5,2) COMMENT '温度(摄氏度)',
    `power_status` VARCHAR(20) COMMENT '电源状态: NORMAL-正常, LOW-低电量, POWER_OFF-断电',
    `network_status` VARCHAR(20) COMMENT '网络状态: CONNECTED-已连接, DISCONNECTED-断开, WEAK-信号弱',
    `door_status` VARCHAR(20) COMMENT '门状态: OPEN-开启, CLOSED-关闭, LOCKED-锁定, UNKNOWN-未知',
    `fault_code` VARCHAR(50) COMMENT '故障代码',
    `fault_description` VARCHAR(500) COMMENT '故障描述',
    `last_comm_time` DATETIME NULL COMMENT '最后通信时间',
    `heartbeat_time` DATETIME NULL COMMENT '心跳时间',
    `status_data` TEXT COMMENT '状态数据(JSON格式，扩展字段)',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标志: 0-正常, 1-已删除',
    `remark` VARCHAR(500) COMMENT '备注',
    PRIMARY KEY (`status_id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_device_status` (`device_status`),
    KEY `idx_heartbeat_time` (`heartbeat_time`),
    KEY `idx_last_comm_time` (`last_comm_time`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门禁设备状态表';

-- ================================
-- 6. 创建外键约束
-- ================================
-- 权限表外键约束
ALTER TABLE `smart_access_permission`
ADD CONSTRAINT `fk_permission_device_id` FOREIGN KEY (`device_id`) REFERENCES `smart_access_device` (`device_id`) ON DELETE CASCADE;

-- 通行记录表外键约束
ALTER TABLE `smart_access_record`
ADD CONSTRAINT `fk_record_device_id` FOREIGN KEY (`device_id`) REFERENCES `smart_access_device` (`device_id`) ON DELETE CASCADE;

-- 设备指令表外键约束
ALTER TABLE `smart_access_command`
ADD CONSTRAINT `fk_command_device_id` FOREIGN KEY (`device_id`) REFERENCES `smart_access_device` (`device_id`) ON DELETE CASCADE;

-- 设备状态表外键约束
ALTER TABLE `smart_access_device_status`
ADD CONSTRAINT `fk_status_device_id` FOREIGN KEY (`device_id`) REFERENCES `smart_access_device` (`device_id`) ON DELETE CASCADE;

-- ================================
-- 7. 插入初始化数据
-- ================================
-- 插入示例门禁设备数据
INSERT INTO `smart_access_device`
(`device_no`, `device_name`, `device_type`, `location`, `ip_address`, `port`, `protocol_type`, `device_status`, `create_user_id`, `remark`) VALUES
('DEV001', '主大门门禁', 'DOOR', '办公楼主入口', '192.168.1.101', 8080, 'TCP', 'ONLINE', 1, '主大门门禁设备，支持IC卡和指纹识别'),
('DEV002', '侧门门禁', 'DOOR', '办公楼侧门', '192.168.1.102', 8080, 'TCP', 'ONLINE', 1, '侧门门禁设备，支持IC卡和人脸识别'),
('DEV003', '停车场入口闸机', 'GATE', '停车场入口', '192.168.1.103', 8081, 'TCP', 'OFFLINE', 1, '停车场入口闸机，支持车牌识别'),
('DEV004', '员工通道转闸', 'TURNSTILE', '员工通道', '192.168.1.104', 8082, 'HTTP', 'ONLINE', 1, '员工通道转闸设备');

-- ================================
-- 8. 创建索引优化查询性能
-- ================================
-- 复合索引
CREATE INDEX idx_permission_user_device_status ON `smart_access_permission`(`user_id`, `device_id`, `permission_status`);
CREATE INDEX idx_record_device_access_time ON `smart_access_record`(`device_id`, `access_time`);
CREATE INDEX idx_command_device_status_time ON `smart_access_command`(`device_id`, `command_status`, `execute_time`);
CREATE INDEX idx_status_device_heartbeat ON `smart_access_device_status`(`device_id`, `heartbeat_time`);

-- ================================
-- 9. 创建视图便于查询
-- ================================
-- 设备状态统计视图
CREATE OR REPLACE VIEW `v_device_status_summary` AS
SELECT
    d.device_id,
    d.device_no,
    d.device_name,
    d.device_type,
    d.location,
    d.device_status,
    ds.cpu_usage,
    ds.memory_usage,
    ds.network_latency,
    ds.heartbeat_time,
    ds.last_comm_time,
    CASE
        WHEN d.device_status = 'ONLINE' AND ds.heartbeat_time >= DATE_SUB(NOW(), INTERVAL 5 MINUTE) THEN '正常'
        WHEN d.device_status = 'OFFLINE' THEN '离线'
        WHEN d.device_status = 'FAULT' THEN '故障'
        ELSE '异常'
    END AS status_summary
FROM `smart_access_device` d
LEFT JOIN `smart_access_device_status` ds ON d.device_id = ds.device_id
WHERE d.deleted_flag = 0;

-- 权限统计视图
CREATE OR REPLACE VIEW `v_permission_summary` AS
SELECT
    COUNT(*) AS total_permissions,
    COUNT(CASE WHEN permission_status = 'APPROVED' AND end_time > NOW() THEN 1 END) AS active_permissions,
    COUNT(CASE WHEN permission_status = 'PENDING' THEN 1 END) AS pending_permissions,
    COUNT(CASE WHEN permission_status = 'EXPIRED' OR end_time <= NOW() THEN 1 END) AS expired_permissions,
    COUNT(CASE WHEN permission_type = 'PERMANENT' THEN 1 END) AS permanent_permissions,
    COUNT(CASE WHEN permission_type = 'TEMPORARY' THEN 1 END) AS temporary_permissions
FROM `smart_access_permission`
WHERE deleted_flag = 0;

-- ================================
-- 脚本执行完成
-- ================================
-- 执行时间: 2025-11-14
-- 说明: 智能门禁系统数据库表结构创建完成，包含完整的设备管理、权限控制、通行记录、设备指令和状态监控功能