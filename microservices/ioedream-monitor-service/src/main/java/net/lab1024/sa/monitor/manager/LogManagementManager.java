package net.lab1024.sa.monitor.manager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.monitor.dao.SystemLogDao;
import net.lab1024.sa.monitor.domain.entity.SystemLogEntity;

/**
 * 日志管理管理器
 *
 * 负责系统日志收集、存储、查询和分析等功能
 *
 * @author IOE-DREAM Team
 */
@Slf4j
@Component
public class LogManagementManager {

    @Resource
    private SystemLogDao systemLogDao;

    // 异步执行器
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    /**
     * 收集系统日志
     *
     * @param logEntity 日志实体
     */
    public void collectLog(SystemLogEntity logEntity) {
        log.debug("收集系统日志，级别：{}，服务：{}", logEntity.getLogLevel(), logEntity.getServiceName());

        try {
            // 异步保存日志
            CompletableFuture.runAsync(() -> {
                try {
                    systemLogDao.insert(logEntity);
                } catch (Exception e) {
                    log.error("保存系统日志失败", e);
                }
            }, executorService);

        } catch (Exception e) {
            log.error("收集系统日志异常", e);
        }
    }

    /**
     * 批量收集系统日志
     *
     * @param logList 日志列表
     */
    public void batchCollectLogs(List<SystemLogEntity> logList) {
        log.info("批量收集系统日志，数量：{}", logList.size());

        try {
            if (!logList.isEmpty()) {
                systemLogDao.batchInsert(logList);
                log.debug("批量收集系统日志完成，数量：{}", logList.size());
            }
        } catch (Exception e) {
            log.error("批量收集系统日志失败", e);
        }
    }

    /**
     * 查询系统日志
     *
     * @param logLevel    日志级别
     * @param serviceName 服务名称
     * @param startTime   开始时间
     * @param endTime     结束时间
     * @param limit       限制数量
     * @return 日志列表
     */
    public List<SystemLogEntity> queryLogs(String logLevel, String serviceName,
            LocalDateTime startTime, LocalDateTime endTime,
            Integer limit) {
        log.debug("查询系统日志，级别：{}，服务：{}，时间范围：{} - {}",
                logLevel, serviceName, startTime, endTime);

        try {
            if (logLevel != null && !logLevel.isEmpty()) {
                return systemLogDao.selectByLogLevel(logLevel, startTime, endTime, limit);
            } else if (serviceName != null && !serviceName.isEmpty()) {
                return systemLogDao.selectByServiceName(serviceName, startTime, endTime, limit);
            } else {
                // 默认查询所有日志
                return searchLogs("", startTime, endTime, limit);
            }
        } catch (Exception e) {
            log.error("查询系统日志失败", e);
            return List.of();
        }
    }

    /**
     * 搜索日志内容
     *
     * @param keyword   搜索关键词
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param limit     限制数量
     * @return 日志列表
     */
    public List<SystemLogEntity> searchLogs(String keyword, LocalDateTime startTime,
            LocalDateTime endTime, Integer limit) {
        log.debug("搜索日志内容，关键词：{}，时间范围：{} - {}", keyword, startTime, endTime);

        try {
            return systemLogDao.searchLogs(keyword, startTime, endTime, limit);
        } catch (Exception e) {
            log.error("搜索日志内容失败", e);
            return List.of();
        }
    }

    /**
     * 根据请求ID查询日志
     *
     * @param requestId 请求ID
     * @return 日志列表
     */
    public List<SystemLogEntity> queryLogsByRequestId(String requestId) {
        log.debug("根据请求ID查询日志，请求ID：{}", requestId);

        try {
            return systemLogDao.selectByRequestId(requestId);
        } catch (Exception e) {
            log.error("根据请求ID查询日志失败", e);
            return List.of();
        }
    }

    /**
     * 根据追踪ID查询日志
     *
     * @param traceId 追踪ID
     * @return 日志列表
     */
    public List<SystemLogEntity> queryLogsByTraceId(String traceId) {
        log.debug("根据追踪ID查询日志，追踪ID：{}", traceId);

        try {
            return systemLogDao.selectByTraceId(traceId);
        } catch (Exception e) {
            log.error("根据追踪ID查询日志失败", e);
            return List.of();
        }
    }

    /**
     * 根据用户ID查询日志
     *
     * @param userId    用户ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param limit     限制数量
     * @return 日志列表
     */
    public List<SystemLogEntity> queryLogsByUserId(Long userId, LocalDateTime startTime,
            LocalDateTime endTime, Integer limit) {
        log.debug("根据用户ID查询日志，用户ID：{}", userId);

        try {
            return systemLogDao.selectByUserId(userId, startTime, endTime, limit);
        } catch (Exception e) {
            log.error("根据用户ID查询日志失败", e);
            return List.of();
        }
    }

