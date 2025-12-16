package net.lab1024.sa.common.monitor.manager;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import jakarta.sql.DataSource;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;

/**
 * Druid连接池监控管理器
 * <p>
 * 用于监控Druid连接池的性能指标
 * 严格遵循CLAUDE.md规范：
 * - Manager类在microservices-common中是纯Java类，不使用Spring注解
 * - 通过构造函数注入依赖
 * </p>
 * <p>
 * 功能说明：
 * - 连接池状态监控（活跃连接数、空闲连接数、等待连接数）
 * - 连接池利用率统计
 * - 连接泄漏检测
 * - 性能指标导出（Prometheus）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
public class DruidConnectionPoolMonitor {

    private final DataSource dataSource;
    private final MeterRegistry meterRegistry;

    /**
     * 构造函数注入依赖
     *
     * @param dataSource    数据源
     * @param meterRegistry Micrometer指标注册表
     */
    public DruidConnectionPoolMonitor(DataSource dataSource, MeterRegistry meterRegistry) {
        this.dataSource = dataSource;
        this.meterRegistry = meterRegistry;
    }

    /**
     * 获取连接池监控信息
     *
     * @return 连接池监控信息
     */
    public Map<String, Object> getConnectionPoolStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 使用反射避免直接依赖Druid（microservices-common中不包含Druid依赖）
        if (!dataSource.getClass().getName().equals("com.alibaba.druid.pool.DruidDataSource")) {
            log.warn("[连接池监控] 数据源不是DruidDataSource类型，无法监控");
            return stats;
        }

        try {
            Object druidDataSource = dataSource;
        
            // 使用反射调用DruidDataSource的方法
            Method getActiveCount = druidDataSource.getClass().getMethod("getActiveCount");
            Method getPoolingCount = druidDataSource.getClass().getMethod("getPoolingCount");
            Method getWaitThreadCount = druidDataSource.getClass().getMethod("getWaitThreadCount");
            Method getMaxActive = druidDataSource.getClass().getMethod("getMaxActive");
            Method getConnectCount = druidDataSource.getClass().getMethod("getConnectCount");
            Method getCloseCount = druidDataSource.getClass().getMethod("getCloseCount");
            Method getConnectErrorCount = druidDataSource.getClass().getMethod("getConnectErrorCount");
            Method getNotEmptyWaitThreadCount = druidDataSource.getClass().getMethod("getNotEmptyWaitThreadCount");
            Method getNotEmptyWaitMillis = druidDataSource.getClass().getMethod("getNotEmptyWaitMillis");
            
            int active = (Integer) getActiveCount.invoke(druidDataSource);
            int idle = (Integer) getPoolingCount.invoke(druidDataSource);
            long wait = (Long) getWaitThreadCount.invoke(druidDataSource);
            int maxActive = (Integer) getMaxActive.invoke(druidDataSource);
            long connectCount = (Long) getConnectCount.invoke(druidDataSource);
            long closeCount = (Long) getCloseCount.invoke(druidDataSource);
            long connectErrorCount = (Long) getConnectErrorCount.invoke(druidDataSource);
            
            // 连接池利用率
            double utilization = maxActive > 0 ? (double) active / maxActive * 100 : 0.0;
            
            // 连接获取时间（平均）
            long notEmptyWaitThreadCount = (Long) getNotEmptyWaitThreadCount.invoke(druidDataSource);
            long waitTime = notEmptyWaitThreadCount > 0 
                    ? (Long) getNotEmptyWaitMillis.invoke(druidDataSource) / notEmptyWaitThreadCount 
                    : 0;
            
            stats.put("active", active);
            stats.put("idle", idle);
            stats.put("wait", wait);
            stats.put("maxActive", maxActive);
            stats.put("utilization", String.format("%.2f", utilization));
            stats.put("connectCount", connectCount);
            stats.put("closeCount", closeCount);
            stats.put("connectErrorCount", connectErrorCount);
            stats.put("waitTime", waitTime);
            
            // 记录到Micrometer
            if (meterRegistry != null) {
                meterRegistry.gauge("druid.connection.active", active);
                meterRegistry.gauge("druid.connection.idle", idle);
                meterRegistry.gauge("druid.connection.wait", wait);
                meterRegistry.gauge("druid.connection.utilization", utilization);
                meterRegistry.gauge("druid.connection.wait.time", waitTime);
            }
            
            // 告警检查
            if (utilization > 90) {
                log.warn("[连接池告警] 连接池利用率过高：{}%，活跃连接数：{}/{}", 
                        String.format("%.2f", utilization), active, maxActive);
            }
            
            if (wait > 10) {
                log.warn("[连接池告警] 等待连接数过多：{}", wait);
            }
            
            if (connectErrorCount > 0) {
                log.warn("[连接池告警] 连接错误次数：{}", connectErrorCount);
            }
            
        } catch (Exception e) {
            log.error("[连接池监控] 获取连接池统计信息失败", e);
        }
        
        return stats;
    }

    /**
     * 检查连接泄漏
     *
     * @return 是否检测到连接泄漏
     */
    public boolean checkConnectionLeak() {
        // 使用反射避免直接依赖Druid
        if (!dataSource.getClass().getName().equals("com.alibaba.druid.pool.DruidDataSource")) {
            return false;
        }

        try {
            Object druidDataSource = dataSource;
            Method getActiveCount = druidDataSource.getClass().getMethod("getActiveCount");
            Method getPoolingCount = druidDataSource.getClass().getMethod("getPoolingCount");
            Method getMaxActive = druidDataSource.getClass().getMethod("getMaxActive");
            
            // 检查是否有未关闭的连接
            int active = (Integer) getActiveCount.invoke(druidDataSource);
            int pooling = (Integer) getPoolingCount.invoke(druidDataSource);
            int maxActive = (Integer) getMaxActive.invoke(druidDataSource);
            
            // 如果活跃连接数接近最大连接数，且空闲连接数为0，可能存在连接泄漏
            if (active >= maxActive * 0.9 && pooling == 0) {
                log.warn("[连接池告警] 可能存在连接泄漏，活跃连接数：{}/{}，空闲连接数：{}", 
                        active, maxActive, pooling);
                return true;
            }
            
            return false;
        } catch (Exception e) {
            log.error("[连接池监控] 检查连接泄漏失败", e);
            return false;
        }
    }

    /**
     * 获取慢查询统计
     *
     * @return 慢查询统计信息
     */
    public Map<String, Object> getSlowQueryStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 使用反射避免直接依赖Druid
        if (!dataSource.getClass().getName().equals("com.alibaba.druid.pool.DruidDataSource")) {
            return stats;
        }

        try {
            Object druidDataSource = dataSource;
            Method getSlowSqlCount = druidDataSource.getClass().getMethod("getSlowSqlCount");
            Method getSqlCount = druidDataSource.getClass().getMethod("getSqlCount");
            
            long slowSqlCount = (Long) getSlowSqlCount.invoke(druidDataSource);
            long sqlCount = (Long) getSqlCount.invoke(druidDataSource);
            double slowSqlRate = sqlCount > 0 ? (double) slowSqlCount / sqlCount * 100 : 0.0;
            
            stats.put("slowSqlCount", slowSqlCount);
            stats.put("sqlCount", sqlCount);
            stats.put("slowSqlRate", String.format("%.2f", slowSqlRate));
            
            // 记录到Micrometer
            if (meterRegistry != null) {
                meterRegistry.gauge("druid.sql.slow.count", slowSqlCount);
                meterRegistry.gauge("druid.sql.slow.rate", slowSqlRate);
            }
            
            // 告警检查
            if (slowSqlRate > 5.0) {
                log.warn("[慢查询告警] 慢查询率过高：{}%，慢查询数：{}，总查询数：{}", 
                        String.format("%.2f", slowSqlRate), slowSqlCount, sqlCount);
            }
            
        } catch (Exception e) {
            log.error("[连接池监控] 获取慢查询统计失败", e);
        }
        
        return stats;
    }
}

