-- ============================================================
-- 离线消费记录表
-- 模块: consume-service
-- 作者: IOE-DREAM Team
-- 创建日期: 2025-12-14
-- ============================================================

CREATE TABLE IF NOT EXISTS `t_offline_consume_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `offline_trans_no` VARCHAR(64) NOT NULL COMMENT '离线交易编号（设备端生成）',
    `device_id` BIGINT NOT NULL COMMENT '设备ID',
    `device_sn` VARCHAR(64) DEFAULT NULL COMMENT '设备序列号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `account_id` BIGINT NOT NULL COMMENT '账户ID',
    `card_no` VARCHAR(32) DEFAULT NULL COMMENT '卡号',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '消费金额',
    `balance_before_consume` DECIMAL(10,2) DEFAULT NULL COMMENT '离线时账户余额',
    `balance_after_consume` DECIMAL(10,2) DEFAULT NULL COMMENT '离线消费后余额',
    `consume_time` DATETIME NOT NULL COMMENT '消费时间（设备端记录）',
    `sync_status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '同步状态：PENDING-待同步, SYNCED-已同步, CONFLICT-冲突, FAILED-失败',
    `sync_time` DATETIME DEFAULT NULL COMMENT '同步时间',
    `sync_message` VARCHAR(500) DEFAULT NULL COMMENT '同步结果消息',
    `retry_count` INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    `online_record_id` BIGINT DEFAULT NULL COMMENT '关联的在线消费记录ID',
    `conflict_type` VARCHAR(20) DEFAULT NULL COMMENT '冲突类型：BALANCE-余额不一致, DUPLICATE-重复消费, OTHER-其他',
    `conflict_status` VARCHAR(20) DEFAULT NULL COMMENT '冲突处理状态：PENDING-待处理, RESOLVED-已解决, IGNORED-已忽略',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_offline_trans_no` (`offline_trans_no`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_account_id` (`account_id`),
    KEY `idx_sync_status` (`sync_status`),
    KEY `idx_consume_time` (`consume_time`),
    KEY `idx_conflict_status` (`conflict_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='离线消费记录表';

-- 添加复合索引优化同步查询
CREATE INDEX idx_offline_sync_pending ON t_offline_consume_record(sync_status, retry_count, consume_time);
CREATE INDEX idx_offline_conflict ON t_offline_consume_record(sync_status, conflict_status);
