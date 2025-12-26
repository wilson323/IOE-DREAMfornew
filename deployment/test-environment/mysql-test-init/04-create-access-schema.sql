-- ============================================================
-- IOE-DREAM 测试数据库初始化脚本
-- 步骤4: 创建门禁模块表结构
-- ============================================================

USE ioedream_test;

-- ============================================================
-- 门禁设备表（继承公共设备表，扩展门禁特定字段）
-- ============================================================
CREATE TABLE IF NOT EXISTS t_access_device (
  device_id VARCHAR(50) PRIMARY KEY COMMENT '设备ID',
  device_code VARCHAR(50) NOT NULL UNIQUE COMMENT '设备编码',
  device_name VARCHAR(100) NOT NULL COMMENT '设备名称',
  device_sub_type TINYINT COMMENT '设备子类型 11-门禁控制器 12-人脸识别机 13-指纹识别机',
  area_id BIGINT COMMENT '所属区域ID',
  ip_address VARCHAR(50) COMMENT 'IP地址',
  port INT COMMENT '端口',
  access_mode TINYINT DEFAULT 1 COMMENT '验证模式 1-边缘验证 2-后台验证 3-混合验证',
  anti_passback_enabled TINYINT DEFAULT 1 COMMENT '是否启用反潜回',
  open_duration INT DEFAULT 3000 COMMENT '开门时长（毫秒）',
  status TINYINT DEFAULT 1 COMMENT '状态 0-离线 1-在线 2-故障',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
  INDEX idx_device_code (device_code),
  INDEX idx_device_sub_type (device_sub_type),
  INDEX idx_area_id (area_id),
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门禁设备表';

-- ============================================================
-- 通行记录表
-- ============================================================
CREATE TABLE IF NOT EXISTS t_access_record (
  record_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
  user_id BIGINT COMMENT '用户ID',
  username VARCHAR(50) COMMENT '用户名',
  device_id VARCHAR(50) NOT NULL COMMENT '设备ID',
  device_name VARCHAR(100) COMMENT '设备名称',
  area_id BIGINT COMMENT '区域ID',
  area_name VARCHAR(100) COMMENT '区域名称',
  access_type VARCHAR(20) NOT NULL COMMENT '通行方式 QR_CODE/BIOMETRIC/CARD',
  verify_result TINYINT COMMENT '验证结果 0-失败 1-成功',
  fail_reason VARCHAR(200) COMMENT '失败原因',
  access_time DATETIME NOT NULL COMMENT '通行时间',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
  INDEX idx_user_id (user_id),
  INDEX idx_device_id (device_id),
  INDEX idx_area_id (area_id),
  INDEX idx_access_time (access_time),
  INDEX idx_verify_result (verify_result)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通行记录表';

-- ============================================================
-- 二维码会话表
-- ============================================================
CREATE TABLE IF NOT EXISTS t_access_qrcode_session (
  session_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '会话ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  device_id VARCHAR(50) COMMENT '设备ID',
  area_id BIGINT COMMENT '区域ID',
  qr_code VARCHAR(200) NOT NULL UNIQUE COMMENT '二维码内容',
  generate_time DATETIME NOT NULL COMMENT '生成时间',
  expire_time DATETIME NOT NULL COMMENT '过期时间',
  verified TINYINT DEFAULT 0 COMMENT '是否已验证 0-未验证 1-已验证',
  verify_time DATETIME COMMENT '验证时间',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_user_id (user_id),
  INDEX idx_qr_code (qr_code),
  INDEX idx_expire_time (expire_time),
  INDEX idx_verified (verified)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='二维码会话表';

-- ============================================================
-- 移动端认证表
-- ============================================================
CREATE TABLE IF NOT EXISTS t_access_mobile_auth (
  auth_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '认证ID',
  user_id BIGINT COMMENT '用户ID',
  device_id VARCHAR(50) NOT NULL COMMENT '设备ID',
  device_type VARCHAR(20) COMMENT '设备类型 ANDROID/IOS',
  access_token VARCHAR(200) NOT NULL COMMENT '访问令牌',
  refresh_token VARCHAR(200) NOT NULL COMMENT '刷新令牌',
  access_expire_time DATETIME NOT NULL COMMENT '访问令牌过期时间',
  refresh_expire_time DATETIME NOT NULL COMMENT '刷新令牌过期时间',
  status TINYINT DEFAULT 1 COMMENT '状态 0-失效 1-有效',
  last_heartbeat_time DATETIME COMMENT '最后心跳时间',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_user_id (user_id),
  INDEX idx_device_id (device_id),
  INDEX idx_access_token (access_token),
  INDEX idx_refresh_token (refresh_token),
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='移动端认证表';

SELECT 'Access schema created successfully' AS status;
