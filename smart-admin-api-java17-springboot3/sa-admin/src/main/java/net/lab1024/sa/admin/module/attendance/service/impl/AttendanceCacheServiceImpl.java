package net.lab1024.sa.admin.module.attendance.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import net.lab1024.sa.admin.module.attendance.domain.entity.AttendanceRecordEntity;
import net.lab1024.sa.admin.module.attendance.domain.entity.AttendanceRuleEntity;
import net.lab1024.sa.admin.module.attendance.domain.entity.AttendanceScheduleEntity;
import net.lab1024.sa.admin.module.attendance.service.AttendanceCacheService;
import net.lab1024.sa.base.common.cache.CacheNamespace;
import net.lab1024.sa.base.common.cache.UnifiedCacheManager;

/**
 * 考勤系统统一缓存服务实现
 * <p>
 * 基于UnifiedCacheManager实现的考勤模块缓存服务
 * 遵循缓存架构统一化规范，使用ATTENDANCE命名空间
 * 缓存键格式：iog:cache:ATTENDANCE:{namespace}:{key}
 * </p>
 *
 * @author SmartAdmin Team
 * @since 2025-11-24
 */
@Slf4j
@Service
public class AttendanceCacheServiceImpl implements AttendanceCacheService {

    @Resource
    private UnifiedCacheManager unifiedCacheManager;

    // 缓存键命名空间定义
    private static final class CacheKey {
        // 考勤记录
        public static final String ATTENDANCE_RECORD = "record";
        public static final String USER_TODAY_ATTENDANCE = "user:today:attendance";
        public static final String DEVICE_TODAY_ATTENDANCE = "device:today:attendance";

        // 考勤规则
        public static final String ATTENDANCE_RULE = "rule:info";
        public static final String DEPARTMENT_RULES = "department:rules";

        // 考勤排班
        public static final String ATTENDANCE_SCHEDULE = "schedule:info";
        public static final String USER_SCHEDULES = "user:schedules";
        public static final String DEVICE_SCHEDULES = "device:schedules";

        // 统计数据
        public static final String ATTENDANCE_STATS = "stats:attendance";
        public static final String USER_STATS = "stats:user";
    }

    // ========== 考勤记录缓存 ==========

    @Override
    public AttendanceRecordEntity getAttendanceRecord(Long recordId) {
        if (recordId == null) {
            return null;
        }

        String key = buildKey(CacheKey.ATTENDANCE_RECORD, recordId.toString());
        UnifiedCacheManager.CacheResult<AttendanceRecordEntity> result =
            unifiedCacheManager.get(CacheNamespace.ATTENDANCE, key, AttendanceRecordEntity.class);

        return result.isSuccess() ? result.getData() : null;
    }

    @Override
    public CompletableFuture<AttendanceRecordEntity> getAttendanceRecordAsync(Long recordId) {
        if (recordId == null) {
            return CompletableFuture.completedFuture(null);
        }

        String key = buildKey(CacheKey.ATTENDANCE_RECORD, recordId.toString());
        return unifiedCacheManager.getAsync(CacheNamespace.ATTENDANCE, key, AttendanceRecordEntity.class)
            .thenApply(result -> result.isSuccess() ? result.getData() : null);
    }

    @Override
    public void cacheAttendanceRecord(AttendanceRecordEntity record) {
        if (record == null || record.getRecordId() == null) {
            return;
        }

        String key = buildKey(CacheKey.ATTENDANCE_RECORD, record.getRecordId().toString());
        unifiedCacheManager.set(CacheNamespace.ATTENDANCE, key, record);

        log.debug("缓存考勤记录: recordId={}", record.getRecordId());
    }

    @Override
    public CompletableFuture<Void> cacheAttendanceRecordAsync(AttendanceRecordEntity record) {
        if (record == null || record.getRecordId() == null) {
            return CompletableFuture.completedFuture(null);
        }

        String key = buildKey(CacheKey.ATTENDANCE_RECORD, record.getRecordId().toString());
        return unifiedCacheManager.setAsync(CacheNamespace.ATTENDANCE, key, record)
            .thenRun(() -> log.debug("异步缓存考勤记录: recordId={}", record.getRecordId()));
    }

    @Override
    public List<AttendanceRecordEntity> getUserTodayAttendance(Long userId, String date) {
        if (userId == null || date == null) {
            return new ArrayList<>();
        }

        String key = buildKey(CacheKey.USER_TODAY_ATTENDANCE, userId.toString(), date);
        UnifiedCacheManager.CacheResult<List<AttendanceRecordEntity>> result =
            unifiedCacheManager.get(CacheNamespace.ATTENDANCE, key, List.class);

        return result.isSuccess() ? result.getData() : new ArrayList<>();
    }

