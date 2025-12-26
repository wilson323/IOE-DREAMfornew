package net.lab1024.sa.attendance.processor;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.dao.AttendanceRecordDao;
import net.lab1024.sa.common.entity.attendance.AttendanceRecordEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 多设备打卡冲突处理器
 * <p>
 * 严格遵循四层架构规范（Controller→Service→Manager→DAO）
 * 负责检测和处理同一用户在短时间内使用多个设备打卡的情况
 * </p>
 * <p>
 * 核心职责：
 * - 检测多设备打卡冲突
 * - 智能选择有效的打卡记录
 * - 标记冲突记录
 * - 提供冲突解决策略
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@Component
@Slf4j
public class MultiDeviceConflictProcessor {

    private final AttendanceRecordDao attendanceRecordDao;

    // 冲突检测时间窗口（分钟）
    private static final int CONFLICT_WINDOW_MINUTES = 5;

    // 冲突记录缓存
    private final Map<String, ConflictRecord> conflictCache = new ConcurrentHashMap<>();

    /**
     * 构造函数注入依赖
     */
    public MultiDeviceConflictProcessor(AttendanceRecordDao attendanceRecordDao) {
        this.attendanceRecordDao = attendanceRecordDao;
        log.info("[冲突处理] 多设备打卡冲突处理器初始化完成");
    }

