-- 考勤模块存储过程优化
-- 用于提高数据处理和维护的效率

-- ========================================
-- 1. 考勤统计计算存储过程
-- ========================================
DELIMITER //

CREATE PROCEDURE sp_calculate_attendance_statistics(
    IN p_employee_id BIGINT,
    IN p_statistics_type VARCHAR(20),
    IN p_period_value VARCHAR(20)
)
BEGIN
    DECLARE v_total_days INT DEFAULT 0;
    DECLARE v_present_days INT DEFAULT 0;
    DECLARE v_absent_days INT DEFAULT 0;
    DECLARE v_late_days INT DEFAULT 0;
    DECLARE v_early_leave_days INT DEFAULT 0;
    DECLARE v_leave_days INT DEFAULT 0;
    DECLARE v_overtime_hours DECIMAL(8,2) DEFAULT 0.00;
    DECLARE v_work_hours DECIMAL(8,2) DEFAULT 0.00;
    DECLARE v_attendance_rate DECIMAL(5,2) DEFAULT 0.00;
    DECLARE v_statistics_date DATE;
    DECLARE v_exists INT DEFAULT 0;

    -- 设置统计日期
    SET v_statistics_date = CURDATE();

    -- 根据统计类型计算数据
    IF p_statistics_type = 'DAILY' THEN
        -- 日统计
        SELECT
            COUNT(*) as total,
            COUNT(CASE WHEN attendance_status = 'NORMAL' THEN 1 END) as present,
            COUNT(CASE WHEN attendance_status = 'ABSENCE' THEN 1 END) as absent,
            COUNT(CASE WHEN attendance_status = 'LATE' THEN 1 END) as late,
            COUNT(CASE WHEN attendance_status = 'EARLY_LEAVE' THEN 1 END) as early_leave,
            COUNT(CASE WHEN attendance_status = 'LEAVE' THEN 1 END) as leave_days,
            COALESCE(SUM(overtime_hours), 0.00) as overtime,
            COALESCE(SUM(work_hours), 0.00) as work_hours
        INTO
            v_total_days, v_present_days, v_absent_days, v_late_days,
            v_early_leave_days, v_leave_days, v_overtime_hours, v_work_hours
        FROM t_attendance_record
        WHERE employee_id = p_employee_id
          AND DATE_FORMAT(attendance_date, '%Y-%m-%d') = p_period_value
          AND deleted_flag = 0;

    ELSEIF p_statistics_type = 'MONTHLY' THEN
        -- 月统计
        SELECT
            COUNT(*) as total,
            COUNT(CASE WHEN attendance_status = 'NORMAL' THEN 1 END) as present,
            COUNT(CASE WHEN attendance_status = 'ABSENCE' THEN 1 END) as absent,
            COUNT(CASE WHEN attendance_status = 'LATE' THEN 1 END) as late,
            COUNT(CASE WHEN attendance_status = 'EARLY_LEAVE' THEN 1 END) as early_leave,
            COUNT(CASE WHEN attendance_status = 'LEAVE' THEN 1 END) as leave_days,
            COALESCE(SUM(overtime_hours), 0.00) as overtime,
            COALESCE(SUM(work_hours), 0.00) as work_hours
        INTO
            v_total_days, v_present_days, v_absent_days, v_late_days,
            v_early_leave_days, v_leave_days, v_overtime_hours, v_work_hours
        FROM t_attendance_record
        WHERE employee_id = p_employee_id
          AND DATE_FORMAT(attendance_date, '%Y-%m') = p_period_value
          AND deleted_flag = 0;

    ELSEIF p_statistics_type = 'YEARLY' THEN
        -- 年统计
        SELECT
            COUNT(*) as total,
            COUNT(CASE WHEN attendance_status = 'NORMAL' THEN 1 END) as present,
            COUNT(CASE WHEN attendance_status = 'ABSENCE' THEN 1 END) as absent,
            COUNT(CASE WHEN attendance_status = 'LATE' THEN 1 END) as late,
            COUNT(CASE WHEN attendance_status = 'EARLY_LEAVE' THEN 1 END) as early_leave,
            COUNT(CASE WHEN attendance_status = 'LEAVE' THEN 1 END) as leave_days,
            COALESCE(SUM(overtime_hours), 0.00) as overtime,
            COALESCE(SUM(work_hours), 0.00) as work_hours
        INTO
            v_total_days, v_present_days, v_absent_days, v_late_days,
            v_early_leave_days, v_leave_days, v_overtime_hours, v_work_hours
        FROM t_attendance_record
        WHERE employee_id = p_employee_id
          AND DATE_FORMAT(attendance_date, '%Y') = p_period_value
          AND deleted_flag = 0;
    END IF;

    -- 计算出勤率
    IF v_total_days > 0 THEN
        SET v_attendance_rate = ROUND((v_present_days * 100.0) / v_total_days, 2);
    END IF;

    -- 检查是否已存在记录
    SELECT COUNT(*) INTO v_exists
    FROM t_attendance_statistics
    WHERE employee_id = p_employee_id
      AND statistics_type = p_statistics_type
      AND period_value = p_period_value
      AND deleted_flag = 0;

    -- 插入或更新统计记录
    IF v_exists > 0 THEN
        UPDATE t_attendance_statistics
        SET total_days = v_total_days,
            present_days = v_present_days,
            absent_days = v_absent_days,
            late_days = v_late_days,
            early_leave_days = v_early_leave_days,
            leave_days = v_leave_days,
            overtime_hours = v_overtime_hours,
            work_hours = v_work_hours,
            attendance_rate = v_attendance_rate,
            statistics_date = v_statistics_date,
            update_time = NOW(),
            version = version + 1
        WHERE employee_id = p_employee_id
          AND statistics_type = p_statistics_type
          AND period_value = p_period_value
          AND deleted_flag = 0;
    ELSE
        INSERT INTO t_attendance_statistics (
            employee_id, statistics_type, statistics_period, period_value,
            total_days, present_days, absent_days, late_days, early_leave_days,
            leave_days, overtime_hours, work_hours, attendance_rate, statistics_date,
            create_time, update_time, create_user_id, update_user_id
        ) VALUES (
            p_employee_id, p_statistics_type,
            CASE
                WHEN p_statistics_type = 'DAILY' THEN 'DAY'
                WHEN p_statistics_type = 'MONTHLY' THEN 'MONTH'
                WHEN p_statistics_type = 'YEARLY' THEN 'YEAR'
            END,
            p_period_value,
            v_total_days, v_present_days, v_absent_days, v_late_days, v_early_leave_days,
            v_leave_days, v_overtime_hours, v_work_hours, v_attendance_rate, v_statistics_date,
            NOW(), NOW(), 1, 1
        );
    END IF;

    -- 返回结果
    SELECT 'SUCCESS' as result,
           CONCAT('考勤统计计算完成: ', p_employee_id, '-', p_statistics_type, '-', p_period_value) as message;
