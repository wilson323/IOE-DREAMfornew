package net.lab1024.sa.attendance.service;

import net.lab1024.sa.attendance.entity.AttendanceAnomalyEntity;
import net.lab1024.sa.attendance.entity.AttendanceRecordEntity;

import java.time.LocalDate;
import java.util.List;

/**
 * 考勤异常检测服务接口
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface AttendanceAnomalyDetectionService {

    /**
     * 检测单个考勤记录的异常
     *
     * @param record 考勤记录
     * @return 检测到的异常记录列表（可能为空）
     */
    List<AttendanceAnomalyEntity> detectAnomalies(AttendanceRecordEntity record);

    /**
     * 检测指定日期的所有异常
     *
     * @param attendanceDate 考勤日期
     * @return 检测到的异常记录列表
     */
    List<AttendanceAnomalyEntity> detectAnomaliesByDate(LocalDate attendanceDate);

    /**
     * 检测缺卡异常
     *
     * @param userId 用户ID
     * @param attendanceDate 考勤日期
     * @param shiftId 班次ID
     * @return 缺卡异常记录列表
     */
    List<AttendanceAnomalyEntity> detectMissingCards(Long userId, LocalDate attendanceDate, Long shiftId);

    /**
     * 检测迟到异常
     *
     * @param record 考勤记录
     * @return 迟到异常记录（如果不存在迟到则返回null）
     */
    AttendanceAnomalyEntity detectLateAnomaly(AttendanceRecordEntity record);

    /**
     * 检测早退异常
     *
     * @param record 考勤记录
     * @return 早退异常记录（如果不存在早退则返回null）
     */
    AttendanceAnomalyEntity detectEarlyAnomaly(AttendanceRecordEntity record);

    /**
     * 检测旷工异常
     *
     * @param userId 用户ID
     * @param attendanceDate 考勤日期
     * @param shiftId 班次ID
     * @return 旷工异常记录（如果不存在旷工则返回null）
     */
    AttendanceAnomalyEntity detectAbsentAnomaly(Long userId, LocalDate attendanceDate, Long shiftId);

    /**
     * 创建异常记录
     *
     * @param anomaly 异常实体
     * @return 创建的异常记录ID
     */
    Long createAnomaly(AttendanceAnomalyEntity anomaly);

    /**
     * 批量创建异常记录
     *
     * @param anomalies 异常记录列表
     * @return 创建的记录数量
     */
    Integer batchCreateAnomalies(List<AttendanceAnomalyEntity> anomalies);

    /**
     * 自动检测并创建异常记录（定时任务调用）
     *
     * @param attendanceDate 考勤日期
     * @return 检测并创建的异常记录数量
     */
    Integer autoDetectAndCreate(LocalDate attendanceDate);
}
