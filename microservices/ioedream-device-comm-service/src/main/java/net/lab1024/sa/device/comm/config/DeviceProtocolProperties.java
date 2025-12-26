package net.lab1024.sa.device.comm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * 设备协议配置属性类
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用@ConfigurationProperties注解绑定配置
 * - 使用@Component注册为Spring Bean
 * - 配置项与application.yml中的device.protocol对应
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Component
@ConfigurationProperties(prefix = "device.protocol")
public class DeviceProtocolProperties {

    /**
     * TCP配置
     */
    private TcpConfig tcp = new TcpConfig();

    /**
     * UDP配置
     */
    private UdpConfig udp = new UdpConfig();

    /**
     * 线程池配置
     */
    private ThreadPoolConfig threadPool = new ThreadPoolConfig();

    /**
     * TCP配置
     */
    @Data
    public static class TcpConfig {
        /**
         * TCP推送服务器端口（用于接收设备推送数据）
         * 注意：使用18087避免与common-service的8088端口冲突
         */
        private Integer port = 18087;
    }

    /**
     * UDP配置
     */
    @Data
    public static class UdpConfig {
        /**
         * UDP推送服务器端口（可选）
         * 注意：使用18089避免与oa-service的8089端口冲突
         */
        private Integer port = 18089;
    }

    /**
     * 线程池配置
     */
    @Data
    public static class ThreadPoolConfig {
        /**
         * 核心线程数
         */
        private Integer coreSize = 10;

        /**
         * 最大线程数
         */
        private Integer maxSize = 50;

        /**
         * 队列容量
         */
        private Integer queueCapacity = 1000;

        /**
         * 线程存活时间（秒）
         */
        private Integer keepAliveSeconds = 60;
    }
}
