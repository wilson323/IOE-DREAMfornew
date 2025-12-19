package net.lab1024.sa.consume.manager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.consume.domain.vo.MobileConsumeStatisticsVO;

/**
 * 移动端消费统计Manager
 *
 * <p>
 * 按照四层架构规范，Manager层负责复杂业务流程编排和多DAO数据组装
 * </p>
 * <p>
 * 严格遵循CLAUDE.md全局架构规范：
 * </p>
 * <p>
 * - microservices-common中的Manager类为纯Java类，不使用Spring注解
 * </p>
 * <p>
 * - 通过构造函数注入依赖，保持为纯Java类
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-09
 */
public class MobileConsumeStatisticsManager {

    private static final Logger log = LoggerFactory.getLogger(MobileConsumeStatisticsManager.class);

    private final GatewayServiceClient gatewayServiceClient;

    // 构造函数注入依赖
    public MobileConsumeStatisticsManager(GatewayServiceClient gatewayServiceClient) {
        this.gatewayServiceClient = gatewayServiceClient;
    }

    /**
     * 获取移动端消费统计数据
     *
     * @param userId         用户ID
     * @param statisticsType 统计类型：daily(日)、weekly(周)、monthly(月)
     * @param startDate      开始日期
     * @param endDate        结束日期
     * @return 消费统计数据
     */
    public MobileConsumeStatisticsVO getConsumeStatistics(Long userId, String statisticsType,
            LocalDateTime startDate, LocalDateTime endDate) {
        log.info("[消费统计Manager] 开始统计, userId={}, statisticsType={}, startDate={}, endDate={}",
                userId, statisticsType, startDate, endDate);

        try {
            // 1. 参数验证和默认值设置
            validateAndSetDefaultParameters(statisticsType, startDate, endDate);

            // 2. 获取用户信息（通过网关调用）- 用于日志记录，不直接使用返回值
            getUserInfoFromGateway(userId);

            // 3. 根据统计类型获取时间范围
            TimeRange timeRange = getTimeRange(statisticsType, startDate, endDate);

            // 4. 并行查询消费数据（通过网关调用consume-service）
            CompletableFuture<List<Map<String, Object>>> todayFuture = CompletableFuture.supplyAsync(
                    () -> getConsumeRecordsByDateRange(userId, timeRange.getTodayStart(), timeRange.getTodayEnd()));

            CompletableFuture<List<Map<String, Object>>> weekFuture = CompletableFuture.supplyAsync(
                    () -> getConsumeRecordsByDateRange(userId, timeRange.getWeekStart(), timeRange.getWeekEnd()));

            CompletableFuture<List<Map<String, Object>>> monthFuture = CompletableFuture.supplyAsync(
                    () -> getConsumeRecordsByDateRange(userId, timeRange.getMonthStart(), timeRange.getMonthEnd()));

            // 5. 等待所有查询完成并计算统计数据
            List<Map<String, Object>> todayRecords = todayFuture.join();
            List<Map<String, Object>> weekRecords = weekFuture.join();
            List<Map<String, Object>> monthRecords = monthFuture.join();

            // 6. 构建统计结果
            MobileConsumeStatisticsVO statistics = buildStatistics(todayRecords, weekRecords, monthRecords);

            log.info("[消费统计Manager] 统计完成, userId={}, 今日消费次数={}, 今日消费金额={}",
                    userId, statistics.getTodayConsumeCount(), statistics.getTodayConsumeAmount());

            return statistics;

        } catch (Exception e) {
            log.error("[消费统计Manager] 统计异常, userId={}, error={}", userId, e.getMessage(), e);
            throw new SystemException("CONSUME_STATISTICS_ERROR", "消费统计失败", e);
        }
    }

