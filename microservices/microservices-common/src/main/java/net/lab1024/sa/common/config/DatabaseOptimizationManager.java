package net.lab1024.sa.common.config;

import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import com.alibaba.druid.pool.DruidDataSource;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Objects;

/**
 * 企业级数据库优化配置管理器
 * <p>
 * 统一管理所有微服务的数据库连接池配置、性能监控、慢查询检测
 * 严格遵循CLAUDE.md全局架构规范：
 * - Manager类在microservices-common中是纯Java类，不使用Spring注解
 * - 通过构造函数注入所有依赖（包括配置对象）
 * - 在微服务中通过配置类注册为Spring Bean
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 3.0.0
 * @since 2025-12-09
 * @updated 2025-01-30 移除Spring注解，改为纯Java类，符合CLAUDE.md规范
 */
@Slf4j
public class DatabaseOptimizationManager {

    /**
     * 数据库连接池配置
     */
    private final PoolConfig pool;

    /**
     * 性能监控配置
     */
    private final MonitoringConfig monitoring;

    /**
     * 查询优化配置
     */
    private final QueryOptimizationConfig queryOptimization;

    /**
     * 构造函数注入所有依赖
     * <p>
     * 符合CLAUDE.md规范：Manager类通过构造函数接收依赖，保持为纯Java类
     * </p>
     *
     * @param pool 连接池配置
     * @param monitoring 性能监控配置
     * @param queryOptimization 查询优化配置
     */
    public DatabaseOptimizationManager(
            PoolConfig pool,
            MonitoringConfig monitoring,
            QueryOptimizationConfig queryOptimization) {
        this.pool = Objects.requireNonNull(pool, "pool配置不能为null");
        this.monitoring = Objects.requireNonNull(monitoring, "monitoring配置不能为null");
        this.queryOptimization = Objects.requireNonNull(queryOptimization, "queryOptimization配置不能为null");
    }

    /**
     * 默认构造函数（使用默认配置）
     */
    public DatabaseOptimizationManager() {
        this.pool = new PoolConfig();
        this.monitoring = new MonitoringConfig();
        this.queryOptimization = new QueryOptimizationConfig();
    }

    /**
     * 连接池缓存（多数据源支持）
     */
    private final Map<String, DataSource> dataSourceCache = new ConcurrentHashMap<>();

    /**
     * 创建优化后的Druid数据源
     */
    public DataSource createOptimizedDataSource(String serviceName, String jdbcUrl, String username, String password) {
        String cacheKey = serviceName + ":" + jdbcUrl;

        return dataSourceCache.computeIfAbsent(cacheKey, key -> {
            log.info("[数据库优化] 创建优化数据源: service={}, url={}", serviceName, jdbcUrl);

            try {
                DruidDataSource dataSource = new DruidDataSource();

                // 基础连接配置
                dataSource.setUrl(jdbcUrl);
                dataSource.setUsername(username);
                dataSource.setPassword(password);
                dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");

                // 连接池配置（遵循CLAUDE.md规范）
                dataSource.setInitialSize(pool.getInitialSize());
                dataSource.setMinIdle(pool.getMinIdle());
                dataSource.setMaxActive(pool.getMaxActive());
                dataSource.setMaxWait(pool.getMaxWait().toMillis());

                // 连接检测配置
                dataSource.setTimeBetweenEvictionRunsMillis(pool.getTimeBetweenEvictionRunsMillis().toMillis());
                dataSource.setMinEvictableIdleTimeMillis(pool.getMinEvictableIdleTimeMillis().toMillis());
                dataSource.setValidationQuery(pool.getValidationQuery());
                dataSource.setTestWhileIdle(pool.isTestWhileIdle());
                dataSource.setTestOnBorrow(pool.isTestOnBorrow());
                dataSource.setTestOnReturn(pool.isTestOnReturn());

                // 防止连接泄露
                dataSource.setRemoveAbandoned(pool.isRemoveAbandoned());
                dataSource.setRemoveAbandonedTimeout((int) pool.getRemoveAbandonedTimeout().toMillis());
                dataSource.setLogAbandoned(pool.isLogAbandoned());

                // 性能监控配置
                if (monitoring.isEnabled()) {
                    // 启用Druid监控
                    dataSource.setFilters("stat,wall,log4j2");

                    // 慢SQL检测
                    dataSource.setConnectionProperties(String.format(
                        "druid.stat.slowSqlMillis=%d;druid.stat.logSlowSql=%s;config.decrypt=false",
                        monitoring.getSlowSqlThreshold(), monitoring.isLogSlowSql()
                    ));
                }

                // 初始化数据源
                dataSource.init();

                log.info("[数据库优化] 数据源创建成功: service={}, initialSize={}, maxActive={}",
                    serviceName, pool.getInitialSize(), pool.getMaxActive());

                return dataSource;

            } catch (SQLException e) {
                log.error("[数据库优化] 数据源创建失败: service={}, error={}", serviceName, e.getMessage(), e);
                throw new RuntimeException("数据源创建失败: " + serviceName, e);
            }
        });
    }