END //

-- ========================================
-- 2. 批量考勤统计计算存储过程
-- ========================================
CREATE PROCEDURE sp_batch_calculate_attendance_statistics(
    IN p_statistics_type VARCHAR(20),
    IN p_period_value VARCHAR(20),
    IN p_batch_size INT DEFAULT 100
)
BEGIN
    DECLARE v_employee_id BIGINT;
    DECLARE v_done INT DEFAULT FALSE;
    DECLARE v_processed_count INT DEFAULT 0;
    DECLARE v_error_count INT DEFAULT 0;

    -- 声明游标
    DECLARE employee_cursor CURSOR FOR
        SELECT DISTINCT employee_id
        FROM t_attendance_record
        WHERE deleted_flag = 0
        ORDER BY employee_id;

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET v_done = TRUE;
    DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
    BEGIN
        SET v_error_count = v_error_count + 1;
    END;

    -- 打开游标
    OPEN employee_cursor;

    -- 循环处理每个员工
    employee_loop: LOOP
        FETCH employee_cursor INTO v_employee_id;

        IF v_done THEN
            LEAVE employee_loop;
        END IF;

        -- 调用单个员工统计计算过程
        CALL sp_calculate_attendance_statistics(v_employee_id, p_statistics_type, p_period_value);

        SET v_processed_count = v_processed_count + 1;

        -- 批量提交控制
        IF v_processed_count % p_batch_size = 0 THEN
            COMMIT;
        END IF;
    END LOOP;

    -- 关闭游标
    CLOSE employee_cursor;

    -- 最终提交
    COMMIT;

    -- 返回结果
    SELECT 'SUCCESS' as result,
           CONCAT('批量考勤统计计算完成: ', v_processed_count, ' 个员工处理完成, ', v_error_count, ' 个错误') as message;
