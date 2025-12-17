package net.lab1024.sa.access.manager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;

import net.lab1024.sa.access.dao.AccessRecordDao;
import net.lab1024.sa.access.domain.form.AntiPassbackPolicyForm;
import net.lab1024.sa.access.domain.vo.AntiPassbackStatisticsVO;
import net.lab1024.sa.common.access.entity.AccessRecordEntity;
import net.lab1024.sa.common.organization.entity.AreaEntity;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import net.lab1024.sa.common.organization.service.AreaService;
import net.lab1024.sa.common.organization.service.DeviceService;

/**
 * 门禁反潜回管理器
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用Manager后缀标识业务编排层
 * - 处理复杂业务流程和数据组装
 * - 纯Java类，不使用Spring注解（@Component, @Service等）
 * - 通过构造函数注入依赖
 * - 集成Redis缓存提升性能
 * - 提供统计分析和决策支持
 * </p>
 * <p>
 * 职责：
 * - 反潜回策略管理
 * - 违规数据统计分析
 * - 实时监控和告警
 * - 性能优化和缓存管理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
public class AntiPassbackManager {

    private final AccessRecordDao accessRecordDao;
    private final RedisTemplate<String, Object> redisTemplate;
    private final DeviceService deviceService;
    private final AreaService areaService;

    // 缓存键前缀
    private static final String POLICY_CACHE_PREFIX = "anti_passback_policy:";
    private static final String STATISTICS_CACHE_PREFIX = "anti_passback_stats:";
    private static final String VIOLATION_RECORD_PREFIX = "violation_record:";

    // 缓存过期时间
    private static final long CACHE_EXPIRE_MINUTES = 30;
    private static final long STATISTICS_CACHE_EXPIRE_MINUTES = 15;

    /**
     * 构造函数
     */
    public AntiPassbackManager(AccessRecordDao accessRecordDao,
                              RedisTemplate<String, Object> redisTemplate,
                              DeviceService deviceService,
                              AreaService areaService) {
        this.accessRecordDao = accessRecordDao;
        this.redisTemplate = redisTemplate;
        this.deviceService = deviceService;
        this.areaService = areaService;
    }

    /**
     * 获取反潜回统计数据
     */
    public AntiPassbackStatisticsVO getAntiPassbackStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        log.debug("[反潜回管理器] 获取统计数据 startTime={}, endTime={}", startTime, endTime);

        String cacheKey = STATISTICS_CACHE_PREFIX + startTime.toLocalDate() + "_" + endTime.toLocalDate();

        // 尝试从缓存获取
        AntiPassbackStatisticsVO cachedStats = (AntiPassbackStatisticsVO) redisTemplate.opsForValue().get(cacheKey);
        if (cachedStats != null) {
            return cachedStats;
        }

        // 查询数据库统计数据
        AntiPassbackStatisticsVO statistics = buildStatisticsFromDatabase(startTime, endTime);

        // 缓存结果
        redisTemplate.opsForValue().set(cacheKey, statistics,
                                       Duration.ofMinutes(STATISTICS_CACHE_EXPIRE_MINUTES));

        return statistics;
    }

    /**
     * 分析反潜回趋势
     */
    public Map<String, Object> analyzeAntiPassbackTrends(int days) {
        log.debug("[反潜回管理器] 分析趋势 days={}", days);

        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(days);

        Map<String, Object> trends = new HashMap<>();

        // 日均违规趋势
        List<Map<String, Object>> dailyTrends = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDateTime dayStart = startTime.plusDays(i);
            LocalDateTime dayEnd = dayStart.plusDays(1);

            Long dailyViolations = countViolationsByTimeRange(dayStart, dayEnd);
            Long dailyTotalChecks = countTotalChecksByTimeRange(dayStart, dayEnd);

            Map<String, Object> dayTrend = new HashMap<>();
            dayTrend.put("date", dayStart.toLocalDate());
            dayTrend.put("violations", dailyViolations);
            dayTrend.put("totalChecks", dailyTotalChecks);
            dayTrend.put("violationRate", dailyTotalChecks > 0 ? (double) dailyViolations / dailyTotalChecks : 0.0);

            dailyTrends.add(dayTrend);
        }
        trends.put("dailyTrends", dailyTrends);

        // 高风险时段分析
        Map<String, Long> highRiskPeriods = analyzeHighRiskPeriods(startTime, endTime);
        trends.put("highRiskPeriods", highRiskPeriods);

        // 违规类型分布
        Map<String, Long> violationTypeDistribution = analyzeViolationTypeDistribution(startTime, endTime);
        trends.put("violationTypeDistribution", violationTypeDistribution);

        return trends;
    }

    /**
     * 检查区域容量
     */
    public boolean checkAreaCapacity(Long areaId) {
        log.debug("[反潜回管理器] 检查区域容量 areaId={}", areaId);

        try {
            AreaEntity area = areaService.getById(areaId);
            if (area == null || area.getMaxCapacity() == null) {
                return true; // 无容量限制
            }

            String currentCountKey = "area_current_count:" + areaId;
            Integer currentCount = (Integer) redisTemplate.opsForValue().get(currentCountKey);

            if (currentCount == null) {
                // 如果缓存中没有，从数据库计算
                currentCount = calculateCurrentAreaOccupancy(areaId);
                redisTemplate.opsForValue().set(currentCountKey, currentCount, Duration.ofMinutes(5));
            }

            return currentCount < area.getMaxCapacity();

        } catch (Exception e) {
            log.error("[反潜回管理器] 检查区域容量异常 areaId={}, error={}", areaId, e.getMessage(), e);
            return true; // 异常时允许通行
        }
    }

    /**
     * 更新区域人数
     */
    public void updateAreaOccupancy(Long areaId, String accessType) {
        log.debug("[反潜回管理器] 更新区域人数 areaId={}, accessType={}", areaId, accessType);

        String currentCountKey = "area_current_count:" + areaId;

        if ("IN".equals(accessType)) {
            redisTemplate.opsForValue().increment(currentCountKey);
        } else if ("OUT".equals(accessType)) {
            redisTemplate.opsForValue().decrement(currentCountKey);
        }

        // 设置过期时间
        redisTemplate.expire(currentCountKey, Duration.ofMinutes(10));
    }

    /**
     * 记录违规事件
     */
    public void recordViolation(Long userId, Long deviceId, Long areaId, String violationType, String reason) {
        log.warn("[反潜回管理器] 记录违规 userId={}, deviceId={}, areaId={}, type={}",
                userId, deviceId, areaId, violationType);

        try {
            // 记录到Redis
            String violationKey = VIOLATION_RECORD_PREFIX + LocalDateTime.now().toLocalDate();
            String violationData = String.format("%d:%d:%d:%s:%s:%d",
                    userId, deviceId, areaId, violationType, reason, System.currentTimeMillis());

            redisTemplate.opsForList().rightPush(violationKey, violationData);
            redisTemplate.expire(violationKey, Duration.ofDays(7));

            // 检查是否需要发送告警
            checkAndSendAlert(userId, violationType);

        } catch (Exception e) {
            log.error("[反潜回管理器] 记录违规异常 userId={}, error={}", userId, e.getMessage(), e);
        }
    }

    /**
     * 获取用户反潜回历史
     */
    public Page<Map<String, Object>> getUserAntiPassbackHistory(Long userId, int page, int size) {
        log.debug("[反潜回管理器] 获取用户反潜回历史 userId={}, page={}, size={}", userId, page, size);

        Pageable pageable = PageRequest.of(page, size);

        // 查询用户通行记录
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(30);

        List<AccessRecordEntity> records = accessRecordDao.selectByUserIdAndTimeRange(
                userId, startTime, endTime, size * (page + 1));

        // 转换为包含反潜回信息的历史记录
        List<Map<String, Object>> history = records.stream()
                .skip((long) page * size)
                .limit(size)
                .map(this::buildAntiPassbackHistory)
                .collect(Collectors.toList());

        return new PageImpl<>(history, pageable, records.size());
    }

    /**
     * 清理过期缓存
     */
    public void cleanExpiredCache() {
        log.debug("[反潜回管理器] 清理过期缓存");

        try {
            Set<String> keys = redisTemplate.keys(POLICY_CACHE_PREFIX + "*");
            Set<String> statsKeys = redisTemplate.keys(STATISTICS_CACHE_PREFIX + "*");

            Set<String> allKeys = new HashSet<>();
            allKeys.addAll(keys);
            allKeys.addAll(statsKeys);

            if (!allKeys.isEmpty()) {
                redisTemplate.delete(allKeys);
                log.info("[反潜回管理器] 清理过期缓存完成，清理{}个缓存", allKeys.size());
            }

        } catch (Exception e) {
            log.error("[反潜回管理器] 清理过期缓存异常 error={}", e.getMessage(), e);
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 从数据库构建统计数据
     */
    private AntiPassbackStatisticsVO buildStatisticsFromDatabase(LocalDateTime startTime, LocalDateTime endTime) {
        // 查询总检查次数
        Long totalCheckCount = countTotalChecksByTimeRange(startTime, endTime);

        // 查询成功次数
        Long successCount = countSuccessChecksByTimeRange(startTime, endTime);

        // 查询各类违规次数
        Long hardViolations = countViolationsByType(startTime, endTime, "HARD");
        Long softExceptions = countViolationsByType(startTime, endTime, "SOFT");
        Long areaViolations = countViolationsByType(startTime, endTime, "AREA");
        Long globalViolations = countViolationsByType(startTime, endTime, "GLOBAL");

        // 计算失败次数
        Long failureCount = totalCheckCount - successCount;

        // 计算成功率
        Double successRate = totalCheckCount > 0 ? (double) successCount / totalCheckCount : 0.0;

        return AntiPassbackStatisticsVO.builder()
                .statisticsTime(LocalDateTime.now())
                .totalCheckCount(totalCheckCount)
                .successCount(successCount)
                .failureCount(failureCount)
                .successRate(successRate * 100)
                .hardAntiPassbackViolations(hardViolations)
                .softAntiPassbackExceptions(softExceptions)
                .areaAntiPassbackViolations(areaViolations)
                .globalAntiPassbackViolations(globalViolations)
                .areaStatisticsList(buildAreaStatistics(startTime, endTime))
                .deviceStatisticsList(buildDeviceStatistics(startTime, endTime))
                .timeDistribution(buildTimeDistribution(startTime, endTime))
                .build();
    }

    /**
     * 构建区域统计
     */
    private List<AntiPassbackStatisticsVO.AreaStatistics> buildAreaStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        // TODO: 实现区域统计查询
        return new ArrayList<>();
    }

    /**
     * 构建设备统计
     */
    private List<AntiPassbackStatisticsVO.DeviceStatistics> buildDeviceStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        // TODO: 实现设备统计查询
        return new ArrayList<>();
    }

    /**
     * 构建时间分布
     */
    private Map<String, Long> buildTimeDistribution(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Long> distribution = new HashMap<>();

        // 按小时统计
        for (int hour = 0; hour < 24; hour++) {
            Long count = countChecksByHour(startTime, endTime, hour);
            distribution.put(String.format("%02d:00", hour), count);
        }

        return distribution;
    }

    /**
     * 构建反潜回历史记录
     */
    private Map<String, Object> buildAntiPassbackHistory(AccessRecordEntity record) {
        Map<String, Object> history = new HashMap<>();

        history.put("recordId", record.getRecordId());
        history.put("accessTime", record.getAccessTime());
        history.put("accessResult", record.getAccessResult());
        history.put("accessType", record.getAccessType());
        history.put("verifyMethod", record.getVerifyMethod());
        history.put("deviceId", record.getDeviceId());
        history.put("areaId", record.getAreaId());

        // 获取设备信息
        DeviceEntity device = deviceService.getById(record.getDeviceId());
        if (device != null) {
            history.put("deviceName", device.getDeviceName());
            history.put("deviceLocation", device.getDeviceLocation());
        }

        // 获取区域信息
        AreaEntity area = areaService.getById(record.getAreaId());
        if (area != null) {
            history.put("areaName", area.getAreaName());
        }

        return history;
    }

    /**
     * 检查并发送告警
     */
    private void checkAndSendAlert(Long userId, String violationType) {
        String alertKey = "violation_alert:" + userId;

        // 检查最近的违规次数
        List<String> recentViolations = redisTemplate.opsForList().range(
                VIOLATION_RECORD_PREFIX + LocalDateTime.now().toLocalDate(), 0, -1);

        if (recentViolations.size() >= 5) { // 违规5次触发告警
            log.warn("[反潜回管理器] 用户违规次数过多，触发告警 userId={}, count={}", userId, recentViolations.size());

            // 发送告警（这里可以集成告警系统）
            sendAlert(userId, violationType, recentViolations.size());
        }
    }

    /**
     * 发送告警
     */
    private void sendAlert(Long userId, String violationType, int violationCount) {
        // TODO: 实现告警发送逻辑
        log.info("[反潜回管理器] 发送告警 userId={}, type={}, count={}", userId, violationType, violationCount);
    }

    /**
     * 计算当前区域占用人数
     */
    private Integer calculateCurrentAreaOccupancy(Long areaId) {
        // 查询最近的进入和离开记录
        // TODO: 实现更精确的人数计算
        return 0;
    }

    // 数据查询辅助方法（简化实现，实际应该使用SQL）
    private Long countTotalChecksByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return 0L; // TODO: 实现数据库查询
    }

    private Long countSuccessChecksByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return 0L; // TODO: 实现数据库查询
    }

    private Long countViolationsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return 0L; // TODO: 实现数据库查询
    }

    private Long countViolationsByType(LocalDateTime startTime, LocalDateTime endTime, String type) {
        return 0L; // TODO: 实现数据库查询
    }

    private Long countChecksByHour(LocalDateTime startTime, LocalDateTime endTime, int hour) {
        return 0L; // TODO: 实现数据库查询
    }

    private Map<String, Long> analyzeHighRiskPeriods(LocalDateTime startTime, LocalDateTime endTime) {
        return new HashMap<>(); // TODO: 实现高风险时段分析
    }

    private Map<String, Long> analyzeViolationTypeDistribution(LocalDateTime startTime, LocalDateTime endTime) {
        return new HashMap<>(); // TODO: 实现违规类型分布分析
    }
}