    /**
     * 获取数据源性能统计信息
     *
     * 注意：dataSource是从缓存中获取的DruidDataSource实例，由Spring容器管理生命周期，
     * 不需要手动关闭，因此不存在资源泄漏问题。
     */
    @SuppressWarnings("resource")
    public Map<String, Object> getDataSourceStatistics(String serviceName) {
        String cacheKey = serviceName + ":";

        return dataSourceCache.entrySet().stream()
            .filter(entry -> entry.getKey().startsWith(cacheKey))
            .findFirst()
            .map(entry -> {
                if (entry.getValue() instanceof DruidDataSource) {
                    // dataSource是从缓存中获取的，由Spring管理，不需要关闭
                    DruidDataSource dataSource = (DruidDataSource) entry.getValue();
                    Map<String, Object> stats = new ConcurrentHashMap<>();

                    // 连接池状态
                    stats.put("activeCount", dataSource.getActiveCount());
                    stats.put("poolingCount", dataSource.getPoolingCount());
                    stats.put("connectCount", dataSource.getConnectCount());
                    stats.put("closeCount", dataSource.getCloseCount());

                    // 连接池配置
                    stats.put("initialSize", dataSource.getInitialSize());
                    stats.put("minIdle", dataSource.getMinIdle());
                    stats.put("maxActive", dataSource.getMaxActive());

                    // 性能统计
                    stats.put("connectErrorCount", dataSource.getConnectErrorCount());
                    stats.put("createCount", dataSource.getCreateCount());
                    stats.put("destroyCount", dataSource.getDestroyCount());

                    return stats;
                }
                return null;
            })
            .orElse(null);
    }

    /**
     * 检查数据库连接健康状态
     *
     * 注意：dataSource是从缓存中获取的DruidDataSource实例，由Spring容器管理生命周期，
     * 不需要手动关闭。但通过getConnection()获取的连接需要关闭。
     */
    @SuppressWarnings("resource")
    public boolean checkDataSourceHealth(String serviceName) {
        String cacheKey = serviceName + ":";

        return dataSourceCache.entrySet().stream()
            .filter(entry -> entry.getKey().startsWith(cacheKey))
            .findFirst()
            .map(entry -> {
                if (entry.getValue() instanceof DruidDataSource) {
                    // dataSource是从缓存中获取的，由Spring管理，不需要关闭
                    @SuppressWarnings("resource")
                    DruidDataSource dataSource = (DruidDataSource) entry.getValue();
                    java.sql.Connection connection = null;
                    try {
                        connection = dataSource.getConnection();
                        return connection.isValid(5); // 5秒超时
                    } catch (Exception e) {
                        log.warn("[数据库优化] 数据源健康检查失败: service={}, error={}", serviceName, e.getMessage());
                        return false;
                    } finally {
                        // 确保连接被正确关闭，避免资源泄漏
                        if (connection != null) {
                            try {
                                connection.close();
                            } catch (Exception e) {
                                log.warn("[数据库优化] 关闭连接失败: service={}, error={}", serviceName, e.getMessage());
                            }
                        }
                    }
                }
                return false;
            })
            .orElse(false);
    }

    /**
     * 关闭数据源
     */
    public void closeDataSource(String serviceName) {
        String cacheKey = serviceName + ":";

        dataSourceCache.entrySet().removeIf(entry -> {
            if (entry.getKey().startsWith(cacheKey)) {
                if (entry.getValue() instanceof DruidDataSource) {
                    DruidDataSource dataSource = (DruidDataSource) entry.getValue();
                    try {
                        dataSource.close();
                        log.info("[数据库优化] 数据源已关闭: service={}", serviceName);
                    } catch (Exception e) {
                        log.warn("[数据库优化] 数据源关闭异常: service={}, error={}", serviceName, e.getMessage());
                    }
                }
                return true;
            }
            return false;
        });
    }

    /**
     * 连接池配置类
     */
    public static class PoolConfig {
        private int initialSize = 10;
        private int minIdle = 10;
        private int maxActive = 50;
        private Duration maxWait = Duration.ofSeconds(60);
        private Duration timeBetweenEvictionRunsMillis = Duration.ofSeconds(60);
        private Duration minEvictableIdleTimeMillis = Duration.ofMinutes(5);
        private String validationQuery = "SELECT 1";
        private boolean testWhileIdle = true;
        private boolean testOnBorrow = false;
        private boolean testOnReturn = false;
        private boolean removeAbandoned = true;
        private Duration removeAbandonedTimeout = Duration.ofMinutes(5);
        private boolean logAbandoned = true;

