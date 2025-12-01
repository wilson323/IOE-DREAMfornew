package net.lab1024.sa.admin.module.smart.access.manager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.smart.access.dao.AccessRecordDao;
import net.lab1024.sa.admin.module.smart.access.domain.entity.AccessRecordEntity;
import net.lab1024.sa.base.common.manager.BaseCacheManager;

/**
 * 门禁记录管理器 - 缓存层加速数据访问
 *
 * 基于BaseCacheManager实现多级缓存架构 - L1: Caffeine本地缓存(5分钟过期) - L2: Redis分布式缓存(30分钟过期)
 *
 * 核心职责: - 门禁记录缓存管理 - 用户权限缓存 - 访问统计分析 - 实时门禁状态监控
 *
 * 缓存Key规范: - access:record:{recordId} - 门禁记录详情 - access:user:today:{userId} -
 * 用户当日访问记录 -
 * access:device:today:{deviceId} - 设备当日访问记录 - access:stats:{date} - 访问统计数据 -
 * access:permission:{userId} - 用户权限信息
 *
 * @author IOE-DREAM Team
 * @since 2025-11-16
 */
@Slf4j
@Component
public class AccessRecordManager extends BaseCacheManager {

    @Resource
    private AccessRecordDao accessRecordDao;

    // 实时访问统计缓存
    private final Map<String, Long> accessStatsCache = new ConcurrentHashMap<>();

    @Override
    protected String getCachePrefix() {
        return "access:";
    }

    /**
     * 获取门禁记录详情
     *
     * @param recordId 记录ID
     * @return 门禁记录
     */
    public AccessRecordEntity getAccessRecord(Long recordId) {
        String cacheKey = buildCacheKey(recordId, ":record");
        return getCache(cacheKey, () -> accessRecordDao.selectById(recordId));
    }

    /**
     * 缓存门禁记录
     *
     * @param record 门禁记录
     */
    public void cacheAccessRecord(AccessRecordEntity record) {
        if (record != null && record.getRecordId() != null) {
            String cacheKey = buildCacheKey(record.getRecordId(), ":record");
            setCache(cacheKey, record);

            // 更新相关统计缓存
            updateAccessStats(record);
        }
    }

