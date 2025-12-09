package net.lab1024.sa.consume.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.Resource;
import net.lab1024.sa.common.cache.CacheService;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.consume.dao.ConsumeRecordDao;
import net.lab1024.sa.common.consume.manager.MobileConsumeStatisticsManager;
import net.lab1024.sa.common.consume.manager.MobileAccountInfoManager;
import net.lab1024.sa.common.workflow.manager.WorkflowApprovalManager;
import net.lab1024.sa.consume.dao.AccountDao;
import net.lab1024.sa.consume.dao.ConsumeAreaDao;
import net.lab1024.sa.consume.report.dao.ConsumeReportTemplateDao;
import net.lab1024.sa.consume.manager.AccountManager;
import net.lab1024.sa.consume.manager.ConsumeAreaManager;
import net.lab1024.sa.consume.manager.ConsumeDeviceManager;
import net.lab1024.sa.consume.manager.ConsumeExecutionManager;
import net.lab1024.sa.consume.manager.MultiPaymentManager;
import net.lab1024.sa.consume.manager.impl.AccountManagerImpl;
import net.lab1024.sa.consume.manager.impl.ConsumeAreaManagerImpl;
import net.lab1024.sa.consume.manager.impl.ConsumeDeviceManagerImpl;
import net.lab1024.sa.consume.manager.impl.ConsumeExecutionManagerImpl;
import net.lab1024.sa.consume.manager.impl.MultiPaymentManagerImpl;
import net.lab1024.sa.consume.report.manager.ConsumeReportManager;
import net.lab1024.sa.consume.report.manager.impl.ConsumeReportManagerImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

/**
 * Manager配置类
 * <p>
 * 用于将Manager实现类注册为Spring Bean
 * 严格遵循CLAUDE.md规范：
 * - Manager类在microservices-common中是纯Java类，不使用Spring注解
 * - 在ioedream-consume-service中，通过配置类将Manager注册为Spring Bean
 * - Service层通过@Resource注入Manager实例（由Spring容器管理）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Configuration
public class ManagerConfiguration {

    @Resource
    private AccountDao accountDao;

    @Resource
    private ConsumeAreaDao consumeAreaDao;

    @Resource
    private ConsumeReportTemplateDao reportTemplateDao;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    @Resource
    private ConsumeRecordDao consumeRecordDao;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private CacheService cacheService;

    @Resource
    private net.lab1024.sa.consume.dao.ConsumeTransactionDao consumeTransactionDao;

    @Resource
    private net.lab1024.sa.consume.dao.ConsumeProductDao consumeProductDao;

    @Resource
    private net.lab1024.sa.consume.service.impl.DefaultFixedAmountCalculator fixedAmountCalculator;

    @Resource
    private net.lab1024.sa.consume.strategy.ConsumeAmountCalculatorFactory calculatorFactory;

    @Resource
    private net.lab1024.sa.consume.dao.PaymentRecordDao paymentRecordDao;

    @Resource
    private RestTemplate restTemplate;

    // 银行支付网关配置
    @Value("${bank.payment.gateway-url:}")
    private String bankGatewayUrl;

    @Value("${bank.payment.merchant-id:}")
    private String bankMerchantId;

    @Value("${bank.payment.api-key:}")
    private String bankApiKey;

    @Value("${bank.payment.enabled:false}")
    private Boolean bankPaymentEnabled;

    // 信用额度配置
    @Value("${credit.limit.enabled:false}")
    private Boolean creditLimitEnabled;

    @Value("${credit.limit.default-limit:0}")
    private java.math.BigDecimal defaultCreditLimit;

    /**
     * 注册AccountManager为Spring Bean
     *
     * @return AccountManager实例
     */
    @Bean
    public AccountManager accountManager() {
        return new AccountManagerImpl(accountDao);
    }

    /**
     * 注册ConsumeAreaManager为Spring Bean
     *
     * @return ConsumeAreaManager实例
     */
    @Bean
    public ConsumeAreaManager consumeAreaManager() {
        return new ConsumeAreaManagerImpl(
                consumeAreaDao,
                objectMapper,
                accountManager(),
                gatewayServiceClient,
                cacheService
        );
    }

    /**
     * 注册ConsumeDeviceManager为Spring Bean
     *
     * @return ConsumeDeviceManager实例
     */
    @Bean
    public ConsumeDeviceManager consumeDeviceManager() {
        return new ConsumeDeviceManagerImpl(gatewayServiceClient, objectMapper);
    }

    /**
     * 注册ConsumeExecutionManager为Spring Bean
     *
     * @return ConsumeExecutionManager实例
     */
    @Bean
    public ConsumeExecutionManager consumeExecutionManager() {
        return new ConsumeExecutionManagerImpl(
                consumeAreaManager(),
                consumeDeviceManager(),
                gatewayServiceClient,
                accountManager(),
                consumeTransactionDao,
                consumeProductDao,
                fixedAmountCalculator,
                objectMapper,
                calculatorFactory
        );
    }

    /**
     * 注册ConsumeReportManager为Spring Bean
     *
     * @return ConsumeReportManager实例
     */
    @Bean
    public ConsumeReportManager consumeReportManager() {
        return new ConsumeReportManagerImpl(
                reportTemplateDao,
                consumeTransactionDao,
                objectMapper
        );
    }

    /**
     * 注册WorkflowApprovalManager为Spring Bean
     * <p>
     * 供消费模块使用，用于启动充值退款、报销等审批流程
     * </p>
     *
     * @return WorkflowApprovalManager实例
     */
    @Bean
    public WorkflowApprovalManager workflowApprovalManager() {
        return new WorkflowApprovalManager(gatewayServiceClient);
    }

    /**
     * 注册MultiPaymentManager为Spring Bean
     * <p>
     * 用于管理多种支付方式的统一调用和信用额度扣除
     * </p>
     *
     * @return MultiPaymentManager实例
     */
    @Bean
    public MultiPaymentManager multiPaymentManager() {
        return new MultiPaymentManagerImpl(
                accountManager(),
                accountDao,
                paymentRecordDao,
                gatewayServiceClient,
                restTemplate,
                bankGatewayUrl,
                bankMerchantId,
                bankApiKey,
                bankPaymentEnabled,
                creditLimitEnabled,
                defaultCreditLimit
        );
    }

    /**
     * 注册移动端消费统计Manager为Spring Bean
     *
     * @return MobileConsumeStatisticsManager实例
     */
    @Bean
    public MobileConsumeStatisticsManager mobileConsumeStatisticsManager() {
        return new MobileConsumeStatisticsManager(gatewayServiceClient);
    }

    /**
     * 注册移动端账户信息Manager为Spring Bean
     *
     * @return MobileAccountInfoManager实例
     */
    @Bean
    public MobileAccountInfoManager mobileAccountInfoManager() {
        return new MobileAccountInfoManager(gatewayServiceClient);
    }

    /**
     * 注册RestTemplate为Spring Bean
     * <p>
     * 用于HTTP客户端调用（银行支付网关等）
     * </p>
     *
     * @return RestTemplate实例
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
