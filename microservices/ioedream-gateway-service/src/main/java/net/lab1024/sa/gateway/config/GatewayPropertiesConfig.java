package net.lab1024.sa.gateway.config;

import net.lab1024.sa.common.config.properties.IoeDreamGatewayProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 网关配置属性启用配置类
 * 
 * 由于GatewayProperties没有使用@Component注解（避免与Spring Cloud Gateway冲突），
 * 需要在网关服务中显式启用该配置属性类
 * 
 * @author IOE-DREAM Team
 * @date 2025-12-08
 */
@Configuration
@EnableConfigurationProperties(IoeDreamGatewayProperties.class)
public class GatewayPropertiesConfig {
    // 配置类，无需添加其他代码
    // Spring会自动将GatewayProperties注册为Bean
}
