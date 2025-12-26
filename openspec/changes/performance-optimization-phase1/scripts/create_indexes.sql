-- ================================================================
-- IOE-DREAM 数据库索引优化脚本
-- 目标: 查询性能提升70% (800ms → 240ms)
-- 创建日期: 2025-01-XX
-- 使用方法: mysql -u root -p ioedream < create_indexes.sql
-- ================================================================

-- 设置安全模式（允许创建索引）
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

-- ================================================================
-- 1. 门禁记录表索引 (t_access_record)
-- ================================================================
SELECT '开始创建门禁记录表索引...' AS '进度';

-- 用户ID+通行时间复合索引（高频查询）
CREATE INDEX IF NOT EXISTS idx_access_user_time
ON t_access_record(user_id, pass_time DESC)
COMMENT '用户ID+通行时间复合索引';

-- 设备ID+通行时间复合索引（设备查询）
CREATE INDEX IF NOT EXISTS idx_access_device_time
ON t_access_record(device_id, pass_time DESC)
COMMENT '设备ID+通行时间复合索引';

-- 区域ID+通行时间复合索引（区域统计）
CREATE INDEX IF NOT EXISTS idx_access_area_time
ON t_access_record(area_id, pass_time DESC)
COMMENT '区域ID+通行时间复合索引';

-- 通行结果索引（统计分析）
CREATE INDEX IF NOT EXISTS idx_access_result
ON t_access_record(access_result, pass_time DESC)
COMMENT '通行结果索引';

-- 覆盖索引（避免回表）
CREATE INDEX IF NOT EXISTS idx_access_cover
ON t_access_record(user_id, device_id, area_id, access_result, pass_time)
COMMENT '门禁记录覆盖索引';

SELECT '✅ 门禁记录表索引创建完成' AS '状态';

-- ================================================================
-- 2. 考勤记录表索引 (t_attendance_record)
-- ================================================================
SELECT '开始创建考勤记录表索引...' AS '进度';

-- 用户ID+打卡时间复合索引
CREATE INDEX IF NOT EXISTS idx_attendance_user_time
ON t_attendance_record(user_id, punch_time DESC)
COMMENT '用户ID+打卡时间复合索引';

-- 考勤日期+用户ID复合索引（日期查询）
CREATE INDEX IF NOT EXISTS idx_attendance_date_user
ON t_attendance_record(attendance_date, user_id)
COMMENT '考勤日期+用户ID复合索引';

-- 打卡类型+打卡时间复合索引
CREATE INDEX IF NOT EXISTS idx_attendance_type_time
ON t_attendance_record(punch_type, punch_time DESC)
COMMENT '打卡类型+打卡时间复合索引';

-- 覆盖索引
CREATE INDEX IF NOT EXISTS idx_attendance_cover
ON t_attendance_record(user_id, punch_type, punch_time, attendance_date)
COMMENT '考勤记录覆盖索引';

SELECT '✅ 考勤记录表索引创建完成' AS '状态';

-- ================================================================
-- 3. 消费记录表索引 (t_consume_record)
-- ================================================================
SELECT '开始创建消费记录表索引...' AS '进度';

-- 用户ID+消费时间复合索引
CREATE INDEX IF NOT EXISTS idx_consume_user_time
ON t_consume_record(user_id, consume_time DESC)
COMMENT '用户ID+消费时间复合索引';

-- 设备ID+消费时间复合索引
CREATE INDEX IF NOT EXISTS idx_consume_device_time
ON t_consume_record(device_id, consume_time DESC)
COMMENT '设备ID+消费时间复合索引';

-- 账户ID索引（账户查询）
CREATE INDEX IF NOT EXISTS idx_consume_account
ON t_consume_record(account_id)
COMMENT '账户ID索引';

-- 消费类型+消费时间复合索引
CREATE INDEX IF NOT EXISTS idx_consume_type_time
ON t_consume_record(consume_type, consume_time DESC)
COMMENT '消费类型+消费时间复合索引';

-- 覆盖索引
CREATE INDEX IF NOT EXISTS idx_consume_cover
ON t_consume_record(user_id, account_id, consume_amount, consume_time)
COMMENT '消费记录覆盖索引';

-- 消费状态查询
CREATE INDEX IF NOT EXISTS idx_consume_status
ON t_consume_record(consume_status, consume_time DESC)
COMMENT '消费状态索引';

SELECT '✅ 消费记录表索引创建完成' AS '状态';

-- ================================================================
-- 4. 访客记录表索引 (t_visitor_record)
-- ================================================================
SELECT '开始创建访客记录表索引...' AS '进度';