END //

-- ========================================
-- 3. 数据清理存储过程
-- ========================================
CREATE PROCEDURE sp_cleanup_attendance_data(
    IN p_months_to_keep INT DEFAULT 24,
    IN p_batch_size INT DEFAULT 1000
)
BEGIN
    DECLARE v_deleted_count INT DEFAULT 0;
    DECLARE v_batch_deleted INT DEFAULT 0;
    DECLARE v_total_deleted INT DEFAULT 0;

    -- 创建临时表存储要删除的记录ID
    CREATE TEMPORARY TABLE IF NOT EXISTS temp_delete_records (
        record_id BIGINT PRIMARY KEY
    ) ENGINE=MEMORY;

    -- 循环删除标记为删除的数据
    REPEAT
        -- 清空临时表
        DELETE FROM temp_delete_records;

        -- 获取要删除的记录ID（已标记为删除且超过保留期）
        INSERT INTO temp_delete_records (record_id)
        SELECT record_id
        FROM t_attendance_record
        WHERE deleted_flag = 1
          AND update_time < DATE_SUB(NOW(), INTERVAL p_months_to_keep MONTH)
        LIMIT p_batch_size;

        -- 获取实际插入的记录数
        SET v_batch_deleted = ROW_COUNT();

        -- 删除记录
        IF v_batch_deleted > 0 THEN
            DELETE ar FROM t_attendance_record ar
            INNER JOIN temp_delete_records tdr ON ar.record_id = tdr.record_id;

            SET v_deleted_count = ROW_COUNT();
            SET v_total_deleted = v_total_deleted + v_deleted_count;

            -- 提交事务
            COMMIT;
        END IF;

    UNTIL v_batch_deleted = 0 END REPEAT;

    -- 清理临时表
    DROP TEMPORARY TABLE temp_delete_records;

    -- 返回结果
    SELECT 'SUCCESS' as result,
           CONCAT('考勤数据清理完成: 共删除 ', v_total_deleted, ' 条记录') as message;
END //

-- ========================================
-- 4. 异常处理存储过程
-- ========================================
CREATE PROCEDURE sp_process_attendance_exceptions(
    IN p_employee_id BIGINT DEFAULT NULL,
    IN p_days_back INT DEFAULT 7
)
BEGIN
    DECLARE v_processed_count INT DEFAULT 0;
    DECLARE v_error_count INT DEFAULT 0;

    -- 处理未处理的考勤异常
    UPDATE t_attendance_exception
    SET is_processed = 1,
        processed_by = 1,
        processed_time = NOW(),
        update_time = NOW(),
        version = version + 1
    WHERE (p_employee_id IS NULL OR employee_id = p_employee_id)
      AND is_processed = 0
      AND create_time >= DATE_SUB(NOW(), INTERVAL p_days_back DAY)
      AND deleted_flag = 0;

    SET v_processed_count = ROW_COUNT();

    -- 返回结果
    SELECT 'SUCCESS' as result,
           CONCAT('考勤异常处理完成: 共处理 ', v_processed_count, ' 条异常记录') as message;
END //

