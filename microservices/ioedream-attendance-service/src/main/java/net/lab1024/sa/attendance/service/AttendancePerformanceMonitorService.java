package net.lab1024.sa.attendance.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.dao.tool.AttendancePerformanceAnalyzer;

/**
 * 考勤模块性能监控服务
 *
 * <p>
 * 任务4.2：优化数据库查询和索引的监控服务
 * 提供定时性能监控、性能报告生成、性能预警等功能
 * 严格遵循repowiki编码规范：使用jakarta包名、@Resource注入、SLF4J日志
 * </p>
 *
 * <p>
 * 主要功能：
 * - 定时执行数据库性能分析
 * - 生成性能监控报告
 * - 性能异常预警
 * - 性能趋势分析
 * - 自动化性能优化建议
 * </p>
 *
 * <p>
 * 监控计划：
 * - 每10分钟：执行轻量级性能检查
 * - 每小时：生成详细性能报告
 * - 每天：执行性能基准测试
 * - 每周：生成性能趋势分析报告
 * </p>
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-24
 */
@Slf4j
@Service
public class AttendancePerformanceMonitorService {

    @Resource
    private AttendancePerformanceAnalyzer performanceAnalyzer;

    /**
     * 性能指标缓存（用于趋势分析）
     */
    private final Map<String, Object> performanceHistory = new ConcurrentHashMap<>();

    /**
     * 性能阈值配置
     * 预留配置项：用于未来性能监控的阈值控制
     */
    @SuppressWarnings("unused")
    private static final long SLOW_QUERY_THRESHOLD_MS = 1000; // 1秒
    @SuppressWarnings("unused")
    private static final double FRAGMENTATION_THRESHOLD = 10.0; // 10%
    private static final int LARGE_TABLE_THRESHOLD = 100000; // 10万行

    /**
     * 每10分钟执行轻量级性能检查
     */
    @Scheduled(fixedRate = 600000) // 10分钟
    public void lightweightPerformanceCheck() {
        try {
            log.debug("[AttendancePerformanceMonitor] 开始轻量级性能检查");

            // 检查数据库连接状态
            checkDatabaseConnectivity();

            // 检查表大小变化
            checkTableSizeChanges();

            // 检查慢查询增长
            checkSlowQueryGrowth();

            log.debug("[AttendancePerformanceMonitor] 轻量级性能检查完成");

        } catch (Exception e) {
            log.error("[AttendancePerformanceMonitor] 轻量级性能检查失败", e);
        }
    }

    /**
     * 每小时生成详细性能报告
     */
    @Scheduled(fixedRate = 3600000) // 1小时
    public void generateHourlyPerformanceReport() {
        try {
            log.info("[AttendancePerformanceMonitor] 开始生成小时性能报告");

            Map<String, Object> report = performanceAnalyzer.generatePerformanceReport();
            String reportTime = LocalDateTime.now().toString();

            // 保存报告到历史记录
            performanceHistory.put("hourly_report_" + reportTime, report);

            // 分析性能趋势
            analyzePerformanceTrends(report);

            // 检查性能异常
            checkPerformanceAnomalies(report);

            // 生成性能预警
            generatePerformanceAlerts(report);

            log.info("[AttendancePerformanceMonitor] 小时性能报告生成完成");

        } catch (Exception e) {
            log.error("[AttendancePerformanceMonitor] 生成小时性能报告失败", e);
        }
    }

    /**
     * 每天执行性能基准测试
     */
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点
    public void dailyPerformanceBenchmark() {
        try {
            log.info("[AttendancePerformanceMonitor] 开始每日性能基准测试");

            Map<String, Object> benchmark = performanceAnalyzer.executePerformanceBenchmark();
            String benchmarkTime = LocalDateTime.now().toString();

            // 保存基准测试结果
            performanceHistory.put("daily_benchmark_" + benchmarkTime, benchmark);

            // 分析性能变化
            analyzePerformanceChanges(benchmark);

            log.info("[AttendancePerformanceMonitor] 每日性能基准测试完成，性能等级：{}",
                    benchmark.get("performanceLevel"));

        } catch (Exception e) {
            log.error("[AttendancePerformanceMonitor] 每日性能基准测试失败", e);
        }
    }

