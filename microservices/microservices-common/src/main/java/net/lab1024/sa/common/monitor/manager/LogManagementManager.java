package net.lab1024.sa.common.monitor.manager;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.monitor.dao.SystemLogDao;
import net.lab1024.sa.common.monitor.domain.entity.SystemLogEntity;

/**
 * 日志管理管理器
 * <p>
 * 负责系统日志收集、存储、查询和分析等功能
 * 严格遵循CLAUDE.md规范:
 * - Manager类在microservices-common中是纯Java类，不使用Spring注解
 * - 通过构造函数注入依赖（DAO等）
 * - 在微服务中通过配置类注册为Spring Bean
 * - 完整的异常处理和日志记录
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02
 * @updated 2025-01-30 移除Spring注解，改为纯Java类，符合CLAUDE.md规范
 */
@Slf4j
public class LogManagementManager {

    private final SystemLogDao systemLogDao;

    /**
     * 构造函数注入依赖
     * <p>
     * 符合CLAUDE.md规范：Manager类在microservices-common中不使用Spring注解，
     * 通过构造函数接收依赖，保持为纯Java类
     * </p>
     *
     * @param systemLogDao 系统日志DAO
     */
    public LogManagementManager(SystemLogDao systemLogDao) {
        this.systemLogDao = systemLogDao;
    }

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
     * 获取日志统计信息
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计信息
     */
    public Map<String, Object> getLogStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        log.debug("获取日志统计信息，时间范围：{} - {}", startTime, endTime);

        try {
            List<Map<String, Object>> levelStats = systemLogDao.countLogsByLevel(startTime, endTime);
            List<Map<String, Object>> typeStats = systemLogDao.countLogsByType(startTime, endTime);
            List<Map<String, Object>> serviceStats = systemLogDao.countLogsByService(startTime, endTime);

            Map<String, Object> statistics = new HashMap<>();
            statistics.put("byLevel", levelStats);
            statistics.put("byType", typeStats);
            statistics.put("byService", serviceStats);

            return statistics;

        } catch (Exception e) {
            log.error("获取日志统计信息失败", e);
            return new HashMap<>();
        }
    }

    /**
     * 清理历史日志
     *
     * @param days 保留天数
     * @return 删除数量
     */
    public int cleanHistoryLogs(Integer days) {
        log.info("清理历史日志，保留天数：{}", days);

        try {
            LocalDateTime beforeTime = LocalDateTime.now().minusDays(days);
            int deletedCount = systemLogDao.deleteHistoryLogs(beforeTime);

            log.info("清理历史日志完成，删除数量：{}", deletedCount);
            return deletedCount;

        } catch (Exception e) {
            log.error("清理历史日志失败", e);
            return 0;
        }
    }
}
