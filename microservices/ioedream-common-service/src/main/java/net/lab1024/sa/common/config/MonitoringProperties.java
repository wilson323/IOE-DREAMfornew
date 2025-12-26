package net.lab1024.sa.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * 监控配置属性
 * <p>
 * 绑定application-monitoring.yml中的监控配置
 * 提供类型安全的配置访问
 * 支持多环境配置和动态配置更新
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Data
@ConfigurationProperties(prefix = "monitoring")
public class MonitoringProperties {

    /**
     * 全局监控开关
     */
    private boolean enabled = true;

    /**
     * 性能监控配置
     */
    private Performance performance = new Performance();

    /**
     * 业务监控配置
     */
    private Business business = new Business();

    /**
     * 告警配置
     */
    private Alert alert = new Alert();

    /**
     * 性能监控配置
     */
    @Data
    public static class Performance {
        private boolean enabled = true;
        private boolean jvm = true;
        private boolean system = true;
        private boolean custom = true;
    }

    /**
     * 业务监控配置
     */
    @Data
    public static class Business {
        private boolean enabled = true;
        private boolean http = true;
        private boolean database = true;
        private boolean cache = true;
        private boolean metrics = true;
    }

    /**
     * 告警配置
     */
    @Data
    public static class Alert {
        private boolean enabled = true;
        private Convergence convergence = new Convergence();
        private Map<String, Channel> channels = new HashMap<>();

        @Data
        public static class Convergence {
            private long defaultInterval = 300; // 5分钟
            private long maxCountWindow = 1800; // 30分钟
            private long maxCount = 3;
        }

        @Data
        public static class Channel {
            private boolean enabled;
            private String webhookUrl;
            private String secret;
            private String atPhones;
            private String smtpHost;
            private Integer smtpPort;
            private String username;
            private String password;
            private String recipients;
            private String apiKey;
            private String apiSecret;
        }
    }
}
