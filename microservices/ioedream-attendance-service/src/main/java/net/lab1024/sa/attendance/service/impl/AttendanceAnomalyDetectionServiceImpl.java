package net.lab1024.sa.attendance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.dao.AttendanceAnomalyDao;
import net.lab1024.sa.attendance.dao.AttendanceRecordDao;
import net.lab1024.sa.attendance.dao.AttendanceRuleConfigDao;
import net.lab1024.sa.attendance.dao.WorkShiftDao;
import net.lab1024.sa.common.entity.attendance.AttendanceAnomalyEntity;
import net.lab1024.sa.common.entity.attendance.AttendanceRecordEntity;
import net.lab1024.sa.common.entity.attendance.AttendanceRuleConfigEntity;
import net.lab1024.sa.common.entity.attendance.WorkShiftEntity;
import net.lab1024.sa.attendance.service.AttendanceAnomalyDetectionService;
import net.lab1024.sa.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 考勤异常检测服务实现类
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AttendanceAnomalyDetectionServiceImpl implements AttendanceAnomalyDetectionService {

    private final AttendanceAnomalyDao anomalyDao;
    private final AttendanceRecordDao recordDao;
    private final AttendanceRuleConfigDao ruleConfigDao;
    private final WorkShiftDao workShiftDao;

    public AttendanceAnomalyDetectionServiceImpl(AttendanceAnomalyDao anomalyDao,
                                                  AttendanceRecordDao recordDao,
                                                  AttendanceRuleConfigDao ruleConfigDao,
                                                  WorkShiftDao workShiftDao) {
        this.anomalyDao = anomalyDao;
        this.recordDao = recordDao;
        this.ruleConfigDao = ruleConfigDao;
        this.workShiftDao = workShiftDao;
    }

    @Override
    public List<AttendanceAnomalyEntity> detectAnomalies(AttendanceRecordEntity record) {
        log.info("[异常检测] 开始检测考勤记录: recordId={}, userId={}, date={}",
                record.getRecordId(), record.getUserId(), record.getAttendanceDate());

        List<AttendanceAnomalyEntity> anomalies = new ArrayList<>();

        try {
            // 获取适用的规则配置
            AttendanceRuleConfigEntity rule = getApplicableRule(record);
            if (rule == null) {
                log.warn("[异常检测] 未找到适用的规则配置: userId={}", record.getUserId());
                return anomalies;
            }

            // 根据打卡类型检测不同异常
            if ("CHECK_IN".equals(record.getAttendanceType())) {
                // 上班打卡：检测迟到
                AttendanceAnomalyEntity lateAnomaly = detectLateAnomaly(record, rule);
                if (lateAnomaly != null) {
                    anomalies.add(lateAnomaly);
                }
            } else if ("CHECK_OUT".equals(record.getAttendanceType())) {
                // 下班打卡：检测早退
                AttendanceAnomalyEntity earlyAnomaly = detectEarlyAnomaly(record, rule);
                if (earlyAnomaly != null) {
                    anomalies.add(earlyAnomaly);
                }
            }

            // 保存异常记录
            if (!anomalies.isEmpty()) {
                batchCreateAnomalies(anomalies);
                log.info("[异常检测] 检测到 {} 条异常记录: userId={}", anomalies.size(), record.getUserId());
            } else {
                log.info("[异常检测] 未检测到异常: userId={}", record.getUserId());
            }

        } catch (Exception e) {
            log.error("[异常检测] 检测过程异常: recordId={}, error={}", record.getRecordId(), e.getMessage(), e);
            throw new BusinessException("ANOMALY_DETECTION_ERROR", "异常检测失败: " + e.getMessage());
        }

        return anomalies;
    }

    @Override
    public List<AttendanceAnomalyEntity> detectAnomaliesByDate(LocalDate attendanceDate) {
        log.info("[异常检测] 开始检测日期异常: date={}", attendanceDate);

        // 查询当天的所有考勤记录
        QueryWrapper<AttendanceRecordEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("attendance_date", attendanceDate);
        List<AttendanceRecordEntity> records = recordDao.selectList(queryWrapper);

        log.info("[异常检测] 查询到 {} 条考勤记录", records.size());

        List<AttendanceAnomalyEntity> allAnomalies = new ArrayList<>();
        for (AttendanceRecordEntity record : records) {
            List<AttendanceAnomalyEntity> anomalies = detectAnomalies(record);
            allAnomalies.addAll(anomalies);
        }

        // 检测缺卡异常
        List<AttendanceAnomalyEntity> missingCardAnomalies = detectMissingCardsForDate(attendanceDate);
        allAnomalies.addAll(missingCardAnomalies);

        log.info("[异常检测] 日期检测完成: date={}, totalAnomalies={}", attendanceDate, allAnomalies.size());
        return allAnomalies;
    }

    @Override
    public List<AttendanceAnomalyEntity> detectMissingCards(Long userId, LocalDate attendanceDate, Long shiftId) {
        log.info("[异常检测] 检测缺卡: userId={}, date={}, shiftId={}", userId, attendanceDate, shiftId);

        List<AttendanceAnomalyEntity> anomalies = new ArrayList<>();

        try {
            // 获取班次信息
            WorkShiftEntity shift = workShiftDao.selectById(shiftId);
            if (shift == null) {
                log.warn("[异常检测] 班次不存在: shiftId={}", shiftId);
                return anomalies;
            }

            // 查询当天的打卡记录
            QueryWrapper<AttendanceRecordEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId)
                    .eq("attendance_date", attendanceDate);
            List<AttendanceRecordEntity> records = recordDao.selectList(queryWrapper);

            // 检查是否有上班打卡
            boolean hasCheckIn = records.stream()
                    .anyMatch(r -> "CHECK_IN".equals(r.getAttendanceType()));

            // 检查是否有下班打卡
            boolean hasCheckOut = records.stream()
                    .anyMatch(r -> "CHECK_OUT".equals(r.getAttendanceType()));

            // 创建缺卡异常
            if (!hasCheckIn) {
                AttendanceAnomalyEntity anomaly = createMissingCardAnomaly(
                        userId, attendanceDate, shift, "CHECK_IN"
                );
                anomalies.add(anomaly);
            }

            if (!hasCheckOut) {
                AttendanceAnomalyEntity anomaly = createMissingCardAnomaly(
                        userId, attendanceDate, shift, "CHECK_OUT"
                );
                anomalies.add(anomaly);
            }

            log.info("[异常检测] 缺卡检测完成: userId={}, missingCount={}", userId, anomalies.size());

        } catch (Exception e) {
            log.error("[异常检测] 缺卡检测异常: userId={}, error={}", userId, e.getMessage(), e);
        }

        return anomalies;
    }

    @Override
    public AttendanceAnomalyEntity detectLateAnomaly(AttendanceRecordEntity record) {
        AttendanceRuleConfigEntity rule = getApplicableRule(record);
        return detectLateAnomaly(record, rule);
    }

    /**
     * 检测迟到异常（内部方法）
     */
    private AttendanceAnomalyEntity detectLateAnomaly(AttendanceRecordEntity record,
                                                      AttendanceRuleConfigEntity rule) {
        if (rule == null || rule.getLateCheckEnabled() == 0) {
            return null;
        }

        if (!"CHECK_IN".equals(record.getAttendanceType())) {
            return null;
        }

        try {
            // 获取班次信息
            WorkShiftEntity shift = workShiftDao.selectById(record.getShiftId());
            if (shift == null) {
                return null;
            }

            // 计算弹性时间
            int flexibleMinutes = 0;
            if (rule.getFlexibleStartEnabled() == 1) {
                flexibleMinutes = rule.getFlexibleStartMinutes();
            }

            // 计算允许的最晚打卡时间
            LocalTime workStartTime = shift.getWorkStartTime();
            LocalTime allowedLateTime = workStartTime.plusMinutes(
                    rule.getLateMinutes() + flexibleMinutes
            );

            // 获取实际打卡时间
            LocalTime actualPunchTime = record.getPunchTime().toLocalTime();

            // 判断是否迟到
            if (actualPunchTime.isAfter(allowedLateTime)) {
                // 计算迟到时长
                int lateDuration = (int) Duration.between(workStartTime, actualPunchTime).toMinutes();

                // 判断严重程度
                String severityLevel = "NORMAL";
                if (rule.getSeriousLateMinutes() > 0 &&
                    lateDuration >= rule.getSeriousLateMinutes()) {
                    severityLevel = "SERIOUS";
                }

                // 创建迟到异常记录
                AttendanceAnomalyEntity anomaly = new AttendanceAnomalyEntity();
                anomaly.setUserId(record.getUserId());
                anomaly.setUserName(record.getUserName());
                anomaly.setDepartmentId(record.getDepartmentId());
                anomaly.setDepartmentName(record.getDepartmentName());
                anomaly.setShiftId(record.getShiftId());
                anomaly.setShiftName(record.getShiftName());
                anomaly.setAttendanceDate(record.getAttendanceDate());
                anomaly.setAnomalyType("LATE");
                anomaly.setSeverityLevel(severityLevel);
                anomaly.setExpectedPunchTime(
                        LocalDateTime.of(record.getAttendanceDate(), workStartTime)
                );
                anomaly.setActualPunchTime(record.getPunchTime());
                anomaly.setPunchType("CHECK_IN");
                anomaly.setAnomalyDuration(lateDuration);
                anomaly.setAnomalyReason(String.format("迟到超过%d分钟", lateDuration));
                anomaly.setAnomalyStatus("PENDING");
                anomaly.setAttendanceRecordId(record.getRecordId());

                log.info("[异常检测] 检测到迟到异常: userId={}, lateDuration={}分钟",
                        record.getUserId(), lateDuration);

                return anomaly;
            }

        } catch (Exception e) {
            log.error("[异常检测] 迟到检测异常: recordId={}, error={}",
                    record.getRecordId(), e.getMessage(), e);
        }

        return null;
    }

    @Override
    public AttendanceAnomalyEntity detectEarlyAnomaly(AttendanceRecordEntity record) {
        AttendanceRuleConfigEntity rule = getApplicableRule(record);
        return detectEarlyAnomaly(record, rule);
    }

    /**
     * 检测早退异常（内部方法）
     */
    private AttendanceAnomalyEntity detectEarlyAnomaly(AttendanceRecordEntity record,
                                                       AttendanceRuleConfigEntity rule) {
        if (rule == null || rule.getEarlyCheckEnabled() == 0) {
            return null;
        }

        if (!"CHECK_OUT".equals(record.getAttendanceType())) {
            return null;
        }

        try {
            // 获取班次信息
            WorkShiftEntity shift = workShiftDao.selectById(record.getShiftId());
            if (shift == null) {
                return null;
            }

            // 计算弹性时间
            int flexibleMinutes = 0;
            if (rule.getFlexibleEndEnabled() == 1) {
                flexibleMinutes = rule.getFlexibleEndMinutes();
            }

            // 计算允许的最早下班时间
            LocalTime workEndTime = shift.getWorkEndTime();
            LocalTime allowedEarlyTime = workEndTime.minusMinutes(
                    rule.getEarlyMinutes() + flexibleMinutes
            );

            // 获取实际打卡时间
            LocalTime actualPunchTime = record.getPunchTime().toLocalTime();

            // 判断是否早退
            if (actualPunchTime.isBefore(allowedEarlyTime)) {
                // 计算早退时长
                int earlyDuration = (int) Duration.between(actualPunchTime, workEndTime).toMinutes();

                // 判断严重程度
                String severityLevel = "NORMAL";
                if (rule.getSeriousEarlyMinutes() > 0 &&
                    earlyDuration >= rule.getSeriousEarlyMinutes()) {
                    severityLevel = "SERIOUS";
                }

                // 创建早退异常记录
                AttendanceAnomalyEntity anomaly = new AttendanceAnomalyEntity();
                anomaly.setUserId(record.getUserId());
                anomaly.setUserName(record.getUserName());
                anomaly.setDepartmentId(record.getDepartmentId());
                anomaly.setDepartmentName(record.getDepartmentName());
                anomaly.setShiftId(record.getShiftId());
                anomaly.setShiftName(record.getShiftName());
                anomaly.setAttendanceDate(record.getAttendanceDate());
                anomaly.setAnomalyType("EARLY");
                anomaly.setSeverityLevel(severityLevel);
                anomaly.setExpectedPunchTime(
                        LocalDateTime.of(record.getAttendanceDate(), workEndTime)
                );
                anomaly.setActualPunchTime(record.getPunchTime());
                anomaly.setPunchType("CHECK_OUT");
                anomaly.setAnomalyDuration(earlyDuration);
                anomaly.setAnomalyReason(String.format("早退超过%d分钟", earlyDuration));
                anomaly.setAnomalyStatus("PENDING");
                anomaly.setAttendanceRecordId(record.getRecordId());

                log.info("[异常检测] 检测到早退异常: userId={}, earlyDuration={}分钟",
                        record.getUserId(), earlyDuration);

                return anomaly;
            }

        } catch (Exception e) {
            log.error("[异常检测] 早退检测异常: recordId={}, error={}",
                    record.getRecordId(), e.getMessage(), e);
        }

        return null;
    }

    @Override
    public AttendanceAnomalyEntity detectAbsentAnomaly(Long userId, LocalDate attendanceDate, Long shiftId) {
        log.info("[异常检测] 检测旷工: userId={}, date={}, shiftId={}", userId, attendanceDate, shiftId);

        try {
            AttendanceRuleConfigEntity rule = ruleConfigDao.selectGlobalRule();
            if (rule == null || rule.getAbsentCheckEnabled() == 0) {
                return null;
            }

            // 获取班次信息
            WorkShiftEntity shift = workShiftDao.selectById(shiftId);
            if (shift == null) {
                return null;
            }

            // 查询当天的打卡记录
            QueryWrapper<AttendanceRecordEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId)
                    .eq("attendance_date", attendanceDate);
            List<AttendanceRecordEntity> records = recordDao.selectList(queryWrapper);

            // 检查是否有任何打卡记录
            if (records.isEmpty()) {
                // 全天无打卡记录，判定为旷工
                return createAbsentAnomaly(userId, attendanceDate, shift, "全天无打卡记录");
            }

            // 检查是否迟到超过规定时间转旷工
            if (rule.getLateToAbsentMinutes() != null && rule.getLateToAbsentMinutes() > 0) {
                for (AttendanceRecordEntity record : records) {
                    if ("CHECK_IN".equals(record.getAttendanceType())) {
                        LocalTime workStartTime = shift.getWorkStartTime();
                        LocalTime actualPunchTime = record.getPunchTime().toLocalTime();
                        int lateMinutes = (int) Duration.between(workStartTime, actualPunchTime).toMinutes();

                        if (lateMinutes >= rule.getLateToAbsentMinutes()) {
                            return createAbsentAnomaly(userId, attendanceDate, shift,
                                    String.format("迟到超过%d分钟，判定为旷工", lateMinutes));
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error("[异常检测] 旷工检测异常: userId={}, error={}", userId, e.getMessage(), e);
        }

        return null;
    }

    @Override
    public Long createAnomaly(AttendanceAnomalyEntity anomaly) {
        try {
            anomalyDao.insert(anomaly);
            log.info("[异常检测] 创建异常记录成功: anomalyId={}, type={}, userId={}",
                    anomaly.getAnomalyId(), anomaly.getAnomalyType(), anomaly.getUserId());
            return anomaly.getAnomalyId();
        } catch (Exception e) {
            log.error("[异常检测] 创建异常记录失败: error={}", e.getMessage(), e);
            throw new BusinessException("ANOMALY_CREATE_ERROR", "创建异常记录失败: " + e.getMessage());
        }
    }

    @Override
    public Integer batchCreateAnomalies(List<AttendanceAnomalyEntity> anomalies) {
        if (anomalies == null || anomalies.isEmpty()) {
            return 0;
        }

        try {
            int count = 0;
            for (AttendanceAnomalyEntity anomaly : anomalies) {
                anomalyDao.insert(anomaly);
                count++;
            }
            log.info("[异常检测] 批量创建异常记录成功: count={}", count);
            return count;
        } catch (Exception e) {
            log.error("[异常检测] 批量创建异常记录失败: error={}", e.getMessage(), e);
            throw new BusinessException("ANOMALY_BATCH_CREATE_ERROR", "批量创建异常记录失败: " + e.getMessage());
        }
    }

    @Override
    public Integer autoDetectAndCreate(LocalDate attendanceDate) {
        log.info("[异常检测] 自动检测开始: date={}", attendanceDate);

        try {
            List<AttendanceAnomalyEntity> anomalies = detectAnomaliesByDate(attendanceDate);

            // 过滤已存在的异常记录，避免重复创建
            List<AttendanceAnomalyEntity> newAnomalies = filterExistingAnomalies(anomalies);

            if (!newAnomalies.isEmpty()) {
                batchCreateAnomalies(newAnomalies);
            }

            log.info("[异常检测] 自动检测完成: date={}, total={}, new={}",
                    attendanceDate, anomalies.size(), newAnomalies.size());

            return newAnomalies.size();

        } catch (Exception e) {
            log.error("[异常检测] 自动检测异常: date={}, error={}", attendanceDate, e.getMessage(), e);
            return 0;
        }
    }

    // ========== 私有辅助方法 ==========

    /**
     * 获取适用的规则配置
     */
    private AttendanceRuleConfigEntity getApplicableRule(AttendanceRecordEntity record) {
        return ruleConfigDao.selectApplicableRule(
                record.getUserId(),
                record.getDepartmentId(),
                record.getShiftId()
        );
    }

    /**
     * 创建缺卡异常记录
     */
    private AttendanceAnomalyEntity createMissingCardAnomaly(Long userId,
                                                             LocalDate attendanceDate,
                                                             WorkShiftEntity shift,
                                                             String punchType) {
        AttendanceAnomalyEntity anomaly = new AttendanceAnomalyEntity();
        anomaly.setUserId(userId);
        anomaly.setShiftId(shift.getShiftId());
        anomaly.setShiftName(shift.getShiftName());
        anomaly.setAttendanceDate(attendanceDate);
        anomaly.setAnomalyType("MISSING_CARD");
        anomaly.setSeverityLevel("NORMAL");
        anomaly.setPunchType(punchType);

        // 设置应打卡时间
        LocalTime expectedTime = "CHECK_IN".equals(punchType) ?
                shift.getWorkStartTime() : shift.getWorkEndTime();
        anomaly.setExpectedPunchTime(LocalDateTime.of(attendanceDate, expectedTime));

        anomaly.setAnomalyReason("未在规定时间打卡");
        anomaly.setAnomalyStatus("PENDING");

        return anomaly;
    }

    /**
     * 创建旷工异常记录
     */
    private AttendanceAnomalyEntity createAbsentAnomaly(Long userId,
                                                        LocalDate attendanceDate,
                                                        WorkShiftEntity shift,
                                                        String reason) {
        AttendanceAnomalyEntity anomaly = new AttendanceAnomalyEntity();
        anomaly.setUserId(userId);
        anomaly.setShiftId(shift.getShiftId());
        anomaly.setShiftName(shift.getShiftName());
        anomaly.setAttendanceDate(attendanceDate);
        anomaly.setAnomalyType("ABSENT");
        anomaly.setSeverityLevel("CRITICAL");
        anomaly.setAnomalyReason(reason);
        anomaly.setAnomalyStatus("PENDING");
        anomaly.setAnomalyDuration(shift.getWorkDuration() != null ?
                shift.getWorkDuration() : 480); // 工作时长（分钟）

        return anomaly;
    }

    /**
     * 检测指定日期的缺卡异常
     */
    private List<AttendanceAnomalyEntity> detectMissingCardsForDate(LocalDate attendanceDate) {
        List<AttendanceAnomalyEntity> anomalies = new ArrayList<>();

        // TODO: 实现逻辑：查询当天应该打卡的员工，检查是否缺卡

        return anomalies;
    }

    /**
     * 过滤已存在的异常记录
     */
    private List<AttendanceAnomalyEntity> filterExistingAnomalies(List<AttendanceAnomalyEntity> anomalies) {
        // TODO: 实现逻辑：检查数据库中是否已存在相同的异常记录
        return anomalies;
    }
}
