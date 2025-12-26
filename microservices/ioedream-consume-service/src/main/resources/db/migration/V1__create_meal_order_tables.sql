-- ============================================================
-- 订餐管理模块数据库表设计
-- 创建时间: 2025-12-26
-- 说明: 支持企业食堂订餐、菜单管理、订单管理
-- ============================================================

-- 1. 菜品分类表
CREATE TABLE t_meal_category (
    category_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    category_name VARCHAR(50) NOT NULL COMMENT '分类名称',
    category_code VARCHAR(20) NOT NULL COMMENT '分类编码',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    status TINYINT DEFAULT 1 COMMENT '状态（1-启用 0-禁用）',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记（0-未删除 1-已删除）',
    UNIQUE KEY uk_category_code (category_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品分类表';

-- 2. 菜品表
CREATE TABLE t_meal_menu (
    menu_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '菜品ID',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    menu_name VARCHAR(100) NOT NULL COMMENT '菜品名称',
    menu_code VARCHAR(50) NOT NULL COMMENT '菜品编码',
    menu_image VARCHAR(500) COMMENT '菜品图片URL',
    price DECIMAL(10,2) NOT NULL COMMENT '价格（元）',
    original_price DECIMAL(10,2) COMMENT '原价（元）',
    unit VARCHAR(20) COMMENT '单位（份/个/碗）',
    description VARCHAR(500) COMMENT '菜品描述',
    ingredients TEXT COMMENT '食材清单（JSON格式）',
    nutrition_info TEXT COMMENT '营养信息（JSON格式）',
    spicy_level TINYINT DEFAULT 0 COMMENT '辣度（0-不辣 1-微辣 2-中辣 3-重辣）',
    available_days VARCHAR(50) COMMENT '供应日期（1,2,3,4,5代表周一到周五）',
    available_start_time TIME COMMENT '供应开始时间',
    available_end_time TIME COMMENT '供应结束时间',
    max_daily_quantity INT DEFAULT 999 COMMENT '每日最大供应数量',
    current_quantity INT DEFAULT 0 COMMENT '当前剩余数量',
    status TINYINT DEFAULT 1 COMMENT '状态（1-上架 0-下架）',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记（0-未删除 1-已删除）',
    UNIQUE KEY uk_menu_code (menu_code),
    KEY idx_category_id (category_id),
    KEY idx_status (status),
    KEY idx_available_days (available_days)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品表';

-- 3. 订单表
CREATE TABLE t_meal_order (
    order_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
    order_no VARCHAR(50) NOT NULL COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    user_name VARCHAR(50) COMMENT '用户姓名',
    user_phone VARCHAR(20) COMMENT '用户手机号',
    order_date DATE NOT NULL COMMENT '订餐日期',
    meal_type TINYINT NOT NULL COMMENT '餐别（1-早餐 2-午餐 3-晚餐）',
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '订单总额（元）',
    discount_amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '优惠金额（元）',
    actual_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '实付金额（元）',
    subsidy_amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '补贴金额（元）',
    order_status TINYINT DEFAULT 1 COMMENT '订单状态（1-待支付 2-已支付 3-已完成 4-已取消 5-已退款）',
    payment_status TINYINT DEFAULT 0 COMMENT '支付状态（0-未支付 1-已支付 2-支付失败）',
    payment_time DATETIME COMMENT '支付时间',
    payment_method VARCHAR(20) COMMENT '支付方式（balance-余额 wechat-微信 alipay-支付宝）',
    pickup_time TIME COMMENT '取餐时间',
    pickup_location VARCHAR(100) COMMENT '取餐地点',
    special_requirements VARCHAR(500) COMMENT '特殊要求',
    cancel_reason VARCHAR(500) COMMENT '取消原因',
    cancel_time DATETIME COMMENT '取消时间',
    refund_amount DECIMAL(10,2) COMMENT '退款金额',
    refund_time DATETIME COMMENT '退款时间',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记（0-未删除 1-已删除）',
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_user_id (user_id),
    KEY idx_order_date (order_date),
    KEY idx_order_status (order_status),
    KEY idx_meal_type (meal_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订餐订单表';

-- 4. 订单明细表
CREATE TABLE t_meal_order_item (
    item_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '明细ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    menu_id BIGINT NOT NULL COMMENT '菜品ID',
    menu_name VARCHAR(100) NOT NULL COMMENT '菜品名称',
    menu_code VARCHAR(50) COMMENT '菜品编码',
    menu_image VARCHAR(500) COMMENT '菜品图片URL',
    unit_price DECIMAL(10,2) NOT NULL COMMENT '单价（元）',
    quantity INT NOT NULL DEFAULT 1 COMMENT '数量',
    subtotal DECIMAL(10,2) NOT NULL COMMENT '小计（元）',
    special_requirements VARCHAR(500) COMMENT '特殊要求',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_order_id (order_id),
    KEY idx_menu_id (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订餐订单明细表';

-- 5. 菜品库存表
CREATE TABLE t_meal_inventory (
    inventory_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '库存ID',
    menu_id BIGINT NOT NULL COMMENT '菜品ID',
    inventory_date DATE NOT NULL COMMENT '库存日期',
    meal_type TINYINT NOT NULL COMMENT '餐别（1-早餐 2-午餐 3-晚餐）',
    initial_quantity INT DEFAULT 0 COMMENT '初始数量',
    sold_quantity INT DEFAULT 0 COMMENT '已售数量',
    remaining_quantity INT DEFAULT 0 COMMENT '剩余数量',
    status TINYINT DEFAULT 1 COMMENT '状态（1-有效 0-无效）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_menu_date_type (menu_id, inventory_date, meal_type),
    KEY idx_inventory_date (inventory_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品库存表';

-- 6. 订餐配置表
CREATE TABLE t_meal_order_config (
    config_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    config_key VARCHAR(50) NOT NULL COMMENT '配置键',
    config_value VARCHAR(500) COMMENT '配置值',
    config_desc VARCHAR(200) COMMENT '配置描述',
    status TINYINT DEFAULT 1 COMMENT '状态（1-启用 0-禁用）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订餐配置表';

-- 初始化订餐配置数据
INSERT INTO t_meal_order_config (config_key, config_value, config_desc) VALUES
('order.advance.minutes', '60', '提前订餐分钟数'),
('order.cancel.minutes', '30', '订单取消截止时间（分钟）'),
('order.max.daily', '10', '每日最大订餐数量'),
('payment.enable.balance', '1', '是否启用余额支付'),
('payment.enable.wechat', '0', '是否启用微信支付'),
('payment.enable.alipay', '0', '是否启用支付宝支付'),
('subsidy.enable', '1', '是否启用补贴'),
('subsidy.breakfast.amount', '5.00', '早餐补贴金额'),
('subsidy.lunch.amount', '15.00', '午餐补贴金额'),
('subsidy.dinner.amount', '10.00', '晚餐补贴金额');

-- 初始化菜品分类数据
INSERT INTO t_meal_category (category_name, category_code, sort_order, status) VALUES
('主食类', 'STAPLE', 1, 1),
('肉类', 'MEAT', 2, 1),
('蔬菜类', 'VEGETABLE', 3, 1),
('汤品类', 'SOUP', 4, 1),
('饮品类', 'DRINK', 5, 1),
('水果类', 'FRUIT', 6, 1);

-- 创建索引优化
CREATE INDEX idx_menu_date_type ON t_meal_menu (available_days, available_start_time, available_end_time);
CREATE INDEX idx_order_user_date ON t_meal_order (user_id, order_date);
CREATE INDEX idx_order_status_date ON t_meal_order (order_status, order_date);