    @Override
    public void cacheUserTodayAttendance(Long userId, String date, List<AttendanceRecordEntity> records) {
        if (userId == null || date == null || records == null) {
            return;
        }

        String key = buildKey(CacheKey.USER_TODAY_ATTENDANCE, userId.toString(), date);
        unifiedCacheManager.set(CacheNamespace.ATTENDANCE, key, records);

        log.debug("缓存用户当日考勤记录: userId={}, date={}, count={}", userId, date, records.size());
    }

    @Override
    public List<AttendanceRecordEntity> getDeviceTodayAttendance(Long deviceId, String date) {
        if (deviceId == null || date == null) {
            return new ArrayList<>();
        }

        String key = buildKey(CacheKey.DEVICE_TODAY_ATTENDANCE, deviceId.toString(), date);
        UnifiedCacheManager.CacheResult<List<AttendanceRecordEntity>> result =
            unifiedCacheManager.get(CacheNamespace.ATTENDANCE, key, List.class);

        return result.isSuccess() ? result.getData() : new ArrayList<>();
    }

    @Override
    public void cacheDeviceTodayAttendance(Long deviceId, String date, List<AttendanceRecordEntity> records) {
        if (deviceId == null || date == null || records == null) {
            return;
        }

        String key = buildKey(CacheKey.DEVICE_TODAY_ATTENDANCE, deviceId.toString(), date);
        unifiedCacheManager.set(CacheNamespace.ATTENDANCE, key, records);

        log.debug("缓存设备当日考勤记录: deviceId={}, date={}, count={}", deviceId, date, records.size());
    }

    // ========== 考勤规则缓存 ==========

    @Override
    public AttendanceRuleEntity getAttendanceRule(Long ruleId) {
        if (ruleId == null) {
            return null;
        }

        String key = buildKey(CacheKey.ATTENDANCE_RULE, ruleId.toString());
        UnifiedCacheManager.CacheResult<AttendanceRuleEntity> result =
            unifiedCacheManager.get(CacheNamespace.ATTENDANCE, key, AttendanceRuleEntity.class);

        return result.isSuccess() ? result.getData() : null;
    }

    @Override
    public void cacheAttendanceRule(AttendanceRuleEntity rule) {
        if (rule == null || rule.getRuleId() == null) {
            return;
        }

        String key = buildKey(CacheKey.ATTENDANCE_RULE, rule.getRuleId().toString());
        unifiedCacheManager.set(CacheNamespace.ATTENDANCE, key, rule);

        log.debug("缓存考勤规则: ruleId={}", rule.getRuleId());
    }

    @Override
    public Map<Long, AttendanceRuleEntity> batchGetAttendanceRules(List<Long> ruleIds) {
        Map<Long, AttendanceRuleEntity> result = new HashMap<>();

        if (CollectionUtils.isEmpty(ruleIds)) {
            return result;
        }

        // 批量获取缓存
        List<String> keys = ruleIds.stream()
            .map(id -> buildKey(CacheKey.ATTENDANCE_RULE, id.toString()))
            .collect(Collectors.toList());

        UnifiedCacheManager.BatchCacheResult<AttendanceRuleEntity> batchResult =
            unifiedCacheManager.mGet(CacheNamespace.ATTENDANCE, keys, AttendanceRuleEntity.class);

        // 整理结果
        for (int i = 0; i < ruleIds.size() && i < batchResult.getResults().size(); i++) {
            Long ruleId = ruleIds.get(i);
            UnifiedCacheManager.CacheResult<AttendanceRuleEntity> cacheResult = batchResult.getResults().get(i);

            if (cacheResult.isSuccess() && cacheResult.getData() != null) {
                result.put(ruleId, cacheResult.getData());
            }
        }

        log.debug("批量获取考勤规则: 总数={}, 命中={}", ruleIds.size(), result.size());
        return result;
    }

    @Override
    public void batchCacheAttendanceRules(List<AttendanceRuleEntity> rules) {
        if (CollectionUtils.isEmpty(rules)) {
            return;
        }

        Map<String, AttendanceRuleEntity> keyValues = new HashMap<>();
        for (AttendanceRuleEntity rule : rules) {
            if (rule != null && rule.getRuleId() != null) {
                String key = buildKey(CacheKey.ATTENDANCE_RULE, rule.getRuleId().toString());
                keyValues.put(key, rule);
            }
        }

        unifiedCacheManager.mSet(CacheNamespace.ATTENDANCE, keyValues);

        log.debug("批量缓存考勤规则: 数量={}", rules.size());
    }

