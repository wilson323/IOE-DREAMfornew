package net.lab1024.sa.common.config;

import lombok.extern.slf4j.Slf4j;


/**
 * 数据库优化管理器
 * <p>
 * 职责：
 * - 连接池优化配置
 * - 数据库监控配置
 * - 查询优化配置
 * </p>
 * <p>
 * 注意：Manager类是纯Java类，不使用Spring注解，通过构造函数注入依赖。
 * 在微服务中通过配置类将Manager注册为Spring Bean。
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-20
 */
@Slf4j
public class DatabaseOptimizationManager {


    private final PoolConfig poolConfig;
    private final MonitoringConfig monitoringConfig;
    private final QueryOptimizationConfig queryOptimizationConfig;

    public DatabaseOptimizationManager(PoolConfig poolConfig, MonitoringConfig monitoringConfig,
            QueryOptimizationConfig queryOptimizationConfig) {
        this.poolConfig = poolConfig;
        this.monitoringConfig = monitoringConfig;
        this.queryOptimizationConfig = queryOptimizationConfig;
        log.info("[DatabaseOptimizationManager] 初始化数据库优化管理器");
    }

    /**
     * 连接池配置
     */
    public static class PoolConfig {
        private Integer initialSize;
        private Integer minIdle;
        private Integer maxActive;
        private Long maxWait;

        public Integer getInitialSize() {
            return initialSize;
        }

        public void setInitialSize(Integer initialSize) {
            this.initialSize = initialSize;
        }

        public Integer getMinIdle() {
            return minIdle;
        }

        public void setMinIdle(Integer minIdle) {
            this.minIdle = minIdle;
        }

        public Integer getMaxActive() {
            return maxActive;
        }

        public void setMaxActive(Integer maxActive) {
            this.maxActive = maxActive;
        }

        public Long getMaxWait() {
            return maxWait;
        }

        public void setMaxWait(Long maxWait) {
            this.maxWait = maxWait;
        }
    }

    /**
     * 监控配置
     */
    public static class MonitoringConfig {
        private Boolean enabled;
        private Long slowSqlMillis;

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public Long getSlowSqlMillis() {
            return slowSqlMillis;
        }

        public void setSlowSqlMillis(Long slowSqlMillis) {
            this.slowSqlMillis = slowSqlMillis;
        }
    }

    /**
     * 查询优化配置
     */
    public static class QueryOptimizationConfig {
        private Boolean enabled;
        private Integer maxQueryTime;

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public Integer getMaxQueryTime() {
            return maxQueryTime;
        }

        public void setMaxQueryTime(Integer maxQueryTime) {
            this.maxQueryTime = maxQueryTime;
        }
    }

    public PoolConfig getPoolConfig() {
        return poolConfig;
    }

    public MonitoringConfig getMonitoringConfig() {
        return monitoringConfig;
    }

    public QueryOptimizationConfig getQueryOptimizationConfig() {
        return queryOptimizationConfig;
    }
}
