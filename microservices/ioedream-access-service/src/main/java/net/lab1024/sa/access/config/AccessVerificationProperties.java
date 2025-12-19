package net.lab1024.sa.access.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 门禁验证配置属性类
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用@ConfigurationProperties注解绑定配置
 * - 使用@Component注册为Spring Bean
 * - 配置项与application.yml中的access.verification对应
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Component
@ConfigurationProperties(prefix = "access.verification")
public class AccessVerificationProperties {

    /**
     * 验证模式配置
     */
    private ModeConfig mode = new ModeConfig();

    /**
     * 后台验证配置
     */
    private BackendConfig backend = new BackendConfig();

    /**
     * 设备端验证配置
     */
    private EdgeConfig edge = new EdgeConfig();

    /**
     * 验证模式配置
     */
    @Data
    public static class ModeConfig {
        /**
         * 默认验证模式（edge/backend）
         */
        private String defaultMode = "edge";

        /**
         * 是否支持后台验证
         */
        private Boolean backendEnabled = true;

        /**
         * 是否支持设备端验证
         */
        private Boolean edgeEnabled = true;
    }

    /**
     * 后台验证配置
     */
    @Data
    public static class BackendConfig {
        /**
         * 响应超时时间（毫秒）
         */
        private Integer timeout = 3000;

        /**
         * 是否启用反潜
         */
        private Boolean antiPassbackEnabled = true;

        /**
         * 反潜时间窗口（秒）
         */
        private Integer antiPassbackWindow = 300;

        /**
         * 是否启用互锁
         */
        private Boolean interlockEnabled = true;

        /**
         * 互锁超时（秒）
         */
        private Integer interlockTimeout = 60;

        /**
         * 是否启用多人验证
         */
        private Boolean multiPersonEnabled = false;

        /**
         * 多人验证超时（秒）
         */
        private Integer multiPersonTimeout = 120;
    }

    /**
     * 设备端验证配置
     */
    @Data
    public static class EdgeConfig {
        /**
         * 权限数据同步间隔（分钟）
         */
        private Integer syncInterval = 5;

        /**
         * 批量上传记录数量阈值
         */
        private Integer batchUploadThreshold = 100;

        /**
         * 批量上传时间间隔（秒）
         */
        private Integer batchUploadInterval = 60;

        /**
         * 离线验证支持
         */
        private Boolean offlineEnabled = true;
    }
}
