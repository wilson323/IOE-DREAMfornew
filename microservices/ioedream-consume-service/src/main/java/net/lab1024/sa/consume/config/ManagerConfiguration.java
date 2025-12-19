package net.lab1024.sa.consume.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.consume.client.AccountKindConfigClient;
import net.lab1024.sa.consume.dao.AccountDao;
import net.lab1024.sa.consume.dao.ConsumeAreaDao;
import net.lab1024.sa.consume.dao.ConsumeProductDao;
import net.lab1024.sa.consume.dao.ConsumeRecordDao;
import net.lab1024.sa.consume.dao.ConsumeTransactionDao;
import net.lab1024.sa.consume.dao.PaymentRecordDao;
import net.lab1024.sa.consume.manager.AccountManager;
import net.lab1024.sa.consume.manager.ConsumeAreaManager;
import net.lab1024.sa.consume.manager.ConsumeDeviceManager;
import net.lab1024.sa.consume.manager.ConsumeExecutionManager;
import net.lab1024.sa.consume.manager.ConsumeTransactionManager;
import net.lab1024.sa.consume.manager.MobileAccountInfoManager;
import net.lab1024.sa.consume.manager.MobileConsumeStatisticsManager;
import net.lab1024.sa.consume.manager.MultiPaymentManager;
import net.lab1024.sa.consume.manager.impl.AccountManagerImpl;
import net.lab1024.sa.consume.manager.impl.ConsumeAreaManagerImpl;
import net.lab1024.sa.consume.manager.impl.ConsumeDeviceManagerImpl;
import net.lab1024.sa.consume.manager.impl.ConsumeExecutionManagerImpl;
import net.lab1024.sa.consume.manager.impl.MultiPaymentManagerImpl;
import net.lab1024.sa.consume.service.ConsumeAreaCacheService;
import net.lab1024.sa.consume.service.impl.DefaultFixedAmountCalculator;
import net.lab1024.sa.consume.strategy.ConsumeAmountCalculatorFactory;

/**
 * Manager Bean配置类
 * <p>
 * 用于注册consume-service中的Manager Bean
 * 严格遵循CLAUDE.md规范：
 * - Manager实现类不使用Spring注解
 * - 通过配置类注册Bean
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 * @updated 2025-12-17 添加ConsumeTransactionManager Bean注册，修复Manager注解违规
 */