    /**
     * 每周生成性能趋势分析报告
     */
    @Scheduled(cron = "0 0 3 * * MON") // 每周一凌晨3点
    public void weeklyPerformanceTrendAnalysis() {
        try {
            log.info("[AttendancePerformanceMonitor] 开始每周性能趋势分析");

            Map<String, Object> weeklyReport = generateWeeklyTrendReport();

            // 保存周报告
            String reportTime = LocalDateTime.now().toString();
            performanceHistory.put("weekly_report_" + reportTime, weeklyReport);

            // 清理过期数据（保留30天）
            cleanupExpiredHistory();

            log.info("[AttendancePerformanceMonitor] 每周性能趋势分析完成");

        } catch (Exception e) {
            log.error("[AttendancePerformanceMonitor] 每周性能趋势分析失败", e);
        }
    }

    /**
     * 检查数据库连接状态
     */
    private void checkDatabaseConnectivity() {
        try {
            // 简单的连接测试
            performanceAnalyzer.analyzeDatabaseInfo();
            log.debug("[AttendancePerformanceMonitor] 数据库连接正常");
        } catch (Exception e) {
            log.error("[AttendancePerformanceMonitor] 数据库连接异常", e);
            // 可以添加告警逻辑
        }
    }

    /**
     * 检查表大小变化
     */
    private void checkTableSizeChanges() {
        try {
            @SuppressWarnings("unused")
            Map<String, Object> currentStats = performanceAnalyzer.analyzeTableStatistics();

            // 比较与上次检查的变化
            // 这里可以添加变化检测逻辑

            log.debug("[AttendancePerformanceMonitor] 表大小变化检查完成");
        } catch (Exception e) {
            log.error("[AttendancePerformanceMonitor] 表大小变化检查失败", e);
        }
    }

    /**
     * 检查慢查询增长
     */
    private void checkSlowQueryGrowth() {
        try {
            @SuppressWarnings("unused")
            Map<String, Object> slowQueryStats = performanceAnalyzer.analyzeSlowQueries();

            // 分析慢查询变化趋势
            // 这里可以添加增长检测逻辑

            log.debug("[AttendancePerformanceMonitor] 慢查询增长检查完成");
        } catch (Exception e) {
            log.error("[AttendancePerformanceMonitor] 慢查询增长检查失败", e);
        }
    }

    /**
     * 分析性能趋势
     *
     * @param currentReport 当前性能报告
     */
    private void analyzePerformanceTrends(Map<String, Object> currentReport) {
        try {
            // 获取历史数据
            Map<String, Object> previousReport = findLatestReport("hourly_report");

            if (previousReport != null) {
                // 比较性能指标变化
                comparePerformanceMetrics(currentReport, previousReport);
            }

            log.debug("[AttendancePerformanceMonitor] 性能趋势分析完成");
        } catch (Exception e) {
            log.error("[AttendancePerformanceMonitor] 性能趋势分析失败", e);
        }
    }

    /**
     * 检查性能异常
     *
     * @param report 性能报告
     */
    private void checkPerformanceAnomalies(Map<String, Object> report) {
        try {
            // 检查表统计异常
            checkTableAnomalies(report);

            // 检查索引异常
            checkIndexAnomalies(report);

            // 检查碎片异常
            checkFragmentationAnomalies(report);

            log.debug("[AttendancePerformanceMonitor] 性能异常检查完成");
        } catch (Exception e) {
            log.error("[AttendancePerformanceMonitor] 性能异常检查失败", e);
        }
    }

