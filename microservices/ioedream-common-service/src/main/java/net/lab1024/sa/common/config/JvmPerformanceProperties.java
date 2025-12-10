package net.lab1024.sa.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JVM性能配置属性
 * <p>
 * 绑定JVM性能监控配置
 * 提供类型安全的配置访问
 * 支持多环境配置和动态调整
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Data
@ConfigurationProperties(prefix = "performance")
public class JvmPerformanceProperties {

    /**
     * JVM监控配置
     */
    private Monitoring monitoring = new Monitoring();

    /**
     * 内存配置
     */
    private Memory memory = new Memory();

    /**
     * GC配置
     */
    private Gc gc = new Gc();

    /**
     * 性能配置
     */
    private Performance performance = new Performance();

    /**
     * 告警配置
     */
    private Alerts alerts = new Alerts();

    /**
     * 监控配置
     */
    @Data
    public static class Monitoring {
        private boolean enabled = true;
        private int collectionIntervalSeconds = 60;
        private int reportIntervalSeconds = 300;
        private boolean gcLogging = true;
        private String gcLogFile = "./logs/gc.log";
        private String gcLogRotation = "daily";
        private boolean jfrEnabled = false;
        private String jfrFile = "./logs/jfr.jfr";
    }

    /**
     * 内存配置
     */
    @Data
    public static class Memory {
        private long initial = 2048;        // 初始堆内存（MB）
        private long maximum = 4096;        // 最大堆内存（MB）
        private long young = 1024;           // 年轻代（MB）
        private long metaspace = 512;       // 元空间（MB）
        private long direct = 256;          // 直接内存（MB）
    }

    /**
     * GC配置
     */
    @Data
    public static class Gc {
        private String collector = "G1";
        private int pauseTimeTarget = 200;
        private int parallelThreads = 4;
        private int concurrentThreads = 2;
    }

    /**
     * 性能配置
     */
    @Data
    public static class Performance {
        private boolean jitOptimization = true;
        private boolean tieredCompilation = true;
        private boolean compressedOops = true;
        private boolean stringDeduplication = true;
    }

    /**
     * 告警配置
     */
    @Data
    public static class Alerts {
        private Memory memory = new Memory();
        private Gc gc = new Gc();
        private Thread thread = new Thread();

        @Data
        public static class Memory {
            private boolean heapUsageHigh = true;
            private double usageThreshold = 85.0;
            private boolean metaspaceUsageHigh = true;
            private double metaspaceThreshold = 90.0;
        }

        @Data
        public static class Gc {
            private boolean pauseTimeLong = true;
            private double pauseTimeThreshold = 300.0;
            private boolean frequencyHigh = true;
            private double frequencyThreshold = 20.0;
        }

        @Data
        public static class Thread {
            private boolean deadlockDetected = true;
            private boolean activeCountHigh = true;
            private int activeCountThreshold = 500;
            private boolean blockedCountHigh = true;
            private int blockedCountThreshold = 10;
        }
    }

    /**
     * 高并发场景配置
     */
    private HighConcurrency highConcurrency = new HighConcurrency();

    @Data
    public static class HighConcurrency {
        private ThreadPoolConfig threadPools = new ThreadPoolConfig();
        private ConnectionPoolConfig connectionPools = new ConnectionPoolConfig();
        private CacheConfig cache = new CacheConfig();

        @Data
        public static class ThreadPoolConfig {
            private BusinessConfig business = new BusinessConfig();
            private AsyncConfig async = new AsyncConfig();
            private ScheduledConfig scheduled = new ScheduledConfig();

            @Data
            public static class BusinessConfig {
                private int coreSize = 50;
                private int maxSize = 200;
                private int queueCapacity = 1000;
                private String keepAliveTime = "60s";
                private String threadNamePrefix = "business-";
            }

            @Data
            public static class AsyncConfig {
                private int coreSize = 20;
                private int maxSize = 100;
                private int queueCapacity = 500;
                private String keepAliveTime = "30s";
                private String threadNamePrefix = "async-";
            }

            @Data
            public static class ScheduledConfig {
                private int coreSize = 10;
                private int maxSize = 50;
                private String keepAliveTime = "60s";
                private String threadNamePrefix = "scheduled-";
            }
        }

        @Data
        public static class ConnectionPoolConfig {
            private DatasourceConfig datasource = new DatasourceConfig();
            private RedisConfig redis = new RedisConfig();

            @Data
            public static class DatasourceConfig {
                private int initialSize = 10;
                private int minIdle = 10;
                private int maxActive = 50;
                private int maxWait = 60000;
                private String validationQuery = "SELECT 1";
                private boolean testWhileIdle = true;
                private boolean testOnBorrow = false;
                private boolean testOnReturn = false;
                private long timeBetweenEvictionRunsMillis = 60000;
                private long minEvictableIdleTimeMillis = 300000;
            }

            @Data
            public static class RedisConfig {
                private int maxActive = 20;
                private int maxIdle = 10;
                private int minIdle = 5;
                private int maxWait = 3000;
                private long timeBetweenEvictionRuns = 10000;
            }
        }

        @Data
        public static class CacheConfig {
            private long maximumSize = 10000;
            private long expireAfterWrite = 300L;
            private boolean recordStats = true;
            private boolean weakKeys = false;
            private boolean softValues = false;

            private WarmupConfig warmup = new WarmupConfig();

            @Data
            public static class WarmupConfig {
                private boolean enabled = true;
                private int threadPoolSize = 4;
                private String timeout = "30s";
            }
        }
    }
}
