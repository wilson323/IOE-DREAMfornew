package net.lab1024.sa.admin.module.consume.engine;

import net.lab1024.sa.admin.module.consume.domain.entity.ConsumeRecordEntity;
import net.lab1024.sa.admin.module.consume.domain.dto.ConsumeRequestDTO;
import net.lab1024.sa.admin.module.consume.domain.dto.ConsumeResultDTO;

import java.math.BigDecimal;

/**
 * 消费模式接口
 *
 * <p>
 * 严格遵循repowiki规范:
 * - 定义消费模式的核心接口
 * - 支持不同的消费策略实现
 * - 提供统一的消费验证和处理流程
 * - 确保接口设计的一致性和可扩展性
 * </p>
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-17
 */
public interface ConsumeMode {

    /**
     * 获取消费模式类型
     *
     * @return 消费模式类型
     */
    String getModeType();

    /**
     * 获取消费模式名称
     *
     * @return 消费模式名称
     */
    String getModeName();

    /**
     * 获取消费模式描述
     *
     * @return 消费模式描述
     */
    String getModeDescription();

    /**
     * 验证消费请求
     *
     * @param consumeRequest 消费请求
     * @return 验证结果
     */
    ConsumeValidationResult validateConsume(ConsumeRequestDTO consumeRequest);

    /**
     * 执行消费处理
     *
     * @param consumeRequest 消费请求
     * @return 消费结果
     */
    ConsumeResultDTO processConsume(ConsumeRequestDTO consumeRequest);

    /**
     * 计算消费金额
     *
     * @param consumeRequest 消费请求
     * @return 消费金额
     */
    BigDecimal calculateAmount(ConsumeRequestDTO consumeRequest);

    /**
     * 检查消费条件
     *
     * @param consumeRequest 消费请求
     * @return 是否满足消费条件
     */
    boolean checkConsumeCondition(ConsumeRequestDTO consumeRequest);

    /**
     * 获取消费配置
     *
     * @return 消费配置
     */
    ConsumeModeConfig getConfig();

    /**
     * 设置消费配置
     *
     * @param config 消费配置
     */
    void setConfig(ConsumeModeConfig config);

    /**
     * 初始化消费模式
     */
    void initialize();

    /**
     * 销毁消费模式
     */
    void destroy();

    /**
     * 消费验证结果类
     */
    class ConsumeValidationResult {
        private boolean valid;
        private String message;
        private String errorCode;

        public ConsumeValidationResult(boolean valid, String message, String errorCode) {
            this.valid = valid;
            this.message = message;
            this.errorCode = errorCode;
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }

        public String getErrorCode() {
            return errorCode;
        }

        public static ConsumeValidationResult success() {
            return new ConsumeValidationResult(true, null, null);
        }

        public static ConsumeValidationResult fail(String message, String errorCode) {
            return new ConsumeValidationResult(false, message, errorCode);
        }
    }

    /**
     * 消费模式配置类
     */
    class ConsumeModeConfig {
        private boolean enabled;
        private BigDecimal maxAmount;
        private BigDecimal minAmount;
        private String description;
        private java.util.Map<String, Object> properties;

        public ConsumeModeConfig() {
            this.enabled = true;
            this.maxAmount = new BigDecimal("1000.00");
            this.minAmount = new BigDecimal("0.01");
            this.properties = new java.util.HashMap<>();
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public BigDecimal getMaxAmount() {
            return maxAmount;
        }

        public void setMaxAmount(BigDecimal maxAmount) {
            this.maxAmount = maxAmount;
        }

        public BigDecimal getMinAmount() {
            return minAmount;
        }

        public void setMinAmount(BigDecimal minAmount) {
            this.minAmount = minAmount;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public java.util.Map<String, Object> getProperties() {
            return properties;
        }

        public void setProperties(java.util.Map<String, Object> properties) {
            this.properties = properties;
        }

        public Object getProperty(String key) {
            return properties.get(key);
        }

        public void setProperty(String key, Object value) {
            properties.put(key, value);
        }
    }
}