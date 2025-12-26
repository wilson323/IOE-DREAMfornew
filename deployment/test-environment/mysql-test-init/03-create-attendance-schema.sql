-- ============================================================
-- IOE-DREAM 测试数据库初始化脚本
-- 步骤3: 创建考勤模块表结构
-- ============================================================

USE ioedream_test;

-- ============================================================
-- 班次表
-- ============================================================
CREATE TABLE IF NOT EXISTS t_attendance_work_shift (
  shift_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '班次ID',
  shift_name VARCHAR(100) NOT NULL COMMENT '班次名称',
  shift_type TINYINT NOT NULL COMMENT '班次类型 1-标准工时 2-弹性工时 3-轮班制',
  work_start_time TIME COMMENT '上班时间',
  work_end_time TIME COMMENT '下班时间',
  work_duration INT DEFAULT 480 COMMENT '工作时长（分钟）',
  late_tolerance INT DEFAULT 0 COMMENT '迟到宽限（分钟）',
  early_tolerance INT DEFAULT 0 COMMENT '早退宽限（分钟）',
  min_overtime_duration INT DEFAULT 60 COMMENT '最小加班时长（分钟）',
  flex_start_earliest TIME COMMENT '弹性上班最早时间',
  flex_start_latest TIME COMMENT '弹性上班最晚时间',
  flex_end_earliest TIME COMMENT '弹性下班最早时间',
  flex_end_latest TIME COMMENT '弹性下班最晚时间',
  status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
  INDEX idx_shift_type (shift_type),
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='班次表';

-- ============================================================
-- 考勤记录表
-- ============================================================
CREATE TABLE IF NOT EXISTS t_attendance_record (
  record_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  shift_id BIGINT COMMENT '班次ID',
  attendance_date DATE NOT NULL COMMENT '考勤日期',
  punch_time DATETIME NOT NULL COMMENT '打卡时间',
  attendance_type VARCHAR(20) NOT NULL COMMENT '打卡类型 CHECK_IN/CHECK_OUT',
  device_id VARCHAR(50) COMMENT '设备ID',
  area_id BIGINT COMMENT '区域ID',
  status VARCHAR(20) COMMENT '考勤状态 NORMAL/LATE/EARLY_LEAVE/ABSENT',
  late_duration INT DEFAULT 0 COMMENT '迟到时长（分钟）',
  early_duration INT DEFAULT 0 COMMENT '早退时长（分钟）',
  working_minutes INT DEFAULT 0 COMMENT '工作时长（分钟）',
  overtime_duration INT DEFAULT 0 COMMENT '加班时长（分钟）',
  remark VARCHAR(500) COMMENT '备注',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
  INDEX idx_user_id (user_id),
  INDEX idx_shift_id (shift_id),
  INDEX idx_attendance_date (attendance_date),
  INDEX idx_punch_time (punch_time),
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤记录表';

SELECT 'Attendance schema created successfully' AS status;