-- ========================================
-- 5. 性能统计存储过程
-- ========================================
CREATE PROCEDURE sp_get_attendance_performance_stats(
    IN p_date_from DATE,
    IN p_date_to DATE
)
BEGIN
    -- 总体统计
    SELECT
        '总打卡记录数' as metric_name,
        COUNT(*) as count_value,
        '条' as unit
    FROM t_attendance_record
    WHERE attendance_date BETWEEN p_date_from AND p_date_to
      AND deleted_flag = 0

    UNION ALL

    SELECT
        '员工总数' as metric_name,
        COUNT(DISTINCT employee_id) as count_value,
        '人' as unit
    FROM t_attendance_record
    WHERE attendance_date BETWEEN p_date_from AND p_date_to
      AND deleted_flag = 0

    UNION ALL

    SELECT
        '正常考勤数' as metric_name,
        COUNT(CASE WHEN attendance_status = 'NORMAL' THEN 1 END) as count_value,
        '条' as unit
    FROM t_attendance_record
    WHERE attendance_date BETWEEN p_date_from AND p_date_to
      AND deleted_flag = 0

    UNION ALL

    SELECT
        '异常考勤数' as metric_name,
        COUNT(CASE WHEN attendance_status != 'NORMAL' THEN 1 END) as count_value,
        '条' as unit
    FROM t_attendance_record
    WHERE attendance_date BETWEEN p_date_from AND p_date_to
      AND deleted_flag = 0

    UNION ALL

    SELECT
        '平均工作时长' as metric_name,
        ROUND(AVG(work_hours), 2) as count_value,
        '小时' as unit
    FROM t_attendance_record
    WHERE attendance_date BETWEEN p_date_from AND p_date_to
      AND deleted_flag = 0
      AND work_hours IS NOT NULL

    UNION ALL

    SELECT
        '总加班时长' as metric_name,
        ROUND(SUM(overtime_hours), 2) as count_value,
        '小时' as unit
    FROM t_attendance_record
    WHERE attendance_date BETWEEN p_date_from AND p_date_to
      AND deleted_flag = 0
      AND overtime_hours IS NOT NULL;
END //

