-- ============================================================
-- IOE-DREAM 数据库性能优化索引创建脚本
-- 创建日期: $(date)
-- 优化目标: 解决65%查询缺少索引问题
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 核心表索引创建（P1优先级）
-- ============================================================

-- 1. 用户管理相关索引
-- t_common_user
CREATE INDEX IF NOT EXISTS idx_user_status_deleted ON t_common_user(status, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_user_dept_status ON t_common_user(dept_id, status, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_user_create_time ON t_common_user(create_time DESC, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_user_phone ON t_common_user(phone, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_user_email ON t_common_user(email, deleted_flag);

-- t_common_user_session
CREATE INDEX IF NOT EXISTS idx_session_user_token ON t_common_user_session(user_id, token, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_session_expire_time ON t_common_user_session(expire_time, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_session_create_time ON t_common_user_session(create_time, deleted_flag);

-- 2. 设备管理相关索引
-- t_common_device
CREATE INDEX IF NOT EXISTS idx_device_type_status ON t_common_device(device_type, status, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_device_area_status ON t_common_device(area_id, status, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_device_create_time ON t_common_device(create_time DESC, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_device_code ON t_common_device(device_code, deleted_flag);

-- 3. 消费管理相关索引
-- t_consume_account
CREATE INDEX IF NOT EXISTS idx_account_user_status ON t_consume_account(user_id, status, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_account_balance ON t_consume_account(balance DESC, status);
CREATE INDEX IF NOT EXISTS idx_account_type ON t_consume_account(account_type, status, deleted_flag);

-- t_consume_transaction
CREATE INDEX IF NOT EXISTS idx_transaction_user_time ON t_consume_transaction(user_id, create_time DESC, status);
CREATE INDEX IF NOT EXISTS idx_transaction_device_time ON t_consume_transaction(device_id, create_time DESC, status);
CREATE INDEX IF NOT EXISTS idx_transaction_amount_time ON t_consume_transaction(amount, create_time DESC);
CREATE INDEX IF NOT EXISTS idx_transaction_type ON t_consume_transaction(transaction_type, status, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_transaction_order_no ON t_consume_transaction(order_no, deleted_flag);

-- 4. 门禁管理相关索引
-- t_access_record
CREATE INDEX IF NOT EXISTS idx_access_user_time ON t_access_record(user_id, access_time DESC, access_type, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_access_device_time ON t_access_record(device_id, access_time DESC, access_type, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_access_area_time ON t_access_record(area_id, access_time DESC, access_type, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_access_result ON t_access_record(access_result, deleted_flag);

-- 5. 考勤管理相关索引
-- t_attendance_record
CREATE INDEX IF NOT EXISTS idx_attendance_user_time ON t_attendance_record(user_id, clock_time DESC, record_type, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_attendance_date_type ON t_attendance_record(date, record_type, status, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_attendance_device_time ON t_attendance_record(device_id, clock_time DESC, deleted_flag);

-- 6. 访客管理相关索引
-- t_visitor_record
CREATE INDEX IF NOT EXISTS idx_visitor_user_time ON t_visitor_record(visitor_id, visit_time DESC, status, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_visitor_phone ON t_visitor_record(phone_number, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_visitor_appointment ON t_visitor_record(appointment_id, deleted_flag);

-- ============================================================
-- 业务表索引（P2优先级）
-- ============================================================

-- 主题配置相关索引
CREATE INDEX IF NOT EXISTS idx_theme_config_user ON t_user_theme_config(user_id, device_type, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_theme_config_default ON t_user_theme_config(user_id, is_default, status, deleted_flag);

-- 通知相关索引
CREATE INDEX IF NOT EXISTS idx_notification_user ON t_notification(user_id, notification_type, read_status, deleted_flag);
CREATE INDEX IF NOT EXISTS idx_notification_create_time ON t_notification(create_time DESC, deleted_flag);

-- 审计日志相关索引
CREATE INDEX IF NOT EXISTS idx_audit_user_time ON t_audit_log(user_id, create_time DESC, operation_type);
CREATE INDEX IF NOT EXISTS idx_audit_module ON t_audit_log(module_name, operation_type, create_time);

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 索引创建统计
-- ============================================================
SELECT COUNT(*) as created_indexes
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'ioedream'
AND INDEX_NAME LIKE 'idx_%';
