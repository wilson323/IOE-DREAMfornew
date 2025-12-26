package net.lab1024.sa.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * JVM性能配置属性
 *
 * @author IOE-DREAM Team
 * @since 2025-12-20
 */
@Data
@ConfigurationProperties(prefix = "performance.jvm")
public class JvmPerformanceProperties {

    private Monitoring monitoring = new Monitoring();
    private Alerts alerts = new Alerts();

    @Data
    public static class Monitoring {
        private Boolean enabled = true;
        private Integer collectionIntervalSeconds = 60;
        private Integer reportIntervalSeconds = 300;
    }

    @Data
    public static class Alerts {
        private Memory memory = new Memory();
        private Gc gc = new Gc();
        private Thread thread = new Thread();
    }

    @Data
    public static class Memory {
        private Double usageThreshold = 80.0;
    }

    @Data
    public static class Gc {
        private Long pauseTimeThreshold = 1000L;
    }

    @Data
    public static class Thread {
        private Integer blockedCountThreshold = 10;
    }
}
