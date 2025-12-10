-- 访客物流管理模块数据库表
-- 生成时间: 2025-12-04
-- 用途: 支持访客车辆、司机和电子出门单管理

-- 1. 访客车辆表
CREATE TABLE IF NOT EXISTS `t_visitor_vehicle` (
  `vehicle_id` BIGINT NOT NULL COMMENT '车辆ID',
  `vehicle_number` VARCHAR(20) NOT NULL COMMENT '车牌号',
  `vehicle_type` TINYINT NOT NULL DEFAULT 1 COMMENT '车辆类型（1-小型车 2-中型车 3-大型车）',
  `vehicle_color` VARCHAR(20) COMMENT '车辆颜色',
  `vehicle_brand` VARCHAR(50) COMMENT '车辆品牌',
  `vehicle_model` VARCHAR(50) COMMENT '车辆型号',
  `company_name` VARCHAR(100) COMMENT '所属公司',
  `driver_id` BIGINT COMMENT '司机ID',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（1-正常 0-禁用）',
  `remark` VARCHAR(500) COMMENT '备注',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  `create_user_id` BIGINT COMMENT '创建人ID',
  `update_time` DATETIME NOT NULL COMMENT '更新时间',
  `update_user_id` BIGINT COMMENT '更新人ID',
  `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记（0-未删除 1-已删除）',
  PRIMARY KEY (`vehicle_id`),
  UNIQUE KEY `uk_vehicle_number` (`vehicle_number`, `deleted_flag`),
  KEY `idx_driver_id` (`driver_id`),
  KEY `idx_company` (`company_name`(50)),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='访客车辆表';

-- 2. 访客司机表
CREATE TABLE IF NOT EXISTS `t_visitor_driver` (
  `driver_id` BIGINT NOT NULL COMMENT '司机ID',
  `driver_name` VARCHAR(50) NOT NULL COMMENT '司机姓名',
  `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
  `id_card` VARCHAR(18) COMMENT '身份证号',
  `license_type` VARCHAR(10) NOT NULL COMMENT '驾照类型（A1/A2/B1/B2/C1等）',
  `license_number` VARCHAR(30) NOT NULL COMMENT '驾照号',
  `license_expiry_date` DATE COMMENT '驾照有效期',
  `company_name` VARCHAR(100) COMMENT '所属公司',
  `photo_url` VARCHAR(200) COMMENT '司机照片URL',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（1-正常 0-禁用）',
  `remark` VARCHAR(500) COMMENT '备注',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  `create_user_id` BIGINT COMMENT '创建人ID',
  `update_time` DATETIME NOT NULL COMMENT '更新时间',
  `update_user_id` BIGINT COMMENT '更新人ID',
  `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记（0-未删除 1-已删除）',
  PRIMARY KEY (`driver_id`),
  UNIQUE KEY `uk_license_number` (`license_number`, `deleted_flag`),
  KEY `idx_phone` (`phone`),
  KEY `idx_id_card` (`id_card`),
  KEY `idx_company` (`company_name`(50)),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='访客司机表';

-- 3. 电子出门单表
CREATE TABLE IF NOT EXISTS `t_visitor_electronic_pass` (
  `pass_id` BIGINT NOT NULL COMMENT '出门单ID',
  `pass_number` VARCHAR(50) NOT NULL COMMENT '出门单编号',
  `vehicle_id` BIGINT NOT NULL COMMENT '车辆ID',
  `driver_id` BIGINT NOT NULL COMMENT '司机ID',
  `goods_list` TEXT COMMENT '物品清单（JSON格式）',
  `total_value` DECIMAL(10,2) COMMENT '物品总价值',
  `departure_place` VARCHAR(100) COMMENT '出发地',
  `destination_place` VARCHAR(100) COMMENT '目的地',
  `scheduled_departure_time` DATETIME NOT NULL COMMENT '预计出发时间',
  `actual_departure_time` DATETIME COMMENT '实际出发时间',
  `scheduled_return_time` DATETIME COMMENT '预计返回时间',
  `actual_return_time` DATETIME COMMENT '实际返回时间',
  `applicant_id` BIGINT NOT NULL COMMENT '申请人ID',
  `applicant_name` VARCHAR(50) NOT NULL COMMENT '申请人姓名',
  `approver_id` BIGINT COMMENT '审批人ID',
  `approver_name` VARCHAR(50) COMMENT '审批人姓名',
  `approval_status` TINYINT NOT NULL DEFAULT 0 COMMENT '审批状态（0-待审批 1-已同意 2-已拒绝）',
  `approval_time` DATETIME COMMENT '审批时间',
  `approval_comment` VARCHAR(500) COMMENT '审批意见',
  `pass_status` TINYINT NOT NULL DEFAULT 1 COMMENT '出门单状态（1-已创建 2-已审批 3-已出门 4-已返回 5-已取消）',
  `remark` VARCHAR(500) COMMENT '备注',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  `create_user_id` BIGINT COMMENT '创建人ID',
  `update_time` DATETIME NOT NULL COMMENT '更新时间',
  `update_user_id` BIGINT COMMENT '更新人ID',
  `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记（0-未删除 1-已删除）',
  PRIMARY KEY (`pass_id`),
  UNIQUE KEY `uk_pass_number` (`pass_number`, `deleted_flag`),
  KEY `idx_vehicle_id` (`vehicle_id`),
  KEY `idx_driver_id` (`driver_id`),
  KEY `idx_applicant_id` (`applicant_id`),
  KEY `idx_approval_status` (`approval_status`),
  KEY `idx_pass_status` (`pass_status`),
  KEY `idx_departure_time` (`scheduled_departure_time`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='电子出门单表';