    /**
     * 获取日志统计信息
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计信息
     */
    public Map<String, Object> getLogStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        log.debug("获取日志统计信息，时间范围：{} - {}", startTime, endTime);

        try {
            Map<String, Object> statistics = new java.util.HashMap<>();

            // 按级别统计
            List<Map<String, Object>> levelStats = systemLogDao.countLogsByLevel(startTime, endTime);
            statistics.put("levelStatistics", levelStats);

            // 按类型统计
            List<Map<String, Object>> typeStats = systemLogDao.countLogsByType(startTime, endTime);
            statistics.put("typeStatistics", typeStats);

            // 按服务统计
            List<Map<String, Object>> serviceStats = systemLogDao.countLogsByService(startTime, endTime);
            statistics.put("serviceStatistics", serviceStats);

            // 计算总量
            long totalLogs = levelStats.stream()
                    .mapToLong(stat -> (Long) stat.get("count"))
                    .sum();
            statistics.put("totalLogs", totalLogs);

            // 错误日志趋势
            List<Map<String, Object>> errorTrends = systemLogDao.selectErrorLogTrends(24);
            statistics.put("errorTrends", errorTrends);

            statistics.put("statisticsTime", LocalDateTime.now());

            return statistics;

        } catch (Exception e) {
            log.error("获取日志统计信息失败", e);
            return new java.util.HashMap<>();
        }
    }

    /**
     * 获取错误日志分析
     *
     * @param hours 时间范围（小时）
     * @return 错误日志分析结果
     */
    public Map<String, Object> analyzeErrorLogs(Integer hours) {
        log.debug("分析错误日志，时间范围：{}小时", hours);

        try {
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusHours(hours);

            Map<String, Object> analysis = new java.util.HashMap<>();

            // 查询错误日志
            List<SystemLogEntity> errorLogs = systemLogDao.selectByLogLevel("ERROR", startTime, endTime, 1000);

            analysis.put("errorCount", errorLogs.size());
            analysis.put("analysisTime", LocalDateTime.now());

            // 按错误类型分组
            Map<String, Long> errorTypeCount = new java.util.HashMap<>();
            for (SystemLogEntity errorLog : errorLogs) {
                String exceptionType = extractExceptionType(errorLog.getExceptionInfo());
                errorTypeCount.put(exceptionType, errorTypeCount.getOrDefault(exceptionType, 0L) + 1);
            }

            // 找出最常见的错误
            List<Map<String, Object>> topErrors = errorTypeCount.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(10)
                    .map(entry -> {
                        Map<String, Object> error = new java.util.HashMap<>();
                        error.put("type", entry.getKey());
                        error.put("count", entry.getValue());
                        return error;
                    })
                    .toList();

            analysis.put("topErrors", topErrors);

            // 按服务分组
            Map<String, Long> errorServiceCount = new java.util.HashMap<>();
            for (SystemLogEntity errorLog : errorLogs) {
                String service = errorLog.getServiceName();
                errorServiceCount.put(service, errorServiceCount.getOrDefault(service, 0L) + 1);
            }

            analysis.put("errorByService", errorServiceCount);

            // 生成建议
            List<String> recommendations = generateErrorRecommendations(topErrors);
            analysis.put("recommendations", recommendations);

            return analysis;

        } catch (Exception e) {
            log.error("分析错误日志失败", e);
            return new java.util.HashMap<>();
        }
    }

    /**
     * 清理历史日志数据
     *
     * @param days 保留天数
     */
    public void cleanHistoryLogs(Integer days) {
        log.info("开始清理{}天前的历史日志数据", days);

        try {
            LocalDateTime beforeTime = LocalDateTime.now().minusDays(days);
            int deletedCount = systemLogDao.deleteHistoryLogs(beforeTime);

            log.info("历史日志数据清理完成，删除记录数：{}", deletedCount);

        } catch (Exception e) {
            log.error("清理历史日志数据失败", e);
        }
    }

    /**
     * 获取日志健康度评分
     *
     * @param hours 时间范围（小时）
     * @return 健康度评分（0-100）
     */
    @SuppressWarnings("unchecked")
    public Double getLogHealthScore(Integer hours) {
        log.debug("计算日志健康度评分，时间范围：{}小时", hours);

        try {
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusHours(hours);

            Map<String, Object> stats = getLogStatistics(startTime, endTime);

            long totalLogs = (Long) stats.getOrDefault("totalLogs", 0L);
            List<Map<String, Object>> levelStats = (List<Map<String, Object>>) stats.get("levelStatistics");

            if (levelStats == null) {
                levelStats = new ArrayList<>();
            }

            long errorCount = levelStats.stream()
                    .filter(stat -> "ERROR".equals(stat.get("logLevel")))
                    .mapToLong(stat -> (Long) stat.get("count"))
                    .sum();

            long warnCount = levelStats.stream()
                    .filter(stat -> "WARN".equals(stat.get("logLevel")))
                    .mapToLong(stat -> (Long) stat.get("count"))
                    .sum();

            // 计算健康度评分
            if (totalLogs == 0) {
                return 100.0; // 没有日志，认为是健康的
            }

            double errorRate = (double) errorCount / totalLogs * 100;
            double warnRate = (double) warnCount / totalLogs * 100;

            // 健康度评分 = 100 - 错误率 * 10 - 警告率 * 2
            double healthScore = Math.max(0, 100 - errorRate * 10 - warnRate * 2);

            log.debug("日志健康度评分：{}，错误率：{}%，警告率：{}%", healthScore, errorRate, warnRate);

            return healthScore;

        } catch (Exception e) {
            log.error("计算日志健康度评分失败", e);
            return 50.0; // 默认中等健康度
        }
    }

    // 私有辅助方法

    private String extractExceptionType(String exceptionInfo) {
        if (exceptionInfo == null || exceptionInfo.isEmpty()) {
            return "Unknown";
        }

        // 提取异常类型，例如从 "java.lang.NullPointerException: ..." 中提取 "NullPointerException"
        String[] lines = exceptionInfo.split("\n");
        if (lines.length > 0) {
            String firstLine = lines[0];
            int colonIndex = firstLine.indexOf(':');
            if (colonIndex > 0) {
                String exceptionClass = firstLine.substring(0, colonIndex);
                int lastDotIndex = exceptionClass.lastIndexOf('.');
                if (lastDotIndex > 0) {
                    return exceptionClass.substring(lastDotIndex + 1);
                }
                return exceptionClass;
            }
        }

        return "Unknown";
    }

    private List<String> generateErrorRecommendations(List<Map<String, Object>> topErrors) {
        List<String> recommendations = new java.util.ArrayList<>();

        for (Map<String, Object> error : topErrors) {
            String errorType = (String) error.get("type");
            Long count = (Long) error.get("count");

            if (count > 10) { // 超过10次的需要关注
                if (errorType.contains("NullPointerException")) {
                    recommendations.add("检查空指针异常，加强空值校验");
                } else if (errorType.contains("IOException")) {
                    recommendations.add("检查IO操作，添加异常处理和重试机制");
                } else if (errorType.contains("SQLException")) {
                    recommendations.add("检查数据库连接和SQL语句，优化数据库操作");
                } else if (errorType.contains("Timeout")) {
                    recommendations.add("检查超时设置，优化性能或增加超时时间");
                } else {
                    recommendations.add("重点关注" + errorType + "异常，出现" + count + "次");
                }
            }
        }

        if (recommendations.isEmpty()) {
            recommendations.add("系统运行良好，错误日志数量在正常范围内");
        }

        return recommendations;
    }

    /**
     * 创建日志实体
     *
     * @param logLevel      日志级别
     * @param logType       日志类型
     * @param serviceName   服务名称
     * @param message       日志消息
     * @param exceptionInfo 异常信息
     * @return 日志实体
     */
    public SystemLogEntity createLogEntity(String logLevel, String logType, String serviceName,
            String message, String exceptionInfo) {
        SystemLogEntity logEntity = new SystemLogEntity();

        logEntity.setLogLevel(logLevel);
        logEntity.setLogType(logType != null ? logType : "APPLICATION");
        logEntity.setServiceName(serviceName != null ? serviceName : "unknown-service");
        logEntity.setMessage(message);
        logEntity.setExceptionInfo(exceptionInfo);
        logEntity.setLogTime(LocalDateTime.now());
        logEntity.setCreateTime(LocalDateTime.now());
        logEntity.setDeletedFlag(0);

        // 设置线程信息
        Thread currentThread = Thread.currentThread();
        logEntity.setThreadName(currentThread.getName());

        // 设置主机信息
        try {
            logEntity.setHostname(java.net.InetAddress.getLocalHost().getHostName());
        } catch (Exception e) {
            logEntity.setHostname("unknown-host");
        }

        return logEntity;
    }
}