-- ========================================
-- 6. 数据同步存储过程
-- ========================================
CREATE PROCEDURE sp_sync_attendance_data(
    IN p_source_table VARCHAR(100),
    IN p_target_table VARCHAR(100)
)
BEGIN
    DECLARE v_sync_count INT DEFAULT 0;
    DECLARE v_error_count INT DEFAULT 0;

    -- 根据表名执行不同的同步逻辑
    CASE p_source_table
        WHEN 't_attendance_record' THEN
            -- 同步考勤记录到备份表
            INSERT INTO t_attendance_record_backup (
                record_id, employee_id, attendance_date, clock_time, punch_in_time,
                punch_out_time, clock_type, record_type, device_id, punch_in_location,
                punch_out_location, punch_in_device_id, punch_out_device_id, work_hours,
                overtime_hours, attendance_status, exception_type, exception_reason,
                is_processed, processed_by, processed_time, remark, create_time,
                update_time, create_user_id, update_user_id, deleted_flag, version
            )
            SELECT
                record_id, employee_id, attendance_date, clock_time, punch_in_time,
                punch_out_time, clock_type, record_type, device_id, punch_in_location,
                punch_out_location, punch_in_device_id, punch_out_device_id, work_hours,
                overtime_hours, attendance_status, exception_type, exception_reason,
                is_processed, processed_by, processed_time, remark, create_time,
                update_time, create_user_id, update_user_id, deleted_flag, version
            FROM t_attendance_record
            WHERE update_time > COALESCE((
                SELECT MAX(update_time)
                FROM t_attendance_record_backup
            ), '1970-01-01')
            ON DUPLICATE KEY UPDATE
                employee_id = VALUES(employee_id),
                attendance_date = VALUES(attendance_date),
                clock_time = VALUES(clock_time),
                punch_in_time = VALUES(punch_in_time),
                punch_out_time = VALUES(punch_out_time),
                clock_type = VALUES(clock_type),
                record_type = VALUES(record_type),
                device_id = VALUES(device_id),
                punch_in_location = VALUES(punch_in_location),
                punch_out_location = VALUES(punch_out_location),
                punch_in_device_id = VALUES(punch_in_device_id),
                punch_out_device_id = VALUES(punch_out_device_id),
                work_hours = VALUES(work_hours),
                overtime_hours = VALUES(overtime_hours),
                attendance_status = VALUES(attendance_status),
                exception_type = VALUES(exception_type),
                exception_reason = VALUES(exception_reason),
                is_processed = VALUES(is_processed),
                processed_by = VALUES(processed_by),
                processed_time = VALUES(processed_time),
                remark = VALUES(remark),
                update_time = VALUES(update_time),
                update_user_id = VALUES(update_user_id),
                deleted_flag = VALUES(deleted_flag),
                version = VALUES(version);

            SET v_sync_count = ROW_COUNT();

        WHEN 't_attendance_statistics' THEN
            -- 同步考勤统计到备份表
            INSERT INTO t_attendance_statistics_backup (
                statistics_id, employee_id, statistics_type, statistics_period, period_value,
                total_days, present_days, absent_days, late_days, early_leave_days,
                leave_days, overtime_hours, work_hours, attendance_rate, statistics_date,
                create_time, update_time, create_user_id, update_user_id, deleted_flag, version
            )
            SELECT
                statistics_id, employee_id, statistics_type, statistics_period, period_value,
                total_days, present_days, absent_days, late_days, early_leave_days,
                leave_days, overtime_hours, work_hours, attendance_rate, statistics_date,
                create_time, update_time, create_user_id, update_user_id, deleted_flag, version
            FROM t_attendance_statistics
            WHERE update_time > COALESCE((
                SELECT MAX(update_time)
                FROM t_attendance_statistics_backup
            ), '1970-01-01')
            ON DUPLICATE KEY UPDATE
                employee_id = VALUES(employee_id),
                statistics_type = VALUES(statistics_type),
                statistics_period = VALUES(statistics_period),
                period_value = VALUES(period_value),
                total_days = VALUES(total_days),
                present_days = VALUES(present_days),
                absent_days = VALUES(absent_days),
                late_days = VALUES(late_days),
                early_leave_days = VALUES(early_leave_days),
                leave_days = VALUES(leave_days),
                overtime_hours = VALUES(overtime_hours),
                work_hours = VALUES(work_hours),
                attendance_rate = VALUES(attendance_rate),
                statistics_date = VALUES(statistics_date),
                update_time = VALUES(update_time),
                update_user_id = VALUES(update_user_id),
                deleted_flag = VALUES(deleted_flag),
                version = VALUES(version);

            SET v_sync_count = ROW_COUNT();
    END CASE;

    -- 返回结果
    SELECT 'SUCCESS' as result,
           CONCAT('数据同步完成: 共同步 ', v_sync_count, ' 条记录, 错误 ', v_error_count, ' 条') as message;
END //

DELIMITER ;

-- ========================================
-- 7. 使用示例
-- ========================================

-- 7.1 计算单个员工的月度考勤统计
-- CALL sp_calculate_attendance_statistics(1, 'MONTHLY', '2024-01');

-- 7.2 批量计算所有员工的月度考勤统计
-- CALL sp_batch_calculate_attendance_statistics('MONTHLY', '2024-01');

-- 7.3 清理超过24个月的已删除数据
-- CALL sp_cleanup_attendance_data(24);

-- 7.4 处理最近7天的考勤异常
-- CALL sp_process_attendance_exceptions(NULL, 7);

-- 7.5 获取指定日期范围的性能统计
-- CALL sp_get_attendance_performance_stats('2024-01-01', '2024-01-31');

-- 7.6 同步考勤记录数据
-- CALL sp_sync_attendance_data('t_attendance_record', 't_attendance_record_backup');

-- ========================================
-- 8. 维护和监控
-- ========================================

-- 8.1 查看存储过程列表
-- SHOW PROCEDURE STATUS WHERE Db = DATABASE() AND Name LIKE 'sp_%';

-- 8.2 查看存储过程定义
-- SHOW CREATE PROCEDURE sp_calculate_attendance_statistics;

-- 8.3 性能监控
-- SELECT EVENT_NAME, COUNT_STAR, SUM_TIMER_WAIT/1000000000 AS SUM_TIMER_WAIT_MS
-- FROM performance_schema.events_statements_summary_by_event_name
-- WHERE EVENT_NAME LIKE 'statement/sql/call_procedure'
-- ORDER BY SUM_TIMER_WAIT DESC;