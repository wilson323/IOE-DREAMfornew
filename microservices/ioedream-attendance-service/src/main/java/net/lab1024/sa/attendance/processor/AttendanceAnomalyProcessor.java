package net.lab1024.sa.attendance.processor;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.dao.AttendanceRecordDao;
import net.lab1024.sa.attendance.dao.SmartScheduleResultDao;
import net.lab1024.sa.attendance.entity.AttendanceRecordEntity;
import net.lab1024.sa.attendance.entity.SmartScheduleResultEntity;
import net.lab1024.sa.attendance.message.AttendanceAnomalyEventProducer;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 考勤异常处理器
 * <p>
 * 严格遵循四层架构规范（Controller→Service→Manager→DAO）
 * 负责检测、记录和处理异常考勤情况
 * </p>
 * <p>
 * 核心职责：
 * - 检测迟到、早退、缺勤等异常
 * - 识别频繁打卡、跨设备打卡等可疑行为
 * - 记录异常详情并触发告警
 * - 支持异常级别分类和统计
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@Component
@Slf4j
public class AttendanceAnomalyProcessor {

    @Resource
    private AttendanceRecordDao attendanceRecordDao;

    @Resource
    private SmartScheduleResultDao smartScheduleResultDao;

    @Resource
    private AttendanceAnomalyEventProducer anomalyEventProducer;

    // 异常缓存（避免重复检测）
    private final Map<String, AnomalyRecord> anomalyCache = new ConcurrentHashMap<>();

