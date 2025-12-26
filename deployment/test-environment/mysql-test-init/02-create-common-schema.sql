-- ============================================================
-- IOE-DREAM 测试数据库初始化脚本
-- 步骤2: 创建公共表结构
-- ============================================================

USE ioedream_test;

-- ============================================================
-- 用户表
-- ============================================================
CREATE TABLE IF NOT EXISTS t_common_user (
  user_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
  username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
  password VARCHAR(100) NOT NULL COMMENT '密码（加密）',
  phone VARCHAR(20) UNIQUE COMMENT '手机号',
  email VARCHAR(100) COMMENT '邮箱',
  real_name VARCHAR(50) COMMENT '真实姓名',
  gender TINYINT DEFAULT 0 COMMENT '性别 0-未知 1-男 2-女',
  dept_id BIGINT COMMENT '部门ID',
  status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
  INDEX idx_username (username),
  INDEX idx_phone (phone),
  INDEX idx_dept_id (dept_id),
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================================
-- 部门表
-- ============================================================
CREATE TABLE IF NOT EXISTS t_common_department (
  department_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '部门ID',
  department_name VARCHAR(100) NOT NULL COMMENT '部门名称',
  parent_id BIGINT DEFAULT 0 COMMENT '父部门ID',
  department_level INT DEFAULT 1 COMMENT '部门层级',
  sort_order INT DEFAULT 0 COMMENT '排序',
  status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
  INDEX idx_parent_id (parent_id),
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门表';

-- ============================================================
-- 设备表
-- ============================================================
CREATE TABLE IF NOT EXISTS t_common_device (
  device_id VARCHAR(50) PRIMARY KEY COMMENT '设备ID',
  device_code VARCHAR(50) NOT NULL UNIQUE COMMENT '设备编码',
  device_name VARCHAR(100) NOT NULL COMMENT '设备名称',
  device_type TINYINT NOT NULL COMMENT '设备类型 1-门禁 2-考勤 3-消费 4-视频 5-访客',
  device_sub_type TINYINT COMMENT '设备子类型',
  area_id BIGINT COMMENT '所属区域ID',
  ip_address VARCHAR(50) COMMENT 'IP地址',
  port INT COMMENT '端口',
  status TINYINT DEFAULT 1 COMMENT '状态 0-离线 1-在线 2-故障',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
  INDEX idx_device_code (device_code),
  INDEX idx_device_type (device_type),
  INDEX idx_area_id (area_id),
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备表';

-- ============================================================
-- 区域表
-- ============================================================
CREATE TABLE IF NOT EXISTS t_common_area (
  area_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '区域ID',
  area_name VARCHAR(100) NOT NULL COMMENT '区域名称',
  area_type TINYINT COMMENT '区域类型 1-大楼 2-楼层 3-房间 4-其他',
  parent_id BIGINT DEFAULT 0 COMMENT '父区域ID',
  area_level INT DEFAULT 1 COMMENT '区域层级',
  sort_order INT DEFAULT 0 COMMENT '排序',
  status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
  INDEX idx_parent_id (parent_id),
  INDEX idx_area_type (area_type),
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='区域表';

-- ============================================================
-- 区域设备关联表
-- ============================================================
CREATE TABLE IF NOT EXISTS t_area_device_relation (
  relation_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
  area_id BIGINT NOT NULL COMMENT '区域ID',
  device_id VARCHAR(50) NOT NULL COMMENT '设备ID',
  device_code VARCHAR(50) COMMENT '设备编码',
  device_name VARCHAR(100) COMMENT '设备名称',
  device_type TINYINT COMMENT '设备类型',
  device_sub_type TINYINT COMMENT '设备子类型',
  business_module VARCHAR(50) COMMENT '业务模块',
  relation_status TINYINT DEFAULT 1 COMMENT '关联状态 1-正常 2-维护 3-故障',
  priority TINYINT DEFAULT 2 COMMENT '优先级 1-主设备 2-辅助设备 3-备用设备',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_area_id (area_id),
  INDEX idx_device_id (device_id),
  UNIQUE KEY uk_area_device (area_id, device_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='区域设备关联表';

SELECT 'Common schema created successfully' AS status;