-- 访客ID+访问时间复合索引
CREATE INDEX IF NOT EXISTS idx_visitor_time
ON t_visitor_record(visitor_id, visit_time DESC)
COMMENT '访客ID+访问时间复合索引';

-- 访问状态查询
CREATE INDEX IF NOT EXISTS idx_visitor_status
ON t_visitor_record(visit_status, visit_time DESC)
COMMENT '访问状态索引';

-- 预约ID查询（关联查询）
CREATE INDEX IF NOT EXISTS idx_visitor_appointment
ON t_visitor_record(appointment_id)
COMMENT '预约ID索引';

-- 访问时间范围查询
CREATE INDEX IF NOT EXISTS idx_visitor_visit_time_range
ON t_visitor_record(visit_time_start, visit_time_end)
COMMENT '访问时间范围索引';

SELECT '✅ 访客记录表索引创建完成' AS '状态';

-- ================================================================
-- 5. 视频设备表索引 (t_video_device)
-- ================================================================
SELECT '开始创建视频设备表索引...' AS '进度';

-- 设备状态查询
CREATE INDEX IF NOT EXISTS idx_video_device_status
ON t_video_device(device_status)
COMMENT '设备状态索引';

-- 区域ID索引（区域查询）
CREATE INDEX IF NOT EXISTS idx_video_device_area
ON t_video_device(area_id)
COMMENT '区域ID索引';

-- 设备类型筛选
CREATE INDEX IF NOT EXISTS idx_video_device_type
ON t_video_device(device_type)
COMMENT '设备类型索引';

-- 设备在线状态
CREATE INDEX IF NOT EXISTS idx_video_device_online
ON t_video_device(is_online, device_status)
COMMENT '设备在线状态索引';

SELECT '✅ 视频设备表索引创建完成' AS '状态';

-- ================================================================
-- 6. 用户表索引 (t_user)
-- ================================================================
SELECT '开始创建用户表索引...' AS '进度';

-- 登录名查询（唯一索引）
CREATE UNIQUE INDEX IF NOT EXISTS idx_user_username
ON t_user(username)
COMMENT '用户名唯一索引';

-- 手机号查询（唯一索引）
CREATE UNIQUE INDEX IF NOT EXISTS idx_user_phone
ON t_user(phone)
COMMENT '手机号唯一索引';

-- 部门用户查询
CREATE INDEX IF NOT EXISTS idx_user_dept
ON t_user(dept_id, status)
COMMENT '部门ID+状态复合索引';

-- 用户状态查询
CREATE INDEX IF NOT EXISTS idx_user_status
ON t_user(status, create_time DESC)
COMMENT '用户状态索引';

SELECT '✅ 用户表索引创建完成' AS '状态';

-- ================================================================
-- 7. 部门表索引 (t_department)
-- ================================================================
SELECT '开始创建部门表索引...' AS '进度';

-- 父部门查询
CREATE INDEX IF NOT EXISTS idx_dept_parent
ON t_department(parent_id, status)
COMMENT '父部门ID+状态复合索引';

-- 部门路径查询
CREATE INDEX IF NOT EXISTS idx_dept_path
ON t_department(dept_path)
COMMENT '部门路径索引（支持前缀查询）';

SELECT '✅ 部门表索引创建完成' AS '状态';

-- ================================================================
-- 8. 设备表索引 (t_device)
-- ================================================================
SELECT '开始创建设备表索引...' AS '进度';

-- 设备编码查询（唯一索引）
CREATE UNIQUE INDEX IF NOT EXISTS idx_device_code
ON t_device(device_code)
COMMENT '设备编码唯一索引';

-- 设备类型筛选
CREATE INDEX IF NOT EXISTS idx_device_type
ON t_device(device_type, status)
COMMENT '设备类型+状态复合索引';

-- 设备区域查询
CREATE INDEX IF NOT EXISTS idx_device_area
ON t_device(area_id)
COMMENT '区域ID索引';

SELECT '✅ 设备表索引创建完成' AS '状态';

-- ================================================================
-- 索引创建完成统计
-- ================================================================
SELECT '========================================' AS '分隔线';
SELECT '✅ 所有索引创建完成！' AS '完成状态';
SELECT '索引总数:' AS '统计';

SELECT COUNT(*) AS '索引数量'
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'ioedream'
  AND INDEX_NAME != 'PRIMARY';

-- 恢复SQL模式
SET SQL_MODE=@OLD_SQL_MODE;

-- ================================================================
-- 下一步操作
-- ================================================================
SELECT '请运行 verify_indexes.sql 脚本验证索引效果' AS '下一步';