    /**
     * 检测考勤异常
     *
     * @param record 考勤记录
     * @return 检测到的异常列表
     */
    public List<AttendanceAnomaly> detectAnomalies(AttendanceRecordEntity record) {
        if (record == null) {
            return Collections.emptyList();
        }

        log.debug("[异常检测] 开始检测考勤异常: userId={}, date={}",
                record.getUserId(), record.getAttendanceDate());

        List<AttendanceAnomaly> anomalies = new ArrayList<>();

        try {
            // 1. 检测迟到异常
            AttendanceAnomaly lateAnomaly = detectLateAnomaly(record);
            if (lateAnomaly != null) {
                anomalies.add(lateAnomaly);
            }

            // 2. 检测早退异常
            AttendanceAnomaly earlyLeaveAnomaly = detectEarlyLeaveAnomaly(record);
            if (earlyLeaveAnomaly != null) {
                anomalies.add(earlyLeaveAnomaly);
            }

            // 3. 检测缺勤异常
            AttendanceAnomaly absentAnomaly = detectAbsentAnomaly(record);
            if (absentAnomaly != null) {
                anomalies.add(absentAnomaly);
            }

            // 4. 检测可疑打卡
            List<AttendanceAnomaly> suspiciousAnomalies = detectSuspiciousPunch(record);
            anomalies.addAll(suspiciousAnomalies);

            // 5. 记录并发布异常事件
            if (!anomalies.isEmpty()) {
                recordAndPublishAnomalies(record, anomalies);
            }

            log.debug("[异常检测] 检测完成: userId={}, 异常数={}",
                    record.getUserId(), anomalies.size());

            return anomalies;

        } catch (Exception e) {
            log.error("[异常检测] 检测考勤异常失败: userId={}", record.getUserId(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 批量检测考勤异常
     *
     * @param records 考勤记录列表
     * @return 异常统计
     */
    public AnomalyStatistics batchDetectAnomalies(List<AttendanceRecordEntity> records) {
        log.info("[异常检测] 开始批量检测考勤异常: recordCount={}", records.size());

        AnomalyStatistics statistics = new AnomalyStatistics();
        statistics.setTotalRecords(records.size());
        statistics.setDetectionTime(LocalDateTime.now());

        int totalAnomalies = 0;
        Map<String, Integer> anomalyTypeCount = new HashMap<>();

        for (AttendanceRecordEntity record : records) {
            List<AttendanceAnomaly> anomalies = detectAnomalies(record);

            if (!anomalies.isEmpty()) {
                totalAnomalies += anomalies.size();

                for (AttendanceAnomaly anomaly : anomalies) {
                    String type = anomaly.getAnomalyType();
                    anomalyTypeCount.put(type, anomalyTypeCount.getOrDefault(type, 0) + 1);
                }
            }
        }

        statistics.setTotalAnomalies(totalAnomalies);
        statistics.setAnomalyTypeCount(anomalyTypeCount);

        log.info("[异常检测] 批量检测完成: recordCount={}, totalAnomalies={}",
                records.size(), totalAnomalies);

        return statistics;
    }

    /**
     * 检测迟到异常
     */
    private AttendanceAnomaly detectLateAnomaly(AttendanceRecordEntity record) {
        try {
            // 获取用户的排班规则
            LocalTime workStartTime = getWorkStartTime(record.getUserId(), record.getAttendanceDate());
            if (workStartTime == null) {
                return null; // 无排班规则，不检测
            }

            // 检查打卡时间
            LocalDateTime punchDateTime = record.getPunchTime();
            if (punchDateTime == null) {
                return null;
            }
            LocalTime punchTime = punchDateTime.toLocalTime();

            // 判断是否迟到（超过上班时间5分钟）
            if (punchTime.isAfter(workStartTime.plusMinutes(5))) {
                long lateMinutes = Duration.between(workStartTime, punchTime).toMinutes();

                AttendanceAnomaly anomaly = new AttendanceAnomaly();
                anomaly.setAnomalyId(generateAnomalyId());
                anomaly.setUserId(record.getUserId());
                anomaly.setDepartmentId(record.getDepartmentId());
                anomaly.setAttendanceDate(record.getAttendanceDate());
                anomaly.setAnomalyType("LATE");
                anomaly.setAnomalyLevel(lateMinutes > 30 ? "HIGH" : "MEDIUM");
                anomaly.setDescription(String.format("迟到%d分钟", lateMinutes));
                anomaly.setDetectionTime(LocalDateTime.now());

                log.info("[异常检测] 检测到迟到异常: userId={}, date={}, lateMinutes={}",
                        record.getUserId(), record.getAttendanceDate(), lateMinutes);

                return anomaly;
            }

        } catch (Exception e) {
            log.error("[异常检测] 检测迟到异常失败", e);
        }

        return null;
    }

    /**
     * 检测早退异常
     */
    private AttendanceAnomaly detectEarlyLeaveAnomaly(AttendanceRecordEntity record) {
        try {
            // 获取用户的排班规则
            LocalTime workEndTime = getWorkEndTime(record.getUserId(), record.getAttendanceDate());
            if (workEndTime == null) {
                return null; // 无排班规则，不检测
            }

            // 检查打卡时间
            LocalDateTime punchDateTime = record.getPunchTime();
            if (punchDateTime == null) {
                return null;
            }
            LocalTime punchTime = punchDateTime.toLocalTime();

            // 判断是否早退（提前下班超过5分钟）
            if (punchTime.isBefore(workEndTime.minusMinutes(5))) {
                long earlyMinutes = Duration.between(punchTime, workEndTime).toMinutes();

                AttendanceAnomaly anomaly = new AttendanceAnomaly();
                anomaly.setAnomalyId(generateAnomalyId());
                anomaly.setUserId(record.getUserId());
                anomaly.setDepartmentId(record.getDepartmentId());
                anomaly.setAttendanceDate(record.getAttendanceDate());
                anomaly.setAnomalyType("EARLY_LEAVE");
                anomaly.setAnomalyLevel(earlyMinutes > 30 ? "HIGH" : "MEDIUM");
                anomaly.setDescription(String.format("早退%d分钟", earlyMinutes));
                anomaly.setDetectionTime(LocalDateTime.now());

                log.info("[异常检测] 检测到早退异常: userId={}, date={}, earlyMinutes={}",
                        record.getUserId(), record.getAttendanceDate(), earlyMinutes);

                return anomaly;
            }

        } catch (Exception e) {
            log.error("[异常检测] 检测早退异常失败", e);
        }

        return null;
    }

    /**
     * 检测缺勤异常
     */
    private AttendanceAnomaly detectAbsentAnomaly(AttendanceRecordEntity record) {
        // 如果记录状态已标记为缺勤，则记录异常
        if ("ABSENT".equals(record.getAttendanceStatus())) {
            AttendanceAnomaly anomaly = new AttendanceAnomaly();
            anomaly.setAnomalyId(generateAnomalyId());
            anomaly.setUserId(record.getUserId());
            anomaly.setDepartmentId(record.getDepartmentId());
            anomaly.setAttendanceDate(record.getAttendanceDate());
            anomaly.setAnomalyType("ABSENT");
            anomaly.setAnomalyLevel("HIGH");
            anomaly.setDescription("未打卡且无请假");
            anomaly.setDetectionTime(LocalDateTime.now());

            log.warn("[异常检测] 检测到缺勤异常: userId={}, date={}",
                    record.getUserId(), record.getAttendanceDate());

            return anomaly;
        }

        return null;
    }

    /**
     * 检测可疑打卡行为
     */
    private List<AttendanceAnomaly> detectSuspiciousPunch(AttendanceRecordEntity record) {
        List<AttendanceAnomaly> anomalies = new ArrayList<>();

        try {
            // 1. 检测频繁打卡（短时间内多次打卡）
            List<AttendanceRecordEntity> recentPunches = getRecentPunches(
                    record.getUserId(),
                    record.getAttendanceDate(),
                    5); // 5分钟内

            if (recentPunches.size() > 3) {
                AttendanceAnomaly anomaly = new AttendanceAnomaly();
                anomaly.setAnomalyId(generateAnomalyId());
                anomaly.setUserId(record.getUserId());
                anomaly.setDepartmentId(record.getDepartmentId());
                anomaly.setAttendanceDate(record.getAttendanceDate());
                anomaly.setAnomalyType("FREQUENT_PUNCH");
                anomaly.setAnomalyLevel("LOW");
                anomaly.setDescription(String.format("短时间内打卡%d次", recentPunches.size()));
                anomaly.setDetectionTime(LocalDateTime.now());

                anomalies.add(anomaly);

                log.warn("[异常检测] 检测到频繁打卡: userId={}, date={}, count={}",
                        record.getUserId(), record.getAttendanceDate(), recentPunches.size());
            }

            // 2. 检测跨设备打卡（短时间内不同地点打卡）
            List<AttendanceRecordEntity> multiDevicePunches = recentPunches.stream()
                    .filter(r -> !r.getDeviceId().equals(record.getDeviceId()))
                    .collect(Collectors.toList());

            if (!multiDevicePunches.isEmpty()) {
                AttendanceAnomaly anomaly = new AttendanceAnomaly();
                anomaly.setAnomalyId(generateAnomalyId());
                anomaly.setUserId(record.getUserId());
                anomaly.setDepartmentId(record.getDepartmentId());
                anomaly.setAttendanceDate(record.getAttendanceDate());
                anomaly.setAnomalyType("MULTI_DEVICE_PUNCH");
                anomaly.setAnomalyLevel("MEDIUM");
                anomaly.setDescription(String.format("短时间内使用%d个不同设备打卡",
                        multiDevicePunches.size() + 1));
                anomaly.setDetectionTime(LocalDateTime.now());

                anomalies.add(anomaly);

                log.warn("[异常检测] 检测到跨设备打卡: userId={}, date={}, deviceCount={}",
                        record.getUserId(), record.getAttendanceDate(), multiDevicePunches.size() + 1);
            }

        } catch (Exception e) {
            log.error("[异常检测] 检测可疑打卡失败", e);
        }

        return anomalies;
    }

    /**
     * 记录并发布异常事件
     */
    private void recordAndPublishAnomalies(AttendanceRecordEntity record, List<AttendanceAnomaly> anomalies) {
        try {
            // 1. 缓存异常记录
            for (AttendanceAnomaly anomaly : anomalies) {
                String cacheKey = generateCacheKey(anomaly);
                anomalyCache.put(cacheKey, new AnomalyRecord(anomaly, record));
            }

            // 2. 发布异常事件到消息队列
            anomalyEventProducer.sendAnomalyEvent(anomalies);

        } catch (Exception e) {
            log.error("[异常检测] 记录并发布异常事件失败", e);
        }
    }

    /**
     * 获取用户上班时间
     */
    private LocalTime getWorkStartTime(Long userId, LocalDate date) {
        try {
            LambdaQueryWrapper<SmartScheduleResultEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SmartScheduleResultEntity::getEmployeeId, userId)
                    .eq(SmartScheduleResultEntity::getScheduleDate, date)
                    .eq(SmartScheduleResultEntity::getDeletedFlag, 0);

            SmartScheduleResultEntity schedule = smartScheduleResultDao.selectOne(queryWrapper);
            return schedule != null ? schedule.getWorkStartTime() : null;

        } catch (Exception e) {
            log.error("[异常检测] 获取用户上班时间失败: userId={}, date={}", userId, date, e);
            return null;
        }
    }

    /**
     * 获取用户下班时间
     */
    private LocalTime getWorkEndTime(Long userId, LocalDate date) {
        try {
            LambdaQueryWrapper<SmartScheduleResultEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SmartScheduleResultEntity::getEmployeeId, userId)
                    .eq(SmartScheduleResultEntity::getScheduleDate, date)
                    .eq(SmartScheduleResultEntity::getDeletedFlag, 0);

            SmartScheduleResultEntity schedule = smartScheduleResultDao.selectOne(queryWrapper);
            return schedule != null ? schedule.getWorkEndTime() : null;

        } catch (Exception e) {
            log.error("[异常检测] 获取用户下班时间失败: userId={}, date={}", userId, date, e);
            return null;
        }
    }

    /**
     * 获取最近的打卡记录
     */
    private List<AttendanceRecordEntity> getRecentPunches(Long userId, LocalDate date, int minutes) {
        try {
            LocalDateTime startDateTime = date.atStartOfDay();
            LocalDateTime endDateTime = date.atTime(LocalTime.MAX);

            LambdaQueryWrapper<AttendanceRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AttendanceRecordEntity::getUserId, userId)
                    .eq(AttendanceRecordEntity::getAttendanceDate, date)
                    .eq(AttendanceRecordEntity::getDeletedFlag, 0)
                    .orderByDesc(AttendanceRecordEntity::getPunchTime)
                    .last("50"); // 最多查询50条

            return attendanceRecordDao.selectList(queryWrapper);

        } catch (Exception e) {
            log.error("[异常检测] 获取最近打卡记录失败: userId={}, date={}", userId, date, e);
            return Collections.emptyList();
        }
    }

    /**
     * 生成异常ID
     */
    private String generateAnomalyId() {
        return "ANOMALY-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * 生成缓存键
     */
    private String generateCacheKey(AttendanceAnomaly anomaly) {
        return String.format("%s:%d:%s",
                anomaly.getAnomalyType(),
                anomaly.getUserId(),
                anomaly.getAttendanceDate());
    }

    /**
     * 清除异常缓存
     */
    public void clearAnomalyCache() {
        log.info("[异常检测] 清除异常缓存");
        anomalyCache.clear();
    }

    // ==================== 内部类 ====================

    /**
     * 考勤异常
     */
    public static class AttendanceAnomaly {
        private String anomalyId;
        private Long userId;
        private Long departmentId;
        private LocalDate attendanceDate;
        private String anomalyType; // LATE, EARLY_LEAVE, ABSENT, FREQUENT_PUNCH, MULTI_DEVICE_PUNCH
        private String anomalyLevel; // HIGH, MEDIUM, LOW
        private String description;
        private LocalDateTime detectionTime;

        // Getters and Setters
        public String getAnomalyId() {
            return anomalyId;
        }

        public void setAnomalyId(String anomalyId) {
            this.anomalyId = anomalyId;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public Long getDepartmentId() {
            return departmentId;
        }

        public void setDepartmentId(Long departmentId) {
            this.departmentId = departmentId;
        }

        public LocalDate getAttendanceDate() {
            return attendanceDate;
        }

        public void setAttendanceDate(LocalDate attendanceDate) {
            this.attendanceDate = attendanceDate;
        }

        public String getAnomalyType() {
            return anomalyType;
        }

        public void setAnomalyType(String anomalyType) {
            this.anomalyType = anomalyType;
        }

        public String getAnomalyLevel() {
            return anomalyLevel;
        }

        public void setAnomalyLevel(String anomalyLevel) {
            this.anomalyLevel = anomalyLevel;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public LocalDateTime getDetectionTime() {
            return detectionTime;
        }

        public void setDetectionTime(LocalDateTime detectionTime) {
            this.detectionTime = detectionTime;
        }
    }

    /**
     * 异常记录
     */
    private static class AnomalyRecord {
        private final AttendanceAnomaly anomaly;
        private final AttendanceRecordEntity record;
        private final LocalDateTime recordTime;

        public AnomalyRecord(AttendanceAnomaly anomaly, AttendanceRecordEntity record) {
            this.anomaly = anomaly;
            this.record = record;
            this.recordTime = LocalDateTime.now();
        }

        public AttendanceAnomaly getAnomaly() {
            return anomaly;
        }

        public AttendanceRecordEntity getRecord() {
            return record;
        }

        public LocalDateTime getRecordTime() {
            return recordTime;
        }
    }

    /**
     * 异常统计
     */
    public static class AnomalyStatistics {
        private int totalRecords;
        private int totalAnomalies;
        private Map<String, Integer> anomalyTypeCount;
        private LocalDateTime detectionTime;

        // Getters and Setters
        public int getTotalRecords() {
            return totalRecords;
        }

        public void setTotalRecords(int totalRecords) {
            this.totalRecords = totalRecords;
        }

        public int getTotalAnomalies() {
            return totalAnomalies;
        }

        public void setTotalAnomalies(int totalAnomalies) {
            this.totalAnomalies = totalAnomalies;
        }

        public Map<String, Integer> getAnomalyTypeCount() {
            return anomalyTypeCount;
        }

        public void setAnomalyTypeCount(Map<String, Integer> anomalyTypeCount) {
            this.anomalyTypeCount = anomalyTypeCount;
        }

        public LocalDateTime getDetectionTime() {
            return detectionTime;
        }

        public void setDetectionTime(LocalDateTime detectionTime) {
            this.detectionTime = detectionTime;
        }
    }
}
