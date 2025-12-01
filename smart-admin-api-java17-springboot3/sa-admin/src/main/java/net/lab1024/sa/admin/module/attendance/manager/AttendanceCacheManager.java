package net.lab1024.sa.admin.module.attendance.manager;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.attendance.domain.entity.AttendanceRecordEntity;
import net.lab1024.sa.admin.module.attendance.domain.entity.AttendanceRuleEntity;
import net.lab1024.sa.admin.module.attendance.domain.entity.AttendanceScheduleEntity;
import net.lab1024.sa.admin.module.attendance.repository.AttendanceRecordRepository;
import net.lab1024.sa.admin.module.attendance.repository.AttendanceRuleRepository;
import net.lab1024.sa.admin.module.attendance.repository.AttendanceScheduleRepository;
import net.lab1024.sa.base.common.cache.RedisUtil;
import net.lab1024.sa.base.common.code.UserErrorCode;
import net.lab1024.sa.base.common.exception.SmartException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 考勤缓存管理器
 *
 * 严格遵循repowiki规范:
 * - 二级缓存架构：L1 Caffeine + L2 Redis
 * - 自动缓存失效和刷新机制
 * - 性能优化和内存管理
 * - 缓存监控和统计
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Component
public class AttendanceCacheManager {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AttendanceCacheManager.class);

    // L1缓存：Caffeine本地缓存
    private final Cache<String, Object> localCache;
    private final Cache<String, AttendanceRecordEntity> attendanceRecordCache;
    private final Cache<String, AttendanceRuleEntity> attendanceRuleCache;
    private final Cache<String, AttendanceScheduleEntity> attendanceScheduleCache;

    // 缓存键前缀
    private static final String CACHE_PREFIX = "attendance:";
    private static final String RECORD_PREFIX = CACHE_PREFIX + "record:";
    private static final String RULE_PREFIX = CACHE_PREFIX + "rule:";
    private static final String SCHEDULE_PREFIX = CACHE_PREFIX + "schedule:";
    private static final String STATS_PREFIX = CACHE_PREFIX + "stats:";

    // 缓存过期时间（秒）
    private static final long CACHE_EXPIRE_SECONDS = 3600; // 1小时
    private static final long SHORT_CACHE_EXPIRE = 300;    // 5分钟
    private static final long LONG_CACHE_EXPIRE = 86400;   // 24小时

    @Resource
    private AttendanceRecordRepository attendanceRecordRepository;

    @Resource
    private AttendanceRuleRepository attendanceRuleRepository;

    @Resource
    private AttendanceScheduleRepository attendanceScheduleRepository;

    @Resource
    private RedisUtil redisUtil;

    public AttendanceCacheManager() {
        // 初始化本地缓存
        this.localCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .recordStats()
                .build();

        this.attendanceRecordCache = Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .recordStats()
                .build();

        this.attendanceRuleCache = Caffeine.newBuilder()
                .maximumSize(200)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .recordStats()
                .build();

        this.attendanceScheduleCache = Caffeine.newBuilder()
                .maximumSize(800)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .recordStats()
                .build();
    }

    /**
     * 获取员工考勤记录（带缓存）
     */
    public Optional<AttendanceRecordEntity> getAttendanceRecord(Long employeeId, LocalDate attendanceDate) {
        String cacheKey = buildRecordKey(employeeId, attendanceDate);

        // 先查L1缓存
        AttendanceRecordEntity record = attendanceRecordCache.getIfPresent(cacheKey);
        if (record != null) {
            log.debug("从L1缓存获取考勤记录：{}", cacheKey);
            return Optional.ofNullable(record);
        }

        // 再查L2缓存
        try {
            Object cachedValue = redisUtil.get(cacheKey);
            if (cachedValue != null) {
                record = (AttendanceRecordEntity) cachedValue;
                attendanceRecordCache.put(cacheKey, record);
                log.debug("从L2缓存获取考勤记录：{}", cacheKey);
                return Optional.ofNullable(record);
            }
        } catch (Exception e) {
            log.warn("从Redis获取考勤记录缓存失败：{}", cacheKey, e);
        }

        // 查询数据库
        Optional<AttendanceRecordEntity> optionalRecord = attendanceRecordRepository.findByEmployeeAndDate(employeeId, attendanceDate);
        if (optionalRecord.isPresent()) {
            putAttendanceRecord(cacheKey, optionalRecord.get());
        }

        return optionalRecord;
    }

    /**
     * 缓存考勤记录
     */
    public void putAttendanceRecord(AttendanceRecordEntity record) {
        if (record == null || record.getEmployeeId() == null || record.getAttendanceDate() == null) {
            return;
        }

        String cacheKey = buildRecordKey(record.getEmployeeId(), record.getAttendanceDate());
        putAttendanceRecord(cacheKey, record);
    }

    /**
     * 移除考勤记录缓存
     */
    public void evictAttendanceRecord(Long employeeId, LocalDate attendanceDate) {
        String cacheKey = buildRecordKey(employeeId, attendanceDate);

        // 移除L1缓存
        attendanceRecordCache.invalidate(cacheKey);

        // 移除L2缓存
        try {
            redisUtil.delete(cacheKey);
        } catch (Exception e) {
            log.warn("Remove Redis cache failed: {}", cacheKey, e);
        }
    }

    /**
     * 获取员工今日适用规则（带缓存）
     */
    public Optional<AttendanceRuleEntity> getTodayRule(Long employeeId, Long departmentId, String employeeType) {
        String cacheKey = buildTodayRuleKey(employeeId, departmentId, employeeType);

        // 先查L1缓存
        AttendanceRuleEntity rule = attendanceRuleCache.getIfPresent(cacheKey);
        if (rule != null) {
            log.debug("从L1缓存获取今日规则：{}", cacheKey);
            return Optional.ofNullable(rule);
        }

        // 查询数据库
        Optional<AttendanceRuleEntity> optionalRule = attendanceRuleRepository.selectTodayRule(employeeId, departmentId, employeeType);
        if (optionalRule.isPresent()) {
            putTodayRule(cacheKey, optionalRule.get());
        }

        return optionalRule;
    }

    /**
     * 获取员工排班（带缓存）
     */
    public Optional<AttendanceScheduleEntity> getSchedule(Long employeeId, LocalDate scheduleDate) {
        String cacheKey = buildScheduleKey(employeeId, scheduleDate);

        // 先查L1缓存
        AttendanceScheduleEntity schedule = attendanceScheduleCache.getIfPresent(cacheKey);
        if (schedule != null) {
            log.debug("从L1缓存获取排班：{}", cacheKey);
            return Optional.ofNullable(schedule);
        }

        // 查询数据库
        Optional<AttendanceScheduleEntity> optionalSchedule = attendanceScheduleRepository.selectByEmployeeAndDate(employeeId, scheduleDate);
        if (optionalSchedule.isPresent()) {
            putSchedule(cacheKey, optionalSchedule.get());
        }

        return optionalSchedule;
    }

    /**
     * 缓存排班信息
     */
    public void putSchedule(AttendanceScheduleEntity schedule) {
        if (schedule == null || schedule.getEmployeeId() == null || schedule.getScheduleDate() == null) {
            return;
        }

        String cacheKey = buildScheduleKey(schedule.getEmployeeId(), schedule.getScheduleDate());
        putSchedule(cacheKey, schedule);
    }

    /**
     * 清除员工相关的所有缓存
     */
    public void evictEmployeeCache(Long employeeId) {
        try {
            // 清除L1缓存中的相关记录
            attendanceRecordCache.asMap().keySet().stream()
                    .filter(key -> key.contains(employeeId.toString()))
                    .forEach(attendanceRecordCache::invalidate);

            attendanceScheduleCache.asMap().keySet().stream()
                    .filter(key -> key.contains(employeeId.toString()))
                    .forEach(attendanceScheduleCache::invalidate);

            attendanceRuleCache.asMap().keySet().stream()
                    .filter(key -> key.contains(employeeId.toString()))
                    .forEach(attendanceRuleCache::invalidate);

            // 清除Redis中的相关缓存
            redisUtil.deleteByPattern(RECORD_PREFIX + employeeId + "*");
            redisUtil.deleteByPattern(SCHEDULE_PREFIX + employeeId + "*");
            redisUtil.deleteByPattern(RULE_PREFIX + employeeId + "*");

            log.info("已清除员工{}的所有相关缓存", employeeId);
        } catch (Exception e) {
            log.warn("清除员工缓存失败：employeeId={}", employeeId, e);
        }
    }

    /**
     * 清空所有缓存
     */
    public void evictAll() {
        try {
            // 清空L1缓存
            attendanceRecordCache.invalidateAll();
            attendanceRuleCache.invalidateAll();
            attendanceScheduleCache.invalidateAll();
            localCache.invalidateAll();

            // 清空L2缓存中的考勤相关数据
            redisUtil.deleteByPattern(CACHE_PREFIX + "*");

            log.info("已清空所有考勤相关缓存");
        } catch (Exception e) {
            log.warn("清空缓存失败", e);
        }
    }

    /**
     * 获取缓存统计信息
     */
    public Map<String, Object> getCacheStats() {
        return Map.of(
                "attendanceRecordCache", Map.of(
                        "size", attendanceRecordCache.estimatedSize(),
                        "hitCount", attendanceRecordCache.stats().hitCount(),
                        "missCount", attendanceRecordCache.stats().missCount()
                ),
                "attendanceRuleCache", Map.of(
                        "size", attendanceRuleCache.estimatedSize(),
                        "hitCount", attendanceRuleCache.stats().hitCount(),
                        "missCount", attendanceRuleCache.stats().missCount()
                ),
                "attendanceScheduleCache", Map.of(
                        "size", attendanceScheduleCache.estimatedSize(),
                        "hitCount", attendanceScheduleCache.stats().hitCount(),
                        "missCount", attendanceScheduleCache.stats().missCount()
                ),
                "localCache", Map.of(
                        "size", localCache.estimatedSize(),
                        "hitCount", localCache.stats().hitCount(),
                        "missCount", localCache.stats().missCount()
                )
        );
    }

    // ==================== 私有方法：构建缓存键 ====================

    private String buildRecordKey(Long employeeId, LocalDate date) {
        return RECORD_PREFIX + employeeId + ":" + date;
    }

    private String buildScheduleKey(Long employeeId, LocalDate date) {
        return SCHEDULE_PREFIX + employeeId + ":" + date;
    }

    private String buildTodayRuleKey(Long employeeId, Long departmentId, String employeeType) {
        StringBuilder key = new StringBuilder(RULE_PREFIX + "today:" + employeeId);
        if (departmentId != null) {
            key.append(":dept:").append(departmentId);
        }
        if (employeeType != null) {
            key.append(":type:").append(employeeType);
        }
        return key.toString();
    }

    private void putAttendanceRecord(String cacheKey, AttendanceRecordEntity record) {
        // 写入L1缓存
        attendanceRecordCache.put(cacheKey, record);

        // 写入L2缓存
        try {
            redisUtil.set(cacheKey, record, CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("写入考勤记录缓存到Redis失败：{}", cacheKey, e);
        }
    }

    private void putTodayRule(String cacheKey, AttendanceRuleEntity rule) {
        // 写入L1缓存
        attendanceRuleCache.put(cacheKey, rule);

        // 写入L2缓存，今日规则缓存时间短一些
        try {
            redisUtil.set(cacheKey, rule, SHORT_CACHE_EXPIRE, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("写入今日规则缓存到Redis失败：{}", cacheKey, e);
        }
    }

    private void putSchedule(String cacheKey, AttendanceScheduleEntity schedule) {
        // 写入L1缓存
        attendanceScheduleCache.put(cacheKey, schedule);

        // 写入L2缓存
        try {
            redisUtil.set(cacheKey, schedule, CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("写入排班缓存到Redis失败：{}", cacheKey, e);
        }
    }
}