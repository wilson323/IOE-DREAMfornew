package net.lab1024.sa.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;

/**
 * 网关配置属性
 *
 * <p>
 * <strong>迁移说明</strong>：此类已从microservices-common迁移到gateway-service，
 * 以消除gateway-service对microservices-common聚合模块的依赖。
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Data
@Component
@ConfigurationProperties(prefix = "ioe-dream.gateway")
public class IoeDreamGatewayProperties {
    private boolean enabled = true;
    private String whiteList;
    private String aesKey = "ioedream2025key";
}
