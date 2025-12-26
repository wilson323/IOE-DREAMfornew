-- ============================================================
-- 双写验证日志表
-- ============================================================
-- 功能：记录新旧表数据一致性验证结果
-- 版本：V20251223
-- 作者：IOE-DREAM架构团队
-- ============================================================

USE ioedream;

-- ============================================================
-- 1. 双写验证日志表
-- ============================================================

CREATE TABLE IF NOT EXISTS `dual_write_validation_log` (
  `log_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `validation_type` VARCHAR(50) NOT NULL COMMENT '验证类型（account/transaction）',
  `total_count` INT NOT NULL DEFAULT 0 COMMENT '总数据量',
  `consistent_count` INT NOT NULL DEFAULT 0 COMMENT '一致数据量',
  `inconsistent_count` INT NOT NULL DEFAULT 0 COMMENT '不一致数据量',
  `consistency_rate` DECIMAL(5,4) NOT NULL DEFAULT 1.0000 COMMENT '一致性比率（0-1）',
  `validation_status` TINYINT NOT NULL DEFAULT 1 COMMENT '验证状态（1-通过 2-失败）',
  `validate_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '验证时间',
  `details` TEXT COMMENT '详细信息（JSON格式）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`log_id`),
  KEY `idx_validation_type` (`validation_type`),
  KEY `idx_validate_time` (`validate_time`),
  KEY `idx_validation_status` (`validation_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='双写验证日志表';

-- ============================================================
-- 2. 数据差异记录表
-- ============================================================

CREATE TABLE IF NOT EXISTS `dual_write_difference_record` (
  `difference_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '差异ID',
  `data_type` VARCHAR(50) NOT NULL COMMENT '数据类型（account/transaction）',
  `data_id` BIGINT NOT NULL COMMENT '数据ID',
  `old_value` TEXT COMMENT '旧表值（JSON格式）',
  `new_value` TEXT COMMENT '新表值（JSON格式）',
  `difference_reason` VARCHAR(500) COMMENT '差异原因',
  `detected_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '检测时间',
  `resolved` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已解决（0-未解决 1-已解决）',
  `resolved_time` DATETIME COMMENT '解决时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`difference_id`),
  KEY `idx_data_type` (`data_type`),
  KEY `idx_data_id` (`data_id`),
  KEY `idx_resolved` (`resolved`),
  KEY `idx_detected_time` (`detected_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据差异记录表';

-- ============================================================
-- 3. 初始化示例数据（可选）
-- ============================================================

-- 插入初始验证日志示例
INSERT INTO `dual_write_validation_log` (
  `validation_type`,
  `total_count`,
  `consistent_count`,
  `inconsistent_count`,
  `consistency_rate`,
  `validation_status`,
  `details`
) VALUES (
  'account',
  0,
  0,
  0,
  1.0000,
  1,
  '{"message": "双写验证服务已启动，等待数据..."}'
);

-- ============================================================
-- 4. 创建视图：验证结果汇总
-- ============================================================

CREATE OR REPLACE VIEW `v_dual_write_validation_summary` AS
SELECT
    validation_type,
    COUNT(*) AS total_validations,
    SUM(CASE WHEN validation_status = 1 THEN 1 ELSE 0 END) AS passed_validations,
    SUM(CASE WHEN validation_status = 2 THEN 1 ELSE 0 END) AS failed_validations,
    AVG(consistency_rate) AS avg_consistency_rate,
    MIN(consistency_rate) AS min_consistency_rate,
    MAX(validate_time) AS last_validate_time
FROM dual_write_validation_log
WHERE validate_time >= DATE_SUB(NOW(), INTERVAL 7 DAY)
GROUP BY validation_type;

-- ============================================================
-- 5. 创建存储过程：清理历史验证日志
-- ============================================================

DELIMITER $$

CREATE PROCEDURE IF NOT EXISTS `sp_cleanup_validation_log`(
  IN p_days INT
)
BEGIN
  DECLARE p_delete_count INT DEFAULT 0;

  -- 删除指定天数前的验证日志
  DELETE FROM dual_write_validation_log
  WHERE validate_time < DATE_SUB(NOW(), INTERVAL p_days DAY);

  SET p_delete_count = ROW_COUNT();

  -- 删除指定天数前的已解决差异记录
  DELETE FROM dual_write_difference_record
  WHERE resolved = 1
    AND resolved_time < DATE_SUB(NOW(), INTERVAL p_days DAY);

  SELECT CONCAT('已清理 ', p_delete_count, ' 条验证日志') AS result;
END$$

DELIMITER ;

-- ============================================================
-- 完成标记
-- ============================================================

-- 执行完成
SELECT 'V20251223__create_dual_write_validation_tables.sql executed successfully' AS status;