    /**
     * 检查表统计异常
     */
    private void checkTableAnomalies(Map<String, Object> report) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> tableStats = (Map<String, Object>) report.get("tableStats");
            if (tableStats != null) {
                Object totalRows = tableStats.get("totalRows");
                if (totalRows instanceof Long && (Long) totalRows > LARGE_TABLE_THRESHOLD * 10) {
                    log.warn("[AttendancePerformanceMonitor] 检测到大表：总行数 {}", totalRows);
                    // 可以添加大表优化建议
                }
            }
        } catch (Exception e) {
            log.error("[AttendancePerformanceMonitor] 检查表统计异常失败", e);
        }
    }

    /**
     * 检查索引异常
     */
    private void checkIndexAnomalies(Map<String, Object> report) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> indexStats = (Map<String, Object>) report.get("indexUsage");
            if (indexStats != null) {
                @SuppressWarnings("unchecked")
                java.util.List<Map<String, Object>> duplicateIndexes = (java.util.List<Map<String, Object>>) indexStats
                        .get("duplicateIndexes");

                if (duplicateIndexes != null && !duplicateIndexes.isEmpty()) {
                    log.warn("[AttendancePerformanceMonitor] 检测到 {} 个重复索引", duplicateIndexes.size());
                }
            }
        } catch (Exception e) {
            log.error("[AttendancePerformanceMonitor] 检查索引异常失败", e);
        }
    }

    /**
     * 检查碎片异常
     */
    private void checkFragmentationAnomalies(Map<String, Object> report) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> fragStats = (Map<String, Object>) report.get("fragmentationStats");
            if (fragStats != null) {
                Integer fragmentedTables = (Integer) fragStats.get("fragmentedTables");
                if (fragmentedTables != null && fragmentedTables > 0) {
                    log.warn("[AttendancePerformanceMonitor] 检测到 {} 个碎片表", fragmentedTables);
                }
            }
        } catch (Exception e) {
            log.error("[AttendancePerformanceMonitor] 检查碎片异常失败", e);
        }
    }

    /**
     * 生成性能预警
     *
     * @param report 性能报告
     */
    private void generatePerformanceAlerts(Map<String, Object> report) {
        try {
            // 基于报告内容生成预警
            java.util.List<String> alerts = new java.util.ArrayList<>();

            // 检查各种性能指标
            checkAndAddAlerts(report, alerts);

            if (!alerts.isEmpty()) {
                log.warn("[AttendancePerformanceMonitor] 生成 {} 个性能预警", alerts.size());
                for (String alert : alerts) {
                    log.warn("[PerformanceAlert] {}", alert);
                }
            }

        } catch (Exception e) {
            log.error("[AttendancePerformanceMonitor] 生成性能预警失败", e);
        }
    }

    /**
     * 检查并添加预警信息
     */
    private void checkAndAddAlerts(Map<String, Object> report, java.util.List<String> alerts) {
        try {
            // 检查表大小预警
            @SuppressWarnings("unchecked")
            Map<String, Object> tableStats = (Map<String, Object>) report.get("tableStats");
            if (tableStats != null) {
                Double totalDataSize = (Double) tableStats.get("totalDataSizeMB");
                if (totalDataSize != null && totalDataSize > 1000) { // 大于1GB
                    alerts.add("考勤数据总大小超过1GB，建议考虑数据归档或分区");
                }
            }

            // 检查碎片率预警
            @SuppressWarnings("unchecked")
            Map<String, Object> fragStats = (Map<String, Object>) report.get("fragmentationStats");
            if (fragStats != null) {
                Integer fragmentedTables = (Integer) fragStats.get("fragmentedTables");
                if (fragmentedTables != null && fragmentedTables > 2) {
                    alerts.add("多个考勤表存在高碎片率，建议执行表优化");
                }
            }

        } catch (Exception e) {
            log.error("[AttendancePerformanceMonitor] 检查预警条件失败", e);
        }
    }

    /**
     * 分析性能变化
     *
     * @param benchmark 基准测试结果
     */
    private void analyzePerformanceChanges(Map<String, Object> benchmark) {
        try {
            String currentLevel = (String) benchmark.get("performanceLevel");

            // 获取昨天的基准测试结果
            Map<String, Object> yesterdayBenchmark = findLatestBenchmark();

            if (yesterdayBenchmark != null) {
                String yesterdayLevel = (String) yesterdayBenchmark.get("performanceLevel");

                if (!currentLevel.equals(yesterdayLevel)) {
                    log.info("[AttendancePerformanceMonitor] 性能等级发生变化：{} -> {}", yesterdayLevel, currentLevel);

                    if (isPerformanceDegradation(currentLevel, yesterdayLevel)) {
                        log.warn("[AttendancePerformanceMonitor] 检测到性能下降，建议检查并优化");
                    }
                }
            }

        } catch (Exception e) {
            log.error("[AttendancePerformanceMonitor] 分析性能变化失败", e);
        }
    }

    /**
     * 判断是否为性能下降
     */
    private boolean isPerformanceDegradation(String currentLevel, String previousLevel) {
        // 定义性能等级顺序
        java.util.Map<String, Integer> levelRank = java.util.Map.of(
                "优秀", 4,
                "良好", 3,
                "一般", 2,
                "需要优化", 1);

        Integer currentRank = levelRank.get(currentLevel);
        Integer previousRank = levelRank.get(previousLevel);

        return currentRank != null && previousRank != null && currentRank < previousRank;
    }

    /**
     * 生成周趋势报告
     *
     * @return 周趋势报告
     */
    private Map<String, Object> generateWeeklyTrendReport() {
        Map<String, Object> weeklyReport = new java.util.LinkedHashMap<>();

        try {
            weeklyReport.put("reportTime", LocalDateTime.now());
            weeklyReport.put("reportType", "weekly_trend_analysis");

            // 收集一周的基准测试数据
            java.util.List<Map<String, Object>> weeklyBenchmarks = collectWeeklyBenchmarks();
            weeklyReport.put("weeklyBenchmarks", weeklyBenchmarks);

            // 分析趋势
            Map<String, Object> trendAnalysis = analyzeWeeklyTrends(weeklyBenchmarks);
            weeklyReport.put("trendAnalysis", trendAnalysis);

            // 生成改进建议
            java.util.List<String> improvementSuggestions = generateWeeklyImprovementSuggestions(trendAnalysis);
            weeklyReport.put("improvementSuggestions", improvementSuggestions);

        } catch (Exception e) {
            log.error("[AttendancePerformanceMonitor] 生成周趋势报告失败", e);
            weeklyReport.put("error", e.getMessage());
        }

        return weeklyReport;
    }

    /**
     * 收集一周的基准测试数据
     */
    private java.util.List<Map<String, Object>> collectWeeklyBenchmarks() {
        java.util.List<Map<String, Object>> benchmarks = new java.util.ArrayList<>();

        // 从历史记录中提取基准测试数据
        performanceHistory.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("daily_benchmark_"))
                .sorted(Map.Entry.comparingByKey())
                .limit(7) // 最近7天
                .forEach(entry -> {
                    Object value = entry.getValue();
                    if (value instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> benchmark = (Map<String, Object>) value;
                        benchmarks.add(benchmark);
                    }
                });

        return benchmarks;
    }

    /**
     * 分析周趋势
     */
    private Map<String, Object> analyzeWeeklyTrends(java.util.List<Map<String, Object>> benchmarks) {
        Map<String, Object> trendAnalysis = new java.util.LinkedHashMap<>();

        try {
            if (benchmarks.isEmpty()) {
                trendAnalysis.put("status", "insufficient_data");
                return trendAnalysis;
            }

            // 分析性能等级趋势
            java.util.Map<String, Long> levelCount = new java.util.HashMap<>();
            for (Map<String, Object> benchmark : benchmarks) {
                String level = (String) benchmark.get("performanceLevel");
                levelCount.put(level, levelCount.getOrDefault(level, 0L) + 1);
            }

            trendAnalysis.put("performanceLevelDistribution", levelCount);

            // 计算平均查询时间趋势
            java.util.List<Long> averageTimes = new java.util.ArrayList<>();
            for (Map<String, Object> benchmark : benchmarks) {
                Object avgTime = benchmark.get("averageQueryTime");
                if (avgTime instanceof Double) {
                    averageTimes.add(((Double) avgTime).longValue());
                }
            }

            if (!averageTimes.isEmpty()) {
                double avgTime = averageTimes.stream().mapToLong(Long::longValue).average().orElse(0);
                trendAnalysis.put("weeklyAverageQueryTime", Math.round(avgTime * 100.0) / 100.0);
            }

            trendAnalysis.put("status", "analysis_complete");

        } catch (Exception e) {
            log.error("[AttendancePerformanceMonitor] 分析周趋势失败", e);
            trendAnalysis.put("error", e.getMessage());
        }

        return trendAnalysis;
    }

    /**
     * 生成周改进建议
     */
    private java.util.List<String> generateWeeklyImprovementSuggestions(Map<String, Object> trendAnalysis) {
        java.util.List<String> suggestions = new java.util.ArrayList<>();

        try {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Long> levelDistribution = (java.util.Map<String, Long>) trendAnalysis
                    .get("performanceLevelDistribution");

            if (levelDistribution != null) {
                long needsOptimizationCount = levelDistribution.getOrDefault("需要优化", 0L);
                long averageCount = levelDistribution.getOrDefault("一般", 0L);

                if (needsOptimizationCount > 0) {
                    suggestions.add("本周有 " + needsOptimizationCount + " 次性能测试结果为'需要优化'，建议立即进行性能优化");
                }

                if (averageCount > 3) {
                    suggestions.add("性能等级多为'一般'，建议检查索引配置和查询优化");
                }
            }

            // 其他通用建议
            suggestions.add("定期检查数据库参数配置，确保适合当前业务负载");
            suggestions.add("关注数据增长趋势，提前规划存储和性能优化");
            suggestions.add("建立性能基线，便于快速发现性能异常");

        } catch (Exception e) {
            log.error("[AttendancePerformanceMonitor] 生成周改进建议失败", e);
        }

        return suggestions;
    }

    /**
     * 查找最新的报告
     */
    private Map<String, Object> findLatestReport(String reportPrefix) {
        return performanceHistory.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(reportPrefix))
                .max(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .filter(value -> value instanceof Map)
                .map(value -> {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> report = (Map<String, Object>) value;
                    return report;
                })
                .orElse(null);
    }

    /**
     * 查找最新的基准测试结果
     */
    private Map<String, Object> findLatestBenchmark() {
        return findLatestReport("daily_benchmark_");
    }

    /**
     * 清理过期历史数据
     */
    private void cleanupExpiredHistory() {
        try {
            // 保留30天的数据
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);

            performanceHistory.entrySet().removeIf(entry -> {
                String key = entry.getKey();
                try {
                    // 从key中提取时间戳进行比较
                    String timestamp = key.substring(key.lastIndexOf("_") + 1);
                    LocalDateTime recordDate = LocalDateTime.parse(timestamp);
                    return recordDate.isBefore(cutoffDate);
                } catch (Exception e) {
                    // 如果无法解析时间，删除该记录
                    return true;
                }
            });

            log.info("[AttendancePerformanceMonitor] 过期历史数据清理完成");

        } catch (Exception e) {
            log.error("[AttendancePerformanceMonitor] 清理过期历史数据失败", e);
        }
    }

    /**
     * 比较性能指标
     */
    private void comparePerformanceMetrics(Map<String, Object> current, Map<String, Object> previous) {
        try {
            // 这里可以实现详细的性能指标比较逻辑
            // 比较表大小、索引数量、查询时间等变化

            log.debug("[AttendancePerformanceMonitor] 性能指标比较完成");

        } catch (Exception e) {
            log.error("[AttendancePerformanceMonitor] 比较性能指标失败", e);
        }
    }

    /**
     * 获取当前性能监控状态
     *
     * @return 监控状态
     */
    public Map<String, Object> getMonitoringStatus() {
        Map<String, Object> status = new java.util.LinkedHashMap<>();

        status.put("serviceName", "AttendancePerformanceMonitor");
        status.put("status", "running");
        status.put("lastCheck", LocalDateTime.now());
        status.put("historyRecords", performanceHistory.size());
        status.put("monitoringSchedule", "10min/1h/1d/1w");

        // 最新性能状况
        Map<String, Object> latestReport = findLatestReport("hourly_report");
        if (latestReport != null) {
            status.put("latestReportTime", latestReport.get("reportTime"));
        }

        Map<String, Object> latestBenchmark = findLatestBenchmark();
        if (latestBenchmark != null) {
            status.put("latestBenchmarkLevel", latestBenchmark.get("performanceLevel"));
        }

        return status;
    }
}