    /**
     * 验证参数并设置默认值
     */
    private void validateAndSetDefaultParameters(String statisticsType, LocalDateTime startDate,
            LocalDateTime endDate) {
        if (statisticsType == null || statisticsType.trim().isEmpty()) {
            throw new IllegalArgumentException("统计类型不能为空");
        }

        // 验证统计类型是否有效
        if (!isValidStatisticsType(statisticsType)) {
            throw new IllegalArgumentException("不支持的统计类型: " + statisticsType);
        }

        // 如果提供了自定义时间范围，进行验证
        if (startDate != null && endDate != null) {
            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("开始时间不能晚于结束时间");
            }

            // 限制查询范围不超过1年
            if (startDate.isBefore(LocalDateTime.now().minusYears(1))) {
                throw new IllegalArgumentException("查询时间范围不能超过1年");
            }
        }
    }

    /**
     * 检查统计类型是否有效
     */
    private boolean isValidStatisticsType(String statisticsType) {
        return "daily".equals(statisticsType) || "weekly".equals(statisticsType) || "monthly".equals(statisticsType);
    }

    /**
     * 通过网关获取用户信息
     */
    private String getUserInfoFromGateway(Long userId) {
        try {
            // 严格按照架构规范，所有服务间调用必须通过API网关
            return gatewayServiceClient.callCommonService(
                    "/api/v1/user/info/" + userId,
                    org.springframework.http.HttpMethod.GET,
                    null,
                    String.class).getData();
        } catch (Exception e) {
            log.warn("[消费统计Manager] 获取用户信息失败, userId={}, error={}", userId, e.getMessage());
            // 降级处理：用户信息获取失败不影响统计功能
            return "用户" + userId;
        }
    }

    /**
     * 获取时间范围
     */
    private TimeRange getTimeRange(String statisticsType, LocalDateTime startDate, LocalDateTime endDate) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        switch (statisticsType) {
            case "daily":
                return new TimeRange(
                        today.atStartOfDay(),
                        today.atTime(LocalTime.MAX),
                        today.minusDays(today.getDayOfWeek().getValue() - 1).atStartOfDay(),
                        today.plusDays(7 - today.getDayOfWeek().getValue()).atTime(LocalTime.MAX),
                        today.withDayOfMonth(1).atStartOfDay(),
                        today.withDayOfMonth(today.lengthOfMonth()).atTime(LocalTime.MAX));

            case "weekly":
                // 自定义周范围或当前周
                if (startDate != null && endDate != null) {
                    return new TimeRange(
                            startDate,
                            endDate,
                            startDate,
                            endDate,
                            today.withDayOfMonth(1).atStartOfDay(),
                            today.withDayOfMonth(today.lengthOfMonth()).atTime(LocalTime.MAX));
                } else {
                    LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
                    LocalDate weekEnd = weekStart.plusDays(6);
                    return new TimeRange(
                            weekStart.atStartOfDay(),
                            weekEnd.atTime(LocalTime.MAX),
                            weekStart.atStartOfDay(),
                            weekEnd.atTime(LocalTime.MAX),
                            today.withDayOfMonth(1).atStartOfDay(),
                            today.withDayOfMonth(today.lengthOfMonth()).atTime(LocalTime.MAX));
                }

            case "monthly":
                // 自定义月范围或当前月
                if (startDate != null && endDate != null) {
                    return new TimeRange(
                            startDate,
                            endDate,
                            today.minusDays(today.getDayOfWeek().getValue() - 1).atStartOfDay(),
                            today.plusDays(7 - today.getDayOfWeek().getValue()).atTime(LocalTime.MAX),
                            startDate,
                            endDate);
                } else {
                    return new TimeRange(
                            today.withDayOfMonth(1).atStartOfDay(),
                            today.withDayOfMonth(today.lengthOfMonth()).atTime(LocalTime.MAX),
                            today.minusDays(today.getDayOfWeek().getValue() - 1).atStartOfDay(),
                            today.plusDays(7 - today.getDayOfWeek().getValue()).atTime(LocalTime.MAX),
                            today.withDayOfMonth(1).atStartOfDay(),
                            today.withDayOfMonth(today.lengthOfMonth()).atTime(LocalTime.MAX));
                }

            default:
                throw new IllegalArgumentException("不支持的统计类型: " + statisticsType);
        }
    }

    /**
     * 根据时间范围获取消费记录（通过网关调用consume-service）
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getConsumeRecordsByDateRange(Long userId, LocalDateTime startTime,
            LocalDateTime endTime) {
        try {
            // 构建查询参数
            Map<String, Object> params = new HashMap<>();
            params.put("userId", userId);
            params.put("startTime", startTime.toString());
            params.put("endTime", endTime.toString());

            // 通过网关调用consume-service获取消费记录
            return gatewayServiceClient.callConsumeService(
                    "/api/v1/consume/records/query",
                    org.springframework.http.HttpMethod.POST,
                    params,
                    List.class).getData();
        } catch (Exception e) {
            log.error("[消费统计Manager] 查询消费记录失败, userId={}, startTime={}, endTime={}, error={}",
                    userId, startTime, endTime, e.getMessage(), e);
            // 返回空列表，不影响其他统计
            return List.of();
        }
    }

    /**
     * 构建统计数据
     */
    private MobileConsumeStatisticsVO buildStatistics(List<Map<String, Object>> todayRecords,
            List<Map<String, Object>> weekRecords,
            List<Map<String, Object>> monthRecords) {
        MobileConsumeStatisticsVO statistics = new MobileConsumeStatisticsVO();

        // 今日统计
        @SuppressWarnings("null")
        BigDecimal todayAmount = todayRecords.stream()
                .map(record -> new BigDecimal(record.getOrDefault("consumeAmount", "0").toString()))
                .reduce(BigDecimal.ZERO, (a, b) -> a != null && b != null ? a.add(b)
                        : (a != null ? a : (b != null ? b : BigDecimal.ZERO)));
        statistics.setTodayConsumeCount(todayRecords.size());
        statistics.setTodayConsumeAmount(todayAmount);

        // 本周统计
        @SuppressWarnings("null")
        BigDecimal weekAmount = weekRecords.stream()
                .map(record -> new BigDecimal(record.getOrDefault("consumeAmount", "0").toString()))
                .reduce(BigDecimal.ZERO, (a, b) -> a != null && b != null ? a.add(b)
                        : (a != null ? a : (b != null ? b : BigDecimal.ZERO)));
        statistics.setWeekConsumeCount(weekRecords.size());
        statistics.setWeekConsumeAmount(weekAmount);

        // 本月统计
        @SuppressWarnings("null")
        BigDecimal monthAmount = monthRecords.stream()
                .map(record -> new BigDecimal(record.getOrDefault("consumeAmount", "0").toString()))
                .reduce(BigDecimal.ZERO, (a, b) -> a != null && b != null ? a.add(b)
                        : (a != null ? a : (b != null ? b : BigDecimal.ZERO)));
        statistics.setMonthConsumeCount(monthRecords.size());
        statistics.setMonthConsumeAmount(monthAmount);

        return statistics;
    }

    /**
     * 时间范围内部类
     */
    private static final class TimeRange {
        private final LocalDateTime todayStart;
        private final LocalDateTime todayEnd;
        private final LocalDateTime weekStart;
        private final LocalDateTime weekEnd;
        private final LocalDateTime monthStart;
        private final LocalDateTime monthEnd;

        private TimeRange(LocalDateTime todayStart,
                LocalDateTime todayEnd,
                LocalDateTime weekStart,
                LocalDateTime weekEnd,
                LocalDateTime monthStart,
                LocalDateTime monthEnd) {
            this.todayStart = todayStart;
            this.todayEnd = todayEnd;
            this.weekStart = weekStart;
            this.weekEnd = weekEnd;
            this.monthStart = monthStart;
            this.monthEnd = monthEnd;
        }

        private LocalDateTime getTodayStart() {
            return todayStart;
        }

        private LocalDateTime getTodayEnd() {
            return todayEnd;
        }

        private LocalDateTime getWeekStart() {
            return weekStart;
        }

        private LocalDateTime getWeekEnd() {
            return weekEnd;
        }

        private LocalDateTime getMonthStart() {
            return monthStart;
        }

        private LocalDateTime getMonthEnd() {
            return monthEnd;
        }
    }
}
