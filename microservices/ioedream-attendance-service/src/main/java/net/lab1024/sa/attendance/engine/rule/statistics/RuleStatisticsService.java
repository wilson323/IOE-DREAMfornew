package net.lab1024.sa.attendance.engine.rule.statistics;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.engine.rule.model.RuleExecutionStatistics;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 规则统计服务
 * <p>
 * 负责规则执行统计的收集、计算和查询
 * 严格遵循CLAUDE.md全局架构规范,纯Java类
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
public class RuleStatisticsService {

    // 规则执行统计
    private final Map<String, Long> executionStatistics = new ConcurrentHashMap<>();

    /**
     * 获取执行统计信息
     *
     * @param startTime 开始时间戳
     * @param endTime   结束时间戳
     * @return 统计信息
     */
    public RuleExecutionStatistics getExecutionStatistics(long startTime, long endTime) {
        log.debug("[规则统计服务] 获取执行统计, startTime={}, endTime={}", startTime, endTime);

        try {
            RuleExecutionStatistics statistics = RuleExecutionStatistics.builder()
                    .startTime(startTime)
                    .endTime(endTime)
                    .totalExecutions(getStatisticsValue("totalExecutions"))
                    .successfulExecutions(getStatisticsValue("successfulExecutions"))
                    .failedExecutions(getStatisticsValue("failedExecutions"))
                    .averageEvaluationTime(calculateAverageEvaluationTime())
                    .statisticsTimestamp(LocalDateTime.now())
                    .build();

            log.debug("[规则统计服务] 执行统计查询完成: total={}, success={}, failed={}",
                    statistics.getTotalExecutions(),
                    statistics.getSuccessfulExecutions(),
                    statistics.getFailedExecutions());

            return statistics;

        } catch (Exception e) {
            log.error("[规则统计服务] 获取执行统计失败", e);

            return RuleExecutionStatistics.builder()
                    .startTime(startTime)
                    .endTime(endTime)
                    .totalExecutions(0L)
                    .successfulExecutions(0L)
                    .failedExecutions(0L)
                    .averageEvaluationTime(0.0)
                    .statisticsTimestamp(LocalDateTime.now())
                    .build();
        }
    }

    /**
     * 更新执行统计
     *
     * @param resultType 结果类型 (SUCCESS, FAILED, ERROR等)
     */
    public void updateExecutionStatistics(String resultType) {
        log.debug("[规则统计服务] 更新执行统计: resultType={}", resultType);

        try {
            // 增加总执行次数
            Long totalExecutions = getStatisticsValue("totalExecutions");
            setStatisticsValue("totalExecutions", totalExecutions + 1);

            // 根据结果类型更新对应统计
            if ("SUCCESS".equals(resultType)) {
                Long successCount = getStatisticsValue("successfulExecutions");
                setStatisticsValue("successfulExecutions", successCount + 1);
            } else if ("FAILED".equals(resultType) || "ERROR".equals(resultType)) {
                Long failedCount = getStatisticsValue("failedExecutions");
                setStatisticsValue("failedExecutions", failedCount + 1);
            }

        } catch (Exception e) {
            log.error("[规则统计服务] 更新执行统计失败", e);
        }
    }

    /**
     * 获取统计值
     *
     * @param key 统计键
     * @return 统计值
     */
    public Long getStatisticsValue(String key) {
        return executionStatistics.getOrDefault(key, 0L);
    }

    /**
     * 设置统计值
     *
     * @param key   统计键
     * @param value 统计值
     */
    public void setStatisticsValue(String key, Long value) {
        executionStatistics.put(key, value);
    }

    /**
     * 计算平均评估时间
     *
     * @return 平均评估时间 (毫秒)
     */
    private Double calculateAverageEvaluationTime() {
        Long totalTime = getStatisticsValue("totalEvaluationTime");
        Long totalCount = getStatisticsValue("totalExecutions");

        if (totalCount == 0) {
            return 0.0;
        }

        return (double) totalTime / totalCount;
    }

    /**
     * 重置统计信息
     */
    public void resetStatistics() {
        log.info("[规则统计服务] 重置统计信息");
        executionStatistics.clear();
    }

    /**
     * 获取所有统计信息
     *
     * @return 统计信息Map
     */
    public Map<String, Long> getAllStatistics() {
        return new ConcurrentHashMap<>(executionStatistics);
    }
}
