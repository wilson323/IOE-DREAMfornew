-- ============================================================
-- Seata Undo Log表创建脚本
-- ============================================================
-- 说明: 每个业务数据库都需要创建此表，用于Seata分布式事务的undo日志
-- 执行: 在每个微服务的业务数据库中执行此脚本
-- ============================================================

-- 创建undo_log表
CREATE TABLE IF NOT EXISTS `undo_log`
(
    `branch_id`     BIGINT       NOT NULL COMMENT 'branch transaction id',
    `xid`           VARCHAR(128) NOT NULL COMMENT 'global transaction id',
    `context`       VARCHAR(128) NOT NULL COMMENT 'undo_log context,such as serialization',
    `rollback_info` LONGBLOB     NOT NULL COMMENT 'rollback info',
    `log_status`    INT          NOT NULL COMMENT '0:normal status,1:defense status',
    `log_created`   DATETIME(6)  NOT NULL COMMENT 'create datetime',
    `log_modified`  DATETIME(6)  NOT NULL COMMENT 'modify datetime',
    UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4 COMMENT ='AT transaction mode undo table';

-- 创建索引
CREATE INDEX `idx_log_created` ON `undo_log` (`log_created`);
CREATE INDEX `idx_xid` ON `undo_log` (`xid`);

