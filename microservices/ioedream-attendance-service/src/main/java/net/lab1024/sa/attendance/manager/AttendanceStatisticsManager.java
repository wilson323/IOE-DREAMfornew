package net.lab1024.sa.attendance.manager;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.dao.AttendanceRecordDao;
import net.lab1024.sa.common.entity.attendance.AttendanceRecordEntity;
import net.lab1024.sa.common.domain.PageResult;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 考勤数据统计管理器
 * <p>
 * 严格遵循四层架构规范（Controller→Service→Manager→DAO）
 * Manager层负责业务编排和复杂业务逻辑处理
 * </p>
 * <p>
 * 核心职责：
 * - 考勤数据批量统计
 * - 统计结果缓存管理
 * - 并行统计处理
 * - 统计性能优化
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@Component
@Slf4j
public class AttendanceStatisticsManager {

    private final AttendanceRecordDao attendanceRecordDao;

    // 统计缓存（按统计键缓存结果）
    private final Map<String, CachedStatistics> statisticsCache = new ConcurrentHashMap<>();

    // 并行处理线程池
    private final ExecutorService statisticsExecutor = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors(),
            r -> {
                Thread thread = new Thread(r, "attendance-statistics-worker");
                thread.setDaemon(true);
                return thread;
            }
    );

    // 缓存配置
    private static final long CACHE_EXPIRE_MINUTES = 10; // 缓存过期时间（分钟）
    private static final int MAX_CACHE_SIZE = 500; // 最大缓存条目数

    /**
     * 构造函数注入依赖
     */
    public AttendanceStatisticsManager(AttendanceRecordDao attendanceRecordDao) {
        this.attendanceRecordDao = attendanceRecordDao;
        log.info("[考勤统计] 考勤数据统计管理器初始化完成");
    }

    /**
     * 统计用户指定日期范围的考勤数据
     *
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计结果
     */
    public UserAttendanceStatistics getUserStatistics(Long userId, LocalDate startDate, LocalDate endDate) {
        String cacheKey = generateCacheKey("USER", userId, startDate, endDate);

        // 尝试从缓存获取
        CachedStatistics cached = statisticsCache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            log.debug("[考勤统计] 使用缓存的统计数据: userId={}, dates={}~{}",
                    userId, startDate, endDate);
            return (UserAttendanceStatistics) cached.getStatistics();
        }

        log.info("[考勤统计] 开始统计用户考勤数据: userId={}, dates={}~{}", userId, startDate, endDate);

        long startTime = System.currentTimeMillis();

        try {
            // 查询考勤记录
            LambdaQueryWrapper<AttendanceRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AttendanceRecordEntity::getUserId, userId)
                    .ge(AttendanceRecordEntity::getAttendanceDate, startDate)
                    .le(AttendanceRecordEntity::getAttendanceDate, endDate)
                    .eq(AttendanceRecordEntity::getDeletedFlag, 0)
                    .orderByAsc(AttendanceRecordEntity::getAttendanceDate);

            List<AttendanceRecordEntity> records = attendanceRecordDao.selectList(queryWrapper);

            // 计算统计数据
            UserAttendanceStatistics statistics = calculateUserStatistics(userId, records, startDate, endDate);

            // 缓存结果
            cacheStatistics(cacheKey, statistics);

            long duration = System.currentTimeMillis() - startTime;
            log.info("[考勤统计] 用户考勤统计完成: userId={}, 记录数={}, 耗时={}ms",
                    userId, records.size(), duration);

            return statistics;

        } catch (Exception e) {
            log.error("[考勤统计] 用户考勤统计失败: userId={}", userId, e);
            return createEmptyUserStatistics(userId, startDate, endDate);
        }
    }

    /**
     * 批量统计多个用户的考勤数据
     *
     * @param userIds 用户ID列表
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 用户统计结果列表
     */
    public List<UserAttendanceStatistics> batchGetUserStatistics(List<Long> userIds, LocalDate startDate, LocalDate endDate) {
        log.info("[考勤统计] 开始批量统计用户考勤数据: userCount={}, dates={}~{}",
                userIds.size(), startDate, endDate);

        long startTime = System.currentTimeMillis();

        try {
            // 并行统计
            List<CompletableFuture<UserAttendanceStatistics>> futures = userIds.stream()
                    .map(userId -> CompletableFuture.supplyAsync(
                            () -> getUserStatistics(userId, startDate, endDate),
                            statisticsExecutor))
                    .collect(Collectors.toList());

            // 等待所有任务完成
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0]));

            allFutures.join();

            List<UserAttendanceStatistics> results = futures.stream()
                    .map(CompletableFuture::join)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            long duration = System.currentTimeMillis() - startTime;
            log.info("[考勤统计] 批量用户考勤统计完成: userCount={}, 成功={}, 耗时={}ms",
                    userIds.size(), results.size(), duration);

            return results;

        } catch (Exception e) {
            log.error("[考勤统计] 批量用户考勤统计失败: userCount={}", userIds.size(), e);
            return userIds.stream()
                    .map(userId -> createEmptyUserStatistics(userId, startDate, endDate))
                    .collect(Collectors.toList());
        }
    }

    /**
     * 统计部门指定日期范围的考勤数据
     *
     * @param departmentId 部门ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 部门统计结果
     */
    public DepartmentAttendanceStatistics getDepartmentStatistics(Long departmentId, LocalDate startDate, LocalDate endDate) {
        String cacheKey = generateCacheKey("DEPT", departmentId, startDate, endDate);

        // 尝试从缓存获取
        CachedStatistics cached = statisticsCache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            log.debug("[考勤统计] 使用缓存的部门统计数据: departmentId={}, dates={}~{}",
                    departmentId, startDate, endDate);
            return (DepartmentAttendanceStatistics) cached.getStatistics();
        }

        log.info("[考勤统计] 开始统计部门考勤数据: departmentId={}, dates={}~{}",
                departmentId, startDate, endDate);

        long startTime = System.currentTimeMillis();

        try {
            // 查询部门所有用户的考勤记录
            LambdaQueryWrapper<AttendanceRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AttendanceRecordEntity::getDepartmentId, departmentId)
                    .ge(AttendanceRecordEntity::getAttendanceDate, startDate)
                    .le(AttendanceRecordEntity::getAttendanceDate, endDate)
                    .eq(AttendanceRecordEntity::getDeletedFlag, 0);

            List<AttendanceRecordEntity> records = attendanceRecordDao.selectList(queryWrapper);

            // 计算部门统计数据
            DepartmentAttendanceStatistics statistics = calculateDepartmentStatistics(departmentId, records, startDate, endDate);

            // 缓存结果
            cacheStatistics(cacheKey, statistics);

            long duration = System.currentTimeMillis() - startTime;
            log.info("[考勤统计] 部门考勤统计完成: departmentId={}, 记录数={}, 耗时={}ms",
                    departmentId, records.size(), duration);

            return statistics;

        } catch (Exception e) {
            log.error("[考勤统计] 部门考勤统计失败: departmentId={}", departmentId, e);
            return createEmptyDepartmentStatistics(departmentId, startDate, endDate);
        }
    }

    /**
     * 统计月度考勤汇总数据
     *
     * @param userId 用户ID
     * @param yearMonth 年月
     * @return 月度统计结果
     */
    public MonthlyAttendanceStatistics getMonthlyStatistics(Long userId, YearMonth yearMonth) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        String cacheKey = generateCacheKey("MONTHLY", userId, yearMonth);

        // 尝试从缓存获取
        CachedStatistics cached = statisticsCache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            log.debug("[考勤统计] 使用缓存的月度统计数据: userId={}, month={}",
                    userId, yearMonth);
            return (MonthlyAttendanceStatistics) cached.getStatistics();
        }

        log.info("[考勤统计] 开始统计月度考勤数据: userId={}, month={}", userId, yearMonth);

        long startTime = System.currentTimeMillis();

        try {
            // 获取用户统计数据
            UserAttendanceStatistics userStats = getUserStatistics(userId, startDate, endDate);

            // 计算月度统计
            MonthlyAttendanceStatistics statistics = new MonthlyAttendanceStatistics();
            statistics.setUserId(userId);
            statistics.setYearMonth(yearMonth.toString());
            statistics.setTotalDays(userStats.getTotalDays());
            statistics.setPresentDays(userStats.getPresentDays());
            statistics.setLateDays(userStats.getLateDays());
            statistics.setEarlyLeaveDays(userStats.getEarlyLeaveDays());
            statistics.setAbsentDays(userStats.getAbsentDays());
            statistics.setAttendanceRate(userStats.getAttendanceRate());

            // 缓存结果
            cacheStatistics(cacheKey, statistics);

            long duration = System.currentTimeMillis() - startTime;
            log.info("[考勤统计] 月度考勤统计完成: userId={}, month={}, 耗时={}ms",
                    userId, yearMonth, duration);

            return statistics;

        } catch (Exception e) {
            log.error("[考勤统计] 月度考勤统计失败: userId={}, month={}", userId, yearMonth, e);
            return createEmptyMonthlyStatistics(userId, yearMonth);
        }
    }

    /**
     * 清除统计缓存
     */
    public void clearStatisticsCache() {
        log.info("[考勤统计] 清除统计缓存");
        statisticsCache.clear();
    }

    /**
     * 清除指定用户的统计缓存
     */
    public void clearUserStatisticsCache(Long userId) {
        log.info("[考勤统计] 清除用户统计缓存: userId={}", userId);

        // 查找并移除所有相关的缓存
        statisticsCache.entrySet().removeIf(entry -> {
            String key = entry.getKey();
            return key.startsWith("USER:") && key.contains(userId.toString());
        });
    }

    /**
     * 获取缓存统计信息
     */
    public Map<String, Object> getCacheStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("cacheSize", statisticsCache.size());
        stats.put("maxCacheSize", MAX_CACHE_SIZE);
        stats.put("cacheExpireMinutes", CACHE_EXPIRE_MINUTES);

        // 计算缓存命中率（简化实现）
        long totalHits = statisticsCache.values().stream().mapToLong(CachedStatistics::getHitCount).sum();
        stats.put("totalHits", totalHits);

        return stats;
    }

    /**
     * 关闭统计管理器
     */
    public void shutdown() {
        log.info("[考勤统计] 关闭考勤数据统计管理器");
        statisticsExecutor.shutdown();
        try {
            if (!statisticsExecutor.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS)) {
                statisticsExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            statisticsExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 计算用户统计数据
     */
    private UserAttendanceStatistics calculateUserStatistics(Long userId, List<AttendanceRecordEntity> records,
                                                              LocalDate startDate, LocalDate endDate) {
        UserAttendanceStatistics stats = new UserAttendanceStatistics();
        stats.setUserId(userId);
        stats.setStartDate(startDate);
        stats.setEndDate(endDate);

        if (records.isEmpty()) {
            stats.setTotalDays(0);
            stats.setPresentDays(0);
            stats.setLateDays(0);
            stats.setEarlyLeaveDays(0);
            stats.setAbsentDays(0);
            stats.setAttendanceRate(0.0);
            return stats;
        }

        // 统计各项数据
        long totalDays = records.size();
        long presentDays = records.stream().filter(r -> "PRESENT".equals(r.getAttendanceStatus())).count();
        long lateDays = records.stream().filter(r -> "LATE".equals(r.getAttendanceStatus())).count();
        long earlyLeaveDays = records.stream().filter(r -> "EARLY_LEAVE".equals(r.getAttendanceStatus())).count();
        long absentDays = records.stream().filter(r -> "ABSENT".equals(r.getAttendanceStatus())).count();

        stats.setTotalDays((int) totalDays);
        stats.setPresentDays((int) presentDays);
        stats.setLateDays((int) lateDays);
        stats.setEarlyLeaveDays((int) earlyLeaveDays);
        stats.setAbsentDays((int) absentDays);

        // 计算出勤率
        double attendanceRate = totalDays > 0 ? (presentDays * 100.0 / totalDays) : 0.0;
        stats.setAttendanceRate(Math.round(attendanceRate * 100.0) / 100.0);

        return stats;
    }

    /**
     * 计算部门统计数据
     */
    private DepartmentAttendanceStatistics calculateDepartmentStatistics(Long departmentId,
                                                                      List<AttendanceRecordEntity> records,
                                                                      LocalDate startDate,
                                                                      LocalDate endDate) {
        DepartmentAttendanceStatistics stats = new DepartmentAttendanceStatistics();
        stats.setDepartmentId(departmentId);
        stats.setStartDate(startDate);
        stats.setEndDate(endDate);

        if (records.isEmpty()) {
            stats.setTotalEmployees(0);
            stats.setTotalRecords(0);
            stats.setPresentRecords(0);
            stats.setAbsentRecords(0);
            stats.setOverallAttendanceRate(0.0);
            return stats;
        }

        // 统计部门员工数和各项记录数
        Set<Long> uniqueEmployees = records.stream()
                .map(AttendanceRecordEntity::getUserId)
                .collect(Collectors.toSet());

        long presentRecords = records.stream().filter(r -> "PRESENT".equals(r.getAttendanceStatus())).count();
        long absentRecords = records.stream().filter(r -> "ABSENT".equals(r.getAttendanceStatus())).count();

        stats.setTotalEmployees(uniqueEmployees.size());
        stats.setTotalRecords(records.size());
        stats.setPresentRecords((int) presentRecords);
        stats.setAbsentRecords((int) absentRecords);

        // 计算总体出勤率
        double attendanceRate = records.size() > 0 ? (presentRecords * 100.0 / records.size()) : 0.0;
        stats.setOverallAttendanceRate(Math.round(attendanceRate * 100.0) / 100.0);

        return stats;
    }

    /**
     * 缓存统计结果
     */
    private void cacheStatistics(String cacheKey, Object statistics) {
        // 检查缓存大小
        if (statisticsCache.size() >= MAX_CACHE_SIZE) {
            // 清理部分缓存
            cleanupExpiredCache();
        }

        CachedStatistics cached = new CachedStatistics(statistics);
        statisticsCache.put(cacheKey, cached);
    }

    /**
     * 清理过期缓存
     */
    private void cleanupExpiredCache() {
        int cleanupCount = MAX_CACHE_SIZE / 10; // 清理10%的缓存

        statisticsCache.entrySet().removeIf(entry -> {
            CachedStatistics cached = entry.getValue();
            return cached.isExpired() || (Math.random() < 0.1 && statisticsCache.size() > MAX_CACHE_SIZE - cleanupCount);
        });
    }

    /**
     * 生成缓存键
     */
    private String generateCacheKey(String type, Object... params) {
        return type + ":" + Arrays.stream(params).map(Object::toString).collect(Collectors.joining(":"));
    }

    /**
     * 创建空的用户统计数据
     */
    private UserAttendanceStatistics createEmptyUserStatistics(Long userId, LocalDate startDate, LocalDate endDate) {
        UserAttendanceStatistics stats = new UserAttendanceStatistics();
        stats.setUserId(userId);
        stats.setStartDate(startDate);
        stats.setEndDate(endDate);
        stats.setTotalDays(0);
        stats.setPresentDays(0);
        stats.setLateDays(0);
        stats.setEarlyLeaveDays(0);
        stats.setAbsentDays(0);
        stats.setAttendanceRate(0.0);
        return stats;
    }

    /**
     * 创建空的部门统计数据
     */
    private DepartmentAttendanceStatistics createEmptyDepartmentStatistics(Long departmentId, LocalDate startDate, LocalDate endDate) {
        DepartmentAttendanceStatistics stats = new DepartmentAttendanceStatistics();
        stats.setDepartmentId(departmentId);
        stats.setStartDate(startDate);
        stats.setEndDate(endDate);
        stats.setTotalEmployees(0);
        stats.setTotalRecords(0);
        stats.setPresentRecords(0);
        stats.setAbsentRecords(0);
        stats.setOverallAttendanceRate(0.0);
        return stats;
    }

    /**
     * 创建空的月度统计数据
     */
    private MonthlyAttendanceStatistics createEmptyMonthlyStatistics(Long userId, YearMonth yearMonth) {
        MonthlyAttendanceStatistics stats = new MonthlyAttendanceStatistics();
        stats.setUserId(userId);
        stats.setYearMonth(yearMonth.toString());
        stats.setTotalDays(0);
        stats.setPresentDays(0);
        stats.setLateDays(0);
        stats.setEarlyLeaveDays(0);
        stats.setAbsentDays(0);
        stats.setAttendanceRate(0.0);
        return stats;
    }

    // ==================== 内部类 ====================

    /**
     * 缓存的统计数据
     */
    private static class CachedStatistics {
        private final Object statistics;
        private final LocalDateTime cacheTime;
        private final long expireMinutes;
        private long hitCount;

        public CachedStatistics(Object statistics) {
            this.statistics = statistics;
            this.cacheTime = LocalDateTime.now();
            this.expireMinutes = CACHE_EXPIRE_MINUTES;
            this.hitCount = 0;
        }

        public boolean isExpired() {
            LocalDateTime expireTime = cacheTime.plusMinutes(expireMinutes);
            return LocalDateTime.now().isAfter(expireTime);
        }

        public Object getStatistics() {
            hitCount++;
            return statistics;
        }

        public long getHitCount() {
            return hitCount;
        }
    }

    /**
     * 用户考勤统计数据
     */
    public static class UserAttendanceStatistics {
        private Long userId;
        private LocalDate startDate;
        private LocalDate endDate;
        private int totalDays;
        private int presentDays;
        private int lateDays;
        private int earlyLeaveDays;
        private int absentDays;
        private double attendanceRate;

        // Getters and Setters
        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public LocalDate getStartDate() {
            return startDate;
        }

        public void setStartDate(LocalDate startDate) {
            this.startDate = startDate;
        }

        public LocalDate getEndDate() {
            return endDate;
        }

        public void setEndDate(LocalDate endDate) {
            this.endDate = endDate;
        }

        public int getTotalDays() {
            return totalDays;
        }

        public void setTotalDays(int totalDays) {
            this.totalDays = totalDays;
        }

        public int getPresentDays() {
            return presentDays;
        }

        public void setPresentDays(int presentDays) {
            this.presentDays = presentDays;
        }

        public int getLateDays() {
            return lateDays;
        }

        public void setLateDays(int lateDays) {
            this.lateDays = lateDays;
        }

        public int getEarlyLeaveDays() {
            return earlyLeaveDays;
        }

        public void setEarlyLeaveDays(int earlyLeaveDays) {
            this.earlyLeaveDays = earlyLeaveDays;
        }

        public int getAbsentDays() {
            return absentDays;
        }

        public void setAbsentDays(int absentDays) {
            this.absentDays = absentDays;
        }

        public double getAttendanceRate() {
            return attendanceRate;
        }

        public void setAttendanceRate(double attendanceRate) {
            this.attendanceRate = attendanceRate;
        }
    }

    /**
     * 部门考勤统计数据
     */
    public static class DepartmentAttendanceStatistics {
        private Long departmentId;
        private LocalDate startDate;
        private LocalDate endDate;
        private int totalEmployees;
        private int totalRecords;
        private int presentRecords;
        private int absentRecords;
        private double overallAttendanceRate;

        // Getters and Setters
        public Long getDepartmentId() {
            return departmentId;
        }

        public void setDepartmentId(Long departmentId) {
            this.departmentId = departmentId;
        }

        public LocalDate getStartDate() {
            return startDate;
        }

        public void setStartDate(LocalDate startDate) {
            this.startDate = startDate;
        }

        public LocalDate getEndDate() {
            return endDate;
        }

        public void setEndDate(LocalDate endDate) {
            this.endDate = endDate;
        }

        public int getTotalEmployees() {
            return totalEmployees;
        }

        public void setTotalEmployees(int totalEmployees) {
            this.totalEmployees = totalEmployees;
        }

        public int getTotalRecords() {
            return totalRecords;
        }

        public void setTotalRecords(int totalRecords) {
            this.totalRecords = totalRecords;
        }

        public int getPresentRecords() {
            return presentRecords;
        }

        public void setPresentRecords(int presentRecords) {
            this.presentRecords = presentRecords;
        }

        public int getAbsentRecords() {
            return absentRecords;
        }

        public void setAbsentRecords(int absentRecords) {
            this.absentRecords = absentRecords;
        }

        public double getOverallAttendanceRate() {
            return overallAttendanceRate;
        }

        public void setOverallAttendanceRate(double overallAttendanceRate) {
            this.overallAttendanceRate = overallAttendanceRate;
        }
    }

    /**
     * 月度考勤统计数据
     */
    public static class MonthlyAttendanceStatistics {
        private Long userId;
        private String yearMonth;
        private int totalDays;
        private int presentDays;
        private int lateDays;
        private int earlyLeaveDays;
        private int absentDays;
        private double attendanceRate;

        // Getters and Setters
        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getYearMonth() {
            return yearMonth;
        }

        public void setYearMonth(String yearMonth) {
            this.yearMonth = yearMonth;
        }

        public int getTotalDays() {
            return totalDays;
        }

        public void setTotalDays(int totalDays) {
            this.totalDays = totalDays;
        }

        public int getPresentDays() {
            return presentDays;
        }

        public void setPresentDays(int presentDays) {
            this.presentDays = presentDays;
        }

        public int getLateDays() {
            return lateDays;
        }

        public void setLateDays(int lateDays) {
            this.lateDays = lateDays;
        }

        public int getEarlyLeaveDays() {
            return earlyLeaveDays;
        }

        public void setEarlyLeaveDays(int earlyLeaveDays) {
            this.earlyLeaveDays = earlyLeaveDays;
        }

        public int getAbsentDays() {
            return absentDays;
        }

        public void setAbsentDays(int absentDays) {
            this.absentDays = absentDays;
        }

        public double getAttendanceRate() {
            return attendanceRate;
        }

        public void setAttendanceRate(double attendanceRate) {
            this.attendanceRate = attendanceRate;
        }
    }
}