    @Override
    public List<AttendanceRuleEntity> getDepartmentRules(Long departmentId) {
        if (departmentId == null) {
            return new ArrayList<>();
        }

        String key = buildKey(CacheKey.DEPARTMENT_RULES, departmentId.toString());
        UnifiedCacheManager.CacheResult<List<AttendanceRuleEntity>> result =
            unifiedCacheManager.get(CacheNamespace.ATTENDANCE, key, List.class);

        return result.isSuccess() ? result.getData() : new ArrayList<>();
    }

    @Override
    public void cacheDepartmentRules(Long departmentId, List<AttendanceRuleEntity> rules) {
        if (departmentId == null || rules == null) {
            return;
        }

        String key = buildKey(CacheKey.DEPARTMENT_RULES, departmentId.toString());
        unifiedCacheManager.set(CacheNamespace.ATTENDANCE, key, rules);

        log.debug("缓存部门考勤规则: departmentId={}, count={}", departmentId, rules.size());
    }

    // ========== 考勤排班缓存 ==========

    @Override
    public AttendanceScheduleEntity getAttendanceSchedule(Long scheduleId) {
        if (scheduleId == null) {
            return null;
        }

        String key = buildKey(CacheKey.ATTENDANCE_SCHEDULE, scheduleId.toString());
        UnifiedCacheManager.CacheResult<AttendanceScheduleEntity> result =
            unifiedCacheManager.get(CacheNamespace.ATTENDANCE, key, AttendanceScheduleEntity.class);

        return result.isSuccess() ? result.getData() : null;
    }

    @Override
    public void cacheAttendanceSchedule(AttendanceScheduleEntity schedule) {
        if (schedule == null || schedule.getScheduleId() == null) {
            return;
        }

        String key = buildKey(CacheKey.ATTENDANCE_SCHEDULE, schedule.getScheduleId().toString());
        unifiedCacheManager.set(CacheNamespace.ATTENDANCE, key, schedule);

        log.debug("缓存考勤排班: scheduleId={}", schedule.getScheduleId());
    }

    @Override
    public List<AttendanceScheduleEntity> getUserSchedules(Long userId, String startDate, String endDate) {
        if (userId == null || startDate == null || endDate == null) {
            return new ArrayList<>();
        }

        String key = buildKey(CacheKey.USER_SCHEDULES, userId.toString(), startDate, endDate);
        UnifiedCacheManager.CacheResult<List<AttendanceScheduleEntity>> result =
            unifiedCacheManager.get(CacheNamespace.ATTENDANCE, key, List.class);

        return result.isSuccess() ? result.getData() : new ArrayList<>();
    }

    @Override
    public void cacheUserSchedules(Long userId, String startDate, String endDate, List<AttendanceScheduleEntity> schedules) {
        if (userId == null || startDate == null || endDate == null || schedules == null) {
            return;
        }

        String key = buildKey(CacheKey.USER_SCHEDULES, userId.toString(), startDate, endDate);
        unifiedCacheManager.set(CacheNamespace.ATTENDANCE, key, schedules);

        log.debug("缓存用户排班列表: userId={}, startDate={}, endDate={}, count={}",
                userId, startDate, endDate, schedules.size());
    }

    @Override
    public List<AttendanceScheduleEntity> getDeviceSchedules(Long deviceId, String date) {
        if (deviceId == null || date == null) {
            return new ArrayList<>();
        }

        String key = buildKey(CacheKey.DEVICE_SCHEDULES, deviceId.toString(), date);
        UnifiedCacheManager.CacheResult<List<AttendanceScheduleEntity>> result =
            unifiedCacheManager.get(CacheNamespace.ATTENDANCE, key, List.class);

        return result.isSuccess() ? result.getData() : new ArrayList<>();
    }

    @Override
    public void cacheDeviceSchedules(Long deviceId, String date, List<AttendanceScheduleEntity> schedules) {
        if (deviceId == null || date == null || schedules == null) {
            return;
        }

        String key = buildKey(CacheKey.DEVICE_SCHEDULES, deviceId.toString(), date);
        unifiedCacheManager.set(CacheNamespace.ATTENDANCE, key, schedules);

        log.debug("缓存设备排班列表: deviceId={}, date={}, count={}", deviceId, date, schedules.size());
    }

    // ========== 统计数据缓存 ==========

    @Override
    public Map<String, Object> getAttendanceStats(String date, String type) {
        if (date == null || type == null) {
            return new HashMap<>();
        }

        String key = buildKey(CacheKey.ATTENDANCE_STATS, date, type);
        UnifiedCacheManager.CacheResult<Map<String, Object>> result =
            unifiedCacheManager.get(CacheNamespace.ATTENDANCE, key, Map.class);

        return result.isSuccess() ? result.getData() : new HashMap<>();
    }