    /**
     * 检测并处理打卡冲突
     *
     * @param newRecord 新打卡记录
     * @return 处理结果
     */
    public ConflictResolutionResult resolveConflict(AttendanceRecordEntity newRecord) {
        if (newRecord == null) {
            return ConflictResolutionResult.failure("打卡记录为空");
        }

        log.info("[冲突处理] 开始检测打卡冲突: userId={}, date={}, device={}",
                newRecord.getUserId(), newRecord.getAttendanceDate(), newRecord.getDeviceId());

        try {
            // 1. 查询时间窗口内的相关打卡记录
            LocalDateTime windowStart = newRecord.getPunchTime().minusMinutes(CONFLICT_WINDOW_MINUTES);
            LocalDateTime windowEnd = newRecord.getPunchTime().plusMinutes(CONFLICT_WINDOW_MINUTES);

            LambdaQueryWrapper<AttendanceRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AttendanceRecordEntity::getUserId, newRecord.getUserId())
                    .eq(AttendanceRecordEntity::getAttendanceDate, newRecord.getAttendanceDate())
                    .ge(AttendanceRecordEntity::getPunchTime, windowStart)
                    .le(AttendanceRecordEntity::getPunchTime, windowEnd)
                    .eq(AttendanceRecordEntity::getDeletedFlag, 0)
                    .orderByAsc(AttendanceRecordEntity::getPunchTime);

            List<AttendanceRecordEntity> conflictingRecords = attendanceRecordDao.selectList(queryWrapper);

            // 2. 过滤出使用不同设备的记录
            List<AttendanceRecordEntity> multiDeviceRecords = conflictingRecords.stream()
                    .filter(r -> !r.getDeviceId().equals(newRecord.getDeviceId()))
                    .collect(Collectors.toList());

            if (multiDeviceRecords.isEmpty()) {
                log.debug("[冲突处理] 无冲突: userId={}, date={}",
                        newRecord.getUserId(), newRecord.getAttendanceDate());
                return ConflictResolutionResult.noConflict();
            }

            log.warn("[冲突处理] 检测到多设备打卡冲突: userId={}, date={}, deviceCount={}",
                    newRecord.getUserId(), newRecord.getAttendanceDate(),
                    new HashSet<>(multiDeviceRecords.stream().map(AttendanceRecordEntity::getDeviceId).collect(Collectors.toList())).size() + 1);

            // 3. 应用冲突解决策略
            ConflictResolutionStrategy strategy = determineStrategy(newRecord, multiDeviceRecords);
            ConflictResolutionResult result = applyStrategy(strategy, newRecord, multiDeviceRecords);

            // 4. 记录冲突
            recordConflict(newRecord, multiDeviceRecords, result);

            return result;

        } catch (Exception e) {
            log.error("[冲突处理] 处理打卡冲突失败: userId={}", newRecord.getUserId(), e);
            return ConflictResolutionResult.failure("处理冲突异常: " + e.getMessage());
        }
    }

    /**
     * 批量检测并处理打卡冲突
     *
     * @param records 打卡记录列表
     * @return 冲突处理结果列表
     */
    public List<ConflictResolutionResult> batchResolveConflicts(List<AttendanceRecordEntity> records) {
        log.info("[冲突处理] 开始批量检测打卡冲突: recordCount={}", records.size());

        List<ConflictResolutionResult> results = new ArrayList<>();

        int conflictCount = 0;
        int noConflictCount = 0;

        for (AttendanceRecordEntity record : records) {
            ConflictResolutionResult result = resolveConflict(record);
            results.add(result);

            if (result.hasConflict()) {
                conflictCount++;
            } else {
                noConflictCount++;
            }
        }

        log.info("[冲突处理] 批量检测完成: total={}, conflict={}, noConflict={}",
                records.size(), conflictCount, noConflictCount);

        return results;
    }

    /**
     * 确定冲突解决策略
     */
    private ConflictResolutionStrategy determineStrategy(AttendanceRecordEntity newRecord,
                                                          List<AttendanceRecordEntity> existingRecords) {
        // 策略1: 保留第一个打卡记录
        // 策略2: 保留最后一个打卡记录
        // 策略3: 保留主设备的打卡记录
        // 策略4: 根据时间顺序选择

        // 检查是否有主设备打卡
        boolean hasPrimaryDevice = existingRecords.stream()
                .anyMatch(r -> isPrimaryDevice(r.getDeviceId()));

        if (hasPrimaryDevice) {
            return ConflictResolutionStrategy.KEEP_PRIMARY_DEVICE;
        }

        // 检查新记录是否来自主设备
        if (isPrimaryDevice(newRecord.getDeviceId())) {
            return ConflictResolutionStrategy.KEEP_NEW_PRIMARY;
        }

        // 默认策略：保留最早的打卡记录
        return ConflictResolutionStrategy.KEEP_FIRST_PUNCH;
    }

    /**
     * 应用冲突解决策略
     */
    private ConflictResolutionResult applyStrategy(ConflictResolutionStrategy strategy,
                                                      AttendanceRecordEntity newRecord,
                                                      List<AttendanceRecordEntity> existingRecords) {
        ConflictResolutionResult result = new ConflictResolutionResult();
        result.setHasConflict(true);
        result.setStrategy(strategy);

        switch (strategy) {
            case KEEP_FIRST_PUNCH:
                // 保留第一个打卡记录，标记其他为冲突
                AttendanceRecordEntity firstRecord = existingRecords.stream()
                        .min(Comparator.comparing(AttendanceRecordEntity::getPunchTime))
                        .orElse(newRecord);

                result.setKeepRecordId(firstRecord.getRecordId());
                result.setConflictRecordIds(existingRecords.stream()
                        .map(AttendanceRecordEntity::getRecordId)
                        .collect(Collectors.toList()));
                result.setDescription("保留最早打卡记录");

                break;

            case KEEP_PRIMARY_DEVICE:
                // 保留主设备打卡记录
                AttendanceRecordEntity primaryRecord = existingRecords.stream()
                        .filter(r -> isPrimaryDevice(r.getDeviceId()))
                        .findFirst()
                        .orElse(existingRecords.get(0));

                result.setKeepRecordId(primaryRecord.getRecordId());
                result.setConflictRecordIds(existingRecords.stream()
                        .filter(r -> !r.getRecordId().equals(primaryRecord.getRecordId()))
                        .map(AttendanceRecordEntity::getRecordId)
                        .collect(Collectors.toList()));
                result.setDescription("保留主设备打卡记录");

                break;

            case KEEP_NEW_PRIMARY:
                // 新记录是主设备，保留新记录，标记其他为冲突
                result.setKeepRecordId(newRecord.getRecordId());
                result.setConflictRecordIds(existingRecords.stream()
                        .map(AttendanceRecordEntity::getRecordId)
                        .collect(Collectors.toList()));
                result.setDescription("新记录为主设备，保留新记录");

                break;

            default:
                // 默认保留第一个
                AttendanceRecordEntity defaultFirst = existingRecords.stream()
                        .min(Comparator.comparing(AttendanceRecordEntity::getPunchTime))
                        .orElse(newRecord);

                result.setKeepRecordId(defaultFirst.getRecordId());
                result.setConflictRecordIds(existingRecords.stream()
                        .map(AttendanceRecordEntity::getRecordId)
                        .collect(Collectors.toList()));
                result.setDescription("保留最早打卡记录（默认）");
        }

        log.info("[冲突处理] 应用冲突解决策略: strategy={}, keep={}, conflicts={}",
                strategy, result.getKeepRecordId(), result.getConflictRecordIds().size());

        return result;
    }

    /**
     * 判断是否为主设备
     */
    private boolean isPrimaryDevice(Long deviceId) {
        // 简单实现：假设设备ID较小的为主设备
        // 实际应该根据设备类型、优先级等判断
        return deviceId != null && deviceId < 1000;
    }

    /**
     * 记录冲突
     */
    private void recordConflict(AttendanceRecordEntity newRecord, List<AttendanceRecordEntity> conflictingRecords,
                                ConflictResolutionResult result) {
        String conflictKey = generateConflictKey(newRecord.getUserId(), newRecord.getAttendanceDate());

        ConflictRecord record = new ConflictRecord();
        record.setUserId(newRecord.getUserId());
        record.setAttendanceDate(newRecord.getAttendanceDate());
        record.setConflictingDevices(collectDeviceIds(newRecord, conflictingRecords));
        record.setResolutionStrategy(result.getStrategy());
        record.setKeepRecordId(result.getKeepRecordId());
        record.setConflictRecordIds(result.getConflictRecordIds());
        record.setDetectionTime(LocalDateTime.now());

        conflictCache.put(conflictKey, record);

        log.warn("[冲突处理] 记录打卡冲突: userId={}, date={}, devices={}",
                newRecord.getUserId(), newRecord.getAttendanceDate(), record.getConflictingDevices());
    }

    /**
     * 收集设备ID列表
     */
    private Set<Long> collectDeviceIds(AttendanceRecordEntity newRecord, List<AttendanceRecordEntity> existingRecords) {
        Set<Long> deviceIds = new HashSet<>();
        deviceIds.add(newRecord.getDeviceId());
        existingRecords.forEach(r -> deviceIds.add(r.getDeviceId()));
        return deviceIds;
    }

    /**
     * 生成冲突键
     */
    private String generateConflictKey(Long userId, LocalDate date) {
        return String.format("CONFLICT:%d:%s", userId, date);
    }

    /**
     * 获取冲突记录
     */
    public ConflictRecord getConflictRecord(Long userId, LocalDate date) {
        String conflictKey = generateConflictKey(userId, date);
        return conflictCache.get(conflictKey);
    }

    /**
     * 获取所有冲突记录
     */
    public Collection<ConflictRecord> getAllConflictRecords() {
        return conflictCache.values();
    }

    /**
     * 清除冲突缓存
     */
    public void clearConflictCache() {
        log.info("[冲突处理] 清除冲突缓存");
        conflictCache.clear();
    }

    // ==================== 内部类 ====================

    /**
     * 冲突解决策略枚举
     */
    public enum ConflictResolutionStrategy {
        KEEP_FIRST_PUNCH("保留最早打卡"),
        KEEP_LAST_PUNCH("保留最晚打卡"),
        KEEP_PRIMARY_DEVICE("保留主设备打卡"),
        KEEP_NEW_PRIMARY("保留新的主设备打卡");

        private final String description;

        ConflictResolutionStrategy(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 冲突解决结果
     */
    public static class ConflictResolutionResult {
        private boolean hasConflict;
        private ConflictResolutionStrategy strategy;
        private Long keepRecordId;
        private List<Long> conflictRecordIds;
        private String description;

        public static ConflictResolutionResult noConflict() {
            ConflictResolutionResult result = new ConflictResolutionResult();
            result.setHasConflict(false);
            result.setDescription("无冲突");
            return result;
        }

        public static ConflictResolutionResult failure(String errorMessage) {
            ConflictResolutionResult result = new ConflictResolutionResult();
            result.setHasConflict(false);
            result.setDescription("处理失败: " + errorMessage);
            return result;
        }

        // Getters and Setters
        public boolean hasConflict() {
            return hasConflict;
        }

        public void setHasConflict(boolean hasConflict) {
            this.hasConflict = hasConflict;
        }

        public ConflictResolutionStrategy getStrategy() {
            return strategy;
        }

        public void setStrategy(ConflictResolutionStrategy strategy) {
            this.strategy = strategy;
        }

        public Long getKeepRecordId() {
            return keepRecordId;
        }

        public void setKeepRecordId(Long keepRecordId) {
            this.keepRecordId = keepRecordId;
        }

        public List<Long> getConflictRecordIds() {
            return conflictRecordIds;
        }

        public void setConflictRecordIds(List<Long> conflictRecordIds) {
            this.conflictRecordIds = conflictRecordIds;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    /**
     * 冲突记录
     */
    public static class ConflictRecord {
        private Long userId;
        private LocalDate attendanceDate;
        private Set<Long> conflictingDevices;
        private ConflictResolutionStrategy resolutionStrategy;
        private Long keepRecordId;
        private List<Long> conflictRecordIds;
        private LocalDateTime detectionTime;

        // Getters and Setters
        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public LocalDate getAttendanceDate() {
            return attendanceDate;
        }

        public void setAttendanceDate(LocalDate attendanceDate) {
            this.attendanceDate = attendanceDate;
        }

        public Set<Long> getConflictingDevices() {
            return conflictingDevices;
        }

        public void setConflictingDevices(Set<Long> conflictingDevices) {
            this.conflictingDevices = conflictingDevices;
        }

        public ConflictResolutionStrategy getResolutionStrategy() {
            return resolutionStrategy;
        }

        public void setResolutionStrategy(ConflictResolutionStrategy resolutionStrategy) {
            this.resolutionStrategy = resolutionStrategy;
        }

        public Long getKeepRecordId() {
            return keepRecordId;
        }

        public void setKeepRecordId(Long keepRecordId) {
            this.keepRecordId = keepRecordId;
        }

        public List<Long> getConflictRecordIds() {
            return conflictRecordIds;
        }

        public void setConflictRecordIds(List<Long> conflictRecordIds) {
            this.conflictRecordIds = conflictRecordIds;
        }

        public LocalDateTime getDetectionTime() {
            return detectionTime;
        }

        public void setDetectionTime(LocalDateTime detectionTime) {
            this.detectionTime = detectionTime;
        }
    }
}
