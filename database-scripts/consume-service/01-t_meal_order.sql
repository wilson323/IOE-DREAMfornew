-- ============================================================
-- 订餐订单表
-- 模块: consume-service
-- 作者: IOE-DREAM Team
-- 创建日期: 2025-12-14
-- ============================================================

-- 订餐订单主表
CREATE TABLE IF NOT EXISTS `t_meal_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `order_no` VARCHAR(32) NOT NULL COMMENT '订单编号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `account_id` BIGINT NOT NULL COMMENT '账户ID',
    `area_id` BIGINT NOT NULL COMMENT '区域ID（食堂）',
    `area_name` VARCHAR(100) DEFAULT NULL COMMENT '区域名称',
    `meal_type_id` BIGINT NOT NULL COMMENT '餐别ID',
    `meal_type_name` VARCHAR(50) DEFAULT NULL COMMENT '餐别名称',
    `order_date` DATETIME NOT NULL COMMENT '预约日期',
    `pickup_start_time` DATETIME DEFAULT NULL COMMENT '取餐开始时间',
    `pickup_end_time` DATETIME DEFAULT NULL COMMENT '取餐结束时间',
    `amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '订单金额',
    `actual_amount` DECIMAL(10,2) DEFAULT NULL COMMENT '实付金额',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '订单状态：PENDING-待取餐, COMPLETED-已完成, CANCELLED-已取消, EXPIRED-已过期',
    `pickup_method` VARCHAR(20) DEFAULT NULL COMMENT '取餐方式：QR_CODE-二维码, FACE-刷脸, CARD-刷卡',
    `pickup_time` DATETIME DEFAULT NULL COMMENT '取餐时间',
    `pickup_device_id` BIGINT DEFAULT NULL COMMENT '取餐设备ID',
    `cancel_reason` VARCHAR(200) DEFAULT NULL COMMENT '取消原因',
    `cancel_time` DATETIME DEFAULT NULL COMMENT '取消时间',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_account_id` (`account_id`),
    KEY `idx_area_id` (`area_id`),
    KEY `idx_order_date` (`order_date`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订餐订单表';

-- 订餐订单明细表
CREATE TABLE IF NOT EXISTS `t_meal_order_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '明细ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `dish_id` BIGINT NOT NULL COMMENT '菜品ID',
    `dish_name` VARCHAR(100) NOT NULL COMMENT '菜品名称',
    `price` DECIMAL(10,2) NOT NULL COMMENT '菜品单价',
    `quantity` INT NOT NULL DEFAULT 1 COMMENT '数量',
    `subtotal` DECIMAL(10,2) NOT NULL COMMENT '小计金额',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_dish_id` (`dish_id`),
    CONSTRAINT `fk_meal_order_item_order` FOREIGN KEY (`order_id`) REFERENCES `t_meal_order` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订餐订单明细表';

-- 添加索引优化查询性能
CREATE INDEX idx_meal_order_composite ON t_meal_order(user_id, status, order_date);
CREATE INDEX idx_meal_order_area_meal ON t_meal_order(area_id, meal_type_id, order_date);
