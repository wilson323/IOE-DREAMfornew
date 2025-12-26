package net.lab1024.sa.common.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * 网关配置属性
 *
 * <p>
 * <strong>迁移说明</strong>：此类已从`microservices-common-core`迁移到`microservices-common`，
 * 因为其依赖Spring框架（@ConfigurationProperties、@Component），不符合common-core的"最小稳定内核"原则。
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
