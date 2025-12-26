-- ==================== Seata Undo Log表 ====================
-- 用于Seata AT模式的回滚日志
-- 分布式事务回滚时使用
-- 创建时间: 2025-12-23
-- 作者: IOE-DREAM架构团队

CREATE TABLE IF NOT EXISTS `undo_log` (
  `branch_id` BIGINT NOT NULL COMMENT '分支事务ID',
  `xid` VARCHAR(128) NOT NULL COMMENT '全局事务ID',
  `context` VARCHAR(128) NOT NULL COMMENT '上下文',
  `rollback_info` LONGBLOB NOT NULL COMMENT '回滚数据',
  `log_status` INT NOT NULL COMMENT '状态（0-正常，1-已完成回滚）',
  `log_created` DATETIME NOT NULL COMMENT '创建时间',
  `log_modified` DATETIME NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`branch_id`),
  KEY `idx_xid` (`xid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Seata AT模式回滚日志表';

-- 添加索引优化查询性能
CREATE INDEX idx_log_status ON undo_log(log_status);
CREATE INDEX idx_log_created ON undo_log(log_created);
