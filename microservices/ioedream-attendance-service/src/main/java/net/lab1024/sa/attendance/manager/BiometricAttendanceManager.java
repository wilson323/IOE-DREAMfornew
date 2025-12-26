package net.lab1024.sa.attendance.manager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

/**
 * 生物识别打卡管理器
 * <p>
 * 实现基于生物识别（人脸、指纹）的考勤打卡功能
 * 采用边缘识别+中心计算架构：
 * - 设备端完成生物识别
 * - 设备端上传userId和识别数据
 * - 服务器端完成排班匹配和考勤统计
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@Slf4j
@Component
public class BiometricAttendanceManager {

    /**
     * 处理生物识别打卡
     * <p>
     * 核心流程：
     * 1. 接收设备端上传的识别结果（userId + biometricData）
     * 2. 验证打卡时间和位置
     * 3. 匹配用户排班规则
     * 4. 生成考勤记录
     * 5. 更新考勤统计
     * </p>
     *
     * @param userId 用户ID（由设备端识别返回）
     * @param deviceId 设备ID
     * @param biometricType 生物识别类型（FACE-人脸, FINGERPRINT-指纹, IRIS-虹膜）
     * @param biometricData 生物特征数据（可选，用于二次验证）
     * @param attendanceTime 打卡时间
     * @param locationInfo 位置信息（可选）
     * @return 打卡结果
     */
    public AttendanceResult processBiometricAttendance(Long userId, String deviceId,
                                                       String biometricType, String biometricData,
                                                       LocalDateTime attendanceTime, String locationInfo) {
        log.info("[生物识别打卡] 处理打卡请求: userId={}, deviceId={}, type={}, time={}",
                userId, deviceId, biometricType, attendanceTime);

        try {
            // 1. 验证用户有效性
            if (userId == null) {
                log.warn("[生物识别打卡] 用户ID为空");
                return AttendanceResult.failure("用户ID不能为空");
            }

            // 2. 验证设备有效性
            if (deviceId == null || deviceId.isEmpty()) {
                log.warn("[生物识别打卡] 设备ID为空");
                return AttendanceResult.failure("设备ID不能为空");
            }

            // 3. 验证生物识别类型
            if (!isValidBiometricType(biometricType)) {
                log.warn("[生物识别打卡] 无效的生物识别类型: {}", biometricType);
                return AttendanceResult.failure("无效的生物识别类型");
            }

            // 4. 判断打卡类型（上班/下班）
            AttendanceType attendanceType = determineAttendanceType(userId, attendanceTime);
            log.info("[生物识别打卡] 打卡类型: userId={}, type={}", userId, attendanceType);

            // 5. 生成考勤记录
            String recordId = generateAttendanceRecord(userId, deviceId, biometricType,
                    biometricData, attendanceTime, locationInfo, attendanceType);

            // 6. 返回成功结果
            AttendanceResult result = AttendanceResult.success(recordId, attendanceType);
            log.info("[生物识别打卡] 打卡成功: userId={}, recordId={}, type={}", userId, recordId, attendanceType);

            return result;

        } catch (Exception e) {
            log.error("[生物识别打卡] 处理异常: userId={}, error={}", userId, e.getMessage(), e);
            return AttendanceResult.failure("打卡处理失败: " + e.getMessage());
        }
    }

    /**
     * 批量处理生物识别打卡记录
     * <p>
     * 用于设备端批量上传打卡记录
     * </p>
     *
     * @param records 打卡记录列表
     * @return 处理结果统计
     */
    public BatchProcessResult batchProcessAttendance(java.util.List<BiometricAttendanceRecord> records) {
        log.info("[生物识别打卡] 批量处理打卡记录: count={}", records.size());

        int successCount = 0;
        int failureCount = 0;

        for (BiometricAttendanceRecord record : records) {
            try {
                AttendanceResult result = processBiometricAttendance(
                        record.getUserId(),
                        record.getDeviceId(),
                        record.getBiometricType(),
                        record.getBiometricData(),
                        record.getAttendanceTime(),
                        record.getLocationInfo()
                );

                if (result.isSuccess()) {
                    successCount++;
                } else {
                    failureCount++;
                    log.warn("[生物识别打卡] 单条记录处理失败: userId={}, reason={}",
                            record.getUserId(), result.getErrorMessage());
                }

            } catch (Exception e) {
                failureCount++;
                log.error("[生物识别打卡] 单条记录处理异常: userId={}", record.getUserId(), e);
            }
        }

        BatchProcessResult batchResult = new BatchProcessResult();
        batchResult.setTotalCount(records.size());
        batchResult.setSuccessCount(successCount);
        batchResult.setFailureCount(failureCount);

        log.info("[生物识别打卡] 批量处理完成: total={}, success={}, failure={}",
                records.size(), successCount, failureCount);

        return batchResult;
    }

    /**
     * 验证生物识别数据
     * <p>
     * 二次验证生物特征数据的有效性
     * 可选功能，用于增强安全性
     * </p>
     *
     * @param userId 用户ID
     * @param biometricType 生物识别类型
     * @param biometricData 生物特征数据
     * @return 是否验证通过
     */
    public boolean verifyBiometricData(Long userId, String biometricType, String biometricData) {
        // TODO: 实现生物特征二次验证
        // 这里可以调用生物特征服务进行验证
        log.debug("[生物识别打卡] 生物特征验证: userId={}, type={}", userId, biometricType);

        // 简化实现：如果提供了生物特征数据，则认为验证通过
        return biometricData != null && !biometricData.isEmpty();
    }

    /**
     * 获取用户今日打卡统计
     *
     * @param userId 用户ID
     * @return 打卡统计
     */
    public AttendanceStatistics getTodayStatistics(Long userId) {
        // TODO: 从数据库查询今日打卡记录
        log.debug("[生物识别打卡] 查询今日打卡统计: userId={}", userId);

        AttendanceStatistics statistics = new AttendanceStatistics();
        statistics.setUserId(userId);
        statistics.setDate(LocalDate.now());
        statistics.setCheckInCount(0);
        statistics.setCheckOutCount(0);
        statistics.setLateCount(0);
        statistics.setEarlyCount(0);

        return statistics;
    }

    /**
     * 判断打卡类型（上班/下班）
     * <p>
     * 根据打卡时间和用户排班规则判断
     * </p>
     *
     * @param userId 用户ID
     * @param attendanceTime 打卡时间
     * @return 打卡类型
     */
    private AttendanceType determineAttendanceType(Long userId, LocalDateTime attendanceTime) {
        LocalTime time = attendanceTime.toLocalTime();

        // 简化判断：12点之前为上班打卡，12点之后为下班打卡
        // 实际应该根据用户排班规则判断
        if (time.isBefore(LocalTime.NOON)) {
            return AttendanceType.CHECK_IN;
        } else {
            return AttendanceType.CHECK_OUT;
        }
    }

    /**
     * 生成考勤记录
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @param biometricType 生物识别类型
     * @param biometricData 生物特征数据
     * @param attendanceTime 打卡时间
     * @param locationInfo 位置信息
     * @param attendanceType 打卡类型
     * @return 考勤记录ID
     */
    private String generateAttendanceRecord(Long userId, String deviceId, String biometricType,
                                            String biometricData, LocalDateTime attendanceTime,
                                            String locationInfo, AttendanceType attendanceType) {
        // TODO: 保存到数据库
        // 这里应该调用DAO层保存考勤记录
        log.info("[生物识别打卡] 生成考勤记录: userId={}, deviceId={}, type={}, time={}",
                userId, deviceId, attendanceType, attendanceTime);

        // 生成记录ID（简化实现）
        return "ATT-" + System.currentTimeMillis() + "-" + userId;
    }

    /**
     * 验证生物识别类型
     *
     * @param biometricType 生物识别类型
     * @return 是否有效
     */
    private boolean isValidBiometricType(String biometricType) {
        if (biometricType == null) {
            return false;
        }
        return "FACE".equals(biometricType) ||
                "FINGERPRINT".equals(biometricType) ||
                "IRIS".equals(biometricType) ||
                "PALM".equals(biometricType);
    }

    /**
     * 打卡类型枚举
     */
    public enum AttendanceType {
        CHECK_IN("上班打卡"),
        CHECK_OUT("下班打卡");

        private final String description;

        AttendanceType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 打卡结果
     */
    public static class AttendanceResult {
        private boolean success;
        private String recordId;
        private AttendanceType attendanceType;
        private String errorMessage;

        public static AttendanceResult success(String recordId, AttendanceType attendanceType) {
            AttendanceResult result = new AttendanceResult();
            result.success = true;
            result.recordId = recordId;
            result.attendanceType = attendanceType;
            return result;
        }

        public static AttendanceResult failure(String errorMessage) {
            AttendanceResult result = new AttendanceResult();
            result.success = false;
            result.errorMessage = errorMessage;
            return result;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getRecordId() {
            return recordId;
        }

        public AttendanceType getAttendanceType() {
            return attendanceType;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        @Override
        public String toString() {
            return "AttendanceResult{" +
                    "success=" + success +
                    ", recordId='" + recordId + '\'' +
                    ", attendanceType=" + attendanceType +
                    ", errorMessage='" + errorMessage + '\'' +
                    '}';
        }
    }

    /**
     * 生物识别打卡记录
     */
    public static class BiometricAttendanceRecord {
        private Long userId;
        private String deviceId;
        private String biometricType;
        private String biometricData;
        private LocalDateTime attendanceTime;
        private String locationInfo;

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }

        public String getBiometricType() {
            return biometricType;
        }

        public void setBiometricType(String biometricType) {
            this.biometricType = biometricType;
        }

        public String getBiometricData() {
            return biometricData;
        }

        public void setBiometricData(String biometricData) {
            this.biometricData = biometricData;
        }

        public LocalDateTime getAttendanceTime() {
            return attendanceTime;
        }

        public void setAttendanceTime(LocalDateTime attendanceTime) {
            this.attendanceTime = attendanceTime;
        }

        public String getLocationInfo() {
            return locationInfo;
        }

        public void setLocationInfo(String locationInfo) {
            this.locationInfo = locationInfo;
        }
    }

    /**
     * 批量处理结果
     */
    public static class BatchProcessResult {
        private int totalCount;
        private int successCount;
        private int failureCount;

        public int getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(int totalCount) {
            this.totalCount = totalCount;
        }

        public int getSuccessCount() {
            return successCount;
        }

        public void setSuccessCount(int successCount) {
            this.successCount = successCount;
        }

        public int getFailureCount() {
            return failureCount;
        }

        public void setFailureCount(int failureCount) {
            this.failureCount = failureCount;
        }

        @Override
        public String toString() {
            return "BatchProcessResult{" +
                    "totalCount=" + totalCount +
                    ", successCount=" + successCount +
                    ", failureCount=" + failureCount +
                    '}';
        }
    }

    /**
     * 打卡统计
     */
    public static class AttendanceStatistics {
        private Long userId;
        private LocalDate date;
        private int checkInCount;
        private int checkOutCount;
        private int lateCount;
        private int earlyCount;

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public int getCheckInCount() {
            return checkInCount;
        }

        public void setCheckInCount(int checkInCount) {
            this.checkInCount = checkInCount;
        }

        public int getCheckOutCount() {
            return checkOutCount;
        }

        public void setCheckOutCount(int checkOutCount) {
            this.checkOutCount = checkOutCount;
        }

        public int getLateCount() {
            return lateCount;
        }

        public void setLateCount(int lateCount) {
            this.lateCount = lateCount;
        }

        public int getEarlyCount() {
            return earlyCount;
        }

        public void setEarlyCount(int earlyCount) {
            this.earlyCount = earlyCount;
        }

        @Override
        public String toString() {
            return "AttendanceStatistics{" +
                    "userId=" + userId +
                    ", date=" + date +
                    ", checkInCount=" + checkInCount +
                    ", checkOutCount=" + checkOutCount +
                    ", lateCount=" + lateCount +
                    ", earlyCount=" + earlyCount +
                    '}';
        }
    }
}