@Configuration
public class ManagerConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ManagerConfiguration.class);

    /**
     * 注册 AccountManager Bean
     */
    @Bean
    public AccountManager accountManager(AccountDao accountDao) {
        log.info("[ManagerConfiguration] 初始化AccountManager");
        return new AccountManagerImpl(accountDao);
    }

    /**
     * 注册 ConsumeAreaManager Bean
     */
    @Bean
    public ConsumeAreaManager consumeAreaManager(
            ConsumeAreaDao consumeAreaDao,
            ObjectMapper objectMapper,
            AccountManager accountManager,
            AccountKindConfigClient accountKindConfigClient) {
        log.info("[ManagerConfiguration] 初始化ConsumeAreaManager");
        return new ConsumeAreaManagerImpl(consumeAreaDao, objectMapper, accountManager, accountKindConfigClient);
    }

    /**
     * 注册 ConsumeDeviceManager Bean
     */
    @Bean
    public ConsumeDeviceManager consumeDeviceManager(
            GatewayServiceClient gatewayServiceClient,
            ObjectMapper objectMapper) {
        log.info("[ManagerConfiguration] 初始化ConsumeDeviceManager");
        return new ConsumeDeviceManagerImpl(gatewayServiceClient, objectMapper);
    }

    /**
     * 注册 ConsumeExecutionManager Bean
     */
    @Bean
    public ConsumeExecutionManager consumeExecutionManager(
            ConsumeAreaManager consumeAreaManager,
            ConsumeAreaCacheService consumeAreaCacheService,
            ConsumeDeviceManager consumeDeviceManager,
            GatewayServiceClient gatewayServiceClient,
            AccountKindConfigClient accountKindConfigClient,
            AccountManager accountManager,
            ConsumeTransactionDao consumeTransactionDao,
            ConsumeProductDao consumeProductDao,
            DefaultFixedAmountCalculator fixedAmountCalculator,
            ObjectMapper objectMapper,
            ConsumeAmountCalculatorFactory calculatorFactory) {
        log.info("[ManagerConfiguration] 初始化ConsumeExecutionManager");
        return new ConsumeExecutionManagerImpl(
                consumeAreaManager,
                consumeAreaCacheService,
                consumeDeviceManager,
                gatewayServiceClient,
                accountKindConfigClient,
                accountManager,
                consumeTransactionDao,
                consumeProductDao,
                fixedAmountCalculator,
                objectMapper,
                calculatorFactory);
    }

    /**
     * 注册 DefaultFixedAmountCalculator Bean
     */
    @Bean
    public DefaultFixedAmountCalculator defaultFixedAmountCalculator(ConsumeAreaManager consumeAreaManager) {
        log.info("[ManagerConfiguration] 初始化DefaultFixedAmountCalculator");
        return new DefaultFixedAmountCalculator(consumeAreaManager);
    }

    /**
     * 注册 MobileConsumeStatisticsManager Bean
     */
    @Bean
    public MobileConsumeStatisticsManager mobileConsumeStatisticsManager(GatewayServiceClient gatewayServiceClient) {
        log.info("[ManagerConfiguration] 初始化MobileConsumeStatisticsManager");
        return new MobileConsumeStatisticsManager(gatewayServiceClient);
    }

    /**
     * 注册 MobileAccountInfoManager Bean
     */
    @Bean
    public MobileAccountInfoManager mobileAccountInfoManager(GatewayServiceClient gatewayServiceClient) {
        log.info("[ManagerConfiguration] 初始化MobileAccountInfoManager");
        return new MobileAccountInfoManager(gatewayServiceClient);
    }

    /**
     * 注册 MultiPaymentManager Bean
     */
    @Bean
    public MultiPaymentManager multiPaymentManager(
            AccountManager accountManager,
            AccountDao accountDao,
            PaymentRecordDao paymentRecordDao,
            GatewayServiceClient gatewayServiceClient,
            RestTemplate restTemplate) {
        log.info("[ManagerConfiguration] 初始化MultiPaymentManager");

        // 从环境变量或配置中读取银行支付配置
        String bankGatewayUrl = System.getenv().getOrDefault("BANK_GATEWAY_URL", "https://api.bank.com/payment");
        String bankMerchantId = System.getenv().getOrDefault("BANK_MERCHANT_ID", "test_merchant_001");
        String bankApiKey = System.getenv().getOrDefault("BANK_API_KEY", "test_api_key_001");
        Boolean bankPaymentEnabled = Boolean
                .parseBoolean(System.getenv().getOrDefault("BANK_PAYMENT_ENABLED", "false"));
        Boolean creditLimitEnabled = Boolean.parseBoolean(System.getenv().getOrDefault("CREDIT_LIMIT_ENABLED", "true"));
        java.math.BigDecimal defaultCreditLimit = new java.math.BigDecimal(
                System.getenv().getOrDefault("DEFAULT_CREDIT_LIMIT", "1000.00"));

        return new MultiPaymentManagerImpl(
                accountManager,
                accountDao,
                paymentRecordDao,
                gatewayServiceClient,
                restTemplate,
                bankGatewayUrl,
                bankMerchantId,
                bankApiKey,
                bankPaymentEnabled,
                creditLimitEnabled,
                defaultCreditLimit);
    }

    /**
     * 注册 ConsumeTransactionManager Bean
     * <p>
     * 消费事务管理器，使用Seata分布式事务
     * 负责复杂的消费事务编排：账户验证、余额扣减、记录创建等
     * </p>
     *
     * @param consumeRecordDao     消费记录DAO
     * @param accountDao           账户DAO
     * @param gatewayServiceClient 网关服务客户端
     * @return ConsumeTransactionManager实例
     */
    @Bean
    public ConsumeTransactionManager consumeTransactionManager(
            ConsumeRecordDao consumeRecordDao,
            AccountDao accountDao,
            GatewayServiceClient gatewayServiceClient) {
        log.info("[ManagerConfiguration] 初始化ConsumeTransactionManager");
        return new ConsumeTransactionManager(consumeRecordDao, accountDao, gatewayServiceClient);
    }
}
