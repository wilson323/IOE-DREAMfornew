package net.lab1024.sa.attendance.realtime.anomaly;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import net.lab1024.sa.attendance.realtime.cache.RealtimeCacheManager;
import net.lab1024.sa.attendance.realtime.event.AttendanceEvent;
import net.lab1024.sa.attendance.realtime.model.AnomalyDetectionResult;
import net.lab1024.sa.attendance.realtime.model.AnomalyFilterParameters;
import net.lab1024.sa.attendance.realtime.model.TimeRange;

/**
 * 考勤异常检测服务
 * <p>
 * 负责考勤异常的检测和识别功能
 * </p>
 * <p>
 * 职责范围：
 * <ul>
 *   <li>检测频繁打卡异常（10分钟内打卡超过3次）</li>
 *   <li>检测跨设备打卡异常（30分钟内在距离>5公里的设备上打卡）</li>
 *   <li>检测异常时间打卡（凌晨0-6点、深夜22-24点）</li>
 *   <li>检测连续缺勤异常（连续3天未打卡）</li>
 *   <li>检测打卡地点异常（非允许地点、移动速度过快）</li>
 *   <li>检测早退迟到异常（迟到超过30分钟、早退超过30分钟）</li>
 * </ul>
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@Service
public class AttendanceAnomalyDetectionService {

    /**
     * 缓存管理器（用于缓存打卡记录）
     */
    @Resource
    private RealtimeCacheManager cacheManager;

    /**
     * 引擎状态
     */
    private volatile boolean running = false;

    /**
     * 检测考勤异常
     * <p>
     * P0级核心功能：统一的异常检测入口，支持6种异常类型
     * </p>
     *
     * @param timeRange 时间范围
     * @param filterParameters 过滤参数
     * @return 异常检测结果
     */
    public AnomalyDetectionResult calculateAttendanceAnomalies(TimeRange timeRange,
            AnomalyFilterParameters filterParameters) {
        log.info("[异常检测] 计算考勤异常，时间范围: {} - {}, 过滤参数: {}",
                timeRange.getWorkStartTime(), timeRange.getWorkEndTime(), filterParameters);

        if (!running) {
            return AnomalyDetectionResult.builder()
                    .detectionId(UUID.randomUUID().toString())
                    .detectionTime(LocalDateTime.now())
                    .detectionSuccessful(false)
                    .errorMessage("引擎未运行")
                    .build();
        }

        try {
            List<Object> detectedAnomalies = new ArrayList<>();

            // 1. 频繁打卡异常检测
            detectFrequentPunchAnomalies(timeRange, filterParameters, detectedAnomalies);

            // 2. 跨设备打卡异常检测
            detectCrossDevicePunchAnomalies(timeRange, filterParameters, detectedAnomalies);

            // 3. 异常时间打卡检测
            detectAbnormalTimePunchAnomalies(timeRange, filterParameters, detectedAnomalies);

            // 4. 连续缺勤检测
            detectContinuousAbsenceAnomalies(timeRange, filterParameters, detectedAnomalies);

            // 5. 打卡地点异常检测
            detectAbnormalLocationAnomalies(timeRange, filterParameters, detectedAnomalies);

            // 6. 早退迟到异常检测
            detectEarlyLeaveLateArrivalAnomalies(timeRange, filterParameters, detectedAnomalies);

            log.info("[异常检测] 异常检测完成，检测到异常数量: {}", detectedAnomalies.size());

            return AnomalyDetectionResult.builder()
                    .detectionId(UUID.randomUUID().toString())
                    .detectionTime(LocalDateTime.now())
                    .timeRange(timeRange)
                    .anomalies(detectedAnomalies)
                    .detectionSuccessful(true)
                    .build();

        } catch (Exception e) {
            log.error("[异常检测] 计算考勤异常失败", e);
            return AnomalyDetectionResult.builder()
                    .detectionId(UUID.randomUUID().toString())
                    .detectionTime(LocalDateTime.now())
                    .detectionSuccessful(false)
                    .errorMessage("异常检测失败: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 设置引擎运行状态
     *
     * @param running 运行状态
     */
    public void setRunning(boolean running) {
        this.running = running;
        log.info("[异常检测] 引擎状态更新: running={}", running);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 检测频繁打卡异常
     * <p>
     * P0级核心异常检测：短时间内多次打卡（如：10分钟内打卡超过3次）
     * </p>
     */
    private void detectFrequentPunchAnomalies(TimeRange timeRange, AnomalyFilterParameters filterParameters,
            List<Object> detectedAnomalies) {
        log.debug("[异常检测] 检测频繁打卡异常");

        try {
            // TODO: 从数据库或缓存中查询指定时间范围内的打卡记录
            // 这里实现检测逻辑：
            // 1. 按员工ID分组
            // 2. 对每个员工的打卡记录按时间排序
            // 3. 检测10分钟内打卡次数是否超过阈值（默认3次）
            // 4. 如果超过阈值，生成异常记录

            // 示例实现（伪代码）：
            // Map<Long, List<AttendanceEvent>> employeeEvents = groupEventsByEmployee(timeRange);
            // for (Map.Entry<Long, List<AttendanceEvent>> entry : employeeEvents.entrySet()) {
            //     List<AttendanceEvent> events = entry.getValue();
            //     for (int i = 0; i < events.size(); i++) {
            //         int frequentCount = 0;
            //         for (int j = i + 1; j < events.size(); j++) {
            //             long minutesDiff = ChronoUnit.MINUTES.between(events.get(i).getEventTime(),
            //                         events.get(j).getEventTime());
            //             if (minutesDiff <= 10) {
            //                 frequentCount++;
            //             } else {
            //                 break;
            //             }
            //         }
            //         if (frequentCount >= 3) {
            //             detectedAnomalies.add(createFrequentPunchAnomaly(entry.getKey(), events.get(i), frequentCount));
            //         }
            //     }
            // }

            log.trace("[异常检测] 频繁打卡异常检测完成");

        } catch (Exception e) {
            log.error("[异常检测] 频繁打卡异常检测失败", e);
        }
    }

    /**
     * 检测跨设备打卡异常
     * <p>
     * P0级核心异常检测：短时间内（如30分钟）在不同设备上打卡，可能存在代打卡行为
     * </p>
     */
    private void detectCrossDevicePunchAnomalies(TimeRange timeRange, AnomalyFilterParameters filterParameters,
            List<Object> detectedAnomalies) {
        log.debug("[异常检测] 检测跨设备打卡异常");

        try {
            // TODO: 从数据库或缓存中查询指定时间范围内的打卡记录
            // 这里实现检测逻辑：
            // 1. 按员工ID分组
            // 2. 对每个员工的打卡记录按时间排序
            // 3. 检测30分钟内是否在不同设备上打卡
            // 4. 计算设备间距离（如果设备有位置信息）
            // 5. 如果距离超过阈值（如5公里），生成异常记录

            // 示例实现（伪代码）：
            // Map<Long, List<AttendanceEvent>> employeeEvents = groupEventsByEmployee(timeRange);
            // for (Map.Entry<Long, List<AttendanceEvent>> entry : employeeEvents.entrySet()) {
            //     List<AttendanceEvent> events = entry.getValue();
            //     for (int i = 0; i < events.size(); i++) {
            //         for (int j = i + 1; j < events.size(); j++) {
            //             long minutesDiff = ChronoUnit.MINUTES.between(events.get(i).getEventTime(),
            //                         events.get(j).getEventTime());
            //             if (minutesDiff > 30) {
            //                 break; // 超过30分钟，不可能是连续打卡
            //             }
            //             if (!events.get(i).getDeviceId().equals(events.get(j).getDeviceId())) {
            //                 // 不同设备，计算距离
            //                 double distance = calculateDistance(events.get(i), events.get(j));
            //                 if (distance > 5.0) { // 超过5公里
            //                     detectedAnomalies.add(createCrossDeviceAnomaly(entry.getKey(),
            //                                 events.get(i), events.get(j), distance));
            //                 }
            //             }
            //         }
            //     }
            // }

            log.trace("[异常检测] 跨设备打卡异常检测完成");

        } catch (Exception e) {
            log.error("[异常检测] 跨设备打卡异常检测失败", e);
        }
    }

    /**
     * 检测异常时间打卡
     * <p>
     * P0级核心异常检测：在非工作时间打卡（如凌晨、深夜）
     * </p>
     */
    private void detectAbnormalTimePunchAnomalies(TimeRange timeRange, AnomalyFilterParameters filterParameters,
            List<Object> detectedAnomalies) {
        log.debug("[异常检测] 检测异常时间打卡");

        try {
            // TODO: 从数据库或缓存中查询指定时间范围内的打卡记录
            // 这里实现检测逻辑：
            // 1. 查询所有打卡记录
            // 2. 检测打卡时间是否在异常时间段（如0:00-6:00, 22:00-24:00）
            // 3. 如果是，生成异常记录

            // 示例实现（伪代码）：
            // List<AttendanceEvent> events = queryEvents(timeRange);
            // for (AttendanceEvent event : events) {
            //     int hour = event.getEventTime().getHour();
            //     if (hour >= 0 && hour < 6) {
            //         detectedAnomalies.add(createAbnormalTimeAnomaly(event, "凌晨打卡"));
            //     } else if (hour >= 22) {
            //         detectedAnomalies.add(createAbnormalTimeAnomaly(event, "深夜打卡"));
            //     }
            // }

            log.trace("[异常检测] 异常时间打卡检测完成");

        } catch (Exception e) {
            log.error("[异常检测] 异常时间打卡检测失败", e);
        }
    }

    /**
     * 检测连续缺勤异常
     * <p>
     * P0级核心异常检测：员工连续多天未打卡（如连续3天缺勤）
     * </p>
     */
    private void detectContinuousAbsenceAnomalies(TimeRange timeRange, AnomalyFilterParameters filterParameters,
            List<Object> detectedAnomalies) {
        log.debug("[异常检测] 检测连续缺勤异常");

        try {
            // TODO: 从数据库或缓存中查询指定时间范围内的打卡记录
            // 这里实现检测逻辑：
            // 1. 获取所有员工列表
            // 2. 对每个员工，检查是否连续N天未打卡
            // 3. 如果是，生成异常记录

            // 示例实现（伪代码）：
            // List<Long> allEmployeeIds = getAllActiveEmployees();
            // for (Long employeeId : allEmployeeIds) {
            //     List<LocalDate> punchDates = getEmployeePunchDates(employeeId, timeRange);
            //     int continuousDays = 0;
            //     LocalDate currentDate = timeRange.getWorkStartTime().toLocalDate();
            //     LocalDate endDate = timeRange.getWorkEndTime().toLocalDate();
            //     while (!currentDate.isAfter(endDate)) {
            //         if (!punchDates.contains(currentDate)) {
            //             continuousDays++;
            //             if (continuousDays >= 3) {
            //                 detectedAnomalies.add(createContinuousAbsenceAnomaly(employeeId,
            //                         currentDate, continuousDays));
            //                 break;
            //             }
            //         } else {
            //             continuousDays = 0;
            //         }
            //         currentDate = currentDate.plusDays(1);
            //     }
            // }

            log.trace("[异常检测] 连续缺勤异常检测完成");

        } catch (Exception e) {
            log.error("[异常检测] 连续缺勤异常检测失败", e);
        }
    }

    /**
     * 检测打卡地点异常
     * <p>
     * P1级异常检测：打卡地点异常（如：在非允许地点打卡、地点变化过快）
     * </p>
     */
    private void detectAbnormalLocationAnomalies(TimeRange timeRange, AnomalyFilterParameters filterParameters,
            List<Object> detectedAnomalies) {
        log.debug("[异常检测] 检测打卡地点异常");

        try {
            // TODO: 从数据库或缓存中查询指定时间范围内的打卡记录
            // 这里实现检测逻辑：
            // 1. 查询所有包含位置信息的打卡记录
            // 2. 检测打卡地点是否在允许的考勤范围内
            // 3. 检测两次打卡间的时间与距离是否合理（移动速度是否过快）

            // 示例实现（伪代码）：
            // List<AttendanceEvent> events = queryEventsWithLocation(timeRange);
            // for (AttendanceEvent event : events) {
            //     // 检测是否在允许范围内
            //     if (!isLocationAllowed(event.getLatitude(), event.getLongitude())) {
            //         detectedAnomalies.add(createLocationAnomaly(event, "地点不在允许范围内"));
            //     }
            // }

            log.trace("[异常检测] 打卡地点异常检测完成");

        } catch (Exception e) {
            log.error("[异常检测] 打卡地点异常检测失败", e);
        }
    }

    /**
     * 检测早退迟到异常
     * <p>
     * P0级核心异常检测：检测员工早退、迟到情况
     * </p>
     */
    private void detectEarlyLeaveLateArrivalAnomalies(TimeRange timeRange, AnomalyFilterParameters filterParameters,
            List<Object> detectedAnomalies) {
        log.debug("[异常检测] 检测早退迟到异常");

        try {
            // TODO: 从数据库或缓存中查询指定时间范围内的打卡记录
            // 这里实现检测逻辑：
            // 1. 获取员工排班信息（班次开始时间、结束时间）
            // 2. 对比实际打卡时间与排班时间
            // 3. 如果迟到/早退超过阈值（如迟到超过30分钟），生成异常记录

            // 示例实现（伪代码）：
            // List<AttendanceEvent> events = queryEvents(timeRange);
            // for (AttendanceEvent event : events) {
            //     WorkShiftEntity shift = getEmployeeShift(event.getEmployeeId(), event.getEventTime());
            //     if (shift == null) {
            //         continue; // 无排班信息，跳过
            //     }
            //     if (event.getEventType() == AttendanceEvent.EventType.CLOCK_IN) {
            //         // 上班打卡，检测是否迟到
            //         if (event.getEventTime().isAfter(shift.getWorkStartTime().plusMinutes(30))) {
            //             detectedAnomalies.add(createLateArrivalAnomaly(event, shift));
            //         }
            //     } else if (event.getEventType() == AttendanceEvent.EventType.CLOCK_OUT) {
            //         // 下班打卡，检测是否早退
            //         if (event.getEventTime().isBefore(shift.getWorkEndTime().minusMinutes(30))) {
            //             detectedAnomalies.add(createEarlyLeaveAnomaly(event, shift));
            //         }
            //     }
            // }

            log.trace("[异常检测] 早退迟到异常检测完成");

        } catch (Exception e) {
            log.error("[异常检测] 早退迟到异常检测失败", e);
        }
    }

    // ==================== 辅助创建方法（占位实现）====================

    /**
     * 创建频繁打卡异常记录
     */
    private Map<String, Object> createFrequentPunchAnomaly(Long employeeId, AttendanceEvent event, int count) {
        Map<String, Object> anomaly = new HashMap<>();
        anomaly.put("anomalyId", UUID.randomUUID().toString());
        anomaly.put("anomalyType", "FREQUENT_PUNCH");
        anomaly.put("employeeId", employeeId);
        anomaly.put("eventId", event.getEventId());
        anomaly.put("eventTime", event.getEventTime());
        anomaly.put("frequentCount", count);
        anomaly.put("description", String.format("10分钟内打卡%d次", count));
        anomaly.put("severity", count >= 5 ? "HIGH" : "MEDIUM");
        return anomaly;
    }

    /**
     * 创建跨设备打卡异常记录
     */
    private Map<String, Object> createCrossDeviceAnomaly(Long employeeId, AttendanceEvent event1,
            AttendanceEvent event2, double distance) {
        Map<String, Object> anomaly = new HashMap<>();
        anomaly.put("anomalyId", UUID.randomUUID().toString());
        anomaly.put("anomalyType", "CROSS_DEVICE");
        anomaly.put("employeeId", employeeId);
        anomaly.put("eventId1", event1.getEventId());
        anomaly.put("eventId2", event2.getEventId());
        anomaly.put("device1", event1.getDeviceId());
        anomaly.put("device2", event2.getDeviceId());
        anomaly.put("distance", String.format("%.2f公里", distance));
        anomaly.put("description", String.format("30分钟内在%.2f公里外的设备上打卡", distance));
        anomaly.put("severity", distance >= 10 ? "HIGH" : "MEDIUM");
        return anomaly;
    }

    /**
     * 创建异常时间打卡记录
     */
    private Map<String, Object> createAbnormalTimeAnomaly(AttendanceEvent event, String timeDescription) {
        Map<String, Object> anomaly = new HashMap<>();
        anomaly.put("anomalyId", UUID.randomUUID().toString());
        anomaly.put("anomalyType", "ABNORMAL_TIME");
        anomaly.put("employeeId", event.getEmployeeId());
        anomaly.put("eventId", event.getEventId());
        anomaly.put("eventTime", event.getEventTime());
        anomaly.put("timeDescription", timeDescription);
        anomaly.put("description", timeDescription + "打卡");
        anomaly.put("severity", "MEDIUM");
        return anomaly;
    }

    /**
     * 创建连续缺勤异常记录
     */
    private Map<String, Object> createContinuousAbsenceAnomaly(Long employeeId, LocalDate date, int days) {
        Map<String, Object> anomaly = new HashMap<>();
        anomaly.put("anomalyId", UUID.randomUUID().toString());
        anomaly.put("anomalyType", "CONTINUOUS_ABSENCE");
        anomaly.put("employeeId", employeeId);
        anomaly.put("absenceDate", date);
        anomaly.put("continuousDays", days);
        anomaly.put("description", String.format("连续%d天未打卡", days));
        anomaly.put("severity", days >= 7 ? "HIGH" : "MEDIUM");
        return anomaly;
    }

    /**
     * 创建地点异常记录
     */
    private Map<String, Object> createLocationAnomaly(AttendanceEvent event, String locationDescription) {
        Map<String, Object> anomaly = new HashMap<>();
        anomaly.put("anomalyId", UUID.randomUUID().toString());
        anomaly.put("anomalyType", "ABNORMAL_LOCATION");
        anomaly.put("employeeId", event.getEmployeeId());
        anomaly.put("eventId", event.getEventId());
        anomaly.put("eventTime", event.getEventTime());
        anomaly.put("locationDescription", locationDescription);
        anomaly.put("description", locationDescription);
        anomaly.put("severity", "MEDIUM");
        return anomaly;
    }

    /**
     * 创建迟到异常记录
     */
    private Map<String, Object> createLateArrivalAnomaly(AttendanceEvent event, Object shiftInfo) {
        Map<String, Object> anomaly = new HashMap<>();
        anomaly.put("anomalyId", UUID.randomUUID().toString());
        anomaly.put("anomalyType", "LATE_ARRIVAL");
        anomaly.put("employeeId", event.getEmployeeId());
        anomaly.put("eventId", event.getEventId());
        anomaly.put("eventTime", event.getEventTime());
        anomaly.put("description", "迟到超过30分钟");
        anomaly.put("severity", "MEDIUM");
        return anomaly;
    }

    /**
     * 创建早退异常记录
     */
    private Map<String, Object> createEarlyLeaveAnomaly(AttendanceEvent event, Object shiftInfo) {
        Map<String, Object> anomaly = new HashMap<>();
        anomaly.put("anomalyId", UUID.randomUUID().toString());
        anomaly.put("anomalyType", "EARLY_LEAVE");
        anomaly.put("employeeId", event.getEmployeeId());
        anomaly.put("eventId", event.getEventId());
        anomaly.put("eventTime", event.getEventTime());
        anomaly.put("description", "早退超过30分钟");
        anomaly.put("severity", "MEDIUM");
        return anomaly;
    }
}