    /**
     * 获取用户当日访问记录
     *
     * @param userId 用户ID
     * @param date   日期 yyyy-MM-dd
     * @return 访问记录列表
     */
    public List<AccessRecordEntity> getUserTodayAccess(Long userId, String date) {
        String cacheKey = buildCacheKey(userId, ":user:today:" + date);
        return getCache(cacheKey, () -> {
            LambdaQueryWrapper<AccessRecordEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AccessRecordEntity::getUserId, userId).apply("DATE(access_time) = {0}", date)
                    .orderByDesc(AccessRecordEntity::getAccessTime);
            return accessRecordDao.selectList(wrapper);
        });
    }

    /**
     * 获取设备当日访问记录
     *
     * @param deviceId 设备ID
     * @param date     日期 yyyy-MM-dd
     * @return 访问记录列表
     */
    public List<AccessRecordEntity> getDeviceTodayAccess(Long deviceId, String date) {
        String cacheKey = buildCacheKey(deviceId, ":device:today:" + date);
        return getCache(cacheKey, () -> {
            LambdaQueryWrapper<AccessRecordEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AccessRecordEntity::getDeviceId, deviceId)
                    .apply("DATE(access_time) = {0}", date)
                    .orderByDesc(AccessRecordEntity::getAccessTime);
            return accessRecordDao.selectList(wrapper);
        });
    }

    /**
     * 获取访问统计数据
     *
     * @param date 日期 yyyy-MM-dd
     * @return 统计信息
     */
    public Map<String, Object> getAccessStats(String date) {
        String cacheKey = buildCacheKey("stats", ":" + date);
        return getCache(cacheKey, () -> {
            Map<String, Object> stats = new ConcurrentHashMap<>();

            try {
                // 总访问次数
                LambdaQueryWrapper<AccessRecordEntity> totalWrapper = new LambdaQueryWrapper<>();
                totalWrapper.apply("DATE(access_time) = {0}", date);
                long totalCount = accessRecordDao.selectCount(totalWrapper);
                stats.put("totalCount", totalCount);

                // 成功次数
                LambdaQueryWrapper<AccessRecordEntity> successWrapper = new LambdaQueryWrapper<>();
                successWrapper.apply("DATE(access_time) = {0}", date)
                        .eq(AccessRecordEntity::getAccessResult, "SUCCESS");
                long successCount = accessRecordDao.selectCount(successWrapper);
                stats.put("successCount", successCount);

                // 失败次数
                long failCount = totalCount - successCount;
                stats.put("failCount", failCount);

                // 成功率
                double successRate = totalCount > 0 ? (double) successCount / totalCount * 100 : 0;
                stats.put("successRate", Math.round(successRate * 100.0) / 100.0);

                // 按访问类型统计
                Map<String, Long> accessTypeStats = new ConcurrentHashMap<>();
                LambdaQueryWrapper<AccessRecordEntity> typeWrapper = new LambdaQueryWrapper<>();
                typeWrapper.apply("DATE(access_time) = {0}", date)
                        .select(AccessRecordEntity::getAccessType);
                List<AccessRecordEntity> records = accessRecordDao.selectList(typeWrapper);

                records.forEach(record -> {
                    String accessType = record.getAccessType();
                    accessTypeStats.compute(accessType, (k, v) -> v == null ? 1L : v + 1L);
                });
                stats.put("accessTypeStats", accessTypeStats);

            } catch (Exception e) {
                log.error("获取访问统计数据异常: date={}", date, e);
                stats.put("error", "获取统计数据失败");
            }

            return stats;
        });
    }

    /**
     * 获取实时访问统计
     *
     * @return 实时统计信息
     */
    public Map<String, Object> getRealTimeStats() {
        Map<String, Object> stats = new ConcurrentHashMap<>();

        // 今日访问总数
        String today = LocalDateTime.now().toLocalDate().toString();
        Long todayCount = accessStatsCache.get("today_total_" + today);
        if (todayCount == null) {
            LambdaQueryWrapper<AccessRecordEntity> todayWrapper = new LambdaQueryWrapper<>();
            todayWrapper.apply("DATE(access_time) = {0}", today);
            todayCount = accessRecordDao.selectCount(todayWrapper);
            accessStatsCache.put("today_total_" + today, todayCount);
        }
        stats.put("todayTotal", todayCount);

        // 今日成功次数
        Long todaySuccessCount = accessStatsCache.get("today_success_" + today);
        if (todaySuccessCount == null) {
            LambdaQueryWrapper<AccessRecordEntity> successTodayWrapper = new LambdaQueryWrapper<>();
            successTodayWrapper.apply("DATE(access_time) = {0}", today)
                    .eq(AccessRecordEntity::getAccessResult, "SUCCESS");
            todaySuccessCount = accessRecordDao.selectCount(successTodayWrapper);
            accessStatsCache.put("today_success_" + today, todaySuccessCount);
        }
        stats.put("todaySuccess", todaySuccessCount);

        // 实时在线设备数（简化实现）
        LambdaQueryWrapper<AccessRecordEntity> onlineWrapper = new LambdaQueryWrapper<>();
        onlineWrapper.apply("DATE(access_time) = {0}", today)
                .groupBy(AccessRecordEntity::getDeviceId);
        stats.put("onlineDevices", accessRecordDao.selectCount(onlineWrapper));

        return stats;
    }

    /**
     * 检查用户访问权限
     *
     * @param userId   用户ID
     * @param deviceId 设备ID
     * @return 权限检查结果
     */
    public Map<String, Object> checkUserPermission(Long userId, Long deviceId) {
        Map<String, Object> result = new ConcurrentHashMap<>();

        try {
            // 简化的权限检查逻辑
            // 实际实现应该查询用户权限表和设备权限配置

            // 检查用户状态
            String userStatus = getUserStatus(userId);
            if (!"ACTIVE".equals(userStatus)) {
                result.put("allowed", false);
                result.put("reason", "用户状态异常: " + userStatus);
                return result;
            }

            // 检查设备状态
            String deviceStatus = getDeviceStatus(deviceId);
            if (!"ONLINE".equals(deviceStatus)) {
                result.put("allowed", false);
                result.put("reason", "设备状态异常: " + deviceStatus);
                return result;
            }

            // 检查用户设备权限
            boolean hasPermission = checkUserDevicePermission(userId, deviceId);

            result.put("allowed", hasPermission);
            result.put("reason", hasPermission ? "权限验证通过" : "无访问权限");

        } catch (Exception e) {
            log.error("检查用户权限异常: userId={}, deviceId={}", userId, deviceId, e);
            result.put("allowed", false);
            result.put("reason", "系统异常，请稍后重试");
        }

        return result;
    }

    /**
     * 更新访问统计
     *
     * @param record 门禁记录
     */
    private void updateAccessStats(AccessRecordEntity record) {
        String date = record.getAccessTime() != null ? record.getAccessTime().toLocalDate().toString()
                : null;
        if (date != null) {
            // 更新当日统计计数
            String totalKey = "today_total_" + date;
            accessStatsCache.compute(totalKey, (k, v) -> v == null ? 1L : v + 1L);

            if ("SUCCESS".equals(record.getAccessResult())) {
                String successKey = "today_success_" + date;
                accessStatsCache.compute(successKey, (k, v) -> v == null ? 1L : v + 1L);
            }

            // 清除当日统计缓存，强制重新计算
            String statsKey = buildCacheKey("stats", ":" + date);
            removeCache(statsKey);
        }
    }

    /**
     * 获取用户状态
     *
     * @param userId 用户ID
     * @return 用户状态
     */
    private String getUserStatus(Long userId) {
        // 简化实现，实际应该查询用户表
        return "ACTIVE";
    }

    /**
     * 获取设备状态
     *
     * @param deviceId 设备ID
     * @return 设备状态
     */
    private String getDeviceStatus(Long deviceId) {
        // 简化实现，实际应该查询设备表
        return "ONLINE";
    }

    /**
     * 检查用户设备权限
     *
     * @param userId   用户ID
     * @param deviceId 设备ID
     * @return 是否有权限
     */
    private boolean checkUserDevicePermission(Long userId, Long deviceId) {
        // 简化实现，实际应该查询权限表
        return true;
    }

    /**
     * 清除用户相关缓存
     *
     * @param userId 用户ID
     */
    public void clearUserCache(Long userId) {
        String pattern = buildCacheKey(userId, ":user:*");
        removeCacheByPattern(pattern);
    }

    /**
     * 清除设备相关缓存
     *
     * @param deviceId 设备ID
     */
    public void clearDeviceCache(Long deviceId) {
        String pattern = buildCacheKey(deviceId, ":device:*");
        removeCacheByPattern(pattern);
    }

    /**
     * 清除日期相关缓存
     *
     * @param date 日期
     */
    public void clearDateCache(String date) {
        String pattern = buildCacheKey("*", ":*:" + date);
        removeCacheByPattern(pattern);

        // 清除统计缓存
        String statsKey = buildCacheKey("stats", ":" + date);
        removeCache(statsKey);
    }

    /**
     * 清除实时统计缓存
     */
    public void clearRealTimeStatsCache() {
        accessStatsCache.clear();
    }

    /**
     * 获取访问趋势数据
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 趋势数据
     */
    public List<Map<String, Object>> getAccessTrend(String startDate, String endDate) {
        // 简化实现，实际应该查询数据库获取趋势数据
        List<Map<String, Object>> trend = new java.util.ArrayList<>();

        // 示例数据
        for (int i = 0; i < 7; i++) {
            Map<String, Object> dayData = new ConcurrentHashMap<>();
            dayData.put("date", startDate);
            dayData.put("totalCount", (int) (Math.random() * 1000) + 500);
            dayData.put("successCount", (int) (Math.random() * 900) + 450);
            trend.add(dayData);
        }

        return trend;
    }
}