        // Getters and Setters
        public int getInitialSize() { return initialSize; }
        public void setInitialSize(int initialSize) { this.initialSize = initialSize; }

        public int getMinIdle() { return minIdle; }
        public void setMinIdle(int minIdle) { this.minIdle = minIdle; }

        public int getMaxActive() { return maxActive; }
        public void setMaxActive(int maxActive) { this.maxActive = maxActive; }

        public Duration getMaxWait() { return maxWait; }
        public void setMaxWait(Duration maxWait) { this.maxWait = maxWait; }

        public Duration getTimeBetweenEvictionRunsMillis() { return timeBetweenEvictionRunsMillis; }
        public void setTimeBetweenEvictionRunsMillis(Duration timeBetweenEvictionRunsMillis) {
            this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
        }

        public Duration getMinEvictableIdleTimeMillis() { return minEvictableIdleTimeMillis; }
        public void setMinEvictableIdleTimeMillis(Duration minEvictableIdleTimeMillis) {
            this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
        }

        public String getValidationQuery() { return validationQuery; }
        public void setValidationQuery(String validationQuery) { this.validationQuery = validationQuery; }

        public boolean isTestWhileIdle() { return testWhileIdle; }
        public void setTestWhileIdle(boolean testWhileIdle) { this.testWhileIdle = testWhileIdle; }

        public boolean isTestOnBorrow() { return testOnBorrow; }
        public void setTestOnBorrow(boolean testOnBorrow) { this.testOnBorrow = testOnBorrow; }

        public boolean isTestOnReturn() { return testOnReturn; }
        public void setTestOnReturn(boolean testOnReturn) { this.testOnReturn = testOnReturn; }

        public boolean isRemoveAbandoned() { return removeAbandoned; }
        public void setRemoveAbandoned(boolean removeAbandoned) { this.removeAbandoned = removeAbandoned; }

        public Duration getRemoveAbandonedTimeout() { return removeAbandonedTimeout; }
        public void setRemoveAbandonedTimeout(Duration removeAbandonedTimeout) {
            this.removeAbandonedTimeout = removeAbandonedTimeout;
        }

        public boolean isLogAbandoned() { return logAbandoned; }
        public void setLogAbandoned(boolean logAbandoned) { this.logAbandoned = logAbandoned; }
    }

    /**
     * 监控配置类
     */
    public static class MonitoringConfig {
        private boolean enabled = true;
        private long slowSqlThreshold = 1000; // 1秒
        private boolean logSlowSql = true;
        private boolean enableStatFilter = true;
        private boolean enableWallFilter = true;

        // Getters and Setters
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public long getSlowSqlThreshold() { return slowSqlThreshold; }
        public void setSlowSqlThreshold(long slowSqlThreshold) { this.slowSqlThreshold = slowSqlThreshold; }

        public boolean isLogSlowSql() { return logSlowSql; }
        public void setLogSlowSql(boolean logSlowSql) { this.logSlowSql = logSlowSql; }

        public boolean isEnableStatFilter() { return enableStatFilter; }
        public void setEnableStatFilter(boolean enableStatFilter) { this.enableStatFilter = enableStatFilter; }

        public boolean isEnableWallFilter() { return enableWallFilter; }
        public void setEnableWallFilter(boolean enableWallFilter) { this.enableWallFilter = enableWallFilter; }
    }

    /**
     * 查询优化配置类
     */
    public static class QueryOptimizationConfig {
        private boolean enableQueryCache = true;
        private Duration queryTimeout = Duration.ofSeconds(30);
        private int maxRows = 10000;
        private boolean enablePreparedStatementCache = true;

        // Getters and Setters
        public boolean isEnableQueryCache() { return enableQueryCache; }
        public void setEnableQueryCache(boolean enableQueryCache) { this.enableQueryCache = enableQueryCache; }

        public Duration getQueryTimeout() { return queryTimeout; }
        public void setQueryTimeout(Duration queryTimeout) { this.queryTimeout = queryTimeout; }

        public int getMaxRows() { return maxRows; }
        public void setMaxRows(int maxRows) { this.maxRows = maxRows; }

        public boolean isEnablePreparedStatementCache() { return enablePreparedStatementCache; }
        public void setEnablePreparedStatementCache(boolean enablePreparedStatementCache) {
            this.enablePreparedStatementCache = enablePreparedStatementCache;
        }
    }

    // Getters for main configuration
    public PoolConfig getPool() { return pool; }

    public MonitoringConfig getMonitoring() { return monitoring; }

    public QueryOptimizationConfig getQueryOptimization() { return queryOptimization; }
}
