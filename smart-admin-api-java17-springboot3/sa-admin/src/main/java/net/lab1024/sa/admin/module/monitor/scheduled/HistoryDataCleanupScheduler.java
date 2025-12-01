package net.lab1024.sa.admin.module.monitor.scheduled;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import net.lab1024.sa.admin.module.monitor.service.AccessMonitorService;

/**
 * 历史数据清理定时任务
 * <p>
 * 严格遵循repowiki规范：
 * - 使用@Scheduled注解实现定时任务
 * - 完整的异常处理和日志记录
 * - 支持可配置的清理策略
 * - 避免在业务高峰期执行
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Component
public class HistoryDataCleanupScheduler {
    private static final Logger log = LoggerFactory.getLogger(HistoryDataCleanupScheduler.class);

    @Resource
    private AccessMonitorService accessMonitorService;

    /**
     * 默认保留天数配置
     */
    private static final int DEFAULT_RETAIN_DAYS_ACCESS_EVENT = 90; // 门禁事件保留90天
    private static final int DEFAULT_RETAIN_DAYS_MONITOR_HISTORY = 180; // 监控历史保留180天
    private static final int DEFAULT_RETAIN_DAYS_REALTIME_EVENT = 30; // 实时事件保留30天
    private static final int DEFAULT_RETAIN_DAYS_STATISTICS = 365; // 统计数据保留365天

    /**
     * 定时清理门禁事件历史数据
     * 每天凌晨3点执行，清理90天前的数据
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupAccessEvents() {
        log.info("开始执行门禁事件历史数据清理任务");
        try {
            accessMonitorService.clearHistoryData("access_event", DEFAULT_RETAIN_DAYS_ACCESS_EVENT);
            log.info("门禁事件历史数据清理任务完成");
        } catch (Exception e) {
            log.error("门禁事件历史数据清理任务失败", e);
        }
    }

    /**
     * 定时清理监控历史数据
     * 每周日凌晨2点执行，清理180天前的数据
     */
    @Scheduled(cron = "0 0 2 ? * SUN")
    public void cleanupMonitorHistory() {
        log.info("开始执行监控历史数据清理任务");
        try {
            accessMonitorService.clearHistoryData("monitor_history", DEFAULT_RETAIN_DAYS_MONITOR_HISTORY);
            log.info("监控历史数据清理任务完成");
        } catch (Exception e) {
            log.error("监控历史数据清理任务失败", e);
        }
    }

    /**
     * 定时清理实时事件数据
     * 每天凌晨4点执行，清理30天前的数据
     */
    @Scheduled(cron = "0 0 4 * * ?")
    public void cleanupRealtimeEvents() {
        log.info("开始执行实时事件数据清理任务");
        try {
            accessMonitorService.clearHistoryData("realtime_event", DEFAULT_RETAIN_DAYS_REALTIME_EVENT);
            log.info("实时事件数据清理任务完成");
        } catch (Exception e) {
            log.error("实时事件数据清理任务失败", e);
        }
    }

    /**
     * 定时清理统计数据
     * 每月1号凌晨5点执行，清理365天前的数据
     */
    @Scheduled(cron = "0 0 5 1 * ?")
    public void cleanupStatistics() {
        log.info("开始执行统计数据清理任务");
        try {
            accessMonitorService.clearHistoryData("statistics", DEFAULT_RETAIN_DAYS_STATISTICS);
            log.info("统计数据清理任务完成");
        } catch (Exception e) {
            log.error("统计数据清理任务失败", e);
        }
    }

    /**
     * 综合清理任务（可选，用于一次性清理所有类型）
     * 每月15号凌晨1点执行，清理所有类型的历史数据
     */
    @Scheduled(cron = "0 0 1 15 * ?")
    public void cleanupAllHistoryData() {
        log.info("开始执行综合历史数据清理任务");
        try {
            // 清理所有类型的历史数据
            accessMonitorService.clearHistoryData("all", DEFAULT_RETAIN_DAYS_ACCESS_EVENT);
            log.info("综合历史数据清理任务完成");
        } catch (Exception e) {
            log.error("综合历史数据清理任务失败", e);
        }
    }

    /**
     * 获取任务执行状态（用于监控）
     *
     * @return 任务状态信息
     */
    public String getTaskStatus() {
        return String.format("历史数据清理定时任务状态 - 最后执行时间: %s", LocalDateTime.now());
    }
}
