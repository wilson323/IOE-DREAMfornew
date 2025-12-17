package net.lab1024.sa.common.config;

import com.alibaba.druid.pool.DruidDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * Druid连接池监控组件
 * 
 * <p>监控指标:</p>
 * <ul>
 *   <li>1. 活跃连接数</li>
 *   <li>2. 空闲连接数</li>
 *   <li>3. 等待线程数</li>
 *   <li>4. 连接获取失败次数</li>
 * </ul>
 * 
 * <p>告警阈值:</p>
 * <ul>
 *   <li>活跃连接数 > 83% (25/30)</li>
 *   <li>存在等待线程</li>
 *   <li>连接失败次数 > 100</li>
 * </ul>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-17
 */
@Slf4j
@Component
@ConditionalOnClass(DruidDataSource.class)
public class DruidPoolMonitor {

    @Resource
    private DruidDataSource dataSource;

    /**
     * 监控连接池状态
     * 执行频率: 每5分钟
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void monitorConnectionPool() {
        try {
            int activeCount = dataSource.getActiveCount();
            int poolingCount = dataSource.getPoolingCount();
            int waitThreadCount = dataSource.getWaitThreadCount();
            long connectErrorCount = dataSource.getConnectErrorCount();
            int maxActive = dataSource.getMaxActive();
            
            // 计算连接池使用率
            double usageRate = (double) activeCount / maxActive * 100;
            
            log.info("[Druid监控] 活跃连接:{}/{} (使用率:{:.2f}%), 空闲连接:{}, 等待线程:{}, 连接失败次数:{}", 
                     activeCount, maxActive, usageRate, poolingCount, waitThreadCount, connectErrorCount);
            
            // 告警检查
            checkAndAlert(activeCount, maxActive, waitThreadCount, connectErrorCount);
            
        } catch (Exception e) {
            log.error("[Druid监控] 监控连接池状态异常", e);
        }
    }

    /**
     * 检查并发出告警
     */
    private void checkAndAlert(int activeCount, int maxActive, int waitThreadCount, long connectErrorCount) {
        // 告警1: 活跃连接数超过83%
        double usageRate = (double) activeCount / maxActive;
        if (usageRate > 0.83) {
            log.warn("[Druid告警] 活跃连接数过高: {}/{} (使用率:{:.2f}%), 建议检查连接泄漏或增加连接池大小", 
                     activeCount, maxActive, usageRate * 100);
        }
        
        // 告警2: 存在等待连接的线程
        if (waitThreadCount > 0) {
            log.warn("[Druid告警] 存在{}个等待连接的线程, 连接池可能不足", waitThreadCount);
        }
        
        // 告警3: 连接失败次数过高
        if (connectErrorCount > 100) {
            log.error("[Druid告警] 连接失败次数过高: {}, 请检查数据库连接", connectErrorCount);
        }
    }

    /**
     * 获取连接池详细统计信息
     * 可通过Actuator端点暴露
     */
    public String getPoolStatistics() {
        try {
            return String.format(
                "Druid连接池统计: " +
                "活跃连接=%d, " +
                "空闲连接=%d, " +
                "总连接数=%d, " +
                "等待线程=%d, " +
                "连接失败次数=%d, " +
                "创建连接次数=%d, " +
                "销毁连接次数=%d, " +
                "最大活跃连接数=%d",
                dataSource.getActiveCount(),
                dataSource.getPoolingCount(),
                dataSource.getPoolingCount() + dataSource.getActiveCount(),
                dataSource.getWaitThreadCount(),
                dataSource.getConnectErrorCount(),
                dataSource.getCreateCount(),
                dataSource.getDestroyCount(),
                dataSource.getMaxActive()
            );
        } catch (Exception e) {
            log.error("[Druid监控] 获取连接池统计信息异常", e);
            return "获取统计信息失败: " + e.getMessage();
        }
    }

    /**
     * 重置连接池统计信息
     * 谨慎使用
     */
    public void resetStatistics() {
        try {
            dataSource.resetStat();
            log.info("[Druid监控] 已重置连接池统计信息");
        } catch (Exception e) {
            log.error("[Druid监控] 重置统计信息异常", e);
        }
    }
}
