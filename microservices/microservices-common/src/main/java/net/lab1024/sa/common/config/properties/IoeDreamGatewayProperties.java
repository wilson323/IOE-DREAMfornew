package net.lab1024.sa.common.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
// ❌ 移除@Component注解，避免与Spring Cloud Gateway自动配置冲突
// import org.springframework.stereotype.Component;

/**
 * 网关配置属性
 * 
 * 注意：此类不使用@Component注解，避免与Spring Cloud Gateway的自动配置类冲突
 * Spring Cloud Gateway会自动创建名为gatewayProperties的Bean
 * 
 * @author IOE-DREAM Team
 * @date 2025-01-30
 */
@Data
// ❌ 移除@Component注解
// @Component
@ConfigurationProperties(prefix = "ioedream.gateway")  // 修改prefix避免冲突
public class IoeDreamGatewayProperties {

    /**
     * 网关服务地址
     * 默认: http://localhost:8080
     */
    private String url = "http://localhost:8080";
}
