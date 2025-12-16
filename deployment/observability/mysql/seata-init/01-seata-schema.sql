-- Seata数据库初始化脚本
CREATE DATABASE IF NOT EXISTS seata CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE seata;

-- 创建seata用户并授权
CREATE USER IF NOT EXISTS 'seata'@'%' IDENTIFIED BY 'K1M2N3O4P5Q6R7S8T9U0V1W2X3Y4Z5A';
GRANT ALL PRIVILEGES ON seata.* TO 'seata'@'%';
FLUSH PRIVILEGES;

-- Seata全局事务表
CREATE TABLE IF NOT EXISTS `global_table` (
    `xid` VARCHAR(128) NOT NULL,
    `transaction_id` BIGINT,
    `status` TINYINT NOT NULL,
    `application_id` VARCHAR(32),
    `transaction_service_group` VARCHAR(32),
    `transaction_name` VARCHAR(128),
    `timeout` INT,
    `begin_time` BIGINT,
    `application_data` VARCHAR(2000),
    `gmt_create` DATETIME,
    `gmt_modified` DATETIME,
    PRIMARY KEY (`xid`),
    KEY `idx_status_gmt_modified` (`status`, `gmt_modified`),
    KEY `idx_transaction_id` (`transaction_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seata分支事务表
CREATE TABLE IF NOT EXISTS `branch_table` (
    `branch_id` BIGINT NOT NULL,
    `xid` VARCHAR(128) NOT NULL,
    `transaction_id` BIGINT,
    `resource_group_id` VARCHAR(32),
    `resource_id` VARCHAR(256),
    `branch_type` VARCHAR(8),
    `status` TINYINT,
    `client_id` VARCHAR(64),
    `application_data` VARCHAR(2000),
    `gmt_create` DATETIME,
    `gmt_modified` DATETIME,
    PRIMARY KEY (`branch_id`),
    KEY `idx_xid` (`xid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