    @Override
    public void cacheAttendanceStats(String date, String type, Map<String, Object> stats) {
        if (date == null || type == null || stats == null) {
            return;
        }

        String key = buildKey(CacheKey.ATTENDANCE_STATS, date, type);
        unifiedCacheManager.set(CacheNamespace.ATTENDANCE, key, stats);

        log.debug("缓存考勤统计数据: date={}, type={}", date, type);
    }

    @Override
    public Map<String, Object> getUserAttendanceStats(Long userId, String startDate, String endDate) {
        if (userId == null || startDate == null || endDate == null) {
            return new HashMap<>();
        }

        String key = buildKey(CacheKey.USER_STATS, userId.toString(), startDate, endDate);
        UnifiedCacheManager.CacheResult<Map<String, Object>> result =
            unifiedCacheManager.get(CacheNamespace.ATTENDANCE, key, Map.class);

        return result.isSuccess() ? result.getData() : new HashMap<>();
    }

    @Override
    public void cacheUserAttendanceStats(Long userId, String startDate, String endDate, Map<String, Object> stats) {
        if (userId == null || startDate == null || endDate == null || stats == null) {
            return;
        }

        String key = buildKey(CacheKey.USER_STATS, userId.toString(), startDate, endDate);
        unifiedCacheManager.set(CacheNamespace.ATTENDANCE, key, stats);

        log.debug("缓存用户考勤统计: userId={}, startDate={}, endDate={}", userId, startDate, endDate);
    }

    // ========== 缓存管理操作 ==========

    @Override
    public void clearUserCache(Long userId) {
        if (userId == null) {
            return;
        }

        // 清除用户相关的所有缓存
        String userPattern = "*:" + userId.toString() + ":*";
        unifiedCacheManager.deleteByPattern(CacheNamespace.ATTENDANCE, userPattern);

        log.debug("清除用户缓存: userId={}", userId);
    }

    @Override
    public void clearDeviceCache(Long deviceId) {
        if (deviceId == null) {
            return;
        }

        // 清除设备相关的所有缓存
        String devicePattern = "*:" + deviceId.toString() + ":*";
        unifiedCacheManager.deleteByPattern(CacheNamespace.ATTENDANCE, devicePattern);

        log.debug("清除设备缓存: deviceId={}", deviceId);
    }

    @Override
    public void clearRuleCache(Long ruleId) {
        if (ruleId == null) {
            return;
        }

        // 清除规则相关的所有缓存
        String rulePattern = CacheKey.ATTENDANCE_RULE + ":" + ruleId.toString() + ":*";
        unifiedCacheManager.deleteByPattern(CacheNamespace.ATTENDANCE, rulePattern);

        log.debug("清除规则缓存: ruleId={}", ruleId);
    }

    @Override
    public void clearDateCache(String date) {
        if (date == null) {
            return;
        }

        // 清除日期相关的统计缓存
        String statsPattern = CacheKey.ATTENDANCE_STATS + ":" + date + ":*";
        unifiedCacheManager.deleteByPattern(CacheNamespace.ATTENDANCE, statsPattern);

        // 清除日期相关的记录缓存
        String recordPattern = "*:*:" + date;
        unifiedCacheManager.deleteByPattern(CacheNamespace.ATTENDANCE, recordPattern);

        log.debug("清除日期相关缓存: date={}", date);
    }

    @Override
    public void clearScheduleCache(Long scheduleId) {
        if (scheduleId == null) {
            return;
        }

        // 清除排班相关的所有缓存
        String schedulePattern = CacheKey.ATTENDANCE_SCHEDULE + ":" + scheduleId.toString() + ":*";
        unifiedCacheManager.deleteByPattern(CacheNamespace.ATTENDANCE, schedulePattern);

        log.debug("清除排班缓存: scheduleId={}", scheduleId);
    }

    @Override
    public void warmupCache() {
        log.info("开始预热考勤缓存...");

        // 预热逻辑由具体的Manager类实现
        log.info("考勤缓存预热完成");
    }

    @Override
    public Map<String, Object> getCacheStatistics() {
        return unifiedCacheManager.getCacheStatistics(CacheNamespace.ATTENDANCE);
    }

    @Override
    public void clearAllAttendanceCache() {
        unifiedCacheManager.clearNamespace(CacheNamespace.ATTENDANCE);
        log.info("清除ATTENDANCE命名空间下的所有缓存");
    }

    // ========== 私有辅助方法 ==========

    /**
     * 构建缓存键
     *
     * @param parts 键的部分
     * @return 完整的缓存键
     */
    private String buildKey(String... parts) {
        if (parts == null || parts.length == 0) {
            return "";
        }

        return String.join(":", parts);
    }
}