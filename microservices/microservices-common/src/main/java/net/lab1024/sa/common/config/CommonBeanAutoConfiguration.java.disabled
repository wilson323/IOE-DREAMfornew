package net.lab1024.sa.common.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.gateway.DirectServiceClient;
import net.lab1024.sa.common.gateway.GatewayServiceClient;

/**
 * 公共Bean自动装配配置
 * <p>
 * 统一管理所有微服务都需要的基础Bean，包括：
 * - CacheService: 缓存服务接口和实现
 * - GatewayServiceClient: 网关服务客户端
 * - RestTemplate: HTTP客户端
 * - DirectServiceClient: 直连服务客户端（可选）
 * - NotificationManager: 通知管理器
 * - WorkflowApprovalManager: 工作流审批管理器
 * - ApprovalConfigManager: 审批配置管理器
 * </p>
 * <p>
 * 使用 @ConditionalOnMissingBean 确保只在没有同名Bean时才创建，
 * 避免与各服务的自定义配置冲突
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-01-30
 * @updated 2025-12-14 添加更多公共Bean统一装配
 */
@Slf4j
@AutoConfiguration
public class CommonBeanAutoConfiguration {

    @Value("${spring.cloud.gateway.url:http://localhost:8080}")
    private String gatewayUrl;

    @Value("${spring.application.name:unknown}")
    private String applicationName;

    @Value("${ioedream.direct-call.shared-secret:}")
    private String directCallSharedSecret;

    @Value("${ioedream.direct-call.enabled:false}")
    private boolean directCallEnabled;

    /**
     * 注册 RestTemplate Bean
     * <p>
     * 如果服务中没有自定义的RestTemplate，则创建默认的
     * </p>
     */
    @Bean
    @ConditionalOnMissingBean
    public RestTemplate restTemplate() {
        log.info("[公共Bean] 初始化默认HTTP客户端");
        return new RestTemplate();
    }

    /**
     * 注册 ObjectMapper Bean
     * <p>
     * 如果服务中没有自定义的ObjectMapper，则创建默认的
     * </p>
     */
    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        log.info("[公共Bean] 初始化默认ObjectMapper");
        return new ObjectMapper();
    }

    /**
     * 注册 GatewayServiceClient Bean
     * <p>
     * 所有微服务都需要的服务间调用客户端，统一配置
     * </p>
     */
    @Bean
    @ConditionalOnMissingBean
    public GatewayServiceClient gatewayServiceClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        log.info("[公共Bean] 配置默认网关客户端，网关URL: {}", gatewayUrl);
        return new GatewayServiceClient(restTemplate, objectMapper, gatewayUrl);
    }

    // CacheService Bean 已移至各业务服务的配置类中
    // 网关服务是 WebFlux 应用，不需要 RedisTemplate

    // ==================== 工作流相关Bean ====================
    // 注意：ApprovalConfigManager 和 NotificationManager 等依赖 MyBatis-Plus 的 Bean
    // 已移至 MybatisDependentBeanAutoConfiguration 类中，避免网关等 WebFlux 服务加载时报错

    /**
     * 注册 WorkflowApprovalManager Bean
     * <p>
     * 工作流审批管理器，供各业务模块使用
     * 支持启动审批流程、处理审批任务、查询审批状态等功能
     * </p>
     * <p>
     * 条件：仅在类路径中存在 WorkflowApprovalManager 时注册
     * 网关服务等不需要工作流的服务会自动排除
     * </p>
     * <p>
     * 注意：此Bean的注册已移至 MybatisDependentBeanAutoConfiguration 中
     * 因为 WorkflowApprovalManager 依赖 ApprovalConfigManager，而 ApprovalConfigManager 依赖 MyBatis-Plus
     * 网关服务等 WebFlux 服务不需要数据库依赖，因此不应注册此Bean
     * </p>
     */
    // 已移除：WorkflowApprovalManager Bean注册
    // 原因：WorkflowApprovalManager 依赖 ApprovalConfigManager，而 ApprovalConfigManager 依赖 MyBatis-Plus
    // 网关服务等 WebFlux 服务不需要数据库依赖，因此不应在 CommonBeanAutoConfiguration 中注册
    // 此Bean的注册已移至 MybatisDependentBeanAutoConfiguration 中

    // ==================== 直连调用相关Bean ====================

    /**
     * 注册 DirectServiceClient Bean
     * <p>
     * 直连服务客户端，用于东西向热路径直连调用
     * 仅在 ioedream.direct-call.enabled=true 时启用
     * </p>
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "ioedream.direct-call.enabled", havingValue = "true", matchIfMissing = false)
    @ConditionalOnBean(DiscoveryClient.class)
    public DirectServiceClient directServiceClient(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            DiscoveryClient discoveryClient,
            ObjectProvider<RetryRegistry> retryRegistry,
            ObjectProvider<CircuitBreakerRegistry> circuitBreakerRegistry,
            ObjectProvider<MeterRegistry> meterRegistry) {
        log.info("[公共Bean] 初始化直连服务客户端，服务名: {}, 启用状态: {}", applicationName, directCallEnabled);
        return new DirectServiceClient(
                restTemplate,
                objectMapper,
                discoveryClient,
                applicationName,
                directCallSharedSecret,
                directCallEnabled,
                retryRegistry.getIfAvailable(),
                circuitBreakerRegistry.getIfAvailable(),
                meterRegistry.getIfAvailable()
        );
    }
}
